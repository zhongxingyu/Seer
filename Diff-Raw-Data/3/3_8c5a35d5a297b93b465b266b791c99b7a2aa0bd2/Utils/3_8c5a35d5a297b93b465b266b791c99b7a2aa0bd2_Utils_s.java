 package com.rava.voting.utils;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import retrofit.RetrofitError;
 import retrofit.client.Response;
 import android.app.Activity;
 import android.content.Context;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.pm.Signature;
 import android.graphics.Bitmap;
 import android.util.Base64;
 import android.util.Log;
 import android.view.ViewGroup;
 import android.widget.Toast;
 
 public class Utils {
 	/**
 	 * Take screenshot of the activity including the action bar
 	 * 
 	 * @param activity
 	 * @return The screenshot of the activity including the action bar
 	 */
 	public static Bitmap takeScreenshot(Activity activity) {
 		ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
 		ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
 		decorChild.setDrawingCacheEnabled(true);
 		decorChild.buildDrawingCache();
 		Bitmap drawingCache = decorChild.getDrawingCache(true);
 		Bitmap bitmap = Bitmap.createBitmap(drawingCache);
 		decorChild.setDrawingCacheEnabled(false);
 		return bitmap;
 	}
 
 	/**
 	 * Print hash key
 	 */
 	public static void printHashKey(Context context) {
 		try {
 			String TAG = "com.sromku.simple.fb.example";
 			PackageInfo info = context.getPackageManager().getPackageInfo(TAG,
 					PackageManager.GET_SIGNATURES);
 			for (Signature signature : info.signatures) {
 				MessageDigest md = MessageDigest.getInstance("SHA");
 				md.update(signature.toByteArray());
 				String keyHash = Base64.encodeToString(md.digest(),
 						Base64.DEFAULT);
 				Log.d(TAG, "keyHash: " + keyHash);
 			}
 		} catch (NameNotFoundException e) {
 
 		} catch (NoSuchAlgorithmException e) {
 
 		}
 	}
 
 	public static void parseError(RetrofitError error, Context context) {
 		String errorString = error.getMessage();
 		if (errorString == null) {
 			Response response = error.getResponse();
 			errorString = response.getReason();
 			if (errorString == null) {
 				errorString = "error";
 			}
 		}
		Toast.makeText(context, errorString, Toast.LENGTH_SHORT).show();
 	}
 }
