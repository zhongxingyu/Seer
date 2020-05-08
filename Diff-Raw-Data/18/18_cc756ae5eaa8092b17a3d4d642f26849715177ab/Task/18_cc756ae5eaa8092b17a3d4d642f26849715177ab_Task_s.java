 package ee.ut.jf.concurrency;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.concurrent.Callable;
 
 public class Task implements Callable<String> {
   private final URL file;
 
   public Task(URL file) {
     this.file = file;
   }
 
   public String call() throws IOException {
     String fileName = file.getPath().substring(file.getPath().lastIndexOf("/") + 1);
 
     ReadableByteChannel rbc = Channels.newChannel(file.openStream());
     FileOutputStream fos = null;
     try {
       fos = new FileOutputStream(fileName);
      fos.getChannel().transferFrom(rbc, 0, 1 << 24);
     }
     finally {
       if (fos != null)
         fos.close();
     }
 
     return fileName;
   }
 }
