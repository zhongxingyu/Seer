 package uk.co.mtford.jalp.abduction.rules.visitor;
 
 import choco.kernel.model.constraints.Constraint;
 import org.apache.log4j.Logger;
 import uk.co.mtford.jalp.JALPException;
 import uk.co.mtford.jalp.abduction.DefinitionException;
 import uk.co.mtford.jalp.abduction.Store;
 import uk.co.mtford.jalp.abduction.logic.instance.*;
 import uk.co.mtford.jalp.abduction.logic.instance.constraints.ConstraintInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.constraints.IConstraintInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.constraints.InListConstraintInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.constraints.NegativeConstraintInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.equalities.EqualityInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.equalities.InEqualityInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.term.ListInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.term.ConstantInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.term.VariableInstance;
 import uk.co.mtford.jalp.abduction.rules.*;
 
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mtford
  * Date: 18/05/2012
  * Time: 06:44
  * To change this template use File | Settings | File Templates.
  */
 public abstract class RuleNodeVisitor {
 
     private static final Logger LOGGER = Logger.getLogger(RuleNodeVisitor.class);
 
     protected RuleNode currentRuleNode;
 
     public RuleNodeVisitor(RuleNode ruleNode) throws Exception {
         currentRuleNode = ruleNode;
         currentRuleNode.getNextGoals().addAll(currentRuleNode.getAbductiveFramework().getIC()); // TODO: This should be somewhere else?
         currentRuleNode.acceptVisitor(this);
     }
 
     private RuleNode constructPositiveChildNode(IInferableInstance newGoal, List<IInferableInstance> newRestOfGoals,
                                                 RuleNode previousNode) {
         RuleNode newRuleNode;
         if (!(newGoal==null)) {
             newRuleNode = newGoal.getPositiveRootRuleNode(previousNode.getAbductiveFramework(), newRestOfGoals);
             newRuleNode.setStore(previousNode.getStore().shallowClone());
             newRuleNode.setAssignments(new HashMap<VariableInstance, IUnifiableAtomInstance>(previousNode.getAssignments()));
             newRuleNode.setConstraintSolver(previousNode.getConstraintSolver().shallowClone());
         }
         else {
             Map<VariableInstance, IUnifiableAtomInstance> assignments = new HashMap<VariableInstance, IUnifiableAtomInstance>(previousNode.getAssignments());
             newRuleNode = new LeafRuleNode(previousNode.getAbductiveFramework(),previousNode.getStore().shallowClone(),assignments,previousNode);
             newRuleNode.setConstraintSolver(previousNode.getConstraintSolver().shallowClone());
         }
 
         return newRuleNode;
     }
 
 
     private RuleNode constructNegativeChildNode(IInferableInstance newGoal, List<DenialInstance> nestedDenialList,
                                                 List<IInferableInstance> newRestOfGoals, RuleNode previousNode) {
         newRestOfGoals = new LinkedList<IInferableInstance>(newRestOfGoals);
         RuleNode newRuleNode = newGoal.getNegativeRootRuleNode(previousNode.getAbductiveFramework(), nestedDenialList, newRestOfGoals);
         Map<VariableInstance, IUnifiableAtomInstance> assignments = new HashMap<VariableInstance, IUnifiableAtomInstance>(previousNode.getAssignments());
         newRuleNode.setAssignments(assignments);
         newRuleNode.setStore(previousNode.getStore().shallowClone());
         newRuleNode.setConstraintSolver(previousNode.getConstraintSolver().shallowClone());
         return newRuleNode;
     }
 
     public void visit(A1RuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying A1 to node.");
         // 1st Branch: Unify with an already collected abducible.
         LinkedList<RuleNode> childNodes = new LinkedList<RuleNode>();
         Store store = ruleNode.getStore();
         PredicateInstance goalAbducible = (PredicateInstance) ruleNode.getCurrentGoal();
         LinkedList<RuleNode> firstBranchChildNodes = getA1FirstBranch(ruleNode, store, goalAbducible);
         childNodes.addAll(0,firstBranchChildNodes);
 
         // Second branch: Add a new abducible. Check satisfies collected nestedDenialsList. Check not possible to unifyLeftRight with any existing.
         RuleNode secondBranchChildNode = getA1SecondBranch(ruleNode, store, goalAbducible);
         childNodes.add(secondBranchChildNode);
         if (LOGGER.isInfoEnabled()) LOGGER.info("A1 generated "+childNodes.size()+" new states.");
         expandNode(ruleNode, childNodes);
     }
 
     private LinkedList<RuleNode> getA1FirstBranch(A1RuleNode ruleNode, Store store, PredicateInstance goalAbducible) {
         LinkedList<RuleNode> childNodes = new LinkedList<RuleNode>();
         for (PredicateInstance storeAbducible : store.abducibles) {
             if (goalAbducible.isSameFunction(storeAbducible)) {
                 List<IInferableInstance> equalitySolved = new LinkedList<IInferableInstance>(storeAbducible.reduce(goalAbducible));
                 List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
                 IInferableInstance newGoal = equalitySolved.remove(0);
                 newRestOfGoals.addAll(0,equalitySolved);
                 RuleNode childNode = constructPositiveChildNode(newGoal, newRestOfGoals, ruleNode);
                 childNodes.add(childNode);
             }
         }
         return childNodes;
     }
 
     private RuleNode getA1SecondBranch(A1RuleNode ruleNode, Store store, PredicateInstance goalAbducible) {
         // Set up new child node and it's data structures.
         RuleNode childNode;
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
 
         // Check our new collected abducible doesn't violate any collected constraints.
         for (DenialInstance collectedDenial : store.denials) {
             PredicateInstance collectedDenialHead = (PredicateInstance) collectedDenial.getBody().get(0);
             if (collectedDenialHead.isSameFunction(goalAbducible)) {
                 DenialInstance newDenial = (DenialInstance) collectedDenial.deepClone(new HashMap<VariableInstance, IUnifiableAtomInstance>(ruleNode.getAssignments()));
                 collectedDenialHead = (PredicateInstance) newDenial.getBody().remove(0);
                 newDenial.getBody().addAll(0,goalAbducible.reduce(collectedDenialHead));
                 newRestOfGoals.add(0, newDenial);
             }
         }
 
         // Check our new collected abducible won't unifyLeftRight with any already collected abducibles.
         for (PredicateInstance storeAbducible : store.abducibles) {
             if (goalAbducible.isSameFunction(storeAbducible)) {
                 List<EqualityInstance> equalitySolved = goalAbducible.reduce(storeAbducible);
                 for (EqualityInstance result : equalitySolved) {
                     newRestOfGoals.add(0,new InEqualityInstance(result));
                 }
             }
         }
 
         IInferableInstance newGoal = null;
         if (!newRestOfGoals.isEmpty()) newGoal = newRestOfGoals.remove(0);
 
         childNode = constructPositiveChildNode(newGoal, newRestOfGoals, ruleNode);
         childNode.getStore().abducibles.add(goalAbducible);
 
         return childNode;
     }
 
     public void visit(A2RuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying A2 to node.");
 
         // Set up new child nodes data structures.
         RuleNode childNode;
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         List<DenialInstance> newNestedDenialList = new LinkedList<DenialInstance>(ruleNode.getNestedDenialsList());
         DenialInstance newCurrentDenial = newNestedDenialList.remove(0).shallowClone();
 
         PredicateInstance currentGoal = (PredicateInstance) ruleNode.getCurrentGoal();
 
         // Check that the new constraint we are creating isn't instantly violated by an already collected abducible.
         Store store = ruleNode.getStore();
         List<DenialInstance> newDenials = new LinkedList<DenialInstance>();
         for (PredicateInstance storeAbducible : store.abducibles) {
             if (storeAbducible.isSameFunction(currentGoal)) {
                 HashMap<VariableInstance, IUnifiableAtomInstance> subst = new HashMap<VariableInstance, IUnifiableAtomInstance>(ruleNode.getAssignments());
                 DenialInstance newDenial = (DenialInstance) newCurrentDenial.shallowClone();
                 newDenial = (DenialInstance) newDenial.performSubstitutions(subst);
                 List<EqualityInstance> equalitySolved = storeAbducible.reduce(currentGoal);
                 newDenial.getBody().addAll(0,equalitySolved);
                 newDenials.add(newDenial);
             }
         }
 
         if (newNestedDenialList.isEmpty()) {
             newRestOfGoals.addAll(0,newDenials);
             IInferableInstance newGoal = null;
             if (!newRestOfGoals.isEmpty()) newGoal = newRestOfGoals.remove(0);
             childNode = constructPositiveChildNode(newGoal,newRestOfGoals,ruleNode);
         }
         else {
             newNestedDenialList.get(0).getBody().addAll(0, newDenials);
             IInferableInstance newGoal = newNestedDenialList.remove(0);
             childNode = constructNegativeChildNode(newGoal,newNestedDenialList,newRestOfGoals,ruleNode);
         }
 
         newCurrentDenial.getBody().add(0,currentGoal);
         childNode.getStore().denials.add(newCurrentDenial);
 
         expandNode(ruleNode,childNode);
     }
 
     public void visit(D1RuleNode ruleNode) throws DefinitionException {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying D1 to node.");
         PredicateInstance definedPredicate = (PredicateInstance) ruleNode.getCurrentGoal();
         List<List<IInferableInstance>> possibleUnfolds = ruleNode.getAbductiveFramework().unfoldDefinitions(definedPredicate);
         LinkedList<RuleNode> childNodes = new LinkedList<RuleNode>();
         for (List<IInferableInstance> possibleUnfold : possibleUnfolds) {
             List<IInferableInstance> restOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
             restOfGoals.addAll(0,possibleUnfold);
             IInferableInstance newGoal = restOfGoals.remove(0);
             RuleNode childNode = constructPositiveChildNode(newGoal, restOfGoals, ruleNode);
             childNodes.add(childNode);
         }
         if (LOGGER.isInfoEnabled()) LOGGER.info("D1 generated "+childNodes.size()+" new states.");
         expandNode(ruleNode,childNodes);
     }
 
     public void visit(D2RuleNode ruleNode) throws DefinitionException {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying D2 to node.");
 
         PredicateInstance currentGoal = (PredicateInstance) ruleNode.getCurrentGoal();
 
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         List<DenialInstance> newNestedDenials = new LinkedList<DenialInstance>(ruleNode.getNestedDenialsList());
         DenialInstance newCurrentDenial = newNestedDenials.remove(0).shallowClone();
         IInferableInstance newGoal = null;
 
         RuleNode childNode;
 
         List<List<IInferableInstance>> possibleUnfolds = ruleNode.getAbductiveFramework().unfoldDefinitions(currentGoal);
 
         List<DenialInstance> newUnfoldedDenials = new LinkedList<DenialInstance>();
 
         for (List<IInferableInstance> possibleUnfold:possibleUnfolds) {
 
             HashMap<VariableInstance,IUnifiableAtomInstance> subst = new HashMap<VariableInstance, IUnifiableAtomInstance>(ruleNode.getAssignments());
             DenialInstance newUnfoldedDenial = (DenialInstance) newCurrentDenial.shallowClone();
             newUnfoldedDenial = (DenialInstance) newUnfoldedDenial.performSubstitutions(subst);
             Set<VariableInstance> newUniversalVariables = new HashSet<VariableInstance>();
             List<IInferableInstance> toAddToBody = new LinkedList<IInferableInstance>();
             for (IInferableInstance unfold:possibleUnfold) {
                 toAddToBody.add((IInferableInstance) unfold.performSubstitutions(subst));
                 if (!(unfold instanceof EqualityInstance)) {
                     newUniversalVariables.addAll(unfold.getVariables());
                 }
             }
             newUnfoldedDenial.getBody().addAll(0,toAddToBody);
             newUnfoldedDenial.getUniversalVariables().addAll(newUniversalVariables);
             newUnfoldedDenials.add(newUnfoldedDenial);
         }
 
         if (newNestedDenials.isEmpty()) {
             newRestOfGoals.addAll(0,newUnfoldedDenials);
             newGoal = null;
             if (!newRestOfGoals.isEmpty()) newGoal = newRestOfGoals.remove(0);
             childNode = constructPositiveChildNode(newGoal,newRestOfGoals,ruleNode);
         }
 
         else {
             newNestedDenials.get(0).getBody().addAll(0,newUnfoldedDenials);
             newGoal = null;
             if (!newNestedDenials.get(0).getBody().isEmpty()) newGoal = newNestedDenials.get(0).getBody().remove(0);
             childNode = constructNegativeChildNode(newGoal,newNestedDenials,newRestOfGoals,ruleNode);
         }
 
         expandNode(ruleNode,childNode);
 
     }
 
     public void visit(E1RuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying E1 to node.");
         RuleNode childNode;
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         EqualityInstance currentGoal = (EqualityInstance) ruleNode.getCurrentGoal();
         IInferableInstance newGoal = null;
         if (!newRestOfGoals.isEmpty()) newGoal = newRestOfGoals.remove(0);
         childNode = constructPositiveChildNode(newGoal,newRestOfGoals,ruleNode);
         //childNode.getStore().equalities.add(currentGoal);
 
         // Optimization.
         boolean equalitySolveSuccess = currentGoal.equalitySolve(childNode.getAssignments());
         if (equalitySolveSuccess)  {
             expandNode(ruleNode,childNode);
         }
         else {
             childNode.setNodeMark(RuleNode.NodeMark.FAILED);
         }
     }
 
     public void visit(InE1RuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying E1 Inequality to node.");
         RuleNode childNode;
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         InEqualityInstance currentGoal = (InEqualityInstance) ruleNode.getCurrentGoal();
         IInferableInstance newGoal = null;
 
         if (!newRestOfGoals.isEmpty()) newGoal = newRestOfGoals.remove(0);
         childNode = constructPositiveChildNode(newGoal,newRestOfGoals,ruleNode);
         childNode.getStore().equalities.add(currentGoal);
         expandNode(ruleNode,childNode);
     }
 
     public void visit(E2RuleNode ruleNode) throws Exception {
         EqualityInstance currentGoal = (EqualityInstance) ruleNode.getCurrentGoal();
 
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         List<DenialInstance> newNestedDenials = new LinkedList<DenialInstance>(ruleNode.getNestedDenialsList());
         DenialInstance newCurrentDenial = newNestedDenials.remove(0).shallowClone();
         IInferableInstance newGoal = null;
 
         RuleNode childNode;
         List<RuleNode> newChildNodes = new LinkedList<RuleNode>();
 
         List<EqualityInstance> reductionResult = reductionResult = currentGoal.reduceLeftRight();
 
 
         while (!reductionResult.isEmpty()) {
             newCurrentDenial.getBody().addAll(0,reductionResult);
             currentGoal = (EqualityInstance) newCurrentDenial.getBody().remove(0);
         }
 
         if (currentGoal.getLeft() instanceof ConstantInstance) {  // c=c or for all X c=X
             HashMap<VariableInstance,IUnifiableAtomInstance> newAssignments = new HashMap<VariableInstance, IUnifiableAtomInstance>(ruleNode.getAssignments());
             boolean unificationSuccess = currentGoal.unifyLeftRight(newAssignments); // Blank assignments as should be just constants.
             newNestedDenials.add(newCurrentDenial);
             if (unificationSuccess) {
                 childNode = constructNegativeChildNode(new TrueInstance(),newNestedDenials,newRestOfGoals,ruleNode);
                 childNode.setAssignments(newAssignments);
             }
             else {
                 childNode = constructNegativeChildNode(new FalseInstance(),newNestedDenials,newRestOfGoals,ruleNode);
             }
             newChildNodes.add(childNode);
 
         }
 
         else if (currentGoal.getLeft() instanceof VariableInstance) {
             VariableInstance left = (VariableInstance) currentGoal.getLeft();
             if (newCurrentDenial.getUniversalVariables().contains(left)) {
                 HashMap<VariableInstance,IUnifiableAtomInstance> newAssignments = new HashMap<VariableInstance,IUnifiableAtomInstance>(ruleNode.getAssignments());
                 boolean unificationSuccess = currentGoal.unifyLeftRight(newAssignments);
 
                 newCurrentDenial = newCurrentDenial.shallowClone();
                 newCurrentDenial = (DenialInstance)newCurrentDenial.performSubstitutions(newAssignments);
 
                 newNestedDenials.add(newCurrentDenial);
 
                 if (unificationSuccess) {
                     newGoal = new TrueInstance();
                 }
                 else {
                     newGoal = new FalseInstance();
                 }
                 childNode = constructNegativeChildNode(newGoal, newNestedDenials,newRestOfGoals,ruleNode);
 
                 // TODO: No assignment needed for universally quantified? childNode.setAssignments(newAssignments);
                 newChildNodes.add(childNode);
             }
             else { // Now in equational solved form.
                 if (currentGoal.getRight() instanceof VariableInstance) {
                     if (LOGGER.isInfoEnabled()) LOGGER.info("Applying E2c to node.");
                     HashMap<VariableInstance,IUnifiableAtomInstance> newAssignments = new HashMap<VariableInstance,IUnifiableAtomInstance>(ruleNode.getAssignments());
                     boolean unificationSuccess = currentGoal.unifyRightLeft(newAssignments);
                     if (!unificationSuccess) {
                         //throw new JALPException("Error in JALP. E2c should never fail unification");
                     }
                     if (unificationSuccess) {
                        newCurrentDenial = newCurrentDenial.shallowClone();
                         newCurrentDenial = (DenialInstance)newCurrentDenial.performSubstitutions(newAssignments);
                         if (newCurrentDenial.getBody().isEmpty()) {
                             newGoal = new FalseInstance();
                             if (newNestedDenials.isEmpty()) {
                                 childNode = constructPositiveChildNode(newGoal,newRestOfGoals,ruleNode);
                             }
                             else {
                                 childNode = constructNegativeChildNode(newGoal,newNestedDenials,newRestOfGoals,ruleNode);
                             }
                         }
                         else {
                             newGoal = newCurrentDenial.getBody().remove(0);
                             newNestedDenials.add(newCurrentDenial);
                             childNode = constructNegativeChildNode(newGoal,newNestedDenials,newRestOfGoals,ruleNode);
                         }
                     }
                     else {
                         if (newNestedDenials.isEmpty()) {
                             childNode = constructPositiveChildNode(new TrueInstance(), newRestOfGoals,ruleNode);
                         }
                         else {
                             childNode = constructNegativeChildNode(new TrueInstance(), newNestedDenials,newRestOfGoals,ruleNode);
                         }
                     }
 
 
                     // TODO: No assignment needed for universally quantified? childNode.setAssignments(newAssignments);
                     newChildNodes.add(childNode);
 
                 }
                 else {
                     if (LOGGER.isInfoEnabled()) LOGGER.info("Applying E2b to node.");
                     // Branch 1
                     InEqualityInstance inEqualityInstance = new InEqualityInstance(currentGoal);
                     newGoal = new TrueInstance(); // TODO: Is this correct?
                     if (newNestedDenials.isEmpty()) {
                         childNode = constructPositiveChildNode(newGoal,newRestOfGoals,ruleNode);
                     }
                     else {
                         childNode = constructNegativeChildNode(newGoal,newNestedDenials,newRestOfGoals,ruleNode);
                     }
                     childNode.getStore().equalities.add(inEqualityInstance);
                     newChildNodes.add(childNode);
                     // Branch 2
                     newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
                     newNestedDenials = new LinkedList<DenialInstance>(ruleNode.getNestedDenialsList());
                     newCurrentDenial = newNestedDenials.remove(0).shallowClone();
                     HashMap<VariableInstance,IUnifiableAtomInstance> newAssignments = new HashMap<VariableInstance,IUnifiableAtomInstance>(ruleNode.getAssignments());
                     boolean unificationSuccess = currentGoal.unifyLeftRight(newAssignments);
                     if (unificationSuccess) {
                        newCurrentDenial = newCurrentDenial.shallowClone();
                         newCurrentDenial = (DenialInstance)newCurrentDenial.performSubstitutions(newAssignments);
                         if (newCurrentDenial.getBody().isEmpty()) {
                             newGoal = new FalseInstance();
                             if (newNestedDenials.isEmpty()) {
                                 childNode = constructPositiveChildNode(newGoal,newRestOfGoals,ruleNode);
                             }
                             else {
                                 childNode = constructNegativeChildNode(newGoal,newNestedDenials,newRestOfGoals,ruleNode);
                             }
                         }
                         else {
                             newGoal = newCurrentDenial.getBody().remove(0);
                             newNestedDenials.add(newCurrentDenial);
                             childNode = constructNegativeChildNode(newGoal, newNestedDenials,newRestOfGoals,ruleNode);
                         }
                         childNode.setAssignments(newAssignments);
                         newChildNodes.add(childNode);
                     }
 
 
                 }
             }
         }
 
         expandNode(ruleNode,newChildNodes);
     }
 
     public void visit(InE2RuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying E2 inequality to node.");
         InEqualityInstance currentGoal = (InEqualityInstance) ruleNode.getCurrentGoal();
 
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         List<DenialInstance> newNestedDenials = new LinkedList<DenialInstance>(ruleNode.getNestedDenialsList());
         DenialInstance newCurrentDenial = newNestedDenials.remove(0).shallowClone();
         IInferableInstance newGoal = null;
 
         RuleNode childNode;
         List<RuleNode> newChildNodes = new LinkedList<RuleNode>();
 
         // Branch 1
         newGoal = currentGoal.getEqualityInstance();
         if (newNestedDenials.isEmpty()) {
             childNode = constructPositiveChildNode(newGoal,newRestOfGoals,ruleNode);
         }
         else {
             childNode = constructNegativeChildNode(newGoal,newNestedDenials,newRestOfGoals,ruleNode);
         }
         newChildNodes.add(childNode);
         // Branch 2
         newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         newNestedDenials = new LinkedList<DenialInstance>(ruleNode.getNestedDenialsList());
         newCurrentDenial = newNestedDenials.remove(0).shallowClone();
 
         newGoal = newCurrentDenial;
         if (newNestedDenials.isEmpty()) {
             newRestOfGoals.add(0,currentGoal);
             childNode = constructPositiveChildNode(newGoal,newRestOfGoals,ruleNode);
         }
         else {
             newNestedDenials.get(0).getBody().add(0,currentGoal);
             childNode = constructNegativeChildNode(newGoal,newNestedDenials,newRestOfGoals,ruleNode);
         }
 
         newChildNodes.add(childNode);
         expandNode(ruleNode,newChildNodes);
 
     }
 
     public void visit(N1RuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying N1 to node.");
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         NegationInstance goal = (NegationInstance) ruleNode.getCurrentGoal();
         DenialInstance denial = new DenialInstance(goal.getSubFormula());
         RuleNode childNode = constructPositiveChildNode(denial, newRestOfGoals, ruleNode);   // TODO The denial should be at the end of the list, not the front, according to nuffelen?
         expandNode(ruleNode,childNode);
     }
 
     public void visit(N2RuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying N2 to node.");
         RuleNode childNode;
         LinkedList<RuleNode> childNodes = new LinkedList<RuleNode>();
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         NegationInstance goal = (NegationInstance) ruleNode.getCurrentGoal();
         List<DenialInstance> newNestedDenialList = new LinkedList<DenialInstance>(ruleNode.getNestedDenialsList());
         DenialInstance currentDenial = newNestedDenialList.remove(0).shallowClone();
         if (newNestedDenialList.isEmpty()) {
             childNode = constructPositiveChildNode(goal.getSubFormula(), newRestOfGoals, ruleNode);
 
         } else {
             childNode = constructNegativeChildNode(goal.getSubFormula(), newNestedDenialList, newRestOfGoals, ruleNode);
         }
         childNodes.add(childNode);
         // OR
         newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
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
         expandNode(ruleNode,childNodes);
 
     }
 
     public void visit(F1RuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying F1 to node.");
         RuleNode childNode;
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         ConstraintInstance currentGoal = (ConstraintInstance) ruleNode.getCurrentGoal();
         IInferableInstance newGoal = null;
 
         if (!newRestOfGoals.isEmpty()) newGoal = newRestOfGoals.remove(0);
         childNode = constructPositiveChildNode(newGoal,newRestOfGoals,ruleNode);
         childNode.getStore().constraints.add(currentGoal);
         expandNode(ruleNode,childNode);
 
     }
 
     public void visit(F2RuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying F2 to node.");
         ConstraintInstance currentGoal = (ConstraintInstance) ruleNode.getCurrentGoal();
 
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         List<DenialInstance> newNestedDenials = new LinkedList<DenialInstance>(ruleNode.getNestedDenialsList());
         DenialInstance newCurrentDenial = newNestedDenials.remove(0).shallowClone();
         IInferableInstance newGoal = null;
 
         RuleNode childNode;
         List<RuleNode> newChildNodes = new LinkedList<RuleNode>();
         // Branch 1
         NegativeConstraintInstance negativeConstraintInstance = new NegativeConstraintInstance(currentGoal);
         newGoal = new FalseInstance(); // TODO: Is this correct?
         newNestedDenials.add(newCurrentDenial);
         childNode = constructNegativeChildNode(newGoal,newNestedDenials,newRestOfGoals,ruleNode);
         childNode.getStore().constraints.add(negativeConstraintInstance);
         newChildNodes.add(childNode);
         // Branch 2
         newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         newNestedDenials = new LinkedList<DenialInstance>(ruleNode.getNestedDenialsList());
         newCurrentDenial = newNestedDenials.remove(0).shallowClone();
         newNestedDenials.add(newCurrentDenial);
         newGoal = new TrueInstance();
         childNode = constructNegativeChildNode(newGoal,newNestedDenials,newRestOfGoals,ruleNode);
         childNode.getStore().constraints.add(currentGoal);
         newChildNodes.add(childNode);
         expandNode(ruleNode,newChildNodes);
 
     }
 
     public void visit(F2bRuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying F2b to node.");
         // Very much like D2.
         // Make copies of the denial and perform an assignment to the variable for each element in the domain
         // of X. For now this just supports a list.
 
         InListConstraintInstance constraintInstance = (InListConstraintInstance) ruleNode.getCurrentGoal();
 
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         List<DenialInstance> newNestedDenials = new LinkedList<DenialInstance>(ruleNode.getNestedDenialsList());
         DenialInstance newCurrentDenial = newNestedDenials.remove(0).shallowClone();
 
         IInferableInstance newGoal;
 
         RuleNode childNode;
 
         VariableInstance variable = (VariableInstance) constraintInstance.getLeft();
         LinkedList<ConstantInstance> constantList = ((ListInstance)constraintInstance.getRight()).getList();
 
         LinkedList<DenialInstance> newDenials = new LinkedList<DenialInstance>();
 
         for (ConstantInstance constant:constantList) {
             Map<VariableInstance,IUnifiableAtomInstance> newAssignments = new HashMap<VariableInstance, IUnifiableAtomInstance>(ruleNode.getAssignments());
             boolean unificationSuccess = variable.unify(constant,newAssignments);
             if (unificationSuccess) {
                 DenialInstance newDenialInstance = (DenialInstance) newCurrentDenial.shallowClone();
                 newDenialInstance = (DenialInstance) newDenialInstance.performSubstitutions(newAssignments);
                 newDenials.add(newDenialInstance);
             }
         }
 
         if (newNestedDenials.isEmpty()) {
             newRestOfGoals.addAll(0,newDenials);
             newGoal = null;
             if (!newRestOfGoals.isEmpty()) newGoal = newRestOfGoals.remove(0);
             childNode = constructPositiveChildNode(newGoal,newRestOfGoals,ruleNode);
         }
 
         else {
             newNestedDenials.get(0).getBody().addAll(0, newDenials);
             newGoal = null;
             if (!newNestedDenials.get(0).getBody().isEmpty()) newGoal = newNestedDenials.get(0).getBody().remove(0);
             childNode = constructNegativeChildNode(newGoal,newNestedDenials,newRestOfGoals,ruleNode);
         }
 
         expandNode(ruleNode,childNode);
 
     }
 
 
         /**
         * Produces one child node where the true instance is removed from the goal stack.
         *
         * @param ruleNode
         */
     public void visit(PositiveTrueRuleNode ruleNode)
     {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying truth conjunction rule to node.");
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         IInferableInstance newGoal = null;
         if (!newRestOfGoals.isEmpty()) newGoal = newRestOfGoals.remove(0);
         RuleNode newRuleNode = constructPositiveChildNode(newGoal, newRestOfGoals, ruleNode);
         expandNode(ruleNode,newRuleNode);
 
     }
 
     /**
      * Produces one child node where the true instance is removed from the denial.
      *
      * @param ruleNode
      */
     public void visit(NegativeTrueRuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying truth denial conjunction rule to node.");
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         List<DenialInstance> newNestedDenialList = new LinkedList<DenialInstance>(ruleNode.getNestedDenialsList());
         DenialInstance newCurrentDenial = newNestedDenialList.remove(0).shallowClone();
         IInferableInstance newGoal;
 
         RuleNode childNode;
 
         if (newCurrentDenial.getBody().isEmpty()) {
             if (newNestedDenialList.isEmpty()) {
                 childNode = constructPositiveChildNode(new FalseInstance(),newRestOfGoals,ruleNode);
             }
             else {
                 childNode = constructNegativeChildNode(new FalseInstance(),newNestedDenialList,newRestOfGoals,ruleNode);
             }
         }
         else {
             newNestedDenialList.add(newCurrentDenial);
             newGoal = newCurrentDenial.getBody().remove(0);
             childNode = constructNegativeChildNode(newGoal,newNestedDenialList,newRestOfGoals,ruleNode);
         }
 
         expandNode(ruleNode,childNode);
     }
 
     /**
      * Fails the node.
      *
      * @param ruleNode
      */
     public void visit(PositiveFalseRuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying false conjunction rule to node.");
         ruleNode.setNodeMark(RuleNode.NodeMark.FAILED);
     }
 
     /**
      * Produces one child node whereby the nestedDenialsList succeeds.
      *
      * @param ruleNode
      */
     public void visit(NegativeFalseRuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying false denial conjunction rule to node.");
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         List<DenialInstance> newNestedDenialList = new LinkedList<DenialInstance>(ruleNode.getNestedDenialsList());
         RuleNode newChildNode;
         newNestedDenialList.remove(0);
         if (newNestedDenialList.isEmpty()) {
             IInferableInstance newGoal = new TrueInstance();
             newChildNode = constructPositiveChildNode(newGoal, newRestOfGoals, ruleNode);
         } else {
             DenialInstance newDenial = newNestedDenialList.remove(0).shallowClone();
             IInferableInstance newGoal = newDenial.getBody().remove(0);
             newChildNode = constructNegativeChildNode(newGoal, newNestedDenialList, newRestOfGoals, ruleNode);
         }
         expandNode(ruleNode,newChildNode);
 
     }
 
     // Expands a leaf node using the constraint solver.
     public void visit(LeafRuleNode ruleNode) {
         //ruleNode.setNodeMark(RuleNode.NodeMark.SUCCEEDED);
         if (ruleNode.getNodeMark()==RuleNode.NodeMark.UNEXPANDED) {
             if (LOGGER.isDebugEnabled()) LOGGER.debug("Found a leaf node to expand:\n"+ruleNode);
             ruleNode.getParentNode().getChildren().remove(ruleNode);
             executeConstraintSolver(ruleNode.getParentNode(),ruleNode);
             currentRuleNode = ruleNode.getParentNode();
         }
     }
 
 
     private void executeConstraintSolver(RuleNode parentNode,RuleNode childNode) {
         List<Map<VariableInstance,IUnifiableAtomInstance>> possibleAssignments = childNode.constraintSolve();
         if (possibleAssignments.isEmpty()) {
             parentNode.setNodeMark(RuleNode.NodeMark.FAILED);
             if (LOGGER.isDebugEnabled()) LOGGER.debug("Constraint solver failed on\n"+childNode);
         }
         else { // Constraint solver succeeded. Generate possible children.
             parentNode.setNodeMark(RuleNode.NodeMark.EXPANDED);
             for (Map<VariableInstance,IUnifiableAtomInstance> assignment:possibleAssignments) {
                 RuleNode newLeafNode = childNode.shallowClone();
                 newLeafNode.setAssignments(assignment);
                 Map<VariableInstance, IUnifiableAtomInstance> n = newLeafNode.equalitySolve();
                 if (n==null) {
                     newLeafNode.setNodeMark(RuleNode.NodeMark.FAILED);
                     if (LOGGER.isDebugEnabled()) LOGGER.debug("Equality solver failed on\n"+newLeafNode);
                 }
                 else {
                     newLeafNode.setNodeMark(RuleNode.NodeMark.SUCCEEDED);
                 }
                 childNode.setAssignments(n);
                 parentNode.getChildren().add(newLeafNode);
             }
         }
     }
 
     private void expandNode(RuleNode parent, RuleNode child) {
         Map<VariableInstance,IUnifiableAtomInstance> assignments = child.equalitySolve();
         if (assignments == null) {
             child.setNodeMark(RuleNode.NodeMark.FAILED);
             if (LOGGER.isDebugEnabled()) LOGGER.debug("Equality solver failed on\n"+child);
             parent.getChildren().add(child);
         }
         else { // Equality solver succeeded.
             child.setAssignments(assignments);
             parent.getChildren().add(child);
         }
         parent.setNodeMark(RuleNode.NodeMark.EXPANDED);
     }
 
     private void expandNode(RuleNode parent, List<RuleNode> children) {
         for (RuleNode child:children) {
             expandNode(parent, child);
         }
     }
 
     public RuleNode stateRewrite() throws Exception {
         currentRuleNode = chooseNextNode();
         if (currentRuleNode==null) return null;
         currentRuleNode.acceptVisitor(this);
         return currentRuleNode;
     }
 
     public RuleNode getCurrentRuleNode() {
         return currentRuleNode;
     }
 
 
     protected abstract RuleNode chooseNextNode();
     public abstract boolean hasNextNode();
 
 }
