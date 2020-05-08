 /*
  * ImageLoader.java
  * © 2011 nuboat.net. All rights reserved
  */
 package net.nuboat.petboat.helper;
 
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.os.Environment;
 import android.util.Log;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 /**
  *
  * @author  Peerapat Asoktummarungsri [nuboat@gmail.com]
  */
 public class ImageLoader {
 
     private static final String TAG = "ImageLoader";
 
 	private static final String FILEDIR = "/petboat";
 	private static final String FILETYPE= ".png";
 
     private ImageLoader() {}
 
 	public static boolean existImage(String episode, String order) throws Exception {
 		String filename = episode + "_" + order;
 		String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();
 
 		filepath += FILEDIR + "/" + filename + FILETYPE;
 
        File file = new File(filepath);

        if (!file.exists() || file.length() == 0) {
            return false;
        } else {
            return true;
        }
     }
 
     public static Bitmap getBitmapFromURL(String src, String episode, String order) throws Exception {
		Bitmap bit;
 		String filename = episode + "_" + order;
 		String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();
 
 		filepath += FILEDIR;
 		File dir = new File(filepath);
 		if (dir.exists() || !dir.isDirectory())
 			dir.mkdir();
 
 		filepath += "/" + filename + FILETYPE;
 		File file = new File(filepath);
		if (file.exists() && file.length() != 0)
 			bit = BitmapFactory.decodeFile(filepath);
 		else
 			bit = getBitmap(src, filepath);
 
         if (bit == null)
             throw new Exception("Cannot load anything");
 
         return bit;
     }
 
     private static Bitmap getBitmap(String src, String filepath) {
         Bitmap bit;
 		FileOutputStream fout = null;
 
         for (int i=0; i<3; i++) {
             try {
                 URL url = new URL(src);
                 HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                 connection.setDoInput(true);
                 connection.connect();
                 InputStream input = connection.getInputStream();
                 bit = BitmapFactory.decodeStream(input);
 
 				fout = new FileOutputStream(new File(filepath));
 				bit.compress(CompressFormat.PNG, 100, fout);
 				fout.flush();
 
                 return bit;
 
             } catch(Exception e) {
                 Log.e(TAG, Log.getStackTraceString(e));
             } finally {
 				if (fout != null)
 					try { fout.close(); } catch(IOException io) { Log.e(TAG, Log.getStackTraceString(io)); }
 			}
 
         }
         return null;
 
     }
 
 }
