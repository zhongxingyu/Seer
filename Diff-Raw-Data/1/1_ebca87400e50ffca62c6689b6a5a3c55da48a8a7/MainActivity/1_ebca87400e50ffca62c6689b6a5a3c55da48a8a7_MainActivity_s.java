 package com.timetracker.ui;
 
 import android.app.*;
 import android.content.*;
 import android.content.res.Resources;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
 import com.j256.ormlite.dao.GenericRawResults;
 import com.timetracker.R;
 import com.timetracker.domain.Task;
 import com.timetracker.domain.TaskContext;
 import com.timetracker.domain.TaskSwitchEvent;
 import com.timetracker.domain.persistance.DatabaseHelper;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class MainActivity extends OrmLiteBaseActivity<DatabaseHelper> {
     public static final long ONE_MINUTE = 60 * 1000;
     public static final int POMODORO_NOTIFICATION_ID = 0;
     public static final int CURRENT_TASK_NOTIFICATION_ID = 1;
 
     PendingIntent pomodoroIntent;
     BroadcastReceiver pomodoroBroadcastReceiver;
     AlarmManager alarmManager;
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         initContextSpinner();
         loadTaskList();
         refreshTimer();
         initTimelineReportButton();
         initComparisonReportButton();
         initContextCreationButton();
         initTaskCreationButton();
         initRemoveContextButton();
         initPomodoroTimer();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         initContextSpinner();
         loadTaskList();
 
         NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
         notificationManager.cancel(POMODORO_NOTIFICATION_ID);
     }
 
     protected void onDestroy() {
         alarmManager.cancel(pomodoroIntent);
         unregisterReceiver(pomodoroBroadcastReceiver);
         super.onDestroy();
     }
 
     private void initTimelineReportButton() {
         Button button = (Button) findViewById(R.id.showTimelineReportButton);
         button.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(MainActivity.this, TimelineReportActivity.class);
                 startActivity(intent);
             }
         });
     }
 
     private void initComparisonReportButton() {
         Button button = (Button) findViewById(R.id.showComparisonReportButton);
         button.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(MainActivity.this, ComparisonReportActivity.class);
                 startActivity(intent);
             }
         });
     }
 
     private void initContextSpinner() {
         Spinner spinner = (Spinner) findViewById(R.id.spinner);
         try {
             List<TaskContext> contexts = getHelper().getContextDao().queryBuilder().orderBy("name", true)
                     .where().eq("isDeleted", Boolean.FALSE).query();
             ArrayAdapter<TaskContext> dataAdapter = new ArrayAdapter<>(this,
                     android.R.layout.simple_spinner_item, contexts);
             dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
             spinner.setAdapter(dataAdapter);
             spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                 @Override
                 public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                     loadTaskList();
                 }
 
                 @Override
                 public void onNothingSelected(AdapterView<?> adapterView) {
 
                 }
             });
             TaskSwitchEvent switchEvent = lastTaskSwitch();
             if (switchEvent != null) {
                 TaskContext currentContext = switchEvent.task.context;
                 spinner.setSelection(contexts.indexOf(currentContext));
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void loadTaskList() {
         try {
             TaskContext context = getCurrentContext();
             Integer id = context != null ? context.id : null;
             List<Task> queryResult;
             if (context == null) {
                 queryResult = new ArrayList<>();
             } else {
                 queryResult = getHelper().getTaskDao().queryBuilder().orderBy("name", true)
                         .where().eq("context_id", id).and().eq("isDeleted", Boolean.FALSE).query();
             }
             final List<Task> tasks = queryResult;
             ListAdapter adapter = new BaseAdapter() {
                 @Override
                 public int getCount() {
                     return tasks.size();
                 }
 
                 @Override
                 public Object getItem(int position) {
                     return tasks.get(position);
                 }
 
                 @Override
                 public long getItemId(int position) {
                     return position;
                 }
 
                 @Override
                 public View getView(int position, View convertView, ViewGroup parent) {
                     View row = convertView;
 
                     if (row == null) {
                         LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                         row = inflater.inflate(R.layout.task_list_item, parent, false);
                     }
 
                     final Task task = tasks.get(position);
                     row.setTag(task);
                     TextView taskName = (TextView) row.findViewById(R.id.taskName);
                     taskName.setText(task.name);
 
                     Button taskStartButton = (Button) row.findViewById(R.id.startTask);
                     taskStartButton.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             startTask(task);
                             refreshTimer();
                         }
                     });
 
                     Button removeTaskButton = (Button) row.findViewById(R.id.removeTaskButton);
                     removeTaskButton.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             showRemoveDialog(task);
                         }
                     });
 
                     row.setOnLongClickListener(new View.OnLongClickListener() {
                         @Override
                         public boolean onLongClick(View v) {
 
                             Intent intent = new Intent(MainActivity.this, TaskCreationActivity.class);
                             intent.putExtra(TaskCreationActivity.CONTEXT_ID, getCurrentContext().id);
                             intent.putExtra(TaskCreationActivity.TASK_ID, task.id);
                             startActivity(intent);
                             return false;
                         }
                     });
                     return row;
                 }
             };
             ListView listView = (ListView) findViewById(R.id.listView);
             listView.setAdapter(adapter);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void showRemoveDialog(final Task task) {
         Resources res = getResources();
         String msg = String.format(res.getString(R.string.removeContextDialogText, task.name));
         AlertDialog dialog = new AlertDialog.Builder(this).setMessage(msg)
                 .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         removeTask(task);
                         loadTaskList();
                         dialog.dismiss();
                     }
                 }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.dismiss();
                     }
                 }).create();
         dialog.show();
     }
 
     private TaskContext getCurrentContext() {
         Spinner contextSpinner = (Spinner) findViewById(R.id.spinner);
         return (TaskContext) contextSpinner.getSelectedItem();
     }
 
     private void refreshTimer() {
         TaskSwitchEvent lastEvent = lastTaskSwitch();
         if (lastEvent != null) {
             EditText taskName = (EditText) findViewById(R.id.currentTaskName);
             taskName.setText(lastEvent.task.name);
 
             Chronometer chronometer = (Chronometer) findViewById(R.id.chronometer);
             chronometer.start();
             chronometer.setBase(SystemClock.elapsedRealtime() - (new Date().getTime() - lastEvent.switchTime.getTime()));
         } else {
             Log.i(this.getClass().getName(), "Started the first task!");
         }
     }
 
     private void initContextCreationButton() {
         Button button = (Button) findViewById(R.id.createContextButton);
         button.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(MainActivity.this, ContextCreationActivity.class);
                 startActivity(intent);
             }
         });
     }
 
     private void initTaskCreationButton() {
         Button button = (Button) findViewById(R.id.createTaskButton);
         button.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(MainActivity.this, TaskCreationActivity.class);
                 intent.putExtra(TaskCreationActivity.CONTEXT_ID, getCurrentContext().id);
                 intent.putExtra(TaskCreationActivity.TASK_ID, -1);
                 startActivity(intent);
             }
         });
     }
 
     private void initRemoveContextButton() {
         Button removeContextButton = (Button) findViewById(R.id.removeContextButton);
         removeContextButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 final TaskContext context = getCurrentContext();
                 showRemoveDialog(context);
             }
         });
     }
 
     private void showRemoveDialog(final TaskContext context) {
         Resources res = getResources();
         String msg = String.format(res.getString(R.string.removeContextDialogText, context.name));
         AlertDialog dialog = new AlertDialog.Builder(this).setMessage(msg)
                 .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         removeTaskContext(context);
                         initContextSpinner();
                         dialog.dismiss();
                     }
                 }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.dismiss();
                     }
                 }).create();
         dialog.show();
     }
 
     private void startTask(Task task) {
         try {
             TaskSwitchEvent lastSwitchEvent = lastTaskSwitch();
             if (lastSwitchEvent != null && lastSwitchEvent.task.id.equals(task.id)) {
                 return;
             }
             TaskSwitchEvent event = new TaskSwitchEvent();
             event.task = task;
             event.switchTime = new Date();
             getHelper().getEventsDao().create(event);
 
             stopPomodoro();
             if (task.pomodoroDuration != 0) {
                 startPomodoro(task.pomodoroDuration);
             }
             showCurrentTaskNotification(this, task);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void initPomodoroTimer() {
         pomodoroBroadcastReceiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context c, Intent i) {
                 showPomodoroNotification(c);
             }
         };
         String action = "com.timetracker.pomodoroEnd";
         registerReceiver(pomodoroBroadcastReceiver, new IntentFilter(action));
         pomodoroIntent = PendingIntent.getBroadcast(this, 0, new Intent(action),0);
         alarmManager = (AlarmManager) (this.getSystemService(Context.ALARM_SERVICE));
     }
 
     private void showPomodoroNotification(Context c) {
         Intent intent = new Intent(c, MainActivity.class);
         PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
 
         Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
 
         Notification noti = new Notification.Builder(this)
                 .setContentTitle("Pomodoro finished")
                 .setContentText("Click to open tracker")
                 .setSmallIcon(R.drawable.check)
                 .setSound(soundUri)
                 .setContentIntent(pIntent)
                 .build();
 
         NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
         noti.flags |= Notification.FLAG_AUTO_CANCEL;
 
         notificationManager.notify(POMODORO_NOTIFICATION_ID, noti);
     }
 
     private void showCurrentTaskNotification(Context c, Task task) {
         Intent intent = new Intent(c, MainActivity.class);
         PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
 
         Notification notification = new Notification.Builder(this)
                 .setContentTitle("Working on " + task.name)
                 .setContentText("Click to open tracker")
                 .setSmallIcon(R.drawable.clock)
                 .setContentIntent(pIntent)
                 .build();
 
         NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
         notification.flags |= Notification.FLAG_NO_CLEAR;
 
         notificationManager.cancel(CURRENT_TASK_NOTIFICATION_ID);
         notificationManager.notify(CURRENT_TASK_NOTIFICATION_ID, notification);
     }
 
     private void startPomodoro(int durationMinutes) {
         alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + durationMinutes * ONE_MINUTE, pomodoroIntent);
     }
 
     private void stopPomodoro() {
         if (pomodoroIntent != null) {
             alarmManager.cancel(pomodoroIntent);
         }
     }
 
     private TaskSwitchEvent lastTaskSwitch() {
         try {
             GenericRawResults<String[]> results = getHelper().getEventsDao()
                     .queryRaw("select id from task_switch_events where switchTime = (select max(switchTime) from task_switch_events)");
 
             String[] firstResult = results.getFirstResult();
             if (firstResult == null) {
                 return null;
             } else {
                 int lastEventId = Integer.valueOf(firstResult[0]);
                 return getHelper().getEventsDao().queryForId(lastEventId);
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void removeTaskContext(TaskContext context) {
         context.isDeleted = true;
         try {
             getHelper().getContextDao().update(context);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void removeTask(Task task) {
         task.isDeleted = true;
         try {
             getHelper().getTaskDao().update(task);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 }
