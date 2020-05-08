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
      int e1, e2 = problem.evaluate(solution);
      do {
         e1 = e2;
          mutation = mutator.generateMutation(solution);
          mutator.doMutation(solution, mutation);
         e2 = problem.evaluate(solution);
       }
      while(e2 < e1);

      // Undo last mutation
      mutator.undoMutation(solution, mutation);
 
       return solution;
    }
 }
