 package com.nearinfinity.mysqlengine;
 
 import java.nio.ByteBuffer;
 import java.util.UUID;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jedstrom
  * Date: 8/9/12
  * Time: 9:43 AM
  * To change this template use File | Settings | File Templates.
  */
 public class RowKeyFactory {
     public static final byte[] ROOT = ByteBuffer.allocate(7)
                                             .put(RowType.TABLES.getValue())
                                             .put("TABLES".getBytes())
                                             .array();
 
     private static final byte BYTE_MASK = (byte) 0x000000ff;
 
     public static byte[] buildColumnsKey(long tableId) {
         return ByteBuffer.allocate(9)
                 .put(RowType.COLUMNS.getValue())
                 .putLong(tableId)
                 .array();
     }
 
     public static byte[] buildDataKey(long tableId, UUID uuid) {
         return ByteBuffer.allocate(25)
                 .put(RowType.DATA.getValue())
                 .putLong(tableId)
                 .putLong(uuid.getMostSignificantBits())
                 .putLong(uuid.getLeastSignificantBits())
                 .array();
     }
 
     public static byte[] buildValueIndexKey(long tableId, long columnId, byte[] value, UUID uuid) {
         return ByteBuffer.allocate(33 + value.length)
                 .put(RowType.VALUE_INDEX.getValue())
                 .putLong(tableId)
                 .putLong(columnId)
                 .put(value)
                 .putLong(uuid.getMostSignificantBits())
                 .putLong(uuid.getLeastSignificantBits())
                 .array();
     }
 
     public static byte[] buildSecondaryIndexKey(long tableId, long columnId, byte[] value) {
         return ByteBuffer.allocate(17 + value.length)
                 .put(RowType.SECONDARY_INDEX.getValue())
                 .putLong(tableId)
                 .putLong(columnId)
                 .put(value)
                 .array();
     }
 
     public static byte[] buildReverseIndexKey(long tableId, long columnId, byte[] value) {
         byte[] reverseValue = reverseValue(value);
         return ByteBuffer.allocate(17 + value.length)
                 .put(RowType.REVERSE_INDEX.getValue())
                 .putLong(tableId)
                 .putLong(columnId)
                .put(value)
                 .array();
     }
 
     public static byte[] parseValueFromReverseIndexKey(byte[] reverseIndexKey) {
         ByteBuffer buffer = ByteBuffer.wrap(reverseIndexKey, 17, reverseIndexKey.length - 17);
         byte[] actualValue = reverseValue(buffer.array());
         return actualValue;
     }
 
     private static byte[] reverseValue(byte[] value) {
         ByteBuffer buffer = ByteBuffer.allocate(value.length);
 
         for (int i = 0 ; i < value.length ; i++) {
             buffer.put((byte) (BYTE_MASK ^ value[i]));
         }
 
         return buffer.array();
     }
 
     public static byte[] buildNullIndexKey(long tableId, long columnId, UUID uuid) {
         return ByteBuffer.allocate(33)
                 .put(RowType.NULL_INDEX.getValue())
                 .putLong(tableId)
                 .putLong(columnId)
                 .putLong(uuid.getMostSignificantBits())
                 .putLong(uuid.getLeastSignificantBits())
                 .array();
     }
 }
