 package com.nearinfinity.hbaseclient;
 
 import com.google.common.base.Function;
 import org.apache.hadoop.hbase.util.Bytes;
 
 import java.util.*;
 
 import static com.google.common.base.Preconditions.checkState;
 import static java.lang.String.format;
 
 public class Index {
     public static byte[] createColumnIds(final Iterable<String> columns, final Map<String, Long> columnNameToId) {
         return correctColumnIdSize(convertToByteArray(columns, new Function<String, byte[]>() {
             @Override
             public byte[] apply(String column) {
                 return Bytes.toBytes(columnNameToId.get(column));
             }
         }));
     }
 
     public static byte[] createValues(final Iterable<String> columns, final Map<String, byte[]> values) {
         return convertToByteArray(columns, new Function<String, byte[]>() {
             @Override
             public byte[] apply(String column) {
                 return values.get(column);
             }
         });
     }
 
     public static int calculateIndexValuesFullLength(final Iterable<String> columns, final Map<String, Integer> columnLengthMap) {
         int size = 0;
         for (String column : columns) {
             size += columnLengthMap.get(column);
         }
 
         return size;
     }
 
     private static byte[] correctColumnIdSize(final byte[] columnIds) {
         int expectedSize = Constants.KEY_PART_COUNT * Bytes.SIZEOF_LONG;
        checkState(columnIds.length < expectedSize, format("There should never be more than %d columns indexed. Found %d columns.", expectedSize / Bytes.SIZEOF_LONG, columnIds.length / Bytes.SIZEOF_LONG));
 
         if (columnIds.length == expectedSize) {
             return columnIds;
         }
 
         return Bytes.padTail(columnIds, expectedSize - columnIds.length);
     }
 
     private static byte[] convertToByteArray(final Iterable<String> columns,
                                              final Function<String, byte[]> conversion) {
         List<byte[]> pieces = new LinkedList<byte[]>();
         int size = 0;
         for (final String column : columns) {
             byte[] bytes = conversion.apply(column);
             if (bytes != null) {
                 size += bytes.length;
                 pieces.add(bytes);
             }
         }
 
         return Util.mergeByteArrays(pieces, size);
     }
 
     public static List<List<String>> indexForTable(final Map<byte[], byte[]> tableMetadata) {
         byte[] jsonBytes = null;
         for (Map.Entry<byte[], byte[]> entry : tableMetadata.entrySet()) {
             if (Arrays.equals(entry.getKey(), Constants.INDEXES)) {
                 jsonBytes = entry.getValue();
             }
         }
 
         if (jsonBytes == null) {
             return new LinkedList<List<String>>();
         }
 
         return Util.deserializeList(jsonBytes);
     }
 
     public static byte[] createReverseIndex(long tableId, UUID rowId, Map<String, byte[]> descendingValues, List<String> columns, byte[] columnIds) {
         final byte[] descendingIndexValues = createValues(columns, descendingValues);
         return RowKeyFactory.buildReverseIndexRowKey(tableId, columnIds, descendingIndexValues, rowId);
     }
 
     public static byte[] createPrimaryIndex(long tableId, UUID rowId, Map<String, byte[]> ascendingValues, List<String> columns, byte[] columnIds) {
         final byte[] ascendingIndexValues = createValues(columns, ascendingValues);
         return RowKeyFactory.buildIndexRowKey(tableId, columnIds, ascendingIndexValues, rowId);
     }
 }
