 package server;
 
 import org.junit.After;
 import org.junit.Before;
 
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import org.testng.annotations.Test;
 
 /**
  * Performs datastore setup, as described <a
  * href="http://code.google.com/appengine/docs/java/howto/unittesting.html">here</a>.
  *
  * @author androns
  */
 public abstract class LocalDatastoreTest {
 
     private final LocalServiceTestHelper helper =
         new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
 
 
     /**
      *
      */
     @Before
     public void setUp() {
         this.helper.setUp();
     }
 
     /**
      * @see LocalServiceTest#tearDown()
      */
     @After
     public void tearDown() {
         this.helper.tearDown();
     }
 
     @Test
     public void testIndex() {
        assert new Integer(1).equals(1);
     }
 }
