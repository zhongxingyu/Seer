 package net.preoccupied.bukkit.dispenser;
 
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockDispenseEvent;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.EventExecutor;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import net.preoccupied.bukkit.PluginConfiguration;
 
 
 /**
    Improved Dispensers plugin for Bukkit.
 
    @author Christopher O'Brien <cobrien@gmail.com>
  */
 public class DispenserPlugin extends JavaPlugin {
 
 
     private Set<Block> flowingDispensers = null;
 
 
     private Material waterType = Material.WATER_BUCKET;
     private Material lavaType = Material.LAVA_BUCKET;
 
     private Material fireType = Material.FLINT_AND_STEEL;
     private Material lightningType = Material.GOLD_BOOTS;
     private Material slimeType = Material.SLIME_BALL;
     private Material boatType = Material.BOAT;
     private Material cartType = Material.MINECART;
 
 
 
     public void onLoad() {
 	flowingDispensers = new HashSet<Block>();
 
 	// if you're not using the bukkit-utils module, you can
 	// comment the rest of this method out, but you won't be able
 	// to override the default item types via config.yml
 
 	try {
 	    Configuration conf = PluginConfiguration.load(this, this.getFile(), "config.yml");
 
 	    // sustained effects
 	    waterType = Material.getMaterial(conf.getInt("dispenser.water-type", waterType.getId()));
 	    lavaType = Material.getMaterial(conf.getInt("dispenser.lava-type", lavaType.getId()));
 
 	    // instant effects
 	    fireType = Material.getMaterial(conf.getInt("dispenser.fire-type", fireType.getId()));
 	    lightningType = Material.getMaterial(conf.getInt("dispenser.lightning-type", lightningType.getId()));
 	    slimeType = Material.getMaterial(conf.getInt("dispenser.slime-type", slimeType.getId()));
 	    boatType = Material.getMaterial(conf.getInt("dispenser.boat-type", boatType.getId()));
 	    cartType = Material.getMaterial(conf.getInt("dispenser.minecart-type", cartType.getId()));
 
 	} catch(IOException ioe) {
 	    System.out.println(ioe);
 	    ioe.printStackTrace();
 	    return;
 	}
     }
 
 
 
     public void onEnable() {
 	PluginManager pm = getServer().getPluginManager();
 	EventExecutor ee;
 
 	ee = new EventExecutor() {
 		public void execute(Listener ignored, Event e) {
 		    onBlockRedstoneChange((BlockRedstoneEvent) e);
 		}
 	    };
 	pm.registerEvent(Event.Type.REDSTONE_CHANGE, null, ee, Priority.Low, this);
 
 	ee = new EventExecutor() {
 		public void execute(Listener ignored, Event e) {
 		    onBlockDispense((BlockDispenseEvent) e);
 		}
 	    };
 	pm.registerEvent(Event.Type.BLOCK_DISPENSE, null, ee, Priority.Low, this);
     }
 
 
 
     public void onDisable() {
 	for(Block b : flowingDispensers) {
 	    Block t = getFacingBlock(b);
 	    t.setTypeId(0, true);
 	}
 	flowingDispensers.clear();
     }
 
 
 
     private void onBlockRedstoneChange(BlockRedstoneEvent e) {

	//debugging an oddity that turned out to be redstone repeaters
	//not emitting a redstone change event, but still providing
	//current. If you power a dispenser directly off of a
	//repeater, it won't shut off for you currently.

	//System.out.println("REDSTONE " + e.getOldCurrent() + " -> " + e.getNewCurrent());
	
 	// if the change is from powered to unpowered
 	if(e.getOldCurrent() > 0 && e.getNewCurrent() == 0) {

	    // check our powered dispensers, if any
	    for(Block b : flowingDispensers) {

		//System.out.println("checking " + b);
		//System.out.println("b.isBlockPowered() == " + b.isBlockPowered());
		//System.out.println("b.isBlockIndirectlyPowered() == " + b.isBlockIndirectlyPowered());

 		// if one of our powered dispensers is no longer
 		// powered, shut down the flow
 		if(! (b.isBlockPowered() || b.isBlockIndirectlyPowered())) {
 		    Block t = getFacingBlock(b);
 		    safeSetBlockType(t, Material.AIR);
		    flowingDispensers.remove(b);
 		}
 	    }
 	}
     }
 
 
 
     /**
        check that the spew type is enabled (by not being AIR) and
        matches the material being spewed
      */
     private static boolean spewCheck(Material spewed, Material check) {
 	return (check != Material.AIR && spewed == check);
     }
 
 
 
     private void onBlockDispense(BlockDispenseEvent e) {
 	if(e.isCancelled())
 	    return;
 
 	Material spewType = e.getItem().getType();
 	boolean cancel = false;
 
 	if(spewCheck(spewType, slimeType)) {
 	    if(spewSlime(e.getBlock())) {
 		// we change it to a snowball because we want to consume the item,
 		// but by default a slimeball would just plop out.
 		// TODO: use inventory editing and cancel the event.
 		e.setItem(new ItemStack(Material.SNOW_BALL, 1));
 	    }
 	    
 	} else if(spewCheck(spewType, lightningType)) {
 	    if(spewLightning(e.getBlock())) {
 		cancel = true;
 	    }
 
 	} else if(spewCheck(spewType, fireType)) {
 	    if(spewFire(e.getBlock())) {
 		cancel = true;
 	    }
 
 	} else if(spewCheck(spewType, boatType)) {
 	    if(spewBoat(e.getBlock())) {
 		// TODO: use inventory editing and cancel the event.
 		e.setItem(new ItemStack(Material.SNOW_BALL, 1));
 	    }
 
 	} else if(spewCheck(spewType, cartType)) {
 	    if(spewMinecart(e.getBlock())) {
 		// TODO: use inventory editing and cancel the event.
 		e.setItem(new ItemStack(Material.SNOW_BALL, 1));
 	    }
 
 	} else if(spewCheck(spewType, waterType)) {
 	    if(spewWater(e.getBlock())) {
 		cancel = true;
 	    }
 	    
 	} else if(spewCheck(spewType, lavaType)) {
 	    if(spewLava(e.getBlock())) {
 		cancel = true;
 	    }
 	}
 
 	// canceling this event means that the item is NOT consumed,
 	// and nothing is plopped out automatically. So for non-consumable
 	// triggers, such cancel should be true.
 	e.setCancelled(cancel);
     }
 
 
 
     private void safeSetBlockType(final Block block, final Material material) {
 	Runnable task = new Runnable() {
 		public void run() {
 		    block.setType(material);
 		}
 	    };
 
 	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, task);
     }
 
 
 
     /**
        Determine the Block that is count away from the face of the
        Dispenser b.
      */
     private static Block getFacingBlock(Block b, int count) {
 	switch(b.getData()) {
 	case 2:
 	    return b.getFace(BlockFace.EAST, count);
 	case 3:
 	    return b.getFace(BlockFace.WEST, count);
 	case 4:
 	    return b.getFace(BlockFace.NORTH, count);
 	case 5:
 	    return b.getFace(BlockFace.SOUTH, count);
 	default:
 	    return b.getFace(BlockFace.UP, count);
 	}
     }
     
 
 
     private static Block getFacingBlock(Block b) {
 	return getFacingBlock(b, 1);
     }
     
 
     
     /**
        Spawns a slime creature
     */
     public boolean spewSlime(Block b) {
 	b = getFacingBlock(b, 2);
 	
 	if(! isMaterialOpen(b.getType()))
 	    return false;
 	
 	b.getWorld().spawnCreature(b.getLocation(), CreatureType.SLIME);
 	return true;
     }
     
 
     
     /**
        Causes a lightning strike
     */
     public boolean spewLightning(Block b) {
 	b.getWorld().strikeLightning(b.getLocation());
 	return true;
     }
 
 
 
     /**
        Lights a fire. Uses the BlockIgniteEvent check to make sure
        fire is permitted.
     */
     public boolean spewFire(Block b) {
 	b = getFacingBlock(b);
 	
 	if(! isMaterialOpen(b.getType()))
 	    return false;
 
 	IgniteCause cause = BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL;
 	BlockIgniteEvent event = new BlockIgniteEvent(b, cause, null);
 	getServer().getPluginManager().callEvent(event);
 	
 	if(! event.isCancelled()) {
 	    safeSetBlockType(b, Material.FIRE);
 	}
 
 	return true;
     }
 
 
 
     /**
        Spawns a boat entity
      */
     public boolean spewBoat(Block b) {
 	b = getFacingBlock(b);
 
 	switch(b.getType()) {
 	case AIR:
 	case WATER:
 	    b.getWorld().spawnBoat(b.getLocation());
 	    return true;
 
 	default:
 	    return false;
 	}
     }
 
 
 
     /**
        Spawns a minecart entity
      */
     public boolean spewMinecart(Block b) {
 	b = getFacingBlock(b);
 	
 	switch(b.getType()) {
 	case AIR:
 	case RAILS:
 	case POWERED_RAIL:
 	case DETECTOR_RAIL:
 	    b.getWorld().spawnMinecart(b.getLocation());
 	    return true;
 
 	default:
 	    return false;
 	}
     }
 
 
 
     /**
        Starts a water flow
      */
     public boolean spewWater(Block b) {
 	Block t = getFacingBlock(b);
 
 	if(! isMaterialOpen(t.getType()))
 	    return false;
 	
 	safeSetBlockType(t, Material.WATER);
 	flowingDispensers.add(b);
 	return true;
     }
 
 
 
     /**
        Starts a lava flow
     */
     public boolean spewLava(Block b) {
 	Block t = getFacingBlock(b);
 
 	if(! isMaterialOpen(t.getType()))
 	    return false;
 	
 	safeSetBlockType(t, Material.LAVA);
 	flowingDispensers.add(b);
 	return true;
     }
 
     
     
     /**
        Check if the material is considered "open" such that you could
        reasonably expect something to spawn there. Non obtrusive blocks.
      */
     private static boolean isMaterialOpen(Material m) {
 	switch(m) {
 	case AIR:
 	case BROWN_MUSHROOM:
 	case CAKE:
 	case DETECTOR_RAIL:
 	case FIRE:
 	case GRASS:
 	case LAVA:
 	case POWERED_RAIL:
 	case RAILS:
 	case RED_MUSHROOM:
 	case RED_ROSE:
 	case REDSTONE_WIRE:
 	case REDSTONE_TORCH_OFF:
 	case REDSTONE_TORCH_ON:
 	case SNOW:
 	case TORCH:
 	case WATER:
 	case YELLOW_FLOWER:
 	    return true;
 	    
 	default:
 	    return false;
 	}
     }
 
 }
 
 
 /* The end. */
