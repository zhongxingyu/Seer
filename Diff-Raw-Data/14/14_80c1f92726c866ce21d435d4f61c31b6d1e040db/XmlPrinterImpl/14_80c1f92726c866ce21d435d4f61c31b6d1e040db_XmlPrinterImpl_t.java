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
 package com.google.jstestdriver.output;
 
 import com.google.common.base.Supplier;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Multimaps;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import com.google.jstestdriver.BrowserInfo;
 import com.google.jstestdriver.TestResult;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 public class XmlPrinterImpl implements XmlPrinter {
 
   private final Logger logger = LoggerFactory.getLogger(XmlPrinterImpl.class);
   private final TestResultHolder resultHolder;
   private final String xmlOutputDir;
   private final String fileNameFormat = "TEST-%s.xml";
 
   @Inject
   public XmlPrinterImpl(TestResultHolder resultHolder, @Named("testOutput") String xmlOutputDir) {
     this.resultHolder = resultHolder;
     this.xmlOutputDir = xmlOutputDir;
   }
 
   public void writeXmlReportFiles() {
     Map<BrowserInfo, String> browserNames =
         createUniqueBrowserNames(resultHolder.getResults().keySet());
     for (BrowserInfo browser : resultHolder.getResults().keySet()) {
       Multimap<String, TestResult> testCaseRollup = newMultiMap();
       for (TestResult testResult : resultHolder.getResults().get(browser)) {
         testCaseRollup.put(testResult.getTestCaseName(), testResult);
       }
       for (String testCaseName : testCaseRollup.keySet()) {
         String suiteName = formatSuiteName(browserNames.get(browser), testCaseName);
         File xmlOutputFile = new File(xmlOutputDir, formatFileName(suiteName));
         try {
           xmlOutputFile.createNewFile();
           TestXmlSerializer serializer = new TestXmlSerializer(new FileWriter(xmlOutputFile));
 
           serializer.writeTestCase(suiteName, testCaseRollup.get(testCaseName));
         } catch (IOException e) {
           logger.error("Could not create file: {}", xmlOutputFile.getAbsolutePath(), e);
         }
       }
     }
   }
 
  public static Map<BrowserInfo, String> createUniqueBrowserNames(Set<BrowserInfo> browserInfos) {
     Map<BrowserInfo, String> result = Maps.newHashMap();
     for (BrowserInfo browserInfo : browserInfos) {
       if (result.containsValue(browserInfo.toString())) {
         result.put(browserInfo, browserInfo.toUniqueString());
       } else {
         result.put(browserInfo, browserInfo.toString());
       }
     }
     return result;
   }
 
   private String formatFileName(String suiteName) {
     return String.format(fileNameFormat, suiteName);
   }
 
   private String formatSuiteName(String browser, String testCaseName) {
     return String.format("%s.%s",
         browser.replaceAll("\\s", "_").replaceAll("\\.", ""), testCaseName);
   }
 
   private Multimap<String, TestResult> newMultiMap() {
     return Multimaps.newMultimap(
             Maps.<String, Collection<TestResult>>newHashMap(),
         new Supplier<Collection<TestResult>>() {
       public Collection<TestResult> get() {
         return Lists.newLinkedList();
       }
     });
   }
 }
