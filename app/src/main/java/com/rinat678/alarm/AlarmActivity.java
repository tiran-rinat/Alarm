package com.rinat678.alarm;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

public class AlarmActivity extends BaseActivity
        implements View.OnClickListener
{


    private AlarmListAdapter alarmListAdapter;
    private Date dateClose = new Date(0);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(this.getClass().getSimpleName(), "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newAlarmIntent = new Intent(getApplicationContext(), AlarmPreferencesActivity.class);
                startActivity(newAlarmIntent);
            }
        });

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.setDrawerListener(toggle);
//        toggle.syncState();
//
//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
//        navigationView.setVisibility(View.INVISIBLE);

        ListView alarmListView = (ListView) findViewById(android.R.id.list);
        alarmListView.setLongClickable(true);
        alarmListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                final Alarm alarm = (Alarm) alarmListAdapter.getItem(position);
                Builder dialog = new AlertDialog.Builder(AlarmActivity.this);
                dialog.setTitle("Delete");
                dialog.setMessage("Delete this alarm?");
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Database.Companion.init(AlarmActivity.this);
                        Database.Companion.deleteEntry(alarm);
                        AlarmActivity.this.callAlarmScheduleService();

                        updateAlarmList();
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                dialog.show();

                return true;
            }
        });

        callAlarmScheduleService();

        alarmListAdapter = new AlarmListAdapter(this);
        alarmListView.setAdapter(alarmListAdapter);
        alarmListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {

                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Alarm alarm = (Alarm) alarmListAdapter.getItem(position);
                Intent intent = new Intent(AlarmActivity.this, AlarmPreferencesActivity.class);
                intent.putExtra("alarm", alarm);
                startActivity(intent);
            }

        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Date newDate = new Date();
            if (newDate.getTime() - dateClose.getTime() < 2000) {
                super.onBackPressed();
            }
            else {
                Toast.makeText(getApplicationContext(),"Нажмите снова для выхода",Toast.LENGTH_SHORT).show();
                dateClose = new Date();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d(this.getClass().getSimpleName(), "options item selected");
        switch (item.getItemId()) {
            case R.id.action_delete:
                Builder dialog = new AlertDialog.Builder(AlarmActivity.this);
                dialog.setTitle("Delete");
                dialog.setMessage("Delete all alarms?");
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Database.Companion.init(AlarmActivity.this);
                        Database.Companion.deleteAll();
                        AlarmActivity.this.callAlarmScheduleService();

                        updateAlarmList();
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                dialog.show();

            return true;
            case R.id.menu_item_new:
                Intent newAlarmIntent = new Intent(this, AlarmPreferencesActivity.class);
                startActivity(newAlarmIntent);
                break;
            default:break;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(getClass().getSimpleName(), "onCreate Options menu");
        // Inflate the menu; this adds items to the action bar if it is present.
        boolean result = super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.menu_item_save).setVisible(false);
        return result;
    }

    @Override
    protected void onPause() {
        Log.d(getClass().getSimpleName(), "onPause");
        // setListAdapter(null);
        Database.Companion.deactivate();
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(getClass().getSimpleName(), "onResume");
        super.onResume();
        updateAlarmList();
    }

    private void updateAlarmList(){
        Log.d(getClass().getSimpleName(), "updateAlarmList");
        Database.Companion.init(AlarmActivity.this);
        final List<Alarm> alarms = Database.Companion.getAll();
        alarmListAdapter.setAlarms(alarms);

        runOnUiThread(new Runnable() {
            public void run() {
                // reload content
                AlarmActivity.this.alarmListAdapter.notifyDataSetChanged();
                if(alarms.size() > 0){
                    findViewById(android.R.id.empty).setVisibility(View.INVISIBLE);
                }else{
                    findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        Log.d(getClass().getSimpleName(), "onClick");
        if (v.getId() == R.id.checkBox_alarm_active) {
            Switch aSwitch = (Switch) v;
            Alarm alarm = (Alarm) alarmListAdapter.getItem((Integer) aSwitch.getTag());
            alarm.setAlarmActive(aSwitch.isChecked());
            Database.Companion.update(alarm);
            AlarmActivity.this.callAlarmScheduleService();
            if (aSwitch.isChecked()) {
                Toast.makeText(AlarmActivity.this, alarm.getTimeUntilNextAlarmMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
