 package com.hackathon;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseHandler extends SQLiteOpenHelper{
 	/* This will be the Change*/
 	private static final int DATABASE_VERSION = 1;
 	private static final String DATABASE_NAME = "FoodManager";
 	
 	private static final String TABLE_FOODS = "foods";
 	
 	private static final String KEY_ID = "id";
 	private static final String KEY_NAME = "name";
 	private static final String KEY_PRICE = "price";
 	private static final String KEY_PLACE = "place";
 	private static final String KEY_PHONENUMBER = "phone_number";
 	private static final String KEY_TIME = "time";
 	private static final String KEY_QUANTITY = "quantity";
 	private static final String KEY_EXPLANATION = "explanation";
 	private static final String KEY_IMAGE = "image";
 
 	public DatabaseHandler(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		// TODO Auto-generated method stub
 		String CREATE_FOODS_TABLE = "CREATE TABLE "+ TABLE_FOODS + "("
 				+ KEY_ID + " INTEGER PRIMARY KEY," 
 				+ KEY_NAME + " TEXT NOT NULL,"
 				+ KEY_PRICE + " INTEGER"
 				+ KEY_PLACE + " TEXT,"
 				+ KEY_PHONENUMBER + " TEXT,"
 				+ KEY_TIME + " TEXT,"
 				+ KEY_QUANTITY + " TEXT,"
 				+ KEY_EXPLANATION + " TEXT,"
 				+ KEY_IMAGE + " TEXT,"
 				+ ")";
 		db.execSQL(CREATE_FOODS_TABLE);
 		
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// TODO Auto-generated method stub
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOODS);
 		
 		onCreate(db);
 		
 	}
 	
 	public void addFoods(Food food){
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		values.put(KEY_NAME, food.getName());
 		values.put(KEY_PRICE, food.getPrice());
 		
 		db.insert(TABLE_FOODS, null, values);
 		db.close();
 	}
 	
 	public Food getFood(int id){
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor cursor = db.query(TABLE_FOODS, 
 				new String[]{KEY_ID, KEY_NAME, KEY_PRICE, KEY_PLACE, KEY_PHONENUMBER,
 				KEY_TIME, KEY_QUANTITY, KEY_EXPLANATION, KEY_IMAGE}, 
 				KEY_ID+"=?", new String[]{String.valueOf(id)},null,null,null,null);
 		if(cursor != null)
 			cursor.moveToFirst();
 		Food food = new Food(Integer.parseInt(cursor.getString(0)),
 				cursor.getString(1),
 				Integer.parseInt(cursor.getString(2)),
 				cursor.getString(3),
 				cursor.getString(4),
 				cursor.getString(5),
 				cursor.getString(6),
 				cursor.getString(7),
 				cursor.getString(8));
 		
 		return food;
 	}
 	
 	public List<Food> getAllFoods(){
 		List<Food> foodList = new ArrayList<Food>();
 		String selectQuery = "SELECT * FROM " + TABLE_FOODS;
 		
 		SQLiteDatabase db = this.getWritableDatabase();
 		Cursor cursor = db.rawQuery(selectQuery, null);
 		
 		if(cursor.moveToFirst()){
 			do{
				Food food = new Food(Integer.parseInt(cursor.getString(0)),
						cursor.getString(1),
						Integer.parseInt(cursor.getString(2)),
						cursor.getString(3),
						cursor.getString(4),
						cursor.getString(5),
						cursor.getString(6),
						cursor.getString(7),
						cursor.getString(8));
 			}while(cursor.moveToNext());
 		}
 		
 		return foodList;
 	}
 
 }
