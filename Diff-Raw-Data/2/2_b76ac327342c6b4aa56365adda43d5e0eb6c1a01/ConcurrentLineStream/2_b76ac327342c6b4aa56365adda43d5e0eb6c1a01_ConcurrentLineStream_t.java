 package com.ksmpartners.tlswriter;
 
 import java.util.List;
 import java.util.LinkedList;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ConcurrentLineStream
     extends OutputStream
 {
     private static final Logger log =
         LoggerFactory.getLogger(ConcurrentLineStream.class);
 
     private static List<StringBuffer> threadLineBuffers =
         new LinkedList<StringBuffer>();
 
     ThreadLocal<StringBuffer> buf = new ThreadLocal<StringBuffer>() {
         protected StringBuffer initialValue() {
             StringBuffer buf = new StringBuffer();
 
             synchronized(threadLineBuffers) {
                 threadLineBuffers.add(buf);
             }
 
             return buf;
         }
     };
 
     protected OutputStream underlying = null;
 
     public ConcurrentLineStream(OutputStream underlying)
     {
         this.underlying = underlying;
     }
 
     protected void flushBuffer(StringBuffer buf)
         throws IOException
     {
         synchronized(underlying) {
             underlying.write(buf.toString().getBytes());
         }
     }
 
     public void flush()
         throws IOException
     {
         flushBuffer(buf.get());
 
         buf.get().setLength(0);
     }
 
     public void write(int b)
         throws IOException
     {
        buf.get().append((char)b);
 
         if (b == (int)'\n')
             flush();
     }
 
     public void close()
         throws IOException
     {
         for(StringBuffer lbuf : threadLineBuffers)
             flushBuffer(lbuf);
 
         underlying.close();
     }
 }
