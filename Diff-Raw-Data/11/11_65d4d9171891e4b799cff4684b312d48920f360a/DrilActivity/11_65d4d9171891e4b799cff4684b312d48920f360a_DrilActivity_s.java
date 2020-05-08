 package sk.peterjurkovic.dril.de;
 
 import java.util.List;
 import java.util.Locale;
 import java.util.regex.Pattern;
 
 import sk.peterjurkovic.dril.de.db.StatisticDbAdapter;
 import sk.peterjurkovic.dril.de.db.WordDBAdapter;
 import sk.peterjurkovic.dril.de.model.Word;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class DrilActivity extends MainActivity implements OnInitListener{
 	
 	public static final String TAG = "DRIL";
 	
 	public static final int RATE_1 = 1;
 	public static final int RATE_2 = 2;
 	public static final int RATE_3 = 3;
 	public static final int RATE_4 = 4;
 	public static final int RATE_5 = 5;
 	public static final int DATA_CHECK_CODE = 0;
 	
 	public static final String STATISTIC_ID_KEY = "statisticId";
 	
 	private TextToSpeech textToSpeachService;
 	
 	
 	Button rateButton1;
 	Button rateButton2;
 	Button rateButton3;
 	Button rateButton4;
 	Button rateButton5;
 	Button showAnswerBtn;
 	
 	ImageButton speachQuestionBtn;
 	ImageButton speachAnswerBtn;
 	
 	TextView question;
 	TextView answer;
 	TextView drilheaderInfo;
 	TextView answerLabel;
 	
 	Animation slideLeftIn;
 	View  layout;
 	Word currentWord = null;
 	
 	LinearLayout answerLayout;
 	
 	int position = 0;
 	
 	long statisticId = 0;
 	
 	boolean isAnswerVisible = false;
 	
 	List<Word> activatedWords = null;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main_dril);
         
         ImageButton goHome = (ImageButton) findViewById(R.id.home);
         goHome.setOnClickListener(new View.OnClickListener() {
             @Override
 			public void onClick(View v) {
                 startActivity( new Intent(DrilActivity.this, DashboardActivity.class) );
             }
         });
 
         if(savedInstanceState != null)
         	statisticId = savedInstanceState.getLong(STATISTIC_ID_KEY, 0);
         
         slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.left_ight);
 
         layout = findViewById(R.id.dril);
         
         layout.startAnimation(slideLeftIn);
         
         rateButton1 = (Button) findViewById(R.id.btn_1);
         rateButton2 = (Button) findViewById(R.id.btn_2);
         rateButton3 = (Button) findViewById(R.id.btn_3);
         rateButton4 = (Button) findViewById(R.id.btn_4);
         rateButton5 = (Button) findViewById(R.id.btn_5);
         
         speachQuestionBtn = (ImageButton) findViewById(R.id.speakQuestion);
         speachAnswerBtn = (ImageButton) findViewById(R.id.speakAnswer);
         
         showAnswerBtn = (Button) findViewById(R.id.showAnswer);
         
         question = (TextView) findViewById(R.id.question);
         answer = (TextView) findViewById(R.id.answer);
         answerLabel = (TextView) findViewById(R.id.answerLabel);
         drilheaderInfo = (TextView) findViewById(R.id.drilLabel1);
         
         answerLayout = (LinearLayout)findViewById(R.id.answerLayout);
         
         Intent checkTTSIntent = new Intent();
         checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
         startActivityForResult(checkTTSIntent, DATA_CHECK_CODE);
         
         showAnswerBtn.setOnClickListener(new  OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showAnswer();				
 			}
 		});
         
         
         /* RATE BUTTONs listeners -----------------------------------------------*/
         
         rateButton1.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				currentWord.setRate(RATE_1);
 				currentWord.setActive(false);
 				updateRatedWord();
 				activatedWords.remove(currentWord);
 				position--;
 				nextWord();
 				
 			}
 		});
         
         rateButton2.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				currentWord.setRate(RATE_2);
 				updateRatedWord();
 				nextWord();
 				
 			}
 		});
         
         rateButton3.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				currentWord.setRate(RATE_3);
 				updateRatedWord();
 				nextWord();
 				
 			}
 		});
         
         rateButton4.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				currentWord.setRate(RATE_4);
 				updateRatedWord();
 				nextWord();
 				
 			}
 		});
         
         rateButton5.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				currentWord.setRate(RATE_5);
 				updateRatedWord();
 				nextWord();
 			}
 		});
         
         /* SPEACHs listeners -----------------------------------------------*/
         
         
         speachQuestionBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				speakWords( currentWord.getQuestion() );
 			}
 		});
         speachAnswerBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				speakWords( currentWord.getAnsware() );
 			}
 		});
         
         inicializeDril();
         
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
     	outState.putLong(STATISTIC_ID_KEY, statisticId);
     	super.onSaveInstanceState(outState);
     }
     
     @Override
     protected void onPause() {
     	super.onPause();
     	saveStatisticId();
     }
         
     public void inicializeDril(){
     	activatedWords = inicializeWords();
          
  	   	if(activatedWords == null){
          	throw new Error("Can not recieve activated words.");
          }
          
         if(activatedWords.size() == 0){
         	 drilFinished( R.string.zero_cards_alert );         	
         }else{
         	layout.setVisibility(View.VISIBLE);
         	statisticId = loadStatisticId();
         	if(statisticId == 0 ){
         		initStatistic();
         	}
          	nextWord();
             hideAnswer();
         }
     }
        
     
     
     private void initStatistic() {
     	StatisticDbAdapter statisticDbAdapter = new StatisticDbAdapter(this);
   	    try{
   	    	statisticId = statisticDbAdapter.createNewDrilSession();
   	    } catch (Exception e) {
   			Log.d( TAG , "ERROR: " + e.getMessage());
   		} finally {
   			statisticDbAdapter.close();
   		}	
 	}
 
 
 
 	public List<Word> inicializeWords(){
     	WordDBAdapter wordDbAdapter = new WordDBAdapter(this);
     	List<Word> activatedWords = null;
   	    try{
   	    	activatedWords = wordDbAdapter.getActivatedWords();
   	    } catch (Exception e) {
   			Log.d( TAG , "ERROR: " + e.getMessage());
   		} finally {
   			wordDbAdapter.close();
   		}
   	    return activatedWords;
     }
     
     
     
     
     public void nextWord(){
     	if( goToNextWord() ){ 					
 			hideAnswer();	 									
 	    	question.setText( currentWord.getQuestion() );
 	        answer.setText( currentWord.getAnsware() );
 	        drilheaderInfo.setText( 
 	        		getString(R.string.activated_words, 
 								activatedWords.size(), 
 								position, 
 								(currentWord.getHit()),
 								getLastRate()
 	        				));
 	        layout.startAnimation(slideLeftIn);
     	}
     }
     
     
     public boolean goToNextWord(){
     	if(activatedWords.size() == 0){
     		drilFinished( R.string.dril_finished );
     		return false;
     	}
     	if(position == activatedWords.size()) position = 0;
     	currentWord = activatedWords.get(position);
 		currentWord.increaseHit();
     	position++;
     	return true;
     }
     
     public void drilFinished(int resourceId){
     	layout.setVisibility(View.INVISIBLE);
     	TextView alertBox = (TextView)findViewById(R.id.drilAlertBox);
     	alertBox.setText(resourceId);
     	alertBox.setVisibility(View.VISIBLE);
     	
     }
     
     
     public void updateRatedWord(){
     	WordDBAdapter wordDbAdapter = new WordDBAdapter(this);
   	    try{
   	    	wordDbAdapter.updateReatedWord(currentWord, statisticId);
   	    } catch (Exception e) {
   			Log.d( TAG , "ERROR: " + e.getMessage());
   		} finally {
   			wordDbAdapter.close();
   		}
     }
     
     
     public void showAnswer(){
     	answerLayout.setVisibility(View.VISIBLE);
     	answerLabel.setVisibility(View.VISIBLE);
     	answer.setVisibility(View.VISIBLE);
     	speachAnswerBtn.setVisibility(View.VISIBLE);
     	showAnswerBtn.setVisibility(View.GONE);
     	isAnswerVisible = true;
     }
     
     public void hideAnswer(){
     	answerLayout.setVisibility(View.GONE);
     	answerLabel.setVisibility(View.GONE);
     	answer.setVisibility(View.GONE);
     	speachAnswerBtn.setVisibility(View.GONE);
     	showAnswerBtn.setVisibility(View.VISIBLE);
     	isAnswerVisible = false;
     }
     
     
     public String getLastRate(){
     	return (currentWord.getRate() == 0 ? " -" : currentWord.getRate()+"");
     }
     
     
     private void speakWords(String speech) {
     	 textToSpeachService.speak(clearWord( speech ), TextToSpeech.QUEUE_FLUSH, null);
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	if (requestCode == DATA_CHECK_CODE) {
             if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {      
                textToSpeachService = new TextToSpeech(this, this);
             }
             else {
                 Intent installTTSIntent = new Intent();
                 installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                 startActivity(installTTSIntent);
             }
             }
     	super.onActivityResult(requestCode, resultCode, data);
     }
     
     
     
     @Override
     public void onInit(int initStatus) {
     	if (initStatus == TextToSpeech.SUCCESS) {
             textToSpeachService.setLanguage(Locale.GERMAN);
         }else if (initStatus == TextToSpeech.ERROR) {
             Toast.makeText(this, R.string.speach_failed, Toast.LENGTH_LONG).show();
         }
     }
     
     
     @Override
     protected void onDestroy() {
     	if (textToSpeachService != null) {
         	textToSpeachService.stop();
         	textToSpeachService.shutdown();
 	    }
     	super.onDestroy();
     }
     
     
     /**
      * Clean word witch will by pronaucmend
      * 
      * escapten caractes:
      * s n v abj adv conj (s) (n) (v) (abj) (adv) (conj)
      * and [.*]
      * 
      * @param String word to escape
      * @return String escaped string
      */
     public String clearWord(String word){
     	Pattern pat = Pattern.compile("((\\(.*)\\)|(\\/(.*)\\/))");   
     	String w = pat.matcher(word).replaceAll(" ");
     	//Log.d("DRIL", "Original: "+word + " Replaced: " +w);
     	return w;  
     }
     
     
     private void saveStatisticId() {
         SharedPreferences sharedPreferences = getSharedPreferences(STATISTIC_ID_KEY, MODE_PRIVATE);
         SharedPreferences.Editor editor = sharedPreferences.edit();
         editor.putLong(STATISTIC_ID_KEY, statisticId);
         editor.commit();
     }
 
     private long loadStatisticId() { 
         SharedPreferences sharedPreferences = getSharedPreferences(STATISTIC_ID_KEY, MODE_PRIVATE);
         return sharedPreferences.getLong(STATISTIC_ID_KEY, 0);
     }
 }
