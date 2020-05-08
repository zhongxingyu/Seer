 /*
  * Copyright 2011 Google Inc.
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
 package com.google.jstestdriver.plugins.testisolation;
 
 import com.google.common.collect.Lists;
 import com.google.jstestdriver.FileInfo;
 import com.google.jstestdriver.model.JstdTestCase;
 
 import junit.framework.TestCase;
 
 import java.util.List;
 
 /**
  * @author Andrew Trenk
  *
  */
 public class IsolationTestCaseProcessorTest extends TestCase {
 
   IsolationTestCaseProcessor processor = new IsolationTestCaseProcessor();
   
   static final FileInfo TEST_1 = getFileInfo("test1");
   static final FileInfo TEST_2 = getFileInfo("test2");
   static final FileInfo TEST_3 = getFileInfo("test3");
   
   static final List<FileInfo> DEPS_1 = Lists.newArrayList(getFileInfo("dep1"));    
   static final List<FileInfo> DEPS_2 = Lists.newArrayList(getFileInfo("dep2"));
    
   static final List<FileInfo> PLUGINS_1 = Lists.newArrayList(getFileInfo("plugin1"));    
   static final List<FileInfo> PLUGINS_2 = Lists.newArrayList(getFileInfo("plugin2"));
 
   public void testProcess() {
     JstdTestCase testCaseWithTwoTests =
        new JstdTestCase(DEPS_1, Lists.newArrayList(TEST_1, TEST_2), PLUGINS_1, null);
     JstdTestCase testCaseWithOneTest =
        new JstdTestCase(DEPS_2, Lists.newArrayList(TEST_3), PLUGINS_2, null);
     
     List<JstdTestCase> processedTests =
         processor.process(Lists.newArrayList(testCaseWithTwoTests, testCaseWithOneTest).iterator());
 
     // Each test file should have been moved into its own JstdTestCase    
     assertEquals(3, processedTests.size());
     assertEquals(
        new JstdTestCase(DEPS_1, Lists.newArrayList(TEST_1), PLUGINS_1, null), processedTests.get(0));
     assertEquals(
        new JstdTestCase(DEPS_1, Lists.newArrayList(TEST_2), PLUGINS_1, null), processedTests.get(1)); 
     assertEquals(
        new JstdTestCase(DEPS_2, Lists.newArrayList(TEST_3), PLUGINS_2, null), processedTests.get(2)); 
   }
   
   private static FileInfo getFileInfo(String name) {
     return new FileInfo(name, 0, 0, false, false, "", "");
   }
 }
