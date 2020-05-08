 package uk.co.mtford.jalp.abduction.logic.instance.term;
 
 import choco.Choco;
 import choco.kernel.model.variables.Variable;
 import org.apache.log4j.Logger;
 import uk.co.mtford.jalp.abduction.logic.instance.IFirstOrderLogicInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.IUnifiableAtomInstance;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mtford
  * Date: 30/05/2012
  * Time: 09:27
  * To change this template use File | Settings | File Templates.
  */
 public class IntegerConstantInstance extends ConstantInstance {
     private static final Logger LOGGER = Logger.getLogger(ConstantInstance.class);
     private Integer value;
 
     public IntegerConstantInstance(Integer value1) {
         value = value1;
     }
 
     public Integer getValue() {
         return value;
     }
 
     public void setValue(Integer value) {
         this.value = value;
     }
 
     @Override
     public IFirstOrderLogicInstance deepClone(Map<VariableInstance, IUnifiableAtomInstance> substitutions) {
        return new IntegerConstantInstance(new Integer(value));
     }
 
     @Override
     public IFirstOrderLogicInstance shallowClone() {
        return new IntegerConstantInstance(new Integer(value));
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof IntegerConstantInstance)) return false;
 
         IntegerConstantInstance that = (IntegerConstantInstance) o;
 
         if (!value.equals(that.value)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return value.hashCode();
     }
 
     @Override
     public String toString() {
         return value.toString();
     }
 
     @Override
     public boolean reduceToChoco(List<Map<VariableInstance, IUnifiableAtomInstance>> possSubst, HashMap<ITermInstance, Variable> termToVarMap) {
         if (!termToVarMap.containsKey(this)) {
             termToVarMap.put(this,Choco.constant(value));
         }
         return true;
     }
 
     @Override
     public boolean inList(CharConstantListInstance constantList, List<Map<VariableInstance, IUnifiableAtomInstance>> possSubst) {
         throw new UnsupportedOperationException(); // Dealt with by Choco.
     }
 }
