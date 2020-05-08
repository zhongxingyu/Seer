 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
 import gx.realtime.CollaborativeListTest;
 import gx.realtime.CollaborativeMapTest;
 import gx.realtime.CollaborativeObjectTest;
 import gx.realtime.CollaborativeStringTest;
 import gx.realtime.EventTargetTest;
 import gx.realtime.IndexReferenceTest;
 import gx.realtime.ClientTest;
 import gx.realtime.ModelTest;
 
 /**
  * Test suite for all non-db errors
  * @author Rdebokx
  *
  */
 @RunWith(Suite.class)
 @SuiteClasses({
 	//gx.realtime tests
 	CollaborativeListTest.class,
 	CollaborativeMapTest.class,
 	CollaborativeObjectTest.class,
 	CollaborativeStringTest.class,
 	EventTargetTest.class,
 	IndexReferenceTest.class,
	ClientTest.class,
	ModelTest.class
 })
 
 public class AllTests {
 
 	private AllTests(){
 		
 	}
 	
 	public static void main(String[] args) {
         org.junit.runner.JUnitCore.runClasses(AllTests.class);
 	}
 
 }
