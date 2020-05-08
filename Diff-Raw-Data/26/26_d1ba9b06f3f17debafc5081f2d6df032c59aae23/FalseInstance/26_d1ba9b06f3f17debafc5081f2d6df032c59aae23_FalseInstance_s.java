 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.co.mtford.jalp.abduction.logic.instance;
 
 import uk.co.mtford.jalp.abduction.AbductiveFramework;
 import uk.co.mtford.jalp.abduction.logic.instance.term.VariableInstance;
 import uk.co.mtford.jalp.abduction.rules.NegativeFalseRuleNode;
 import uk.co.mtford.jalp.abduction.rules.PositiveFalseRuleNode;
 import uk.co.mtford.jalp.abduction.rules.RuleNode;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author mtford
  */
 public class FalseInstance implements IAtomInstance, IInferableInstance {
 
     @Override
     public String toString() {
         return "FALSE";
     }
 
     @Override
     public RuleNode getPositiveRootRuleNode(AbductiveFramework abductiveFramework, List<IInferableInstance> goals) {
         return new PositiveFalseRuleNode(abductiveFramework, this, goals);
     }
 
     @Override
     public RuleNode getNegativeRootRuleNode(AbductiveFramework abductiveFramework, List<DenialInstance> nestedDenialList, List<IInferableInstance> goals) {
         return new NegativeFalseRuleNode(abductiveFramework, this, goals, nestedDenialList);
     }
 
     @Override
     public IFirstOrderLogicInstance performSubstitutions(Map<VariableInstance, IUnifiableAtomInstance> substitutions) {
         return this;
     }
 
     @Override
     public IFirstOrderLogicInstance deepClone(Map<VariableInstance, IUnifiableAtomInstance> substitutions) {
         return new FalseInstance();
     }
 
     @Override
     public IFirstOrderLogicInstance shallowClone() {
        return new FalseInstance();
     }
 
     @Override
     public Set<VariableInstance> getVariables() {
         return new HashSet<VariableInstance>();
     }
 
     @Override
     public boolean equals(Object other) {
         if (other instanceof FalseInstance) return true;
         return false;
     }
 }
