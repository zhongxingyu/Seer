 package uk.co.drnaylor.mcmmopartyadmin;
 
 import com.gmail.nossr50.api.PartyAPI;
 import com.gmail.nossr50.events.party.McMMOPartyChangeEvent;
 import java.util.List;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 
 /**
  *
  * @author dualspiral
  */
 public class PartyChangeListener implements Listener {
     
     @EventHandler
     public void PartyChange(McMMOPartyChangeEvent event) {
         if (event.getNewParty() != null) {
            List<Player> players = PartyAPI.getOnlineMembers(event.getNewParty());
            for (Player a : players) {
                if (a == event.getPlayer()) continue;
                a.sendMessage(ChatColor.YELLOW + event.getPlayer().getName() + ChatColor.GREEN + " has joined your party.");
             }
         }
         
         if (event.getOldParty() != null) {
            List<Player> players = PartyAPI.getOnlineMembers(event.getOldParty());
            for (Player a : players) {
                if (a == event.getPlayer()) continue;
                a.sendMessage(ChatColor.YELLOW + event.getPlayer().getName() + ChatColor.GREEN + " has left your party.");
             }
         }
         
         String oldp = event.getOldParty();
         String newp = event.getNewParty();
         
         if (oldp == null) {
             oldp = "No party";
         }
         
         if (newp == null) {
             newp = "No party";
         }
         
         for (Player online : PartyAdmin.plugin.getServer().getOnlinePlayers()) {
           if (online.hasPermission("mcmmopartyadmin.spy") || online.isOp())
           {
                 String p2 = ChatColor.GRAY + "[Party Change] " + ChatColor.YELLOW + event.getPlayer().getName() + ChatColor.GREEN + ": " + oldp + " => " + newp;
                 online.sendMessage(p2);
           }
         }
         
     }
     
 }
