 package com.example.roboarmapp;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.text.InputType;
 import android.view.KeyEvent;
 import android.view.MenuItem;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 
 public class SettingsActivity extends Activity {
 	
 	private static final String[] items={"10Hz", "50Hz", "100Hz"};
 	EditText ipAddress;
 	EditText portText;
 	EditText sendRateText;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_settings);
 		ipAddress = (EditText)findViewById(R.id.editIP);
 		ipAddress.setRawInputType(InputType.TYPE_CLASS_NUMBER);
 		portText = (EditText)findViewById(R.id.editPort);
 		portText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
 		sendRateText = (EditText)findViewById(R.id.editSendRate);
 		sendRateText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
 		// Show the Up button in the action bar.
 		//getActionBar().setDisplayHomeAsUpEnabled(true);
 		Spinner spin=(Spinner)findViewById(R.id.accelHZ);
 		//spin.setOnItemSelectedListener(this);
 		ArrayAdapter<String> aa=new ArrayAdapter<String>(this, R.layout.simple_spinner_item,items);
 		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spin.setAdapter(aa);
 	}
 	
 	@Override
 	public void onResume() {
 		ipAddress.setText(MainActivity.serverIP);
 		portText.setText(MainActivity.serverPort);
 		sendRateText.setText(MainActivity.period);
 		super.onResume();
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)
 	{
 		MainActivity.serverIP = ipAddress.getText().toString();
 		MainActivity.serverPort = portText.getText().toString();
 		MainActivity.period = sendRateText.getText().toString();
 	    if ((keyCode == KeyEvent.KEYCODE_BACK))
 	    {
 	        finish();
 	    }
 	    return super.onKeyDown(keyCode, event);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			finish();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 }
