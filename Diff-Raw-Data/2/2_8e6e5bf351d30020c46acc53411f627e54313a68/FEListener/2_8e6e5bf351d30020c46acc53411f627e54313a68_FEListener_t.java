 package me.sinnoh.FamousEssentials;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class FEListener implements Listener 
 {
 	
 	private FamousEssentials plugin;
 	public FEListener(FamousEssentials instance)
 	{
 		plugin = instance;
 	}
 	public Map<Player, Integer>selectedworld = new HashMap<Player, Integer>();
 	
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent event)
 	{
 		Player player = event.getEntity();
		if(plugin.getConfig().getStringList("DontDropItems.worlds").contains(player.getWorld().getName()))
 			if(!player.hasPermission("FamousEssentials.keepitems"))
 			{
 			List<ItemStack> drops = event.getDrops();
 				if(player.getKiller() instanceof Player)
 				{
 					Player killer = player.getKiller();
 	
 					for(ItemStack i : drops)
 					{
 						killer.getInventory().addItem(i);
 					}
 					drops.clear();
 					
 				}
 			}
 	}
 }
