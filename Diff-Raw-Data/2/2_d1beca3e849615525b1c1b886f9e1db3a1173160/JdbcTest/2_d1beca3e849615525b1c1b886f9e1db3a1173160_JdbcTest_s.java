 package de.tiq.hive.jdbc;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.DatabaseMetaData;
 import java.sql.Driver;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Vector;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameter;
 import org.junit.runners.Parameterized.Parameters;
 
 @RunWith(Parameterized.class)
 public class JdbcTest {
 
 	private java.sql.Connection conn;
 	private static Properties properties = new Properties();
 	@Parameter(0) public static String prefix;
 	private static List<String[]> prefixes = new ArrayList<String[]>(); 
 
 	@Parameters(name="Configuration: {0}")
 	public static Iterable<String[]> prefixes() throws ClassNotFoundException, IOException {
 		loadProperties();
 		
 		normalizeProperties();
 		
 		for (String key : properties.stringPropertyNames()) {
 			if (key.endsWith(".driver.class")) {
 				String prefix = key.substring(0, key.lastIndexOf(".driver.class"));
 				
 				Assert.assertTrue("'" + prefix + ".driver.class' must be set.", properties.containsKey(prefix + ".driver.class"));
 				Assert.assertTrue("'" + prefix + ".jdbc.uri' must be set.", properties.containsKey(prefix + ".jdbc.uri"));
 		
 				Class.forName(properties.getProperty(prefix + ".driver.class"));
 				
 				prefixes.add(new String[]{prefix});
 			}
 		}
 		
 		return prefixes;
 	}
 
 	private static void normalizeProperties() {
 		if (properties.containsKey("driver.class")) {
 			properties.setProperty("default.driver.class", properties.getProperty("driver.class"));
 			properties.setProperty("default.jdbc.uri", properties.getProperty("jdbc.uri"));
 		}
 	}
 
 	private static void loadProperties() throws IOException {
 		InputStream is = JdbcTest.class.getResourceAsStream("/test.properties");
 		try {
 			Assert.assertNotNull(
 					"'test.properties' not found. Please create this file.\n" +
 					"For further information take a look at 'test.properties.sample'.", is);
 			
 			properties.load(is);
 		} finally {
 			if (is != null) is.close();
 		}
 	}
 	
 	@Before
 	public void connect() throws SQLException {
 		Driver driver = DriverManager.getDriver(properties.getProperty(prefix + ".jdbc.uri"));
 		conn = driver.connect(properties.getProperty(prefix + ".jdbc.uri"), new Properties());
 		Assert.assertNotNull(conn);
 	}
 	
 	@Test
 	public void executeQuery_with_limit() throws Exception {
 		Statement statement = conn.createStatement();
 		
 		Assert.assertNotNull(statement);
 		try {
 			ResultSet resultSet = statement.executeQuery("select * from jdbctest limit 10");
 			
 			Assert.assertNotNull(resultSet);
 	
 			try {
 				int cnt = 0;
 				while (resultSet.next()) {
 					cnt++;
 				}
 				Assert.assertEquals(10, cnt);
 			} finally {
 				resultSet.close();
 			}
 		} finally {
 			statement.close();
 		}
 	}
 	
 	@Test
 	public void executeQuery_with_where() throws SQLException {
 		Statement statement = conn.createStatement();
 		
 		Assert.assertNotNull(statement);
 		try {
 			ResultSet resultSet = statement.executeQuery("select * from jdbctest where id = 1234");
 			
 			Assert.assertNotNull(resultSet);
 			try {
 				Assert.assertTrue(resultSet.next());
 				Assert.assertEquals("1234", resultSet.getString(1));
 				Assert.assertEquals("667", resultSet.getString(2));
 				Assert.assertEquals("6119367", resultSet.getString(3));
 				Assert.assertEquals("SNZ^AO", resultSet.getString(4));
 				Assert.assertEquals("HDD[`A", resultSet.getString(5));
 				Assert.assertEquals("NpqSbQ", resultSet.getString(6));
 				Assert.assertEquals("IOejDf", resultSet.getString(7));
 				Assert.assertEquals("DI@^Hf", resultSet.getString(8));
 				Assert.assertEquals("F[CaGD", resultSet.getString(9));
 			} finally {
 				resultSet.close();
 			}
 		} finally {
 			statement.close();
 		}
 	}
 	
 	@Test
 	public void executeQuery_check_ResultSetMetaData() throws SQLException {
 		Statement statement = conn.createStatement();
 		
 		Assert.assertNotNull(statement);
 		
 		try {
 			ResultSet resultSet = statement.executeQuery("select * from jdbctest limit 1");
 			
 			Assert.assertNotNull(resultSet);
 			
 			try {
 				ResultSetMetaData metaData = resultSet.getMetaData();
 				Assert.assertNotNull(metaData);
 				Assert.assertEquals(9, metaData.getColumnCount());
 				Assert.assertEquals("id", metaData.getColumnName(1));
 				Assert.assertEquals(Types.INTEGER, metaData.getColumnType(1));
 			} finally {
 				resultSet.close();
 			}
 		} finally {
 			statement.close();
 		}
 	}
 
 	@Test
 	public void execute_with_limit() throws SQLException {
 		Statement statement = conn.createStatement();
 		
 		Assert.assertNotNull(statement);
 		try {
 			Assert.assertTrue(statement.execute("select * from jdbctest limit 20"));
 			Assert.assertNotNull(statement.getResultSet());
 			statement.getResultSet().close();
 		} finally {
 			statement.close();
 		}
 	}
 	
 	@Test
 	public void database_metadata_catalogs() throws SQLException {
 		DatabaseMetaData metaData = conn.getMetaData();
 		Assert.assertNotNull(metaData);
 
 		HashSet<String> catalogSet = new HashSet<String>();
 		ResultSet catalogs = metaData.getCatalogs();
 		try {
 			while (catalogs.next()) {
 				catalogSet.add(catalogs.getString(1));
 			}
 			Assert.assertTrue(catalogSet.contains("default"));
 		} finally {
 			catalogs.close();
 		}
 	}
 	
 	@Test
 	public void database_metadata_tables() throws SQLException {
 		DatabaseMetaData metaData = conn.getMetaData();
 		Assert.assertNotNull(metaData);
 		
 		HashSet<String> tableSet = new HashSet<String>();
 		ResultSet tables = metaData.getTables("default", null, null, null);
 		try {
 			while (tables.next()) {
 				tableSet.add(tables.getString(3));
 			}
 			Assert.assertTrue(tableSet.contains("jdbctest"));
 		} finally {
 			tables.close();
 		}
 	}
 	
 	@Test
 	public void database_metadata_tabletypes() throws SQLException {
 		DatabaseMetaData metaData = conn.getMetaData();
 		Assert.assertNotNull(metaData);
 		
 		HashSet<String> tableTypeSet = new HashSet<String>();
 		ResultSet tableTypes = metaData.getTableTypes();
 		try {
 			while (tableTypes.next()) {
 				tableTypeSet.add(tableTypes.getString(1));
 			}
 			Assert.assertTrue(tableTypeSet.contains("INDEX_TABLE"));
 		} finally {
 			tableTypes.close();
 		}
 	}
 	
 	@Test
 	public void database_metadata_columns() throws SQLException {
 		DatabaseMetaData metaData = conn.getMetaData();
 		Assert.assertNotNull(metaData);
 		
 		Vector<String> columnList = new Vector<String>();
 		ResultSet columns = metaData.getColumns("default", null, "jdbctest", null);
 		try {
 			while (columns.next()) {
 				columnList.add(columns.getString(4));
 			}
 			Assert.assertArrayEquals(
 					Arrays.asList("id","random","sum_randoms","var_1","var_2","var_3","var_4","var_5","var_6").toArray(),
 					columnList.toArray());
 		} finally {
 			columns.close();
 		}
 	}
 	
 	@After
 	public void close() throws SQLException {
 		conn.close();
 	}
 }
