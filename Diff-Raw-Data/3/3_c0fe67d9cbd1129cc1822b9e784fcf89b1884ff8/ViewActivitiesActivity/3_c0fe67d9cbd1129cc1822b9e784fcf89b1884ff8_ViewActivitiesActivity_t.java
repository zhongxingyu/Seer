 package com.github.lazycure;
 
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
import java.util.TimeZone;
 
 import com.github.lazycure.activities.Activity;
 import com.github.lazycure.db.DatabaseHandler;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.text.method.ScrollingMovementMethod;
 import android.util.Log;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class ViewActivitiesActivity extends LazyCureActivity {
 
 	private Button doneButton;
 	private TextView activityText;
 	private EditText activityNameEditText;
 	private Button cancelButton;
 	DatabaseHandler db = new DatabaseHandler(this);
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         setUpViews();
     }
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		showActivities();
 	}
 
 	private void showActivities() {
 		String prefix = "> ";
 		String delimiter = " - ";
 		SimpleDateFormat ft = new SimpleDateFormat ("HH:mm:ss");
		ft.setTimeZone(TimeZone.getTimeZone("UTC"));
         List<Activity> activities = db.getAllActivities();
         //Reverse the activities order
         Collections.reverse(activities);
 		StringBuffer sb = new StringBuffer();
 		for (int i=1;i<activities.size();i++){
 			if (activities.get(i-1).getStartTime() != null){
 				activities.get(i).setFinishTime(activities.get(i-1).getStartTime());
 			}
 		}
 		for (Activity t:activities) {
 			sb.append(prefix);
 			sb.append(t.getName().toString());
 			sb.append(delimiter);
 			if (t.getFinishTime() != null){
 				Date delta = new Date(t.getFinishTime().getTime() - t.getStartTime().getTime());
 				Log.d("Date: " , "Inserting [" + t.getName().toString() + "] with duration: " + ft.format(delta));
 				sb.append(ft.format(delta));
 			}
 			sb.append("\n");
 		}
 		activityText.setMovementMethod(new ScrollingMovementMethod());
 		activityText.setText(sb.toString());
 	}
 
 	private void setUpViews() {
 		doneButton = (Button)findViewById(R.id.done_button);
 		activityText = (TextView)findViewById(R.id.activities_list_text);
 		activityNameEditText = (EditText)findViewById(R.id.input);
 		cancelButton = (Button)findViewById(R.id.cancel_button);
 		
 		doneButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				addActivity();
 			}
 		});
 		cancelButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				clearInput();
 			}
 		});
 
 		clearInput();
 	}
 	
 	public Date getCuttentDate(){
 		Date date = new Date();
 		return date;
 	}
 	
 	private void addActivity() {
 		String activityName = activityNameEditText.getText().toString();
 		if (activityName.length() != 0){
 			Log.d("Insert: " , "Inserting [" + activityName + "] with date: " + getCuttentDate().toString());
 	        db.addActivity(new Activity(activityName, getCuttentDate(), null));
 		}
 		clearInput();
 		showActivities();
 	}
 	
 	protected void clearInput() {
 		activityNameEditText.setText("");
 		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(activityNameEditText.getWindowToken(), 0);
 	}
 	
 }
