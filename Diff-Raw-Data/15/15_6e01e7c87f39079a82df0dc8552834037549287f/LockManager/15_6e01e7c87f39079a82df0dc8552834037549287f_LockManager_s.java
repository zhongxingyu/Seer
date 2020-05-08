 package uk.co.jacekk.bukkit.grouplock.locakble;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.HashMap;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 
 import uk.co.jacekk.bukkit.baseplugin.BaseObject;
 import uk.co.jacekk.bukkit.grouplock.GroupLock;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 public class LockManager extends BaseObject<GroupLock> {
 	
 	private HashMap<BlockLocation, LockableBlock> lockedBlocks;
 	private Gson gson;
 	
 	public LockManager(GroupLock plugin){
 		super(plugin);
 		
 		this.lockedBlocks = new HashMap<BlockLocation, LockableBlock>();
 		this.gson = (new GsonBuilder()).setPrettyPrinting().create();
 	}
 	
 	public void load(){
 		File dir = new File(plugin.getDataFolder(), "locks");
 		
 		if (!dir.exists()){
 			dir.mkdirs();
 		}
 		
		for (File worldDir : dir.listFiles()){
			for (File lockFile : worldDir.listFiles()){
 				try{
 					BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(lockFile)));
					this.gson.fromJson(input, LockableBlock.class);
 					input.close();
 				}catch (Exception e){
 					plugin.log.warn("Failed to load lock file " + lockFile.getName());
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	public void saveLockable(LockableBlock lockable){
 		BlockLocation location = lockable.getLocation();
 		
 		File worldDir = new File(plugin.getDataFolder() + File.separator +  "locks", location.getWorldUID().toString());
 		File lockFile = new File(worldDir, location.getX() + "_" + location.getY() + "_" + location.getZ() + ".json");
 		
 		if (!worldDir.exists()){
 			worldDir.mkdirs();
 		}
 		
 		try{
 			OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(lockFile));
 			
 			output.write(this.gson.toJson(lockable, LockableBlock.class));
 			
 			output.close();
 		}catch (Exception e){
 			plugin.log.warn("Failed to save lock file " + lockFile.getName());
 			e.printStackTrace();
 		}
 	}
 	
 	public void save(){
 		for (LockableBlock lockable : this.lockedBlocks.values()){
 			this.saveLockable(lockable);
 		}
 	}
 	
 	public int getTotalLockedBlocks(){
 		return this.lockedBlocks.size();
 	}
 	
 	public LockableBlock getLockedBlock(BlockLocation location){
 		return this.lockedBlocks.get(location);
 	}
 	
 	public LockableBlock getLockedBlock(Location location){
 		for (LockableBlock lockable : this.lockedBlocks.values()){
 			if (lockable.getLocation().equals(location)){
 				return lockable;
 			}
 		}
 		
 		return null;
 	}
 	
 	public LockableBlock getLockedBlock(World world, int x, int y, int z){
 		for (LockableBlock lockable : this.lockedBlocks.values()){
 			BlockLocation location = lockable.getLocation();
 			
 			if (location.getX() == x && location.getY() == y && location.getZ() == z && location.getWorldUID().equals(world.getUID())){
 				return lockable;
 			}
 		}
 		
 		return null;
 	}
 	
 	public LockableBlock addLockedBlock(BlockLocation location, String ownerName){
 		if (this.lockedBlocks.containsKey(location)){
 			throw new IllegalArgumentException("Locked block already exists at this location");
 		}
 		
 		if (plugin.server.getWorld(location.getWorldUID()) == null){
 			throw new IllegalArgumentException("No world with UID " + location.getWorldUID().toString() + " loaded");
 		}
 		
 		LockableBlock lockable = new LockableBlock(location, ownerName);
 		
 		this.lockedBlocks.put(location, lockable);
 		
 		this.saveLockable(lockable);
 		
 		return lockable;
 	}
 	
 	public void removeLockedBlock(BlockLocation location){
 		if (!this.lockedBlocks.containsKey(location)){
 			throw new IllegalArgumentException("No locked exists at this location");
 		}
 		
 		String dirName = plugin.getDataFolder() + File.separator +  "locks" + File.separator + location.getWorldUID().toString();
 		String fileName = location.getX() + "_" + location.getY() + "_" + location.getZ() + ".json";
 		
 		(new File(dirName, fileName)).delete();
 		
 		this.lockedBlocks.remove(location);
 	}
 	
 }
