 package se.goagubbar.twee;
 
 import java.util.ArrayList;
 import se.goagubbar.twee.Models.Episode;
 import se.goagubbar.twee.Models.ExtendedSeries;
 import se.goagubbar.twee.Models.Profile;
 import se.goagubbar.twee.Models.Series;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DatabaseHandler extends SQLiteOpenHelper {
 
 	private static final int DATABASE_VERSION = 18;
 	private static final String DATABASE_NAME = "Teewee";
 
 	private static final String TABLE_SERIES = "Series";
 	private static final String TABLE_EPISODES = "Episodes";
 	private static final String TABLE_PROFILE = "Profile";
 
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
 
 	private static final String KEY_PROFILENAME = "Name";
 	private static final String KEY_PROFILEID = "ProfileId";
 
 
 	public DatabaseHandler(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(ENCODING_SETTING);
 
 		String CREATE_SERIES_TABLE = String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY autoincrement, %s TEXT,%s TEXT,%s TEXT,%s TEXT,%s TEXT,%s TEXT,%s TEXT,%s TEXT,%s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %S TEXT)", TABLE_SERIES, KEY_ID, KEY_SUMMARY, KEY_NAME,KEY_ACTORS,KEY_DAYTIME,KEY_GENRE,KEY_IMDBID,KEY_RATING,KEY_STATUS,KEY_IMAGE, KEY_FIRSTAIRED, KEY_HEADER, KEY_SERIESID, KEY_LASTUPDATED, KEY_PROFILEID);
 		db.execSQL(CREATE_SERIES_TABLE);
 
 		String CREATE_EPISODE_TABLE = String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY autoincrement,%s TEXT,%s TEXT,%s TEXT,%s TEXT,%s TEXT,%s TEXT, %s TEXT, %s TEXT, %s TEXT, %S TEXT)", TABLE_EPISODES, KEY_ID, KEY_SEASON, KEY_EPISODE, KEY_TITLE, KEY_AIRED, KEY_WATCHED, KEY_SUMMARY, KEY_SERIESID, KEY_LASTUPDATED, KEY_EPISODEID, KEY_PROFILEID);
 		db.execSQL(CREATE_EPISODE_TABLE);
 
 		String CREATE_PROFILE_TABLE = String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY autoincrement,%s TEXT)", TABLE_PROFILE, KEY_ID, KEY_PROFILENAME);
 		db.execSQL(CREATE_PROFILE_TABLE);
 
 		//Add default profile
 		ContentValues values = new ContentValues();
 		values.put(KEY_PROFILENAME, "Default");
 		db.insert(TABLE_PROFILE, null, values);
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
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
 		onCreate(db);
 
 		Log.d("Rebuild db", "Done");
 	}
 
 	//CRUD
 
 	public void AddShow(Series series)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		try {
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
 			values.put(KEY_PROFILEID, Utils.selectedProfile);
 
 			db.insert(TABLE_SERIES, null, values);
 
 
 		} catch (Exception e) {
 			Log.d("AddShow", e.getMessage());
 			// TODO: handle exception
 		}
 		finally
 		{
 			db.close();
 		}
 
 	}
 
 	public void AddEpisodes(ArrayList<Episode> episodes)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		try {
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
 				values.put(KEY_PROFILEID, Utils.selectedProfile);
 				db.insert(TABLE_EPISODES, null, values);
 
 			}
 			db.setTransactionSuccessful();
 			db.endTransaction();
 		} catch (Exception e) {
 			Log.d("AddEpisodes",e.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			db.close();
 		}
 
 
 	}
 
 	public void UpdateAndAddEpisodes(ArrayList<Episode> episodes, String seriesId)
 	{
 
 		ArrayList<String> oldEpisodes = GetEpisodesForShow(seriesId);
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = null;
 
 		try {
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
 					values.put(KEY_PROFILEID, Utils.selectedProfile);
 
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
 					values.put(KEY_PROFILEID, Utils.selectedProfile);
 					db.insert(TABLE_EPISODES, null, values);
 				}
 			}
 			db.setTransactionSuccessful();
 			db.endTransaction();
 		} catch (Exception e) {
 			Log.d("UpdateAndAddEpisodes",e.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			db.close();
 		}
 	}
 
 	public void UpdateShowImage(Series series)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 
 		try {
 			ContentValues values = new ContentValues();
 			values.put(KEY_IMAGE, series.getImage());
 			values.put(KEY_HEADER, series.getHeader());
 
 			db.update(TABLE_SERIES, values, KEY_SERIESID + " = ? AND " + KEY_PROFILEID + " = " + Utils.selectedProfile, new String[] { series.getSeriesId() });
 		} catch (Exception e) {
 			Log.d("UpdateShowImage",e.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			db.close();
 		}
 
 	}
 	
 	//DU HLL P MED KOLLEN AV EPISODEERNA!!!!!	
 	private ArrayList<String> GetEpisodesForShow(String seriesId)
 	{
 		ArrayList<String> episodes = new ArrayList<String>();
 		String sql = "SELECT "+ KEY_EPISODEID +" FROM " + TABLE_EPISODES + " WHERE " + KEY_SERIESID + " = " + seriesId + " AND " + KEY_PROFILEID + " = " + Utils.selectedProfile;
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(sql,null);
 		try {
 			cursor.moveToFirst();
 			while (!cursor.isAfterLast()) {
 				episodes.add(cursor.getString(0));
 				cursor.moveToNext();
 			}
 		} catch (Exception e) {
 			Log.d("GetEpisodesForSearies",e.getMessage());
 			// TODO: handle exception
 		}
 		finally
 		{
 			cursor.close();
 			db.close();
 		}
 
 
 
 
 		return episodes;
 
 	}
 
 	public ArrayList<Series> GetAllShows()
 	{
 
 		ArrayList<Series> series = new ArrayList<Series>();
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		String sql = "SELECT * FROM " + TABLE_SERIES + " WHERE ProfileId = "+ Utils.selectedProfile + " ORDER BY "+ KEY_NAME +" ASC";
 
 		Cursor cursor = db.rawQuery(sql,null);
 		try {
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
 
 				s.Episodes.add(GetNextEpisodeForShow(s.getSeriesId()));
 
 				series.add(s);
 				cursor.moveToNext();
 
 			}
 
 		} catch (Exception e) {
 			Log.d("GetAllSeries",e.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			cursor.close();
 			db.close();
 		}
 
 		return series;
 
 
 	}
 
 	public ArrayList<ExtendedSeries> GetMyShows()
 	{
 
 		ArrayList<ExtendedSeries> series = new ArrayList<ExtendedSeries>();
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		String sql = "SELECT * FROM " + TABLE_SERIES + " WHERE ProfileId = "+ Utils.selectedProfile + " ORDER BY "+ KEY_NAME +" ASC";
 
 		Cursor cursor = db.rawQuery(sql,null);
 		try {
 			cursor.moveToFirst();
 			while (!cursor.isAfterLast()) {
 				ExtendedSeries s = new ExtendedSeries();
 
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
 
 				s.Episodes.add(GetNextEpisodeForShow(s.getSeriesId()));
 
 				Episode e = s.Episodes.get(0);
 
 				if(s.getStatus().equals("Ended"))
 				{
 					s.setNextEpisodeInformation("");
 				}
 				else
 				{
 					s.setNextEpisodeInformation(e.getAired() != null ? new DateHelper().Episodenumber(e) + " " + e.getTitle() + " - " + new DateHelper().DaysTilNextEpisode(e.getAired()) : "No information");	
 				}
 
 
 				s.setTotalEpisodes(GetTotalEpisodes(s.getSeriesId()));
 				s.setwatchedEpisodes(GetWatchedEpisodes(s.getSeriesId()));
 				series.add(s);
 				cursor.moveToNext();
 
 			}
 
 		} catch (Exception e) {
 			Log.d("GetMyShows",e.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			cursor.close();
 			db.close();
 		}
 
 		return series;
 	}
 	
 	public ArrayList<se.goagubbar.twee.dto.Series> BackupShows()
 	{
 		ArrayList<se.goagubbar.twee.dto.Series> series = new ArrayList<se.goagubbar.twee.dto.Series>();
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		String sql = "SELECT * FROM " + TABLE_SERIES + " WHERE ProfileId = "+ Utils.selectedProfile + " ORDER BY "+ KEY_NAME +" ASC";
 
 		Cursor cursor = db.rawQuery(sql,null);
 		try {
 			cursor.moveToFirst();
 			while (!cursor.isAfterLast()) {
 				se.goagubbar.twee.dto.Series s = new se.goagubbar.twee.dto.Series();
 
 				s.Actors = cursor.getString(3);
 				s.Airs = cursor.getString(4);
 				s.Image = cursor.getString(9);
 				s.Genre = cursor.getString(5);
 				s.ImdbId = cursor.getString(6);
 				s.Name = cursor.getString(2);			
 				s.Rating = cursor.getString(7);
 				s.Status = cursor.getString(8);
 				s.Summary = cursor.getString(1);
 				s.FirstAired = cursor.getString(10);
 				s.Header = cursor.getString(11);
 				s.SeriesId = cursor.getString(12);
 				s.LastUpdated = cursor.getString(13);
 				
 				s.Episodes = BackupEpisodes(s.SeriesId);
 				
 				series.add(s);
 				cursor.moveToNext();
 
 			}
 
 		} catch (Exception e) {
 			Log.d("GetMyShows",e.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			cursor.close();
 			db.close();
 		}
 
 		return series;
 	}
 	
 	public ArrayList<se.goagubbar.twee.dto.Episode> BackupEpisodes(String showId)
 	{
 		ArrayList<se.goagubbar.twee.dto.Episode> episodes = new ArrayList<se.goagubbar.twee.dto.Episode>();
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		String sql = "SELECT * FROM " + TABLE_EPISODES + " WHERE ProfileId = "+ Utils.selectedProfile + " AND " + KEY_SERIESID + " = " + showId;
 
 		Cursor cursor = db.rawQuery(sql,null);
 		try {
 			cursor.moveToFirst();
 			while (!cursor.isAfterLast()) {
 				se.goagubbar.twee.dto.Episode e = new se.goagubbar.twee.dto.Episode();
 				
 				e.Aired = cursor.getString(4);
 				e.Episode = cursor.getString(2);
 				e.Season =cursor.getString(1);
 				e.Title =cursor.getString(3);
 				e.Watched = cursor.getString(5);
 				e.LastUpdated = cursor.getString(8);
 				e.EpisodeId = cursor.getString(9);
 				episodes.add(e);
 				cursor.moveToNext();
 
 			}
 
 		} catch (Exception ex) {
 			Log.d("BackupEpisodes", "" +  ex.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			cursor.close();
 			db.close();
 		}
 
 		return episodes;
 	}
 
 	public Episode GetLastAiredEpisodeForShow(String seriesId)
 	{
 		Episode e = new Episode();
 		SQLiteDatabase db = this.getReadableDatabase();
 		String dateWithoutTime  = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();
 
 		String sql = "SELECT * FROM "+ TABLE_EPISODES +" WHERE date("+ KEY_AIRED +") < date('"+ dateWithoutTime +"') AND "+KEY_SERIESID+" = "+ seriesId +" AND "+KEY_SEASON+" != 0 AND "+ KEY_PROFILEID + " = " + Utils.selectedProfile +" ORDER BY "+ KEY_AIRED +" DESC LIMIT 1";	
 
 
 		Cursor cursor = db.rawQuery(sql, null);
 		try {
 
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
 
 		} catch (Exception ex) {
 			Log.d("GetLastAiredEpisodeForSeries",ex.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			cursor.close();
 			db.close();
 		}
 		return e;
 
 	}
 
 	public Episode GetNextEpisodeForShow(String seriesId)
 	{
 		Episode e = new Episode();
 		SQLiteDatabase db = this.getReadableDatabase();
 		String dateWithoutTime  = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();
 
 		String sql = "SELECT * FROM "+ TABLE_EPISODES +" WHERE date("+ KEY_AIRED +") >= date('"+ dateWithoutTime +"') AND "+KEY_SERIESID+" = "+ seriesId +" AND "+KEY_SEASON+" != 0 AND "+KEY_PROFILEID+" = "+Utils.selectedProfile+" ORDER BY "+ KEY_AIRED +" ASC LIMIT 1";	
 
 		Cursor cursor = db.rawQuery(sql, null);
 		try {
 
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
 
 		} catch (Exception ex) {
 			Log.d("GetNextEpisodeForSeries", ex.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			cursor.close();
 			db.close();
 		}
 
 		return e;
 
 	}
 
 	public Episode GetEpisodeById(String episodeId)
 	{
 		Episode e = new Episode();
 
 		String sql = "SELECT * FROM "+ TABLE_EPISODES +" WHERE "+KEY_ID+" = "+ episodeId + " AND " + KEY_PROFILEID + " = " + Utils.selectedProfile;	
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		Cursor cursor = db.rawQuery(sql, null);
 		try {
 
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
 
 
 
 		} catch (Exception ex) {
 			Log.d("GetEpisodeById",ex.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			cursor.close();
 			db.close();
 		}
 
 		return e;
 	}
 
 	public Series GetShowById(String id)
 	{
 		Series s = new Series();
 		String sql = "SELECT * FROM " + TABLE_SERIES + " WHERE " + KEY_SERIESID + " = " + id + " AND " + KEY_PROFILEID + " = " + Utils.selectedProfile;
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(sql, null);
 
 		try {
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
 			}
 			//s.Episodes = GetEpisodes(s.getSeriesId());
 		} catch (Exception e) {
 			Log.d("GetSeriesById",e.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			cursor.close();
 			db.close();
 		}
 		return s;
 	}
 
 	public Boolean ShowExists(String seriesId)
 	{
 		String sql = "SELECT * FROM " + TABLE_SERIES + " WHERE " + KEY_SERIESID + " = " + seriesId + " AND " + KEY_PROFILEID + " = " + Utils.selectedProfile;
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(sql, null);
 
 		try {
 			if(cursor != null)
 			{
 				if(cursor.moveToFirst())
 				{
 					cursor.close();
 					return true;
 				}
 			}
 		} catch (Exception e) {
 			Log.d("SeriesExist",e.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			cursor.close();
 			db.close();
 		}
 
 
 
 		return false;
 	}
 
 	public void ToggleEpisodeWatched(String id, boolean watched)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 
 		try {
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
 
 			db.update(TABLE_EPISODES, values, KEY_ID + " = ? AND " + KEY_PROFILEID + " = " + Utils.selectedProfile, new String[] { id });
 		} catch (Exception e) {
 			Log.d("ToggleEpisodeWatch",e.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			db.close();
 		}
 
 	}
 
 	public void ToggleSeasonWatched(String id, String season, boolean watched)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 
 		try {
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
 
 			db.update(TABLE_EPISODES, values, KEY_AIRED + " < date('"+ dateWithoutTime +"') AND "+KEY_SERIESID + " = ? AND " + KEY_SEASON + " = ? AND " + KEY_PROFILEID + " = " + Utils.selectedProfile, new String[] { id, season });
 
 		} catch (Exception e) {
 			Log.d("ToggleSeasonWatched",e.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			db.close();
 		}
 	}
 
 	public void MarkShowAsWatched(String id)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 
 		try {
 			String dateWithoutTime  = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();
 
 			ContentValues values = new ContentValues();
 			values.put(KEY_WATCHED, "1");
 			db.update(TABLE_EPISODES, values, KEY_AIRED + " < date('"+ dateWithoutTime +"') AND "+ KEY_SERIESID +" = ? AND " + KEY_PROFILEID + " = " + Utils.selectedProfile, new String[] {id});
 
 		} catch (Exception e) {
 			Log.d("MarkSeriesAsWatched",e.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			db.close();
 		}
 
 	}
 
 	public ArrayList<Episode> GetEpisodes(String seriesId)
 	{
 		ArrayList<Episode> episodes = new ArrayList<Episode>();
 		String sql = "SELECT * FROM " + TABLE_EPISODES + " WHERE " + KEY_SEASON + " != 0 AND " + KEY_AIRED + " != '' AND " +  KEY_SERIESID + " = " + seriesId + " AND " + KEY_PROFILEID + " = " + Utils.selectedProfile + " ORDER BY CAST("+ KEY_SEASON +" AS INTEGER) DESC, CAST(" + KEY_EPISODE + " AS INTEGER) DESC";
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		Cursor cursor = db.rawQuery(sql, null);
 
 		try {
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
 		} catch (Exception e) {
 			Log.d("GetEpisodes",e.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			cursor.close();
 			db.close();
 		}
 
 		return episodes;
 	}
 
 	public ArrayList<Episode> GetAiredEpisodes(String seriesId)
 	{
 
 		String dateWithoutTime  = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();
 		ArrayList<Episode> episodes = new ArrayList<Episode>();
 		String sql = "SELECT * FROM " + TABLE_EPISODES + " WHERE " + KEY_SEASON + " != 0 AND " + KEY_AIRED + " != '' AND " + KEY_AIRED + " <= date('" + dateWithoutTime + "') AND " +  KEY_SERIESID + " = " + seriesId + " AND " + KEY_PROFILEID + " = " + Utils.selectedProfile + " ORDER BY "+KEY_AIRED+" DESC";
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(sql, null);
 		try {
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
 
 		} catch (Exception e) {
 			Log.d("GetAiredEpisodes",e.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			cursor.close();
 			db.close();
 		}
 
 
 
 
 
 		return episodes;
 	}
 
 	public ArrayList<Episode> GetAllEpisodesForGivenTimePeriod(int timeperiod)
 	{
 		ArrayList<Episode> episodes = new ArrayList<Episode>();
 		String dateWithoutTime  = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();
 		String sql = "";
 		SQLiteDatabase db = this.getReadableDatabase();
 
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
 
 		Cursor cursor = db.rawQuery(sql, null);
 
 		try {
 			cursor.moveToFirst();
 
 			while(!cursor.isAfterLast())
 			{
 				Episode e = new Episode();
 
 				e.setTitle(cursor.getString(3));
 				e.setAired(cursor.getString(4));
 				e.setSummary(cursor.getString(6));
 				e.setSeason(cursor.getString(1));
 				e.setEpisode(cursor.getString(2));
 				e.setSeriesId(cursor.getString(20));
 
 				e.setWatched("0");
 
 				episodes.add(e);
 
 				cursor.moveToNext();
 			}
 
 		} catch (Exception e) {
 			Log.d("GetAllEpisodesForGivenTimePeriod",e.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			cursor.close();
 			db.close();	
 		}
 		return episodes;
 	}
 
 	public void DeleteShow(String seriesId)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 
 		try {
 			db.delete(TABLE_EPISODES, KEY_SERIESID + " = ? AND " + KEY_PROFILEID + " = " + Utils.selectedProfile, new String[] { String.valueOf(seriesId) });
 			db.delete(TABLE_SERIES, KEY_SERIESID + " = ? AND " + KEY_PROFILEID + " = " + Utils.selectedProfile, new String[] { String.valueOf(seriesId) });	
 		} catch (Exception e) {
 			Log.d("DeleteSeries",e.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			db.close();			
 		}
 
 
 	}
 
 	//Profiles
 	public ArrayList<Profile> GetAllprofiles()
 	{
 
 		ArrayList<Profile> profiles = new ArrayList<Profile>();
 		SQLiteDatabase db = this.getReadableDatabase();
 		String sql = "SELECT * FROM " + TABLE_PROFILE;
 
 		Cursor cursor = db.rawQuery(sql, null);
 		try {
 			cursor.moveToFirst();
 			while (!cursor.isAfterLast()) {
 				Profile p = new Profile();
 
 				p.setId(Integer.parseInt(cursor.getString(0)));
 				p.setName(cursor.getString(1));
 
 				profiles.add(p);
 
 				cursor.moveToNext();
 			}
 
 
 		} catch (Exception e) {
 			Log.d("GetAllProfiles",e.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			cursor.close();
 			db.close();
 		}
 
 		return profiles;
 	}
 
 	public void AddNewProfile(String profileName)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 
 		try {
 			ContentValues values = new ContentValues();
 
 			values.put(KEY_PROFILENAME, profileName);
 			db.insert(TABLE_PROFILE, null, values);			
 		} catch (Exception e) {
 			Log.d("AddNewProfile",e.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			db.close();
 		}
 
 
 
 	}
 
 	public String GetSelectedProfile(){
 
 		String name = "";
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		String sql = "SELECT * FROM " + TABLE_PROFILE + " WHERE Id = " + Utils.selectedProfile;
 
 		Cursor cursor = db.rawQuery(sql, null);
 
 		try {
 			if(cursor != null)
 			{
 				if(cursor.moveToFirst())
 				{
 					name = cursor.getString(1);				
 				}
 			}
 
 		} catch (Exception e) {
 			Log.d("GetSelectedProfile",e.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			cursor.close();
 			db.close();
 		}
 		return name;
 	}
 
 //	public String GetSelectedProfile(Integer profileId){
 //		SQLiteDatabase db = this.getReadableDatabase();
 //		String name = "";
 //		String sql = "SELECT * FROM " + TABLE_PROFILE + " WHERE Id = " + profileId;
 //
 //		Cursor cursor = db.rawQuery(sql, null);
 //		try {
 //
 //			if(cursor != null)
 //			{
 //				if(cursor.moveToFirst())
 //				{
 //					name = cursor.getString(1);				
 //				}
 //			}
 //		} catch (Exception e) {
 //			Log.d("GetSelectedProfile",e.getMessage());
 //			// TODO: handle exception
 //		}
 //		finally{
 //			cursor.close();
 //			db.close();
 //		}
 //		return name;
 //	}
 
 	private int GetTotalEpisodes(String seriesId){
 		SQLiteDatabase db = this.getReadableDatabase();
 		int episodes = 0;
 		String dateWithoutTime  = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();
 		String sql = "SELECT COUNT(*) FROM " + TABLE_EPISODES + " WHERE " + KEY_SEASON + " != 0 AND " + KEY_AIRED + " != '' AND " + KEY_AIRED + " <= ('" + dateWithoutTime + "') AND "  +  KEY_SERIESID + " = " + seriesId + " AND " + KEY_PROFILEID + " = " + Utils.selectedProfile;
 
 		Cursor cursor = db.rawQuery(sql, null);
 		try {
 
 			if(cursor != null)
 			{
 				if(cursor.moveToFirst())
 				{
 					episodes = Integer.parseInt(cursor.getString(0));				
 				}
 
 			}
 
 		} catch (Exception e) {
 			Log.d("GetTotalEpisodes",e.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			cursor.close();
 			db.close();
 		}
 		return episodes;
 	}
 
 	private int GetWatchedEpisodes(String seriesId){
 		SQLiteDatabase db = this.getReadableDatabase();
 		int episodes = 0;
 
 		String sql = "SELECT COUNT(*) FROM " + TABLE_EPISODES + " WHERE " + KEY_SEASON + " != 0 AND " + KEY_AIRED + " != '' AND " +  KEY_SERIESID + " = " + seriesId + " AND " + KEY_PROFILEID + " = " + Utils.selectedProfile + " AND " + KEY_WATCHED + " = '1'";
 
 		Cursor cursor = db.rawQuery(sql, null);
 		try {
 
 			if(cursor != null)
 			{
 				if(cursor.moveToFirst())
 				{
 					episodes = Integer.parseInt(cursor.getString(0));				
 				}
 
 			}
 
 		} catch (Exception e) {
 			Log.d("GetWatchedEpisodes",e.getMessage());
 			// TODO: handle exception
 		}
 		finally{
 			cursor.close();
 			db.close();
 		}
 		return episodes;
 	}
 
 }
 
 
