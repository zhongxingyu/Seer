 package com.adtworker.mail;
 
 import java.io.IOException;
 import java.sql.Time;
 import java.util.Random;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.app.WallpaperManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.drawable.BitmapDrawable;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.telephony.TelephonyManager;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.adtworker.mail.ImageManager.IMAGE_PATH_TYPE;
 import com.adview.AdViewInterface;
 import com.adview.AdViewLayout;
 import com.adview.AdViewTargeting;
 import com.adview.AdViewTargeting.RunMode;
 
 public class WatchActivity extends Activity implements AdViewInterface {
 
 	private final Handler mHandler = new Handler();
 	private int mImageViewCurrent = 0;
 	private final ImageView[] mImageViews = new ImageView[2];
 	private final Random mRandom = new Random(System.currentTimeMillis());
 	private TextView mBtnPrev;
 	private TextView mBtnNext;
 	private TextView mBtnDisp;
 	private TextView mBtnClock;
 	private LinearLayout mAdLayout;
 	private ViewGroup mClockLayout;
 	private View mClock = null;
 
 	private int mAnimationIndex = -1;
 	private Animation[] mSlideShowInAnimation;
 	private Animation[] mSlideShowOutAnimation;
 
 	private final String TAG = "WatchActivity";
 	public final static int CLICKS_TO_HIDE_AD = 1;
 	public final static String APP_FOLDER = "/data/com.adtworker.mail";
 	public final static String PIC_FOLDER = "/iWatch";
 	private final ScaleType DEFAULT_SCALETYPE = ScaleType.FIT_CENTER;
 	private final ScaleType ALTER_SCALETYPE = ScaleType.CENTER_INSIDE;
 	// private final ScaleType ALTER_SCALETYPE = ScaleType.CENTER_CROP;
 	private ImageView.ScaleType mScaleType = DEFAULT_SCALETYPE;
 	private ImageManager mImageManager;
 
 	private boolean bStarted = false;
 	private int mFace = -1;
 	private int mStep = 1;
 	private int iAdClick = 0;
 	private boolean bKeyBackIn2Sec = false;
 	private boolean bLargePicLoaded = false;
 	private GestureDetector mGestureDetector;
 	private GestureDetector mClockGestureDetector;
 	private ProgressDialog mProcessDialog;
 	public ProgressBar mProgressBar;
 	public ProgressBar mProgressIcon;
 	private SharedPreferences mSharedPref;
 
 	final static String PREFERENCES = "iWatch";
 	final static String PREF_CLOCK_FACE = "face";
 	final static String PREF_PIC_CODE = "pic_code";
 	final static String PREF_LAST_CODE = "last_code";
 	final static String PREF_FULL_SCR = "full_screen";
 	final static String PREF_AUTOHIDE_CLOCK = "autohide_clock";
 	final static String PREF_AUTOHIDE_AD = "autohide_ad";
 	final static String PREF_AUTOHIDE_SB = "autohide_sb";
 	final static String PREF_AD_CLICK_TIME = "ad_click_time";
 	final static String PREF_BOSS_KEY = "boss_key";
 	final static String PREF_PIC_FULL_FILL = "pic_fullfill";
 	final static String PREF_WP_FULL_FILL = "wp_fullfill";
 	final static String PREF_SLIDE_ANIM = "slide_anim";
 
 	private final static int[] CLOCKS = {R.layout.clock_no_dial,
 			R.layout.clock_appwidget, R.layout.clock_basic_bw,
 			R.layout.clock_basic_bw1, R.layout.clock_basic_bw3,
 			R.layout.clock_googly, R.layout.clock_googly1,
 			R.layout.clock_googly3, R.layout.clock_droid2,
 			R.layout.clock_droid2_1, R.layout.clock_droid2_2,
 			R.layout.clock_droid2_3, R.layout.clock_droids,
 			R.layout.clock_droids1, R.layout.clock_droids2,
 			R.layout.clock_droids3, R.layout.digital_clock};
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// Log.v(TAG, "onCreate()");
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.main);
 
 		mImageManager = new ImageManager(this);
 		mImageViews[0] = (ImageView) findViewById(R.id.picView1);
 		mImageViews[1] = (ImageView) findViewById(R.id.picView2);
 		mBtnPrev = (TextView) findViewById(R.id.btnPrev);
 		mBtnNext = (TextView) findViewById(R.id.btnNext);
 		mBtnDisp = (TextView) findViewById(R.id.btnDisp);
 		mBtnClock = (TextView) findViewById(R.id.btnClock);
 		mBtnPrev.setVisibility(View.GONE);
 
 		mSharedPref = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
 		mAdLayout = (LinearLayout) findViewById(R.id.adLayout);
 		mClockLayout = (ViewGroup) findViewById(R.id.clockLayout);
 		mProgressBar = (ProgressBar) findViewById(R.id.prgbar);
 		mProgressBar.setVisibility(View.GONE);
 		mProgressIcon = (ProgressBar) findViewById(R.id.prgIcon);
 		mProgressIcon.setVisibility(View.GONE);
 
 		mClockGestureDetector = new GestureDetector(this,
 				new MyClockGestureListener());
 		mClockLayout.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				mClockGestureDetector.onTouchEvent(event);
 				return true;
 			}
 		});
 
 		mGestureDetector = new GestureDetector(this, new MyGestureListener());
 		OnTouchListener rootListener = new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				mGestureDetector.onTouchEvent(event);
 				return true;
 			}
 		};
 		for (ImageView iv : mImageViews) {
 			iv.setOnTouchListener(rootListener);
 		}
 
 		setupAdLayout();
 		setupButtons();
 
 		mSlideShowInAnimation = new Animation[]{
 				makeInAnimation(R.anim.transition_in),
 				makeInAnimation(R.anim.slide_in),
 				makeInAnimation(R.anim.slide_in_vertical),
 				makeInAnimation(R.anim.slide_in_r),
 				makeInAnimation(R.anim.slide_in_vertical_r),};
 
 		mSlideShowOutAnimation = new Animation[]{
 				makeOutAnimation(R.anim.transition_out),
 				makeOutAnimation(R.anim.slide_out),
 				makeOutAnimation(R.anim.slide_out_vertical),
 				makeOutAnimation(R.anim.slide_out_r),
 				makeOutAnimation(R.anim.slide_out_vertical_r),};
 
 		mAnimationIndex = mSharedPref.getInt(PREF_SLIDE_ANIM, 0);
 	}
 
 	private final Runnable mCheck2ShowAD = new Runnable() {
 		@Override
 		public void run() {
 			check2showAD();
 			mHandler.postDelayed(mCheck2ShowAD, 60000); // check every 60sec
 		}
 	};
 
 	public void setImageView(Bitmap bm) {
 		if (bm == null)
 			return;
 
 		mImageViews[mImageViewCurrent].setImageBitmap(bm);
 		mImageViews[mImageViewCurrent].setScaleType(mScaleType);
 		// TextView tv = (TextView) findViewById(R.id.picName);
 		// tv.setText(bm.getWidth() + "x" + bm.getHeight());
 	}
 
 	private final Runnable mUpdateImageView = new Runnable() {
 		@Override
 		public void run() {
 
 			if (mSharedPref.getBoolean(PREF_BOSS_KEY, false)) {
 				if (getClockVisibility()) {
 					setClockVisibility(false);
 				}
 			}
 
 			ImageView oldView = mImageViews[mImageViewCurrent];
 			if (++mImageViewCurrent == mImageViews.length) {
 				mImageViewCurrent = 0;
 			}
 			ImageView newView = mImageViews[mImageViewCurrent];
 			newView.setVisibility(View.VISIBLE);
 
 			Bitmap bm;
 			if (!bStarted) {
 				bm = mImageManager.getCurrentBitmap();
 				bStarted = !bStarted;
 			} else {
 				bm = mImageManager.getImageBitmap(mStep);
 			}
 
 			Log.v(TAG, "current str: " + mImageManager.getCurrentStr());
 			if (mSharedPref.getBoolean(PREF_PIC_FULL_FILL, true)) {
 				mScaleType = DEFAULT_SCALETYPE;
 			}
 			newView.setScaleType(mScaleType);
 			newView.setImageBitmap(bm);
 			newView.scrollTo(0, 0);
 
 			if (mAnimationIndex >= 0) {
 				int animation = mAnimationIndex;
 				if (mAnimationIndex > 0) {
 					animation = mAnimationIndex + (1 - mStep);
 				}
 				Animation aIn = mSlideShowInAnimation[animation];
 				newView.startAnimation(aIn);
 				newView.setVisibility(View.VISIBLE);
 				Animation aOut = mSlideShowOutAnimation[animation];
 				oldView.setVisibility(View.INVISIBLE);
 				oldView.startAnimation(aOut);
 			} else {
 				newView.setVisibility(View.VISIBLE);
 				oldView.setVisibility(View.INVISIBLE);
 			}
 
 			DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
 			if (bm.getWidth() > displayMetrics.widthPixels
 					|| bm.getHeight() > displayMetrics.heightPixels) {
 				bLargePicLoaded = true;
 			} else {
 				bLargePicLoaded = false;
 			}
 		}
 	};
 
 	public String getPicPath() {
 		return Environment.getDataDirectory() + APP_FOLDER + PIC_FOLDER;
 		// return Environment.getExternalStorageDirectory() + PIC_FOLDER;
 	}
 
 	@Override
 	public void onStart() {
 		Log.v(TAG, "onStart()");
 		super.onStart();
 
 		// If Image list initialization failed, restart the process
 		Log.d(TAG, "Init List failed? " + mImageManager.isInitListFailed());
 		if (mImageManager.isInitListFailed()) {
 			ImageManager.IMAGE_PATH_TYPE type = mImageManager
 					.getImagePathType();
 			mImageManager.setImagePathType(type);
 			initStartIndex();
 		}
 	}
 
 	@Override
 	public void onResume() {
 		Log.v(TAG, "onResume()");
 		super.onResume();
 
 		int face = mSharedPref.getInt(PREF_CLOCK_FACE, 0);
 		if (mFace != face) {
 			if (face < 0 || face >= CLOCKS.length)
 				mFace = 0;
 			else
 				mFace = face;
 			inflateClock();
 		}
 
 		check2showAD();
 		if (mSharedPref.getBoolean(PREF_AUTOHIDE_SB, false)) {
 			setSBVisibility(false);
 		} else {
 			setSBVisibility(getMLVisibility());
 		}
 
 		if (mSharedPref.getBoolean(PREF_BOSS_KEY, false)) {
 			Boolean bClockVisible = getClockVisibility();
 			if (bClockVisible) {
 				mImageViews[mImageViewCurrent].setVisibility(bClockVisible
 						? View.GONE
 						: View.VISIBLE);
 			}
 		}
 
 		mAnimationIndex = mSharedPref.getInt(PREF_SLIDE_ANIM, 0);
 	}
 
 	@Override
 	public void onPause() {
 		// Log.v(TAG, "onPause()");
 		super.onPause();
 	}
 
 	@Override
 	public void onStop() {
 		// Log.v(TAG, "onStop()");
 		super.onStop();
 	}
 
 	@Override
 	public void onDestroy() {
 		// Log.v(TAG, "onDestroy()");
 		super.onDestroy();
 
 		Editor editor = mSharedPref.edit();
 		if (mImageManager.getCurrent() != ImageManager.INVALID_PIC_INDEX) {
 			Log.v(TAG, "Save current image index " + mImageManager.getCurrent()
 					+ " to shared pref");
 			editor.putInt(PREF_LAST_CODE, mImageManager.getCurrent()).commit();
 		}
 	}
 
 	@Override
 	public void onClickAd() {
 		Log.v(TAG, "onClickAd()");
 		if (mSharedPref.getBoolean(PREF_AUTOHIDE_AD, false)) {
 			mHandler.postDelayed(new Runnable() {
 				@Override
 				public void run() {
 					iAdClick = 0;
 				}
 			}, 5000);
 
 			if (++iAdClick >= CLICKS_TO_HIDE_AD) {
 				Log.d(TAG, "User just clicked AD.");
 				mHandler.postDelayed(new Runnable() {
 					@Override
 					public void run() {
 						setAdVisibility(false);
 						Time time = new Time(System.currentTimeMillis());
 						Editor ed = mSharedPref.edit();
 						ed.putString(PREF_AD_CLICK_TIME, time.toString())
 								.commit();
 					}
 				}, 2000);
 			}
 		}
 
 		// get phone info for test
 		TelephonyManager telephonyManager = (TelephonyManager) this
 				.getSystemService(Context.TELEPHONY_SERVICE);
 		String imei = telephonyManager.getDeviceId();
 		Log.v(TAG, "Test IMEI is " + imei);
 
 		WifiManager wifi = (WifiManager) this
 				.getSystemService(Context.WIFI_SERVICE);
 		WifiInfo info = wifi.getConnectionInfo();
 		String macAddr = info.getMacAddress();
 		Log.v(TAG, "Test MAC is " + macAddr);
 	}
 
 	@Override
 	public void onDisplayAd() {
 		// Log.v(TAG, "onDisplayAd()");
 		check2showAD();
 	}
 
 	private void check2showAD() {
 		// autohide_ad is checked and within an hour, do hide AD
 		if (mSharedPref.getBoolean(PREF_AUTOHIDE_AD, false)) {
 			String timeStr = mSharedPref.getString(PREF_AD_CLICK_TIME, "");
 			if (timeStr.length() != 0) {
 				Time time = new Time(System.currentTimeMillis());
 				Time time2Cmp = new Time(time.getHours() - 1,
 						time.getMinutes(), time.getSeconds());
 				Time timeClick = Time.valueOf(timeStr);
 
 				if (timeClick.after(time2Cmp)) {
 					// Log.v(TAG, "Hiding AD Layout.");
 					setAdVisibility(false);
 					return;
 				} else {
 					Log.v(TAG, "Removing click time tag.");
 					Editor ed = mSharedPref.edit();
 					ed.remove(PREF_AD_CLICK_TIME).commit();
 				}
 			}
 		}
 		// Log.v(TAG, "Showing AD Layout.");
 		setAdVisibility(true);
 	}
 
 	private void setupAdLayout() {
 		/* 下面两行只用于测试,完成后一定要去掉,参考文挡说明 */
 		// AdViewTargeting.setUpdateMode(UpdateMode.EVERYTIME); //
 		// 保证每次都从服务器取配置
 		AdViewTargeting.setRunMode(RunMode.NORMAL); // 保证所有选中的广告公司都为测试状态
 		/* 下面这句方便开发者进行发布渠道统计,详细调用可以参考java doc */
 		// AdViewTargeting.setChannel(Channel.GOOGLEMARKET);
 		AdViewLayout adViewLayout = new AdViewLayout(this,
 				"SDK20122309480217x9sp4og4fxrj2ur");
 		adViewLayout.setAdViewInterface(this);
 		mAdLayout.addView(adViewLayout);
 		mAdLayout.invalidate();
 	}
 
 	private void setupButtons() {
 
 		mBtnPrev.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				goPrev();
 			}
 		});
 
 		mBtnNext.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				goNext();
 			}
 		});
 
 		mBtnDisp.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				// hide mainLayout only leave background image
 				if (getMLVisibility()) {
 					setMLVisibility(false);
 				}
 			}
 		});
 
 		mBtnClock.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				boolean bClockVisible = getClockVisibility();
 				setClockVisibility(!bClockVisible);
 				if (mSharedPref.getBoolean(PREF_BOSS_KEY, false)) {
 					mImageViews[mImageViewCurrent].setVisibility(bClockVisible
 							? View.VISIBLE
 							: View.GONE);
 				}
 			}
 		});
 	}
 
 	private void initStartIndex() {
 		if (mImageManager.getCurrent() != ImageManager.INVALID_PIC_INDEX)
 			return;
 		int size = mImageManager.getImageListSize();
 		if (size == 0)
 			return;
 
 		mImageManager.setCurrent(mRandom.nextInt(size));
 
 		if (mImageManager.getImagePathType() == IMAGE_PATH_TYPE.LOCAL_ASSETS) {
 			int index = mSharedPref.getInt(PREF_LAST_CODE,
 					ImageManager.INVALID_PIC_INDEX);
 
 			if (bStarted) {
 				index = (size + index - 1) % size;
 			}
 			mImageManager.setCurrent(index);
 		}
 		Log.d(TAG, "initStartIndex(): start from " + mImageManager.getCurrent());
 	}
 	private void goNext() {
 		if (mImageManager.getImageListSize() == 0)
 			return;
 
 		if (!bStarted) {
 
 			if (mSharedPref.getBoolean(PREF_AUTOHIDE_CLOCK, true)) {
 				setClockVisibility(false);
 			}
 
 			mBtnNext.setText(getResources().getString(R.string.strNext));
 			mBtnPrev.setVisibility(View.VISIBLE);
 
 			initStartIndex();
 		}
 
 		mStep = 1;
 		mHandler.postDelayed(mUpdateImageView, 200);
 	}
 
 	private void goPrev() {
 		mStep = -1;
 		mHandler.postDelayed(mUpdateImageView, 20);
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
 		menu.findItem(R.id.menu_toggle_clock).setVisible(false);
 
 		menu.findItem(R.id.menu_toggle_mode)
 				.setTitle(
 						mImageManager.getImagePathType() == IMAGE_PATH_TYPE.LOCAL_ASSETS
 								? R.string.remote_mode
 								: R.string.local_mode);
 		menu.findItem(R.id.menu_toggle_mode).setEnabled(mBtnNext.isEnabled());
 
 		if (mImageManager.getCurrent() == ImageManager.INVALID_PIC_INDEX) {
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
 
 			case R.id.menu_toggle_mode :
 
 				if (mImageManager.getImagePathType() == IMAGE_PATH_TYPE.LOCAL_ASSETS) {
 					mImageManager.setQueryKeyword("android MM bizhi");
 					mImageManager
 							.setImagePathType(IMAGE_PATH_TYPE.REMOTE_HTTP_URL);
 					Intent intent = new Intent();
 					intent.setClass(this, WallPhotoActivity.class);
 					startActivity(intent);
 					// 如果不关闭当前的会出现好多个页面
 					this.finish();
 				} else {
 					mImageManager
 							.setImagePathType(IMAGE_PATH_TYPE.LOCAL_ASSETS);
 				}
 				if (bStarted) {
 					initStartIndex();
 				}
 				break;
 
 			case R.id.menu_settings :
 				startActivity(new Intent(this, Settings.class));
 				break;
 
 			case R.id.menu_set_livewallpaper :
 				Editor myEdit = mSharedPref.edit();
 				if (mImageManager.getCurrent() == ImageManager.INVALID_PIC_INDEX
 						&& mImageManager.getImagePathType() == IMAGE_PATH_TYPE.LOCAL_ASSETS) {
 					// Now only support local assets to set as background of
 					// live wall paper
 					myEdit.putString(PREF_PIC_CODE,
 							mImageManager.getCurrentStr());
 
 				} else {
 					// remove the preference to set default clock as live
 					// wall paper
 					myEdit.remove(PREF_PIC_CODE);
 				}
 				myEdit.commit();
 
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
 		mBtnClock.setText(!bVisibility
 				? R.string.show_clock
 				: R.string.hide_clock);
 	}
 
 	private boolean getMLVisibility() {
 		return getLayoutVisibility(R.id.mainLayout);
 	}
 
 	private void setMLVisibility(boolean bVisibility) {
 		((LinearLayout) findViewById(R.id.mainLayout))
 				.startAnimation(bVisibility ? AnimationUtils.loadAnimation(
 						this, R.anim.footer_appear) : AnimationUtils
 						.loadAnimation(this, R.anim.footer_disappear));
 
 		setLayoutVisibility(R.id.mainLayout, bVisibility);
 		if (mSharedPref.getBoolean(PREF_AUTOHIDE_SB, false)) {
 			bVisibility = false;
 		}
 		setSBVisibility(bVisibility);
 	}
 
 	private void setSBVisibility(boolean bVisibility) {
 		if (bVisibility) {
 			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		} else {
 			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 					WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		}
 	}
 
 	private boolean getAdVisibility() {
 		return getLayoutVisibility(R.id.adLayout);
 	}
 
 	private void setAdVisibility(boolean bVisibility) {
 		setLayoutVisibility(R.id.adLayout, bVisibility);
 	}
 
 	public void EnableNextPrevButtons(boolean enabled) {
 		mBtnPrev.setEnabled(enabled);
 		mBtnNext.setEnabled(enabled);
 	}
 
 	private void setWallpaper() {
 		try {
 
 			if (mSharedPref.getBoolean(PREF_WP_FULL_FILL, false)
 					|| mImageManager.getImagePathType() == IMAGE_PATH_TYPE.REMOTE_HTTP_URL) {
 
 				WallpaperManager.getInstance(this).setBitmap(
 						((BitmapDrawable) mImageViews[mImageViewCurrent]
 								.getDrawable()).getBitmap());
 
 			} else {
 
 				DisplayMetrics displayMetrics = getResources()
 						.getDisplayMetrics();
 				int width = displayMetrics.widthPixels * 2;
 				int height = displayMetrics.heightPixels;
 
 				BitmapFactory.Options opt = new BitmapFactory.Options();
 				opt.inJustDecodeBounds = true;
 
 				Bitmap bm = BitmapFactory.decodeStream(
 						getAssets().open(mImageManager.getCurrentStr()), null,
 						opt);
 
 				int bm_w = opt.outWidth;
 				int bm_h = opt.outHeight;
 				Log.d(TAG, "origin: " + bm_w + "x" + bm_h);
 
 				float ratio_hw = (float) bm_h / bm_w;
 				Log.d(TAG, "bitmap original ratio height/width = " + ratio_hw);
 				if (bm_w > width || bm_h > height) {
 					if (height / ratio_hw <= width) {
 						opt.outHeight = height;
 						opt.outWidth = (int) (height / ratio_hw);
 					} else {
 						opt.outWidth = width;
 						opt.outHeight = (int) (width * ratio_hw);
 					}
 				} else {
 					if (height / ratio_hw <= width) {
 						opt.outWidth = width;
 						opt.outHeight = (int) (width * ratio_hw);
 					} else {
 						opt.outHeight = height;
 						opt.outWidth = (int) (height / ratio_hw);
 					}
 				}
 				Log.d(TAG, "scaled: " + opt.outWidth + "x" + opt.outHeight);
 
 				opt.inJustDecodeBounds = false;
 				opt.inSampleSize = bm_w / width;
 				Log.d(TAG, "bitmap inSampleSize = " + opt.inSampleSize);
 
 				bm = BitmapFactory.decodeStream(
 						getAssets().open(mImageManager.getCurrentStr()), null,
 						opt);
 
 				Bitmap bm_wp = Bitmap.createBitmap(width, height,
 						Bitmap.Config.ARGB_8888);
 				Canvas canvas = new Canvas(bm_wp);
 
 				canvas.drawBitmap(bm, (width - opt.outWidth) / 2,
 						(height - opt.outHeight) / 2, null);
 				bm.recycle();
 
 				WallpaperManager.getInstance(this).setBitmap(bm_wp);
 			}
 			WallpaperManager.getInstance(this).setWallpaperOffsetSteps(0, 0);
 
 		} catch (IOException e) {
 			Log.e(TAG, "Failed to set wallpaper!");
 		}
 	}
 
 	private class MyClockGestureListener
 			extends
 				GestureDetector.SimpleOnGestureListener {
 		private final int LARGE_MOVE = 80;
 
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 				float velocityY) {
 			if (e1.getX() - e2.getX() > LARGE_MOVE) {
 				Log.d(TAG, "ClockGesture Fling Left with velocity " + velocityX);
 				ChangeClockFace(1);
 				return true;
 			} else if (e2.getX() - e1.getX() > LARGE_MOVE) {
 				Log.d(TAG, "ClockGesture Fling Right with velocity "
 						+ velocityX);
 				ChangeClockFace(-1);
 				return true;
 			}
 
 			return false;
 		}
 
 		private void ChangeClockFace(int step) {
 			int face = (mFace + step) % CLOCKS.length;
 			if (mFace != face) {
 				if (face < 0 || face >= CLOCKS.length) {
 					mFace = 0;
 				} else {
 					mFace = face;
 				}
 
 				inflateClock();
 				Animation aIn = mSlideShowInAnimation[0];
 				mClockLayout.setVisibility(View.VISIBLE);
 				mClockLayout.startAnimation(aIn);
 
 				Editor edit = mSharedPref.edit();
 				edit.putInt(PREF_CLOCK_FACE, mFace).commit();
 			}
 		}
 
 		@Override
 		public boolean onSingleTapConfirmed(MotionEvent e) {
 			if (!getMLVisibility()) {
 				setMLVisibility(true);
 			}
 			return true;
 		}
 	}
 
 	private class MyGestureListener
 			extends
 				GestureDetector.SimpleOnGestureListener {
 		private final int LARGE_MOVE = 80;
 
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 				float velocityY) {
 			if (mScaleType == ScaleType.CENTER) {
 				return false;
 			}
 
 			if (mAnimationIndex == 1) { // slide horizontal
 				if (e1.getX() - e2.getX() > LARGE_MOVE) {
 					Log.d(TAG, "Fling Left with velocity " + velocityX);
 					goNext();
 					return true;
 				} else if (e2.getX() - e1.getX() > LARGE_MOVE) {
 					Log.d(TAG, "Fling Right with velocity " + velocityX);
 					goPrev();
 					return true;
 				}
 			} else if (mAnimationIndex == 2) { // slide vertical
 				if (e1.getY() - e2.getY() > LARGE_MOVE) {
 					Log.d(TAG, "Fling Up with velocity " + velocityY);
 					goNext();
 					return true;
 				} else if (e2.getY() - e1.getY() > LARGE_MOVE) {
 					Log.d(TAG, "Fling Down with velocity " + velocityY);
 					goPrev();
 					return true;
 				}
 			}
 
 			return false;
 		}
 
 		@Override
 		public boolean onScroll(MotionEvent e1, MotionEvent e2,
 				float distanceX, float distanceY) {
 			if (mScaleType == ScaleType.CENTER) {
 				mImageViews[mImageViewCurrent].scrollBy((int) distanceX,
 						(int) distanceY);
 			}
 			return true;
 		}
 
 		@Override
 		public boolean onDoubleTap(MotionEvent e) {
 			if (mScaleType != DEFAULT_SCALETYPE) {
 				mScaleType = DEFAULT_SCALETYPE;
 				mImageViews[mImageViewCurrent].setScaleType(mScaleType);
 				mImageViews[mImageViewCurrent].scrollTo(0, 0);
 			} else if (mScaleType == DEFAULT_SCALETYPE) {
 				if (bLargePicLoaded) {
 					mScaleType = ScaleType.CENTER;
 				} else {
 					mScaleType = ALTER_SCALETYPE;
 				}
 				mImageViews[mImageViewCurrent].setScaleType(mScaleType);
 			}
 			return true;
 		}
 
 		@Override
 		public boolean onSingleTapConfirmed(MotionEvent e) {
 			if (!getMLVisibility()) {
 				setMLVisibility(true);
 			}
 			return true;
 		}
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
 					mHandler.postDelayed(new Runnable() {
 						@Override
 						public void run() {
 							bKeyBackIn2Sec = false;
 						}
 
 					}, 2000); // reset BACK status in 2 seconds
 
 				} else {
 					finish();
 				}
 
 				return false;
 		}
 
 		return super.onKeyUp(keycode, event);
 	}
 
 	protected void inflateClock() {
 		if (mClock != null) {
 			mClockLayout.removeView(mClock);
 		}
 
 		LayoutInflater.from(this).inflate(CLOCKS[mFace], mClockLayout);
 		mClock = findViewById(R.id.clock);
 	}
 
 	private Animation makeInAnimation(int id) {
 		Animation inAnimation = AnimationUtils.loadAnimation(this, id);
 		return inAnimation;
 	}
 
 	private Animation makeOutAnimation(int id) {
 		Animation outAnimation = AnimationUtils.loadAnimation(this, id);
 		return outAnimation;
 	}
 }
