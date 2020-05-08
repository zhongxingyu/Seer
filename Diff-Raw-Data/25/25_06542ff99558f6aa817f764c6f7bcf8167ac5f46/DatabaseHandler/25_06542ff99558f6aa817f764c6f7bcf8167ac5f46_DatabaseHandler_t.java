 /*
  ** Copyright (C)  2012  Tomas Arnesson, Hampus Lilja, Mattias Herzog & Mathias Dos
  ** Permission is granted to copy, distribute and/or modify this document
  ** under the terms of the GNU Free Documentation License, Version 1.3
  ** or any later version published by the Free Software Foundation;
  ** with no Invariant Sections, no Front-Cover Texts, and no Back-Cover Texts.
  ** A copy of the license is included in the section entitled "LICENSE.txt".
  */
 /*
 ** This file is part of ShutApp.
 **
 ** ShutApp is free software: you can redistribute it and/or modify
 ** it under the terms of the GNU General Public License as published by
 ** the Free Software Foundation, either version 3 of the License, or
 ** (at your option) any later version.
 **
 ** ShutApp is distributed in the hope that it will be useful,
 ** but WITHOUT ANY WARRANTY; without even the implied warranty of
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 ** GNU General Public License for more details.
 **
 ** You should have received a copy of the GNU General Public License
 ** along with ShutApp.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.example.shutapp;
 
 import java.io.BufferedInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.util.ByteArrayBuffer;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DatabaseHandler extends SQLiteOpenHelper implements Runnable{
 	
 	// Database Version
     private static final int DATABASE_VERSION = 1;
     
     // Database Name
     private static final String DATABASE_NAME = "chatroomsManager";
     
     // Chatrooms table name
     private static final String TABLE_CHATROOMS = "chatrooms";
     
     // Chatrooms Table Columns names
     //private static final String KEY_ID = "id";
     private static final String KEY_NAME = "NAME";
     private static final String KEY_LATITUDE = "LAT";
     private static final String KEY_LONGITUDE = "LONG";
     private static final String KEY_RADIUS = "RADIUS";
     
 
 	private static final String TAG = "DatabaseHandler";
     
 	private Context mContext;
 	
 	public DatabaseHandler(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
         mContext = context;
         
         
     }
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		/*String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CHATROOMS + "("
 				+ KEY_NAME + " TEXT PRIMARY KEY,"
                 + KEY_LATITUDE + " REAL," + KEY_LONGITUDE + " REAL," + KEY_RADIUS + " INTEGER" + ")" ;
         db.execSQL(CREATE_CONTACTS_TABLE);
 		*/
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// Drop older table if existed
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHATROOMS);
  
         // Create tables again
         onCreate(db);
 		
 	}
 	
 	public void downloadAndCopyDB(){
 		new Thread(this).start();
 	}
 	public void addChatroom(Chatroom chatroom){
 		SQLiteDatabase db = this.getWritableDatabase();
 		 
 	    ContentValues values = new ContentValues();
 	    values.put(KEY_NAME, chatroom.getName()); // Chatroom Name
 	    values.put(KEY_LATITUDE, chatroom.getLatitude()); // Chatroom Latitude
 	    values.put(KEY_LONGITUDE, chatroom.getLongitude()); // Chatroom Longitude
 	    values.put(KEY_RADIUS, chatroom.getRadius()); // Chatroom Radius
 	 
 	    // Inserting Row
 	    db.insert(TABLE_CHATROOMS, null, values);
 	    db.close(); // Closing database connection
 	}
 	
 	public Chatroom getChatroom(String name){
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.query(TABLE_CHATROOMS, new String[] { KEY_NAME,
 				KEY_LATITUDE, KEY_LONGITUDE, KEY_RADIUS }, KEY_NAME + "=?",
 				new String[] { name }, null, null, null, null);
 		if (cursor != null)
 			cursor.moveToFirst();
 		
 		Chatroom chatroom = new Chatroom(cursor.getString(0),
				cursor.getDouble(2), cursor.getDouble(3),
							cursor.getInt(4));
 		// return chatroom
 		return chatroom;
 	}
 
 	public List<Chatroom> getAllChatrooms(){
 		List<Chatroom> chatroomList = new ArrayList<Chatroom>();
 	    // Select All Query
 	    String selectQuery = "SELECT  * FROM " + TABLE_CHATROOMS;
 	 
 	    SQLiteDatabase db = this.getWritableDatabase();
 	    Cursor cursor = db.rawQuery(selectQuery, null);
 	 
 	    // looping through all rows and adding to list
 	    if (cursor.moveToFirst()) {
 	        do {
 	            Chatroom chatroom = new Chatroom();
 	            chatroom.setName(cursor.getString(0));
 	            chatroom.setLatitude(cursor.getDouble(2));
 	            chatroom.setLongitude(cursor.getDouble(3));
 	            chatroom.setRadius(cursor.getInt(4));
 	            // Adding chatroom to list
 	            chatroomList.add(chatroom);
 	        } while (cursor.moveToNext());
 	    }
 	 
 	    // return contact list
 	    return chatroomList;
 	}
 	
 	public int getChatroomsCount(){
 		String countQuery = "SELECT  * FROM " + TABLE_CHATROOMS;
         SQLiteDatabase db = this.getReadableDatabase();
         Cursor cursor = db.rawQuery(countQuery, null);
         if (cursor != null)
 			cursor.moveToFirst();
         int count = cursor.getCount();
         cursor.close();
  
         // return count
         return count;
 	}
 	
 	public int updateChatrooms(Chatroom chatroom){
 		SQLiteDatabase db = this.getWritableDatabase();
 		 
 	    ContentValues values = new ContentValues();
 	    values.put(KEY_NAME, chatroom.getName()); // Chatroom Name
 	    values.put(KEY_LATITUDE, chatroom.getLatitude()); // Chatroom Latitude
 	    values.put(KEY_LONGITUDE, chatroom.getLongitude()); // Chatroom Longitude
 	    values.put(KEY_RADIUS, chatroom.getRadius()); // Chatroom Radius
 	 
 	    // updating row
 	    return db.update(TABLE_CHATROOMS, values, KEY_NAME + " = ?",
 	            new String[] { chatroom.getName() });
 	}
 	
 	public void deleteContact(Chatroom chatroom){
 		SQLiteDatabase db = this.getWritableDatabase();
 	    db.delete(TABLE_CHATROOMS, KEY_NAME + " = ?",
 	            new String[] { chatroom.getName() });
 	    db.close();
 	}
 	
 	private static boolean downloadDatabase(Context context) {
         try {
                 Log.d(TAG, "downloading database");
                 URL url = new URL(MiscResources.SERVER_DB_URL);
                 /* Open a connection to that URL. */
                 URLConnection ucon = url.openConnection();
                 /*
                  * Define InputStreams to read from the URLConnection.
                  */
                 InputStream is = ucon.getInputStream();
                 BufferedInputStream bis = new BufferedInputStream(is);
                 /*
                  * Read bytes to the Buffer until there is nothing more to read(-1).
                  */
                 ByteArrayBuffer baf = new ByteArrayBuffer(50);
                 int current = 0;
                 while ((current = bis.read()) != -1) {
                         baf.append((byte) current);
                 }
 
                 /* Convert the Bytes read to a String. */
                 FileOutputStream fos = null;
                 // Select storage location
                 fos = context.openFileOutput("db_name.s3db", Context.MODE_PRIVATE); 
 
                 fos.write(baf.toByteArray());
                 fos.close();
                 Log.d(TAG, "downloaded");
         } catch (IOException e) {
                 Log.e(TAG, "downloadDatabase Error: " , e);
                 return false;
         }  catch (NullPointerException e) {
                 Log.e(TAG, "downloadDatabase Error: " , e);
                 return false;
         } catch (Exception e){
                 Log.e(TAG, "downloadDatabase Error: " , e);
                 return false;
         }
         return true;
 	}
 	/**
      * Copies your database from your local downloaded database that is copied from the server 
      * into the just created empty database in the
      * system folder, from where it can be accessed and handled.
      * This is done by transfering bytestream.
      * */
         private void copyServerDatabase() {
             // by calling this line an empty database will be created into the default system path
             // of this app - we will then overwrite this with the database from the server
             SQLiteDatabase db = getReadableDatabase();
             db.close();
 
 
                 OutputStream os = null;
                 InputStream is = null;
                 try {
                         Log.d(TAG, "Copying DB from server version into app");
                         is = mContext.openFileInput("db_name.s3db");
                         os = new FileOutputStream("/data/data/com.example.shutapp/databases/chatroomsManager"); // XXX change this
 
                         copyFile(os, is);
                 } catch (Exception e) {
                         Log.e(TAG, "Server Database was not found - did it download correctly?", e);                          
                 } finally {
                         try {
                                 //Close the streams
                                 if(os != null){
                                         os.close();
                                 }
                                 if(is != null){
                                         is.close();
                                 }
                         } catch (IOException e) {
                                 Log.e(TAG, "failed to close databases");
                         }
                 }
                   // Log.d(TAG, "Done Copying DB from server");
         }
 
 
 
 
      private void copyFile(OutputStream os, InputStream is) throws IOException {
     	    Log.d(TAG, "copy method runs");
             byte[] buffer = new byte[1024];
             int length;
             while((length = is.read(buffer))>0){
                     os.write(buffer, 0, length);
             }
             os.flush();
     }
 	public void run() {
 		Log.d("Thread", "kommer hit");
 		downloadDatabase(mContext);
         copyServerDatabase();
         Log.d("Thread", "copying done");
 	}
 }
