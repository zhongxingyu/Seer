 package civchat.listener;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import civchat.manager.PlayerManager;
 import civchat.model.CivPlayer;
 import civchat.model.Mode;
 import civchat.utility.Utility;
 
 public class PlayerListener implements Listener
 {
 	@EventHandler
 	public void login(PlayerLoginEvent event)
 	{
 		Player player = event.getPlayer();
 		PlayerManager playerManager = PlayerManager.getInstance();
		playerManager.getOrCreateCivPlayer(player);
 	}
 	
 	@EventHandler
 	public void quit(PlayerQuitEvent event)
 	{
 		Player player = event.getPlayer();
 		PlayerManager playerManager = PlayerManager.getInstance();
 		playerManager.removeCivPlayer(player);
 	}
 	
 	@EventHandler
 	public void interact(PlayerInteractEvent event)
 	{
 		if(!event.hasBlock()){return;}
 
 		Block block = event.getClickedBlock();
		
 		Player player = event.getPlayer();		
 		PlayerManager playerManager = PlayerManager.getInstance();
 		CivPlayer civPlayer = playerManager.getCivPlayer(player);
 		
 		if(civPlayer != null)
 		{
 			Mode mode = civPlayer.getMode();
 			switch(mode)
 			{
 				case NORMAL:
 					return;
 				case CREATE_ANTENNA:
					Boolean canBeAntenna = Utility.canBeAntenna(block);
 					if(canBeAntenna)
 					{
 						System.out.println("antenna created");
 					}
 					else
 					{
 						System.out.println("not possible to create an antenna here");
 					}
 					civPlayer.reset();
 			}
 		}
 	}
 }
