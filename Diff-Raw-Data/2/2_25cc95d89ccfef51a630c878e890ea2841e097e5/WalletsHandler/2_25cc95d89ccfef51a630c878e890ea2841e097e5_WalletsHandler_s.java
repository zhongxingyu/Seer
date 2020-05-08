 package com.paymium.instawallet.database;
 
 import java.math.BigDecimal;
 import java.util.LinkedList;
 
 import com.paymium.instawallet.wallet.Wallet;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class WalletsHandler 
 {
 	// All Static variables
 	// Database Version
 	private static final int DATABASE_VERSION = 2;
 
 	// Database Name
 	private static final String DATABASE_NAME = "WalletsManager";
 
 	// Table name
 	private static final String TABLE_WALLETS = "Wallets";
 	
 	// Table Columns names
 	private static final String KEY_ID = "id";
 	private static final String KEY_ADDRESS = "address";
 	private static final String KEY_BALANCE = "balance";
 	
 	private static final String TAG = "DBAdapter";
 	
 	private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_WALLETS + "(" + KEY_ID + " TEXT PRIMARY KEY,"								 
 																						+ KEY_ADDRESS + " TEXT NOT NULL,"
 																						+ KEY_BALANCE + " TEXT NOT NULL"
 																						+ ")";	
 	private DatabaseHelper DBHelper;
     private SQLiteDatabase db;
 	
 	public WalletsHandler(Context context) 
 	{
 		this.DBHelper = new DatabaseHelper(context);
 	}
 
 	private static class DatabaseHelper extends SQLiteOpenHelper
 	{
 		DatabaseHelper(Context context)
         {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
         }
 		
 		// Creating Tables
 		@Override
         public void onCreate(SQLiteDatabase db)
         {
             try 
             {
                 db.execSQL(DATABASE_CREATE);
             } 
             catch (SQLException e) 
             {
                 e.printStackTrace();
             }
         }
 		
 		// Upgrading database
 		@Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
         {
             Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
             			+ newVersion + ", which will destroy all old data");
             
         	truncate(db);
         }
 		
 		// Truncate table's content
 		public void truncate(SQLiteDatabase db)
 		{ 
 			// Drop older table if existed
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_WALLETS);
 
 			// Create tables again
 			onCreate(db);
 
 		}
 	}
 	
 	//---opens the database---
     public WalletsHandler open() throws SQLException 
     {
         db = DBHelper.getWritableDatabase();
         return this;
     }
 
     //---closes the database---
     public void close() 
     {
         DBHelper.close();
     }
     
     //---insert a wallet into the database---
    public void insertBTCAddress(Wallet wallet) 
     {
 		//System.out.println("PREPARING FOR ADDING A WALLET!!");
 		this.open();
 		
 		ContentValues value = new ContentValues();
 
 		value.put(KEY_ID, wallet.getWallet_id());
 		value.put(KEY_ADDRESS, wallet.getWallet_address());
 		value.put(KEY_BALANCE, wallet.getWallet_balance().toString());
 		
 		db.insert(TABLE_WALLETS, null, value);
 		
 		this.close();
 		
 		//System.out.println("ADDING A WALLET IS DONE !!");
 		
     }
     
 	// Getting a single wallet
 	public Wallet getWallet(String id) 
 	{
 		this.open();
 	 
 	    Cursor cursor = db.query(TABLE_WALLETS, new String[] { KEY_ID, KEY_ADDRESS}, KEY_ID + "=?",
 	            								new String[] { id }, null, null, null, null);
 	    if (cursor != null)
 	        cursor.moveToFirst();
 	 
 	    Wallet wallet = new Wallet();
 		
 		wallet.setWallet_id(cursor.getString(0));
 		wallet.setWallet_address(cursor.getString(1));
 		wallet.setWallet_balance(new BigDecimal(cursor.getString(2)));
 	    
 	    cursor.close();
 	    
 	    
 	    this.close();
 	    
 	    // return wallet
 	    return wallet;
 	}
     
 	// Getting all wallets
 	public LinkedList<Wallet> getAllWallets() 
 	{
 		LinkedList<Wallet> walletsList = new LinkedList<Wallet>();
 		
 		// Select All Query
 		String selectQuery = "SELECT * FROM " + TABLE_WALLETS;
 
 		this.open();
 		
 		Cursor cursor = db.rawQuery(selectQuery, null);
 
 		// looping through all rows and adding to list
 		if (cursor.moveToFirst()) 
 		{
 			do 
 			{
 				Wallet wallet = new Wallet();
 				
 				wallet.setWallet_id(cursor.getString(0));
 				wallet.setWallet_address(cursor.getString(1));
 				wallet.setWallet_balance(new BigDecimal(cursor.getString(2)));
 				
 				
 				walletsList.add(wallet);
 				
 			} 
 			while (cursor.moveToNext());
 		}
 		
 		cursor.close();
 		
 		this.close();
 		
 		// return wallets list
 		
 		return walletsList;
 	}
 	
 	
     // Updating a wallet
 	public void updateWallet(Wallet wallet) 
 	{ 
 		this.open();
 			
 	    ContentValues value = new ContentValues();
 	    
 	    value.put(KEY_ID, wallet.getWallet_id());
 		value.put(KEY_ADDRESS, wallet.getWallet_address());
 		value.put(KEY_BALANCE, wallet.getWallet_balance().toString());
 	 
 	    // updating row
 	    db.update(TABLE_WALLETS, value, KEY_ID + " = ?", new String[] { wallet.getWallet_id() });
 	    
 	    this.close();
 	}
 	
 	
     // Deleting a wallet
 	public void deleteWallet(Wallet wallet) 
 	{
 		this.open();
 		
 	    db.delete(TABLE_WALLETS, KEY_ID + " = ?", new String[] { wallet.getWallet_id() });
 	    
 	    this.close();
 	}
     
 }
