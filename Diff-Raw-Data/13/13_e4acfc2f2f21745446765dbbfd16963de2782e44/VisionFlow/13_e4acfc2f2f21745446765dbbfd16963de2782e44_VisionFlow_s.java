 package me.muffinjello.visionflow;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class VisionFlow extends JavaPlugin implements Listener {
     //Startup
     @Override
     public void onEnable(){
         PluginDescriptionFile pdfFile = this.getDescription();
         this.getLogger().info(pdfFile.getName() + " version " + pdfFile.getVersion() + " has been enabled!");
     }
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
         if (command.getName().equalsIgnoreCase("vision")){
             if (sender.hasPermission("visionflow.vision")){
                 if (args.length >= 1){
                     String argtwo = args[0].trim();
                     if (argtwo.equalsIgnoreCase("on")){
                        sender.removePotionEffect(PotionEffectType.NIGHT_VISION);
                        sender.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 18000, 1));
                         sender.sendMessage(ChatColor.YELLOW + "Turned vision " + ChatColor.GOLD + "on");
                     } else if(argtwo.equalsIgnoreCase("off")){
                         sender.sendMessage(ChatColor.YELLOW + "Turned vision " + ChatColor.RED + "off");
                        sender.removePotionEffect(PotionEffectType.NIGHT_VISION);
                     }
                 } else {
                     sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                 }
             }
         }
         return false;
     }
 }
