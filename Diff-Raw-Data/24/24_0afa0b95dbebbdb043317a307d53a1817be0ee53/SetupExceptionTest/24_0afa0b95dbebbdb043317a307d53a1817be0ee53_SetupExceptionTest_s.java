 /*
 * $Id: SetupExceptionTest.java,v 1.1 2003-01-29 23:31:36 o_rossmueller Exp $
  *
  */
 package org.junitee.ejb.einstein.test;
 
 
 import junit.framework.TestCase;
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 
 /**
  * This test is to reproduce bug #676419.
  *
 * @version $Revision: 1.1 $
  * @author <a href="mailto:oliver@oross.net">Oliver Rossmueller</a>
  */
 public class SetupExceptionTest extends TestCase {
 
 
   public static Test suite() {
     TestSuite suite = new TestSuite();
     suite.addTest(new SetupExceptionTest("testRun"));
     return new JUnitEETestSetup(suite);
   }
 
   public SetupExceptionTest(String s) {
     super(s);
   }
 
 
   public void setUp() throws Exception {
   }
 
 
   public void testRun() {
     assertTrue(true);
    fail("This is bug 676419");
   }


 }
 
