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
import android.util.Log;

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
 		try
 		{
			db.beginTransaction();
			db.delete(res.getString(R.string.routes_table_name), "name = ?", convert("NewRoute"));
 			returnedValue = db.insertOrThrow(res.getString(R.string.routes_table_name), null, values);
 		}
 		catch(SQLException ex)
 		{
			e = ex;
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
 		if( name==null || name.isEmpty() ){
 			this.insertPointStmt.bindNull(1);
 		}
 		else
 		{
 			this.insertPointStmt.bindString(1, name);
 		}
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
 
	public int updateRoute(Integer routeId, String name, Integer end, Integer size, Integer length, Integer userId)
 	{
 		ContentValues values = new ContentValues();
 		if(name!=null && !name.isEmpty())
 		{
 			values.put("name", name);
 		}
 		if(end!=null)
 		{
 			values.put("finish", end.toString());
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
		return db.update(res.getString(R.string.routes_table_name), values, "_id = ?", convert(routeId));
 	}
 
	public int appendLast(Integer routeId, int nextPoint) throws DatabaseException
 	{
 		int returnValue;
 		int lastPoint;
 		Cursor cursor = db.query(context.getString(R.string.routes_table_name),
 				new String[] { "_id","name","start","finish","size","length","userId" },
 				"_id = ?",
 				convert(routeId),
 				null, null, null);
 		if (cursor.moveToFirst())
 		{
 			lastPoint = Integer.parseInt(cursor.getString(3));
 			returnValue = Integer.parseInt(cursor.getString(4));
 		}
 		else
 		{
 			close(cursor);
 			throw new DatabaseException("No such route in database");
 		}
 		close(cursor);
 		ContentValues values = new ContentValues();
 		values.put("from", lastPoint);
 		values.put("to", nextPoint);
 		db.update(res.getString(R.string.routes_table_name), values, "_id  ?", convert("routeId") );
 		return returnValue;
 	}
 
 	public int appendLast(int routeId, int lastPoint, int nextPoint, int number)
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
 		Cursor cursor = db.query(
 				context.getString(R.string.points_table_name),
 				new String[] { "_id","name","latitude","longitude","accuracy","priority","userId" },
 				null, null, null, null,
 				"priority ASC, name ASC"); 
 		if (cursor.moveToFirst())
 		{
 			do {
 				list.add(createPoint(cursor));
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
 		Cursor cursor = db.query(
 				context.getString(R.string.points_table_name),
 				new String[] { "_id","name","latitude","longitude","accuracy","priority","userId" },
 				null, null, null, null, null); 
 		if (cursor.moveToFirst())
 		{
 			do {
 				Point p = createPoint(cursor);
 				if( ((int)p.distanceTo(point)) < pointsToShow)
 				{
 					list.add(p);
 				}
 			} while (cursor.moveToNext());
 		}
 		close(cursor);
 		return list;
 	}
 
 	public List<Route> selectAllRoutes() throws DatabaseException
 	{
 		List<Route> list = new ArrayList<Route>();
 		Cursor cursor = db.query(
 				context.getString(R.string.routes_table_name),
 				new String[] { "_id","name","start","finish","size","length","userId" },
 				null, null, null, null, null); 
 		if (cursor.moveToFirst())
 		{
 			do {
 				try
 				{
 					list.add(createRoute(cursor));
 				}
 				catch (DatabaseException e)
 				{
 					close(cursor);
 					throw e;
 				}
 			} while (cursor.moveToNext());
 		}
 		close(cursor);
 		return list;
 	}
 
 	public Route selectRoute(String routeName) throws DatabaseException
 	{
 		Route returnValue = null;
 		Cursor cursor = db.query(
 				context.getString(R.string.routes_table_name),
 				new String[] { "_id","name","start","finish","size","length","userId" },
 				"name = ?",
 				convert(routeName),
 				null, null, null); 
 		if (cursor.moveToFirst())
 		{
 			try
 			{
 				returnValue = createRoute(cursor);
 				Point[] array = new Point[returnValue.size];
				//getPointsForRoute();
 			}
 			catch (DatabaseException e)
 			{
 				close(cursor);
 				throw e;
 			}
 		}
 		close(cursor);
 		return returnValue;
 	}
 
 	public String selectUtil(String name)
 	{
 		String returnValue = null;
 		Cursor cursor = db.query(
 				context.getString(R.string.utils_table_name),
 				new String[] { "_id","name","value" },
 				"name=?",
 				convert(name),
 				null, null, null); 
 		if (cursor.moveToFirst())
 		{
 			returnValue = cursor.getString(2);
 		}
 		if (cursor != null && !cursor.isClosed())
 		{
 			cursor.close();
 		}
 		return returnValue;
 	}
 
 	public int selectMyId() throws DatabaseException
 	{
 		int myId = 0;
 		Cursor cursor = db.query(
 				context.getString(R.string.users_table_name),
 				new String[] { "_id","name","userId" },
 				"_id=?",
 				convert(1),
 				null, null, null); 
 		if (cursor.moveToFirst())
 		{
 			myId = Integer.parseInt(cursor.getString(2));
 		}
 		if (cursor != null && !cursor.isClosed())
 		{
 			cursor.close();
 		}
 		if (myId == 0)
 		{
 			throw new DatabaseException(new Exception("No user id in Database"));
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
 
 
 	private void close(Cursor cursor)
 	{
 		if (cursor != null && !cursor.isClosed())
 		{
 			cursor.close();
 		}
 	}
 
 	private String[] convert(Object itemToConvert)
 	{
 		return new String[] { itemToConvert.toString() };
 	}
 	
 	private Point findPoint(Integer pointId) throws DatabaseException
 	{
 		Cursor cursor = db.query(
 				context.getString(R.string.points_table_name),
 				new String[] { "_id","name","latitude","longitude","accuracy","priority","userId" },
 				"_id = ?",
 				new String[] { pointId.toString() },
 				null, null, null
 		);
 		if(cursor.moveToFirst())
 		{
 			return createPoint(cursor);
 		}
 		else
 		{
 			throw new DatabaseException("Inconsistent database - pionts not found.");
 		}
 	}
 
 	private Point createPoint(Cursor pointPointer)
 	{
 		return new Point(
 				Integer.parseInt(pointPointer.getString(0)),
 				pointPointer.getString(1),
 				Integer.parseInt(pointPointer.getString(2)),
 				Integer.parseInt(pointPointer.getString(3)),
 				Integer.parseInt(pointPointer.getString(4)),
 				Integer.parseInt(pointPointer.getString(5)),
 				Integer.parseInt(pointPointer.getString(6))
 		);
 	}
 
 	private Route createRoute(Cursor routePointer) throws DatabaseException
 	{
 		return new Route(
 				Integer.parseInt(routePointer.getString(0)),
 				routePointer.getString(1),
 				findPoint(Integer.parseInt(routePointer.getString(2))),
 				findPoint(Integer.parseInt(routePointer.getString(3))),
 				Integer.parseInt(routePointer.getString(4)),
 				Double.parseDouble(routePointer.getString(5)),
 				Integer.parseInt(routePointer.getString(6))
 		);
 	}
	
	private Point[] getPointsForRoute(Route route)
	{
		Point[] array = new Point[route.size];
		return array;
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
 			db.execSQL("CREATE TABLE " + res.getString(R.string.routes_table_name)           + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE, start INTEGER, finish INTEGER, size INTEGER, length REAL, userId INTEGER)");
 			db.execSQL("CREATE TABLE " + res.getString(R.string.routes_to_points_table_name) + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, point INTEGER, next INTEGER, routeId INTEGER, number INTEGER)");
 			db.execSQL("CREATE TABLE " + res.getString(R.string.users_table_name)            + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, userId INTEGER)");
 			db.execSQL("CREATE TABLE " + res.getString(R.string.utils_table_name)            + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, value TEXT)");
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
 		{
 		}
 
 	}
 
 }
