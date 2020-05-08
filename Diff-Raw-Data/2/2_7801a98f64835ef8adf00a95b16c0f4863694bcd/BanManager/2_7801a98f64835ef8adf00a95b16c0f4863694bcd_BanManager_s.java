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
 package be.darnell.superbans.bans;
 
 import be.darnell.superbans.SuperBans;
 import be.darnell.superbans.storage.FlatFileStore;
 import be.darnell.superbans.storage.SuperBanStore;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.CommandSender;
 
 import java.io.File;
 import java.util.List;
 
 /**
  * A ban manager that handles the creation and removal of bans in SuperBans
  * @author cedeel
  */
 public class BanManager {
     private SuperBans plugin;
     private SuperBanStore store;
 
     public BanManager(SuperBans instance) {
         plugin = instance;
 
         // Select ban storage
         switch (plugin.getConfig().getString("Storage", "Flat").toLowerCase()) {
             case "mysql":
                 // Select MySQL storage
             default:
                 // Select flat file storage
                store = new FlatFileStore(new File(plugin.getDataFolder() + "bans.yml"));
         }
         // TODO: Add logic for selecting a ban store
     }
 
     /**
      * Ban a user from logging on.
      * @param sender The user issuing the ban
      * @param target The user being banned
      * @param reason The reason for the ban
      */
     public void ban(CommandSender sender, OfflinePlayer target, String reason) {
         plugin.debug(sender.getName() + ": Running ban for " + target.getName() + ".");
         store.ban(new Ban(target.getName(), BanType.REGULAR, reason));
     }
 
     /**
      * Temporarily ban a user from logging on.
      * @param sender The user issuing the ban
      * @param target The user being banned
      * @param duration The duration of the ban, in milliseconds
      * @param reason The reason for the ban
      */
     public void tempBan(CommandSender sender, OfflinePlayer target, String reason, long duration) {
         plugin.debug(sender.getName() + ": Running tempban for " + target.getName() + ".");
         store.ban(new Ban(target.getName(), BanType.TEMPORARY, reason, duration));
     }
 
     /**
      * Ban a user by his/her IP address, preventing any accounts from logging in from that address
      * @param sender The user issuing the ban
      * @param target The user being banned
      * @param reason The reason for the ban
      */
     public void ipBan(CommandSender sender, OfflinePlayer target, String reason) {
         plugin.debug(sender.getName() + ": Running IP ban for " + target.getName() + ".");
         store.ban(new Ban(target.getName(), BanType.IP, reason));
     }
 
     /**
      * Temporarily ban a user by his/her IP address
      * @param sender The user issuing the ban
      * @param target The user being banned
      * @param reason The reason for the ban
      * @param duration The duration of the ban
      */
     public void tempIpBan(CommandSender sender, OfflinePlayer target, String reason, long duration) {
         plugin.debug(sender.getName() + ": Running temp IP ban for " + target.getName() + ".");
         store.ban(new Ban(target.getName(), BanType.IP_TEMPORARY, reason, duration));
     }
 
     /**
      * Check if a user is currently banned
      * @param sender The user checking for the ban status
      * @param target The user whose ban status is being checked
      * @return Whether the user is banned
      */
     public boolean isBanned(CommandSender sender, OfflinePlayer target) {
         return store.isBanned(target.getName());
     }
 
     /**
      * Gets a history of infractions of the selected user
      * @param sender The user looking up infractions
      * @param target The user in question
      * @return A list of infractions
      */
     public List<Ban> getHistory(CommandSender sender, OfflinePlayer target) {
         plugin.debug(sender.getName() + ": Getting ban history of " + target.getName() + ".");
         return store.getBans(target.getName());
     }
 
     /**
      * Lift a ban on a user
      * @param sender The user lifting the ban
      * @param target The user whose ban if being lifted
      */
     public void unban(CommandSender sender, OfflinePlayer target) {
         plugin.debug(sender.getName() + ": Running unban for " + target.getName() + ".");
         if(store.isBanned(target.getName()))
             store.unban(target.getName());
     }
 }
