 /*
  * Copyright (c) Mattia Barbon <mattia@barbon.org>
  * distributed under the terms of the MIT license
  */
 
 package org.barbon.acash;
 
 import android.content.Context;
 import android.content.ContentValues;
 
 import android.database.Cursor;
 
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintWriter;
 
 import java.text.SimpleDateFormat;
 
 import java.util.Date;
 
 public class ExpenseDatabase {
     private static final int VERSION = 1;
     private static final String DATABASE_NAME = "expenses";
 
     private static final String ACCOUNTS_TABLE = "accounts";
     private static final String EXPENSES_TABLE = "expenses";
 
     public static final String GNUCASH_ACCOUNT_COLUMN = "gc_account";
     public static final String ACCOUNT_DESCRIPTION_COLUMN =
         "account_description";
     public static final String FROM_ACCOUNT_COLUMN = "account_from";
     public static final String TO_ACCOUNT_COLUMN = "account_to";
     public static final String DATE_COLUMN = "transaction_date";
     public static final String DATE_YEAR_COLUMN = "transaction_year";
     public static final String DATE_MONTH_COLUMN = "transaction_month";
     public static final String DATE_DAY_COLUMN = "transaction_day";
     public static final String AMOUNT_COLUMN = "transaction_amount";
     public static final String EXPENSE_DESCRIPTION_COLUMN =
         "transaction_description";
 
     private static ExpenseDatabase theInstance;
     private static final SimpleDateFormat iso8601 =
         new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
     private ExpenseOpenHelper openHelper;
     private SQLiteDatabase database;
 
     private ExpenseDatabase(Context context) {
         openHelper = new ExpenseOpenHelper(context);
     }
 
     public static ExpenseDatabase getInstance(Context context) {
         if (theInstance == null)
             theInstance = new ExpenseDatabase(context);
 
         return theInstance;
     }
 
     private SQLiteDatabase getDatabase() {
         if (database == null)
             database = openHelper.getWritableDatabase();
 
         return database;
     }
 
     public Cursor getFromAccountList() {
         SQLiteDatabase db = getDatabase();
 
         return db.rawQuery(
             "SELECT id AS _id, " + ACCOUNT_DESCRIPTION_COLUMN +
             "     FROM " + ACCOUNTS_TABLE +
             "     ORDER BY " + ACCOUNT_DESCRIPTION_COLUMN, null);
     }
 
     public Cursor getToAccountList() {
         SQLiteDatabase db = getDatabase();
 
         return db.rawQuery(
             "SELECT id AS _id, " + ACCOUNT_DESCRIPTION_COLUMN +
             "    FROM " + ACCOUNTS_TABLE +
             "    ORDER BY " + ACCOUNT_DESCRIPTION_COLUMN, null);
     }
 
     public Cursor getAccountList() {
         SQLiteDatabase db = getDatabase();
 
         return db.rawQuery(
             "SELECT id AS _id, " + ACCOUNT_DESCRIPTION_COLUMN +
             "     FROM " + ACCOUNTS_TABLE +
             "     ORDER BY " + ACCOUNT_DESCRIPTION_COLUMN, null);
     }
 
     public Cursor getExpenseList() {
         SQLiteDatabase db = getDatabase();
 
         return db.rawQuery(
             "SELECT id AS _id, " + AMOUNT_COLUMN + ", " +
                     EXPENSE_DESCRIPTION_COLUMN +
             "     FROM " + EXPENSES_TABLE +
             "     ORDER BY " + DATE_COLUMN, null);
     }
 
     public boolean insertAccount(String description, String gnuCash) {
         SQLiteDatabase db = getDatabase();
         ContentValues vals = new ContentValues();
 
         vals.put(ACCOUNT_DESCRIPTION_COLUMN, description);
         vals.put(GNUCASH_ACCOUNT_COLUMN, gnuCash);
 
         return db.insert(ACCOUNTS_TABLE, null, vals) == 1;
     }
 
     public boolean updateAccount(long id, String description, String gnuCash) {
         SQLiteDatabase db = getDatabase();
         ContentValues vals = new ContentValues();
 
         vals.put(ACCOUNT_DESCRIPTION_COLUMN, description);
         vals.put(GNUCASH_ACCOUNT_COLUMN, gnuCash);
 
         return db.update(ACCOUNTS_TABLE, vals, "id = ?",
                          new String[] { Long.toString(id) }) == 1;
     }
 
     public boolean insertExpense(int from_account, int to_account,
                                  double amount, Date date,
                                  String description) {
         SQLiteDatabase db = getDatabase();
         ContentValues vals = new ContentValues();
 
         vals.put(FROM_ACCOUNT_COLUMN, from_account);
         vals.put(TO_ACCOUNT_COLUMN, to_account);
         vals.put(DATE_COLUMN, iso8601.format(date));
         vals.put(AMOUNT_COLUMN, amount);
         vals.put(EXPENSE_DESCRIPTION_COLUMN, description);
 
         return db.insert(EXPENSES_TABLE, null, vals) != -1;
     }
 
     public boolean updateExpense(long id,
                                  int from_account, int to_account,
                                  double amount, Date date,
                                  String description) {
         SQLiteDatabase db = getDatabase();
         ContentValues vals = new ContentValues();
 
         vals.put(FROM_ACCOUNT_COLUMN, from_account);
         vals.put(TO_ACCOUNT_COLUMN, to_account);
         vals.put(DATE_COLUMN, iso8601.format(date));
         vals.put(AMOUNT_COLUMN, amount);
         vals.put(EXPENSE_DESCRIPTION_COLUMN, description);
 
         return db.update(EXPENSES_TABLE, vals, "id = ?",
                          new String[] { Long.toString(id) }) == 1;
     }
 
     public boolean deleteExpense(long id) {
         SQLiteDatabase db = getDatabase();
 
         return db.delete(EXPENSES_TABLE, "id = ?",
                          new String[] { Long.toString(id) }) == 1;
     }
 
     public ContentValues getAccount(long id) {
         SQLiteDatabase db = getDatabase();
         Cursor cursor = db.rawQuery(
             "SELECT id AS _id, " + ACCOUNT_DESCRIPTION_COLUMN + ", " +
                     GNUCASH_ACCOUNT_COLUMN +
             "    FROM " + ACCOUNTS_TABLE +
             "    WHERE id = ?", new String[] { Long.toString(id) });
 
         ContentValues vals = null;
 
         if (cursor.moveToNext()) {
             vals = new ContentValues();
 
             vals.put(ACCOUNT_DESCRIPTION_COLUMN, cursor.getString(1));
             vals.put(GNUCASH_ACCOUNT_COLUMN, cursor.getString(2));
         }
 
         cursor.close();
 
         return vals;
     }
 
     public ContentValues getExpense(long id) {
         SQLiteDatabase db = getDatabase();
         Cursor cursor = db.rawQuery(
             "SELECT id AS _id, " + AMOUNT_COLUMN + ", " +
                     EXPENSE_DESCRIPTION_COLUMN + ", " +
                     FROM_ACCOUNT_COLUMN + ", " +
                     TO_ACCOUNT_COLUMN + ", " +
             "       strftime('%Y', transaction_date)," +
             "       strftime('%m', transaction_date)," +
             "       strftime('%d', transaction_date)" +
             "     FROM " + EXPENSES_TABLE +
             "    WHERE id = ?", new String[] { Long.toString(id) });
 
         ContentValues vals = null;
 
         if (cursor.moveToNext()) {
             vals = new ContentValues();
 
             vals.put(AMOUNT_COLUMN, cursor.getDouble(1));
             vals.put(EXPENSE_DESCRIPTION_COLUMN, cursor.getString(2));
             vals.put(FROM_ACCOUNT_COLUMN, cursor.getInt(3));
             vals.put(TO_ACCOUNT_COLUMN, cursor.getInt(4));
             vals.put(DATE_YEAR_COLUMN, cursor.getInt(5));
             vals.put(DATE_MONTH_COLUMN, cursor.getInt(6));
             vals.put(DATE_DAY_COLUMN, cursor.getInt(7));
         }
 
         cursor.close();
 
         return vals;
     }
 
     public boolean exportQif(File qifFile) {
         PrintWriter qif = null;
 
         try {
             qif = new PrintWriter(new FileOutputStream(qifFile));
 
             return exportQif(qif);
         }
         catch (FileNotFoundException e) {
             return false;
         }
         finally {
             if (qif != null)
                 qif.close();
         }
     }
 
     public boolean exportQif(PrintWriter qif) {
         SQLiteDatabase db = getDatabase();
         Cursor expenses = db.rawQuery(
             "SELECT af.gc_account, at.gc_account," +
             "        transaction_amount, transaction_description," +
             "        strftime('%Y', transaction_date)," +
             "        strftime('%m', transaction_date)," +
             "        strftime('%d', transaction_date)" +
             "    FROM " + EXPENSES_TABLE +
             "    INNER JOIN accounts AS af" +
             "        ON account_from = af.id" +
             "    INNER JOIN accounts AS at" +
             "        ON account_to = at.id" +
             "    ORDER BY af.id, transaction_date", null);
 
         while (expenses.moveToNext()) {
             // from account
             qif.println("!Account");
 
             qif.print('N');
             qif.println(expenses.getString(0));
 
             qif.println("^");
 
             // start expense
             qif.println("!Type:Cash");
 
             // date
             qif.print('D');
             qif.print(expenses.getString(6));
             qif.print('/');
             qif.print(expenses.getString(5));
             qif.print('/');
             qif.println(expenses.getString(4));
 
             // to account
             qif.print('L');
             qif.println(expenses.getString(1));
 
             // amount
             qif.print('T');
            qif.println(expenses.getFloat(2));
 
             // description
             qif.print('M');
             qif.println(expenses.getString(3));
 
             // end of record
             qif.println('^');
         }
 
         return true;
     }
 
     public boolean deleteExpenses() {
         SQLiteDatabase db = getDatabase();
 
         return db.delete(EXPENSES_TABLE, null, null) != -1;
     }
 
     private static class ExpenseOpenHelper extends SQLiteOpenHelper {
         private static final String ACCOUNTS_TABLE_CREATE =
             "CREATE TABLE " + ACCOUNTS_TABLE + " ( " +
             "id INTEGER PRIMARY KEY," +
             GNUCASH_ACCOUNT_COLUMN + " TEXT UNIQUE, " +
             ACCOUNT_DESCRIPTION_COLUMN + " TEXT UNIQUE" +
             ")";
 
         private static final String EXPENSES_TABLE_CREATE =
             "CREATE TABLE " + EXPENSES_TABLE + " ( " +
             "id INTEGER PRIMARY KEY," +
             FROM_ACCOUNT_COLUMN + " INTEGER, " +
             TO_ACCOUNT_COLUMN + " INTEGER, " +
             EXPENSE_DESCRIPTION_COLUMN + " TEXT, " +
             DATE_COLUMN + " DATETIME, " +
             AMOUNT_COLUMN + " FLOAT," +
             "FOREIGN KEY(" + FROM_ACCOUNT_COLUMN + ") REFERENCES " +
                 ACCOUNTS_TABLE + "(id)," +
             "FOREIGN KEY(" + FROM_ACCOUNT_COLUMN + ") REFERENCES " +
                 ACCOUNTS_TABLE + "(id)" +
             ")";
 
         public ExpenseOpenHelper(Context context) {
             super(context, DATABASE_NAME, null, VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             db.execSQL(ACCOUNTS_TABLE_CREATE);
             db.execSQL(EXPENSES_TABLE_CREATE);
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int from, int to) {
             throw new UnsupportedOperationException(
                 "Unable to upgrade database");
         }
     }
 }
