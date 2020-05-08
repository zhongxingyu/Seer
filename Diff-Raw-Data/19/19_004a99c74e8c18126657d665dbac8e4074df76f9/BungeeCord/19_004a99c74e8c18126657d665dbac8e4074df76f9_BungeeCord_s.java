 package net.year4000.echat;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.messaging.PluginMessageListener;
 
 import com.sk89q.commandbook.CommandBook;
 
 public class BungeeCord implements PluginMessageListener {
 
     /**
      * Send the needed data to every single server.
      */
     public void sendChatBungeeCord() {
         try {
             // Send out to eChat.
             ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
             DataOutputStream msgout = new DataOutputStream(msgbytes);
             msgout.writeUTF(EChat.inst().getMessage().getPlayerName());
             msgout.writeUTF(EChat.inst().getMessage().getPlayerDisplayName());
             msgout.writeUTF(EChat.inst().getMessage().getPlayerServer());
             msgout.writeUTF(EChat.inst().getMessage().getPlayerWorldName());
             msgout.writeUTF(EChat.inst().getMessage().getPlayerGroupName(0));
             msgout.writeUTF(EChat.inst().getMessage().getPlayerMessage());
             msgout.writeUTF(EChat.inst().getMessage().getPlayerFormat());
             int data = EChat.inst().getMessage().getPlayerMessage().length();
             msgout.writeShort(data);
 
             // Send out to BungeeCord.
             ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(b);
             out.writeUTF("Forward");
             out.writeUTF("ALL");
             out.writeUTF("eChat");
             out.writeShort(msgbytes.toByteArray().length);
             out.write(msgbytes.toByteArray());
             Bukkit.getOnlinePlayers()[0].sendPluginMessage(CommandBook.inst(),
                     "BungeeCord", b.toByteArray());
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 
     /**
      * Receive the data from another server and processes that chat.
      */
     @Override
     public void onPluginMessageReceived(String channel, Player player,
             byte[] data) {
         // Make sure to get results only from BungeeCord.
         if (channel.equals("BungeeCord")) {
             try {
                 DataInputStream in =
                         new DataInputStream(new ByteArrayInputStream(data));
                 String subchannel = in.readUTF();
                 short len = in.readShort();
                 byte[] msgbytes = new byte[len];
                 in.readFully(msgbytes);
                DataInputStream msgin =
                        new DataInputStream(new ByteArrayInputStream(msgbytes));
                 // Only get data from eChat
                 if (subchannel.equals("eChat")) {
                    String messageUTF = msgin.readUTF();
                    EChat.inst().getMessage().setPlayerName(messageUTF);
                    EChat.inst().getMessage().setPlayerDisplayName(messageUTF);
                    EChat.inst().getMessage().setPlayerServer(messageUTF);
                    EChat.inst().getMessage().setPlayerWorldName(messageUTF);
                    EChat.inst().getMessage().setPlayerGroups(messageUTF);
                    EChat.inst().getMessage().setPlayerMessage(messageUTF);
                    EChat.inst().getMessage().setPlayerFormat(messageUTF);
 
                     EChat.inst().getSender().sendChatMessage();
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 }
