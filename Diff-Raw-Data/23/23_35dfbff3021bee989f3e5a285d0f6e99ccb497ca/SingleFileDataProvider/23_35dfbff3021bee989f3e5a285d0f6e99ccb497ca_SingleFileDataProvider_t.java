 package proj.zoie.example.service;
 
 import org.apache.log4j.Logger;
 import proj.zoie.api.DataConsumer;
 import proj.zoie.impl.indexing.StreamDataProvider;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.ReadableByteChannel;
 
 public class SingleFileDataProvider<T> extends StreamDataProvider<T> {
 
   private static final Logger log = Logger.getLogger(SingleFileDataProvider.class);
 
   protected Deserializer<T> deserializer;
   protected final int datumByteSize;
 
   protected ReadableByteChannel inputChannel;
   protected ByteBuffer buf;
   protected byte[] bytes;
 
   protected long position;
 
   public SingleFileDataProvider(int size)
   {
     datumByteSize = size;
   }
 
   public SingleFileDataProvider(String fileName,
                                 int datumByteSize,
                                 Deserializer<T> deserializer) throws IOException {
     this.deserializer = deserializer;
     this.datumByteSize = datumByteSize;
     bytes = new byte[datumByteSize];
    File f = new File(fileName);
    if(!f.exists() || !f.canRead()) throw new IOException("Unreadable file: " + fileName);
     inputChannel = new FileInputStream(new File(fileName)).getChannel();
     buf = ByteBuffer.wrap(bytes);
     position = 0;
     new Thread(new Starter()).start();
   }
 
   private final class Starter implements Runnable {
 
     @Override
     public void run() {
       try {
         Thread.sleep(10000);
         SingleFileDataProvider.this.start();
       } catch(Exception e) {
         System.out.println(e);
       }
     }
   }
 
   public static interface Deserializer<T> {
     public T deserialize(byte[] bytes) throws IOException;
   }
 
   protected long time = 0;
 
   @Override
   public DataConsumer.DataEvent<T> next() {
     if((position % 100000) == 0) {
      if(time > 0) System.out.println(((float)(System.nanoTime() - time)) / 1e9 + "s per 100k docs");
       time = System.nanoTime();
     }
 
     try {
       long version = position;
       buf.clear();
       boolean full = false;
       int bytesRead = 0;
       T t = null;
       while(!full && t == null) {
        bytesRead += Math.max(0, inputChannel.read(buf));
         full = (bytesRead >= datumByteSize);
         if(!full) {
           try { sleep(); } catch (InterruptedException ie) {
             return null;
           }
         } else {
           byte[] bufArray = buf.array();   // latest buf bits.
           try {
             t = deserializer.deserialize(bufArray);
             position +=  bytesRead / datumByteSize;
           } catch (IOException ioe) {
             char[] out = new char[bufArray.length];
             for(int j=0;j<out.length;j++) out[j] = (char)bufArray[j];
             System.out.println(new String(out));
             flushInputToNextEvent();
             return next();
           }
         }
       }
       return new DataConsumer.DataEvent<T>(version, t);
     } catch (IOException ioe) {
       log.error("Cannot read from file!", ioe);
       try { inputChannel.close(); } catch(Exception e) {
         log.warn("Could not close FileChannel: ", e);
       }
       throw new RuntimeException(ioe);
     }
   }
 
   protected void flushInputToNextEvent() throws IOException {
     ByteBuffer smallBuf = ByteBuffer.wrap(new byte[1]);
     while(true) {
       smallBuf.clear();
       int read = inputChannel.read(smallBuf);
       if(read < 1) {
         try { sleep(); } catch (InterruptedException ie) {
           return;
         }
       } else {
         char c = (char)smallBuf.array()[0];
         if(c == '\n') {
           return;
         }
       }
     }
   }
 
   static int sleepCt = 0;
 
   public void sleep() throws InterruptedException {
     Thread.sleep(25);
//    if((sleepCt++ % 120) == 0) System.out.println("\n.");
   }
 
   @Override
   public void reset() {
     // noop!
   }
 }
