 /*
  * Funambol is a mobile platform developed by Funambol, Inc.
  * Copyright (C) 2008 Funambol, Inc.
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
 
 package com.funambol.sync.client;
 
 import java.util.Hashtable;
 import java.util.Vector;
 import java.util.Enumeration;
 import java.io.IOException;
 import java.io.InputStream;
 
 import com.funambol.util.MD5;
 import com.funambol.util.Base64;
 import com.funambol.util.Log;
 import com.funambol.storage.StringKeyValueStore;
 import com.funambol.sync.SyncItem;
 import com.funambol.sync.SyncSource;
 import com.funambol.sync.SyncException;
 import com.funambol.sync.ItemStatus;
 
 /**
  * This class implements a ChangesTracker and it is based on comparison
  * of fingerprints. This means that the class can take
  * a snapshot of the SyncSource and store it in a StringKeyValueStore (a parameter
  * the client must provide). For each item in the SyncSource its
  * fingerprint is stored in the store.
  * When getNewItems, getUpdatedItems and getDeletedItems are invoked, they
  * compare the SyncSource current state and the last snapshot and detect
  * changes.
  * By default MD5 is used to compute fingerprints, but the method can be
  * redefined if a client wants to use a different method.
  */
 public class CacheTracker implements ChangesTracker {
 
     private static final String TAG_LOG = "CacheTracker";
 
     protected Hashtable newItems;
     protected Hashtable deletedItems;
     protected Hashtable updatedItems;
     protected TrackableSyncSource ss;
     protected StringKeyValueStore status;
     
     protected int syncMode;
 
     /**
      * Creates a CacheTracker. The constructor detects changes so that
      * the method to get the changes can be used right away
      *
      * @param status is the key value store with stored data
      */
     public CacheTracker(StringKeyValueStore status) {
         this.status = status;
     }
 
     /**
      * Associates this tracker to the given sync source
      *
      * @param ss the sync source
      */
     public void setSyncSource(TrackableSyncSource ss) {
         this.ss = ss;
     }
 
     /**
      * This method cleans any pending change. In the cache sync source
      * this means that the fingerprint of each item is updated to its current
      * value. The fingerprint tables will contain exactly the same items that
      * are currently in the Sync source.
      */
     public void reset() throws TrackerException {
         //first of all, reset the status
         try{
             status.reset();
         } catch (Exception e){
             throw new TrackerException(e.toString());
         }
 
         //then, add current files fingerprint
         Hashtable snapshot;
         try {
             snapshot = getAllFilesFingerprint();
         } catch (SyncException e) {
             throw new TrackerException(e.toString());
         }
 
         // Now compute the three lists
         Enumeration snapshotKeys = snapshot.keys();
         // Detect new items and updated items
         while (snapshotKeys.hasMoreElements()) {
             String newKey = (String)snapshotKeys.nextElement();
             status.add(newKey, snapshot.get(newKey).toString());
         }
 
         //finally, save the status
         try {
             this.status.save();
         } catch (Exception e) {
             Log.error(TAG_LOG, "Cannot save tracker status");
             throw new TrackerException(e.toString());
         }
     }
 
     public void begin(int syncMode, boolean reset) throws TrackerException {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "begin");
         }
 
         this.syncMode = syncMode;
         
         newItems      = new Hashtable();
         updatedItems  = new Hashtable();
         deletedItems  = new Hashtable();
 
         if(syncMode == SyncSource.INCREMENTAL_SYNC ||
            syncMode == SyncSource.INCREMENTAL_UPLOAD) {
 
             Hashtable snapshot;
             try {
                 snapshot = getAllFilesFingerprint();
             } catch (SyncException e) {
                 Log.error(TAG_LOG, "Cannot compute fingerprint for items ", e);
                 throw new TrackerException(e.toString());
             }
 
             // Initialize the status by loading its content
             try {
                 this.status.load();
             } catch (Exception e) {
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Cannot load tracker status, create an empty one");
                 }
                 try {
                     this.status.save();
                 } catch (Exception e1) {
                     Log.error(TAG_LOG, "Cannot save tracker status");
                     throw new TrackerException(e.toString());
                 }
             }
             
             // Now compute the three lists
             Enumeration snapshotKeys = snapshot.keys();
             // Detect new items and updated items
             while (snapshotKeys.hasMoreElements()) {
                 String newKey = (String)snapshotKeys.nextElement();
                 if (status.get(newKey) == null) {
                     if (Log.isLoggable(Log.TRACE)) {
                         Log.trace(TAG_LOG, "Found a new item with key: " + newKey);
                     }
                     newItems.put(newKey, snapshot.get(newKey));
                 } else {
                     // Check if their fingerprints are the same
                     String oldFP = (String)this.status.get(newKey);
                     String newFP = (String)snapshot.get(newKey);
                     if (!oldFP.equals(newFP)) {
                         if (Log.isLoggable(Log.TRACE)) {
                             Log.trace(TAG_LOG, "Found an updated item with key: " + newKey);
                         }
                         updatedItems.put(newKey, newFP);
                     }
                 }
             }
             // Detect deleted items
             Enumeration statusKeys = this.status.keys();
             while (statusKeys.hasMoreElements()) {
                 String oldKey = (String)statusKeys.nextElement();
                 if (snapshot.get(oldKey) == null) {
                     if (Log.isLoggable(Log.TRACE)) {
                         Log.trace(TAG_LOG, "Found a deleted item with key: " + oldKey);
                     }
                     deletedItems.put(oldKey, (String)status.get(oldKey));
                 }
             }
             
             //now apply filters to items
             filterOutOutgoingItems(newItems);
             filterOutOutgoingItems(updatedItems);
             filterOutOutgoingItems(deletedItems);
             
         } else if(syncMode == SyncSource.FULL_SYNC ||
                   syncMode == SyncSource.FULL_UPLOAD ||
                   syncMode == SyncSource.FULL_DOWNLOAD) {
             // Reset the status when performing a slow sync
             try {
                 status.reset();
             } catch(IOException ex) {
                 Log.error(TAG_LOG, "Cannot reset status", ex);
                 throw new TrackerException("Cannot reset status");
             }
         }
     }
 
     public void end() throws TrackerException {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "end");
         }
         // We must update the data store
         try {
             status.save();
         } catch (IOException ioe) {
             Log.error(TAG_LOG, "Cannot save cache data store");
             throw new TrackerException("Cannot save cache data store");
         }
 
         // Allow the GC to pick this memory
         newItems      = null;
         updatedItems  = null;
         deletedItems  = null;
     }
 
     /**
      * Returns the list of new items.
      * @return the list of new items as an Enumeration
      *         of SyncItem
      */
     public Enumeration getNewItems() throws TrackerException {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "getNewItems");
         }
         // Any item in the sync source which is not part of the
         // old state is a new item
         return getItemsKeys(newItems);
     }
 
     /**
      * Returns the number of new items that will be returned by the getNewItems
      * method
      *
      * @return the number of items
      */
     public int getNewItemsCount() throws TrackerException {
         return getItemsCount(newItems);
     }
 
 
 
     /**
      * Returns the list of updated items.
      * @return the list of updated items as an Enumeration
      *         of SyncItem
      */
     public Enumeration getUpdatedItems() throws TrackerException {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "getUpdatedItems");
         }
         // Any item whose fingerprint has changed is a new item
         return getItemsKeys(updatedItems);
     }
 
     /**
      * Returns the number of deleted items that will be returned by the getDeletedItems
      * method
      *
      * @return the number of items
      */
     public int getUpdatedItemsCount() throws TrackerException {
         return getItemsCount(updatedItems);
     }
 
 
     /**
      * Returns the list of deleted items.
      * @return the list of updated items as an Enumeration
      *         of strings (SyncItem's keys)
      */
     public Enumeration getDeletedItems() throws TrackerException {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "getDeletedItems");
         }
         return getItemsKeys(deletedItems);
     }
 
     /**
      * Returns the number of deleted items that will be returned by the getDeletedItems
      * method
      *
      * @return the number of items
      */
     public int getDeletedItemsCount() throws TrackerException {
         return getItemsCount(deletedItems);
     }
 
     public void setItemsStatus(Vector itemsStatus) throws TrackerException {
         for(int i=0;i<itemsStatus.size();++i) {
             ItemStatus itemStatus = (ItemStatus)itemsStatus.elementAt(i);
             String key = itemStatus.getKey();
             int status = itemStatus.getStatus();
             setItemStatus(key, status);
         }
 
     }
 
     protected void setItemStatus(String key, int itemStatus) throws TrackerException {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "setItemStatus " + key + "," + itemStatus);
         }
 
         if(isSuccess(itemStatus) && (syncMode == SyncSource.FULL_SYNC ||
                 syncMode == SyncSource.FULL_UPLOAD)) {
             SyncItem item = new SyncItem(key);
             try {
                 item = getItemContent(item);
             } catch(SyncException ex) {
                 throw new TrackerException(ex.toString());
             }
             if(status.get(key) != null) {
                 status.update(key, computeFingerprint(item));
             } else {
                 status.add(key, computeFingerprint(item));
             }
         } else if (isSuccess(itemStatus) && itemStatus != SyncSource.CHUNK_SUCCESS_STATUS) {
             // We must update the fingerprint store with the value of the
             // fingerprint at the last sync
             if (newItems.get(key) != null) {
                 // This is a new item
                 String itemFP = (String)newItems.get(key);
                 // Update the fingerprint
                 status.add(key, itemFP);
             } else if (updatedItems.get(key) != null) {
                 // This is a new item
                 String itemFP = (String)updatedItems.get(key);
                 // Update the fingerprint
                 status.update(key, itemFP);
             } else if (deletedItems.get(key) != null) {
                 // Update the fingerprint
                 status.remove(key);
             }
             // Save the status after each item
             try {
                 this.status.save();
             } catch (Exception e) {
                 // We try to let this error go trough as we save the status at
                 // the end of the sync. Even though it is likely that operation
                 // will fail as well and an exception will be thrown
                 Log.error(TAG_LOG, "Cannot save tracker status, the status will be written at the end");
             }
         } else {
             // On error we do not change the fp so the change will
             // be reconsidered at the next sync
         }
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "status set for item: " + key);
         }
     }
 
     protected String computeFingerprint(SyncItem item) {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "computeFingerprint");
         }
         // We don't want to load the entire item in memory, but we rather
         // compute the MD5 chunk by chunk and recursively compute the MD5
         final int CHUNK_SIZE = 256 * 1024;
         MD5 md5 = new MD5();
         StringBuffer md5Sequence = new StringBuffer();
         byte chunk[] = new byte[CHUNK_SIZE];
         InputStream is = null;
         try {
             is = item.getInputStream();
             int actualSize = is.read(chunk);
             byte fp[] = md5.calculateMD5(chunk);
             byte[] fpB64 = Base64.encode(fp);
             md5Sequence.append(new String(fpB64));
 
             while (actualSize == CHUNK_SIZE) {
                 actualSize = is.read(chunk);
                 if (actualSize > 0) {
                     fp = md5.calculateMD5(chunk);
                     fpB64 = Base64.encode(fp);
                     md5Sequence.append(new String(fpB64));
 
                     if (md5Sequence.length() > CHUNK_SIZE) {
                         String val = md5Sequence.toString();
                         fp = md5.calculateMD5(val.getBytes());
                         fpB64 = Base64.encode(fp);
                         md5Sequence = new StringBuffer();
                         md5Sequence.append(new String(fpB64));
                     }
                 }
             }
             
             // Finally compute the MD5 of md5sequence
             String val = md5Sequence.toString();
             fp = md5.calculateMD5(val.getBytes());
             fpB64 = Base64.encode(fp);
             return new String(fpB64);
         } catch (IOException ioe) {
             Log.error(TAG_LOG, "Cannot compute fingerprint " + ioe.toString());
             return "";
         } finally {
             try {
                 // Close the stream
                 if (is != null) {
                     is.close();
                 }
             } catch (IOException ioe) {
             }
         }
 
     }
 
     /**
      * Create an hashtable with all files and their fingerprints
      * @return
      * @throws SyncException
      */
     protected Hashtable getAllFilesFingerprint()
             throws SyncException
     {
         Enumeration allItemsKeys = ss.getAllItemsKeys();
         Hashtable snapshot = new Hashtable();
 
         while (allItemsKeys.hasMoreElements()) {
             String key = (String) allItemsKeys.nextElement();
             SyncItem item = new SyncItem(key);
             item = getItemContent(item);
             // Compute the fingerprint for this item
             if (Log.isLoggable(Log.TRACE)) {
                 Log.trace(TAG_LOG, "Computing fingerprint for " + item.getKey());
             }
             String fp = computeFingerprint(item);
             if (Log.isLoggable(Log.TRACE)) {
                 Log.trace(TAG_LOG, "Fingerpint is: " + fp);
             }
             
             // Store the fingerprint for this item
             snapshot.put(item.getKey(), fp);
         }
 
         return snapshot;
     }
 
     protected boolean isSuccess(int status) {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "isSuccess " + status);
         }
         return status == SyncSource.SUCCESS_STATUS || status == SyncSource.CHUNK_SUCCESS_STATUS;
     }
 
     public boolean removeItem(SyncItem item) throws TrackerException {
         // In a cache sync source an item is removed from the cache
         // if it actually part of the cache. In such a case it will not
         // be reported as a new item
         String fp;
         boolean res = true;
         switch (item.getState()) {
             case SyncItem.STATE_NEW:
                 try {
                     // We need the item as it has been stored on the device and
                     // not the original one. If we compute the fingerpring on
                     // the original item, we may detect an update at the next
                     // sync if the device does not support/store all fields
                     SyncItem deviceItem = new SyncItem(item.getKey());
                     deviceItem = getItemContent(deviceItem);
                     fp = computeFingerprint(deviceItem);
                     status.add(item.getKey(), fp);
                 } catch(SyncException ex) {
                     throw new TrackerException(ex.toString());
                 }
                 break;
             case SyncItem.STATE_UPDATED:
                 try {
                     // We need the item as it has been stored on the device and
                     // not the original one. If we compute the fingerpring on
                     // the original item, we may detect an update at the next
                     // sync if the device does not support/store all fields
                     SyncItem deviceItem = new SyncItem(item.getKey());
                     deviceItem = getItemContent(deviceItem);
                     fp = computeFingerprint(deviceItem);
                     status.update(item.getKey(), fp);
                 } catch(SyncException ex) {
                     throw new TrackerException(ex.toString());
                 }
                 break;
             case SyncItem.STATE_DELETED:
                 status.remove(item.getKey());
                 break;
             default:
                 Log.error(TAG_LOG, "Cache Tracker cannot remove item");
                 res = false;
         }
         return res;
     }
 
     public void empty() throws TrackerException {
         try {
             status.reset();
         } catch (Exception ioe) {
             Log.error(TAG_LOG, "Cannot empty cache tracker ", ioe);
             throw new TrackerException("Cannot empty cache tracker");
         }
     }
 
     public boolean supportsResume() {
         return false;
     }
 
     public boolean hasChangedSinceLastSync(String key, long ts) {
         return true;
     }
 
     protected SyncItem getItemContent(SyncItem item) throws SyncException {
         return ss.getItemContent(item);
     }
     
     
     private Enumeration getItemsKeys(Hashtable items) {
         return items != null ? items.keys() : null;
     }
     
     private int getItemsCount(Hashtable items) {
         return items != null ? items.size() : 0;
     }
 
     /**
      * Takes lists of items (generally, new, updated and deleted items) and
      * remove the items that doen't match filter criteria
      * 
      *  @param itemsToFilterOut
      */
     private void filterOutOutgoingItems(Hashtable itemsToFilterOut) {
         if (null == itemsToFilterOut || 0 == itemsToFilterOut.size()) return;
         
         Vector keysToRemove = new Vector(); 
 
         //detects items to filter out
         Enumeration keys = itemsToFilterOut.keys();
         while (keys.hasMoreElements()) {
             String key = (String)keys.nextElement();
             boolean filteredOut = filterItem(key);
             if (filteredOut) {
                 if (Log.isLoggable(Log.TRACE)) {
                     Log.trace(TAG_LOG, "Item with key " + key + " was filtered out");
                 }
                keysToRemove.add(key);
             }
         }
         
         //remote these items from the list
         for (int i=0; i<keysToRemove.size(); i++) {
            String keyToRemove = (String)keysToRemove.get(i);
             itemsToFilterOut.remove(keyToRemove);
         }
     }
     
     /**
      * Delegates to SyncSource all the logic for detecting items
      * to filter out
      */
     public boolean filterItem(String key) {
         return ss.filterOutgoingItem(key);
     }
 
 }
 
