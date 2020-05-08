 package il.ac.huji.chores.dal;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 import com.parse.PushService;
 
 import il.ac.huji.chores.Settings;
 import il.ac.huji.chores.exceptions.FailedToGetApartmentSettings;
 import il.ac.huji.chores.exceptions.FailedToUpdateSettingsException;
 import il.ac.huji.chores.exceptions.UserNotLoggedInException;
 
 import java.util.List;
 
 public class ApartmentSettingsDAL {
 
 	public static Settings getSettings() throws UserNotLoggedInException,
 			FailedToGetApartmentSettings {
 		String username = RoommateDAL.getRoomateUsername();
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("Settings");
 		query.whereEqualTo("username", username);
 		ParseObject result;
 		try {
 			List<ParseObject> results = query.find();
 			if (results.size() == 0) {
 				return getDefaultSettings();
 			}
 			result = query.find().get(0);
 		} catch (ParseException e) {
 			throw new FailedToGetApartmentSettings(e.getMessage());
 		}
 		return convertObjectToSettings(result);
 	}
 
 	public static void registerToNotificationChannel(Context context,
 			String channel) {
 		PushService.subscribe(context, channel,
 				il.ac.huji.chores.ChoresNotification.class);
 
 	}
 
 	public static void unegisterToNotificationChannel(Context context,
 			String channel) {
 		PushService.unsubscribe(context, channel);
 	}
 
 	// get default settings and store the default settings in the database. Used
 	// by getSettings method.
 
 	private static Settings getDefaultSettings()
 			throws UserNotLoggedInException, FailedToGetApartmentSettings {
 
 		Settings settings = new Settings();
 		// Notifications
 		settings.notifications = settings.new Notifications();
 		settings.notifications.newChoresHasBeenDivided = true;
 		settings.notifications.roommateFinishedChore = true;
 		settings.notifications.roommateMissedChore = true;
 		settings.notifications.roommateStoleMyChore = true;
 
 		// chores
 		settings.chores = settings.new Chores();
 		settings.chores.disableRemindersAboutUpcomingChores = false;
 		settings.chores.forbidRoommatesFromTakingMyChores = false;
 
 		// reminders
 		settings.reminders = settings.new Reminders();
 		settings.reminders.hours = 2;
 
 		// store defaults in database
 		try {
 			updateSettings(settings);
 		} catch (FailedToUpdateSettingsException e) {
 			FailedToGetApartmentSettings ex = new FailedToGetApartmentSettings(
 					e.toString());
 			throw ex;
 		}
 
 		return settings;
 	}
 
 	public static void updateSettings(Settings settings)
 			throws UserNotLoggedInException, FailedToUpdateSettingsException {
		String apartmentID = RoommateDAL.getApartmentID();
		ParseObject parseSettings = new ParseObject("Settings");
 		//parseSettings.put("apartment", apartmentID);
 		parseSettings.put("username",ParseUser.getCurrentUser().getUsername());
 		parseSettings.put("newChoresHasBeenDivided",
 				settings.notifications.newChoresHasBeenDivided);
 		parseSettings.put("roommateFinishedChore",
 				settings.notifications.roommateFinishedChore);
 		parseSettings.put("roommateMissedChore",
 				settings.notifications.roommateMissedChore);
 		parseSettings.put("roommateStoleMyChore",
 				settings.notifications.roommateStoleMyChore);
 		parseSettings.put("disableRemindersAboutUpcomingChores",
 				settings.chores.disableRemindersAboutUpcomingChores);
 		parseSettings.put("forbidRoommatesFromTakingMyChores",
 				settings.chores.forbidRoommatesFromTakingMyChores);
 		parseSettings.put("reminderHours", settings.reminders.hours);
 		try {
 			parseSettings.save();
 		} catch (ParseException e) {
 			throw new FailedToUpdateSettingsException(e.toString());
 		}
 	}
 
 	private static Settings convertObjectToSettings(ParseObject obj) {
 		Settings settings = new Settings();
 
 		settings.notifications = settings.new Notifications();
 		settings.notifications.newChoresHasBeenDivided = obj
 				.getBoolean("newChoresHasBeenDivided");
 		settings.notifications.roommateFinishedChore = obj
 				.getBoolean("roommateFinishedChore");
 		settings.notifications.roommateMissedChore = obj
 				.getBoolean("roommateMissedChore");
 		settings.notifications.roommateStoleMyChore = obj
 				.getBoolean("roommateStoleMyChore");
 		settings.chores = settings.new Chores();
 		settings.chores.disableRemindersAboutUpcomingChores = obj
 				.getBoolean("disableRemindersAboutUpcomingChores");
 		settings.chores.forbidRoommatesFromTakingMyChores = obj
 				.getBoolean("forbidRoommatesFromTakingMyChores");
 
 		settings.reminders = settings.new Reminders();
 		settings.reminders.hours = obj.getInt("reminderHours");
 		return settings;
 	}
 
 }
