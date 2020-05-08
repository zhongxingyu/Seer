 public class PercolationStats {
    private final double mean;
    private final double sigma;
    private final double confLo;
    private final double confHi;
 
     public PercolationStats(int N, int T){
         if (N <= 0) throw new IllegalArgumentException("N <= 0");
         if (T <= 0) throw new IllegalArgumentException("T <= 0");
         double times = T;
         double factor = N * N;
         double results[] = new double[T];
 
         double sum = 0;
         for (int k = 0; k < T; k++) {
             Percolation p = new Percolation(N);
             int c = 0;
             while(!p.percolates()) {
                 int i = StdRandom.uniform(1, N+1);
                 int j = StdRandom.uniform(1, N+1);
                 if (!p.isOpen(i, j)) {
                     p.open(i, j);
                     c++;
                 }
             }
             double v = c / factor;
             results[k] = v;
             sum += v;
         }
         mean = sum / times;
 
         double sumsq = 0;
         for (int k = 0; k < T; k++) {
             double v = (mean - results[k]);
             sumsq += v*v;
         }
         sigma = sumsq / (times-1);
 
         double x = 1.96*sigma/Math.sqrt(T);
         confLo = mean - x;
         confHi = mean + x;
     }
     public double mean() {
         return mean;
     }
     public double stddev() {
         return sigma;
     }
     public double confidenceLo() {
         return confLo;
     }
     public double confidenceHi() {
         return confHi;
     }
     public static void main(String[] args) {
         PercolationStats stats = new PercolationStats(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
         StdOut.println("mean\t\t\t\t\t= " + stats.mean());
         StdOut.println("stddev\t\t\t\t\t= " + stats.stddev());
         StdOut.println("95% confidence interval = " + stats.confidenceLo() + ", " + stats.confidenceHi());
     }
 }
