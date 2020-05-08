 package me.KeybordPiano459.kEssentials.commands;
 
 import java.lang.reflect.Field;
 import java.util.logging.Level;
 import me.KeybordPiano459.kEssentials.kEssentials;
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CommandPing extends kCommand implements CommandExecutor {
     
     String packageV = "";
     
     public CommandPing(kEssentials plugin) {
         super(plugin);
         
         String bv = Bukkit.getVersion();
         int index = bv.indexOf("(MC: ")+5;
         int end = bv.indexOf(")", index);
         String version = bv.substring(index, end);
         plugin.getLogger().info(new StringBuilder("Version found: ").append(version).toString()); 
         try {
             String[] vs = version.split("\\.");
             if (Integer.parseInt(vs[0]) >= 1) {
                 if (Integer.parseInt(vs[1]) >= 4) {
                     if (Integer.parseInt(vs[2]) >= 5) {
                         packageV = new StringBuilder(".v").append(version.replace(".", "_")).toString();
                     }
                 }
             }
         } catch(Exception e) {
             plugin.getLogger().info("Version lower than 1.4.5 found");
         }
         
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         if (cmd.getName().equalsIgnoreCase("ping")) {
             if (sender instanceof Player) {
                 Player player = (Player) sender;
                 if (args.length == 0) {
                     if (player.hasPermission("kessentials.ping")) {
                         try {
                             Object nmsPlayer = Class.forName("org.bukkit.craftbukkit"+packageV+".entity.CraftPlayer")
                                     .getMethod("getHandle", new Class[0])
                                     .invoke(player, new Object[0]);
                             Field con = Class.forName("net.minecraft.server"+packageV+".EntityPlayer").getDeclaredField("ping");
                             con.setAccessible(true);
                            int ping = con.getInt(nmsPlayer);
                             player.sendMessage(GREEN + "Pong - " + ping + " MS");
                         } catch (Exception e) {
                             player.sendMessage(GREEN + "Pong");
                             log(Level.WARNING, "Error collecting ping data for " + player.getName());
                         }
                     } else {
                         noPermissionsMessage(player);
                     }
                 } else {
                     incorrectUsage(player, "/ping");
                 }
             } else {
                 if (args.length == 0) {
                     log(Level.INFO, "Pong");
                 } else {
                     incorrectUsageC("/ping");
                 }
             }
         }
         return false;
     }
}
