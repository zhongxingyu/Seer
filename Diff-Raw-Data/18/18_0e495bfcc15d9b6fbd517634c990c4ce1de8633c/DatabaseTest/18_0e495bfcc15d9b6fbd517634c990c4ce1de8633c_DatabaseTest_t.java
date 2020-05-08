 package api;
 
 import static org.junit.Assert.*;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 public class DatabaseTest {
 
 	Database DB = null;
 	// practice expecting exceptions
 	/*@Test (expected=Exception.class)
 	public void getInstancetest() throws Exception {
 		Database DB = Database.getInstance();
 	}*/
 
 	/// test for getInstance function of database
 	@Test
 	public void getInstancetest(){
 		
 		try{
 			DB = Database.getInstance();
 		}
 		catch (Exception e){
 			Assert.fail();
 		}
 		Assert.assertNotNull(DB); 
 	}
 
 	// Query Tests
 	
 	/// Null String
 	@Test (expected=IllegalArgumentException.class)
	public void NullStringQuery() throws SQLException{
 		DB.query(null);
 	}
 	/// Blank String
 	@Test (expected=IllegalArgumentException.class)
	public void EmptyStringQuery() throws SQLException{
 		DB.query("");
 	}
 	/// Random String
 	@Test (expected=IllegalArgumentException.class)
	public void RandomStringQuery() throws SQLException{
 		DB.query("adblksd soreo");
 	}
 	/// Select a table that doesn't exist
 	@Test
 	public void TableDoesntExistQuery(){
		
 		try {
			ResultSet rs = DB.query("SELECT Name FROM Country");
 			if(rs.next())
 				Assert.fail();
 		} catch (SQLException e) {
 			Assert.fail();
 		}
 		Assert.assertTrue(true);
 		
 	}
 	/// Select a table that does exist
 	// This test is not correct. Change query
 	@Test
 	public void TableDoesExistQuery(){
		
 		try {
			ResultSet rs = DB.query("SELECT Name FROM XYZ");
 			if(!rs.next())
 				Assert.fail();
 		} catch (SQLException e) {
 			Assert.fail();
 		}
 		Assert.assertTrue(true);
 		
 	}
 	/// Test for close database
 	@Test
 	public void closetest(){
 		DB.close();
 		Assert.assertNull(DB);
 	}
 	
 	
 }
