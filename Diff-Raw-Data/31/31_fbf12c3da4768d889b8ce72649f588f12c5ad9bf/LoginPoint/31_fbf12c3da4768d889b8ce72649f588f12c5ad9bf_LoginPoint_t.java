 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pl.chormon.LoginPoint;
 
import java.util.logging.Level;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.HandlerList;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author Chormon
  */
 public final class LoginPoint extends JavaPlugin {
 
     @Override
     public void onDisable() {
         PluginDescriptionFile pdf = this.getDescription();
        getLogger().log(Level.INFO, "{0} {1} disabled!", new Object[]{pdf.getName(), pdf.getVersion()});
         HandlerList.unregisterAll(this);
     }
 
     @Override
     public void onEnable() {
         PluginDescriptionFile pdf = this.getDescription();
        getLogger().log(Level.INFO, "{0} {1} enabled!", new Object[]{pdf.getName(), pdf.getVersion()});
         new LoginListener(this);
         Config.initConfig(this);
         Location l = Config.getLoginPoint();
         if (l.getY() < 0 || l.getWorld() == null) {
             World world = this.getServer().getWorlds().get(0);
             Location spawn = world.getSpawnLocation();
             Config.setLoginPoint(spawn);
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if (cmd.getName().equalsIgnoreCase("setloginpoint")) {
             if (sender instanceof Player) {
                 Player player = (Player) sender;
                 if (player.hasPermission("loginpoint." + cmd) || player.isOp()) {
                     if (args.length > 4) {
                         sender.sendMessage(Config.getMessage("tooManyParams"));
                         return false;
                     }
                     if (args.length == 0) {
                         Config.setLoginPoint(player.getLocation(), player);
                         return true;
                     }
                     if (args.length == 3) {
                         try {
                             double x = Double.parseDouble(args[0]);
                             double y = Double.parseDouble(args[1]);
                             double z = Double.parseDouble(args[2]);
                             Location location = new Location(player.getWorld(), x, y, z);
                             Config.setLoginPoint(location, sender);
                         } catch (NumberFormatException e) {
                             return false;
                         }
                         return true;
                     }
                     if (args.length == 4) {
                         try {
                             double x = Double.parseDouble(args[0]);
                             double y = Double.parseDouble(args[1]);
                             double z = Double.parseDouble(args[2]);
                             World world = this.getServer().getWorld(args[3]);
                             Location location = new Location(world, x, y, z);
                             Config.setLoginPoint(location, player);
                         } catch (NumberFormatException e) {
                             return false;
                         }
                         return true;
                     }
                     sender.sendMessage(Config.getMessage("notEnoughParams"));
                     return false;
                 } else {
 //                    sender.sendMessage(Config.getMessage("noPerimissions"));
                 }
             } else {
                 if (args.length > 4) {
                     sender.sendMessage(Config.getMessage("tooManyParams"));
                     return false;
                 }
                 if (args.length < 4) {
                     sender.sendMessage(Config.getMessage("notEnoughParams"));
                     return false;
                 }
                 try {
                     double x = Double.parseDouble(args[0]);
                     double y = Double.parseDouble(args[1]);
                     double z = Double.parseDouble(args[2]);
                     World world = this.getServer().getWorld(args[3]);
                     Location location = new Location(world, x, y, z);
                     Config.setLoginPoint(location);
                 } catch (NumberFormatException e) {
                     return false;
                 }
                 return true;
             }
         }
         return false;
     }
 }
