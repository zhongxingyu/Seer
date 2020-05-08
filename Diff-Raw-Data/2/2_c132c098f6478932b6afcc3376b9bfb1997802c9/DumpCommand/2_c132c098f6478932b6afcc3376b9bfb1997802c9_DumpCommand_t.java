 package me.HariboPenguin.PermissionFinder;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.permissions.Permission;
 import org.bukkit.plugin.Plugin;
 
 public class DumpCommand implements CommandExecutor {
     
     public PermissionFinder plugin;
     
     public DumpCommand(PermissionFinder instance) {
         this.plugin = instance;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args) {
         
        if (args.length == 1) {
             
             if (Bukkit.getPluginManager().getPlugin(args[0]) != null) {
                 Plugin enteredPlugin = Bukkit.getPluginManager().getPlugin(args[0]);
                 
                 List permList = enteredPlugin.getDescription().getPermissions();
                 
                 if (permList.isEmpty()) {
                     sender.sendMessage(plugin.prefix + ChatColor.RED + "No permission nodes were found for that plugin");
                 } else {
                     
                     int listSize = permList.size();
                     int counter = 0;
                     
                     plugin.getDataFolder().mkdir();
                     File dumpFile = new File(plugin.getDataFolder().getPath() + File.separatorChar + enteredPlugin.getName() + "-perms.txt");
                     try {
                         dumpFile.createNewFile();
                         
                         BufferedWriter dumpOut = new BufferedWriter(new FileWriter(dumpFile));
                         
                         dumpOut.write("---------- Permission nodes for " + enteredPlugin.getName() + " ----------");
                         
                         while (counter < listSize) {
                             Permission permissionNode = (Permission) permList.get(counter);
                             
                             if (args.length == 1) {
                                 
                                 dumpOut.newLine();
                                 dumpOut.write(permissionNode.getName() + " - " + permissionNode.getDescription());
                                 
                                 counter++;
                                 
                             }      
                         }
                         
                         dumpOut.close();
                         sender.sendMessage(plugin.prefix + ChatColor.GREEN + "Permission nodes successfully dumped to: " + dumpFile.getPath());
                         
                     } catch (IOException ex) {
                         Logger.getLogger(DumpCommand.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }  
             } else {
                 sender.sendMessage(plugin.prefix + ChatColor.RED + "Plugin is not enabled!");
             } 
         } else {
             sender.sendMessage(plugin.prefix + ChatColor.RED + "Command usage is: /dumpperms [Plugin]");
         }
         return true;
     }
     
 }
