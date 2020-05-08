 package ezhun.smsb.storage;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 
 /**
  * Application Settings storage class.
  */
 public class ApplicationSettings {
 	private static final String FILE_NAME = "ezhun.smsb.settings";
 	private static final String DISPLAY_NOTIFICATION_VALUE = "display_notification";
 	private static final String DELETE_MESSAGES_AFTER_VALUE = "delete_after";
 
 	private Context context;
 	private SharedPreferences preferencesProvider;
 	private final Object lock = new Object();

	private Boolean displayNotification = null;
	private DeleteAfter deleteAfter = null;
 
 	private Context getContext() {
 		return context;
 	}
 
 	public boolean getDisplayNotification() {
 		synchronized (lock) {
			if (displayNotification != null) {
 				return displayNotification;
 			} else {
 				displayNotification = getPreferencesProvider().getBoolean(
 						DISPLAY_NOTIFICATION_VALUE,
 						DefaultApplicationSettings.DISPLAY_NOTIFICATION_VALUE);
 				return displayNotification;
 			}
 		}
 	}
 
 	public DeleteAfter getDeleteAfter() {
 		synchronized (lock) {
			if (deleteAfter != null) {
 				return deleteAfter;
 			} else {
 				String res = getPreferencesProvider().getString(
 						DELETE_MESSAGES_AFTER_VALUE,
 						DefaultApplicationSettings.DELETE_MESSAGES_AFTER_VALUE.name());
 				deleteAfter = Enum.valueOf(DeleteAfter.class, res);
 				return deleteAfter;
 			}
 		}
 	}
 
 	private SharedPreferences getPreferencesProvider() {
 		return preferencesProvider;
 	}
 
 	public ApplicationSettings(Context context) {
 		this.preferencesProvider = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
 		this.context = context;
 	}
 
 	public void setDisplayNotification(boolean value) {
 		synchronized (lock) {
			displayNotification = null;
 			SharedPreferences.Editor e = getPreferencesProvider().edit();
 			e.putBoolean(DISPLAY_NOTIFICATION_VALUE, value);
 			commit(e);
 		}
 	}
 
 	public void setDeleteAfter(DeleteAfter value) {
 		synchronized (lock) {
			deleteAfter = null;
 			SharedPreferences.Editor e = getPreferencesProvider().edit();
 			e.putString(DELETE_MESSAGES_AFTER_VALUE, value.name());
 			commit(e);
 		}
 	}
 
 	private void commit(SharedPreferences.Editor e) {
 		boolean res = e.commit();
 		if (!res) {
 			//todo: PANIC
 		}
 	}
 }
