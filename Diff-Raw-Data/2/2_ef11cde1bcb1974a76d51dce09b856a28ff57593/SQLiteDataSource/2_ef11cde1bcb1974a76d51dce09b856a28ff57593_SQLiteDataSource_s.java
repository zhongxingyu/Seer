 package com.teamluper.luper;
 
 // TODO: generalize this to all database tables, and do something more clever with the cursors.
 // TODO: instead of selection by id, use composite key of id and ownerUserId
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class SQLiteDataSource {
   // Database fields
   private SQLiteDatabase database = null;
   private SQLiteHelper dbHelper;
   private long activeUserID; // TODO change this on register / login
 
   public SQLiteDataSource(Context context) {
     this(context, -1);
   }
   public SQLiteDataSource(Context context, long userID) {
     dbHelper = new SQLiteHelper(context);
     activeUserID = userID;
   }
 
   public boolean isOpen() {
     return database != null;
   }
 
   public void open() throws SQLException {
     database = dbHelper.getWritableDatabase();
   }
 
   public void close() {
     dbHelper.close();
   }
 
   public User createUser(long id, String username, String email) {
     ContentValues values = new ContentValues();
     values.put("_id", id);
     values.put("username", username);
     values.put("email", email);
     values.put("isActiveUser", 1);
     values.put("isDirty", 1);
     long insertId = database.insert("Users", null, values);
     return getUserById(insertId);
   }
   public User getUserById(long id) {
     return getUserWhere("_id = " + id);
   }
   public User getUserByEmail(String email) {
    return getUserWhere("email = " + email);
   }
   public User getUserWhere(String where) {
     Cursor cursor = database.query("Users", null,
       where, null, null, null, null);
     cursor.moveToFirst();
     User u = cursorToUser(cursor);
     cursor.close();
     return u;
   }
   public void deleteUser(long userID) {
 	  database.delete("Users", "_id = " + userID, null);
   }
 
   public User getActiveUser() {
     Cursor cursor = database.query("Users", null,
       "isActiveUser = 1", null, null, null, null);
     int numResults = cursor.getCount();
     if(numResults < 1) return null;
     cursor.moveToFirst();
     User u = cursorToUser(cursor);
     cursor.close();
     return u;
   }
   public void setActiveUser(User user) {
     ContentValues values = new ContentValues();
     values.put("isActiveUser", 1);
     database.update("Users", values, "_id = "+user.getId(), null);
   }
   public void logoutActiveUser() {
     ContentValues values = new ContentValues();
     values.put("isActiveUser", 0);
     database.update("Users", values, "isActiveUser = 1", null);
   }
 
   public Sequence createSequence(User owner, String title) {
     if(title == "") title = "Untitled";
     ContentValues values = new ContentValues();
     values.put("ownerUserID", owner.getId());
     values.put("title", title);
     values.put("isDirty", 1);
     long insertId = database.insert("Sequences", null, values);
     return getSequenceById(insertId);
   }
 
   public Sequence getSequenceById(long id) {
     Cursor cursor = database.query("Sequences", null,
       "_id = " + id, null, null, null, null);
     cursor.moveToFirst();
     Sequence s = cursorToSequence(cursor);
     cursor.close();
     return s;
   }
 
   public void deleteSequence(long sequenceID) {
     database.delete("Sequences", "_id = " + sequenceID, null);
   }
 
   public Track createTrack(Sequence parentSequence) {
     ContentValues values = new ContentValues();
     values.put("ownerUserID", parentSequence.getOwnerUserID());
     values.put("parentSequenceID", parentSequence.getId());
     values.put("isMuted", false);
     values.put("isLocked", false);
     values.put("isDirty", 1);
     long insertId = database.insert("Tracks", null, values);
     return getTrackById(insertId);
   }
   public Track getTrackById(long id) {
     Cursor cursor = database.query("Tracks", null,
       "_id = " + id, null, null, null, null);
     cursor.moveToFirst();
     Track t = cursorToTrack(cursor);
     cursor.close();
     return t;
   }
   public void deleteTrack(long trackID) {
 	  database.delete("Tracks", "_id = " + trackID, null);
   }
 
   public AudioFile createAudioFile(User owner, String filePath) {
     ContentValues values = new ContentValues();
     values.put("ownerUserID", owner.getId());
     values.put("clientFilePath", filePath);
     values.put("fileFormat", "3gp");
     values.put("isReadyOnClient", 1);
     values.put("isReadyOnServer", 0);
     values.put("isDirty", 1);
     long insertId = database.insert("Files", null, values);
     return getAudioFileById(insertId);
   }
   public AudioFile getAudioFileById(long id) {
     Cursor cursor = database.query("Files", null,
       "_id = " + id, null, null, null, null);
     AudioFile f = cursorToFile(cursor);
     cursor.close();
     return f;
   }
   public void deleteAudioFile(long fileID) {
 	  database.delete("Files", "_id = " + fileID, null);
   }
 
   public Clip createClip(Track parentTrack, AudioFile file, int startTime) {
     ContentValues values = new ContentValues();
     values.put("ownerUserID", parentTrack.getOwnerUserID());
     values.put("parentTrackID", parentTrack.getId());
     values.put("audioFileID", file.getId());
     values.put("startTime", startTime);
     values.put("loopCount", 1);
     values.put("isLocked", 0);
     values.put("playbackOptions", "{}");
     values.put("isDirty",1);
     long insertId = database.insert("Clips", null, values);
     return getClipById(insertId);
   }
   public Clip getClipById(long id) {
     Cursor cursor = database.query("Clips", null,
       "_id = " + id, null, null, null, null);
     Clip c = cursorToClip(cursor);
     cursor.close();
     return c;
   }
   public void deleteClip(long clipID) {
 	  database.delete("Clips", "_id = " + clipID, null);
   }
 
 
   // more complex queries
   public List<Sequence> getAllSequences() {
     List<Sequence> sequences = new ArrayList<Sequence>();
 
     Cursor cursor = database.query("Sequences", null, null,
         null, null, null, null);
     cursor.moveToFirst();
     while (!cursor.isAfterLast()) {
       Sequence sequence = cursorToSequence(cursor);
       sequences.add(sequence);
       cursor.moveToNext();
     }
     // Make sure to close the cursor
     cursor.close();
     return sequences;
   }
 
   public ArrayList<Track> getTracksBySequenceId(long sequenceId) {
     ArrayList<Track> tracks = new ArrayList<Track>();
     String[] selectionArgs = new String[1];
     selectionArgs[0] = ""+sequenceId;
     Cursor cursor = database.query("Tracks", null, "parentSequenceID = ?",
       selectionArgs, null, null, null);
     cursor.moveToFirst();
     while(!cursor.isAfterLast()) {
       Track track = cursorToTrack(cursor);
       tracks.add(track);
       cursor.moveToNext();
     }
     cursor.close();
     return tracks;
   }
 
   public List<Clip> getClipsByTrackId(long trackId) {
     List<Clip> clips = new ArrayList<Clip>();
     String[] selectionArgs = new String[1];
     selectionArgs[0] = ""+trackId;
     Cursor cursor = database.query("Clips", null, "parentTrackID = ?",
       selectionArgs, null, null, null);
     cursor.moveToFirst();
     while(!cursor.isAfterLast()) {
       Clip clip = cursorToClip(cursor);
       clips.add(clip);
       cursor.moveToNext();
     }
     cursor.close();
     return clips;
   }
 
 
   // database-cursor-to-object conversion
   private User cursorToUser(Cursor cursor) {
     if(cursor.getCount() != 1) return null;
     User user = new User(this,
       cursor.getLong(cursor.getColumnIndex("_id")),
       cursor.getString(cursor.getColumnIndex("username")),
       cursor.getString(cursor.getColumnIndex("email")),
       cursor.getInt(cursor.getColumnIndex("isActiveUser")) == 1,
       cursor.getLong(cursor.getColumnIndex("linkedFacebookID")),
       cursor.getString(cursor.getColumnIndex("preferences")),
       cursor.getInt(cursor.getColumnIndex("isDirty")) == 1
     );
     return user;
   }
 
   private Sequence cursorToSequence(Cursor cursor) {
     if(cursor.getCount() != 1) return null;
     Sequence sequence = new Sequence(this,
       cursor.getLong(cursor.getColumnIndex("_id")),
       cursor.getLong(cursor.getColumnIndex("ownerUserID")),
       cursor.getString(cursor.getColumnIndex("title")),
       cursor.getInt(cursor.getColumnIndex("sharingLevel")),
       cursor.getString(cursor.getColumnIndex("playbackOptions")),
       cursor.getInt(cursor.getColumnIndex("isDirty")) == 1
     );
     return sequence;
   }
   private Track cursorToTrack(Cursor cursor) {
     if(cursor.getCount() != 1) return null;
     Track track = new Track(this,
       cursor.getLong(cursor.getColumnIndex("_id")),
       cursor.getLong(cursor.getColumnIndex("ownerUserID")),
       cursor.getLong(cursor.getColumnIndex("parentSequenceID")),
       cursor.getInt(cursor.getColumnIndex("isMuted")) == 1,
       cursor.getInt(cursor.getColumnIndex("isLocked")) == 1,
       cursor.getString(cursor.getColumnIndex("playbackOptions")),
       cursor.getInt(cursor.getColumnIndex("isDirty")) == 1
     );
     return track;
   }
 
   private Clip cursorToClip(Cursor cursor) {
     if(cursor.getCount() != 1) return null;
     Clip clip = new Clip(this,
       cursor.getLong(cursor.getColumnIndex("_id")),
       cursor.getLong(cursor.getColumnIndex("ownerUserID")),
       cursor.getLong(cursor.getColumnIndex("parentSequenceID")),
       cursor.getLong(cursor.getColumnIndex("audioFileID")),
       cursor.getInt(cursor.getColumnIndex("startTime")),
       cursor.getInt(cursor.getColumnIndex("durationMS")),
       cursor.getInt(cursor.getColumnIndex("loopCount")),
       cursor.getInt(cursor.getColumnIndex("isLocked")) == 1,
       cursor.getString(cursor.getColumnIndex("playbackOptions")),
       cursor.getInt(cursor.getColumnIndex("isDirty")) == 1
     );
     return clip;
   }
 
   private AudioFile cursorToFile(Cursor cursor) {
     if(cursor.getCount() != 1) return null;
     AudioFile file = new AudioFile(this,
       cursor.getLong(cursor.getColumnIndex("_id")),
       cursor.getLong(cursor.getColumnIndex("ownerUserID")),
       cursor.getString(cursor.getColumnIndex("clientFilePath")),
       cursor.getString(cursor.getColumnIndex("serverFilePath")),
       cursor.getString(cursor.getColumnIndex("fileFormat")),
       cursor.getDouble(cursor.getColumnIndex("bitrate")),
       cursor.getDouble(cursor.getColumnIndex("durationMS")),
       cursor.getInt(cursor.getColumnIndex("isReadyOnClient")) == 1,
       cursor.getInt(cursor.getColumnIndex("isReadyOnServer")) == 1,
       cursor.getLong(cursor.getColumnIndex("renderSequenceID")),
       cursor.getInt(cursor.getColumnIndex("isDirty")) == 1
     );
     return file;
   }
 
   public void updateString(String table, long id, String key, String value) {
     ContentValues values = new ContentValues();
     values.put(key, value);
     values.put("isDirty", 1);
     database.update(table, values, "_id = "+id, null);
   }
   public void updateDouble(String table, long id, String key, double value) {
     ContentValues values = new ContentValues();
     values.put(key, value);
     values.put("isDirty", 1);
     database.update(table, values, "_id = "+id, null);
   }
   public void updateLong(String table, long id, String key, long value) {
     ContentValues values = new ContentValues();
     values.put(key, value);
     values.put("isDirty", 1);
     database.update(table, values, "_id = "+id, null);
   }
   public void updateInt(String table, long id, String key, int value) {
     ContentValues values = new ContentValues();
     values.put(key, value);
     values.put("isDirty", 1);
     database.update(table, values, "_id = "+id, null);
   }
 
   // PROCEED WITH CAUTION, THIS DOES EXACTLY WHAT IT SOUNDS LIKE
   public void dropAllData() {
     // forces a drop and recreate of all tables
     dbHelper.onUpgrade(database, 0, dbHelper.getVersion());
   }
 
 }
