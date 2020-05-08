 public class PercolationStats {
     private int gridSize;
     private int numberOfRuns;
     private double[] results;
 
     private int runSimulation() {
         Percolation p = new Percolation(gridSize);
         int openSites = 0;
 
         do {
            int i = StdRandom.uniform(gridSize);
            int j = StdRandom.uniform(gridSize);
            if(!p.isOpen(i, j)) {
                p.open(i, j);
                openSites += 1;
            }
         } while(!p.percolates());
 
         return openSites;
     }
 
     public PercolationStats(int N, int T) {
         if (N <= 0 || T <= 0) {
             throw new IllegalArgumentException();
         }
         gridSize = N;
         numberOfRuns = T;
         results = new double[numberOfRuns];
 
         for(int i=0; i<numberOfRuns; i++) {
             double openSites = runSimulation();
             results[i] = openSites/(gridSize*gridSize);
         }
     }
 
     public double mean() {
         return StdStats.mean(results);
     }
 
     public double stddev() {
         return StdStats.stddev(results);
     }
 
     public double confidenceLo() {
        return mean() - 1.96 * stddev();
     }
 
     public double confidenceHi() {
        return mean() + 1.96 * stddev();
     }
 
     public static void main(String[] args) {
         int N = Integer.parseInt(args[0]);
         int T = Integer.parseInt(args[1]);
 
         PercolationStats ps = new PercolationStats(N, T);
         System.out.println("mean                    = " + ps.mean());
         System.out.println("stddev                  = " + ps.stddev());
         System.out.println("95% confidence interval = " + ps.confidenceLo() + ", " + ps.confidenceHi());
     }
 }
