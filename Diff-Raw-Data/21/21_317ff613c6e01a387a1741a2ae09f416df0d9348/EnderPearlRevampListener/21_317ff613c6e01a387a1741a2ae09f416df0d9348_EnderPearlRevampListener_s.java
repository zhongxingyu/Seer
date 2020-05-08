 package clavus.enderpearlrevamp;
 
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.EnderPearl;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.event.world.WorldSaveEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 
 public class EnderPearlRevampListener implements Listener
 {
 	private EnderPearlRevamp plugin;
 	
 	private HashMap<Player, Long> plTime = new HashMap<Player, Long>();
 	private long interactCooldown = 500; // in milliseconds
 	
 	private HashSet<Byte> transparent = new HashSet<Byte>();
 	
 	public EnderPearlRevampListener(EnderPearlRevamp plugin)
 	{
 		this.plugin = plugin;
 		
 		for( Material mat : Material.values()) {
 			if (mat.isBlock() && !mat.isSolid()) {
 				transparent.add((byte) mat.getId());
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerInteract(PlayerInteractEvent e)
 	{
 		ItemStack item = e.getItem();
 		Player pl = e.getPlayer();
 		
 		if (item != null && item.getType() == Material.ENDER_PEARL && e.getAction() == Action.LEFT_CLICK_BLOCK) {
 			
 			BlockFace face = e.getBlockFace();
 			Block bl = e.getClickedBlock();
 			
			if (MarkerMetaData.isMarkable(bl.getType()) && !bl.isLiquid() && playerCanHit(pl, bl)) {
				if (face == BlockFace.UP) {
					if (!plTime.containsKey(pl) || System.currentTimeMillis() - plTime.get(pl) > interactCooldown) { // prevent spam
						plTime.put(pl, System.currentTimeMillis());
						
 						//plugin.print("Player interacted with block: (" + bl.getTypeId() + ":" + bl.getData() + ") " + bl.getType().toString());
 						plugin.playerMarkBlock(pl, bl);
 					}
 				}
 				else {
					plugin.sendMessageTo(pl, "Touch the top side of the block to mark it!");
 				}
 			}
			else {
				plugin.sendMessageTo(pl, "Cannot mark this spot...");
			}
 		}
 	
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerTeleport(PlayerTeleportEvent e)
 	{
 		if (e.getCause() == TeleportCause.ENDER_PEARL) {
 			e.setCancelled(true);
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onProjectileHit(ProjectileHitEvent e)
 	{
 		Projectile entity = (Projectile) e.getEntity();
 		Entity shooter = entity.getShooter();
 		
 		if (entity instanceof EnderPearl && shooter instanceof Player && !shooter.isDead())
 		{
 			Location loc = entity.getLocation();
             Vector vec = entity.getVelocity().normalize();
             Block bl = loc.getBlock();
             int i = 0;
             
             while(!bl.getType().isSolid() && i < 20) {
             	Location blockLoc = new Location(loc.getWorld(), loc.getX()+(vec.getX()*0.1*i), loc.getY()+(vec.getY()*0.1*i), loc.getZ()+(vec.getZ()*0.1*i));
             	bl = blockLoc.getBlock();
             	i++;
             }
             
             //plugin.print("EnderPearl hit block: (" + bl.getTypeId() + ":" + bl.getData() + ") " + bl.getType().toString());
             boolean success = plugin.playerInitTeleportTo((Player) shooter, bl);
             if (!success) {
             	plugin.dropPearl(loc);
             }
 		}
 		
 	}
 	
 	// if damage was registered while player is in teleport sequence, drop items
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e)
 	{
 		if (e.isCancelled()) { return; }
 		
 		Entity ent = e.getEntity();
 		if (Settings.dropItemsOnDamageWhileSpinning && ent instanceof Player && plugin.isPlayerSpinning((Player) ent)) {
 			plugin.playerDropRandomItems((Player) ent);
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onWorldSaveEvent(WorldSaveEvent e)
 	{
 		plugin.savePlayerData();
 	}
 	
 	// Helpers
 	
 	private boolean playerCanHit(Player pl, Block hitBlock)
 	{
 		Block hitCBlock = pl.getTargetBlock(transparent, 5);
 		// TODO: check for opened door
 		return hitCBlock.getLocation().equals(hitBlock.getLocation());
 	}
 	
 }
