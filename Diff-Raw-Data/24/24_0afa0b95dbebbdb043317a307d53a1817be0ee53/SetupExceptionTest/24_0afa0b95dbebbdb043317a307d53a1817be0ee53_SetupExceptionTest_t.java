 /*
 * $Id: SetupExceptionTest.java,v 1.2 2003-04-10 19:55:43 o_rossmueller Exp $
  *
  */
 package org.junitee.ejb.einstein.test;
 
 
 import junit.framework.TestCase;
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 
 /**
  * This test is to reproduce bug #676419.
  *
 * @version $Revision: 1.2 $
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
   }
 }
 
