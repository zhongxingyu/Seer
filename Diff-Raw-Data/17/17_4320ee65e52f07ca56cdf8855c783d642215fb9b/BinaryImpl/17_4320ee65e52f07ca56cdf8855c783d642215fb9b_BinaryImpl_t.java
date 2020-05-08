 package ch.x42.terye.value;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.jcr.Binary;
 import javax.jcr.RepositoryException;
 
 public class BinaryImpl implements Binary {
 
     // store contents in memory
     private byte[] data;
     // keep track of the opened input streams
     private List<InputStream> streams;
     private boolean isDisposed;
 
     public BinaryImpl(InputStream stream) throws IOException {
         // XXX: improve exception handling
         ByteArrayOutputStream buffer = new ByteArrayOutputStream();
         int nRead;
         byte[] data = new byte[1048576];
         while ((nRead = stream.read(data, 0, data.length)) != -1) {
             buffer.write(data, 0, nRead);
         }
         buffer.flush();
        this.data = buffer.toByteArray();
         buffer.close();
         stream.close();
         streams = new LinkedList<InputStream>();
         isDisposed = false;
     }
 
     private void checkDisposed() {
         if (isDisposed) {
             throw new IllegalStateException(
                     "Can't call any method on a disposed binary value");
         }
     }
 
     @Override
     public InputStream getStream() throws RepositoryException {
         checkDisposed();
         InputStream stream = new ByteArrayInputStream(data);
         streams.add(stream);
         return stream;
     }
 
     @Override
     public int read(byte[] b, long position) throws IOException,
             RepositoryException {
         checkDisposed();
         // XXX: improve exception handling
         ByteArrayInputStream is = (ByteArrayInputStream) getStream();
         int res = is.read(b, (int) position, b.length);
         is.close();
         return res;
     }
 
     @Override
     public long getSize() throws RepositoryException {
         return data.length;
     }
 
     @Override
     public void dispose() {
         isDisposed = true;
         Iterator<InputStream> iterator = streams.iterator();
         while (iterator.hasNext()) {
             try {
                 iterator.next().close();
             } catch (IOException e) {
                 // can't do nothing
             }
         }
     }
 
 }
