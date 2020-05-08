 package org.liveSense.api.sql;
 
 import java.io.IOException;
 import java.sql.SQLException;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.hsqldb.Server;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class SQLExecuteHSQLTest
 	extends SQLExecuteBaseTest {
 	
 	
 	//FIELDS
 	private Server hsqlServer;
 	
 	//TEST - init/fina
 	@Before
 	public void before() 
 		throws Exception {
 		
 		connect();
 		init("longvarchar");
 	}
 	
 	@After
 	public void after() 
 		throws SQLException {
 		
 		disconnect();
 	}
 	
 	//METHODS - abstract
 	@Override
 	protected void createTestProcedure()
 		throws Exception {
 		
 		//VERY unique storedproc support		
 	}
 
 	
 	//METHODS - private
 	private Server connect() 
		throws Exception {
 		
 		hsqlServer = new Server();
 		hsqlServer.setDatabaseName(0, "testDb");
 		hsqlServer.setAddress("127.0.0.1");
 		hsqlServer.setDatabasePath(0, "file:./target/test-classes/testDb");
 		
 		hsqlServer.start();
		boolean running = false;
		long serverTimeout = 10000;
		long startTime = System.currentTimeMillis();
		while (!running) {
			if (System.currentTimeMillis() > startTime+serverTimeout)
				throw new Exception("HSQL Server timed out");
			try {
				hsqlServer.checkRunning(true);
				running = true;
			} catch (Exception e) {
			}
		}
 		dataSource = new BasicDataSource();
 		dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
 		dataSource.setUrl("jdbc:hsqldb:hsql://localhost/testDb");
 		dataSource.setUsername("sa");
 		dataSource.setPassword("");
 		dataSource.setDefaultAutoCommit(false);
 		connection = dataSource.getConnection();		
 		connection2 = dataSource.getConnection();
 		
 		connection.setAutoCommit(false);
 		connection2.setAutoCommit(false);
 		
 		executeSql(connection, "SET PROPERTY \"sql.enforce_strict_size\" true");
 		
 		return hsqlServer;
 	}
 	
 	@Override
 	protected void disconnect() throws SQLException {
 		
 		super.disconnect();
 
 		hsqlServer.stop();
 	}
 	
 	
 	//TESTS 
 	//record level lock unsupported by HSQL
 	@Test
 	public void testPrepareLockMulti() 
 		throws Exception {
 	}
 	
 	@Test
 	public void testRunPreparedLockMulti() 
 		throws Exception {
 	}
 	
 	@Test
 	public void testRunLockMulti() 
 		throws Exception {
 	}
 	
 	@Test
 	public void testPrepareLockSingle() 
 		throws Exception {
 	}
 	
 	@Test
 	public void testRunPreparedLockSingle() 
 		throws Exception {
 	}
 	
 	@Test
 	public void testRunLockSingle() 
 		throws Exception {
 	}
 	
 	//VERY unique storedproc support by HSQL
 	@Test
 	public void testPrepareExecuteProcedure() 
 		throws Exception {
 	}
 	
 	@Test
 	public void testRunPreparedExecuteProcedure() 
 		throws Exception {
 	}
 	
 	@Test
 	public void testRunExecuteProcedure() 
 		throws Exception {
 	}
 	
 
 }
