 package net.mcforge.networking.packets.minecraft;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import net.mcforge.networking.IOClient;
 import net.mcforge.networking.packets.DynamicPacket;
 import net.mcforge.networking.packets.PacketManager;
 import net.mcforge.server.Server;
 
 public class Handshake extends DynamicPacket {
 
     public Handshake(String name, byte ID, PacketManager parent) {
         super(name, ID, parent);
     }
 
     public Handshake(PacketManager pm) {
         super("Hankshake", (byte)0x02, pm);
     }
 
     @Override
     public void handle(Server server, IOClient player, InputStream reader) {
        
     }
 
     @Override
    public void write(Server server, IOClient player, OutputStream writer) {
        
    }
 
 }
