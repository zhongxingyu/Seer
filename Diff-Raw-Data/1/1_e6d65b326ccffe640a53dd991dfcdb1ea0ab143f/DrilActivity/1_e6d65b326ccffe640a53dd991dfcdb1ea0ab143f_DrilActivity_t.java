 
 package sk.peterjurkovic.dril.v2.activities;
 
 
 import java.util.Locale;
 
 import sk.peterjurkovic.dril.DrilService;
 import sk.peterjurkovic.dril.R;
 import sk.peterjurkovic.dril.dao.StatisticsDao;
 import sk.peterjurkovic.dril.dao.StatisticsDaoImpl;
 import sk.peterjurkovic.dril.db.WordDBAdapter;
 import sk.peterjurkovic.dril.dto.WordToPronauceDto;
 import sk.peterjurkovic.dril.exceptions.DrilUnexpectedFinishedException;
 import sk.peterjurkovic.dril.model.Statistics;
 import sk.peterjurkovic.dril.model.Word;
 import sk.peterjurkovic.dril.utils.GoogleAnalyticsUtils;
 import sk.peterjurkovic.dril.utils.StringUtils;
 import sk.peterjurkovic.dril.v2.constants.Constants;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.TranslateAnimation;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.analytics.tracking.android.Fields;
 import com.google.analytics.tracking.android.Log;
 import com.google.analytics.tracking.android.MapBuilder;
 
 
 public class DrilActivity extends BaseActivity implements OnInitListener {
 	
 	public static final String DRIL_ID = "drilActivity";
 	private static final String QUESTION_TAG = "question";
 	private static final String ANSWER_TAG = "answer";
 	
 	public static final String TAG = "DRIL";
 	public static final int DATA_CHECK_CODE = 0;
 	public static final String STATISTIC_ID_KEY = "statisticId";
 	
 	private int helpClickedCounter = 0;
 	
 	private TextToSpeech tts = null;
 	private DrilService drilService;
 
 	private Button showAnswerBtn;
 	private ImageButton speachQuestionBtn;
 	private ImageButton speachAnswerBtn;
 	private TextView question;
 	private TextView answer;
 	private TextView drilheaderInfo;
 	private TextView answerLabel;
 	private Animation slideLeftIn;
 	private View  layout;
 	private LinearLayout answerLayout;
 	private EditText input;
 	private TextView userAnserBox;
 	private TextView userAnserBoxResult;
 	private TextView helpMe;
 	private boolean writeAnswer = false;
 	SharedPreferences preferences;
 	
 
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.v2_dril_layout);
 	    
        
         //slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.left_ight);
         slideLeftIn = new TranslateAnimation(1000, 0, 0, 0);
         slideLeftIn.setDuration(500);
         slideLeftIn.setFillAfter(true);
         
         helpMe = (TextView) findViewById(R.id.helpMe);    	
         layout = findViewById(R.id.dril);
     	layout.startAnimation(slideLeftIn);
     	
 	    speachQuestionBtn = (ImageButton) findViewById(R.id.speakQuestion);
 	    speachAnswerBtn = (ImageButton) findViewById(R.id.speakAnswer);
 	    showAnswerBtn = (Button) findViewById(R.id.showAnswer);
         question = (TextView) findViewById(R.id.question);
         answer = (TextView) findViewById(R.id.answer);
         answerLabel = (TextView) findViewById(R.id.answerLabel);
         drilheaderInfo = (TextView) findViewById(R.id.drilHeaderInfo);
         answerLayout = (LinearLayout)findViewById(R.id.answerLayout);
         input = (EditText) findViewById(R.id.inputAnswer);
         
         userAnserBox = (TextView) findViewById(R.id.userAnserBox);
         userAnserBoxResult = (TextView) findViewById(R.id.userAnserBoxResult);
         
         checkTTSDataForLocale();
         init();
     }
     
     private void setListeners(){
     	 showAnswerBtn.setOnClickListener(new  OnClickListener() {
  			@Override
  			public void onClick(View v) {
  				showAnswer();				
  			}
  		});
 
          OnClickListener onQuestionClick = new OnClickListener() {
  			@Override
  			public void onClick(View v) {
  				try{
  					speakWords( getQuestionToPronauce() );
  				}catch(Exception e){
  					GoogleAnalyticsUtils.logException(e, getApplicationContext());
  				}
  			}
  		};
  		
  		OnClickListener onAnswerClick = new OnClickListener() {
  			@Override
  			public void onClick(View v) {
  				try{
  					speakWords( getAnserToPronauce() );
  				}catch(Exception e){
  					GoogleAnalyticsUtils.logException(e, getApplicationContext());
  				}	
  			}
  		};
          speachQuestionBtn.setOnClickListener(onQuestionClick);
          speachAnswerBtn.setOnClickListener(onAnswerClick);
          question.setOnClickListener(onQuestionClick);
          answer.setOnClickListener(onAnswerClick);
          helpMe.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				onHelpMeClicked();
 			}
 		});
     }
     
     private void init(){
     	setListeners();
     	preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
     	drilService = new DrilService(new WordDBAdapter(this));
     	writeAnswer = preferences.getBoolean(Constants.PREF_WRITE_ANSWER_KEY, false);
         if(!drilService.hasNext()){
         	showNoCardsAlert();
         }else{
         	layout.setVisibility(View.VISIBLE);
         	tryNextWord();
         	initStatistics();
         }
         
         logInit();
     }
     
     private void tryNextWord(){
     	helpClickedCounter = 0;
     	hideAnswer();
     	try{
 	    	Word currentWord = drilService.getNext();
 	    	setWordIntoViews(currentWord);
 	    	boolean autoplayPronunciation =  preferences.getBoolean(Constants.PREF_AUTOPLAY_PRONAUCE_KEY, false);
 	    	if(autoplayPronunciation){
 	    		playPronunciationInNewThread(Constants.DELAY_BEFORE_PRONUNCIATION);
 	    	}
     	}catch(DrilUnexpectedFinishedException e){
     		Log.e(e);
     	}
     }
     
     private void setWordIntoViews(Word word){
     	if(word == null){
     		return;
     	}
     	String shouldBeShown = preferences.getString(Constants.PREF_TEST_VALUE_KEY, "question");
     	if(shouldBeShown.equals("question")){
     		setVisibleQuestion(word);
     	}else if(shouldBeShown.equals("answer")){
     		setVisibleAnswer(word);
     	}else{
     		long timestamp = System.currentTimeMillis();
     		if(timestamp % 2 == 0){
     			setVisibleQuestion(word);
     		}else{
     			setVisibleAnswer(word);
     		}
     	}
     	
     	 drilheaderInfo.setText( 
 	        		getString(R.string.activated_words, 
 						drilService.getCountOfWords(), 
 						word.getHit(),
 						word.getLastRate()
     				));
     	 layout.startAnimation(slideLeftIn);
     }
     
     private void setVisibleQuestion(Word word){
     	question.setText( word.getQuestion() );
     	question.setTag(QUESTION_TAG);
         answer.setText( word.getAnsware() );
         answer.setTag(ANSWER_TAG);
     }
     
     private void setVisibleAnswer(Word word){
     	question.setText( word.getAnsware()  );
     	question.setTag(ANSWER_TAG);
         answer.setText(  word.getQuestion());
         answer.setTag(QUESTION_TAG);
     }
     
     public void showNoCardsAlert(){
     	 layout.setVisibility(View.INVISIBLE);
          TextView alertBox = ((TextView)findViewById(R.id.noCardActivatedAlert));
          alertBox.setOnClickListener(new OnClickListener() {
                      @Override
                      public void onClick(View v) {
                              Intent i = new Intent( getApplicationContext() , BookListActivity.class);
                      startActivity(i);
                      }
              });
          alertBox.setVisibility(View.VISIBLE);
     }
     
     public void processRate(View view){
     	final int rate = Integer.valueOf((String)view.getTag());
     	drilService.precessRating(rate);
     	if(drilService.hasNext()){
     		tryNextWord();
     	}else{
     		drilFinished();
     	}
     	
     }
     
     
     public void drilFinished(){
     	Intent shareIntent = new Intent(this, FacebookShare.class);
     	Statistics statistics = drilService.getStatistics();
     	int learnedCards = 1;
     	if(statistics != null){
     		statistics.setFinished(Boolean.TRUE);
     		StatisticsDao statisDao = new StatisticsDaoImpl(this);
     		statisDao.updateStatistics(statistics);
     		learnedCards = statistics.getLearnedCards();
     	}
     	shareIntent.putExtra(FacebookShare.EXTRA_LEARNED_DARDS, learnedCards);
     	startActivity(shareIntent);
     }
     
     
    
     
     public void showAnswer(){
     	answerLayout.setVisibility(View.VISIBLE);
     	answerLabel.setVisibility(View.VISIBLE);
     	answer.setVisibility(View.VISIBLE);
     	speachAnswerBtn.setVisibility(View.VISIBLE);
     	showAnswerBtn.setVisibility(View.GONE);
     	hideHelper();
     	if(writeAnswer){
     		hideInputField();
     		String text = input.getText().toString();
     		int rating = StringUtils.determineSimularity(getAnserToPronauce().getValue(), text);
     		userAnserBoxResult.setText( "" + rating);
     		userAnserBoxResult.setVisibility(View.VISIBLE);
     		userAnserBox.setVisibility(View.VISIBLE);
     		userAnserBox.setText(text);   		
     	}
 		
     }
     
     public void showInputField(){
     	userAnserBoxResult.setVisibility(View.GONE);
 		userAnserBox.setVisibility(View.GONE);
 		input.setVisibility(View.VISIBLE);
 		input.setText("");
     }
     
     public void hideInputField(){
     	input.setVisibility(View.GONE);
     }
     
     public void hideAnswer(){
     	answerLayout.setVisibility(View.GONE);
     	answerLabel.setVisibility(View.GONE);
     	answer.setVisibility(View.GONE);
     	speachAnswerBtn.setVisibility(View.GONE);
     	showAnswerBtn.setVisibility(View.VISIBLE);
     	if(writeAnswer){
     		 showInputField();
     	}
     	
     	if(shouldShowHelper()){
     		showHelper();
     	}
     }
     
     private void showHelper(){
     	helpMe.setVisibility(View.VISIBLE);
     }
     
     private void hideHelper(){
     	helpMe.setVisibility(View.GONE);
     }
 
     private boolean shouldShowHelper(){
     	return preferences.getBoolean(Constants.PREF_SHOW_HELPER, true);
     }
    
     
     
     private void speakWords(final WordToPronauceDto wordDto) {
     	if(wordDto == null || StringUtils.isBlank(wordDto.getValue())){
     		Toast.makeText(this, R.string.nothing_to_speeach, Toast.LENGTH_LONG).show();
    		return;
     	}
     	if(wordDto.getLanguage() == null){
     		setEnglishTTSLocale();
     		speek(wordDto.getValue());
     	}else{
     		Locale locale = wordDto.getLanguage().getLocale();
     		if(isLanguageAvailable(locale) && tts != null){
     			tts.setLanguage(locale);
     			speek(wordDto.getValue());
     		}else{
     			if(wordDto.isShowFailureToast()){
     			Toast.makeText(this, 
     					getString(R.string.error_tts_locale, getString( wordDto.getLanguage().getResource())) , 
     					Toast.LENGTH_LONG).show();
     			}
     		}
     	}
     	
     	
     }
     
     private void speek(final String word){
     	if(tts != null ){
     		tts.speak(StringUtils.removeSpecialCharacters(word), TextToSpeech.QUEUE_FLUSH, null);
     	}
     }
     
     @Override
     protected void onActivityResult(final int requestCode,final int resultCode, Intent data) {
     	if (requestCode == DATA_CHECK_CODE) {
             if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {      
                tts = new TextToSpeech(this, this);
             }
             else {
             	if(!preferences.getBoolean(Constants.PREF_DISABLE_TTS_NOTIFi, false)){
 	                Intent installIntent = new Intent();
 	                installIntent.setAction( TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
 	                startActivity(installIntent);
             	}
             }
         }
     	super.onActivityResult(requestCode, resultCode, data);
     }
     
     
     
     @Override
     public void onInit(int initStatus) {
     	if (initStatus == TextToSpeech.SUCCESS) {
     		setEnglishTTSLocale();
         }else if (initStatus == TextToSpeech.ERROR) {
             Toast.makeText(this, R.string.speach_failed, Toast.LENGTH_LONG).show();
         }
     }
     
     
     @Override
     protected void onDestroy() {
     	if (tts != null) {
         	tts.stop();
         	tts.shutdown();
 	    }
     	super.onDestroy();
     }
     
     
     
     
     public WordToPronauceDto getQuestionToPronauce(){
     	String tagValue = (String) question.getTag();
     	return determineWord(tagValue);
     }
     
     
     public WordToPronauceDto getAnserToPronauce(){
     	String tagValue = (String) answer.getTag();
     	return determineWord(tagValue);
     }
     
     
     public WordToPronauceDto determineWord(final String tagValue){
     	if(StringUtils.isBlank(tagValue)){
     		return null;
     	}
     	Word currentWord = drilService.getCurrentWord();
     	if(currentWord == null){
     		return null;
     	}
     	
     	WordToPronauceDto wordDto = new WordToPronauceDto();
     	if(tagValue.equals(QUESTION_TAG)){
     			wordDto.setValue(currentWord.getQuestion());
     			wordDto.setLanguage( currentWord.getQuestionLanguage() );
     	}else{
     		wordDto.setValue(currentWord.getAnsware());
     		wordDto.setLanguage(currentWord.getAnserLanguage() );
     	}
     	return wordDto;
     }
     
     public boolean isLanguageAvailable(Locale speechLocale) {
         if(tts == null){
         	return false;
         }
     	return tts.isLanguageAvailable(speechLocale) != TextToSpeech.LANG_MISSING_DATA
                 && tts.isLanguageAvailable(speechLocale) != TextToSpeech.LANG_NOT_SUPPORTED;
     }
 
     
     private void setEnglishTTSLocale(){
 		if(tts != null){
 			tts.setLanguage(Locale.ENGLISH);
 		}
     }
     
     private void checkTTSDataForLocale(){
     	 Intent checkTTSIntent = new Intent();
          checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
          PackageManager pm = getPackageManager();
          ResolveInfo resolveInfo = pm.resolveActivity( checkTTSIntent, PackageManager.MATCH_DEFAULT_ONLY );
          if( resolveInfo != null ) {
         	 startActivityForResult(checkTTSIntent, DATA_CHECK_CODE);
          }
          
     }
     
     private void playPronunciationInNewThread(final int delay){
     	Thread thread = new Thread()
     	{
     	    @Override
     	    public void run() {
     	        try {
 	                WordToPronauceDto wordDto = getQuestionToPronauce();
 	            	if(wordDto != null && wordDto.getLanguage() != null){
 	            		 sleep(delay);
 	            		int targetLang = Integer.valueOf(preferences.getString(Constants.PREF_TARGET_LANG_KEY, "1"));
 	            		if(targetLang == wordDto.getLanguage().getId()){
 	            			wordDto.setShowFailureToast(false);
 	            			speakWords(wordDto);
 	            		}
 	            	}
     	            
     	        } catch (InterruptedException e) {
     	            e.printStackTrace();
     	        }
     	    }
     	};
 
     	thread.start();    	
     }
     
     
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
     	if(KeyEvent.KEYCODE_VOLUME_UP == keyCode || KeyEvent.KEYCODE_VOLUME_DOWN == keyCode){
     		updateVolume(keyCode);
     		return true;
     	}
     	return super.onKeyDown(keyCode, event); 
     }
     
     private void updateVolume(final int keyCode){
     	AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
 		final int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 		final int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
 		if(KeyEvent.KEYCODE_VOLUME_DOWN == keyCode && currentVolume > 0){
 			am.setStreamVolume(AudioManager.STREAM_MUSIC, (currentVolume - 1),  AudioManager.FLAG_SHOW_UI);
 		}else if(KeyEvent.KEYCODE_VOLUME_UP == keyCode && currentVolume < maxVolume){
 			am.setStreamVolume(AudioManager.STREAM_MUSIC, (currentVolume + 1), AudioManager.FLAG_SHOW_UI);
 		}
     }
     
     
    
     
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
     	MenuItem menuItem = menu.findItem(R.id.settings);
     	menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
     	menuItem = menu.findItem(R.id.startDril);
     	menuItem.setVisible(Boolean.FALSE);
     	return super.onPrepareOptionsMenu(menu);
     }
     
     
     @Override
     protected void onResume() {
     	writeAnswer = preferences.getBoolean(Constants.PREF_WRITE_ANSWER_KEY, false);
     	if(answer.getVisibility() != View.VISIBLE){
     		if(writeAnswer){
     			showInputField();
     		}
     		if(shouldShowHelper()){
     			showHelper();
     		}else{
     			hideHelper();
     		}
     	}else{
     		hideInputField();
     		
     	}
     	super.onResume();
     }
     
     
     private void logInit(){
     	 EasyTracker.getInstance(this).send(MapBuilder
          		.createAppView()
          		.set(Fields.SCREEN_NAME, "Dril Screen")
          		.set(Constants.PREF_WRITE_ANSWER_KEY, writeAnswer + "")
          		.set("wordCount", drilService.getCountOfWords() + "")
          		.build()
          		);
     }
     
     
     private void onHelpMeClicked(){
     	String text =  StringUtils.getDrilHelpMessage(getAnserToPronauce().getValue(), helpClickedCounter);
     	if(!StringUtils.isBlank(text)){
     		Toast.makeText(this, text , Toast.LENGTH_SHORT).show();
     		helpClickedCounter++;
     	}
     }
     
     private void initStatistics(){
     	StatisticsDao statisticsDao = new StatisticsDaoImpl(this);
     	drilService.setStatistics(statisticsDao.getSessionStatisticsOrCreateNew());
     }
 }
