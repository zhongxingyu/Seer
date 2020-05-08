 package edu.usu.cs.pddl.parser;
 
 import java.util.ArrayList;
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
 import edu.usu.cs.pddl.domain.LiteralOperation;
 import edu.usu.cs.pddl.domain.MethodDef;
 import edu.usu.cs.pddl.domain.PDDLObject;
 import edu.usu.cs.pddl.domain.TrueGoal;
 import edu.usu.cs.pddl.goalseffects.ConjunctionGoalDesc;
 
 public class DisjunctionGoalDesc  implements GoalDesc {
 
 	private final List<GoalDesc> disjuncts;
 	
 	public DisjunctionGoalDesc(List<GoalDesc> subGoals) {
		this.disjuncts = new ArrayList<GoalDesc>();
		for(GoalDesc g : subGoals)
			this.disjuncts.add(g.deepCopy());
 	}
 
 	@Override
 	public boolean evaluate(ConsistentLiteralSet literals) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void getMethods(List<MethodDef> preconditionMethods) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean notSatisfiedBy(
 			Map<FormalArgument, PDDLObject> partialArgMap,
 			ConsistentLiteralSet startState,
 			Set<PDDLObject> allObjects) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void getLiteralsUsed(Set<LiteralInstance> resultSet) {
 		// TODO Auto-generated method stub
 
 	}
 
 //	@Override
 //	public LiteralOperation instantiate(
 //			Map<FormalArgument, PDDLObject> parameters, Set<PDDLObject> objects) {
 //		// TODO Auto-generated method stub
 //		return null;
 //	}
 
 	@Override
 	public boolean isEvaluable() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public List<GoalDesc> getDisjuncts() {
 		// TODO Auto-generated method stub
 		return disjuncts;
 	}
 
 	@Override
 	public GoalDesc toDNF(Map<FormalArgument, PDDLObject> quantifiedVariableMap, Set<PDDLObject> objects, ConsistentLiteralSet startState) {
 		
 		List<GoalDesc> dnfDisjuncts = new ArrayList<GoalDesc>();
 		for(GoalDesc disjunct : disjuncts){
 			GoalDesc g = disjunct.toDNF(quantifiedVariableMap, objects,startState);
 			if(g instanceof FalseGoal){
 				//do nothing
 			}
 			else if (g instanceof TrueGoal){
 				return g;
 			}
 			else{
 			dnfDisjuncts.add(g);
 			}
 		}
 		List<GoalDesc> dnfDisjunctsWithoutQuantifiers = dnfDisjuncts;
 		//new ArrayList<GoalDesc>();
 		//GoalDesc quantifiers = GoalUtils.stripAndStandardizeQuantifiers(dnfDisjuncts, dnfDisjunctsWithoutQuantifiers);
 
 		//combine dnfs w/o quantifiers
 		List<GoalDesc> resultDisjuncts = new ArrayList<GoalDesc>();
 		for(GoalDesc d : dnfDisjunctsWithoutQuantifiers){
 			if(d instanceof DisjunctionGoalDesc){
 				resultDisjuncts.addAll(((DisjunctionGoalDesc) d).getDisjuncts());
 			}
 			else{
 				if(d instanceof FalseGoal){
 					//do nothing
 				}
 				else if (d instanceof TrueGoal){
 					return d;
 				}
 				else{
 				resultDisjuncts.add(d);
 				}
 			}
 			
 		}
 		GoalDesc newDNF = new DisjunctionGoalDesc(resultDisjuncts);
 		//add back quantifiers
 		GoalDesc result = newDNF;//GoalUtils.appendQuantifier(quantifiers, newDNF); //adds newDNF to end of quantifiers
 		return result;
 
 	}
 
 	@Override
 	public GoalDesc instantiate(Map<FormalArgument, PDDLObject> parameters,
 			Set<PDDLObject> objects) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void renameVariables(Map<FormalArgument, FormalArgument> nameMap) throws Exception {
 		for(GoalDesc g : disjuncts){
 			g.renameVariables(nameMap);
 		}
 
 		
 	}
 	
 
 	public String toString() 
 	{
 		String sep = "";
 		StringBuffer result = new StringBuffer();
 		result.append("(or ");
 		for (GoalDesc goal : disjuncts) {
 			result.append(sep).append(goal);
 			sep = " ";
 		}
 		result.append(")");
 		return result.toString();
 	}
 
 	@Override
 	public GoalDesc deepCopy() {
 		List<GoalDesc> d = new ArrayList<GoalDesc>();
 		for(GoalDesc g : disjuncts){
 			d.add(g.deepCopy());
 		}
 		return new DisjunctionGoalDesc(d);
 	}
 
 	
 }
