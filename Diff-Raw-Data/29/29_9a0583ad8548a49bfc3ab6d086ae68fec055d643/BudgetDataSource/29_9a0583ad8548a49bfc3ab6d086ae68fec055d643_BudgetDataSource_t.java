 package budgetapp.util;
 /**
  * The class used to access the database. It itself uses the DatabaseAccess class to
  * query the database.
  * 
  * @author Steen
  * 
  */
 
 import java.util.List;
 
 
 import android.content.Context;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 
 public class BudgetDataSource {
 
 	
 	// Database fields
	public SQLiteDatabase database;
	public BudgetDatabase dbHelper;
 	private DatabaseAccess dbAccess;
 	
 	private String[] allColumnsTransactions = {BudgetDatabase.COLUMN_ID,
 			BudgetDatabase.COLUMN_VALUE, BudgetDatabase.COLUMN_DATE, BudgetDatabase.COLUMN_CATEGORY};
 	private String[] allColumnsCategories = {BudgetDatabase.COLUMN_ID,BudgetDatabase.COLUMN_CATEGORY};
 	
 	public BudgetDataSource(Context context)
 	{
 		dbHelper = new BudgetDatabase(context);
 	}
 	
 	public void open() throws SQLException
 	{
 		database = dbHelper.getWritableDatabase();
 		dbAccess = new DatabaseAccess(database);
 	}
 	
 	public void close()
 	{
 		dbHelper.close();
 	}
 	
 	public BudgetEntry createTransactionEntry(BudgetEntry theEntry)
 	{
 		return dbAccess.addEntry(theEntry);
 	}
 	public boolean removeTransactionEntry(BudgetEntry theEntry)
 	{
 		return dbAccess.removeEntry(theEntry);
 	}
 	
 	public CategoryEntry createCategoryEntry(CategoryEntry theEntry)
 	{
 		return dbAccess.addEntry(theEntry);
 	}
 	public void updateCategory(String theCategory,int value)
 	{
 		dbAccess.updateCategory(theCategory,value);
 	}
 	public void updateDaySum(BudgetEntry theEntry)
 	{
 		dbAccess.updateDaySum(theEntry);
 	}
 	
 	public void dropTables()
 	{
 		database.execSQL("DROP TABLE IF EXISTS " + "cashflow");
 		database.execSQL("DROP TABLE IF EXISTS " + "categories");
 	
 	}
 	public List<BudgetEntry> getAllTransactions()
 	{
 		return dbAccess.getTransactions(0);
 	}
 	
 	public List<DayEntry> getAllDays()
 	{
 		return dbAccess.getDaySum(0);
 	}
 	public List<DayEntry> getSomeDays(int n)
 	{
 		return dbAccess.getDaySum(n);
 	}
 	public List<BudgetEntry> getSomeTransactions(int n)
 	{
 		return dbAccess.getTransactions(n);
 	}
 	
 	public List<CategoryEntry> getAllCategories()
 	{
 		return dbAccess.getCategories();
 	}
 	
 	public boolean addCategory(String theCategory)
 	{
 		return dbAccess.addCategory(theCategory);
 	}
 
 	public boolean removeCategory(String theCategory)
 	{
 		return dbAccess.removeCategory(theCategory);
 	}
 	
 }
