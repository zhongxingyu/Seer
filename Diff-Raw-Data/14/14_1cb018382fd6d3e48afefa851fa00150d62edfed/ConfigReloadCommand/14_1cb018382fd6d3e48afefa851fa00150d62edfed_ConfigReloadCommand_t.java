 package com.herocraftonline.dev.heroes.command.commands;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.BaseCommand;
import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class ConfigReloadCommand extends BaseCommand {
 
     public ConfigReloadCommand(Heroes plugin) {
         super(plugin);
         setName("Reload");
         setDescription("Reloads the heroes config file");
         setUsage("/hero admin reload");
         setMinArgs(0);
         setMaxArgs(0);
         getIdentifiers().add("hero admin reload");
         setPermissionNode("heroes.admin.reload");
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         if (sender instanceof Player) {
             if (!Heroes.Permissions.has((Player) sender, "heroes.admin.reload")) {
                Messaging.send(sender, "Insufficient permission.");
                 return;
             }
             try {
                 plugin.getConfigManager().reload();
                Messaging.send(sender, "Configs reloaded.");
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
 }
