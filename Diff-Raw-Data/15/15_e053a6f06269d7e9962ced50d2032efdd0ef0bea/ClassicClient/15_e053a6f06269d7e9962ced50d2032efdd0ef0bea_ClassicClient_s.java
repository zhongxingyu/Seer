 package net.mcforge.networking.packets.clients;
 
 import java.io.IOException;
 import java.net.Socket;
 
 import net.mcforge.iomodel.Player;
 import net.mcforge.networking.IOClient;
 import net.mcforge.networking.packets.IClient;
 import net.mcforge.networking.packets.Packet;
 import net.mcforge.networking.packets.PacketManager;
 
 public class ClassicClient implements IClient {
 
     @Override
     public byte getOPCode() {
         return 0;
     }
 
     @Override
     public IOClient create(Socket client, PacketManager pm) {
         Player object = new Player(client, pm);
         Packet packet = pm.getPacket(getOPCode());
         if (packet == null)
             return null;
         else {
             byte[] message = new byte[packet.length];
             try {
                if(object.getInputStream().read(message) != message.length) {
                     pm.server.Log("Bad packet...");
                     return null;
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
             packet.Handle(message, pm.server, object);
             return object;
         }
     }
 
 }
