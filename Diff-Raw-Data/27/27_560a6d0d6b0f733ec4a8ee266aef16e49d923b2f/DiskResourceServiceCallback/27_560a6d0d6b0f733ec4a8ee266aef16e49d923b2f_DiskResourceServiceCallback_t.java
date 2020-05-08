 package org.iplantc.de.client.services;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.utils.DataUtils;
 
 import com.extjs.gxt.ui.client.widget.Component;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * A common callback for disk resource service calls that implements a common onFailure method that will
  * parse JSON error messages from the given Throwable::getMessage method to display to the user.
  * 
  * @author psarando
  * 
  */
 public abstract class DiskResourceServiceCallback implements AsyncCallback<String> {
 
     public static final String STATUS_FAILURE = "failure"; //$NON-NLS-1$
     public static final String STATUS = "status"; //$NON-NLS-1$
     public static final String REASON = "reason"; //$NON-NLS-1$
    public static final String MESSAGE = "message"; //$NON-NLS-1$
     public static final String PATHS = "paths"; //$NON-NLS-1$
     public static final String PATH = "path"; //$NON-NLS-1$
     public static final String ERROR_CODE = "error_code"; //$NON-NLS-1$
 
     protected Component maskedCaller;
 
     protected enum ErrorCode {
         ERR_DOES_NOT_EXIST,
         ERR_EXISTS,
         ERR_NOT_WRITEABLE,
         ERR_NOT_READABLE,
         ERR_WRITEABLE,
         ERR_READABLE,
         ERR_NOT_A_USER,
         ERR_NOT_A_FILE,
         ERR_NOT_A_FOLDER,
         ERR_IS_A_FILE,
         ERR_IS_A_FOLDER,
         ERR_INVALID_JSON,
         ERR_BAD_OR_MISSING_FIELD,
         ERR_NOT_AUTHORIZED,
         ERR_MISSING_QUERY_PARAMETER,
         ERR_INCOMPLETE_DELETION,
         ERR_INCOMPLETE_MOVE,
         ERR_INCOMPLETE_RENAME
     }
 
     public void setMaskedCaller(Component maskedCaller) {
         this.maskedCaller = maskedCaller;
     }
 
     protected void unmaskCaller() {
         if (maskedCaller != null) {
             maskedCaller.unmask();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void onFailure(Throwable caught) {
         unmaskCaller();
 
         String errMsg = getErrorMessageDefault();
 
         JSONObject jsonError = parseJsonError(caught);
         if (jsonError != null) {
            // The exception message is a JSON object.
             String status = JsonUtil.getString(jsonError, STATUS);
             String reason = JsonUtil.getString(jsonError, REASON);
             String errCode = JsonUtil.getString(jsonError, ERROR_CODE);
 
            if (reason.isEmpty()) {
                // Additional check for error message.
                reason = JsonUtil.getString(jsonError, MESSAGE);
            }

            // Build a custom exception message from the parsed JSON object.
            if (!errCode.isEmpty() || !reason.isEmpty() || !status.isEmpty()) {
                // Lookup the error code
                ErrorCode code = null;
                try {
                    code = ErrorCode.valueOf(errCode);
                } catch (Exception ignore) {
                    // intentionally ignore
                }

                if (code != null) {
                    // Get a custom user error message for the error code and parsed JSON object.
                    errMsg = getErrorMessageByCode(code, jsonError);
                }

                 caught = new Exception(I18N.ERROR.dataServiceErrorReport(status, errCode, reason),
                         caught);
             }
         }
 
         ErrorHandler.post(errMsg, caught);
     }
 
     /**
      * Gets a default error message to display to the user on failure, if no error code could be parsed
      * from the service response.
      * 
      * @return Default error message to display to the user.
      */
     protected abstract String getErrorMessageDefault();
 
     /**
      * Builds an error message to display to the user on failure, containing a message parsed from the
      * given error code, and potentially additional details parsed from the JSON error object.
      * 
      * @param code The error code of the failure, as returned by the service call.
      * @param jsonError The parsed JSON object error message, as returned by the service call, which may
      *            contain additional details.
      * @return An error message containing the given reason to display to the user.
      */
     protected abstract String getErrorMessageByCode(ErrorCode code, JSONObject jsonError);
 
     /**
      * Returns an internationalized string parsed from the error code and optional disk resource name(s).
      * 
      * @param code
      * @param resourceNames
      * @return localized text for the given code and resourceNames, or null if the code is unrecognized.
      */
     protected String getErrorMessage(ErrorCode code, String resourceNames) {
         switch (code) {
             case ERR_DOES_NOT_EXIST:
                 return I18N.ERROR.diskResourceDoesNotExist(resourceNames);
             case ERR_EXISTS:
                 return I18N.ERROR.diskResourceExists(resourceNames);
             case ERR_NOT_WRITEABLE:
                 return I18N.ERROR.diskResourceNotWriteable(resourceNames);
             case ERR_NOT_READABLE:
                 return I18N.ERROR.diskResourceNotReadable(resourceNames);
             case ERR_WRITEABLE:
                 return I18N.ERROR.diskResourceWriteable(resourceNames);
             case ERR_READABLE:
                 return I18N.ERROR.diskResourceReadable(resourceNames);
             case ERR_NOT_A_USER:
                 return I18N.ERROR.dataErrorNotAUser();
             case ERR_NOT_A_FILE:
                 return I18N.ERROR.diskResourceNotAFile(resourceNames);
             case ERR_NOT_A_FOLDER:
                 return I18N.ERROR.diskResourceNotAFolder(resourceNames);
             case ERR_IS_A_FILE:
                 return I18N.ERROR.diskResourceIsAFile(resourceNames);
             case ERR_IS_A_FOLDER:
                 return I18N.ERROR.diskResourceIsAFolder(resourceNames);
             case ERR_INVALID_JSON:
                 return I18N.ERROR.dataErrorInvalidJson();
             case ERR_BAD_OR_MISSING_FIELD:
                 return I18N.ERROR.dataErrorBadOrMissingField();
             case ERR_NOT_AUTHORIZED:
                 return I18N.ERROR.dataErrorNotAuthorized();
             case ERR_MISSING_QUERY_PARAMETER:
                 return I18N.ERROR.dataErrorMissingQueryParameter();
             case ERR_INCOMPLETE_DELETION:
                 return I18N.ERROR.diskResourceIncompleteDeletion();
             case ERR_INCOMPLETE_MOVE:
                 return I18N.ERROR.diskResourceIncompleteMove();
             case ERR_INCOMPLETE_RENAME:
                 return I18N.ERROR.diskResourceIncompleteRename();
         }
 
         return null;
     }
 
     /**
      * Returns an internationalized error message, specifically for folders, parsed from the error code
      * and optional folder name(s). Returns a getErrorMessage string if no folder specific message is
      * defined.
      * 
      * @param code
      * @param folderNames
      * @return localized text for the given code and folderNames, or null if the code is unrecognized.
      */
     protected String getErrorMessageForFolders(ErrorCode code, String folderNames) {
         switch (code) {
             case ERR_DOES_NOT_EXIST:
                 return I18N.ERROR.folderDoesNotExist(folderNames);
             case ERR_EXISTS:
                 return I18N.ERROR.folderExists(folderNames);
             case ERR_NOT_WRITEABLE:
                 return I18N.ERROR.folderNotWriteable(folderNames);
             case ERR_NOT_READABLE:
                 return I18N.ERROR.folderNotReadable(folderNames);
             case ERR_WRITEABLE:
                 return I18N.ERROR.folderWriteable(folderNames);
             case ERR_READABLE:
                 return I18N.ERROR.folderReadable(folderNames);
             default:
                 return getErrorMessage(code, folderNames);
         }
     }
 
     /**
      * Returns an internationalized error message, specifically for files, parsed from the error code and
      * optional file name(s). Returns a getErrorMessage string if no file specific message is defined.
      * 
      * @param code
      * @param fileNames
      * @return localized text for the given code and fileNames, or null if the code is unrecognized.
      */
     protected String getErrorMessageForFiles(ErrorCode code, String fileNames) {
         switch (code) {
             case ERR_DOES_NOT_EXIST:
                 return I18N.ERROR.fileDoesNotExist(fileNames);
             case ERR_EXISTS:
                 return I18N.ERROR.fileExists(fileNames);
             case ERR_NOT_WRITEABLE:
                 return I18N.ERROR.fileNotWriteable(fileNames);
             case ERR_NOT_READABLE:
                 return I18N.ERROR.fileNotReadable(fileNames);
             case ERR_WRITEABLE:
                 return I18N.ERROR.fileWriteable(fileNames);
             case ERR_READABLE:
                 return I18N.ERROR.fileReadable(fileNames);
             default:
                 return getErrorMessage(code, fileNames);
         }
     }
 
     /**
      * Parses the given caught message as a JSON error message.
      * 
      * @param caught
      * @return The JSON object parsed from caught.getMessage(), or null if the parse fails.
      */
     protected JSONObject parseJsonError(Throwable caught) {
         try {
             return JsonUtil.getObject(caught.getMessage());
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * Builds a comma separated string list of disk resource names, parsed from a "paths" array in the
      * given jsonError object.
      * 
      * @param jsonError A JSON object containing a "paths" JSON array.
      * @return A comma separated list of disk resource names parsed from jsonError, or an empty string if
      *         parsing fails.
      */
     protected String parsePathsToNameList(JSONObject jsonError) {
         StringBuilder filenames = new StringBuilder();
 
         JSONArray paths = JsonUtil.getArray(jsonError, PATHS);
         if (paths != null) {
             for (int i = 0,size = paths.size(); i < size; i++) {
                 if (i > 0) {
                     filenames.append(", "); //$NON-NLS-1$
                 }
 
                 String path = JsonUtil.getRawValueAsString(paths.get(i));
                 filenames.append(DataUtils.parseNameFromPath(path));
             }
         }
 
         return filenames.toString();
     }
 
     /**
      * Parses result as a JSON object, checks for a failure status field, then returns the parsed object.
      * Throws an exception with result as its message if the given string is not a JSON object or if a
      * status failure field was parsed from the object.
      * 
      * @param result The JSON object string.
      * @return The parsed JSON object.
      * @throws Throwable if result is not a JSON object or if a failure status is parsed from result.
      */
     protected JSONObject getJsonResponse(String result) throws Throwable {
 
         JSONObject ret = JsonUtil.getObject(result);
         if (ret == null) {
             throw new Exception(result);
         }
 
         String status = JsonUtil.getString(ret, STATUS);
         // TODO ignore status if not found in response?
         if (status.equals(STATUS_FAILURE)) {
             throw new Exception(result);
         }
 
         return ret;
     }
 
 }
