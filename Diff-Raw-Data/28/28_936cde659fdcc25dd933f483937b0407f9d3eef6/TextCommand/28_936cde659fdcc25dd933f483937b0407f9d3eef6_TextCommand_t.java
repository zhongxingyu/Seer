 package com.dumptruckman.minecraft.gmtools.command;
 
 import com.dumptruckman.minecraft.gmtools.GMToolsPlugin;
 import com.dumptruckman.minecraft.gmtools.util.Perms;
 import com.dumptruckman.minecraft.pluginbase.util.Font;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 
 import java.util.List;
 
 public class TextCommand extends GMCommand {
 
     public TextCommand(GMToolsPlugin plugin) {
         super(plugin);
         this.setName("In game flavor text");
         this.setCommandUsage("/text [-r:radius|-g] <text>");
         this.setArgRange(1, 700);
         this.addKey("text");
         this.addPrefixedKey("text");
         this.addPrefixedKey(" text");
         this.setPermission(Perms.CMD_TEXT.getPermission());
     }
 
     @Override
     public void runCommand(CommandSender sender, List<String> args) {
         int r = 50;
         int i = 0;
         boolean isGlobal = false;
         boolean isTargeted = false;
         Player target = null;
 
         if (args.get(0).startsWith("-r:")) {
             i++;
             try {
                 r = Integer.valueOf(args.get(0).substring(3));
             } catch (NumberFormatException ignore) {
                 sender.sendMessage("Enter a number for the radius!");
                 return;
             }
         } else if (args.get(0).startsWith("-g")) {
             i++;
             isGlobal = true;
         } else if (args.get(0).startsWith("-p")) {
             i += 2;
             isTargeted = true;
             if (args.size() < 3) {
                 sender.sendMessage(ChatColor.RED + "Usage: /text -p <name> <msg>");
                 return;
             }
             target = Bukkit.getServer().getPlayer(args.get(1));
             if (target == null) {
                 sender.sendMessage(ChatColor.RED + "Player not found!");
                 return;
             }
         }
 
         StringBuilder message = new StringBuilder();
         for (i = i; i < args.size(); i++) {
             if (!message.toString().isEmpty()) message.append(" ");
             message.append(args.get(i));
         }
 
        String formattedMessage = messager.format(message.toString());
 
        List<String> lines = Font.splitString(formattedMessage);
 
         if (isGlobal) {
            sender.sendMessage("Global GMText: ");
             for (String s : lines)
                 Bukkit.getServer().broadcastMessage(s);
             return;
        } else if (isTargeted) {
            sender.sendMessage("GMText to " + target.getName() + ": ");
             for (String s : lines) {
                sender.sendMessage(s);
                 target.sendMessage(s);
             }
             return;
         }
 
         if (!(sender instanceof Player)) {
             sender.sendMessage("Must be in game to use locally.");
             return;
         }
         Player player = (Player) sender;
 
         List<Entity> entities = player.getNearbyEntities(r,r,r);
        int listenerCount = 0;
         for (Entity entity : entities) {
             if (entity instanceof CommandSender && !entity.equals(player)) {
                listenerCount++;
                 for (String s : lines) {
                     ((CommandSender)entity).sendMessage(s);
                 }
             }
         }
        sender.sendMessage("Local GMText (radius: " + r + ", listeners: " + listenerCount + "): ");
        for (String s : lines) {
            sender.sendMessage(s);
        }
     }
 }
