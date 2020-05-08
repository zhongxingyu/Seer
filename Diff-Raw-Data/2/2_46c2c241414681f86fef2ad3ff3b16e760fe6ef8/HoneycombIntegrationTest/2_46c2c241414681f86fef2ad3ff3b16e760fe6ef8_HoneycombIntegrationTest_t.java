 package integrationtests;
 
 import com.nearinfinity.honeycomb.mysql.Bootstrap;
 import com.nearinfinity.honeycomb.mysql.HandlerProxy;
 import com.nearinfinity.honeycomb.mysql.HandlerProxyFactory;
 import com.nearinfinity.honeycomb.mysql.schema.TableSchema;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Rule;
 import org.junit.rules.TestRule;
 import org.junit.rules.TestWatcher;
 import org.junit.runner.Description;
 
 /**
  * Test class used for running integration tests. Ensures setup and teardown
  * of the application framework between test classes in the test suite.
  */
 public abstract class HoneycombIntegrationTest {
     @Rule
     public final TestRule watcher = new TestWatcher() {
         @Override
         protected void starting(Description description) {
             System.out.println(String.format("Executing: %s", description.getMethodName()));
         }
     };
 
     protected static HandlerProxyFactory factory;
     protected HandlerProxy proxy;
 
     @BeforeClass
     public static void setupFramework() {
        factory = Bootstrap.startup("/usr/share/mysql/honeycomb/honeycomb.xml", "/usr/share/mysql/honeycomb/honeycomb.xsd");
         System.out.println("Initialized application framework");
     }
 
     @Before
     public void setupTestCase() {
         proxy = factory.createHandlerProxy();
 
         proxy.createTable(TestConstants.TABLE_NAME,
                 getTableSchema().serialize(), 1);
 
         proxy.openTable(TestConstants.TABLE_NAME);
     }
 
     @After
     public void teardownTestCase() {
         proxy.closeTable();
 
         proxy.dropTable(TestConstants.TABLE_NAME);
     }
 
     /**
      * Provides the {@link TableSchema} to use for a test case
      *
      * @return The schema used for testing
      */
     protected TableSchema getTableSchema() {
         return TestConstants.TABLE_SCHEMA;
     }
 }
