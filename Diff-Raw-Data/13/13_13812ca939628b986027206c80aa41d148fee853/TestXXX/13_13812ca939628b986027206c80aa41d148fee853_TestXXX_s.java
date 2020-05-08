 package com.logentries.murmur_test;
 
 import org.junit.Test;
 import java.util.Arrays;
 import java.util.List;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 
 import java.lang.StringBuffer;
 
 public class TestXXX {
     static String getXXX(final int len) {
         StringBuffer sb = new StringBuffer(len);
         for (int i = 0; i < len; ++i) {
             sb.append('x');
         }
         return sb.toString();
     }
 
     @Test
     public void testXXX() {
         final String[] xxx = {"xxx",
                               "xxxxxxxxxxxxxxxxxx",
                               "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
                               getXXX(256*1024),
                               getXXX(128*1024*1024)};
 
         for (int i = 0; i < xxx.length; ++i) {
             final long hash1 = com.logentries.murmur.MurmurHashV3.hash128_64(xxx[i]);
             final long hash2 = ie.ucd.murmur.MurmurHashV3.hash128_64(xxx[i]);
             System.err.println("Str.   i=" + i + "\thash1=" + hash1 + "; hash2=" + hash2);
 
             assertEquals(hash1, hash2);
         }
 
         for (int i = 0; i < xxx.length; ++i) {
             final byte[] xxx_bytes = xxx[i].getBytes();
             final long[] hashes1 = com.logentries.murmur.MurmurHashV3.hash128(xxx_bytes, xxx_bytes.length);
             final long[] hashes2 = ie.ucd.murmur.MurmurHashV3.hash128(xxx_bytes, xxx_bytes.length);
             System.err.println("Bytes. i=" + i + "\thash1=" + Arrays.toString(hashes1) + "; hash2=" + Arrays.toString(hashes2));
 
             assertTrue(Arrays.equals(hashes1, hashes2));
         }
     }
 
     private List<String> mAlphabet = Arrays.asList("aaa", "b", "ccc", "uvw", "-", "$", "Příliš", "žluťoučký", "kůň", "úpěl", "ďábelské", "ódy");
 
     @Test
     public void TestRandStrings() {
         GenString gs = new GenString(mAlphabet, 7);
          for (int i = 0; i < 25; ++i) {
               final String str = gs.next();
               final long hash1 = com.logentries.murmur.MurmurHashV3.hash128_64(str);
               final long hash2 = ie.ucd.murmur.MurmurHashV3.hash128_64(str);
               System.err.println("Str.   s=" + str + "; hash1=" + hash1 + "; hash2=" + hash2);
               assertEquals(hash1, hash2);
          }
     }
 }
