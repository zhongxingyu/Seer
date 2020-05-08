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
 
 import com.gmail.nossr50.api.ChatAPI;
 import com.gmail.nossr50.api.PartyAPI;
 import com.gmail.nossr50.datatypes.party.Party;
 import com.gmail.nossr50.datatypes.player.McMMOPlayer;
 import com.gmail.nossr50.events.party.McMMOPartyChangeEvent;
 import com.gmail.nossr50.party.PartyManager;
 import com.gmail.nossr50.util.player.UserManager;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.TabExecutor;
 import org.bukkit.entity.Player;
 import uk.co.drnaylor.mcmmopartyadmin.PartyAdmin;
 import uk.co.drnaylor.mcmmopartyadmin.Util;
 import uk.co.drnaylor.mcmmopartyadmin.locales.L10n;
 import uk.co.drnaylor.mcmmopartyadmin.permissions.PermissionHandler;
 
 public class PartyAdminCommand implements TabExecutor {
 
     private final String[] commands = {"apl", "chown", "pc", "rp", "rpl"};
     private final String[] addplayer = {"addplayer", "apl"};
     private final String[] changeowner = {"chown", "changeowner"};
     private final String[] removeplayer = {"removeplayer", "kickplayer", "rpl"};
     private final String[] removeparty = {"removeparty", "remparty", "delparty", "rp"};
     private final String[] chat = {"chat", "pc"};
     
     private HashMap<CommandSender,List<String>> auto;
 
     public PartyAdminCommand() {
         this.auto = new HashMap<CommandSender,List<String>>();
     }
 
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         Player player = null;
         if (sender instanceof Player) {
             player = (Player) sender;
         }
 
         if (player == null || PermissionHandler.isAdmin(player)) {
 
             if (args.length > 2 && (Arrays.asList(chat).contains(args[0]))) {
                 StringBuilder a = new StringBuilder();
 
                 for (int i = 2; i < args.length; i++) {
                     a.append(args[i]);
                     if (i != args.length - 1) {
                         a.append(" ");
                     }
                 }
 
                 partyChat(sender, args[1], a.toString());
                 return true;
             }
 
             switch (args.length) {
                 case 1:
                     if (args[0].equalsIgnoreCase("list")) {
                         listParties(sender);
                         return true;
                     }
                     listCommands(sender);
                     return true;
                 case 2:
                     if (Arrays.asList(removeparty).contains(args[0])) {
                         disbandParty(sender, args[1]);
                         return true;
                     } else if (Arrays.asList(removeplayer).contains(args[0])) {
                         removePlayerFromParty(sender, args[1]);
                         return true;
                     } else {
                         listCommands(sender);
                         return true;
                     }
 
                 case 3:
                     if (Arrays.asList(addplayer).contains(args[0])) {
                         addPlayerToParty(sender, args[1], args[2]);
                         return true;
                     } else if (Arrays.asList(changeowner).contains(args[0])) {
                         changePartyOwner(sender, args[1], args[2]);
                         return true;
                     } else if (Arrays.asList(chat).contains(args[0])) {
                         partyChat(sender, args[1], args[2]);
                     } else {
                         listCommands(sender);
                         return true;
                     }
                 default:
                     listCommands(sender);
                     return true;
             }
         } else {
             // No perms! Leave us be!
             player.sendMessage(L10n.getString("Commands.NoPermission"));
         }
 
         return true;
     }
 
     /**
      * Suggests tab completions, replacing players with parties.
      *
      * @param sender
      * @param command
      * @param alias
      * @param args
      * @return
      */
     public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
 
         switch (args.length) {
             case 1: // If we have no arguments to the command yet
                 return Arrays.asList(commands);
             case 2: 
                 if ((Arrays.asList(removeparty).contains(args[0])) || (Arrays.asList(chat).contains(args[0]))) {
                     List<String> collection = Util.getPartyCollection(); // Get the list of parties
                     
                     if (args[1].isEmpty()) {
                         return collection; // If we haven't started typing a party name yet, return the whole list
                     }
                         
                     if (collection.contains(args[1]) && auto.get(sender) != null) {
                         return auto.get(sender); // If it seems that we have tabbed before, and not got the right party, just continue
                     }
                     
                     auto.remove(sender); // Otherwise, if the argument is user altered, then remove the previous list, and build a new one
                     List<String> c = new ArrayList<String>();
                     for (String s : collection) {
                         if (s.toLowerCase().startsWith(args[1].toLowerCase())) { // Check to see if the substring is contained in any parties
                             c.add(s);
                         }
                     }
                     auto.put(sender,c); // Store the list for if we "double tab"
                     return c; // Return this list
                 }
                 return null;
             case 3:
                 if ((Arrays.asList(addplayer).contains(args[0])) || (Arrays.asList(changeowner).contains(args[0]))) {
                     List<String> collection = Util.getPartyCollection();
                     
                     if (args[2].isEmpty()) {
                         return collection;   
                     }
                     
                     if (collection.contains(args[2]) && auto.get(sender) != null) {
                         return auto.get(sender);
                     }
                     
                     auto.remove(sender);
                     List<String> c = new ArrayList<String>();
                     for (String s : collection) {
                         if (s.toLowerCase().startsWith(args[2].toLowerCase())) {
                             c.add(s);
                         }
                     }
                     auto.put(sender,c);
                     return c;
                 }
                 return null;
             default:
                 return null;
         }
     }
 
     /**
      * Sends a list of all parties and it's members to the requester.
      *
      * @param sender Requester to send the list to.
      */
     private void listParties(CommandSender sender) {
         // Get ALL the parties!
         List<Party> parties = PartyAPI.getParties();
 
         // No parties? That's a shame...
         if (parties.isEmpty()) {
             sender.sendMessage(L10n.getString("Commands.List.NoParties"));
         } else {
             // Header
             sender.sendMessage(L10n.getString("Commands.List.PartyListHeader"));
             sender.sendMessage(ChatColor.DARK_AQUA + "===============");
 
             // This next bit has no need for localisation
 
             // Over each party...
             for (Party a : parties) {
 
                 // Get Party Leader
                 String leader = PartyAPI.getPartyLeader(a.getName());
 
                 // Start building new string
                 StringBuilder tempList = new StringBuilder();
 
                 tempList.append(ChatColor.DARK_AQUA);
                 tempList.append(a.getName());
                 tempList.append(":");
 
                 // Over all players
                 for (OfflinePlayer otherPlayerName : a.getMembers()) {
                     tempList.append(" ");
                     if (leader.equals(otherPlayerName.getName())) {
                         // Leader in Gold
                         tempList.append(ChatColor.GOLD);
                     } else if (otherPlayerName.isOnline()) {
                         // Online players in White
                         tempList.append(ChatColor.WHITE);
                     } else {
                         // Offline players in Grey
                         tempList.append(ChatColor.GRAY);
                     }
                     // Add name and space
                     tempList.append(otherPlayerName.getName());
                 }
 
                 // Send the message
                 sender.sendMessage(tempList.toString());
             }
         }
     }
 
     /**
      * Disbands an mcMMO party.
      *
      * @param sender Player or console who requested the disband
      * @param party Party to disband
      */
     private void disbandParty(CommandSender sender, String party) {
         Party target = Util.getPartyFromList(party);
 
         if (target == null) {
             sender.sendMessage(L10n.getString("Party.DoesNotExist", party));
             return;
         }
 
         // From Party Disband command
 
         for (Player member : target.getOnlineMembers()) {
             if (!PartyManager.handlePartyChangeEvent(member, target.getName(), null, McMMOPartyChangeEvent.EventReason.KICKED_FROM_PARTY)) {
                 sender.sendMessage(L10n.getString("Commands.Disband.Fail", party));
                 return;
             }
 
             member.sendMessage(L10n.getString("Commands.Disband.ByAdmin"));
         }
 
         //It would be nice to get API to do this.
         PartyManager.disbandParty(target);
 
         sender.sendMessage(L10n.getString("Commands.Disband.Success", party));
     }
 
     /**
      * Removes a player from their party.
      *
      * @param sender Player requesting removal
      * @param player Name of player to remove
      */
     private void removePlayerFromParty(CommandSender sender, String player) {
         Player targetPlayer = PartyAdmin.getPlugin().getServer().getPlayer(player);
 
         // If the player is online
         if (targetPlayer != null) {
             // Is the player in a party?
             if (PartyAPI.inParty(targetPlayer)) {
                 // Remove from the party!
                 PartyAPI.removeFromParty(targetPlayer);
 
                 // Tell them, and the sender
                targetPlayer.sendMessage(L10n.getString("Commands.Kicked.ByAdmin"));
                sender.sendMessage(L10n.getString("Commands.Kicked.Success", targetPlayer.getName()));
             } else {
                 // Tell the sender that the can't do that!
                 sender.sendMessage(L10n.getString("Player.NotInParty", targetPlayer.getName()));
             }
         } else {
             sender.sendMessage(L10n.getString("Player.NotOnline", player));
         }
     }
 
     /**
      * Adds a player to a party.
      *
      * @param sender Player requesting the addition
      * @param player Player to add to party
      * @param partyName Party to add player to
      */
     private void addPlayerToParty(CommandSender sender, String player, String partyName) {
         // Get the OfflinePlayer
         Player targetPlayer = PartyAdmin.getPlugin().getServer().getPlayerExact(player);
 
         Party party = Util.getPartyFromList(partyName);
 
         // No party!
         if (party == null) {
             sender.sendMessage(L10n.getString("Party.DoesNotExist", partyName));
             return;
         } else if (targetPlayer == null) {
             sender.sendMessage(L10n.getString("Player.NotOnline", player));
             return;
         }
 
         if (PartyAPI.inParty(targetPlayer)) {
             PartyAPI.removeFromParty(targetPlayer);
         }
 
         // If the player is online, we can add them to the party using the API
         PartyAPI.addToParty(targetPlayer, partyName);
         // Check to see that it happened and the event wasn't cancelled.
         if (PartyAPI.getPartyName(targetPlayer).equals(partyName)) {
             sender.sendMessage(L10n.getString("Commands.Added.Success", targetPlayer.getName(), partyName));
         } else {
             sender.sendMessage(L10n.getString("Commands.Added.Failed", targetPlayer.getName(), partyName));
         }
     }
 
     /**
      * Change the owner of a party.
      *
      * @param sender Player requesting the change
      * @param player Player to make the owner
      * @param partyName Party to make them the owner of
      */
     private void changePartyOwner(CommandSender sender, String player, String partyName) {
         OfflinePlayer targetPlayer = PartyAdmin.getPlugin().getServer().getOfflinePlayer(player);
 
         if (targetPlayer == null) {
             // Player doesn't exist
             sender.sendMessage(L10n.getString("Player.NotFound", player));
             return;
         }
 
         McMMOPlayer mcplayer = UserManager.getPlayer(targetPlayer);
         Party party = mcplayer.getParty();
 
         if (party.getName().equals(partyName)) {
             PartyAPI.setPartyLeader(partyName, targetPlayer.getName());
             sender.sendMessage(L10n.getString("Commands.ChangeOwner.Success", player, partyName));
         } else {
             sender.sendMessage(L10n.getString("Commands.ChangeOwner.NotInParty", player, partyName));
         }
     }
 
     /**
      * Send a message to the specified party.
      *
      * @param sender Player sending the message
      * @param args
      */
     private void partyChat(CommandSender sender, String party, String message) {
         if (Util.getPartyFromList(party) == null) {
             sender.sendMessage(L10n.getString("Party.DoesNotExist", party));
             return;
         }
 
         if (!(sender instanceof Player)) {
             ChatAPI.sendPartyChat(PartyAdmin.getPlugin(), L10n.getString("Console.Name"), party, message);
         } else {
             ChatAPI.sendPartyChat(PartyAdmin.getPlugin(), ((Player) sender).getDisplayName(), party, message);
         }
 
         if (sender instanceof Player) {
             Player send = (Player) sender;
             if (!PartyAdmin.getPlugin().getPartySpyHandler().isSpy(send)) {
                 sender.sendMessage(L10n.getString("PartySpy.Off"));
                 String p2 = ChatColor.GRAY + "[" + party + "] " + ChatColor.GREEN + " (" + ChatColor.WHITE + ((Player) sender).getDisplayName() + ChatColor.GREEN + ") ";
                 sender.sendMessage(p2 + message);
             }
         }
     }
 
     /**
      * Send a list of the permissible commands to the sender.
      *
      * @param player CommandSender to send the messages to.
      */
     private void listCommands(CommandSender player) {
         player.sendMessage(ChatColor.DARK_AQUA + "mcMMO Party Admin v" + PartyAdmin.getPlugin().getDescription().getVersion()); //No need to localise this line
         player.sendMessage(ChatColor.DARK_AQUA + "=================");
         player.sendMessage(ChatColor.YELLOW + "/partyadmin list " + ChatColor.WHITE + "- " + L10n.getString("Description.List"));
         player.sendMessage(ChatColor.YELLOW + "/partyadmin rp <party> " + ChatColor.WHITE + "- " + L10n.getString("Description.Disband"));
         player.sendMessage(ChatColor.YELLOW + "/partyadmin apl <player> <party> " + ChatColor.WHITE + "- " + L10n.getString("Description.Add"));
         player.sendMessage(ChatColor.YELLOW + "/partyadmin rpl <player> " + ChatColor.WHITE + "- " + L10n.getString("Description.Remove"));
         player.sendMessage(ChatColor.YELLOW + "/partyadmin chown <player> <party> " + ChatColor.WHITE + "- " + L10n.getString("Description.ChangeOwner"));
         player.sendMessage(ChatColor.YELLOW + "/partyadmin pc <party> " + ChatColor.WHITE + "- " + L10n.getString("Description.PartyChat"));
     }
 }
