 package tests;
 
 import junit.framework.JUnit4TestAdapter;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 
 
 @RunWith(Suite.class)
 @Suite.SuiteClasses({
 		ObserverTests.class,
 		ParserTests.class,
		IntegrationTests.class,
		DataStoreTests.class
         })
         
 public class TestSuite {
     // Used for backward compatibility (IDEs, Ant and JUnit 3 text runner)	
     public static junit.framework.Test suite() {
         return new JUnit4TestAdapter(TestSuite.class);
     }
 
 
 }
