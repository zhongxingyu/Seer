 package org.lastbamboo.common.util.mina;
 
import java.net.DatagramPacket;
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.DatagramChannel;
 import java.util.Arrays;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * Test for the behavior of datagrams.  Are multiple {@link ByteBuffer}s 
  * combined into single datagrams, for example? 
  */
 public class DatagramTest
     {
 
     @Test
     public void testDatagramConsolidation() throws Exception
         {
         final ByteBuffer receiveBuf = ByteBuffer.allocate(2000);
         final DatagramChannel serverChannel = DatagramChannel.open();
        serverChannel.socket().bind(new InetSocketAddress(7777));
         
         
         final DatagramChannel clientChannel = DatagramChannel.open();
        clientChannel.connect(new InetSocketAddress(7777));
         
         final ByteBuffer message1 = createBuf(1);
         final ByteBuffer message2 = createBuf(2);
         final ByteBuffer message3 = createBuf(3000, 3);
         final ByteBuffer message4 = createBuf(6000, 4);
         
         clientChannel.write(message1);
         clientChannel.write(message2);
         clientChannel.write(message3);
         clientChannel.write(message4);
         
         final ByteBuffer[] bufs = createBufs();
         
         //clientChannel.write(bufs);
         serverChannel.receive(receiveBuf);
         
         Assert.assertEquals(200, receiveBuf.position());
         
         serverChannel.receive(receiveBuf);
         
         Assert.assertEquals(400, receiveBuf.position());
         
         serverChannel.receive(receiveBuf);
         
         // The rest of the message is truncated here.
         Assert.assertEquals(2000, receiveBuf.position());
         
         final ByteBuffer receiveBuf2 = ByteBuffer.allocate(10000);
         serverChannel.receive(receiveBuf2);
         
         Assert.assertEquals(6000, receiveBuf2.position());
         
         }
 
     private ByteBuffer createBuf(final int i)
         {
         return createBuf(200, i);
         }
     
     private ByteBuffer createBuf(final int size, final int fill)
         {
         final byte[] messageBytes = new byte[size];
         Arrays.fill(messageBytes, (byte)fill);
         final ByteBuffer message = ByteBuffer.wrap(messageBytes);
         return message;
         }
 
     private ByteBuffer[] createBufs()
         {
         final ByteBuffer[] bufs = new ByteBuffer[8];
         for (int i = 0; i < 8; i++)
             {
             bufs[i] = createBuf(i);
             }
         return bufs;
         }
 
     }
