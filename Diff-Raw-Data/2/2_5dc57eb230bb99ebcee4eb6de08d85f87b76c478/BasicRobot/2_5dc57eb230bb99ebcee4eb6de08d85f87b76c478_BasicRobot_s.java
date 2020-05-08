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
 
 package com.funambol.client.test.basic;
 
 import com.funambol.client.configuration.Configuration;
 import com.funambol.client.controller.Controller;
 import com.funambol.client.test.ClientTestException;
 import com.funambol.client.test.Robot;
 import com.funambol.client.test.util.SyncMonitor;
 import com.funambol.client.test.util.TestFileManager;
 import java.util.Vector;
 
 import com.funambol.sync.SyncItem;
 import com.funambol.sync.ItemStatus;
 import com.funambol.sync.SyncReport;
 import com.funambol.sync.SyncSource;
 import com.funambol.syncml.spds.SyncStatus;
 import com.funambol.util.StringUtil;
 import com.funambol.util.Log;
 
 
 public abstract class BasicRobot extends Robot {
    
     private static final String TAG_LOG = "BasicRobot";
 
     protected TestFileManager fileManager;
 
     public BasicRobot(TestFileManager fileManager) {
         this.fileManager = fileManager;
     }
 
     public TestFileManager getTestFileManager() {
         return fileManager;
     }
 
     public void waitForSyncToComplete(int minStart, int max,
             SyncMonitor syncMonitor) throws Throwable {
         
         if (Log.isLoggable(Log.DEBUG)) {
             Log.debug(TAG_LOG, "waiting for sync to complete");
         }
 
         // We wait no more than minStart for sync client to start
         while(!syncMonitor.isSyncing()) {
             Thread.sleep(WAIT_DELAY);
             minStart -= WAIT_DELAY;
             if (minStart < 0) {
                 throw new ClientTestException("Sync did not start within time limit");
             }
         }
 
         // Now wait until the busy is in progress for a max amount of time
         while(syncMonitor.isSyncing()) {
             Thread.sleep(WAIT_DELAY);
             max -= WAIT_DELAY;
             if (max < 0) {
                 throw new ClientTestException("Sync did not complete before timeout");
             }
         }
     }
 
     public void interruptSyncAfterPhase(String phase, int num, String reason, SyncMonitor syncMonitor) throws Throwable {
         if (Log.isLoggable(Log.DEBUG)) {
             Log.debug(TAG_LOG, "Preparing to interrupt after phase " + phase + "," + num);
         }
         syncMonitor.interruptSyncAfterPhase(phase, num, reason);
     }
 
     public void checkLastSyncRequestedSyncMode(String source, int mode,
             SyncMonitor syncMonitor) throws Throwable {
         if (Log.isLoggable(Log.DEBUG)) {
             Log.debug(TAG_LOG, "check last sync requested sync mode");
         }
 
         SyncReport sr = (SyncReport)syncMonitor.getSyncStatus(source);
         assertTrue(sr != null, "source has no report associated");
         assertTrue(sr.getRequestedSyncMode() == mode, "Requested sync mode mismatch");
     }
 
     public void checkLastSyncAlertedSyncMode(String source, int mode,
             SyncMonitor syncMonitor) throws Throwable {
         if (Log.isLoggable(Log.DEBUG)) {
             Log.debug(TAG_LOG, "check last sync alerted sync mode");
         }
 
         SyncReport sr = (SyncReport)syncMonitor.getSyncStatus(source);
         assertTrue(sr != null, "source has no report associated");
 
         assertTrue(sr instanceof SyncStatus, "Invalid sync report format");
         assertTrue(((SyncStatus)sr).getAlertedSyncMode() == mode, "Alerted sync mode mismatch");
     }
 
     public void checkLastSyncRemoteUri(String source, String uri,
             SyncMonitor syncMonitor) throws Throwable {
         if (Log.isLoggable(Log.DEBUG)) {
             Log.debug(TAG_LOG, "check last sync remote URI");
         }
 
         SyncReport sr = (SyncReport)syncMonitor.getSyncStatus(source);
         assertTrue(sr != null, "source has no report associated");
         assertTrue(sr.getRemoteUri(), uri, "Requested remote URI mismatch");
     }
 
     public void checkLastSyncExchangedData(String source,
             int sentAdd, int sentReplace, int sentDelete,
             int receivedAdd, int receivedReplace, int receivedDelete,
             SyncMonitor syncMonitor) throws Throwable
     {
         if (Log.isLoggable(Log.DEBUG)) {
             Log.debug(TAG_LOG, "check last sync exchanged data");
         }
 
         SyncReport sr = (SyncReport)syncMonitor.getSyncStatus(source);
         assertTrue(sr != null, "source has no report associated");
 
         assertTrue(receivedAdd, sr.getReceivedAddNumber(),
                 "Received add mismatch");
         assertTrue(receivedReplace, sr.getReceivedReplaceNumber(),
                 "Received replace mismatch");
         assertTrue(receivedDelete, sr.getReceivedDeleteNumber(),
                 "Received delete mismatch");
         assertTrue(sentAdd, sr.getSentAddNumber(),
                 "Sent add mismatch");
         assertTrue(sentReplace, sr.getSentReplaceNumber(),
                 "Sent replace mismatch");
         assertTrue(sentDelete, sr.getSentDeleteNumber(),
                 "Sent delete mismatch");
     }
 
     public void resetSourceAnchor(String sourceName) throws Throwable {
         if (Log.isLoggable(Log.DEBUG)) {
             Log.debug(TAG_LOG, "resetting source anchor");
         }
 
         SyncSource source = getSyncSource(sourceName);
         source.getSyncAnchor().reset();
         saveSourceConfig(sourceName);
     }
 
     public void checkItemsCount(String sourceName, int count) throws Throwable {
 
         SyncSource source = getSyncSource(sourceName);
 
         source.beginSync(SyncSource.FULL_SYNC, false); // Resets the tracker status
         int itemsCount = 0;
         SyncItem item = source.getNextItem();
         Vector items = new Vector();
         while(item != null) {
             itemsCount++;
            items.addElement(new ItemStatus(item.getKey(), 200));
             item = source.getNextItem();
         }
         source.applyItemsStatus(items);
         source.endSync();
         assertTrue(count, itemsCount, "Items count mismatch for source: " + sourceName);
     }
 
     public void resetFirstRunTimestamp() throws Throwable {
         Configuration configuration = getConfiguration();
         configuration.setFirstRunTimestamp(System.currentTimeMillis());
         long ts = getController().getHomeScreenController().checkServerMediaCaps();
         if(ts > 0) {
             // Update the server first run timestamp
             configuration.setServerFirstRunTimestamp(ts);
         }
         configuration.save();
     }
 
     public abstract void waitForAuthToComplete(int minStart, int max, SyncMonitor syncMonitor) throws Throwable;
 
     public abstract void keyPress(String keyName, int count) throws Throwable;
     public abstract void writeString(String text) throws Throwable;
 
     public abstract SyncSource getSyncSource(String sourceName) throws Exception;
     public abstract void saveSourceConfig(String sourceName) throws Exception;
 
     protected abstract Configuration getConfiguration();
     protected abstract Controller getController();
     
 }
