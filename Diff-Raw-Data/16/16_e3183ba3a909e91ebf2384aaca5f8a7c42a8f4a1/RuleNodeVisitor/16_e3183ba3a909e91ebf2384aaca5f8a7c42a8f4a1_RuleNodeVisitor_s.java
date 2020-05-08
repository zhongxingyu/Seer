 package uk.co.mtford.alp.abduction.rules.visitor;
 
 import uk.co.mtford.alp.abduction.Store;
 import uk.co.mtford.alp.abduction.logic.instance.*;
 import uk.co.mtford.alp.abduction.rules.*;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mtford
  * Date: 18/05/2012
  * Time: 06:44
  * To change this template use File | Settings | File Templates.
  */
 public abstract class RuleNodeVisitor {
 
     RuleNode currentRuleNode;
 
     public RuleNodeVisitor(RuleNode ruleNode) {
         currentRuleNode = ruleNode;
         ruleNode.acceptVisitor(this);
     }
 
     private RuleNode constructPositiveChildNode(IASystemInferableInstance newGoal, List<IASystemInferableInstance> newRestOfGoals,
                                                 RuleNode previousNode) {
         RuleNode newRuleNode = newGoal.getPositiveRootRuleNode(previousNode.getAbductiveFramework(), newRestOfGoals);
         Map<VariableInstance, IUnifiableAtomInstance> assignments = new HashMap<VariableInstance, IUnifiableAtomInstance>(previousNode.getAssignments());
         newRuleNode.setAssignments(assignments);
         newRuleNode.setStore(previousNode.getStore().shallowClone());
         return newRuleNode;
     }
 
     private RuleNode constructNegativeChildNode(IASystemInferableInstance newGoal, List<DenialInstance> nestedDenialList,
                                                 List<IASystemInferableInstance> newRestOfGoals, RuleNode previousNode) {
         newRestOfGoals = new LinkedList<IASystemInferableInstance>(newRestOfGoals);
         RuleNode newRuleNode = newGoal.getNegativeRootRuleNode(previousNode.getAbductiveFramework(), nestedDenialList, newRestOfGoals);
         Map<VariableInstance, IUnifiableAtomInstance> assignments = new HashMap<VariableInstance, IUnifiableAtomInstance>(previousNode.getAssignments());
         newRuleNode.setAssignments(assignments);
         newRuleNode.setStore(previousNode.getStore().shallowClone());
         return newRuleNode;
     }
 
     public void visit(A1RuleNode ruleNode) {
 
         // First branch.
         LinkedList<RuleNode> childNodes = new LinkedList<RuleNode>();
         Store store = ruleNode.getStore();
         PredicateInstance goalAbducible = (PredicateInstance) ruleNode.getCurrentGoal();
         LinkedList<RuleNode> firstBranchChildNodes = getA1FirstBranch(ruleNode, store, goalAbducible);
         childNodes.addAll(firstBranchChildNodes);
 
         // Second branch.
         RuleNode secondBranchChildNode = getA1SecondBranch(ruleNode, store, goalAbducible);
         secondBranchChildNode.getStore().abducibles.add(goalAbducible);
         childNodes.add(secondBranchChildNode);
         ruleNode.getChildren().addAll(childNodes);
 
     }
 
     private LinkedList<RuleNode> getA1FirstBranch(A1RuleNode ruleNode, Store store, PredicateInstance goalAbducible) {
         LinkedList<RuleNode> childNodes2 = new LinkedList<RuleNode>();
         for (PredicateInstance storeAbducible : store.abducibles) {
             if (goalAbducible.isSameFunction(storeAbducible)) {
                 List<IASystemInferableInstance> equalitySolved = new LinkedList<IASystemInferableInstance>(storeAbducible.equalitySolve(goalAbducible, ruleNode.getAssignments()));
                 List<IASystemInferableInstance> newRestOfGoals = new LinkedList<IASystemInferableInstance>(ruleNode.getNextGoals());
                 IASystemInferableInstance newGoal = equalitySolved.remove(0);
                 newRestOfGoals.addAll(equalitySolved);
                 RuleNode childNode = constructPositiveChildNode(newGoal, newRestOfGoals, ruleNode);
                 childNodes2.add(childNode);
             }
         }
         return childNodes2;
     }
 
     private RuleNode getA1SecondBranch(A1RuleNode ruleNode, Store store, PredicateInstance goalAbducible) {
         RuleNode childNode;
         List<IASystemInferableInstance> newRestOfGoals = new LinkedList<IASystemInferableInstance>(ruleNode.getNextGoals());
         for (DenialInstance collectedDenial : store.denials) {
             PredicateInstance collectedDenialHead = (PredicateInstance) collectedDenial.getBody().get(0);
             if (collectedDenialHead.isSameFunction(goalAbducible)) {
                 DenialInstance newDenial = (DenialInstance) collectedDenial.deepClone(new HashMap<VariableInstance, IUnifiableAtomInstance>(ruleNode.getAssignments()));
                 collectedDenialHead = (PredicateInstance) newDenial.getBody().remove(0);
                 newDenial.getBody().addAll(goalAbducible.equalitySolve(collectedDenialHead, ruleNode.getAssignments()));
                 newRestOfGoals.add(0, newDenial);
             }
         }
         for (PredicateInstance storeAbducible : store.abducibles) {
             if (goalAbducible.isSameFunction(storeAbducible)) {
                 List<IEqualitySolverResultInstance> equalitySolved = goalAbducible.equalitySolve(storeAbducible, ruleNode.getAssignments());
                 for (IEqualitySolverResultInstance result : equalitySolved) {
                     newRestOfGoals.add(new NegationInstance(result));
                 }
             }
         }
 
         childNode = constructPositiveChildNode(newRestOfGoals.remove(0), newRestOfGoals, ruleNode);
         return childNode;
     }
 
     public void visit(A2RuleNode ruleNode) {
         List<IASystemInferableInstance> restOfGoals = new LinkedList<IASystemInferableInstance>(ruleNode.getNextGoals());
         List<DenialInstance> nestedDenialList = new LinkedList<DenialInstance>(ruleNode.getDenials());
         DenialInstance currentDenial = nestedDenialList.remove(0);
         PredicateInstance goalAbducible = (PredicateInstance) ruleNode.getCurrentGoal();
         currentDenial.getBody().add(0, goalAbducible);
         Store store = ruleNode.getStore();
         for (PredicateInstance storeAbducible : store.abducibles) {
             if (storeAbducible.isSameFunction(goalAbducible)) {
                 DenialInstance newDenial = (DenialInstance) currentDenial.deepClone(new HashMap<VariableInstance, IUnifiableAtomInstance>(ruleNode.getAssignments()));
                 PredicateInstance newDenialHead = (PredicateInstance) newDenial.getBody().remove(0);
                 List<IEqualitySolverResultInstance> equalitySolved = newDenialHead.equalitySolve(storeAbducible, ruleNode.getAssignments());
                 newDenial.getBody().addAll(equalitySolved);
                 if (nestedDenialList.isEmpty()) {
                     restOfGoals.add(0, newDenial);
                 } else {
                     nestedDenialList.get(0).getBody().add(0, newDenial);
                 }
             }
         }
         RuleNode childNode;
         if (nestedDenialList.isEmpty()) {
             IASystemInferableInstance newGoal = restOfGoals.remove(0);
             childNode = constructPositiveChildNode(newGoal, restOfGoals, ruleNode);
         } else {
             IASystemInferableInstance newGoal = nestedDenialList.get(0).getBody().remove(0);
             childNode = constructNegativeChildNode(newGoal, nestedDenialList, restOfGoals, ruleNode);
         }
         currentDenial = currentDenial.shallowClone();
         currentDenial.getBody().add(0, goalAbducible);
         childNode.getStore().denials.add(currentDenial);
         ruleNode.getChildren().add(childNode);
     }
 
    public void visit(D1RuleNode ruleNode) {

     }
 
     public void visit(D2RuleNode ruleNode) {
 
     }
 
     public void visit(E1RuleNode ruleNode) {
         List<IASystemInferableInstance> restOfGoals = new LinkedList<IASystemInferableInstance>(ruleNode.getNextGoals());
         Map<VariableInstance, IUnifiableAtomInstance> newAssignments = new HashMap<VariableInstance, IUnifiableAtomInstance>(ruleNode.getAssignments());
         EqualityInstance currentGoal = (EqualityInstance) ruleNode.getCurrentGoal();
         List<IEqualitySolverResultInstance> equalitySolved = currentGoal.getLeft().equalitySolve(currentGoal.getRight(), newAssignments);
         restOfGoals.addAll(equalitySolved);
         IASystemInferableInstance newGoalNode = restOfGoals.remove(0);
         RuleNode childNode = constructPositiveChildNode(newGoalNode, restOfGoals, ruleNode);
         childNode.setAssignments(newAssignments);
         ruleNode.getChildren().add(childNode);
     }
 
     public void visit(E2RuleNode ruleNode) {
 
     }
 
     public void visit(N1RuleNode ruleNode) {
         List<IASystemInferableInstance> newRestOfGoals = new LinkedList<IASystemInferableInstance>(ruleNode.getNextGoals());
         NegationInstance goal = (NegationInstance) ruleNode.getCurrentGoal();
         DenialInstance denial = new DenialInstance(goal.getSubFormula());
         RuleNode childNode = constructPositiveChildNode(denial, newRestOfGoals, ruleNode);
         ruleNode.getChildren().add(childNode);
     }
 
     public void visit(N2RuleNode ruleNode) {
         RuleNode childNode;
         LinkedList<RuleNode> childNodes = new LinkedList<RuleNode>();
         List<IASystemInferableInstance> newRestOfGoals = new LinkedList<IASystemInferableInstance>(ruleNode.getNextGoals());
         NegationInstance goal = (NegationInstance) ruleNode.getCurrentGoal();
         List<DenialInstance> newNestedDenialList = new LinkedList<DenialInstance>(ruleNode.getDenials());
         DenialInstance currentDenial = newNestedDenialList.remove(0).shallowClone();
         if (newNestedDenialList.isEmpty()) {
             childNode = constructPositiveChildNode(goal.getSubFormula(), newRestOfGoals, ruleNode);
 
         } else {
             childNode = constructNegativeChildNode(goal.getSubFormula(), newNestedDenialList, newRestOfGoals, ruleNode);
         }
         childNodes.add(childNode);
         // OR
         newRestOfGoals = new LinkedList<IASystemInferableInstance>(ruleNode.getNextGoals());
         if (newNestedDenialList.isEmpty()) {
             newRestOfGoals.add(0, currentDenial);
             childNode = constructPositiveChildNode(goal, newRestOfGoals, ruleNode);
         } else {
             DenialInstance nestedDenial = newNestedDenialList.remove(0).shallowClone();
             nestedDenial.getBody().add(0, currentDenial);
             nestedDenial.getBody().add(0, goal);
             newNestedDenialList.add(0, nestedDenial);
             childNode = constructNegativeChildNode(goal, newNestedDenialList, newRestOfGoals, ruleNode);
         }
         childNodes.add(childNode);
         ruleNode.getChildren().addAll(childNodes);
     }
 
     /**
      * Produces one child node where the true instance is removed from the goal stack.
      *
      * @param ruleNode
      */
     public void visit(PositiveTrueRuleNode ruleNode) {
         List<IASystemInferableInstance> newRestOfGoals = new LinkedList<IASystemInferableInstance>(ruleNode.getNextGoals());
         IASystemInferableInstance newGoal = newRestOfGoals.remove(0);
         RuleNode newRuleNode = constructPositiveChildNode(newGoal, newRestOfGoals, ruleNode);
         ruleNode.getChildren().add(newRuleNode);
     }
 
     /**
      * Produces one child node where the true instance is removed from the denial.
      *
      * @param ruleNode
      */
     public void visit(NegativeTrueRuleNode ruleNode) {
         List<IASystemInferableInstance> newRestOfGoals = new LinkedList<IASystemInferableInstance>(ruleNode.getNextGoals());
         List<DenialInstance> newNestedDenialList = new LinkedList<DenialInstance>(ruleNode.getDenials());
         DenialInstance currentDenialInstance = newNestedDenialList.get(0);
         IASystemInferableInstance newGoal;
         if (!currentDenialInstance.getBody().isEmpty()) {
             newGoal = currentDenialInstance.getBody().remove(0);
         } else {
             newNestedDenialList.remove(0);
             newGoal = new FalseInstance();
         }
         RuleNode newChildNode = constructNegativeChildNode(newGoal, newNestedDenialList, newRestOfGoals, ruleNode);
         ruleNode.getChildren().add(newChildNode);
     }
 
     /**
      * Fails the node.
      *
      * @param ruleNode
      */
     public void visit(PositiveFalseRuleNode ruleNode) {
         ruleNode.setNodeMark(RuleNode.NodeMark.FAILED_MARK);
     }
 
     /**
      * Produces one child node whereby the denials succeeds.
      *
      * @param ruleNode
      */
     public void visit(NegativeFalseRuleNode ruleNode) {
         List<IASystemInferableInstance> newRestOfGoals = new LinkedList<IASystemInferableInstance>(ruleNode.getNextGoals());
         List<DenialInstance> newNestedDenialList = new LinkedList<DenialInstance>(ruleNode.getDenials());
         RuleNode newChildNode;
         newNestedDenialList.remove(0);
         if (newNestedDenialList.isEmpty()) {
             IASystemInferableInstance newGoal = newRestOfGoals.remove(0);
             newChildNode = constructPositiveChildNode(newGoal, newRestOfGoals, ruleNode);
         } else {
             DenialInstance newDenial = newNestedDenialList.remove(0).shallowClone();
             IASystemInferableInstance newGoal = newDenial.getBody().remove(0);
             newChildNode = constructNegativeChildNode(newGoal, newNestedDenialList, newRestOfGoals, ruleNode);
         }
         ruleNode.getChildren().add(ruleNode);
     }
 
     /**
      * Explores a new node based on exploration strategy defined in chooseNextNode. Then proceeds to expand that
      * node to generate it's children using the rules defined above.
      *
      * @return
      */
     public RuleNode stateRewrite() {
         RuleNode nextNode = chooseNextNode();
         if (nextNode == null) {  // Finished.
             return null;
         }
         if (nextNode.getNextGoals().isEmpty()) { // Success.
             nextNode.setNodeMark(RuleNode.NodeMark.SUCCEEDED_MARK);
             nextNode = chooseNextNode();
         }
         currentRuleNode = nextNode;
         nextNode.acceptVisitor(this); // Choose the correct rule to apply.
         return nextNode;
     }
 
     protected abstract RuleNode chooseNextNode();
 }
