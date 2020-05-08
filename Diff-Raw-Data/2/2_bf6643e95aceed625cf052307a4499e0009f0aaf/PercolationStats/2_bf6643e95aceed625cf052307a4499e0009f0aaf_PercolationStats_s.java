 /** 
  * Maria Pacana (mariapacana)
  * 10/8/2013 (Algorithms, Part I)
  *
  * The PercolationStats class runs Monte Carlo simulations on Percolation systems in order to determine the
  * "percolation threshold." 
  */ 
 
 public class PercolationStats {
 
 	public double[] stats;				//An array containing the percentage of open sites for each experiment.
 	public int numExperiments;			//Number of experiments being run.
 
 	//Code for displaying stats (takes command-line args).
 	public static void main(String[] args) {
 		int N = Integer.parseInt(args[0]);
 		int T = Integer.parseInt(args[1]);
 		PercolationStats myStats = new PercolationStats(N,T);
 		System.out.println("mean 			 = "+myStats.mean());
 		System.out.println("stddev 			 = "+myStats.stddev());
 		System.out.println("95% confidence interval	 = "+myStats.confidenceLo()+","+myStats.confidenceHi());
 	}  
 
 	//Confirms that input is valid.
     private void validNT(int N, int T) {
 		if (N <= 0 || T <= 0) {
     		throw new IndexOutOfBoundsException();
     	}
     }
 
    	//Perform T independent computational experiments on an N-by-N grid. 
    	//Each experiment starts by generating a grid with all sites blocked.
    	//Then, sites are chosen at random and opened until the system percolates.
    	//We track the percentage of sites that are open when the system has percolated.
 	public PercolationStats(int N, int T) {
 		validNT(N,T);
 
 		stats = new double[T];
 		numExperiments = T;
 
 		for (int i=0; i<T; i++) {
 			Percolation myPerc = new Percolation(N);
 
 			float openSites = 0;						
 
 			while (!myPerc.percolates()) { 
 				int randX = StdRandom.uniform(N)+1;
 				int randY = StdRandom.uniform(N)+1;
 
 				if (!myPerc.isOpen(randX, randY)) {
 					myPerc.open(randX, randY);
 					openSites = openSites + 1;
 				}
 			}
			stats[i] = openSites / N;
 		}
 	} 
 
 	//Sample mean of percolation threshold.
 	public double mean() {
 		return StdStats.mean(stats);
 	}                    
 	
 	//Sample standard deviation of percolation threshold.
 	public double stddev() {
 		return StdStats.stddev(stats);
 	}                   
 
 	//Lower bound of the 95% confidence interval.
 	public double confidenceLo() {
 		return mean() - (1.96*stddev()/Math.pow(numExperiments,.5));
 	}             
 	
 	//Upper bound of the 95% confidence interval.
 	public double confidenceHi(){
 		return mean() + (1.96*stddev()/Math.pow(numExperiments,.5));
 	}            
 	
 }
