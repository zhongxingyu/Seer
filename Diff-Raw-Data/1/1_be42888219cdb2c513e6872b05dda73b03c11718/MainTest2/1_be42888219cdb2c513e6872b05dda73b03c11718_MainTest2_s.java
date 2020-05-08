 package main;
 
 import org.testng.annotations.Test;
 
 import java.util.Random;
 
 @Test
 public class MainTest2 extends BaseTestCase {
     public void test_success() {
         assertEquals(1, 1);
     }
 
     public void test_failure() {
         fail();
     }
 
     @Test(invocationCount = 10)
     public void test_random() {
         assertTrue(new Random().nextBoolean());
     }
 }
