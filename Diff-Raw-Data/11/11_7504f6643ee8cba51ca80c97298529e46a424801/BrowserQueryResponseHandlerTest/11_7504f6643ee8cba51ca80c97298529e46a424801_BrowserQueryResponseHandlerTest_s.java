 /*
  * Copyright 2008 Google Inc.
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
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintWriter;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.TimeUnit;
 
 import junit.framework.TestCase;
 
 import com.google.common.collect.Lists;
 import com.google.gson.Gson;
 import com.google.jstestdriver.BrowserInfo;
 import com.google.jstestdriver.CapturedBrowsers;
 import com.google.jstestdriver.Command;
 import com.google.jstestdriver.FileInfo;
 import com.google.jstestdriver.FileResult;
 import com.google.jstestdriver.FileSource;
 import com.google.jstestdriver.JsonCommand;
 import com.google.jstestdriver.LoadedFiles;
 import com.google.jstestdriver.MockTime;
 import com.google.jstestdriver.Response;
 import com.google.jstestdriver.SlaveBrowser;
 import com.google.jstestdriver.TimeImpl;
 import com.google.jstestdriver.JsonCommand.CommandType;
 import com.google.jstestdriver.Response.ResponseType;
 import com.google.jstestdriver.protocol.BrowserStreamAcknowledged;
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 public class BrowserQueryResponseHandlerTest extends TestCase {
 
   private final Gson gson = new Gson();
   private final ByteArrayOutputStream out = new ByteArrayOutputStream();
   private final PrintWriter writer = new PrintWriter(out);
 
   private final ConcurrentMap<SlaveBrowser, List<String>> streamedResponses =
       new ConcurrentHashMap<SlaveBrowser, List<String>>();
 
   public void testGetDataFromJsPuppetServer() throws Exception {
     CapturedBrowsers browsers = new CapturedBrowsers();
     String id = "1";
     SlaveBrowser slave = new SlaveBrowser(new TimeImpl(), id, new BrowserInfo(), SlaveBrowser.TIMEOUT);
     String data = "hello";
 
     slave.createCommand(data);
     browsers.addSlave(slave);
     BrowserQueryResponseHandler handler = new BrowserQueryResponseHandler(null, null, browsers, streamedResponses);
 
     handler.service(id, null, "true", null, writer);
     assertEquals(data, out.toString());
   }
 
   public void testSettingResponseForACommand() throws Exception {
     CapturedBrowsers browsers = new CapturedBrowsers();
     String id = "1";
     SlaveBrowser slave = new SlaveBrowser(new TimeImpl(), id, new BrowserInfo(), SlaveBrowser.TIMEOUT);
 
     browsers.addSlave(slave);
     BrowserQueryResponseHandler handler = new BrowserQueryResponseHandler(null, null, browsers, streamedResponses);
     slave.createCommand("awaitingResponse");
     slave.dequeueCommand();
     slave.createCommand("BrowserCommand");
     Gson gson = new Gson();
     Response response = new Response();
 
     response.setResponse("response");
     BrowserInfo browserInfo = new BrowserInfo();
 
     browserInfo.setId(1);
     browserInfo.setName("browser");
     browserInfo.setOs("OS");
     browserInfo.setVersion("version");
     response.setBrowser(browserInfo);
     handler.service(id, gson.toJson(response), "true", null, writer);
     assertEquals("BrowserCommand", out.toString());
     assertEquals(response, slave.getResponse().getResponse());
   }
 
   public void testSimulatePollTimeoutDequeueNullCommand() throws Exception {
     CapturedBrowsers browsers = new CapturedBrowsers();
     String id = "1";
     SlaveBrowser slave = new SlaveBrowser(new TimeImpl(), id, new BrowserInfo(), SlaveBrowser.TIMEOUT);
 
     slave.setDequeueTimeout(0L, TimeUnit.NANOSECONDS);
     browsers.addSlave(slave);
     BrowserQueryResponseHandler handler = new BrowserQueryResponseHandler(null, null, browsers, streamedResponses);
 
     handler.service(id, null, "true", null, writer);
    assertEquals("noop", out.toString());
   }
 
   public void testConnectionHeartBeat() throws Exception {
     CapturedBrowsers browsers = new CapturedBrowsers();
     String id = "1";
     MockTime time = new MockTime(42L);
     SlaveBrowser slave = new SlaveBrowser(time, id, new BrowserInfo(), SlaveBrowser.TIMEOUT);
     String data = "hello";
 
     slave.createCommand(data);
     browsers.addSlave(slave);
     BrowserQueryResponseHandler handler = new BrowserQueryResponseHandler(null, null, browsers, streamedResponses);
 
     handler.service(id, null, null, null, writer);
     assertEquals(42L, slave.getLastHeartBeat().getMillis());
   }
 
   public void testBrowserIsNotSlave() throws Exception {
     CapturedBrowsers capturedBrowsers = new CapturedBrowsers();
     BrowserQueryResponseHandler handler =
         new BrowserQueryResponseHandler(null, null, capturedBrowsers, streamedResponses);
 
     handler.service("1", "response", "true", null, writer);
     assertEquals(0, out.toString().length());
   }
 
   public void testDoNotGetCommandIfNotLastResponse() throws Exception {
     CapturedBrowsers browsers = new CapturedBrowsers();
     String id = "1";
     SlaveBrowser slave = new SlaveBrowser(new TimeImpl(), id, new BrowserInfo(), SlaveBrowser.TIMEOUT);
 
     browsers.addSlave(slave);
     BrowserQueryResponseHandler handler = new BrowserQueryResponseHandler(null, null, browsers, streamedResponses);
     slave.createCommand("awaitingResponse");
     slave.dequeueCommand();
     slave.createCommand("BrowserCommand");
     Gson gson = new Gson();
     Response response = new Response();
 
     response.setResponse("response");
     BrowserInfo browserInfo = new BrowserInfo();
 
     browserInfo.setId(1);
     browserInfo.setName("browser");
     browserInfo.setOs("OS");
     browserInfo.setVersion("version");
     response.setBrowser(browserInfo);
     handler.service(id, gson.toJson(response), "", null, writer);
     assertEquals(new Gson().toJson(new BrowserStreamAcknowledged(Collections.<String>emptyList())),
         out.toString());
   }
 
   public void testFilesLoadedAreAddedToTheBrowserFileSet() throws Exception {
     CapturedBrowsers browsers = new CapturedBrowsers();
     String id = "1";
     SlaveBrowser slave = new SlaveBrowser(new TimeImpl(), id, new BrowserInfo(), SlaveBrowser.TIMEOUT);
 
     browsers.addSlave(slave);
     BrowserQueryResponseHandler handler = new BrowserQueryResponseHandler(null, null, browsers, streamedResponses);
     List<FileResult> fileResults = new LinkedList<FileResult>();
 
     fileResults.add(new FileResult(new FileSource("/test/filename1.js", 123), true, ""));
     fileResults.add(new FileResult(new FileSource("/test/filename2.js", 456), true, ""));
     fileResults.add(new FileResult(new FileSource("/test/filename3.js", 789), true, ""));
     slave.createCommand("awaitingResponse");
     slave.dequeueCommand();
     slave.createCommand("BrowserCommand");
     Response response = new Response();
 
     response.setType(ResponseType.FILE_LOAD_RESULT.name());
     response.setResponse(gson.toJson(new LoadedFiles(fileResults)));
     handler.service("1", gson.toJson(response), "", null, writer);
 
     Set<FileInfo> fileInfos = slave.getFileSet();
 
     assertEquals(3, fileInfos.size());
     Iterator<FileInfo> iterator = fileInfos.iterator();
 
     FileInfo info1 = iterator.next();
 
     assertEquals("filename1.js", info1.getFilePath());
     assertEquals(123, info1.getTimestamp());
 
     FileInfo info2 = iterator.next();
 
     assertEquals("filename2.js", info2.getFilePath());
     assertEquals(456, info2.getTimestamp());
 
     FileInfo info3 = iterator.next();
 
     assertEquals("filename3.js", info3.getFilePath());
     assertEquals(789, info3.getTimestamp());
   }
 
   public void testResetClearsTheBrowserFileSet() throws Exception {
     CapturedBrowsers browsers = new CapturedBrowsers();
     String id = "1";
     SlaveBrowser slave = new SlaveBrowser(new TimeImpl(), id, new BrowserInfo(), SlaveBrowser.TIMEOUT);
 
     browsers.addSlave(slave);
     BrowserQueryResponseHandler handler =
         new BrowserQueryResponseHandler(null, null, browsers, streamedResponses);
 
     slave.addFiles(Lists.newArrayList(new FileInfo()));
     Response response = new Response();
     response.setType(ResponseType.RESET_RESULT.name());
     response.setResponse("Runner reset.");
     response.setBrowser(new BrowserInfo());
 
     JsonCommand resetCommand = new JsonCommand(CommandType.RESET,
         Collections.<String> emptyList());
     slave.createCommand(gson.toJson(resetCommand));
     slave.dequeueCommand();
 
     handler.service(id, gson.toJson(response), "", null, writer);
 
     Set<FileInfo> fileInfos = slave.getFileSet();
 
     assertEquals("Command is not in the approriate state to deal with race condition.",
         new Command(gson.toJson(resetCommand)), slave.getCommandRunning());
 
     assertEquals(0, fileInfos.size());
   }
 }
