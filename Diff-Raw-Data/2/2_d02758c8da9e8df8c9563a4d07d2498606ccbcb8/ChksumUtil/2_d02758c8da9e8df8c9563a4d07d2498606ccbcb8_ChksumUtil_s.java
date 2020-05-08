 /**
 * Copyright (C) 2011 - 101loops.com <dev@101loops.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.crashnote.core.util;
 
 import java.io.Serializable;
 
 /**
  * We compute the checksum using Broder s implementation of
  * Rabin s fingerprinting algorithm. Fingerprints offer
  * provably strong probabilistic guarantees that two
  * different strings will not have the same fingerprint.
  * Other checksum algorithms, such as MD5 and SHA, do not
  * offer such provable guarantees, and are also more
  * expensive to compute than Rabin fingerprint.
  * <p/>
  * A disadvantage is that these faster functions are
  * efficiently invertible (that is, one can easily build an
  * URL that hashes to a particular location), a fact that
  * might be used by malicious users to nefarious purposes.
  * <p/>
  * Using the Rabin's fingerprinting function, the probability of
  * collision of two strings s1 and s2 can be bounded (in a adversarial
  * model for s1 and s2) by max(|s1|,|s2|)/2**(l-1), where |s1| is the
  * length of the string s1 in bits.
  * <p/>
  * The advantage of choosing Rabin fingerprints (which are based on random
  * irreducible polynomials) rather than some arbitrary hash function is that
  * their probability of collision os well understood. Furthermore Rabin
  * fingerprints can be computed very efficiently in software and we can
  * take advantage of their algebraic properties when we compute the
  * fingerprints of "sliding windows".
  * <p/>
  * M. O. Rabin
  * Fingerprinting by random polynomials.
  * Center for Research in Computing Technology
  * Harvard University Report TR-15-81
  * 1981
  * <p/>
  * A. Z. Broder
  * Some applications of Rabin's fingerprinting method
  * In R.Capicelli, A. De Santis and U. Vaccaro editors
  * Sequences II:Methods in Communications, Security, and Computer Science
  * pages 143-152
  * Springer-Verlag
  * 1993
  */
 public final class ChksumUtil implements Serializable {
 
     // INTERFACE ==================================================================================
 
     /**
      * Computes the Rabin hash value of a String.
      *
      * @param s the string to be hashed
      * @return the hash value
      */
     public static long hash(final String s) {
         return hash(s.toCharArray());
     }
 
     // INTERNALS ==================================================================================
 
     private final static int P_DEGREE = 64;
     private final static int X_P_DEGREE = 1 << (P_DEGREE - 1);
     private static final long POLY = Long.decode("0x004AE1202C306041") | 1 << 63;
 
     private static final long[] table32, table40, table48, table54;
     private static final long[] table62, table70, table78, table84;
 
     static {
         table32 = new long[256];
         table40 = new long[256];
         table48 = new long[256];
         table54 = new long[256];
         table62 = new long[256];
         table70 = new long[256];
         table78 = new long[256];
         table84 = new long[256];
         final long[] mods = new long[P_DEGREE];
         mods[0] = POLY;
         for (int i = 0; i < 256; i++) {
             table32[i] = 0;
             table40[i] = 0;
             table48[i] = 0;
             table54[i] = 0;
             table62[i] = 0;
             table70[i] = 0;
             table78[i] = 0;
             table84[i] = 0;
         }
         for (int i = 1; i < P_DEGREE; i++) {
             mods[i] = mods[i - 1] << 1;
             if ((mods[i - 1] & X_P_DEGREE) != 0) {
                 mods[i] = mods[i] ^ POLY;
             }
         }
         for (int i = 0; i < 256; i++) {
             long c = i;
             for (int j = 0; j < 8 && c != 0; j++) {
                 if ((c & 1) != 0) {
                     table32[i] = table32[i] ^ mods[j];
                     table40[i] = table40[i] ^ mods[j + 8];
                     table48[i] = table48[i] ^ mods[j + 16];
                     table54[i] = table54[i] ^ mods[j + 24];
                     table62[i] = table62[i] ^ mods[j + 32];
                     table70[i] = table70[i] ^ mods[j + 40];
                     table78[i] = table78[i] ^ mods[j + 48];
                     table84[i] = table84[i] ^ mods[j + 56];
                 }
                 c >>>= 1;
             }
         }
     }
 
     /**
      * Return the Rabin hash value of an array of chars.
      *
      * @param A the array of chars
      * @return the hash value
      */
     private static long hash(final char[] A) {
         long w = 0;
         final int start = A.length % 4;
         for (int s = 0; s < start; s++) {
             w = (w << 16) ^ (A[s] & 0xFFFF);
         }
         for (int s = start; s < A.length; s += 4) {
             w =
                 table32[(int) (w & 0xFF)]
                     ^ table40[(int) ((w >>> 8) & 0xFF)]
                     ^ table48[(int) ((w >>> 16) & 0xFF)]
                     ^ table54[(int) ((w >>> 24) & 0xFF)]
                     ^ table62[(int) ((w >>> 32) & 0xFF)]
                     ^ table70[(int) ((w >>> 40) & 0xFF)]
                     ^ table78[(int) ((w >>> 48) & 0xFF)]
                     ^ table84[(int) ((w >>> 56) & 0xFF)]
                     ^ ((long) (A[s] & 0xFFFF) << 48)
                     ^ ((long) (A[s + 1] & 0xFFFF) << 32)
                     ^ ((long) (A[s + 2] & 0xFFFF) << 16)
                     ^ ((long) (A[s + 3] & 0xFFFF));
         }
         return w;
     }
 }
