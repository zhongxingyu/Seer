 package com.ninja_squad.jb.codestory;
 
 import com.google.common.io.LineProcessor;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.charset.Charset;
 
 /**
  * Allows reading HTTP lines (i.e. lines terminated by \r\n)
  * @author JB
  */
 class HttpLineReader {
 
     private final InputStream in;
     private final Charset charset;
 
     public HttpLineReader(InputStream in, Charset charset) {
         this.in = in;
         this.charset = charset;
     }
 
     public <T> T readLines(LineProcessor<T> lineProcessor) throws IOException {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         int i;
         byte lastByte = 0;
         boolean firstRead = true;
         while ((i = in.read()) != -1) {
             byte currentByte = (byte) i;
             if (lastByte == '\r' && currentByte == '\n') {
                 String line = new String(baos.toByteArray(), charset);
                 boolean continueReading = lineProcessor.processLine(line);
                 if (!continueReading) {
                     return lineProcessor.getResult();
                 }
                 else {
                     firstRead = true;
                     lastByte = 0;
                     baos.reset();
                 }
             }
             else if (!firstRead) {
                 baos.write(lastByte);
                 lastByte = currentByte;
             }
             else {
                 lastByte = currentByte;
                 firstRead = false;
             }
         }
         if (!firstRead) {
             baos.write(lastByte);
            lineProcessor.processLine(baos.toString());
         }
         return lineProcessor.getResult();
     }
 }
