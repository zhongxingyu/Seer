 package net.nabaal.majiir.realtimerender.rendering;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.ChunkSnapshot;
 import org.bukkit.Material;
 
 public final class TerrainHelper {
 
 	private TerrainHelper() throws InstantiationException {
 		throw new InstantiationException("Cannot instantiate a helper class.");
 	} 
 	
 	private static Set<Material> terrain = new HashSet<Material>(Arrays.asList(new Material[] {
 		Material.SAND,
 		Material.SANDSTONE,
 		Material.BEDROCK,
 		Material.CLAY, 
 		Material.DIRT,
 		Material.GRASS,
 		Material.GRAVEL,
 		Material.STONE,
 		Material.SNOW,
 		Material.MYCEL,
 		Material.NETHERRACK,
 	}));
 	
 	private static Set<Material> structure = new HashSet<Material>(Arrays.asList(new Material[] {
 		Material.WOOD,
 		Material.WOOL,
 		Material.COBBLESTONE,
 		Material.LEAVES,
 		Material.LOG,
 		Material.BOOKSHELF,
 		Material.BRICK,
 		Material.BRICK_STAIRS,
 		Material.DOUBLE_STEP,
 		Material.STEP,
 		Material.HUGE_MUSHROOM_1,
 		Material.HUGE_MUSHROOM_2,
 		Material.CROPS,
 		Material.WOOD_STAIRS,
 		Material.WOOD_DOOR,
 		Material.WORKBENCH,
 	}));
 	
 	public static boolean isTerrain(Material material) {
 		return terrain.contains(material);
 	}
 	
 	public static boolean isStructure(Material material) {
 		return structure.contains(material);
 	}
 	
 	public static byte getTerrainHeight(int x, int z, ChunkSnapshot snapshot) {
 		for (int y = Math.min(snapshot.getHighestBlockYAt(x, z) + 1, 127); y >= 0; y--) {
 			if (isTerrain(Material.getMaterial(snapshot.getBlockTypeId(x, y, z)))) {
 				return (byte) y;
 			}
 		}
 		return HeightMap.NO_HEIGHT_INFORMATION;
 	}
 	
 	public static byte getStructureHeight(int x, int z, ChunkSnapshot snapshot) {
 		for (int y = Math.min(snapshot.getHighestBlockYAt(x, z) + 1, 127); y >= 0; y--) {
			Material material = Material.getMaterial(snapshot.getBlockTypeId(x, y, z)));
 			if (isStructure(material)) {
 				return (byte) y;
 			} else if (isTerrain(material)) {
 				break;
 			}
 		}
 		return HeightMap.NO_HEIGHT_INFORMATION;
 	}
 	
 }
