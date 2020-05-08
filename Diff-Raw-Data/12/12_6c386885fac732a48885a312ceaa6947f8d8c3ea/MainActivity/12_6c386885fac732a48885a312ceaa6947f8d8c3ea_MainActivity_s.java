 package com.mutableconst.chatserver.gui;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.Menu;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 
 import com.mutableconst.android.dashboard_manager.AndroidEventManager;
 import com.mutableconst.protocol.ConnectionType;
 
 public class MainActivity extends Activity {
 	
 	private SharedPreferences sharedPreferences;	
 	private String preferenceKey = "PREFERENCES";
 	private ConnectionType connectionType;
 	// Reference to ConnectionType Enum class name, used for getting
 	// SharedPreferences connection type name (eg. "WIFI")
 	private String connectionTypeName = ConnectionType.class.getSimpleName();
 	// Reference to ConnectionType Enum class name + Id, used for
 	// accessing sharedPreferences RadioButton id (eg. R.id.buttonId)
 	private String referenceId = ConnectionType.class.getSimpleName() + "Id";
 
 	
 	// Connection details
 	// public static ConnectionType CONNECTION_TYPE = ConnectionType.ADB;
 	// public stati192.168.1.132"c String LOCALHOST = "10.0.2.2";
 
 	// TIMEOUT of connection, in seconds
 	// public static int TIMEOUT = 10;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		AndroidEventManager.getAndroidEventManager().setupEnvironment(this);
 		new AndroidConnection(this);
 		
 		// TODO: Runtime shutdown handler thread
 		// TODO: Communication protocol
 		
 		sharedPreferences = getSharedPreferences(preferenceKey, 0);
 		connectionType = ConnectionType.valueOf(sharedPreferences.getString(connectionTypeName, ConnectionType.WIFI.name()));
 
 		/**
 		 * Get the radio group for selecting the connection type. 
 		 * Check if a radio button has been selected, if so, select it.
 		 */
 		RadioGroup connectionSelection = (RadioGroup) findViewById(R.id.connectionSelection);
 		int selectedRadioButtonId = sharedPreferences.getInt(referenceId, -1);
 		if(selectedRadioButtonId != -1)
 		{
 			RadioButton checkedButton = (RadioButton) findViewById(selectedRadioButtonId);
 			checkedButton.setChecked(true);
 		}
 
 		/**
 		 * Create listener to update the pref when a new connection type is selected
 		 */
 		connectionSelection.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(RadioGroup group, int checkedId) {
 				SharedPreferences.Editor editor = sharedPreferences.edit();
 				RadioButton selectedRadioButton = (RadioButton) findViewById(checkedId);
 				
 				String selectedOption = selectedRadioButton.getText().toString().toUpperCase();
 				editor.putString(connectionTypeName, selectedOption);
 				editor.putInt(referenceId, checkedId);
 				editor.commit();
 				
 				connectionType = ConnectionType.valueOf(selectedOption);
 			}
 		});
 
 		// Quit button
 		Button quitButton = (Button) findViewById(R.id.quitButton);
 		quitButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				System.exit(0);
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
