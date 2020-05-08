 package com.em.fan;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Chunk;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Item;
 import org.bukkit.util.Vector;
 
 public class FanBlock {
 
 	Fan thePlugin;
 
 	private Location location;
 	public int getPower() {
 		return power;
 	}
 
 	public void setPower(int power) {
 		this.power = power;
 	}
 
 	public Location getLocation() {
 		return location;
 	}
 
 	public BlockFace getFace() {
 		return face;
 	}
 
 	private BlockFace face;
 	private int power;
 
 	/**
 	 * Create a new Fan
 	 * 
 	 * @param block
 	 *          the block
 	 * @param face
 	 *          the face
 	 */
 	public FanBlock(Block block, Fan thePlugin) {
 		this.thePlugin = thePlugin;
 		this.location = block.getLocation();
 		this.power = block.getBlockPower();
 		this.face = getFace(block);
 		setFace(block, face);
 	}
 
 	/**
 	 * Just blow items
 	 */
 	public void blow() {
 
 		int range = thePlugin.fanRange;
 
 		if (getPower() > 0) {
 
 			int dx = getBlowingDirectionX();
 			int dy = getBlowingDirectionY();
 			int dz = getBlowingDirectionZ();
 
 			Location pLocation = getLocation().clone();
 			pLocation = pLocation.add(0.5D, 0.5D, 0.5D);
 
 			for (int it = 0; it < range; it++) {
 				pLocation = pLocation.add(dx, dy, dz);
 				if (!isNotBlockingBlock(pLocation)) {
 					break;
 				}
 
 				List<Entity> entities = getEntitiesAtLocation(pLocation);
 				for (Entity entity : entities) {
 					if (entity instanceof Item) {
 						pushEntity(entity, dx, dy, dz);
 					}
 				}
 			}
 
 			final FanBlock fb = this;
 			this.thePlugin.getServer().getScheduler().scheduleSyncDelayedTask(this.thePlugin, new Runnable() {
 				
 				public void run() {
 					fb.blow();
 				}
 			}, 3L);
 
 		}
 		
 
 	}
 
 	private void pushEntity(Entity entity, int dx, int dy, int dz) {
 
 		double maxspeed = 0.4;
 		double boost = 0.15;
 		Vector motion = entity.getVelocity();
 		if (Math.abs(dx) != 0) {
 			if (motion.getX() * dx < 0) {
 				motion.setX(0);
 			}
 			if (motion.getX() * dx <= maxspeed) {
 				motion.setX(motion.getX() + dx * boost);
 			}
 		}
 		if (Math.abs(dy) != 0) {
 			if (motion.getY() * dy < 0) {
 				motion.setY(0);
 			}
 			if (dy > 0) {
 				boost *= 0.5;
 			}
 			if (motion.getY() * dy <= maxspeed) {
 				motion.setY(motion.getY() + dy * boost);
 			}
 		}
 		if (Math.abs(dz) != 0) {
 			if (motion.getZ() * dz < 0) {
 				motion.setZ(0);
 			}
 			if (motion.getZ() * dz <= maxspeed) {
 				motion.setZ(motion.getZ() + dz * boost);
 			}
 		}
 		entity.setVelocity(motion);
 	}
 
 	/**
 	 * Utilities to get Dropped entities at a Location
 	 * 
 	 * @param inputLocation
 	 * @param world
 	 * @return
 	 */
 	private List<Entity> getEntitiesAtLocation(Location inputLocation) {
 		//double d = 0.85D;
 
 		List<Entity> entities = new ArrayList<Entity>();
 		Chunk chunk = inputLocation.getBlock().getChunk();
 		if (chunk.isLoaded()) {
 			Entity[] cEntities = chunk.getEntities();
 			
 			 for(Entity  ent:cEntities)
 		        {
 		            if(ent.getLocation().getBlock().equals(inputLocation.getBlock()))
 		            {
 		            	entities.add(ent);
 		            }
 		        }
 		        
 			// for (int i = 0; i < cEntities.length; i++) {
 			// Location location = cEntities[i].getLocation();
 			// // compare
 			// if ((location.getX() < inputLocation.getX() - d) ||
 			// (location.getX() > inputLocation.getX() + d)) {
 			// continue;
 			// }
 			// if ((location.getY() < inputLocation.getY() - d) ||
 			// (location.getY() > inputLocation.getY() + d)) {
 			// continue;
 			// }
 			// if ((location.getZ() < inputLocation.getZ() - d) ||
 			// (location.getZ() > inputLocation.getZ() + d)) {
 			// continue;
 			// }
 			// // it's Ok, add it
 			// entities.add(cEntities[i]);
 			// }
 		}
 
 		return entities;
 	}
 
 	/**
 	 * Get face from Block (it's strange to redefine it...)
 	 * 
 	 * @param block
 	 * @return
 	 */
 	private BlockFace getFace(Block block) {
 		BlockFace face;
 		switch (block.getData()) {
 		case 0:
 		default:
 			face = BlockFace.SOUTH;
 			break;
 		case 1:
 			face = BlockFace.WEST;
 			break;
 		case 2:
 			face = BlockFace.NORTH;
 			break;
 		case 3:
 			face = BlockFace.EAST;
 			break;
 
 		}
 		// face = ((Directional) block.getState().getData()).getFacing();
 		return face;
 	}
 
 	/**
 	 * Set face to Block (it's strange to redefine it...)
 	 * 
 	 * @param block
 	 * @return
 	 */
 	private void setFace(Block block, BlockFace face) {
 
 		switch (face) {
 		case SOUTH:
 		default:
 			block.setData((byte) 0);
 			break;
 		case WEST:
 			block.setData((byte) 1);
 			break;
 		case NORTH:
 			block.setData((byte) 2);
 			break;
 		case EAST:
 			block.setData((byte) 3);
 			break;
 		case UP:
 			block.setData((byte) 4);
 			break;
 		case DOWN:
 			block.setData((byte) 5);
 			break;
 
 		}
 	}
 	
 	/**
 	 * Returns blowing direction along the x-axis (-1, 0, 1)
 	 */
 	public int getBlowingDirectionX() {
 		int dx = 0;
 		switch (face) {
 		case EAST:
 			dx = 1;
 			break;
 		case WEST:
 			dx = -1;
 			break;
 		default:
 			break;
 		}
 
 		return dx;
 	}
 
 	/**
 	 * Returns blowing direction along the y-axis (-1, 0, 1)
 	 */
 	public int getBlowingDirectionY() {
 		int dy = 0;
 		switch (face) {
 		case UP:
 			dy = 1;
 			break;
 		case DOWN:
 			dy = -1;
 			break;
 		default:
 			break;
 		}
 
 		return dy;
 	}
 
 	/**
 	 * Returns blowing direction along the z-axis (-1, 0, 1)
 	 */
 	public int getBlowingDirectionZ() {
 		int dz = 0;
 		switch (face) {
 		case SOUTH:
 			dz = 1;
 			break;
 		case NORTH:
 			dz = -1;
 			break;
 		default:
 			break;
 		}
 
 		return dz;
 	}
 
 	/**
 	 * Is the block at position px, py, pz a blocking block for this fan (yes, it depend from facing)
 	 * @param px
 	 * @param py
 	 * @param pz
 	 * @return
 	 */
 	public boolean isNotBlockingBlock(Location location) {
 		Block block = location.getBlock();
 		switch (face) {
 		case UP:
 		case DOWN:
 			return NOT_BLOCKING_VERTICAL.contains((byte)block.getTypeId());
 		default:
 			return NOT_BLOCKING_HORIZONTAL.contains((byte)block.getTypeId());
 		}
 	}
 
 	private static final List<Byte> NOT_BLOCKING_HORIZONTAL = new ArrayList<Byte>();
 	private static final List<Byte> NOT_BLOCKING_VERTICAL = new ArrayList<Byte>();
 	static {
 		NOT_BLOCKING_HORIZONTAL.add((byte)Material.AIR.getId());
 		NOT_BLOCKING_HORIZONTAL.add((byte)Material.DETECTOR_RAIL.getId());
 		NOT_BLOCKING_HORIZONTAL.add((byte)Material.POWERED_RAIL.getId());
 		NOT_BLOCKING_HORIZONTAL.add((byte)Material.RAILS.getId());
 		NOT_BLOCKING_HORIZONTAL.add((byte)Material.REDSTONE_WIRE.getId());
 		NOT_BLOCKING_HORIZONTAL.add((byte)Material.WOOD_PLATE.getId());
 		NOT_BLOCKING_HORIZONTAL.add((byte)Material.STONE_PLATE.getId());
 
 		NOT_BLOCKING_VERTICAL.add((byte)Material.AIR.getId());
 		NOT_BLOCKING_VERTICAL.add((byte)Material.FENCE.getId());
 		NOT_BLOCKING_VERTICAL.add((byte)Material.FENCE_GATE.getId());
 		NOT_BLOCKING_VERTICAL.add((byte)Material.REDSTONE_WIRE.getId());
 		NOT_BLOCKING_VERTICAL.add((byte)Material.TORCH.getId());
 
 	}
 	
 }
