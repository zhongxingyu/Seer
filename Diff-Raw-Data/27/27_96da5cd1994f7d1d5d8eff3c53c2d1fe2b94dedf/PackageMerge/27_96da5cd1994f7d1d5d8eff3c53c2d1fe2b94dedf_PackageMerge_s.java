 /*-
  * Copyright (c) 2014 Mikolaj Izdebski
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
 
 /**
  * This class is an implementation of the Package-Merge algorithm for finding an optimal length-limited prefix-free code
  * set.
  * 
  * @author Mikolaj Izdebski
  */
 class PackageMerge
     implements PrefixCoder
 {
     /**
      * {@code ceil(log2(MAX_ALPHA_SIZE))}
      */
     private static final int BITS_PER_SYMBOL = 9;
 
     /**
      * {@code floor(64 / BITS_PER_SYMBOL)}
      */
     private static final int SYMBOLS_PER_WORD = 7;
 
     /**
      * {@code ceil(MAX_CODE_LENGTH / SYMBOLS_PER_WORD)}
      */
     private static final int VECTOR_SIZE = 3;
 
     /**
      * It can be easily proved by induction that number of elements stored in the queue is always stricly less elements
      * than alphabet size.
      */
     private static final int QUEUE_SIZE = MAX_ALPHA_SIZE - 1;
 
     /**
      * Internal node weights.
      */
     private final long[] W = new long[2 * QUEUE_SIZE];
 
     private final long[][] P = new long[2 * QUEUE_SIZE][VECTOR_SIZE];
 
     public void build_tree( int[] C, long[] Pr, int n )
     {
         int x; /* number of unprocessed singleton nodes */
         int i; /* general purpose index */
         int k; /* vector index */
         int d; /* current node depth */
         int jP; /* current index in queue P */
         int jL; /* current index in queue L */
         int szP; /* current size of queue P */
         int iP; /* current offset of head of queue P */
         long dw; /* symbol weight at current depth */
 
         iP = MAX_CODE_LENGTH % 2 * ( MAX_ALPHA_SIZE - 1 );
         szP = 0;
 
         d = VECTOR_SIZE - 1;
         dw = 1L << ( MAX_CODE_LENGTH % SYMBOLS_PER_WORD * BITS_PER_SYMBOL );
         for ( ;; )
         {
             dw >>>= BITS_PER_SYMBOL;
             int dwm = ( dw == 0 ) ? -1 : 0;
             d += dwm;
             if ( d + 1 == 0 )
                 break;
             dw += dwm & ( 1L << ( ( SYMBOLS_PER_WORD - 1 ) * BITS_PER_SYMBOL ) );
 
             x = n;
             jP = iP;
             iP ^= MAX_ALPHA_SIZE - 1;
             jL = iP;
 
             for ( jL = iP; x + szP > 1; jL++ )
             {
                 if ( szP == 0 || ( x > 1 && Pr[x - 2] < W[jP] ) )
                 {
                     W[jL] = Pr[x - 1] + Pr[x - 2];
                     for ( k = 0; k < VECTOR_SIZE; k++ )
                         P[jL][k] = 0;
                     P[jL][d] += 2 * dw;
                     x -= 2;
                 }
                 else if ( x == 0 || ( szP > 1 && W[jP + 1] <= Pr[x - 1] ) )
                 {
                     W[jL] = W[jP] + W[jP + 1];
                     for ( k = 0; k < VECTOR_SIZE; k++ )
                         P[jL][k] = P[jP][k] + P[jP + 1][k];
                     jP += 2;
                     szP -= 2;
                 }
                 else
                 {
                     W[jL] = W[jP] + Pr[x - 1];
                     for ( k = 0; k < VECTOR_SIZE; k++ )
                         P[jL][k] = P[jP][k];
                     P[jL][d] += dw;
                     jP++;
                     x--;
                     szP--;
                 }
             }
 
             szP = jL - iP;
             assert ( szP >= n / 2 );
             assert ( szP < n );
         }
         assert ( iP == 0 );
         assert ( szP == n - 1 );
 
         k = VECTOR_SIZE * SYMBOLS_PER_WORD;
         for ( i = VECTOR_SIZE; i-- != 0; )
         {
            dw = P[szP - 1][i];
             for ( d = SYMBOLS_PER_WORD * BITS_PER_SYMBOL; d != 0; )
                C[k--] = (int) ( dw >>> ( d -= BITS_PER_SYMBOL ) ) & 0x1ff;
         }
         C[0] = 0;
     }
 }
