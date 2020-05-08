 package jamm;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
// import junit.extensions.TestSetup;
 
 public class AllTests
 {
     public static Test suite()
     {
         TestSuite suite;
 
         suite = new TestSuite();
         suite.addTest(jamm.ldap.AllTests.suite());
         suite.addTest(jamm.backend.AllTests.suite());
         
         return suite;
     }
 }
