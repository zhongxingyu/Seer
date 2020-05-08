 package dps.Assignment2.WorkoutTracker;
 
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.app.PendingIntent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 import android.os.SystemClock;
 import android.widget.TimePicker;
 import android.widget.DatePicker;
 
 public class SetAlarmActivity extends Activity {
 	private PendingIntent pendingIntent;
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.setalarm);
 	    Button buttonStart = (Button)findViewById(R.id.startalarm);
 	    //Button buttonCancel = (Button)findViewById(R.id.cancelalarm);
 	    buttonStart.setOnClickListener(new Button.OnClickListener(){
 	    	
 			 public void onClick(View arg0) {
 	    		
 			  // TODO Auto-generated method stub
 		
 				  TimePicker tp1 = (TimePicker)findViewById(R.id.tPAlarm);
 				  DatePicker dp1 = (DatePicker)findViewById(R.id.dPAlarm);
 				  int hour = tp1.getCurrentHour();
 				  int min = tp1.getCurrentMinute();
 				  Calendar ca = Calendar.getInstance();
 				  ca.set(dp1.getYear(), dp1.getMonth(), dp1.getDayOfMonth(), hour, min, 0);
 				 
 				  Calendar nowCa = Calendar.getInstance();
 				  nowCa.setTimeInMillis(System.currentTimeMillis());
 				  if (nowCa.getTimeInMillis() > ca.getTimeInMillis())
 				  {
 		
 					  Toast.makeText(SetAlarmActivity.this, "Reminder Not Set: \nReminders can not be set for the Past", Toast.LENGTH_LONG).show();
 				  }
 				  else
 				  {
 					  String reminder = "Reminder is Set!: \n" + dp1.getYear() + " " + dp1.getMonth() + "-" + dp1.getDayOfMonth()+ "  " + hour + ":" + min;
 					  Toast.makeText(SetAlarmActivity.this, reminder, Toast.LENGTH_SHORT).show();
 					  
 					  //Creating and Saving Workout
					  /*
 					  Workout workout = new Workout(dp1.getYear(), dp1.getMonth(), dp1.getDayOfMonth(), hour, min);
 					  WorkoutDbHelper handler = new WorkoutDbHelper(SetAlarmActivity.this);
 					  Workout workoutChecker = handler.getWorkout(dp1.getYear(), dp1.getMonth(), dp1.getDayOfMonth());
 					  if (workoutChecker == null){
 						  handler.addWorkout(workout);
 					  }
					  handler.close();*/
 
 					  
 					  //Intent myIntent = new Intent(SetAlarmActivity.this, MyAlarmService.class);
 					  Intent i = new Intent(getBaseContext(), DisplayNotification.class);
 					  i.putExtra("NotifID", 1);
 					  pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, i, 0);  
 					  //pendingIntent = PendingIntent.getService(SetAlarmActivity.this, 0, myIntent, 0);
 				      AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
 				      Calendar calendar = Calendar.getInstance();
 					  calendar.setTimeInMillis(System.currentTimeMillis());
 					  calendar.add(Calendar.SECOND, 8);
 					  //alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), displayIntent);
 				      alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
 				      Intent backH = new Intent(getBaseContext(), HomePageActivity.class);
 				      startActivity(backH);
 				}
 	    	}
 	    });
 
 
 
 		/*buttonCancel.setOnClickListener(new Button.OnClickListener(){
 	
 			@Override
 			public void onClick(View arg0) {
 			// TODO Auto-generated method stub
 			AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
 			alarmManager.cancel(pendingIntent);
 		    Toast.makeText(SetAlarmActivity.this, "Cancel!", Toast.LENGTH_LONG).show();
 			}
 		});*/
 	}
 }
