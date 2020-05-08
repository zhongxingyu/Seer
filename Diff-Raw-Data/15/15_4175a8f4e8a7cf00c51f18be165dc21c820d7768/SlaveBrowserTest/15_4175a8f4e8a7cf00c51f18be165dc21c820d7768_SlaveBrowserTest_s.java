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
 
import org.joda.time.Instant;

 import junit.framework.TestCase;
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 public class SlaveBrowserTest extends TestCase {
 
   public void testSlaveBrowserHeartBeat() throws Exception {
     MockTime mockTime = new MockTime(0);
     SlaveBrowser browser = new SlaveBrowser(mockTime, "1", new BrowserInfo(), SlaveBrowser.TIMEOUT);
 
     assertEquals(new Instant(0), browser.getLastHeartbeat());
     assertEquals(-1.0, browser.getSecondsSinceLastHeartbeat());
 
     mockTime.add(5);
     browser.heartBeat();
     assertEquals(5L, browser.getLastHeartbeat().getMillis());
     assertEquals(0.0, browser.getSecondsSinceLastHeartbeat());
  
     mockTime.add(5000);
     assertEquals(5.0, browser.getSecondsSinceLastHeartbeat());
   }
 }
