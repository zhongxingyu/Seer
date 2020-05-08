 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.wgr.xenmaster.monitoring;
 
 import net.wgr.xenmaster.api.GuestMetrics;
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.TTCCLayout;
 import org.apache.log4j.Logger;
 import net.wgr.xenmaster.controller.Controller;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author double-u
  */
 public class RecordTest {
 
     public RecordTest() {
         
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
         Logger root = Logger.getRootLogger();
         root.setLevel(Level.DEBUG);
         root.addAppender(new ConsoleAppender(new TTCCLayout()));
     }
 
     @After
     public void tearDown() {
     }
 
     //@Test http://lists.xensource.com/archives/html/xen-devel/2011-10/msg00584.html
    @Test
     public void testRecord() {
         GuestMetrics.getAll();
         GuestMetrics guestMetrics = Controller.getSession().getThisHost().getResidentVMs().get(0).getGuestMetrics();
         Record r = new Record(Controller.getSession().getThisHost().getResidentVMs().get(0).getReference(), true);
         assertNotNull(r);
     }
 }
