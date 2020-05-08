 package dk.aau.sw802f12.proto3.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 /**
  * The database helper to interact with the database.
  * 
  * @author sw802f12 (mlisby)
  *
  */
 class DBHelper extends SQLiteOpenHelper {
 	static final String TAG = "SW8DBH";
 	
 	/**
 	 * Database helper class for table and column names
 	 * @author sw802f12 (mlisby)
 	 */
 	class DB {
 		/**
 		 * Database version.
 		 */
 		private static final int VERSION = 1;
 		/**
 		 * Database name.
 		 */
 		private static final String NAME = "socialMusic";
 		
 		/**
 		 * Artist table name.
 		 */
 		private static final String TB_ARTIST = "tbl_artist";
 		/**
 		 * Artist name column of artist table.
 		 */
 		private static final String ARTIST_NAME = "name";
 		
 		/**
 		 * Song table name.
 		 */
 		private static final String TB_SONG = "tbl_song";
 		/**
 		 * Artist column of Song table.
 		 */
 		private static final String SONG_ARTIST = "artist";
 		/**
 		 * Song title column of Song table.
 		 */
 		private static final String SONG_TITLE = "title";
 		/**
 		 * Song host column of Song table.
 		 */
 		private static final String SONG_HOST = "host";
 		/**
 		 * Song location column of Song table.
 		 */
 		private static final String SONG_LOCATION = "location";
 		
 		/**
 		 * Tag table name.
 		 */
 		private static final String TB_TAG = "tbl_tag";
 		/**
 		 * Tag value column of Tag table.
 		 */
 		private static final String TAG_NAME = "tagname";
 		
 		/**
 		 * SongTag relation table name.
 		 */
 		private static final String TB_SONGTAGS = "tbl_songtags";
 		/**
 		 * Song id column of SongTag relation table.
 		 */
 		private static final String SONGTAG_SONG = "song";
 		/**
 		 * Tag id column of SongTag relation table.
 		 */
 		private static final String SONGTAG_TAG = "tag";
 		
 		/**
 		 * ArtistTag relation table name.
 		 */
 		private static final String TB_ARTISTTAGS = "tbl_artisttags";
 		/**
 		 * Tag id column of ArtistTag relation table.
 		 */
 		private static final String ARTISTTAG_TAG = "tag";
 		/**
 		 * Artist id column of ArtistTag relation table.
 		 */
 		private static final String ARTISTTAG_ARTIST = "artist";
 		/**
 		 * User table name.
 		 */
 		private static final String TB_USER = "tbl_user";
 		/**
 		 * User address column of User table.
 		 */
 		private static final String USER_ADDRESS = "address";
 		/**
 		 * LastFM username column of User table.
 		 */
 		private static final String USER_LASTFM = "lastfm_username";
 		/**
 		 * UserArtist relation table name.
 		 */
 		private static final String TB_USERARTIST = "tbl_userartist";
 		/**
 		 * User column of the UserArtist relation table.
 		 */
 		private static final String USERARTIST_USER = "user";
 		/**
 		 * Artist column of the UserArtist relation table.
 		 */
 		private static final String USERARTIST_ARTIST = "artist";
 		/**
 		 * Rating column of the UserArtist relation table.
 		 */
 		private static final String USERARTIST_RATING = "rating";
 		
 		private static final String TB_SIMILARARTIST = "similar_artist";
 		private static final String SIMILARARTIST_FIRST_ARTIST = "simart1";
 		private static final String SIMILARARTIST_SECOND_ARTIST = "simart2";
 		private static final String SIMILARARTIST_RATING = "simartsimilarity";
 		
 		/**
 		 * Create artist table.
 		 */
 		private static final String SQL_ARTIST
 									= "create table " + DB.TB_ARTIST + " ("
 									+ DB.ARTIST_NAME + " text)";
 		
 		/**
 		 * Create song table.
 		 */
 		private static final String SQL_SONG
 									= "CREATE TABLE " + DB.TB_SONG + " ("
 									+ DB.SONG_ARTIST + " INTEGER, "
 									+ DB.SONG_TITLE + " TEXT, "
 									+ DB.SONG_HOST + " INTEGER, "
 									+ DB.SONG_LOCATION + " TEXT)";
 		
 		/**
 		 * Create Tag table.
 		 */
 		private static final String SQL_TAG
 									= "CREATE TABLE " + DB.TB_TAG + " ("
 										+ DB.TAG_NAME + " TEXT)";
 		
 		/**
 		 * Create SongTag relation table.
 		 */
 		private static final String SQL_SONGTAG
 									= "CREATE TABLE " + DB.TB_SONGTAGS + " ("
 									+ DB.SONGTAG_SONG + " INTEGER, "
 									+ DB.SONGTAG_TAG + " INTEGER,"
 									+ "PRIMARY KEY(" + DB.SONGTAG_SONG + ", " + DB.SONGTAG_TAG + "))";
 		
 		/**
 		 * Create ArtistTag relation table.
 		 */
 		private static final String SQL_ARTISTTAG
 									= "CREATE TABLE " + DB.TB_ARTISTTAGS + " ("	
 									+ DB.ARTISTTAG_ARTIST + " INTEGER, " 
 									+ DB.ARTISTTAG_TAG + " INTEGER, "
 									+ " PRIMARY KEY(" + DB.ARTISTTAG_ARTIST + "," + DB.ARTISTTAG_TAG + "))";
 		
 		private static final String SQL_USER
 									= "CREATE TABLE " + DB.TB_USER + "("
 									+ DB.USER_ADDRESS + " STRING, "
 									+ DB.USER_LASTFM + " STRING)";
 		
 		private static final String SQL_USERARTIST
 									= "CREATE TABLE " + DB.TB_USERARTIST + " ("
 									+ DB.USERARTIST_ARTIST + " INTEGER, "
 									+ DB.USERARTIST_USER + " INTEGER, "
 									+ DB.USERARTIST_RATING + " INTEGER, "
 									+ "PRIMARY KEY(" + DB.USERARTIST_ARTIST + "," + DB.USERARTIST_USER + "))";
 		
 		private static final String SQL_SIMILARARTIST
 									= "CREATE TABLE " + DB.TB_SIMILARARTIST + " ("
 									+ DB.SIMILARARTIST_FIRST_ARTIST + " INTEGER, "
 									+ DB.SIMILARARTIST_SECOND_ARTIST + " INTEGER, "
 									+ DB.SIMILARARTIST_RATING + " INTEGER)";
 	}
 		
 	private SQLiteDatabase db;
 	private MusicRegistry mr;
 	
 	private void openDB() {
 		if (db != null && !db.isOpen()) {
 			db.close();
 			db = null;
 		}
 		
 		if (db == null) 
 			db = getWritableDatabase();
 	}
 	
 	private void closeDB() {
 		db.close();
 		db = null;
 	}
 	
 	DBHelper(Context context) {
 		super(context, DB.NAME, null, DB.VERSION);
 		mr = MusicRegistry.getInstance(context);
 	}
 	
 	protected void finalize() {
 		closeDB();
 	}
 
 	private void create() throws SQLException {
 		openDB();
 		db.execSQL(DB.SQL_ARTIST);
 		db.execSQL(DB.SQL_SONG);
 		db.execSQL(DB.SQL_TAG);
 		db.execSQL(DB.SQL_SONGTAG);
 		db.execSQL(DB.SQL_ARTISTTAG);
 		db.execSQL(DB.SQL_USER);
 		db.execSQL(DB.SQL_USERARTIST);
 		db.execSQL(DB.SQL_SIMILARARTIST);
 	}
 		
 	@Override
 	public void onCreate(SQLiteDatabase dbs) {
 		db = dbs;
 		//Create database skeleton
 		db.beginTransaction();
 		try {
 			create();
 			db.setTransactionSuccessful();
 		} catch (SQLException e) {
 		} finally {
 			db.endTransaction();
 		}
 	}
 	
 	@Override
 	public void onUpgrade(SQLiteDatabase dbs, int oldVersion, int newVersion) {
 		db = dbs;
 		db.execSQL("drop table if exists " + DB.TB_ARTIST);
 		db.execSQL("drop table if exists " + DB.TB_SONG);
 		db.execSQL("drop table if exists " + DB.TB_TAG);
 		db.execSQL("drop table if exists " + DB.TB_SONGTAGS);
 		db.execSQL("drop table if exists " + DB.TB_ARTISTTAGS);
 		db.execSQL("drop table if exists " + DB.TB_USER);
 		db.execSQL("drop table if exists " + DB.TB_USERARTIST);
 		db.execSQL("drop table if exists " + DB.TB_SIMILARARTIST);
 		onCreate(db);
 	}
 	
 	@Override
 	public void onOpen(SQLiteDatabase dbs) {
 		db = dbs;
 		//onUpgrade(db, 0,0);
 	}
 	
 	/**
 	 * Empty the database (when the library is no longer up to date).
 	 */
 	void clearDB() {
 		openDB();
 		String truncate = "truncate table if exists ";
 		db.execSQL(truncate + DB.TB_ARTIST);
 		db.execSQL(truncate + DB.TB_SONG);
 		db.execSQL(truncate + DB.TB_TAG);
 		db.execSQL(truncate + DB.TB_SONGTAGS);
 		db.execSQL(truncate + DB.TB_ARTISTTAGS);
 		db.execSQL(truncate + DB.TB_USER);
 		db.execSQL(truncate + DB.TB_USERARTIST);
 	}
 	
 	/**
 	 * Update database with the provided {@link Song}.
 	 * If the {@link Song} already exists in the database, the Song is updated.
 	 * If it does not yet exist, it will be inserted.
 	 * @param song The Song to insert or update.
 	 * @return The id of the Song in the database.
 	 */
 	long updateDB(Song song) {
 		removeRelations(song);
 		long ret = -1;
 		if (song.getId() == -1) {
 			ret = add(song);
 		}
 		else ret = update(song);
 		
 		for (Tag tag : song.getTags()) {
 			add(song, tag);
 		}
 		
 		return ret;
 	}
 	
 	/**
 	 * Update database with the provided {@link Artist}.
 	 * If the Artist already exists in the database, the Artist is updated.
 	 * If it does not yet exist, it will be inserted.
 	 * @param artist The Artist to insert or update.
 	 * @return The id of the Artist in the database.
 	 */
 	long updateDB(Artist artist) {
 		removeRelations(artist);
 		long ret;
 		if (artist.getId() == -1) ret = add(artist);
 		else ret = update(artist);
 		
 		HashMap<User, Short> hm = artist.getUsers();
 		for (User user : hm.keySet()) {
 			add(artist, user, hm.get(user));
 		}
 		
 		for (Tag tag : artist.getTags()) {
 			add(artist, tag);
 		}
 		
 		return ret;
 	}
 	
 	/**
 	 * Update database with the provided {@link Tag}.
 	 * If the {@link Tag} already exists in the database, the Tag is updated.
 	 * If it does not yet exist, it will be inserted.
 	 * @param tag The Tag to update or add.
 	 * @return The id of the Tag in the Database.
 	 */
 	long updateDB(Tag tag) {
 		removeRelations(tag);
 		
 		long tId = (tag.getId() == -1) ? add(tag) : update(tag);
 		
 		for (Artist artist : tag.getTaggedArtists()) {
 			add(artist, tag);
 		}
 		
 		for (Song song : tag.getTaggedSongs()) {
 			add(song, tag);
 		}
 		return tId;
 	}
 	
 	/**
 	 * Update database with the provided {@link User}.
 	 * If the User already exists in the database, the User is updated.
 	 * If it does not yet exist, it will be inserted.
 	 * @param user The User to update or add.
 	 * @return The id of the User in the Database.
 	 */
 	long updateDB(User user) {
 		long ret;
 		removeRelations(user);
 		if (user.getId() == -1) ret = add(user);
 		else ret = update(user);
 		
 		HashMap<Artist, Short> hm = user.getRatedArtists();
 		for (Artist artist : hm.keySet()) {
 			add(artist, user, hm.get(artist));
 		}
 		
 		return ret;
 	}
 	
 	/**
 	 * Update the {@link Song} in the database.
 	 * @param song The Song to update.
 	 * @return The id of the Song.
 	 */
 	private long update(Song song) {
 		openDB();
 		String whereClause = "rowid = " + song.getId();
 		return db.update(DB.TB_SONG, getCV(song), whereClause, null);
 	}
 	
 	/**
 	 * Update the {@link Artist} in the database.
 	 * @param artist The Artist to update.
 	 * @return The id of the Artist.
 	 */
 	private long update(Artist artist) {
 		openDB();
 		String whereClause = "rowid = " + artist.getId();	
 		return db.update(DB.TB_ARTIST, getCV(artist), whereClause, null);
 	}
 	
 	/**
 	 * Update the {@link Tag} in the database.
 	 * @param tag The Tag to update.
 	 * @return The id of the Tag.
 	 */
 	private long update(Tag tag) {
 		openDB();
 		String whereClause = "rowid = " + tag.getId();
 		return db.update(DB.TB_TAG, getCV(tag), whereClause, null);
 	}
 	
 	/**
 	 * Update the {@link User} in the database.
 	 * @param user The User to update.
 	 * @return The id of the User.
 	 */
 	private long update(User user) {
 		openDB();
 		String whereClause = "rowid = " + user.getId();
 		return db.update(DB.TB_USER, getCV(user), whereClause, null);
 	}
 	
 	/**
 	 * Add a {@link Song} to the database.
 	 * Also updates the id of the Song.
 	 * @param song The Song to add.
 	 * @return The id, the Song has been assigned.
 	 */
 	private long add(Song song) {
 		openDB();
 		long res = db.insert(DB.TB_SONG, null, getCV(song));
 		song.setId(res);
 		mr.add(song);
 		return res;
 	}
 	
 	/**
 	 * Add the {@link Artist} to the database.
 	 * @param artist The Artist to insert.
 	 * @return The id, the Artist has been assigned.
 	 */
 	private long add(Artist artist) {
 		openDB();
 		long res = db.insert(DB.TB_ARTIST, null, getCV(artist));
 		artist.setId(res);
 		mr.add(artist);
 		return res;
 	}
 
 	/**
 	 * Add the {@link Tag} to the database.
 	 * @param tag The Tag to insert.
 	 * @return The id, the Tag has been assigned.
 	 */
 	private long add(Tag tag) {
 		openDB();
 		ContentValues cv = getCV(tag);
 		long res = db.insert(DB.TB_TAG, null, cv);
 		tag.setId(res);
 		mr.add(tag);
 		return res;
 	}
 
 	/**
 	 * Add the {@link User} to the database.
 	 * @param user The User to insert.
 	 * @return The id, the User has been assigned.
 	 */
 	private long add(User user) {
 		openDB();
 		long res = db.insert(DB.TB_USER, null, getCV(user));
 		user.setId(res);
 		mr.add(user);
 		return res;
 	}
 	
 	/**
 	 * Add the provided {@link Tag} to the provided {@link Song}.
 	 * @param song The Song to add the Tag to.
 	 * @param tag The Tag to add.
 	 * @return The database id of the added relation.
 	 */
 	private void add(Song song, Tag tag) {
 		openDB();
 		if (song.getId() == -1) add(song);
 		if (tag.getId() == -1) add(tag);
 		db.insert(DB.TB_SONGTAGS, null, getCV(song, tag));
 	}
 	
 	/**
 	 * Add the provided {@link Tag} to the provided {@link Artist}.
 	 * @param artist The Artist to add the Tag to.
 	 * @param tag The Tag to add.
 	 * @return The database id of the added relation.
 	 */
 	private void add(Artist artist, Tag tag) {
 		openDB();
 		if (artist.getId() == -1) add(artist);
 		if (tag.getId() == -1) add(tag);
		db.insert(DB.TB_ARTISTTAGS, null, getCV(artist, tag));
 	}
 	
 	/**
 	 * Add the provided {@link Artist}-{@link User} relation, with the provided rating.
 	 * @param artist The Artist of the Artist-User relation.
 	 * @param user The User of the Artist-User relation.
 	 * @param rating The rating of the Artist-User relation
 	 */
 	private void add(Artist artist, User user, short rating) {
 		openDB();
 		if (artist.getId() == -1) add(artist);
 		if (user.getId() == -1) add(user);
 		db.insert(DB.TB_USERARTIST, null, getCV(artist, user, rating));
 	}
 	
 	/**
 	 * Generate {@link ContentValues} (for insertion and update) for the provided {@link Song}.
 	 * @param song The Song to generate ContentValues for.
 	 * @return The ContentValues object holding the values.
 	 */
 	private ContentValues getCV(Song song) {
 		ContentValues cv = new ContentValues(4);
 		cv.put(DB.SONG_ARTIST, song.getArtist().getId()); //TODO: Fail-check
 		cv.put(DB.SONG_TITLE, song.getTitle());
 		cv.put(DB.SONG_HOST, song.getHost().getId());
 		cv.put(DB.SONG_LOCATION, song.getLocation());
 		
 		return cv;
 	}
 	
 	/**
 	 * Generate {@link ContentValues} (for insertion and update) for the provided {@link Artist}.
 	 * @param artist The Artist to generate ContentValues for.
 	 * @return The ContentValues object holding the values.
 	 */
 	private ContentValues getCV(Artist artist) {
 		ContentValues cv = new ContentValues(1);
 		cv.put(DB.ARTIST_NAME, artist.getName());
 		return cv;
 	}
 	
 	/**
 	 * Generate {@link ContentValues} (for insertion and update) for the provided {@link Tag}.
 	 * @param tag The Tag to generate ContentValues for.
 	 * @return The ContentValues object holding the values.
 	 */
 	private ContentValues getCV(Tag tag) {
 		ContentValues cv = new ContentValues(1);
 		cv.put(DB.TAG_NAME, tag.getTagName());
 		return cv;
 	}
 	
 	/**
 	 * Generate {@link ContentValues} (for insertion and update) for the provided {@link User}.
 	 * @param user The User to generate ContentValues for.
 	 * @return The ContentValues object holding the values.
 	 */
 	private ContentValues getCV(User user) {
 		ContentValues cv = new ContentValues(2);
 		cv.put(DB.USER_ADDRESS, user.getBtdeviceAddress());
 		try {
 			cv.put(DB.USER_LASTFM, user.getLastfmName());
 		} catch (IllegalAccessException e) {
 			cv.put(DB.USER_LASTFM, "");
 		}
 		return cv;
 	}
 	
 	/**
 	 * Generate {@link ContentValues} (for insertion and update) for the provided {@link Song}-{@link Tag} relation.
 	 * @param song The Song that is part of the Song-Tag relation to generate ContentValues for.
 	 * @param tag The Tag that is part of the Song-Tag relation to generate ContentValues for.
 	 * @return The ContentValues object holding the values.
 	 */
 	private ContentValues getCV(Song song, Tag tag) {
 		ContentValues cv = new ContentValues(2);
 		cv.put(DB.SONGTAG_SONG, song.getId());
 		cv.put(DB.SONGTAG_TAG, tag.getId());
 		return cv;
 	}
 	
 	/**
 	 * Generate {@link ContentValues} (for insertion and update) for the provided {@link Artist}-{@link Tag} relation.
 	 * @param artist The Artist that is part of the Artist-Tag relation to generate ContentValues for.
 	 * @param tag The Tag that is part of the Artist-Tag relation to generate ContentValues for.
 	 * @return The ContentValues object holding the values.
 	 */
 	private ContentValues getCV(Artist artist, Tag tag) {
 		ContentValues cv = new ContentValues(2);
 		cv.put(DB.ARTISTTAG_ARTIST, artist.getId());
 		cv.put(DB.ARTISTTAG_TAG, tag.getId());
 		return cv;
 	}
 	
 	/**
 	 * Generate {@link ContentValues} (for insertion and update) for the {@link Artist}-{@link User} relation.
 	 * @param artist The Artist that is part of the Artist-User relation to generate ContentValues for.
 	 * @param user The User that is part of the Artist-User relation to generate ContentValues for.
 	 * @param rating The user rating of the artist.
 	 * @return The ContentValues object holding the values.
 	 */
 	private ContentValues getCV(Artist artist, User user, short rating) {
 		ContentValues cv = new ContentValues(3);
 		cv.put(DB.USERARTIST_ARTIST, artist.getId());
 		cv.put(DB.USERARTIST_USER, user.getId());
 		cv.put(DB.USERARTIST_RATING, rating);
 		return cv;
 	}
 	
 	/**
 	 * Generate {@link ContentValues} (for insertion and update) for similar {@link Artist}s.
 	 * @param artist1 Artist1 of the Artist pair to set similarity for.
 	 * @param artist2 Artist2 of the Artist pair to set similarity for.
 	 * @param similarity The similarity between the provided artists.
 	 * @return
 	 */
 	private ContentValues getCV(Artist artist1, Artist artist2, Short similarity) {
 		ContentValues cv = new ContentValues(3);
 		cv.put(DB.SIMILARARTIST_FIRST_ARTIST, artist1.getId());
 		cv.put(DB.SIMILARARTIST_SECOND_ARTIST, artist2.getId());
 		cv.put(DB.SIMILARARTIST_RATING, similarity);
 		return cv;
 	}
 	
 	/**
 	 * Retrieve the {@link Song} with the provided id from the database.
 	 * Returns null if the Song does not exist.
 	 * @param songId The id to search for.
 	 * @return The Song with matching id.
 	 */
 	Song getSong(long songId) {
 		openDB();
 		String[] cols = {DB.SONG_ARTIST, DB.SONG_TITLE, DB.SONG_HOST, DB.SONG_LOCATION};
 		Cursor c = db.query(DB.TB_SONG, cols, "rowid = " + songId, null, null, null, null);
 		
 		if (!c.moveToFirst()) return null;
 		
 		long aId = c.getLong(0);
 		long uId = c.getLong(2);
 		String sTitle = c.getString(1);
 		String sLoc = c.getString(3);
 		
 		c.close();
 		
 		Artist a = mr.getArtist(aId);
 		User u = mr.getUser(uId);
 		Song s = new Song(sTitle, a, u, sLoc);
 		songTags(s);
 		s.setId(songId);
 		return s;
 	}
 
 	/**
 	 * Retrieve the {@link Artist} with the provided id from the Database.
 	 * Returns null if the Artist does not exist.
 	 * @param artistId Id to search for.
 	 * @return Artist with matching id.
 	 */
 	Artist getArtist(long artistId) {
 		openDB();
 		
 		String[] cols = {DB.ARTIST_NAME};
 		
 		Cursor c = db.query(DB.TB_ARTIST, cols, "rowid = " + artistId, null, null, null, null);
 		
 		if (!c.moveToFirst()) return null;
 		String aName = c.getString(0);
 		c.close();
 		
 		Artist a = new Artist(aName);
 		artistTags(a);
 		a.setId(artistId);
 		return a;
 	}
 	
 	/**
 	 * Retrieve the {@link Tag} with the provided id from the database.
 	 * Returns null if the Tag does not exist.
 	 * @param tagId The id to search for.
 	 * @return The Tag matching the provided id.
 	 */
 	Tag getTag(long tagId) {
 		openDB();
 		String[] cols = {DB.TAG_NAME};
 		Cursor c = db.query(DB.TB_TAG, cols, "rowid = " + tagId, null, null, null, null);
 		
 		if (!c.moveToFirst()) return null;
 		String tName = c.getString(0);
 		c.close();
 		
 		Tag t = new Tag(tName);
 		taggedSongs(t);
 		taggedArtists(t);
 		return t;
 	}
 	
 	/**
 	 * Retrieve the {@link User} with the provided id from the database.
 	 * Returns null if the User does not exist.
 	 * @param userId The id to search for.
 	 * @return The User matching the provided id.
 	 */
 	User getUser(long userId) {
 		openDB();
 		String[] cols = {DB.USER_ADDRESS, DB.USER_LASTFM};
 		Cursor c = db.query(DB.TB_USER, cols, "rowid = " + userId, null, null, null, null);
 		
 		if (!c.moveToFirst()) return null;
 		String uAddr = c.getString(0);
 		String lastfm = c.getString(1);
 		c.close();
 		
 		User u = new User(uAddr, lastfm);
 		u.setId(userId);
 		userArtists(u);
 		return u;
 	}
 
 	/**
 	 * Add applied {@link Tag}s to the provided {@link Song}.
 	 * @param song The Song to find Tags for.
 	 * @return The Tags applied to the provided Song.
 	 */
 	private void songTags(Song s) {
 		openDB();
 		Collection<Tag> c = new HashSet<Tag>();
 		
 		String[] cols = {DB.SONGTAG_TAG};
 		String selection = DB.SONGTAG_SONG + " = " + s.getId();
 		Cursor cu = db.query(DB.TB_SONGTAGS, cols, selection, null, null, null, null);
 		
 		ArrayList<Long> tagIds = new ArrayList<Long>();
 		
 		while (cu.moveToNext()) {
 			tagIds.add(cu.getLong(0));
 		}
 		cu.close();
 		
 		for (Long tagid : tagIds)
 			c.add(mr.getTag(tagid));
 
 		s.tag(c);
 	}
 	
 	/**
 	 * Add {@link Tag}s to the {@link Artist}, they have been applied to.
 	 * @param artist The Artist to find Tags for.
 	 */
 	private void artistTags(Artist artist) {
 		openDB();
 		Collection<Tag> c = new HashSet<Tag>();
 		
 		String[] cols = {DB.ARTISTTAG_TAG};
 		String selection = DB.ARTISTTAG_ARTIST + " = " + artist.getId();
 		Cursor cu = db.query(DB.TB_ARTISTTAGS, cols, selection, null, null, null, null);
 		
 		ArrayList<Long> tagIds = new ArrayList<Long>();
 		
 		while (cu.moveToNext()) {
 			tagIds.add(cu.getLong(0));
 		}
 		
 		cu.close();
 		
 		for (Long tagId : tagIds)
 			c.add(mr.getTag(tagId));
 		
 		artist.addTags(c);
 	}
 	
 	/**
 	 * Add {@link Artist}s, the provided {@link User} has been suggested, to the User.
 	 * @param user The User to find Artists for.
 	 */
 	private void userArtists(User user) {
 		openDB();
 		HashMap<Artist, Short> hm = new HashMap<Artist, Short>();
 		String[] columns = {DB.USERARTIST_ARTIST, DB.USERARTIST_RATING};
 		String selection = DB.USERARTIST_USER + " = ?";
 		String[] selectionArgs = {user.getId() + ""};
 		
 		Cursor cu = db.query(DB.TB_USERARTIST, columns, selection, selectionArgs, null, null, null);
 		
 		ArrayList<Long> artistIds = new ArrayList<Long>();
 		ArrayList<Short> ratings = new ArrayList<Short>();
 		
 		while (cu.moveToNext()) {
 			artistIds.add(cu.getLong(0));
 			ratings.add(cu.getShort(1));
 		}
 		
 		cu.close();
 		
 		for (int i = 0, s = artistIds.size(); i < s; ++i)
 			hm.put(mr.getArtist(artistIds.get(i)), ratings.get(i));
 		
 		user.addArtistRatings(hm);
 	}
 	
 	/**
 	 * Add {@link Song}s to the {@link Tag}s list of Songs.
 	 * @param tag The Tag to add Songs to.
 	 */
 	private void taggedSongs(Tag tag) {
 		openDB();
 		Collection<Song> songs = new HashSet<Song>();
 		
 		String[] cols = {DB.SONGTAG_SONG};
 		String select = DB.SONGTAG_TAG + "=" + tag.getId();
 		Cursor c = db.query(DB.TB_SONGTAGS, cols, select, null, null, null, null);
 		
 		ArrayList<Long> songIds = new ArrayList<Long>();
 		while (c.moveToNext()) {
 			songIds.add(c.getLong(0));
 		}
 		c.close();
 		
 		for (long l : songIds) {
 			songs.add(mr.getSong(l));
 		}		
 		tag.tagSongs(songs);
 	}
 	
 	/**
 	 * Add {@link Artist}s to the {@link Tag}s list of artists.
 	 * @param tag The Tag to retrieve Artists for.
 	 */
 	private void taggedArtists(Tag tag) {
 		openDB();
 		Collection<Artist> artists = new HashSet<Artist>();
 		
 		String[] cols = {DB.ARTISTTAG_ARTIST};
 		String select = DB.ARTISTTAG_TAG + "=" + tag.getId();
 		Cursor c = db.query(DB.TB_ARTISTTAGS, cols, select, null, null, null, null);
 		
 		ArrayList<Long> artistIds = new ArrayList<Long>();
 		while (c.moveToNext()) {
 			artistIds.add(c.getLong(0));
 		}
 		c.close();
 		
 		for (long l : artistIds) {
 			artists.add(mr.getArtist(l));
 		}
 		tag.tagArtists(artists);
 	}
 	
 	/**
 	 * Add {@link User}s that have rated an {@link Artist}, and the rating, to the Artist.
 	 * @param artist The Artist to retrieve Users for.
 	 * @return A HashMap of Users and the User's rating of the Artist.
 	 */
 	private void userArtists(Artist artist) {
 		openDB();
 		HashMap<User, Short> urat = new HashMap<User, Short>();
 		String[] cols = {DB.USERARTIST_USER, DB.USERARTIST_RATING};
 		String select = DB.USERARTIST_ARTIST + "=" + artist.getId();
 		Cursor c = db.query(DB.TB_USERARTIST, cols, select, null, null, null, null);
 		
 		HashMap<Long, Short> uIdRat = new HashMap<Long, Short>();
 		while(c.moveToNext()) {
 			uIdRat.put(c.getLong(0), c.getShort(1));
 		}
 		c.close();
 		
 		for (Long l : uIdRat.keySet()) {
 			User u = mr.getUser(l);
 			urat.put(u, uIdRat.get(l));
 		}
 		
 		artist.addUserRatings(urat);
 	}
 	
 	/**
 	 * Retreive all {@link Song}s currently stored in the database.
 	 * @return All Songs stored in the database.
 	 */
 	ArrayList<Song> getSongs() {
 		openDB();
 		ArrayList<Song> songs = new ArrayList<Song>();
 		
 		String[] columns = {DB.SONG_ARTIST, DB.SONG_TITLE, DB.SONG_HOST, DB.SONG_LOCATION, "rowid"};
 		Cursor cu = db.query(DB.TB_SONG, columns, null, null, null, null, null);
 		
 		ArrayList<Long> songIds = new ArrayList<Long>();
 		
 		HashMap<Long, Long> artistIds 	= new HashMap<Long, Long>();
 		HashMap<Long, Long> userIds 	= new HashMap<Long, Long>();
 		HashMap<Long, String> titles 	= new HashMap<Long, String>();
 		HashMap<Long, String> locations = new HashMap<Long, String>();
 		
 		while (cu.moveToNext()) {
 			Long l = cu.getLong(4);
 			Song s = mr.songLoaded(l);
 			if (s == null) {
 				songIds.add(l);
 				artistIds.put(l, cu.getLong(0));
 				userIds.put(l, cu.getLong(2));
 				titles.put(l, cu.getString(1));
 				locations.put(l, cu.getString(3));
 			} else songs.add(s);
 		}
 		
 		cu.close();
 		
 		for (Long sid : songIds) {
 			Song s = new Song(titles.get(sid), mr.getArtist(artistIds.get(sid)), 
 					mr.getUser(userIds.get(sid)), locations.get(sid));
 			songTags(s);
 			s.setId(sid);
 		}
 		
 		return songs;
 	}
 	
 	/**
 	 * Retreive {@link Artist}s from the database.
 	 * @return Artists from the database.
 	 */
 	ArrayList<Artist> getArtists() {
 		openDB();
 		ArrayList<Artist> artists = new ArrayList<Artist>();
 		String[] cols = {DB.ARTIST_NAME, "rowid"};
 		Cursor cu = db.query(DB.TB_ARTIST, cols, null, null, null, null, null);
 		
 		HashMap<Long, String> hm = new HashMap<Long, String>();
 		while(cu.moveToNext()) {
 			Artist artist = mr.artistLoaded(cu.getLong(1));
 			if (artist == null) {
 				hm.put(cu.getLong(1), cu.getString(0));
 			} else {
 				artists.add(artist);
 			}
 		}
 		cu.close();
 		
 		for (Long l : hm.keySet()) {
 			Artist a = new Artist(hm.get(l));
 			a.setId(l);
 			artistTags(a);
 			userArtists(a);
 			artists.add(a);
 		}
 		
 		return artists;
 	}
 	
 	/**
 	 * Retreive {@link User}s from the database.
 	 * @return Users from the database.
 	 */
 	ArrayList<User> getUsers() {
 		openDB();
 		ArrayList<User> users = new ArrayList<User>();
 		String[] cols = {DB.USER_ADDRESS, DB.USER_LASTFM, "rowid"};
 		Cursor cu = db.query(DB.TB_USER, cols, null, null, null, null, null);
 		
 		ArrayList<Long> userIds = new ArrayList<Long>();
 		ArrayList<String> userLocs = new ArrayList<String>();
 		ArrayList<String> uLastfmName = new ArrayList<String>();
 		while (cu.moveToNext()) {
 			Long uId = cu.getLong(2);
 			User user = mr.userLoaded(uId);
 
 			if (user != null) users.add(user);
 			else {
 				userIds.add(cu.getLong(2));
 				uLastfmName.add(cu.getString(1));
 				userLocs.add(cu.getString(0));
 			}
 		}
 		cu.close();
 		
 		for (int i = 0, s = userIds.size(); i < s; ++i) {
 			long sid = userIds.get(i);
 			User u = new User(userLocs.get(i), uLastfmName.get(i));
 			userArtists(u);
 			u.setId(sid);
 			userArtists(u);
 			users.add(u);
 		}
 		
 		return users;
 	}
 	
 	/**
 	 * Retrieve {@link Tag}s from the database.
 	 * @return Tags stored in the database.
 	 */
 	ArrayList<Tag> getTags() {
 		openDB();
 		ArrayList<Tag> tags = new ArrayList<Tag>();
 		String[] cols = {"rowid", DB.TAG_NAME};
 		Cursor cu = db.query(DB.TB_TAG, cols, null, null, null, null, null);
 		
 		HashMap<Long, String> ids = new HashMap<Long, String>();
 		while (cu.moveToNext()) {
 			Tag t = mr.tagLoaded(cu.getLong(0));
 			if (t == null) ids.put(cu.getLong(0), cu.getString(1));
 			else tags.add(t);
 			
 		}
 		cu.close();
 		
 		for (long l : ids.keySet()) {
 			Tag t = new Tag(ids.get(l));
 			t.setId(l);
 			taggedSongs(t);
 			taggedArtists(t);
 			tags.add(t);
 		}
 				
 		return tags;
 	}
 	
 	/**
 	 * Remove the coupling betweeen {@link User}s and {@link Artist}s in the database.
 	 * This method clears ALL Song relations in the database, regardless of whether they are associated to the Song object or not.
 	 * @param user The User to clear relations on.
 	 */
 	private void removeRelations(User user) {
 		openDB();
 		db.delete(DB.TB_USERARTIST, DB.USERARTIST_USER + "= '" + user.getId() + "'", null);
 	}
 	
 	/**
 	 * Remove the coupling between {@link Artist}s and {@link Tag}s or {@link User}s in the database.
 	 * This method clears ALL Artist relations in the database, regardless of whether they are associated to the Artist object or not.
 	 * @param artist The Artist to clear relations on.
 	 */
 	private void removeRelations(Artist artist) {
 		openDB();
 		db.delete(DB.TB_ARTISTTAGS, DB.ARTISTTAG_ARTIST + " = '" + artist.getId() + "'", null);
 		db.delete(DB.TB_USERARTIST, DB.USERARTIST_ARTIST + " = '" + artist.getId() + "'", null);
 		db.delete(DB.TB_SIMILARARTIST, DB.SIMILARARTIST_FIRST_ARTIST + "= '" + artist.getId() + "'" , null);
 		db.delete(DB.TB_SIMILARARTIST, DB.SIMILARARTIST_SECOND_ARTIST + "= '" + artist.getId() + "'" , null);
 	}
 	
 	/**
 	 * Remove the coupling between {@link Tag}s and {@link Artist}s or {@link Song}s.
 	 * This method clears ALL Tag relations in the database, regardless of whether they are associated to the Tag object or not.
 	 * @param tag The Tag to clear relations on.
 	 */
 	private void removeRelations(Tag tag) {
 		openDB();
 		db.delete(DB.TB_ARTISTTAGS, DB.ARTISTTAG_TAG + " = '" + tag.getId() + "'", null);
 		db.delete(DB.TB_SONGTAGS, DB.SONGTAG_TAG + " = '" + tag.getId() + "'", null);
 	}
 	
 	/**
 	 * Remove the coupling between {@link Song}s and {@link Tag}s in the database.
 	 * This method clears ALL Song relations in the database, regardless of whether they are associated to the Song object or not.
 	 * @param song The Song to clear relations on.
 	 */
 	private void removeRelations(Song song) {
 		openDB();
 		db.delete(DB.TB_SONGTAGS, DB.SONGTAG_SONG + " = '" + song.getId() + "'", null);
 	}
 	
 	/**
 	 * Search the database for an {@link Artist} with the provided name.
 	 * Currently only does a "stupid search", ie. strings must be exactly equal.
 	 * Returns null if no Artist with the provided attributes exist.
 	 * @param name The name to search for.
 	 * @return The first Artist matching the provided name.
 	 */
 	Artist searchArtist(String name) {
 		openDB();
 		String[] columns = {"rowid"};
 		String selection = DB.ARTIST_NAME + " = ?";
 		String[] selectionArgs = {name};
 		
 		Cursor c = db.query(DB.TB_ARTIST, columns, selection, selectionArgs, null, null, null);
 		
 		long aId = -1;
 		if (c.moveToFirst()) {
 			aId = c.getLong(0);
 		}
 		c.close();
 		return mr.getArtist(aId);		
 	}
 	
 	/**
 	 * Search the database for a {@link Song} with the provided attributes.
 	 * Currently only does a "stupid search", ie. strings must be exactly equal.
 	 * Returns null if no Song with the provided attributes exist.
 	 * @param title The title to search for.
 	 * @param artist The name of the artist to search for.
 	 * @param host The host, the Song is at (can be null).
 	 * @param location The location of the Song.
 	 * @return The first Song matching the provided attributes.
 	 */
 	Song searchSong(String title, String artist, String host, String location) {
 		if (host == null) return searchSong(title, searchArtist(artist).getId(), -1, location);
 		return searchSong(title, searchArtist(artist).getId(), searchUser(host, null).getId(), location);
 	}
 	
 	/**
 	 * Search the database for a {@link Song} with the provided attributes.
 	 * Currently only does a "stupid search", ie. strings must be exactly equal.
 	 * Returns null if no Song with the provided attributes exist.
 	 * @param title The title to search for.
 	 * @param artist The {@link Artist} to search.
 	 * @param host The host, the Song is at (can be null).
 	 * @param location The location of the Song.
 	 * @return The Song matching the provided attributes.
 	 */
 	Song searchSong(String title, Artist artist, String host, String location) {
 		User searchUserRes = searchUser(host, null);
 		
 		if (searchUserRes == null) return searchSong(title, artist.getId(), -1, location);
 		return searchSong(title, artist.getId(), searchUserRes.getId(), location);
 	}
 	
 	/**
 	 * Search the database for a {@link Song} with the provided attributes.
 	 * Currently only does a "stupid search", ie. strings must be exactly equal.
 	 * @param title The title to search for.
 	 * @param artist The Artist name to search for.
 	 * @param host The {@link User} that hosts the Song (can be null).
 	 * @param location The path of the Song.
 	 * @return The Song matching the provided attributes.
 	 */
 	Song searchSong(String title, String artist, User host, String location) {
 		if (host == null) return searchSong(title, searchArtist(artist).getId(), -1, location);
 		return searchSong(title, searchArtist(artist).getId(), host.getId(), location);
 	}
 	
 	/**
 	 * Search the database for a {@link Song} with the provided attributes.
 	 * Currently only does a "stupid search", ie. strings must be exactly equal.
 	 * Returns null if no Song with the provided attributes exist.
 	 * @param title The title to search for.
 	 * @param artist The {@link Artist} to search for.
 	 * @param host The {@link User} that hosts the Song (can be null).
 	 * @param location The path of the song.
 	 * @return The Song matching the provided attributes.
 	 */
 	Song searchSong(String title, Artist artist, User host, String location) {
 		if (host == null) return searchSong(title, artist.getId(), -1, location);
 		return searchSong(title, artist.getId(), host.getId(), location);
 	}
 	
 	Song searchSong(String location) {
 		return searchSong(null, -1, -1, location);
 	}
 	
 	/**
 	 * Search the database for a {@link Song} with the provided attributes.
 	 * Currently only does a "stupid search", ie. strings must be exactly equal.
 	 * Returns null if no Song with the provided attributes exist.
 	 * @param title The title to search for.
 	 * @param artist The id of the {@link Artist} to search for.
 	 * @param host The id of the {@link Host} to search for.
 	 * @param location The path of the song.
 	 * @return The Song matching the provided attributes.
 	 */
 	private Song searchSong(String title, long artist, long host, String location) {
 		openDB();
 		long sId = -1;
 		
 		String selection = "";
 		ArrayList<String> selArgs = new ArrayList<String>();
 		if (title != null) 
 		{
 			selection += DB.SONG_TITLE + " = ? AND ";
 			selArgs.add(title);
 		}
 		
 		if (artist != -1) {
 			selection += DB.SONG_ARTIST + " = ? AND ";
 			selArgs.add("" + artist);
 		}
 		
 		if (location != null) {
 			selection += DB.SONG_LOCATION + " = ? AND ";
 			selArgs.add(location);
 		}
 		
 		if (host != -1) {
 			selection += DB.SONG_HOST + " = ? AND ";
 			selArgs.add(host + "");
 		}
 		
 		selection = selection.substring(0, selection.length() - 5);
 		int selSize = selArgs.size();
 		String[] selectionArgs = new String[selSize];
 		for (int i = 0; i < selSize; ++i) {
 			selectionArgs[i] = selArgs.get(i);
 		}
 				
 		Cursor c = db.query(DB.TB_SONG, new String[] {"rowid"}, selection, selectionArgs, null, null, null);
 		if (c.moveToFirst()) sId = c.getLong(0);
 		c.close();
 		
 		return mr.getSong(sId);
 	}
 	
 	/**
 	 * Search the database for a {@link User} with the provided bluetooth MAC address.
 	 * Currently only does a "stupid search", ie. strings must be exactly equal.
 	 * Returns null if no user with the provided address exist has been added to the database.
 	 * @param location The bluetooth address of the user to search for.
 	 * @return The User matching the Bluetooth address.
 	 */
 	User searchUser(String location, String lastFM) {
 		openDB();
 		String[] columns = {"rowid"};
 		String selection = "";
 		long uId = -1;
 		if (location != null) selection += DB.USER_ADDRESS + " = '" + location + "'";
 		if (location != null && lastFM != null) selection += " AND ";
 		if (lastFM != null) selection += DB.USER_LASTFM + " = '" + lastFM + "'";
 		if (selection == "") selection = null;
 		
 		Cursor c = db.query(DB.TB_USER, columns, selection, null, null, null, null);
 		
 		if (c.moveToFirst()) uId = c.getLong(0);
 		return mr.getUser(uId);
 	}
 	
 	/**
 	 * Search the database for a {@link Tag} with the provided name.
 	 * Currently only does a "stupid search", ie. strings must be exactly equal.
 	 * Returns null if no user with the provided address exist has been added to the database.
 	 * @param tagName The Tag name to search for.
 	 * @return The Tag matching the provided Tag name.
 	 */
 	Tag searchTag(String tagName) {
 		openDB();
 		String[] columns = {"rowid"};
 		String[] selectionArgs = {tagName};
 		long tId = -1;
 		
 		Cursor c = db.query(DB.TB_TAG, columns, DB.TAG_NAME + " = ?", selectionArgs, null, null, null);
 		
 		if (c.moveToFirst()) tId = c.getLong(0);
 		return mr.getTag(tId);
 	}
 	
 	/**
 	 * Check whether the provided {@link Artist} name has any {@link Song}s in the library.
 	 * @param artist The Artist name to check.
 	 * @return Whether the Artist has any Songs in the library.
 	 */
 	boolean existSongsWithArtist(String artist) {
 		return existSongsWithArtist(searchArtist(artist).getId());
 	}
 	
 	/**
 	 * Check whether the provided {@link Artist} has any {@link Song}s in the library.
 	 * @param artist The Artist to check.
 	 * @return Whether the Artist has any Songs in the library.
 	 */
 	boolean existSongsWithArtist(Artist artist) {
 		return existSongsWithArtist(artist.getId());
 	}
 	
 	/**
 	 * Check whether the {@link Artist} with the provided id has any {@link Song}s in the library.
 	 * @param artist The Artist id to check.
 	 * @return Whether the Artist has any Songs in the library.
 	 */
 	boolean existSongsWithArtist(long artist) {
 		openDB();
 		String[] columns = {"rowid"};
 		String selection = DB.SONG_ARTIST + " = ?";
 		String[] selectionArgs = {artist + ""};
 		
 		Cursor c = db.query(DB.TB_SONG, columns, selection, selectionArgs, null, null, null);
 		
 		boolean ret = c.getCount() > 0;
 		c.close();
 		return ret;
 	}
 	
 	/**
 	 * Load {@link Artist}s similar to the provided Artist, and update the provided Artist's list of similar Artists.
 	 * Note that the relation is NOT updated the other way - this is due to the lazy load property of similar artists. 
 	 * @param artist The Artist to retrieve similar Artists for.
 	 */
 	void loadSimilarArtists(Artist artist) {
 		openDB();
 		String query 	= "SELECT " + DB.SIMILARARTIST_FIRST_ARTIST + ", " + DB.SIMILARARTIST_RATING 
 						+ " FROM " + DB.TB_SIMILARARTIST 
 						+ " WHERE " + DB.SIMILARARTIST_SECOND_ARTIST + " = '" + artist.getId() + "'"
 						+ " UNION SELECT " + DB.SIMILARARTIST_SECOND_ARTIST + ", " + DB.SIMILARARTIST_RATING 
 						+ " FROM " + DB.TB_SIMILARARTIST 
 						+ " WHERE " + DB.SIMILARARTIST_FIRST_ARTIST + " = '" + artist.getId() + "'";
 		
 		Cursor cu = db.rawQuery(query, null);
 		
 		HashMap<Long, Short> idToRating = new HashMap<Long,Short>();
 		while (cu.moveToNext()) {
 			idToRating.put(cu.getLong(0), cu.getShort(0));
 		}
 		cu.close();
 		for (Long id : idToRating.keySet()) {
 			artist.similarArtists.put(mr.getArtist(id), idToRating.get(id));
 		}
 	}
 
 	/**
 	 * Add a similar {@link Artist} relation to the database.
 	 * @param artist1 First Artist of the relation.
 	 * @param artist2 Second Artist of the problem.
 	 * @param similarity The similarity of the artists.
 	 */
 	void addSimilarArtist(Artist artist1, Artist artist2, Short similarity) {
 		db.insert(DB.TB_SIMILARARTIST, null, getCV(artist1, artist2, similarity));
 	}
 
 	/**
 	 * Remove the similarity between the provided {@link Artist}s.
 	 * @param artist First Artist of the pair to remove similarity from.
 	 * @param artist2 Second Artist of the pair to remove similarity from.
 	 */
 	void removeSimilarArtist(Artist artist, Artist artist2) {
 		String query 	= "DELETE FROM " + DB.TB_SIMILARARTIST + " WHERE " 
 						+ DB.SIMILARARTIST_FIRST_ARTIST + " = " + artist.getId() 
 						+ " AND " + DB.SIMILARARTIST_SECOND_ARTIST + " = " + artist2.getId() 
 						+ " OR " + DB.SIMILARARTIST_FIRST_ARTIST + " = " + artist2.getId()
 						+ " AND " + DB.SIMILARARTIST_SECOND_ARTIST + " = " + artist.getId();
 		db.execSQL(query);
 		
 	}
 	
 	void removeUser(User user) {
 		db.delete(DB.TB_USER, "rowid = ?", new String[] {user.getId() + ""});
 		db.delete(DB.TB_USERARTIST, DB.USERARTIST_USER + " = ?", new String[] {user.getId() + ""});
 	}
 }
