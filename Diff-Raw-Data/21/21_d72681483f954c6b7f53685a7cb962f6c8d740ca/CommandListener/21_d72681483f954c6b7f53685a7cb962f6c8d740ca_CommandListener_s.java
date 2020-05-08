 package org.wargamer2010.wildcardcommand;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.server.ServerCommandEvent;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.Bukkit;
 import java.util.List;
 import java.util.regex.*;
 import org.wargamer2010.wildcardcommand.util.Vault;
 import org.wargamer2010.wildcardcommand.wildcards.Wildcard;
 import org.wargamer2010.wildcardcommand.wildcards.Wildcardplayer;
 
 public class CommandListener implements Listener {
     
     @EventHandler(priority = EventPriority.HIGH)
     public void onServerCommand(ServerCommandEvent event) {
        Wildcard wc = parseCommand(event.getCommand());
         event.setCommand((wc != null) ? "" : event.getCommand());        
     }
     
     public Boolean hasPerm(Player player, String perm) {
         if(!Vault.vaultFound || Vault.permission == null)
             return false;
         return Vault.permission.playerHas(player.getWorld(), player.getName(), perm);        
     }
     
     @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {        
        if(event.getPlayer() == null || (!event.getPlayer().isOp() && !hasPerm(event.getPlayer(), "wc.use"))) {
             Wildcardplayer.sendMessage(event.getPlayer(), "You do not have permission to use WildcardCommands.");
             return;
         }
        Wildcard wc = parseCommand(event.getMessage().substring(1));
         if(wc != null) {
             event.setCancelled(true);
             Wildcardplayer.sendMessage(event.getPlayer(), ("Sent command to " + wc.getWildcardName()));            
         }
     }
     
     private Wildcard getWildcardFromCommand(String sCommand) {
         if(sCommand.length() < 3)
             return null;        
         if(sCommand.contains("[") && sCommand.contains("]"))
             return Wildcard.getWildcardInstance(sCommand.substring(1, sCommand.length()-1));
         return null;
     }
     
    private Wildcard parseCommand(String sCommand) {        
         Matcher m = Pattern.compile("\\[([a-zA-Z0-9]+\\:)*[^\\]]+\\]").matcher(sCommand);        
         String sWildcardFound = "";
         Wildcard sLastWildcard = null;
 
         while(m.find() && sLastWildcard == null) {
             sLastWildcard = getWildcardFromCommand(m.group().trim());
             if(sLastWildcard != null)
                 sWildcardFound = m.group();
         }
 
         if(sLastWildcard == null)
             return null;
             
         CommandSender console = Bukkit.getServer().getConsoleSender();
 
         List<Wildcardplayer> players = sLastWildcard.getPlayers();
         if(players.isEmpty())
             return sLastWildcard;
 
        for(Wildcardplayer player : players)            
            Bukkit.getServer().dispatchCommand(console, sCommand.replace(sWildcardFound, player.getName()));            
         
         return sLastWildcard;
     }
     
 }
