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
 package com.google.jstestdriver;
 
 import junit.framework.TestCase;
 
 import org.joda.time.Instant;
 
 import com.google.common.collect.Lists;
 import com.google.jstestdriver.SlaveBrowser.BrowserState;
 import com.google.jstestdriver.model.NullPathPrefix;
 import com.google.jstestdriver.runner.RunnerType;
 import com.google.jstestdriver.server.handlers.CaptureHandler;
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 public class SlaveBrowserTest extends TestCase {
   
   /* (non-Javadoc)
    * @see junit.framework.TestCase#setUp()
    */
   @Override
   protected void setUp() throws Exception {
     System.out.println(this);
   }
 
   public void testSlaveBrowserHeartBeat() throws Exception {
     MockTime mockTime = new MockTime(0);
     SlaveBrowser browser = new SlaveBrowser(mockTime,
         "1",
         new BrowserInfo(),
         SlaveBrowser.TIMEOUT,
         null,
         CaptureHandler.QUIRKS,
         RunnerType.CLIENT, BrowserState.CAPTURED, new Instant(0));
 
    assertEquals(new Instant(0), browser.getLastHeartbeat());
    assertEquals(-1.0, browser.getSecondsSinceLastHeartbeat());

     mockTime.add(5);
     browser.heartBeat();
     assertEquals(5L, browser.getLastHeartbeat().getMillis());
     assertEquals(0.0, browser.getSecondsSinceLastHeartbeat());
 
     mockTime.add(5000);
     assertEquals(5.0, browser.getSecondsSinceLastHeartbeat());
   }
 
   public void testRedirectQuirksUrl() throws Exception {
     SlaveBrowser browser = new SlaveBrowser(null,
         "1",
         new BrowserInfo(),
         SlaveBrowser.TIMEOUT,
         new NullPathPrefix(),
         CaptureHandler.QUIRKS,
         RunnerType.CLIENT, BrowserState.CAPTURED, new Instant(0));
 
     assertEquals(
         "/slave/id/1/page/CONSOLE/mode/quirks/timeout/" + SlaveBrowser.TIMEOUT + "/upload_size/"
             + FileUploader.CHUNK_SIZE + "/rt/CLIENT", browser.getCaptureUrl());
   }
 
   public void testInUseExpired() throws Exception {
     MockTime mockTime = new MockTime(System.currentTimeMillis());
     SlaveBrowser browser = new SlaveBrowser(mockTime,
         "1",
         new BrowserInfo(),
         SlaveBrowser.TIMEOUT,
         null,
         CaptureHandler.QUIRKS,
         RunnerType.CLIENT, BrowserState.CAPTURED, new Instant(0));
     String sessionId = "foo";
     browser.tryLock(sessionId);
     browser.createCommand("json command");
     assertTrue(browser.inUse());
     mockTime.add(SlaveBrowser.SESSION_TIMEOUT + 1);
     browser.resetCommandQueue();
     assertFalse(browser.inUse());
   }
   
   public void testInUseFalse() throws Exception {
     MockTime mockTime = new MockTime(System.currentTimeMillis());
     SlaveBrowser browser = new SlaveBrowser(mockTime,
         "1",
         new BrowserInfo(),
         SlaveBrowser.TIMEOUT,
         null,
         CaptureHandler.QUIRKS,
         RunnerType.CLIENT, BrowserState.CAPTURED, new Instant(0));
     assertFalse(browser.inUse());
   }
 
   public void testInUseSessionTimeout() throws Exception {
     MockTime mockTime = new MockTime(System.currentTimeMillis());
     SlaveBrowser browser = new SlaveBrowser(mockTime,
         "1",
         new BrowserInfo(),
         SlaveBrowser.TIMEOUT,
         null,
         CaptureHandler.QUIRKS,
         RunnerType.CLIENT, BrowserState.CAPTURED, new Instant(0));
     String sessionId = "foo";
     assertTrue(browser.tryLock(sessionId));
     browser.createCommand("json command");
     assertTrue(browser.inUse());
     mockTime.add(SlaveBrowser.SESSION_TIMEOUT + 1);
     assertFalse(browser.inUse());
   }
   
   public void testInUseAfterSessionStart() throws Exception {
     MockTime mockTime = new MockTime(System.currentTimeMillis());
     SlaveBrowser browser = new SlaveBrowser(mockTime,
         "1",
         new BrowserInfo(),
         SlaveBrowser.TIMEOUT,
         null,
         CaptureHandler.QUIRKS,
         RunnerType.CLIENT, BrowserState.CAPTURED, new Instant(0));
     String sessionId = "foo";
     assertTrue(browser.tryLock(sessionId));
     assertTrue(browser.inUse());
   }
 
   public void testAddFileResults() throws Exception {
     MockTime mockTime = new MockTime(System.currentTimeMillis());
     SlaveBrowser browser = new SlaveBrowser(mockTime,
         "1",
         new BrowserInfo(),
         SlaveBrowser.TIMEOUT,
         null,
         CaptureHandler.QUIRKS,
         RunnerType.CLIENT, BrowserState.CAPTURED, new Instant(0));
     FileSource fileSource = new FileSource("/foo.js", "/foo/bar.js", 10, 100);
     browser.addFileResults(Lists.newArrayList(new FileResult(fileSource, true, "")));
     assertTrue(browser.getFileSet().contains(fileSource.toFileInfo(null)));
     assertFalse(browser.hasFileLoadErrors());
   }
 
   public void testAddFileResultsWithError() throws Exception {
     MockTime mockTime = new MockTime(System.currentTimeMillis());
     SlaveBrowser browser = new SlaveBrowser(mockTime,
         "1",
         new BrowserInfo(),
         SlaveBrowser.TIMEOUT,
         null,
         CaptureHandler.QUIRKS,
         RunnerType.CLIENT, BrowserState.CAPTURED, new Instant(0));
     FileSource fileSource = new FileSource("/foo.js", "/foo/bar.js", 10, 100);
     browser.addFileResults(Lists.newArrayList(new FileResult(fileSource, false, "")));
     assertTrue(browser.getFileSet().contains(fileSource.toFileInfo(null)));
     assertTrue(browser.hasFileLoadErrors());
   }
 }
