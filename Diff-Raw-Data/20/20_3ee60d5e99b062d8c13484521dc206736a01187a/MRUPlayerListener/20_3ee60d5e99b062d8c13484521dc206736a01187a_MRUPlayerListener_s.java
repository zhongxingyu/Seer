 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.stutiguias.listeners;
 
 import java.util.logging.Level;
 import me.stutiguias.mcmmorankup.Mcmmorankup;
 import me.stutiguias.profile.Profile;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 /**
  *
  * @author Daniel
  */
 public class MRUPlayerListener implements Listener {
     
     private final Mcmmorankup plugin;
 
     public MRUPlayerListener(Mcmmorankup plugin){
         this.plugin = plugin;
     }
     
     @EventHandler(priority= EventPriority.NORMAL)
     public void onPlayerJoin(PlayerJoinEvent event) {
         Player pl = event.getPlayer();
         Profile _profile = new Profile(plugin, pl);
         if(plugin.PromoteOnJoin) {
             String skill = _profile.getHabilityForRank().toUpperCase();
             String gender = _profile.getGender();
             Boolean sucess;
             if(plugin.TagSystem) {
                 sucess = plugin.RankUp.tryRankUpWithoutGroup(pl, skill, gender);
             }else{
                 sucess =  plugin.RankUp.tryRankUp(pl,skill,gender);
             }
             if(sucess) event.getPlayer().sendMessage(plugin.MSucess);
         }
     }
     
     @EventHandler(priority= EventPriority.NORMAL)
     public void onPlayerChat(AsyncPlayerChatEvent event) {
         if(event.isCancelled()) return;
         if(!plugin.TagSystem) return;
         Player _player = event.getPlayer();
         Profile _profile = new Profile(plugin, _player);
         String Tag = _profile.getTag();
        if(Tag == null) Tag = "Default";
         String format = event.getFormat();
         event.setFormat(plugin.parseColor(Tag) + " " + format);
         
     }
 }
