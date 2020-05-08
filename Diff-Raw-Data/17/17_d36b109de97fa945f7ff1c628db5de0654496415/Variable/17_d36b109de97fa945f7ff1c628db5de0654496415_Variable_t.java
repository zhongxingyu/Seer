 package LJava;
 import static LJava.LJ.*;
 import static LJava.Utils.*;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.locks.ReentrantLock;
  
 
 public class Variable {
 
 	private Object[] value=new Object[0];
 	private boolean isVar=true;
 	private AtomicBoolean inSet=new AtomicBoolean(false);
 	private HashSet<Variable> looksAt= new HashSet<Variable>();
 	private Constraint constraint=new Constraint(LJFalse);
 	private static ReentrantLock lockKey = new ReentrantLock();
 
 
 	public final boolean equals(Object x) {		
 		x=val(x);
 		Object o=this.get();		
 		if (o==this) return (o==x);
 		return same(o,x);					
 	}
 
 
 //Properties		
 	public final String toString(){
 		if (this.noValue()) return none.toString();
 		StringBuilder s= new StringBuilder("[");
 		if (value.length>0) {
 			for (int i=0; i<value.length-1; i++) s.append(value[i]+",");
 			s.append(value[value.length-1].toString());
 		}
 		s.append("]");
 		if (constraint.asFormula()!=LJFalse) s.append(" OR "+constraint.toString());
 		return s.toString();
 	}
 
 
 	public final Object get(){
 		if (isVar()) return this;
 		if (noValue()) return none;
 		return flat();
 	}
 
 
 	public final Constraint getConstraint() {
 		return constraint.replaceVariable(this,this);
 	}
 
 
 	public final boolean contains(Object o) {
 		for (Object v : value)
 			if (same(v,o)) return true;
 		return constraint.satisfy(this, o);
 	}
 
 
 	public final Object[] getValues() {
 		if (isVar()) return new Object[]{undefined};
 		else if (noValue()) return new Object[]{none};
 		Object[] result = new Object[value.length];
 		for (int i=0; i<value.length; i++) result[i]=val(value[i]);
 		return result;
 	}
 
 
 	public final HashSet<Object> getValuesSet(){
 		HashSet<Object> valueSet = new HashSet<Object>();
 		for (int i=0; i<value.length; i++)
 			valueSet.add(val(value[i]));
 		return valueSet;
 	}
 
 
 	public final boolean equalValues(Variable x) {
 		if (x.value.length!=value.length) return false;
 		for (int i=0; i<value.length; i++)
 			if (!same(value[i],x.value[i])) return false;
 		return true;
 	}
 	
 	
 	public final boolean equalValuesSet(Variable x) {
 		HashSet<Object> thisSet = getValuesSet();
 		HashSet<Object> xSet = x.getValuesSet();
 		for (Object o : thisSet) if (!xSet.remove(o)) return false;
 		if (!xSet.isEmpty()) return false;
 		return true;
 	}
 
 
 //Turns the mutable Variable that has no value to an Immutable Variable that has meaning.
 	public final boolean instantiate(List<Object> vals, Constraint where, Constraint valByConstraint){
 		if (vals==null) vals = new LinkedList<Object>();
 		return instantiate(vals.toArray(), where, valByConstraint);
 	}
 	
 	public final boolean set(Object... args) {
 		return instantiate(args, null, null);
 	}
 	
 	public final boolean instantiate(Object[] vals, Constraint where, Constraint valByConstraint){
 		lockKey.lock();
 		if (isVar && inSet.compareAndSet(false,true)){
 			lockKey.unlock();
 			if (valByConstraint==null) valByConstraint=new Constraint(LJFalse);			
 			if (vals!=null && vals.length>0) {
 				ArrayList<Object> correct= new ArrayList<Object>();
 				if (where==null) where=new Constraint(LJTrue);
 				for (int i=0; i<vals.length; i++) {
 					if (variable(vals[i])) { 
 						if (!this.consistWith((Variable) vals[i])) {							
 							correct.add(undefined);
 							continue;
 						}					
 					}
 					if (where.satisfy(this, vals[i])) correct.add(vals[i]);
 				}			
 				if (!correct.isEmpty()) this.value=correct.toArray();				
 			}
 			this.constraint=valByConstraint;
 			isVar=false;
 			return true;						
 		}
 		lockKey.unlock();
 		return false;
 	}
 
 	
 //Identifying characteristics of this Variable
 	public final boolean isVar(){
 		return isVar;
 	}
 
 
 	public final boolean noValue(){
 		if (isVar()) return false;
 		return (value.length==0 && constraint.asFormula()==LJFalse);
 	}
 
 
 	public final boolean isPrimitive(){
 		return (get().getClass().isPrimitive());
 	}
 
 
 	public final boolean isConstraint(){
 		if ((!this.isVar()) && (!noValue()))
 			return (value.length>1 || constraint.asFormula()!=LJFalse);
 		return false;
 	}
 
 
 	public final boolean isRelation(){
 		return (get() instanceof Relation);
 	}
 
 
 	public final boolean isString(){
 		return (get() instanceof String);
 	}
 
 
 	public final boolean isInteger(){
 		return (get() instanceof Integer);
 	}
 
 
 	public final boolean isDouble(){
 		return (get() instanceof Double);	
 	}
 
 
 	public final boolean isFloat(){
 		return (get() instanceof Float);	
 	}
 
 
 	public final boolean ofClass(Class<?> c){						
 		return (get().getClass().equals(c));
 	}
 
 
 	public final boolean isNumber(){
 		return (get() instanceof Number);		
 	}		
 
 
 //Returns the variable value.
 	private final Object flat(){
 		if (value.length==0) return undefined;
 		return val(value[0]);
 	}
 
 
 //Tests all required characteristics for this Variable to be of a single value
 	public final boolean singleValue(){
 		if (isVar() || noValue() || isConstraint()) return false;
 		return true;
 	}
 
 
 //A consistency check for this Variable against another Variable.				
 	private final boolean consistWith(Variable b) {
 		if (b.isVar()){ 
 			if (!b.inSet.get()) { 
 				looksAt.add(b);  
 				return true; 
 			}
 			else return false;
 		}		
 		for (Variable element : b.looksAt) {			
 			if (element.isVar()) {
 				if (this==element) return false;
 				lockKey.lock();
 				if (!element.inSet.get()) looksAt.add(element);
 				else {
 					lockKey.unlock();
 					return false;			
 				}
 				lockKey.unlock();
 			}
 			else if (!this.consistWith(element)) return false;		
 		}		
 		return true;			
 	}
	
/* to fix:
 * consistWith and instantiate are long and locking. Need to figure out if there's another way to do them. 	
 */
 }
