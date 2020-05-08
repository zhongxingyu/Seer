package com.gplio.common;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import android.R.bool;
 import android.content.Context;
 import android.util.Log;
 
 public class TextResourceReader {
 
 	private static File unpackFromAssets(final Context context, String pathAssets,boolean dontUseCacheFile) {
 
 		File f = new File(context.getCacheDir() + pathAssets);
 		if(dontUseCacheFile){
 			Log.d("WARNING", "NOT USING CACHE FILE " + pathAssets);			
 		}
 		if (!f.exists() ||  dontUseCacheFile) {
 			Log.d("APP", "Unpacking " + pathAssets);
 			InputStream is;
 			try {
 				is = context.getAssets().open(pathAssets);
 				int size = is.available();
 				byte[] buffer = new byte[size];
 				is.read(buffer);
 				is.close();
 
 				FileOutputStream fos = new FileOutputStream(f);
 				fos.write(buffer);
 				fos.close();
 
 			} catch (IOException e) {
 				Log.d("APP", "Could not read " + pathAssets + " Used: "
 						+ (context.getCacheDir() + pathAssets));
 				return null;
 			}
 		}
 
 		return f;
 
 	}
 
 	public static String readTextFileFromAssets(final Context context,
 			String pathAssets) {
 		File f = unpackFromAssets(context, pathAssets,true);
 		if (f == null) {
 			Log.d("APP", "Could not read " + pathAssets);
 			return null;
 		}
 		InputStream is;
 		try {
 			is = new FileInputStream(f);
 		} catch (FileNotFoundException e1) {
 			Log.e("TextResourceReader", "File not found");
 			return null;
 		}
 
 		String t = "";
 		try {
 			t = readInputStream(is);
 
 		} catch (IOException e) {
 			Log.d("TextResourceReader", "Could not read file");
 			return null;
 		}
 
 		return t;
 
 	}
 
 	private static String readInputStream(InputStream is) throws IOException {
 		final InputStreamReader inputStreamReader = new InputStreamReader(is);
 		final BufferedReader bufferedReader = new BufferedReader(
 				inputStreamReader);
 
 		String nextLine = "";
 		final StringBuilder body = new StringBuilder();
 
 		while ((nextLine = bufferedReader.readLine()) != null) {
 			body.append(nextLine);
 			body.append('\n');
 		}
 		return body.toString();
 	}
 
 	public static String readTextFileFrowRawResource(final Context context,
 			final int resourceID) {
 		final InputStream inputStream = context.getResources().openRawResource(
 				resourceID);
 		String t = "";
 		try {
 			t = readInputStream(inputStream);
 
 		} catch (IOException e) {
 			Log.d("TextResourceReader", "Could not read file");
 			return null;
 		}
 
 		return t;
 
 	}
 }
