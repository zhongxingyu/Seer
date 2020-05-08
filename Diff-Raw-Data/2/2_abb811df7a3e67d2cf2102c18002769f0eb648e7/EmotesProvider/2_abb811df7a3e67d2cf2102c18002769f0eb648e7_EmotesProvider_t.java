 /*
  * BerryMotes android 
  * Copyright (C) 2013 Daniel Triendl <trellmor@trellmor.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.trellmor.berrymotes.provider;
 
 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.net.Uri;
 import android.preference.PreferenceManager;
 
 import com.trellmor.berrymotes.provider.EmotesContract;
 import com.trellmor.berrymotes.SettingsActivity;
 import com.trellmor.berrymotes.util.SelectionBuilder;
 
 public class EmotesProvider extends ContentProvider {
 	EmotesDatabase mDatabaseHelper;
 
 	private static final String AUTHORITY = EmotesContract.CONTENT_AUTHORITY;
 
 	public static final int ROUTE_EMOTES = 1;
 	public static final int ROUTE_EMOTES_ID = 2;
 	public static final int ROUTE_EMOTES_DISTINCT = 3;
 
 	private static final UriMatcher sUriMatcher = new UriMatcher(
 			UriMatcher.NO_MATCH);
 	static {
 		sUriMatcher.addURI(AUTHORITY, EmotesContract.PATH_EMOTES, ROUTE_EMOTES);
 		sUriMatcher.addURI(AUTHORITY, EmotesContract.PATH_EMOTES + "/*",
 				ROUTE_EMOTES_ID);
 		sUriMatcher.addURI(AUTHORITY, EmotesContract.PATH_EMOTES_DISTINCT,
 				ROUTE_EMOTES_DISTINCT);
 	}
 
 	@Override
 	public boolean onCreate() {
 		mDatabaseHelper = new EmotesDatabase(getContext());
 		return true;
 	}
 
 	@Override
 	public String getType(Uri uri) {
 		final int match = sUriMatcher.match(uri);
 		switch (match) {
 		case ROUTE_EMOTES:
 			return EmotesContract.Emote.CONTENT_TYPE;
 		case ROUTE_EMOTES_DISTINCT:
 			return EmotesContract.Emote.CONTENT_TYPE;
 		case ROUTE_EMOTES_ID:
 			return EmotesContract.Emote.CONTENT_ITEM_TYPE;
 		default:
 			throw new UnsupportedOperationException("Unknown uri: " + uri);
 		}
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder) {
 		SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
 		SelectionBuilder builder = new SelectionBuilder();
 		Context ctx = getContext();
 		assert ctx != null;
 		Cursor c;
 
 		int uriMatch = sUriMatcher.match(uri);
 		switch (uriMatch) {
 		case ROUTE_EMOTES_ID:
 			// Return a single entry, by ID
 			String id = uri.getLastPathSegment();
 			builder.where(EmotesContract.Emote._ID + "=?", id);
 		case ROUTE_EMOTES:
 			// Return all known entries
 			builder.table(EmotesContract.Emote.TABLE_NAME).where(selection,
 					selectionArgs);
 
			if (!PreferenceManager.getDefaultSharedPreferences(getContext())
 					.getBoolean(SettingsActivity.KEY_SHOW_NSFW, false)) {
 				builder.where(EmotesContract.Emote.COLUMN_NSFW + "=?", "0");
 			}
 			c = builder.query(db, projection, sortOrder);
 
 			// Note: Notification URI must be manually set here for loaders to
 			// correctly register ContentObservers.
 			c.setNotificationUri(ctx.getContentResolver(), uri);
 
 			return c;
 		case ROUTE_EMOTES_DISTINCT:
 			// Return all known entries
 			builder.table(EmotesContract.Emote.TABLE_NAME).where(selection,
 					selectionArgs);
 
 			c = builder.query(true, db, projection, sortOrder);
 
 			// Note: Notification URI must be manually set here for loaders to
 			// correctly register ContentObservers.
 			c.setNotificationUri(ctx.getContentResolver(), uri);
 
 			return c;
 		default:
 			throw new UnsupportedOperationException("Unknown uri: " + uri);
 		}
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
 		assert db != null;
 		final int match = sUriMatcher.match(uri);
 		Uri result;
 		switch (match) {
 		case ROUTE_EMOTES:
 			long id = db.insertOrThrow(EmotesContract.Emote.TABLE_NAME, null,
 					values);
 			result = Uri.parse(EmotesContract.Emote.CONTENT_URI + "/" + id);
 			break;
 		case ROUTE_EMOTES_ID:
 			throw new UnsupportedOperationException(
 					"Insert not supported on URI: " + uri);
 		default:
 			throw new UnsupportedOperationException("Unknown uri: " + uri);
 		}
 
 		// Send broadcast to registered ContentObservers, to refresh UI.
 		Context ctx = getContext();
 		assert ctx != null;
 		ctx.getContentResolver().notifyChange(uri, null, false);
 
 		return result;
 	}
 
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		SelectionBuilder builder = new SelectionBuilder();
 		final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
 		final int match = sUriMatcher.match(uri);
 		int count;
 		switch (match) {
 		case ROUTE_EMOTES:
 			count = builder.table(EmotesContract.Emote.TABLE_NAME)
 					.where(selection, selectionArgs).delete(db);
 			break;
 		case ROUTE_EMOTES_ID:
 			String id = uri.getLastPathSegment();
 			count = builder.table(EmotesContract.Emote.TABLE_NAME)
 					.where(EmotesContract.Emote._ID + "=?", id)
 					.where(selection, selectionArgs).delete(db);
 			break;
 		default:
 			throw new UnsupportedOperationException("Unknown uri: " + uri);
 		}
 
 		// Send broadcast to registered ContentObservers, to refresh UI.
 		Context ctx = getContext();
 		assert ctx != null;
 		ctx.getContentResolver().notifyChange(uri, null, false);
 
 		return count;
 	}
 
 	public int update(Uri uri, ContentValues values, String selection,
 			String[] selectionArgs) {
 		throw new UnsupportedOperationException("Update not supported");
 	}
 
 	static class EmotesDatabase extends SQLiteOpenHelper {
 		private final Context mContext;
 
 		public static final int DATABASE_VERSION = 3;
 
 		private static final String DATABASE_NAME = "emotes.db";
 		private static final String IDX_ENTRIES_NAME = "idx_"
 				+ EmotesContract.Emote.TABLE_NAME + "_"
 				+ EmotesContract.Emote.COLUMN_NAME;
 		private static final String IDX_ENTRIES_HASH = "idx_"
 				+ EmotesContract.Emote.TABLE_NAME + "_"
 				+ EmotesContract.Emote.COLUMN_HASH;
 
 		private static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
 				+ EmotesContract.Emote.TABLE_NAME + " ("
 				+ EmotesContract.Emote._ID + " INTEGER PRIMARY KEY,"
 				+ EmotesContract.Emote.COLUMN_NAME + " TEXT,"
 				+ EmotesContract.Emote.COLUMN_NSFW + " INTEGER,"
 				+ EmotesContract.Emote.COLUMN_APNG + " INTEGER,"
 				+ EmotesContract.Emote.COLUMN_IMAGE + " TEXT,"
 				+ EmotesContract.Emote.COLUMN_HASH + " TEXT,"
 				+ EmotesContract.Emote.COLUMN_INDEX + " INTEGER,"
 				+ EmotesContract.Emote.COLUMN_DELAY + " INTEGER)";
 		private static final String SQL_CREATE_IDX_ENTRIES_NAME = "CREATE INDEX "
 				+ IDX_ENTRIES_NAME
 				+ " ON "
 				+ EmotesContract.Emote.TABLE_NAME
 				+ "(" + EmotesContract.Emote.COLUMN_NAME + ")";
 		private static final String SQL_CREATE_IDX_ENTRIES_HASH = "CREATE INDEX "
 				+ IDX_ENTRIES_HASH
 				+ " ON "
 				+ EmotesContract.Emote.TABLE_NAME
 				+ "(" + EmotesContract.Emote.COLUMN_HASH + ")";
 
 		private static final String SQL_DROP_ENTRIES = "DROP TABLE IF EXISTS "
 				+ EmotesContract.Emote.TABLE_NAME;
 		private static final String SQL_DROP_IDX_ENTRIES_NAME = "DROP INDEX IF EXISTS "
 				+ IDX_ENTRIES_NAME;
 		private static final String SQL_DROP_IDX_ENTRIES_HASH = "DROP INDEX IF EXISTS "
 				+ IDX_ENTRIES_HASH;
 
 		public EmotesDatabase(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 			mContext = context;
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(SQL_CREATE_ENTRIES);
 			db.execSQL(SQL_CREATE_IDX_ENTRIES_NAME);
 			db.execSQL(SQL_CREATE_IDX_ENTRIES_HASH);
 			PreferenceManager.getDefaultSharedPreferences(mContext).edit()
 					.remove(SettingsActivity.KEY_SYNC_LAST_MODIFIED).commit();
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			db.execSQL(SQL_DROP_IDX_ENTRIES_HASH);
 			db.execSQL(SQL_DROP_IDX_ENTRIES_NAME);
 			db.execSQL(SQL_DROP_ENTRIES);
 			onCreate(db);
 		}
 	}
 }
