 package cornell.eickleapp;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Locale;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseHandler extends SQLiteOpenHelper {
 	private static final int DATABASE_VERSION = 1;
 	private static final String DATABASE_NAME = "QuestionDB";
 
 	private static final String TABLE_MULTI = "multichoice";
 	private static final String TABLE_QUES = "questions";
 	
 	// Multi Choice column names
 	private static final String MULTI_KEY_QUES_ID = "question_id";
 	private static final String MULTI_KEY_VALUE = "value";
 
 	// Question column names
 	private static final String QUES_KEY_VAR = "variable";
 	private static final String QUES_KEY_DAY = "day";
 	private static final String QUES_KEY_MONTH = "month";
 	private static final String QUES_KEY_YEAR = "year";
 	private static final String QUES_KEY_DAY_WEEK = "day_week";
 	private static final String QUES_KEY_TIME = "time";
 	private static final String QUES_KEY_TYPE = "type";
 	private static final String QUES_KEY_VALUE = "value";
 
 	public DatabaseHandler(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		// create Multi Question Table
 		String CREATE_MULTI = "CREATE TABLE multichoice ("
 				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, "
 				+ "question_id INTEGER, " + "value TEXT ); ";
 		// create the mutli-answer table
 		db.execSQL(CREATE_MULTI);
 
 		// create Question Table
 		String CREATE_QUES = "CREATE TABLE questions ("
 				+ "id INTEGER PRIMARY KEY AUTOINCREMENT," + "variable TEXT, "
 				+ "month INTEGER, " + "day INTEGER, " + "year INTEGER, "
 				+ "day_week TEXT, " + "time TEXT, " + "type TEXT, "
 				+ "value TEXT );";
 
 		// create the question table
 		db.execSQL(CREATE_QUES);
 	}
 
 	//Will update the value if it already exists in the db for the 
 	//current day. Otherwise it will add the value to the db
 	public void updateOrAdd(String variable, Integer int_value){
 		
 		if (!variableExist(variable)){
 			addValue(variable, int_value);
 		}else{
 			updateValue(variable, int_value);
 		}
 	}
 	
 	//Will update the value if it already exists in the db for the 
 	//current day. Otherwise it will add the value to the db
 	public void updateOrAdd(String variable, String str_value){
 		if (!variableExist(variable)){
 			addValue(variable, str_value);
 		}else{
 			updateValue(variable, str_value);
 		}
 	}
 	
 	// Adds an integer value to the database
 	public void addValue(String variable, Integer int_value) {
 		Date date = new Date();
 		DatabaseStore ds = DatabaseStore.DatabaseIntegerStore(variable,
 				int_value.toString(), date);
 		addQuestion(ds);
 	}
 
 	// Adds an integer value to the database
 	public void addValue(String variable, String str_value) {
 		Date date = new Date();
 		DatabaseStore ds = DatabaseStore.DatabaseTextStore(variable,
 				str_value.toString(), date);
 		addQuestion(ds);
 	}
 	
 	public void addValueTomorrow(String variable, String str_value) {
 		Date date = new Date();
 		//Add a day to our date
 		GregorianCalendar gc = new GregorianCalendar();
 		gc.setTime(date);
 		gc.add(Calendar.HOUR_OF_DAY, -6);
 		gc.add(Calendar.DAY_OF_YEAR, 1);
 		date = gc.getTime();
 		DatabaseStore ds = DatabaseStore.DatabaseTextStore(variable,
 				str_value.toString(), date);
 		addQuestion(ds);
 	}
 
 	public void addDelayValue(String variable, String str_value) {
 		Date date = new Date();
 		//Add a day to our date
 		GregorianCalendar gc = new GregorianCalendar();
 		gc.setTime(date);
 		gc.add(Calendar.HOUR_OF_DAY, -6);
 		date = gc.getTime();
 		DatabaseStore ds = DatabaseStore.DatabaseTextStore(variable,
 				str_value.toString(), date);
 		addQuestion(ds);
 	}
 	
 	public void addValueYesterday(String variable, String str_val){
 		Date date = new Date();
 		//Add a day to our date
 		GregorianCalendar gc = new GregorianCalendar();
 		gc.setTime(date);
 		gc.add(Calendar.DAY_OF_YEAR, -1);
 		date = gc.getTime();
 		DatabaseStore ds = DatabaseStore.DatabaseTextStore(variable,
 				str_val.toString(), date);
 		addQuestion(ds);
 	}
 	
 	public void addDelayValue(String variable, int int_value) {
 		Date date = new Date();
 		//Add a day to our date
 		GregorianCalendar gc = new GregorianCalendar();
 		gc.setTime(date);
 		gc.add(Calendar.HOUR_OF_DAY, -6);
 		date = gc.getTime();
 		DatabaseStore ds = DatabaseStore.DatabaseIntegerStore(variable,
 				String.valueOf(int_value), date);
 		addQuestion(ds);
 	}	
 	// Adds an integer value to the database
 	public void addValue(String variable, List<String> list_values) {
 		Date date = new Date();
 		DatabaseStore ds = DatabaseStore.DatabaseMultichoiceStore(variable,
 				"multichoice", date);
 		long pid = addQuestion(ds);
 		for (int i = 0; i < list_values.size(); i++) {
 			addMulti((int) pid, list_values.get(i));
 		}
 	}
 
 	// Updates an integer value in the database
 	public void updateValue(String variable, Integer int_value) {
 		Date date = new Date();
 		DatabaseStore ds = DatabaseStore.DatabaseIntegerStore(variable,
 				int_value.toString(), date);
 		updateQuestion(ds);
 	}
 
 	// updates all values that have the variable and current
 	// date to the value supplied.
 	public void updateValue(String variable, String str_value) {
 		Date date = new Date();
 		DatabaseStore ds = DatabaseStore.DatabaseTextStore(variable,
 				str_value.toString(), date);
 		updateQuestion(ds);
 	}
 
 	/*
 	 * Inserts a row into the table mutlichoice and returns the pid.
 	 */
 	public long addMulti(Integer qid, String value) {
 		SQLiteDatabase db = this.getWritableDatabase();
 
 		ContentValues values = new ContentValues();
 		values.put(MULTI_KEY_QUES_ID, qid);
 		values.put(MULTI_KEY_VALUE, value);
 
 		long pid = db.insert(TABLE_MULTI, null, values);
 		db.close();
 		return pid;
 	}
 
 	public void updateQuestion(DatabaseStore store) {
 		// get reference to the database
 		SQLiteDatabase db = this.getWritableDatabase();
 		// create Question Table
 		String update_sql = "UPDATE questions SET " + QUES_KEY_VALUE + "='"
 				+ store.value + "' WHERE " + QUES_KEY_VAR + "='"
 				+ store.variable + "' AND " + QUES_KEY_MONTH + "="
 				+ store.month + " AND " + QUES_KEY_YEAR + "=" + store.year
 				+ " AND " + QUES_KEY_DAY + "=" + store.day + " AND "
 				+ QUES_KEY_TYPE + "='" + store.type + "';";
 
 		// create the question table
 		db.execSQL(update_sql);
 		// close the database
 		db.close();
 	}
 
 	public void clearAllValuesDay(String variable){
 		SQLiteDatabase db = this.getWritableDatabase();
 		Date date = new Date();
 		DatabaseStore ds = DatabaseStore.DatabaseTextStore(variable,
 				"", date);		
 		// Delete the variable for today from the DB
 		String delete_sql = "DELETE FROM " +  TABLE_QUES +
 				" WHERE " + QUES_KEY_VAR + "='" + ds.variable +
 				"' AND " + QUES_KEY_YEAR + "=" +  ds.year +
 				" AND " + QUES_KEY_MONTH + "=" + ds.month +
 				" AND " + QUES_KEY_DAY + "=" + ds.day + ";";
 		// execute the SQL
 		db.execSQL(delete_sql);
 		// close the database
 		db.close();
 	}
 	/*
 	 * Inserts a row into the table questions and returns the pid.
 	 */
 	public long addQuestion(DatabaseStore store) {
 		// get reference to the database
 		SQLiteDatabase db = this.getWritableDatabase();
 
 		// create the ContentValues to hold column values
 		ContentValues values = new ContentValues();
 		values.put(QUES_KEY_DAY, store.day);
 		values.put(QUES_KEY_MONTH, store.month);
 		values.put(QUES_KEY_YEAR, store.year);
 		values.put(QUES_KEY_TIME, store.time);
 		values.put(QUES_KEY_DAY_WEEK, store.day_week);
 		values.put(QUES_KEY_VALUE, store.value);
 		values.put(QUES_KEY_TYPE, store.type);
 		values.put(QUES_KEY_VAR, store.variable);
 		// insert the values into the table
 		long pid = db.insert(TABLE_QUES, null, values);
 
 		// close the database
 		db.close();
 		return pid;
 	}
 	
 	private List<DatabaseStore> handleCursor(Cursor cursor) {
 		// check to see if our query returned values
 		try {
 			DatabaseStore store = null;
 			ArrayList<DatabaseStore> store_list = new ArrayList<DatabaseStore>();
 			if (cursor.moveToFirst()) {
 				do {
 					String variable = cursor.getString(1);
 					Integer month = cursor.getInt(2);
 					Integer day = cursor.getInt(3);
 					Integer year = cursor.getInt(4);
 					String time = cursor.getString(6);
 					String type = cursor.getString(7);
 					String value = cursor.getString(8);
 					Date date = DatabaseStore.GetDate(month, day, year, time);
 					store = DatabaseStore.FromDatabase(variable, value, date,
 							type);
 					store_list.add(store);
 				} while (cursor.moveToNext());
 			} else {
 				cursor.close();
 				return null;
 			}
 			cursor.close();
 			return store_list;
 		} catch (ParseException pe) {
 			System.out.println("Cannot parse string.");
 			cursor.close();
 			return null;
 		}
 	}
 
 	public List<DatabaseStore> dataDump() {
 		SQLiteDatabase db = this.getWritableDatabase();
 		String query = "SELECT * FROM "+ TABLE_QUES +";";
 		Cursor cursor = db.rawQuery(query, null);
 		return handleCursor(cursor);
 	}
 	
 	public List<DatabaseStore> getAllVarValue(String variable) {
 		// Get reference to the database
 		SQLiteDatabase db = this.getWritableDatabase();
 		String query = "Select * FROM " + TABLE_QUES + " WHERE " + QUES_KEY_VAR
 				+ "='" + variable + "'; ";
 		Cursor cursor = db.rawQuery(query, null);
 		return handleCursor(cursor);
 	}
 
 	public List<DatabaseStore> getVarValuesForDay(String variable,
 			Integer month, Integer day, Integer year) {
 		// get reference to the database
 		SQLiteDatabase db = this.getWritableDatabase();
 		String query = "SELECT * FROM " + TABLE_QUES + " WHERE " + QUES_KEY_VAR
 				+ "='" + variable + "' AND " + QUES_KEY_DAY + "=" + day
 				+ " AND " + QUES_KEY_MONTH + "=" + month + " AND "
 				+ QUES_KEY_YEAR + "=" + year;
 		Cursor cursor = db.rawQuery(query, null);
 		if (cursor.getCount() == 0) {
 			return null;
 		} else {
 			return handleCursor(cursor);
 		}
 	}
 	
 	/*
 	 * Must sort both color and values by time before calling.
 	 */
 	private ArrayList<DatabaseStore> getDaysList(ArrayList<DatabaseStore> data){
 		data = DatabaseStore.sortByTime(data);
 		ArrayList<DatabaseStore> day_data = new ArrayList<DatabaseStore>();
 		
 		DatabaseStore max_day=null;
 		for(int i=0; i< data.size(); i++){
 			DatabaseStore s = data.get(i);
 			if(max_day == null){
 				max_day = s;
 			}else{
				if((max_day.day < s.day) || (s.month > max_day.month) || (s.year > max_day.year)){
 					day_data.add(max_day);
 					max_day = s;
 				} else if(Double.valueOf(max_day.value)< Double.valueOf(s.value)){
 					max_day = s;
 				}
 			}
 		}
 		day_data.add(max_day);
 		return day_data;
 	}
 	
 	public ArrayList<DatabaseStore> getDrinkCountForAllDays(){
 		SQLiteDatabase db = this.getWritableDatabase();
 		String query = "SELECT * FROM " + TABLE_QUES + " WHERE " + QUES_KEY_VAR
 				+ "='drink_count'";
 		Cursor cursor = db.rawQuery(query, null);
 		if (cursor.getCount() == 0) {
 			return null;
 		} else {
 			ArrayList<DatabaseStore> data = (ArrayList<DatabaseStore>)handleCursor(cursor);
 			if(data!=null){
 				return getDaysList(data);
 			}else{
 				return null;
 			}
 		}
 	}
 	
 	public List<DatabaseStore> getVarValuesForWeek(String variable, Date date){		
 		SimpleDateFormat year_fmt = new SimpleDateFormat("yyyy", Locale.US);
 		SimpleDateFormat month_fmt = new SimpleDateFormat("MM", Locale.US);
 		SimpleDateFormat day_fmt = new SimpleDateFormat("dd", Locale.US);
 		SimpleDateFormat day_week_ft = new SimpleDateFormat("E", Locale.US);
 
 		int year = Integer.parseInt(year_fmt.format(date));
 		int month = Integer.parseInt(month_fmt.format(date));
 		int day = Integer.parseInt(day_fmt.format(date));
 		String week_day = day_week_ft.format(date);
 		
 		Integer week_val=null;
 		if (week_day.equals("Sun")){
 			week_val = 0;
 		} else if(week_day.equals("Mon")){
 			week_val = 1;
 		} else if(week_day.equals("Tue")){
 			week_val = 2;
 		} else if(week_day.equals("Wed")){
 			week_val = 3;
 		} else if(week_day.equals("Thu")){
 			week_val = 4;
 		} else if(week_day.equals("Fri")){
 			week_val = 5;
 		} else if(week_day.equals("Sat")){
 			week_val = 6;
 		}
 		return getVarValuesForWeek(variable, month, day, year,week_val);
 	}
 	
 	public List<DatabaseStore> getVarValuesForWeek(String variable,
 			Integer month, Integer day, Integer year, Integer week_day) {
 		// get reference to the database
 		SQLiteDatabase db = this.getWritableDatabase();
 		String day_str = QUES_KEY_VAR + "='" + variable + "' AND ( (" + QUES_KEY_DAY +"="+ day + " AND " +
 				QUES_KEY_MONTH + "="  + month + " AND " + 
 				QUES_KEY_YEAR + "=" + year + ")";	
 		
 		SimpleDateFormat year_fmt = new SimpleDateFormat("yyyy", Locale.US);
 		SimpleDateFormat month_fmt = new SimpleDateFormat("MM", Locale.US);
 		SimpleDateFormat day_fmt = new SimpleDateFormat("dd", Locale.US);
 		
 		for (int i=0; i<7; i++){	
 			Integer diff = null;
 			GregorianCalendar cal = new GregorianCalendar(year, month-1, day);
 			if(week_day < i){
 				diff = i-week_day;
 				cal.add(Calendar.DAY_OF_YEAR, diff);
 				Date date = cal.getTime();
 				int y = Integer.parseInt(year_fmt.format(date));
 				int m = Integer.parseInt(month_fmt.format(date));
 				int d = Integer.parseInt(day_fmt.format(date));
 				day_str = day_str + " OR (" + QUES_KEY_DAY + "=" + d + 
 						" AND " + QUES_KEY_MONTH + "=" + m + " AND " +
 						QUES_KEY_YEAR + "=" + y + ")";
 			}else if(week_day>i){
 				diff = week_day-i;
 				cal.add(Calendar.DAY_OF_YEAR, -1 * diff);
 				Date date = cal.getTime();
 				int y = Integer.parseInt(year_fmt.format(date));
 				int m = Integer.parseInt(month_fmt.format(date));
 				int d = Integer.parseInt(day_fmt.format(date));
 				day_str = day_str + " OR (" + QUES_KEY_DAY + "=" + d + 
 						" AND " + QUES_KEY_MONTH + "=" + m + " AND " +
 						QUES_KEY_YEAR + "=" + y +")";
 			}
 		}
 		day_str += ")";
 		String query = "SELECT * FROM " + TABLE_QUES + " WHERE " + day_str;
 		Cursor cursor = db.rawQuery(query, null);
 		if (cursor.getCount() == 0) {
 			return null;
 		} else {
 			return handleCursor(cursor);
 		}
 	}
 	
 	public List<DatabaseStore> getVarValuesForYesterday(String variable, Date date) {
 		//subtract a day from our date
 		GregorianCalendar gc = new GregorianCalendar();
 		gc.setTime(date);
 		gc.add(Calendar.DAY_OF_YEAR, -1);
 		date = gc.getTime();
 		
 		SimpleDateFormat year_fmt = new SimpleDateFormat("yyyy", Locale.US);
 		SimpleDateFormat month_fmt = new SimpleDateFormat("MM", Locale.US);
 		SimpleDateFormat day_fmt = new SimpleDateFormat("dd", Locale.US);
 
 		int year = Integer.parseInt(year_fmt.format(date));
 		int month = Integer.parseInt(month_fmt.format(date));
 		int day = Integer.parseInt(day_fmt.format(date));
 
 		return getVarValuesForDay(variable, month, day, year);
 	}
 	
 	public List<DatabaseStore> getVarValuesDelay(String variable, Date date) {
 		//subtract a day from our date
 		GregorianCalendar gc = new GregorianCalendar();
 		gc.setTime(date);
 		gc.add(Calendar.HOUR_OF_DAY, -6);
 		date = gc.getTime();
 		
 		SimpleDateFormat year_fmt = new SimpleDateFormat("yyyy", Locale.US);
 		SimpleDateFormat month_fmt = new SimpleDateFormat("MM", Locale.US);
 		SimpleDateFormat day_fmt = new SimpleDateFormat("dd", Locale.US);
 
 		int year = Integer.parseInt(year_fmt.format(date));
 		int month = Integer.parseInt(month_fmt.format(date));
 		int day = Integer.parseInt(day_fmt.format(date));
 
 		return getVarValuesForDay(variable, month, day, year);
 	}
 	public List<DatabaseStore> getVarValuesForDay(String variable, Date date) {
 		SimpleDateFormat year_fmt = new SimpleDateFormat("yyyy", Locale.US);
 		SimpleDateFormat month_fmt = new SimpleDateFormat("MM", Locale.US);
 		SimpleDateFormat day_fmt = new SimpleDateFormat("dd", Locale.US);
 
 		int year = Integer.parseInt(year_fmt.format(date));
 		int month = Integer.parseInt(month_fmt.format(date));
 		int day = Integer.parseInt(day_fmt.format(date));
 
 		return getVarValuesForDay(variable, month, day, year);
 	}
 
 	public List<DatabaseStore> getVarValuesForMonth(String variable,
 			Integer month, Integer year) {
 		// get reference to the database
 		SQLiteDatabase db = this.getWritableDatabase();
 		String query = "Select * FROM " + TABLE_QUES + " WHERE " + QUES_KEY_VAR
 				+ "='" + variable + "' AND " + QUES_KEY_MONTH + "=" + month
 				+ " AND " + QUES_KEY_YEAR + "=" + year + ";";
 		Cursor cursor = db.rawQuery(query, null);
 		return handleCursor(cursor);
 	}
 
 	public ArrayList<String[]> handleWordle(Cursor cursor){
 	// check to see if our query returned values
 			ArrayList<String []> total_list = new ArrayList<String []>();
 			if (cursor.moveToFirst()) {
 				do {
 					String value = cursor.getString(0);
 					String cnt = String.valueOf(cursor.getInt(1));
 					String[] store_list = {value, cnt};
 					total_list.add(store_list);
 				} while (cursor.moveToNext());
 			} else {
 				cursor.close();
 				return null;
 			}
 			cursor.close();
 			return total_list;
 		
 	}
 	
 	public void deleteValuesTomorrow(ArrayList<String> names){
 		Date date = new Date();
 		//Add a day to our date
 		GregorianCalendar gc = new GregorianCalendar();
 		gc.setTime(date);
 		gc.add(Calendar.HOUR_OF_DAY, -6);
 		gc.add(Calendar.DAY_OF_YEAR, 1);
 		date = gc.getTime();
 		DatabaseStore dbstore = DatabaseStore.DatabaseTextStore("",
 				"", date);
 		SQLiteDatabase db = this.getWritableDatabase();
 		String variable_str = "(" + QUES_KEY_VAR + "='" +names.get(0);
 		for(int i=1; i<names.size(); i++){
 			variable_str += "' OR " + QUES_KEY_VAR +"='" + names.get(i);
 		}
 		variable_str += "')";
 		String query = "DELETE FROM " + TABLE_QUES + " WHERE " +
 				QUES_KEY_MONTH +"=" + dbstore.month + " AND "+
 				QUES_KEY_DAY + "=" + dbstore.day + " AND "+
 				QUES_KEY_YEAR + "=" + dbstore.year + " AND " +
 				variable_str + ";";
 		db.execSQL(query);
 		db.close();
 	}
 	
 	public void deleteVaribles(ArrayList<String> names, DatabaseStore dbstore){
 		SQLiteDatabase db = this.getWritableDatabase();
 		String variable_str = "(" + QUES_KEY_VAR + "='" +names.get(0);
 		for(int i=1; i<names.size(); i++){
 			variable_str += "' OR " + QUES_KEY_VAR +"='" + names.get(i);
 		}
 		variable_str += "')";
 		String query = "DELETE FROM " + TABLE_QUES + " WHERE " +
 				QUES_KEY_MONTH +"=" + dbstore.month + " AND "+
 				QUES_KEY_DAY + "=" + dbstore.day + " AND "+
 				QUES_KEY_YEAR + "=" + dbstore.year + " AND " +
 				QUES_KEY_TIME + "='" + dbstore.time + "' AND " +
 				variable_str + ";";
 		db.execSQL(query);
 		db.close();
 	}
 	
 	
 	public ArrayList<String []> getWordleDrink(){
 		SQLiteDatabase db = this.getWritableDatabase();
 		String query = "Select a.value, COUNT(a.value) FROM " + TABLE_QUES + " a INNER JOIN " + TABLE_QUES +
 				" b ON a." + QUES_KEY_MONTH +"=b." + QUES_KEY_MONTH + 
 				" AND a." + QUES_KEY_DAY  + "=b." + QUES_KEY_DAY +
 				" AND a." + QUES_KEY_YEAR + "=b." + QUES_KEY_YEAR +
 				" WHERE a.variable='wordle' AND b.variable='drank_last_night'" +
 				" AND b.value='True' GROUP BY a.value;";
 		Cursor cursor = db.rawQuery(query, null);
 		return handleWordle(cursor);	
 	}
 	
 	public ArrayList<String []> getWordleNoDrink(){
 		SQLiteDatabase db = this.getWritableDatabase();
 		String query = "Select c.value, COUNT(c.value) FROM " + TABLE_QUES + " c WHERE c.variable=\"wordle\" " +
 				"AND c.id NOT IN " +
 				"(Select a.id FROM " + TABLE_QUES + " a INNER JOIN " + TABLE_QUES +
 				" b ON a." + QUES_KEY_MONTH +"=b." + QUES_KEY_MONTH + 
 				" AND a." + QUES_KEY_DAY  + "=b." + QUES_KEY_DAY +
 				" AND a." + QUES_KEY_YEAR + "=b." + QUES_KEY_YEAR +
 				" WHERE a.variable='wordle' AND b.variable='drank_last_night'" +
 				" AND b.value='True') GROUP BY c.value;";
 		
 		Cursor cursor = db.rawQuery(query, null);
 		return handleWordle(cursor);
 	}
 	
 	
 	
 	public List<DatabaseStore> getVarValuesForMonth(String variable, Date date) {
 		SimpleDateFormat year_fmt = new SimpleDateFormat("yyyy", Locale.US);
 		SimpleDateFormat month_fmt = new SimpleDateFormat("MM", Locale.US);
 
 		int year = Integer.parseInt(year_fmt.format(date));
 		int month = Integer.parseInt(month_fmt.format(date));
 
 		return getVarValuesForMonth(variable, month, year);
 	}
 	
 	public Boolean variableExist(String variable){
 		Date date = new Date();
 		ArrayList<DatabaseStore> exist = (ArrayList<DatabaseStore>)getVarValuesForDay(variable, date);
 		
 		if (exist!=null)
 			return true;
 		else
 			return false;
 	}
 	
 
 	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
