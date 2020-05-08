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
 package com.funambol.syncml.spds;
 
 import java.io.IOException;
 import java.io.ByteArrayOutputStream;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import com.funambol.util.MD5;
 import com.funambol.util.Base64;
 import com.funambol.util.StringUtil;
 import com.funambol.util.Log;
 
 import com.funambol.sync.SyncItem;
 import com.funambol.sync.SyncSource;
 import com.funambol.sync.SyncListener;
 import com.funambol.sync.BasicSyncListener;
 import com.funambol.sync.SyncException;
 import com.funambol.sync.SyncConfig;
 import com.funambol.sync.SourceConfig;
 import com.funambol.sync.ItemStatus;
 import com.funambol.sync.SyncManagerI;
 
 import com.funambol.syncml.protocol.*;
 import com.funambol.util.HttpTransportAgent;
 import com.funambol.util.TransportAgent;
 import com.funambol.util.CodedException;
 
 /**
  * The SyncManager is the engine of the synchronization process on the
  * client library. It initializes the sync, checks the server responses
  * and communicate with the SyncSource, which is the client-specific
  * source of data.
  * A client developer must prepare a SyncConfig to instantiate a
  * SyncManager, and then can sync its sources calling the sync()
  * method.
  * By default the SyncManager uses an HttpTransportAgent to communicate with the
  * server, but the interface allows clients to specify their own transport
  * agent.
  */
 public class SyncManager implements SyncManagerI {
 
     //------------------------------------------------------------- Private data
     private static final String TAG_LOG = "SyncManager";
 
     /* Fast sync sending add state*/
     private static final int STATE_SENDING_ADD = 1;
     /* Fast sync sending update state*/
     private static final int STATE_SENDING_REPLACE = 2;
     /* Fast sync sending delete state*/
     private static final int STATE_SENDING_DELETE = 3;
     /* Fast sync modification complete state*/
     private static final int STATE_MODIFICATION_COMPLETED = 4;
     /* The current SyncML message must be flushed */
     private static final int STATE_FLUSHING_MSG = 5;
 
     /* These are average sizes of messages used to estimate the msg size
      * before the message is actually formatted
      **/
     private static final int SYNCML_XML_HDR_SIZE = 450;
     private static final int SYNCML_WBXML_HDR_SIZE = 250;
     private static final int SYNCML_XML_STATUS_SIZE = 140;
     private static final int SYNCML_WBXML_STATUS_SIZE = 100;
     private static final int SYNCML_XML_MAP_SIZE = 120;
     private static final int SYNCML_WBXML_MAP_SIZE = 80;
 
     /* SyncManager configuration*/
     private SyncConfig config;
     private DeviceConfig deviceConfig;
     /* SyncSource to sync*/
     protected SyncSource source;
     /* Device ID taken from DeviceConfig*/
     private String deviceId;
     /* Max SyncML Message Size taken from DeviceConfig*/
     protected int maxMsgSize;
 
     /**
      * A flag indicating if the client has to prepare the <DevInf> part of the
      * initialization SyncML message containing the device capabilities. It can
      * be set to <code>true</code> in two falls:
      *
      * a) the <code>serverUrl</code> isn't on the list of the already
      * connected servers
      *
      * b) the device configuration is changed
      */
     private boolean sendDevInf = false;
     /**
      * A value indicating if the client has to add the device capabilities to the
      * modification message as content of a <Results> element. This occurs when
      * the server has sent a <Get> command request, sollicitating items of type
      * './devinf12'.
      * The value stored in this field is the cmd id of the get command or -1 if
      * no get command was sent by the server.
      */
     private String addDevInfResults = null;
     /**
      * String containing the last Url of the server the client was connected to
      */
     private String lastServerUrl;
     // state used for fast sync
     int state;
     // The alerts sent by server, indexed by source name, instantiated in
     // checkServerAlerts
     private Hashtable serverAlerts;
     // The alert code for the current source (i.e. the actual sync mode
     // eventually modified by ther server
     protected int alertCode;
     // Server URL modified with session id.
     protected String serverUrl;
     protected String sessionID = null;
     /**
      * This table is a helper to handle the hierarchy. It is the reverse table
      * of the mappings as it allows guid -> luid retrieval.
      * It is used when an item has the source parent set, to retrieve the
      * corresponding item in the client representation (luid)
      * There is a specific table because a reverse search in the mappings would
      * be too inefficient.
      */
     private Hashtable hierarchy = null;
 
     protected SyncStatus syncStatus = null;
 
     /**
      * This member stores the Status commands to send back to the server
      * in the next message. It is modified at each item received,
      * and is cleared after the status are sent.
      */
     protected Vector statusList = null;
 
     /**
      * This is the list of items to be processed. The engine collects all the items sent from the
      * server and then apply all the changes in one shot to the sync source.
      */
     protected ItemsList itemsToProcess = null;
 
     /**
      * This is the list of status to be processed. The engine collects all the status sent from the
      * server and then apply them in one shot to the sync source.
      */
     protected Vector statusToProcess = null;
 
     /**
      * This member is used to store the current message ID.
      * It is sent to the server in the MsgID tag.
      */
     private int msgID = 0;
     /**
      * This member is used to store the current command ID.
      * It is sent to the server in the CmdID tag.
      */
     private CmdId cmdID = new CmdId(0);
     /**
      * A single TransportAgent for all the operations
      * performed in this Sync Manager
      */
     private TransportAgent transportAgent;
     /**
      * This member is used to indicate if the SyncManager is busy, that is
      * if a sync is on going (SyncManager supports only one synchronization
      * at a time, and requests are queued in the synchronized method sync
      */
     private boolean busy;
 
     /**
      * Unique instance of a BasicSyncListener which is used when the user does
      * not set up a listener in the SyncSource. In order to avoid the creation
      * of multiple instances of this class we use this static variable
      */
     private static SyncListener basicListener = null;
 
     /**
      * Mapping manager instance
      */
     private MappingManager mappingManager = null;
     
     /**
      * This is a wrapper to handle incomining large object for sources that do
      * not support it.
      */
     protected SyncSourceLOHandler sourceLOHandler = null;
 
     /**
      * This is the flag used to indicate that the sync shall be cancelled. Users
      * can call the cancel (@see cancel) method to cancel the current sync
      */
     private boolean cancel;
 
     private SyncMLFormatter formatter;
     private SyncMLParser    parser;
 
     // Are we using wbxml?
     protected boolean wbxml;
 
     // Has this message a gloabal no resp?
     protected boolean globalNoResp = false;
 
     // Are we resuming a sync?
     protected boolean resume = false;
 
     // This flag controls the logging of wbxml binary messages. To be used only
     // during debugging
     private boolean logBinaryMessages = false;
 
     private boolean forceCapsInXml = false;
 
     //------------------------------------------------------------- Constructors
     /**
      * SyncManager constructor
      *
      * @param conf is the configuration data filled by the client
      *
      */
     public SyncManager(SyncConfig conf, DeviceConfig deviceConfig) {
         this.config = conf;
         this.deviceConfig = deviceConfig;
         this.source = null;
 
         // Cache device info
         this.deviceId = deviceConfig.getDevID();
         this.maxMsgSize = deviceConfig.getMaxMsgSize();
 
         this.state = 0;
         this.serverAlerts = null;
         this.alertCode = 0;
 
         this.busy = false;
 
         // status commands
         statusList = null;
         transportAgent =
                 new HttpTransportAgent(
                 config.syncUrl,
                 config.userAgent,
                 "UTF-8",
                 conf.compress,
                 conf.forceCookies,
                 conf.proxyConfig);
 
          wbxml  = deviceConfig.isWBXML();
          if (wbxml) {
              transportAgent.setRequestContentType("application/vnd.syncml+wbxml");
          }
 
          if (Log.isLoggable(Log.DEBUG)) {
              Log.debug(TAG_LOG, "Using wbxml=" + wbxml);
          }
 
          parser = new SyncMLParser(wbxml);
          formatter = new SyncMLFormatter(wbxml);
     }
 
     //----------------------------------------------------------- Public methods
     /**
      * Synchronizes synchronization source, using the preferred sync
      * mode defined for that SyncSource.
      *
      * @param source the SyncSource to synchronize
      *
      * @throws SyncException
      *                  If an error occurs during synchronization
      *
      */
     public void sync(SyncSource source) throws SyncException {
         sync(source, source.getSyncMode(), false);
     }
 
 
     /**
      * Synchronizes synchronization source, using the preferred sync
      * mode defined for that SyncSource.
      *
      * @param source the SyncSource to synchronize
      * @param askServerDevInf forces the sync to query for server device
      * information. The information is returned to the client via the
      * SyncListener (@see SyncListener.startSyncing)
      *
      * @throws SyncException
      *                  If an error occurs during synchronization
      *
      */
     public void sync(SyncSource source, boolean askServerDevInf) throws SyncException {
         sync(source, source.getSyncMode(), askServerDevInf);
     }
 
     /**
      * Synchronizes synchronization source
      *
      * @param source the SyncSource to synchronize
      * @param syncMode the sync mode
      * @throws SyncException
      *                  If an error occurs during synchronization
      */
     public synchronized void sync(SyncSource src, int syncMode)
             throws SyncException {
 
         sync(src, syncMode, false);
     }
 
     /**
      * Synchronizes synchronization source
      *
      * @param source the SyncSource to synchronize
      * @param syncMode the sync mode
      * @param askServerDevInf forces the sync to query for server device
      * information. The information is returned to the client via the
      * SyncListener (@see SyncListener.startSyncing)
      *
      * @throws SyncException
      *                  If an error occurs during synchronization
      */
     public synchronized void sync(SyncSource src, int syncMode,
                                   boolean askServerDevInf)
             throws SyncException {
 
         // Translate the abstract sync mode into the corresponding syncml one
         syncMode = getSyncMLSyncMode(syncMode);
 
         busy = true;
         cancel = false;
         resume = false;
 
         // Initialize the mapping message manager
         if (Log.isLoggable(Log.DEBUG)) {
             Log.debug(TAG_LOG, "Creating Mapping Manager");
         }
         mappingManager = new MappingManager(src.getName());
 
         // Initialize the sync status
         syncStatus = new SyncStatus(src.getName());
         try {
             syncStatus.load();
             // Is there an interrupted sync?
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "Last sync was interrupted = " + syncStatus.getInterrupted());
                 Log.info(TAG_LOG, "Source resume support is = " + src.supportsResume());
             }
             if (src.supportsResume() && syncStatus.getInterrupted()) {
                 // The last sync was a slow sync and it was interrupted, we can try
                 // to resume it
                 boolean exchangePhase = syncStatus.getSentItemsCount() > 0 ||
                                         syncStatus.getReceivedItemsCount() > 0;
 
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Number of sent items = " + syncStatus.getSentItemsCount());
                     Log.debug(TAG_LOG, "Number of received items = " + syncStatus.getReceivedItemsCount());
                 }
 
                 int interruptedSyncMode = syncStatus.getAlertedSyncMode();
 
                 if (exchangePhase && interruptedSyncMode == SyncML.ALERT_CODE_SLOW) {
                     syncMode = SyncML.ALERT_CODE_RESUME;
                     resume = true;
                     if (Log.isLoggable(Log.INFO)) {
                         Log.info(TAG_LOG, "Resuming interrupted session: " + syncStatus.getSessionId());
                     }
                 }
             }
             // If the previous sync was not interrupted then we clear the status
             if (!syncStatus.getInterrupted()) {
                 syncStatus.reset();
             }
         } catch (Exception e) {
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "No sync status found for source: " + src.getName());
             }
         }
         // We set the sync got interrupted so that if the application dies or
         // crashes for any reason, we know a sync was interrupted.
         // This flag is unset at the end of a successfull sync.
         syncStatus.setInterrupted(true);
 
         // Creates a sync source large object handler
         sourceLOHandler = new SyncSourceLOHandler(src, maxMsgSize, wbxml);
 
         // Update the sync status (do not save as it is not very useful so far)
         syncStatus.setRequestedSyncMode(syncMode);
         syncStatus.setLocUri(src.getName());
         syncStatus.setRemoteUri(src.getSourceUri());
 
         // By default the sync does not keep track of the hierarchy, we do it
         // only for slow and refresh from server. The hashtable is re-initialized
         // below, once the sync mode has been determined
         hierarchy = null;
 
         // Initialize the basicSyncListener
         if (basicListener == null) {
             basicListener = new BasicSyncListener();
         }
 
         // Notifies the listener that a new sync is about to start
         getSyncListenerFromSource(src).startSession();
 
         if (syncMode == SyncML.ALERT_CODE_NONE) {
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "Source not active.");
             }
             syncStatus.setStatusCode(SyncListener.SUCCESS);
             getSyncListenerFromSource(src).endSession(syncStatus);
             return;
         }
 
         int syncStatusCode = SyncListener.SUCCESS;
         try {
             byte response[] = null;
 
             // Set source attribute
             this.source = src;
 
             // Set initial state
             nextState(STATE_SENDING_ADD);
 
             //Set NEXT Anchor referring to current timestamp
             long syncStartTime = System.currentTimeMillis();
             SyncMLAnchor anchor = (SyncMLAnchor)this.source.getSyncAnchor();
 
             anchor.setNext(syncStartTime);
             syncStatus.setStartTime(syncStartTime);
 
             this.sessionID = String.valueOf(System.currentTimeMillis());
             this.serverUrl = config.syncUrl;
 
             // init status commands list
             this.statusList = new Vector();
 
             //deciding if the device capabilities have to be sent
             if (isNewServerUrl(serverUrl)) {
                 setFlagSendDevInf();
             }
 
             // ================================================================
             // Initialization phase
             // ================================================================
 
             // Update the sync status
             syncStatus.setSessionId(sessionID);
             syncStatus.setLastSyncStartTime(syncStartTime);
             saveSyncStatus();
             
             DevInf devInf = performInitializationPhase(syncMode, askServerDevInf, syncStatus);
 
             // ================================================================
             // Sync phase
             // ================================================================
 
             if (isSyncToBeCancelled()) {
                 cancelSync();
             }
 
             // Inform the handler about the actual sync mode
             sourceLOHandler.setResume(resume);
                     
             // The hierarchy needs to be initialized for any sync
             switch (alertCode) {
                 case SyncML.ALERT_CODE_FAST:
                 case SyncML.ALERT_CODE_TWO_WAY_BY_SERVER:
                 case SyncML.ALERT_CODE_ONE_WAY_FROM_SERVER_BY_SERVER:
                 case SyncML.ALERT_CODE_ONE_WAY_FROM_SERVER:
                     hierarchy = mappingManager.getMappings("hierarchy-" + source.getName());
                     break;
                 case SyncML.ALERT_CODE_SLOW:
                 case SyncML.ALERT_CODE_NONE:
                 case SyncML.ALERT_CODE_REFRESH_FROM_SERVER_BY_SERVER:
                 case SyncML.ALERT_CODE_REFRESH_FROM_SERVER:
                 case SyncML.ALERT_CODE_REFRESH_FROM_CLIENT_BY_SERVER:
                 case SyncML.ALERT_CODE_REFRESH_FROM_CLIENT:
                 case SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_BY_SERVER:
                 case SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT:
                 case SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_NO_SLOW:
                     // We start from scratch because we don't support slow sync
                     // resuming. Otherwise we should keep the hierarchy around
                     hierarchy = new Hashtable();
                     mappingManager.resetMappings("hierarchy-" + source.getName());
                     break;
                 default: 
                     break;
             }
             // Prepopulate the hierarchy with the root item, whose
             // mapping is itself
             if (hierarchy.get("/") == null) {
                 hierarchy.put("/", "/");
             }
             
             // Notifies that the synchronization is going to begin
             boolean ok = getSyncListenerFromSource(src).startSyncing(alertCode, devInf);
 
             if (!ok) {
                 //User Aborts the slow sync request
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "Sync process aborted by the user");
                 }
                 syncStatusCode = SyncListener.CANCELLED;
                 return;
             }
 
             int actualSyncMode;
 
             if (resume) {
                 // In case of resume, the alert code is the alert code of the
                 // interrupted sync
                 actualSyncMode = syncStatus.getAlertedSyncMode();
                 source.beginSync(getSourceSyncMode(actualSyncMode), true);
             } else {
                 actualSyncMode = alertCode;
                 source.beginSync(getSourceSyncMode(alertCode), false);
             }
             getSyncListenerFromSource(src).syncStarted(alertCode);
 
             boolean done = false;
 
             // the implementation of the client/server multi-messaging
             // through a do while loop: while </final> tag is reached.
             if (actualSyncMode == SyncML.ALERT_CODE_SLOW ||
                 actualSyncMode == SyncML.ALERT_CODE_REFRESH_FROM_CLIENT) {
 
                 getSyncListenerFromSource(src).startSending(0, source.getClientItemsNumber(), 0);
             } else {
                 getSyncListenerFromSource(src).startSending(source.getClientAddNumber(),
                     source.getClientReplaceNumber(),
                     source.getClientDeleteNumber());
             }
 
             do {
                 byte modificationsMsg[] = prepareModificationMessage();
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "Sending modification");
                 }
 
                 // Release the memory pool. All the objects that were part of
                 // the sent message are discarded
                 ObjectsPool.release();
 
                 if (isSyncToBeCancelled()) {
                     cancelSync();
                 }
 
                 response = postRequest(modificationsMsg);
 
                 if (wbxml) {
                     logBinaryMessage(response);
                 }
                 
                 modificationsMsg = null;
 
                 // Update the sync status since we are sure a bunch of mappings
                 // were sent
                 Hashtable mappings = syncStatus.getPendingMappings();
                 Enumeration mapKeys = mappings.keys();
                 while(mapKeys.hasMoreElements()) {
                     String luid = (String)mapKeys.nextElement();
                     syncStatus.addMappingSent(luid);
                 }
                 saveSyncStatus();
 
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "Response received");
                 }
                 logMessage(response, false);
                 getSyncListenerFromSource(src).endSending();
 
                 // The startReceiving(n) is notified from within the
                 // processModifications because here we do not know the number
                 // of messages to be received
                 SyncML msg = parser.parse(response);
                 response = null;
 
                 // Note that we cannot release the pool here, as during the
                 // parsing we prepare the status to be used during the next
                 // message preparation
 
                 getSyncListenerFromSource(src).endSending();
                 done = processModifications(msg, source);
 
                 getSyncListenerFromSource(src).endReceiving();
             } while (!done);
 
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "Modification session succesfully completed");
             }
             getSyncListenerFromSource(src).endSyncing();
 
             // ================================================================
             // Mapping phase
             // ================================================================
             if (isSyncToBeCancelled()) {
                 cancelSync();
             }
 
             getSyncListenerFromSource(src).startFinalizing();
             
             // Send the map message only if a mapping or a status has to be sent
             Hashtable mappings = syncStatus.getPendingMappings(); 
             if (statusList.size() > 0 || mappings.size() > 0) {
                 
                 byte mapMsg[] = prepareMappingMessage();
                 
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "Sending Mappings\n");
                 }
 
                 try {
                     response = postRequest(mapMsg);
 
                     if (wbxml) {
                         logBinaryMessage(response);
                     }
 
                     // Update the sync status since we are sure a bunch of mappings
                     // were sent
                     Enumeration mapKeys = mappings.keys();
                     while(mapKeys.hasMoreElements()) {
                         String luid = (String)mapKeys.nextElement();
                         syncStatus.addMappingSent(luid);
                     }
                     saveSyncStatus();
 
                     // Release the memory pool
                     ObjectsPool.release();
                 } catch (ReadResponseException rre) {
                     SyncMLAnchor syncMLAnchor = (SyncMLAnchor)source.getSyncAnchor();
                     syncMLAnchor.setLast(syncMLAnchor.getNext());
                     //save last anchors if the mapping message has been sent but
                     //the response has not been received due to network problems
                     if (Log.isLoggable(Log.INFO)) {
                         Log.info(TAG_LOG, "Last sync message sent - Error reading the response " + rre);
                     }
                 }
                 
                 mapMsg = null;
 
                 if (response != null) {
                     if (Log.isLoggable(Log.INFO)) {
                         Log.info(TAG_LOG, "Response received");
                     }
                     logMessage(response, false);
 
                     SyncML msg = parser.parse(response);
                     response = null;
                     processMapResponse(msg);
 
                 } else {
                     if (Log.isLoggable(Log.INFO)) {
                         Log.info(TAG_LOG, "Response not received, skipping check for status");
                     }
                 }
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "Mapping session succesfully completed");
                 }
             } else {
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "No mapping message to send");
                 }
             }
            
             // TODO: following code must be run only for succesfull path or error reading inputstream
             //       the other cases must skip the following code
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "Notifying listener end mapping");
             }
             getSyncListenerFromSource(src).endFinalizing();
 
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "Changing anchors");
             }
             // Set the last anchor to the next timestamp for the source
             SyncMLAnchor syncMLAnchor = (SyncMLAnchor)source.getSyncAnchor();
             syncMLAnchor.setLast(syncMLAnchor.getNext());
 
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "Ending session (" + syncStatusCode + ")");
             }
             // Tell the source that the sync is finished
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "Calling source endSync");
             }
             source.endSync();
 
             // If the synchronization terminates with no errors, then we reset
             // the hierarchy, because the sync is really over and it cannot be
             // resumed
             mappingManager.resetMappings("hierarchy-" + source.getName());
 
             // This sync terminated
             syncStatus.setInterrupted(false);
 
             // Create a listener status from the source status
             syncStatusCode = getListenerStatusFromSourceStatus(source.getStatus());
         } catch (CompressedSyncException compressedSyncException) {
             Log.error(TAG_LOG, "CompressedSyncException: ", compressedSyncException);
             syncStatusCode = SyncListener.COMPRESSED_RESPONSE_ERROR;
             throw compressedSyncException;
         } catch (SyncException se) {
             Log.error(TAG_LOG, "SyncException", se);
             // Create a listener status from the exception
             syncStatusCode = getListenerStatusFromSyncException(se);
             throw se;
         } catch (Throwable e) {
             if (e instanceof SecurityException) {
                 Log.error(TAG_LOG, "Security Exception", e);
                 throw (SecurityException)e;
             } else {
                 Log.error(TAG_LOG, "Exception", e);
                 syncStatusCode = SyncListener.GENERIC_ERROR;
                 throw new SyncException(SyncException.CLIENT_ERROR, e.toString());
             }
         } finally {
             // Regardless of how we got here, we save the sync status to be sure
             // it is persisted
             syncStatus.setStatusCode(syncStatusCode);
 
             // Save the sync end time
             long syncEndTime = System.currentTimeMillis();
             syncStatus.setEndTime(syncEndTime);
 
             // Persist the status
             saveSyncStatus();
 
             // Notifies the listener that the session is over
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "Ending session (" + syncStatusCode + ")");
             }
             // Notify the listener that the sync is finished
             try {
                 getSyncListenerFromSource(src).endSession(syncStatus);
             } finally {
                 releaseResources();
                 sourceLOHandler.releaseResources();
             }
         }
     }
 
 
     /**
      * This method cancels the current sync. The sync is interrupted once the
      * engine reaches the next check point (in other words the termination is
      * not immediate). 
      * When a sync is interrupted, a SyncException with code CANCELLED is
      * thrown. This exception will be thrown by the thread running the
      * synchronization itself, not by this method.
      * This method is non blocking, it marks the sync as to be cancelled and
      * returns, without waiting for the sync to be really cancelled.
      * If this SyncManager is performing more syncs cuncurrently, then all of
      * them are cancelled.
      */
     public void cancel() {
         cancel = true;
         if (sourceLOHandler != null) {
             sourceLOHandler.cancel(); 
         }
     }
 
 
     /**
      * To be invoked by every change of the device configuration and if the
      * serverUrl is a new one (i.e., not already on the list
      * <code>lastServerUrl</code>
      */
     public void setFlagSendDevInf() {
         sendDevInf = true;
     }
 
     /**
      * Checks if the manager is currently busy performing a synchronization.
      * @return true if a sync is currently on going
      */
     public boolean isBusy() {
         return busy;
     }
 
     /**
      * Sets the transport agent to be used for the next message to be sent. If
      * this method is invoked in the middle of a sync, it changes the connection
      * method for that very sync. This is a possible behavior, but it is very
      * uncommon. Users should make sure no sync is in progress when the
      * transport agent is changed. Typically the transport agent is changed
      * before the first sync and not changed afterward.
      * @param ta the transport agent
      * @throws IllegalArgumentException if the give transport agent is null
      */
     public void setTransportAgent(TransportAgent ta) {
         if (ta != null) {
             transportAgent = ta;
         } else {
             throw new IllegalArgumentException("Transport agent cannot be null");
         }
     }
 
     /**
      * This method allows clients to add custom headers into HTTP requests
      */
     public void addTranportAgentHeaders(Hashtable headers) {
         if (transportAgent != null) {
             transportAgent.setCustomHeaders(headers);
         }
     }
 
     /**
      * This method returns the sync report of the last sync performed. This
      * information is valid until a new sync is fired.
      */
     public SyncStatus getSyncStatus() {
         return syncStatus;
     }
 
     public void setForceCapsInXml(boolean value) {
         this.forceCapsInXml = value;
     }
 
     //---------------------------------------------------------- Private methods
 
     private DevInf performInitializationPhase(int syncMode, boolean askServerDevInf,
                                               SyncStatus syncStatus)
     throws SyncException, SyncMLParserException
     {
         // Get ready to try the authentication more than once, because it can
         // fail for invalid nonce or invalid auth method and we must perform
         // different attempts
         boolean md5 = SyncConfig.AUTH_TYPE_MD5 == config.preferredAuthType;
         int md5Attempts = 0;
         boolean retry;
 
         // Reset the msgId here
         resetMsgID();
 
         getSyncListenerFromSource(source).startConnecting();
 
         do {
             retry = false;
 
             // For efficiency we clear the status list
             statusList.removeAllElements();
 
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "Sending init message " + md5);
             }
             //Format request message to be sent to the server
             byte initMsg[] = prepareInitializationMessage(syncMode, askServerDevInf,
                                                           md5);
             if (isSyncToBeCancelled()) {
                 cancelSync();
             }
 
             if (wbxml) {
                 logBinaryMessage(initMsg);
                 logMessage(initMsg, false);
             }
 
             byte response[] = postRequest(initMsg);
             initMsg = null;
             logMessage(response, false);
             if (wbxml) {
                 logBinaryMessage(response);
             }
             // Release the memory pool
             ObjectsPool.release();
 
             SyncML syncMLMsg = parser.parse(response);
             try {
                 DevInf devInf = processInitMessage(syncMLMsg, source);
                 // If we asked for server caps but did not get them, then we throw an
                 // exception
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "Response received");
                 }
                 logMessage(response, false);
 
                 if (askServerDevInf && devInf == null) {
                     Log.error(TAG_LOG, "Server did not send requested capabilities");
                     // TODO: the server could return a 204 status. In such a
                     // case we should not throw an exception
                     throw new SyncException(SyncException.SERVER_ERROR,
                             "Cannot find server capabilities in server response");
                 }
 
                 return devInf;
             } catch (AuthenticationException ae) {
                 // Handle authentication errors and retries
                 String authMethod = ae.getAuthMethod();
                 String nextNonce  = ae.getNextNonce();
                 if (SyncML.AUTH_TYPE_MD5.equals(authMethod)) {
                     // The server required md5 authentication
                     if (config.allowMD5Authentication()) {
                         // If the previous attempt was not md5 or
                         // the previous was md5 but the first try, then
                         // we try again with the new nonce
                         if (!md5 || (md5 && md5Attempts == 0)) {
                             // Try again with the new nonce
                             if (Log.isLoggable(Log.DEBUG)) {
                                 Log.debug(TAG_LOG, "Setting next nonce to " + nextNonce);
                             }
                             retry = true;
                             md5 = true;
                         }
                     }
                     if (nextNonce != null) {
                         config.clientNonce=nextNonce;
                     }
                 } else if (SyncML.AUTH_TYPE_BASIC.equals(authMethod) && md5) {
                     // The previous md5 auth failed and the server required a
                     // basic auth. If the client allows it, we fall back to
                     // basic
                     if (config.allowBasicAuthentication()) {
                         retry = true;
                         md5 = false;
                     }
                 }
                 if (!retry) {
                     throw new SyncException(SyncException.AUTH_ERROR, "Invalid credentials");
                 }
             }
             if (md5) {
                 md5Attempts++;
             }
         } while(retry);
 
         // If we get here, we could not authenticate successfully
         throw new SyncException(SyncException.CLIENT_ERROR, "Cannot authenticate");
     }
 
     protected DevInf processInitMessage(SyncML message, SyncSource source) throws SyncException {
 
         String sourceName   = source.getName();
         SyncBody body       = message.getSyncBody();
 
         DevInf serverDevInf = null;
 
         // Process the header
         SyncHdr hdr = message.getSyncHdr();
         if (hdr != null) {
             String respURI = hdr.getRespURI();
             if (respURI != null) {
                 serverUrl = respURI;
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Found respURI = " + serverUrl);
                 }
             }
             globalNoResp = hasNoResp(hdr.getNoResp());
 
             Meta metaHdr = hdr.getMeta();
             if (metaHdr != null && metaHdr.getMaxMsgSize() != null) {
                 Long maxMsgSizeValue = metaHdr.getMaxMsgSize();
                 if (maxMsgSizeValue != null) {
                     try {
                         int serverMaxMsgSize = (int)maxMsgSizeValue.longValue();
                         // If the server max msg size is smaller than the our
                         // one then we use it
                         if (serverMaxMsgSize < maxMsgSize) {
                             maxMsgSize = serverMaxMsgSize;
                             if (Log.isLoggable(Log.INFO)) {
                                 Log.info(TAG_LOG, "Reducing maxMsgSize according to server request " + maxMsgSize);
                             }
                         }
                     } catch (Exception e) {
                         // In this case we just ignore the error
                         Log.error(TAG_LOG, "Cannot parse max msg size sent from server", e);
                     }
                 }
             }
         }
 
         if (!globalNoResp) {
             Status hdrStatus = Status.newInstance();
             hdrStatus.setMsgRef(""+msgID);
             hdrStatus.setCmdRef("0");
             hdrStatus.setCmd(SyncML.TAG_SYNCHDR);
             TargetRef tr = TargetRef.newInstance();
             tr.setValue(deviceId);
             hdrStatus.setTargetRef(tr);
             SourceRef sr = SourceRef.newInstance();
             sr.setValue(serverUrl);
             hdrStatus.setSourceRef(sr);
             hdrStatus.setData(Data.newInstance("" + SyncMLStatus.SUCCESS));
             // Add this status to the list
             statusList.addElement(hdrStatus);
         }
 
         // Process the body
         if (body == null) {
             return null;
         }
 
         Vector commands = body.getCommands();
 
         // Process all the commands and verify the status codes for the header
         // and the alert
         boolean hdrStatus = false;
         boolean alertStatus = false;
         for(int i=0;i<commands.size();++i) {
             Object command = commands.elementAt(i);
 
             if (command instanceof Alert) {
                 Alert alert = (Alert)command;
 
                 // Iterate over the items, looking for the one whose target is
                 // the source name
                 Vector items = alert.getItems();
                 String serverNextAnchor = null;
 
                 for(int j=0;j<items.size();++j) {
                     Item item = (Item)items.elementAt(j);
                     Target target = item.getTarget();
                     if (target != null) {
                         String locURI = target.getLocURI();
                         if (sourceName.equals(locURI)) {
                             alertCode = alert.getData();
                             if (Log.isLoggable(Log.DEBUG)) {
                                 Log.debug(TAG_LOG, "Found alert tag " + alertCode);
                             }
 
                             // The server may send its anchor here. We need to
                             // store to generate the proper status
                             Meta alertMeta = item.getMeta();
                             if (alertMeta != null) {
                                 Anchor anchor = alertMeta.getAnchor();
                                 if (anchor != null) {
                                     if (Log.isLoggable(Log.TRACE)) {
                                         Log.trace(TAG_LOG, "Server next anchor is: " + anchor.getNext());
                                     }
                                     serverNextAnchor = anchor.getNext();
                                 }
                             }
 
                             // Update the sync status
                             syncStatus.setAlertedSyncMode(alertCode);
                             saveSyncStatus();
 
                             break;
                         }
                     }
                 } 
 
                 if (!globalNoResp) {
                     // Prepare a status for the alert
                     Status aStatus = Status.newInstance();
                     aStatus.setMsgRef("" + 1);
                     aStatus.setCmdRef(alert.getCmdID());
                     aStatus.setCmd(SyncML.TAG_ALERT);
                     TargetRef tr = TargetRef.newInstance();
                     tr.setValue(source.getSourceUri());
                     aStatus.setTargetRef(tr);
                     SourceRef sr = SourceRef.newInstance();
                     sr.setValue(source.getName());
                     aStatus.setSourceRef(sr);
                     aStatus.setData(Data.newInstance(""+SyncMLStatus.SUCCESS));
                     Item alertStatusItem = Item.newInstance();
                     Anchor nAnchor = new Anchor();
                     if (serverNextAnchor == null) {
                         SyncMLAnchor anchor = (SyncMLAnchor)source.getSyncAnchor();
                         long nextAnchor = anchor.getNext();
                         nAnchor.setNext(""+nextAnchor);
                     } else {
                         nAnchor.setNext(serverNextAnchor);
                     }
                     alertStatusItem.setData(Data.newInstance(nAnchor));
                     aStatus.setItem(alertStatusItem);
 
                     // Add the status to the list
                     statusList.addElement(aStatus);
                 }
             } else if (command instanceof Status) {
                 Status status = (Status)command;
                 String cmd = status.getCmd();
 
                 if (SyncML.TAG_SYNCHDR.equals(cmd)) {
                     checkStatusCode(status);
                     hdrStatus = true;
                 }
 
                 if (SyncML.TAG_ALERT.equals(cmd)) {
                     checkStatusCode(status);
 
                     int statusCode = getStatusCode(status);
                     int syncMode = syncStatus.getRequestedSyncMode();
                     if (statusCode == SyncMLStatus.REFRESH_REQUIRED &&
                         syncMode == SyncML.ALERT_CODE_RESUME) {
                         // The server refused our resume attempt. At this point
                         // we can wipe the SyncStatus info
                         if (Log.isLoggable(Log.INFO)) {
                             Log.info(TAG_LOG, "Server refused to resume. Wiping old sync status");
                         }
                         try {
                             //long lastSyncTimeStamp = syncStatus.getLastSyncStartTime();
                             //String locUri = syncStatus.getLocUri();
                             syncStatus.resetExchangedItems();
                             syncStatus.setSessionId(sessionID);
                             syncStatus.setRequestedSyncMode(syncMode);
                             syncStatus.setInterrupted(true);
                             //syncStatus.setLastSyncStartTime(lastSyncTimeStamp);
                             saveSyncStatus();
                             resume = false;
                         } catch (Exception e) {
                             Log.error(TAG_LOG, "Cannot reset sync status", e);
                             throw new SyncException(SyncException.CLIENT_ERROR, "Cannot reset sync status");
                         }
                     }
                     alertStatus = true;
                 }
             } else if (command instanceof Get) {
                 // TODO Today we only support dev cap get
                 addDevInfResults = checkIfServerRequiredDevInf((Get)command);
             } else if (command instanceof Results) {
 
                 if (Log.isLoggable(Log.TRACE)) {
                     Log.trace(TAG_LOG, "Found Results command");
                 }
 
                 // These are the results of the get commands we sent
                 Results results = (Results)command;
                 Vector  items   = results.getItems();
 
                 if (Log.isLoggable(Log.TRACE)) {
                     Log.trace(TAG_LOG, "Number of items: " + items.size());
                 }
 
                 for(int j=0;j<items.size();++j) {
                     Object item = items.elementAt(j);
                     if (Log.isLoggable(Log.TRACE)) {
                         Log.trace(TAG_LOG, "item=" + item);
                     }
                     // TODO: we shall check the cmdref to make sure this is what
                     // we are looking for
                     if (item instanceof DevInfItem) {
                         DevInfItem devInfItem = (DevInfItem)item;
                         serverDevInf = devInfItem.getDevInf();
                     }
                 }
             }
         }
         // If we did not receive the status(es) we expected, then we throw an
         // error
         if (!hdrStatus || !alertStatus) {
             String msg = "Status code from server not received ";
             Log.error(TAG_LOG, msg + " hdr=" + hdrStatus + " alert=" + alertStatus);
             throw new SyncException(SyncException.SERVER_ERROR, msg);
         }
 
         return serverDevInf;
     }
 
 
     /**
      * Checks if the current server URL is the same as by the last connection.
      * If not, the current server URL is persisted in a record store on the
      * device
      *
      * @param url
      *            The server URL coming from the SyncConfig
      * @return true if the client wasn't ever connected to the corresponding
      *         server, false elsewhere
      */
     private boolean isNewServerUrl(String url) {
 
         //retrieve last server URL from the configuration
         lastServerUrl = config.lastServerUrl;
 
         if (StringUtil.equalsIgnoreCase(lastServerUrl, url)) {
             // the server url is the same as by the last connection, the client
             // may not send the device capabilities
             return false;
         } else {
             // the server url is new, the value has to be stored (this is let to
             // the SyncmlMPIConfig, while the SyncConfig isn't currently stored)
             return true;//the url is different, client can send the device info
         }
     }
 
     /**
      * Posts the given message to the url specified by <code>serverUrl</code>.
      *
      * @param request the request msg
      * @return the response of the server as a string
      *
      * @throws SyncException in case of network errors (thrown by sendMessage)
      */
     private byte[] postRequest(byte request[]) throws SyncException {
         transportAgent.setRequestURL(serverUrl);
         try {
             return transportAgent.sendMessage(request);
         } catch (CodedException ce) {
             int code;
             switch (ce.getCode()) {
                 case CodedException.DATA_NULL:
                     code = SyncException.DATA_NULL;
                     break;
                 case CodedException.CONN_NOT_FOUND:
                     code = SyncException.CONN_NOT_FOUND;
                     break;
                 case CodedException.ILLEGAL_ARGUMENT:
                     code = SyncException.ILLEGAL_ARGUMENT;
                     break;
                 case CodedException.WRITE_SERVER_REQUEST_ERROR:
                     code = SyncException.WRITE_SERVER_REQUEST_ERROR;
                     WriteRequestException wre = new WriteRequestException(code, ce.toString());
                     throw wre;
                 case CodedException.ERR_READING_COMPRESSED_DATA:
                     CompressedSyncException cse = new CompressedSyncException(ce.toString());
                     throw cse;
                 case CodedException.CONNECTION_BLOCKED_BY_USER:
                     code = SyncException.CONNECTION_BLOCKED_BY_USER;
                     break;
                 case CodedException.READ_SERVER_RESPONSE_ERROR:
                     code = SyncException.READ_SERVER_RESPONSE_ERROR;
                     ReadResponseException rre = new ReadResponseException(code, ce.toString());
                     throw rre;
                 case CodedException.OPERATION_INTERRUPTED:
                     code = SyncException.CANCELLED;
                     break;
                 default:
                     code = SyncException.CLIENT_ERROR;
                     break;
             }
             SyncException se = new SyncException(code, ce.toString());
             throw se;
         }
     }
 
     private int getStatusCode(Status status) throws SyncException {
         Data data = status.getData();
 
         if (data == null) {
             String msg = "Status from server has no data";
             Log.error(TAG_LOG, msg);
             throw new SyncException(SyncException.SERVER_ERROR, msg);
         }
 
         String codeVal = data.getData();
 
         try {
             int code = Integer.parseInt(codeVal);
             return code;
         } catch (Exception e) {
             String msg = "Status code from server is not a valid number " + codeVal;
             Log.error(TAG_LOG, msg);
             throw new SyncException(SyncException.SERVER_ERROR, msg);
         }
     }
 
     private void checkStatusCode(Status status) throws SyncException {
         int code = getStatusCode(status);
 
         Data data = status.getData();
         String msg = data.getData();
 
         switch (code) {
             case SyncMLStatus.SUCCESS:                      // 200
                 return;
             case SyncMLStatus.REFRESH_REQUIRED:             // 508
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "Refresh required by server.");
                 }
                 return;
             case SyncMLStatus.AUTHENTICATION_ACCEPTED:      // 212
             {
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "Authentication accepted by the server");
                 }
                 Chal chal = status.getChal();
                 if (chal != null) {
                     NextNonce nextNonce = chal.getNextNonce();
                     if (nextNonce != null) {
                         // Save the new nonce if the server sent it
                         if (Log.isLoggable(Log.TRACE)) {
                             Log.trace(TAG_LOG, "Saving next nonce");
                         }
                         config.clientNonce = new String(nextNonce.getValue());
                     }
                 }
                 return;
             }
             case SyncMLStatus.INVALID_CREDENTIALS:          // 401
             {
                 Log.error(TAG_LOG, "Invalid credentials: " + config.userName);
                 // Grab the authentication chal info and propagate
                 // it
                 String nextNonce   = null;
                 String authMethod  = null;
                 String nonceFormat = null;
                 Chal chal = status.getChal();
                 if (chal != null) {
                     NextNonce nNonce = chal.getNextNonce();
 
                     nonceFormat   = chal.getFormat();
                     authMethod    = chal.getType();
 
                     if (Log.isLoggable(Log.TRACE)) {
                         Log.trace(TAG_LOG, "Required auth method: " + authMethod);
                     }
                     if (Log.isLoggable(Log.TRACE)) {
                         Log.trace(TAG_LOG, "Nonce format " + nonceFormat);
                     }
 
                     if (nNonce != null) {
                         // Save the new nonce if the server sent it
                         nextNonce = nNonce.getValue();
                         if (Log.isLoggable(Log.TRACE)) {
                             Log.trace(TAG_LOG, "Saving next nonce " + nextNonce);
                         }
                     }
                 }
 
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "Server required authentication " + authMethod + " and nonce: " + nextNonce);
                 }
                 AuthenticationException authExc = new AuthenticationException("Authentication failed",
                         authMethod,
                         nonceFormat,
                         nextNonce);
                 throw authExc;
             }
             case SyncMLStatus.FORBIDDEN:                    // 403
                 throw new SyncException(
                         //SyncException.AUTH_ERROR,
                         SyncException.FORBIDDEN_ERROR,
                         "User not authorized: " + config.userName + " for source: " + source.getSourceUri());
             case SyncMLStatus.NOT_FOUND:                    // 404
                 Log.error(TAG_LOG, "Source URI not found on server: " + source.getSourceUri());
                 throw new SyncException(
                         //SyncException.ACCESS_ERROR,
                         SyncException.NOT_FOUND_URI_ERROR,
                         "Source URI not found on server: " + source.getSourceUri());
             case SyncMLStatus.SERVER_BUSY:                  // 503
                 throw new SyncException(
                         SyncException.SERVER_BUSY,
                         "Server busy, another sync in progress for " + source.getSourceUri());
             case SyncMLStatus.PROCESSING_ERROR:             // 506
                 throw new SyncException(
                         SyncException.BACKEND_ERROR,
                         "Error processing source: " + source.getSourceUri() + "," + msg);
             case SyncMLStatus.BACKEND_AUTH_ERROR:             // 511
                 throw new SyncException(
                         SyncException.BACKEND_AUTH_ERROR,
                         "Error processing source: " + source.getSourceUri() + "," + msg);
             default:
                 // Unhandled status code
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Unhandled Status Code, throwing exception " + code);
                 }
                 throw new SyncException(
                         SyncException.SERVER_ERROR,
                         "Error from server: " + code);
         }
     }
 
     /**
      * Check if this Get command is for devinf12 (we do not support devinf11)
      * @param get the Get command
      * @return the cmdId of the get command for the device caps, null otherwise
      */
     private String checkIfServerRequiredDevInf(Get get) {
 
         Vector items = get.getItems();
         for(int i=0;i<items.size();++i) {
             Item item = (Item)items.elementAt(i);
             Target target = item.getTarget();
             if (target != null) {
                 String locURI = target.getLocURI();
                 if (SyncML.DEVINF12.equals(locURI)) {
                     return get.getCmdID();
                 }
             }
         }
         return null;
     }
 
 
     /**
      * Prepares inizialization SyncML message
      */
     protected byte[] prepareInitializationMessage(int syncMode, boolean requireDevInf,
                                                   boolean md5Auth)
     throws SyncException
     {
         try {
             SyncML msg = new SyncML();
 
             // Prepare the header
             SyncHdr syncHdr = new SyncHdr();
 
             // Prepare the credentials
             MetInf credMetInf = MetInf.newInstance();
             String token;
             if (md5Auth) {
                 credMetInf.setType(Cred.AUTH_TYPE_MD5);
                 // Prepare a MD5 authentication tag
                 MD5 md5Computer = new MD5();
                 String nonceB64 = config.clientNonce;
                 byte nonce[];
                 if (nonceB64 == null) {
                     nonce = "".getBytes();
                 } else {
                     // The nonce in the config is b64 encoded
                     // (in XML this is a MUST)
                     nonce = Base64.decode(nonceB64.getBytes());
                 }
                 if (Log.isLoggable(Log.TRACE)) {
                     Log.trace(TAG_LOG, "Computing cred with nonce: " + nonceB64);
                 }
                 byte byteToken[] = md5Computer.computeMD5Credentials(config.userName,
                         config.password,
                         nonce);
                 token = new String(byteToken);
             } else {
                 credMetInf.setType(Cred.AUTH_TYPE_BASIC);
                 // TODO FIXME: use a constant
                 credMetInf.setFormat("b64");
 
                 String login = config.userName + ":" + config.password;
                 token = new String(Base64.encode(login.getBytes()));
             }
             Meta credMeta = Meta.newInstance();
             credMeta.setMetInf(credMetInf);
             Cred cred = new Cred();
             cred.setMeta(credMeta);
             cred.setData(Data.newInstance(token));
             syncHdr.setCred(cred);
 
             // Prepare the SyncHdr meta
             MetInf hdrMetInf = MetInf.newInstance();
             hdrMetInf.setMaxMsgSize(new Long(maxMsgSize));
             Meta hdrMeta = Meta.newInstance();
             hdrMeta.setMetInf(hdrMetInf);
             syncHdr.setMeta(hdrMeta);
 
             // Prepare the VerDTD and VerProto tags
             VerDTD verDTD = new VerDTD("1.2");
             syncHdr.setVerDTD(verDTD);
             syncHdr.setVerProto("SyncML/1.2");
             // Set the session ID
             syncHdr.setSessionID(sessionID);
 
             // Set the message ID
             resetMsgID();
             syncHdr.setMsgID(getNextMsgID());
 
             // Set the source and the target
             Source hdrSource = Source.newInstance();
             hdrSource.setLocURI(deviceId);
             hdrSource.setLocName(config.userName);
             syncHdr.setSource(hdrSource);
 
             Target hdrTarget = Target.newInstance();
             hdrTarget.setLocURI(serverUrl);
             syncHdr.setTarget(hdrTarget);
 
             // Now create the sync header and add it to the msg
             msg.setSyncHdr(syncHdr);
 
             // Prepare the body 
             SyncBody syncBody = new SyncBody();
 
             SyncMLAnchor syncMLAnchor = (SyncMLAnchor)source.getSyncAnchor();
             long nextAnchor = syncMLAnchor.getNext();
             long lastAnchor = syncMLAnchor.getLast();
             String sourceUri = source.getSourceUri();
             String sourceName = source.getName();
 
             // Prepare the alert
             resetCmdID();
             Alert alert = new Alert();
             alert.setCmdID(getNextCmdID());
             alert.setData(syncMode);
             Item alertItem = Item.newInstance();
             Source alertSource = Source.newInstance();
             alertSource.setLocURI(sourceName);
             alertItem.setSource(alertSource);
             Target alertTarget = Target.newInstance();
 
             // In a resume, the target shall specify the name of the remote
             // source
             if (syncMode == SyncML.ALERT_CODE_RESUME) {
                 alertTarget.setLocURI(source.getConfig().getRemoteUri());
             } else {
                 alertTarget.setLocURI(sourceUri);
             }
             alertItem.setTarget(alertTarget);
             Meta alertItemMeta = Meta.newInstance();
             Anchor anchor = new Anchor();
             if (syncMode != SyncML.ALERT_CODE_RESUME) {
                 anchor.setLast(""+lastAnchor);
             }
             anchor.setNext(""+nextAnchor);
             alertItemMeta.setAnchor(anchor);
             alertItem.setMeta(alertItemMeta);
             Vector alertItems = new Vector(1);
             alertItems.addElement(alertItem);
             alert.setItems(alertItems);
             Vector bodyCommands = new Vector(1);
             bodyCommands.addElement(alert);
 
             // Handle DevInf (both ways)
             // Add DevInf if we need to put them
             if (sendDevInf) {
                 Put devInfPut = new Put();
                 devInfPut.setCmdID(getNextCmdID());
                 Meta putMeta = Meta.newInstance();
 
                 devInfPut.setMeta(putMeta);
                 Item putItem = Item.newInstance();
                 Source putItemSource = Source.newInstance();
                 putItemSource.setLocURI(SyncML.DEVINF12);
                 putItem.setSource(putItemSource);
 
                 if (wbxml && forceCapsInXml) {
                     // We always send caps in xml, even if the sync is in wbxml
                     putMeta.setType("application/vnd.syncml-devinf+xml");
                     String xmlDevInf = createXmlDevInf(deviceConfig, source);
                     putItem.setData(Data.newInstance(xmlDevInf));
                 } else if (!wbxml) {
                     putMeta.setType("application/vnd.syncml-devinf+xml");
                     DevInf devInf = createDevInf(deviceConfig, source);
                     putItem.setData(Data.newInstance(devInf));
                 } else {
                     putMeta.setType("application/vnd.syncml-devinf+wbxml");
                     DevInf devInf = createDevInf(deviceConfig, source);
                     putItem.setData(Data.newInstance(devInf));
                 }
 
                 Vector putItems = new Vector();
                 putItems.addElement(putItem);
                 devInfPut.setItems(putItems);
 
                 bodyCommands.addElement(devInfPut);
 
                 //reset the flag
                 sendDevInf = false;
             }
 
             // Add a get command to query the server for its caps
             if (requireDevInf) {
                 // We need to add a Get command for the server caps
                 Get devInfGet = new Get();
                 devInfGet.setCmdID(getNextCmdID());
 
                 Meta getMeta = Meta.newInstance();
                 devInfGet.setMeta(getMeta);
                 Item getItem = Item.newInstance();
                 Target getItemTarget = Target.newInstance();
                 getItemTarget.setLocURI(SyncML.DEVINF12);
                 getItem.setTarget(getItemTarget);
 
                 if (wbxml && forceCapsInXml) {
                     // We always send caps in xml, even if the sync is in wbxml
                     // This is because some servers do not understand wbxml caps
                     getMeta.setType("application/vnd.syncml-devinf+xml");
                 } else if (!wbxml) {
                     getMeta.setType("application/vnd.syncml-devinf+xml");
                 } else {
                     getMeta.setType("application/vnd.syncml-devinf+wbxml");
                 }
 
                 Vector getItems = new Vector();
                 getItems.addElement(getItem);
                 devInfGet.setItems(getItems);
                 bodyCommands.addElement(devInfGet);
             }
 
             syncBody.setCommands(bodyCommands);
 
             // This is the end of the init msg
             syncBody.setFinalMsg(new Boolean(true));
 
             msg.setSyncBody(syncBody);
 
             logMessage(msg, false);
 
             ByteArrayOutputStream os = new ByteArrayOutputStream();
             formatter.format(msg, os, "UTF-8");
             return os.toByteArray();
         } catch (IOException ioe) {
             String msg = "Cannot prepare output message: " + ioe.toString();
             Log.error(TAG_LOG, msg);
             throw new SyncException(SyncException.CLIENT_ERROR, msg);
         }
     }
 
     private String createXmlDevInf(DeviceConfig deviceConfig, SyncSource source) throws IOException {
         DevInf devInf = createDevInf(deviceConfig, source);
         SyncMLFormatter xmlFormatter = new SyncMLFormatter(false);
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         xmlFormatter.formatXmlDevInf(devInf, os, "UTF-8");
         return os.toString();
     }
 
     private DevInf createDevInf(DeviceConfig deviceConfig, SyncSource source) {
 
         String sourceName = source.getName();
         String sourceType = source.getType();
 
         DevInf devInf = new DevInf();
         devInf.setVerDTD(new VerDTD(deviceConfig.getVerDTD()));
         devInf.setMan(deviceConfig.getMan());
         devInf.setMod(deviceConfig.getMod());
         devInf.setOEM(deviceConfig.getOEM());
         devInf.setSwV(deviceConfig.getSwV());
         devInf.setFwV(deviceConfig.getFwV());
         devInf.setHwV(deviceConfig.getHwV());
         devInf.setDevID(deviceConfig.getDevID());
         devInf.setDevTyp(deviceConfig.getDevType());
         devInf.setUTC(new Boolean(deviceConfig.getUtc()));
         devInf.setSupportLargeObjs(new Boolean(deviceConfig.getLoSupport()));
         devInf.setSupportNumberOfChanges(new Boolean(deviceConfig.getNocSupport()));
 
         // The source can also provide Ext
         SourceConfig srcConfig = source.getConfig();
         DataStore ds = null;
 
         if (srcConfig instanceof SyncMLSourceConfig) {
             SyncMLSourceConfig syncMLSrcConfig = (SyncMLSourceConfig)srcConfig;
             Vector devInfExts = syncMLSrcConfig.getDevInfExts();
             if (devInfExts != null) {
                 // We must append this extra info
                 devInf.addExts(devInfExts);
             }
 
             // A source can provide its own full device info, but for
             // backward compatibility this is not strictly necessary
             ds = syncMLSrcConfig.getDataStore();
         }
 
         if (ds == null) {
             // Add one store for this source
             ds = new DataStore();
             SourceRef sr = SourceRef.newInstance();
             sr.setValue(sourceName);
             ds.setSourceRef(sr);
             CTInfo rxPref = new CTInfo();
             rxPref.setCTType(sourceType);
             ds.setRxPref(rxPref);
             CTInfo txPref = new CTInfo();
             txPref.setCTType(sourceType);
             ds.setTxPref(txPref);
             SyncCap syncCap = new SyncCap();
 
             Vector types = new Vector();
             types.addElement(SyncType.TWO_WAY);
             types.addElement(SyncType.SLOW);
             types.addElement(SyncType.SERVER_ALERTED);
             syncCap.setSyncType(types);
             ds.setSyncCap(syncCap);
         }
 
         Vector stores = new Vector();
         stores.addElement(ds);
         devInf.setDataStores(stores);
 
         return devInf;
     }
 
     /**
      * Processes the modifications from the received response from server
      *
      * @param message The modification message from server
      * @param source  the source to be synchronized
      * @return true if the incoming message contains a Final tag 
      * @throws SyncException
      */
     protected boolean processModifications(SyncML message, SyncSource source) throws SyncException {
 
         String msgId = null;
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "processModifications");
         }
 
         SyncHdr hdr = message.getSyncHdr();
         SyncBody body = message.getSyncBody();
 
         if (hdr == null || body == null) {
             Log.error(TAG_LOG, "Invalid message from server.");
             throw new SyncException(
                     SyncException.SERVER_ERROR,
                     "Invalid message from server.");
         }
 
         globalNoResp = hasNoResp(hdr.getNoResp());
 
         // Get the message id
         msgId = hdr.getMsgID();
 
         // Ignore incoming modifications for one way from client modes (but
         // still process all status)
         boolean processIncomingMods = alertCode != SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT &&
                                       alertCode != SyncML.ALERT_CODE_REFRESH_FROM_CLIENT &&
                                       alertCode != SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_NO_SLOW;
 
         itemsToProcess = new ItemsList();
         statusToProcess = new Vector();
 
         // Process all commands
         try {
             Vector commands = body.getCommands();
             for(int j=0;j<commands.size();++j) {
                 Object command = commands.elementAt(j);
 
                 if (command instanceof Sync) {
                     Sync sync = (Sync)command;
                     processSyncCommand(sync, msgId);
                 } else if (command instanceof Status) {
                     Status status = (Status)command;
                     processStatus(status);
                 } else if (command instanceof SyncMLCommand) {
                     if (processIncomingMods) {
                         SyncMLCommand cmd = (SyncMLCommand)command;
                         processCommand(cmd, msgId);
                     } else {
                         Log.error(TAG_LOG, "Ignoring server to client changes in one way sync");
                     }
                 } else {
                     Log.error(TAG_LOG, "Unknwon kind of command " + command);
                 }
             }
         } finally {
             try {
                 // The loop above collects all the item commands for the syn source
                 // and we apply them in batch
                 applySourceItems(msgId);
 
                 // The loop above collects all the item commands for the syn source
                 // and we apply them in batch
                 applySourceStatus();
             } finally {
                 // Now we need to save the sync status because we changed it appliying modifications
                 saveSyncStatus();
             }
         }
 
         // Unless the server required no response, we shall send a Status to the
         // SyncHdr
         if (!globalNoResp) {
             Status hdrStatus = Status.newInstance();
             hdrStatus.setMsgRef(""+msgID);
             hdrStatus.setCmdRef("0"); // FIXME
             hdrStatus.setCmd(SyncML.TAG_SYNCHDR);
             TargetRef tr = TargetRef.newInstance();
             tr.setValue(deviceId);
             hdrStatus.setTargetRef(tr);
             SourceRef sr = SourceRef.newInstance();
             sr.setValue(serverUrl);
             hdrStatus.setSourceRef(sr);
             hdrStatus.setData(Data.newInstance("" + SyncMLStatus.SUCCESS));
             // Add this status to the list
             statusList.addElement(hdrStatus);
         }
 
         return body.isFinalMsg();
     }
 
     protected void applySourceItems(String msgId) {
         // Now we can apply all the modification commands in one shot
         Vector syncItems = applySourceChanges(itemsToProcess);
         // Now we need to update the SyncStatus and create the proper
         // commands
         for(int i=0;i<itemsToProcess.size();++i) {
             Chunk chunk = (Chunk)itemsToProcess.elementAt(i);
 
             // It is possible that the sync source did not process all the items
             // In such a case we just ignore the skipped ones
             if (i == syncItems.size()) {
                 break;
             }
 
             SyncItem item = (SyncItem)syncItems.elementAt(i);
             SyncMLCommand command = itemsToProcess.getItemCommand(chunk);
 
             String luid = item.getKey();
             String guid = chunk.getKey();
             int status  = item.getSyncStatus();
 
             if (SyncML.TAG_ADD.equals(command.getName())) {
                 if (SyncMLStatus.isSuccess(status) && hierarchy != null) {
                     hierarchy.put(guid, luid);
                 }
             }
             syncStatus.addReceivedItem(guid, luid, command.getName(), status);
 
             // Generate the Status if required
             boolean noResp = globalNoResp || command.getNoResp();
 
             Status statusCmd = null;
             if (noResp) {
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Found a command with NoResp (or SyncHdr NoResp), skipping status generation");
                 }
             } else {
                 // Init the status object
                 statusCmd = Status.newInstance();
                 statusCmd.setCmd(command.getName());
                 statusCmd.setCmdRef(command.getCmdId());
                 statusCmd.setMsgRef(msgId);
                 // Save the source ref if present (ADD), otherwise the target ref (UPD & DEL)
                 if (guid != null) {
                     SourceRef sr = SourceRef.newInstance();
                     sr.setValue(guid);
                     statusCmd.setSourceRef(sr);
                 } else {
                     TargetRef tr = TargetRef.newInstance();
                     tr.setValue(luid);
                     statusCmd.setTargetRef(tr);
                 }
                 statusCmd.setData(Data.newInstance(""+status));
                 statusList.addElement(statusCmd);
             }
         }
         // Allow GC to pick memory
         itemsToProcess = null;
     }
 
     protected void applySourceStatus() {
 
         Vector sourceStatusList = new Vector();
         for(int i=0;i<statusToProcess.size();++i) {
             Status status = (Status)statusToProcess.elementAt(i);
             String cmd = status.getCmd();
             Vector items = status.getItems();
             int code = status.getStatusCode();
             if (code != SyncMLStatus.CHUNKED_ITEM_ACCEPTED) {
                 // Check if it's a multi-item response
                 if (items != null && items.size() > 0) {
                     for (int j = 0,  n = items.size(); j < n; j++) {
                         Item item = (Item)items.elementAt(j);
                         Target target = item.getTarget();
                         String key = null;
                         if (target != null) {
                             key = target.getLocURI();
                         }
                         if (key == null) {
                             Source source = item.getSource();
                             if (source != null) {
                                 key = source.getLocURI();
                             }
                         }
 
                         if (key != null) {
                             if (SyncML.TAG_ADD.equals(cmd) || SyncML.TAG_REPLACE.equals(cmd) ||
                                 SyncML.TAG_DELETE.equals(cmd))
                             {
                                 // The sync source is unware of chunks, it
                                 // is only interested at items
                                 if (code != SyncMLStatus.CHUNKED_ITEM_ACCEPTED) {
                                     sourceStatusList.addElement(new ItemStatus(key, getSourceStatusCode(code)));
                                     // Register the status for this item in
                                     // the current sync
                                     syncStatus.addSentItem(key, cmd);
                                     syncStatus.receivedItemStatus(key, code);
                                 }
                             } else {
                                 sourceStatusList.addElement(new ItemStatus(key, getSourceStatusCode(code)));
                             }
                         } else {
                             Log.error(TAG_LOG, "Cannot set item status for unknwon item");
                         }
                     }
                 } else {
                     String ref = null;
                     Vector srcRefs = status.getSourceRef();
                     if (srcRefs != null && srcRefs.size() > 0) {
                         SourceRef srcRef = (SourceRef)srcRefs.elementAt(0);
                         ref = srcRef.getValue();
                     }
 
                     if (ref == null) {
                         Vector tgtRefs = status.getTargetRef();
                         if (tgtRefs != null && tgtRefs.size() > 0) {
                             TargetRef tgtRef = (TargetRef)tgtRefs.elementAt(0);
                             ref = tgtRef.getValue();
                         }
                     }
 
                     if (ref == null) {
                         Log.error(TAG_LOG, "Cannot set item status for unknown item");
                     } else {
                         // The chunk accepted status (213) is not
                         // propagated to the source, because the source
                         // has no knowledge/visibility of the individual
                         // chunks
                         if (SyncML.TAG_ADD.equals(cmd) || SyncML.TAG_REPLACE.equals(cmd) ||
                             SyncML.TAG_DELETE.equals(cmd))
                         {
                             if (code != SyncMLStatus.CHUNKED_ITEM_ACCEPTED) {
                                 sourceStatusList.addElement(new ItemStatus(ref, getSourceStatusCode(code)));
                                 // Register the status for this item in
                                 // the current sync
                                 syncStatus.addSentItem(ref, status.getCmd());
                                 syncStatus.receivedItemStatus(ref, code);
                             }
                         } else {
                             sourceStatusList.addElement(new ItemStatus(ref, getSourceStatusCode(code)));
                         }
                     }
                 }
             }
         }
 
         // Apply all the statuses in one shot
         source.applyItemsStatus(sourceStatusList);
 
         // Allow the GC to pick this memory
         sourceStatusList = null;
         statusToProcess = null;
     }
 
     private void processStatus(Status status) throws SyncException {
         String cmd = status.getCmd();
         if (Log.isLoggable(Log.DEBUG)) {
             Log.debug(TAG_LOG, "Processing Status for <" + cmd + "> command.");
         }
 
         // Check status to SyncHdr and Sync
         if (isSyncCommand(cmd)) {
 
             // In case of error we throw a SyncException
             if (!SyncMLStatus.isSuccess(status.getStatusCode())) {
                 String msg = "Server responded " + status.getStatusCode()
                              + " to command " + cmd;
                 Log.error(TAG_LOG, msg);
                 SyncException exc;
 
                 switch(status.getStatusCode()) {
                     case SyncMLStatus.SERVER_BUSY:
                         // 503
                         exc = new SyncException(SyncException.SERVER_BUSY, msg);
                         break;
                     case SyncMLStatus.PROCESSING_ERROR:
                         // 506
                         exc = new SyncException(SyncException.BACKEND_ERROR, msg);
                         break;
                     case SyncMLStatus.BACKEND_AUTH_ERROR:
                         // 511
                         exc = new SyncException(SyncException.BACKEND_AUTH_ERROR, msg);
                         break;
                     default:
                         // All error codes should be trapped by the above
                         // cases, but to be conservative we leave this
                         // fallback
                         exc = new SyncException(SyncException.SERVER_ERROR, msg);
                         break;
                 }
                 throw exc;
             }
         } else if (isMappingCommand(cmd)) {
             // The status of Map commands is ignored
         } else if (isPutCommand(cmd)) {
             // The status of Put commands is ignored
         } else if (isResultsCommand(cmd)) {
             // The status of Results commands is ignored
         } else if (isAlertCommand(cmd)) {
             // The status of Alert command is ignored
         } else {
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "Adding status to be processed");
             }
             // Otherwise, pass it to the source
             statusToProcess.addElement(status);
         }
     }
 
     /**
      * Processes a modification command received from server,
      * returning the command parts in an Hashtable
      *
      * @param msgRef The messageId tag of the message containing this command
      * @param cmdName the command name
      * @param command the body of the command
      *
      * @return the number of modifications made
      *
      * @throws SyncException if the command parsing failed
      *
      */
     protected int processCommand(SyncMLCommand command, String msgRef) throws SyncException {
 
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "processCommand");
         }
 
         // Get the type of the items for this command, if present
         // otherwise use the type defined for this source.
         Meta meta = command.getMeta();
         String itemType = null;
 
         if (meta != null) {
             itemType = meta.getType();
         }
 
         // Set the command type or use the source one
         if (itemType != null) {
             command.setType(itemType);
         } else {
             command.setType(source.getType());
         }
 
         String formatList[] = null;
         if (meta != null) {
             String format = meta.getFormat();
             if (format != null) {
                 formatList = StringUtil.split(format, ";");
             }
         }
 
         Vector items = command.getItems();
         for(int i=0;i<items.size();++i) {
             Item item = (Item)items.elementAt(i);
             Chunk chunk = createSyncItem(command, item, itemType, formatList);
 
             itemsToProcess.addElement(command,  chunk);
         }
         return items.size();
     }
 
 
     /**
      * Process the Sync command (check the source uri, save the
      * number of changes).
      * The visibility of this method is left at the package level so that we can
      * do unit tests.
      */
     protected void processSyncCommand(Sync sync, String msgRef) throws SyncException {
 
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "processSyncCommand");
         }
 
         String cmdId  = sync.getCmdID();
         Target target = sync.getTarget();
         String locURI = null;
         
         if (target != null) {
             locURI = target.getLocURI();
         }
 
         if (locURI == null || cmdId == null) {
             Log.error(TAG_LOG, "Invalid Sync command: ");
             throw new SyncException(
                     SyncException.SERVER_ERROR,
                     "Invalid Sync command from server.");
         }
 
         // If this sync is not for this source, throw an exception
         if (!locURI.equals(source.getName())) {
             Log.error(TAG_LOG, "Invalid uri: '" + locURI + "' for source: '" + source.getName() + "'");
             throw new SyncException(
                     SyncException.SERVER_ERROR,
                     "Invalid source to sync: " + locURI);
         }
 
         Long nc = sync.getNumberOfChanges();
         int ncVal = -1;
         if (nc != null) {
             ncVal = (int)nc.longValue();
         }
 
         // This is the very first moment we know how many message we're about
         // to receive. This is when we notify the listener about it, even though
         // the receiving phase has already begun.
         getSyncListenerFromSource(source).startReceiving(ncVal);
         source.setServerItemsNumber(ncVal);
 
         Vector commands = sync.getCommands();
         for(int i=0;i<commands.size();++i) {
             SyncMLCommand command = (SyncMLCommand)commands.elementAt(i);
             processCommand(command, msgRef);
         }
 
         boolean noResp = hasNoResp(sync.getNoResp());
 
         // A NoResp in the Sync tag is considered global. This is not clear from
         // the spec (they state that a NoResp in the SyncHdr is to be considered
         // global) but some servers like Ovi expects this behavior.
         // TODO: shall this depend on the server?
         if (noResp && !globalNoResp) {
             globalNoResp = true;
         }
 
         if (noResp || globalNoResp) {
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "Skipping status for sync command as NoResp was specified");
             }
         } else {
             Status status = Status.newInstance();
             status.setMsgRef(msgRef.toString());
             status.setCmdRef(cmdId);
             status.setCmd(SyncML.TAG_SYNC);
             TargetRef tr = TargetRef.newInstance();
             tr.setValue(source.getName());
             status.setTargetRef(tr);
             SourceRef sr = SourceRef.newInstance();
             sr.setValue(source.getSourceUri());
             status.setSourceRef(sr);
             status.setData(Data.newInstance("" + SyncMLStatus.SUCCESS));
 
             statusList.addElement(status);
         }
     }
 
     public Vector applySourceChanges(ItemsList items) throws SyncException {
 
         // The last item can be the beginning of a large object. LO are not sent to the source
         // until they are completed
         Chunk loChunk = null;
         SyncMLCommand loCmd = null;
         if (items.size() > 0) {
             Chunk lastItem = (Chunk)items.elementAt(items.size() - 1);
             if (lastItem.hasMoreData()) {
                 loChunk = lastItem;
                 loCmd  = items.getItemCommand(loChunk);
                 items.removeElementAt(items.size() - 1);
             }
         }
 
         // Apply all the other items. We need to create the SyncItem(s)
         SyncListener listener = getSyncListenerFromSource(source);
         Vector syncItems = sourceLOHandler.applyChanges(items, listener);
 
         if (loChunk != null) {
             // Apply the large object chunk via the source handler
             int status;
             char itemState = SyncML.TAG_ADD.equals(loCmd.getName()) ? SyncItem.STATE_NEW : SyncItem.STATE_UPDATED;
             status = sourceLOHandler.addUpdateChunk(loChunk, itemState == SyncItem.STATE_NEW);
             // Generate an item for this command
             SyncItem item = new SyncItem(loChunk.getKey(),loChunk.getType(),
                                          itemState,loChunk.getParent());
             item.setSyncStatus(status);
             syncItems.addElement(item);
         }
 
         return syncItems;
     }
 
     protected Chunk createSyncItem(SyncMLCommand command, Item item,
                                    String itemType, String [] formatList) throws SyncException {
 
         int status = 0;
         String guid = null;
 
         String cmdTag = command.getName();
 
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "createSyncItem");
         }
 
         Chunk chunk = sourceLOHandler.getItem(item, itemType, formatList, hierarchy);
         return chunk;
     }
 
     private boolean isSyncCommand(String cmd) {
         return cmd.equals(SyncML.TAG_SYNCHDR) || cmd.equals(SyncML.TAG_SYNC);
     }
 
     private boolean isMappingCommand(String cmd) {
         return cmd.equals(SyncML.TAG_MAP);
     }
 
     private boolean isPutCommand(String cmd) {
         return cmd.equals(SyncML.TAG_PUT);
     }
 
     private boolean isResultsCommand(String cmd) {
         return cmd.equals(SyncML.TAG_RESULTS);
     }
 
     private boolean isAlertCommand(String cmd) {
         return SyncML.TAG_ALERT.equals(cmd);
     }
 
     /**
      * Prepares the modification message in SyncML.
      *
      * @return the formatted message
      */
     protected byte[] prepareModificationMessage() throws SyncException {
 
         ByteArrayOutputStream os = new ByteArrayOutputStream();
 
         try {
             int msgSize = 0;
             SyncML msg = new SyncML();
 
             // Prepare the header
             SyncHdr syncHdr = new SyncHdr();
 
             // Prepare the VerDTD and VerProto tags
             VerDTD verDTD = new VerDTD("1.2");
             syncHdr.setVerDTD(verDTD);
             syncHdr.setVerProto("SyncML/1.2");
 
             // Set the session ID
             syncHdr.setSessionID(sessionID);
 
             // Set the message ID
             syncHdr.setMsgID(getNextMsgID());
 
             // Set the source and the target
             Source hdrSource = Source.newInstance();
             hdrSource.setLocURI(deviceId);
             syncHdr.setSource(hdrSource);
 
             Target hdrTarget = Target.newInstance();
             hdrTarget.setLocURI(serverUrl);
             syncHdr.setTarget(hdrTarget);
 
             // Now create the sync header and add it to the msg
             msg.setSyncHdr(syncHdr);
 
 
             // Prepare the sync body
             SyncBody body = new SyncBody();
             
             Vector bodyCommands = new Vector();
 
             resetCmdID();
 
             int msgIdRef = msgID - 1;
 
             // Update the estimated msgSize
             msgSize += wbxml ? SYNCML_WBXML_HDR_SIZE : SYNCML_XML_HDR_SIZE;
 
             // Add all the status commands...
             msgSize += prepareStatus(bodyCommands);
             // ...and cleanup the status vector
             statusList.removeAllElements();
 
             // Add mappings if necessary.
             msgSize += prepareMappings(bodyCommands);
 
             //Adding the device capabilities as response to the <Get> command
             if (addDevInfResults != null) {
 
                 Results devInfRes = new Results();
                 devInfRes.setCmdID(getNextCmdID());
                 devInfRes.setMsgRef(""+msgIdRef);
                 devInfRes.setCmdRef(addDevInfResults);
 
                 Meta devInfMeta = Meta.newInstance();
                 MetInf devInfMetInf = MetInf.newInstance();
                 // TODO at the moment the capabilities are always sent in xml,
                 // even when we do WBXML sync
                 if (wbxml) {
                     devInfMetInf.setType("application/vnd.syncml-devinf+wbxml");
                 } else {
                     devInfMetInf.setType("application/vnd.syncml-devinf+xml");
                 }
                 devInfMeta.setMetInf(devInfMetInf);
                 devInfRes.setMeta(devInfMeta);
 
                 Item devInfItem = Item.newInstance();
                 Source src = Source.newInstance();
                 src.setLocURI(SyncML.DEVINF12);
                 devInfItem.setSource(src);
 
                 DevInf devInf = createDevInf(deviceConfig, source);
                 Data data = Data.newInstance(devInf);
                 devInfItem.setData(data);
                 devInfRes.setItem(devInfItem);
 
                 bodyCommands.addElement(devInfRes);
 
                 //reset the flag
                 addDevInfResults = null;
             }
 
             if (this.state == STATE_MODIFICATION_COMPLETED) {
                 // We ask for the next message to the other side
                 Alert nextMsgAlert = new Alert();
                 nextMsgAlert.setCmdID(getNextCmdID());
                 nextMsgAlert.setData(SyncML.ALERT_CODE_NEXT_MESSAGE);
                 Item alertItem = Item.newInstance();
                 Source alertSource = Source.newInstance();
                 alertSource.setLocURI(source.getConfig().getName());
                 alertItem.setSource(alertSource);
                 Target alertTarget = Target.newInstance();
                 alertTarget.setLocURI(source.getConfig().getRemoteUri());
                 alertItem.setTarget(alertTarget);
                 Vector alertItems = new Vector(1);
                 alertItems.addElement(alertItem);
                 nextMsgAlert.setItems(alertItems);
 
                 bodyCommands.addElement(nextMsgAlert);
             }
 
 
             if (this.state != STATE_MODIFICATION_COMPLETED) {
                 Sync syncCommand = prepareSyncTag(msgSize);
                 bodyCommands.addElement(syncCommand);
             }
 
             if (this.state == STATE_MODIFICATION_COMPLETED) {
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "Modification done, sending <final> tag.");
                 }
                 body.setFinalMsg(new Boolean(true));
             }
 
             body.setCommands(bodyCommands);
 
             msg.setSyncBody(body);
 
             logMessage(msg, false);
 
             formatter.format(msg, os, "UTF-8");
             return os.toByteArray();
         } catch (IOException ioe) {
             String msg = "Cannot prepare output message: " + ioe.toString();
             Log.error(TAG_LOG, msg);
             throw new SyncException(SyncException.CLIENT_ERROR, msg);
         }
     }
 
     /**
      * return Sync tag about sourceUri
      *
      * @param size the current size of the msg being prepared
      * @return sync value
      */
     private Sync prepareSyncTag(int size) throws SyncException {
 
         Sync syncCommand = new Sync();
 
         syncCommand.setCmdID(getNextCmdID());
         Target target = Target.newInstance();
         target.setLocURI(source.getSourceUri());
         syncCommand.setTarget(target);
         Source s = Source.newInstance();
         s.setLocURI(source.getName());
         syncCommand.setSource(s);
 
         Vector commands = new Vector();
 
         do {
             int oldState = state;
             SyncMLCommand cmd = getNextCmd(size);
 
             // Last command?
             if (cmd == null) {
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "No more commands to send");
                 }
                 break;
             }
 
             if (cmd.getCmdId() != null) {
 
                 // Update the size
                 size += cmd.getSize();
 
                 // append command
                 commands.addElement(cmd);
 
                 // If the message must be flushed here, we do it, but we restore the
                 // previous state so that we can continue afterward
                 if (state == STATE_FLUSHING_MSG) {
                     if (Log.isLoggable(Log.INFO)) {
                         Log.info(TAG_LOG, "SyncML msg flushed");
                     }
                     nextState(oldState);
                     break;
                 }
             }
         } while (size < maxMsgSize);
 
         if (commands.size() > 0) {
             syncCommand.setCommands(commands); 
         }
 
         return syncCommand;
     }
 
 
     private byte[] prepareMappingMessage() {
         return prepareMappingMessage(true);
     }
 
     protected int prepareStatus(Vector commands) {
         // Add the status of the other commands
         int l = statusList.size();
 
         // Build status commands...
         for (int idx = 0; idx < l; idx++) {
             Status status = (Status) statusList.elementAt(idx);
             status.setCmdID(getNextCmdID());
             commands.addElement(status);
         }
         // Return an estimated size for this set of commands
         int statusSize = wbxml ? SYNCML_WBXML_STATUS_SIZE : SYNCML_XML_STATUS_SIZE;
         return l * statusSize;
     }
 
     private byte[] prepareMappingMessage(boolean isAddStatusEnabled) {
 
         ByteArrayOutputStream os = new ByteArrayOutputStream();
 
         try {
             SyncML msg = new SyncML();
 
             // Prepare the header
             SyncHdr syncHdr = new SyncHdr();
 
             // Prepare the VerDTD and VerProto tags
             VerDTD verDTD = new VerDTD("1.2");
             syncHdr.setVerDTD(verDTD);
             syncHdr.setVerProto("SyncML/1.2");
 
             // Set the session ID
             syncHdr.setSessionID(sessionID);
 
             // Set the message ID
             syncHdr.setMsgID(getNextMsgID());
 
             // Set the source and the target
             Source hdrSource = Source.newInstance();
             hdrSource.setLocURI(deviceId);
             syncHdr.setSource(hdrSource);
 
             Target hdrTarget = Target.newInstance();
             hdrTarget.setLocURI(serverUrl);
             syncHdr.setTarget(hdrTarget);
 
             // Now create the sync header and add it to the msg
             msg.setSyncHdr(syncHdr);
 
             // Prepare the sync body
             SyncBody body = new SyncBody();
             
             Vector bodyCommands = new Vector();
 
             resetCmdID();
             // Add all the status commands...
             prepareStatus(bodyCommands);
             // ...and cleanup the status vector
             statusList.removeAllElements();
 
             // Add mappings if necessary.
             prepareMappings(bodyCommands);
 
             body.setFinalMsg(new Boolean(true));
 
             body.setCommands(bodyCommands);
 
             msg.setSyncBody(body);
 
             logMessage(msg, false);
 
             formatter.format(msg, os, "UTF-8");
             return os.toByteArray();
         } catch (IOException ioe) {
             String msg = "Cannot prepare output message: " + ioe.toString();
             Log.error(TAG_LOG, msg);
             throw new SyncException(SyncException.CLIENT_ERROR, msg);
         }
     }
 
 
     private int prepareMappings(Vector commands) throws IOException {
 
         Hashtable mappings = syncStatus.getPendingMappings();
 
         if (mappings.size() > 0) {
 
             Map mapCommand = new Map();
             mapCommand.setCmdID(getNextCmdID());
             String sourceUri = source.getSourceUri();
             String sourceName = source.getName();
             Source source = Source.newInstance();
             source.setLocURI(sourceName);
             mapCommand.setSource(source);
             Target target = Target.newInstance();
             target.setLocURI(sourceUri);
             mapCommand.setTarget(target);
 
             Vector mapItems = new Vector();
 
             Enumeration e = mappings.keys();
             while (e.hasMoreElements()) {
                 String sourceRef = (String) e.nextElement();
                 String targetRef = (String) mappings.get(sourceRef);
 
                 MapItem mapItem = new MapItem();
                 target = Target.newInstance();
                 target.setLocURI(targetRef);
                 mapItem.setTarget(target);
                 source = Source.newInstance();
                 source.setLocURI(sourceRef);
                 mapItem.setSource(source);
 
                 mapItems.addElement(mapItem);
             }
             if (mapItems.size() > 0) {
                 mapCommand.setMapItems(mapItems);
             }
             commands.addElement(mapCommand);
         }
         // Return an estimated size for this set of commands
         int mapSize = wbxml ? SYNCML_WBXML_MAP_SIZE : SYNCML_XML_MAP_SIZE;
         return mappings.size() * mapSize;
     }
 
     /**
      * This method returns the Add command tag.
      */
     private SyncMLCommand getAddCommand(int size) throws SyncException {
 
         SyncMLCommand command = SyncMLCommand.newInstance(SyncML.TAG_ADD);
         int status = sourceLOHandler.getAddCommand(size, getSyncListenerFromSource(source), command,
                                                    cmdID, syncStatus);
 
         if (status == SyncSourceLOHandler.DONE) {
             nextState(STATE_SENDING_REPLACE);
         } else if (status == SyncSourceLOHandler.FLUSH) {
             nextState(STATE_FLUSHING_MSG);
         }
         return command;
     }
 
     /**
      * This method returns the Replace command tag.
      */
     private SyncMLCommand getReplaceCommand(int size) throws SyncException {
 
         SyncMLCommand command = SyncMLCommand.newInstance(SyncML.TAG_REPLACE);
         int status = sourceLOHandler.getReplaceCommand(size, getSyncListenerFromSource(source), command, cmdID);
         if (status == SyncSourceLOHandler.DONE) {
             nextState(STATE_SENDING_DELETE);
         } else if (status == SyncSourceLOHandler.FLUSH) {
             nextState(STATE_FLUSHING_MSG);
         }
         return command;
     }
 
     /**
      * This method returns the Delete command tag.
      */
     private SyncMLCommand getDeleteCommand(int size) throws SyncException {
 
         SyncMLCommand command = SyncMLCommand.newInstance(SyncML.TAG_DELETE);
         boolean done = sourceLOHandler.getDeleteCommand(size, getSyncListenerFromSource(source), command, cmdID);
 
         // No item for this source
         if (done) {
             // All new items are donw, go to the next state.
             nextState(STATE_MODIFICATION_COMPLETED);
         }
         return command;
     }
 
     /**
      *  Get the next command tag, with all the items that can be contained
      *  in defined the message size.
      *
      *  @param size
      *
      *  @return the command tag of null if no item to send.
      */
     private SyncMLCommand getNextCmd(int size) throws SyncException {
 
         SyncMLCommand command = null;
         switch (alertCode) {
 
             case SyncML.ALERT_CODE_SLOW:
             case SyncML.ALERT_CODE_REFRESH_FROM_CLIENT:
 
                 int msgStatus[] = new int[1];
                 command = sourceLOHandler.getNextCommand(size, getSyncListenerFromSource(source), cmdID,
                                                          syncStatus, msgStatus);
                 int status = msgStatus[0];
 
                 if (status == SyncSourceLOHandler.DONE) {
                     nextState(STATE_MODIFICATION_COMPLETED);
                 } else if (status == SyncSourceLOHandler.FLUSH) {
                     nextState(STATE_FLUSHING_MSG);
                 }
 
                 // Check if there are no items, then we signal the end of the
                 // sync
                 if (command.getCmdId() == null) {
                     return null;
                 }
                 break;
 
             case SyncML.ALERT_CODE_REFRESH_FROM_SERVER:
             case SyncML.ALERT_CODE_ONE_WAY_FROM_SERVER:
                 nextState(STATE_MODIFICATION_COMPLETED);
                 return null; // no items sent for refresh from server
 
             case SyncML.ALERT_CODE_FAST:
             case SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT:
             case SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_NO_SLOW:
                 //
                 // Fast Sync or One way from client.
                 //
                 switch (state) {
                     case STATE_SENDING_ADD:
                         command = getAddCommand(size);
                         break;
                     case STATE_SENDING_REPLACE:
                         command = getReplaceCommand(size);
                         break;
                     case STATE_SENDING_DELETE:
                         command = getDeleteCommand(size);
                         break;
                     default:
                         return null;
                 }
                 break;
 
             default:
                 Log.error(TAG_LOG, "Invalid alert code: " + alertCode);
                 throw new SyncException(
                         SyncException.SERVER_ERROR,
                         "Invalid alert code: " + alertCode);
         }
 
         return command;
     }
 
     private void processMapResponse(SyncML message) throws SyncException {
 
         SyncBody body       = message.getSyncBody();
         // Process the body
         if (body == null) {
             return;
         }
 
         Vector commands = body.getCommands();
 
         // Process all the commands and verify the status codes for the header
         // and the alert
         boolean hdrStatus = false;
         for(int i=0;i<commands.size();++i) {
             Object command = commands.elementAt(i);
 
             if (command instanceof Status) {
                 Status status = (Status)command;
                 String cmd = status.getCmd();
 
                 // At the moment we do not check for single map commands status,
                 // we just check the status to the header command
                 if (SyncML.TAG_SYNCHDR.equals(cmd)) {
                     checkStatusCode(status);
                     hdrStatus = true;
                 }
             }
         }
         // If we did not receive the status(es) we expected, then we throw an
         // error
         if (!hdrStatus) {
             String msg = "Status code for map from server not received ";
             Log.error(TAG_LOG, msg);
             throw new SyncException(SyncException.SERVER_ERROR, msg);
         }
     }
 
     /**
      * Returns the server alert code for the given source
      *
      * @param sourceURI the source
      *
      * @return the server alert code for the given source or -1 if it is not
      *         found/parsable
      */
     private int getSourceAlertCode(String sourceURI) {
 
         try {
             String alert = (String) serverAlerts.get(sourceURI);
             return Integer.parseInt(alert);
         } catch (Throwable t) {
             Log.error(TAG_LOG, "ERROR: unrecognized server alert code ("
                       + serverAlerts.get(sourceURI) + ") for " + sourceURI.toString(), t);
         }
 
         return -1;
     }
 
     // Reset the message ID counter.
     private void resetMsgID() {
         msgID = 0;
     }
 
     // Return the next message ID to use.
     private String getNextMsgID() {
         return String.valueOf(++msgID);
     }
 
     // Reset the command ID counter.
     private void resetCmdID() {
         cmdID.setValue(0);
     }
 
     // Return the next message ID to use.
     public String getNextCmdID() {
         return String.valueOf(cmdID.next());
     }
 
     private void nextState(int state) {
         this.state = state;
         String msg = null;
 
         if (Log.getLogLevel() >= Log.DEBUG) {
             switch (state) {
                 case STATE_SENDING_ADD:
                     msg = "state=>STATE_SENDING_ADD";
                     break;
                 case STATE_SENDING_REPLACE:
                     msg = "state=>STATE_SENDING_REPLACE";
                     break;
                 case STATE_SENDING_DELETE:
                     msg = "state=>STATE_SENDING_DELETE";
                     break;
                 case STATE_MODIFICATION_COMPLETED:
                     msg = "state=>STATE_MODIFICATION_COMPLETED";
                     break;
                 case STATE_FLUSHING_MSG:
                     msg = "state=>STATE_FLUSHING_MSG";
                     break;
                 default:
                     msg = "UNKNOWN STATE!";
             }
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, msg);
             }
         }
     }
 
     private void cancelSync() throws SyncException
     {
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Cancelling sync for source ["+source.getName()+"]");
         }
         throw new SyncException(SyncException.CANCELLED, "SyncManager sync got cancelled");
     }
 
     private boolean isSyncToBeCancelled() {
         return cancel;
     }
 
     private SyncListener getSyncListenerFromSource(SyncSource source) {
         SyncListener slistener = source.getListener();
         if(slistener != null) {
             return slistener;
         } else {
             return basicListener;
         }
     }
 
     private int getListenerStatusFromSourceStatus(int status) {
         int syncStatus;
         switch(status) {
             case SyncSource.STATUS_SUCCESS:
                 syncStatus = SyncListener.SUCCESS;
                 break;
             case SyncSource.STATUS_SEND_ERROR:
                 syncStatus = SyncListener.ERROR_SENDING_ITEMS;
                 break;
             case SyncSource.STATUS_RECV_ERROR:
                 syncStatus = SyncListener.ERROR_RECEIVING_ITEMS;
                 break;
             case SyncSource.STATUS_SERVER_ERROR:
             case SyncSource.STATUS_CONNECTION_ERROR:
             default:
                 syncStatus = SyncListener.GENERIC_ERROR;
         }
         return syncStatus;
     }
 
     private int getListenerStatusFromSyncException(SyncException se) {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "getting listener status for " + se.getCode());
         }
         int syncStatus;
         switch (se.getCode()) {
             case SyncException.AUTH_ERROR:
                 syncStatus = SyncListener.INVALID_CREDENTIALS;
                 break;
             case SyncException.FORBIDDEN_ERROR:
                 syncStatus = SyncListener.FORBIDDEN_ERROR;
                 break;
             case SyncException.CONN_NOT_FOUND:
                 syncStatus = SyncListener.CONN_NOT_FOUND;
                 break;
             case SyncException.READ_SERVER_RESPONSE_ERROR:
                 syncStatus = SyncListener.READ_SERVER_RESPONSE_ERROR;
                 break;
             case SyncException.WRITE_SERVER_REQUEST_ERROR:
                 syncStatus = SyncListener.WRITE_SERVER_REQUEST_ERROR;
                 break;
             case SyncException.SERVER_CONNECTION_REQUEST_ERROR:
                 syncStatus = SyncListener.SERVER_CONNECTION_REQUEST_ERROR;
                 break;
             case SyncException.BACKEND_AUTH_ERROR:
                 syncStatus = SyncListener.BACKEND_AUTH_ERROR;
                 break;
             case SyncException.NOT_FOUND_URI_ERROR:
                 syncStatus = SyncListener.URI_NOT_FOUND_ERROR;
                 break;
             case SyncException.CONNECTION_BLOCKED_BY_USER:
                 syncStatus = SyncListener.CONNECTION_BLOCKED_BY_USER;
                 break;
             case SyncException.SMART_SLOW_SYNC_UNSUPPORTED:
                 syncStatus = SyncListener.SMART_SLOW_SYNC_UNSUPPORTED;
                 break;
             case SyncException.CLIENT_ERROR:
                 syncStatus = SyncListener.CLIENT_ERROR;
                 break;
             case SyncException.ACCESS_ERROR:
                 syncStatus = SyncListener.ACCESS_ERROR;
                 break;
             case SyncException.DATA_NULL:
                 syncStatus = SyncListener.DATA_NULL;
                 break;
             case SyncException.ILLEGAL_ARGUMENT:
                 syncStatus = SyncListener.ILLEGAL_ARGUMENT;
                 break;
             case SyncException.SERVER_ERROR:
                 syncStatus = SyncListener.SERVER_ERROR;
                 break;
             case SyncException.SERVER_BUSY:
                 syncStatus = SyncListener.SERVER_BUSY;
                 break;
             case SyncException.BACKEND_ERROR:
                 syncStatus = SyncListener.BACKEND_ERROR;
                 break;
             case SyncException.CANCELLED:
                 syncStatus = SyncListener.CANCELLED;
                 break;
             case SyncException.ERR_READING_COMPRESSED_DATA:
                 syncStatus = SyncListener.COMPRESSED_RESPONSE_ERROR;
                 break;
             case SyncException.DEVICE_FULL:
                 syncStatus = SyncListener.SERVER_FULL_ERROR;
                 break;
             case SyncException.LOCAL_DEVICE_FULL:
                syncStatus = SyncListener.LOCAL_DEVICE_FULL_ERROR;
                 break;
             default:
                 syncStatus = SyncListener.GENERIC_ERROR;
                 break;
         }
         return syncStatus;
     }
 
     private void releaseResources() {
         // Release resources
         this.syncStatus = null;
         this.hierarchy = null;
         this.statusList = null;
 
         this.source = null;
         this.sessionID = null;
         this.serverUrl = null;
 
         this.busy = false;
         ObjectsPool.releaseAll();
     }
 
     private void logMessage(byte msg[], boolean hideData) {
         if (Log.getLogLevel() > Log.INFO) {
             try {
 
                 // if the message is XML, there is no need to parse it here.
                 // Just dump it
                 if (wbxml) {
                     SyncML syncMLMsg = parser.parse(msg);
                     // We must format the message in XML and then print it
                     SyncMLFormatter xmlFormatter = new SyncMLFormatter(false);
                     xmlFormatter.setHideData(hideData);
                     xmlFormatter.setPrettyPrint(true);
                     ByteArrayOutputStream os = new ByteArrayOutputStream();
                     xmlFormatter.format(syncMLMsg, os, "UTF-8");
                     if (Log.isLoggable(Log.DEBUG)) {
                         Log.debug(TAG_LOG, os.toString());
                     }
                 } else {
                     if (Log.isLoggable(Log.DEBUG)) {
                         Log.debug(TAG_LOG, new String(msg, "UTF-8"));
                     }
                 }
             } catch (Exception e) {
                 Log.error(TAG_LOG, "Cannot print message: " + e.toString());
             }
         }
     }
 
     private void logMessage(SyncML syncMLMsg, boolean hideData) {
         if (Log.getLogLevel() > Log.INFO) {
             try {
                 // We must format the message in XML and then print it
                 SyncMLFormatter xmlFormatter = new SyncMLFormatter(false);
                 xmlFormatter.setHideData(hideData);
                 xmlFormatter.setPrettyPrint(true);
                 ByteArrayOutputStream os = new ByteArrayOutputStream();
                 xmlFormatter.format(syncMLMsg, os, "UTF-8");
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, os.toString());
                 }
             } catch (Exception e) {
                 Log.error(TAG_LOG, "Cannot print message: " + e.toString());
             }
         }
     }
 
     private void logBinaryMessage(byte msg[]) {
         if (logBinaryMessages && Log.getLogLevel() > Log.INFO) {
             StringBuffer binMsg = new StringBuffer();
             for(int i=0;i<msg.length;++i) {
                 byte b = msg[i];
                 int  v = ((int)b) & 0xFF;
                 String hexValue = Integer.toHexString(v);
                 // The value must be printed as double digits
                 if (hexValue.length() < 2) {
                     hexValue = "0" + hexValue;
                 }
 
                 binMsg.append(hexValue);
                 binMsg.append(" ");
             }
 
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, binMsg.toString());
             }
         }
     }
 
     private boolean hasNoResp(Boolean nr) {
         if (nr != null && nr.booleanValue()) {
             return true;
         } else {
             return false;
         }
     }
 
     private void saveSyncStatus() {
         if (syncStatus != null) {
             try {
                 syncStatus.save();
             } catch (Exception e) {
                 Log.error(TAG_LOG, "Cannot save sync status", e);
             }
         }
     }
 
     private int getSyncMLSyncMode(int sourceSyncMode) {
         int ret = SyncML.ALERT_CODE_FAST;
         switch (sourceSyncMode) {
             case SyncSource.FULL_SYNC:
                 ret = SyncML.ALERT_CODE_SLOW;
                 break;
             case SyncSource.FULL_UPLOAD:
                 ret = SyncML.ALERT_CODE_REFRESH_FROM_CLIENT;
                 break;
             case SyncSource.FULL_DOWNLOAD:
                 ret = SyncML.ALERT_CODE_REFRESH_FROM_SERVER;
                 break;
             case SyncSource.INCREMENTAL_SYNC:
                 ret = SyncML.ALERT_CODE_FAST;
                 break;
             case SyncSource.INCREMENTAL_UPLOAD:
                 ret = SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT;
                 break;
             case SyncSource.INCREMENTAL_DOWNLOAD:
                 ret = SyncML.ALERT_CODE_ONE_WAY_FROM_SERVER;
                 break;
             default:
                 Log.error(TAG_LOG, "Unexpected source sync mode " + sourceSyncMode);
         }
         return ret;
     }
 
     private int getSourceSyncMode(int syncMLSyncMode) {
         int ret = SyncSource.INCREMENTAL_SYNC;
         switch (syncMLSyncMode) {
             case SyncML.ALERT_CODE_SLOW:
                 ret = SyncSource.FULL_SYNC;
                 break;
             case SyncML.ALERT_CODE_REFRESH_FROM_CLIENT:
                 ret = SyncSource.FULL_UPLOAD;
                 break;
             case SyncML.ALERT_CODE_REFRESH_FROM_SERVER:
                 ret = SyncSource.FULL_DOWNLOAD;
                 break;
             case SyncML.ALERT_CODE_FAST:
                 ret = SyncSource.INCREMENTAL_SYNC;
                 break;
             case SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT:
                 ret = SyncSource.INCREMENTAL_UPLOAD;
                 break;
             case SyncML.ALERT_CODE_ONE_WAY_FROM_SERVER:
                 ret = SyncSource.INCREMENTAL_DOWNLOAD;
                 break;
             default:
                 Log.error(TAG_LOG, "Unexpected syncml sync mode " + syncMLSyncMode);
         }
         return ret;
     }
 
     private int getSourceStatusCode(int syncMLStatusCode) {
         if (SyncMLStatus.isSuccess(syncMLStatusCode)) {
             if (syncMLStatusCode == SyncMLStatus.CHUNKED_ITEM_ACCEPTED) {
                 return SyncSource.CHUNK_SUCCESS_STATUS;
             } else {
                 return SyncSource.SUCCESS_STATUS;
             }
         } else {
             if (syncMLStatusCode == SyncMLStatus.DEVICE_FULL) {
                 return SyncSource.SERVER_FULL_ERROR_STATUS;
             } else {
                 return SyncSource.ERROR_STATUS;
             }
         }
     }
 
 }
 
