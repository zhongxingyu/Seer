 package com.nearinfinity.honeycomb.hbaseclient;
 
 import com.google.common.collect.Maps;
 import org.apache.hadoop.hbase.TableNotFoundException;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.HTableInterface;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.util.Bytes;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.Map;
 import java.util.NavigableMap;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Preconditions.checkState;
 import static java.lang.String.format;
 
 public class TableCache {
     private static final ConcurrentHashMap<String, TableInfo> tableCache = new ConcurrentHashMap<String, TableInfo>();
 
     private static final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
 
     public static TableInfo getTableInfo(String tableName, HTableInterface table) throws IOException {
         checkNotNull(tableName);
         cacheLock.readLock().lock();
         try {
             TableInfo tableInfo = tableCache.get(tableName);
             if (tableInfo != null) {
                 return tableInfo;
             }
         } finally {
             cacheLock.readLock().unlock();
         }
 
         cacheLock.writeLock().lock();
         try {
            TableInfo tableInfo = tableCache.get(tableName); // Did the table cache get updated before entering the write lock?
            if (tableInfo != null) {
                return tableInfo;
            }

             return TableCache.refreshCache(tableName, table, tableCache);
         } finally {
             cacheLock.writeLock().unlock();
         }
     }
 
     public static void put(String tableName, TableInfo info) {
         tableCache.put(tableName, info);
     }
 
     public static TableInfo get(String tableName) {
         return tableCache.get(tableName);
     }
 
     public static void swap(String from, String to, TableInfo info) {
         cacheLock.writeLock().lock();
         try {
             tableCache.remove(from);
             tableCache.put(to, info);
         } finally {
             cacheLock.writeLock().unlock();
         }
     }
 
     public static TableInfo refreshCache(String tableName, HTableInterface table, ConcurrentHashMap<String, TableInfo> tableCache) throws IOException {
         if (table == null) {
             throw new IllegalStateException(format("Table %s was null. Cannot get table information from null table.", tableName));
         }
 
         String htableName = format("HTable used \"%s\"", Bytes.toString(table.getTableName()));
         Get tableIdGet = new Get(RowKeyFactory.ROOT);
         Result result = table.get(tableIdGet);
         String tableNotFoundMessage = format("SQL table \"%s\" was not found. %s", tableName, htableName);
         if (result.isEmpty()) {
             throw new TableNotFoundException(tableNotFoundMessage);
         }
 
         byte[] sqlTableBytes = result.getValue(Constants.NIC, tableName.getBytes());
         if (sqlTableBytes == null) {
             throw new TableNotFoundException(tableNotFoundMessage);
         }
 
         long tableId = ByteBuffer.wrap(sqlTableBytes).getLong();
 
         checkState(tableId >= 0, "Table id %d retrieved from HBase was not valid.", tableId);
 
         TableInfo info = new TableInfo(tableName, tableId);
 
         byte[] rowKey = RowKeyFactory.buildColumnsKey(tableId);
 
         Get columnsGet = new Get(rowKey);
         Result columnsResult = table.get(columnsGet);
         checkNotNull(columnsResult);
         if (columnsResult.isEmpty()) {
             throw new IllegalStateException("Column result from the get was empty for row key " + Bytes.toStringBinary(rowKey));
         }
 
         Map<byte[], byte[]> columns = columnsResult.getFamilyMap(Constants.NIC);
         if (columns == null) {
             throw new NullPointerException("Columns were null after getting family map.");
         }
 
         for (byte[] qualifier : columns.keySet()) {
             String columnName = new String(qualifier);
             long columnId = ByteBuffer.wrap(columns.get(qualifier)).getLong();
             info.addColumn(columnName, columnId, getMetadataForColumn(tableId, columnId, table));
         }
 
         rowKey = RowKeyFactory.buildTableInfoKey(tableId);
         Result tableMetadata = table.get(new Get(rowKey));
         NavigableMap<byte[], byte[]> familyMap = tableMetadata.getFamilyMap(Constants.NIC);
        if (familyMap.isEmpty()) {
            throw new IllegalStateException(format("SQL Table \"%s\" is missing metadata.", tableName));
        }

         Map<String, byte[]> stringFamily = Maps.newHashMap();
         for (Map.Entry<byte[], byte[]> entry : familyMap.entrySet()) {
             stringFamily.put(new String(entry.getKey()), entry.getValue());
         }
         info.setTableMetadata(stringFamily);
 
         tableCache.put(tableName, info);
 
         return info;
     }
 
     private static ColumnMetadata getMetadataForColumn(long tableId, long columnId, HTableInterface table) throws IOException {
         Get metadataGet = new Get(RowKeyFactory.buildColumnInfoKey(tableId, columnId));
         Result result = table.get(metadataGet);
 
         byte[] jsonBytes = result.getValue(Constants.NIC, Constants.METADATA);
         return new ColumnMetadata(jsonBytes);
     }
 }
