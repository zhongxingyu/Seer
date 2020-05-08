 package edu.vub.at.nfcpoker.smartwatch;
 
 import android.content.ContentValues;
 import android.content.Context;
 
 import com.sonyericsson.extras.liveware.aef.registration.Registration;
 import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
 import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;
 
public class WePokerSWRegistrationInformation extends
		RegistrationInformation {
 	
 	private Context mContext;
 
 	public WePokerSWRegistrationInformation(Context ctx) {
 		mContext = ctx;
 	}
 
 	@Override
 	public ContentValues getExtensionRegistrationConfiguration() {
 		String iconHostapp = ExtensionUtils.getUriString(mContext, R.drawable.ic_launcher);
 		String iconExtension = ExtensionUtils.getUriString(mContext, R.drawable.applevel_icon);
 
 		ContentValues values = new ContentValues();
 
 		values.put(Registration.ExtensionColumns.NAME, "wePoker");
 		values.put(Registration.ExtensionColumns.EXTENSION_KEY, WePokerSWService.SMARTWATCH_KEY);
 		values.put(Registration.ExtensionColumns.HOST_APP_ICON_URI, iconHostapp);
 		values.put(Registration.ExtensionColumns.EXTENSION_ICON_URI, iconExtension);
 		values.put(Registration.ExtensionColumns.NOTIFICATION_API_VERSION,
 				getRequiredNotificationApiVersion());
 		values.put(Registration.ExtensionColumns.PACKAGE_NAME, mContext.getPackageName());
 
 	        return values;
 	}
 	
 	@Override
 	public int getRequiredNotificationApiVersion() {
 		return 0;
 	}
 
 	@Override
 	public int getRequiredWidgetApiVersion() {
 		return 1;
 	}
 
 	@Override
 	public int getRequiredControlApiVersion() {
 		return 0;
 	}
 
 	@Override
 	public int getRequiredSensorApiVersion() {
 		return 0;
 	}
 
 	@Override
 	public boolean isWidgetSizeSupported(int width, int height) {
 		return true;
 	}
 	
 	@Override
 	public boolean isDisplaySizeSupported(int width, int height) {
 		return true;
 	}
 }
