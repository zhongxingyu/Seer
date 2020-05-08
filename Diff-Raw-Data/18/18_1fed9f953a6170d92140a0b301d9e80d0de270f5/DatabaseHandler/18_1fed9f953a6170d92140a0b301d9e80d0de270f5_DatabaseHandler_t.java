 package be.drupalcamp.leuven;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class DatabaseHandler extends SQLiteOpenHelper {
 
     // Database Version.
     private static final int DATABASE_VERSION = 1;
 
     // Database name.
     private static final String DATABASE_NAME = "DrupalCamp";
 
     // Sessions table name.
     public static final String TABLE_SESSIONS = "sessions";
 
     // Sessions table column names.
     public static final String SESSIONS_KEY_ID = "id";
     public static final String SESSIONS_KEY_TITLE = "name";
     public static final String SESSIONS_KEY_DESCRIPTION = "description";
     public static final String SESSIONS_KEY_SPECIAL = "special";
     public static final String SESSIONS_KEY_START_DATE = "start_date";
     public static final String SESSIONS_KEY_END_DATE = "end_date";
     public static final String SESSIONS_KEY_LEVEL = "level";
     public static final String SESSIONS_KEY_DAY = "day";
 
     // Speaker table name.
     public static final String TABLE_SPEAKERS = "speakers";
 
     // Speakers table column names.
     public static final String SPEAKERS_KEY_ID = "id";
     public static final String SPEAKERS_KEY_USERNAME = "username";
     public static final String SPEAKERS_KEY_FIRSTNAME = "firstname";
     public static final String SPEAKERS_KEY_LASTNAME = "lastname";
     public static final String SPEAKERS_KEY_ORG = "organisation";
     public static final String SPEAKERS_KEY_TWITTER = "twitter";
     public static final String SPEAKERS_KEY_AVATAR = "avatar";
 
     // Speakers x sessions table.
     public static final String TABLE_SPEAKERS_SESSIONS = "speakers_sessions";
 
     // Speakers x sessions table column names.
     public static final String SPEAKERS_SESSIONS_KEY_SESSION_ID = "cross_session_id";
     public static final String SPEAKERS_SESSIONS_KEY_SPEAKER_ID = "cross_speaker_id";
 
     // Favorites table name.
     public static final String TABLE_FAVORITES = "favorites";
 
     // Favorites table column names.
     public static final String FAVORITES_KEY_ID = "fav_id";
 
 
     public DatabaseHandler(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
 
         String CREATE_SESSIONS_TABLE = "CREATE TABLE " + TABLE_SESSIONS + "(" +
                 "" + SESSIONS_KEY_ID + " INTEGER PRIMARY KEY," +
                 "" + SESSIONS_KEY_TITLE + " TEXT," +
                 "" + SESSIONS_KEY_DESCRIPTION + " TEXT," +
                 "" + SESSIONS_KEY_SPECIAL + " INTEGER," +
                 "" + SESSIONS_KEY_START_DATE + " INTEGER," +
                 "" + SESSIONS_KEY_END_DATE + " INTEGER," +
                 "" + SESSIONS_KEY_LEVEL + " INTEGER," +
                 "" + SESSIONS_KEY_DAY + " INTEGER" +
                 ")";
         db.execSQL(CREATE_SESSIONS_TABLE);
 
         String CREATE_SPEAKERS_TABLE = "CREATE TABLE " + TABLE_SPEAKERS + "(" +
                 "" + SPEAKERS_KEY_ID + " INTEGER PRIMARY KEY," +
                 "" + SPEAKERS_KEY_USERNAME + " TEXT," +
                 "" + SPEAKERS_KEY_FIRSTNAME + " TEXT," +
                 "" + SPEAKERS_KEY_LASTNAME + " TEXT," +
                 "" + SPEAKERS_KEY_ORG + " TEXT," +
                 "" + SPEAKERS_KEY_TWITTER + " TEXT," +
                 "" + SPEAKERS_KEY_AVATAR + " TEXT" +
                 ")";
         db.execSQL(CREATE_SPEAKERS_TABLE);
 
         String CREATE_SPEAKERS_SESSIONS_TABLE = "CREATE TABLE " + TABLE_SPEAKERS_SESSIONS + "(" +
                 "" + SPEAKERS_SESSIONS_KEY_SESSION_ID + " INTEGER," +
                 "" + SPEAKERS_SESSIONS_KEY_SPEAKER_ID + " INTEGER" +
                 ")";
         db.execSQL(CREATE_SPEAKERS_SESSIONS_TABLE);
 
         String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "(" +
                 "" + FAVORITES_KEY_ID + " INTEGER" +
                 ")";
         db.execSQL(CREATE_FAVORITES_TABLE);
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         // No upgrades.
     }
 
     /**
      * Save the favorite.
      *
      * @param favorite
      *   The favorite status (either 0 or 1).
      * @param sessionId
      *   The id of the session.
      */
     public void saveFavorite(int favorite, int sessionId) {
         SQLiteDatabase db = this.getWritableDatabase();
 
         // Always delete first.
         assert db != null;
         db.delete(TABLE_FAVORITES, FAVORITES_KEY_ID + " = ?",
                 new String[] { "" + sessionId });
 
         // Insert if favorite is 1.
         if (favorite == 1) {
             ContentValues values = new ContentValues();
             values.put(FAVORITES_KEY_ID, sessionId);
             db.insert(TABLE_FAVORITES, null, values);
         }
 
         db.close();
     }
 
     /**
      * Truncate the session and speaker, this only happens for update.
      */
     public void truncateTable() {
         SQLiteDatabase db = this.getWritableDatabase();
         db.delete(TABLE_SESSIONS, null, null);
         db.delete(TABLE_SPEAKERS, null, null);
         db.delete(TABLE_SPEAKERS_SESSIONS, null, null);
         db.close();
     }
 
     /**
      * Get number of sessions.
      */
     public int getSessionCount() {
         SQLiteDatabase db = this.getReadableDatabase();
         Cursor dataCount = db.rawQuery("select * from " + TABLE_SESSIONS, null);
         int count = dataCount.getCount();
         db.close();
         return count;
     }
 
     /**
      * Get sessions.
      *
      * @param day
      *   The day to get sessions for.
      *
      * @return <List>Session
      *   A list of sessions.
      */
     public List<Session> getSessions(Integer day) {
         List<Session> sessionList = new ArrayList<Session>();
 
         String sessionsQuery = "SELECT * FROM " + DatabaseHandler.TABLE_SESSIONS;
         sessionsQuery += " ts LEFT JOIN " + DatabaseHandler.TABLE_FAVORITES + " tf ON ts." + DatabaseHandler.SESSIONS_KEY_ID + " = tf." + DatabaseHandler.FAVORITES_KEY_ID;
         sessionsQuery += " WHERE " + DatabaseHandler.SESSIONS_KEY_DAY + " = " + day;
         sessionsQuery += " ORDER BY " + DatabaseHandler.SESSIONS_KEY_START_DATE + " ASC, " + DatabaseHandler.SESSIONS_KEY_TITLE + " ASC";
 
         SQLiteDatabase db = this.getWritableDatabase();
         Cursor sessionCursor = db.rawQuery(sessionsQuery, null);
 
         // Loop through all session results.
         if (sessionCursor.moveToFirst()) {
             do {
 
                 // Get speakers.
                 List<Speaker> speakerList = this.getSessionSpeakers(sessionCursor.getInt(0));
 
                 Session session = new Session(
                     sessionCursor.getInt(0),
                     sessionCursor.getString(1),
                     sessionCursor.getString(2),
                     sessionCursor.getInt(3),
                     sessionCursor.getInt(4),
                     sessionCursor.getInt(5),
                     sessionCursor.getInt(6),
                     sessionCursor.getInt(7),
                     sessionCursor.getInt(8),
                     speakerList
                 );
 
                 // Add session to list.
                 sessionList.add(session);
 
             }
             while (sessionCursor.moveToNext());
         }
 
         db.close();
 
         return sessionList;
     }
 
     /**
      * Get speakers per session.
      *
      * @param sessionId
      *   The id of the session to get speakers for.
      *
      * @return <List>Speaker
      *   A list of speakers.
      */
     public List<Speaker> getSessionSpeakers(Integer sessionId) {
         SQLiteDatabase db = this.getWritableDatabase();
         List<Speaker> speakerList = new ArrayList<Speaker>();
         String speakersQuery = "SELECT * FROM " + DatabaseHandler.TABLE_SPEAKERS_SESSIONS + " INNER JOIN " + TABLE_SPEAKERS;
         speakersQuery += " WHERE " + TABLE_SPEAKERS + "." + DatabaseHandler.SPEAKERS_KEY_ID  + " = " + TABLE_SPEAKERS_SESSIONS + "." + DatabaseHandler.SPEAKERS_SESSIONS_KEY_SPEAKER_ID;
         speakersQuery += " AND " + DatabaseHandler.SPEAKERS_SESSIONS_KEY_SESSION_ID + " = " + sessionId;
 
         Cursor speakerCursor = db.rawQuery(speakersQuery, null);
 
         // Loop through all speaker results.
         if (speakerCursor.moveToFirst()) {
             do {
 
                 Log.d("speakersQuery", "" + speakerCursor.getString(3));
 
                 Speaker speaker = new Speaker(
                     speakerCursor.getInt(2),
                     speakerCursor.getString(3),
                     speakerCursor.getString(4),
                     speakerCursor.getString(5),
                     speakerCursor.getString(6),
                     speakerCursor.getString(7),
                     speakerCursor.getString(8)
                 );
 
                 // Add session to list.
                 speakerList.add(speaker);
             }
             while (speakerCursor.moveToNext());
         }
         db.close();
 
         return speakerList;
     }
 
     /**
      * Get sessions from a speaker.
      *
      * @param speakerId
      *   The id of the speaker to get sessions for.
      *
      * @return <List>Session
      *   A list of sessions.
      */
     public List<Session> getSpeakerSessions(Integer speakerId) {
         SQLiteDatabase db = this.getWritableDatabase();
         List<Session> sessionList = new ArrayList<Session>();
 
         String sessionsQuery = "SELECT * FROM " + DatabaseHandler.TABLE_SPEAKERS_SESSIONS + " INNER JOIN " + TABLE_SESSIONS;
         sessionsQuery += " WHERE " + TABLE_SESSIONS + "." + DatabaseHandler.SESSIONS_KEY_ID  + " = " + TABLE_SPEAKERS_SESSIONS + "." + DatabaseHandler.SPEAKERS_SESSIONS_KEY_SESSION_ID;
         sessionsQuery += " AND " + DatabaseHandler.SPEAKERS_SESSIONS_KEY_SPEAKER_ID + " = " + speakerId;
         Cursor sessionCursor = db.rawQuery(sessionsQuery, null);
 
         // Loop through all session results.
         if (sessionCursor.moveToFirst()) {
             do {
 
                 Session session = new Session(
                         sessionCursor.getInt(2),
                         sessionCursor.getString(3),
                         sessionCursor.getString(4),
                         sessionCursor.getInt(5),
                         sessionCursor.getInt(6),
                         sessionCursor.getInt(7),
                         sessionCursor.getInt(8),
                         sessionCursor.getInt(9),
                         0
                 );
 
                 sessionList.add(session);
             }
             while (sessionCursor.moveToNext());
         }
         db.close();
 
         return sessionList;
     }
 
     /**
      * Get speakers.
      *
      * @return <List>Speaker
      *   A list of speakers.
      */
     public List<Speaker> getSpeakers() {
         SQLiteDatabase db = this.getWritableDatabase();
         List<Speaker> speakerList = new ArrayList<Speaker>();
         String speakersQuery = "SELECT * FROM " + DatabaseHandler.TABLE_SPEAKERS + " ORDER BY " + DatabaseHandler.SPEAKERS_KEY_FIRSTNAME + " ASC";
         Cursor speakerCursor = db.rawQuery(speakersQuery, null);
 
         // Loop through all speaker results.
         if (speakerCursor.moveToFirst()) {
             do {
 
                 Speaker speaker = new Speaker(
                         speakerCursor.getInt(0),
                         speakerCursor.getString(1),
                         speakerCursor.getString(2),
                         speakerCursor.getString(3),
                         speakerCursor.getString(4),
                         speakerCursor.getString(5),
                         speakerCursor.getString(6)
                 );
 
                 // Add session to list.
                 speakerList.add(speaker);
             }
             while (speakerCursor.moveToNext());
         }
         db.close();
 
         return speakerList;
     }
 
     /**
      * Insert a session into the database.
      *
      * @param session
      *   A full session object.
      */
     public void insertSession(Session session) {
         SQLiteDatabase db = this.getWritableDatabase();
 
         ContentValues values = new ContentValues();
         values.put(SESSIONS_KEY_ID, session.getId());
         values.put(SESSIONS_KEY_TITLE, session.getTitle());
         values.put(SESSIONS_KEY_DESCRIPTION, session.getDescription());
         values.put(SESSIONS_KEY_SPECIAL, session.getSpecial());
         values.put(SESSIONS_KEY_START_DATE, session.getStartDate());
         values.put(SESSIONS_KEY_END_DATE, session.getEndDate());
         values.put(SESSIONS_KEY_LEVEL, session.getLevel());
         values.put(SESSIONS_KEY_DAY, session.getDay());
 
         db.insert(TABLE_SESSIONS, null, values);
         db.close();
     }
 
     /**
      * Insert a speaker into the database.
      *
      * @param speaker
      *   A full speaker object.
      */
     public void insertSpeaker(Speaker speaker) {
 
         int count = this.getSpeakerCount(speaker.getId());
         if (count == 0) {
             SQLiteDatabase db = this.getWritableDatabase();
 
             ContentValues values = new ContentValues();
             values.put(SPEAKERS_KEY_ID, speaker.getId());
             values.put(SPEAKERS_KEY_USERNAME, speaker.getUsername());
             values.put(SPEAKERS_KEY_FIRSTNAME, speaker.getFirstName());
             values.put(SPEAKERS_KEY_LASTNAME, speaker.getLastName());
             values.put(SPEAKERS_KEY_ORG, speaker.getOrganisation());
             values.put(SPEAKERS_KEY_TWITTER, speaker.getTwitter());
             values.put(SPEAKERS_KEY_AVATAR, speaker.getAvatar());
 
             db.insert(TABLE_SPEAKERS, null, values);
             db.close();
         }
     }
 
     /**
      * Insert a speaker + session into the database.
      *
      * @param sessionId
      *   The id of the sessions.
      * @param speakerId
      *   The id of the speaker.
      */
     public void insertSpeakerSession(Integer sessionId, Integer speakerId) {
         SQLiteDatabase db = this.getWritableDatabase();
 
         ContentValues values = new ContentValues();
         values.put(SPEAKERS_SESSIONS_KEY_SESSION_ID, sessionId);
         values.put(SPEAKERS_SESSIONS_KEY_SPEAKER_ID, speakerId);
         db.insert(TABLE_SPEAKERS_SESSIONS, null, values);
         db.close();
     }
 
     /**
      * Get a single session.
      *
      * @param sessionId
      *   The id of the session.
      *
      * @return Session session
      *   A full session object.
      */
     public Session getSession(int sessionId) {
         SQLiteDatabase db = this.getReadableDatabase();
 
         assert db != null;
         String selectQuery = "SELECT * FROM " + TABLE_SESSIONS;
         selectQuery += " te LEFT JOIN " + DatabaseHandler.TABLE_FAVORITES + " tf ON te." + DatabaseHandler.SESSIONS_KEY_ID + " = tf." + DatabaseHandler.FAVORITES_KEY_ID + " ";
         selectQuery += " WHERE " + SESSIONS_KEY_ID + " = " + sessionId;
         Cursor cursor = db.rawQuery(selectQuery, null);
 
         if (cursor != null) {
             cursor.moveToFirst();
         }
 
         // Get speakers.
         List<Speaker> speakerList = this.getSessionSpeakers(cursor.getInt(0));
 
         assert cursor != null;
         return new Session(
                 cursor.getInt(0),
                 cursor.getString(1),
                 cursor.getString(2),
                 cursor.getInt(3),
                 cursor.getInt(4),
                 cursor.getInt(5),
                 cursor.getInt(6),
                 cursor.getInt(7),
                 cursor.getInt(8),
                 speakerList
         );
     }
 
     /**
      * Get a single speaker.
      *
      * @param speakerId
      *   The id of the speaker.
      *
      * @return Speaker speaker | null if the speaker does not exists.
      *   A full speaker object.
      */
     public Speaker getSpeaker(int speakerId) {
         SQLiteDatabase db = this.getReadableDatabase();
 
         assert db != null;
         String selectQuery = "SELECT * FROM " + TABLE_SPEAKERS + " WHERE " + SPEAKERS_KEY_ID + " = " + speakerId;
         Cursor cursor = db.rawQuery(selectQuery, null);
 
         cursor.moveToFirst();
         return new Speaker(
                 cursor.getInt(0),
                 cursor.getString(1),
                 cursor.getString(2),
                 cursor.getString(3),
                 cursor.getString(4),
                 cursor.getString(5),
                 cursor.getString(6)
         );
     }
 
     /**
      * Get speaker count.
      *
      * @param speakerId
      *   The id of the speaker.
      *
      * @return int 0
      */
     public int getSpeakerCount(int speakerId) {
         SQLiteDatabase db = this.getReadableDatabase();
         String selectQuery = "SELECT * FROM " + TABLE_SPEAKERS + " WHERE " + SPEAKERS_KEY_ID + " = " + speakerId;
         Cursor cursor = db.rawQuery(selectQuery, null);
         int count = cursor.getCount();
         return count;
     }
 }
