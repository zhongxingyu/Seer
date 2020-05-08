 package org.kevoree.genetic.framework.internal;
 
 import org.kevoree.ContainerRoot;
 import org.kevoree.genetic.framework.KevoreePopulationFactory;
 import org.moeaframework.core.Initialization;
 import org.moeaframework.core.Problem;
 import org.moeaframework.core.Solution;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: duke
  * Date: 12/02/13
  * Time: 17:21
  */
 public class KevoreeInitialization implements Initialization {
 
     private KevoreePopulationFactory factory = null;
     private Problem problem = null;
 
     public KevoreeInitialization(KevoreePopulationFactory _factory, Problem _problem) {
         factory = _factory;
         problem = _problem;
     }
 
     @Override
     public Solution[] initialize() {
         List<ContainerRoot> models = factory.createPopulation();
         Solution[] results = new Solution[models.size()];
         for (int i = 0; i < models.size(); i++) {
            Solution s = new Solution(_problem.getNumberOfVariables(), _problem.getNumberOfObjectives());
             results[i] = s;
 
         }
         return results;
     }
 }
