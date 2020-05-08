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
 	
	private DatabaseConnector dbc;
 	
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception{
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
 
 }
