 package budgetapp.util;
 /**
  * The database containing all the tables used by the application
  * 
  * @author Steen
  * 
  */
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import android.widget.Toast;
 
 public class BudgetDatabase extends SQLiteOpenHelper{
 
 	// The table for cash flow
 	// Basically a log for the transactions
 	public static final String TABLE_CASHFLOW = "cashflow";
 	public static final String COLUMN_ID = "_id";
 	public static final String COLUMN_VALUE = "value";
 	public static final String COLUMN_DATE = "date";
 	public static final String COLUMN_CATEGORY = "category";
 	public static final String COLUMN_FLAGS = "flags";
 	
 	//The table with the different names for categories, used by the Spinner to select categories
 	public static final String TABLE_CATEGORY_NAMES = "categorynames";
 	// The table for transactions for cataegories
 	// Keeps track of total number of transactions of a category
 	// and the total money spent on a category
 	public static final String TABLE_CATEGORIES = "categories";
 	//COLUMN_ID
 	//COLUMN_CATEGORY
 	public static final String COLUMN_NUM = "num"; // Number of times this has been bought
 	public static final String COLUMN_TOTAL = "total"; // Total sum of money spent on this category
 	
 	// The table for cash flow in a day
 	public static final String TABLE_DAYSUM = "daysum";
 	
 	// Total som for a day
 	public static final String TABLE_DAYTOTAL = "daytotal";
 	
 	//COLUMN_ID
 	//COLUMN_DATE
 	//COLUMN_TOTAL
 
 	private static final String DATABASE_NAME = "budget.db";
 	private static final int DATABASE_VERSION = 7;
 	
 	private static final String DATABASE_CREATE_TABLE_CATEGORY_NAMES = "create table "
 			+ TABLE_CATEGORY_NAMES + "(" + COLUMN_ID
 			+ " integer primary key autoincrement, " + COLUMN_CATEGORY
 			+ " text);";
 	private static final String DATABASE_CREATE_TABLE_CASHFLOW = "create table "
 			+ TABLE_CASHFLOW + "(" + COLUMN_ID
 			+ " integer primary key autoincrement, " +COLUMN_VALUE +
 			" integer, " + COLUMN_DATE + " text, " + COLUMN_CATEGORY + " text, " + COLUMN_FLAGS + " integer);";
 	
 	private static final String DATABASE_CREATE_TABLE_CATEGORIES = "create table "
 			+ TABLE_CATEGORIES + "(" + COLUMN_ID
 			+ " integer primary key autoincrement, " + COLUMN_CATEGORY +
 			" text, "+ COLUMN_NUM + " integer not null, " + COLUMN_TOTAL + " long integer not null, " + COLUMN_FLAGS + " integer);";
 	
 	private static final String DATABASE_CREATE_TABLE_DAYSUM = "create table "
 			+ TABLE_DAYSUM + "(" + COLUMN_ID
 			+ " integer primary key autoincrement, " + COLUMN_DATE
 			+ " text, " + COLUMN_TOTAL + " long integer not null, " + COLUMN_FLAGS + " integer);";
 	private static final String DATABASE_CREATE_TABLE_DAYTOTAL = "create table "
 			+ TABLE_DAYTOTAL + "(" + COLUMN_ID
 			+ " integer primary key autoincrement, " + COLUMN_DATE
 			+ " text, " + COLUMN_TOTAL + " long integer not null, " + COLUMN_FLAGS + " integer);";
 
 	public BudgetDatabase(Context context)
 	{
 		super(context,DATABASE_NAME,null,DATABASE_VERSION);
 	}
 	
 	@Override
 	public void onCreate(SQLiteDatabase database)
 	{
 		database.execSQL(DATABASE_CREATE_TABLE_CASHFLOW);
 		database.execSQL(DATABASE_CREATE_TABLE_CATEGORIES);
 		database.execSQL(DATABASE_CREATE_TABLE_DAYSUM);
 		database.execSQL(DATABASE_CREATE_TABLE_CATEGORY_NAMES);
 		database.execSQL(DATABASE_CREATE_TABLE_DAYTOTAL);
 		
 		// Put in initial categories
 		ContentValues values = new ContentValues();
 		values.put(BudgetDatabase.COLUMN_CATEGORY, "Income");
 		database.insert(BudgetDatabase.TABLE_CATEGORY_NAMES, null,values);
 		values.put(BudgetDatabase.COLUMN_CATEGORY, "Food");
 		database.insert(BudgetDatabase.TABLE_CATEGORY_NAMES, null,values);
 		values.put(BudgetDatabase.COLUMN_CATEGORY, "Groceries");
 		database.insert(BudgetDatabase.TABLE_CATEGORY_NAMES, null,values);
 		/*
 		values.put(BudgetDatabase.COLUMN_CATEGORY, "Income");
 		values.put(BudgetDatabase.COLUMN_NUM, 0);
 		values.put(BudgetDatabase.COLUMN_TOTAL, 0);
 		database.insert(BudgetDatabase.TABLE_CATEGORIES, null,values);
 		
 		values.put(BudgetDatabase.COLUMN_CATEGORY, "Food");
 		values.put(BudgetDatabase.COLUMN_NUM, 0);
 		values.put(BudgetDatabase.COLUMN_TOTAL, 0);
 		database.insert(BudgetDatabase.TABLE_CATEGORIES, null,values);
 		
 		values.put(BudgetDatabase.COLUMN_CATEGORY, "Groceries");
 		values.put(BudgetDatabase.COLUMN_NUM, 0);
 		values.put(BudgetDatabase.COLUMN_TOTAL, 0);
 		database.insert(BudgetDatabase.TABLE_CATEGORIES, null,values);
 		
 		values.put(BudgetDatabase.COLUMN_CATEGORY, "Misc");
 		values.put(BudgetDatabase.COLUMN_NUM, 0);
 		values.put(BudgetDatabase.COLUMN_TOTAL, 0);
 		database.insert(BudgetDatabase.TABLE_CATEGORIES, null,values);*/
 		
 	}
 	
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
 	{
 		Cursor cursor;
 		switch(oldVersion)  
 		{
 		case 1:// Database from app version 2.0. Add the flags-column
 			db.execSQL("ALTER TABLE " + TABLE_CASHFLOW + " ADD COLUMN " + COLUMN_FLAGS);
 			db.execSQL("ALTER TABLE " + TABLE_DAYSUM + " ADD COLUMN " + COLUMN_FLAGS);
 			db.execSQL("ALTER TABLE " + TABLE_CATEGORIES + " ADD COLUMN " + COLUMN_FLAGS);
 		case 2:// Intermediate version 2.0.whatever. Create the new database storing category names
 			db.execSQL(DATABASE_CREATE_TABLE_CATEGORY_NAMES);
 			ArrayList<String> tempNames = new ArrayList<String>();
 			cursor = db.rawQuery("SELECT "+BudgetDatabase.COLUMN_CATEGORY+" FROM "+BudgetDatabase.TABLE_CATEGORIES, null);
 			if(cursor.getCount()!=0)
 			{
 				cursor.moveToFirst();
 				while(!cursor.isAfterLast())
 				{
 					System.out.println(cursor.getString(0));
 					tempNames.add(cursor.getString(0));
 					cursor.moveToNext();
 				}
 				
 			}
 			ContentValues values = new ContentValues();
 			for(int i=0;i<tempNames.size();i++)
 			{	
 				values.put(BudgetDatabase.COLUMN_CATEGORY, tempNames.get(i));
 				db.insert(BudgetDatabase.TABLE_CATEGORY_NAMES, null,values);
 			}
 		case 6:
 			db.execSQL(DATABASE_CREATE_TABLE_DAYTOTAL);
 			ArrayList<DayEntry> tempDays = new ArrayList<DayEntry>();
			cursor = db.rawQuery("SELECT " + COLUMN_DATE + "," + COLUMN_TOTAL + " FROM "+BudgetDatabase.TABLE_DAYSUM + " ORDER BY '_id' ASC", null);
 			
 			if(cursor.getCount()!=0)
 			{
 				cursor.moveToFirst();
 				while(!cursor.isAfterLast())
 				{
 					System.out.println(cursor.getString(0));
 					tempDays.add(new DayEntry(cursor.getString(0), cursor.getLong(1)));
 					cursor.moveToNext();
 				}
 				values = new ContentValues();
 				long sum=0;
 				for(int i=0;i<tempDays.size();i++)
 				{	
 					sum+= tempDays.get(i).getTotal();
 					values.put(BudgetDatabase.COLUMN_DATE, tempDays.get(i).getDate());
 					values.put(BudgetDatabase.COLUMN_TOTAL,sum);
 					
 					db.insert(BudgetDatabase.TABLE_DAYTOTAL, null,values);
 				}
 				
 			}
 			/*values.put(BudgetDatabase.COLUMN_CATEGORY, "Food");
 			db.insert(BudgetDatabase.TABLE_CATEGORY_NAMES, null,values);
 			values.put(BudgetDatabase.COLUMN_CATEGORY, "Groceries");
 			db.insert(BudgetDatabase.TABLE_CATEGORY_NAMES, null,values);*/
 		}
 		
 		System.out.println("Updated database");
 	    
 	}
 }
