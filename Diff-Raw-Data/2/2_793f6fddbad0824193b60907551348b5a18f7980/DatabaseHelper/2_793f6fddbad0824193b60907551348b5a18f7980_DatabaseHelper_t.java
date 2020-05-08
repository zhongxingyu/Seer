 package ua.in.leopard.androidCoocooAfisha;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 /*
  * ./adb -s emulator-5554 shell
  * sqlite3 /data/data/ua.in.leopard.androidCoocooAfisha/databases/coocoo_afisha_db
 */
 public class DatabaseHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 2;
 	private static final String DATABASE_NAME="coocoo_afisha_db";
 	private final Context myContext;
 	
 	private static final String AFISHA_TABLE="afisha";
 	private static final String AFISHA_TABLE_EXT_ID="id";
 	private static final String AFISHA_TABLE_CINEMA_ID="cinema_id";
 	private static final String AFISHA_TABLE_THEATER_ID="theater_id";
 	private static final String AFISHA_TABLE_ZAL="zal_title";
 	private static final String AFISHA_TABLE_DATA_BEGIN="data_begin";
 	private static final String AFISHA_TABLE_DATA_END="data_end";
 	private static final String AFISHA_TABLE_TIMES="times";
 	private static final String AFISHA_TABLE_PRICES="prices";
 	
 	private static final String CINEMAS_TABLE="cinemas";
 	private static final String CINEMAS_TABLE_EXT_ID="id";
 	private static final String CINEMAS_TABLE_TITLE="title";
 	private static final String CINEMAS_TABLE_OR_TITLE="orig_title";
 	private static final String CINEMAS_TABLE_YEAR="year";
 	private static final String CINEMAS_TABLE_POSTER="poster";
 	private static final String CINEMAS_TABLE_DESCRIPTION="description";
 	private static final String CINEMAS_TABLE_POSTER_IMAGE="poster_img";
 
 	private static final String THEATERS_TABLE="theaters";
 	private static final String THEATERS_TABLE_EXT_ID="id";
 	private static final String THEATERS_TABLE_CITY_ID="city_id";
 	private static final String THEATERS_TABLE_TITLE="title";
 	private static final String THEATERS_TABLE_LINK="link";
 	private static final String THEATERS_TABLE_ADDRESS="address";
 	private static final String THEATERS_TABLE_PHONE="phone";
 	private static final String THEATERS_TABLE_FILTER="is_filter";
 
 	
 	public DatabaseHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		this.myContext = context;
 	}
 	
 	public List<TheaterDB> getTheaters(Boolean force_select){
 		String filter_sql = "";
 		if (!force_select){
 			filter_sql = this.isFiltered();
 		}
 		
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor result = db.query(THEATERS_TABLE, 
 				new String[] {
 				THEATERS_TABLE_EXT_ID, 
 				THEATERS_TABLE_CITY_ID, 
 				THEATERS_TABLE_TITLE,
 				THEATERS_TABLE_LINK,
 				THEATERS_TABLE_ADDRESS,
 				THEATERS_TABLE_PHONE,
 				THEATERS_TABLE_FILTER
 				}, THEATERS_TABLE_CITY_ID + " = ?" + filter_sql,
 				new String[] {EditPreferences.getCityId(this.myContext)}, null, null, THEATERS_TABLE_TITLE);
 		
 		result.moveToFirst();
 		List<TheaterDB> theaters = new ArrayList<TheaterDB>();
 		while (!result.isAfterLast()) {
 			theaters.add(new TheaterDB(
 				result.getInt(result.getColumnIndex(THEATERS_TABLE_EXT_ID)),
 				result.getInt(result.getColumnIndex(THEATERS_TABLE_CITY_ID)),
 				result.getString(result.getColumnIndex(THEATERS_TABLE_TITLE)),
 				result.getString(result.getColumnIndex(THEATERS_TABLE_LINK)),
 				result.getString(result.getColumnIndex(THEATERS_TABLE_ADDRESS)),
 				result.getString(result.getColumnIndex(THEATERS_TABLE_PHONE)),
 				result.getInt(result.getColumnIndex(THEATERS_TABLE_FILTER))
 			));
 			result.moveToNext();
 		}
 		result.close();
 		db.close();
 		return theaters;
 	}
 	
 	public TheaterDB getTheater(int id){
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor result = db.query(THEATERS_TABLE, 
 				new String[] {
 				THEATERS_TABLE_EXT_ID, 
 				THEATERS_TABLE_CITY_ID, 
 				THEATERS_TABLE_TITLE,
 				THEATERS_TABLE_LINK,
 				THEATERS_TABLE_ADDRESS,
 				THEATERS_TABLE_PHONE,
 				THEATERS_TABLE_FILTER
 				}, THEATERS_TABLE_EXT_ID + " = ?",
 				new String[] {Integer.toString(id)}, null, null, THEATERS_TABLE_TITLE, "1");
 		
 		result.moveToFirst();
 		TheaterDB theater_row = null;
 		while (!result.isAfterLast()) {
 			theater_row = new TheaterDB(
 				result.getInt(result.getColumnIndex(THEATERS_TABLE_EXT_ID)),
 				result.getInt(result.getColumnIndex(THEATERS_TABLE_CITY_ID)),
 				result.getString(result.getColumnIndex(THEATERS_TABLE_TITLE)),
 				result.getString(result.getColumnIndex(THEATERS_TABLE_LINK)),
 				result.getString(result.getColumnIndex(THEATERS_TABLE_ADDRESS)),
 				result.getString(result.getColumnIndex(THEATERS_TABLE_PHONE)),
 				result.getInt(result.getColumnIndex(THEATERS_TABLE_FILTER))
 			);
 			result.moveToNext();
 		}
 		result.close();
 		db.close();
 		return theater_row;
 	}
 
 	
 	public void setTheater(TheaterDB theater_row){
 		TheaterDB tmp_obj = this.getTheater(theater_row.getId());
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues cv = new ContentValues();
 		cv.put(THEATERS_TABLE_EXT_ID, theater_row.getId());
 		cv.put(THEATERS_TABLE_CITY_ID, theater_row.getCityId());
 		cv.put(THEATERS_TABLE_TITLE, theater_row.getTitle());
 		cv.put(THEATERS_TABLE_LINK, theater_row.getLink());
 		cv.put(THEATERS_TABLE_ADDRESS, theater_row.getAddress());
 		cv.put(THEATERS_TABLE_PHONE, theater_row.getPhone());
 		
 		if (tmp_obj == null){
 			db.insert(THEATERS_TABLE, null, cv);
 		} else if (!tmp_obj.equal(theater_row)){
 			db.update(THEATERS_TABLE, cv, THEATERS_TABLE_EXT_ID + " = ?", new String[] {Integer.toString(theater_row.getId())});
 		}
 		db.close();
 	}
 	
 	public void addFilterTheater(TheaterDB theater_row){
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues cv = new ContentValues();
 		cv.put(THEATERS_TABLE_FILTER, 1);
 		db.update(THEATERS_TABLE, cv, THEATERS_TABLE_EXT_ID + " = ?", new String[] {Integer.toString(theater_row.getId())});
 		db.close();
 	}
 	
 	public void removeFilterTheater(TheaterDB theater_row){
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues cv = new ContentValues();
 		cv.put(THEATERS_TABLE_FILTER, 0);
 		db.update(THEATERS_TABLE, cv, THEATERS_TABLE_EXT_ID + " = ?", new String[] {Integer.toString(theater_row.getId())});
 		db.close();
 	}
 	
 	public void clearFilterTheaters(){
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues cv = new ContentValues();
 		cv.put(THEATERS_TABLE_FILTER, 0);
 		db.update(THEATERS_TABLE, cv, null, null);
 		db.close();
 	}
 	
 	public Boolean isSetFilters(){
 		SQLiteDatabase db = this.getWritableDatabase();
 		Cursor result = db.query(THEATERS_TABLE, 
 				new String[] {
 				THEATERS_TABLE_EXT_ID, 
 				THEATERS_TABLE_FILTER
 				}, THEATERS_TABLE_FILTER + " = 1",
 				null, null, null, THEATERS_TABLE_EXT_ID);
 		Integer row_count = result.getCount();
 		result.close();
 		db.close();
 		if (row_count > 0){
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private String isFiltered(){
 		if (EditPreferences.isTheatersIsFilter(this.myContext)){
 			return " AND " + THEATERS_TABLE + "." + THEATERS_TABLE_FILTER + " = 1 ";
 		} else {
 			return "";
 		}
 	}
 	
 	public CinemaDB getCinema(int id){
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor result = db.query(CINEMAS_TABLE, 
 				new String[] {
 				CINEMAS_TABLE_EXT_ID, 
 				CINEMAS_TABLE_TITLE, 
 				CINEMAS_TABLE_OR_TITLE,
 				CINEMAS_TABLE_YEAR,
 				CINEMAS_TABLE_POSTER,
 				CINEMAS_TABLE_DESCRIPTION,
 				CINEMAS_TABLE_POSTER_IMAGE
 				}, CINEMAS_TABLE_EXT_ID + " = ?",
 				new String[] {Integer.toString(id)}, null, null, CINEMAS_TABLE_TITLE, "1");
 		
 		result.moveToFirst();
 		CinemaDB cinema_row = null;
 		while (!result.isAfterLast()) {
 			cinema_row = new CinemaDB(
 				result.getInt(result.getColumnIndex(CINEMAS_TABLE_EXT_ID)),
 				result.getString(result.getColumnIndex(CINEMAS_TABLE_TITLE)),
 				result.getString(result.getColumnIndex(CINEMAS_TABLE_OR_TITLE)),
 				result.getString(result.getColumnIndex(CINEMAS_TABLE_YEAR)),
 				result.getString(result.getColumnIndex(CINEMAS_TABLE_POSTER)),
 				result.getString(result.getColumnIndex(CINEMAS_TABLE_DESCRIPTION))
 			);
 			if (EditPreferences.isCachedPosters(this.myContext) && 
 					!result.isNull(result.getColumnIndex(CINEMAS_TABLE_POSTER_IMAGE))){
 				cinema_row.setCachedPoster(result.getBlob(result.getColumnIndex(CINEMAS_TABLE_POSTER_IMAGE)));
 			}
 			result.moveToNext();
 		}
 		result.close();
 		db.close();
 		return cinema_row;
 	}
 	
 	public void setCinema(CinemaDB cinema_row){
 		CinemaDB tmp_obj = this.getCinema(cinema_row.getId());
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues cv = new ContentValues();
 		cv.put(CINEMAS_TABLE_EXT_ID, cinema_row.getId());
 		cv.put(CINEMAS_TABLE_TITLE, cinema_row.getTitle());
 		cv.put(CINEMAS_TABLE_OR_TITLE, cinema_row.getOrigTitle());
 		cv.put(CINEMAS_TABLE_YEAR, cinema_row.getYear());
 		cv.put(CINEMAS_TABLE_POSTER, cinema_row.getPoster());
 		cv.put(CINEMAS_TABLE_DESCRIPTION, cinema_row.getDescription());
 		
 		if (EditPreferences.isCachedPosters(this.myContext) && cinema_row.getPoster() != null && tmp_obj == null){
 			byte[] poster = cinema_row.setFromInetPoster();
 			if (poster != null){
 				cv.put(CINEMAS_TABLE_POSTER_IMAGE, poster);
 			}
 		}
 		
 		if (tmp_obj == null){
 			db.insert(CINEMAS_TABLE, null, cv);
 		} else if (!tmp_obj.equal(cinema_row)){
 			db.update(CINEMAS_TABLE, cv, CINEMAS_TABLE_EXT_ID + " = ?", new String[] {Integer.toString(cinema_row.getId())});
 		} else if (tmp_obj != null && EditPreferences.isCachedPosters(this.myContext) && 
 				tmp_obj.getPoster() != null && tmp_obj.getCachedPoster() == null){
 			
 			byte[] poster = tmp_obj.setFromInetPoster();
 			if (poster != null){
 				ContentValues cv_2 = new ContentValues();
 				cv_2.put(CINEMAS_TABLE_POSTER_IMAGE, poster);
 				db.update(CINEMAS_TABLE, cv_2, CINEMAS_TABLE_EXT_ID + " = ?", new String[] {Integer.toString(tmp_obj.getId())});
 			}
 		}
 		db.close();
 	}
 	
 	public List<CinemaDB> getTodayOrTomorrowByTheater(TheaterDB theater, Boolean is_today){
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Calendar currentDate = Calendar.getInstance();
 		SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
 		if (!is_today){
 			currentDate.add(Calendar.DATE, 1);
 		}
 		String dateNow = iso8601Format.format(currentDate.getTime()) + " 00:00:00";
 		
 		
 		Cursor result = db.rawQuery("SELECT " + 
 				CINEMAS_TABLE + "." + CINEMAS_TABLE_EXT_ID + "," + 
 				CINEMAS_TABLE + "." + CINEMAS_TABLE_TITLE + "," + 
 				CINEMAS_TABLE + "." + CINEMAS_TABLE_OR_TITLE + "," + 
 				CINEMAS_TABLE + "." + CINEMAS_TABLE_YEAR + "," + 
 				CINEMAS_TABLE + "." + CINEMAS_TABLE_POSTER + "," + 
 				CINEMAS_TABLE + "." + CINEMAS_TABLE_DESCRIPTION + "," + 
 				CINEMAS_TABLE + "." + CINEMAS_TABLE_POSTER_IMAGE + "," + 
 				AFISHA_TABLE + "." + AFISHA_TABLE_ZAL + "," + 
 				AFISHA_TABLE + "." + AFISHA_TABLE_DATA_BEGIN + "," + 
 				AFISHA_TABLE + "." + AFISHA_TABLE_DATA_END + "," + 
 				AFISHA_TABLE + "." + AFISHA_TABLE_TIMES + "," + 
 				AFISHA_TABLE + "." + AFISHA_TABLE_PRICES + 
 				" FROM " + AFISHA_TABLE + 
 				" LEFT JOIN " + CINEMAS_TABLE + " ON " + 
 				AFISHA_TABLE + "." + AFISHA_TABLE_CINEMA_ID + " = " + 
 				CINEMAS_TABLE + "." + CINEMAS_TABLE_EXT_ID + 
 				" WHERE " + AFISHA_TABLE + "." + AFISHA_TABLE_THEATER_ID + " = " + 
 				Integer.toString(theater.getId()) + " AND " + 
 				AFISHA_TABLE + "." + AFISHA_TABLE_DATA_BEGIN + " <= ? AND " + 
 				AFISHA_TABLE + "." + AFISHA_TABLE_DATA_END + " >= ? " + 
 				" ORDER BY " + CINEMAS_TABLE + "." + CINEMAS_TABLE_TITLE,
 				new String[] {dateNow, dateNow});
 		
 		result.moveToFirst();
 		List<CinemaDB> cinemas_list = new ArrayList<CinemaDB>();
 		while (!result.isAfterLast()) {
 			CinemaDB cinema_row = new CinemaDB(
 				result.getInt(result.getColumnIndex(CINEMAS_TABLE_EXT_ID)),
 				result.getString(result.getColumnIndex(CINEMAS_TABLE_TITLE)),
 				result.getString(result.getColumnIndex(CINEMAS_TABLE_OR_TITLE)),
 				result.getString(result.getColumnIndex(CINEMAS_TABLE_YEAR)),
 				result.getString(result.getColumnIndex(CINEMAS_TABLE_POSTER)),
 				result.getString(result.getColumnIndex(CINEMAS_TABLE_DESCRIPTION))
 			);
 			if (!result.isNull(result.getColumnIndex(AFISHA_TABLE_ZAL))){
 				cinema_row.setZalTitle(result.getString(result.getColumnIndex(AFISHA_TABLE_ZAL)));
 			}
 			if (!result.isNull(result.getColumnIndex(AFISHA_TABLE_TIMES))){
 				cinema_row.setTimes(result.getString(result.getColumnIndex(AFISHA_TABLE_TIMES)));
 			}
 			if (!result.isNull(result.getColumnIndex(AFISHA_TABLE_PRICES))){
 				cinema_row.setPrices(result.getString(result.getColumnIndex(AFISHA_TABLE_PRICES)));
 			}
 			if (EditPreferences.isCachedPosters(this.myContext) && 
 					!result.isNull(result.getColumnIndex(CINEMAS_TABLE_POSTER_IMAGE))){
 				cinema_row.setCachedPoster(result.getBlob(result.getColumnIndex(CINEMAS_TABLE_POSTER_IMAGE)));
 			}
 			cinemas_list.add(cinema_row);
 			result.moveToNext();
 		}
 		result.close();
 		db.close();
 		return cinemas_list;
 	}
 	
 	public List<TheaterDB> getTodayOrTomorrowByCinema(CinemaDB cinema, Boolean is_today){
 		String filter_sql = "";
 		filter_sql = this.isFiltered();
 		
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Calendar currentDate = Calendar.getInstance();
 		SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
 		if (!is_today){
 			currentDate.add(Calendar.DATE, 1);
 		}
 		String dateNow = iso8601Format.format(currentDate.getTime()) + " 00:00:00";
 		
 		Cursor result = db.rawQuery("SELECT " + 
 				THEATERS_TABLE + "." + THEATERS_TABLE_EXT_ID + "," + 
 				THEATERS_TABLE + "." + THEATERS_TABLE_CITY_ID + "," + 
 				THEATERS_TABLE + "." + THEATERS_TABLE_TITLE + "," + 
 				THEATERS_TABLE + "." + THEATERS_TABLE_LINK + "," + 
 				THEATERS_TABLE + "." + THEATERS_TABLE_ADDRESS + "," + 
 				THEATERS_TABLE + "." + THEATERS_TABLE_PHONE + "," + 
 				THEATERS_TABLE + "." + THEATERS_TABLE_FILTER + "," + 
 				AFISHA_TABLE + "." + AFISHA_TABLE_DATA_BEGIN + "," + 
 				AFISHA_TABLE + "." + AFISHA_TABLE_DATA_END + 
 				" FROM " + AFISHA_TABLE + 
 				" LEFT JOIN " + THEATERS_TABLE + " ON " + 
 				AFISHA_TABLE + "." + AFISHA_TABLE_THEATER_ID + " = " + 
 				THEATERS_TABLE + "." + THEATERS_TABLE_EXT_ID + 
 				" WHERE " + AFISHA_TABLE + "." + AFISHA_TABLE_CINEMA_ID + " = " + 
 				Integer.toString(cinema.getId()) + " AND " + 
 				AFISHA_TABLE + "." + AFISHA_TABLE_DATA_BEGIN + " <= ? AND " + 
 				AFISHA_TABLE + "." + AFISHA_TABLE_DATA_END + " >= ? " + 
 				filter_sql + 
 				" ORDER BY " + THEATERS_TABLE + "." + THEATERS_TABLE_TITLE,
 				new String[] {dateNow, dateNow});
 		
 		result.moveToFirst();
 		List<TheaterDB> theater_list = new ArrayList<TheaterDB>();
 		while (!result.isAfterLast()) {
 			theater_list.add(new TheaterDB(
 				result.getInt(result.getColumnIndex(THEATERS_TABLE_EXT_ID)),
 				result.getInt(result.getColumnIndex(THEATERS_TABLE_CITY_ID)),
 				result.getString(result.getColumnIndex(THEATERS_TABLE_TITLE)),
 				result.getString(result.getColumnIndex(THEATERS_TABLE_LINK)),
 				result.getString(result.getColumnIndex(THEATERS_TABLE_ADDRESS)),
 				result.getString(result.getColumnIndex(THEATERS_TABLE_PHONE)),
 				result.getInt(result.getColumnIndex(THEATERS_TABLE_FILTER))
 			));
 			result.moveToNext();
 		}
 		result.close();
 		db.close();
 		return theater_list;
 	}
 
 	public List<CinemaDB> getTodayCinemas(){
 		Calendar currentDate = Calendar.getInstance();
 		SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
 		String dateNow = iso8601Format.format(currentDate.getTime()) + " 00:00:00";
 		
 		List<TheaterDB> theaters_list = this.getTheaters(false);
 		String theater_ids = "";
 		for (int i = 0; i < theaters_list.size(); i++){
 			if (theater_ids != ""){
 				theater_ids = theater_ids + ",";
 			}
 			theater_ids = theater_ids + Integer.toString(theaters_list.get(i).getId());
 		}
 		
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor result = db.rawQuery("SELECT * FROM " + CINEMAS_TABLE + 
 				" WHERE " + CINEMAS_TABLE_EXT_ID + " IN (SELECT DISTINCT " + 
 				AFISHA_TABLE_CINEMA_ID + " FROM " + AFISHA_TABLE + " WHERE " + 
 				AFISHA_TABLE_THEATER_ID + " IN (" + theater_ids + ") AND " + 
 				AFISHA_TABLE_DATA_BEGIN + " <= ? AND " + 
 				AFISHA_TABLE_DATA_END + " >= ?) ORDER BY " + CINEMAS_TABLE_TITLE,
 				new String[] {dateNow, dateNow});
 		
 		result.moveToFirst();
 		List<CinemaDB> cinemas_list = new ArrayList<CinemaDB>();
 		while (!result.isAfterLast()) {
 			cinemas_list.add(new CinemaDB(
 				result.getInt(result.getColumnIndex(CINEMAS_TABLE_EXT_ID)),
 				result.getString(result.getColumnIndex(CINEMAS_TABLE_TITLE)),
 				result.getString(result.getColumnIndex(CINEMAS_TABLE_OR_TITLE)),
 				result.getString(result.getColumnIndex(CINEMAS_TABLE_YEAR)),
 				result.getString(result.getColumnIndex(CINEMAS_TABLE_POSTER)),
 				result.getString(result.getColumnIndex(CINEMAS_TABLE_DESCRIPTION))
 			));
 			result.moveToNext();
 		}
 		result.close();
 		db.close();
 		return cinemas_list;
 	}
 	
 	public AfishaDB getAfisha(int id){
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor result = db.query(AFISHA_TABLE, 
 				new String[] {
 				AFISHA_TABLE_EXT_ID, 
 				AFISHA_TABLE_CINEMA_ID, 
 				AFISHA_TABLE_THEATER_ID, 
 				AFISHA_TABLE_ZAL, 
 				AFISHA_TABLE_DATA_BEGIN,
 				AFISHA_TABLE_DATA_END,
 				AFISHA_TABLE_TIMES,
 				AFISHA_TABLE_PRICES
 				}, AFISHA_TABLE_EXT_ID + " = ?",
 				new String[] {Integer.toString(id)}, null, null, AFISHA_TABLE_EXT_ID, "1");
 		
 		result.moveToFirst();
 		AfishaDB afisha_row = null;
 		while (!result.isAfterLast()) {
 			afisha_row = new AfishaDB(
 				result.getInt(result.getColumnIndex(AFISHA_TABLE_EXT_ID)),
 				result.getInt(result.getColumnIndex(AFISHA_TABLE_CINEMA_ID)),
 				result.getInt(result.getColumnIndex(AFISHA_TABLE_THEATER_ID)),
 				result.getString(result.getColumnIndex(AFISHA_TABLE_ZAL)),
 				result.getString(result.getColumnIndex(AFISHA_TABLE_DATA_BEGIN)),
 				result.getString(result.getColumnIndex(AFISHA_TABLE_DATA_END)),
 				result.getString(result.getColumnIndex(AFISHA_TABLE_TIMES)),
 				result.getString(result.getColumnIndex(AFISHA_TABLE_PRICES))
 			);
 			result.moveToNext();
 		}
 		result.close();
 		db.close();
 		return afisha_row;
 	}
 	
 
 	public void setAfisha(AfishaDB afisha_row){
 		AfishaDB tmp_obj = this.getAfisha(afisha_row.getId());
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues cv = new ContentValues();
 		cv.put(AFISHA_TABLE_EXT_ID, afisha_row.getId());
 		cv.put(AFISHA_TABLE_CINEMA_ID, afisha_row.getCinemaId());
 		cv.put(AFISHA_TABLE_THEATER_ID, afisha_row.getTheaterId());
 		cv.put(AFISHA_TABLE_ZAL, afisha_row.getZalTitle());
 		cv.put(AFISHA_TABLE_DATA_BEGIN, afisha_row.getDataBegin());
 		cv.put(AFISHA_TABLE_DATA_END, afisha_row.getDataEnd());
 		cv.put(AFISHA_TABLE_TIMES, afisha_row.getTimes());
 		cv.put(AFISHA_TABLE_PRICES, afisha_row.getPrices());
 		
 		if (tmp_obj == null){
 			db.insert(AFISHA_TABLE, null, cv);
 		} else if (!tmp_obj.equal(afisha_row)){
 			db.update(AFISHA_TABLE, cv, AFISHA_TABLE_EXT_ID + " = ?", 
 					new String[] {Integer.toString(afisha_row.getId())});
 		}
 		db.close();
 	}
 	
 	public void setAfishaTransaction(List<AfishaDB> afisha_data){
 		SQLiteDatabase db = this.getWritableDatabase();
 		db.beginTransaction();
 		try {
 			for (int i = 0; i < afisha_data.size(); i++){
 				AfishaDB afisha_row = afisha_data.get(i);
 				Cursor result = db.query(AFISHA_TABLE, 
 						new String[] {
 						AFISHA_TABLE_EXT_ID
 						}, AFISHA_TABLE_EXT_ID + " = ?",
 						new String[] {Integer.toString(afisha_row.getId())}, null, null, 
 						null, "1");
 				
 				if (result.getCount() == 0){
 					ContentValues cv = new ContentValues();
 					cv.put(AFISHA_TABLE_EXT_ID, afisha_row.getId());
 					cv.put(AFISHA_TABLE_CINEMA_ID, afisha_row.getCinemaId());
 					cv.put(AFISHA_TABLE_THEATER_ID, afisha_row.getTheaterId());
 					cv.put(AFISHA_TABLE_ZAL, afisha_row.getZalTitle());
 					cv.put(AFISHA_TABLE_DATA_BEGIN, afisha_row.getDataBegin());
 					cv.put(AFISHA_TABLE_DATA_END, afisha_row.getDataEnd());
 					cv.put(AFISHA_TABLE_TIMES, afisha_row.getTimes());
 					cv.put(AFISHA_TABLE_PRICES, afisha_row.getPrices());
 					db.insert(AFISHA_TABLE, null, cv);
 				} 
 				result.close();		
 			}
 			db.setTransactionSuccessful();
 		} finally {
 			db.endTransaction();
 		}
 		db.close();
 	}
 	
 	public void clearOldData(){
 		Calendar currentDate = Calendar.getInstance();
 		SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
 		String dateNow = iso8601Format.format(currentDate.getTime()) + " 00:00:00";
 		
 		SQLiteDatabase db = this.getWritableDatabase();
 		db.delete(AFISHA_TABLE, AFISHA_TABLE_DATA_END + " < ?", new String[] {dateNow});
 		
 		db.beginTransaction();
 		try {
 			Cursor result = db.query(CINEMAS_TABLE, 
 					new String[] {
 					CINEMAS_TABLE_EXT_ID
 					}, null, null, null, null, CINEMAS_TABLE_EXT_ID);
 			
 			result.moveToFirst();
 			while (!result.isAfterLast()) {
 				String cinema_id = Integer.toString(result.getInt(result.getColumnIndex(CINEMAS_TABLE_EXT_ID)));
 				Cursor result_rows = db.query(AFISHA_TABLE, 
 						new String[] {
 						AFISHA_TABLE_EXT_ID
 						}, AFISHA_TABLE_CINEMA_ID + " = ?",
 						new String[] {cinema_id}, null, null, 
 						null);
 				if (result_rows.getCount() == 0){
 					db.delete(CINEMAS_TABLE, CINEMAS_TABLE_EXT_ID + " = ?", new String[] {cinema_id});
 				}
 				result_rows.close();
 				result.moveToNext();
 			}
 			result.close();
 			
 			db.setTransactionSuccessful();
 		} finally {
 			db.endTransaction();
 		}
 		db.close();
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL("CREATE TABLE " + AFISHA_TABLE + 
 				" (" + 
 				AFISHA_TABLE_EXT_ID + " INTEGER PRIMARY KEY, " + 
 				AFISHA_TABLE_CINEMA_ID + " INTEGER, " + 
 				AFISHA_TABLE_THEATER_ID + " INTEGER, " + 
 				AFISHA_TABLE_ZAL + " TEXT, " + 
 				AFISHA_TABLE_DATA_BEGIN + " DATE, " + 
 				AFISHA_TABLE_DATA_END + " DATE, " + 
 				AFISHA_TABLE_TIMES + " TEXT, " + 
 				AFISHA_TABLE_PRICES + " TEXT" + 
 				");");
 		
 		db.execSQL("CREATE TABLE " + CINEMAS_TABLE + 
 				" (" + 
 				CINEMAS_TABLE_EXT_ID + " INTEGER PRIMARY KEY, " + 
 				CINEMAS_TABLE_TITLE + " TEXT, " + 
 				CINEMAS_TABLE_OR_TITLE + " TEXT, " + 
 				CINEMAS_TABLE_YEAR + " INTEGER, " + 
 				CINEMAS_TABLE_POSTER + " TEXT, " + 
 				CINEMAS_TABLE_DESCRIPTION + " TEXT, " +
 				CINEMAS_TABLE_POSTER_IMAGE + " BLOB" + 
 				");");
 		
 		db.execSQL("CREATE TABLE " + THEATERS_TABLE + 
 				" (" + 
 				THEATERS_TABLE_EXT_ID + " INTEGER PRIMARY KEY, " + 
 				THEATERS_TABLE_CITY_ID + " INTEGER, " + 
 				THEATERS_TABLE_TITLE + " TEXT, " + 
 				THEATERS_TABLE_LINK + " TEXT, " + 
 				THEATERS_TABLE_ADDRESS + " TEXT, " + 
 				THEATERS_TABLE_PHONE + " TEXT, " +  
 				THEATERS_TABLE_FILTER + " INTEGER DEFAULT 0" + 
 				");");
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		android.util.Log.w("DatabaseHelper", "Upgrading database, which will destroy all old data");
 		db.execSQL("DROP TABLE IF EXISTS " + AFISHA_TABLE);
 		db.execSQL("DROP TABLE IF EXISTS " + CINEMAS_TABLE);
 		db.execSQL("DROP TABLE IF EXISTS " + THEATERS_TABLE);
 		onCreate(db);
 	}
 	
 	public void clearAllTables(){
 		SQLiteDatabase db = this.getWritableDatabase();
 		db.execSQL("DELETE FROM " + AFISHA_TABLE);
 		db.execSQL("DELETE FROM " + CINEMAS_TABLE);
 		db.execSQL("DELETE FROM " + THEATERS_TABLE);
 		db.close();
 	}
 	
 }
