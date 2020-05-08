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
 package org.sakaiproject.nakamura.lite.storage.mem;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 import org.sakaiproject.nakamura.api.lite.RemoveProperty;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.api.lite.content.Content;
 import org.sakaiproject.nakamura.api.lite.util.PreemptiveIterator;
 import org.sakaiproject.nakamura.lite.content.BlockContentHelper;
 import org.sakaiproject.nakamura.lite.content.BlockSetContentHelper;
 import org.sakaiproject.nakamura.lite.content.InternalContent;
 import org.sakaiproject.nakamura.lite.storage.DisposableIterator;
 import org.sakaiproject.nakamura.lite.storage.StorageClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 public class MemoryStorageClient implements StorageClient {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(MemoryStorageClient.class);
     private static final Set<String> INDEX_COLUMNS = ImmutableSet.of(
             "au:rep:principalName",
             "au:type"
     );
 
     private static final Set<String> AUTO_INDEX_COLUMNS = ImmutableSet.of(
             "cn:_:parenthash",
             "au:_:parenthash",
             "ac:_:parenthash");
 
     Map<String, Object> store;
     private int blockSize;
     private int maxChunksPerBlockSet;
     private BlockContentHelper contentHelper;
     private MemoryStorageClientPool pool;
 
     public MemoryStorageClient(MemoryStorageClientPool pool,
             Map<String, Object> store, Map<String, Object> properties) {
         this.store = store;
         this.pool = pool;
         contentHelper = new BlockSetContentHelper(this);
         blockSize = StorageClientUtils.getSetting(
                 properties.get(BlockSetContentHelper.CONFIG_BLOCK_SIZE),
                 BlockSetContentHelper.DEFAULT_BLOCK_SIZE);
         maxChunksPerBlockSet = StorageClientUtils.getSetting(
                 properties.get(BlockSetContentHelper.CONFIG_MAX_CHUNKS_PER_BLOCK),
                 BlockSetContentHelper.DEFAULT_MAX_CHUNKS_PER_BLOCK);
 
     }
 
     public void close() {
         pool.releaseClient(this);
     }
 
     public void destroy() {
     }
 
     public Map<String, Object> get(String keySpace, String columnFamily, String key)
             throws StorageClientException {
         return (Map<String, Object>) getOrCreateRow(keySpace, columnFamily, key);
     }
 
     private Map<String, Object> getOrCreateRow(String keySpace, String columnFamily, String key) {
         String keyName = rowHash(keySpace, columnFamily, key);
 
         if (!store.containsKey(keyName)) {
             Map<String, Object> row = Maps.newConcurrentHashMap();
             store.put(keyName, row);
             LOGGER.debug("Created {}  as {} ", new Object[] { keyName, row });
             return row;
         }
         @SuppressWarnings("unchecked")
         Map<String, Object> row = (Map<String, Object>) store.get(keyName);
         LOGGER.debug("Got {} as {} ", new Object[] { keyName, row });
         return row;
     }
 
     public String rowHash(String keySpace, String columnFamily, String key) {
         return keySpace + ":" + columnFamily + ":" + key;
     }
 
     public String keyHash(String keySpace, String columnFamily, String columnKey, Object columnValue) {
         return "_"+keySpace + ":" + columnFamily + ":" + columnKey + ":"+columnValue;
     }
 
     public void insert(String keySpace, String columnFamily, String key, Map<String, Object> values, boolean probablyNew)
             throws StorageClientException {
         Map<String, Object> row = get(keySpace, columnFamily, key);
 
         for (Entry<String, Object> e : values.entrySet()) {
             Object value = e.getValue();
             if (value instanceof byte[]) {
                 byte[] bvalue = (byte[]) e.getValue();
                 byte[] nvalue = new byte[bvalue.length];
                 System.arraycopy(bvalue, 0, nvalue, 0, bvalue.length);
                 value = nvalue;
             }
             if (value == null || value instanceof RemoveProperty) {
                 Object previous = row.remove(e.getKey());
                 removeIndex(keySpace, columnFamily, key, e.getKey(), previous);
             } else {
                 Object previous = row.put(e.getKey(), value);
                 removeIndex(keySpace, columnFamily, key, e.getKey(), previous);
                 addIndex(keySpace, columnFamily, key, e.getKey(), e.getValue());
             }
         }
         LOGGER.debug("Updated {} {} ", key, row);
     }
 
 
     private void addIndex(String keySpace, String columnFamily, String key, String columnKey,
             Object value) {
         if ( INDEX_COLUMNS.contains(columnFamily+":"+columnKey)) {
             addIndexValue(keySpace, columnFamily, key, columnKey, value);
         }
         if ( !StorageClientUtils.isRoot(key) ) {
             addIndexValue(keySpace, columnFamily, key, InternalContent.PARENT_HASH_FIELD, (rowHash(keySpace, columnFamily,StorageClientUtils.getParentObjectPath(key))));
         }
     }
 
 
     private void addIndexValue(String keySpace, String columnFamily, String key, String columnKey,
             Object columnValue) {
         String indexKey = keyHash(keySpace,columnFamily, columnKey, columnValue);
         @SuppressWarnings("unchecked")
         Set<String> index = (Set<String>) store.get(indexKey);
         if ( index == null ) {
             index = Sets.newConcurrentHashSet();
             store.put(indexKey, index);
         }
         index.add(rowHash(keySpace,columnFamily, key));
     }
 
     private void removeIndex(String keySpace, String columnFamily, String key, String columnKey, Object columnValue) {
         @SuppressWarnings("unchecked")
         Set<String> index = (Set<String>) store.get(keyHash(keySpace,columnFamily, columnKey, columnValue));
         if ( index != null ) {
             index.remove(rowHash(keySpace, columnFamily, key));
         }
     }
 
     public void remove(String keySpace, String columnFamily, String key)
             throws StorageClientException {
         String keyName = rowHash(keySpace, columnFamily, key);
         if (store.containsKey(keyName)) {
             @SuppressWarnings("unchecked")
             Map<String, Object> previous = (Map<String, Object>) store.remove(keyName);
             for( Entry<String, Object> e : previous.entrySet() ) {
                 removeIndex(keySpace, columnFamily, key, e.getKey(), e.getValue());
             }
         }
     }
 
     public Map<String, Object> streamBodyIn(String keySpace, String contentColumnFamily,
             String contentId, String contentBlockId, String streamId, Map<String, Object> content, InputStream in)
             throws StorageClientException, AccessDeniedException, IOException {
         return contentHelper.writeBody(keySpace, contentColumnFamily, contentId, contentBlockId, streamId,
                 blockSize, maxChunksPerBlockSet, in);
     }
 
     public InputStream streamBodyOut(String keySpace, String contentColumnFamily, String contentId,
             String contentBlockId, String streamId, Map<String, Object> content) throws StorageClientException,
             AccessDeniedException {
 
         int nBlocks = toInt(content.get(Content.NBLOCKS_FIELD));
         return contentHelper.readBody(keySpace, contentColumnFamily, contentBlockId, streamId, nBlocks);
     }
 
     private int toInt(Object object) {
         if ( object instanceof Integer) {
             return ((Integer) object).intValue();
         }
         return 0;
     }
 
     public DisposableIterator<Map<String, Object>> find(String keySpace,
             String columnFamily, Map<String, Object> properties) {
         List<Set<String>> matchingSets = Lists.newArrayList();
         for (Entry<String, Object> e : properties.entrySet()) {
             Object v = e.getValue();
             String k = e.getKey();
             if ( shouldIndex(keySpace, columnFamily, k) ) {
                 if (v != null) {
                     @SuppressWarnings("unchecked")
                     Set<String> matches = (Set<String>) store.get(keyHash(keySpace, columnFamily, e.getKey(), e.getValue()));
                     LOGGER.debug("Searching for {} found {} ",keyHash(keySpace, columnFamily, e.getKey(), e.getValue()), matches);
                     if ( matches != null) {
                         matchingSets.add(matches);
                     }
                 }
             } else {
                 LOGGER.warn("Search on {}:{} is not supported, filter dropped ",columnFamily,k);
             }
         }
 
         // find the union of all matching sets, using set views to build a tree of sets. This will be lazy iterating.
         Set<String> setOfRowHashes = null;
         for ( Set<String> m : matchingSets) {
             if ( setOfRowHashes == null ) {
                 setOfRowHashes = m;
             } else {
                 setOfRowHashes = Sets.intersection(setOfRowHashes, m);
             }
         }
         LOGGER.debug("Matching Rowhashes is {} ", setOfRowHashes);
        final Iterator<String> matchedRowIds = setOfRowHashes.iterator();
          return new PreemptiveIterator<Map<String,Object>>() {
 
             private Map<String, Object> nextMap;
 
             @SuppressWarnings("unchecked")
             @Override
             protected boolean internalHasNext() {
                 while(matchedRowIds.hasNext()) {
                    nextMap = (Map<String, Object>) store.get(matchedRowIds.next());
                    if ( nextMap != null ) {
                        return true;
                    }
                 }
                 nextMap = null;
                 return false;
             }
 
             @Override
             protected Map<String, Object> internalNext() {
                 return nextMap;
             }
         };
     }
 
     private boolean shouldIndex(String keySpace, String columnFamily, String k) {
         if ( AUTO_INDEX_COLUMNS.contains(columnFamily+":"+k) || INDEX_COLUMNS.contains(columnFamily+":"+k) ) {
             return true;
         }
         return false;
     }
 
     public DisposableIterator<Map<String, Object>> listChildren(String keySpace,
             String columnFamily, String key) throws StorageClientException {
         String hash = rowHash(keySpace, columnFamily, key);
         LOGGER.debug("Finding {}:{}:{} as {} ",new Object[]{keySpace,columnFamily, key, hash});
         return find(keySpace, columnFamily, ImmutableMap.of(InternalContent.PARENT_HASH_FIELD, (Object)hash));
     }
 
 
 }
