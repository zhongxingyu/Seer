 /*--------------------------------------------------------------------------
  *  Copyright 2011 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // genome-weaver Project
 //
 // BitVectorTest.java
 // Since: 2011/02/15
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 import org.xerial.util.log.Logger;
 
 public class BitVectorTest
 {
     private static Logger _logger = Logger.getLogger(BitVectorTest.class);
 
     @Test
     public void bitFlag() throws Exception {
         int n = 100;
         for (int i = 0; i < n; ++i) {
             BitVector v = new BitVector(n);
             v.set(i);
             assertTrue(v.get(i));
         }
     }
 
     @Test
     public void countOneBit() throws Exception {
         String orig = "00101101000100000111111001010101000000000001101010100010101010011100001010100010100111";
         BitVector v = BitVector.parseString(orig);
         for (int s = 0; s < v.size(); ++s) {
             for (int e = s; e < v.size(); ++e) {
                 assertEquals(String.format("range:(%d, %d)", s, e), countOne(orig, s, e), v.countOneBits(s, e));
             }
         }
     }
 
     private static int countOne(String s, int start, int end) {
         int count = 0;
         for (int i = start; i < end; ++i) {
             if (s.charAt(s.length() - i - 1) == '1')
                 count++;
         }
         return count;
     }
 
     @Test
     public void lshift() throws Exception {
         String orig = "001011010001000001111110010101010011101111110001010101001111110000000001101010100010101010011100001010100010100111";
 
         for (int i = 0; i < orig.length(); ++i) {
             BitVector v = BitVector.parseString(orig);
             StringBuilder s = new StringBuilder();
             s.append(orig.substring(i));
             for (int k = 0; k < i; ++k)
                 s.append("0");
 
             BitVector ans = BitVector.parseString(s.toString());
             assertEquals(ans.toString(), v.lshift(i).toString());
         }
     }
 
     @Test
     public void lshift7() throws Exception {
         BitVector v = new BitVector(28).not();
         BitVector l = v.lshift(7);
         assertEquals("1111111111111111111110000000", l.toString());
     }
 
     @Test
     public void rshift() throws Exception {
         String orig = "001011010001000001111110011110000000001101010100010101010011100001010100010100111";
 
         for (int i = 0; i < orig.length(); ++i) {
             BitVector v = BitVector.parseString(orig);
             StringBuilder s = new StringBuilder();
             for (int k = 0; k < i; ++k)
                 s.append("0");
             s.append(orig.substring(0, orig.length() - i));
 
             BitVector ans = BitVector.parseString(s.toString());
             assertEquals("rshift:" + i, ans.toString(), v.rshift(i).toString());
         }
     }
 
     @Test
     public void add() throws Exception {
 
         BitVector v = new BitVector(128);
         v.set(64);
         BitVector v2 = new BitVector(128);
         v2.set(64);
        v._add(v2);
         BitVector ans = new BitVector(128);
        ans.set(65);
         assertEquals(ans, v);
 
     }
 
 }
