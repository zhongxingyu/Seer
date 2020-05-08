 package com.upgrade.library.task;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.RandomAccessFile;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.support.v4.app.NotificationCompat;
 import android.text.TextUtils;
 
 import com.upgrade.library.R;
 import com.upgrade.library.util.LogUtil;
 import com.upgrade.library.util.MD5;
 import com.upgrade.library.util.UpgradeUtil;
 
 /**
  * 
  * @description 
  * 
  *
  *
  * @email 6881797@163.com
  */
 public class DownLoadUpgradeFileTask extends AsyncTask<Void, Integer, String> {
 
 	private String mUrl;
 	private Context mContext;
 	private String mDownloadFileDir;
 	private boolean mSupportBreakPoint;
 	private NotificationManager mNotiManager;
 	private Notification mNoti;
 	private NotificationCompat.Builder mBuilder;
 	private static final int notificationId = (int) System.currentTimeMillis();
 
 	/***
 	 * 
 	 * @param context  
 	 * @param url  The url of upgrade file url ,the format like: http://www...... .apk 
 	 * @param downLoadFileDir   the dir of the downloading upgrade file. Format like : /sdcard/...../ (It is required that this dir is only used to store the download upgrade file)
 	 * @param supportedBreakPoint  true means the downloading should support breakpoint continuingly.False means that every download is a new task ,and will
 	 * clear the former download.
 	 */
 	public DownLoadUpgradeFileTask(Context context, String url,String downLoadFileDir,boolean supportedBreakPoint) {
 		mUrl = url;
 		mContext = context.getApplicationContext();
 		mDownloadFileDir = downLoadFileDir;
 		mSupportBreakPoint = supportedBreakPoint;
 		mNotiManager = (NotificationManager) context
 				.getSystemService(Context.NOTIFICATION_SERVICE);
 		mBuilder = new NotificationCompat.Builder(mContext);
 		mBuilder.setAutoCancel(false);
 		mBuilder.setContentTitle(mContext
 				.getString(R.string.upgrade_is_loading));
 		mBuilder.setSmallIcon(R.drawable.ic_notification);
 		mBuilder.setProgress(100, 0, false);
 		mBuilder.setWhen(System.currentTimeMillis());
 		mBuilder.setTicker(mContext.getString(R.string.upgrade_is_loading));
		mBuilder.setContentIntent(PendingIntent.getBroadcast(mContext, 1, new Intent(), 0));
 		mNoti = mBuilder.build();
 
 		mNoti.flags =  Notification.FLAG_SHOW_LIGHTS;
 
 	}
 
 	@Override
 	protected void onPreExecute() {
 		mNoti = mBuilder.build();
 		mNoti.flags = Notification.FLAG_SHOW_LIGHTS;
 		mNoti.defaults = Notification.DEFAULT_SOUND;
 		mNoti.ledARGB = 0xff00ffff;
 		mNoti.ledOffMS = 2000;
 		mNoti.ledOnMS = 2000;
 		mNotiManager.notify(notificationId, mNoti);
 		super.onPreExecute();
 	}
 
 	@Override
 	protected String doInBackground(Void... params) {
 		String filePath = null;
 		InputStream is = null;
 		FileOutputStream fos = null;
 		HttpURLConnection conn = null;
 		RandomAccessFile raf = null;
 		try {
 			long total = UpgradeUtil.getNetFileSize(mUrl);
 			URL url = new URL(mUrl);
 			conn = (HttpURLConnection) url.openConnection();
 			conn.setConnectTimeout(5 * 1000);
 			conn.setReadTimeout(1000 * 10);
 			conn.setRequestMethod("GET");
 			
 			String fileName = MD5.md5s(mUrl+ UpgradeUtil.getPackageVersionName(mContext)+UpgradeUtil.getPackageVersionCode(mContext))+".apk";
 			File upgradeDir = new File(mDownloadFileDir);
 			File upgrade = new File(upgradeDir,fileName);
 		
 			if(mSupportBreakPoint  && upgrade.exists() && upgrade.length()<=total){
 				long fileLength = upgrade.length();
 				
 				if(fileLength == total){
 					filePath = upgrade.getAbsolutePath();
 					return filePath;
 				}
 				conn.setRequestProperty("RANGE", "bytes="+fileLength+"-");
 				conn.connect();
 				is = conn.getInputStream();
 				int preciousProgress = 0;
 				long size = fileLength;
 				int count = 0;
 				raf = new RandomAccessFile(upgrade, "rws");
 				byte[] temp = new byte[1024 * 60];
 				raf.seek(fileLength);
 				count = is.read(temp, 0, temp.length);
 				while (count > 0) {
 					if(isCancelled()){
 						return null;
 					}
 					raf.write(temp, 0, count);
 					size += count;
 					int progress =(int) (size * 100 / total);
 					if (progress - preciousProgress > 0) {
 						publishProgress(progress);
 						preciousProgress = progress;
 					}
 					count = is.read(temp, 0, temp.length);
 				}
 				raf.close();
 				filePath = upgrade.getAbsolutePath();
 			}else{
 				conn.connect();
 				is = conn.getInputStream();
 				UpgradeUtil.deleteContentsOfDir(upgradeDir);
 				fos = new FileOutputStream(upgrade);
 				long size = 0;
 				int count = 0;
 				byte[] temp = new byte[1024 * 60];
 				count = is.read(temp, 0, temp.length);
 				int preciousProgress = 0;
 				while (count > 0) {
 					if(isCancelled()){
 						return null;
 					}
 					fos.write(temp, 0, count);
 					size += count;
 					int progress =(int) (size * 100 / total);
 					if (progress - preciousProgress > 0) {
 						publishProgress(progress);
 						preciousProgress = progress;
 					}
 					count = is.read(temp, 0, temp.length);
 				}
 				fos.flush();
 				filePath = upgrade.getAbsolutePath();
 			}
 			//File upgrade = new File(upgradeDir, "focus_second.apk");
 		} catch (MalformedURLException e) {
 			LogUtil.e(e);
 			cancel(true);
 			return null;
 		} catch (IOException e) {
 			LogUtil.e(e);
 			cancel(true);
 			return null;
 		} finally {
 			UpgradeUtil.closeInputStream(is);
 			UpgradeUtil.closeOutputStream(fos);
 			if(raf!=null){
 				try {
 					raf.close();
 				} catch (IOException e) {
 					LogUtil.e(e);
 				}
 			}
 		}
 
 		return filePath;
 	}
 
 	@Override
 	protected void onProgressUpdate(Integer... values) {
 		LogUtil.i("the current percent of downloaded:" + values[0]);
 		super.onProgressUpdate(values);
 		if (values[0] < 100) {
 			mBuilder.setContentText(mContext.getString(R.string.has_downed)
 					+ " " + values[0] + "%");
 			mNoti = mBuilder.setProgress(100, values[0], false).build();
 			mNotiManager.notify(notificationId, mNoti);
 		}
 	}
 
 	@Override
 	protected void onPostExecute(String result) {
 		if (TextUtils.isEmpty(result))
 			return;
 		super.onPostExecute(result);
 		// mBuilder.setTicker(mContext.getString(R.string.downed_complete));
 		Intent promptInstall = new Intent(Intent.ACTION_VIEW).setDataAndType(
 				Uri.fromFile(new File(result)),
 				"application/vnd.android.package-archive");
 		promptInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 
 		mBuilder.setTicker(mContext.getString(R.string.downed_complete));
 		mBuilder.setContentTitle(mContext.getString(R.string.downed_complete));
 		mBuilder.setContentIntent(PendingIntent.getActivity(mContext, 0,
 				promptInstall, PendingIntent.FLAG_UPDATE_CURRENT));
 		mBuilder.setContentText(mContext.getString(R.string.has_downed) + " "
 				+ 100 + "%");
 		mBuilder.setDefaults(Notification.DEFAULT_SOUND);
 		mNoti = mBuilder.setProgress(100, 100, false).build();
 		mNoti.flags = Notification.FLAG_SHOW_LIGHTS;
 		mNoti.defaults = Notification.DEFAULT_SOUND;
 		mNoti.ledARGB = 0xff00ffff;
 		mNoti.ledOffMS = 2000;
 		mNoti.ledOnMS = 2000;
 		mNotiManager.notify(notificationId, mNoti);
 
 		mContext.startActivity(promptInstall);
 	}
 
 	@Override
 	protected void onCancelled(String result) {
 		mBuilder.setTicker(mContext.getString(R.string.download_fail));
 		mBuilder.setContentTitle(mContext.getString(R.string.download_fail));
 		mBuilder.setContentText(mContext.getString(R.string.please_try_later));
 		mBuilder.setContentIntent(PendingIntent.getBroadcast(mContext, 0,
 				new Intent(), PendingIntent.FLAG_NO_CREATE));
 		mBuilder.setDefaults(Notification.DEFAULT_SOUND);
 		mBuilder.setAutoCancel(true);
 		mNoti = mBuilder.build();
 		mNoti.flags = Notification.FLAG_SHOW_LIGHTS
 				| Notification.FLAG_AUTO_CANCEL;
 		mNoti.defaults = Notification.DEFAULT_SOUND;
 		mNoti.ledARGB = 0xff00ffff;
 		mNoti.ledOffMS = 2000;
 		mNoti.ledOnMS = 2000;
 		mNotiManager.notify(notificationId, mNoti);
 	}
 
 }
