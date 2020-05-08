 package com.nearinfinity.hbaseclient;
 
 import org.apache.hadoop.hbase.client.Delete;
 import org.apache.hadoop.hbase.client.Result;
 
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
 
 public class DeleteListFactory {
     public static List<Delete> createDeleteRowList(UUID uuid, TableInfo info, Result result, byte[] dataRowKey, final List<List<String>> indexedKeys) {
         List<Delete> deleteList = new LinkedList<Delete>();
         deleteList.add(new Delete(dataRowKey));
 
         Map<String, byte[]> values = ResultParser.parseDataRow(result, info);
        for (String columnName : info.getColumnNames()) {
             ColumnMetadata metadata = info.getColumnMetadata(columnName);
             if (!values.containsKey(columnName)) {
                 values.put(columnName, new byte[metadata.getMaxLength()]);
             }
         }
 
         deleteList.addAll(createDeleteForIndex(values, info, indexedKeys, uuid));
 
         return deleteList;
     }
 
     public static List<Delete> createDeleteForIndex(Map<String, byte[]> values, TableInfo info, UUID uuid, List<String> indexedKeys) {
         List<List<String>> newIndexColumns = new LinkedList<List<String>>();
         newIndexColumns.add(new LinkedList<String>(indexedKeys));
         return createDeleteForIndex(values, info, newIndexColumns, uuid);
     }
 
     public static List<Delete> createDeleteForIndex(Map<String, byte[]> values, TableInfo info, List<List<String>> indexedKeys, UUID uuid) {
         final long tableId = info.getId();
         List<Delete> deleteList = new LinkedList<Delete>();
         final Map<String, Long> columnNameToId = info.columnNameToIdMap();
 
         final Map<String, byte[]> ascendingValues = ValueEncoder.correctAscendingValuePadding(info, values);
         final Map<String, byte[]> descendingValues = ValueEncoder.correctDescendingValuePadding(info, values);
 
         for (List<String> columns : indexedKeys) {
             final byte[] columnIds = Index.createColumnIds(columns, columnNameToId);
 
             final byte[] ascendingIndexKey = Index.createPrimaryIndex(tableId, uuid, ascendingValues, columns, columnIds);
             final byte[] descendingIndexKey = Index.createReverseIndex(tableId, uuid, descendingValues, columns, columnIds);
 
             deleteList.add(new Delete(ascendingIndexKey));
             deleteList.add(new Delete(descendingIndexKey));
         }
 
         return deleteList;
     }
 }
