 package org.adeveloper.expensemanager.lib;
 
 import java.util.ArrayList;
import java.util.List;
 
 import org.adeveloper.expensemanager.db.ExpensemanagerHelper;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
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
 	
	public List<org.adeveloper.expensemanager.model.Expense> getAllExpenses()
 	{
		String query = "SELECT "
				+ ExpensemanagerHelper.TABLE_NAME_EXPENSE + ".Id, " 
				+ ExpensemanagerHelper.TABLE_NAME_EXPENSE + ".Caption, " 
				+ ExpensemanagerHelper.TABLE_NAME_BALANCE + ".NewBalance, "
				+ ExpensemanagerHelper.TABLE_NAME_EXPENSE + ".UpdateTime "
				+ "FROM " + ExpensemanagerHelper.TABLE_NAME_EXPENSE + 
 						" INNER JOIN " + ExpensemanagerHelper.TABLE_NAME_BALANCE +
 						" ON " + ExpensemanagerHelper.TABLE_NAME_EXPENSE + ".BalanceId=" +
 						ExpensemanagerHelper.TABLE_NAME_BALANCE + ".Id ORDER BY " +
 						ExpensemanagerHelper.TABLE_NAME_EXPENSE + ".UpdateTime DESC";
 		Cursor resultset = database.rawQuery(query, null);
 		
		List<org.adeveloper.expensemanager.model.Expense> list = 
 				new ArrayList<org.adeveloper.expensemanager.model.Expense>();
 		
 		while (resultset.moveToNext())
 		{
 			org.adeveloper.expensemanager.model.Expense expense = 
 					new org.adeveloper.expensemanager.model.Expense();

			expense.setId(
					resultset.getLong(resultset.getColumnIndex("Id")));
			expense.setCaption(
					resultset.getString(resultset.getColumnIndex("Caption")));
			expense.setPrice(
					resultset.getDouble(resultset.getColumnIndex("NewBalance")));
			expense.setUpdateTime(
					resultset.getLong(resultset.getColumnIndex("UpdateTime")));
 			
 			list.add(expense);
 		}
 		
 		return list;
 	}
 }
