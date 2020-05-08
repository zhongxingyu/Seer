 /**
  * Author - Prasannajeet Pani, Copyright reserved
  */
 
 
 package com.praszapps.owetracker.util;
 
 import java.util.UUID;
 
 import com.praszapps.owetracker.R;
 import com.praszapps.owetracker.application.OweTrackerApplication;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.view.Gravity;
 import android.widget.Toast;
 
 public class Utils {
 
 	public static interface DialogResponse {
 		void onPositive();
 		void onNegative();
 		void onNeutral();
 	}
 	
 	public static void showAlertDialog(Context context, String title, String message, Drawable icon, Boolean isCancelable,
 			String positiveString, String negativeString, String neutralString, final DialogResponse response){
 		
 		AlertDialog.Builder alert = new AlertDialog.Builder(context);
 		
 		if(title != null) {
 			alert.setTitle(title);
 		}
 		
 		alert.setMessage(message);
 		
 		if(icon != null) {
 			alert.setIcon(icon);
 		}
 		
 		if(isCancelable) {
 			alert.setCancelable(true);
 		} else {
 			alert.setCancelable(false);
 		}
 		
 		if(positiveString != null) {
 			alert.setPositiveButton(positiveString, new OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					response.onPositive();
 				}
 			});
 		}
 		
 		if(negativeString != null) {
 			alert.setNegativeButton(negativeString, new OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					response.onNegative();
 				}
 			});
 		}
 		
 		if(neutralString != null) {
 			alert.setNeutralButton(neutralString, new OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					response.onNeutral();
 				}
 			});
 		}
 		alert.show();
 	}
 	
 	public static void showToast(Context context, String text, int displayTime) {
 		if(displayTime == Toast.LENGTH_LONG || displayTime == Toast.LENGTH_SHORT) {
 			
 			Toast toast = Toast.makeText(context, text, displayTime);
 			toast.setGravity(Gravity.CENTER, 0, 0);
 			toast.show();
 		}
 		
 	}
 		
 	public static void showLog(String tag, String msg, int level) {
 		switch(level) {
 		
 		case Log.ERROR:
 			Log.e(tag, msg);
 			break;
 		case Log.VERBOSE:
 			Log.v(tag, msg);
 			break;
 		case Log.INFO:
 			Log.i(tag, msg);
 			break;
 		case Log.DEBUG:
 			Log.d(tag, msg);
 			break;
 		default:
 			Log.v(tag, msg);
 			break;
 		}
 	}
 	
 	public static String generateUniqueID()
 	{
 		String uid = UUID.randomUUID().toString().toUpperCase();
 		return uid;
 	}
 	
 	public static String getCurrencyFromArrayItem(String arrayItem) {
 		
 		if(arrayItem.equals(OweTrackerApplication.getContext().getResources().getString(R.string.array_currency_item_rupee))) {
			
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH){
			    // Rupee symbol replaced by INR for below ICS version
					return "INR ";
				} else {
					return OweTrackerApplication.getContext().getResources().getString(R.string.currency_rupee);
				}
			
 		} else if(arrayItem.equals(OweTrackerApplication.getContext().getResources().getString(R.string.array_currency_item_dollar))) {
 			return OweTrackerApplication.getContext().getResources().getString(R.string.currency_dollar);
 		} else if(arrayItem.equals(OweTrackerApplication.getContext().getResources().getString(R.string.array_currency_item_yen))) {
 			return OweTrackerApplication.getContext().getResources().getString(R.string.currency_yen);
 		} else if(arrayItem.equals(OweTrackerApplication.getContext().getResources().getString(R.string.array_currency_item_pound))) {
 			return OweTrackerApplication.getContext().getResources().getString(R.string.currency_pound);
 		} else if(arrayItem.equals(OweTrackerApplication.getContext().getResources().getString(R.string.array_currency_item_euro))) {
 			return OweTrackerApplication.getContext().getResources().getString(R.string.currency_euro);
 		} else {
 			return null;
 		}
 	}
 	
 	public static String getArrayItemFromCurrency(String currency) {
 		
		if(currency.equals(OweTrackerApplication.getContext().getResources().getString(R.string.currency_rupee)) || currency.equals("INR ")) {
 			return OweTrackerApplication.getContext().getResources().getString(R.string.array_currency_item_rupee);
 		} else if(currency.equals(OweTrackerApplication.getContext().getResources().getString(R.string.currency_dollar))) {
 			return OweTrackerApplication.getContext().getResources().getString(R.string.array_currency_item_dollar);
 		} else if(currency.equals(OweTrackerApplication.getContext().getResources().getString(R.string.currency_yen))) {
 			return OweTrackerApplication.getContext().getResources().getString(R.string.array_currency_item_yen);
 		} else if(currency.equals(OweTrackerApplication.getContext().getResources().getString(R.string.currency_pound))) {
 			return OweTrackerApplication.getContext().getResources().getString(R.string.array_currency_item_pound);
 		} else if(currency.equals(OweTrackerApplication.getContext().getResources().getString(R.string.currency_euro))) {
 			return OweTrackerApplication.getContext().getResources().getString(R.string.array_currency_item_euro);
 		} else {
 			return null;
 		}
 	}
 	
 }
