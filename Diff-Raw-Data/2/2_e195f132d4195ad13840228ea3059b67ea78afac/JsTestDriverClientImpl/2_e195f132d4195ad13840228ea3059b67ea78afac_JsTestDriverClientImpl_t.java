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
 
 import com.google.common.collect.Sets;
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
import com.google.common.collect.Lists;
 import com.google.inject.name.Named;
 import com.google.jstestdriver.JsonCommand.CommandType;
 import com.google.jstestdriver.model.JstdTestCase;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 @Singleton
 public class JsTestDriverClientImpl implements JsTestDriverClient {
 
   private final Gson gson = new Gson();
 
   private final CommandTaskFactory commandTaskFactory;
   private final String baseUrl;
   private final Server server;
 
   private final Boolean debug;
 
   private final FileUploader uploader;
 
   @Inject
   public JsTestDriverClientImpl(CommandTaskFactory commandTaskFactory,
                                 @Named("server") String baseUrl,
                                 Server server,
                                 @Named("debug") Boolean debug,
                                 FileUploader uploader) {
     this.commandTaskFactory = commandTaskFactory;
     this.baseUrl = baseUrl;
     this.server = server;
     this.debug = debug;
     this.uploader = uploader;
   }
 
   public Collection<BrowserInfo> listBrowsers() {
     return gson.fromJson(server.fetch(baseUrl + "/cmd?listBrowsers"),
         new TypeToken<Collection<BrowserInfo>>() {}.getType());
   }
 
   public String getNextBrowserId() {
     return server.fetch(baseUrl + "/cmd?nextBrowserId");
   }
 
   private void sendCommand(String browserId, ResponseStream stream, String cmd,
       boolean uploadFiles, JstdTestCase testCase) {
     Map<String, String> params = new LinkedHashMap<String, String>();
 
     params.put("data", cmd);
     params.put("id", browserId);
     CommandTask task =
         commandTaskFactory.getCommandTask(stream, baseUrl, server, params, uploadFiles);
 
     // TODO(corysmith): Work out the contradiction between ResponseStream and
     // RunData, possibly returning runData here.
     task.run(testCase);
   }
 
   public void eval(String browserId, ResponseStream responseStream, String cmd, JstdTestCase testCase) {
     List<String> parameters = new LinkedList<String>();
 
     parameters.add(cmd);
     JsonCommand jsonCmd = new JsonCommand(CommandType.EXECUTE, parameters);
 
     sendCommand(browserId, responseStream, gson.toJson(jsonCmd), false, testCase);
   }
 
   public void runAllTests(String browserId, ResponseStream responseStream, boolean captureConsole,
       JstdTestCase testCase) {
     runTests(browserId, responseStream, Lists.newArrayList("all"), captureConsole, testCase);
   }
 
   public void reset(String browserId, ResponseStream responseStream, JstdTestCase testCase) {
     JsonCommand cmd = new JsonCommand(CommandType.RESET, Collections.<String>emptyList());
 
     sendCommand(browserId, responseStream, gson.toJson(cmd), false, testCase);
   }
 
   public void runTests(String browserId, ResponseStream responseStream, List<String> tests,
       boolean captureConsole, JstdTestCase testCase) {
     List<String> parameters = new LinkedList<String>();
 
     parameters.add(gson.toJson(tests));
     parameters.add(String.valueOf(captureConsole));
     parameters.add(debug ? "1":""); // The json serialization of 0,
     // false as strings evals to true on the js side. so, "" it is.
     JsonCommand cmd = new JsonCommand(CommandType.RUNTESTS, parameters);
 
     sendCommand(browserId, responseStream, gson.toJson(cmd), true, testCase);
   }
 
   public void dryRun(String browserId, ResponseStream responseStream, JstdTestCase testCase) {
     JsonCommand cmd = new JsonCommand(CommandType.DRYRUN, Collections.<String>emptyList());
 
     sendCommand(browserId, responseStream, gson.toJson(cmd), true, testCase);
   }
 
   public void dryRunFor(String browserId, ResponseStream responseStream, List<String> expressions,
       JstdTestCase testCase) {
     List<String> parameters = new LinkedList<String>();
 
     parameters.add(gson.toJson(expressions));
     JsonCommand cmd = new JsonCommand(CommandType.DRYRUNFOR, parameters);
 
     sendCommand(browserId, responseStream, gson.toJson(cmd), true, testCase);
   }
 
   public void uploadFiles(String browserId, JstdTestCase testCase) {
     Set<FileInfo> fileSet = Sets.newLinkedHashSet(testCase.getDependencies());
     fileSet.addAll(testCase.getTests());
     uploader.uploadFileSet(browserId, fileSet, new BrowserPanicResponseStream());
   }
 }
