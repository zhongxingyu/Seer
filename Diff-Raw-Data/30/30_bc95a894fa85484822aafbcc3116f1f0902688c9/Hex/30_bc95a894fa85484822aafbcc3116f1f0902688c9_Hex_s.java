 package com.yasminapp.client;
 
 public class Hex {
   protected static final char[] ENCODING_TABLE = {
       '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
   protected static final byte[] DECODING_TABLE = new byte[128];
 
   /**
    * Initialize the decoding table.
    */
   protected static void initialiseDecodingTable() {
     for (int i = 0; i < ENCODING_TABLE.length; i++) {
       DECODING_TABLE[ENCODING_TABLE[i]] = (byte) i;
     }
 
     // deal with lower case letters as well
     DECODING_TABLE['a'] = DECODING_TABLE['A'];
     DECODING_TABLE['b'] = DECODING_TABLE['B'];
     DECODING_TABLE['c'] = DECODING_TABLE['C'];
     DECODING_TABLE['d'] = DECODING_TABLE['D'];
     DECODING_TABLE['e'] = DECODING_TABLE['E'];
     DECODING_TABLE['f'] = DECODING_TABLE['F'];
   }
 
   static {
     initialiseDecodingTable();
   }
 
   public static String toHex(byte[] data) {
     return toHex(data, 0, data.length);
   }
 
   public static String toHex(byte[] data, final int off, final int length) {
     StringBuilder sb = new StringBuilder(length * 2);
     for (int i = off; i < (off + length); i++) {
       int v = data[i] & 0xff;
       sb.append(ENCODING_TABLE[(v >>> 4)]);
       sb.append(ENCODING_TABLE[(v & 0xf)]);
     }
     return sb.toString();
   }
 
   private static boolean ignore(final char c) {
     return (c == '\n' || c == '\r' || c == '\t' || c == ' ');
   }
 
   public static byte[] fromHex(String data) {
     StringBuilder sb = new StringBuilder(data.length());
     for (char ch : data.toCharArray()) {
       if (!ignore(ch)) {
         sb.append(ch);
       }
     }
     byte[] bytes = new byte[sb.length() / 2];
     char[] chars = sb.toString().toCharArray();
 
     byte b1 = 0, b2 = 0;
     int offset = 0;
 
     for (int i = 0; i < chars.length;) {
       b1 = (byte) (DECODING_TABLE[chars[i++] & 0x7f] << 4);
       b2 = DECODING_TABLE[((byte) chars[i++])];
       bytes[offset++] = (byte) (b1 | b2);
     }
     return bytes;
   }
 }
