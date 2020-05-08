 /**
  * 
  */
 package com.hardincoding.sonar.util;
 
import android.content.Context;
 import android.widget.Toast;
 
 /**
  * @author Kurt Hardin
  *
  */
 public class ToastProgressListener implements ProgressListener {
 	
	private final Context mContext;
 	private int mToastLength = Toast.LENGTH_SHORT;
 	
	public ToastProgressListener(final Context context) {
		mContext = context;
 	}
 	
 	public void setToastLength(final int toastLength) {
 		mToastLength = toastLength;
 	}
 
 	@Override
	public void updateProgress(String message) {
		Toast.makeText(mContext, message, mToastLength).show();
 	}
 	
 	@Override
 	public void updateProgress(int messageId) {
 		// TODO Show toast message for messageId
 	}
 
 }
