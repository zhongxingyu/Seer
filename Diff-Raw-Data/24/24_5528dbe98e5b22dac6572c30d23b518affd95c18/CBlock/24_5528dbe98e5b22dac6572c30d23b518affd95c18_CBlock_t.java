 package net.servfire.hellfire.bukkit.ControllerBlock;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.RedstoneWire;
 
 public class CBlock {
 	private Location blockLocation = null;
 	private Material blockType = null;
 	private List<BlockDesc> placedBlocks = new ArrayList<BlockDesc>();
 	private String owner = null;
 	
 	private ControllerBlock parent = null;
 	private boolean on = false;
 	private boolean edit = false;
 	public byte protectedLevel = 0; // 0 is fully, 1 partially and 2 unprotected
 	
 	public CBlock(ControllerBlock p, Location l, String o, byte pl) {
 		parent = p;
 		blockLocation = l;
 		owner = o;
 		protectedLevel = pl;
 	}
 	
 	public ControllerBlock getParent() {
 		return parent;
 	}
 	
 	public String getOwner() {
 		return owner;
 	}
 	
 	public Material getType() {
 		return blockType;
 	}
 	
 	public void setType(Material m) {
 		blockType = m;
 	}
 	
 	public Location getLoc() {
 		return blockLocation;
 	}
 	
 	public Iterator<BlockDesc> getBlocks() {
 		return placedBlocks.iterator();
 	}
 	
 	public boolean addBlock(Block b) {
 		if (b.getType().equals(blockType)) {
 			Location bloc = b.getLocation();
 							
 			if (placedBlocks.size() == 0) {
 				placedBlocks.add(new BlockDesc(bloc, b.getData()));
 				return true;
 			} else {
 				ListIterator<BlockDesc> i = placedBlocks.listIterator();
 				while (i.hasNext()) {
 					BlockDesc loc = i.next();
 					if (bloc.getBlockY() > loc.blockLoc.getBlockY()) {
 						i.previous();
 						i.add(new BlockDesc(bloc, b.getData()));
 						return true;
 					}
 				}
 				placedBlocks.add(new BlockDesc(bloc, b.getData()));
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean delBlock(Block b) {
 		Location u = b.getLocation();
 		for (Iterator<BlockDesc> i = placedBlocks.iterator(); i.hasNext(); ) {
 			Location t = i.next().blockLoc;
 			if (t.equals(u)) {
 				i.remove();
 				CBlock check = parent.getControllerBlockFor(this, u, null, true);
 				if (check != null) {
 					b.setType(check.blockType);
 					b.setData(check.getBlock(u).blockData);
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public int numBlocks() {
 		return placedBlocks.size();
 	}
 	
 	public BlockDesc getBlock(Location l) {
 		Iterator<BlockDesc> i = placedBlocks.iterator();
 		while (i.hasNext()) {
 			BlockDesc d = i.next();
 			if (d.blockLoc.equals(l)) return d;
 		}
 		return null;
 	}
 	
 	public boolean hasBlock(Location l) {
 		if (getBlock(l) != null) return true;
 		return false;
 	}
 	
 	public void updateBlock(Block b) {
 		Iterator<BlockDesc> i = placedBlocks.iterator();
 		while (i.hasNext()) {
 			BlockDesc d = i.next();
 			if (d.blockLoc.equals(b.getLocation())) {
 				d.blockData = b.getState().getData().getData();
 				return;
 			}
 		}
 	}
 	
 	public boolean isBeingEdited() {
 		return edit;
 	}
 	
 	public void editBlock(boolean b) {
 		edit = b;
 		if (edit) {
 			this.turnOn();
 		} else {
 			parent.saveData();
 			doRedstoneCheck();
 		}
 	}
 	
 	public void destroy() {
 		this.turnOff();
 		int i = placedBlocks.size();
 		int j = 0;
 		while (i > 0) {
 			if (i > 64) {
 				j = 64;
 				i -= 64;
 			} else {
 				j = i;
 				i -= i;
 			}
 			blockLocation.getWorld().dropItemNaturally(blockLocation, new ItemStack(blockType, j));
 		}
 	}
 	
 	public boolean isOn() {
 		return on;
 	}
 	
 	public void doRedstoneCheck() {
 		Block check = Util.getBlockAtLocation(blockLocation).getRelative(BlockFace.UP);
 		doRedstoneCheck(check.getState());
 	}
 	
 	public void doRedstoneCheck(BlockState s) { // quickRedstoneCheck enabled check
 		if (isBeingEdited()) return;
 		if (s.getType().equals(Material.REDSTONE_TORCH_ON)) {
 			this.turnOff();
 		} else if (s.getType().equals(Material.REDSTONE_TORCH_OFF)){
 			this.turnOn();
 		} else if (s.getType().equals(Material.REDSTONE_WIRE)) {
 			if (((RedstoneWire) s.getData()).isPowered()) {
 				this.turnOff();
 			} else {
 				this.turnOn();
 			}
 		} else if (s.getType().equals(Material.AIR)){
 			this.turnOn();
 		}
 	}
 	
 	public void turnOff() {
 		Iterator<BlockDesc> i = placedBlocks.iterator();
 		while (i.hasNext()) {
 			BlockDesc d = i.next();
 			Location loc = d.blockLoc;
 			CBlock check = parent.getControllerBlockFor(this, loc, null, true);
 			Block cur = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
 			if (check != null) {
 				cur.setType(check.blockType);
 				cur.setData(check.getBlock(loc).blockData);
 			} else if (protectedLevel == 0) {
 				cur.setType(Material.AIR);
 			}
 		}
 		on = false;
 	}
 	
 	public void turnOn() {
 		for (Iterator<BlockDesc> i = placedBlocks.iterator(); i.hasNext(); ) {
 			BlockDesc b = i.next();
 			Location loc = b.blockLoc;
 			Block cur = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
 			cur.setType(blockType);	
 			cur.setData(b.blockData);
 		}
 		on = true;
 	}
 	
 	public void turnOn(Location l) {
 		Iterator<BlockDesc> i = placedBlocks.iterator();
 		while (i.hasNext()) {
 			BlockDesc b = i.next();
 			if (l.equals(b.blockLoc)) {
 				Block cur = Util.getBlockAtLocation(l);
 				cur.setType(blockType);
 				cur.setData(b.blockData);
 			}
 		}
 	}
 	
 	/*
 	 * Constructor for loading up from file
 	 * If changing the serialisation structure, we need to change the loading code here
 	 * String s is the string we should have saved out to file when our serialize() function is called
 	 */
 	public CBlock(ControllerBlock p, int version, String s) {
 		parent = p;
 		String args[] = s.split(",");
 		
 		if (
 				version < 3 && args.length < 4 ||
 				version >= 3 && args.length < 5
 				) {
 			parent.log.severe("ERROR: Invalid ControllerBlock description in data file, skipping");
 			return;
 		}
 		
 		// Version 4 switched to using the world name (portability, and required to load worlds)
 		if (version >= 4) {
 			blockLocation = parseLocation(p.getServer(), args[0], args[1], args[2], args[3]);
 			parent.log.debug("CB Location: " + Util.formatLocation(blockLocation));
 		} else {
 			blockLocation = oldParseLocation(p.getServer(), args[0], args[1], args[2], args[3]);
 		}
 		
 		blockType = Material.getMaterial(args[4]);
 		
 		// Version 3 introduced owners
 		int i;
 		if (version >= 3) {
 			owner = args[5];
 			i = 6;
 		} else {
 			owner = null;
 			i = 5;
 		}
 		
 		// Assume protected (keeps compatibility with older version save files)
 		protectedLevel = 0;
		if (i < args.length){ // CBs with no controlled blocks die without this
			if (args[i].equals("protected")) {
				protectedLevel = 0;
				i++;
			}
			if (args[i].equals("semi-protected")) {
				protectedLevel = 1;
				i++;
			}
			else if (args[i].equals("unprotected")) {
				protectedLevel = 2;
				i++;
			}
 		}
 		
 		while (i < args.length) {
 			if (version == 1) {
 				if (args.length - i >= 4) {
 					placedBlocks.add(new BlockDesc(oldParseLocation(p.getServer(), args[i++], args[i++], args[i++], args[i++]), (byte) 0));
 				} else {
 					parent.log.severe("ERROR: Block description in save file is corrupt");
 					return;
 				}
 				
 			} else if (version >= 2 && version <= 3) {
 				if (args.length - i >= 5) {
 					placedBlocks.add(new BlockDesc(oldParseLocation(p.getServer(), args[i++], args[i++], args[i++], args[i++]), Byte.parseByte(args[i++])));
 				} else {
 					parent.log.severe("ERROR: Block description in save file is corrupt");
 					return;
 				}
 			
 			} else if (version >= 4) {
 				if (args.length - i >= 5) {
 					placedBlocks.add(new BlockDesc(parseLocation(p.getServer(), args[i++], args[i++], args[i++], args[i++]), Byte.parseByte(args[i++])));
 				} else {
 					parent.log.severe("ERROR: Block description in save file is corrupt");
 					return;
 				}
 			}
 		}
 	}
 	
 	/*
 	 * Converts the CBlock into a tight string format, CSV split in the format:
 	 * CBlockX,CBlockY,CBlockZ,BlockType,Owner,Loc1X,Loc1Y,Loc1Z,Loc2X,Loc2X,Loc2X,LocNX,LocNY,LocNZ,etc
 	 */
 	public String serialize() {
 		String result = loc2str(blockLocation);
 		result += "," + blockType;
 		result += "," + owner;
 		// Loading code assumes protected, only add something if it's otherwise
 		if (protectedLevel == 1) {
 			result += ",semi-protected";
 		} else if (protectedLevel == 2) {
 			result += ",unprotected";
 		}
 		Iterator<BlockDesc> i = placedBlocks.iterator();
 		while (i.hasNext()) {
 			BlockDesc b = i.next();
 			result += "," + loc2str(b.blockLoc);
 			result += "," + Byte.toString(b.blockData);
 		}
 		return result;
 	}
 	
 	/*
 	 * Converts the string inputs into a new Location object
 	 */
 	public Location parseLocation(Server server, String worldName, String X, String Y, String Z) {
 		return new Location(server.getWorld(worldName), Integer.parseInt(X), Integer.parseInt(Y), Integer.parseInt(Z));
 	}
 	/*
 	 * Old parseLocation function for <= v3 data files (used the worldID)
 	 * 
 	 * TODO: World names are supposed to be used instead, remove this later
 	 */
 	public Location oldParseLocation(Server server, String worldId, String X, String Y, String Z) {
 		World tmp = getWorldById(server, Long.parseLong(worldId));
 		if (tmp == null) {
 			parent.log.severe("ERROR: couldn't get world by ID for old save format");
 			return null;
 		}
 		return new Location(tmp, Integer.parseInt(X), Integer.parseInt(Y), Integer.parseInt(Z));
 	}
 	
 	/*
 	 * There's no lookup function in bukkit for returning a World object from the ID
 	 * So, here's a simple one
 	 * 
 	 * TODO: World names are supposed to be used instead, remove this later
 	 */
 	public World getWorldById(Server s, long id) {
 		/*
 		 * Bukkit changed this in bukkit-122 - craftbukkit-265
 		World worlds[] = s.getWorlds();
 		for (int i = 0; i < worlds.length; ++i) {
 			if (worlds[i].getId() == id) {
 				return worlds[i];
 			}
 		}
 		*/
 		Iterator<World> i = s.getWorlds().iterator();
 		while (i.hasNext()) {
 			World w = i.next();
 			if (w.getId() == id)
 				return w;
 		}
 		return null;
 	}
 	
 	/*
 	 * Little function to standardise Location to string format
 	 * Ends up being CSV of WorldName,X,Y,Z
 	 * They're the important parts anyway
 	 */
 	public String loc2str(Location l) {
 		if (l == null) {
 			parent.log.severe("ERROR: null location while trying to save CBlock at " + loc2str(blockLocation));
 		}
 		if (l.getWorld() == null) {
 			parent.log.severe("ERROR: null world in location while trying to save CBlock");
 		}
 		return  l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
 	}
 
 
 
 }
