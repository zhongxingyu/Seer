 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package badm.models;
 
 import badm.Audit;
 import java.util.ArrayList;
 import static org.junit.Assert.assertEquals;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * Audit Bean Test
  *
  * @author Colby Rabideau
  */
 public class AuditBeanTest {
 
 	/**
      * Audit
      */
     public static Audit audit;
 
     /**
      * Set Up Class
      *
      * @return void
      */
     @BeforeClass
     public static void setUpClass() {
         audit = new Audit();
     }
 
     /**
      * [test] Timestamp
      *
      * @return void
      */
     @Test
     public void testTimestamp() {
         Integer test = 123;
        audit.setTimeStamp(test);
        assertEquals(test, audit.getTimeStamp());
     }
 
     /**
      * [test] Updated
      *
      * @return void
      */
     @Test
     public void testUpdated() {
         ArrayList<Integer> test = new ArrayList();
         audit.setUpdated(test);
         assert(test.equals(audit.getUpdated()));
     }
 
     /**
      * [test] Description
      *
      * @return void
      */
     @Test
     public void testDescription() {
         String test = "This is the coolest audit ever.";
         audit.setDescription(test);
         assertEquals(test, audit.getDescription());
     }
 
     /**
      * [test] Budget Id
      *
      * @return void
      */
     @Test
     public void testBudgetId() {
         Integer test = 1;
         audit.setBudgetId(test);
         assertEquals(test, audit.getBudgetId());
     }
 
     /**
      * [test] Value
      *
      * @return void
      */
     @Test
     public void testValue() {
         Double test = 20.12314;
         audit.setValue(test);
         assertEquals(test, audit.getValue());
     }
 
 }
