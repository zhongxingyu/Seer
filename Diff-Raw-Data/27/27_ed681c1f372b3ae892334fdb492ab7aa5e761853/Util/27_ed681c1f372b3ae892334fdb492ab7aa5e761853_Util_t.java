 package com.joy.launcher2.util;
 
 import static android.os.Environment.MEDIA_MOUNTED;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Random;
 
 import org.apache.http.util.EncodingUtils;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.PixelFormat;
 import android.graphics.PorterDuff.Mode;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Environment;
 import android.util.Log;
 
 import com.joy.launcher2.LauncherApplication;
 import com.joy.launcher2.install.SecretlyInstallReceiver;
 
 /**
  * 工具类 提供随机数生成（数字和字符），加密相关（mod5），字符串操作（追加和替换），图片操作（生成、缩放、裁剪）这些分类方法。
  * 
  * @author hao.wang
  * 
  */
 public class Util {
 
 	private static String TAG = "Util";
 	private static String deviceid;
 	private static int sBitmapTextureWidth = 48;
 	private static int sBitmapTextureHeight = 48;
 	private static Random mRandom = new Random();
 	/**
 	 * 随机数
 	 * 
 	 * @return
 	 */
 	public static String getTS() {
 		String str = String.valueOf(mRandom.nextInt(9999));
 		return str;
 	}
 
 	/**
 	 * 获取随机字符串
 	 */
 
 	public static final String randomString(int length) {
 		if (length < 1) {
 			return null;
 		}
 		char[] numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz"
 				+ "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
 		char[] randBuffer = new char[length];
 		for (int i = 0; i < randBuffer.length; i++) {
 			randBuffer[i] = numbersAndLetters[mRandom.nextInt(71)];
 			// randBuffer[i] = numbersAndLetters[randGen.nextInt(35)];
 		}
 		return new String(randBuffer);
 	}
 
 	/**
 	 * 将字符串编码为md5格式
 	 * 
 	 * @param value
 	 * @return
 	 */
 	public static String md5Encode(String value) {
 		String tmp = null;
 		try {
 			MessageDigest md5 = MessageDigest.getInstance("MD5");
 			md5.update(value.getBytes("utf8"));
 			byte[] md = md5.digest();
 			tmp = binToHex(md);
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		return tmp;
 	}
 
 	public static String binToHex(byte[] md) {
 		StringBuffer sb = new StringBuffer("");
 		int read = 0;
 		for (int i = 0; i < md.length; i++) {
 			read = md[i];
 			if (read < 0)
 				read += 256;
 			if (read < 16)
 				sb.append("0");
 			sb.append(Integer.toHexString(read));
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * 构造String字符串
 	 * 
 	 * @param args
 	 * @return
 	 */
 	public static String buildString(String... args) {
 		StringBuffer buffer = new StringBuffer();
 		for (String arg : args) {
 			buffer.append(arg);
 		}
 		return buffer.toString();
 	}
 
 	/**
 	 * 字符串转换
 	 * 
 	 * @param content
 	 * @return
 	 */
 	public static String encodeContentForUrl(String content) {
 
 		try {
 			return (content == null ? "" : URLEncoder.encode(
 					URLEncoder.encode(content, "UTF-8"), "UTF-8"));
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		return content;
 	}
 	/**
 	 * 字符串转换
 	 * 
 	 * @param content
 	 * @return
 	 */
 	public static String decodeContentFromUrl(String content) {
 
 		try {
 			return (content == null ? "" : URLDecoder.decode(
 					URLDecoder.decode(content, "UTF-8"), "UTF-8"));
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		return content;
 	}
 	/**
 	 * 字符串转换
 	 * 
 	 * @param content
 	 * @return
 	 */
 	public static String encodeDevice(String value) {
 		StringBuilder sb = new StringBuilder(32);
 		try {
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			byte[] array = md.digest(value.getBytes("utf-8"));
 
 			for (int i = 0; i < array.length; i++) {
 				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
 						.toUpperCase().substring(1, 3));
 			}
 		} catch (Exception e) {
 			return null;
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * 根据url返回文件名
 	 * 
 	 * @param content
 	 * @return
 	 */
 	public static String getFileNameByUrl(String url) {
 		if (url == null || "".equals(url.trim())) {
 			return null;
 		}
 		return url.substring(url.lastIndexOf("/") + 1);
 	}
 
 	/**
 	 * 将流转换成字节数组
 	 * 
 	 * @param is
 	 * @return
 	 */
 	public static byte[] getBytes(InputStream is) {
 
 		ByteArrayOutputStream bab = new ByteArrayOutputStream();
 		byte[] datas = new byte[8192];
 		int count = -1;
 
 		try {
 			while ((count = is.read(datas, 0, datas.length)) != -1) {
 				bab.write(datas, 0, count);
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return bab.toByteArray();
 	}
 
 	/**
 	 * generate bitmap rely on ID
 	 * 
 	 * @author yongjian.he
 	 * @param id
 	 * @return
 	 */
 	public static Bitmap getBitmapById(int id) {
 		return BitmapFactory.decodeResource(
 				LauncherApplication.mContext.getResources(), id);
 	}
 
 	/**
 	 * bitmap to drawable
 	 * 
 	 * @author yongjian.he
 	 */
 	public static Drawable bitmapToDrawable(Bitmap bp) {
 		Bitmap bitmap = bp;
 		if (bitmap != null) {
 			return new BitmapDrawable(
 					LauncherApplication.mContext.getResources(), bitmap);
 		} else {
 			Log.e(TAG, "---bitmapToDrawable "
 					+ "error: the src bitmap is null!");
 			bitmap = Bitmap.createBitmap(sBitmapTextureWidth,
 					sBitmapTextureHeight, Bitmap.Config.ARGB_8888);
 			return new BitmapDrawable(
 					LauncherApplication.mContext.getResources(), bitmap);
 		}
 	}
 
 	/**
 	 * bitmap to drawable
 	 * 
 	 * @author yongjian.he
 	 */
 	public static Bitmap drawableToBitmap(Drawable drawable) {
 		if (drawable != null) {
 			int w = drawable.getIntrinsicWidth();
 			int h = drawable.getIntrinsicHeight();
 			Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
 					: Bitmap.Config.RGB_565;
 			Bitmap bitmap = Bitmap.createBitmap(w, h, config);
 			Canvas canvas = new Canvas(bitmap);
 			drawable.setBounds(0, 0, w, h);
 			drawable.draw(canvas);
 			return bitmap;
 		} else {
 			Log.e(TAG, "---drawableToBitmap "
 					+ "error: the src drawable is null!");
 			return null;
 		}
 	}
 
 	/**
 	 * zoom drawable
 	 * 
 	 * @author yongjian.he
 	 */
 	public static Drawable zoomDrawable(Drawable drawable, int w, int h) {
 		if (drawable != null) {
 			int width = drawable.getIntrinsicWidth();
 			int height = drawable.getIntrinsicHeight();
 			Bitmap oldbmp = drawableToBitmap(drawable);
 			Matrix matrix = new Matrix();
 			float sx = ((float) w / width);
 			float sy = ((float) h / height);
 			matrix.postScale(sx, sy);
 			Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
 					matrix, true);
 			return new BitmapDrawable(
 					LauncherApplication.mContext.getResources(), newbmp);
 		} else {
 			Log.e(TAG, "---zoomDrawable " + "error: the src drawable is null!");
 			return null;
 		}
 	}
 
 	/**
 	 * generate RoundedCornerBitmap
 	 * 
 	 * @author yongjian.he
 	 * @param roundPx
 	 *            : corner radius.
 	 */
 	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
 		int w = bitmap.getWidth();
 		int h = bitmap.getHeight();
 		Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
 		Canvas canvas = new Canvas(output);
 		final int color = 0xff424242;
 		final Paint paint = new Paint();
 		final Rect rect = new Rect(0, 0, w, h);
 		final RectF rectF = new RectF(rect);
 		paint.setAntiAlias(true);
 		canvas.drawARGB(0, 0, 0, 0);
 		paint.setColor(color);
 		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
 		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
 		canvas.drawBitmap(bitmap, rect, rect, paint);
 
 		return output;
 	}
 
 	/**
 	 * 判断是否有网络连接
 	 * 
 	 * @return
 	 */
 	public static boolean isNetworkConnected() {
 		ConnectivityManager mConnectivityManager = (ConnectivityManager) LauncherApplication.mContext
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
 		if (mNetworkInfo != null) {
 			return mNetworkInfo.isAvailable();
 		}
 		return false;
 	}
 
 	/**
 	 * 判断SD卡是否存在
 	 * 
 	 * @return
 	 */
 	public static boolean hasSdcard() {
 		String status = Environment.getExternalStorageState();
 		if (Environment.MEDIA_MOUNTED.equals(status)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * 把文件放到SD卡中
 	 * 
 	 * @param bm
 	 */
 	public static boolean saveFileToSD(InputStream is, String path) {
 		if (!hasSdcard()) {
 			return false;
 		}
 		if (is == null) {
 			return false;
 		}
 		FileOutputStream fos = null;
 		boolean success = false;
 		try {
 			File file = new File(path).getParentFile();
 			if (!file.exists()) {
 				file.mkdirs();
 			}
 			fos = new FileOutputStream(path);
 			byte[] b = new byte[1024];
 			int len = 0;
 			while ((len = is.read(b)) != -1) {
 				fos.write(b, 0, len);
 			}
 			success = true;
 		} catch (Exception e) {
 		} finally {
 			if (fos != null) {
 				try {
 					fos.close();
 				} catch (Exception e) {
 				}
 			}
 			if (is != null) {
 				try {
 					is.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 		return success;
 	}
 	/**
 	 * 将字符串以utf8保存到sd卡 hao.wang
 	 * @param filename 路径+文件名
 	 * @param content 内容
 	 * @throws Exception
 	 */
 	public static void saveString(String filename, String content){
 		if (!hasSdcard()) {
 			return;
 		}
 		FileOutputStream fos = null;
 		OutputStreamWriter writer = null;
 		try {
 			File file = new File(filename);
 			if (!file.getParentFile().exists()) {
 				file.getParentFile().mkdirs();
 			}
 			fos = new FileOutputStream(file);
 			writer = new OutputStreamWriter(fos,"utf-8");
 			writer.write(content);
 			writer.flush();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		finally {
 			try {
 				if (fos != null) {
 					fos.close();
 				}
 				if (writer != null) {
 					writer.close();
 				}
 
 			} catch (Exception e1) {
 			}
 		}
 	}
 
 	/**
 	 * 读取本地文本 utf8  hao.wang
 	 * @param filename 路径+文件名
 	 * @return
 	 */
 	public static String readString(String filename){
 		if (!hasSdcard()) {
 			return null;
 		}
 		StringBuffer buffer = new StringBuffer();
 	    try { 
 	        FileInputStream fis = new FileInputStream(filename); 
 	        InputStreamReader isr = new InputStreamReader(fis, "UTF-8"); 
 	        BufferedReader br = new BufferedReader(isr); 
 	        String line = null; 
 	        while ((line = br.readLine()) != null) { 
 //	            FileContent += "\r\n"; // 补上换行符 
 	        	buffer.append(line);
 	        } 
 	    } catch (Exception e) { 
 	        e.printStackTrace(); 
 	    }
 		return buffer.toString();
 	}
 	/**
 	 * get current API Versions
 	 * 
 	 * @return
 	 */
 	public static int getCurrentApiVersion() {
 		return Build.VERSION.SDK_INT;
 	}
  
 	/**
 	 * 检测文件是否有重名 若重名则在名字后面加数字来区别 eg：a.txt --> a(1).txt --> a(2).txt
 	 * 
 	 * @param file
 	 * @return
 	 */
 	public static File getCleverFileName(File file) {
 		if (file == null) {
 			return null;
 		}
 		String fileName = file.getName();
 		if (!file.exists()) {
 			if (!file.getParentFile().exists()) {
 				file.getParentFile().mkdirs();
 			}
 			return file;
 		} else {
 			int index = fileName.lastIndexOf(".");
 			if (index == -1) {
 				index = fileName.length();
 			}
 			boolean end = false;
 			String numStr = "";
 			String name = file.getName().substring(0, index);
 			for (int i = name.length(); i > 0; i--) {
 				String c = name.substring(i - 1, i);
 				if ("(".equals(c)) {
 					break;
 				}
 				if (end) {
 					numStr += c;
 				}
 				if (")".equals(c)) {
 					end = true;
 				}
 			}
 			if (end) {
 				int x = 1;
 				try {
 					x = Integer.parseInt(numStr);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				int y = name.length() - ("(" + x + ")").length();
 				name = name.substring(0, y);
 				fileName = name + "(" + (x + 1) + ")"
 						+ file.getName().substring(index);
 			} else {
 				fileName = name + "(1)" + file.getName().substring(index);
 			}
 
 			file = new File(file.getParentFile() + "/" + fileName);
 			if (file.exists()) {
 				return getCleverFileName(file);
 			} else {
 				return file;
 			}
 		}
 	}
 	
 	/**
 	 * 自动安装 hao.wang
 	 * @param apkPath
 	 * @param apkName
 	 */
 	public static void installAPK(String apkPath, String apkName,boolean isSecretly) {
 		File file = new File(apkPath, apkName);
 		Log.i("OpenFile", file.getName());
 		Log.i("OpenFile", apkPath);
 		Log.i("OpenFile", apkName);
 		if (!fileIsExist(file.getAbsolutePath())) {
 			return;
 		}
 
 		if (!isSecretly) {//手动安装
 			Intent intent = new Intent();
 			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			intent.setAction(android.content.Intent.ACTION_VIEW);
 			intent.setDataAndType(Uri.fromFile(file),
 					"application/vnd.android.package-archive");
 			LauncherApplication.mContext.startActivity(intent);
 		}else {//静默安装
 			Intent intent = new Intent(SecretlyInstallReceiver.ACTION_SECRETLY_INSTALL);
 			intent.putExtra(SecretlyInstallReceiver.INSTALL_APK_NAME, apkName);
 			intent.putExtra(SecretlyInstallReceiver.INSTALL_APK_PATCH, apkPath);//从assets中安装
 			LauncherApplication.mContext.sendBroadcast(intent);
 		}
 	}
 
 	/**
 	 * 是否已经安装APP应用程序
 	 * @param context
 	 * @param packageName
 	 * @return
 	 */
 	public static boolean isInstallApplication(Context context, String packageName){
 		try {
 			PackageInfo pkg = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
 			return pkg != null;
 		} catch(Exception e){
 		}
 		return false;
 	}
 	
 	/**
 	 * 删除文件 add by wanghao
 	 * @param file
 	 */
 	public static void deleteFile(File file) {
 		if (file.exists()) {
 			if (file.isFile()) {
 				file.delete();
 			} else if (file.isDirectory()) {
 				File files[] = file.listFiles();
 				for (int i = 0; i < files.length; i++) {
 					deleteFile(files[i]);
 				}
 			}
 			file.delete();
 		}
 	}
 	//get string from assert
 	public static String getStringFromAssets(String fileName) {
 		String result = "";
 		try {
 			InputStream in = LauncherApplication.mContext.getResources()
 					.getAssets().open(fileName);
 			int lenght = in.available();
 			byte[] buffer = new byte[lenght];
 			in.read(buffer);
 			result = EncodingUtils.getString(buffer, "UTF-8");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 	
 	/**
 	 * 根据文件名获得sd file
 	 * @param fileName
 	 * @return
 	 */
 	public static File getSdBackupFile(String fileName)
     {
     	File sdBackupFile = null;
     	if (Environment.getExternalStorageState().equals(MEDIA_MOUNTED))
     	{
     		sdBackupFile = new File(Environment.getExternalStorageDirectory(), fileName);
     		if(!sdBackupFile.exists())
     		{
     			try {
 					if(!sdBackupFile.createNewFile())
 					{
 						sdBackupFile = null;
 					}
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					sdBackupFile = null;
 					e.printStackTrace();
 				}
     		}
     	}
     	return sdBackupFile;
     }
     
 	/**
 	 * 将文件内容copy到另一文件
 	 * @param formFile
 	 * @param toFile
 	 * @return
 	 */
     public static boolean copyFile(File formFile, File toFile)
     {
     	boolean success = false;
     	FileInputStream fis = null;
     	FileOutputStream fos = null;
     	try {
 			fis = new FileInputStream(formFile);
 			fos = new FileOutputStream(toFile);
 			byte bt[] = new byte[1024];
 			int c;
 			while ((c = fis.read(bt)) > 0) 
 			{
 				fos.write(bt, 0, c);
 			}
 			success = true;
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			try {
 				if(fis != null)fis.close();
 				if(fos != null)fos.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
     	
     	return success;
     }
 	/** 
 	   * 从Assets中读取图片 
 	   * eg:getImageFromAssetsFile("img/cat_blink0000.png");  
 	   */  
 	public static Bitmap getBitmapFromAssets(String fileName) {
 		if (fileName == null) {
 			return null;
 		}
 		Bitmap image = null;
 		try {
 			InputStream in = LauncherApplication.mContext.getResources()
 					.getAssets().open(fileName);
 			image = BitmapFactory.decodeStream(in);
 			in.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return image;
 	}
 	/**
 	 * 判断文件是否存在
 	 * @param fileAbsolutePath 绝对路径
 	 * @return
 	 */
 	public static boolean fileIsExist(String fileAbsolutePath) {
 		String fileName = fileAbsolutePath;
 		File file = new File(fileName);
 		if (file.exists()) {
 			return true;
 		}
 		return false;
 	}
 	/**
 	 * 比较时间
 	 * @param s1
 	 * @param s2
 	 * @return 相差多少天
 	 * @throws Exception
 	 */
 	public static long dateCompare(String s1,String s2){
 		long differ = 0;
 		try {
 			//设定时间的模板
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 			//得到指定模范的时间
 			Date d1 = sdf.parse(s1);
 			Date d2 = sdf.parse(s2);
 			//比较
 			differ = (int)Math.abs(((d1.getTime() - d2.getTime())/(24*3600*1000)));
 		} catch (Exception e) {
 		}
 		return differ;
 	}
 	/**
 	 * 当前时间
 	 * @return
 	 */
 	public static String getCurrentDate() {
 		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		String time = sDateFormat.format(new java.util.Date());
 		return time;
 	}
 	
 	/**
 	 * 是否为系统应用程序
 	 * @param context
 	 * @param packageName
 	 * @return
 	 */
 	public static boolean isSystemApplication(Context context, String packageName){
 		boolean ret = false;
 		try {
 			PackageInfo pkg = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
 			if(pkg != null){
 				if((pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 || (pkg.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0){
 				}else{
 					ret = true;
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
	
	/**
	 * 获取版本号(内部识别号)
	 * @param context
	 * @return
	 */
	public static int getVersionCode(Context context)  
	{  
	    try {  
	        PackageInfo pi=context.getPackageManager().getPackageInfo(context.getPackageName(), 0);  
	        return pi.versionCode;  
	    } catch (NameNotFoundException e) {  
	        // TODO Auto-generated catch block  
	        e.printStackTrace();  
	        return 0;  
	    }  
	}  

 }
