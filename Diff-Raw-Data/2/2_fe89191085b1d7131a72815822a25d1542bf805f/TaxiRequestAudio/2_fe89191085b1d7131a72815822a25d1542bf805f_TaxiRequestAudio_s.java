 package com.benbentaxi.passenger.taxirequest.player;
 
 import java.io.IOException;
 
 import com.benbentaxi.passenger.taxirequest.create.CreateTaxiRequestActivity;
 
 import android.media.MediaPlayer;
 import android.util.Log;
 
 public class TaxiRequestAudio implements android.media.MediaPlayer.OnErrorListener,
 		MediaPlayer.OnCompletionListener {
 	private final String TAG			     		= TaxiRequestAudio.class.getName();
 
 	/*
 	 * INIT 表示没有占用任何资源
 	 * PLAY 表示正在播放
 	 */
 	enum STATE {INIT,PALY}
 	
 	private STATE 		mState					=	STATE.INIT;
 	private String		mAudioFilePath			=	CreateTaxiRequestActivity.PASSENGER_VOICE_FILE;
 	private MediaPlayer mMediaPlayer			=	null; 
 
 	public TaxiRequestAudio()
 	{
 		
 	}
 	public synchronized void play()
 	{
 		Log.d(TAG,"Begin .................................");
 		if (mState != STATE.INIT){
 			Log.w(TAG,"mMediaPlayer play need in INIT state");
 			return;
 		}
 		if (mMediaPlayer != null){
 			Log.w(TAG,"mMediaPlayer not destroy!" + mMediaPlayer);
 		}
     	mMediaPlayer = new MediaPlayer();
     	Log.d(TAG,"MediaPlayer init ............"+mMediaPlayer);
     	try {
 			mMediaPlayer.setDataSource(mAudioFilePath);
 		} catch (IllegalArgumentException e) {
 			Log.w(TAG,"mMediaPlayer set data source fail!");
 			e.printStackTrace();
 			return;
 		} catch (SecurityException e) {
 			Log.w(TAG,"mMediaPlayer set data source fail!");
 			e.printStackTrace();
 			return;
 		} catch (IllegalStateException e) {
 			Log.w(TAG,"mMediaPlayer set data source fail!");
 			e.printStackTrace();
 			return;
 		} catch (IOException e) {
 			Log.w(TAG,"mMediaPlayer set data source fail!");
 			e.printStackTrace();
 			return;
 		}
     	mMediaPlayer.setOnErrorListener(this);
     	mMediaPlayer.setOnCompletionListener(this);
     	mState = STATE.PALY;
     	Thread p = new Thread(){
     		public void run()
     		{
     			Log.d(TAG,"thread start........................");
 
     			try {
 					mMediaPlayer.prepare();
 				} catch (IllegalStateException e) {
 					Log.w(TAG,"mMediaPlayer prepare fail!");
 					e.printStackTrace();
 					return;
 				} catch (IOException e) {
 					Log.w(TAG,"mMediaPlayer prepare fail!");
 					e.printStackTrace();
 					return;
 				}
     			Log.d(TAG,"thread prepare ready........................");
 
     			try{
     				mMediaPlayer.start();
     			}catch (IllegalStateException e){
 					Log.w(TAG,"mMediaPlayer start fail!");
 					e.printStackTrace();
 					return;
     			}
     			Log.d(TAG,"thread exit........................");
     		}
     	};
 		Log.d(TAG,"End .................................");
 
     	p.start();
 	}
 	
 	public void release()
 	{
 		if (this.mMediaPlayer != null){
 			Log.d(TAG,"1. MediaPlaer release......................................."+mMediaPlayer);
 			this.mMediaPlayer.release();
 			Log.d(TAG,"2. MediaPlaer release......................................."+mMediaPlayer);
 
 		}
 		this.mState 			= STATE.INIT;
 		this.mMediaPlayer		= null;
 	}
 
 	@Override
 	public boolean onError(MediaPlayer mp, int what, int extra) {
 		Log.d(TAG,"find error!!"+what+"|"+extra);
		//return false 一定会调用onComplete回调函数，在哪里处理状态，释放资源。
 		return false;
 	}
 	@Override
 	public void onCompletion(MediaPlayer mp) {
 		mp.release();
 		if (mp != this.mMediaPlayer){
 			Log.e(TAG, "not the same media player");
 		}
 		release();
 	}
 	
 }
