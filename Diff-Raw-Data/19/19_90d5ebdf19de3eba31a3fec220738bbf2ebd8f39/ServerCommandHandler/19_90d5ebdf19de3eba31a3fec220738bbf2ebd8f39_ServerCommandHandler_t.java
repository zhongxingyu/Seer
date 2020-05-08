 package com.drtshock.willie.command.minecraft;
 
 import ch.jamiete.mcping.MinecraftPing;
 import ch.jamiete.mcping.MinecraftPingReply;
 
 import com.drtshock.willie.Willie;
 import com.drtshock.willie.command.CommandHandler;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.pircbotx.Channel;
 import org.pircbotx.Colors;
 import org.pircbotx.User;
 
 /**
  *
  * @author drtshock
  */
 public class ServerCommandHandler implements CommandHandler {
 
     @Override
     public void handle(Willie bot, Channel channel, User sender, String[] args) {
         if (args.length == 0) {
             channel.sendMessage("Usage: !server <IP>");
         } else {
             try {
                 String motd = new MinecraftPing().getPing(args[0]).getMotd();
                 int max = new MinecraftPing().getPing(args[0]).getOnlinePlayers();
                 int players = new MinecraftPing().getPing(args[0]).getMaxPlayers(); // appear to be backwards lol
                 String version = new MinecraftPing().getPing(args[0]).getVersion();
                String replaceAll = motd.replaceAll("§0", Colors.BLACK).replaceAll("§1", Colors.BLUE).replaceAll("§2", Colors.GREEN).replaceAll("§3", Colors.BLUE).replaceAll("§4", Colors.RED).replaceAll("§5", Colors.MAGENTA).replaceAll("§6", Colors.YELLOW).replaceAll("§7", Colors.LIGHT_GRAY).replaceAll("§8", Colors.DARK_GRAY).replaceAll("§9", Colors.BLUE).replaceAll("§a", Colors.GREEN).replaceAll("§b", Colors.BLUE).replaceAll("§c", Colors.RED).replaceAll("§d", Colors.MAGENTA).replaceAll("§e", Colors.YELLOW).replaceAll("§f", Colors.WHITE);
                channel.sendMessage("(" + args[0] + ") " + replaceAll + " - " + version + " - " + players + "/" + max + " players");
             } catch (IOException ex) {
                 channel.sendMessage(Colors.RED + "Failed to ping that server.");
             }
 
         }
     }
 }
