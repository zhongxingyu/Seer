 package com.olympuspvp.squads;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 
 import com.olympuspvp.spawn.olySpawn;
 
 
 public class TeleportManager implements Listener{
 
 	HashMap<String, Double> waiting = new HashMap<String, Double>();
 	olySquads squad;
 	olySpawn spawn;
 	String tag = ChatColor.GOLD + "[" + ChatColor.YELLOW + "olySquads" + ChatColor.GOLD + "] " + ChatColor.GRAY;
 
 	protected TeleportManager(final olySquads squad){
 		this.squad = squad;
 		spawn = (olySpawn) Bukkit.getPluginManager().getPlugin("olySpawn");
 		Bukkit.getPluginManager().registerEvents(this, squad);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerTeleport(final PlayerTeleportEvent e){
 		final Player p = e.getPlayer();
 		if(e.getCause() != TeleportCause.COMMAND) return;
 		if(spawn.isProtected(p)) return;
 		if(waiting.containsKey(p.getName())){
 			p.sendMessage(tag + "You already have a teleport in progress.");
 			return;
 		}
 		final List<String> playersNearby = new ArrayList<String>();
 		for(final Entity ent : p.getNearbyEntities(20, 20, 20)){
 			if(ent instanceof Player) playersNearby.add(((Player)ent).getName());
		}if(playersNearby.size() == 0 || p.getGameMode() == GameMode.CREATIVE){ 
 			p.sendMessage(tag + "You have been teleported.");
 			return;
 		}final Squad s = squad.getPlayerSquad(p.getName());
 		if(s != null){
 			for(final String name : s.getMembers()){
 				if(playersNearby.contains(name)) playersNearby.remove(name);
 			}
 		}if(playersNearby.size() == 0){
 			p.sendMessage(tag + "You have been teleported.");
 			return;
 		}
 
 		final Location tpto = e.getTo();
 		final Random r = new Random();
 		final double serializedTeleportCode = r.nextDouble();
 		waiting.put(p.getName(), serializedTeleportCode);
 		e.setCancelled(true);
 		p.sendMessage(tag + "There are enemies nearby!");
 		p.sendMessage(tag + "Your teleport will begin in ten seconds.");
 		Bukkit.getScheduler().scheduleSyncDelayedTask(squad, new Runnable(){
 			@Override
 			public void run(){
 
 				if(waiting.containsKey(p.getName())){
 					final double key = waiting.get(p.getName());
 					if(key != serializedTeleportCode) return;
 					p.sendMessage(tag + "You have been teleported.");
 					p.teleport(tpto, TeleportCause.PLUGIN);
 					waiting.remove(p.getName());
 				}
 
 			}
 		}, 200L);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerMove(final PlayerMoveEvent e){
 		final Player p = e.getPlayer();
 		if(waiting.containsKey(p.getName())){
 			Location from = e.getFrom();
 			Location to = e.getTo();
 			if(from.getX() != to.getX() || from.getY() != to.getY() || from.getY() != to.getY()){
 				waiting.remove(p.getName());
 				p.sendMessage(tag + "Your in-progress teleport has been cancelled.");
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerDamage(final EntityDamageEvent e){
 		Entity ent = e.getEntity();
 		if(!(ent instanceof Player)) return;
 		final Player p = (Player) ent;
 		if(waiting.containsKey(p.getName())){
 			waiting.remove(p.getName());
 			p.sendMessage(tag + "Your in-progress teleport has been cancelled.");
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerDamage(final BlockPlaceEvent e){
 		final Player p = e.getPlayer();
 		if(waiting.containsKey(p.getName())){
 			waiting.remove(p.getName());
 			p.sendMessage(tag + "Your in-progress teleport has been cancelled.");
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerDamage(final BlockBreakEvent e){
 		final Player p = e.getPlayer();
 		if(waiting.containsKey(p.getName())){
 			waiting.remove(p.getName());
 			p.sendMessage(tag + "Your in-progress teleport has been cancelled.");
 		}
 	}
 
 }
