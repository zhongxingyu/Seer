 package net.Zelif.DeathTimeout;
 
 import java.util.HashMap;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 
 public class DeathTListener implements Listener
 {
 	public static HashMap<String, Long> BanID= new HashMap<String, Long>();
 	DeathTimeout plugin;
 	public DeathTListener(DeathTimeout instance)
 	{
 		plugin = instance;
     }
 
 	public boolean checkBan(PlayerLoginEvent event , Player p )
 	{
 		if(BanID.containsKey(p.getName().toLowerCase()))
 		{
 			long tempTime = BanID.get(p.getName().toLowerCase());
 			long now = System.currentTimeMillis();
 			long diff = tempTime - now;
 			if(diff <= 0)
 			{
 				return false;
 			}
 			else
 			{
 				String caltime = ((diff/1000)/60+"mins "+(diff/1000)% 60+"sec");
 				String timeleft = plugin.getConfig().getString("Kickrejoin").replace("time", caltime);
 				event.setKickMessage(timeleft);
 				return true;
 			}	
 		}
 		return false;
 	}
 			
     @EventHandler(priority = EventPriority.HIGH)
     public void onPlayerLogin(PlayerLoginEvent event)
     {
 		Player p = event.getPlayer();
     	if(BanID.get(p.getName().toLowerCase()) != null)
     	{
     		if (checkBan(event, p)==true)
     		{
     			event.setResult(Result.KICK_OTHER);
     		}
     		else
     		{
     			BanID.remove(p.getName().toLowerCase());
     		}
     	}
 	}
 	
     @EventHandler(priority = EventPriority.HIGH)
 	public void onEntityDeathEvent(EntityDeathEvent event)
     {
     	if (event.getEntity() instanceof Player)
     	{
     		Player pl = (Player)event.getEntity();
    		if(!(pl.hasPermission("deathtimeout.bypass"))){ 									//bypass the ban if user has this node
    	    	pl.getInventory().clear();												//Removes all inventory items ;D
     			int banTime = plugin.getConfig().getInt("SecondsBanned");							//Grabs the amount of seconds to be banned from config
    			long tempT1 = System.currentTimeMillis()+(banTime*1000); 							//Finds the current time in milliseconds
    			BanID.put(pl.getName().toLowerCase(),tempT1); 									//Uses a hashmap with the time from tempT1
     			String timecal = (banTime/60+"mins "+banTime%60+"sec"); 							//Calculates the mins and seconds and sets to a string
    			String timevar = plugin.getConfig().getString("Kickmsg").replace("time", timecal ); 				//Replaces the word time with the timecal variable
    			pl.kickPlayer(timevar); 											//kicks player with the msg from timeval
     		}
     	}
     }
 }
