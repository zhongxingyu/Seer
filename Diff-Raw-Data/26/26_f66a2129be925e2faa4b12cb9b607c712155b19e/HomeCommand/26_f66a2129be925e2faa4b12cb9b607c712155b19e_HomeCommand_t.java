 /*
 * Copyright (c) 2012 cedeel.
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
 package com.github.lankylord.SimpleHomes.commands;
 
 import com.github.lankylord.SimpleHomes.SimpleHomes;
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
 public class HomeCommand implements CommandExecutor {
 
     private SimpleHomes instance;
 
     public HomeCommand(SimpleHomes instance) {
         this.instance = instance;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
         if (sender instanceof Player) {
             Player player = (Player) sender;
 
             if (instance.getHomes().contains(player.getName())) {
                 ConfigurationSection home = instance.getHomes().getConfigurationSection(player.getName());
 
                 if (args.length == 0) {
                     if (sender.hasPermission("simplehomes.homes")) {
                         String w = home.getString("world");
                         int x = home.getInt("x"),
                                 y = home.getInt("y"),
                                 z = home.getInt("z");
                         player.teleport(new Location(Bukkit.getWorld(w), x, y, z));
                         sender.sendMessage(ChatColor.YELLOW + "Teleported.");
                         return true;
                     }
                 } else if (args.length == 1) {
                     if (sender.hasPermission("simplehomes.multihomes")) {
                         String w = home.getString(args[0] + ".world");
                         int x = home.getInt(args[0] + ".x"),
                                 y = home.getInt(args[0] + ".y"),
                                 z = home.getInt(args[0] + ".z");
                         player.teleport(new Location(Bukkit.getWorld(w), x, y, z));
                         sender.sendMessage(ChatColor.YELLOW + "Teleported.");
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 }
