 package net.endercraftbuild.cod.guns;
 
 import java.util.Map;
 
 import net.endercraftbuild.cod.CoDMain;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 
 public class Shoot implements Listener {
 	
 	private final CoDMain plugin;
 
 	public Shoot(CoDMain plugin) {
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Action action = event.getAction();
 		final Player player = event.getPlayer();
 		ItemStack hand = player.getItemInHand();
 		
 		if ((action == Action.RIGHT_CLICK_AIR) || (action == Action.RIGHT_CLICK_BLOCK)) {
 			if (hand.getType() == Material.IRON_HOE) { // AK-47
 				if (!consumeAmmo(player, 2)) {
 					player.sendMessage(plugin.prefix + ChatColor.RED + "Out of ammo! Buy some at an ammo sign!");
 					return;
 				}
 				
 				player.launchProjectile(Snowball.class);
 				Location loc = player.getEyeLocation().toVector().add(player.getLocation().getDirection().multiply(1)).toLocation(player.getWorld(), player.getLocation().getYaw(), player.getLocation().getPitch());
 				smokepase(player, loc);
 				player.playSound(player.getLocation(), Sound.CLICK, 160.0F, 0.0F);
 			
 			} else if (hand.getType() == Material.STONE_HOE) { // Shotgun 
 				if (this.plugin.reloaders.contains(player.getName()))
 					return;
 			
 				if (!consumeAmmo(player, 5)) {
 					player.sendMessage(plugin.prefix + ChatColor.RED + "Out of ammo! Buy some at an ammo sign!");
 					return;
 				}
 
 				this.plugin.reloaders.add(player.getName());
 				player.launchProjectile(Snowball.class);
 				player.launchProjectile(Snowball.class);
 				player.launchProjectile(Snowball.class);
 				player.launchProjectile(Snowball.class);
 				player.launchProjectile(Snowball.class);
 				player.launchProjectile(Snowball.class);
 				Location loc = player.getEyeLocation().toVector().add(player.getLocation().getDirection().multiply(1)).toLocation(player.getWorld(), player.getLocation().getYaw(), player.getLocation().getPitch());
 				smokepase(player, loc);
 				player.playSound(player.getLocation(), Sound.EXPLODE, 70.0F, 70.0F);
 				
 				final Shoot self = this;
 				Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
 					public void run() {
 						self.plugin.reloaders.remove(player.getName());
 					}
 				}, 40L);
 				
 			} else if (hand.getType() == Material.DIAMOND_HOE) { // RAY GUN
 				if (this.plugin.pistol.contains(player.getName()))
 					return;
 				
 				if (!consumeAmmo(player, 1)) {
 					player.sendMessage(plugin.prefix + ChatColor.RED + "Out of ammo! Buy some at an ammo sign!");
 					return;
 				}
 				
 				// Pistol fire rate
 				this.plugin.pistol.add(player.getName());
 				player.launchProjectile(Snowball.class);
 				player.launchProjectile(Snowball.class);
 				Location loc = player.getEyeLocation().toVector().add(player.getLocation().getDirection().multiply(1)).toLocation(player.getWorld(), player.getLocation().getYaw(), player.getLocation().getPitch());
 				smokepase(player, loc);
 				player.playSound(player.getLocation(), Sound.FIZZ, 160.0F, 0.0F);
 				
 				final Shoot self = this;
 				Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
 					public void run() {
 						self.plugin.pistol.remove(player.getName());
 					}
 				}, 12L);
 			
 			} else if (hand.getType() == Material.WOOD_HOE) { // Pistol
 		        if (this.plugin.pistol.contains(player.getName())) {
 		        	return;
 		        }
 		        
 				if (!consumeAmmo(player, 1)) {
 					player.sendMessage(plugin.prefix + ChatColor.RED + "Out of ammo! Buy some at an ammo sign!");
 					return;
 				}
 
 				this.plugin.pistol.add(player.getName());
 				player.launchProjectile(Snowball.class);
 				Location loc = player.getEyeLocation().toVector().add(player.getLocation().getDirection().multiply(1)).toLocation(player.getWorld(), player.getLocation().getYaw(), player.getLocation().getPitch());
 				smokepase(player, loc);
 				player.playSound(player.getLocation(), Sound.CLICK, 160.0F, 0.0F);
 				
 				final Shoot self = this;
 				Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
 					public void run() {
 						self.plugin.pistol.remove(player.getName());
 					}
 				}, 12L);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onBulletHit(EntityDamageByEntityEvent event) { // Bullet hit
 		Entity damager = event.getDamager();
 		
 		if (!(damager instanceof Snowball))
 			return;
 		
 		damager = ((Snowball) damager).getShooter();
 		((HumanEntity) damager).getItemInHand();
 		event.setDamage(12);
 	}
 	
 	@EventHandler
 	public void onRayGunHit(ProjectileHitEvent event) { // Make ray gun explode
 		if (event.getEntity().getType() != EntityType.SNOWBALL)
 			return;
 		
 		if (!(event.getEntity().getShooter() instanceof Player))
 			return;
 		
 		Player player = (Player) event.getEntity().getShooter();
 	
 		if (player.getItemInHand().getType() == Material.DIAMOND_HOE) 
			player.getWorld().createExplosion(event.getEntity().getLocation(), 1F, false);
 	}
 	
 	@EventHandler
 	public void shootArrow(ProjectileLaunchEvent event) {
 		if ((event.getEntity() instanceof Snowball)) {
 			double x = 3.0D;
 			Vector v = event.getEntity().getVelocity();
 			v.multiply(new Vector(x, x, x));
 			event.getEntity().setVelocity(v);
 		} else if ((event.getEntity() instanceof Arrow)) {
 			double x = 1.0D;
 			Vector v = event.getEntity().getVelocity();
 			v.multiply(new Vector(x, x, x));
 			event.getEntity().setVelocity(v);
 		}
 	}
 	
 	public void smokepase(Player player, Location loc) {
 		double rotation = (player.getLocation().getYaw() - 90.0F) % 360.0F;
 	    if (rotation < 0.0D)
 	      rotation += 360.0D;
 	    if ((0.0D <= rotation) && (rotation < 22.5D))
 	      player.getWorld().playEffect(loc, Effect.SMOKE, 3);
 	    else if ((22.5D <= rotation) && (rotation < 67.5D))
 	      player.getWorld().playEffect(loc, Effect.SMOKE, 9);
 	    else if ((67.5D <= rotation) && (rotation < 112.5D))
 	      player.getWorld().playEffect(loc, Effect.SMOKE, 1);
 	    else if ((112.5D <= rotation) && (rotation < 157.5D))
 	      player.getWorld().playEffect(loc, Effect.SMOKE, 2);
 	    else if ((157.5D <= rotation) && (rotation < 202.5D))
 	      player.getWorld().playEffect(loc, Effect.SMOKE, 5);
 	    else if ((202.5D <= rotation) && (rotation < 247.5D))
 	      player.getWorld().playEffect(loc, Effect.SMOKE, 8);
 	    else if ((247.5D <= rotation) && (rotation < 292.5D))
 	      player.getWorld().playEffect(loc, Effect.SMOKE, 7);
 	    else if ((292.5D <= rotation) && (rotation < 337.5D))
 	      player.getWorld().playEffect(loc, Effect.SMOKE, 6);
 	    else if ((337.5D <= rotation) && (rotation < 360.0D))
 	      player.getWorld().playEffect(loc, Effect.SMOKE, 3);
 	}
 	
 	@SuppressWarnings("deprecation")
 	public boolean consumeAmmo(Player player, int count) {
 		Map<Integer, ? extends ItemStack> ammo = player.getInventory().all(Material.CLAY_BALL);
 		
 		int found = 0;
 		for (ItemStack stack : ammo.values())
 			found += stack.getAmount();
 		if (count > found)
 			return false;
 		
 		for (Integer index : ammo.keySet()) {
 			ItemStack stack = ammo.get(index);
 			
 			int removed = Math.min(count, stack.getAmount());
 			count -= removed;
 			
 			if (stack.getAmount() == removed)
 				player.getInventory().setItem(index, null);
 			else
 				stack.setAmount(stack.getAmount() - removed);
 			
 			if (count <= 0)
 				break;
 		}
 		
 		player.updateInventory();
 		return true;
 	}
 	
 }
