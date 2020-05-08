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
 package com.google.jstestdriver.server.handlers;
 
 import static org.easymock.EasyMock.expect;
 
 import java.io.CharArrayWriter;
 import java.io.PrintWriter;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import junit.framework.TestCase;
 
 import org.easymock.EasyMock;
 import org.easymock.IMocksControl;
 import org.joda.time.Instant;
 
 import com.google.gson.Gson;
 import com.google.jstestdriver.BrowserInfo;
 import com.google.jstestdriver.CapturedBrowsers;
 import com.google.jstestdriver.Response;
 import com.google.jstestdriver.SlaveBrowser;
 import com.google.jstestdriver.StreamMessage;
 import com.google.jstestdriver.Time;
 import com.google.jstestdriver.TimeImpl;
 import com.google.jstestdriver.Response.ResponseType;
 import com.google.jstestdriver.runner.RunnerType;
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 public class CommandGetHandlerTest extends TestCase {
 
   public void testListBrowsers() throws Exception {
     CapturedBrowsers capturedBrowsers = new CapturedBrowsers();
     BrowserInfo browserInfo = new BrowserInfo();
     browserInfo.setId(1);
     SlaveBrowser slave =
         new SlaveBrowser(new TimeImpl(), "1", browserInfo, 20, null, CaptureHandler.QUIRKS,
             RunnerType.CLIENT);
 
     capturedBrowsers.addSlave(slave);
     CommandGetHandler handler = new CommandGetHandler(null, null, new Gson(), capturedBrowsers);
 
    assertEquals("[{\"id\":1,\"uploadSize\":1,\"serverReceivedHeartbeat\":false}]", handler.listBrowsers());
   }
 
   public void testBrowserPanic() throws Exception {
     CapturedBrowsers capturedBrowsers = new CapturedBrowsers();
     BrowserInfo browserInfo = new BrowserInfo();
     browserInfo.setId(1);
 
     CharArrayWriter out = new CharArrayWriter();
     PrintWriter writer = new PrintWriter(out);
 
     IMocksControl control = EasyMock.createControl();
 
     HttpServletResponse response = control.createMock(HttpServletResponse.class);
     expect(response.getWriter()).andReturn(writer).anyTimes();
     HttpServletRequest request = control.createMock(HttpServletRequest.class);
     expect(request.getParameter("listBrowsers")).andReturn(null);
     expect(request.getParameter("nextBrowserId")).andReturn(null);
     expect(request.getParameter("id")).andReturn(browserInfo.getId().toString());
     control.replay();
 
     SlaveBrowser slave = new SlaveBrowser(new Time() {
       int i = 0;
 
       public Instant now() {
         i += 100;
         return new Instant(i);
       }
     }, "1", browserInfo, 0, null, CaptureHandler.QUIRKS, RunnerType.CLIENT);
     slave.addResponse(new Response(ResponseType.LOG.name(), "", browserInfo, "", -1), true);
     capturedBrowsers.addSlave(slave);
     Gson gson = new Gson();
     CommandGetHandler handler = new CommandGetHandler(request, response, gson, capturedBrowsers);
     handler.handleIt();
     assertEquals(ResponseType.BROWSER_PANIC, gson.fromJson(out.toString(), StreamMessage.class)
         .getResponse().getResponseType());
   }
 }
