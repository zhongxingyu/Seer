 package de.aidger.model.models;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.sql.Date;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 
 /**
  * Tests the Activity class.
  * 
  * @author aidGer Team
  */
 public class ActivityTest {
 
     private Activity activity = null;
 
     private static Assistant assistant = null;
 
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
         financial.setBudgetCosts(new Integer[] { 100, 200 });
         financial.setCostUnits(new Integer[] { 10001000, 20002000 });
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
         course.setUnqualifiedWorkingHours(100.0);
         course.save();
     }
 
     /**
      * Sets the Activity class up for tests.
      */
     @Before
     public void setUp() {
         activity = new Activity();
         activity.setId(1);
         activity.setAssistantId(assistant.getId());
         activity.setContent("New assistant");
         activity.setCourseId(course.getId());
         activity.setDate(new Date(100));
         activity.setDocumentType("Test Type");
         activity.setProcessor("T");
         activity.setRemark("Remark");
         activity.setSender("Test Sender");
         activity.setType("Test Type");
     }
 
     /**
      * Test of constructor, of class Activity.
      */
     @Test
     public void testConstructor() throws AdoHiveException {
         System.out.println("Constructor");
 
         activity.setNew(true);
         activity.save();
 
         Activity result = new Activity(activity.getById(activity.getId()));
 
         assertNotNull(result);
         assertEquals(activity, result);
     }
 
     /**
      * Test of validation, of class Activity.
      */
     @Test
     public void testValidation() throws AdoHiveException {
         System.out.println("Validation");
 
         activity.setNew(true);
 
         activity.setAssistantId(0);
         assertFalse(activity.save());
         activity.resetErrors();
         activity.setAssistantId(assistant.getId());
 
         activity.setContent(null);
         assertFalse(activity.save());
         activity.resetErrors();
         activity.setContent("New assistant");
 
         activity.setCourseId(0);
         assertFalse(activity.save());
         activity.resetErrors();
         activity.setCourseId(course.getId());
 
         activity.setDate(null);
         assertFalse(activity.save());
         activity.resetErrors();
         activity.setDate(new Date(100));
 
         activity.setDocumentType(null);
         assertFalse(activity.save());
         activity.resetErrors();
 
         activity
             .setDocumentType("012345678901234567890123456789012345678901234567890");
         assertFalse(activity.save());
         activity.resetErrors();
         activity.setDocumentType("Type");
 
         activity.setProcessor(null);
         assertFalse(activity.save());
         activity.resetErrors();
 
        activity.setProcessor("Tester");
         assertFalse(activity.save());
         activity.resetErrors();
        activity.setProcessor("T");
 
         activity.setType(null);
         assertFalse(activity.save());
         activity.resetErrors();
 
         activity.setType("012345678901234567890");
         assertFalse(activity.save());
         activity.resetErrors();
     }
 
     /**
      * Test of clone method, of class Activity.
      */
     @Test
     public void testClone() {
         System.out.println("clone");
 
         Activity result = activity.clone();
 
         assertEquals(activity.getId(), result.getId());
         assertEquals(activity.getAssistantId(), result.getAssistantId());
         assertEquals(activity.getContent(), result.getContent());
         assertEquals(activity.getCourseId(), result.getCourseId());
         assertEquals(activity.getDate(), result.getDate());
         assertEquals(activity.getDocumentType(), result.getDocumentType());
         assertEquals(activity.getProcessor(), result.getProcessor());
         assertEquals(activity.getRemark(), result.getRemark());
         assertEquals(activity.getSender(), result.getSender());
         assertEquals(activity.getType(), result.getType());
     }
 
     /**
      * Test of equals method, of class Activity.
      */
     @Test
     public void testEquals() {
         System.out.println("equals");
         Activity result = activity.clone();
 
         assertEquals(activity, result);
         assertFalse(activity.equals(new Object()));
     }
 
     /**
      * Test of hashCode method, of class Activity.
      */
     @Test
     public void testHashCode() {
         System.out.println("hashCode");
         Activity result = activity.clone();
 
         assertEquals(activity.hashCode(), result.hashCode());
     }
 
     /**
      * Test of getActivities method, of class Activity.
      */
     @Test
     public void testGetActivities_Assistant() throws AdoHiveException {
         System.out.println("getActivities");
 
         Assistant a = new Assistant();
         a.setEmail("test@example.com");
         a.setFirstName("Test");
         a.setLastName("Tester");
         a.setQualification("g");
         a.save();
 
         activity.setAssistantId(a.getId());
         activity.setNew(true);
         activity.save();
 
         List result = activity.getActivities(a);
 
         assertNotNull(result);
         assertTrue(result.size() == 1);
     }
 
     /**
      * Test of getActivities method, of class Activity.
      */
     @Test
     public void testGetActivities_Course() throws AdoHiveException {
         System.out.println("getActivities");
 
         activity.setCourseId(course.getId());
         activity.setNew(true);
         activity.save();
 
         List result = activity.getActivities(course);
 
         assertNotNull(result);
         assertTrue(result.size() > 0);
     }
 
     /**
      * Test of getActivities method, of class Activity.
      */
     @Test
     public void testGetActivities_Date_Date() throws AdoHiveException {
         System.out.println("getActivities");
 
         List result = activity.getActivities(new Date(99), new Date(101));
 
         assertNotNull(result);
         assertTrue(result.size() >= 3);
     }
 
 }
