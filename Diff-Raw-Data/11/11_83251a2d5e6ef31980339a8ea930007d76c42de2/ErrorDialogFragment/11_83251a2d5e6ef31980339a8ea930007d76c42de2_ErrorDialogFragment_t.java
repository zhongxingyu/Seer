 package com.mango.tagtweets;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 
 public class ErrorDialogFragment extends DialogFragment {
 	
 	public static final int ERROR_NO_NETWORK = 0;
 	
 	private int mType = 0;
 	
 	
 	private DialogInterface.OnClickListener mPositiveButtonListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			switch(mType) {
 			case ERROR_NO_NETWORK :
 				//go to wifi settings
 				Intent intent = new Intent();
 				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				intent.setAction("android.settings.WIFI_SETTINGS");
 				getActivity().startActivity(intent);
 				break;
 			default:
 				break;
 			}
 		}
 	};
 	
 	private DialogInterface.OnClickListener mNegativeButtonListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			switch(mType) {
 			case ERROR_NO_NETWORK :
 				getActivity().finish();
 				break;
 			default:
 				break;
 			}
 		}
 	};
 	
 	public ErrorDialogFragment (int type) {
 		super ();
 		
 		mType = type;
     }
     
 	private int getTitle () {
 		switch(mType) {
 		case ERROR_NO_NETWORK :
 			return R.string.error_dialog_no_network;
 		default:
 			return R.string.alert;
 		}
 	}
 	
	private int getMessage () {
		switch(mType) {
		case ERROR_NO_NETWORK :
			return R.string.need_data_connection;
		default:
			return R.string.alert;
		}
	}
	
 	private int getPositiveButtonText () {
 		switch(mType) {
 		case ERROR_NO_NETWORK :
 			return R.string.go_to_settings;
 		default:
 			return R.string.ok;
 		}
 	}
 	
 	private int getNegativeButtonText () {
 		switch(mType) {
 		case ERROR_NO_NETWORK :
 			return R.string.close;
 		default:
 			return R.string.cancel;
 		}
 	}
 
     @Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
     	
     	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         
         builder.setTitle(getTitle());
        builder.setMessage(getMessage());
         builder.setPositiveButton(getPositiveButtonText(), mPositiveButtonListener);
         builder.setNegativeButton(getNegativeButtonText(), mNegativeButtonListener);
         
         return builder.create();
 	}
 }
