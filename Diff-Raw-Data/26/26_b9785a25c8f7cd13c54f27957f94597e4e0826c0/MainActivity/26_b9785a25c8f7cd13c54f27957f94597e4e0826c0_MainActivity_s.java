 package com.unusualbob.soundtrack;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewFragment;
 import android.widget.Button;
 
 import com.google.android.youtube.player.YouTubePlayer;
 import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
 import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
 import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
 import com.google.android.youtube.player.YouTubePlayerFragment;
 import com.google.android.youtube.player.YouTubePlayer.PlayerStyle;
 
 public class MainActivity extends YouTubeFailureRecoveryActivity
 {
 	String TAG = "MainActivity";
 	WebViewFragment webViewFragment;
 	WebView webView;
 	YouTubePlayer ytPlayer;
 	PowerManager.WakeLock wl;
 	String currentTrack;
 
 	@SuppressLint("SetJavaScriptEnabled")
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		//Wakelock init
 		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Sountrack.io");
 
 		//Webview init
 		webViewFragment = (WebViewFragment) getFragmentManager().findFragmentById(R.id.webpage);
 		webView = webViewFragment.getWebView();
 
 		//Make sure our webview uses the chrome client so we can have awesome javascript
 		webView.setWebChromeClient(new WebChromeClient());
 
 		// requires javascript
 		webView.getSettings().setJavaScriptEnabled(true);
 
 		JsInterface jsInterface = new JsInterface();
 
 		// provide an interface accessible to javascript
 		webView.addJavascriptInterface(jsInterface, "android");
 
 		// load the page
 		webView.loadUrl("http://thegridcraft.com:13000/android");
 
 		//Initialize the youtube player
 		YouTubePlayerFragment youTubePlayerFragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment);
 		youTubePlayerFragment.initialize(Configuration.DEVELOPER_KEY, this);
 		
 		Button btnReload = (Button)findViewById(R.id.reload);
 		btnReload.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v)
 			{
 				// TODO Auto-generated method stub
 				webView.loadUrl("http://thegridcraft.com:13000/android");
 				
 			}
 		
 		});
 
 	}
 	
 	//This method is from the base class and gives us access to the player
 	@Override
 	public void onInitializationSuccess(YouTubePlayer.Provider provider, final YouTubePlayer player, boolean wasRestored)
 	{
 		ytPlayer = player;
 		player.setPlayerStyle(PlayerStyle.CHROMELESS);
 		
 		player.setPlaybackEventListener(new PlaybackEventListener() {
 
 			@Override
 			public void onBuffering(boolean arg0)
 			{
 				Log.d("Player", "Buffering: " + arg0);
 			}
 
 			@Override
 			public void onPaused()
 			{
 				//For now the only reason for pauses is a screen-off, we try to resume after 500ms
 				Log.d("Youtube Player", "Paused");
				TimerTask task = new TimerTask() {
					public void run() {
						Log.d("Player", "Resume Track");
						player.loadVideo(currentTrack, player.getCurrentTimeMillis()+1500);
					}
				};
				Timer timer = new Timer();
				timer.schedule(task, 500);
 				
 			}
 
 			@Override
 			public void onPlaying()
 			{
 				Log.d("Player", "playing");
 			}
 
 			@Override
 			public void onSeekTo(int arg0)
 			{
 				//Auto stub				
 			}
 
 			@Override
 			public void onStopped()
 			{
 				Log.d("Youtube Player", "Stopped");
 			}
 			
 		});
 		
 		player.setPlayerStateChangeListener(new PlayerStateChangeListener() {
 
 			@Override
 			public void onAdStarted()
 			{
 				//Auto stub
 			}
 
 			@Override
 			public void onError(ErrorReason arg0)
 			{
 				// TODO Intercept 'unplayable' error here and send back to the server
 				Log.e("Youtube Player", "Error: " + arg0);
 				
 			}
 
 			@Override
 			public void onLoaded(String arg0)
 			{
 				//Auto stub
 			}
 
 			@Override
 			public void onLoading()
 			{
 				//Auto stub
 			}
 
 			@Override
 			public void onVideoEnded()
 			{
 				//Auto stub
 			}
 
 			@Override
 			public void onVideoStarted()
 			{
 				//Auto stub
 			}
 			
 		});
 	}
 
 	@Override
 	protected YouTubePlayer.Provider getYouTubePlayerProvider()
 	{
 		return (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment);
 	}
 	
 
 	//This is our really basic javascript interface
 	public class JsInterface
 	{
 		//Shows a basic log statement
 		public void showLog(String msg)
 		{
 			Log.d("Log from JS", msg);
 		}
 
 		//Starts a track at the specified number of milliseconds
 		public void startTrack(String id, int seek)
 		{
 			currentTrack = id;
 			ytPlayer.loadVideo(id, seek);
 		}
 	}
 
 	//TODO: make an actual menu of some kind
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 
 }
