 package cstdr.ningningcat.util;
 
 import java.math.BigDecimal;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 import android.util.DisplayMetrics;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import cstdr.ningningcat.NncApp;
 import cstdr.ningningcat.R;
 
 /**
  * UI相关工具
  * 
  * @author cstdingran@gmail.com
  */
 public class UIUtil {
 
 	private static final String TAG = "UIUtil";
 
 	private static final String SCREEN_BRIGHTNESS = "screen_brightness";
 
 	private static final int NIGHT_MODE_BRIGHTNESS = 30;
 
 	private static final int BRIGHT_MODE_UNDOWN = -2;
 
 	private static final int BRIGHT_MODE_NIGHT = -1;
 
 	private static final int BRIGHT_MODE_AUTO = 0;
 
 	private static final int BRIGHT_MODE_DAY = 1;
 
 	/**
 	 * 隐藏键盘
 	 * 
 	 * @param v
 	 */
 	public static void hideInputWindow(final View v) {
 		InputMethodManager imm = (InputMethodManager) v.getContext()
 				.getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
 	}
 
 	/**
 	 * 显示键盘
 	 * 
 	 * @param v
 	 */
 	public static void showInputWindow(final View v) {
 		InputMethodManager imm = (InputMethodManager) v.getContext()
 				.getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
 	}
 
 	/**
 	 * 夜间模式变换
 	 * 
 	 * @param context
 	 */
 	public static void changeBrightMode(Context context, Activity activity) {
 		SharedPreferences sp = NncApp.getInstance().getSp();
 		int brightModeNow = sp.getInt(
 				context.getString(R.string.spkey_bright_mode_now),
 				BRIGHT_MODE_UNDOWN);
 		int brightModeLast = sp.getInt(
 				context.getString(R.string.spkey_bright_mode_last),
 				BRIGHT_MODE_UNDOWN);
 		int lastBrightness = sp.getInt(
 				context.getString(R.string.spkey_last_brightness), 0);
 		if (brightModeNow == BRIGHT_MODE_UNDOWN) {
 			// 第一次获取当前亮度模式，并存入SP
 			if (isAutoBrightness(activity)) {
 				brightModeNow = BRIGHT_MODE_AUTO;
 			} else {
 				brightModeNow = BRIGHT_MODE_DAY;
 				lastBrightness = getScreenBrightness(activity);
 			}
 			brightModeLast = brightModeNow;
 		}
 		switch (brightModeNow) {
 		case BRIGHT_MODE_NIGHT: // 夜间
 			if (brightModeLast == BRIGHT_MODE_AUTO) {
 				startAutoBrightness(activity);
 			} else if (brightModeLast == BRIGHT_MODE_DAY) {
 				setScreenBrightness(activity, lastBrightness);
 				saveScreenBrightness(activity, lastBrightness);
 			}
 			break;
 		case BRIGHT_MODE_AUTO: // 自动亮度
 			stopAutoBrightness(activity);
 		case BRIGHT_MODE_DAY: // 白天
 			lastBrightness = getScreenBrightness(activity);
 			setScreenBrightness(activity, NIGHT_MODE_BRIGHTNESS);
 			saveScreenBrightness(activity, NIGHT_MODE_BRIGHTNESS);
 			break;
 		}
 		if (brightModeNow == BRIGHT_MODE_NIGHT) {
 			brightModeNow = brightModeLast;
 			brightModeLast = BRIGHT_MODE_NIGHT;
 		} else {
 			brightModeLast = brightModeNow;
 			brightModeNow = BRIGHT_MODE_NIGHT;
 		}
 		SPUtil.commitIntArrayToSP(
 				sp,
 				new String[] {
 						context.getString(R.string.spkey_bright_mode_now),
 						context.getString(R.string.spkey_bright_mode_last),
 						context.getString(R.string.spkey_last_brightness) },
 				new int[] { brightModeNow, brightModeLast, lastBrightness });
 
 	}
 
 	/**
 	 * 是否为自动亮度
 	 * 
 	 * @param activity
 	 * @return
 	 */
 	public static boolean isAutoBrightness(Activity activity) {
 		boolean isAuto = false;
 		ContentResolver cr = activity.getContentResolver();
 		try {
 			isAuto = Settings.System.getInt(cr,
 					Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
 		} catch (SettingNotFoundException e) {
 			LOG.exception(e);
 		}
 		return isAuto;
 	}
 
 	/**
 	 * 开启自动亮度
 	 * 
 	 * @param activity
 	 */
 	public static void startAutoBrightness(Activity activity) {
 		ContentResolver cr = activity.getContentResolver();
 		Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE,
 				Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
 	}
 
 	/**
 	 * 关闭自动亮度
 	 * 
 	 * @param activity
 	 */
 	public static void stopAutoBrightness(Activity activity) {
 		ContentResolver cr = activity.getContentResolver();
 		Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE,
 				Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
 	}
 
 	/**
 	 * 得到当前亮度
 	 * 
 	 * @param activity
 	 * @return
 	 */
 	public static int getScreenBrightness(Activity activity) {
 		int brightness = 0;
 		ContentResolver cr = activity.getContentResolver();
 		try {
 			brightness = Settings.System.getInt(cr,
 					Settings.System.SCREEN_BRIGHTNESS);
 		} catch (SettingNotFoundException e) {
 			LOG.exception(e);
 		}
 		return brightness;
 	}
 
 	/**
 	 * 设置亮度，若只设置此项则只在activity有效，退出后恢复
 	 * 
 	 * @param activity
 	 * @param brightness
 	 */
 	public static void setScreenBrightness(Activity activity, int brightness) {
 		WindowManager.LayoutParams params = activity.getWindow()
 				.getAttributes();
 		params.screenBrightness = Float.valueOf(brightness) / 255F;
 		activity.getWindow().setAttributes(params);
 	}
 
 	/**
 	 * 保存亮度
 	 * 
 	 * @param activity
 	 * @param brightness
 	 */
 	public static void saveScreenBrightness(Activity activity, int brightness) {
 		ContentResolver cr = activity.getContentResolver();
 		Settings.System.putInt(cr, SCREEN_BRIGHTNESS, brightness);
 		Uri uri = Settings.System.getUriFor(SCREEN_BRIGHTNESS);
 		cr.notifyChange(uri, null);
 	}
 
 	/**
 	 * 将long类型长度转换为合理的文件大小
 	 * 
 	 * @param contentLength
 	 * @return
 	 */
 	public static String changeSize(long length) {
 		String size;
 		if (length < 1024) {
 			size = length + "Byte";
 		} else if (length < 1024 * 1024) {
 			int kb = (int) (length / 1024);
 			size = kb + "KB";
 		} else {
 			double dLength = (double) length / 1024 / 1024;
 			BigDecimal b = new BigDecimal(dLength);
 			size = String.valueOf(b.setScale(2, BigDecimal.ROUND_HALF_UP))
 					+ "MB";
 		}
 		return size;
 	}
 
 	/**
 	 * 初始化UI参数，将值传入NncApp
 	 */
 	public static void initUI(Activity activity) {
 		if (NncApp.getUI_SCALE_X() == 0) {
 			DisplayMetrics metrics = new DisplayMetrics();
 			activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
 			if (LOG.DEBUG) {
 				LOG.cstdr(TAG, "metrics.widthPixels = " + metrics.widthPixels);
 				LOG.cstdr(TAG, "metrics.heightPixels = " + metrics.heightPixels);
 				LOG.cstdr(TAG, "metrics.density = " + metrics.density);
				LOG.cstdr(TAG, "(float) metrics.widthPixels / 720 = "
						+ (float) metrics.widthPixels / 720);
 			}
			NncApp.setUI_SCALE_X((float) metrics.widthPixels / 720);
 		}
 	}
 
 }
