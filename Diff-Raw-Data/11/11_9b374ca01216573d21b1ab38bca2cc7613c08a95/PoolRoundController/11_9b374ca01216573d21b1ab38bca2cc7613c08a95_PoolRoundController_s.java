 package final_project.control;
 
 import final_project.model.*;
 
 
 public class PoolRoundController {
 	PoolRound _poolRound;
 	int _numPools, _numBigPools, _numSmallPools;
 
 	public PoolRoundController() {
 	}
 
 	public boolean createPools(int poolSize) {
 		//Try to calculate the pool size. If unable to do so, return false
 		try {
 			calcPoolSize(poolSize);
 		} catch (IllegalArgumentException e) {
 			return false;
 		}
 
 		_poolRound = new FencerPoolRound(_numPools, poolSize);
         _poolRound.populatePools();
 
         return true;
 	}
 
 	private void calcPoolSize(int poolSize) throws IllegalArgumentException{
 		PoolSizeCalculator poolSizeCalc = new PoolSizeCalculator(_poolRound.getNumPlayers(), poolSize);
 		_numBigPools = poolSizeCalc.getNumBigPools();
 		_numSmallPools = poolSizeCalc.getNumSmallPools();
 		_numPools = _numBigPools + _numSmallPools;
 	}
 }
