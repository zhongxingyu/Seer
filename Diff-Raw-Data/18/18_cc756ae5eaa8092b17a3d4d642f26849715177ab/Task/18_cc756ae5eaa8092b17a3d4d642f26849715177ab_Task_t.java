 package ee.ut.jf.concurrency;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URL;
import java.nio.ByteBuffer;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
 import java.util.concurrent.Callable;
 
 public class Task implements Callable<String> {
   private final URL file;
 
   public Task(URL file) {
     this.file = file;
   }
 
   public String call() throws IOException {
     String fileName = file.getPath().substring(file.getPath().lastIndexOf("/") + 1);
 
     ReadableByteChannel rbc = Channels.newChannel(file.openStream());
    ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);

     FileOutputStream fos = null;
     try {
       fos = new FileOutputStream(fileName);
      WritableByteChannel wbc = Channels.newChannel(fos);

      while (rbc.read(buffer) != -1) {
        buffer.flip();
        wbc.write(buffer);
        buffer.compact();
      }
      buffer.flip();
      while (buffer.hasRemaining())
        wbc.write(buffer);
     }
     finally {
       if (fos != null)
         fos.close();
     }
 
     return fileName;
   }
 }
