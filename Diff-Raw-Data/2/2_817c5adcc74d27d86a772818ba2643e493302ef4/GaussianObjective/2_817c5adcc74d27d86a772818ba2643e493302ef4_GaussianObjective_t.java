 package SigmaEC.evaluate;
 
 import SigmaEC.represent.DoubleVectorPhenotype;
 import SigmaEC.util.Misc;
 import java.util.Arrays;
 
 /**
  *
  * @author Eric 'Siggy' Scott
  * @author Jeff Bassett
  */
 public class GaussianObjective implements ObjectiveFunction<DoubleVectorPhenotype>
 {
     private final int numDimensions;
     private final double height;
     private final double[] mean;
     private final double std;
     
     public GaussianObjective(final int numDimensions, final double[] mean, final double std, final double height)
     {
         if (numDimensions < 1)
             throw new IllegalArgumentException("GaussianObjective: numDimensions is < 1.");
         if (height <= 0.0)
             throw new IllegalArgumentException("GaussianObjective: height is <= 0, must be positive.");
         if (mean.length != numDimensions)
             throw new IllegalArgumentException("GaussianObjective: length of mean vector must match numDimensions.");
         if (!Misc.finiteValued(mean))
             throw new IllegalArgumentException("GaussianObjective: mean vector contains non-fininte values, must be finite.");
         if (std <= 0)
             throw new IllegalArgumentException("GaussianObjective: std is <= 0, must be positive.");
         if (std == Double.POSITIVE_INFINITY || Double.isNaN(std))
             throw new IllegalArgumentException("GaussianObjective: std is infinite, must be finite.");
         this.numDimensions = numDimensions;
         this.height = height;
         this.mean = Arrays.copyOf(mean, mean.length);
         this.std = std;
         assert(repOK());
     }
 
     @Override
     public int getNumDimensions()
     {
         return numDimensions;
     }
     
     @Override
     public double fitness(DoubleVectorPhenotype ind)
     {
         assert(ind.size() == numDimensions);
         double exponent = 0;
         for (double d : ind.getVector())
             exponent+= Math.pow(d, 2)/(2*Math.pow(std, 2));
         assert(repOK());
         return height*Math.exp(-exponent);
     }
 
     //<editor-fold defaultstate="collapsed" desc="Standard Methods">
     @Override
     final public boolean repOK()
     {
         return numDimensions > 0
                 && mean.length == numDimensions
                 && Misc.finiteValued(mean)
                 && std != Double.POSITIVE_INFINITY
                && !Double.isNaN(std)
                 && std > 0;
     }
 
     @Override
     public String toString()
     {
         return String.format("[GaussianObjective: NumDimensions=%d, mean=%s, std=%f, height=%f]", numDimensions, Arrays.toString(mean), std, height);
     }
     
     @Override
     public boolean equals(Object o)
     {
         if (!(o instanceof GaussianObjective))
             return false;
         
         final GaussianObjective cRef = (GaussianObjective) o;
         return numDimensions == cRef.numDimensions
                 && Misc.doubleEquals(height, cRef.height)
                 && Misc.doubleArrayEquals(mean, cRef.mean)
                 && Misc.doubleEquals(std, cRef.std);
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 59 * hash + this.numDimensions;
         hash = 59 * hash + (int) (Double.doubleToLongBits(this.height) ^ (Double.doubleToLongBits(this.height) >>> 32));
         hash = 59 * hash + Arrays.hashCode(this.mean);
         hash = 59 * hash + (int) (Double.doubleToLongBits(this.std) ^ (Double.doubleToLongBits(this.std) >>> 32));
         return hash;
     }
     //</editor-fold>
 }
