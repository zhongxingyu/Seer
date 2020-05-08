 package se.mah.kd330a.project.find.data;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.DisplayMetrics;
 
 public class GetImage {
 
 	public static boolean doesFileExists(String filename,Context c){
 		File f = c.getFileStreamPath(filename);
 		return f.exists();
 	}
 
 	public static boolean deletefile(String filename, Context c){
 		c.deleteFile(filename);
 		return !doesFileExists(filename,c);
 	}
 
 	//Gets a picture from local storage
 	public static Bitmap getImageFromLocalStorage(String filename, Context c){
 		String fname=new File(c.getFilesDir(), filename).getAbsolutePath();
 		Bitmap bitmap = BitmapFactory.decodeFile(fname);
 		return bitmap;
 	}
 
 	//gets a picture from the net should be enclosed in AsyncTask
 	public static Bitmap getImageFromNet(String filename, boolean storeImageLocally, Context c){
 		String imageUrl = "http://195.178.234.7/mahapp/pictlib.aspx?filename="+filename+"&resolution="+getResolution(c);
 		//try {
 		Bitmap bitmap = null;
 		try {
 			bitmap = BitmapFactory.decodeStream((InputStream)new URL(imageUrl).getContent());
 			if (storeImageLocally){
 				storeImageLocal(filename,bitmap,c);
 				return bitmap;
 			}
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return bitmap;
 	}
 
	private static String getResolution(Context c) {
 		switch (c.getResources().getDisplayMetrics().densityDpi) {
 		case DisplayMetrics.DENSITY_MEDIUM:
 			return "mdpi";
 		case DisplayMetrics.DENSITY_HIGH:
 			return "hdpi";
 		case DisplayMetrics.DENSITY_XHIGH:
 			return "xhdpi";   
 		case DisplayMetrics.DENSITY_XXHIGH:
 			return "xxhdpi";   
 		default:
 			return "mdpi";
 		}
 	}
 
 	private static boolean storeImageLocal(String filename, Bitmap b, Context c){
 		boolean success = false;
 		try {
 			FileOutputStream fos = c.openFileOutput(filename, Context.MODE_PRIVATE);
 			success = b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
 			fos.close();
 			doesFileExists(filename,c);
 			success = true;
 		} catch (Exception e) {
 			e.printStackTrace();
 		} 
 		return success;
 	}
 
 }
