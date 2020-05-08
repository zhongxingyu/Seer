 package ch.checkbit.activities;
 
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageView;
 import ch.checkbit.LogType;
 import ch.checkbit.MultipleTransitionDrawable;
 import ch.checkbit.R;
 import ch.checkbit.db.DBContract.ActionType;
 import ch.checkbit.db.SQLDataSource;
 
 /**
  * this is where all starts
  * 
  * @author zeta
  * 
  */
 public class MainActivity extends Activity {
 
     private static final int TRANSITION_DURATION_GROW = 1000;
     private static final int TRANSITION_DURATION_WATER = 3000;
     private static final int TRANSITION_DURATION_CUT = 3000;
     private static final int TRANSITION_DURATION_THURSTY = 3000;
     private static final int TRANSITION_DURATION_HAIRY = 3000;
    private static final long TIME_WATER_MILLISECONDS = 20000;
    private static final long TIME_CUT_MILLISECONDS = 160203;
     private static final int PAUSE_DURATION = 1;
 
     private static final boolean TEST_ENVIRONMENT = true;
 
     private MultipleTransitionDrawable ctdGrow;
     private MultipleTransitionDrawable ctdThursty;
     private MultipleTransitionDrawable ctdHairy;
     private ImageView scene;
     private Drawable finalState;
 
     private boolean cut = false;
     private boolean water = false;
 
     private SQLDataSource dataSource;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
 
         super.onCreate(savedInstanceState);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         setContentView(R.layout.activity_grow);
         initDB();
 
         View mainScreen = findViewById(R.id.mainscreen);
        finalState = getResources().getDrawable(R.drawable.grow_13);
 
         mainScreen.setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View v) {
                 scene = (ImageView) findViewById(R.id.grow1);
 
                 if (dataSource.getLastUpdateFrom(ActionType.START) == null) {
                     startGrowSequence();
                     dataSource.put(ActionType.START);
                 } else {
                     // TODO
                 }
             }
         });
 
     }
 
     @Override
     public void onResume() {
         super.onResume();
         // dataSource.open();
         checkLastTimeCare();
     }
 
     @Override
     public void onPause() {
         super.onPause();
         // dataSource.close();
     }
 
     private void checkLastTimeCare() {
 
         if (dataSource.getLastUpdateFrom(ActionType.CUT) == null
                 && dataSource.getLastUpdateFrom(ActionType.WATER) != null) {
             dataSource.put(ActionType.CUT);
         }
 
         if (dataSource.getLastUpdateFrom(ActionType.WATER) == null) {
             dataSource.put(ActionType.WATER);
         }
 
         Long lastWateringTimestamp = dataSource.getLastUpdateFrom(ActionType.WATER).getTime();
         Long lastCuttingTimestamp = Long.MAX_VALUE;
         if (dataSource.getLastUpdateFrom(ActionType.CUT) != null) {
             lastCuttingTimestamp = dataSource.getLastUpdateFrom(ActionType.CUT).getTime();
         }
 
         Long now = new Date().getTime();
 
         if (lastWateringTimestamp > lastCuttingTimestamp) {
             // time to cut!
             if (now - lastCuttingTimestamp > TIME_CUT_MILLISECONDS) {
                 startHairySequence();
                 cut = true;
             }
         } else {
             // time to water!
             if (now - lastWateringTimestamp > TIME_WATER_MILLISECONDS) {
                 startThurstySequence();
                 water = true;
             }
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     public void play(View view) {
         Log.i(LogType.ACTION.name(), "Bonsai is happy to hear some music! :)");
         if (cut == false && water == false) {
             dataSource.put(ActionType.MUSIC);
             Intent intent = new Intent(this, MusicActivity.class);
             startActivity(intent);
         }
     }
 
     public void cut(View view) {
         Log.i(LogType.ACTION.name(), "Bonsai is happy to get new hair cut! :)");
         if (cut == true) {
             invalidatePrevious();
             Drawable thursty = getResources().getDrawable(R.drawable.hairy);
             ctdGrow = new MultipleTransitionDrawable(new Drawable[] { thursty, finalState });
             scene.setImageDrawable(ctdGrow);
             ctdGrow.startTransition(TRANSITION_DURATION_CUT, PAUSE_DURATION);
             cut = false;
             dataSource.put(ActionType.CUT);
         }
     }
 
     public void water(View view) {
         Log.i(LogType.ACTION.name(), "Bonsai is happy to get some water! :)");
         if (water == true) {
             invalidatePrevious();
             Drawable thursty = getResources().getDrawable(R.drawable.thursty);
             ctdGrow = new MultipleTransitionDrawable(new Drawable[] { thursty, finalState });
             scene.setImageDrawable(ctdGrow);
             ctdGrow.startTransition(TRANSITION_DURATION_WATER, PAUSE_DURATION);
             water = false;
             dataSource.put(ActionType.WATER);
         }
     }
 
     private void invalidatePrevious() {
         try {
             ctdThursty.invalidateSelf();
         } catch (Exception e) {
             Log.i(LogType.ERROR.name(), "Problem while invalidating thursy sequence.");
         }
 
         try {
             ctdHairy.invalidateSelf();
         } catch (Exception e) {
             Log.i(LogType.ERROR.name(), "Problem while invalidating hairy sequence.");
         }
     }
 
     private void startGrowSequence() {
         Log.i(LogType.ACTION.name(), "Bonsai just plant and starting to grow.");
         Drawable d2 = getResources().getDrawable(R.drawable.grow_2);
         Drawable d3 = getResources().getDrawable(R.drawable.grow_3);
         Drawable d4 = getResources().getDrawable(R.drawable.grow_4);
         Drawable d5 = getResources().getDrawable(R.drawable.grow_5);
         Drawable d6 = getResources().getDrawable(R.drawable.grow_6);
         Drawable d7 = getResources().getDrawable(R.drawable.grow_7);
         Drawable d8 = getResources().getDrawable(R.drawable.grow_8);
         Drawable d9 = getResources().getDrawable(R.drawable.grow_9);
         Drawable d10 = getResources().getDrawable(R.drawable.grow_10);
         Drawable d11 = getResources().getDrawable(R.drawable.grow_11);
         Drawable d12 = getResources().getDrawable(R.drawable.grow_12);
         ctdGrow = new MultipleTransitionDrawable(new Drawable[] { d2, d3, d4, d5, d6, d7, d8, d9, d10, d11, d12,
                 finalState });
         scene.setImageDrawable(ctdGrow);
         ctdGrow.startTransition(TRANSITION_DURATION_GROW, PAUSE_DURATION);
     }
 
     private void startThurstySequence() {
         Log.i(LogType.ACTION.name(), "Bonsai is thursty. Water it!");
         Drawable thursty = getResources().getDrawable(R.drawable.thursty);
         ctdThursty = new MultipleTransitionDrawable(new Drawable[] { finalState, thursty });
         scene.setImageDrawable(ctdThursty);
         ctdThursty.startTransition(TRANSITION_DURATION_THURSTY, PAUSE_DURATION);
     }
 
     private void startHairySequence() {
         Log.i(LogType.ACTION.name(), "What a messy hairstyle! Bonsai needs cutting!");
         Drawable hairy = getResources().getDrawable(R.drawable.hairy);
         ctdHairy = new MultipleTransitionDrawable(new Drawable[] { finalState, hairy });
         scene.setImageDrawable(ctdHairy);
         ctdHairy.startTransition(TRANSITION_DURATION_HAIRY, PAUSE_DURATION);
     }
 
     private void initDB() {
         dataSource = new SQLDataSource(this);
         dataSource.open();
         try {
             dataSource.createTable();
         } catch (Exception e) {
             Log.i(LogType.DB.name(), "The table already exists.");
         }
     }
 
     @Override
     public void onDestroy() {
         Log.i(LogType.ACTIVITY_LOG.name(), "Destroying main activity.");
         super.onDestroy();
         if (TEST_ENVIRONMENT) {
             dataSource.dropTable();
         }
         dataSource.close();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
     }
 
 }
