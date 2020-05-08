 package net.nexisonline.spade.generators;
 
 import java.util.Random;
 
 import net.minecraft.server.Block;
 import net.nexisonline.spade.SpadePlugin;
 
 import org.bukkit.World;
 import org.bukkit.util.config.ConfigurationNode;
 
 public class StalactiteGenerator extends SpadeEffectGenerator {
 	public StalactiteGenerator(SpadePlugin plugin, World w,
 			ConfigurationNode node, long seed) {
 		super(plugin, w, node, seed);
 		rnd=new Random((seed*1024)+15);
 	}
 
 	private byte[] chunk;
 	private int X;
 	private int Z;
 	private Random rnd;
 
 	public void addToChunk(byte[] chunk, int x, int z) {
 		this.chunk=chunk;
 		this.X=x;
 		this.Z=z;
 
 		for(int i = 0;i<10;i++) {
 			addStalactite(rnd.nextInt(15),rnd.nextInt(15)); 
 		}
 	}
 
 	private void addStalactite(int x, int z) {
 		for(int y = 1;y<128;y++) {
			if(get(x,y,z)==1 && (get(x,y-1,z)==0 || get(x,y-1,z)==Block.STATIONARY_WATER.id)) {
 				int ch=0;
 				for(;!(get(x,y-ch,z)==1)&&y-ch>1;ch++) {}
 				if(ch<5) return;
 				addStalactite(x+(X*16), y, x+(Z*16));
 			}
 		}
 	}
 
 	private void addStalactite(int x, int y, int z) {
 		if(world.isChunkLoaded(x>>4, z>>4)) {
 			world.loadChunk(x>>4, z>>4);
 		}
 		if(y>=world.getHighestBlockYAt(x, z)) return;
 		boolean N=false;
 		boolean E=false;
 		boolean W=false;
 		boolean S=false;
 		for(;y<128&&world.getBlockAt(x,y,z).getTypeId()==0;y++) {
 			world.getBlockAt(x,y,z).setTypeId(1);
 			if(rnd.nextDouble()<0.25 && !N)
 				addStalactite(x+1,y,z);
 			if(rnd.nextDouble()<0.25 && !E)
 				addStalactite(x,y+1,z);
 			if(rnd.nextDouble()<0.25 && !W)
 				addStalactite(x-1,y,z);
 			if(rnd.nextDouble()<0.25 && !S)
 				addStalactite(x,y-1,z);
 		}
 	}
 
 	private int get(int x, int y, int z) {
 		return chunk[x << 11 | z << 7 | y];
 	}
 }
