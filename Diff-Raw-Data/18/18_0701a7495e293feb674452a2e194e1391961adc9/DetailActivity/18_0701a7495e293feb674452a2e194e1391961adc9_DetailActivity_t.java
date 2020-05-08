 package cn.huaxingtan.view;
 
 import java.io.IOException;
 import java.io.StreamCorruptedException;
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.umeng.analytics.MobclickAgent;
 
 import cn.huaxingtan.controller.CachedDataProvider;
 import cn.huaxingtan.controller.CachedDataProvider.Callback;
 import cn.huaxingtan.controller.FileManager;
 import cn.huaxingtan.model.AudioItem;
 import cn.huaxingtan.model.Serial;
 
 import cn.huaxingtan.player.R;
 import cn.huaxingtan.service.MusicPlayerService;
 import cn.huaxingtan.util.ImageDownloader;
 import cn.huaxingtan.util.Misc;
 import cn.huaxingtan.util.Serialize;
 import cn.huaxingtan.util.ImageDownloader.Mode;
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DownloadManager;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.text.TextPaint;
 import android.util.Log;
 import android.view.ContextThemeWrapper;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class DetailActivity extends Activity {
 	private static final String TAG = DetailActivity.class.getCanonicalName();
     private static final ImageDownloader mImageDownloader = new ImageDownloader();
     static {
     	mImageDownloader.setMode(Mode.CORRECT);
     }
 	private Serial mSerial;
 	private View mHeaderView;
 	private ListView mListView;
 	private List<Long> mData;
 	private boolean mOffline = false;
 
 	private CachedDataProvider mDataProvider;
 	private FileManager mFileManager;
 	private DetailAdapter mAdapter;
 	private Handler mHandler;
 	private volatile boolean mRefreshUI = false;
 	private MusicPlayerService mPlayerService;
 	private long prePlayingId = -1;
 	private volatile boolean mUIReady = false;
 	private DownloadManager mDownloadManager;
 	private Thread mThread;
 	private ProgressDialog mProgressDialog;
 	public static volatile WeakReference<DetailActivity> running;
 	private MenuItem mPlayItem;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		
 		setContentView(R.layout.activity_detail);
 		
 		
 		mData = new ArrayList<Long>();
 		mDataProvider = new CachedDataProvider();
 		mFileManager = new FileManager(this);
 		mHandler = new Handler();
 		Intent intent = getIntent();
 		byte[] bytes = (byte[])intent.getExtras().get("Serial");
 		mOffline = intent.getExtras().getBoolean("isOffline");
 		try {
 			mSerial = (Serial) Serialize.deserialize(bytes);
 		} catch (Exception e) {
 			Log.e(TAG, "fail to deserialize Serial", e);
 		}
 		
 		LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		mHeaderView = inflator.inflate(R.layout.detail_header, null);
 		ImageView imageView = (ImageView) mHeaderView.findViewById(R.id.detail_header_image);
 		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
 		mImageDownloader.download(mSerial.getCoverUrl(), imageView);
 		mDownloadManager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
 		
 		TextView textView = (TextView) mHeaderView.findViewById(R.id.detail_header_name);
 		TextPaint tp = textView.getPaint(); 
 //		tp.setFakeBoldText(true);
 		textView.setText(mSerial.getName());
 		
 		textView = (TextView) mHeaderView.findViewById(R.id.detail_header_quantity);
 		tp = textView.getPaint(); 
 //		tp.setFakeBoldText(true);
 		textView.setText("共"+mSerial.getQuantity()+"讲");
 		textView = (TextView) mHeaderView.findViewById(R.id.detail_header_duration);
 		textView.setText(Misc.formatDuration(mSerial.getDuration()));
 
 		mProgressDialog = ProgressDialog.show(this, "加载数据", "请稍候");
 		mListView = (ListView)this.findViewById(R.id.detail);
 		mListView.addHeaderView(mHeaderView);
 		mRefreshUI = true;
 		mHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				if (mRefreshUI) {
 					Log.d(TAG, "refreshing UI");
 					if (mUIReady) {
						if (mProgressDialog.isShowing())
							mProgressDialog.dismiss();
 						mAdapter.updatePlayingId();
 						mAdapter.updateInfoView();
 					}
 					mHandler.postDelayed(this, 200);
 				}
 			}
 			
 		}, 200);
 		
 		mThread = new Thread(new Runnable(){
 			@Override
 			public void run() {
 				while(mRefreshUI) {
 					Log.d(TAG, "updating data");
 					try {
 						if (mUIReady)
 							updateDownloadData();
 					} catch (Exception e) {
 						Log.e(TAG, "download data update failed", e);
 					}
 					try {
 						Thread.sleep(200);
 					} catch (InterruptedException e) {
 						Log.e(TAG, "download data thread interrupted", e);
 					}
 				}
 			}
 		});
 		
 		mThread.start();
 		
 
 		bindService(new Intent(this,  MusicPlayerService.class),
 				mConn, Context.BIND_AUTO_CREATE);
 		
 		
 	}
 	
 	private ServiceConnection mConn = new ServiceConnection(){
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			Log.i(TAG, "music service connected");
 			mPlayerService = ((MusicPlayerService.PlayerBinder) service).getService();
 			mAdapter = new DetailAdapter(DetailActivity.this, mData, mPlayerService, mOffline);
 			mListView.setAdapter(mAdapter);
 			
 			if (mOffline) {
 				refreshOfflineUI();
 				mProgressDialog.dismiss();
 			} else {
 				mDataProvider.getAudiosBySerial(mSerial.getId(), new Callback(){
 		
 					@Override
 					public void success(Object result) {
 						List<AudioItem> list = (List<AudioItem>) result;
 						if (list.size() == 0)
 							return;
 						
 						mData.clear();
 						for (AudioItem o:list) {
 							o = mFileManager.updateByManager(o);
 							mData.add(o.getFileId());
 						}
 						mAdapter.notifyDataSetChanged();
 						mProgressDialog.dismiss();
 						mUIReady = true;
 					}
 		
 					@Override
 					public void fail(Exception e) {
 						Toast.makeText(DetailActivity.this, "网络错误，加载失败",
 							     Toast.LENGTH_SHORT).show();
 						mProgressDialog.dismiss();
 					}
 					
 				});
 			}
 			
 
 		}
 	
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			mPlayerService = null;
 		}
 	};
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		
 		MenuItem settingItem = menu.findItem(R.id.action_settings);
 		settingItem.setOnMenuItemClickListener(new OnSettingClickedListener(this));
 		
 		MenuItem downloadItem = menu.findItem(R.id.action_download);
 		downloadItem.setOnMenuItemClickListener(new OnMenuItemClickListener(){
 			@Override
 			public boolean onMenuItemClick(MenuItem item) {
 				Intent intent = new Intent(DetailActivity.this, DownloadingActivity.class);
 				startActivity(intent);
 				return true;
 			}
 		});
 		
 		mPlayItem = menu.findItem(R.id.action_play);
 		mPlayItem.setVisible(true);
 		
 		mPlayItem.setOnMenuItemClickListener(new OnMenuItemClickListener(){
 			@Override
 			public boolean onMenuItemClick(MenuItem item) {
 				long fileId = -1;
 				if (mPlayerService != null) {
 					fileId = mPlayerService.getNowPlayingId();
 				}
 				if (fileId != -1) {
 					Intent intent = new Intent(DetailActivity.this, MusicPlayerActivity.class);
 					intent.putExtra("fileId", fileId);
 					startActivity(intent);
 					return true;
 				}
 				Toast.makeText(DetailActivity.this, "没有播放内容", Toast.LENGTH_SHORT).show();
 				return false;
 			}
 		});
 		
 		
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	public void refreshOfflineUI() {
 		if (mOffline) {
 			mData.clear();
 			List<AudioItem> items = mFileManager.getAudioItems(mSerial.getId());
 			for (AudioItem item:items) {
 				if (item.getStatus() == AudioItem.Status.FINISHED)
 					mData.add(item.getFileId());
 			}
 			mHandler.post(new Runnable(){
 
 				@Override
 				public void run() {
					mProgressDialog.dismiss();
 					mAdapter.notifyDataSetChanged();
 				}
 				
 			});
 		}
 	}
 	
 	public void onPause() {
 		super.onPause();
 		MobclickAgent.onPause(this);
 		if (running != null)
 			running.clear();
 		running = null;
 	}
 	
 	public void onResume(){
 		super.onResume();
 		MobclickAgent.onResume(this);
 		this.refreshOfflineUI();
 		running = new WeakReference<DetailActivity>(this);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			this.finish();
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	@Override
 	public void onDestroy() {
 		mRefreshUI = false;
 		unbindService(mConn);
 		super.onDestroy();
 	}
 
 	
 	
 	private void updateDownloadData() {
 		long[] downloadids = new long[mData.size()];
 		Arrays.fill(downloadids, -1);
 		int i = 0;
 		for (Long id:mData) {
 			AudioItem item = mFileManager.getAudioItem(id);
 			if (item.getStatus() == AudioItem.Status.STARTED) {
 				downloadids[i++] = item.getDownloadId();
 			}
 		}
 		
 		DownloadManager.Query query = new DownloadManager.Query();
 		query.setFilterById(downloadids);
 		query.setFilterByStatus( DownloadManager.STATUS_PAUSED |
 				DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING);
 		Cursor c = mDownloadManager.query(query);
 		if (c != null) {
 			if (c.moveToFirst()) {
 				do {
 					int status =  c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
 					long id    = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID));
 					final long fileId = mFileManager.getIdByDownloadId(id);
 					long bytes = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
 					AudioItem aitem = mFileManager.getAudioItem(fileId);
 					if (aitem != null) {
 						aitem.setFinishedSize(bytes);
 						Log.d(TAG, fileId + " downloaded " + bytes + " percentage: " + (100.0f * bytes / aitem.getFileSize()));
 					}
 				} while(c.moveToNext());
 			}
 			c.close();
 		}
 		
 		
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 	    if(keyCode == KeyEvent.KEYCODE_MENU) {
 	    	LayoutInflater inflater = LayoutInflater.from(this);
 			View dialogView = inflater.inflate(R.layout.setting, null);
 			ListView listView = (ListView) dialogView.findViewById(R.id.setting_list);
 			listView.setAdapter(new SettingAdapter(this));
 			new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light))
 					.setTitle(R.string.action_settings)
 					.setView(dialogView)
 					.setPositiveButton(R.string.finish, null)
 					.show();
 			return true;
 	    }
 	    return super.onKeyDown(keyCode, event);
 	}
 	
 }
