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
 
 import static org.lbzip2.impl.Parser.State.ACCEPT;
 import static org.lbzip2.impl.Parser.State.BLOCK_CRC_1;
 import static org.lbzip2.impl.Parser.State.BLOCK_CRC_2;
 import static org.lbzip2.impl.Parser.State.BLOCK_MAGIC_1;
 import static org.lbzip2.impl.Parser.State.BLOCK_MAGIC_2;
 import static org.lbzip2.impl.Parser.State.BLOCK_MAGIC_3;
 import static org.lbzip2.impl.Parser.State.EOS_2;
 import static org.lbzip2.impl.Parser.State.EOS_3;
 import static org.lbzip2.impl.Parser.State.EOS_CRC_1;
 import static org.lbzip2.impl.Parser.State.EOS_CRC_2;
 import static org.lbzip2.impl.Parser.State.STREAM_MAGIC_1;
 import static org.lbzip2.impl.Parser.State.STREAM_MAGIC_2;
 import static org.lbzip2.impl.Status.FINISH;
 import static org.lbzip2.impl.Status.MORE;
 import static org.lbzip2.impl.Status.OK;
 
 import org.lbzip2.StreamFormatException;
 
 /**
  * @author Mikolaj Izdebski
  */
 class Parser
 {
     enum State
     {
         STREAM_MAGIC_1,
         STREAM_MAGIC_2,
         BLOCK_MAGIC_1,
         BLOCK_MAGIC_2,
         BLOCK_MAGIC_3,
         BLOCK_CRC_1,
         BLOCK_CRC_2,
         EOS_2,
         EOS_3,
         EOS_CRC_1,
         EOS_CRC_2,
         ACCEPT,
     }
 
     State state;
 
     int bs100k;
 
     int stored_crc;
 
     int computed_crc;
 
     public Parser( int my_bs100k )
     {
         state = BLOCK_MAGIC_1;
         bs100k = my_bs100k;
         computed_crc = 0;
     }
 
     /*
      * Parse stream headers until a compressed block or end of stream is reached. Possible return codes: OK - a
      * compressed block was found FINISH - end of stream was reached MORE - more input is need, parsing was suspended
      * ERR_HEADER - invalid stream header ERR_STRMCRC - stream CRC does not match ERR_EOF - unterminated stream (EOF
      * reached before end of stream) garbage is set only when returning FINISH. It is number of garbage bits consumed
      * after end of stream was reached.
      */
     Status parse( Header hd, BitStream bs, int[] garbage )
         throws StreamFormatException
     {
         assert ( state != ACCEPT );
 
         while ( OK == bs.need( 16 ) )
         {
             int word = bs.peek( 16 );
 
             bs.dump( 16 );
 
             switch ( state )
             {
                 case STREAM_MAGIC_1:
                     if ( 0x425A != word )
                     {
                         hd.bs100k = -1;
                         hd.crc = 0;
                         state = ACCEPT;
                         garbage[0] = 16;
                         return FINISH;
                     }
                     state = STREAM_MAGIC_2;
                     continue;
 
                 case STREAM_MAGIC_2:
                     if ( 0x6839 < word || 0x6831 > word )
                     {
                         hd.bs100k = -1;
                         hd.crc = 0;
                         state = ACCEPT;
                         garbage[0] = 32;
                         return FINISH;
                     }
                     bs100k = word & 0xF;
                     state = BLOCK_MAGIC_1;
                     continue;
 
                 case BLOCK_MAGIC_1:
                     if ( 0x1772 == word )
                     {
                         state = EOS_2;
                         continue;
                     }
                     if ( 0x3141 != word )
                         throw new StreamFormatException( "ERR_HEADER" );
                     state = BLOCK_MAGIC_2;
                     continue;
 
                 case BLOCK_MAGIC_2:
                     if ( 0x5926 != word )
                         throw new StreamFormatException( "ERR_HEADER" );
                     state = BLOCK_MAGIC_3;
                     continue;
 
                 case BLOCK_MAGIC_3:
                     if ( 0x5359 != word )
                         throw new StreamFormatException( "ERR_HEADER" );
                     state = BLOCK_CRC_1;
                     continue;
 
                 case BLOCK_CRC_1:
                     stored_crc = word;
                     state = BLOCK_CRC_2;
                     continue;
 
                 case BLOCK_CRC_2:
                     hd.crc = ( stored_crc << 16 ) | word;
                     hd.bs100k = bs100k;
                    computed_crc = ( computed_crc << 1 ) ^ ( computed_crc >> 31 ) ^ hd.crc;
                     state = BLOCK_MAGIC_1;
                     return OK;
 
                 case EOS_2:
                     if ( 0x4538 != word )
                         throw new StreamFormatException( "ERR_HEADER" );
                     state = EOS_3;
                     continue;
 
                 case EOS_3:
                     if ( 0x5090 != word )
                         throw new StreamFormatException( "ERR_HEADER" );
                     state = EOS_CRC_1;
                     continue;
 
                 case EOS_CRC_1:
                     stored_crc = word;
                     state = EOS_CRC_2;
                     continue;
 
                 case EOS_CRC_2:
                     stored_crc = ( stored_crc << 16 ) | word;
                     if ( stored_crc != computed_crc )
                         throw new StreamFormatException( "ERR_STRMCRC" );
                     computed_crc = 0;
                     bs.align();
                     state = STREAM_MAGIC_1;
                     continue;
 
                 default:
                     break;
             }
 
             throw new IllegalStateException();
         }
 
         if ( FINISH != bs.need( 16 ) )
             return MORE;
 
         if ( state == STREAM_MAGIC_1 )
         {
             state = ACCEPT;
             garbage[0] = 0;
             return FINISH;
         }
         if ( state == STREAM_MAGIC_2 )
         {
             state = ACCEPT;
             garbage[0] = 16;
             return FINISH;
         }
 
         throw new StreamFormatException( "ERR_EOF" );
     }
 }
