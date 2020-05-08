 package SigmaEC.evaluate;
 
 import SigmaEC.represent.DoubleVectorIndividual;
 
 /**
  * An n-dimensional generalization of Rosenbrock's objective function:
  * f(x) = \sum_i^{n-1} 100(x_i^2 - x_{i+1})^2 + (1 - x_i)^2.  Traditionally,
  * each variable is bound between -2.048 and 2.048 (inclusive).
  * 
  * @author Eric 'Siggy' Scott
  */
 public class RosenbrockObjective implements ObjectiveFunction<DoubleVectorIndividual>, Dimensioned
 {
     private final int numDimensions;
     
     public RosenbrockObjective(int numDimensions)
     {
         if (numDimensions < 1)
             throw new IllegalArgumentException("RosenbrockObjective: numDimensions is < 1.");
         if (numDimensions == Double.POSITIVE_INFINITY)
             throw new IllegalArgumentException("RosenbrockObjective: numDimensions is infinite, must be finite.");
         this.numDimensions = numDimensions;
         assert(repOK());
     }
 
     @Override
     public int getNumDimensions()
     {
         return numDimensions;
     }
     
     @Override
     public double fitness(DoubleVectorIndividual ind)
     {
         assert(ind.size() == numDimensions);
         double sum = 0;
         for(int i = 0; i < ind.size() - 1; i++)
            sum+= 100*(ind.getElement(i+1) - Math.pow(ind.getElement(i), 2)) + Math.pow((1 - ind.getElement(i)), 2);
         return sum;
     }
 
     //<editor-fold defaultstate="collapsed" desc="Standard Methods">
     @Override
     final public boolean repOK()
     {
         return numDimensions > 0;
     }
     
     @Override
     public String toString()
     {
         return String.format("[RosenbrockObjective: NumDimensionts=%f]", numDimensions);
     }
     
     @Override
     public boolean equals(Object o)
     {
         if (!(o instanceof RosenbrockObjective))
             return false;
         
         RosenbrockObjective cRef = (RosenbrockObjective) o;
         return numDimensions == cRef.numDimensions;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 97 * hash + this.numDimensions;
         return hash;
     }
     //</editor-fold>
 }
