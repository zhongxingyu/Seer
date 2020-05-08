 package com.prototype1.analyticsprototype1;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import edu.channel4.mobilemetrics.sdk.android.LocalyticsSession;
 
 public class MainActivity extends Activity {
 
 	private LocalyticsSession localyticsSession;
 	private final static String LOCALYTICS_APP_KEY = "2b9b47ca4e9178b076524b4-d8a060da-215f-11e2-5ebd-00ef75f32667";
 	private final static String NAME_AGE_EVENT = "record name and age";
 	private final static String NAME_ATTRIB = "name";
 	private final static String AGE_ATTRIB = "age";
 	private final static String BUTTON_PRESSED_EVENT = "button pressed";
 	private final static String BUTTON_ATTRIB = "button";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// Activity Creation Code
 
 		// Instantiate the object
 		this.localyticsSession = new LocalyticsSession(
 				this.getApplicationContext(), // Context used to access device resources
 				LOCALYTICS_APP_KEY); // Application's key
 
 		this.localyticsSession.open(); // open the session
 
 		// At this point, Localytics Initialization is done. After uploads complete, nothing
 		// more will happen due to Localytics until the next time you call it.
 	}
 
 	public void uploadEvent(View view) {
 		EditText nameText = (EditText) findViewById(R.id.name_message);
 		EditText ageText = (EditText) findViewById(R.id.age_message);
 
 		String name = nameText.getText().toString();
 		int age = Integer.parseInt(ageText.getText().toString());
 		
 		LocalyticsSession.MobileMetricsEvent event = localyticsSession.getNewEvent(NAME_AGE_EVENT);
 		event.addAttribute(NAME_ATTRIB, name);
 		event.addAttribute(AGE_ATTRIB, age);
 		event.tagEvent();
 		
 		this.localyticsSession.upload();
 	}
 	
 	public void tagButton1(View view) {
 		buttonPressed("b1");
 	}
 	
 	public void tagButton2(View view) {
 		buttonPressed("b2");
 	}
 	
 	public void tagButton3(View view) {
 		buttonPressed("b3");
 	}
 	
 	private void buttonPressed(String buttonName) {
 		LocalyticsSession.MobileMetricsEvent event = localyticsSession.getNewEvent(BUTTON_PRESSED_EVENT);
 		
 		event.addAttribute(BUTTON_ATTRIB, buttonName);
 				
 		event.tagEvent();
 	}
 
 	public void onPause() {
 		this.localyticsSession.close();
 		this.localyticsSession.upload();
 
 		super.onPause();
 	}
 }
