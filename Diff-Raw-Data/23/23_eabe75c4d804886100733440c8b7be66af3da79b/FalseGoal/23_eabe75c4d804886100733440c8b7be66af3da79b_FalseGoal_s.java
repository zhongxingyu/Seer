 package edu.usu.cs.pddl.domain;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class FalseGoal implements GoalDesc {
 
 	@Override
 	public GoalDesc deepCopy() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean evaluate(ConsistentLiteralSet literals) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void getLiteralsUsed(Set<LiteralInstance> resultSet) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void getMethods(List<MethodDef> preconditionMethods) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public GoalDesc instantiate(Map<FormalArgument, PDDLObject> parameters,
 			Set<PDDLObject> objects) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean isEvaluable() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean notSatisfiedBy(
 			Map<FormalArgument, PDDLObject> partialArgMap,
 			ConsistentLiteralSet startState, Set<PDDLObject> allObjects) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void renameVariables(Map<FormalArgument, FormalArgument> nameMap)
 			throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public GoalDesc toDNF(Map<FormalArgument, PDDLObject> quantifiedVariableMap, Set<PDDLObject> objects, ConsistentLiteralSet startState) {
 		// TODO Auto-generated method stub
		return null;
 	}
 
 }
