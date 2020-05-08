 package it.chalmers.mufasa.android_budget_app.controller.database;
 
 import it.chalmers.mufasa.android_budget_app.model.Account;
 import it.chalmers.mufasa.android_budget_app.model.Category;
 import it.chalmers.mufasa.android_budget_app.model.Transaction;
 
 import java.text.SimpleDateFormat;
import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 public class DataAccessor {
 	Context context;
 
 	public DataAccessor(Context context) {
 		this.context = context;
 	}
 
 	public enum SortBy {
 		NAME, DATE, CATEGORY
 	}
 
 	public enum SortByOrder {
 		DESC, ASC
 	}
 
 	public int getAccountBalance(int accountID) {
 		SQLiteDatabase db = new DatabaseOpenHelper(context)
 				.getWritableDatabase();
 		String[] arr = { "balance" };
 		Cursor cursor = db.query("accounts", arr, "id == " + accountID, null,
 				null, null, null);
 
 		if (cursor.moveToFirst()) {
 			return cursor.getInt(0);
 		} else {
 			throw new IllegalArgumentException("Account ID "+accountID+" does not exist");
 		}
 	}
 
 	public void setAccountBalance(int balance, int accountID) {
 		SQLiteDatabase db = new DatabaseOpenHelper(context)
 				.getWritableDatabase();
 
 		try {
 		    int currentBalance = getAccountBalance(accountID);
 		    if(currentBalance != balance) {
 			db.execSQL("UPDATE accounts SET balance=" + balance
 				+ " WHERE id == " + accountID);
 		    }
 		} catch(IllegalArgumentException e) {
 		    db.execSQL("INSERT INTO accounts VALUES (" + accountID + ",null,"
 				+ balance + ")");
 		}
 
 	}
 
 	public void addTransaction(Transaction transaction) {
 		SQLiteDatabase db = new DatabaseOpenHelper(context)
 				.getWritableDatabase();
 		db.execSQL("INSERT INTO transactions (account-id, name, date, value, category-id ) ("
 				+ transaction.getAccount().getId()
 				+ ", "
 				+ transaction.getName()
 				+ ", "
				+ transaction.getDate().getYear() + "-" + transaction.getDate().getMonth() + "-" + transaction.getDate().getDay()
 				+ ", "
 				+ transaction.getAmount()
 				+ ", "
 				+ transaction.getCategory().getId() + ")");
 
 	}
 
 	public void removeTransaction(Transaction transaction) {
 
 	}
 
 	public List<Transaction> getTransactions(Account account, SortBy sortBy,
 			SortByOrder sortByOrder, int start, int stop) {
		List<Transaction> transactions = new ArrayList<Transaction>();
 		String sortByTemp = "date";
 		String sortByOrderTemp = "desc";			
 
 		switch (sortBy) {
 		case NAME:
 			sortByTemp = "name";
 			break;
 		case DATE:
 			sortByTemp = "date";
 			break;
 		case CATEGORY:
 			sortByTemp = "category";
 			break;
 		}
 		switch (sortByOrder) {
 		case ASC:
 			sortByOrderTemp = "asc";
 			break;
 		case DESC:
 			sortByOrderTemp = "desc";
 			break;
 		}
 
 		SQLiteDatabase db = new DatabaseOpenHelper(context)
 				.getWritableDatabase();
 		String[] arr = { "name", "date", "category", "value" };
 
 		Cursor cursor = db.query("table", arr, "account-id == "
 				+ account.getId(), null, null, null, sortByTemp + " "
 				+ sortByOrderTemp);
 
 		if (cursor.moveToFirst()) {
 			cursor.move(start);
 			for (int i = start; i < stop; i++) {
				String dateString = cursor.getString(2);
				String[] list= dateString.split("-");
				
				Date date = new Date(Integer.parseInt(list[0]), Integer.parseInt(list[1]), Integer.parseInt(list[2]));
				
 				Category category = new Category((cursor.getString(4)));
				Transaction transaction = new Transaction((cursor.getInt(3)), date, cursor.getString(2), category, account);
 				transactions.add(transaction); 
 				cursor.moveToNext();
 			}
 			return transactions;
 		} else {
 			return null;
 		}
 	}
 }
