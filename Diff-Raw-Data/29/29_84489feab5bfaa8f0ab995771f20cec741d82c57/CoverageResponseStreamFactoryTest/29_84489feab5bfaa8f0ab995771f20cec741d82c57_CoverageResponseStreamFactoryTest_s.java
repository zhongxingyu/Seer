 /*
  * Copyright 2009 Google Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver.coverage;
 
 import junit.framework.TestCase;
 
 import com.google.jstestdriver.DefaultResponseStreamFactory;
 import com.google.jstestdriver.ResponsePrinterFactory;
 import com.google.jstestdriver.ResponseStream;
 import com.google.jstestdriver.TestResult;
 import com.google.jstestdriver.TestResultPrinter;
 
 /**
  * @author corysmith
  * 
  */
 public class CoverageResponseStreamFactoryTest extends TestCase {
 
   public final class TestResultPrinterStub implements TestResultPrinter {
     public TestResult testResult;
 
     public void close() {
     }
 
     public void open(String name) {
     }
 
     public void print(TestResult testResult) {
       this.testResult = testResult;
     }
   }
 
   final TestResultPrinter printer = new TestResultPrinterStub();
 
   public void testGetRunTestsActionResponseStream() throws Exception {
     CoverageResponseStreamFactory factory = new CoverageResponseStreamFactory(
       new DefaultResponseStreamFactory(new ResponsePrinterFactory() {
         public TestResultPrinter getResponsePrinter(String xmlFile) {
           return printer;
         }
      }), null);
 
     ResponseStream responseStream = factory.getRunTestsActionResponseStream("browserId");
     assertNotNull(responseStream);
   }
 }
