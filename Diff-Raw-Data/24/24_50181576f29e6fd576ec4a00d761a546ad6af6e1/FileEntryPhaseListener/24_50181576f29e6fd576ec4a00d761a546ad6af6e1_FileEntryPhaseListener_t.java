 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.ace.component.fileentry;
 
 import org.icefaces.impl.context.DOMPartialViewContext;
 import org.icefaces.impl.util.CoreUtils;
 import org.icefaces.apache.commons.fileupload.FileItemStream;
 import org.icefaces.apache.commons.fileupload.FileItemIterator;
 import org.icefaces.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.icefaces.apache.commons.fileupload.util.Streams;
 import org.icefaces.util.EnvUtils;
 
 import javax.faces.application.ProjectStage;
 import javax.faces.event.PhaseEvent;
 import javax.faces.event.PhaseId;
 import javax.faces.event.PhaseListener;
 import javax.faces.event.FacesEvent;
 import javax.faces.context.PartialViewContext;
 import javax.faces.context.FacesContext;
 import javax.faces.component.visit.VisitContext;
 import javax.faces.component.visit.VisitCallback;
 import javax.faces.component.visit.VisitHint;
 import javax.faces.component.visit.VisitResult;
 import javax.faces.component.UIComponent;
 import javax.faces.application.FacesMessage;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 import java.lang.reflect.Constructor;
 import java.util.*;
 import java.io.InputStream;
 import java.io.File;
 import java.io.OutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class FileEntryPhaseListener implements PhaseListener {
     private static Logger log = Logger.getLogger(FileEntryPhaseListener.class.getName());
 
     public void afterPhase(PhaseEvent phaseEvent) {
 //        System.out.println("FileEntryPhaseListener.afterPhase()   " + phaseEvent.getPhaseId());
 //        System.out.println("FileEntryPhaseListener.afterPhase()     renderResponse  : " + phaseEvent.getFacesContext().getRenderResponse());
 //        System.out.println("FileEntryPhaseListener.afterPhase()     responseComplete: " + phaseEvent.getFacesContext().getResponseComplete());
         if (phaseEvent.getPhaseId().equals(PhaseId.APPLY_REQUEST_VALUES)) {
 //System.out.println("FileEntryPhaseListener.afterPhase()  FileEntry.removeResults()");
             FileEntry.removeResults(phaseEvent.getFacesContext());
         }
     }
 
     public void beforePhase(PhaseEvent phaseEvent) {
 //        System.out.println("FileEntryPhaseListener.beforePhase()  " + phaseEvent.getPhaseId());
         if (!phaseEvent.getPhaseId().equals(PhaseId.RESTORE_VIEW)) {
             // Don't evaluate any of the fields on FacesContext or
             // PartialViewContext until after we've swapped in our
             // HttpServletRequest object
 //            System.out.println("FileEntryPhaseListener.beforePhase()    postback: " + phaseEvent.getFacesContext().isPostback());
 //            System.out.println("FileEntryPhaseListener.beforePhase()    partialViewContext.class: " + phaseEvent.getFacesContext().getPartialViewContext().getClass().getName());
 //            System.out.println("FileEntryPhaseListener.beforePhase()    partialViewContext.isAjaxRequest: " + phaseEvent.getFacesContext().getPartialViewContext().isAjaxRequest());
 //            System.out.println("FileEntryPhaseListener.beforePhase()    partialViewContext.isExecuteAll : " + phaseEvent.getFacesContext().getPartialViewContext().isExecuteAll());
 //            System.out.println("FileEntryPhaseListener.beforePhase()    partialViewContext.isRenderAll  : " + phaseEvent.getFacesContext().getPartialViewContext().isRenderAll());
         }
         
         if (phaseEvent.getPhaseId().equals(PhaseId.RENDER_RESPONSE)) {
             final Map<String, FacesEvent> clientId2FacesEvent = FileEntry.
                 removeEventsForPreRender(phaseEvent.getFacesContext());
             if (clientId2FacesEvent != null) {
                 Set<String> clientIds = clientId2FacesEvent.keySet();
                 EnumSet<VisitHint> hints = EnumSet.of(
                     VisitHint.SKIP_UNRENDERED);
                 VisitContext visitContext = VisitContext.createVisitContext(
                     phaseEvent.getFacesContext(), clientIds, hints);
                 VisitCallback vcall = new VisitCallback() {
                     public VisitResult visit(VisitContext visitContext,
                                              UIComponent uiComponent) {
                         FacesContext facesContext = visitContext.getFacesContext();
                         String clientId = uiComponent.getClientId(facesContext);
                         FacesEvent event = clientId2FacesEvent.get(clientId);
 //System.out.println("FileEntryPhaseListener  pre-Render  clientId: " + clientId + "  event: " + event);
                         if (event != null) {
                             uiComponent.broadcast(event);
                         }
                         return VisitResult.REJECT;
                     }
                 };
                 phaseEvent.getFacesContext().getViewRoot().visitTree(
                     visitContext, vcall);
             }
             return;
         }
         
         if (!phaseEvent.getPhaseId().equals(PhaseId.RESTORE_VIEW))
             return;
 
         Object requestObject = phaseEvent.getFacesContext().getExternalContext().getRequest();
         HttpServletRequest request = EnvUtils.getSafeRequest(phaseEvent.getFacesContext());
         boolean isPortlet = EnvUtils.instanceofPortletRequest(requestObject);
         boolean isMultipart = ServletFileUpload.isMultipartContent(request);
 //        System.out.println("FileEntryPhaseListener.beforePhase()  isMultipart: " + isMultipart);
 //        System.out.println("FileEntryPhaseListener.beforePhase()  isPortlet: " + isPortlet);
         if (isMultipart) {
             final ServletFileUpload uploader = new ServletFileUpload();
             Map<String, FileEntryResults> clientId2Results =
                     new HashMap<String, FileEntryResults>(6);
             ProgressListenerResourcePusher progressListenerResourcePusher =
                     new ProgressListenerResourcePusher(clientId2Results);
             uploader.setProgressListener(progressListenerResourcePusher);
             Map<String, FileEntryCallback> clientId2Callbacks =
                     new HashMap<String, FileEntryCallback>(6);
             Map<String, List<String>> parameterListMap =
                     new HashMap<String, List<String>>();
             byte[] buffer = new byte[16*1024];
             try {
                 String reqCharEnc = request.getCharacterEncoding();
                 FileItemIterator iter = uploader.getItemIterator(request);
                 while (iter.hasNext()) {
                     FileItemStream item = iter.next();
                     if (item.isFormField()) {
                         String name = item.getFieldName();
                         String value;
                         if(null != reqCharEnc){
                             value = Streams.asString(item.openStream(), reqCharEnc);
                         } else {
                             value = Streams.asString(item.openStream());
                         }
 //System.out.println("Field:  " + name + "  ->  " + value);
                         
                         List<String> parameterList = parameterListMap.get(name);
                         if (parameterList == null) {
                             parameterList = new ArrayList<String>(6);
                             parameterListMap.put(name, parameterList);
                         }
                         parameterList.add(value);
                     } else {
                         uploadFile(phaseEvent.getFacesContext(), item,
                                 clientId2Results, clientId2Callbacks,
                                 progressListenerResourcePusher, buffer);
                     }
                 }
             }
             catch(Exception e) {
                 FacesMessage fm = FileEntryStatuses.PROBLEM_READING_MULTIPART.
                     getFacesMessage(phaseEvent.getFacesContext(), null, null);
                 phaseEvent.getFacesContext().addMessage(null, fm);
                 System.out.println("Problem: " + e);
                 e.printStackTrace();
             }
             FileEntry.storeResultsForLaterInLifecycle(phaseEvent.getFacesContext(), clientId2Results);
             progressListenerResourcePusher.clear();
             clientId2Callbacks.clear();
             Arrays.fill(buffer, (byte) 0);
             buffer = null;
             
             // Map<String, List<String>> parameterListMap = new HashMap<String, List<String>>();
             Map<String, String[]> parameterMap = new HashMap<String, String[]>(
                 ((parameterListMap.size() > 0) ? parameterListMap.size() : 1) );            
             boolean ajaxResponse = false;
             for(String key : parameterListMap.keySet()) {  
                 if (key.equals("ice.fileEntry.ajaxResponse")) ajaxResponse = true;
                 List<String> parameterList = parameterListMap.get(key);
                 String[] values = new String[parameterList.size()];
                 values = parameterList.toArray(values);
                 parameterMap.put(key, values);
             }
 //System.out.println("FileEntryPhaseListener.beforePhase()  Our ajaxResponse: " + ajaxResponse);
 
 //            System.out.println("FileEntryPhaseListener.beforePhase()  parameterMap    : " + parameterMap);
             if (!parameterMap.isEmpty()) {
                 Object wrapper = null;
                 if (isPortlet) {
                     wrapper = getPortletRequestWrapper(requestObject, parameterMap);
                 } else {
                     wrapper = new FileUploadRequestWrapper((HttpServletRequest) requestObject, parameterMap);
                 }
                 phaseEvent.getFacesContext().getExternalContext().setRequest(wrapper);
 
                 if (ajaxResponse) {
                     PartialViewContext pvc = phaseEvent.getFacesContext().getPartialViewContext();
                     if (pvc instanceof DOMPartialViewContext)
                         ((DOMPartialViewContext) pvc).setAjaxRequest(true);
                     pvc.setPartialRequest(true);
                 }
             }
             // Apparently not necessary, as long as we don't call
             // FacesContext.isPostback() before this point
             //phaseEvent.getFacesContext().getAttributes().remove(
             //   "com.sun.faces.context.FacesContextImpl_POST_BACK");
             
 //            System.out.println("FileEntryPhaseListener.beforePhase()  old    : " + request);
 //            System.out.println("FileEntryPhaseListener.beforePhase()  wrapper: " + wrapper);
 //            System.out.println("FileEntryPhaseListener.beforePhase()  set    : " + phaseEvent.getFacesContext().getExternalContext().getRequest());
         }        
         else if (false && phaseEvent.getFacesContext().isPostback()) {
             //TODO
             // This is only for testing with non-file-upload, regular postback,
             // to have it respond with an ajax response. This should typically
             // be disabled
 //            System.out.println("FileEntryPhaseListener.beforePhase()  Temporary test of adding Faces-Request HTTP header");
             Object wrapper = null;
             if(isPortlet){
                 wrapper = getPortletRequestWrapper(requestObject,null);
             } else {
                 wrapper = new FileUploadRequestWrapper((HttpServletRequest) requestObject, null);
             }
             phaseEvent.getFacesContext().getExternalContext().setRequest(wrapper);
             PartialViewContext pvc = phaseEvent.getFacesContext().getPartialViewContext();
             if (pvc instanceof DOMPartialViewContext)
                 ((DOMPartialViewContext) pvc).setAjaxRequest(true);
             pvc.setPartialRequest(true);
 //            System.out.println("FileEntryPhaseListener.beforePhase()  partialViewContext.isAjaxRequest: " + phaseEvent.getFacesContext().getPartialViewContext().isAjaxRequest());
             
 //            System.out.println("FileEntryPhaseListener.beforePhase()  old    : " + request);
 //            System.out.println("FileEntryPhaseListener.beforePhase()  wrapper: " + wrapper);
 //            System.out.println("FileEntryPhaseListener.beforePhase()  set    : " + phaseEvent.getFacesContext().getExternalContext().getRequest());
         }
 
 //        request = (HttpServletRequest) phaseEvent.getFacesContext().getExternalContext().getRequest();
         
         /*
         System.out.println("About to list headers");
         Enumeration<String> headerNames = request.getHeaderNames();
         while (headerNames.hasMoreElements()) {
             String name = headerNames.nextElement();
             String value = request.getHeader(name);
             System.out.println("  HtServReq Header  " + name + " -> " + value);
         }
         Map<String,String> rhm = phaseEvent.getFacesContext().getExternalContext().getRequestHeaderMap();
         for(String key : rhm.keySet()) {
             System.out.println("  RequestHeaderMap  " + key + " -> " + rhm.get(key));
         }
         */
 
         /*
         System.out.println("About to list parameters");
         Enumeration<String> parameterNames = request.getParameterNames();
         while (parameterNames.hasMoreElements()) {
             String name = parameterNames.nextElement();
             String value = request.getParameter(name);
             System.out.println("  HtServReq Parameter  " + name + " -> " + value);
         }
         Map<String,String> rpm = phaseEvent.getFacesContext().getExternalContext().getRequestParameterMap();
         for(String key : rpm.keySet()) {
             System.out.println("  RequestParameterMap  " + key + " -> " + rpm.get(key));
         }
         */
     }
 
     private static Object getPortletRequestWrapper(Object requestObject, Map map){
         Object wrapper = null;
         try {
             Class wrapperClass = Class.forName("org.icefaces.ace.component.fileentry.FileUploadPortletRequestWrapper");
             Class paramClasses[] = new Class[2];
             paramClasses[0] = Object.class;
             paramClasses[1] = Map.class;
             Constructor constructor = wrapperClass.getConstructor(paramClasses);
             wrapper = constructor.newInstance(requestObject,map);
         } catch (Exception e) {
             throw new RuntimeException("Problem getting FileUploadPortletRequestWrapper", e);
         }
         return wrapper;
     }
     
     private static void uploadFile(
             FacesContext facesContext,
             FileItemStream item,
             Map<String, FileEntryResults> clientId2Results,
             Map<String, FileEntryCallback> clientId2Callbacks,
             ProgressListenerResourcePusher progressListenerResourcePusher,
             byte[] buffer) {
         FileEntryResults results = null;
         FileEntryCallback callback = null;
         FileEntryResults.FileInfo fileInfo = null;
         FileEntryConfig config = null;
 
         File file = null;
         long fileSizeRead = 0L;
         FileEntryStatus status = FileEntryStatuses.UPLOADING;
         
 //System.out.println("vvvvvvvvvvvvvvv");
         try {
             String name = item.getName();
             String fieldName = item.getFieldName();
             String contentType = item.getContentType();
 //System.out.println("File  name: " + name);
 //System.out.println("File  fieldName: " + fieldName);
 //System.out.println("File  contentType: " + contentType);
 
             // IE gives us the whole path on the client, but we just
             //  want the client end file name, not the path
             String fileName = null;
             if (name != null && name.length() > 0) {
                 fileName = trimInternetExplorerPath(name);
             }
 //System.out.println("File    IE adjusted fileName: " + fileName);
             
             // When no file name is given, that means the user did
             // not upload a file
             if (fileName != null && fileName.length() > 0) {
                 String identifier = fieldName;
                 config = FileEntry.retrieveConfigFromPreviousLifecycle(facesContext, identifier);
                 if (config != null) {
                     // config being null might be indicative of a non-ICEfaces' file upload component in the form
     //System.out.println("File    config: " + config);
 
                     results = clientId2Results.get(config.getClientId());
                     if (results == null) {
                         results = new FileEntryResults(config.isViaCallback());
                         clientId2Results.put(config.getClientId(), results);
                     }
     //System.out.println("File    results: " + results);
 
                     fileInfo = new FileEntryResults.FileInfo();
                     fileInfo.begin(fileName, contentType);
 
                     progressListenerResourcePusher.setPushResourcePathAndGroupName(
                             facesContext, config.getProgressResourcePath(),
                             config.getProgressGroupName());
 
                     if (config.isViaCallback()) {
                         callback = clientId2Callbacks.get(config.getClientId());
                         if (callback == null) {
                             try {
                                 callback = evaluateCallback(facesContext, config);
                             } catch(javax.el.ELException e) {
                                 status = FileEntryStatuses.PROBLEM_WITH_CALLBACK;
                                 throw e;
                             }
                         }
                     }
     //System.out.println("File    callback: " + callback);
 
                     long availableTotalSize = results.getAvailableTotalSize(config.getMaxTotalSize());
     //System.out.println("File    availableTotalSize: " + availableTotalSize);
                     long availableFileSize = config.getMaxFileSize();
     //System.out.println("File    availableFileSize: " + availableFileSize);
                     int maxFileCount = config.getMaxFileCount();
     //System.out.println("File    maxFileCount: " + maxFileCount);
                     if (results.getFiles().size() >= maxFileCount) {
                         status = FileEntryStatuses.MAX_FILE_COUNT_EXCEEDED;
                         fileInfo.prefail(status);
                         InputStream in = item.openStream();
                         while (in.read(buffer) >= 0) {}
                         if (callback != null) {
                             try {
                                 callback.begin(fileInfo);
                                 callback.end(fileInfo);
                             } catch(RuntimeException e) {
                                 status = FileEntryStatuses.PROBLEM_WITH_CALLBACK;
                                 handleCallbackException(
                                         facesContext, config.getClientId(), e);
                                 throw e;
                             }
                         }
                     }
                     else {
                         OutputStream output = null;
                         if (callback != null) {
                             try {
                                 callback.begin(fileInfo);
                             } catch(RuntimeException e) {
                                 status = FileEntryStatuses.PROBLEM_WITH_CALLBACK;
                                 handleCallbackException(
                                         facesContext, config.getClientId(), e);
                                 throw e;
                             }
                         }
                         else {
                             String folder = calculateFolder(facesContext, config);
                             file = makeFile(config, folder, fileName);
     //System.out.println("File    file: " + file);
                             output = new FileOutputStream(file);
                         }
 
                         InputStream in = item.openStream();
                         try {
                             boolean overQuota = false;
                             while (true) {
                                 int read = in.read(buffer);
                                 if (read < 0) {
                                     break;
                                 }
                                 fileSizeRead += read;
                                 if (!overQuota) {
                                     if (fileSizeRead > availableFileSize) {
                                         overQuota = true;
                                         status = FileEntryStatuses.MAX_FILE_SIZE_EXCEEDED;
                                     }
                                     else if (fileSizeRead > availableTotalSize) {
                                         overQuota = true;
                                         status = FileEntryStatuses.MAX_TOTAL_SIZE_EXCEEDED;
                                     }
                                     if (!overQuota) {
                                         if (callback != null) {
                                             try {
                                                 callback.write(buffer, 0, read);
                                             } catch(RuntimeException e) {
                                                 status = FileEntryStatuses.PROBLEM_WITH_CALLBACK;
                                                 handleCallbackException(
                                                         facesContext, config.getClientId(), e);
                                                 throw e;
                                             }
                                         }
                                         else if (output != null) {
                                             output.write(buffer, 0, read);
                                         }
                                     }
                                 }
                             }
     //System.out.println("File    fileSizeRead: " + fileSizeRead);
     //if (overQuota)
     //  System.out.println("File    overQuota  status: " + status);
                             if (status == FileEntryStatuses.UPLOADING) {
                                 status = FileEntryStatuses.SUCCESS;
                             }
                         }
                         finally {
                             if (output != null) {
                                 output.flush();
                                 output.close();
                             }
                         }
                     }
                 }
             }
             else { // If no file name specified
 //System.out.println("File    UNSPECIFIED_NAME");
                 status = FileEntryStatuses.UNSPECIFIED_NAME;
                 InputStream in = item.openStream();
                 while (in.read(buffer) >= 0) {}
             }
         }
         catch(Exception e) {
 //System.out.println("File    Exception: " + e);
             if (status == FileEntryStatuses.UPLOADING ||
                     status == FileEntryStatuses.SUCCESS) {
                 status = FileEntryStatuses.INVALID;
             }
             if (facesContext.isProjectStage(ProjectStage.Development)) {
                 log.log(Level.SEVERE, "Problem processing uploaded file", e);
             }
         }
 
         if (file != null && !status.isSuccess()) {
 //System.out.println("File    Unsuccessful file being deleted");
             file.delete();
             file = null;
         }
         
 //System.out.println("File    Ending  status: " + status);
         if (results != null && fileInfo != null) {
             fileInfo.finish(file, fileSizeRead, status);
             results.addCompletedFile(fileInfo);
             if (callback != null) {
                 try {
                     callback.end(fileInfo);
                 } catch(RuntimeException e) {
                     status = FileEntryStatuses.PROBLEM_WITH_CALLBACK;
                     fileInfo.postfail(status);
                     handleCallbackException(
                             facesContext, config.getClientId(), e);
                 }
             }
 //System.out.println("File    Added completed file");
         }
 //System.out.println("^^^^^^^^^^^^^^^");
     }
 
     protected static FileEntryCallback evaluateCallback(
             FacesContext facesContext, FileEntryConfig config) {
         String callbackEL = config.getCallbackEL();
 //System.out.println("File    evaluateCallback()  callbackEL: " + callbackEL);
         FileEntryCallback callback = null;
         try {
             callback = facesContext.getApplication().evaluateExpressionGet(
                     facesContext, callbackEL, FileEntryCallback.class);
 //System.out.println("File    evaluateCallback()  callback: " + callback);
             if (callbackEL != null && callback == null &&
                     facesContext.isProjectStage(ProjectStage.Development)) {
                 log.warning("For the fileEntry component with the clientId " +
                         "of '" + config.getClientId() + "', the callback " +
                         "property is set but resolves to null. This might " +
                         "indicate an application error. The uploaded file " +
                         "will be saved to the server file-system.");
             }
         } catch(javax.el.ELException e) {
             if (facesContext.isProjectStage(ProjectStage.Development)) {
                 log.log(Level.SEVERE, "For the fileEntry component with the " +
                         "clientId of '" + config.getClientId() + "'", e);
             }
             throw e;
         }
         return callback;
     }
 
     protected static void handleCallbackException(FacesContext facesContext,
             String clientId, RuntimeException e) {
         if (facesContext.isProjectStage(ProjectStage.Development)) {
             log.log(Level.SEVERE, "An exception was thrown by the callback " +
                     "for the fileEntry component with clientId of '" +
                     clientId + "'", e);
         }
     }
 
     protected static String calculateFolder(
             FacesContext facesContext, FileEntryConfig config) {
         String folder = null;
         // absolutePath takes precedence over relativePath
         if (config.getAbsolutePath() != null && config.getAbsolutePath().length() > 0) {
             folder = config.getAbsolutePath();
 //System.out.println("File    Using absolutePath: " + folder);
         }
         else {
             folder = CoreUtils.getRealPath(facesContext, config.getRelativePath());
 //System.out.println("File    Using relativePath: " + folder);
         }
         if (folder == null) {
 //System.out.println("File    folder is null");
             folder = "";
         }
 
         if (config.isUseSessionSubdir()) {
             String sessionId = CoreUtils.getSessionId(facesContext);
             if (sessionId != null && sessionId.length() > 0) {
                 String FILE_SEPARATOR = System.getProperty("file.separator");
                 if (folder != null && folder.trim().length() > 0) {
                     folder = folder + FILE_SEPARATOR;
                 }
                 folder = folder + sessionId;
 //System.out.println("File    Using sessionSubdir: " + folder);
             }
         }
         return folder;
     }
     
     protected static File makeFile(
             FileEntryConfig config, String folder, String fileName)
             throws IOException {
         File file = null;
         File folderFile = new File(folder);
         if (!folderFile.exists())
             folderFile.mkdirs();
         if (config.isUseOriginalFilename()) {
             file = new File(folderFile, fileName);
 //System.out.println("File    original  file: " + file);
         }
         else {
             file = File.createTempFile("ice_file_", null, folderFile);
 //System.out.println("File    sanitise  file: " + file);
         }
         return file;
     }
 
     protected static String trimInternetExplorerPath(String path) {
         String[] seps = new String[] {File.separator, "/", "\\"};
         for (String sep : seps) {
             String ret = afterLast(path, sep);
             if (!path.equals(ret)) {
                 return ret;
             }
         }
         return path;
     }
 
     protected static String afterLast(String str, String seek) {
         int index = str.lastIndexOf(seek);
         if (index >= 0) {
             return str.substring(index + seek.length());
         }
         return str;
     }
 
     public PhaseId getPhaseId() {
         return PhaseId.ANY_PHASE;
     }
     
     
     static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
 
     private static class FileUploadRequestWrapper extends HttpServletRequestWrapper {
         private static final String FACES_REQUEST = "Faces-Request";
         private static final String PARTIAL_AJAX = "partial/ajax";
         private static final String CONTENT_TYPE = "content-type";
 
         private Map<String,String[]> parameterMap;
 
         public FileUploadRequestWrapper(HttpServletRequest httpServletRequest,
                                         Map<String,String[]> parameterMap)
         {
             super(httpServletRequest);
             this.parameterMap = parameterMap;
         }
 
         public String getHeader(String name) {
 //System.out.println("getHeader()  " + name);
             if (name != null) {
                 if (name.equals(FACES_REQUEST)) {
 //System.out.println("getHeader()  FACES_REQUEST -> PARTIAL_AJAX");
 //Thread.dumpStack();
                     return PARTIAL_AJAX;
                 }
                 else if (name.equals(CONTENT_TYPE)) {
                     return APPLICATION_FORM_URLENCODED;
                 }
             }
             return super.getHeader(name);
         }
 
         public java.util.Enumeration<String> getHeaders(java.lang.String name) {
 //System.out.println("getHeaders()  " + name);
             if (name != null) {
                 if (name.equals(FACES_REQUEST)) {
                     Vector<String> list = new Vector<String>(1);
                     list.add(PARTIAL_AJAX);
 //System.out.println("getHeader()  FACES_REQUEST -> PARTIAL_AJAX");
                     return list.elements();
                 }
                 else if (name.equals(CONTENT_TYPE)) {
                     Vector<String> list = new Vector<String>(1);
                     list.add(APPLICATION_FORM_URLENCODED);
                     return list.elements();
                 }
             }
             return super.getHeaders(name);
         }
 
         public int getIntHeader(java.lang.String name) {
             if (name != null) {
                 if (name.equals(FACES_REQUEST))
                     throw new NumberFormatException("Can not convert " + FACES_REQUEST + " to integer");
                 else if (name.equals(CONTENT_TYPE))
                     throw new NumberFormatException("Can not convert " + CONTENT_TYPE + " to integer");
             }
             return super.getIntHeader(name);
         }
 
         public java.util.Enumeration<String> getHeaderNames() {
             java.util.Vector<String> list = new java.util.Vector<String>();
             java.util.Enumeration<String> names = super.getHeaderNames();
             while (names != null && names.hasMoreElements()) {
                 list.add(names.nextElement());
             }
             if (!list.contains(FACES_REQUEST))
                 list.add(FACES_REQUEST);
             if (!list.contains(CONTENT_TYPE))
                 list.add(CONTENT_TYPE);
             return list.elements();
         }
         
         
         // Returns a java.util.Map of the parameters of this request.
         public Map<String, String[]> getParameterMap() {
             if (parameterMap != null) {
                 return Collections.unmodifiableMap(parameterMap);
             }
             return super.getParameterMap();
         }
 
         // Returns an Enumeration of String objects containing the names of
         // the parameters contained in this request.
         public Enumeration<String> getParameterNames() {
             if (parameterMap != null) {
                 Vector<String> keyVec = new Vector<String>(parameterMap.keySet());
                 return keyVec.elements();
             }
             return super.getParameterNames();
         }
 
         // Returns the value of a request parameter as a String, or null if
         // the parameter does not exist.
         public String getParameter(String name) {
             if (parameterMap != null) {
                 if (!parameterMap.containsKey(name)) {
                     return null;
                 }
                 String[] values = parameterMap.get(name);
                 if (values != null && values.length >= 1) {
                     return values[0];
                 }
                 return null; // Or "", since the key does exist?
             }
             return super.getParameter(name);
         }
 
         // Returns an array of String objects containing all of the values the
         // given request parameter has, or null if the parameter does not exist.
         public String[] getParameterValues(String name) {
             if (parameterMap != null) {
                 if (!parameterMap.containsKey(name)) {
                     return null;
                 }
                 return parameterMap.get(name);
             }
             return super.getParameterValues(name);
         }
 
         public String getContentType() {
             return APPLICATION_FORM_URLENCODED;
         }
     }
 
 }
