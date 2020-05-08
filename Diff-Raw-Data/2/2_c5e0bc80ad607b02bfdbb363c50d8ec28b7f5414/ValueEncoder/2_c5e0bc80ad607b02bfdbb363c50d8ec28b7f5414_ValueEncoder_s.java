 package com.nearinfinity.hbaseclient;
 
 import com.google.common.collect.ImmutableMap;
 import org.apache.hadoop.hbase.util.Bytes;
 
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 public class ValueEncoder {
     private static final byte BYTE_MASK = (byte) 0x000000ff;
 
     private static final byte ASC_BYTE_MASK = (byte) 0x00000000;
 
     private static final byte NEGATIVE_MASK = (byte) 0x00000080;
 
     private static final long INVERT_SIGN_MASK = 0x8000000000000000L;
 
     private static final long INVERT_ALL_BITS_MASK = 0xFFFFFFFFFFFFFFFFL;
 
     public static byte[] descendingEncode(final byte[] value, final ColumnType columnType, final int padLength) {
         final byte[] encodedValue = ValueEncoder.encodeValue(value, columnType);
         final byte[] reversedValue = ValueEncoder.reverseValue(encodedValue);
         final byte[] paddedValue = ValueEncoder.padValueDescending(reversedValue, padLength);
         return paddedValue;
     }
 
     public static byte[] ascendingEncode(final byte[] value, final ColumnType columnType, final int padLength) {
         final byte[] encodedValue = ValueEncoder.encodeValue(value, columnType);
         return ValueEncoder.padValueAscending(encodedValue, padLength);
     }
 
     private static byte[] encodeValue(byte[] value, ColumnType columnType) {
         if (value == null || value.length == 0) {
             return new byte[0];
         }
 
         byte[] encodedValue;
         switch (columnType) {
             case LONG: {
                 long longValue = ByteBuffer.wrap(value).getLong();
                 encodedValue = positionOfLong(longValue);
             }
             break;
             case DOUBLE: {
                 double doubleValue = ByteBuffer.wrap(value).getDouble();
                 encodedValue = positionOfDouble(doubleValue);
             }
             break;
             default:
                 encodedValue = value;
         }
 
         return encodedValue;
     }
 
     private static byte[] padValueDescending(byte[] value, int padLength) {
         return padValue(value, padLength, BYTE_MASK);
     }
 
     private static byte[] padValueAscending(byte[] value, int padLength) {
         return padValue(value, padLength, ASC_BYTE_MASK);
     }
 
     private static byte[] reverseValue(byte[] value) {
         ByteBuffer buffer = ByteBuffer.allocate(value.length);
 
         for (byte aValue : value) {
             buffer.put((byte) (~aValue));
         }
 
         return buffer.array();
     }
 
     private static byte[] padValue(byte[] value, int padLength, byte mask) {
         byte[] paddedValue = new byte[value.length + padLength];
         Arrays.fill(paddedValue, mask);
         System.arraycopy(value, 0, paddedValue, 0, value.length);
 
         return paddedValue;
     }
 
     private static byte[] positionOfLong(long value) {
         return ByteBuffer.allocate(8).putLong(value ^ INVERT_SIGN_MASK).array();
     }
 
     private static byte[] positionOfDouble(double value) {
         long longValue = Double.doubleToLongBits(value);
         if (isNegative(value)) {
             return ByteBuffer.allocate(8).putLong(longValue ^ INVERT_ALL_BITS_MASK).array();
         }
         return ByteBuffer.allocate(8).putLong(longValue ^ INVERT_SIGN_MASK).array();
     }
 
     private static boolean isNegative(double value) {
         byte[] bytes = ByteBuffer.allocate(8).putDouble(value).array();
         return (bytes[0] & NEGATIVE_MASK) != 0;
     }
 
     public static Map<String, byte[]> correctAscendingValuePadding(final TableInfo info, final Map<String, byte[]> values) {
         return correctAscendingValuePadding(info, values, new HashSet<String>());
     }
 
     public static Map<String, byte[]> correctDescendingValuePadding(final TableInfo info, final Map<String, byte[]> values) {
         return correctDescendingValuePadding(info, values, new HashSet<String>());
     }
 
     public static Map<String, byte[]> correctAscendingValuePadding(final TableInfo info, final Map<String, byte[]> values, final Set<String> nullSearchColumns) {
         return convertToCorrectOrder(info, values, nullSearchColumns, new Function<byte[], ColumnType, Integer, byte[]>() {
             @Override
             public byte[] apply(byte[] value, ColumnType columnType, Integer padLength) {
                 return ascendingEncode(value, columnType, padLength);
             }
         });
     }
 
     public static Map<String, byte[]> correctDescendingValuePadding(final TableInfo info, final Map<String, byte[]> values, final Set<String> nullSearchColumns) {
         return convertToCorrectOrder(info, values, nullSearchColumns, new Function<byte[], ColumnType, Integer, byte[]>() {
             @Override
             public byte[] apply(byte[] value, ColumnType columnType, Integer padLength) {
                 return descendingEncode(value, columnType, padLength);
             }
         });
     }
 
     private static Map<String, byte[]> convertToCorrectOrder(final TableInfo info, final Map<String, byte[]> values, final Set<String> nullSearchColumns, Function<byte[], ColumnType, Integer, byte[]> convert) {
         ImmutableMap.Builder<String, byte[]> result = ImmutableMap.builder();
         for (String columnName : values.keySet()) {
             final ColumnMetadata metadata = info.getColumnMetadata(columnName);
             final ColumnType columnType = info.getColumnTypeByName(columnName);
             byte[] value = values.get(columnName);
             boolean isNull = value == null || nullSearchColumns.contains(columnName);
             if (isNull) {
                 value = new byte[metadata.getMaxLength()];
             }
 
             int padLength = 0;
             if (columnType == ColumnType.STRING || columnType == ColumnType.BINARY) {
                 final long maxLength = metadata.getMaxLength();
                padLength = (int) maxLength - value.length;
             }
 
             byte[] paddedValue = convert.apply(value, columnType, padLength);
             if (metadata.isNullable()) {
                 byte[] nullPadValue = Bytes.padHead(paddedValue, 1);
                 nullPadValue[0] = isNull ? (byte) 1 : 0;
                 result.put(columnName, nullPadValue);
             } else {
                 result.put(columnName, paddedValue);
             }
         }
 
         return result.build();
     }
 
     private interface Function<F1, F2, F3, T> {
         T apply(F1 f1, F2 f2, F3 f3);
     }
 }
