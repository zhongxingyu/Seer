 /**
  * TerraCraftTools(SuperSoapTools) - SuperSoapToolsMeta.java
  * Copyright (c) 2013 Jeremy Koletar (jjkoletar), <http://jj.koletar.com>
  * Copyright (c) 2013 computerdude5000,<computerdude5000@gmail.com>
  *
  * TerraCraftTools is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * TerraCraftTools is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with TerraCraftTools.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.github.computerdude5000.terracrafttools.modules;
 
 import com.github.computerdude5000.terracrafttools.TerraCraftTools;
 import com.github.computerdude5000.terracrafttools.datatypes.MagicStrings;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 import java.util.Random;
 
 /**
  * TerraCraftTools Meta Module
  * <p/>
  * Handles commands for versioning and vanishing
  *
  * @author jjkoletar , computerdude5000
  */
 public class SuperSoapToolsMeta implements SuperSoapToolsModule, Listener
 {
     private TerraCraftTools plugin;
     private MagicStrings magicStrings;
     private static String version = "0.0.40!";
     private static String[] sillyMessages = {"The wonder plugin!",
             "Rinse and repeat!", "Respect and Receive!",
             "Critical!", "Inconceivable!",
             "That's not my wife!", "Undocumented!",
             "600 TB/s over Sneakernet!", "SEEMS LEGIT!!",
             "ROBOTICS RULE", "Buster approves!!!", "Java 7!!",
             "Java Powered!!", "Don't Give comp mountain dew he goes nuts",
             "Causes massive ragefits!!!", "Powahed by the Dew, please Dew responsibly!!",
             "powered by Intel", "YO mamma approved!!!", "mcMMO Powered", "Doesn't include a .exe!!!!",
             "whats java?", "this.SILLINESS string is long!!", "Get crafting!!",
             "Not typo free!!!", "Born This Way!!", "Make me some cake!!", "Don't forget Minecraft Style",
             "Search AND Destroy 'Em all!!", "76.6% Java Coding standards Compliance",
             "Intellj is best IDE", "Jenkins Build me my Code!!", "Sonar Analyze my code NOW!!!",
             "[SuperMod]Gundam: On RC, saying \"Lag\" helps \"Fix\" it", "I've got my eye on you!!", " SCUBA_117 msg to twist lol, \"Lag Wars\" far far away, in a server long forgotten..."};
 
     /**
      * initModule enables the module called by {@link TerraCraftTools}
      */
     public void initModule(TerraCraftTools p)
     {
         plugin = p;
         plugin.commandRegistrar.registerCommand("infiniumcrafttools", this);
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.logger.info(magicStrings.getPluginLoggerTag() + magicStrings.getMetaModuleTag() + " InfiniumCraftToolsMeta Module Enabled!");
     }
 
     /**
      * deinitModule disables the module called by {@link TerraCraftTools}
      */
     public void deinitModule()
     {
         HandlerList.unregisterAll(this);
         plugin.commandRegistrar.unregisterCommand("infiniumcrafttools", this);
         plugin.logger.info(magicStrings.getPluginLoggerTag() + magicStrings.getMetaModuleTag() + " InfiniumCraftToolsMeta Module Disabled!");
         plugin = null;
     }
 
     /**
      * Returns true or false when a player types a command
      *
      * @param sender  sender who sent the message
      * @param command command that was sent
      * @param label   idk what this does.
      * @param args    arguments that were sent along with the command
      * @return true or false
      */
     public boolean callCommand(CommandSender sender, Command command, String label, String[] args)
     {
         if (command.getName().equalsIgnoreCase("InfiniumCraftTools") && args.length == 0 && plugin.permissionApi.has(sender, "terracrafttools.terracrafttools"))
         {
 
             sender.sendMessage(ChatColor.DARK_PURPLE + "InfiniumCraftTools: "
                     + sillyMessages[new Random().nextInt(sillyMessages.length)]);
             sender.sendMessage(ChatColor.DARK_PURPLE + "Originally written by jjkoletar");
             sender.sendMessage(ChatColor.DARK_PURPLE + "Modified Written by Computerdude5000");
             sender.sendMessage(ChatColor.DARK_PURPLE + "Version: " + version);
 
             return true;
         }
         return false;
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerJoin(PlayerJoinEvent event)
     {
         event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "InfiniumCraftTools: " + sillyMessages[new Random().nextInt(sillyMessages.length)]);
         event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Written by Computerdude5000");
         event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Version: " + version);
     }
 }
