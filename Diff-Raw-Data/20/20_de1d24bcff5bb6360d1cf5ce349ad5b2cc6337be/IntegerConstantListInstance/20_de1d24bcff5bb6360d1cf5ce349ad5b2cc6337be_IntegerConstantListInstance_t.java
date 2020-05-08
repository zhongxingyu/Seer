 package uk.co.mtford.jalp.abduction.logic.instance.term;
 
 import choco.kernel.model.variables.Variable;
 import choco.kernel.model.variables.set.SetVariable;
 import uk.co.mtford.jalp.abduction.logic.instance.*;
 import uk.co.mtford.jalp.abduction.tools.NameGenerator;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static choco.Choco.makeSetVar;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mtford
  * Date: 30/05/2012
  * Time: 10:24
  * To change this template use File | Settings | File Templates.
  */
 public class IntegerConstantListInstance extends ListInstance<IntegerConstantInstance> {
     public IntegerConstantListInstance() {
         super();
     }
 
     public IntegerConstantListInstance(Collection<IntegerConstantInstance> integers) {
         super();
         this.getList().addAll(integers);
     }
 
     public IntegerConstantListInstance(IntegerConstantInstance[] integers) {
         super();
         for (IntegerConstantInstance instance:integers) {
             this.getList().add(instance);
         }
     }
 
     public IntegerConstantListInstance(int ... integers) {
         super();
         for (int i:integers) {
             this.list.add(new IntegerConstantInstance(i));
         }
     }
 
 
     @Override
     public IFirstOrderLogicInstance performSubstitutions(Map<VariableInstance, IUnifiableAtomInstance> substitutions) {
         IntegerConstantListInstance newListInstance = new IntegerConstantListInstance();
         for (IntegerConstantInstance term:list) {
            IntegerConstantInstance newTerm = (IntegerConstantInstance) term.performSubstitutions(substitutions);
             newListInstance.getList().add(newTerm);
         }
        list = newListInstance.getList();
        return this;
     }
 
     @Override
     public IFirstOrderLogicInstance deepClone(Map<VariableInstance, IUnifiableAtomInstance> substitutions) {
         IntegerConstantListInstance newListInstance = new IntegerConstantListInstance();
         for (IntegerConstantInstance term:list) {
             newListInstance.getList().add((IntegerConstantInstance) term.deepClone(substitutions));
         }
         return newListInstance;
     }
 
     @Override
     public IFirstOrderLogicInstance shallowClone() {
         IntegerConstantListInstance newListInstance = new IntegerConstantListInstance();
         for (IntegerConstantInstance term:list) {
            newListInstance.getList().add((IntegerConstantInstance) term.shallowClone());
         }
         return newListInstance;
     }
 
     @Override
     public boolean reduceToChoco(List<Map<VariableInstance, IUnifiableAtomInstance>> possSubst, HashMap<ITermInstance, Variable> termToVarMap) {
         if (!termToVarMap.containsKey(this)) {
             SetVariable setVariable = makeSetVar(NameGenerator.upperCaseNameGen.getNextName(), getMin(), getMax());
             termToVarMap.put(this,setVariable);
         }
         return true;
     }
 
     @Override
     public boolean inList(CharConstantListInstance constantList, List<Map<VariableInstance, IUnifiableAtomInstance>> possSubst) {
         throw new UnsupportedOperationException();
     }
 
     public int getMax() {
         int max = Integer.MIN_VALUE;
         for (IntegerConstantInstance constantInstance:list) {
             if (constantInstance.getValue()>max) max = constantInstance.getValue();
         }
         return max;
     }
 
     public int getMin() {
         int min = Integer.MAX_VALUE;
         for (IntegerConstantInstance constantInstance:list) {
             if (constantInstance.getValue()<min) min = constantInstance.getValue();
         }
         return min;
     }
 
     public int[] getIntArray() {
         int[] ints = new int[list.size()];
         int i =0;
         for (IntegerConstantInstance c:list) {
            ints[i]=c.getValue();
             i++;
         }
         return ints;
     }
 }
