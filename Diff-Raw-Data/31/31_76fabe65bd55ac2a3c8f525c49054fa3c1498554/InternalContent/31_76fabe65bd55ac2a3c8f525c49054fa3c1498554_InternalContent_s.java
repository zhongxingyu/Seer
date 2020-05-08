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
 
 import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 import org.apache.commons.lang.StringUtils;
 import org.sakaiproject.nakamura.api.lite.RemoveProperty;
 import org.sakaiproject.nakamura.api.lite.Repository;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.api.lite.content.Content;
 import org.sakaiproject.nakamura.lite.ConfigurationImpl;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 /**
  * Internal Content Object for holding sparse Content objects. Has a protected
  * constructor for internal use.
  */
 public class InternalContent {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(Content.class);
 
     public static final String INTERNAL_FIELD_PREFIX = Repository.SYSTEM_PROP_PREFIX + ":";
     /**
      * The ID of a content item
      */
     private static String UUID_FIELD = ConfigurationImpl.DEFAULT_UUID_FIELD;
 
     static boolean idFieldIsSet = false;
     /**
      * The path of the content item (used in structure row)
      */
     public static final String PATH_FIELD = Repository.SYSTEM_PROP_PREFIX + "path";
     /**
      * The parent path (
      */
     public static final String PARENT_HASH_FIELD = INTERNAL_FIELD_PREFIX + "parenthash";
 
     /**
      * content item ID referenced by a Structure item
      */
     public static final String STRUCTURE_UUID_FIELD = INTERNAL_FIELD_PREFIX + "cid";
     /**
      * Where a structure object is a link, this field contains the location of
      * the target of the link
      */
     public static final String LINKED_PATH_FIELD = INTERNAL_FIELD_PREFIX + "link";
     /**
      * BlockID where the body of this content item is stored, if there is a body
      * (content row)
      */
     public static final String BLOCKID_FIELD = Repository.SYSTEM_PROP_PREFIX + "blockId";
     /**
      * ID of the previous version (content row)
      */
     public static final String PREVIOUS_VERSION_UUID_FIELD = Repository.SYSTEM_PROP_PREFIX
             + "previousVersion";
     /**
      * Previous Block ID. (content row)
      */
     public static final String PREVIOUS_BLOCKID_FIELD = Repository.SYSTEM_PROP_PREFIX
             + "previousBlockId";
     /**
      * The ID of the next version (content row)
      */
     public static final String NEXT_VERSION_FIELD = Repository.SYSTEM_PROP_PREFIX + "nextVersion";
     /**
      * Set to "Y" if the content item is read only. (content row)
      */
     public static final String READONLY_FIELD = Repository.SYSTEM_PROP_PREFIX + "readOnly";
     /**
      * set to "Y" if deleted. (content row)
      */
     public static final String DELETED_FIELD = Repository.SYSTEM_PROP_PREFIX + "deleted";
 
     /**
      * The block size in bytes in each block in a block set, if body store uses
      * blocking (content row)
      */
     public static final String BLOCKSIZE_FIELD = Repository.SYSTEM_PROP_PREFIX + "blocksize";
     /**
      * Total length of the content body (content row)
      */
     public static final String LENGTH_FIELD = Repository.SYSTEM_PROP_PREFIX + "length";
     /**
      * The number of block sets in a body (content row)
      */
     public static final String NBLOCKS_FIELD = Repository.SYSTEM_PROP_PREFIX + "nblocks";
     /**
      * Yes, True, etc
      */
     public static final String TRUE = "Y";
 
     /**
      * The date (stored as GMT epoch long) the body was last modified. (content
      * row)
      */
     public static final String BODY_LAST_MODIFIED_FIELD = Repository.SYSTEM_PROP_PREFIX
             + "bodyLastModified";
 
     /**
      * The user ID that last modified the body. (content row)
      */
     public static final String BODY_LAST_MODIFIED_BY_FIELD = Repository.SYSTEM_PROP_PREFIX
             + "bodyLastModifiedBy";
 
     /**
      * The date the body was created (GMT epoch long) (content row)
      */
     public static final String BODY_CREATED_FIELD = Repository.SYSTEM_PROP_PREFIX + "bodyCreated";
 
     /**
      * The user that created the body. (content row)
      */
     public static final String BODY_CREATED_BY_FIELD = Repository.SYSTEM_PROP_PREFIX
             + "bodyCreatedBy";
 
     /**
      * The time the item was created. (content row)
      */
     public static final String CREATED_FIELD = Repository.SYSTEM_PROP_PREFIX + "created";
 
     /**
      * The user that created the item. (content row)
      */
     public static final String CREATED_BY_FIELD = Repository.SYSTEM_PROP_PREFIX + "createdBy";
 
     /**
      * The time the item was last modified. (content row)
      */
     public static final String LASTMODIFIED_FIELD = Repository.SYSTEM_PROP_PREFIX + "lastModified";
 
     /**
      * The user that lastModified the item. (content row)
      */
     public static final String LASTMODIFIED_BY_FIELD = Repository.SYSTEM_PROP_PREFIX
             + "lastModifiedBy";
 
     /**
      * The path the content object was copied from if it was copied
      */
     public static final String COPIED_FROM_PATH_FIELD = Repository.SYSTEM_PROP_PREFIX
             + "copiedFrom";
 
     /**
      * The ID the content object was copied from.
      */
     public static final String COPIED_FROM_ID_FIELD = Repository.SYSTEM_PROP_PREFIX
             + "copiedFromId";
 
     /**
      * If the copy was deep, then true
      */
     public static final String COPIED_DEEP_FIELD = Repository.SYSTEM_PROP_PREFIX + "copiedDeep";
 
     /**
      * Mime type
      */
     public static final String MIMETYPE_FIELD = Repository.SYSTEM_PROP_PREFIX + "mimeType";
 
     /**
      * Charset encoding if char based.
      */
     public static final String ENCODING_FIELD = Repository.SYSTEM_PROP_PREFIX + "encoding";
 
     /**
      * 
      */
     public static final String VERSION_HISTORY_ID_FIELD = Repository.SYSTEM_PROP_PREFIX
             + "versionHistoryId";
 
     /**
      * 
      */
     public static final String VERSION_NUMBER_FIELD = Repository.SYSTEM_PROP_PREFIX
             + "versionNumber";
 
     /**
      * The who this version was saved by
      */
     public static final String VERSION_SAVEDBY_FIELD = Repository.SYSTEM_PROP_PREFIX
             + "versionSavedBy";
 
     /**
      * Map of the content object itself.
      */
     private ImmutableMap<String, Object> content;
     /**
      * Path locating this content object within the overall content structure.
      */
     private String path;
     /**
      * The ContentManager that manages this content object.
      */
     private ContentManagerImpl contentManager;
     /**
      * Map of content object that has been updated since this content object was
      * retrieved.
      */
     private Map<String, Object> updatedContent;
     /**
      * True if updated.
      */
     private boolean updated;
     /**
      * True if the content is new.
      */
     private boolean newcontent;
     private boolean readOnly;
 
     /**
      * Create a new Content Object that has not been persisted
      * 
      * @param path
      *            the path.
      * @param content
      *            the content map.
      */
     public InternalContent(String path, Map<String, Object> content) {
         if (content == null) {
             content = ImmutableMap.of();
         }
         this.content = ImmutableMap.copyOf(content);
         this.updatedContent = Maps.newHashMap();
         this.path = path;
         updated = true;
         newcontent = true;
         readOnly = false;
     }
 
     /**
      * Convert a new content object to an internal version.
      *
      * @param contentManager
      *            the content manager now managing this content object.
      *
      * @param readOnly sets this contentManager to be either read-only or not.
      */
     void internalize(ContentManagerImpl contentManager, boolean readOnly) {
         this.contentManager = contentManager;
         updated = false;
         newcontent = false;
         this.readOnly = readOnly;
     }
 
     /**
      * Reset the object back to its last saved state.
      */
     public void reset(Map<String, Object> updatedMap) {
         if (!readOnly) {
             this.content = ImmutableMap.copyOf(updatedMap);
             updatedContent.clear();
             updated = false;
             LOGGER.debug("Reset to {} ", updatedMap);
         }
     }
 
     /**
      * @return true if the Content object is new and not connected to a content
      *         manager or structure.
      */
     public boolean isNew() {
         return newcontent;
     }
 
     /**
      * @return true if the content object has been updated since it was
      *         retrieved.
      */
     public boolean isUpdated() {
         if (readOnly) {
             return false;
         }
         return updated;
     }
 
     /**
      * @return get a read only copy of the properties. Property values are in
      *         store format and must be converted before use with
      *         StorageContentUtils.toString, toInteger, toLong etc
      */
     public Map<String, Object> getProperties() {
         LOGGER.debug("getting properties map {}", content);
         return StorageClientUtils.getFilterMap(content, updatedContent, null, null, false);
     }
 
     public Map<String, Object> getPropertiesForUpdate() {
         return StorageClientUtils.getFilterMap(content, updatedContent, null, null, true);
     }
 
     public Map<String, Object> getOriginalProperties() {
         return StorageClientUtils.getFilterMap(content, null, null, null, false);
     }
 
     /**
      * set a property, creating if it does not exist, overwriting if it does.
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the value for the property in storage format created with
      *            StorageContentUtils.toStore(). Must not be null.
      */
     public void setProperty(String key, Object value) {
         if (readOnly) {
             return;
         }
         if (value == null) {
             throw new IllegalArgumentException("value must not be null");
         }
         Object o = content.get(key);
         if (!value.equals(o)) {
             updatedContent.put(key, value);
             updated = true;
         } else if (updatedContent.containsKey(key) && !value.equals(updatedContent.get(key))) {
             updatedContent.put(key, value);
             updated = true;
         }
 
     }
 
     public void removeProperty(String name) {
         if (readOnly) {
             return;
         }
         setProperty(name, new RemoveProperty());
     }
 
     /**
      * @param key
      * @return the value of the property in storage format (use
      *         StorageContentUtils.toString etc to convert). null if the
      *         property does not exist. null can be ambiguous and
      *         hasProperty(String key) should be checked for an authoratative
      *         answer.
      */
     public Object getProperty(String key) {
         if (updatedContent.containsKey(key)) {
             Object o = updatedContent.get(key);
             if (o instanceof RemoveProperty) {
                 return null;
             }
             return o;
         }
         Object o = content.get(key);
         if (o instanceof RemoveProperty) {
             return null;
         }
         return o;
     }
 
     public String getId() {
         return (String) content.get(Content.getUuidField());
     }
 
     /**
      * @param key
      * @return true if the property exists.
      */
     public boolean hasProperty(String key) {
         if (updatedContent.containsKey(key)) {
             return !(updatedContent.get(key) instanceof RemoveProperty);
         }
         return content.containsKey(key) && !(content.get(key) instanceof RemoveProperty);
     }
 
     /**
      * @return get the path of the content object.
      */
     public String getPath() {
         return path;
     }
 
     /**
      * @return an iterable for all children of this content item.
      */
     public Iterable<Content> listChildren() {
         if (newcontent) {
             return Collections.<Content>emptyList();
         }
         return new Iterable<Content>() {
 
             public Iterator<Content> iterator() {
                 try {
                     return contentManager.listChildren(path);
                 } catch (StorageClientException e) {
                     LOGGER.error(e.getMessage(), e);
                 }
                 return Iterators.emptyIterator();
             }
         };
     }
 
     /**
      * @return an iterable of all relative child paths of this object.
      */
     public Iterable<String> listChildPaths() {
         if (newcontent) {
             return Collections.<String>emptyList();
         }
         return new Iterable<String>() {
 
             public Iterator<String> iterator() {
                 try {
                     return contentManager.listChildPaths(path);
                 } catch (StorageClientException e) {
                     LOGGER.error(e.getMessage(), e);
                 }
                 return Iterators.emptyIterator();
             }
         };
     }
 
     public Iterable<String> listStreams() {
         final Set<String> streams = Sets.newHashSet();
         for (Entry<String, Object> e : content.entrySet()) {
             String k = e.getKey();
             String[] streamIds = StringUtils.split(k, "/", 2);
             if (streamIds.length == 2) {
                 streams.add(streamIds[1]);
             }
         }
         return new Iterable<String>() {
             public Iterator<String> iterator() {
                 return streams.iterator();
             }
         };
     }
 
     @Override
     public String toString() {
         return "Path: " + getPath() + "; Properties: " + getProperties();
     }
 
     /**
      * @deprecated This method sets the ID field for the whole system. Do not
      *             use. Its been provided to make it possible to configure the
      *             ID field name used by Sparse to allow Berkley to continue
      *             running without migration. DO NOT USE, IT WILL HAVE NO
      *             EFFECT.
      * @param idFieldName
      */
     public static void setUuidField(String idFieldName) {
         if (!idFieldIsSet) {
             idFieldIsSet = true;
             LOGGER.warn("ID Field is being set to {}, this can only be done once per JVM start ",
                     idFieldName);
             UUID_FIELD = idFieldName;
         } else {
             LOGGER.warn("ID Field has already been set to {} and cannot be reset. ", idFieldName);
         }
     }
 
     /**
      * @deprecated this is a transitional measure that will be removed once
      *             Berkley have migrated. It allows them (and anyone else with
      *             content containing _sparseId) in their data to configure
      *             their system to use that field name. Eventually this method
      *             will be replaced throughout the code base with a static
      *             final.
      * @return
      */
     public static String getUuidField() {
         return UUID_FIELD;
     }
 
     /**
      * 
      * @return true if the content item is deleted, the system does not delete
      *         content items, it marks items as deleted. This will allow the
      *         storage layers to maintain an update pattern that is close to
      *         append only allowing compression of the storage to be
      *         achieved by background task which may then also remove holes in the storage.
      *         This is not dissimilar from the way most file systems work.
      */
     public boolean isDeleted() {
         return TRUE.equals(content.get(DELETED_FIELD));
     }
 
 }
