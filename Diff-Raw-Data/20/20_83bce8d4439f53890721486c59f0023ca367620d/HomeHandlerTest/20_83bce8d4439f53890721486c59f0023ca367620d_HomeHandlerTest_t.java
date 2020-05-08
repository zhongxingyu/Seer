 /*
  * Copyright 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver.server.handlers;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintWriter;
 
 import javax.servlet.http.HttpServletResponse;
 
 import junit.framework.TestCase;
 
 import org.easymock.EasyMock;
 import org.joda.time.Instant;
 
 import com.google.jstestdriver.BrowserInfo;
 import com.google.jstestdriver.CapturedBrowsers;
 import com.google.jstestdriver.MockTime;
 import com.google.jstestdriver.SlaveBrowser;
 import com.google.jstestdriver.SlaveBrowser.BrowserState;
 import com.google.jstestdriver.browser.BrowserIdStrategy;
 import com.google.jstestdriver.runner.RunnerType;
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 public class HomeHandlerTest extends TestCase {
 
   public void testDisplayInfo() throws Exception {
     CapturedBrowsers capturedBrowsers =
         new CapturedBrowsers(new BrowserIdStrategy(new MockTime(0)));
     BrowserInfo browserInfo = new BrowserInfo();
 
     browserInfo.setId(1L);
     browserInfo.setName("browser");
     browserInfo.setOs("OS");
     browserInfo.setVersion("1.0");
     SlaveBrowser slave = new SlaveBrowser(new MockTime(0),
         "1",
         browserInfo,
         SlaveBrowser.TIMEOUT,
         null,
         CaptureHandler.QUIRKS,
         RunnerType.CLIENT,
         BrowserState.CAPTURED,
         new Instant(0));
 
     capturedBrowsers.addSlave(slave);
     ByteArrayOutputStream stream = new ByteArrayOutputStream();
     PrintWriter writer = new PrintWriter(stream);
     HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
     HomeHandler handler = new HomeHandler(capturedBrowsers, response, writer);
 
     /* expect */response.setContentType("text/html");
 
     EasyMock.replay(response);
 
     handler.handleIt();
    assertEquals("<html><head><title>JsTestDriver</title><script>"
        + "function getEl(id){return document.getElementById(id);}"
        + "function toggle(id) {\n"
        + "if (getEl(id).style.display=='block') {"
        + "getEl(id).style.display='none';"
        + "} else {"
        + "getEl(id).style.display='block';}"
        + "}</script>"
        + "</head><body>"
         + "<a href=\"/capture\">Capture This Browser</a><br/>"
         + "<a href=\"/capture?strict\">Capture This Browser in strict mode</a>"
         + "<br/><p><strong>Captured Browsers: (1)</strong></p>"
         + "<div>Id: 1<br/>Name: browser<br/>Version: 1.0"
        + "<br/>Operating System: OS<br/>In use.<br/>RunnerType CLIENT <br/>"
        + "Currently waiting...<br/>"
        + "<input type='button' value='List Files' onclick=\"toggle('f1')\"/>"
        + "<ul style='display:none' id='f1'></ul>"
        + "<input type='button' value='Show Responses' "
        + "onclick=\"toggle('r1')\"/><pre id='r1' style='display:none'>[]</pre>"
        + "</div></body></html>", stream.toString());
 
     EasyMock.verify(response);
   }
 }
