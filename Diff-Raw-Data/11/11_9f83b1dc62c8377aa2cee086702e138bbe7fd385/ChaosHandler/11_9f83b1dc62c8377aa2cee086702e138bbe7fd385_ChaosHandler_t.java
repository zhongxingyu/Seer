 package silentAbyss.core.handlers;
 
 import net.minecraft.world.World;
 import silentAbyss.configuration.Config;
 import silentAbyss.world.AbyssWorldData;
 
 public class ChaosHandler {
 
 	private static AbyssWorldData worldData = null;
 	
 	public static void init(World world) {
 		worldData = AbyssWorldData.forWorld(world);
 	}
 	
 	public static void doWorldTick(World world) {
 		if (worldData == null) {
 			init(world);
 		}
 		
 		if (worldData.chaos < Config.CHAOS_MAX_AMBIENT) {
			worldData.addChaos(Config.CHAOS_PER_WORLD_TICK);
 		}
 		else {
			worldData.addChaos(-Config.CHAOS_PER_WORLD_TICK);
 		}
 	}
 	
 	public static void addChaos(int change) {
 		if (worldData == null) {
 			return;
 		}
 		worldData.chaos += change;
 	}
 	
 	public static int getChaos() {
 		if (worldData == null) {
			return 0;
 		}
 		return worldData.chaos;
 	}
 }
