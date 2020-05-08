 package uk.co.mtford.jalp.abduction.logic.instance.equalities;
 
 import uk.co.mtford.jalp.abduction.AbductiveFramework;
 import uk.co.mtford.jalp.abduction.logic.instance.*;
 import uk.co.mtford.jalp.abduction.logic.instance.term.VariableInstance;
 import uk.co.mtford.jalp.abduction.rules.*;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mtford
  * Date: 21/05/2012
  * Time: 13:00
  * To change this template use File | Settings | File Templates.
  */
 public class InEqualityInstance implements IEqualityInstance {
 
     private EqualityInstance equalityInstance;
 
     public InEqualityInstance(EqualityInstance equalityInstance) {
         this.equalityInstance = equalityInstance;
     }
 
     public InEqualityInstance(IUnifiableAtomInstance left, IUnifiableAtomInstance right) {
         this.equalityInstance = new EqualityInstance(left,right);
     }
 
     public EqualityInstance getEqualityInstance() {
         return equalityInstance;
     }
 
     public void setEqualityInstance(EqualityInstance equalityInstance) {
         this.equalityInstance = equalityInstance;
     }
 
     @Override
     public String toString() {
         return equalityInstance.getLeft()+"!="+equalityInstance.getRight();
     }
 
     @Override
     public RuleNode getPositiveRootRuleNode(AbductiveFramework abductiveFramework, List<IInferableInstance> goals) {
         return new InE1RuleNode(abductiveFramework, this, goals);
 
     }
 
     @Override
     public RuleNode getNegativeRootRuleNode(AbductiveFramework abductiveFramework, List<DenialInstance> nestedDenials, List<IInferableInstance> goals) {
         return new InE2RuleNode(abductiveFramework, this, goals, nestedDenials);
 
     }
 
     @Override
     public IFirstOrderLogicInstance performSubstitutions(Map<VariableInstance, IUnifiableAtomInstance> substitutions) {
        return new InEqualityInstance((EqualityInstance) equalityInstance.performSubstitutions(substitutions));
     }
 
     @Override
     public IFirstOrderLogicInstance deepClone(Map<VariableInstance, IUnifiableAtomInstance> substitutions) {
         return new InEqualityInstance((EqualityInstance) equalityInstance.deepClone(substitutions));
     }
 
     @Override
     public IFirstOrderLogicInstance shallowClone() {
         return new InEqualityInstance((EqualityInstance) equalityInstance.shallowClone());
     }
 
     @Override
     public Set<VariableInstance> getVariables() {
         return equalityInstance.getVariables();
     }
 
     @Override
     public boolean equalitySolve(Map<VariableInstance, IUnifiableAtomInstance> equalitySolverAssignments) {
         HashMap<VariableInstance, IUnifiableAtomInstance> newAssignments = new HashMap<VariableInstance, IUnifiableAtomInstance>(equalitySolverAssignments);
         boolean success
                 = equalityInstance.equalitySolve(newAssignments);
 
         if (success) {
             if (newAssignments.size()>equalitySolverAssignments.size()) return true;
             else return false;
         }
         else {
             return true;
         }
 
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof InEqualityInstance)) return false;
 
         InEqualityInstance that = (InEqualityInstance) o;
 
         if (equalityInstance != null ? !equalityInstance.equals(that.equalityInstance) : that.equalityInstance != null)
             return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return equalityInstance != null ? equalityInstance.hashCode() : 0;
     }
 }
