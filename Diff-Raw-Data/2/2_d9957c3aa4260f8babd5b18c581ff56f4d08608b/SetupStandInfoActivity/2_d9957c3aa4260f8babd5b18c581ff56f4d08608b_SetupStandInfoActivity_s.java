 package edu.upenn.cis.fruity;
 
 import java.util.Calendar;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class SetupStandInfoActivity extends Activity {
 
 	public static final int InventoryPreprocessActivity_ID = 8;
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_setup_stand_info);
 		Intent intent = getIntent();
 
 		
 		// Set school title from prior screen
 		String schoolName;
 		if (intent != null && intent.getExtras() != null) {
 			schoolName = (String) intent.getExtras().get("schoolName");
 		} else {
 			schoolName = "Filler Text";
 		}
 		
 		TextView schoolNameView = (TextView) findViewById(R.id.standInfo_schoolName);
 		schoolNameView.setText(schoolName);
 		
 		// Set date
 		TextView dateView = (TextView) findViewById(R.id.standInfo_dateField);
 		Calendar calendar = Calendar.getInstance();
		int month = calendar.get(Calendar.MONTH);
 		int day = calendar.get(Calendar.DAY_OF_MONTH);
 		int year = calendar.get(Calendar.YEAR);
 		dateView.setText(month + "/" + day + "/" + year);
 
 		// Populate weather choice spinner
 		Spinner weatherInput = (Spinner) findViewById(R.id.standInfo_weatherInput);
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
 				this, R.array.weather_options,
 				android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		weatherInput.setAdapter(adapter);
 
 		// Register listener to update temperature
 		SeekBar temperatureInput = (SeekBar) findViewById(R.id.standInfo_temperatureInput);
 		temperatureInput
 				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 					@Override
 					public void onProgressChanged(SeekBar sb, int arg1,
 							boolean arg2) {
 						TextView temp = (TextView) findViewById(R.id.standInfo_temperatureText);
 						temp.setText(sb.getProgress() + "F");
 
 					}
 
 					@Override
 					public void onStartTrackingTouch(SeekBar arg0) {
 						// Obligatory implementation by class inheritance
 
 					}
 
 					@Override
 					public void onStopTrackingTouch(SeekBar arg0) {
 						// Obligatory implementation by class inheritance
 					}
 				});
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	// TODO: Take data from input boxes and log.
 	public void onInventoryPreprocessButtonClick(View v) {
 		Intent i = new Intent(this, InventoryPreprocessActivity.class);
 		startActivityForResult(i, InventoryPreprocessActivity_ID);
 	}
 }
