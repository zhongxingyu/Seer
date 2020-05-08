 import com.tulskiy.tta.TTADecoder;
 import com.tulskiy.tta.TTA_info;
 import org.junit.Test;
 
 import java.io.*;
 import java.net.URISyntaxException;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 
 import static org.junit.Assert.*;
 
 /**
  * Author: Denis Tulskiy
  * Date: 5/30/11
  */
 public class TestTTA {
     @Test
     public void testAll() throws IOException {
         try {
             TTADecoder decoder = new TTADecoder(
                    new FileInputStream("/home/tulskiy/Downloads/libtta++-2.1/Debug/console/1.tta"));
            long time = System.currentTimeMillis();
             TTA_info info = decoder.init_get_info(0);
            assertEquals(16, info.bps);                            assertEquals(2, info.nch);
//            assertEquals(29400, info.samples);
             assertEquals(44100, info.sps);
             int smp_size = info.nch * ((info.bps + 7) / 8);
 
             FileOutputStream fos = new FileOutputStream("output.wav");
             ByteBuffer header = ByteBuffer.allocate(44);
             header.order(ByteOrder.LITTLE_ENDIAN);
             header.put("RIFF".getBytes());
             header.putInt((int) (36 + info.samples * smp_size));
             header.put("WAVE".getBytes());
             header.put("fmt ".getBytes());
             header.putInt(16);
             header.putShort((short) 1);
             header.putShort((short) info.nch);
             header.putInt((int) info.samples);
             header.putInt((int) (info.samples * info.nch * info.bps / 8));
             header.putShort((short) (info.nch * info.bps / 8));
             header.putShort((short) info.bps);
             header.put("data".getBytes());
             header.putInt((int) info.samples * smp_size);
             fos.write(header.array());
             byte[] buffer = new byte[5120 + 4];
 
             while (true) {
                 int len = decoder.process_stream(buffer);
                 if (len <= 0) {
                     break;
                 }
 
                 fos.write(buffer, 0, len * smp_size);
             }
 
             fos.close();
            System.out.println(System.currentTimeMillis() - time);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             fail();
         }
     }
 }
