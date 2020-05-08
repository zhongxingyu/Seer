 /*
  * Funambol is a mobile platform developed by Funambol, Inc.
  * Copyright (C) 2011 Funambol, Inc.
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
  * The interactive user interfaces in modified sourceName and object code versions
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
 
 import java.util.Hashtable;
 import java.util.Enumeration;
 import java.util.Vector;
 import java.io.IOException;
 
 import com.funambol.storage.StringKeyValueStore;
 import com.funambol.storage.StringKeyValueStoreFactory;
 import com.funambol.sync.SyncReport;
 import com.funambol.sync.SyncSource;
 import com.funambol.sync.SyncItem;
 import com.funambol.util.StringUtil;
 import com.funambol.util.DateUtil;
 import com.funambol.util.Log;
 
 public class SapiSyncStatus implements SyncReport {
     
     private static final String TAG_LOG = "SapiSyncStatus";
 
     private static final String SYNC_STATUS_TABLE_PREFIX = "syncstatus_";
 
     public static final int INIT_PHASE      = 0;
     public static final int SENDING_PHASE   = 1;
     public static final int RECEIVING_PHASE = 2;
     public static final int MAPPING_PHASE   = 3;
 
     private static final String REQUESTED_SYNC_MODE_KEY = "REQUESTED_SYNC_MODE";
     private static final String SYNC_PHASE_KEY          = "SYNC_PHASE";
     private static final String SENT_ITEM_KEY           = "SENT_ITEM_";
     private static final String RECEIVED_ITEM_KEY       = "RECEIVED_ITEM_";
     private static final String INTERRUPTED_KEY         = "INTERRUPTED";
     private static final String STATUS_CODE_KEY         = "STATUS_CODE";
     private static final String LAST_SYNC_START_TIME_KEY= "LAST_SYNC_START_TIME";
     private static final String LOC_URI_KEY             = "LOC_URI";
     private static final String REMOTE_URI_KEY          = "REMOTE_URI";
 
     private static final String TRUE                    = "TRUE";
     private static final String FALSE                   = "FALSE";
 
     private String sourceName;
     private StringKeyValueStore store;
 
     private int requestedSyncMode = -1;
     private int oldRequestedSyncMode = -1;
     private int statusCode = -1;
     private int oldStatusCode = -1;
 
     private Throwable se = null;
 
     private String locUri = null;
     private String oldLocUri = null;
 
     private String remoteUri = null;
     private String oldRemoteUri = null;
 
     private long lastSyncStartTime = 0;
     private long oldLastSyncStartTime = 0;
 
     private boolean interrupted = false;
     private boolean oldInterrupted = false;
 
     private long       startTime = 0;
     private long       endTime   = 0;
 
     private Hashtable sentItems = new Hashtable();
     private Hashtable receivedItems = new Hashtable();
 
     private Hashtable pendingSentItems = new Hashtable();
     private Hashtable pendingReceivedItems = new Hashtable();
 
     private Vector sentResumedItems = new Vector();
     private Vector receivedResumedItems = new Vector();
 
     private static StringKeyValueStoreFactory storeFactory = StringKeyValueStoreFactory.getInstance();
 
     public SapiSyncStatus(String sourceName) {
         this.sourceName = sourceName;
         store = storeFactory.getStringKeyValueStore(SYNC_STATUS_TABLE_PREFIX + sourceName);
     }
 
     public int getRequestedSyncMode() {
         return requestedSyncMode;
     }
 
     public void setRequestedSyncMode(int requestedSyncMode) {
         this.requestedSyncMode = requestedSyncMode;
     }
 
     public long getLastSyncStartTime() {
         return lastSyncStartTime;
     }
 
     public void setLastSyncStartTime(long lastSyncStartTime) {
         oldLastSyncStartTime = this.lastSyncStartTime;
         this.lastSyncStartTime = lastSyncStartTime;
     }
 
     public int getStatusCode() {
         return statusCode;
     }
 
     public void setStatusCode(int statusCode) {
         oldStatusCode = this.statusCode;
         this.statusCode = statusCode;
     }
 
     public void setLocUri(String locUri) {
         oldLocUri = this.locUri;
         this.locUri = locUri;
     }
 
     public String getLocUri() {
         return locUri;
     }
 
     public void setRemoteUri(String remoteUri) {
         oldRemoteUri = this.remoteUri;
         this.remoteUri = remoteUri;
     }
 
     public String getRemoteUri() {
         return remoteUri;
     }
 
     public void addSentItem(String key, char cmd) {
         // The item was sent but a status has not been received yet
         SentItemStatus status = new SentItemStatus(cmd);
         if(sentItems.containsKey(key)) {
             sentItems.put(key, status);
         } else {
             pendingSentItems.put(key, status);
         }
     }
 
     public void setSentItemStatus(String key, int status) {
         setSentItemStatus(key, status, null);
     }
 
     public void setSentItemStatus(String key, int status, String guid) {
         SentItemStatus s = (SentItemStatus)sentItems.get(key);
         if (s == null) {
             s = (SentItemStatus)pendingSentItems.get(key);
         }
         if(s == null) {
             Log.error(TAG_LOG, "Cannot find sent item with key: " + key);
             return;
         }
         s.setStatus(status);
         if (guid != null) {
             s.setGuid(guid);
         }
     }
 
     public int getSentItemsCount() {
         return sentItems.size() + pendingSentItems.size();
     }
 
     public Enumeration getSentItems() {
         if (pendingSentItems.isEmpty()) {
             return sentItems.keys();
         } else if (sentItems.isEmpty()) {
             return pendingSentItems.keys();
         } else {
             // This should never happen with our SyncManager, but we support the
             // case as well
             Vector res = new Vector();
             Enumeration e = sentItems.keys();
             while(e.hasMoreElements()) {
                 String k = (String)e.nextElement();
                 res.addElement(k);
             }
             e = pendingSentItems.keys();
             while(e.hasMoreElements()) {
                 String k = (String)e.nextElement();
                 res.addElement(k);
             }
             return res.elements();
         }
     }
 
     /**
      * The client received the status for an item it sent out.
      */
     public void receivedItemStatus(String key, int status) {
 
         SentItemStatus itemStatus = (SentItemStatus)sentItems.get(key);
         if (itemStatus == null) {
             itemStatus = (SentItemStatus)pendingSentItems.get(key);
         }
 
         if (itemStatus == null) {
             Log.error(TAG_LOG, "Setting the status for an item which was not sent " + key);
         } else {
             itemStatus.setStatus(status);
         }
     }
 
     /**
      * The client received an item via a command and it has been processed
      * generating a certain status.
      *
      * @param guid the server id for the item
      * @param luid the client id for the item
      * @param cmd the command the server sent the item into
      * @param status the client status for this command
      */
     public void addReceivedItem(String guid, String luid, char cmd, int statusCode) {
         ReceivedItemStatus status = new ReceivedItemStatus(guid, cmd);
         status.setStatus(statusCode);
         // If the pending received items already have this key, then we add
         // the current item with another key. This may happen for example if two
         // commands have the same key. When keys are used (e.g. calendar sync)
         // we may receive a delete for an item and then the successive add will
         // reuse the same key. The status for the delete is not used for the
         // mappings, so we can safely change its key. We still keep track of it
         // so that the total number of excahnged items is correct.
         if(pendingReceivedItems.containsKey(luid)) {
             ReceivedItemStatus oldStatus = (ReceivedItemStatus)pendingReceivedItems.get(luid);
             pendingReceivedItems.put(luid, status);
             StringBuffer newKey = new StringBuffer(luid);
             newKey.append("bis");
             pendingReceivedItems.put(newKey.toString(), oldStatus);
         } else {
             pendingReceivedItems.put(luid, status);
         }
     }
 
     public int getReceivedItemsCount() {
         return receivedItems.size() + pendingReceivedItems.size();
     }
 
     public void addMappingSent(String luid) {
         ReceivedItemStatus status = (ReceivedItemStatus)receivedItems.get(luid);
         if (status == null) {
             status = (ReceivedItemStatus)pendingReceivedItems.get(luid);
         }
         status.setMapSent(true);
     }
 
     public boolean getInterrupted() {
         return interrupted;
     }
 
     public void setInterrupted(boolean interrupted) {
         this.interrupted = interrupted;
     }
 
     /**
      * Gets the status for a sent item. If the item has not been sent or its
      * status has not been received yet, then -1 is returned
      */
     public int getSentItemStatus(String key) {
         SentItemStatus s = (SentItemStatus)sentItems.get(key);
         if (s == null) {
             s = (SentItemStatus)pendingSentItems.get(key);
         }
         if (s == null) {
             return ItemStatus.UNDEFINED_STATUS;
         } else {
             return s.getStatus();
         }
     }
 
     public String getSentItemGuid(String key) {
         SentItemStatus s = (SentItemStatus)sentItems.get(key);
         if (s == null) {
             s = (SentItemStatus)pendingSentItems.get(key);
         }
         if (s == null) {
             return null;
         } else {
             return s.getGuid();
         }
     }
 
     public String getReceivedItemLuid(String guid) {
         Enumeration keys = receivedItems.keys();
         while(keys.hasMoreElements()) {
             String luid = (String)keys.nextElement();
             ReceivedItemStatus status = (ReceivedItemStatus)receivedItems.get(luid);
             String g = status.getGuid();
             if (guid.equals(g)) {
                 return luid;
             }
         }
         keys = pendingReceivedItems.keys();
         while(keys.hasMoreElements()) {
             String luid = (String)keys.nextElement();
             ReceivedItemStatus status = (ReceivedItemStatus)pendingReceivedItems.get(luid);
             String g = status.getGuid();
             if (guid.equals(g)) {
                 return luid;
             }
         }
         return null;
     }
 
     public Hashtable getPendingMappings() {
         // Create an enumeration with all the pending mappings. The value is
         Hashtable res = new Hashtable();
         Enumeration keys = receivedItems.keys();
         while(keys.hasMoreElements()) {
             String luid = (String)keys.nextElement();
             ReceivedItemStatus status = (ReceivedItemStatus)receivedItems.get(luid);
             if (!status.getMapSent() && SyncItem.STATE_NEW == status.getCmd()) {
                 res.put(luid, status.getGuid());
             }
         }
         keys = pendingReceivedItems.keys();
         while(keys.hasMoreElements()) {
             String luid = (String)keys.nextElement();
             ReceivedItemStatus status = (ReceivedItemStatus)pendingReceivedItems.get(luid);
             if (!status.getMapSent() && SyncItem.STATE_NEW == status.getCmd()) {
                 res.put(luid, status.getGuid());
             }
         }
         return res;
     }
 
     /**
      * Returns the ItemMap related to the given name
      * 
      * @param sourceName the name of the source to be retrieved
      * @return ItemMap of the given source
      */
     public void load() throws IOException {
 
         store.load();
         Enumeration keys = store.keys();
 
         while(keys.hasMoreElements()) {
             String key   = (String)keys.nextElement();
             String value = store.get(key);
 
              if (REQUESTED_SYNC_MODE_KEY.equals(key)) {
                 requestedSyncMode = Integer.parseInt(value);
                 oldRequestedSyncMode = requestedSyncMode;
             } else if (INTERRUPTED_KEY.equals(key)) {
                 interrupted = TRUE.equals(value.toUpperCase());
                 oldInterrupted = interrupted;
             } else if (key.startsWith(SENT_ITEM_KEY)) {
                 String itemKey = key.substring(SENT_ITEM_KEY.length());
                 // The value contains both the cmd and the status
                 String values[] = StringUtil.split(value, ",");
                 char cmd = values[0].charAt(0);
                 int status = Integer.parseInt(values[1]);
                 String guid = null;
                 if (values.length == 3) {
                     guid = values[2];
                 }
                 SentItemStatus v = new SentItemStatus(cmd);
                 v.setStatus(status);
                 if (guid != null) {
                     v.setGuid(guid);
                 }
                 sentItems.put(itemKey, v);
             } else if (key.equals(LOC_URI_KEY)) {
                 locUri = value;
             } else if (key.equals(REMOTE_URI_KEY)) {
                 remoteUri = value;
             } else if (key.startsWith(RECEIVED_ITEM_KEY)) {
                 String itemKey = key.substring(RECEIVED_ITEM_KEY.length());
                 // The value contains the GUID, the map flag, the cmd and the
                 // status
                 String values[] = StringUtil.split(value, ",");
                 String guid   = values[0];
                 String mapped = values[1];
                 char cmd    = values[2].charAt(0);
                 String stat   = values[3];
                 ReceivedItemStatus status = new ReceivedItemStatus(guid, cmd);
                 if (TRUE.equals(mapped.toUpperCase())) {
                     status.setMapSent(true);
                 } else {
                     status.setMapSent(false);
                 }
                 int s = Integer.parseInt(stat);
                 status.setStatus(s);
                 receivedItems.put(itemKey, status);
             }
         }
     }
 
     /**
      * Replace the current mappings with the new one and persist the info
      *
      * @param mappings the mapping hshtable
      */
     public void save() throws IOException {
 
         // We save only what changed, nothing more
         Enumeration keys = pendingSentItems.keys();
         while(keys.hasMoreElements()) {
             String key    = (String)keys.nextElement();
             SentItemStatus status = (SentItemStatus)pendingSentItems.get(key);
 
             StringBuffer v = new StringBuffer();
             v.append(status.getCmd()).append(",").append(status.getStatus());
             String guid = status.getGuid();
             if (guid != null) {
                 v.append(",").append(guid);
             }
 
             store.add(SENT_ITEM_KEY + key, v.toString());
 
             // Now move this item into the in memory values
             sentItems.put(key, status);
         }
 
         keys = pendingReceivedItems.keys();
         while(keys.hasMoreElements()) {
             String key    = (String)keys.nextElement();
             ReceivedItemStatus status = (ReceivedItemStatus)pendingReceivedItems.get(key);
 
             StringBuffer v = new StringBuffer(status.getGuid());
             v.append(",").append(status.getMapSent() ? TRUE : FALSE);
             v.append(",").append(status.getCmd());
             v.append(",").append(status.getStatus());
             store.add(RECEIVED_ITEM_KEY + key, v.toString());
 
             // Now move this item into the in memory values
             receivedItems.put(key, status);
         }
        
         if (oldRequestedSyncMode != requestedSyncMode) {
             if (oldRequestedSyncMode == -1) {
                 store.add(REQUESTED_SYNC_MODE_KEY, "" + requestedSyncMode);
             } else {
                 store.update(REQUESTED_SYNC_MODE_KEY, "" + requestedSyncMode);
             }
             oldRequestedSyncMode = requestedSyncMode;
         }
 
         if (!locUri.equals(oldLocUri)) {
             if (oldLocUri == null) {
                 store.add(LOC_URI_KEY, locUri);
             } else {
                 store.update(LOC_URI_KEY, locUri);
             }
             oldLocUri = locUri;
         }
 
         if (!remoteUri.equals(oldRemoteUri)) {
             if (oldRemoteUri == null) {
                 store.add(REMOTE_URI_KEY, remoteUri);
             } else {
                 store.update(REMOTE_URI_KEY, remoteUri);
             }
             oldRemoteUri = remoteUri;
         }
 
         if (oldStatusCode != statusCode) {
             if (oldStatusCode == -1) {
                 store.add(STATUS_CODE_KEY, "" + statusCode);
             } else {
                 store.update(STATUS_CODE_KEY, "" + statusCode);
             }
             oldStatusCode = statusCode;
         }
 
         if (oldLastSyncStartTime != lastSyncStartTime) {
             if (oldLastSyncStartTime == 0) {
                 store.add(LAST_SYNC_START_TIME_KEY, "" + lastSyncStartTime);
             } else {
                 store.update(LAST_SYNC_START_TIME_KEY, "" + lastSyncStartTime);
             }
             oldLastSyncStartTime = lastSyncStartTime;
         }
 
         if (interrupted != oldInterrupted ) {
             if (store.get(INTERRUPTED_KEY) == null) {
                 store.add(INTERRUPTED_KEY, "" + interrupted);
             } else {
                 store.update(INTERRUPTED_KEY, "" + interrupted);
             }
             oldInterrupted = interrupted;
         }
 
         pendingSentItems.clear();
         pendingReceivedItems.clear();
 
         store.save();
     }
 
     /**
      * Completely reset the sync status.
      */
     public void reset() throws IOException {
         store.reset();
         init();
     }
 
     public int getReceivedAddNumber() {
         int v1 = getItemsNumber(receivedItems, SyncItem.STATE_NEW);
         int v2 = getItemsNumber(pendingReceivedItems, SyncItem.STATE_NEW);
         return v1 + v2;
     }
 
     public int getReceivedReplaceNumber() {
         int v1 = getItemsNumber(receivedItems, SyncItem.STATE_UPDATED);
         int v2 = getItemsNumber(pendingReceivedItems, SyncItem.STATE_UPDATED);
         return v1 + v2;
     }
 
     public int getReceivedDeleteNumber() {
         int v1 = getItemsNumber(receivedItems, SyncItem.STATE_DELETED);
         int v2 = getItemsNumber(pendingReceivedItems, SyncItem.STATE_DELETED);
         return v1 + v2;
     }
 
     public int getSentAddNumber() {
         int v1 = getItemsNumber(sentItems, SyncItem.STATE_NEW);
         int v2 = getItemsNumber(pendingSentItems, SyncItem.STATE_NEW);
         return v1 + v2;
     }
     
     public int getSentReplaceNumber() {
         int v1 = getItemsNumber(sentItems, SyncItem.STATE_UPDATED);
         int v2 = getItemsNumber(pendingSentItems, SyncItem.STATE_UPDATED);
         return v1 + v2;
     }
 
     public int getSentDeleteNumber() {
         int v1 = getItemsNumber(sentItems, SyncItem.STATE_DELETED);
         int v2 = getItemsNumber(pendingSentItems, SyncItem.STATE_DELETED);
         return v1 + v2;
     }
 
     public int getNumberOfReceivedItemsWithError() {
         return getNumberOfItemsWithError(receivedItems) + getNumberOfItemsWithError(pendingReceivedItems);
     }
     
     public int getNumberOfReceivedItemsWithSyncStatus(int syncStatus) {
         return getNumberOfItemsWithSyncStatus(receivedItems, syncStatus) + getNumberOfItemsWithSyncStatus(pendingReceivedItems, syncStatus);
     }
     
     public int getNumberOfSentItemsWithError() {
         return getNumberOfItemsWithError(sentItems) + getNumberOfItemsWithError(pendingSentItems);
     }
     
     public int getNumberOfSentItemsWithSyncStatus(int syncStatus) {
         return getNumberOfItemsWithSyncStatus(sentItems, syncStatus) + getNumberOfItemsWithSyncStatus(pendingSentItems, syncStatus);
     }
 
     public void addSentResumedItem(String key) {
         sentResumedItems.addElement(key);
     }
 
     public void addReceivedResumedItem(String key) {
         receivedResumedItems.addElement(key);
     }
 
     public int getReceivedResumedNumber() {
         return receivedResumedItems.size();
     }
 
     public int getSentResumedNumber() {
         return sentResumedItems.size();
     }
 
     public String toString() {
         StringBuffer res = new StringBuffer();
 
         res.append("\n");
         res.append("==================================================================\n");
         res.append("| Syncrhonization report for\n");
         res.append("| Local URI: ").append(locUri).append(" - Remote URI:").append(remoteUri).append("\n");
         res.append("| Requested sync mode: ").append(requestedSyncMode).append("\n");
         res.append("|-----------------------------------------------------------------\n");
         res.append("| Changes received from server in this sync\n");
         res.append("|-----------------------------------------------------------------\n");
         res.append("| Add: ").append(getReceivedAddNumber()).append("\n");
         res.append("| Replace: ").append(getReceivedReplaceNumber()).append("\n");
         res.append("| Delete: ").append(getReceivedDeleteNumber()).append("\n");
         res.append("| Resumed: ").append(getReceivedResumedNumber()).append("\n");
         res.append("| Total errors: ").append(getNumberOfReceivedItemsWithError()).append("\n");
         res.append("|-----------------------------------------------------------------\n");
         res.append("| Changes sent to server in this sync\n");
         res.append("|-----------------------------------------------------------------\n");
         res.append("| Add: ").append(getSentAddNumber()).append("\n");
         res.append("| Replace: ").append(getSentReplaceNumber()).append("\n");
         res.append("| Delete: ").append(getSentDeleteNumber()).append("\n");
         res.append("| Resumed: ").append(getSentResumedNumber()).append("\n");
         res.append("| Total errors: ").append(getNumberOfSentItemsWithError()).append("\n");
         res.append("|-----------------------------------------------------------------\n");
         res.append("| Global sync status: ").append(getStatusCode()).append("\n");
         res.append("|-----------------------------------------------------------------\n");
         String start = DateUtil.formatDateTimeUTC(startTime);
         res.append("| Sync start time: ").append(start).append("\n");
         String end   = DateUtil.formatDateTimeUTC(endTime);
         res.append("| Sync end time: ").append(end).append("\n");
         long totalSecs = (endTime - startTime) / 1000;
         res.append("| Sync total time: ").append(totalSecs).append(" [secs]\n");
         res.append("==================================================================\n");
 
         return res.toString();
     }
 
     public void setSyncException(Throwable exc) {
         se = exc;
     }
 
     public Throwable getSyncException() {
         return se;
     }
 
     public long getStartTime() {
         return startTime;
     }
 
     public void setStartTime(long startTime) {
         this.startTime = startTime;
     }
 
     public long getEndTime() {
         return endTime;
     }
 
     public void setEndTime(long endTime) {
         this.endTime = endTime;
     }
 
     /**
      * This method is mainly intended for testing. It allows to use a store
      * factory different from the platform standard one.
      */
     public static void setStoreFactory(StringKeyValueStoreFactory factory) {
         SapiSyncStatus.storeFactory = factory;
     }
 
     private void init(){
         requestedSyncMode = -1;
         oldRequestedSyncMode = -1;
         statusCode = -1;
         oldStatusCode = -1;
         sentItems.clear();
         receivedItems.clear();
         pendingSentItems.clear();
         pendingReceivedItems.clear();
         sentResumedItems.clear();
         receivedResumedItems.clear();
         locUri = null;
         remoteUri = null;
         se = null;
         startTime = 0;
         endTime = 0;
         interrupted = false;
         oldInterrupted = false;
     }
 
     private int getItemsNumber(Hashtable table, char cmd) {
         int count = 0;
         Enumeration keys = table.keys();
         while (keys.hasMoreElements()) {
             String key = (String)keys.nextElement();
             ItemStatus status = (ItemStatus)table.get(key);
             if (cmd == status.getCmd()) {
                 count++;
             }
         }
         return count;
     }
     
     private int getNumberOfItemsWithSyncStatus(Hashtable table, int syncStatus) {
         int count = 0;
         Enumeration keys = table.keys();
         while (keys.hasMoreElements()) {
             String key = (String)keys.nextElement();
             ItemStatus status = (ItemStatus)table.get(key);
             if (syncStatus == status.getStatus()) {
                 count++;
             }
         }
         return count;
     }
 
     private int getNumberOfItemsWithError(Hashtable table) {
         int count = 0;
         Enumeration keys = table.keys();
         while (keys.hasMoreElements()) {
             String key = (String)keys.nextElement();
             ItemStatus status = (ItemStatus)table.get(key);
             if (status.getStatus() != SyncSource.SUCCESS_STATUS) {
                 count++;
             }
         }
         return count;
     }
 
     private class ItemStatus {
 
         public static final int UNDEFINED_STATUS = -1;
         protected char cmd;
         protected int status = UNDEFINED_STATUS;
         protected String  guid;
 
         public ItemStatus(char cmd) {
             this.cmd = cmd;
         }
 
         public void setStatus(int status) {
             this.status = status;
         }
 
         public int getStatus() {
             return status;
         }
 
         public char getCmd() {
             return cmd;
         }
 
         public String getGuid() {
             return guid;
         }
     }
 
     private class ReceivedItemStatus extends ItemStatus {
         private boolean mapSent;
         private int status;
 
         public ReceivedItemStatus(String guid, char cmd) {
             super(cmd);
             this.guid = guid;
         }
 
         public void setMapSent(boolean value) {
             mapSent = value;
         }
 
         public boolean getMapSent() {
             return mapSent;
         }
     }
 
     private class SentItemStatus extends ItemStatus {
 
         public SentItemStatus(char cmd) {
             super(cmd);
         }
 
         public void setGuid(String guid) {
             this.guid = guid;
         }
     }
 
 
 }
