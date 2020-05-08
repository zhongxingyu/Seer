 /**
 	This file is part of Personal Trainer.
 
     Personal Trainer is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     any later version.
 
     Personal Trainer is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Personal Trainer.  If not, see <http://www.gnu.org/licenses/>.
  */
 package se.team05.data;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 
 /**
  * This class is the route table adapter class used to communicate with the
  * "routes" table in the SQLite database. It contains information about the
  * columns, basic Create, Read, Update, Delete operations and also the initial
  * "create table" statement.
  * 
  * @author Daniel Kvist, Gustaf Werlinder, Henrik Hugo, Markus Schutzer, Patrik
  *         Thitusson
  * 
  */
 public class DBRouteAdapter extends DBAdapter
 {
 	public static final String TABLE_ROUTES = "routes";
	public static final String COLUMN_ID = "id";
 	public static final String COLUMN_NAME = "name";
 	public static final String COLUMN_DESCRIPTION = "description";
 	public static final String COLUMN_TYPE = "type";
 	public static final String COLUMN_TIMECOACH = "timecoach";
 	public static final String COLUMN_LENGTHCOACH = "lengthcoach";
 
 	public static final String DATABASE_CREATE_ROUTE_TABLE = "create table " + TABLE_ROUTES + "(" + COLUMN_ID
 			+ " integer primary key autoincrement, " + COLUMN_NAME + " text not null, " + COLUMN_DESCRIPTION
 			+ " text not null," + COLUMN_TYPE + " integer not null," + COLUMN_TIMECOACH + " integer not null,"
 			+ COLUMN_LENGTHCOACH + " integer not null);";
 
 	/**
 	 * The constructor of the class which creates a new instance of the database
 	 * helper and stores it as an instance of the class
 	 * 
 	 * @param context
 	 *            the context which to operate in
 	 */
 	public DBRouteAdapter(Context context)
 	{
 		super(context);
 	}
 
 	/**
 	 * Creates a route.
 	 * 
 	 * @param name
 	 * @param description
 	 * @param type
 	 * @param timeCoach
 	 * @param lengthCoach
 	 */
 	public void createRoute(String name, String description, int type, int timeCoach, int lengthCoach)
 	{
 		ContentValues values = new ContentValues();
 		values.put(COLUMN_NAME, "myRoute");
 		values.put(COLUMN_DESCRIPTION, "myDescription");
 		values.put(COLUMN_TYPE, 0);
 		values.put(COLUMN_TIMECOACH, 0);
 		values.put(COLUMN_LENGTHCOACH, 0);
 		db.insert(TABLE_ROUTES, null, values);
 	}
 	
 	/**
 	 * Execute query on the database, asking for all routes.
 	 * @return a cursor contraining the results
 	 */
 	public Cursor getAllRoutes()
 	{
 		return db.query(TABLE_ROUTES, new String[] {COLUMN_ID, COLUMN_NAME, COLUMN_DESCRIPTION, COLUMN_TYPE,
 				COLUMN_TIMECOACH, COLUMN_LENGTHCOACH}, null, null, null, null, null);
 	}
 }
