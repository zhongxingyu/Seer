 package com.google.jstestdriver.output;
 
 import com.google.common.base.Supplier;
 import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
 import com.google.common.collect.Multimap;
 import static com.google.common.collect.Multimaps.newMultimap;
 import static com.google.common.collect.Multimaps.synchronizedMultimap;
 import com.google.jstestdriver.BrowserInfo;
 import com.google.jstestdriver.TestResult;
 
 import java.util.Collection;
 import java.util.HashMap;
 
 /**
  * A data storage for test results. It listens on each browser for incoming test results,
  * on multiple threads. Then the XmlPrinter can use it to find the test results, so it can
  * produce our XML output files.
  * It should be bound as a Singleton to be sure the data is shared between these classes.
  * @author alexeagle@google.com (Alex Eagle)
  */
 public class TestResultHolder implements TestResultListener {
   private final Multimap<BrowserInfo, TestResult> results;
 
   public TestResultHolder() {
    HashMap<BrowserInfo, Collection<TestResult>> map = newHashMap();
     Supplier<Collection<TestResult>> collectionSupplier = new Supplier<Collection<TestResult>>() {
       public Collection<TestResult> get() {
         return newLinkedList();
       }
     };
     Multimap<BrowserInfo, TestResult> resultMultimap = newMultimap(map, collectionSupplier);
     results = synchronizedMultimap(resultMultimap);
   }
 
   /**
    * @return a map of browser name to test results from that browser
    */
   public Multimap<BrowserInfo, TestResult> getResults() {
     return results;
   }
 
   public void onTestComplete(TestResult testResult) {
     results.put(testResult.getBrowserInfo(), testResult);
   }
 
   public void finish() {
   }
 }
