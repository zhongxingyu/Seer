 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.co.mtford.alp.abduction.logic.instance;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import uk.co.mtford.alp.abduction.asystem.DenialInstance;
 import uk.co.mtford.alp.abduction.asystem.IASystemInferable;
 import uk.co.mtford.alp.abduction.tools.UniqueIdGenerator;
 
 /**
  *
  * @author mtford
  */
 public class VariableInstance implements ITermInstance {
     
     private static final Logger LOGGER = Logger.getLogger(VariableInstance.class);
    private final int uniqueId = UniqueIdGenerator.getUniqueId();
     
     String name;
     IAtomInstance value;
 
     public int getUniqueId() {
         return uniqueId;
     }
 
     public VariableInstance(String name, IAtomInstance value) {
         this.name = name;
         this.value = value;
     }
 
     public VariableInstance(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     /** Returns the actual assigned value.
      * 
      * @return 
      */
     public IAtomInstance getValue() {
         return value;
     }
     
     /** Iterates through the assignment structure and returns the deepest value.
      *  i.e. X=Y=Z=john.
      * @return 
      */
     public IAtomInstance getDeepValue() {
         IAtomInstance v = this;
         while (v instanceof VariableInstance) {
             IAtomInstance value = ((VariableInstance) v).getValue();
             if (value!=null) v=value;
             else break;
         }
         return v;
     }
 
     public void setValue(IAtomInstance value) {
        this.value = value;
     }
     
     public boolean isAssigned() {
         return value!=null;
     }
     
     @Override
     public String toString() {
         if (value==null) return name+"<"+uniqueId+">";
         return "("+name+"<"+uniqueId+">"+"="+value.toString()+")";
     }
 
     /** Returns true if variable names at the same. Not concerned with value. */
     
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
         hash = 89 * hash + (this.value != null ? this.value.hashCode() : 0);
         return hash;
     }
 
     @Override
     public Object clone() {
         String clonedName = new String(name);
         IAtomInstance clonedValue = null;
         if (value!=null) {
             clonedValue = (IAtomInstance) value.clone();
         }
         return new VariableInstance(clonedName,clonedValue);
     }
 
     /** Returns true if same name.
      * 
      * @param obj
      * @return 
      */
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final VariableInstance other = (VariableInstance) obj;
         if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
             return false;
         }
         return true;
     }
     
     /** Returns true if same name or same value.
      * 
      * @param obj
      * @return 
      */
     public boolean deepEquals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (obj instanceof ConstantInstance) {
             ConstantInstance constant = (ConstantInstance) obj;
             if (value == null ) {
                 return false;
             }
             IAtomInstance realValue = value;
             while (realValue instanceof VariableInstance) {
                 realValue=((VariableInstance)realValue).value;
             }
             if (realValue== null) {
                 return false;
             }
             if (realValue instanceof ConstantInstance) {
                 return value.equals(constant);
             }
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final VariableInstance other = (VariableInstance) obj;
         if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
             return false;
         }
         if (this.value != other.value && (this.value == null || !this.value.deepEquals(other.value))) {
             return false;
         }
         return true;
     }
 
     public Object clone(Map<String, VariableInstance> variablesSoFar) {
         if (variablesSoFar.containsKey(name+"<"+uniqueId+">")) {
             return variablesSoFar.get(name+"<"+uniqueId+">");
         }
         else {
             VariableInstance clone = new VariableInstance(name);
             if (value!=null) clone.setValue((IAtomInstance)value.clone(variablesSoFar));
             variablesSoFar.put(name+"<"+uniqueId+">",clone);
             return clone;
         }
     }
 
     @Override
     public List<VariableInstance> getVariables() {
         List<VariableInstance> vars = new LinkedList<VariableInstance>();
         vars.add(this);
         return vars;
     }
 
     @Override
     public List<IASystemInferable> positiveEqualitySolve(IAtomInstance other) {
         // TODO Occurs check. If fails occurs check, return false instance.
         LinkedList<IASystemInferable> newInferables = new LinkedList<IASystemInferable>();
         if (other instanceof VariableInstance) other=((VariableInstance) other).getDeepValue();
         IAtomInstance deepValue = this.getDeepValue();
         if (deepValue instanceof VariableInstance) {
             ((VariableInstance) deepValue).setValue(other);
             return newInferables;
         }
         else if (deepValue instanceof ConstantInstance) {
              if (other instanceof ConstantInstance) {
                  return deepValue.positiveEqualitySolve(other);
              }
              else if (other instanceof VariableInstance) {
                  ((VariableInstance) other).setValue(deepValue);
                  return newInferables;
              }
         }
         newInferables.add(new FalseInstance());
         return newInferables;
     }
 
     @Override
     public List<IASystemInferable> negativeEqualitySolve(DenialInstance denial, IAtomInstance other) {
         // TODO Occurs check. If fails occurs check, return false instance.
         /*List<IASystemInferable> newInferables = new LinkedList<IASystemInferable>();
         denial.addLiteral(0,positiveEqualitySolve(other));
         newInferables.add(denial);
         return newInferables; */
         //boolean isUniversallyQuantified =
         return null;
     }
 }
