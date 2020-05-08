 /*
  * DvRLib - Local search
  * Duncan van Roermund, 2010
  * HillClimbingLS.java
  */
 
 package dvrlib.localsearch;
 
 public class HillClimbingLS extends LocalSearch {
    @Override
    public Solution search(Problem problem, Solution solution) {
       Mutator mutator = problem.getMutator();
       Object mutation = null;
 
       // Keep mutating as long as it improves the solution
      for(double e1 = Double.NEGATIVE_INFINITY, e2 = problem.evaluate(solution); e1 < e2; e1 = e2, e2 = problem.evaluate(solution)) {
          mutation = mutator.generateMutation(solution);
          mutator.doMutation(solution, mutation);
       }
      if(mutation != null) {
         // Undo last mutation
         mutator.undoMutation(solution, mutation);
      }
 
       return solution;
    }
 }
