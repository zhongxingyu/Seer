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
 
 import com.google.common.collect.Lists;
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.google.jstestdriver.JsonCommand.CommandType;
 
 import junit.framework.TestCase;
 
 import java.util.List;
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 public class CommandServletTest extends TestCase {
 
   private final Gson gson = new Gson();
 
   public void testListBrowsers() throws Exception {
     CapturedBrowsers capturedBrowsers = new CapturedBrowsers();
     BrowserInfo browserInfo = new BrowserInfo();
     browserInfo.setId(1);
     SlaveBrowser slave = new SlaveBrowser(new TimeImpl(), "1", browserInfo);
 
     capturedBrowsers.addSlave(slave);
     CommandServlet servlet = new CommandServlet(capturedBrowsers, null, null, null);
 
     assertEquals("[{\"id\":1}]", servlet.listBrowsers());
   }
 
   public void testRewriteUrls() throws Exception {
     CapturedBrowsers capturedBrowsers = new CapturedBrowsers();
     BrowserInfo browserInfo = new BrowserInfo();
     browserInfo.setId(1);
     SlaveBrowser slave = new SlaveBrowser(new TimeImpl(), "1", browserInfo);
 
     capturedBrowsers.addSlave(slave);
     CommandServlet servlet =
         new CommandServlet(capturedBrowsers, new DefaultURLTranslator(), new DefaultURLRewriter(),
             new ForwardingMapper());
     List<String> parameters = Lists.newArrayList();
     List<FileSource> fileSources = Lists.newArrayList();
 
     fileSources.add(new FileSource("http://server/file1.js", -1));
     fileSources.add(new FileSource("http://server/file2.js", -1));
     parameters.add(gson.toJson(fileSources));
     parameters.add("false");
     JsonCommand command = new JsonCommand(CommandType.LOADTEST, parameters);
 
     servlet.service("1", gson.toJson(command));
     Command cmd = slave.dequeueCommand();
 
     command = gson.fromJson(cmd.getCommand(), JsonCommand.class);
     assertEquals(2, command.getParameters().size());
     List<FileSource> changedFileSources =
         gson.fromJson(command.getParameters().get(0), new TypeToken<List<FileSource>>() {}
             .getType());
 
     assertEquals(2, changedFileSources.size());
    assertEquals("/file1.js", changedFileSources.get(0).getFileSrc());
     assertEquals("http://server/file1.js", changedFileSources.get(0).getBasePath());
    assertEquals("/file2.js", changedFileSources.get(1).getFileSrc());
     assertEquals("http://server/file2.js", changedFileSources.get(1).getBasePath());
   }
 }
