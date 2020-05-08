 package org.measureit.androet.db;
 
 import org.measureit.androet.util.Cache;
 import java.util.Calendar;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import org.measureit.androet.util.Helper;
 
 /**
  *
  * @author ezsi
  */
 public class Transaction implements Serializable{
     public static final String TABLE_NAME = "transactions";
     public static final String VIEW_NAME = "vtransactions";
     public static final String COL_ID = "id";
     public static final String COL_CATEGORY_ID = "categoryId";
     public static final String COL_ACCOUNT_ID = "accountId";
     public static final String COL_AMOUNT = "amount";
     public static final String COL_DATE = "date";
     public static final String COL_DESCRIPTION = "description";
     public static final String VIEW_COL_YEAR = "year";
     public static final String VIEW_COL_MONTH = "month";
     public static final String VIEW_COL_WEEK = "week";
     public static final String VIEW_COL_DAY = "day";
     
     
     public static final String TABLE_CREATE =
             "CREATE TABLE " + TABLE_NAME + " ("
             + COL_ID+" integer primary key autoincrement,"
             + COL_ACCOUNT_ID + " INTEGER, "
             + COL_CATEGORY_ID + " INTEGER, "
             + COL_AMOUNT + " REAL, "
             + COL_DESCRIPTION + " TEXT, "
             + COL_DATE + " INTEGER, "
             + "FOREIGN KEY("+COL_ACCOUNT_ID+") REFERENCES "+Account.TABLE_NAME+"("+Account.COL_ID+"), "
             + "FOREIGN KEY("+COL_CATEGORY_ID+") REFERENCES "+Category.TABLE_NAME+"("+Category.COL_ID+"));";
    
     public static final String VIEW_CREATE = 
             "CREATE VIEW " + VIEW_NAME 
             + " AS SELECT *"
             + ", CAST(strftime(\"%Y\", "+COL_DATE+", 'unixepoch') AS INTEGER) AS " + VIEW_COL_YEAR
             + ", CAST(strftime(\"%m\", "+COL_DATE+", 'unixepoch') AS INTEGER) AS " + VIEW_COL_MONTH
             + ", CAST(strftime(\"%W\", "+COL_DATE+", 'unixepoch') AS INTEGER) AS " + VIEW_COL_WEEK
             + ", CAST(strftime(\"%d\", "+COL_DATE+", 'unixepoch') AS INTEGER) AS " + VIEW_COL_DAY
             + " FROM "+TABLE_NAME;
     
     private int id;
     private int accountId;
     private Category category;
     private double amount;
     private String description;
     private Calendar date;
     private int year;
     private int month;
 
     public Transaction(int id, int accountId, Category category, double amount, String description, Calendar date) {
         this.id = id;
         this.accountId = accountId;
         this.category = category;
         this.amount = amount;
         this.description = description;
         this.date = date;
     }
     
     public Transaction(int id, int accountId, Category category, double amount, String description, Calendar date, int year, int month) {
         this.id = id;
         this.accountId = accountId;
         this.category = category;
         this.amount = amount;
         this.description = description;
         this.date = date;
         this.year = year;
         this.month = month;
     }
 
     @Override
     public String toString() {
         return "Transaction{" + "id=" + id + ", accountId=" + accountId + ", category=" + category + ", amount=" + amount + ", description=" + description + ", date=" + (date == null ? "" : date.getTime().toString()) + ", year=" + year  + ", month=" + month + '}';
     }
 
     public void setAccountId(int accountId) {
         this.accountId = accountId;
     }
 
     public void setAmount(double amount) {
         this.amount = amount;
     }
     
     public int getAccountId() {
         return accountId;
     }
 
     public double getAmount() {
         return amount;
     }
 
     public Category getCategory() {
         return category;
     }
 
     public int getId() {
         return id;
     }
 
     public Calendar getDate() {
         return date;
     }
 
     public String getDescription() {
         return description;
     }
 
     public int getMonth() {
         return month;
     }
 
     public int getYear() {
         return year;
     }
     
     public static void insert(Transaction transaction) {
         insert(transaction.getAccountId(), transaction.getCategory().getId(), transaction.getAmount(), transaction.getDescription(), Helper.calendarToSeconds(transaction.getDate()));
     }
     
     public static void insert(int accountId, int categoryId, double amount, String description, int date) {
         insert(DatabaseHelper.getInstance().getWritableDatabase(), accountId, categoryId, amount, description, date);
     }
     
     public static void insert(SQLiteDatabase db, int accountId, int categoryId, double amount, String description, int date) {
         ContentValues initialValues = new ContentValues();
         initialValues.put(COL_ACCOUNT_ID, accountId);
         initialValues.put(COL_CATEGORY_ID, categoryId);
         initialValues.put(COL_AMOUNT, amount);
         initialValues.put(COL_DESCRIPTION, description);
         initialValues.put(COL_DATE, date);
         db.insert(TABLE_NAME, null, initialValues);
     }
     
     public static void delete(int transactionId){
         DatabaseHelper.getInstance().getWritableDatabase().delete(TABLE_NAME, COL_ID + "="+transactionId, null);
     }
     
     public static void delete(Account account){
         DatabaseHelper.getInstance().getWritableDatabase().delete(TABLE_NAME, COL_ACCOUNT_ID + "="+account.getId(), null);
     }
     
     
     public static double sum(int accountId){
         Cursor cursor = DatabaseHelper.getInstance().getWritableDatabase().rawQuery(
             "SELECT SUM("+COL_AMOUNT+") FROM "+TABLE_NAME+" WHERE "+COL_ACCOUNT_ID+"="+accountId, null);
         if(cursor.moveToFirst()) 
             return cursor.getDouble(0);
         return -1;
     }
     
     /**
      * Summarize transaction values for the current month (1-12).
      */
     public static double sum(Account account, int month){
         Cursor cursor = account.isGroup() ? 
             DatabaseHelper.getInstance().getWritableDatabase().rawQuery(
                 "SELECT SUM(t." + COL_AMOUNT + ") FROM " + VIEW_NAME + " AS t," + Account.MAP_TABLE_NAME
                 + " AS a WHERE t." + COL_ACCOUNT_ID + " = a." + Account.COL_MAP_ACCOUNT_ID 
                 + " AND a." + Account.COL_MAP_GROUP_ID + " = " + account.getId() 
                 + " AND t." + VIEW_COL_MONTH + " = " + month, null)
             : DatabaseHelper.getInstance().getWritableDatabase().rawQuery(
                 "SELECT SUM("+COL_AMOUNT+") FROM "+VIEW_NAME+" WHERE "+COL_ACCOUNT_ID+"="+account.getId() + " AND " + VIEW_COL_MONTH + " = " + month, null);
         
         if(cursor.moveToFirst()) 
             return cursor.getDouble(0);
         return -1;
     }
 
     public static List<Transaction> sumByCategory(Account account){
         List<Transaction> transactions = new ArrayList<Transaction>();
         SQLiteDatabase db = DatabaseHelper.getInstance().getWritableDatabase();
         String commonSuffix = " GROUP BY t." + VIEW_COL_YEAR + ", t." + VIEW_COL_MONTH + ", t." + COL_CATEGORY_ID
                + " ORDER by t." + VIEW_COL_YEAR + ", t." + VIEW_COL_MONTH + ", t." + COL_CATEGORY_ID;
         Cursor cursor = db.rawQuery((account.isGroup()) ?
             "SELECT t." + COL_CATEGORY_ID + ", sum(t." + COL_AMOUNT + "), t." + VIEW_COL_YEAR + ", t." + VIEW_COL_MONTH
                 + " FROM "+ VIEW_NAME + " AS t," + Account.MAP_TABLE_NAME + " AS a"
                 + " WHERE t." + COL_ACCOUNT_ID + " = a." + Account.COL_MAP_ACCOUNT_ID 
                 + " AND a." + Account.COL_MAP_GROUP_ID + " = " + account.getId() 
                 + commonSuffix
                 : 
             "SELECT t." + COL_CATEGORY_ID + ", sum(t." + COL_AMOUNT + "), t." + VIEW_COL_YEAR + ", t." + VIEW_COL_MONTH
                 + " FROM "+ VIEW_NAME + " AS t" 
                 + " WHERE t." + COL_ACCOUNT_ID + " = " + account.getId() 
                 + commonSuffix
                 , null);
         while(cursor.moveToNext())
             transactions.add(new Transaction(-1, account.getId(), Cache.getCategory(cursor.getInt(0))
                     , cursor.getDouble(1), "", null, cursor.getInt(2), cursor.getInt(3)));
         return transactions;
     }
     
     public static double sumGroup(int groupId){
         Cursor cursor = DatabaseHelper.getInstance().getWritableDatabase().rawQuery(
             "SELECT SUM(t." + COL_AMOUNT + ") FROM " + TABLE_NAME + " AS t," + Account.MAP_TABLE_NAME
                 + " AS a WHERE t." + COL_ACCOUNT_ID + " = a." + Account.COL_MAP_ACCOUNT_ID 
                 + " AND a." + Account.COL_MAP_GROUP_ID + " = " + groupId, null);
         if(cursor.moveToFirst()) 
             return cursor.getDouble(0);
         return -1;
     }
 
     
     public static List<Transaction> list(Account account, int year, int month, int category_id){
         SQLiteDatabase db = DatabaseHelper.getInstance().getWritableDatabase();
         String commonCondition = 
                 " AND t." + VIEW_COL_YEAR + " = " + year 
                 + " AND t." + VIEW_COL_MONTH + " = " + month;
         if(category_id > -1)
             commonCondition += " AND t." + COL_CATEGORY_ID + " = " + category_id;
         commonCondition += " ORDER by t." + COL_DATE +" DESC";
         
         String queryString = (account.isGroup()) 
             ? "SELECT t.* FROM " + VIEW_NAME + " AS t," + Account.MAP_TABLE_NAME
                 + " AS a WHERE t." + COL_ACCOUNT_ID + " = a." + Account.COL_MAP_ACCOUNT_ID 
                 + " AND a." + Account.COL_MAP_GROUP_ID + " = " + account.getId() 
                 + commonCondition
             : "SELECT t.* FROM " + VIEW_NAME + " AS t"
                 + " WHERE t." + COL_ACCOUNT_ID + " = " + account.getId() 
                 + commonCondition;
         Cursor cursor = db.rawQuery(queryString, null);
         return fetchData(cursor);
     }
     
     public static List<Transaction> list(Account account){
         return list(account, DatabaseHelper.getInstance().getWritableDatabase());
     }
     
     public static List<Transaction> list(Account account, SQLiteDatabase db){
         final int accountId = account.getId();
         Cursor cursor = (account.isGroup()) ? db.rawQuery(
             "SELECT t.* FROM " + TABLE_NAME + " AS t," + Account.MAP_TABLE_NAME
                 + " AS a WHERE t." + COL_ACCOUNT_ID + " = a." + Account.COL_MAP_ACCOUNT_ID 
                 + " AND a." + Account.COL_MAP_GROUP_ID + " = " + account.getId() + " ORDER by t." + COL_DATE +" DESC", null)
                 : db.query(TABLE_NAME, null, COL_ACCOUNT_ID+" = "+accountId, null, null, null, COL_DATE + " DESC");
         return fetchData(cursor);
     }
  
     private static List<Transaction> fetchData(final Cursor cursor){
         List<Transaction> transactions = new ArrayList<Transaction>();        
         while(cursor.moveToNext()){
             Calendar calendar = Calendar.getInstance();
             final int timeInSec = cursor.getInt(5);
             calendar.setTimeInMillis(timeInSec * 1000L);
             final int categoryId = cursor.getInt(2);
             Transaction transaction = new Transaction(cursor.getInt(0), cursor.getInt(1), Cache.getCategory(categoryId)
                     , cursor.getDouble(3), cursor.getString(4), calendar);
             transactions.add(transaction);
         }
         return transactions;
     }
     
 }
