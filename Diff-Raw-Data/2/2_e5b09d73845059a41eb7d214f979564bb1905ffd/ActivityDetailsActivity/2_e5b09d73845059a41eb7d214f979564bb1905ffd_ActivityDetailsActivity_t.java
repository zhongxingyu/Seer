 package edu.kaist.kse631.bmaingret_achin.mycoach;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.concurrent.TimeUnit;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class ActivityDetailsActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_activity_details);
 		Intent intent = getIntent();
 		
 		/*Continue button */
 		String from = intent.getStringExtra(C.DETAILS_FROM);
 		Button continueButton = (Button) findViewById(R.id.details_continue_button);
 		if (null == from){
 			continueButton.setVisibility(View.GONE);
 		}
 		else{
 			continueButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					manageProject();
 				}
 			});
 		}
 		
 		/* Retrieving userAactivityId*/
 		long activityId = intent.getLongExtra("activityId", -1);
 		
 		/* Fecthing databse entry*/
 		UserActivitiesTableHelper helper = new UserActivitiesTableHelper(this);
 		Cursor activity = helper.getUserActivity(activityId);
 		
 		/* Filling the fields */
 		if (activity.moveToFirst()){
 			/* Activity type*/
 			TextView type = (TextView) findViewById(R.id.details_type);
 			type.setText(activity.getString(activity.getColumnIndex(ActivitiesTableHelper.COLUMN_ACTIVITY)));
 			
 			/* Duration */
 			TextView durationTextView = (TextView) findViewById(R.id.details__duration);
 			long duration = activity.getLong(activity.getColumnIndex(UserActivitiesTableHelper.COLUMN_DURATION));
 			String durationStr = String.format(" %02d:%02d'%d\"", 
 					TimeUnit.MILLISECONDS.toHours(duration),
 				    TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
 				    TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.HOURS.toSeconds(TimeUnit.MILLISECONDS.toHours(duration)) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
 			);
 			durationTextView.setText(durationStr);
 			
 			/* Date-time*/
 			TextView dateTextView = (TextView) findViewById(R.id.details_date);
 			long datetime = activity.getLong(activity.getColumnIndex(UserActivitiesTableHelper.COLUMN_DATETIME));
 			Date date = new Date(datetime);
 			DateFormat formatter = new SimpleDateFormat(" MM/dd/yyyy 'at' HH:mm");
 			String dateFormatted = formatter.format(date);
 			dateTextView.setText(dateFormatted);
 			
 			/* Calories */
 			int[] base = {
 					activity.getInt(activity.getColumnIndex(ActivitiesTableHelper.COLUMN_W1)),
 					activity.getInt(activity.getColumnIndex(ActivitiesTableHelper.COLUMN_W2)),
 					activity.getInt(activity.getColumnIndex(ActivitiesTableHelper.COLUMN_W3)),
 					activity.getInt(activity.getColumnIndex(ActivitiesTableHelper.COLUMN_W4))				
 			};
 			SharedPreferences prefs = getSharedPreferences(C.PREF, MODE_PRIVATE);
 			
			int weight = prefs.getInt(C.P_WEIGHT, 50);
 			int calories = CaloriesHelper.getCalories(weight, base, duration);
 			TextView caloriesTextView = (TextView) findViewById(R.id.details_calories);
 			caloriesTextView.setText(String.valueOf(calories));
 		}
 		
 
 		
 	}
 
 	private void manageProject() {
 		ProjectManager manager = new ProjectManager(this);
 		if (manager.isEndPeriod()){
 			Intent intent = new Intent(ActivityDetailsActivity.this, LevelUpActivity.class);
 			startActivity(intent);
 			finish();
 		}
 		else {
 			backToMain();
 		}
 	}
 	
 	private void backToMain(){
 		Intent intent = new Intent(ActivityDetailsActivity.this, MainActivity.class);
 		startActivity(intent);
 		finish();
 	}
 }
