 package com.dbstar.DbstarDVB.VideoPlayer;
 
 import android.os.storage.*;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 import android.os.SystemProperties;
 
 import com.subtitleparser.*;
 import com.subtitleview.SubtitleView;
 import android.content.Context;
 
 import com.dbstar.DbstarDVB.PlayerService.*;
 import com.dbstar.DbstarDVB.VideoPlayer.alert.DbVideoInfoDlg;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.res.Configuration;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.os.*;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 import android.provider.Settings.System;
 import android.util.Log;
 import android.view.*;
 import android.widget.*;
 import android.graphics.Canvas;
 import android.graphics.PixelFormat;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.media.AudioManager;
 import android.net.Uri;
 import android.content.SharedPreferences;
 
 import java.io.FileOutputStream;
 import java.lang.Thread.UncaughtExceptionHandler;
 
 import android.content.ContentResolver;
 import android.content.res.AssetFileDescriptor;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 
 import com.dbstar.DbstarDVB.R;
 
 class SubtitleParameter {
 	public int totalnum;
 	public int curid;
 	public int color;
 	public int font;
 	public SubID sub_id;
 	public boolean enable;
 	public int position_v;
 }
 
 public class PlayerMenu extends Activity {
 	private static String TAG = "PlayerMenu";
 
 	public static final String PREFS_SUBTITLE_NAME = "subtitlesetting";
 
 	private static final String ACTION_REALVIDEO_ON = "android.intent.action.REALVIDEO_ON";
 	private static final String ACTION_REALVIDEO_OFF = "android.intent.action.REALVIDEO_OFF";
 	private static final String ACTION_VIDEOPOSITION_CHANGE = "android.intent.action.VIDEOPOSITION_CHANGE";
 
 	private static final String STR_OUTPUT_MODE = "ubootenv.var.outputmode";
 
 	private static final String InputFile = "/sys/class/audiodsp/codec_mips";
 	private static final String OutputFile = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
 	private static final String ScaleaxisFile = "/sys/class/graphics/fb0/scale_axis";
 	private static final String ScaleFile = "/sys/class/graphics/fb0/scale";
 	private static final String RequestScaleFile = "/sys/class/graphics/fb0/request2XScale";
 	private static final String FormatMVC = "/sys/class/amhdmitx/amhdmitx0/config";
 	private static final String Filemap = "/sys/class/vfm/map";
 	private static final String File_amvdec_mpeg12 = "/sys/module/amvdec_mpeg12/parameters/dec_control";
 	private static final String File_amvdec_h264 = "/sys/module/amvdec_h264/parameters/dec_control";
 
 	private static final String VideoAxisFile = "/sys/class/video/axis";
 	private static final String RegFile = "/sys/class/display/wr_reg";
 	private static final String Fb0Blank = "/sys/class/graphics/fb0/blank";
 	private static final String Fb1Blank = "/sys/class/graphics/fb1/blank";
 
 	// patch for hide OSD
 	private static final String OSD_BLANK_PATH = "/sys/class/graphics/fb0/blank";
 	private static final String OSD_BLOCK_MODE_PATH = "/sys/class/graphics/fb0/block_mode";
 
 	private static final String FormatMVC_3dtb = "3dtb";
 	private static final String FormatMVC_3doff = "3doff";
 
 	private static final int SeekToNone = 0;
 	private static final int SeekToForward = 1;
 	private static final int SeekToBackward = 2;
 
 	private static final int OSDShow = 0;
 	private static final int OSDHidePart = 1;
 	private static final int OSDHideAll = 2;
 
 	private String mCodecMIPS = null;
 
 	private boolean mHdmiPlugged;
 	private boolean mPaused;
 
 	/** Called when the activity is first created. */
 	private int mTotalTime = 0;
 	private int mCurrentTime = 0;
 
 	private boolean INITOK = false;
 	private boolean FF_FLAG = false;
 	private boolean FB_FLAG = false;
 
 	// The ffmpeg step is 2*step
 	private int FF_LEVEL = 0;
 	private int FB_LEVEL = 0;
 	private static int FF_MAX = 5;
 	private static int FB_MAX = 5;
 	private static int FF_SPEED[] = { 0, 2, 4, 8, 16, 32 };
 	private static int FB_SPEED[] = { 0, 2, 4, 8, 16, 32 };
 	private static int FF_STEP[] = { 0, 1, 2, 4, 8, 16 };
 	private static int FB_STEP[] = { 0, 1, 2, 4, 8, 16 };
 	private static int VOLUME_LEVEL[] = { 0, 2, 4, 6, 8, 10, 11, 12, 13, 14, 15 };
 	private static int VOLUME_ADJUST_STEP[] = { 2, 2, 2, 2, 2, 1, 1, 1, 1, 1 };
 
 	private static final int MID_FREESCALE = 0x10001;
 	private boolean mFB32 = false;
 
 	private SeekBar mProgressBar = null;
 	private ImageView mPlayButton = null;
 
 	private TextView mCurrentTimeView = null;
 	private TextView mTotalTimeView = null;
 
 	private ImageView mSoundStateView = null, mSoundVolumeView = null;
 
 	private LinearLayout mInfoBar = null;
 
 	Timer mInfoBarTimer = new Timer();
 
 	private static final int DefaultVolumeLevel = 10;
 	AudioManager mAudioManager;
 	boolean mIsMute = false;
 	int mMaxVolumeLevel = -1; // default is 15 on Android.
 	int mVolumeLevel = -1;
 	int mVolumeStep = -1;
 	int mVolumeAdjustStepIndex = 0;
 
 	// private AlertDialog mConfirmDialog = null;
 	Toast mFFToast = null;
 	public MediaInfo mMediaInfo = null;
 	private int mPlayerStatus = VideoInfo.PLAYER_UNKNOWN;
 	private int mSeekDirection = SeekToNone;
 	private int mCurrentSeekTime = 0;
 
 	private int mOSDState = OSDShow;
 	// if already set 2xscale
 	private boolean bSet2XScale = false;
 
 	// MBX freescale mode
 	private int m1080scale = 0;
 	private String mOutputMode = "720p";
 
 	// for subtitle
 	private SubtitleUtils mSubtitleUtils = null;
 	private SubtitleView mSubTitleView = null;
 
 	private SubtitleView mSubTitleView_sm = null;
 	SubtitleParameter mSubtitleParameter = null;
 
 	public IPlayerService mAmplayer = null;
 
 	private WindowManager mWindowManager;
 	PowerManager.WakeLock mScreenLock = null;
 
 	private boolean mSuspendFlag = false;
 
 	private int[] angle_table = { 0, 1, 2, 3 };
 	private final static int GETROTATION_TIMEOUT = 500;
 	private final static int GETROTATION = 0x0001;
 	private int mLastRotation;
 
 	// Input parameters
 	// used for resume. last played position when player exit.
 	private int mPlayPosition = 0;
 	// subtitle file
 	private String mSubtitleFile = null;
 	// media file
 	private Uri mUri = null;
 	private String mFilePath = null;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Thread.currentThread().setUncaughtExceptionHandler(mExceptionHandler);
 
 		mUri = getIntent().getData();
 		if (mUri == null) {
 			return;
 		}
 
 		if (!mUri.getScheme().equals("file")) {
 			return;
 		}
 
 		mFilePath = Utils.getFilePath(mUri);
 		if (mFilePath == null || mFilePath.isEmpty())
 			return;
 
 		m1080scale = SystemProperties.getInt("ro.platform.has.1080scale", 0);
 		mOutputMode = SystemProperties.get(STR_OUTPUT_MODE);
 
 		if (m1080scale == 2
 				|| (m1080scale == 1 && (mOutputMode.equals("1080p")
 						|| mOutputMode.equals("1080i") || mOutputMode
 							.equals("720p")))) {
 			Intent intentVideoOn = new Intent(ACTION_REALVIDEO_ON);
 			sendBroadcast(intentVideoOn);
 			SystemProperties.set("vplayer.hideStatusBar.enable", "true");
 		}
 
 		if (AmPlayer.getProductType() == 1) {
 			AmPlayer.disable_freescale(MID_FREESCALE);
 		}
 
 		// fixed bug for green line
 		FrameLayout foreground = (FrameLayout) findViewById(android.R.id.content);
 		foreground.setForeground(null);
 
 		mFB32 = SystemProperties.get("sys.fb.bits", "16").equals("32");
 		if (mFB32) {
 			setContentView(R.layout.infobar32);
 		} else {
 			setContentView(R.layout.infobar);
 		}
 
 		SettingsVP.init(this);
 		SettingsVP.setVideoLayoutMode();
 		if (m1080scale == 2) {
 			// set video position for MBX
 			Intent changeIntent = new Intent(ACTION_VIDEOPOSITION_CHANGE);
 			sendBroadcast(changeIntent);
 		}
 		SettingsVP.enableVideoLayout();
 
 		mFFToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
 		mFFToast.setGravity(Gravity.TOP | Gravity.RIGHT, 10, 10);
 		mFFToast.setDuration(0x00000001);
 
 		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		mScreenLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
 		mWindowManager = getWindowManager();
 
 		initAngleTable();
 		initSubTitle();
 		initOSDView();
 
 		mSeekDirection = SeekToNone;
 		mCurrentSeekTime = 0;
 
 		startPlayerService();
 
 		displayInit();
 		set2XScale();
 
 		registerHDMIReceiver();
 	}
 
 	public void onStart() {
 		super.onStart();
 
 		mDialogHandler.sendEmptyMessageDelayed(MSG_DIALOG_POPUP,
 				MSG_DIALOG_TIMEOUT);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		// setOSDOn(true);
 
 		SystemProperties.set("vplayer.playing", "true");
 		keepScreenOn();
 
 		mPaused = false;
 
 		int rotation = mWindowManager.getDefaultDisplay().getRotation();
 		if ((rotation >= 0) && (rotation <= 3)) {
 			SettingsVP.setVideoRotateAngle(angle_table[rotation]);
 			mLastRotation = rotation;
 		}
 
 		// install an intent filter to receive SD card related events.
 		registerUSBReceiver();
 		registerCommandReceiver();
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 
 		Log.d(TAG, "onPause");
 
 		keepScreenOff();
 		SystemProperties.set("vplayer.playing", "false");
 
 		mPaused = true;
 
 		if (mVideoInfoDlg != null && mVideoInfoDlg.isShowing()) {
 			mVideoInfoDlg.dismiss();
 		}
 
 		setOSDOn(true);
 
 		unregisterReceiver(mMountReceiver);
 		unregisterCommandReceiver();
 
 		SystemProperties.set("vplayer.hideStatusBar.enable", "false");
 
 		if (mSuspendFlag) {
 			if (mPlayerStatus == VideoInfo.PLAYER_RUNNING) {
 				try {
 					mAmplayer.Pause();
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 			}
 			mSuspendFlag = false;
 			keepScreenOn();
 		}
 		// else {
 		// finish();
 		// }
 
 		if (m1080scale == 2
 				|| (m1080scale == 1 && (mOutputMode.equals("1080p")
 						|| mOutputMode.equals("1080i") || mOutputMode
 							.equals("720p")))) {
 			Utils.writeSysfs(Fb0Blank, "1");
 			Intent intentVideoOff = new Intent(ACTION_REALVIDEO_OFF);
 			sendBroadcast(intentVideoOff);
 		}
 
 		Utils.writeSysfs(FormatMVC, FormatMVC_3doff);
 		disable2XScale();
 		ScreenMode.setScreenMode("0");
 	}
 
 	public void onStop() {
 		super.onStop();
 		Log.d(TAG, "onStop");
 	}
 
 	@Override
 	public void onDestroy() {
 
 		Log.d(TAG, "onDestroy");
 
 		// ResumePlay
 		// .saveResumePara(PlayList.getinstance().getcur(), mCurrentTime);
 		ResumePlay.saveResumePara(mFilePath, mCurrentTime);
 
 		closeSubtitleView();
 
 		Amplayer_stop();
 
 		if (mAmplayer != null)
 			try {
 				if (SystemProperties.getBoolean("3D_setting.enable", false)) {
 					mAmplayer.Set3Dgrating(0);
 					mAmplayer.Set3Dmode(0); // close 3D
 				}
 
 				if (!mFB32) {
 					mAmplayer.DisableColorKey();
 				}
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 
 		stopPlayerService();
 		setDefCodecMips();
 		SettingsVP.disableVideoLayout();
 		SettingsVP.setVideoRotateAngle(0);
 
 		unregisterReceiver(mHDMIEventReceiver);
 
 		if (AmPlayer.getProductType() == 1) // 1:MID 0:other
 			AmPlayer.enable_freescale(MID_FREESCALE);
 
 		super.onDestroy();
 	}
 
 	@Override
 	public void finish() {
 		super.finish();
 
 		Log.d(TAG, "finsh");
 	}
 
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog;
 		switch (id) {
 		case MSG_DIALOG_POPUP:
 			mVideoInfoDlg = new DbVideoInfoDlg(this, getIntent());
 			dialog = mVideoInfoDlg;
 			break;
 		default:
 			dialog = null;
 			break;
 		}
 
 		return dialog;
 	}
 
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
 			mDuringKeyActions = false;
 		}
 
 		return super.onKeyUp(keyCode, event);
 	}
 
 	boolean mDuringKeyActions = false;
 
 	void exitPlayer() {
 		if (SettingsVP.chkEnableOSD2XScale() == true) {
 			hideOSDView();
 		}
 
 		closeSubtitleView();
 
 		// stop play
 		if (mAmplayer != null)
 			Amplayer_stop();
 
 		finish();
 	}
 
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 
 		Log.d(TAG, "onKeyDown " + keyCode);
 
 		// setOSDOn(true);
 		if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
 			mDuringKeyActions = true;
 		}
 
 		// if (keyCode == KeyEvent.KEYCODE_POWER) {
 		// if (!mSuspendFlag) {
 		// if (mPlayerStatus == VideoInfo.PLAYER_RUNNING) {
 		// try {
 		// mAmplayer.Pause();
 		// } catch (RemoteException e) {
 		// e.printStackTrace();
 		// }
 		// }
 		// mSuspendFlag = true;
 		// keepScreenOff();
 		// } else {
 		// try {
 		// mAmplayer.Resume();
 		// } catch (RemoteException e) {
 		// e.printStackTrace();
 		// }
 		//
 		// mSuspendFlag = false;
 		// keepScreenOn();
 		// }
 		// return true;
 		// } else
 
 		switch (keyCode) {
 		case KeyEvent.KEYCODE_BACK: {
 
 			exitPlayer();
 
 			return true;
 
 		}
 		case KeyEvent.KEYCODE_MENU:
 		case KeyEvent.KEYCODE_9: {
 			if (mInfoBar.getVisibility() == View.VISIBLE) {
 				hideInfoBar();
 			} else {
 				showInfoBar(true);
 			}
 			return true;
 		}
 
 		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
 		case KeyEvent.KEYCODE_DPAD_CENTER: {
 			showInfoBar(true);
 			playVideo();
 
 			return true;
 		}
 		case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD: {
 			if (!INITOK)
 				return false;
 
 			showInfoBar(false);
 			fastForward();
 			return true;
 		}
 
 		case KeyEvent.KEYCODE_MEDIA_REWIND: {
 			if (!INITOK)
 				return false;
 
 			showInfoBar(false);
 			fastBackword();
 			return true;
 		}
 
 		case KeyEvent.KEYCODE_MUTE: {
 			showInfoBar(true);
 
 			setMute(!mIsMute);
 
 			return true;
 		}
 
 		case KeyEvent.KEYCODE_DPAD_DOWN: {
 			showInfoBar(true);
 			decreaseVolume();
 			return true;
 		}
 
 		case KeyEvent.KEYCODE_DPAD_UP: {
 			showInfoBar(true);
 			increaseVolume();
 			return true;
 		}
 
 		case KeyEvent.KEYCODE_NOTIFICATION: {
 			setOSDOn(true);
 			mDialogHandler.sendEmptyMessageDelayed(MSG_DIALOG_POPUP,
 					MSG_DIALOG_TIMEOUT);
 		}
 
 		case KeyEvent.KEYCODE_DPAD_LEFT:
 		case KeyEvent.KEYCODE_DPAD_RIGHT: {
 			showInfoBar(true);
 			// mProgressBar.requestFocus();
 			//
 			// if (mPlayerStatus == VideoInfo.PLAYER_SEARCHING) {
 			// try {
 			// mFFToast.cancel();
 			// if (FF_FLAG)
 			// mAmplayer.FastForward(0);
 			// if (FB_FLAG)
 			// mAmplayer.BackForward(0);
 			// FF_FLAG = false;
 			// FB_FLAG = false;
 			// FF_LEVEL = 0;
 			// FB_LEVEL = 0;
 			// } catch (RemoteException e) {
 			// e.printStackTrace();
 			// }
 			// }
 
 			event.startTracking();
 			return true;
 		}
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
 		switch (keyCode) {
 		case KeyEvent.KEYCODE_DPAD_RIGHT: {
 			if (!INITOK)
 				return false;
 
 			showInfoBar(false);
 			fastForward();
 			return true;
 		}
 
 		case KeyEvent.KEYCODE_DPAD_LEFT: {
 			if (!INITOK)
 				return false;
 
 			showInfoBar(false);
 			fastBackword();
 			return true;
 		}
 		}
 
 		return super.onKeyLongPress(keyCode, event);
 	}
 
 	void setMute(boolean mute) {
 		if (mIsMute != mute) {
			int level = mute ? 1 : 0;
			mSoundStateView.setImageLevel(level);
 			mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, mute);
 		}
 	}
 
 	int getVolumeLevelByStepIndex(int stepIndex) {
 		int volume = 0;
 		for (int i = 0; i < stepIndex; i++) {
 			volume += VOLUME_ADJUST_STEP[stepIndex];
 		}
 
 		return volume;
 	}
 
 	int mVolumeLevelIndex = -1;
 	int mIncreaseStepIndex = -1, mDecreaseStepIndex = -1;
 
 	int getVolumeLevelIndex(int volume) {
 		int i = 0;
 
 		for (i = 0; i < VOLUME_LEVEL.length; i++) {
 			if (volume < VOLUME_LEVEL[i]) {
 				break;
 			}
 		}
 
 		if (i > 0) {
 			i--;
 		}
 
 		return i;
 	}
 
 	void increaseVolume() {
 
		if (mVolumeLevel == mMaxVolumeLevel)
 			return;
 
 		if (mVolumeLevel > VOLUME_LEVEL[mVolumeLevelIndex]) {
 			mVolumeLevel += 1;
 		}
 		mVolumeLevel += VOLUME_ADJUST_STEP[mVolumeLevelIndex];
 		mVolumeLevelIndex++;
 
 		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolumeLevel,
 				0);
 		mSoundVolumeView.setImageLevel(mVolumeLevelIndex);
 	}
 
 	void decreaseVolume() {
		if (mVolumeLevel == 0)
 			return;
 
 		if (mVolumeLevel > VOLUME_LEVEL[mVolumeLevelIndex]) {
 			mVolumeLevel -= 1;
 		}
 
 		mVolumeLevel -= VOLUME_ADJUST_STEP[mVolumeLevelIndex - 1];
 		mVolumeLevelIndex--;
 		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolumeLevel,
 				0);
 		mSoundVolumeView.setImageLevel(mVolumeLevelIndex);
 	}
 
 	void playVideo() {
 		if (mPlayerStatus == VideoInfo.PLAYER_RUNNING) {
 			try {
 				mAmplayer.Pause();
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		} else if (mPlayerStatus == VideoInfo.PLAYER_PAUSE) {
 			try {
 				mAmplayer.Resume();
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		} else if (mPlayerStatus == VideoInfo.PLAYER_SEARCHING) {
 			try {
 				mFFToast.cancel();
 				if (FF_FLAG)
 					mAmplayer.FastForward(0);
 				if (FB_FLAG)
 					mAmplayer.BackForward(0);
 				FF_FLAG = false;
 				FB_FLAG = false;
 				FF_LEVEL = 0;
 				FB_LEVEL = 0;
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	void fastForward() {
 		if (!INITOK)
 			return;
 
 		if (mPlayerStatus == VideoInfo.PLAYER_SEARCHING) {
 			if (FF_FLAG) {
 				if (FF_LEVEL < FF_MAX) {
 					FF_LEVEL = FF_LEVEL + 1;
 				} else {
 					FF_LEVEL = 0;
 				}
 
 				try {
 					mAmplayer.FastForward(FF_STEP[FF_LEVEL]);
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 
 				if (FF_LEVEL == 0) {
 					mFFToast.cancel();
 					FF_FLAG = false;
 				} else {
 					mFFToast.cancel();
 					mFFToast.setText(new String("FF x"
 							+ Integer.toString(FF_SPEED[FF_LEVEL])));
 					mFFToast.show();
 				}
 			}
 
 			if (FB_FLAG) {
 				if (FB_LEVEL > 0) {
 					FB_LEVEL = FB_LEVEL - 1;
 				} else {
 					FB_LEVEL = 0;
 				}
 
 				try {
 					mAmplayer.BackForward(FB_STEP[FB_LEVEL]);
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 
 				if (FB_LEVEL == 0) {
 					mFFToast.cancel();
 					FB_FLAG = false;
 				} else {
 					mFFToast.cancel();
 					mFFToast.setText(new String("FB x"
 							+ Integer.toString(FB_SPEED[FB_LEVEL])));
 					mFFToast.show();
 				}
 			}
 		} else {
 			try {
 				mAmplayer.FastForward(FF_STEP[1]);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 			FF_FLAG = true;
 			FF_LEVEL = 1;
 			mFFToast.cancel();
 			mFFToast.setText(new String("FF x" + FF_SPEED[FF_LEVEL]));
 			mFFToast.show();
 		}
 
 	}
 
 	void fastBackword() {
 		if (!INITOK)
 			return;
 
 		if (mPlayerStatus == VideoInfo.PLAYER_SEARCHING) {
 			if (FB_FLAG) {
 				if (FB_LEVEL < FB_MAX) {
 					FB_LEVEL = FB_LEVEL + 1;
 				} else {
 					FB_LEVEL = 0;
 				}
 
 				try {
 					mAmplayer.BackForward(FB_STEP[FB_LEVEL]);
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 
 				if (FB_LEVEL == 0) {
 					mFFToast.cancel();
 					FB_FLAG = false;
 				} else {
 					mFFToast.cancel();
 					mFFToast.setText(new String("FB x"
 							+ Integer.toString(FB_SPEED[FB_LEVEL])));
 					mFFToast.show();
 				}
 			}
 
 			if (FF_FLAG) {
 				if (FF_LEVEL > 0) {
 					FF_LEVEL = FF_LEVEL - 1;
 				} else {
 					FF_LEVEL = 0;
 				}
 
 				try {
 					mAmplayer.FastForward(FF_STEP[FF_LEVEL]);
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 
 				if (FF_LEVEL == 0) {
 					mFFToast.cancel();
 					FF_FLAG = false;
 				} else {
 					mFFToast.cancel();
 					mFFToast.setText(new String("FF x"
 							+ Integer.toString(FF_SPEED[FF_LEVEL])));
 					mFFToast.show();
 				}
 			}
 		} else {
 			try {
 				mAmplayer.BackForward(FB_STEP[1]);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 			FB_FLAG = true;
 			FB_LEVEL = 1;
 			mFFToast.cancel();
 			mFFToast.setText(new String("FB x" + FB_SPEED[FB_LEVEL]));
 			mFFToast.show();
 		}
 	}
 
 	void seekPlayback(int position) {
 		int seekToPos = mTotalTime * (position + 1) / 100;
 
 		try {
 			if (mAmplayer != null) {
 				mCurrentSeekTime = mCurrentTime;
 				if (seekToPos > mCurrentTime)
 					mSeekDirection = SeekToForward;
 				else
 					mSeekDirection = SeekToBackward;
 				mAmplayer.Seek(seekToPos);
 			}
 		} catch (RemoteException e) {
 			mSeekDirection = SeekToNone;
 			mCurrentSeekTime = 0;
 			e.printStackTrace();
 		}
 	}
 
 	private void Amplayer_play(int startPosition) {
 		// stop music player
 
 		Intent intent = new Intent();
 		intent.setAction("com.android.music.musicservicecommand.pause");
 		intent.putExtra("command", "stop");
 		sendBroadcast(intent);
 
 		mSeekDirection = SeekToNone;
 		mCurrentSeekTime = 0;
 
 		mFFToast.cancel();
 		FF_FLAG = false;
 		FB_FLAG = false;
 		FF_LEVEL = 0;
 		FB_LEVEL = 0;
 
 		Log.d(TAG, "Amplayer_play");
 
 		try {
 			// showOSDView();
 
 			// reset sub;
 			mSubTitleView.clear();
 			initSubTitle();
 			initSubtitleView();
 
 			if (SystemProperties.getBoolean("3D_setting.enable", false)) {
 				try {
 					mAmplayer.Set3Dmode(0);
 					mAmplayer.Set3Dviewmode(0);
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 
 				if (mFilePath.indexOf("[3D]") != -1
 						&& mFilePath.indexOf("[HALF]") != -1) {
 					mAmplayer.Set3Dgrating(1);
 					mAmplayer.Set3Dmode(1);
 				} else if (mFilePath.indexOf("[3D]") != -1
 						&& mFilePath.indexOf("[FULL]") != -1) {
 					mAmplayer.Set3Dgrating(1);
 					mAmplayer.Set3Dmode(2);
 					mAmplayer.Set3Daspectfull(1);
 				} else if (mFilePath.indexOf("[3D]") != -1) {
 					mAmplayer.Set3Dgrating(1);
 					mAmplayer.Set3Dmode(2);
 				}
 			}
 
 			if (mSubTitleView_sm != null
 					&& SystemProperties.getBoolean("3D_setting.enable", false)) {
 				mSubTitleView_sm.clear();
 				mSubTitleView_sm.setTextColor(android.graphics.Color.GRAY);
 				mSubTitleView_sm.setTextSize(mSubtitleParameter.font);
 			}
 
 			if (mUri.getScheme().equals("file")) {
 				Log.d(TAG, "++++++++++ Open(" + mUri.getPath() + ")");
 
 				mAmplayer.Open(mUri.getPath(), startPosition);
 			}
 
 			// openFile(mSubtitleParameter.sub_id);
 			mRotateHandler.sendEmptyMessageDelayed(GETROTATION,
 					GETROTATION_TIMEOUT);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void Amplayer_stop() {
 		try {
 			mAmplayer.Stop();
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 		deinitializePlayer();
 
 		mRotateHandler.removeMessages(GETROTATION);
 	}
 
 	void deinitializePlayer() {
 		try {
 			mAmplayer.Close();
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 		AudioTrackOperation.AudioStreamFormat.clear();
 		AudioTrackOperation.AudioStreamInfo.clear();
 		INITOK = false;
 		if (SystemProperties.getBoolean("ro.video.deinterlace.enable", false)) {
 			if (mMediaInfo != null
 					&& mMediaInfo.getWidth() * mMediaInfo.getHeight() < 1280 * 720) {
 				Utils.writeSysfs(Filemap,
 						"rm default decoder deinterlace amvideo");
 				Utils.writeSysfs(Filemap, "add default decoder ppmgr amvideo");
 				Utils.writeSysfs(Filemap, "add default_osd osd amvideo");
 				Utils.writeSysfs(Filemap, "add default_ext vdin amvideo2");
 				Utils.writeSysfs(File_amvdec_h264, "0");
 				Utils.writeSysfs(File_amvdec_mpeg12, "0");
 			}
 		}
 	}
 
 	ServiceConnection mPlayerConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			mAmplayer = IPlayerService.Stub.asInterface(service);
 
 			try {
 				mAmplayer.Init();
 			} catch (RemoteException e) {
 				e.printStackTrace();
 				Log.d(TAG, "init fail!");
 			}
 
 			try {
 				mAmplayer.RegisterClientMessager(m_PlayerMsg.getBinder());
 			} catch (RemoteException e) {
 				e.printStackTrace();
 				Log.e(TAG, "set client fail!");
 			}
 
 			// auto play
 			Log.d(TAG, "to play files!");
 
 			try {
 				final short color = ((0x8 >> 3) << 11) | ((0x30 >> 2) << 5)
 						| ((0x8 >> 3) << 0);
 				mAmplayer.SetColorKey(color);
 				Log.d(TAG, "set colorkey() color=" + color);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 
 			Amplayer_play(mPlayPosition);
 		}
 
 		public void onServiceDisconnected(ComponentName name) {
 			try {
 				mAmplayer.Stop();
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 
 			try {
 				mAmplayer.Close();
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 			mAmplayer = null;
 		}
 	};
 
 	public void startPlayerService() {
 		Intent intent = new Intent();
 		ComponentName component = new ComponentName("com.dbstar.DbstarDVB",
 				"com.dbstar.DbstarDVB.PlayerService.AmPlayer");
 		intent.setComponent(component);
 		startService(intent);
 		bindService(intent, mPlayerConnection, BIND_AUTO_CREATE);
 	}
 
 	public void stopPlayerService() {
 		unbindService(mPlayerConnection);
 		Intent intent = new Intent();
 		ComponentName component = new ComponentName("com.dbstar.DbstarDVB",
 				"com.dbstar.DbstarDVB.PlayerService.AmPlayer");
 		intent.setComponent(component);
 		stopService(intent);
 		mAmplayer = null;
 	}
 
 	// =========================================================
 	private Messenger m_PlayerMsg = new Messenger(new Handler() {
 		Toast tp = null;
 
 		public void handleMessage(Message msg) {
 			Log.d(TAG, " =========== Player msg = " + msg.what);
 			switch (msg.what) {
 			case VideoInfo.TIME_INFO_MSG:
 
 				mCurrentTime = msg.arg1 / 1000;
 				mTotalTime = msg.arg2;
 
 				mCurrentTimeView.setText(Utils.secToTime(mCurrentTime, false));
 				mTotalTimeView.setText(Utils.secToTime(mTotalTime, true));
 
 				boolean mVfdDisplay = SystemProperties.getBoolean("hw.vfd",
 						false);
 				if (mVfdDisplay) {
 					String[] cmdtest = {
 							"/system/bin/sh",
 							"-c",
 							"echo"
 									+ " "
 									+ mCurrentTimeView.getText().toString()
 											.substring(1) + " "
 									+ "> /sys/devices/platform/m1-vfd.0/led" };
 					Utils.do_exec(cmdtest);
 				}
 
 				// for subtitle tick;
 				if (mPlayerStatus == VideoInfo.PLAYER_RUNNING) {
 					if (mSubTitleView != null
 							&& mSubtitleParameter.sub_id != null)
 						mSubTitleView.tick(msg.arg1);
 
 					if (SystemProperties.getBoolean("3D_setting.enable", false)) {
 						if (mSubTitleView_sm != null) {
 							if (View.INVISIBLE == mSubTitleView_sm
 									.getVisibility()) {
 								mSubTitleView_sm.setVisibility(View.VISIBLE);
 							}
 							if (mSubtitleParameter.sub_id != null) {
 								mSubTitleView_sm.tick(msg.arg1);
 							}
 						}
 					}
 				}
 
 				if (mTotalTime == 0)
 					mProgressBar.setProgress(0);
 				else {
 					if ((mSeekDirection == SeekToBackward)
 							&& (mCurrentTime >= (mCurrentSeekTime - 2))) {
 						// Log.d(TAG, "count mCurrentTime: " + mCurrentTime);
 						// Log.d(TAG, "seek mCurrentTime: " + mCurrentSeekTime);
 						return;
 					} else if ((mSeekDirection == SeekToForward)
 							&& (mCurrentTime <= (mCurrentSeekTime + 2))) {
 						// Log.d(TAG, "count mCurrentTime: " + mCurrentTime);
 						// Log.d(TAG, "seek mCurrentTime: " + mCurrentSeekTime);
 						return;
 					}
 					// Log.d(TAG, "mCurrentTime: " + mCurrentTime);
 
 					mSeekDirection = 0;
 					mCurrentSeekTime = 0;
 					mProgressBar.setProgress(msg.arg1 / 10 / mTotalTime);
 					// .setProgress(msg.arg1 / 1000 * 100 / mTotalTime);
 				}
 				break;
 			case VideoInfo.STATUS_CHANGED_INFO_MSG:
 				Log.d(TAG, " ==================== Player status = " + msg.arg1);
 				mPlayerStatus = msg.arg1;
 
 				switch (mPlayerStatus) {
 				case VideoInfo.PLAYER_RUNNING:
 					mPlayButton.setImageResource(R.drawable.pause);
 					String videoFormat = mMediaInfo.getFullFileName(mUri
 							.getPath());
 					if (videoFormat.endsWith(".mvc")) {
 						Utils.writeSysfs(FormatMVC, FormatMVC_3dtb);
 					} else {
 						Utils.writeSysfs(FormatMVC, FormatMVC_3doff);
 					}
 					break;
 				case VideoInfo.PLAYER_PAUSE:
 				case VideoInfo.PLAYER_SEARCHING:
 					mPlayButton.setImageResource(R.drawable.play);
 					break;
 				case VideoInfo.PLAYER_EXIT:
 					Log.d(TAG, "VideoInfo.PLAYER_EXIT");
 
 					closeSubtitleView();
 
 					mSubtitleParameter.totalnum = 0;
 					InternalSubtitleInfo.setInsubNum(0);
 
 					boolean mVfdDisplay_exit = SystemProperties.getBoolean(
 							"hw.vfd", false);
 					if (mVfdDisplay_exit) {
 						String[] cmdtest = {
 								"/system/bin/sh",
 								"-c",
 								"echo"
 										+ " "
 										+ "0:00:00"
 										+ " "
 										+ "> /sys/devices/platform/m1-vfd.0/led" };
 						Utils.do_exec(cmdtest);
 					}
 					break;
 				case VideoInfo.PLAYER_STOPED:
 					break;
 				case VideoInfo.PLAYER_PLAYEND:
 
 					deinitializePlayer();
 					ResumePlay.saveResumePara(mFilePath, 0);
 					mPlayPosition = 0;
 					break;
 				case VideoInfo.PLAYER_ERROR:
 					String InfoStr = null;
 					InfoStr = Errorno.getErrorInfo(msg.arg2);
 					if (tp == null) {
 						tp = Toast.makeText(PlayerMenu.this, "Status Error:"
 								+ InfoStr, Toast.LENGTH_SHORT);
 					} else {
 						tp.cancel();
 						tp.setText("Status Error:" + InfoStr);
 					}
 					tp.show();
 					Log.d(TAG,
 							"Player error, msg.arg2 = "
 									+ Integer.toString(msg.arg2));
 					if (msg.arg2 < 0) {
 
 						if (mAmplayer != null)
 							Amplayer_stop();
 
 						ResumePlay.saveResumePara(mFilePath, 0);
 						mPlayPosition = 0;
 
 						finish();
 					}
 					break;
 				case VideoInfo.PLAYER_INITOK:
 					INITOK = true;
 					try {
 						mMediaInfo = mAmplayer.GetMediaInfo();
 						if (SystemProperties.getBoolean(
 								"ro.video.deinterlace.enable", false)) {
 							if (mMediaInfo != null
 									&& mMediaInfo.getWidth()
 											* mMediaInfo.getHeight() < 1280 * 720) {
 								Utils.writeSysfs(Filemap,
 										"rm default decoder ppmgr amvideo");
 								Utils.writeSysfs(Filemap,
 										"rm default_osd osd amvideo");
 								Utils.writeSysfs(Filemap,
 										"rm default_ext vdin amvideo2");
 								Utils.writeSysfs(Filemap,
 										"add default decoder deinterlace amvideo");
 								Utils.writeSysfs(File_amvdec_h264, "3");
 								Utils.writeSysfs(File_amvdec_mpeg12, "14");
 							}
 						}
 					} catch (RemoteException e) {
 						e.printStackTrace();
 					}
 
 					if ((mMediaInfo != null) && (mSubTitleView != null)) {
 						mSubTitleView
 								.setDisplayResolution(SettingsVP.panel_width,
 										SettingsVP.panel_height);
 						mSubTitleView.setVideoResolution(mMediaInfo.getWidth(),
 								mMediaInfo.getHeight());
 					}
 					if (mMediaInfo != null
 							&& mSubTitleView_sm != null
 							&& SystemProperties.getBoolean("3D_setting.enable",
 									false)) {
 						mSubTitleView_sm
 								.setDisplayResolution(SettingsVP.panel_width,
 										SettingsVP.panel_height);
 						mSubTitleView_sm.setVideoResolution(
 								mMediaInfo.getWidth(), mMediaInfo.getHeight());
 					}
 					if (SystemProperties.getBoolean("3D_setting.enable", false)
 							&& mMediaInfo.getVideoFormat().compareToIgnoreCase(
 									"H264MVC") == 0) {// if 264mvc,set auto
 														// mode.
 						try {
 							mAmplayer.Set3Dgrating(1); // open grating
 							mAmplayer.Set3Dmode(1);
 
 						} catch (RemoteException e) {
 							e.printStackTrace();
 						}
 					}
 
 					if (mMediaInfo.drm_check == 0) {
 						try {
 							mAmplayer.Play();
 						} catch (RemoteException e) {
 							e.printStackTrace();
 						}
 					}
 
 					mSubtitleParameter.totalnum = mSubtitleUtils
 							.getExSubTotal()
 							+ InternalSubtitleInfo.getInsubNum();
 					if (mSubtitleParameter.totalnum > 0) {
 						mSubtitleParameter.curid = mSubtitleUtils
 								.getCurrentInSubtitleIndexByJni();
 						if (mSubtitleParameter.curid == 0xff
 								|| mSubtitleParameter.enable == false)
 							mSubtitleParameter.curid = mSubtitleParameter.totalnum;
 						if (mSubtitleParameter.totalnum > 0)
 							mSubtitleParameter.sub_id = mSubtitleUtils
 									.getSubID(mSubtitleParameter.curid);
 						else
 							mSubtitleParameter.sub_id = null;
 
 						initSubtitleView();
 						openFile(mSubtitleParameter.sub_id);
 					} else {
 						mSubtitleParameter.sub_id = null;
 					}
 
 					if (mMediaInfo.seekable == 0) {
 						mProgressBar.setEnabled(false);
 					} else {
 						mProgressBar.setEnabled(true);
 					}
 
 					if (setCodecMips() != 0) {
 						Log.d(TAG, "setCodecMips Failed");
 					}
 
 					if (SystemProperties.getBoolean(
 							"vplayer.hideStatusBar.enable", false) == false) {
 						Log.d(TAG, "hideStatusBar is false");
 						try {
 							SystemProperties.set(
 									"vplayer.hideStatusBar.enable", "true");
 						} catch (RuntimeException e) {
 							e.printStackTrace();
 						}
 					}
 					break;
 				case VideoInfo.PLAYER_SEARCHOK:
 					break;
 				case VideoInfo.DIVX_AUTHOR_ERR: {
 					Log.d(TAG, "Authorize Error");
 					DivxInfo divxInfo = null;
 					try {
 						divxInfo = mAmplayer.GetDivxInfo();
 
 					} catch (RemoteException e) {
 						e.printStackTrace();
 					}
 
 					if (divxInfo != null) {
 						alertDivxAuthorError(divxInfo, msg.arg2);
 					}
 					break;
 				}
 				case VideoInfo.DIVX_EXPIRED: {
 					Log.d(TAG, "Authorize Expired");
 					DivxInfo divxInfo = null;
 					try {
 						divxInfo = mAmplayer.GetDivxInfo();
 					} catch (RemoteException e) {
 						e.printStackTrace();
 					}
 
 					if (divxInfo != null) {
 						alertDivxExpired(divxInfo, msg.arg2);
 					}
 					break;
 				}
 				case VideoInfo.DIVX_RENTAL: {
 					Log.d(TAG, "Authorize rental");
 					DivxInfo divxInfo = null;
 					try {
 						divxInfo = mAmplayer.GetDivxInfo();
 					} catch (RemoteException e) {
 						e.printStackTrace();
 					}
 
 					if (divxInfo != null) {
 						alertDivxRental(divxInfo, msg.arg2);
 					}
 
 					break;
 				}
 				default:
 					break;
 				}
 				break;
 
 			case VideoInfo.AUDIO_CHANGED_INFO_MSG:
 				// total_audio_num = msg.arg1;
 				// cur_audio_stream = msg.arg2;
 				break;
 			case VideoInfo.HAS_ERROR_MSG:
 				String errStr = null;
 				errStr = Errorno.getErrorInfo(msg.arg2);
 				if (tp == null) {
 					tp = Toast.makeText(PlayerMenu.this, errStr,
 							Toast.LENGTH_SHORT);
 				} else {
 					tp.cancel();
 					tp.setText(errStr);
 				}
 				tp.show();
 				break;
 			default:
 				super.handleMessage(msg);
 				break;
 			}
 		}
 	});
 
 	private static SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
 		public void surfaceChanged(SurfaceHolder holder, int format, int w,
 				int h) {
 			Log.d(TAG, "surfaceChanged");
 		}
 
 		public void surfaceCreated(SurfaceHolder holder) {
 			Log.d(TAG, "surfaceCreated");
 			initSurface(holder);
 		}
 
 		public void surfaceDestroyed(SurfaceHolder holder) {
 			Log.d(TAG, "surfaceDestroyed");
 		}
 
 		private void initSurface(SurfaceHolder h) {
 			Canvas c = null;
 			try {
 				Log.d(TAG, "initSurface");
 				c = h.lockCanvas();
 			} finally {
 				if (c != null)
 					h.unlockCanvasAndPost(c);
 			}
 		}
 	};
 
 	void registerHDMIReceiver() {
 		IntentFilter intentFilter = new IntentFilter(
 				WindowManagerPolicy.ACTION_HDMI_PLUGGED);
 
 		Intent intent = registerReceiver(mHDMIEventReceiver, intentFilter);
 		if (intent != null) {
 			// Retrieve current sticky dock event broadcast.
 			mHdmiPlugged = intent.getBooleanExtra(
 					WindowManagerPolicy.EXTRA_HDMI_PLUGGED_STATE, false);
 		}
 	}
 
 	private BroadcastReceiver mHDMIEventReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			boolean plugged = intent.getBooleanExtra(
 					WindowManagerPolicy.EXTRA_HDMI_PLUGGED_STATE, false);
 
 			if (!SystemProperties.getBoolean("ro.vout.player.exit", true)) {
 				SettingsVP.setVideoLayoutMode();
 				mInfoBar = null;
 				initOSDView();
 				return;
 			}
 
 			if (!SystemProperties.getBoolean("ro.vout.dualdisplay", false)) {
 				if (mHdmiPlugged != plugged) {
 					mHdmiPlugged = plugged;
 					finish();
 				}
 			}
 		}
 	};
 
 	void registerCommandReceiver() {
 		IntentFilter intentFilter = new IntentFilter(Common.ActionReplay);
 		intentFilter.addAction(Common.ActionExit);
 		registerReceiver(mPlayerCommandReceiver, intentFilter);
 	}
 
 	void unregisterCommandReceiver() {
 		unregisterReceiver(mPlayerCommandReceiver);
 	}
 
 	private BroadcastReceiver mPlayerCommandReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 
 			if (action.equals(Common.ActionReplay)) {
 				Amplayer_play(0);
 			} else if (action.equals(Common.ActionExit)) {
 				exitPlayer();
 			}
 		}
 	};
 
 	void registerUSBReceiver() {
 		IntentFilter intentFilter = new IntentFilter(
 				Intent.ACTION_MEDIA_MOUNTED);
 		intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
 		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
 		intentFilter.addDataScheme("file");
 		registerReceiver(mMountReceiver, intentFilter);
 	}
 
 	private BroadcastReceiver mMountReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			Uri uri = intent.getData();
 			String path = uri.getPath();
 
 			if (action == null || path == null)
 				return;
 
 			if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
 				if (mFilePath != null) {
 					if (mFilePath.startsWith(path)) {
 						closeSubtitleView();
 						// stop play
 						if (mAmplayer != null)
 							Amplayer_stop();
 
 						finish();
 					}
 				}
 			} else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
 				// Nothing
 			} else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
 				// SD card unavailable
 				// handled in ACTION_MEDIA_EJECT
 			}
 		}
 	};
 
 	UncaughtExceptionHandler mExceptionHandler = new UncaughtExceptionHandler() {
 		public void uncaughtException(Thread thread, Throwable ex) {
 
 			Log.d(TAG, " ===================== " + ex.getMessage());
 
 			SystemProperties.set("vplayer.hideStatusBar.enable", "false");
 			SystemProperties.set("vplayer.playing", "false");
 
 			if (SettingsVP.chkEnableOSD2XScale() == true) {
 				if (mInfoBar != null) {
 					mInfoBar.setVisibility(View.GONE);
 				}
 			}
 
 			closeSubtitleView();
 
 			if (!mFB32) {
 				// Hide the view with key color
 				FrameLayout layout = (FrameLayout) findViewById(R.id.BaseLayout1);
 				if (layout != null) {
 					layout.setVisibility(View.INVISIBLE);
 					layout.invalidate();
 				}
 			}
 			// stop play
 			if (mAmplayer != null)
 				Amplayer_stop();
 
 			onPause(); // for disable 2Xscale
 			finish(); // will call onDestroy()
 			onDestroy(); // set freescale when exception
 			Log.d(TAG, "----------------uncaughtException--------------------");
 
 			android.os.Process.killProcess(android.os.Process.myPid());
 		}
 	};
 
 	private static final int MSG_DIALOG_POPUP = 1;
 	private static final int MSG_DIALOG_TIMEOUT = 500;
 
 	DbVideoInfoDlg mVideoInfoDlg = null;
 	Handler mDialogHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_DIALOG_POPUP:
 				showDialog(MSG_DIALOG_POPUP);
 				break;
 			default:
 				break;
 			}
 		}
 	};
 
 	Handler mRotateHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case GETROTATION:
 				int getRotation = mWindowManager.getDefaultDisplay()
 						.getRotation();
 				// Log.d("sensor",
 				// "rotate angle: "+Integer.toString(getRotation));
 				if ((getRotation >= 0) && (getRotation <= 3)
 						&& (getRotation != mLastRotation)) {
 					SettingsVP.setVideoRotateAngle(angle_table[getRotation]);
 					mLastRotation = getRotation;
 				}
 				mRotateHandler.sendEmptyMessageDelayed(GETROTATION,
 						GETROTATION_TIMEOUT);
 				break;
 			}
 			super.handleMessage(msg);
 		}
 	};
 
 	protected void keepScreenOn() {
 		if (mScreenLock.isHeld() == false)
 			mScreenLock.acquire();
 	}
 
 	protected void keepScreenOff() {
 		if (mScreenLock.isHeld() == true)
 			mScreenLock.release();
 	}
 
 	public int setCodecMips() {
 		String buf = null;
 
 		mCodecMIPS = Utils.readSysfs(InputFile);
 		if (mCodecMIPS != null) {
 			int tmp = Integer.parseInt(mCodecMIPS) * 2;
 			buf = Integer.toString(tmp);
 
 			return Utils.writeSysfs(OutputFile, buf);
 		}
 
 		return 1;
 	}
 
 	public int setDefCodecMips() {
 		if (mCodecMIPS == null)
 			return 1;
 
 		Log.d(TAG, "set codec mips ok:" + mCodecMIPS);
 		return Utils.writeSysfs(OutputFile, mCodecMIPS);
 	}
 
 	public int set2XScale() {
 		if (SettingsVP.chkEnableOSD2XScale() == false)
 			return 0;
 		bSet2XScale = true;
 		Log.d(TAG, "request  2XScale");
 		return Utils.writeSysfs(RequestScaleFile, " 1 ");
 	}
 
 	public int disable2XScale() {
 		if (bSet2XScale == false)
 			return 0;
 		bSet2XScale = false;
 		Log.d(TAG, "request disable2XScale");
 
 		return Utils.writeSysfs(RequestScaleFile, " 2 ");
 	}
 
 	private void displayInit() {
 		int mode = SettingsVP.getParaInt(SettingsVP.DISPLAY_MODE);
 		switch (mode) {
 		case ScreenMode.NORMAL:
 			ScreenMode.setScreenMode("0");
 			break;
 		case ScreenMode.FULLSTRETCH:
 			ScreenMode.setScreenMode("1");
 			break;
 		case ScreenMode.RATIO4_3:
 			ScreenMode.setScreenMode("2");
 			break;
 		case ScreenMode.RATIO16_9:
 			ScreenMode.setScreenMode("3");
 			break;
 
 		default:
 			Log.e(TAG, "load display mode para error!");
 			break;
 		}
 	}
 
 	TimerTask mHideInfoBarTask = null;
 
 	protected void hideInfoBarDelayed() {
 		final Handler handler = new Handler() {
 
 			public void handleMessage(Message msg) {
 				switch (msg.what) {
 				case 0x4c:
 					Log.d(TAG, "================hide info bar ==============");
 					mHideInfoBarTask.cancel();
 					mHideInfoBarTask = null;
 					if (!mDuringKeyActions) {
 						hideInfoBar();
 					}
 					break;
 				}
 				super.handleMessage(msg);
 			}
 
 		};
 
 		if (mHideInfoBarTask != null) {
 			mHideInfoBarTask.cancel();
 		}
 
 		mHideInfoBarTask = new TimerTask() {
 
 			public void run() {
 				Message message = Message.obtain();
 				message.what = 0x4c;
 				handler.sendMessage(message);
 			}
 		};
 
 		mInfoBarTimer.schedule(mHideInfoBarTask, 5000);
 	}
 
 	private void hideOSDView() {
 
 		hideInfoBar();
 
 		setOSDOn(false);
 	}
 
 	private void showOSDView() {
 		setOSDOn(true);
 		showInfoBar(true);
 	}
 
 	private void hideInfoBar() {
 		if (null != mInfoBar) {
 			mInfoBar.setVisibility(View.GONE);
 		}
 	}
 
 	private void showInfoBar(boolean hideDelayed) {
 
 		if (mOSDState != OSDShow) {
 			setOSDOn(true);
 		}
 
 		if (mInfoBar == null) {
 			return;
 		}
 
 		if (mInfoBar.getVisibility() == View.GONE) {
 			mInfoBar.setVisibility(View.VISIBLE);
 			// mInfoBar.requestFocus();
 		}
 
 		if (mInfoBar.getVisibility() == View.VISIBLE) {
 			if (hideDelayed) {
 				hideInfoBarDelayed();
 			}
 		}
 	}
 
 	private int getOSDRotation() {
 		Display display = getWindowManager().getDefaultDisplay();
 		int orientation = display.getOrientation();
 		int hw_rotation = SystemProperties.getInt("ro.sf.hwrotation", 0);
 		return (orientation * 90 + hw_rotation) % 360;
 	}
 
 	void setOSDOn(boolean on) {
 
 		if (!on && !mPaused) {
 			if (isSubtitleOn()) {
 				mOSDState = OSDHidePart;
 
 				int ori = getOSDRotation();
 				if (ori == 90)
 					Utils.writeSysfs(OSD_BLOCK_MODE_PATH, "0x20001"); // OSD ver
 																		// blk0
 				// enable
 				else if (ori == 180)
 					Utils.writeSysfs(OSD_BLOCK_MODE_PATH, "0x10001"); // OSD hor
 																		// blk0
 				// enable
 				else if (ori == 270)
 					Utils.writeSysfs(OSD_BLOCK_MODE_PATH, "0x20008"); // OSD ver
 																		// blk3
 				// enable
 				else
 					Utils.writeSysfs(OSD_BLOCK_MODE_PATH, "0x10008"); // OSD hor
 																		// blk3
 				// enable
 
 			} else {
 				mOSDState = OSDHideAll;
 				Utils.writeSysfs(OSD_BLANK_PATH, "1");
 				AmPlayer.setOSDOnFlag(false);
 			}
 		} else {
 			mOSDState = OSDShow;
 			Utils.writeSysfs(OSD_BLANK_PATH, "0");
 			Utils.writeSysfs(OSD_BLOCK_MODE_PATH, "0");
 			AmPlayer.setOSDOnFlag(true);
 		}
 	}
 
 	private void initVideoView(int resourceId) {
 		if (mFB32) {
 			Log.d(TAG, "initVideoView");
 			SurfaceView v = (SurfaceView) findViewById(resourceId);
 			if (v != null) {
 				Log.d(TAG, "initVideoView 2");
 				v.getHolder().addCallback(mSHCallback);
 				v.getHolder().setFormat(PixelFormat.VIDEO_HOLE);
 			}
 		} else
 			Log.d(TAG, "!initVideoView");
 	}
 
 	protected void initOSDView() {
 		mInfoBar = (LinearLayout) findViewById(R.id.infobarLayout);
 		mPlayButton = (ImageView) findViewById(R.id.PlayBtn);
 		mProgressBar = (SeekBar) findViewById(R.id.SeekBar02);
 		mCurrentTimeView = (TextView) findViewById(R.id.TextView03);
 		mTotalTimeView = (TextView) findViewById(R.id.TextView04);
 
 		mSoundStateView = (ImageView) findViewById(R.id.sound_indicator);
 		mSoundVolumeView = (ImageView) findViewById(R.id.volume_indicator);
 
 		mInfoBar.setVisibility(View.GONE);
 
 		initVideoView(R.id.VideoView);
 
 		// set subtitle
 		initSubtitleView();
 
 		if (SystemProperties.getBoolean("3D_setting.enable", false)) {
 			mSubTitleView_sm = (SubtitleView) findViewById(R.id.subTitle_sm);
 			mSubTitleView_sm.setGravity(Gravity.CENTER);
 			mSubTitleView_sm.setTextColor(android.graphics.Color.GRAY);
 			mSubTitleView_sm.setTextSize(mSubtitleParameter.font);
 			mSubTitleView_sm.setTextStyle(Typeface.BOLD);
 		}
 
 		LinearLayout.LayoutParams linearParams = null;
 
 		if (AmPlayer.getProductType() == 1) {
 			if (SettingsVP.display_mode.equals("480p")
 					&& SettingsVP.panel_height > 480) {
 				linearParams = (LinearLayout.LayoutParams) mSubTitleView
 						.getLayoutParams();
 				if (SettingsVP.panel_width > 720)
 					linearParams.width = 720;
 				linearParams.bottomMargin = SettingsVP.panel_height - 480 + 10;
 				mSubTitleView.setLayoutParams(linearParams);
 				if (mSubTitleView_sm != null
 						&& SystemProperties.getBoolean("3D_setting.enable",
 								false)) {
 					mSubTitleView_sm.setLayoutParams(linearParams);
 				}
 			} else if (SettingsVP.display_mode.equals("720p")
 					&& SettingsVP.panel_height > 720) {
 				linearParams = (LinearLayout.LayoutParams) mSubTitleView
 						.getLayoutParams();
 				if (SettingsVP.panel_width > 1280)
 					linearParams.width = 1280;
 				linearParams.bottomMargin = SettingsVP.panel_height - 720 + 10;
 				mSubTitleView.setLayoutParams(linearParams);
 				if (mSubTitleView_sm != null
 						&& SystemProperties.getBoolean("3D_setting.enable",
 								false)) {
 					mSubTitleView_sm.setLayoutParams(linearParams);
 				}
 			}
 		}
 
 		if (m1080scale == 2) {
 			linearParams = (LinearLayout.LayoutParams) mSubTitleView
 					.getLayoutParams();
 			linearParams.leftMargin = 50;
 			linearParams.width = 1180;
 			linearParams.bottomMargin = 40;
 			mSubTitleView.setLayoutParams(linearParams);
 			if (mSubTitleView_sm != null
 					&& SystemProperties.getBoolean("3D_setting.enable", false)) {
 				mSubTitleView_sm.setLayoutParams(linearParams);
 			}
 		}
 
 		openFile(mSubtitleParameter.sub_id);
 
 		mCurrentTimeView.setText(Utils.secToTime(mCurrentTime, false));
 		mTotalTimeView.setText(Utils.secToTime(mTotalTime, true));
 
 		if (mMediaInfo != null) {
 			if (mMediaInfo.seekable == 0) {
 				mProgressBar.setEnabled(false);
 			}
 		}
 
 		if (mPlayerStatus == VideoInfo.PLAYER_RUNNING)
 			mPlayButton.setImageResource(R.drawable.pause);
 
 		if (mCurrentTime != 0)
 			mProgressBar.setProgress(mCurrentTime * 100 / mTotalTime);
 
 		mProgressBar.setOnSeekBarChangeListener(mProgressChangeListener);
 
 		mAudioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
 		mMaxVolumeLevel = mAudioManager
 				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 		mVolumeLevel = mAudioManager
 				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 
 		mVolumeLevelIndex = getVolumeLevelIndex(mVolumeLevel);
 		mSoundVolumeView.setImageLevel(mVolumeLevelIndex);
 	}
 
 	SeekBar.OnSeekBarChangeListener mProgressChangeListener = new SeekBar.OnSeekBarChangeListener() {
 		public void onStopTrackingTouch(SeekBar seekBar) {
 		}
 
 		public void onStartTrackingTouch(SeekBar seekBar) {
 		}
 
 		public void onProgressChanged(SeekBar seekBar, int progress,
 				boolean fromUser) {
 			if (fromUser == true) {
 				seekPlayback(progress);
 			}
 		}
 	};
 
 	public void initAngleTable() {
 		String hwrotation = SystemProperties.get("ro.sf.hwrotation");
 		if (hwrotation == null) {
 			angle_table[0] = 0;
 			angle_table[1] = 1;
 			angle_table[2] = 2;
 			angle_table[3] = 3;
 			Log.e(TAG, "initAngleTable, Can not get hw rotation!");
 			return;
 		}
 
 		if (hwrotation.equals("90")) {
 			angle_table[0] = 1;
 			angle_table[1] = 2;
 			angle_table[2] = 3;
 			angle_table[3] = 0;
 		} else if (hwrotation.equals("180")) {
 			angle_table[0] = 2;
 			angle_table[1] = 3;
 			angle_table[2] = 0;
 			angle_table[3] = 1;
 		} else if (hwrotation.equals("270")) {
 			angle_table[0] = 3;
 			angle_table[1] = 0;
 			angle_table[2] = 1;
 			angle_table[3] = 2;
 		} else {
 			angle_table[0] = 0;
 			angle_table[1] = 1;
 			angle_table[2] = 2;
 			angle_table[3] = 3;
 		}
 	}
 
 	// ----------------- Subtitle related ---------------------------------
 
 	protected void initSubTitle() {
 		// mSubtitleUtils = new SubtitleUtils(PlayList.getinstance().getcur());
 		mSubtitleUtils = new SubtitleUtils(mFilePath);
 		mSubtitleParameter = new SubtitleParameter();
 
 		mSubtitleParameter.totalnum = 0;
 		mSubtitleParameter.curid = 0;
 		// add by jeff.yang
 		SharedPreferences settings = getSharedPreferences(PREFS_SUBTITLE_NAME,
 				0);
 		mSubtitleParameter.enable = settings.getBoolean("enable", true);
 		mSubtitleParameter.color = settings.getInt("color",
 				android.graphics.Color.WHITE); // android.graphics.Color.WHITE;
 		mSubtitleParameter.font = settings.getInt("font", 20);// 20;
 		mSubtitleParameter.position_v = settings.getInt("position_v", 0);// 0;
 
 		mSubtitleParameter.sub_id = null;
 	}
 
 	private void initSubtitleView() {
 		mSubTitleView = (SubtitleView) findViewById(R.id.subTitle);
 		mSubTitleView.clear();
 		mSubTitleView.setGravity(Gravity.CENTER);
 		mSubTitleView.setTextColor(mSubtitleParameter.color);
 		mSubTitleView.setTextSize(mSubtitleParameter.font);
 		mSubTitleView.setTextStyle(Typeface.BOLD);
 		mSubTitleView.setPadding(mSubTitleView.getPaddingLeft(),
 				mSubTitleView.getPaddingTop(), mSubTitleView.getPaddingRight(),
 				getWindowManager().getDefaultDisplay().getRawHeight()
 						* mSubtitleParameter.position_v / 20 + 10);
 	}
 
 	private boolean isSubtitleOn() {
 		if (mSubtitleParameter != null && mSubtitleParameter.totalnum > 0
 				&& mSubtitleParameter.sub_id != null) {
 			AmPlayer.setSubOnFlag(true);
 			return true;
 		} else {
 			AmPlayer.setSubOnFlag(false);
 			return false;
 		}
 	}
 
 	void closeSubtitleView() {
 		if (mSubTitleView != null) {
 			mSubTitleView.closeSubtitle();
 			mSubTitleView.clear();
 		}
 		if (mSubTitleView_sm != null
 				&& SystemProperties.getBoolean("3D_setting.enable", false)) {
 			mSubTitleView_sm.closeSubtitle();
 			mSubTitleView_sm.clear();
 		}
 	}
 
 	private String setSublanguage() {
 		String type = null;
 		String able = getResources().getConfiguration().locale.getCountry();
 
 		if (able.equals("TW"))
 			type = "BIG5";
 		else if (able.equals("JP"))
 			type = "cp932";
 		else if (able.equals("KR"))
 			type = "cp949";
 		else if (able.equals("IT") || able.equals("FR") || able.equals("DE"))
 			type = "iso88591";
 		else
 			type = "GBK";
 
 		return type;
 	}
 
 	private void openFile(SubID filepath) {
 
 		if (filepath == null)
 			return;
 
 		setSublanguage();
 
 		try {
 			if (mSubTitleView.setFile(filepath, setSublanguage()) == Subtitle.SUBTYPE.SUB_INVALID)
 				return;
 			if (mSubTitleView_sm != null
 					&& SystemProperties.getBoolean("3D_setting.enable", false)) {
 				if (mSubTitleView_sm.setFile(filepath, setSublanguage()) == Subtitle.SUBTYPE.SUB_INVALID) {
 					return;
 				}
 			}
 
 		} catch (Exception e) {
 			Log.d(TAG, "open:error");
 			mSubTitleView = null;
 			if (mSubTitleView_sm != null
 					&& SystemProperties.getBoolean("3D_setting.enable", false)) {
 				mSubTitleView_sm = null;
 			}
 			e.printStackTrace();
 		}
 	}
 
 	// --------------------- Divx alert handler ----------------------------
 	void alertDivxExpired(DivxInfo divxInfo, int args) {
 		String s = "This rental has " + args
 				+ " views left\nDo you want to use one of your " + args
 				+ " views now";
 		new AlertDialog.Builder(PlayerMenu.this)
 				.setTitle("View DivX(R) VOD Rental").setMessage(s)
 				.setPositiveButton(R.string.str_ok, mAlertButtonClickListener)
 				.show();
 	}
 
 	void alertDivxAuthorError(DivxInfo divxInfo, int args) {
 		new AlertDialog.Builder(this)
 				.setTitle("Authorization Error")
 				.setMessage(
 						"This player is not authorized to play this DivX protected video")
 				.setPositiveButton(R.string.str_ok, mAlertButtonClickListener)
 				.show();
 	}
 
 	void alertDivxRental(DivxInfo divxInfo, int args) {
 		String s = "This rental has " + args
 				+ " views left\nDo you want to use one of your " + args
 				+ " views now?";
 		new AlertDialog.Builder(PlayerMenu.this)
 				.setTitle("View DivX(R) VOD Rental")
 				.setMessage(s)
 				.setPositiveButton(R.string.str_ok,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int whichButton) {
 								// finish();
 								try {
 									mAmplayer.Play();
 								} catch (RemoteException e) {
 									e.printStackTrace();
 								}
 							}
 						})
 				.setNegativeButton(R.string.str_cancel,
 						mAlertButtonClickListener).show();
 	}
 
 	DialogInterface.OnClickListener mAlertButtonClickListener = new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int whichButton) {
 			// close sub;
 			if (mSubTitleView != null)
 				mSubTitleView.closeSubtitle();
 
 			if (mSubTitleView_sm != null
 					&& SystemProperties.getBoolean("3D_setting.enable", false)) {
 				mSubTitleView_sm.closeSubtitle();
 			}
 
 			if (!mFB32) {
 				// Hide the view with key color
 				LinearLayout layout = (LinearLayout) findViewById(R.id.BaseLayout1);
 				if (layout != null) {
 					layout.setVisibility(View.INVISIBLE);
 					layout.invalidate();
 				}
 			}
 			// stop play
 			if (mAmplayer != null)
 				Amplayer_stop();
 
 			finish();
 		}
 	};
 }
