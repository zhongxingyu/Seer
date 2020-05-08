 package com.cachirulop.moneybox.manager;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 import com.cachirulop.moneybox.data.MoneyboxDataHelper;
 import com.cachirulop.moneybox.entity.Moneybox;
 import com.cachirulop.moneybox.entity.Movement;
 
 public class MoneyboxesManager {
     /**
      * Create a list with all the moneyboxes of the database
      * 
      * @return New ArrayList of Moneybox objects
      */
     public static ArrayList<Moneybox> getAllMoneyboxes() {
         Cursor c;
         SQLiteDatabase db = null;
 
         try {
             db = new MoneyboxDataHelper(ContextManager.getContext())
                     .getReadableDatabase();
 
             c = db.query("moneyboxes", null, null, null, null, null,
                     "description COLLATE NOCASE ASC");
 
             return createMoneyboxesList(c);
         } finally {
             if (db != null) {
                 db.close();
             }
         }
     }
 
     /**
      * Create a list of moneyboxes from a database cursor
      * 
      * @param c
      *            Cursor with the database data
      * @return New ArrayList with the moneyboxes of the cursor
      */
     private static ArrayList<Moneybox> createMoneyboxesList(Cursor c) {
         ArrayList<Moneybox> result;
 
         result = new ArrayList<Moneybox>();
 
         if (c != null) {
             if (c.moveToFirst()) {
                 do {
                     result.add(createMoneybox(c));
                 } while (c.moveToNext());
             }
         }
 
         return result;
     }
 
     /**
      * Create new moneybox object from a database cursor
      * 
      * @param c
      *            Cursor with the data of the moneybox
      * @return New object of {@link com.cachirulop.moneybox.entity.Moneybox}
      *         class with the data of the cursor
      */
     private static Moneybox createMoneybox(Cursor c) {
         Moneybox result;
 
         result = new Moneybox();
         result.setIdMoneybox(c.getLong(c.getColumnIndex("id_moneybox")));
         result.setDescription(c.getString(c.getColumnIndex("description")));
         result.setCreationDate(new Date(c.getLong(c
                 .getColumnIndex("creation_date"))));
 
         return result;
     }
 
     /**
      * Gets a moneybox from the database
      * 
      * @param idMoneybox
      *            ID of the Moneybox to be loaded
      * @return New Moneybox object with the data of the database
      */
     public static Moneybox getMoneybox(long idMoneybox) {
         SQLiteDatabase db = null;
         Cursor c;
 
         try {
             db = new MoneyboxDataHelper(ContextManager.getContext())
                     .getReadableDatabase();
 
             c = db.query("moneyboxes", null, "id_moneybox = ?",
                     new String[] { Long.toString(idMoneybox) }, null, null,
                     null);
 
             if (c != null && c.moveToFirst()) {
                 return createMoneybox(c);
             } else {
                 return null;
             }
         } finally {
             if (db != null) {
                 db.close();
             }
         }
     }
 
     /**
      * Add a moneybox to the database
      * 
      * @param m
      *            Moneybox to be added
      * @return The new Moneybox inserted
      */
     public static Moneybox insertMoneybox(Moneybox m) {
         SQLiteDatabase db = null;
 
         try {
             db = new MoneyboxDataHelper(ContextManager.getContext())
                     .getWritableDatabase();
 
             ContentValues values;
 
             values = new ContentValues();
             values.put("description", m.getDescription());
             values.put("creation_date", m.getCreationDateDB());
 
             db.insert("moneyboxes", null, values);
 
             m.setIdMoneybox(getLastIdMoneybox());
 
             return m;
         } finally {
             if (db != null) {
                 db.close();
             }
         }
     }
 
     /**
      * Gets the maximum identifier of the moneyboxes table
      * 
      * @return
      */
     private static long getLastIdMoneybox() {
         return new MoneyboxDataHelper(ContextManager.getContext())
                 .getLastId("moneyboxes");
     }
 
     /**
      * Add a moneybox with specified description to the database. The creation
      * date is the current date.
      * 
      * @param description
      *            Description of the moneybox
      * @return The new Moneybox inserted
      */
     public static Moneybox insertMoneybox(String description) {
         Moneybox m;
 
         m = new Moneybox();
         m.setDescription(description);
         m.setCreationDate(new Date());
 
         return insertMoneybox(m);
     }
 
     /**
      * Saves the values of a moneybox in the database
      * 
      * @param m
      *            Moneybox to be saved
      */
     public static void updateMoneybox(Moneybox m) {
         SQLiteDatabase db = null;
 
         try {
             db = new MoneyboxDataHelper(ContextManager.getContext())
                     .getWritableDatabase();
 
             ContentValues values;
 
             values = new ContentValues();
             values.put("description", m.getDescription());
             values.put("creation_date", m.getCreationDateDB());
 
             db.update("moneyboxes", values, "id_moneybox = ?",
                     new String[] { Long.toString(m.getIdMoneybox()) });
         } finally {
             if (db != null) {
                 db.close();
             }
         }
     }
 
     /**
      * Delete a moneybox from the database
      * 
      * @param m
      *            Moneybox to be deleted
      */
     public static void deleteMoneybox(Moneybox m) {
         SQLiteDatabase db = null;
 
         try {
             db = new MoneyboxDataHelper(ContextManager.getContext())
                     .getWritableDatabase();
 
             db.delete("moneyboxes", "id_moneybox = ?",
                     new String[] { Long.toString(m.getIdMoneybox()) });
         } finally {
             if (db != null) {
                 db.close();
             }
         }
     }
     
     public static double getMoneyboxTotal(Moneybox m) {
         double result;
         ArrayList<Movement> movements;
         
         result = 0.0;
         movements = MovementsManager.getActiveMovements(m);
         for (Movement mov : movements) {
             result += mov.getAmount();
         }
         
         return result;
     }
 }
