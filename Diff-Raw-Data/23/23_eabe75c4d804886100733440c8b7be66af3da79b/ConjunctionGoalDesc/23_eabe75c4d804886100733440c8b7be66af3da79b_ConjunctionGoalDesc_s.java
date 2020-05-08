 /**
  * PDDL Graph Planner
  * Zeyn Saigol <zeyn@zeyn.net>
  * School of Computer Science
  * University of Birmingham
  *
  * $Id: ConjunctionGoalDesc.java 33 2007-02-23 15:45:34Z zas $
  */
 package edu.usu.cs.pddl.goalseffects;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import edu.usu.cs.pddl.domain.ConsistentLiteralSet;
 import edu.usu.cs.pddl.domain.DefaultGoalDesc;
 import edu.usu.cs.pddl.domain.FalseGoal;
 import edu.usu.cs.pddl.domain.FormalArgument;
 import edu.usu.cs.pddl.domain.GoalDesc;
 import edu.usu.cs.pddl.domain.GoalUtils;
 import edu.usu.cs.pddl.domain.LiteralInstance;
 import edu.usu.cs.pddl.domain.MethodDef;
 import edu.usu.cs.pddl.domain.PDDLObject;
 import edu.usu.cs.pddl.domain.PredicateHeader;
 import edu.usu.cs.pddl.domain.TrueGoal;
 import edu.usu.cs.pddl.parser.DisjunctionGoalDesc;
 import edu.usu.cs.pddl.parser.ForAllGoalDesc;
 import edu.usu.cs.pddl.parser.InvalidPDDLElementException;
 import edu.usu.cs.pddl.parser.QuantifiedGoal;
 
 /**
  * An 'and' goal.
  */
 public class ConjunctionGoalDesc implements GoalDesc
 {
 	private final boolean evaluable;
 	private List<GoalDesc> subGoals;
 
 	public List<GoalDesc> getSubGoals() {
 		return subGoals;
 	}
 
 	public ConjunctionGoalDesc(List<GoalDesc> subGoals2) {
		this.subGoals = subGoals2;

 		boolean tempEval = true;
 		for (GoalDesc sg : subGoals2) {
 			if (!sg.isEvaluable()) {
 				tempEval = false;
 			}
 		}
 		evaluable = tempEval;
 	}
 
 	public boolean isEvaluable() {
 		return evaluable;
 	}
 
 	public GoalDesc instantiate(Map<FormalArgument, PDDLObject> parameters, Set<PDDLObject> objects) {
 		if (evaluable) {
 			return this;
 		} else {
 			List<GoalDesc> newSubs = new ArrayList<GoalDesc>(subGoals.size());
 			for (GoalDesc sg : subGoals) {
 				newSubs.add(sg.instantiate(parameters, objects));
 			}
 			return new ConjunctionGoalDesc(newSubs);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.usu.cs.pddl.domain.GoalDesc#evaluate(java.util.Collection)
 	 */
 	public boolean evaluate(ConsistentLiteralSet literals) {
 		for (GoalDesc sg : subGoals) {
 			if (!sg.evaluate(literals)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public void getLiteralsUsed(Set<LiteralInstance> resultSet)
 	{
 		if (!evaluable) {
 			throw new IllegalStateException("Attempt to get literals used by non-evaluable goal desc");
 		}
 		for (GoalDesc sg : subGoals) {
 			sg.getLiteralsUsed(resultSet);
 		}
 	}
 
 	public String toString() 
 	{
 		String sep = "";
 		StringBuffer result = new StringBuffer();
 		result.append("(and ");
 		for (GoalDesc goal : subGoals) {
 			result.append(sep).append(goal);
 			sep = " ";
 		}
 		result.append(")");
 		return result.toString();
 	}
 
 
 	public void getMethods(List<MethodDef> preconditionMethods) {
 		// TODO Auto-generated method stub
 		for(GoalDesc g : subGoals)
 			g.getMethods(preconditionMethods);
 	}
 
 
 	public boolean notSatisfiedBy(
 			Map<FormalArgument, PDDLObject> partialArgMap,
 			ConsistentLiteralSet startState,
 			Set<PDDLObject> allObjects) {
 		//if any precondition is not satisfied then true, else false
 		for(GoalDesc sg :subGoals){
 			if(sg.notSatisfiedBy(partialArgMap, startState, allObjects)){
 				return true;
 			}
 		}
 		return false;
 	}
 
 		
 	@Override
 	public GoalDesc toDNF(Map<FormalArgument, PDDLObject> quantifiedVariableMap, Set<PDDLObject> objects, ConsistentLiteralSet startState) {
 		// TODO Auto-generated method stub
 		
 		List<GoalDesc> dnfConjuncts = new ArrayList<GoalDesc>();
 		for(GoalDesc c : this.subGoals){
 			GoalDesc g = c.toDNF(quantifiedVariableMap, objects, startState);
 			if(g instanceof FalseGoal){
 				return g;
 			}
 			else if(g instanceof TrueGoal){
 				//dnfConjuncts.add(g);
 			}
 			else{
 				dnfConjuncts.add(g);
 			}
 		}
 		
 		if(dnfConjuncts.size() == 0){
 			return new TrueGoal();
 		}
 		
 		//		(and
 		//		(or (and) (and))
 		//		(or (and) (and))
 		//		)
 		List<GoalDesc> dnfConjunctsWithoutQuantifiers = dnfConjuncts; 
 			//new ArrayList<GoalDesc>();
 //		GoalDesc quantifiers = GoalUtils.stripAndStandardizeQuantifiers(dnfConjuncts, dnfConjunctsWithoutQuantifiers);
 		
 		//distribute or over and		
 		while(dnfConjunctsWithoutQuantifiers.size() > 1){
 			//result of distribution
 			List<GoalDesc> nextDnfConjuncts = new ArrayList<GoalDesc>();
 
 			//distribute d
 			GoalDesc d = dnfConjunctsWithoutQuantifiers.remove(0);
 
 			//distribute each clause of d 
 			List<GoalDesc> dClauses = new ArrayList<GoalDesc>();
 			if(d instanceof DisjunctionGoalDesc){
 				dClauses.addAll(((DisjunctionGoalDesc) d).getDisjuncts());
 			}
 			else{
 				dClauses.add(d);
 			}
 
 
 			//over rest of dnfConjuncts
 			for(GoalDesc dnf : dnfConjunctsWithoutQuantifiers){
 				//result of distributing dc over dnf
 				List<GoalDesc> nextDNF = new ArrayList<GoalDesc>();
 
 			for(GoalDesc dc : dClauses){
 
 					
 					List<GoalDesc> dnfClauses = new ArrayList<GoalDesc>();
 					if(dnf instanceof DisjunctionGoalDesc){
 						dnfClauses.addAll(((DisjunctionGoalDesc) dnf).getDisjuncts());
 					}
 					else{
 						dnfClauses.add(dnf);
 					}
 					
 					
 					//add dc to each dnf clause
 					for(GoalDesc dClause : dnfClauses){
 						Set<GoalDesc> conjuncts = new HashSet<GoalDesc>();
 						if(dClause instanceof ConjunctionGoalDesc){
 							conjuncts.addAll(((ConjunctionGoalDesc) dClause).getSubGoals());
 						}
 						else {
 							conjuncts.add(dClause);
 						}
 						if(dc instanceof ConjunctionGoalDesc){
 							conjuncts.addAll(((ConjunctionGoalDesc) dc).getSubGoals());
 						}
 						else {
 							conjuncts.add(dc);
 						}
 						
 						
 						GoalDesc nextClause = new ConjunctionGoalDesc(new ArrayList<GoalDesc>(conjuncts));
 						nextDNF.add(nextClause);
 					}
 					
 				}
 				nextDnfConjuncts.add(new DisjunctionGoalDesc(nextDNF));
 			}
 			dnfConjunctsWithoutQuantifiers = nextDnfConjuncts;
 		}
 		
 		GoalDesc newDNF = dnfConjunctsWithoutQuantifiers.get(0);
 		GoalDesc result = newDNF;//GoalUtils.appendQuantifier(quantifiers, newDNF); //adds newDNF to end of quantifiers
 		return result;
 	}
 
 	
 
 	@Override
 	public void renameVariables(Map<FormalArgument, FormalArgument> nameMap) throws Exception {
 		for(GoalDesc g : subGoals){
 			g.renameVariables(nameMap);
 		}
 	}
 
 	@Override
 	public GoalDesc deepCopy() {
 		List<GoalDesc> conjuncts = new ArrayList<GoalDesc>();
 		for(GoalDesc g : subGoals){
 			conjuncts.add(g.deepCopy());
 		}
 		return new ConjunctionGoalDesc(conjuncts);
 	}
 
 
 
 
 	
 }
