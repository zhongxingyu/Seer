     package com.jamesst20.jcommandessentials.Commands;
 
     import com.jamesst20.jcommandessentials.Utils.Methods;
     import com.jamesst20.jcommandessentials.Utils.WarpConfig;
     import org.bukkit.ChatColor;
     import org.bukkit.Location;
     import org.bukkit.command.Command;
     import org.bukkit.command.CommandExecutor;
     import org.bukkit.command.CommandSender;
     import org.bukkit.entity.Player;
 
     import java.util.ArrayList;
 
     public class WarpCommand implements CommandExecutor {
 
         @Override
         public boolean onCommand(CommandSender cs, Command command, String cmd, String[] args) {
             if (args.length == 1) {
                 if (args[0].equalsIgnoreCase("list")){
                     if (!Methods.hasPermissionTell(cs, "JCMDEss.commands.warp.list")){
                         return true;
                     }
                     ArrayList<String> warpsName = WarpConfig.getWarpsName();
                     if (warpsName.size() < 1){
                         Methods.sendPlayerMessage(cs, ChatColor.RED + "There is no warps available.");
                         return true;
                     }
                     StringBuilder msgToSend = new StringBuilder("Available Warps (" + Methods.red(String.valueOf(warpsName.size())) + ") : ");
                     for (String name:warpsName){
                         msgToSend.append(Methods.red(name) + ", ");
                     }
                     msgToSend = Methods.replaceLast(msgToSend, ", ", ".");
                     Methods.sendPlayerMessage(cs, msgToSend.toString());
                     return true;
                 }else {
                     if (Methods.isConsole(cs)) {
                         Methods.sendPlayerMessage(cs, ChatColor.RED + "The console can't teleport to warps.");
                         return true;
                     }
                     if (!Methods.hasPermissionTell(cs, "JCMDEss.commands.warp.tp")) {
                         return true;
                     }
                     Location location = WarpConfig.getWarpLocation(args[0]);
                     if (location != null) {
                         Player player = (Player) cs;
                         player.teleport(location);
                         Methods.sendPlayerMessage(player, "You have teleported to " + args[0] + ".");
                     } else {
                         Methods.sendPlayerMessage(cs, ChatColor.RED + "The warp " + args[0] + " doesn't exist.");
                         return true;
                     }
                 }
             } else if (args.length == 2) {
                 if (args[0].equalsIgnoreCase("add")) {
                     if (Methods.isConsole(cs)) {
                         Methods.sendPlayerMessage(cs, ChatColor.RED + "The console can't add warps.");
                         return true;
                     }
                     if (!Methods.hasPermissionTell(cs, "JCMDEss.commands.warp.edit")) {
                         return true;
                     }
                     WarpConfig.createWarp((Player) cs, args[1]);
                 } else if (args[0].equalsIgnoreCase("delete")) {
                     if (!Methods.hasPermissionTell(cs, "JCMDEss.commands.warp.edit")) {
                         return true;
                     }
                    WarpConfig.deleteWarp(cs, args[1]);
                 } else {
                     return false;
                 }
             } else {
                 return false;
             }
             return true;
         }
     }
