 package me.devcom.pdrill;
 
 
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 
 public class Drill {
 	PDrill plugin;
 	
 	boolean linked = false;
 	
 	public Player owner;
 	public Location position;
 	public Block block;
 	public Integer id;
 	public boolean enabled;
 	public JobManager JobMG;
 	public FuelManager FuelMG;
 	public BlockPlaceManager blockPlaceManager = null;
 	
 	public Integer blockCount = 0;
 	public Location forwardDir;
 	public Location backwardDir;
 	public Location rightDir;
 	public Location leftDir;
 	public Location upDir;
 	public Location downDir;
 
 	public final Logger logger = Logger.getLogger("Minecraft");
 	public final String prefix = "[PDrill] ";
 
 	public boolean isVirtual = false;
 	public Fuel virtualFuel = null;
 
 	public LinkDrill parent = null;
 	
 	public Drill(PDrill instance, Player player, Block block, Integer id){
 		plugin = instance;
 		this.owner = player;
 		this.block = block;
 		this.id = id;
 		
 		this.enable();
 		
 		JobMG = new JobManager( this );
 		FuelMG = new FuelManager( this );
 		
 		World world = block.getWorld();
 
 		upDir = new Location(world, 0,1,0);
 		downDir = new Location( world, 0,-1,0);
 		
 		Vector direction = player.getLocation().getDirection();
 		if (direction.getZ() > .5d) {
 			forwardDir = new Location(world, 0, 0, 1);
 			rightDir = new Location(world, -1, 0, 0);
 		} else if (direction.getZ() < -.5d) {
 			forwardDir = new Location(world, 0, 0, -1);
 			rightDir = new Location(world, 1, 0, 0);
 		} else if (direction.getX() > .5d) {
 			forwardDir = new Location(world, 1, 0, 0);
 			rightDir = new Location(world, 0, 0, 1);
 		} else if (direction.getX() < -.5d) {
 			forwardDir = new Location(world, -1, 0, 0);
 			rightDir = new Location(world, 0, 0, -1);
 		} else {
 			forwardDir = new Location(world, 0, 0, -1);
 			rightDir = new Location(world, 1, 0, 0);
 		}
 
 		backwardDir = new Location( world, forwardDir.getBlockX(), forwardDir.getBlockY(), forwardDir.getBlockZ());
 		backwardDir.multiply(-1);
 
 		leftDir = new Location(world, rightDir.getBlockX(), rightDir.getBlockY(), rightDir.getBlockZ());
 		leftDir.multiply(-1);
 
 	}
 	
 	public void update(){
 		JobMG.doJob();
 	}
 
 	public boolean moveInDirection(String direction) {
 		if( FuelMG.getFuelLevel() > 0){
 			Fuel fuel = FuelMG.fuel();
 			
 			
 			Location nextLoc = block.getLocation().add( getDirection(direction) );
 			Block nextBlock = block.getWorld().getBlockAt( nextLoc );
 			dropBlock( nextBlock );
 			nextBlock.setTypeId( Material.FURNACE.getId() );
 			
 			if( blockCount >= fuel.fuelConsumptionBlockCount){
 				FuelMG.consumeFuel( fuel.fuelConsumptionFuelCount );
 				blockCount = 0;
 			}
 			FuelMG.swapFuel( nextBlock );
 			
 			block.setTypeId( 0 );
 			block = nextBlock;
 			
 			if( blockPlaceManager != null ){
 				blockPlaceManager.tick();
 			}
 			
 			blockCount++;
 			return true;
 		}
 		return false;
 	}
 
 	public void dropBlock(Block nextBlock) {
 		if( plugin.configManager.dropItemNaturally){
 			if( nextBlock.getTypeId() != Material.AIR.getId() ){
 				boolean drop = false;
				boolean changed = false;
 				Integer dropId = nextBlock.getTypeId();
 				
 				if(plugin.configManager.drops.containsKey( dropId )){
 					dropId = plugin.configManager.drops.get( dropId );
					changed = true;
 				}
 				
 				if( plugin.configManager.dropItemList.isEmpty() ){
 					drop = true;
 				}else if( plugin.configManager.dropItemList.contains( dropId )){
 					drop = true;
				}else if( changed && !plugin.configManager.checkItemChange ){
					drop = true;
 				}
				
 				if( drop ){
 					ItemStack dropStack = new ItemStack( dropId, 1 );
 	                block.getWorld().dropItemNaturally( nextBlock.getLocation() , dropStack);
 				}
 			}
 		}
 	}
 
 	public Location getDirection(String direction) {
 		Location dirLoc = null;
 		
 		if( direction.equals("f") ){
 			dirLoc = forwardDir;
 		}else if( direction.equals("b") ){
 			dirLoc = backwardDir;
 		}else if( direction.equals("r") ){
 			dirLoc = rightDir;
 		}else if( direction.equals("l") ){
 			dirLoc = leftDir;
 		}else if( direction.equals("u") ){
 			dirLoc = upDir;
 		}else if( direction.equals("d") ){
 			dirLoc = downDir;
 		}
 		
 		return dirLoc;
 	}
 
 	public void disable() {
 		owner.sendMessage(prefix + "Drill deactivated! [" + id +"]" );
 		enabled = false;
 	}
 	
 	public void enable() {
 		owner.sendMessage(prefix + "Drill activated! [" + id +"]" );
 		enabled = true;
 	}
 
 	public Block getNextBlock(String direction) {
 		Block drillBlock = block;
 		Location location = drillBlock.getLocation();
 		World world = drillBlock.getWorld();
 		
 		int x = location.getBlockX();
 		int y = location.getBlockY();
 		int z = location.getBlockZ();
 		
 		Location nextLoc = new Location(world, x, y, z);
 		nextLoc.add( getDirection( direction ));
 		
 		return world.getBlockAt( nextLoc );
 	}
 
 	public boolean canMoveInDirection(String direction) {
 		Location nextLoc = block.getLocation().add( getDirection(direction) );
 		Block nextBlock = block.getWorld().getBlockAt( nextLoc );
 		return !(plugin.configManager.stopblocks.contains( nextBlock.getTypeId() ) || (plugin.drillManager.getDrillFromBlock( nextBlock ) != null));
 	}
 
 	public void updateParentOnBreak() {
 		parent.DrillDB.remove( this );
 		owner.sendMessage( prefix + "Drill ["+ this.id +"] removed from parent [" + -parent.id + "]");
 		
 		parent.checkDatabase();
 	}
 }
