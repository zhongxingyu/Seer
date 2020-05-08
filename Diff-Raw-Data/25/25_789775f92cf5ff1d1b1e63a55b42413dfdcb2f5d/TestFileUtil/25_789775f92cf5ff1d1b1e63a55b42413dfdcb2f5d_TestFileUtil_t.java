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
 package com.google.jstestdriver.server.handlers.pages;
 
 import com.google.gson.Gson;
 import com.google.inject.Inject;
 import com.google.jstestdriver.FileInfo;
 import com.google.jstestdriver.FileSource;
 import com.google.jstestdriver.hooks.FileInfoScheme;
 import com.google.jstestdriver.model.HandlerPathPrefix;
 import com.google.jstestdriver.model.JstdTestCase;
 import com.google.jstestdriver.server.JstdTestCaseStore;
 import com.google.jstestdriver.util.HtmlWriter;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Set;
 
 /**
  * A utility that write The contents of the FilesCache into an HtmlWriter.
  *
  * @author Cory Smith (corbinrsmith@gmail.com) 
  */
 // TODO(corysmith): Probably, horribly, misnamed. Fix.
 public class TestFileUtil {
   private static final Logger logger = LoggerFactory.getLogger(TestFileUtil.class);
   private final HandlerPathPrefix prefix;
   private final Set<FileInfoScheme> schemes;
   private final Gson gson;
   private final JstdTestCaseStore store;
 
   /**
    * Creates a new TestFileUtil from the dependencies.
    */
   @Inject
    TestFileUtil(JstdTestCaseStore store, HandlerPathPrefix prefix, Set<FileInfoScheme> schemes,
       Gson gson) {
     this.store = store;
     this.prefix = prefix;
     this.schemes = schemes;
     this.gson = gson;
   }
 
   /**
    * Writes as many of the test files into the HtmlWriter, until it reaches a
    * FileInfo that cannot be handled using a &lt;link&gt; or a &lt;script&gt;.
    * @param writer The output writer.
    */
   public void writeTestFiles(HtmlWriter writer, String testCaseId) {
     JstdTestCase testCase;
 
     if (store.getCase(testCaseId) == null) { // no optimization without testcase id
       if (store.getCases().size() == 1) {
         testCase = store.getCases().iterator().next();
        logger.debug("One test case found, writing: {}", testCase);
       } else {
        logger.debug("More than one testcase found:\n{}", store.getCases());
         // no point in writing out random test cases, just quit.
         return;
       }
     } else {
       testCase = store.getCase(testCaseId);
      logger.debug("Testcase found at id {}: {}", testCaseId, testCase);
     }
 
     logger.info("preloading {}", testCase.getId());
     for (FileInfo file : testCase) {
       if (file.isServeOnly()) {
         continue;
       }
       // TODO(corysmith): This is a problematic optimization.
       // If a client connects with unknown schemes and causes a reset, this will mess up miserably.
       // Must fix.
       FileSource fileSource = file.toFileSource(prefix, schemes);
       if (!(fileSource.getFileSrc().startsWith("http") || fileSource.getFileSrc().startsWith("/test"))) {
        logger.debug("Unknown FileSource [{}], bail out on writing", fileSource.getFileSrc());
         // better safe than sorry.
         break;
       }
       logger.debug("Writing " + fileSource.getFileSrc());
       writer.writeScript(String.format(
           "jstestdriver.manualResourceTracker.startResourceLoad('%s')",
           gson.toJson(fileSource)));
       if (fileSource.getFileSrc().endsWith(".css")) {
         writer.writeStyleSheet(fileSource.getFileSrc());
       } else {
         writer.writeExternalScript(fileSource.getFileSrc());
       }
       writer.writeScript("jstestdriver.manualResourceTracker.finishResourceLoad()");
     }
   }
 }
