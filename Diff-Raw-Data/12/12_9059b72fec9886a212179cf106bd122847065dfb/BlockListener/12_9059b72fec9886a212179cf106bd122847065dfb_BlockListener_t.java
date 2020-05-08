 package net.catharos.recipes.listener;
 
 import net.catharos.recipes.cRecipes;
 import net.catharos.recipes.crafting.CustomRecipe;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class BlockListener implements Listener {
 	protected cRecipes plugin;
 
 	public BlockListener( cRecipes plugin ) {
 		this.plugin = plugin;
 		Bukkit.getServer().getPluginManager().registerEvents( this, plugin );
 	}
 
	@EventHandler( priority = EventPriority.HIGHEST )
 	public void e( BlockBreakEvent event ) {
 		if (event.isCancelled() || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
 
 		Block block = event.getBlock();
 		CustomRecipe cr = plugin.getRecipe( block.getTypeId(), block.getData() );
 
 		if (cr != null) {
 			for (ItemStack drop : cr.getDrops()) {
 				block.getWorld().dropItemNaturally( block.getLocation(), drop );
 			}
 
 			block.setType( Material.AIR );
 		}
 	}
 
 }
