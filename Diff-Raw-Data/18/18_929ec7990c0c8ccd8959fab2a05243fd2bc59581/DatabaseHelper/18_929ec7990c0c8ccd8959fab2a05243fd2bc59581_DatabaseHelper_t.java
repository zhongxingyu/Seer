 package com.thorgaming.throwme.billing;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 /**
 * Creates and updates if necessary the database used to store transactions locally
  * 
  * @author Thomas Cheyney
  * @version 1.4
  */
 public class DatabaseHelper extends SQLiteOpenHelper {
 	public DatabaseHelper(Context context) {
 		super(context, "throwme.purchase.db", null, 1);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		createPurchaseTable(db);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
 	}
 
 	/**
 	 * Creates the two tables used to store data
 	 * 
 	 * throwmeHistory stores a list of all transactions made
 	 * throwmePurchased stores items purchased and the quantity owned by the user
 	 * 
 	 * @param db SQLiteDatabase object
 	 */
 	private void createPurchaseTable(SQLiteDatabase db) {
 		db.execSQL("CREATE TABLE throwmeHistory (_id TEXT PRIMARY KEY, state INTEGER, productId TEXT, developerPayload TEXT, purchaseTime INTEGER)");
 		db.execSQL("CREATE TABLE throwmePurchased (_id TEXT PRIMARY KEY, quantity INTEGER)");
 	}
 }
