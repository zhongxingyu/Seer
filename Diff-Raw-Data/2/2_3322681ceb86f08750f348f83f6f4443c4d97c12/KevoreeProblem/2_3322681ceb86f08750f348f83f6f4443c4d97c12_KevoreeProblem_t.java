 package org.kevoree.genetic.framework.internal;
 
 import org.kevoree.KevoreeFactory;
 import org.kevoree.genetic.framework.KevoreeFitnessFunction;
 import org.kevoree.impl.ContainerRootImpl;
 import org.kevoree.impl.DefaultKevoreeFactory;
 import org.moeaframework.core.Solution;
 import org.moeaframework.problem.AbstractProblem;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jbourcie
  * Date: 11/02/13
  * Time: 14:57
  */
 public class KevoreeProblem extends AbstractProblem {
 
     private List<KevoreeFitnessFunction> fitnesses = new ArrayList<KevoreeFitnessFunction>();
     private KevoreeFactory factory = new DefaultKevoreeFactory();
 
     public KevoreeProblem(int numberOfObjectives) {
         // 1 variable = 1 Kevoree Variable
         // Number of Objectives = number of Objectives of the subproblem
         super(1, numberOfObjectives);
     }
 
     public List<KevoreeFitnessFunction> getFitnesses() {
         return fitnesses;
     }
 
    public void setFitnesses(List<KevoreeFitnessFunction> _fitnesses) {
         this.fitnesses = Collections.unmodifiableList(_fitnesses); //Set to immutable to protect the use of index
     }
 
     @Override
     public void evaluate(Solution solution) {
         for (int i = 0; i < fitnesses.size(); i++) {
             KevoreeVariable var = (KevoreeVariable) solution.getVariable(0);
             double result = fitnesses.get(0).evaluate(var.getModel());
             solution.setObjective(i, result);
         }
     }
 
     @Override
     public Solution newSolution() {
         Solution solution = new Solution(1, numberOfObjectives);
         solution.setVariable(0, new KevoreeVariable(new ContainerRootImpl()));
         return solution;
     }
 }
