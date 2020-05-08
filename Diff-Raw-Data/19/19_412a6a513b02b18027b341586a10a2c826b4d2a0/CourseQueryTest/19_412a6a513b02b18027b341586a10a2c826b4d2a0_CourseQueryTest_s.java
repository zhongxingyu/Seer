 package com.huskysoft.eduki.test;
 
 import android.test.AndroidTestCase;
 
 import java.util.List;
 
 import org.junit.Test;
 
 import com.huskysoft.eduki.data.Course;
 import com.huskysoft.eduki.data.CourseQuery;
 
 
 /**
  * 
  * @author Cody Thomas CourseQueryTests are unit tests for the CourseQuery class
  *
  */
 public class CourseQueryTest extends AndroidTestCase {
 
     private static final int TIMEOUT = 5000;
     
     /**
      * Tests the courseQuery's parseCourseList method
      */
     @Test(timeout=TIMEOUT)
     public void testParsingList() {
         String sampleGson = "[\n" + 
                        "{\"id\":1,\"title\":\"Bear Recipes 101\"},\n" + 
                        "{\"id\":2,\"title\":\"Spicy Bear Recipes\"},\n" +
                        "{\"id\":3,\"title\":\"Soups Involving Bears\"},\n" +
                        "{\"id\":4,\"title\":\"Anatomy of Bears with regard to the culinary arts\"}\n" +
                        "]";
         List<Course> results = CourseQuery.parseCourseList(sampleGson);
         assertEquals(4, results.size());
         assertCorrectResults(1, "Bear Recipes 101", results.get(0));
         assertCorrectResults(2, "Spicy Bear Recipes", results.get(1));
         assertCorrectResults(3, "Soups Involving Bears", results.get(2));
         assertCorrectResults(4, "Anatomy of Bears with regard to the culinary arts", results.get(3));
     }
     
     /**
      * Assert the id and title match the course
      */
     public void assertCorrectResults(int id, String title, Course c) {
         assertEquals(id, c.getId());
        assertEquals(title, c.getTitle());
     }
 
 }
