 package org.hopto.seed419;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.hopto.seed419.file.Config;
 import org.hopto.seed419.file.FileHandler;
 import org.hopto.seed419.listeners.VictoryPlaceListener;
 import org.hopto.seed419.listeners.WoolFindListener;
 
 import java.util.List;
 
 /**
  * Attribute Only (Public) License
  * Version 0.a3, July 11, 2011
  * <p/>
  * Copyright (C) 2012 Blake Bartenbach <seed419@gmail.com> (@seed419)
  * <p/>
  * Anyone is allowed to copy and distribute verbatim or modified
  * copies of this license document and altering is allowed as long
  * as you attribute the author(s) of this license document / files.
  * <p/>
  * ATTRIBUTE ONLY PUBLIC LICENSE
  * TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
  * <p/>
  * 1. Attribute anyone attached to the license document.
  * Do not remove pre-existing attributes.
  * <p/>
  * Plausible attribution methods:
  * 1. Through comment blocks.
  * 2. Referencing on a site, wiki, or about page.
  * <p/>
  * 2. Do whatever you want as long as you don't invalidate 1.
  *
  * @license AOL v.a3 <http://aol.nexua.org>
  */
 public class CTMSocial extends JavaPlugin {
 
 
     private final FileHandler fh = new FileHandler(this);
     private final Menu menu = new Menu(this, fh);
 
 
     @Override
     public void onEnable() {
         fh.checkFiles();
         registerEnabledListeners();
     }
 
     private void registerEnabledListeners() {
         PluginManager pm = getServer().getPluginManager();
         if (this.getConfig().getBoolean(Config.announceVMPlacement)) {
             pm.registerEvents(new VictoryPlaceListener(this, fh), this);
         }
         if (this.getConfig().getBoolean(Config.announceWoolFinds)) {
             pm.registerEvents(new WoolFindListener(this, fh), this);
         }
 /*
         No book api yet.
         if (this.getConfig().getBoolean(Config.announceBookFinds)) {
             pm.registerEvents(new BookListener(this, fh), this);
         }*/
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (label.equalsIgnoreCase("ctm")) {
             if (args.length == 0) {
                 Menu.showMenu(sender);
             } else  {
                 String arg = args[0];
                 if (arg.equalsIgnoreCase("world")) {
                     if (hasPerms(sender)) {
                         menu.handleWorldMenu(sender, args);
                     } else {
                         sendPermissionsMessage(sender);
                     }
                     return true;
                 }
             }
         } else if (label.equalsIgnoreCase("vm")) {
             if (sender instanceof Player) {
                 menu.handleVMList((Player)sender);
             } else {
                 sender.sendMessage("You must be a player to view the VM list");
             }
         }
         return false;
     }
 
     public boolean isEnabledWorld(String worldName) {
         for (String x : (List<String>) getConfig().getList(Config.enabledWorlds)) {
             if (x.equals(worldName)) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean hasPerms(CommandSender sender) {
        return (sender.hasPermission("shs.*") || sender.isOp());
     }
 
     public void sendPermissionsMessage(CommandSender sender) {
         sender.sendMessage(ChatColor.RED + "You don't have sufficient permission.");
     }
 }
