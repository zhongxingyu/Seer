 package com.herocraftonline.dev.heroes.command.commands;
 
import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.BaseCommand;
 
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
                sender.sendMessage(ChatColor.RED + "You don't have permission to do this");
                 return;
             }
             try {
                 plugin.getConfigManager().reload();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
 }
