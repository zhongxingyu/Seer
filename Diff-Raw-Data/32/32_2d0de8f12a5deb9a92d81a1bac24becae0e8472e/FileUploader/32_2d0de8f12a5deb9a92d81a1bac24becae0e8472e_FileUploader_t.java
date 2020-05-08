 /*
  * Copyright 2010 Google Inc.
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
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import com.google.jstestdriver.JsonCommand.CommandType;
import com.google.jstestdriver.Response.ResponseType;
 import com.google.jstestdriver.browser.BrowserFileSet;
 import com.google.jstestdriver.hooks.FileInfoScheme;
 import com.google.jstestdriver.model.HandlerPathPrefix;
 import com.google.jstestdriver.servlet.fileset.BrowserFileCheck;
 import com.google.jstestdriver.servlet.fileset.ServerFileCheck;
 import com.google.jstestdriver.servlet.fileset.ServerFileUpload;
 import com.google.jstestdriver.util.StopWatch;
 
 /**
  * Handles the uploading of files.
  *
  * @author corysmith@google.com (Cory Smith)
  */
 public class FileUploader {
   public static final int CHUNK_SIZE = 50;
 
   private final StopWatch stopWatch;
   private final Gson gson = new Gson();
   private final Server server;
   private final String baseUrl;
   private final FileLoader fileLoader;
   private final JsTestDriverFileFilter filter;
   private final Set<FileInfoScheme> schemes;
 
   private static final Logger logger = LoggerFactory.getLogger(FileUploader.class);
 
   private final HandlerPathPrefix prefix;
 
   @Inject
   public FileUploader(StopWatch stopWatch, Server server,
       @Named("server") String baseUrl, FileLoader fileLoader,
       JsTestDriverFileFilter filter,
       Set<FileInfoScheme> schemes,
       @Named("serverHandlerPrefix") HandlerPathPrefix prefix) {
     this.stopWatch = stopWatch;
     this.server = server;
     this.baseUrl = baseUrl;
     this.fileLoader = fileLoader;
     this.filter = filter;
     this.schemes = schemes;
     this.prefix = prefix;
   }
 
   /** Determines what files have been changed as compared to the server. */
   public List<FileInfo> determineBrowserFileSet(String browserId, Set<FileInfo> files,
       ResponseStream stream) {
     
     stopWatch.start("get upload set %s", browserId);
     Map<String, String> fileSetParams = new LinkedHashMap<String, String>();
 
     final List<FileInfo> serverable = Lists.newLinkedList();
     for (FileInfo fileInfo : files) {
       if (!fileInfo.isServeOnly()) {
         serverable.add(fileInfo);
       }
     }
 
     fileSetParams.put("id", browserId);
     fileSetParams.put("data", gson.toJson(serverable));
     fileSetParams.put("action", BrowserFileCheck.ACTION);
     String postResult = server.post(baseUrl + "/fileSet", fileSetParams);
     stopWatch.stop("get upload set %s", browserId);
 
     if (postResult.length() > 0) {
       stopWatch.start("resolving browser upload %s", browserId);
       BrowserFileSet browserFileSet = gson.fromJson(postResult, BrowserFileSet.class);
       logger.debug("Updating files {}", browserFileSet.getFilesToUpload());
 
       // need a linked hashset here to avoid adding a file more than once.
       final Set<FileInfo> finalFilesToUpload = new LinkedHashSet<FileInfo>();
       // reset if there are extra files in the browser
       if (!browserFileSet.getExtraFiles().isEmpty()) {
         reset(browserId, stream);
         // since the browser has been reset, reload all files.
         finalFilesToUpload.addAll(serverable);
       } else {
         for (FileInfo file : browserFileSet.getFilesToUpload()) {
           finalFilesToUpload.addAll(determineInBrowserDependencies(file, Lists.newArrayList(serverable)));
         }
       }
       stopWatch.stop("resolving browser upload %s", browserId);
       return Lists.newArrayList(finalFilesToUpload);
     } else {
       logger.debug("No files to update on server.");
     }
     return Collections.<FileInfo>emptyList();
   }
 
   /** Uploads the changed files to the server and the browser. */
   public void uploadFileSet(String browserId, Set<FileInfo> files, ResponseStream stream) {
 
     logger.debug("Files: {}",
         Lists.transform(Lists.newArrayList(files), new Function<FileInfo, String>() {
           public String apply(FileInfo in) {
             return "\n" + in.getFilePath();
           }
         }));
     
     stopWatch.start("determineServerFileSet(%s)", browserId);
     final List<FileInfo> serverFilesToUpdate = determineServerFileSet(files);
     stopWatch.stop("determineServerFileSet(%s)", browserId);
 
     logger.debug("Files: {}",
         Lists.transform(Lists.newArrayList(files), new Function<FileInfo, String>() {
           public String apply(FileInfo in) {
             return "\n" + in.getFilePath();
           }
         }));
     stopWatch.start("upload to server %s", browserId);
     uploadToServer(serverFilesToUpdate);
     stopWatch.stop("upload to server %s", browserId);
 
     logger.debug("Files: {}",
         Lists.transform(Lists.newArrayList(files), new Function<FileInfo, String>() {
           public String apply(FileInfo in) {
             return "\n" + in.getFilePath();
           }
         }));
     stopWatch.start("determineBrowserFileSet(%s)", browserId);
     final List<FileInfo> browserFilesToUpdate = determineBrowserFileSet(browserId, files, stream);
     stopWatch.stop("determineBrowserFileSet(%s)", browserId);
 
     stopWatch.start("uploadToTheBrowser(%s)", browserId);
     uploadToTheBrowser(browserId, stream, browserFilesToUpdate, CHUNK_SIZE);
     stopWatch.stop("uploadToTheBrowser(%s)", browserId);
   }
 
   /**
    * Creates a loaded list of files that are out of date with the server cache.
    */
   public List<FileInfo> determineServerFileSet(Set<FileInfo> files) {
     Map<String, String> fileSetParams = new LinkedHashMap<String, String>();
     fileSetParams.put("data", gson.toJson(files));
     fileSetParams.put("action", ServerFileCheck.ACTION);
     String postResult = server.post(baseUrl + "/fileSet", fileSetParams);
     final Collection<FileInfo> filesToUpload =
         gson.fromJson(postResult, new TypeToken<Collection<FileInfo>>() {}.getType());
 
     // need to use the same instance we have locally, as types don't pass the
     // json conversion.
     Set<FileInfo> filesToLoad = Sets.filter(files, new Predicate<FileInfo>() {
       public boolean apply(FileInfo file) {
         return filesToUpload.contains(file);
       }
     });
 
     if (logger.isDebugEnabled()) {
       logger.debug("Loading {} from disk",
           Lists.transform(Lists.newArrayList(filesToLoad), new Function<FileInfo, String>() {
             public String apply(FileInfo in) {
               return "\n" + in.getFilePath();
             }
           }));
     }
     return fileLoader.loadFiles(filesToLoad, false);
   }
 
   /** Uploads files to the browser. */
   public void uploadToTheBrowser(String browserId, ResponseStream stream,
       List<FileInfo> loadedFiles, int chunkSize) {
     List<FileSource> filesSrc = Lists.newLinkedList(filterFilesToLoad(loadedFiles));
     int numberOfFilesToLoad = filesSrc.size();
     logger.debug("Files toupload {}",
         Lists.transform(Lists.newArrayList(loadedFiles), new Function<FileInfo, String>() {
           public String apply(FileInfo in) {
             return "\n" + in.toString();
           }
         }));
     for (int i = 0; i < numberOfFilesToLoad; i += chunkSize) {
 
       int chunkEndIndex = Math.min(i + chunkSize, numberOfFilesToLoad);
       List<String> loadParameters = new LinkedList<String>();
       List<FileSource> filesToLoad = filesSrc.subList(i, chunkEndIndex);
       loadParameters.add(gson.toJson(filesToLoad));
       loadParameters.add("false");
       JsonCommand cmd = new JsonCommand(CommandType.LOADTEST, loadParameters);
       Map<String, String> loadFileParams = new LinkedHashMap<String, String>();
 
       loadFileParams.put("id", browserId);
       loadFileParams.put("data", gson.toJson(cmd));
       if (logger.isDebugEnabled()) {
         logger.debug("Sending LOADTEST to {} for {}", browserId,
             Lists.transform(filesToLoad, new Function<FileSource, String>() {
               @Override
               public String apply(FileSource in) {
                 return "\n" + in.getFileSrc();
               }
             }));
       }
       server.post(baseUrl + "/cmd", loadFileParams);
       while (true) {
         String jsonResponse = server.fetch(baseUrl + "/cmd?id=" + browserId);
         StreamMessage message = gson.fromJson(jsonResponse, StreamMessage.class);
         Response response = message.getResponse();
         logger.trace("LOADTEST response for {}", response);
         stream.stream(response);
         if (message.isLast()) {
          logger.debug("Finished LOADTEST on {} with {}", browserId, response.getResponseType());
           break;
         }
       }
     }
    
   }
 
   public void uploadToServer(final List<FileInfo> loadedFiles) {
     if (loadedFiles.isEmpty()) {
       return;
     }
     Map<String, String> uploadFileParams = new LinkedHashMap<String, String>();
     uploadFileParams.put("action", ServerFileUpload.ACTION);
     uploadFileParams.put("data", gson.toJson(loadedFiles));
     server.post(baseUrl + "/fileSet", uploadFileParams);
   }
 
   private void reset(String browserId, ResponseStream stream) {
     stopWatch.start("reset %s", browserId);
     JsonCommand cmd = new JsonCommand(CommandType.RESET, Lists.newArrayList("load"));
     Map<String, String> resetParams = new LinkedHashMap<String, String>();
 
     resetParams.put("id", browserId);
     resetParams.put("data", gson.toJson(cmd));
     server.post(baseUrl + "/cmd", resetParams);
 
     logger.debug("Starting File Upload Refresh for {}", browserId);
    Response response;
    StreamMessage message;
    do {
      String jsonResponse = server.fetch(baseUrl + "/cmd?id=" + browserId);
      message = gson.fromJson(jsonResponse, StreamMessage.class);
      response = message.getResponse();
      stream.stream(response);
    } while(!(ResponseType.RESET_RESULT.equals(response.getResponseType()) && message.isLast()));
     logger.debug("Finished File Upload Refresh for {}", browserId);
     stopWatch.stop("reset %s", browserId);
   }
 
   /**
    * Determines what files must be reloaded in the browser, based on this file
    * being updated.
    */
   private Collection<FileInfo> determineInBrowserDependencies(FileInfo file, List<FileInfo> files) {
     LinkedHashSet<FileInfo> deps = Sets.newLinkedHashSet();
     for (FileInfo dep : filter.resolveFilesDeps(file, files)) {
       deps.add(dep);
     }
     return deps;
   }
 
   private List<FileSource> filterFilesToLoad(Collection<FileInfo> fileInfos) {
     List<FileSource> filteredFileSources = new LinkedList<FileSource>();
 
     for (FileInfo fileInfo : fileInfos) {
       if (!fileInfo.isServeOnly()) {
         filteredFileSources.add(fileInfo.toFileSource(prefix, schemes));
       }
     }
     return filteredFileSources;
   }
 }
