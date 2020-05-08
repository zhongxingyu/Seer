 /*
 * $Id: JUnitEETestSetup.java,v 1.2 2003-04-10 19:55:43 o_rossmueller Exp $
  */
 package org.junitee.ejb.einstein.test;
 
 
 import junit.framework.Test;
 import junit.extensions.TestSetup;
 
 
 /**
  * @author <a href="mailto:oliver@tuxerra.com">Oliver Rossmueller</a>
 * @version $Revision: 1.2 $
  */
 public class JUnitEETestSetup extends TestSetup {
 
   public JUnitEETestSetup(Test test) {
     super(test);
   }
 
   
   protected void setUp() throws Exception {
     super.setUp();
    throw new Exception("TestSetup.setUp");
   }
 
 
   protected void tearDown() throws Exception {
     super.tearDown();
    throw new Exception("TestSetup.tearDown");
   }
 }
