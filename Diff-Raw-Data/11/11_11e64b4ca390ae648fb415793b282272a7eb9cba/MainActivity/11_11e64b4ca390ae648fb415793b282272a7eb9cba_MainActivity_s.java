 package com.prototype1.analyticsprototype1;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import edu.channel4.mobilemetrics.sdk.android.LocalyticsSession;
 //import com.localytics.android.LocalyticsSession;
 
 public class MainActivity extends Activity {
 
 	private LocalyticsSession localyticsSession;
 	private final static String LOCALYTICS_APP_KEY = "2b9b47ca4e9178b076524b4-d8a060da-215f-11e2-5ebd-00ef75f32667";
 	private Map<String, String> dataMap = new HashMap<String, String>();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// Activity Creation Code
 
 		// Instantiate the object
 		this.localyticsSession = new LocalyticsSession(
 				this.getApplicationContext(), // Context used to access device
 												// resources
 				LOCALYTICS_APP_KEY); // Application's key
 
 		this.localyticsSession.open(); // open the session
		this.localyticsSession.upload(); // upload any data
 
 		// At this point, Localytics Initialization is done. After uploads
 		// complete nothing
 		// more will happen due to Localytics until the next time you call it.
 	}
 
 	public void recordData() {
 		EditText nameText = (EditText) findViewById(R.id.name_message);
 		EditText ageText = (EditText) findViewById(R.id.age_message);
 
 		String name = nameText.getText().toString();
 		String age = ageText.getText().toString();
 
 		dataMap.put("name", name);
 		dataMap.put("age", age);
 	}
 
 	public void uploadEvent(View view) {
 		recordData();
 		localyticsSession.tagEvent("record first data", dataMap);
 		this.localyticsSession.upload();
 		this.localyticsSession.close();
 	}
 
 	/*
 	 * public void onResume() { super.onResume(); this.localyticsSession.open();
 	 * }
 	 */
 
 	public void onPause() {
 		super.onPause();
 		this.localyticsSession.upload(); // upload any data
 		// this.localyticsSession.close();
 		// this.localyticsSession.upload(); why was this being called twice?
 	}
 }
