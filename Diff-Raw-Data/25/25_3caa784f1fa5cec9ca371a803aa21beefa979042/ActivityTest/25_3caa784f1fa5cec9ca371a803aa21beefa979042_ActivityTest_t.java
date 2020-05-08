 package de.aidger.model.models;
 
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 import java.sql.Date;
 import java.util.List;
 
 /**
  * Tests the Activity class.
  *
  * @author aidGer Team
  */
 public class ActivityTest {
 
 
     private Activity activity = null;
 
     /**
      * Sets the Activity class up for tests.
      */
     @Before
     public void setUp() {
         activity = new Activity();
         activity.setId(1);
         activity.setAssistantId(1);
         activity.setContent("New assistant");
         activity.setCourseId(1);
         activity.setDate(new Date(100));
         activity.setDocumentType("Test Type");
         activity.setProcessor("T");
         activity.setRemark("Remark");
         activity.setSender("Test Sender");
         activity.setType("Test Type");
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
         a.setQualification("Q");
         a.save();
         System.out.println(a.getId());
 
         activity.setAssistantId(a.getId());
        activity.setNew(true);
         activity.save();
         System.out.println(activity.getId());
 
         //TODO: Currently not implemented by AdoHive
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
 
         Course course = new Course();
         course.setAdvisor("Tester");
         course.setDescription("Description");
         course.setFinancialCategoryId(1);
         course.setGroup("2");
         course.setLecturer("Test Tester");
         course.setNumberOfGroups(3);
         course.setPart('a');
         course.setRemark("Remark");
         course.setScope("Sniper Scope");
         course.setSemester("SS 09");
         course.setTargetAudience("Testers");
         course.setUnqualifiedWorkingHours(100);
         course.save();
 
         activity.setCourseId(course.getId());
        activity.setNew(true);
         activity.save();
 
         //TODO: Currently not implemented by AdoHive
         List result = activity.getActivities(course);
 
         assertNotNull(result);
         assertTrue(result.size() > 0);
     }
 
 }
