 package com.scholarscraper.alarm;
 
 import java.util.Calendar;
 import android.content.SharedPreferences;
 import com.scholarscraper.SettingsFragment;
 import com.scholarscraper.update.DataManager;
 import java.util.Date;
 import java.text.SimpleDateFormat;
 import android.net.Uri;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Intent;
 import com.scholarscraper.listview.TaskListComparator;
 import com.scholarscraper.model.Task;
 import java.util.ArrayList;
 import java.util.Collections;
 import android.content.Context;
 import com.scholarscraper.model.Course;
 import java.util.List;
 
 /**
 // -------------------------------------------------------------------------
 /**
  *  This class handles alarm management for the app. It operates under a
  *  one-alarm-at-a-time principle, so it will only set alarms for a user's
  *  nearest upcoming assignment (however, if multiple assignments share the
  *  same due date then it will still set alarms for all of them).
  *
  *
  *  @author Alex
  *  @version Nov 19, 2013
  */
 public class AlarmSetter
 {
     //identifier strings for passing extra intent data
     public static final String DUE_DATE_EXTRA = "dueDate";
     public static final String ASSIGNMENT_NAME_EXTRA = "assignmentName";
     public static final String COURSE_NAME_EXTRA = "courseName";
 
     public static void setNextAlarm(Context context) {
         List<Course> courses = DataManager.recoverCourses(context);
         if (courses == null) {
             System.out.println("no alarms set, failed to retrieve courses");
             return;
         }
         List<Task> sortedTasks = flattenCourseList(courses, context);
         if (sortedTasks.size() == 0) {
             System.out.println("No alarms to set");
             return;
         }
         Task nextTask = sortedTasks.get(0);
         if (nextTask.getDueDate() == null) {
             System.out.println(nextTask.getName() + " has an indefinite due date, no alarms set");
             return;
         }
         enableAlarm(sortedTasks, context);
     }
 
     /**
      * Use this method if the course list is already readily known, avoids IO
      */
     public static void setNextAlarm(Context context, List<Course> courses) {
         if (courses == null) {
             System.out.println("no alarms set, courselist is null");
             return;
         }
         List<Task> sortedTasks = flattenCourseList(courses, context);
         if (sortedTasks.size() == 0) {
             System.out.println("No alarms to set");
             return;
         }
         Task nextTask = sortedTasks.get(0);
         if (nextTask.getDueDate() == null) {
             System.out.println(nextTask.getName() + " has an indefinite due date, no alarms set");
             return;
         }
         enableAlarm(sortedTasks, context);
     }
 
     /**
      * Cancels any alarms that match the given alarmId. Note that this will
      * not prevent future alarms from being set, and will only cancel the
      * current alarm in the queue as long as it matches the given alarmId.
      */
     public static void cancelAlarm(Context context, long alarmId) {
         Intent intent = buildCancellingIntent(alarmId);
         PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
         //TODO do testing on this to verify intent filtering works
         AlarmManager alarmManager = (AlarmManager)(context.getSystemService(Context.ALARM_SERVICE));
         alarmManager.cancel(pIntent);
     }
 
     /**
      * Making this recursive so we can easily set sequential alarms that have
      * the same due date, each call sets an alarm for the current task in the
      * task list, and we stop once nextTask's due date differs from currentTask's
      */
     private static void enableAlarm(List<Task> sortedTasks, Context context) {
         if (sortedTasks.size() == 0) {
             System.out.println("Tasklist is empty, no alarms set");
             return;
         }
         enableAlarm(sortedTasks, 0, context); //always start with first task in the list
     }
 
     private static void enableAlarm(List<Task> sortedTasks, int index, Context context) {
         Task currentTask = sortedTasks.get(index);
         if (currentTask.getDueDate() == null) {
             System.out.println(currentTask.getName() + " has an indefinite due date, no alarms set");
             return;
         }
         if (index + 1 < sortedTasks.size() &&
             sortedTasks.get(index + 1).getDueDate() != null &&
             sortedTasks.get(index + 1).getDueDate().equals(currentTask.getDueDate()))
         {
             enableAlarm(sortedTasks, index + 1, context);
         }
         setAlarm(currentTask, context);
     }
 
     private static void setAlarm(Task task, Context context) {
         Intent intent = buildIntent(task);
         PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
 
         //TODO with the way pending intents interact with the alarm manager, if duplicate
         //alarms get set for the same task then the previous one should get cancelled
         //(regardless of the time the alarm is set for, useful if the user changes
         //what time the alarms should go off). Verify that this actually works as intended.
         AlarmManager alarmManager = (AlarmManager)(context.getSystemService(Context.ALARM_SERVICE));
         long alarmTime = calculateAlarmTime(task, context);
         long currentTime = System.currentTimeMillis();
         //we are not going to set retroactive alarms because it can cause an infinite
         //loop dynamic between the AlarmSetter and the AlarmReceiver
         if (alarmTime > currentTime) {
             alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pIntent);
 
             SimpleDateFormat formatter = new SimpleDateFormat("h:mm a, ddd");
             String date = formatter.format(new Date(alarmTime));
             System.out.println("Alarm set for " + task + " at time " + date);
         }
         else {
             System.out.println("Alarm not set because its trigger time has already past");
         }
     }
 
     private static Intent buildIntent(Task task) {
         Uri uri = Uri.parse("android.resource://com.scholarscraper/alarmId/?" + task.getUniqueId());
 
         Intent intent = new Intent("com.scholarscraper.ALARM_ALERT", uri);
 
 
         SimpleDateFormat formatter = new SimpleDateFormat("EEEE 'at' h:mm a");
         String date = formatter.format(task.getDueDate().getTime());
 
         intent.putExtra(Task.class.getName() + "." + DUE_DATE_EXTRA, date);
         intent.putExtra(Task.class.getName() + "." + COURSE_NAME_EXTRA, task.getCourseName());
         intent.putExtra(Task.class.getName() + "." + ASSIGNMENT_NAME_EXTRA, task.getName());
         return intent;
     }
 
     /**
      * When passed through the alarm manager, these intents will cancel out any
      * existing alarms that match the given uniqueId.
      */
     private static Intent buildCancellingIntent(long uniqueId) {
         Uri uri = Uri.parse("android.resource://com.scholarscraper/alarmId/?" + uniqueId);
 
         return new Intent("com.scholarscraper.ALARM_ALERT", uri);
     }
 
     /**
      * Calculates the time (in milliseconds) that the alarm setter should set
      * for the given task. Takes into account whether or not the user wants to
      * be notified at night.
      */
     private static long calculateAlarmTime(Task task, Context context) {
         SharedPreferences sharedPreferences = context.getSharedPreferences(SettingsFragment.PREFERENCES,
             Context.MODE_PRIVATE);
         boolean cancelNightAlarms = sharedPreferences.getBoolean(SettingsFragment.CANCEL_NIGHT_ALARMS, true);
         int offset = retrieveOffset(context);
         Calendar dueDate = task.getDueDate();
         long alarmTime = dueDate.getTimeInMillis() - offset;
 
         if (cancelNightAlarms) {
            //with this modulus we will get a value from 0-23, subtracting 5 from
            //the alarmTime to account for EST offset, this will probably be off by an
            //hour once we hit daylight savings time. //TODO fix daylight savings issue
            long alarmHourOfDay = ((alarmTime - 5 * 1000 * 3600) % (24 * 1000 * 3600)) / (1000 * 3600) ;
             if (alarmHourOfDay > SettingsFragment.LOW_CUTOFF) {
                 Calendar c = Calendar.getInstance();
                 c.setTimeInMillis(alarmTime);
                 c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE),
                       SettingsFragment.LOW_CUTOFF, 0 , 0);
                 return c.getTimeInMillis();
             }
             else if (alarmHourOfDay < SettingsFragment.HIGH_CUTOFF) {
                 Calendar c = Calendar.getInstance();
                 c.setTimeInMillis(alarmTime);
                 //subtract date by 1 here, we want to set the alarm back in time, not forward
                 c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE) - 1,
                       SettingsFragment.LOW_CUTOFF, 0 , 0);
                 return c.getTimeInMillis();
             }
         }
         return alarmTime;
     }
 
     /**
      * returns the current alarm offset in milliseconds
      */
     private static int retrieveOffset(Context context) {
         SharedPreferences sharedPreferences = context.getSharedPreferences(SettingsFragment.PREFERENCES,
             Context.MODE_PRIVATE);
         int offset = sharedPreferences.getInt(SettingsFragment.OFFSET, SettingsFragment.DEFAULT_OFFSET);
         return offset * 3600 * 1000; //from hours to milliseconds
     }
 
     /**
      * Flattens the list of courses into an array of sorted tasks
      */
     private static List<Task> flattenCourseList(List<Course> courses, Context context)
     {
         int offset = retrieveOffset(context);
         List<Task> flattenedList = new ArrayList<Task>();
 
         // add all tasks that aren't older than some constant
         long currentTime = System.currentTimeMillis();
         for (Course course : courses)
         {
             for (Task task : course.getAssignments())
             {
                 if (task.getDueDate() == null)
                 {
                     flattenedList.add(task);
                 }
                 else if (task.getDueDate().getTimeInMillis() >= currentTime + offset)
                 {
                     flattenedList.add(task);
                 }
             }
         }
         Collections.sort(flattenedList, new TaskListComparator());
         return flattenedList;
     }
 }
