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
 
 import com.google.jstestdriver.browser.BrowserHunter;
 import com.google.jstestdriver.browser.BrowserIdStrategy;
 import com.google.jstestdriver.model.NullPathPrefix;
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 public class BrowserHunterTest extends TestCase {
 
   public void testCaptureAndGenerateUrlQuirks() throws Exception {
     Time time = new MockTime(444);
    BrowserHunter browserHunter = new BrowserHunter(new CapturedBrowsers(new BrowserIdStrategy(time)),
        SlaveBrowser.TIMEOUT, new NullPathPrefix(), new TimeImpl());
    SlaveBrowser slaveBrowser = browserHunter.captureBrowser("name", "version",
        "os");
     BrowserInfo browserInfo = slaveBrowser.getBrowserInfo();
 
     assertEquals("name", browserInfo.getName());
     assertEquals("version", browserInfo.getVersion());
     assertEquals("os", browserInfo.getOs());
     assertEquals(
        "/slave/id/444/page/CONSOLE/mode/quirks/timeout/15000/upload_size/" + FileUploader.CHUNK_SIZE
            + "/rt/CLIENT", slaveBrowser.getCaptureUrl());
   }
 }
