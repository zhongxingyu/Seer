 package net.ae97.teamstats.networking;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 
 /**
  * @version 0.1
  * @author Lord_Ralex
  */
 public class PacketSender {
 
     private final ObjectOutputStream output;
 
     public PacketSender(OutputStream out) throws IOException {
         output = new ObjectOutputStream(out);
     }
 
     public void sendPacket(Packet pk) throws IOException {
         output.writeObject(pk);
         output.flush();
     }
 }
