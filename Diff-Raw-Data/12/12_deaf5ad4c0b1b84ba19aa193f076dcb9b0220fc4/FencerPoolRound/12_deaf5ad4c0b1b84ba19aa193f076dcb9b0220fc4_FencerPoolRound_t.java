 package final_project.model;
 
import java.util.List;

 public class FencerPoolRound extends PoolRound{
	public FencerPoolRound(List<Integer> initialSeeding, int numPools, int poolSize) {
 		_poolSize = poolSize;
 		Pool newPool;
 		for (int i = 0; i < numPools; i++) {
 			newPool = new FencerPool();
 			_pools.add(newPool);
 		}
 	}
 }
