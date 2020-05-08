 package zhang.abel.memmo.android;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.TimePickerDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.widget.TimePicker;
 import zhang.abel.memmo.android.entities.Album;
 import zhang.abel.memmo.android.repositories.AlbumRepository;
 
 import java.util.Calendar;
 import java.util.Random;
 
 public class NotificationSettingActivity extends Activity {
 
     private static final String NOTIFICATION_CONTENT = "该拍照啦！";
     private static final int NOTIFICATION_INTERVAL = 24 * 60 * 60 * 1000;
     private int NOTIFICATION_ID = GenerateNotificationId();
     private AlarmManager alarmManager;
     private Calendar calendar;
     private Album currentAlbum;
     private AlbumRepository albumRepository;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.albumlist);
 
         albumRepository = new AlbumRepository();
         currentAlbum = (Album) getIntent().getSerializableExtra(AlbumListActivity.SER_KEY);
 
         alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
         calendar = Calendar.getInstance();
         calendar.setTimeInMillis(System.currentTimeMillis());
         int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
         int minute = calendar.get(Calendar.MINUTE);
         new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                     public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                         setCalendar(hourOfDay, minute);
                         alarmManager.setRepeating(
                                 AlarmManager.RTC_WAKEUP,
                                 calendar.getTimeInMillis(),
                                 NOTIFICATION_INTERVAL,
                                 getPendingIntent()
                         );
                         SaveNotificationInfo(calendar);
                         Intent intent = new Intent(NotificationSettingActivity.this, AlbumActivity.class);
                         Bundle bundle = new Bundle();
                         bundle.putSerializable(AlbumListActivity.SER_KEY, currentAlbum);
                         intent.putExtras(bundle);
                         startActivity(intent);
                     }
                 }, hourOfDay, minute, true).show();
     }
 
     private void SaveNotificationInfo(Calendar calendar) {
         String prefName = "notification";
         SharedPreferences pref = getSharedPreferences(prefName, MODE_PRIVATE);
         SharedPreferences.Editor editor = pref.edit();
 
        String key = albumRepository.getAlbumDirectory(currentAlbum).getPath();
         editor.putLong(key, calendar.getTimeInMillis());
         editor.commit();
     }
 
     private PendingIntent getPendingIntent() {
         Intent intent = new Intent(this, NotificationReceiver.class);
         intent.putExtras(setNotificationBundle());
         return PendingIntent.getBroadcast(NotificationSettingActivity.this, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
     }
 
     private void setCalendar(int hourOfDay, int minute) {
         calendar.setTimeInMillis(System.currentTimeMillis());
         calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
         calendar.set(Calendar.MINUTE, minute);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
     }
 
     private Bundle setNotificationBundle() {
         Bundle bundle = new Bundle();
         bundle.putString("info", NOTIFICATION_CONTENT);
         bundle.putInt("id", NOTIFICATION_ID);
         bundle.putSerializable(AlbumListActivity.SER_KEY, currentAlbum);
         return bundle;
     }
 
     private int GenerateNotificationId(){
         Random random = new Random();
         int randomInt = random.nextInt(100);
         return randomInt + 1;
     }
 }
