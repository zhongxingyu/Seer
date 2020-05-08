 package uk.co.mtford.jalp.abduction.logic.instance.term;
 
 import choco.kernel.model.variables.Variable;
 import uk.co.mtford.jalp.abduction.logic.instance.IFirstOrderLogicInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.IUnifiableAtomInstance;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mtford
  * Date: 30/05/2012
  * Time: 10:24
  * To change this template use File | Settings | File Templates.
  */
 public class CharConstantListInstance extends ListInstance<CharConstantInstance> {
 
     public CharConstantListInstance() {
         super();
     }
 
     public CharConstantListInstance(Collection<CharConstantInstance> constants) {
         super();
         this.getList().addAll(constants);
     }
 
     public CharConstantListInstance(CharConstantInstance[] constants) {
         super();
         for (CharConstantInstance instance:constants) {
             this.getList().add(instance);
         }
     }
 
     public CharConstantListInstance(String... strings) {
         super();
         for (String s:strings) {
             this.getList().add(new CharConstantInstance(s));
         }
     }
 
     @Override
     public IFirstOrderLogicInstance performSubstitutions(Map<VariableInstance, IUnifiableAtomInstance> substitutions) {
         CharConstantListInstance newListInstance = new CharConstantListInstance();
         for (CharConstantInstance term:list) {
            CharConstantInstance newTerm = (CharConstantInstance) term.shallowClone();
            newTerm = (CharConstantInstance) newTerm.performSubstitutions(substitutions);
            newListInstance.getList().add(newTerm);
         }
         return newListInstance;
     }
 
     @Override
     public IFirstOrderLogicInstance deepClone(Map<VariableInstance, IUnifiableAtomInstance> substitutions) {
         CharConstantListInstance newListInstance = new CharConstantListInstance();
         for (CharConstantInstance term:list) {
             newListInstance.getList().add((CharConstantInstance) term.deepClone(substitutions));
         }
         return newListInstance;
     }
 
     @Override
     public IFirstOrderLogicInstance shallowClone() {
         CharConstantListInstance newListInstance = new CharConstantListInstance();
         for (CharConstantInstance term:list) {
             newListInstance.getList().add(term);
         }
         return newListInstance;
     }
 
     @Override
     public boolean reduceToChoco(List<Map<VariableInstance, IUnifiableAtomInstance>> possSubst, HashMap<ITermInstance, Variable> termToVarMap) {
         throw new UnsupportedOperationException(); // Dealt with natively.
     }
 
     @Override
     public boolean inList(CharConstantListInstance constantList, List<Map<VariableInstance, IUnifiableAtomInstance>> possSubst) {
         throw new UnsupportedOperationException();
     }
 }
