 package com.kinnack.nthings;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 import java.util.Date;
 
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface.OnClickListener;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.opengl.Visibility;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.kinnack.nthings.activity.RestActivity;
 import com.kinnack.nthings.model.ExerciseSet;
 import com.kinnack.nthings.model.History;
 import com.kinnack.nthings.model.Test;
 import com.kinnack.nthings.model.Workout;
 import com.kinnack.nthings.model.level.Level;
 
 public class Home extends Activity {
     public static final String TAG = "nthings:HOME";
     
     private static final int COUNTER_INTENT = 100;
     private static final int TEST_INTENT = 150;
     private static final int REST_INTENT = 200;
     private static final int FINAL_TEST_INTENT=175;
     private static final String PUBLIC_FOLDER_PATH=Environment.getExternalStorageDirectory()+"/nhundredthings/";
     private static final String PUBLIC_FILE_PATH=PUBLIC_FOLDER_PATH+"/prefs_config.xml";
     private static final String PRIVATE_FILE_PATH = "/data/data/"+Home.class.getPackage().getName()+"/shared_prefs/prefs_config.xml";
     
     
     public static final String PREFS = "prefs_config";
     public static final String KEY_CURRENT_WEEK = "current_week";
     public static final String KEY_CURRENT_DAY = "current_day";
     public static final String KEY_CURRENT_LEVEL = "current_level";
     public static final String KEY_HISTORY = "history";
     
     private ExerciseSet set;
 
     private Editor prefEditor;
     private History pushupHistory;
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         boolean copiedFile = copyFile(new File(PUBLIC_FILE_PATH),new File(PRIVATE_FILE_PATH));
         SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
         prefEditor = prefs.edit();
         if (!copiedFile) {
             // if first time prefs won't exist and couldn' copy file in so commit to create file and try to copy again.
             prefEditor.commit();
             copyFile(new File(PUBLIC_FILE_PATH),new File(PRIVATE_FILE_PATH));
         }
         Log.d(TAG,"Loaded history as "+prefs.getString(KEY_HISTORY, "[Not found]"));
         
         loadPushupHistory(prefs);
          
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         configureMainView();
         
     }
     
 
     /**
      * @param prefs
      */
     private void loadPushupHistory(SharedPreferences prefs) {
         try {
             pushupHistory = new History(prefs.getString(KEY_HISTORY, null));
         } catch (JSONException e) {
             Log.e(TAG, "Couldn't unmarshal history", e);
         } catch (NullPointerException npe) {
             Log.i(TAG, "No history to load");
         }
         if (pushupHistory == null) {
             pushupHistory = new History();
             pushupHistory.setDay(0);
             pushupHistory.setWeek(1);
             pushupHistory.setType(Workout.Type.PUSHUP);
         }
     }
 
     
 
     /**
      * 
      */
     private void configureMainView() {
         ((Button)findViewById(R.id.PushupsButton)).setEnabled(true);
         ((Button)findViewById(R.id.FinalButton)).setEnabled(false);
         TextView currentWeek = (TextView)findViewById(R.id.HomeCurrentWeek);
         String value = (pushupHistory == null ? "1" : ""+pushupHistory.getWeek());
         currentWeek.setText(value);
         
         TextView currentDay = (TextView)findViewById(R.id.HomeCurrentDay);
         View currentDayLabel = findViewById(R.id.HomeDayLabel);
         currentDay.setVisibility(View.VISIBLE);
         currentDayLabel.setVisibility(View.VISIBLE);
         
         value = (pushupHistory == null ? "0": ""+pushupHistory.getDay());
         currentDay.setText(value);
         if (value.equals("0")) {
             currentDay.setVisibility(View.INVISIBLE);
             currentDayLabel.setVisibility(View.INVISIBLE);
         }
         
         TextView currentLevel = (TextView)findViewById(R.id.HomeCurrentLevel);
         if (pushupHistory == null || value.equals("0") && pushupHistory.getWeek() != 7) {
             value = "TEST";
         } else if (pushupHistory.getWeek() >= 7) {
             value = "FINAL";
             ((Button)findViewById(R.id.PushupsButton)).setEnabled(false);
             pushupHistory.setFinalUnlocked(true);
         } else {
             value = pushupHistory.getCurrentLevel().getLabel();
         }
         
         currentLevel.setText(value);
         
         if (pushupHistory.isFinalUnlocked()) ((Button)findViewById(R.id.FinalButton)).setEnabled(true);
     }
     
     public void doPushups(View target_) {
         
         if (pushupHistory.getDay() == 0 && pushupHistory.getWeek() < 7) { startTestActivity(); return;}
         if (pushupHistory.getDay() == 0 && pushupHistory.getWeek() >= 7) { startFinalTestActivity(); return;}
         History.Log currentLog = pushupHistory.getCurrentLog();
         if (!currentLog.isFor(pushupHistory.getWeek(),pushupHistory.getDay())) {
             currentLog = pushupHistory.new Log(pushupHistory.getWeek(),pushupHistory.getDay());
         
             pushupHistory.getLogs().add(currentLog);
         }
         set = Workout.getPushupSetFor(pushupHistory.getWeek(), pushupHistory.getDay(), pushupHistory.getCurrentLevel());
         
         
         startCounterActivity();
         
     }
     
     public void doFinalTest(View target_) {
         startFinalTestActivity();
     }
     
     public void showProgress(View target_) {
         showProgress(pushupHistory);
     }
 
     /**
      * 
      */
     private void startCounterActivity() {
         Intent counterIntent = new Intent(this, CounterActivity.class);
         Log.d(TAG,"About to launch intent for "+CounterActivity.class.getName());
         counterIntent.putExtra(CounterActivity.INIT_COUNT_KEY, set.next());
         counterIntent.putExtra(CounterActivity.SHOW_DONE, set.isMax());
    
         Log.d(TAG, "Intent about to start");
         startActivityForResult(counterIntent, COUNTER_INTENT);
         Log.d(TAG, "Intent started and returned");
     }
     
     private void startTestActivity() {
         Intent counterIntent = new Intent(this, CounterActivity.class);
         Log.d(TAG,"About to launch intent for "+CounterActivity.class.getName());
         counterIntent.putExtra(CounterActivity.INIT_COUNT_KEY, 0);
         counterIntent.putExtra(CounterActivity.SHOW_DONE, true);
         counterIntent.putExtra(CounterActivity.IS_TEST, true);
         Log.d(TAG, "Intent about to start");
         startActivityForResult(counterIntent, TEST_INTENT);
         Log.d(TAG, "Intent started and returned");
     }
     
     private void startFinalTestActivity() {
         Intent counterIntent = new Intent(this, CounterActivity.class);
         Log.d(TAG,"About to launch intent for "+CounterActivity.class.getName());
         counterIntent.putExtra(CounterActivity.INIT_COUNT_KEY, 100);
         counterIntent.putExtra(CounterActivity.SHOW_DONE, true);
         counterIntent.putExtra(CounterActivity.IS_TEST, true);
         counterIntent.putExtra(CounterActivity.USE_SUBCOUNT, true);
         Log.d(TAG, "Intent about to start");
         startActivityForResult(counterIntent, FINAL_TEST_INTENT);
         Log.d(TAG, "Intent started and returned");
     }
     
     private void startRestActivity() {
         Intent restIntent = new Intent(this, RestActivity.class);
         Log.d(TAG, "About to launch intnet for "+RestActivity.class.getName());
         restIntent.putExtra(RestActivity.REST_LENGTH, set.next());
         restIntent.putExtra(RestActivity.SETS_DONE, set.getSetsDone());
         restIntent.putExtra(RestActivity.SETS_TO_GO, set.getSetsToGo());
         Log.d(TAG, "Rest about to start");
         startActivityForResult(restIntent, REST_INTENT);
     }
     
     @Override
     protected void onActivityResult(int requestCode_, int resultCode_, Intent data_) {
         switch (requestCode_) {
         case COUNTER_INTENT:
             // this was because the back button was pressed during a counter. FIXME do something better
             if (data_ == null) { 
                 deleteAnyUnwantedLogs();
                 return; 
             }
             Bundle extras = data_.getExtras();
             int count = extras.getInt(CounterActivity.MAX_COUNT);
             long avgTime = extras.getLong(CounterActivity.AVG_TIME);
             History.Log currentLog =pushupHistory.getCurrentLog();
             currentLog.addCountAndTime(count, avgTime);
            
             if (!set.hasNext()) { 
                 advanceDate();
                 saveHistory(); 
                 configureMainView();
                 showProgress(pushupHistory);
                 
                 shareResults(currentLog);
                 return; 
             }
             startRestActivity();
             
             
             
             break;
         case REST_INTENT:
             startCounterActivity();
             break;
         case TEST_INTENT:
            if (data_ == null) { return; }
             int test_count = data_.getExtras().getInt(CounterActivity.MAX_COUNT);
             
             Level level;
             switch(pushupHistory.getWeek()) {
                 case 1:
                     level = Test.initialTestLevel(test_count);
                     break;
                 case 3:
                     level = Test.secondTestLevel(test_count);
                     break;
                 case 5:
                     level = Test.thirdTestLevel(test_count);
                 case 6:                   
                     level = Test.fourthTestLevel(test_count);
                     break;
                 default:
                     Log.w(TAG,"Don't know why user is taking test week="+pushupHistory.getWeek()+", day="+pushupHistory.getDay());
                     return;
             }
             pushupHistory.getTestResults().add(test_count);
             pushupHistory.setCurrentLevel(level);
             pushupHistory.setDay(1);
             saveHistory();
             configureMainView();
             Toast.makeText(this, level.toString(), Toast.LENGTH_SHORT).show();
             break;
         case FINAL_TEST_INTENT:
            if(data_ == null) { return; }
             test_count = data_.getExtras().getInt(CounterActivity.MAX_COUNT);
             long totalTime = data_.getExtras().getLong(CounterActivity.TOTAL_TIME);
             pushupHistory.getTestResults().add(test_count);
             if (test_count >= 100) {
                 shareComplete(test_count, totalTime);
                 showUserDialog(R.string.final_complete_title, R.string.final_complete_msg);
                 pushupHistory.setFinished(true);
             } else {
                 shareDNFFinal(test_count, totalTime);
                 showUserDialog(R.string.final_DNF_title, R.string.final_DNF_msg);
             }
             pushupHistory.setWeek(6);
             pushupHistory.setDay(1);
             saveHistory();
             break;
         default:
             Log.d(TAG, "Got an unknown activity result. request["+requestCode_+"], result["+resultCode_+"]");
             break;
         }
         
         
         
     }
 
 
 
     private void deleteAnyUnwantedLogs() {
         pushupHistory.removeCurrentLog();
     }
 
 
   
     private void showUserDialog(int title_, int msg_) {
         new AlertDialog.Builder(this)
         .setTitle(title_)
         .setMessage(msg_)
         .setIcon(R.drawable.dialog)
         .setPositiveButton(R.string.final_complete_OK, new OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog_, int which_) {
                 dialog_.dismiss();
             }
         }).show();
     }
     
     /**
      * @param currentLog
      */
     private void shareResults(History.Log currentLog) {
         long roundedFrequency = Math.round(60000*currentLog.getAveragePushupFrequency());
         launchSharingChooser("My Latest DGMT! Results",
                 "I just did "+currentLog.getTotalCount()+" pushups at "+roundedFrequency+" pushups/min in #DGMT!");
 
     }
     
     private void shareComplete(int totalCount_, long totalTime_) {
         long roundedFrequency = Math.round(60000.0*totalCount_/totalTime_);
         launchSharingChooser("Mission Accomplished!",
                 "I finished the 100 pushup program using #DGMT! with "+totalCount_+" pushups IN A ROW at "+roundedFrequency+" pushups/min!");
     }
     
     private void shareDNFFinal(int totalCount_, long totalTime_) {
         long roundedFrequency = Math.round(60000.0*totalCount_/totalTime_);
         launchSharingChooser("Almost There!",
                 "I just did "+totalCount_+" pushups IN A ROW at "+roundedFrequency+" pushups/min in #DGMT! Almost there!");
     }
     
     
     private void launchSharingChooser(String subject_, String text_) {
         Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
         shareIntent.setType("text/plain");
         shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject_);
         shareIntent.putExtra(Intent.EXTRA_TEXT, text_);
         startActivity(Intent.createChooser(shareIntent, "Share Results"));
     }
     
     
     private void advanceDate() {
         int day = pushupHistory.getDay();
         int week = pushupHistory.getWeek();
         if (day == 3) {
             day = (week==5 ? 0 : week%2);
             Log.i(TAG, "Setting day to "+day+" because week%2="+(week%2));
             pushupHistory.setDay(day);
             pushupHistory.setWeek(pushupHistory.getWeek()+1);
             configureMainView();
         } else {
             pushupHistory.setDay(day+1);
         }
         
     }
     
     private void saveHistory() {
         
         try {
             pushupHistory.setLastWorkout(new Date());
             prefEditor.putString(KEY_HISTORY, pushupHistory.toJSON().toString());
             Log.d(TAG, "Saved history as "+pushupHistory.toJSON().toString());
             File externalFolder = new File(PUBLIC_FOLDER_PATH);
             if (!externalFolder.exists()) { externalFolder.mkdir(); }
             prefEditor.commit();
             copyFile(new File(PRIVATE_FILE_PATH),new File(PUBLIC_FILE_PATH));
         } catch (JSONException e) {
             Log.e(TAG,"Couldn't convert history to JSON! ",e);
             Toast.makeText(this, "Error saving history", Toast.LENGTH_SHORT);
         }
     }
     
     private void showProgress(History history_) {
         ProgressChart chart = new ProgressChart();
         Intent progressIntent = chart.progressChart(history_, this);
         startActivity(progressIntent);
     }
     
     /**
      * @return TODO
      * 
      */
     private boolean copyFile(File originalFile_, File copyFile_) {
         try {
             
             FileOutputStream out = new FileOutputStream(copyFile_);
             FileInputStream in = new FileInputStream(originalFile_);
             
             FileChannel inChannel = in.getChannel();
             FileChannel outChannel = out.getChannel();
 
             outChannel.transferFrom(inChannel, 0, inChannel.size());
 
             inChannel.close();
             outChannel.close();
             in.close();
             out.close();
 
             return true;
         } catch (FileNotFoundException e1) {
            Log.i(TAG,"Could nto find file shared_prefs/prefs_config.xml",e1);
         } catch (IOException e) {
             Log.w(TAG, "ERROR trying to write preferences to disk",e);
         } 
         return false;
     }
 }
