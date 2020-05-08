 package org.clafer.compiler;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import org.clafer.ast.AstConstraint;
 import org.clafer.common.Check;
 import org.clafer.collection.Pair;
 import org.clafer.instance.InstanceModel;
 import solver.ResolutionPolicy;
 import solver.Solver;
 import solver.constraints.ICF;
 import solver.variables.BoolVar;
 import solver.variables.IntVar;
 import util.ESat;
 
 /**
  * Either call {@link #minUnsat()} xor {@link #unsatCore()} at most once. If you
  * need to invoke both, you need to two ClaferUnsat objects.
  *
  * @author jimmy
  */
 public class ClaferUnsat {
 
     private final Solver solver;
     private final ClaferSolutionMap solutionMap;
     private final Pair<AstConstraint, BoolVar>[] softVars;
     private final IntVar score;
 
     ClaferUnsat(Solver solver, ClaferSolutionMap solutionMap, Pair<AstConstraint, BoolVar>[] softVars, IntVar score) {
         this.solver = Check.notNull(solver);
         this.solutionMap = Check.notNull(solutionMap);
         this.softVars = Check.noNulls(softVars);
         this.score = Check.notNull(score);
     }
 
     public Solver getInternalSolver() {
         return solver;
     }
 
     /**
      * Compute the minimal set of constraints that need to be removed before the
      * model is satisfiable. If the model is already satisfiable, then the set
      * is empty. Guaranteed to be minimum.
      *
      * @return the Min-Unsat and the corresponding near-miss example
      */
     public Pair<Set<AstConstraint>, InstanceModel> minUnsat() {
         if (ESat.TRUE.equals(maximize())) {
             Set<AstConstraint> unsat = new HashSet<AstConstraint>();
             for (Pair<AstConstraint, BoolVar> softVar : softVars) {
                 if (softVar.getSnd().instantiatedTo(0)) {
                     unsat.add(softVar.getFst());
                 }
             }
             return new Pair<Set<AstConstraint>, InstanceModel>(unsat, solutionMap.getInstance());
         }
         return null;
     }
 
     /**
      * Compute a small set of constraints that are mutually unsatisfiable.
      * Undefined behaviour if the model is satisfiable. This method is always
      * slower to compute than {@link #minUnsat()}. Not guaranteed to be minimum.
      *
     * @return the Min-Unsat-Core
      */
     public Set<AstConstraint> unsatCore() {
         Set<AstConstraint> unsat = new HashSet<AstConstraint>();
         boolean changed = true;
         while (changed && ESat.TRUE.equals(maximize())) {
             changed = false;
             List<BoolVar> minUnsat = new ArrayList<BoolVar>();
             for (Pair<AstConstraint, BoolVar> softVar : softVars) {
                 if (softVar.getSnd().instantiatedTo(0)) {
                     changed |= unsat.add(softVar.getFst());
                     minUnsat.add(softVar.getSnd());
                 }
             }
             solver.getSearchLoop().reset();
             for (BoolVar var : minUnsat) {
                 solver.postCut(ICF.arithm(var, "=", 1));
             }
         }
         return unsat;
     }
 
     private ESat maximize() {
         solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, score);
         return solver.isFeasible();
     }
 }
