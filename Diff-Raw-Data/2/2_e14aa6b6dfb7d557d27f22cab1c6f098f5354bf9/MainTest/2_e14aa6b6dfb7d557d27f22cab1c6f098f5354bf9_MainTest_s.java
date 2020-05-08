 package main;
 
 import org.testng.annotations.Test;
 
 @Test
 public class MainTest extends BaseTestCase {
     public void test_success() {
        assertEquals(1, 2);
     }
 
     public void test_failure() {
         fail();
     }
 }
