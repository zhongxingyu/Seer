 /**
  * This file is part of ASxcel.
  *
  * ASxcel is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * ASxcel is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with ASxcel.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.jandavid.asxcel.tests;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import de.jandavid.asxcel.model.Airport;
 import de.jandavid.asxcel.model.Database;
 import de.jandavid.asxcel.model.DatabaseResult;
 import de.jandavid.asxcel.model.Model;
 
 /**
  * This test validates the correct behavior of the most important methods of
  * the class 'Model'.
  * 
  * @author jdno
  */
 public class ModelTest {
 	
 	/**
 	 * This is used by the tests.
 	 */
 	private Database db;
 	
 	/**
 	 * This is used by the tests.
 	 */
 	private Model model;
 	
 	/**
 	 * This initiates the private attributes before every test. 
 	 */
 	@Before
	public void initializeModel() throws Exception {
 		model = new Model("resources" + File.separator + "testDb.sqlite");
 		db = model.getDatabase();
		model.initializeModel();
		model.loadEnterprise("TestEnterprise");
 	}
 	
 	/**
 	 * This is a test for the initialization itself. 
 	 */
 	@Test
 	public void testInitialization() throws ClassNotFoundException, SQLException {
 		model = new Model("resources" + File.separator + "testDb.sqlite");
 	}
 	
 	/**
 	 * This test creates an airport. 
 	 */
 	@Test
 	public void testCreateAirport() throws SQLException {
 		Airport airport1 = model.createAirport("TestAirportA");
 		Airport airport2 = model.createAirport("TestAirportA");
 		
 		assertEquals(airport1.toString(), airport2.toString());
 		
 		String query = "SELECT `id` FROM `airports` WHERE `name` = 'TestAirportA' LIMIT 1";
 		DatabaseResult dr = db.executeQuery(query);
 		assertTrue(dr.next());
 		
 		query = "DELETE FROM `airports` WHERE `id` = '" + dr.getInt(0) + "'";
 		int rowCount = db.executeUpdate(query, new ArrayList<Object>(0));
 		assertEquals(1, rowCount);
 	}
 	
 	/**
 	 * This tests deletes an airport. 
 	 */
 	@Test
 	public void testDeleteAirport() throws SQLException {
 		model.createAirport("TestAirportA");
 		model.deleteAirport("TestAirportA");
 		
 		String query = "SELECT `id` FROM `airports` WHERE `name` = 'TestAirportA' LIMIT 1";
 		DatabaseResult dr = db.executeQuery(query);
 		
 		assertFalse(dr.next());
 	}
 	
 	/**
 	 * This tests loads all airports from the database. 
 	 */
 	@Test
 	public void testLoadAirports() throws SQLException {
 		model.loadAirports(1);
 		
 		assertEquals(9, model.getAirports().size());
 		
 		String testName = "";
 		for(int i = 1; i <= model.getAirports().size(); i++) {
 			testName = "TestAirport" + String.valueOf(i);
 			assertEquals(testName, model.getAirports().get(i-1).getName());
 		}
 	}
 
 }
