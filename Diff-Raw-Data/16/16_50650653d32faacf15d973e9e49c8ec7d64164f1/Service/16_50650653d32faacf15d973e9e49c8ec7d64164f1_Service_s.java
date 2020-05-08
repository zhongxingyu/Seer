 package com.joy.launcher2.network.impl;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONObject;
 
 import android.R.integer;
 import android.graphics.Bitmap;
 import android.os.Handler;
 import android.os.Message;
 
 import com.joy.launcher2.cache.ImageOption;
 import com.joy.launcher2.network.handler.ActivateHanlder;
 import com.joy.launcher2.network.handler.AppListHandler;
 import com.joy.launcher2.network.handler.BitmapHandler;
 import com.joy.launcher2.network.handler.VirtualShortcutListHandler;
 import com.joy.launcher2.network.util.ClientHttp;
 import com.joy.launcher2.network.util.ClientInterface;
 import com.joy.launcher2.network.util.Protocal;
 import com.joy.launcher2.util.SystemInfo;
 import com.joy.launcher2.wallpaper.ImageLoader;
 
 /**
  * 联网接口的具体实现
  * @author wanghao
  *
  */
 public class Service {
 	private static Service service;
 	ClientInterface cs = null;
 	ProtocalFactory pfactory;
 	Map<String, Protocal> protocals = Collections.synchronizedMap(new HashMap<String, Protocal>());
 
 	// 类似于AsycTask类
 	public interface CallBack {
 		/**
 		 * 在doInBackground之前被调用，这里是联网前，更新UI
 		 */
 		public void onPreExecute();
 
 		/**
 		 * 在doInBackground之后被调用，更新UI
 		 */
 		public void onPostExecute();
 
 		/**
 		 * 处理后台耗时事情，不可在此更新UI
 		 */
 		public void doInBackground();
 	}
 
 	private Service() {
 	};
 
 	public static synchronized Service getInstance() throws Exception {
 		if (service == null) {
 			service = new Service();
 			service.cs = new ClientHttp();
 			service.pfactory = new ProtocalFactory();
 		}
 		return service;
 	}
 
 	public void GotoNetwork(final CallBack callBack) {
 
 		final Handler handler = new Handler() {
 			public void handleMessage(Message message) {
 
 				int what = message.what;
 				switch (what) {
 				case 0:
 					callBack.onPreExecute();
 					break;
 				case 1:
 					callBack.onPostExecute();
 					break;
 				}
 			}
 		};
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				handler.sendEmptyMessage(0);
 				callBack.doInBackground();
 				handler.sendEmptyMessage(1);
 			}
 		}).start();
 		;
 	}
 
 	// --------------------------------------------------------------------------
 	/**
 	 * 释放网络资源
 	 */
 	public void shutdownNetwork() {
 		cs.shutdownNetwork();
 		cs = null;
 		service = null;
 	}
 
 	public boolean netWorkIsOK() {
 		return cs.isOK();
 	}
 
 	public Bitmap getBitmapByUrl(String url, ImageOption... option) {
 		// TODO Auto-generated method stub
 		Protocal protocal = pfactory.bitmapProtocal(url);
 		InputStream in = cs.getInputStream(protocal);
 		BitmapHandler bhandler = new BitmapHandler();
 		Bitmap bp = bhandler.getBitmapByUrl(in, url, option);
 		return bp;
 	}
 
 	public InputStream getDownLoadInputStream(String url){
 		
 		Protocal protocal = pfactory.downloadApkProtocal(url);
 		InputStream iStream = cs.getInputStream(protocal);
		
 		return iStream;
 	}
 	public InputStream getDownLoadInputStream(String url,int startPos,int endPos){
 		
 		Protocal protocal = pfactory.downloadApkProtocal(url);
 		protocal.setStartPos(startPos);
 		protocal.setEndPos(endPos);
 		InputStream iStream = cs.getInputStream(protocal);
		
 		return iStream;
 	}
 	
     public InputStream getPushDownLoadInputStream(String url,int startPos,int endPos){
 		
 		Protocal protocal = pfactory.downloadPushApkProtocal(url);
 		protocal.setStartPos(startPos);
 		protocal.setEndPos(endPos);
 		InputStream iStream = cs.getInputStream(protocal);
 		protocals.put(url, protocal);
 		return iStream;
 	}
     
     public boolean getIsBreakPoint(String url)
     {
     	Protocal protocal = protocals.get(url);
     	boolean isBreakPoint = false;
     	if(protocal != null)
     	{
     		isBreakPoint = protocal.getIsBreakPoint();
     	}
     	protocals.remove(url);
     	return isBreakPoint;
     }
     
      //add by huangming for push.
 	
     public InputStream getDownLoadPushApkInputStream(int id){
 		
 		Protocal protocal = pfactory.downloadPushApkProtocal(id);
 		InputStream iStream = cs.getInputStream(protocal);
 		return iStream;
 	}
     
     public InputStream getDownLoadPushApkInputStream(String url){
 		
 		Protocal protocal = pfactory.downloadPushApkProtocal(url);
 		InputStream iStream = cs.getInputStream(protocal);
 		return iStream;
 	}
     
    public Bitmap getDownLoadPushImage(int id){
 		
 		Protocal protocal = pfactory.downloadPushImageProtocal(id);
 		Bitmap image = cs.getBitmap(protocal);
 		return image;
 	}
    
    public Bitmap getDownLoadPushImage(String url){
 		
 		Protocal protocal = pfactory.downloadPushImageProtocal(url);
 		Bitmap image = cs.getBitmap(protocal);
 		return image;
 	}
    //end
 
 	public boolean activateLauncher(){
 		// TODO Auto-generated method stub
 		Protocal protocal = pfactory.activateProtocal();
 		JSONObject result = null;
 		try {
 			result = cs.request(protocal);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		ActivateHanlder activate = new ActivateHanlder();
 		boolean isActivate = activate.isActivate(result);
 	
 		return isActivate;
 	}
 
 	/**
 	 * 获取在线文件夹软件列表
 	 * @param folderType  0:game folder     1:application folder
 	 */
 	public List<Map<String, Object>> getShortcutListInFolder(int folderType){
 		Protocal protocal = pfactory.getAppInFolderProtocal(folderType);
 		JSONObject result = null;
 		try {
 			result = cs.request(protocal);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		VirtualShortcutListHandler handler = new VirtualShortcutListHandler();
 	
 		return handler.geShortcutList(result);
 	}
 	
 	/**
 	 * 获取游戏、应用列表
 	 * @param folderType  0:game     1:application
 	 */
 	public ArrayList<List<Map<String, Object>>> getApkList(int type,int index,int num){
 		Protocal protocal = pfactory.getApkListProtocal(type,index,num);
 		String string = null;
 		try {
 			string = cs.getString(protocal);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		AppListHandler handler = new AppListHandler();
 		return handler.getAppList(string,4,type);
 	}
 	 
 	//add by huangming for push.
 	public JSONObject getPushSettings()
 	{
 		Protocal protocal = pfactory.pushSettingsProtocal();
 		JSONObject json = null;;
 		try {
 			json = cs.request(protocal);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return json;
 	}
 	
 	public JSONObject getPushList()
 	{
 		Protocal protocal = pfactory.pushListProtocal();
 		JSONObject json = null;;
 		try {
 			json = cs.request(protocal);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return json;
 	}
 	
 	public JSONObject getPushDetail(int id)
 	{
 		Protocal protocal = pfactory.pushDetailProtocal(id);
 		JSONObject json = null;;
 		try {
 			json = cs.request(protocal);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return json;
 	}
 	//end 
 	
 	//add by huangming for online wallpaper
 	/**
 	 * 获得壁纸列表json对象
 	 * @param category
 	 * @param previousPage
 	 * @return JSONObject
 	 */
 	public JSONObject getWallPaperListJson(int category,  int previousPage) 
 	{
 		Protocal protocal = pfactory.wallpaperListProtocal(category, previousPage);
 		JSONObject result = null;
 		try {
 			result = cs.request(protocal);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return result;
 	}	
 		
 		/**
 		 *  获得壁纸类别json对象
 		 * @return JSONObject
 		 */
 	public JSONObject getWallpaperCategoryJson()
 	{
 		Protocal protocal = pfactory.wallpaperCategoryProtocal();
 		JSONObject result = null;
 		try {
 			result = cs.request(protocal);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return result;
 	}	
 		
 	/**
 	 * 通过data获得bitmap
 	 * @param data
 	 * @return Bitmap
 	 */
 	public Bitmap getWallpaperBitmap(String data, int width)
 	{
 		Protocal protocal = pfactory.wallpaperBitmapProtocal(data);
 		Bitmap bm = cs.getBitmap(protocal, width);
 		return bm;
 	}
 	
 	/**
 	 * 通过data获得流对象
 	 * @param data
 	 * @return InputStream
 	 */
 	public InputStream getWallpaperInputStream(String data)
 	{
 		Protocal protocal = pfactory.wallpaperBitmapProtocal(data);
 		InputStream is = cs.getInputStream(protocal);
 		return is;
 	}
 	//end	
 
 }
