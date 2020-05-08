 package SigmaEC.evaluate;
 
 import SigmaEC.represent.Individual;
 
 /**
  * A decorator that turns a maximization problem into a minimization problem and
  * vice versa.
  * 
  * @author Eric 'Siggy' Scott
  */
 public class InvertedObjective<T extends Individual> implements ObjectiveFunction<T>
 {
     final private ObjectiveFunction<? super T> objective;
     
     public InvertedObjective(ObjectiveFunction<? super T> objective)
     {
         if (objective == null)
            throw new IllegalArgumentException("MemoizedObjective: objective was null.");
         this.objective = objective;
         assert(repOK());
     }
 
     @Override
     public double fitness(T ind)
     {
         return -objective.fitness(ind);
     }
     
     // <editor-fold defaultstate="collapsed" desc="Standard Methods">
     @Override
     final public boolean repOK()
     {
         return objective != null
                 && objective.repOK();
     }
     
     @Override
     public String toString()
     {
         return String.format("[InvertedObjective: Objective=%s]", objective.toString());
     }
     
     @Override
     public boolean equals(Object o)
     {
         if (o == this)
             return true;
         if (!(o instanceof InvertedObjective))
             return false;
         
         InvertedObjective cRef = (InvertedObjective) o;
         return objective.equals(cRef.objective);
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 79 * hash + (this.objective != null ? this.objective.hashCode() : 0);
         return hash;
     }
     //</editor-fold>
     
 }
