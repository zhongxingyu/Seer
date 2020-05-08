 package crp;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
 /**
 * This class is the test suit for all the test cases
* @author mohan
 */
 @RunWith(Suite.class)
 @SuiteClasses({
 	LoginServiceTestCases.class,
 	EmailServiceTest.class,
 	ScheduleServiceTest.class,
 	MyBatisConnectionFactoryTest.class,
 	CrsDAOTest.class
 })public class CRPTestSuite
 {
  /* empty class */
 }
