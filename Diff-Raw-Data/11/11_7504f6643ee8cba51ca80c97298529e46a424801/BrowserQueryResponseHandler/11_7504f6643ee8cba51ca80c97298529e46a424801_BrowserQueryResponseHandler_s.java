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
 
 import com.google.gson.Gson;
 import com.google.inject.Inject;
 import com.google.jstestdriver.CapturedBrowsers;
 import com.google.jstestdriver.Command;
 import com.google.jstestdriver.FileInfo;
 import com.google.jstestdriver.FileResult;
 import com.google.jstestdriver.FileSource;
 import com.google.jstestdriver.JsonCommand;
 import com.google.jstestdriver.LoadedFiles;
 import com.google.jstestdriver.Response;
 import com.google.jstestdriver.SlaveBrowser;
 import com.google.jstestdriver.protocol.BrowserStreamAcknowledged;
 import com.google.jstestdriver.requesthandlers.RequestHandler;
 
 import org.mortbay.jetty.MimeTypes;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 class BrowserQueryResponseHandler implements RequestHandler {
   private static final Logger logger =
       LoggerFactory.getLogger(BrowserQueryResponseHandler.class);
 
   /** for something completely unrelated see: http://noop.googlecode.com/ */
   private static final String NOOP = "noop";
 
   private final Gson gson = new Gson();
 
   private final HttpServletRequest request;
   private final HttpServletResponse response;
   private final CapturedBrowsers browsers;
   // TODO(corysmith): factor out a streaming session class.
   private final ConcurrentMap<SlaveBrowser, List<String>> streamedResponses;
 
   @Inject
   public BrowserQueryResponseHandler(
       HttpServletRequest request,
       HttpServletResponse response,
       CapturedBrowsers browsers,
       ConcurrentMap<SlaveBrowser,
       List<String>> streamedResponses) {
     this.request = request;
     this.response = response;
     this.browsers = browsers;
     this.streamedResponses = streamedResponses;
   }
 
   public void handleIt() throws IOException {
     logger.trace("Browser Query Post:\n\tpath:{}\n\tresponse:{}\n\tdone:{}\n\tresponseId:{}",
         new Object[] {
           request.getPathInfo().substring(1),
           request.getParameter("response"),
           request.getParameter("done"),
           request.getParameter("responseId")
         });
 
     response.setContentType(MimeTypes.TEXT_JSON_UTF_8);
     service(request.getPathInfo().substring(1),
             request.getParameter("response"),
             request.getParameter("done"),
             request.getParameter("responseId"),
             response.getWriter());
   }
 
   public void service(String id,
                       String response,
                       String done,
                       String responseId,
                       PrintWriter writer) {
     SlaveBrowser browser = browsers.getBrowser(id);
 
     if (browser != null) {
       boolean isLast = Boolean.parseBoolean(done);
       serviceBrowser(response, isLast, responseId, writer, browser);
     } else {
       logger.warn("Unknown browser {}", id);
     }
     writer.flush();
   }
 
   private void serviceBrowser(String response, Boolean done, String responseId, PrintWriter writer,
       SlaveBrowser browser) {
     addResponseId(responseId, browser);
     browser.heartBeat();
     Command command = null;
     if (isResponseValid(response) && browser.isCommandRunning()) {
       Response res = gson.fromJson(response, Response.class);
       // TODO (corysmith): Replace this with polymorphism,
       // using the response type to create disposable actions.
       switch (res.getResponseType()) {
         case FILE_LOAD_RESULT:
           LoadedFiles loadedFiles = gson.fromJson(res.getResponse(), res.getGsonType());
           Collection<FileResult> allLoadedFiles = loadedFiles.getLoadedFiles();
           if (!allLoadedFiles.isEmpty()) {
             LinkedHashSet<FileInfo> fileInfos = new LinkedHashSet<FileInfo>();
             Collection<FileSource> errorFiles = new LinkedHashSet<FileSource>();
 
             for (FileResult fileResult : allLoadedFiles) {
               FileSource fileSource = fileResult.getFileSource();
 
               if (fileResult.isSuccess()) {
                 fileInfos.add(new FileInfo(fileSource.getBasePath(), fileSource.getTimestamp(),
                     -1, false, false, null));
               } else {
                 errorFiles.add(fileSource);
               }
             }
             browser.addFiles(fileInfos);
             if (errorFiles.size() > 0) {
               logger.debug("clearing fileset on browser errors:" + errorFiles.size());
               browser.resetFileSet();
             }
           }
           break;
         // reset the browsers fileset.
         case RESET_RESULT:
           Command commandRunning = browser.getCommandRunning();
 
           if (commandRunning != null) {
             JsonCommand jsonCommand = gson.fromJson(commandRunning.getCommand(), JsonCommand.class);
 
             if (jsonCommand.getCommand().equals(JsonCommand.CommandType.RESET.getCommand())) {
               command = browser.getLastDequeuedCommand();
             }
           }
         //$FALL-THROUGH$
         case BROWSER_READY:
           logger.debug("Clearing fileset for {}", browser);
           browser.resetFileSet();
           break;
       }
       //logger.trace("Received:\n done: {} \n res:\n {}\n", new Object[] {done, res});
       browser.addResponse(res, done);
     }
     if (isResponseIdValid(responseId) && !done && !isResponseValid(response)) {
       logger.trace("Streaming query for ids {} from {}", streamedResponses.get(browser), browser);
     }
     // TODO(corysmith): What do we do?
     if (!isResponseValid(response) && done && browser.isCommandRunning()) {
       logger.error("Streaming ending, but no response sent for {} while running {}",
           browser,
           browser.getCommandRunning());
     }
     // TODO(corysmith): Refactoring the streaming into a separate layer.
     if (!done) { // we are still streaming, so we respond with the streaming
                  // acknowledge.
       // this is independent of receiving an actual response.
       final String jsonResponse = gson.toJson(new BrowserStreamAcknowledged(streamedResponses.get(browser)));
       logger.info("sending jsonResponse {}", jsonResponse);
       writer.print(jsonResponse);
       writer.flush();
       return;
     } else {
       streamedResponses.clear();
     }
     if(command == null) {
      command = browser.dequeueCommand();
      browser.heartBeat();
     }
     
     logger.info("sending command {}", command == null ? "null" : command.getCommand());
    writer.print(command);
   }
 
   private boolean isResponseValid(String response) {
     return response != null && !"null".equals(response) && response.length() > 0;
   }
 
   private void addResponseId(String responseId, SlaveBrowser browser) {
     if (!streamedResponses.containsKey(browser)) {
       streamedResponses.put(browser, new CopyOnWriteArrayList<String>());
     }
     if (isResponseIdValid(responseId)) {
       return;
     }
     streamedResponses.get(browser).add(responseId);
   }
 
   private boolean isResponseIdValid(String responseId) {
     return responseId == null || "".equals(responseId);
   }
 }
