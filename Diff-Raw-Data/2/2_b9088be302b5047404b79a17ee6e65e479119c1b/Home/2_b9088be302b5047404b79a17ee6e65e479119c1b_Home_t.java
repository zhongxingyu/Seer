 package com.kinnack.nthings;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Toast;
 
 import com.kinnack.nthings.activity.RestActivity;
 import com.kinnack.nthings.model.ExerciseSet;
 import com.kinnack.nthings.model.Test;
 import com.kinnack.nthings.model.Workout;
 import com.kinnack.nthings.model.level.Level;
 
 public class Home extends Activity {
     public static final String TAG = "nthings:HOME";
     
     private static final int COUNTER_INTENT = 100;
     private static final int TEST_INTENT = 150;
     private static final int REST_INTENT = 200;
     
     public static final String PREFS = "prefs_config";
     public static final String KEY_CURRENT_WEEK = "current_week";
     public static final String KEY_CURRENT_DAY = "current_day";
     public static final String KEY_CURRENT_LEVEL = "current_level";
     
     private ExerciseSet set;
     private int week;
     private int day;
     private Level level;
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
         week = prefs.getInt(KEY_CURRENT_DAY, 1);
         day = prefs.getInt(KEY_CURRENT_DAY, 0);
         level = Test.findLevelForWeekByIndex(week, prefs.getInt(KEY_CURRENT_LEVEL, 0));
         Log.i(TAG,"Loaded week "+week+", day "+day+" with level="+level);
         if (day == 0) startTestActivity();
     }
     
     public void doPushups(View target_) {
         set = Workout.getPushupSetFor(week, day, level);
         startCounterActivity();
         
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
         Log.d(TAG, "Intent about to start");
         startActivityForResult(counterIntent, TEST_INTENT);
         Log.d(TAG, "Intent started and returned");
     }
     
     private void startRestActivity() {
         Intent restIntent = new Intent(this, RestActivity.class);
         Log.d(TAG, "About to launch intnet for "+RestActivity.class.getName());
         restIntent.putExtra(RestActivity.REST_LENGTH, set.next());
         Log.d(TAG, "Rest about to start");
         startActivityForResult(restIntent, REST_INTENT);
     }
     
     @Override
     protected void onActivityResult(int requestCode_, int resultCode_, Intent data_) {
         switch (requestCode_) {
         case COUNTER_INTENT:
             if (!set.hasNext()) return;
             startRestActivity();
             break;
         case REST_INTENT:
             startCounterActivity();
             break;
         case TEST_INTENT:
             int test_count = data_.getExtras().getInt(CounterActivity.MAX_COUNT);
            level = Test.initialTestLevel(test_count);
             Toast.makeText(this, level.toString(), Toast.LENGTH_SHORT).show();
             day = 1;
             break;
         default:
             Log.d(TAG, "Got an unknown activity result. request["+requestCode_+"], result["+resultCode_+"]");
             break;
         }
     }
 }
