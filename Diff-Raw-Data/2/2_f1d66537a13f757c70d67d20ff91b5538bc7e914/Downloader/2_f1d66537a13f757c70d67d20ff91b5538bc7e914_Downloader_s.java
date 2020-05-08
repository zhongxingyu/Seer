 package com.albaniliu.chuangxindemo.util;
 
 import java.io.File;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.albaniliu.chuangxindemo.data.FInode;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.Environment;
 import android.os.IBinder;
 import android.util.Log;
 
 public class Downloader extends Service {
 
 	private String TAG = "Downloader";
 	private MyBinder mBinder = new MyBinder();
 	private static Thread downloadThread;
 	private JSONArray allDir;
 	private FInode root;
 
 	public FInode getRoot() {
 		return root;
 	}
 
 	public void setRoot(FInode root) {
 		this.root = root;
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// TODO Auto-generated method stub
 		return mBinder;
 	}
 
 	@Override
 	public void onCreate() {
 		// TODO Auto-generated method stub
 		super.onCreate();
 		allDir = new JSONArray();
 		root = new FInode();
 		if (downloadThread == null) {
 	        downloadThread = new DownloadThread();
 	        downloadThread.start();
         }
 	}
 	
 	public void refreshForce() {
 		if (downloadThread == null || !downloadThread.isAlive()) {
 			downloadThread = new DownloadThread();
 	        downloadThread.start();
 		}
 	}
 	
 	public void refresh() {
 		if (allDir.length() == 0) {
 			downloadThread = new DownloadThread();
 	        downloadThread.start();
 		}
 	}
 	
 	public boolean isFinished() {
 		return !downloadThread.isAlive();
 	}
 	
 	public JSONArray getAllDir() {
 		return allDir;
 	}
 	
 	public FInode getLeaf(String path) {
 		String paths[] = path.split(",");
 		FInode res = new FInode();
 		FInode tmp = root;
 		for (int i = 0; i < paths.length; i++) {
 			int index = Integer.parseInt(paths[i]);
 			if (index >= tmp.getChildren().size())
 				break;
 			tmp = tmp.getChildren().get(index);
 		}
 		if (tmp.isLeaf())
 			res = tmp;
 		return res;
 	}
 	
 	class DownloadThread extends Thread {
         public void run() {
         	int process = 0;
             try {
             	FInode tmpRoot = new FInode();
                 allDir = HTTPClient.getJSONArrayFromUrl(HTTPClient.URL_INDEX + "?timestamp=" + System.currentTimeMillis());
 //                allDir = HTTPClient.getJSONArrayFromUrl(HTTPClient.URL_INDEX);
                 tmpRoot.setDirs(allDir);
                 download(tmpRoot, allDir);
                 root = tmpRoot;
                 process = 100;
             } catch (Exception e) {
                 e.printStackTrace();
             }
             
             //  send finished broadcast
             Intent intent=new Intent();
             intent.putExtra("process", process);
             intent.setAction("com.albaniliu.chuangxindemo.action.downloader");
             sendBroadcast(intent);
         }
         
         void download(FInode parent, JSONArray json) throws Exception {
         	int index = 0;
         	for (int i = 0; i < json.length() && !Thread.currentThread().isInterrupted(); i++) {
                 JSONObject obj = (JSONObject) json.get(i);
                 if (obj.has("url")) {
                 	FInode inode = new FInode(parent);
 	                String url = HTTPClient.HOST + obj.getString("url");
 	                Log.v(TAG, "url: " + url);
 	                JSONArray jArray = HTTPClient.getJSONArrayFromUrl(url + "?timestamp=" + System.currentTimeMillis());
 //	                JSONArray jArray = HTTPClient.getJSONArrayFromUrl(url);
 	                inode.setDirs(jArray);
 	                download(inode, jArray);
 	                inode.setIndex(index);
 	                parent.addChild(inode);
 	                index++;
 	                
 	                String cover = HTTPClient.COVER_INDEX_PREFIX + obj.getString("cover");
 	                Log.v(TAG, obj.getString("cover"));
 	                String coverPath = obj.getString("cover");
 	                String coverName = coverPath.substring(coverPath.lastIndexOf('/') + 1);
 	                Log.v(TAG, coverName);
 	                String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/liangdemo1/"
 	                        + coverName;
 	                File file = new File(fileName);
 	                if (file.exists()) {
 	                    // 
 	                } else {
 	                	HTTPClient.getStreamFromUrl(cover, fileName);
 	                }
                 } else {
                 	String path = HTTPClient.HOST + obj.getString("path");
                 	String name = path.substring(path.lastIndexOf('/') + 1);
                 	String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/liangdemo1/"
 	                        + name;
                 	File file = new File(fileName);
 	                if (file.exists()) {
 	                    // 
 	                } else {
 	                	HTTPClient.getStreamFromUrl(path, fileName);
 	                }
 	                
 	                if (obj.has("pic")) {
 	                	String picPath = HTTPClient.HOST + obj.getString("pic");
	                	String picName = path.substring(path.lastIndexOf('/') + 1);
 	                	String picFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/liangdemo1/"
 		                        + picName;
 	                	File picFile = new File(picFileName);
 		                if (picFile.exists()) {
 		                    // 
 		                } else {
 		                	HTTPClient.getStreamFromUrl(picPath, picFileName);
 		                }
 	                }
                 }
             }
         }
     }
 	
 	public class MyBinder extends Binder {
         public Downloader getService() {
             return Downloader.this;
         }
     }
 
 }
