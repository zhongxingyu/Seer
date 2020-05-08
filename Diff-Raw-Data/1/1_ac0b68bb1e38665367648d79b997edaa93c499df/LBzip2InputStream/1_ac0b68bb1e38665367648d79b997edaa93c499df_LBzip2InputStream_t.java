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
 
 import static org.lbzip2.impl.Status.FINISH;
 import static org.lbzip2.impl.Status.MORE;
 import static org.lbzip2.impl.Status.OK;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.lbzip2.StreamFormatException;
 
 /**
  * @author Mikolaj Izdebski
  */
 public class LBzip2InputStream
     extends InputStream
 {
     private final InputStream is;
 
     private static final int BUFSIZ = 65536;
 
     private final byte[] buf1 = new byte[1];
 
     private final byte[] in_buf = new byte[BUFSIZ];
 
     private boolean emit;
 
     private final BitStream bitStream = new BitStream();
 
     private Parser parser;
 
     private final Retriever retriever = new Retriever();
 
     private final Header hd = new Header();
 
     private final Decoder decoder = new Decoder();
 
     private boolean done;
 
     public LBzip2InputStream( InputStream is )
     {
         this.is = is;
 
         bitStream.ptr = in_buf;
     }
 
     @Override
     public int read()
         throws IOException
     {
         if ( read( buf1, 0, 1 ) < 0 )
             return -1;
         return buf1[0] & 0xFF;
     }
 
     @Override
     public int read( byte[] buf, int off, int len )
         throws IOException
     {
         if ( done )
             return -1;
 
         int origLen = len;
         while ( len > 0 )
         {
             if ( emit )
             {
                 int[] buf_sz = new int[1];
                 buf_sz[0] = len;
                 if ( decoder.emit( buf, off, buf_sz ) == OK )
                 {
                     emit = false;
                     if ( hd.crc != decoder.crc )
                         throw new StreamFormatException( "Invalid block CRC" );
                 }
                 len = buf_sz[0];
                off += origLen - len;
             }
             else
             {
                 int[] garbage = new int[1];
 
                 if ( parser == null )
                 {
                     int bs100k;
                     if ( is.read() != 0x42 || is.read() != 0x5A || is.read() != 0x68
                         || ( bs100k = ( ( is.read() - 0x31 ) & 0xFF ) + 1 ) > 9 )
                         throw new StreamFormatException( "Not a bz2 file" );
                     parser = new Parser( bs100k );
                 }
 
                 Status s;
                 while ( ( s = parser.parse( hd, bitStream, garbage ) ) == MORE )
                 {
                     readMoreInput();
                 }
                 if ( s == FINISH )
                 {
                     finish();
                     return origLen > len ? origLen - len : -1;
                 }
                 assert s == OK;
 
                 retriever.setMbs( hd.bs100k * 100000 );
                 while ( ( s = retriever.retr( decoder, bitStream ) ) == MORE )
                 {
                     readMoreInput();
                 }
                 assert s == OK;
 
                 decoder.decode();
                 emit = true;
             }
         }
 
         return origLen;
     }
 
     private void readMoreInput()
         throws IOException
     {
         int r = is.read( in_buf );
         if ( r < 0 )
             bitStream.eof = true;
         else
         {
             bitStream.off = 0;
             bitStream.len = r;
         }
     }
 
     private void finish()
     {
         done = true;
     }
 
     @Override
     public void close()
         throws IOException
     {
         finish();
     }
 
     public static void main( String[] args )
     {
         try
         {
             InputStream zis = new LBzip2InputStream( System.in );
 
             byte[] buf = new byte[4096];
             int r;
             while ( ( r = zis.read( buf ) ) != -1 )
                 System.out.write( buf, 0, r );
 
             zis.close();
         }
         catch ( IOException e )
         {
             e.printStackTrace();
         }
     }
 }
