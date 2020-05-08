 package com.sbar.rebudget.database;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 
 import com.sbar.rebudget.database.DatabaseHelper;
 import com.sbar.rebudget.Common;
 import com.sbar.rebudget.FilterStruct;
 
 public class DatabaseConnector {
     public final String DB_NAME = "rebudget";
     private DatabaseHelper dbHelper;
     private SQLiteDatabase db = null;
 
     public DatabaseConnector(Context context) {
         dbHelper = new DatabaseHelper(context, DB_NAME, null, 1);
     }
 
     public void open() throws SQLException {
         if (db == null)
             db = dbHelper.getWritableDatabase();
     }
 
     public void close() {
         if (db != null) {
             db.close();
             db = null;
         }
     }
 
     public boolean addFilter(FilterStruct f)
     {
         open();
 
         Common.LOGI("addFilter");
 
        if (f.costFracRegexp == null) f.costFracRegexp = "";
        if (f.remainingFracRegexp == null) f.remainingFracRegexp = "";

         Cursor cur = db.query(
             "filters",
             new String[] {"wallet_id"},
             "outcome= ? and wallet_id = (select id from wallets where name =?) and sms_address = ? and sms_text_contains = ? and cost_integer_regexp = ? and cost_frac_regexp = ? and remaining_integer_regexp = ? and remaining_frac_regexp = ?",
            new String[]{f.outcome ? "1" : "0", f.wallet, f.smsAddress, f.smsTextContains, f.costIntegerRegexp, f.costFracRegexp, f.remainingIntegerRegexp, f.remainingFracRegexp},
             null, null, null, null
         );
         int count = cur.getCount();
         Common.LOGI("count=" + count);
         if (count > 0)
             return false;
 
         ContentValues c = new ContentValues();
         c.put("wallet_id", "(select id from wallets where name = \"" + f.wallet + "\")");
         c.put("outcome", f.outcome);
         c.put("sms_address", f.smsAddress);
         c.put("sms_text_contains", f.smsTextContains);
         c.put("cost_integer_regexp", f.costIntegerRegexp);
         c.put("cost_frac_regexp", f.costFracRegexp);
         c.put("remaining_integer_regexp", f.remainingIntegerRegexp);
         c.put("remaining_frac_regexp", f.remainingFracRegexp);
         c.put("store_regexp", f.storeRegexp);
 
         return db.insert("filters", null, c) != -1;
     }
 
     public boolean addCategory(String name, int color, float moneyPlanned, float moneySpent) {
         open();
 
         Common.LOGI("addCategory");
         ContentValues c = new ContentValues();
 
         c.put("name", name);
         c.put("color", color);
         c.put("money_planned", moneyPlanned);
         c.put("money_spent", moneySpent);
 
         return db.insert("categories", null, c) != -1;
     }
 
     public boolean addDefaultCategories() {
         boolean result = true;
         result &= addCategory("Reserved", 0xFFAAAA00, 5000.0f, 0.0f);
         result &= addCategory("Food", 0xFFAA00AA, 3000.0f, 0.0f);
         return result;
     }
 
     public boolean addWallet(String name, float money) {
         open();
 
         Common.LOGI("addWallet");
         ContentValues c = new ContentValues();
 
         c.put("name", name);
         c.put("money", money);
 
         return db.insert("wallets", null, c) != -1;
     }
 
     public boolean addStat(long datetime,
                            float moneyPlanned, float moneySpent)
     {
         open();
 
         Common.LOGI("addStat");
         ContentValues c = new ContentValues();
 
         c.put("datetime", datetime);
         c.put("money_planned", moneyPlanned);
         c.put("money_spent", moneySpent);
 
         return db.insert("stats", null, c) != -1;
     }
 
     public boolean addStore(String name) {
         open();
 
         Common.LOGI("addStore");
         ContentValues c = new ContentValues();
 
         c.put("name", name);
         c.put("category_id", "(select id from categories where name = \"Reserved\")");
 
         return db.insert("stores", null, c) != -1;
     }
 
     public boolean removeCategory(String name) {
         // substract category money from Reserved category
         // update category_id to Reserved in stores table
         // remove category
         if (name == "Reserved")
             return false;
         return false;
     }
 
     public boolean updateStore(String name, String category) {
         open();
         ContentValues c = new ContentValues();
         c.put("category_id", "(select id from categories where name = " + category + ")");
         return db.update("stores", c, "name = ?", new String[] {name}) != 0;
     }
 
     public boolean deleteWallet(String name) {
         open();
         // TODO: remove filters
         return db.delete("wallets", "name = ?", new String[] {name}) != 0;
     }
 
     public boolean renameWallet(String currentName, String name)
     {
         open();
 
         ContentValues c = new ContentValues();
         c.put("name", name);
 
         return db.update("wallets", c, "name = ?", new String[] {currentName}) != 0;
     }
 
     public Cursor selectCategories() {
         open();
 
         return db.query("categories", new String[] {"name", "color", "money_planned", "money_spent"}, null, null, null, null, null, null);
     }
 
     public Cursor selectFilters() {
         open();
 
         return db.query("filters", new String[] {"sms_address", "sms_text_contains"}, null, null, null, null, null, null);
     }
 
     public Cursor selectWallets() {
         open();
 
         return db.query("wallets", new String[] {"name", "money"}, null, null, null, null, null, null);
     }
 }
