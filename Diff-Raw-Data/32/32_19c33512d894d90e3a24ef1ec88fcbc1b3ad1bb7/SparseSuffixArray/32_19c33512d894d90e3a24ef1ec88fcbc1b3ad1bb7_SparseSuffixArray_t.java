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
 // SparseSuffixArray.java
 // Since: 2011/02/12
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import org.utgenome.weaver.align.sais.Int40Array;
 
 /**
  * 
  * 
  * S(i) = S(X^{(j)}(i)) + j
  * 
  * X(i) = C(BWT[i]) + O(BWT[i], i)
  * 
  * @author leo
  * 
  */
 public class SparseSuffixArray
 {
     final Int40Array   sparseSA;
     private final long N;
     private final int  L;
 
     private SparseSuffixArray(Int40Array sparseSA, long N, int L) {
         this.sparseSA = sparseSA;
         this.N = N;
         this.L = L;
     }
 
     public static SparseSuffixArray createFromWaveletBWT(WaveletArray W, int suffixInterval) {
         FMIndex F = new FMIndexOnWaveletArray(W);
 
         final long N = W.textSize();
         final long sparseSA_length = (N + suffixInterval - 1) / suffixInterval;
         Int40Array sparseSA = new Int40Array(sparseSA_length);
 
         long sa = N;
         long saIndex = 0;
         for (long i = 0; i < N; ++i) {
             if (saIndex % suffixInterval == 0) {
                 sparseSA.set(saIndex / suffixInterval, sa);
             }
             --sa;
 
             saIndex = F.suffixLink(saIndex);
         }
 
         return new SparseSuffixArray(sparseSA, N, suffixInterval);
     }
 
     public void saveTo(File f) throws IOException {
         DataOutputStream d = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
         try {
             saveTo(d);
         }
         finally {
             d.close();
         }
     }
 
     public void saveTo(DataOutputStream d) throws IOException {
         d.writeLong(N);
         d.writeInt(L);
         d.writeLong(sparseSA.textSize());
         for (int i = 0; i < sparseSA.textSize(); ++i) {
             d.writeInt((int) sparseSA.lookup(i));
         }
         d.flush();
     }
 
     public static SparseSuffixArray loadFrom(File f) throws IOException {
         DataInputStream d = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
         try {
             return SparseSuffixArray.loadFrom(d);
         }
         finally {
             d.close();
         }
     }
 
     public static SparseSuffixArray loadFrom(DataInputStream d) throws IOException {
         final long N = d.readLong();
         final int L = d.readInt();
         final long sparseSALength = d.readLong();
         Int40Array sparseSA = new Int40Array(sparseSALength);
         for (int i = 0; i < sparseSA.textSize(); ++i) {
             int val = d.readInt();
             // Handle val as an uint32 integer
             sparseSA.set(i, 0xFFFFFFFFL & val);
         }
         return new SparseSuffixArray(sparseSA, N, L);
     }
 
     public static SparseSuffixArray buildFromSuffixArray(LSeq SA, int L) {
         long sparseSA_length = (SA.textSize() + L - 1) / L;
         Int40Array sparseSA = new Int40Array(sparseSA_length);
         for (long i = 0; i < sparseSA_length; ++i) {
             sparseSA.set(i, SA.lookup(i * L));
         }
         return new SparseSuffixArray(sparseSA, SA.textSize(), L);
     }
 
     /**
      * Get the index in the text
      * 
      * @param index
      *            index on the suffix array
      * @param fmIndex
      * @return
      */
     public long get(long index, FMIndex fmIndex) {
         long pos = index / L;
         long offset = index % L;
 
         if (pos > Integer.MAX_VALUE) {
             throw new ArrayIndexOutOfBoundsException("index: " + index);
         }
 
         if (offset == 0)
             return sparseSA.lookup(pos);
 
         long cursor = index;
         final long N = fmIndex.textSize();
         for (long j = 1; j <= N; j++) {
             cursor = fmIndex.suffixLink(cursor);
             if (cursor % L == 0)
                 return (sparseSA.lookup(cursor / L) + j + N) % N;
         }
         throw new IllegalStateException(String.format("cannot reach here: get(index:%d)", index));
 
     }
 }
