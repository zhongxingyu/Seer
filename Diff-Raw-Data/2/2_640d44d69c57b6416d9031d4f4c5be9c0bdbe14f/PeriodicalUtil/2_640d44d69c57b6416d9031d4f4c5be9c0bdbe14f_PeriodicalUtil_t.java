 package jp.gr.java_conf.neko_daisuki.simplemediascanner;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashSet;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 import jp.gr.java_conf.neko_daisuki.simplemediascanner.Database.Schedule;
 
 public class PeriodicalUtil {
 
     private static class Now {
 
         private Calendar mCalendar;
         private int mSecondOfDay;
 
         public Now() {
             mCalendar = Calendar.getInstance();
             int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
             int minute = mCalendar.get(Calendar.MINUTE);
             int second = mCalendar.get(Calendar.SECOND);
             mSecondOfDay = 60 * (60 * hour + minute) + second;
         }
 
         public int getYear() {
             return mCalendar.get(Calendar.YEAR);
         }
 
         public int getMonth() {
             return mCalendar.get(Calendar.MONTH) + 1;
         }
 
         public int getDay() {
             return mCalendar.get(Calendar.DAY_OF_MONTH);
         }
 
         public int getHour() {
             return mCalendar.get(Calendar.HOUR_OF_DAY);
         }
 
         public int getMinute() {
             return mCalendar.get(Calendar.MINUTE);
         }
 
         public int getSecondOfDay() {
             return mSecondOfDay;
         }
     }
 
     private static class MostRecentPair {
 
         private static class MostRecentPairComparator implements Comparator<MostRecentPair> {
 
             @Override
             public int compare(MostRecentPair lhs, MostRecentPair rhs) {
                 return lhs.getCalendar().compareTo(rhs.getCalendar());
             }
         }
 
         public static final Comparator<MostRecentPair> COMPARATOR = new MostRecentPairComparator();
 
         private Schedule mSchedule;
         private Calendar mCalendar;
 
         public MostRecentPair(Schedule schedule, Calendar calendar) {
             mSchedule = schedule;
             mCalendar = calendar;
         }
 
         public Schedule getSchedule() {
             return mSchedule;
         }
 
         public Calendar getCalendar() {
             return mCalendar;
         }
     }
 
     private static final String LOG_TAG = "periodical";
 
     public static void schedule(Context context, Database database) {
         Schedule[] schedules = database.getSchedules();
         int length = schedules.length;
         if (length == 0) {
             removeLogFile();
             return;
         }
         MostRecentPair[] pairs = new MostRecentPair[length];
         Now now = new Now();
         for (int i = 0; i < length; i++) {
             pairs[i] = computeNextTime(now, schedules[i]);
         }
         Arrays.sort(pairs, MostRecentPair.COMPARATOR);
         MostRecentPair mostRecent = pairs[0];
         Calendar mostRecentTime = mostRecent.getCalendar();
         Collection<Schedule> mostRecentSchedules = new HashSet<Schedule>();
         for (int i = 0; i < length; i++) {
             MostRecentPair pair = pairs[i];
             if (mostRecentTime.equals(pair.getCalendar())) {
                 mostRecentSchedules.add(pair.getSchedule());
             }
         }
         Collection<Integer> tasks = new HashSet<Integer>();
         for (Schedule schedule: mostRecentSchedules) {
             int[] ids = database.getTaskIdsOfSchedule(schedule.getId());
             for (int i = 0; i < ids.length; i++) {
                 tasks.add(Integer.valueOf(ids[i]));
             }
         }
         int[] ids = new int[tasks.size()];
         int i = 0;
         for (Integer id: tasks) {
             ids[i] = id.intValue();
             i++;
         }
         logSchedule(mostRecentTime);
 
        Intent intent = new Intent(context, PeriodicalService.class);
         intent.putExtra(PeriodicalService.EXTRA_IDS, ids);
         int flags = PendingIntent.FLAG_UPDATE_CURRENT;
         PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                                                                intent, flags);
         String name = Context.ALARM_SERVICE;
         AlarmManager am = (AlarmManager)context.getSystemService(name);
         am.set(AlarmManager.RTC, mostRecentTime.getTimeInMillis(),
                pendingIntent);
     }
 
     private static MostRecentPair computeNextTimeOfHourlySchedule(Now now, Schedule schedule) {
         int minute = schedule.getMinute();
         Calendar calendar = Calendar.getInstance();
         calendar.set(now.getYear(), now.getMonth() - 1, now.getDay(),
                      now.getHour() + (now.getMinute() < minute ? 0 : 1),
                      minute);
         return new MostRecentPair(schedule, calendar);
     }
 
     private static MostRecentPair computeNextTimeOfDailySchedule(Now now, Schedule schedule) {
         int hour = schedule.getHour();
         int minute = schedule.getMinute();
         int secondOfDay = 60 * (60 * hour + minute);
         int day = now.getDay() + (now.getSecondOfDay() < secondOfDay ? 0 : 1);
         Calendar calendar = Calendar.getInstance();
         calendar.set(now.getYear(), now.getMonth() - 1, day, hour, minute);
         return new MostRecentPair(schedule, calendar);
     }
 
     private static MostRecentPair computeNextTime(Now now, Schedule schedule) {
         boolean isDaily = schedule.isDaily();
         return isDaily ? computeNextTimeOfDailySchedule(now, schedule)
                        : computeNextTimeOfHourlySchedule(now, schedule);
     }
 
     private static String formatCalendar(Calendar calendar) {
         int year = calendar.get(Calendar.YEAR);
         int month = calendar.get(Calendar.MONTH) + 1;
         int day = calendar.get(Calendar.DAY_OF_MONTH);
         int hour = calendar.get(Calendar.HOUR_OF_DAY);
         int minute = calendar.get(Calendar.MINUTE);
         String fmt = "%04d/%02d/%02d %02d:%02d";
         return String.format(fmt, year, month, day, hour, minute);
     }
 
     private static void removeLogFile() {
         new File(getLogPath()).delete();
     }
 
     private static String getLogPath() {
         String directory = Util.getLogDirectory().getAbsolutePath();
         return Util.joinPath(directory, "next_schedule");
     }
 
     private static void logSchedule(Calendar calendar) {
         String s = formatCalendar(calendar);
         Log.i(LOG_TAG, String.format("Next schedule: %s", s));
 
         try {
             FileWriter out = new FileWriter(getLogPath());
             try {
                 PrintWriter writer = new PrintWriter(out);
                 try {
                     writer.print(s);
                 }
                 finally {
                     writer.close();
                 }
             }
             finally {
                 out.close();
             }
         }
         catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
