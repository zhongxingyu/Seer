 package com.runninghusky.spacetracker;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 import android.provider.ContactsContract;
 import android.util.Log;
 
 public class DataHelper {
 
 	private static final String DATABASE_NAME = "spacetracker.db";
 	private static final int DATABASE_VERSION = 1;
 	private static final String TABLE_FLIGHTS = "flights";
 	private static final String TABLE_FLIGHT_DETAILS = "details";
 	private static final String TABLE_SMS_HISTORY = "smshistory";
 
 	private Context context;
 	private SQLiteDatabase db;
 
 	private SQLiteStatement insertFlights, insertDetails, insertSms;
 
 	private static final String INSERT_FLIGHTS = "insert into " + TABLE_FLIGHTS
 			+ "(name, takePic, sendSms, duration, smsNumber, picDuration) "
 			+ "values (?, ?, ?, ?, ?, ?)";
 	private static final String INSERT_FLIGHT_DETAILS = "insert into "
 			+ TABLE_FLIGHT_DETAILS
 			+ "(flightId, longitude, latitude, elevation, speed, time, accuracy, bearing, provider, distance) "
 			+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 	private static final String INSERT_SMS_HISTORY = "insert into "
 			+ TABLE_SMS_HISTORY
 			+ "(flightId, number, message, time, statusMessage) "
 			+ "values (?, ?, ?, ?, ?)";
 
 	public DataHelper(Context context) {
 		this.context = context;
 		OpenHelper openHelper = new OpenHelper(this.context);
 		this.db = openHelper.getWritableDatabase();
 		this.insertFlights = this.db.compileStatement(INSERT_FLIGHTS);
 		this.insertDetails = this.db.compileStatement(INSERT_FLIGHT_DETAILS);
 		this.insertSms = this.db.compileStatement(INSERT_SMS_HISTORY);
 	}
 
 	public long insertFlights(String name, Boolean takePic, Boolean sendSms,
 			long duration, String smsNumber, Long picDuration) {
 		// this.insertFlights.bindLong(0, id);
 		this.insertFlights.bindString(1, name);
 		this.insertFlights.bindString(2, String.valueOf(takePic));
 		this.insertFlights.bindString(3, String.valueOf(sendSms));
 		this.insertFlights.bindLong(4, duration);
 		this.insertFlights.bindString(5, smsNumber);
 		this.insertFlights.bindLong(6, picDuration);
 
 		return this.insertFlights.executeInsert();
 	}
 
 	public long insertDetails(long flightId, float longitude, float latitude,
 			float elevation, float speed, long time, float accuracy,
 			float bearing, String provider, float distance) {
 		this.insertDetails.bindLong(1, flightId);
 		this.insertDetails.bindDouble(2, longitude);
 		this.insertDetails.bindDouble(3, latitude);
 		this.insertDetails.bindDouble(4, elevation);
 		this.insertDetails.bindDouble(5, speed);
 		this.insertDetails.bindLong(6, time);
 		this.insertDetails.bindDouble(7, accuracy);
 		this.insertDetails.bindDouble(8, bearing);
 		this.insertDetails.bindString(9, provider);
 		this.insertDetails.bindDouble(10, distance);
 
 		return this.insertDetails.executeInsert();
 	}
 
 	public long insertSms(long flightId, String number, String message,
 			long time, String statusMessage) {
 		this.insertSms.bindLong(1, flightId);
 		this.insertSms.bindString(2, number);
 		this.insertSms.bindString(3, message);
 		this.insertSms.bindLong(4, time);
 		this.insertSms.bindString(5, statusMessage);
 
 		return this.insertSms.executeInsert();
 	}
 
 	public void updateFlights(String name, Boolean takePic, Boolean sendSms,
 			long duration, String smsNumber, Long picDuration, Long id) {
 		ContentValues vals = new ContentValues();
 		vals.put("name", name);
 		vals.put("takePic", String.valueOf(takePic));
 		vals.put("sendSms", String.valueOf(sendSms));
 		vals.put("duration", duration);
 		vals.put("smsNumber", smsNumber);
 		vals.put("picDuration", picDuration);
 		this.db.update(TABLE_FLIGHTS, vals, "id=" + id, null);
 	}
 
 	// public String[] getContacts() {
 	// String[] str = { "unknown" };
 	// Cursor cursor = getContentResolver().query(
 	// ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
 	// while (cursor.moveToNext()) {
 	// String contactId = cursor.getString(cursor
 	// .getColumnIndex(ContactsContract.Contacts._ID));
 	// String hasPhone = cursor
 	// .getString(cursor
 	// .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
 	// if (Boolean.parseBoolean(hasPhone)) {
 	// List<String> strList = null;
 	// // You know it has a number so now query it like this
 	// Cursor phones = getContentResolver().query(
 	// ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
 	// null,
 	// ContactsContract.CommonDataKinds.Phone.CONTACT_ID
 	// + " = " + contactId, null, null);
 	// while (phones.moveToNext()) {
 	// strList
 	// .add(phones
 	// .getString(phones
 	// .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
 	// }
 	// if (!strList.isEmpty()) {
 	// str = strList.toArray(new String[strList.size()]);
 	// }
 	// phones.close();
 	// }
 	// cursor.close();
 	// }
 	// return str;
 	// }
 
 	public void deleteFlights() {
 		this.db.delete(TABLE_FLIGHTS, null, null);
 	}
 
 	public void deleteSms() {
 		this.db.delete(TABLE_SMS_HISTORY, null, null);
 	}
 
 	public void deleteFlightDetails() {
 		this.db.delete(TABLE_FLIGHT_DETAILS, null, null);
 	}
 
 	public void deleteSingleFlight(long flightId) {
 		this.db.delete(TABLE_FLIGHTS, "id = " + flightId, null);
 		this.db.delete(TABLE_FLIGHT_DETAILS, "flightId = " + flightId, null);
 		this.db.delete(TABLE_SMS_HISTORY, "flightId = " + flightId, null);
 	}
 
 	public void resetFlight(long flightId) {
 		this.db.delete(TABLE_FLIGHT_DETAILS, "flightId = " + flightId, null);
 		this.db.delete(TABLE_SMS_HISTORY, "flightId = " + flightId, null);
 	}
 
 	// ---closes the database---
 	public void close() {
 		this.db.close();
 	}
 
 	public Boolean flightsExist() {
 		Boolean b = false;
 		Cursor cursor = this.db.rawQuery("SELECT COUNT(*) FROM "
 				+ TABLE_FLIGHTS, null);
 		if (cursor.moveToFirst()) {
 			do {
 				if (Integer.parseInt(cursor.getString(0)) >= 1) {
 					b = true;
 				} else {
 					b = false;
 				}
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 
 		return b;
 	}
 
 	public List<Flight> selectFlightHistory() {
 		List<Flight> list = new ArrayList<Flight>();
 		Cursor cursor = this.db
 				.rawQuery("SELECT * FROM " + TABLE_FLIGHTS, null);
 		if (cursor.moveToFirst()) {
 			do {
 				Flight f = new Flight();
 				f.setId(cursor.getLong(0));
 				f.setName(cursor.getString(1));
 				if (cursor.getString(2).equalsIgnoreCase("true")) {
 					f.setTakePic(true);
 				} else {
 					f.setTakePic(false);
 				}
 				if (cursor.getString(3).equalsIgnoreCase("true")) {
 					f.setSendSms(true);
 				} else {
 					f.setSendSms(false);
 				}
 				f.setSmsDuration(cursor.getString(4));
 				f.setSmsNumber(cursor.getString(5));
 				f.setPicDuration(cursor.getString(6));
 				list.add(f);
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 
 		for (Flight f : list) {
 			cursor = this.db.rawQuery("SELECT distance FROM "
 					+ TABLE_FLIGHT_DETAILS + " where flightId = "
 					+ String.valueOf(f.getId()) + " order by distance asc",
 					null);
 			if (cursor.moveToFirst()) {
 				do {
 					f.setDistance(String
 							.valueOf(HlprUtil.roundTwoDecimals(cursor
 									.getFloat(0) * 0.000621371192)));
					if (f.getDistance() == null){
						f.setDistance("0");
					}
 				} while (cursor.moveToNext());
 			}
 			if (cursor != null && !cursor.isClosed()) {
 				cursor.close();
 			}
 		}
 
 		return list;
 	}
 
 	public Flight selectFlightHistoryById(Long id) {
 		Flight f = new Flight();
 		Cursor cursor = this.db.rawQuery("SELECT * FROM " + TABLE_FLIGHTS
 				+ " where id = " + id, null);
 		if (cursor.moveToFirst()) {
 			do {
 				f.setId(cursor.getLong(0));
 				f.setName(cursor.getString(1));
 				if (cursor.getString(2).equalsIgnoreCase("true")) {
 					f.setTakePic(true);
 				} else {
 					f.setTakePic(false);
 				}
 				if (cursor.getString(3).equalsIgnoreCase("true")) {
 					f.setSendSms(true);
 				} else {
 					f.setSendSms(false);
 				}
 				f.setSmsDuration(cursor.getString(4));
 				f.setSmsNumber(cursor.getString(5));
 				f.setPicDuration(cursor.getString(6));
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 		return f;
 	}
 
 	public List<FlightData> selectFlightData(long flightId) {
 		List<FlightData> list = new ArrayList<FlightData>();
 		Cursor cursor = this.db.rawQuery("SELECT * FROM "
 				+ TABLE_FLIGHT_DETAILS + " where flightId = " + flightId, null);
 		if (cursor.moveToFirst()) {
 			do {
 				FlightData f = new FlightData();
 				f.setId(cursor.getLong(0));
 				f.setFlightId(cursor.getLong(1));
 				f.setLongitude(cursor.getFloat(2));
 				f.setLatitude(cursor.getFloat(3));
 				f.setElevation(cursor.getFloat(4));
 				f.setSpeed(cursor.getFloat(5));
 				f.setTime(cursor.getLong(6));
 				f.setAccuracy(cursor.getFloat(7));
 				f.setBearing(cursor.getFloat(8));
 				f.setProvider(cursor.getString(9));
 				f.setDistance(cursor.getFloat(10));
 				list.add(f);
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 		return list;
 	}
 
 	public float getLastDistance(long flightId) {
 		float distance = 0;
 		Cursor cursor = this.db.rawQuery("SELECT distance FROM "
 				+ TABLE_FLIGHT_DETAILS + " where flightId = " + flightId
 				+ " order by time desc limit 1", null);
 		if (cursor.moveToFirst()) {
 			do {
 				distance = cursor.getLong(0);
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 		return distance;
 	}
 
 	public List<SmsData> selectSmsData(long flightId) {
 		List<SmsData> list = new ArrayList<SmsData>();
 		Cursor cursor = this.db.rawQuery("SELECT * FROM " + TABLE_SMS_HISTORY
 				+ " where flightId = " + flightId, null);
 		if (cursor.moveToFirst()) {
 			do {
 				SmsData f = new SmsData();
 				f.setId(cursor.getLong(0));
 				f.setFlightId(cursor.getLong(1));
 				f.setNumber(cursor.getString(2));
 				f.setMessage(cursor.getString(3));
 				f.setTime(cursor.getLong(4));
 				f.setStatusMessage(cursor.getString(5));
 				list.add(f);
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 		return list;
 	}
 
 	// public void saveDay(long planNumber, long weekNumber, String dayName,
 	// float distance, String description, String pace) {
 	// ContentValues vals = new ContentValues();
 	// vals.put(dayName + "Distance", distance);
 	// vals.put(dayName + "Description", description);
 	// vals.put(dayName + "Pace", pace);
 	// this.db.update(TABLE_PLANS, vals, "planNumber=" + planNumber
 	// + " AND weekNumber =" + weekNumber, null);
 	// }
 	//
 	// public int setDayCompletion(long planNumber, long weekNumber,
 	// String dayName, String completed) {
 	// ContentValues vals = new ContentValues();
 	// vals.put(dayName, completed);
 	// return this.db.update(TABLE_PLANS, vals, "planNumber=" + planNumber
 	// + " AND weekNumber =" + weekNumber, null);
 	// }
 	//
 	// public int setWeeksCompletion(long planNumber, long weekNumber,
 	// String completed) {
 	// ContentValues vals = new ContentValues();
 	// vals.put("mondayComplete", completed);
 	// vals.put("tuesdayComplete", completed);
 	// vals.put("wednesdayComplete", completed);
 	// vals.put("thursdayComplete", completed);
 	// vals.put("fridayComplete", completed);
 	// vals.put("saturdayComplete", completed);
 	// vals.put("sundayComplete", completed);
 	// return this.db.update(TABLE_PLANS, vals, "planNumber=" + planNumber
 	// + " AND weekNumber =" + weekNumber, null);
 	//
 	// }
 
 	private static class OpenHelper extends SQLiteOpenHelper {
 
 		OpenHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		private static final String INSERT_FLIGHTS = "insert into "
 				+ TABLE_FLIGHTS
 				+ "(id, name, takePic, sendSms, duration, smsNumber, picDuration) "
 				+ "values (?, ?, ?, ?, ?, ?)";
 		private static final String INSERT_FLIGHT_DETAILS = "insert into "
 				+ TABLE_FLIGHT_DETAILS
 				+ "(id, flightId, longitude, latitude, elevation, speed, time, accuracy, bearing, provider, distance) "
 				+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 		private static final String INSERT_SMS_HISTORY = "insert into "
 				+ TABLE_SMS_HISTORY
 				+ "(id, flightId, number, message, time, statusMessage) "
 				+ "values (?, ?, ?, ?, ?, ?)";
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db
 					.execSQL("CREATE TABLE "
 							+ TABLE_FLIGHTS
 							+ "(id INTEGER PRIMARY KEY, name TEXT, takePic TEXT, sendSMS TEXT, duration TEXT, smsNumber TEXT, picDuration TEXT) ");
 			db
 					.execSQL("CREATE TABLE "
 							+ TABLE_FLIGHT_DETAILS
 							+ "(id INTEGER PRIMARY KEY, flightId INTEGER, longitude FLOAT, latitude FLOAT, "
 							+ "elevation FLOAT, speed FLOAT, time FLOAT, accuracy FLOAT, bearing FLOAT, provider TEXT, distance FLOAT) ");
 			db
 					.execSQL("CREATE TABLE "
 							+ TABLE_SMS_HISTORY
 							+ "(id INTEGER PRIMARY KEY, flightId INTEGER, number TEXT, message TEXT, "
 							+ "time FLOAT, statusMessage TEXT) ");
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w("Example",
 					"Upgrading database, this will drop tables and recreate.");
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_FLIGHT_DETAILS);
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_FLIGHTS);
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_SMS_HISTORY);
 			// db.execSQL("ALTER TABLE "
 			// + TABLE_PLANS
 			// + " ADD dayOneComplete TEXT DEFAULT 'Not Complete' NOT NULL");
 			// db.execSQL("ALTER TABLE "
 			// + TABLE_PLANS
 			// + " ADD dayTwoComplete TEXT DEFAULT 'Not Complete' NOT NULL");
 			// db.execSQL("ALTER TABLE "
 			// + TABLE_PLANS
 			// + " ADD dayThreeComplete TEXT DEFAULT 'Not Complete' NOT NULL");
 			onCreate(db);
 		}
 	}
 }
