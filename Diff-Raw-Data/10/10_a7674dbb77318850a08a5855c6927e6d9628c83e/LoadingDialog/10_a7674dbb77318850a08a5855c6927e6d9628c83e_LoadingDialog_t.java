 package com.anddevbg.dialogs.loading;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface.OnCancelListener;
 
import com.anddevbg.andlib.R;
 import com.anddevbg.dialogs.IProgressDialog;
 
 /**
  * 
  * @author anddevbg@gmail.com
  *
  */
 public class LoadingDialog implements IProgressDialog {
 	
 	private ProgressDialog mLoadingDialog;
 	
 	/**
 	 *  Same as show(activity, R.string.loading, true);
 	 */
 	public void show(final Activity activity) {
 		show(activity, R.string.loading, true);
 	}
 	
 	public void show(final Activity activity, final int textResId, final boolean isCancelable) {
 		if (mLoadingDialog != null && mLoadingDialog.isShowing() || activity.isFinishing()) {
 			return;
 		}
 
 		activity.runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 				mLoadingDialog = ProgressDialog.show(activity, null, activity.getString(textResId), true, isCancelable);
 			}
 		});
 	}
 	
 	@Override
 	public void dismiss() {
 		if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
 			mLoadingDialog.dismiss();
 		}
 		
 		mLoadingDialog = null;
 	}
 	
 	@Override
 	public void setOnCancelListener(OnCancelListener listener) {
 		if (mLoadingDialog != null) {
 			mLoadingDialog.setOnCancelListener(listener);
 		}
 	}
 }
