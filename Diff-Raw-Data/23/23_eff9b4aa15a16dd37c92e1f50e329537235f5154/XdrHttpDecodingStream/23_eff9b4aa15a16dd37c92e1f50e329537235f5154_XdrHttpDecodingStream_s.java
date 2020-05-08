 /*
  * $Header$
  *
  * Copyright (c) 1999, 2000
  * Lehrstuhl fuer Prozessleittechnik (PLT), RWTH Aachen
  * D-52064 Aachen, Germany.
  * All rights reserved.
  *
  * This library is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Library General Public License as
  * published by the Free Software Foundation; either version 2 of the
  * License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Library General Public License for more details.
  *
  * You should have received a copy of the GNU Library General Public
  * License along with this program (see the file COPYING.LIB for more
  * details); if not, write to the Free Software Foundation, Inc.,
  * 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 
 package org.acplt.oncrpc;
 
 import java.io.*;
 import java.net.*;
 import org.acplt.oncrpc.web.HttpClientConnection;
 import org.acplt.oncrpc.web.HttpTunnelConstants;
 import org.acplt.oncrpc.web.Base64;
 
 /**
  * The <code>XdrHttpDecodingStream</code> class provides the necessary
  * functionality to {@link XdrDecodingStream} to receive XDR data through
  * HTTP tunnels.
  *
  * <p>Please note that there is currently no standard about how to tunnel
  * XDR data over HTTP connections. There are a (quite a) few solutions out
  * there, but they are more or less incompatible due to the lack of a RFC.
  *
  * <p>This class is responsible solely for <em>receiving</em> ONC/RPC replies.
  * The reply data is base64 encoded and embedded within an ordinary plain
  * ASCII page, as is shown in this example.
  *
  * <pre>
  *     DEADBEEFDEADBEEFDEADBEEF...&lt;CR&gt;&lt;LF&gt;
  *     B0D0EADSDEADBEEFB0D0EADS...&lt;CR&gt;&lt;LF&gt;
  *     ...&lt;CR&gt;&lt;LF&gt;
  *     DEADBE==&lt;CR&gt;&lt;LF&gt;
  * </pre>
  *
  * <p>Parsing is minimalistic to make the whole sucker as fast as possible (not
  * looking at Java's performance at all).
  *
  * @version $Revision$ $Date$ $State$ $Locker$
  * @author Harald Albrecht
  */
 public class XdrHttpDecodingStream extends XdrDecodingStream {
 
     /**
      * Constructs a new <code>XdrHttpDecodingStream</code>.
      *
      * @param httpClient HTTP client connection from which to read the
      *   encoded and embedded ONC/RPC reply message.
      */
     public XdrHttpDecodingStream(HttpClientConnection httpClient) {
         this.httpClient = httpClient;
         //
         //
         // Calculate the buffer size depending on the number of plain
         // ASCII lines we try to read and process as one chunk. Then allocate
         // the necessary buffers.
         //
         int lines = HttpTunnelConstants.LINES_PER_BLOCK;
         int bufferSize = lines * HttpTunnelConstants.BYTES_PER_LINE;
         int asciiBufferSize = lines * HttpTunnelConstants.ENCODED_BYTES_PER_LINE_CRLF;
 
         buffer = new byte[bufferSize];
         this.bufferSize = bufferSize;
 
         asciiBuffer = new byte[asciiBufferSize];
         this.asciiBufferSize = asciiBufferSize;
         //
         // Reset the incomming buffer, just to be sure.
         //
         bufferIndex = 0;
         bufferHighmark = -4;
     }
 
     /**
      * Returns the Internet address of the sender of the current XDR data.
      * This method should only be called after {@link #beginDecoding},
      * otherwise it might return stale information.
      *
      * @return InetAddress of the sender of the current XDR data.
      */
     public InetAddress getSenderAddress() {
         return null; // FIXME
     }
 
     /**
      * Returns the port number of the sender of the current XDR data.
      * This method should only be called after {@link #beginDecoding},
      * otherwise it might return stale information.
      *
      * @return Port number of the sender of the current XDR data.
      */
     public int getSenderPort() {
         return 0; // FIXME
     }
 
     /**
      * Initiates decoding of the next XDR record. For HTTP-based XDR we
      * just read the content delivered with the answer to the POST command.
      *
      * @throws OncRpcException if an ONC/RPC error occurs.
      * @throws IOException if an I/O error occurs.
      */
     public void beginDecoding()
            throws OncRpcException, IOException {
         //
         // Note that we don't assume getting a content length in advance,
         // but instead read 'till we drop. This is assumption is necessary
         // because we can not rely on keep-alive connections but instead
         // need to be able to deal with ordinay the close-after-receipt
         // interaction pattern of older HTTP versions.
         //
         httpClient.beginDecoding();
         //
         // Just set up the (binary) data buffer to contain no data so that
         // the next decoding of some XDR data will result in the buffer being
         // filled from the HTTP connection. Delegation is sooo sweeeet!
         //
         bufferIndex = 0;
         bufferHighmark = -4;
     }
 
     /**
      * End decoding of the current XDR record. The general contract of
      * <code>endDecoding</code> is that calling it is an indication that
      * the current record is no more interesting to the caller and any
      * allocated data for this record can be freed.
      *
      * <p>To help the HTTP connection keeping alive, we swallow all data
      * until we reach the end. If this is not possible, either because the
      * server indicated that it can not keep the connection open, the content
      * length was unknown in advance, or we got an I/O exception, we close
      * the connection.
      *
      * @throws OncRpcException if an ONC/RPC error occurs.
      * @throws IOException if an I/O error occurs.
      */
     public void endDecoding()
            throws OncRpcException, IOException {
         //
         // Let the HTTP client object handle all the crude aspects of keeping
         // alive connections, swallowing unwanted chunked data, etc.
         //
         try {
             httpClient.endDecoding();
         } catch ( IOException e ) {
         }
         //
         // Make sure that no-one can accidently retrieve any stale data from
         // the decoding buffer.
         //
         bufferIndex = 0;
         bufferHighmark = -4;
     }
 
     /**
      * Closes this decoding XDR stream and releases any system resources
      * associated with this stream. A closed XDR stream cannot perform decoding
      * operations and cannot be reopened.
      *
      * <p>This implementation frees the allocated buffer but does not close
      * the associated datagram socket. It only throws away the reference to
      * this socket.
      *
      * @throws OncRpcException if an ONC/RPC error occurs.
      * @throws IOException if an I/O error occurs.
      */
     public void close()
            throws OncRpcException, IOException {
         //
         // Do not close the httpClient, as it does not belong to us, but we
         // simply got a reference to use this particular client.
         //
         httpClient = null;
         buffer = null;
     }
 
     /**
      * Receives more encoded data over the HTTP connection and decodes it into
      * octets, making them available through the <code>buffer</code> field.
      */
     private void fill()
             throws OncRpcException, IOException {
         //
         // In the first step we receive the base64 encoded plain ASCII data
         // through the HTTP connection one block at a time.
         //
         int charsRead;
         try {
             int remaining = httpClient.getRemainingContentLength();
             if ( remaining < 0 ) {
                 charsRead = httpClient.readContentBytes(asciiBuffer, 0, asciiBufferSize);
             } else {
                 charsRead = httpClient.readContentBytes(asciiBuffer, 0, remaining);
             }
         } catch ( ProtocolException e ) {
             throw(new OncRpcException(OncRpcException.RPC_BUFFERUNDERFLOW));
         }
         //
         // In the next step we decode the base64 data and store the octets into
         // the real buffer.
         //
         int decoded = 0;
         int encoded = 0;
         int toDecode;
         while ( charsRead > 0 ) {
             // FIXME: more sanity checks...
             // We currently simply assume that the last two characters are CRLF
             // and thus skip them.
             toDecode = charsRead >= HttpTunnelConstants.ENCODED_BYTES_PER_LINE ?
                            HttpTunnelConstants.ENCODED_BYTES_PER_LINE : (charsRead - 2);
             decoded += Base64.decode(asciiBuffer, encoded, toDecode, buffer, decoded);
             //
             // FIXME: check CRLF
             //
             encoded += toDecode + 2;
             charsRead -= toDecode + 2;
         }
         //
         // Set the buffer "pointers" accordingly.
         //
         bufferIndex = 0;
         bufferHighmark = decoded - 4;
     }
 
 
     /**
      * Decodes (aka "deserializes") a "XDR int" value received from a
      * XDR stream. A XDR int is 32 bits wide -- the same width Java's "int"
      * data type has.
      *
      * @return The decoded int value.
      *
      * @throws OncRpcException if an ONC/RPC error occurs.
      * @throws IOException if an I/O error occurs.
      */
     public int xdrDecodeInt()
            throws OncRpcException, IOException {
         if ( bufferIndex > bufferHighmark ) {
             fill();
         }
         //
         // There's enough space in the buffer to hold at least one
         // XDR int. So let's retrieve it now.
         // Note: buffer[...] gives a byte, which is signed. So if we
         // add it to the value (which is int), it has to be widened
         // to 32 bit, so its sign is propagated. To avoid this sign
         // madness, we have to "and" it with 0xFF, so all unwanted
         // bits are cut off after sign extension. Sigh.
         //
         int value = buffer[bufferIndex++] & 0xFF;
         value = (value << 8) + (buffer[bufferIndex++] & 0xFF);
         value = (value << 8) + (buffer[bufferIndex++] & 0xFF);
         value = (value << 8) + (buffer[bufferIndex++] & 0xFF);
         return value;
     }
 
     /**
      * Decodes (aka "deserializes") an opaque value, which is nothing more
      * than a series of octets (or 8 bits wide bytes). Because the length
      * of the opaque value is given, we don't need to retrieve it from the
      * XDR stream. This is different from
      * {@link #xdrDecodeOpaque(byte[], int, int)} where
      * first the length of the opaque value is retrieved from the XDR stream.
      *
      * @param length Length of opaque data to decode.
      *
      * @return Opaque data as a byte vector.
      *
      * @throws OncRpcException if an ONC/RPC error occurs.
      * @throws IOException if an I/O error occurs.
      */
     public byte [] xdrDecodeOpaque(int length)
            throws OncRpcException, IOException {
         int padding = (4 - (length & 3)) & 3;
         int offset = 0; // current offset into bytes vector
         int toCopy;
         //
         // Now allocate enough memory to hold the data to be retrieved and
         // get part after part from the buffer.
         //
         byte [] bytes = new byte[length];
         if ( bufferIndex > bufferHighmark ) {
             fill();
         }
         while ( length > 0 ) {
             toCopy = bufferHighmark - bufferIndex + 4;
             if ( toCopy >= length ) {
                 //
                 // The buffer holds more data than we need. So copy the bytes
                 // and leave the stage.
                 //
                 System.arraycopy(buffer, bufferIndex, bytes, offset, length);
                 bufferIndex += length;
                 // No need to adjust "offset", because this is the last round.
                 break;
             } else {
                 //
                 // We need to copy more data than currently available from our
                 // buffer, so we copy all we can get our hands on, then fill
                 // the buffer again and repeat this until we got all we want.
                 //
                 System.arraycopy(buffer, bufferIndex, bytes, offset, toCopy);
                 bufferIndex += toCopy;
                 offset += toCopy;
                 length -= toCopy;
                 fill();
             }
         }
         bufferIndex += padding;
         return bytes;
     }
 
     /**
      * Decodes (aka "deserializes") a XDR opaque value, which is represented
      * by a vector of byte values, and starts at <code>offset</code> with a
      * length of <code>length</code>. Only the opaque value is decoded, so the
      * caller has to know how long the opaque value will be. The decoded data
      * is always padded to be a multiple of four (because that's what the
      * sender does).
      *
      * @param opaque Byte vector which will receive the decoded opaque value.
      * @param offset Start offset in the byte vector.
      * @param length the number of bytes to decode.
      *
     * @return The number of opaque bytes read.
     *
      * @throws OncRpcException if an ONC/RPC error occurs.
      * @throws IOException if an I/O error occurs.
      */
     public void xdrDecodeOpaque(byte [] opaque, int offset, int length)
            throws OncRpcException, IOException {
         int padding = (4 - (length & 3)) & 3;
         int toCopy;
         //
         // Now get part after part and fill the byte vector.
         //
         if ( bufferIndex > bufferHighmark ) {
             fill();
         }
         while ( length > 0 ) {
             toCopy = bufferHighmark - bufferIndex + 4;
             if ( toCopy >= length ) {
                 //
                 // The buffer holds more data than we need. So copy the bytes
                 // and leave the stage.
                 //
                 System.arraycopy(buffer, bufferIndex, opaque, offset, length);
                 bufferIndex += length;
                 // No need to adjust "offset", because this is the last round.
                 break;
             } else {
                 //
                 // We need to copy more data than currently available from our
                 // buffer, so we copy all we can get our hands on, then fill
                 // the buffer again and repeat this until we got all we want.
                 //
                 System.arraycopy(buffer, bufferIndex, opaque, offset, toCopy);
                 bufferIndex += toCopy;
                 offset += toCopy;
                 length -= toCopy;
                 fill();
             }
         }
         bufferIndex += padding;
     }
 
     /**
      * Client HTTP tunnel to retrieve embedded XDR records from.
      */
     private HttpClientConnection httpClient;
 
     /**
      * The buffer which will be filled from the datagram socket and then
      * be used to supply the information when decoding data.
      */
     private byte [] buffer;
 
     /**
      *
      */
     private int bufferSize;
 
     /**
      * The buffer receiving base64 encoded plain ASCII data from a HTTP web
      * server. This buffer is only used for immediate decoding of the binary
      * data, which is then stored in the usual <code>buffer</code> field.
      */
     private byte [] asciiBuffer;
 
     /**
      * Size of buffer for receiving the base64 encoded plain ASCII data. The
      * encoded data is then immediately decoded after recept into "ordinary"
      * binary data, which is stored in the usual <code>buffer</code> field.
      */
     private int asciiBufferSize;
 
     /**
      * The read pointer is an index into the <code>buffer</code>.
      */
     private int bufferIndex;
 
     /**
      * Index of the last four byte word in the buffer, which has been read
      * in from the datagram socket.
      */
     private int bufferHighmark;
 
 }
 
 // End of XdrHttpDecodingStream
