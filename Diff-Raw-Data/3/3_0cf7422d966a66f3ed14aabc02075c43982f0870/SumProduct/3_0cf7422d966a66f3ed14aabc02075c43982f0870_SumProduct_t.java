 package com.mikea.bayes;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Set;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Sets.newHashSet;
 import static com.mikea.bayes.VarSet.newVarSet;
 
 /**
  * @author mike.aizatsky@gmail.com
  */
 public class SumProduct {
     public static Factor sumProductVariableElimination(
             Iterable<Var> vars,
             List<Factor> factors) {
         return sumProductVariableElimination(vars, factors, new MinNeighborsStrategy());
     }
 
     public static Factor sumProductVariableElimination(
             Iterable<Var> vars,
             List<Factor> factors,
             VarOrderStrategy strategy) {
         return sumProductVariableElimination(ProbabilitySpace.get(vars), vars, factors, strategy);
     }
 
     private static Factor sumProductVariableElimination(ProbabilitySpace space, Iterable<Var> vars, List<Factor> factors, VarOrderStrategy strategy) {
         Set<Var> varSet = newHashSet(vars);
         while (!varSet.isEmpty()) {
             Var var = strategy.pickVar(space, varSet, factors);
             factors = sumProductEliminateVar(factors, var);
             varSet.remove(var);
         }
 
         return Factor.product(factors);
     }
 
     private static List<Factor> sumProductEliminateVar(List<Factor> factors, Var var) {
         List<Factor> result = newArrayList();
         List<Factor> product = newArrayList();
         for (Factor factor : factors) {
             VarSet scope = factor.getScope();
             if (scope.contains(var)) {
                 product.add(factor);
             } else {
                 result.add(factor);
             }
         }
 
         result.add(Factor.product(product).marginalize(newVarSet(var)));
         return result;
     }
 
     public static interface VarOrderStrategy {
         Var pickVar(ProbabilitySpace space, Set<Var> vars, List<Factor> factors);
     }
 
    // todo: add min-fill & weighted-min-fill strategies.
     public static abstract class GreedyOrderStrategy implements VarOrderStrategy {
         public abstract void computeCosts(int[] costs, Set<Var> vars, List<Factor> factors);
 
         @Override
         public Var pickVar(ProbabilitySpace space, Set<Var> vars, List<Factor> factors) {
             int[] costs = new int[space.getNumVars()];
             Arrays.fill(costs, 1);
 
             computeCosts(costs, vars, factors);
 
 
             Var minVar = null;
             int minCost = Integer.MAX_VALUE;
 
             for (Var var : vars) {
                 int cost = costs[var.getIndex()];
                 if (cost < minCost) {
                     minCost = cost;
                     minVar = var;
                 }
             }
 
             return checkNotNull(minVar);
         }
     }
 
     public static class MinNeighborsStrategy extends GreedyOrderStrategy {
         @Override
         public void computeCosts(int[] costs, Set<Var> vars, List<Factor> factors) {
             Multimap<Var, Var> neighbors = HashMultimap.create();
 
             for (Factor factor : factors) {
                 VarSet scope = factor.getScope();
 
                 for (Var var : scope) {
                     neighbors.putAll(var, scope);
                 }
             }
 
             for (Var var : neighbors.keySet()) {
                 costs[var.getIndex()] = neighbors.get(var).size();
             }
         }
     }
 
     public static class MinWeightStrategy extends GreedyOrderStrategy {
         @Override
         public void computeCosts(int[] costs, Set<Var> vars, List<Factor> factors) {
             for (Factor factor : factors) {
                 VarSet scope = factor.getScope();
                 int factorCardinality = scope.getCardinality();
 
                 for (Var var : scope) {
                     costs[var.getIndex()] *= factorCardinality;
                 }
             }
         }
     }
 }
