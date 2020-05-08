 public class PercolationStats 
 {
     private int times;
     public double[] percolationThresholds;
     
    public PercolationStats(int N, int T)    // perform T independent computational experiments on an N-by-N grid
    {
        times = T;
        percolationThresholds = new double[T];
        for(int time = 0; time < T; time++)
        {
            Percolation percolation = new Percolation(N);
            int count = 0;
            while(!percolation.percolates())
            {
                 int i = StdRandom.uniform(0,N);
                 int j = StdRandom.uniform(0,N);
                 if(percolation.isOpen(i ,j) == false)
                 {
                     count++;
                     percolation.open(i ,j);
                 }
            }
            percolationThresholds[time] = 1.0 * count / (N * N);
        }       
    }
    
    public double mean()                     // sample mean of percolation threshold
    {
        return StdStats.mean(percolationThresholds);
    }
    public double stddev()                   // sample standard deviation of percolation threshold
    {
        return StdStats.stddev(percolationThresholds);
    }
    
    public double confidenceLo()             // returns lower bound of the 95% confidence interval
    {
        return mean() - 1.96 * stddev() / Math.sqrt(times);
    }
    public double confidenceHi()             // returns upper bound of the 95% confidence interval
    {
        return mean() + 1.96 * stddev() / Math.sqrt(times);
    }
    public static void main(String[] args)   // test client, described below
    {
       if (args.length == 2 && Integer.parseInt(args[0]) > 0 && Integer.parseInt(args[1]) > 0) 
       {
           int N = Integer.parseInt(args[0]);        
           int T = Integer.parseInt(args[1]);    
           PercolationStats percolationStats = new PercolationStats(N, T);
           StdOut.println("mean" + "                    = " + percolationStats.mean());
           StdOut.println("stddev" + "                  = " + percolationStats.stddev());
           StdOut.println("95% confidence interval"  + " = " + percolationStats.confidenceLo() + ", " + percolationStats.confidenceHi());
       }
       else
       {
           throw new RuntimeException("Invalid arguments");
       }
    }
 }
