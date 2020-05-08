 package uk.co.drnaylor.mcmmopartyadmin.commands.subcommands;
 
 import com.gmail.nossr50.datatypes.party.Party;
 import com.gmail.nossr50.events.party.McMMOPartyChangeEvent;
 import com.gmail.nossr50.party.PartyManager;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import uk.co.drnaylor.mcmmopartyadmin.Util;
 import uk.co.drnaylor.mcmmopartyadmin.dualcommandexecutor.DualSubCommandInterface;
 import uk.co.drnaylor.mcmmopartyadmin.locales.L10n;
 
 public class RemovePartySubCommand implements DualSubCommandInterface {
 
     private List<String> permissions = new ArrayList<String>();
     
     public RemovePartySubCommand() {
         permissions.add("mcmmopartyadmin.admin");
     }
 
     public List<String> getSubCommands() {
        return Arrays.asList(new String[] {"removeplayer", "kickplayer", "rpl"});
     }
 
     public String getShortHelp() {
         return ChatColor.YELLOW + "/partyadmin rp <party> " + ChatColor.WHITE + "- " + L10n.getString("Description.Disband");
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
             disbandParty(sender, cmdargs[0]);
             return;
         }
         sender.sendMessage(ChatColor.RED + "Incorrect usage!");
         sender.sendMessage(getLongHelp());
     }
 
     public List<String> onSubCommandTabComplete(CommandSender sender, String[] args) {
         if (args.length == 1) {
             return Util.getPartyCollection();
         }
         return null;
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
     
 }
