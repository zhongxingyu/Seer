 package soot.jimple.infoflow.data;
 
 import soot.EquivalentValue;
 import soot.Local;
 import soot.SootField;
 import soot.Value;
 import soot.jimple.InstanceFieldRef;
 import soot.jimple.StaticFieldRef;
 
 public class AccessPath {
 	private Value value;
 	private SootField field;
 	//in contrast to a certain value which is tainted unknownfieldtainted says that any (*) field of the value is tainted
 	private boolean unknownfieldtainted; //also known as star/*
 
 	
 	public AccessPath(Value val){
 		assert !(val instanceof EquivalentValue);
 		if(val instanceof StaticFieldRef){
 			StaticFieldRef ref = (StaticFieldRef) val;
 			field = ref.getField();
 		} else if(val instanceof InstanceFieldRef){
 			InstanceFieldRef ref = (InstanceFieldRef) val;
 			value = ref.getBase();
 			field = ref.getField();
 		}else{
 			value = val;
 		}
 		unknownfieldtainted = false;
 	}
 	
 	
 	public AccessPath(Value val, boolean fieldtainted){
 		this(val);
 		unknownfieldtainted = fieldtainted;
 	}
 	
 	
 	public AccessPath(SootField staticfield){
 		field = staticfield;
 	}
 	
 	public AccessPath(Value base, SootField field){
 		value = base;
 		this.field = field;
 	}
 
 	
 	/**
 	 * replaces value and returns it if matches with val, otherwise original is returned
 	 * @param val
 	 * @param replacement
 	 * @return
 	 */
 	public AccessPath replace(Value val, Value replacement){
 		if(val instanceof Local){
 			if(val.equals(value)){
 				return new AccessPath(replacement);
 			}
 		} 
 		return this;
 	}
 		
 	public Value getPlainValue() {
 		if(value == null){
 			return null;
 		}
 		return value;
 	}
 	
 	public Local getPlainLocal(){
 		if(value != null && value instanceof Local){
 			return (Local)value;
 		}
 		return null;
 	}
 	
 	public void setValue(Value value) {
 		this.value = value;
 	}
 	
 	
 	public SootField getField() {
 		return field;
 	}
 	public void setField(SootField field) {
 		this.field = field;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((field == null) ? 0 : field.hashCode());
 		result = prime * result + (unknownfieldtainted ? 1231 : 1237);
 		result = prime * result + ((value == null) ? 0 : value.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		AccessPath other = (AccessPath) obj;
 		if (field == null) {
 			if (other.field != null)
 				return false;
 		} else if (!field.equals(other.field))
 			return false;
 		if (unknownfieldtainted != other.unknownfieldtainted)
 			return false;
 		if (value == null) {
 			if (other.value != null)
 				return false;
 		} else if (!value.equals(other.value))
 			return false;
 		return true;
 	}
 	
 	public boolean isStaticFieldRef(){
 		if(value == null && field != null){
			assert (field.makeRef() instanceof StaticFieldRef || field.makeRef().isStatic());
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean isInstanceFieldRef(){
 		if(value != null && field != null){
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * only fields (*) are tainted, not the object itself 
 	 * @return
 	 */
 	public boolean isOnlyFieldsTainted(){
 		return unknownfieldtainted;
 	}
 	
 	public boolean isLocal(){
 		if(value != null && value instanceof Local && field == null){
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public String toString(){
 		String str = "";
 		if(value != null){
 			str += value.toString() +"(" + value.getType() +")" + " ";
 		}
 		if(field != null){
 			str += field.toString();
 		}
 		if(unknownfieldtainted)
 			str += ".*";
 		return str;
 	}
 	
 	public AccessPath copyWithNewValue(Value val){
 		if(val instanceof Local){
 			AccessPath a = new AccessPath(val);
 			a.field = this.field;
 			a.unknownfieldtainted = this.unknownfieldtainted;
 			return a;
 		}else{
 			if(val instanceof InstanceFieldRef && field != null){
 				AccessPath a = new AccessPath(val);
 				a.unknownfieldtainted = true;
 				return a;
 			}
 			
 			return new AccessPath(val);
 		}
 	}
 
 }
