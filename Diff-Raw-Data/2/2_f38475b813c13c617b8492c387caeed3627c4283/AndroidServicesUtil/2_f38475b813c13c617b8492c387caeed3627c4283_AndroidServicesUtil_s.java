 package com.mawape.aimant.utilities;
 
 import com.mawape.aimant.R;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 
 public class AndroidServicesUtil {
 	public static void makePhoneCall(Context context, String phoneNumber) {
 		String defaultPhonePrefix = context.getResources().getString(
 				R.string.default_prefijo_telefono);
		if (phoneNumber.startsWith(defaultPhonePrefix)) {
 			defaultPhonePrefix = "";
 		}
 		makePhoneCall(context, defaultPhonePrefix, phoneNumber);
 	}
 
 	public static void makePhoneCall(Context context, String prefijoTelefono,
 			String phoneNumber) {
 		Intent intent = new Intent(Intent.ACTION_CALL);
 		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		intent.setData(Uri.parse("tel:" + prefijoTelefono + phoneNumber));
 		context.startActivity(intent);
 	}
 }
