 package SigmaEC;
 
 import SigmaEC.represent.Individual;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * A main evolution loop
  * 
  * @author Eric 'Siggy' Scott
  */
 public interface CircleOfLife<T extends Individual>
 {
     /** Takes a population of individuals and evolves them for a number of
      *  generations.
      */
     public abstract List<T> evolve(int run, List<T> population, int generations) throws IOException;
     
     /** Representation invariant.  If this returns false, there is something
     * invalid about the Individual's internal state. */
     public abstract boolean repOK();
 }
