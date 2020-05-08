 package com.t1dm.t1dmanagementapp;
 
 import java.io.File;
 import java.util.Calendar;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.provider.MediaStore.Images.Media;
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class BeginMonitoring extends Activity {
 
 	private CheckBox cbInsulin;
 	private CheckBox cbMeal;
 	private CheckBox cbMisc;
 
 	private EditText etGlucoseLevel;
 	private EditText etInsulin;
 	private EditText etMeal;
 	private EditText etMisc;
 	
 	private Button btnCapture;
 	private T1DMApplication appContext;
 	private CommonMethods commonMethods = new CommonMethods();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_begin_monitoring);
 		appContext = ((T1DMApplication) getApplication());
 		appContext.setDbHandler(new DatabaseHandler());
 		
 		prepareUI();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.begin_monitoring, menu);
 		return true;
 	}
 
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 
 
 		switch (item.getItemId()) {
 
 		case R.id.menuDone:
 
 				if (saveReading())
 					Toast.makeText(this, "T1DM says, reading saved !!!!", Toast.LENGTH_SHORT).show();
 				else{
 					//startActivity(new Intent(appContext.getContext(), T1DM_Menu.class));
 					Toast.makeText(this, "T1DM says, oops no reading saved !!!!", Toast.LENGTH_SHORT).show();
 				}
 				
 				
 			return true;
 		
 		}
 
 		return super.onOptionsItemSelected(item);
 
 
 
 	}
 
 
 	private void prepareUI() {
 		cbInsulin = (CheckBox) findViewById(R.id.checkBoxInsulin);
 		cbMeal = (CheckBox) findViewById(R.id.checkBoxMeal);
 		cbMisc = (CheckBox) findViewById(R.id.checkBoxMisc);
 
 		etGlucoseLevel = (EditText) findViewById(R.id.editTextGlucoseLevel);
 		etInsulin = (EditText) findViewById(R.id.editTextInsulin);
 		etMeal = (EditText) findViewById(R.id.editTextMeal);
 		etMisc = (EditText) findViewById(R.id.editTextMisc);
 		btnCapture = (Button)findViewById(R.id.btnCapture);
 		
 		cbInsulin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 
 					@Override
 					public void onCheckedChanged(CompoundButton arg0,
 							boolean isChecked) {
 						etInsulin.setEnabled(isChecked);
 					}
 				});
 
 		cbMeal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
 				etMeal.setEnabled(isChecked);
 
 			}
 		});
 
 		cbMisc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
 				etMisc.setEnabled(isChecked);
 			}
 		});
 		
 		btnCapture.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Calendar calendar = Calendar.getInstance();
 				File file = new File(commonMethods.APP_PATH + File.separator + commonMethods.CAPTURE_FOLDER , "T1DM_"+Long.toString(calendar.getTimeInMillis())+".jpeg");
 			    Uri uri = Uri.fromFile(file);	       
 
 	         Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 	                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
 	                startActivityForResult(cameraIntent, 0);
 				
 			}
 		});
 		
 	}
 
 	@Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
 
         if( requestCode == 0 && resultCode == RESULT_OK )
         	Toast.makeText(getApplicationContext(), "T1DM says,  your capture is saved", Toast.LENGTH_SHORT).show();
         else
         	Toast.makeText(getApplicationContext(), "T1DM says, oops capture not saved", Toast.LENGTH_SHORT).show();
 
 
     }
 	private boolean saveReading()
 	{
 		boolean retVal = false;
 		try{
 		MonitoringReadings reading = new MonitoringReadings();
 		reading.set_Reading(Integer.parseInt(etGlucoseLevel.getText().toString()));
 		
 		if (cbInsulin.isChecked() && (etInsulin.getText().length()>0))
 		reading.set_Insulin(Integer.parseInt(etInsulin.getText().toString()));
 		
 		if (cbMeal.isChecked() && (etMeal.getText().length()>0))
 		reading.set_Meal(etMeal.getText().toString());
 		
 		if (cbMisc.isChecked() && (etMisc.getText().length()>0))
 		reading.set_Misc(etMisc.getText().toString());
 		
 		Calendar calendar = Calendar.getInstance();
 		reading.set_Timestamp(calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE));
 		int month = calendar.get(Calendar.MONTH)+1;
 		int date = calendar.get(Calendar.DAY_OF_MONTH);
 		reading.set_Date((calendar.get(Calendar.YEAR)+"-"+((month<10)?"0"+month:month)+"-"+((date<10)?"0"+date:date)));
 		
 		if ( appContext.getDbHandler().insertBGReading(reading) >= 1)
 			retVal = true;
 		}catch(Exception e){
 			retVal = false;
 			return retVal;
 		}
 		return retVal;
 	}
 }
