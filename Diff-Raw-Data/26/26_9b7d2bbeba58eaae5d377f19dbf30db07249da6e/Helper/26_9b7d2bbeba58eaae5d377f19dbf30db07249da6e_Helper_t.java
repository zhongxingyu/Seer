 package com.testterra.main;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 
 public class Helper {
 
 	public static final String KEY_ROWID = "_id";
 	public static final String KEY_TEST = "TestText";
 	public static final String KEY_A_answer = "A_answer";
 	public static final String KEY_B_answer = "B_answer";
 	public static final String KEY_C_answer = "C_answer";
 	public static final String KEY_D_answer = "D_answer";
 	public static final String KEY_E_answer = "E_answer";
 	public static final String KEY_CORRECT = "CorrectAnswer";
 	public static final String KEY_IFEEXISTS = "IfEExists";
 
 	private static final String DATABASE_NAME = "ukrm.sqlite";
 	private static final String DATABASE_TABLE = "Ukrm1";
 	private static final int DATABASE_VERSION = 1;
 
 	private DbHelper ourHelper;
 	private final Context ourContext;
 	private SQLiteDatabase ourDatabase;
 
 	private static class DbHelper extends SQLiteOpenHelper {
 
 		public DbHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 			// TODO Auto-generated constructor stub
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
 		}
 
 	}
 
 	public Helper(Context c) {
 		ourContext = c;
 	}
 
 	public Helper open() throws SQLException {
 		ourHelper = new DbHelper(ourContext);
 		ourDatabase = ourHelper.getReadableDatabase();
 		return this;
 	}
 
 	public void close() {
 		ourHelper.close();
 	}
 
 	public long getSize() {
 
 		String sql = "SELECT COUNT(*) FROM " + DATABASE_TABLE;
 		SQLiteStatement statement = ourDatabase.compileStatement(sql);
 		long count = statement.simpleQueryForLong();
 		return count;
 	}
 
 	public String getKEY_TEST(long l) throws SQLException {
 		// TODO Auto-generated method stub
 		String[] columns = new String[] { KEY_ROWID, KEY_TEST, KEY_A_answer,
 				KEY_B_answer, KEY_C_answer, KEY_D_answer, KEY_E_answer,
 				KEY_CORRECT, KEY_IFEEXISTS };
 		Cursor c = ourDatabase.query(DATABASE_TABLE, columns, KEY_ROWID + "="
 				+ l, null, null, null, null);
 		if (c != null) {
 			c.moveToFirst();
 			String data = c.getString(1);
 			c.close();
 			return data;
 		}
 		return null;
 	}
 
 	public String getKEY_A_answer(long l) throws SQLException {
 		// TODO Auto-generated method stub
 		String[] columns = new String[] { KEY_ROWID, KEY_TEST, KEY_A_answer,
 				KEY_B_answer, KEY_C_answer, KEY_D_answer, KEY_E_answer,
 				KEY_CORRECT, KEY_IFEEXISTS };
 		Cursor c = ourDatabase.query(DATABASE_TABLE, columns, KEY_ROWID + "="
 				+ l, null, null, null, null);
 		if (c != null) {
 			c.moveToFirst();
 			String data = c.getString(2);
 			c.close();
 			return data;
 		}
 		return null;
 	}
 
 	public String getKEY_B_answer(long l) throws SQLException {
 		// TODO Auto-generated method stub
 		String[] columns = new String[] { KEY_ROWID, KEY_TEST, KEY_A_answer,
 				KEY_B_answer, KEY_C_answer, KEY_D_answer, KEY_E_answer,
 				KEY_CORRECT, KEY_IFEEXISTS };
 		Cursor c = ourDatabase.query(DATABASE_TABLE, columns, KEY_ROWID + "="
 				+ l, null, null, null, null);
 		if (c != null) {
 			c.moveToFirst();
 			String data = c.getString(3);
 			c.close();
 			return data;
 		}
 		return null;
 	}
 
 	public String getKEY_C_answer(long l) throws SQLException {
 		// TODO Auto-generated method stub
 		String[] columns = new String[] { KEY_ROWID, KEY_TEST, KEY_A_answer,
 				KEY_B_answer, KEY_C_answer, KEY_D_answer, KEY_E_answer,
 				KEY_CORRECT, KEY_IFEEXISTS };
 		Cursor c = ourDatabase.query(DATABASE_TABLE, columns, KEY_ROWID + "="
 				+ l, null, null, null, null);
 		if (c != null) {
 			c.moveToFirst();
 			String data = c.getString(4);
 			c.close();
 			return data;
 		}
 		return null;
 	}
 
 	public String getKEY_D_answer(long l) throws SQLException {
 		// TODO Auto-generated method stub
 		String[] columns = new String[] { KEY_ROWID, KEY_TEST, KEY_A_answer,
 				KEY_B_answer, KEY_C_answer, KEY_D_answer, KEY_E_answer,
 				KEY_CORRECT, KEY_IFEEXISTS };
 		Cursor c = ourDatabase.query(DATABASE_TABLE, columns, KEY_ROWID + "="
 				+ l, null, null, null, null);
 		if (c != null) {
 			c.moveToFirst();
 			String data = c.getString(5);
 			c.close();
 			return data;
 		}
 		return null;
 	}
 
 	public int getKEY_CORRECT(long l) throws SQLException {
 		// TODO Auto-generated method stub
 		String[] columns = new String[] { KEY_ROWID, KEY_TEST, KEY_A_answer,
 				KEY_B_answer, KEY_C_answer, KEY_D_answer, KEY_E_answer,
 				KEY_CORRECT, KEY_IFEEXISTS };
 		Cursor c = ourDatabase.query(DATABASE_TABLE, columns, KEY_ROWID + "="
 				+ l, null, null, null, null);
 		if (c != null) {
 			c.moveToFirst();
 			String data = c.getString(7);
 			c.close();
 			return Integer.parseInt(data);
 		}
 		return 0;
 	}
 
 	public boolean getKEY_IFEEXISTS(long l) throws SQLException {
 		// TODO Auto-generated method stub
 		String[] columns = new String[] { KEY_ROWID, KEY_TEST, KEY_A_answer,
 				KEY_B_answer, KEY_C_answer, KEY_D_answer, KEY_E_answer,
 				KEY_CORRECT, KEY_IFEEXISTS };
 		Cursor c = ourDatabase.query(DATABASE_TABLE, columns, KEY_ROWID + "="
 				+ l, null, null, null, null);
 		if (c != null) {
 			c.moveToFirst();
			boolean data = c.getInt(8)>0;
 			c.close();
 			return data;
 		}
 		return true;
 	}
 
 	public String getKEY_E_answer(long l) throws SQLException {
 		// TODO Auto-generated method stub
 		String[] columns = new String[] { KEY_ROWID, KEY_TEST, KEY_A_answer,
 				KEY_B_answer, KEY_C_answer, KEY_D_answer, KEY_E_answer,
 				KEY_CORRECT, KEY_IFEEXISTS };
 		Cursor c = ourDatabase.query(DATABASE_TABLE, columns, KEY_ROWID + "="
 				+ l, null, null, null, null);
 		if (c != null) {
 			c.moveToFirst();
 			String data = c.getString(6);
 			c.close();
 			return data;
 		}
 		return null;
 	}
 
 }
