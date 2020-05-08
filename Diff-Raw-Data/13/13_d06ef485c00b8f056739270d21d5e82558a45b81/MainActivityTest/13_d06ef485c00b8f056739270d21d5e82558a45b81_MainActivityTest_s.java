 package ${package};
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 @RunWith(org.robolectric.RobolectricTestRunner.class)
 public class MainActivityTest {
     
     @Test
    public void testA() {
         MainActivity mainActivity = new MainActivity();
         mainActivity.onCreate(null);
        // Assert.assertEquals(1, 1);
     }
 }
