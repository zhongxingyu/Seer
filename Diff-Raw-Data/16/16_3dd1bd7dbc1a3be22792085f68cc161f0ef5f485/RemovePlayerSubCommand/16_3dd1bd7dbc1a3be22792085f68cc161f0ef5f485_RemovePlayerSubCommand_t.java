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
 package uk.co.drnaylor.mcmmopartyadmin.commands.subcommands;
 
 import com.gmail.nossr50.api.PartyAPI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import uk.co.drnaylor.mcmmopartyadmin.PartyAdmin;
 import uk.co.drnaylor.mcmmopartyadmin.dualcommandexecutor.DualSubCommandInterface;
 import uk.co.drnaylor.mcmmopartyadmin.locales.L10n;
 
 public class RemovePlayerSubCommand implements DualSubCommandInterface {
 
     private List<String> permissions = new ArrayList<String>();
     
     public RemovePlayerSubCommand() {
         permissions.add("mcmmopartyadmin.admin");
     }
 
     public List<String> getSubCommands() {
        return Arrays.asList(new String[] {"removeplayer", "kickplayer", "rpl"});
     }
 
     public String getShortHelp() {
         return ChatColor.YELLOW + "/partyadmin rpl <player> " + ChatColor.WHITE + "- " + L10n.getString("Description.Remove");
     }
 
     public String[] getLongHelp() {
         return new String[] { getShortHelp() };
     }
 
     public List<String> getPermissions() {
         return permissions;
     }
 
     public boolean checkPermissions(CommandSender sender) {
         if (!(sender instanceof Player) || sender.isOp()) return true;
         
         for (String p : permissions) {
             if (sender.hasPermission(p)) return true;
         }
         
         return false;
     }
 
     public void executeSubCommand(CommandSender sender, String[] cmdargs) {
          if (cmdargs.length == 1) {
             removePlayerFromParty(sender, cmdargs[0]);
             return;
         }
         sender.sendMessage(ChatColor.RED + "Incorrect usage!");
         sender.sendMessage(getLongHelp());
     }
 
     public List<String> onSubCommandTabComplete(CommandSender sender, String[] args) {
         return null;
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
     
 }
