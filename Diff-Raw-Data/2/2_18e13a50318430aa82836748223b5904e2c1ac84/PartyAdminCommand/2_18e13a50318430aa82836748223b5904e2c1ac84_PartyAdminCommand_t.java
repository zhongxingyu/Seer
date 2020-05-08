 package uk.co.drnaylor.mcmmopartyadmin;
 
 import com.gmail.nossr50.api.ChatAPI;
 import com.gmail.nossr50.api.PartyAPI;
 import com.gmail.nossr50.datatypes.PlayerProfile;
 import com.gmail.nossr50.locale.LocaleLoader;
 import com.gmail.nossr50.mcMMO;
 import com.gmail.nossr50.party.Party;
 import com.gmail.nossr50.party.PartyManager;
 import com.gmail.nossr50.util.Users;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author dualspiral
  */
 public class PartyAdminCommand implements CommandExecutor {
 
     private mcMMO mcmmo;
     private PartyAdmin plugin;
     
     public PartyAdminCommand(PartyAdmin plugin) {
         this.plugin = plugin;
         this.mcmmo = plugin.mcmmo;
     }
     
     
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         Player player = null;
         if (sender instanceof Player) {
             player = (Player) sender;
         }
 
         if (sender.hasPermission("mcmmopartyadmin.commands.partyadmin") || sender.isOp() || player == null) {
 
             if (args.length > 2 && (args[0].equalsIgnoreCase("pc") || args[0].equalsIgnoreCase("chat"))) {
                 partyChat(sender, player, command, label, args);
                 return true;
             }
 
             switch (args.length) {
                 case 1:
                     if (args[0].equalsIgnoreCase("list")) {
                         List<Party> parties = PartyAPI.getParties();
                         if (parties.isEmpty()) {
                             player.sendMessage(ChatColor.DARK_AQUA + "There are no parties.");
                         } else {
                             player.sendMessage(ChatColor.DARK_AQUA + "Current Parties");
                             player.sendMessage(ChatColor.DARK_AQUA + "===============");
                             for (Party a : parties) {
                                 Server server = PartyAdmin.plugin.getServer();
                                 String leader = PartyAPI.getPartyLeader(a.getName());
                                 StringBuffer tempList = new StringBuffer();
 
                                 for (String otherPlayerName : a.getMembers()) {
                                     if (leader.equals(otherPlayerName)) {
                                         tempList.append(ChatColor.GOLD);
                                     } else if (server.getPlayer(otherPlayerName) != null) {
                                         tempList.append(ChatColor.WHITE);
                                     } else {
                                         tempList.append(ChatColor.GRAY);
                                     }
 
                                     tempList.append(otherPlayerName + " ");
                                 }
                                 player.sendMessage(ChatColor.DARK_AQUA + a.getName() + ": " + tempList);
 
                             }
                         }
                         return true;
                     }
 
 
                 case 2:
                     if (args[0].equalsIgnoreCase("removeparty") || args[0].equalsIgnoreCase("remparty") || args[0].equalsIgnoreCase("delparty") || args[0].equalsIgnoreCase("rp")) {
                         
                         // Get the party
                         Party target = PartyManager.getInstance().getParty(args[1]);
                         
                         if (target != null) {
                             String oldparty = args[1];
                             List<Player> players = Collections.unmodifiableList(PartyAPI.getOnlineMembers(target.getName()));
 
                             // Remove online players
                             if (!players.isEmpty() && players != null) {
                                 for (Player a : players) {
                                     a.sendMessage(ChatColor.RED + "You have left your party, as it has been deleted by an admin.");
                                     PartyAPI.removeFromParty(a);
                                 }
                             }
                             
                             // Remove offline players
                             
                             //boolean empty = false;
                                 List<String> members = target.getMembers();
                                 if (!members.isEmpty()) {
                                     List<String> mem = new ArrayList<String>();
                                     mem.addAll(members);
                                     for (int i = 0; i < mem.size(); i++) {
                                         PartyManager.getInstance().removeFromParty(mem.get(i), target);
                                     }
                                 }
                                 target = PartyManager.getInstance().getParty(args[1]);
                               //  empty = (target == null);
                             
                             sender.sendMessage(ChatColor.DARK_AQUA + "The party " + oldparty + " has been deleted!");
                         } else {
                             sender.sendMessage(ChatColor.DARK_AQUA + "The party " + args[1] + " does not exist!");
                         }
                         return true;
                         
                     } else if (args[0].equalsIgnoreCase("removeplayer") || args[0].equalsIgnoreCase("rpl") || args[0].equalsIgnoreCase("kickplayer")) {
                         String playername;
                         Player targetPlayer = plugin.getServer().getPlayer(args[1]);
                         String partyname;
                         if (targetPlayer != null) {
                             //Get the name!
                             partyname = PartyAPI.getPartyName(player);
                             if (partyname != null || partyname != "") {   
                                 PartyAPI.removeFromParty(player);
                                 targetPlayer.sendMessage(ChatColor.DARK_AQUA + "An admin has kicked you from the party");
                                 sender.sendMessage(ChatColor.DARK_AQUA + "The player " + args[1] + " is no longer in a party");
                             } else {
                                 sender.sendMessage(ChatColor.DARK_AQUA + "The player " + args[1] + " is not in a party");
                             }
                         } else {
                             //Check to see if there is an offline player
                             OfflinePlayer targetOfflinePlayer = plugin.getServer().getOfflinePlayer(args[1]);
                             if (targetOfflinePlayer == null) {
                                sender.sendMessage(ChatColor.DARK_AQUA + "The player " + args[1] + " cannot be found!");
                                 return true;
                             }
                             
                                 playername = targetOfflinePlayer.getName();
                                 Party party = PartyManager.getInstance().getPlayerParty(playername);
                                 if (party == null) {
                                     sender.sendMessage(ChatColor.DARK_AQUA + "The player " + args[1] + " is not in a party");
                                     return true;
                                 } else {
                                     PartyManager.getInstance().removeFromParty(playername, party);
                                     sender.sendMessage(ChatColor.DARK_AQUA + "The player " + args[1] + " is no longer in a party");                                    
                                 }
                         }
                         return true;
                     } else {
                         listCommands(sender);
                         return true;
                     }
 
                 case 3:
                     if (args[0].equalsIgnoreCase("addplayer") || args[0].equalsIgnoreCase("apl")) {
                         String playername;
                         Player targetPlayer = plugin.getServer().getPlayer(args[1]);
                         PlayerProfile profile = null;
                         if (targetPlayer == null) {
                             //Check to see if there is an offline player
                             OfflinePlayer targetOfflinePlayer = plugin.getServer().getOfflinePlayer(args[1]);
                             playername = targetOfflinePlayer.getName();
                             profile = Users.getProfile(playername);
 
                             if (playername == null) {
                                 sender.sendMessage(ChatColor.DARK_AQUA + "The player " + args[1] + " cannot be found!");
                                 return true;
                             } else if (profile == null) {
                                 sender.sendMessage(ChatColor.DARK_AQUA + "The player " + args[1] + " cannot be added to a party at this time!");
                                 return true;
                             }
                         }
                         
                         //Get the name!
                             playername = targetPlayer.getName();
                             profile = Users.getProfile(targetPlayer);
                             if (PartyManager.getInstance().isParty(args[2])) {
                                 PartyAPI.addToParty(player, args[2]);
                                 sender.sendMessage(ChatColor.DARK_AQUA + "Player " + ChatColor.WHITE + playername + ChatColor.DARK_AQUA + " has been added to the party " + ChatColor.WHITE + args[2]);
                             }
                             else {
                                 sender.sendMessage(ChatColor.DARK_AQUA + "That party cannot be found.");
                             }
                             return true;
                     } else if (args[0].equalsIgnoreCase("changeowner") || args[0].equalsIgnoreCase("chown")) {
                         String playername;
                         Player targetPlayer = plugin.getServer().getPlayer(args[1]);
                         if (targetPlayer != null) {
                             //Get the name!
                             playername = targetPlayer.getName();
                         } else {
                             //Check to see if there is an offline player
                             OfflinePlayer targetOfflinePlayer = plugin.getServer().getOfflinePlayer(args[1]);
                             playername = targetOfflinePlayer.getName();
                         }
 
                         if (playername == null) {
                             sender.sendMessage(ChatColor.DARK_AQUA + "The player " + args[1] + " cannot be found!");
                             return true;
                         }
 
                         Party party = PartyManager.getInstance().getParty(args[2]);
                         if (party != null) {
                             List<String> members = party.getMembers();
                             if (members.contains(playername)) {
                                 sender.sendMessage(ChatColor.DARK_AQUA + "Player " + ChatColor.WHITE + playername + ChatColor.DARK_AQUA + " is now the owner of " + ChatColor.WHITE + args[2]);
                                 PartyAPI.setPartyLeader(party.getName(), playername);
                                 if (targetPlayer != null) {
                                     targetPlayer.sendMessage(ChatColor.DARK_AQUA + "You are now the owner of " + ChatColor.WHITE + args[2]);
                                 }
                             } else {
                                 sender.sendMessage(ChatColor.DARK_AQUA + "Player " + ChatColor.WHITE + playername + ChatColor.DARK_AQUA + " is not a member of the party " + ChatColor.WHITE + args[2]);
                             }
                         } else {
                             sender.sendMessage(ChatColor.DARK_AQUA + "That party cannot be found.");
                         }
                         return true;
                     } else if ((args[0].equalsIgnoreCase("pc") || args[0].equalsIgnoreCase("chat"))) {
                             partyChat(sender, player, command, label, args);
                     } else {
                         listCommands(sender);
                         return true;
                     }
                 default:
                     listCommands(sender);
                     return true;
             }
         } else {
             player.sendMessage(ChatColor.RED + "You do not have permission to do this");
 
         }
 
         return true;
     }
     
     
     
     private void partyChat(CommandSender sender, Player player, Command command, String label, String[] args) {
         if (!PartyManager.getInstance().isParty(args[1])) {
             sender.sendMessage(LocaleLoader.getString("Party.InvalidName"));
             return;
         }
 
         StringBuilder buffer = new StringBuilder();
         buffer.append(args[2]);
 
         for (int i = 3; i < args.length; i++) {
             buffer.append(" ");
             buffer.append(args[i]);
         }
 
         String message = buffer.toString();
         
         ChatAPI.sendPartyChat(sender.getName(), args[1], message);
     }
 
     private void listCommands(CommandSender player) {
         player.sendMessage(ChatColor.DARK_AQUA + "mcMMO Party Admin");
         player.sendMessage(ChatColor.DARK_AQUA + "=================");
         player.sendMessage(ChatColor.YELLOW + "/partyadmin list " + ChatColor.WHITE + "- List current parties");
         player.sendMessage(ChatColor.YELLOW + "/partyadmin rp <party> " + ChatColor.WHITE + "- Delete party");
         player.sendMessage(ChatColor.YELLOW + "/partyadmin apl <player> <party> " + ChatColor.WHITE + "- Add player to party");
        player.sendMessage(ChatColor.YELLOW + "/partyadmin rpl <player> " + ChatColor.WHITE + "- Remove player from party");
         player.sendMessage(ChatColor.YELLOW + "/partyadmin chown <player> <party> " + ChatColor.WHITE + "- Change ownership of party to player");
         player.sendMessage(ChatColor.YELLOW + "/partyadmin pc <party> " + ChatColor.WHITE + "- Chat to party without joining it");
     }
 }
