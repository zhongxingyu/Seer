 package au.net.ravex.xdroid;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 import android.speech.RecognizerIntent;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 
 public class xdroid extends Activity implements
 		android.view.View.OnClickListener, OnInitListener {
 
 	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
 	private static final int TTS_REQUEST_CODE = 1235;
 	//private static final String XNET_URL = "http://192.168.2.8/xnet_dev/xnetos/lib/brain.preprocessAjax.php";
 	private static final String XNET_URL = "http://nexos.ravex.net.au/lib/brain.preprocessAjax.php";
 	private static final String BOT_NAME = "TestBot"; //FIXME xnet
 	private static final String HELLO = "Online";
 	private static final String CALLER = "xdroid";
 
 	private TextToSpeech tts;
 	private ListView mList;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.main);
 
 		// Get display items for later interaction
 		Button speakButton = (Button) findViewById(R.id.btn_speak);
 
 		mList = (ListView) findViewById(R.id.list);
 
 		// Setup tts
 		Intent checkIntent = new Intent();
 		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
 		startActivityForResult(checkIntent, TTS_REQUEST_CODE);
 
 		// Check to see if a recognition activity is present
 		PackageManager pm = getPackageManager();
 		List activities = pm.queryIntentActivities(new Intent(
 				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
 		if (activities.size() != 0) {
 			// startVoiceRecognitionActivity();
 			speakButton.setOnClickListener(this);
 			Log.d("Recognised speech engine", "");
 		} else {
 			speakButton.setEnabled(false);
 			speakButton.setText("Recognizer not present");
 		}

 	}
 
 	public void onInit(int status) {
 		tts.setLanguage(Locale.US);
 		tts.speak(HELLO, TextToSpeech.QUEUE_FLUSH, null);
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v.getId() == R.id.btn_speak) {
 			startVoiceRecognitionActivity();
 		}
 	}
 
 	/**
 	 * Fire an intent to start the speech recognition activity.
 	 */
 	private void startVoiceRecognitionActivity() {
 		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
 				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk to XNET");
 		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
 	}
 
 	/**
 	 * Handle the results from the recognition activity.
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE
 				&& resultCode == RESULT_OK) {
 
 			// Fill the list view with the strings the recognizer thought it
 			// could have heard
 			ArrayList<String> matches = data
 					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 			mList.setAdapter(new ArrayAdapter<String>(this,
 					android.R.layout.simple_list_item_2, matches));
 
 			Log.d("MATCHED WORD", matches.get(0));
 			String toSend = matches.get(0).replace(" ", "+"); //Need to strip illegal chars
 			toSend = toSend.replace("#", ""); /// cleanse
 			
 			// Go and talk to XNET now
 			DefaultHttpClient httpclient = new DefaultHttpClient();
 			HttpPost httpost = new HttpPost(XNET_URL);
 			
 			// Add your data    
 			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();    
 			nameValuePairs.add(new BasicNameValuePair("botname", BOT_NAME));      
 			nameValuePairs.add(new BasicNameValuePair("input", toSend));      
 			nameValuePairs.add(new BasicNameValuePair("caller", CALLER)); 
 			
 			try {
 				httpost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 				Log.d("HTTP POST", httpost.getURI().toString());
 				try {
 					HttpResponse response = httpclient.execute(httpost);
 					Log.d("HTTP POST RESP",response.toString());
 					Log.d("HTTP POST STATUS",String.valueOf(response.getStatusLine().getStatusCode()));
 					Log.d("HTTP POST REASON",response.getStatusLine().getReasonPhrase());
 					
 					HttpEntity entity = response.getEntity();
 					String toTalk = convertStreamToString(entity.getContent());
 					ArrayList<String> array = new ArrayList<String>();
 					array.add(toTalk);
 					mList.setAdapter(new ArrayAdapter<String>(this,
 							android.R.layout.simple_list_item_1, array));
 					Log.d("XNET RESPONSE",toTalk);
 					
 					tts.speak(toTalk, TextToSpeech.QUEUE_FLUSH, null);
 					entity.consumeContent();
 				} catch (ClientProtocolException e) {
 					Log.e("CPE", e.getMessage());
 				} catch (IOException e) {
 					Log.e("IOE", e.getMessage());
 				}
 			} catch (UnsupportedEncodingException e1) {
 				Log.e("UEE", e1.getMessage());
 			}    
 		
 			// startVoiceRecognitionActivity();
 		} else if (requestCode == TTS_REQUEST_CODE) {
 			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
 				// success, create the TTS instance
 				tts = new TextToSpeech(this, this);
 			} else {
 				// missing data, install it
 				Intent installIntent = new Intent();
 				installIntent
 						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
 				startActivity(installIntent);
 			}
 		}
 
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	public String convertStreamToString(InputStream is) throws IOException {
 		/*
 		 * To convert the InputStream to String we use the
 		 * BufferedReader.readLine() method. We iterate until the BufferedReader
 		 * return null which means there's no more data to read. Each line will
 		 * appended to a StringBuilder and returned as String.
 		 */
 		if (is != null) {
 			StringBuilder sb = new StringBuilder();
 			String line;
 
 			try {
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(is, "UTF-8"));
 				while ((line = reader.readLine()) != null) {
 					sb.append(line).append("\n");
 				}
 			} finally {
 				is.close();
 			}
 			return sb.toString();
 		} else {
 			return "";
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		tts.shutdown();
 	}
 }
