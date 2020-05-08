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
 
 /**
  * Inverse Move-To-Front transformation.
  * <p>
  * This class is an implementation of Sliding Lists algorithm for doing Inverse Move-To-Front (IMTF) transformation in
  * {@code O(n)} space and amortized {@code O(sqrt(n))} time. The naive IMTF algorithm does the same in both {@code O(n)}
  * space and time.
  * <p>
  * IMTF could be done in {@code O(log(n))} time using algorithms based on (quite) complex data structures such as
  * self-balancing binary search trees, but these algorithms have quite big constant factor which makes them impractical
  * for MTF of 256 items.
  * 
 * @author MikolajIzdebski
  */
 class MtfDecoder
 {
     static final int ROW_WIDTH = 16;
 
     static final int SLIDE_LENGTH = 8192;
 
     static final int NUM_ROWS = 256 / ROW_WIDTH;
 
     static final int CMAP_BASE = SLIDE_LENGTH - 256;
 
     int[] imtf_row;
 
     byte[] imtf_slide;
 
     public MtfDecoder()
     {
         this.imtf_row = new int[NUM_ROWS];
         this.imtf_slide = new byte[SLIDE_LENGTH];
     }
 
     /**
      * Initialize IMTF decoding structure.
      */
     void initialize()
     {
         for ( int i = 0; i < NUM_ROWS; i++ )
             imtf_row[i] = CMAP_BASE + i * ROW_WIDTH;
     }
 
     /**
      * Transform a single byte.
      * 
      * @param c byte to be transformed
      * @return transformation result
      */
     byte mtf_one( byte c )
     {
         int pp;
 
         /* We expect the index to be small, so we have a special case for that. */
         int nn = c & 0xff;
         if ( nn < ROW_WIDTH )
         {
 
             pp = imtf_row[0];
             c = imtf_slide[pp + nn];
 
             while ( nn > 0 )
             {
                 imtf_slide[pp + nn] = imtf_slide[pp + nn - 1];
                 nn--;
             }
         }
 
         /* A general case for indices >= ROW_WIDTH. */
         else
         {
             /*
              * If the sliding list already reached the bottom of memory pool allocated for it, we need to rebuild it.
              */
             if ( imtf_row[0] == 0 )
             {
                 int kk = SLIDE_LENGTH;
                 int rr = NUM_ROWS;
                 while ( rr > 0 )
                 {
                     int bg = imtf_row[--rr];
                     int bb = bg + ROW_WIDTH;
 
                     assert ( bg >= 0 && bb <= SLIDE_LENGTH );
 
                     while ( bb > bg )
                         imtf_slide[--kk] = imtf_slide[--bb];
                     imtf_row[rr] = kk;
                 }
             }
 
             int lno = ( c >> 4 ) & 0xf;
             int bb = imtf_row[lno];
 
             pp = bb + ( c & 0xf );
             c = imtf_slide[pp];
 
             while ( pp > bb )
             {
                 int tt = pp--;
                 imtf_slide[tt] = imtf_slide[pp];
             }
 
             while ( lno > 0 )
             {
                 int lno1 = lno;
                 pp = --imtf_row[--lno];
                 imtf_slide[imtf_row[lno1]] = imtf_slide[pp + ROW_WIDTH];
             }
         }
 
         imtf_slide[pp] = c;
         return c;
     }
 }
