 package me.crosant.activate_bukkit_plugin;
 
 import java.io.File;
 import java.util.Random;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import net.milkbowl.vault.chat.Chat;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.ChatColor;
import org.bukkit.World;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 public class activate_bukkit_plugin extends JavaPlugin {
 
     protected FileConfiguration config;
     public static final Logger log = Logger.getLogger("Minecraft");
     public static Permission perms = null;
 
     public void onDisable() {
         log.info("Activate V0.1 has been disabled.");
     }
 
     public void onEnable() {
         boolean exists = (new File("plugins/Activate_Bukkit_Plugin/config.yml")).exists();
         setupPermissions();
 
         config = getConfig();
         if (exists) {
         } else {
             this.getConfig().set("Basic.activated", true);
             this.getConfig().set("Basic.string", "1234567890");
 
             //this.getConfig().set("Basic.Permission", "yes");
             this.getConfig().set("Activate.Group", "player");
             this.getConfig().set("Activate.replaceGroup", "visitor");
             // this.getConfig().set("Activate.Perm", "player.player");
             //  this.getConfig().set("Activate.World", "*");
             //  this.getConfig().set("Activate.Prefix", "Activated");
             this.getConfig().set("Messages.nopermission", "You don't have the required permissions to do this.");
             this.getConfig().set("Messages.activation", "You have been Activated");
             this.getConfig().set("Messages.wrong", "Wrong Word");
 
             this.saveConfig();
         }
 
         log.info("Activate V0.1 has been enabled.");
 
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         Player player = null;
         Player user = null;
 
         if (sender instanceof Player) {
             player = (Player) sender;
         }
         if (player == null) {
             sender.sendMessage("this command can only be run by a player");
         } else {
             if (cmd.getName().equalsIgnoreCase("activate")) {
 
                 if (args.length > 1) {
                     if (args[0].equalsIgnoreCase("refresh") && perms.has(player, "Activate_Bukkit_Plugin.refresh")) {
 
                         String allowedChars = "0123456789abcdefghijklmnopqrstuvwxyz";
                         Random random = new Random();
                         String length1 = args[1];
                         int length = Integer.parseInt(length1);
                         String randomString = generateRandomString(allowedChars, random, length);
 
                         this.getConfig().set("Basic.string", randomString);
                         player.sendMessage( ChatColor.translateAlternateColorCodes('&',randomString));
                         this.saveConfig();
                     } else if (args[0].equalsIgnoreCase("set") && perms.has(player, "Activate_Bukkit_Plugin.refresh")) {
 
                         this.getConfig().set("Basic.string", args[1]);
                         player.sendMessage( ChatColor.translateAlternateColorCodes('&',"New String set"));
                         this.saveConfig();
                     } else if (args[0].equalsIgnoreCase("player") && perms.has(player, "Activate_Bukkit_Plugin.activate.manual")) {
 
                         String[] groups = new String[1];
                         groups[0] = this.getConfig().getString("Activate.Group");
                        for(World world : Bukkit.getWorlds())
                            perms.playerAddGroup(world, player.getName(), groups[0]);
                        
                         perms.playerRemoveGroup(player, this.getConfig().getString("Activate.replaceGroup"));
                         String user1 = args[1];
                         user = org.bukkit.Bukkit.getPlayer(user1);
                         player.sendMessage( ChatColor.translateAlternateColorCodes('&',this.getConfig().getString(user + "has been activated")));
                     }
 
                     /*  else{
                
                      player.sendMessage( ChatColor.translateAlternateColorCodes('&',this.getConfig().getString("Messages.wrong"));
                 
                      }*/
                 } else if (args.length > 0) {
                     if (args[0].equalsIgnoreCase("reload") && perms.has(player, "Activate_Bukkit_Plugin.reload")) {
 
                         this.reloadConfig();
                         player.sendMessage( ChatColor.translateAlternateColorCodes('&',"Activate reloaded"));
 
                     } else if (args[0].equalsIgnoreCase(this.getConfig().getString("Basic.string")) && perms.has(player, "Activate_Bukkit_Plugin.activate")) {
 
                         String[] groups = new String[1];
                         groups[0] = this.getConfig().getString("Activate.Group");
                         perms.playerRemoveGroup(player, this.getConfig().getString("Activate.replaceGroup"));
                        for(World world : Bukkit.getWorlds())
                            perms.playerAddGroup(world, player.getName(), groups[0]);
                         player.sendMessage( ChatColor.translateAlternateColorCodes('&',this.getConfig().getString("Messages.activation")));
                     } else {
 
                         player.sendMessage( ChatColor.translateAlternateColorCodes('&',this.getConfig().getString("Messages.wrong")));
 
                     }
 
                 } else {
                     player.sendMessage( ChatColor.translateAlternateColorCodes('&',"Check your syntax."));
                     return false;
                 }
 
             } else {
                 return false;
             }
 
             return true;
 
         }
         return false;
     }
 
     private static String generateRandomString(String allowedChars,
             Random random, int length) {
         int max = allowedChars.length();
         StringBuffer buffer = new StringBuffer();
         for (int i = 0; i < length; i++) {
             int value = random.nextInt(max);
             buffer.append(allowedChars.charAt(value));
         }
         return buffer.toString();
     }
 
     private boolean setupPermissions() {
         RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
         perms = rsp.getProvider();
         return perms != null;
     }
 }
