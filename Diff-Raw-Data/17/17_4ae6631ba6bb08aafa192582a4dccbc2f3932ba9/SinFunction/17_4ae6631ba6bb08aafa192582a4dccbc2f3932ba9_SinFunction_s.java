 // Oliver Kullmann, 8/10/2009 (Swansea)
 
 class SinFunction {
     public static void main(final String[] args) {
         if (args.length == 0) {
             System.err.println("ERROR[SinFunction]: The number of intervals is needed as parameter.");
            return;
         }
        int N;
         try { N = Integer.parseInt(args[0]); }
         catch(RuntimeException e) {
             System.err.println("ERROR[SinFunction]: The parameter must be an integer.");
            return;
         }
         if (N <= 0) {
             System.err.println("ERROR[SinFunction]: The parameter must positive.");
            return;
         }
 
         final double alpha = 4;
         final double beta = 20;
         double[] x = new double[N+1];
         double[] y = new double[N+1];
         for (int i = 0; i <= N; ++i) {
             x[i] = Math.PI * i/N;
             y[i] = Math.sin(alpha*x[i]) + Math.sin(beta*x[i]);
         }
 
         StdDraw.setXscale(0,Math.PI);
         StdDraw.setYscale(-2,2);
         for (int i = 1; i <= N; ++i)
             StdDraw.line(x[i-1], y[i-1], x[i], y[i]);
     }
 }
