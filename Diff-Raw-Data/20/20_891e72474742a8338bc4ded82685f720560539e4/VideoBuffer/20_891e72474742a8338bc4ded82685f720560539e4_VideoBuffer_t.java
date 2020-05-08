 /*
  * Copyright (C) 2013 yixia.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.examples.gg.twitchplayers;
 
 import io.vov.vitamio.LibsChecker;
 import io.vov.vitamio.MediaPlayer;
 import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
 import io.vov.vitamio.MediaPlayer.OnInfoListener;
 import io.vov.vitamio.widget.VideoView;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnSystemUiVisibilityChangeListener;
 import android.view.View.OnTouchListener;
 import android.view.WindowManager;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.examples.gg.R;
 
 public class VideoBuffer extends Activity implements OnInfoListener,
 		OnBufferingUpdateListener {
 
 	/**
 	 * TODO: Set the path variable to a streaming video URL or a local media
 	 * file path.
 	 */
 	private String path = "";
 
 	private Uri uri;
 	private VideoView mVideoView;
 	private ImageView qualityView;
 	private TextView mMsgView;
 	private boolean isStart;
 	private ProgressBar pb;
 	private TextView downloadRateView, loadRateView;
 	private Handler h;
 	private Handler h2;
 	private int uiOptions;
 	private int mLastSystemUiVis;
 	private String responseString;
 	private ArrayList<String> videoSources;
 	private OnInfoListener mInfoListener;
 	private OnBufferingUpdateListener mBufferListener;
 	// private PopupMenu popup;
 	private String channelName;
 	private Context mContext;
 	List<String> listItems = new ArrayList<String>();
 	private SharedPreferences prefs;
 	private String globalPath;
 	private Runnable mNavHider = new Runnable() {
 		@Override
 		public void run() {
 			View decorView = getWindow().getDecorView();
 			decorView.setSystemUiVisibility(uiOptions);
 			qualityView.setVisibility(View.GONE);
 		}
 
 	};
 
 	private Runnable mSettingHider = new Runnable() {
 		@Override
 		public void run() {
 			qualityView.setVisibility(View.GONE);
 		}
 
 	};
 
 	@Override
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		if (!LibsChecker.checkVitamioLibs(this))
 			return;
 		setContentView(R.layout.videobuffer);
 		mVideoView = (VideoView) findViewById(R.id.buffer);
 		qualityView = (ImageView) findViewById(R.id.qualitySwitch);
 		mMsgView = (TextView) findViewById(R.id.load_msg);
 		pb = (ProgressBar) findViewById(R.id.probar);
 
 		downloadRateView = (TextView) findViewById(R.id.download_rate);
 		loadRateView = (TextView) findViewById(R.id.load_rate);
 
 		mInfoListener = this;
 		mBufferListener = this;
 
 		// Initialize variables
 		globalPath = null;
 		responseString = null;
 		videoSources = new ArrayList<String>();
 
 		mContext = this;
 
 		// Getting the prefs
 		// prefs = this.getSharedPreferences("com.examples.gg",
 		// Context.MODE_PRIVATE);
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 
 		Intent intent = getIntent();
 		channelName = intent.getStringExtra("video");
 		// try {
 		// popup = new PopupMenu(VideoBuffer.this, qualityView);
 		// } catch (Exception e) {
 		// }
 
 		// Getting twitch sources
 		try {
 			channelName = URLEncoder.encode(
 					channelName.toLowerCase(Locale.ENGLISH), "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		// new MyAsyncTask().execute("http://usher.twitch.tv/select/"
 		// + channelName + ".json?nauthsig=&nauth=&allow_source=true");
 		String token_api = String.format(
 				"http://api.twitch.tv/api/channels/%s/access_token",
 				new Object[] { channelName });
 		new MyAsyncTask().execute(token_api);
 
 		mMsgView.setText("Loading channel data...");
 	}
 
 	@Override
 	public boolean onInfo(MediaPlayer mp, int what, int extra) {
 		switch (what) {
 		case MediaPlayer.MEDIA_INFO_BUFFERING_START:
 			if (mVideoView.isPlaying()) {
 				mVideoView.pause();
 				isStart = true;
 				mMsgView.setText("Loading video...");
 				pb.setVisibility(View.VISIBLE);
 				// downloadRateView.setVisibility(View.VISIBLE);
 				loadRateView.setVisibility(View.VISIBLE);
 
 			}
 			break;
 		case MediaPlayer.MEDIA_INFO_BUFFERING_END:
 			if (isStart) {
 				mVideoView.start();
 				pb.setVisibility(View.GONE);
 				downloadRateView.setVisibility(View.GONE);
 				loadRateView.setVisibility(View.GONE);
 				qualityView.setVisibility(View.GONE);
 				mMsgView.setVisibility(View.GONE);
 			}
 			break;
 		// case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
 		// downloadRateView.setText("" + extra + "kb/s" + "  ");
 		// break;
 		}
 		return true;
 	}
 
 	@Override
 	public void onBufferingUpdate(MediaPlayer mp, int percent) {
 		loadRateView.setText(percent + "%");
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		mVideoView.pause();
 
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		mVideoView.start();
 		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
 			try {
 				if (h != null) {
 					qualityView.setVisibility(View.VISIBLE);
 
 					h.removeCallbacks(mNavHider);
 					h.postDelayed(mNavHider, 3000);
 				}
 			} catch (Exception e) {
 			}
 		}
 
 	}
 
 	@SuppressLint("NewApi")
 	private void hideBars() {
 		try {
 			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
 				// only for HONEYCOMB and newer versions
 
 				View decorView = getWindow().getDecorView();
 				// Hide both the navigation bar and the status bar.
 				// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1
 				// and
 				// higher, but as
 				// a general rule, you should design your app to hide the status
 				// bar
 				// whenever you
 				// hide the navigation bar.
 
 				uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
 						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
 						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
 						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
 						| View.SYSTEM_UI_FLAG_FULLSCREEN;
 
 				decorView.setSystemUiVisibility(uiOptions);
 				qualityView.setVisibility(View.GONE);
 
 				decorView
 						.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
 
 							@Override
 							public void onSystemUiVisibilityChange(
 									int visibility) {
 								int diff = mLastSystemUiVis ^ visibility;
 								mLastSystemUiVis = visibility;
 								if ((diff & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0
 										&& (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
 									if (h != null) {
 										// Log.i("debug", "in system");
 										qualityView.setVisibility(View.VISIBLE);
 
 										h.removeCallbacks(mNavHider);
 										h.postDelayed(mNavHider, 3000);
 									}
 								}
 
 							}
 						});
 
 			} else {
 				// For android version 4.0 or lower
 
 				getWindow().setFlags(
 						WindowManager.LayoutParams.FLAG_FULLSCREEN,
 						WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 			}
 		} catch (Exception e) {
 		}
 
 	}
 
 	public class MyAsyncTask extends AsyncTask<String, String, String> {
 
 		protected MyAsyncTask() {
 
 		}
 
 		@Override
 		protected String doInBackground(String... uri) {
 			// Log.i("debug", "do in back");
 			HttpURLConnection conn = null;
 			try {
 
 				URL url = new URL(uri[0]);
 				conn = (HttpURLConnection) url.openConnection();
 
 				conn.setRequestMethod("GET");
 				conn.setReadTimeout(10000);
 
 				conn.setRequestProperty(
 						"User-Agent",
 						"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
 
 				InputStream is = conn.getInputStream();
 
 				int http_status = conn.getResponseCode();
 
 				// better check it first
 				if (http_status / 100 != 2) {
 					cancel(true);
 
 				}
 
 				responseString = getStringFromInputStream(is);
 				is.close();
 
 			} catch (Exception e) {
 				// Log.d("debug", conn.getErrorStream().toString());
 				// pb.setVisibility(View.GONE);
 				// mMsgView.setVisibility(View.VISIBLE);
 				// mMsgView.setText("Sorry, there is a problem connecting to the server.");
 				// e.printStackTrace();
 				cancel(true);
 
 			} finally {
 
 				// Log.d("AsyncDebug", "shutdown");
 				if (conn != null)
 					conn.disconnect();
 
 			}
 
 			if (responseString != null) {
 				String streams_api = null;
 				try {
 					// Get token and sig in JSON
 					JSONTokener jsonParser = new JSONTokener(
 							responseString.toString());
 					JSONObject wholeJson = (JSONObject) jsonParser.nextValue();
 					String token = wholeJson.getString("token");
 					String sig = wholeJson.getString("sig");
 					
 					Object aobj[] = {
 		                    "usher.twitch.tv", channelName, URLEncoder.encode(token, "UTF-8"), sig
 		                };
 					
 					streams_api = String.valueOf(new URL(String.format("http://%s/api/channel/hls/%s.m3u8?token=%s&sig=%s", aobj)));
 					
 					
 				} catch (Exception e) {
 
 				}
 				
 				if(streams_api != null){
 					try {
 						
 						URL url = new URL(streams_api);
 						conn = (HttpURLConnection) url.openConnection();
 	
 						conn.setRequestMethod("GET");
 						conn.setReadTimeout(10000);
 	
 						conn.setRequestProperty(
 								"User-Agent",
 								"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
 	
 						InputStream is = conn.getInputStream();
 	
 						int http_status = conn.getResponseCode();
 	
 						// better check it first
 						if (http_status / 100 != 2) {
 							cancel(true);
 	
 						}
 	
 						responseString = getStringFromInputStream2(is);
 						is.close();
 	
 					} catch (Exception e) {
 						cancel(true);
 	
 					} finally {
 	
 						// Log.d("AsyncDebug", "shutdown");
 						if (conn != null)
 							conn.disconnect();
 	
 					}
 				}
 				
 			}
 			return responseString;
 		}
 
 		@Override
 		protected void onCancelled() {
			errorView();
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			// Log.i("debug result", result);
 
 			// Checking the number of stream sources
 			if (videoSources.size() > 0) {
 				// Check user preferred quality
 				String pQuality = prefs.getString("preferredQuality", "none");
 				path = getSpecificSource(videoSources, pQuality);
 				if (path == null) {
 					// Set default quality to the medium one
 					path = getSpecificSource(videoSources, "medium");
 					if (path == null) {
 						// No medium stream, set to the first one
 						path = videoSources.get(0);
 					} else {
 						// Set default quality to medium
 						prefs.edit().putString("preferredQuality", "medium")
 								.commit();
 
 					}
 				}
 				globalPath = path;
 				// Adding stream sources to quality switcher.
 				addingMenuStreamSources();
 			}
 
 			// Setting up player
 			if (path == "") {
 				// Tell the user to provide a media file URL/path.
 				Toast.makeText(VideoBuffer.this, "No stream sources",
 						Toast.LENGTH_LONG).show();
				errorView();
 				return;
 			} else {
 				/*
 				 * Alternatively,for streaming media you can use
 				 * mVideoView.setVideoURI(Uri.parse(URLstring));
 				 */
 				uri = Uri.parse(path);
 				mVideoView.setVideoURI(uri);
 				// mVideoView.setMediaController(new MediaController(this));
 				mVideoView.requestFocus();
 				mVideoView.setOnInfoListener(mInfoListener);
 				mVideoView.setOnBufferingUpdateListener(mBufferListener);
 				h = new Handler();
 				h2 = new Handler();
 				mVideoView
 						.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
 							@Override
 							public void onPrepared(MediaPlayer mediaPlayer) {
 								// optional need Vitamio 4.0
 								mediaPlayer.setPlaybackSpeed(1.0f);
 							}
 						});
 
 				// Setting up touch listener for screen touch
 				mVideoView.setOnTouchListener(new OnTouchListener() {
 					@Override
 					public boolean onTouch(View arg0, MotionEvent arg1) {
 						qualityView.setVisibility(View.VISIBLE);
 						if (h2 != null) {
 
 							h2.removeCallbacks(mSettingHider);
 							h2.postDelayed(mSettingHider, 3000);
 						}
 						return true;
 					}
 				});
 
 				// Setting up click listener for source switcher
 				qualityView.setOnClickListener(new OnClickListener() {
 
 					@SuppressLint("NewApi")
 					@Override
 					public void onClick(View v) {
 						try {
 							// // Inflating the Popup using xml file
 							// popup.getMenuInflater().inflate(R.menu.popup_menu,
 							// popup.getMenu());
 							//
 							// // registering popup with OnMenuItemClickListener
 							// popup.setOnMenuItemClickListener(new
 							// PopupMenu.OnMenuItemClickListener() {
 							// public boolean onMenuItemClick(MenuItem item) {
 							// Toast.makeText(VideoBuffer.this,
 							// "Quality: " + item.getTitle(),
 							// Toast.LENGTH_SHORT).show();
 							//
 							// // Reloading the stream according to the
 							// // selected source
 							// String mTitle = (String) item.getTitle();
 							//
 							// path = getSpecificSource(videoSources,
 							// mTitle);
 							// if (path != null) {
 							// uri = Uri.parse(path);
 							// mVideoView.setVideoURI(uri);
 							//
 							// // Set user preferred quality
 							// prefs.edit()
 							// .putString("preferredQuality",
 							// mTitle).commit();
 							// }
 							//
 							// return true;
 							// }
 							// });
 							//
 							// popup.show();// showing popup menu
 
 							openDialog();
 
 						} catch (Exception e) {
 						}
 
 					}
 				});
 
 				hideBars();
 
 			}
 		}
 
 		// Open video quality chooser
 		private void openDialog() {
 			if (listItems.size() > 0) {
 				// Open a alert dialog
 				final CharSequence[] sources_radio = listItems
 						.toArray(new CharSequence[listItems.size()]);
 
 				String mq = prefs.getString("preferredQuality", "none");
 				final int mIndex = getQualityIndex(mq, sources_radio);
 				// Log.i("debug", Integer.toString(mIndex));
 
 				new AlertDialog.Builder(mContext)
 						.setSingleChoiceItems(sources_radio, mIndex, null)
 						.setPositiveButton("OK",
 								new DialogInterface.OnClickListener() {
 									public void onClick(DialogInterface dialog,
 											int whichButton) {
 										dialog.dismiss();
 
 										int selectedPosition = ((AlertDialog) dialog)
 												.getListView()
 												.getCheckedItemPosition();
 										if (selectedPosition != -1) {
 											Toast.makeText(
 													VideoBuffer.this,
 													"Quality: "
 															+ sources_radio[selectedPosition],
 													Toast.LENGTH_SHORT).show();
 											if (selectedPosition != mIndex) {
 												// Reloading the stream
 												// according to the
 												// selected source
 												String mTitle = (String) sources_radio[selectedPosition];
 
 												path = getSpecificSource(
 														videoSources, mTitle);
 												if (path != null) {
 													uri = Uri.parse(path);
 													mVideoView.setVideoURI(uri);
 
 													// Set user
 													// preferred quality
 													prefs.edit()
 															.putString(
 																	"preferredQuality",
 																	mTitle)
 															.commit();
 												}
 											}
 										}
 									}
 								}).show();
 
 			} else {
 				Toast.makeText(VideoBuffer.this, "No other sources",
 						Toast.LENGTH_SHORT).show();
 			}
 
 		}
 
 		private int getQualityIndex(String quality, CharSequence[] mRadios) {
 			for (int i = 0; i < mRadios.length; i++) {
 				if (mRadios[i].equals(quality)) {
 					return i;
 				}
 			}
 			return -1;
 		}
 
 		private void addingMenuStreamSources() {
 			try {
 				// Menu mMenu = popup.getMenu();
 				for (int i = 0; i < videoSources.size(); i++) {
 					String mUri = videoSources.get(i);
 					if (mUri.contains("source")) {
 						// Adding menu items programmly
 						// mMenu.add(0, 0, 0, "source");
 						listItems.add("source");
 					} else if (mUri.contains("high")) {
 						// mMenu.add(0, 1, 1, "high");
 						listItems.add("high");
 					} else if (mUri.contains("medium")) {
 						// mMenu.add(0, 2, 2, "medium");
 						listItems.add("medium");
 					} else if (mUri.contains("low")) {
 						// mMenu.add(0, 3, 3, "low");
 						listItems.add("low");
 					} else if (mUri.contains("mobile")) {
 						// mMenu.add(0, 4, 4, "mobile");
 						listItems.add("mobile");
 					}
 				}
 			} catch (Exception e) {
 			}
 		}
 
 		private String getStringFromInputStream(InputStream is) {
 			// Log.i("debug", "in get");
 			BufferedReader br = null;
 			StringBuilder sb = new StringBuilder();
 
 			String line;
 			try {
 
 				br = new BufferedReader(new InputStreamReader(is));
 				while ((line = br.readLine()) != null) {
 					sb.append(line);
 				}
 
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 				if (br != null) {
 					try {
 						br.close();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 
 			return sb.toString();
 
 		}
 		
 		private String getStringFromInputStream2(InputStream is) {
 			// Log.i("debug", "in get");
 			BufferedReader br = null;
 			StringBuilder sb = new StringBuilder();
 
 			String line;
 			try {
 
 				br = new BufferedReader(new InputStreamReader(is));
 				while ((line = br.readLine()) != null) {
 					// pick up the video sources
 					if (line.contains("http")) {
 						videoSources.add(line);
 						// Log.i("debug", "source: " + line);
 					}
 					sb.append(line);
 				}
 
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 				if (br != null) {
 					try {
 						br.close();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 
 			return sb.toString();
 
 		}
 	}
 	
 	
 
 	// Return the stream source path according to given quality
 	private String getSpecificSource(ArrayList<String> al, String key) {
 		for (int i = 0; i < al.size(); i++) {
 			if (al.get(i).contains(key)) {
 				return al.get(i);
 			}
 		}
 		return null;
 	}
	
	private void errorView(){
		pb.setVisibility(View.GONE);
		mMsgView.setVisibility(View.VISIBLE);
		mMsgView.setText("Sorry, there is a problem connecting to the server.");
	}
 
 }
