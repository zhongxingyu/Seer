 package com.trellmor.berrymotes;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.net.Uri;
 
 /**
  * Emote helpers
  * 
  * @author Daniel
  *
  */
 public class EmoteUtils {
 	public static String BERRYMOTES_NAME = "com.trellmor.berrymotes";
 	
 	private EmoteUtils() {
 	}
 	
 	/**
 	 * Check if BerryMotes app is installed 
 	 * 
 	 * @param context Android context
 	 * @return true if BerryMotes is installed
 	 */
 	public static boolean isBerryMotesInstalled(Context context) {
 		PackageManager pm = context.getPackageManager();
 		try {
 			pm.getPackageInfo(BERRYMOTES_NAME, PackageManager.GET_ACTIVITIES);
 			return true;
 		} catch (PackageManager.NameNotFoundException e) {
 			return false;
 		}
 	}
 	
 	/**
 	 * Launch play store to install BerryMotes
 	 * 
 	 * @param context Android context
 	 */
 	public static void launchMarkert(Context context) {
 		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("market://details?id=" + BERRYMOTES_NAME));
 		context.startActivity(intent);
 	}
 	
 	/**
 	 * Open BerryMotes settings
 	 * 
 	 * @param context Android context
 	 */
 	public static void launchBerryMotesSettings(Context context) {
 		Intent intent = new Intent();
 		intent.setAction(BERRYMOTES_NAME + ".Settings");
 		context.startActivity(intent);
 	}
 	
 	/**
 	 * Show AlertDialog to install BerryMotes
 	 * 
 	 * @param context Android context
 	 */
 	public static void showInstallDialog(Context context) {
 		final Context theContext = context;
 		AlertDialog.Builder builder = new AlertDialog.Builder(theContext);
 		builder.setTitle(R.string.berrymotes_app_name);
 		builder.setMessage(R.string.berrymotes_not_installed);
 		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				EmoteUtils.launchMarkert(theContext);
 			}
 		});
 		builder.setNegativeButton(android.R.string.cancel, null);
 		builder.show();
 	}
 }
