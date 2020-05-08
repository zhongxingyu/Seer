 package com.saturdaycoder.easydoubanfm.player;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.TimerTask;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import com.saturdaycoder.easydoubanfm.Database;
 import com.saturdaycoder.easydoubanfm.Debugger;
 import com.saturdaycoder.easydoubanfm.EasyDoubanFm;
 import com.saturdaycoder.easydoubanfm.EasyDoubanFmWidget;
 import com.saturdaycoder.easydoubanfm.Global;
 import com.saturdaycoder.easydoubanfm.Preference;
 import com.saturdaycoder.easydoubanfm.R;
 import com.saturdaycoder.easydoubanfm.Utility;
 import com.saturdaycoder.easydoubanfm.WidgetContent;
 import com.saturdaycoder.easydoubanfm.R.drawable;
 import com.saturdaycoder.easydoubanfm.R.string;
 import com.saturdaycoder.easydoubanfm.apis.DoubanFmApi;
 import com.saturdaycoder.easydoubanfm.channels.FmChannel;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.os.Message;
 import android.widget.Toast;
 import java.util.concurrent.Semaphore;
 
 
 public class DoubanFmPlayer implements IHttpFetcherObserver, IPlayListObserver {
 	private static final int MAX_HISTORY_COUNT = 20;
 	private static final int MAX_PENDING_COUNT = 5;
 	
 	private static final int NO_REASON = -1;
 	
 	private boolean isOpen = false;
 	private Context context = null;
 	private Database db = null;
	//private ArrayList<MusicInfo> musicHistory = new ArrayList<MusicInfo>();
	//private ArrayList<MusicInfo> pendingMusicList = new ArrayList<MusicInfo>();
 	
 	private MusicInfo curMusic = null;
	//private MusicInfo getCurrentMusic() {
		
	//}
	
	//private MusicInfo getLastMusic() {
		
	//}
 	private MusicInfo lastMusic = null;
 	
 	private boolean isPreparing = false;
 	private final Object musicSessionLock = new Object();
 	
 	private DoubanPlayListManager playListManager = new DoubanPlayListManager(MAX_PENDING_COUNT, MAX_HISTORY_COUNT);
 	
 	private Bitmap curPic = null;
	//private GetPictureTask picTask = null;
 	
 	private char lastStopReason = DoubanFmApi.TYPE_NEW;
 	private final Object stopReasonLock = new Object();
 	
 	private LoginSession loginSession = null;
 	private final Object loginLock = new Object();
 	
 	private MediaPlayer mPlayer;
 	private boolean playerReset = false;
 	
 	private final Object channelTableLock = new Object();
 	
 	
 	public DoubanFmPlayer(Context context, Database db) {
 		this.isOpen = false;
 		this.context = context;
 		this.db = db;
 		//this.picTask = new GetPictureTask();
 		
 		synchronized(channelTableLock) {
 			if (db.getChannels() == null || db.getChannels().length < 1) {
 				for(FmChannel fc: FmChannel.PreinstalledChannels) {
 					db.saveChannel(fc);
 				}
 			}
 		}
 		new AsyncChannelTableUpdater().execute();
 		
 	}
 	
 	public boolean isOpen() {
 		return isOpen;
 	}
 	
 	public boolean isPlaying() {
 		if (mPlayer == null)
 			return false;
 		else 
 			return mPlayer.isPlaying();
 	}
 	
 	public boolean isPreparing() {
 		return isPreparing;
 	}
 	
 	public MusicInfo getCurMusic() {
 		return curMusic;
 	}
 	
 	public Bitmap getCurPic() {
 		return curPic;
 	}
 	
 	public void open() {
 		synchronized(loginLock) {
 			this.loginSession = null;
 		}
 		
 		playListManager.registerObserver(this);
 		HttpFetcher.getInstance().registerObserver(this);
 		try {
 			notifyPowerStateChanged(Global.STATE_PREPARE);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		if (!isOpen) {
 			synchronized(this) {
 				if (!isOpen) {
 					if (mPlayer != null) {
 						mPlayer.reset();
 						mPlayer.release();
 						mPlayer = null;
 					}
 					
 					mPlayer = new MediaPlayer();
 					mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
 					mPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
 						@Override
 						public void onBufferingUpdate(MediaPlayer mp, int percent) {
 							isPreparing = false;
 							
 						}
 					});
 					mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
 						@Override
 						public boolean onError(MediaPlayer mp, int what, int extra) {
 							synchronized(mPlayer) {
 								isPreparing = false;
 								//reportState(PlayState.ERROR);
 								Debugger.error("media player onError: what=" + what + " extra=" + extra);
 								mPlayer.reset();
 								//nextMusic();
 							    return true;
 							}
 						}
 					});
 					mPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
 						
 						@Override
 						public boolean onInfo(MediaPlayer mp, int what, int extra) {
 							Debugger.warn("media player onInfo: what=" + what + " extra=" + extra);
 							return true;
 						}
 					});
 					mPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
 						
 						@Override
 						public void onSeekComplete(MediaPlayer mp) {
 							isPreparing = false;
 							Debugger.info("media player onSeekComplete");
 							//EasyDoubanFm.updatePosition(getCurPosition(), getCurDuration());
 						}
 					});
 					mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
 						
 						@Override
 						public void onPrepared(MediaPlayer mp) {
 							synchronized(mPlayer) {
 								isPreparing = false;
 								Debugger.info("media player onPrepare");
 								try {
 									mPlayer.seekTo(0);
 									mPlayer.start();
 									notifyMusicPosition(mPlayer.getCurrentPosition(), 
 											mPlayer.getDuration());
 								} catch (Exception e) {
 									e.printStackTrace();
 								}
 							}
 						}
 					});
 					mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
 						@Override
 						public void onCompletion(MediaPlayer mp) {
 							isPreparing = false;
 							Debugger.info("media player onCompletion");
 							//reportState(PlayState.COMPLETED);
 							nextMusic();
 						}
 					});
 			        
 					
 			        //musicHistory = new ArrayList<MusicInfo>();
 			        //pendingMusicList = new ArrayList<MusicInfo>();	
 
 
 					// check login
 					boolean login = Preference.getLogin(context);
 					if (login) {
 						String email = Preference.getAccountEmail(context);
 						String passwd = Preference.getAccountPasswd(context);
 						login(email, passwd);
 					}
 					else {
 						try {
 							notifyLoginStateChanged(Global.STATE_IDLE, NO_REASON);
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 						
 						loginSession = null;
 					}
 
 
 		
 					// get the currently selected channel
 					int ci = Preference.getSelectedChannel(context);
 										
 					// if channel not exist, or it need login but not logged in
 					// find the first public channel to select
 					if (!db.isChannelIdValid(ci)
 							|| (FmChannel.channelNeedLogin(ci) && loginSession == null)) {
 						// selected channel not exist
 						ci = db.getFirstPublicChannel();
 						
 						Preference.selectChannel(context, ci);
 						
 					}
 					
 					playListManager.setChannel(ci);
 					FmChannel c = db.getChannelInfo(ci);
 					
 					try {
 						notifyPowerStateChanged( Global.STATE_STARTED);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					
 					try {
 						notifyChannelChanged(c.channelId, c.getDisplayName(login));
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					
 					isOpen = true;
 					
 					nextMusic();
 
 				}
 			}
 		}
 	}
 	
 	public void close() {
 		synchronized(loginLock) {
 			this.loginSession = null;
 		}
 		
 		playListManager.unregisterObserver(this);
 		HttpFetcher.getInstance().unregisterObserver(this);
 		
 		if (isOpen) {
 			synchronized(this) {
 				if (isOpen) {
 					isPreparing = false;
 					
 					mPlayer.reset();
 					mPlayer.release();
 					mPlayer = null;
 					
 					
 					Debugger.info("DoubanFmService closeFM");
 					
 					//notifyChannelChanged(-1, context.getResources().getString(R.string.text_channel_closing));
 					try {
 						notifyPowerStateChanged(Global.STATE_PREPARE);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 
 					curMusic = lastMusic = null;
 					curPic = null;
 					
 					/*if (picTask != null) {
 						picTask.cancel(true);
 						picTask = null;
 					}*/
 					
 					try {
 						notifyPowerStateChanged( Global.STATE_IDLE);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					isOpen = false;
 				}
 			}
 		}
 		
 	}
 	
 	public void skipMusic() {
 		// according to Douban API, should send a "type=s" report.
 		// but considering efficiency, I just pick one new music from pending list
 		nextMusic();
 	}
 	
 	public void pauseMusic() {
 		try {
 			mPlayer.pause();
 			
 		} catch (IllegalStateException e) {
 			mPlayer.reset();
 		}
 		Debugger.debug("player playing is " + mPlayer.isPlaying());
 		try {
 			notifyMusicPaused();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void resumeMusic() {
 		try {
 			mPlayer.start();
 			notifyMusicResumed();
 		} catch (IllegalStateException e) {
 			mPlayer.reset();
 			nextMusic();
 		}
 		
 	}
 	
 	public void playPauseMusic() {
 		if (mPlayer.isPlaying()) {
 			Debugger.debug("player switched to PAUSE state");
 			pauseMusic();
 		} else {
 			if (isPreparing) {
 				Debugger.warn("player is preparing");
 				return;
 			}
 			Debugger.debug("player switched to RESUME state");
 			resumeMusic();
 		}
 	}
 	
 	public void rateMusic() {
 		Debugger.verbose("Player.rateMusic");
 		new AsyncMusicRater(true).execute();
 	}
 	
 	public void unrateMusic() {
 		Debugger.verbose("Player.unrateMusic");
 		new AsyncMusicRater(false).execute();
 	}
 	
 	public boolean isMusicRated() {
 		synchronized(musicSessionLock) {
 			if (curMusic != null)
 				return curMusic.isRated();
 			else return false;
 		}
 	}
 	
 	public void banMusic() {
 		new AsyncMusicBanner().execute();
 	}
 	
 	public void selectChannel(int id) {
 		Debugger.info("SELECT CHANNEL to " + id);
 		FmChannel chan = db.getChannelInfo(id);
 		if (chan == null) {
 			Debugger.error("#### channel user selected is invalid");
 			id = 0;
 			chan = db.getChannelInfo(id);
 			Preference.selectChannel(context, id);
 		}
 		else {
 			Preference.selectChannel(context, id);
 		}
 		
 		try {
 			notifyChannelChanged(chan.channelId, chan.name);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		playListManager.setChannel(id);
 		playListManager.clearList();
 
 		lastStopReason = DoubanFmApi.TYPE_NEW;
 
 		if (isOpen()) {
 			nextMusic();
 		}
 		
 	}
 	
 	public void forwardChannel() {
 		
 		int chanId = Preference.getSelectedChannel(context);
 		int idx = db.getChannelIndex(chanId);
 		FmChannel chan = null;
 		
 		synchronized(channelTableLock) {
 			while(true) {
 				idx = (idx + 1) % FmChannel.PreinstalledChannels.length;
 				chan = FmChannel.PreinstalledChannels[idx];
 				if (chan == null)
 					return;
 				if (FmChannel.channelNeedLogin(chan.channelId)){
 					Debugger.debug("forward channel needs login, loginSession = "
 							+ ((loginSession == null)? "y": "n"));
 					if (loginSession == null) {
 						
 						continue;
 					}
 					else {
 						break;
 					}
 				} else {
 					break;
 				}
 			} 
 		}
 		Debugger.debug("forwarded to channel " + chan.channelId);
 		selectChannel(chan.channelId);
 	}
 	
 	public void backwardChannel() {
 		
 	}
 	
 	public void randomChannel() {
 		
 	}
 	
 	public void notifyFullStatus() {
 		WidgetContent wc = EasyDoubanFmWidget.getContent(context);
 		
 		notifyPowerStateChanged(wc.onState);
 		
 		switch(wc.onState) {
 		case Global.STATE_IDLE:
 			break;
 		case Global.STATE_PREPARE:
 			notifyMusicStateChanged(Global.STATE_PREPARE, null, Global.NO_REASON);
 			break;
 		case Global.STATE_STARTED:
 			if (curMusic != null) {
 				int chanid = Preference.getSelectedChannel(context);
 				notifyChannelChanged(chanid, 
 						db.getChannelInfo(chanid).name);
 				
 				if (!wc.paused)
 					notifyMusicStateChanged(Global.STATE_MUSIC_RESUMED, curMusic, Global.NO_REASON);
 				else
 					notifyMusicStateChanged(Global.STATE_MUSIC_PAUSED, curMusic, Global.NO_REASON);
 				//if (wc.picture != null) {
 				notifyPictureStateChanged(Global.STATE_FINISHED, curMusic.pictureUrl, Global.NO_REASON);
 				//}
 			}
 			//if (wc.paused) {
 			//	notifyMusicPaused();
 			//}
 			break;
 		default:
 			break;
 		}
 	}
 	
 	public boolean login(String username, String passwd) {
 		if (username == null || username.equals("") 
 				|| passwd == null || passwd.equals("")) {
 			
 			try {
 				notifyLoginStateChanged(Global.STATE_ERROR, Global.REASON_API_REQUEST_ERROR);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 			return false;
 		}
 			
 		synchronized(loginLock) {
 			try {
 				try {
 					notifyLoginStateChanged(Global.STATE_PREPARE, NO_REASON);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				
 				loginSession = DoubanFmApi.login(username, passwd, Utility.getClientVersion());
 				if (loginSession != null) {
 					Preference.setAccountEmail(context, username);
 					Preference.setAccountPasswd(context, passwd);
 					Preference.setLogin(context, true);
 					
 					try {
 						playListManager.setLoginSession(loginSession);
 						playListManager.setChannel(Preference.getSelectedChannel(context));
 						notifyLoginStateChanged(Global.STATE_STARTED, NO_REASON);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					
 					return true;
 				} else {
 					Preference.setAccountEmail(context, username);
 					Preference.setAccountPasswd(context, null);
 					Preference.setLogin(context, false);
 					
 					try {
 						notifyLoginStateChanged(Global.STATE_ERROR, Global.REASON_API_REQUEST_ERROR);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					return false;
 				}
 				
 				
 			} catch (Exception e) {
 				Debugger.error("IO ERROR loging in: " + e.toString());
 				
 				try {
 					notifyLoginStateChanged(Global.STATE_ERROR, 
 												Global.REASON_NETWORK_IO_ERROR);
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 				loginSession = null;
 				return false;
 			}
 		}
 		//return (session != null);		
 	}
 	
 	public void logout() {
 		synchronized(loginLock) {
 			Preference.setLogin(context, false);
 			loginSession = null;
 			playListManager.setLoginSession(null);
 			playListManager.setChannel(Preference.getSelectedChannel(context));
 			try {
 				notifyLoginStateChanged(Global.STATE_IDLE, NO_REASON);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private void notifyMusicPosition(int pos, int dur) {
 
 		Intent i = new Intent(Global.EVENT_PLAYER_MUSIC_POSITION);
 		i.putExtra(Global.EXTRA_MUSIC_POSITION, pos);
 		i.putExtra(Global.EXTRA_MUSIC_DURATION, dur);
 		context.sendBroadcast(i);
 	}
 	
 	private void notifyMusicPrepareProgress(int progress) {
 		EasyDoubanFmWidget.setPrepareProgress(context, progress);
 	}
 	
 	private void notifyPowerStateChanged(int powerState) {
 		WidgetContent wc = EasyDoubanFmWidget.getContent(context);
 		
 		switch(powerState) {
 		case Global.STATE_PREPARE:
 			wc.onState = powerState;
 			//wc.onState = ;
 			EasyDoubanFmWidget.updateContent(context, wc, null);
 			break;
 		case Global.STATE_IDLE:
 			//wc.onState = EasyDoubanFmWidget.STATE_OFF;
 			wc.onState = powerState;
 			wc.channel = context.getResources().getString(R.string.text_channel_unselected);
 			wc.rated = false;
 			wc.artist = wc.title = "";
 			wc.picture = null;
 			EasyDoubanFmWidget.updateContent(context, wc, null);
 			break;
 		case Global.STATE_STARTED:
 			//wc.onState = EasyDoubanFmWidget.STATE_ON;
 			wc.onState = powerState;
 			EasyDoubanFmWidget.updateContent(context, wc, null);
 			break;
 		default:
 			break;
 		}
 		
 		Intent i = new Intent(Global.EVENT_PLAYER_POWER_STATE_CHANGED);
 		i.putExtra(Global.EXTRA_STATE, powerState);
 		context.sendBroadcast(i);
 	}
 	
 	private void notifyMusicStateChanged(int musicState, MusicInfo musicInfo, int reason) {
 		WidgetContent content = EasyDoubanFmWidget.getContent(context);
 		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 		
 		Intent intent = new Intent(Global.EVENT_PLAYER_MUSIC_STATE_CHANGED);
 		intent.putExtra(Global.EXTRA_STATE, musicState);
 		
 		switch(musicState) {
 		case Global.STATE_PREPARE:
 			content.artist = content.title = "";
 			content.rated = false;
 			EasyDoubanFmWidget.updateContent(context, content, null);
 			//EasyDoubanFm.updateContents(content);
 			//EasyDoubanFm.updatePosition(0, 0);
 			break;
 		case Global.STATE_STARTED:	 {
 			if (musicInfo != null) {
 				content.artist = musicInfo.artist;
 				content.rated = musicInfo.isRated();
 				content.title = musicInfo.title;
 				content.paused = false;
 			}
 			EasyDoubanFmWidget.updateContent(context, content, null);
 			//EasyDoubanFm.updateContents(content);
 			intent.putExtra(Global.EXTRA_MUSIC_ARTIST, musicInfo.artist);
 			intent.putExtra(Global.EXTRA_MUSIC_ISRATED, musicInfo.isRated());
 			intent.putExtra(Global.EXTRA_MUSIC_TITLE, musicInfo.title);
 			
 			Notification fgNotification = new Notification(R.drawable.icon,
 					content.artist + " -- " + content.title,
 			        System.currentTimeMillis());
 			Intent it = new Intent(context, EasyDoubanFm.class);
 			PendingIntent pi = PendingIntent.getActivity(context, 0, it, 0);
 			fgNotification.setLatestEventInfo(context, content.artist, content.title, pi);	
 			notificationManager.notify(Global.NOTIFICATION_ID_PLAYER, fgNotification);
 			break;
 		}
 		case Global.STATE_FINISHED: {
 			content.artist = content.title = "";
 			content.rated = false;
 			content.picture = null;//BitmapFactory.decodeResource(context.getResources(), R.drawable.default_album);
 			EasyDoubanFmWidget.updateContent(context, content, null);
 			//EasyDoubanFm.updateContents(content);
 			break;
 		}
 		
 		case Global.STATE_CANCELLED:
 			break;
 		case Global.STATE_IDLE:
 			break;
 		case Global.STATE_FAILED:
 		case Global.STATE_ERROR:
 			popNotify("无法获得音乐信息，这可能是网络问题或是服务器端问题。请检查网络，或稍后再试");
 			break;		
 		case Global.STATE_MUSIC_SKIPPED:
 			break;
 		case Global.STATE_MUSIC_PAUSED:
 			content.paused = true;
 			intent.putExtra(Global.EXTRA_MUSIC_ARTIST, musicInfo.artist);
 			intent.putExtra(Global.EXTRA_MUSIC_ISRATED, musicInfo.isRated());
 			intent.putExtra(Global.EXTRA_MUSIC_TITLE, musicInfo.title);
 			
 			EasyDoubanFmWidget.updateContent(context, content, null);
 			//EasyDoubanFm.updateContents(content);
 			break;
 		case Global.STATE_MUSIC_RESUMED:
 			content.paused = false;
 			intent.putExtra(Global.EXTRA_MUSIC_ARTIST, musicInfo.artist);
 			intent.putExtra(Global.EXTRA_MUSIC_ISRATED, musicInfo.isRated());
 			intent.putExtra(Global.EXTRA_MUSIC_TITLE, musicInfo.title);
 			
 			EasyDoubanFmWidget.updateContent(context, content, null);
 			//EasyDoubanFm.updateContents(content);
 			break;
 		default:
 			break;
 		}
 		
 		context.sendBroadcast(intent);
 	}
 	
 	/*private void notifyPictureStateChanged(int picState, Bitmap pic, int reason) {
 		WidgetContent content = EasyDoubanFmWidget.getContent(context);
 		switch (picState) {
 		case Global.STATE_ERROR:
 		case Global.STATE_CANCELLED:
 		case Global.STATE_IDLE:
 			//break;
 		case Global.STATE_PREPARE:
 			//content.picture = defaultAlbumPic;
 			//EasyDoubanFmWidget.updateContent(context, content, null);
 			break;
 		case Global.STATE_STARTED:
 		case Global.STATE_FINISHED:
 			if (pic != null) {
 				content.picture = pic;
 				
 				Intent i = new Intent(Global.EVENT_PLAYER_PICTURE_STATE_CHANGED);
 				i.putExtra(Global.EXTRA_STATE, Global.STATE_FINISHED);
 				//i.putExtra(Global.EXTRA_PICTURE, pic.getNinePatchChunk());
 				//i.putExtra(Global.EXTRA_PICTURE_URL, );
 				context.sendBroadcast(i);				
 				
 				EasyDoubanFmWidget.updateContent(context, content, null);
 				//EasyDoubanFm.updateContents(content);
 			}
 			break;
 		default:
 		}
 		
 		
 	}*/
 	
 	private void notifyPictureStateChanged(int state, String picurl, int reason) {
 		//WidgetContent content = EasyDoubanFmWidget.getContent(context);
 		switch (state) {
 		case Global.STATE_ERROR:
 		case Global.STATE_CANCELLED:
 		case Global.STATE_IDLE:
 			//break;
 		case Global.STATE_PREPARE:
 			//content.picture = defaultAlbumPic;
 			//EasyDoubanFmWidget.updateContent(context, content, null);
 			break;
 		case Global.STATE_STARTED:
 		case Global.STATE_FINISHED:
 			if (HttpFetcher.getInstance().finishedKeySet().contains(picurl)) {
 				byte[] bytes = HttpFetcher.getInstance().getContent(picurl);
 				
 				// decode bitmap and update widgets
 				Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
 				WidgetContent content = EasyDoubanFmWidget.getContent(context);
 				content.picture = bmp;
 				EasyDoubanFmWidget.updateContent(context, content, null);
 				
 				// send broadcast to notify activity
 				Intent i = new Intent(Global.EVENT_PLAYER_PICTURE_STATE_CHANGED);
 				i.putExtra(Global.EXTRA_STATE, Global.STATE_FINISHED);
 				i.putExtra(Global.EXTRA_PICTURE_URL, picurl);
 				context.sendBroadcast(i);
 			}
 			break;
 		default:
 		}
 	}
 
 	private void notifyMusicProgress(int progress) {
 		
 	}
 	
 	private void notifyChannelChanged(int chanId, String chanName) {
 		WidgetContent content = EasyDoubanFmWidget.getContent(context);
 		content.channel = chanName;
 		EasyDoubanFmWidget.updateContent(context, content, null);
 		//EasyDoubanFm.updateContents(content);
 		
 		Intent intent = new Intent(Global.EVENT_CHANNEL_CHANGED);
 		intent.putExtra(Global.EXTRA_CHANNEL, chanName);
 		context.sendBroadcast(intent);
 	}
 	
 	private void notifyLoginStateChanged(int loginState, int reason) {
 		WidgetContent content = EasyDoubanFmWidget.getContent(context);
 		switch (loginState) {
 		case Global.STATE_ERROR:
 		case Global.STATE_IDLE:
 			
 			break;
 		case Global.STATE_PREPARE:
 			//content.channel = context.getResources().getString(R.string.text_login_inprocess);
 			break;
 		case Global.STATE_STARTED:
 		case Global.STATE_FINISHED:
 			
 			break;
 		}
 		//EasyDoubanFmWidget.updateContent(context, content, null);
 		//EasyDoubanFm.updateContents(content);
 	}
 	
 	private void notifyMusicRated(boolean rated) {
 		WidgetContent content = EasyDoubanFmWidget.getContent(context);
 		content.rated = rated;
 		EasyDoubanFmWidget.updateContent(context, content, null);
 		//EasyDoubanFm.updateContents(content);
 		Intent i;
 		if (rated) {
 			i = new Intent(Global.EVENT_PLAYER_MUSIC_RATED);
 		} else {
 			i = new Intent(Global.EVENT_PLAYER_MUSIC_UNRATED);
 		}
 		context.sendBroadcast(i);
 		
 	}
 	
 	private void notifyMusicPaused() {
 		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 		notificationManager.cancel(Global.NOTIFICATION_ID_PLAYER);
 		WidgetContent content = EasyDoubanFmWidget.getContent(context);
 		content.paused = true;
 		EasyDoubanFmWidget.updateContent(context, content, null);
 		//EasyDoubanFm.updateContents(content);
 		
 		Intent i = new Intent(Global.EVENT_PLAYER_MUSIC_STATE_CHANGED);
 		i.putExtra(Global.EXTRA_STATE, Global.STATE_MUSIC_PAUSED);
 		context.sendBroadcast(i);
 	}
 	
 	private void notifyMusicResumed() {
 		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 		if (curMusic != null) {
 			Notification fgNotification = new Notification(R.drawable.icon,
 					curMusic.artist + " -- " + curMusic.title,
 			        System.currentTimeMillis());
 			fgNotification.flags |= Notification.FLAG_NO_CLEAR;
 			Intent it = new Intent(context, EasyDoubanFm.class);
 			PendingIntent pi = PendingIntent.getActivity(context, 0, it, 0);
 			fgNotification.setLatestEventInfo(context, curMusic.artist, curMusic.title, pi);	
 			notificationManager.notify(Global.NOTIFICATION_ID_PLAYER, fgNotification);
 		}
 		WidgetContent content = EasyDoubanFmWidget.getContent(context);
 		content.paused = false;
 		EasyDoubanFmWidget.updateContent(context, content, null);
 		//EasyDoubanFm.updateContents(content);
 		
 		Intent i = new Intent(Global.EVENT_PLAYER_MUSIC_STATE_CHANGED);
 		i.putExtra(Global.EXTRA_STATE, Global.STATE_MUSIC_RESUMED);
 		context.sendBroadcast(i);
 	}
 	
 	private void notifyMusicBanned() {
 		
 	}
 	
 	private void nextMusic() {
 
 		AsyncMusicForwarder nextMusicTask = new AsyncMusicForwarder();
 		Debugger.verbose("starting asyncNextMusicTask");
 		nextMusicTask.execute();
 	}
     
 	/*private void fillPendingList() throws IOException {
 		String[] historySids = null;
 		if (musicHistory.size() > 0) {
 			historySids = new String[musicHistory.size()];
 			for (int i = 0; i < historySids.length; ++i)
 				historySids[i] = musicHistory.get(i).sid;
 		}
 		MusicInfo[] musics = null;
 		int channel = Preference.getSelectedChannel(context);
 		musics = DoubanFmApi.report(loginSession, channel, 
 					((curMusic != null)? curMusic.sid: ""), lastStopReason, historySids);
 
 		if (musics == null || musics.length == 0) {
 			Debugger.error("musics == 0");
 			//popNotify(getResources().getString(R.string.err_get_songs));
 			return;
 		}
 		for (MusicInfo i: musics) {
 			if (pendingMusicList.size() >= MAX_PENDING_COUNT) {
 				break;
 			} else {
 				pendingMusicList.add(i);
 				Debugger.debug("pending list size is " + pendingMusicList.size());
 			}
 		}
 		
 		
 	}*/
 	
 	public int getCurPosition() {
 		if (isOpen() && mPlayer != null && mPlayer.isPlaying()) {
 			return mPlayer.getCurrentPosition();
 		}
 		
 		else return 0;
 	}
 	
 	public int getCurDuration() {
 		if (isOpen() && mPlayer != null && mPlayer.isPlaying()) {
 			return mPlayer.getDuration();
 		}
 		
 		else return 0;
 	}
 	
 	private MusicInfo getNextMusic() {
 		return playListManager.getNext();//forward(DoubanFmApi.TYPE_NEW);
 	}
 	
 	private class AsyncChannelSwitcher extends AsyncTask<Integer, Integer, Integer> {
 
 		@Override
 		protected Integer doInBackground(Integer... params) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 		
 	}
 	
 	private class AsyncMusicRater extends AsyncTask<Integer, Integer, Integer> {
 		private boolean rate;
 		public AsyncMusicRater(boolean rate) {
 			this.rate = rate;
 			
 		}
 		@Override
 		protected void onPreExecute() {
 			notifyMusicRated(rate);
 			
 		}
 		@Override
 		protected void onPostExecute(Integer result) {
 			if (result != 0) {
 				notifyMusicRated(!rate);
 				curMusic.rate(!rate);
 				popNotify("无法标记喜爱歌曲");
 			}
 		}
 		@Override
 		protected Integer doInBackground(Integer... params) {
 			//synchronized(musicSessionLock) {
 				if (curMusic == null){ 
 					Debugger.warn("no music is playing");
 					return 0;			
 				}
 				
 				if (curMusic.isRated() == rate) {
 					Debugger.warn("no need to do anything");
 					return 0;
 				}
 			
 				curMusic.rate(rate);
 				playListManager.clearList();
 				
 				//synchronized(stopReasonLock) {
 				lastStopReason = rate? DoubanFmApi.TYPE_RATE: DoubanFmApi.TYPE_UNRATE;
 				playListManager.requestList(lastStopReason);
 				lastStopReason = DoubanFmApi.TYPE_NEW;
 				//}
 			//}
 			return 0;
 		}
 		
 	}
 	
 	private class AsyncMusicBanner extends AsyncTask<Integer, Integer, Integer> {
 		@Override
 		protected void onPostExecute(Integer i) {
 			if (i == 0) {
 				notifyMusicBanned();
 				lastStopReason = DoubanFmApi.TYPE_NEW;
 				nextMusic();
 			}
 		}
 		@Override
 		protected Integer doInBackground(Integer... params) {
 	    	if (curMusic == null) {
 	    		return -1;
 	    	}
 	    	
 	    	synchronized(stopReasonLock) {
 				lastStopReason = DoubanFmApi.TYPE_BYE;
 				//pendingMusicList.clear();
 				try {
 					//fillPendingList();
 					playListManager.clearList();
 					playListManager.requestList(lastStopReason);
 					
 
 					
 				} catch (Exception e) {
 					Debugger.error("error in banMusic: " + e.toString());
 				}
 				
 	    	}
 			
 			return 0;
 		}
 		
 	}
 	
 	private class AsyncChannelTableUpdater extends AsyncTask<Integer, Integer, Integer> {
 
 		@Override
 		protected Integer doInBackground(Integer... arg0) {
 			
 			try {
 				FmChannel[] newChannels = DoubanFmApi.getChannelTable();
 				if (newChannels.length > 0) {
 					synchronized(channelTableLock) {
 						db.clearChannels();
 						for (FmChannel dfc: newChannels)
 							db.saveChannel(dfc);
 					}
 				}
 				return 0;
 			} catch (Exception e) {
 				return -1;
 			}
 			
 		}
 		
 	}
 	
 	private class AsyncMusicForwarder extends AsyncTask<String, Integer, Integer> {
 		@Override
 		protected void onPreExecute() {
 			//synchronized(mPlayer) {
 			mPlayer.reset();
 			notifyMusicPosition(-1, -1);
 				//playerReset = true;
 			//}
 			notifyMusicStateChanged( Global.STATE_PREPARE, null, NO_REASON);			
 		}
 		@Override
         protected void onPostExecute(Integer errorcode) {
 			Debugger.verbose("AsyncNextMusicTask.onPostExecute(" + errorcode + ")");
 			if (errorcode != 0) {
 				notifyMusicStateChanged( Global.STATE_ERROR, null, 
 						errorcode);
 				return;
 			}
 			// mPlayer prepare
 			isPreparing = true;
 			
 			try {
 				mPlayer.setDataSource(curMusic.musicUrl);
 				mPlayer.prepareAsync();
 				// report music info (artist, title, rated)
 		    	notifyMusicStateChanged( Global.STATE_STARTED, curMusic, NO_REASON);	    	
 				
 		    	//picTask.execute(curMusic.pictureUrl);
 				HttpFetcher.getInstance().fetch(curMusic.pictureUrl);
 			} catch (IOException e) {
 				Debugger.error("media player setDataSource IO error! " + e.toString());
 				notifyMusicStateChanged(Global.STATE_ERROR, null,
 								Global.REASON_NETWORK_IO_ERROR);
 			    return;
 			} catch (IllegalStateException e) {
 				Debugger.warn("media player illegal state! " + e.toString());
 				return;
 			} catch (Exception e) {
 				Debugger.error("media player unexpected error! " + e.toString());
 				return;
 			}
 						
         }
 		@Override
 		protected Integer doInBackground(String... arg0) {
 			
 			//notifyMusicPrepareProgress(10);
 			
 			int chan = Preference.getSelectedChannel(context);
 			Debugger.verbose("AsyncNextMusicTask.doInBackground, selected channel " + chan);
 			synchronized(musicSessionLock) {
 				lastMusic = curMusic;
 				curMusic = getNextMusic();
 				
 				/*if (lastMusic != null) {
 					musicHistory.add(0, lastMusic);
 					Debugger.verbose("adding music to history");
 				}
 				while (musicHistory.size() > MAX_HISTORY_COUNT) {
 					musicHistory.remove(musicHistory.size() - 1);
 					Debugger.verbose("remove old history");
 				}*/
 				
 				
 				
 				if (curMusic == null) {
 				    Debugger.error("curMusic == null!!");
 				    
 					
 				    return Global.REASON_NETWORK_IO_ERROR;
 				} 
 				return 0;
 				
 			}
 		}
 		
 	}
 	
     /*private class GetPictureTask extends AsyncTask<String, Integer, Bitmap> {
     	public GetPictureTask(){
     		
     	}
     	
     	@Override
     	protected void onCancelled () {
     		Debugger.info("GetPictureTask is cancelled");
     		
     	}
     	@Override
     	protected Bitmap doInBackground(String... params) {
         	if (params.length < 1)
         		return null;
         	
     		HttpGet httpGet = new HttpGet(params[0]);
     		httpGet.setHeader("User-Agent", 
     				String.valueOf(Utility.getSdkVersionName()));
     		httpGet.setHeader("Connection", "Keep-Alive");
     		HttpResponse httpResponse = null;
     		try {
     			httpResponse = new DefaultHttpClient().execute(httpGet);
 				if (isCancelled())
 					return null;
     			publishProgress(70);
     		} catch (Exception e) {
     			Debugger.error("Error getting response of downloading picture: " + e.toString());
     			return null;
     		}
     		
     		int statuscode = httpResponse.getStatusLine().getStatusCode();
 			if (statuscode != 200) {
 				Debugger.error("Error getting response of downloading music: status " + statuscode);
     			return null;
 			}
 				
 			byte b[] = null;
 			try {
 				InputStream is = httpResponse.getEntity().getContent();
 				long len = httpResponse.getEntity().getContentLength();
 				
 				publishProgress(80);
 				
 				int length = (int)(len);
 				b = new byte[length];
 				int l = 0;
 				while (l < length) {
 					if (isCancelled())
 						return null;
 					int tmpl = is.read(b, l, length);
 					if (tmpl == -1)
 						break;
 					l += tmpl;
 				}
 				
 				publishProgress(90);
 				
 				if (isCancelled())
 					return null;
 				Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, length);
 				
 				publishProgress(100);
 				
 				return bmp;
 			} catch (Exception e) {
 				Debugger.error("Error getting picture: " + e.toString());
 				return null;
 			}
         	
     	}
     	@Override
     	protected void onProgressUpdate(Integer... progress) {
     		if (progress.length < 1)
     			return;
     		notifyMusicPrepareProgress(progress[0]);
         }
     	@Override
         protected void onPostExecute(Bitmap bmp) {
     		if (bmp == null) {
         		notifyPictureStateChanged(Global.STATE_IDLE, null, NO_REASON);
     		}
      		
     		curPic = bmp;
     		
     		if (isCancelled()) {
     			return;
     		}
     		
     		notifyPictureStateChanged(Global.STATE_STARTED, bmp, NO_REASON);
     		
         }
     }*/
     
     private void popNotify(String msg)
     {
         Toast.makeText(context, msg,
                 Toast.LENGTH_LONG).show();
     }
 
 	@Override
 	public void onHttpFetchSuccess(String url) {
 		
 		
 		notifyPictureStateChanged(Global.STATE_FINISHED, url, Global.NO_REASON);
 	}
 
 	@Override
 	public void onHttpFetchProgress(int progress) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onHttpFetchFailure(String url, int reason) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onPlayListChanged(int count) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onPlayListError(int reason) {
 		// TODO Auto-generated method stub
 		
 	}
 }
