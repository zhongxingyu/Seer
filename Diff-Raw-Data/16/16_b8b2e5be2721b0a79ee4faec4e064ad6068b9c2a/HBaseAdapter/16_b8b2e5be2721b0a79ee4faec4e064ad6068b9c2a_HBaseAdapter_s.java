 package com.nearinfinity.mysqlengine.jni;
 
 import com.google.common.collect.Iterables;
 import com.nearinfinity.hbaseclient.*;
 import com.nearinfinity.hbaseclient.strategy.*;
 import com.nearinfinity.mysqlengine.Connection;
 import com.nearinfinity.mysqlengine.scanner.HBaseResultScanner;
 import com.nearinfinity.mysqlengine.scanner.SingleResultScanner;
 import org.apache.hadoop.hbase.ZooKeeperConnectionException;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.log4j.Logger;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicLong;
 
 import static java.text.MessageFormat.format;
 
 public class HBaseAdapter {
     private static AtomicLong connectionCounter;
     private static Map<Long, Connection> clientPool;
     private static HBaseClient client;
     private static final Logger logger = Logger.getLogger(HBaseAdapter.class);
 
     private static final int DEFAULT_NUM_CACHED_ROWS = 2500;
     private static final long DEFAULT_WRITE_BUFFER_SIZE = 5 * 1024 * 1024;
     private static boolean isInitialized = false;
 
     public static void initialize() throws IOException {
         if (isInitialized) {
             return;
         }
 
         logger.info("Static Initializer-> Begin");
 
         //Read config options from adapter.conf
         File source = new File("/etc/mysql/adapter.conf");
         if (!(source.exists() && source.canRead() && source.isFile())) {
             throw new FileNotFoundException("/etc/mysql/adapter.conf doesn't exist or cannot be read.");
         }
 
         Map<String, String> params = readParameters(source);
         logger.info(format("Read in {0} parameters.", params.size()));
 
         try {
             client = new HBaseClient(params.get("hbase_table_name"), params.get("zk_quorum"));
         } catch (ZooKeeperConnectionException e) {
             logger.fatal("Could not connect to zookeeper. ", e);
             throw e;
         } catch (IOException e) {
             logger.fatal("Could not create HBase client. Aborting initialization.");
             throw e;
         }
         logger.info("HBaseClient successfully created.");
         connectionCounter = new AtomicLong(0L);
         clientPool = new ConcurrentHashMap<Long, Connection>();
 
         try {
             int cacheSize = Integer.parseInt(params.get("table_scan_cache_rows"));
             client.setCacheSize(cacheSize);
         } catch (NumberFormatException e) {
             logger.info(format("Static Initializer-> Number of rows to cache was not provided or invalid - using default of {0}", DEFAULT_NUM_CACHED_ROWS));
             client.setCacheSize(DEFAULT_NUM_CACHED_ROWS);
         }
 
         try {
             long writeBufferSize = Long.parseLong(params.get("write_buffer_size"));
             client.setWriteBufferSize(writeBufferSize);
         } catch (NumberFormatException e) {
             logger.info(format("Static Initializer-> Write buffer size was not provided or invalid - using default of {0}", DEFAULT_WRITE_BUFFER_SIZE));
             client.setWriteBufferSize(DEFAULT_WRITE_BUFFER_SIZE);
         }
 
         boolean flushChangesImmediately = Boolean.parseBoolean(params.get("flush_changes_immediately"));
         client.setAutoFlushTables(flushChangesImmediately);
         isInitialized = true;
         logger.info("Static Initializer-> End");
     }
 
     private static Map<String, String> readParameters(File source) throws FileNotFoundException {
         Scanner confFile = new Scanner(source);
         Map<String, String> params = new HashMap<String, String>();
         while (confFile.hasNextLine()) {
             Scanner line = new Scanner(confFile.nextLine());
             params.put(line.next(), line.next());
         }
         return params;
     }
 
     public static boolean createTable(String tableName, Map<String, ColumnMetadata> columns, TableMultipartKeys multipartKeys) throws HBaseAdapterException {
         logger.info("creatingTable-> tableName:" + tableName);
 
         try {
             client.createTableFull(tableName, columns, multipartKeys);
         } catch (Exception e) {
             logger.error("createTable-> Exception:", e);
             throw new HBaseAdapterException("createTable", e);
         }
 
         return true;
     }
 
     public static long startScan(String tableName, boolean isFullTableScan) throws HBaseAdapterException {
         long scanId = connectionCounter.incrementAndGet();
         logger.info("startScan-> tableName: " + tableName + ", scanId: " + scanId + ", isFullTableScan: " + isFullTableScan);
         try {
             ScanStrategy strategy = new FullTableScanStrategy(tableName);
             SingleResultScanner dataScanner = new SingleResultScanner(client.getScanner(strategy));
             clientPool.put(scanId, new Connection(tableName, dataScanner));
         } catch (Exception e) {
             logger.error("startScan-> Exception:", e);
             throw new HBaseAdapterException("startScan", e);
         }
 
         return scanId;
     }
 
     public static Row nextRow(long scanId) throws HBaseAdapterException {
         Connection conn = getConnectionForId(scanId);
 
         Row row = new Row();
         try {
             HBaseResultScanner scanner = conn.getScanner();
             Result result = scanner.next(null);
 
             if (result == null) {
                 return null;
             }
 
             //Set values and UUID
             TableInfo info = client.getTableInfo(conn.getTableName());
             Map<String, byte[]> values = ResultParser.parseDataRow(result, info);
             UUID uuid = ResultParser.parseUUID(result);
             row.parse(values, uuid);
         } catch (Exception e) {
             logger.error("nextRow-> Exception:", e);
             throw new HBaseAdapterException("nextRow", e);
         }
 
         return row;
     }
 
     public static void endScan(long scanId) throws HBaseAdapterException {
         logger.info("endScan-> scanId: " + scanId);
         Connection conn = getConnectionForId(scanId);
         conn.close();
     }
 
     public static boolean writeRow(String tableName, Map<String, byte[]> values, List<Blob> blobs) throws HBaseAdapterException {
         try {
             client.writeRow(tableName, values, blobs);
         } catch (Exception e) {
             logger.error("writeRow-> Exception:", e);
             throw new HBaseAdapterException("writeRow", e);
         }
 
         return true;
     }
 
     public static void flushWrites() {
         client.flushWrites();
     }
 
     public static boolean deleteRow(long scanId) throws HBaseAdapterException {
         logger.info("deleteRow-> scanId: " + scanId);
 
         boolean deleted;
         try {
             Connection conn = getConnectionForId(scanId);
             HBaseResultScanner scanner = conn.getScanner();
             Result result = scanner.getLastResult();
             String tableName = conn.getTableName();
             UUID uuid = ResultParser.parseUUID(result);
 
             deleted = client.deleteRow(tableName, uuid);
        } catch (IOException e) {
             logger.error("deleteRow-> Exception:", e);
             throw new HBaseAdapterException("deleteRow", e);
         }
 
         return deleted;
     }
 
     public static int deleteAllRows(String tableName) throws HBaseAdapterException {
         logger.info("deleteAllRows-> tableName: " + tableName);
 
         int deleted;
         try {
             deleted = client.deleteAllRows(tableName);
         } catch (IOException e) {
             logger.error("deleteAllRows-> Exception:", e);
             throw new HBaseAdapterException("deleteAllRows", e);
         }
 
         return deleted;
     }
 
     public static boolean dropTable(String tableName) throws HBaseAdapterException {
         logger.info("dropTable-> tableName: " + tableName);
 
         boolean deleted;
         try {
             deleted = client.dropTable(tableName);
         } catch (IOException e) {
             logger.error("dropTable-> Exception:", e);
             throw new HBaseAdapterException("dropTable", e);
         }
 
         return deleted;
     }
 
     public static Row getRow(long scanId, byte[] uuid) throws HBaseAdapterException {
         logger.info("getRow-> scanId: " + scanId + "," + Bytes.toString(uuid));
         Connection conn = getConnectionForId(scanId);
 
         Row row = new Row();
         try {
             String tableName = conn.getTableName();
             ByteBuffer buffer = ByteBuffer.wrap(uuid);
             UUID rowUuid = new UUID(buffer.getLong(), buffer.getLong());
 
             Result result = client.getDataRow(rowUuid, tableName);
 
             if (result == null) {
                 logger.error("getRow-> Exception: Row not found");
                 throw new HBaseAdapterException("getRow", new Exception());
             }
 
             conn.getScanner().setLastResult(result);
 
             TableInfo info = client.getTableInfo(conn.getTableName());
             Map<String, byte[]> values = ResultParser.parseDataRow(result, info);
             row.setUUID(rowUuid);
             row.setRowMap(values);
         } catch (Exception e) {
             logger.error("getRow-> Exception:", e);
             throw new HBaseAdapterException("getRow", e);
         }
 
         return row;
     }
 
     public static long startIndexScan(String tableName, String columnName) throws HBaseAdapterException {
         logger.info("startIndexScan-> tableName " + tableName + ", columnNames: " + columnName);
 
         long scanId = connectionCounter.incrementAndGet();
         try {
             clientPool.put(scanId, new Connection(tableName, columnName));
         } catch (Exception e) {
             logger.error("startIndexScan-> Exception:", e);
             throw new HBaseAdapterException("startIndexScan", e);
         }
 
         return scanId;
     }
 
     public static String findDuplicateKey(String tableName, Map<String, byte[]> values) throws HBaseAdapterException {
         String result = null;
 
         try {
             result = client.findDuplicateKey(tableName, values);
         } catch (Exception e) {
             logger.error("hasDuplicateValues-> Exception:", e);
             throw new HBaseAdapterException("hasDuplicateValues", e);
         }
 
         return result;
     }
 
     public static byte[] findDuplicateValue(String tableName, String columnName) throws HBaseAdapterException {
         byte[] duplicate;
 
         try {
             duplicate = client.findDuplicateValue(tableName, columnName);
         } catch (Exception e) {
             logger.error("columnContainsDuplicates-> Exception:", e);
             throw new HBaseAdapterException("columnContainsDuplicates", e);
         }
 
         return duplicate;
     }
 
     public static long getNextAutoincrementValue(String tableName, String columnName) throws HBaseAdapterException {
         long autoIncrementValue = 0;
         try {
             autoIncrementValue = client.getNextAutoincrementValue(tableName, columnName);
         } catch (Exception e) {
             logger.error("columnContainsDuplicates-> Exception:", e);
             throw new HBaseAdapterException("columnContainsDuplicates", e);
         }
 
         return autoIncrementValue;
     }
 
     public static IndexRow indexRead(long scanId, List<KeyValue> keyValues, IndexReadType readType) throws HBaseAdapterException {
         Connection conn = getConnectionForId(scanId);
         IndexRow indexRow = new IndexRow();
         try {
             String tableName = conn.getTableName();
             List<String> columnName = conn.getColumnName();
             if (keyValues == null) {
                 if (readType != IndexReadType.INDEX_FIRST && readType != IndexReadType.INDEX_LAST) {
                     throw new IllegalArgumentException("keyValues can't be null unless first/last index read");
                 }
 
                 keyValues = new LinkedList<KeyValue>();
                 byte fill = (byte) (readType == IndexReadType.INDEX_FIRST ? 0x00 : 0xFF);
                 client.setupKeyValues(tableName, columnName, keyValues, fill);
             }
 
             ScanStrategyInfo scanInfo = new ScanStrategyInfo(tableName, columnName, keyValues);
 
             byte[] valueToSkip = null;
             HBaseResultScanner scanner = null;
 
             switch (readType) {
                 case HA_READ_KEY_EXACT: {
                     ScanStrategy strategy = new PrefixScanStrategy(scanInfo);
                     scanner = new SingleResultScanner(client.getScanner(strategy));
                 }
                 break;
                 case HA_READ_AFTER_KEY: {
                     ScanStrategy strategy = new OrderedScanStrategy(scanInfo);
                     scanner = new SingleResultScanner(client.getScanner(strategy));
                     valueToSkip = Iterables.getLast(scanInfo.keyValueValues());
                 }
                 break;
                 case HA_READ_KEY_OR_NEXT: {
                     ScanStrategy strategy = new OrderedScanStrategy(scanInfo);
                     scanner = new SingleResultScanner(client.getScanner(strategy));
                 }
                 break;
                 case HA_READ_BEFORE_KEY: {
                     ScanStrategy strategy = new ReverseScanStrategy(scanInfo);
                     scanner = new SingleResultScanner(client.getScanner(strategy));
                     valueToSkip = Iterables.getLast(scanInfo.keyValueValues());
                 }
                 break;
                 case HA_READ_KEY_OR_PREV: {
                     ScanStrategy strategy = new ReverseScanStrategy(scanInfo);
                     scanner = new SingleResultScanner(client.getScanner(strategy));
                 }
                 break;
                 case INDEX_FIRST: {
                     ScanStrategy strategy = new OrderedScanStrategy(scanInfo, true);
                     scanner = new SingleResultScanner(client.getScanner(strategy));
                 }
                 break;
                 case INDEX_LAST: {
                     ScanStrategy strategy = new ReverseScanStrategy(scanInfo, true);
                     scanner = new SingleResultScanner(client.getScanner(strategy));
                 }
                 break;
             }
 
             scanner.setColumnName(Iterables.getLast(scanInfo.keyValueColumns()));
 
             conn.setScanner(scanner);
             Result result = scanner.next(valueToSkip);
 
             if (result == null) {
                 return indexRow;
             }
 
             indexRow.parseResult(result);
         } catch (Exception e) {
             logger.error("indexRead-> Exception:", e);
             throw new HBaseAdapterException("indexRead", e);
         }
 
         return indexRow;
     }
 
     public static IndexRow nextIndexRow(long scanId) throws HBaseAdapterException {
         Connection conn = getConnectionForId(scanId);
 
         IndexRow indexRow = new IndexRow();
         try {
             HBaseResultScanner scanner = conn.getScanner();
             Result result = scanner.next(null);
             if (result == null) {
                 return indexRow;
             }
 
             indexRow.parseResult(result);
         } catch (Exception e) {
             logger.error("nextIndexRow-> Exception:", e);
             throw new HBaseAdapterException("nextIndexRow", e);
         }
 
         return indexRow;
     }
 
     private static Connection getConnectionForId(long scanId) throws HBaseAdapterException {
         Connection conn = clientPool.get(scanId);
         if (conn == null) {
             throw new HBaseAdapterException("getConnectionForId->No connection for scanId: " + scanId, null);
         }
         return conn;
     }
 
     public static void incrementRowCount(String tableName, long delta) throws HBaseAdapterException {
         try {
             client.incrementRowCount(tableName, delta);
         } catch (Exception e) {
             logger.error("incrementRowCount-> Exception: ", e);
             throw new HBaseAdapterException("incrementRowCount", e);
         }
     }
 
     public static void setRowCount(String tableName, long delta) throws HBaseAdapterException {
         try {
             client.setRowCount(tableName, delta);
         } catch (Exception e) {
             logger.error("setRowCount-> Exception: ", e);
             throw new HBaseAdapterException("setRowCount", e);
         }
     }
 
     public static long getRowCount(String tableName) throws HBaseAdapterException {
         try {
             return client.getRowCount(tableName);
         } catch (Exception e) {
             logger.error("getRowCount-> Exception: ", e);
             throw new HBaseAdapterException("getRowCount", e);
         }
     }
 
     public static void renameTable(String from, String to) throws HBaseAdapterException {
         try {
             client.renameTable(from, to);
         } catch (Exception e) {
             logger.error("renameTable-> Exception: ", e);
             throw new HBaseAdapterException("renameTable", e);
         }
     }
 
     public static boolean isNullable(String tableName, String columnName) throws HBaseAdapterException {
         boolean result = false;
         try {
             result = client.isNullable(tableName, columnName);
         } catch (Exception e) {
             logger.error("isNullable->Exception: ", e);
             throw new HBaseAdapterException("isNullable", e);
         }
 
         return result;
     }
 
     public static void addIndex(String tableName, String columnsToIndex) throws HBaseAdapterException {
         try {
             client.addIndex(tableName, columnsToIndex);
         } catch (Exception e) {
             logger.error("addIndex->Exception: ", e);
             throw new HBaseAdapterException("addIndex", e);
         }
     }
 
     public static void dropIndex(String tableName, String indexToDrop) throws HBaseAdapterException {
         try {
             client.dropIndex(tableName, indexToDrop);
         } catch (Exception e) {
             logger.error("addIndex->Exception: ", e);
             throw new HBaseAdapterException("dropIndex", e);
         }
     }
 }
