 package de.aidger.model.models;
 
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import java.sql.Date;
 import java.util.List;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import java.util.GregorianCalendar;
 
 /**
  * Tests the Employment class.
  *
  * @author aidGer Team
  */
 public class EmploymentTest {
 
     protected Employment employment = null;
 
     @Before
     public void setUp() {
         employment = new Employment();
         employment.setId(1);
         employment.setAssistantId(1);
         employment.setContractId(1);
         employment.setCourseId(0);
         employment.setCostUnit("0711");
         employment.setFunds(1);
         employment.setHourCount(40);
         employment.setMonth((byte) 10);
        employment.setQualification("Q");
         employment.setRemark("Remark");
         employment.setYear((short) 2010);
     }
 
     /**
      * Test of clone method, of class Employment.
      */
     @Test
     public void testClone() {
         System.out.println("clone");
 
         Employment result = employment.clone();
 
         assertEquals(result.getId(), employment.getId());
         assertEquals(result.getAssistantId(), employment.getAssistantId());
         assertEquals(result.getContractId(), employment.getContractId());
         assertEquals(result.getCostUnit(), employment.getCostUnit());
         assertEquals(result.getCourseId(), employment.getCourseId());
         assertEquals(result.getFunds(), employment.getFunds());
         assertEquals(result.getHourCount(), employment.getHourCount(), 0.001);
         assertEquals(result.getMonth(), employment.getMonth());
         assertEquals(result.getQualification(), employment.getQualification());
         assertEquals(result.getRemark(), employment.getRemark());
         assertEquals(result.getYear(), employment.getYear());
     }
 
     /**
      * Test of equals method, of class Employment.
      */
     @Test
     public void testEquals() {
         System.out.println("equals");
         Employment result = employment.clone();
 
         assertEquals(employment, result);
         assertFalse(employment.equals(new Object()));
     }
 
     /**
      * Test of hashCode method, of class Assistant.
      */
     @Test
     public void testHashCode() {
         System.out.println("hashCode");
         Employment result = employment.clone();
 
         assertEquals(employment.hashCode(), result.hashCode());
     }
 
     /**
      * Test of getEmployments method, of class Employment.
      */
     @Test
     public void testGetEmployments_Date_Date() throws AdoHiveException {
         System.out.println("getEmployments");
         Date start = new Date(new GregorianCalendar(2010, 10, 1).getTime().
                 getTime());
         Date end = new Date(new GregorianCalendar(2010, 12, 1).getTime().
                 getTime());
 
         employment.setMonth((byte) 10);
         employment.setYear((short) 2010);
         employment.setId(-1);
         employment.save();
 
         employment.setId(-1);
         employment.setMonth((byte) 11);
         employment.save();
 
         //TODO: Currently not implemented by AdoHive
         List result = employment.getEmployments(start, end);
 
         assertNotNull(result);
         assertTrue(result.size() == 1);
     }
 
     /**
      * Test of getEmployments method, of class Employment.
      */
     @Test
     public void testGetEmployments_String() throws AdoHiveException {
         System.out.println("getEmployments");
         String semester = "SS 10";
 
         employment.setMonth((byte) 7);
         employment.setYear((short) 2010);
         employment.setId(-1);
         employment.save();
         
         //TODO: Currently not implemented by AdoHive
         List result = employment.getEmployments(semester);
 
         assertNotNull(result);
         assertTrue(result.size() == 1);
     }
 
 }
