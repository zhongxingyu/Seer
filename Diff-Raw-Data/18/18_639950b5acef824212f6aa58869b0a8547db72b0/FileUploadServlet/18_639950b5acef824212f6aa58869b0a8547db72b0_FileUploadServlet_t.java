 package org.iplantc.de.server;
 
 import gwtupload.server.UploadAction;
 import gwtupload.server.exceptions.UploadActionException;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.http.HttpServletRequest;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.iplantc.de.client.views.panels.FileUploadDialogPanel;
 import org.iplantc.de.shared.services.MultiPartServiceWrapper;
 import org.iplantc.de.shared.services.ServiceCallWrapper;
 
 /**
  * A class to accept files from the client.
  * 
  * This class extends the UploadAction class provided by the GWT Upload library. The executeAction method
  * must be overridden for custom behavior.
  * 
  * @author sriram
  * 
  */
 @SuppressWarnings("nls")
 public class FileUploadServlet extends UploadAction {
     private static final long serialVersionUID = 1L;
 
     /**
      * The logger for error and informational messages.
      */
     private static Logger LOG = Logger.getLogger(FileUploadServlet.class);
 
     /**
      * Performs the necessary operations for an upload action.
      * 
      * @param request the HTTP request associated with the action.
      * @param sessionFiles the file associated with the action.
      * @return a string representing data in JSON format.
      * @throws UploadActionException if there is an issue invoking the dispatch to the servlet
      */
     @Override
     public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles)
             throws UploadActionException {
         String json = null;
         String idFolder = null;
         String user = null;
         String type = "AUTO";
 
         LOG.debug("Upload Action started.");
 
         List<FileItem> fileItems = new ArrayList<FileItem>();
         List<String> urlItems = new ArrayList<String>();
 
         for (FileItem item : sessionFiles) {
             if (item.isFormField()) {
                 String name = item.getFieldName();
                 String contents = new String(item.get());
 
                 if (name.equals(FileUploadDialogPanel.HDN_PARENT_ID_KEY)) {
                     idFolder = contents;
                 } else if (name.equals(FileUploadDialogPanel.HDN_USER_ID_KEY)) {
                     user = contents;
                 } else if (name.equals(FileUploadDialogPanel.FILE_TYPE)) {
                     type = contents;
                 } else if (name.equals(FileUploadDialogPanel.URL_FIELD)) {
                     urlItems.add(contents);
                 }
             } else if (validFileInfo(item)) {
                 fileItems.add(item);
             }
         }
 
         // do we have enough information to make a service call?
         if (sufficientData(user, idFolder, fileItems, urlItems)) {
             json = invokeService(request, idFolder, user, type, fileItems, urlItems);
         }
 
         // remove files from session. this avoids duplicate submissions
         removeSessionFileItems(request, false);
 
         LOG.debug("executeAction - JSON returned: " + json);
         return json;
     }
 
     /**
      * Handles the invocation of the file upload service.
      * 
      * @param request current HTTP request
      * @param idFolder the folder identifier for where the file will be related
      * @param user the name of the user account that is uploading the file
      * @param type the file type. It can be AUTO or CSVNAMELIST
      * @param fileItems a list of files to be uploaded
      * @param urlItems a list of urls to import
      * @return a string representing data in JSON format.
      * @throws UploadActionException if there is an issue invoking the dispatch to the servlet
      */
     private String invokeService(HttpServletRequest request, String idFolder, String user, String type,
             List<FileItem> fileItems, List<String> urlItems) throws UploadActionException {
         String filename;
         long fileLength;
         String mimeType;
         InputStream fileContents;
 
         JSONObject jsonResults = new JSONObject();
         JSONArray jsonResultsArray = new JSONArray();
 
         ServletConfig servletConfig = getServletConfig();
 
         // Call the file upload service for each file.
        DataApiServiceDispatcher dispatcherDataApi = new DataApiServiceDispatcher();
 
         try {
            dispatcherDataApi.init(servletConfig);
         } catch (Exception e) {
             LOG.error("DEServiceDispatcher::init - unable to init from getServletConfig()", e);
             e.printStackTrace();
 
             jsonResultsArray.add(buildJsonError(idFolder, type, "", e));
             jsonResults.put("results", jsonResultsArray);
 
             throw new UploadActionException(jsonResults.toString());
         }
 
        dispatcherDataApi.setRequest(request);
 
         for (FileItem item : fileItems) {
             filename = item.getName();
             fileLength = item.getSize();
             mimeType = item.getContentType();
 
             try {
                 fileContents = item.getInputStream();
             } catch (IOException e) {
                 LOG.error("invokeService - Exception while getting file input stream.", e);
                 e.printStackTrace();
 
                 // add the error to the results array, in case some files successfully uploaded already.
                 jsonResultsArray.add(buildJsonError(idFolder, type, filename, e));
                 jsonResults.put("results", jsonResultsArray);
 
                 throw new UploadActionException(jsonResults.toString());
             }
 
             MultiPartServiceWrapper wrapper = createServiceWrapper(idFolder, user, type, filename,
                     fileLength, mimeType, fileContents);
 
             // call the RESTful service and get the results.
             try {
                 LOG.debug("invokeService - Making service call.");
                String repsonse = dispatcherDataApi.getServiceData(wrapper);
 
                LOG.debug(repsonse);
                 jsonResultsArray.add(JSONObject.fromObject(repsonse));
             } catch (Exception e) {
                 LOG.error("invokeService - unable to upload file", e);
                 e.printStackTrace();
 
                 // add the error to the results array, in case some files successfully uploaded already.
                 jsonResultsArray.add(buildJsonError(idFolder, type, filename, e));
                 jsonResults.put("results", jsonResultsArray);
 
                 throw new UploadActionException(jsonResults.toString());
             }
         }
 
         // Call the URL import service for each URL.
         try {
             dispatcherDataApi.init(servletConfig);
         } catch (Exception e) {
             LOG.error("DataApiServiceDispatcher::init - unable to init from getServletConfig()", e);
             e.printStackTrace();
 
             jsonResultsArray.add(buildJsonError(idFolder, type, "", e));
             jsonResults.put("results", jsonResultsArray);
 
             throw new UploadActionException(jsonResults.toString());
         }
 
         dispatcherDataApi.setRequest(request);
         dispatcherDataApi.setForceJsonContentType(true);
 
         for (String url : urlItems) {
             filename = url.replaceAll(".*/", "");
 
             ServiceCallWrapper wrapper = createUrlServiceWrapper(idFolder, user, type, filename, url);
 
             // call the RESTful service and get the results.
             try {
                 LOG.debug("invokeService - Making service call.");
                 String repsonse = dispatcherDataApi.getServiceData(wrapper);
 
                 jsonResultsArray.add(JSONObject.fromObject(repsonse));
             } catch (Exception e) {
                 LOG.error("invokeService - unable to import URL", e);
                 e.printStackTrace();
 
                 // add the error to the results array, in case some files successfully uploaded already.
                 jsonResultsArray.add(buildJsonError(idFolder, type, url, e));
                 jsonResults.put("results", jsonResultsArray);
 
                 throw new UploadActionException(jsonResults.toString());
             }
         }
 
         jsonResults.put("results", jsonResultsArray);
 
         return jsonResults.toString();
     }
 
     private JSONObject buildJsonError(String idFolder, String type, String filename, Throwable e) {
         JSONObject ret = new JSONObject();
 
         ret.put("action", "file-upload");
         ret.put("status", "failure");
         ret.put("reason", e.getMessage());
         ret.put("id", idFolder + "/" + filename);
         ret.put("label", filename);
         ret.put("type", type);
 
         return ret;
     }
 
     /**
      * Constructs and configures a multi-part service wrapper.
      * 
      * @param idFolder the folder identifier for where the file will be related
      * @param user the name of the user account that is uploading the file
      * @param type the file type. It can be AUTO or CSVNAMELIST
      * @param filename the name of the file being uploaded
      * @param fileLength the length of the file being uploaded.
      * @param fileContents the content of the file
      * @return an instance of a multi-part service wrapper.
      */
     private MultiPartServiceWrapper createServiceWrapper(String idFolder, String user, String type,
             String filename, long fileLength, String mimeType, InputStream fileContents) {
         // TODO: Should there be a FileServices class that is wrapping all of
         // this like
         // FolderServices/etc.???
         String address = DiscoveryEnvironmentProperties.getUploadFileServiceBaseUrl();
 
         // build our wrapper
         MultiPartServiceWrapper wrapper = new MultiPartServiceWrapper(MultiPartServiceWrapper.Type.POST,
                 address);
         wrapper.addPart(new FileHTTPPart(fileContents, "file", filename, mimeType, fileLength));
         wrapper.addPart(idFolder, "dest");
         wrapper.addPart(type, "type");
 
         return wrapper;
     }
 
     private ServiceCallWrapper createUrlServiceWrapper(String idFolder, String user, String type,
             String filename, String url) {
         String address = DiscoveryEnvironmentProperties.getUrlImportServiceBaseUrl();
 
         JSONObject body = new JSONObject();
         body.put("dest", idFolder + "/" + filename);
         body.put("address", url);
 
         return new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address, body.toString());
     }
 
     /**
      * Determines if sufficient data is present to perform an action.
      * 
      * @param user the name of the user account that is uploading the file
      * @param idFolder the folder identifier for where the file will be related
      * @param fileItems a list of files to be uploaded
      * @param urlItems a list of urls to import
      * @return true if all argument have valid values; otherwise false
      */
     private boolean sufficientData(String user, String idFolder, List<FileItem> fileItems,
             List<String> urlItems) {
         boolean validFileItems = false;
         if (fileItems != null) {
             for (FileItem item : fileItems) {
                 if (validFileInfo(item)) {
                     validFileItems = true;
                     break;
                 }
             }
         }
 
         if (!validFileItems && urlItems != null) {
             for (String url : urlItems) {
                 if (!StringUtils.isEmpty(url)) {
                     validFileItems = true;
                     break;
                 }
             }
         }
 
         return validFileItems && !StringUtils.isEmpty(user) && !StringUtils.isEmpty(idFolder);
     }
 
     private boolean validFileInfo(FileItem item) {
         return item != null && !StringUtils.isEmpty(item.getName())
                 && !StringUtils.isEmpty(item.getContentType()) && item.getSize() > 0;
     }
 }
