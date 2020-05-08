 /*
  * Funambol Citadel Connector
  * (C) 2007-2008 Mathew McBride
  * http://bionicmessage.net
  * 
  * Portions of code may come from: 
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
 package net.bionicmessage.funambol.citadel.sync;
 
 import com.funambol.email.engine.source.HelperForFilter;
 import com.funambol.email.exception.EntityException;
 import com.funambol.email.model.EmailFilter;
 import com.funambol.email.pdi.converter.FolderToXML;
 import com.funambol.email.pdi.folder.Folder;
 import com.funambol.email.pdi.parser.XMLFolderParser;
 import com.funambol.email.util.Def;
 import com.funambol.framework.core.AlertCode;
 import com.funambol.framework.engine.SyncItem;
 import com.funambol.framework.engine.SyncItemImpl;
 import com.funambol.framework.engine.SyncItemKey;
 import com.funambol.framework.engine.SyncItemState;
 import com.funambol.framework.engine.source.AbstractSyncSource;
 import com.funambol.framework.engine.source.FilterableSyncSource;
 import com.funambol.framework.engine.source.SyncContext;
 import com.funambol.framework.engine.source.SyncSourceException;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.bionicmessage.funambol.citadel.store.CitadelMailObject;
 import net.bionicmessage.funambol.citadel.store.CtdlFnblConstants;
 import net.bionicmessage.funambol.citadel.store.EmailObjectStore;
 import net.bionicmessage.funambol.citadel.util.HTMLFormatter;
 import net.bionicmessage.funambol.util.Lookup;
 
 /**
  *
  * @author matt
  */
 public class CitadelSyncSource extends AbstractSyncSource implements FilterableSyncSource {
 
     protected SyncContext ctx = null;
     protected EmailObjectStore eos = null;
     protected Properties syncSourceProperties = null;
     protected Logger log = null;
     protected String storeLoc = null;
     protected FileHandler fh = null;
     public static final SyncItemKey inboxKey = new SyncItemKey("ROOT/I");
     public static final SyncItemKey sentKey = new SyncItemKey("ROOT/S");
     public static final SyncItemKey trashKey = new SyncItemKey("ROOT/T");
     public static final SyncItemKey outboxKey = new SyncItemKey("ROOT/O");
     protected EmailFilter filter = null;
     protected String userStoreLoc = null;
     private SyncItemKey[] updatedSyncItemKeys = null;
 
     public void CitadelSyncSource() {
     }
 
     /**
      * Called before any other synchronization method. To interrupt the sync
      * process, throw a SyncSourceException.
      *
      * @param syncContext the context of the sync.
      *
      * @see SyncContext
      *
      * @throws SyncSourceException to interrupt the process with an error
      */
     public void beginSync(SyncContext syncContext) throws SyncSourceException {
         log = Logger.getLogger(CtdlFnblConstants.SYNC_SOURCE_LOGGER + "-" + System.currentTimeMillis());
         this.ctx = syncContext;
         storeLoc = syncSourceProperties.getProperty(CtdlFnblConstants.STORE_LOC);
         //todo: escape
         userStoreLoc = storeLoc + File.separatorChar + ctx.getPrincipal().getDeviceId();
 
         File storeDir = new File(userStoreLoc);
         if (!storeDir.exists()) {
             storeDir.mkdirs();
         }
         try {
             setupLogging();
             log.info("beginSync(" + ctx.getPrincipal().getName() + ")");
         } catch (IOException ex) {
             throw new SyncSourceException("Cannot set up logging", ex);
         }
         Properties storeProps = new Properties(syncSourceProperties);
         storeProps.setProperty(CtdlFnblConstants.STORE_LOC, userStoreLoc);
         if (ctx.getSyncMode() == AlertCode.SLOW) {
             log.info("Slow sync mode. Purging old data");
             storeProps.setProperty(CtdlFnblConstants.PURGE_DB_OPTION, "");
         }
         eos = new EmailObjectStore(storeProps);
         eos.setCredentials(ctx.getPrincipal().getUsername(),
                 ctx.getPrincipal().getUser().getPassword());
         if (ctx.getFilterClause() != null && ctx.getFilterClause().getClause() != null) {
             try {
                 filter = HelperForFilter.setFilter(ctx, 1, 2, true, true, false, false, false);
             } catch (EntityException ex) {
                 log.log(Level.SEVERE, "Error setting up filter", ex);
             }
 
         }
         try {
             long fromTime = 0;
            if (filter != null)
                 fromTime = filter.getTime().getTime();
             eos.startSync(fromTime);
         } catch (Exception ex) {
             log.log(Level.SEVERE, "Failure in server sync", ex);
             throw new SyncSourceException("Error in server sync", ex);
         }
         updatedSyncItemKeys = null;
     }
 
     /**
      * Called after the modifications have been applied.
      *
      * @throws SyncSourceException to interrupt the process with an error
      */
     public void endSync() throws SyncSourceException {
         log.info("endSync()");
         try {
             eos.close();
             fh.close();
         } catch (Exception ex) {
             throw new SyncSourceException("Error closing database", ex);
         }
     }
 
     /**
      * Commits the changes applied during the sync session. If the underlying
      * datastore can not commit the changes, a SyncSourceException is thrown.
      *
      * @throws SyncSourceException if the changes cannot be committed
      */
     public void commitSync() throws SyncSourceException {
         log.info("commitSync()");
     }
 
     /**
      * Called to get the keys of the items updated in the time frame sinceTs - untilTs.
      * <br><code>sinceTs</code> null means all keys of the items updated until <code>untilTs</code>.
      * <br><code>untilTs</code> null means all keys of the items updated since <code>sinceTs</code>.
      *
      * @param sinceTs consider the changes since this point in time.
      * @param untilTs consider the changes until this point in time.
      *
      * @return an array of keys containing the <code>SyncItemKey</code>'s key of the updated
      *         items in the given time frame. It MUST NOT return null for
      *         no keys, but instad an empty array.
      */
     public SyncItemKey[] getUpdatedSyncItemKeys(Timestamp sinceTs,
             Timestamp untilTs)
             throws SyncSourceException {
         log.info("getUpdatedSyncItemKeys()");
         List<CitadelMailObject> seenStatus = eos.getSeenStatusUpdated();
         updatedSyncItemKeys = new SyncItemKey[seenStatus.size()];
         for (int i = 0; i < seenStatus.size(); i++) {
             CitadelMailObject cmo = seenStatus.get(i);
             String key = "I/" + Long.toString(cmo.getCtdlMessagePointer());
             updatedSyncItemKeys[i] = new SyncItemKey(key);
             log.fine("Updated " + cmo.getCtdlMessagePointer());
         }
         return updatedSyncItemKeys;
     }
 
     /**
      * Called to get the keys of the items deleted in the time frame sinceTs - untilTs.
      * <br><code>sinceTs</code> null means all keys of the items deleted until <code>untilTs</code>.
      * <br><code>untilTs</code> null means all keys of the items deleted since <code>sinceTs</code>.
      *
      * @param sinceTs consider the changes since this point in time.
      * @param untilTs consider the changes until this point in time.
      *
      * @return an array of keys containing the <code>SyncItemKey</code>'s key of the deleted
      *         items in the given time frame. It MUST NOT return null for
      *         no keys, but instad an empty array.
      */
     public SyncItemKey[] getDeletedSyncItemKeys(Timestamp sinceTs,
             Timestamp untilTs)
             throws SyncSourceException {
         log.info("getDeletedSyncItemKeys()");
         List<CitadelMailObject> deletedObjects = eos.getDeletedOnServerObjects();
         SyncItemKey[] deletedKeys = new SyncItemKey[deletedObjects.size()];
         int i = 0;
         Iterator<CitadelMailObject> deletedIterator = deletedObjects.iterator();
         while (deletedIterator.hasNext()) {
             CitadelMailObject deleted = deletedIterator.next();
             SyncItemKey dKey = new SyncItemKey(
                     Long.toString(deleted.getCtdlMessagePointer()));
             deletedKeys[i++] = dKey;
         }
         return deletedKeys;
     }
 
     /**
      * Called to get the keys of the items created in the time frame sinceTs - untilTs.
      * <br><code>sinceTs</code> null means all keys of the items created until <code>untilTs</code>.
      * <br><code>untilTs</code> null means all keys of the items created since <code>sinceTs</code>.
      *
      * @param sinceTs consider the changes since this point in time.
      * @param untilTs consider the changes until this point in time.
      *
      * @return an array of keys containing the <code>SyncItemKey</code>'s key of the created
      *         items in the given time frame. It MUST NOT return null for
      *         no keys, but instad an empty array.
      */
     public SyncItemKey[] getNewSyncItemKeys(Timestamp sinceTs,
             Timestamp untilTs)
             throws SyncSourceException {
         log.info("getNewSyncItemKeys()");
         List<CitadelMailObject> newOnServer = eos.getAddedOnServerObjects();
         int newSize = newOnServer.size();
         SyncItemKey[] newKeys = new SyncItemKey[newSize];
         Iterator<CitadelMailObject> it = newOnServer.iterator();
         int i = 0;
         while (it.hasNext()) {
             CitadelMailObject newMail = it.next();
             String msgPointerF = "I/" + Long.toString(newMail.getCtdlMessagePointer());
             SyncItemKey newKey = new SyncItemKey(msgPointerF);
             newKeys[i++] = newKey;
         }
         return newKeys;
     }
 
     /**
      * Adds a new <code>SyncItem</code>.
      * The item is also returned giving the opportunity to the
      * source to modify its content and return the updated item (i.e. updating
      * the id to the GUID).
      *
      * @param syncInstance  the item to add
      *
      * @return the inserted item
      *
      * @throws SyncSourceException in case of error (for instance if the
      *         underlying data store runs into problems)
      */
     public SyncItem addSyncItem(SyncItem syncInstance)
             throws SyncSourceException {
         try {
             log.info("addSyncItem(" + syncInstance.getKey().getKeyAsString() + ")");
             InboundEmailItem iei = new InboundEmailItem();
             iei.setSourceItem(syncInstance);
             // What room does it belong to?
             char prefix = determinePrefix(syncInstance.getKey());
             String room = syncSourceProperties.getProperty(CtdlFnblConstants.ROOM_MAIL);
             if (prefix != 'I' && prefix == 'S') {
                 room = syncSourceProperties.getProperty(CtdlFnblConstants.ROOM_SENT);
             }
             String toString = iei.getToAddresses();
             String ccString = iei.getCCAddresses();
             String bccString = iei.getBCCAddresses();
             String subject = iei.getSubject();
             String fromName = iei.getSenderPersonName();
             String fromMail = iei.getSenderAddress();
             String rfc822 = iei.getRFC822Content();
             // Pray to $DEITY it works
             eos.getToolkit().postMessage(room, toString, subject, fromName, ccString, bccString, fromMail, rfc822);
             return iei.generateAckSyncItem(this);
         } catch (Exception ex) {
             log.throwing(this.getClass().getName(), "addSyncItem", ex);
             throw new SyncSourceException(ex);
         }
     }
 
     /**
      * Update a <code>SyncItem</code>.
      * The item is also returned giving the opportunity to the
      * source to modify its content and return the updated item (i.e. updating
      * the id to the GUID).
      *
      * @param syncInstance the item to replace
      *
      * @return the updated item
      *
      * @throws SyncSourceException in case of error (for instance if the
      *         underlying data store runs into problems)
      */
     public SyncItem updateSyncItem(SyncItem syncInstance)
             throws SyncSourceException {
         log.info("updateSyncItem(" + syncInstance.getKey().getKeyAsString() + ")");
         try {
             InboundEmailItem inboundEmailItem = new InboundEmailItem();
             inboundEmailItem.setSourceItem(syncInstance);
 
             boolean isRead = inboundEmailItem.isRead();
             SyncItemKey originalKey = removePrefix(syncInstance.getKey());
             CitadelMailObject cmo = eos.getMessageByPointer(originalKey.getKeyAsString());
             if (cmo.isSeen() != isRead) {
                 // Set message flag
                 cmo.setSeen(isRead);
                 eos.getToolkit().setSeenStatus(originalKey.getKeyAsString(), isRead);
             }
         } catch (Exception ex) {
             log.log(Level.SEVERE, null, ex);
         }
         return getSyncItemFromId(syncInstance.getKey()); // don't do anything, just yet.
     }
 
     /**
      * Removes a SyncItem given its key.
      *
      * @param itemKey the key of the item to remove
      * @param time the time of the deletion
      * @param softDelete is a soft delete ?
      *
      * @throws SyncSourceException in case of error (for instance if the
      *         underlying data store runs into problems)
      */
     public void removeSyncItem(SyncItemKey itemKey, Timestamp time, boolean softDelete)
             throws SyncSourceException {
         log.info("removeSyncItem(" + itemKey.getKeyAsString() + ")");
         if (!softDelete) {
             try {
                 SyncItemKey withoutFolder = removePrefix(itemKey);
                 CitadelMailObject cmo = eos.getMessageByPointer(withoutFolder.getKeyAsString());
                 //eos.getToolkit().deleteMessage(withoutFolder.getKeyAsString());
                 eos.moveToTrash(withoutFolder.getKeyAsString());
             } catch (Exception ex) {
                 log.log(Level.SEVERE, "Exception while deleting message", ex);
                 throw new SyncSourceException(ex);
             }
         }
     }
 
     /**
      * Called to get the keys of all items accordingly with the parameters
      * used in the beginSync call.
      * @return an array of all <code>SyncItemKey</code>s stored in this source.
      *         If there are no items an empty array is returned.
      *
      * @throws SyncSourceException in case of error (for instance if the
      *         underlying data store runs into problems)
      */
     public SyncItemKey[] getAllSyncItemKeys()
             throws SyncSourceException {
         log.info("getAllSyncItemKeys()");
         ArrayList keysToOutput = new ArrayList();
         Iterator<String> mailIterator = null;
         /* Iterator<String> pointerIterator = allObjs.iterator();
         while (pointerIterator.hasNext()) {
         String msgPointer = pointerIterator.next();
         allKeys[i++] = new SyncItemKey(msgPointer);
         } */
         long filterTime = 0;
         if (filter != null && filter.getTime() != null) {
             filterTime = filter.getTime().getTime();
             mailIterator = eos.listMessagesInRoomFromTime("Mail", filterTime).iterator();
         } else {
             mailIterator = eos.listMessagesInRoom("Mail").iterator();
         }
         while (mailIterator.hasNext()) {
             boolean allow = true;
             String msgPointer = mailIterator.next();
             String key = "I/" + msgPointer;
             CitadelMailObject cmo = eos.getMessageByPointer(msgPointer);
             Date mdate = cmo.getTime();
             // Do filter check
             if (filter != null &&
                     filter.getTime() != null &&
                     mdate.getTime() > filter.getTime().getTime()) {
                 keysToOutput.add(new SyncItemKey(key));
             } else if (filter == null || filter.getTime() == null) {
                 keysToOutput.add(new SyncItemKey(key));
             }
         }
 
         keysToOutput.add(inboxKey);
         keysToOutput.add(outboxKey);
         SyncItemKey[] allKeys = new SyncItemKey[keysToOutput.size()];
         keysToOutput.toArray(allKeys);
         return allKeys;
     }
 
     /**
      * Called to get the item with the given key.
      *
      * @return return the <code>SyncItem</code> corresponding to the given
      *         key. If no item is found, null is returned.
      *
      * @param syncItemKey the key of the SyncItem to return
      *
      * @throws SyncSourceException in case of errors (for instance if the
      *         underlying data store runs into problems)
      */
     public SyncItem getSyncItemFromId(SyncItemKey syncItemKey) throws SyncSourceException {
         log.info("getSyncItemFromId(" + syncItemKey.getKeyAsString() + ")");
         if (syncItemKey.getKeyAsString().startsWith("ROOT/")) {
             return getSyncItemForFolder(syncItemKey);
         }
         SyncItemKey actual = removePrefix(syncItemKey);
         log.info("Actual sync item key: " + actual.getKeyAsString());
         boolean isAnUpdate = Lookup.isInArray(updatedSyncItemKeys, syncItemKey);
         CitadelMailObject cmo = null;
         OutboundEmailItem omi = new OutboundEmailItem();
         if (!isAnUpdate) {
             try {
                 cmo = eos.getFilledMessageByPointer(actual.getKeyAsString());
                 if (cmo == null) {
                     return new SyncItemImpl(this, syncItemKey, SyncItemState.DELETED);
                 }
             } catch (Exception ex) {
                 Logger.getLogger(CitadelSyncSource.class.getName()).log(Level.SEVERE, null, ex);
                 throw new SyncSourceException("Error getting message: " + actual.getKeyAsString(), ex);
             }
             omi.setMailObject(cmo);
             if (filter != null) {
                 omi.setCropAtBytes(filter.getNumBytes());
             }
             try {
                 if (filter != null && filter.getSize() == 63 && omi.doAttachmentsFitInsideCrop()) {
                     // Does it fit? Attach the parts
                     eos.fillPartsForMessage(cmo);
                     omi.setAddAttachments(true);
                 }
             } catch (Exception e) {
                 log.log(Level.WARNING, "Unable to add attachments", e);
             }
         } else {
             cmo = eos.getMessageByPointer(actual.getKeyAsString());
             omi.setMailObject(cmo);
         }
         try {
             SyncItemKey parent = null;
             if (cmo.getCtdlMessageRoom().equals("Mail")) {
                 parent = inboxKey;
 
             } else {
                 parent = sentKey;
 
             }
             SyncItem si = omi.getSyncItem(this, actual, parent);
             if (!isAnUpdate) {
                 si.setState(SyncItemState.SYNCHRONIZED);
             } else {
                 si.setState(SyncItemState.UPDATED);
             }
             return si;
         } catch (Exception e) {
             log.throwing(this.getClass().getName(), "getSyncItemFromId", e);
             throw new SyncSourceException("Error converting message: " + syncItemKey.getKeyAsString(), e);
         }
     }
 
     /**
      * Called to retrive the keys of the twins of the given item
      *
      * @param syncItem the twin item
      *
      * @return the keys of the twin. Each source implementation is free to
      *         interpret this as it likes (i.e.: comparing all fields).
      *
     
      *
      * @throws SyncSourceException in case of errors (for instance if the
      *         underlying data store runs into problems)
      */
     public SyncItemKey[] getSyncItemKeysFromTwin(SyncItem syncItem) throws SyncSourceException {
         log.info("getSyncItemKeysFromTwin(" + syncItem.getKey().getKeyAsString() + ")");
         if (syncItem.getType().equals("application/vnd.omads-folder+xml")) {
             try {
                 ByteArrayInputStream bis = new ByteArrayInputStream(syncItem.getContent());
                 XMLFolderParser xfp = new XMLFolderParser(bis);
                 Folder f = xfp.parse();
                 if (f.getRole().getPropertyValueAsString().equals(Def.FOLDER_INBOX_ROLE)) {
                     return new SyncItemKey[]{inboxKey};
                 } else if (f.getRole().getPropertyValueAsString().equals(Def.FOLDER_OUTBOX_ROLE)) {
                     return new SyncItemKey[]{outboxKey};
                 }
             } catch (Exception e) {
                 log.log(Level.WARNING, "Error getting twin for folder", e);
             }
         }
         return null;
     }
 
     /**
      * Called by the engine to notify an operation status.
      * @param operationName the name of the operation.
      *        One between:
      *        - Add
      *        - Replace
      *        - Delete
      * @param status the status of the operation
      * @param keys the keys of the items
      */
     public void setOperationStatus(String operationName, int status, SyncItemKey[] keys) {
         StringBuffer logOutput = new StringBuffer();
         for (int i = 0; i < keys.length; i++) {
             SyncItemKey syncItemKey = keys[i];
             logOutput.append("setOperationStatus(").append(operationName).append(",");
             logOutput.append(status).append(",").append(keys[i].getKeyAsString());
             logOutput.append(")\n");
         }
         log.info(logOutput.toString());
 
     }
 
     public void setSyncSourceProperties(Properties syncSourceProperties) {
         this.syncSourceProperties = syncSourceProperties;
     }
 
     public Properties getSyncSourceProperties() {
         return syncSourceProperties;
     }
 
     protected void setupLogging() throws IOException {
         String connectorLog = null;
         String suffix = "";
         if (syncSourceProperties.getProperty(CtdlFnblConstants.CONNECTOR_SINGLE_LOG_OPTION) == null) {
             suffix = "-" + System.currentTimeMillis();
         }
         connectorLog = String.format("%s%s%s%s.html",
                 userStoreLoc,
                 File.separator,
                 "funambol",
                 suffix);
         File logFile = new File(connectorLog);
         fh = new FileHandler(connectorLog);
         fh.setFormatter(new HTMLFormatter());
         fh.setLevel(Level.ALL);
         log.setLevel(Level.ALL);
         log.addHandler(fh);
     }
 
     public String getType() {
         return "";
     }
 
     public SyncItem getSyncItemForFolder(SyncItemKey folder) throws SyncSourceException {
         try {
             Folder fl = new Folder();
             fl.getUID().setPropertyValue(folder.getKeyAsString());
             if (folder.getKeyAsString().endsWith("I")) {
                 fl.getName().setPropertyValue(Def.FOLDER_INBOX_ENG);
                 fl.getRole().setPropertyValue(Def.FOLDER_INBOX_ROLE);
             } else if (folder.getKeyAsString().endsWith("S")) {
                 fl.getName().setPropertyValue(Def.FOLDER_SENT_ENG);
                 fl.getRole().setPropertyValue(Def.FOLDER_SENT_ROLE);
             } else if (folder.getKeyAsString().endsWith("O")) {
                 fl.getName().setPropertyValue(Def.FOLDER_OUTBOX_ENG);
                 fl.getRole().setPropertyValue(Def.FOLDER_OUTBOX_ROLE);
             }
             fl.getParentId().setPropertyValue("ROOT");
             FolderToXML ftxml = new FolderToXML(null, null);
             String content = ftxml.convert(fl);
             SyncItemImpl si = new SyncItemImpl(this, folder);
             si.setContent(content.getBytes("UTF-8"));
             si.setType("application/vnd.omads-folder+xml");
             return si;
         } catch (Exception ex) {
             log.throwing(this.getClass().getName(), "getSyncItemFromFolder", ex);
             throw new SyncSourceException(ex);
         }
     }
 
     /** Remove a prefix (I/, S/ etc.) */
     private SyncItemKey removePrefix(SyncItemKey fullKey) {
         String fullString = fullKey.getKeyAsString();
         if (fullString.contains("/")) {
             String[] split = fullString.split("/");
             fullString = split[1];
         }
         fullKey.setKeyValue(fullString);
         return fullKey;
     }
 
     private char determinePrefix(SyncItemKey fullKey) {
         String fullString = fullKey.getKeyAsString();
         if (fullString.substring(0, 3).contains("/")) {
             return fullString.charAt(0);
         } else {
             return 'I';
         }
     }
 
     public char getSyncItemStateFromId(SyncItemKey arg0) throws SyncSourceException {
         log.log(Level.FINER, "getSyncItemStateFromId(" + arg0.getKeyAsString() + ")");
         if (arg0.getKeyAsString().contains("O/")) {
             return SyncItemState.DELETED;
         }
         return SyncItemState.SYNCHRONIZED;
     }
 
     public boolean isSyncItemInFilterClause(SyncItem arg0) throws SyncSourceException {
         if (arg0 != null) {
             log.log(Level.FINER, "isSyncItemInFilterClause(" + arg0.getKey().getKeyAsString() + ")");
             return true;
         }
         return false;
     }
 
     public boolean isSyncItemInFilterClause(SyncItemKey arg0) throws SyncSourceException {
         if (arg0 != null) {
             log.log(Level.FINER, "isSyncItemInFilterClause(" + arg0.getKeyAsString() + ")");
             return true;
         }
         return false;
     }
 }
