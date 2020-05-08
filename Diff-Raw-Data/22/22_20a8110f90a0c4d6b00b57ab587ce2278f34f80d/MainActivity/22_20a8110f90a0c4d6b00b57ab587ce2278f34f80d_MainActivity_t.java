 package com.example.text;
 
 
 
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.os.Bundle;
 import android.speech.RecognizerIntent;
 import android.speech.SpeechRecognizer;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
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
 	static Socket client;
 	static Socket clientSocket;
 	static ServerSocket  serverSocket;
 //	Connection Client;
 	Boolean ciao=false;
 	Boolean esci=false;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 //		ServerConnect.start();
 
 		speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getBaseContext());
 		talker = new TextToSpeech(this, this);
 		Button btnSpeak = (Button) findViewById(R.id.btn_speak);
		btnSpeak.setBackgroundResource(R.drawable.androido);
 		OnClickListener onClickListener = new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
				    
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
 		String giusto=".*[Ss]i.*|" +
 				".*[Ee]satt.*|" +
 				".*[Pp]erfett.*|" +
 				".*[Gg]iusto.*|" +
 				".*[Cc]orrett.*|" +
 				".*[Rr]agion.*|" +
 				".*[Ff]inalment.*";
 		
 		String sbagliato=".*[Nn]o.*|" +
 				".*[Nn]eanch.*|" +
 				".*[Ss]bagli.*|" +
 				".*[Nn]on.*|";
 		
 		String name="^[Gg]iord.*|" +
 				"^[Jj]arv.*|" +
 				"^[Gg]iard.*|" +
 				"^[Gg]iar.*|" +
 				"^[Gg]ior.*|" ;
 		
 		String inizio="^[Hh]o [Dd]ett.*|";
 		
 		String inizio1="^[Tt]i [Hh]o [Dd]ett.*|"+
 					"^[Tt]i [Ss]to [Dd]ic.*|";
 		
 		String orario=".*[Oo]ra.*|"+
 				".*[Oo]rario.*|";
 		
 		switch (requestCode) {
 		case SPEECHTOTEXT:
 			if (resultCode == RESULT_OK && null != data) {
 				esci=false;
 				if(!ciao){	text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);}
 				else textOther= data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 
 				if(ciao)
 					
 					for(int i=0;i<textOther.size();i++)
 					{
 						String testo=textOther.get(i);
 
 						if(testo.matches(sbagliato)){
 							say("Scusa ho capito male,,,ripeti???");
 							ciao=false;
 							esci=true;
 							while(talker.isSpeaking());
 							break;
 						}
 						else if(testo.matches(giusto)){
 							say(" OK spedisco");
 							while(talker.isSpeaking());
 							ciao=false;
 //							new Connection().execute();
 							new MultiThread("raelixx.ns0.it", 20,new PaccoString(text.get(0)));
 							
 							return;
 						}
 						}
 				
 				
 				while(talker.isSpeaking());
 				if(!esci)
 				{
 					if(text.get(0).matches(name)){
 						String c[]=text.get(0).split("\\s",2);
 						text.add(0, c[1]);
 						}
 					else if(text.get(0).matches(inizio) || (text.get(0).matches(inizio1))){
 						if(text.get(0).matches(inizio1)){
 								String c[]=text.get(0).split("\\s",4);
 								text.add(0, c[3]);}
 						else {
 							String c[]=text.get(0).split("\\s",3);
 							text.add(0, c[2]);}}
 					if(text.get(0).matches(orario)){
 						say("L'ora ,,,");
 						while(talker.isSpeaking());
 						say( java.text.DateFormat.getTimeInstance().format(Calendar.getInstance().getTime()));
 						while(talker.isSpeaking());
 						break;
 					}
 					/*if(text.get(0).matches(orario)){
 						say("L'ora ,,,");
 						while(talker.isSpeaking());
 						say( java.text.DateFormat.getTimeInstance().format(Calendar.getInstance().getTime()));
 						while(talker.isSpeaking());
 						break;
 					}*/
 					else say("Hai detto "+text.get(0)+"?");
 				}
 				
 //Toast.makeText(this,"Hai appena detto "+text.get(0)+"?", Toast.LENGTH_SHORT).show();
 				while(talker.isSpeaking());
 				if(esci)ciao=false;
 				else ciao=true;
 				while(talker.isSpeaking());	
 				listen();
 
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
		ciao=false;
		esci=false;
 		super.onDestroy();
		
 		if (talker != null) {
 			talker.stop();
 			talker.shutdown();
 		}
 	}
 
 }
