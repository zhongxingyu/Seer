 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2013 Per Cederberg. All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.app.web;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.lang.StringUtils;
 import org.rapidcontext.app.ApplicationContext;
 import org.rapidcontext.app.plugin.PluginManager;
 import org.rapidcontext.core.data.Array;
 import org.rapidcontext.core.data.Binary;
 import org.rapidcontext.core.data.Dict;
 import org.rapidcontext.core.js.JsSerializer;
 import org.rapidcontext.core.storage.Metadata;
 import org.rapidcontext.core.storage.Path;
 import org.rapidcontext.core.type.Session;
 import org.rapidcontext.core.web.Mime;
 import org.rapidcontext.core.web.Request;
 import org.rapidcontext.util.FileUtil;
 
 /**
  * An app web service. This service extends the file web service with
  * a RapidContext API and a default app launcher page. The API pages
  * are available under the "/rapidcontext/" path.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class AppWebService extends FileWebService {
 
     /**
      * The class logger.
      */
     private static final Logger LOG =
         Logger.getLogger(AppWebService.class.getName());
 
     /**
      * The dictionary key for the app identifier.
      */
     public static final String KEY_APP = "app";
 
     /**
      * The storage web service used for the to "rapidcontext/storage"
      * URLs.
      */
     protected StorageWebService storage;
 
     /**
      * Returns the platform version number.
      *
      * @return the platform version number
      */
     protected static String version() {
         ApplicationContext ctx = ApplicationContext.getInstance();
         Dict dict = (Dict) ctx.getStorage().load(PluginManager.PATH_INFO);
         return dict.getString("version", "1");
     }
 
     /**
      * Finds binary files of a specified type from the storage. The
      * type must be used both as a named subdirectory (i.e. "css")
      * and as file name suffix (i.e. "*.css") in order to match. The
      * returned files will be prefixed with "rapidcontext/files/".
      *
      * @param base           the base storage path (e.g. "/files/")
      * @param type           the file type to find
      *
      * @return a sorted list of all matching files found in storage
      */
     protected static Array resources(Path base, String type) {
         ApplicationContext  ctx = ApplicationContext.getInstance();
         String              root = base.toString();
         Path                path = base.child(type, true);
         Metadata[]          meta;
         String              file;
         Array               res = new Array();
 
         meta = ctx.getStorage().lookupAll(path);
         for (int i = 0; i < meta.length; i++) {
             file = StringUtils.removeStart(meta[i].path().toString(), root);
             if (meta[i].isBinary() && file.endsWith("." + type)) {
                 res.add("rapidcontext/files/" + file);
             }
         }
         res.sort();
         return res;
     }
 
     /**
      * Creates a new app web service from a serialized representation.
      *
      * @param id             the object identifier
      * @param type           the object type name
      * @param dict           the serialized representation
      */
     public AppWebService(String id, String type, Dict dict) {
         super(id, type, dict);
         dict.set(KEY_APP, appId());
         storage = new StorageWebService("id", "type", new Dict());
     }
 
     /**
      * Returns the app identifier for the default launcher.
      *
      * @return the app identifier for the default launcher, or
      *         "start" if no app identifier was specified
      */
     public String appId() {
         return dict.getString(KEY_APP, "start");
     }
 
     /**
      * Processes a request for this handler. This method assumes
      * local request paths (removal of the mapped URL base).
      *
      * @param request the request to process
      */
     public void process(Request request) {
         if (request.matchPath("rapidcontext/storage/")) {
             storage.process(request);
         } else if (!request.hasResponse()) {
             super.process(request);
         }
     }
 
     /**
      * Processes an HTTP GET request.
      *
      * @param request        the request to process
      */
     protected void doGet(Request request) {
         super.doGet(request);
         if (!request.hasResponse()) {
             String path = request.getPath();
             String baseUrl = StringUtils.removeEnd(request.getUrl(), path);
             boolean isRoot = path.equals("") || path.startsWith("index.htm");
             if (request.matchPath("rapidcontext/files/")) {
                 processFile(request, new Path(PATH_FILES, request.getPath()));
             } else if (request.matchPath("rapidcontext/app/")) {
                 processApp(request, request.getPath(), baseUrl);
             } else if (isRoot) {
                 processApp(request, appId(), baseUrl);
             }
         }
     }
 
     /**
      * Processes an HTTP POST request.
      *
      * @param request        the request to process
      */
     protected void doPost(Request request) {
         if (request.matchPath("rapidcontext/download")) {
             processDownload(request);
         } else if (request.matchPath("rapidcontext/upload")) {
             processUpload(request);
         } else if (request.matchPath("rapidcontext/procedure/")) {
             processProcedure(request);
         } else {
             super.doPost(request);
         }
     }
 
     /**
      * Processes an HTML app launcher request. This method loads the
      * app launcher template from storage and replaces all template
      * variables with their corresponding search results and values.
      *
      * @param request        the request to process
      * @param appId          the app identifier to launch (or null)
      * @param baseUrl        the base URL for requests
      */
     protected void processApp(Request request, String appId, String baseUrl) {
         ApplicationContext  ctx = ApplicationContext.getInstance();
         Path                path;
         Binary              template;
         Object              obj;
         String              str;
 
         session(request, true);
         path = PATH_FILES.child("index.tmpl", false);
         obj = ctx.getStorage().load(path);
         if (obj instanceof Binary) {
             template = (Binary) obj;
             try {
                 str = processAppTemplate(template, baseUrl, appId);
                 request.sendText(Mime.HTML[0], str);
             } catch (IOException e) {
                 LOG.log(Level.WARNING, "failed to launch app: " + appId, e);
                 errorInternal(request, e.getMessage());
             }
         } else {
             str = "app template 'index.tmpl' not found";
             LOG.log(Level.WARNING, "app template 'index.tmpl' not found");
             errorInternal(request, str);
         }
     }
 
     /**
      * Processes an HTML template file for a app and returns the text.
      *
      * @param template       the template file to read
      * @param baseUrl        the base URL for requests
      * @param appId          the app identifier, or null for 'start'
      *
      * @return the processed contents of the HTML template file
      *
      * @throws IOException if the template file couldn't be read
      */
     protected String processAppTemplate(Binary template, String baseUrl, String appId)
     throws IOException {
 
         InputStreamReader  is;
         BufferedReader     reader;
         String             line;
         Array              files;
         String             ver = version();
         StringBuilder      res = new StringBuilder();
         String             str;
 
         is = new InputStreamReader(template.openStream(), "UTF-8");
         reader = new BufferedReader(is);
         while ((line = reader.readLine()) != null) {
             if (line.contains("%APP_ID%")) {
                 if (appId == null || appId.equals("")) {
                     res.append(line.replace("%APP_ID%", "null"));
                 } else {
                     res.append(line.replace("%APP_ID%", "'" + appId + "'"));
                 }
                 res.append("\n");
             } else if (line.contains("%BASE_URL%")) {
                 res.append(line.replace("%BASE_URL%", baseUrl));
                 res.append("\n");
             } else if (line.contains("%JS_FILES%")) {
                 files = resources(PATH_FILES, "js");
                 for (int i = 0; i < files.size(); i++) {
                     str = files.getString(i, "") + "?" + ver;
                     res.append(line.replace("%JS_FILES%", str));
                     res.append("\n");
                 }
             } else if (line.contains("%CSS_FILES%")) {
                 files = resources(PATH_FILES, "css");
                 for (int i = 0; i < files.size(); i++) {
                     str = files.getString(i, "") + "?" + ver;
                     res.append(line.replace("%CSS_FILES%", str));
                     res.append("\n");
                 }
             } else {
                 res.append(line);
                 res.append("\n");
             }
         }
         reader.close();
         return res.toString();
     }
 
     /**
      * Processes a file download request. This is used when file data
      * is POST:ed to the special download URL, which makes the handler
      * send the data back with the specified MIME type. Optionally, the
      * "Content-Disposition" header is also set to force the browser
      * "save as" dialog.
      *
      * @param request        the request to process
      */
     protected void processDownload(Request request) {
         String  name = request.getParameter("fileName", request.getPath());
         String  data = request.getParameter("fileData");
         String  mimeType = request.getParameter("mimeType", Mime.type(name));
         String  disp = "attachment";
 
         if (data == null) {
             errorBadRequest(request, "Missing 'fileData' parameter");
         } else {
             if (request.getParameter("download") != null) {
                 if (name.indexOf("/") >= 0) {
                     name = StringUtils.substringAfterLast(name, "/");
                 }
                 if (name.length() > 0) {
                     disp += "; filename=" + name;
                 }
                 request.setResponseHeader("Content-Disposition", disp);
             }
             request.sendText(mimeType, data);
         }
     }
 
     /**
      * Processes a file upload request. This is used when files are
      * POST:ed to the special upload URL.
      *
      * @param request        the request to process
      */
     protected void processUpload(Request request) {
         Session          session = (Session) Session.activeSession.get();
         FileItemStream   stream;
         String           fileId = request.getPath();
         String           fileName;
         File             file;
 
         if (session == null) {
             errorUnauthorized(request);
             return;
         }
         try {
             stream = request.getNextFile();
             if (stream == null) {
                 errorBadRequest(request, "Missing file data");
                 return;
             }
             fileName = stream.getName();
             if (fileName.lastIndexOf("/") >= 0) {
                 fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
             }
             if (fileName.lastIndexOf("\\") >= 0) {
                 fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
             }
             while (fileId != null && fileId.startsWith("/")) {
                 fileId = fileId.substring(1);
             }
             if (fileId == null || fileId.trim().length() == 0) {
                 fileId = fileName;
             }
            session.removeFile(fileId);
             file = FileUtil.tempFile(fileName);
             FileUtil.copy(stream.openStream(), file);
             session.addFile(fileId, file);
             request.sendText(Mime.TEXT[0], "Session file " + fileId + " uploaded");
         } catch (IOException e) {
             LOG.log(Level.WARNING, "failed to process file upload", e);
             errorBadRequest(request, e.getMessage());
         }
     }
 
     /**
      * Processes a procedure execution request. This is used to trigger
      * server-side procedure execution with the POST:ed data.
      *
      * @param request        the request to process
      */
     protected void processProcedure(Request request) {
         ApplicationContext  ctx = ApplicationContext.getInstance();
         Dict                res = new Dict();
         ArrayList           argList = new ArrayList();
         Object[]            args;
         String              source = "web [" + request.getRemoteAddr() + "]";
         StringBuffer        trace = null;
         String              str = "";
         Object              obj;
 
         session(request, true);
         res.set("data", null);
         res.set("trace", null);
         res.set("error", null);
         LOG.fine(source + ": init procedure call " + request.getPath());
         try {
             for (int i = 0; str != null; i++) {
                 str = request.getParameter("arg" + i, null);
                 if (str != null) {
                     LOG.fine(source + ": procedure arg " + (i + 1) + ":" + str);
                     argList.add(JsSerializer.unserialize(str));
                 }
             }
             args = argList.toArray();
             if (request.getParameter("trace", null) != null) {
                 trace = new StringBuffer();
             }
             obj = ctx.execute(request.getPath(), args, source, trace);
             res.set("data", obj);
             LOG.fine(source + ": done procedure call " + request.getPath());
         } catch (Exception e) {
             res.set("error", e.getMessage());
             LOG.fine(source + ": error in procedure call: " + e.getMessage());
         }
         if (trace != null) {
             res.set("trace", trace.toString());
         }
         request.sendText(Mime.JSON[0], JsSerializer.serialize(res));
     }
 }
