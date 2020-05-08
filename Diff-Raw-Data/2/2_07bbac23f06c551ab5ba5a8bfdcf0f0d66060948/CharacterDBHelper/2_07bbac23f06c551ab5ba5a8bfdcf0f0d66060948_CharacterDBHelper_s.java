 package edu.mines.alterego;
 
 import android.content.Context;
 import android.content.ContentValues;
 
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.Cursor;
 
 import android.util.Log;
 import android.util.Pair;
 import android.widget.ListView;
 
 import java.util.Date;
 import java.util.ArrayList;
 
 /**
  *  <h1>SQLite Database Adapter (helper as Google/Android calls it)</h1>
  *
  *  Offers many static functions that can be used to update or view game-statistics in the database
  *  The API follows the general rule of first aquiring the database via 'getWritable/ReadableDatabase',
  *  then using the static functions defined in this class to interact with the database.
  *
  *  @author: Matt Buland
  */
 public class CharacterDBHelper extends SQLiteOpenHelper {
 
     private static final String DB_NAME = "";
     private static final int DB_VERSION = 1;
 
     public CharacterDBHelper(Context context) {
         super(context, DB_NAME, null, DB_VERSION);
     }
 
     /**
      * For an SQLiteOpenHelper, the onCreate method is called if and only if
      * the database-name in question does not already exist. Theoretically,
      * this should only happen once ever, and after the one time, updates
      * will be applied for schema updates.
      */
     @Override
     public void onCreate(SQLiteDatabase database) {
 
         /*
          * Game table: Base unifying game_id construct
          * The game is used to reference 
          */
         database.execSQL("CREATE TABLE IF NOT EXISTS game ( " +
                 "game_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                 "name TEXT," +
                 ")");
 
 
         database.execSQL("CREATE TABLE IF NOT EXISTS character ( " +
                 "character_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                 "game_id INTEGER," +
                 "name TEXT," +
                 "description TEXT," +
                 "FOREIGN KEY(game_id) REFERECES game(game_id)" +
                 ")");
 
         database.execSQL("CREATE TABLE IF NOT EXISTS inventory_item ( "+
                 "inventory_item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                 "character_id INTEGER," +
                 "FOREIGN KEY(character_id) REFERECES character(character_id)" +
                 ")");
 
         database.execSQL("CREATE TABLE IF NOT EXISTS character_stat ( " +
                 "character_stat_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                 "character_id INTEGER," +
                 "stat_value INTEGER," +
                 "stat_name INTEGER," +
                 "description/usage/etc INTEGER," +
                 "category_id INTEGER," +
                 "FOREIGN KEY(character_id) REFERECES character(character_id)" +
                 "FOREIGN KEY(category_id) REFERECES category(category_id)" +
                 ")");
 
         database.execSQL("CREATE TABLE IF NOT EXISTS item_stat ( " +
                 "item_stat_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "entity_id INTEGER," +
                 "stat_value INTEGER," +
                 "stat_name INTEGER," +
                 "description/usage/etc INTEGER," +
                 "category_id INTEGER," +
                 "FOREIGN KEY(category_id) REFERECES category(category_id)" +
                 "FOREIGN KEY(inventory_item_id) REFERECES inventory_item(inventory_item_id)" +
                 ")");
 
         database.execSQL("CREATE TABLE IF NOT EXISTS category ( " +
                 "category_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                 "category_name TEXT" +
                 ")");
 
         database.execSQL("CREATE TABLE IF NOT EXISTS note ( " +
                 "note_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                 "game_id INTEGER," +
                 "note TEXT" +
                 "FOREIGN KEY(game_id) REFERECES game(game_id)" +
                 ")");
         /* Example DDL from Matt's Quidditch scoring app
         database.execSQL("CREATE TABLE IF NOT EXISTS score ( " +
                 "score_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                 "score_datetime INTEGER, " +
                 "team_id INTEGER, " +   // team_id is a number identifying the team. In this first revision, it will be 0 or 1 for left and right
                 "amount INTEGER, " +
                 "snitch INTEGER, " +
                 "game_id INTEGER, " +
                 "FOREIGN KEY(game_id) REFERENCES game(game_id) )");
          */
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
         // Do nothing.
     }
 
 }
