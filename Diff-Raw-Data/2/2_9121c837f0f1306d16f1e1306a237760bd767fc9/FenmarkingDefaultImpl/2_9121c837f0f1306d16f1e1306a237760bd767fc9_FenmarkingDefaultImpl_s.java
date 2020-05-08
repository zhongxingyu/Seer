 package org.fenwaygrp.fenmarking;
 
 import static org.hamcrest.Matchers.greaterThan;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.notNullValue;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.hamcrest.MatcherAssert;
 import static org.hamcrest.MatcherAssert.*;
 
 public class FenmarkingDefaultImpl implements Fenmarking {
 
     private FenmarkUtility fenmarkUtility = new FenmarkUtility();
 
     @SuppressWarnings("unchecked")
     public List<MetricResult> submit(PerformanceConfiguration configuration,
             List<Class<? extends Algorithm>> clazzez) {
         MatcherAssert.assertThat(clazzez.size(), is(greaterThan(0)));
         List<MetricResult> results = new ArrayList<MetricResult>();
         for (Class<? extends Algorithm> clazz : clazzez) {
             results.add(submitInternal(configuration, fenmarkUtility.newInstance(clazz),new HashMap()));
         }
         return results;
     }
 
     public MetricResult submit(Algorithm algorithm) {
         assertThat("Alogirhm is required", algorithm, notNullValue());
         return submit(new PerformanceConfiguration(), algorithm);
     }
 
     @SuppressWarnings("unchecked")
     public MetricResult submit(PerformanceConfiguration perfConfig, Algorithm algorithm) {
         assertThat("Alogirhm is required", algorithm, notNullValue());
         return submitInternal(perfConfig, algorithm, new HashMap());
     }
 
     @SuppressWarnings("unchecked")
     public List<MetricResult> submit(ScalabilityConfiguration configuration, Algorithm algorithm) {
         List<MetricResult> results = new ArrayList<MetricResult>();
         PerformanceConfiguration perfConfig = new PerformanceConfiguration(configuration);
         results.add(submitInternal(perfConfig, algorithm, new HashMap()));
         for (int i = configuration.getIncrementBy(); i <= configuration.getMaxThreads(); i = i
                 + configuration.getIncrementBy()) {
             perfConfig.setNumberOfThreads(i);
             results.add(submitInternal(perfConfig, algorithm, new HashMap()));
         }
         return results;
     }
 
     public ScalabilityProfile submit(ScalabilityTrial scalabilityTrial) {
 
         List<Integer> threadCounts = new ArrayList<Integer>();
         threadCounts.add(1);
         for (int i = scalabilityTrial.getConfiguration().getIncrementBy(); i <= scalabilityTrial
                 .getConfiguration().getMaxThreads(); i = i
                 + scalabilityTrial.getConfiguration().getIncrementBy()) {
             threadCounts.add(i);
         }
 
         PerformanceTrial perfTrial = new PerformanceTrial();
         perfTrial.setAlgorithms(scalabilityTrial.getAlgorithms());
         perfTrial.setTestCases(scalabilityTrial.getTestCases());
         perfTrial.getConfiguration().setNumberOfWarmUps(scalabilityTrial.getConfiguration().getNumberOfWarmUps());
         perfTrial.getConfiguration().setNumberOfExecutions(scalabilityTrial.getConfiguration().getNumberOfExecutions());
         
         ScalabilityProfile scalabilityProfile = new ScalabilityProfile();
         for (Integer threadCount : threadCounts) {
             perfTrial.getConfiguration().setNumberOfThreads(threadCount);
             PerformanceProfile perfProfile = submitInternal(perfTrial);
             scalabilityProfile.addPerformanceProfile(perfProfile);
         }
         return scalabilityProfile;
     }
 
     @SuppressWarnings("unchecked")
     private MetricResult submitInternal(PerformanceConfiguration configuration,
             final Algorithm algorithm, Map testData) {
         if (testData == null) {
             testData = new HashMap();
         }
         
     
         for (int i = 0; i < configuration.getNumberOfWarmUps(); i++) {
             algorithm.setUp(testData);
             algorithm.warmUp();
             algorithm.tearDown();
         }
     
         final List<Long> times = Collections.synchronizedList(new ArrayList<Long>());
         ExecutorService pool = Executors.newFixedThreadPool(configuration.getNumberOfThreads());
     
         for (int i = 0; i < configuration.getNumberOfExecutions(); i++) {
             pool.submit(new FenmarkCallable(times, algorithm, testData));
         }
     
         pool.shutdown();
         try {
             if(!pool.isTerminated()){
                pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);    
             }
         } catch (InterruptedException e) {
             throw new RuntimeException(e);
         }
     
         
     
         MetricResult result = new MetricResult(times);
         return result;
     }
 
     @SuppressWarnings("unchecked")
     private PerformanceProfile submitInternal(PerformanceTrial trial){
         PerformanceProfile perfProfile = new PerformanceProfile();
 
         for (Map.Entry<String, Class<? extends Algorithm>> algorithmEntry : trial.getAlgorithms()
                 .entrySet()) {
             AlgorithmProfile algorithmProfile = new AlgorithmProfile(algorithmEntry.getKey());
             perfProfile.addAlgorithmProfile(algorithmProfile);
             for (Map.Entry<String, Map> testDataEntry : trial.getTestCases().entrySet()) {
                 PerformanceConfiguration config = new PerformanceConfiguration(trial
                         .getConfiguration());
                 MetricResult metricResult = submitInternal(config, fenmarkUtility
                         .newInstance(algorithmEntry.getValue()), testDataEntry.getValue());
                 TestCaseProfile testCaseProfile = new TestCaseProfile(testDataEntry.getKey());
                 testCaseProfile.setMetricResult(metricResult);
                 algorithmProfile.addTestCaseProfile(testCaseProfile);
             }
         }
         return perfProfile;
     }
     
 }
