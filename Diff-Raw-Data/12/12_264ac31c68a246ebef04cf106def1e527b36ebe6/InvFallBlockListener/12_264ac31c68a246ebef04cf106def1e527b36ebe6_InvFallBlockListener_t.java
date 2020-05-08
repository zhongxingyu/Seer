 package spia1001.InvFall;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 /*
 InvFall Plugin
 
 @author Chris Lloyd (SPIA1001)
 */
 
 public class InvFallBlockListener extends BlockListener
 {
 	private PlayerManager playerManager;
 	private InvFall plugin;
 	public InvFallBlockListener(PlayerManager pm,InvFall p)
 	{
 		playerManager = pm;
 		plugin = p;
 	}
 	public void onBlockPlace(BlockPlaceEvent event)
 	{
 		Player player = event.getPlayer();
		if(player.getItemInHand().getType().isBlock())
			if(playerManager.playerIsEnabled(player)&& plugin.permissionWrapper.hasPermission(player,PermissionWrapper.NODE_BLOCKFALL))
				 new ItemFall(player,playerManager.freeFallEnabled(player),1);
 	}
 }
