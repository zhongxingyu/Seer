 package com.pengjun.ka.utils;
 
 import java.io.File;
 import java.io.InputStream;
 import java.lang.reflect.Field;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import android.app.ActivityManager;
 import android.content.Context;
 import android.graphics.Color;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Environment;
 import android.os.StatFs;
 
 import com.pengjun.ka.R;
 
 import de.mindpipe.android.logging.log4j.LogConfigurator;
 
 public class ResourceUtils {
 
 	public static final String IS_DEBUG = "debug";
 
 	public static int[] COLOR_ARR = new int[] { Color.BLUE, Color.MAGENTA, Color.DKGRAY, Color.CYAN,
 			Color.GREEN, Color.GRAY, Color.RED, Color.WHITE, Color.LTGRAY };
 	// res
 	public static final String RES_IMAGE_PREFIX = "type";
 
 	private static Map<String, Integer> imgResName2ResId = new HashMap<String, Integer>();
 
 	// use SharedPreferences to check first install
 	public final static String SP_FIRST_START = "firstStart";
 
 	public static final Logger dbLogger = Logger.getLogger("db");
 	public static final Logger serviceLogger = Logger.getLogger("service");
	public static final Logger kaLogger = Logger.getLogger("ka");
 
 	public static void initLogger(boolean useLogCatAppender, boolean useFileAppender) {
 		LogConfigurator logConfigurator = new LogConfigurator();
 		logConfigurator.setFileName(KaConstants.LOG_ROOT + File.separator + "ka.log");
 		logConfigurator.setRootLevel(Level.DEBUG);
 		logConfigurator.setLevel("org.apache", Level.ERROR);
 		logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
 		logConfigurator.setMaxFileSize(1024 * 1024 * 5);
 		logConfigurator.setImmediateFlush(true);
 
 		logConfigurator.setUseLogCatAppender(useLogCatAppender);
 		logConfigurator.setUseFileAppender(useFileAppender);
 		logConfigurator.configure();
 		Logger logger = Logger.getLogger("ka");
		logger.info("ka logger start");
 	}
 
 	static {
 		Field[] fields = R.drawable.class.getDeclaredFields();
 		for (Field field : fields) {
 			// get all image from res which name start with type
 			if (field.getName().startsWith(RES_IMAGE_PREFIX)) {
 				try {
 					imgResName2ResId.put(field.getName(), field.getInt(R.drawable.class));
 				} catch (IllegalArgumentException e) {
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public static int getImgResIdByResName(String imgResName) {
 		return imgResName2ResId.get(imgResName);
 	}
 
 	private static Properties props = new Properties();
 
 	static {
 		try {
 			InputStream in = ResourceUtils.class.getResourceAsStream("/setting.properties");
 			props.load(in);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public static boolean getBoolean(String key, boolean defaultValue) {
 		String value = (String) props.get(key);
 		return value == null ? defaultValue : Boolean.valueOf(value);
 	}
 
 	public static String getString(String key, String defaultValue) {
 		String value = (String) props.get(key);
 		return value == null ? defaultValue : value;
 	}
 
 	public static int getInteger(String key, int defaultValue) {
 		String value = (String) props.get(key);
 		return value == null ? defaultValue : Integer.valueOf(value);
 	}
 
 	public static String getValue(String key) {
 
 		try {
 			String value = props.getProperty(key);
 			MyDebug.printFromPJ(value);
 			return value;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public static String getSharedPreferencesString(Context context, String key) {
 		return context.getSharedPreferences(SP_FIRST_START, 0).getString(key, StringUtils.STRING_NULL_VALUE);
 	}
 
 	public static void putSharedPreferencesString(Context context, String key, String value) {
 		context.getSharedPreferences(SP_FIRST_START, 0).edit().putString(key, value).commit();
 	}
 
 	public static boolean isServiceRunning(Context context, String className) {
 
 		boolean isRunning = false;
 		ActivityManager activityManager = (ActivityManager) context
 				.getSystemService(Context.ACTIVITY_SERVICE);
 		List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(30);
 
 		if (!(serviceList.size() > 0)) {
 			return false;
 		}
 
 		for (int i = 0; i < serviceList.size(); i++) {
 			if (serviceList.get(i).service.getClassName().equals(className) == true) {
 				isRunning = true;
 				break;
 			}
 		}
 
 		return isRunning;
 	}
 
 	public static boolean CheckNetwork(Context context, boolean isNotify) {
 		ConnectivityManager connectivityManager = (ConnectivityManager) context
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
 
 		if (activeNetInfo != null) {
 			int netType = activeNetInfo.getType();
 			switch (netType) {
 			case 0:
 			case 1:
 			case 9:
 			case 6:
 			case 7:
 				return true;
 			default:
 				return false;
 			}
 		}
 		return false;
 	}
 
 	public static boolean checkStorageSpace() {
 
 		StatFs stat = new StatFs(Environment.getDataDirectory().getAbsolutePath());
 		long blockSize = stat.getBlockSize();
 		long availableBlocks = stat.getAvailableBlocks();
 
 		if (blockSize * availableBlocks / 1024 / 1024 >= 128) {
 			return true;
 		}
 
 		return false;
 	}
 
 	public static String getLocalIpAddress() {
 		String localIPs = "";
 		try {
 			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
 					.hasMoreElements();) {
 				NetworkInterface intf = en.nextElement();
 				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
 						.hasMoreElements();) {
 					InetAddress inetAddress = enumIpAddr.nextElement();
 					if (!inetAddress.isLoopbackAddress()) {
 						localIPs = inetAddress.getHostAddress().toString();
 					}
 				}
 			}
 		} catch (SocketException ex) {
 		}
 		return localIPs;
 	}
 
 	public static boolean hasExternalStorage() {
 		String state = android.os.Environment.getExternalStorageState();
 		if (android.os.Environment.MEDIA_MOUNTED.equals(state)) {
 			return true;
 		}
 		return false;
 	}
 
 	public static long getSDCardIdleSpace() {
 		if (!hasExternalStorage())
 			return 0;
 
 		return getPathSpace(getSDCardpath());
 	}
 
 	public static long getPathSpace(String path) {
 		StatFs statFs = new StatFs(path);
 		return statFs.getBlockSize() * (long) statFs.getAvailableBlocks();
 	}
 
 	public static String getSDCardpath() {
 		return Environment.getExternalStorageDirectory().getPath();
 	}
 
 	public static int dip2px(Context context, float dipValue) {
 		final float scale = context.getResources().getDisplayMetrics().density;
 		return (int) (dipValue * scale + 0.5f);
 	}
 
 	public static int px2dip(Context context, float pxValue) {
 		final float scale = context.getResources().getDisplayMetrics().density;
 		return (int) (pxValue / scale + 0.5f);
 	}
 
 }
