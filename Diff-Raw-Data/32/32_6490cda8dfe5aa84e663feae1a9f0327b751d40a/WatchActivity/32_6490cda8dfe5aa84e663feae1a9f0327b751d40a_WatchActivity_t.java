 package com.norman.apps;
 
 import java.io.IOException;
 import java.util.Random;
 import java.util.Stack;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.app.WallpaperManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.adview.AdViewInterface;
 import com.adview.AdViewLayout;
 import com.adview.AdViewTargeting;
 import com.adview.AdViewTargeting.RunMode;
 
 public class WatchActivity extends Activity implements AdViewInterface {
 
 	private final Handler mHandler = new Handler();
 	private ImageView mImageView;
 	private TextView mBtnPrev;
 	private TextView mBtnNext;
 	private TextView mBtnDisp;
	// private LinearLayout mClockLayout;
 
 	private final String TAG = "WatchActivity";
 	public final static int INVALID_PIC_INDEX = -1;
 	private final Random mRandom = new Random(System.currentTimeMillis());
 
 	private int iPicIndex = INVALID_PIC_INDEX;
 	private boolean bKeyBackIn2Sec = false;
 	private final Stack<Integer> sPicHistory = new Stack<Integer>();
 	private GestureDetector mGestureDetector;
 	private ProgressDialog mProcessDialog;
 	private SharedPreferences mSharedPref;
 
 	// 采用反射运行时动态读取图片，在res/raw文件目录下按数组创建对应文件名
 	final static String[] PICS = {"m1", "m2", "m3", "m4"};
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		Log.d(TAG, "onCreate()");
 		super.onCreate(savedInstanceState);
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.main);
 		mImageView = (ImageView) findViewById(R.id.picView);
 		mBtnPrev = (TextView) findViewById(R.id.btnPrev);
 		mBtnNext = (TextView) findViewById(R.id.btnNext);
 		mBtnDisp = (TextView) findViewById(R.id.btnDisp);
		// mClockLayout = (LinearLayout) findViewById(R.id.clockLayout);
 		mSharedPref = getSharedPreferences("iWatch", Context.MODE_PRIVATE);
 		// Editor edit = mSharedPref.edit();
 		// edit.remove("CurPicIndex").commit();
 
 		if (sPicHistory.empty()) {
 			mBtnPrev.setEnabled(false);
 		}
 
 		LinearLayout layout = (LinearLayout) findViewById(R.id.adLayout);
 		if (layout == null) {
 			return;
 		}
 
 		/* 下面两行只用于测试,完成后一定要去掉,参考文挡说明 */
 		// AdViewTargeting.setUpdateMode(UpdateMode.EVERYTIME); // 保证每次都从服务器取配置
 		AdViewTargeting.setRunMode(RunMode.NORMAL); // 保证所有选中的广告公司都为测试状态
 		/* 下面这句方便开发者进行发布渠道统计,详细调用可以参考java doc */
 		// AdViewTargeting.setChannel(Channel.GOOGLEMARKET);
 		AdViewLayout adViewLayout = new AdViewLayout(this,
 				"SDK20122309480217x9sp4og4fxrj2ur");
 		adViewLayout.setAdViewInterface(this);
 		layout.addView(adViewLayout);
 		layout.invalidate();
 		setupButtons();
 
 		mGestureDetector = new GestureDetector(this, new MyGestureListener());
 		OnTouchListener rootListener = new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				mGestureDetector.onTouchEvent(event);
 				return true;
 			}
 		};
 		mImageView.setOnTouchListener(rootListener);
 
 		mHandler.removeCallbacks(mUpdateImageView);
 	}
 
	// private final Runnable mUpdateTimeTask = new Runnable() {
	// @Override
	// public void run() {
	// mClockLayout.invalidate();
	// mHandler.postDelayed(mUpdateTimeTask, 100);
	// }
	// };
 
 	private final Runnable mUpdateKeyBackState = new Runnable() {
 		@Override
 		public void run() {
 			bKeyBackIn2Sec = false;
 			mHandler.removeCallbacks(mUpdateKeyBackState);
 		}
 	};
 
 	private final Runnable mUpdateImageView = new Runnable() {
 		@Override
 		public void run() {
 			try {
 				Bitmap bm = BitmapFactory.decodeResource(getResources(),
 						ImageUtil.getImage(PICS[iPicIndex]));
 				mImageView.setImageBitmap(bm);
 
 			} catch (Exception e) {
 				e.printStackTrace();
 
 			} finally {
 				mHandler.removeCallbacks(mUpdateImageView);
 			}
 		}
 	};
 
 	@Override
 	public void onStart() {
 		// Log.d(TAG, "onStart()");
 		super.onStart();
 	}
 
 	@Override
 	public void onResume() {
 		// Log.d(TAG, "onResume()");
 		super.onResume();
 	}
 
 	@Override
 	public void onPause() {
 		// Log.d(TAG, "onPause()");
 		super.onPause();
 	}
 
 	@Override
 	public void onStop() {
 		// Log.d(TAG, "onStop()");
 		super.onStop();
 	}
 
 	@Override
 	public void onDestroy() {
 		// Log.d(TAG, "onDestroy()");
 		super.onDestroy();
 	}
 
 	private void setupButtons() {
 
 		mBtnPrev.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 
 				iPicIndex = sPicHistory.pop();
 				Log.d(TAG, "Showing previous picture id " + iPicIndex);
 
 				mHandler.postDelayed(mUpdateImageView, 100);
 
 				if (sPicHistory.empty()) {
 					mBtnPrev.setEnabled(false);
 					Editor edit = mSharedPref.edit();
 					edit.remove("CurPicIndex").commit();
 				}
 			}
 		});
 
 		mBtnNext.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				if (iPicIndex == INVALID_PIC_INDEX) {
 					setClockVisibility(false);
 					mBtnNext.setText(getResources().getString(R.string.strNext));
 				} else {
 					sPicHistory.push(iPicIndex);
 				}
 
 				int tmpIndex = INVALID_PIC_INDEX;
 				do {
 					tmpIndex = mRandom.nextInt(PICS.length);
 				} while (tmpIndex == iPicIndex);
 
 				iPicIndex = tmpIndex;
 				Log.d(TAG, "Showing new picture id " + iPicIndex);
 
 				mHandler.postDelayed(mUpdateImageView, 100);
 
 				if (!sPicHistory.empty()) {
 					Log.d(TAG, "Picture stack: " + sPicHistory);
 					mBtnPrev.setEnabled(true);
 				}
 
 			}
 		});
 
 		mBtnDisp.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				// hide mainLayout only leave background image
 				if (getMLVisibility()) {
 					setMLVisibility(false);
 					getWindow().setFlags(
 							WindowManager.LayoutParams.FLAG_FULLSCREEN,
 							WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 				}
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main_menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		menu.findItem(R.id.menu_toggle_clock).setTitle(
 				getClockVisibility()
 						? R.string.hide_clock
 						: R.string.show_clock);
 
 		// Hide settings in current version
 		menu.findItem(R.id.menu_settings).setVisible(false);
 
 		if (iPicIndex == INVALID_PIC_INDEX) {
 			menu.findItem(R.id.menu_set_wallpaper).setEnabled(false);
 		} else {
 			menu.findItem(R.id.menu_set_wallpaper).setEnabled(true);
 		}
 
 		return super.onPrepareOptionsMenu(menu);
 	}
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_toggle_clock :
 				setClockVisibility(!getClockVisibility());
 				break;
 
 			case R.id.menu_settings :
 				startActivity(new Intent(this, Settings.class));
 				break;
 
 			case R.id.menu_set_livewallpaper :
 				Editor myEdit = mSharedPref.edit();
 				myEdit.putInt("CurPicIndex", iPicIndex).commit();
 
 				Toast.makeText(this, getString(R.string.help_livewallpaper),
 						Toast.LENGTH_SHORT).show();
 				Intent intent = new Intent();
 				intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
 				startActivity(intent);
 				break;
 
 			case R.id.menu_set_wallpaper :
 				mProcessDialog = ProgressDialog.show(this,
 						getString(R.string.set_wallpaper_title),
 						getString(R.string.set_wallpaper_msg), true);
 
 				new Thread() {
 					@Override
 					public void run() {
 						try {
 							setWallpaper();
 						} catch (Exception e) {
 							e.printStackTrace();
 						} finally {
 							mProcessDialog.dismiss();
 						}
 					}
 				}.start();
 
 				break;
 
 			default :
 
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private boolean getLayoutVisibility(int id) {
 		LinearLayout layout = (LinearLayout) findViewById(id);
 		return layout.getVisibility() == View.VISIBLE;
 	}
 
 	private void setLayoutVisibility(int id, boolean bVisibility) {
 		LinearLayout layout = (LinearLayout) findViewById(id);
 		layout.setVisibility(bVisibility ? View.VISIBLE : View.GONE);
 	}
 
 	private boolean getClockVisibility() {
 		return getLayoutVisibility(R.id.clockLayout);
 	}
 
 	private void setClockVisibility(boolean bVisibility) {
 		setLayoutVisibility(R.id.clockLayout, bVisibility);
 	}
 
 	private boolean getMLVisibility() {
 		return getLayoutVisibility(R.id.mainLayout);
 	}
 
 	private void setMLVisibility(boolean bVisibility) {
 		setLayoutVisibility(R.id.mainLayout, bVisibility);
 	}
 
 	private void setWallpaper() {
 		try {
 			WallpaperManager.getInstance(this).setBitmap(
 					((BitmapDrawable) mImageView.getDrawable()).getBitmap());
 
 		} catch (IOException e) {
 			Log.e(TAG, "Failed to set wallpaper!");
 		}
 	}
 	private class MyGestureListener
 			extends
 				GestureDetector.SimpleOnGestureListener {
 		@Override
 		public boolean onScroll(MotionEvent e1, MotionEvent e2,
 				float distanceX, float distanceY) {
 			if (mImageView.getScaleType() == ScaleType.CENTER) {
 				mImageView.scrollBy((int) distanceX, (int) distanceY);
 			}
 			return true;
 		}
 
 		@Override
 		public boolean onDoubleTap(MotionEvent e) {
 			if (mImageView.getScaleType() == ScaleType.CENTER) {
 				mImageView.setScaleType(ScaleType.CENTER_INSIDE);
 				mImageView.scrollTo(0, 0);
 			} else {
 				mImageView.setScaleType(ScaleType.CENTER);
 			}
 			return true;
 		}
 
 		@Override
 		public boolean onSingleTapConfirmed(MotionEvent e) {
 			if (!getMLVisibility()) {
 				setMLVisibility(true);
 				getWindow().clearFlags(
 						WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 			}
 			return true;
 		}
 	}
 
 	@Override
 	public void onClickAd() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onDisplayAd() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean onKeyUp(int keycode, KeyEvent event) {
 		switch (keycode) {
 
 			case KeyEvent.KEYCODE_BACK :
 				// alert user when key_back is pressed
 				// new AlertDialog.Builder(this)
 				// .setMessage(getString(R.string.exit_msg))
 				// .setPositiveButton(getString(R.string.ok),
 				// new DialogInterface.OnClickListener() {
 				// @Override
 				// public void onClick(DialogInterface dialog,
 				// int whichButton) {
 				// finish();
 				// }
 				// })
 				// .setNegativeButton(getString(R.string.cancel),
 				// new DialogInterface.OnClickListener() {
 				// @Override
 				// public void onClick(DialogInterface dialog,
 				// int whichButton) {
 				//
 				// }
 				// }).create().show();
 
 				if (!bKeyBackIn2Sec) {
 					Toast.makeText(this, getString(R.string.exit_toast),
 							Toast.LENGTH_SHORT).show();
 					bKeyBackIn2Sec = true;
 					mHandler.postDelayed(mUpdateKeyBackState, 2000);
 				} else {
 					finish();
 				}
 
 				return false;
 		}
 
 		return super.onKeyUp(keycode, event);
 	}
 }
