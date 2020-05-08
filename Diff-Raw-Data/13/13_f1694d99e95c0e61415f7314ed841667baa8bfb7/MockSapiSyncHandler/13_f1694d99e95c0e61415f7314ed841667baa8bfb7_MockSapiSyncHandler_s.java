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
 
 import org.json.me.JSONException;
 import org.json.me.JSONObject;
 import org.json.me.JSONArray;
 
 import com.funambol.sync.SyncException;
 import com.funambol.sync.SyncItem;
 import com.funambol.sync.SyncListener;
 
 /**
  * Implements a Mock for the SapiSyncHandler interface
  */
 public class MockSapiSyncHandler extends SapiSyncHandler {
 
     private static final String TEST_SERVER = "http://test.server.url";
     private static final String TEST_USERNAME = "user";
     private static final String TEST_PASSWORD = "pwd";
 
     private ChangesSet changesSet = null;
     private JSONArray fullSyncItems[] = null;
     private int itemsCount;
     private Vector uploadedItems = new Vector();
     private int loginCount = 0;
     private int logoutCount = 0;
     private int fullSyncItemsIdx = 0;
 
     private Vector limitRequests        = new Vector();
     private Vector dateLimitRequests    = new Vector();
     private Vector dateLimitAllRequests = new Vector();
     private Vector offsetRequests       = new Vector();
     private Vector idsRequests          = new Vector();
 
     public MockSapiSyncHandler() {
         super(TEST_SERVER, TEST_USERNAME, TEST_PASSWORD);
     }
 
     public void login() throws SyncException {
         loginCount++;
     }
 
     public void logout() throws SyncException {
         logoutCount++;
     }
 
     public void uploadItem(SyncItem item, SyncListener listener)
             throws SyncException {
         uploadedItems.addElement(item);
     }
 
     public ChangesSet getIncrementalChanges(Date from, String dataType) throws JSONException {
         return changesSet;
     }
 
     public void setIncrementalChanges(JSONArray added, JSONArray updated, JSONArray deleted) {
         ChangesSet res = new ChangesSet();
         res.added = added;
         res.updated = updated;
         res.deleted = deleted;
         this.changesSet = res;
     }
 
    public JSONArray getItems(String remoteUri, String dataTag, JSONArray ids,
             String limit, String offset, Date from) throws JSONException {
 
         // Save this information to be checked later
         if (from != null) {
             dateLimitRequests.addElement(from);
         }
         if (limit != null) {
             limitRequests.addElement(limit);
         }
         if (offset != null) {
             offsetRequests.addElement(offset);
         }
         if (ids != null) {
             idsRequests.addElement(ids);
         }
         // Return the proper value
        if (fullSyncItems == null) {
            return null;
        } else {
            return fullSyncItems[fullSyncItemsIdx++];
         }
     }
 
     public void setItems(JSONArray fullSyncItems[]) {
         this.fullSyncItems = fullSyncItems;
     }
 
     public Vector getDateLimitRequests() {
         return dateLimitRequests;
     }
 
     public Vector getDateLimitAllRequests() {
         return dateLimitAllRequests;
     }
 
     public Vector getLimitRequests() {
         return limitRequests;
     }
 
     public Vector getOffsetRequests() {
         return offsetRequests;
     }
 
     public Vector getIdsRequests() {
         return idsRequests;
     }
 
     public int getItemsCount(String remoteUri, Date from) throws JSONException {
         if (from != null) {
             dateLimitAllRequests.addElement(from);
         }
         return itemsCount;
     }
 
     public void setItemsCount(int itemsCount) {
         this.itemsCount = itemsCount;
     }
 
     public Vector getUploadedItems() {
         return uploadedItems;
     }
 
     public int getLoginCount() {
         return loginCount;
     }
 
     public int getLogoutCount() {
         return logoutCount;
     }
 }
