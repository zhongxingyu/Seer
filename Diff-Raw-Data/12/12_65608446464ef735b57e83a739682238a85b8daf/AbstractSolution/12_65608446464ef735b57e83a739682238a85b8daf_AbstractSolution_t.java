 
 public abstract class AbstractSolution implements Solution {
 
 	public Solution runRandom(){
 		Solution s = this;
 		int count = 0;
 		while (count < Main.MAX_ITER){
 			Solution rand_soln = this.randSolution();
 			if (rand_soln.getResidue() < s.getResidue())
 				s = rand_soln;
 			count++;
 		}
 		return s;
 	}
 	public Solution runHillClimbing(){
 		Solution s = this;
 		int count = 0;
 		while(count < Main.MAX_ITER){
 			Solution rand_neighbor = s.getRandNeigbor();
 			if (rand_neighbor.getResidue() < s.getResidue())
 				s = rand_neighbor;
 			count++;
 		}
 		return s;
 	}
 	public Solution runSimulatedAnnealing(){
 		Solution s = this;
 		Solution s_store = s;
 		int count = 0;
 		while (count < Main.MAX_ITER){
 			Solution rand_neighbor = s.getRandNeigbor();
 			if (rand_neighbor.getResidue() < s.getResidue())
 				s = rand_neighbor;
 			else if (Math.random() < Math.exp((s.getResidue()-rand_neighbor.getResidue())/timeFunction(count)))
 				s = rand_neighbor;
			if (s.getResidue() < s_store.getResidue())
				s_store = s;
			count++;
		}
			
		return s_store;
		
 	}
 	private static double timeFunction(int iter){
 		return (Math.pow(10, 10)*Math.pow(0.8, iter/300.0));
 	}
 }
