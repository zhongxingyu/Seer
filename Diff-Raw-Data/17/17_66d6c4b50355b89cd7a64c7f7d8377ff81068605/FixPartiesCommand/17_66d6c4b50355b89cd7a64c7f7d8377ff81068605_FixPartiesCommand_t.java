 /* 
  * Copyright (C) 2013 Dr Daniel R. Naylor
  * 
  * This file is part of mcMMO Party Admin.
  *
  * mcMMO Party Admin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * mcMMO Party Admin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with mcMMO Party Admin.  If not, see <http://www.gnu.org/licenses/>.
  * 
  **/
 package uk.co.drnaylor.mcmmopartyadmin.commands;
 
 import com.gmail.nossr50.api.PartyAPI;
 import com.gmail.nossr50.datatypes.party.Party;
 import com.gmail.nossr50.party.PartyManager;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import uk.co.drnaylor.mcmmopartyadmin.PartyAdmin;
 import uk.co.drnaylor.mcmmopartyadmin.Util;
 import uk.co.drnaylor.mcmmopartyadmin.locales.L10n;
 
 public class FixPartiesCommand implements CommandExecutor {
 
     public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
         /* We assume the following:
          * 
          * 1) Command /fixparties has been requested
          * 2) Player has permission
          */
 
        /* This requires hooking into mcMMO 1.4 internals and as such may break with an update 
         * Works with 1.4-1.4.03         
         */
 
         // Avoiding concurrent modifcation exceptions
         List<Party> parties = new ArrayList<Party>(PartyAPI.getParties());
         Map<OfflinePlayer, Party> playermap = new HashMap<OfflinePlayer, Party>();
 
         for (Party p : parties) {
             // Avoiding concurrent modifcation exceptions
             List<OfflinePlayer> pl = new ArrayList<OfflinePlayer>(p.getMembers());
 
             for (OfflinePlayer player : pl) {
                 // If player is online and not put into the playermap yet
                if (player.isOnline()) {
                    if (playermap.containsKey(player)) {
                        cs.sendMessage(L10n.getString("Commands.FixParties.RemoveDuplicate", player.getName(), p.getName()));
                        PartyAdmin.getPlugin().getLogger().info(L10n.getString("Commands.FixParties.RemoveDuplicate", player.getName(), p.getName()));
                        p.getMembers().remove(player); // Player is alread registered, so remove this entry
                        continue;
                    }
                    
                     Party a;
                     if (PartyAPI.inParty(player.getPlayer())) {
                       a = Util.getPartyFromList(PartyAPI.getPartyName(player.getPlayer()));
                     } else {
                       a = null;
                     }
                     
                     if (a != p) { // Not in the party we are checking
                        cs.sendMessage(L10n.getString("Commands.FixParties.RemoveDuplicate", player.getName(), p.getName()));
                        PartyAdmin.getPlugin().getLogger().info(L10n.getString("Commands.FixParties.RemoveDuplicate", player.getName(), p.getName()));
                         p.getMembers().remove(player);
                     } else { // We are in the party. We can make all checks as normal after this one.
                         playermap.put(player, a);
                     }
                     continue;
                 }
 
                 if (playermap.containsKey(player)) { // This means we have a duplicate.
 
                     if (playermap.get(player) == p) { // Duplication in the same party.
                         cs.sendMessage(L10n.getString("Commands.FixParties.RemoveDuplicate", player.getName(), p.getName()));
                         PartyAdmin.getPlugin().getLogger().info(L10n.getString("Commands.FixParties.RemoveDuplicate", player.getName(), p.getName()));
                         p.getMembers().remove(p.getMembers().lastIndexOf(player)); // Remove an instance
                         continue;
                     }
 
                     // Player is the leader in this party, but not online. We want to move players only if they are offline...
                     if (p.getLeader().equals(player.getName()) && !player.isOnline()) {
                         playermap.get(player).getMembers().remove(player); // Remove player from previous party
                         playermap.remove(player); // Remove from player map
                         playermap.put(player, p); // Add to player map a new
                     } else {
                         cs.sendMessage(L10n.getString("Commands.FixParties.RemoveDuplicate", player.getName(), p.getName()));
                         PartyAdmin.getPlugin().getLogger().info(L10n.getString("Commands.FixParties.RemoveDuplicate", player.getName(), p.getName()));
                         p.getMembers().remove(player);
                         continue;
                     }
                 } else {
                     playermap.put(player, p); // Put the OfflinePlayer into the playermap
                 }
             }
 
             if (p.getMembers().isEmpty()) {
                 PartyManager.disbandParty(p);
             }
         }
 
 
 
         cs.sendMessage(L10n.getString("Commands.FixParties.Success"));
 
         return true;
     }
 }
