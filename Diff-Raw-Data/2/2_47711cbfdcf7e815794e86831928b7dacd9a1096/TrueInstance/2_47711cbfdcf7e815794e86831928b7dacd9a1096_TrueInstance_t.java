 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.co.mtford.jalp.abduction.logic.instance;
 
 import uk.co.mtford.jalp.abduction.AbductiveFramework;
 import uk.co.mtford.jalp.abduction.logic.instance.term.VariableInstance;
 import uk.co.mtford.jalp.abduction.rules.NegativeTrueRuleNode;
 import uk.co.mtford.jalp.abduction.rules.PositiveTrueRuleNode;
 import uk.co.mtford.jalp.abduction.rules.RuleNode;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author mtford
  */
 public class TrueInstance implements IAtomInstance, IInferableInstance {
 
     @Override
     public String toString() {
         return "TRUE";
     }
 
     @Override
     public RuleNode getPositiveRootRuleNode(AbductiveFramework abductiveFramework, List<IInferableInstance> goals) {
         return new PositiveTrueRuleNode(abductiveFramework, this, goals);
     }
 
     @Override
     public RuleNode getNegativeRootRuleNode(AbductiveFramework abductiveFramework, List<DenialInstance> nestedDenialList, List<IInferableInstance> goals) {
         return new NegativeTrueRuleNode(abductiveFramework, this, goals, nestedDenialList);
     }
 
     @Override
     public IFirstOrderLogicInstance performSubstitutions(Map<VariableInstance, IUnifiableAtomInstance> substitutions) {
         return this;
     }
 
     @Override
     public IFirstOrderLogicInstance deepClone(Map<VariableInstance, IUnifiableAtomInstance> substitutions) {
         return new TrueInstance();
     }
 
     @Override
     public IFirstOrderLogicInstance shallowClone() {
        return this;
     }
 
     @Override
     public Set<VariableInstance> getVariables() {
         return new HashSet<VariableInstance>();
     }
 
     @Override
     public boolean equals(Object other) {
         if (other instanceof TrueInstance) return true;
         return false;
     }
 }
