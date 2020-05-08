 package db;
 
 import static org.junit.Assert.*;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.Set;
 
 import org.junit.After;
 import org.junit.Assume;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class WikipediaConnectorTestCase {
 	
 private static String propertyFileName;
 private static Properties prop;
 
 private Connection testConection;
 
    @BeforeClass
    public static void classSetUp() throws IOException{
 	   Assume.assumeTrue(WikipediaConnector.isTestEnvironment());
 	   
 	   propertyFileName = "setup.properties";
 	   prop = new Properties();
 	   prop.load(WikipediaConnectorTestCase.class.getClassLoader().getResourceAsStream(propertyFileName));
    	
    }
    
    @Before
    public void setUp() throws ClassNotFoundException, SQLException, TestDatabaseSameThatWikipediaDatabaseException{
 	   this.testConection=WikipediaConnector.getTestConnection();
    }
    
    @After
    public void tearDown() throws SQLException, FileNotFoundException, ClassNotFoundException, IOException{
 		WikipediaConnector.restoreResultIndex();
 	   this.testConection.close();
    }
    
    @Test
    public void testConnectionClosedMustBeRecreated() throws ClassNotFoundException, SQLException, TestDatabaseSameThatWikipediaDatabaseException{
 	   Connection wpConnection = WikipediaConnector.getConnection();
 	   Connection resConnection = WikipediaConnector.getResultsConnection();
 	   Connection testConnection = WikipediaConnector.getTestConnection();
 	   wpConnection.prepareCall("");
 	   resConnection.prepareCall("");
 	   testConnection.prepareCall("");
 	   wpConnection.close();
 	   resConnection.close();
 	   testConnection.close();
 	   wpConnection = WikipediaConnector.getConnection();
 	   resConnection = WikipediaConnector.getResultsConnection();
 	   testConnection = WikipediaConnector.getTestConnection();
 	   wpConnection.prepareCall("");
 	   resConnection.prepareCall("");
 	   testConnection.prepareCall("");
 	   
    }
    
    
    @Test
    public void testRestoreTestDatabase() throws FileNotFoundException, ClassNotFoundException, SQLException, TestDatabaseSameThatWikipediaDatabaseException, IOException{
 	   WikipediaConnector.restoreTestDatabase();
 	   
 	   ResultSet result = this.testConection.createStatement().executeQuery("select page_title from page where page_id=1");
 	   result.first();
 	   String messi = result.getString("page_title");
 	   
 	   this.testConection.createStatement().executeUpdate("UPDATE page SET page_title='Maradona' WHERE page_id=1");
 	   
 	   ResultSet maradonaRS = this.testConection.createStatement().executeQuery("select page_title from page where page_id=1");
 	   maradonaRS.first();
 	   
 	   assertEquals("Maradona", maradonaRS.getString("page_title"));
 	   
 	   WikipediaConnector.restoreTestDatabase();
 	   
 	   ResultSet messiRS = this.testConection.createStatement().executeQuery("select page_title from page where page_id=1");
 	   messiRS.first();
 	   assertEquals(messi, messiRS.getString("page_title"));
 	   
    }
    
    
    @Test
    public void testRestoreResultIndex() throws FileNotFoundException, ClassNotFoundException, SQLException, TestDatabaseSameThatWikipediaDatabaseException, IOException{
 	   
 	   
 	   Connection resultsConnection = WikipediaConnector.getResultsConnection();
 	   WikipediaConnector.restoreResultIndex();
 	   
 	   ResultSet result = resultsConnection.createStatement().executeQuery("select count(*) as suma from `NFPC`");
 	   result.first();
 	   assertSame(0,result.getInt("suma"));
 	   
 	   result = resultsConnection.createStatement().executeQuery("select count(*) as suma from `U_page`");
 	   result.first();
 	   assertSame(0,result.getInt("suma"));
 	   
 	   result = resultsConnection.createStatement().executeQuery("select count(*) as suma from `UxV`");
 	   result.first();
 	   assertSame(0,result.getInt("suma"));
 	   
 	   result = resultsConnection.createStatement().executeQuery("select count(*) as suma from `V_Normalized`");
 	   result.first();
 	   assertSame(0,result.getInt("suma"));
 	   
 	   
 	  
 	   
 	   WikipediaConnector.getResultsConnection().createStatement().executeUpdate("INSERT INTO `NFPC` (`v_from`,`u_to`) values (\"test\",\"test\")");
 	   WikipediaConnector.getResultsConnection().createStatement().executeUpdate("INSERT INTO `U_page` (`page`) values (\"test\")");
 	   WikipediaConnector.getResultsConnection().createStatement().executeUpdate("INSERT INTO `UxV` (`u_from`,`v_to`) values (1,2)");
 	   WikipediaConnector.getResultsConnection().createStatement().executeUpdate("INSERT INTO `V_Normalized` (`path`) values (\"#from / Cat:#from / People_from_#from / #to\")");
 	   
   WikipediaConnector.restoreResultIndex();
 	   
 	   result = resultsConnection.createStatement().executeQuery("select count(*) as suma from `NFPC`");
 	   result.first();
 	   assertSame(0,result.getInt("suma"));
 	   
 	   result = resultsConnection.createStatement().executeQuery("select count(*) as suma from `U_page`");
 	   result.first();
 	   assertSame(0,result.getInt("suma"));
 	   
 	   result = resultsConnection.createStatement().executeQuery("select count(*) as suma from `UxV`");
 	   result.first();
 	   assertSame(0,result.getInt("suma"));
 	   
 	   result = resultsConnection.createStatement().executeQuery("select count(*) as suma from `V_Normalized`");
 	   result.first();
 	   assertSame(0,result.getInt("suma"));
    }
 
 
 	@Test
 	public void testReadConfigurationPropertiesWikipediaBase() {
 		assertEquals(prop.getProperty("wikipediaDatabase"), WikipediaConnector.getWikipediaBase());
 		assertNotNull(WikipediaConnector.getWikipediaBase());
 		assertEquals(prop.getProperty("wikipediaDatabaseUser"), WikipediaConnector.getWikipediaDatabaseUser());
 		assertNotNull(WikipediaConnector.getWikipediaDatabaseUser());
 		assertEquals(prop.getProperty("wikipediaDatabasePass"), WikipediaConnector.getWikipediaDatabasePass());
 		assertNotNull(WikipediaConnector.getWikipediaDatabasePass());
 	}
 	
 	@Test
 	public void testReadConfigurationPropertiesResultDatabase() {
 		assertEquals(prop.getProperty("resultDatabase"), WikipediaConnector.getResultDatabase());
 		assertNotNull(WikipediaConnector.getResultDatabase());
 		assertEquals(prop.getProperty("resultDatabaseUser"), WikipediaConnector.getResultDatabaseUser());
 		assertNotNull(WikipediaConnector.getResultDatabaseUser());
 		assertEquals(prop.getProperty("resultDatabasePass"), WikipediaConnector.getResultDatabasePass());
 		assertNotNull(WikipediaConnector.getResultDatabasePass());
 	}
 	
 	@Test
 	public void testReadConfigurationPropertiesTestDatabase() {
 		assertEquals(prop.getProperty("testDatabase"), WikipediaConnector.getTestDatabase());
 		assertNotNull(WikipediaConnector.getTestDatabase());
 		assertEquals(prop.getProperty("testDatabaseUser"), WikipediaConnector.getTestDatabaseUser());
 		assertNotNull(WikipediaConnector.getTestDatabaseUser());
 		assertEquals(prop.getProperty("testDatabasePass"), WikipediaConnector.getTestDatabasePass());
 		assertNotNull(WikipediaConnector.getTestDatabasePass());
 	}
 
 	
 	@Test
 	public void testResultsConnection() throws ClassNotFoundException, SQLException{
 		Connection connection = WikipediaConnector.getResultsConnection();
 		if(prop.getProperty("testEnvironment").equals("true")){
 			assertEquals(WikipediaConnector.getTestDatabase(), "localhost/"+connection.getCatalog());
 		}else{
 		assertEquals(WikipediaConnector.getResultDatabase(), "localhost/"+connection.getCatalog());}
 		connection.close();
 	}
 	
 	@Test
 	public void testTestConnection() throws ClassNotFoundException, SQLException, TestDatabaseSameThatWikipediaDatabaseException{
 		Connection connection;
 		connection = WikipediaConnector.getTestConnection();
 		assertEquals(WikipediaConnector.getTestDatabase(), "localhost/"+connection.getCatalog());
 		connection.close();
 		
 	}
 	
 	@Test
 	public void testTestEnvironmentWithConnection() throws ClassNotFoundException, SQLException, TestDatabaseSameThatWikipediaDatabaseException{
 		if(prop.getProperty("testEnvironment").equals("true")){
 			Connection connection = WikipediaConnector.getConnection();
 			assertEquals(WikipediaConnector.getTestDatabase(), "localhost/"+connection.getCatalog());
 		}else{
 			Connection connection = WikipediaConnector.getConnection();
 			assertEquals(WikipediaConnector.getWikipediaBase(), "localhost/"+connection.getCatalog());
 				
 		}
 	}
 	
 	@Test
 	public void testGetTypesFromDB() throws SQLException, ClassNotFoundException, FileNotFoundException, IOException{
 		WikipediaConnector.restoreResultIndex();
 		String[] dt = {"<http://dbpedia.org/class/yago/ArgentinePopSingers>","<http://dbpedia.org/class/yago/PeopleFromBuEnosAires>",
 				"<http://dbpedia.org/class/yago/Actor109765278>", "<http://dbpedia.org/class/yago/LivingPeople>",
 				"<http://dbpedia.org/class/yago/ArgentinePeopleOfItalianDescent>"};
 		Set<String> diegoTypes = new HashSet<String>(Arrays.asList(dt));
 		
 		assertEquals(diegoTypes, new HashSet<String>(WikipediaConnector.getResourceDBTypes("Diego_Torres")));
 		
 	}
 	
 	@Test
 	public void testGetProportionOfConnectedPairs() throws ClassNotFoundException, SQLException, FileNotFoundException, IOException{
 		
 		WikipediaConnector.restoreResultIndex();
 		
 		for (int i = 1; i < 11; i++) {
 			Connection con = WikipediaConnector.getResultsConnection();
 			Statement st = con.createStatement();
			st.executeUpdate("insert into U_pageEnhanced(`page`,`id`,`subjectTypes`,`objectTypes`) values ("+i+","+i+40+",\"sT\",\"oT\")");
 			st.close();
 		}
 		
 		ResultSet rs = WikipediaConnector.getRandomProportionOfConnectedPairs(10);
 		rs.last();
 		assertEquals(1,rs.getRow());
 		
 		rs = WikipediaConnector.getRandomProportionOfConnectedPairs(100);
 		rs.last();
 		
 		rs = WikipediaConnector.getRandomProportionOfConnectedPairs(120);
 		rs.last();
 		assertEquals(10, rs.getRow());
 		
 		
 	}
 	
 	
 	
 
 }
