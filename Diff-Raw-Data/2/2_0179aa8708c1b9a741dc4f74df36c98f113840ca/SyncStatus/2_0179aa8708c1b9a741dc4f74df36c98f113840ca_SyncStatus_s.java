 /*
  * Funambol is a mobile platform developed by Funambol, Inc.
  * Copyright (C) 2010 Funambol, Inc.
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
 
 package com.funambol.syncml.spds;
 
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import com.funambol.storage.QueryResult;
 import com.funambol.storage.StringKeyValueStore;
 import com.funambol.storage.StringKeyValueStoreFactory;
 import com.funambol.storage.Table;
 import com.funambol.storage.TableFactory;
 import com.funambol.storage.Tuple;
 import com.funambol.sync.SyncReport;
 import com.funambol.syncml.protocol.SyncML;
 import com.funambol.syncml.protocol.SyncMLStatus;
 import com.funambol.util.DateUtil;
 import com.funambol.util.Log;
 import com.funambol.util.StringUtil;
 
 /**
  * A class that saves and retrieves the mapping information from the store
  */
 public class SyncStatus implements SyncReport {
 
     private static final String TAG_LOG = "SyncStatus";
 
     /**
      * This is currently used to retrieve from / store on a Table
      * the values in a sync status that refer to individual items.
      */
     public final static boolean USES_TABLE = true;
     private static final String SYNC_STATUS_TABLE_PREFIX = "itemsyncstatuses_";
     private static TableFactory tableFactory = TableFactory.getInstance();
     private static boolean tableFactoryChanged = false;
     private Table table;
     private static String[] COLS_NAME = {
             "key",
             "guid",
             "mapped",
             "cmd",
             "stat"
     };
     private static int[] COLS_TYPE = {
             Table.TYPE_STRING,
             Table.TYPE_STRING,
             Table.TYPE_LONG,
             Table.TYPE_STRING,
             Table.TYPE_LONG
     };
 
     /**
      * This is currently used to retrieve from / store on a StringValueKeyStore
      * the values in a sync status that refer to the whole sync source.
      * Previously, this was also used for values related to individual items:
      * such values can be read once again in this obsolete way and then stored
      * in the new Table. After this sort of migration, such data will be
      * removed from the StringValueKeyStore.
      */
     private static final String SYNC_STATUS_STORE_PREFIX = "syncstatus_";
     private static StringKeyValueStoreFactory storeFactory = StringKeyValueStoreFactory.getInstance();
     private static boolean storeFactoryChanged = false;
     private StringKeyValueStore store;
 
     public static final int INIT_PHASE      = 0;
     public static final int SENDING_PHASE   = 1;
     public static final int RECEIVING_PHASE = 2;
     public static final int MAPPING_PHASE   = 3;
 
     private static final String REQUESTED_SYNC_MODE_KEY = "REQUESTED_SYNC_MODE";
     private static final String ALERTED_SYNC_MODE_KEY   = "ALERTED_SYNC_MODE";
     private static final String SYNC_PHASE_KEY          = "SYNC_PHASE";
     private static final String SENT_ITEM_KEY           = "SENT_ITEM_";
     private static final String RECEIVED_ITEM_KEY       = "RECEIVED_ITEM_";
     private static final String INTERRUPTED_KEY         = "INTERRUPTED";
     private static final String SESSIOND_ID_KEY         = "SESSION_ID";
     private static final String STATUS_CODE_KEY         = "STATUS_CODE";
     private static final String LAST_SYNC_START_TIME_KEY= "LAST_SYNC_START_TIME";
     private static final String LOC_URI_KEY             = "LOC_URI";
     private static final String REMOTE_URI_KEY          = "REMOTE_URI";
 
     private static final String TRUE                    = "TRUE";
     private static final String FALSE                   = "FALSE";
 
     private static final Long MAPPED = new Long(1L);
     private static final Long NOT_MAPPED = new Long(0L);
 
     private String sourceName;
 
     private int requestedSyncMode = -1;
     private int alertedSyncMode = -1;
     private String sessionId = null;
     private int oldRequestedSyncMode = -1;
     private int oldAlertSyncMode = -1;
     private String oldSessionId = null;
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
 
     private int initialReceivedAddNumber = 0;
     private int initialReceivedReplaceNumber = 0;
     private int initialReceivedDeleteNumber = 0;
     private int initialSentAddNumber = 0;
     private int initialSentReplaceNumber = 0;
     private int initialSentDeleteNumber = 0;
 
     public SyncStatus(String sourceName) {
         this.sourceName = sourceName;
 
         grantStore();
 
         grantTable();
     }
 
     private void grantStore() {
         if (storeFactoryChanged) {
             store = null;
             storeFactoryChanged = false;
         }
         if (store == null) {
             this.store = storeFactory.getStringKeyValueStore(SYNC_STATUS_STORE_PREFIX + sourceName);            
         } 
     }
 
     private void grantTable() {
         if (tableFactoryChanged) {
             table = null;
             tableFactoryChanged = false;
         }
         if (table == null) {
             String tableName = SYNC_STATUS_TABLE_PREFIX + sourceName;
             this.table = tableFactory.getStringTable(tableName, COLS_NAME, COLS_TYPE, 0);
         }
 
     }
 
 
     public int getRequestedSyncMode() {
         return requestedSyncMode;
     }
 
     public void setRequestedSyncMode(int requestedSyncMode) {
         oldRequestedSyncMode = this.requestedSyncMode;
         this.requestedSyncMode = requestedSyncMode;
     }
 
     public long getLastSyncStartTime() {
         return lastSyncStartTime;
     }
 
     public void setLastSyncStartTime(long lastSyncStartTime) {
         oldLastSyncStartTime = this.lastSyncStartTime;
         this.lastSyncStartTime = lastSyncStartTime;
     }
 
     public int getAlertedSyncMode() {
         return alertedSyncMode;
     }
 
     public void setAlertedSyncMode(int alertedSyncMode) {
         oldAlertSyncMode = this.alertedSyncMode;
         this.alertedSyncMode = alertedSyncMode;
     }
 
     public int getStatusCode() {
         return statusCode;
     }
 
     public void setStatusCode(int statusCode) {
         oldStatusCode = this.statusCode;
         this.statusCode = statusCode;
     }
 
     public void setSessionId(String sessionId) {
         oldSessionId = this.sessionId;
         this.sessionId = sessionId;
     }
 
     public String getSessionId() {
         return sessionId;
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
 
 
     public void addSentItem(String key, String cmd) {
         // The item was sent but a status has not been received yet
         SentItemStatus status = new SentItemStatus(cmd);
         pendingSentItems.put(key, status);
     }
 
     public int getSentItemsCount() {
         return sentItems.size() + pendingSentItems.size();
     }
 
     public Enumeration getSentItems() {
         if (pendingSentItems.size() == 0) {
             return sentItems.keys();
         } else if (sentItems.size() == 0) {
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
     public void addReceivedItem(String guid, String luid, String cmd, int statusCode) {
         ReceivedItemStatus status = new ReceivedItemStatus(guid, cmd);
         status.setStatus(statusCode);
         // If the pending received items already have this key, then we add
         // the current item with another key. This may happen for example if two
         // commands have the same key. When keys are used (e.g. calendar sync)
         // we may receive a delete for an item and then the successive add will
         // reuse the same key. The status for the delete is not used for the
         // mappings, so we can safely change its key. We still keep track of it
         // so that the total number of exchanged items is correct.
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
 
     // TODO: provide a meaningful implementation if needed
     public int getReceivedItemStatus(String guid) {
         return -1;
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
         oldInterrupted = this.interrupted;
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
             if (!status.getMapSent() && SyncML.TAG_ADD.equals(status.getCmd())) {
                 res.put(luid, status.getGuid());
             }
         }
         keys = pendingReceivedItems.keys();
         while(keys.hasMoreElements()) {
             String luid = (String)keys.nextElement();
             ReceivedItemStatus status = (ReceivedItemStatus)pendingReceivedItems.get(luid);
             if (!status.getMapSent() && SyncML.TAG_ADD.equals(status.getCmd())) {
                 res.put(luid, status.getGuid());
             }
         }
         return res;
     }
 
     /**
      * Loads the relevant values from the store and table.
      */
     public void load() throws IOException {
 
         // In any case loads from the store
         loadFromStore();
 
         if (USES_TABLE) {
             // Then loads from the table
             loadFromTable();
         }
         
         // Keeps track of how many items have been exchanged so far
         initialReceivedAddNumber = getTotalReceivedAddNumber();
         initialReceivedReplaceNumber = getTotalReceivedReplaceNumber();
         initialReceivedDeleteNumber = getTotalReceivedDeleteNumber();
         initialSentAddNumber = getTotalSentAddNumber();
         initialSentReplaceNumber = getTotalSentReplaceNumber();
         initialSentDeleteNumber = getTotalSentDeleteNumber();
     }
 
 
     /**
      * Loads the relevant values from the store.
      */
     protected void loadFromStore() throws IOException {
 
         grantStore();
         store.load();
         Enumeration keys = store.keys();
 
         while(keys.hasMoreElements()) {
             String key   = (String)keys.nextElement();
             String value = store.get(key);
 
             if (REQUESTED_SYNC_MODE_KEY.equals(key)) {
                 requestedSyncMode = Integer.parseInt(value);
             } else if (ALERTED_SYNC_MODE_KEY.equals(key)) {
                 alertedSyncMode = Integer.parseInt(value);
             } else if (INTERRUPTED_KEY.equals(key)) {
                 interrupted = TRUE.equals(value.toUpperCase());
             } else if (SESSIOND_ID_KEY.equals(key)) {
                 sessionId = value;
             } else if (key.startsWith(SENT_ITEM_KEY)) {
                 String itemKey = key.substring(SENT_ITEM_KEY.length());
                 // The value contains both the cmd and the status
                 String values[] = StringUtil.split(value, ",");
                 String cmd = values[0];
                 int    status = Integer.parseInt(values[1]);
                 SentItemStatus v = new SentItemStatus(cmd);
                 v.setStatus(status);
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
                 String cmd    = values[2];
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
      * Loads the relevant values from the table.
      */
     protected void loadFromTable() throws IOException {
 
         grantTable();
         table.open();
         
         final int GUID_COL = table.getColIndexOrThrow("guid");
         final int MAPPED_COL = table.getColIndexOrThrow("mapped");
         final int CMD_COL = table.getColIndexOrThrow("cmd");
         final int STAT_COL = table.getColIndexOrThrow("stat");
 
         QueryResult res = table.query();
         while (res.hasMoreElements()) {
             Tuple tuple = res.nextElement();
             DirectionAndKey dak =
                 new DirectionAndKey((String) tuple.getKey());
             String key = dak.getKey();
             String cmd = tuple.getStringField(CMD_COL);
             int stat = tuple.getLongField(STAT_COL).intValue();
             ItemStatus v;
             if (dak.isSentItem()) {
                 v = new SentItemStatus(cmd);
                 sentItems.put(key, v);
             } else { // it is received item
                 String guid = tuple.getStringField(GUID_COL);
                 boolean mapped =
                    (tuple.getLongField(MAPPED_COL) == NOT_MAPPED);
                 ReceivedItemStatus received =
                     new ReceivedItemStatus(guid, cmd);
                 received.setMapSent(mapped);
                 v = received;
                 receivedItems.put(key, v);
             }
             v.setStatus(stat);
         }
 
         table.close();
         
     }
 
     /**
      * Replace the current mappings with the new one and persist the info
      *
      * @param mappings the mapping hashtable
      */
     public void save() throws IOException {
 
         if (USES_TABLE) {
             saveOnTable();
         }
         
         saveOnStore();
     }
 
     /**
      * Replace the current mappings with the new one and persist the info
      *
      * @param mappings the mapping hashtable
      */
     protected void saveOnTable() throws IOException {
 
         grantTable();
         table.open();
 
         final int GUID_COL = table.getColIndexOrThrow("guid");
         final int MAPPED_COL = table.getColIndexOrThrow("mapped");
         final int CMD_COL = table.getColIndexOrThrow("cmd");
         final int STAT_COL = table.getColIndexOrThrow("stat");
 
         // We save only what changed, nothing more
         Enumeration keys = pendingSentItems.keys();
         while(keys.hasMoreElements()) {
             String key = (String) keys.nextElement();
             SentItemStatus status = (SentItemStatus) pendingSentItems.get(key);
             DirectionAndKey dak = new DirectionAndKey(true, key);
             Tuple tuple = table.createNewRow(dak.getCompactForm());
             tuple.setField(GUID_COL, "N/A");
             tuple.setField(MAPPED_COL, -1);
             tuple.setField(CMD_COL, status.getCmd());
             tuple.setField(STAT_COL, status.getStatus());
             table.update(tuple);
 
             // Now move this item into the in memory values
             sentItems.put(key, status);
         }
 
         // We save only what changed, nothing more
         keys = pendingReceivedItems.keys();
         while(keys.hasMoreElements()) {
             String key = (String) keys.nextElement();
             ReceivedItemStatus status = (ReceivedItemStatus) pendingReceivedItems.get(key);
             DirectionAndKey dak = new DirectionAndKey(false, key);
             Tuple tuple = table.createNewRow(dak.getCompactForm());
             tuple.setField(GUID_COL, status.getGuid());
             tuple.setField(MAPPED_COL, (status.getMapSent() ? MAPPED : NOT_MAPPED));
             tuple.setField(CMD_COL, status.getCmd());
             tuple.setField(STAT_COL, status.getStatus());
             table.update(tuple);
 
             // Now move this item into the in memory values
             receivedItems.put(key, status);
         }
         pendingSentItems.clear();
         pendingReceivedItems.clear();
 
         table.save();
         table.close();
 
     }
 
     /**
      * Replace the current mappings with the new one and persist the info
      *
      * @param mappings the mapping hashtable
      */
     protected void saveOnStore() throws IOException {
 
         grantStore();
 
         // We save only what changed, nothing more
         Enumeration keys = pendingSentItems.keys();
         while(keys.hasMoreElements()) {
             String key    = (String)keys.nextElement();
             SentItemStatus status = (SentItemStatus)pendingSentItems.get(key);
 
             StringBuffer v = new StringBuffer();
             v.append(status.getCmd()).append(",").append(status.getStatus());
 
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
 
         if (oldAlertSyncMode != alertedSyncMode) {
             if (oldAlertSyncMode == -1) {
                 store.add(ALERTED_SYNC_MODE_KEY, "" + alertedSyncMode);
             } else {
                 store.update(ALERTED_SYNC_MODE_KEY, "" + alertedSyncMode);
             }
             oldAlertSyncMode = alertedSyncMode;
         }
 
         if (oldSessionId != sessionId) {
             if (oldSessionId == null) {
                 store.add(SESSIOND_ID_KEY, sessionId);
             } else {
                 store.update(SESSIOND_ID_KEY, sessionId);
             }
             oldSessionId = sessionId;
         }
 
         if (locUri != oldLocUri) {
             if (oldLocUri == null) {
                 store.add(LOC_URI_KEY, locUri);
             } else {
                 store.update(LOC_URI_KEY, locUri);
             }
             oldLocUri = locUri;
         }
 
         if (remoteUri != oldRemoteUri) {
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
 
         if (interrupted != oldInterrupted) {
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
         if (store != null) {
             store.reset();
         }
         if (table != null) {
             table.open();
             table.reset();
             table.close();
         }
         init();
     }
 
     /**
      * Partially reset the status. This method clears the persisted info and the
      * set of exchanged data, but leaves intact other information such as the
      * sessionId, the alert code and so on. This method is meant to be used when
      * the server refuses the resume and we shall start exchanging from scratch.
      */
     public void resetExchangedItems() throws IOException {
 
         store.reset();
 
         table.open();
         table.reset();
         table.close();
         
         // Reset info about the exchanged data
         sentItems.clear();
         receivedItems.clear();
         pendingSentItems.clear();
         pendingReceivedItems.clear();
         initialReceivedAddNumber = 0;
         initialReceivedReplaceNumber = 0;
         initialReceivedDeleteNumber = 0;
         initialSentAddNumber = 0;
         initialSentReplaceNumber = 0;
         initialSentDeleteNumber = 0;
     }
 
     public int getTotalReceivedAddNumber() {
         int v1 = getItemsNumber(receivedItems, SyncML.TAG_ADD);
         int v2 = getItemsNumber(pendingReceivedItems, SyncML.TAG_ADD);
         return v1 + v2;
     }
 
     public int getReceivedAddNumber() {
         return getTotalReceivedAddNumber() - initialReceivedAddNumber;
     }
 
     public int getTotalReceivedReplaceNumber() {
         int v1 = getItemsNumber(receivedItems, SyncML.TAG_REPLACE);
         int v2 = getItemsNumber(pendingReceivedItems, SyncML.TAG_REPLACE);
         return v1 + v2;
     }
 
     public int getReceivedReplaceNumber() {
         return getTotalReceivedReplaceNumber() - initialReceivedReplaceNumber;
     }
 
     public int getTotalReceivedDeleteNumber() {
         int v1 = getItemsNumber(receivedItems, SyncML.TAG_DELETE);
         int v2 = getItemsNumber(pendingReceivedItems, SyncML.TAG_DELETE);
         return v1 + v2;
     }
 
     public int getReceivedDeleteNumber() {
         return getTotalReceivedDeleteNumber() - initialReceivedDeleteNumber;
     }
 
     public int getTotalSentAddNumber() {
         int v1 = getItemsNumber(sentItems, SyncML.TAG_ADD);
         int v2 = getItemsNumber(pendingSentItems, SyncML.TAG_ADD);
         return v1 + v2;
     }
 
     public int getSentAddNumber() {
         return getTotalSentAddNumber() - initialSentAddNumber;
     }
 
 
     public int getTotalSentReplaceNumber() {
         int v1 = getItemsNumber(sentItems, SyncML.TAG_REPLACE);
         int v2 = getItemsNumber(pendingSentItems, SyncML.TAG_REPLACE);
         return v1 + v2;
     }
 
     public int getSentReplaceNumber() {
         return getTotalSentReplaceNumber() - initialSentReplaceNumber;
     }
 
     public int getTotalSentDeleteNumber() {
         int v1 = getItemsNumber(sentItems, SyncML.TAG_DELETE);
         int v2 = getItemsNumber(pendingSentItems, SyncML.TAG_DELETE);
         return v1 + v2;
     }
 
     public int getSentDeleteNumber() {
         return getTotalSentDeleteNumber() - initialSentDeleteNumber;
     }
 
     public int getNumberOfReceivedItemsWithError() {
         return getNumberOfItemsWithError(receivedItems) + getNumberOfItemsWithError(pendingReceivedItems);
     }
 
     public int getNumberOfSentItemsWithError() {
         return getNumberOfItemsWithError(sentItems) + getNumberOfItemsWithError(pendingSentItems);
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
         res.append("| Requested sync mode: ").append(requestedSyncMode)
            .append(" - Alerted sync mode:").append(alertedSyncMode).append("\n");
         res.append("|-----------------------------------------------------------------\n");
         res.append("| Total changes received from server\n");
         res.append("|-----------------------------------------------------------------\n");
         res.append("| Add: ").append(getTotalReceivedAddNumber()).append("\n");
         res.append("| Replace: ").append(getTotalReceivedReplaceNumber()).append("\n");
         res.append("| Delete: ").append(getTotalReceivedDeleteNumber()).append("\n");
         res.append("| Total errors: ").append(getNumberOfReceivedItemsWithError()).append("\n");
         res.append("|-----------------------------------------------------------------\n");
         res.append("| Total changes sent to server\n");
         res.append("|-----------------------------------------------------------------\n");
         res.append("| Add: ").append(getTotalSentAddNumber()).append("\n");
         res.append("| Replace: ").append(getTotalSentReplaceNumber()).append("\n");
         res.append("| Delete: ").append(getTotalSentDeleteNumber()).append("\n");
         res.append("| Total errors: ").append(getNumberOfSentItemsWithError()).append("\n");
         res.append("|-----------------------------------------------------------------\n");
         res.append("| Changes received from server in this sync\n");
         res.append("|-----------------------------------------------------------------\n");
         res.append("| Add: ").append(getReceivedAddNumber()).append("\n");
         res.append("| Replace: ").append(getReceivedReplaceNumber()).append("\n");
         res.append("| Delete: ").append(getReceivedDeleteNumber()).append("\n");
         res.append("|-----------------------------------------------------------------\n");
         res.append("| Changes sent to server in this sync\n");
         res.append("|-----------------------------------------------------------------\n");
         res.append("| Add: ").append(getSentAddNumber()).append("\n");
         res.append("| Replace: ").append(getSentReplaceNumber()).append("\n");
         res.append("| Delete: ").append(getSentDeleteNumber()).append("\n");
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
         SyncStatus.storeFactory = factory;
         storeFactoryChanged = true;
     }
 
     /**
      * This method is mainly intended for testing. It allows to use a store
      * factory different from the platform standard one.
      */
     public static void setTableFactory(TableFactory factory) {
         SyncStatus.tableFactory = factory;
         tableFactoryChanged = true;
     }
     
     private void init(){
         requestedSyncMode = -1;
         alertedSyncMode = -1;
         sessionId = null;
         oldRequestedSyncMode = -1;
         oldAlertSyncMode = -1;
         oldSessionId = null;
         statusCode = -1;
         oldStatusCode = -1;
         sentItems.clear();
         receivedItems.clear();
         pendingSentItems.clear();
         pendingReceivedItems.clear();
         sentResumedItems.removeAllElements();
         receivedResumedItems.removeAllElements();
         locUri = null;
         remoteUri = null;
         se = null;
         startTime = 0;
         endTime = 0;
         initialReceivedAddNumber = 0;
         initialReceivedReplaceNumber = 0;
         initialReceivedDeleteNumber = 0;
         initialSentAddNumber = 0;
         initialSentReplaceNumber = 0;
         initialSentDeleteNumber = 0;
         interrupted = false;
         oldInterrupted = false;
     }
 
     private int getItemsNumber(Hashtable table, String cmd) {
         int count = 0;
         Enumeration keys = table.keys();
         while (keys.hasMoreElements()) {
             String key = (String)keys.nextElement();
             ItemStatus status = (ItemStatus)table.get(key);
             if (cmd.equals(status.getCmd())) {
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
             if (!SyncMLStatus.isSuccess(status.getStatus())) {
                 count++;
             }
         }
         return count;
     }
 
     private class ItemStatus {
 
         public static final int UNDEFINED_STATUS = -1;
         protected String cmd;
         protected int status = UNDEFINED_STATUS;
 
         public ItemStatus(String cmd) {
             this.cmd = cmd;
         }
 
         public void setStatus(int status) {
             this.status = status;
         }
 
         public int getStatus() {
             return status;
         }
 
         public String getCmd() {
             return cmd;
         }
     }
 
     private class ReceivedItemStatus extends ItemStatus {
         private String  guid;
         private boolean mapSent;
         private int status;
 
         public ReceivedItemStatus(String guid, String cmd) {
             super(cmd);
             this.guid = guid;
         }
 
         public void setMapSent(boolean value) {
             mapSent = value;
         }
 
         public String getGuid() {
             return guid;
         }
 
         public boolean getMapSent() {
             return mapSent;
         }
     }
 
     private class SentItemStatus extends ItemStatus {
 
         public SentItemStatus(String cmd) {
             super(cmd);
         }
     }
 
     private class DirectionAndKey {
 
         private boolean isSent;
         private String key;
 
         public DirectionAndKey(String directionAndKey) {
             if (directionAndKey.startsWith(SENT_ITEM_KEY)) {
                 this.isSent = true;
                 this.key = directionAndKey.substring(SENT_ITEM_KEY.length());
             } else if (directionAndKey.startsWith(RECEIVED_ITEM_KEY)) {
                 this.isSent = false;
                 this.key = directionAndKey.substring(RECEIVED_ITEM_KEY.length());
             } else {
                 throw new IllegalArgumentException(
                         "Compact key \"" + directionAndKey + 
                         "\" not OK: it must start with either " + RECEIVED_ITEM_KEY + 
                         " or " + SENT_ITEM_KEY);
             }
         }
 
         public DirectionAndKey(boolean direction, String key) {
             this.isSent = direction;
             this.key = key;
         }
 
         public boolean isSentItem() {
             return isSent;
         }
 
         public boolean isReceivedItem() {
             return !isSentItem();
         }
 
         public String getKey() {
             return key;
         }
 
         public String getCompactForm() {
             return (isSent ? SENT_ITEM_KEY : RECEIVED_ITEM_KEY) + key;
         }
 
     }
 
 
 }
