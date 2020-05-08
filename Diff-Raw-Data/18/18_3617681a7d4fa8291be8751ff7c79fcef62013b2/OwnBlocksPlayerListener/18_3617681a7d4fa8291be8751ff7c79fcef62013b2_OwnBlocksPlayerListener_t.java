 package me.cvenomz.OwnBlocks;
 
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class OwnBlocksPlayerListener extends PlayerListener{
 	
 	OwnBlocks pluginRef;
 	private Map<OBBlock, String> database;
 	
 	public OwnBlocksPlayerListener(OwnBlocks ob)
 	{
 		pluginRef = ob;
 		database = pluginRef.database;
 	}
 	
 	@Override
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		// TODO Auto-generated method stub
 		//super.onPlayerJoin(event);
 		
 		String player = event.getPlayer().getName();
 		pluginRef.addPlayer(player);
 	}
 	
 	@Override
 	public void onPlayerQuit(PlayerQuitEvent event)
 	{
 		String player = event.getPlayer().getName();
 		pluginRef.removePlayer(player);
 	}
 	
 	@Override
 	public void onPlayerInteract(PlayerInteractEvent e)
 	{
		pluginRef.debugMessage("PlayerInteractEvent");
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
 			return;
		if (e.getItem() != null && e.getItem().getTypeId() == pluginRef.getInfoID())
 			handleInfo(e);
 	}
 	
 	private void handleInfo(PlayerInteractEvent e)
 	{
 		OBBlock obb = new OBBlock(e.getClickedBlock());
 		if (database.containsKey(obb))
 		{
 			e.getPlayer().sendMessage(ChatColor.YELLOW + "Block owned by: " + database.get(obb));
 		}
 		else
 		{
 			e.getPlayer().sendMessage(ChatColor.YELLOW + "Block not owned");
 		}
 	}
 
 }
