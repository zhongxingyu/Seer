 /* 
  * This file is part of OppiaMobile - http://oppia-mobile.org/
  * 
  * OppiaMobile is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * OppiaMobile is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.digitalcampus.oppia.activity;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.concurrent.Callable;
 
 import org.digitalcampus.mobile.learning.R;
 import org.digitalcampus.oppia.adapter.SectionListAdapter;
 import org.digitalcampus.oppia.application.Tracker;
 import org.digitalcampus.oppia.gesture.PageGestureDetector;
 import org.digitalcampus.oppia.model.Module;
 import org.digitalcampus.oppia.model.Section;
 import org.digitalcampus.oppia.service.TrackerService;
 import org.digitalcampus.oppia.utils.MetaDataUtils;
 import org.digitalcampus.oppia.utils.UIUtils;
 import org.digitalcampus.oppia.widgets.PageWidget;
 import org.digitalcampus.oppia.widgets.QuizWidget;
 import org.digitalcampus.oppia.widgets.ResourceWidget;
 import org.digitalcampus.oppia.widgets.WidgetFactory;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ModuleActivity extends AppActivity implements OnUtteranceCompletedListener, OnInitListener {
 
 	public static final String TAG = ModuleActivity.class.getSimpleName();
 	public static final String BASELINE_TAG = "BASELINE";
 	private Section section;
 	private Module module;
 	private int currentActivityNo = 0;
 	private WidgetFactory currentActivity;
 	private SharedPreferences prefs;
 	private boolean isBaselineActivity = false;
 	
 	private GestureDetector pageGestureDetector;
 	View.OnTouchListener pageGestureListener;
 	
 	private static int TTS_CHECK = 0;
 	static TextToSpeech myTTS;
 	private boolean ttsRunning = false;
 
 	private HashMap<String, Object> widgetState = new HashMap<String, Object>();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_module);
 		this.drawHeader();
 
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 
 		Bundle bundle = this.getIntent().getExtras();
 		if (bundle != null) {
 			section = (Section) bundle.getSerializable(Section.TAG);
 			module = (Module) bundle.getSerializable(Module.TAG);
 			currentActivityNo = (Integer) bundle.getSerializable(SectionListAdapter.TAG_PLACEHOLDER);
 			if(bundle.getSerializable(ModuleActivity.BASELINE_TAG) != null){
 				this.isBaselineActivity = (Boolean) bundle.getSerializable(ModuleActivity.BASELINE_TAG);
 			}
 		}
 		
 		// OppiaMobileGesture detection for pages
 		pageGestureDetector = new GestureDetector(this, new PageGestureDetector(this));
 		pageGestureListener = new View.OnTouchListener() {
 			
 			public boolean onTouch(View v, MotionEvent event) {
 				//return pageGestureDetector.onTouchEvent(event);
 				if(pageGestureDetector.onTouchEvent(event)){
 			         return true;
 			    }
 			    return false;
 			}
 		};
 		
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
		//savedInstanceState.putLong("activityStartTimeStamp", currentActivity.getStartTime());
 		savedInstanceState.putInt("currentActivityNo", this.currentActivityNo);
		//savedInstanceState.putSerializable("currentActivity", this.currentActivity);
 		savedInstanceState.putSerializable("widget_config", currentActivity.getWidgetConfig());
 		super.onSaveInstanceState(savedInstanceState);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		this.currentActivityNo = savedInstanceState.getInt("currentActivityNo");
		//currentActivity = section.getActivities().get(this.currentActivityNo);
		//currentActivity.setWidgetConfig((HashMap<String, Object>) savedInstanceState.getSerializable("widget_config"));
		widgetState = (HashMap<String, Object>) savedInstanceState.getSerializable("widget_config");
		//currentActivity.setStartTime(savedInstanceState.getLong("activityStartTimeStamp"));
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		setTitle(section.getTitle(prefs
 				.getString(getString(R.string.prefs_language), Locale.getDefault().getLanguage())));
 		//loadActivity();
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		if (myTTS != null) {
 			myTTS.shutdown();
 		}
 
 		ArrayList<org.digitalcampus.oppia.model.Activity> acts = section.getActivities();
 		this.saveTracker(acts.get(this.currentActivityNo).getDigest());
 
 		// start a new tracker service
 		Log.d(TAG, "Starting tracker service");
 		Intent service = new Intent(this, TrackerService.class);
 
 		Bundle tb = new Bundle();
 		tb.putBoolean("backgroundData", true);
 		service.putExtras(tb);
 		this.startService(service);
 		
 		if (currentActivity != null) {
 			widgetState = currentActivity.getWidgetConfig();
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		loadActivity();
 		if (currentActivity != null) {
 			currentActivity.setWidgetConfig(widgetState);
 			
 		}
 		Log.d(TAG,"onresume called");
 	}
 
 	@Override
 	protected void onDestroy() {
 		if (myTTS != null) {
 			myTTS.shutdown();
 			myTTS = null;
 		}
 		super.onDestroy();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_module, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		MenuItem item = (MenuItem) menu.findItem(R.id.menu_tts);
 		if (ttsRunning) {
 			item.setTitle(R.string.menu_stop_read_aloud);
 		} else {
 			item.setTitle(R.string.menu_read_aloud);
 		}
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.menu_language:
 			createLanguageDialog();
 			return true;
 		case R.id.menu_help:
 			startActivity(new Intent(this, HelpActivity.class));
 			return true;
 		case R.id.menu_tts:
 			if (myTTS == null && !ttsRunning) {
 				// check for TTS data
 				Intent checkTTSIntent = new Intent();
 				checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
 				startActivityForResult(checkTTSIntent, TTS_CHECK);
 			} else if (myTTS != null && ttsRunning){
 				this.stopReading();
 			} else {
 				// TTS not installed so show message
 				Toast.makeText(this, this.getString(R.string.error_tts_start), Toast.LENGTH_LONG).show();
 			}
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void loadActivity() {
 		ArrayList<org.digitalcampus.oppia.model.Activity> acts = section.getActivities();
 		TextView tb = (TextView) this.findViewById(R.id.module_activity_title);
 
 		tb.setText(acts.get(this.currentActivityNo).getTitle(
 				prefs.getString(getString(R.string.prefs_language), Locale.getDefault().getLanguage())));
 
 		if (acts.get(this.currentActivityNo).getActType().equals("page")) {
 			currentActivity = new PageWidget(ModuleActivity.this, module, acts.get(this.currentActivityNo));
 			WebView wv = (WebView) this.findViewById(R.id.page_webview);
 			wv.setOnTouchListener(pageGestureListener);
 		} else if (acts.get(this.currentActivityNo).getActType().equals("quiz")) {
 			if(widgetState.isEmpty()){
 				currentActivity = new QuizWidget(ModuleActivity.this, module, acts.get(this.currentActivityNo));
 			} else {
 				currentActivity = new QuizWidget(ModuleActivity.this, module, acts.get(this.currentActivityNo), widgetState);
 			}
 			currentActivity.setBaselineActivity(this.isBaselineActivity);
 		} else if (acts.get(this.currentActivityNo).getActType().equals("resource")) {
 			currentActivity = new ResourceWidget(this, module, acts.get(this.currentActivityNo));
 		}
 		currentActivity.setWidgetConfig(widgetState);
 		this.setUpNav();
 	}
 
 	private void setUpNav() {
 		Button prevB = (Button) ModuleActivity.this.findViewById(R.id.prev_btn);
 		Button nextB = (Button) ModuleActivity.this.findViewById(R.id.next_btn);
 		if (this.hasPrev()) {
 			prevB.setVisibility(View.VISIBLE);
 			prevB.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					movePrev();
 				}
 			});
 		} else {
 			prevB.setVisibility(View.INVISIBLE);
 		}
 
 		if (this.hasNext()) {
 			nextB.setVisibility(View.VISIBLE);
 			nextB.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					moveNext();
 				}
 			});
 		} else {
 			nextB.setVisibility(View.INVISIBLE);
 		}
 	}
 
 	public boolean hasPrev() {
 		if (this.currentActivityNo == 0) {
 			return false;
 		}
 		return true;
 	}
 
 	public boolean hasNext() {
 		int noActs = section.getActivities().size();
 		if (this.currentActivityNo + 1 == noActs) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	public void moveNext() {
 		this.stopReading();
 		ArrayList<org.digitalcampus.oppia.model.Activity> acts = section.getActivities();
 		this.saveTracker(acts.get(currentActivityNo).getDigest());
 		currentActivityNo++;
 		loadActivity();
 	}
 
 	public void movePrev() {
 		this.stopReading();
 		ArrayList<org.digitalcampus.oppia.model.Activity> acts = section.getActivities();
 		this.saveTracker(acts.get(currentActivityNo).getDigest());
 		currentActivityNo--;
 		loadActivity();
 	}
 
 	private boolean saveTracker(String digest) {
 		if (currentActivity != null && currentActivity.activityHasTracker()) {
 			Tracker t = new Tracker(this);
 			JSONObject json = currentActivity.getTrackerData();
 			MetaDataUtils mdu = new MetaDataUtils(this);
 			// add in extra meta-data
 			try {
 				json = mdu.getMetaData(json);
 			} catch (JSONException e) {
 				// Do nothing
 			} 
 			// if it's a baseline activity then assume completed
 			if(this.isBaselineActivity){
 				t.saveTracker(module.getModId(), digest, json, true);
 			} else {
 				t.saveTracker(module.getModId(), digest, json, currentActivity.getActivityCompleted());
 			}
 		}
 		return true;
 	}
 
 	private void createLanguageDialog() {
 		UIUtils ui = new UIUtils();
 		ui.createLanguageDialog(this, module.getLangs(), prefs, new Callable<Boolean>() {
 			public Boolean call() throws Exception {
 				ModuleActivity.this.onStart();
 				return true;
 			}
 		});
 	}
 	
 	public void onInit(int status) {
 		// check for successful instantiation
 		if (status == TextToSpeech.SUCCESS) {
 			Log.d(TAG, "tts success");
 			ttsRunning = true;
 			currentActivity.setReadAloud(true);
 			HashMap<String,String> params = new HashMap<String,String>();
 			params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,TAG);
 			myTTS.speak(currentActivity.getContentToRead(), TextToSpeech.QUEUE_FLUSH, params);
 			myTTS.setOnUtteranceCompletedListener(this);
 		} else {
 			// TTS not installed so show message
 			Toast.makeText(this, this.getString(R.string.error_tts_start), Toast.LENGTH_LONG).show();
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == TTS_CHECK) {
 			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
 				// the user has the necessary data - create the TTS
 				myTTS = new TextToSpeech(this, this);
 				
 			}
 		}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	private void stopReading() {
 		if (myTTS != null) {
 			myTTS.stop();
 			myTTS = null;
 		}
 		this.ttsRunning = false;
 	}
 
 	public void onUtteranceCompleted(String utteranceId) {
 		Log.d(TAG,"Finished reading");
 		this.ttsRunning = false;
 		myTTS = null;
 	}
 	
 	public WidgetFactory getCurrentActivity(){
 		return this.currentActivity;
 	}
 }
