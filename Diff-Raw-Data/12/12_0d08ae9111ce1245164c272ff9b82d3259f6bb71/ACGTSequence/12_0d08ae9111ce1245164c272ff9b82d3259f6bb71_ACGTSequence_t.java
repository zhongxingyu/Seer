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
 // ACGTSequence.java
 // Since: 2011/06/30
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
 import java.util.Arrays;
 
 import org.xerial.snappy.SnappyInputStream;
 import org.xerial.snappy.SnappyOutputStream;
 import org.xerial.util.log.Logger;
 
 /**
  * ACGT genome sequence with N, using 3-bit encoding. This class uses 3 long
  * values to represent 64 ACGTN characters.
  * 
  * <pre>
  *   (64-bit)    (64-bit)        (64-bit)
  * |N0 ... N63|B0 B1 ....  B31|B32 B33 ... B63|
  * </pre>
  * 
  * @author leo
  * 
  */
 public class ACGTSequence implements LSeq
 {
     private static Logger     _logger        = Logger.getLogger(ACGTSequence.class);
 
     private static final int  LONG_BYTE_SIZE = 8;
     private static final long MAX_SIZE       = (2 * 1024 * 1024 * 1024 * 8 * 8) / 3;
 
     // 2G (max of Java array size) * 8 (long byte size) * 8 / 3 (bit) =  42G  (42G characters)
     private long[]            seq;
     private long              numBases;
     private long              capacity;
 
     private int               hash;                                                 // default to 0
 
     /**
      * Create an emptry sequence
      */
     public ACGTSequence() {
         ensureArrayCapacity(10);
         numBases = 0;
     }
 
     /**
      * Create ACGTSequence from the input ACGT(N) sequence
      * 
      * @param s
      */
     public ACGTSequence(CharSequence s) {
         this(s.length());
 
         for (int i = 0; i < s.length(); ++i) {
             byte code = ACGT.to3bitCode(s.charAt(i));
             set(i, code);
         }
     }
 
     /**
      * Create a sequence that can hold the given number of bases
      * 
      * @param numBases
      */
     public ACGTSequence(long numBases) {
         this.numBases = numBases;
 
         ensureArrayCapacity(numBases);
     }
 
     private static int minArraySize(long numBases) {
         long bitSize = numBases * 3L;
         long blockBitSize = LONG_BYTE_SIZE * 3L * 8L;
         long arraySize = ((bitSize + blockBitSize - 1L) / blockBitSize) * 3L;
         if (arraySize > Integer.MAX_VALUE) {
             throw new IllegalArgumentException(String.format("Cannot create ACGTSequece more than %,d size: %,d",
                     MAX_SIZE, numBases));
         }
         return (int) arraySize;
     }
 
     private void ensureArrayCapacity(long newCapacity) {
         if (seq != null && newCapacity < (seq.length * 64L / 3L)) {
             return;
         }
         long arraySize = minArraySize(newCapacity);
 
         if (seq == null) {
             seq = new long[(int) arraySize];
         }
         else {
             seq = Arrays.copyOf(seq, (int) arraySize);
         }
         this.capacity = (seq.length / 3L) * 64L;
     }
 
     private ACGTSequence(long[] rawSeq, long numBases) {
         this.seq = rawSeq;
         this.numBases = numBases;
     }
 
     public ACGT getACGT(long index) {
         return ACGT.decode((byte) lookup(index));
     }
 
     @Override
     public long lookup(long index) {
         int pos = (int) (index >> 6);
         int offset = (int) (index & 0x03FL);
         int shift = 62 - ((int) (index & 0x1FL) << 1);
 
         long nFlag = seq[pos * 3] & (1L << (63 - offset));
         int code = (int) (seq[pos * 3 + (offset >> 5) + 1] >>> shift) & 0x03;
         return nFlag == 0 ? code : 4;
     }
 
     @Override
     public void set(long index, long val) {
         // |N0 ... N63|B0 B1 ....  B31|B32 B33 ... B63|
         int pos = (int) (index >> 6);
         int offset = (int) (index & 0x3FL);
         int shift = (offset & 0x1F) << 1;
 
         seq[pos * 3] &= ~(1L << (63 - offset));
         seq[pos * 3] |= ((val >>> 2) & 0x01L) << (63 - offset);
         int bPos = pos * 3 + (offset >> 5) + 1;
         seq[bPos] &= ~(0xC000000000000000L >>> shift);
         seq[bPos] |= (val & 0x03) << (62 - shift);
     }
 
     public void append(ACGT base) {
         long index = this.numBases++;
         if (index >= this.capacity) {
             long newCapacity = (index * 3L / 2L) + 64L;
             ensureArrayCapacity(newCapacity);
         }
         set(index, base.code);
     }
 
     public void append(long ch) {
         long index = this.numBases++;
         if (index >= this.capacity) {
             long newCapacity = (index * 3L / 2L) + 64L;
             ensureArrayCapacity(newCapacity);
         }
         byte code = ACGT.to3bitCode((char) ch);
         set(index, code);
     }
 
     @Override
     public int hashCode() {
         if (hash != 0)
             return hash;
         int numFilledBlocks = (int) (numBases / 64L * 3L);
         long h = numBases * 31L;
         int pos = 0;
         for (; pos < numFilledBlocks; ++pos) {
             h += seq[pos] * 31L;
         }
         int offset = (int) (numBases % 64L);
         if (offset > 0) {
             h += (seq[pos] & (~0L << 64 - offset)) * 31L;
             h += (seq[pos + 1] & (offset < 32 ? ~0L << (32 - offset) * 2 : ~0L)) * 31L;
             h += (seq[pos + 2] & (offset < 32 ? 0L : ~0L << (64 - offset) * 2)) * 31L;
         }
         hash = (int) h;
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (!(obj instanceof ACGTSequence))
             return false;
 
         ACGTSequence other = ACGTSequence.class.cast(obj);
         if (this.numBases != other.numBases)
             return false;
 
         int numFilledBlocks = (int) (numBases / 64L * 3L);
         int pos = 0;
         for (; pos < numFilledBlocks; ++pos) {
             if (this.seq[pos] != other.seq[pos])
                 return false;
         }
         int offset = (int) (numBases % 64L);
         if (offset > 0) {
             long mask[] = new long[3];
             mask[0] = ~0L << 64 - offset;
             mask[1] = offset < 32 ? ~0L << (32 - offset) * 2 : ~0L;
             mask[2] = offset <= 32 ? 0L : ~0L << (64 - offset) * 2;
             for (int i = 0; i < mask.length; ++i) {
                 if ((seq[pos + i] & mask[i]) != (other.seq[pos + i] & mask[i]))
                     return false;
             }
         }
 
         return true;
     }
 
     /**
      * Extract and clone a subsequence of the specified range [start, end)
      * 
      * @param start
      * @param end
      * @return
      */
     public ACGTSequence subSequence(long start, long end) {
         if (start > end)
             throw new IllegalArgumentException(String.format("invalid range [%d, %d)", start, end));
         final long len = end - start;
         int minArraySize = minArraySize(len);
         ACGTSequence ss = new ACGTSequence(len);
         long[] dest = ss.seq;
         Arrays.fill(dest, 0L);
 
         for (long i = 0; i < len;) {
             int sPos = (int) ((start + i) >> 6);
             int sOffset = (int) ((start + i) & 0x3FL);
             int dPos = (int) (i >> 6);
             int dOffset = (int) (i & 0x3FL);
 
             int copyLen = 0;
             long n = seq[sPos * 3];
             long h = seq[sPos * 3 + 1];
             long l = seq[sPos * 3 + 2];
             if (sOffset == dOffset) {
                 copyLen = 64;
             }
             else if (sOffset < dOffset) {
                 // right shift
                 int shiftLen = dOffset - sOffset;
                 copyLen = 64 - dOffset;
                 // Copy Ns
                 n >>>= shiftLen;
                 // Copy ACGT blocks
                 if (shiftLen < 32) {
                     h >>= shiftLen * 2;
                    l = (h << (64 - shiftLen * 2)) | (l >>> shiftLen * 2);
                 }
                 else {
                     l = h >>> (shiftLen - 32) * 2;
                     h = 0L;
                 }
             }
             else {
                 // left shift
                 int shiftLen = sOffset - dOffset;
                 copyLen = 64 - sOffset;
                 // Copy Ns
                 n <<= shiftLen;
                 // Copy ACGT blocks
                 if (shiftLen < 32) {
                    h = (h << shiftLen * 2) | (l >>> (64 - shiftLen * 2));
                     l <<= shiftLen * 2;
                 }
                 else {
                     h = l << (shiftLen - 32) * 2;
                     l = 0L;
                 }
             }
             dest[dPos * 3] |= n;
             dest[dPos * 3 + 1] |= h;
             dest[dPos * 3 + 2] |= l;
 
             i += copyLen;
         }
 
         return ss;
     }
 
     @Override
     public long textSize() {
         return numBases;
     }
 
     /**
      * Create a reverse string of the this sequence. The sentinel is appended as
      * the last character of the resulting sequence.
      * 
      * @return Reverse sequence. The returned sequence is NOT a complement of
      *         the original sequence.
      */
     public ACGTSequence reverse() {
         ACGTSequence rev = new ACGTSequence(this.numBases);
         for (long i = 0; i < numBases; ++i) {
             rev.set(i, this.lookup(numBases - i - 1));
         }
         return rev;
     }
 
     /**
      * Create a complementary sequence (not reversed)
      * 
      * @return complementary sequence
      */
     public ACGTSequence complement() {
         ACGTSequence c = new ACGTSequence(this.numBases);
 
         int numBlocks = seq.length / 3;
         for (int i = 0; i < numBlocks; ++i) {
             c.seq[i * 3] = this.seq[i * 3];
             c.seq[i * 3 + 1] = ~(this.seq[i * 3 + 1]);
             c.seq[i * 3 + 2] = ~(this.seq[i * 3 + 2]);
         }
         return c;
     }
 
     public ACGTSequence reverseComplement() {
         ACGTSequence rc = reverse();
         int numBlocks = seq.length / 3;
         for (int i = 0; i < numBlocks; ++i) {
             rc.seq[i * 3 + 1] = ~(rc.seq[i * 3 + 1]);
             rc.seq[i * 3 + 2] = ~(rc.seq[i * 3 + 2]);
         }
         return rc;
     }
 
     public static ACGTSequence loadFrom(File f) throws IOException {
         DataInputStream d = new DataInputStream(new BufferedInputStream(new FileInputStream(f), 4 * 1024 * 1024));
         try {
             return ACGTSequence.loadFrom(d);
         }
         finally {
             d.close();
         }
     }
 
     public static ACGTSequence loadFrom(DataInputStream in) throws IOException {
         // The num bases must be always 2
 
         long numBases = in.readLong();
         int longArraySize = minArraySize(numBases);
         long[] seq = new long[longArraySize];
         SnappyInputStream sin = new SnappyInputStream(in);
         int readBytes = sin.read(seq);
         return new ACGTSequence(seq, numBases);
     }
 
     public void saveTo(DataOutputStream out) throws IOException {
         out.writeLong(this.numBases);
         SnappyOutputStream sout = new SnappyOutputStream(out);
         int longArraySize = minArraySize(this.numBases);
         sout.write(seq, 0, longArraySize);
         sout.flush();
     }
 
     public void saveTo(File file) throws IOException {
         DataOutputStream d = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
         try {
             saveTo(d);
         }
         finally {
             d.close();
         }
     }
 
     @Override
     public String toString() {
         StringBuilder b = new StringBuilder();
         for (int i = 0; i < numBases; ++i) {
             ACGT base = ACGT.decode((byte) lookup(i));
             b.append(base);
         }
         return b.toString();
     }
 
     /**
      * Count the number of the specified character in the range
      * 
      * @param code
      * @param start
      * @param end
      * @return
      */
     public long count(ACGT base, long start, long end) {
         long count = 0;
         for (long i = start; i < end; ++i) {
             if ((char) lookup(i) == base.code)
                 count++;
         }
         return count;
     }
 
     /**
      * Count the number of occurrence of the code within the specified range
      * 
      * @param code
      * @param start
      * @param end
      *            (exclusive)
      * @return
      */
     public long fastCount(ACGT base, long start, long end) {
         long count = 0;
 
         if (base == ACGT.N) {
             // Count N
             int sPos = (int) (start >>> 6);
             int sOffset = (int) (start & 0x3FL);
             int ePos = (int) (end >>> 6);
             for (; sPos <= ePos; ++sPos) {
                 long mask = ~0L;
                 if (sOffset != 0) {
                     mask >>>= sOffset;
                     sOffset = 0;
                 }
                 if (sPos == ePos) {
                     int eOffset = (int) (end & 0x3FL);
                     long rMask = (eOffset == 0) ? 0L : ~((1L << (64 - eOffset)) - 1);
                     mask &= rMask;
                 }
                 count += countOneBit(seq[sPos * 3] & mask);
             }
         }
         else {
             // Count A, C, G, T
             int sPos = (int) (start >>> 5);
             int sOffset = (int) (start & 0x1FL);
             int ePos = (int) (end >>> 5);
 
             for (; sPos <= ePos; ++sPos) {
 
                 long mask = ~0L;
                 if (sOffset != 0) {
                     mask >>>= sOffset * 2;
                     sOffset = 0;
                 }
                 int bIndex = sPos / 2 * 3;
                 int block = sPos % 2;
                 long v = seq[bIndex + 1 + block];
                 long nFlag = interleave32With0(seq[bIndex] >>> (32 * (1 - block)));
                 if (sPos == ePos) {
                     int eOffset = (int) (end & 0x1FL);
                     long rMask = (eOffset == 0) ? 0L : ~((1L << (32 - eOffset) * 2) - 1);
                     mask &= rMask;
                 }
                 long r = ~0L;
                 r &= ((base.code & 0x02) == 0 ? ~v : v) >>> 1;
                 r &= ((base.code & 0x01) == 0 ? ~v : v);
                 r &= 0x5555555555555555L;
                 r &= ~nFlag;
                 r &= mask;
                 count += countOneBit(r);
             }
         }
 
         return count;
     }
 
     static int interleaveWith0(int v) {
         v = ((v & 0xFF00) << 8) | (v & 0x00FF);
         v = ((v << 4) | v) & 0x0F0F0F0F;
         v = ((v << 2) | v) & 0x33333333;
         v = ((v << 1) | v) & 0x55555555;
         return v;
     }
 
     /**
      * Interleave low 32bits (in a long value) with 0s. For example, 11110011 (8
      * bit value) becomes 0101010100000101 (16 bit value)
      * 
      * @param v
      * @return
      */
     static long interleave32With0(long v) {
         v = ((v & 0xFFFF0000L) << 16) | (v & 0x0000FFFFL);// 0000000000000000
         v = ((v << 8) | v) & 0x00FF00FF00FF00FFL; // 0000000011111111
         v = ((v << 4) | v) & 0x0F0F0F0F0F0F0F0FL; // 00001111
         v = ((v << 2) | v) & 0x3333333333333333L; // 0011
         v = ((v << 1) | v) & 0x5555555555555555L; // 0101
         return v;
     }
 
     /**
      * Count the number of 1s in the input. See also the Hacker's Delight:
      * http://hackers-delight.org.ua/038.htm
      * 
      * @param x
      * @return the number of 1-bit in the input x
      */
     public static long countOneBit(long x) {
         x = (x & 0x5555555555555555L) + ((x >>> 1) & 0x5555555555555555L);
         x = (x & 0x3333333333333333L) + ((x >>> 2) & 0x3333333333333333L);
         x = (x + (x >>> 4)) & 0x0F0F0F0F0F0F0F0FL;
         x = x + (x >>> 8);
         x = x + (x >>> 16);
         x = x + (x >>> 32);
         return x & 0x7FL;
     }
 
     @Override
     public long increment(long i, long val) {
         throw new UnsupportedOperationException("update");
     }
 
 }
