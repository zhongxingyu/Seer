 package net.milkycraft;
 
import static net.milkycraft.objects.Option.*;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.milkycraft.config.WorldConfiguration;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.entity.EnderPearl;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockDispenseEvent;
 import org.bukkit.event.enchantment.EnchantItemEvent;
 import org.bukkit.event.entity.EntityCreatePortalEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.entity.PotionSplashEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.inventory.InventoryType.SlotType;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.weather.LightningStrikeEvent;
 import org.bukkit.event.weather.ThunderChangeEvent;
 import org.bukkit.event.weather.WeatherChangeEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.Potion;
 
 public class PrimaryListener extends Utility implements Listener {
 
 	private Map<String, List<ItemStack>> drops = new HashMap<String, List<ItemStack>>();
 
 	public PrimaryListener(EntityManager manager) {
 		super(manager);
 		super.register(this);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPortalCreation(EntityCreatePortalEvent e) {
 		if (e.getEntity() instanceof Player) {
 			Player pl = ((Player) e.getEntity());
 			WorldConfiguration conf = get(pl.getWorld().getName());
 			if (conf.get(PORTAL_CREATE) && !b(pl, p[6])) {
 				e.setCancelled(true);
 				al(conf, "Player " + pl.getName() + " tried to create portals");
 				al(conf, pl, "&cYou don't have permission to create a portal.");
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPvp(EntityDamageByEntityEvent e) {
 		if (e.getEntity() instanceof Player) {
 			Player attacked = ((Player) e.getEntity());
 			WorldConfiguration conf = get(attacked.getWorld().getName());
 			if (e.getDamager() instanceof Player) {
 				Player ag = ((Player) e.getDamager());
 				e.setCancelled(a(conf, ag, attacked.getName()));
 			} else if (e.getDamager() instanceof Projectile
 					&& !(e.getDamager() instanceof EnderPearl)) {
 				Entity a = ((Projectile) e.getDamager()).getShooter();
 				if (a instanceof Player) {
 					Player p = (Player) a;
 					e.setCancelled(a(conf, p, attacked.getName()));
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onArrowShoot(EntityShootBowEvent e) {
 		if (e.getEntity() instanceof Player) {
 			Player pl = ((Player) e.getEntity());
 			WorldConfiguration conf = get(pl.getWorld().getName());
 			if (conf.get(SHOOTING) && !b(pl, p[1])) {
 				e.setCancelled(true);
 				al(conf, "Player " + pl.getName() + " tried to shoot a bow.");
 				al(conf, pl, "&cYou don't have permission to shoot bows.");
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onEnchant(EnchantItemEvent e) {
 		Player pl = e.getEnchanter();
 		WorldConfiguration conf = get(pl.getWorld().getName());
 		if (conf.get(ENCHANTING) && !b(pl, p[2])) {
 			e.setCancelled(true);
 			al(conf, "Player " + pl.getName() + " tried to enchant a "
 					+ e.getItem().getType().toString().toLowerCase());
 			al(conf, pl, "&cYou don't have permission to enchant.");
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onAnvilEnchant(InventoryClickEvent e) {
 		if (e.getInventory().getType() == InventoryType.ANVIL) {
 			if (e.getSlotType() == SlotType.RESULT) {
 				if (e.getWhoClicked() instanceof Player) {
 					Player pl = (Player) e.getWhoClicked();
 					WorldConfiguration conf = a(pl.getWorld());
 					if (conf.get(ENCHANTING) && !b(pl, p[3])) {
 						e.setCancelled(true);
 						al(conf, "Player " + pl.getName() + " tried to enchant a "
 								+ e.getCurrentItem().getType().toString().toLowerCase()
 								+ " in an anvil.");
 						al(conf, pl, "&cYou don't have permission to use anvils.");
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onLightning(LightningStrikeEvent e) {
 		e.setCancelled(a(e.getWorld()).get(LIGHTNING));
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onThunderChange(ThunderChangeEvent e) {
 		boolean b = e.toThunderState();
 		e.setCancelled(b ? a(e.getWorld()).get(THUNDER) : false);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onWeatherChange(WeatherChangeEvent e) {
 		boolean b = e.toWeatherState();
 		e.setCancelled(b ? a(e.getWorld()).get(RAIN) : false);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPotionSplash(PotionSplashEvent e) {
 		if (e.getEntity().getShooter() instanceof Player) {
 			Player p = (Player) e.getEntity().getShooter();
			Potion b = fromDamage(e.getPotion().getItem().getDurability());
 			if (b(p, "entitymanager.interact.potion." + b.getNameId())) {
 				return;
 			}
 			WorldConfiguration conf = a(p.getWorld());
 			if (conf.usagePotion(b.getNameId())) {
 				e.setCancelled(true);
 				al(conf, "Player " + p.getName() + " tried to use an " + c(b) + " potion"
 						+ ".");
 				al(conf, p, "&cYou don't have permission to use that &6" + c(b)
 						+ " potion" + "&c.");
 			}
 			return;
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPlayerDeath(PlayerDeathEvent e) {
 		Player pl = e.getEntity();
 		WorldConfiguration conf = a(pl.getWorld());
 		if (b(pl, p[5]) || conf.get(PDEATHITEMS)) {
 			drops.put(pl.getName(), e.getDrops());
 			e.getDrops().clear();
 		}
 		if (b(pl, p[4]) || conf.get(PDEATHEXP)) {
 			e.setKeepLevel(true);
 			e.setDroppedExp(0);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onRespawn(PlayerRespawnEvent e) {
 		final Player p = e.getPlayer();
 		if (drops.containsKey(p.getName())) {
 			Bukkit.getScheduler().runTaskLater(getHandle(), new Runnable() {
 				@Override
 				public void run() {
 					WorldConfiguration wc = a(p.getWorld());
 					al(wc, "Player " + p.getName() + " respawned with their items");
 					al(wc, p, "&6Your items were returned after death!");
 					for (ItemStack is : drops.get(p.getName())) {
 						if (is != null) {
 							p.getInventory().addItem(is);
 						}
 					}
 					drops.remove(p.getName());
 				}
 			}, 10L);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onDispense(BlockDispenseEvent e) {
 		WorldConfiguration conf = a(e.getBlock().getWorld());
 		if (e.getItem().getType() == Material.POTION) {
			Potion b = fromDamage(e.getItem().getDurability());
 			if (conf.dispensePotion(b.getNameId())) {
 				e.setCancelled(true);
 			}
 		} else {
 			if (conf.dispense(e.getItem().getTypeId())) {
 				e.setCancelled(true);
 			}
			return;
 		}
 	}
 }
