 package com.ubicomp.iseesomethingyoudont;
 
 import java.util.Locale;
 import java.util.UUID;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.support.v4.view.GestureDetectorCompat;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.TextView;
 import com.h5n1.eventsys.EventSystem;
 import com.h5n1.eventsys.JsonRequester;
 import com.h5n1.eventsys.events.EventState;
 import com.h5n1.eventsys.events.NavigationEvent;
 import com.h5n1.eventsys.events.RFIDEvent;
 import com.h5n1.eventsys.events.RFIDEvent.RFIDEventType;
 import com.h5n1.hardwareServices.GestureServices;
 import com.h5n1.hardwareServices.HapticalFeedbackServices;
 import com.h5n1.hardwareServices.LocationServices;
 import com.ubicomp.iseesomethingyoudont.util.SystemUiHider;
 
 
 public class ControlActivity extends Activity implements OnTouchListener, OnInitListener{
 
 	private GestureDetectorCompat detector = null;
 	private static final String DEBUG_TAG = "Gestures";
 	private static final int DATA_CHECK_CODE = 0;
 	private EventToSpeechSynthesis eventToSpeechSynthesis = null;
 	private EventSystem eventSystem;
 	private EventHandler eventHandler = null;
 	private String deviceId = "42";
 	private LocationServices locationServices;
 	private HapticalFeedbackServices vibrator;
 	private GestureServices gestures;
 	private static final boolean TOGGLE_ON_CLICK = false; // set=will toggle systemUI visibility upon interaction. Otherwise will show systemUI visibility upon interaction.
 	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION; // The flags to pass to {@link SystemUiHider#getInstance}.
 	private SystemUiHider mSystemUiHider; // The instance of the {@link SystemUiHider} for this activity.
 	private TextView gestureText = null; // DEBUG
 
 	// Called when app is created (not when displayed)
 	@Override // Main method of the android application
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		
		// Creates a unique device id
		createDeviceId();
		// initialises the json requester
		JsonRequester.setDeviceID(deviceId);
		
 		setContentView(R.layout.activity_control);
 		final View controlsView = findViewById(R.id.fullscreen_content_controls);
 		final View contentView = findViewById(R.id.fullscreen_content);
 		gestureText = (TextView) findViewById(R.id.gestureText);
 
 		// WENN DEAKTIVIERT, ERKENNT ER ALLE GESTEN ALS LONG PRESS
 		// Set up the user interaction to manually show or hide the system UI.
 		contentView.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				if (TOGGLE_ON_CLICK) {} 
 				else {}}});
 				
 		// Check if speak engine is installed??
 		Intent checkIntent = new Intent();
 		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
 		startActivityForResult(checkIntent, DATA_CHECK_CODE);
 		// Starts all hardware services
 		//enableHardwareServices();
 	}	
 	
 	// Initialises all Hardware Services
 	public void enableHardwareServices(){
 		final View contentView = findViewById(R.id.fullscreen_content);
 		// apply a touch listener to the view
 		contentView.setOnTouchListener(this);
 		// Creates a vibrator mechanism
 		vibrator = new HapticalFeedbackServices(this);
 		// Creates the recognition of gestures -- VERURSACHT NOCH FEHLER WEGEN DER SPEECH SYNTHESIS
 		gestures = new GestureServices(gestureText, vibrator, eventToSpeechSynthesis, this);
 		// implementation of GestureDetector.OnGestureListener
 		detector = new GestureDetectorCompat(this, gestures);
 		// Creates location Services, GPS and WIFI Location
 		locationServices = new LocationServices(vibrator, eventToSpeechSynthesis, this);
 		// Creates event system
 		eventSystem = EventSystem.getInstance();
 		// Creates the event handler
 		eventHandler = new EventHandler(eventSystem, eventToSpeechSynthesis);
 	}
 	
 	// Creates the option menu
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	
 	// Tries to find the ttsengine, if not installed, install it
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == DATA_CHECK_CODE) {
 			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
 				// ttesengine is  installed, create the TTS instance
 				eventToSpeechSynthesis = new EventToSpeechSynthesis(this);
 			} else {
 				// ttsengine is not installed, install it
 				Intent installIntent = new Intent();
 				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
 				startActivity(installIntent);
 			}
 		}
 	}
 
 	// Called when a new thing? is initiated, here: ttsengine
 	@Override
 	public void onInit(int status) {
 		eventToSpeechSynthesis.getTtsengine().setLanguage(Locale.GERMAN);
 		enableHardwareServices();
 	}
 	
 	public void createRFIDEvent() {
 		float mass = 100;
 		// b, h, t
 		float[] size = { 40, 160, 60 };
 		RFIDEvent event = new RFIDEvent(JsonRequester.getDeviceID(), RFIDEventType.NEW_TAG, "Oma", size, mass);
 		//event.setState(EventState.)
 		EventSystem.pushEvent(event);
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	@Override
 	public boolean onTouch(View view, MotionEvent event) {
 		this.detector.onTouchEvent(event);
 		return super.onTouchEvent(event);
 	}
 
 	// Creates a unique device id
 	private void createDeviceId() {
 		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
 		final String tmDevice, tmSerial, androidId;
 		
 		tmDevice = "" + tm.getDeviceId();
 		tmSerial = "" + tm.getSimSerialNumber();
 		
 		androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
 		UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
 		deviceId = deviceUuid.toString();
 	}
 
 	
 
 
 
 	// ===========================================================================
 	// # Automatically Created Content Below
 	// ===========================================================================
 
 	/**
 	 * Whether or not the system UI should be auto-hidden after
 	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
 	 */
 	//////////////////////////////__________________________________________________
 
 	@Override
  	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 
 		// Trigger the initial hide() shortly after the activity has been
 		// created, to briefly hint to the user that UI controls
 		// are available.
 		//delayedHide(100);
 	}
 }
