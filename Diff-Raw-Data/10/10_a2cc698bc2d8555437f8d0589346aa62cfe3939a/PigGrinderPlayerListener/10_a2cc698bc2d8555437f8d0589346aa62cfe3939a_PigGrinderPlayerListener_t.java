 package net.zhuoweizhang.piggrinder;
 
 import org.bukkit.entity.Cow;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class PigGrinderPlayerListener extends PlayerListener {
 	public PigGrinderPlugin plugin;
 
 	public PigGrinderPlayerListener(PigGrinderPlugin plugin) {
 		this.plugin = plugin;
 	}
 
 	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
 		if (event.isCancelled() || !(event.getRightClicked() instanceof Pig || event.getRightClicked() instanceof Cow ||
 			event.getRightClicked() instanceof Sheep)) {
 			return;
 		}
 		ItemStack tool = event.getPlayer().getItemInHand();
 		if (tool == null || !tool.getType().equals(plugin.grinderMaterial) || tool.getDurability() != plugin.grinderMetadata) {
 			return;
 		}
 
		if (!plugin.checkCanUseGrinder(event.getPlayer(), (LivingEntity) event.getRightClicked())) {
			event.setCancelled(true);
 			return;
 		}
 
 		LivingEntity pig = (LivingEntity) event.getRightClicked();
 		for (PigGrinderPlugin.PigGrinderTask task: plugin.tasks) {
 			if (task.pig.equals(pig)) {
 				return;
 			}
 		}
 
 		event.setCancelled(true);
 		event.getPlayer().setItemInHand(new ItemStack(plugin.grinderMaterial, tool.getAmount() - 1, plugin.grinderMetadata));
 		plugin.grind(pig);
 	}
 
 }
