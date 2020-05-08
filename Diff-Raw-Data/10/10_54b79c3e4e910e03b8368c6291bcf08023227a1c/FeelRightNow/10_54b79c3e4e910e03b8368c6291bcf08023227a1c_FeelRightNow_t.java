 package edu.uci.asleepawake;
 
 //This class reads in the answers to the "How Do You Feel Right Now" (Epworth)
 //survey and sends them to the Google Form via the HttpRequest class
 
 import java.io.IOException;
 import java.net.URLEncoder;
import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 public class FeelRightNow extends Activity implements OnSeekBarChangeListener, OnClickListener{
 
 	String sittingReading, watchingTV, publicPlace, passenger,
 			afternoonRest, sittingTalking, afterLunch, carTraffic;
 	TextView sittingReadingTextView, watchingTVTextView, publicPlaceTextView,
 			passengerTextView, afternoonRestTextView, sittingTalkingTextView,
 			afterLunchTextView, carTrafficTextView;
 	SeekBar sittingReadingSeekBar, watchingTVSeekBar, publicPlaceSeekBar,
 			passengerSeekBar, afternoonRestSeekBar, sittingTalkingSeekBar,
 			afterLunchSeekBar, carTrafficSeekBar;
 	Button submit;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_feel_right_now);
 
 		//Assign ids of views on layout to local TextView objects
 		sittingReadingTextView = (TextView)findViewById(R.id.SittingReading);
 		watchingTVTextView = (TextView)findViewById(R.id.WatchingTV);
 		publicPlaceTextView = (TextView)findViewById(R.id.PublicPlace);
 		passengerTextView = (TextView)findViewById(R.id.Passenger);
 		afternoonRestTextView = (TextView)findViewById(R.id.AfternoonRest);
 		sittingTalkingTextView = (TextView)findViewById(R.id.SittingTalking);
 		afterLunchTextView = (TextView)findViewById(R.id.AfterLunch);
 		carTrafficTextView = (TextView)findViewById(R.id.CarTraffic);
 		
 		//Assign ids of seekbars (sliders) on layout to local SeekBar objects		
 		SeekBar sittingReadingSeekBar = (SeekBar)findViewById(R.id.SittingReadingSeekBar);
 		sittingReadingSeekBar.setMax(100); //This is how far the slider can go
 		sittingReadingSeekBar.setProgress(50); //This is the default setting for the slider
 		sittingReadingSeekBar.setOnSeekBarChangeListener(this); //Setting listener
 
 		SeekBar watchingTVSeekBar = (SeekBar)findViewById(R.id.WatchingTVSeekBar);
 		watchingTVSeekBar.setMax(100);
 		watchingTVSeekBar.setProgress(50);
 		watchingTVSeekBar.setOnSeekBarChangeListener(this);
 				
 		SeekBar publicPlaceSeekBar = (SeekBar)findViewById(R.id.PublicPlaceSeekBar);
 		publicPlaceSeekBar.setMax(100);
 		publicPlaceSeekBar.setProgress(50);
 		publicPlaceSeekBar.setOnSeekBarChangeListener(this);
 		
 		SeekBar passengerSeekBar = (SeekBar)findViewById(R.id.PassengerSeekBar);
 		passengerSeekBar.setMax(100);
 		passengerSeekBar.setProgress(50);
 		passengerSeekBar.setOnSeekBarChangeListener(this);
 		
 		SeekBar afternoonRestSeekBar = (SeekBar)findViewById(R.id.AfternoonRestSeekBar);
 		afternoonRestSeekBar.setMax(100);
 		afternoonRestSeekBar.setProgress(50);
 		afternoonRestSeekBar.setOnSeekBarChangeListener(this);
 		
 		SeekBar sittingTalkingSeekBar = (SeekBar)findViewById(R.id.SittingTalkingSeekBar);
 		sittingTalkingSeekBar.setMax(100);
 		sittingTalkingSeekBar.setProgress(50);
 		sittingTalkingSeekBar.setOnSeekBarChangeListener(this);
 		
 		SeekBar afterLunchSeekBar = (SeekBar)findViewById(R.id.AfterLunchSeekBar);
 		afterLunchSeekBar.setMax(100);
 		afterLunchSeekBar.setProgress(50);
 		afterLunchSeekBar.setOnSeekBarChangeListener(this);
 		
 		SeekBar carTrafficSeekBar = (SeekBar)findViewById(R.id.CarTrafficSeekBar);
 		carTrafficSeekBar.setMax(100);
 		carTrafficSeekBar.setProgress(50);
 		carTrafficSeekBar.setOnSeekBarChangeListener(this);
 
 	     submit = (Button)findViewById(R.id.RelationshipSurveyButton);
 	     submit.setOnClickListener(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_display_message, menu);
 		return true;
 	}
 
 	@Override
 	public void onProgressChanged(SeekBar v, int progress, boolean isUser) {
 		TextView tv = null;
 
 		//This is the listener for when the slider has been changed
 		//Associate the seekbar that was touched and its accompanying TextView
 		//to a local TextView object
 		if(v.getId() == R.id.SittingReadingSeekBar){
 			tv = (TextView)findViewById(R.id.SittingReading);
 		} else if(v.getId() == R.id.WatchingTVSeekBar){
 			tv = (TextView)findViewById(R.id.WatchingTV);
 		} else if(v.getId() == R.id.PublicPlaceSeekBar){
 			tv = (TextView)findViewById(R.id.PublicPlace);
 		} else if(v.getId() == R.id.PassengerSeekBar){
 			tv = (TextView)findViewById(R.id.Passenger);
 		} else if(v.getId() == R.id.AfternoonRestSeekBar){
 			tv = (TextView)findViewById(R.id.AfternoonRest);
 		} else if(v.getId() == R.id.SittingTalkingSeekBar){
 			tv = (TextView)findViewById(R.id.SittingTalking);
 		} else if(v.getId() == R.id.AfterLunchSeekBar){
 			tv = (TextView)findViewById(R.id.AfterLunch);
 		} else if(v.getId() == R.id.CarTrafficSeekBar){
 			tv = (TextView)findViewById(R.id.CarTraffic);
 		}
 		//So long as the local TextView object has been assigned,
 		//Output the answer onto the layout
 		if (tv != null) {
 			if (progress >= 0 && progress <= 24)
 				tv.setText("Would Never Doze");
 			if (progress >= 25 && progress <= 49)
 				tv.setText("Slight Chance of Dozing");
 			if (progress >= 50 && progress <= 74)
 				tv.setText("Moderate Chance of Dozing");
 			if (progress >= 75 && progress <= 100)
 				tv.setText("High Chance of Dozing");
 
 		}
 	}
 	
 	@Override
 	public void onStartTrackingTouch(SeekBar arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onStopTrackingTouch(SeekBar arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
 
 		//This is the listener for the submit button
 		//If at least one of the questions haven't been answered,
 		//display an alert telling the user to answer all the questions
 		if(sittingReadingTextView.getText().equals("Slide left or right")
 				|| watchingTVTextView.getText().equals("Slide left or right")	
 				|| publicPlaceTextView.getText().equals("Slide left or right")	
 				|| passengerTextView.getText().equals("Slide left or right")	
 				|| afternoonRestTextView.getText().equals("Slide left or right")	
 				|| sittingTalkingTextView.getText().equals("Slide left or right")	
 				|| afterLunchTextView.getText().equals("Slide left or right")	
 				|| carTrafficTextView.getText().equals("Slide left or right")	
 	
 				) {
 			
 			AlertDialog.Builder builder = new AlertDialog.Builder(FeelRightNow.this);
 			builder.setMessage("Please answer all the questions before submitting")
 			       .setCancelable(false)
 			       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			                //do things
 			        	   dialog.cancel();
 			           }
 			       });    
 			     
 			AlertDialog alert = builder.create();
 			alert.show();
 			//With all the questions answered, make a connection to the Google Form and 
 			//send the answers to the spreadsheet
 			
 			//Instructions for getting Google Form URL and entry code can be found here:
 			//http://www.youtube.com/watch?v=GyuJ2GtpZd0
 
 		} else {
 			String fullUrl = "https://docs.google.com/a/uci.edu/forms/d/1x-YIb5tAnkImWDLaw0YtNIyqa0AXCroq26ogf_2yS9o/formResponse";
 			HttpRequest mReq = new HttpRequest();
 
 	        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
 	        String participant = sp.getString("Participant", "");
 	        
 			String data = "entry_1794600332=" + URLEncoder.encode(participant) + "&"
 					+ "entry_2035552502=" + URLEncoder.encode(sittingReadingTextView.getText().toString()) + "&"
 					+ "entry_430809170=" + URLEncoder.encode(watchingTVTextView.getText().toString()) + "&"
 					+ "entry_1462571302=" + URLEncoder.encode(publicPlaceTextView.getText().toString()) + "&"
 					+ "entry_729054248=" + URLEncoder.encode(passengerTextView.getText().toString()) + "&"
 					+ "entry_302061174=" + URLEncoder.encode(afternoonRestTextView.getText().toString()) + "&"
 					+ "entry_487982611=" + URLEncoder.encode(sittingTalkingTextView.getText().toString()) + "&"
 					+ "entry_1030822716=" + URLEncoder.encode(afterLunchTextView.getText().toString()) + "&"
 					+ "entry_500458483=" + URLEncoder.encode(carTrafficTextView.getText().toString());
 			String response = mReq.sendPost(fullUrl, data);
 			System.out.println("postData response: " + response);
 			
 			savePrefs("feelRightNowSurveyIgnored","NO");
 			
 //			finish();
 //	    	   Intent backToMain = new Intent(FeelRightNow.this,MainActivity.class);
 //	    	   backToMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 //	    	   FeelRightNow.this.startActivity(backToMain);
 			
 			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
 					FeelRightNow.this);
 			alertDialogBuilder
 					.setTitle("Thank you for completing the questionnaires!")
 					.setMessage("Are you ready to get into bed?")
 					.setCancelable(false)
 					.setPositiveButton("Yes",
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									// TODO Auto-generated method stub
 									
 							        AlertDialog.Builder alertWatchBuilder = new AlertDialog.Builder(FeelRightNow.this);
 							        alertWatchBuilder
 							        	.setTitle("Please put on your sleep watch now")
 							        	.setCancelable(false)
 							        	.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 											
 											@Override
 											public void onClick(DialogInterface dialog, int which) {
												// TODO Auto-generated method stub													

										    	   Intent backToMain = new Intent(FeelRightNow.this,MainActivity.class);										    	   
 										    	   backToMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 										    	   FeelRightNow.this.startActivity(backToMain);
 											}
 										});
 							        
 //									Intent surveyIntent = new Intent(FeelRightNow.this, Relationship.class);
 //								    backToMainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 //									FeelRightNow.this.startActivity(surveyIntent);
							        
							        alertWatchBuilder.show();
 								}
 							})
 					.setNegativeButton("No",
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									// TODO Auto-generated method stub
 
 									
 							        //Create an offset from the current time in which the alarm will go off.
 							        Calendar cal = Calendar.getInstance();
 							        cal.add(Calendar.MINUTE, 10);
 
 							        //Create a new PendingIntent and add it to the AlarmManager
 							        Intent intent = new Intent(FeelRightNow.this, SleepAlarmReceiverActivity.class);
 							        intent.putExtra("intentID", 12345);
 							        PendingIntent pendingIntent = PendingIntent.getActivity(FeelRightNow.this,
 							            12345, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 							        AlarmManager am = (AlarmManager)FeelRightNow.this.getSystemService(ALARM_SERVICE);
 								        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
 								                pendingIntent);	
 									
 									finish();
 								}
 							});
 
 			AlertDialog alert = alertDialogBuilder.create();
 			alert.show();
 
 			}
 	}
 	
     private void savePrefs(String key, String value) {
         SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
         Editor edit = sp.edit();
         edit.putString(key, value);
         edit.commit();
     }
 
 }
