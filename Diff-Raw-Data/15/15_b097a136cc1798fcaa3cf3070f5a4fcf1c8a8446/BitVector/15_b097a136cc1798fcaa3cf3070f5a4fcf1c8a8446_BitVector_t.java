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
 // BitVector.java
 // Since: 2011/07/20
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.Arrays;
 
 /**
  * Bit vector of 0/1 binary string
  * 
  * @author leo
  * 
  */
 public class BitVector
 {
     private static final int B = 64; // the number of bits in a block
     private final long[]     block;
     private final long       size;
 
     private int              hash;  // default to 0
 
     public BitVector(long size) {
         this.size = size;
         long blockSize = (size + B - 1) / B;
         if (blockSize > Integer.MAX_VALUE)
             throw new IllegalArgumentException("cannot create bit vector with size > " + Integer.MAX_VALUE
                     + ". size = " + size);
 
         this.block = new long[(int) blockSize];
         clear();
     }
 
     private BitVector(long size, long[] block) {
         this.size = size;
         this.block = block;
     }
 
     public BitVector(BitVector other) {
         this.size = other.size;
         this.block = other.block.clone();
     }
 
     public BitVector copy() {
         return new BitVector(size, this.block.clone());
     }
 
     public long size() {
         return size;
     }
 
     public void clear() {
         Arrays.fill(block, 0L);
     }
 
     public boolean get(long index) {
         long blockPos = index / B;
         if (blockPos > block.length)
             return false;
 
         long offset = index % B;
         return ((block[(int) blockPos] >>> offset) & 1L) != 0;
     }
 
     public int getInt(long index) {
         long blockPos = index / B;
         if (blockPos > block.length)
             return 0;
 
         long offset = index % B;
         return (int) ((block[(int) blockPos] >>> offset) & 1L);
     }
 
     //    /**
     //     * @param index
     //     * @param len < 64
     //     * @return
     //     */
     //    public long get64(long index, int len) {
     //        long blockPos = index / B;
     //        if (blockPos > block.length)
     //            return 0L;
     //            
     //        long offset = index % B;
     //        long mask = 0x8000000000000000L >>> offset;
     //        
     //        
     //    }
 
     public void set(long index, boolean c) {
         if (c)
             set(index);
         else
             reset(index);
     }
 
     /**
      * Set the bit at the index. Note that this operation invalidates the
      * current rank table, so you must refresh the rank table by calling
      * {@link #refreshRankTable()}
      * 
      * @param index
      */
     public void set(long index) {
         long blockPos = index / B;
         long offset = index % B;
         block[(int) blockPos] |= 1L << offset;
     }
 
     public void reset(long index) {
         long blockPos = index / B;
         long offset = index % B;
         long mask = 1L << offset;
         block[(int) blockPos] &= ~mask;
     }
 
     public BitVector set(BitVector other) {
         System.arraycopy(other.block, 0, this.block, 0, block.length);
         return this;
     }
 
     public BitVector lshift(int len) {
         return new BitVector(this)._lshift(len);
     }
 
     public BitVector rshift(int len) {
         return new BitVector(this)._rshift(len);
     }
 
     public BitVector or(BitVector other) {
         return new BitVector(this)._or(other);
     }
 
     public BitVector and(BitVector other) {
         return new BitVector(this)._and(other);
     }
 
     public BitVector not() {
         return new BitVector(this)._not();
     }
 
     public BitVector xor(BitVector other) {
         return new BitVector(this)._xor(other);
     }
 
     public BitVector _lshift(int len) {
         int blockOffset = len / B;
         long offset = len % B;
         long lowMask = ~(~0L >>> offset);
 
         for (int i = block.length - 1; i >= 0; --i) {
             int b = i - blockOffset;
             long high = b >= 0 ? block[b] << offset : 0L;
             long low = (b - 1 >= 0) ? ((block[b - 1] & lowMask) >>> (B - offset)) : 0L;
             block[i] = high | low;
         }
         return this;
     }
 
     public BitVector _or(BitVector other) {
         int l = Math.min(block.length, other.block.length);
         for (int i = 0; i < l; ++i) {
             block[i] |= other.block[i];
         }
         return this;
     }
 
     public BitVector _rshift(int len) {
         int blockOffset = len / B;
         long offset = len % B;
         long mask = ~(~0L << offset);
 
         for (int i = 0; i < block.length; ++i) {
             int bi = i + blockOffset;
             long low = bi < block.length ? block[bi] >>> offset : 0L;
             block[i] = low;
             long high = bi + 1 < block.length ? (block[bi + 1] & mask) << (B - offset) : 0L;
             block[i] |= high;
         }
         return this;
     }
 
     public BitVector _not() {
         for (int i = 0; i < block.length - 1; ++i) {
             block[i] = ~block[i];
         }
         int offset = (int) size % B;
         block[block.length - 1] = (~block[block.length - 1]) & ~(~0L << offset);
         return this;
     }
 
     public BitVector _and(BitVector other) {
         for (int i = 0; i < block.length; ++i) {
             block[i] &= other.block[i];
         }
         return this;
     }
 
     public BitVector _xor(BitVector other) {
         for (int i = 0; i < block.length; ++i) {
             block[i] ^= other.block[i];
         }
         return this;
     }
 
     public boolean isZero() {
         for (int i = 0; i < block.length - 1; ++i) {
             if (block[i] != 0L)
                 return false;
         }
         if ((block[block.length - 1] & (~0L >>> (B - size % B))) != 0L)
             return false;
 
         return true;
     }
 
     /**
      * 
      * Reference: Hacker's delight. Chapter 2
      * 
      * @param other
      * @return
      */
    public BitVector _add(BitVector other) {
         long c = 0; // carry (borrow in from lower bits)
        for (int i = 0; i < block.length; ++i) {
             long x = block[i];
             long y = other.block[i];
             long v = x + y + c; // 0 <= c <= 1
             block[i] = v;
             // x y  x+y carry  
             // 0 0   0    0    
             // 0 1   1    0    
             // 1 0   1    0    
             // 1 1   0    1     
             // Detect overflows in v=x+y+c. Adding c (0 or 1) does not affect this expression 
             c = ((v ^ x) & (v ^ y)) >>> 63;
         }
         return this;
     }
 
     public int countOneBits(long start, long end) {
         int count = 0;
         int eIndex = (int) (end / B);
         int eOffset = (int) (end % B);
         for (long i = start; i < end;) {
             int index = (int) (i / B);
             int sOffset = (int) (i % B);
 
             // 00011111
             // 01234567
             long mask = ~0L << sOffset;
             long v = block[index] & mask;
             if (index == eIndex) {
                 // tail mask
                 // 11111110
                 // 01234567
                 long tMask = eOffset == 0 ? 0L : (~0L) >>> (B - eOffset);
                 v &= tMask;
             }
             count += popCount(v);
 
             i = (index + 1) * B;
         }
 
         return count;
     }
 
     /**
      * Count the number of 1s in the input. See also the Hacker's Delight:
      * http://hackers-delight.org.ua/038.htm
      * 
      * @param x
      * @return the number of 1-bit in the input x
      */
     public static long popCount(long x) {
         x = (x & 0x5555555555555555L) + ((x >>> 1) & 0x5555555555555555L);
         x = (x & 0x3333333333333333L) + ((x >>> 2) & 0x3333333333333333L);
         x = (x + (x >>> 4)) & 0x0F0F0F0F0F0F0F0FL;
         x = x + (x >>> 8);
         x = x + (x >>> 16);
         x = x + (x >>> 32);
         return x & 0x7FL;
     }
 
     public static BitVector parseString(String binaryString) {
         int n = binaryString.length();
         BitVector v = new BitVector(n);
         for (int i = 0; i < n; ++i) {
             v.set(i, binaryString.charAt(n - i - 1) == '1');
         }
         return v;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (!(obj instanceof BitVector))
             return false;
 
         BitVector other = BitVector.class.cast(obj);
         if (this.size != other.size)
             return false;
 
         int numFilledBlocks = (int) (this.size / B);
         int i = 0;
         for (; i < numFilledBlocks; ++i) {
             if (this.block[i] != other.block[i])
                 return false;
         }
         // Apply mask for flanking bits
         if (i < this.block.length) {
             long offset = this.size % B;
             long mask = ~(~0L << offset);
             if ((this.block[i] & mask) != (other.block[i] & mask))
                 return false;
         }
 
         return true;
     }
 
     /**
      * Return string representation of this bit vector. LSB is left
      * 
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
         StringBuilder buf = new StringBuilder();
         for (long i = size - 1; i >= 0; --i)
             buf.append(get(i) ? "1" : "0");
         return buf.toString();
     }
 
     public DataOutputStream saveTo(DataOutputStream out) throws IOException {
         out.writeLong(size);
         out.writeInt(block.length);
         for (int i = 0; i < block.length; ++i)
             out.writeLong(block[i]);
         return out;
     }
 
     public static BitVector loadFrom(DataInputStream in) throws IOException {
         long size = in.readLong();
         int blockSize = in.readInt();
         long[] block = new long[blockSize];
         for (int i = 0; i < blockSize; ++i)
             block[i] = in.readLong();
         BitVector v = new BitVector(size, block);
         return v;
     }
 
 }
