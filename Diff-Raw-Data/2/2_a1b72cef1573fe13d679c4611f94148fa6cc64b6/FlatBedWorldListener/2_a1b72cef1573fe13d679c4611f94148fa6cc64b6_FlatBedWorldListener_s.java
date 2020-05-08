 package zonedabone.FlatBed;
 
 import org.bukkit.Chunk;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.event.world.ChunkLoadEvent;
 import org.bukkit.event.world.WorldListener;
 
 
 public class FlatBedWorldListener extends WorldListener {
 	
 	public static FlatBed plugin;
 	
 	public FlatBedWorldListener(FlatBed instance) {
 		plugin = instance;
 	}
 	
 	public void onChunkLoad(ChunkLoadEvent e){
 		Chunk chunk = e.getChunk();
 		String path = Integer.toString(chunk.getX())+'.'+Integer.toString(chunk.getZ());
 		if(!FlatBed.chunks.getBoolean(path, false)){
 			FlatBed.chunks.setProperty(path, true);
 			for(int x = 0;x<16;x++){
 				for(int z = 0;z<16;z++){
					chunk.getBlock(x, 1, z).setType(Material.BEDROCK);
 					for(int y = 1;y<5;y++){
 						Block block = chunk.getBlock(x, y, z);
 						if(block.getType()==Material.BEDROCK){
 							block.setType(Material.STONE);
 						}
 					}
 				}
 			}
 		}
 	}
 
 }
