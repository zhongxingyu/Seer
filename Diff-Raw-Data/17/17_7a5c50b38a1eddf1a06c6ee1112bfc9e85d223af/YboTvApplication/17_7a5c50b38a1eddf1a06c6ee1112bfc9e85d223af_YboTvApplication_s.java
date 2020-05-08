 package fr.ybo.ybotv.android;
 
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.Application;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Build;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import fr.ybo.ybotv.android.activity.CeSoirActivity;
 import fr.ybo.ybotv.android.activity.NowActivity;
 import fr.ybo.ybotv.android.activity.ParChaineActivity;
 import fr.ybo.ybotv.android.database.YboTvDatabase;
 import fr.ybo.ybotv.android.receiver.AlarmReceiver;
 import org.acra.ACRA;
 import org.acra.annotation.ReportsCrashes;
 
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.Set;
 
 @ReportsCrashes(formKey = "dDBLNm5ZR2dLWFhyQTV0dDMtTDdFZVE6MQ")
 public class YboTvApplication extends Application {
 
 
     public static final String TAG = "YboTv";
 
     private final static Set<String> EMULATORS_PRDODUCT = new HashSet<String>(){{
         add("full_x86");
         add("google_sdk");
         add("sdk");
     }};
 
     private YboTvDatabase database;
 
     public static boolean isEmulator() {
         Log.d(YboTvApplication.TAG, "BuildProduct : " + Build.PRODUCT);
         return EMULATORS_PRDODUCT.contains(Build.PRODUCT);
     }
 
     @Override
     public void onCreate() {
         if (!isEmulator()) {
             ACRA.init(this);
         }
         super.onCreate();
         database = new YboTvDatabase(this);
         /*StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                 .detectAll()
                 .penaltyLog()
                 .penaltyDeath()
                 .build());
 
         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                 .detectAll()
                 .penaltyLog()
                 .penaltyDeath()
                 .build());*/
     }
 
     public YboTvDatabase getDatabase() {
         return database;
     }
 
     private static enum SCREEN {
         NOW(NowActivity.class),
         CE_SOIR(CeSoirActivity.class),
         PAR_CHAINE(ParChaineActivity.class);
 
         private Class<? extends Activity> activity;
 
         private SCREEN(Class<? extends Activity> activity) {
             this.activity = activity;
         }
 
         public Class<? extends Activity> getActivity() {
             return activity;
         }
     }
 
     public Class<? extends Activity> getDefaultActivity() {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultScreen = prefs.getString("YboTv_defaultScreen", SCREEN.NOW.name());
        return SCREEN.valueOf(defaultScreen).getActivity();
     }
 
     private String versionInPref = null;
 
     public String getVersionInPref() {
         if (versionInPref == null) {
             SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
             versionInPref = prefs.getString("ybo-tv.version", "0");
         }
         return versionInPref;
     }
 
     public void updateVersionInPref(String currentVersion) {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         SharedPreferences.Editor editor = prefs.edit();
         editor.putString("ybo-tv.version", currentVersion);
         editor.commit();
         versionInPref = currentVersion;
     }
 
     public void setRecurringAlarm() {
         Calendar updateTime = Calendar.getInstance();
         Intent alarm = new Intent(this, AlarmReceiver.class);
         PendingIntent recurringAlarm = PendingIntent.getBroadcast(this, 0, alarm, PendingIntent.FLAG_CANCEL_CURRENT);
         AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(), AlarmManager.INTERVAL_HALF_DAY, recurringAlarm);
     }
 
 }
