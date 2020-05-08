 /*
  * SQLBans
  * Copyright 2012 Matt Baxter
  *
  * Google Gson
  * Copyright 2008-2011 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.kitteh.sqlbans.commands;
 
 import org.kitteh.sqlbans.ChatColor;
 import org.kitteh.sqlbans.Perm;
 import org.kitteh.sqlbans.SQLBans;
 import org.kitteh.sqlbans.Util;
 import org.kitteh.sqlbans.api.CommandSender;
 import org.kitteh.sqlbans.api.Player;
 import org.kitteh.sqlbans.api.SQLBansCommand;
 
 public class KickCommand extends SQLBansCommand {
 
     private final SQLBans plugin;
 
     public KickCommand(SQLBans plugin) {
         super("kick", Perm.COMMAND_KICK, "k");
         this.plugin = plugin;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, String[] args) {
         if (args.length == 0) {
             return false;
         }
 
         final Player target = this.plugin.getPlayer(args[0]);
 
        if (target == null) {
             sender.sendMessage(ChatColor.RED + "Could not find a player named " + args[0]);
             return true;
         }
 
         final String reason = args.length > 1 ? Util.separatistsUnite(args, " ", 1) : "Kicked";
 
         final String kick = SQLBans.Messages.getIngameKicked(target.getName(), reason, sender.getName(), false);
         final String kickAdmin = SQLBans.Messages.getIngameKicked(target.getName(), reason, sender.getName(), true);
         target.kick(SQLBans.Messages.getDisconnectKicked(reason, sender.getName()));
         for (final Player player : this.plugin.getOnlinePlayers()) {
            if (player != null) {
                 if (player.hasPermission(Perm.MESSAGE_KICK_ADMIN)) {
                     player.sendMessage(kickAdmin);
                 } else if (player.hasPermission(Perm.MESSAGE_KICK_NORMAL)) {
                     player.sendMessage(kick);
                 }
             }
         }
         this.plugin.getLogger().info(kickAdmin);
         return true;
     }
 
 }
