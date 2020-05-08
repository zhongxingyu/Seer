 package com.github.r0306.RollTheDice.Handlers;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerToggleSprintEvent;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.util.Vector;
 import org.xml.sax.SAXException;
 
 import com.github.r0306.RollTheDice.Executor;
 import com.github.r0306.RollTheDice.RollTheDice;
 import com.github.r0306.RollTheDice.DiceHandlers.Arena;
 import com.github.r0306.RollTheDice.DiceHandlers.Dice;
 import com.github.r0306.RollTheDice.DiceHandlers.MovementHandlers;
 import com.github.r0306.RollTheDice.Util.Plugin;
 
 public class PlayerHandlers extends Executor implements Listener 
 {
 	
 	private RollTheDice plugin;
 	
 	public PlayerHandlers(RollTheDice plugin) {
 		super(plugin);
 		this.plugin = plugin;
 	}
 
 	HashMap<Player, Location> locations = new HashMap<Player, Location>();
 	@EventHandler
 	public void onLeave(PlayerQuitEvent event)
 	{
 		
 		Player player = event.getPlayer();
 		
 		if (inMatch.contains(player) && isStarted)
 		{
 			
 			leaveMatch(false, player);
 			
 		}
 		
 	}
 	
 	@EventHandler
 	public void onJoin(PlayerJoinEvent event)
 	{
 		
 		Player player = event.getPlayer();
 
 		List<String> playerNames = plugin.getConfig().getStringList("Players.List");
 		
 		if (!playerNames.contains(player.getName()))
 		{
 			
 			playerNames.add(player.getName());
 			
 		}
 		
 		plugin.getConfig().set("Players.List", playerNames);
 		plugin.getConfig().set("Data." + player.getName() + ".Wins", 0);
 		plugin.getConfig().set("Data." + player.getName() + ".Kills", 0);
 		plugin.saveConfig();
 		
 		
 	}
 	
 	@EventHandler
 	public void onDeath(PlayerDeathEvent event) throws SAXException, IOException, ParserConfigurationException
 	{
 		
 		Player player = event.getEntity();
 		Player killer = null;
 		int min = plugin.getConfig().getInt("RTD.Minimum");
 
 		if (inMatch.contains(player))
 		{
 			
 			if (player.getKiller() != null)
 			{
 				
 				killer = player.getKiller();
 				
 			}
 			else
 			{
 				
 				
 				
 			}
 
 			kills.put(killer, kills.get(killer) + 1);
 				
 			if (kills.get(killer) >= min)
 			{
 				
 				stopMatch(false, killer);
 				
 			}
 			
 		}
 		
 	}
 	
 	@EventHandler
 	public void onRespawn(PlayerRespawnEvent event) throws SAXException, IOException, ParserConfigurationException
 	{
 		
 		final Player player = event.getPlayer();
 		
 		if (inMatch.contains(player))
 		{
 			assignPlayer(player, Dice.roll());	
 			
 		}
 		
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
 		{
 			
 			   public void run()
 			   {
 			    
 				   	player.setExp(1F);
 			   
 			   }
 			
 		}, 5L);
 		
 	}
 	
 	@EventHandler
 	public void onInteract(PlayerInteractEvent e) throws SAXException, IOException, ParserConfigurationException
 	{
 		Executor ee = new Executor(plugin);
		ee.assignPlayer(e.getPlayer(), 13);
 
 		
 	}
 	public 		Vector v = null;
 	@EventHandler
 	public void onMove(PlayerMoveEvent event){
 
 	}
 	
 	boolean sprint = false;
 	/*
 	@EventHandler
 	public void onToggle(PlayerToggleSprintEvent event)
 	{
 		final int food = event.getPlayer().getFoodLevel();
 		final Player p = event.getPlayer();
 		
 		if (event.isSprinting())
 		{
 			
 			event.getPlayer().setFoodLevel(1);
 			//sprint = true;
 			//event.getPlayer().setVelocity(event.getPlayer().getLocation().getDirection().normalize().multiply(0.050));
 		//event.setCancelled(true);
 	//	event.getPlayer().setFoodLevel(food);
 			 Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Plugin.getPlugin(), new Runnable()
 				{
 					   			
 					   public void run()
 					   {
 						   p.setFoodLevel(food);
 						   
 					   }
 					
 				}, 0L);
 			
 		}
 		
 	}
 	*/
 }
