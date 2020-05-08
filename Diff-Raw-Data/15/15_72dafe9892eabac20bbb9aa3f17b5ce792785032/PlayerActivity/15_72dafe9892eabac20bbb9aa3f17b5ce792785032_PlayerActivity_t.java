 package com.tikal.share;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 
 import com.google.android.youtube.player.YouTubeBaseActivity;
 import com.google.android.youtube.player.YouTubeInitializationResult;
 import com.google.android.youtube.player.YouTubePlayer;
 import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
 import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
 import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
 import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
 import com.google.android.youtube.player.YouTubePlayer.Provider;
 import com.google.android.youtube.player.YouTubePlayerView;
 import com.google.cloud.backend.android.tikal.share.sync.CloudSync;
 
 public class PlayerActivity extends YouTubeBaseActivity implements OnInitializedListener {
 
 	public static final String DEVELOPER_KEY = "AIzaSyAF1kx_N4utAzBWt_t1bdcRpgBQlwyAFgo";
 	private MHPlaybackEventListener playerEventListener;
 	private MHPlayerStateChangeListener playerStateChangeListener;
 	private YouTubePlayer playa; //hollla
 	private boolean playerLoaded;
 	private int previousReqSeek;
 	
 	
 	//remove when integrated
 	private String videoID;
 	private String clientID = "mikeh";
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_play);
 
 		playerLoaded = false;
 		previousReqSeek = 0;
 		
 		videoID = getIntent().getExtras().getString(CloudSync.INTENT_VIDEO_ID);
 		clientID = getIntent().getExtras().getString(CloudSync.INTENT_VIDEO_ID);
		/*
		Intent syncStart = new Intent(this, CloudSync.class);
 		syncStart.putExtra(CloudSync.INTENT_VIDEO_ID, videoID);
 		syncStart.putExtra(CloudSync.INTENT_CLIENT_ID, clientID);
 		syncStart.putExtra(CloudSync.INTENT_COMMAND,CloudSync.INTENT_COMMAND_GET);
 		this.startActivityForResult(syncStart, 55);*/
 		
 		YouTubePlayerView youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
 		youTubeView.initialize(DEVELOPER_KEY, this);  
 
 		playerEventListener = new MHPlaybackEventListener();
 		playerStateChangeListener = new MHPlayerStateChangeListener();
 
 		
 
 //		YouTubePlayerView youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
 //		youTubeView.initialize(DEVELOPER_KEY, this);  
 //
 //		playerEventListener = new MHPlaybackEventListener();
 //		playerStateChangeListener = new MHPlayerStateChangeListener();
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		String offSet = "";
 		if(data != null){
 			offSet = ((String)data.getExtras().getString("result"));
 			if(!offSet.equals("")){
 				try{
 					previousReqSeek = Integer.parseInt(offSet);
 				}
 				catch(Exception ex){
 					previousReqSeek = 0;
 				}
 			}
 		}
 		YouTubePlayerView youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
 		youTubeView.initialize(DEVELOPER_KEY, this);  
 
 		playerEventListener = new MHPlaybackEventListener();
 		playerStateChangeListener = new MHPlayerStateChangeListener();
 	}
 
 	@Override
 	public void onInitializationFailure(Provider arg0,
 			YouTubeInitializationResult arg1) {
 
 		//TODO add error here
 	}
 
 
 	@Override
 	public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
 			boolean wasRestored) {
 		if (!wasRestored) {
 			playa = player;
 
 			playa.cueVideo(videoID);
 			playa.setPlaybackEventListener(playerEventListener);
 			playa.setPlayerStateChangeListener(playerStateChangeListener);
 		}
 	}
 
 	private void startVideoAt(int milis){
 		if(playerLoaded){
 			previousReqSeek = 0;
 			playa.seekToMillis(milis);
 			playa.play();
 		}
 		else{
 			previousReqSeek = milis;
 		}
 	}
 
 	private class MHPlaybackEventListener implements PlaybackEventListener {
 
 		@Override
 		public void onBuffering(boolean arg0) {
 			Log.d("PlayerStateChanged", "onBuffering");
 
 		}
 
 		@Override
 		public void onPaused() {
 			//call save here or in onStopped, seems that when one is called the other is
 			/*Log.d("PlayerStateChanged", "onPaused");
 			Intent syncStart = new Intent(PlayerActivity.this, CloudSync.class);
 			syncStart.putExtra(CloudSync.INTENT_VIDEO_ID, videoID);
 			syncStart.putExtra(CloudSync.INTENT_CLIENT_ID, clientID);
 			syncStart.putExtra(CloudSync.INTENT_OFFSET_MILIS, "" + playa.getCurrentTimeMillis());
 			syncStart.putExtra(CloudSync.INTENT_COMMAND, CloudSync.INTENT_COMMAND_SET);
 			PlayerActivity.this.startActivityForResult(syncStart, 55);*/
 			
 		}
 
 		@Override
 		public void onPlaying() {
 			Log.d("PlayerStateChanged", "onPlaying");
 
 		}
 
 		@Override
 		public void onSeekTo(int arg0) {
 			Log.d("PlayerStateChanged", "onSeekTo");
 
 		}
 
 		@Override
 		public void onStopped() {
 		}
 	}
 
 	private class MHPlayerStateChangeListener implements PlayerStateChangeListener  {
 
 		@Override
 		public void onAdStarted() {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void onError(ErrorReason arg0) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void onLoaded(String arg0) {
 			playerLoaded = true;
 			//in case previously requested seek seek to that point
 			if(previousReqSeek > 0){
 				startVideoAt(previousReqSeek);
 			}
 		}
 
 		@Override
 		public void onLoading() {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void onVideoEnded() {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void onVideoStarted() {
 			// TODO Auto-generated method stub
 
 		}
 
 	}
 }
 
