 package net.croxis.plugins.research;
 
 import org.bukkit.GameMode;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 
import java.util.HashSet;

 public class RPlayerListener extends PlayerListener{
 	@Override
     public void onPlayerJoin(PlayerJoinEvent event) {
 		if(event.getPlayer().hasPermission("research")){
 			TechManager.initPlayer(event.getPlayer());
 			Player player = event.getPlayer();
 			Tech t = TechManager.getCurrentResearch(player);
 			if(t == null){
 				t = new Tech();
 				t.name = "None";
 			}
 			event.getPlayer().sendMessage("You currently know " + TechManager.getResearched(event.getPlayer()).size() + " technologies" +
         		" and are currently researching " + t.name + ".");
 		}
     }
 	
 	@Override
 	public void onPlayerInteract(PlayerInteractEvent event){
 		if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
 			return;
 		if(event.getPlayer().hasPermission("research") && event.hasItem()){
 			if(TechManager.players.get(event.getPlayer()).cantUse.contains(event.getItem().getTypeId()))
 				event.setCancelled(true);
 			
 		}
 	}
 }
