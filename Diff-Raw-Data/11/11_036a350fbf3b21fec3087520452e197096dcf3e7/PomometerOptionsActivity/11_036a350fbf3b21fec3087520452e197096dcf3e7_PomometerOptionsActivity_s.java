 package com.pomometer.PomometerApp;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.NumberPicker;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PomometerOptionsActivity extends Activity {
 	
 	String task_id;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_pomometer_options);
 		
 		Bundle extras = getIntent().getExtras();
 		if (extras != null){
 			task_id = extras.getString("task_id");
 		}
 		
 		final int MIN_DURATION_IN_MINUTES = 1;
 		final int MAX_DURATION_IN_MINUTES = 60;
 		
 		NumberPicker duration_picker = (NumberPicker) findViewById(R.id.duration_picker);
 		duration_picker.setMaxValue(MAX_DURATION_IN_MINUTES);
 		duration_picker.setMinValue(MIN_DURATION_IN_MINUTES);
 			
 		Button start_button = (Button) findViewById(R.id.start_button);
 		start_button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	//if the edit box where the goal is entered is blank, then tell them to enter something, otherwise, load next screen
             	if (((EditText) findViewById(R.id.goal_entry)).getText().toString().length() < 1)
             	{
             		Toast.makeText(getBaseContext(), "You must enter a goal!", Toast.LENGTH_SHORT).show();
             	}
             	else
             	{
             		int duration = ((NumberPicker) findViewById(R.id.duration_picker)).getValue();
             		EditText goalEditText = (EditText) findViewById(R.id.goal_entry);
             		
             		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.Z", Locale.ENGLISH);
             		Date endPomometer = new Date();
             		Date startPomometer = new Date();
             		//since declaring starts a new date NOW (finished timer) we want to set that to end
             		//and calculate backwards to find start time.  
             		
             		//convert duration to milliseconds and substract to get start time
             		startPomometer.setTime(endPomometer.getTime() - (duration*60*1000));
             		
             		String formattedStartDate = dateFormatter.format(startPomometer);
             		String formattedEndDate = dateFormatter.format(endPomometer);
             		
             		Intent i = new Intent(getApplicationContext(), PomometerTimerActivity.class);
             		
             		i.putExtra("duration", ((NumberPicker) findViewById(R.id.duration_picker)).getValue());
             		i.putExtra("task_id", task_id);
             		i.putExtra("goal", goalEditText.getText().toString());
             		i.putExtra("started_at", formattedStartDate);
             		i.putExtra("ended_at", formattedEndDate);
             		
             		startActivity(i);
             	}
             }});
 		
 		// retrieve the textview reference to change text colour
		TextView npTextView = (TextView) duration_picker.getChildAt(1); // since indexing begins at 0
 		npTextView.setTextColor(getResources().getColor(R.color.white_text));
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.project_list, menu);
 	    return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    //respond to menu item selection
 		switch (item.getItemId()) {
 			case R.id.durationAndBreakLength:
 				startActivity(new Intent(this, DurationAndBreak.class));
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 }
