 package uk.co.mtford.jalp.abduction.rules;
 
 import uk.co.mtford.jalp.abduction.AbductiveFramework;
 import uk.co.mtford.jalp.abduction.Store;
 import uk.co.mtford.jalp.abduction.logic.instance.IInferableInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.IUnifiableAtomInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.term.VariableInstance;
 import uk.co.mtford.jalp.abduction.rules.visitor.RuleNodeVisitor;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mtford
  * Date: 18/05/2012
  * Time: 06:47
  * To change this template use File | Settings | File Templates.
  */
 public class A1RuleNode extends PositiveRuleNode {
 
     public A1RuleNode(AbductiveFramework abductiveFramework, IInferableInstance goal, List<IInferableInstance> restOfGoals) {
         super(abductiveFramework, goal, restOfGoals);
     }
 
     public A1RuleNode(AbductiveFramework abductiveFramework, IInferableInstance goal, List<IInferableInstance> restOfGoals, Store store, Map<VariableInstance, IUnifiableAtomInstance> assignments) {
         super(abductiveFramework, goal, restOfGoals, store, assignments);
     }
 
     protected A1RuleNode() {
         super();
     }
 
     @Override
     public RuleNode shallowClone() {
         A1RuleNode newRuleNode = new A1RuleNode();
         newRuleNode.children = new LinkedList<RuleNode>(children);
         newRuleNode.assignments = new HashMap<VariableInstance, IUnifiableAtomInstance>(assignments);
         newRuleNode.currentGoal = currentGoal;
         newRuleNode.abductiveFramework = abductiveFramework;
         newRuleNode.constraintSolver = constraintSolver.shallowClone();

         newRuleNode.store = store.shallowClone();
         newRuleNode.nextGoals = new LinkedList<IInferableInstance>(nextGoals);
         return newRuleNode;
     }
 
     @Override
     public void acceptVisitor(RuleNodeVisitor v) {
         v.visit(this);
     }
 }
