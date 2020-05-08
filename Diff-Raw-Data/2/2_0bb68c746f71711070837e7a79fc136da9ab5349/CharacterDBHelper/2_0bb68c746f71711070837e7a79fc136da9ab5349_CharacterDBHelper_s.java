 package edu.mines.alterego;
 
 import android.content.Context;
 import android.content.ContentValues;
 
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.Cursor;
 
 import android.util.Log;
 
 import java.util.ArrayList;
 
 import edu.mines.alterego.GameData;
 
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
 
     private static final String DB_NAME = "alterego";
     private static final int DB_VERSION = 2;
 
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
                 "name TEXT" +
                 ")");
 
 
         database.execSQL("CREATE TABLE IF NOT EXISTS character ( " +
                 "character_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                 "name TEXT, " +
                 "description TEXT, " +
                 "game_id INTEGER, " +
                 "FOREIGN KEY(game_id) REFERENCES game(game_id) )");
 
         database.execSQL("CREATE TABLE IF NOT EXISTS inventory_item ( "+
                 "inventory_item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                 "character_id INTEGER," +
                 "FOREIGN KEY(character_id) REFERENCES character(character_id)" +
                 ")");
 
         database.execSQL("CREATE TABLE IF NOT EXISTS character_stat ( " +
                 "character_stat_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                 "character_id INTEGER," +
                 "stat_value INTEGER," +
                 "stat_name TEXT," +
                 "description_usage_etc INTEGER," +
                 "category_id INTEGER," +
                 "FOREIGN KEY(character_id) REFERENCES character(character_id)" +
                 "FOREIGN KEY(category_id) REFERENCES category(category_id)" +
                 ")");
 
         database.execSQL("CREATE TABLE IF NOT EXISTS item_stat ( " +
                 "item_stat_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                 "inventory_item_id INTEGER," +
                 "stat_value INTEGER," +
                 "stat_name INTEGER," +
                 "description_usage_etc INTEGER," +
                 "category_id INTEGER," +
                 "FOREIGN KEY(category_id) REFERENCES category(category_id)" +
                 "FOREIGN KEY(inventory_item_id) REFERENCES inventory_item(inventory_item_id)" +
                 ")");
 
         database.execSQL("CREATE TABLE IF NOT EXISTS category ( " +
                 "category_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                 "category_name TEXT" +
                 ")");
 //
 //        database.execSQL("CREATE TABLE IF NOT EXISTS note ( " +
 //                "note_id INTEGER PRIMARY KEY AUTOINCREMENT," +
 //                "game_id INTEGER," +
 //                "note TEXT," +
 //                "FOREIGN KEY(game_id) REFERENCES game(game_id)" +
 //                ")");
         database.execSQL("CREATE TABLE IF NOT EXISTS notes_data ( "+
                 "notes_data_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                 "subject TEXT, " +
                 "description TEXT, " +
                 "character_id INTEGER," +
                 "FOREIGN KEY(character_id) REFERENCES character(character_id)" +
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
         if (newVersion > 1) {
             // Add the name and description columns
             database.execSQL("ALTER TABLE inventory_item ADD COLUMN"+
                     "name TEXT");
             database.execSQL("ALTER TABLE inventory_item ADD COLUMN"+
                     "description TEXT");
             database.execSQL("ALTER TABLE notes_data ADD COLUMN"+
                     "subject TEXT");
             database.execSQL("ALTER TABLE  ADD COLUMN"+
                     "description TEXT");
         }
 
         if (oldVersion == 2) {
             // Verify that the name and description columns exist
             Cursor cursor = database.rawQuery("SELECT * FROM inventory_item LIMIT 0", null);
             if (cursor.getColumnIndex("name") < 0 || cursor.getColumnIndex("description") < 0) {
                 Log.i("AlterEgo::CharacterDBHelper", "The name and description columns didn't exist. Dropping the table, and resetting it");
                 database.execSQL("DROP TABLE inventory_item");
                 database.execSQL("CREATE TABLE IF NOT EXISTS inventory_item ( "+
                         "inventory_item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "name TEXT, " +
                         "description TEXT, " +
                         "character_id INTEGER," +
                         "FOREIGN KEY(character_id) REFERENCES character(character_id)" +
                         ")");
             }
             Cursor cursor2 = database.rawQuery("SELECT * FROM notes_data LIMIT 0", null);
             if (cursor2.getColumnIndex("subject") < 0 || cursor2.getColumnIndex("description") < 0) {
                 Log.i("AlterEgo::CharacterDBHelper", "The name and description columns didn't exist. Dropping the table, and resetting it");
                 database.execSQL("DROP TABLE notes_data");
                 database.execSQL("CREATE TABLE IF NOT EXISTS notes_data ( "+
                         "notes_data_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "subject TEXT, " +
                         "description TEXT, " +
                         "character_id INTEGER," +
                         "FOREIGN KEY(character_id) REFERENCES character(character_id)" +
                         ")");
             }
 
         }
     }
 
     public ArrayList<GameData> getGames() {
         Cursor dbGames = getReadableDatabase().rawQuery("SELECT * from game", null);
         dbGames.moveToFirst();
         ArrayList<GameData> games = new ArrayList<GameData>();
         while( !dbGames.isAfterLast()) {
             games.add(new GameData( dbGames.getInt(0), dbGames.getString(1) ));
             dbGames.moveToNext();
         }
         dbGames.close();
         return games;
     }
 
     public GameData addGame(String name) {
         SQLiteDatabase database = getWritableDatabase();
 
         ContentValues gamevals = new ContentValues();
         gamevals.put("name", name);
 
         long rowid = database.insert("game", null, gamevals);
         String[] args = new String[]{ ""+rowid };
 
         Cursor c = database.rawQuery("SELECT * FROM game WHERE game.ROWID =?", args);
         c.moveToFirst();
 
         return new GameData(c.getInt(c.getColumnIndex("game_id")), c.getString(c.getColumnIndex("name")));
     }
 
     public NotesData addNote(String subject, int char_id) {
         SQLiteDatabase database = getWritableDatabase();
 
         ContentValues notevals = new ContentValues();
         notevals.put("character_id", char_id);
         notevals.put("subject", subject);
 
         long rowid = database.insert("notes_data", null, notevals);
         String[] args = new String[]{ ""+rowid };
 
         Cursor c = database.rawQuery("SELECT * FROM notes_data WHERE notes_data.ROWID =?", args);
         c.moveToFirst();
 
         return new NotesData(c.getInt(c.getColumnIndex("notes_data_id")), c.getString(c.getColumnIndex("subject")), c.getString(c.getColumnIndex("description")));
     }
     
     public int getCharacterIdForGame(int gameId) {
         Cursor cursor = getReadableDatabase().rawQuery("SELECT character_id FROM character WHERE character.game_id = ? LIMIT 1", new String[]{""+gameId});
         cursor.moveToFirst();
         if (cursor.getCount() < 1) {
             return -1;
         } else {
             return cursor.getInt(cursor.getColumnIndex("character_id"));
         }
     }
 
     /* Is this useful?
     public ArrayList<String> getCharacters(int gameID) {
         ArrayList<String> characters = new ArrayList<String>();
         return characters;
     }
     */
 
     public CharacterData addCharacter(int gameID, String name, String desc) {
         SQLiteDatabase database = getWritableDatabase();
 
         ContentValues gamevals = new ContentValues();
         gamevals.put("name", name);
         gamevals.put("description", desc);
         gamevals.put("game_id", gameID);
 
         long rowid = database.insert("character", null, gamevals);
 
         String[] args = new String[]{ ""+rowid };
         Cursor c = database.rawQuery("SELECT * FROM character WHERE character.ROWID =?", args);
         c.moveToFirst();
 
         return new CharacterData(c.getInt(c.getColumnIndex("character_id")), c.getString(c.getColumnIndex("name")), c.getString(c.getColumnIndex("description")));
     }
 
     public CharacterData getCharacter(int charId) {
         Cursor c = getReadableDatabase().rawQuery("SELECT * FROM character WHERE character.character_id = ? LIMIT 1", new String[]{""+charId});
         c.moveToFirst();
         if (c.getCount() < 1) {
             // No character with that id available. That's probably bad.
             return null;
         } else {
             return new CharacterData(c.getInt(c.getColumnIndex("character_id")), c.getString(c.getColumnIndex("name")), c.getString(c.getColumnIndex("description")));
         }
     }
 
     public ArrayList<InventoryItem> getInventoryItems(int characterId) {
 
         // Verify that the name and description columns exist
         // This is done here because
         Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM inventory_item LIMIT 0", null);
         if (cursor.getColumnIndex("name") < 0 || cursor.getColumnIndex("description") < 0) {
             Log.i("AlterEgo::CharacterDBHelper", "The name and description columns didn't exist. Dropping the table, and resetting it");
             SQLiteDatabase database = getWritableDatabase();
             database.execSQL("DROP TABLE inventory_item");
             database.execSQL("CREATE TABLE IF NOT EXISTS inventory_item ( "+
                     "inventory_item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "name TEXT, " +
                     "description TEXT, " +
                     "character_id INTEGER," +
                     "FOREIGN KEY(character_id) REFERENCES character(character_id)" +
                     ")");
         }
 
         Cursor invCursor = getReadableDatabase().rawQuery(
                 "SELECT "+
                     "character.character_id," +
                     "inventory_item.inventory_item_id," +
                     "inventory_item.name AS 'item_name'," +
                     "inventory_item.description AS 'item_description'" +
                 "FROM character " +
                     "INNER JOIN inventory_item ON inventory_item.character_id = character.character_id " +
                 "WHERE character.character_id = ?",
                 new String[]{""+characterId});
         ArrayList<InventoryItem> invList = new ArrayList<InventoryItem>();
         invCursor.moveToFirst();
 
         int iidCol = invCursor.getColumnIndex("inventory_item_id");
         int iNameCol = invCursor.getColumnIndex("item_name");
         int iDescCol = invCursor.getColumnIndex("item_description");
         while (!invCursor.isAfterLast()) {
             invList.add(new InventoryItem(
                         invCursor.getInt(iidCol),
                         invCursor.getString(iNameCol),
                         invCursor.getString(iDescCol)));
             invCursor.moveToNext();
         }
         return invList;
     }
 
     public ArrayList<NotesData> getNotesData(int characterId) {
     	Log.i("AlterEgos::CharacterDBHelper::characterId", "characterId " + characterId);
         // Verify that the name and description columns exist
         // This is done here because
         Cursor notesCursor = getReadableDatabase().rawQuery(
                 "SELECT "+
                     "character.character_id," +
                     "notes_data.notes_data_id," +
                     "notes_data.subject AS 'notes_subject'," +
                     "notes_data.description AS 'notes_description'" +
                 "FROM character " +
                     "INNER JOIN notes_data ON notes_data.character_id = character.character_id " +
                 "WHERE character.character_id = ?",
                 new String[]{""+characterId});
         ArrayList<NotesData> notesList = new ArrayList<NotesData>();
         notesCursor.moveToFirst();
         Log.i("AlterEgos::characterDBHelper::notesCursor", "notesCursor " + notesCursor.getCount());
         int nidCol = notesCursor.getColumnIndex("notes_data_id");
         int nNameCol = notesCursor.getColumnIndex("notes_subject");
         int nDescCol = notesCursor.getColumnIndex("notes_description");
         while (!notesCursor.isAfterLast()) {
             notesList.add(new NotesData(
                         notesCursor.getInt(nidCol),
                         notesCursor.getString(nNameCol),
                         notesCursor.getString(nDescCol)));
             notesCursor.moveToNext();
         }
         return notesList;
     }
 
     public InventoryItem addInventoryItem(int charId, String name, String desc) {
         SQLiteDatabase database = getWritableDatabase();
 
         ContentValues gamevals = new ContentValues();
         gamevals.put("name", name);
         gamevals.put("description", desc);
         gamevals.put("character_id", charId);
 
         long rowid = database.insert("inventory_item", null, gamevals);
 
         String[] args = new String[]{ ""+rowid };
         Cursor c = database.rawQuery("SELECT * FROM inventory_item WHERE inventory_item.ROWID =?", args);
         c.moveToFirst();
 
         return new InventoryItem(c.getInt(c.getColumnIndex("inventory_item_id")), c.getString(c.getColumnIndex("name")), c.getString(c.getColumnIndex("description")));
     }
 
 /*            database.execSQL("CREATE TABLE IF NOT EXISTS character_stat ( " +
                 "character_stat_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                 "character_id INTEGER," +
                 "stat_value INTEGER," +
                 "stat_name TEXT," +
                 "description_usage_etc INTEGER," +
                 "category_id INTEGER," +
                 "FOREIGN KEY(character_id) REFERENCES character(character_id)" +
                 "FOREIGN KEY(category_id) REFERENCES category(category_id)" +
                 ")");
 */
     public void insertCharStat(int charID, int statVal, String statName, int category) {
         SQLiteDatabase db = getWritableDatabase();
         ContentValues statVals = new ContentValues();
         statVals.put("character_id", charID);
         statVals.put("stat_value", statVal);
        statVals.put("stat_name", stat_name);
         statVals.put("category_id", category);
 
         db.insert("character_stat", null, statVals);
     }
 }
