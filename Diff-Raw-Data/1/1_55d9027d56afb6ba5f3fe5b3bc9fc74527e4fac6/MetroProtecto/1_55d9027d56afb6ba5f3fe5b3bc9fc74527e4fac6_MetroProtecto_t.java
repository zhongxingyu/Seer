 package net.llamaslayers.minecraft.metroprotecto;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class MetroProtecto extends JavaPlugin {
 	public static final List<Material> trackMaterials;
 	static {
 		ArrayList<Material> materials = new ArrayList<Material>();
 		materials.add(Material.RAILS);
 		materials.add(Material.POWERED_RAIL);
 		materials.add(Material.DETECTOR_RAIL);
 		trackMaterials = Collections.unmodifiableList(materials);
 	}
 	private PermissionHandler permissionHandler;
 	private final HashMap<RegionLocation, ArrayList<BlockLocation>> trackCache = new HashMap<RegionLocation, ArrayList<BlockLocation>>();
 	protected final HashMap<BlockLocationWorld, BlockTypeCache> blockCache = new HashMap<BlockLocationWorld, BlockTypeCache>();
 
 	@Override
 	public void onDisable() {
 		finalizeBlockCache();
 		trackCache.clear();
 	}
 
 	@Override
 	public void onEnable() {
 		setupPermissions();
 		finalizeBlockCache();
 
 		getConfiguration()
 				.setHeader(
 						"# Permissions:",
 						"# metroprotecto.protecttracks.build   - Users with this permission will have their tracks protected",
 						"# metroprotecto.protecttracks.destroy - Can destroy protected tracks",
 						"# metroprotecto.protecttracks.find    - Can use the /metroprotecto command to locate protected tracks.",
 						"# metroprotecto.neartracks.build      - Can build near protected tracks (within the protected zone, but not where the tracks are)",
 						"# metroprotecto.neartracks.destroy    - Can destroy things near protected tracks (includes starting fires, does not include the actual tracks)",
 						"#",
 						"# In addition, liquid flow and fire spread are disabled within the protection zone.",
 						"");
 		getConfiguration().save();
 
 		if (!getConfiguration().getAll().containsKey("protection_radius")) {
 			getConfiguration().setProperty("protection_radius", 3);
 			getConfiguration().save();
 		}
 		if (!getConfiguration().getAll().containsKey("protection_height")) {
 			getConfiguration().setProperty("protection_height", 5);
 			getConfiguration().save();
 		}
 		if (!getConfiguration().getAll().containsKey("search_radius")) {
 			// Actually it's an apothem, but I would alienate half my audience if I said so.
 			getConfiguration().setProperty("search_radius", 20);
 			getConfiguration().save();
 		}
 
 		MetroProtectoBlockListener blockListener = new MetroProtectoBlockListener(
 				this);
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_FROMTO, blockListener,
 				Event.Priority.Normal, this);
 
 		MetroProtectoCommand command = new MetroProtectoCommand(this);
 		getCommand("metroprotecto").setExecutor(command);
 	}
 
 	@SuppressWarnings("unchecked")
 	private void finalizeBlockCache() {
 		if (blockCache.isEmpty()) {
 			try {
 				File cacheFile = new File(getDataFolder(), "blockcache.dat");
 				if (!cacheFile.exists())
 					return;
 				ObjectInputStream in = new ObjectInputStream(
 						new FileInputStream(cacheFile));
 				blockCache
 						.putAll((HashMap<BlockLocationWorld, BlockTypeCache>) in
 								.readObject());
 				in.close();
 				cacheFile.delete();
 			} catch (IOException ex) {
 				return;
 			} catch (ClassNotFoundException ex) {
 				return;
 			}
 		}
 		for (BlockTypeCache cache : blockCache.values()) {
 			Block block = getServer().getWorld(cache.world).getBlockAt(cache.x,
 					cache.y, cache.z);
 			cache.apply(block);
 		}
 		blockCache.clear();
 	}
 
 	private void setupPermissions() {
 		if (permissionHandler != null)
 			return;
 
 		Plugin permissionsPlugin = getServer().getPluginManager().getPlugin(
 				"Permissions");
 
 		if (permissionsPlugin == null)
 			return;
 
 		permissionHandler = ((Permissions) permissionsPlugin).getHandler();
 	}
 
 	public boolean checkPerm(Player player, String perm) {
 		if (permissionHandler == null)
 			return player.isOp();
 		return permissionHandler.has(player, perm);
 	}
 
 	private enum ProtectionBoundType {
 		XZ, Y
 	}
 
 	private int getTrackLookBound(int start, boolean additive,
 			ProtectionBoundType type) {
 		if (type == ProtectionBoundType.Y)
 			return Math.min(
 					Math.max(
 							start
 									+ getConfiguration().getInt(
 											"protection_height", 5)
 									* (additive ? 1 : -1), 0), 127);
 		return start + getConfiguration().getInt("protection_radius", 3)
 				* (additive ? 1 : -1);
 	}
 
 	@SuppressWarnings("unchecked")
 	public boolean isSpaceProtected(Location location) {
 		RegionLocation region = new RegionLocation(location);
 		ArrayList<BlockLocation> tracks;
 		if (trackCache.containsKey(region)) {
 			tracks = trackCache.get(region);
 		} else {
			trackCache.put(region, new ArrayList<BlockLocation>()); // Will be overwritten if the file is found. Otherwise, this prevents epic lag.
 			File worldDir = new File(getDataFolder(), region.world);
 			if (!worldDir.exists())
 				return false;
 			File regionFile = new File(worldDir, "r." + region.x + "."
 					+ region.z + ".dat");
 			if (!regionFile.exists())
 				return false;
 			try {
 				ObjectInputStream in = new ObjectInputStream(
 						new FileInputStream(regionFile));
 				tracks = (ArrayList<BlockLocation>) in.readObject();
 				in.close();
 				trackCache.put(region, tracks);
 			} catch (IOException ex) {
 				return false;
 			} catch (ClassNotFoundException ex) {
 				return false;
 			}
 		}
 
 		return tracks.contains(new BlockLocation(location));
 	}
 
 	public boolean isNearTrain(Location location) {
 		int x1 = getTrackLookBound(location.getBlockX(), false,
 				ProtectionBoundType.XZ), x2 = getTrackLookBound(
 				location.getBlockX(), true, ProtectionBoundType.XZ), z1 = getTrackLookBound(
 				location.getBlockZ(), false, ProtectionBoundType.XZ), z2 = getTrackLookBound(
 				location.getBlockZ(), true, ProtectionBoundType.XZ), y1 = getTrackLookBound(
 				location.getBlockY(), false, ProtectionBoundType.Y), y2 = getTrackLookBound(
 				location.getBlockY(), true, ProtectionBoundType.Y);
 
 		for (int x = x1; x <= x2; x++) {
 			for (int y = y1; y <= y2; y++) {
 				for (int z = z1; z <= z2; z++) {
 					if (isSpaceProtected(new Location(location.getWorld(), x,
 							y, z)))
 						return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 	private void saveRegionFile(RegionLocation region,
 			ArrayList<BlockLocation> locations) {
 		File worldDir = new File(getDataFolder(), region.world);
 		worldDir.mkdirs();
 		File regionFile = new File(worldDir, "r." + region.x + "." + region.z
 				+ ".dat");
 
 		try {
 			ObjectOutputStream out = new ObjectOutputStream(
 					new FileOutputStream(regionFile));
 			out.writeObject(locations);
 			out.flush();
 			out.close();
 		} catch (IOException ex) {
 		}
 	}
 
 	public void protectLocaton(Location location) {
 		if (isSpaceProtected(location))
 			return;
 
 		RegionLocation region = new RegionLocation(location);
 		ArrayList<BlockLocation> tracks;
 		if (trackCache.containsKey(region)) {
 			tracks = trackCache.get(region);
 		} else {
 			tracks = new ArrayList<BlockLocation>();
 			trackCache.put(region, tracks);
 		}
 
 		tracks.add(new BlockLocation(location));
 
 		saveRegionFile(region, tracks);
 	}
 
 	public void unprotectLocaton(Location location) {
 		if (!isSpaceProtected(location))
 			return;
 
 		RegionLocation region = new RegionLocation(location);
 		ArrayList<BlockLocation> tracks;
 		if (trackCache.containsKey(region)) {
 			tracks = trackCache.get(region);
 		} else
 			return;
 
 		tracks.remove(new BlockLocation(location));
 
 		saveRegionFile(region, tracks);
 	}
 
 	public synchronized void temporaryChangeBlock(Block block,
 			final Material changeTo, byte data, long timeout) {
 		temporaryChangeBlock(Collections.singletonList(block), changeTo, data,
 				timeout);
 	}
 
 	public synchronized void temporaryChangeBlock(List<Block> blocks,
 			final Material changeTo, byte data, long timeout) {
 		final ArrayList<Block> changedBlocks = new ArrayList<Block>();
 		for (Block block : blocks) {
 			BlockLocationWorld loc = new BlockLocationWorld(block.getLocation());
 			if (blockCache.containsKey(loc)) {
 				continue;
 			}
 			blockCache.put(loc, new BlockTypeCache(block));
 		}
 		saveBlockCache();
 		for (Block block : changedBlocks) {
 			block.setType(changeTo);
 			block.setData(data);
 		}
 		new Timer().schedule(new TimerTask() {
 			@Override
 			public void run() {
 				for (Block block : changedBlocks) {
 					BlockLocationWorld loc = new BlockLocationWorld(block
 							.getLocation());
 					BlockTypeCache cache = blockCache.get(loc);
 					if (cache != null) {
 						cache.apply(block);
 					}
 					blockCache.remove(loc);
 				}
 				saveBlockCache();
 			}
 		}, timeout);
 	}
 
 	public synchronized void temporaryChangeBlock(final Player player,
 			final List<Block> blocks, final Material changeTo, byte data,
 			long timeout) {
 		for (Block block : blocks) {
 			player.sendBlockChange(block.getLocation(), changeTo, data);
 		}
 		new Timer().schedule(new TimerTask() {
 			@Override
 			public void run() {
 				for (Block block : blocks) {
 					player.sendBlockChange(block.getLocation(),
 							block.getType(), block.getData());
 				}
 			}
 		}, timeout);
 	}
 
 	private synchronized void saveBlockCache() {
 		File cacheFile = new File(getDataFolder(), "blockcache.dat");
 		if (blockCache.isEmpty()) {
 			cacheFile.delete();
 			return;
 		}
 		try {
 			ObjectOutputStream out = new ObjectOutputStream(
 					new FileOutputStream(cacheFile));
 			out.writeObject(blockCache);
 			out.flush();
 			out.close();
 		} catch (IOException ex) {
 		}
 	}
 }
