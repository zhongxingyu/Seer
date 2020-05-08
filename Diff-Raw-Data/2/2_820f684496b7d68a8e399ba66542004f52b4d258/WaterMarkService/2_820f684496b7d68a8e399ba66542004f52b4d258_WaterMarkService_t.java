 package com.zgy.ringforu.service;
 
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningTaskInfo;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.PixelFormat;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.util.DisplayMetrics;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import com.zgy.ringforu.LogRingForu;
 import com.zgy.ringforu.MainCanstants;
 import com.zgy.ringforu.R;
 import com.zgy.ringforu.RingForU;
 import com.zgy.ringforu.activity.ToolsListActivity;
 import com.zgy.ringforu.config.MainConfig;
 import com.zgy.ringforu.util.BitmapUtil;
 import com.zgy.ringforu.util.WaterMarkUtil;
 import com.zgy.ringforu.view.MyToast;
 
 public class WaterMarkService extends Service {
 
 	private static final String TAG = "WaterMarkService";
 
 	private WindowManager wm = null;
 	private WindowManager.LayoutParams wmParams = null;
 	private View view;
 
 	public static boolean show = true;
 
 	private static final int MSG_NOOP = 100;
 	private static final int MSG_SHOW = 101;
 	private static final int MSG_HIDE = 102;
 	private static final int TIME_DELAY = 800;
 	private static final int TIME_DELAY_SHOW_HIDE = 10;
 
 	private ActivityManager mActivityManager;
 	private ImageView mImageView;
 	private int mAlpah;
 	private int mTempAlpha;
 	private Handler mHandler;
 
 	private String mPackageNameTemp = "";
 
 	@Override
 	public void onStart(Intent intent, int startId) {
 		// TODO Auto-generated method stub
 		super.onStart(intent, startId);
 
 		LogRingForu.e(TAG, "WaterMarkService is start!");
 		if (show) {
 
 			if (WaterMarkUtil.isWaterMarkSeted()) {
 				if (wm == null && view == null) {
 					try {
 						createView();
 						Intent i = new Intent(ToolsListActivity.ACTION_WATERMARK_ON);
 						sendBroadcast(i);
 					} catch (Exception e) {
 						MyToast.makeText(WaterMarkService.this, R.string.watermark_show_fail, Toast.LENGTH_SHORT, true).show();
 					}
 				}
 
 			} else {
 				stopSelf();
 			}
 
 		} else {
 			stopSelf();
 		}
 
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 	}
 
 	private void createView() {
 		// LogRingForu.v(TAG, "set View ");
 
 		mPackageNameTemp = "";
 		mTempAlpha = 0;
 
 		mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
 
 		// LayoutParams(ȫֱز
 		view = LayoutInflater.from(this).inflate(R.layout.watermark_floating, null);
 		wm = (WindowManager) getApplicationContext().getSystemService("window");
 		wmParams = RingForU.getMywmParams();
 
 		wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;// ṩûӦóϷ״̬
 		// wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;// ϵͳʾǳӦó򴰿֮
 		// wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;// ϵͳڲʾʾ֮
 		// wmParams.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;// 绰ȣʱʾ˴ڲܻ뽹㣬Ӱ
 		// wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;// 绰ȣʱʾ˴ڲܻ뽹㣬Ӱ
 
 		wmParams.flags =
 
 		WindowManager.LayoutParams.FLAG_FULLSCREEN
 
 		| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
 
 		| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN // windowռֻĻκα߽磨border
 
 		// | WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES// κΰ¼
 
 		| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE // windowܻý㣬ûͲwindowͰ¼ť¼
 
 		| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE // øwindowܴ¼
 		// | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH //øwindowܴ¼
 
 		// | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
 		// //ʹڸwindowڿɻý£ȻѸwindow֮κevent͵window֮window.
 
 		;
 		wmParams.gravity = Gravity.FILL;
 
 		// Rect frame = new Rect();
 		// Window w = (Window)
 		// getApplicationContext().getSystemService("window");
 		// w.getDecorView().getWindowVisibleDisplayFrame(frame);
 		// LogRingForu.v(TAG, "getWindowVisibleDisplayFrame " + frame.top);
 		// ĻϽΪԭ㣬xyʼֵ
 		// wmParams.x = 0;
 		// wmParams.y = 0;
 		// ڳ
 		// wmParams.width = WindowManager.LayoutParams.FILL_PARENT;
 		// wmParams.height = WindowManager.LayoutParams.FILL_PARENT;
 		wmParams.format = PixelFormat.RGBA_8888;// ͸
 		mImageView = (ImageView) view.findViewById(R.id.watermark_image);
 
 		mAlpah = MainConfig.getInstance().getWaterMarkAlpha();
 		// LogRingForu.v(TAG, "set alpha " + alpha);
 		mImageView.setAlpha(0);
 
 		// if (new File(WaterMarkUtil.FILEPATH_WATERMARK).exists()) {
 		// imgShow.setImageDrawable(new
 		// BitmapDrawable(Globle.FILEPATH_WATERMARK));
 		// Bitmap bitmap =
 		// BitmapFactory.decodeFile(WaterMarkUtil.FILE_WATERMARK_IMG.getAbsolutePath());
 		DisplayMetrics metric = getResources().getDisplayMetrics();
 		mImageView.setImageBitmap(BitmapUtil.readBitmapAutoSize(MainCanstants.FILE_WATERMARK_IMG.getAbsolutePath(), metric.widthPixels, metric.heightPixels).getBitMap());
 		// } else {
 		// imgShow.setImageDrawable(null);
 		// stopSelf();
 		// }
 //		wm.addView(view, wmParams);
 
 		mHandler = new ListenerHandler();
		mHandler.sendEmptyMessage(MSG_NOOP);
 		// LogRingForu.v(TAG, "imgShow h=" + imgShow.getMeasuredHeight() +
 		// "  w=" + imgShow.getMeasuredWidth());
 	}
 
 	// private void updateViewPosition() {
 	// // ¸λò
 	// wmParams.x = (int) (x - mTouchStartX);
 	// wmParams.y = (int) (y - mTouchStartY);
 	// wm.updateViewLayout(view, wmParams);
 	// }
 
 	private class ListenerHandler extends Handler {
 
 		@Override
 		public void handleMessage(Message msg) {
 
 			switch (msg.what) {
 			case MSG_NOOP:
 
 				mHandler.removeMessages(MSG_NOOP);
 
 				if (mActivityManager != null && mImageView != null) {
 					RunningTaskInfo info = mActivityManager.getRunningTasks(1).get(0);
 					// String shortClassName =
 					// info.topActivity.getShortClassName(); //
 					// 
 					String className = info.topActivity.getClassName(); //
 					// 
 					String packageName = info.topActivity.getPackageName(); // 
 					// LogRingForu.v(TAG, "top app className=" + className);
 					if (!packageName.equals(mPackageNameTemp)) {
 						// лʱſ
 						mPackageNameTemp = packageName;
 						if (RingForU.getInstance().getPackageNameHideWaterMark().contains(packageName) || MainCanstants.ACTIVITY_NAME_INSTALLAPP.equals(className)) {
 							hide();
 						} else {
 							show();
 						}
 					}
 
 				}
 
 				mHandler.sendEmptyMessageDelayed(MSG_NOOP, TIME_DELAY);
 				break;
 			case MSG_HIDE:
 				mHandler.removeMessages(MSG_HIDE);
 				if (mTempAlpha > 0) {
 					mTempAlpha -= 10;
 					mTempAlpha = mTempAlpha < 0 ? 0 : mTempAlpha;
 					mImageView.setAlpha(mTempAlpha);
 
 					mHandler.sendEmptyMessageDelayed(MSG_HIDE, TIME_DELAY_SHOW_HIDE);
 				} else {
 
 					try {
 						wm.removeView(view);
 					} catch (Exception e) {
 						// e.printStackTrace();
 					}
 				}
 
 				break;
 
 			case MSG_SHOW:
 				mHandler.removeMessages(MSG_SHOW);
 				if (mTempAlpha < mAlpah) {
 					mTempAlpha += 10;
 					mTempAlpha = mTempAlpha >= mAlpah ? mAlpah : mTempAlpha;
 					mImageView.setAlpha(mTempAlpha);
 
 					mHandler.sendEmptyMessageDelayed(MSG_SHOW, TIME_DELAY_SHOW_HIDE);
 				}
 
 				break;
 			default:
 				break;
 			}
 
 			super.handleMessage(msg);
 		}
 	}
 
 	private void show() {
 		try {
 			wm.addView(view, wmParams);
 		} catch (Exception e) {
 			// e.printStackTrace();
 		}
 
 		if (mTempAlpha < mAlpah) {
 			mTempAlpha = 0;
 			mHandler.sendEmptyMessageDelayed(MSG_SHOW, TIME_DELAY_SHOW_HIDE);
 		}
 	}
 
 	private void hide() {
 		if (mTempAlpha > 0) {
 			mTempAlpha = mAlpah;
 			mHandler.sendEmptyMessageDelayed(MSG_HIDE, TIME_DELAY_SHOW_HIDE);
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 
 		LogRingForu.e(TAG, "WaterMarkService is destroyed!");
 
 		if (wm != null && view != null) {
 			if (view.isShown())
 				wm.removeView(view);
 			wm = null;
 			view = null;
 		}
 
 		if (mHandler != null) {
 			mHandler.removeMessages(MSG_NOOP);
 		}
 	}
 }
