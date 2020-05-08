 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 
 public class MChatClient extends Thread {
 
   protected DatagramSocket socket = null;
   protected final int DEF_SIZE = 256;
   protected final String DEF_ADDR = "192.168.0.150";
   protected int PORT_NO = 4446;
  protected String nickname = "imyjimmy";
   protected int messageNo = 0;
 
   public MChatClient() throws SocketException {
     socket = new DatagramSocket();
   }
 
   @Override
   public void run() {
     sendGDay();
   }
 
   public void sendGDay() {
     try {
      String goodDay = "GDAY imyjimmy";
      byte[] buffer = goodDay.getBytes();
      System.out.println("sending: " + goodDay);
       InetAddress group = InetAddress.getByName(DEF_ADDR);
       DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT_NO);
       socket.send(packet);
     } catch (IOException ioe) {
       ioe.printStackTrace();
     }
   }
 
   public void sendMessage(String message) {
     try {
       String says = "SAYS";
       String toSend = says.concat(" " + nickname + " " + messageNo + " " + message);
       System.out.println("Sending: " + toSend);
       byte[] buffer = toSend.getBytes();
       InetAddress group = InetAddress.getByName(DEF_ADDR);
       DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT_NO);
       socket.send(packet);
       messageNo++;
     } catch (IOException ioe) {
       ioe.printStackTrace();
     }
   }
 
 }
