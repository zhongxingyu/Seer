 package me.bibo38.Register;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 
 public class InteractListener implements Listener
 {	
 	public boolean interact(Player player) // Die Haupt Interagier-Methode
 	{
 		if(Register.hasPerm(player.getName()))
 		{
 			return true;
 		} else
 		{
 			return false;
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent evt)
 	{
 		if(!interact(evt.getPlayer())) // Nicht Interagieren
 		{
 			evt.setCancelled(true);
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerInteractEntity(PlayerInteractEntityEvent evt)
 	{
 		if(!interact(evt.getPlayer())) // Nicht Interagieren
 		{
 			evt.setCancelled(true);
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerBlockBreakEvent(BlockBreakEvent evt)
 	{
 		if(!interact(evt.getPlayer())) // Nicht Interagieren
 		{
 			evt.setCancelled(true);
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerBlockPlace(BlockPlaceEvent evt)
 	{
 		if(!interact(evt.getPlayer())) // Nicht Interagieren
 		{
 			evt.setCancelled(true);
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerDamage(EntityDamageEvent evt)
 	{
 		if(evt.getEntity() instanceof Player)
 		{
 			if(!interact((Player) evt.getEntity())) // Nicht Interagieren
 			{
 				evt.setCancelled(true);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerPickupItem(PlayerPickupItemEvent evt)
 	{
 		if(!interact(evt.getPlayer())) // Nicht Interagieren
 		{
 			evt.setCancelled(true);
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerDamageByPlayer(EntityDamageByEntityEvent evt)
 	{
 		if(evt.getDamager() instanceof Player)
 		{
 			if(!interact((Player) evt.getDamager())) // Nicht Interagieren
 			{
 				evt.setCancelled(true);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onEntityTarget(EntityTargetEvent evt)
 	{
 		if(evt.getTarget() instanceof Player)
 		{
 			if(!interact((Player) evt.getTarget())) // Nicht Interagieren
 			{
 				evt.setCancelled(true);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerChat(PlayerChatEvent evt)
 	{
 		String msg = evt.getMessage().toLowerCase();
 		String pwd = Register.getCfg().getString("password").toLowerCase();
 		int pos;
		if((pos = msg.indexOf(pwd)) >= 0)
 		{
 			// Password kommt vor
 			msg = msg.substring(0, pos) + Register.getCfg().getString("replace-pw") + msg.substring(pos + pwd.length());
			evt.setMessage(msg);
 		}
 	}
 }
