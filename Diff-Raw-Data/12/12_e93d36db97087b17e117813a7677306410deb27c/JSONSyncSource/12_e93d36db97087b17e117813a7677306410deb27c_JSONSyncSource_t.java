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
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import com.funambol.sync.SyncItem;
 import com.funambol.sync.SourceConfig;
 import com.funambol.sync.SyncException;
 import com.funambol.sync.ItemDownloadInterruptionException;
 import com.funambol.sync.SyncSource;
 import com.funambol.sync.client.ChangesTracker;
 import com.funambol.sync.client.TrackableSyncSource;
 import com.funambol.sapisync.source.util.HttpDownloader;
 import com.funambol.sync.Filter;
 import com.funambol.sync.SyncConfig;
 import com.funambol.sync.SyncListener;
 import com.funambol.sync.SyncReport;
 import com.funambol.util.Log;
 import com.funambol.util.StringUtil;
 
 import org.json.me.JSONObject;
 import org.json.me.JSONException;
 
 /**
  * Represents a SyncSource which handles JSON file objects as input SyncItems.
  * You should define the getDownloadOutputStream in order to provide the
  * OutputSteam used to download the file.
  */
 public abstract class JSONSyncSource extends TrackableSyncSource {
 
     private static final String TAG_LOG = "JSONSyncSource";
 
     protected boolean downloadFileObject;
     protected boolean downloadThumbnails;
 
     private HttpDownloader downloader = null;
     private SyncConfig syncConfig = null;
     private String dataTag = null;
 
     //------------------------------------------------------------- Constructors
 
     /**
      * JSONSyncSource constructor: initialize source config
      */
     public JSONSyncSource(SourceConfig config, SyncConfig syncConfig, ChangesTracker tracker) {
         super(config, tracker);
         this.downloadFileObject = true;
         this.downloadThumbnails = false;
         this.downloader = new HttpDownloader();
         this.syncConfig = syncConfig;
     }
 
     public SyncItem createSyncItem(String key, String type, char state,
                                    String parent, JSONObject json) throws JSONException {
         JSONSyncItem item = new JSONSyncItem(key, type, state, parent, json);
         return item;
     }
 
     public int addItem(SyncItem item) throws SyncException {
         // Note that the addItem must still download the actual item content, therefore
         // it can get a network error and this must be propagated
         try {
             JSONFileObject jsonFile = getJSONFileFromSyncItem(item);
             int res = addUpdateItem(item, jsonFile, false);
             super.addItem(item);
             return res;
         } catch (IOException ioe) {
             Log.error(TAG_LOG, "Cannot add item", ioe);
             return SyncSource.ERROR_STATUS;
         } catch (SyncException se) {
             // This kind of exception blocks the sync because it is a network error of some kind
             throw se;
         } catch (Throwable t) {
             Log.error(TAG_LOG, "Cannot add item", t);
             return SyncSource.ERROR_STATUS;
         }
     }
 
     public int updateItem(SyncItem item) throws SyncException {
         // We consider IOException and other generic exception as non
         // blocking exceptions for the sync. Only network exceptions will
         // block it
         try {
             JSONFileObject jsonFile = getJSONFileFromSyncItem(item);
             int res = addUpdateItem(item, jsonFile, true);
             super.updateItem(item);
             return res;
         } catch (IOException ioe) {
             Log.error(TAG_LOG, "Cannot add item, ioe");
             return SyncSource.ERROR_STATUS;
         } catch (SyncException se) {
             // This kind of exception blocks the sync because it is a network error of some kind
             throw se;
         } catch (Throwable t) {
             Log.error(TAG_LOG, "Cannot add item", t);
             return SyncSource.ERROR_STATUS;
         }
     }
 
     private JSONFileObject getJSONFileFromSyncItem(SyncItem item) throws JSONException {
         JSONFileObject jsonFile = null;
         if(item instanceof JSONSyncItem) {
             jsonFile = ((JSONSyncItem)item).getJSONFileObject();
         } else {
             String itemContent = new String(item.getContent());
             jsonFile = new JSONFileObject(itemContent);
         }
         return jsonFile;
     }
 
     public void updateSyncConfig(SyncConfig syncConfig) {
         this.syncConfig = syncConfig;
     }
 
     // This method returns the tag name in the JSONobject for the specific
     // type of data handled by this source. Refer to the SAPI documentation
     // for more info.
     public String getDataTag() {
         return dataTag;
     }
 
     public void setDataTag(String dataTag) {
         this.dataTag = dataTag;
     }
 
     protected int addUpdateItem(SyncItem item, JSONFileObject jsonFile, boolean isUpdate)
     throws SyncException, IOException {
         if(downloadFileObject) {
             String baseUrl = jsonFile.getUrl();
             long size = jsonFile.getSize();
             OutputStream fileos = null;
             fileos = getDownloadOutputStream(jsonFile, isUpdate, false);
 
            downloader.setDownloadListener(new DownloadSyncListener(item, 
                    super.getListener()));
             long actualSize = downloader.download(composeUrl(baseUrl), fileos, size);
             if (size != actualSize) {
                 // The download was interrupted. We shall keep track of this interrupted download
                 // so that it can be resumed
                 throw new ItemDownloadInterruptionException(item, actualSize);
             }
         }
         if(downloadThumbnails) {
             // TODO FIXME download thumbnails
         }
         return SyncSource.SUCCESS_STATUS;
     }
 
     protected OutputStream getDownloadOutputStream(JSONFileObject jsonItem,
             boolean isUpdate, boolean isThumbnail) throws IOException {
         return getDownloadOutputStream(
                 jsonItem.getName(),
                 jsonItem.getSize(),
                 isUpdate,
                 isThumbnail);
     }
 
     /**
      * Must be implemented in order to provide a proper OutputStream to download
      * the item.
      * 
      * @param name
      * @param size
      * @param isUpdate
      * @param isThumbnail
      * @return
      */
     protected abstract OutputStream getDownloadOutputStream(String name,
             long size, boolean isUpdate, boolean isThumbnail) throws IOException;
 
     /**
      * Composes the url to use for the download operation.
      * 
      * @param baseUrl
      * @param filename
      * @return
      */
     private String composeUrl(String baseUrl) {
         String serverUrl = StringUtil.extractAddressFromUrl(
                 syncConfig.getSyncUrl());
         StringBuffer res = new StringBuffer();
         res.append(serverUrl);
         res.append(baseUrl);
         return res.toString();
     }
 
     /**
      * Return whether the given item is supported by the source
      * @param item
      * @return
      */
     public boolean filterSyncItem(SyncItem item) {
 
         //
         // TODO: FIXME remove date filtering once implemented server side
         //
         if(item instanceof JSONSyncItem) {
             if(filter != null) {
                 Filter df = filter.getFullDownloadFilter();
                 if(df != null && df.getType() == Filter.DATE_RECENT_TYPE) {
                     long dateFilter = df.getDate();
                     JSONFileObject json = ((JSONSyncItem)item).getJSONFileObject();
                     // Reject the item if it doesn't respect the date filter
                     if(json.getDate() < dateFilter) {
                         return false;
                     }
                 }
             }
         }
         return true;
     }
 
     public SyncListener getListener() {
        return new ProxySyncListener(super.getListener());
     }
 
     /**
      * Filters some SyncListener calls which are actually invoked by the DownloadSyncListener
      */
     private class ProxySyncListener implements SyncListener {
 
         private SyncListener syncListener = null;
 
         public ProxySyncListener(SyncListener syncListener) {
             this.syncListener = syncListener;
         }
 
         public void startSession() {
             syncListener.startSession();
         }
         public void endSession(SyncReport report) {
             syncListener.endSession(report);
         }
         public void startConnecting() {
             syncListener.startConnecting();
         }
         public void endConnecting(int action) {
             syncListener.endConnecting(action);
         }
         public void syncStarted(int alertCode) {
             syncListener.syncStarted(alertCode);
         }
         public void endSyncing() {
             syncListener.endSyncing();
         }
         public void startReceiving(int number) {
             syncListener.startReceiving(number);
         }
         public void itemAddReceivingStarted(String key, String parent, long size) {
             // Do nothing
             // This is actually called by the DownloadSyncListener
         }
         public void itemAddReceivingEnded(String key, String parent) {
             // Do nothing
             // This is actually called by the DownloadSyncListener
         }
         public void itemAddReceivingProgress(String key, String parent, long size) {
             // Do nothing
             // This is actually called by the DownloadSyncListener
         }
         public void itemReplaceReceivingStarted(String key, String parent, long size) {
             // Do nothing
             // This is actually called by the DownloadSyncListener
         }
         public void itemReplaceReceivingEnded(String key, String parent) {
             // Do nothing
             // This is actually called by the DownloadSyncListener
         }
         public void itemReplaceReceivingProgress(String key, String parent, long size) {
             // Do nothing
             // This is actually called by the DownloadSyncListener
         }
         public void itemDeleted(SyncItem item) {
             syncListener.itemDeleted(item);
         }
         public void endReceiving() {
             syncListener.endReceiving();
         }
         public void startSending(int numNewItems, int numUpdItems, int numDelItems) {
             syncListener.startSending(numNewItems, numUpdItems, numDelItems);
         }
         public void itemAddSendingStarted(String key, String parent, long size) {
             syncListener.itemAddSendingStarted(key, parent, size);
         }
         public void itemAddSendingEnded(String key, String parent) {
             syncListener.itemAddSendingEnded(key, parent);
         }
         public void itemAddSendingProgress(String key, String parent, long size) {
             syncListener.itemAddSendingProgress(key, parent, size);
         }
         public void itemReplaceSendingStarted(String key, String parent, long size) {
             syncListener.itemReplaceSendingStarted(key, parent, size);
         }
         public void itemReplaceSendingEnded(String key, String parent) {
             syncListener.itemReplaceSendingEnded(key, parent);
         }
         public void itemReplaceSendingProgress(String key, String parent, long size) {
             syncListener.itemReplaceSendingProgress(key, parent, size);
         }
         public void itemDeleteSent(SyncItem item) {
             syncListener.itemDeleteSent(item);
         }
         public void endSending() {
             syncListener.endSending();
         }
         public void startFinalizing() {
             syncListener.startFinalizing();
         }
         public void endFinalizing() { 
             syncListener.endFinalizing();
         }
         public boolean startSyncing(int alertCode, Object devInf) {
             return syncListener.startSyncing(alertCode, devInf);
         }
     }
     
     /**
      * Translates the HttpDownloader.DownloadListener calls into SyncListener calls.
      */
     private class DownloadSyncListener implements HttpDownloader.DownloadListener {
 
         private SyncListener syncListener = null;
         private String itemKey = null;
         private String itemParent = null;
         private char itemState;
 
         public DownloadSyncListener(SyncItem item, SyncListener syncListener) {
             this.syncListener = syncListener;
             this.itemKey = item.getKey();
             this.itemParent = item.getParent();
             this.itemState = item.getState();
         }
 
         public void downloadStarted(long totalSize) {
             if(syncListener != null) {
                 if(itemState == SyncItem.STATE_NEW) {
                     syncListener.itemAddReceivingStarted(itemKey, itemParent, totalSize);
                 } else {
                     syncListener.itemReplaceReceivingStarted(itemKey, itemParent, totalSize);
                 }
             }
         }
 
         public void downloadProgress(long size) {
             if(syncListener != null) {
                 if(itemState == SyncItem.STATE_NEW) {
                     syncListener.itemAddReceivingProgress(itemKey, itemParent, size);
                 } else {
                     syncListener.itemReplaceReceivingProgress(itemKey, itemParent, size);
                 }
             }
         }
 
         public void downloadEnded() {
             if(syncListener != null) {
                 if(itemState == SyncItem.STATE_NEW) {
                     syncListener.itemAddReceivingEnded(itemKey, itemParent);
                 } else {
                     syncListener.itemReplaceReceivingEnded(itemKey, itemParent);
                 }
             }
         }
     }
 }
