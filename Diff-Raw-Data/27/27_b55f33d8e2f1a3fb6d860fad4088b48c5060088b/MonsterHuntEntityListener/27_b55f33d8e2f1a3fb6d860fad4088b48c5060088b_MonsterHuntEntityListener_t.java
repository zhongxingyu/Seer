 package com.matejdro.bukkit.monsterhunt.listeners;
 
 import java.util.Map.Entry;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.Giant;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Slime;
 import org.bukkit.entity.Spider;
 import org.bukkit.entity.Wolf;
 import org.bukkit.entity.Zombie;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageByProjectileEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 
 import com.matejdro.bukkit.monsterhunt.HuntWorldManager;
 import com.matejdro.bukkit.monsterhunt.HuntZone;
 import com.matejdro.bukkit.monsterhunt.MonsterHunt;
 import com.matejdro.bukkit.monsterhunt.MonsterHuntWorld;
 import com.matejdro.bukkit.monsterhunt.Util;
 
 public class MonsterHuntEntityListener extends EntityListener {
 private MonsterHunt plugin;
 	//HashMap<Integer, Player> lastHits = new HashMap<Integer, Player>();
 	//HashMap<Integer, Integer> lastHitCauses = new HashMap<Integer, Integer>();
 	public MonsterHuntEntityListener(MonsterHunt instance)
 	{
 		plugin = instance;
 	}
 	
 		
 	public void onEntityDeath (EntityDeathEvent event) {
 		if (event.getEntity() instanceof Player)
 		{
 			Player player = (Player) event.getEntity();
 			MonsterHuntWorld world = HuntWorldManager.getWorld(player.getWorld().getName());
 			
 			if (world == null || world.getWorld() == null) return;
 			if (world.settings.getInt("DeathPenalty") == 0) return;
 			
 			if (world.state > 1 && world.Score.containsKey(player.getName()))
 			{
 				double score = world.Score.get(player.getName()) + 0.00;
 				score = score - (score * world.settings.getInt("DeathPenalty") / 100.00);	
 				world.Score.put(player.getName(), (int) Math.round(score));
 				Util.Message(world.settings.getString("Messages.DeathMessage"),player);
 			}
 		}
 		if (!HuntZone.isInsideZone(event.getEntity().getLocation())) return;
		if (event.getEntity() == null || !(event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent)) return;
			
		MonsterHuntWorld world = HuntWorldManager.getWorld(event.getEntity().getWorld().getName());
		if (world == null || world.getWorld() == null || world.state < 2) return;	
			kill((LivingEntity) event.getEntity(), world);
		}
 	
 	
 	private void kill(LivingEntity monster, MonsterHuntWorld world)
 	{
 			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) monster.getLastDamageCause();
 			String name;
 			Player player = null;
 
 			String cause = "General";
 			if (event instanceof EntityDamageByProjectileEvent)
 			{
 				cause = "Arrow";
 			}
 			else if (event.getDamager() instanceof Wolf && ((Wolf) event.getDamager()).isTamed())
 			{
 				cause = "Wolf";
 				player = (Player) ((Wolf) event.getDamager()).getOwner();
 			}
 			
 			if (player == null) 
 			{
 				if (!(event.getDamager() instanceof Player)) return;
 				player = (Player) event.getDamager();
 			
 				if (cause.equals("General"))
 				{
 					if (player.getItemInHand() == null)
 						cause = String.valueOf(0);
 					else
 						cause = String.valueOf(player.getItemInHand().getTypeId());
 				}
 			}
 			
 			int points = 0;
 			if (monster instanceof Skeleton)
 			{
 				points = world.settings.getMonsterValue("Skeleton", cause);
 				name = "Skeleton";
 			}
 			else if (monster instanceof Spider)
 			{
 				points = world.settings.getMonsterValue("Spider", cause);
 				name = "Spider";
 			}
 			else if (monster instanceof Creeper)
 			{
 				Creeper creeper = (Creeper) monster;
 				if (creeper.isPowered())
 				{
 					points = world.settings.getMonsterValue("ElectrifiedCreeper", cause);
 					name = "Electrified Creeper";
 				}
 				else
 				{
 					points = world.settings.getMonsterValue("Creeper", cause);
 					name = "Creeper";
 				}
 			}
 			else if (monster instanceof Ghast)
 			{
 				points = world.settings.getMonsterValue("Ghast", cause);
 				name = "Ghast";
 			}
 			else if (monster instanceof Slime)
 			{
 				points = world.settings.getMonsterValue("Slime", cause);
 				name = "Slime";
 			}
 			else if (monster instanceof PigZombie)
 			{
 				points = world.settings.getMonsterValue("ZombiePigman", cause);
 				name = "Zombie Pigman";
 			}
 			else if (monster instanceof Giant)
 			{
 				points = world.settings.getMonsterValue("Giant", cause);
 				name = "Giant";
 			}
 			else if (monster instanceof Zombie)
 			{
 				points = world.settings.getMonsterValue("Zombie", cause);
 				name = "Zombie";
 			}
 			else if (monster instanceof Wolf)
 			{
 				Wolf wolf = (Wolf) monster;
 				if (wolf.isTamed())
 				{
 					points = world.settings.getMonsterValue("TamedWolf", cause);
 					name = "Tamed Wolf";
 				}
 				else
 				{
 					points = world.settings.getMonsterValue("WildWolf", cause);
 					name = "Wild Wolf";
 				}
 				
 			}
 			else if (monster instanceof Player)
 			{
 				points = world.settings.getMonsterValue("Player", cause);
 				name = "Player";
 			}
 			else
 			{
 				return;
 			}
 			if (points < 1) return;
 			
 			if (!world.Score.containsKey(player.getName()) && !world.settings.getBoolean("EnableSignup"))
 				world.Score.put(player.getName(), 0);
 			if (world.Score.containsKey(player.getName()))
 			{
 				if (!world.properlyspawned.contains(monster.getEntityId()) && world.settings.getBoolean("OnlyCountMobsSpawnedOutside"))
 				{
 					String message = world.settings.getString("Messages.KillMobSpawnedInsideMessage");
 					Util.Message(message, player);
 					world.blacklist.add(monster.getEntityId());
 					return;
 					
 				}
 				int newscore = world.Score.get(player.getName()) + points;
 
 				if (world.settings.getBoolean("AnnounceLead"))
 				{
 					Entry<String, Integer> leadpoints = null;
 					for (Entry<String,Integer> e : world.Score.entrySet())
 					{
 						if (leadpoints == null || e.getValue() > leadpoints.getValue() || (e.getValue() == leadpoints.getValue() && leadpoints.getKey().equalsIgnoreCase(player.getName())))
 						{
 							leadpoints = e;
 						}
 							
 					}
 					Util.Debug(leadpoints.toString());
 					Util.Debug(String.valueOf(newscore));
 					Util.Debug(String.valueOf(!leadpoints.getKey().equals(player.getName())));
 										
 					if (leadpoints != null && newscore > leadpoints.getValue() && !leadpoints.getKey().equals(player.getName()))
 					{
 						String message = world.settings.getString("Messages.MessageLead");
 						message = message.replace("<Player>", player.getName());
 						message = message.replace("<Points>", String.valueOf(newscore));
 						message = message.replace("<World>", world.name);
 						Util.Broadcast(message);
 						
 					}
 						
 				}
 				
 				world.Score.put(player.getName(), newscore);
 				world.blacklist.add(monster.getEntityId());
 				
 				world.properlyspawned.remove((Object) monster.getEntityId());
 				
 				String message = world.settings.getKillMessage(cause);
 				message = message.replace("<MobValue>", String.valueOf(points));
 				message = message.replace("<MobName>", name);
 				message = message.replace("<Points>",String.valueOf(newscore));
 				Util.Message(message,player);
 				}
 	}
 	
 	public void onCreatureSpawn(CreatureSpawnEvent event) {
 		if (event.getEntity() instanceof Creature)
 		{
 			MonsterHuntWorld world = HuntWorldManager.getWorld(event.getLocation().getWorld().getName());
 			if (world == null || world.getWorld() == null) return;
 			if (world.state == 0) return;
 			if (!world.settings.getBoolean("OnlyCountMobsSpawnedOutside")) return;
 			Block block = event.getLocation().getBlock();
 			int number = 0;
 			while (block.getY() < 125)
 			{
 				number++;
 				block = block.getFace(BlockFace.UP);
 				Boolean empty = false;
 				
 				if (block.getType() == Material.AIR || block.getType() == Material.LEAVES) empty = true;
 				else if (block.getType() == Material.LOG)
 				{
 					if (block.getFace(BlockFace.NORTH).getType() == Material.AIR || block.getFace(BlockFace.NORTH).getType() == Material.LEAVES) empty = true;
 					else if (block.getFace(BlockFace.EAST).getType() == Material.AIR || block.getFace(BlockFace.EAST).getType() == Material.LEAVES) empty = true;
 					else if (block.getFace(BlockFace.WEST).getType() == Material.AIR || block.getFace(BlockFace.WEST).getType() == Material.LEAVES) empty = true;
 					else if (block.getFace(BlockFace.SOUTH).getType() == Material.AIR || block.getFace(BlockFace.SOUTH).getType() == Material.LEAVES) empty = true;
 					else if (block.getFace(BlockFace.UP).getType() == Material.AIR || block.getFace(BlockFace.UP).getType() == Material.LEAVES) empty = true;
 					else if (block.getFace(BlockFace.DOWN).getType() == Material.AIR || block.getFace(BlockFace.DOWN).getType() == Material.LEAVES) empty = true;
 				}
 				
 				if (!empty) return;
 				if (world.settings.getInt("OnlyCountMobsSpawnedOutsideHeightLimit") > 0 && world.settings.getInt("OnlyCountMobsSpawnedOutsideHeightLimit") < number) break;
 			}
 				world.properlyspawned.add(event.getEntity().getEntityId());
 
 			
 		}
 
 	}
 	
 }
