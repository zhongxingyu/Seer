 /**
  * 
  */
 package com.hardincoding.sonar.util;
 
import android.app.Activity;
 import android.widget.Toast;
 
 /**
  * @author Kurt Hardin
  *
  */
 public class ToastProgressListener implements ProgressListener {
 	
	private final Activity mActivity;
 	private int mToastLength = Toast.LENGTH_SHORT;
 	
	public ToastProgressListener(final Activity context) {
		mActivity = context;
 	}
 	
 	public void setToastLength(final int toastLength) {
 		mToastLength = toastLength;
 	}
 
 	@Override
	public void updateProgress(final String message) {
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(mActivity, message, mToastLength).show();
			}
		});
 	}
 	
 	@Override
 	public void updateProgress(int messageId) {
 		// TODO Show toast message for messageId
 	}
 
 }
