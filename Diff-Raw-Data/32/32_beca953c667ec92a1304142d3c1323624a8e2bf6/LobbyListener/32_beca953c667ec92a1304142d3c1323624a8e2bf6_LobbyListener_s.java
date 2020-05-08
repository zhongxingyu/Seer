 package com.CC.Lobby;
 
 import java.util.HashMap;
 
 import org.bukkit.DyeColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import com.CC.Arenas.Team;
 import com.CC.General.onStartup;
 
 
 public class LobbyListener implements Listener
 {
 	
 	public static HashMap<Player, Team> quedplayers = new HashMap<Player, Team>();
 	
 	private onStartup plugin;
     
     public LobbyListener(onStartup instance) {
     	plugin = instance;
     	//System.out.println("LobbyListener created.");
 	}
 	
 	@EventHandler
 	public void onQueue(PlayerMoveEvent event)
     {
 		//System.out.println("onQueue called.");
 		Player player = event.getPlayer();
 		if(player.getLocation().getWorld().getName().equalsIgnoreCase("lobby"))
         {
 			//player.sendMessage("Step 1");
 			if(quedplayers.containsKey(event.getPlayer()))
             {
 				//player.sendMessage("Step 2");
 				if(!onLobby(player))
                 {
 					//player.sendMessage("Step 3");
 						quedplayers.remove(player);
						player.sendMessage("You have left the lobby, thus, unqueed.");
                 }
             }
             else
             {
             	//player.sendMessage("Step 1");
                 if(onLobby(player))
                 {
                 	//player.sendMessage("Step 2");
                 	Block block = event.getPlayer().getWorld().getBlockAt(event.getPlayer().getLocation().getBlockX(), event.getPlayer().getLocation().getBlockY() - 1, event.getPlayer().getLocation().getBlockZ());
                     if(block.getType() == Material.WOOL)
                     {
                     	//player.sendMessage("Step 3");
                         byte blue = DyeColor.BLUE.getData();
                         if( block.getData() == blue)
                         {
                         	//player.sendMessage("Step 4");
                            player.sendMessage("You have been added to the blue team waiting list");
                             quedplayers.put(player, Team.BLUE);
                         }
                         else
                         {
                         	//player.sendMessage("Step 4");
                            player.sendMessage("You have been added to the red team waiting list");
                             quedplayers.put(player, Team.RED);
                         }    
                     }
                 }
             }
         }
     }
 	
 	
 	public boolean onLobby(Player player)
     {
 		Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ());
 		if(block.getType() == Material.WOOL)
         {
             byte red = DyeColor.RED.getData();
             byte blue = DyeColor.BLUE.getData();
             if(block.getData() == red || block.getData() == blue)
             {
                 return true;
             }    
         }
         return false;
     }
     
     @EventHandler(priority=EventPriority.LOW)
     public void onQuit(PlayerQuitEvent event)
     {
         Player player = event.getPlayer();
         if(quedplayers.containsKey(player))
         {
             quedplayers.remove(player);
         }
     }
}
 	
 
 
