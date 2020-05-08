 package org.fenwaygrp.fenmarking.fenmarkingdefaultimpl;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.*;
 import static org.junit.Assert.assertThat;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.fenwaygrp.fenmarking.Algorithm;
 import org.fenwaygrp.fenmarking.Configuration;
 import org.fenwaygrp.fenmarking.Fenmarking;
 import org.fenwaygrp.fenmarking.FenmarkingDefaultImpl;
 import org.fenwaygrp.fenmarking.MetricResult;
 import org.fenwaygrp.fenmarking.PerformanceConfiguration;
 import org.fenwaygrp.fenmarking.ScalabilityConfiguration;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class WhenSubmittingWithAlgorithmInstAndCustomScalConfig {
 
     private static Fenmarking fenmarking = new FenmarkingDefaultImpl();  
     private static List<MetricResult> results; 
     private static AlgorithmOne algorithmOne = new AlgorithmOne();
     
     @BeforeClass
     public static void beforeClass() throws Exception {
         AlgorithmOne.reset();
         AlgorithmTwo.reset();
        results = fenmarking.submit(new ScalabilityConfiguration(10, 5, 100, 100), algorithmOne);
     }
     
     @Test
     public void shouldHaveRunDefaultNumberWarmUps() throws Exception {
         assertThat(algorithmOne.warmups, is(100*3));
     }
     
     @Test
     public void shouldHaveRunDefaultNumberExecutions() throws Exception {
         assertThat(algorithmOne.executions, is(100*3));
     }
 
     @Test
     public void shouldHaveConfiguredNumberOfThreads() throws Exception {
         assertThat(algorithmOne.threadCount.size(), is(16));
     }
     
     @Test
     public void shouldHaveCalledSetUp() throws Exception {
         assertThat(algorithmOne.isSetUpCalled, is(true));
     }
     
     @Test
     public void shouldHaveCalledTearDown() throws Exception {
         assertThat(algorithmOne.isTearDownCalled, is(true));
     }
     
     @Test
     public void shouldHaveThreeResults() throws Exception {
         assertThat(results.size(), is(3));
     }
 
     @Test
     public void shouldHaveValidMetricResultsForFirstResult() throws Exception {
         new MetricResultAssertions(results.get(0));
     }
     
     @Test
     public void shouldHaveValidMetricResultsForSecondResult() throws Exception {
         new MetricResultAssertions(results.get(1));
     }
     
     @Test
     public void shouldHaveValidMetricResultsForThirdResult() throws Exception {
         new MetricResultAssertions(results.get(2));
     }
     
     @Test(expected=AssertionError.class)
     public void shouldThrowExceptionOnNullAlgorithm() throws Exception {
         Algorithm a = null;
         try {
             fenmarking.submit(a);
         } catch (AssertionError e) {
             assertThat(e.getMessage(),is("\nExpected: not null\n     got: null\n"));
             throw e;
         }
     }
     
     @Test
     public void shouldHaveRunSetUpWarmUpPlusExecutionRuns() throws Exception {
         assertThat(AlgorithmOne.setupCount, is(200*3));
     }
     
     @Test
     public void shouldHaveRunTearDownWarmUpPlusExectionRuns() throws Exception {
         assertThat(AlgorithmOne.tearDownCount, is(200*3));
     }
 }
 
