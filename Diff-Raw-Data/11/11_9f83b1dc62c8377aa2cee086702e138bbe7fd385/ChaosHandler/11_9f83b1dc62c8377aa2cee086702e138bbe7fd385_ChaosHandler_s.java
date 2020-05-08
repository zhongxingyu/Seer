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
			worldData.chaos += Config.CHAOS_PER_WORLD_TICK;
 		}
 		else {
			worldData.chaos -= Config.CHAOS_PER_WORLD_TICK;
 		}
 	}
 	
 	public static void addChaos(int change) {
 		if (worldData == null) {
 			return;
 		}
 		worldData.chaos += change;
		
		//LogHelper.debug("Chaos: " + worldData.chaos);
 	}
 	
 	public static int getChaos() {
 		if (worldData == null) {
			return -1;
 		}
 		return worldData.chaos;
 	}
 }
