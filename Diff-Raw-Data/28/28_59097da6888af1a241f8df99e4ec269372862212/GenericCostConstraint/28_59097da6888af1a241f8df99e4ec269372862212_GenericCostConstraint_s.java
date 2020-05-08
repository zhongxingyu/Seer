 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.venky.csfj.solver;
 
 import java.util.List;
 
 import com.venky.core.util.Bucket;
 import com.venky.csfj.solver.variable.Variable;
 import com.venky.csfj.solver.variable.VariableAssignment;
 
 /**
  *
  * @author venky
  */
 public class GenericCostConstraint<V extends Variable<DT>,DT> implements Constraint<V,DT>{
     private final Problem<V,DT> problem; 
     public GenericCostConstraint(Problem<V,DT> problem){
         this.problem = problem;
     }
     private Double minCost = null; 
     
     public void setMinCost(double minCost){
         this.minCost = minCost;
     }
     
 
     
     public void propagate(VariableAssignment<V, DT> workingAssignment, List<VariableAssignment<V, DT>> assigned, List<VariableAssignment<V, DT>> unassigned) throws ConstraintViolationException {
        if (minCost == null){
            return;
        }
         VariableAssignment<V, DT> lastAssignment = null; 
         Bucket costSoFar = null ;
         if (!assigned.isEmpty()) {
         	lastAssignment = assigned.get(assigned.size()-1);
         	costSoFar = (Bucket)lastAssignment.getAttribute("costSoFar");
     		costSoFar = costSoFar.clone();
         }else {
         	costSoFar = new Bucket(0);
         }
         
         costSoFar.increment(problem.getCost(workingAssignment));
        if (costSoFar.value() >= minCost){
            throw new ConstraintViolationException("Cost must be less than minimum Cost");
         }
         workingAssignment.setAttribute("costSoFar", costSoFar);
         
        if (unassigned.isEmpty()){
            Solution<V,DT> partial = new Solution<V, DT>(this.problem); 
            partial.merge(assigned);
            partial.merge(workingAssignment);
            if (problem.getCost(partial) >= minCost){
                throw new ConstraintViolationException("Cost must be less than minimum Cost");
            }
        }
     }
     
 }
