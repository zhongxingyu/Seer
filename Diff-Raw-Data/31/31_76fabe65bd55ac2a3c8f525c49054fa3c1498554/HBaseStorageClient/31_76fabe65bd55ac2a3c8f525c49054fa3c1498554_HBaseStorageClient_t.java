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
 package org.sakaiproject.nakamura.lite.storage.hbase;
 
 import org.apache.hadoop.hbase.client.Delete;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HTableInterface;
 import org.apache.hadoop.hbase.client.HTablePool;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 import org.sakaiproject.nakamura.lite.ConfigurationImpl;
 import org.sakaiproject.nakamura.lite.content.BlockContentHelper;
 import org.sakaiproject.nakamura.lite.content.BlockSetContentHelper;
 import org.sakaiproject.nakamura.lite.storage.Disposable;
 import org.sakaiproject.nakamura.lite.storage.DisposableIterator;
 import org.sakaiproject.nakamura.lite.storage.Disposer;
 import org.sakaiproject.nakamura.lite.storage.SparseRow;
 import org.sakaiproject.nakamura.lite.storage.StorageClient;
 import org.sakaiproject.nakamura.lite.storage.StorageClientListener;
 import org.sakaiproject.nakamura.lite.types.Types;
 import org.sakaiproject.nakamura.api.lite.RemoveProperty;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.api.lite.content.Content;
 import org.sakaiproject.nakamura.api.lite.util.PreemptiveIterator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.NavigableMap;
 import java.util.Set;
 
 public class HBaseStorageClient implements StorageClient {
 
   private static final Logger LOGGER = LoggerFactory.getLogger(HBaseStorageClient.class);
   public static final String CONFIG_BLOCK_SIZE = "block-size";
   public static final String CONFIG_MAX_CHUNKS_PER_BLOCK = "chunks-per-block";
 
   private static final int DEFAULT_BLOCK_SIZE = 1024 * 1024;
   private static final int DEFAULT_MAX_CHUNKS_PER_BLOCK = 64;
   private static final String INDEX_COLUMN_FAMILY = "smcindex";
   private BlockContentHelper contentHelper;
   private int blockSize;
   private int maxChunksPerBlockSet;
   private HBaseStorageClientPool pool;
   HTablePool htab;
   public List<Map<String, Object>> tResultRows;
   private boolean active;
   private List<Disposable> toDispose = Lists.newArrayList();
 
   public HBaseStorageClient(HBaseStorageClientPool pool, Map<String, Object> properties,
       HTablePool htab) {
     this.pool = pool;
     this.htab = htab;
     contentHelper = new BlockSetContentHelper(this);
     blockSize = StorageClientUtils.getSetting(properties.get(CONFIG_BLOCK_SIZE),
         DEFAULT_BLOCK_SIZE);
     maxChunksPerBlockSet = StorageClientUtils.getSetting(
         properties.get(CONFIG_MAX_CHUNKS_PER_BLOCK), DEFAULT_MAX_CHUNKS_PER_BLOCK);
   }
 
   public void insert(String keySpace, String columnFamily, String key,
       Map<String, Object> values, boolean probablyNew) throws StorageClientException {
     HTableInterface table = null;
     try {
       table = htab.getTable(columnFamily);
       Put row = new Put(key.getBytes("UTF-8"), System.currentTimeMillis());
 
       for (Entry<String, Object> value : values.entrySet()) {
         String q = value.getKey();
         byte[] qualifier = null;
         qualifier = q.getBytes("UTF-8");
         Object v = value.getValue();
         byte qualifierValue[] = Types.toByteArray(v);
 
         if (v instanceof RemoveProperty) {
           Delete delRow = new Delete(key.getBytes("UTF-8"));
           delRow.deleteColumns(columnFamily.getBytes("UTF-8"), qualifier);
           table.delete(delRow);
         } else {
           row.add(columnFamily.getBytes("UTF-8"), qualifier, System.currentTimeMillis(),
               qualifierValue);
           table.put(row);
 
           if ((!columnFamily.equals(INDEX_COLUMN_FAMILY))
               && shouldIndex(keySpace, columnFamily, q)) {
             addIndex(keySpace, columnFamily, key, qualifier, qualifierValue);
           }
         }
       }
     } catch (UnsupportedEncodingException e1) {
       LOGGER.debug(e1.getMessage());
     } catch (IOException e) {
       LOGGER.debug("IOException. Stack trace:", e.getStackTrace());
     } finally {
       if (htab != null) {
         htab.putTable(table);
       }
     }
 
   }
 
   public Map<String, Object> get(String keySpace, String columnFamily, String key)
       throws StorageClientException {
     NavigableMap<byte[], byte[]> row;
     HTableInterface table = null;
     Map<String, Object> resultRow = new HashMap<String, Object>();
     try {
       table = htab.getTable(columnFamily);
 
       Get getRow = new Get(key.getBytes("UTF-8"));
       Result rowResult = table.get(getRow);
       row = rowResult.getFamilyMap(columnFamily.getBytes("UTF-8"));
 
       for (Entry<byte[], byte[]> value : row.entrySet()) {
         String valueKey = new String(value.getKey());
         Object valueObject = Types.toObject(value.getValue());
 
         resultRow.put(valueKey, valueObject);
       }
 
     } catch (UnsupportedEncodingException e1) {
       LOGGER.debug(e1.getMessage());
     } catch (IOException e1) {
       LOGGER.debug(e1.getMessage());
     } catch (Exception e) {
       LOGGER.debug(e.getMessage());
     } finally {
       if (htab != null) {
         htab.putTable(table);
       }
     }
     return resultRow;
   }
 
   public void remove(String keySpace, String columnFamily, String key)
       throws StorageClientException {
     HTableInterface indexTable = null;
     HTableInterface table = null;
     if (!columnFamily.equals(INDEX_COLUMN_FAMILY)) {
       Map<String, Object> row = get(keySpace, columnFamily, key);
 
       try {
         indexTable = htab.getTable(INDEX_COLUMN_FAMILY);
         for (Entry<String, Object> value : row.entrySet()) {
           String qualifierName = value.getKey();
           String qualifierValue = null;
           qualifierValue = new String(Types.toByteArray(value.getValue()));
           qualifierValue = StorageClientUtils.insecureHash(qualifierValue);
           String indexKey = qualifierName + ":" + INDEX_COLUMN_FAMILY + ":"
               + StorageClientUtils.insecureHash(qualifierValue);
           Delete delIndexKey = new Delete(indexKey.getBytes("UTF-8"));
           delIndexKey.deleteColumns(columnFamily.getBytes("UTF-8"),
               qualifierName.getBytes("UTF-8"));
           indexTable.delete(delIndexKey);
         }
       } catch (IOException e) {
         LOGGER.debug("IOException. Stack trace:",e);
       }
     }
 
     try {
       table = htab.getTable(columnFamily);
       Delete delRow = new Delete(key.getBytes("UTF-8"));
       delRow.deleteFamily(columnFamily.getBytes("UTF-8"));
       table.delete(delRow);
     } catch (UnsupportedEncodingException e1) {
       LOGGER.debug(e1.getMessage());
     } catch (IOException e) {
       LOGGER.debug("IOException. Stack trace:", e.getStackTrace());
     } finally {
       if (htab != null) {
         htab.putTable(table);
         htab.putTable(indexTable);
       }
     }
   }
 
   private boolean shouldIndex(String keySpace, String columnFamily, String columnName)
       throws StorageClientException {
 
     String properties[] = new ConfigurationImpl().getIndexColumnNames();
    Set<String> indexColumns = ImmutableSet.copyOf(properties);
 
     if (indexColumns.contains(columnFamily + ":" + columnName)) {
       LOGGER.debug("Should Index {}:{}", columnFamily, columnName);
       return true;
     } else {
       LOGGER.debug("Should Not Index {}:{}", columnFamily, columnName);
       return false;
     }
 
   }
 
   private void addIndex(String keySpace, String columnFamily, String key, byte[] bname,
       byte[] b) throws StorageClientException {
     String indexKey = new String(bname) + ":" + columnFamily + ":"
         + StorageClientUtils.insecureHash(new String(b));
     Map<String, Object> values = new HashMap<String, Object>();
     values.put(key, (Object) "Value of index yet to be decided");
     insert(keySpace, INDEX_COLUMN_FAMILY, indexKey, values, true);
   }
 
   public boolean hasBody(Map<String, Object> content, String streamId) {
     return contentHelper.hasBody(content, streamId);
   }
 
   public Map<String, Object> streamBodyIn(String keySpace, String contentColumnFamily,
       String contentId, String contentBlockId, String streamId,
       Map<String, Object> content, InputStream in) throws StorageClientException,
       AccessDeniedException, IOException {
     return contentHelper.writeBody(keySpace, contentColumnFamily, contentId,
         contentBlockId, streamId, blockSize, maxChunksPerBlockSet, in);
   }
 
   public void close() {
     pool.releaseClient(this);
   }
 
   public InputStream streamBodyOut(String keySpace, String contentColumnFamily,
       String contentId, String contentBlockId, String streamId,
       Map<String, Object> content) throws StorageClientException, AccessDeniedException,
       IOException {
     int nBlocks = StorageClientUtils.toInt(content.get(Content.NBLOCKS_FIELD));
     return contentHelper.readBody(keySpace, contentColumnFamily, contentBlockId,
         streamId, nBlocks);
   }
 
   public DisposableIterator<Map<String, Object>> find(String keySpace,
       String authorizableColumnFamily, Map<String, Object> properties)
       throws StorageClientException {
     final String fKeyspace = keySpace;
     final String fAuthorizableColumnFamily = authorizableColumnFamily;
     List<Set<String>> andTerms = new ArrayList<Set<String>>();
 
     for (Entry<String, Object> e : properties.entrySet()) {
       String k = e.getKey();
       Object v = e.getValue();
 
       if (shouldIndex(keySpace, authorizableColumnFamily, k) || (v instanceof Map)) {
         if (v != null) {
           if (v instanceof Map) {
             List<Set<String>> orTerms = new ArrayList<Set<String>>();
             Set<String> orResultSet = new HashSet<String>();
 
             @SuppressWarnings("unchecked")
             Set<Entry<String, Object>> subterms = ((Map<String, Object>) v).entrySet();
 
             for (Iterator<Entry<String, Object>> subtermsIter = subterms.iterator(); subtermsIter
                 .hasNext();) {
               Entry<String, Object> subterm = subtermsIter.next();
               String subk = subterm.getKey();
               Object subv = subterm.getValue();
               if (shouldIndex(keySpace, authorizableColumnFamily, subk)) {
                 try {
                     Set<String> or = new HashSet<String>();
                   String indexKey = new String(subk.getBytes("UTF-8"))
                       + ":"
                       + authorizableColumnFamily
                       + ":"
                       + StorageClientUtils.insecureHash(new String(Types
                           .toByteArray(subv)));
                   Map<String, Object> tempRow = get(keySpace, INDEX_COLUMN_FAMILY, indexKey);
                   for (Entry<String, Object> tempRows : tempRow.entrySet()) {
                     or.add(tempRows.getKey());
                   }
                   orTerms.add(or);
                 } catch (IOException e1) {
                   LOGGER.warn("IOException {}", e1.getMessage());
                 }
               }
             }
 
             if (!orTerms.isEmpty())
               orResultSet = orTerms.get(0);
 
             for (int i = 0; i < orTerms.size(); i++) {
               orResultSet = Sets.union(orResultSet, orTerms.get(i));
 
             }
             andTerms.add(orResultSet);
           } else {
             try {
                   Set<String> and = new HashSet<String>();
                   String indexKey = new String(k.getBytes("UTF-8")) + ":" + authorizableColumnFamily
                       + ":"
                       + StorageClientUtils.insecureHash(new String(Types.toByteArray(v)));
                   Map<String, Object> tempRow = get(keySpace, INDEX_COLUMN_FAMILY, indexKey);
                   for (Entry<String, Object> tempRows : tempRow.entrySet()) {
                     and.add(tempRows.getKey());
                   }
                   andTerms.add(and);
             } catch (IOException e1) {
               LOGGER.warn("IOException {}", e1.getMessage());
             }
           }
         }
       }
     }
 
     Set<String> andResultSet = new HashSet<String>();
 
     if (!andTerms.isEmpty())
       andResultSet = andTerms.get(0);
 
     for (int i = 0; i < andTerms.size(); i++) {
       andResultSet = Sets.intersection(andResultSet, andTerms.get(i));
     }
 
     List<Map<String, Object>> resultRows = new ArrayList<Map<String, Object>>();
 
     Iterator<String> iterator = andResultSet.iterator();
 
     while (iterator.hasNext()) {
       Map<String, Object> row = get(keySpace, authorizableColumnFamily, iterator.next());
       resultRows.add(row);
     }
 
     tResultRows = resultRows;
     final Iterator<String> fIterator = andResultSet.iterator();
 
     if (tResultRows.isEmpty()) {
       return new DisposableIterator<Map<String, Object>>() {
         private Disposer disposer;
         public boolean hasNext() {
           return false;
         }
 
         public Map<String, Object> next() {
           return null;
         }
 
         public void remove() {
         }
 
         public void close() {
           if (disposer != null) {
             disposer.unregisterDisposable(this);
           }
         }
 
         public void setDisposer(Disposer disposer) {
           this.disposer = disposer;
           
         }
       };
     }
     return registerDisposable(new PreemptiveIterator<Map<String, Object>>() {
 
       private Map<String, Object> nextValue = Maps.newHashMap();
       private boolean open = true;
 
       @Override
       protected Map<String, Object> internalNext() {
         return nextValue;
       }
 
       @Override
       protected boolean internalHasNext() {
         if (fIterator.hasNext()) {
           try {
             String id = fIterator.next();
             nextValue = get(fKeyspace, fAuthorizableColumnFamily, id);
             LOGGER.debug("Got Row ID {} {} ", id, nextValue);
             return true;
           } catch (StorageClientException e) {
 
           }
         }
         close();
         nextValue = null;
         LOGGER.debug("End of Set ");
         return false;
       }
 
       @Override
       public void close() {
         if (open) {
           open = false;
         }
 
       }
     });
   }
 
   public DisposableIterator<Map<String, Object>> listChildren(String keySpace,
       String columnFamily, String key) throws StorageClientException {
     throw new UnsupportedOperationException();
   }
 
   public void passivate() {
   }
 
   public void activate() {
   }
 
   public void validate() {
   }
 
   public void destroy() {
     try {
       ((HTableInterface) htab).close();
     } catch (IOException e) {
       LOGGER.debug(e.getMessage());
     }
   }
 
   private <T extends Disposable> T registerDisposable(T disposable) {
     toDispose.add(disposable);
     return disposable;
   }
 
   public void shutdownConnection() {
     if (active) {
       disposeDisposables();
       active = false;
     }
   }
 
   private void disposeDisposables() {
     for (Disposable d : toDispose) {
       d.close();
     }
   }
 
 public DisposableIterator<SparseRow> listAll(String keySpace, String columnFamily) {
     // TODO Auto-generated method stub
     return null;
 }
 
 public long allCount(String keySpace, String columnFamily) {
     // TODO Auto-generated method stub
     return 0;
 }
 
 public void setStorageClientListener(StorageClientListener storageClientListener) {
     // TODO Auto-generated method stub
     
 }
 
 }
