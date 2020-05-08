 package com.anddevbg.dialogs.alert;
 
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 
import com.anddevbg.tools.R;
 
 /**
  * Default no internet connection dialog.
  * 
  * @author anddevbg@gmail.com
  * 
  */
 public class NoInternetConnectionDialog extends AbstractAlertDialog {
 
 	private final Runnable mOnCancel;
 	private final Runnable mOnRetry;
 
 	public NoInternetConnectionDialog(Runnable onRetry, Runnable onCancel) {
 		mOnRetry = onRetry;
 		mOnCancel = onCancel;
 	}
 
 	@Override
 	protected void build(Builder builder) {
 		builder.setTitle(R.string.error).setMessage(R.string.internet_connection_is_off).setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				if (mOnRetry != null) {
 					mOnRetry.run();
 				}
 
 				NoInternetConnectionDialog.this.dismiss();
 			}
 		}).setOnCancelListener(new OnCancelListener() {
 
 			@Override
 			public void onCancel(DialogInterface dialog) {
 				if (mOnCancel != null) {
 					mOnCancel.run();
 				}
 
 				NoInternetConnectionDialog.this.dismiss();
 			}
 		});
 	}
 }
