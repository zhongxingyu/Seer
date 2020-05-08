 package com.dary.xmpp;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.RingtoneManager;
 import android.os.Environment;
 import android.os.Vibrator;
 
 import com.dary.xmpp.ui.MainActivity;
 
 public class Tools {
 
 	public static void Vibrator(Context context, int time) {
 		Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
 		long[] pattern = { 0, time };
 		vibrator.vibrate(pattern, -1);
 	}
 
 	public static void makeNotification(Context context, String str) {
 		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 		Notification notification = new Notification(R.drawable.ic_launcher, str, System.currentTimeMillis());
 		notification.flags = Notification.FLAG_AUTO_CANCEL;
 		Intent intent = new Intent(MyApp.getContext(), MainActivity.class);
 		PendingIntent contentIntent = PendingIntent.getActivity(MyApp.getContext(), 0, intent, 0);
 		notification.setLatestEventInfo(MyApp.getContext(), str, str, contentIntent);
 		notificationManager.notify(R.drawable.ic_launcher, notification);
 	}
 
 	public static void makeSound(Context context) {
 		MediaPlayer mediaPlayer = new MediaPlayer();
 		try {
 			mediaPlayer.setDataSource(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
 		try {
 			mediaPlayer.prepare();
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		mediaPlayer.start();
 		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
 
 			public void onCompletion(MediaPlayer mp) {
 				mp.release();
 			}
 		});
 	}
 
 	public static String delLastLine(StringBuilder sb) {
 		return sb.delete(sb.toString().length() - 1, sb.toString().length()).toString();
 	}
 
 	// Photo命令中,照片的命名
 	public static String getTimeStrHyphen() {
 		Date d = new Date();
 		SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
 		String timeStr = sdFormatter.format(d);
 		return timeStr;
 	}
 
 	// doLog中用到
 	public static String getTimeStr() {
 		Date d = new Date();
 		SimpleDateFormat sdFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
 		String retStrFormatNowDate = sdFormatter.format(d);
 		return retStrFormatNowDate;
 	}
 
 	public static String getTimeStr(long time) {
 		Date d = new Date(time);
 		SimpleDateFormat sdFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
 		String retStrFormatNowDate = sdFormatter.format(d);
 		return retStrFormatNowDate;
 	}
 
 	public static void doLog(String str, boolean isLogToFile, boolean isLogToDatabase) {
 		System.out.println(str);
 		if (isLogToFile) {
 			try {
 				// 注意如果文件不存在的时候(确切的说应该是文件的内容为空时),添加新内容之前要先添加换行符.
 				// FileOutputStream outStream =
 				// MyApp.getContext().openFileOutput("Log",
 				// Context.MODE_APPEND);
 				// 会直接创建
 				File file = MyApp.getContext().getFileStreamPath("Log");
 
 				StringBuilder sb = new StringBuilder();
 				if (file.exists()) {
 					sb.append("\n");
 				} else {
 					file.createNewFile();
 				}
 				sb.append(str);
 				// 占满整个一行,对齐
 				int length = 32;
 				for (int i = 0; i < length - str.length(); i++) {
 					sb.append(" ");
 				}
 				// sb.append("\n");
 				sb.append(Tools.getTimeStr());
 				FileOutputStream fos = new FileOutputStream(file, true);
 				fos.write(sb.toString().getBytes());
 				fos.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		if (isLogToDatabase) {
 			MainActivity.sendHandlerMessageToAddMsgView(DatabaseHelper.LOG_MESSAGE, "System Log", str, Tools.getTimeStr());
 			DatabaseHelper.insertMsgToDatabase(DatabaseHelper.LOG_MESSAGE, "System Log", str, Tools.getTimeStr());
 		}
 	}
 
 	public static void doLogJustPrint(String str) {
 		doLog(str, false, false);
 	}
 
 	public static void doLogPrintAndFile(String str) {
		doLog(str, true, false);
 	}
 
 	public static void doLogAll(String str) {
 		doLog(str, true, true);
 	}
 
 	public static String getAppVersionName(Context context) {
 		String versionName = "";
 		try {
 			PackageManager pm = context.getPackageManager();
 			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
 			versionName = pi.versionName;
 			if (versionName == null || versionName.length() <= 0) {
 				return "";
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return versionName;
 	}
 
 	// 移除Resource
 	public static String getAddress(String str) {
 		return str.split("/", -1)[0];
 	}
 
 	public static boolean isNumeric(String str) {
 		Pattern pattern = Pattern.compile("[0-9]*");
 		Matcher isNum = pattern.matcher(str);
 		if (!isNum.matches()) {
 			return false;
 		}
 		return true;
 	}
 
 	public static String getSDPath() {
 		File sdDir = null;
 		boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
 		if (sdCardExist) {
 			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
 		}
 		return sdDir.toString();
 	}
 
 }
