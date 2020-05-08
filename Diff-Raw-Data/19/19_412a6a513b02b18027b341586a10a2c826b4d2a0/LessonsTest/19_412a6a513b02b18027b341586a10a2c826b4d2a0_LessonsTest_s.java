 package com.huskysoft.eduki.test;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.huskysoft.eduki.data.Lesson;
 
 import android.test.AndroidTestCase;
 
 /**
  * 
  * @author Cody Thomas LessonsTests contains unit tests for lessons
  *
  */
 public class LessonsTest extends AndroidTestCase {
 
     private static final int TIMEOUT = 3000;
     
     private Lesson lesson;
     private int id;
     private int c_id;
     private String title;
     private String body;
     
     /**
      * Construct the lesson
      */
     @Before
     public void setUp() throws Exception {
         super.setUp();
         id = 0;
         c_id = 1;
         title = "Foobars";
         body = "Super body";
         lesson = new Lesson(id, title, c_id, body);
     }
     
     /**
      * Test the lesson's toString
      */
     @Test(timeout=TIMEOUT)
 	public void testToString() {
		assertEquals(title, lesson.toString());
 	}
 	
     /**
      * Test the getId method
      */
 	@Test(timeout=TIMEOUT)
 	public void testGetId() {
 	    assertEquals(id, lesson.getId());
 	}
 	
 	/**
 	 * Test the get course id method
 	 */
 	@Test(timeout=TIMEOUT)
 	public void testGetCourseId() {
 	    assertEquals(c_id, lesson.getCourseId());
 	}
 	
 	/**
 	 * Test the get title method
 	 */
 	@Test(timeout=TIMEOUT)
 	public void testGetTitle() {
	    assertEquals(title, lesson.getTitle());
 	}
 	
 	/**
 	 * Test the get body method
 	 */
 	@Test(timeout=TIMEOUT)
 	public void testGetBody() {
 	    assertEquals(body, lesson.getBody());
 	}
 
 }
