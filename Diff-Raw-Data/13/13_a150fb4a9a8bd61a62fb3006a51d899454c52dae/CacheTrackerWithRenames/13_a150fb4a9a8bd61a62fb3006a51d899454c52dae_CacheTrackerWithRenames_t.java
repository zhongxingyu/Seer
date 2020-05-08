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
 
 package com.funambol.sapisync.source;
 
 import com.funambol.sync.client.CacheTracker;
 import com.funambol.storage.StringKeyValueStore;
 import com.funambol.storage.StringKeyValueStoreFactory;
 import com.funambol.sync.SyncItem;
 import com.funambol.sync.SyncSource;
 import com.funambol.sync.client.TrackerException;
 import com.funambol.util.Log;
 
 import java.io.IOException;
 import java.util.Enumeration;
 
 
 /**
  * Represents a CacheTracker which is able to detect rename operation within the
  * retrieved changes.
  */
 public class CacheTrackerWithRenames extends CacheTracker implements FileRenameListener {
 
     private static final String TAG_LOG = "CacheTrackerWithRenames";
 
     private StringKeyValueStore renamesStore;
 
     public CacheTrackerWithRenames(String sourceName, StringKeyValueStore statusStore) {
         super(statusStore);
         StringKeyValueStoreFactory storeFactory =
                 StringKeyValueStoreFactory.getInstance();
         this.renamesStore = storeFactory.getStringKeyValueStore(
                 "renames_" + sourceName);
     }
 
     public CacheTrackerWithRenames(StringKeyValueStore renamesStore, StringKeyValueStore statusStore) {
         super(statusStore);
         this.renamesStore = renamesStore;
     }
 
     /**
      * @see FileRenameListener#fileRenamed(java.lang.String, java.lang.String) 
      */
     public void fileRenamed(String oldFileName, String newFileName) {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "fileRenamed. oldFileName=" + oldFileName +
                     " newFileName=" + newFileName);
         }
         try {
             // The new file name is the key
             // If the renames store already contains this file as renamed we
             // update the new file name
             if (renamesStore.get(oldFileName) != null) {
                 String savedOldFileName = renamesStore.get(oldFileName);
                 renamesStore.remove(oldFileName);
                 oldFileName = savedOldFileName;
             }
            // If the renamed file was never synchronized we can ignore it
            if(!status.contains(oldFileName)) {
                Log.debug(TAG_LOG, "Ignoring renamed file: " + oldFileName +
                        " becouse it was never synchronized");
                return;
            }
             if(!newFileName.equals(oldFileName)) {
                 renamesStore.add(newFileName, oldFileName);
             }
             renamesStore.save();
         } catch(IOException ex) {
             Log.error(TAG_LOG, "Cannot track rename event: " + newFileName);
         }
     }
 
     public void begin(int syncMode, boolean reset) throws TrackerException {
         super.begin(syncMode, reset);
         try {
             renamesStore.load();
         } catch (Exception e) {
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "Cannot load renames status, create an empty one");
             }
             try {
                 this.renamesStore.save();
             } catch (Exception e1) {
                 Log.error(TAG_LOG, "Cannot save renames status");
                 throw new TrackerException(e.toString());
             }
         }
         // Start detecting renames
         if(Log.isLoggable(Log.DEBUG)) {
             Log.debug(TAG_LOG, "Checking renames");
         }
         Enumeration keys = renamesStore.keys();
         while(keys.hasMoreElements()) {
             String newFileName = (String)keys.nextElement();
             String oldFileName = (String)renamesStore.get(newFileName);
             if(newItems.get(newFileName) != null && deletedItems.get(oldFileName) != null) {
                 if(Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Detected file rename. "
                             + "oldFileName=" + oldFileName + " newFileName=" + newFileName);
                 }
                 // A rename operation (add + delete) shall be converted into an update
                 String fp = (String)newItems.get(newFileName);
                 newItems.remove(newFileName);
                 deletedItems.remove(oldFileName);
                 updatedItems.put(newFileName, fp);
             }
         }
     }
 
     public String getRenamedFileName(String newFileName) {
         return renamesStore.get(newFileName);
     }
 
     public boolean isRenamedItemUpdated(String oldKey, String newKey) {
         String oldFP = status.get(oldKey);
         String newFP = (String)updatedItems.get(newKey);
         boolean updated = true;
         if(oldFP != null && newFP != null) {
             updated = !oldFP.equals(newFP);
         }
         return updated;
     }
 
     public boolean isRenamedItem(String key) {
         return renamesStore.get(key) != null;
     }
 
     public void end() throws TrackerException {
         super.end();
         try {
             renamesStore.save();
         } catch (IOException ioe) {
             Log.error(TAG_LOG, "Cannot save renames status");
             throw new TrackerException("Cannot save renames status");
         }
     }
 
     protected void setItemStatus(String key, int itemStatus) throws TrackerException {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "setItemStatus " + key + "," + itemStatus);
         }
         boolean renameStatusHandled = false;
         if (isSuccess(itemStatus) && itemStatus != SyncSource.CHUNK_SUCCESS_STATUS) {
             if (renamesStore.get(key) != null) {
                 // Update the base tracker status
                 String deletedKey = renamesStore.get(key);
                 removeItem(new SyncItem(key, null, SyncItem.STATE_NEW, null));
                 removeItem(new SyncItem(deletedKey, null, SyncItem.STATE_DELETED, null));
                 // Update the renames status
                 renamesStore.remove(key);
                 renameStatusHandled = true;
             }
             try {
                 renamesStore.save();
             } catch (Exception e) {
                 Log.error(TAG_LOG, "Cannot save renames status");
             }
         }
         if(!renameStatusHandled) {
             super.setItemStatus(key, itemStatus);
         }
     }
 
     public void reset() throws TrackerException {
         super.reset();
         resetRenamesStore();
     }
 
     public void empty() throws TrackerException {
         super.empty();
         resetRenamesStore();
     }
 
     private void resetRenamesStore() throws TrackerException {
         try {
             renamesStore.reset();
         } catch (Exception ioe) {
             Log.error(TAG_LOG, "Cannot reset renames store", ioe);
             throw new TrackerException("Cannot reset renames store");
         }
     }
     
 }
 
 
