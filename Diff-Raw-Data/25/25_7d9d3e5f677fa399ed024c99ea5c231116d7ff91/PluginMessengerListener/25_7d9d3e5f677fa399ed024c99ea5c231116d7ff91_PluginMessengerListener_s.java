 package net.robbytu.banjoserver.framework.listeners;
 
 import net.robbytu.banjoserver.framework.Main;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.messaging.PluginMessageListener;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 
 public class PluginMessengerListener implements PluginMessageListener {
     @Override
     public void onPluginMessageReceived(String channel, Player s, byte[] message) {
        if(channel != "BSBungee") return;
 
         DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
         try {
            if(in.readUTF().equals("teleport")) {
                 Player sender = Main.plugin.getServer().getPlayer(in.readUTF());
                 Player target = Main.plugin.getServer().getPlayer(in.readUTF());
 
                 if(sender == null || target == null) {
                     Main.plugin.getLogger().warning("Could not teleport! Got request from PluginMessenger.");
                     return;
                 }
 
                 sender.teleport(target);
             }

            if(in.readUTF().equals("userCommand")) {
                 Player target = Main.plugin.getServer().getPlayer(in.readUTF());
                 if(target != null) target.performCommand(in.readUTF());
             }
         } catch (IOException ignored) {}
     }
 }
