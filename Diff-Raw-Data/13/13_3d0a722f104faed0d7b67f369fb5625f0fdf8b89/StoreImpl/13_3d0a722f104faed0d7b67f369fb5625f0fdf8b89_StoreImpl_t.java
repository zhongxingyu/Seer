 package assignmentImplementation;
 
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.channels.FileChannel;
 
 import keyValueBaseInterfaces.MemoryMappedFile;
 import keyValueBaseInterfaces.Store;
 
 public class StoreImpl implements Store
 {
     final private static long TOTAL_SIZE = 10737418240L; // 10 GB
     
     private RandomAccessFile raf;
     private MemoryMappedFile mmapfile;
     
     public long getTotalSize()
     {
         return TOTAL_SIZE;
     }
     
     public StoreImpl() throws IndexOutOfBoundsException, IOException {
         raf = new RandomAccessFile("/tmp/pcsd_store", "rw");
         raf.setLength(TOTAL_SIZE);
         mmapfile = new MemoryMappedFile(raf.getChannel(), FileChannel.MapMode.READ_WRITE, 0, TOTAL_SIZE);
     }
 
     @Override
     public byte[] read(Long position, int length) {
         byte[] res = new byte[length];
         mmapfile.get(res, position);
         return res;
     }
 
     @Override
     public void write(Long position, byte[] value) {
         mmapfile.put(value, position);
     }

    @Override
    public Long getSize() {
        // TODO Auto-generated method stub
        return null;
    }
     
 }
