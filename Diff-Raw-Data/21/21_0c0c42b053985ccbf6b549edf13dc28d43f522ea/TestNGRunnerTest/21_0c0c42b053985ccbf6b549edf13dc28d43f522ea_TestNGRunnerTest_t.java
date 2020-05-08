 package com.maxheapsize.quant.testng;
 
 import org.testng.annotations.Test;
 
 import java.io.IOException;
 
 @Test(groups = "unitTest")
 public class TestNGRunnerTest {
 
   @Test
   public void testTestNGRunner() throws IOException {
     TestNGRunner testNGRunner = new TestNGRunner();
    testNGRunner.run("target/test-classes");
   }
 }
