 package cn.huaxingtan.view;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import com.umeng.analytics.MobclickAgent;
 
 import cn.huaxingtan.controller.FileManager;
 import cn.huaxingtan.model.AudioItem;
 import cn.huaxingtan.model.AudioItem.Status;
 import cn.huaxingtan.player.R;
 import cn.huaxingtan.service.MusicPlayerService;
 import cn.huaxingtan.util.Misc;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnBufferingUpdateListener;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnErrorListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.media.MediaPlayer.OnSeekCompleteListener;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class MusicPlayerActivity extends Activity implements OnPreparedListener, OnBufferingUpdateListener,
 		OnSeekCompleteListener, OnErrorListener, OnCompletionListener{
 	private static final String TAG = MusicPlayerActivity.class.getCanonicalName();
 	private Long fileId;
 	private MusicPlayerService mPlayerService;
 	private SeekBar mSeekBar;
 	private Handler mHandler;
 	private boolean mRefreshUI;
 	private FileManager mFileManager;
 	private ImageButton mPreButton;
 	private ImageButton mNextButton;
 	private ImageButton mPlayButton;
 	private ImageButton mPauseButton;
 	private TextView mPlayTime;
 	private TextView mLeftTime;
 	private static boolean offline;
 	private List<Long> mPlayList;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		super.onCreate(savedInstanceState);
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		
 		
 		mFileManager = new FileManager(this);
 		Intent musicIntent = getIntent();
 		fileId = musicIntent.getExtras().getLong("fileId");
 		mPlayList = new ArrayList<Long>();
 		offline = musicIntent.getExtras().getBoolean("offline", false);
 		List<AudioItem> list = mFileManager.getAudioItems(mFileManager.getAudioItem(fileId).getSerialId());
 		Collections.sort(list, new Comparator<AudioItem>(){
 			@Override
 			public int compare(AudioItem lhs, AudioItem rhs) {
 				long ret = lhs.getFileId()- rhs.getFileId();
 				if (ret < 0)
 					return -1;
 				return ret == 0 ? 0 : 1;
 			}
 		});
 		for (AudioItem item:list) {
 			if (!offline || item.getStatus() == Status.FINISHED)
 				mPlayList.add(item.getFileId());
 		}
 
 		setTitle(mFileManager.getAudioItem(fileId).getName());
 		setContentView(R.layout.activity_music_player);
 		mSeekBar = (SeekBar)findViewById(R.id.music_seekBar);
 		
 		mSeekBar.setClickable(false);
 		
 		mPlayButton = (ImageButton)findViewById(R.id.play_button);
 		
 		mPauseButton = (ImageButton)findViewById(R.id.pause_button);
 		mNextButton = (ImageButton)findViewById(R.id.next_button);
 		mPreButton = (ImageButton)findViewById(R.id.last_button);
 		mPlayTime = (TextView) findViewById(R.id.music_time_played);
 		mLeftTime = (TextView) findViewById(R.id.music_time_left);
 		
 		mHandler = new Handler();
 		mRefreshUI = true;
 		bindService(new Intent(this,  MusicPlayerService.class),
 				mConn, Context.BIND_AUTO_CREATE);
 		
 
 		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				if (fromUser) {
 					if(mPlayerService.seek(seekBar.getProgress()))
 						mSeekBar.setClickable(false);
 				}
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 		});
 		
 	}
 	
 	public void onResume() {
 		super.onResume();
 		MobclickAgent.onResume(this);
 		}
 	
 	public void onPause() {
 		super.onPause();
 		MobclickAgent.onResume(this);
 	}
 	
 	
 	private ServiceConnection mConn = new ServiceConnection(){
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			Log.i(TAG, "service connected");
 			mPlayerService = ((MusicPlayerService.PlayerBinder) service).getService();
 			mPlayerService.setAudioItem(mFileManager.getAudioItem(fileId), MusicPlayerActivity.this,  MusicPlayerActivity.this, null, MusicPlayerActivity.this, MusicPlayerActivity.this);
 			
 			mHandler.postDelayed(new Runnable() {
 				@Override
 				public void run() {
 					int percentage = 0;
 					long cur = mPlayerService.getCurrentPosition();
 					long dur = mPlayerService.getDuration();
 					if (dur > 0 && cur >= 0)
 						percentage = (int)(cur * 100/dur);
 					Log.d(TAG, "current play percentage:" + percentage + ", current " + cur + ", duration " + dur);
 					
 					if (mPlayerService.isPlaying()) 
 						toPlayState();
 					else
 						toPauseState();
 					mPlayTime.setText(Misc.formatEnglishDuration(cur/1000));
 					mLeftTime.setText(Misc.formatEnglishDuration((dur - cur)/1000));
 					mSeekBar.setProgress(percentage);
 					if (mRefreshUI) {
 						mHandler.postDelayed(this, 200);
 					}
 				}
 				
 			}, 200);
 			
 			mPlayButton.setOnClickListener(new View.OnClickListener(){
 				@Override
 				public void onClick(View v) {
 					MusicPlayerActivity.this.mPlayerService.play();
 				}});
 			
 			mPauseButton.setOnClickListener(new View.OnClickListener(){
 				@Override
 				public void onClick(View v) {
 					MusicPlayerActivity.this.mPlayerService.pause();  
 				}});
 			
 			mNextButton.setOnClickListener(new View.OnClickListener(){
 				@Override
 				public void onClick(View v) {
 					int idx = mPlayList.indexOf(fileId);
 					if (idx < 0 || idx == mPlayList.size()) {
 						Toast.makeText(MusicPlayerActivity.this, "已是最后一讲", Toast.LENGTH_SHORT).show();
 					} else {
 						fileId = mPlayList.get(idx + 1);
 						mPlayerService.setAudioItem(mFileManager.getAudioItem(fileId), MusicPlayerActivity.this,  MusicPlayerActivity.this, null, MusicPlayerActivity.this, MusicPlayerActivity.this);
 					}
 				}});
 			
 			mPreButton.setOnClickListener(new View.OnClickListener(){
 				@Override
 				public void onClick(View v) {
 					int idx = mPlayList.indexOf(fileId);
 					if (idx <= 0 ) {
 						Toast.makeText(MusicPlayerActivity.this, "已是第一讲", Toast.LENGTH_SHORT).show();
 					} else {
 						fileId = mPlayList.get(idx - 1);
 						mPlayerService.setAudioItem(mFileManager.getAudioItem(fileId), MusicPlayerActivity.this,  MusicPlayerActivity.this, null, MusicPlayerActivity.this, MusicPlayerActivity.this);
 					}
 				}});
 
 		}
 	
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			mPlayerService = null;
 		}
 	};
 	
 	@Override
 	protected void onDestroy() {
 		mRefreshUI = false;
 		unbindService(mConn);
 		super.onDestroy();
 	}
 
 	@Override
 	public void onPrepared(MediaPlayer mp) {
 		mPlayerService.play();
 		mSeekBar.setClickable(true);
 	}
 
 	@Override
 	public void onBufferingUpdate(MediaPlayer mp, int percent) {
 		Log.d(TAG, "music buffered %" + percent);
 		this.mSeekBar.setSecondaryProgress(percent);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		
 		MenuItem settingItem = menu.findItem(R.id.action_settings);
 		settingItem.setOnMenuItemClickListener(new OnSettingClickedListener(this));
 		
 		MenuItem downloadItem = menu.findItem(R.id.action_download);
 		downloadItem.setOnMenuItemClickListener(new OnMenuItemClickListener(){
 			@Override
 			public boolean onMenuItemClick(MenuItem item) {
 				Intent intent = new Intent(MusicPlayerActivity.this, DownloadingActivity.class);
 				startActivity(intent);
 				return true;
 			}
 		});
 		
 		MenuItem playItem = menu.findItem(R.id.action_play);
 		playItem.setVisible(false);
 		
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public void onSeekComplete(MediaPlayer mp) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	private void toPlayState(){
 		mPlayButton.setVisibility(View.INVISIBLE);
 		mPauseButton.setVisibility(View.VISIBLE);
 	}
 	
 	private void toPauseState(){
 		mPlayButton.setVisibility(View.VISIBLE);
 		mPauseButton.setVisibility(View.INVISIBLE);
 	}
 
 	@Override
 	public boolean onError(MediaPlayer mp, int what, int extra) {
 		Toast.makeText(this, "音乐加载失败", Toast.LENGTH_LONG).show();
 		return true;
 	}
 
 	@Override
 	public void onCompletion(MediaPlayer mp) {
 		mPlayerService.pause();
 		
 	}
 
 }
