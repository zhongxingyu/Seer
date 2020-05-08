 package com.joy.launcher2.download;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.RandomAccessFile;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import android.R.integer;
 import android.content.Context;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.widget.Toast;
 
 import com.joy.launcher2.LauncherApplication;
 import com.joy.launcher2.R;
 import com.joy.launcher2.joyfolder.JoyIconView;
 //import com.joy.launcher2.ShortcutInfo;
 import com.joy.launcher2.network.impl.Service;
 import com.joy.launcher2.util.Constants;
 import com.joy.launcher2.util.Util;
 
 /**
  * download manager
  * 
  * @author wanghao
  * 
  */
 public class DownloadManager {
 
 	final boolean isDebug = true;
 	final String TAG = "DownloadManager";
 	
 	private ExecutorService pool = Executors.newFixedThreadPool(2);
 
 	public static Map<String, DownLoadTask> map = new HashMap<String, DownLoadTask>();
 
 	private Service mService;
 
 	static DownloadManager mDownloadManager;
 
 	Context mContext;
 	private DownloadManager(Context context) {
 		 mContext = context;
 		try {
 			mService = Service.getInstance();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static DownloadManager getInstances() {
 		if (mDownloadManager == null) {
 			mDownloadManager = new DownloadManager(LauncherApplication.mContext);
 		}
 		return mDownloadManager;
 	}
 
 	public static boolean isPause = false;
 
 	public void createTask(View view,DownloadInfo dInfo,CallBack callback,boolean secretly) {
 		
 		if (!Util.hasSdcard()) {
 			return;
 		}
 		if (dInfo == null) {
 			return;
 		}
 
 		int id = dInfo.getId();
 		// 已经在下载了
 		DownLoadTask task = getDowmloadingTask(id);
 		if (task != null) {
 			if(isDebug) Log.i(TAG, "is downloading,please wait for a moment");
 			return;
 		}
 		if(isDebug) Log.i(TAG, "getCompletesize start:" + dInfo.getCompletesize());
 		//completesize == 0是新建下载
 		if (dInfo.getCompletesize() == 0) {
 			// 检查本地是否有重名了的文件
 			File localfile = new File(Constants.DOWNLOAD_APK_DIR + "/" + dInfo.getFilename() + ".apk");
 			localfile = Util.getCleverFileName(localfile);
 			dInfo.setLocalname(localfile.getName());
 		}
 
 		dInfo.setView(view);
 
 		// 创建线程开始下载
 		File file = new File(Constants.DOWNLOAD_APK_DIR + "/"+ dInfo.getLocalname());
 
 		RandomAccessFile rf = null;
 		try {
 			rf = new RandomAccessFile(file, "rwd");
 			rf.setLength(dInfo.getFilesize());
 			rf.close();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		DownLoadTask downloader = new DownLoadTask(dInfo, file,callback,secretly);
 		// 加入map
 		map.put(String.valueOf(id), downloader);
 		// 加入线程池
 		pool.execute(downloader);
 
 	}
 	// 下载子线程
 	public class DownLoadTask extends Thread {
 
 		private boolean isSecretly;//下载方式 false正常  true静默下载
 		private File file;
 		private DownloadInfo downinfo;
 		CallBack callback;
 		public DownLoadTask(DownloadInfo downinfo,File file,CallBack callback,boolean secretly) {
 			this.downinfo = downinfo;
 			this.file = file;
 			this.callback = callback;
 			isSecretly = secretly;
 			callback.downloadUpdate();
 		}
 		public DownloadInfo getDownloadInfo(){
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
 				is = mService.getDownLoadInputStream(downinfo.getUrl(),startPos,endPos);
 				if (is == null) {
 					return;
 				}
 				randomAccessFile = new RandomAccessFile(file, "rwd");
 				
 				boolean isBreakPoint = mService.getIsBreakPoint(downinfo.getUrl());
 				if(!isBreakPoint)
 				{
 					downinfo.setCompletesize(0);
 					startPos = 0;
 				}
 				// 从断点处 继续下载（初始为0）
 				randomAccessFile.seek(startPos);
 				
				final int length = 1024;
 				byte[] b = new byte[length];
 				int len = -1;
 				int pool = 0;
 
 				boolean isover = false;
 				int tempLen = startPos;
 				callback.downloadUpdate();
 				while ((len = is.read(b))!=-1) {
 					if (isPause) {
 						return;
 					}
 
 					randomAccessFile.write(b, 0, len);
 					
 					pool += len;
					if (pool >= 100 * 1024) { // 100kb写一次数据库
 						if(isDebug) Log.i(TAG, "--downloading--");
 						DownLoadDBHelper.getInstances().update(downinfo); //暂不支持断点下载
 						pool = 0;
 						callback.downloadUpdate();// 刷新一次
 					}
 					tempLen += len;
 					downinfo.setCompletesize(tempLen/1024);
 					if (pool != 0) {
 						DownLoadDBHelper.getInstances().update(downinfo);/// 暂不支持断点下载
 					}
 				}
 			} catch (Exception e) {
 				Log.i(TAG, "DownLoadTask error " + e);
 			} finally {
 				// end.countDown();
 				if(isDebug) Log.i(TAG, "download over");
 
 				callback.downloadUpdate();
 				if (downinfo.getCompletesize() >= downinfo.getFilesize()) {
 					if(isDebug) Log.i(TAG, "download finsh");
 					DownLoadDBHelper.getInstances().update(downinfo);
 					if(callback != null){
 						callback.downloadSucceed();
 					}
 				}else{
 					callback.downloadFailed();
 				}
 				
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
 	public DownLoadTask getDowmloadingTask(int id){
 		DownLoadTask task = map.get(String.valueOf(id));
 		if (task != null) {
 			return task;
 		}
 		return null;
 	}
 	public boolean isCompleted(int id){
 		final DownloadInfo dInfo = DownLoadDBHelper.getInstances().get(id);
 		if (dInfo!= null&&dInfo.getCompletesize() >= dInfo.getFilesize()) {
 			return true;
 		}
 		return false;
 	}
 
 	public interface CallBack{
 		public void downloadSucceed();
 		public void downloadFailed();
 		public void downloadUpdate();
 	}
 
 	public void onDestroy() {
 		pool.shutdown();
 	}
 }
