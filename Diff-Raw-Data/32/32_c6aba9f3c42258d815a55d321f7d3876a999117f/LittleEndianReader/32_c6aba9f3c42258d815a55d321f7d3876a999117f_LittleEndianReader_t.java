 package rio.sorter;
 
 import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
 
 public class LittleEndianReader {
 
     private BufferedInputStream stream;
     private long[] array;
     
     public LittleEndianReader(File file) throws FileNotFoundException {
         
         int longs = (int) file.length() / 8;
         
         array = new long[longs];
         stream = new BufferedInputStream(new FileInputStream(file));
     }
     
     public long[] read() {
         
         int arrayIndex = 0;
         
         try {
             byte[] bytes = new byte[1024];
             
             while (stream.available() != 0) {
                 
                 int readBytes = stream.read(bytes);
                 
                 int bytesConverted = 0;
                 
                 while (readBytes - bytesConverted >= 8) {
                    long value = this.littleEndianToLong(bytes, bytesConverted);
                     bytesConverted += 8;

                     array[arrayIndex] = value;
                     arrayIndex++;
                 }
             }
             
             stream.close();
         } catch (IOException exception) {
             System.out.println("Ohoi! Captain overboard!");
         }
         
         return array;
     }
     
    private long littleEndianToLong(byte[] bytes, int offset) {
         
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
         
        buffer.put(bytes, offset, 8);
        buffer.position(0);
        
        return buffer.getLong();
     }
 }
