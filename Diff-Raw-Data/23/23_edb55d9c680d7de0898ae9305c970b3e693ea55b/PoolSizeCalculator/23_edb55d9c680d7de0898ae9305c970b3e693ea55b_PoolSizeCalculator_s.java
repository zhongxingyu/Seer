 package final_project.control;
 
 public class PoolSizeCalculator {
 	private static int _numBigPools;
 	private static int _numSmallPools;
 	
 	public static int getNumBigPools() {
 		return _numBigPools;
 	}
 
 	public static int getNumSmallPools() {
 		return _numSmallPools;
 	}
 
 	/**
 	 * Calculates the number of pools of either size required to put all fencers into pools.
 	 * @param numPlayers A non-negative integer representing the number of fencers to be placed into pools
 	 * @param poolSize A non-negative integer representing the ideal number of fencers to be placed in each pool
 	 * @return A Java.awt.Point representing the proper number of pools of poolSize fencers(X) and the number of pools of poolsize - 1 fencers(Y)
 	 */
 	public static java.awt.Point calcPoolSize(int numPlayers, int poolSize) throws IllegalArgumentException{
		_numBigPools = 0;
		_numSmallPools = 0;
 		int placedFencers = 0;
 		
 		if (isInvalidPoolSizeForNumPlayers(numPlayers, poolSize)) {
 			throw new IllegalArgumentException("Invalid pool size or number of players");
 		}
 		
 		if (numPlayers == poolSize) {
 			return new java.awt.Point(1, 0);
 		}
         
 		while (placedFencers <= numPlayers) {
 			if (placedFencers == numPlayers) {
 				return new java.awt.Point(_numBigPools, _numSmallPools);
 			}
 
 			_numBigPools++;
			placedFencers = _numBigPools * poolSize;
 		}
 
 		while (placedFencers >= numPlayers) {
 			if (placedFencers == numPlayers) {
 				if (_numBigPools == 0)                    
 					return Constants.IMPOSSIBLE_POOL_CONSTRAINTS;
 				return new java.awt.Point(_numBigPools, _numSmallPools);
 			}
 
 			_numSmallPools++;
 			_numBigPools--;
			placedFencers = (_numBigPools * poolSize) + (_numSmallPools * (poolSize - 1));
 		}
 		//Does this ever return given the first if statement?
 		//yes, I have renamed it.  see Constants class for more information
 		return Constants.IMPOSSIBLE_POOL_CONSTRAINTS;
 	}
 
 	private static boolean isInvalidPoolSizeForNumPlayers(int numPlayers,
 			int poolSize) {
 		return poolSize <= 0 || numPlayers <= 0 || numPlayers < poolSize;
 	}
 }
