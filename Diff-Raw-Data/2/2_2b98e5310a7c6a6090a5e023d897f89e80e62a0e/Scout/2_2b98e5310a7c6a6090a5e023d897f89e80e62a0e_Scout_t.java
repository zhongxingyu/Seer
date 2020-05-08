 package org.github.craftfortress2;
 import org.bukkit.Material;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.player.PlayerToggleSneakEvent;
 import org.bukkit.potion.*;
 import org.bukkit.inventory.*;
public class Scout {
 
 	public static void init(Player player){
 		PlayerInventory inv = player.getInventory();
 		inv.clear();
 		player.setFoodLevel(17);
 		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999999, 1));
 		inv.setHelmet(new ItemStack(Material.IRON_HELMET, 1));
 		inv.setBoots(new ItemStack(Material.IRON_BOOTS, 1));
 		inv.setItem(2, new ItemStack(Material.STICK, 1));
 	}
 	@EventHandler
     	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) { // This probably won't work, but it's worth a shot.
         if (!event.isSneaking()) {
             return;
         }
         Player player = event.getPlayer();
         if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR && CFCommandExecutor.isPlaying(player) && CFCommandExecutor.getClass(player).equals("scout")){
         	player.setVelocity(player.getVelocity().setY(1));
         }else{
         	event.setCancelled(true);
         }
 	}
 }
