 
 public class AnnealerFactory{
   public static int MAXITER = 1000000;
   public static int MAXSTEP = 20;
   public static int MAXTEMP = 100;
   public static int LASTSTEP = 4;
   public static double INITTEMP = 1E6;
   public static double EPSILON = 1E-6;
   public static double RATE = 0.995;
   public static double STEPSIZE = 0.001;
  public static long SEED = System.nanoTime();
 
   public static SimulatedAnnealer getAnnealer( final int dim, 
                                                 final double[] lb,
                                                 final double[] ub,
                                                 final double[] x0,
                                                 final ObjectiveFunction obj ){
     double[] V = new double[ dim ];
     for( int i = 0; i < dim; i++ ){
       V[i] = STEPSIZE;
     }
     return new SimulatedAnnealer(
           dim,
           MAXITER,
           MAXSTEP,                    // # of steps before updating step length
           Math.max(MAXTEMP, 5* dim),  // # of steps before updating temp
           LASTSTEP,                   // # of steps in considering convergence
           EPSILON,                    // Convergence tol
           SEED,                       // Seed for PRNG
           x0,                         // Initial guess
           lb,                         // lower bound
           ub,                         // upper bound
           V,                          // Step vector
           INITTEMP,                   // initial temp
           RATE,                       // cooling rate
           obj                         // The objective function
           );
 
   }
                                                 
   private AnnealerFactory(){}
 }
