 package se.goagubbar.twee;
 
 import java.util.ArrayList;
 
 import se.goagubbar.twee.Models.Episode;
 import se.goagubbar.twee.Models.Series;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DatabaseHandler extends SQLiteOpenHelper {
 
 	private static final int DATABASE_VERSION = 12;
 	private static final String DATABASE_NAME = "Teewee";
 
 	private static final String TABLE_SERIES = "Series";
 	private static final String TABLE_EPISODES = "Episodes";
 	
 	private static final String ENCODING_SETTING = "PRAGMA encoding = 'UTF-8'";
 
 	//BaseEntity
 	private static final String KEY_ID = "Id";
 	private static final String KEY_SUMMARY = "Summary";
 	private static final String KEY_LASTUPDATED = "LastUpdated";
 	private static final String KEY_SERIESID = "SeriesId";
 	private static final String KEY_EPISODEID = "EpisodeId";
 
 	//Series
 	private static final String KEY_NAME = "Name";
 	private static final String KEY_ACTORS = "Actors";
 	private static final String KEY_DAYTIME = "Airs";
 	private static final String KEY_GENRE = "Genre";
 	private static final String KEY_IMDBID = "ImdbId";
 	private static final String KEY_RATING = "Rating";
 	private static final String KEY_STATUS = "Status";
 	private static final String KEY_IMAGE = "Image";
 	private static final String KEY_HEADER = "Header";
 	private static final String KEY_FIRSTAIRED = "FirstAired";
 
 
 	//Episode
 	private static final String KEY_SEASON = "Season";
 	private static final String KEY_EPISODE = "Episode";
 	private static final String KEY_TITLE = "Title";
 	private static final String KEY_AIRED = "Aired";
 	private static final String	KEY_WATCHED = "Watched";
 
 
 	public DatabaseHandler(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(ENCODING_SETTING);
 		
 		String CREATE_SERIES_TABLE = String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY autoincrement, %s TEXT,%s TEXT,%s TEXT,%s TEXT,%s TEXT,%s TEXT,%s TEXT,%s TEXT,%s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT)", TABLE_SERIES, KEY_ID, KEY_SUMMARY, KEY_NAME,KEY_ACTORS,KEY_DAYTIME,KEY_GENRE,KEY_IMDBID,KEY_RATING,KEY_STATUS,KEY_IMAGE, KEY_FIRSTAIRED, KEY_HEADER, KEY_SERIESID, KEY_LASTUPDATED);
 		db.execSQL(CREATE_SERIES_TABLE);
 
 		String CREATE_EPISODE_TABLE = String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY autoincrement,%s TEXT,%s TEXT,%s TEXT,%s TEXT,%s TEXT,%s TEXT, %s TEXT, %s TEXT, %s TEXT)", TABLE_EPISODES, KEY_ID, KEY_SEASON, KEY_EPISODE, KEY_TITLE, KEY_AIRED, KEY_WATCHED, KEY_SUMMARY, KEY_SERIESID, KEY_LASTUPDATED, KEY_EPISODEID);
 		db.execSQL(CREATE_EPISODE_TABLE);
 	}
 
 	@Override
 	public void onOpen(SQLiteDatabase db) {
 	   if (!db.isReadOnly()) {
 	      db.execSQL(ENCODING_SETTING);
 	   }
 	} 
 	
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.d("Rebuild db", "Start");
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_EPISODES);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERIES);
 		onCreate(db);
 		Log.d("Rebuild db", "Done");
 	}
 
 
 	//CRUD
 
 	public void addSeries(Series series)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = new ContentValues();
 
 		values.put(KEY_NAME, series.getName());
 		values.put(KEY_ACTORS, series.getActors());
 		values.put(KEY_SUMMARY, series.getSummary());
 		values.put(KEY_DAYTIME, series.getAirs());
 		values.put(KEY_GENRE, series.getGenre());
 		values.put(KEY_IMDBID, series.getImdbId());
 		values.put(KEY_RATING, series.getRating());
 		values.put(KEY_STATUS, series.getStatus());
 		values.put(KEY_IMAGE, series.getImage());
 		values.put(KEY_HEADER, series.getHeader());
 		values.put(KEY_FIRSTAIRED, series.getFirstAired());
 		values.put(KEY_SERIESID, series.getSeriesId());
 		values.put(KEY_LASTUPDATED, series.getLastUpdated());
 
 		db.insert(TABLE_SERIES, null, values);
 		db.close();
 
 	}
 
 	public void addEpisodes(ArrayList<Episode> episodes)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = null;
 
 		db.beginTransaction();
 		for (Episode ep : episodes) {
 			values = new ContentValues();
 			values.put(KEY_AIRED, ep.getAired());
 			values.put(KEY_EPISODE, ep.getEpisode());
 			values.put(KEY_SEASON, ep.getSeason());
 			values.put(KEY_SERIESID, ep.getSeriesId());
 			values.put(KEY_SUMMARY, ep.getSummary());
 			values.put(KEY_TITLE, ep.getTitle());
 			values.put(KEY_WATCHED, ep.getWatched());
 			values.put(KEY_LASTUPDATED, ep.getLastUpdated());
 			values.put(KEY_EPISODEID, ep.getEpisodeId());
 			db.insert(TABLE_EPISODES, null, values);
 		}
 		db.setTransactionSuccessful();
 		db.endTransaction();
 		db.close();
 
 	}
 
 	public void updateAndAddEpisodes(ArrayList<Episode> episodes, String seriesId)
 	{
 		ArrayList<String> oldEpisodes = GetEpisodesForSearies(seriesId);
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = null;
 
 		db.beginTransaction();
 	
 		for (Episode ep : episodes) {
 			if(oldEpisodes.contains(ep.getEpisodeId())){
 				values = new ContentValues();
 				values.put(KEY_AIRED, ep.getAired());
 				values.put(KEY_EPISODE, ep.getEpisode());
 				values.put(KEY_SEASON, ep.getSeason());
 				values.put(KEY_SERIESID, ep.getSeriesId());
 				values.put(KEY_SUMMARY, ep.getSummary());
 				values.put(KEY_TITLE, ep.getTitle());
 				values.put(KEY_LASTUPDATED, ep.getLastUpdated());
 
 				db.update(TABLE_EPISODES, values, KEY_EPISODEID +"="+ ep.getEpisodeId(),null);
 			}
 			else
 			{
 				values = new ContentValues();
 				values.put(KEY_AIRED, ep.getAired());
 				values.put(KEY_EPISODE, ep.getEpisode());
 				values.put(KEY_SEASON, ep.getSeason());
 				values.put(KEY_SERIESID, ep.getSeriesId());
 				values.put(KEY_SUMMARY, ep.getSummary());
 				values.put(KEY_TITLE, ep.getTitle());
 				values.put(KEY_WATCHED, ep.getWatched());
 				values.put(KEY_LASTUPDATED, ep.getLastUpdated());
 				values.put(KEY_EPISODEID, ep.getEpisodeId());
 				db.insert(TABLE_EPISODES, null, values);
 			}
 		}
 		db.setTransactionSuccessful();
 		db.endTransaction();
 		db.close();
 
 	}
 	
 	
 	
 	//DU HLL P MED KOLLEN AV EPISODEERNA!!!!!	
 	private ArrayList<String> GetEpisodesForSearies(String seriesId)
 	{
 		
 		ArrayList<String> episodes = new ArrayList<String>();
 		String sql = "SELECT "+ KEY_EPISODEID +" FROM " + TABLE_EPISODES + " WHERE " + KEY_SERIESID + " = " + seriesId;
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(sql,null);
 
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			episodes.add(cursor.getString(0));
 			cursor.moveToNext();
 		}
 
 		cursor.close();
 		db.close();
 		return episodes;
 		
 	}
 
 	public ArrayList<Series> getAllSeries()
 	{
 		ArrayList<Series> series = new ArrayList<Series>();
 		String sql = "SELECT * FROM " + TABLE_SERIES + " ORDER BY "+ KEY_NAME +" ASC";
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(sql,null);
 
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			Series s = new Series();
 
 			s.setActors(cursor.getString(3));
 			s.setAirs(cursor.getString(4));
 			s.setImage(cursor.getString(9));
 			s.setGenre(cursor.getString(5));
 			s.setID(Integer.parseInt(cursor.getString(0)));
 			s.setImdbId(cursor.getString(6));
 			s.setName(cursor.getString(2));			
 			s.setRating(cursor.getString(7));
 			s.setStatus(cursor.getString(8));
 			s.setSummary(cursor.getString(1));
 			s.setFirstAired(cursor.getString(10));
 			s.setHeader(cursor.getString(11));
 			s.setSeriesId(cursor.getString(12));
 
 			s.Episodes = new ArrayList<Episode>();
 
 			s.Episodes.add(GetNextEpisodeForSeries(s.getSeriesId()));
 
 			series.add(s);
 			cursor.moveToNext();
 		}
 
 
 
 		cursor.close();
 		db.close();
 		return series;
 	}
 
 	public Episode GetLastAiredEpisodeForSeries(String seriesId)
 	{
 		Episode e = new Episode();
 		try {
 
 			String dateWithoutTime  = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();
 
 			String sql = "SELECT * FROM "+ TABLE_EPISODES +" WHERE date("+ KEY_AIRED +") < date('"+ dateWithoutTime +"') AND "+KEY_SERIESID+" = "+ seriesId +" AND "+KEY_SEASON+" != 0 ORDER BY "+ KEY_AIRED +" DESC LIMIT 1";	
 			SQLiteDatabase db = this.getReadableDatabase();
 
 			Cursor cursor = db.rawQuery(sql, null);
 
 			if(cursor != null)
 			{
 				if(cursor.moveToFirst())
 				{
 					e.setAired(cursor.getString(4));
 					e.setEpisode(cursor.getString(2));
 					e.setID(Integer.parseInt(cursor.getString(0)));
 					e.setSeason(cursor.getString(1));
 					e.setSeriesId(cursor.getString(7));
 					e.setSummary(cursor.getString(6));
 					e.setTitle(cursor.getString(3));
 					e.setWatched(cursor.getString(5));
 				}
 			}
 
 			cursor.close();
 			db.close();
 
 		} catch (Exception e2) {
 			// TODO: handle exception
 			Log.d("Error",e2.getLocalizedMessage());
 		}
 
 		return e;
 
 	}
 
 
 	public Episode GetNextEpisodeForSeries(String seriesId)
 	{
 		Episode e = new Episode();
 		try {
 
 			String dateWithoutTime  = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();
 
 			String sql = "SELECT * FROM "+ TABLE_EPISODES +" WHERE date("+ KEY_AIRED +") >= date('"+ dateWithoutTime +"') AND "+KEY_SERIESID+" = "+ seriesId +" AND "+KEY_SEASON+" != 0 ORDER BY "+ KEY_AIRED +" ASC LIMIT 1";	
 			SQLiteDatabase db = this.getReadableDatabase();
 
 			Cursor cursor = db.rawQuery(sql, null);
 
 			if(cursor != null)
 			{
 				if(cursor.moveToFirst())
 				{
 					e.setAired(cursor.getString(4));
 					e.setEpisode(cursor.getString(2));
 					e.setSeason(cursor.getString(1));
 					e.setTitle(cursor.getString(3));
 				}
 			}
 
 			cursor.close();
 			db.close();
 
 		} catch (Exception e2) {
 			// TODO: handle exception
 			Log.d("Error",e2.getLocalizedMessage());
 		}
 
 		return e;
 
 	}
 
 
 	public Episode GetEpisodeById(String episodeId)
 	{
 		Episode e = new Episode();
 		try {
 
 
 			String sql = "SELECT * FROM "+ TABLE_EPISODES +" WHERE "+KEY_ID+" = "+ episodeId;	
 			SQLiteDatabase db = this.getReadableDatabase();
 
 			Cursor cursor = db.rawQuery(sql, null);
 
 			if(cursor != null)
 			{
 				if(cursor.moveToFirst())
 				{
 					e.setAired(cursor.getString(4));
 					e.setEpisode(cursor.getString(2));
 					e.setID(Integer.parseInt(cursor.getString(0)));
 					e.setSeason(cursor.getString(1));
 					e.setSeriesId(cursor.getString(7));
 					e.setSummary(cursor.getString(6));
 					e.setTitle(cursor.getString(3));
 					e.setWatched(cursor.getString(5));
 				}
 			}
 
 			cursor.close();
 			db.close();
 
 		} catch (Exception e2) {
 			// TODO: handle exception
 			Log.d("Error",e2.getLocalizedMessage());
 		}
 
 		return e;
 	}
 
 	public Series getSeriesById(String id)
 	{
 		Series s = new Series();
 		String sql = "SELECT * FROM " + TABLE_SERIES + " WHERE " + KEY_ID + " = " + id;
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(sql, null);
 
 		if(cursor != null)
 		{
 			if(cursor.moveToFirst())
 			{
 				s.setActors(cursor.getString(3));
 				s.setAirs(cursor.getString(4));
 				s.setImage(cursor.getString(9));
 				s.setHeader(cursor.getString(11));
 				s.setGenre(cursor.getString(5));
 				s.setID(Integer.parseInt(cursor.getString(0)));
 				s.setImdbId(cursor.getString(6));
 				s.setName(cursor.getString(2));			
 				s.setRating(cursor.getString(7));
 				s.setStatus(cursor.getString(8));
 				s.setSummary(cursor.getString(1));
 				s.setFirstAired(cursor.getString(10));
 				s.setSeriesId(cursor.getString(12));
 			}
 			cursor.close();
 		}
 		s.Episodes = GetEpisodes(s.getSeriesId());
 
 		return s;
 	}
 
 	public Boolean SeriesExist(String seriesId)
 	{
 		String sql = "SELECT * FROM " + TABLE_SERIES + " WHERE " + KEY_SERIESID + " = " + seriesId;
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(sql, null);
 
 		if(cursor != null)
 		{
 			if(cursor.moveToFirst())
 			{
 				cursor.close();
 				return true;
 			}
 			cursor.close();
 		}
 
 		return false;
 	}
 
 	public void ToggleEpisodeWatched(String id, boolean watched)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 
 		String value;
 
 		if(watched)
 		{
 			value = "1";
 		}
 		else
 		{
 			value = "0";
 		}
 
 		ContentValues values = new ContentValues();
 		values.put(KEY_WATCHED, value);
 
 		db.update(TABLE_EPISODES, values, KEY_ID + " = ?", new String[] { id });
 		Log.d("Event","Updatering klar");
 		db.close();
 
 	}
 
 	public void ToggleSeasonWatched(String id, String season, boolean watched)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 
 		String value;
 		String dateWithoutTime  = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();
 
 		if(watched)
 		{
 			value = "1";
 		}
 		else
 		{
 			value = "0";
 		}
 
 		ContentValues values = new ContentValues();
 		values.put(KEY_WATCHED, value);
 
 		db.update(TABLE_EPISODES, values, KEY_AIRED + " < date('"+ dateWithoutTime +"') AND "+KEY_SERIESID + " = ? AND " + KEY_SEASON + " = ?", new String[] { id, season });
 		Log.d("Event","Updatering klar");
 		db.close();
 
 	}
 
 	public void MarkSeriesAsWatched(String id)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 
 		String dateWithoutTime  = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();
 
 		ContentValues values = new ContentValues();
 		values.put(KEY_WATCHED, "1");
 		db.update(TABLE_EPISODES, values, KEY_AIRED + " < date('"+ dateWithoutTime +"') AND "+ KEY_SERIESID +" = ?", new String[] {id});
 		db.close();
 
 	}
 
 	private ArrayList<Episode> GetEpisodes(String seriesId)
 	{
 		ArrayList<Episode> episodes = new ArrayList<Episode>();
 		String sql = "SELECT * FROM " + TABLE_EPISODES + " WHERE " + KEY_SEASON + " != 0 AND " + KEY_AIRED + " != '' AND " +  KEY_SERIESID + " = " + seriesId + " ORDER BY "+KEY_AIRED+" DESC";
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(sql, null);
 
 		cursor.moveToFirst();
 
 		while(!cursor.isAfterLast())
 		{
 			Episode e = new Episode();
 
 			e.setAired(cursor.getString(4));
 			e.setEpisode(cursor.getString(2));
 			e.setID(Integer.parseInt(cursor.getString(0)));
 			e.setSeason(cursor.getString(1));
 			e.setSeriesId(cursor.getString(7));
 			e.setSummary(cursor.getString(6));
 			e.setTitle(cursor.getString(3));
 			e.setWatched(cursor.getString(5));
 
 			episodes.add(e);
 
 			cursor.moveToNext();
 		}
 		cursor.close();
 		db.close();
 
 
 
 		return episodes;
 	}
 
 	public ArrayList<Episode> GetAiredEpisodes(String seriesId)
 	{
 		String dateWithoutTime  = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();
 		ArrayList<Episode> episodes = new ArrayList<Episode>();
 		String sql = "SELECT * FROM " + TABLE_EPISODES + " WHERE " + KEY_SEASON + " != 0 AND " + KEY_AIRED + " != '' AND " + KEY_AIRED + " <= date('" + dateWithoutTime + "') AND " +  KEY_SERIESID + " = " + seriesId + " ORDER BY "+KEY_AIRED+" DESC";
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(sql, null);
 
 		cursor.moveToFirst();
 
 		while(!cursor.isAfterLast())
 		{
 			Episode e = new Episode();
 
 			e.setAired(cursor.getString(4));
 			e.setEpisode(cursor.getString(2));
 			e.setID(Integer.parseInt(cursor.getString(0)));
 			e.setSeason(cursor.getString(1));
 			e.setSeriesId(cursor.getString(7));
 			e.setSummary(cursor.getString(6));
 			e.setTitle(cursor.getString(3));
 			e.setWatched(cursor.getString(5));
 
 			episodes.add(e);
 
 			cursor.moveToNext();
 		}
 		cursor.close();
 		db.close();
 
 
 
 		return episodes;
 	}
 
 
 
 	public ArrayList<Episode> GetAllEpisodesForGivenTimePeriod(int timeperiod)
 	{
 		ArrayList<Episode> episodes = new ArrayList<Episode>();
 		String dateWithoutTime  = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();
 		String sql = "";
 		//"SELECT * FROM table_a a INNER JOIN table_b b ON a.id=b.other_id WHERE b.property_id=?";
 		switch (timeperiod) {
 		case 0: //Today
 			sql = "SELECT * FROM "+ TABLE_EPISODES + " e INNER JOIN "+TABLE_SERIES+" s ON e.seriesId = s.SeriesId WHERE "+KEY_AIRED+" >= date('"+dateWithoutTime+"') AND STRFTIME('%j', "+ KEY_AIRED +") = STRFTIME('%j', '"+ dateWithoutTime +"') AND STRFTIME('%Y', "+ KEY_AIRED +") = STRFTIME('%Y', '"+ dateWithoutTime +"') ORDER BY " + KEY_AIRED;		
 			break;
 		case 1: //This week
 			sql = "SELECT * FROM "+ TABLE_EPISODES +" e INNER JOIN "+TABLE_SERIES+" s ON e.seriesId = s.SeriesId WHERE "+KEY_AIRED+" >= date('"+dateWithoutTime+"') AND STRFTIME('%W', "+ KEY_AIRED +") = STRFTIME('%W', '"+ dateWithoutTime +"') AND STRFTIME('%Y', "+ KEY_AIRED +") = STRFTIME('%Y', '"+ dateWithoutTime +"') ORDER BY " + KEY_AIRED;
 			break;
 		case 2: //This month
 			sql = "SELECT * FROM "+ TABLE_EPISODES +" e INNER JOIN "+TABLE_SERIES+" s ON e.seriesId = s.SeriesId WHERE "+KEY_AIRED+" >= date('"+dateWithoutTime+"') AND STRFTIME('%m', "+ KEY_AIRED +") = STRFTIME('%m', '"+ dateWithoutTime +"') AND STRFTIME('%Y', "+ KEY_AIRED +") = STRFTIME('%Y', '"+ dateWithoutTime +"') ORDER BY " + KEY_AIRED;
 			break;
 		default:
 			break;
 		}
 
 		//String sql = "SELECT * FROM " + TABLE_EPISODES + " WHERE " + KEY_SEASON + " != 0 AND " + KEY_AIRED + " != '' AND " +  KEY_SERIESID + " = " + seriesId + " ORDER BY "+KEY_AIRED+" DESC";
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(sql, null);
 
 		cursor.moveToFirst();
 
 		while(!cursor.isAfterLast())
 		{
 			Episode e = new Episode();
 		
 			e.setTitle(cursor.getString(3));
 			e.setAired(cursor.getString(4));
 			e.setSummary(cursor.getString(6));
 			e.setSeason(cursor.getString(1));
 			e.setEpisode(cursor.getString(2));
 			e.setSeriesId(cursor.getString(19));
 
 			e.setWatched("0");
 			
 
 			
 			episodes.add(e);
 
 			cursor.moveToNext();
 		}
 		cursor.close();
 		db.close();
 
 
 
 		return episodes;
 	}
 
 	public void DeleteSeries(String seriesId)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		db.delete(TABLE_EPISODES, KEY_SERIESID + " = ?", new String[] { String.valueOf(seriesId) });
 		db.delete(TABLE_SERIES, KEY_SERIESID + " = ?", new String[] { String.valueOf(seriesId) });
 		db.close();
 	}
 }
 
 
