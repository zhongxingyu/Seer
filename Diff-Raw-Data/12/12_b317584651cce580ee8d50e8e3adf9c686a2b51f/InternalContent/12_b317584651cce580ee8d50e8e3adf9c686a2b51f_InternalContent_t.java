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
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Maps;
 
 import org.sakaiproject.nakamura.api.lite.RemoveProperty;
 import org.sakaiproject.nakamura.api.lite.Repository;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.api.lite.content.Content;
 import org.sakaiproject.nakamura.api.lite.util.PreemptiveIterator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * Internal Content Object for holding sparse Content objects. Has a protected
  * constructor for internal use.
  */
 public class InternalContent {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(Content.class);
    
    
    public static final String INTERNAL_FIELD_PREFIX = Repository.SYSTEM_PROP_PREFIX+":";
     /**
      * The ID of a content item
      */
     public static final String UUID_FIELD = Repository.SYSTEM_PROP_PREFIX + "id";
     /**
      * The path of the content item (used in structure row)
      */
     public static final String PATH_FIELD = Repository.SYSTEM_PROP_PREFIX + "path";
     /**
      * content item ID referenced by a Structure item
      */
    public static final String STRUCTURE_UUID_FIELD = INTERNAL_FIELD_PREFIX + "cid";
     /**
      * Where a structure object is a link, this field contains the location of the target of the link
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
     public static final String PREVIOUS_VERSION_UUID_FIELD = Repository.SYSTEM_PROP_PREFIX + "previousVersion";
     /**
      * Previous Block ID. (content row)
      */
     public static final String PREVIOUS_BLOCKID_FIELD = Repository.SYSTEM_PROP_PREFIX + "previousBlockId";
     /**
      * The ID of the next version (content row)
      */
     public static final String NEXT_VERSION_FIELD = Repository.SYSTEM_PROP_PREFIX + "nextVersion";
     /**
      * Set to "Y" if the content item is read only. (content row)
      */
     public static final String READONLY_FIELD = "readOnly";
     /**
      * set to "Y" if deleted. (content row)
      */
     public static final String DELETED_FIELD = "deleted";
 
     /**
      * The block size in bytes in each block in a block set, if body store uses
      * blocking (content row)
      */
     public static final String BLOCKSIZE_FIELD = Repository.SYSTEM_PROP_PREFIX + "blocksize";
     /**
      * Total length of the content body (content row)
      */
     public static final String LENGTH_FIELD = "length";
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
     public static final String BODY_LAST_MODIFIED_FIELD = Repository.SYSTEM_PROP_PREFIX + "bodyLastModified";
 
     /**
      * The user ID that last modified the body. (content row)
      */
     public static final String BODY_LAST_MODIFIED_BY_FIELD = Repository.SYSTEM_PROP_PREFIX + "bodyLastModifiedBy";
 
     /**
      * The date the body was created (GMT epoch long) (content row)
      */
     public static final String BODY_CREATED_FIELD = Repository.SYSTEM_PROP_PREFIX + "bodyCreated";
 
     /**
      * The user that created the body. (content row)
      */
     public static final String BODY_CREATED_BY_FIELD = Repository.SYSTEM_PROP_PREFIX + "bodyCreatedBy";
 
     /**
      * The time the item was created. (content row)
      */
     public static final String CREATED_FIELD = "created";
 
     /**
      * The user that created the item. (content row)
      */
     public static final String CREATED_BY_FIELD = "createdBy";
 
     /**
      * The time the item was last modified. (content row)
      */
     public static final String LASTMODIFIED_FIELD = "lastModified";
 
     /**
      * The user that lastModified the item. (content row)
      */
     public static final String LASTMODIFIED_BY_FIELD = "lastModifiedBy";
 
     /**
      * The path the content object was copied from if it was copied
      */
     public static final String COPIED_FROM_PATH_FIELD = Repository.SYSTEM_PROP_PREFIX + "copiedFrom";
 
     /**
      * The ID the content object was copied from.
      */
     public static final String COPIED_FROM_ID_FIELD = Repository.SYSTEM_PROP_PREFIX + "copiedFromId";
 
     /**
      * If the copy was deep, then true
      */
     public static final String COPIED_DEEP_FIELD = Repository.SYSTEM_PROP_PREFIX + "copiedDeep";
 
     /**
      * Mime type
      */
     public static final String MIMETYPE_FIELD = "mimeType";
 
     /**
      * Charset encoding if char based.
      */
     public static final String ENCODING_FIELD = "encoding";
     
     /**
      * 
      */
     public static final String VERSION_HISTORY_ID_FIELD = Repository.SYSTEM_PROP_PREFIX + "versionHistoryId";
 
     /**
      * 
      */
     public static final String VERSION_NUMBER_FIELD = Repository.SYSTEM_PROP_PREFIX + "versionNumber";
     
     /**
      * The who this version was saved by
      */
     public static final String VERSION_SAVEDBY_FIELD = Repository.SYSTEM_PROP_PREFIX + "versionSavedBy";
 
 
 
 
     /**
      * Map of the structure object for the content object.
      */
     private ImmutableMap<String, Object> structure;
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
      * @param structure
      *            the structure object
      * @param contentManager
      *            the content manager now managing this content object.
      */
     void internalize(Map<String, Object> structure, ContentManagerImpl contentManager, boolean readOnly) {
         this.structure = ImmutableMap.copyOf(structure);
         this.contentManager = contentManager;
         updated = false;
         newcontent = false;
         this.readOnly = readOnly;
     }
 
 
     /**
      * Reset the object back to its last saved state.
      */
     public void reset(Map<String, Object> updatedMap) {
         if ( !readOnly ) {
             this.content = ImmutableMap.copyOf(updatedMap);
             updatedContent.clear();
             updated = false;
             newcontent = false;
             LOGGER.debug("Reset to {} ",updatedMap);
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
         if ( readOnly ) {
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
         return StorageClientUtils.getFilterMap(content, updatedContent, null, null);
     }
 
     public Map<String, Object> getPropertiesForUpdate() {
         return StorageClientUtils.getFilterMap(content, updatedContent, null,
                 null);
     }
 
     /**
      * set a property, creating if it does not exist, overwriting if it does.
      * 
      * @param key
      *            the key for the property
      * @param value
      *            the value for the property in storage format created with
      *            StorageContentUtils.toStore()
      */
     public void setProperty(String key, Object value) {
         if ( readOnly) {
             return;
         }
         Object o = content.get(key);
         if (!value.equals(o) ) {
             updatedContent.put(key, value);
             updated = true;
         } else if ( updatedContent.containsKey(key) && !value.equals(updatedContent.get(key)) ) {
             updatedContent.put(key, value);
             updated = true;            
         }
 
     }
     
     public void removeProperty(String name) {
         if ( readOnly) {
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
     // TODO: Unit test
     public Object getProperty(String key) {
         if ( updatedContent.containsKey(key)) {
             Object o = updatedContent.get(key);
             if ( o instanceof RemoveProperty ) {
                 return null;
             }
             return o;
         }
         Object o =  content.get(key);
         if ( o instanceof RemoveProperty ) {
             return null;
         }
         return o;
     }
 
     /**
      * @param key
      * @return true if the property exists.
      */
     public boolean hasProperty(String key) {
         if ( updatedContent.containsKey(key)) {
             return !( updatedContent.get(key) instanceof RemoveProperty);
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
         return new Iterable<Content>() {
 
             public Iterator<Content> iterator() {
                 final Iterator<String> childIterator = listChildPaths().iterator();
                 return new PreemptiveIterator<Content>() {
                     Content childContent;
 
                     protected boolean internalHasNext() {
                         childContent = null;
                         try {
                             while (childContent == null && childIterator.hasNext()) {
                                 String child = childIterator.next();
                                 try {
                                     childContent = contentManager.get(StorageClientUtils.newPath(
                                             path, child));
                                 } catch (AccessDeniedException e) {
                                     LOGGER.debug("Unable to load {} cause {}", new Object[] {
                                             child, e.getMessage() }, e);
                                 }
 
                             }
                         } catch (StorageClientException e) {
                             LOGGER.debug("Unable to load Children cause {}", e.getMessage(), e);
 
                         }
                         return (childContent != null);
                     }
 
                     protected Content internalNext() {
                         return childContent;
                     }
                 };
             }
         };
     }
 
     /**
      * @return an iterable of all relative child paths of this object.
      */
     public Iterable<String> listChildPaths() {
         return new Iterable<String>() {
             public Iterator<String> iterator() {
                 return Iterators.filter(structure.keySet().iterator(), new Predicate<String>() {
                     public boolean apply(String input) {
                        return !input.startsWith(INTERNAL_FIELD_PREFIX);
                     }
                 });
             }
         };
     }
 
 }
