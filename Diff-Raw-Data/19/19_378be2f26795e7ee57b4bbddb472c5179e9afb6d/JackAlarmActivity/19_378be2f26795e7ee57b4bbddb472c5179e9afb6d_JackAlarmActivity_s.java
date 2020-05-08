 package jack.Alarm;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.Dialog;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.app.TimePickerDialog;
 import android.app.SearchManager.OnCancelListener;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.sax.StartElementListener;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewDebug.FlagToString;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.ToggleButton;
 
 public class JackAlarmActivity extends Activity implements OnInitListener, OnUtteranceCompletedListener{
     private String tag = "JackAlarmActivity";
     private String intent_alarm_action;
     private final int MY_DATA_CHECK_CODE = 99;
     private final int TIME_DIALOG_ID = 0;
     private final int ALARM_NOW_ID = 1;
 
     public WakeLock mWakeLock;
     private AlarmManager mAlarmManager;
     private TextView alarmSetFor_tv;
     private CheckBox[] cbForDays = new CheckBox[7];
     private jackAlarmTimePickedListener mAlarmTimePickedListener;
     private ProgressDialog alarmProgressDialog;
     private TextToSpeech mTts;
     private Parcel pickerState;
     private int hours, minutes;
     //private boolean[] cbForDays = new boolean[7];
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         PowerManager pwm = (PowerManager) getSystemService(POWER_SERVICE);
         mWakeLock = pwm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "alarm wake lock");
 
         setContentView(R.layout.main);
         Log.d(tag, "onCreate");
         onCBClickListener cbClickListener = new onCBClickListener(this);
         cbForDays[2] = (CheckBox)findViewById(R.id.monday_cb);
         cbForDays[3] = (CheckBox)findViewById(R.id.tuesday_cb);
         cbForDays[4] = (CheckBox)findViewById(R.id.wednesday_cb);
         cbForDays[5] = (CheckBox)findViewById(R.id.thursday_cb);
         cbForDays[6] = (CheckBox)findViewById(R.id.friday_cb);
         cbForDays[0] = (CheckBox)findViewById(R.id.saturday_cb);
         cbForDays[1] = (CheckBox)findViewById(R.id.sunday_cb);
         for(int i=0; i<cbForDays.length; i++){
             cbForDays[i].setOnClickListener(cbClickListener);
         }
 
         intent_alarm_action = getString(R.string.ALARM_EXECUTE_INTENT);
         alarmSetFor_tv = (TextView) findViewById(R.id.alarmSetFor_tv);
         Button setTimeBut = (Button) findViewById(R.id.setTime);
         setTimeBut.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 showDialog(TIME_DIALOG_ID);
             }
         });
         ToggleButton setAlarmBut = (ToggleButton) findViewById(R.id.setAlarm);
         setAlarmBut.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View setAlarmBut) {
                 ToggleButton tb = (ToggleButton) setAlarmBut;
                 if(tb.isChecked()){
                     Log.d(tag, "toggle is checked");
                     setAlarm();
                 }
                 else{
                     Log.d(tag, "toggle is unchecked");
                     unSetAlarm();
                 }
             }
         });
         IntentFilter IF = new IntentFilter();
         IF.addAction(intent_alarm_action);
         registerReceiver(new alarmReciever(), IF);
 
         Intent checkIntent = new Intent();
         checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
         startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
 
         mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 
         mAlarmTimePickedListener = new jackAlarmTimePickedListener(this);
 
     }
 
     @Override
     public void onResume(){
         Log.d(tag, "onResume");
         super.onResume();
        String intentAction = getIntent().getAction();

     }
 
     public void onNewIntent(Intent intent){
         Log.d(tag, "onNewIntent");
         if(intent.getAction().equals(intent_alarm_action)){
             startAlarming();
         }
     }
 
     private void setAlarm(){
         Log.d(tag, "setting the alarms to go off");
         ArrayList<Integer> armedDays = alarmedDays(); //returns 1-7 for sunday through saturday
         if(armedDays.isEmpty()){
             Log.d(tag, "no checked days");
             return;
         }
         Calendar calOfNextAlarm = findNextCheckedDay(armedDays);
 
 
         PendingIntent alarmPending = createAlarmPendingIntent();
         mAlarmManager.set(AlarmManager.RTC_WAKEUP, calOfNextAlarm.getTimeInMillis(), alarmPending);
         Log.d(tag, "next alarm set to go off on day "+calOfNextAlarm.get(Calendar.DAY_OF_WEEK)+" hour "+hours+" min. "+minutes);
     }
 
     private PendingIntent createAlarmPendingIntent(){
         Intent rcvIntent = new Intent(intent_alarm_action);
         return PendingIntent.getBroadcast(this, 0, rcvIntent, PendingIntent.FLAG_CANCEL_CURRENT);
     }
 
     private ArrayList<Integer> alarmedDays(){
        ArrayList<Integer> days = new ArrayList<Integer>();
         for(int i=0; i<7; i++){
           if((i==0) && (cbForDays[i].isChecked())){
               days.add(7);
           }
           if(cbForDays[i].isChecked()){
               days.add(i);
           }
         }
         return days;
     }
 
     private Calendar findNextCheckedDay(ArrayList<Integer> armedDays){
         Log.d(tag, "finding the next checked Day, checked days are "+armedDays.toString());
         Calendar test = Calendar.getInstance();
         Calendar now = Calendar.getInstance();
         long leastTime = Long.MAX_VALUE;
         while(leastTime == Long.MAX_VALUE){
             for(int i=0; i<armedDays.size(); i++){
                 test.set(Calendar.DAY_OF_WEEK, armedDays.get(i));
                 test.set(Calendar.HOUR_OF_DAY, hours);
                 test.set(Calendar.MINUTE, minutes);
                 test.set(Calendar.SECOND, 0);
                 test.getTime();
                 if(now.compareTo(test) < 0){
                     long thisTime = test.getTimeInMillis();
                     if(thisTime < leastTime)
                         leastTime = thisTime;
                 }
             }
             if(leastTime == Long.MAX_VALUE){
                 test.add(Calendar.WEEK_OF_MONTH, 1);
                 test.getTime();
             }
         }
         Calendar returnCal = Calendar.getInstance();
         returnCal.setTimeInMillis(leastTime);
         returnCal.getTime();
         return returnCal;
     }
 
     public void unSetAlarm(){
         Log.d(tag, "unsetting the alarm, should remove all intents, and never go off");
         mAlarmManager.cancel(createAlarmPendingIntent());
     }
 
     protected void onActivityResult(int requestCode, int resultCode, Intent data){
         switch (requestCode) {
         case MY_DATA_CHECK_CODE:
             if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
                 mTts = new TextToSpeech(this, this);
             }
             else {
                 // missing data, install it
                 Intent installIntent = new Intent();
                 installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                 startActivity(installIntent);
             }
             break;
 
         default:
             Log.d(tag, "onActivtyResult error with requestCode in switch");
             break;
         }
     }
     public void setTime(int hour, int minute){
         hours = hour;
         minutes = minute;
         alarmSetFor_tv.setText("Alarm set for "+hour+":"+minute);
     }
     protected Dialog onCreateDialog(int id){
         Date now = new Date();
         switch (id){
         case TIME_DIALOG_ID:
             return new TimePickerDialog(this, mAlarmTimePickedListener, now.getHours(), now.getMinutes(), false);
         case ALARM_NOW_ID:
             alarmProgressCancelListener listener = new alarmProgressCancelListener(this);
            alarmProgressDialog = ProgressDialog.show(this, "Title", "message", true, true, listener);
             return alarmProgressDialog;
         default:
             Log.d(tag, "uhoh, got a bad ID in onCreateDialog, don't have a proper dialog to create :(");
             return null;
         }
     }
 
     @Override
     public void onInit(int status) {
         Log.d(tag, "onInit");
         mTts.setLanguage(Locale.US);
//        Intent intent = getIntent();
//        String action = intent.getAction();
//        if(action.equals(intent_alarm_action)){
//            startAlarming();
//        }
 
     }
 
     private void startAlarming(){
         //TODO: set the alarm to something cool, get weather etc.
         Log.d(tag, "startAlarming");
         showDialog(ALARM_NOW_ID);
         mTts.setOnUtteranceCompletedListener(this);
         HashMap<String, String> speachParams = new HashMap();
         speachParams.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
         speachParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "alarm speech");
         mTts.speak("STARTING ALARMING", TextToSpeech.QUEUE_ADD, speachParams);
     }
 
     public void onUtteranceCompleted(String uttId){
         Log.d(tag, "in onUtteranceCompleted id "+uttId+" . Should be dismissing dialog");
         dismissDialog(ALARM_NOW_ID);
         setAlarm();
         mWakeLock.release();
     }
 
     public void stopSpeaking(){
         Log.d(tag, "in stopSpeaking() cancelling the current alarm speech");
         mTts.stop();
         dismissDialog(ALARM_NOW_ID);
     }
 
     public void onDestroy(){
         Log.d(tag, "onDestroy");
         mTts.shutdown();
         super.onDestroy();
     }
 }
