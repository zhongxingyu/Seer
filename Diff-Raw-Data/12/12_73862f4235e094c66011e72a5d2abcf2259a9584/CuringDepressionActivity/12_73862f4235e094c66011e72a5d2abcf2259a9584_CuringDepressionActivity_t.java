 package com.onedatapoint;
 
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.widget.TextView;
 import com.onedatapoint.config.Config;
 import com.onedatapoint.model.Question;
 
 public class CuringDepressionActivity extends Activity {
     private final static String LOGTAG = "onedatapoint";
     private Iterable<Question> questions;
     private boolean canExit = true;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         super.onCreate(savedInstanceState);
 
         questions = Config.getInstance().getQuestionRepository().getQuestions();
 
         setupAlarms();
         for (Question question : questions)
             Log.v(LOGTAG, question.toString());
 
         //questionViews = new Vector<View>();
         //createQuestionViews();
         setContentView(R.layout.home);
     }
 
     private void setupAlarms() {
         AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
         PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, LogNotificationReceiver.class), 0);               
 
         Calendar calendar = Calendar.getInstance();
 
         // TODO: This is the real code
         //calendar.set(Calendar.HOUR_OF_DAY, 9);
         //calendar.set(Calendar.MINUTE, 00);
         //calendar.set(Calendar.SECOND, 00);
 
         // Uncomment for testing
         calendar.setTimeInMillis(System.currentTimeMillis());
         calendar.add(Calendar.SECOND, 2);
 
         // This one sets the alarm for 9am daily
         alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24*60*60*1000 , pendingIntent);  //set repeating every 24 hours
 	}
 
 	// Home screen button onClick handlers
     public void openJournal(View v) {
         canExit = false;
         setContentView(R.layout.journal);
     }
     public void openReview(View v) {
         canExit = false;
         setContentView(R.layout.review);
     }
     public void openMedicine(View v) {
         canExit = false;
         setContentView(R.layout.medicine);
     }
     public void openGraphs(View v) {
         canExit = false;
         setContentView(R.layout.graphs);
     }
 
     public void saveAndGoHome(View v) {
         canExit = true;
         setContentView(R.layout.home);
     }
 
     public void saveAndQuit(View v) {
         moveTaskToBack(true);
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_BACK) {
             if (canExit) {
                 moveTaskToBack(true);
                 return true;
             }
 
             canExit = true;
             setContentView(R.layout.home);
             return true;
         }
         return super.onKeyDown(keyCode, event);
     }
 
     /*
     private void createQuestionViews() {
         for (Question question : questions) {
             View questionView = new View(this);
             // Create respective View object
             if (question.type.equals("graph"))
                 // Instantiate View
                 break;
             else if (question.type.equals("slider"))
                 // Instantiate View
                 break;
             else if (question.type.equals("buttons"))
                 // Instantiate View
                 break;
             questionViews.add(questionView);
         }
     }
     */
 
     private void showQuestions() {
         setContentView(R.layout.main);
 
         TextView text = (TextView)findViewById(R.id.xmlText);
         for (Question question : questions) {
             text.append("\n" + question.toString());
         }
     }
 }
