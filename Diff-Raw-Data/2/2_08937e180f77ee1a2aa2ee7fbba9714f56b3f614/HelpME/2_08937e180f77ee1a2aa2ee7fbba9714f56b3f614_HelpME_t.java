 package de.sgtgtug.android.hackathon.helpme;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.speech.RecognizerIntent;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
 import android.telephony.SmsManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.Toast;
 
 public class HelpME extends Activity implements OnInitListener, OnUtteranceCompletedListener {
 	private static String LOG_TAG = "HelpME";
 	
 	private static final String TTS_UTTERANCE_ID_FINAL_HELP_MSG = "text_after_help_msg";
 	private static final String TTS_UTTERANCE_ID_BEFORE_HELP_MSG = "text_before_help_msg";
 	private static final int VOICE_RECOGNITION_REQUEST_CODE = 0x000;
 	private static final int TTS_CHECK_CODE = 0x001;
 	private static final int DIALOG_NO_TTS = 0x002;
 	private static final int DIALOG_ABORT_TTS = 0x003;
 
 	private static final int MENU_SETUP = 0x004;
 	private static final int MENU_ABOUT = 0x005;
 
 	
 	private boolean USE_SPEECH_SERVICES;
 	private boolean USE_SMS_MSG;
 	private boolean USE_EMAIL_MSG;
 	private boolean STT_AVAILABLE = false;
 
 	private Locale locale = Locale.US;
 	private TextToSpeech mTts = null;
 
 	private SharedPreferences sharedPrefs;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// hide title and force portrait mode
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		// ---use portrait mode only---
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
 		setContentView(R.layout.main);
 		
 		checkforSpeechServices();
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		sharedPrefs = PreferenceManager
 		.getDefaultSharedPreferences(getApplicationContext());
 		USE_SPEECH_SERVICES = sharedPrefs.getBoolean(HelpMePreferences.PREFERENCE_USE_SPEECH_SERVICES, true);
 		USE_SMS_MSG = sharedPrefs.getBoolean(HelpMePreferences.PREFERENCE_USE_SMS_MSG, true);
 		USE_EMAIL_MSG = sharedPrefs.getBoolean(HelpMePreferences.PREFERENCE_USE_EMAIL_MSG, true);
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		menu.clear();
 		menu.add(0, MENU_SETUP, 0, R.string.settings_title).setIcon(
 				android.R.drawable.ic_menu_preferences);
 		menu.add(0, MENU_ABOUT, 0, R.string.about_title).setIcon(
 				android.R.drawable.ic_menu_info_details);
 
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case MENU_SETUP:
 			onSetupClick(new View(getApplicationContext()));
 			break;
 		case MENU_ABOUT:
 			onAboutClick(new View(getApplicationContext()));
 			break;
 
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void onSetupClick(View v) {
 		startActivity(new Intent(getApplicationContext(),
 				HelpMePreferences.class));
 
 	}
 
 	public void onAboutClick(View v) {
 		/*
 		 * TODO: add About dialog
 		 */
 		Toast.makeText(getApplicationContext(),
 				"TODO: Add some application info", Toast.LENGTH_SHORT).show();
 	}
 
 	public void onButtonHelpClick(View v) {
 		if (USE_SPEECH_SERVICES && STT_AVAILABLE) {
 			HashMap<String, String> ttsParams = new HashMap<String, String>();
 			ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, TTS_UTTERANCE_ID_BEFORE_HELP_MSG);
 			mTts.speak(getString(R.string.tts_speak_hlp_msg_now), TextToSpeech.QUEUE_FLUSH,
 					ttsParams);
 		} else {
 			requestHelp(null);
 		}
 	}
 
 	protected Dialog onCreateDialog(final int pDialogID) {
 		switch (pDialogID) {
 		case DIALOG_NO_TTS:
 			return new AlertDialog.Builder(this)
 					.setTitle("STT-Error")
 					.setIcon(android.R.drawable.ic_dialog_alert)
 					.setMessage("Sorry, tts service not installed...fetch()?")
 					.setNegativeButton(R.string.tts_cancel,
 							new Dialog.OnClickListener() {
 								public void onClick(
 										final DialogInterface pDialog,
 										final int pWhich) {
 									dismissDialog(pWhich);
 								}
 							})
 					.setPositiveButton(R.string.tts_install,
 							new Dialog.OnClickListener() {
 								public void onClick(
 										final DialogInterface pDialog,
 										final int pWhich) {
 									Intent installIntent = new Intent();
 									installIntent
 											.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
 									startActivity(installIntent);
 								}
 							}).create();
 		case DIALOG_ABORT_TTS:
 			return new AlertDialog.Builder(this)
 			.setTitle("Abort TTS")
 			.setIcon(android.R.drawable.ic_dialog_alert)
 			.setMessage("Cancel Speaking?")
 			.setPositiveButton(R.string.tts_cancel,
 					new Dialog.OnClickListener() {
 						public void onClick(
 								final DialogInterface pDialog,
 								final int pWhich) {
 							mTts.stop();
 						}
 					}).create();
 		default:
 			return super.onCreateDialog(pDialogID);
 		}
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
 			if (resultCode == RESULT_OK) {
 				ArrayList<String> matches = data
 						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 				requestHelp(matches);
 			} else {
 				requestHelp(null);
 			}
 		}
 
 		if (requestCode == TTS_CHECK_CODE) {
 			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
 				mTts = new TextToSpeech(this, this);
 			} else {
 				this.showDialog(DIALOG_NO_TTS);
 			}
 		}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	protected void onDestroy() {
 		if (mTts != null)
 			mTts.shutdown();
 		super.onDestroy();
 	}
 	
 	/**
 	 * Called when tts engine finished initialization
 	 * 
 	 * @param int status code which can be checked for success/error of init.
 	 */
 	public void onInit(int status) {
 		if(status==TextToSpeech.SUCCESS){
 			mTts.setLanguage(locale);
 			//set UtteranceCompleted to get notified when TTS speaking finishes
			mTts.setOnUtteranceCompletedListener(this);
 		}
 		else if(status==TextToSpeech.ERROR)
 			  mTts.shutdown();
 	}
 	
 	/**
 	 * Implements OnUtteranceCompletedListener Interface.
 	 * Fires when an utterance is complete.
 	 * 
 	 * @param String utteranceId ID which was passed to speak() method...
 	 */
 	public void onUtteranceCompleted(String utteranceId) {
 		if(utteranceId.equals(TTS_UTTERANCE_ID_BEFORE_HELP_MSG))
 			startVoiceRecognitionActivity();
 		if(utteranceId.equals(TTS_UTTERANCE_ID_FINAL_HELP_MSG))
 			dismissDialog(DIALOG_ABORT_TTS);
 	}
 
 	/**
 	 * Checks if text-to-speech/speech-to-text services are available on device.
 	 * If not TTS will be installed
 	 */
 	private void checkforSpeechServices() {
 		Intent checkIntent = new Intent();
 		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
 		startActivityForResult(checkIntent, TTS_CHECK_CODE);
 
 		PackageManager pm = getPackageManager();
 		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
 				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
 		if (activities.size() > 0)
 			STT_AVAILABLE = true;
 		else
 			Log.w(LOG_TAG, "Recognizer not present");
 	}
 
 	/**
 	 * Reads contacts which should be notified and triggers sending help message
 	 * 
 	 * @param voiceText
 	 *            an array containing voice recognition help message text
 	 */
 	private void requestHelp(ArrayList<String> voiceText) {
 		String helpMsg = createHelpMsg(voiceText != null
 				&& voiceText.size() > 0 ? voiceText.get(0) : "");
 		// does not work yet...
 		// final List<HelpME.BrokerContact> allEmergencyContacts =
 		// PreferencesUtil
 		// .getAllEmergencyContacts(this);
 		ArrayList<HelperContact> testContact = new ArrayList<HelperContact>();
 		HelperContact c1 = new HelperContact("5556", "gtugna@googlemail.com");
 		testContact.add(c1);
 		sendHelpMsgs(testContact, helpMsg);
 		
 		if(USE_SPEECH_SERVICES) {
 			HashMap<String, String> ttsParams = new HashMap<String, String>();
 			ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, TTS_UTTERANCE_ID_FINAL_HELP_MSG);
 			
 			mTts.speak(getString(R.string.tts_message_sent) + helpMsg,
 					TextToSpeech.QUEUE_FLUSH, ttsParams);
 			showDialog(DIALOG_ABORT_TTS);
 		}
 	}		
 
 	/**
 	 * Choose message types and send sms/email
 	 * 
 	 * @param contacts
 	 *            list of contacts which should be notified
 	 */
 	private void sendHelpMsgs(List<HelperContact> contacts, String msg) {
 		for (HelperContact contact : contacts) {
 			if (USE_SMS_MSG)
 				sendSMS(msg.toString(), contact.sms);
 			if (USE_EMAIL_MSG)
 				sendEmail(msg.toString(), contact.email);
 		}
 	}
 
 	/**
 	 * Creates a help message consisting of geo-location and voice message
 	 * 
 	 * @param voiceMsg
 	 *            String representation of recorded voice message
 	 * @return Returns a message containing geo-location and help message
 	 */
 	private String createHelpMsg(String voiceMsg) {
 		StringBuffer msgBfr = new StringBuffer();
 		String name = sharedPrefs.getString(HelpMePreferences.PREFERENCE_USER_NAME, "");
 		String sex = sharedPrefs.getString(HelpMePreferences.PREFERENCE_USER_SEX, "");
 		String age = sharedPrefs.getString(HelpMePreferences.PREFERENCE_USER_AGE, "");
 		String securityNr = sharedPrefs.getString(HelpMePreferences.PREFERENCE_SECURITY_NR, "");
 		msgBfr.append(getString(R.string.helpmsg_helpMe) + "\n");
 		msgBfr.append(getString(R.string.helpmsg_iam) + "\n");
 		msgBfr.append(name + "\n" + sex + "\n" + age + "\n" + securityNr + "\n");
 		msgBfr.append(getEmergencyLocation());
 		msgBfr.append(R.string.tts_voice_msg + voiceMsg + "\n");
 		return msgBfr.toString();
 	}
 
 	/**
 	 * Sends help Email to contacts
 	 * 
 	 * @param message
 	 *            the message to be sent
 	 * @param sendTo
 	 *            the recipient of the help message
 	 */
 	private void sendEmail(String message, String sendTo) {
 		/*
 		 * TODO: remove for production mode
 		 */
 		Toast.makeText(getApplicationContext(),
 				"TODO: Email triggered remove in prod mode: )" + message, Toast.LENGTH_LONG).show();
 		
 		final Intent intent = new Intent(Intent.ACTION_SEND);
 		intent.setType("plain/text");
 		intent.putExtra(Intent.EXTRA_EMAIL, new String[] { sendTo });
 		intent.putExtra(Intent.EXTRA_SUBJECT, "Help Me!");
 		intent.putExtra(Intent.EXTRA_TEXT, message);
 		startActivity(intent);
 	}
 
 	/**
 	 * Sends help SMS to contacts
 	 * 
 	 * @param msg
 	 *            the message to be sent
 	 * @param sendTo
 	 *            mobile number of recipient
 	 */
 	private void sendSMS(String msg, String sendTo) {
 		SmsManager smsMngr = SmsManager.getDefault();
 
 		ArrayList<String> chunkedMessages = smsMngr.divideMessage(msg);
 		for (String messageChunk : chunkedMessages)
 		/*
 		 * TODO: uncomment for production mode
 		 * smsMngr.sendTextMessage(sendTo, this.getString(R.string.app_name),messageChunk, null, null);
 		 */
 		Toast.makeText(getApplicationContext(),
 				"SMS send, remove in prod. mode msg: " + messageChunk, Toast.LENGTH_LONG).show();
 	}
 
 	/**
 	 * Builds current location and reverse geocoded address
 	 * 
 	 * @return A String containing the current location + address
 	 */
 	private String getEmergencyLocation() {
 		StringBuffer locBuf = new StringBuffer();
 		Location currLoc = getCurrentLocation();
 		if (currLoc != null) {
 			Log.i(LOG_TAG, "Current  Location -> Lat: " + currLoc.getLatitude()
 					+ "Long: " + currLoc.getLongitude());
 			locBuf.append(R.string.tts_iam_at + currLoc.getLatitude() + " Long: "
 					+ currLoc.getLongitude() + "\n");
 		}
 
 		List<Address> addresses = resolveLocation(currLoc);
 		if (addresses != null && !addresses.isEmpty()) {
 			Address currentAddress = addresses.get(0);
 			if (currentAddress.getMaxAddressLineIndex() > 0) {
 				for (int i = 0; i < currentAddress.getMaxAddressLineIndex(); i++) {
 					locBuf.append(currentAddress.getAddressLine(i));
 					locBuf.append("\n");
 				}
 			} else {
 				if (currentAddress.getPostalCode() != null)
 					locBuf.append(currentAddress.getPostalCode());
 			}
 		}
 		return locBuf.toString();
 	}
 
 	/**
 	 * Gets last known location from the device
 	 * 
 	 * @return Location
 	 */
 	private Location getCurrentLocation() {
 		LocationManager locMngr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		Location currLoc = null;
 		currLoc = locMngr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		if (currLoc == null) {
 			currLoc = locMngr
 					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 		}
 		return currLoc;
 	}
 
 	/**
 	 * Reverse geocoding of current location
 	 * 
 	 * @param currLoc
 	 *            current location
 	 * @return List address locations
 	 */
 	private List<Address> resolveLocation(Location currLoc) {
 		Geocoder gCoder = new Geocoder(getApplicationContext(),
 				Locale.getDefault());
 		try {
 			return gCoder.getFromLocation(currLoc.getLatitude(),
 					currLoc.getLongitude(), 1);
 		} catch (Exception e) {
 			Log.e(LOG_TAG,
 					"Could not resolve GeoLocation, here is what i know: "
 							+ e.getMessage());
 			return null;
 		}
 	}
 
 	/**
 	 * Starts the voice recognition activity from google
 	 */
 	private void startVoiceRecognitionActivity() {
 		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
 				getLanguageModel());
 		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, R.string.tts_speak_hlp_msg_now_dialog);
 		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toString());
 		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
 	}
 
 	/**
 	 * Returns the language model to be used with speech recognition
 	 * 
 	 * @return a constant indicating which language model is used
 	 * 
 	 * @TODO let user choose from settings in future.
 	 * 
 	 *       For now default: LANGUAGE_MODEL_WEB_SEARCH
 	 * 
 	 * */
 	private String getLanguageModel() {
 		return true ? RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
 				: RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;
 	}
 
 	/**
 	 * A class which represent a contact that has to be notified in emergency
 	 * situations
 	 */
 	public static class HelperContact {
 		public String sms;
 		public String email;
 
 		public HelperContact(String sms, String email) {
 			this.sms = sms;
 			this.email = email;
 		}
 	}
 }
