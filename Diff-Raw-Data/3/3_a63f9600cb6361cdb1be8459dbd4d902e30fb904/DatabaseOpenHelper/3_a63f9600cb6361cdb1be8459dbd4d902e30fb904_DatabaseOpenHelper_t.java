 package it.chalmers.mufasa.android_budget_app.controller.database;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseOpenHelper extends SQLiteOpenHelper{
 
 	public DatabaseOpenHelper(Context context) {
 		super(context, "StudentBudget", null, 1);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL("CREATE TABLE settings ( currentAccountId INTEGER);");
 		db.execSQL("CREATE TABLE accounts ( id INTEGER PRIMARY KEY, name TEXT, balance FLOAT NOT NULL);");
 		db.execSQL("CREATE TABLE transactions ( id INTEGER PRIMARY KEY, name TEXT, value FLOAT NOT NULL, date DATE NOT NULL, accountId INTEGER, categoryId INTEGER);");
 		db.execSQL("CREATE TABLE categories ( id INTEGER PRIMARY KEY, name TEXT, parentId INTEGER);");
 		db.execSQL("CREATE TABLE budgetitems ( id INTEGER PRIMARY KEY, categoryId INTEGER, value FLOAT NOT NULL);");
 
 		this.insertDefaultValues(db);
 	}
 	
 	private void insertDefaultValues(SQLiteDatabase db) {
 		//Assumes this is an empty database
 		
 		db.execSQL("INSERT INTO accounts (id,name,balance) VALUES (1,\"My Account\",0)");
 		db.execSQL("INSERT INTO settings (currentAccountId) VALUES (1)");
 		
 		//Values from average student between 18-30 years in sweden. Data from konsumentverket.se
 		db.execSQL("INSERT INTO categories (name) VALUES (\"Inkomster\")"); //id = 1
 		db.execSQL("INSERT INTO categories (name) VALUES (\"Utgifter\")"); //id = 2
 		
 		//Income
 		db.execSQL("INSERT INTO categories (name,parentId) VALUES (\"Studiemedel\",1)");
 		db.execSQL("INSERT INTO budgetitems (categoryId,value) VALUES (3,8920)");
 		
 		//Expenses
 		db.execSQL("INSERT INTO categories (name,parentId) VALUES (\"Mat\",2)");
 		db.execSQL("INSERT INTO budgetitems (categoryId,value) VALUES (4,8920)");
 		
 		db.execSQL("INSERT INTO categories (name,parentId) VALUES (\"Hygien\",2)");
 		db.execSQL("INSERT INTO budgetitems (categoryId,value) VALUES (5,370)");
 
 		db.execSQL("INSERT INTO categories (name,parentId) VALUES (\"Klder\",2)");
 		db.execSQL("INSERT INTO budgetitems (categoryId,value) VALUES (6,600)");
 		
 		db.execSQL("INSERT INTO categories (name,parentId) VALUES (\"Fritid\",2)");
 		db.execSQL("INSERT INTO budgetitems (categoryId,value) VALUES (7,630)");
 		
 		db.execSQL("INSERT INTO categories (name,parentId) VALUES (\"Mobiltelefon\",2)");
 		db.execSQL("INSERT INTO budgetitems (categoryId,value) VALUES (8,180)");
 		
 		db.execSQL("INSERT INTO categories (name,parentId) VALUES (\"Frbrukningsvaror\",2)");
 		db.execSQL("INSERT INTO budgetitems (categoryId,value) VALUES (9,100)");
 		
 		db.execSQL("INSERT INTO categories (name,parentId) VALUES (\"Medier\",2)");
 		db.execSQL("INSERT INTO budgetitems (categoryId,value) VALUES (10,930)");
 		
 		db.execSQL("INSERT INTO categories (name,parentId) VALUES (\"Hemfrskring\",2)");
 		db.execSQL("INSERT INTO budgetitems (categoryId,value) VALUES (11,100)");
 		
 		db.execSQL("INSERT INTO categories (name,parentId) VALUES (\"Hyra\",2)");
 		db.execSQL("INSERT INTO budgetitems (categoryId,value) VALUES (12,3200)");
 		
 		db.execSQL("INSERT INTO categories (name,parentId) VALUES (\"El\",2)");
 		db.execSQL("INSERT INTO budgetitems (categoryId,value) VALUES (13,250)");
 		
 		db.execSQL("INSERT INTO categories (name,parentId) VALUES (\"Kursliteratur\",2)");
 		db.execSQL("INSERT INTO budgetitems (categoryId,value) VALUES (14,750)");
 		
 		db.execSQL("INSERT INTO categories (name,parentId) VALUES (\"Resor\",2)");
 		db.execSQL("INSERT INTO budgetitems (categoryId,value) VALUES (15,500)");
 		
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		
 	}
 
 }
