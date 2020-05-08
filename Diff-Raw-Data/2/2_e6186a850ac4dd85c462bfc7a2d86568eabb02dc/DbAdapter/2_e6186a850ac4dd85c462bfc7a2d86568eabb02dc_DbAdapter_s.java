 package eu.spyropoulos.android.oldweather;
 
 import static eu.spyropoulos.android.oldweather.OldWeatherApp.TAG;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 import java.util.ArrayList;
 
 public class DbAdapter {
     public static final String DATABASE_NAME = "oldWeatherData";
     private static final int DATABASE_VERSION = 1;
     private static final String SQL_DB_CREATE_USERS =
         "CREATE TABLE users (" +
         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
         "login TEXT NOT NULL, " +
         "password TEXT)";
     private static final String SQL_DB_DROP_USERS = "DROP TABLE IF EXISTS users";
     private static final String SQL_DB_CREATE_SHIPS =
         "CREATE TABLE ships (" +
         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
         "name TEXT NOT NULL, " +
        "transcriber INTEGER NOT NULL))";
     private static final String SQL_DB_DROP_SHIPS = "DROP TABLE IF EXISTS ships";
 
     private DatabaseHelper mDbHelper;
     private final Context mCtx;
     private SQLiteDatabase mDb;
 
     private static class DatabaseHelper extends SQLiteOpenHelper {
         DatabaseHelper(Context ctx) {
             super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             db.execSQL(SQL_DB_CREATE_USERS);
             db.execSQL(SQL_DB_CREATE_SHIPS);
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             db.execSQL(SQL_DB_DROP_USERS);
             db.execSQL(SQL_DB_DROP_SHIPS);
             db.execSQL(SQL_DB_CREATE_USERS);
             db.execSQL(SQL_DB_CREATE_SHIPS);
         }
     }
 
     /**
      * Constructor - takes the context to allow the database to be opened/created
      *
      * @param ctx the Context within which to work
      */
     public DbAdapter(Context ctx) {
         this.mCtx = ctx;
     }
 
     /**
      * Open the database.
      * If it cannot be opened, try to create a new instance of the database. If it cannot
      * be created, throw an exception to signal the failure
      * 
      * @return this (self reference, allowing this to be chained in an initialization call)
      * @throws SQLException if the database could be neither opened or created
      */
     public DbAdapter open() throws SQLException {
         mDbHelper = new DatabaseHelper(mCtx);
         mDb = mDbHelper.getWritableDatabase();
         return this;
     }
 
     /**
      * Close the database.
      */
     public void close() {
         mDbHelper.close();
     }
 
     /* Users CRUD */
     /**
      * Create a new user.
      *
      * @param login the login name for connecting to the server
      * @param password the password used for login in the server
      */
     public void createUser(String username, String password) {
         ContentValues cv = new ContentValues();
         cv.put("login", username);
         cv.put("password", password);
         mDb.insert("users", null, cv);
     }
 
     /**
      * Return a Cursor pointing at the user that matches the given login
      *
      * @param login the login of the user to retrieve
      * @return Cursor pointing at the matching user, if found
      * @throws SQLException if user could not be found/retrieved
      */
     public Cursor fetchUser(String login) throws SQLException {
         Cursor cur = mDb.query("users", new String[] {"login", "password"}, "login = '" + login + "'",
                 null, null, null, null);
         cur.moveToFirst();
         return cur;
     }
 
     /* Ships CRUD */
     /**
      * Create a new ship.
      *
      * @param name the name of the ship
      * @param user the user that transcribes this ship's logs
      */
     public void createShip(String name, String user) {
         ContentValues cv = new ContentValues();
         cv.put("name", name);
         cv.put("transcriber", user);
         mDb.insert("ships", null, cv);
     }
 
     /**
      * Retrieve all ships associated with a user.
      *
      * @param userId the id of the user whose list of ships are going to be returned
      * @return a list of String (for now) containing the associated ships' names
      */
     public ArrayList<String> fetchAllShips(long userId) {
         ArrayList<String> results = new ArrayList<String>();
         Cursor cur = null;
 
         try {
             String args[] = new String[1];
             args[0] = String.valueOf(userId);
             cur = mDb.rawQuery("SELECT name FROM ships WHERE transcriber = ?", new String[] {String.valueOf(userId)});
             while(cur.moveToNext()) {
                 results.add(cur.getString(0));
             }
         } catch (SQLException e) {
             Log.e(TAG, "fetchAllShips: Error while retrieving all ships of user with ID " + userId + ": " +
                     e.toString());
         } finally {
             if (cur != null) {
                 cur.close();
             }
         }
         return results;
     }
 
 }
 
 
