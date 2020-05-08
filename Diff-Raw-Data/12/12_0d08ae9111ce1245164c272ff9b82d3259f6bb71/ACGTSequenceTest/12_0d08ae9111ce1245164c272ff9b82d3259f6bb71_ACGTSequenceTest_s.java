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
 // ACGTSequenceTest.java
 // Since: 2011/06/30
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align;
 
 import static org.junit.Assert.*;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.util.Random;
 
 import org.junit.Test;
 import org.xerial.util.log.Logger;
 
 public class ACGTSequenceTest
 {
     private static Logger _logger = Logger.getLogger(ACGTSequenceTest.class);
 
     private final String  orig    = "AGCCCCGCATTNNATAGATTTAGCGCGGATTTTAATANNNATATATNNATATNATANNNNAACCCGCGCTTAGAGAGANNGGGGCCCCTTTTCCATAGAGAAATTAGCGGATNNATGC";
 
     @Test
     public void constructor() throws Exception {
 
         _logger.info(orig);
         ACGTSequence s = new ACGTSequence(orig);
 
         assertEquals(orig.length(), s.textSize());
         assertEquals(orig, s.toString());
 
         for (int i = 0; i < orig.length(); ++i) {
             ACGT base = s.getACGT(i);
             assertEquals(String.format("index %d", i), ACGT.encode(orig.charAt(i)), base);
         }
     }
 
     @Test
     public void reverse() throws Exception {
         ACGTSequence s = new ACGTSequence(orig);
         ACGTSequence rev = s.reverse();
         for (int i = 0; i < s.textSize(); ++i) {
             assertEquals(s.lookup(i), rev.lookup(s.textSize() - i - 1));
         }
     }
 
     @Test
     public void complement() throws Exception {
         ACGTSequence s = new ACGTSequence(orig);
         ACGTSequence r = s.complement();
 
         for (int i = 0; i < s.textSize(); ++i) {
             ACGT b = s.getACGT(i);
             ACGT c = r.getACGT(i);
             assertEquals(b.complement(), c);
         }
     }
 
     @Test
     public void reverseComplement() throws Exception {
         ACGTSequence s = new ACGTSequence(orig);
         ACGTSequence rc = s.reverseComplement();
 
         for (int i = 0; i < orig.length(); ++i) {
             ACGT c = s.getACGT(orig.length() - i - 1).complement();
             ACGT r = rc.getACGT(i);
             assertEquals("index " + i, c, r);
         }
     }
 
     @Test
     public void save() throws Exception {
         ACGTSequence s = new ACGTSequence(orig);
 
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream(b);
         s.saveTo(out);
 
         out.close();
         byte[] bb = b.toByteArray();
         ACGTSequence s2 = ACGTSequence.loadFrom(new DataInputStream(new ByteArrayInputStream(bb)));
         assertEquals(s.toString(), s2.toString());
         _logger.info(s2.toString());
     }
 
     @Test
     public void interleave() throws Exception {
 
         int v = 0x0000FFFF;
         _logger.info(toBinaryString(v, 32));
         _logger.info(toBinaryString(ACGTSequence.interleaveWith0(v), 32));
 
         v = 0x00001234;
         _logger.info(toBinaryString(v, 32));
         _logger.info(toBinaryString(ACGTSequence.interleaveWith0(v), 32));
     }
 
     @Test
     public void interleave32() throws Exception {
         long v = 0x00000000FFFFFFFFL;
         _logger.info(toBinaryString(v, 64));
         _logger.info(toBinaryString(ACGTSequence.interleave32With0(v), 64));
 
         v = 0xF0F0F0F0L;
         _logger.info(toBinaryString(v, 64));
         _logger.info(toBinaryString(ACGTSequence.interleave32With0(v), 64));
 
     }
 
     public String toBinaryString(long v, int size) {
         StringBuilder b = new StringBuilder();
         for (long i = 1L << (size - 1); i != 0; i >>>= 1) {
             b.append(((v & i) == 0 ? "0" : "1"));
         }
         return b.toString();
     }
 
     @Test
     public void fastCount() throws Exception {
         Random r = new Random(0);
         StringBuilder seq = new StringBuilder();
         for (int i = 0; i < 69; ++i) {
             seq.append(ACGT.decode((byte) (r.nextInt(4) + 1)).toString());
         }
 
         ACGTSequence s = new ACGTSequence(seq.toString());
         _logger.info(s);
 
         //        {
         //            ACGT c = ACGT.N;
         //            int x = 0, y = 2;
         //            assertEquals(String.format("code:%s, s=%d, e=%d", c, x, y), s.count(c, x, y), s.fastCount(c, x, y));
         //        }
 
         {
             for (ACGT c : ACGT.values()) {
                 for (int x = 0; x < s.textSize(); x++) {
                     for (int y = x; y < s.textSize(); y++)
                         assertEquals(String.format("code:%s, s=%d, e=%d", c, x, y), s.count(c, x, y),
                                 s.fastCount(c, x, y));
                 }
             }
         }
 
     }
 
     @Test
     public void fastCount2() throws Exception {
         ACGTSequence s = new ACGTSequence("TTTTATTAAAAAAAA");
         assertEquals(9, s.fastCount(ACGT.A, 0, 15));
     }
 
     @Test
     public void append() throws Exception {
         ACGTSequence s1 = new ACGTSequence(orig);
 
         ACGTSequence s2 = new ACGTSequence();
         for (int i = 0; i < orig.length(); ++i) {
             s2.append(orig.charAt(i));
         }
         assertEquals(s1.textSize(), s2.textSize());
         assertEquals(s1.toString(), s2.toString());
 
     }
 
     @Test
     public void subSequence() throws Exception {
         ACGTSequence seq = new ACGTSequence(orig);
        assertEquals(new ACGTSequence(orig.subSequence(1, 65)), seq.subSequence(1, 65));
 
         for (int s = 0; s < seq.textSize(); ++s) {
             for (int e = s; e < seq.textSize(); ++e) {
                 ACGTSequence ss = seq.subSequence(s, e);
                 assertEquals(String.format("[%d, %d)", s, e), new ACGTSequence(orig.subSequence(s, e)), ss);
             }
         }
 
     }
 
     @Test
     public void testEquals() {
 
     }
 
 }
