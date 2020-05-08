 package org.adeveloper.expensemanager.lib;
 
 import java.util.ArrayList;
 
 import org.adeveloper.expensemanager.db.ExpensemanagerHelper;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
 
 public class Expense
 {
 	private SQLiteDatabase database;
 	
 	public Expense(SQLiteDatabase database)
 	{
 		this.database = database;
 	}
 	
 	/**
 	 * Adds a new row to expense table as expense with caption and price.
 	 * 
 	 * @param caption
 	 * @param price
 	 * @param balanceId
 	 * @return
 	 */
 	public boolean add(String caption, double price, Balance balanceObject)
 	{
 		if(!balanceObject.subtract(price)){
 			return false;
 		}
 		
 		ContentValues values = new ContentValues();
 		values.put("Caption", caption);
 		values.put("BalanceId", balanceObject.getLastInsertId());
 		values.put("UpdateTime", System.currentTimeMillis());
 		long insertId = database.insert(ExpensemanagerHelper.TABLE_NAME_EXPENSE, null, values);
 		
 		if(insertId == -1){
 			return false;
 		}else {
 			return true;
 		}
 	}
 	
	public ArrayList<org.adeveloper.expensemanager.model.Expense> getAllExpenses()
 	{
		String query = "SELECT * FROM " + ExpensemanagerHelper.TABLE_NAME_EXPENSE + 
 						" INNER JOIN " + ExpensemanagerHelper.TABLE_NAME_BALANCE +
 						" ON " + ExpensemanagerHelper.TABLE_NAME_EXPENSE + ".BalanceId=" +
 						ExpensemanagerHelper.TABLE_NAME_BALANCE + ".Id ORDER BY " +
 						ExpensemanagerHelper.TABLE_NAME_EXPENSE + ".UpdateTime DESC";
 		Cursor resultset = database.rawQuery(query, null);
 		
		ArrayList<org.adeveloper.expensemanager.model.Expense> list = 
 				new ArrayList<org.adeveloper.expensemanager.model.Expense>();
 		
 		while (resultset.moveToNext())
 		{
 			org.adeveloper.expensemanager.model.Expense expense = 
 					new org.adeveloper.expensemanager.model.Expense();
			expense.setCaption(resultset.getString(resultset.getColumnIndex("Caption")));
			Log.i("Debug", expense.getCaption());
			
			expense.setPrice(resultset.getDouble(resultset.getColumnIndex("NewBalance")));
			expense.setUpdateTime(resultset.getLong(resultset.getColumnIndex("UpdateTime")));
 			
 			list.add(expense);
 		}
 		
 		return list;
 	}
 }
