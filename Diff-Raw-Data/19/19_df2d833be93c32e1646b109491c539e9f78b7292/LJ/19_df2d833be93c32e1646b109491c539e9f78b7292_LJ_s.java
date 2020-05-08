 package LJava;
 import static LJava.Utils.*;
 
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 
 public final class LJ {
 	
 	public final static Association _=new Association("_");
 	public final static Association undefined=new Association("$undefined$");
 	public static final Association none=new Association("$no_variable_value$");		
 	private static final HashMap<Integer, LinkedHashSet<Association>> LJavaRelationTable=new HashMap<Integer, LinkedHashSet<Association>>();
 
 	
 	public static void associate(Association r) {
 		addTo(LJavaRelationTable, r.argsLength(), r, LinkedHashSet.class);
 	}
 	
 
 	public static void relate(Object... args) {		
 		Relation r=new Relation("#LJavaRelationTableEntry#", args);
 		associate(r);
 	}	
 
 	
 	public static void group(Object... args) {		
 		Group r=new Group("#LJavaRelationTableEntry#", args);
 		associate(r);
 	}
 	
 			
 	public static boolean exists(Relation r) {		
 		VariableMap varValues=new VariableMap();		
 		if (!conduct(r, varValues, true)) return false;
 		return instantiate(varValues);
 	}
 	
 	
 	public static boolean exists(QueryParameter a, LogicOperator op, QueryParameter b){
 		//TBD
 		return false;
 	}
 	
 	
 	public static boolean exists(Object... args) {
 		Relation r=new Relation("#query",args);
 		return exists(r);
 	}
 		
 
 	public static boolean all(Relation r){		
 		VariableMap varValues=new VariableMap(); 
 		if (!conduct(r, varValues, false)) return false;
 		return instantiate(varValues);
 	}
 	
 	
 	public static boolean all(QueryParameter a, LogicOperator op, QueryParameter b){
 		if (op==OR) return instantiate(or(a,b));
 		if (op==AND) return instantiate(and(a,b));
 		if (op==DIFFER) return instantiate(differ(a,b));
 		//TBD
 		if (op==WHERE) return false; 
 		return false;
 	}
 	
 	
 	public static boolean all(VariableMap m){
 		return instantiate(m);
 	}	
 	
 	
 	public static boolean all(Object... args){
 		Relation r=new Relation("#query",args);
 		return all(r);		
 	}
 	
 	
 	public static VariableMap where(Variable x, Constraint c) {
 		//TBD
 		return null;
 	}
 	
 	
 	private static boolean searchOnIndexByName(int index, Relation r) {
 		LinkedHashSet<Association> associationsSet = LJavaRelationTable.get(index);		
 		if (associationsSet!=null) {
 			for (Association element : associationsSet)
 				if (element.associationNameCompare(r))	return true;			
 		}
 		return false;
 	}
 	
 	
 	private static boolean nameQuerying(Relation r) {
 		if (searchOnIndexByName(0, r)) return true;
 		if (searchOnIndexByName(-1, r)) return true;
 		return false;
 	}
 	
 	
 	private static boolean searchOnIndex(int index, Relation r, Object[] rArgs, VariableMap varValues, boolean cut) {
 		LinkedHashSet<Association> associationsSet = LJavaRelationTable.get(index);		
 		if (associationsSet!=null)
 			for (Association element : associationsSet) 
 				if (element.associationNameCompare(r) && element.satisfy(rArgs, varValues))
 						if (cut) return true;
 		return !varValues.isEmpty();
 	}
 	
 	
 	protected static boolean conduct(Relation r, VariableMap varValues, boolean cutFlag) {	
 		Object[] rArgs=r.args();
 		if (!cutFlag) {
 			for (Object o : rArgs) if (var(o)) { cutFlag=true;   break; }
 			cutFlag=!cutFlag;
 		}
 		if (rArgs.length==0) return nameQuerying(r);
 		if (searchOnIndex(rArgs.length, r, rArgs, varValues, cutFlag))
 			if (cutFlag) return true;
 		if (searchOnIndex(-1, r, rArgs, varValues, cutFlag))
 			if (cutFlag) return true;
 		if (varValues.isEmpty()) return false;
 		return true;
 	}
 	
 	
 	public static VariableMap and(QueryParameter a, QueryParameter b){
 		//TBD
 		return new VariableMap();
 	}
 	
 
 	public static VariableMap or(QueryParameter a, QueryParameter b){
 		//TBD
 		return new VariableMap();
 	}	
 	
 
 	public static VariableMap differ(QueryParameter a, QueryParameter b){
 		//TBD
 		return new VariableMap();
 	}
 	
 	
 	public static boolean var(Object x) {
 		if (variable(x)) return var(((Variable) x));
 		return false;
 	}
 	
 
 	public static boolean var(Variable x) {
 		return x.isVar();
 	}
 	
 	
 	public static Variable var() {
 		return new Variable();
 	}
 	
 	
 	public static Relation relation(String n,Object... args) {
 		return new Relation(n,args);
 	}
 	
 	
 	public static Relation relation(Object... args) {
 		return new Relation("",args);
 	}
 	
 	
 	@SuppressWarnings("rawtypes")
 	public static Constraint constraint(Formula f, Object... args) {
 		return new Constraint(f, args);
 	}
 	
 	
 	public static Object val(Object o) {
 		if (variable(o)) return ((Variable) o).get(); 
 		return o;
 	}
 	
 	
 	public static boolean same(Object a, Object b) {		
 		if ((variable(a)) || (a instanceof Association) )
 				return a.equals(b);
 		if ((a instanceof Number) && (b instanceof Number))
 				return ((Number) a).doubleValue()==((Number) b).doubleValue();
 		return b.equals(a);
 	}	
 	
 
 	private static synchronized boolean instantiate(VariableMap varValues) {
         boolean answer=true;
 		for (Variable v : varValues.getVars())													
			answer=(v.instantiateByList(varValues.map.get(v), null, varValues.constraints.get(v)) && answer);	
 		return answer;
 	}
 }
 
 
 /* Problems to solve:
  * exists(relation(1,2,x),LogicalOperator,relation(1,3,x))
  * exists/all(relation(1,2,x),LogicalOperator,formula(cmp,1,y,x))
  * exists/all(relation(1,2,x),LogicalOperator,formula(cmp,1,y,3))
  * exists/all(relation(1,2,x),LogicalOperator,relation(1,y,x))
  * exists/all(relation(1,2,x),LogicalOperator,relation(1,y,3))
  * exists/all(group(1,2,x),LogicalOperator,formula(cmp,1,x,3))
  * exists/all(group(1,2,x),LogicalOperator,formula(cmp,1,x,y))
  * exists/all(group(1,2,x),LogicalOperator,formula(cmp,1,y,3))
  * 
  * to fix:
  * All the TBD in the class.
  * instantiate is synchronized because a variable might get instantiated while it's follower will return false from instantiation and that's incoherent. Need to figure out how to do it in a different way.
  */
 
 
 
