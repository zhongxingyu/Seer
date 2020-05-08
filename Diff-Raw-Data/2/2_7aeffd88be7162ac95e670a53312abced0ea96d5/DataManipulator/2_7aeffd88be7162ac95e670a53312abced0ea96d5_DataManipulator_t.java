 package com.km2team.syriush.database;
 
 import com.km2team.syriush.*;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class DataManipulator
 {
 
 	private Context          context;
 	private static Resources res;
 	static  SQLiteDatabase   db;
 	private SQLiteStatement  insertPointStmt;
 	private SQLiteStatement  insertRouteToPointsStmt;
 	private SQLiteStatement  insertUtilStmt;
 
 	public DataManipulator(Context context)
 	{
 		this.context = context;
 		res = context.getResources();
 		OpenHelper openHelper = new OpenHelper(this.context);
 		DataManipulator.db = openHelper.getWritableDatabase();
 		this.insertPointStmt = DataManipulator.db.compileStatement(String.format(res.getString(R.string.insert_point), res.getString(R.string.points_table_name)));
 		this.insertRouteToPointsStmt = DataManipulator.db.compileStatement(String.format(res.getString(R.string.insert_route_to_points), res.getString(R.string.routes_to_points_table_name)));
 		this.insertUtilStmt = DataManipulator.db.compileStatement(String.format(res.getString(R.string.insert_util), res.getString(R.string.utils_table_name)));
 	}
 
 	@SuppressWarnings("finally")
 	public int getNewRouteId(Point routeStart) throws DatabaseException
 	{
 		long returnedValue=-1;
 		Exception e = new Exception("Błąd tworzenia nowej trasy w bazie danych");
 		ContentValues values = new ContentValues();
 		values.put("name", "NewRoute");
 		db.beginTransaction();
 		try
 		{
 			returnedValue = db.insertOrThrow(res.getString(R.string.routes_table_name), null, values);
 			db.setTransactionSuccessful();
 		}
 		catch(SQLException ex)
 		{
 			e=ex;
 		}
 		finally
 		{
 			db.endTransaction();
 			if(returnedValue != -1)
 			{
 				return (int)returnedValue;
 			}
 			else
 			{
 				throw new DatabaseException(e);
 			}
 		}
 	}
 
 	public int insertPoint(String name, double latitude, double longitude, double accuracy, int priority, int userId)
 	{
 		this.insertPointStmt.bindString(1, name);
 		this.insertPointStmt.bindDouble(2, latitude);
 		this.insertPointStmt.bindDouble(3, longitude);
 		this.insertPointStmt.bindDouble(4, accuracy);
 		this.insertPointStmt.bindLong(5, priority);
 		this.insertPointStmt.bindLong(6, userId);
 		return (int)this.insertPointStmt.executeInsert();
 	}
 
 	public int insertPoint(String name, Point point)
 	{
 		return insertPoint(name, point.getLatitude(), point.getLongitude(), point.getAltitude(), point.priority.toInt(), point.userId);
 	}
 
 	public int insertUtil(String name, String value)
 	{
 		this.insertUtilStmt.bindString(1, name);
 		this.insertUtilStmt.bindString(2, value);
 		return (int)this.insertUtilStmt.executeInsert();
 	}
 
 	public void updateRoute(int routeId, String name, Integer end, Integer size, Integer length, Integer userId)
 	{
 		ContentValues values = new ContentValues();
 		if(name!=null && !name.isEmpty())
 		{
 			values.put("name", name);
 		}
 		if(end!=null)
 		{
 			values.put("end2", end.toString());
 		}
 		if(size!=null)
 		{
 			values.put("size", size.toString());
 		}
 		if(length!=null)
 		{
 			values.put("length", length.toString());
 		}
 		if(userId!=null)
 		{
 			values.put("userId", userId.toString());
 		}
 		db.update(res.getString(R.string.routes_table_name), values, "_id = ?", new String[] { "routeId" });
 	}
 
 	public int appendLast(int routeId, int nextPoint) throws DatabaseException
 	{
 		int returnValue;
 		int lastPoint;
 		Cursor cursor = db.query(context.getString(R.string.routes_table_name),
 				new String[] { "_id","name","end1","end2","size","length","userId" },
 				"_id = ?",
 				new String[] { Integer.valueOf(routeId).toString() },
 				null, null, null);
 		if (cursor.moveToFirst())
 		{
 			lastPoint = Integer.parseInt(cursor.getString(3));
 			returnValue = Integer.parseInt(cursor.getString(4));
 		}
 		else
 		{
 			throw new DatabaseException("No such route in database");
 		}
 		ContentValues values = new ContentValues();
 		values.put("from", lastPoint);
 		values.put("to", nextPoint);
 		db.update(res.getString(R.string.routes_table_name), values, "_id  ?", new String[] { "routeId" });
 		return returnValue;
 	}
 
 	public int appendLast(int routeId, int lastPoint, int nextPoint, int number) throws DatabaseException
 	{
 		this.insertRouteToPointsStmt.bindLong(1, lastPoint);
 		this.insertRouteToPointsStmt.bindLong(2, nextPoint);
 		this.insertRouteToPointsStmt.bindLong(3, routeId);
 		this.insertRouteToPointsStmt.bindLong(4, number);
 		return (int)this.insertRouteToPointsStmt.executeInsert();
 	}
 
 	public void appendToRoute(int routeId, Point[] part) throws DatabaseException
 	{
 		if(part.length<2)
 		{
 			throw new DatabaseException("Nothing to add - table empty");
 		}
 		int x;
 		int y=(int)insertPoint(null, part[1]);
 		int count = appendLast(routeId, y);
 		for(int i=2; i<part.length-1; ++i)
 		{
 			count++;
 			x=y;
 			y=(int)insertPoint(null, part[i]);
 			appendLast(routeId, x, y, count);
 		}
 		count++;
 		x=y;
 		appendLast(routeId, x, 0, count);
 	}
 
 	public List<Point> selectAllPoints()
 	{
 		List<Point> list = new ArrayList<Point>();
 		Cursor cursor = db.query(context.getString(R.string.points_table_name), new String[] { "_id","name","latitude","longitude","accuracy","priority","userId" }, null, null, null, null, "priority ASC, name ASC"); 
 		if (cursor.moveToFirst())
 		{
 			do {
 				Point p=new Point(
 						Integer.parseInt(cursor.getString(0)),
 						cursor.getString(1),
 						Integer.parseInt(cursor.getString(2)),
 						Integer.parseInt(cursor.getString(3)),
 						Integer.parseInt(cursor.getString(4)),
 						Integer.parseInt(cursor.getString(5)),
 						Integer.parseInt(cursor.getString(6))
 				);
 				list.add(p);
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null && !cursor.isClosed())
 		{
 			cursor.close();
 		}
 		return list;
 	}
 
 	public List<Point> selectNearestPoints(Point point, int pointsToShow)
 	{
 		List<Point> list = new ArrayList<Point>();
 		Cursor cursor = db.query(context.getString(R.string.points_table_name), new String[] { "_id","name","latitude","longitude","accuracy","priority","userId" }, null, null, null, null, "priority ASC, name ASC"); 
 		if (cursor.moveToFirst())
 		{
 			do {
 				Point p=new Point(
 						Integer.parseInt(cursor.getString(0)),
 						cursor.getString(1),
 						Integer.parseInt(cursor.getString(2)),
 						Integer.parseInt(cursor.getString(3)),
 						Integer.parseInt(cursor.getString(4)),
 						Integer.parseInt(cursor.getString(5)),
 						Integer.parseInt(cursor.getString(6))
 				);
 				if( ((int)p.distanceTo(point)) < pointsToShow)
 				{
 					list.add(p);
 				}
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null && !cursor.isClosed())
 		{
 			cursor.close();
 		}
 		return list;
 	}
 
 	public String selectUtil(String name)
 	{
 		String value = null;
 		Cursor cursor = db.query(
 				context.getString(R.string.utils_table_name),
 				new String[] { "_id","name","value" },
 				"name=?",
 				new String[] { name },
 				null, null, null); 
 		if (cursor.moveToFirst())
 		{
 			value = cursor.getString(2);
 		}
 		if (cursor != null && !cursor.isClosed())
 		{
 			cursor.close();
 		}
 		return value;
 	}
 
 	public int selectMyId() throws DatabaseException
 	{
 		int myId = 0;
 		Cursor cursor = db.query(
 				context.getString(R.string.users_table_name),
 				new String[] { "_id","name","userId" },
 				"_id=?",
 				new String[] { "1" },
 				null, null, null); 
 		if (cursor.moveToFirst())
 		{
 			myId = Integer.parseInt(cursor.getString(2));
 		}
 		else
 		{
 			throw new DatabaseException(new Exception("No user id in Database"));
 		}
 		if (cursor != null && !cursor.isClosed())
 		{
 			cursor.close();
 		}
 		return myId;
 	}
 
 
 	public void deleteAll()
 	{
 		db.delete(context.getString(R.string.points_table_name),           null, null);
 		db.delete(context.getString(R.string.routes_table_name),           null, null);
 		db.delete(context.getString(R.string.routes_to_points_table_name), null, null);
 		db.delete(context.getString(R.string.users_table_name),            null, null);
 		db.delete(context.getString(R.string.utils_table_name),            null, null);
 	}
 
 	private static class OpenHelper extends SQLiteOpenHelper
 	{
 
 		OpenHelper(Context context)
 		{
 			super(context, context.getString(R.string.database_file_name), null, context.getResources().getInteger(R.integer.database_version));
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db)
 		{
 			db.execSQL("CREATE TABLE " + res.getString(R.string.points_table_name)           + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, latitude DOUBLE, longitude DOUBLE, accuracy DOUBLE, priority INTEGER, userId INTEGER)");
 			db.execSQL("CREATE TABLE " + res.getString(R.string.routes_table_name)           + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE, end1 INTEGER, end2 INTEGER, size INTEGER, length REAL, userId INTEGER)");
			db.execSQL("CREATE TABLE " + res.getString(R.string.routes_to_points_table_name) + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, end1 INTEGER, end2 INTEGER, routeId INTEGER, ordinal INTEGER)");
 			db.execSQL("CREATE TABLE " + res.getString(R.string.users_table_name)            + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, userId INTEGER)");
 			db.execSQL("CREATE TABLE " + res.getString(R.string.utils_table_name)            + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, value TEXT)");
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
 		{
 		}
 
 	}
 
 }
