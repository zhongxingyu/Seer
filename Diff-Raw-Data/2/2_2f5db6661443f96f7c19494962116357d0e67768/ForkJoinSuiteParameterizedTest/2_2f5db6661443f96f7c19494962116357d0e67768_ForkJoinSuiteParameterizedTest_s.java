 package com.github.marschall.forkjoinjunit;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized.Parameters;
 
 import static org.junit.Assert.assertNotNull;
 
 //@RunWith(Parameterized.class)
 @RunWith(ForkJoinSuite.class)
 @ForkJoinParameters(runnerBuilder = ParameterizedBuild.class, parallelism = 2)
 public class ForkJoinSuiteParameterizedTest {
   
   private final Integer parameter;
 
   public ForkJoinSuiteParameterizedTest(Integer parameter) {
     this.parameter = parameter;
   }
   
   @Test
   public void testNotNull() throws InterruptedException {
    Thread.currentThread().sleep(100);
     assertNotNull(this.parameter);
   }
   
   @Parameters
   public static Collection<Object[]> parameters() {
     int parameterCount = 100;
     List<Object[]> parameters = new ArrayList<>(parameterCount);
     for (int i = 0; i < parameterCount; i++) {
       parameters.add(new Object[]{i});
     }
     return parameters;
   }
 
 }
