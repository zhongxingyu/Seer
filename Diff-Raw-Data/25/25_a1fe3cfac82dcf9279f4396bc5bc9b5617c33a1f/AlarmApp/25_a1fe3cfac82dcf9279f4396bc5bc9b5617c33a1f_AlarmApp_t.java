 package org.alarmapp;
 
import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.annotation.ReportsCrashes;
 import org.alarmapp.model.AlarmStore;
 import org.alarmapp.model.User;
 import org.alarmapp.model.classes.PersistentAlarmStore;
 import org.alarmapp.util.Ensure;
 import org.alarmapp.util.LogEx;
 import org.alarmapp.util.Store;
 import org.alarmapp.web.AuthHttpClient;
 import org.alarmapp.web.AuthWebClient;
 import org.alarmapp.web.HttpWebClient;
 
 import android.app.Application;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
//dFRiRHVCbGVxVzNva0NkdHB0NVN5Q0E6MQ
@ReportsCrashes(formKey = "dFRiRHVCbGVxVzNva0NkdHB0NVN5Q0E6MQ")
 public class AlarmApp extends Application {
 
 	private static AlarmApp instance;
 
 	private static AuthWebClient webClient = null;
 
 	/***
 	 * @throws IllegalStateException
 	 *             if the user did not log in yet
 	 * @return A web client for accessing the alarm web service
 	 */
 	public static AuthWebClient getWebClient() {
 		Ensure.notNull(instance);
 
 		if (getUser() == null)
 			throw new IllegalStateException(
 					"Kein Benutzer verfügbar. Sie müssen sich erst einloggen!");
 
 		if (webClient == null)
 			webClient = new AuthHttpClient(new HttpWebClient(), getUser()
 					.getAuthToken());
 		return webClient;
 	}
 
 	private static User user = null;
 
 	/**
 	 * 
 	 * @return The user or null if the user did not log in yet
 	 */
 	public static User getUser() {
 		Ensure.notNull(instance);
 
 		if (user == null) {
 			Store<User> userStore = new Store<User>(instance, "USER");
 			user = userStore.read();
 			LogEx.verbose("Loaded the user " + user + " from local storage");

			if (user != null) {

				ErrorReporter.getInstance().putCustomData("UserId",
						Integer.toString(user.getId()));
				ErrorReporter.getInstance().putCustomData("User Name",
						user.getFullName());

				if (user.getFireDepartment() != null)
					ErrorReporter.getInstance().putCustomData(
							"Fire Department",
							user.getFireDepartment().GetName());
			}
 		}
 		return user;
 	}
 
 	public static void setUser(User user) {
 		Ensure.notNull(user);
 
 		AlarmApp.user = user;
 		Store<User> userStore = new Store<User>(instance, "USER");
 		userStore.write(user);
 
 		LogEx.info("Wrote user " + user + " to local storage");
 	}
 
 	private static AlarmStore alarmStore = null;
 
 	public static AlarmStore getAlarmStore() {
 		Ensure.notNull(instance);
 
 		if (alarmStore == null)
 			alarmStore = new PersistentAlarmStore(instance);
 
		// throw new NullPointerException();

 		return alarmStore;
 	}
 
 	public static SharedPreferences getPreferences() {
 		Ensure.notNull(instance);
 		return PreferenceManager.getDefaultSharedPreferences(instance);
 	}
 
 	@Override
 	public void onCreate() {
		ACRA.init(this);
 		super.onCreate();
 		instance = this;
 		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 	}
 }
