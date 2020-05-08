 /*
  * Funambol is a mobile platform developed by Funambol, Inc. 
  * Copyright (C) 2003 - 2007 Funambol, Inc.
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission 
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE 
  * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Affero General Public License 
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  * 
  * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite 
  * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
  * 
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  * 
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Powered by Funambol" logo. If the display of the logo is not reasonably 
  * feasible for technical reasons, the Appropriate Legal Notices must display
  * the words "Powered by Funambol". 
  */
 
 package com.funambol.sapisync;
 
 import java.util.Vector;
 import java.util.Date;
 import java.io.IOException;
 
 import com.funambol.org.json.me.JSONArray;
 import com.funambol.org.json.me.JSONException;
 import com.funambol.org.json.me.JSONObject;
 import com.funambol.sapisync.sapi.SapiHandler;
 import com.funambol.sapisync.source.JSONFileObject;
 import com.funambol.sapisync.source.JSONSyncItem;
 import com.funambol.sync.SyncItem;
 import com.funambol.sync.SyncListener;
 import com.funambol.util.Log;
 import com.funambol.util.DateUtil;
 import com.funambol.util.StringUtil;
 
 import java.io.InputStream;
 import java.util.Hashtable;
 
 
 /**
  * Handler for calls to SAPI.
  * 
  * All methods of this class must return a SapiException in case of problems,
  * not a SyncException. Caller class is responsible for translation from
  * SapiException to SyncExcetion
  */
 public class SapiSyncHandler {
 
     private static final String TAG_LOG = "SapiSyncHandler";
 
     private static final int MAX_RETRIES = 3;
 
     private SapiHandler sapiHandler = null;
 
     private static final String JSON_OBJECT_DATA  = "data";
     private static final String JSON_OBJECT_ERROR = "error";
 
     private static final String JSON_OBJECT_DATA_FIELD_JSESSIONID = "jsessionid";
 
     private static final String JSON_OBJECT_ERROR_FIELD_CODE    = "code";
     private static final String JSON_OBJECT_ERROR_FIELD_MESSAGE = "message";
     private static final String JSON_OBJECT_ERROR_FIELD_CAUSE   = "cause";
 
     // This field keeps track of the time difference between the server and the
     // client, based on the responsetime returned
     private static long deltaTime;
     
     /**
      * SapiSyncHandler constructor
      * 
      * @param baseUrl the server base url
      * @param user the username to be used for the authentication
      * @param pwd the password to be used for the authentication
      */
     public SapiSyncHandler(String baseUrl, String user, String pwd) {
         this.sapiHandler = new SapiHandler(baseUrl, user, pwd);
     }
 
     /**
      * Login to the current server.
      *
      * @param deviceId this is the device id used to identify the device on the
      * server. This is required to let the server know that a sync session is
      * about to start.
      *
      * @throws SapiException
      */
     public void login(String deviceId) throws SapiException {
         JSONObject response = login(deviceId, 0);
         try {
             updateDeltaTime(response);
         } catch(JSONException ex) {
             Log.error(TAG_LOG, "Failed to login", ex);
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         }
     }
 
     public long getDeltaTime() {
         return deltaTime;
     }
 
     public JSONObject loginAndGetServerInfo() throws SapiException {
         return login(null, 0);
     }
 
     /**
      * Logout from the current server.
      *
      * @throws SapiException
      */
     public void logout() throws SapiException {
         JSONObject response = null;
         try {
             response = sapiQueryWithRetries("login", "logout", null, null, null);
             if (SapiResultError.hasError(response)) {
                 checkForCommonSapiErrorCodesAndThrowSapiException(response, "Cannot logout", true);
                 //TODO manage custom errors
             }
         } catch (NotSupportedCallException e) {
             Log.error(TAG_LOG, "Server doesn't support the SAPI call", e);
             throw SapiException.SAPI_EXCEPTION_CALL_NOT_SUPPORTED;
         } catch (IOException ioe) {
             throw SapiException.SAPI_EXCEPTION_NO_CONNECTION;
         } catch(JSONException ex) {
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         } finally {
             sapiHandler.enableJSessionAuthentication(false);
             sapiHandler.forceJSessionId(null);
             sapiHandler.setAuthenticationMethod(SapiHandler.AUTH_NONE);
         }
     }
 
     public ResumeResult resumeItemUpload(JSONSyncItem item, String remoteUri, SyncListener listener)
     throws SapiException {
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Resuming upload for item: " + item.getKey());
         }
         JSONFileObject json = item.getJSONFileObject();
 
         // First of all we need to query the server to understand where we shall
         // restart from. The item must have a valid guid, otherwise we cannot
         // resume
         String guid = item.getGuid();
 
         if (guid == null) {
             Log.error(TAG_LOG, "Cannot resume, a complete upload will be performed instead");
             String remoteKey = prepareItemUpload(item, remoteUri);
             item.setGuid(remoteKey);
             String crc = uploadItem(item, remoteUri, listener);
             return new ResumeResult(remoteUri, crc);
         }
 
         long length = -1;
         try {
             length = sapiHandler.getMediaPartialUploadLength(remoteUri, guid, json.getSize());
         } catch (NotSupportedCallException e) {
             Log.error(TAG_LOG, "Server doesn't support the SAPI call", e);
             throw SapiException.SAPI_EXCEPTION_CALL_NOT_SUPPORTED;
         } catch(IOException ex) {
             Log.error(TAG_LOG, "Failed to upload item", ex);
             throw SapiException.SAPI_EXCEPTION_NO_CONNECTION;
         }
 
         if (length > 0) {
             if(length == json.getSize()) {
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "No need to resume item " + length);
                 }
                 return new ResumeResult(guid, null);
             } else {
                 long fromByte = length + 1;
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "Upload can be resumed at byte " + fromByte);
                 }
                 String crc = uploadItem(item, remoteUri, listener, fromByte);
                 return new ResumeResult(guid, crc);
             }
         } else {
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "Upload cannot be resumed, perform a complete upload");
             }
             guid = prepareItemUpload(item, remoteUri);
             item.setGuid(guid);
             String crc = uploadItem(item, remoteUri, listener);
             return new ResumeResult(guid, crc);
         }
     }
 
     public String uploadItem(JSONSyncItem item, String remoteUri, SyncListener listener)
     throws SapiException {
         return uploadItem(item, remoteUri, listener, 0);
     }
 
     public String prepareItemUpload(JSONSyncItem item, String remoteUri) throws SapiException {
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Preparing item upload: " + item.getKey());
         }
         try {
             JSONObject metadata = new JSONObject();
             JSONFileObject json = item.getJSONFileObject();
 
             metadata.put(SapiSyncManager.NAME_FIELD, json.getName());
             metadata.put("modificationdate", DateUtil.formatDateTimeUTC(json.getLastModifiedDate()));
             metadata.put("contenttype", json.getMimetype());
             metadata.put(SapiSyncManager.SIZE_FIELD, json.getSize());
             if (item.getState() == SyncItem.STATE_UPDATED) {
                 metadata.put(SapiSyncManager.ID_FIELD, item.getGuid());
                 metadata.put("creationdate", DateUtil.formatDateTimeUTC(json.getCreationDate()));
             } else {
                 // We set the creation date only when the item is first added.
                 // We don't have the real creation date, but most of the time
                 // the last mod time is a good approximation for the first sync
                 metadata.put("creationdate", DateUtil.formatDateTimeUTC(json.getCreationDate()));
             }
 
             JSONObject addRequest = new JSONObject();
             addRequest.put("data", metadata);
 
             if (Log.isLoggable(Log.TRACE)) {
                 Log.trace(TAG_LOG, "metadata " + addRequest.toString());
             }
 
             // Send the meta data request
             sapiHandler.setSapiRequestListener(null);
 
             //json an other exception are managed in the catch at the end of
             //the method
             JSONObject addMetadataResponse = sapiQueryWithRetries("upload/" + remoteUri,
                     "save-metadata", null, null, addRequest);
 
             //original code: throws exception if success is not present
             if (SapiResultError.hasError(addMetadataResponse)) {
                 checkForCommonSapiErrorCodesAndThrowSapiException(addMetadataResponse, null, true);
             }
 
             String remoteKey = addMetadataResponse.getString(SapiSyncManager.ID_FIELD);
             return remoteKey;
         } catch (NotSupportedCallException e) {
             Log.error(TAG_LOG, "Server doesn't support the SAPI call", e);
             throw SapiException.SAPI_EXCEPTION_CALL_NOT_SUPPORTED;
         } catch(JSONException ex) {
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         } catch(IOException ex) {
             throw SapiException.SAPI_EXCEPTION_NO_CONNECTION;
         }
     }
 
     /**
      * Upload the given item to the server (and possibly resume it)
      * @return the remote item key
      * @param item the item to be uploaded. It is mandatory that this item has
      * its guid filled with a valid token returned by the server. A valid token
      * can be obtained by invoking prepareItemUpload
      * @param remoteUri the source remote uri
      * @param listener the sync listener
      * @param fromByte a zero value means the method shall upload a new item
      * from scratch, a non zero value means the method will try to resume a
      * resume
      * @return the item upload time
      */
     public String uploadItem(JSONSyncItem item, String remoteUri, SyncListener listener, long fromByte)
     throws SapiException
     {
         //FIXME
         //attempt, what is its use? Does it really need?
         int attempt = 0;
         do {
             try {
                 // Get ready to perform twice if we have an authorization failure first time
                 return uploadItemHelper(item, remoteUri, listener, fromByte);
             } catch (NotAuthorizedCallException nae) {
                 if (attempt < 2) {
                     if (Log.isLoggable(Log.INFO)) {
                         Log.info(TAG_LOG, "Retrying operation after logging in");
                         // No need to refresh the device id
                         login(null);
                     }
                 } else {
                     throw SapiException.SAPI_EXCEPTION_UNKNOWN;
                 }
             }
         } while(true);
     }
 
 
     public String updateItemName(String remoteUri, String itemId,
             String newItemName) throws SapiException {
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Updating item name");
         }
         try {
             Vector params = new Vector();
             params.addElement("lastupdate=true");
 
             JSONObject data = new JSONObject();
             data.put(SapiSyncManager.ID_FIELD, itemId);
             data.put(SapiSyncManager.NAME_FIELD, newItemName);
             JSONObject request = new JSONObject();
             request.put("data", data);
 
             if (Log.isLoggable(Log.TRACE)) {
                 Log.trace(TAG_LOG, "Update request: " + request.toString());
             }
             JSONObject response = sapiQueryWithRetries("media/" + remoteUri,
                     "update", params, null, request);
             if (SapiResultError.hasError(response)) {
                 checkForCommonSapiErrorCodesAndThrowSapiException(response,
                         "Error in update sapi call", true);
                 //TODO manage custom error code
             }
             return response.getString("lastupdate");
         } catch (NotSupportedCallException e) {
             Log.error(TAG_LOG, "Server doesn't support the SAPI call", e);
             throw SapiException.SAPI_EXCEPTION_CALL_NOT_SUPPORTED;
         } catch (IOException ioe) {
             Log.error(TAG_LOG, "Failed update item name", ioe);
             throw SapiException.SAPI_EXCEPTION_NO_CONNECTION;
         } catch(JSONException ex) {
             Log.error(TAG_LOG, "Failed update item name", ex);
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         }
     }
     
     /**
      * Removes an item from server.
      * {@link SapiException} thrown in case of error could have a code equal to
      * {@link SapiException.CUS_0002}
      * 
      * @param key
      * @param remoteUri
      * @param dataTag
      * 
      * @throws SapiException
      */
     public void deleteItem(String key, String remoteUri, String dataTag)
     throws SapiException {
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Deleting item: " + key);
         }
         try {
             JSONArray pictures = new JSONArray();
             int id;
             try {
                 id = Integer.parseInt(key);
             } catch (Exception e) {
                 Log.error(TAG_LOG, "Invalid key while deleting item", e);
                 throw new SapiException(SapiException.CUS_0002,
                         "Invalid key while deleting item");
             }
             pictures.put(id);
             JSONObject data = new JSONObject();
             data.put(dataTag, pictures);
             JSONObject request = new JSONObject();
             request.put("data", data);
 
             if (Log.isLoggable(Log.TRACE)) {
                 Log.trace(TAG_LOG, "delete item request " + request.toString());
             }
 
             JSONObject response = sapiQueryWithRetries("media/" + remoteUri,
                     "delete", null, null, request);
             if (SapiResultError.hasError(response)) {
                 checkForCommonSapiErrorCodesAndThrowSapiException(response,
                         "Error in delete sapi call", true);
                 //TODO manage custom error code
             }
         } catch (NotSupportedCallException e) {
             Log.error(TAG_LOG, "Server doesn't support the SAPI call", e);
             throw SapiException.SAPI_EXCEPTION_CALL_NOT_SUPPORTED;
         } catch (IOException ioe) {
             Log.error(TAG_LOG, "Failed to delete item: " + key, ioe);
             throw SapiException.SAPI_EXCEPTION_NO_CONNECTION;
         } catch(JSONException ex) {
             Log.error(TAG_LOG, "Failed to delete item: " + key, ex);
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         }
     }
 
     public void deleteAllItems(String remoteUri) throws SapiException {
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Deleting all items");
         }
         try {
             JSONObject response = sapiHandler.query("media/" + remoteUri,
                     "reset", null, null, null);
             if (SapiResultError.hasError(response)) {
                 checkForCommonSapiErrorCodesAndThrowSapiException(response,
                         "Error in reset sapi call", true);
                 //TODO personalized error code
             }
         } catch (NotSupportedCallException e) {
             Log.error(TAG_LOG, "Server doesn't support the SAPI call", e);
             throw SapiException.SAPI_EXCEPTION_CALL_NOT_SUPPORTED;
         } catch (IOException ioe) {
             Log.error(TAG_LOG, "Failed to delete all items", ioe);
             throw SapiException.SAPI_EXCEPTION_NO_CONNECTION;
         } catch(JSONException ex) {
             Log.error(TAG_LOG, "Failed to delete all items", ex);
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         }
     }
 
     public ChangesSet getIncrementalChanges(Date from, String dataType)
     throws SapiException {
         Vector params = new Vector();
         params.addElement("from=" + from.getTime());
         params.addElement("type=" + dataType);
         params.addElement("responsetime=true");
         //FIXME: future implementation of SAPI, not supported now
         params.addElement("sortby=creationdate");
         params.addElement("sortorder=ascending");
 
         JSONObject response = null;
         try {
             response = sapiQueryWithRetries(
                     "profile/changes",
                     "get",
                     params,
                     null,
                     null);
 
         } catch (NotSupportedCallException e) {
             throw SapiException.SAPI_EXCEPTION_CALL_NOT_SUPPORTED;
         } catch (IOException ioe) {
             throw SapiException.SAPI_EXCEPTION_NO_CONNECTION;
         } catch (JSONException e) {
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         }
 
         if (SapiResultError.hasError(response)) {
             checkForCommonSapiErrorCodesAndThrowSapiException(response, "Error in incremental changes sapi call", true);
             //TODO personalized error code
         }
         
         try {
             ChangesSet res = new ChangesSet();
             JSONObject data = getDataFromResponse(response);
             if (data.has(dataType)) {
                 JSONObject items = data.getJSONObject(dataType);
                 if (items != null) {
                     if (items.has("N")) {
                         res.added = items.getJSONArray("N");
                     }
                     if (items.has("U")) {
                         res.updated = items.getJSONArray("U");
                     }
                     if (items.has("D")) {
                         res.deleted = items.getJSONArray("D");
                     }
                 }
             }
 
             // Get the timestamp if available
             if (response.has("responsetime")) {
                 String ts = response.getString("responsetime");
                 if (Log.isLoggable(Log.TRACE)) {
                     Log.trace(TAG_LOG, "SAPI returned response time = " + ts);
                 }
                 try {
                     res.timeStamp = Long.parseLong(ts);
                 } catch (Exception e) {
                     Log.error(TAG_LOG, "Cannot parse server responsetime");
                     res.timeStamp = -1;
                 }
             }
             return res;
 
         } catch (JSONException e) {
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         }
     }
 
     public FullSet getItems(String remoteUri, String dataTag, JSONArray ids, String limit, String offset, Date from)
     throws SapiException {
         JSONObject response = null;
         try {
             Vector params = new Vector();
             if (ids != null) {
                 JSONObject request = new JSONObject();
                 request.put("ids", ids);
     
                 params.addElement("id=" + request.toString());
             }
             if (limit != null) {
                 params.addElement("limit=" + limit);
             }
             if (offset != null) {
                 params.addElement("offset=" + offset);
             }
             if (from != null) {
                 params.addElement("from=" + from.getTime());
             }
             params.addElement("responsetime=true");
             params.addElement("exif=none");
    
             response = sapiQueryWithRetries("media/" + remoteUri, "get",
                     params, null, null);
         } catch (NotSupportedCallException e) {
             throw SapiException.SAPI_EXCEPTION_CALL_NOT_SUPPORTED;
         } catch (IOException ioe) {
             throw SapiException.SAPI_EXCEPTION_NO_CONNECTION;
         } catch (JSONException e) {
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         }
     
         if (SapiResultError.hasError(response)) {
             checkForCommonSapiErrorCodesAndThrowSapiException(response, "Error in get items sapi call", true);
             //TODO personalized error code
         }
 
         try {
             FullSet res = new FullSet();
             JSONObject data = getDataFromResponse(response);
             if (data.has(dataTag)) {
                 JSONArray items = data.getJSONArray(dataTag);
                 res.items = items;
             }
             if (data.has("mediaserverurl")) {
                 res.serverUrl = data.getString("mediaserverurl");
             }
             
             //TODO responsetime outside the success or error check?
             if (response.has("responsetime")) {
                 String ts = response.getString("responsetime");
                 if (Log.isLoggable(Log.TRACE)) {
                     Log.trace(TAG_LOG, "SAPI returned response time = " + ts);
                 }
                 try {
                     res.timeStamp = Long.parseLong(ts);
                 } catch (Exception e) {
                     Log.error(TAG_LOG, "Cannot parse server responsetime");
                     res.timeStamp = -1;
                 }
             }
             return res;
         } catch (JSONException e) {
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         }
     }
 
     public int getItemsCount(String remoteUri, Date from)
     throws SapiException {
         Vector params = new Vector();
         if (from != null) {
             params.addElement("from=" + from.getTime());
         }
         
         try {
             JSONObject response = sapiQueryWithRetries(
                     "media/" + remoteUri,
                     "count",
                     params,
                     null,
                     null);
             
             if (SapiResultError.hasError(response)) {
                 checkForCommonSapiErrorCodesAndThrowSapiException(response, "Error in get items count sapi call", true);
                 //TODO checks custom error
             }
             JSONObject data = getDataFromResponse(response);
             if (data.has("count")) {
                 return Integer.parseInt(data.getString("count"));
             }
             
         } catch (NotSupportedCallException e) {
             throw SapiException.SAPI_EXCEPTION_CALL_NOT_SUPPORTED;
         } catch (IOException ioe) {
             throw SapiException.SAPI_EXCEPTION_NO_CONNECTION;
         } catch (JSONException e) {
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         }
         return -1;
     }
 
     /**
      * Cancels the current operation
      */
     public void cancel() {
         if (Log.isLoggable(Log.DEBUG)) {
             Log.debug(TAG_LOG, "Cancelling any current operation");
         }
         if(sapiHandler != null) {
             sapiHandler.cancel();
         }
     }
     
     
     /**
      * 
      * @param source
      * @return
      */
     public long getUserAvailableServerQuota(String remoteUri) throws SapiException {
         JSONObject response;
         try {
             response = sapiQueryWithRetries(
                     "media",
                     "get-storage-space",
                     null, null, null);
             
             if (SapiResultError.hasError(response)) {
                 checkForCommonSapiErrorCodesAndThrowSapiException(response, "Error in get user available server quota call", true);
             }
             JSONObject data = getDataFromResponse(response);
             if (data.has("free")) {
                 return Long.parseLong(data.getString("free"));
             }
             return -1;
             
         } catch (NotSupportedCallException e) {
             throw SapiException.SAPI_EXCEPTION_CALL_NOT_SUPPORTED;
         } catch (IOException ioe) {
             throw SapiException.SAPI_EXCEPTION_NO_CONNECTION;
         } catch (JSONException e) {
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         }
     }
 
     public class ResumeResult {
         private String key;
         private String crc;
 
         public ResumeResult(String key, String crc) {
             this.key = key;
             this.crc = crc;
         }
 
         public String getKey() {
             return key;
         }
 
         public String getCRC() {
             return crc;
         }
 
         public boolean uploadPerformed() {
             return crc != null;
         }
     }
 
 
     private JSONObject sapiQueryWithRetries(String name, String action, Vector params,
                                             Hashtable headers, JSONObject request)
     throws NotSupportedCallException, JSONException, IOException
     {
         return sapiQueryWithRetries(name, action, params, headers, request, true);
     }
 
     /**
      * Send a SAPI query with a retry mechanism.
      * @param name
      * @param action
      * @param params
      * @param headers
      * @param request
      * @param loginOnExpired perform a login if the invoked SAPI fails with an
      * authentication error (most likely a session has expired)
      * @return
      * @throws JSONException
      */
     private JSONObject sapiQueryWithRetries(String name, String action, Vector params,
                                             Hashtable headers, JSONObject request, boolean loginOnExpired)
     throws NotSupportedCallException, JSONException, IOException
     {
         JSONObject resp = null;
         boolean retry = true;
         int attempt = 0;
         do {
             try {
                 attempt++;
                 resp = sapiHandler.query(name, action, params, headers, request);
                 updateDeltaTime(resp);
                 retry = false;
             } catch (NotSupportedCallException e) {
                 throw SapiException.SAPI_EXCEPTION_CALL_NOT_SUPPORTED;
             } catch (NotAuthorizedCallException nae) {
                 if (attempt >= MAX_RETRIES || !loginOnExpired) {
                     throw nae;
                 }
                 Log.error(TAG_LOG, "Not authorized error, login again");
                 // No need to update the device id
                 login(null);
             } catch (IOException ioe) {
                 if (attempt >= MAX_RETRIES) {
                     throw ioe;
                 }
             }
         } while(retry);
         return resp;
     }
 
     public class ChangesSet {
         public JSONArray added     = null;
         public JSONArray updated   = null;
         public JSONArray deleted   = null;
         public long      timeStamp = -1;
     }
 
     public class FullSet {
         public JSONArray items     = null;
         public long      timeStamp = -1;
         public String    serverUrl = null;
     }
 
     protected JSONObject login(String deviceId, int attempt) throws SapiException {
         try {
             long responseTime = -1;
             sapiHandler.setAuthenticationMethod(SapiHandler.AUTH_IN_QUERY_STRING);
 
             Vector params = new Vector();
             params.addElement("responsetime=true");
             params.addElement("details=true");
             if (deviceId != null) {
                 params.addElement("syncdeviceid=" + deviceId);
             }
 
             JSONObject response = sapiQueryWithRetries("login", "login", params, null, null, false);
 
             if (SapiResultError.hasError(response)) {
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "login returned an error " + response.toString());
                 }
 
                 //verify if it's a already logged problem. if yes, logout and re-login
                 try {
                     checkForCommonSapiErrorCodesAndThrowSapiException(response, null, true);
                 } catch (SapiException e) {
                     if (attempt == 0 && SapiException.SEC_1002.equals(e.getCode())) {
                         // We are already logged in.
                         // Certain HttpConnectionAdapter implementations reuse
                         // connections. In such a case there is no need to
                         // supply credentials again.
                         if (sapiHandler.getConnectionsReuse()) {
                             sapiHandler.setAuthenticationMethod(SapiHandler.AUTH_NONE);
                             sapiHandler.enableJSessionAuthentication(false);
                             return response;
                         }
 
                         // We need to logout first.
                         if (Log.isLoggable(Log.INFO)) {
                             Log.info(TAG_LOG, "logging out");
                         }
                         logout();
                         // login again
                         if (Log.isLoggable(Log.INFO)) {
                             Log.info(TAG_LOG, "logging in");
                         }
                         return login(deviceId, ++attempt);
                     } else {
                         if (Log.isLoggable(Log.INFO)) {
                             Log.info(TAG_LOG, "login error code " + e.getCode());
                         }
                         // propagated exception
                         throw e;
                     }
                 }
             }
 
             JSONObject resData = response.getJSONObject(JSON_OBJECT_DATA);
             if(resData != null) {
                 String jsessionid = resData.getString(JSON_OBJECT_DATA_FIELD_JSESSIONID);
                 sapiHandler.enableJSessionAuthentication(true);
                 sapiHandler.forceJSessionId(jsessionid);
                 sapiHandler.setAuthenticationMethod(SapiHandler.AUTH_NONE);
             } else {
                 throw SapiException.SAPI_EXCEPTION_UNKNOWN;
             }
             return response;
         } catch (NotAuthorizedCallException nae) {
             Log.error(TAG_LOG, "User not authenticated", nae);
             throw SapiException.SAPI_INVALID_CREDENTIALS;
         } catch (NotSupportedCallException e) {
             Log.error(TAG_LOG, "Server doesn't support the SAPI call", e);
             throw SapiException.SAPI_EXCEPTION_CALL_NOT_SUPPORTED;
         } catch(IOException ex) {
             Log.error(TAG_LOG, "Failed to login", ex);
             throw SapiException.SAPI_EXCEPTION_NO_CONNECTION;
         } catch(JSONException ex) {
             Log.error(TAG_LOG, "Failed to login", ex);
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         }
     }
 
     private String uploadItemHelper(JSONSyncItem item, String remoteUri, SyncListener listener, long fromByte)
     throws SapiException, NotAuthorizedCallException
 
     {
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Uploading item: " + item.getKey());
         }
 
         InputStream is = null;
         Hashtable headers = new Hashtable();
         JSONFileObject json = item.getJSONFileObject();
         String remoteKey = item.getGuid();
 
         try {
 
             // If this is not a resume, we must perform the upload in two
             // phases. Send the metadata first and then the actual content
             is = item.getInputStream();
             if(is == null) {
                 if(Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Upload is not needed, item content is null");
                 }
                 return "";
             }
 
             if (fromByte == 0) {
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Uploading a new item with guid " + remoteKey);
                 }
             } else {
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Resuming an item with guid " + remoteKey);
                 }
                 StringBuffer contentRangeValue = new StringBuffer();
                 contentRangeValue.append("bytes ").append(fromByte)
                         .append("-").append(json.getSize()-1)
                         .append("/").append(json.getSize());
                 headers.put("Content-Range", contentRangeValue.toString());
             }
             
             headers.put("x-funambol-id", remoteKey);
             headers.put("x-funambol-file-size", Long.toString(json.getSize()));
 
             SapiUploadSyncListener sapiListener = new SapiUploadSyncListener(item, listener);
             sapiHandler.setSapiRequestListener(sapiListener);
         } catch (NotSupportedCallException e) {
             Log.error(TAG_LOG, "Server doesn't support the SAPI call", e);
             throw SapiException.SAPI_EXCEPTION_CALL_NOT_SUPPORTED;
         } catch(IOException ex) {
             Log.error(TAG_LOG, "Cannot open media stream", ex);
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         }
 
 
         // Send the upload request
         JSONObject uploadResponse = null;
         try {
             Vector params = new Vector();
             params.addElement("lastupdate=true");
 
             uploadResponse = sapiHandler.query("upload/" + remoteUri,
                     "save", params, headers, is, json.getMimetype(),
                     json.getSize(), fromByte, json.getName());
 
             //original code: process the exception if error is present in the object
             if (SapiResultError.hasError(uploadResponse)) {
                 checkForCommonSapiErrorCodesAndThrowSapiException(uploadResponse, null, true);
             }
 
             sapiHandler.setSapiRequestListener(null);
             return uploadResponse.getString("lastupdate");
         } catch (NotSupportedCallException e) {
             Log.error(TAG_LOG, "Server doesn't support the SAPI call", e);
             throw SapiException.SAPI_EXCEPTION_CALL_NOT_SUPPORTED;
         } catch (NotAuthorizedCallException nae) {
             Log.error(TAG_LOG, "Server authentication failure, try to login again", nae);
             throw nae;
         } catch(JSONException ex) {
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         } catch (IOException ioe) {
             // The upload failed and got interrupted. We report this error
             // so that a resume is possible
             throw new SapiException(SapiException.CUS_0001, "Error upload item on server");
         }
     }
 
 
 
     /**
      * Translates the SapiQueryListener calls into SyncListener calls.
      */
     private class SapiUploadSyncListener implements SapiHandler.SapiQueryListener {
 
         private SyncListener syncListener = null;
         private SyncItem item = null;
 
         public SapiUploadSyncListener(SyncItem item, SyncListener syncListener) {
             this.syncListener = syncListener;
             this.item = item;
         }
 
         public void queryStarted(int totalSize) {      
         }
 
         public void queryProgress(int size) {
             if(syncListener != null) {
                 if (item.getState() == SyncItem.STATE_NEW) {
                     syncListener.itemAddSendingProgress(item.getKey(), item.getParent(), size);
                 } else if (item.getState() == SyncItem.STATE_UPDATED) {
                     syncListener.itemReplaceSendingProgress(item.getKey(), item.getParent(), size);
                 }
             }
         }
 
         public void queryEnded() {
         }
     }
 
     /**
      * Extracts data part from a SAPI response. If data is no present, a
      * {@link SapiException} is thrown
      * @param response
      * @return
      * @throws SapiException
      */
     private JSONObject getDataFromResponse(JSONObject response)
     throws SapiException {
         try {
             JSONObject data = response.getJSONObject(JSON_OBJECT_DATA);
             return data;
         } catch (JSONException e) {
             Log.debug(TAG_LOG, "Sapi response doesn't contain data object");
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         }
     }
 
     /**
      * Handles error response from SAPI, logging the error and creating the
      * proper {@link SapiException} object to throw
      * 
      * @param sapiResponse error response to analyze
      * @param fallbackMessage
      * @param throwGenericException
      * @throws SapiException
      */
     private SapiResultError checkForCommonSapiErrorCodesAndThrowSapiException(
             JSONObject sapiResponse,
             String fallbackMessage,
             boolean throwGenericException)
     throws SapiException {
         if (null == sapiResponse) {
             Log.error(TAG_LOG, "Null response from sapi call");
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         }
 
         SapiResultError resultError = SapiResultError.extractFromSapiResponse(sapiResponse);
 
         if (StringUtil.isNullOrEmpty(resultError.code)) {
             Log.error(TAG_LOG, "Invalid return code from sapi call");
             throw SapiException.SAPI_EXCEPTION_UNKNOWN;
         }
         
         //Referring to section 4.1.3 of "Funambol Server API Developers Guide" document
         if (SapiException.NO_CONNECTION.equals(resultError.code)) {
             throw new SapiException(
                     resultError.code,
                     StringUtil.isNullOrEmpty(fallbackMessage) 
                             ? "Connection with server not found"
                             : fallbackMessage,
                     resultError.cause);
         } else if (SapiException.COM_1005.equals(resultError.code)) {
             throw SapiException.SAPI_EXCEPTION_CALL_NOT_SUPPORTED;
         } else if (SapiException.PAPI_0000.equals(resultError.code)) {
             throw new SapiException(
                     resultError.code,
                     StringUtil.isNullOrEmpty(fallbackMessage)
                             ? "Unrecognized error"
                             : fallbackMessage,
                     resultError.cause);
         } else if (SapiException.SEC_1001.equals(resultError.code)) {
             throw new SapiException(
                     resultError.code,
                     StringUtil.isNullOrEmpty(fallbackMessage)
                             ? "The administrator must specify the userid to perform the action."
                             : fallbackMessage,
                     resultError.cause);
         } else if (SapiException.SEC_1002.equals(resultError.code)) {
             throw new SapiException(
                     resultError.code,
                     StringUtil.isNullOrEmpty(fallbackMessage)
                             ? "A session is already open. To provide new credentials please logout first."
                             : fallbackMessage,
                     resultError.cause);
         } else if (SapiException.SEC_1003.equals(resultError.code)) {
             throw new SapiException(
                     resultError.code,
                     StringUtil.isNullOrEmpty(fallbackMessage)
                             ? "Invalid mandatory validation key"
                             : fallbackMessage,
                     resultError.cause);
         } else if (SapiException.SEC_1004.equals(resultError.code)) {
             throw new SapiException(
                     resultError.code,
                     StringUtil.isNullOrEmpty(fallbackMessage)
                             ? "Both header and parameter credentials provided, please use only one authentication schema."
                             : fallbackMessage,
                     resultError.cause);
         }
 
         if (throwGenericException) {
             throw new SapiException(
                     resultError.code,
                     StringUtil.isNullOrEmpty(fallbackMessage)
                             ? "Unmanager SAPI error"
                             : fallbackMessage,
                     resultError.cause);
         } else {
             //non standard error code, so calling method must handles it
             return resultError;
         }
     }
 
     private void updateDeltaTime(JSONObject response) throws JSONException {
         long responseTime = -1;
        if (response != null && response.has("responsetime")) {
             // Update the time difference
             String ts = response.getString("responsetime");
             if (Log.isLoggable(Log.TRACE)) {
                 Log.trace(TAG_LOG, "SAPI returned response time = " + ts);
             }
             long now = System.currentTimeMillis();
             try {
                 responseTime = Long.parseLong(ts);
                 deltaTime = responseTime - now;
             } catch (Exception e) {
                 Log.error(TAG_LOG, "Cannot parse server responsetime");
             }
         }
     }
 
     /**
      * Handles SAPI result in case of error
      */
     static class SapiResultError {
         public String code;
         public String message;
         public String cause;
         
         public SapiResultError() {}
         
         public SapiResultError(String code, String message, String cause) {
             this.code = code;
             this.message = message;
             this.cause = cause;
         }
         
         public static boolean hasError(JSONObject sapiResponse) {
             //some APIs reply with a null response
             if (null == sapiResponse) return false;
             return sapiResponse.has(JSON_OBJECT_ERROR);
             //TODO add the check for "success" string inside the object?
         }
         
         public static SapiResultError extractFromSapiResponse(JSONObject sapiResponse) {
             if (null == sapiResponse) {
                 throw new IllegalArgumentException("SAPI response cannot be null");
             }
             
             //before, if json object doens't have all tree field, an error is trown
             SapiResultError sapiResultError = new SapiResultError();
             if (sapiResponse.has(JSON_OBJECT_ERROR)) {
                 JSONObject error = null;
                 try {
                     error = sapiResponse.getJSONObject(JSON_OBJECT_ERROR);
                 } catch (JSONException e) {
                     //cannot happen
                 }
                 try {
                     sapiResultError.code = error.getString(JSON_OBJECT_ERROR_FIELD_CODE);
                 } catch (JSONException e) {}
                 try {
                     sapiResultError.message = error.getString(JSON_OBJECT_ERROR_FIELD_MESSAGE);
                 } catch (JSONException e) {}
                 try {
                     sapiResultError.cause = error.getString(JSON_OBJECT_ERROR_FIELD_CAUSE);
                 } catch (JSONException e) {}
             }
             
             //log the error
             if (Log.isLoggable(Log.DEBUG)) {
                 StringBuffer logMsg = new StringBuffer()
                         .append("Error in SAPI response").append("\r\n")
                         .append("code: ").append(sapiResultError.code).append("\r\n")
                         .append("cause: ").append(sapiResultError.cause).append("\r\n")
                         .append("message: ").append(sapiResultError.message).append("\r\n");
                 Log.debug(TAG_LOG, logMsg.toString());
             }
             return sapiResultError;
         }
     }
 }
