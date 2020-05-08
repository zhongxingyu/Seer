 package com.main.harriscam;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.hardware.Camera.Size;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.net.Uri;
import android.opengl.Visibility;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	// Load a library
 	static {
 		System.loadLibrary("imgeffects");
 	}
 
 	// Harris Shutter Effect
 	private static native void applyHarris(Bitmap bitG, Bitmap bitR, Bitmap bitB);
 
 	// Old Screen Effect
 	private static native void applyScreen(Bitmap bitResult, Bitmap bitOrigin, Bitmap bitTemp);
 
 	boolean bEnd = false;
 	Handler mHandler;
 
 	private Preview mPreview;
 
 	// Constant about state and properties
 	private static final int FLASH_OFF = 0;
 	private static final int FLASH_ON = 1;
 	private static final int FLASH_AUTO = 2;
 	public static final int SAVE_HIGH = 840;
 	public static final int SAVE_MIDDLE = 600;
 	public static final int SAVE_LOW = 360;
 	private static final int AUDIO_FOCUS = 0;
 	private static final int ACTIVITY_CROP = 0;
 	private static final int ACTIVITY_SETTINGS = 1;
 	private static final int DIALOG_VERSION = 0;
 
 	private static final int RESULT_FAILED = 999;
 
 	// ID about view
 	private static final int ID_UPPERLAYOUT = 101;
 	private static final int ID_LOWERLAYOUT = 102;
 	private static final int ID_AUTOSHOT = 103;
 	private static final int ID_FLASH = 104;
 	private static final int ID_SHUTTER = 105;
 	private static final int ID_TURNOVER = 106;
 	private static final int ID_UPPERBLIND = 107;
 	private static final int ID_LOWERBLIND = 108;
 	private static final int ID_SETTINGS = 109;
 	private static final int ID_MODE = 109;
 
 	Camera.PictureCallback mPictureCallbackJpeg;
 	Camera.ShutterCallback mShutterCallback;
 	Camera.PictureCallback mPictureCallbackRaw;
 	Camera.AutoFocusCallback mAutoFocusCallback;
 	byte byOriImage[][]; // Opened JPEG byte file is here.
 	// Opened original image filename is here.
 	public static String strOriFilename;
 	// 0: First Shot, 1: Second Shot, 2: Last Shot
 	public static Bitmap bitOpened[];
 	public static Bitmap bitResultImage;
 	long LastTime; // Difference time.
 	boolean bAutoCapture = true; // Using an autoshot?
 	int nAutoInterval = 500; // Autoshot time interval.
 	public static int nTimes = 0; // Manual shot times.
 	boolean bMultiMode = true; // Using harris mode?
 
 	LinearLayout llButtons, llUpperButtons, llPreview, llUpperBlind, llLowerBlind;
 	FrameLayout flPreview;
 	public static ImageButton ibShutter, ibTurn, ibAutoshot, ibFlash, ibSettings, ibMode;
 
 	public static int displayWidth = 0; // Device's display size information.
 	public static int displayHeight = 0;
 
 	public static boolean bFrontCam = false; // Using an front camera?
 	int bFlash = FLASH_OFF; // Flashlight state.
 
 	Intent iCropActivity;
 	Intent iSettingsActivity;
 
 	// I wanna play a auto focus sound!@#@!
 	private SoundPool mSoundPool;
 	private HashMap<Integer, Integer> mSoundPoolMap;
 
 	// Advanced Users settings
 	public static boolean bOriginalAutoSave = true;
 	public static int nSaveResolution = SAVE_MIDDLE;
 	public static boolean bScaledSquare = true;
 	public static int nCameraOffset = 0;
 	public static int nImageOffset = 0;
 	public static boolean bAutoFocusBeep = true;
 	public static boolean bAutoUpdate = false;
 	public static String strAppVersion = null;
 	public static String strLatestVersion = null;
 	public static Locale lcLanguage = null;
 	public static PackageInfo piPackageInfo = null;
 
 	public static String strFilePath = null;
 	public static int nSampleSize = 0;
 
 	ApplyHarrisShutter threadApplyHarris;
 
 	public static String strVersionAddress = "http://cfs.tistory.com/custom/blog/75/751637/skin/images/harrisversion.html";
 	HttpURLConnection connVersionCheck;
 
 	public static boolean bStarted = false;
 	public static boolean bLatestVersion = false;
 
 	public static LocalString lsSTRINGs = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
 
 		displayWidth = display.getWidth();
 		displayHeight = display.getHeight();
 
 		iCropActivity = new Intent(this, CropActivity.class);
 		iSettingsActivity = new Intent(this, SettingsActivity.class);
 
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
 		// For finish this application.
 		mHandler = new Handler() {
 			public void handleMessage(Message msg) {
 				if (msg.what == 0) {
 					bEnd = false;
 				}
 			}
 		};
 
 		byOriImage = new byte[3][];
 		bitOpened = new Bitmap[3];
 
 		mSoundPool = new SoundPool(16, AudioManager.STREAM_MUSIC, 0);
 		mSoundPoolMap = new HashMap<Integer, Integer>();
 
 		mSoundPoolMap.put(AUDIO_FOCUS, mSoundPool.load(getBaseContext(), R.raw.focus, 1));
 
 		mPictureCallbackJpeg = new Camera.PictureCallback() {
 
 			public void onPictureTaken(byte[] data, Camera camera) {
 				if (nTimes == 0) {
 					ibShutter.setBackgroundResource(R.drawable.shutter_2);
 				} else if (nTimes == 1) {
 					ibShutter.setBackgroundResource(R.drawable.shutter_1);
 				}
 
 				byOriImage[nTimes] = data;
 
 				try {
 
 					if (camera != null) {
 						camera.lock();
 						camera.startPreview();
 					}
 
 					if (bMultiMode == true) {
 						if (bAutoCapture == true) {
 							while (System.currentTimeMillis() - LastTime <= nAutoInterval) {
 							}
 
 							LastTime = System.currentTimeMillis();
 
 							if (nTimes < 2) {
 								if (camera != null) {
 									nTimes++;
 									camera.takePicture(mShutterCallback, mPictureCallbackRaw, mPictureCallbackJpeg);
 								}
 							} else {
 								ibShutter.setBackgroundResource(R.drawable.shutter_auto);
 
 								readyOpenImage();
 							}
 						} else {
 							if (nTimes < 2) {
 								if (camera != null) {
 									nTimes++;
 
 									if (nTimes == 1) {
 										ibShutter.setBackgroundResource(R.drawable.shutter_2);
 									} else if (nTimes == 2) {
 										ibShutter.setBackgroundResource(R.drawable.shutter_1);
 									}
 								}
 							} else {
 								ibShutter.setBackgroundResource(R.drawable.shutter_3);
 
 								readyOpenImage();
 							}
 						}
 					} else {
 						readyOpenImage();
 					}
 				} catch (Exception e) {
					showToast("Camera settings failed.");
 				}
 			}
 		};
 
 		mShutterCallback = new Camera.ShutterCallback() {
 
 			public void onShutter() {

 			}
 		};
 
 		mPictureCallbackRaw = new Camera.PictureCallback() {
 
 			public void onPictureTaken(byte[] data, Camera c) {
 
 			}
 		};
 
 		mAutoFocusCallback = new Camera.AutoFocusCallback() {
 
 			public void onAutoFocus(boolean success, Camera camera) {
 				if (bAutoFocusBeep == true) {
 					// mSoundPool.play(mSoundPoolMap.get(AUDIO_FOCUS), 0.3f,
 					// 0.3f, 1, 0, 1f);
 				}
 
 				LastTime = System.currentTimeMillis();
				camera.takePicture(null, null, mPictureCallbackJpeg);
 			}
 		};
 
 		llButtons = llUpperButtons = llPreview = llUpperBlind = llLowerBlind = null;
 		ibShutter = ibTurn = ibAutoshot = ibFlash = ibSettings = ibMode = null;
 
 		try {
 			piPackageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
 		} catch (NameNotFoundException e1) {
 			Log.e("Can't found package information...", e1.toString());
 		}
 
 		strAppVersion = piPackageInfo.versionName;
 
 		lcLanguage = getResources().getConfiguration().locale;
 		lsSTRINGs = new LocalString(lcLanguage.toString());
 
 		mPreview = new Preview(this);
 		flPreview = (FrameLayout) findViewById(R.id.flPreview);
 		flPreview.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, displayHeight));
 		flPreview.addView(mPreview, new LinearLayout.LayoutParams(displayWidth, displayHeight));
 
 		settingBlindLayout();
 		settingUpperButtons();
 		settingLowerButtons();
 
 		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/Pictures/HarrisCam");
 		dir.mkdirs();
 		strFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/HarrisCam/HarrisCam_";
 
 		try {
 			FileInputStream fi = openFileInput("harrissettings");
 			fi.close();
 		} catch (FileNotFoundException e) {
 			saveSettings();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		loadSettings();
 
 		ibSettings.setOnTouchListener(new View.OnTouchListener() {
 
 			public boolean onTouch(View v, MotionEvent event) {
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 					v.setBackgroundResource(R.drawable.settings_push);
 
 					break;
 				case MotionEvent.ACTION_UP:
 					v.setBackgroundResource(R.drawable.settings);
 					getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 					clearScreen();
 
 					openSettingsPreference();
 
 					break;
 				}
 
 				return false;
 			}
 		});
 
 		ibShutter.setOnTouchListener(new View.OnTouchListener() {
 
 			public boolean onTouch(View v, MotionEvent event) {
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 					v.setBackgroundResource(R.drawable.shutter_push);
 
 					break;
 				case MotionEvent.ACTION_UP:
 					if (bAutoCapture == true) {
 						v.setBackgroundResource(R.drawable.shutter_3);
 					}
 
 					if (mPreview.mCamera != null) {
 						mPreview.mCamera.autoFocus(mAutoFocusCallback);
 					}
 
 					break;
 				}
 
 				return false;
 			}
 		});
 
 		ibTurn.setOnTouchListener(new View.OnTouchListener() {
 
 			public boolean onTouch(View v, MotionEvent event) {
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 					v.setBackgroundResource(R.drawable.turnover1_push);
 
 					break;
 				case MotionEvent.ACTION_UP:
 					v.setBackgroundResource(R.drawable.turnover1);
 
 					if (bFrontCam == false) {
 						openFrontCamera();
 
 					} else {
 						openFrontCamera();
 					}
 
 					break;
 				}
 
 				return false;
 			}
 		});
 
 		mPreview.setOnTouchListener(new View.OnTouchListener() {
 
 			public boolean onTouch(View v, MotionEvent event) {
 				try {
 					mPreview.mCamera.autoFocus(null);
 				} catch (Exception e) {
 
 				}
 
 				return false;
 			}
 		});
 
 		ibAutoshot.setOnTouchListener(new View.OnTouchListener() {
 
 			public boolean onTouch(View v, MotionEvent event) {
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 					v.setBackgroundResource(R.drawable.shot_push);
 
 					break;
 				case MotionEvent.ACTION_UP:
 					if (bAutoCapture == true && nAutoInterval == 2000) {
 
 						bAutoCapture = false;
 						v.setBackgroundResource(R.drawable.manual);
 						ibShutter.setBackgroundResource(R.drawable.shutter_3);
 						nAutoInterval = 0;
 					} else {
 						switch (nAutoInterval) {
 						case 0:
 							bAutoCapture = true;
 
 							deleteAllBitmap();
 
 							nAutoInterval = 500;
 							v.setBackgroundResource(R.drawable.auto05);
 							ibShutter.setBackgroundResource(R.drawable.shutter_auto);
 
 							break;
 						case 500:
 							nAutoInterval = 1000;
 							v.setBackgroundResource(R.drawable.auto10);
 
 							break;
 						case 1000:
 							nAutoInterval = 1500;
 							v.setBackgroundResource(R.drawable.auto15);
 
 							break;
 						case 1500:
 							nAutoInterval = 2000;
 							v.setBackgroundResource(R.drawable.auto20);
 
 							break;
 						}
 					}
 
 					break;
 				}
 
 				return false;
 			}
 		});
 
 		ibFlash.setOnTouchListener(new View.OnTouchListener() {
 
 			public boolean onTouch(View v, MotionEvent event) {
 				Camera.Parameters parameter = mPreview.mCamera.getParameters();
 
 				if (parameter.getFlashMode() != null) {
 					switch (event.getAction()) {
 					case MotionEvent.ACTION_DOWN:
 
 						break;
 					case MotionEvent.ACTION_UP:
 
 						if (bFlash == FLASH_OFF) {
 							bFlash = FLASH_AUTO;
 							v.setBackgroundResource(R.drawable.flash_auto);
 							parameter.setFlashMode(Parameters.FLASH_MODE_AUTO);
 						} else if (bFlash == FLASH_AUTO) {
 							bFlash = FLASH_ON;
 							v.setBackgroundResource(R.drawable.flash_on);
 							parameter.setFlashMode(Parameters.FLASH_MODE_ON);
 						} else {
 							bFlash = FLASH_OFF;
 							v.setBackgroundResource(R.drawable.flash_off);
 							parameter.setFlashMode(Parameters.FLASH_MODE_OFF);
 						}
 
 						try {
 							mPreview.mCamera.setParameters(parameter);
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 
 						break;
 					}
 				}
 
 				return false;
 			}
 		});
 
 		ibMode.setOnTouchListener(new View.OnTouchListener() {
 
 			public boolean onTouch(View v, MotionEvent event) {
 
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 
 					break;
 				case MotionEvent.ACTION_UP:
 					if (bMultiMode == true) {
 						bMultiMode = false;
 						v.setBackgroundResource(R.drawable.mode_single);
 						ibAutoshot.setVisibility(View.INVISIBLE);
 						ibShutter.setBackgroundResource(R.drawable.shutter_auto);
 					} else {
 						bMultiMode = true;
 						v.setBackgroundResource(R.drawable.mode_multi);
 						ibAutoshot.setVisibility(View.VISIBLE);
 						ibShutter.setBackgroundResource(R.drawable.shutter_auto);
 					}
 
 					break;
 				}
 
 				return false;
 			}
 		});
 
 		if (bStarted == false) {
 			bStarted = true;
 
 			isLatestVersion();
 		}
 	}
 
 	class ConnectionThread extends Thread {
 		String mAddr;
 		String mResult;
 
 		ConnectionThread(String addr) {
 			mAddr = addr;
 			mResult = "";
 		}
 
 		public void run() {
 
 			try {
 				URL url = new URL(strVersionAddress);
 				connVersionCheck = (HttpURLConnection) url.openConnection();
 				if (connVersionCheck != null) {
 					connVersionCheck.setConnectTimeout(10000);
 					connVersionCheck.setUseCaches(false);
 
 					if (connVersionCheck.getResponseCode() == HttpURLConnection.HTTP_OK) {
 						BufferedReader br = new BufferedReader(new InputStreamReader(connVersionCheck.getInputStream()));
 						Boolean bVersion = false;
 
 						String strTemp = null;
 						do {
 							strTemp = br.readLine();
 
 							if (bVersion) {
 								strLatestVersion = strTemp;
 
 								break;
 							}
 
 							if (strTemp.equals("harris:")) {
 								bVersion = true;
 							}
 						} while (strTemp != null);
 
 						br.close();
 					}
 					connVersionCheck.disconnect();
 				}
 			}
 
 			catch (Exception ex) {
 				Log.e("Failed get a latest version.", ex.toString());
 			}
 
 			mFindVersion.sendEmptyMessage(0);
 		}
 	}
 
 	Handler mFindVersion = new Handler() {
 
 		public void handleMessage(Message msg) {
 			if (strAppVersion.equals(strLatestVersion) == false) {
 				if (strAppVersion != null && strLatestVersion != null) {
 					float fMyVersion = 0f;
 					float fServerVersion = 0f;
 
 					try {
 						fMyVersion = Float.parseFloat(strAppVersion);
 						fServerVersion = Float.parseFloat(strLatestVersion);
 					} catch (Exception e) {
 						Log.e("Failed parseFloat in Main...", e.toString());
 					}
 
 					if (fServerVersion > fMyVersion) { // This is not latest
 														// version
 						bLatestVersion = false;
 
 						if (bAutoUpdate == true) {
 							showDialog(DIALOG_VERSION);
 						}
 					} else { // Test version
 						bLatestVersion = true;
 					}
 				} else {
 					bLatestVersion = false;
 				}
 			} else { // This is latest version
 				bLatestVersion = true;
 			}
 		}
 	};
 
 	private void isLatestVersion() {
 		ConnectionThread mThread;
 
 		mThread = new ConnectionThread(strVersionAddress);
 		mThread.start();
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DIALOG_VERSION:
 			return new AlertDialog.Builder(this).setTitle(lsSTRINGs.mUpdating).setMessage(lsSTRINGs.mUpdateAsking)
 					.setCancelable(true).setPositiveButton(lsSTRINGs.aYes, new DialogInterface.OnClickListener() {
 
 						public void onClick(DialogInterface dialog, int whichButton) {
 							Intent intent = new Intent(Intent.ACTION_VIEW, Uri
 									.parse("market://details?id=com.main.harriscam"));
 
 							startActivity(intent);
 							bStarted = false;
 
 							dialog.dismiss();
 						}
 
 					}).setNegativeButton(lsSTRINGs.aNo, new DialogInterface.OnClickListener() {
 
 						public void onClick(DialogInterface dialog, int whichButton) {
 							bAutoUpdate = false;
 
 							saveSettings();
 
 							dialog.cancel();
 						}
 
 					}).create();
 		}
 
 		return super.onCreateDialog(id);
 	}
 
 	private void settingUpperButtons() {
 		llUpperButtons = new LinearLayout(this);
 		llUpperButtons.setId(ID_UPPERLAYOUT);
 		llUpperButtons.setBackgroundResource(R.drawable.background);
 		llUpperButtons.setOrientation(LinearLayout.HORIZONTAL);
 		llUpperButtons.setGravity(Gravity.LEFT);
 		llUpperButtons.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, displayWidth / 6));
 
 		ibFlash = new ImageButton(this);
 		ibFlash.setId(ID_FLASH);
 		ibFlash.setBackgroundResource(R.drawable.flash_off);
 		ibFlash.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 4, displayWidth / 8));
 
 		ibAutoshot = new ImageButton(this);
 		ibAutoshot.setId(ID_AUTOSHOT);
 		ibAutoshot.setBackgroundResource(R.drawable.auto05);
 		ibAutoshot.setLayoutParams(new LinearLayout.LayoutParams((int) (displayWidth / 3.5), displayWidth / 7));
 
 		ibMode = new ImageButton(this);
 		ibMode.setId(ID_MODE);
 		ibMode.setBackgroundResource(R.drawable.mode_multi);
 		ibMode.setLayoutParams(new LinearLayout.LayoutParams((int) (displayWidth / 4), displayWidth / 8));
 
 		flPreview.addView(llUpperButtons);
 
 		LinearLayout llLeft = new LinearLayout(this);
 		llLeft.setOrientation(LinearLayout.HORIZONTAL);
 		llLeft.setGravity(Gravity.CENTER);
 		llLeft.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 3, displayWidth / 6));
 		llLeft.addView(ibFlash);
 
 		LinearLayout llMid = new LinearLayout(this);
 		llMid.setOrientation(LinearLayout.HORIZONTAL);
 		llMid.setGravity(Gravity.CENTER);
 		llMid.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 3, displayWidth / 6));
 		llMid.addView(ibAutoshot);
 
 		LinearLayout llRight = new LinearLayout(this);
 		llRight.setOrientation(LinearLayout.HORIZONTAL);
 		llRight.setGravity(Gravity.CENTER);
 		llRight.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 3, displayWidth / 6));
 		llRight.addView(ibMode);
 
 		llUpperButtons.addView(llLeft);
 		llUpperButtons.addView(llMid);
 		llUpperButtons.addView(llRight);
 	}
 
 	private void settingLowerButtons() {
 		llButtons = new LinearLayout(this);
 		llButtons.setId(ID_LOWERLAYOUT);
 		llButtons.setBackgroundResource(R.drawable.background);
 		llButtons.setOrientation(LinearLayout.HORIZONTAL);
 		llButtons.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, displayWidth / 6));
 
 		ibSettings = new ImageButton(this);
 		ibSettings.setId(ID_SETTINGS);
 		ibSettings.setBackgroundResource(R.drawable.settings);
 		ibSettings.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 7, displayWidth / 7));
 
 		ibShutter = new ImageButton(this);
 		ibShutter.setId(ID_SHUTTER);
 		ibShutter.setBackgroundResource(R.drawable.shutter_auto);
 		ibShutter.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 6, displayWidth / 6));
 
 		ibTurn = new ImageButton(this);
 		ibTurn.setId(ID_TURNOVER);
 		ibTurn.setBackgroundResource(R.drawable.turnover1);
 		ibTurn.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 7, displayWidth / 7));
 
 		flPreview.addView(llButtons);
 		FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) llButtons.getLayoutParams();
 		flp.gravity = Gravity.TOP;
 		flp.setMargins(0, displayHeight - displayWidth / 6, 0, 0);
 		llButtons.setLayoutParams(flp);
 
 		LinearLayout llLeft = new LinearLayout(this);
 		llLeft.setOrientation(LinearLayout.HORIZONTAL);
 		llLeft.setGravity(Gravity.CENTER);
 		llLeft.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 3, displayWidth / 6));
 		llLeft.addView(ibSettings);
 
 		LinearLayout llMid = new LinearLayout(this);
 		llMid.setOrientation(LinearLayout.HORIZONTAL);
 		llMid.setGravity(Gravity.CENTER);
 		llMid.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 3, displayWidth / 6));
 		llMid.addView(ibShutter);
 
 		LinearLayout llRight = new LinearLayout(this);
 		llRight.setOrientation(LinearLayout.HORIZONTAL);
 		llRight.setGravity(Gravity.CENTER);
 		llRight.setLayoutParams(new LinearLayout.LayoutParams(displayWidth / 3, displayWidth / 6));
 		llRight.addView(ibTurn);
 
 		llButtons.addView(llLeft);
 		llButtons.addView(llMid);
 		llButtons.addView(llRight);
 	}
 
 	private void settingBlindLayout() {
 		int nBlindHeight = displayHeight - 2 * (displayWidth / 5);
 
 		if (bScaledSquare == true) { // square scale
 			nBlindHeight = (nBlindHeight - displayWidth) / 2;
 		} else { // 3:4 scale
 			nBlindHeight = (nBlindHeight - (4 * displayWidth) / 3) / 2;
 		}
 
 		nCameraOffset = displayWidth / 5 + nBlindHeight;
 
 		llUpperBlind = new LinearLayout(this);
 		llUpperBlind.setId(ID_UPPERBLIND);
 		llUpperBlind.setBackgroundColor(Color.argb(255, 0, 0, 0));
 		llUpperBlind.setOrientation(LinearLayout.HORIZONTAL);
 		llUpperBlind.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, nCameraOffset));
 
 		llLowerBlind = new LinearLayout(this);
 		llLowerBlind.setId(ID_LOWERBLIND);
 		llLowerBlind.setBackgroundColor(Color.argb(255, 0, 0, 0));
 		llLowerBlind.setOrientation(LinearLayout.HORIZONTAL);
 		llLowerBlind.setLayoutParams(new LinearLayout.LayoutParams(displayWidth, nCameraOffset));
 
 		flPreview.addView(llUpperBlind);
 		flPreview.addView(llLowerBlind);
 		FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) llLowerBlind.getLayoutParams();
 		flp.gravity = Gravity.TOP;
 		flp.setMargins(0, displayHeight - nCameraOffset, 0, 0);
 		llLowerBlind.setLayoutParams(flp);
 
 	}
 
 	private void clearScreen() {
 		llUpperButtons.setVisibility(View.INVISIBLE);
 		llButtons.setVisibility(View.INVISIBLE);
 		llUpperBlind.setVisibility(View.INVISIBLE);
 		llLowerBlind.setVisibility(View.INVISIBLE);
 		flPreview.setVisibility(View.INVISIBLE);
 	}
 
 	private void drawScreen() {
 		llUpperButtons.setVisibility(View.VISIBLE);
 		llButtons.setVisibility(View.VISIBLE);
 		llUpperBlind.setVisibility(View.VISIBLE);
 		llLowerBlind.setVisibility(View.VISIBLE);
 		flPreview.setVisibility(View.VISIBLE);
 	}
 
 	private void openSettingsPreference() {
 		if (iSettingsActivity == null) {
 			iSettingsActivity = new Intent(this, SettingsActivity.class);
 		}
 
 		startActivityForResult(iSettingsActivity, ACTIVITY_SETTINGS);
 	}
 
 	private void openCropActivity() {
 		if (iCropActivity == null) {
 			iCropActivity = new Intent(this, CropActivity.class);
 		}
 
 		startActivityForResult(iCropActivity, ACTIVITY_CROP);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case ACTIVITY_CROP:
 			switch (resultCode) {
 			case RESULT_CANCELED:
 				showToast(lsSTRINGs.mSaveCancel);
 
 				MainActivity.deleteAllBitmap();
 
 				break;
 			case RESULT_OK:
 				showToast(lsSTRINGs.mSaveSuccess);
 
 				MainActivity.deleteAllBitmap();
 
 				break;
 			case RESULT_FAILED:
 				showToast(lsSTRINGs.mSaveFailed);
 
 				break;
 			}
 
 			break;
 
 		case ACTIVITY_SETTINGS:
 			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 			drawScreen();
 			saveSettings();
 
 			break;
 		}
 
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	protected void onResume() {
 
 		super.onResume();
 	}
 
 	public static void deleteAllBitmap() {
 		for (int i = 0; i < 3; i++) {
 			if (bitOpened[i] != null) {
 				bitOpened[i].recycle();
 				bitOpened[i] = null;
 			}
 		}
 
 		if (bitResultImage != null) {
 			bitResultImage.recycle();
 			bitResultImage = null;
 		}
 
 		System.gc();
 
 		nTimes = 0;
 	}
 
 	private void readyOpenImage() {
 		try {
 			nTimes = 0;
 
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
 			strOriFilename = strFilePath + sdf.format(System.currentTimeMillis()) + ".jpg";
 
 			for (int i = 0; i < 3; i++) {
 				Bitmap bitTemp = BitmapFactory.decodeByteArray(byOriImage[i], 0, byOriImage[i].length);
 
 				int nWidth = bitTemp.getWidth();
 				int nHeight = bitTemp.getHeight();
 				int nTargetWidth = 0;
 
 				Bitmap bitTemp2 = null;
 
 				if (nWidth > nHeight) {
 					nTargetWidth = nHeight;
 					nImageOffset = (nCameraOffset * nWidth) / displayHeight;
 					if (bScaledSquare == true) {
 						bitTemp2 = Bitmap.createBitmap(bitTemp, nImageOffset, 0, nHeight, nHeight);
 					} else {
 						bitTemp2 = Bitmap.createBitmap(bitTemp, nImageOffset, 0, nWidth - 2 * nImageOffset, nHeight);
 					}
 				} else if (nWidth < nHeight) {
 					nTargetWidth = nWidth;
 					nImageOffset = (nCameraOffset * nHeight) / displayWidth;
 					if (bScaledSquare == true) {
 						bitTemp2 = Bitmap.createBitmap(bitTemp, 0, nImageOffset, nWidth, nWidth);
 					} else {
 						bitTemp2 = Bitmap.createBitmap(bitTemp, 0, nImageOffset, nWidth, nHeight - 2 * nImageOffset);
 					}
 				} else {
 					nTargetWidth = nWidth;
 					nImageOffset = (nCameraOffset * nHeight) / displayWidth;
 					bitTemp2 = Bitmap.createBitmap(bitTemp, 0, 0, nWidth, nWidth);
 				}
 
 				if (bitTemp != null) {
 					bitTemp.recycle();
 					bitTemp = null;
 				}
 
 				nWidth = bitTemp2.getWidth();
 				nHeight = bitTemp2.getHeight();
 
 				Matrix matrix = new Matrix();
 				float fScaledRate = (float) MainActivity.nSaveResolution / nTargetWidth;
 				matrix.postScale(fScaledRate, fScaledRate);
 				if (bFrontCam == false) {
 					matrix.postRotate(90);
 				} else {
 					matrix.postRotate(270);
 				}
 
 				bitOpened[i] = Bitmap.createBitmap(bitTemp2, 0, 0, nWidth, nHeight, matrix, true);
 
 				if (bitTemp2 != null) {
 					bitTemp2.recycle();
 					bitTemp2 = null;
 				}
 
 				if (byOriImage[i] != null) {
 					byOriImage[i] = null;
 				}
 
 				System.gc();
 
 				if (bMultiMode == false) {
 					break;
 				}
 			}
 
 			threadApplyHarris = new ApplyHarrisShutter();
 			threadApplyHarris.start();
 		} catch (OutOfMemoryError e) {
 			showToast("Memory space is full... Try again.");
 
 			MainActivity.deleteAllBitmap();
 		}
 	}
 
 	private class ApplyHarrisShutter extends Thread {
 
 		public ApplyHarrisShutter() {
 
 		}
 
 		public void run() {
 			try {
 
 				// Old Screen Mode
 				if (bMultiMode == false) {
 					Bitmap bitTempImage = Bitmap.createBitmap(MainActivity.bitOpened[0]);
 					bitResultImage = Bitmap.createBitmap(MainActivity.bitOpened[0]);
 
 					applyScreen(bitResultImage, MainActivity.bitOpened[0], bitTempImage);
 
 					if (bitTempImage != null) {
 						bitTempImage.recycle();
 						bitTempImage = null;
 					}
 
 					System.gc();
 				} else { // Harris Shutter Mode
 					bitResultImage = Bitmap.createBitmap(MainActivity.bitOpened[0]);
 
 					applyHarris(bitResultImage, MainActivity.bitOpened[1], MainActivity.bitOpened[2]);
 				}
 			} catch (OutOfMemoryError e) {
 				showToast("Memory space is full... Try again.");
 
 				MainActivity.deleteAllBitmap();
 			}
 
 			openCropActivity();
 		}
 	}
 
 	public void openFrontCamera() {
 		int cameraCount = 0;
 		int camNum = 0;
 		Camera.CameraInfo ciCamera = new Camera.CameraInfo();
 		cameraCount = Camera.getNumberOfCameras();
 
 		for (int i = 0; i < cameraCount; i++) {
 			Camera.getCameraInfo(i, ciCamera);
 
 			// Toggle camera.
 			if (bFrontCam == false) {
 				if (ciCamera.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
 					camNum = i;
 
 					bFrontCam = true;
 
 					break;
 				}
 			} else {
 				if (ciCamera.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
 					camNum = i;
 
 					bFrontCam = false;
 
 					break;
 				}
 			}
 		}
 
 		try {
 			if (mPreview.mCamera != null) {
 				mPreview.mCamera.release();
 			}
 			mPreview.mCamera = null;
 
 			mPreview.mCamera = Camera.open(camNum);
 			mPreview.mCamera.setDisplayOrientation(90);
 
 			try {
 				mPreview.mCamera.setPreviewDisplay(mPreview.mHolder);
 			} catch (IOException exception) {
 				mPreview.mCamera.release();
 				mPreview.mCamera = null;
 			}
 
 			mPreview.mCamera.startPreview();
 		} catch (RuntimeException e) {
 			showToast(e.toString());
 			Log.e("Failed open a front cam...", e.toString());
 		}
 	}
 
 	public void adjustViewSize(View view, int nWidth, int nHeight) {
 		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
 		lp.width = nWidth;
 		lp.height = nHeight;
 		view.setLayoutParams(lp);
 	}
 
 	class Preview extends SurfaceView implements SurfaceHolder.Callback {
 		SurfaceHolder mHolder;
 		Camera mCamera;
 
 		Preview(Context context) {
 			super(context);
 
 			// SurfaceHolder.Callback. For surface create, destroy.
 			mHolder = getHolder();
 			mHolder.addCallback(this);
 			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 		}
 
 		public void surfaceCreated(SurfaceHolder holder) {
 			try {
 				// Surface has been created, set a position to drawing preview.
 				mCamera = Camera.open(0);
 				mCamera.setDisplayOrientation(90);
 
 				mCamera.setPreviewDisplay(holder);
 
 				Camera.Parameters parameters = mCamera.getParameters();
 
 				List<Size> sizes = parameters.getSupportedPictureSizes();
 				Size optimalSize;
 				optimalSize = getOptimalPreviewSize(sizes, SAVE_HIGH, SAVE_HIGH);
 				parameters.setPictureSize(optimalSize.width, optimalSize.height);
 				mCamera.setParameters(parameters);
 			} catch (IOException exception) {
 				mCamera.release();
 				mCamera = null;
 
 				showToast("Camera settings failed.");
 			}
 		}
 
 		public void surfaceDestroyed(SurfaceHolder holder) {
 			// If surface has been destroyed, release a camera resource.
 			if (mCamera != null) {
 				mCamera.stopPreview();
 				mCamera.release();
 				mCamera = null;
 			}
 		}
 
 		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
 			// I know preview size!!! So I'll change size.
 			if (mCamera != null) {
 				Camera.Parameters parameters = mCamera.getParameters();
 
 				List<Size> sizes = parameters.getSupportedPreviewSizes();
 				Size optimalSize = getOptimalPreviewSize(sizes, w, h);
 				parameters.setPreviewSize(optimalSize.width, optimalSize.height);
 
 				if (bFlash == FLASH_OFF) {
 					parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
 				} else if (bFlash == FLASH_AUTO) {
 					parameters.setFlashMode(Parameters.FLASH_MODE_AUTO);
 				} else {
 					parameters.setFlashMode(Parameters.FLASH_MODE_ON);
 				}
 
 				// parameters.setPreviewSize(w, h);
 
 				try {
 					mCamera.setParameters(parameters);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				mCamera.startPreview();
 
 				// If before camera setting is front camera.
 				if (bFrontCam == true) {
 					bFrontCam = false;
 					openFrontCamera();
 				}
 			}
 		}
 
 	}
 
 	// Find a optimal preview size of device
 	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
 		final double ASPECT_TOLERANCE = 0.1;
 		double targetRatio = (double) w / h;
 		if (sizes == null)
 			return null;
 
 		Size optimalSize = null;
 		double minDiff = Double.MAX_VALUE;
 		int targetHeight = h;
 
 		for (Size size : sizes) {
 			double ratio = (double) size.width / size.height;
 
 			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
 				continue;
 			if (Math.abs(size.height - targetHeight) < minDiff) {
 				optimalSize = size;
 				minDiff = Math.abs(size.height - targetHeight);
 			}
 		}
 
 		if (optimalSize == null) {
 			minDiff = Double.MAX_VALUE;
 
 			for (Size size : sizes) {
 				if (Math.abs(size.height - targetHeight) < minDiff) {
 					optimalSize = size;
 					minDiff = Math.abs(size.height - targetHeight);
 				}
 			}
 		}
 
 		return optimalSize;
 	}
 
 	public void showToast(String s) {
 		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
 	}
 
 	public void showToast(String s, int LENGTH) {
 		Toast.makeText(this, s, LENGTH).show();
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			if (bEnd == false) {
 				showToast(lsSTRINGs.mFinish);
 
 				bEnd = true;
 				mHandler.sendEmptyMessageDelayed(0, 2000);
 
 				return false;
 			} else {
 				finish();
 			}
 		} else if (keyCode == KeyEvent.KEYCODE_HOME) {
 			finish();
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public void finish() {
 		if (mPreview.mCamera != null) {
 			mPreview.mCamera.stopPreview();
 			mPreview.mCamera.release();
 			mPreview.mCamera = null;
 		}
 
 		deleteAllBitmap();
 
 		android.os.Process.killProcess(android.os.Process.myPid());
 
 		super.finish();
 	}
 
 	@Override
 	protected void onDestroy() {
 		if (mPreview.mCamera != null) {
 			mPreview.mCamera.stopPreview();
 			mPreview.mCamera.release();
 			mPreview.mCamera = null;
 		}
 
 		deleteAllBitmap();
 
 		super.onDestroy();
 	}
 
 	private void loadSettings() {
 		String strAutoSave = null;
 		String strResolution = null;
 		String strScaled = null;
 		String strAutoUpdate = null;
 
 		try {
 			FileInputStream fi = openFileInput("harrissettings");
 			int nFileSize = fi.available();
 			byte[] byData = new byte[nFileSize];
 
 			if (fi.read(byData) != -1) {
 				strAutoSave = new String(byData, 0, 1);
 				if (strAutoSave.compareTo("o") == 0) {
 					bOriginalAutoSave = true;
 				} else {
 					bOriginalAutoSave = false;
 				}
 
 				strResolution = new String(byData, 1, 1);
 				if (strResolution.compareTo("l") == 0) {
 					nSaveResolution = SAVE_LOW;
 				} else if (strResolution.compareTo("h") == 0) {
 					nSaveResolution = SAVE_HIGH;
 				} else {
 					nSaveResolution = SAVE_MIDDLE;
 				}
 
 				strScaled = new String(byData, 2, 1);
 				if (strScaled.compareTo("o") == 0) {
 					bScaledSquare = true;
 				} else {
 					bScaledSquare = false;
 				}
 
 				strAutoUpdate = new String(byData, 3, 1);
 				if (strAutoUpdate.compareTo("o") == 0) {
 					bAutoUpdate = true;
 				} else {
 					bAutoUpdate = false;
 				}
 			}
 
 			fi.close();
 		} catch (Exception e) {
 			showToast("Settings load failed.");
 		}
 	}
 
 	private void saveSettings() {
 		String strAutoSave = null;
 		String strResolution = null;
 		String strScaled = null;
 		String strAutoUpdate = null;
 
 		if (bOriginalAutoSave == true) {
 			strAutoSave = "o";
 		} else {
 			strAutoSave = "x";
 		}
 
 		switch (nSaveResolution) {
 		case SAVE_LOW:
 			strResolution = "l";
 
 			break;
 		case SAVE_MIDDLE:
 			strResolution = "m";
 
 			break;
 		case SAVE_HIGH:
 			strResolution = "h";
 
 			break;
 		}
 
 		if (bScaledSquare == true) {
 			strScaled = "o";
 		} else {
 			strScaled = "x";
 		}
 
 		if (bAutoUpdate == true) {
 			strAutoUpdate = "o";
 		} else {
 			strAutoUpdate = "x";
 		}
 
 		try {
 			FileOutputStream fo = openFileOutput("harrissettings", Context.MODE_WORLD_READABLE);
 			fo.write(strAutoSave.getBytes());
 			fo.write(strResolution.getBytes());
 			fo.write(strScaled.getBytes());
 			fo.write(strAutoUpdate.getBytes());
 			fo.close();
 		} catch (Exception e) {
 			showToast("Settings save failed.");
 		}
 	}
 
 }
