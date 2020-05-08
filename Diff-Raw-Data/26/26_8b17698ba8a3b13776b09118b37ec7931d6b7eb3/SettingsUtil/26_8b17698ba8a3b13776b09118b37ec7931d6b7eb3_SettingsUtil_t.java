 package com.bustiblelemons.tasque.main;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import com.bustiblelemons.tasque.R;
 import com.bustiblelemons.tasque.utilities.Database;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 public class SettingsUtil {
 
 	private static SharedPreferences getPreferencesFile(Context context) {
 		return PreferenceManager.getDefaultSharedPreferences(context);
 	}
 
 	public static int getTimeOut(Context context) {
 		return SettingsUtil.getPreferencesFile(context).getInt(context.getString(R.string.pref_time_out_key), 5);
 	}
 
 	public static boolean isFirstRun(Context context) {
 		return SettingsUtil.getPreferencesFile(context).getBoolean(context.getString(R.string.pref_first_run), true);
 	}
 
 	public static void firstRunDone(Context context) {
 		SettingsUtil.getPreferencesFile(context).edit().putBoolean(context.getString(R.string.pref_first_run), false)
 				.commit();
 	}
 
 	public static boolean hideKeyboard(Context context) {
 		return SettingsUtil.getPreferencesFile(context).getBoolean(context.getString(R.string.pref_hide_keyboard_key),
 				false);
 	}
 
 	public static boolean autoCap(Context context) {
 		return SettingsUtil.getPreferencesFile(context).getBoolean(
 				context.getString(R.string.pref_auto_cap_characters_key), true);
 	}
 
 	public static void setDefaultCategoryId(Context context, int categoryID) {
 		SettingsUtil.getPreferencesFile(context).edit()
 				.putInt(context.getString(R.string.pref_default_category_key), categoryID).commit();
 	}
 
 	/**
 	 * 
 	 * @param context
 	 * @return 0 if not found
 	 */
 	public static int getDefaultCategoryId(Context context) {
 		return SettingsUtil.getPreferencesFile(context)
 				.getInt(context.getString(R.string.pref_default_category_key), 0);
 	}
 
 	public static boolean isDefaultCategory(Context context, int categoryId) {
 		return (SettingsUtil.getDefaultCategoryId(context) == categoryId) ? true : false;
 	}
 
 	public static void setSelectedCategories(Context context, Iterable<String> categories) {
 		JSONArray json = new JSONArray();
 		for (String cat : categories) {
 			json.put(cat);
 		}
 		SettingsUtil.getPreferencesFile(context).edit()
 				.putString(context.getString(R.string.pref_all_selected_categories_key), json.toString()).commit();
 	}
 
 	public static ArrayList<String> getSelectedCategories(Context context) {
 		ArrayList<String> ret = new ArrayList<String>();
 		String JSONCategories = SettingsUtil.getPreferencesFile(context).getString(
 				context.getString(R.string.pref_all_selected_categories_key), "");
 		try {
 			JSONArray array = new JSONArray(JSONCategories);
 			for (int i = 0; i < array.length(); i++) {
 				ret.add(array.getString(i));
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	public static float getListFontSize(Context context) {
 		String _r = SettingsUtil.getPreferencesFile(context).getString(
 				context.getString(R.string.pref_font_size_list_key), "13");
 		return Float.valueOf(_r);
 	}
 
 	public static float getInputFontSize(Context context) {
 		String _r = SettingsUtil.getPreferencesFile(context).getString(
 				context.getString(R.string.pref_font_size_input_key), "13");
 		return Float.valueOf(_r);
 	}
 
 	public static float getCompletionDateFontSize(Context context) {
 		return Float.valueOf(SettingsUtil.getPreferencesFile(context).getString(
 				context.getString(R.string.pref_font_size_completion_date_key), "13"));
 	}
 
 	public static float getDueDateFontSize(Context context) {
 		return Float.valueOf(SettingsUtil.getPreferencesFile(context).getString(
 				context.getString(R.string.pref_font_size_due_date_key), "13"));
 	}
 
 	public static void setSelectedCategoriesToAll(Context context) {
 		ArrayList<String> categories = Database.getAllCategoryIDS(context);
 		SettingsUtil.setSelectedCategories(context, categories);
 	}
 
 	public static boolean showDate(Context context) {
 		return SettingsUtil.getPreferencesFile(context).getBoolean(context.getString(R.string.pref_date_show_date_key),
 				false);
 	}
 
 	public static String getDateFromat(Context context) {
 		String r = SettingsUtil.getPreferencesFile(context).getString(context.getString(R.string.pref_date_format_key),
 				"");
 		return SettingsUtil.showHour(context) ? r + SettingsUtil.getHourFormat(context) : r;
 	}
 
 	public static String getHourFormat(Context context) {
 		return SettingsUtil.hourFormatAMPM(context) ? " h:mm a" : " HH:mm";
 	}
 
 	public static boolean hourFormatAMPM(Context context) {
 		return SettingsUtil.getPreferencesFile(context).getBoolean(
 				context.getString(R.string.pref_date_format_show_hour_ampm_key), false);
 	}
 
 	public static boolean showHour(Context context) {
 		return SettingsUtil.getPreferencesFile(context).getBoolean(
 				context.getString(R.string.pref_date_format_show_hour_key), false);
 	}
 
 	public static boolean syncedDatabsePathSaved(Context context) {
 		return SettingsUtil.getSyncedDatabasePath(context).length() > 0 ? true : false;
 	}
 
 	/**
 	 * 
 	 * @param context
 	 * @return zero-length is not found. If is saved the absolute path
 	 */
 	public static String getSyncedDatabasePath(Context context) {
 		return SettingsUtil.getPreferencesFile(context).getString(
 				context.getString(R.string.pref_database_external_path_key), "");
 	}
 
 	public static void setSyncedDatabsePath(Context context, String path) {
 		SettingsUtil.getPreferencesFile(context).edit()
 				.putString(context.getString(R.string.pref_database_external_path_key), path).commit();
 	}
 
 	public static void setExportOnExit(Context context, boolean exportToExternal) {
 		SettingsUtil.getPreferencesFile(context).edit()
 				.putBoolean(context.getString(R.string.pref_export_database_key), exportToExternal).commit();
 	}
 
 	public static boolean getExportOnExit(Context context) {
 		return SettingsUtil.getPreferencesFile(context).getBoolean(
 				context.getString(R.string.pref_export_database_key), true);
 	}
 
 	public static boolean getStartedFresh(Context context) {
 		return SettingsUtil.getPreferencesFile(context).getBoolean(context.getString(R.string.pref_started_fresh),
 				false);
 	}
 
 	public static void setStartedFresh(Context context, boolean startedFresh) {
 		SettingsUtil.getPreferencesFile(context).edit()
 				.putBoolean(context.getString(R.string.pref_started_fresh), startedFresh).commit();
 	}
 
 	public static boolean useColours(Context context) {
 		return SettingsUtil.getPreferencesFile(context).getBoolean(context.getString(R.string.pref_use_color_key),
 				false);
 	}
 
 	public static int getTodayColor(Context context) {
 		return SettingsUtil.getPreferencesFile(context).getInt(context.getString(R.string.pref_color_today_key),
 				context.getResources().getColor(R.color.default_today_color));
 	}
 
 	public static int getOverdueColor(Context context) {
 		return SettingsUtil.getPreferencesFile(context).getInt(context.getString(R.string.pref_color_today_key),
 				context.getResources().getColor(R.color.default_overdue_color));
 	}

	public static boolean startedFresh(Context context) {
		return SettingsUtil.getStartedFresh(context);
	}
 }
