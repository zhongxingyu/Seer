 package com.zgy.ringforu.util;
 
 import android.content.Context;
 import android.content.Intent;
 
 import com.zgy.ringforu.LogRingForu;
 import com.zgy.ringforu.MainCanstants;
 import com.zgy.ringforu.config.MainConfig;
 import com.zgy.ringforu.service.WaterMarkService;
 
 /**
  * ˮӡ
  * 
  * @author ZGY
  * 
  */
 public class WaterMarkUtil {
 
 	private static final String TAG = "WaterMarkUtil";
 
 	// TODO getstate , check ,
 
 	public static boolean isWaterMarkShowing(Context context) {
		//&& MainUtil.isServiceStarted(context, MainCanstants.SERVICE_NAME_WATERMARK)
		if (isWaterMarkSeted() ) {
 			return true;
 		} else {
 			return false;
 		}
 
 	}
 
 	/**
 	 * check state
 	 * 
 	 * @return
 	 */
 	public static boolean isWaterMarkSeted() {
 		if (MainCanstants.FILE_WATERMARK_IMG.exists() && MainConfig.getInstance().isWaterMarkOn()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * ˮӡ
 	 * 
 	 * @Description:
 	 * @param on
 	 * @see:
 	 * @since:
 	 * @author: zhuanggy
 	 * @date:2013-7-18
 	 */
 	public static void setSwitchOnOff(boolean on) {
 		MainConfig mainConfig = MainConfig.getInstance();
 		if (on) {
 			mainConfig.setWaterMarkOnOff(true);
 
 		} else {
 			mainConfig.setWaterMarkOnOff(false);
 		}
 	}
 
 	/**
 	 * check
 	 * 
 	 * @param context
 	 */
 	public static void checkState(Context context) {
 		if (isWaterMarkSeted()) {
 			WaterMarkService.show = true;
 			ctrlWaterMarkBackService(context, true);
 			NotificationUtil.showHideWaterMarkNotify(true, context);
 		} else {
 			ctrlWaterMarkBackService(context, false);
 			NotificationUtil.showHideWaterMarkNotify(false, context);
 		}
 	}
 
 	/**
 	 * ̨񿪹
 	 */
 	public static void ctrlWaterMarkBackService(Context context, boolean open) {
 		if (open) {
 //			if (!MainUtil.isServiceStarted(context, SERVICE_NAME_WATERMARK)) {
 				Intent i = new Intent(context, WaterMarkService.class);
 				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				context.startService(i);
 
 //				LogRingForu.v(TAG, "service is not running, need to start service!");
 //			} else {
 
 //				LogRingForu.v(TAG, "service is running, no need to start service!");
 //			}
 		} else {
 			if (MainUtil.isServiceStarted(context, MainCanstants.SERVICE_NAME_WATERMARK)) {
 				Intent i = new Intent(context, WaterMarkService.class);
 				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				context.stopService(i);
 
 				LogRingForu.v(TAG, "service is running, need to stop service!");
 			} else {
 
 				LogRingForu.v(TAG, "service is not running, no need to stop service!");
 			}
 		}
 
 	}
 
 }
