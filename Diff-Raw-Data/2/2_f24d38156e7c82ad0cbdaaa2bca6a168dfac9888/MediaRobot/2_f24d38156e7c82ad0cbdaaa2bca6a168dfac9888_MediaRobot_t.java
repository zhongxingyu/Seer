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
 
 package com.funambol.client.test.media;
 
 import com.funambol.client.source.AppSyncSource;
 import com.funambol.client.source.AppSyncSourceManager;
 import com.funambol.client.test.BasicScriptRunner;
 import com.funambol.client.test.Robot;
 import com.funambol.client.test.util.TestFileManager;
 import com.funambol.client.test.basic.BasicUserCommands;
 import com.funambol.sapisync.SapiSyncHandler;
 import com.funambol.sapisync.source.JSONFileObject;
 import com.funambol.sapisync.source.JSONSyncItem;
 import com.funambol.sapisync.source.JSONSyncSource;
 import com.funambol.sync.SyncConfig;
 import com.funambol.sync.SyncItem;
 import com.funambol.sync.SyncSource;
 import com.funambol.util.Log;
 import com.funambol.util.StringUtil;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import org.json.me.JSONArray;
 
 import org.json.me.JSONException;
 import org.json.me.JSONObject;
 
 public abstract class MediaRobot extends Robot {
 
     private static final String TAG_LOG = "MediaRobot";
 
     protected AppSyncSourceManager appSourceManager;
     protected TestFileManager fileManager;
 
     protected SapiSyncHandler sapiSyncHandler = null;
 
     public MediaRobot(AppSyncSourceManager appSourceManager,
             TestFileManager fileManager) {
         this.appSourceManager = appSourceManager;
         this.fileManager = fileManager;
     }
 
     public MediaRobot(TestFileManager fileManager) {
         this.fileManager = fileManager;
     }
 
     public void addMedia(String type, String filename) throws Throwable {
 
         SyncSource source = getAppSyncSource(type).getSyncSource();
 
         assertTrue(source instanceof JSONSyncSource,
                 "Sync source format not supported");
 
         getMediaFile(filename,
                 ((JSONSyncSource) source).getDownloadOutputStream(filename, -1,
                         false, false));
     }
 
     public abstract void deleteMedia(String type, String filename)
             throws Throwable;
 
     public abstract void deleteAllMedia(String type) throws Throwable;
 
     /**
      * Add a media to the server. Media content is read based on specified
      * file name (a file in application's assets or an online resource).
      * 
      * @param type sync source type
      * @param filename a relative path to the resource. BaseUrl parameter on test
      *                 configuration defines if the resource is a local file or is
      *                 an URL. In both cases, file is copied locally and then
      *                 uploaded to the server.
      * @throws Throwable
      */
     public void addMediaOnServer(String type, String filename) throws Throwable {
 
         //make file available on local storage
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         String contentType = getMediaFile(filename, os);
 
         byte[] fileContent = os.toByteArray();
         int size = fileContent.length;
         InputStream is = new ByteArrayInputStream(fileContent);
         
         addMediaOnServerFromStream(type, filename, is, size, contentType);
     }
 
     /**
      * Add a media to the server. Media content is read from a stream.
      * 
      * @param type sync source type
      * @param itemName name of the item to add
      * @param contentStream stream to the content of the item
      * @param contentSize size of the item
      * @param contentType mimetype of the content to add
      * 
      * @throws JSONException
      */
     protected void addMediaOnServerFromStream(
             String type,
             String itemName,
             InputStream contentStream,
             long contentSize,
             String contentType)
             throws JSONException {
 
         // Prepare json item to upload
         JSONFileObject jsonFileObject = new JSONFileObject();
         jsonFileObject.setName(itemName);
         jsonFileObject.setSize(contentSize);
         jsonFileObject.setCreationdate(System.currentTimeMillis());
         jsonFileObject.setLastModifiedDate(System.currentTimeMillis());
         jsonFileObject.setMimetype(contentType);
 
         MediaSyncItem item = new MediaSyncItem("fake_key", "fake_type",
                 SyncItem.STATE_NEW, null, jsonFileObject,
                 contentStream, contentSize);
 
         SapiSyncHandler sapiHandler = getSapiSyncHandler();
         sapiHandler.login(null);
         sapiHandler.uploadItem(item, getRemoteUri(type), null);
         sapiHandler.logout();
     }
 
     public void deleteMediaOnServer(String type, String filename)
             throws Throwable {
         SapiSyncHandler sapiHandler = getSapiSyncHandler();
         String itemId = findMediaOnServer(type, filename);
         sapiHandler.login(null);
         sapiHandler.deleteItem(itemId, getRemoteUri(type), getDataTag(type));
         sapiHandler.logout();
     }
 
     private String findMediaOnServer(String type, String filename)
             throws Throwable {
         SapiSyncHandler sapiHandler = getSapiSyncHandler();
         sapiHandler.login(null);
         try {
             SapiSyncHandler.FullSet itemsSet = sapiHandler.getItems(
                     getRemoteUri(type), getDataTag(type), null, null, null,
                     null);
             JSONArray items = itemsSet.items;
             for (int i = 0; i < items.length(); ++i) {
                 JSONObject item = items.getJSONObject(i);
                 String aFilename = item.getString("name");
                 if (filename.equals(aFilename)) {
                     String id = item.getString("id");
                     return id;
                 }
             }
         } finally {
             sapiHandler.logout();
         }
         return null;
     }
 
     public void deleteAllMediaOnServer(String type) throws Throwable {
         SapiSyncHandler sapiHandler = getSapiSyncHandler();
         sapiHandler.login(null);
         sapiHandler.deleteAllItems(getRemoteUri(type));
         sapiHandler.logout();
     }
 
     /**
      * Fill the server with some data in order to leave only specified quota
      * free.
      * Is free user quota is less that the required size, no action is
      * performed. 
      * 
      * @param type
      * @param byteToLeaveFree amount of byte to leave free on server 
      * @throws Throwable
      */
     public void leaveFreeServerQuota(String type, long byteToLeaveFree)
             throws Throwable {
         // get free quota for the current user
         SapiSyncHandler sapiHandler = getSapiSyncHandler();
        sapiHandler.login(null);
         long availableSpace = sapiHandler
                 .getUserAvailableServerQuota(getRemoteUri(type));
         
         //calculate server space to fill
         long spaceToFill = availableSpace - byteToLeaveFree;
         if (spaceToFill > 0) {
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "Available user quota is " + availableSpace + ", creating a fake file of bytes " + spaceToFill);
             }
             String extension;
             String mimetype;
             if (BasicUserCommands.SOURCE_NAME_PICTURES.equals(type)) {
                 extension = ".jpg";
                 mimetype = "image/jpeg";
             } else if (BasicUserCommands.SOURCE_NAME_VIDEOS.equals(type)) {
                 extension = ".avi";
                 mimetype = "video/avi";
             } else {
                 extension = ".txt";
                 mimetype = "application/*";
             }
 
             //create a fake file and fill it
             File tempFile = createFileWithSizeOnDevice(spaceToFill);
             FileInputStream fis = new FileInputStream(tempFile);
             if (tempFile != null ) addMediaOnServerFromStream(getRemoteUri(type), "fillspaceitem" + extension, fis, spaceToFill, mimetype);
         } else {
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "Available user quota is " + availableSpace + ", less that the required. Nothing to do.");
             }
         }
         sapiHandler.logout();
     }
 
     protected AppSyncSourceManager getAppSyncSourceManager() {
         return appSourceManager;
     }
 
     /**
      * Returns the AppSyncSource related to the given data type
      * 
      * @param type
      * @return
      */
     protected AppSyncSource getAppSyncSource(String type) {
         if (StringUtil.equalsIgnoreCase(BasicUserCommands.SOURCE_NAME_PICTURES,
                 type)) {
             return getAppSyncSourceManager().getSource(
                     AppSyncSourceManager.PICTURES_ID);
         } else if (StringUtil.equalsIgnoreCase(
                 BasicUserCommands.SOURCE_NAME_VIDEOS, type)) {
             return getAppSyncSourceManager().getSource(
                     AppSyncSourceManager.VIDEOS_ID);
         } else {
             throw new IllegalArgumentException("Invalid type: " + type);
         }
     }
 
     /**
      * Returns the SAPI data tag related to the given data type.
      * 
      * @param type
      * @return
      */
     private String getRemoteUri(String type) {
         return getAppSyncSource(type).getSyncSource().getConfig()
                 .getRemoteUri();
     }
 
     private String getDataTag(String type) {
         SyncSource src = getAppSyncSource(type).getSyncSource();
         String dataTag = null;
         if (src instanceof JSONSyncSource) {
             JSONSyncSource jsonSyncSource = (JSONSyncSource) src;
             dataTag = jsonSyncSource.getDataTag();
         }
         return dataTag;
     }
 
     private SapiSyncHandler getSapiSyncHandler() {
         if (sapiSyncHandler == null) {
             SyncConfig syncConfig = getSyncConfig();
             sapiSyncHandler = new SapiSyncHandler(
                     StringUtil.extractAddressFromUrl(syncConfig.getSyncUrl()),
                     syncConfig.getUserName(), syncConfig.getPassword());
         }
         return sapiSyncHandler;
     }
 
     /**
      * This is used to override the item input stream
      */
     private class MediaSyncItem extends JSONSyncItem {
 
         private InputStream stream;
         private long size;
 
         public MediaSyncItem(String key, String type, char state,
                 String parent, JSONFileObject jsonFileObject,
                 InputStream stream, long size) throws JSONException {
             super(key, type, state, parent, jsonFileObject);
             this.stream = stream;
             this.size = size;
         }
 
         public InputStream getInputStream() throws IOException {
             return stream;
         }
 
         public long getObjectSize() {
             return size;
         }
     }
 
     protected String getMediaFile(String filename, OutputStream output)
             throws Throwable {
         String baseUrl = BasicScriptRunner.getBaseUrl();
         String url = baseUrl + "/" + filename;
         return fileManager.getFile(url, output);
     }
 
     protected abstract void fillLocalStorage();
 
     protected abstract void restoreLocalStorage();
 
     public abstract void checkMediaCount(String type, int count)
             throws Throwable;
 
     protected abstract SyncConfig getSyncConfig();
 
     /**
      * Creates a temporary file of specified size
      * 
      * @param byteSize size of the file
      * @return name of the file created
      * @throws IOException
      */
     protected abstract File createFileWithSizeOnDevice(long byteSize)
             throws IOException;
 
 }
