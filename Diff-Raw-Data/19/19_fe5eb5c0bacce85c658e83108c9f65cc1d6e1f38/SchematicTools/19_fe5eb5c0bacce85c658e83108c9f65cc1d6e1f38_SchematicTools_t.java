 package com.randrdevelopment.propertygroup.regions;
 
 import java.io.File;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 
 import com.randrdevelopment.propertygroup.log.PropertyGroupLogger;
 import com.randrdevelopment.propertygroup.PropertyGroup;
 
 import com.sk89q.worldedit.CuboidClipboard;
 import com.sk89q.worldedit.EditSession;
 import com.sk89q.worldedit.LocalPlayer;
 import com.sk89q.worldedit.LocalWorld;
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldedit.WorldEdit;
 import com.sk89q.worldedit.bukkit.BukkitWorld;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.regions.CuboidRegion;
 import com.sk89q.worldedit.schematic.MCEditSchematicFormat;
 import com.sk89q.worldedit.regions.Region;
 
 public class SchematicTools {
 	private WorldEditPlugin wep;
 	private WorldEdit we;
 	private File saveFile;
 	private LocalPlayer localPlayer;
 	
 	private SchematicTools () {
 		wep = PropertyGroup.getWorldEdit();
 		if (wep == null) {
 			return;
 		}
 		we = wep.getWorldEdit();	
 	}
 	
 	private boolean loadSchematicFile(String FileName){
 		try {
 			File dir = we.getWorkingDirectoryFile("schematics");
 	        saveFile = we.getSafeOpenFile(localPlayer, dir, FileName, "schematic", "schematic");
 			return true;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 	
 	private int getWidth() {
 		if (wep == null){
 			return 0;
 		}
 		
 		try {
 			CuboidClipboard cc = MCEditSchematicFormat.MCEDIT.load(saveFile);
 			return cc.getWidth();
 		} catch (Exception e) {
 			return 0;
 		}
 	}
 	
 	private int getLength() {
 		if (wep == null){
 			return 0;
 		}
 		
 		try {		
 			CuboidClipboard cc = MCEditSchematicFormat.MCEDIT.load(saveFile);
 			return cc.getLength();
 		} catch (Exception e) {
 			return 0;
 		}
 	}
 	
 	private int getHeight() {
 		if (wep == null){
 			return 0;
 		}
 		
 		try {
 			CuboidClipboard cc = MCEditSchematicFormat.MCEDIT.load(saveFile);
 			return cc.getHeight();
 		} catch (Exception e) {
 			return 0;
 		}
 	}
 	
 	private boolean placeSchematic(int blocks, int start_x, int start_y, int start_z, String worldName){
 		if (wep == null){
 			return false;
 		}
 		
 		try {
 			World world = Bukkit.getWorld(worldName);
 			
 			EditSession es = new EditSession(new BukkitWorld(world), blocks);
 			CuboidClipboard cc = MCEditSchematicFormat.MCEDIT.load(saveFile);
 			Vector originaloffset = cc.getOffset();
 			Vector newoffset = new Vector(0, originaloffset.getY(), 0);
 			cc.setOffset(newoffset);
 			Vector pos = new Vector(start_x, start_y, start_z);
 			cc.paste(es, pos, false);
 			return true;
 		} catch (Exception e) {
 			PropertyGroupLogger.severe(e.getMessage());
 			return false;
 		}
 	}
 	
 	private boolean reloadArea(int blocks, String worldName, int x1, int y1, int z1, int x2, int y2, int z2) {
 		if (wep == null) {
 			return false;
 		}
 		
 		try {
 			World world = Bukkit.getWorld(worldName);
 			EditSession es = new EditSession(new BukkitWorld(world), blocks);
 		
 			LocalWorld lworld = new BukkitWorld(world);
 			
 			Vector min = new Vector(x1, y1, z1);
 			Vector max = new Vector(x2, y2, z2);
 			
 			Region region = new CuboidRegion(lworld, min, max);
 			
 			lworld.regenerate(region, es);
 			
 			return true;
 		} catch (Exception e) {
 			PropertyGroupLogger.severe(e.getMessage());
 			return false;
 		}
 	}
 	
 	public static boolean reload(String FileName, String worldname, int x, int y, int z, int blocks) {
 		boolean restored = false;
 		try {
 			SchematicTools st = new SchematicTools();
 			if (st.loadSchematicFile(FileName)) {
 				if (st.placeSchematic(blocks, x, y, z, worldname)) {
 					restored = true;
 				}
 			}
 		} catch (Exception e) {
 			restored = false;
 		}
 		return restored;
 	}
 	
 	public static boolean reload(String FileName, Location location, int blocks) {
 		boolean restored = false;
 		try {
 			SchematicTools st = new SchematicTools();
 			if (st.loadSchematicFile(FileName)) {
 				if (st.placeSchematic(blocks, (int)location.getX(), (int)location.getY(), (int)location.getZ(), location.getWorld().getName())) {
 					restored = true;
 				}
 			}
 		} catch (Exception e) {
 			restored = false;
 		}
 		return restored;
 	}
 	
 	public static boolean regen(Location l1, Location l2, int blocks) {
 		World world = l1.getWorld();
 		String worldName = world.getName();
 		boolean restored = false;
 		try {
 			SchematicTools st = new SchematicTools();
 			if (st.reloadArea(blocks, worldName, (int)l1.getX(), (int)l1.getY(), (int)l1.getZ(), (int)l2.getX(), (int)l2.getY(), (int)l2.getZ())) {
 				restored = true;
 			}
 		} catch (Exception e) {
 			restored = false;
 		}
 		return restored;
 	}
 	
 	public static Location getSize(String FileName) {
		SchematicTools st = new SchematicTools();
		Location size;
 		try {
 			if (st.loadSchematicFile(FileName)) {
 				int width = st.getWidth();
 				int height = st.getHeight();
 				int length = st.getLength();
 				
				size = new Location(null, width, height, length);
 			} else {
				size = new Location(null, 0, 0, 0);
 			}
 		} catch (Exception e) {
			size = new Location(null, 0, 0, 0);
 		}
		
		return size;
 	}
 }
