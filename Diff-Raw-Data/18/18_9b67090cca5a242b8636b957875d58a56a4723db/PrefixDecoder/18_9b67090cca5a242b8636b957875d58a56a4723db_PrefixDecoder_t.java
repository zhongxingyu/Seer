 /*-
  * Copyright (c) 2013 Mikolaj Izdebski
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.lbzip2.impl;
 
 import static org.lbzip2.impl.Constants.MAX_ALPHA_SIZE;
 import static org.lbzip2.impl.Constants.MAX_CODE_LENGTH;
 import static org.lbzip2.impl.Constants.MIN_CODE_LENGTH;
 import static org.lbzip2.impl.Unsigned.uge;
 import static org.lbzip2.impl.Unsigned.ult;
 
 import org.lbzip2.StreamFormatException;
 
 /**
  * Decodes canonical prefix code.
  * <p>
  * Notes on prefix code decoding:
  * <ol>
  * <li>Width of a tree node is defined as 2<sup>-d</sup>, where {@code d} is depth of that node. A prefix tree is said
  * to be complete iff all leaf widths sum to 1. If this sum is less (greater) than 1, we say the tree is incomplete
  * (oversubscribed). See also: Kraft's inequality.
  * <li>In this implementation, malformed trees (oversubscribed or incomplete) aren't rejected directly at creation
  * (that's the moment when both bad cases are detected). Instead, invalid trees cause decode error only when they are
  * actually used to decode a group. This is nonconforming behavior &ndash; the original <em>bzip2</em>, which serves as
  * a reference implementation, accepts malformed trees as long as nonexistent codes don't appear in compressed stream.
  * Neither bzip2 nor any alternative implementation I know produces such trees, so this behavior seems sane.
  * <li>When held in variables, codes are usually in left-justified form, meaning that they occupy consecutive most
  * significant bits of the variable they are stored in, while less significant bits of variable are padded with zeroes.
  * <p>
  * Such form allows for easy lexicographical comparison of codes using unsigned arithmetic comparison operators, without
  * the need for normalization.
  * 
  * @author Mikolaj Izdebski
  */
 class PrefixDecoder
 {
     /**
      * Number of bits in lookup table.
      * <p>
      * Prefix code decoding is performed using a multilevel table lookup. The fastest way to decode is to simply build a
      * lookup table whose size is determined by the longest code. However, the time it takes to build this table can
      * also be a factor if the data being decoded is not very long. The most common codes are necessarily the shortest
      * codes, so those codes dominate the decoding time, and hence the speed. The idea is you can have a shorter table
      * that decodes the shorter, more probable codes, and then point to subsidiary tables for the longer codes. The time
      * it costs to decode the longer codes is then traded against the time it takes to make longer tables.
      * <p>
      * This result of this trade are in this constant. {@code HUFF_START_WIDTH} is the number of bits the first level
     * table can decode in one step. Subsequent tables always decode one bit at time. The current value of {@code
      * HUFF_START_WIDTH} was determined with a series of benchmarks. The optimum value may differ though from machine to
      * machine, and possibly even between compilers. Your mileage may vary.
      */
     static final int HUFF_START_WIDTH = 10;
 
     /**
      * Decoding start point.
      * <p>
      * {@code k = start[c] & 0x1F} is code length. If {@code k <= HUFF_START_WIDTH} then {@code s = start[c] >> 5} is
      * the immediate symbol value. If {@code k > HUFF_START_WIDTH} then {@code s} is undefined, but code starting with
      * {@code c} is guaranteed to be at least {@code k} bits long.
      */
     final short[] start;
 
     /**
      * Base codes.
      * <p>
     * For {@code k} in 1..20, {@code base[k]} is either the first code of length {@code k} or it is equal to {@code
     * base[k+1]} if there are no codes of length {@code k}. The other 2 elements are sentinels: {@code base[0]} is
     * always zero, {@code base[MAX_CODE_LENGTH+1]} is plus infinity (represented as {@code -1L}).
      */
     final long[] base;
 
     /**
      * cumulative code length counts.
      * <p>
      * For {@code k} in 1..20, {@code count[k]} is the number of symbols which codes are shorter than {@code k} bits;
      * {@code count[0]} is a sentinel (always zero).
      */
     final int[] count;
 
     /**
      * Sorting permutation.
      * <p>
      * The rules of canonical prefix coding require that the source alphabet is sorted stably by ascending code length
      * (the order of symbols of the same code length is preserved). The {@code perm} table holds the sorting
      * permutation.
      */
     final short[] perm;
 
     public PrefixDecoder()
     {
         this.start = new short[1 << HUFF_START_WIDTH];
         this.base = new long[MAX_CODE_LENGTH + 2];
         this.count = new int[MAX_CODE_LENGTH + 1];
         this.perm = new short[MAX_ALPHA_SIZE];
     }
 
     /*
      * Internal symbol values differ from that used in bzip2! 257 - RUN-A 258 - RUN-B 1-255 - MTFV 0 - EOB
      */
     static final int RUN_A = 256 + 1;
 
     static final int RUN_B = 256 + 2;
 
     static final int EOB = 0;
 
     /**
      * Given a list of code lengths, make a set of tables to decode that set of codes.
      * <p>
      * Because the alphabet size is always less or equal to 258 (2 RUN symbols, at most 255 MFV values and 1 EOB symbol)
      * the average code length is strictly less than 9. Hence the probability of decoding code longer than 10 bits is
      * quite small (usually < 0.2).
      * <p>
      * lbzip2 utilizes this fact by implementing a hybrid algorithm for prefix decoding. For codes of length <= 10
      * lbzip2 maintains a LUT (look-up table) that maps codes directly to corresponding symbol values. Codes longer than
      * 10 bits are not mapped by the LUT are decoded using cannonical prefix decoding algorithm.
      * <p>
      * The above value of 10 bits was determined using a series of benchmarks. It's not hardcoded but instead it is
      * defined as a constant {@code HUFF_START_WIDTH}. If on some system a different value works better, it can be
      * adjusted freely.
      * 
      * @param L code lengths
      * @param n alphabet size
      * @throws StreamFormatException if given code set is incomplete or invalid
      */
     void make_tree( int[] L, int n )
         throws StreamFormatException
     {
         int[] C; /* code length count; C[0] is a sentinel */
         long[] B; /* left-justified base */
         short[] P; /* symbols sorted by code length */
         short[] S; /* lookup table */
 
         int k; /* current code length */
         int s; /* current symbol */
         int cum;
         int code;
         long sofar;
         long next;
         long inc;
         int v;
 
         /* Initialize constants. */
         C = this.count;
         B = this.base;
         P = this.perm;
         S = this.start;
 
         /* Count symbol lengths. */
         for ( k = 0; k <= MAX_CODE_LENGTH; k++ )
             C[k] = 0;
         for ( s = 0; s < n; s++ )
         {
             k = L[s];
             C[k]++;
         }
         /* Make sure there are no zero-length codes. */
         assert ( C[0] == 0 );
 
         /* Check if Kraft's inequality is satisfied. */
         sofar = 0;
         for ( k = MIN_CODE_LENGTH; k <= MAX_CODE_LENGTH; k++ )
             sofar += (long) C[k] << ( MAX_CODE_LENGTH - k );
         if ( sofar != ( 1 << MAX_CODE_LENGTH ) )
         {
             if ( ult( sofar, 1 << MAX_CODE_LENGTH ) )
                 throw new StreamFormatException( "Incomplete prefix code" );
             else
                 throw new StreamFormatException( "Oversubscribed prefix code" );
         }
 
         /* Create left-justified base table. */
         sofar = 0;
         for ( k = MIN_CODE_LENGTH; k <= MAX_CODE_LENGTH; k++ )
         {
             next = sofar + ( (long) C[k] << ( 64 - k ) );
             assert ( next == 0 || uge( next, sofar ) );
             B[k] = sofar;
             sofar = next;
         }
         /* Ensure that "sofar" has overflowed to zero. */
         assert ( sofar == 0 );
 
         /*
          * The last few entries of lj-base may have overflowed to zero, so replace all trailing zeros with the greatest
          * possible 64-bit value (which is greater than the greatest possible left-justified base).
          */
         assert ( k == MAX_CODE_LENGTH + 1 );
        do
         {
             assert ( k > MIN_CODE_LENGTH );
            assert ( k > MAX_CODE_LENGTH || B[k] == 0 );
             B[k--] = -1;
         }
        while ( C[k] == 0 );
 
         /* Transform counts into indices (cumulative counts). */
         cum = 0;
         for ( k = MIN_CODE_LENGTH; k <= MAX_CODE_LENGTH; k++ )
         {
             int t1 = C[k];
             C[k] = cum;
             cum += t1;
         }
         assert ( cum == n );
 
         /* Perform counting sort. */
         P[C[L[0]]++] = RUN_A;
         P[C[L[1]]++] = RUN_B;
         for ( s = 2; s < n - 1; s++ )
             P[C[L[s]]++] = (short) ( s - 1 );
         P[C[L[n - 1]]++] = EOB;
 
         /* Create first, complete start entries. */
         code = 0;
         inc = 1 << ( HUFF_START_WIDTH - 1 );
         for ( k = 1; k <= HUFF_START_WIDTH; k++ )
         {
             for ( s = C[k - 1]; s < C[k]; s++ )
             {
                 short x = (short) ( ( P[s] << 5 ) | k );
                 v = code;
                 code += inc;
                 while ( v < code )
                     S[v++] = x;
             }
             inc >>= 1;
         }
 
         /* Fill remaining, incomplete start entries. */
         assert ( k == HUFF_START_WIDTH + 1 );
         sofar = (long) code << ( 64 - HUFF_START_WIDTH );
         while ( code < ( 1 << HUFF_START_WIDTH ) )
         {
             while ( uge( sofar, B[k + 1] ) )
                 k++;
             S[code] = (short) k;
             code++;
             sofar += 1L << ( 64 - HUFF_START_WIDTH );
         }
         assert ( sofar == 0 );
 
         /*
          * Restore cumulative counts as they were destroyed by the sorting phase. The sentinel wasn't touched, so there
          * is no need to restore it.
          */
         for ( k = MAX_CODE_LENGTH; k > 0; k-- )
             C[k] = C[k - 1];
         assert ( C[0] == 0 );
     }
 }
