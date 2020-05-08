 package com.zybnet.abc.utils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.reflect.Field;
 import java.sql.Time;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 import com.zybnet.abc.R;
 import com.zybnet.abc.model.Model;
 import com.zybnet.abc.model.Slot;
 import com.zybnet.abc.model.Subject;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 
 	protected static final int VERSION = 1;
 	public static final String FILENAME = "abc.db";
 	
 	private Context context;
 	
 	/*
 	 * Returns the default database. For now, it's an in-memory db
 	 * so data is not persistent.
 	 * 
 	 */
 	public DatabaseHelper(Context ctx) {
 		this(ctx, FILENAME, null, VERSION);
 	}
 	
 	public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
 		super(context, name, factory, version);
 		this.context = context;
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {	
 		try {
 			runScript(db, R.raw.schema);
 		} catch (IOException e) {
 			L.og("Script failed");
 			L.og(e);
 		}
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		throw new UnsupportedOperationException();
 	}
 	
 	// IMPORTANT! Statements must be separated by and empty line
 	// The last statement must be terminated by a newline, too
 	// so likely the fill will end with a newline
 	protected void runScript(SQLiteDatabase db, int resourceId) throws IOException {
 		BufferedReader in = new BufferedReader(new InputStreamReader(
 				context.getResources().openRawResource(resourceId)));
 		
 		StringBuilder builder = new StringBuilder();
 		String line = null;
 		
 		while ((line = in.readLine() )!= null) {
 			if (line.trim().length() == 0 && builder.length() > 2 && builder.charAt(builder.length() - 2) == ';') {
 				try {
 					db.execSQL(builder.toString());
 				} catch (SQLException e) {
 					L.og(e);
 					L.og(builder.toString());
 				}
 				
 				builder = new StringBuilder();
 				continue;
 			}
 			builder.append(line);
 			builder.append('\n');
 		}
 		
 	}
 	
 	/*
 	 * Returns the Slot from the database. If no slot exists
 	 * returns Slot.template, but still this is not saved
 	 * in the database
 	 */
 	public Slot getSlot(int day, int ord) {
 		String sql = "" +
 				"SELECT slot.* , " +
 				"  subject.name AS subject_name, subject._id AS subject_id " +
 				"FROM slot LEFT JOIN subject ON slot.subject_id = subject._id " +
 				"WHERE slot.day = ? AND slot.ord = ? ";
 		String[] args = {Integer.toString(day), Integer.toString(ord)};
 		Cursor c = getReadableDatabase().rawQuery(sql, args);
 		
 		if (c.getCount() != 1) {
 			Slot slot = new Slot();
 			slot.day = day;
 			slot.ord = ord;
 			return slot;
 		}
 		
 		c.moveToFirst();
 		
 		Slot slot = new Slot();
 		slot._id = _i(c, "_id");
 		slot.day = _i(c, "day");
 		slot.ord = _i(c, "ord");
 		slot.display_text = _s(c, "display_text");
 		slot.teacher = _s(c, "teacher");
 		slot.place = _s(c, "place");
 		slot.start = _time(c, "start");
 		slot.end = _time(c, "end");
 		slot.subject_name = _s(c, "subject_name");
 		slot.subject_id = _i(c, "subject_id");
 		c.close();
 		return slot;
 	}
 	
 	public String _s(Cursor c, String column) {
 		return c.getString(c.getColumnIndex(column));
 	}
 	
 	public int _i(Cursor c, String column) {
 		return c.getInt(c.getColumnIndex(column));
 	}
 
 	public long _l(Cursor c, String column) {
 		return c.getLong(c.getColumnIndex(column));
 	}
 	
 	public java.sql.Date _date(Cursor c, String column) {
 		Date date = dateFromFormat(c, column, U.SQL_DATE_FORMAT);
 		
 		if (date == null)
 			return null;
 		
		return new java.sql.Date(date.getTime());
 	}
 	
 	// May return null
 	private java.util.Date dateFromFormat(Cursor c, String column, String format) {
 		SimpleDateFormat sdf = new SimpleDateFormat(format);
 		String str = c.getString(c.getColumnIndex(column));
 		
 		if (str == null)
 			return null;
 		
 		try {
 			return sdf.parse(str);
 		} catch (ParseException e) {
 			Log.e(L.TAG, "Cannot parse date", e);
 			return new java.util.Date();
 		}
 	}
 	
 	public Time _time(Cursor c, String column) {
 		Date date = dateFromFormat(c, column, U.SQL_TIME_FORMAT);
 		
 		if (date == null)
 			return null;
 		
 		return new Time(date.getHours(), date.getMinutes(), 0);
 	}
 	
 	public Cursor getSubjects() {
 		return getReadableDatabase().query("subject",
 				new String[] {"_id", "name", "name_short"},
 				null, null, null, null, null);
 	}
 	
 	// Selection contains a single element with the subject id
 	public Cursor getHomework(long subject) {
 		return getReadableDatabase().query("homework",
 				new String[] {"_id", "description", "due"},
 				"subject_id = ?", new String[] {Long.toString(subject)},
 				null, null, null);
 	}
 	
 	// Selection contains a single element with the subject id
 	public Cursor getGrades(long subject) {
 		return getReadableDatabase().query("grade",
 				new String[] {"_id", "date", "description", "score"},
 				"subject_id = ?", new String[] {Long.toString(subject)},
 				null, null, null);
 	}
 	
 	public Subject getSubject(long id) {
 		Cursor c = getReadableDatabase().query("subject",
 				new String[] {"name", "name_short", "default_place"},
 				"_id = ?", new String[] {Long.toString(id)}, null, null, null);
 		
 		Subject s = new Subject();
 		
 		if (c.getCount() != 1)
 			return s;
 		
 		c.moveToFirst();
 		s._id = id;
 		s.name = _s(c, "name");
 		s.name_short = _s(c, "name_short");
 		s.default_place = _s(c, "default_place");
 		
 		return s;
 	}
 	
 	/*
 	 * This is a "magic" method that tries to fill the passed
 	 * model by reading fields from the database.
 	 * 
 	 * It assumes a number of conventions:
 	 *  - the table name is the model name to lower case
 	 *  - 
 	 *  
 	 * This method does not fill fields marked @Extern
 	 */
 	public <T extends Model> T fill(Class<T> token, long id) {
 		String table = token.getSimpleName();
 		T instance = null;
 		try {
 			instance = token.getDeclaredConstructor().newInstance();
 		} catch (Exception e) {
 			throw new RuntimeException(token.getSimpleName() + "Doesn't have a default constructor");
 		}
 		List<String> columns = new LinkedList<String>();
 		List<Field> fields = instance.getPublicFields(false);
 		for (Field field: fields) {
 			columns.add(field.getName());
 		}
 		
 		String[] cols = new String[columns.size()];
 		
 		Cursor c = getReadableDatabase().query(table, columns.toArray(cols),
 				"_id = ?", new String[] {Long.toString(id)}, null, null, null);
 		
 		if (c.getCount() != 1) {
 			return instance;
 		}
 		
 		c.moveToFirst();
 		
 		for (Field field: fields) {
 			try {
 				String key = field.getName();
 				Class<?> type = field.getType();
 				if (type.equals(String.class)){
 					field.set(instance, _s(c, key));
 				} else if (type.equals(long.class)) {
 					field.set(instance, _l(c, key));
 				} else if (type.equals(java.sql.Date.class)) {
 					field.set(instance, _date(c, key));
 				} else if (type.equals(java.sql.Time.class)) {
 					field.set(instance, _time(c, key));
 				} else {
 					throw new IllegalArgumentException(
 							String.format("Can't bind %s.%s of type %s",
 							token.getSimpleName(),
 							field.getName(),
 							type.getCanonicalName())
 					);
 				}
 			} catch (Exception e) {
 				throw new RuntimeException(e);
 			}
 		}
 		
 		return instance;
 	}
 }
