 package com.nearinfinity.honeycomb.hbaseclient;
 
 import com.google.common.collect.ImmutableMap;
 import org.apache.hadoop.hbase.util.Bytes;
 
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 public class ValueEncoder {
     private static final byte BYTE_MASK = (byte) 0x000000ff;
 
     private static final byte ASC_BYTE_MASK = (byte) 0x00000000;
 
     private static final long INVERT_SIGN_MASK = 0x8000000000000000L;
 
     public static Map<String, byte[]> correctAscendingValuePadding(final TableInfo info, final Map<String, byte[]> values) {
         return correctAscendingValuePadding(info, values, new HashSet<String>());
     }
 
     public static Map<String, byte[]> correctDescendingValuePadding(final TableInfo info, final Map<String, byte[]> values) {
         return correctDescendingValuePadding(info, values, new HashSet<String>());
     }
 
     public static Map<String, byte[]> correctAscendingValuePadding(final TableInfo info, final Map<String, byte[]> values, final Set<String> nullSearchColumns) {
         checkNotNull(info, "info");
         checkNotNull(nullSearchColumns, "nullSearchColumns");
         checkNotNull(values, "values");
         return convertToCorrectOrder(info, values, nullSearchColumns, new Function<byte[], ColumnType, Integer, byte[]>() {
             @Override
             public byte[] apply(byte[] value, ColumnType columnType, Integer padLength) {
                 return ascendingEncode(value, columnType, padLength);
             }
         });
     }
 
     public static Map<String, byte[]> correctDescendingValuePadding(final TableInfo info, final Map<String, byte[]> values, final Set<String> nullSearchColumns) {
         checkNotNull(info, "info");
         checkNotNull(nullSearchColumns, "nullSearchColumns");
         checkNotNull(values, "values");
         return convertToCorrectOrder(info, values, nullSearchColumns, new Function<byte[], ColumnType, Integer, byte[]>() {
             @Override
             public byte[] apply(byte[] value, ColumnType columnType, Integer padLength) {
                 return descendingEncode(value, columnType, padLength);
             }
         });
     }
 
     private static byte[] descendingEncode(final byte[] value, final ColumnType columnType, final int padLength) {
         checkParameters(value, columnType, padLength);
         final byte[] encodedValue = encodeValue(value, columnType);
         final byte[] reversedValue = reverseValue(encodedValue);
         return padValueDescending(reversedValue, padLength);
     }
 
     private static byte[] ascendingEncode(final byte[] value, final ColumnType columnType, final int padLength) {
         checkParameters(value, columnType, padLength);
         final byte[] encodedValue = encodeValue(value, columnType);
         return padValueAscending(encodedValue, padLength);
     }
 
     private static void checkParameters(byte[] value, ColumnType columnType, int padLength) {
         checkNotNull(value, "value");
         checkNotNull(columnType, "columnType");
         checkArgument(padLength >= 0, "padLength cannot be less than zero. Value: %s", padLength);
     }
 
     private static Map<String, byte[]> convertToCorrectOrder(final TableInfo info, final Map<String, byte[]> values, final Set<String> nullSearchColumns, Function<byte[], ColumnType, Integer, byte[]> convert) {
         final ImmutableMap.Builder<String, byte[]> result = ImmutableMap.builder();
         for (String columnName : values.keySet()) {
             final ColumnMetadata metadata = info.getColumnMetadata(columnName);
             final ColumnType columnType = info.getColumnTypeByName(columnName);
             byte[] value = values.get(columnName);
             final boolean isNull = value == null || nullSearchColumns.contains(columnName);
             if (isNull) {
                 value = new byte[metadata.getMaxLength()];
             }
 
             int padLength = 0;
             if (columnType == ColumnType.STRING || columnType == ColumnType.BINARY) {
                 final long maxLength = metadata.getMaxLength();
                 padLength = (int) Math.abs(maxLength - value.length);
             }
 
             final byte[] paddedValue = convert.apply(value, columnType, padLength);
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
 
     private static byte[] encodeValue(byte[] value, ColumnType columnType) {
         if (value.length == 0) {
             return new byte[0];
         }
 
         byte[] encodedValue;
         switch (columnType) {
            case LONG: {
                 long longValue = ByteBuffer.wrap(value).getLong();
                 encodedValue = encodeLong(longValue);
             }
             break;
             case DOUBLE: {
                 double doubleValue = ByteBuffer.wrap(value).getDouble();
                 encodedValue = encodeDouble(doubleValue);
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
 
     private static byte[] encodeLong(long value) {
         return Bytes.toBytes(value ^ INVERT_SIGN_MASK);
     }
 
     private static byte[] encodeDouble(double value) {
         long longValue = Double.doubleToLongBits(value);
         if (value < 0.0) {
             return Bytes.toBytes(~longValue);
         }
         return Bytes.toBytes(longValue ^ INVERT_SIGN_MASK);
     }
 
     private interface Function<F1, F2, F3, T> {
         T apply(F1 f1, F2 f2, F3 f3);
     }
 }
