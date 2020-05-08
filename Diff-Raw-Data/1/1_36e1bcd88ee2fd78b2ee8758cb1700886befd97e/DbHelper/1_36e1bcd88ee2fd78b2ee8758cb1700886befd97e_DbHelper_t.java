 package com.suresh.whereismycash;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.widget.TextView;
 
 public class DbHelper extends SQLiteOpenHelper {
 	
 	private static final String TAG = "DbHelper";
 	private Context context;
 	
 	public enum PaymentType { GET, PAY }
 	
     /**
      * Database values and creation statement
      */
     private static final String DATABASE_NAME = "data";
     private static final String DATABASE_TABLE_LOANS = "loans";
     private static final String DATABASE_TABLE_NAMES = "names";
     private static final int DATABASE_VERSION = 4;
     private static final String DATABASE_CREATE_LOANS =
 	    "create table if not exists loans (_id integer primary key autoincrement, "
 	    + "name varchar(100) not null, amount float not null, note text, date bigint not null)";
     private static final String DATABASE_CREATE_NAMES = 
     	"create table if not exists names (_id integer primary key autoincrement, " +
     	"name varchar(100) not null)";
     
     /**
      * SQL Column Keys
      */
     public static final String KEY_ID = "_id";
     public static final String KEY_NAME = "name";
     public static final String KEY_AMOUNT = "amount";
     public static final String KEY_NOTE = "note";
     public static final String KEY_DATE = "date";
 
 	public DbHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		this.context = context;
 	}
 	
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(DATABASE_CREATE_LOANS);
 		db.execSQL(DATABASE_CREATE_NAMES);
 	}
 
 	/**
 	 * Upgrading from version 3 --> 4
 	 */
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                 + newVersion);
 		
 		db.execSQL("ALTER TABLE loans ADD COLUMN date bigint not null default " + Calendar.getInstance().getTimeInMillis());
 //		ContentValues values = new ContentValues();
 //		values.put(KEY_DATE, Calendar.getInstance().getTimeInMillis());
 //		db.update(DATABASE_CREATE_LOANS, values, null, null);
 	}
 	
 	public Cursor getAllLoans() {
 		SQLiteDatabase db = getWritableDatabase();
 		String[] columns = {KEY_ID, KEY_NAME, "SUM(" + KEY_AMOUNT + ") as amount"};
 		Cursor c = db.query(DATABASE_TABLE_LOANS, columns, null, null, KEY_NAME, null, null);
 		return c;
 	}
 	
 	public Cursor getMatchingNames(String input) {
 		SQLiteDatabase db = getWritableDatabase();
 		String query = "SELECT " + KEY_NAME + ", _id FROM " + DATABASE_TABLE_NAMES +
 				" WHERE " + KEY_NAME + " LIKE ? GROUP BY " + KEY_NAME;
 		Cursor c = db.rawQuery(query, new String[]{input + "%"});
 		return c;
 	}
 	
 	public boolean addEntry(PaymentType type, float amount,
 			String name, String note, long dateMillis) {
 		SQLiteDatabase db = getWritableDatabase();
 		db.beginTransaction();
 		try {
 			ContentValues val = new ContentValues();
 			float storedAmount = (float) 0.0;
 			switch(type) {
 			case GET:
 				storedAmount = -1 * amount;
 				break;
 			case PAY:
 				storedAmount = amount;
 				break;
 			}
 			val.put(KEY_AMOUNT, storedAmount);
 			
 			val.put(KEY_NAME, name);
 			addName(db, name);
 			if (note != null && !note.isEmpty()) val.put(KEY_NOTE, note);
 			
 			val.put(KEY_DATE, dateMillis);
 			
 			db.insert(DATABASE_TABLE_LOANS, null, val);
 			db.setTransactionSuccessful();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		} finally {
 			db.endTransaction();
 			db.close();
 		}
 		
 		return true;
 	}
 	
 	public boolean updateEntry(int id, PaymentType type, float amount, String note, long dateMillis) {
 		SQLiteDatabase db = getWritableDatabase();
 		ContentValues val = new ContentValues();
 		float storedAmount = (float) 0.0;
 		switch(type) {
 		case GET:
 			storedAmount = -1 * amount;
 			break;
 		case PAY:
 			storedAmount = amount;
 			break;
 		}
 		val.put(KEY_AMOUNT, storedAmount);
 		
 		if (note != null && !note.isEmpty()) {
 			val.put(KEY_NOTE, note);
 		} else {
 			val.putNull(KEY_NOTE);
 		}
 		
 		val.put(KEY_DATE, dateMillis);
 		
 		db.update(DATABASE_TABLE_LOANS, val, KEY_ID + " = ?", new String[] {String.valueOf(id)});
 		db.close();
 		return true;
 	}
 	
 	public void delete(String name) {
 		SQLiteDatabase db = getWritableDatabase();
 		db.delete(DATABASE_TABLE_LOANS, KEY_NAME + " = ?", new String[]{name});
 	}
 	
 	public void delete(int id) {
 		SQLiteDatabase db = getWritableDatabase();
 		db.delete(DATABASE_TABLE_LOANS, KEY_ID + " = ?", new String[]{String.valueOf(id)});
 	}
 	
 	public float getLoanAmountByName(String name) {
 		float amount = 0f;
 		SQLiteDatabase db = getWritableDatabase();
 		String[] columns = {KEY_ID, "SUM(" + KEY_AMOUNT + ") as amount"};
 		Cursor c = db.query(DATABASE_TABLE_LOANS, columns, KEY_NAME + " = ?", new String[]{name}, KEY_NAME, null, null);
 		if (c.moveToFirst()) {
 			amount = c.getFloat(c.getColumnIndex(KEY_AMOUNT));
 			amount = (float) (Math.round(amount*100.0)/100.0);
 		}
 		
 		return amount;
 	}
 	
 	public float getSumByType(PaymentType type) {
 		float amount = 0f;
 		String sign = "";
 		switch (type) {
 		case GET:
 			sign = " < 0";
 			break;
 		case PAY:
 			sign = " > 0";
 			break;
 		}
 		SQLiteDatabase db = getWritableDatabase();
 		String[] columns = {KEY_ID, "SUM(" + KEY_AMOUNT + ") as amount"};
 		Cursor c = db.query(DATABASE_TABLE_LOANS, columns, KEY_AMOUNT + sign, null, null, null, null);
 		if (c.moveToFirst()) {
 			amount = c.getFloat(c.getColumnIndex(KEY_AMOUNT));
 			amount = (float) (Math.round(amount*100.0)/100.0);
 			if (amount < 0) amount *= -1;
 		}
 		
 		return amount;
 	}
 	
 	public float getNetSum() {
 		float getSum = getSumByType(PaymentType.GET);
 		float paySum = getSumByType(PaymentType.PAY);
 		float netSum = paySum - getSum;
 		netSum = (float) (Math.round(netSum*100.0)/100.0);
 		return netSum;
 	}
 	
 	public Cursor getLoansByName(String name) {
 		SQLiteDatabase db = getWritableDatabase();
 		String[] columns = {KEY_ID, KEY_AMOUNT, KEY_NOTE, KEY_DATE};
 		Cursor c = db.query(DATABASE_TABLE_LOANS, columns,  KEY_NAME + " = ?", new String[]{name},
 				null, null, KEY_DATE);
 		return c;
 	}
 	
 	public ArrayList<HashMap<String, Object>> getLoansByNameAsList(String name) {
 		Cursor c = getLoansByName(name);
 		ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String,Object>>();
 		
 		while (c.moveToNext()) {
 			HashMap<String, Object> map = new HashMap<String, Object>();
 			map.put("viewType", NewEditAdapter.TYPE_ITEM);
 			map.put(KEY_ID, c.getInt(c.getColumnIndex(DbHelper.KEY_ID)));
 			map.put(KEY_AMOUNT, c.getFloat(c.getColumnIndex(DbHelper.KEY_AMOUNT)));
 			map.put(KEY_NOTE, c.getString(c.getColumnIndex(DbHelper.KEY_NOTE)));
 			map.put(KEY_DATE, c.getString(c.getColumnIndex(DbHelper.KEY_DATE)));
 			
 			list.add(map);
 		}
 		
 		return list;
 	}
 	
 	public ArrayList<HashMap<String, Object>> getLoansByNameForDisplay(String name) {
 		ArrayList<HashMap<String, Object>> list = getLoansByNameAsList(name);
 
 		String prevFormat = "";
 		for (int i = 0; i < list.size(); i++) {
 			HashMap<String, Object> item = list.get(i);
 			String monthFormat = (String) DateFormat.format(
 					context.getString(R.string.section_header_format),
 					Long.parseLong((String) item.get(KEY_DATE)));
 			monthFormat = monthFormat.toUpperCase();
 			
 			if (!monthFormat.equals(prevFormat)) {
 				HashMap<String, Object> sectionItem = new HashMap<String, Object>();
 				sectionItem.put("viewType", NewEditAdapter.TYPE_SEPARATOR);
 				sectionItem.put("header", monthFormat);
 				list.add(i, sectionItem);
 				prevFormat = monthFormat;
 				i++;
 			}
 		}
 		return list;
 	}
 	
 	public void addName(SQLiteDatabase db, String name) {
 		if (checkNameExists(name)) {
 			return;
 		}
 		
 		ContentValues val = new ContentValues();
 		val.put(KEY_NAME, name);
 		db.insert(DATABASE_TABLE_NAMES, null, val);
 	}
 	
 	public boolean checkNameExists(String name) {
 		SQLiteDatabase db = getReadableDatabase();
 		String[] columns = {KEY_ID, KEY_NAME};
 		Cursor c = db.query(DATABASE_TABLE_NAMES, columns, null, null, KEY_NAME, null, null);
 		
 		while (c.moveToNext()) {
 			String rowName = c.getString(c.getColumnIndex(KEY_NAME));
 			if (rowName.equals(name)) return true;
 		}
 		return false;
 	}
 	
 	public static void setTextandColor(Context context, TextView tv, float amount) {
 		int color = 0;
 		String tag = null;
 		if (amount < 0) {
 			amount *= -1;
 			color = R.color.amount_green;
 			tag = PaymentType.GET.name();
 		} else if (amount == 0) {
 			color = R.color.amount_blue;
			tag = PaymentType.GET.name();
 		} else if (amount > 0) {
 			color = R.color.amount_red;
 			tag = PaymentType.PAY.name();
 		}
 		
 		tv.setText(String.valueOf(amount));
 		tv.setTextColor(context.getResources().getColor(color));
 		tv.setTag(tag);
 	}
 }
