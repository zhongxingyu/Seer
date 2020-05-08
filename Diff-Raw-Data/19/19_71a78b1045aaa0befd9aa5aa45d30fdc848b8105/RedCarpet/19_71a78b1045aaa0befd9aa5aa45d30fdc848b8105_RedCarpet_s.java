 package anotherDEVer.RedCarpet;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.entity.Player;
 import org.bukkit.Location;
import org.bukkit.BlockChangeDelegate.*;
 import org.bukkit.event.*;
 import org.bukkit.event.player.*;
 import org.bukkit.block.*;
 import org.bukkit.World;
 
 public final class RedCarpet extends JavaPlugin implements Listener
 {
 	public void onEnable()
 	{
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 	
 	@EventHandler
 	public void PlayerMovement(PlayerMoveEvent move)
 	{
 		Player mover = move.getPlayer();
 		
 		Location playerLoc = mover.getLocation();
 		
 		World currentWorld = mover.getWorld();
 		
		int x = (int) playerLoc.getX();
		int y = (int) playerLoc.getY();
		int z = (int) playerLoc.getZ();
 		
 		Block changeBlock = currentWorld.getBlockAt(x, y - 1, z);
 		
		changeBlock.breakNaturally();
 	}
 }
