 package edu.ucsb.cs.cs185.moneysaver;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 
 public class DataBaseWrapper {
 	private SQLiteDataBaseHelper sql_database_helper;
 	private SQLiteDatabase sql_database;
 	private String[] all_columns = { SQLiteDataBaseHelper.COLUMN_ID,
 			SQLiteDataBaseHelper.COL_NAME,
 			SQLiteDataBaseHelper.COL_AMOUNT, 
 			SQLiteDataBaseHelper.COL_CATEGORY, 
 			SQLiteDataBaseHelper.COL_DATE };
 	
 	public DataBaseWrapper(Context context) {
 		sql_database_helper = new SQLiteDataBaseHelper(context);
 		sql_database = sql_database_helper.getWritableDatabase();
 
 	}
 	
 	/*
 	 * Open/close the database
 	 */
 	public void open() throws SQLException {
 		sql_database = sql_database_helper.getWritableDatabase();
 	}
 	
 	public void close() {
 		sql_database_helper.close();
 	}
 	
 	/*
 	 * Insert row into table, store row ID into object
 	 */
 	public Transaction insertTransaction(Transaction new_transaction) {
 		//store values with appropriate column keys
 		ContentValues values = new ContentValues();
 		
 		values.put(SQLiteDataBaseHelper.COL_NAME, new_transaction.getName());
 		
 		values.put(SQLiteDataBaseHelper.COL_AMOUNT, new_transaction.getValue());
 		values.put(SQLiteDataBaseHelper.COL_CATEGORY, new_transaction.getCategory());
 		values.put(SQLiteDataBaseHelper.COL_DATE, new_transaction.getDate());
 		//insert into database and return id of transaction
 		long insert_id = sql_database.insert(SQLiteDataBaseHelper.TABLE_TRANS, null,
 				values);
 		
 		new_transaction.setId(insert_id);
 		return new_transaction;
 	}
 	
 	/*
 	 * Delete row inside databases
 	 */
 	public long deleteTransaction(long row_id) {
 		
 		long row = sql_database.delete(SQLiteDataBaseHelper.TABLE_TRANS, SQLiteDataBaseHelper.COLUMN_ID
 				+ " = " + row_id, null);
 		
 		return row;
 	}
 	
 	/*
 	 * delete row inside database
 	 */
 	public long deleteTransaction(Transaction transaction) {
 		long row_id = transaction.getId();
 		
 		return deleteTransaction(row_id); 
 	}
 	
 	/*
 	 * Get total balance
 	 * 		add up all amount values
 	 */
 	public float getBalance() {
 		float total_balance = 0;
 		
 		List<Transaction> trans_list = getAllTransactions();
 		
 		int list_size = trans_list.size();
 		
 		for(int i=0; i<list_size; i++) {
 			 Transaction current_trans = trans_list.get(i);
 			 float current_amount = current_trans.getValue();
 			 
 			 total_balance += current_amount;
 			 
 		}
 		return total_balance;
 	}
 	
 	//http://www.vogella.com/articles/AndroidSQLite/article.html#databasetutorial
 	public List<Transaction> getAllTransactions() {
 		List<Transaction> trans_list = new ArrayList<Transaction>();
 
 		Cursor cursor = sql_database.query(sql_database_helper.TABLE_TRANS,
 				all_columns, null, null, null, null, null);
 
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			Transaction trans = cursorToComment(cursor);
 			trans_list.add(trans);
 			cursor.moveToNext();
 		}
 		// Make sure to close the cursor
 		cursor.close();
 		return trans_list;
 	}
 		
 	private Transaction cursorToComment(Cursor cursor) {
 		//TODO: get rest of fields
		Transaction trans = new Transaction(cursor.getString(1), cursor.getFloat(2), cursor.getString(3), cursor.getString(4));
 		return trans;
 	}
 	
 	public void dropTable() {
 		sql_database_helper.dropTable(sql_database);
 		sql_database = sql_database_helper.getWritableDatabase();
 	}
 	
 }
