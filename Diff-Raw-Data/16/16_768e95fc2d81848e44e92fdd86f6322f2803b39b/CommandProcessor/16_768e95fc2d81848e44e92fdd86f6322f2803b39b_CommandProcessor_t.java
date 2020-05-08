 /*
  * Copyright 2013 Topher Donovan (themaskedcrusader.com)
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package com.themaskedcrusader.tmcz.command;
 
 import com.themaskedcrusader.bukkit.config.Settings;
 import com.themaskedcrusader.tmcz.settings.Permissions;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CommandProcessor {
     protected static JavaPlugin plugin = null;
     protected static String unauthorized = ChatColor.RED + "Unauthorized: Only an admin may use that command";
 
     public CommandProcessor(JavaPlugin plugin) {
         if (this.plugin == null) {
             this.plugin = plugin;
         }
     }
     
     public void processCommand(CommandSender sender, String[] args) {
         try {
             if (args[0].equalsIgnoreCase("help"))          { CommandHelp.baseCommandHelp(sender);              return; }
             if (args[0].equalsIgnoreCase("spawn"))         { GameCommands.startGame((Player) sender);          return; }
             if (args[0].equalsIgnoreCase("die"))           { GameCommands.killPlayer((Player) sender);         return; }
             if (args[0].equalsIgnoreCase("kills"))         { GameCommands.playerStats((Player) sender);        return; }
             if (args[0].equalsIgnoreCase("addSpawn"))      { WorldCommands.addNewSpawn((Player) sender);       return; }
             if (args[0].equalsIgnoreCase("setWorldSpawn")) { WorldCommands.setWorldSpawn((Player) sender);     return; }
             if (args[0].equalsIgnoreCase("worldName"))     { WorldCommands.tellWorldName((Player) sender);     return; }
             if (args[0].equalsIgnoreCase("disable"))       { PluginCommands.disablePlugin(sender);             return; }
             if (args[0].equalsIgnoreCase("reload"))        { PluginCommands.reloadMe(sender);                  return; }
 
             if (Settings.getConfig().getBoolean("debug")) { // TODO: REMOVE WHEN BETA!!!
                 if (args[0].equalsIgnoreCase("item"))      { DebugCommands.giveItem((Player) sender, args);   return; }
                 if (args[0].equalsIgnoreCase("potion"))    { DebugCommands.potionEffect(sender, args);        return; }
                 if (args[0].equalsIgnoreCase("infect"))    { DebugCommands.infectPlayer(sender, args);        return; }
                 if (args[0].equalsIgnoreCase("food"))      { DebugCommands.setFoodLevel(sender, args);        return; }
             }
 
         } catch (Exception ignored) {
             plugin.getLogger().info(ignored.getMessage());
         }
 
         sender.sendMessage(ChatColor.RED + "[TMCZ] Command not found... please try again");
 
     }
     
     protected static boolean authorized(Player player) {
         return player.isOp() || player.hasPermission(Permissions.COMMANDS);
     }
 
     protected static boolean authorized(CommandSender sender) {
         return (sender instanceof Player && authorized((Player) sender) || sender instanceof CommandProcessor);
     }
 }
