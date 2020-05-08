 /**
  * Copyright
  */
 package scldclng;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.ParseException;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 public class Multipart {
    
    private ProgressListener progressListener;
 
     private final InputStream input;
 
     private final ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();
 
     private byte[] boundary;
 
     private byte[] closingBoundary;
 
     private int readState = BEGIN_READ;
 
     private static final int BEGIN_READ = 1;
     private static final int READ_HEADERS = 2;
     private static final int READ_DATA = 3;
 
     private LinkedList<MimePart> parts = new LinkedList<MimePart>();
 
     public Multipart(final InputStream input, final String boundary) {
         this.input = input;
         this.boundary = ("--" + boundary).getBytes();
         this.closingBoundary = ("--" + boundary + "--").getBytes();
     }
 
    public void setProgressListener(final ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

     public List<MimePart> getParts() {
         return parts;
     }
 
     public void parse() throws IOException, ParseException {
         byte[] line;
         int lineNo = 0;
         int offset = 0;
 
         boolean eof = false;
 
         while (!eof && (line = readLine()) != null) {
            
             offset += line.length;
             lineNo++;
 
            if (progressListener != null) {
                progressListener.onBytesRead(offset);
            }

             switch (readState) {
                 case BEGIN_READ:
                     parts.add(new MimePart());
 
                     if (Arrays.equals(boundary, line)) {
                         readState = READ_HEADERS;
                     } else {
                         throw new ParseException("line " + lineNo + ": expected boundary, but got " + new String(line),
                                 offset);
                     }
                     break;
 
                 case READ_HEADERS:
                     final MimePart currentPart = parts.peekLast();
                     if (line.length == 0) {
                         if (!currentPart.getHeaders().isEmpty()) {
                             readState = READ_DATA;
                         }
                     } else {
                         final MimeHeader header = new MimeHeader(new String(line));
                         currentPart.addHeader(header);
                     }
                     break;
 
                 case READ_DATA:
                     if (Arrays.equals(closingBoundary, line)) {
                         eof = true;
                     } else if (Arrays.equals(boundary, line)) {
                         parts.add(new MimePart());
                         readState = READ_HEADERS;
                     } else {
                         // skip data so far
                     }
                     break;
             }
 
             Thread.yield();
 
         }
     }
 
     private byte[] readLine() throws IOException {
         final byte[] buff = new byte[1024];
         int bytesRead;
         boolean eol = false;
 
         byte[] line = null;
         while (!eol && (bytesRead = readLine(buff, 0, buff.length)) > 0) {
             eol = buff[bytesRead - 1] == '\n';
 
             lineBuffer.write(buff, 0, eol ? bytesRead - 1 : bytesRead);
 
             if (eol) {
                 line = lineBuffer.toByteArray();
                 lineBuffer.reset();
             }
         }
 
         return line;
     }
 
     private int readLine(byte[] b, int off, int len) throws IOException {
         if (len <= 0) {
             return 0;
         }
 
         int count = 0, pnlc = 0, c;
 
         while ((c = input.read()) != -1) {
 
             // \r\n, \n\r workaround
             if (c == '\r' || c == '\n') {
                 if (pnlc != 0 && pnlc != c) {
                     pnlc = 0;
                     continue;
                 } else if (pnlc == 0) {
                     pnlc = c;
                 }
                 c = '\n';
             } else {
                 pnlc = 0;
             }
 
             b[off++] = (byte) c;
             count++;
             if (c == '\n' || count == len) {
                 break;
             }
         }
 
         return count > 0 ? count : -1;
     }
 
 }
