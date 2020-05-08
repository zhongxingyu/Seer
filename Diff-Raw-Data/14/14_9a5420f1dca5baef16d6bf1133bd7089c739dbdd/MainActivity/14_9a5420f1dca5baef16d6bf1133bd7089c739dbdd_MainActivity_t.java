 package com.megamip.voice;
 
 import java.util.ArrayList;
 
 import com.megamip.voice.R;
 import android.os.Bundle;
 import android.os.Handler;
 import android.app.Activity;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.Intent;
 import android.speech.RecognizerIntent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
 import android.webkit.WebView;
import android.webkit.WebSettings.PluginState;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	
	
	public static final int USER_MOBILE  = 0;
	public static final int USER_DESKTOP = 1;
	
 	public static  Context context;  // reference vers l'activit MainActivity 
 	protected static final int RESULT_SPEECH = 1;
 	public static final String TAG = "A3";
 	private ImageButton btnSpeak;
 	private TextView textView;
 	private EditText editText1;
 	private WebView webView;
 	private Button  btnNext, btnShow, btnHome;
 	private String accountType = null;
 	private String accountName = null;
 	private Handler handler = null;
 
 	private static final String HTML_ROOT = "file:///mnt/sdcard/DCIM/gui/";	
 	
 	private Command mCommand;
 	private MipCommand.Next nextCommand;
 	
 	private Invoker invoker;
 	private MipReceiver receiver;
 	private MipCommand mc;
 
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		context = this;
 	
 		webView = (WebView) findViewById(R.id.webView1);
 		handler = new Handler();
 	    webView.getSettings().setJavaScriptEnabled(true);  
	    webView.setWebChromeClient(new WebChromeClient() {
		});
		webView.getSettings().setPluginState(PluginState.ON);
		webView.getSettings().setUserAgent(USER_DESKTOP);
         webView.addJavascriptInterface(this, "contactSupport");    
         setWidgets();
         setListeners();
         loadPage("index.html");
         
         invoker = new Invoker();
         receiver = new MipReceiver(handler, webView);
         mc = new MipCommand();
         
 
 	}
 	
 	public void loadPage(String in) {
 		final String url = HTML_ROOT + in;
 		
 		Log.d(TAG, " loadPage url = "+url);
     	loadURL(url);
 		
 	}
 
 	
 	  private void loadURL(final String in){
 	    	handler.post(new Runnable() {
 	            public void run() {
 	            	webView.loadUrl(in);
 	            }
 	        });
 	    }
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		switch (requestCode) {
 		case RESULT_SPEECH: {
 			if (resultCode == RESULT_OK && null != data) {
 
 				ArrayList<String> text = data
 						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 
 			
 				
 				VoiceCommand voiceCommand = new VoiceCommand(text.get(0));
 				Log.d(TAG,"onActivityResult, avant appel de voiceHandler vc= "+voiceCommand);
 				voiceHandler(voiceCommand);
 			}
 			break;
 		}
 
 		}
 	}
 	
 	
 	
 
 
 
 private void voiceHandler(VoiceCommand command) {
 
 
 
 String[] args = command.getArgs();
 String action = command.getAction();
 
 
 String keywords ="";
 String apiSelect= "";
 
 	for(int i = 0; i < args.length; i++){
 		keywords += args[i]+" ";
 	}
 
 
 
  Log.d(TAG, "WebViewActivity launch - call of callJsFunction ");
 
  
    if(action.equals("picture"))
    mCommand = mc.new PictureSearch(receiver, keywords);
    else
    mCommand = mc.new VideoSearch(receiver, keywords);
    
    invoker.launch(mCommand);
 
 }
 
 private void setListeners() {
 	
 	
 
 	// btnSpeak 
 	
 	btnSpeak.setOnClickListener(new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 
 			Intent intent = new Intent(
 					RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 
 			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
 
 			try {
 				startActivityForResult(intent, RESULT_SPEECH);
 			
 			} catch (ActivityNotFoundException a) {
 				Toast t = Toast.makeText(getApplicationContext(),
 						"Ops! Your device doesn't support Speech to Text",
 						Toast.LENGTH_SHORT);
 				t.show();
 			}
 		}
 	});
 	
 	
 	//--- btnHome
     btnHome.setOnClickListener(new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			String seq = "clearScreen();hideCenterPanel();showEyes";
 		//	callJsFunction(seq, "");
 			
 		}
 	});
 	
 	
 	
 	
 	//--- btnNext 
 	btnNext.setOnClickListener(new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			
 	/*
 	 * 
 	 *  MipCommands mc = new MipCommands();
    psCommand =  mc.new PictureSearch(receiver, keywords);
    invoker.launch(psCommand);
 	 * */
 			
 	mCommand = mc.new Next(receiver);
 	invoker.launch(mCommand);
 			
 		}
 	});
 	
 	//--- btnShow
 	btnShow.setOnClickListener(new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			
 	       
 			
 		}
 	});
 }
 
 private void setWidgets() {
 	
 	textView = (TextView)findViewById(R.id.textView1);
 	webView = (WebView)findViewById(R.id.webView1);
 	btnShow = (Button)findViewById(R.id.btnShow);
 	btnNext = (Button)findViewById(R.id.btnNext);
 	btnHome = (Button)findViewById(R.id.btnHome);
 	editText1 = (EditText)findViewById(R.id.editText1);
 	btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
 	
 }
 
 
 }
