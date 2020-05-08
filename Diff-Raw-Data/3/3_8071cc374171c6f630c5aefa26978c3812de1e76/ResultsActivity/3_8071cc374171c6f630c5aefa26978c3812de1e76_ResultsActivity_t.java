 package com.vorsk.crossfitr;
 
 import com.vorsk.crossfitr.models.WorkoutModel;
 import com.vorsk.crossfitr.models.WorkoutRow;
 import com.vorsk.crossfitr.models.WorkoutSessionModel;
 import com.vorsk.crossfitr.models.WorkoutSessionRow;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.TextView;
 
 /**
  * Creates and displays the Results view after completing a workout.
  * 
  * When invoking this activity, the caller must provide the relevant
  * workout session ID via the Intent. The results will be displayed for
  * this session.
  * 
  * @requires Intent->session_id
  * @author Vivek
  */
 public class ResultsActivity extends Activity implements OnClickListener
 {
 	private long session_id;
 	private EditText commentTextField;
 	private InputMethodManager keyControl;
 	
 	/**
 	 * Automatically ends this activity and returns control to the caller
 	 * This should be called if there was insufficient privelages to invoke
 	 * this activity.
 	 * 
 	 * @param tag A category for the reason
 	 * @param reason Reason that we must stop this activity (for logging)
 	 */
 	private void reject(String tag, String reason)
 	{
 		Log.v(tag, reason);
 		finish();
 	}
 	
 	/**
 	 * Performs all the validation for session_id
 	 * 
 	 * @return session associated with the ID
 	 */
 	private WorkoutSessionRow validateAccess()
 	{
 		if (getIntent() == null) {
 			reject("Page_Requirement", "No intent provided");
 		}
 		
 		session_id = getIntent().getLongExtra("session_id", 0L);
 		if (session_id == 0L) {
 			reject("Page_Requirement", "No session id provided");
 		}
 		
 		WorkoutSessionModel wsmodel = new WorkoutSessionModel(this);
 		wsmodel.open();
 		WorkoutSessionRow session = wsmodel.getByID(session_id);
 		if (session == null) {
 			reject("Page_Requirement", "invalid session id was provided");
 		}
 		wsmodel.close();
 		
 		return session;
 	}
 	
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		keyControl = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		
 		WorkoutSessionRow session = validateAccess();
 		
 		// Get the relevant Workout performed
 		WorkoutModel wmodel = new WorkoutModel(this);
 		wmodel.open();
 		WorkoutRow workout = wmodel.getByID(session.workout_id);
 		if (workout == null) 
 		{
 			Log.e("DB_Inconsistency", "Invalid workout id on session");
 			finish();
 		}
 		wmodel.close();
 		
 		// Create the initial view objects
 		setContentView(R.layout.results);
 		
 		// Get views
 		View btn_save = findViewById(R.id.button_results_sav_workout);
 		View btn_nosave = findViewById(R.id.button_results_dontsav_workout);
 		View btn_sharefb = findViewById(R.id.button_results_share_workout_FB);
 		
 		// text views
 		TextView txt_name = (TextView)findViewById(R.id.text_workout_name);
 		TextView txt_desc = (TextView)findViewById(R.id.text_workout_desc);
 		TextView txt_record = (TextView)findViewById(R.id.text_workout_record);
 		TextView txt_score = (TextView)findViewById(R.id.text_session_score);
 		
 		// Set content
 		txt_name.setText(workout.name);
 		txt_desc.setText(workout.description);
 		txt_record.setText(StopwatchActivity.formatElapsedTime(workout.record)); // Formatted using Stopwatch Activity
 		txt_score.setText(StopwatchActivity.formatElapsedTime(session.score)); // Formatted using Stopwatch Activity
 		
 		//edittext handler
 		commentTextField = (EditText) findViewById(R.id.results_comment_edittext_add);
 		commentTextField.setOnClickListener(this);
 
 		// Set handlers
 		btn_save.setOnClickListener(this);
 		btn_nosave.setOnClickListener(this);
 		btn_sharefb.setOnClickListener(this);
 	}
 	
 	//method to hide the keyboard
 	private void hideKeyboard(EditText eBox) 
 	{
 		keyControl.hideSoftInputFromWindow(eBox.getWindowToken(), 0);
 	}
 	
 	/**
 	 * Handles all the button clicks
 	 */
 	public void onClick(View v)
 	{
 		WorkoutSessionModel model = new WorkoutSessionModel(this);
 		switch(v.getId())
 		{
 			// if user presses save and end button button, will go back to home screen after saving.
 			case R.id.button_results_sav_workout:
				Log.v("text", commentTextField.getText().toString());
 				WorkoutSessionRow session = validateAccess();
 				WorkoutSessionModel model1 = new WorkoutSessionModel(this);
 				model1.editComment(session_id, commentTextField.getText().toString());
 				finish();
 				
 				//close all activity except homepage activity
 				Intent intent  = new Intent(getBaseContext(), CrossFitrActivity.class);
                 intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);        
                 startActivity(intent);
 
 				break;
 			// if user presses dont save button, go back to home screen.
 			case R.id.button_results_dontsav_workout:
 				model.open();
 				model.delete(session_id);
 				model.close();
 				finish();
 				
 				//close all activities except homepage
 				Intent intent2  = new Intent(getBaseContext(), CrossFitrActivity.class);
                 intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);        
                 startActivity(intent2);
 				break;
 				   
 			// if user presses share on fb button, results will be shared on fb.			
 			case R.id.button_results_share_workout_FB:
 				//TODO: implement fb functionality
 			    // if user presses this button, user will now go into the timer page.
 				break;
 				
 			//should close keyboard if clicks on background
 			case R.id.results_background:
 				hideKeyboard(commentTextField);
 				break;
 		}
 	}
 	
 }
