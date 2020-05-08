 package com.quaintsoft.imageviewer;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import android.content.Context;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.util.Log;
 
 public class CachedBitmap {
 
 	private Context context;
 	private File cache;
 	
 	private int width;
 	private int height;
 	
 	public CachedBitmap(Context ctx, Uri uri) {
 		context = ctx;
 		
 		try {
 			cache = File.createTempFile(".cachedbmp", null);
 			copyUriToFile(uri, cache);
 		} catch (Exception e) {
 			Log.d("Test", "Couldn't create temp file");
 		}
 		
 		initDimensions();
 	}
 	
 	private void copyUriToFile(Uri uri, File file) throws FileNotFoundException, IOException {
 		InputStream uriStream = context.getContentResolver().openInputStream(uri);
 		OutputStream fileStream = new FileOutputStream(file);
 		
 		copyStream(uriStream, fileStream);
 		
 		uriStream.close();
 		fileStream.close();
 	}
 	
 	private void copyStream(InputStream in, OutputStream out) throws IOException {
 		BufferedInputStream inBuf = new BufferedInputStream(in);
 		BufferedOutputStream outBuf = new BufferedOutputStream(out);
 
 		int oneByte = inBuf.read();
 		while (oneByte != -1) {
 			outBuf.write(oneByte);
 			oneByte = inBuf.read();
 		}
 		
 		outBuf.flush();
 	}
 	
 	private void initDimensions() {
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inJustDecodeBounds = true;
 		BitmapFactory.decodeFile(cache.getPath(), options);
 		width = options.outWidth;
 		height = options.outHeight;
 	}
 	
 	public int getWidth() {
 		return width;
 	}
 	
 	public int getHeight() {
 		return height;
 	}
 	
	public Bitmap 
	
 }
