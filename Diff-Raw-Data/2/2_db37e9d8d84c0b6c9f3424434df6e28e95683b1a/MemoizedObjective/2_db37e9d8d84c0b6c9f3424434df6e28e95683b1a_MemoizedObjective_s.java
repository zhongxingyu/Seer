 package SigmaEC.evaluate;
 
 import SigmaEC.represent.Individual;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Decorates an ObjectiveFunction with memoization -- i.e. caches the results of
  * previous evaluations.  This is effective if evaluating the objective is
  * significantly more expensive than evaluating the Individual's hashcode()
  * method.
  * 
  * @author Eric 'Siggy' Scott
  */
 public class MemoizedObjective<T extends Individual> implements ObjectiveFunction<T>
 {
     final private ObjectiveFunction<T> objective;
     final private Map<T, Double> memory = new HashMap<T, Double>();
     
     public MemoizedObjective(ObjectiveFunction<T> objective)
     {
         if (objective == null)
            throw new IllegalArgumentException("BoundedDoubleObjective: objective was null.");
         this.objective = objective;
         assert(repOK());
     }
     @Override
     public double fitness(T ind)
     {
         assert(ind != null);
         if (memory.containsKey(ind))
             return memory.get(ind);
         else
         {
             double fitness = objective.fitness(ind);
             memory.put(ind, fitness);
             return fitness;
         }
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
         return String.format("[MemoizedObjective: Objective=%s]", objective.toString());
     }
     
     @Override
     public boolean equals(Object o)
     {
         if (o == this)
             return true;
         if (!(o instanceof MemoizedObjective))
             return false;
         
         MemoizedObjective cRef = (MemoizedObjective) o;
         return objective.equals(cRef.objective);
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 23 * hash + (this.objective != null ? this.objective.hashCode() : 0);
         return hash;
     }
     //</editor-fold>
 }
