 package com.mareksebera.simpledilbert;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Random;
 
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTimeZone;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.DownloadManager;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.Toast;
 
 public class DilbertPreferences {
 
 	private SharedPreferences preferences;
 	private SharedPreferences.Editor editor;
 
 	private static final String PREF_CURRENT_DATE = "dilbert_current_date";
 	private static final String PREF_CURRENT_URL = "dilbert_current_url";
 	private static final String PREF_HIGH_QUALITY_ENABLED = "dilbert_use_high_quality";
 	private static final String TAG = "DilbertPreferences";
	public static final DateTimeZone TIME_ZONE = DateTimeZone.forOffsetHours(-3);
 
 	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat
 			.forPattern("yyyy-MM-dd");
 
 	@SuppressLint("CommitPrefEdits")
 	public DilbertPreferences(Context context) {
 		preferences = PreferenceManager.getDefaultSharedPreferences(context);
 		editor = preferences.edit();
 	}
 
 	public DateMidnight getCurrentDate() {
 		String savedDate = preferences.getString(PREF_CURRENT_DATE, null);
 		if (savedDate == null) {
 			return DateMidnight.now();
 		} else {
 			return DateMidnight.parse(savedDate, DATE_FORMATTER);
 		}
 	}
 
 	public boolean saveCurrentDate(DateMidnight currentDate) {
 		editor.putString(PREF_CURRENT_DATE,
 				currentDate.toString(DilbertPreferences.DATE_FORMATTER));
 		return editor.commit();
 	}
 
 	public String getLastUrl() {
 		return preferences.getString(PREF_CURRENT_URL, null);
 	}
 
 	public boolean isHighQualityOn() {
 		return preferences.getBoolean(PREF_HIGH_QUALITY_ENABLED, true);
 	}
 
 	public boolean saveCurrentUrl(String date, String s) {
 		editor.putString(PREF_CURRENT_URL, s);
 		editor.putString(date, s);
 		return editor.commit();
 	}
 
 	public boolean toggleHighQuality() {
 		return editor.putBoolean(PREF_HIGH_QUALITY_ENABLED, !isHighQualityOn())
 				.commit();
 	}
 
 	public String getCachedUrl(DateMidnight dateKey) {
 		return getCachedUrl(dateKey.toString(DATE_FORMATTER));
 	}
 
 	public String getCachedUrl(String dateKey) {
 		return preferences.getString(dateKey, null);
 	}
 
 	public boolean removeCache(DateMidnight currentDate) {
 		return editor.remove(
 				currentDate.toString(DilbertPreferences.DATE_FORMATTER))
 				.commit();
 	}
 
 	public boolean isFavorited(DateMidnight currentDay) {
 		return preferences.getBoolean(toFavoritedKey(currentDay), false);
 	}
 
 	public boolean toggleIsFavorited(DateMidnight currentDay) {
 		boolean newState = !isFavorited(currentDay);
 		editor.putBoolean(toFavoritedKey(currentDay), newState).commit();
 		return newState;
 	}
 
 	private String toFavoritedKey(DateMidnight currentDay) {
 		return "favorite_"
 				+ currentDay.toString(DilbertPreferences.DATE_FORMATTER);
 	}
 
 	public List<FavoritedItem> getFavoritedItems() {
 		List<FavoritedItem> favorites = new ArrayList<FavoritedItem>();
 		Map<String, ?> allPreferences = preferences.getAll();
 		for (String key : allPreferences.keySet()) {
 			if (key.startsWith("favorite_")
 					&& (Boolean) allPreferences.get(key)) {
 				String date = key.replace("favorite_", "");
 				favorites.add(new FavoritedItem(DateMidnight.parse(date,
 						DATE_FORMATTER), (String) allPreferences.get(date)));
 			}
 		}
 		return favorites;
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	public void downloadImageViaManager(final Activity activity,
 			final String downloadUrl, DateMidnight stripDate) {
 		try {
 			DownloadManager dm = (DownloadManager) activity
 					.getSystemService(Context.DOWNLOAD_SERVICE);
 			String url = toHighQuality(downloadUrl);
 			DownloadManager.Request request = new DownloadManager.Request(
 					Uri.parse(url));
 			request.setDestinationInExternalPublicDir(
 					Environment.DIRECTORY_DOWNLOADS,
 					DATE_FORMATTER.print(stripDate) + ".gif");
 			request.setVisibleInDownloadsUi(true);
 			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
 				request.allowScanningByMediaScanner();
 				request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
 			}
 			dm.enqueue(request);
 		} catch (Exception e) {
 			Log.e(TAG, "Should not happen", e);
 			Toast.makeText(activity, R.string.download_manager_unsupported,
 					Toast.LENGTH_LONG).show();
 		} catch (Error e) {
 			Log.e(TAG, "Should not happen", e);
 			Toast.makeText(activity, R.string.download_manager_unsupported,
 					Toast.LENGTH_LONG).show();
 		}
 	}
 
 	public String toHighQuality(String url) {
 		return url.replace(".gif", ".zoom.gif").replace("zoom.zoom", "zoom");
 	}
 
	public String toLowQuality(String url) {
 		return url.replace(".zoom.gif", ".gif").replace("zoom.zoom", "zoom");
 	}
 
 	public boolean saveDateForWidgetId(int appWidgetId, DateMidnight date) {
 		date = validateDate(date);
 		return editor.putString("widget_" + appWidgetId,
 				date.toString(DATE_FORMATTER)).commit();
 	}
 
 	public DateMidnight getDateForWidgetId(int appWidgetId) {
 		String savedDate = preferences.getString("widget_" + appWidgetId, null);
 		if (savedDate == null)
 			return DateMidnight.now();
 		else
 			return DateMidnight.parse(savedDate, DATE_FORMATTER);
 	}
 
 	public static DateMidnight getRandomDateMidnight() {
 		Random random = new Random();
 		DateMidnight now = DateMidnight.now();
 		int year = 1989 + random.nextInt(now.getYear() - 1989);
 		int month = 1 + random.nextInt(12);
 		int day = random.nextInt(31);
 		return DateMidnight.parse(
 				String.format(new Locale("en"), "%d-%d-1", year, month))
 				.plusDays(day);
 	}
 
 	/**
 	 * First strip was published on 16.4.1989
 	 * 
 	 * @see <a href="http://en.wikipedia.org/wiki/Dilbert">Wikipedia</a>
 	 * */
 	public static DateMidnight getFirstStripDate() {
 		return DateMidnight.parse("1989-04-16",
 				DilbertPreferences.DATE_FORMATTER);
 	}
 
 	public boolean deleteDateForWidgetId(int widgetId) {
 		return editor.remove("widget_" + widgetId).commit();
 	}
 
 	public static DateMidnight validateDate(DateMidnight selDate) {
 		if (selDate.isAfterNow()) {
 			selDate = DateMidnight.now(DilbertPreferences.TIME_ZONE);
 		}
 		if (selDate.isBefore(DilbertPreferences.getFirstStripDate())) {
 			selDate = DilbertPreferences.getFirstStripDate();
 		}
 		return selDate;
 	}
 
 }
