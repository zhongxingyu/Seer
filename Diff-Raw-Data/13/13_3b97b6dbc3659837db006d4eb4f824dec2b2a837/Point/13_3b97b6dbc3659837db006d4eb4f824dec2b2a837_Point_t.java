 package com.remote.phonemodemanager;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 public class Point {
 	long id;
 	String name;
 	double x;
 	double y;
 	int radius;
 	int mode;
 	String ringtone;
 	int connection;
 	
     
     private Point() {}
     
     public Point(final String name, final double x, final double y, final int radius, final int mode, final String ringtone, final int connection  ) {
     	this.name = name;
     	this.x = x;
     	this.y = y;
     	this.radius = radius;
     	this.mode = mode;
     	this.ringtone = ringtone;
     	this.connection = connection;
     }
     
     public void save(DatabaseHelper dbHelper) {
             final ContentValues values = new ContentValues();
             values.put(NAME, this.name);
             values.put(X, this.x);
             values.put(Y, this.y);
             values.put(RADIUS, this.radius);
             values.put(MODE, this.mode);
             values.put(RINGTONE, this.ringtone);
             values.put(CONNECTION, this.connection);
             
             final SQLiteDatabase db = dbHelper.getReadableDatabase();
             this.id = db.insert(POINTS_TABLE_NAME, null, values);
             db.close();
     }
     
     public static Point[] getAll(final DatabaseHelper dbHelper) {
              final List<Point> points = new ArrayList<Point>();
              final SQLiteDatabase db = dbHelper.getWritableDatabase();
              final Cursor c = db.query(POINTS_TABLE_NAME,
                              new String[] { ID, NAME, X, Y, RADIUS, MODE, RINGTONE, CONNECTION}, null, null, null, null, null);
              // make sure you start from the first item
              c.moveToFirst();
              while (!c.isAfterLast()) {
                      final Point point = cursorToPoint(c);
                      points.add(point);
                  c.moveToNext();
              }
              // Make sure to close the cursor
              c.close();
              return points.toArray(new Point[points.size()]);
     }
     
     public static Point cursorToPoint(Cursor c) {
             final Point point = new Point();
             point.setName(c.getString(c.getColumnIndex(NAME)));
             point.setX(c.getDouble(c.getColumnIndex(X)));
             point.setY(c.getDouble(c.getColumnIndex(Y)));
             point.setRadius(c.getInt(c.getColumnIndex(RADIUS)));
             point.setMode(c.getInt(c.getColumnIndex(MODE)));
             point.setRingtone(c.getString(c.getColumnIndex(RINGTONE)));
             point.setConnection(c.getInt(c.getColumnIndex(CONNECTION)));
             return point;
     }
 
     
     public String getName() {
             return name;
     }
 
     public void setName(String name) {
             this.name = name;
     }
 
     public double getX() {
             return x;
     }
 
     public void setX(double x) {
             this.x = x;
     }
 
     public double getY() {
         return y;
 }
 
     public void setY(double y) {
         this.y = y;
     }
     
     public int getRadius() {
         return radius;
 }
 
     public void setRadius(int radius) {
         this.radius = radius;
     }
     
     public int getMode() {
         return mode;
     }
 
     public void setMode(int mode) {
         this.mode = mode;
     }
     
     public String getRingtone() {
         return ringtone;
     }
 
     public void setRingtone(String ringtone) {
 		this.ringtone = ringtone;
 	}
     
     public int getConnection() {
         return connection;
     }
 
     public void setConnection(int connection) {
         this.connection = connection;
     }
 
    public static final String POINTS_TABLE_NAME = "points";
     // column names
     static final String ID = "id"; // 
     static final String NAME = "name";
     static final String X = "x";
     static final String Y = "y";
     static final String RADIUS = "radius";
     static final String MODE = "mode";
     static final String RINGTONE = "ringtone";
     static final String CONNECTION = "connection";
     // SQL statement to create our table
     public static final String POINTS_CREATE_TABLE = "CREATE TABLE " + Point.POINTS_TABLE_NAME + " ("
    		 										+ Point.ID + " INTEGER PRIMARY KEY,"
                                                    + Point.NAME + " TEXT,"
                                                     + Point.X + " DOUBLE,"
                                                     + Point.Y + " DOUBLE,"
                                                    + Point.RADIUS + " TEXT,"
                                                    + Point.MODE + " INTEGER,"
                                                    + Point.RINGTONE + " TEXT,"
                                                     + Point.CONNECTION + " INTEGER"
                                                     + ");";
 
     
 }
