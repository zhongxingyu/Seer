 package de.questmaster.fatremote.fragments;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.media.AudioManager;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.provider.Settings;
 import android.support.v4.app.Fragment;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 import de.questmaster.fatremote.DebugHelper;
 import de.questmaster.fatremote.FatRemoteSettings;
 import de.questmaster.fatremote.FatRemoteSettings.AppSettings;
 import de.questmaster.fatremote.NetworkProxy;
 import de.questmaster.fatremote.R;
 import de.questmaster.fatremote.SelectFATActivity;
 import de.questmaster.fatremote.datastructures.FATRemoteEvent;
 
 public class RemoteFragment extends Fragment implements View.OnClickListener {
 
 //	private static final String LOG_TAG = "RemoteFragment";
 	
 	protected static final String INTENT_ALLFATS = "de.questmaster.fatremote.fats";
 
 	public static final int ON_SETTINGS_CHANGE = 0;
 
 	private AppSettings mSettings = new FatRemoteSettings.AppSettings();
 	private AudioManager mAudioManager = null;
 
 	private Activity c = null;
 	private short keyCode;
 	private short keyModifier;
 
 	// TODO Listener interface?
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// enable options menu
 		this.setHasOptionsMenu(true);
 		
 		// set activity
 		c = this.getActivity();
 
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		super.onCreateView(inflater, container, savedInstanceState);
 
         if (container == null) {
             // We have different layouts, and in one of them this
             // fragment's containing frame doesn't exist.  The fragment
             // may still be created from its saved state, but there is
             // no reason to try to create its view hierarchy because it
             // won't be displayed.  Note this is not needed -- we could
             // just run the code below, where we would create and return
             // the view hierarchy; it would just never be used.
             return null;
         }
 
         // Inflate the layout for this fragment
         return inflater.inflate(R.layout.remote_fragment, container, false);
 }
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		
 		/* start audio */
 		mAudioManager = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
 		mAudioManager.loadSoundEffects();
 
 		/* read Settings */
 		mSettings.readSettings(c);
 
 		/* setup onClickListener for remote buttons */
 		((ImageView) c.findViewById(R.id.remote_button_power)).setOnClickListener(this);
 		((ImageView) c.findViewById(R.id.remote_button_up)).setOnClickListener(this);
 		((ImageView) c.findViewById(R.id.remote_button_home)).setOnClickListener(this);
 		((ImageView) c.findViewById(R.id.remote_button_left)).setOnClickListener(this);
 		((ImageView) c.findViewById(R.id.remote_button_ok)).setOnClickListener(this);
 		((ImageView) c.findViewById(R.id.remote_button_right)).setOnClickListener(this);
 		((ImageView) c.findViewById(R.id.remote_button_back)).setOnClickListener(this);
 		((ImageView) c.findViewById(R.id.remote_button_down)).setOnClickListener(this);
 		((ImageView) c.findViewById(R.id.remote_button_info)).setOnClickListener(this);
 
 		((ImageView) c.findViewById(R.id.textButton)).setOnClickListener(this);
 
 		((ImageView) c.findViewById(R.id.remote_button_rew)).setOnClickListener(this);
 		((ImageView) c.findViewById(R.id.remote_button_menu)).setOnClickListener(this);
 		((ImageView) c.findViewById(R.id.remote_button_fwd)).setOnClickListener(this);
 		((ImageView) c.findViewById(R.id.remote_button_prev)).setOnClickListener(this);
 		((ImageView) c.findViewById(R.id.remote_button_play)).setOnClickListener(this);
 		((ImageView) c.findViewById(R.id.remote_button_next)).setOnClickListener(this);
 		((ImageView) c.findViewById(R.id.remote_button_volup)).setOnClickListener(this);
 		((ImageView) c.findViewById(R.id.remote_button_stop)).setOnClickListener(this);
 		((ImageView) c.findViewById(R.id.remote_button_zoomup)).setOnClickListener(this);
 		((ImageView) c.findViewById(R.id.remote_button_voldown)).setOnClickListener(this);
 		((ImageView) c.findViewById(R.id.remote_button_mute)).setOnClickListener(this);
 		((ImageView) c.findViewById(R.id.remote_button_zoomdown)).setOnClickListener(this);
 		
 		/* setup key listener */
 		EditText text = (EditText) c.findViewById(R.id.enterText);
 		text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 	        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 	            if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
 	            	hideKeyboard();
 	            	
             		return true;
 	            }
 	            return false;
 	        }
 	    });
 		text.addTextChangedListener(determineKeyboardTextChangedListener());
 		text.setFocusable(true);
 
 		// check if WIFI available
 		if (!NetworkProxy.getInstance(c).isWifiEnabled()) {
 			Toast.makeText(c, R.string.app_err_enwifi, Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	private TextWatcher determineKeyboardTextChangedListener() {
 		TextWatcher tw = null;
 
     	//check for bad keyboards
     	boolean hwKeyboardShown = getActivity().getResources().getConfiguration().hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO;
     	String swKeyboardName = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
     	
     	if (!hwKeyboardShown && swKeyboardName.contains("nuance.xt9")) { // ASUS Transformer Prime SoftKeyboard
     		tw = new TextWatcher() {
                 public void  afterTextChanged (Editable s) { } 
                 public void  beforeTextChanged  (CharSequence s, int start, int count, int after) { } 
                 
                 public void  onTextChanged  (CharSequence s, int start, int before, int count) { 
                 	int diff = count - before;
                 	
                 	if (s.length() > 0) {
                 		// Send Character
                 		keyCode = 0xf9;
                 		if (diff > 0) { // +1
                 			keyModifier = (short) Character.codePointAt(s, before);
                 		} else if (diff < 0) { // -1
                 			keyModifier = (short) 0x7f; // backspace
                 		} else {
                 			keyModifier = (short) 0x0a; // enter
                 		}
 	
                 		// send keyCode
                 		invokeSend();
                 	}
                 }};
     	} else {	// Android Default Behaviour             	
     		tw = new TextWatcher() {
                 public void  afterTextChanged (Editable s) { } 
                 public void  beforeTextChanged  (CharSequence s, int start, int count, int after) { } 
                 
                 public void  onTextChanged  (CharSequence s, int start, int before, int count) { 
                 	if (s.length() > 0 || before != 0) {
                 		// Send Character
                 		keyCode = 0xf9;
                 		if (before == 0) {
                 			keyModifier = (short) Character.codePointAt(s, start);
                 		} else if (start == 0) {
                 			keyModifier = 0x0a; // enter
                 		} else {
                 			keyModifier = (short) 0x7f; // backspace
                 		}
 	
                 		// send keyCode
                 		invokeSend();
                 	}
                 }};
         }
     	
     	return tw;
 	}
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		super.onCreateOptionsMenu(menu, inflater);
 
 		inflater.inflate(R.menu.options_menu, menu);
 
 		// For Honeycomb and up
 		if (Build.VERSION.SDK_INT >= 11) {
 			MenuItem mi = menu.findItem(R.id.MENU_ITEM_SELECTFAT);
 			mi.setVisible(false);
 		}
 		
 		// For testing purpose
 		if (DebugHelper.SHOW_DEBUG_SCREEN) {
 			MenuItem mi = menu.findItem(R.id.MENU_ITEM_DEBUG);
 			mi.setVisible(true);
 		}
 	}
 	
 	/**
 	 * Define menu action
 	 * 
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 		case R.id.MENU_ITEM_SELECTFAT: {
 			Intent selectFAT = new Intent(Intent.ACTION_PICK);
 			selectFAT.setClass(c, SelectFATActivity.class);
 			selectFAT.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(selectFAT);
 						
 			break;
 		}
 		case R.id.MENU_ITEM_SETTINGS: {
 			Intent iSettings = new Intent();
 			iSettings.setClass(c, FatRemoteSettings.class);
 			startActivityForResult(iSettings, ON_SETTINGS_CHANGE);
 
 			break;
 		}
 		case R.id.MENU_ITEM_DEBUG: {
 			c.setContentView(R.layout.debug);
 			break;
 		}
 		default:
 			// should not happen
 			break;
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case ON_SETTINGS_CHANGE: {
 			mSettings.readSettings(c);
 			break;
 		}
 		default:
 			// should not happen
 			break;
 		}
 	}
 	
 	private void showKeyboard() {
 		// show edittext and give it focus
 		ImageView button = (ImageView) c.findViewById(R.id.textButton);
 		EditText text = (EditText) c.findViewById(R.id.enterText);
 		button.setVisibility(View.GONE);
 		text.setVisibility(View.VISIBLE);
 		text.requestFocus();
 		
 		// show keyboard
 		InputMethodManager mgr = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
 		mgr.toggleSoftInput(0, 0);
 	}
 	
 	private void hideKeyboard() {		
 		EditText text = (EditText) c.findViewById(R.id.enterText);
 		ImageView button = (ImageView) c.findViewById(R.id.textButton);
 
 		// clear entered Text
 		text.setText("");
 
 		// hide text field
 		text.setVisibility(View.GONE);
 		button.setVisibility(View.VISIBLE);
 		
 		// hide keyboard
     	InputMethodManager mgr = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
     	mgr.hideSoftInputFromWindow(text.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 	}
 
 	public void onDebugButton(View v) {
 		// short in1 = Short.decode(((TextView)findViewById(R.id.pos1)).getText().toString());
 		// short in2 = Short.decode(((TextView)findViewById(R.id.pos2)).getText().toString());
 		short in3 = Short.decode(((TextView) c.findViewById(R.id.pos3)).getText().toString());
 		short in4 = Short.decode(((TextView) c.findViewById(R.id.pos4)).getText().toString());
 
 		keyCode = in3;
 		keyModifier = in4;
 
 		// send keyCode
 		invokeSend();
 	}
 
 	@Override
 	public void onClick(View v) {
 
 		// show button again
 		EditText text = (EditText) c.findViewById(R.id.enterText);
 		if (text.getVisibility() == View.VISIBLE) {
 			hideKeyboard();
 		}
 		
 		// solve to code
 		if (v instanceof ImageView) {
 			ImageView bv = (ImageView) v;
 			String t = (String) bv.getTag();
 
 			if (t.equals("text")) {
 				showKeyboard();
 				return;
 			} else {
 				keyCode = Integer.decode(t).shortValue();	
 			}
 		}
 		keyModifier = 0x00;
 
 		// send keyCode
 		invokeSend();
 	}
 
 	private void invokeSend() {
 		// ring / vibrate
		if (!mSettings.isOverride() && mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM) != 0 || (mSettings.isOverride() && mSettings.isTone())) {
			mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK, (float) -1.0);
 		}
 
 		if (mSettings.isVibrate()) {
 			Vibrator vibrator = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
 			vibrator.vibrate(25);
 		}
 
 		// Set sending image
 		ImageView sending = (ImageView) c.findViewById(R.id.sendLED);
 		sending.setImageResource(R.drawable.light_highlight);
 		sending.invalidate();
 
 		// start sending in new thread
 		NetworkProxy.getInstance(c).addRemoteEvent(new FATRemoteEvent(keyCode, keyModifier));
 	}
 }
