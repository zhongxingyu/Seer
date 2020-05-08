package com.domotica.jarvise;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.os.Bundle;
 import android.os.Environment;
 import android.speech.RecognizerIntent;
 import android.speech.SpeechRecognizer;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.view.Menu;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
import com.domotica.jarvise.R;
 
 /**
  * @author raelix
  *
  */
 public class MainActivity extends Activity  implements OnInitListener {
 	@SuppressWarnings("unused")
 	private SpeechRecognizer speechRecognizer;
 	private final int SPEECHTOTEXT = 1;
 	static TextToSpeech talker;
 	ArrayList<String> text;
 	ArrayList<String> textOther;
 	ArrayList<String> trueChoose;
 	ArrayList<String> falseChoose;
 	static PrintWriter printwriter;
 	Jarvise jarvis;
 	static Socket client;
 	static Socket clientSocket;
 	static ServerSocket  serverSocket;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 		jarvis = new Jarvise();
 		speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getBaseContext());
 		talker = new TextToSpeech(this, this);
 		Button btnSpeak = (Button) findViewById(R.id.btn_speak);
 		btnSpeak.setBackgroundResource(R.drawable.androido);
 		File file = new File(Environment.getExternalStorageDirectory()+"/jarvise.txt");
 		if (!file.exists()) {
 		        try {
 		            file.createNewFile();
 		            BufferedWriter br = new BufferedWriter(new FileWriter(file));
 		            br.write("host address / ip address");
 		            br.write("\n");
 		            br.write("username");
 		            br.write("\n");
 		            br.write("password");
 		            br.flush();
 		            br.close();
 		        } catch (IOException e) {
 		        	System.out.println("erroree creazione file");
 		            e.printStackTrace();
 		        }
 		}
 		OnClickListener onClickListener = new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				jarvis.domanda = true;
 				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 				PackageManager pm = getPackageManager();
 				List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
 				if(activities.size()<=0){
 					Toast.makeText(getBaseContext(),
 							"No Activity found to handle the action ACTION_RECOGNIZE_SPEECH",
 							Toast.LENGTH_SHORT).show();
 					return;
 				}
 				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
 						RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 				intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Jarvin..");
 				startActivityForResult(intent, SPEECHTOTEXT);
 			}
 		};
 		btnSpeak.setOnClickListener(onClickListener);
 	}
 
 
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		switch (requestCode) {
 		case SPEECHTOTEXT:
 			if (resultCode == RESULT_OK && null != data) {
 				text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 				jarvis.fine = false;
 				jarvis.execute(text.get(0));
 				if(!jarvis.fine)listen();
 			}
 		}
 	}
 
 
 
 	public static void say(String text2say){
 		talker.setPitch(0.55f);
 		talker.speak(text2say, TextToSpeech.QUEUE_FLUSH, null);
 		talker.setPitch(0.55f);
 	}
 
 	public void listen(){
 		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 		PackageManager pm = getPackageManager();
 		List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
 		if(activities.size()<=0){
 			Toast.makeText(getBaseContext(),
 					"No Activity found to handle the action ACTION_RECOGNIZE_SPEECH",
 					Toast.LENGTH_SHORT).show();
 			return;
 		}
 		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
 				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Jarvin..");
 		startActivityForResult(intent, SPEECHTOTEXT);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public void onInit(int arg0) {
 
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 
 		if (talker != null) {
 			talker.stop();
 			talker.shutdown();
 		}
 	}
 
 }
