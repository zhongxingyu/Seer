 package com.chariot.lunchlearn.testingtalk.service;
 
 import com.chariot.lunchlearn.testingtalk.model.Course;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 
 import java.math.BigDecimal;
 
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

 public class TrainingServiceUnitTest {
 
   private TrainingServiceImpl trainingService;
 
   @Before
   public void setUp() {
     trainingService = new TrainingServiceImpl();
   }
 
   public void testAddNewCourseHappyPath() {
     Course course = new Course();
     course.setDescription("It's a biggie.");
     course.setName("Basket Weaving");
     course.setListPrice(new BigDecimal("100.00"));
     // must mock course service AND audit service
 
 
     trainingService.setAuditService(
         Mockito.mock(AuditService.class)
     );
     trainingService.setCourseService(
         Mockito.mock(CourseService.class)
     );
     
     trainingService.addNewCourse(course);
  }
 
  @Test
  public void testSomething() {
    // actual, comparison value
    int actual = 1;
    assertEquals(1, actual);
  }

  @Test
  public void matchSomething() {
    // actual, comparison matcher
    // notice, we construct a sentence this way...
    int actual = 1;
    assertThat(actual, equalTo(1) );
   }
 
   @After
   public void tearDown() {
     System.out.println("Cleaning up...");
     // redundant... we do setUp each time
     trainingService = null;
   }
 
 }
