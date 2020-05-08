 package budgetapp.main;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 /***
  * DatabaseAccess. The class responsible for communicating with the database. Used by DatabaseSource
  * 
  * @author Steen
  *
  */
 public class DatabaseAccess {
 
 	private SQLiteDatabase database;
 	private String[] allColumnsTransactions = {BudgetDatabase.COLUMN_ID,
 			BudgetDatabase.COLUMN_VALUE, BudgetDatabase.COLUMN_DATE, BudgetDatabase.COLUMN_CATEGORY};
 	private String[] allColumnsCategories = {BudgetDatabase.COLUMN_ID,BudgetDatabase.COLUMN_CATEGORY,BudgetDatabase.COLUMN_NUM,BudgetDatabase.COLUMN_TOTAL};
 	
 	public DatabaseAccess(SQLiteDatabase theDatabase)
 	{
 		database = theDatabase;
 	}
 	
 	
 	public boolean addCategory(String theCategory)
 	{
 		ContentValues values = new ContentValues();
 		values.put(BudgetDatabase.COLUMN_CATEGORY, theCategory);
 		long insertID = database.insert(BudgetDatabase.TABLE_CATEGORIES, null,values);
 		
 		if(insertID==-1)
 			return false;
 		else
 			return true;
 	}
 	public BudgetEntry addEntry(BudgetEntry theEntry)
 	{
 		ContentValues values = new ContentValues();
 		// Put in the values
 		values.put(BudgetDatabase.COLUMN_VALUE, theEntry.getValue());
 		values.put(BudgetDatabase.COLUMN_DATE,theEntry.getDate());
 		values.put(BudgetDatabase.COLUMN_CATEGORY, theEntry.getCategory());
 		
 		long insertId = database.insert(BudgetDatabase.TABLE_CASHFLOW, null,values);
 		
 		Cursor cursor = database.query(BudgetDatabase.TABLE_CASHFLOW,
 				allColumnsTransactions,BudgetDatabase.COLUMN_ID + " = " + insertId, null,
 				null, null, null);
 		cursor.moveToFirst();
 		BudgetEntry entry = new BudgetEntry(cursor.getLong(0),cursor.getInt(1),cursor.getString(2),cursor.getString(3));
 		cursor.close();
 		return entry;
 	}
 	
 	public void updateCategory(String theCategory,long value)
 	{
 		Cursor cursor;
 		cursor = database.rawQuery("select "+BudgetDatabase.COLUMN_NUM+","+BudgetDatabase.COLUMN_TOTAL+" from "+BudgetDatabase.TABLE_CATEGORIES+" where "+BudgetDatabase.COLUMN_CATEGORY+"="+"'"+theCategory+"'",null);
 		cursor.moveToFirst();
 		int num = cursor.getInt(0)+1; // Number of transactions of this category 
 		long newTotal = cursor.getLong(1)+value;
 		ContentValues values = new ContentValues();
 		values.put(BudgetDatabase.COLUMN_NUM,num);
 		values.put(BudgetDatabase.COLUMN_TOTAL,newTotal);
 		database.update(BudgetDatabase.TABLE_CATEGORIES, values, BudgetDatabase.COLUMN_CATEGORY+" = '"+theCategory+"'", null);
 		cursor.close();
 	}
 	
 	public CategoryEntry addEntry(CategoryEntry theEntry)
 	{
 		ContentValues values = new ContentValues();
 		values.put(BudgetDatabase.COLUMN_CATEGORY,theEntry.getCategory());
 		long insertId = database.insert(BudgetDatabase.TABLE_CATEGORIES, null, values);
 		
 		Cursor cursor = database.query(BudgetDatabase.TABLE_CATEGORIES,
 				allColumnsCategories,BudgetDatabase.COLUMN_ID + " = " + insertId,null,
 				null,null,null);
 		cursor.moveToFirst();
 		CategoryEntry entry = new CategoryEntry(cursor.getLong(0),cursor.getString(1));
 		cursor.close();
 		return entry;
 	}
 	// Get n number of transactions
 	public List<BudgetEntry> getTransactions(int n)
 	{
 		List<BudgetEntry> entries = new ArrayList<BudgetEntry>();
		
 		Cursor cursor;
 		if(n<=0) // Get all entries
			 cursor = database.query(BudgetDatabase.TABLE_CASHFLOW,allColumnsTransactions,null,null,null,null,null);
		else
			 cursor = database.query(BudgetDatabase.TABLE_CASHFLOW, allColumnsTransactions, null, null, null, null, null, ""+n);
 		cursor.moveToFirst();
 		while(!cursor.isAfterLast())
 		{
 			BudgetEntry entry =  new BudgetEntry(cursor.getLong(0),cursor.getInt(1),cursor.getString(2),cursor.getString(3));
 
 			entries.add(entry);
 			cursor.moveToNext();
 		}
 		cursor.close();
 		return entries;
 	}
 	// Gets all the categories in the database
 	public List<CategoryEntry> getCategories()
 	{
 		List<CategoryEntry> entries = new ArrayList<CategoryEntry>();
 		
 		Cursor cursor;
 		cursor = database.query(BudgetDatabase.TABLE_CATEGORIES,allColumnsCategories,null,null,null,null,null);
 		cursor.moveToFirst();
 		while(!cursor.isAfterLast())
 		{
 			entries.add(new CategoryEntry(cursor.getLong(0),cursor.getString(1),cursor.getInt(2),cursor.getLong(3)));
 			cursor.moveToNext();
 		}
 		cursor.close();
 		return entries;
 	}
 }
