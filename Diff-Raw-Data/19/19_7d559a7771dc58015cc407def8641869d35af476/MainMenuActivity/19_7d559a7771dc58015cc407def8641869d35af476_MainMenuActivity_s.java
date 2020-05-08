 package org.pmix.ui;
 
 import org.a0z.mpd.MPD;
 import org.a0z.mpd.MPDServerException;
 import org.a0z.mpd.MPDStatus;
 import org.a0z.mpd.MPDStatusMonitor;
 import org.a0z.mpd.event.MPDConnectionStateChangedEvent;
 import org.a0z.mpd.event.MPDPlaylistChangedEvent;
 import org.a0z.mpd.event.MPDRandomChangedEvent;
 import org.a0z.mpd.event.MPDRepeatChangedEvent;
 import org.a0z.mpd.event.MPDStateChangedEvent;
 import org.a0z.mpd.event.MPDTrackChangedEvent;
 import org.a0z.mpd.event.MPDTrackPositionChangedEvent;
 import org.a0z.mpd.event.MPDUpdateStateChangedEvent;
 import org.a0z.mpd.event.MPDVolumeChangedEvent;
 import org.a0z.mpd.event.StatusChangeListener;
 import org.a0z.mpd.event.TrackPositionListener;
 import org.pmix.settings.Contexte;
 import org.pmix.settings.Settings;
 import android.util.Log;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Message;
 import android.text.Layout.Alignment;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageSwitcher;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.helloandroid.android.horizontalslider.HorizontalSlider;
 
 public class MainMenuActivity extends Activity implements StatusChangeListener, TrackPositionListener {
 
 	public static final String PREFS_NAME = "pmix.properties";
 
 	public static final int ARTISTS = 1;
 
 	public static final int SETTINGS = 3;
 
 	private TextView artistNameText;
 
 	private TextView songNameText;
 
 	private TextView albumNameText;
 
 	private MPDStatusMonitor monitor;
 
 	public static final int ALBUMS = 4;
 
 	public static final int FILES = 5;
 
 	private TextView mainInfo = null;
 
 	private HorizontalSlider progressBar = null;
 
 	private HorizontalSlider progressBarTrack = null;
 
 	private TextView trackTime = null;
 
 	private MyHandler handler;
 
 	private ImageSwitcher coverSwitcher;
 
 	private static final int VOLUME_STEP = 5;
 
 	private static final int TRACK_STEP = 10;
 
 	//protected void onActivityResult(int requestCode, int resultCode, String data, Bundle extras) {
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
     Log.v("PMIX", "MAIN ACTIVITY RESULT: " + requestCode);
 		//super.onActivityResult(requestCode, resultCode, data, extras);
 		super.onActivityResult(requestCode, resultCode, data);
     Log.v("PMIX", "MAIN ACTIVITY RESULT: " + requestCode);
 
 		switch (requestCode) {
 		case SETTINGS:
 			init();
 			break;
 
 		default:
 			break;
 		}
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		/*try {
 			//if (monitor != null)
         // FIXME -bob
 				//monitor.setMpd(Contexte.getInstance().getMpd());
 		} catch (MPDServerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}*/
 	}
 
 	private void init() {
     Log.v("PMIX", "Starting main view");
 		setContentView(R.layout.main);
 		mainInfo = (TextView) findViewById(R.id.mainInfo);
 		//progressBar = (HorizontalSlider) findViewById(R.id.progress_volume);
 		artistNameText = (TextView) findViewById(R.id.artistName);
 		albumNameText = (TextView) findViewById(R.id.albumName);
 		songNameText = (TextView) findViewById(R.id.songName);
 
 		//progressBarTrack = (HorizontalSlider) findViewById(R.id.progress_track);
 
 		trackTime = (TextView) findViewById(R.id.trackTime);
 
 		try {
 			// final MPD mpd = Contexte.getInstance().getMpd();
 
 		/*	progressBar.setOnProgressChangeListener(new HorizontalSlider.OnProgressChangeListener() {
 
 				//@Override
 				public void onProgressChanged(View v, int progress) {
 					try {
 						Contexte.getInstance().getMpd().setVolume(progress);
 					} catch (MPDServerException e) {
 						e.printStackTrace();
 					}
 
 				}
 			});
 
 			progressBarTrack.setOnProgressChangeListener(new HorizontalSlider.OnProgressChangeListener() {
 
 				//@Override
 				public void onProgressChanged(View v, int progress) {
 					try {
 						int position = (progress * handler.getCurrentSongTime()) / 100;
 						Contexte.getInstance().getMpd().seek(position);
 					} catch (MPDServerException e) {
 						e.printStackTrace();
 					}
 
 				}
 			});
 */
 			String mpdVersion = Contexte.getInstance().getMpd().getMpdVersion();
 
 			//((TextView) findViewById(R.id.volume)).setAlignment(Alignment.ALIGN_CENTER);
 			((TextView) findViewById(R.id.volume)).setTextSize(12);
 			//((TextView) findViewById(R.id.track)).setAlignment(Alignment.ALIGN_CENTER);
 			((TextView) findViewById(R.id.track)).setTextSize(12);
 			//((TextView) findViewById(R.id.trackTime)).setAlignment(Alignment.ALIGN_CENTER);
 			((TextView) findViewById(R.id.trackTime)).setTextSize(12);
 
 			coverSwitcher = (ImageSwitcher) findViewById(R.id.albumCover);
 
 			handler = new MyHandler(this);
			//coverSwitcher.setFactory(handler);
 
 			StringBuffer stringBuffer = new StringBuffer(100);
 
 			stringBuffer.append("\nMPD version " + mpdVersion + " running at " + Settings.getInstance().getServerAddress() + "\n");
 
 			monitor = new MPDStatusMonitor(Contexte.getInstance().getMpd(), 500);
 			monitor.addStatusChangeListener(this);
 			monitor.addTrackPositionListener(this);
       Log.v("PMIX", "mid main view");
 
 			Button button = (Button) findViewById(R.id.next);
 			button.setOnClickListener(new Button.OnClickListener() {
 				public void onClick(View v) {
 
 					try {
 						Contexte.getInstance().getMpd().next();
 					} catch (MPDServerException e) {
 						e.printStackTrace();
 					}
 				}
 			});
 
 			button = (Button) findViewById(R.id.prev);
 			button.setOnClickListener(new Button.OnClickListener() {
 				public void onClick(View v) {
 					try {
 						Contexte.getInstance().getMpd().previous();
 					} catch (MPDServerException e) {
 						e.printStackTrace();
 					}
 				}
 			});
 
 			button = (Button) findViewById(R.id.back);
 			button.setOnClickListener(new Button.OnClickListener() {
 				public void onClick(View v) {
 
 					try {
 						Contexte.getInstance().getMpd().seek(handler.getLastKnownElapsedTime() - TRACK_STEP);
 					} catch (MPDServerException e) {
 						e.printStackTrace();
 					}
 				}
 			});
 
 			button = (Button) findViewById(R.id.playpause);
 			button.setOnClickListener(new Button.OnClickListener() {
 				public void onClick(View v) {
 
 					try {
 						Contexte.getInstance().getMpd().pause();
 					} catch (MPDServerException e) {
 						e.printStackTrace();
 					}
 				}
 			});
 
 			button = (Button) findViewById(R.id.forward);
 			button.setOnClickListener(new Button.OnClickListener() {
 				public void onClick(View v) {
 					try {
 						Contexte.getInstance().getMpd().seek(handler.getLastKnownElapsedTime() + TRACK_STEP);
 					} catch (MPDServerException e) {
 						e.printStackTrace();
 					}
 				}
 			});
 
 			monitor.start();
 
 			mainInfo.setText(stringBuffer.toString());
 
 		} catch (MPDServerException e) {
 			this.setTitle("Error");
 		}
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		// mainInfo.setText(keyCode + "");
 		try {
 			switch (keyCode) {
 			case KeyEvent.KEYCODE_VOLUME_UP:
 				//progressBar.incrementProgressBy(VOLUME_STEP);
 				Contexte.getInstance().getMpd().adjustVolume(VOLUME_STEP);
 				return true;
 			case KeyEvent.KEYCODE_VOLUME_DOWN:
 				//progressBar.incrementProgressBy(-VOLUME_STEP);
 				Contexte.getInstance().getMpd().adjustVolume(-VOLUME_STEP);
 				return true;
 			case KeyEvent.KEYCODE_DPAD_LEFT:
 				Contexte.getInstance().getMpd().previous();
 				return true;
 			case KeyEvent.KEYCODE_DPAD_RIGHT:
 				Contexte.getInstance().getMpd().next();
 				return true;
 			default:
 				return false;
 			}
 		} catch (MPDServerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 
 	}
 
 	@Override
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
     Log.v("PMIX","Creating Main Shit");
 
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		if (settings.getAll().get("serverAddress") == null) {
       Log.v("PMIX","*Creating Main Shit");
 			//this.startSubActivity(new Intent(this, SettingsActivity.class), SETTINGS);
 			startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS);
 		}
 		// Settings.getInstance().setServerAddress(set)
 		else {
       Log.v("PMIX","$Creating Main Shit");
 			init();
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		boolean result = super.onCreateOptionsMenu(menu);
 		menu.add(0, ARTISTS, 0, R.string.artists);
 		menu.add(2, ALBUMS, 0, R.string.albums);
 		menu.add(3, FILES, 0, R.string.files);
 		menu.add(5, SETTINGS, 0, R.string.settings);
 		return result;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		Intent i = null;
 
 		switch (item.getItemId()) {
 
 		case ARTISTS:
 
 			i = new Intent(this, ArtistsActivity.class);
 			//startSubActivity(i, ARTISTS);
 			startActivity(i);
 			return true;
 		case ALBUMS:
 
 			i = new Intent(this, AlbumsActivity.class);
 			//startSubActivity(i, ALBUMS);
 			startActivity(i);
 			return true;
 		case FILES:
 
 			i = new Intent(this, FSActivity.class);
 			//startSubActivity(i, FILES);
 			startActivity(i);
 			return true;
 		case SETTINGS:
 			i = new Intent(this, SettingsActivity.class);
 			//startSubActivity(i, FILES);
 			startActivity(i);
 			return true;
 		default:
 			// showAlert("Menu Item Clicked", "Not yet implemented", "ok", null,
 			// false, null);
 			return true;
 		}
 
 	}
 
 	public void connectionStateChanged(MPDConnectionStateChangedEvent event) {
 		// this.setTitle(event.isConnectionLost() + "");
 	}
 
 	public void playlistChanged(MPDPlaylistChangedEvent event) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void randomChanged(MPDRandomChangedEvent event) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void repeatChanged(MPDRepeatChangedEvent event) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void stateChanged(MPDStateChangedEvent event) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void trackChanged(MPDTrackChangedEvent event) {
 
 		MPDStatus status = event.getMpdStatus();
 
 		// if (status.getState().equals(MPDStatus.MPD_STATE_PLAYING)) {
 
 		Message message = Message.obtain();
 		message.obj = status;
 		handler.sendMessage(message);
 		// }
 
 	}
 
 	public TextView getMainInfo() {
 		return mainInfo;
 	}
 
 	public void updateStateChanged(MPDUpdateStateChangedEvent event) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void volumeChanged(MPDVolumeChangedEvent event) {
 		MPDStatus status = event.getMpdStatus();
 
 		Message message = Message.obtain();
 		message.obj = status;
 		handler.sendMessage(message);
 
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		Contexte.getInstance().disconnect();
 		savePreferences();
 	}
 
 	/*@Override
 	protected void onFreeze(Bundle outState) {
 		super.onFreeze(outState);
 		Contexte.getInstance().disconnect();
 	}*/
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		Contexte.getInstance().disconnect();
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		Contexte.getInstance().disconnect();
 		savePreferences();
 	}
 
 	private void savePreferences() {
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString("serverAddress", Settings.getInstance().getServerAddress());
 
 		editor.commit();
 	}
 
 	public ProgressBar getProgressBar() {
 		return progressBar;
 	}
 
 	public ProgressBar getProgressBarTrack() {
 		return progressBarTrack;
 	}
 
 	public void setProgressBarTrack(HorizontalSlider progressBarTrack) {
 		this.progressBarTrack = progressBarTrack;
 	}
 
 	public void trackPositionChanged(MPDTrackPositionChangedEvent event) {
 		MPDStatus status = event.getMpdStatus();
 
 		Message message = Message.obtain();
 		message.obj = status;
 		handler.sendMessage(message);
 
 	}
 
 	public TextView getTrackTime() {
 		return trackTime;
 	}
 
 	public ImageSwitcher getCoverSwitcher() {
 		return coverSwitcher;
 
 	}
 
 	public TextView getArtistNameText() {
 		return artistNameText;
 	}
 
 	public TextView getSongNameText() {
 		return songNameText;
 	}
 
 	public TextView getAlbumNameText() {
 		return albumNameText;
 	}
 }
