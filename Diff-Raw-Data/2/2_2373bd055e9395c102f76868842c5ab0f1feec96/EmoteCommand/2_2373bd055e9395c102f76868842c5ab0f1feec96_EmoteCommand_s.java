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
 package be.darnell.mc.FuzzyMessenger.commands;
 
 import be.darnell.mc.FuzzyMessenger.MuteManager;
 import be.darnell.mc.FuzzyMessenger.PrivateMessaging;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class EmoteCommand implements CommandExecutor {
 
     private MuteManager manager;
 
     public EmoteCommand(MuteManager mm) {
         manager = mm;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String alias, String... args) {
         emote(sender, PrivateMessaging.constructMessage(args, 0));
         return true;
     }
 
     private void emote(CommandSender sender, String message) {
         if (sender instanceof Player) {
             Player player = (Player) sender;
             if (player.hasPermission("fuzzymessenger.me")) {
                if (manager.getAll().containsKey(player.getName())) {
                     player.sendMessage(ChatColor.RED + "You can't use emotes while muted.");
                 } else {
                     Bukkit.getServer().broadcastMessage(ChatColor.DARK_GRAY + "* "
                             + ChatColor.WHITE + player.getDisplayName()
                             + " " + message + ChatColor.DARK_GRAY + " *");
                 }
             } else {
                 player.sendMessage(ChatColor.RED + "You don't have permission to use emotes.");
             }
         } else {
             Bukkit.getServer().broadcastMessage(ChatColor.DARK_GRAY + "* "
                     + ChatColor.WHITE + "Console "
                     + message
                     + ChatColor.DARK_GRAY + " *");
         }
     }
 }
