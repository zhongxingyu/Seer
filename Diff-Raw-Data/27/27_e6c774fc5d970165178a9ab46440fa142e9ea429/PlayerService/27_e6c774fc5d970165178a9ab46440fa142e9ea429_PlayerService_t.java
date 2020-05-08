 package jp.dip.ysato.onsenplayer;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.RandomAccessFile;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.AudioManager.OnAudioFocusChangeListener;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.util.Log;
 
 public class PlayerService extends Service {
 	public static final String Notify = PlayerService.class.getName()+".NOTIFY";
 	public static final String WaitStream = "WaitStream";
 	public static final String Resume = "Resume";
 	public static final String RemotePause = "Pause";
 	public static final String RemotePlay = "Play";
 	public static final String START = "Start";
 	public static final String PAUSE = "Pause";
 	public static final String PLAY = "Play";
 	public static final String SEEK = "Seek";
	public static final String PlayPause = "PlayPause";
 	private MediaPlayer mediaPlayer;
 	private NotificationManager notificationManager;
 	private int length = 0;
 	private GetStream getStream;
 	protected RandomAccessFile cachefile;
 	protected RandomAccessFile streamfile;
 	protected int prefetch;
 	private Bundle bundle;
 	private boolean manualPause;
 	public boolean waitstream;
 	private WakeLock wakeLock;
 	private String url;
 	private ScheduledFuture<?> monitorThread;
 	private PlayActivity activity;
 	private ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);
 	private AudioManager audioManager;
 	private OnAudioFocusChangeListener onAudioFocusChangeListener;
 	class GetStream extends Thread {
 		private String url;
 		public GetStream(String url) {
 			this.url = url;
 		}
 		public void run() {
 			InputStream in = null;
 			FileOutputStream out = null;
 			HttpURLConnection http = null;
 			try {
 				String filename = urltofile(this.url);
				File file = new File (PlayerService.this.getExternalFilesDir(null), filename);
 				if (file.exists())
 					file.delete();
 				file.deleteOnExit();
 				cachefile = new RandomAccessFile(file, "rw");
 				URL url = new URL(this.url);
 				do {
 					http = (HttpURLConnection) url.openConnection();
 					http.setRequestMethod("GET");
 					if (length > 0)
						http.setRequestProperty("Range", String.format("%d-", cachefile.getFilePointer()));
 					http.connect();
 					http.setReadTimeout(30 * 1000);
 					if (length == 0) {
 						length = Integer.parseInt(http.getHeaderField("Content-Length"));
 						cachefile.setLength(length);
 					}
 					in = http.getInputStream();
 					byte buf[] = new byte[128 * 1024];
 					while(length > 0) {
 						try {
 							int receive = in.read(buf);
 							cachefile.write(buf,0, receive);
 							length -= receive;
 							prefetch += receive;
 						} catch (IOException e) {
 							prefetch = 0;
 							http.disconnect();
 							break;
 						}
 					}
 				} while(length > 0);
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} finally {
 				try {
 					if (in != null)
 						in.close();
 					if (out != null)
 						out.close();
 					if (http != null)
 						http.disconnect();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			Log.d("OnsenPlayer", "Download complete");
 			// TODO Auto-generated method stub
 		}
 	}
 	
 	class PlayMonitor implements Runnable {
 		private String title;
 		private String file;
 		public PlayMonitor(String file, String title) {
 			this.title = title;
 			this.file = file;
 		}
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			try {
 				if (cachefile != null && streamfile != null) {
 					try {
 						if ((cachefile.getFilePointer() - streamfile.getFilePointer()) < 65536) {
 							if (mediaPlayer.isPlaying()) {
 								mediaPlayer.pause();
 								prefetch = 0;
 								sendMessage(WaitStream);
 								wakeLock.release();
 							}
 						} else {
							if (mediaPlayer != null && !mediaPlayer.isPlaying() && !manualPause) {
 								mediaPlayer.start();
 								sendMessage(Resume);
 								wakeLock.acquire();
 								setNotification(title);
 							}
 						}
 					} catch (IllegalStateException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				if (streamfile == null && prefetch > 65536) {
 					try {
						streamfile = new RandomAccessFile(new File(PlayerService.this.getExternalFilesDir(null), urltofile(file)), "r");
 						mediaPlayer.setDataSource(streamfile.getFD());
 						mediaPlayer.setDisplay(null);
 						mediaPlayer.prepare();
 						setNotification(title);
 						audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
 						mediaPlayer.start();
 						wakeLock.acquire();
 						if (activity != null)
 							activity.setDuration(mediaPlayer.getDuration() / 1000);
 					} catch (FileNotFoundException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IllegalArgumentException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IllegalStateException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				if (activity != null) {
 					int position = mediaPlayer.getCurrentPosition() / 1000;
 					activity.setPosition(position);
 				}
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	class PlayerServiceBinder extends Binder {
 		public void registerActivity(PlayActivity activity) {
 			PlayerService.this.activity = activity;
 			if (streamfile != null && prefetch > 65536)
 				activity.setDuration(mediaPlayer.getDuration() / 1000);
 		}
 	}
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		mediaPlayer = new MediaPlayer();
 		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
 			@Override
 			public void onCompletion(MediaPlayer arg0) {
 				// TODO Auto-generated method stub
 				if (length <= 0)
					notificationManager.cancel(R.string.app_name);
 			}
 		});
 		onAudioFocusChangeListener = new OnAudioFocusChangeListener() {
 			@Override
 			public void onAudioFocusChange(int arg0) {
 				// TODO Auto-generated method stub
 			}
 			
 		};
 		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getString(R.string.app_name));
 	}
 	public void setNotification(String title) {
 		// TODO Auto-generated method stub
		Notification n = new Notification(R.drawable.ic_launcher, getString(R.string.playNotification, title), 
 				System.currentTimeMillis());
 		n.flags = Notification.FLAG_ONGOING_EVENT;
 		Intent intent = new Intent(this, PlayActivity.class);
 		intent.putExtra("program", bundle);
 		PendingIntent ci = PendingIntent.getActivity(this, 0, intent, 0);
 		n.setLatestEventInfo(this, getString(R.string.app_name), getString(R.string.playNotification, title), ci);
 		notificationManager.notify(R.string.app_name, n);
 	}
 	private void sendMessage(String message) {
 		// TODO Auto-generated method stub
 		Intent i = new Intent(Notify);
 		i.putExtra("message", message);
 		sendBroadcast(i);
 	}
 	
 	private String urltofile(String url) {
 		// TODO Auto-generated method stub
 		String f[] = url.split("/");
 		String filename = f[f.length - 1];
 		return filename;
 	}
 	@Override
 	public void onDestroy()
 	{
 		notificationManager.cancelAll();
 		getStream.stop();
 		if(mediaPlayer.isPlaying())
 			wakeLock.release();
 		mediaPlayer.release();
 		mediaPlayer = null;
 		audioManager.abandonAudioFocus(onAudioFocusChangeListener);
 	}
 	@Override
 	public void onStart(Intent intent, int startId) {
 		super.onStart(intent, startId);
 		String action = intent.getAction();
 		if (action.equals(PlayerService.START)) {
 			bundle = intent.getBundleExtra("program");
 			String[] program = bundle.getStringArray("program");
 			if (!program[1].equals(url)) {
 				url = program[1];
 				getStream = new GetStream(program[1]);
 				getStream.start();
 				sendMessage(WaitStream);
 				if (monitorThread != null)
 					monitorThread.cancel(true);
 				monitorThread = threadPool.scheduleAtFixedRate(new PlayMonitor(program[1], program[3]), 0, 500, TimeUnit.MILLISECONDS);
 			}
 		}
 		if (action.equals(PlayerService.PAUSE) && mediaPlayer.isPlaying()) {
 			manualPause = true;
 			mediaPlayer.pause();
 			notificationManager.cancel(R.string.app_name);
 			if (activity != null)
 				activity.setPlayState(false);
 		}
 		if (action.equals(PlayerService.PLAY)) {
 			manualPause = false;
 		}
 		if (action.equals(PlayerService.SEEK)) {
 			if (length <= 0) {
 				int position = intent.getIntExtra("position", -1);
 				if (position >= 0)
 					mediaPlayer.seekTo(position * 1000);				
 			}
 		}
 		if (action.equals(PlayerService.PlayPause)) {
 			if (manualPause)
 				manualPause = false;
 			else if (mediaPlayer.isPlaying()) {
 				manualPause = true;
 				mediaPlayer.pause();
 				notificationManager.cancel(R.string.app_name);
 			}
 			if (activity != null)
 				activity.setPlayState(!manualPause);
 		}
 	}
 	@Override
 	public IBinder onBind(Intent intent) {
 		// TODO Auto-generated method stub
 		return new PlayerServiceBinder();
 	}
 	@Override
 	public boolean onUnbind(Intent intent) {
 		activity = null;
 		return true;
 	}
 }
