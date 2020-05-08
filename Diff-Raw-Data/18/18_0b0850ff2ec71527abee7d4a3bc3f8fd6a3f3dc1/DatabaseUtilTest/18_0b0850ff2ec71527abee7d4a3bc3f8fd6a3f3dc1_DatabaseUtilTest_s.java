 package gov.usgs.cida.ncetl.utils;
 
 import java.io.InputStream;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.List;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.sql.Connection;
 import java.io.File;
 import java.sql.SQLException;
 import javax.naming.NamingException;
 import org.apache.commons.io.FileUtils;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.hamcrest.MatcherAssert.*;
 import static org.hamcrest.Matchers.*;
 
 /**
  *
  * @author Ivan Suftin <isuftin@usgs.gov>
  */
 public class DatabaseUtilTest {
     private String DB_LOCATION = FileHelper.getTempDirectory() +  "test_delete_me" + File.separator;
     private String createDb = null;
     private String destroyDb = null;
     private Connection connection;
     
     public DatabaseUtilTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
     
     @Before
     public void setUp() throws IOException {
         FileUtils.forceMkdir(new File(DB_LOCATION));
     }
     
     @After
     public void tearDown() throws IOException, SQLException, NamingException, ClassNotFoundException {
         if (connection != null && !connection.isClosed()) {
             DatabaseUtil.closeConnection(connection);
         }
         if (destroyDb != null) {
             DatabaseUtil.shutdownDatabase(destroyDb);
         }
         
         FileUtils.forceDelete(new File(DB_LOCATION));
     }
 
     @Test
     public void testSetupDatabaseConnection() throws SQLException, NamingException, ClassNotFoundException {
         String DB_FULL_LOCATION = DB_LOCATION + "test1.db";
         createDb = "jdbc:derby:" + DB_FULL_LOCATION + ";create=true;";
         destroyDb = "jdbc:derby:" + DB_FULL_LOCATION + ";shutdown=true;";
         DatabaseUtil.setupDatabase(createDb, "org.apache.derby.jdbc.EmbeddedDriver");
         connection = DatabaseUtil.getConnection("java:comp/env/jdbc/test1.db");
         assertThat(connection.isClosed(), is(false));
     }
     
     
     
     @Test
     public void testReadDxLUsingFileAsInput() {
         InputStream createTablesInputStream = DatabaseUtil.class.getClassLoader().getResourceAsStream("gov/usgs/cida/ddl/test/create_tables.ddl");
         List<String> result = DatabaseUtil.readDxL(createTablesInputStream);
 
         assertThat(result.size(), is(not(equalTo(0))));
        assertThat(result.size(), is(equalTo(20)));
        assertThat(result.get(0), is(equalTo("CREATE TABLE collection_type      (id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), type varchar(32), inserted boolean, updated boolean, PRIMARY KEY (id))")));
     }
     
     @Test
     public void testReadDxLUsingStringAsInput() {
         ByteArrayInputStream bais = new ByteArrayInputStream("test one;test two;test three;".getBytes());
         List<String> result = DatabaseUtil.readDxL(bais);
         assertThat(result.size(), is(equalTo(3)));
         assertThat(result.get(0), is(equalTo("test one")));
         assertThat(result.get(1), is(equalTo("test two")));
         assertThat(result.get(2), is(equalTo("test three")));
     }
     
     @Test 
     public void testWriteDDL() throws SQLException, NamingException, ClassNotFoundException {
         String DB_FULL_LOCATION = DB_LOCATION + "test1.db";
         createDb = "jdbc:derby:" + DB_FULL_LOCATION + ";create=true;";
         destroyDb = "jdbc:derby:" + DB_FULL_LOCATION + ";shutdown=true;";
         DatabaseUtil.setupDatabase(createDb, "org.apache.derby.jdbc.EmbeddedDriver");
         connection = DatabaseUtil.getConnection("java:comp/env/jdbc/test1.db");
         
         List<String> ddl = new ArrayList<String>();
         ddl.add("CREATE TABLE test_table1 (id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), k1 varchar(32), v1 varchar(32))");
         ddl.add("CREATE TABLE test_table2 (id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), k2 varchar(32), v2 varchar(32))");
         ddl.add("CREATE TABLE test_table3 (id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), k3 varchar(32), v3 varchar(32))");
         DatabaseUtil.writeDDL(ddl, connection);
         
         ResultSet rs = connection.createStatement().executeQuery("SELECT TABLENAME FROM SYS.SYSTABLES WHERE TABLENAME LIKE 'test_table%'");
         
         int rowCount = 1;
         while (rs.next()) {
             String tableName = rs.getString(1);
             assertThat(tableName, is(equalTo("test_table" + rowCount)));
             rowCount++;
         }
         
         DatabaseUtil.closeConnection(connection);
     }
     
     @Test 
     public void testWriteDML() throws SQLException, NamingException, ClassNotFoundException {
         String DB_FULL_LOCATION = DB_LOCATION + "test1.db";
         createDb = "jdbc:derby:" + DB_FULL_LOCATION + ";create=true;";
         destroyDb = "jdbc:derby:" + DB_FULL_LOCATION + ";shutdown=true;";
         DatabaseUtil.setupDatabase(createDb, "org.apache.derby.jdbc.EmbeddedDriver");
         connection = DatabaseUtil.getConnection("java:comp/env/jdbc/test1.db");
         
         List<String> dml = new ArrayList<String>();
         dml.add("CREATE TABLE test_table (id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), k varchar(32), v varchar(32))");
         dml.add("INSERT INTO test_table (k, v) values ('test k 1','test v 1')");
         dml.add("INSERT INTO test_table (k, v) values ('test k 2','test v 2')");
         dml.add("INSERT INTO test_table (k, v) values ('test k 3','test v 3')");
         dml.add("INSERT INTO test_table (k, v) values ('test k 4','test v 4')");
         dml.add("INSERT INTO test_table (k, v) values ('test k 5','test v 5')");
         dml.add("INSERT INTO test_table (k, v) values ('test k 6','test v 6')");
         DatabaseUtil.writeDML(dml, connection);
         
         ResultSet rs = connection.createStatement().executeQuery("SELECT k, v FROM TEST_TABLE");
         
         int rowCount = 1;
         while (rs.next()) {
             String kColumnValue = rs.getString(1);
             String vColumnValue = rs.getString(2);
             assertThat(kColumnValue, is(equalTo("test k " + rowCount)));
             assertThat(vColumnValue, is(equalTo("test v " + rowCount)));
             rowCount++;
         }
         
         DatabaseUtil.closeConnection(connection);
     }
     
 //    @Test
 //    public void testSetupDatabaseConnectionWithIncorrectJNDIContext() throws SQLException, NamingException, ClassNotFoundException {
 //        String DB_FULL_LOCATION = DB_LOCATION + "test2.db";
 //        createDb = "jdbc:derby:" + DB_FULL_LOCATION + ";create=true;";
 //        destroyDb = "jdbc:derby:" + DB_FULL_LOCATION + ";shutdown=true;";
 //        DatabaseUtil.setupDatabase(createDb, "org.apache.derby.jdbc.EmbeddedDriver");
 //        connection = DatabaseUtil.getConnection("java:comp/env/jdbc/wrong.db");
 //        assertThat(connection.isClosed(), is(false));
 //    }
 //    
 //    @Test
 //    public void testSetupDatabaseConnectionWithNoContext() throws SQLException, NamingException, ClassNotFoundException {
 //        String DB_FULL_LOCATION = DB_LOCATION + "derp.db";
 //        createDb = "jdbc:derby:" + DB_FULL_LOCATION + ";create=true;";
 //        destroyDb = "jdbc:derby:" + DB_FULL_LOCATION + ";shutdown=true;";
 //        DatabaseUtil.setupDatabase(createDb, "org.apache.derby.jdbc.EmbeddedDriver");
 //        connection = DatabaseUtil.getConnection();
 //        assertThat(connection.isClosed(), is(false));
 //    }
 }
