 package us.fitzpatricksr.cownet.utils;
 
 import com.sk89q.worldedit.CuboidClipboard;
 import com.sk89q.worldedit.EditSession;
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldedit.bukkit.BukkitWorld;
 import com.sk89q.worldedit.data.DataException;
 import com.sk89q.worldedit.schematic.SchematicFormat;
 import org.bukkit.Location;
 import org.bukkit.World;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 
 public class SchematicUtils {
 	public static boolean placeSchematic(File schematic, Location location, boolean placeAir, int maxBlocks) {
 		try {
 			World w = location.getWorld();
 			EditSession session = new EditSession(new BukkitWorld(w), maxBlocks);
			session.enableQueue();
 			CuboidClipboard clipBoard = SchematicFormat.MCEDIT.load(schematic);
 			Vector v = new Vector(location.getX(), location.getY(), location.getZ());
 			clipBoard.place(session, v, placeAir);
			session.flushQueue();
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public static File[] getSchematics(File folder) {
 		if ((folder != null) && folder.exists() && folder.isDirectory()) {
 			return folder.listFiles(new FilenameFilter() {
 				@Override
 				public boolean accept(File file, String s) {
 					return s.endsWith(".schematic");
 				}
 			});
 		} else {
 			return new File[0];
 		}
 	}
 
 	public static void saveSchematic(File schematic, Location l1, Location l2, int maxBlocks) {
 		Vector min = new Vector(l1.getX(), l1.getY(), l1.getZ());
 		Vector max = new Vector(l2.getX(), l2.getY(), l2.getZ());
 		Vector size = max.subtract(min);
 		CuboidClipboard clip = new CuboidClipboard(size, min);
		EditSession session = new EditSession(new BukkitWorld(l1.getWorld()), maxBlocks);
		session.enableQueue();
		clip.copy(session);
		session.flushQueue();
 		try {
 			SchematicFormat.MCEDIT.save(clip, schematic);
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (DataException e) {
 			e.printStackTrace();
 		}
		session.flushQueue();
 	}
 }
