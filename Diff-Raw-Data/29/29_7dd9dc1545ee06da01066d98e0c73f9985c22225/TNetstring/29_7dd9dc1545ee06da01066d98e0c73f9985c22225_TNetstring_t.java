 /* Copyright 2011 Armando Singer
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.tnetstrings;
 
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  * A very fast tnetstring parser and dumper (serializers/deserializer). Parsing
  * produces no side affect garbage for all core tnetstring types (except for tnetstring
  * floating poing numbers). Each data element is parsed directly from the tnetstring
  * byte array range without converstion to intermediate String or temporary object holders.<br><br>
  * 
  * Supports the full tnetstrings spec as of 2011/3/16 (blobs, dicts, lists, integers,
  * floats, boolean and null).<br><br>
  * 
  * Maps preserve order (implementation is backed by LinkedHashMap). Tnetstring
  * blobs are returned as java byte[] arrays. Note that byte array keys in a Map are almost
  * useless in java--you can't really get a value by the byte[] array key because a byte array
  * uses the default identity based equality and hash from Object. You most likely
  * want the keys as Strings if you're getting data by keys. Otherwise, treat the map as a List
  * of pairs blobs in them--simply iterate over the map entries.<br><br>
  * 
  * Use the convenience methods {@link #parseWithBytesAsString(byte[], Charset)}
  * to parse the object graph with every occurance of byte[] converted to a String using
  * the specified charset. If you are expecting an object graph with both String and byte[]
  * data, you'll need to use {@link #parse(byte[])} and convert the the specific byte arrays
  * you want as Strings yourself.<br><br>
  * 
  * The convenience methods to convert byte[] to Strings are also optimized to prevent
  * the double copy that would occur if you first got the bytes then converted them to Strings.
  * 
  * @author Armando Singer (armando.singer at gmail dot com)
  */
 public final class TNetstring {
 
   private TNetstring() { }
 
   private static final Charset ASCII = Charset.forName("US-ASCII");
 
   /**
    * @return byte[] or Long or Double or Boolean or Map<byte[], Object> or List<Object> or null;
    *   Map values or List elements may be any of the previously listed types.
    *   Maps preserve order.
    */
   @SuppressWarnings("unchecked")
   public static <T> T parse(final byte[] msg) {
     return (T) parse(msg, 0, null);
   }
 
   /** Same as {@link #parse(byte[])} but starts from the specified offset index */
   @SuppressWarnings("unchecked")
   public static <T> T parse(final byte[] msg, int offset) {
     return (T) parse(msg, offset, null);
   }
 
   /**
    * Convenience method to parse with any occurance of byte[] as a Java String
    * and optimized to prevent double copy. String conversion is applied recursively to Map
    * values and list elements if they are byte[] types.
    * 
    * @return String or Long or Double or Boolean or Map<String, Object> or List<Object> or null;
    *   Map values or List elements may be any of the previously listed types.
    *   Maps preserve order.
    */
   @SuppressWarnings("unchecked") 
   public static <T> T parseWithBytesAsString(final byte[] msg, final Charset charset) {
     return (T) parse(msg, 0, charset);
   }
 
   /** Same as {@link #parseWithBytesAsString(byte[], Charset)} but starts from the specified offset index */
   @SuppressWarnings("unchecked")
   public static <T> T parseWithBytesAsString(final byte[] msg, int offset, final Charset charset) {
     return (T) parse(msg, offset, charset);
   }
 
   /** Internal parsing impl w/ an optimization if we want a String that prevents double copy */
   private static Object parse(final byte[] msg, final int offset, final Charset charset) {
     if (msg == null || msg.length < 3)
       throw new IllegalArgumentException("Nestring can't be null or < 3 length");
     final int i = dataIndex(msg, offset);
     final int size = parseSize(msg, offset, i - 1);
     if (size > msg.length)
       throw new IllegalArgumentException("Invalid tnetstring size. Can't be > msg size");
     final int typeIndex = i + size;
     switch (msg[typeIndex]) {
       case ',': return charset == null ? copyRange(msg, i, size) : parseString(msg, i, size, charset);
       case '}': return parseDict(msg, i, size, charset);
       case ']': return parseList(msg, i, size, charset);
       case '#': return parseLong(msg, i, typeIndex);
       case '^': return parseDouble(msg, i, typeIndex);
       case '!': return msg[i] == 't' && msg[i + 1] == 'r' && msg[i + 2] == 'u' && msg[i + 3] == 'e';
       case '~': if (size != 0) throw new IllegalArgumentException("Payload must be 0 length for null.");
         return null;
       default: throw new IllegalArgumentException(
         "Invalid payload type: " + msg[typeIndex] + " at index: " + typeIndex);
     }
   }
 
   /**
    * @return the parsed SIZE portion of a tnetstring SIZE:DATA,
    *   The integer is parsed directly from the bytes from the specified index,
    *   inclusive, to the specifed index, exclusive. Produces no garbage.
    * @throws NumberFormatException if the size bytes are not an ascii encoded
    *   integer that has no more than 9 digits
    */
   static int parseSize(final byte[] msg, final int from, final int to) {
     final int length = to - from;
     if (length <= 0) throw new IllegalArgumentException(from + " >= " + to);
     if (msg == null) throw new NumberFormatException("null");
     if (length > 9) throw new NumberFormatException("tnetstring size digits can't be > 9");
 
     int result = 0;
     for (int i = from; i < to; i++) {
       final byte digit = digitFrom(msg[i], msg, from, to);
       result *= 10;
       result += digit;
     }
     return result;
   }
 
   static int parseSize(final byte[] msg) {
     return parseSize(msg, 0, msg.length);
   }
 
   private static Map<Object, Object> parseDict(final byte[] msg, final int dataIndex,
     final int size, final Charset charset) {
     if (size == 0) return Collections.emptyMap();
     final Map<Object, Object> map = new LinkedHashMap<Object, Object>();
     final int limit = dataIndex + size;
     for (int keyIndex = dataIndex; keyIndex < limit; ) {
       final int keyDataIndex = dataIndex(msg, keyIndex);
       final int keySize = parseSize(msg, keyIndex, keyDataIndex - 1);
       final int valueIndex = keyDataIndex + keySize + 1;
       map.put(parse(msg, keyIndex, charset), parse(msg, valueIndex, charset));
       final int valueDataIndex = dataIndex(msg, valueIndex);
       final int valueSize = parseSize(msg, valueIndex, valueDataIndex - 1);
       keyIndex = valueDataIndex + valueSize + 1;
     }
     return Collections.unmodifiableMap(map);
   }
 
   private static List<Object> parseList(final byte[] msg, final int dataIndex,
     final int size, final Charset charset) {
     if (size == 0) return Collections.emptyList();
     final List<Object> list = new ArrayList<Object>();
     final int limit = dataIndex + size;
     for (int elementIndex = dataIndex; elementIndex < limit; ) {
       list.add(parse(msg, elementIndex, charset));
       final int elementSize = parseSize(msg, elementIndex, dataIndex(msg, elementIndex) - 1);
       elementIndex = dataIndex(msg, elementIndex) + elementSize + 1;
     }
     return Collections.unmodifiableList(list);
   }
 
   private static final int dataIndex(final byte[] msg, final int offset) {
     for (int i = offset; i < msg.length; i++)
       if (msg[i] == ':') return i + 1;
     throw new IllegalArgumentException("TNetstring does not have a ':' between offset "
       + offset + " and length " + msg.length);
   }
 
   private static byte[] copyRange(final byte[] msg, final int offset, final int size) {
     final byte[] copy = new byte[size];
     System.arraycopy(msg, offset, copy, 0, Math.min(msg.length - offset, size));
     return copy;
   }
 
   private static final long LONG_MULTMIN = Long.MIN_VALUE / 10;
   private static final long LONG_NEG_MULTMAX = -Long.MAX_VALUE / 10;
 
   /** Parse a long from a byte range. Produces no garbage. */
   static long parseLong(final byte[] msg, final int from, final int to) {
     if (msg == null) throw new NumberFormatException("null");
 
     final long limit;
     final boolean negative;
     int i = from;
     if (msg[i] == '-') {
       negative = true;
       limit = Long.MIN_VALUE;
       i++;
     } else {
       negative = false;
       limit = -Long.MAX_VALUE;
     }
     byte digit;
     long result = 0;
     if (i < to) {
       digit = digitFrom(msg[i++], msg, from, to);
       result = -digit;
     }
     final long multmin = negative ? LONG_MULTMIN : LONG_NEG_MULTMAX;
     while (i < to) {
       digit = digitFrom(msg[i++], msg, from, to);
       if (result < multmin) throw badNumberFormat(msg, from, to);
       result *= 10;
       if (result < limit + digit) throw badNumberFormat(msg, from, to);
       result -= digit;
     }
 
     if (negative) {
       if (i > 1) return result;
       throw badNumberFormat(msg, from, to);
     }
     return -result;
   }
 
   static double parseDouble(final byte[] msg, final int from, final int to) {
     return Double.parseDouble(parseAscii(msg, from, to - from));
   }
 
   static byte digitFrom(byte ascii, byte[] msg, int from, int to) {
     switch (ascii) {
       case '1': return 1;
       case '2': return 2;
       case '3': return 3;
       case '4': return 4;
       case '5': return 5;
       case '6': return 6;
       case '7': return 7;
       case '8': return 8;
       case '9': return 9;
       case '0': return 0;
       default: throw badNumberFormat(msg, from, to);
     }
   }
 
   private static NumberFormatException badNumberFormat(byte[] asciiNum, int from, int to) {
     return new NumberFormatException("For input: '" + parseString(asciiNum, from, to - from, ASCII) + '\'');
   }
 
   /** Parse String with fast & minimum possible garbage path for ASCII */
   static String parseString(final byte[] msg, final int from, final int size, final Charset charset) {
     if(!ASCII.equals(charset)) {
       try {
        // using charset.name() instead of Charset overload because the former is faster
         return new String(msg, from, size, charset.name());
       } catch (final UnsupportedEncodingException e) {
         throw new IllegalArgumentException(e);
       }
     }
     return parseAscii(msg, from, size);
   }
 
   private static String parseAscii(final byte[] msg, final int from, final int size) {
     // ascii fast path. ~3x faster than both overloads of new String(msg, from, size, US_ASCII);
     final char[] result = new char[size];
     for (int i = 0; i < size; i++) {
       final byte b = msg[from + i];
       result[i] = b < 0 ? '?' : (char) b;
     }
     return String.valueOf(result);
   }
 
   
   /// start code for dumping
   private static final byte REPLACEMENT = '?';
 
   static byte[] getBytes(CharSequence s, Charset charset) {
     if(!ASCII.equals(charset)) {
       try {
        // using charset.name() instead of Charset overload because the former is faster
         return String.valueOf(s).getBytes(charset.name());
       } catch (final UnsupportedEncodingException e) {
         throw new IllegalArgumentException(e);
       }
     }
     return asciiBytes(s);
   }
 
   /** ~2x faster than both overloads of string.getBytes(US_ASCII) */
   static byte[] asciiBytes(CharSequence s) {
     final int size = s.length();
     final byte[] result = new byte[size];
     for (int i = 0; i < size; i++) {
       final char c = s.charAt(i);
       result[i] = c > 127 ? REPLACEMENT : (byte) c;
     }
     return result;
   }
 
   private static final byte[] COMMA_BYTES = new byte[] { ',' };
 
   public static byte[] dump(final byte[] data) {
     return concat(asciiBytes(data.length + ":"), data, COMMA_BYTES);
   }
 
   public static byte[] dump(final CharSequence data, final Charset charset) {
    final byte[] dataBytes = getBytes(data, charset);
    return concat(asciiBytes(dataBytes.length + ":"), dataBytes, COMMA_BYTES);
   }
 
   private static final byte[] TRUE_BYTES = asciiBytes("4:true!");
   private static final byte[] FALSE_BYTES = asciiBytes("5:false!");
 
   public static byte[] dump(final boolean data) {
     return data ? TRUE_BYTES : FALSE_BYTES;
   }
   
   public static byte[] dump(final boolean[] data) {
     if (data == null) return NULL_BYTES;
     final int length = data.length;
     final byte[][] result = new byte[length][];
     int totalSize = 0;
     for (int i = 0; i < length; i++) {
       final byte[] bytes = dump(data[i]);
       totalSize += bytes.length;
       result[i] = bytes;
     }
     return concat(asciiBytes(totalSize + ":"), concat(result), RIGHT_SQUARE_BRACE_BYTES);  
   }
 
   private static final byte[] POUND_BYTES = new byte[] { '#' };
 
   public static byte[] dump(final long data) {
     return numberBytes(Long.toString(data), POUND_BYTES);
   }
 
   public static byte[] dump(final long[] data) {
     if (data == null) return NULL_BYTES;
     final int length = data.length;
     final byte[][] result = new byte[length][];
     int totalSize = 0;
     for (int i = 0; i < length; i++) {
       final byte[] bytes = dump(data[i]);
       totalSize += bytes.length;
       result[i] = bytes;
     }
     return concat(asciiBytes(totalSize + ":"), concat(result), RIGHT_SQUARE_BRACE_BYTES);  
   }
 
   public static byte[] dump(final int[] data) {
     if (data == null) return NULL_BYTES;
     final int length = data.length;
     final byte[][] result = new byte[length][];
     int totalSize = 0;
     for (int i = 0; i < length; i++) {
       final byte[] bytes = dump(data[i]);
       totalSize += bytes.length;
       result[i] = bytes;
     }
     return concat(asciiBytes(totalSize + ":"), concat(result), RIGHT_SQUARE_BRACE_BYTES);  
   }
   
   public static byte[] dump(final short[] data) {
     if (data == null) return NULL_BYTES;
     final int length = data.length;
     final byte[][] result = new byte[length][];
     int totalSize = 0;
     for (int i = 0; i < length; i++) {
       final byte[] bytes = dump(data[i]);
       totalSize += bytes.length;
       result[i] = bytes;
     }
     return concat(asciiBytes(totalSize + ":"), concat(result), RIGHT_SQUARE_BRACE_BYTES);  
   }
 
   /* Dumps byte array as a list of integers rather than as a binary blob */
   public static byte[] dumpIntegers(final byte[] data) {
     if (data == null) return NULL_BYTES;
     final int length = data.length;
     final byte[][] result = new byte[length][];
     int totalSize = 0;
     for (int i = 0; i < length; i++) {
       final byte[] bytes = dump(data[i]);
       totalSize += bytes.length;
       result[i] = bytes;
     }
     return concat(asciiBytes(totalSize + ":"), concat(result), RIGHT_SQUARE_BRACE_BYTES);  
   }
 
   private static final byte[] CARROT_BYTES = new byte[] { '^' };
 
   private static final DecimalFormat DECIMAL_FORMAT;
   static {
     final DecimalFormatSymbols dotSeparator = new DecimalFormatSymbols();
     dotSeparator.setDecimalSeparator('.');
     DECIMAL_FORMAT = new DecimalFormat("0.0", dotSeparator);
     DECIMAL_FORMAT.setDecimalSeparatorAlwaysShown(true);
     DECIMAL_FORMAT.setMinimumFractionDigits(1);
     DECIMAL_FORMAT.setMaximumFractionDigits(340);
     DECIMAL_FORMAT.setMinimumIntegerDigits(1);
     DECIMAL_FORMAT.setGroupingUsed(false);
   }
 
   public static byte[] dump(final double data) {
     return numberBytes(((DecimalFormat) DECIMAL_FORMAT.clone()).format(data), CARROT_BYTES);
   }
 
   public static byte[] dump(final float[] data) {
     if (data == null) return NULL_BYTES;
     final int length = data.length;
     final byte[][] result = new byte[length][];
     int totalSize = 0;
     for (int i = 0; i < length; i++) {
       final byte[] bytes = dump(data[i]);
       totalSize += bytes.length;
       result[i] = bytes;
     }
     return concat(asciiBytes(totalSize + ":"), concat(result), RIGHT_SQUARE_BRACE_BYTES);  
   }
 
   public static byte[] dump(final double[] data) {
     if (data == null) return NULL_BYTES;
     final int length = data.length;
     final byte[][] result = new byte[length][];
     int totalSize = 0;
     for (int i = 0; i < length; i++) {
       final byte[] bytes = dump(data[i]);
       totalSize += bytes.length;
       result[i] = bytes;
     }
     return concat(asciiBytes(totalSize + ":"), concat(result), RIGHT_SQUARE_BRACE_BYTES);  
   }
 
   public static byte[] dump(final char data, final Charset charset) {
    final byte[] dataBytes = getBytes(String.valueOf(data), charset);
    return concat(asciiBytes(dataBytes.length + ":"), dataBytes, COMMA_BYTES);
   }
 
  /* Dumps a tnetstring array of strings */
   public static byte[] dump(final char[] data, final Charset charset) {
     if (data == null) return NULL_BYTES;
     final int length = data.length;
     final byte[][] result = new byte[length][];
     int totalSize = 0;
     for (int i = 0; i < length; i++) {
       final byte[] bytes = dump(data[i], charset);
       totalSize += bytes.length;
       result[i] = bytes;
     }
     return concat(asciiBytes(totalSize + ":"), concat(result), RIGHT_SQUARE_BRACE_BYTES);  
   }
   
   private static final byte[] RIGHT_SQUARE_BRACE_BYTES = new byte[] { ']' };
 
   public static byte[] dump(final List<?> data) {
     return dumpList(data, null);
   }
   
   public static byte[] dump(final List<? extends CharSequence> data, Charset charset) {
     return dumpList(data, charset);
   }
 
   private static byte[] dumpList(final List<?> data, Charset charset) {
     final byte[][] result = new byte[data.size()][];
     int totalSize = 0;
     for (int i = 0; i < result.length; i++) {
       final byte[] bytes = dump(data.get(i), charset);
       totalSize += bytes.length;
       result[i] = bytes;
     }
     return concat(asciiBytes(totalSize + ":"), concat(result), RIGHT_SQUARE_BRACE_BYTES);
   }
 
   public static byte[] dump(final Object[] data) {
     return dumpArray(data, null);
   }
   
   private static byte[] dumpArray(final Object[] data, Charset c) {
     final byte[][] result = new byte[data.length][];
     int totalSize = 0;
     for (int i = 0; i < result.length; i++) {
       final byte[] bytes = dump(data[i], c);
       totalSize += bytes.length;
       result[i] = bytes;
     }
     return concat(asciiBytes(totalSize + ":"), concat(result), RIGHT_SQUARE_BRACE_BYTES);
   }
 
   public static byte[] dump(final Iterable<?> data) {
     return dumpIterable(data, null);
   }
 
   public static byte[] dump(final Iterable<? extends CharSequence> data, Charset charset) {
     return dumpIterable(data, charset);
   }
 
   private static byte[] dumpIterable(final Iterable<?> data, Charset charset) {
     if (data instanceof List) return dump((List<?>) data);
     final List<Object> result = new ArrayList<Object>();
     for (final Object o : data) {
       result.add(o);
     }
     return dump(result, charset);
   }
 
   private static final byte[] RIGHT_CURLY_BRACE_BYTES = new byte[] { '}' };
 
   public static byte[] dump(final Map<? extends CharSequence, ?> data, Charset forStrings) {
     return dumpMap(data, forStrings);
   }
 
   public static byte[] dumpByteMap(final Map<byte[], ?> data, Charset forMapStringValues) {
     return dumpMap(data, forMapStringValues);
   }
 
   public static byte[] dumpByteMap(final Map<byte[], ?> data) {
     return dumpMap(data, null);
   }
 
   private static byte[] dumpMap(final Map<?, ?> data, Charset forStrings) {
     final byte[][] result = new byte[data.size() * 2][];
     int totalSize = 0;
     int i = 0;
     for (final Entry<?, ?> entry : data.entrySet()) {
       final byte[] keyBytes = dump(entry.getKey(), forStrings);
       final byte[] valueBytes = dump(entry.getValue(), forStrings);
       result[i] = keyBytes;
       result[i + 1] = valueBytes;
       totalSize += keyBytes.length + valueBytes.length;
       i += 2;
     }
     return concat(asciiBytes(totalSize + ":"), concat(result),  RIGHT_CURLY_BRACE_BYTES);
   }
 
   private static byte[] numberBytes(final String number, final byte[] numType) {
     // string number representation is always single byte values so we just use .length()
     return concat(asciiBytes(number.length() + ":"), asciiBytes(number), numType);
   }
 
   private static final byte[] NULL_BYTES = asciiBytes("0:~");
 
   public static byte[] dump(final Object data) {
     return dump(data, null);
   }
 
   public static byte[] dump(final Object data, Charset charsetForStrings) {
     if (data instanceof byte[]) return dump((byte[]) data);
     else if (data instanceof CharSequence) {
       if (charsetForStrings == null)
         throw new IllegalArgumentException("Can't serialize a String without a charset supplied.");
       return dump((CharSequence) data, charsetForStrings);
     }
     else if (data instanceof Map) return dumpMap((Map<?, ?>) data, charsetForStrings);
     else if (data instanceof Double || data instanceof Float) return dump(((Number) data).doubleValue());
     else if (data instanceof Number) return dump(((Number) data).longValue());
     else if (data instanceof Boolean) return dump(((Boolean) data).equals(true));
     else if (data == null) return NULL_BYTES;
     else if (data instanceof List) return dumpList((List<?>) data, charsetForStrings);
     else if (data instanceof Object[]) return dumpArray((Object[]) data, charsetForStrings);
     else if (data instanceof Iterable) return dumpIterable((Iterable<?>) data, charsetForStrings);
     else if (data instanceof long[]) return dump((long[]) data);
     else if (data instanceof int[]) return dump((int[]) data);
     else if (data instanceof short[]) return dump((short[]) data);
     else if (data instanceof double[]) return dump((double[]) data);
     else if (data instanceof float[]) return dump((float[]) data);
     else if (data instanceof char[]) return dump((char[]) data, charsetForStrings);
     else if (data instanceof boolean[]) return dump((boolean[]) data);
     throw new IllegalArgumentException("Can't serialize a " + data.getClass().getName());
   }
 
   private static byte[] concat(final byte[]... arrays) {
     int length = 0;
     for (final byte[] array : arrays) length += array.length;
     final byte[] result = new byte[length];
     int pos = 0;
     for (final byte[] array : arrays) {
       System.arraycopy(array, 0, result, pos, array.length);
       pos += array.length;
     }
     return result;
   }
 
 }
