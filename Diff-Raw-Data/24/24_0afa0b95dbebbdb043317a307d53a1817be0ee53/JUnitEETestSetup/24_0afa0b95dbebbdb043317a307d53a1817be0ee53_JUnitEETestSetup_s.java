 /*
 * $Id: JUnitEETestSetup.java,v 1.1 2003-01-29 23:31:36 o_rossmueller Exp $
  */
 package org.junitee.ejb.einstein.test;
 
 
 import junit.framework.Test;
 import junit.extensions.TestSetup;
 
 
 /**
  * @author <a href="mailto:oliver@tuxerra.com">Oliver Rossmueller</a>
 * @version $Revision: 1.1 $
  */
 public class JUnitEETestSetup extends TestSetup {
 
   public JUnitEETestSetup(Test test) {
     super(test);
   }
 
   
   protected void setUp() throws Exception {
     super.setUp();
   }
 
 
   protected void tearDown() throws Exception {
     super.tearDown();
   }
 }
