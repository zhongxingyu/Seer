 package com.nearinfinity.hbaseclient;
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Maps;
 import com.nearinfinity.hbaseclient.strategy.PrefixScanStrategy;
 import com.nearinfinity.hbaseclient.strategy.ScanStrategy;
 import com.nearinfinity.hbaseclient.strategy.ScanStrategyInfo;
 import com.nearinfinity.mysqlengine.jni.Blob;
 import com.nearinfinity.mysqlengine.scanner.HBaseResultScanner;
 import com.nearinfinity.mysqlengine.scanner.SingleResultScanner;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.*;
 import org.apache.hadoop.hbase.client.*;
 import org.apache.hadoop.hbase.filter.PrefixFilter;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 import static java.lang.String.format;
 
 public class HBaseClient {
     private HTable table;
 
     private HBaseAdmin admin;
 
     private final static ConcurrentHashMap<String, TableInfo> tableCache = new ConcurrentHashMap<String, TableInfo>();
 
     private static final Logger logger = Logger.getLogger(HBaseClient.class);
 
     public HBaseClient(String tableName, String zkQuorum) throws IOException {
         logger.info("HBaseClient: Constructing with HBase table name: " + tableName);
         logger.info("HBaseClient: Constructing with ZK Quorum: " + zkQuorum);
 
         Configuration configuration = HBaseConfiguration.create();
         configuration.set("hbase.zookeeper.quorum", zkQuorum);
 
         this.admin = new HBaseAdmin(configuration);
         this.initializeSqlTable();
         logger.info("Sql table successfully initialized.");
 
         this.table = new HTable(configuration, tableName);
         logger.info("HTable successfully created.");
     }
 
     private void initializeSqlTable() throws IOException {
         HTableDescriptor sqlTableDescriptor;
         HColumnDescriptor nicColumn = new HColumnDescriptor(Constants.NIC);
 
         if (!this.admin.tableExists(Constants.SQL)) {
             logger.info("Creating sql table");
             sqlTableDescriptor = new HTableDescriptor(Constants.SQL);
             sqlTableDescriptor.addFamily(nicColumn);
 
             this.admin.createTable(sqlTableDescriptor);
         }
 
         sqlTableDescriptor = this.admin.getTableDescriptor(Constants.SQL);
         if (!sqlTableDescriptor.hasFamily(Constants.NIC)) {
             logger.info("Adding nic column family to sql table");
 
             if (!this.admin.isTableDisabled(Constants.SQL)) {
                 logger.info("Disabling sql table");
                 this.admin.disableTable(Constants.SQL);
             }
 
             this.admin.addColumn(Constants.SQL, nicColumn);
         }
 
         if (this.admin.isTableDisabled(Constants.SQL)) {
             logger.info("Enabling sql table");
             this.admin.enableTable(Constants.SQL);
         }
 
         try {
             this.admin.flush(Constants.SQL);
         } catch (InterruptedException e) {
             logger.warn("HBaseAdmin flush was interrupted. Retrying.");
             try {
                 this.admin.flush(Constants.SQL);
             } catch (InterruptedException e1) {
                 throw new RuntimeException(e1);
             }
         }
     }
 
     private void createTable(String tableName, List<Put> puts, TableMultipartKeys multipartKeys) throws IOException {
         long tableId = table.incrementColumnValue(RowKeyFactory.ROOT, Constants.NIC, new byte[0], 1);
         tableCache.put(tableName, new TableInfo(tableName, tableId));
 
         puts.add(new Put(RowKeyFactory.ROOT).add(Constants.NIC, tableName.getBytes(), Bytes.toBytes(tableId)));
         Put put = new Put(RowKeyFactory.buildTableInfoKey(tableId));
         put.add(Constants.NIC, Constants.ROW_COUNT, Bytes.toBytes(0l));
         final byte[] bytes = multipartKeys.toJson();
         updateTableCacheIndex(tableName, bytes);
         put.add(Constants.NIC, Constants.INDEXES, bytes);
         puts.add(put);
     }
 
     private void updateTableCacheIndex(String tableName, final byte[] bytes) {
         tableCache.get(tableName).setTableMetadata(new HashMap<String, byte[]>() {{
             put(Constants.INDEXES_STRING, bytes);
         }});
     }
 
     private void addColumns(String tableName, Map<String, ColumnMetadata> columns, List<Put> puts) throws IOException {
         //Get table id from cache
         long tableId = tableCache.get(tableName).getId();
 
         //Build the column row key
         byte[] columnBytes = ByteBuffer.allocate(9).put(RowType.COLUMNS.getValue()).putLong(tableId).array();
 
         //Allocate ids and compute start id
         long numColumns = columns.size();
         long lastColumnId = table.incrementColumnValue(columnBytes, Constants.NIC, new byte[0], numColumns);
         long startColumn = lastColumnId - numColumns;
 
         for (String columnName : columns.keySet()) {
             long columnId = ++startColumn;
 
             //Add column
             Put columnPut = new Put(columnBytes).add(Constants.NIC, columnName.getBytes(), Bytes.toBytes(columnId));
             puts.add(columnPut);
 
             // Add column metadata
             byte[] columnInfoBytes = RowKeyFactory.buildColumnInfoKey(tableId, columnId);
             Put columnInfoPut = new Put(columnInfoBytes);
 
             ColumnMetadata metadata = columns.get(columnName);
 
             columnInfoPut.add(Constants.NIC, Constants.METADATA, metadata.toJson());
             if (metadata.isAutoincrement()) {
                 columnInfoPut.add(Constants.NIC, new byte[0], Bytes.toBytes(0L));
             }
 
             puts.add(columnInfoPut);
 
             //Add to cache
             tableCache.get(tableName).addColumn(columnName, columnId, columns.get(columnName));
         }
     }
 
     public void createTableFull(String tableName, Map<String, ColumnMetadata> columns, TableMultipartKeys multipartKeys) throws IOException {
         //Batch put list
         List<Put> putList = new LinkedList<Put>();
 
         createTable(tableName, putList, multipartKeys);
 
         addColumns(tableName, columns, putList);
 
         this.table.put(putList);
 
         this.table.flushCommits();
     }
 
     public void writeRow(String tableName, Map<String, byte[]> values, List<Blob> blobs) throws IOException {
         TableInfo info = getTableInfo(tableName);
         List<List<String>> multipartIndex = Index.indexForTable(info.tableMetadata());
         List<Put> putList = PutListFactory.createDataInsertPutList(values, info, multipartIndex);
         Put dataRow = putList.get(0);
         for (Blob blob : blobs) {
             writeBlob(tableName, blob.getColumnName(), blob.getData(), dataRow);
         }
 
         this.table.put(putList);
     }
 
     public Result getDataRow(UUID uuid, String tableName) throws IOException {
         TableInfo info = getTableInfo(tableName);
         long tableId = info.getId();
 
         byte[] rowKey = RowKeyFactory.buildDataKey(tableId, uuid);
 
         Get get = new Get(rowKey);
         return table.get(get);
     }
 
     public TableInfo getTableInfo(String tableName) throws IOException {
         if (tableCache.containsKey(tableName)) {
             return tableCache.get(tableName);
         }
 
         if (table == null) {
             throw new IllegalStateException(format("Table %s was null. Cannot get table information from null table.", tableName));
         }
 
         //Get the table id from HBase
         Get tableIdGet = new Get(RowKeyFactory.ROOT);
         Result result = table.get(tableIdGet);
         if (result.isEmpty()) {
             throw new TableNotFoundException("SQL table " + tableName + " was not found.");
         }
 
         byte[] sqlTableBytes = result.getValue(Constants.NIC, tableName.getBytes());
         if (sqlTableBytes == null) {
             throw new TableNotFoundException("SQL table " + tableName + " was not found.");
         }
 
         long tableId = ByteBuffer.wrap(sqlTableBytes).getLong();
 
         TableInfo info = new TableInfo(tableName, tableId);
 
         byte[] rowKey = RowKeyFactory.buildColumnsKey(tableId);
 
         Get columnsGet = new Get(rowKey);
         Result columnsResult = table.get(columnsGet);
 
         if (columnsResult.isEmpty()) {
             throw new IllegalStateException("Column result from the get was empty for row key " + Bytes.toStringBinary(rowKey));
         }
         Map<byte[], byte[]> columns = columnsResult.getFamilyMap(Constants.NIC);
         if (columns == null) {
             throw new NullPointerException("columns was null after getting family map.");
         }
 
         for (byte[] qualifier : columns.keySet()) {
             String columnName = new String(qualifier);
             long columnId = ByteBuffer.wrap(columns.get(qualifier)).getLong();
             info.addColumn(columnName, columnId, getMetadataForColumn(tableId, columnId));
         }
 
         rowKey = RowKeyFactory.buildTableInfoKey(tableId);
         Result tableMetadata = table.get(new Get(rowKey));
         NavigableMap<byte[], byte[]> familyMap = tableMetadata.getFamilyMap(Constants.NIC);
         Map<String, byte[]> stringFamily = Maps.newHashMap();
         for (Map.Entry<byte[], byte[]> entry : familyMap.entrySet()) {
             stringFamily.put(new String(entry.getKey()), entry.getValue());
         }
         info.setTableMetadata(stringFamily);
 
         tableCache.put(tableName, info);
 
         return info;
     }
 
     public void renameTable(String from, String to) throws IOException {
         logger.info("Renaming table " + from + " to " + to);
 
         TableInfo info = tableCache.get(from);
 
         byte[] rowKey = RowKeyFactory.ROOT;
 
         Delete oldNameDelete = new Delete(rowKey);
 
         oldNameDelete.deleteColumn(Constants.NIC, from.getBytes());
 
         this.table.delete(oldNameDelete);
 
         Put nameChangePut = new Put(rowKey);
         nameChangePut.add(Constants.NIC, to.getBytes(), Bytes.toBytes(info.getId()));
 
         this.table.put(nameChangePut);
         this.table.flushCommits();
 
         info.setName(to);
 
         tableCache.remove(from);
         tableCache.put(to, info);
 
         logger.info("Rename complete!");
     }
 
     public ColumnMetadata getMetadataForColumn(long tableId, long columnId) throws IOException {
         Get metadataGet = new Get(RowKeyFactory.buildColumnInfoKey(tableId, columnId));
         Result result = table.get(metadataGet);
 
         byte[] jsonBytes = result.getValue(Constants.NIC, Constants.METADATA);
         return new ColumnMetadata(jsonBytes);
     }
 
     public boolean deleteRow(String tableName, UUID uuid) throws IOException {
         if (uuid == null) {
             return false;
         }
 
         TableInfo info = getTableInfo(tableName);
         long tableId = info.getId();
 
         byte[] dataRowKey = RowKeyFactory.buildDataKey(tableId, uuid);
         Get get = new Get(dataRowKey);
         Result result = table.get(get);
 
         List<Delete> deleteList = DeleteListFactory.createDeleteRowList(uuid, info, result, dataRowKey, Index.indexForTable(info.tableMetadata()));
 
         table.delete(deleteList);
        // incrementRowCount(tableName, -1);
 
         return true;
     }
 
     public boolean dropTable(String tableName) throws IOException {
         logger.info("Preparing to drop table " + tableName);
         TableInfo info = getTableInfo(tableName);
         long tableId = info.getId();
 
         deleteIndexRows(tableId);
         deleteDataRows(tableId);
         deleteColumnInfoRows(info);
         deleteColumns(tableId);
         deleteTableInfoRows(tableId);
         deleteTableFromRoot(tableName);
 
         logger.info("Table " + tableName + " is no more!");
 
         return true;
     }
 
     public int deleteAllRows(String tableName) throws IOException {
         long tableId = getTableInfo(tableName).getId();
 
         logger.info("Deleting all rows from table " + tableName + " with tableId " + tableId);
 
         deleteIndexRows(tableId);
 
         return deleteDataRows(tableId);
     }
 
     private int deleteTableInfoRows(long tableId) throws IOException {
         byte[] prefix = ByteBuffer.allocate(9).put(RowType.TABLE_INFO.getValue()).putLong(tableId).array();
         return deleteRowsWithPrefix(prefix);
     }
 
     private int deleteDataRows(long tableId) throws IOException {
         logger.info("Deleting all data rows");
         byte[] prefix = ByteBuffer.allocate(9).put(RowType.DATA.getValue()).putLong(tableId).array();
         return deleteRowsWithPrefix(prefix);
     }
 
     private int deleteColumns(long tableId) throws IOException {
         logger.info("Deleting all columns");
         byte[] prefix = ByteBuffer.allocate(9).put(RowType.COLUMNS.getValue()).putLong(tableId).array();
         return deleteRowsWithPrefix(prefix);
     }
 
     private int deleteIndexRows(long tableId) throws IOException {
         logger.info("Deleting all index rows");
 
         int affectedRows = 0;
 
         byte[] valuePrefix = ByteBuffer.allocate(9).put(RowType.PRIMARY_INDEX.getValue()).putLong(tableId).array();
         byte[] reversePrefix = ByteBuffer.allocate(9).put(RowType.REVERSE_INDEX.getValue()).putLong(tableId).array();
 
         affectedRows += deleteRowsWithPrefix(valuePrefix);
         affectedRows += deleteRowsWithPrefix(reversePrefix);
 
         return affectedRows;
     }
 
     private int deleteColumnInfoRows(TableInfo info) throws IOException {
         logger.info("Deleting all column metadata rows");
 
         long tableId = info.getId();
         int affectedRows = 0;
 
         for (Long columnId : info.getColumnIds()) {
             byte[] metadataKey = RowKeyFactory.buildColumnInfoKey(tableId, columnId);
             affectedRows += deleteRowsWithPrefix(metadataKey);
         }
 
         return affectedRows;
     }
 
     private int deleteRowsWithPrefix(byte[] prefix) throws IOException {
         Scan scan = ScanFactory.buildScan();
         PrefixFilter filter = new PrefixFilter(prefix);
         scan.setFilter(filter);
 
         ResultScanner scanner = table.getScanner(scan);
         List<Delete> deleteList = new LinkedList<Delete>();
         int count = 0;
 
         for (Result result : scanner) {
             byte[] rowKey = result.getRow();
             Delete rowDelete = new Delete(rowKey);
             deleteList.add(rowDelete);
 
             ++count;
         }
 
         table.delete(deleteList);
 
         return count;
     }
 
     public void incrementRowCount(String tableName, long delta) throws IOException {
         long tableId = getTableInfo(tableName).getId();
         byte[] rowKey = RowKeyFactory.buildTableInfoKey(tableId);
         table.incrementColumnValue(rowKey, Constants.NIC, Constants.ROW_COUNT, delta);
     }
 
     public void setRowCount(String tableName, long value) throws IOException {
         long tableId = getTableInfo(tableName).getId();
         Put put = new Put(RowKeyFactory.buildTableInfoKey(tableId)).add(Constants.NIC, Constants.ROW_COUNT, Bytes.toBytes(value));
         table.put(put);
     }
 
     public long getRowCount(String tableName) throws IOException {
         TableInfo tableInfo = getTableInfo(tableName);
         long tableId = tableInfo.getId();
         byte[] rowKey = RowKeyFactory.buildTableInfoKey(tableId);
         return table.incrementColumnValue(rowKey, Constants.NIC, Constants.ROW_COUNT, 0);
     }
 
     public void deleteTableFromRoot(String tableName) throws IOException {
         Delete delete = new Delete((RowKeyFactory.ROOT));
         delete.deleteColumns(Constants.NIC, tableName.getBytes());
 
         table.delete(delete);
     }
 
     public void setCacheSize(int cacheSize) {
         logger.info("Setting table scan row cache to " + cacheSize);
         ScanFactory.setCacheAmount(cacheSize);
     }
 
     public void setAutoFlushTables(boolean shouldFlushChangesImmediately) {
         this.table.setAutoFlush(shouldFlushChangesImmediately);
 
         logger.info(shouldFlushChangesImmediately
                 ? "Changes to tables will be written to HBase immediately"
                 : "Changes to tables will be written to HBase when the write buffer has become full");
     }
 
     public String findDuplicateKey(String tableName, Map<String, byte[]> values) throws IOException {
         ScanStrategyInfo scanInfo = new ScanStrategyInfo(tableName, values.keySet(), valueMapToKeyValues(tableName, values));
         PrefixScanStrategy strategy = new PrefixScanStrategy(scanInfo);
 
         HBaseResultScanner scanner = new SingleResultScanner(getScanner(strategy));
 
         if (scanner.next(null) != null) {
             return Joiner.on(", ").join(scanInfo.columnNames());
         }
 
         return null;
     }
 
     public byte[] findDuplicateValue(String tableName, String columnNameStrings) throws IOException {
         TableInfo info = getTableInfo(tableName);
         List<String> columnNames = Arrays.asList(columnNameStrings.split(","));
 
         Scan scan = ScanFactory.buildScan();
         byte[] prefix = ByteBuffer.allocate(9).put(RowType.DATA.getValue()).putLong(info.getId()).array();
         PrefixFilter prefixFilter = new PrefixFilter(prefix);
 
         List<byte[]> columnIds = new LinkedList<byte[]>();
         for (String columnName : columnNames) {
             byte[] columnIdBytes = Bytes.toBytes(info.getColumnIdByName(columnName));
             columnIds.add(columnIdBytes);
             scan.addColumn(Constants.NIC, columnIdBytes);
         }
 
         scan.setFilter(prefixFilter);
 
         Set<ByteBuffer> columnValues = new HashSet<ByteBuffer>();
 
         ResultScanner scanner = this.table.getScanner(scan);
         Result result;
 
         while ((result = scanner.next()) != null) {
             List<byte[]> values = new LinkedList<byte[]>();
             int size = 0;
             for (byte[] columnIdBytes : columnIds) {
                 byte[] value = result.getValue(Constants.NIC, columnIdBytes);
                 values.add(value);
                 size += value.length;
             }
             ByteBuffer value = ByteBuffer.wrap(Util.mergeByteArrays(values, size));
 
             if (columnValues.contains(value)) {
                 return value.array();
             }
 
             columnValues.add(value);
         }
 
         return null;
     }
 
     public void setWriteBufferSize(long numBytes) {
         try {
             this.table.setWriteBufferSize(numBytes);
         } catch (IOException e) {
             logger.error("Encountered an error setting write buffer size", e);
         }
 
         logger.info("Size of HBase write buffer set to " + numBytes + " bytes (" + (numBytes / 1024 / 1024) + " megabytes)");
     }
 
     public void flushWrites() {
         try {
             table.flushCommits();
         } catch (IOException e) {
             logger.error("Encountered an exception while flushing commits : ", e);
         }
     }
 
     public ResultScanner getScanner(ScanStrategy strategy) throws IOException {
         TableInfo info = getTableInfo(strategy.getTableName());
 
         Scan scan = strategy.getScan(info);
 
         return table.getScanner(scan);
     }
 
     public long getNextAutoincrementValue(String tableName, String columnName) throws IOException {
         TableInfo info = getTableInfo(tableName);
         long columnId = info.getColumnIdByName(columnName);
         long tableId = info.getId();
         byte[] columnInfoBytes = RowKeyFactory.buildColumnInfoKey(tableId, columnId);
         return table.incrementColumnValue(columnInfoBytes, Constants.NIC, new byte[0], 1);
     }
 
     public void setupKeyValues(String tableName, List<String> columnName, List<KeyValue> keyValues, byte fill) throws IOException {
         TableInfo info = getTableInfo(tableName);
         for (String column : columnName) {
             ColumnMetadata metadata = info.getColumnMetadata(column);
             byte[] value = new byte[metadata.getMaxLength()];
             Arrays.fill(value, fill);
             keyValues.add(new KeyValue(column, value, metadata.isNullable(), false));
         }
     }
 
     public boolean isNullable(String tableName, String columnName) throws IOException {
         TableInfo info = getTableInfo(tableName);
         return info.getColumnMetadata(columnName).isNullable();
     }
 
     private List<KeyValue> valueMapToKeyValues(String tableName, Map<String, byte[]> valueMap) throws IOException {
         TableInfo info = getTableInfo(tableName);
         List<KeyValue> keyValues = new LinkedList<KeyValue>();
         for (Map.Entry<String, byte[]> entry : valueMap.entrySet()) {
             String key = entry.getKey();
             ColumnMetadata metadata = info.getColumnMetadata(key);
             byte[] value = entry.getValue();
             keyValues.add(new KeyValue(key, value, metadata.isNullable(), value == null));
         }
 
         return keyValues;
     }
 
     public void addIndex(String tableName, String columnString) throws IOException {
         final List<String> columnsToIndex = Arrays.asList(columnString.split(","));
         final TableInfo info = getTableInfo(tableName);
         updateIndexEntryToMetadata(info, new Function<List<List<String>>, Void>() {
             @Override
             public Void apply(List<List<String>> index) {
                 index.add(columnsToIndex);
                 return null;
             }
         });
 
         changeIndex(info, new IndexFunction<Map<String, byte[]>, UUID, Void>() {
             @Override
             public Void apply(Map<String, byte[]> values, UUID uuid) {
                 List<Put> puts = PutListFactory.createIndexForColumns(values, info, uuid, columnsToIndex);
                 try {
                     table.put(puts);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
 
                 return null;
             }
         });
     }
 
     public void dropIndex(String tableName, String indexToDrop) throws IOException {
         final List<String> indexColumns = Arrays.asList(indexToDrop.split(","));
         final TableInfo info = getTableInfo(tableName);
         updateIndexEntryToMetadata(info, new Function<List<List<String>>, Void>() {
             @Override
             public Void apply(List<List<String>> index) {
                 index.remove(indexColumns);
                 return null;
             }
         });
 
         changeIndex(info, new IndexFunction<Map<String, byte[]>, UUID, Void>() {
             @Override
             public Void apply(Map<String, byte[]> values, UUID rowId) {
                 List<Delete> deletes = DeleteListFactory.createDeleteForIndex(values, info, rowId, indexColumns);
                 try {
                     table.delete(deletes);
                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }
                 return null;
             }
         });
     }
 
     private void updateIndexEntryToMetadata(TableInfo info, Function<List<List<String>>, Void> updateFunc) throws IOException {
         final String tableName = info.getName();
         final long tableId = info.getId();
         List<List<String>> index = Index.indexForTable(info.tableMetadata());
         updateFunc.apply(index);
         final byte[] bytes = TableMultipartKeys.indexJson(index);
         updateTableCacheIndex(tableName, bytes);
 
         Put indexUpdate = new Put(RowKeyFactory.buildTableInfoKey(tableId));
         indexUpdate.add(Constants.NIC, Constants.INDEXES, bytes);
         this.table.put(indexUpdate);
         this.table.flushCommits();
     }
 
     private void changeIndex(TableInfo info, IndexFunction<Map<String, byte[]>, UUID, Void> function) throws IOException {
         final long tableId = info.getId();
         byte[] startKey = RowKeyFactory.buildDataKey(tableId, Constants.ZERO_UUID);
         byte[] endKey = RowKeyFactory.buildDataKey(tableId, Constants.FULL_UUID);
         Scan scan = ScanFactory.buildScan(startKey, endKey);
         ResultScanner scanner = this.table.getScanner(scan);
         Result result;
         while ((result = scanner.next()) != null) {
             Map<String, byte[]> values = ResultParser.parseDataRow(result, info);
             UUID rowId = ResultParser.parseUUID(result);
             function.apply(values, rowId);
         }
 
         table.flushCommits();
     }
 
     private void writeBlob(String tableName, String columnName, ByteBuffer blob, Put blobPut) throws IOException {
         // This is a nasty hack to reduce the memory used by blobs when writing to HBase.
         TableInfo info = getTableInfo(tableName);
         int vlength = blob.capacity();
         long columnId = info.columnNameToIdMap().get(columnName);
         byte[] rowKey = blobPut.getRow();
         byte[] qualifier = Bytes.toBytes(columnId);
         int qlength = qualifier.length;
         int flength = Constants.NIC.length;
         org.apache.hadoop.hbase.KeyValue keyValue = new org.apache.hadoop.hbase.KeyValue(
                 rowKey,
                 0,
                 rowKey.length,
                 Constants.NIC,
                 0,
                 flength,
                 qualifier,
                 0,
                 qlength,
                 HConstants.LATEST_TIMESTAMP,
                 org.apache.hadoop.hbase.KeyValue.Type.Put,
                 new byte[0],
                 0,
                 vlength);
         byte[] buffer = keyValue.getBuffer();
         blob.get(buffer, buffer.length - vlength, vlength);
         blobPut.add(keyValue);
     }
 
     private interface IndexFunction<F1, F2, T> {
         T apply(F1 f1, F2 f2);
     }
 }
