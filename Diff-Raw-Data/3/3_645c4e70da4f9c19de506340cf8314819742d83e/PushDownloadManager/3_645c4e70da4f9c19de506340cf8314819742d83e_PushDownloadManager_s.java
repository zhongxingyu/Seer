 package com.joy.launcher2.push;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.RandomAccessFile;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.content.Context;
 import android.util.Log;
 import android.view.View;
 
 import com.joy.launcher2.LauncherApplication;
 import com.joy.launcher2.network.impl.Service;
 import com.joy.launcher2.util.Constants;
 import com.joy.launcher2.util.Util;
 
 /**
  * download manager
  * 
  * @author wanghao
  * 
  */
 public class PushDownloadManager {
 
 	final String TAG = "PushDownloadManager";
 	
 	public static Map<String, PushDownLoadTask> map = new HashMap<String, PushDownLoadTask>();
 
 	private Service mService;
 
 	static PushDownloadManager mDownloadManager;
 	
 	public static boolean isPause = false;
 
 	Context mContext;
 	private PushDownloadManager(Context context) {
 		 mContext = context;
 		try {
 			mService = Service.getInstance();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static PushDownloadManager getInstances() {
 		if (mDownloadManager == null) {
 			mDownloadManager = new PushDownloadManager(LauncherApplication.mContext);
 		}
 		return mDownloadManager;
 	}
 
     public PushDownLoadTask newDownLoadTask(View view, PushDownloadInfo dInfo, PushCallBack callback, boolean secretly)
     {
     	if (dInfo == null) {
 			return null;
 		}
 
 		int id = dInfo.getId();
 		// 已经在下载了
 		PushDownLoadTask task = getDowmloadingTask(id);
 		if (task != null) {
 			Log.i(TAG, "--------> 已经在下载了！！！");
 			return null;
 		}
 		Log.i(TAG, "-----dInfo---> getCompletesize :" + dInfo.getCompletesize());
 		//completesize == 0是新建下载
 		if (dInfo.getCompletesize() == 0) {
 			// 检查本地是否有重名了的文件
 			File localfile = new File(Constants.DOWNLOAD_APK_DIR + "/"+ dInfo.getFilename());
 			localfile = Util.getCleverFileName(localfile);
 			dInfo.setLocalname(localfile.getName());
 		}
 
 
 		// 创建线程开始下载
 		File file = new File(Constants.DOWNLOAD_APK_DIR + "/"+ dInfo.getLocalname());
 
 		RandomAccessFile rf = null;
 		try {
 			rf = new RandomAccessFile(file, "rwd");
 			rf.setLength(dInfo.getFilesize()*1024);
 			rf.close();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		PushDownLoadTask downloader = new PushDownLoadTask(dInfo, file,callback,secretly);
 		// 加入map
 		map.put(String.valueOf(id), downloader);
     	return downloader;
     }
 	
 	// 下载子线程
 	public class PushDownLoadTask implements Runnable {
 
 		private boolean isSecretly;//下载方式 false正常  true静默下载
 		private File file;
 		private PushDownloadInfo downinfo;
 		PushCallBack callback;
 		public PushDownLoadTask(PushDownloadInfo downinfo,File file,PushCallBack callback,boolean secretly) {
 			this.downinfo = downinfo;
 			this.file = file;
 			this.callback = callback;
 			isSecretly = secretly;
 			callback.downloadUpdate();
 		}
 		public PushDownloadInfo getDownloadInfo(){
 			return downinfo;
 		}
 		public boolean isSecretly(){
 			return isSecretly;
 		}
 		public void run() {
 			InputStream is = null;
 			RandomAccessFile randomAccessFile = null;
 			try {
 				int startPos = downinfo.getCompletesize()*1024;
 				int endPos = downinfo.getFilesize()*1024;
 				is = mService.getPushDownLoadInputStream(downinfo.getUrl(), startPos, endPos);
 				boolean isBreakPoint = mService.getIsBreakPoint(downinfo.getUrl());
 				randomAccessFile = new RandomAccessFile(file, "rwd");
 				if(!isBreakPoint)
 				{
 					downinfo.setCompletesize(0);
 					startPos = 0;
 				}
 				// 从断点处 继续下载（初始为0）
 				randomAccessFile.seek(startPos);
 				if (is == null) {
 					return;
 				}
 				final int length = 1024;
 				byte[] b = new byte[length];
 				int len = -1;
 				int pool = 0;
 
 				boolean isover = false;
 				long startime = System.currentTimeMillis();
 				Log.e(TAG, "-----downinfo starttime--->1 " + startime);
 				int tempLen = startPos;
 				callback.downloadUpdate();
 				while ((len = is.read(b))!=-1) {
 					if (isPause) {
 						return;
 					}
 
 					randomAccessFile.write(b, 0, len);
 					
 					pool += len;
 					if (pool >= 100 * 1024) { // 100kb写一次数据库
 						Log.e(TAG, "-----下载  未完成----" + len);
 						PushDownLoadDBHelper.getInstances().update(downinfo); //暂不支持断点下载
 						pool = 0;
 						callback.downloadUpdate();// 刷新一次
 					}
 					tempLen += len;
 					downinfo.setCompletesize(tempLen/1024);
 					if (pool != 0) {
 						//PushDownLoadDBHelper.getInstances().update(downinfo);/// 暂不支持断点下载
 					}
 				}
 				long endtime = System.currentTimeMillis();
 				Log.e(TAG, "-----downinfo time--->1 " + (endtime - startime));
 			} catch (Exception e) {
 				//Log.i(TAG, "-------->e " + e);
 				e.printStackTrace();
 			} finally {
 				// end.countDown();
 				Log.e(TAG, "---over----");
 
 				if (downinfo.getCompletesize() >= downinfo.getFilesize()) {
 					Log.i(TAG, "----finsh----");
					PushDownLoadDBHelper.getInstances().update(downinfo);
 					if(callback != null){
 						callback.downloadSucceed();
 					}
 				}else{
 					callback.downloadFailed();
 				}
 				callback.downloadUpdate();
 				map.remove(String.valueOf(downinfo.getId()));
 				if (is != null) {
 					try {
 						is.close();
 					} catch (IOException e) {
 					}
 				}
 				if (randomAccessFile != null) {
 					try {
 						randomAccessFile.close();
 					} catch (IOException e) {
 					}
 				}
 			}
 		}
 	}
 	public PushDownLoadTask getDowmloadingTask(int id){
 		PushDownLoadTask task = map.get(String.valueOf(id));
 		if (task != null) {
 			return task;
 		}
 		return null;
 	}
 	public boolean isCompleted(int id){
 		final PushDownloadInfo dInfo = PushDownLoadDBHelper.getInstances().get(id);
 		if (dInfo!= null&&dInfo.getCompletesize() >= dInfo.getFilesize()) {
 			return true;
 		}
 		return false;
 	}
 
 	public interface PushCallBack{
 		public void downloadSucceed();
 		public void downloadFailed();
 		public void downloadUpdate();
 	}
 }
