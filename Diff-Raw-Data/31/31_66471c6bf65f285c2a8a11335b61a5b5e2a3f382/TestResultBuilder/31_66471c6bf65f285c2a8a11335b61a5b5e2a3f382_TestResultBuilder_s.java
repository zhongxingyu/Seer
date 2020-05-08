 /**
  *     Copyright (C) 2010 Julien SMADJA <julien dot smadja at gmail dot com> - Arnaud LEMAIRE <alemaire at norad dot fr>
  *
  *     Licensed under the Apache License, Version 2.0 (the "License");
  *     you may not use this file except in compliance with the License.
  *     You may obtain a copy of the License at
  *
  *             http://www.apache.org/licenses/LICENSE-2.0
  *
  *     Unless required by applicable law or agreed to in writing, software
  *     distributed under the License is distributed on an "AS IS" BASIS,
  *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *     See the License for the specific language governing permissions and
  *     limitations under the License.
  */
 
 package com.visuwall.client.hudson;
 
 import com.visuwall.client.hudson.domain.HudsonTest;
 import com.visuwall.client.hudson.domain.HudsonTestResult;
 import com.visuwall.client.hudson.resource.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * TestResultBuilder is used to manipulate Hudson Test data
  */
 public class TestResultBuilder {
 
     public HudsonTestResult buildUnitTestResult(SurefireAggregatedReport surefireReport) {
         HudsonTestResult unitTestResult = new HudsonTestResult();
         List<ChildReport> tests = surefireReport.getChildReports();
         countUnitTests(unitTestResult, tests);
         return unitTestResult;
     }
 
     private void countUnitTests(HudsonTestResult unitTestsResult, List<ChildReport> testReport) {
         List<HudsonTest> tests = createTestsFrom(testReport);
         for (HudsonTest test : tests) {
             updateTestResult(unitTestsResult, test);
         }
     }
 
     private void updateTestResult(HudsonTestResult unitTestsResult, HudsonTest test) {
         String status = test.getStatus();
         if ("FAILED".equals(status)) {
             unitTestsResult.setFailCount(unitTestsResult.getFailCount() + 1);
         }
         if ("SKIPPED".equals(status)) {
             unitTestsResult.setSkipCount(unitTestsResult.getSkipCount() + 1);
         }
        if ("PASSED".equals(status)) {
             unitTestsResult.setPassCount(unitTestsResult.getPassCount() + 1);
         }
     }
 
     private List<HudsonTest> createTestsFrom(List<ChildReport> testReport) {
         List<HudsonTest> tests = new ArrayList<HudsonTest>();
         for (ChildReport childReport : testReport) {
             Result childReportResult = childReport.getResult();
             List<Suite> suites = childReportResult.getSuites();
             for (Suite suite : suites) {
                 for (Case case_ : suite.getCases()) {
                     HudsonTest test = createTestFrom(case_);
                     tests.add(test);
                 }
             }
         }
         return tests;
     }
 
     private HudsonTest createTestFrom(Case case_) {
         HudsonTest test = new HudsonTest();
         test.setClassName(case_.getClassName());
         test.setStatus(case_.getStatus());
         return test;
     }
 
 }
