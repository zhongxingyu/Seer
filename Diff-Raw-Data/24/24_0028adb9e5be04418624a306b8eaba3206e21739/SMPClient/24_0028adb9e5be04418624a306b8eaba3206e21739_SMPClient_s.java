 package net.mcforge.networking.packets.clients;
 
import java.io.IOException;
 import java.net.Socket;
 
 import net.mcforge.iomodel.SMPPlayer;
 import net.mcforge.networking.IOClient;
 import net.mcforge.networking.packets.DynamicPacket;
 import net.mcforge.networking.packets.IClient;
 import net.mcforge.networking.packets.Packet;
 import net.mcforge.networking.packets.PacketManager;
 
 public class SMPClient implements IClient {
 
     @Override
     public byte getOPCode() {
         return 2;
     }
 
     @Override
     public IOClient create(Socket client, PacketManager pm) {
         SMPPlayer object = new SMPPlayer(client, pm);
         Packet packet = pm.getPacket("Hankshake");
         if (packet == null)
             return null;
         else {
             ((DynamicPacket)packet).handle(pm.server, object, object.getInputStream());
             object.kick("This is not an SMP Server!");
             return null;
         }
     }
 
 }
