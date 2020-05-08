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
 
 import net.year4000.echat.Message;
 
 public class BungeeCord implements PluginMessageListener {
 
     /**
      * Send the needed data to every single server.
      */
     public BungeeCord() {
     }
 
     /**
      * Send the needed data to every single server.
      *
      * @param message The message class
      */
     public BungeeCord(Message message) {
         try {
             // Send out to eChat.
             ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
             DataOutputStream msgout = new DataOutputStream(msgbytes);
             msgout.writeLong(System.currentTimeMillis()); // Send sent time
             msgout.writeUTF(message.getPlayerName());
             msgout.writeUTF(message.getPlayerDisplayName());
             msgout.writeUTF(message.getPlayerServer());
             msgout.writeUTF(message.getPlayerWorldName());
             msgout.writeUTF(message.getPlayerGroup());
             msgout.writeUTF(message.getPlayerMessage());
             msgout.writeUTF(message.getPlayerFormat());
             msgout.writeUTF(message.getPlayerColor());
             msgout.writeUTF(message.getPlayerFaction());
             msgout.writeUTF(message.getPlayerTitle());
             int data = message.getPlayerMessage().length();
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
      *
      * @param channel The channel of the Plugin Message.
      * @param player The player that sent the plugin message.
      * @param data The byte array of the plugin message.
      */
     @Override
     public void onPluginMessageReceived(String channel, Player player,
             byte[] data) {
         // Make sure to get results only from BungeeCord.
         if (!channel.equals("BungeeCord")) {
             return;
         }
 
         try {
             DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
             String subchannel = in.readUTF();
             short len = in.readShort();
             byte[] msgbytes = new byte[len];
             in.readFully(msgbytes);
             DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
            long sentTime = msgin.readLong(); // Get sent time

            // Only run if message was sent with in 1 second
            long currentTime = System.currentTimeMillis();
            if (sentTime + 1000 < currentTime) {
                return;
            }
 
             // Only get data from eChat
             if (subchannel.equals("eChat")) {
                 // Set up the environment.
                 Message message = new Message();
                 message.setPlayerName(msgin.readUTF());
                 message.setPlayerDisplayName(msgin.readUTF());
                 message.setPlayerServer(msgin.readUTF());
                 message.setPlayerWorldName(msgin.readUTF());
                 message.setPlayerGroup(msgin.readUTF());
                 message.setPlayerMessage(msgin.readUTF());
                 message.setPlayerFormat(msgin.readUTF());
                 message.setPlayerColor(msgin.readUTF());
                 message.setPlayerFaction(msgin.readUTF());
                 message.setPlayerTitle(msgin.readUTF());
 
                 // Send the message to its own thread.
                 message.sendMessage(message);
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
