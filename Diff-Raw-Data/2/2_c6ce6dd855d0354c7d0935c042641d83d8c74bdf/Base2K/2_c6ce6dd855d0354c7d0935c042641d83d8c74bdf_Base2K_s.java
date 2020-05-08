 package com.yasminapp.client;
 
 public class Base2K {
   // See http://twuuenc.atxconsulting.com/index.htm
 
   private static final char[] ALPHABET;
   static {
     ALPHABET = concat(
       range((char) 0x20, (char) 0x7E),
       range((char) 0xA1, (char) 0xFF),
       range((char) 0x100, (char) 0x17F),
       range((char) 0x180, (char) 0x24F),
       range((char) 0x250, (char) 0x2AE),
       range((char) 0x2B0, (char) 0x2FE),
       range((char) 0x390, (char) 0x3CE),
       range((char) 0x3D0, (char) 0x3FF),
       range((char) 0x400, (char) 0x4FF),
       range((char) 0x1401, (char) 0x1676),
       range((char) 0x2580, (char) 0x259F),
       range((char) 0x25A0, (char) 0x25FF),
       range((char) 0x2701, (char) 0x27BE),
       range((char) 0x27C0, (char) 0x27EB),
       range((char) 0x27F0, (char) 0x27FF));
   }
 
   public static String encode(byte[] buf) {
     StringBuilder sb = new StringBuilder();
     int value;
     int chunks = buf.length / 11;
     int offset = 0;
     // process whole 11-byte chunks
     // MSB-first
     for (int i=0; i < chunks; i++) {
       // 8/3
      value = ((buf[offset] & 0xff << 3)) | ((buf[offset+1] & 0xe0) >>> 5);
       sb.append(ALPHABET[value]);
       offset++;
       // 5/6
       value = ((buf[offset] & 0x1f) << 6)  | ((buf[offset+1] & 0xfd) >>> 2);
       sb.append(ALPHABET[value]);
       offset++;
       // 2/8/1
       value = ((buf[offset] & 0x03) << 9)  | ((buf[offset+1] & 0xff) << 1) | ((buf[offset+2] & 0x80) >>> 7);
       sb.append(ALPHABET[value]);
       offset += 2;
       // 7/4
       value = ((buf[offset] & 0x7f) << 4)  | ((buf[offset+1] & 0xf0) >>> 4);
       sb.append(ALPHABET[value]);
       offset++;
       // 4/7
       value = ((buf[offset] & 0x0f) << 7)  | ((buf[offset+1] & 0xfe) >>> 1);
       sb.append(ALPHABET[value]);
       offset++;
       // 1/8/2
       value = ((buf[offset] & 0x01) << 10)  | ((buf[offset+1] & 0xff) << 2) | ((buf[offset+2] & 0xc0) >>> 6);
       sb.append(ALPHABET[value]);
       offset += 2;
       // 6/5
       value = ((buf[offset] & 0x3f) << 5)  | ((buf[offset+1] & 0xf8) >>> 3);
       sb.append(ALPHABET[value]);
       offset++;
       // 3/8
       value = ((buf[offset] & 0x07) << 8)  | ((buf[offset+1] & 0xff) >>> 3);
       sb.append(ALPHABET[value]);
       offset++;
     }
     // TODO process remainder bytes (buf.length % 11)
     return sb.toString();
   }
 
   public static byte[] decode(String s) {
     throw new RuntimeException("Not Yet Implemented");
   }
 
   private static char[] range(char start, char stop) {
     char[] result = new char[stop - start];
     for (char ch = start, i = 0; ch < stop; ch++, i++)
       result[i] = ch;
 
     return result;
   }
 
   private static char[] concat(char[]... lst) {
     int totalLength = 0;
     for (char[] arr : lst) {
       totalLength += arr.length;
     }
 
     char[] result = new char[totalLength];
     int offset = 0;
     for (char[] arr : lst) {
       System.arraycopy(arr, 0, result, offset, arr.length);
       offset += arr.length;
     }
     return result;
   }
 }
