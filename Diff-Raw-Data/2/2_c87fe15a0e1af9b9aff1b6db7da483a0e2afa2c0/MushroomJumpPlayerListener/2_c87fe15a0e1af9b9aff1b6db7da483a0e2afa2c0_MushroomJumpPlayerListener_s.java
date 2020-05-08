 package me.FieldZ.MushroomJump;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Dispenser;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.util.Vector;
 
 public class MushroomJumpPlayerListener extends PlayerListener{
 	public MushroomJump plugin;
 	/**
 	 * Constructor for PlayerListener
 	 * @param instance Grabs an instance of MushroomJump
 	 */
 	public MushroomJumpPlayerListener(MushroomJump instance){
 		plugin = instance;
 	}
 
 	/**
 	 * Calls when player moves
 	 * If a player moves on to a mushroom block he is catapulted up in the air! 
 	 * @param ev A PlayerMoveEvent object
 	 */
 	@Override
 	public void onPlayerMove(PlayerMoveEvent ev){
 		Player player = ev.getPlayer();
 		
		if(ev.isCancelled()||ev.getFrom().getBlock().getLocation()==ev.getFrom().getBlock().getLocation())
 		    return;
 		
 		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
 		
 		if(block.getType() == Material.HUGE_MUSHROOM_1){
 			Vector dir = player.getLocation().getDirection().multiply(1.75);
 			Vector vec = new Vector(dir.getX(), 1.5D, dir.getZ());
 			player.setVelocity(vec);
 		}
 		if(block.getType() == Material.HUGE_MUSHROOM_2){
 			Vector dir = player.getLocation().getDirection().multiply(1.75);
 			Vector vec = new Vector(dir.getX(), 2.0D, dir.getZ());
 			player.setVelocity(vec);
 		}
 		
 	}
 
 }
