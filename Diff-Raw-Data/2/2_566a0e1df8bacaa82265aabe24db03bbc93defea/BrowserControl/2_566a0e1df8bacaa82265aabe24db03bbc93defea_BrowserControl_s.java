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
 package com.google.jstestdriver.browser;
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 import com.google.inject.assistedinject.Assisted;
 import com.google.inject.name.Named;
 import com.google.jstestdriver.BrowserInfo;
 import com.google.jstestdriver.JsTestDriverClient;
 import com.google.jstestdriver.model.JstdTestCase;
 import com.google.jstestdriver.server.handlers.pages.SlavePageRequest;
 import com.google.jstestdriver.util.StopWatch;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Class that handles the 
  *
  * @author Cory Smith (corbinrsmith@gmail.com) 
  */
 public class BrowserControl {
   
   public static interface BrowserControlFactory {
     public BrowserControl create(BrowserRunner runner,
                                  String serverAddress,
                                  List<JstdTestCase> testCases);
   }
 
   private final Function<JstdTestCase, String> TESTCASE_TO_ID = new Function<JstdTestCase, String>() {
     @Override
     public String apply(JstdTestCase testCase) {
       return testCase.getId();
     }
   };
 
   private static final String CAPTURE_URL =
     String.format("%%s/capture/%s/%%s/%s/%%s/%s/%%s/",
         SlavePageRequest.ID, SlavePageRequest.TIMEOUT, SlavePageRequest.UPLOAD_SIZE);
   private static final String CAPTURE_URL_WITH_TESTCASE_IDS =
     String.format("%%s/capture/%s/%%s/%s/%%s/%s/%%s/%s/%%s",
         SlavePageRequest.ID,
         SlavePageRequest.TIMEOUT,
         SlavePageRequest.UPLOAD_SIZE,
         SlavePageRequest.TESTCASE_ID);
 
   private static final Logger logger = LoggerFactory.getLogger(BrowserControl.class);
   private final BrowserRunner runner;
   private final String serverAddress;
   private final StopWatch stopWatch;
   private final JsTestDriverClient client;
 
   private final List<JstdTestCase> testCases;
 
   private final long browserTimeout;
 
   /**
    * @param runner
    * @param serverAddress 
    * @param stopWatch 
    * @param client 
    * @param testCases 
    * @param browserTimeout TODO
    */
   @Inject
   public BrowserControl(
       @Assisted BrowserRunner runner,
       @Assisted String serverAddress,
       StopWatch stopWatch,
       JsTestDriverClient client,
       @Assisted List<JstdTestCase> testCases,
       @Named("browserTimeout") long browserTimeout) {
     this.runner = runner;
     this.serverAddress = serverAddress;
     this.stopWatch = stopWatch;
     this.client = client;
     this.testCases = testCases;
     this.browserTimeout = browserTimeout;
   }
 
   /** Slaves a new browser window with the provided id. */
   public String captureBrowser(String browserId) throws InterruptedException {
     try {
       stopWatch.start("browser start %s", browserId);
       final String url;
       if (testCases.isEmpty()) {
         url = String.format(CAPTURE_URL,
             serverAddress,
             browserId,
             Math.max(runner.getHeartbeatTimeout(), browserTimeout),
             runner.getUploadSize());
       } else {
         url = String.format(CAPTURE_URL_WITH_TESTCASE_IDS,
             serverAddress,
             browserId,
             Math.max(runner.getHeartbeatTimeout(), browserTimeout),
             runner.getUploadSize(),
            Joiner.on(",").join(Lists.transform(testCases, TESTCASE_TO_ID)));
       }
       runner.startBrowser(url);
       long timeOut =
           TimeUnit.MILLISECONDS.convert(Math.max(runner.getTimeout(), browserTimeout),
               TimeUnit.SECONDS);
       long start = System.currentTimeMillis();
       // TODO(corysmith): replace this with a stream from the client on browser
       // updates.
       try {
         stopWatch.start("Capturing browser", browserId);
         while (!isBrowserCaptured(browserId, client)) {
           Thread.sleep(50);
           if (System.currentTimeMillis() - start > timeOut) {
             throw new RuntimeException("Could not start browser " + runner + " in "
                 + runner.getTimeout());
           }
         }
       } finally {
         stopWatch.stop("Capturing browser", browserId);
       }
       logger.debug("Browser {} started with id {}", runner, browserId);
       return browserId;
     } finally {
       stopWatch.stop("browser start %s", browserId);
     }
   }
 
   /** Stop a browser window. */
   public void stopBrowser() {
     stopWatch.start("browser stop %s", runner);
     runner.stopBrowser();
     stopWatch.stop("browser stop %s", runner);
   }
 
   public boolean isBrowserCaptured(String browserId, JsTestDriverClient client) {
     for (BrowserInfo browserInfo : client.listBrowsers()) {
       if (browserId.equals(String.valueOf(browserInfo.getId())) 
           && browserInfo.serverReceivedHeartbeat()
           && browserInfo.browserReady()) {
         logger.debug("Started {}", browserInfo);
         return true;
       }
     }
     return false;
   }
 }
