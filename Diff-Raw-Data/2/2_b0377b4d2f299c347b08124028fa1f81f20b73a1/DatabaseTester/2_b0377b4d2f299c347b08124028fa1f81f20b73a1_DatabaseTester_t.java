 package com.seitenbau.testing.dbunit;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.dbcp.BasicDataSource;
 
 import com.seitenbau.testing.dbunit.modifier.IDataSetModifier;
 import com.seitenbau.testing.dbunit.tester.DatabaseTesterBase;
 import com.seitenbau.testing.util.Future;
 
 /**
  * Class for DBUbit Tests that does not have to be derived. For class that
  * holds static connection information see {@link AbstractDBUnitTests}.
  * <p/>
  * It is recommended to store the DatabaseTester as a field inside the JUnit Test:<code><pre>
  * private DatabaseTester dbTester;
  * </pre></code>Create an instance while setting up the test: <code><pre>
  *  &#064;Before
  *  public void setUp()
  *  {
  *     dbTester = new DatabaseTester(
  *        "org.gjt.mm.mysql.Driver",
  *        "jdbc:mysql://192.168.0.42:3306/my_database_name",
  *        "user",
  *        "password",
  *        getClass()
  *       );
  *  }
  * </pre></code> Inside the test several methods are accessible on the DatabaseTester object:<code><pre>
  *  &#064;Test
  *  public void testDatabaseWhatever() {
  *     dbTester.cleanInsert("testDatabaseWhatever_prepare.xml");
  *     [... do call target ...]
  *     dbTester.assertDatabase("testDatabaseWhatever_xpect.xml");
  *  }
  *  </pre></code>
  */
 public class DatabaseTester extends DatabaseTesterBase<DatabaseTester>
 {
   /**
    * Constructor that sets the database connection information.
    * 
    * <code><pre>
    *     dbTester = new DatabaseTester(
    *        "org.gjt.mm.mysql.Driver",
    *        "jdbc:mysql://192.168.0.42:3306/my_database_name",
    *        "user",
    *        "password"
    *       );
    * </pre></code>
    * 
    * @param driverName Class name for the database driver.
    * 
    * @param url URL of the database.
    * 
    * @param username The name of the user.
    * 
    * @param password The password for the user.
    * 
    * @param defaultModifiers the default modifiers that should be
    *        applied to datasets.
    */
   public DatabaseTester(String driverName, String url, String username, String password,
       IDataSetModifier... defaultModifiers)
   {
     super(driverName, url, username, password, defaultModifiers);
   }
 
   /**
    * Constructor that sets the database connection information.
    * 
    * <code><pre>
    * dbTester = new DatabaseTester(
    * "org.gjt.mm.mysql.Driver",
    * "jdbc:mysql://192.168.0.42:3306/my_database_name",
    * "user",
    * "password",
    * getClass()
    * );
    * </pre></code>
    * 
    * @param driverName Class name for the database driver.
    * @param url URL of the database.
    * @param username The name of the user.
    * @param password The password for the user.
    * @param clazz Class object that is used to determine the proper
    *        package while loading XML DataSets.
    * @param defaultModifiers the default modifiers that should be
    *        applied to datasets.
    */
   public DatabaseTester(String driverName, String url, String username, String password, Class<?> clazz,
       IDataSetModifier... defaultModifiers)
   {
     super(driverName, url, username, password, clazz, defaultModifiers);
   }
 
   /**
    * Constructor that sets the database connection information.
    * 
    * <code><pre>
    *     dbTester = new DatabaseTester(
    *        "org.gjt.mm.mysql.Driver",
    *        "jdbc:mysql://192.168.0.42:3306/my_database_name",
    *        "user",
    *        "password",
    *        getClass()
    *       );
    * </pre></code>
    * 
    * @param driverName Class name for the database driver.
    * @param url URL of the database.
    * @param username The name of the user.
    * @param password The password for the user.
   * @param schema The database schema.
    * @param defaultModifiers the default modifiers that should be
    *        applied to datasets.
    */
   public DatabaseTester(String driverName, String url, String username, String password, String schema,
       IDataSetModifier... defaultModifiers)
   {
     super(driverName, url, username, password, schema, defaultModifiers);
   }
 
   public DatabaseTester(DataSource ds, String schema, IDataSetModifier... defaultModifiers)
   {
     super(ds, schema, defaultModifiers);
   }
 
   public DatabaseTester(Future<DataSource> lazySource, IDataSetModifier... defaultModifiers)
   {
     super(lazySource, defaultModifiers);
   }
 
   public DatabaseTester(BasicDataSource dataSource, IDataSetModifier... defaultModifiers)
   {
     super(dataSource, defaultModifiers);
   }
 
   public DatabaseTester(Class<? extends TestConfigDatabase> configClass, IDataSetModifier... defaultModifiers)
   {
     super(configClass, defaultModifiers);
   }
 
 }
