 package de.aidger.model.models;
 
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import java.sql.Date;
 import java.util.List;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import java.util.GregorianCalendar;
 
 /**
  * Tests the Employment class.
  *
  * @author aidGer Team
  */
 public class EmploymentTest {
 
     private Employment employment = null;
 
     private static Assistant assistant = null;
 
     private static Contract contract = null;
 
     private static Course course = null;
 
     private static FinancialCategory financial = null;
 
     @BeforeClass
     public static void beforeClassSetUp() throws AdoHiveException {
         de.aidger.model.Runtime.getInstance().initialize();
 
         assistant = new Assistant();
         assistant.setEmail("test@example.com");
         assistant.setFirstName("Test");
         assistant.setLastName("Tester");
         assistant.setQualification("g");
         assistant.save();
 
         financial = new FinancialCategory();
         financial.setBudgetCosts(new int[] { 100, 200 });
         financial.setFunds(new int[] { 10001000, 20002000 });
         financial.setName("Tester");
         financial.setYear((short) 2010);
         financial.save();
 
         course = new Course();
         course.setAdvisor("Tester");
         course.setDescription("Description");
         course.setFinancialCategoryId(financial.getId());
         course.setGroup("2");
         course.setLecturer("Test Tester");
         course.setNumberOfGroups(3);
         course.setPart('a');
         course.setRemark("Remark");
         course.setScope("Sniper Scope");
         course.setSemester("SS09");
         course.setTargetAudience("Testers");
         course.setUnqualifiedWorkingHours(100);
         course.save();
 
         contract = new Contract();
         contract.setCompletionDate(new Date(100));
         contract.setConfirmationDate(new Date(10));
         contract.setDelegation(false);
         contract.setEndDate(new Date(1000));
         contract.setStartDate(new Date(20));
         contract.setType("Type");
         contract.save();
     }
 
     @Before
     public void setUp() {
         employment = new Employment();
         employment.setId(1);
         employment.setAssistantId(assistant.getId());
         employment.setContractId(contract.getId());
         employment.setCourseId(course.getId());
         employment.setCostUnit("0711");
         employment.setFunds(1);
         employment.setHourCount(40);
         employment.setMonth((byte) 10);
         employment.setQualification("g");
         employment.setRemark("Remark");
         employment.setYear((short) 2010);
     }
 
     /**
      * Test of constructor, of class Employment.
      */
     @Test
     public void testConstructor() throws AdoHiveException {
         System.out.println("Constructor");
 
         employment.setNew(true);
         employment.save();
 
         Employment result = new Employment(employment.getById(
                 employment.getId()));
 
         assertNotNull(result);
         assertEquals(employment, result);
     }
 
     /**
      * Test of validation, of class Employment.
      */
     @Test
     public void testValidation() throws AdoHiveException {
         System.out.println("Validation");
 
         employment.setNew(true);
         assertTrue(employment.save());
 
         employment.setAssistantId(-1);
         assertFalse(employment.save());
         employment.resetErrors();
         employment.setAssistantId(assistant.getId());
 
         employment.setContractId(-1);
         assertFalse(employment.save());
         employment.resetErrors();
         employment.setContractId(contract.getId());
 
         employment.setCostUnit(null);
         assertFalse(employment.save());
         employment.resetErrors();
         employment.setCostUnit("0711");
 
         employment.setCourseId(-1);
         assertFalse(employment.save());
         employment.resetErrors();
         employment.setCourseId(course.getId());
 
         employment.setFunds(0);
         assertFalse(employment.save());
         employment.resetErrors();
         employment.setFunds(1);
 
         employment.setHourCount(-1.0);
         assertFalse(employment.save());
         employment.resetErrors();
         employment.setHourCount(40);
 
         employment.setMonth((byte) 0);
         assertFalse(employment.save());
         employment.resetErrors();
         employment.setMonth((byte) 10);
 
         employment.setQualification(null);
         assertFalse(employment.save());
         employment.resetErrors();
 
         employment.setQualification("Q");
         assertFalse(employment.save());
         employment.resetErrors();
         employment.setQualification("g");
 
         employment.setYear((short) 999);
         assertFalse(employment.save());
         employment.resetErrors();
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
 
         employment.clearTable();
         employment.setMonth((byte) 10);
         employment.setYear((short) 2010);
         employment.setNew(true);
         employment.save();
 
         employment.setNew(true);
         employment.setMonth((byte) 11);
         employment.save();
 
         List result = employment.getEmployments(start, end);
 
         assertNotNull(result);
         assertTrue(result.size() == 2);
     }
 
     /**
      * Test of getEmployments method, of class Employment.
      */
     @Test
     public void testGetEmployments_short_byte_short_byte()
             throws AdoHiveException {
         System.out.println("getEmployments");
 
         employment.clearTable();
         employment.setMonth((byte) 7);
         employment.setYear((short) 2010);
         employment.setNew(true);
         employment.save();
         
         List result = employment.getEmployments((short) 2010, (byte) 7,
                 (short) 2010, (byte) 8);
 
         assertNotNull(result);
         assertTrue(result.size() == 1);
         assertEquals(employment, result.get(0));
     }
 
     /**
      * Test of getEmployments method, of class Employment.
      */
     @Test
     public void testGetEmployments_Contract() throws AdoHiveException {
         System.out.println("getEmployments");
 
         employment.clearTable();
         employment.setContractId(contract.getId());
         employment.setMonth((byte) 7);
         employment.setYear((short) 2010);
         employment.setNew(true);
         employment.save();
 
         List result = employment.getEmployments(contract);
 
         assertNotNull(result);
         assertTrue(result.size() == 1);
         assertEquals(employment, result.get(0));
     }
 
     /**
      * Test of getEmployments method, of class Employment.
      */
     @Test
     public void testGetEmployments_Assistant() throws AdoHiveException {
         System.out.println("getEmployments");
 
         employment.clearTable();
         employment.setAssistantId(assistant.getId());
         employment.setMonth((byte) 7);
         employment.setYear((short) 2010);
         employment.setNew(true);
         employment.save();
 
         List result = employment.getEmployments(assistant);
 
         assertNotNull(result);
         assertTrue(result.size() == 1);
         assertEquals(employment, result.get(0));
     }
 
     /**
      * Test of getEmployments method, of class Employment.
      */
     @Test
     public void testGetEmployments_Course() throws AdoHiveException {
         System.out.println("getEmployments");
 
         employment.clearTable();
         employment.setCourseId(course.getId());
         employment.setMonth((byte) 7);
         employment.setYear((short) 2010);
         employment.setNew(true);
         employment.save();
 
         List result = employment.getEmployments(course);
 
         assertNotNull(result);
         assertTrue(result.size() == 1);
         assertEquals(employment, result.get(0));
     }
 
 }
