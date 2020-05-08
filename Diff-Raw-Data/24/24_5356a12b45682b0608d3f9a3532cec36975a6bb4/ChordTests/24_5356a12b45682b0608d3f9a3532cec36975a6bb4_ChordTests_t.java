 package org.h2.test.h2o;
 
 import static org.junit.Assert.*;
 import static org.junit.Assert.fail;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.h2.engine.Constants;
 import org.h2.h2o.manager.PersistentSchemaManager;
 import org.h2.h2o.remote.ChordDatabaseRemote;
 import org.h2.h2o.util.DatabaseURL;
 import org.h2.h2o.util.H2oProperties;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 
 
 /**
  * Class which conducts tests on 10 in-memory databases running at the same time.
  * 
  * @author Angus Macdonald (angus@cs.st-andrews.ac.uk)
  */
 public class ChordTests extends TestBase {
 
 	private Connection[] cas;
 	private Statement[] sas;
 
 	private static String[] dbs =  {"two", "three"}; //, "four", "five", "six", "seven", "eight", "nine"
 
 	/**
 	 * Whether the schema manager state has been replicated yet.
 	 */
 	public static boolean isReplicated = false;
 
 	@BeforeClass
 	public static void initialSetUp(){
 		Diagnostic.setLevel(DiagnosticLevel.FULL);
 		Constants.IS_TEST = true;
 		Constants.IS_NON_SM_TEST = false;
 		createMultiplePropertiesFiles(dbs);
 
 		setReplicated(false);
 	}
 
 	public static synchronized void setReplicated(boolean b){
 		isReplicated = b;
 	}
 
 	public static synchronized boolean isReplicated(){
 		return isReplicated;
 	}
 
 
 	private static void createMultiplePropertiesFiles(String[] dbNames){
 
 		for (String db: dbNames){
 
 			String fullDBName = "jdbc:h2:mem:" + db;
 
 			H2oProperties properties = new H2oProperties(DatabaseURL.parseURL(fullDBName));
 
 			properties.createNewFile();
 
 			properties.setProperty("schemaManagerLocation", "jdbc:h2:sm:mem:one");
 
 			properties.saveAndClose();
 
 		}
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		//Constants.DEFAULT_SCHEMA_MANAGER_LOCATION = "jdbc:h2:sm:mem:one";
 		//PersistentSchemaManager.USERNAME = "angus";
 		//PersistentSchemaManager.PASSWORD = "";
 
 		org.h2.Driver.load();
 
 		for (String db: dbs){
 
 			H2oProperties knownHosts = new H2oProperties(DatabaseURL.parseURL("jdbc:h2:mem:" + db), "instances");
 			knownHosts.createNewFile();
 			knownHosts.setProperty("jdbc:h2:sm:mem:one", ChordDatabaseRemote.currentPort + "");
 			knownHosts.saveAndClose();
 
 		}
 
 
 		cas = new Connection[dbs.length + 1];
 		cas[0] = DriverManager.getConnection("jdbc:h2:sm:mem:one", PersistentSchemaManager.USERNAME, PersistentSchemaManager.PASSWORD);
 		for (int i = 1; i < cas.length; i ++){
 			cas[i] = DriverManager.getConnection("jdbc:h2:mem:" + dbs[i-1], PersistentSchemaManager.USERNAME, PersistentSchemaManager.PASSWORD);
 		}
 
 		sas = new Statement[dbs.length + 1];
 
 		for (int i = 0; i < cas.length; i ++){
 			sas[i] = cas[i].createStatement();
 		}
 
		String sql = "CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255));";
		sql += "INSERT INTO TEST VALUES(1, 'Hello');";
		sql += "INSERT INTO TEST VALUES(2, 'World');";

		sas[0].execute(sql);
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() {
 
 		for (int i = 0; i < sas.length; i ++){
 			try{ 
 				if (!sas[i].isClosed())sas[i].close();	
 				sas[i] = null;
 			} catch (Exception e){
 				e.printStackTrace();
 				fail("Statements aren't being closed correctly.");
 			}
 		}
 
 		for (int i = 0; i < cas.length; i ++){
 			try{ 
 				if (!cas[i].isClosed())cas[i].close();	
 				cas[i] = null;
 			} catch (Exception e){
 				e.printStackTrace();
 				fail("Connections aren't being closed correctly.");
 			}
 		}
 
 
 
 		closeDatabaseCompletely();
 
 		cas = null;
 		sas = null;
 	}
 
 
 	//	/**
 	//	 * Get the RMI port of the local chord node.
 	//	 */
 	//	@Test
 	//	public void GetRmiPort(){
 	//		Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "STARTING TEST");
 	//		
 	//		
 	//		try{
 	//			int result = sas[0].executeUpdate("GET RMI PORT");
 	//
 	//			assertEquals(result, 30000);
 	//		} catch (SQLException e){
 	//			e.printStackTrace();
 	//			fail("An Unexpected SQLException was thrown.");
 	//		}
 	//	}
 
 	//	/**
 	//	 * Get the RMI port of a remote chord node.
 	//	 */
 	//	@Test
 	//	public void GetRmiPortRemote(){
 	//		Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "STARTING TEST");
 	//		
 	//		
 	//		try{
 	//			int result = sas[1].executeUpdate("GET RMI PORT AT 'jdbc:h2:mem:one'");
 	//
 	//			assertEquals(result, 30000);
 	//		} catch (SQLException e){
 	//			e.printStackTrace();
 	//			fail("An Unexpected SQLException was thrown.");
 	//		}
 	//	}
 
 
 	/**
 	 * Tests that the state of the schema manager is replicated - inserts data before the schema manager
 	 * state is replicated (probably - not deterministic).
 	 */
 	@Test
 	public void ReplicateSchemaManagerInsertFirst() throws InterruptedException{
 		Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "STARTING TEST");
 
 
 		try{
 			sas[0].executeUpdate("CREATE TABLE TEST2(ID INT PRIMARY KEY, NAME VARCHAR(255));");
 		} catch (SQLException e){
 			fail("An Unexpected SQLException was thrown.");
 		}
 
 		while (!isReplicated){
 			Thread.sleep(100);
 		}
 
 		try {
 			ResultSet rs = sas[1].executeQuery("SELECT LOCAL * FROM H2O.H2O_TABLE");
 
 			if (!(rs.next() && rs.next())){
 				fail("Expected a result here.");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	@Test
 	public void SchemaManagerFailure() throws InterruptedException {
 		Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "STARTING TEST");
 		try {
 			sas[0].close();
 			cas[0].close();
 			
 			Thread.sleep(5000);
 			
 			sas[1].executeUpdate("CREATE TABLE TEST2(ID INT PRIMARY KEY, NAME VARCHAR(255));");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void SchemaManagerMigration() throws InterruptedException {
 		Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "STARTING TEST");
 		try {
 			sas[1].executeUpdate("MIGRATE SCHEMA MANAGER");
 
 			sas[2].executeUpdate("CREATE TABLE TEST2(ID INT PRIMARY KEY, NAME VARCHAR(255));");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	//	/**
 	//	 * Tests that the state of the schema manager is replicated - inserts data after the schema manager
 	//	 * state is replicated (probably - not deterministic).
 	//	 */
 	//	@Test
 	//	public void ReplicateSchemaManagerInsertAfter() throws InterruptedException{
 	//		Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "STARTING TEST");
 	//
 	//		while (!isReplicated){
 	//			Thread.sleep(100);
 	//		}
 	//
 	//		try {
 	//			ResultSet rs = sas[1].executeQuery("SELECT LOCAL * FROM H2O.H2O_TABLE");
 	//
 	//			if ((rs.next() && rs.next())){
 	//				fail("Expected nothing here.");
 	//			}
 	//		} catch (SQLException e) {
 	//			e.printStackTrace();
 	//		}
 	//
 	//		try{
 	//			int result = sas[0].executeUpdate("CREATE TABLE TEST2(ID INT PRIMARY KEY, NAME VARCHAR(255));");
 	//		} catch (SQLException e){
 	//			fail("An Unexpected SQLException was thrown.");
 	//		}
 	//
 	//
 	//		try {
 	//			ResultSet rs = sas[1].executeQuery("SELECT LOCAL * FROM H2O.H2O_TABLE");
 	//
 	//			if (!(rs.next() && rs.next())){
 	//				fail("Expected a result here.");
 	//			}
 	//		} catch (SQLException e) {
 	//			e.printStackTrace();
 	//		}
 	//
 	//	}
 }
