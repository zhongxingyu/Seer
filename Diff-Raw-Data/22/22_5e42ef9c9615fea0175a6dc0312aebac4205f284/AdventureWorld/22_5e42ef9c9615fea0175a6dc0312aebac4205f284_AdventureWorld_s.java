 package me.Guga.Guga_SERVER_MOD;
 
 import java.util.HashMap;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 public class AdventureWorld 
 {
 	AdventureWorld(Guga_SERVER_MOD gugaSM)
 	{
 		plugin = gugaSM;
 	}
 	public boolean IsAdventureWorld(Location loc)
 	{
 		if (loc.getWorld().getName().matches("world_adventure"))
 			return true;
 		return false;
 	}
 	public void PlayerJoin(Location loc, Player p)
 	{
 		if(enabled)
 		{
 			if (!IsAdventureWorld(p.getLocation()))
 			{
 				baseLocation.put(p.getName(), p.getLocation());
 				InventoryBackup.InventoryClearWrapped(p);
 			}
 			p.teleport(plugin.getServer().getWorld("world_adventure").getSpawnLocation());
 		}
 		else
 			p.sendMessage("Adventure world je vypnuty!");
 	}
 	public void PlayerLeave(Player p)
 	{
 		if (!IsAdventureWorld(p.getLocation()))
 		{
 			p.sendMessage("Tento prikaz jde pouzit pouze v AdventureWorldu!");
 			return;
 		}
 		if (baseLocation.get(p.getName()) != null)
 		{
 			p.teleport(baseLocation.get(p.getName()));
 			baseLocation.remove(p.getName());
 		}
 		else
 		{
 			p.teleport(plugin.getServer().getWorld("world").getSpawnLocation());
 		}
 		InventoryBackup.InventoryReturnWrapped(p, true);
 	}
 	public void togglePvP(Player p)
 	{
 		if(plugin.getServer().getWorld("world_adventure").getPVP())
 		{
 			plugin.getServer().getWorld("world_adventure").setPVP(false);
 			p.sendMessage("PvP for AdventureWorld is disable");
 		}
 		else
 		{
 			plugin.getServer().getWorld("world_adventure").setPVP(true);
 			p.sendMessage("PvP for AdventureWorld is enable");
 		}
 	}
 	public void toggleMobs(Player p)
 	{
 		if(plugin.getServer().getWorld("world_adventure").getAllowAnimals())
 		{
 			plugin.getServer().getWorld("world_adventure").setSpawnFlags(false, false);
 			p.sendMessage("MobSpawns for arena world is disable");
 		}
 		else
 		{
 			plugin.getServer().getWorld("world_adventure").setSpawnFlags(true, true);
 			p.sendMessage("MobSpawns for arena world is enable");
 		}
 	}
 	public void toggleRegion(Player p)
 	{
 		if(region==false)
 		{
 			region=true;
 			p.sendMessage("Region for AdventureWorld is on");	
 		}
 		else
 		{
 			region=false;
 			p.sendMessage("Region for AdventureWorld is off");	
 		}
 	}
 	public void toggleWorld(Player p)
 	{
 		if(!enabled)
 		{
 			enabled=true;
 			p.sendMessage("AdventureWorld is on");	
 		}
 		else
 		{
 			enabled=false;
 			p.sendMessage("AdventureWorld is off");	
 		}
 	}
	public static boolean regionStatus()
 	{
 		return region;
 	}
 	private Guga_SERVER_MOD plugin;
 	private static boolean region = true;
 	private static boolean enabled = false;
 	private HashMap<String,Location> baseLocation = new HashMap<String,Location>();
 }
