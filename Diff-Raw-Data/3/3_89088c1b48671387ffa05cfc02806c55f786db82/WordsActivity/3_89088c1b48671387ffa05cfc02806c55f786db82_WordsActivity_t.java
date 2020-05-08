 package com.github.kencordero.rishi;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Locale;
 import java.util.Random;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.AssetManager;
 import android.content.res.Configuration;
 import android.graphics.drawable.Drawable;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.github.kencordero.rishi.SimpleGestureFilter.SimpleGestureListener;
 
 public class WordsActivity extends Activity implements SimpleGestureListener, OnInitListener, OnClickListener {
 	private static final String BUNDLE_FILE_KEY = "currentFileNumber";
 	private static final String BUNDLE_LOCALE_KEY = "currentLocale";
 	
 	protected AssetManager _assets;	
 	private Locale _locale;
 	private Random _random;
 	private String[] _files;
 	private ArrayList<String> _fileList;
 	private String _currentFileName;
 	private int _currentFileNumber;
 	private SimpleGestureFilter _detector;
 	private String _localeId;
 	private int _folderResId;
 	private String _imageFolderName;
 	private TextToSpeech _tts;
 	private int _resId;
 	private ImageView _imageView;
 	private TextView _textView;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_words);
 		Bundle bundle = getIntent().getExtras();
 		_folderResId = bundle.getInt("folder");
 		_imageFolderName = getString(_folderResId).toLowerCase(Locale.ENGLISH);
 		ActionBar ab = getActionBar();
 		ab.setTitle(_folderResId);
 		findImages();
 		_detector = new SimpleGestureFilter(this, this);
 		_currentFileNumber = 0;
 		_random = new Random();	
 		_tts = new TextToSpeech(this, this);
 		_imageView = (ImageView) findViewById(R.id.imgView_Words);
 		_imageView.setOnClickListener(this);
 		_textView = (TextView) findViewById(R.id.txtView_Words);
 		_textView.setOnClickListener(this);
 		setVolumeControlStream(AudioManager.STREAM_MUSIC);
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle bundle) {
 		bundle.putInt(BUNDLE_FILE_KEY , _currentFileNumber);
 		bundle.putString(BUNDLE_LOCALE_KEY, _localeId);
 	}
 	
 	protected void onRestoreInstanceState(Bundle bundle) {
 		_currentFileNumber = bundle.getInt(BUNDLE_FILE_KEY);
 		_localeId = bundle.getString(BUNDLE_LOCALE_KEY);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		loadImage();
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		_localeId = preferences.getString(SettingsFragment.KEY_PREF_LANGUAGE,  "0");
 		Configuration config = getBaseContext().getResources().getConfiguration();
 		_locale = new Locale(_localeId);
 		config.locale = _locale;
 		getBaseContext().getResources().updateConfiguration(config, null);
 		if (_localeId.equals("mr")) //There's no speech engine for Marathi			
 			_locale = new Locale("hi");		
 		
 		//_tts.setLanguage(_locale); // this doesn't seem to work
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.alternate, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.action_random:
 			loadRandomImage();
 			return true;
 		case R.id.action_settings:
 			Intent intent = new Intent(this, SettingsActivity.class);
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public boolean dispatchTouchEvent(MotionEvent me) {
 		this._detector.onTouchEvent(me);
 		return super.dispatchTouchEvent(me);
 	}
 
 	@Override
 	public void onSwipe(int direction) {
 		switch (direction) {
 		case SimpleGestureFilter.SWIPE_LEFT:
 			loadNextImage();
 			break;
 		case SimpleGestureFilter.SWIPE_RIGHT:
 			loadPreviousImage();
 			break;
 		}
 	}
 	
 	private void displayText() {
 		if (_textView.getText().length() == 0) {
 			String displayName = _currentFileName.replace(".jpg", "");
 			try {
 				_resId = R.string.class.getField(displayName).getInt(null);			
 				_textView.setText(_resId);
 			} catch (Exception e) {
 				throwError(e);
 			}
 		}
 	}
 	
 	private void speakText() {
 		if (_resId > 0)
		{
 			_tts.setLanguage(_locale);
 			_tts.speak(getString(_resId), TextToSpeech.QUEUE_ADD, null);
		}
 	}
 
 	private void findImages() {
 		_assets = getAssets();
 		try {
 			_files = _assets.list(_imageFolderName);
 			_fileList = new ArrayList<String>(Arrays.asList(_files));
 			if (_folderResId != R.string.activity_letters_name && 
 				_folderResId != R.string.activity_numbers_name)
 				Collections.shuffle(_fileList);
 		} catch (Exception e) {
 			throwError(e);
 		}
 	}
 
 	private void loadImage() {
 		//_currentFileName = _files[_currentFileNumber];
 		_currentFileName = _fileList.get(_currentFileNumber);
 		InputStream stream = null;
 		try {
 			stream = _assets.open(_imageFolderName + "/" + _currentFileName);
 		} catch (Exception e) {
 			throwError(e);
 		}
 		Drawable img = Drawable.createFromStream(stream, _currentFileName);
 		//ImageView iv = (ImageView) findViewById(R.id.imgView_Words);
 		_imageView.setImageDrawable(img);				
 		_textView.setText("");
 		// not resetting _resId causes tts to speak previous word
 		_resId = -1; 
 	}
 
 	private void loadNextImage() {
 		_currentFileNumber = (++_currentFileNumber) % (_files.length);
 		loadImage();
 	}
 
 	private void loadPreviousImage() {
 		_currentFileNumber = (--_currentFileNumber) % (_files.length);
 		if (_currentFileNumber < 0)
 			_currentFileNumber = _files.length - 1;
 		loadImage();
 	}
 
 	private void loadRandomImage() {
 		int randInt;
 		do {
 			randInt = _random.nextInt(_files.length);
 		} while (randInt == _currentFileNumber);
 		_currentFileNumber = randInt;
 		loadImage();
 	}
 
 	private void throwError(Exception e) {
 		Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
 		e.printStackTrace();
 	}
 
 	@Override
 	public void onInit(int arg0) {}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		_tts.stop();
 	}
 	
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.imgView_Words:
 			displayText();
 			speakText();
 			//onImageClick(v);
 			break;
 		case R.id.txtView_Words:
 			speakText();
 			//onTextClick(v);
 			break;
 		}
 	}
 }
