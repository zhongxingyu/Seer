 package cz.havlena.dictionary.ui;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 
 import cz.havlena.dictionary.DictionaryService;
 import cz.havlena.dictionary.IDictionaryService;
 import cz.havlena.dictionary.ui.R;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnTouchListener;
 import android.view.animation.Animation;
 import android.view.animation.TranslateAnimation;
 import android.view.animation.Animation.AnimationListener;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class DictionaryActivity extends Activity {
 	
 	private static final boolean D = false;
 	private static final String TAG = "DictionaryActivity";
 	private static final String DICTIONARY_FILE = "gcide-entries.xml";
 	private static final int FORMAT = DictionaryService.FORMAT_HTML;
 	private static final int ANIMATION_DURATION = 1000;
 	
 	private static final int SEARCHING_FOUND = 1;
 	private static final int SEARCHING_COMPLETED = 2;
 	private static final int SEARCHING_STARTED = 3;
 	private static final int SEARCHING_ERROR = -1;
 	
 	private static final int ANIMATION_FADEOUT = 4;
 	private static final int ANIMATION_FADEIN = 5;
 	private static final String ANIMATION_STATE = "AnimationState";
 	
 	
 	private static final int TTS_REQUEST = 1;
 	
 	private static final int MENU_TTS = 1;
 	
 	private DictionaryService mService;
 	private TextToSpeech mTts;
 	private TextToSpeechHandler mSpeechHandler;
 	private int mMaxResults = 10;
 	private int mAnimationState = ANIMATION_FADEIN;
 	
 	private Animation mHeaderAnimation;
 	private LinearLayout mHeaderLayout;
 	private LinearLayout mContainerLayout;
 	private Button mStopView;
 	private TextView mCounterView;
 	private WebView mResultView;
 	//private ListView mResultView;
 	private EditText mSearchView;
 	private LayoutInflater mInflater;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(R.layout.dictionary_activity);
         
         if (savedInstanceState != null) {
             mAnimationState = savedInstanceState.getInt(ANIMATION_STATE);
         }
         setupUi(); // setup user interface
         checkTextToSpeech(); // check whether tts is available
     }
     
 
     /**
      * Initialize all UI elements from resources.
      */
     private void initResourceRefs() {
     	mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
         
     	mContainerLayout = (LinearLayout) findViewById(R.id.linearlayout_container);
     	mHeaderLayout = (LinearLayout) findViewById(R.id.linearlayout_header);
     	mSearchView = (EditText) findViewById(R.id.edittext_search);
 		mResultView = (WebView) findViewById(R.id.webview_result);
     	//mResultView = (ListView) findViewById(R.id.listview_results);
 		mStopView = (Button) findViewById(R.id.button_stop);
 		mCounterView = (TextView) findViewById(R.id.textview_counter);
     }
     
     private void setupUi() {
     	initResourceRefs();
     	
     	mSearchView.addTextChangedListener(new SearchHandler());
     	mSearchView.setOnTouchListener(new OnTouchListener() {
 			
 			public boolean onTouch(View v, MotionEvent event) {
 				EditText edt = (EditText) v;
 				edt.selectAll();
 				return false;
 			}
 		});
     	
     	mStopView.setEnabled(false);
     	mStopView.setOnTouchListener(new OnTouchListener() {
 			
 			public boolean onTouch(View v, MotionEvent event) {
 				mService.stopSearching();
 				return true;
 			}
 		});
 		
 		// Since we are caching large views, we want to keep their cache
         // between each animation
         //mHeaderView.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
     }
     
     @Override
     public boolean onKeyUp(int keyCode, KeyEvent event) {
     	if(keyCode == KeyEvent.KEYCODE_MENU) {
     		if(mAnimationState == ANIMATION_FADEOUT) {
     			if(D) Log.w(TAG, "anim state: " + mAnimationState);
     			fadeHeaderIn();
     		}
     	}
     	return true;
     }
     
     private void fadeHeaderIn() {
     	if(D) Log.w(TAG, "fading in");
     	mHeaderLayout.setVisibility(View.VISIBLE); // here because onStart animation isn't called properly
 		mHeaderAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
 												  Animation.RELATIVE_TO_PARENT, 0.0f,
 												  Animation.RELATIVE_TO_PARENT, -0.25f,
 												  Animation.RELATIVE_TO_PARENT, 0.0f);
 		mHeaderAnimation.setAnimationListener(new AnimationHandler());
 		mHeaderAnimation.setDuration(ANIMATION_DURATION);
 		mHeaderLayout.setAnimation(mHeaderAnimation);
     }
     
     private void fadeHeaderOut() {
     	if(D) Log.w(TAG, "fading out");
     	mHeaderAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, 
 				  								  Animation.RELATIVE_TO_PARENT, 0.0f, 
 				  								  Animation.RELATIVE_TO_PARENT, 0.0f, 
 				  								  Animation.RELATIVE_TO_PARENT, -0.25f);
 		mHeaderAnimation.setAnimationListener(new AnimationHandler());
 		mHeaderAnimation.setDuration(ANIMATION_DURATION);
 		mHeaderLayout.setAnimation(mHeaderAnimation);
     }
     
     
     private void checkTextToSpeech() {
     	Intent checkIntent = new Intent();
     	checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
     	startActivityForResult(checkIntent, TTS_REQUEST);
     }
     
     /*
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	menu.add(0, MENU_TTS, 0, "TextToSpeech");
     	return true;
     }
     
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch(item.getItemId()) {
     	case MENU_TTS:
     		if(mTts != null && mTts.isSpeaking()) {
     			mTts.stop();
     		}
     		return true;
     	}
     	return false;
     }
     */
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
     	super.onSaveInstanceState(outState);
     	outState.putInt(ANIMATION_STATE, mAnimationState); // save headerview state
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	if (requestCode == TTS_REQUEST) {
             if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                 // success, create the TTS instance
             	mSpeechHandler = new TextToSpeechHandler();
                 mTts = new TextToSpeech(this, mSpeechHandler);
             } else {
             	Builder dialog = new AlertDialog.Builder(this);
             	dialog.setMessage(this.getString(R.string.tts_not_available));
             	dialog.setPositiveButton("Install", new OnClickListener() {
         			public void onClick(DialogInterface dialog, int which) {
         				Intent installIntent = new Intent();
         		        installIntent.setAction(
         		            TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
         		        startActivity(installIntent);
         			}
         		});
             	dialog.setNegativeButton("Not using", new OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						Log.w(TAG, "User doesn't want to use TTS service");
 					}
 				});
             	dialog.show();
             }
         }
     }
     
     @Override
     protected void onDestroy() {
     	super.onDestroy();
     	if(mTts != null) mTts.shutdown();
     }
     
     private void processError(Exception ex) {
     	Message msg = mHandler.obtainMessage(SEARCHING_ERROR);
 		msg.obj = ex;
 		mHandler.sendMessage(msg);
     }
     
     private Handler mHandler = new Handler() {
     	private StringBuilder result = new StringBuilder();
     	private int mCounter = 0;
     	
     	public String toString() {
     		return result.toString();
     	}
     	
     	@Override
     	public void handleMessage(Message msg) {
     		switch(msg.what) {
     		case SEARCHING_STARTED:
     			Log.w(TAG, "Searching started");
     			mStopView.setEnabled(true);
     			//mResultView.removeAllViews();
     			result.delete(0, result.length());
     			setTitle(getString(R.string.searching));
     			setProgressBarIndeterminateVisibility(true);
     			mCounter = 0;
     			mCounterView.setText(mCounter + "/" + mMaxResults);
     			break;
     			
     		case SEARCHING_FOUND:
     			switch(FORMAT) {
     			case DictionaryService.FORMAT_HTML:
 	    			String element = (String) msg.obj;
 	    			if(element != null) {
 		    			mCounter++;
 		    			result.append(element);
 		    			//ElementListItem item = (ElementListItem) mInflater.inflate(R.layout.element_list_item, mResultView, false);
 		    			//item.setText("<html>" + element + "</html>");
 		        		mResultView.loadData("<html>" + result.toString() + "</html>", "text/html", "utf-8");
 		        		mResultView.invalidate();
 		        		mCounterView.setText(mCounter + "/" + mMaxResults);
 		    		}
 	    			break;
 	    			
     			case DictionaryService.FORMAT_ELEMENT:
     				/*
     				DictionaryElement el = (DictionaryElement) msg.obj;
 	    			if(el != null) {
 	    				txtResult.setText(txtResult.getText() + el.key + "\n"/* + el.definition + "\n" + 
 	    						el.expression + "\n" + el.senses + "\n" +
 	    						"----------------------------------------------" + "\n");
 	        			//txtResult.invalidate();
     				}*/
     				break;
     			}
     			break;
     			
     		case SEARCHING_COMPLETED:
     			Log.w(TAG, "Searching stopped");
     			mStopView.setEnabled(false);
     			setTitle(getString(R.string.app_name));
     			setProgressBarIndeterminateVisibility(false);
     			if(mCounter == 0) {
 	    			//ElementListItem item = (ElementListItem) mInflater.inflate(R.layout.element_list_item, mResultView, false);
     				mResultView.loadData("<html>no word found</html>", "text/html", "utf-8");
     			}
     			else {
     				fadeHeaderOut();
     			}
     			break;
     			
     		case SEARCHING_ERROR:
     			Exception ex = (Exception) msg.obj;
     			//result.append("</br>" + "</br>" + "</br>" + "ERR: " + ex.getMessage());
     			Log.e(TAG, "Searching error: " + ex.getMessage());
     			break;
     		}
     	}
     };
     
     private class AnimationHandler implements AnimationListener {
 
 		public void onAnimationEnd(Animation animation) {			
 			switch(mAnimationState) {
 			case ANIMATION_FADEOUT:
 				mAnimationState = ANIMATION_FADEIN;
 				break;
 			case ANIMATION_FADEIN:
 				mAnimationState = ANIMATION_FADEOUT;
 				break;
 			}
 			
 			if(mAnimationState == ANIMATION_FADEOUT) {
 				if(D) Log.w(TAG, "ANIMATION_FADEOUT");
 				mHeaderLayout.setVisibility(View.GONE);
 			}
 		}
 
 		public void onAnimationRepeat(Animation animation) {}
 
 		public void onAnimationStart(Animation animation) {
 			if(mAnimationState == ANIMATION_FADEOUT) {
 				if(D) Log.w(TAG, "ANIMATION_FADEIN");
 				mHeaderLayout.setVisibility(View.VISIBLE);
 			}
 		}
     	
     }
     
     private class TextToSpeechHandler implements OnInitListener {
     	private boolean ready = false;
     	
     	public boolean isReady() {
     		return ready;
     	}
     	
     	public void onInit(int status) {
         	Log.w(TAG, "TextToSpeech initzialized");
         	
         	/*int result = mTts.isLanguageAvailable(Locale.);
         	if(result != TextToSpeech.LANG_AVAILABLE) {
         		Log.e(TAG, "UK language isn't available [" + result + "]");
         		return;
         	}*/
         	
         	ready = true;
         }
     	
     }
     
     private class DictionaryHandler implements IDictionaryService {
 
 		public void onFoundElement(Object element) {
 			Message msg = mHandler.obtainMessage(SEARCHING_FOUND);
 			msg.obj = element;
 			mHandler.sendMessage(msg);
 		}
 
		public void onSearchStop() {
 			Message msg = mHandler.obtainMessage(SEARCHING_COMPLETED);
 			mHandler.sendMessage(msg);
 		}
 
 		public void onSearchStart() {
 			Message msg = mHandler.obtainMessage(SEARCHING_STARTED);
 			mHandler.sendMessage(msg);
 		}
 
 		public void onError(Exception ex) {
 			processError(ex);
 		}
     	
     }
     
     private class SearchHandler implements TextWatcher {
 
 		public void afterTextChanged(final Editable arg0) {
 			mService.searchAsync(arg0.toString(), mMaxResults, FORMAT);
 		}
 
 		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
 			if(mService == null) {
 				Log.w(TAG, "Initzializing dictionary service");
 				
 				mService = new DictionaryService();
 				mService.setListener(new DictionaryHandler());
 				mCounterView.setVisibility(View.VISIBLE);
 				try {
 					mService.init(new File("/sdcard/" + DICTIONARY_FILE));
 					Log.w(TAG, "Dictionary service initzialized");
 				} catch (FileNotFoundException e) {
 					processError(e);
 				}
 			}
 		}
 
 		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
 				int arg3) {}
     	
     }
 }
