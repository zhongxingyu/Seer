 package org.graylog2;
 
 import org.graylog2.log4j2.GelfConsoleAppender.Target;
 
 import java.io.*;
 import java.nio.charset.Charset;
 
 public class GelfConsoleSender implements GelfSender {
 
     public Target target;
 
     public GelfConsoleSender(Target t) {
        this.target = t;
    }
 
    public boolean sendMessage(GelfMessage message) {
        return message.isValid() && AppendToConsole(message.toJson() + "\n");
    }
 
     public void close() {
         //We can never close stdout/stderr.
     }
 
     private boolean AppendToConsole(String message) {
         final String enc = Charset.defaultCharset().name();
 
         PrintStream printStream = null;
         if(target == Target.SYSTEM_ERR) {
             try {
                 printStream = new PrintStream(new SystemErrStream(), true, enc);
             } catch (UnsupportedEncodingException e) {
                 e.printStackTrace();
             }
         } else {
             try {
                 printStream = new PrintStream(new SystemOutStream(), true, enc);
             } catch (UnsupportedEncodingException e) {
                 e.printStackTrace();
             }
         }
 
         if (printStream != null) {
             try {
                 printStream.write(message.getBytes(enc));
             } catch (IOException e) {
                 e.printStackTrace();
             }
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * An implementation of OutputStream that redirects to the current System.err.
      */
     private static class SystemErrStream extends OutputStream {
         public SystemErrStream() {
         }
 
         @Override
         public void close() {
             // do not close sys err!
         }
 
         @Override
         public void flush() {
             System.err.flush();
         }
 
         @Override
         public void write(final byte[] b) throws IOException {
             System.err.write(b);
         }
 
         @Override
         public void write(final byte[] b, final int off, final int len)
                 throws IOException {
             System.err.write(b, off, len);
         }
 
         @Override
         public void write(final int b) {
             System.err.write(b);
         }
     }
 
     /**
      * An implementation of OutputStream that redirects to the current System.out.
      */
     private static class SystemOutStream extends OutputStream {
         public SystemOutStream() {
         }
 
         @Override
         public void close() {
             // do not close sys out!
         }
 
         @Override
         public void flush() {
             System.out.flush();
         }
 
         @Override
         public void write(final byte[] b) throws IOException {
             System.out.write(b);
         }
 
         @Override
         public void write(final byte[] b, final int off, final int len) throws IOException {
             System.out.write(b, off, len);
         }
 
         @Override
         public void write(final int b) throws IOException {
             System.out.write(b);
         }
     }
 
 
}
