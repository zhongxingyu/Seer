 package com.music;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.codehaus.jackson.map.ObjectMapper;
 
 import android.app.Activity;
 import android.app.Notification;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.media.AudioManager;
 import android.media.AudioManager.OnAudioFocusChangeListener;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnErrorListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.media.audiofx.Equalizer;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.PowerManager;
 import android.os.RemoteException;
 import android.util.Log;
 
 import com.music.pojo.EqSetting;
 import com.music.pojo.Music;
 
 public class MusicPlayerService extends Service implements OnPreparedListener,
 		OnErrorListener, OnCompletionListener, OnAudioFocusChangeListener {
 
 	private int savedpos;
 
 	private Equalizer equalizer;
 
 	short[] bandslevels = new short[5];
 
 	short[] minMaxEQLevels;
 
 	private boolean isLoadedPrefData = false;
 
 	boolean isUserRequest = false;
 
 	private EqSetting setupEQ(MediaPlayer mp) {
 
 		if (mp != null) { // from processPlayRequest()
 			if (equalizer != null) {
 				equalizer.release();
 			}
 			equalizer = new Equalizer(0, mp.getAudioSessionId());
 			equalizer.setEnabled(true);
 
 		} else {
 			MediaPlayer tempMp = new MediaPlayer();
 			equalizer = new Equalizer(0, tempMp.getAudioSessionId());
 		}
 
 		short bands = equalizer.getNumberOfBands();
 
 		minEQLevel = equalizer.getBandLevelRange()[0];
 		maxEQLevel = equalizer.getBandLevelRange()[1];
 
 		Log.d("service", "bands : " + bands);
 		Log.d("service", "minEQ : " + minEQLevel);
 		Log.d("service", "maxEQ : " + maxEQLevel);
 
 		minMaxEQLevels = new short[2];
 		minMaxEQLevels[0] = minEQLevel;
 		minMaxEQLevels[1] = maxEQLevel;
 
 		if (setEQ == true) {
 			Log.d("service", "setEQ==true");
 
 			short i = 0;
 			for (i = 0; i < bands; i++) {
 				equalizer
 						.setBandLevel(i, (short) (bandslevels[i] - maxEQLevel));
 				Log.d("service", "setbandlevel: " + bandslevels[i]);
 			}
 
 		} else {
 			// if there is saved setting. used only on initialize. from LOAD_EQ
 
 			if (!isLoadedPrefData) {
 				EqSetting eqSetting = openPreferenceData();
 				if (eqSetting != null) {
 					bandslevels[0] = eqSetting.getEq1();
 					bandslevels[1] = eqSetting.getEq2();
 					bandslevels[2] = eqSetting.getEq3();
 					bandslevels[3] = eqSetting.getEq4();
 					bandslevels[4] = eqSetting.getEq5();
 					isLoadedPrefData = true;
 					currentVol = (float) eqSetting.getVolume();
 
 					return eqSetting;
 				}
 			}
 
 			// default
 			for (short i = 0; i < bands; i++) {
 				Log.d("service", "bandLv : " + equalizer.getBandLevel(i));
 				bandslevels[i] = (short) (minMaxEQLevels[1] + equalizer
 						.getBandLevel(i));
 			}
 		}
 
 		return null;
 	}
 
 	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (intent.getAction().equals(
 					android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
 				Log.d("service", "audio becoming noisy");
 				if (musicp != null && musicp.isPlaying()) {
 
 					savedpos = musicp.getCurrentPosition();
 
 					Log.d("service", "state= State.mnull");
 
 					state = State.mnull;
 
 					Message mes = Message.obtain(null,
 							MusicPlayerService.SEEKBAR_PROGRESS, null);
 					Bundle b = new Bundle();
 					b.putInt("duration", duration / 1000);
 
 					b.putInt("currentposition", savedpos / 1000);
 					mes.setData(b);
 					try {
 						if (player != null)
 							player.send(mes);
 					} catch (RemoteException e) {
 						e.printStackTrace();
 					}
 					Message m = Message.obtain(null,
 							MusicPlayerService.AUDIO_BECOMING_NOISY, null);
 					try {
 						if (player != null)
 							player.send(m);
 					} catch (RemoteException e) {
 						//
 						e.printStackTrace();
 					}
 					noisystate = BecomeNoisyState.True;
 					musicp.pause();
 
 				}
 			}
 		}
 	};
 
 	enum BecomeNoisyState {
 		True, False
 	}
 
 	BecomeNoisyState noisystate = BecomeNoisyState.False;
 
 	public static final int PLAY = 1;
 	public static final int PAUSE = 2;
 	public static final int SKIP = 3;
 	public static final int REWIND = 4;
 
 	// repeat setting
 	public static final int straight = 0;
 	public static final int onesong = 1;
 	public static final int allsongs = 2;
 
 	@Override
 	public boolean onUnbind(Intent intent) {
 		Log.d("service", "onUnbind");
 		// runstopTimer();
 
 		return super.onUnbind(intent);
 	}
 
 	public static final int SET_PLAY_REQUEST = 5;
 	public static final int SET_ALLSONGS = 6;
 
 	public static final int COMPLETION = 7;
 	public static final int REPEAT_ENABLED = 8;
 	private boolean repeatEnabled = false;
 
 	public static final int REGISTER_ACTIVITY = 9;
 
 	public static ArrayList<Music> requests = new ArrayList<Music>();
 
 	public ArrayList<Music> tempShuffledRequests = new ArrayList<Music>();
 	public String tempShuffledPlaylistName = null;
 
 	static final int AUDIO_BECOMING_NOISY = 10;
 
 	static final int SEEKBAR_PROGRESS = 11;
 
 	static final int IS_PLAYING = 12;
 
 	static final int ALLCOMPLETION = 13;
 	final Messenger mes = new Messenger(new IncomingHandler(this));
 
 	public static final int SEEKBAR_SET = 14;
 
 	public static final int MUSICLIST_ITEM_POS = 15;
 
 	final public static int ALLMUSICLIST = 16;
 	final public static int PLAYER = 17;
 	final public static int LASTPLAYLIST = 18;
 
 	public final static int INFO = 20;
 
 	final public static int PREVTITLEARTIST = 21;
 
 	final public static int PLAYLIST_MUSIC_DELETE = 22;
 
 	public static final int HASH = 23;
 
 	public static final int PREVHASH = 24;
 
 	public static final int PREVCOMMENTS = 25;
 
 	public static final int UNBINDSERVICE = 26;
 
 	public static final int SET_VOLUME = 27;
 
 	public static final int LOAD_EQ = 28;
 
 	public static final int SET_EQ = 29;
 
 	public static final int SET_LOOP = 31;
 
 	// TODO static finals END
 
 	private Messenger player;
 
 	private Messenger list;
 
 	private int currentIndex;
 
 	enum ButtonType {
 		rewindInNullState, play
 	}
 
 	ButtonType bt = ButtonType.play;
 
 	enum State {
 		unknown, play, pause, completion, mnull, nextsong, lossTransient, rewind
 	}
 
 	State state = State.unknown;
 
 	Boolean pause = false;
 
 	private String playlistName = null;
 
 	boolean setEQ = false;
 
 	public void replyMessage(Object o, int type, Bundle b, Messenger mes) {
 		Log.d("player", "sendMessage");
 
 		Message msg = Message.obtain(null, type, o);
 		if (b == null) {
 			b = new Bundle();
 		}
 		msg.setData(b);
 		try {
 			mes.send(msg);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private float currentVol = 1;
 	private int loopState;
 
 	public static float getVolume(float uiData) {
 		return (float) (uiData / 100);
 	}
 
 	@SuppressWarnings("unchecked")
 	private void handleMessageFromActivity(Message msg) {
 
 		Bundle bun = msg.getData();
 		if (bun.getString("Activity") != null
 				&& bun.getString("Activity").equals("player")) {
 			player = msg.replyTo; // 他の箇所でいろいろplayerを参照して通信しているので確保
 			Log.d("service", "replyto Player");
 		}
 		if (bun.getString("Activity") != null
 				&& bun.getString("Activity").equals("list")) {
 			list = msg.replyTo;
 			Log.d("service replyto", "list");
 		} else {
 			Log.d("service ", "replyto null");
 		}
 		Log.d("service", "handlemessage: type" + msg.what);
 		Bundle b = null;
 
 		switch (msg.what) {
 
 		// TODO incoming handler
 
 		case SET_VOLUME:
 			// TODO case SET_VOLUME
 			currentVol = bun.getFloat("vol");
 			float vol = MusicPlayerService.getVolume(currentVol);
 			Log.d("service", "SET_VOLUME: " + vol);
 
 			if (musicp != null) {
 				Log.d("service", "!=null, currentVol: " + currentVol);
 				musicp.setVolume(vol, vol);
 			} else {
 
 			}
 
 			break;
 
 		case UNBINDSERVICE:
 			Log.d("service", "UNBINDSERVICE");
 			if (bun.getString("Activity") != null
 					&& bun.getString("Activity").equals("player")) {
 				player = null;
 				Log.d("service", "player unbind");
 			} else if (bun.getString("Activity") != null
 					&& bun.getString("Activity").equals("list")) {
 				list = null;
 				Log.d("service", "list unbind");
 			} else {
 			}
 			break;
 
 		case PLAYLIST_MUSIC_DELETE:
 
 			if (playlistName == null) {
 				Log.d("service delete",
 						" current requests is from all music list");
 			} else if (playlistName.equals(bun.getString("playlistName"))) {
 				Log.d("service delete", "equals");
 				int num = bun.getInt("num");
 
 				requests.remove(num);
 
 				for (Music m : requests) {
 					Log.d("service delete", " before: name:" + m.getTitle());
 				}
 
 				Log.d("service delete", "cur: " + currentIndex + " num: " + num);
 
 				if (currentIndex > num) {
 					Log.d("service delete", "fix order");
 					currentIndex--;
 				} else if (currentIndex == num) {
 					Log.d("service delete",
 							"current song is deleted from the playlist");
 					if (state == State.play) {
 						if (musicp != null) {
 							Log.d("service delete", " State=play  musicp!=null");
 							musicp.pause();
 							musicp.stop();
 						} else {
 							Log.d("service delete", " musicp null");
 						}
 
 					} else if (musicp != null) {
 						Log.d("service delete", "state: " + state.toString());
 						musicp.stop();
 						musicp.reset();
 
 					} else {
 						Log.d("service delete", " unknown");
 					}
 					savedpos = 0;
 
 					if (currentIndex == requests.size()) {
 						currentIndex = 0;
 					} else {
 						processPlayRequest();
 					}
 
 				} else if (currentIndex < num) {
 					Log.d("service delete", "check currentIndex < num");
 					for (Music m : requests) {
 						Log.d("service delete",
 								" check!! after: name:" + m.getTitle());
 					}
 				}
 				for (Music m : requests) {
 					Log.d("service delete", " after: name:" + m.getTitle());
 				}
 			} else {
 				Log.d("playlist", " It's not a current playlist");
 			}
 
 			break;
 
 		default:
 			Log.e("service case default", "something is wrong");
 			break;
 
 		// used when get eq setting
 		case LOAD_EQ:
 			EqSetting eqSetting = null;
 			if (musicp != null) {
 				// from MusicPlayerActivity
 				eqSetting = setupEQ(musicp);
 			} else {
 				// from MusicListActivity
 				MediaPlayer temp = new MediaPlayer();
 				temp.setAudioStreamType(AudioManager.STREAM_MUSIC);
 				eqSetting = setupEQ(temp);
 				temp.release();
 				temp = null;
 			}
 			setEQ = true;
 			Bundle eqbundle = new Bundle();
 
 			eqbundle.putShortArray("eqlevel", bandslevels);
 			eqbundle.putShortArray("eqminmax", minMaxEQLevels);
 			eqbundle.putParcelable("eqSettings", eqSetting);
 			int volme = (int) currentVol;
 			eqbundle.putInt("volume", volme);
 			replyMessage(null, MusicPlayerService.LOAD_EQ, eqbundle, list);
 			break;
 
 		case SET_LOOP:
 			// TODO case SET_LOOP
 
 			loopState = bun.getInt("repeat"); // loopstate can be onesong,
 												// allsongs,or straight
 
 			if (musicp == null) {
 				Log.e("service", "music player is null");
 				return;
 			}
 
 			switch (loopState) {
 			case allsongs:
 				musicp.setLooping(false);
 				break;
 			case onesong:
 				musicp.setLooping(true);
 				break;
 			case straight:
 				musicp.setLooping(false);
 				break;
 			}
 
 			replyMessage(null, MusicPlayerService.SET_LOOP, null, player);
 			break;
 
 		case SET_EQ:
 			// used when setting eq
 
 			bandslevels = bun.getShortArray("seteq");
 
 			if (equalizer != null && musicp != null) {
 				short i = 0;
 				for (i = 0; i < bandslevels.length; i++) {
 					short temp = 0;
 					temp = (short) (bandslevels[i] - maxEQLevel);
 
 					Log.d("service", " set_EQ : " + temp);
 
 					equalizer.setBandLevel(i, temp);
 
 				}
 			} else {
 				short i = 0;
 				for (i = 0; i < bandslevels.length; i++) {
 					short temp = 0;
 					temp = (short) (bandslevels[i] - maxEQLevel);
 					Log.d("service", " set_EQ : " + temp);
 				}
 
 			}
 
 			break;
 		case INFO:
 			Log.d("service case:INFO", "INFO");
 
 			// TODO requests.size
 			if (mSong != null) {
 				Log.d("service", "playlistName: " + playlistName + " size: "
 						+ requests.size() + " song:" + mSong.getTitle());
 				b = new Bundle();
 				try {
 					b.putParcelable("music", mSong);
 					Log.d("service",
 							" currentSong: " + requests.get(currentIndex));
 				} catch (IndexOutOfBoundsException e) {
 					e.printStackTrace();
 				} finally {
 					replyMessage(null, MusicPlayerService.INFO, b, msg.replyTo);
 				}
 
 			}
 			break;
 
 		case SEEKBAR_SET:
 			Log.d("service", "SEEKBAR_SET");
 			b = msg.getData();
 			if (msg.obj == null) {
 				Log.e("service", "msg.obj is null");
 				return;
 			}
 			int pos = (Integer) msg.obj;
 			savedpos = pos * 1000;
 			if (state == State.play) {
 				savedpos = pos * 1000;
 				musicp.seekTo(savedpos);
 			} else if (state == State.completion || state == State.mnull
 					|| state == State.pause)
 				savedpos = pos * 1000;
 			break;
 		case PLAY:
 			Log.d("service", "play");
 			processPlayRequest();
 
 			Message mes = Message.obtain(null, MusicPlayerService.PLAY, null);
 			try {
 				if (player != null)
 					player.send(mes);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 
 			break;
 		case PAUSE:
 			Log.d("service", "pause");
 
 			try {
 				processPauseRequest();
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 			break;
 		case SKIP:
 			break;
 
 		case REWIND:
 			Log.d("service", "rewind");
 			processRewindRequest();
 
 			break;
 
 		case SET_ALLSONGS:
 			Log.d("service", "setallsongs");
 			mSongs = (ArrayList<Music>) msg.obj;
 
 			int i = 0;
 			while (i < mSongs.size()) {
 				Log.d("service 曲名", mSongs.get(i).toString());
 				i++;
 			}
 			break;
 
 		case SET_PLAY_REQUEST:
 			Log.d("service", "SET_PLAY_REQUEST");
 
 			b = msg.getData();
 			Music temp = (Music) b.getParcelable("music");
 			boolean dont_play = b.getBoolean("don't_play");
 			isUserRequest = b.getBoolean("userRequest");
 
 			if (b.getParcelableArrayList("all list") != null) {
 				requests = b.getParcelableArrayList("all list");
 				Log.d("service", "requests size:" + requests.size());
 			} else {
 				Log.e("service", "SETPLAYREQUEST requests null!!");
 			}
 
 			if (playlistName == null) {
 				playlistName = b.getString("currentPlaylist", null);
 
 				if (playlistName == null) {
 					Log.e("service", "currentplaylist null");
 				} else {
 					Log.d("service", "currentplaylist: " + playlistName
 							+ " music: " + temp.getTitle());
 				}
 
 			} else if (!playlistName.equals(b
 					.getString("currentPlaylist", null))) {
 
 				playlistName = b.getString("currentPlaylist", null);
 				if (playlistName == null) {
 					Log.e("service", "currentplaylist null");
 				} else {
 					Log.d("service", "currentplaylist: " + playlistName
 							+ " music: " + temp.getTitle());
 				}
 
 			}
 
 			int num = b.getInt("order");
 			currentIndex = num;
 
 			Log.d("service indexof", Integer.toString(currentIndex));
 
 			if (mSong != null) {
 				if (temp.getPlaylist_id() == mSong.getPlaylist_id()) {
 					Log.d("service", "same song");
 					Log.d("service", state.name());
					if (state == State.mnull && !dont_play) {
 						Log.d("service", "state = mnull???");
 						processPlayRequest();
 					}
 
 					else if (state == State.play) {
 						Log.d("service", "play request");
 					} else if (state == State.pause) {
 						Log.d("service", "pause request");
 					} else {
 						Log.d("service", "wakaranai");
 					}
 
 				} else {
 					Log.d("service", "new song");
 					mSong = temp;
 
 					Log.d("service Title:", mSong.getTitle());
 					Log.d("service Artist:", mSong.getArtist());
 					Log.d("service url:", mSong.getDatauri());
 
 					savedpos = 0;
 
 					if (musicp != null) {
 						if (state != State.mnull) {
 							// musicp.stop();
 							musicp.reset();
 						} else {
 							Log.d("service", "state:" + state);
 						}
 					}
					if (!dont_play ) {
 						processPlayRequest();
 					}
 				}
 
 			} else {
 				Log.d("service ppr", "new song ");
 				mSong = temp;
 				if (musicp != null) {
 					// musicp.stop();
 					musicp.reset();
 				}
 
 				savedpos = 0;
 				Log.d("service Title:", mSong.getTitle());
 				Log.d("service Artist:", mSong.getArtist());
 				Log.d("service url:", mSong.getDatauri());
 				Log.d("service all requests", Integer.toString(requests.size()));
				if (!dont_play) {
 					processPlayRequest();
 				}
 			}
 
 			if (dont_play) {
 				PlayerInfoHolder holdr = ((PlayerInfoHolder) getApplication());
 				holdr.selectedMusic = mSong;
 				holdr.selectedMusicNumber = currentIndex;
 				if (holdr.getShuffleState()) {
 					holdr.shufflePlaylistNumber = currentIndex;
 				}
 
 				SharedPreferences pushState = getSharedPreferences(
 						"randomBtn:pushState", Activity.MODE_PRIVATE);
 				SharedPreferences.Editor editor = pushState.edit();
 				editor.putString("lastPlaylistName", playlistName);
 				editor.putString("lastMusicUri", mSong.getDatauri());
 				editor.putLong("lastMusicOrder", currentIndex);
 				editor.commit();
 			}
 
 			// isUserRequest = false;
 
 			break;
 
 		case IS_PLAYING:
 			Log.d("service", "IS_PLAYING");
 			Message mes2 = null;
 
 			if (state == State.play) {
 				mes2 = Message
 						.obtain(null, MusicPlayerService.IS_PLAYING, true);
 				doTimerTask();
 			} else {
 				mes2 = Message.obtain(null, MusicPlayerService.IS_PLAYING,
 						false);
 				duration = (int) mSong.getDuration();
 			}
 
 			Bundle b2 = new Bundle();
 
 			b2.putParcelable("music", mSong);
 
 			b2.putInt("duration", duration / 1000);
 
 			b2.putInt("currentposition", savedpos / 1000);
 
 			b2.putParcelableArrayList("playlist", requests);
 
 			b2.putInt("loop", loopState);
 
 			if (playlistName != null) {
 				Log.d("service", "IS_PLAYING playlistName: " + playlistName);
 			} else {
 				Log.d("service", "IS_PLAYING no playlistName");
 			}
 			b2.putString("playlistName", playlistName);
 
 			b2.putInt("order", currentIndex);
 
 			mes2.setData(b2);
 
 			Log.d("service", " setData");
 			Log.d("service", " duration" + duration / 1000);
 			Log.d("service", " savedpos" + savedpos / 1000);
 			try {
 				if (player != null)
 					player.send(mes2);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 
 		}
 
 	}
 
 	static class IncomingHandler extends Handler {
 
 		private WeakReference<MusicPlayerService> ref;
 
 		public IncomingHandler(MusicPlayerService service) {
 			ref = new WeakReference<MusicPlayerService>(service);
 		}
 
 		public void handleMessage(Message msg) {
 			ref.get().handleMessageFromActivity(msg);
 		}
 	}
 
 	private ArrayList<Music> mSongs;
 	private Music mSong = null;
 
 	@Override
 	public void onCreate() {
 		Log.d("service", "onCreate");
 		super.onCreate();
 	}
 
 	@Override
 	public void onLowMemory() {
 		Log.d("service", "onLowMemory");
 
 		if (musicp != null) {
 			musicp.stop();
 			musicp.reset();
 			musicp.release();
 			musicp = null;
 		}
 
 		if (equalizer != null) {
 			equalizer.release();
 			equalizer = null;
 		}
 
 		super.onLowMemory();
 	}
 
 	@Override
 	public void onDestroy() {
 		Log.e("service onDestroy", "onDestroy");
 		System.out.println("ondestroy");
 		if (musicp != null) {
 			musicp.stop();
 			musicp.reset();
 			musicp.release();
 			musicp = null;
 		}
 
 		if (equalizer != null) {
 			equalizer.release();
 			equalizer = null;
 		}
 
 		try {
 			unregisterReceiver(mIntentReceiver);
 		} catch (IllegalArgumentException e) {
 			Log.e("Service", e.getMessage());
 		}
 
 		super.onDestroy();
 	}
 
 	private SharedPreferences mPref;
 
 	private EqSetting openPreferenceData() {
 		mPref = getSharedPreferences("rMuEqSettings", MODE_PRIVATE);
 		String eqSettings = mPref.getString("eqPrevSettings", null);
 
 		EqSetting settings = null;
 		if (eqSettings != null) {
 			ObjectMapper mapper = new ObjectMapper();
 			try {
 				settings = mapper.readValue(eqSettings, EqSetting.class);
 
 			} catch (Exception e) {
 				Log.e("service", "error opening eqData");
 			}
 		}
 		return settings;
 
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 
 		Log.d("service", "start service");
 		IntentFilter intentToReceiveFilter = new IntentFilter();
 		intentToReceiveFilter.addAction("android.media.AUDIO_BECOMING_NOISY");
 		this.registerReceiver(mIntentReceiver, intentToReceiveFilter, null,
 				null);
 
 		Notification notification = new Notification.Builder(
 				getApplicationContext())
 				.setContentTitle("QuicknessMusicPlayer")
 				.setContentText("player").setSmallIcon(R.drawable.ic_launcher)
 				.getNotification();
 
 		startForeground(1234, notification);
 
 		Log.d("service", "startforeground");
 
 		return START_STICKY;
 	}
 
 	private void processRewindRequest() {
 		Log.d("service", "processrewind");
 		Message mes = Message.obtain(null, MusicPlayerService.REWIND, null);
 		if (musicp != null) {
 
 			state = State.rewind;
 
 			if (musicp.isPlaying()) {
 				savedpos = 0;
 				if (musicp.getCurrentPosition() <= 1500) {
 
 					Log.d("service", "421 getcurrentPosition ");
 					if (currentIndex == 0) {
 						currentIndex = requests.size() - 1;
 					} else {
 						currentIndex--;
 					}
 					Log.d("service", "currentIndex" + currentIndex + ": "
 							+ requests.get(currentIndex).getTitle());
 
 					musicp.pause();
 					musicp.stop();
 					musicp.reset();
 
 					Bundle bundle = new Bundle();
 					bundle.putBoolean("prev", true);
 
 					bundle.putInt("order", currentIndex);
 					mes.setData(bundle);
 
 					processPlayRequest();
 				} else {
 
 					state = State.play;
 
 					doTimerTask();
 
 					musicp.seekTo(0);
 
 					Message smes = Message.obtain(null,
 							MusicPlayerService.SEEKBAR_PROGRESS, null);
 					Bundle b = new Bundle();
 					b.putInt("duration", duration / 1000);
 
 					b.putInt("currentposition", savedpos / 1000);
 					smes.setData(b);
 					try {
 						if (player != null)
 							player.send(smes);
 
 					} catch (RemoteException e) {
 						//
 						e.printStackTrace();
 					}
 				}
 
 				Log.d("service", "playing rewound");
 			} else {
 				if (savedpos == 0) {
 					if (currentIndex > 0)
 						currentIndex--;
 					else
 						currentIndex = requests.size() - 1;
 
 					Log.d("service", "currentPos == 0 && not playing");
 
 					Bundle bundle = new Bundle();
 					bundle.putBoolean("prev", true);
 					bundle.putInt("order", currentIndex);
 					mes.setData(bundle);
 
 					processPlayRequest();
 				} else {
 					Log.d("service", "currentPos > 0  && not playing");
 					savedpos = 0;
 					musicp.stop();
 					musicp.reset();
 
 					Message smes = Message.obtain(null,
 							MusicPlayerService.SEEKBAR_PROGRESS, null);
 					Bundle b = new Bundle();
 					b.putInt("duration", duration / 1000);
 
 					b.putInt("currentposition", savedpos / 1000);
 					smes.setData(b);
 					try {
 						if (player != null)
 							player.send(smes);
 					} catch (RemoteException e) {
 						e.printStackTrace();
 					}
 				}
 
 			}
 
 		} else {
 			bt = ButtonType.rewindInNullState; // to prevent player from
 												// starting music and
 			// stay it paused
 			savedpos = 0;
 			Message smes = Message.obtain(null,
 					MusicPlayerService.SEEKBAR_PROGRESS, null);
 			Bundle b = new Bundle();
 			b.putInt("duration", duration / 1000);
 
 			b.putInt("currentposition", savedpos / 1000);
 			smes.setData(b);
 			try {
 				if (player != null)
 					player.send(smes);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 			processPlayRequest();
 		}
 
 		try {
 			if (player != null)
 				player.send(mes);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private void processPauseRequest() throws RemoteException {
 		Log.d("service", "pause");
 
 		if (musicp != null) {
 			savedpos = musicp.getCurrentPosition();
 			musicp.pause();
 
 			state = State.pause;
 
 			Message mes = Message.obtain(null, MusicPlayerService.PAUSE, null);
 			if (player != null)
 				player.send(mes);
 
 			mes = Message.obtain(null, MusicPlayerService.SEEKBAR_PROGRESS,
 					null);
 			Bundle b = new Bundle();
 			b.putInt("duration", duration / 1000);
 
 			b.putInt("currentposition", savedpos / 1000);
 			mes.setData(b);
 			try {
 				if (player != null)
 					player.send(mes);
 			} catch (RemoteException e) {
 				//
 				e.printStackTrace();
 			}
 		} else {
 			Log.e("service", "musicp null!");
 		}
 
 	}
 
 	MediaPlayer musicp;
 
 	Boolean errorChk = true;
 	int reqType = 0;
 
 	/**
 	 * process used when playing or resume music
 	 */
 	private void processPlayRequest() { // From PlayerActivity
 
 		Log.d("service", "currentIndex  " + currentIndex);
 		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		// ask other players to stop music
 		int result = audioManager.requestAudioFocus(MusicPlayerService.this,
 				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
 
 		if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
 		} else {
 			// could not get audio focus.
 			Log.d("service", "could not get audio focus.");
 		}
 
 		if (musicp != null && savedpos != 0 && state != State.completion) {
 			Log.d("service ppr", "seekto");
 			musicp.start();
 			musicp.seekTo(savedpos);
 			state = State.play;
 			doTimerTask();
 			return;
 		} else {
 			Log.d("service ppr", " else");
 
 			if (musicp == null) {
 				Log.d("service", "new MediaPlayer()");
 				musicp = new MediaPlayer();
 				musicp.setAudioStreamType(AudioManager.STREAM_MUSIC);
 				musicp.setOnPreparedListener(MusicPlayerService.this);
 				musicp.setOnCompletionListener(MusicPlayerService.this);
 				musicp.setOnErrorListener(MusicPlayerService.this);
 				setupEQ(musicp);
 				musicp.setWakeMode(getApplicationContext(),
 						PowerManager.PARTIAL_WAKE_LOCK);
 				musicp.setVolume(MusicPlayerService.getVolume(currentVol),
 						MusicPlayerService.getVolume(currentVol));
 			}
 		}
 		if (loopState == onesong) {
 			musicp.setLooping(true);
 		}
 
 		mSong = requests.get(currentIndex);
 		Log.d("service song ", "song changed.");
 		uri = mSong.getDatauri();
 
 		musicp.reset();
 		try {
 			musicp.setDataSource(uri);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		musicp.prepareAsync();
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return mes.getBinder();
 	}
 
 	private int duration;
 
 	@Override
 	public void onPrepared(MediaPlayer arg0) {
 		Log.d("service", "prepared");
 		duration = arg0.getDuration();
 
 		if (savedpos != 0)// onresume or rewind
 			musicp.seekTo(savedpos);
 		if (bt == ButtonType.rewindInNullState) { // rewind in media player null
 													// state
 			Log.d("service", "rewind button pushed");
 			bt = ButtonType.play;
 			state = State.pause;
 
 			return;
 		}
 
 		Log.d("service", "onprepared");
 		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		int result = audioManager.requestAudioFocus(this,
 				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN); // オーディオフォーカスを要求する
 
 		if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
 			Log.d("service", "could not get audio focus.");
 		}
 
 		state = State.play;
 
 		start();
 
 	}
 
 	public void start() {
 
 		musicp.start();
 
 
 		if (savedpos != 0) {
 			musicp.seekTo(savedpos);
 		}
 
 		Log.d("service playing", currentIndex + " " + mSong.getTitle());
 
 		Log.d("service", "start");
 
 		state = State.play;
 
 		doTimerTask();
 
 		PlayerInfoHolder holdr = ((PlayerInfoHolder) getApplication());
 		holdr.selectedMusic = mSong;
 		holdr.selectedMusicNumber = currentIndex;
 		if (holdr.getShuffleState()) {
 			holdr.shufflePlaylistNumber = currentIndex;
 		}
 
 		SharedPreferences pushState = getSharedPreferences(
 				"randomBtn:pushState", Activity.MODE_PRIVATE);
 		SharedPreferences.Editor editor = pushState.edit();
 		editor.putString("lastPlaylistName", playlistName);
 		editor.putString("lastMusicUri", mSong.getDatauri());
 		editor.putLong("lastMusicOrder", currentIndex);
 		editor.commit();
 
 		try {
 			Bundle b = new Bundle();
 
 			Bundle pinfo = new Bundle();
 			pinfo.putParcelable("music", mSong);
 			pinfo.putInt("order", currentIndex);
 
 			if (player != null && !isUserRequest) {
 				replyMessage(null, MusicPlayerService.SET_PLAY_REQUEST, pinfo,
 						player);
 			} else {
 				isUserRequest = false;
 			}
 
 			if (list != null) {
 				b = new Bundle();
 				b.putParcelable("music", mSong);
 				replyMessage(null, MusicPlayerService.INFO, b, list);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void doTimerTask() {
 		if (timer != null) {
 			timer.cancel();
 			timer = null;
 		}
 		handler = new Handler();
 		timer = new Timer(false);
 		timer.schedule(new TimerTask() {
 
 			@Override
 			public void run() {
 				handler.post(new Runnable() {
 
 					@Override
 					public void run() {
 						if (player != null && musicp != null
 								&& state == State.play
 								&& musicp.getCurrentPosition() < duration) {
 							Message mes = Message.obtain(null,
 									MusicPlayerService.SEEKBAR_PROGRESS, null);
 							Bundle b = new Bundle();
 							// Log.d("service", "duration " + duration / 1000);
 							b.putInt("duration", duration / 1000);
 
 							b.putInt("currentposition",
 									musicp.getCurrentPosition() / 1000);
 							mes.setData(b);
 							try {
 								if (player != null) {
 									player.send(mes);
 								} else {
 									Log.d("service", "player null!!!");
 								}
 							} catch (RemoteException e) {
 								e.printStackTrace();
 							}
 						}
 					}
 				});
 
 			}
 		}, 0, 100);
 	}
 
 	// private PrevUpdateTask prevUpTask;
 
 	private Handler handler;
 
 	private Timer timer;
 
 	private short minEQLevel;
 
 	private short maxEQLevel;
 
 	private String uri;
 
 	@Override
 	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
 		Log.d("service", "onError : " + arg1);
 		if (arg1 == -38 || arg1 == 1) {
 			Log.e("service", "onError1");
 
 		} else {// unknown error
 			Log.d("service", "onError2");
 			state = State.pause;
 		}
 
 		if (currentIndex < requests.size() - 1) {
 			currentIndex++;
 			musicp.stop();
 			musicp.reset();
 			musicp.release();
 			musicp = null;
 			processPlayRequest(); // go to next song
 
 		} else if (currentIndex == requests.size() - 1) {
 			completionProcess();
 		}
 
 		return true;
 	}
 
 	@Override
 	public void onCompletion(MediaPlayer mp) {
 
 		Log.d("service", "onCompletion");
 		savedpos = 0;
 		musicp.stop();
 		musicp.reset();
 		if (currentIndex < requests.size() - 1) {
 
 			currentIndex++;
 			processPlayRequest();
 
 		} else if (currentIndex == requests.size() - 1) {
 			completionProcess();
 		} else {
 			Log.e("service", "something wrong");
 
 		}
 
 	}
 
 	private void completionProcess() {
 		Log.d("service", "onRelease all songs played");
 		Log.d("service", currentIndex + "<index    size >"
 				+ (requests.size() - 1));
 
 		currentIndex = 0;
 
 		if (loopState == allsongs) {
 			processPlayRequest();
 			return;
 		}
 
 		state = State.completion;
 		PlayerInfoHolder holdr = ((PlayerInfoHolder) getApplication());
 		holdr.selectedMusic = requests.get(currentIndex);
 		holdr.selectedMusicNumber = currentIndex;
 		if (holdr.getShuffleState()) {
 			holdr.shufflePlaylistNumber = currentIndex;
 		}
 
 		mSong = requests.get(0);
 		SharedPreferences pushState = getSharedPreferences(
 				"randomBtn:pushState", Activity.MODE_PRIVATE);
 		SharedPreferences.Editor editor = pushState.edit();
 		editor.putString("lastPlaylistName", playlistName);
 		editor.putString("lastMusicUri", mSong.getDatauri());
 		editor.putLong("lastMusicOrder", currentIndex);
 		editor.commit();
 
 		Message m = Message
 				.obtain(null, MusicPlayerService.ALLCOMPLETION, null);
 		try {
 			if (player == null)
 				Log.e("service ", "client null");
 			else if (m == null) {
 				Log.e("service", "m null");
 			} else {
 				Bundle b = new Bundle();
 				b.putParcelable("current_song", requests.get(0));
 				b.putInt("order", currentIndex);
 				Log.d("service", "duration " + requests.get(0).getDuration());
 
 				m.setData(b);
 				if (player != null)
 					player.send(m);
 			}
 
 			if (list != null) {
 				Bundle pos1 = new Bundle();
 				pos1.putParcelable("music", requests.get(currentIndex));
 				replyMessage(null, MusicPlayerService.INFO, pos1, list);
 			}
 
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public void onAudioFocusChange(int arg0) {
 		Log.d("service", "onAudioFocusChange");
 		switch (arg0) {
 		case AudioManager.AUDIOFOCUS_GAIN:
 			Log.d("service", "AUDIOFOCUS_GAIN");
 			if (musicp != null) {
 				musicp.setVolume(MusicPlayerService.getVolume(currentVol),
 						MusicPlayerService.getVolume(currentVol));
 				if (state == State.pause) {
 					return;
 				}
 				if (!musicp.isPlaying() && state == State.lossTransient) {
 					// from audiomanager
 					Log.d("service", "state : " + state.toString());
 					Log.d("service", "setVolume 1.0f");
 					start();
 					Message mes = Message.obtain(null,
 							MusicPlayerService.IS_PLAYING, true);
 					try {
 						if (player != null)
 							player.send(mes);
 					} catch (RemoteException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			break;
 		case AudioManager.AUDIOFOCUS_LOSS:
 			Log.d("service", "AUDIOFOCUS_LOSS");
 			if (musicp != null) {
 				if (state == State.play && musicp.isPlaying()) {
 					savedpos = musicp.getCurrentPosition();
 					musicp.stop();
 					musicp.reset();
 					Log.d("service", "LOSS");
 					state = State.completion;
 					Message mes = Message.obtain(null,
 							MusicPlayerService.IS_PLAYING, false);
 					try {
 						if (player != null)
 							player.send(mes);
 					} catch (RemoteException e) {
 						e.printStackTrace();
 					}
 				}
 			} else {
 			}
 			break;
 		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
 			// Lost focus for a short time, but we have to stop
 			// playback. We don't release the media player because playback
 			// is likely to resume
 			Log.d("service", "AUDIOFOCUS_LOSS_TRANSIENT");
 			if (musicp != null) {
 				if (state == State.play && musicp.isPlaying()) {
 					musicp.pause();
 					state = State.lossTransient;
 					Log.d("service", "pause music for loss transient ");
 				} else {
 					Log.d("service", "is not playing at loss transient");
 				}
 				Message mes = Message.obtain(null,
 						MusicPlayerService.IS_PLAYING, false);
 				try {
 					if (player != null)
 						player.send(mes);
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 			}
 
 			break;
 
 		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
 			// Lost focus for a short time, but it's ok to keep playing
 			// at an attenuated level
 			Log.d("service", "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
 			if (musicp != null) {
 				if (state == State.play && musicp.isPlaying())
 					musicp.setVolume(0.1f, 0.1f);
 			}
 			break;
 		}
 	}
 
 }
