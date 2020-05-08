 
 package me.stutiguias.listeners;
 
 import java.sql.Timestamp;
 import java.util.logging.Level;
 import me.stutiguias.mcpk.MCPlayer;
 import me.stutiguias.mcpk.Mcpk;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 /**
  *
  * @author Stutiguias
  */
 public class McpkPlayerListener implements Listener {
     
     private final Mcpk plugin;
 
     public McpkPlayerListener(Mcpk plugin){
         this.plugin = plugin;
  
     }
  
     @EventHandler(priority= EventPriority.NORMAL)
     public void onPlayerQuit(PlayerQuitEvent event) {
         plugin.MCPlayers.remove(event.getPlayer().getName());
     }
     
     @EventHandler(priority= EventPriority.NORMAL)
     public void onPlayerJoin(PlayerJoinEvent event) {
         Player pl = event.getPlayer();
         MCPlayer _MCPlayer = null;
         try {
             _MCPlayer = plugin.DB.getPlayer(pl);
         }catch(Exception e){
             Mcpk.logger.log(Level.WARNING, "[MCPK] Error get Player from Database: {0}", e.getMessage());
         }
         if(_MCPlayer == null) {
             _MCPlayer = new MCPlayer(pl.getName(),plugin.util.now());
             
             if(!plugin.usenewbieprotect) {
                 plugin.DB.CreatePlayer(pl,new Timestamp(plugin.util.now().getTime()));
                 _MCPlayer.setProtectAlreadyLeft(Boolean.TRUE);
                 Mcpk.logger.log(Level.INFO, "[MCPK] New Player {0}", pl.getName());
             }else{
                 Timestamp ProtectUntil = ProtectUntil();
                 
                 plugin.DB.CreatePlayer(pl, ProtectUntil);
                 
                 _MCPlayer.setNewBieProtectUntil(ProtectUntil);
                 _MCPlayer.setProtectAlreadyLeft(Boolean.FALSE);
                 SendProtectMessage(pl,ProtectUntil.toString());
                 Mcpk.logger.log(Level.INFO, "[MCPK] New Player {0} and is protected until {1}", new Object[]{pl.getName(),ProtectUntil.toString()});
             }
             
             _MCPlayer.setAlertMsg(Boolean.TRUE);
             plugin.DB.SetAlertMsg(pl, Boolean.TRUE);
             _MCPlayer.setPKMsg(Boolean.TRUE);
             plugin.DB.SetPKMsg(pl, Boolean.TRUE);
             
             plugin.MCPlayers.put(pl.getName(),_MCPlayer);
             
         }else {
             
             _MCPlayer.setAlertMsg(plugin.DB.getAlertMsg(pl));
             _MCPlayer.setPKMsg(plugin.DB.getPKMsg(pl));
            
            if(_MCPlayer.getKills() > 0) 
                _MCPlayer.setIsPK(Boolean.TRUE);
            else
                _MCPlayer.setIsPK(Boolean.FALSE);
            
             _MCPlayer.setKillPk(Boolean.FALSE);
             
             if(plugin.util.now().before(_MCPlayer.getNewBieProtectUntil())) {
                 _MCPlayer.setProtectAlreadyLeft(Boolean.FALSE);
                 SendProtectMessage(pl, _MCPlayer.getNewBieProtectUntil().toString() );
             }else{
                 _MCPlayer.setProtectAlreadyLeft(Boolean.TRUE);
             }
             plugin.MCPlayers.put(pl.getName(), _MCPlayer);
         }
         
     }
     
     public Timestamp ProtectUntil() {
         return new Timestamp(plugin.util.addTime(plugin.NewbieProtectTime).getTime());
     }
     
     private void SendProtectMessage(Player pl,String Date) {
         String Time = String.valueOf(plugin.NewbieProtectTime).replace("m","");
         pl.sendMessage(plugin.translate.ProtectMsg.replace("%d%", Time).replace("%date%",Date));
     }
 }
