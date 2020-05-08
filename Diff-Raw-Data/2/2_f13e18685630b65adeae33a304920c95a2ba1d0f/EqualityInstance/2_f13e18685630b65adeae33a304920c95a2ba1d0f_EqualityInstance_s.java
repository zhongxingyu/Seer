 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.co.mtford.jalp.abduction.logic.instance.equalities;
 
 import org.apache.log4j.Logger;
 import uk.co.mtford.jalp.abduction.AbductiveFramework;
 import uk.co.mtford.jalp.abduction.logic.instance.*;
 import uk.co.mtford.jalp.abduction.logic.instance.term.VariableInstance;
 import uk.co.mtford.jalp.abduction.rules.E1RuleNode;
 import uk.co.mtford.jalp.abduction.rules.E2RuleNode;
 import uk.co.mtford.jalp.abduction.rules.RuleNode;
 
 import java.util.*;
 
 /**
  * @author mtford
  */
 public class EqualityInstance implements IEqualityInstance {
 
     private static Logger LOGGER = Logger.getLogger(EqualityInstance.class);
 
     protected IUnifiableAtomInstance left;
     protected IUnifiableAtomInstance right;
 
     public EqualityInstance(IUnifiableAtomInstance left, IUnifiableAtomInstance right) {
         this.left = left;
         this.right = right;
     }
 
     public IUnifiableAtomInstance getLeft() {
         return left;
     }
 
     public void setLeft(IUnifiableAtomInstance left) {
         this.left = left;
     }
 
     public IUnifiableAtomInstance getRight() {
         return right;
     }
 
     public void setRight(IUnifiableAtomInstance right) {
         this.right = right;
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 89 * hash + (this.left != null ? this.left.hashCode() : 0);
         hash = 89 * hash + (this.right != null ? this.right.hashCode() : 0);
         return hash;
     }
 
     @Override
     public String toString() {
         String leftString = left.toString();
         String rightString = right.toString();
 
         return leftString + "==" + rightString;
     }
 
 
     @Override
     public RuleNode getPositiveRootRuleNode(AbductiveFramework abductiveFramework, List<IInferableInstance> goals) {
         return new E1RuleNode(abductiveFramework, this, goals);
     }
 
     @Override
     public RuleNode getNegativeRootRuleNode(AbductiveFramework abductiveFramework, List<DenialInstance> nestedDenialList, List<IInferableInstance> goals) {
         return new E2RuleNode(abductiveFramework, this, goals, nestedDenialList);
     }
 
     @Override
     public IFirstOrderLogicInstance performSubstitutions(Map<VariableInstance, IUnifiableAtomInstance> substitutions) {
         IUnifiableAtomInstance newLeft = (IUnifiableAtomInstance) left.performSubstitutions(substitutions);
         IUnifiableAtomInstance newRight = (IUnifiableAtomInstance) right.performSubstitutions(substitutions);
         left = newLeft;
         right = newRight;
         return this;
     }
 
     @Override
     public IFirstOrderLogicInstance deepClone(Map<VariableInstance, IUnifiableAtomInstance> substitutions) {
         IUnifiableAtomInstance newLeft = (IUnifiableAtomInstance) left.deepClone(substitutions);
         IUnifiableAtomInstance newRight = (IUnifiableAtomInstance) right.deepClone(substitutions);
         return new EqualityInstance(newLeft, newRight);
     }
 
     @Override
     public IFirstOrderLogicInstance shallowClone() {
        return new EqualityInstance(left, right);
     }
 
     @Override
     public Set<VariableInstance> getVariables() {
         HashSet<VariableInstance> variables = new HashSet<VariableInstance>();
         variables.addAll(left.getVariables());
         variables.addAll(right.getVariables());
         return variables;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof EqualityInstance)) return false;
 
         EqualityInstance that = (EqualityInstance) o;
 
         if (!left.equals(that.left)) return false;
         if (!right.equals(that.right)) return false;
 
         return true;
     }
 
     public List<EqualityInstance> reduceLeftRight() {
         return left.reduce(right);
     }
 
     public boolean unifyLeftRight(Map<VariableInstance, IUnifiableAtomInstance> assignments)  {
         return left.unify(right,assignments);
     }
 
     public List<EqualityInstance> reduceRightLeft() {
         return right.reduce(left);
     }
 
     public boolean unifyRightLeft(Map<VariableInstance, IUnifiableAtomInstance> assignments)  {
         return right.unify(left,assignments);
     }
 
     @Override
     public boolean equalitySolve(Map<VariableInstance, IUnifiableAtomInstance> equalitySolverAssignments) {
         return left.unify(right,equalitySolverAssignments);
     }
 }
