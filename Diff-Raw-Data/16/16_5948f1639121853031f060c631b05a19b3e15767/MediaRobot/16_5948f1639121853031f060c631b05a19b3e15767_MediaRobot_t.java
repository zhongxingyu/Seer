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
 import com.funambol.client.test.util.CheckSyncClient;
 import com.funambol.client.test.util.TestFileManager;
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
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.json.me.JSONException;
 
 
 public abstract class MediaRobot extends Robot {
    
     private static final String TAG_LOG = "MediaRobot";
 
     protected AppSyncSourceManager appSourceManager;
     protected TestFileManager fileManager;
 
     public MediaRobot(AppSyncSourceManager appSourceManager, TestFileManager fileManager) {
         this.appSourceManager = appSourceManager;
         this.fileManager = fileManager;
     }
 
     public MediaRobot(TestFileManager fileManager) {
         this.fileManager = fileManager;
     }
     
     public void addMedia(String type, String filename) throws Throwable {
 
         SyncSource source = getAppSyncSource(type).getSyncSource();
         
         assertTrue(source instanceof JSONSyncSource, "Sync source format not supported");
 
         getMediaFile(filename, ((JSONSyncSource)source).getDownloadOutputStream(
                 filename, -1, false, false));
     }
 
     public abstract void deleteMedia(String type, String filename) throws Throwable;
 
     public abstract void deleteAllMedia(String type) throws Throwable;
     
     public void addMediaOnServer(String type, String filename) throws Throwable {
 
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         String contentType = getMediaFile(filename, os);
 
         byte[] fileContent = os.toByteArray();
         int size = fileContent.length;
 
         // Prepare json item to upload
         JSONFileObject jsonFileObject = new JSONFileObject();
         jsonFileObject.setName(filename);
         jsonFileObject.setSize(size);
         jsonFileObject.setCreationdate(System.currentTimeMillis());
         jsonFileObject.setLastModifiedDate(System.currentTimeMillis());
         jsonFileObject.setMimetype(contentType);
 
         MediaSyncItem item = new MediaSyncItem("fake_key",
                 "fake_type", SyncItem.STATE_NEW, null, jsonFileObject,
                 new ByteArrayInputStream(fileContent), size);
 
         SapiSyncHandler sapiHandler = getSapiSyncHandler();
         sapiHandler.login();
        sapiHandler.uploadItem(item, getRemoteUri(type), null);
         sapiHandler.logout();
     }
     
     public void deleteMediaOnServer(String type, String filename) throws Throwable {
         throw new UnsupportedOperationException("Not implemented yet.");
     }
 
     public void deleteAllMediaOnServer(String type) throws Throwable {
         SapiSyncHandler sapiHandler = getSapiSyncHandler();
         sapiHandler.login();
        sapiHandler.deleteAllItems(getRemoteUri(type));
         sapiHandler.logout();
     }
 
     protected abstract SyncConfig getSyncConfig();
 
     protected AppSyncSourceManager getAppSyncSourceManager() {
         return appSourceManager;
     }
 
     /**
      * Returns the AppSyncSource related to the given data type
      * @param type
      * @return
      */
     protected AppSyncSource getAppSyncSource(String type) {
         if(CheckSyncClient.SOURCE_NAME_PICTURES.equals(type)) {
             return getAppSyncSourceManager().getSource(AppSyncSourceManager.PICTURES_ID);
         } else if(CheckSyncClient.SOURCE_NAME_VIDEOS.equals(type)) {
             return getAppSyncSourceManager().getSource(AppSyncSourceManager.VIDEOS_ID);
         } else {
             throw new IllegalArgumentException("Invalid type: " + type);
         }
     }
 
     /**
      * Returns the SAPI data tag related to the given data type.
      * @param type
      * @return
      */
    private String getRemoteUri(String type) {
        return getAppSyncSource(type).getSyncSource().getConfig().getRemoteUri();
     }
 
     private SapiSyncHandler getSapiSyncHandler() {
         SyncConfig syncConfig = getSyncConfig();
         return new SapiSyncHandler(
                 StringUtil.extractAddressFromUrl(syncConfig.getSyncUrl()),
                 syncConfig.getUserName(),
                 syncConfig.getPassword());
     }
 
     /**
      * This is used to override the item input stream
      */
     private class MediaSyncItem extends JSONSyncItem {
 
         private InputStream stream;
         private long size;
 
         public MediaSyncItem(String key, String type, char state, String parent,
             JSONFileObject jsonFileObject, InputStream stream, long size) throws JSONException {
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
 
     protected String getMediaFile(String filename, OutputStream output) throws Throwable {
         String baseUrl = BasicScriptRunner.getBaseUrl();
         String url = baseUrl + "/" + filename;
         return fileManager.getFile(url, output);
     }
 }
