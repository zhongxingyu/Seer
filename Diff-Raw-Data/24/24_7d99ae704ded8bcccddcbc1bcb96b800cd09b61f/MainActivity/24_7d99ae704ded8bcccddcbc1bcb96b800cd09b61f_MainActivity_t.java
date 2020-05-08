 package com.yugy.qianban.activity;
 
 import java.io.ByteArrayInputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.DecelerateInterpolator;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.BaseAdapter;
 import android.widget.Gallery.LayoutParams;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.SeekBar;
 import android.widget.TextView;
 
 import com.fedorvlasov.lazylist.ImageLoader;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.JsonHttpResponseHandler;
 import com.yugy.qianban.R;
 import com.yugy.qianban.asisClass.Conf;
 import com.yugy.qianban.asisClass.Func;
 import com.yugy.qianban.asisClass.FuncInt;
 import com.yugy.qianban.asisClass.LrcFormat;
 import com.yugy.qianban.asisClass.LrcProcesser;
 import com.yugy.qianban.asisClass.Rotate3DAnimation;
 import com.yugy.qianban.asisClass.Song;
 import com.yugy.qianban.sdk.Douban;
 import com.yugy.qianban.sdk.LRC;
 import com.yugy.qianban.service.MusicService;
 import com.yugy.qianban.widget.CoverFlow;
 import com.yugy.qianban.widget.LrcView;
 import com.yugy.qianban.widget.Titlebar;
 
 @SuppressWarnings("deprecation")
 public class MainActivity extends Activity {
 
 	private Titlebar titlebar;
 	private CoverFlow coverFlow;
 	private ImageButton previous;
 	private ImageButton play;
 	private ImageButton next;
 	private SeekBar seekBar;
 	private TextView song;
 	private TextView author;
 	private LrcView lrc;
 	private RelativeLayout coverFlowLayout;
 	private ProgressBar progressBar;
 
 	// private int currentSongId;
 
 	private ArrayList<Song> albums;
 	private Douban douban;
 	private MusicService musicService;
 	private ServiceConnection serviceConnection;
 	private ImageLoader imageLoader;
 	private JSONObject catelog;
 	private AlbumAdapter albumAdapter;
 	String catalogId = "1";
 
 	private String lrcUrl; // 歌词路径
 	private String lrcString; // 歌词字符串
 	private LrcFormat lrcFormat; // 转换后的歌词格式
 	// private Thread thread = new Thread();
 	private UIUpdateThread thread;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		init();
 		getCatelog();
 	}
 
 	private void init() {
 		Intent intent = new Intent(this, MusicService.class);
 		serviceConnection = new ServiceConnection() {
 
 			@Override
 			public void onServiceDisconnected(ComponentName arg0) {
 				musicService = null;
 			}
 
 			@Override
 			public void onServiceConnected(ComponentName arg0, IBinder arg1) {
 				musicService = ((MusicService.LocalBinder) arg1).getService();
 				musicService.setUIController(new UIController());
 				setButtonClick();
 				initCoverFlow();
 				musicService.setSeekbar(seekBar);
 				getSongs(catalogId);
 			}
 		};
 		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
 
 		titlebar = (Titlebar) findViewById(R.id.main_titlebar);
 		titlebar.setTitle("Qianban");
 		titlebar.setLeftButtonIcon(R.drawable.catelog_button_icon);
 		titlebar.setRightButtonIcon(R.drawable.setting_button_icon);
 
 		coverFlow = (CoverFlow) findViewById(R.id.main_coverflow);
 
 		previous = (ImageButton) findViewById(R.id.main_lastsong);
 		play = (ImageButton) findViewById(R.id.main_play);
 		next = (ImageButton) findViewById(R.id.main_nextsong);
 
 		song = (TextView) findViewById(R.id.main_infosong);
 		author = (TextView) findViewById(R.id.main_infoauthor);
 
 		progressBar = (ProgressBar) findViewById(R.id.main_coverflowwaiting);
 
 		lrc = (LrcView) findViewById(R.id.main_lrc);
 		lrc.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				applyRotation(180, 90);
 			}
 		});
 
 		coverFlowLayout = (RelativeLayout) findViewById(R.id.main_coverflowlayout);
 
 		douban = new Douban(this);
 		imageLoader = new ImageLoader(this, "qianban/cache", getResources()
 				.getDrawable(R.drawable.stub));
 		albums = new ArrayList<Song>();
 		albumAdapter = new AlbumAdapter();
 
 		seekBar = (SeekBar) findViewById(R.id.main_seekbar);
 
 		thread = new UIUpdateThread();
 	}
 
 	private void initCoverFlow() {
 		coverFlow.setAdapter(albumAdapter);
 		coverFlow.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				song.setText(albums.get(arg2).title);
 				author.setText(albums.get(arg2).author + " - "
 						+ albums.get(arg2).albumName);
 				if (arg2 > musicService.currentSongId) {
 					if (thread.isAlive()) {
 						thread.stopThread();
 						thread.interrupt();
 					}
 					lrc.clear();
 					musicService.nextSongWithoutFlip();
 				} else if (arg2 < musicService.currentSongId) {
 					if (thread.isAlive()) {
 						thread.stopThread();
 						thread.interrupt();
 					}
 					lrc.clear();
 					musicService.lastSongWithoutFlip();
 				}
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 
 			}
 		});
 		coverFlow.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				if (arg2 == musicService.currentSongId) {
 					applyRotation(0, 90);
 				}
 			}
 		});
 		musicService.setCoverFlow(coverFlow);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		if (requestCode == Conf.REQUEST_CATALOG_CODE
 				&& resultCode == Conf.REQUEST_CATALOG_OK) {
 			catalogId = data.getStringExtra("id");
 			albums = new ArrayList<Song>();
 			getSongs(catalogId);
 		}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	private void setButtonClick() {
 		titlebar.setLeftClick(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 
 				if (catelog != null) {
 					Intent intent = new Intent(MainActivity.this,
 							CatalogActivity.class);
 					intent.putExtra("json", catelog.toString());
 					startActivityForResult(intent, Conf.REQUEST_CATALOG_CODE);
 				}
 			}
 		});
 
 		previous.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				musicService.playLastSong();
 			}
 		});
 		next.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				musicService.playNextSong();
 			}
 		});
 		musicService.setPlayButton(play);
 	}
 
 	private void getCatelog() {
 		douban.getCatalog(new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONObject response) {
 
 				catelog = response;
 				super.onSuccess(response);
 			}
 		});
 	}
 
 	private void getSongs(String id) {
 		musicService.resetMediaPlayer();
 		douban.getSongs(id, new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONArray response) {
 
 				Song song;
 				for (int i = 0; i < response.length(); i++) {
 					song = new Song();
 					try {
 						song.parse(response.getJSONObject(i));
 						albums.add(song);
 					} catch (JSONException e) {
 
 						e.printStackTrace();
 					}
 				}
 				albumAdapter.notifyDataSetChanged();
 				coverFlow.setSelection(0);
 				MainActivity.this.song.setText(albums.get(0).title);
 				author.setText(albums.get(0).author + " - "
 						+ albums.get(0).albumName);
 				if (thread.isAlive()) {
 					thread.stopThread();
 					thread.interrupt();
 				}
 				lrc.clear();
 				musicService.setSongList(albums);
 				super.onSuccess(response);
 			}
 
 			@Override
 			public void onFailure(Throwable error, String content) {
 
 				super.onFailure(error, content);
 			}
 		});
 	}
 
 	private class AlbumAdapter extends BaseAdapter {
 
 		@Override
 		public int getCount() {
 
 			return albums.size();
 		}
 
 		@Override
 		public Object getItem(int arg0) {
 
 			final ImageView image = new ImageView(MainActivity.this);
 			LayoutParams layoutParams = new LayoutParams(FuncInt.dp(
 					MainActivity.this, 140), FuncInt.dp(MainActivity.this, 140));
 			image.setLayoutParams(layoutParams);
 			imageLoader.DisplayImage(albums.get(arg0).albumCoverUrl, image);
 			return image;
 		}
 
 		@Override
 		public long getItemId(int arg0) {
 
 			return arg0;
 		}
 
 		@Override
 		public View getView(int arg0, View arg1, ViewGroup arg2) {
 
 			ImageView image;
 			if (arg1 == null) {
 				image = new ImageView(MainActivity.this);
 				LayoutParams layoutParams = new LayoutParams(FuncInt.dp(
 						MainActivity.this, 140), FuncInt.dp(MainActivity.this,
 						140));
 				image.setLayoutParams(layoutParams);
 			} else {
 				image = (ImageView) arg1;
 			}
 			imageLoader.DisplayImage(albums.get(arg0).albumCoverUrl, image);
 			return image;
 		}
 
 	}
 
 	private void applyRotation(float start, float end) {
 		final float centerX = coverFlowLayout.getWidth() / 2f;
 		final float centerY = coverFlowLayout.getHeight() / 2f;
 		Rotate3DAnimation rotate3dAnimation = new Rotate3DAnimation(start, end,
				centerX, centerY, 310f, true, false);
 		rotate3dAnimation.setDuration(500);
 		rotate3dAnimation.setFillAfter(true);
 		rotate3dAnimation.setInterpolator(new AccelerateInterpolator());
 		rotate3dAnimation.setAnimationListener(new AnimationListener() {
 
 			@Override
 			public void onAnimationStart(Animation arg0) {
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation arg0) {
 			}
 
 			@Override
 			public void onAnimationEnd(Animation arg0) {
 				coverFlowLayout.post(new Runnable() {
 					@Override
 					public void run() {
 						Rotate3DAnimation animation;
 						if (coverFlow.isShown()) {
 							coverFlow.setVisibility(View.GONE);
 							song.setVisibility(View.GONE);
 							author.setVisibility(View.GONE);
 							lrc.setVisibility(View.VISIBLE);
 							animation = new Rotate3DAnimation(90, 180, centerX,
									centerY, 310f, false, true);
 						} else {
 							coverFlow.setVisibility(View.VISIBLE);
 							song.setVisibility(View.VISIBLE);
 							author.setVisibility(View.VISIBLE);
 							lrc.setVisibility(View.GONE);
 							animation = new Rotate3DAnimation(90, 0, centerX,
									centerY, 310f, false, false);
 						}
 						animation.setDuration(500);
 						animation.setFillAfter(true);
 						animation.setInterpolator(new DecelerateInterpolator());
 						coverFlowLayout.startAnimation(animation);
 					}
 				});
 			}
 		});
 		coverFlowLayout.startAnimation(rotate3dAnimation);
 	}
 
 	public class UIController {
 
 		public void showProgressBar() {
 			progressBar.setVisibility(View.VISIBLE);
 		}
 
 		public void hideProgressBar() {
 			progressBar.setVisibility(View.GONE);
 		}
 
 		public void setCoverFlowSelectable(boolean a) {
 			coverFlow.setTouchable(a);
 			previous.setClickable(a);
 			next.setClickable(a);
 			play.setClickable(a);
 		}
 
 		public void loadLrc(int i) {
 			downloadLrc(albums.get(i).title, albums.get(i).author);
 		}
 	}
 
 	// 下载并返回歌词字符串
 	public void downloadLrc(String songName, String authorName) {
 		Func.log("http://box.zhangmen.baidu.com/x?op=12&count=1&title="
 				+ songName + "$$" + authorName + "$$$$");
 		songName = songName.replaceAll(" ", "%20");
 		authorName = authorName.replaceAll(" ", "%20");
 		songName = URLEncoder.encode(songName);
 		authorName = URLEncoder.encode(authorName);
 		// 取第i首歌词列表（即id=i的歌词列表）,并获得列表中第一个路径
 		LRC.searchLrc(songName, authorName, new AsyncHttpResponseHandler() {
 			@Override
 			public void onSuccess(String response) {
 				Pattern p = Pattern.compile("<lrcid>([0-9]+)</lrcid>");
 				Matcher m = p.matcher(response);
 				if (m.find()) {
 					lrcUrl = m.group();
 					lrcUrl = lrcUrl.substring(7, lrcUrl.length() - 8);
 					lrcUrl = "http://box.zhangmen.baidu.com/bdlrc/"
 							+ Integer.parseInt(lrcUrl) / 100 + "/"
 							+ Integer.parseInt(lrcUrl) + ".lrc";
 					Func.log(lrcUrl);
 					// 通过歌词路径获取歌词
 					LRC.downloadLrc(lrcUrl, new AsyncHttpResponseHandler() {
 						@Override
 						public void onSuccess(String content) {
 							lrcString = content;
 							changeFormat();
 							super.onSuccess(content);
 						}
 					});
 				}
 				super.onSuccess(response);
 			}
 		});
 	}
 
 	// 将歌词字符串转换成LrcFormat
 	public void changeFormat() {
 		LrcProcesser pro = new LrcProcesser();
 		try {
 			lrcFormat = pro.process(new ByteArrayInputStream(lrcString
 					.getBytes("UTF-8")));
 			lrc.setLrc(lrcFormat);
 			Func.log(lrc.getIndex() + "");
 			thread = new UIUpdateThread();
 			thread.start();
 			/*
 			 * thread = new Thread(new UIUpdateThread()); thread.start();
 			 */
 			for (int i = 0; i < lrcFormat.getIndex(); i++) {
 				Func.log(lrcFormat.getTime(i) / 60000 + ":"
 						+ lrcFormat.getTime(i) % 60000 / 1000 + "  "
 						+ lrcFormat.getLrc(i));
 			}
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 	}
 
 	class UIUpdateThread extends Thread {
 		private long time = 1; // 开始 的时间，不能为零，否则前面几句歌词没有显示出来
 		private boolean _run = true;
 
 		public void stopThread() {
 			_run = false;
 		}
 
 		public void run() {
 			while (_run) {
 				if (musicService.isPlaying())
 					break;
 			}
 			while (_run) {
 				if(musicService.isPlaying()){
 					long sleeptime = lrc.updateIndexReturnSleeptime(time);
 					time += sleeptime;
 					mHandler.post(mUpdateResults);
 					if (sleeptime == -1)
 						return;
 					try {
 						Thread.sleep(sleeptime);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 
 	Handler mHandler = new Handler();
 	Runnable mUpdateResults = new Runnable() {
 		public void run() {
 			lrc.invalidate(); // 更新视图
 		}
 	};
 
 	@Override
 	protected void onDestroy() {
 		if(thread != null){
 			thread.stopThread();
 		}
 		unbindService(serviceConnection);
 		super.onDestroy();
 	};
 }
