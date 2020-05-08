 /*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.sakaiproject.nakamura.lite.content;
 
 import static org.sakaiproject.nakamura.lite.content.InternalContent.BLOCKID_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.BODY_CREATED;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.BODY_CREATED_BY;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.BODY_LAST_MODIFIED;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.BODY_LAST_MODIFIED_BY;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.COPIED_DEEP;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.COPIED_FROM_ID;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.COPIED_FROM_PATH;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.CREATED;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.CREATED_BY;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.DELETED_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.LASTMODIFIED;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.LASTMODIFIED_BY;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.LENGTH_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.LINKED_PATH_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.NEXT_VERSION_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.PATH_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.PREVIOUS_BLOCKID_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.PREVIOUS_VERSION_UUID_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.READONLY_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.STRUCTURE_UUID_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.TRUE;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.UUID_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.VERSION_HISTORY_ID_FIELD;
 import static org.sakaiproject.nakamura.lite.content.InternalContent.VERSION_NUMBER;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 import org.sakaiproject.nakamura.api.lite.Configuration;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.api.lite.StoreListener;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessControlManager;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Permissions;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.Security;
 import org.sakaiproject.nakamura.api.lite.content.Content;
 import org.sakaiproject.nakamura.api.lite.content.ContentManager;
 import org.sakaiproject.nakamura.lite.storage.RemoveProperty;
 import org.sakaiproject.nakamura.lite.storage.StorageClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 /**
  * <pre>
  * Content Manager.
  * Manages two types of content,
  * Bundles of content properties and bodies.
  * Bodies are chunked into sizes to aide efficiency when retrieving the content.
  * 
  * CF content stores the structure of the content keyed by path.
  * Each item contains child names in columns + the guid of the item
  * eg
  *   path : {
  *       ':id' : thisitemUUID,
  *       subitemA : subitemAUUID,
  *       subitemB : subitemBUUID
  *   }
  * the guid of the item points to the CF content version where items are keyed by the version.
  * These items also contain child nodes under children as an array
  * 
  * eg
  *    itemUUID : {
  *         'id' : thisitemUUID
  *         'children' : [ 
  *           subitemA : subitemAUUID,
  *           subitemB : subitemBUUID
  *         ],
  *         'nblocks' = numberOfBlocksSetsOfContent
  *         'length' = totalLenghtOftheContent
  *         'blocksize' = storageBlockSize
  *         'blockid' = blockID
  *         ... other properties ...
  *    }
  *    
  * The content blocks are stored in CF content body
  * eg
  *   blockID:blockSetNumber : {
  *         'id' : blockID,
  *         'numblocks' : numberOfBlocksInThisSet,
  *         'blocklength0' : lengthOfThisBlock,
  *         'body0' : byte[]
  *         'blocklength1' : lengthOfThisBlock,
  *         'body1' : byte[]
  *         ...
  *         'blocklengthn' : lengthOfThisBlock,
  *         'bodyn' : byte[]
  *    }
  * 
  * 
  * Versioning:
  * 
  * When a version is saved, the CF contentVersion item is cloned and the CF content :id and any subitems IDs are updated.
  * Block 0 is marked as readonly
  * 
  * When the body is written to its CF content row is checked to see if the block is read only. If so a new block is created with and linked in with 'previousversion'
  * A version object is also created to keep track of the versions.
  * 
  * </pre>
  * 
  * @author ieb
  * 
  */
 public class ContentManagerImpl implements ContentManager {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(ContentManagerImpl.class);
 
     /**
      * Key containing deleted items.
      */
     private static final String DELETEDITEMS_KEY = ":deleteditems";
 
     private static final Set<String> DEEP_COPY_FILTER = ImmutableSet.of(LASTMODIFIED,
             LASTMODIFIED_BY, UUID_FIELD, PATH_FIELD);
 
     /**
      * Storage Client
      */
     private StorageClient client;
     /**
      * The access control manager in use.
      */
     private AccessControlManager accessControlManager;
     /**
      * Key space for this content.
      */
     private String keySpace;
     /**
      * Column Family for this content.
      */
     private String contentColumnFamily;
 
     private boolean closed;
 
     private StoreListener eventListener;
 
     public ContentManagerImpl(StorageClient client, AccessControlManager accessControlManager,
             Configuration config, StoreListener eventListener) {
         this.client = client;
         this.accessControlManager = accessControlManager;
         keySpace = config.getKeySpace();
         contentColumnFamily = config.getContentColumnFamily();
         closed = false;
         this.eventListener = eventListener;
     }
 
     // TODO: Unit test
     public boolean exists(String path) {
         try {
             accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_READ);
             Map<String, Object> structure = client.get(keySpace, contentColumnFamily, path);
             return (structure != null && structure.size() > 0);
         } catch (AccessDeniedException e) {
             LOGGER.debug(e.getMessage(), e);
         } catch (StorageClientException e) {
             LOGGER.debug(e.getMessage(), e);
         }
         return false;
     }
 
     public Content get(String path) throws StorageClientException, AccessDeniedException {
         checkOpen();
         accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_READ);
         Map<String, Object> structure = client.get(keySpace, contentColumnFamily, path);
         if (structure != null && structure.size() > 0) {
             String contentId = StorageClientUtils.toString(structure.get(STRUCTURE_UUID_FIELD));
             Map<String, Object> content = client.get(keySpace, contentColumnFamily, contentId);
             if (content != null && content.size() > 0) {
                 Content contentObject = new Content(path, content);
                 ((InternalContent) contentObject).internalize(structure, this, false);
                 return contentObject;
             }
         }
         return null;
 
     }
 
     public void update(Content excontent) throws AccessDeniedException, StorageClientException {
         checkOpen();
         InternalContent content = (InternalContent) excontent;
         String path = content.getPath();
         accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_WRITE);
         String id = null;
         Object idStore = null;
         Map<String, Object> toSave = null;
         Map<String, Object> contentPropertes = content.getContent();
         if (content.isNew()) {
             // create the parents if necessary
             if (!StorageClientUtils.isRoot(path)) {
                 String parentPath = StorageClientUtils.getParentObjectPath(path);
                 Content parentContent = get(parentPath);
                 if (parentContent == null) {
                     update(new Content(parentPath, null));
                 }
             }
             toSave = Maps.newHashMap(contentPropertes);
             id = StorageClientUtils.getUuid();
             idStore = StorageClientUtils.toStore(id);
             toSave.put(UUID_FIELD, idStore);
             toSave.put(PATH_FIELD, StorageClientUtils.toStore(path));
             toSave.put(CREATED, StorageClientUtils.toStore(System.currentTimeMillis()));
             toSave.put(CREATED_BY,
                     StorageClientUtils.toStore(accessControlManager.getCurrentUserId()));
             LOGGER.debug("New Content with {} {} ", id, toSave);
         } else if (content.isUpdated()) {
             toSave = Maps.newHashMap(content.getUpdated());
             id = StorageClientUtils.toString(contentPropertes.get(UUID_FIELD));
             idStore = StorageClientUtils.toStore(contentPropertes.get(UUID_FIELD));
             toSave.put(LASTMODIFIED, StorageClientUtils.toStore(System.currentTimeMillis()));
             toSave.put(LASTMODIFIED_BY,
                     StorageClientUtils.toStore(accessControlManager.getCurrentUserId()));
             LOGGER.debug("Updating Content with {} {} ", id, toSave);
         } else {
             // if not new or updated, dont update.
             return;
         }
 
         Map<String, Object> checkContent = client.get(keySpace, contentColumnFamily, id);
         if (TRUE.equals(StorageClientUtils.toString(checkContent.get(READONLY_FIELD)))) {
             throw new AccessDeniedException(Security.ZONE_CONTENT, path,
                     "update on read only Content Item (possibly a previous version of the item)",
                     accessControlManager.getCurrentUserId());
         }
         boolean isnew = false;
         if (content.isNew()) {
             isnew = true;
             // only when new do we update the structure.
             if (!StorageClientUtils.isRoot(path)) {
                 client.insert(keySpace, contentColumnFamily,
                         StorageClientUtils.getParentObjectPath(path),
                         ImmutableMap.of(StorageClientUtils.getObjectName(path), idStore));
             }
             client.insert(keySpace, contentColumnFamily, path,
                     ImmutableMap.of(STRUCTURE_UUID_FIELD, idStore));
         }
         // save the content id.
         client.insert(keySpace, contentColumnFamily, id, toSave);
         LOGGER.debug("Saved {} at {} as {} ", new Object[] { path, id, toSave });
         // reset state to unmodified to take further modifications.
         content.reset();
         eventListener.onUpdate(Security.ZONE_CONTENT, path, accessControlManager.getCurrentUserId(), isnew, "op:update");        
     }
 
     public void delete(String path) throws AccessDeniedException, StorageClientException {
         checkOpen();
         accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_DELETE);
         Map<String, Object> structure = client.get(keySpace, contentColumnFamily, path);
        if ( structure != null && structure.size() > 0 ) {
            String uuid = StorageClientUtils.toString(structure.get(STRUCTURE_UUID_FIELD));
            client.remove(keySpace, contentColumnFamily, path);
            Map<String, Object> m = new HashMap<String, Object>();
            m.put(StorageClientUtils.getObjectName(path), null);
            client.insert(keySpace, contentColumnFamily, StorageClientUtils.getParentObjectPath(path),
                    m);
            client.insert(keySpace, contentColumnFamily, uuid,
                    ImmutableMap.of(DELETED_FIELD, (Object) TRUE));
            client.insert(keySpace, contentColumnFamily, DELETEDITEMS_KEY,
                    ImmutableMap.of(uuid, StorageClientUtils.toStore(path)));
            eventListener.onDelete(Security.ZONE_CONTENT, path, accessControlManager.getCurrentUserId());
        }
     }
 
     public long writeBody(String path, InputStream in) throws StorageClientException,
             AccessDeniedException, IOException {
         return writeBody(path, in, null);
     }
 
     public long writeBody(String path, InputStream in, String streamId)
             throws StorageClientException, AccessDeniedException, IOException {
         checkOpen();
         accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_WRITE);
         Map<String, Object> structure = client.get(keySpace, contentColumnFamily, path);
         String contentId = StorageClientUtils.toString(structure.get(STRUCTURE_UUID_FIELD));
         Map<String, Object> content = client.get(keySpace, contentColumnFamily, contentId);
         boolean isnew = true;
         String blockIdField = StorageClientUtils.getAltField(BLOCKID_FIELD, streamId);
         if (content.containsKey(blockIdField)) {
             isnew = false;      
         }
         String contentBlockId = StorageClientUtils.getUuid();
         
         Map<String, Object> metadata = client.streamBodyIn(keySpace, contentColumnFamily,
                 contentId, contentBlockId, streamId, content, in);
         metadata.put(StorageClientUtils.getAltField(BODY_LAST_MODIFIED, streamId),
                 StorageClientUtils.toStore(System.currentTimeMillis()));
         metadata.put(StorageClientUtils.getAltField(BODY_LAST_MODIFIED_BY, streamId),
                 StorageClientUtils.toStore(accessControlManager.getCurrentUserId()));
         if (isnew) {
             metadata.put(StorageClientUtils.getAltField(BODY_CREATED, streamId),
                     StorageClientUtils.toStore(System.currentTimeMillis()));
             metadata.put(StorageClientUtils.getAltField(BODY_CREATED_BY, streamId),
                     StorageClientUtils.toStore(accessControlManager.getCurrentUserId()));
         }
         client.insert(keySpace, contentColumnFamily, contentId, metadata);
         long length = StorageClientUtils.toLong(metadata.get(LENGTH_FIELD));
         eventListener.onUpdate(Security.ZONE_CONTENT, path, accessControlManager.getCurrentUserId(), false, "stream", streamId);        
         return length;
 
     }
 
     public InputStream getInputStream(String path) throws StorageClientException,
             AccessDeniedException, IOException {
         return getInputStream(path, null);
     }
 
     public InputStream getInputStream(String path, String streamId) throws StorageClientException,
             AccessDeniedException, IOException {
         checkOpen();
         accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_READ);
         Map<String, Object> structure = client.get(keySpace, contentColumnFamily, path);
         LOGGER.debug("Structure Loaded {} {} ", path, structure);
         String contentId = StorageClientUtils.toString(structure.get(STRUCTURE_UUID_FIELD));
         return internalGetInputStream(contentId, streamId);
     }
 
     private InputStream internalGetInputStream(String contentId, String streamId)
             throws StorageClientException, AccessDeniedException, IOException {
         Map<String, Object> content = client.get(keySpace, contentColumnFamily, contentId);
         String contentBlockId = StorageClientUtils.toString(content.get(StorageClientUtils
                 .getAltField(BLOCKID_FIELD, streamId)));
         return client.streamBodyOut(keySpace, contentColumnFamily, contentId, contentBlockId, streamId,
                 content);
     }
 
     public void close() {
         closed = true;
     }
 
     private void checkOpen() throws StorageClientException {
         if (closed) {
             throw new StorageClientException("Content Manager is closed");
         }
     }
 
     // TODO: Unit test
     public void copy(String from, String to, boolean deep) throws StorageClientException,
             AccessDeniedException, IOException {
         checkOpen();
         // To Copy, get the to object out and copy everything over.
         Content f = get(from);
         if (f == null) {
             throw new StorageClientException(" Source content " + from + " does not exist");
         }
         Content t = get(to);
         if (t != null) {
             delete(to);
         }
         Set<String> streams = Sets.newHashSet();
         Map<String, Object> copyProperties = Maps.newHashMap();
         if (deep) {
             for (Entry<String, Object> p : f.getProperties().entrySet()) {
                 if (!DEEP_COPY_FILTER.contains(p.getKey())) {
                     if (p.getKey().startsWith(BLOCKID_FIELD)) {
                         streams.add(p.getKey());
                     } else {
                         copyProperties.put(p.getKey(), p.getValue());
                     }
                 }
             }
         } else {
             copyProperties.putAll(f.getProperties());
         }
         copyProperties.put(COPIED_FROM_PATH, from);
         copyProperties.put(COPIED_FROM_ID, f.getProperty(UUID_FIELD));
         copyProperties.put(COPIED_DEEP, StorageClientUtils.toStore(deep));
         t = new Content(to, copyProperties);
         update(t);
 
         for (String stream : streams) {
             String streamId = null;
             if (stream.length() > BLOCKID_FIELD.length()) {
                 streamId = stream.substring(BLOCKID_FIELD.length() + 1);
             }
             InputStream fromStream = getInputStream(from, streamId);
             writeBody(to, fromStream);
             fromStream.close();
         }
         eventListener.onUpdate(Security.ZONE_CONTENT, to, accessControlManager.getCurrentUserId(), true, "op:copy");        
 
     }
 
     // TODO: Unit test
     public void move(String from, String to) throws AccessDeniedException, StorageClientException {
         // to move, get the structure object out and modify, recreating parent
         // objects as necessary.
         checkOpen();
         accessControlManager.check(Security.ZONE_CONTENT, from, Permissions.CAN_ANYTHING);
         accessControlManager.check(Security.ZONE_CONTENT, to,
                 Permissions.CAN_READ.combine(Permissions.CAN_WRITE));
         Map<String, Object> fromStructure = client.get(keySpace, contentColumnFamily, from);
         if (fromStructure == null || fromStructure.size() == 0) {
             throw new StorageClientException("The source content to move from " + from
                     + " does not exist, move operation failed");
         }
         Map<String, Object> toStructure = client.get(keySpace, contentColumnFamily, to);
         if (toStructure != null && toStructure.size() > 0) {
             throw new StorageClientException("The destination content to move to " + to
                     + "  exists, move operation failed");
         }
         Object idStore = fromStructure.get(STRUCTURE_UUID_FIELD);
 
         // move the conent to the new location, then delete the old.
         if (!StorageClientUtils.isRoot(to)) {
             // if not a root, modify the new parent location, creating the
             // structured if necessary
             String parent = StorageClientUtils.getParentObjectPath(to);
             Map<String, Object> parentToStructure = client.get(keySpace, contentColumnFamily,
                     parent);
             if (parentToStructure == null || parentToStructure.size() == 0) {
                 // create a new parent
                 Content content = new Content(parent, null);
                 update(content);
             }
 
             client.insert(keySpace, contentColumnFamily, parent,
                     ImmutableMap.of(StorageClientUtils.getObjectName(to), idStore));
         }
         // update the content data to reflect the new primary location.
         client.insert(keySpace, contentColumnFamily, StorageClientUtils.toString(idStore),
                 ImmutableMap.of(PATH_FIELD, StorageClientUtils.toStore(to)));
 
         // insert the new to Structure and remove the from
         client.insert(keySpace, contentColumnFamily, to, fromStructure);
 
         // now remove the old location.
         if (!StorageClientUtils.isRoot(from)) {
             // if it was not a root, then modify the old parent location.
             String fromParent = StorageClientUtils.getParentObjectPath(from);
             client.insert(keySpace, contentColumnFamily, fromParent, ImmutableMap.of(
                     StorageClientUtils.getObjectName(from), (Object) new RemoveProperty()));
         }
         // remove the old from.
         client.remove(keySpace, contentColumnFamily, from);
         eventListener.onDelete(Security.ZONE_CONTENT, from, accessControlManager.getCurrentUserId(), "op:move");        
         eventListener.onUpdate(Security.ZONE_CONTENT, to, accessControlManager.getCurrentUserId(), true, "op:move");        
 
     }
 
     // TODO: Unit test
     public void link(String from, String to) throws AccessDeniedException, StorageClientException {
         // a link places a pointer to the content in the parent of from, but
         // does not delete or modify the structure of to.
         // read from is required and write to.
         checkOpen();
         accessControlManager.check(Security.ZONE_CONTENT, to, Permissions.CAN_READ);
         accessControlManager.check(Security.ZONE_CONTENT, from,
                 Permissions.CAN_READ.combine(Permissions.CAN_WRITE));
         Map<String, Object> toStructure = client.get(keySpace, contentColumnFamily, to);
         if (toStructure == null || toStructure.size() == 0) {
             throw new StorageClientException("The source content to link from " + to
                     + " does not exist, link operation failed");
         }
         Map<String, Object> fromStructure = client.get(keySpace, contentColumnFamily, from);
         if (fromStructure != null && fromStructure.size() > 0) {
             throw new StorageClientException("The destination content to link to " + from
                     + "  exists, link operation failed");
         }
 
         if (StorageClientUtils.isRoot(from)) {
             throw new StorageClientException("The link " + to
                     + "  is a root, not possible to create a soft link");
         }
 
         // create a new structure object pointing back to the shared location
 
         Object idStore = toStructure.get(STRUCTURE_UUID_FIELD);
         // if not a root, modify the new parent location, creating the
         // structured if necessary
         String parent = StorageClientUtils.getParentObjectPath(from);
         Map<String, Object> parentToStructure = client.get(keySpace, contentColumnFamily, parent);
         if (parentToStructure == null || parentToStructure.size() == 0) {
             // create a new parent
             Content content = new Content(parent, null);
             update(content);
         }
 
         client.insert(keySpace, contentColumnFamily, parent,
                 ImmutableMap.of(StorageClientUtils.getObjectName(from), idStore));
         // create the new object for the path, pointing to the Object
         client.insert(keySpace, contentColumnFamily, from, ImmutableMap.of(STRUCTURE_UUID_FIELD,
                 idStore, LINKED_PATH_FIELD, StorageClientUtils.toStore(to)));
 
     }
 
     public String saveVersion(String path) throws StorageClientException, AccessDeniedException {
         checkOpen();
         accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_WRITE);
         Map<String, Object> structure = client.get(keySpace, contentColumnFamily, path);
         String contentId = StorageClientUtils.toString(structure.get(STRUCTURE_UUID_FIELD));
         Map<String, Object> saveVersion = client.get(keySpace, contentColumnFamily, contentId);
 
         // versionHistoryId is the UUID of the version history for this node.
 
         Object saveVersionIdS = saveVersion.get(UUID_FIELD);
         String saveVersionId = StorageClientUtils.toString(saveVersionIdS);
         
         Object versionHistoryIdS = saveVersion.get(VERSION_HISTORY_ID_FIELD);
         String versionHistoryId = StorageClientUtils.toString(versionHistoryIdS);
 
         if (versionHistoryIdS == null) {
             versionHistoryId = StorageClientUtils.getUuid();
             LOGGER.debug("Created new Version History UUID as {} for Object {} ",versionHistoryId, saveVersionId);
             versionHistoryIdS = StorageClientUtils.toStore(versionHistoryId);
             saveVersion.put(VERSION_HISTORY_ID_FIELD, versionHistoryIdS);
         } else {
             LOGGER.debug("Created new Version History UUID as {} for Object {} ",versionHistoryId, saveVersionId);
             
         }
 
         Map<String, Object> newVersion = Maps.newHashMap(saveVersion);
         String newVersionId = StorageClientUtils.getUuid();
         Object newVersionIdS = StorageClientUtils.toStore(newVersionId);
 
 
         Object saveBlockIdS = saveVersion.get(BLOCKID_FIELD);
         String saveBlockId = StorageClientUtils.toString(saveBlockIdS);
 
         newVersion.put(UUID_FIELD, newVersionIdS);
         newVersion.put(PREVIOUS_VERSION_UUID_FIELD, saveVersionIdS);
         if (saveBlockId != null) {
             newVersion.put(PREVIOUS_BLOCKID_FIELD, saveBlockIdS);
         }
 
         saveVersion.put(NEXT_VERSION_FIELD, newVersionIdS);
         saveVersion.put(READONLY_FIELD, TRUE);
         Object versionNumber = StorageClientUtils.toStore(System.currentTimeMillis());
         saveVersion.put(VERSION_NUMBER, versionNumber);
 
         client.insert(keySpace, contentColumnFamily, saveVersionId, saveVersion);
         client.insert(keySpace, contentColumnFamily, newVersionId, newVersion);
         client.insert(keySpace, contentColumnFamily, versionHistoryId,
                 ImmutableMap.of(saveVersionId, versionNumber));
         client.insert(keySpace, contentColumnFamily, path,
                 ImmutableMap.of(STRUCTURE_UUID_FIELD, newVersionIdS));
         if (!path.equals("/")) {
             client.insert(keySpace, contentColumnFamily,
                     StorageClientUtils.getParentObjectPath(path),
                     ImmutableMap.of(StorageClientUtils.getObjectName(path), newVersionIdS));
         }
         LOGGER.debug("Saved Version History  {} {} ", versionHistoryId,
                 client.get(keySpace, contentColumnFamily, versionHistoryId));
         LOGGER.debug("Saved Version [{}] {}", saveVersionId, saveVersion);
         LOGGER.debug("New Version [{}] {}", newVersionId, newVersion);
         LOGGER.debug("Structure {} ", client.get(keySpace, contentColumnFamily, path));
         LOGGER.debug(
                 "Parent Structure {} ",
                 client.get(keySpace, contentColumnFamily,
                         StorageClientUtils.getParentObjectPath(path)));
         return saveVersionId;
     }
 
     public List<String> getVersionHistory(String path) throws AccessDeniedException,
             StorageClientException {
         checkOpen();
         accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_READ);
         Map<String, Object> structure = client.get(keySpace, contentColumnFamily, path);
         if (structure != null && structure.size() > 0) {
             String contentId = StorageClientUtils.toString(structure.get(STRUCTURE_UUID_FIELD));
             Map<String, Object> content = client.get(keySpace, contentColumnFamily, contentId);
             if (content != null && content.size() > 0) {
                 String versionHistoryId = StorageClientUtils.toString(content
                         .get(VERSION_HISTORY_ID_FIELD));
                 if (versionHistoryId != null) {
                     final Map<String, Object> versionHistory = client.get(keySpace,
                             contentColumnFamily, versionHistoryId);
                     LOGGER.debug("Loaded Version History  {} {} ", versionHistoryId, versionHistory);
                     return Lists.sortedCopy(versionHistory.keySet(), new Comparator<String>() {
                         public int compare(String o1, String o2) {
                             long l1 = StorageClientUtils.toLong(versionHistory.get(o1));
                             long l2 = StorageClientUtils.toLong(versionHistory.get(o2));
                             long r = l2 - l1;
                             if (r == 0) {
                                 return 0;
                             } else if (r < 0) {
                                 return -1;
                             }
                             return 1;
                         }
                     });
 
                 }
             }
         }
         return Collections.emptyList();
     }
 
     // TODO: Unit test
     public InputStream getVersionInputStream(String path, String versionId)
             throws AccessDeniedException, StorageClientException, IOException {
         return getVersionInputStream(path, versionId, null);
     }
 
     // TODO: Unit test
     public InputStream getVersionInputStream(String path, String versionId, String streamId)
             throws AccessDeniedException, StorageClientException, IOException {
         accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_READ);
         checkOpen();
         Map<String, Object> structure = client.get(keySpace, contentColumnFamily, path);
         if (structure != null && structure.size() > 0) {
             String contentId = StorageClientUtils.toString(structure.get(STRUCTURE_UUID_FIELD));
             Map<String, Object> content = client.get(keySpace, contentColumnFamily, contentId);
             if (content != null && content.size() > 0) {
                 String versionHistoryId = StorageClientUtils.toString(content
                         .get(VERSION_HISTORY_ID_FIELD));
                 if (versionHistoryId != null) {
                     Map<String, Object> versionHistory = client.get(keySpace, contentColumnFamily,
                             versionHistoryId);
                     if (versionHistory != null && versionHistory.containsKey(versionId)) {
                         return internalGetInputStream(versionId, streamId);
                     }
                 }
             }
         }
         return null;
     }
 
     public Content getVersion(String path, String versionId) throws StorageClientException,
             AccessDeniedException {
         checkOpen();
         accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_READ);
         Map<String, Object> structure = client.get(keySpace, contentColumnFamily, path);
         if (structure != null && structure.size() > 0) {
             String contentId = StorageClientUtils.toString(structure.get(STRUCTURE_UUID_FIELD));
             Map<String, Object> content = client.get(keySpace, contentColumnFamily, contentId);
             if (content != null && content.size() > 0) {
                 String versionHistoryId = StorageClientUtils.toString(content
                         .get(VERSION_HISTORY_ID_FIELD));
                 if (versionHistoryId != null) {
                     Map<String, Object> versionHistory = client.get(keySpace, contentColumnFamily,
                             versionHistoryId);
                     if (versionHistory != null && versionHistory.containsKey(versionId)) {
                         Map<String, Object> versionContent = client.get(keySpace,
                                 contentColumnFamily, versionId);
                         if (versionContent != null && versionContent.size() > 0) {
                             Content contentObject = new Content(path, versionContent);
                             ((InternalContent) contentObject).internalize(structure, this, true);
                             return contentObject;
                         } else {
                             LOGGER.debug("No Content for path {} version History Null{} ", path,
                                     versionHistoryId);
 
                         }
                     } else {
                         LOGGER.debug("History null for path {} version History {} {} ",
                                 new Object[] { path, versionHistoryId, versionHistory });
                     }
                 } else {
                     LOGGER.debug("History Id null for path {} ", path);
                 }
             }
         }
         return null;
     }
 
 }
