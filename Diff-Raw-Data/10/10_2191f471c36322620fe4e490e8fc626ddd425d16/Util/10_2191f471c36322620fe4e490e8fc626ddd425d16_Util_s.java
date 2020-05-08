 package com.quanleimu.util;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.security.MessageDigest;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.entity.StringEntity;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.Bitmap.Config;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo.State;
 import android.os.Environment;
 import android.os.Message;
 import android.provider.Settings.Secure;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.util.Pair;
 import android.view.Display;
 
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.entity.UserBean;
 import com.quanleimu.message.BxMessageCenter;
 import com.quanleimu.message.IBxNotificationNames;
 public class Util {
 	private static String[] keys;
 	private static String[] values;
 	
 	public static String qq_access_key="";
 	public static String qq_access_secret="";
 	
 	private static String currentUserId;
 	/**
 	 * 
 	 * @author henry_young
 	 * @throws IOException
 	 * 
 	 * @保存方式：Stream 数据流方式
 	 * 
 	 * writeUTF(String str); 但是用Data包装后就会支持。
 	 * 
 	 * @操作模式: Context.MODE_PRIVATE：新内容覆盖原内容
 	 * 
 	 *        Context.MODE_APPEND：新内容追加到原内容后
 	 * 
 	 *        Context.MODE_WORLD_READABLE：允许其他应用程序读取
 	 * 
 	 *        Context.MODE_WORLD_WRITEABLE：允许其他应用程序写入，会覆盖原数据。
 	 */
 	//TODO
 	public static List<String> getFileList() {
 		return new ArrayList<String>();
 	}
 	
 	//数据保存SD卡
 	public static String saveDataToSdCard(String path, String file,
 			Object object) {
 		String res = null;
 		FileOutputStream fos = null;
 		ObjectOutputStream oos = null;
 		if (Environment.getExternalStorageState() != null) {
 			try {
 				File p = new File("/sdcard/" + path); // 创建目录
 				File f = new File("/sdcard/" + path + "/" + file + ".txt"); // 创建文件
 				if (!p.exists()) {
 					p.mkdir();
 				}
 				if (!f.exists()) {
 					f.createNewFile();
 				}
 				fos = new FileOutputStream(f, true);
 				oos = new ObjectOutputStream(fos);
 				oos.writeObject(object);
 			} catch (FileNotFoundException e) {
 				res = "没有找到文件";
 				e.printStackTrace();
 			} catch (IOException e) {
 				res = "没有数据";
 				e.printStackTrace();
 			} finally {
 				try {
 					if(null != oos){
 						oos.close();
 					}
 					if(null != fos){
 						fos.close();
 					}
 					res = "保存成功";
 				} catch (IOException e) {
 					res = "没有数据";
 					e.printStackTrace();
 				}
 			}
 		}
 		return res;
 	}
 
 	//从SD卡读取数据
 	public static Object loadDataFromSdCard(String path, String file) {
 		Object obj = null;
 		FileInputStream fis = null;
 		ObjectInputStream ois = null;
 		if (Environment.getExternalStorageState() != null) {
 			try {
 				fis = new FileInputStream("/sdcard/" + path + "/" + file
 						+ ".txt");
 				ois = new ObjectInputStream(fis);
 				obj = ois.readObject();
 			} catch (FileNotFoundException e) {
 				obj = null;
 				e.printStackTrace();
 			} catch (IOException e) {
 				obj = null;
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				obj = null;
 				e.printStackTrace();
 			} finally {
 				try {
 					if(null != ois){
 						ois.close();
 					}
 					if(null != fis){
 						fis.close();
 					}
 				} catch (Exception e) {
 					obj = null;
 					e.printStackTrace();
 				}
 			}
 		}
 		return obj;
 	}
 
 	//将数据保存到手机内存中
 	public static String saveDataToLocate(Context context, String file,
 			Object object) {
 		if(file != null && !file.equals("") && file.charAt(0) != '_'){
 			file = "_" + file;
 		}
 		String res = null;
 		FileOutputStream fos = null;
 		ObjectOutputStream oos = null;
 		try {
 			fos = context.openFileOutput(file, Activity.MODE_PRIVATE);
 			oos = new ObjectOutputStream(fos);
 			oos.writeObject(object);
 		} catch (FileNotFoundException e) {
 			res = "没有找到文件";
 			e.printStackTrace();
 		} catch (IOException e) {
 			res = "没有数据";
 			e.printStackTrace();
 		} catch(NullPointerException e){
 			res = "null pointer";
 			e.printStackTrace();
 		} finally {
 			try {
 				if(null != oos){
 					oos.close();
 				}
 				if(null != fos){
 					fos.close();
 				}
 				res = "保存成功";
 			} catch (IOException e) {
 				res = "没有数据";
 				e.printStackTrace();
 			}
 		}
 		return res;
 	}
 	
 	/**
 	 * Return files under the specified dir.
 	 * 
 	 * @param context
 	 * @param dir
 	 * @return
 	 */
 	public static List<String> listFiles(Context context, String dir)
 	{
 		List<String> list = new ArrayList<String>();
 		String dirPath = context.getFilesDir().getAbsolutePath();
 		dirPath  = dir.startsWith(File.separator) ? dirPath + dir : dirPath + File.separator + dir;
 		
 		File dirF = new File(dirPath);
 		if (dirF.exists() && dirF.isDirectory())
 		{
 			File[] fs = dirF.listFiles();
 			for (File f : fs)
 			{
 				list.add(f.getAbsolutePath());
 			}
 		}
 		
 		return list;
 	}
 	
 	public static byte[] loadData(String absolutePath)
 	{
 
 		if (absolutePath == null)
 		{
 			return null;
 		}
 		
 		File f = new File(absolutePath);
 		if (!f.exists() || f.isDirectory())
 		{
 			return null;
 		}
 		
 		byte[] data = null;
 		FileInputStream ins = null;
 		try
 		{
 			ins = new FileInputStream(f);
 			data = new byte[ins.available()];
 			ins.read(data);
 			
 			return data;
 		}
 		catch(Throwable t)
 		{
 			
 		}
 		finally
 		{
 			try
 			{
 				if (ins != null)
 				{
 					ins.close();
 				}
 			}
 			catch(Throwable t)
 			{
 				//Ignor.
 			}
 			
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Load serializable file from specified path.
 	 * 
 	 * @param absolutePath
 	 * @return
 	 */
 	public static Object loadSerializable(String absolutePath)
 	{
 		if (absolutePath == null)
 		{
 			return null;
 		}
 		
 		File f = new File(absolutePath);
 		if (!f.exists() || f.isDirectory())
 		{
 			return null;
 		}
 		
 		ObjectInputStream ins = null;
 		try
 		{
 			ins = new ObjectInputStream(new FileInputStream(f));
 			return ins.readObject();
 		}
 		catch(Throwable t)
 		{
 			
 		}
 		finally
 		{
 			try
 			{
 				if (ins != null)
 				{
 					ins.close();
 				}
 			}
 			catch(Throwable t)
 			{
 				//Ignor.
 			}
 			
 		}
 		
 		return null;
 		
 	}
 	
 	public static String saveDataToFile(Context context, String dir, String file, byte[] data)
 	{
 		if (file == null || data == null || data.length == 0 || context == null)
 		{
 			return null;
 		}
 		
 		String dirPath = context.getFilesDir().getAbsolutePath();
 		dirPath  = dir.startsWith(File.separator) ? dirPath + dir : dirPath + File.separator + dir;
 		
 		boolean succed = new File(dirPath).mkdirs();
 		if (!succed)
 		{
 			return null;
 		}
 		
 		
 		String filePath = dirPath.endsWith(File.separator) ? dirPath + file : dirPath + File.separator + file; 
 		FileOutputStream os =null;
 		try
 		{
 			os = new FileOutputStream(new File(filePath), true);
 			os.write(data);
 			os.flush();
 			os.close();
 			
 			return filePath;
 		}
 		catch(Throwable t)
 		{
 		}
 		finally
 		{
 			if (os != null)
 			{
 				try
 				{
 					os.flush();
 					os.close();
 				}
 				catch(Throwable t)
 				{
 					//Ignor.
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Save serializable file to speficied path.
 	 * 
 	 * @param context
 	 * @param dir
 	 * @param file
 	 * @param obj
 	 * @return
 	 */
 	public static String saveSerializableToPath(Context context, String dir, String file, Serializable obj)
 	{
 		if (file == null || obj == null || context == null)
 		{
 			return null;
 		}
 		
 		String dirPath = context.getFilesDir().getAbsolutePath();
 		dirPath  = dir.startsWith(File.separator) ? dirPath + dir : dirPath + File.separator + dir;
 		
 		boolean succed = new File(dirPath).mkdirs();
 		if (!succed)
 		{
 			return null;
 		}
 		
 		
 		String filePath = dirPath.endsWith(File.separator) ? dirPath + file : dirPath + File.separator + file; 
 		ObjectOutputStream os =null;
 		try
 		{
 			os = new ObjectOutputStream(new FileOutputStream(new File(filePath)));
 			os.writeObject(obj);
 			os.flush();
 			os.close();
 			
 			return filePath;
 		}
 		catch(Throwable t)
 		{
 		}
 		finally
 		{
 			if (os != null)
 			{
 				try
 				{
 					os.flush();
 					os.close();
 				}
 				catch(Throwable t)
 				{
 					//Ignor.
 				}
 			}
 		}
 		
 		return null;
 		
 	}
 	
 	public static void clearFile(String absolutePath) {
 		new File(absolutePath).delete();
 	}
 	
 	public static void clearData(Context context, String file){
 		if(file != null && !file.equals("") && file.charAt(0) != '_'){
 			file = "_" + file;
 		}
 		
 		context.deleteFile(file);
 	}
 	
 	/**
 	 * 保存json数据和timstamp(秒数)至手机内存。会检查json的完整性。
 	 * @param context
 	 * @param file     
 	 * @param json      json String
 	 * @param timestamp 秒数
 	 * @return
 	 */
 	public static String saveJsonAndTimestampToLocate(Context context, String file, String json, long timestamp) {
 		try { // check the data validate.
 			new JSONObject(json);
 		} catch (JSONException e) {
 			return "data invalid";
 		}
 		String s = String.format("%d,%s", timestamp, json);
 		return saveDataToLocate(context, file, s);
 	}
 	
 	/**
 	 * 将Json数据(String)从手机内存中读出来, 同时将timstamp返回(单位秒)。
 	 * @param context
 	 * @param filename
 	 * @return Pair(LastModifiedTimeStamp, String): if file not exist, LastModifiedTimeStamp = 0;  
 	 */
 	public static Pair<Long, String> loadJsonAndTimestampFromLocate(Context context, String filename) {
 		if(filename != null && !filename.equals("") && filename.charAt(0) != '_'){
 			filename = "_" + filename;
 		}
 		
 //		File file = context.getFileStreamPath(filename);
 //		long timestamp = file.lastModified()/1000;
 		
 		String s = (String) Util.loadDataFromLocate(context, filename);
 		
 		if (s != null && s.length() > 0) {
 			int index = s.indexOf(',');
 			if (index != -1) {
				long timestamp = Long.parseLong(s.substring(0, index));
				String data = s.substring(index+1);
				return new Pair<Long, String>(timestamp, data);
 			}
 		}
 		return new Pair<Long, String>(0l, "");
 	}
 	
 	/**
 	 * 
 	 * @param context
 	 * @param filename
 	 * @return
 	 */
 	public static Pair<Long, String> loadDataAndTimestampFromAssets(Context context, String filename) {
 		InputStream is = null;
 		Pair<Long, String> pair = new Pair<Long, String>(0l, "");
 		try {
 			is = context.getAssets().open(filename);
 			byte[] b = new byte[is.available()];
 			is.read(b);
 			String s = new String(b);
 			if (s != null && s.length() > 0) {
 				int index = s.indexOf(',');
 				if (index != -1) {
 					long timestamp = Long.parseLong(s.substring(0, index));
 					String data = s.substring(index+1);
 					pair = new Pair<Long, String>(timestamp, data);
 				}
 			}
 		}catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (is != null)
 					is.close();
 			} catch (IOException e) {
 			}
 		}
 		return pair;		
 	}
 
 	//将数据从手机内存中读出来
 	public static Object loadDataFromLocate(Context context,String file) {
 		if(file != null && !file.equals("") && file.charAt(0) != '_'){
 			file = "_" + file;
 		}
 //		File testfile = new File(file); 
 //		if(!testfile.exists()){
 //			return null;
 //		}
 
 		Object obj = null;
 		FileInputStream fis = null;
 		ObjectInputStream ois = null;
 		try {
 			fis = context.openFileInput(file);
 			ois = new ObjectInputStream(fis);
 			obj = ois.readObject();
 			
 		} catch (FileNotFoundException e) {
 			obj = null;
 			e.printStackTrace();
 		} catch (IOException e) {
 			obj = null;
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			obj = null;
 			e.printStackTrace();
 		}catch (Exception e) {
 			obj = null;
 			e.printStackTrace();
 		} finally {
 			try {
 				if(null != ois){
 					ois.close();
 				}
 				if(null != fis){
 					fis.close();
 				}
 			} catch (Exception e) {
 				obj = null;
 				e.printStackTrace();
 			}
 		}
 		return obj;
 	}
 	
 	public String[] getKeys() {
 		return keys;
 	}
 	public static void setKeys(Object...args) {
 		keys = new String[args.length];
 		for(int i=0;i<args.length;i++){
 			keys[i] = (String) args[i];
 		}
 	}
 	public static String[] getValues() {
 		return values;
 	}
 	public static void setValues(Object...args) {
 		values = new String[args.length];
 		for(int i=0;i<args.length;i++){
 			values[i] = (String) args[i];
 		}
 	}
 	
 	//����vkey1
 	public static String getStr(){
 		String vkey  = "";
 		//vkey = "api_key=" + Const.API_KEY;
 		for(int i=0;i<keys.length;i++){
 			vkey =vkey + "&" + keys[i] +"="+ values[i];
 		}
 		vkey = vkey.substring(1);
 		String vkey1 = vkey;
 		vkey = vkey + Const.API_SECRET;
 		vkey1 = vkey1 + "&access_token=" + MD5(vkey);
 		
 		return vkey1;	
 	}
 
 	//MD5加密
 	public  static String MD5(String inStr) {
 		MessageDigest md5 = null;
 		try {
 			md5 = MessageDigest.getInstance("MD5");
 		} catch (Exception e) {
 			e.printStackTrace();
 			return "";
 		}
 		char[] charArray = inStr.toCharArray();
 		byte[] byteArray = new byte[charArray.length];
 
 		for (int i = 0; i < charArray.length; i++)
 			byteArray[i] = (byte) charArray[i];
 		byte[] md5Bytes = md5.digest(byteArray);
 		StringBuffer hexValue = new StringBuffer();
 		for (int i = 0; i < md5Bytes.length; i++) {
 			int val = ((int) md5Bytes[i]) & 0xff;
 			if (val < 16)
 				hexValue.append("0");
 			hexValue.append(Integer.toHexString(val));
 		}
 		return hexValue.toString();
 	}
 	
 	public static String requestUserProfile(String usrId){
 		String apiName = "user_profile";
 		ArrayList<String> list = new ArrayList<String>();
 		 
 		list.add("rt=1");
 		list.add("userId=" + usrId);
 		
 		String url = Communication.getApiUrl(apiName, list);
 		try {
 			return Communication.getDataByUrl(url, true);
 		} catch (UnsupportedEncodingException e) {
 			Message msg2 = Message.obtain();
 			msg2.what = ErrorHandler.ERROR_SERVICE_UNAVAILABLE;
 			QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 		} catch (IOException e) {
 			Message msg2 = Message.obtain();
 			msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
 			QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 		} catch (Communication.BXHttpException e) {
 			Message msg2 = Message.obtain();
 			msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
 			QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 		}
 		return null;
 	}
 	
 	
 	
 	
 	
 	public static String getJsonDataFromURLByPost(String path,String params) throws SocketTimeoutException, UnknownHostException {
 		//����һ��URL����
 //		HttpURLConnection urlCon = null;
 		BufferedReader reader = null ;
 		String str = "";
 		try {
 //			URL url = new URL(path);
 //			urlCon = (HttpURLConnection) url.openConnection();
 //			urlCon.setDoOutput(true);
 //			urlCon.setDoInput(true);
 //			urlCon.setRequestMethod("POST");
 //			urlCon.setUseCaches(false);
 //			urlCon.setInstanceFollowRedirects(true);
 //			urlCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 //			urlCon.connect();
 			
 			
 			HttpClient httpClient = NetworkProtocols.getInstance().getHttpClient();
             
             HttpPost httpPost = new HttpPost(path); 
             
             ByteArrayEntity stringEntity = new ByteArrayEntity(params.getBytes());
             stringEntity.setContentType("application/x-www-form-urlencoded");
             httpPost.setEntity(stringEntity);
             
             HttpResponse response = httpClient.execute(httpPost);
             
 //			DataOutputStream out = new DataOutputStream(urlCon.getOutputStream());
 //			
 //			out.writeBytes(params);
 //			
 //			out.flush();
 //			out.close();
 			reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 			String temp = "";
 			while((temp=reader.readLine())!=null){
 				str += (temp + "\n");
 			}
 			
 			httpClient.getConnectionManager().shutdown();
 		}catch(SocketTimeoutException ste){
 			Log.e("��ʱ", "��ʱ��");
 				throw ste; 
 		}catch(UnknownHostException h){
 			Log.e("��·", "���粻��ͨ");
 			throw h;
 		
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if(reader!=null)
 					reader.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 //			urlCon.disconnect();
 		}
 		//Log.d("json", "activityjson--->" + str);
 		return str;
 	}
 	
 	//post�ύ����
 	public static String sendString(String str, String urlString) throws IOException{
 		DataInputStream dis = null;
 		String readUrl = "";
 		try {
 //			URL url = new URL(urlString);
 //			HttpURLConnection urlConn = (HttpURLConnection) url
 //					.openConnection();
 //			urlConn.setRequestMethod("POST");
 //			urlConn.setDoOutput(true);
 //			urlConn.setDoInput(true);
 //			OutputStream os = urlConn.getOutputStream();
 //			os.write(str.getBytes());
 			
 			
 			HttpClient httpClient = NetworkProtocols.getInstance().getHttpClient();
             
             HttpPost httpPost = new HttpPost(urlString); 
             
             StringEntity stringEntity = new StringEntity(str, "UTF-8");
             httpPost.setEntity(stringEntity);
             
             HttpResponse response = httpClient.execute(httpPost);
             
 			dis = new DataInputStream(response.getEntity().getContent());
 			readUrl = dis.readLine();
 			dis.close();
 //			os.close();
 			
 			httpClient.getConnectionManager().shutdown();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		return readUrl;
 	} 
 	
 	public static String saveBitmapToSdCard(String path,String name,Bitmap bitmap) {
 		String res = null;
 		FileOutputStream fos = null; 
 		if (Environment.getExternalStorageState() != null) {
 			try {
 				File p = new File("/sdcard/" + "quanleimu"); // 创建目录
 				File s = new File("/sdcard/" + "quanleimu" + "/" + path); // 创建目录
 				File f = new File("/sdcard/" + "quanleimu" + "/" + path + "/" + name + ".png"); // 创建文件
 				if (!p.exists()) {
 					p.mkdir();
 				}
 				if (!s.exists()) {
 					s.mkdir();
 				}
 				if (!f.exists()) {
 					f.createNewFile();
 				}
 				fos = new FileOutputStream(f);
 				
 				bitmap.compress(CompressFormat.JPEG, 100, fos);
 				fos.close();
 				res = f.getAbsolutePath();
 			} catch (FileNotFoundException e) {
 				res = "没有找到文件";
 				e.printStackTrace();
 			} catch (Exception e) {
 				res = "SD卡未安装";
 				e.printStackTrace();
 			}
 		}else{
 			res = "无SD卡";
 		}
 		return res;
 	}
 	
 	public static List<Bitmap> loadBitmapFromSdCard(String path,String name) {
 		List<Bitmap> b = new ArrayList<Bitmap>();
 		File file = new File("/sdcard/"+"quanleimu/"+path);
 		if (file != null) {
 			File[] files = file.listFiles();
 			if (files != null) {
 				for (File f : files) {
 					if(f.getAbsolutePath().contains(name)){
 					    BitmapFactory.Options o =  new BitmapFactory.Options();
 	                    o.inPurgeable = true;
 						b.add(BitmapFactory.decodeFile(f.getPath(), o));
 					}
 				}
 			}
 		}
 		b = replaceList(b);
 		return b;
 	}
 	
 	public static List<Bitmap> replaceList(List<Bitmap> bit) {
 		List<Bitmap> b = new ArrayList<Bitmap>();
 		for (int i = bit.size() - 1; i > -1; i--) {
 			b.add(bit.get(i));
 		}
 		return b;
 	}
 	
 	//下载图片
 	public static Bitmap getImage(String strURL)throws OutOfMemoryError{	
 		//Log.d("img", strURL);
 				Bitmap img = null;
 				URLConnection conn = null;
 				try {
 					URL url = new URL(strURL);
 					conn = (URLConnection) url.openConnection();
 					BitmapFactory.Options o =  new BitmapFactory.Options();
 					o.inPurgeable = true;
 					//Log.e("o", o.toString());
 					img = BitmapFactory.decodeStream(conn.getInputStream(), null, o);
 				}
 				catch (Exception e) { 
 					img = null;
 					e.printStackTrace();
 					//conn.disconnect();
 				}
 		return img;
 	}
 	
 	//将bitmap转成流存到file里面
 	public static void saveImage2File(Context context,Bitmap bmp,String fileName)
 	{
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
 		byte[] file = bos.toByteArray();
 		FileOutputStream fos = null;
 		try {
 			fos = context.openFileOutput(fileName, Activity.MODE_PRIVATE);
 			fos.write(file);
 			fos.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	//将流转成bitmap里面
 	public static Bitmap getBitmapFromInputstream(Context context,String fileName)
 	{
 		Bitmap bmp = null;
 		FileInputStream fis = null;
 		byte[] aa = null;
 		try {
 			fis = context.openFileInput(fileName);
 			
 			aa = new byte[fis.available()];
 			fis.read(aa);
 		} catch (FileNotFoundException e) {
 			fis = null;
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		if(fis == null)
 		{
 			bmp = null;
 		}
 		else
 		{
 //			bmp = BitmapFactory.decodeStream(fis);
 		    BitmapFactory.Options o =  new BitmapFactory.Options();
             o.inPurgeable = true;
 			bmp = BitmapFactory.decodeByteArray(aa, 0, aa.length, o);
 		}
 		return bmp;
 	}
 	
 	
 	//手机分辨率宽
 	public static int getWidth(Activity activity){
 		Display display = activity.getWindowManager().getDefaultDisplay();
 		int width = display.getWidth();
 		int a = 0;
 		if(width == 240)
 		{
 			a = 1;
 		}
 		else if(width == 320)
 		{
 			a = 2;
 		}
 		else if(width == 480)
 		{
 			a = 3;
 		}
 		else if(width == 540)
 		{
 			a = 4;
 		}
 		else if(width == 640)
 		{
 			a = 5;
 		}else{
 			a = 5;
 		}
 		
 		return a;
 	}
 	
 	public static int getWidthByContext(Context context){
 		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
 		int width = display.getWidth();
 //		int a = 0;
 //		if(width == 240)
 //		{
 //			a = 1;
 //		}
 //		else if(width == 320)
 //		{
 //			a = 2;
 //		}
 //		else if(width == 480)
 //		{
 //			a = 3;
 //		}
 //		else if(width == 540)
 //		{
 //			a = 4;
 //		}
 //		else if(width == 640)
 //		{
 //			a = 5;
 //		}
 		
 		return width;
 	}
 	
 	public static int getHeightByContext(Context context){
 		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
 		int height = display.getHeight();
 //		int a = 0;
 //		if(width == 240)
 //		{
 //			a = 1;
 //		}
 //		else if(width == 320)
 //		{
 //			a = 2;
 //		}
 //		else if(width == 480)
 //		{
 //			a = 3;
 //		}
 //		else if(width == 540)
 //		{
 //			a = 4;
 //		}
 //		else if(width == 640)
 //		{
 //			a = 5;
 //		}
 		
 		return height;
 	}
 	
 
 	//获得手机屏幕焦点
 	public static Point getSrccenPoint(Activity activity){
 		Display display = activity.getWindowManager().getDefaultDisplay();
 		return new Point(display.getWidth(), display.getHeight());
 	}
 	
     public static Bitmap newBitmap(Bitmap b, int w, int h)
     {
         float scaleWidth = 0;
         float scaleHeight = h;
         int width = b.getWidth();
         int height = b.getHeight();
         
         if (w == h)
         {
             int minValue = Math.min(b.getWidth(), b.getHeight());
             if (minValue >= w)
             {
                 float ratio = (float) w / (float) minValue;
                 scaleWidth = ratio;
                 scaleHeight = ratio;
             }
             else
             {
                 minValue = Math.max(b.getWidth(), b.getHeight());
                 float ratio = (float) w / (float) minValue;
                 scaleWidth = ratio;
                 scaleHeight = ratio;
             }
         }
         else
         {
             scaleWidth = ((float) w) / width;
             scaleHeight = ((float) h) / height;
         }
         
         Matrix matrix = new Matrix();
         matrix.postScale(scaleWidth, scaleHeight);// ����
         return Bitmap.createBitmap(b, 0, 0, width, height, matrix, true);
     }
 	
 	//图片旋转
 	public static Bitmap rotate(Bitmap b, int degrees) {
 		if (degrees != 0 && b != null) {
 			Matrix m = new Matrix();
 			m.setRotate(degrees);
 			try {
 				Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b
 						.getHeight(), m, true);
 				if (b != b2) {
 					b = b2;
 				}
 			} catch (OutOfMemoryError ex) {
 				ex.printStackTrace();
 			}
 		}
 		return b;
 	}
 	
 	//һ����ת
 	public static List<Bitmap> rotateList(List<Bitmap> bitmaps) {
 		List<Bitmap> bits = new ArrayList<Bitmap>();
 		for(Bitmap b : bitmaps){
 			bits.add(Util.rotate(b, -90));
 		}
 		return bits;
 	}
 	public static List<Bitmap> newBitmapList(List<Bitmap> bitmaps,Activity activity) {
 		List<Bitmap> bits = new ArrayList<Bitmap>();
 		for(Bitmap b : bitmaps){
 			if(Util.getWidth(activity)==1){
 				bits.add(Util.newBitmap(b, 90, 90));
 			}else if(Util.getWidth(activity)== 2){
 				bits.add(Util.newBitmap(b, 135, 135));
 			}
 			else if(Util.getWidth(activity)== 3){
 				bits.add(Util.newBitmap(b, 60, 60));
 			}
 			else if(Util.getWidth(activity)== 4){
 				bits.add(Util.newBitmap(b, 160, 160));
 			}
 			else if(Util.getWidth(activity)== 5){
 				bits.add(Util.newBitmap(b, 180, 180));
 			}
 			
 		}
 		return bits;
 	}
 	
 	public static Bitmap scaleBitmap(Bitmap src, int outputX, int outputY, int leftMask, int topMask, int rightMask, int bottomMask){
 		Bitmap toRet = null;
 		BitmapFactory.Options o =  new BitmapFactory.Options();
 		o.inPurgeable = true;
 		if(src.getWidth() > outputX){
 			Bitmap scaledBmp = newBitmap(src, outputX, (int)((((float)outputX) / src.getWidth()) * src.getHeight()));
 			if(outputY <= scaledBmp.getHeight()) return scaledBmp;
 	
 		Bitmap lineBk = Bitmap.createBitmap(outputX, 1, Config.ARGB_4444);
 			Canvas canvas = new Canvas(lineBk);
 			Rect srcRc = new Rect();
 			srcRc.left = 0;
 			srcRc.top = topMask;
 			srcRc.right = outputX;
 			srcRc.bottom = topMask + 1;
 			
 			Rect destRc = new Rect();
 			destRc.left = 0;
 			destRc.top = 0;
 			destRc.right = outputX;
 			destRc.bottom = 1;
 			canvas.drawBitmap(scaledBmp, srcRc, destRc, new Paint());
 			
 		toRet = Bitmap.createBitmap(outputX, outputY, Config.ARGB_4444);
 			canvas = new Canvas(toRet);
 			srcRc.left = 0;
 			srcRc.top = 0;
 			srcRc.right = outputX;
 			srcRc.bottom = topMask;
 			
 			destRc.left = srcRc.left;
 			destRc.right = srcRc.right;
 			destRc.top = srcRc.top;
 			destRc.bottom = srcRc.bottom;
 			canvas.drawBitmap(scaledBmp, srcRc, destRc, new Paint());
 			srcRc.top = topMask;
 			srcRc.bottom = topMask + 1;
 			for(int i = 0; i < outputY - topMask - bottomMask; ++ i){
 				destRc.top = i + topMask;
 				destRc.bottom = i + topMask + 1;
 				canvas.drawBitmap(scaledBmp, srcRc, destRc, new Paint());
 			}
 			srcRc.top = scaledBmp.getHeight() - bottomMask;
 			srcRc.bottom = scaledBmp.getHeight();
 			destRc.top = outputY - bottomMask;
 			destRc.bottom = outputY;
 			canvas.drawBitmap(scaledBmp, srcRc, destRc, new Paint());
 			lineBk.recycle();
 			scaledBmp.recycle();
 		}
 		else
 		{
 			Bitmap scaledBmp = newBitmap(src, (int)((((float)outputY) / src.getHeight()) * src.getWidth()), outputY);
 	
 			Bitmap lineBk = Bitmap.createBitmap(1, outputY, Config.ARGB_4444);
 			Canvas canvas = new Canvas(lineBk);
 			Rect srcRc = new Rect();
 			srcRc.left = leftMask;
 			srcRc.top = 0;
 			srcRc.right = leftMask + 1;
 			srcRc.bottom = scaledBmp.getHeight();
 			
 			Rect destRc = new Rect();
 			destRc.left = 0;
 			destRc.top = 0;
 			destRc.right = 1;
 			destRc.bottom = outputY;
 			canvas.drawBitmap(scaledBmp, srcRc, destRc, new Paint());
 			
 			toRet = Bitmap.createBitmap(outputX, outputY, Config.ARGB_4444);
 			canvas = new Canvas(toRet);
 			srcRc.left = 0;
 			srcRc.top = 0;
 			srcRc.right = leftMask;
 			srcRc.bottom = scaledBmp.getHeight();
 			
 			destRc.left = 0;
 			destRc.right = leftMask;
 			destRc.top = 0;
 			destRc.bottom = outputY;
 			canvas.drawBitmap(scaledBmp, srcRc, destRc, new Paint());
 			srcRc.left = leftMask;
 			srcRc.right = leftMask + 1;
 			for(int i = 0; i < outputX - leftMask - rightMask; ++ i){
 				destRc.left = i + leftMask;
 				destRc.right = i + leftMask + 1;
 				canvas.drawBitmap(scaledBmp, srcRc, destRc, new Paint());
 			}
 			srcRc.left = scaledBmp.getWidth() - bottomMask;
 			srcRc.right = scaledBmp.getWidth();
 			destRc.left = outputX - rightMask;
 			destRc.right = outputX;
 			canvas.drawBitmap(scaledBmp, srcRc, destRc, new Paint());
 			lineBk.recycle();
 			scaledBmp.recycle();
 		}
 		return toRet;
 	}
 	
 	//保存数据手机内存
 	public static void saveToData(Context ctx , String title,String key,String value){
 		SharedPreferences.Editor se = ctx.getSharedPreferences(title, Context.MODE_WORLD_WRITEABLE).edit();
 		se.putString(key, value);
 		se.commit();
 	}
 	
 	//获取数据从手机内从
 	public static String getFromData(Context ctx , String title,String key){
 		SharedPreferences se = ctx.getSharedPreferences(title, Context.MODE_WORLD_READABLE);
 		if(se==null){
 			return null;
 		}
 		String value = se.getString(key, "");
 		if(value.equals("")){
 			return null;
 		}else{
 			return value;
 		}
 	}
 	
 	
 	/**
 	 * recycle all the bitmaps in the list
 	 * @param listBm
 	 */
 	public static void recycle(List<Bitmap> listBm){
 		for(int i=0;i<listBm.size();i++){
 			if(listBm!=null&&listBm.get(i)!=null&&!listBm.get(i).isRecycled()){
 				listBm.get(i).recycle();
 			}
 		}
 	}
 	
 	//根据经纬度获取地址
 	public static String GetAddr(double latitude, double longitude) {  
 		String addr = "";  
 		
 		String url = String.format(  
 		  "http://ditu.google.cn/maps/geo?output=csv&key=abcdef&q=%s,%s",  
 		  latitude, longitude);  
 		URL myURL = null;  
 		URLConnection httpsConn = null;  
 		try {  
 			myURL = new URL(url);  
 		} catch (MalformedURLException e) {  
 			e.printStackTrace();  
 		 return null;
 		}  
 		try {  
 		 httpsConn = (URLConnection) myURL.openConnection();  
 		 if (httpsConn != null) {  
 		  InputStreamReader insr = new InputStreamReader(  
 		    httpsConn.getInputStream(), "UTF-8");  
 		  BufferedReader br = new BufferedReader(insr);  
 		  String data = null;  
 		  if ((data = br.readLine()) != null) {  
 		   String[] retList = data.split(",");  
 		   if (retList.length > 2 && ("200".equals(retList[0]))) {  
 		    addr = retList[2];  
 		    addr = addr.replace("/", "");  
 		   } else {  
 		    addr = "";  
 		   }  
 		  }  
 		  insr.close();  
 		 }  
 		} catch (IOException e) {  
 		 e.printStackTrace();  
 		 return null;  
 		}  
 		return addr;  
 	}
 	
 	//GSM和CDMA手机定位
 //	private boolean goGsm = true;
 //	private boolean running = true;
 //	public class NewThread extends Thread{
 //		private long time = 0;
 //		public void run(){
 //			time = System.currentTimeMillis();
 //			while(running){
 //				if(System.currentTimeMillis() - time >= 10000){
 //					break;
 //				}
 //			}
 //			if(running){
 //				goGsm = false;
 //				geoLat = 31.1984;
 //		      	geoLon = 121.436475;
 //		      	myHandler.sendEmptyMessage(1);
 //			}
 //		}
 //	}
 //	public void ddd(){
 //        Thread thread = new Thread(){
 //                public void run(){
 //                        try {
 //                                DefaultHttpClient client = new DefaultHttpClient();
 //                                HttpPost post = new HttpPost("http://www.google.com/loc/json");//http://www.google.com/glm/mmap");//
 //                                
 //                                JSONObject holder = new JSONObject();
 //                                holder.put("version", "1.1.0");
 //                                holder.put("request_address", true);
 //                                holder.put("address_language", "en-us");
 //
 //                                JSONObject data ;
 //                                JSONArray array = new JSONArray();
 //                                data = new JSONObject();
 //                                data.put("cell_id", c.getCid());  // 9457, 8321,8243
 //                                data.put("location_area_code", c.getLac());// 28602
 //                                data.put("mobile_country_code", ss.substring(0, 3));// 208
 //                                data.put("mobile_network_code", ss.substring(3, 5));// 0
 //                                data.put("age", 0);
 ////                                
 //                                array.put(data);
 //                                for(int i =0;i<(count<2?count:2);i++){
 //                                        data = new JSONObject();
 //                                        data.put("cell_id", ci[i]);  // 9457, 8321,8243
 //                                        data.put("location_area_code", lac[i]);// 28602
 //                                        data.put("mobile_country_code", ss.substring(0, 3));// 208
 //                                        data.put("mobile_network_code", ss.substring(3, 5));// 0
 //                                        data.put("age", 0);
 //                                        array.put(data);
 //                                }
 //                                holder.put("cell_towers", array);
 ////                                
 //                                StringEntity se = new StringEntity(holder.toString());
 //                                Log.e("Location send",holder.toString());
 //                                post.setEntity(se);
 //                                HttpResponse resp = client.execute(post);
 //                                
 //                                HttpEntity entity = resp.getEntity();
 //
 //                                BufferedReader br = new BufferedReader(new InputStreamReader(entity
 //                                                .getContent()));
 //                                StringBuffer sb = new StringBuffer();
 //                                String result = br.readLine();
 //                                while (result != null) {
 //                                        Log.e("Locaiton reseive", result);
 //                                        sb.append(result);
 //                                        result = br.readLine(); 
 //                                }
 //                                result_location = sb.toString();
 //                                
 //                                
 //                                data = new JSONObject(sb.toString());
 //                                data = (JSONObject) data.get("location");
 //                                geoLat = (Double) data.get("latitude");
 //                                geoLon = (Double) data.get("longitude");
 //                                if(geoLat != 0.0 && geoLon != 0.0 && geoLat != 0 && geoLon != 0)
 //								{
 //                                	running = false;
 //                                	if(goGsm == true)
 //                                	{
 //                                		myHandler.sendEmptyMessage(0);
 //                                	}
 //								}
 //								else
 //								{
 //                                	running = false;
 //									geoLat = 31.1984;
 //							      	geoLon = 121.436475;
 //							      	if(goGsm == true)
 //                                	{
 //                                		myHandler.sendEmptyMessage(1);
 //                                	} 
 //								}
 //                        } 
 //                        catch (ClientProtocolException e) {
 //                                e.printStackTrace();
 //                        } catch (IOException e) {
 //                                e.printStackTrace();
 //                        } 
 //                        catch (JSONException e) {
 //                                e.printStackTrace();
 ////                                loc.setText("");
 //                        }
 //                }
 //        };
 //        thread.start();
 //	}
 	
 	public static String extractUrlWithoutSecret(String url){
 		if(url == null || url.equals("")) return null;
 		int index1 = url.indexOf("access_token=");
 		int index2 = -1;
 		if(index1 >= 0){
 			index2 = url.indexOf("&", index1);
 			if(index2 > index1){
 				url = url.replace(url.substring(index1, index2 + 1), "");
 			}
 		}
 		index1 = url.indexOf("timestamp=");
 		if(index1 >= 0){
 			index2 = url.indexOf("&", index1);
 			if(index2 > index1){
 				url = url.replace(url.substring(index1, index2 + 1), "");
 			}
 		}
 		index1 = url.indexOf("adIds=");
 		if(index1 >= 0){
 			index2 = url.indexOf("&", index1);
 			if(index2 > index1){
 				url = url.replace(url.substring(index1, index2 + 1), "");
 			}
 		}		
 		return url;
 	}
 
     /**
      * logout 数据清理也放在这里，外部直接调用即可
      */
     public static void logout()
 	{
         Util.clearData(QuanleimuApplication.getApplication().getApplicationContext(), "user");
         Util.clearData(QuanleimuApplication.getApplication().getApplicationContext(), "userProfile");
 		currentUserId = null;
 		
 		UserBean anonymousUser = (UserBean) Helper.loadDataFromLocate(QuanleimuApplication.getApplication().getApplicationContext(), "anonymousUser");
 		if(anonymousUser != null){
 			Helper.saveDataToLocate(QuanleimuApplication.getApplication().getApplicationContext(), "user", anonymousUser);
 		}
 		
 		BxMessageCenter.defaultMessageCenter().postNotification(IBxNotificationNames.NOTIFICATION_LOGOUT, anonymousUser);
 	}
 
     /**
      *
      * @return 返回当前 UserBean user，未登录情况下返回 null
      */
     public static UserBean getCurrentUser() {
         UserBean user = (UserBean) Util.loadDataFromLocate(QuanleimuApplication.getApplication().getApplicationContext(), "user");
         return user;
     }
     
     public static boolean isUserLogin()
     {
     	UserBean user = getCurrentUser();
     	return user != null && user.getPhone() != null && user.getPhone().length() > 0;
     }
 
 	public static String getMyId(Context context)
 	{
 		if (currentUserId != null)
 		{
 			return currentUserId;
 		}
 		
 		UserBean user = Util.getCurrentUser();
 		if (user != null)
 		{
 			currentUserId = user.getId();
 		}
 		
 		return currentUserId;
 	}
 	
 	public static String refreshAndGetMyId(Context context) {
 		UserBean user = Util.getCurrentUser();
 		if (user != null)
 		{
 			currentUserId = user.getId();
 		}
 		else
 		{
 			currentUserId = null;
 		}
 		
 		return currentUserId;
 	}
 	
 	public static boolean isPushAlreadyThere(Context ctx, String pushCode){
 		if(ctx == null) return true;
 		if(pushCode == null || pushCode.equals("")) return false;
 		Object objCode = Util.loadDataFromLocate(ctx, "pushCode");
 		if(objCode != null){
 			String code = (String)objCode;
 			try{
 				return Integer.valueOf(pushCode) <= Integer.valueOf(code);
 			}catch(Throwable e){
 				e.printStackTrace();
 				return false;
 			}
 		}
 		return false;
 	}
 	
 	public static String getVersion(Context ctx){
 		PackageManager packageManager = ctx.getPackageManager();
 		// getPackageName()是你当前类的包名，0代表是获取版本信息
 		PackageInfo packInfo;
 		try {
 			packInfo = packageManager.getPackageInfo(ctx.getPackageName(), 0);
 			return packInfo.versionName;
 		} catch (NameNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		return "";
 	}
 	
     static public String getDeviceUdid(Context context) {
         // Use the Android ID unless it's broken, in which case fallback on deviceId,
         // unless it's not available, then fallback on a random number which we store
         // to a prefs file
     	final String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
     	
         try {
         	
             if ("9774d56d682e549c".equals(androidId) || androidId == null) {
                 final String deviceId = ((TelephonyManager) context.getSystemService( Context.TELEPHONY_SERVICE )).getDeviceId();
                 String uuid = deviceId!=null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")).toString() : UUID.randomUUID().toString();
                 if(uuid.length() >= 20){
                 	uuid = uuid.substring(0, 20);
                 }
                 return uuid;
             }
             
             return androidId;
         } catch (UnsupportedEncodingException e) {
 //            throw new RuntimeException(e);
         	e.printStackTrace();
         }
         if(androidId.length() >= 20){
         	return androidId.substring(0, 20);
         }        
         return androidId;
 
     }	
     
     static public void makeupUserInfoParams(UserBean user, List<String> params){
 		if(user != null && params != null){
 			params.add("mobile=" + user.getPhone());
 			String password1 = Communication.getMD5(user.getPassword());
 			password1 += Communication.apiSecret;
 			String userToken = Communication.getMD5(password1);
 			params.add("userToken=" + userToken);
 		}    	
     }
 }
