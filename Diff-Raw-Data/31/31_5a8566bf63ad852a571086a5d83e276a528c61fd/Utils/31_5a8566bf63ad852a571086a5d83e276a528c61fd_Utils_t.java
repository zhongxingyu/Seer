 package org.mariotaku.twidere.util;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.mariotaku.twidere.Constants;
 import org.mariotaku.twidere.R;
 import org.mariotaku.twidere.provider.TweetStore;
 import org.mariotaku.twidere.provider.TweetStore.Accounts;
 import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
 import org.mariotaku.twidere.provider.TweetStore.Filters;
 import org.mariotaku.twidere.provider.TweetStore.Mentions;
 import org.mariotaku.twidere.provider.TweetStore.Statuses;
 import org.mariotaku.twidere.service.TwidereService;
 
 import twitter4j.GeoLocation;
 import twitter4j.HashtagEntity;
 import twitter4j.MediaEntity;
 import twitter4j.Status;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.URLEntity;
 import twitter4j.User;
 import twitter4j.UserMentionEntity;
 import twitter4j.auth.AccessToken;
 import twitter4j.auth.BasicAuthorization;
 import twitter4j.conf.ConfigurationBuilder;
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.ContextWrapper;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.UriMatcher;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.PorterDuff.Mode;
 import android.graphics.Rect;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Build;
 import android.provider.MediaStore;
 import android.text.Html;
 import android.text.SpannableString;
 import android.text.Spanned;
 import android.text.format.DateUtils;
 import android.text.format.Time;
 import android.text.style.URLSpan;
 import android.util.Log;
 import android.view.View;
 import android.widget.Toast;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 public final class Utils implements Constants {
 
 	private static HashMap<Context, ServiceBinder> mConnectionMap = new HashMap<Context, ServiceBinder>();
 
 	private static UriMatcher CONTENT_PROVIDER_URI_MATCHER;
 
 	static {
 		CONTENT_PROVIDER_URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
 		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_STATUSES, URI_STATUSES);
 		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_ACCOUNTS, URI_ACCOUNTS);
 		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_MENTIONS, URI_MENTIONS);
 		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_FAVORITES, URI_FAVORITES);
 		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_CACHED_USERS, URI_CACHED_USERS);
 		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_FILTERED_USERS, URI_FILTERED_USERS);
 		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_FILTERED_KEYWORDS, URI_FILTERED_KEYWORDS);
 		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_FILTERED_SOURCES, URI_FILTERED_SOURCES);
 		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_STATUSES + "/*", URI_USER_TIMELINE);
 	}
 
 	private static HashMap<Long, Integer> sAccountColors = new HashMap<Long, Integer>();
 
 	private static final String IMAGE_URL_PATTERN = "href=\\s*[\\\"'](http(s?):\\/\\/.+?(?i)(png|jpeg|jpg|gif|bmp))[\\\"']\\s*";
 
 	private static final Uri[] STATUSES_URIS = new Uri[] { Statuses.CONTENT_URI, Mentions.CONTENT_URI };
 
 	public static ServiceToken bindToService(Context context) {
 
 		return bindToService(context, null);
 	}
 
 	public static ServiceToken bindToService(Context context, ServiceConnection callback) {
 
 		ContextWrapper cw = new ContextWrapper(context);
 		cw.startService(new Intent(cw, TwidereService.class));
 		ServiceBinder sb = new ServiceBinder(callback);
 		if (cw.bindService(new Intent(cw, TwidereService.class), sb, 0)) {
 			mConnectionMap.put(cw, sb);
 			return new ServiceToken(cw);
 		}
 		Log.e(LOGTAG, "Failed to bind to service");
 		return null;
 	}
 
 	public static String buildActivatedStatsWhereClause(Context context, String selection) {
 		long[] account_ids = getActivatedAccounts(context);
 		StringBuilder builder = new StringBuilder();
 		if (selection != null) {
 			builder.append(selection);
 			builder.append(" AND ");
 		}
 
 		builder.append(Statuses.ACCOUNT_ID + " IN ( ");
 		for (int i = 0; i < account_ids.length; i++) {
 			String id_string = String.valueOf(account_ids[i]);
 			if (id_string != null) {
 				if (i > 0) {
 					builder.append(", ");
 				}
 				builder.append(id_string);
 			}
 		}
 		builder.append(" )");
 
 		return builder.toString();
 	}
 
 	public static String buildFilterWhereClause(String table, String selection) {
 		StringBuilder builder = new StringBuilder();
 		if (selection != null) {
 			builder.append(selection);
 			builder.append(" AND ");
 		}
 		builder.append(Statuses._ID + " NOT IN ( ");
 		builder.append("SELECT DISTINCT " + table + "." + Statuses._ID + " FROM " + table);
 		builder.append(" WHERE " + table + "." + Statuses.SCREEN_NAME + " IN ( SELECT " + TABLE_FILTERED_USERS + "."
 				+ Filters.Users.TEXT + " FROM " + TABLE_FILTERED_USERS + " )");
 		builder.append(" AND " + table + "." + Statuses.IS_GAP + " IS NULL");
 		builder.append(" OR " + table + "." + Statuses.IS_GAP + " == 0");
 		builder.append(" UNION ");
 		builder.append("SELECT DISTINCT " + table + "." + Statuses._ID + " FROM " + table);
 		builder.append(" WHERE " + table + "." + Statuses.NAME + " IN ( SELECT " + TABLE_FILTERED_USERS + "."
 				+ Filters.Users.TEXT + " FROM " + TABLE_FILTERED_USERS + " )");
 		builder.append(" AND " + table + "." + Statuses.IS_GAP + " IS NULL");
 		builder.append(" OR " + table + "." + Statuses.IS_GAP + " == 0");
 		builder.append(" UNION ");
 		builder.append("SELECT DISTINCT " + table + "." + Statuses._ID + " FROM " + table + ", "
 				+ TABLE_FILTERED_SOURCES);
 		builder.append(" WHERE " + table + "." + Statuses.SOURCE + " LIKE '%'||" + TABLE_FILTERED_SOURCES + "."
 				+ Filters.Sources.TEXT + "||'%'");
 		builder.append(" AND " + table + "." + Statuses.IS_GAP + " IS NULL");
 		builder.append(" OR " + table + "." + Statuses.IS_GAP + " == 0");
 		builder.append(" UNION ");
 		builder.append("SELECT DISTINCT " + table + "." + Statuses._ID + " FROM " + table + ", "
 				+ TABLE_FILTERED_KEYWORDS);
 		builder.append(" WHERE " + table + "." + Statuses.TEXT + " LIKE '%'||" + TABLE_FILTERED_KEYWORDS + "."
 				+ Filters.Keywords.TEXT + "||'%'");
 		builder.append(" AND " + table + "." + Statuses.IS_GAP + " IS NULL");
 		builder.append(" OR " + table + "." + Statuses.IS_GAP + " == 0");
 		builder.append(" )");
 
 		return builder.toString();
 	}
 
 	public static synchronized void cleanDatabasesByItemLimit(Context context) {
 		ContentResolver resolver = context.getContentResolver();
 		String[] cols = new String[0];
 		int item_limit = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).getInt(
 				PREFERENCE_KEY_DATABASE_ITEM_LIMIT, PREFERENCE_DEFAULT_DATABASE_ITEM_LIMIT);
 
 		for (long account_id : getAccountIds(context)) {
 			// Clean statuses.
 			for (Uri uri : STATUSES_URIS) {
 				Cursor cur = resolver.query(uri, cols, Statuses.ACCOUNT_ID + "=" + account_id, null,
 						Statuses.DEFAULT_SORT_ORDER);
 				if (cur != null && cur.getCount() > item_limit) {
 					cur.moveToPosition(item_limit - 1);
 					int _id = cur.getInt(cur.getColumnIndexOrThrow(Statuses._ID));
 					resolver.delete(uri, Statuses._ID + "<" + _id, null);
 				}
 				if (cur != null) {
 					cur.close();
 				}
 			}
 		}
 		// Clean cached users.
 		{
 			Uri uri = CachedUsers.CONTENT_URI;
 			Cursor cur = resolver.query(uri, cols, null, null, null);
 			if (cur != null && cur.getCount() > item_limit * 4) {
 				cur.moveToPosition(item_limit - 1);
 				int _id = cur.getInt(cur.getColumnIndexOrThrow(Statuses._ID));
 				resolver.delete(uri, Statuses._ID + "<" + _id, null);
 			}
 			if (cur != null) {
 				cur.close();
 			}
 		}
 	}
 
 	public static void clearAccountColor() {
 		sAccountColors.clear();
 	}
 
 	/**
 	 * 
 	 * @param location
 	 * 
 	 * @return Location in "[longitude],[latitude]" format.
 	 */
 	public static String formatGeoLocationToString(GeoLocation location) {
 		if (location == null) return null;
 		return location.getLatitude() + "," + location.getLongitude();
 	}
 
 	public static String formatStatusString(Status status, long account_id) {
 		final CharSequence TAG_START = "<p>";
 		final CharSequence TAG_END = "</p>";
 		if (status == null || status.getText() == null) return "";
 		SpannableString text = new SpannableString(status.getText());
 		// Format links.
 		URLEntity[] urls = status.getURLEntities();
 		if (urls != null) {
 			for (URLEntity url_entity : urls) {
 				int start = url_entity.getStart();
 				int end = url_entity.getEnd();
 				if (start < 0 || end > text.length()) {
 					continue;
 				}
 				URL expanded_url = url_entity.getExpandedURL();
 				URL url = url_entity.getURL();
 				if (expanded_url != null) {
 					text.setSpan(new URLSpan(expanded_url.toString()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
 				} else if (url != null) {
 					text.setSpan(new URLSpan(url.toString()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
 				}
 			}
 		}
 		// Format mentioned users.
 		UserMentionEntity[] mentions = status.getUserMentionEntities();
 		if (mentions != null) {
 			for (UserMentionEntity mention : mentions) {
 				int start = mention.getStart();
 				int end = mention.getEnd();
 				if (start < 0 || end > text.length()) {
 					continue;
 				}
 				Uri.Builder builder = new Uri.Builder();
 				builder.scheme(SCHEME_TWIDERE);
				builder.authority(AUTHORITY_USER);
 				builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
 				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, mention.getScreenName());
 				text.setSpan(new URLSpan(builder.build().toString()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
 			}
 		}
 		// Format hashtags.
 		HashtagEntity[] hashtags = status.getHashtagEntities();
 		if (hashtags != null) {
 			for (HashtagEntity hashtag : hashtags) {
 				int start = hashtag.getStart();
 				int end = hashtag.getEnd();
 				if (start < 0 || end > text.length()) {
 					continue;
 				}
 				String link = "https://twitter.com/search/#" + hashtag.getText();
 				text.setSpan(new URLSpan(link), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
 			}
 		}
 		// Format media.
 		MediaEntity[] media = status.getMediaEntities();
 		if (media != null) {
 			for (MediaEntity media_item : media) {
 				int start = media_item.getStart();
 				int end = media_item.getEnd();
 				if (start < 0 || end > text.length()) {
 					continue;
 				}
 				URL media_url = media_item.getMediaURL();
 				if (media_url != null) {
 					text.setSpan(new URLSpan(media_url.toString()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
 				}
 			}
 		}
 		String formatted = Html.toHtml(text);
 		if (formatted != null && formatted.contains(TAG_START) && formatted.contains(TAG_END)) {
 			int start = formatted.indexOf(TAG_START.toString()) + TAG_START.length();
 			int end = formatted.lastIndexOf(TAG_END.toString());
 			return formatted.substring(start, end);
 		}
 		return formatted;
 	}
 
 	public static String formatTimeStampString(Context context, long timestamp) {
 		Time then = new Time();
 		then.set(timestamp);
 		Time now = new Time();
 		now.setToNow();
 
 		int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT | DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_CAP_AMPM;
 
 		if (then.year != now.year) {
 			format_flags |= DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE;
 		} else if (then.yearDay != now.yearDay) {
 			format_flags |= DateUtils.FORMAT_SHOW_DATE;
 		} else {
 			format_flags |= DateUtils.FORMAT_SHOW_TIME;
 		}
 
 		return DateUtils.formatDateTime(context, timestamp, format_flags);
 	}
 
 	@SuppressWarnings("deprecation")
 	public static String formatTimeStampString(Context context, String date_time) {
 		return formatTimeStampString(context, Date.parse(date_time));
 	}
 
 	public static String formatToLongTimeString(Context context, long timestamp) {
 		Time then = new Time();
 		then.set(timestamp);
 		Time now = new Time();
 		now.setToNow();
 
 		int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT | DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_CAP_AMPM;
 
 		format_flags |= DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME;
 
 		return DateUtils.formatDateTime(context, timestamp, format_flags);
 	}
 
 	public static String formatToShortTimeString(Context context, long timestamp) {
 		final Resources res = context.getResources();
 		final Time then = new Time(), now = new Time();
 		then.set(timestamp);
 		now.setToNow();
 		if (then.before(now)) {
 
 			int year_diff = now.year - then.year;
 
 			int month_diff = (year_diff > 0 ? 12 : 0) + now.month - then.month;
 			if (year_diff < 1) {
 				int day_diff = (month_diff > 0 ? then.getActualMaximum(Time.MONTH_DAY) : 0) + now.monthDay
 						- then.monthDay;
 				if (month_diff < 1) {
 					if (day_diff >= then.getActualMaximum(Time.MONTH_DAY))
 						return res.getQuantityString(R.plurals.Nmonths, month_diff, month_diff);
 					int hour_diff = (day_diff > 0 ? 24 : 0) + now.hour - then.hour;
 					if (day_diff < 1) {
 						if (hour_diff >= 24) return res.getQuantityString(R.plurals.Ndays, day_diff, day_diff);
 						int minute_diff = (hour_diff > 0 ? 60 : 0) + now.minute - then.minute;
 						if (hour_diff < 1) {
 							if (minute_diff >= 60)
 								return res.getQuantityString(R.plurals.Nhours, hour_diff, hour_diff);
 							if (minute_diff <= 1) return context.getString(R.string.just_now);
 							return res.getQuantityString(R.plurals.Nminutes, minute_diff, minute_diff);
 						} else if (hour_diff == 1) {
 							if (minute_diff < 60)
 								return res.getQuantityString(R.plurals.Nminutes, minute_diff, minute_diff);
 						}
 						return res.getQuantityString(R.plurals.Nhours, hour_diff, hour_diff);
 					} else if (day_diff == 1) {
 						if (hour_diff < 24) return res.getQuantityString(R.plurals.Nhours, hour_diff, hour_diff);
 					}
 					return res.getQuantityString(R.plurals.Ndays, day_diff, day_diff);
 				} else if (month_diff == 1) {
 					if (day_diff < then.getActualMaximum(Time.MONTH_DAY))
 						return res.getQuantityString(R.plurals.Ndays, day_diff, day_diff);
 				}
 				return res.getQuantityString(R.plurals.Nmonths, month_diff, month_diff);
 			} else if (year_diff == 1) {
 				if (month_diff < 12) return res.getQuantityString(R.plurals.Nmonths, month_diff, month_diff);
 			}
 			return res.getQuantityString(R.plurals.Nyears, year_diff, year_diff);
 		}
 		return then.format3339(true);
 	}
 
 	public static int getAccountColor(Context context, long account_id) {
 
 		Integer color = sAccountColors.get(account_id);
 		if (color == null) {
 			Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, new String[] { Accounts.USER_COLOR },
 					Accounts.USER_ID + "=" + account_id, null, null);
 			if (cur == null) return Color.TRANSPARENT;
 			if (cur.getCount() <= 0) {
 				cur.close();
 				return Color.TRANSPARENT;
 			}
 			cur.moveToFirst();
 			color = cur.getInt(cur.getColumnIndexOrThrow(Accounts.USER_COLOR));
 			cur.close();
 			sAccountColors.put(account_id, color);
 		}
 		return color;
 	}
 
 	public static long getAccountIdForStatusId(Context context, long status_id) {
 
 		String[] cols = new String[] { Statuses.ACCOUNT_ID };
 		String where = Statuses.STATUS_ID + " = " + status_id;
 
 		for (Uri uri : STATUSES_URIS) {
 			Cursor cur = context.getContentResolver().query(uri, cols, where, null, null);
 			if (cur == null) {
 				continue;
 			}
 			if (cur.getCount() > 0) {
 				cur.moveToFirst();
 				long id = cur.getLong(cur.getColumnIndexOrThrow(Statuses.ACCOUNT_ID));
 				cur.close();
 				return id;
 			}
 			cur.close();
 		}
 		return -1;
 	}
 
 	public static long[] getAccountIds(Context context) {
 		long[] accounts = new long[] {};
 		Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, new String[] { Accounts.USER_ID }, null,
 				null, null);
 		if (cur != null) {
 			int idx = cur.getColumnIndexOrThrow(Accounts.USER_ID);
 			cur.moveToFirst();
 			accounts = new long[cur.getCount()];
 			int i = 0;
 			while (!cur.isAfterLast()) {
 				accounts[i] = cur.getLong(idx);
 				cur.moveToNext();
 			}
 			cur.close();
 		}
 		return accounts;
 	}
 
 	public static String getAccountUsername(Context context, long account_id) {
 
 		Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, new String[] { Accounts.USERNAME },
 				Accounts.USER_ID + "=" + account_id, null, null);
 		if (cur == null) return null;
 		if (cur.getCount() <= 0) {
 			cur.close();
 			return null;
 		}
 		cur.moveToFirst();
 		String username = cur.getString(cur.getColumnIndexOrThrow(Accounts.USERNAME));
 		cur.close();
 		return username;
 	}
 
 	public static long[] getActivatedAccounts(Context context) {
 		long[] accounts = new long[] {};
 		Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, new String[] { Accounts.USER_ID },
 				Accounts.IS_ACTIVATED + "=1", null, null);
 		if (cur != null) {
 			int idx = cur.getColumnIndexOrThrow(Accounts.USER_ID);
 			cur.moveToFirst();
 			accounts = new long[cur.getCount()];
 			int i = 0;
 			while (!cur.isAfterLast()) {
 				accounts[i] = cur.getLong(idx);
 				i++;
 				cur.moveToNext();
 			}
 			cur.close();
 		}
 		return accounts;
 	}
 
 	public static Bitmap getColorPreviewBitmap(Context context, int color) {
 
 		float density = context.getResources().getDisplayMetrics().density;
 		int width = (int) (32 * density), height = (int) (32 * density);
 
 		Bitmap bm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
 		Canvas canvas = new Canvas(bm);
 
 		int rectrangle_size = (int) (density * 5);
 		int numRectanglesHorizontal = (int) Math.ceil(width / rectrangle_size);
 		int numRectanglesVertical = (int) Math.ceil(height / rectrangle_size);
 		Rect r = new Rect();
 		boolean verticalStartWhite = true;
 		for (int i = 0; i <= numRectanglesVertical; i++) {
 
 			boolean isWhite = verticalStartWhite;
 			for (int j = 0; j <= numRectanglesHorizontal; j++) {
 
 				r.top = i * rectrangle_size;
 				r.left = j * rectrangle_size;
 				r.bottom = r.top + rectrangle_size;
 				r.right = r.left + rectrangle_size;
 				Paint paint = new Paint();
 				paint.setColor(isWhite ? Color.WHITE : Color.GRAY);
 
 				canvas.drawRect(r, paint);
 
 				isWhite = !isWhite;
 			}
 
 			verticalStartWhite = !verticalStartWhite;
 
 		}
 		canvas.drawColor(color);
 		Paint paint = new Paint();
 		paint.setColor(Color.WHITE);
 		paint.setStrokeWidth(2.0f);
 		float[] points = new float[] { 0, 0, width, 0, 0, 0, 0, height, width, 0, width, height, 0, height, width,
 				height };
 		canvas.drawLines(points, paint);
 
 		return bm;
 	}
 
 	public static int getErrorCode(TwitterException e) {
 		if (e == null) return RESULT_UNKNOWN_ERROR;
 		int status_code = e.getStatusCode();
 		if (status_code == -1)
 			return RESULT_CONNECTIVITY_ERROR;
 		else if (status_code >= 401 && status_code < 404)
 			return RESULT_NO_PERMISSION;
 		else if (status_code >= 404 && status_code < 500)
 			return RESULT_BAD_ADDRESS;
 		else if (status_code >= 500 && status_code < 600)
 			return RESULT_SERVER_ERROR;
 		else
 			return RESULT_UNKNOWN_ERROR;
 	}
 
 	public static GeoLocation getGeoLocationFromString(String location) {
 		if (location == null) return null;
 		String[] longlat = location.split(",");
 		if (longlat == null || longlat.length != 2) return null;
 		try {
 			return new GeoLocation(Double.valueOf(longlat[0]), Double.valueOf(longlat[1]));
 		} catch (NumberFormatException e) {
 			return null;
 		}
 	}
 
 	public static URL[] getImageLinksForText(CharSequence text) {
 
 		final Pattern pattern = Pattern.compile(IMAGE_URL_PATTERN);
 		final Matcher matcher = pattern.matcher(text);
 		final List<URL> image_links = new ArrayList<URL>();
 		while (matcher.find()) {
 			String link_string = matcher.group(1);
 			if (link_string == null) {
 				continue;
 			}
 			URL link = null;
 			try {
 				link = new URL(link_string);
 			} catch (MalformedURLException e) {
 
 			}
 			if (link == null) {
 				continue;
 			}
 			if (!image_links.contains(link)) {
 				image_links.add(link);
 			}
 		}
 		return image_links.toArray(new URL[image_links.size()]);
 	}
 
 	public static String getImagePathFromUri(Context context, Uri uri) {
 		if (uri == null) return null;
 
 		String media_uri_start = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString();
 
 		if (uri.toString().startsWith(media_uri_start)) {
 
 			String[] proj = { MediaStore.Images.Media.DATA };
 			Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
 
 			if (cursor == null || cursor.getCount() <= 0) return null;
 
 			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 
 			cursor.moveToFirst();
 
 			String path = cursor.getString(column_index);
 			cursor.close();
 			return path;
 		} else if (uri.getScheme().equals("file")) return uri.getPath();
 		return null;
 	}
 
 	public static long[] getLastStatusIds(Context context, Uri uri) {
 		long[] account_ids = getActivatedAccounts(context);
 		String[] cols = new String[] { Statuses.STATUS_ID };
 		ContentResolver resolver = context.getContentResolver();
 		long[] status_ids = new long[account_ids.length];
 		int idx = 0;
 		for (long account_id : account_ids) {
 			String where = Statuses.ACCOUNT_ID + " = " + account_id;
 			Cursor cur = resolver.query(uri, cols, where, null, Statuses.STATUS_ID);
 			if (cur == null) {
 				continue;
 			}
 
 			if (cur.getCount() > 0) {
 				cur.moveToFirst();
 				status_ids[idx] = cur.getLong(cur.getColumnIndexOrThrow(Statuses.STATUS_ID));
 			}
 			cur.close();
 			idx++;
 		}
 		return status_ids;
 	}
 
 	public static String[] getMentionedNames(CharSequence user_name, CharSequence text, boolean at_sign,
 			boolean include_author) {
 		Pattern pattern = Pattern.compile("(?<!\\w)(@(\\w+))", Pattern.MULTILINE);
 		Matcher matcher = pattern.matcher(text);
 		List<String> mentions = new ArrayList<String>();
 
 		if (include_author) {
 			mentions.add((at_sign ? "@" : "") + user_name);
 		}
 
 		while (matcher.find()) {
 			String mention = matcher.group(at_sign ? 1 : 2);
 			if (mentions.contains(mention)) {
 				continue;
 			}
 			mentions.add(mention);
 		}
 		return mentions.toArray(new String[mentions.size()]);
 	}
 
 	public static String getNameForStatusId(Context context, long status_id) {
 
 		String[] cols = new String[] { Statuses.NAME };
 		String where = Statuses.STATUS_ID + " = " + status_id;
 
 		for (Uri uri : STATUSES_URIS) {
 			Cursor cur = context.getContentResolver().query(uri, cols, where, null, null);
 			if (cur == null) {
 				continue;
 			}
 			if (cur.getCount() > 0) {
 				cur.moveToFirst();
 				String name = cur.getString(cur.getColumnIndexOrThrow(Statuses.NAME));
 				cur.close();
 				return name;
 			}
 			cur.close();
 		}
 		return null;
 	}
 
 	public static long getRetweetedByUserId(Context context, long status_id) {
 		String[] cols = new String[] { Statuses.RETWEETED_BY_ID };
 		String where = Statuses.STATUS_ID + "=" + status_id;
 
 		for (Uri uri : STATUSES_URIS) {
 			Cursor cur = context.getContentResolver().query(uri, cols, where, null, null);
 			if (cur == null) {
 				continue;
 			}
 			if (cur.getCount() > 0) {
 				cur.moveToFirst();
 				long retweeted_by_id = cur.getLong(cur.getColumnIndexOrThrow(Statuses.RETWEETED_BY_ID));
 				cur.close();
 				return retweeted_by_id;
 			}
 			cur.close();
 		}
 		return -1;
 	}
 
 	public static long getRetweetId(Context context, long status_id) {
 		String[] cols = new String[] { Statuses.RETWEET_ID };
 		String where = Statuses.STATUS_ID + "=" + status_id;
 		for (Uri uri : STATUSES_URIS) {
 			Cursor cur = context.getContentResolver().query(uri, cols, where, null, null);
 			if (cur == null) {
 				continue;
 			}
 			if (cur.getCount() > 0) {
 				cur.moveToFirst();
 				long retweet_id = cur.getLong(cur.getColumnIndexOrThrow(Statuses.RETWEET_ID));
 				cur.close();
 				return retweet_id;
 			}
 			cur.close();
 		}
 		return -1;
 	}
 
 	public static String getScreenNameForStatusId(Context context, long status_id) {
 		String[] cols = new String[] { Statuses.SCREEN_NAME };
 		String where = Statuses.STATUS_ID + " = " + status_id;
 
 		for (Uri uri : STATUSES_URIS) {
 			Cursor cur = context.getContentResolver().query(uri, cols, where, null, null);
 			if (cur == null) {
 				continue;
 			}
 			if (cur.getCount() > 0) {
 				cur.moveToFirst();
 				String name = cur.getString(cur.getColumnIndexOrThrow(Statuses.SCREEN_NAME));
 				cur.close();
 				return name;
 			}
 			cur.close();
 		}
 		return null;
 	}
 
 	public static int getTableId(Uri uri) {
 		return CONTENT_PROVIDER_URI_MATCHER.match(uri);
 	}
 
 	public static String getTableNameForContentUri(Uri uri) {
 		switch (getTableId(uri)) {
 			case URI_STATUSES:
 				return TABLE_STATUSES;
 			case URI_ACCOUNTS:
 				return TABLE_ACCOUNTS;
 			case URI_MENTIONS:
 				return TABLE_MENTIONS;
 			case URI_CACHED_USERS:
 				return TABLE_CACHED_USERS;
 			case URI_FILTERED_USERS:
 				return TABLE_FILTERED_USERS;
 			case URI_FILTERED_KEYWORDS:
 				return TABLE_FILTERED_KEYWORDS;
 			case URI_FILTERED_SOURCES:
 				return TABLE_FILTERED_SOURCES;
 			default:
 				return null;
 		}
 	}
 
 	public static Twitter getTwitterInstance(Context context, long account_id, boolean include_entities) {
 		final SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
 		final boolean enable_gzip_compressing = preferences.getBoolean(PREFERENCE_KEY_GZIP_COMPRESSING, false);
 		Twitter twitter = null;
 		StringBuilder where = new StringBuilder();
 		where.append(Accounts.USER_ID + "=" + account_id);
 		Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, Accounts.COLUMNS, where.toString(), null,
 				null);
 		if (cur != null) {
 			if (cur.getCount() == 1) {
 				cur.moveToFirst();
 				ConfigurationBuilder cb = new ConfigurationBuilder();
 				cb.setGZIPEnabled(enable_gzip_compressing);
 				String rest_api_base = cur.getString(cur.getColumnIndexOrThrow(Accounts.REST_API_BASE));
 				String search_api_base = cur.getString(cur.getColumnIndexOrThrow(Accounts.SEARCH_API_BASE));
 				if (rest_api_base == null || "".equals(rest_api_base)) {
 					rest_api_base = DEFAULT_REST_API_BASE;
 				}
 				if (search_api_base == null || "".equals(search_api_base)) {
 					search_api_base = DEFAULT_SEARCH_API_BASE;
 				}
 				cb.setRestBaseURL(rest_api_base);
 				cb.setSearchBaseURL(search_api_base);
 				cb.setIncludeEntitiesEnabled(include_entities);
 				try {
 					String version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
 					cb.setClientVersion(version);
 				} catch (NameNotFoundException e) {
 					// This should not happen.
 				}
 
 				switch (cur.getInt(cur.getColumnIndexOrThrow(Accounts.AUTH_TYPE))) {
 					case Accounts.AUTH_TYPE_OAUTH:
 					case Accounts.AUTH_TYPE_XAUTH:
 						cb.setOAuthConsumerKey(CONSUMER_KEY);
 						cb.setOAuthConsumerSecret(CONSUMER_SECRET);
 						twitter = new TwitterFactory(cb.build()).getInstance(new AccessToken(cur.getString(cur
 								.getColumnIndexOrThrow(Accounts.OAUTH_TOKEN)), cur.getString(cur
 								.getColumnIndexOrThrow(Accounts.TOKEN_SECRET))));
 						break;
 					case Accounts.AUTH_TYPE_BASIC:
 						twitter = new TwitterFactory(cb.build()).getInstance(new BasicAuthorization(cur.getString(cur
 								.getColumnIndexOrThrow(Accounts.USERNAME)), cur.getString(cur
 								.getColumnIndexOrThrow(Accounts.BASIC_AUTH_PASSWORD))));
 						break;
 					default:
 				}
 			}
 			cur.close();
 		}
 		return twitter;
 	}
 
 	public static int getTypeIcon(boolean is_fav, boolean has_location, boolean has_media) {
 		if (is_fav)
 			return R.drawable.ic_tweet_stat_starred;
 		else if (has_media)
 			return R.drawable.ic_tweet_stat_has_media;
 		else if (has_location) return R.drawable.ic_tweet_stat_has_location;
 		return 0;
 	}
 
 	public static boolean isMyAccount(Context context, long account_id) {
 		for (long id : getAccountIds(context)) {
 			if (id == account_id) return true;
 		}
 		return false;
 	}
 
 	public static boolean isMyRetweet(Context context, long account_id, long status_id) {
 		return account_id == getRetweetedByUserId(context, status_id);
 	}
 
 	public static boolean isNullOrEmpty(CharSequence text) {
 		return text == null || "".equals(text);
 	}
 
 	public static boolean isUserLoggedIn(Context context, long account_id) {
 		long[] ids = getAccountIds(context);
 		if (ids == null) return false;
 		for (long id : ids) {
 			if (id == account_id) return true;
 		}
 		return false;
 	}
 
 	public static ContentValues makeAccountContentValues(int color, AccessToken access_token, User user,
 			String rest_api_base, String search_api_base, String basic_password, int auth_type) {
 		if (user == null) throw new IllegalArgumentException("User can't be null!");
 		ContentValues values = new ContentValues();
 		switch (auth_type) {
 			case Accounts.AUTH_TYPE_BASIC:
 				if (basic_password == null)
 					throw new IllegalArgumentException("Password can't be null in Basic mode!");
 				values.put(Accounts.BASIC_AUTH_PASSWORD, basic_password);
 				break;
 			case Accounts.AUTH_TYPE_OAUTH:
 			case Accounts.AUTH_TYPE_XAUTH:
 				if (access_token == null)
 					throw new IllegalArgumentException("Access Token can't be null in OAuth/xAuth mode!");
 				if (user.getId() != access_token.getUserId())
 					throw new IllegalArgumentException("User and Access Token not match!");
 				values.put(Accounts.OAUTH_TOKEN, access_token.getToken());
 				values.put(Accounts.TOKEN_SECRET, access_token.getTokenSecret());
 				break;
 		}
 		values.put(Accounts.AUTH_TYPE, auth_type);
 		values.put(Accounts.USER_ID, user.getId());
 		values.put(Accounts.USERNAME, user.getScreenName());
 		values.put(Accounts.PROFILE_IMAGE_URL, user.getProfileImageURL().toString());
 		values.put(Accounts.USER_COLOR, color);
 		values.put(Accounts.IS_ACTIVATED, 1);
 		if (rest_api_base != null) {
 			values.put(Accounts.REST_API_BASE, rest_api_base);
 		}
 		if (search_api_base != null) {
 			values.put(Accounts.SEARCH_API_BASE, search_api_base);
 		}
 
 		return values;
 	}
 
 	public static ContentValues makeCachedUsersContentValues(User user) {
 		ContentValues values = new ContentValues();
 		values.put(CachedUsers.NAME, user.getName());
 		values.put(CachedUsers.PROFILE_IMAGE_URL, user.getProfileImageURL().toString());
 		values.put(CachedUsers.SCREEN_NAME, user.getScreenName());
 		values.put(CachedUsers.USER_ID, user.getId());
 
 		return values;
 	}
 
 	public static ContentValues makeStatusesContentValues(Status status, long account_id) {
 		ContentValues values = new ContentValues();
 		int is_retweet = status.isRetweet() ? 1 : 0;
 		Status retweeted_status = status.getRetweetedStatus();
 		if (is_retweet == 1 && retweeted_status != null) {
 			User retweet_user = status.getUser();
 			values.put(Statuses.RETWEET_ID, status.getId());
 			values.put(Statuses.RETWEETED_BY_ID, retweet_user.getId());
 			values.put(Statuses.RETWEETED_BY_NAME, retweet_user.getName());
 			values.put(Statuses.RETWEETED_BY_SCREEN_NAME, retweet_user.getScreenName());
 			status = retweeted_status;
 		}
 		User user = status.getUser();
 		long status_id = status.getId(), user_id = user.getId();
 		String profile_image_url = user.getProfileImageURL().toString();
 		String name = user.getName(), screen_name = user.getScreenName();
 		MediaEntity[] medias = status.getMediaEntities();
 		values.put(Statuses.STATUS_ID, status_id);
 		values.put(Statuses.ACCOUNT_ID, account_id);
 		values.put(Statuses.USER_ID, user_id);
 		values.put(Statuses.STATUS_TIMESTAMP, status.getCreatedAt().getTime());
 		values.put(Statuses.TEXT, formatStatusString(status, account_id));
 		values.put(Statuses.NAME, name);
 		values.put(Statuses.SCREEN_NAME, screen_name);
 		values.put(Statuses.PROFILE_IMAGE_URL, profile_image_url);
 		values.put(Statuses.RETWEET_COUNT, status.getRetweetCount());
 		values.put(Statuses.IN_REPLY_TO_SCREEN_NAME, status.getInReplyToScreenName());
 		values.put(Statuses.IN_REPLY_TO_STATUS_ID, status.getInReplyToStatusId());
 		values.put(Statuses.IN_REPLY_TO_USER_ID, status.getInReplyToUserId());
 		values.put(Statuses.SOURCE, status.getSource());
 		values.put(Statuses.LOCATION, formatGeoLocationToString(status.getGeoLocation()));
 		values.put(Statuses.IS_RETWEET, is_retweet);
 		values.put(Statuses.IS_FAVORITE, status.isFavorited() ? 1 : 0);
 		values.put(Statuses.IS_PROTECTED, user.isProtected() ? 1 : 0);
 		values.put(Statuses.HAS_MEDIA, medias != null && medias.length > 0 ? 1 : 0);
 		return values;
 	}
 
 	public static void restartActivity(Activity activity, boolean animation) {
 		int enter_anim = animation ? android.R.anim.fade_in : 0;
 		int exit_anim = animation ? android.R.anim.fade_out : 0;
 		activity.overridePendingTransition(enter_anim, exit_anim);
 		activity.finish();
 		activity.overridePendingTransition(enter_anim, exit_anim);
 		activity.startActivity(activity.getIntent());
 	}
 
 	public static void setMenuForStatus(Context context, Menu menu, long status_id, Uri uri) {
 		int activated_color = context.getResources().getColor(R.color.holo_blue_bright);
 		ContentResolver resolver = context.getContentResolver();
 		String[] cols = Statuses.COLUMNS;
 		String where = Statuses.STATUS_ID + "=" + status_id;
 		Cursor cur = resolver.query(uri, cols, where, null, null);
 		if (cur != null && cur.getCount() > 0) {
 			cur.moveToFirst();
 			long user_id = cur.getLong(cur.getColumnIndexOrThrow(Statuses.USER_ID));
 			boolean is_protected = cur.getInt(cur.getColumnIndexOrThrow(Statuses.IS_PROTECTED)) == 1;
 			menu.findItem(R.id.delete_submenu).setVisible(isMyAccount(context, user_id));
 			MenuItem itemRetweet = menu.findItem(MENU_RETWEET);
 			itemRetweet.setVisible(!is_protected
 					&& (!isMyAccount(context, user_id) || getActivatedAccounts(context).length > 1));
 			Drawable iconRetweetSubMenu = menu.findItem(R.id.retweet_submenu).getIcon();
 			if (isMyAccount(context, cur.getLong(cur.getColumnIndexOrThrow(Statuses.RETWEETED_BY_ID)))) {
 				iconRetweetSubMenu.setColorFilter(activated_color, Mode.MULTIPLY);
 				itemRetweet.setTitle(R.string.cancel_retweet);
 			} else {
 				iconRetweetSubMenu.clearColorFilter();
 				itemRetweet.setTitle(R.string.retweet);
 			}
 			MenuItem itemFav = menu.findItem(MENU_FAV);
 			Drawable iconFav = itemFav.getIcon();
 			if (cur.getInt(cur.getColumnIndexOrThrow(Statuses.IS_FAVORITE)) == 1) {
 				iconFav.setColorFilter(activated_color, Mode.MULTIPLY);
 				itemFav.setTitle(R.string.unfav);
 			} else {
 				iconFav.clearColorFilter();
 				itemFav.setTitle(R.string.fav);
 			}
 		}
 		if (cur != null) {
 			cur.close();
 		}
 	}
 
 	public static void setViewLayerType(View view, int layerType, Paint paint) {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			new MethodsCompat().setLayerType(view, layerType, paint);
 		}
 	}
 
 	public static void showErrorToast(Context context, Exception e, boolean long_message) {
 		String message = e != null ? context.getString(R.string.error_message, e.getMessage()) : context
 				.getString(R.string.error_unknown_error);
 		int length = long_message ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
 		Toast toast = Toast.makeText(context, message, length);
 		toast.show();
 	}
 
 	public static void unbindFromService(ServiceToken token) {
 
 		if (token == null) {
 			Log.e(LOGTAG, "Trying to unbind with null token");
 			return;
 		}
 		ContextWrapper wrapper = token.mWrappedContext;
 		ServiceBinder binder = mConnectionMap.remove(wrapper);
 		if (binder == null) {
 			Log.e(LOGTAG, "Trying to unbind for unknown Context");
 			return;
 		}
 		wrapper.unbindService(binder);
 	}
 
 	private static class ServiceBinder implements ServiceConnection {
 
 		private ServiceConnection mCallback;
 
 		public ServiceBinder(ServiceConnection callback) {
 
 			mCallback = callback;
 		}
 
 		@Override
 		public void onServiceConnected(ComponentName className, android.os.IBinder service) {
 
 			if (mCallback != null) {
 				mCallback.onServiceConnected(className, service);
 			}
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName className) {
 
 			if (mCallback != null) {
 				mCallback.onServiceDisconnected(className);
 			}
 		}
 	}
 
 }
