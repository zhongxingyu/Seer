 package de.team06.psychoapp;
 
 /**
  * Created by Malte on 28.06.13.
  */
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class DatabaseModel {
 
     // Database fields
     private SQLiteDatabase database;
     private MySQLiteHelper dbHelper;
     private Context context;
 
     public DatabaseModel(Context context) {
         dbHelper = new MySQLiteHelper(context);
         this.context = context;
     }
 
     public void open() throws SQLException {
         database = dbHelper.getWritableDatabase();
     }
 
     public void close() {
         dbHelper.close();
     }
 
     /**
      * Create a new SocialInteraction Object and save it to db.
      *
      * @param long   alarmtime
      * @param String code
      * @return SocialInteraction
      */
     public SocialInteraction createSocialInteraction(long alarmtime, String code) {
         // we are saving the alarmtime in sec not in msec
         alarmtime = alarmtime / 1000;
 
         ContentValues values = new ContentValues();
         values.put("alarmtime", alarmtime);
         values.put("code", code);
 
         long insertId = dbHelper.getWritableDatabase().insert(MySQLiteHelper.TABLE_SOCIAL_INTERACTIONS, null,
                 values);
         Cursor cursor = database.query(MySQLiteHelper.TABLE_SOCIAL_INTERACTIONS,
                 MySQLiteHelper.TABLE_SOCIAL_INTERACTIONS_ALL, MySQLiteHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(insertId)},
                 null, null, null);
 
 
         cursor.moveToFirst();
         SocialInteraction newSocialInteraction = cursorToSocialInteraction(cursor);
        // Toast.makeText(this.context, newSocialInteraction.toString(), Toast.LENGTH_SHORT).show();
 
         cursor.close();
         return newSocialInteraction;
     }
 
     /**
      * Get SocialInteraction object from database by ID
      *
      * @param long id
      * @return SocialInteraction
      */
     public SocialInteraction getSocialInteractionByID(long id) {
         Cursor cursor = database.query(MySQLiteHelper.TABLE_SOCIAL_INTERACTIONS,
                 MySQLiteHelper.TABLE_SOCIAL_INTERACTIONS_ALL, MySQLiteHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)},
                 null, null, null);
 
 
         cursor.moveToFirst();
         SocialInteraction newSocialInteraction = cursorToSocialInteraction(cursor);
 
         cursor.close();
         return newSocialInteraction;
     }
 
     /** Get last social interaction from database
      * @return SocialInteraction | null
      */
     public SocialInteraction getLastSocialInteraction() {
         Cursor cursor = database.query(MySQLiteHelper.TABLE_SOCIAL_INTERACTIONS,
                 MySQLiteHelper.TABLE_SOCIAL_INTERACTIONS_ALL, null, null,
                 null, null, MySQLiteHelper.COLUMN_ID + " DESC", "1");
 
         cursor.moveToFirst();
         SocialInteraction newSocialInteraction = cursorToSocialInteraction(cursor);
         cursor.close();
         return newSocialInteraction;
     }
 
     /** Get last unskipped social interaction from database
      * @return SocialInteraction | null
      */
     public SocialInteraction getLastUnskippedSocialInteraction() {
         Cursor cursor = database.query(MySQLiteHelper.TABLE_SOCIAL_INTERACTIONS,
                 MySQLiteHelper.TABLE_SOCIAL_INTERACTIONS_ALL, "skip = ?", new String[]{"0"},
                 null, null, MySQLiteHelper.COLUMN_ID + " DESC", "1");
 
         cursor.moveToFirst();
         SocialInteraction newSocialInteraction = cursorToSocialInteraction(cursor);
         cursor.close();
 
         if(newSocialInteraction == null)
             newSocialInteraction = getLastSocialInteraction();
 
         return newSocialInteraction;
     }
 
     /**
      * Delete given SocialInteraction from database
      *
      * @param socialInteraction
      */
     public void deleteSocialInteraction(SocialInteraction socialInteraction) {
         long id = socialInteraction.getId();
         System.out.println("SocialInteraction with id " + id + " deleted.");
         database.delete(MySQLiteHelper.TABLE_SOCIAL_INTERACTIONS, MySQLiteHelper.COLUMN_ID
                 + " = " + id, null);
     }
 
     public void updateSocialInteraction(SocialInteraction socialInteraction) {
         long id = socialInteraction.getId();
         System.out.println("SocialInteraction with id " + id + " updated.");
         database.update(MySQLiteHelper.TABLE_SOCIAL_INTERACTIONS, socialInteraction.toContentValues(), MySQLiteHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
     }
 
     /**
     * Clear Database and prepare it for next usage
     */
    public void clear() {
        database.execSQL("DROP TABLE IF EXISTS " + MySQLiteHelper.TABLE_SOCIAL_INTERACTIONS);
        dbHelper.onCreate(database);
    }

    /**
      * Get all social interactions from database as list of social interaction objects
      *
      * @return ArrayList<SocialInteraction>
      */
     public List<SocialInteraction> getAllSocialInteractions() {
         List<SocialInteraction> socialInteractionArrayList = new ArrayList<SocialInteraction>();
 
         Cursor cursor = database.query(MySQLiteHelper.TABLE_SOCIAL_INTERACTIONS,
                 MySQLiteHelper.TABLE_SOCIAL_INTERACTIONS_ALL, null, null, null, null, null);
 
         cursor.moveToFirst();
 
         while (!cursor.isAfterLast()) {
             SocialInteraction socialInteraction = cursorToSocialInteraction(cursor);
             socialInteractionArrayList.add(socialInteraction);
             cursor.moveToNext();
         }
         // Make sure to close the cursor
         cursor.close();
         return socialInteractionArrayList;
     }
 
     /**
      * Create a SocialInteraction object from db-cursor
      *
      * @param cursor SQLite-Cursor
      * @return SocialInteraction | null
      */
     private SocialInteraction cursorToSocialInteraction(Cursor cursor) {
         if (cursor.getCount() > 0) {
             SocialInteraction socialInteraction = new SocialInteraction();
 
             socialInteraction.setId(cursor.getInt(cursor.getColumnIndex("_id")));
             socialInteraction.setCode(cursor.getString(1));
             socialInteraction.setAlarmTime(cursor.getInt(2));
             socialInteraction.setResponseTime(cursor.getInt(3));
             socialInteraction.setSkipped(cursor.getInt(4));
             socialInteraction.setNumberOfContacts(cursor.getInt(5));
             socialInteraction.setHours(cursor.getInt(6));
             socialInteraction.setMinutes(cursor.getInt(7));
 
             return socialInteraction;
         } else return null;
     }
 }
