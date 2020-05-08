 package hk.com.dycx.iptv.player;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnErrorListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.net.Uri;
 import android.os.Bundle;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 import android.widget.VideoView;
 import hk.com.dycx.iptv.R;
 import hk.com.dycx.iptv.utils.Logger;
 import hk.com.dycx.iptv.utils.Utils;
 
 /**
  * @author zhangping e-mail:zp@dycx.com.hk
  * @date 2013-2-3 上午11:47:09
  * @version 1.0
  */
 public class SystemPlayer extends BasePlayer {
 	private static final String TAG = "SystemPlayer";
 	/** 系统播放器 */
 	private VideoView mVideoView;
 	
 	private int errorWhat;
 	
 
 	@Override
 	protected void onResume() {
 		if (mVideoView != null) {
 			mVideoView.resume();
 		}
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		if (mVideoView != null) {
 			mVideoView.pause();
 		}
 		super.onPause();
 	}
 	
 	@Override
 	protected void onDestroy() {
 		if (mVideoView != null){
 			mVideoView.stopPlayback();
 		}
 		super.onDestroy();
 	}
 	
 	@Override
 	public void setVideoViewListener() {
 
 		mVideoView.setOnErrorListener(new OnErrorListener() {
 
 			@Override
 			public boolean onError(MediaPlayer mp, int what, int extra) {
 				mVideoView.stopPlayback();
 				errorWhat = what;
 				Logger.i(TAG, isDebug, "errorWhat:"+errorWhat + "extra:"+ extra);
 				if (Utils.isCheckNetAvailable(getApplicationContext())) { //网络可用
 					//先判断用户是否开启软件解码 用 vitamio 解码
					boolean isDecode = mSharedPreferences.getBoolean(Utils.SP_DECODE, false);
 					Logger.i(TAG, isDebug, "isDecode:"+isDecode);
 					if (isDecode) {
 						startVitamioPlayer();
 					}else {
 						Toast.makeText(getApplicationContext(), R.string.error_path, 1).show();
 					}
 					exit();
 					return true;
 				}else {
 					//弹出网络不可用
 					alert(getString(R.string.net_not_work));
 					return true;
 				}
 			}
 		});
 
 		mVideoView.setOnPreparedListener(new OnPreparedListener() {
 
 			@Override
 			public void onPrepared(MediaPlayer mp) {
 				mVideoView.start();
 				mHandler.sendEmptyMessage(HIDE_PROGRESS);
 				Logger.i(TAG, isDebug, "mVideoView.start()");
 			}
 		});
 		
 		mVideoView.setOnCompletionListener( new OnCompletionListener() {
 			
 			@Override
 			public void onCompletion(MediaPlayer mp) {
 				exit();
 			}
 		});
 	
 	}
 
 	protected void startVitamioPlayer() {
 		Logger.i(TAG, isDebug, "startVitamioPlayer");
 		Intent playIntent = new Intent(SystemPlayer.this, VitamioPlayer.class);
 		playIntent.putExtra(Utils.CHILD_SELECT_POSITION, mChildSelectPosition);
 		playIntent.putExtra(Utils.GROUP_SELECT_POSITION, mGroupSelectPosition);
 		startActivity(playIntent);
 	}
 
 	@Override
 	public boolean initContent() {
 		setContentView(R.layout.system_player);
 		Logger.i(TAG, isDebug, "我是：SystemPlayer");
 		return false;
 	}
 
 	@Override
 	public void findVideoView() {
 		mVideoView = (VideoView) findViewById(R.id.system_vv);
 	}
 
 	@Override
 	public void setVideoURI(String path) {
 		if (path.startsWith("http:"))
 			mVideoView.setVideoURI(Uri.parse(path));
 		else
 			mVideoView.setVideoPath(path);
 		Logger.i(TAG, isDebug, "zzzzzzzzzUri:" + Uri.parse(path));
 	}
 	
 	@Override
 	public void exit() {
 		try {
 			if (mVideoView != null) {
 				mVideoView.stopPlayback();
 			}
 			finish();
 		} catch (Exception e) {
 			finish();
 		}
 	}
 }
