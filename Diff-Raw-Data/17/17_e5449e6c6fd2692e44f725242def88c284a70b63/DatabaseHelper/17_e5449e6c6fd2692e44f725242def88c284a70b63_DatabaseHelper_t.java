 package com.pas.databases;
 
 import java.math.BigDecimal;
 import java.sql.SQLException;
 import java.util.Date;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.util.Log;
 
 import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.dao.RuntimeExceptionDao;
 import com.j256.ormlite.support.ConnectionSource;
 import com.j256.ormlite.table.TableUtils;
 import com.pas.bean.Expenses;
 
 public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
 	
 	
 	private static final String DATABASE_NAME = "expenses";
 	private static final int DATABASE_VERSION = 1;
 	private static final String CLASS_TAG = DatabaseHelper.class.getName();
 
 	private Dao<Expenses, Integer> expensesDao = null;
 	private RuntimeExceptionDao<Expenses, Integer> runTimeExpensesDao = null;
 	
 
 	public DatabaseHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
 		try {
 			Log.i(CLASS_TAG, "Sedang membuat Database");
 			TableUtils.createTable(connectionSource, Expenses.class);
 		} catch (SQLException e) {
 			Log.e(CLASS_TAG, "Tidak dapat membuat database");
 			throw new RuntimeException(e);
 		}
 		
		//Coba insert pada saat database pertama kali dibuat
 		RuntimeExceptionDao<Expenses, Integer> expensesDao = getExpensesDao();
 		Expenses expenses = new Expenses("MKN", "Nasi Padang", new Date(), new BigDecimal(12000));
 		getExpensesDao().create(expenses);
 		Log.i(CLASS_TAG, "Memasukan entry data di method onCreate");
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase arg0, ConnectionSource arg1, int arg2,
 			int arg3) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	
 	public Dao<Expenses, Integer> getDao() throws SQLException{
 		if (expensesDao == null) {
 			expensesDao = getDao(Expenses.class);
 		}
 		
 		return expensesDao;
 	}
 	
 	public RuntimeExceptionDao<Expenses,Integer> getExpensesDao() {
 		if (runTimeExpensesDao == null) {
 			runTimeExpensesDao = getRuntimeExceptionDao(Expenses.class);
 		}
 		return runTimeExpensesDao;
 	}
 
 	@Override
 	public void close() {
 		super.close();
 		runTimeExpensesDao = null;
 	}
 	
 }
