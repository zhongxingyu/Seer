 package fi.helsinki.cs.oato;
 
 import java.util.*;
 import org.junit.Before;
 import org.junit.Test;
 
 import static fi.helsinki.cs.oato.OnlineCourseSourceFixtures.*;
 import static org.junit.Assert.*;
 import static org.junit.Assume.*;
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.matchers.JUnitMatchers.*;
 
 public class OnlineCourseSourceTest {
     private OnlineCourseSource ocs;
 
     @Before
     public void setUp() {
         ocs = new OnlineCourseSource();
     }
 
     @Test(timeout=15*1000)
     public void testOnlineFetchingSuccess() throws InterruptedException {
        while (true) {
            if (ocs.isReady()) {
                break;
            }
            try {
                this.wait(1000);
            } catch (InterruptedException e) {
                throw e;
            }
         }
 
         assertTrue(ocs.isReady());
     }
 
     public void testFetchedDataAgainstFixture() {
         assumeTrue(ocs.isReady());
 
         List<Course> expectedCourses = expectedCourses();
 
         List<Course> fetchedCourses = ocs.getCourses();
         assertNotNull(fetchedCourses);
 
         assertEquals(65, fetchedCourses.size());
         assertEquals(expectedCourses.size(), fetchedCourses.size());
 
         assertArrayEquals(expectedCourses.toArray(new Course[0]), fetchedCourses.toArray(new Course[0]));
     }
 
     public void testFetchedDataAgainstKnownCourses() {
         assumeTrue(ocs.isReady());
         
         List<Course> expectedCourses = expectedCourses();
         
         assertThat(expectedCourses, hasItem(fiveKnownCourses().get(0)));
         assertThat(expectedCourses, hasItem(fiveKnownCourses().get(1)));
         assertThat(expectedCourses, hasItem(fiveKnownCourses().get(2)));
         assertThat(expectedCourses, hasItem(fiveKnownCourses().get(3)));
         assertThat(expectedCourses, hasItem(fiveKnownCourses().get(4)));
     }
 
 }
