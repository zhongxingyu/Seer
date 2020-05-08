 package com.nearinfinity.mysqlengine;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.*;
 import org.apache.hadoop.hbase.client.*;
 import org.apache.hadoop.hbase.filter.BinaryComparator;
 import org.apache.hadoop.hbase.filter.CompareFilter;
 import org.apache.hadoop.hbase.filter.RowFilter;
 import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Filter;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jedstrom
  * Date: 7/25/12
  * Time: 2:09 PM
  * To change this template use File | Settings | File Templates.
  */
 public class HBaseClient {
     private HTable table;
 
     private HBaseAdmin admin;
 
     private static final byte[] SQL = "sql".getBytes();
 
     private static final byte[] NIC = "nic".getBytes();
 
     private static final byte[] IS_DELETED = "isDeleted".getBytes();
 
     private static final byte[] DELETED_VAL = Bytes.toBytes(1L);
 
     private static final byte[] UNIREG = "unireg".getBytes();
 
     private static final byte[] VALUE_COLUMN = "value".getBytes();
 
     private static final UUID ZERO_UUID = new UUID(0L, 0L);
 
    private static final UUID FULL_UUID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
 
     private int cacheSize = 10;
 
     private final ConcurrentHashMap<String, TableInfo> tableCache = new ConcurrentHashMap<String, TableInfo>();
 
     private static final Logger logger = Logger.getLogger(HBaseClient.class);
 
     public HBaseClient(String tableName, String zkQuorum) {
         logger.info("HBaseClient: Constructing with HBase table name: " + tableName);
         logger.info("HBaseClient: Constructing with ZK Quorum: " + zkQuorum);
 
         Configuration configuration = HBaseConfiguration.create();
         configuration.set("hbase.zookeeper.quorum", zkQuorum);
 
         try {
             this.admin = new HBaseAdmin(configuration);
             this.initializeSqlTable();
 
             this.table = new HTable(configuration, tableName);
         } catch (MasterNotRunningException e) {
             logger.error("MasterNotRunningException thrown", e);
         } catch (ZooKeeperConnectionException e) {
             logger.error("ZooKeeperConnectionException thrown", e);
         } catch (IOException e) {
             logger.error("IOException thrown", e);
         } catch (InterruptedException e) {
             logger.error("InterruptedException thrown", e);
         }
     }
 
     private void initializeSqlTable() throws IOException, InterruptedException {
         HTableDescriptor sqlTableDescriptor;
         HColumnDescriptor nicColumn = new HColumnDescriptor(NIC);
 
         if (!this.admin.tableExists(SQL)) {
             logger.info("Creating sql table");
             sqlTableDescriptor = new HTableDescriptor(SQL);
             sqlTableDescriptor.addFamily(nicColumn);
 
             this.admin.createTable(sqlTableDescriptor);
         }
 
         sqlTableDescriptor = this.admin.getTableDescriptor(SQL);
         if (!sqlTableDescriptor.hasFamily(NIC)) {
             logger.info("Adding nic column family to sql table");
 
             if (!this.admin.isTableDisabled(SQL)) {
                 logger.info("Disabling sql table");
                 this.admin.disableTable(SQL);
             }
 
             this.admin.addColumn(SQL, nicColumn);
         }
 
         if (this.admin.isTableDisabled(SQL)) {
             logger.info("Enabling sql table");
             this.admin.enableTable(SQL);
         }
 
         this.admin.flush(SQL);
     }
 
     private void createTable(String tableName, List<Put> puts) throws IOException {
         logger.info("HBaseClient: createTable called");
 
         //Get and increment the table counter (assumes it exists)
         long tableId = table.incrementColumnValue(RowKeyFactory.ROOT, NIC, new byte[0], 1);
 
         //Add a row with the table name
         puts.add(new Put(RowKeyFactory.ROOT).add(NIC, tableName.getBytes(), Bytes.toBytes(tableId)));
 
         //Cache the table
         tableCache.put(tableName, new TableInfo(tableName, tableId));
     }
 
     private void addColumns(String tableName, List<String> columns, List<Put> puts) throws IOException {
         //Get table id from cache
         long tableId = tableCache.get(tableName).getId();
 
         //Build the column row key
         byte[] columnBytes = ByteBuffer.allocate(9).put(RowType.COLUMNS.getValue()).putLong(tableId).array();
 
         //Allocate ids and compute start id
         long numColumns = columns.size();
         long lastColumnId = table.incrementColumnValue(columnBytes, NIC, new byte[0], numColumns);
         long startColumn = lastColumnId - numColumns;
 
         for (String columnName : columns) {
             long columnId = ++startColumn;
 
             //Add put
             Put columnPut = new Put(columnBytes).add(NIC, columnName.getBytes(), Bytes.toBytes(columnId));
             puts.add(columnPut);
 
             //Add to cache
             tableCache.get(tableName).addColumn(columnName, columnId);
         }
     }
 
     public void createTableFull(String tableName, List<String> columns) throws IOException {
         logger.info("HBaseClient: createTableFull");
         //Batch put list
         List<Put> putList = new LinkedList<Put>();
 
         //Create table and add to put list
         createTable(tableName, putList);
 
         //Create columns and add to put list
         addColumns(tableName, columns, putList);
 
         logger.info("HBaseClient: Putting " + putList.size() + " new HBase cells");
 
         //Perform all puts
         table.put(putList);
 
         table.flushCommits();
     }
 
     public void writeRow(String tableName, Map<String, byte[]> values) throws IOException {
         writeRow(tableName, values, null);
     }
 
     public void writeRow(String tableName, Map<String, byte[]> values, byte[] unireg) throws IOException {
         //Get table id
         long tableId = getTableInfo(tableName).getId();
 
         //Get UUID for new entry
         UUID rowId = UUID.randomUUID();
 
         //Build data row key
         byte[] dataKey = RowKeyFactory.buildDataKey(tableId, rowId);
 
         //Create put list
         List<Put> putList = new LinkedList<Put>();
 
         Put dataRow = new Put(dataKey);
 
         byte[] indexQualifier = new byte[0];
         byte[] indexValue = new byte[0];
         if (unireg != null) {
             indexQualifier = UNIREG;
             indexValue = unireg;
         }
 
        boolean allRowsNull = true;
 
         for (String columnName : values.keySet()) {
             //Get column id and value
             long columnId = getTableInfo(tableName).getColumnIdByName(columnName);
             byte[] value = values.get(columnName);
 
             if(value == null) {
                 // Build null index
                 byte[] nullIndexRow = RowKeyFactory.buildNullIndexKey(tableId, columnId, rowId);
                 putList.add(new Put(nullIndexRow).add(NIC, new byte[0], new byte[0]));
             } else {
                allRowsNull = false;
                 // Add data column to put
                 dataRow.add(NIC, Bytes.toBytes(columnId), value);
 
                 // Build value index key
                 byte[] indexRow = RowKeyFactory.buildValueIndexKey(tableId, columnId, value, rowId);
                 putList.add(new Put(indexRow).add(NIC, indexQualifier, indexValue));
 
                 // Build secondary index key
                 byte[] secondaryIndexRow = RowKeyFactory.buildSecondaryIndexKey(tableId, columnId, value);
                 putList.add(new Put(secondaryIndexRow).add(NIC, new byte[0], new byte[0]));
 
                 // Build reverse index key
                 byte[] reverseIndexRow = RowKeyFactory.buildReverseIndexKey(tableId, columnId, value);
                 putList.add(new Put(reverseIndexRow).add(NIC, VALUE_COLUMN, value));
             }
         }
 
        if(allRowsNull) {
             // Add special []->[] data row to signify a row of all null values
             putList.add(dataRow.add(NIC, new byte[0], new byte[0]));
         }
 
         //Add the row to put list
         putList.add(dataRow);
 
         //Final put
         table.put(putList);
     }
 
     public List<Map<String, byte[]>> fullTableScan(String tableName) throws IOException {
         logger.info("HBaseClient.fullTableScan");
 
         //Get table id
         TableInfo info = getTableInfo(tableName);
         long tableId = info.getId();
 
         //Build row keys
         byte[] startRow = RowKeyFactory.buildDataKey(tableId, ZERO_UUID);
         byte[] endRow = RowKeyFactory.buildDataKey(tableId + 1, ZERO_UUID);
 
         Scan scan = new Scan(startRow, endRow);
 
         //Scan all rows in HBase
         List<Map<String, byte[]>> rows = new LinkedList<Map<String, byte[]>>();
         ResultScanner results = table.getScanner(scan);
 
         for (Result result : results) {
             Map<String, byte[]> columns = new HashMap<String, byte[]>();
             Map<byte[], byte[]> returnedColumns = result.getNoVersionMap().get(NIC);
             for (byte[] qualifier : returnedColumns.keySet()) {
                 long columnId = ByteBuffer.wrap(qualifier).getLong();
                 String columnName = info.getColumnNameById(columnId);
                 columns.put(columnName, returnedColumns.get(qualifier));
             }
             rows.add(columns);
         }
 
         return rows;
     }
 
     public Result getDataRow(UUID uuid, String tableName) throws IOException {
         TableInfo info = getTableInfo(tableName);
         long tableId = info.getId();
 
         byte[] rowKey = RowKeyFactory.buildDataKey(tableId, uuid);
 
         Get get = new Get(rowKey);
         return table.get(get);
     }
 
     public ResultScanner search(String tableName, String columnName, byte[] value) throws IOException {
         logger.info("HBaseClient.search");
 
         //Get table and column id
         TableInfo info = getTableInfo(tableName);
         long tableId = info.getId();
         long columnId = info.getColumnIdByName(columnName);
 
         //Build row keys
         byte[] startRow = RowKeyFactory.buildValueIndexKey(tableId, columnId, value, ZERO_UUID);
         byte[] endRow = RowKeyFactory.buildValueIndexKey(tableId, columnId + 1, value, ZERO_UUID);
 
         Scan scan = new Scan(startRow, endRow);
 
         return table.getScanner(scan);
     }
 
     public ResultScanner getTableScanner(String tableName, boolean isFullTableScan) throws IOException {
         //Get table id
         TableInfo info = getTableInfo(tableName);
         long tableId = info.getId();
 
         //Build row keys
         byte[] startRow = RowKeyFactory.buildDataKey(tableId, ZERO_UUID);
         byte[] endRow = RowKeyFactory.buildDataKey(tableId + 1, ZERO_UUID);
 
         Scan scan = new Scan(startRow, endRow);
 
         //Set the caching for the scan
         int rowsToCacheForScan = isFullTableScan ? this.cacheSize : 10;
         logger.info("Starting scan with cache size " + rowsToCacheForScan);
         scan.setCaching(rowsToCacheForScan);
 
         //Exclude deleted values
         SingleColumnValueFilter filter = new SingleColumnValueFilter(NIC, IS_DELETED, CompareFilter.CompareOp.NOT_EQUAL, DELETED_VAL);
         scan.setFilter(filter);
 
         return table.getScanner(scan);
     }
 
     public ResultScanner getIndexScanner(String tableName, String columnName) throws IOException {
         //Get the table id
         TableInfo info = getTableInfo(tableName);
         long tableId = info.getId();
         long columnId = info.getColumnIdByName(columnName);
 
         //Build row keys
         byte[] startRow = RowKeyFactory.buildValueIndexKey(tableId, columnId, new byte[0], ZERO_UUID);
         byte[] endRow = RowKeyFactory.buildValueIndexKey(tableId, columnId + 1, new byte[0], ZERO_UUID);
 
         Scan scan = new Scan(startRow, endRow);
 
         return table.getScanner(scan);
     }
 
     public ResultScanner getValueIndexScanner(String tableName, String columnName, byte[] value) throws IOException {
         //Get the table id
         TableInfo info = getTableInfo(tableName);
         long tableId = info.getId();
         long columnId = info.getColumnIdByName(columnName);
 
         //Build row keys
         byte[] startRow = RowKeyFactory.buildValueIndexKey(tableId, columnId, value, ZERO_UUID);
         byte[] endRow = RowKeyFactory.buildValueIndexKey(tableId, columnId, value, FULL_UUID);
 
         Scan scan = new Scan(startRow, endRow);
 
         return table.getScanner(scan);
     }
 
     private TableInfo getTableInfo(String tableName) throws IOException {
         if (tableCache.containsKey(tableName)) {
             return tableCache.get(tableName);
         }
 
         //Get the table id from HBase
         Get tableIdGet = new Get(RowKeyFactory.ROOT);
         Result result = table.get(tableIdGet);
         long tableId = ByteBuffer.wrap(result.getValue(NIC, tableName.getBytes())).getLong();
 
         TableInfo info = new TableInfo(tableName, tableId);
 
         byte[] rowKey = RowKeyFactory.buildColumnsKey(tableId);
 
         Get columnsGet = new Get(rowKey);
         Result columnsResult = table.get(columnsGet);
         Map<byte[], byte[]> columns = columnsResult.getFamilyMap(NIC);
         for (byte[] qualifier : columns.keySet()) {
             String columnName = new String(qualifier);
             long columnId = ByteBuffer.wrap(columns.get(qualifier)).getLong();
             info.addColumn(columnName, columnId);
         }
 
         return info;
     }
 
     public Map<String, byte[]> parseRow(Result result, String tableName) throws IOException {
         TableInfo info = getTableInfo(tableName);
 
         //Get columns returned from Result
         Map<String, byte[]> columns = new HashMap<String, byte[]>();
         Map<byte[], byte[]> returnedColumns = result.getNoVersionMap().get(NIC);
 
         if(returnedColumns.size() == 1 && returnedColumns.containsKey(new byte[0])) {
             // The row of all nulls special case strikes again
             return columns;
         }
 
         //Loop through columns, add to returned map
         for (byte[] qualifier : returnedColumns.keySet()) {
             long columnId = ByteBuffer.wrap(qualifier).getLong();
             String columnName = info.getColumnNameById(columnId);
             columns.put(columnName, returnedColumns.get(qualifier));
         }
 
         return columns;
     }
 
     public boolean deleteRow(byte[] rowKey) throws IOException {
         Put deletePut = new Put(rowKey);
 
         deletePut.add(NIC, IS_DELETED, DELETED_VAL);
 
         table.put(deletePut);
 
         return true;
     }
 
     public UUID parseUUIDFromDataRow(Result result) {
         byte[] rowKey = result.getRow();
         ByteBuffer buffer = ByteBuffer.wrap(rowKey, rowKey.length - 16, 16);
         return new UUID(buffer.getLong(), buffer.getLong());
     }
 
     public void compact() throws IOException {
         Scan scan = new Scan();
 
         //Filter only rows with isDeleted=1
         SingleColumnValueFilter filter = new SingleColumnValueFilter(NIC, IS_DELETED, CompareFilter.CompareOp.EQUAL, DELETED_VAL);
         filter.setFilterIfMissing(true);
         scan.setFilter(filter);
 
         ResultScanner scanner = table.getScanner(scan);
         List<Delete> deleteList = new LinkedList<Delete>();
 
         Set<UUID> deletedUUIDs = new HashSet<UUID>();
         for (Result result : scanner) {
             //Delete the data row key
             byte[] rowKey = result.getRow();
             Delete rowDelete = new Delete(rowKey);
             deleteList.add(rowDelete);
 
             deletedUUIDs.add(parseUUIDFromDataRow(result));
         }
 
         /**
          * TODO:
          * Still not sure how this should be done. Right now it scans ALL indexes to build Deletes for all row keys.
          * Should we build indexes from the row we got previously? Should we only scan each table we know has deleted
          * values? There is some optimization to be done here...
          */
         Scan indexScan = new Scan();
 //        Filter uuidFilter = new UUIDFilter(deletedUUIDs);
 //        scan.setFilter(uuidFilter);
         ResultScanner indexScanner = table.getScanner(indexScan);
         for (Result result : indexScanner) {
             byte[] rowKey = result.getRow();
 
             /* TODO: This is a temporary workaround until we can write a CustomFilter */
             if (rowKey.length < 16) {
                 continue;
             }
             ByteBuffer byteBuffer = ByteBuffer.wrap(rowKey, rowKey.length - 16, 16);
             UUID rowUUID = new UUID(byteBuffer.getLong(), byteBuffer.getLong());
             if (deletedUUIDs.contains(rowUUID)) {
                 Delete indexDelete = new Delete(rowKey);
                 deleteList.add(indexDelete);
             }
         }
 
         table.delete(deleteList);
     }
 
     public void setCacheSize(int cacheSize)
     {
         logger.info("Setting table scan row cache to " + cacheSize);
         this.cacheSize = cacheSize;
     }
 
     public UUID parseUUIDFromIndexRow(Result result) {
         byte[] rowKey = result.getRow();
         ByteBuffer byteBuffer = ByteBuffer.wrap(rowKey, rowKey.length - 16, 16);
         return new UUID(byteBuffer.getLong(), byteBuffer.getLong());
     }
 
     public Result getIndexRow(String tableName, String columnName, byte[] value) throws IOException {
         //Get the table id
         TableInfo info = getTableInfo(tableName);
         long tableId = info.getId();
         long columnId = info.getColumnIdByName(columnName);
 
         RowKeyFactory.buildValueIndexKey(tableId, columnId, value, ZERO_UUID);
 
         return null;
     }
 
     public void setAutoFlushTables(boolean shouldFlushChangesImmediately)
     {
         this.table.setAutoFlush(shouldFlushChangesImmediately);
 
         logger.info(shouldFlushChangesImmediately
                 ? "Changes to tables will be written to HBase immediately"
                 : "Changes to tables will be written to HBase when the write buffer has become full");
     }
 
     public void setWriteBufferSize(long numBytes) {
         try {
             this.table.setWriteBufferSize(numBytes);
         } catch (IOException e) {
             e.printStackTrace();
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
 
     public byte[] parseUniregFromIndex(Result firstResult) {
         return firstResult.getValue(NIC, UNIREG);
     }
 
     public ResultScanner getSecondaryIndexScanner(String tableName, String columnName, byte[] value) throws IOException {
         TableInfo info = getTableInfo(tableName);
         long tableId = info.getId();
         long columnId = info.getColumnIdByName(columnName);
 
         byte[] startKey = RowKeyFactory.buildSecondaryIndexKey(tableId, columnId, value);
         byte[] endKey = RowKeyFactory.buildSecondaryIndexKey(tableId, columnId+1, new byte[0]);
 
         Scan scan = new Scan(startKey, endKey);
 
         return table.getScanner(scan);
     }
 
     public ResultScanner getSecondaryIndexScannerExact(String tableName, String columnName, byte[] value) throws IOException {
         TableInfo info = getTableInfo(tableName);
         long tableId = info.getId();
         long columnId = info.getColumnIdByName(columnName);
 
         byte[] startKey = RowKeyFactory.buildSecondaryIndexKey(tableId, columnId, value);
         byte[] endKey = RowKeyFactory.buildSecondaryIndexKey(tableId, columnId+1, new byte[0]);
 
         Scan scan = new Scan(startKey, endKey);
 
         RowFilter filter = new RowFilter(CompareFilter.CompareOp.EQUAL, new BinaryComparator(startKey));
         scan.setFilter(filter);
 
         return table.getScanner(scan);
     }
 
     public byte[] parseValueFromSecondaryIndexRow(Result result) {
         byte[] row = result.getRow();
         ByteBuffer buffer = ByteBuffer.wrap(row, 17, row.length - 17);
         return buffer.array();
     }
 
     public ResultScanner getReverseIndexScanner(String tableName, String columnName, byte[] value) throws IOException {
         TableInfo info = getTableInfo(tableName);
         long tableId = info.getId();
         long columnId = info.getColumnIdByName(columnName);
 
         byte[] startKey = RowKeyFactory.buildReverseIndexKey(tableId, columnId, value);
         byte[] endKey = RowKeyFactory.buildReverseIndexKey(tableId, columnId+1, value);
 
         Scan scan = new Scan(startKey, endKey);
 
         return table.getScanner(scan);
     }
 
     public byte[] parseValueFromReverseIndexRow(Result result) {
         return RowKeyFactory.parseValueFromReverseIndexKey(result.getRow());
     }
 }
