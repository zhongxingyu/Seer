 /** 
  *  Copyright 2007 Applied Research in Patacriticism and the University of Virginia
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  **/
 package org.nines;
 
 import java.io.File;
 
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 public class ErrorReportTest extends TestCase {
   ErrorReport errorReport;
 
   protected void setUp() throws Exception {
     super.setUp();
     errorReport = new ErrorReport(new File("test_data","test_report.txt"));
   }
 
   public void testSummary() {
    errorReport.addError(new Error("file1.rdf", "abc1", "my error message"));
    errorReport.addError(new Error("file1.rdf", "abc2", "my error message"));
    errorReport.addError(new Error("file1.rdf", "abc3", "my error message"));
    errorReport.addError(new Error("file2.rdf", "abc4", "my error message"));
    errorReport.addError(new Error("file3.rdf", "abc5", "my error message"));
    errorReport.addError(new Error("file3.rdf", "abc6", "my error message"));

    Summary summary = errorReport.getSummary();
 
     assertTrue(summary.getFileCount() == 3);
   }
 
   public static void main(String[] args) {
     junit.textui.TestRunner.run(new TestSuite(ErrorReportTest.class));
   }
 }
