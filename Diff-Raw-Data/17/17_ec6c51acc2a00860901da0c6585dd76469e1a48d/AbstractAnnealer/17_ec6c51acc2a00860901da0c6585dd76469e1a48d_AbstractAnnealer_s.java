 import edu.rit.util.Random;
 
 public abstract class AbstractAnnealer{
   protected int numPoints;
   protected double[] lb;
   protected double[] ub;
   protected int dim; 
   protected Function f;
 
  public static int SEED = 123;
 
   protected double[][] xOptHist;
   protected double[]   fOptHist;
 
   protected double[] xOpt;
  protected double fOpt;
 
   protected Random prng;
 
  public AbstractAnnealer( final int numPoints,
                              final int dim,
                              final double[] lb,
                              final double[] ub,
                              final Function f ){
     this.numPoints = numPoints;
     this.dim = dim;
     this.lb = lb;
     this.ub = ub;
     this.f = f;
     this.prng = Random.getInstance( SEED );
 
     this.xOptHist = new double[ numPoints ][ dim ];
     this.fOptHist = new double[ numPoints ];
   }
 
   abstract public void run();
 
   protected double boundedRandom( double min, double max ){
     return min + ( max - min ) * prng.nextDouble( );
   }
 
   public double[] getSolution(){
     return xOpt;
   }
   public double getValue(){
     return fOpt;
   }
 
                  
 }
