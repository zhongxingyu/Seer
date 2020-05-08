 package me.Guga.Guga_SERVER_MOD.Listeners;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import me.Guga.Guga_SERVER_MOD.Book;
 import me.Guga.Guga_SERVER_MOD.GugaEvent;
 import me.Guga.Guga_SERVER_MOD.GugaProfession;
 import me.Guga.Guga_SERVER_MOD.Guga_SERVER_MOD;
 import me.Guga.Guga_SERVER_MOD.InventoryBackup;
 import me.Guga.Guga_SERVER_MOD.Handlers.GugaCommands;
 import net.minecraft.server.WorldServer;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Spider;
 import org.bukkit.entity.Wither;
 import org.bukkit.entity.Zombie;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class GugaEntityListener implements Listener
 {
 	public GugaEntityListener(Guga_SERVER_MOD gugaSM)
 	{
 		plugin = gugaSM;
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onCreatureSpawnEvent(CreatureSpawnEvent e)
 	{
 		if((e.getEntity() instanceof Wither))
 		{
 			if(!(e.getEntity().getWorld().getName().matches("world_nether")))
 			{
 				e.setCancelled(true);
 			}
 		}
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onEntityRegainHealth(EntityRegainHealthEvent e)
 	{
 		if (plugin.debug)
 		{
 			plugin.log.info("ENTITY_REGAIN_EVENT: entity=" + e.getEntity().toString());
 		}
 		if (e.getEntity() instanceof Player)
 		{
 			if (e.getRegainReason() == RegainReason.REGEN)
 			{
 				e.setCancelled(true);
 			}
 		}
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onEntityDamage(EntityDamageEvent e)
 	{
 		if (plugin.debug)
 		{
 			plugin.log.info("ENTITY_DAMAGE_EVENT: entity=" + e.getEntity().toString() + ",dmg=" + e.getDamage());
 		}
 		if(e.getEntity() instanceof Player)
 		{
 			if(((Player)e.getEntity()).getWorld().getName().matches("arena"))
 			{
 				if(e instanceof EntityDamageByEntityEvent)
 				{
 					if(plugin.arena.IsImortal(((Player)e.getEntity()).getName()))
 						e.setCancelled(true);
 				}
 				else
 					e.setCancelled(true);
 			}
 			if (GugaCommands.godMode.contains(((Player)e.getEntity()).getName().toLowerCase()))
 			{
 				e.setCancelled(true);
 				return;
 			}
 			if (GugaEvent.godMode)
 			{
 				if (GugaEvent.players.contains(((Player)e.getEntity()).getName().toLowerCase()))
 				{
 					e.setCancelled(true);
 					return;
 				}
 			}
 		}
 		if (e instanceof EntityDamageByEntityEvent)
 		{
 			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)e;
 			if (event.getDamager() instanceof Player)
 			{
 				Player damager = (Player)event.getDamager();
 				if (!plugin.acc.UserIsLogged(damager))
 				{
 					e.setCancelled(true);
 					return;
 				}
 			}
 		}
 		if (plugin.config.accountsModule)
 		{
 			Player p[] = e.getEntity().getServer().getOnlinePlayers();
 			int i=0;
 			while (i < p.length)
 			{
 				if (e.getEntity().getEntityId() == p[i].getEntityId())
 				{	
 					if (!plugin.acc.UserIsLogged(p[i]))
 					{
 						e.setCancelled(true);
 					}
 					break;
 				}
 				i++;
 			}
 		}
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onEntityDeath(EntityDeathEvent e)
 	{
 		/*if (e.getDroppedExp() > 0)
 		{
 			e.setDroppedExp(0);
 		}*/
 		if (e.getEntity() instanceof Player)
 		{
 			Player p = (Player) e.getEntity();
 			if(p.getLocation().getWorld().getName().equalsIgnoreCase("world"))
 			{
 				if(playersDeaths.containsKey(p.getName()))
 				{
 					playersDeaths.remove(p.getName());
 					playersDeaths.put(p.getName(), p.getLocation());
 				}
 				else
 				{
 					playersDeaths.put(p.getName(), p.getLocation());
 				}
 			}
 			if(p.getName().matches("czrikub"))
 			{
 				InventoryBackup.CreateBackup(p.getName(), p.getInventory().getArmorContents(), p.getInventory().getContents(), p.getActivePotionEffects());
 				p.getInventory().clear();
 				e.getDrops().clear();
 				e.getDrops().add(new ItemStack(331, 1));
 			}
 			else if(p.getName().matches("Guga"))
 			{
 				InventoryBackup.CreateBackup(p.getName(), p.getInventory().getArmorContents(), p.getInventory().getContents(), p.getActivePotionEffects());
 				p.getInventory().clear();
 				e.getDrops().clear();
 				e.getDrops().add(new ItemStack(383, 1, (short) 50));
 			}
 			else if(p.getName().matches("Alma_Lodaka"))
 			{
 				InventoryBackup.CreateBackup(p.getName(), p.getInventory().getArmorContents(), p.getInventory().getContents(), p.getActivePotionEffects());
 				p.getInventory().clear();
 				e.getDrops().clear();
 				e.getDrops().add(new ItemStack(111, 1));
 			}
 			else if(p.getName().matches("Virus"))
 			{
 				String author = "Virus";
 				String title = "Pribeh boha Viruse!";
				String[] pages = {"Zemrel jsem za svuj milovany lid. Dokaz ze jsi mi verny a napis do chatu: Virus je buh!"};
 				Book book = new Book(title, author, pages);
 				InventoryBackup.CreateBackup(p.getName(), p.getInventory().getArmorContents(), p.getInventory().getContents(), p.getActivePotionEffects());
 				p.getInventory().clear();
 				e.getDrops().clear();
 				e.getDrops().add(book.generateItemStack());
 			}
 		}
 		if (plugin.arena.IsArena(e.getEntity().getLocation()))
 		{
 			if (e.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent)
 			{
 				EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)e.getEntity().getLastDamageCause();
 				if ((event.getDamager() instanceof Player) && (event.getEntity() instanceof Player))
 				{
 					Player damager = (Player)event.getDamager();
 					e.getDrops().clear();
 					Player target = (Player)event.getEntity();
 					plugin.arena.ArenaKill(damager, target);
 					//((PlayerDeathEvent)e).setDeathMessage(ChatColor.AQUA + damager.getName() + " zabil " + target.getName() + " v Arene!");
 					((PlayerDeathEvent)e).setDeathMessage(null);
 				}
 			}
 			return;
 		}
 		if (e.getEntity().getLastDamageCause() != null)
 		{
 			if (!(e.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent))
 			{
 				return;
 			}
 			Entity target = e.getEntity();
 			Entity ent = ((EntityDamageByEntityEvent)target.getLastDamageCause()).getDamager();
 			if (ent instanceof Player)
 			{
 				Player damager = (Player)ent;
 				GugaProfession prof = plugin.professions.get(damager.getName());
 				/*if (prof instanceof GugaMiner)
 				{
 					if (target instanceof Creeper)
 					{
 							prof.GainExperience(20);
 					}
 					else if(target instanceof Spider)
 					{
 						prof.GainExperience(15);
 					}
 					else if(target instanceof Skeleton)
 					{
 						prof.GainExperience(15);
 					}
 					else if(target instanceof Zombie)
 					{
 						prof.GainExperience(10);
 					}
 					else if (target instanceof Player)
 					{
 						prof.GainExperience(200);
 						damager.getWorld().dropItem(e.getEntity().getLocation(), new ItemStack(262,20));
 					}
 					else
 					{
 						prof.GainExperience(5);
 					}
 				}
 				else if (prof instanceof GugaHunter)
 				{*/
 					if (target instanceof Creeper)
 					{
 						prof.GainExperience(75);
 					}
 					else if(target instanceof Spider)
 					{
 						prof.GainExperience(50);
 					}
 					else if(target instanceof Skeleton)
 					{
 						prof.GainExperience(50);
 					}
 					else if(target instanceof Zombie)
 					{
 						prof.GainExperience(25);
 					}
 					else if (target instanceof Pig)
 					{
 						prof.GainExperience(5);
 					}
 					else if (target instanceof Player)
 					{
 						prof.GainExperience(200);
 						damager.getWorld().dropItem(e.getEntity().getLocation(), new ItemStack(262,20));
 					}
 					else
 					{
 						prof.GainExperience(15);
 					}
 				//}
 			}
 		}
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onEntityExplode(EntityExplodeEvent e)
 	{
 		if (plugin.debug)
 		{
 			plugin.log.info("ENTITY_EXPLODE_EVENT: entity=" + e.getEntity().toString());
 		}
 		if(e.getEntity() instanceof Creeper)
 		{
 			try{
 			Location loc = e.getLocation();
 		    WorldServer localWorldServer = ((CraftWorld)loc.getWorld()).getHandle();
 		    localWorldServer.makeSound(loc.getX(), loc.getY(), loc.getZ(), "random.explode", 4.0F, (1.0F + (localWorldServer.random.nextFloat() - localWorldServer.random.nextFloat()) * 0.2F) * 0.7F);
 		    localWorldServer.a("hugeexplosion", (int)loc.getX(), (int)loc.getY(), (int)loc.getZ());
 			}catch(Exception ex)
 			{
 				ex.printStackTrace();
 			}
 			e.setCancelled(true);
 			return;
 		}
 		if (plugin.arena.IsArena(e.getLocation()))
 		{
 			e.setCancelled(true);
 			return;
 		}
 		List<Block> blockList = e.blockList();
 
 		Iterator<Block> iter = e.blockList().iterator();
 		boolean foundChest = false;
 		while (iter.hasNext())
 		{
 			Block block = iter.next();
 			if (block.getTypeId() == 54)
 			{
 				foundChest = true;
 				break;
 			}
 
 		}
 		if (foundChest)
 		{
 			e.setCancelled(true);
 			iter = blockList.iterator();
 			while(iter.hasNext())
 			{
 				Block block = iter.next();
 				if (block.getTypeId() != 54)
 				{
 					block.setTypeId(0);
 				}
 			}
 		}
 	}
 	public static HashMap<String, Location> playersDeaths = new HashMap<String, Location>();
 	public static Guga_SERVER_MOD plugin;
 }
