 package com.example.firerwar;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DatabaseManager extends SQLiteOpenHelper {
 
 	private static final String DATABASE_NAME ="portdatabase";
 	
 	private static final String TABLE_TCP = "tcp";
 	private static final String TABLE_UDP = "udp";
 	
 	private static final int DATABASE_VERSION = 1;
 	private static final int OPEN = 1;
 	private static final int CLOSED = 0;
 	private static final int TCP = 1;
 	private static final int UDP = 0;
 	
 	public static final String PORT_KEY_ID = "_id";
 	public static final String PORT_NUM = "port_num";
 	public static final String PORT_STATUS = "status"; /*open or closed*/
 	
 
 	public static final String DATABASE_DROP_TCP = "drop table if exists " + TABLE_TCP;
 	
 	public static final String DATABASE_DROP_UDP = "drop table if exists " + TABLE_UDP;
 
 	
 
 	
 	   public DatabaseManager(Context context) {
 	        super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	       
 	    }
 		
 /*how to fix the problem
  * 1.) add all the ports */
 	@Override
 	public void onCreate(SQLiteDatabase databoots) {
 		//databoots = this.getWritableDatabase();
 		String CREATE_TCP_TABLE = "CREATE TABLE " + TABLE_TCP
 				+ " (" + PORT_KEY_ID + " INTEGER PRIMARY KEY, " +
 				PORT_NUM + " INTEGER UNIQUE, " +
 				PORT_STATUS+" INTEGER "+")";
 		
 		String CREATE_UDP_TABLE = "CREATE TABLE " + TABLE_UDP
 				+ " (" + PORT_KEY_ID + " INTEGER PRIMARY KEY, " +
 				PORT_NUM + " INTEGER UNIQUE, " +
 				PORT_STATUS+" INTEGER "+")";
 		
 		Log.d("Database","created table to databoots");
 		databoots.execSQL(CREATE_TCP_TABLE);
 		databoots.execSQL(CREATE_UDP_TABLE);
 								 
 		
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase databoots, int oldVersion, int newVersion) {
 		
 		Log.w("DatabaseManager.java", "Database being upgraded from " + Integer.toString(oldVersion)
 				+" to " + Integer.toString(newVersion));
 		databoots.execSQL(DATABASE_DROP_TCP);
 		databoots.execSQL(DATABASE_DROP_UDP);
 		onCreate(databoots);
 		
 	}
 	/*add status to this*/
 	public void addPort(int port,int port_status,int portType) {
 		
 		SQLiteDatabase databoots = this.getWritableDatabase();
 		ContentValues vals = new ContentValues();
 		vals.put(PORT_NUM, port);
 		vals.put(PORT_STATUS,port_status);
 		Log.d("Database","added port to databoots");
 
 		if(TCP == portType) {
 			databoots.insert(TABLE_TCP, null, vals);
 		}
 		else if(UDP == portType) {
 			databoots.insert(TABLE_UDP, null, vals);
 		}
 		databoots.close();
 		
 	}
 	
 	public int getPort (int id,int portType) {
 		SQLiteDatabase databoots = this.getReadableDatabase();
 		
 		Cursor cursor = null;
 		
 		if (TCP == portType) {
 			cursor = databoots.query(TABLE_TCP, new String[] {PORT_KEY_ID,PORT_NUM},
 				PORT_KEY_ID + "=?",new String[] {String.valueOf(id)},null,null,null,null);
 		}
 		else if (UDP == portType) {
 			cursor = databoots.query(TABLE_UDP, new String[] {PORT_KEY_ID,PORT_NUM},
 				PORT_KEY_ID + "=?",new String[] {String.valueOf(id)},null,null,null,null);
 		}
 		
 		if (cursor != null)
 			cursor.moveToFirst();
 		/* retrieves the port number and returns it  */
 		return cursor.getInt(1);
 	}
 	/*need to appened more data so you can see what type of port */
 	public ArrayList<String> getAllPorts(int portType) {
 		/*Might need to be writable, but not sure why*/
 		SQLiteDatabase databoots = this.getReadableDatabase();
 		ArrayList<String> portList = new ArrayList<String>();
 		String selection = null;
 		
 		if(TCP == portType) {
 			selection = "select * from "+ TABLE_TCP;
 		}
 		else if(UDP == portType) {
 			selection = "select * from "+ TABLE_UDP;
 		}
 		Cursor cursor = databoots.rawQuery(selection, null);
 		
 		if(cursor !=null && cursor.moveToFirst()){
 			do {
 				if(cursor.getInt(2) == OPEN) {
					portList.add(Integer.toString(cursor.getInt(1))+" opened");
 				}
 				else if(cursor.getInt(2) == CLOSED) {
					portList.add(Integer.toString(cursor.getInt(1))+" blocked");
 				}
 			}while (cursor.moveToNext());
 		}
 		
 		return portList;
 	}
 	/*Change this so that it updates the ports from open to closed or vise versa*/
 	public int updatePortStatus(int port,int port_type,int port_status) {
 		SQLiteDatabase databoots = this.getWritableDatabase();
 		int dataReturned = -1;
 		
 		ContentValues vals = new ContentValues();
 		vals.put(PORT_STATUS, port_status);
 		/* might need the id in the list for this value, i believe we can find it, but
 		 * really there should only ever be a need to update a port status
 		 * not a port*/
 		
 		if(TCP == port_type) {
 			dataReturned = databoots.update(TABLE_TCP, vals, PORT_NUM+ " = ?", new String[] {Integer.toString(port)});
 		}
 		else if (UDP == port_type) {
 			dataReturned = databoots.update(TABLE_UDP, vals, PORT_NUM+ " = ?", new String[] {Integer.toString(port)});
 
 		}
 		return dataReturned;
 	}
 	
 	public void deletePort (int delPort,int port_type) {
 		SQLiteDatabase databoots = this.getWritableDatabase();
 		if (port_type == TCP) {
 			databoots.delete(TABLE_TCP, PORT_NUM+ " = ?", new String[] {String.valueOf(delPort)});
 		}
 		else if (port_type == UDP) {
 			databoots.delete(TABLE_UDP, PORT_NUM+ " = ?", new String[] {String.valueOf(delPort)});
 		}
 	}
 	
 	public int getPortCount (int port_type) {
 		SQLiteDatabase databoots = this.getReadableDatabase();
 		String numPorts = "";
 		if(port_type == TCP) {
 			numPorts = "SELECT * FROM " + TABLE_TCP;
 		}
 		else if (port_type == UDP) {
 			numPorts = "SELECT * FROM " + TABLE_UDP;
 
 		}
 		Cursor cursor = databoots.rawQuery(numPorts, null);
 		
 		return cursor.getCount();
 	}
 
 }
