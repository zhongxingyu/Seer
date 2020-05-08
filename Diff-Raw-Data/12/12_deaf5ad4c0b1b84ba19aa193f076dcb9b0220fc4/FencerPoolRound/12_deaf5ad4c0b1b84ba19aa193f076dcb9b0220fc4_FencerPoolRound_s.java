 package final_project.model;
 
 public class FencerPoolRound extends PoolRound{
	public FencerPoolRound(int numPools, int poolSize) {
 		_poolSize = poolSize;
 		Pool newPool;
 		for (int i = 0; i < numPools; i++) {
 			newPool = new FencerPool();
 			_pools.add(newPool);
 		}
 	}
 }
