 package com.zhangwei.stock.storage;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 
import org.apache.http.util.ByteArrayBuffer;

 
 
 import cn.zipper.framwork.core.ZApplication;
 
 import com.google.gson.Gson;
 import com.zhangwei.stock.utils.ZipUtil;
 
 import android.content.Context;
 import android.os.Environment;
 import android.util.Log;
 
 /**
  * <p>在外存储存放stock信息，存放路径在app_data_dir：/sdcard/STOCKS下
  * <p> key值不包括app_data_dir，如 history/sh600031/20130505
  * <p>本目录 存放stocklist列表
  * <p>last目录，存放上次扫描的全部股票文件，eg:sh600031
  * <p>history/<股票代号>目录，存放不同时间版本的stock文件，eg：20130505
  * @author zhangwei
  */
 public class SDCardStorageManager {
 	public static final String CHACHE_PREFIX = "storage_"; 
 	private static final String storage_dir_in_sdcard = ".sobulldog";
 
 	private static final String TAG = "SDCardStorageManager";
 	private static SDCardStorageManager instance;
 	private HashMap<String, StorageValue> cache;
 	private Context context;
 	private Gson gson;
 	
 	private String app_data_dir;
 
 	private SDCardStorageManager() {
 		context = ZApplication.getInstance();
 		cache = new HashMap<String, StorageValue>();
 		gson = new Gson();
 		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
 			File appDataPath = new File(Environment.getExternalStorageDirectory(), storage_dir_in_sdcard);
 			app_data_dir = appDataPath.getAbsolutePath();
 			if(!appDataPath.exists()) {
 				appDataPath.mkdirs();
 			}
 			load(app_data_dir);
 		}
 
 	}
 
 
 	
 	private void load(String fileDir) {
 		Log.e(TAG, "StorageManager load -  fileDir:" + fileDir);
 
 		File parentPath = new File(fileDir);
 		File[] filelist = parentPath.listFiles();
 		for (File f : filelist) {
 			if(f.isFile() && f.getName().startsWith(CHACHE_PREFIX)){
 				String key = f.getAbsolutePath()
 						      .substring(app_data_dir.length()+1);
 				Log.e(TAG, "cache.put key:" + key);
 				cache.put(key, new StorageValue(key, (int) f.length()));
 			}else if(f.isDirectory()){
 				load(f.getPath()) ; 
 			}
 			
 		}
 	}
 
 	public static SDCardStorageManager getInstance() {
 
 		if (instance == null) {
 			instance = new SDCardStorageManager();
 		}
 
 		return instance;
 	}
 
 	/**
 	 * @param dir 在外存储的目录的路径（不含sdcard路径， 上层一般使用包名加多级目录构成）
 	 * @param file 文件名
 	 * @return 被Cache的元数据信息
 	 */
 	public String getItem(String dir, String file) {
 		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
 			return null;
 		}
 
 		
 		String key = null;
 		if(dir!=null){
 			key = dir + "/" + CHACHE_PREFIX + file;
 		}else{
 			key = CHACHE_PREFIX + file;
 		}
 
 		if (cache.containsKey(key)) {
 			// Read the created file and display to the screen
 			try {
 
 				File dataFile = new File(app_data_dir + "/" + key);
 				if(!dataFile.exists()){
 					return null;
 				}
 				
 				FileInputStream mInput =  new FileInputStream(dataFile);
 				//FileInputStream mInput = context.openFileInput(key);
 				int len = 0;
 				byte[] data = new byte[1024];
 
				ByteArrayBuffer ba = new ByteArrayBuffer(1024);
				
 				while ((len = mInput.read(data)) != -1) {
					ba.append(data, 0, len);
 				}
 
 				mInput.close();
 				
 				//return ZipUtil.uncompress(sb.toString());
				return new String(ba.toByteArray());
 
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			return null;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * @param dir 在外存储的目录的路径（不含sdcard路径， 上层一般使用包名加多级目录构成）
 	 * @param file 文件名
 	 * @param cls 类型
 	 * @return 被Cache的元数据信息
 	 */
 	public Object getItem(String dir, String file, Class<?> cls) {
 		String jsonStr = getItem(dir, file);
 		Object object = null;
 		try{
 			if (jsonStr != null) {
 				object = gson.fromJson(jsonStr, cls);
 			}
 			
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 
 		return object;
 	}
 	
 /*	public Object getItem(String path, String file, Type type) {
 		String jsonStr = getItem(path, file);
 		Object object = null;
 		try{
 			if (jsonStr != null) {
 				object = gson.fromJson(jsonStr, type);
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 
 
 		return object;
 	}*/
 
 	/**
 	 * @param dir 要插入的目录, 不包括/sdcard/STOCKS <==> <app_data_dir>
 	 * @param file 要插入的文件名
 	 * @param objStr 将json String作为内容写入内存储(overwrite)
 	 */
 	public StorageValue putItem(String dir, String file, String objStr) {
 		//String key = MD5.encode(CHACHE_PREFIX, uri.getBytes());
 		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
 			return null;
 		}
 		
 		FileOutputStream mOutput = null;
 		StorageValue result = null;
 		String key;
 		
 		if(dir!=null){
 			key = dir + "/" + CHACHE_PREFIX + file;
 		}else{
 			key = CHACHE_PREFIX + file;
 		}
 		
 
 		
 		try {
 			//Internal storage:
 /*			mOutput = context.openFileOutput(key, Activity.MODE_PRIVATE);
 			mOutput.write(objStr.getBytes());
 			mOutput.close();*/
 			
 			//External Storage:
 			//Create a new directory on external storage
 /*			File parentPath = new File(Environment.getExternalStorageDirectory(), dir);
 			if(!parentPath.exists()) {
 				parentPath.mkdirs();
 			}*/
 
 			//Create the file reference
 /*			File dataFile = new File(parentPath, file);*/
 			File dataFile= new File(app_data_dir + "/"  + key);
 			if(!dataFile.getParentFile().exists()) {
 				dataFile.getParentFile().mkdirs();
 			}
 
 
 			mOutput = new FileOutputStream(dataFile, false);
 			
 			//mOutput.write(ZipUtil.compress(objStr).getBytes());
 			mOutput.write(objStr.getBytes());
 			mOutput.close();
 	
 			result = cache.put(key, new StorageValue(key, objStr.getBytes().length));
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param dir 要插入的文件上级目录
 	 * @param file 要插入的文件名
 	 * @param object json Object
 	 * @param cls json对象类型
 	 */
 	public StorageValue putItem(String dir, String file, Object object, Class<?> cls) {
 
 		String jsonStr = null;
 		jsonStr = gson.toJson(object, cls);
 		if (jsonStr != null) {
 			return putItem(dir, file, jsonStr);
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * 
 	 * @param dir 要删除的文件上级目录
 	 * @param file 要删除的文件名
 	 */
 	public void deleteItem(String dir, String file) {
 		String key = null;
 		if(dir!=null){
 			key = dir + "/" + CHACHE_PREFIX + file;
 		}else{
 			key = CHACHE_PREFIX + file;
 		}
 		//String key = MD5.encode(CHACHE_PREFIX, uri.getBytes());
 		if (cache.containsKey(key)) {
 			cache.remove(key);
 			//context.deleteFile(key);
 			File dataFile= new File(app_data_dir + "/"  + key);
 			if(dataFile.isFile()){
 				dataFile.delete();
 			}
 
 			
 		}
 	}
 
 	public void cleanAll() {
 		Iterator<Entry<String, StorageValue>> iter = cache.entrySet()
 				.iterator();
 
 		while (iter.hasNext()) {
 			Map.Entry<String, StorageValue> entry = (Map.Entry<String, StorageValue>) iter
 					.next();
 			String key = entry.getKey();
 			//context.deleteFile(key);
 			File dataFile= new File(app_data_dir + "/"  + key);
 			if(dataFile.isFile()){
 				dataFile.delete();
 			}
 		}
 
 		cache.clear();
 
 	}
 
 }
