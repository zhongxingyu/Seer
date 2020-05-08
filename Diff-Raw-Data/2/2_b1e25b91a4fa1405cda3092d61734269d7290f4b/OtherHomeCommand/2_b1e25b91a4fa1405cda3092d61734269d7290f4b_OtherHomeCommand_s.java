 /*
  * Copyright (c) 2013 cedeel.
  * All rights reserved.
  * 
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * The name of the author may not be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS''
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package net.lankylord.simplehomes.commands;
 
 import net.lankylord.simplehomes.SimpleHomes;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author cedeel
  */
 public class OtherHomeCommand implements CommandExecutor {
 
     private SimpleHomes instance;
 
     public OtherHomeCommand(SimpleHomes instance) {
         this.instance = instance;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
        if (sender instanceof Player && sender.hasPermission("simplehomes.otherhomes")) {
             Player p = (Player) sender;
             String home = "default";
             if (args.length == 2)
                 home = args[1].toLowerCase();
 
             String target = args[0].toLowerCase();
 
             if (instance.getHomes().contains(target + "." + home)) {
                 ConfigurationSection homes = instance.getHomes().getConfigurationSection(target + "." + home);
                 String w = homes.getString("world");
                 int x = homes.getInt("x"),
                         y = homes.getInt("y"),
                         z = homes.getInt("z");
                 p.teleport(new Location(Bukkit.getWorld(w), x, y, z));
                 p.sendMessage(ChatColor.YELLOW + "Teleported to " + target + "'s home.");
                 return true;
             }
         }
         return false;
     }
 }
