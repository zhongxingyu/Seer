 package com.mobidroid.labtts.activity;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.drawable.AnimationDrawable;
 import android.os.Bundle;
 import android.speech.tts.TextToSpeech;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Spinner;
 
 public class LabTTSActivity extends Activity {
 
 	private static final String TAG = LabTTSActivity.class.getName();
 		
 	private static final int TTS_CHECK_RESULT = 666;
 
 	private TextToSpeech mTts;
 
 	private ImageView imgBubble;
 	private ImageView imgAndy;
 	
 	private EditText txtInput;
 	private Spinner spinLoc;
 	
 	private Button btSpeak;
 	private Button btSpeakNow;
 	
 
 
 	private AnimationDrawable andySpeakAnim;
 
 	private AnimationCoordinator bc;
 
 	private ArrayAdapter<LocaleWrapper> adapterLocale;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		// On map les differentes composante du layout
 		mapLayoutCompoments();
 
 		// On desactive les bouttons
 		enableControl(false);
 
 		// On va verifier si le TTS est dispo sur ce telephone
 		checkTTS();
 		
 	}
 
 	/**
 	 * Active ou desactive les bouttons utilisable par l'usager
 	 * 
 	 * @param shouldDisable
 	 */
 	private void enableControl(boolean enable) {
 		btSpeak.setEnabled(enable);
 		btSpeakNow.setEnabled(enable);		
 		txtInput.setEnabled(enable);
 		spinLoc.setEnabled(enable);
 	}
 
 	/**
 	 * Initialise les composantes graphiques
 	 */
 	private void mapLayoutCompoments() {
 
 		txtInput = (EditText) findViewById(R.id.txtInput);
 		btSpeak = (Button) findViewById(R.id.btSpeakMachine);
 		btSpeakNow = (Button) findViewById(R.id.btSpeakMachineNow);
 				
 		imgAndy = (ImageView) findViewById(R.id.imgAndy);		
 		imgBubble = (ImageView) findViewById(R.id.imgBubble);		
 			
 		spinLoc = (Spinner) findViewById(R.id.spinLoc);
 		
 		
 		//le controlleur d'animation s'occupera de coordoner les animations
 	bc = new AnimationCoordinator(this, imgBubble, imgAndy);
 						
 	}
 
 	/**
 	 * Arrete ce que tu disais et dit ceci
 	 * 
 	 * @param text
 	 */
 	private void speakNow(String text) {
 		HashMap<String, String> umap = new HashMap<String, String>();
 		String utId = "UTID_F_"+System.currentTimeMillis();
 		umap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utId);
 		mTts.speak(text, TextToSpeech.QUEUE_FLUSH, umap);
 		//FIXME: Animation
 		bc.onUtteranceBegin(utId, text, TextToSpeech.QUEUE_FLUSH);
 	}
 
 	/**
 	 * Fini ce que tu voulais dire et dit ceci
 	 * 
 	 * @param text
 	 */
 	private void speak(String text) {
 		
 		HashMap<String, String> umap = new HashMap<String, String>();
 		String utId = "UTID_Q_"+System.currentTimeMillis();
 		umap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utId);
 		mTts.speak(text, TextToSpeech.QUEUE_ADD, umap);		
 		//FIXME: Animation
 		bc.onUtteranceBegin(utId, text, TextToSpeech.QUEUE_ADD);				
 	}
 
 	/**
 	 * Lance un appel  l'activit TTS pour vrifier sa disponibilite
 	 */
 	private void checkTTS() {
 		Intent checkIntent = new Intent();
 		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
 		startActivityForResult(checkIntent, TTS_CHECK_RESULT);
 		// On recoit le retour dans onActivityResult
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (requestCode == TTS_CHECK_RESULT) {
 
 			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
 				Log.d(TAG, "TTS supported, let's chat!");
 
 				// Active une instance TTS
 				mTts = new TextToSpeech(this, new EngineInitListener());
 				// Un call au listener sur la methode onInit sera fait
 
 			} else {
 				// missing data, install it
 				Intent installIntent = new Intent();
 				installIntent
 						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
 				startActivity(installIntent);
 			}
 		}
 	}
 	
 	/**
 	 * L'engine est pret a etre utilise, on prepare la vue
 	 */
 	private void postTtsInit() {
 		
 		//FIXME: ANIM PIECE
 		//Notre coordonateur d'animation va s'occuper de synchroniser les animations
 		//et la voix
 		mTts.setOnUtteranceCompletedListener(bc);
 				
 		//On popule le spinner avec les locales dispo
 		initLocaleSpinner();
 		
 		//Ajout des listener des bouttons
 		btSpeak.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				speak(txtInput.getText().toString());
 			}
 		});
 
 		btSpeakNow.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				speakNow(txtInput.getText().toString());
 			}
 		});
 				
 		//Tout est prt pour un monologue on active les bouttons
 		enableControl(true);
 	}
 	
 	
 	/**
 	 * Le spinner contient toutes les locales dispo
 	 */
 	private void initLocaleSpinner() {
 		
 		
 		LocaleWrapper[] availableLocale = getAvailableTTSLocale();
 		
 		// Ajoute les locales dans un beau spinner
 		adapterLocale = new ArrayAdapter<LocaleWrapper>(this,
 														android.R.layout.simple_spinner_item,
 														availableLocale);
 		
 		adapterLocale.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
 		
 		spinLoc.setAdapter(adapterLocale);				
 		spinLoc.setOnItemSelectedListener(new OnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> locales, View view,
 					int position, long id) {				
 				// On change la langue de notre verbeux moteur
 				mTts.setLanguage(adapterLocale.getItem(position).getLocale());
 			}
 			
 			public void onNothingSelected(AdapterView<?> arg0) {}
 		});
 		
 	}
 
 
 	/**
 	 * Simple tts listener
 	 *
 	 */
 	private class EngineInitListener implements TextToSpeech.OnInitListener {
 		@Override
 		public void onInit(int initStatus) {
 			if (initStatus == TextToSpeech.SUCCESS) {
 				postTtsInit();												
 			} else {
 				Log.e(TAG, "Unable to initialize TTS engine");
 			}			
 		}		
 	}
 
 	/**
 	 * Return all country / language available
 	 * 
 	 * @return
 	 */
 	private LocaleWrapper[] getAvailableTTSLocale() {
 
 		ArrayList<LocaleWrapper> supportedLocTTS = new ArrayList<LocaleWrapper>();
 		ArrayList<String> supportedLangTTS = new ArrayList<String>();
 
 		Locale[] locales = Locale.getAvailableLocales();
 		for (Locale locale : locales) {
 			int supportedCode = mTts.isLanguageAvailable(locale);
 			if (supportedCode == TextToSpeech.LANG_AVAILABLE) {
 				if (!supportedLangTTS.contains(locale.getLanguage())) {
 					supportedLocTTS.add(new LocaleWrapper(locale));
 					supportedLangTTS.add(locale.getLanguage());
 				}
 
 			}
 
 		}
 		return supportedLocTTS
 				.toArray(new LocaleWrapper[supportedLocTTS.size()]);
 	}
 
 	@Override
 	protected void onDestroy() {
		// On libere notre ami le TTS
		mTts.shutdown();
 		super.onDestroy();
 	}
 	
 		
 	public void animateBubble(final Animation anim) {		
 		runOnUiThread(new Runnable() {			
 			@Override
 			public void run() {
 				imgBubble.startAnimation(anim);		
 			}
 		});			
 	}
 	
 	public void startAndroidMouthAnimation(){	
 		runOnUiThread(new Runnable() {			
 			@Override
 			public void run() {			
 				imgAndy.setImageResource(R.drawable.andy_speak);
 				andySpeakAnim = (AnimationDrawable) imgAndy.getDrawable();
 				andySpeakAnim.start();					
 			}
 		});
 	}
 	
 	public void stopAndroidMouthAnimation(){		
 		runOnUiThread(new Runnable() {	
 			@Override
 			public void run() {				
 				andySpeakAnim.stop();
 				imgAndy.setImageResource(R.drawable.andy1);		
 			}
 		});			
 	}
 				
 	/**
 	 * Simple wrapper to display language name in the spinner adapter
 	 */
 	private class LocaleWrapper {
 
 		private Locale locale;
 
 		public LocaleWrapper(Locale l) {
 			this.locale = l;
 		}
 
 		public Locale getLocale() {
 			return locale;
 		}
 
 		@Override
 		public String toString() {
 			return locale.getDisplayLanguage();
 		}
 
 	}
 }
