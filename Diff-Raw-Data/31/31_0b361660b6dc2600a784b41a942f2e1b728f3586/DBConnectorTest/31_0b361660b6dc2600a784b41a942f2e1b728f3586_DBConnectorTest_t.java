 package dao;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.sql.Connection;
 
 import org.apache.log4j.PropertyConfigurator;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import dao.DatabaseConnector;
 import dao.DatabaseConnectorImpl;
 
 
 public class DBConnectorTest {
 	
	private static DatabaseConnector dbc;
 	
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception{
		dbc = new DatabaseConnectorImpl();
 		PropertyConfigurator.configure("log4j.properties");
 	}
 
 	@Test
 	public void testGetConnection() {
 		dbc = new DatabaseConnectorImpl();
 		try{
 			Connection c = dbc.getConnection();
 			assertTrue(c != null);
 		}
 		catch(Exception e){
 			fail("Did not get a connection check if DB is running");
 		}
 	}
	
	@Test
	public void testThatOnlyOneConnectionIsReturned() throws Exception{
		Connection c1 = dbc.getConnection();
		Connection c2 = dbc.getConnection();
		
		assertTrue(c1.equals(c2));
	}
 
 }
