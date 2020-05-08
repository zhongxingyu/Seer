 package il.ac.huji.chores.dal;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.PushService;
 import il.ac.huji.chores.Settings;
 import il.ac.huji.chores.exceptions.UserNotLoggedInException;
 
 import java.util.List;
 
 public class ApartmentSettingsDAL {
 
 	public static Settings getSettings() throws UserNotLoggedInException,
 			ParseException {
 		String username = RoommateDAL.getRoomateUsername();
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("Settings");
 		query.whereEqualTo("username", username);
 		ParseObject result;
 		List<ParseObject> results = query.find();
 		if (results.size() == 0) {
 			return getDefaultSettings(true);
 		} else {
 			result = results.get(0);
 			return convertObjectToSettings(result);
 		}
 
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
 
 	public static Settings getDefaultSettings(boolean update)
 			throws UserNotLoggedInException, ParseException {
 
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
 		if (update) {
 			updateSettings(settings, true);
 		}
 		return settings;
 	}
 
 	// isDefault - true if it's an update of the default settings
 	public static void updateSettings(Settings settings, boolean isDefault)
 			throws UserNotLoggedInException, ParseException {
 		String username = RoommateDAL.getRoomateUsername();
 		ParseObject parseSettings = null;
 		if (!isDefault) {
 			parseSettings = getParseSettings(username);
 		}
 		if (parseSettings == null) {
 			parseSettings = new ParseObject("Settings");
 		}
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
 		parseSettings.save();
 
 	}
 
 	private static ParseObject getParseSettings(String username)
 			throws ParseException {
 		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Settings");
 		query.whereEqualTo("username", username);
 		List<ParseObject> parseSettingsList = null;
 		parseSettingsList = query.find();
 		if (parseSettingsList.size() == 0) {
 			return null;
 		}
 		return parseSettingsList.get(0);
 
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
