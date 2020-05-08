 /*
 * Copyright (c) 2013 cedeel.
 * All rights reserved.
 *
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
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
 package be.darnell.superbans.commands;
 
 import be.darnell.superbans.SuperBans;
 import be.darnell.superbans.util.Formatting;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.CommandSender;
 import org.bukkit.permissions.PermissionDefault;
 
 import java.util.List;
 
 public class TempbanCommand extends SuperBansCommand {
 
     public TempbanCommand(SuperBans plugin) {
         super(plugin);
         this.setName("SuperBans: Tempban");
         this.setCommandUsage("/tempban <user> <amount> <unit> [reason]");
         this.setArgRange(3, 4);
         this.addKey("superbans tempban");
         this.addKey("sb tempban");
         this.addKey("tempban");
        this.setPermission("superbans.tempban", "Allows this user to temporarily ban other users.", PermissionDefault.OP);
     }
     @Override
     public void runCommand(CommandSender sender, List<String> args) {
         OfflinePlayer target = plugin.getServer().getOfflinePlayer(args.get(0));
         if (target == null) {
             sender.sendMessage(colour2 + "The target " + colour1 + args.get(0) + colour2 + " doesn't exist.");
             return;
         }
         long duration = Formatting.parseTimeSpec(args.get(1), args.get(2));
         String reason = args.get(3);
         if(reason == null) reason = plugin.getDefaultReason();
         plugin.getBanManager().tempBan(sender, target, reason, duration);
     }
 }
