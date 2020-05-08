 package com.noughmad.slashdotcomments;
 
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.net.Uri;
 
 public class SlashdotProvider extends ContentProvider {
 	
	public static final String AUTHORITY = "content://com.noughmad.slashdotcomments.provider";
	public static final Uri BASE_URI = Uri.parse(AUTHORITY);
 
 	private static final int CODE_STORIES = 1;
 	private static final int CODE_STORY_DETAIL = 2;
 	private static final int CODE_STORY_COMMENTS = 3;
 	private static final int CODE_STORY_COMMENT_ID = 4;
 	
 	static UriMatcher sUriMatcher = new UriMatcher(0);
 	
 	static {
 		sUriMatcher.addURI(AUTHORITY, "stories", CODE_STORIES);
 		sUriMatcher.addURI(AUTHORITY, "stories/#", CODE_STORY_DETAIL);
 		sUriMatcher.addURI(AUTHORITY, "stories/#/comments", CODE_STORY_COMMENTS);
 		sUriMatcher.addURI(AUTHORITY, "stories/#/comments/#", CODE_STORY_COMMENT_ID);
 	}
 
 	static final String STORIES_TABLE_NAME = "stories";
 	static final String COMMENTS_TABLE_NAME = "comments";
 
 	static final String ID = "_id";
 	static final String STORY_TITLE = "title";
 	static final String STORY_SUMMARY = "summary";
 	static final String STORY_COMMENT_COUNT = "comment_count";
 	static final String STORY_URL = "url";
 
 	
 	static final String COMMENT_STORY = "story";
 	static final String COMMENT_TITLE = "title";
 	static final String COMMENT_SCORE = "score";
 	static final String COMMENT_LEVEL = "level";
 	static final String COMMENT_CONTENT = "content";
 	static final String COMMENT_AUTHOR = "author";
 	
 	private Helper mHelper;
 	
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		// TODO: Will probably never need deleting
 		return 0;
 	}
 
 	@Override
 	public String getType(Uri uri) {
 		switch (sUriMatcher.match(uri)) {
 		case CODE_STORIES:
 			return "vnd.android.cursor.dir/vnd.com.noughmad.slashdotcomments.provider.stories";
 		case CODE_STORY_DETAIL:
 			return "vnd.android.cursor.item/vnd.com.noughmad.slashdotcomments.provider.stories";
 		case CODE_STORY_COMMENTS:
 			return "vnd.android.cursor.dir/vnd.com.noughmad.slashdotcomments.provider.comments";
 		}
 		return null;
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		String tableName = null;
 		switch (sUriMatcher.match(uri)) {
 		case CODE_STORIES:
 			tableName = STORIES_TABLE_NAME;
 			break;
 			
 		case CODE_STORY_COMMENTS:
 			tableName = COMMENTS_TABLE_NAME;
 			values.put(COMMENT_STORY, Long.parseLong(uri.getPathSegments().get(1)));
 		}
 				
 		if (tableName != null) {
 			long id = mHelper.getWritableDatabase().insert(tableName, null, values);
 			getContext().getContentResolver().notifyChange(uri, null);
 			return ContentUris.withAppendedId(uri, id);
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public boolean onCreate() {
 		mHelper = new Helper(getContext());
 		return true;
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder) {
 		Cursor cursor = null;
 		switch (sUriMatcher.match(uri)) {
 		case CODE_STORIES:
 			cursor = mHelper.getReadableDatabase().query(STORIES_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
 			cursor.setNotificationUri(getContext().getContentResolver(), uri);
 			return cursor;
 			
 		case CODE_STORY_DETAIL:
 			selection = ID + " = ?";
 			selectionArgs = new String[] {uri.getPathSegments().get(1)};
 			cursor = mHelper.getReadableDatabase().query(STORIES_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
 			cursor.setNotificationUri(getContext().getContentResolver(), uri);
 			return cursor;
 			
 		case CODE_STORY_COMMENTS:
 			selection = COMMENT_STORY + " = ?";
 			selectionArgs = new String[] {uri.getPathSegments().get(1)};
 			cursor = mHelper.getReadableDatabase().query(COMMENTS_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
 			cursor.setNotificationUri(getContext().getContentResolver(), uri);
 			return cursor;			
 		}
 		return null;
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection,
 			String[] selectionArgs) {
 		switch (sUriMatcher.match(uri)) {
 		case CODE_STORY_DETAIL:
 			selection = ID + " = ?";
 			selectionArgs = new String[] {uri.getPathSegments().get(1)};
 			return mHelper.getReadableDatabase().update(STORIES_TABLE_NAME, values, selection, selectionArgs);
 			
 		case CODE_STORY_COMMENT_ID:
 			selection = COMMENT_STORY + " = ? AND " + ID + " = ?";
 			selectionArgs = new String[] {uri.getPathSegments().get(1), uri.getPathSegments().get(3)};
 			return mHelper.getReadableDatabase().update(COMMENTS_TABLE_NAME, values, selection, selectionArgs);
 		}
 		return 0;
 	}
 	
 	private class Helper extends SQLiteOpenHelper {
 
 		private final static String DB_NAME = "slashdot_comments";
 		private final static int DB_VERSION = 1;
 
 		public Helper(Context context) {
 			super(context, DB_NAME, null, DB_VERSION);
 		}
 		
 		private static final String CREATE_STORIES = "CREATE TABLE " + STORIES_TABLE_NAME + " ("
				+ ID + "INTEGER UNIQUE, "
 				+ STORY_TITLE + " TEXT, "
 				+ STORY_COMMENT_COUNT + " INTEGER, "
 				+ STORY_URL + " TEXT, "
 				+ STORY_SUMMARY + " TEXT);";
 				
 		private static final String CREATE_COMMENTS = "CREATE TABLE " + COMMENTS_TABLE_NAME + " ("
				+ ID + "INTEGER UNIQUE, "
 				+ COMMENT_STORY + " INTEGER, "
 				+ COMMENT_TITLE + " TEXT, "
 				+ COMMENT_SCORE + " TEXT, "
 				+ COMMENT_CONTENT + " TEXT, "
 				+ COMMENT_AUTHOR + " TEXT, " +
				"FOREIGN KEY(" + COMMENT_STORY + " REFERENCES " + STORIES_TABLE_NAME + "(" + ID + "));";
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(CREATE_STORIES);
 			db.execSQL(CREATE_COMMENTS);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			
 		}
 	};
 }
