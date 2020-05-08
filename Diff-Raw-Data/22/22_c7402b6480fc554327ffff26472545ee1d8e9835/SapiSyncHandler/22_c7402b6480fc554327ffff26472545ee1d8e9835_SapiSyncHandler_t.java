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
 
 import org.json.me.JSONException;
 import org.json.me.JSONObject;
 import org.json.me.JSONArray;
 
 import com.funambol.sapisync.sapi.SapiHandler;
 import com.funambol.sync.SyncException;
 import com.funambol.sync.SyncItem;
 import com.funambol.sync.SyncListener;
 import com.funambol.util.Log;
 import com.funambol.util.DateUtil;
 
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
     
     public static final String JSON_ERROR_CODE_SEC_1002 = "SEC-1002";
     public static final String JSON_ERROR_CODE_SEC_1004 = "SEC-1004";
 
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
      * @throws SyncException
      */
     public void login() throws SyncException {
         try {
             sapiHandler.setAuthenticationMethod(SapiHandler.AUTH_IN_QUERY_STRING);
             JSONObject res = sapiHandler.query("login", "login", null, null, null);
             JSONObject resData = res.getJSONObject(JSON_OBJECT_DATA);
             if(resData != null) {
                 String jsessionid = resData.getString(JSON_OBJECT_DATA_FIELD_JSESSIONID);
                 sapiHandler.enableJSessionAuthentication(true);
                 sapiHandler.forceJSessionId(jsessionid);
                 sapiHandler.setAuthenticationMethod(SapiHandler.AUTH_NONE);
             } else {
                 handleResponseError(resData);
                 throw new SyncException(SyncException.AUTH_ERROR, "Cannot login");
             }
         } catch(Exception ex) {
             if (Log.isLoggable(Log.ERROR)) {
                 Log.error(TAG_LOG, "Failed to login", ex);
             }
             throw new SyncException(SyncException.AUTH_ERROR, "Cannot login");
         }
     }
 
     /**
      * Logout from the current server.
      *
      * @throws SyncException
      */
     public void logout() throws SyncException {
         try {
             sapiHandler.query("login", "logout", null, null, null);
         } catch(Exception ex) {
             if (Log.isLoggable(Log.ERROR)) {
                 Log.error(TAG_LOG, "Failed to logout", ex);
             }
             throw new SyncException(SyncException.AUTH_ERROR, "Cannot logout");
         }
         sapiHandler.enableJSessionAuthentication(false);
         sapiHandler.forceJSessionId(null);
         sapiHandler.setAuthenticationMethod(SapiHandler.AUTH_NONE);
     }
 
     /**
      * Upload the given item to the server
      * @param item
      */
     public void uploadItem(SyncItem item, SyncListener listener) throws SyncException {
         // TODO FIXME
         try {
             SapiUploadSyncListener sapiListener = new SapiUploadSyncListener(
                     item, listener);
             sapiHandler.setSapiRequestListener(sapiListener);
 
             sapiHandler.query("media/upload", "upload", null, null, 
                     item.getInputStream(),
                     item.getObjectSize());
         } catch(Exception ex) {
             if (Log.isLoggable(Log.ERROR)) {
                 Log.error(TAG_LOG, "Failed to upload item", ex);
             }
             throw new SyncException(SyncException.CLIENT_ERROR,
                     "Cannot upload item");
         }
     }
 
     protected class ChangesSet {
         public JSONArray added;
         public JSONArray updated;
         public JSONArray deleted;
     }
 
     public ChangesSet getIncrementalChanges(Date from, String dataType) throws JSONException {
 
         Vector params = new Vector();
        params.addElement("from=" + DateUtil.formatDateTimeUTC(from));
        params.addElement("type=" + dataType);
 
         JSONObject resp = null;
         boolean retry = true;
         int attempt = 0;
         do {
             try {
                 attempt++;
                 resp = sapiHandler.query("profile/changes", "get", params, null, null);
                 retry = false;
             } catch (IOException ioe) {
                 if (attempt >= MAX_RETRIES) {
                     retry = false;
                 }
             }
         } while(retry);
 
         ChangesSet res = new ChangesSet();
 
         JSONObject data = resp.getJSONObject("data");
         if (data != null) {
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
         }
 
         return res;
     }
 
     public JSONArray getItems(String remoteUri, String dataTag, JSONArray ids,
                               String limit, String offset, Date from) throws JSONException {
 
         Vector params = new Vector();
         if (ids != null) {
             JSONObject request = new JSONObject();
             request.put("ids", ids);
 
             params.addElement(request.toString());
         }
         if (limit != null) {
             params.addElement("limit=" + limit);
         }
         if (offset != null) {
             params.addElement("offset=" + offset);
         }
         if (from != null) {
            params.addElement("from=" + DateUtil.formatDateTimeUTC(from));
         }
         params.addElement("exif=none");
 
         JSONObject resp = null;
         boolean retry = true;
         int attempt = 0;
         do {
             try {
                 attempt++;
                 resp = sapiHandler.query("media/" + remoteUri, "get", params, null, null);
                 retry = false;
             } catch (IOException ioe) {
                 if (attempt >= MAX_RETRIES) {
                     retry = false;
                 }
             }
         } while(retry);
 
         if (resp != null) {
             JSONObject data = resp.getJSONObject("data");
             if (data != null) {
                 if (data.has(dataTag)) {
                     JSONArray items = data.getJSONArray(dataTag);
                     return items;
                 }
             }
         }
 
         return null;
     }
 
     public int getItemsCount(String remoteUri, Date from) throws JSONException {
 
         Vector params = new Vector();
         if (from != null) {
            params.addElement("from=" + DateUtil.formatDateTimeUTC(from));
         }
 
         JSONObject resp = null;
         boolean retry = true;
         int attempt = 0;
         do {
             try {
                 attempt++;
                 resp = sapiHandler.query("media/" + remoteUri, "count", params, null, null);
                 retry = false;
             } catch (IOException ioe) {
                 if (attempt >= MAX_RETRIES) {
                     retry = false;
                 }
             }
         } while(retry);
 
         if (resp != null) {
             JSONObject data = resp.getJSONObject("data");
             if (data != null) {
                 if (data.has("count")) {
                     return Integer.parseInt(data.getString("count"));
                 }
             }
         }
         return -1;
     }
 
     private void handleResponseError(JSONObject response) throws Exception {
         try {
             // Check for errors
             JSONObject error = response.getJSONObject(JSON_OBJECT_ERROR);
             if(error != null) {
                 String code    = error.getString(JSON_OBJECT_ERROR_FIELD_CODE);
                 String message = error.getString(JSON_OBJECT_ERROR_FIELD_MESSAGE);
                 String cause   = error.getString(JSON_OBJECT_ERROR_FIELD_CAUSE);
 
                 StringBuffer logMsg = new StringBuffer(
                         "Error in SAPI response").append("\r\n");
                 logMsg.append("code: ").append(code).append("\r\n");
                 logMsg.append("cause: ").append(cause).append("\r\n");
                 logMsg.append("message: ").append(message).append("\r\n");
 
                 // Handle error codes
                 if(JSON_ERROR_CODE_SEC_1002.equals(code)) {
                     // A session is already open. To provide new credentials 
                     // please logout first.
                 } else if(JSON_ERROR_CODE_SEC_1004.equals(code)) {
                     // Both header and parameter credentials provided, please
                     // use only one authentication schema.
                 } 
             }
         } catch(JSONException ex) {
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "Failed to retrieve error json object");
             }
         }
     }
 
     /**
      * Translates the SapiQueryListener calls into SyncListener calls.
      */
     private class SapiUploadSyncListener implements SapiHandler.SapiQueryListener {
 
         private SyncListener syncListener = null;
         private String itemKey = null;
 
         public SapiUploadSyncListener(SyncItem item, SyncListener syncListener) {
             this.syncListener = syncListener;
             this.itemKey = item.getKey();
         }
 
         public void queryStarted(int totalSize) {
             if(syncListener != null) {
                 syncListener.itemAddSendingStarted(itemKey, null, totalSize);
             }
         }
 
         public void queryProgress(int size) {
             if(syncListener != null) {
                 syncListener.itemAddSendingProgress(itemKey, null, size);
             }
         }
 
         public void queryEnded() {
             if(syncListener != null) {
                 syncListener.itemAddSendingEnded(itemKey, null);
             }
         }
     }
 }
