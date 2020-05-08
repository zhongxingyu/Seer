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
 
 import static org.lbzip2.impl.Constants.MAX_CODE_LENGTH;
 import static org.lbzip2.impl.MtfDecoder.CMAP_BASE;
 import static org.lbzip2.impl.PrefixDecoder.EOB;
 import static org.lbzip2.impl.PrefixDecoder.HUFF_START_WIDTH;
 import static org.lbzip2.impl.PrefixDecoder.RUN_A;
 import static org.lbzip2.impl.Status.MORE;
 import static org.lbzip2.impl.Status.OK;
 import static org.lbzip2.impl.Unsigned.uge;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 import org.lbzip2.StreamFormatException;
 
 /**
  * Retriever parses and decodes <em>bz2</em> streams into internal structures.
  * 
  * @author Mikolaj Izdebski
  */
 class Retriever
 {
     /**
      * FSM states from which retriever can be started or resumed.
      */
     enum State
     {
         /**
          * State indicating that retriever was just initialized.
          */
         S_INIT,
 
         /**
          * State indicating that retriever was stalled while reading BWT primary index.
          */
         S_BWT_IDX,
 
         /**
          * State indicating that retriever was stalled while reading character map.
          */
         S_BITMAP,
 
         /**
          * State indicating that retriever was stalled while reading tree selector MTF value.
          */
         S_SELECTOR_MTF,
 
         /**
          * State indicating that retriever was stalled while reading tree delta code.
          */
         S_DELTA_TAG,
 
         /**
          * State indicating that retriever was stalled while reading symbol prefix code.
          */
         S_PREFIX,
     };
 
     public Retriever()
     {
         for ( int t = 0; t < 6; t++ )
             tree[t] = new PrefixDecoder();
     }
 
     public void setMbs( int mbs )
     {
         this.mbs = mbs;
     }
 
     /**
      * Current state of retriever FSA.
      */
     private State m_state = State.S_INIT;
 
     /**
      * Coding trees.
      */
     private final PrefixDecoder[] tree = new PrefixDecoder[6];
 
     private PrefixDecoder pd;
 
     /**
      * Maximal block size (100k-900k in 100k steps).
      */
     private int mbs;
 
     /**
      * Alphabet size (number of distinct prefix codes, 3-258).
      */
     private int as;
 
     /**
      * Number of prefix trees used for current block (2-6).
      */
     private int nt;
 
     /**
      * Number of selectors used (1-32767).
      */
     private int ns;
 
     /**
      * Coding tree selector MTF values.
      */
     private final byte[] sel = new byte[32767];
 
     /**
      * Current state of inverse MTF FSA.
      */
     private final MtfDecoder mtf = new MtfDecoder();
 
     /**
      * General purpose index.
      */
     private int m_i;
 
     /**
      * Current group number.
      */
     private int m_g;
 
     private final int[] m_len = new int[259];
 
     /**
      * Current tree number.
      */
     private int m_t;
 
     private int m_r;
 
     private int m_h;
 
     private int m_c;
 
     private final int[] m_mtf = new int[6];
 
     private int m_need;
 
     /* Decode a single prefix code. */
     private short get_sym( BitStream bs )
         throws StreamFormatException, IOException
     {
         int x = pd.start[bs.peek( HUFF_START_WIDTH )];
         int k = x & 0x1F;
         short s;
 
         if ( k <= HUFF_START_WIDTH )
         {
             /* Use look-up table in average case. */
             s = (short) ( x >> 5 );
         }
         else
         {
             /*
              * Code length exceeds HUFF_START_WIDTH, use canonical prefix decoding algorithm instead of look-up table.
              */
             while ( uge( bs.buff, pd.base[k + 1] ) )
                 k++;
             s = pd.perm[pd.count[k] + (int) ( ( bs.buff - pd.base[k] ) >>> ( 64 - k ) )];
         }
 
         bs.dump( k );
         return s;
     }
 
     /* Retrieve block. */
     Status retr( Decoder ds, BitStream bs )
         throws StreamFormatException, IOException
     {
         int off = bs.off;
         final int len = bs.len;
         final byte[] buf = bs.ptr;
 
         while ( bs.live < m_need && off < len )
         {
             bs.live += 8;
             bs.buff += ( buf[off++] & 0xFFL ) << ( 64 - bs.live );
         }
         if ( bs.live < m_need )
         {
             bs.off = off;
             return MORE;
         }
 
         switch ( m_state )
         {
             default:
                 m_need = 1 + 24 + 16 + 16;
                 while ( bs.live < m_need && off < len )
                 {
                     bs.live += 8;
                     bs.buff += ( buf[off++] & 0xFFL ) << ( 64 - bs.live );
                 }
                 if ( bs.live < m_need )
                 {
                     m_state = State.S_BWT_IDX;
                     bs.off = off;
                     return MORE;
                 }
             case S_BWT_IDX:
                 ds.rand = bs.take( 1 ) != 0;
                 ds.bwt_idx = bs.take( 24 );
 
                 /* Retrieve bitmap. */
                 m_r = bs.take( 16 ) << 16;
                 as = 0;
                 mtf.initialize();
                 m_i = 0;
             case S_BITMAP:
                 while ( m_i < 256 )
                 {
                     if ( m_r < 0 )
                     {
                         int s = bs.take( 16 ) << 16;
                         for ( int j = 0; j < 16; j++ )
                         {
                             if ( s < 0 )
                                 mtf.imtf_slide[CMAP_BASE + as++] = (byte) ( m_i + j );
                             s *= 2;
                         }
                     }
                     m_i += 16;
                     m_r *= 2;
                     m_need = 3 + 15 + 6;
                     while ( bs.live < m_need && off < len )
                     {
                         bs.live += 8;
                         bs.buff += ( buf[off++] & 0xFFL ) << ( 64 - bs.live );
                     }
                     if ( bs.live < m_need )
                     {
                         m_state = State.S_BITMAP;
                         bs.off = off;
                         return MORE;
                     }
                 }
                 if ( as == 0 )
                    throw new StreamFormatException( "XXX" );
                 as += 2;
 
                 nt = bs.take( 3 );
                 if ( nt < 2 || nt > 6 )
                    throw new StreamFormatException( "XXX" );
                 ns = bs.take( 15 );
 
                 m_g = 0;
             case S_SELECTOR_MTF:
                 for ( ; m_g < ns; )
                 {
                     sel[m_g] = 0;
                     while ( sel[m_g] < nt && bs.take( 1 ) != 0 )
                         sel[m_g]++;
                     if ( sel[m_g] == nt )
                        throw new StreamFormatException( "XXX" );
                     m_g++;
                     m_need = 5 + 1 + 1;
                     while ( bs.live < m_need && off < len )
                     {
                         bs.live += 8;
                         bs.buff += ( buf[off++] & 0xFFL ) << ( 64 - bs.live );
                     }
                     if ( bs.live < m_need )
                     {
                         m_state = State.S_SELECTOR_MTF;
                         bs.off = off;
                         return MORE;
                     }
                 }
                 if ( ns > 18001 )
                     ns = 18001;
 
                 m_t = 0;
                 m_i = 0;
                 m_len[0] = (byte) bs.take( 5 );
             case S_DELTA_TAG:
                 for ( ;; )
                 {
                     if ( bs.take( 1 ) != 0 )
                     {
                         m_len[m_i] += 1 - 2 * bs.take( 1 );
                         if ( m_len[m_i] < 1 || m_len[m_i] > 20 )
                            throw new StreamFormatException( "XXX" );
                     }
                     else
                     {
                         m_len[m_i + 1] = m_len[m_i];
                         if ( ++m_i >= as )
                         {
                             tree[m_t].make_tree( m_len, as );
                             if ( ++m_t >= nt )
                                 break;
                             m_i = 0;
                             m_len[0] = (byte) bs.take( 5 );
                         }
                     }
                     m_need = 1 + MAX_CODE_LENGTH;
                     while ( bs.live < m_need && off < len )
                     {
                         bs.live += 8;
                         bs.buff += ( buf[off++] & 0xFFL ) << ( 64 - bs.live );
                     }
                     if ( bs.live < m_need )
                     {
                         m_state = State.S_DELTA_TAG;
                         bs.off = off;
                         return MORE;
                     }
                 }
 
                 /* Retrieve block MTF values and apply IMTF transformation. */
                 m_g = 0;
                 ds.block_size = m_r = m_h = 0;
                 Arrays.fill( ds.ftab, 0 );
                 m_c = mtf.imtf_slide[mtf.imtf_row[0]] & 0xFF;
                 for ( int i = 0; i < 6; i++ )
                     m_mtf[i] = i;
                 m_i = 0;
             case S_PREFIX:
                 for ( ;; )
                 {
                     if ( m_i == 0 )
                     {
                         if ( m_g++ >= ns )
                            throw new StreamFormatException( "XXX" );
                         int i = sel[m_g - 1];
                         int t = m_mtf[i];
                         while ( i-- > 0 )
                             m_mtf[i + 1] = m_mtf[i];
                         m_mtf[0] = t;
                         pd = tree[t];
                         if ( pd.error != null )
                             throw new StreamFormatException( pd.error );
                         m_i = 50;
                     }
                     int s = get_sym( bs );
                     if ( s >= RUN_A )
                     {
                         m_r += 1 << ( m_h + s - RUN_A );
                         m_h++;
                         if ( m_r < 0 )
                            throw new StreamFormatException( "XXX" );
                     }
                     else
                     {
                         int r = m_r;
                         if ( ds.block_size + r > mbs )
                            throw new StreamFormatException( "XXX" );
                         ds.ftab[m_c] += r;
                         while ( r-- != 0 )
                             ds.tt[ds.block_size++] = m_c;
                         if ( s == EOB )
                         {
                             m_state = State.S_INIT;
                             bs.off = off;
                             return OK;
                         }
                         m_c = mtf.mtf_one( s );
                         m_h = 0;
                         m_r = 1;
                     }
                     m_i--;
                     m_need = MAX_CODE_LENGTH;
                     while ( bs.live < m_need && off < len )
                     {
                         bs.live += 8;
                         bs.buff += ( buf[off++] & 0xFFL ) << ( 64 - bs.live );
                     }
                     if ( bs.live < m_need )
                     {
                         m_state = State.S_PREFIX;
                         bs.off = off;
                         return MORE;
                     }
                 }
         }
     }
 }
