 package uk.co.mtford.jalp.abduction.logic.instance.term;
 
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
 public class CharConstantInstance extends ConstantInstance {
 
     private static final Logger LOGGER = Logger.getLogger(CharConstantInstance.class);
     private String value;
 
     public CharConstantInstance(String value) {
         this.value = value;
     }
 
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 
     @Override
     public IFirstOrderLogicInstance deepClone(Map<VariableInstance, IUnifiableAtomInstance> substitutions) {
         return new CharConstantInstance(value);
     }
 
     @Override
     public IFirstOrderLogicInstance shallowClone() {
        return new CharConstantInstance(new String(value));
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof CharConstantInstance)) return false;
 
         CharConstantInstance that = (CharConstantInstance) o;
 
         if (value != null ? !value.equals(that.value) : that.value != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return value != null ? value.hashCode() : 0;
     }
 
     @Override
     public String toString() {
         return value;
     }
 
     @Override
     public boolean reduceToChoco(List<Map<VariableInstance, IUnifiableAtomInstance>> possSubst, HashMap<ITermInstance, Variable> termToVarMap) {
         throw new UnsupportedOperationException(); // Dealt with natively.
     }
 
     @Override
     public boolean inList(CharConstantListInstance constantList, List<Map<VariableInstance, IUnifiableAtomInstance>> possSubst) {
         return constantList.getList().contains(this);
     }
 }
 
