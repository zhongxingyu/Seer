 package com.huskysoft.eduki.test;
 
 
 import android.test.AndroidTestCase;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.huskysoft.eduki.data.Course;
 
 /**
  * 
  * @author Cody Thomas CourseTest are unit tests for courses
  *
  */
 public class CourseTest extends AndroidTestCase {
 
     private static final int TIMEOUT = 3000;
     private Course course;
     private int id;
     private String title;
     
     /**
      * Creates a course
      */
     @Before
     public void setUp() throws Exception {
         super.setUp();
         title = "OH GOD I HOPE THESE tests PASS";
         id = 100;
         course = new Course(id, title);
     }
 
     /**
      * Test the getId method
      */
     @Test(timeout=TIMEOUT)
     public void testGetId() {
         assertEquals(id, course.getId());
     }
     
     /**
      * Test the getTitle method
      */
     @Test(timeout=TIMEOUT)
     public void testGetTitle() {
        assertEquals(title.toUpperCase(), course.getTitle());
     }
 
 }
