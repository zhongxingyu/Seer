 package all.tests;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
import org.hsqldb.Server;
 //import laboratory.tests.TestExperimentalMultiValueClasses;
 //import laboratory.tests.TestExperimentalSingleValueClasses;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
 import patient.tests.TestUpdatePatient;
 
 import database.setup.ResetTestingDatabaseSchema;
 
 @RunWith(Suite.class)
 @SuiteClasses({
 	//TestExperimentalSingleValueClasses.class,
 	//TestExperimentalMultiValueClasses.class,
 	TestUpdatePatient.class
 	
 })
 
  public final class CoreObjectsNeedsDatabaseSuite {
 
 	public static final String PORT = "9001";
 	
 	private CoreObjectsNeedsDatabaseSuite(){};
 	
 	@BeforeClass
 	public static void setUp() throws Exception {
 		startHSQLDB();
 		resetSchema();
 	}
 
 	@AfterClass
 	public static void tearDown() throws Exception {
 		resetSchema();
 		stopHSQLDBServer();
 	}
 	
 	private static void resetSchema() {
 		ResetTestingDatabaseSchema reset = new ResetTestingDatabaseSchema();
 		reset.resetHSQLDBDatabase();
 	}
 	
 	private static void startHSQLDB() {
 		String[] args1 = { "-database", "testData", "-port",
 				PORT, "-no_system_exit", "true" };
 		Server.main(args1);
 	}
 
 	private static void stopHSQLDBServer() throws ClassNotFoundException,
 			SQLException {
 		Class.forName("org.hsqldb.jdbcDriver");
 		String url = "jdbc:hsqldb:hsql://localhost:9001";
 		String sql = "SHUTDOWN";
 		Connection con = DriverManager.getConnection(url, "sa", "");
 		Statement stmt = con.createStatement();
 		stmt.executeUpdate(sql);		
 		stmt.close();
 		con.close();
 	}
 }
