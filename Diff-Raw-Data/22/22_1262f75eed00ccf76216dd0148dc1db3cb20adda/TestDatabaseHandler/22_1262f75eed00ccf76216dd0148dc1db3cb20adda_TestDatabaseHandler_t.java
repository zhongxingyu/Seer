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
 
     (C) Copyright 2012: Daniel Kvist, Henrik Hugo, Gustaf Werlinder, Patrik Thitusson, Markus Schutzer
 */
 package se.team05.test.data;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import junit.framework.Assert;
 import se.team05.content.Route;
import se.team05.data.DBRouteAdapter;
import se.team05.data.Database;
import se.team05.data.DatabaseHandler;
import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.test.InstrumentationTestCase;
 
 /**
  * JUnit test case class for testing se.team05.data.DatabaseHandler .
  * This class inherits from android.test.InstrumentationTestCase beacuse
  * it makes a database connection and therefor need the application
  * context to pass into androids SQLite handler.
  * @author Henrik Hugo
  *
  */
 
 public class TestDatabaseHandler extends InstrumentationTestCase {
 	
 	Route r1, r2, r3;
 	DatabaseHandler db;
 	
 	public TestDatabaseHandler()
 	{
 		
 	}
 	
 	/**
 	 * Clearing database, setting up dummy routes and setting up database connection.
 	 */
 	@Override
 	protected void setUp() throws Exception
 	{
 		super.setUp();
 		
 		SQLiteDatabase db0 =  new Database(this.getInstrumentation().getTargetContext()).getWritableDatabase();
 		db0.delete(DBRouteAdapter.TABLE_ROUTES, null, null);
 		db0.close();

		Context context = this.getInstrumentation().getTargetContext();
		this.r1 = new Route("Hello route", "Hello description", -1, -1, -1, context);
		this.r2 = new Route("Hello route2", "Hello description2", -1, -1, -1, context);
		this.r3 = new Route("Hello route3", "Hello description3", -1, -1, -1, context);
 		
 		this.db = new DatabaseHandler(this.getInstrumentation().getTargetContext());
 	}
 	
 	/**
 	 * Testing by saving and getting a route.
 	 * Also tries to delete the route and confirms that it's gone.
 	 */
 	public void testSaveDeleteRoute()
 	{
 		long rid = db.saveRoute(r1);
 		r1.setId(rid); // Set id retreived from db
 		
 		Route resultRoute = db.getRoute(r1.getId());
 		
 		this.compareRoute(r1, resultRoute);
 		
 		db.deleteRoute(r1);
 		
 		// Checking that it's really gone
 		resultRoute = db.getRoute(r1.getId());
 		Assert.assertNull(resultRoute);
 	}
 	
 	/**
 	 * Saving several entries and trying to retreive them as a list.
 	 */
 	public void testGetAllRoutes()
 	{
 		long rid1 = db.saveRoute(r1);
 		long rid2 = db.saveRoute(r2);
 		long rid3 = db.saveRoute(r3);
 		
 		// Setting id:s retreived from database
 		r1.setId(rid1);
 		r2.setId(rid2);
 		r3.setId(rid3);
 		
 		// Preparing hashmap of example routes for easy comparison
 		HashMap<Long, Route> map = new HashMap<Long, Route>();
 		map.put(r1.getId(), r1);
 		map.put(r2.getId(), r2);
 		map.put(r3.getId(), r3);
 		
 		ArrayList<Route> results = db.getAllRoutes();
 		
 		Assert.assertEquals(results.size(), 3); // Asserting size of results
 		
 		// Looping results and asserting with help-method
 		for(int i = 0; i < results.size(); i++)
 		{
 			Route tmp = results.get(i);
 			this.compareRoute( map.get(tmp.getId()), tmp );
 		}
 	}
 	
 	/**
 	 * Help method for asserting Route equality.
 	 * @param expected What's expected
 	 * @param actual The actual value
 	 */
 	private void compareRoute(Route expected, Route actual)
 	{	
 		Assert.assertEquals(expected.getId(), actual.getId());
 		Assert.assertEquals(expected.getName(), actual.getName());
 		Assert.assertEquals(expected.getDescription(), actual.getDescription());
 		Assert.assertEquals(expected.getType(), actual.getType());
 	}
 }
