 package de.binaervarianz.holopod.db;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 import android.util.Log;
 
 public class DatabaseHandler extends SQLiteOpenHelper {
 
 	// Database Version
 	private static final int DATABASE_VERSION = 2;
 
 	// Database Name
 	private static final String DATABASE_NAME = "podcast.db";
 
 	// Tables
 	private static final String TABLE_CHANNEL = "channel";
 	private static final String TABLE_EPISODE = "episode";
 
 	// Columns - Channel
 	private static final String CHANNEL_ID = "_id";
 	private static final String CHANNEL_URL = "url";
 	private static final String CHANNEL_TITLE = "title";
 	private static final String CHANNEL_SUBTITLE = "subtitle";
 	private static final String CHANNEL_DESCRIPTION = "description";
 	private static final String CHANNEL_LINK = "link";
 	private static final String CHANNEL_IMAGE = "image";
 	private static final String CHANNEL_AUTHOR = "author";
 	private static final String CHANNEL_COPYRIGHT = "copyright";
 	private static final String CHANNEL_LASTUPDATED = "lastupdated";
 
 	// Columns - Channel
 	private static final String EPISODE_ID = "_id";
 	private static final String EPISODE_CHANNEL = "channel"; // channel._id
 	private static final String EPISODE_TITLE = "title";
 	private static final String EPISODE_SUBTITLE = "subtitle";
 	private static final String EPISODE_DESCRIPTION = "description";
 	private static final String EPISODE_LINK = "link";
 	private static final String EPISODE_IMAGE = "image"; // picture._id
 	private static final String EPISODE_AUTHOR = "author";
 	private static final String EPISODE_KEYWORDS = "keywords";
 	private static final String EPISODE_LASTUPDATED = "lastupdated";
 	private static final String EPISODE_PLAYCOUNT = "playcount";
 	private static final String EPISODE_ENC_URL = "enc_url";
 	private static final String EPISODE_ENC_SIZE = "enc_size";
 	private static final String EPISODE_ENC_RCVSIZE = "enc_rcvsize";
 	private static final String EPISODE_DURATION = "enc_duration";
 	private static final String EPISODE_ENC_TYPE = "enc_type";
 	private static final String EPISODE_ENC_FILEPATH = "enc_filepath";
 	private static final String EPISODE_ENC_ONDEVICE = "enc_onDevice";
 	private static final String EPISODE_ENC_PAUSEDTIME = "enc_pausedTime";
 	private static final String EPISODE_ENC_DLDATE = "enc_dlDate";
 	private static final String EPISODE_PUBDATE = "pubDate";
 	private static final String EPISODE_ARCHIVE = "archive";
 
 	private static final String[] CHANNEL_FIELDS = { CHANNEL_ID, CHANNEL_URL,
 			CHANNEL_TITLE, CHANNEL_SUBTITLE, CHANNEL_DESCRIPTION, CHANNEL_LINK,
 			CHANNEL_IMAGE, CHANNEL_AUTHOR, CHANNEL_COPYRIGHT,
 			CHANNEL_LASTUPDATED };
 
 	private static final String[] EPISODE_FIELDS = { EPISODE_ID,
 			EPISODE_CHANNEL, EPISODE_TITLE, EPISODE_SUBTITLE,
 			EPISODE_DESCRIPTION, EPISODE_LINK,
 			// EPISODE_IMAGE, EPISODE_AUTHOR,
 			// EPISODE_KEYWORDS, EPISODE_LASTUPDATED, EPISODE_PLAYCOUNT
 			EPISODE_ENC_URL, EPISODE_ENC_SIZE,
 			// EPISODE_ENC_RCVSIZE, EPISODE_DURATION,
 			EPISODE_ENC_TYPE, EPISODE_ENC_FILEPATH, EPISODE_ENC_ONDEVICE,
 	// EPISODE_ENC_PAUSEDTIME, EPISODE_ENC_DLDATE, EPISODE_PUBDATE,
 	// EPISODE_ARCHIVE
 	};
 
 	private Context context;
 
 	public DatabaseHandler(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		this.context = context;
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		// @formatter:off
 		String CREATE_CHANNEL_TABLE = "CREATE TABLE " + TABLE_CHANNEL + " ("
 				+ CHANNEL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
 				+ CHANNEL_URL + " TEXT NOT NULL,"
 				+ CHANNEL_TITLE + " TEXT,"
 				+ CHANNEL_SUBTITLE + " TEXT,"
 				+ CHANNEL_DESCRIPTION + " TEXT,"
 				+ CHANNEL_LINK + " TEXT,"
 				+ CHANNEL_IMAGE + " INTEGER DEFAULT 0,"
 				+ CHANNEL_AUTHOR + " TEXT,"
 				+ CHANNEL_COPYRIGHT + " TEXT,"
 				+ CHANNEL_LASTUPDATED + " INTEGER"
 				+ ")";
 		
 		String CREATE_EPISODE_TABLE = "CREATE TABLE " + TABLE_EPISODE + " ("
 				+ EPISODE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
 				+ EPISODE_CHANNEL + " INTEGER NOT NULL,"
 				+ EPISODE_TITLE + " TEXT,"
 				+ EPISODE_SUBTITLE + " TEXT,"
 				+ EPISODE_DESCRIPTION + " TEXT,"
 				+ EPISODE_LINK + " TEXT,"
 				+ EPISODE_IMAGE + " INTEGER DEFAULT 0,"
 				+ EPISODE_AUTHOR + " TEXT,"
 				+ EPISODE_KEYWORDS + " TEXT,"
 				+ EPISODE_PLAYCOUNT + " INTEGER DEFAULT 0,"
 				+ EPISODE_LASTUPDATED + " INTEGER,"
 				+ EPISODE_ENC_URL + " TEXT,"
 				+ EPISODE_ENC_SIZE + " INTEGER,"
 				+ EPISODE_ENC_RCVSIZE + " INTEGER,"
 				+ EPISODE_DURATION + " INTEGER,"
 				+ EPISODE_ENC_TYPE + " TEXT,"
 				+ EPISODE_ENC_FILEPATH + " TEXT,"
 				+ EPISODE_ENC_ONDEVICE + " INTEGER DEFAULT 0"
 				+ ")";
 		// @formatter:on
 		Log.i("onCreate", CREATE_CHANNEL_TABLE);
 		db.execSQL(CREATE_CHANNEL_TABLE);
 		Log.i("onCreate", CREATE_EPISODE_TABLE);
 		db.execSQL(CREATE_EPISODE_TABLE);
 
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHANNEL);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_EPISODE);
 		onCreate(db);
 	}
 
 	// **********************
 	// start of podcast channel
 	// **********************
 
 	// Add Channel (subscribe)
 	public Boolean addChannel(Channel channel) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(CHANNEL_TITLE, channel.getTitle());
 		values.put(CHANNEL_URL, channel.getUrl());
 		values.put(CHANNEL_SUBTITLE, channel.getSubtitle());
 		values.put(CHANNEL_DESCRIPTION, channel.getDescription());
 		values.put(CHANNEL_LINK, channel.getLink());
 		values.put(CHANNEL_IMAGE, channel.getImage());
 		values.put(CHANNEL_AUTHOR, channel.getAuthor());
 		values.put(CHANNEL_COPYRIGHT, channel.getCopyright());
 		// values.put(CHANNEL_LASTUPDATED, channel.getLastupdated());
 		// TODO check if URL is already present in database
 		long result = db.insert(TABLE_CHANNEL, null, values);
 		if (result > 0) {
 			channel.setId(result);
 		}
 		db.close();
 
 		// TODO return false if already in database
 		return true;
 	}
 
 	public Boolean updateChannel(Channel channel) {
 		// TODO
 		return true;
 	}
 
 	public Channel getChannel(long id) {
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		Cursor result = db.query(TABLE_CHANNEL, CHANNEL_FIELDS, CHANNEL_ID
 				+ "=?", new String[] { String.valueOf(id) }, null, null, null,
 				"1");
 
 		if (result != null) {
 			result.moveToFirst();
 			return new Channel(result.getLong(0), result.getString(1),
 					result.getString(2), result.getString(3),
 					result.getString(4), result.getString(5),
 					result.getLong(6), result.getString(7),
 					result.getString(8), result.getString(9));
 		} else {
 			return null; // no channel with this id found in database
 		}
 	}
 
 	public Channel getChannel(String url) {
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		Cursor result = db.query(TABLE_CHANNEL, CHANNEL_FIELDS, CHANNEL_URL
 				+ "=?", new String[] { String.valueOf(url) }, null, null, null,
 				"1");
 		if (result != null) {
 			result.moveToFirst();
 			return new Channel(result.getLong(0), result.getString(1),
 					result.getString(2), result.getString(3),
 					result.getString(4), result.getString(5),
 					result.getLong(6), result.getString(7),
 					result.getString(8), result.getString(9));
 		} else {
 			return null; // no channel with this id found in database
 		}
 	}
 
 	// get all channels in database
 	public List<Channel> getSubscriptions() {
 		List<Channel> channelList = new ArrayList<Channel>();
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		Cursor result = db.rawQuery("SELECT * FROM " + TABLE_CHANNEL, null);
 		// create channel list from results
 		if (result.moveToFirst()) {
 			do {
 				Channel channel = new Channel(result.getLong(0),
 						result.getString(1), result.getString(2),
 						result.getString(3), result.getString(4),
 						result.getString(5), result.getLong(6),
 						result.getString(7), result.getString(8),
 						result.getString(9));
 				channelList.add(channel);
 			} while (result.moveToNext());
 		}
 		db.close();
 		return channelList;
 	}
 
 	// remove channel
 	public Boolean removeChannel(Channel channel) {
 		SQLiteDatabase db = this.getWritableDatabase();
 
 		// delete all episodes from this channel
 		db.delete(TABLE_EPISODE, EPISODE_CHANNEL + " = ?",
 				new String[] { String.valueOf(channel.getId()) });
 
 		//PictureHandler pic_db = new PictureHandler(this.context);
 		// TODO query for count image
 
 		// delete channel
 		int rmCnt = db.delete(TABLE_CHANNEL, CHANNEL_ID + " = ?",
 				new String[] { String.valueOf(channel.getId()) });
 		db.close();
 		return (rmCnt > 0) ? true : false;
 	}
 
 	// **********************
 	// start of podcast items
 	// **********************
 
 	// add podcast episodes
 	public Boolean addEpisode(Episode episode) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(EPISODE_CHANNEL, episode.getChannel());
 		values.put(EPISODE_TITLE, episode.getTitle());
 		values.put(EPISODE_SUBTITLE, episode.getSubtitle());
 		values.put(EPISODE_DESCRIPTION, episode.getDescription());
 		values.put(EPISODE_LINK, episode.getLink());
 		values.put(EPISODE_ENC_URL, episode.getEncUrl());
 		values.put(EPISODE_ENC_SIZE, episode.getEncSize());
 		values.put(EPISODE_ENC_TYPE, episode.getEncType());
 		long result = db.insert(TABLE_EPISODE, null, values);
 		if (result > 0) {
 			episode.setId(result);
 		}
 		db.close();
 		return true;
 	}
 
 	public List<Episode> getEpisodesByChannel(Channel channel) {
 		List<Episode> episodeList = new ArrayList<Episode>();
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		Cursor result = db.query(TABLE_EPISODE, EPISODE_FIELDS, EPISODE_CHANNEL
 				+ "=?", new String[] { String.valueOf(channel.getId()) }, null,
 				null, null, null);
 		// create channel list from results
 		if (result.moveToFirst()) {
 			do {
 				Episode episode = new Episode(result.getLong(0),
 						result.getLong(1), result.getString(2),
 						result.getString(3), result.getString(4),
 						result.getString(5), result.getString(6),
 						result.getLong(7), result.getString(8),
 						result.getString(9), result.getInt(10)>0);
 				episodeList.add(episode);
 			} while (result.moveToNext());
 		}
 		db.close();
 		return episodeList;
 	}
 
 	public void updateEpisode(Episode episode) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		if (!episode.getEncFilepath().isEmpty()) {
 			values.put(EPISODE_ENC_FILEPATH, episode.getEncFilepath());
 		}
		values.put(EPISODE_ENC_ONDEVICE, episode.getEncOnDevice());
 		db.update(TABLE_EPISODE, values,
 				EPISODE_ID + " = " + String.valueOf(episode.getId()), null);
 	}
 }
