 package juglr.net;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.BufferUnderflowException;
 import java.nio.ByteBuffer;
 import java.nio.channels.SocketChannel;
 
 import static juglr.net.HTTP.*;
 
 /**
  * base class for {@link HTTPRequestReader} and {@link HTTPResponseReader}
  */
 public class HTTPReader {
 
     // FIXME: Document MAX_HEADER_LENGTH and make it tweakable
     public static final int MAX_HEADER_LENGTH = 1024;
 
     protected SocketChannel channel;
     protected ByteBuffer buf;
 
     public HTTPReader(SocketChannel channel,
                       ByteBuffer buf) {
         this.channel = channel;
         this.buf = buf;
     }
 
     /**
      * Close the underlying channel if it's open
      * @throws IOException if the underlying channel is open and there is an
      *                     error closing the channel
      */
     public void close() throws IOException {
         if (this.channel.isOpen()) {
             this.channel.close();
         }
     }
 
     /**
      * Reset all state in the reader, preparing it for reading {@code channel}.
      * @param channel the channel to start reading from. If the reader refers
      * a channel then this channel will be closed.
      *
      * @return always returns {@code this}
      * @throws IOException if the reader already refers an open channel and
      *                     there is an error closing it
      */
     public HTTPReader reset(SocketChannel channel) throws IOException {
         close();
         this.channel = channel;
         buf.clear();
 
         return this;
     }
 
     /**
      * Read a HTTP header line (termincated by {@code \r\n})
      * @param target the buffer to read data into
      * @return the number of bytes read into {@code target}. If this is 0
      *         then this was the last header before the body and you may
      *         invoke {@link #readBody} or {@link #streamBody()}
      */
     public int readHeaderField(byte[] target) {
         int fieldEnd = buf.position();
 
         // The empty line just before the body
         if (fieldEnd + 2 < buf.limit() &&
             buf.get(fieldEnd) == '\r' && buf.get(fieldEnd + 1) == '\n') {
 
             // Move position to after fieldEnd + '\r' + '\n'
             buf.position(fieldEnd + 2);
             return 0;
         }
 
         while (fieldEnd < MAX_HEADER_LENGTH &&
                 fieldEnd + 2 < buf.limit()) {
 
             // Success on line end
             if (buf.get(fieldEnd + 1) == '\r' && buf.get(fieldEnd +2) == '\n') {
                 int numRead = Math.min(target.length,
                                        fieldEnd - buf.position() + 1);
                 buf.get(target, 0, numRead);
 
                 // Move position to after fieldEnd + '\r' + '\n', even though
                 // we might have read less than the entire line
                 buf.position(fieldEnd + 3);
                 return numRead;
             }
             fieldEnd++;
         }
 
         // We did not meet a line end
         return -1;
     }
 
     /**
      * Read a numeric HTTP status code and return it as a {@link Status}
      * @return a symbollic representation of the HTTP status code
      * @throws IOException upon errors reading from the socket
      * @throws UnsupportedStatusException if the server returns and uncommon
      *                                    HTTP status code that is not supported
      *                                    by Juglr
      */
     public Status readStatus() throws IOException {
         if (buf.remaining() < 3) {
             throw new BufferUnderflowException();
         }
 
         int d0 = buf.get() - 48;
         int d1 = buf.get() - 48;
         int d2 = buf.get() - 48;
 
         switch (d0) {
             case 2:
                 if (d1 == 0) {
                     switch (d2) {
                         case 0: return Status.OK;
                         case 1: return Status.Created;
                         case 2: return Status.Accepted;
                         case 4: return Status.NoContent;
                     }
                 }
                 throw new UnsupportedStatusException(d0, d1, d2);
             case 3:
                 if (d1 == 0 && d2 == 2) return Status.Found;
                 throw new UnsupportedStatusException(d0, d1, d2);
             case 4:
                 if (d1 == 0) {
                     switch (d2) {
                         case 0: return Status.BadRequest;
                         case 1: return Status.Unauthorized;
                         case 3: return Status.Forbidden;
                         case 4: return Status.NotFound;
                         case 5: return Status.MethodNotAllowed;
                         case 6: return Status.NotAcceptable;
                         case 8: return Status.RequestTimeout;
                         case 9: return Status.Conflict;
                     }
                 }
                 throw new UnsupportedStatusException(d0, d1, d2);
             case 5:
                 if (d1 == 0 && d2 == 0) return Status.InternalError;
                 throw new UnsupportedStatusException(d0, d1, d2);
             default:
                 throw new UnsupportedStatusException(d0, d1, d2);
 
         }
     }
 
     /**
      * Parse a HTTP protocol version declaration as used in the HTTP
      * protocol. The buffer will be positioned just after the last parsed
      * character upon method return
      * @return the HTTP version or {@link Version#ERROR} on errors
      * @throws IOException upon errors reading from the socket
      */
     public HTTP.Version readVersion() throws IOException {
         ensureBuffer();
         if (buf.get() == 'H' &&
                 buf.get() == 'T' &&
                 buf.get() == 'T' &&
                 buf.get() == 'P' &&
                 buf.get() == '/' &&
                 buf.get() == '1' &&
                buf.get() == '.') {
            byte last = buf.get();
            if (last == '0') {
                 return Version.ONE_ZERO;
            } else if (last == '1') {
                 return Version.ONE_ONE;
             } else {
                 return Version.UNKNOWN;
             }
        }
         return Version.ERROR;
     }
 
     /**
      * Read the next byte and return {@code true} if it is a white space
      * @return {@code true} if the next byte is a space, {@code false} otherwise
      * @throws IOException upon errors reading from the socket
      */
     public boolean readSpace() throws IOException {
         ensureBuffer();
         return buf.get() == ' ';
     }
 
     /**
      * Read the next byte and and check if it is a carriage return - if it is
      * then read the second byte and check if it's a newline - and if both
      * bytes are good return {@code true}.
      * @return {@code true} if the next two bytes are {@code \r} and {@code \n}
      *         {@code false} otherwise
      * @throws IOException upon errors reading from the socket
      */
     public boolean readLF() throws IOException {
         ensureBuffer();
         if (buf.get() != '\r') {
             return false;
         }
         ensureBuffer();
         return buf.get() == '\n';
     }
 
     public int readBody(byte[] target) throws IOException {
         ensureBuffer();
         int numRead = Math.min(buf.remaining(), target.length);
         buf.get(target, 0, numRead);
         return numRead;
     }
 
     public int readBody(byte[] target, int offset, int len) throws IOException {
         ensureBuffer();
         int numRead = Math.min(buf.remaining(), len);
         buf.get(target, offset, numRead);
         return numRead;
     }
 
     private void ensureBuffer()  throws IOException {
         if (buf.remaining() == 0) {
             channel.read(buf);
             buf.flip();
         }
     }
 
     /**
      * Read the message body as a stream, starting from the current position.
      * When the returned stream is closed this reader will also be closed
      * @return and input stream reading the message body from the socket
      */
     public InputStream streamBody() {
         return new InputStream() {
 
             @Override
             public int read() throws IOException {
                 if (buf.remaining() == 0) {
                     channel.read(buf);
                     buf.flip();
                 }
                 return buf.get();
             }
 
             @Override
             public int read(byte[] b) throws IOException {
                 return readBody(b);
             }
 
             @Override
             public int read(byte[] b, int offset, int len) throws IOException {
                 return readBody(b, offset, len);
             }
 
             @Override
             public void close() throws IOException {
                 HTTPReader.this.close();
             }
         };
     }
 }
