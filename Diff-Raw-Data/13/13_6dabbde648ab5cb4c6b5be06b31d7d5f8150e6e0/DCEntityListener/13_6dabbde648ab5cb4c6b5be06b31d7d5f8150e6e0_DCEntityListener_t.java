 package com.vildaberper.DefaultCommands.Listener;
 
 import org.bukkit.DyeColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.Fireball;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.Giant;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Painting;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Slime;
 import org.bukkit.entity.Spider;
 import org.bukkit.entity.TNTPrimed;
 import org.bukkit.entity.Zombie;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageByProjectileEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.vildaberper.DefaultCommands.Misc;
 import com.vildaberper.DefaultCommands.Perm;
 import com.vildaberper.DefaultCommands.V;
 
 public class DCEntityListener extends EntityListener{
 	@Override
 	public void onEntityDeath(EntityDeathEvent event){
 		if(event.getEntity() instanceof Pig && ((Pig) event.getEntity()).hasSaddle() && Misc.getConfig(event.getEntity()).getBoolean("saddled_pig_drop_saddle")){
 			event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.SADDLE, 1));
 		}
 	}
 
 	@Override
 	public void onEntityTarget(EntityTargetEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		if(event.getTarget() != null && Misc.isIgnoremob(event.getTarget().getEntityId())){
 			event.setCancelled(true);
 		}
 		if(event.getEntity() instanceof Creeper && Misc.getConfig(event.getEntity()).getBoolean("friendly_creeper")){
 			event.setCancelled(true);
 		}else if(event.getEntity() instanceof Ghast && Misc.getConfig(event.getEntity()).getBoolean("friendly_ghast")){
 			event.setCancelled(true);
 		}else if(event.getEntity() instanceof Giant && Misc.getConfig(event.getEntity()).getBoolean("friendly_giant")){
 			event.setCancelled(true);
 		}else if(event.getEntity() instanceof Monster && Misc.getConfig(event.getEntity()).getBoolean("friendly_monster")){
 			event.setCancelled(true);
 		}else if(event.getEntity() instanceof PigZombie && Misc.getConfig(event.getEntity()).getBoolean("friendly_pigzombie")){
 			event.setCancelled(true);
 		}else if(event.getEntity() instanceof Skeleton && Misc.getConfig(event.getEntity()).getBoolean("friendly_skeleton")){
 			event.setCancelled(true);
 		}else if(event.getEntity() instanceof Slime && Misc.getConfig(event.getEntity()).getBoolean("friendly_slime")){
 			event.setCancelled(true);
 		}else if(event.getEntity() instanceof Spider && Misc.getConfig(event.getEntity()).getBoolean("friendly_spider")){
 			event.setCancelled(true);
 		}else if(event.getEntity() instanceof Zombie && Misc.getConfig(event.getEntity()).getBoolean("friendly_zombie")){
 			event.setCancelled(true);
 		}
 	}
 
 	@Override
 	public void onEntityExplode(EntityExplodeEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		if(event.getEntity() instanceof TNTPrimed){
 			if(Misc.getConfig(event.getEntity()).getBoolean("block_tnt_explosion_block_damage")){
 				event.setCancelled(true);
 			}
 		}else if(event.getEntity() instanceof Creeper){
 			if(Misc.getConfig(event.getEntity()).getBoolean("block_creeper_explosion_block_damage")){
 				event.setCancelled(true);
 			}
 		}else if(event.getEntity() instanceof Fireball){
 			if(Misc.getConfig(event.getEntity()).getBoolean("block_fireball_explosion_block_damage")){
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@Override
 	public void onEntityDamage(EntityDamageEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		if(event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event).getDamager() instanceof Player){
 			if(!Perm.hasPermissionSilent((Player) ((EntityDamageByEntityEvent) event).getDamager(), "dc.do." + event.getEntity().getWorld().getName())){
 				event.setCancelled(true);
 				return;
 			}
 		}
 		if(event.getCause().equals(DamageCause.FALL) && Misc.isFly(event.getEntity().getEntityId())){
 			event.setCancelled(true);
 			return;
 		}
 		if(event instanceof EntityDamageByEntityEvent){
			if(((EntityDamageByEntityEvent) event).getDamager() != null){
				if(Misc.isInstakill(((EntityDamageByEntityEvent) event).getDamager().getEntityId())){
					event.setDamage(((org.bukkit.craftbukkit.entity.CraftCreature) event.getEntity()).getHealth());
				}
 			}
 		}
 		if(event.getEntity() instanceof Painting){
 			if(event.getCause().equals(DamageCause.ENTITY_EXPLOSION)){
 				if(event instanceof EntityDamageByEntityEvent){
 					EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
 
 					if(e.getDamager() instanceof TNTPrimed){
 						if(Misc.getConfig(event.getEntity()).getBoolean("block_tnt_explosion_block_damage")){
 							event.setCancelled(true);
 							return;
 						}
 					}else if(e.getDamager() instanceof Creeper){
 						if(Misc.getConfig(event.getEntity()).getBoolean("block_creeper_explosion_block_damage")){
 							event.setCancelled(true);
 							return;
 						}
 					}else if(e.getDamager() instanceof Fireball){
 						if(Misc.getConfig(event.getEntity()).getBoolean("block_fireball_explosion_block_damage")){
 							event.setCancelled(true);
 							return;
 						}
 					}
 				}
 			}
 		}else if(event.getEntity() instanceof Player){
 			if(Misc.isGod(event.getEntity().getEntityId())){
 				event.setCancelled(true);
 				return;
 			}
 			if(event.getCause().equals(DamageCause.DROWNING)){
 				if(Misc.getConfig(event.getEntity()).getBoolean("block_drowning_player_damage")){
 					event.setCancelled(true);
 					return;
 				}
 			}else if(event.getCause().equals(DamageCause.FALL)){
 				if(Misc.getConfig(event.getEntity()).getBoolean("block_fall_player_damage")){
 					event.setCancelled(true);
 					return;
 				}
 			}else if(event.getCause().equals(DamageCause.FIRE) || event.getCause().equals(DamageCause.FIRE_TICK)){
 				if(Misc.getConfig(event.getEntity()).getBoolean("block_fire_player_damage")){
 					event.setCancelled(true);
 					return;
 				}
 			}else if(event.getCause().equals(DamageCause.LAVA)){
 				if(Misc.getConfig(event.getEntity()).getBoolean("block_lava_player_damage")){
 					event.setCancelled(true);
 					return;
 				}
 			}else if(event.getCause().equals(DamageCause.SUFFOCATION)){
 				if(Misc.getConfig(event.getEntity()).getBoolean("block_suffocation_player_damage")){
 					event.setCancelled(true);
 					return;
 				}
 			}else if(event.getCause().equals(DamageCause.VOID)){
 				if(Misc.getConfig(event.getEntity()).getBoolean("block_void_player_damage")){
 					event.setCancelled(true);
 					return;
 				}
 			}
 			if(event instanceof EntityDamageByEntityEvent){
 				if(((EntityDamageByEntityEvent) event).getDamager() instanceof Player){
 					if(Misc.getConfig(event.getEntity()).getBoolean("block_player_player_damage")){
 						event.setCancelled(true);
 						return;
 					}
 				}else if(((EntityDamageByEntityEvent) event).getDamager() instanceof TNTPrimed){
 					if(Misc.getConfig(event.getEntity()).getBoolean("block_tnt_explosion_player_damage")){
 						event.setCancelled(true);
 						return;
 					}
 				}else if(((EntityDamageByEntityEvent) event).getDamager() instanceof Creeper){
 					if(Misc.getConfig(event.getEntity()).getBoolean("block_creeper_explosion_player_damage")){
 						event.setCancelled(true);
 						return;
 					}
 				}else if(((EntityDamageByEntityEvent) event).getDamager() instanceof Fireball){
 					if(Misc.getConfig(event.getEntity()).getBoolean("block_fireball_explosion_player_damage")){
 						event.setCancelled(true);
 						return;
 					}
 				}else if(Misc.getConfig(event.getEntity()).getBoolean("block_mob_player_damage") && Misc.isMonster(((EntityDamageByEntityEvent) event).getDamager())){
 					event.setCancelled(true);
 					return;
 				}
 			}
 			if(event instanceof EntityDamageByProjectileEvent){
 				if(((EntityDamageByProjectileEvent) event).getProjectile() instanceof Arrow){
 					if(Misc.getConfig(event.getEntity()).getBoolean("block_arrow_player_damage")){
 						event.setCancelled(true);
 						return;
 					}
 				}
 				if(((EntityDamageByProjectileEvent) event).getEntity() instanceof Arrow){
 					if(Misc.getConfig(event.getEntity()).getBoolean("block_arrow_player_damage")){
 						event.setCancelled(true);
 						return;
 					}
 				}
 			}
 		}
 		if(!(event.getEntity() instanceof Item) && !event.getCause().equals(DamageCause.DROWNING) && !event.getCause().equals(DamageCause.FIRE) && !event.getCause().equals(DamageCause.FIRE_TICK) & !event.getCause().equals(DamageCause.LAVA)){
 			if(!(event.getEntity() instanceof Sheep) || event.getEntity() instanceof Sheep && ((Sheep) event.getEntity()).isSheared()){
 				if(Misc.getConfig(event.getEntity()).getBoolean("blood")){
 					Location location = event.getEntity().getLocation();
 
 					location.setY(location.getY() + 1);
 					for(int i = 0; i <= (int) (event.getDamage() * Misc.getConfig(event.getEntity()).getDouble("blood_level")); i++){
 						final Item item;
 
 						if(event.getEntity() instanceof Skeleton){
 							item = event.getEntity().getWorld().dropItem(location, new ItemStack(Material.BONE, 0));
 						}else if(event.getEntity() instanceof Creeper){
 							item = event.getEntity().getWorld().dropItem(location, new ItemStack(Material.WOOL, 0, (short) 0, Byte.parseByte("5")));
 						}else if(event.getEntity() instanceof Giant){
 							location.setY(location.getY() + 3);
 							item = event.getEntity().getWorld().dropItem(location, new ItemStack(Material.WOOL, 0, (short) 0, Byte.parseByte("14")));
 						}else{
 							item = event.getEntity().getWorld().dropItem(location, new ItemStack(Material.WOOL, 0, (short) 0, Byte.parseByte("14")));
 						}
 						V.nopickups.add(item);
 						V.plugin.getServer().getScheduler().scheduleSyncDelayedTask(V.plugin,
 								new Runnable(){
 							@Override
 							public void run(){
 								V.nopickups.remove(item);
 								item.remove();
 							}
 						},
 						(long) (20 * Misc.getConfig(event.getEntity()).getDouble("blood_despawn_time")));
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onCreatureSpawn(CreatureSpawnEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		if(event.getCreatureType().equals(CreatureType.CHICKEN)){
 			if(Misc.getConfig(event.getEntity()).getBoolean("block_chicken_spawn")){
 				event.setCancelled(true);
 			}
 		}else if(event.getCreatureType().equals(CreatureType.COW)){
 			if(Misc.getConfig(event.getEntity()).getBoolean("block_cow_spawn")){
 				event.setCancelled(true);
 			}
 		}else if(event.getCreatureType().equals(CreatureType.CREEPER)){
 			if(Misc.getConfig(event.getEntity()).getBoolean("block_creeper_spawn")){
 				event.setCancelled(true);
 			}
 		}else if(event.getCreatureType().equals(CreatureType.GHAST)){
 			if(Misc.getConfig(event.getEntity()).getBoolean("block_ghast_spawn")){
 				event.setCancelled(true);
 			}
 		}else if(event.getCreatureType().equals(CreatureType.GIANT)){
 			if(Misc.getConfig(event.getEntity()).getBoolean("block_giant_spawn")){
 				event.setCancelled(true);
 			}
 		}else if(event.getCreatureType().equals(CreatureType.MONSTER)){
 			if(Misc.getConfig(event.getEntity()).getBoolean("block_monster_spawn")){
 				event.setCancelled(true);
 			}
 		}else if(event.getCreatureType().equals(CreatureType.PIG)){
 			if(Misc.getConfig(event.getEntity()).getBoolean("block_pig_spawn")){
 				event.setCancelled(true);
 			}
 		}else if(event.getCreatureType().equals(CreatureType.PIG_ZOMBIE)){
 			if(Misc.getConfig(event.getEntity()).getBoolean("block_pigzombie_spawn")){
 				event.setCancelled(true);
 			}
 		}else if(event.getCreatureType().equals(CreatureType.SHEEP)){
 			if(Misc.getConfig(event.getEntity()).getBoolean("block_sheep_spawn")){
 				event.setCancelled(true);
 			}
 			if(Misc.getConfig(event.getEntity()).getBoolean("magic_sheep")){
 				((Sheep) event.getEntity()).setColor(DyeColor.values()[(int) Math.round((Math.random() * 15))]);
 			}
 		}else if(event.getCreatureType().equals(CreatureType.SKELETON)){
 			if(Misc.getConfig(event.getEntity()).getBoolean("block_skeleton_spawn")){
 				event.setCancelled(true);
 			}
 		}else if(event.getCreatureType().equals(CreatureType.SLIME)){
 			if(Misc.getConfig(event.getEntity()).getBoolean("block_slime_spawn")){
 				event.setCancelled(true);
 			}
 		}else if(event.getCreatureType().equals(CreatureType.SPIDER)){
 			if(Misc.getConfig(event.getEntity()).getBoolean("block_spider_spawn")){
 				event.setCancelled(true);
 			}
 		}else if(event.getCreatureType().equals(CreatureType.SQUID)){
 			if(Misc.getConfig(event.getEntity()).getBoolean("block_squid_spawn")){
 				event.setCancelled(true);
 			}
 		}else if(event.getCreatureType().equals(CreatureType.WOLF)){
 			if(Misc.getConfig(event.getEntity()).getBoolean("block_wolf_spawn")){
 				event.setCancelled(true);
 			}
 		}else if(event.getCreatureType().equals(CreatureType.ZOMBIE)){
 			if(Misc.getConfig(event.getEntity()).getBoolean("block_zombie_spawn")){
 				event.setCancelled(true);
 			}
 		}
 	}
 }
