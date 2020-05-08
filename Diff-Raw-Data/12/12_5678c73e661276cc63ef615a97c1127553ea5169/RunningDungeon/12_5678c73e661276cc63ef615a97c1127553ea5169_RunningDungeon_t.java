 package com.imdeity.deitydungeons.obj;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Damageable;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
 import org.bukkit.inventory.EntityEquipment;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 
 import com.imdeity.deityapi.DeityAPI;
 import com.imdeity.deitydungeons.Cloud;
 import com.imdeity.deitydungeons.DeityDungeons;
 
 //Represents a running dungeon
 public class RunningDungeon {
 	Dungeon dungeon;
 
 	ArrayList<Player> players = new ArrayList<Player>();
 	ArrayList<Player> originalPlayers = new ArrayList<Player>();
 	ArrayList<Entity> entities = new ArrayList<Entity>();
 
 	ArrayList<Mob> mobsToBeSpawned = new ArrayList<Mob>();
 
 	Player player;
 
 	Date start;
 
 	public RunningDungeon(Dungeon dungeon, Player[] playerArray) {
 		this.dungeon = dungeon;
 
 		for(Player player : playerArray) {
 			this.players.add(player);
 			this.originalPlayers.add(player);
 		}
 
 		this.start = new Date();
 
 		mobsToBeSpawned = new ArrayList<Mob>(dungeon.getMobs());
 	}
 
 	public ArrayList<Entity> getSpawnedEntities() {
 		return entities;
 	}
 
 	public boolean containsPlayer(Player player) {
 		//This will be an active player
 		return players.contains(player);
 	}
 
 	public boolean containsMob(Entity e) {
 		return entities.contains(e);
 	}
 
 	public void removePlayer(Player player) {
 		players.remove(player);
 	}
 
 	//It is possible for mobs to be left in the world after the
 	//dungeon ends, this removes them
 	public void removeAllMobs() {
 		for(Entity entity : entities) {
 			entity.remove();
 		}
 	}
 
 	public boolean hasPlayers() {
 		return players.size() != 0;
 	}
 
 	public Dungeon getDungeon() {
 		return this.dungeon;
 	}
 
 	public Date getStart() {
 		return this.start;
 	}
 
 	public ArrayList<Player> getOriginalPlayers() {
 		return this.originalPlayers;
 	}
 
 	public boolean notifyDeathOfMob(Entity e) {
 		if(entities.contains(e)) {
 			entities.remove(e);
 		}
 
 		return true;
 	}
 
 	public void handleMove(Player player, Location playerAt) {
 		//First check to see if dungeon has been finished
 		Location endAt = dungeon.getFinish();
 
 		//Vector of the player
 		Vector p = new Vector(playerAt.getBlockX(), playerAt.getBlockY(), playerAt.getBlockZ());
 
 		//Vector of the end point
 		Vector e = new Vector(endAt.getBlockX(), endAt.getBlockY(), endAt.getBlockZ());
 
 		//Distance from end
 		int distance = (int) p.distance(e);
 
 		//See if they are close enough
 		if(distance <= DeityDungeons.FINISH_DISTANCE) {
 			//Player is close enough to win
 
 			DeityAPI.getAPI().getChatAPI().sendPlayerMessage(player, "DeityDungeons", "<green>Congratulations! You have completed the dungeon <yellow>" + dungeon.getName()+ "<green>!");
 
 			//is player active in the dungeon?
 			if(players.contains(player)) {
 				//remove them from the list so they only get the message once
 				players.remove(player);
 			}
 			Cloud.playerFinishedDungeon(player, dungeon.getID());
 		}
 
 		//maybe we have to spawn a mob
 		for(Mob mob : mobsToBeSpawned) {
 			//Vector of the player
 			Vector p1 = new Vector(playerAt.getBlockX(), playerAt.getBlockY(), playerAt.getBlockZ());
 
 			//Vector of the mob
 			Vector m = new Vector(mob.getX(), mob.getY(), mob.getZ());
 
 			//Distance from player
 			int mobDistance = (int) p1.distance(m);
 			
 			//See if they are close enough
 			if(mobDistance <= DeityDungeons.MOB_SPAWN_DISTANCE) {
 				//Spawn the mob(s)
 				for(int i = 0; i < mob.getAmount(); i++) {
 
 					Entity entity = dungeon.getWorld().spawnEntity(mob.getLocation(), mob.getType());
 					if(mob.shouldTarget()) //if player has a target 
 						((Creature) entity).setTarget(player);
 
 					//cant get the equipment of a tnt or boat
 					if(entity instanceof LivingEntity) {
 
 						EntityEquipment equip = ((LivingEntity) entity).getEquipment();
 						equip.setHelmet(new ItemStack(mob.getHelm() == ArmorMaterial.AIR ? Material.AIR : Material.getMaterial(mob.getHelm().getName() + "_HELMET")));
 						equip.setChestplate(new ItemStack(mob.getChest() == ArmorMaterial.AIR ? Material.AIR : Material.getMaterial(mob.getChest().getName() + "_CHESTPLATE")));
 						equip.setLeggings(new ItemStack(mob.getPants() == ArmorMaterial.AIR ? Material.AIR : Material.getMaterial(mob.getPants().getName() + "_LEGGINGS")));
 						equip.setBoots(new ItemStack(mob.getFeet() == ArmorMaterial.AIR ? Material.AIR : Material.getMaterial(mob.getFeet().getName() + "_BOOTS"))); 
 
 						//disabling armor drops
 						equip.setHelmetDropChance(0);
 						equip.setChestplateDropChance(0);
 						equip.setLeggingsDropChance(0);
 						equip.setBootsDropChance(0);
 
 
 						if(mob.getHealth() > 0) {
 							((Damageable)entity).setMaxHealth(mob.getHealth());
 							((Damageable)entity).setHealth(mob.getHealth());
 						}
 						
 						//add weapon if there is one
						System.out.println(entity instanceof Skeleton);
						if(mob.getWeapon() != Material.AIR && !(entity instanceof Skeleton)) {
 							equip.setItemInHand(new ItemStack(mob.getWeapon()));
 							//don't drop it
 							equip.setItemInHandDropChance(0);
 						}
 					}
 
 					entities.add(entity);
 				}
 
 				mobsToBeSpawned.remove(mob);
 				
 				//we've spawned a mob, done for this loop
 				break;
 
 			}
 
 		}
 	}
 }
