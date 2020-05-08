 /*
  * MUSTARD: Android's Client for StatusNet
  * 
  * Copyright (C) 2009-2010 macno.org, Michele Azzolari
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
  * for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 package org.mustard.android;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Random;
 
 import org.mustard.statusnet.Attachment;
 import org.mustard.statusnet.Notice;
 import org.mustard.statusnet.Status;
 import org.mustard.statusnet.User;
 import org.mustard.util.MustardException;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteConstraintException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class MustardDbAdapter {
 
 	public static final int ROWTYPE_ALL = 99;
 	public static final int ROWTYPE_PUBLIC = 0;
 	public static final int ROWTYPE_FRIENDS = 1;
 	public static final int ROWTYPE_USER = 2;
 	public static final int ROWTYPE_GROUP = 3;
 	public static final int ROWTYPE_TAG = 4;
 	public static final int ROWTYPE_SINGLE = 5;
 	public static final int ROWTYPE_MENTION = 6;
 	public static final int ROWTYPE_SEARCH = 7;
 	public static final int ROWTYPE_FAVORITES = 8;
 	public static final int ROWTYPE_CONVERSATION = 9;
 	
 	public static final String KEY_ROWID = "_id";
 
 	private static final String TAG = "MustardDbAdapter";
 
 	public static final String KEY_DENT_ID = "dent_id";
 	public static final String KEY_ROWTYPE = "rowtype";
 	public static final String KEY_ROWEXTRA = "rowextra";
 	public static final String KEY_INSERT_AT = "insert_at";
 	public static final String KEY_STATUS_ID = "status_id";
 	public static final String KEY_USER_ID = "user_id";
 	public static final String KEY_USER_IMAGE = "user_image";
 	public static final String KEY_NAME = "name";
 	public static final String KEY_SCREEN_NAME = "screen_name";
 	public static final String KEY_SOURCE = "source";
 	public static final String KEY_IN_REPLY_TO = "in_reply_to";
 	public static final String KEY_STATUS = "status";
 	public static final String KEY_FAVORITE = "favorite";
 	public static final String KEY_FOLLOWING = "following";
 	public static final String KEY_ATTACHMENT = "attachment";
 	public static final String KEY_GEO = "geo";
 	public static final String KEY_LON = "lon";
 	public static final String KEY_LAT = "lat";
 	public static final String KEY_KEY = "key";
 	public static final String KEY_SECRET = "secret";
 	public static final String KEY_RESERVED = "reserved";
 	public static final String KEY_TOKEN = "token";
 	public static final String KEY_TOKEN_SECRET = "token_secret";
 	public static final String KEY_MENTION_MAX_ID = "mention_max_id";
 
 	public static final String KEY_USER = "user";
 	public static final String KEY_PASSWORD = "password";
 	public static final String KEY_INSTANCE = "instance";
 	public static final String KEY_DEFAULT = "isdefault";
 	public static final String KEY_VERSION = "version";
 	public static final String KEY_TEXTLIMIT = "textlimit";
 
 	public static final String KEY_MIMETYPE = "mimetype";
 	public static final String KEY_URL = "url";
 
 	public static final String KEY_ACCOUNT_ID = "account_id";
 	public static final String KEY_CODE = "code";
 	public static final String KEY_VALUE = "value";
 	
 	public static final String KEY_BTYPE = "btype";
 	public static final String KEY_BPARAM = "bparam";
 	public static final String KEY_COUNTER = "counter";
 	
 	public static final String KEY_FILTER = "filter";
 	
 	private DatabaseHelper mDbHelper;
 	private SQLiteDatabase mDb;
 
 	private static final String DATABASE_NAME = "data";
 	private static final String DATABASE_STATUS_TABLE = "dents";
 	private static final String DATABASE_ACCOUNT_TABLE = "accounts";
 	private static final String DATABASE_ATTACHMENTS_TABLE = "attachments";
 	private static final String DATABASE_PARAM_TABLE = "params";
 	private static final String DATABASE_BOOKMARK_TABLE = "bookmarks";
 	private static final String DATABASE_FILTER_TABLE = "filters";
 	private static final String DATABASE_OAUTH_TABLE = "oauthkeys";
 	
 	/**
 	 * Database creation sql statement
 	 */
 	private static final String DATABASE_CREATE_DENT =
 		"create table " + DATABASE_STATUS_TABLE + " ( " +
 		"_id integer primary key autoincrement, " +
 		"rowtype integer not null," +
 		"rowextra text not null," +
 		"status_id integer not null, " +
 		"insert_at text not null, " +
 		"user_id integer not null, " +
 		"user_image text not null, " +
 		"screen_name text not null, " +
 		"name text not null, " +
 		"source text null, " +
 		"in_reply_to integer null, " +
 		"status text not null," +
 		"favorite int null, " +
 		"following int null, " +
 		"attachment int null, " +
 		"geo int not null," +
 		"lon real not null, " +
 		"lat real not null, " +
 		"unique (rowtype,rowextra,status_id) )"; 
 
 	private static final String DATABASE_CREATE_ACCOUNT =  
 		"create table " + DATABASE_ACCOUNT_TABLE + " (" +
 		"_id integer primary key autoincrement," +
 		"user_id integer not null, " +
 		"user text not null, " +
 		"password text not null," +
 		"instance text not null," +
 		"isdefault integer not null," +
 		"version text null, " +
 		"textlimit integer null, " +
 		"token text null, " +
 		"token_secret text null," +
 		"mention_max_id integer ) ";
 
 	private static final String DATABASE_DROP_ATTACHMENTS = 
 		"DROP TABLE IF EXISTS " + DATABASE_ATTACHMENTS_TABLE; 
 	
 	private static final String DATABASE_CREATE_ATTACHMENTS = 
 		"CREATE TABLE " + DATABASE_ATTACHMENTS_TABLE  + " (" +
 			"_id integer primary key autoincrement, " +
 				"dent_id integer not null,  " +
 				"url text not null," +
 				"mimetype text not null, " +
 				"FOREIGN KEY (dent_id) REFERENCES "+DATABASE_STATUS_TABLE+"("+KEY_ROWID+") ON DELETE CASCADE )";
 	
 	private static final String DATABASE_CREATE_PARAM =  
 		"create table " + DATABASE_PARAM_TABLE + " (" +
 		"code text not null," +
 		"value text null, " +
 		"primary key (code)) ";
 	
 	private static final String DATABASE_CREATE_BOOKMARKS = 
 		"create table " + DATABASE_BOOKMARK_TABLE + " (" +
 		"_id integer primary key autoincrement, " +
 		"user_id integer not null," +
 		"btype text not null, " +
 		"bparam text not null, " +
 		"counter int not null, " +
 		"unique (user_id, btype,bparam)) ";
 	
 	private static final String DATABASE_CREATE_FILTERS = 
 		"create table " + DATABASE_FILTER_TABLE + " (" +
 		"_id integer primary key autoincrement, " +
 		"filter text not null) ";
 	
 	private static final String DATABASE_CREATE_OAUTH = 
 		"create table " + DATABASE_OAUTH_TABLE + " ( " +
 		" _id integer primary key autoincrement, " +
 		" instance text not null," +
 		" key text not null, " +
 		" secret text not null, " +
 		" reserved int not null, " +
 		" unique (instance) " + 
 		" ) ";
 	
 	public static final int DATABASE_VERSION = 37;
 
 	private final Context mCtx;
 
 	private static class DatabaseHelper extends SQLiteOpenHelper {
 
 		DatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL("DROP TABLE IF EXISTS " + 
 					DATABASE_STATUS_TABLE );
 			db.execSQL(DATABASE_CREATE_DENT);
 			db.execSQL("DROP TABLE IF EXISTS " + 
 					DATABASE_ACCOUNT_TABLE );	
 			db.execSQL(DATABASE_CREATE_ACCOUNT);
 			db.execSQL("DROP TABLE IF EXISTS " + 
 					DATABASE_PARAM_TABLE );	
 			db.execSQL(DATABASE_CREATE_PARAM);
 			db.execSQL("DROP TABLE IF EXISTS " +
 					DATABASE_BOOKMARK_TABLE);
 			db.execSQL(DATABASE_CREATE_BOOKMARKS);
 			db.execSQL("DROP TABLE IF EXISTS " +
 					DATABASE_OAUTH_TABLE);
 			db.execSQL(DATABASE_CREATE_OAUTH);
 			db.execSQL("DROP TABLE IF EXISTS " +
 					DATABASE_ATTACHMENTS_TABLE);
 			db.execSQL(DATABASE_CREATE_ATTACHMENTS);
 			db.execSQL("DROP TABLE IF EXISTS " +
 					DATABASE_FILTER_TABLE);
 			db.execSQL(DATABASE_CREATE_FILTERS);
 			
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.i(TAG,"onUpgrade " + oldVersion + " >> " + newVersion);
 			boolean lastTableAccount=false;
 			boolean lastTableParam=false;
 			boolean lastTableBookmark=false;
 			boolean lastTableOAuth=false;
 			boolean lastTableFilter=false;
 			// Simply I don't want to see warning message ;)
 			// -->>
 			if(!lastTableParam) {
 				
 			}
 			if(!lastTableBookmark) {
 				
 			}
 			// <<--
 			if (oldVersion > 1) {
 				if (oldVersion<15) {
 					db.execSQL("DROP TABLE IF EXISTS " + 
 							DATABASE_ACCOUNT_TABLE );			
 					db.execSQL(DATABASE_CREATE_ACCOUNT);
 					lastTableAccount=true;
 				}
 				if (oldVersion<21) {
 					db.execSQL(DATABASE_CREATE_PARAM);
 					lastTableParam=true;
 				} else if (oldVersion < 24) {
 					
 					db.execSQL("CREATE TEMPORARY TABLE t1_backup(a,b)");
 					db.execSQL("INSERT INTO t1_backup SELECT code,value FROM " + DATABASE_PARAM_TABLE + " WHERE code = 'mid'");
 					db.execSQL("DROP TABLE IF EXISTS " + DATABASE_PARAM_TABLE);
 					db.execSQL(DATABASE_CREATE_PARAM);
 					db.execSQL("INSERT INTO " + DATABASE_PARAM_TABLE + " SELECT a,b FROM t1_backup");
 					db.execSQL("DROP TABLE t1_backup");
 					lastTableParam=true;
 				}
 				
 				if (oldVersion < 27) {
 					db.execSQL("DROP TABLE IF EXISTS " +
 							DATABASE_BOOKMARK_TABLE);
 					db.execSQL(DATABASE_CREATE_BOOKMARKS);
 					lastTableBookmark=true;
 				}
 				if (oldVersion < 28) {
 					
 					db.execSQL("DROP TABLE IF EXISTS " +
 							DATABASE_OAUTH_TABLE);
 					db.execSQL(DATABASE_CREATE_OAUTH);
 					lastTableOAuth=true;
 					if(!lastTableAccount) {
 						db.execSQL("ALTER TABLE " + DATABASE_ACCOUNT_TABLE + " ADD token text null");
 						db.execSQL("ALTER TABLE " + DATABASE_ACCOUNT_TABLE + " ADD token_secret text null");
 					}
 				} 
 				if (oldVersion < 30) {
 					if(!lastTableOAuth) {
 						db.execSQL("ALTER TABLE " + DATABASE_OAUTH_TABLE + " ADD reserved int null");
 						db.execSQL("UPDATE " + DATABASE_OAUTH_TABLE + " set reserved = '1' " );
 					}
 					if(!lastTableAccount) {
 						db.execSQL("ALTER TABLE " + DATABASE_ACCOUNT_TABLE + " ADD " + KEY_MENTION_MAX_ID + " integer ");
 					}
 				}
 				if (oldVersion < 32) {
 					if(!lastTableAccount) {
 						db.execSQL("ALTER TABLE " + DATABASE_ACCOUNT_TABLE + " ADD " + KEY_USER_ID + " integer ");
 					}
 				}
 				if (oldVersion < 35) {
 					if(!lastTableAccount) {
 						db.execSQL("ALTER TABLE " + DATABASE_ACCOUNT_TABLE + " ADD " + KEY_TEXTLIMIT + " integer ");
 					}
 				}
 				if(oldVersion<37) {
 					if(!lastTableFilter) {
 						db.execSQL("DROP TABLE IF EXISTS " +
 								DATABASE_FILTER_TABLE);
 						db.execSQL(DATABASE_CREATE_FILTERS);
 					}
 				}
 			} 
 			db.execSQL("DROP TABLE IF EXISTS " + 
 					DATABASE_STATUS_TABLE );
 			db.execSQL(DATABASE_CREATE_DENT);
 			db.execSQL(DATABASE_DROP_ATTACHMENTS);
 			db.execSQL(DATABASE_CREATE_ATTACHMENTS);
 		}
 
 	}
 
 	/**
 	 * Constructor - takes the context to allow the database to be
 	 * opened/created
 	 * 
 	 * @param ctx the Context within which to work
 	 */
 	public MustardDbAdapter(Context ctx) {
 		this.mCtx = ctx;
 	}
 
 	public String creareMustardId() {
 		Random r = new Random();
 		String token = Long.toString(Math.abs(r.nextLong()), 36);
 		addParameter("mid",token);
 		return token;
 	}
 	
 	public String getMustardId() {
 		String mid=getParameter("mid");
 		return mid;
 	}
 	
 	public boolean addParameter(String code, String value) {
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_CODE, code);
 		initialValues.put(KEY_VALUE, value);
 		long _rid = -1;
 		try {
 			_rid  = mDb.insertOrThrow(DATABASE_PARAM_TABLE, null, initialValues);
 		} catch(SQLiteConstraintException e) {
 			// No problem, update
 		}
 		if (_rid < 0) {
 			// Update??
 			return setParameter(code, value);
 		} else {
 			return true;
 		}
 	}
 	
 	public boolean setParameter(String code, String value) {
 		ContentValues args = new ContentValues();
         args.put(KEY_VALUE, value);
         mDb.update(DATABASE_PARAM_TABLE, args, KEY_CODE + "= '" + code + "'", null);
         return true;
 	}
 	
 	private long createAttachment(long status_id, String url, String mimetype) throws MustardException {
 
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_DENT_ID, status_id);
 		initialValues.put(KEY_URL, url);
 		initialValues.put(KEY_MIMETYPE, mimetype);
 		long ret = -1;
 		try {
 			 ret = mDb.insertOrThrow(DATABASE_ATTACHMENTS_TABLE, null, initialValues);
 		} catch(SQLiteConstraintException e) {
 			ret = 0;
 		} catch (Exception e) {
 			throw new MustardException(e.toString());
 		}
 		return ret;
 	}
 
 	public Cursor fetchAttachment(long statusId) throws SQLException {
 
 		Cursor cursor =
 			mDb.query(DATABASE_ATTACHMENTS_TABLE, 
 					new String[] {
 						KEY_URL,
 						KEY_MIMETYPE
 			}, 
 			KEY_DENT_ID + " = ?", new String[]{Long.toString(statusId)},
 			null, null, null, null);
 		return cursor;
 
 	}
 	
 	public long getUserMentionMaxId(long userid) {
 		long mid=0;
 		Cursor mCursor = null;
 		try {
 			mCursor = mDb.query(DATABASE_ACCOUNT_TABLE, 
 					new String[] { KEY_MENTION_MAX_ID
 			}, 
 			KEY_ROWID + " = "+userid,
 			null,
 			null, null, null, null);
 			if (mCursor != null) {
 				if(mCursor.moveToFirst())
 					mid = mCursor.getLong(0);
 			}
 		} finally {
			mCursor.close();
 		}
 		return mid;
 	}
 	
 	public boolean setUserMentionMaxId(long userid,long maxid) {
 		ContentValues args = new ContentValues();
         args.put(KEY_MENTION_MAX_ID, maxid);
         return mDb.update(DATABASE_ACCOUNT_TABLE, args, KEY_ROWID + "= " + userid, null) > 0;
 	}
 	
 	public String getParameter(String code) {
 		String mid="";
 		Cursor mCursor = null;
 		try {
 			mCursor = mDb.query(DATABASE_PARAM_TABLE, 
 					new String[] { KEY_VALUE  
 			}, 
 			KEY_CODE + " = '"+code + "'",
 			null,
 			null, null, null, null);
 			if (mCursor != null) {
 				if(mCursor.moveToFirst())
 					mid = mCursor.getString(0);
 			}
 		} finally {
 			mCursor.close();
 		}
 		return mid;
 	}
 	
 	/**
 	 * Open the notes database. If it cannot be opened, try to create a new
 	 * instance of the database. If it cannot be created, throw an exception to
 	 * signal the failure
 	 * 
 	 * @return this (self reference, allowing this to be chained in an
 	 *         initialization call)
 	 * @throws SQLException if the database could be neither opened or created
 	 */
 	public MustardDbAdapter open() throws SQLException {
 		mDbHelper = new DatabaseHelper(mCtx);
 		mDb = mDbHelper.getWritableDatabase();
 
 		return this;
 	}
 
 	public void close() {
 		mDbHelper.close();
 	}
 
 	public int getDbversion() {
 		return mDb.getVersion();
 	}
 
 //	public boolean createStatuses(int type, ArrayList<Status> dents) {
 //		return createStatuses(type, "", dents);
 //	}
 
 	public boolean createStatuses(int type, String extra, ArrayList<Status> dents) {
 		if(dents == null) {
 			if (MustardApplication.DEBUG) Log.e(TAG,"array is null!!");
 			return false;
 		}
 		Cursor c = null;
 		ArrayList<String> filters = new ArrayList<String>();
 		try {
 			c=fetchFilters();
 			while(c.moveToNext()) {
 				filters.add(c.getString(c.getColumnIndex(KEY_FILTER)).toLowerCase());
 			}
 		} catch (Exception e) {
 			Log.e(TAG, e.getMessage());
 		} finally {
 			if(c!=null) {
 				try {
 					c.close();
 				}catch (Exception e) {
 				}
 			}
 		}
 		try {
 			for (Status dent : dents){
 				boolean skip = false;
 				for (String filter: filters) {
 					if (dent.getNotice().getText().toLowerCase().indexOf(filter)>-1) {
 						skip=true;
 						break;
 					}
 				}
 				if(!skip)
 					createStatus(type, extra, dent);
 			}
 		} catch (Exception e) {
 			if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 			return false;
 		}
 		return true;
 	}
 
 	public long createStatus(int type, Status dent) throws MustardException {
 		return createStatus(type,"",dent);
 	}
 
 	public long createStatus(int type,String extra, Status dent) throws MustardException {
 		Notice n = dent.getNotice();
 		User u = dent.getUser();
 		boolean isTest=true;
 		if (u==null) {
 			if (MustardApplication.DEBUG) Log.e(TAG,mCtx.getString(R.string.error_no_user_from_status));
 			isTest=false;
 		}
 		if(n==null) {
 			if (MustardApplication.DEBUG) Log.e(TAG,mCtx.getString(R.string.error_no_notice_from_status));
 			isTest=false;
 		}
 		if(!isTest)
 			return -1;
 		long ret = 0;
 		try {
 			Date d = n.getCreated_at();
 			String _d = "";
 			if (d != null) {
 				_d = ""+d.getTime();
 			} else {
 				if (MustardApplication.DEBUG) Log.e(TAG,"ERROR!!!!!! NOTICE DATE IS NULL!!!");
 			}
 			boolean hasAttachment = n.getAttachments() != null && n.getAttachments().size()>0 ? true : false;
 			ret = createStatus(type,extra,n.getId(),_d,
 				u.getScreen_name(), u.getId(),u.getName(),
 				n.getSource(),n.getIn_reply_to_status_id(),
 				n.getText(),u.getProfile_image_url(),
 				n.isFavorited() ? 1 : 0,
 				u.isFollowing() ? 1 : 0,
 				hasAttachment ? 1 : 0,
 				n.isGeo(),
 				n.getLat(),
 				n.getLon()
 						);
 			if (hasAttachment && ret > 0) {
 				for (Attachment attachment: n.getAttachments()){
 					createAttachment(ret, attachment.getUrl(), attachment.getMimeType());
 				}
 			}
 		} catch (NullPointerException e) {
 //			e.printStackTrace();
 			throw new MustardException(e.toString());
 		}
 		return ret;
 	}
 
 	private long createStatus(int type, String extra, long status_id, String insert_at, String screen_name, long user_id, String name,
 			String source,int in_reply_to, String status, String user_image, int favorited, int following,int attachment, boolean geo,double lat, double lon) throws MustardException {
 
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_ROWTYPE, type);
 		initialValues.put(KEY_ROWEXTRA, extra);
 		initialValues.put(KEY_STATUS_ID, status_id);
 		initialValues.put(KEY_INSERT_AT, insert_at);
 		initialValues.put(KEY_SCREEN_NAME, screen_name);
 		initialValues.put(KEY_USER_ID, user_id );
 		initialValues.put(KEY_NAME, name );
 		initialValues.put(KEY_SOURCE, source);
 		initialValues.put(KEY_IN_REPLY_TO, in_reply_to);
 		initialValues.put(KEY_STATUS, status);
 		initialValues.put(KEY_USER_IMAGE, user_image);
 		initialValues.put(KEY_FAVORITE, favorited);
 		initialValues.put(KEY_FOLLOWING, following);
 		initialValues.put(KEY_ATTACHMENT, attachment);
 		initialValues.put(KEY_GEO, geo);
 		initialValues.put(KEY_LAT, lat);
 		initialValues.put(KEY_LON, lon);
 		long ret = -1;
 		try {
 			 ret = mDb.insertOrThrow(DATABASE_STATUS_TABLE, null, initialValues);
 		} catch(SQLiteConstraintException e) {
 			ret = 0;
 		} catch (Exception e) {
 			throw new MustardException(e.toString());
 		}
 		return ret;
 	}
 
 	
 	public boolean updateStatusFavor(long status_id, boolean favor) {
 		return updateStatusFavor(Long.toString(status_id), favor);
 	}
 	
     public boolean updateStatusFavor(String status_id, boolean favor) {
         int i_favor = (favor) ? 1 : 0;
         ContentValues args = new ContentValues();
         args.put(KEY_FAVORITE, i_favor);
         return mDb.update(DATABASE_STATUS_TABLE,
                         args, KEY_STATUS_ID + " = ? ",
                         new String[] { status_id}) > 0;
     }
 
 	public boolean updateStatusFollowing(String userid, boolean follow) {
 		
 		ContentValues args = new ContentValues();
         args.put(KEY_FOLLOWING, follow ? 1 : 0);
         return mDb.update(DATABASE_STATUS_TABLE, args, KEY_USER_ID + " = ?", new String[] { userid }) > 0;
 		
 	}
 	
 	public boolean deleteStatuses(int rowtype,String extra) {
 		if (rowtype == ROWTYPE_ALL) {
 			mDb.delete(DATABASE_ATTACHMENTS_TABLE, "1", null);
 			return mDb.delete(DATABASE_STATUS_TABLE, "1", null) > 0;
 		} else {
 			return mDb.delete(DATABASE_STATUS_TABLE, KEY_ROWTYPE + " = " + rowtype + " and " + KEY_ROWEXTRA + " = '" + extra + "'", null) > 0 ;
 		}
 	}
 
 	/**
 	 * Delete the note with the given rowId
 	 * 
 	 * @param rowId id of note to delete
 	 * @return true if deleted, false otherwise
 	 */
 	public boolean deleteStatus(long rowId) {
 		return mDb.delete(DATABASE_STATUS_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
 	}
 
 	/**
 	 * Return a Cursor over the list of all notes in the database
 	 * 
 	 * @return Cursor over all notes
 	 */
 	public Cursor fetchAllStatuses(int rowtype, String extra) {
 		return fetchAllStatuses(rowtype, extra, "DESC");
 	}
 
 	public Cursor fetchAllStatuses(int rowtype, String extra, String order) {
 		Cursor ret = null;
 		try {
 			if (rowtype == ROWTYPE_ALL) {
 				ret = mDb.query(true,DATABASE_STATUS_TABLE, 
 						new String[] {
 						KEY_ROWID, KEY_STATUS_ID, KEY_STATUS,
 						KEY_IN_REPLY_TO, KEY_SOURCE,
 						KEY_SCREEN_NAME, KEY_INSERT_AT, KEY_USER_IMAGE, 
 						KEY_FAVORITE, KEY_FOLLOWING,
 						KEY_USER_ID, KEY_ATTACHMENT,
 						KEY_GEO, KEY_LAT, KEY_LON
 				}, 
 						null, null, null, null, KEY_STATUS_ID + " DESC",null);
 			} else {
 				ret = mDb.query(true,
 						DATABASE_STATUS_TABLE, 
 						new String[] {
 						KEY_ROWID, KEY_STATUS_ID, KEY_STATUS,
 						KEY_IN_REPLY_TO, KEY_SOURCE,
 						KEY_SCREEN_NAME, KEY_INSERT_AT, KEY_USER_IMAGE, 
 						KEY_FAVORITE, KEY_FOLLOWING,
 						KEY_USER_ID, KEY_ATTACHMENT,
 						KEY_GEO, KEY_LAT, KEY_LON
 					}, 
 					KEY_ROWTYPE + " = " + rowtype + " and " + KEY_ROWEXTRA + " = '" + extra + "'", null, null, null, KEY_STATUS_ID +  " " + order,null);
 			}
 		} catch (Exception e) {
 			if (MustardApplication.DEBUG) Log.e(TAG,e.toString());
 		}
 		return ret;
 	}
 
 	/**
 	 * Return a Cursor positioned at the note that matches the given rowId
 	 * 
 	 * @param rowId id of note to retrieve
 	 * @return Cursor positioned to matching note, if found
 	 * @throws SQLException if note could not be found/retrieved
 	 */
 	public Cursor fetchStatus(long rowId) throws SQLException {
 
 		Cursor mCursor =
 			mDb.query(DATABASE_STATUS_TABLE, 
 					new String[] {
 					KEY_ROWID, 
 					KEY_STATUS_ID, 
 					KEY_STATUS,
 					KEY_IN_REPLY_TO, 
 					KEY_SOURCE,
 					KEY_SCREEN_NAME, 
 					KEY_INSERT_AT, 
 					KEY_USER_IMAGE , 
 					KEY_FAVORITE, 
 					KEY_FOLLOWING,
 					KEY_USER_ID,
 					KEY_ATTACHMENT,
 					KEY_GEO, KEY_LAT, KEY_LON
 			}, 
 			KEY_ROWID + "=" + rowId, null,
 			null, null, null, null);
 		if (mCursor != null) {
 			mCursor.moveToFirst();
 		}
 		return mCursor;
 
 	}
 
 	public long createAccount(
 			long userid,
 			String user, 
 			String password, 
 			String instance, 
 			int isDefault, 
 			String version) {
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_USER_ID, userid);
 		initialValues.put(KEY_USER, user);
 		initialValues.put(KEY_PASSWORD, password);
 		initialValues.put(KEY_INSTANCE, instance);
 		initialValues.put(KEY_DEFAULT, isDefault);
 		initialValues.put(KEY_VERSION, version);
 		
 		return mDb.insertOrThrow(DATABASE_ACCOUNT_TABLE, null, initialValues);
 	}
 	
 	public long createAccount(long userid, String user, String token, String token_secret, String instance, int isDefault, String version) {
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_USER_ID, userid);
 		initialValues.put(KEY_USER, user);
 		initialValues.put(KEY_PASSWORD, "XXX");
 		initialValues.put(KEY_TOKEN, token);
 		initialValues.put(KEY_TOKEN_SECRET, token_secret);
 		initialValues.put(KEY_INSTANCE, instance);
 		initialValues.put(KEY_DEFAULT, isDefault);
 		initialValues.put(KEY_VERSION, version);
 		
 		return mDb.insertOrThrow(DATABASE_ACCOUNT_TABLE, null, initialValues);
 	}
 
 	
 	public boolean updateAccount(String newpassword) {
 		ContentValues args = new ContentValues();
 		args.put(KEY_PASSWORD, newpassword);
         return mDb.update(DATABASE_ACCOUNT_TABLE, args, KEY_DEFAULT + " = 1 ", null) > 0;
 	}
 	
 	public boolean updateAccount(String username, String token, String token_secret, String instance, int isDefault, String version) {
 		ContentValues args = new ContentValues();
 		args.put(KEY_PASSWORD, "XXX");
 		args.put(KEY_TOKEN, token);
 		args.put(KEY_TOKEN_SECRET, token_secret);
 		args.put(KEY_DEFAULT, isDefault);
 		args.put(KEY_VERSION, version);
         return mDb.update(DATABASE_ACCOUNT_TABLE, args, KEY_USER + " = '" + username + "' AND " + KEY_INSTANCE + " = '" + instance + "'", null) > 0;
 	}
 
 	public Cursor fetchAllAccounts() {
 		return mDb.query(DATABASE_ACCOUNT_TABLE, new String[] {KEY_ROWID, KEY_USER, KEY_PASSWORD, KEY_TOKEN, KEY_TOKEN_SECRET, KEY_INSTANCE,KEY_DEFAULT, KEY_VERSION}, null, null, null, null, null);
 	}
 	
 	public Cursor fetchAllAccountsDefaultFirst() {
 		return mDb.rawQuery("select " + KEY_ROWID + ", " + KEY_USER + "||'@'||replace(replace("+KEY_INSTANCE +",'http://',''),'https://','') " + KEY_USER + 
 				", " + KEY_TEXTLIMIT + 
 				" from " + DATABASE_ACCOUNT_TABLE + 
 				" order by " + KEY_DEFAULT + " desc",null);
 	}
 	
 	public Cursor fetchAllNonDefaultAccounts() {
 		return mDb.query(DATABASE_ACCOUNT_TABLE, 
 					new String[] {KEY_ROWID, KEY_USER, KEY_USER_ID, KEY_PASSWORD, KEY_TOKEN, KEY_TOKEN_SECRET, KEY_INSTANCE,KEY_DEFAULT,KEY_VERSION}, 
 					KEY_DEFAULT + " = 0 ", 
 					null, null, null, null);
 	}
 	
 	public Cursor fetchAccount(long rowId) {
 		Cursor mCursor = mDb.query(DATABASE_ACCOUNT_TABLE, 
 				new String[] {
 					KEY_ROWID, 
 					KEY_USER,
 					KEY_USER_ID,
 					KEY_PASSWORD, 
 					KEY_TOKEN,
 					KEY_TOKEN_SECRET, 
 					KEY_INSTANCE,
 					KEY_DEFAULT,
 					KEY_VERSION,
 					KEY_TEXTLIMIT
 					}, 
 				KEY_ROWID + "=" + rowId, 
 				null, null, null, null);
 		return mCursor;
 	}
 	
 	public Cursor fetchDefaultAccount() {
 		Cursor mCursor = mDb.query(DATABASE_ACCOUNT_TABLE, 
 				new String[] {
 					KEY_ROWID, 
 					KEY_USER, 
 					KEY_USER_ID,
 					KEY_PASSWORD, 
 					KEY_TOKEN, 
 					KEY_TOKEN_SECRET, 
 					KEY_INSTANCE, 
 					KEY_DEFAULT, 
 					KEY_VERSION,
 					KEY_TEXTLIMIT
 					}, 
 				KEY_DEFAULT + "= 1" , null,
 				null, null, null);
 		if (mCursor.getCount()<1) {
 			// Try ...
 			if(resetDefaultAccount())
 				return fetchDefaultAccount();
 		}
 		return mCursor;
 	}
 	
 	public boolean resetDefaultAccounts() {
 		ContentValues args = new ContentValues();
         args.put(KEY_DEFAULT, 0);
         return mDb.update(DATABASE_ACCOUNT_TABLE, args, null, null) > 0;
 	}
 	
 	public boolean setUserIdAccount(long _id, long userid) {
 		ContentValues args = new ContentValues();
         args.put(KEY_USER_ID, userid);
         return mDb.update(DATABASE_ACCOUNT_TABLE, args, KEY_ROWID + "= " + _id, null) > 0;
 	}
 	
 	public boolean setDefaultAccount(long _id) {
 		ContentValues args = new ContentValues();
         args.put(KEY_DEFAULT, 1);
         return mDb.update(DATABASE_ACCOUNT_TABLE, args, KEY_ROWID + "= " + _id, null) > 0;
 	}
 	
 	public boolean setVersionAccount(long _id,String version) {
 		ContentValues args = new ContentValues();
         args.put(KEY_VERSION, version);
         return mDb.update(DATABASE_ACCOUNT_TABLE, args, KEY_ROWID + "= " + _id, null) > 0;
 	}
 
 	public boolean setVersionInstance(String instance,String version) {
 		ContentValues args = new ContentValues();
         args.put(KEY_VERSION, version);
         return mDb.update(DATABASE_ACCOUNT_TABLE, args, KEY_INSTANCE + " = '" + instance + "'", null) > 0;
 	}
 
 	public boolean setTextlimitInstance(String instance,int textlimit) {
 		ContentValues args = new ContentValues();
         args.put(KEY_TEXTLIMIT, textlimit);
         return mDb.update(DATABASE_ACCOUNT_TABLE, args, KEY_INSTANCE + " = '" + instance + "'", null) > 0;
 	}
 	
 	public long fetchMaxStatusesId(int rowtype, String extra) {
 		long ret=-1;
 		Cursor mCursor =
 			mDb.query(DATABASE_STATUS_TABLE, 
 					new String[] { "MAX("+KEY_STATUS_ID+")" 
 			}, 
 			KEY_ROWTYPE + " = " + rowtype + " and " + KEY_ROWEXTRA + " = '" + extra + "'",
 			null,
 			null, null, null, null);
 		if (mCursor != null) {
 			if(mCursor.moveToFirst())
 				ret = mCursor.getLong(0);
 			mCursor.close();
 			
 		}
 		return ret;
 	}
 	
 	public long fetchMinStatusesId(int rowtype, String extra) {
 		long ret=-1;
 		Cursor cursor =
 			mDb.query(DATABASE_STATUS_TABLE, 
 					new String[] { "MIN("+KEY_STATUS_ID+")" 
 			}, 
 			KEY_ROWTYPE + " = " + rowtype + " and " + KEY_ROWEXTRA + " = '" + extra + "'",
 			null,
 			null, null, null, null);
 		if (cursor != null) {
 			if(cursor.moveToFirst())
 				ret = cursor.getLong(0);
 			cursor.close();
 			
 		}
 		return ret;
 	}
 	
 	public boolean deleteAccount(long userId) {
 		mDb.delete(DATABASE_ACCOUNT_TABLE, KEY_ROWID + "= " + userId , null);
 		resetDefaultAccount();
 		return true;
 	}
 	
 	public boolean deleteBookmarks(long userId) {
 		mDb.delete(DATABASE_BOOKMARK_TABLE, KEY_USER_ID + " = " + userId , null);
 		return true;
 	}
 
 	public boolean resetDefaultAccount() {
 		boolean ret = false;
 		Cursor cursor =
 			mDb.query(DATABASE_ACCOUNT_TABLE, 
 					new String[] { "MAX("+KEY_ROWID+")" 
 			}, 
 			null,
 			null,
 			null, null, null, null);
 		if (cursor != null) {
 			if(cursor.moveToFirst()) {
 				Long _id = cursor.getLong(0);
 				if (_id!=null && _id>0) {
 					if(setDefaultAccount(_id))
 						ret=true;
 				}
 			}
 			cursor.close();
 		}
 		return ret;
 	}
 
 	public long userExists(String username,String instance) {
 		long ret = -1;
 		Cursor cursor = mDb.query(DATABASE_ACCOUNT_TABLE, 
 				new String[] {KEY_ROWID}, 
 				KEY_USER + " = '" + username + "' AND " + KEY_INSTANCE + " = '" + instance + "'", 
 				null, null, null, null);
 		if(cursor.moveToFirst()) {
 			ret = cursor.getLong(cursor.getColumnIndex(KEY_ROWID));
 		}
 		try {
 			cursor.close();
 		}catch(Exception e){
 		}
 		return ret;
 	}
 	
 	public Cursor fetchUserBookmarksByType(long userid,int btype) {
 		return mDb.query(true,
 				DATABASE_BOOKMARK_TABLE, 
 				new String[] {
 				KEY_ROWID, KEY_BPARAM
 			},
 			KEY_USER_ID + " = " + userid + " and " + KEY_BTYPE + " = " + btype , 
 			null, null, null, null, null);
 	}
 	
 	public String fetchBookmark(long id) {
 		String param="";
 		Cursor mCursor = null;
 		try {
 			mCursor = mDb.query(DATABASE_BOOKMARK_TABLE, 
 					new String[] { KEY_BPARAM, KEY_BTYPE },
 			KEY_ROWID + " = " + id,
 			null,
 			null, null, KEY_COUNTER + ", " + KEY_BPARAM, null);
 			if (mCursor != null) {
 				if(mCursor.moveToFirst())
 					param = mCursor.getString(0);
 			}
 		} finally {
 			mCursor.close();
 		}
 		return param;
 	}
 	
 	public boolean createBookmark(long userid,int btype, String bparam) throws MustardException {
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_USER_ID, userid);
 		initialValues.put(KEY_BTYPE, btype);
 		initialValues.put(KEY_BPARAM, bparam);
 		initialValues.put(KEY_COUNTER, 0);
 		try {
 			mDb.insertOrThrow(DATABASE_BOOKMARK_TABLE, null, initialValues);
 		} catch(SQLiteConstraintException e) {
 			throw new MustardException(e.getMessage() == null ? e.toString() : e.getMessage());
 		}
 		return true;
 	}
 
 	public boolean increaseBookmarkCounter(long id) {
 		try {
 			mDb.execSQL("update bookmarks set counter = counter +1 where _id = " + id);
 		} catch(Exception e) {
 			return false;
 		}
 		return true;
 	}
 
 	public boolean deleteBookmark(long id) {
 		try {
 			mDb.delete(DATABASE_BOOKMARK_TABLE, KEY_ROWID + "= " + id , null);
 		} catch(Exception e) {
 			return false;
 		}
 		return true;
 	}
 	
 	
 	public Cursor fetchFilters() {
 		Cursor mCursor = mDb.query(DATABASE_FILTER_TABLE, 
 					new String[] { KEY_ROWID, KEY_FILTER },
 			null,
 			null,
 			null, null, null, null);
 		return mCursor;
 	}
 	
 	public boolean createFilter(String filter) throws MustardException {
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_FILTER, filter);
 		try {
 			mDb.insertOrThrow(DATABASE_FILTER_TABLE, null, initialValues);
 		} catch(SQLiteConstraintException e) {
 			throw new MustardException(e.getMessage() == null ? e.toString() : e.getMessage());
 		}
 		return true;
 	}
 
 	public boolean deleteFilter(long id) {
 		try {
 			mDb.delete(DATABASE_FILTER_TABLE, KEY_ROWID + "= " + id , null);
 		} catch(Exception e) {
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean insertOauth(String instance, String key, String secret,boolean reserved) {
 		Cursor c = fetchOauth(instance);
 		if (c.moveToFirst()) {
 			int iReserved = c.getInt(c.getColumnIndex(KEY_RESERVED));
 			if (iReserved == 1 && !reserved) {
 				c.close();
 				return false;
 			}
 			String localKey=c.getString(c.getColumnIndex(KEY_KEY));
 			String localSecret=c.getString(c.getColumnIndex(KEY_SECRET));
 			if (localKey.equals(key) && localSecret.equals(secret)) {
 				c.close();
 				return true;
 			}
 		}
 		c.close();
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_INSTANCE, instance);
 		initialValues.put(KEY_KEY, key);
 		initialValues.put(KEY_SECRET, secret);
 		initialValues.put(KEY_RESERVED, (reserved ? 1 : 0));
 		long _rid = -1;
 		try {
 			if (mDb == null) {
 				Log.d(TAG, "mDb is null!");
 				return false;
 			}
 			_rid  = mDb.insertOrThrow(DATABASE_OAUTH_TABLE, null, initialValues);
 		} catch(SQLiteConstraintException e) {
 			// No problem, update
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 		if (_rid < 0) {
 			return updateOauth(instance, key, secret);
 		} else {
 			return true;
 		}
 	}
 	
 	public boolean updateOauth(String instance, String key, String secret) {
 		ContentValues args = new ContentValues();
         args.put(KEY_KEY, key);
         args.put(KEY_SECRET, secret);
         mDb.update(DATABASE_OAUTH_TABLE, args, KEY_INSTANCE + "= '" + instance + "'", null);
         return true;
 	}
 	
 	public boolean deleteOauth(long id) {
 		try {
 			mDb.delete(DATABASE_OAUTH_TABLE, KEY_ROWID + " = " + id , null);
 		} catch(Exception e) {
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean deleteAllOauth() {
 		try {
 			mDb.delete(DATABASE_OAUTH_TABLE, null, null);
 		} catch(Exception e) {
 			return false;
 		}
 		return true;
 	}
 	
 	public Cursor fetchOauth(long id) {
 		Cursor mCursor = mDb.query(
 				DATABASE_OAUTH_TABLE, 
 				new String[] {
 						KEY_ROWID, KEY_INSTANCE, KEY_KEY, KEY_SECRET, KEY_RESERVED
 						},
 				KEY_ROWID + "=" + id, 
 				null, null, null, null);
 		return mCursor;
 	}
 	
 	public Cursor fetchOauth(String instance) {
 		Cursor mCursor = mDb.query(
 				DATABASE_OAUTH_TABLE, 
 				new String[] {
 						KEY_ROWID, KEY_INSTANCE, KEY_KEY, KEY_SECRET, KEY_RESERVED
 						},
 						KEY_INSTANCE + "= '" + instance + "'", 
 				null, null, null, null);
 		return mCursor;
 	}
 	
 }
