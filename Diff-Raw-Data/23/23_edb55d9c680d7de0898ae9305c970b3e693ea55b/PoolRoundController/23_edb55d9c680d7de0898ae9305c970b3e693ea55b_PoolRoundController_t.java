 package final_project.control;
 
 import java.awt.Point;
 import final_project.model.*;
 
 public class PoolRoundController {
	PoolRound pools      = new PoolRound(); 
 
 	public boolean createPools(int poolSize) {
        Point p       = PoolSizeCalculator.calcPoolSize(pools.getNumPlayers(), poolSize);
         if (p.equals(Constants.IMPOSSIBLE_POOL_CONSTRAINTS))
         	return false;
         pools.setPoolSize(poolSize);
         //What are these doing?
        int numPools  = pools.getNumPlayers() / pools.getPoolSize();
         int leftOvers = pools.getNumPlayers() % pools.getPoolSize();
         return true;
 	}
 }
