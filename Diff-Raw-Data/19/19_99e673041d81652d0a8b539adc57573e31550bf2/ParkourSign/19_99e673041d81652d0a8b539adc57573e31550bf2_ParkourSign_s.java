 package net.dmulloy2.swornparkour.parkour.objects;
 
 import net.dmulloy2.swornparkour.SwornParkour;
 import net.dmulloy2.swornparkour.parkour.ParkourGame;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 
 public class ParkourSign 
 {
 	private final Location loc;
 	private final ParkourZone pz;
 	private final int id;
 	
 	private final SwornParkour plugin;
 	
 	public ParkourSign(final SwornParkour plugin, final Location loc, final ParkourZone pz, final int id)
 	{
 		this.plugin = plugin;
 		this.loc = loc;
 		this.pz = pz;
 		this.id = id;
 		
 		update();
 	}
 	
 	public final Location getLocation()
 	{
 		return loc;
 	}
 	
 	public final int getId()
 	{
 		return id;
 	}
 	
 	public final ParkourZone getZone()
 	{
 		return pz;
 	}
 	
 	public void update()
 	{
 		Block b = loc.getWorld().getBlockAt(loc);
 		if (b.getState() instanceof Sign)
 		{
 			Sign s = (Sign)b.getState();
 			s.setLine(0, "[SwornParkour]");
 			s.setLine(1, "Click to join");
 			s.setLine(2, "Game " + pz.getId());
 			
 			if (isInGame())
 			{
 				s.setLine(3, "INGAME");
 			}
 			else
 			{
 				s.setLine(3, "AVAILABLE");
 			}
 		}
 		else
 		{
 			plugin.removeSign(this);
 		}
 	}
 	
 	private boolean isInGame()
 	{
 		for (ParkourGame game : plugin.getParkourManager().parkourGames)
 		{
 			if (game.getId() == pz.getId())
 			{
 				return true;
 			}
 		}
 		
 		return false;
 	}
 }
