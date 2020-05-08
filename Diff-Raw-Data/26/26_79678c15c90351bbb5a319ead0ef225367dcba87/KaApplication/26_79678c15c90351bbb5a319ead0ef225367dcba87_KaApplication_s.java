 package com.pengjun.ka.android.activity;
 
 import android.app.Application;
 import android.content.Intent;
 
 import com.j256.ormlite.android.AndroidConnectionSource;
 import com.pengjun.ka.android.service.ReportNotificationService;
 import com.pengjun.ka.db.DbHelper;
 import com.pengjun.ka.db.dao.ArTypeDao;
 import com.pengjun.ka.utils.KaConstants;
 import com.pengjun.ka.utils.ResourceUtils;
 import com.pengjun.ka.utils.StringUtils;
 import com.pengjun.ka.utils.TimeUtils;
 
 public final class KaApplication extends Application {
 
 	private static KaApplication instance = null;
 
 	public static KaApplication instance() {
 		return instance;
 	}
 
 	public static AndroidConnectionSource androidConnectionSource;
 
 	public static AndroidConnectionSource getAndroidConnectionSource() {
 		return androidConnectionSource;
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		instance = this;
 
 		ResourceUtils.initLogger(true, true);
 
 		// load IKSegmenter files, about ten seconds
 		new Thread() {
 			public void run() {
 				StringUtils.initSegmentationTool();
 			}
 		}.start();
 
 		androidConnectionSource = DbHelper.getAndroidConnectionSource(this);
 
 		if (ResourceUtils.isServiceRunning(this, ReportNotificationService.class.getName()) == false) {
 			Intent newIntent = new Intent(this, ReportNotificationService.class);
 			this.startService(newIntent);
 		}
 
 		// if first start of the app
 		if (ResourceUtils.getSharedPreferencesString(this, KaConstants.SP_KEY_FIRST_START_APP).equals(
 				StringUtils.STRING_NULL_VALUE)) {
 
 			ResourceUtils.putSharedPreferencesString(this, KaConstants.SP_KEY_FIRST_START_APP,
 					KaConstants.SP_VALUE_FIRST_START_APP);
 
 			// mark the week and month report has sent at first start app
 			String curMonthYear = TimeUtils.getCurMonthYearStr();
 			ResourceUtils.putSharedPreferencesString(this, curMonthYear, curMonthYear);
 
 			String curWeekYear = TimeUtils.getCurWeekYearStr();
 			ResourceUtils.putSharedPreferencesString(this, curWeekYear, curWeekYear);
 
 			ArTypeDao.initTable();
 
 		}
 	}
 
 	@Override
 	public void onTerminate() {
 		instance = null;
 		super.onTerminate();
 	}
 
 }
