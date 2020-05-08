 package com.mamewo.malarm24;
 
 /**
  * @author Takashi Masuyama <mamewotoko@gmail.com>
  */
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.MessageFormat;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.media.AudioManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.CheckBoxPreference;
 import android.preference.Preference;
 import android.preference.PreferenceManager;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.util.Log;
 import android.view.View;
 
 public class MalarmPreference
 	extends PreferenceActivity
 	implements OnPreferenceClickListener,
 		View.OnClickListener,
 		FileFilter,
 		OnSharedPreferenceChangeListener
 {
 	private Preference help_;
 	private Preference version_;
 	private Preference createPlaylist_;
 	private Preference sleepTime_;
 	private Preference wakeupTime_;
 	private Preference sleepVolume_;
 	private Preference wakeupVolume_;
 	private Preference reloadPlaylist_;
 	private Preference clearWebviewCache_;
 	private View logo_;
 	private CheckBoxPreference sleepPlaylist_;
 	private CheckBoxPreference wakeupPlaylist_;
 	private SharedPreferences pref_;
 	private static final String TAG = "malarm";
 	static final
 	private int VERSION_DIALOG = 1;
 
 	//default values are set in onCreate, so following values are not so important
 	final static
 	public boolean DEFAULT_VIBRATION = true;
 	final static
 	public String DEFAULT_SLEEPTIME = "60";
 	final static
 	public String DEFAULT_WAKEUP_TIME = "7:00";
 	final static
 	public String DEFAULT_SLEEP_VOLUME = "3";
 	final static
 	public String DEFAULT_WAKEUP_VOLUME = "10";
 	final static
 	public String DEFAULT_WEB_LIST = "";
 	final static
 	public File DEFAULT_PLAYLIST_PATH =
 		new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_MUSIC);
 
 	//list preference keys
 	final static
 	public String PREFKEY_URL_LIST = "url_list";
 	final static
 	public String PREFKEY_PLAYLIST_PATH = "playlist_path";
 	final static
 	public String PREFKEY_SLEEP_TIME = "sleeptime";
 	final static
 	public String PREFKEY_WAKEUP_TIME = "default_time";
 	final static
 	public String PREFKEY_VIBRATE = "vibrate";
 	final static
 	public String PREFKEY_SLEEP_VOLUME = "sleep_volume";
 	final static
 	public String PREFKEY_WAKEUP_VOLUME = "wakeup_volume";
 	final static
 	public String PREFKEY_USE_NATIVE_PLAYER = "use_native_player";
 	
 	@Override
 	public boolean accept(File pathname) {
 		String filename = pathname.getName();
 		
 		//TODO: other formats? mp4, m4v...
 		return filename.endsWith(".mp3")
 				|| filename.endsWith(".mp4")
 				|| filename.endsWith(".m4a")
 				|| filename.endsWith(".m4v")
 				|| filename.endsWith(".ogg");
 	}
 
 	private void createDefaultPlaylist(File file) {
 		FileWriter fw = null;
 		try {
 			fw = new FileWriter(file);
 			//find music files
 			for (File music_file : file.getParentFile().listFiles(this)) {
 				fw.append(music_file.getName() + "\n");
 			}
 		}
 		catch (IOException e) {
 			Log.i(TAG, "cannot write: " + e.getMessage());
 		}
 		finally {
 			if (fw != null) {
 				try {
 					fw.close();
 				}
 				catch (IOException e) {
 					Log.i(TAG, "cannot close: " + e.getMessage());
 				}
 			}
 		}
 	}
 
 	
 	@Override
 	public boolean onPreferenceClick(Preference preference) {
 		boolean result = false;
 		if (preference == help_) {
 			Uri url = Uri.parse(getString(R.string.help_url));
 			startActivity(new Intent(Intent.ACTION_VIEW, url));
 			result = true;
 		}
 		else if (preference == version_) {
 			showDialog(VERSION_DIALOG);
 			result = true;
 		}
 		else if (preference == createPlaylist_) {
 			String [] playlists = 
 				{ MalarmPlayerService.WAKEUP_PLAYLIST_FILENAME,
 					MalarmPlayerService.SLEEP_PLAYLIST_FILENAME };
 			for (String filename : playlists) {
 				File file = new File(MalarmActivity.prefPlaylistPath, filename);
 				if (file.exists()) {
 					//show confirm dialog?
 					Log.i(TAG, "playlist file exists: " + filename);
 					String msg = MessageFormat.format(getString(R.string.file_exists_format), filename);
 					MalarmActivity.showMessage(this, msg);
 					continue;
 				}
 				createDefaultPlaylist(file);
 				String msg = MessageFormat.format(getString(R.string.playlist_created_format),
 												filename);
 				MalarmActivity.showMessage(this, msg);
 			}
 			result = true;
 		}
 		else if (preference == sleepPlaylist_ || preference == wakeupPlaylist_) {
 			Log.d(TAG, "View Sleep Playlist from check");
 			//TODO: move check code to Playlist class
 			String prefKey;
 			String playlistFilename;
 
 			if (preference == sleepPlaylist_) {
 				prefKey = "sleep";
 				playlistFilename = MalarmPlayerService.SLEEP_PLAYLIST_FILENAME;
 			}
 			else {
 				prefKey = "wakeup";
 				playlistFilename = MalarmPlayerService.WAKEUP_PLAYLIST_FILENAME;
 			}
 			//get playlist path
 			String path = 
 					pref_.getString(MalarmPreference.PREFKEY_PLAYLIST_PATH,
 							DEFAULT_PLAYLIST_PATH.getAbsolutePath());
 			//TODO: add dependency from playlist path
 			if (! (new File(path, playlistFilename)).exists()) {
 				MalarmActivity.showMessage(this, getString(R.string.pref_playlist_does_not_exist));
 				((CheckBoxPreference) preference).setChecked(false);
 			}
 			else {
 				Intent i = new Intent(this, PlaylistViewer.class);
 				//TODO: define key as constant
 				i.putExtra("playlist", prefKey);
 				startActivity(i);
 			}
 			result = true;
 		}
 		else if (preference == clearWebviewCache_) {
 			String prefKey = preference.getKey();
 			SharedPreferences pref = preference.getSharedPreferences();
 			SharedPreferences.Editor editor = preference.getEditor();
 			editor.putBoolean(prefKey, !pref.getBoolean(prefKey, false));
			//compatibility: apply is not available in 7
 			editor.apply();
 			result = true;
 		}
 		else if (preference == reloadPlaylist_) {
 			Intent i = new Intent(this, MalarmPlayerService.class);
 			i.setAction(MalarmPlayerService.LOAD_PLAYLIST_ACTION);
 			startService(i);
 			updatePlaylistUI();
 			MalarmActivity.showMessage(this, getString(R.string.playlist_reload));
 			result = true;
 		}
 		return result;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		pref_ = PreferenceManager.getDefaultSharedPreferences(this);
 		addPreferencesFromResource(R.xml.preference);
 		version_ = findPreference("malarm_version");
 		PackageInfo pi;
 		try {
 			pi = getPackageManager().getPackageInfo(MalarmActivity.PACKAGE_NAME, 0);
 			version_.setSummary(pi.versionName);
 		}
 		catch (NameNotFoundException e) {
 			version_.setSummary("unknown");
 		}
 		version_.setOnPreferenceClickListener(this);
 		help_ = findPreference("help");
 		help_.setOnPreferenceClickListener(this);
 		createPlaylist_ = findPreference("create_playlist");
 		createPlaylist_.setOnPreferenceClickListener(this);
 		sleepTime_ = findPreference("sleeptime");
 		wakeupTime_ = findPreference("default_time");
 		wakeupVolume_ = findPreference("wakeup_volume");
 		reloadPlaylist_ = findPreference("reload_playlist");
 		reloadPlaylist_.setOnPreferenceClickListener(this);
 		sleepVolume_ = findPreference("sleep_volume");
 		sleepPlaylist_ = (CheckBoxPreference) findPreference("sleep_playlist");
 		sleepPlaylist_.setOnPreferenceClickListener(this);
 		wakeupPlaylist_ = (CheckBoxPreference) findPreference("wakeup_playlist");
 		wakeupPlaylist_.setOnPreferenceClickListener(this);
 		
 		clearWebviewCache_ = findPreference("clear_webview_cache");
 		clearWebviewCache_.setOnPreferenceClickListener(this);
 
 		pref_.registerOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	public void onDestroy() {
 		pref_.unregisterOnSharedPreferenceChangeListener(this);
 		super.onDestroy();
 	}
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 		updatePlaylistUI();
 	}
 	
 	//TODO: call this when playlist_path is modified by user
 	private void updatePlaylistUI(){
 		//get playlist path
 		String path =
 			pref_.getString(MalarmPreference.PREFKEY_PLAYLIST_PATH,
 							DEFAULT_PLAYLIST_PATH.getAbsolutePath());
 		File sleepFile =
 			new File(path, MalarmPlayerService.SLEEP_PLAYLIST_FILENAME);
 		sleepPlaylist_.setChecked(sleepFile.exists());
 		File wakeupFile =
 			new File(path, MalarmPlayerService.WAKEUP_PLAYLIST_FILENAME);
 		wakeupPlaylist_.setChecked(wakeupFile.exists());
 	}
 	
 	@Override
 	public void onClick(View v) {
 		if (v == logo_) {
 			Intent i =
 					new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.git_url)));
 			startActivity(i);
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		updateSummary(pref_, "ALL");
 	}
 
 	private void setVolumeSummary(SharedPreferences pref, String key,
 								Preference prefUI, String defaultVolumeStr) {
 		int volume =
 				Integer.valueOf(pref.getString(key, defaultVolumeStr));
 		AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		int maxVolume = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 		StringBuilder sb = new StringBuilder();
 		for(int i = 0; i < volume; i++){
 			sb.append("|");
 		}
 		for(int i = 0; i < maxVolume - volume; i++){
 			sb.append(".");
 		}
 		prefUI.setSummary(String.format("%s (%s/%d)", sb.toString(), volume, maxVolume));
 	}
 	
 	public void updateSummary(SharedPreferences pref, String key) {
 		boolean updateAll = "ALL".equals(key);
 		if (updateAll || "default_time".equals(key)) {
 			String timestr =
 					pref.getString("default_time", MalarmPreference.DEFAULT_WAKEUP_TIME);
 			String[] hourminStr = timestr.split(":");
 			String hour = hourminStr[0];
 			String min = hourminStr[1];
 			if(min.length() == 1) {
 				min = "0" + min;
 			}
 			wakeupTime_.setSummary(hour + ":" + min);
 		}
 		if (updateAll || "sleeptime".equals(key)) {
 			String sleepTime =
 				pref.getString("sleeptime", MalarmPreference.DEFAULT_SLEEPTIME);
 			String summary = MessageFormat.format(getString(R.string.unit_min), sleepTime);
 			sleepTime_.setSummary(summary);
 		}
 		if (updateAll || "sleep_volume".equals(key)) {
 			setVolumeSummary(pref, "sleep_volume", sleepVolume_, DEFAULT_SLEEP_VOLUME);
 		}
 		if (updateAll || "wakeup_volume".equals(key)) {
 			setVolumeSummary(pref, "wakeup_volume", wakeupVolume_, DEFAULT_WAKEUP_VOLUME);
 		}
 		//url_list has no summary
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog;
 		switch(id) {
 		case VERSION_DIALOG:
 			//show dialog
 			dialog = new Dialog(this);
 			dialog.setContentView(R.layout.dialog);
 			logo_ = dialog.findViewById(R.id.dialog_logo);
 			logo_.setOnClickListener(this);
 			dialog.setTitle(R.string.dialog_title);
 			break;
 		default:
 			dialog = null;
 			break;
 		}
 		return dialog;
 	}
 	
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
 		updateSummary(pref, key);
 	}
 }
