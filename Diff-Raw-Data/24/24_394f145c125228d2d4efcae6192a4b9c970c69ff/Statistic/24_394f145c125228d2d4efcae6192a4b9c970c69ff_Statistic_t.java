 package de.saschahlusiak.hrw.dienststatus.model;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 
 import android.content.Context;
 import android.graphics.drawable.BitmapDrawable;
 import android.util.Log;
 
 public class Statistic {
 	BitmapDrawable d;
 	boolean valid;
 	String url;
 	int index;
 	
 	private static final String tag = Statistic.class.getSimpleName();
 	private static final int TIMEOUT = 30; /* minutes */
 	
 	public Statistic(int index, String url) {
 		this.url = url;
 		this.index = index;
 	}
 	
 	public void setValid(boolean valid) {
 		this.valid = valid;
 	}
 	
 	public boolean getValid() {
 		return valid;
 	}
 	
 	public int getIndex() {
 		return index;
 	}
 	
 	public synchronized BitmapDrawable getBitmap() {
 		return d;
 	}
 	
 	public synchronized void setBitmap(BitmapDrawable bitmap) {
 		this.d = bitmap;
 	}
 	
 	public void setURL(String url) {
 		this.url = url;
 	}
 	
 	public String getURL() {
 		return url;
 	}
 	
 	public String getWebURL() {
 		return "http://static.hs-weingarten.de/portvis/" + url + ".png";
 	}
 	
 	public boolean loadCachedBitmap(Context context) {
 		BitmapDrawable b = null;
 		try {
 			FileInputStream fis = context.openFileInput(url + ".png");
 			b = (BitmapDrawable) BitmapDrawable.createFromStream(fis, null);
 			if (b != null)
 				setBitmap(b);
 			fis.close();
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 		return (b != null);
 	}
 	
 	public File getCacheFile(Context context) {
 		return context.getFileStreamPath(url + ".png");
 	}
 	
 	public boolean fetch(Context context, boolean force) {
 		File file = context.getFileStreamPath(url + ".png");
 
 		/* use cached file if younger than TIMEOUT minutes */
 		if (force || (file.lastModified() < System.currentTimeMillis() - TIMEOUT * 60 * 1000)) {
 			try {
 				InputStream myInput = (InputStream) new URL("http://static.hs-weingarten.de/portvis/" + url + ".png").getContent();
 				OutputStream myOutput = context.openFileOutput(url + ".png", Context.MODE_WORLD_READABLE);
 				
 				byte[] buffer = new byte[4096];
 				int length;
 				while ((length = myInput.read(buffer)) > 0) {
 					myOutput.write(buffer, 0, length);
 					if (Thread.interrupted()) {
 						myOutput.flush();
 						myInput.close();
 						myOutput.close();
 						file.delete();
 						Log.d(tag, "removed partly downloaded file "
 								+ file.getPath());
 						return false;
 					}
 				}
 				myOutput.flush();
 				myInput.close();
 				myOutput.close();
 				return true;
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} else {
 			Log.d(tag, "using cached file " + file.getPath());
 		}
 
 		return false;
 	}
 	
 }
