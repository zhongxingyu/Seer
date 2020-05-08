 package org.avateam.ava;
 
 import java.util.HashMap;
 
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.getspout.commons.ChatColor;
 
 public class ChatListener implements Listener {
 	private Main plugin;
 	private HashMap<Player, Boolean> listen = new HashMap<Player, Boolean>();
 	private HashMap<Player, AvaNPC> has = new HashMap<Player, AvaNPC>();
 	public HashMap<LivingEntity, AvaNPC> targets = new HashMap<LivingEntity, AvaNPC>();
 	
 	public ChatListener(Main main) {
 		plugin = main;
 	}
 	
 	@EventHandler
 	public void hurt(EntityDamageEvent event) {
 		if(targets.containsKey(event.getEntity())) {
 			AvaNPC npc = targets.get(event.getEntity());
 			targets.remove(event.getEntity());
 			npc.stop(plugin);
 		}
 		if(plugin.nm.isNPC(event.getEntity())) {
			//TODO: Negativing damage of mobs? only player does damage
 			if(event instanceof EntityDamageByEntityEvent) {
 				String id = plugin.nm.getNPCIdFromEntity(event.getEntity());
 				AvaNPC npc = (AvaNPC) plugin.nm.getNPC(id);
 				if(event.getCause() == DamageCause.PROJECTILE) {
 					LivingEntity target = ((Arrow) ((EntityDamageByEntityEvent) event).getDamager()).getShooter();
 					npc.attck(target, plugin);
 				} else if(event.getCause() == DamageCause.ENTITY_ATTACK){
 					LivingEntity target = (LivingEntity) ((EntityDamageByEntityEvent) event).getDamager();
 					npc.attck(target, plugin);
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void death(EntityDeathEvent event) {
 		if(plugin.nm.isNPC(event.getEntity())) {
 			String id = plugin.nm.getNPCIdFromEntity(event.getEntity());
 			AvaNPC npc = (AvaNPC) plugin.nm.getNPC(id);
 			//TODO: Most likely cause for below error, but needed to stop Ava's actions
 			npc.stop(plugin);
 			has.remove(npc.getOwner());
 			//TODO: Causes Error with Arrows but without does not remove Ava
 			npc.removeFromWorld();
 		}
 	}
 	
 	@EventHandler
 	public void joined(PlayerJoinEvent event) {
 		if(listen.containsKey(event.getPlayer()) == false) {
 			listen.put(event.getPlayer(),true);
 		}
 	}
 	
 	@EventHandler
 	public void checkMessage(PlayerChatEvent event) {
 		String[] parts = event.getMessage().split("[ ,.?!]+");
 		if(event.getMessage().equalsIgnoreCase("don't listen to me ava")) {
 			event.getPlayer().sendMessage("[Ava]: Okay, I will stop listening to you.");
 			listen.put(event.getPlayer(), false);
 		} else if (event.getMessage().equalsIgnoreCase("listen to me ava")){
 			event.getPlayer().sendMessage("[Ava]: Okay, What do you want to tell me?");
 			listen.put(event.getPlayer(), true);
 		} 
 		if(listen.containsKey(event.getPlayer()) == false) listen.put(event.getPlayer(), true);
 		if(listen.get(event.getPlayer()) == true) {
 			for(int i=0; i < parts.length; i++) {
 				if((i+1) < parts.length) {
 					String test = (parts[i] + " " + parts[i+1]);
 					if(test.equalsIgnoreCase("ava stop") || test.equalsIgnoreCase("stop ava")) {
 						if(has.containsKey(event.getPlayer()) == false) {
 							event.getPlayer().sendMessage(ChatColor.GRAY + "Wait... What am I thinking? She's not here.");
 							event.setCancelled(true);
 						} else {
 							AvaNPC npc = has.get(event.getPlayer());
 							npc.chat("Alright I'll stop.");
 							npc.stop(plugin);
 						}
 					}
 				}
 				if((i+2) < parts.length) {
 					String test = (parts[i] + " " + parts[i+1] + " " + parts[i+2]);
 					if(test.equalsIgnoreCase("ava come here") || test.equalsIgnoreCase("come here ava")) {
 						if(has.containsKey(event.getPlayer()) == false) {
 							AvaNPC npc = (AvaNPC) plugin.nm.spawnAva(event.getPlayer().getLocation());
 							npc.setOwner(event.getPlayer());
 							has.put(event.getPlayer(), npc);
 							npc.chat("Coming!");
 						} else {
 							AvaNPC npc = has.get(event.getPlayer());
 							npc.moveTo(event.getPlayer().getLocation());
 							npc.chat("Coming!");
 						}
 					} else if(test.equalsIgnoreCase("ava chop wood") || test.equalsIgnoreCase("chop wood ava")) {
 						if(has.containsKey(event.getPlayer()) == false) {
 							event.getPlayer().sendMessage(ChatColor.GRAY + "Wait... What am I thinking? She's not here.");
 							event.setCancelled(true);
 						} else {
 							AvaNPC npc = has.get(event.getPlayer());
 							npc.chat("Alright I'll go get some wood.");
 							npc.chop();
 						}
 					} else if((parts[i] + " " + parts[i+1]).equalsIgnoreCase("ava fight") || (parts[i] + " " + parts[i+1]).equalsIgnoreCase("ava attack")) {
 						if(has.containsKey(event.getPlayer()) == false) {
 							event.getPlayer().sendMessage(ChatColor.GRAY + "Wait... What am I thinking? She's not here.");
 							event.setCancelled(true);
 						} else {
 							AvaNPC npc = has.get(event.getPlayer());
 							Player p = npc.getBukkitEntity().getServer().getPlayer(parts[i+2]);
 							if(p != null) {
 								npc.chat("Better Run " + p.getName() + "!");
 								npc.attck(p,plugin);
 							} else {
 								npc.chat("Who should I attack?");
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 }
