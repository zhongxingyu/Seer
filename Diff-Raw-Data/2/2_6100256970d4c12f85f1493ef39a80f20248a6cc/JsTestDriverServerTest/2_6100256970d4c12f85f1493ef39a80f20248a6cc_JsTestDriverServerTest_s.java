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
 package com.google.jstestdriver;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 
 import junit.framework.TestCase;
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 public class JsTestDriverServerTest extends TestCase {
 
   private JsTestDriverServer server =
       new JsTestDriverServer(4224, new CapturedBrowsers(), new FilesCache(
           new HashMap<String, FileInfo>()), new DefaultURLTranslator(), new DefaultURLRewriter());
 
   @Override
   protected void tearDown() throws Exception {
     server.stop();
   }
 
   public void testServerLifecycle() throws Exception {
     server.start();
     URL url = new URL("http://localhost:4224/hello");
     assertEquals("hello", read(url.openStream()));
   }
 
   private String read(InputStream inputStream) throws IOException {
     StringBuilder builder = new StringBuilder();
     int ch;
 
     while ((ch = inputStream.read()) != -1) {
       builder.append((char) ch);
     }
     return builder.toString();
   }
 
   public void testStaticFiles() throws Exception {
     server.start();
    URL url = new URL("http://localhost:4224/slave/XXX/HeartbeatClient.html");
     assertTrue(read(url.openStream()).length() > 0);
   }
 
   public void testListBrowsers() throws Exception {
     JsTestDriverClient client = new JsTestDriverClientImpl(new CommandTaskFactory(
         new DefaultFileFilter(), null, null), new LinkedHashSet<FileInfo>(),
         "http://localhost:4224", new HttpServer());
 
     server.start();
     Collection<BrowserInfo> browsers = client.listBrowsers();
     assertEquals(0, browsers.size());
   }
 }
