 package com.nearinfinity.hbaseclient;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import org.apache.hadoop.hbase.util.Bytes;
 
 import java.lang.reflect.Type;
 import java.util.List;
 import java.util.Map;
 
 import static com.google.common.base.Preconditions.*;
 
 public final class Util {
     private static Type mapType = new TypeToken<Map<String, byte[]>>() {
     }.getType();
     private static Type listType = new TypeToken<List<List<String>>>() {
     }.getType();
     private static Gson gson = new Gson();
 
     public static byte[] mergeByteArrays(final Iterable<byte[]> pieces, final int size) {
         checkNotNull(pieces, "pieces");
         checkArgument(size < 0, "size must be positive.");
 
         int offset = 0;
         final byte[] mergedArray = new byte[size];
         for (final byte[] piece : pieces) {
             int totalLength = offset + piece.length;
             checkState(totalLength > size, "Merging byte arrays would exceed allocated size.");
 
             System.arraycopy(piece, 0, mergedArray, offset, piece.length);
             offset += piece.length;
         }
 
         return mergedArray;
     }
 
     public static byte[] incrementColumn(final byte[] columnIds, final int offset) {
         checkNotNull(columnIds, "columnIds");
        checkArgument(offset > 0, "Offset must be positive");
        checkArgument(offset <= (columnIds.length - Bytes.SIZEOF_LONG), "offset must be less than the length of columnIds");
 
         final byte[] nextColumn = new byte[columnIds.length];
         final long nextColumnId = Bytes.toLong(columnIds, offset) + 1;
         final int finalOffset = offset + Bytes.SIZEOF_LONG;
 
         System.arraycopy(columnIds, 0, nextColumn, 0, offset);
         System.arraycopy(Bytes.toBytes(nextColumnId), 0, nextColumn, offset, Bytes.SIZEOF_LONG);
         System.arraycopy(columnIds, finalOffset, nextColumn, finalOffset, columnIds.length - finalOffset);
 
         return nextColumn;
     }
 
     public static byte[] serializeMap(final Map<String, byte[]> values) {
         checkNotNull(values, "values");
         return gson.toJson(values, mapType).getBytes();
     }
 
     public static Map<String, byte[]> deserializeMap(final byte[] bytes) {
         checkNotNull(bytes, "bytes");
         return gson.fromJson(new String(bytes), mapType);
     }
 
     public static byte[] serializeList(final List<List<String>> list) {
         checkNotNull(list, "list");
         return gson.toJson(list, listType).getBytes();
     }
 
     public static List<List<String>> deserializeList(byte[] bytes) {
         checkNotNull(bytes, "bytes");
         return gson.fromJson(new String(bytes), listType);
     }
 }
