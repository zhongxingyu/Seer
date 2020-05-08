 package LJava;
 import static LJava.Utils.*;
 
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 
 
 public final class LJ {
 	
 	public final static Association _=new Association("_");
 	public final static Association nil=new Association("$nil$");
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
 	
 			
 	public static QueryResult e(Relation r) {		
 		VariableValuesMap varValues=new VariableValuesMap();		
 		if (conduct(r, varValues, true)==FAILED) return FAILED;
 		return instantiate(varValues);
 	}
 	
 	
 	public static QueryResult e(Object... args) {
 		Relation r=new Relation("#query",args);
 		return e(r);
 	}
 		
 
 	public static QueryResult a(Relation r){		
 		VariableValuesMap varValues=new VariableValuesMap(); 
 		if (conduct(r, varValues, false)==FAILED) return FAILED;
 		return instantiate(varValues);
 	}
 	
 	
 	public static QueryResult a(QueryParameter a, LogicOperator op, QueryParameter b){
 		if (op==OR) return instantiate(or(a,b));
 		if (op==AND) return instantiate(and(a,b));
 		if (op==DIFFER) return instantiate(differ(a,b));
 		return FAILED;
 	}
 	
 	
 	public static QueryResult a(VariableValuesMap m){
 		return instantiate(m);
 	}	
 	
 	
 	public static QueryResult a(Object... args){
 		Relation r=new Relation("#query",args);
 		return a(r);		
 	}	
 	
 	
 	private static boolean searchOnIndexByName(int index, Relation r) {
 		LinkedHashSet<Association> associationsSet = LJavaRelationTable.get(index);		
 		if (associationsSet!=null) {
 			for (Association element : associationsSet)
 				if (element.associationNameCompare(r))	return true;			
 		}
 		return false;
 	}
 	
 	
 	private static QueryResult nameQuerying(Relation r) {
 		if (searchOnIndexByName(0, r)) return SUCCESS;
 		if (searchOnIndexByName(-1, r)) return SUCCESS;
 		return FAILED;
 	}
 	
 	
 	private static boolean searchOnIndex(int index, Relation r, Object[] rArgs, VariableValuesMap varValues, boolean cut) {
 		LinkedHashSet<Association> associationsSet = LJavaRelationTable.get(index);		
 		if (associationsSet!=null) {
 			for (Association element : associationsSet) 
 				if (element.associationNameCompare(r))
 					if (element.satisfy(rArgs, varValues))
 						if (cut) return true;
 		}
 		if (varValues.map.isEmpty()) return false;
 		return true;	
 	}
 	
 	
 	protected static QueryResult conduct(Relation r, VariableValuesMap varValues, boolean cutFlag) {	
 		Object[] rArgs=r.args();
 		if (rArgs.length==0) return nameQuerying(r);
 		if (!cutFlag) {
 			for (int i=0; i<rArgs.length; i++)
 				if (var(rArgs[i])) {	cutFlag=true;	break;	}
 			cutFlag=(!cutFlag);
 		}
 		if (searchOnIndex(rArgs.length, r, rArgs, varValues, cutFlag))
 			if (cutFlag) return SUCCESS;
 		if (searchOnIndex(-1, r, rArgs, varValues, cutFlag))
 			if (cutFlag) return SUCCESS;
 		if (varValues.map.isEmpty()) return FAILED;
 		return SUCCESS;		
 	}
 	
 	
 	public static VariableValuesMap and(QueryParameter a, QueryParameter b){
 		VariableValuesMap x=a.map();
 		if (x.isEmpty()) return x;
 		VariableValuesMap y=b.map();
 		ArrayList<Object> xValues;
 		for (Map.Entry<Variable, ArrayList<Object>> entry : y.map.entrySet()) {
 			xValues=x.map.get(entry.getKey());
 			if (xValues!=null) xValues.retainAll(entry.getValue());
 			if (xValues.isEmpty()) x.map.remove(entry.getKey());
 		}		
 		return x;
 	}
 	
 
 	public static VariableValuesMap or(QueryParameter a, QueryParameter b){
 		VariableValuesMap x=a.map();
 		VariableValuesMap y=b.map();
 		ArrayList<Object> xValues;
 		for (Map.Entry<Variable, ArrayList<Object>> entry : y.map.entrySet()) {
 			xValues=x.map.get(entry.getKey());
 			if (xValues==null) x.map.put(entry.getKey(), entry.getValue());
 			else xValues.addAll(entry.getValue());
 		}		
 		return x;
 	}	
 	
 
 	public static VariableValuesMap differ(QueryParameter a, QueryParameter b){
 		VariableValuesMap x=a.map();
 		if (x.isEmpty()) return x;
 		VariableValuesMap y=b.map();
 		ArrayList<Object> xValues;
 		for (Map.Entry<Variable, ArrayList<Object>> entry : y.map.entrySet()) {
 			xValues=x.map.get(entry.getKey());
 			if (xValues!=null) xValues.removeAll(entry.getValue());
 			if (xValues.isEmpty()) x.map.remove(entry.getKey());			
 		}		
 		return x;	
 	}
 	
 	
 	public static boolean var(Object x) {
 		if (variable(x)) {
 			Variable y=(Variable) x;
 			return var(y);
 		}
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
 	
 	
 	public static Object val(Object o) {
 		if (variable(o)) {
 			Variable v=(Variable) o;
 			return v.get();
 		}
 		return o;
 	}
 	
 	
 	public static boolean same(Object a, Object b) {		
 		if ( (variable(a)) || (a instanceof Association) )
 				return a.equals(b);
 		return b.equals(a);
 	}	
 	
 
 	private static QueryResult instantiate(VariableValuesMap varValues) {
         boolean answer=true;		
 		for (Map.Entry<Variable, ArrayList<Object>> entry : varValues.map.entrySet())													
 			answer=(entry.getKey().set(entry.getValue().toArray()) && answer);	
 		if (answer) return SUCCESS;
 		return QueryResult.FAILED_INSTANTIATE;
 	}
 
 		
 	protected static void updateValuesMap(HashMap<Variable,Object> vars, VariableValuesMap varValues){		
 		for (Map.Entry<Variable, Object> entry : vars.entrySet()) 
 			updateValuesMap(entry.getKey(), entry.getValue(), varValues);			
 	}
 	
 	
 	protected static void updateValuesMap(Variable key, Object val, VariableValuesMap varValues) {
 		addTo(varValues.map, key, val, ArrayList.class);
 	}	
 }
 
 
 /* Future Plan:
  * Logical operators: for exists, with functors, logically correct AND, smarter!
 * Solving infinite and finite groups of values from formula into variable - simply if the formula result is boolean or a value.
 * where (A formula with variables in the arguments for logical operators).
  * Atomitize the line in Variable.consistWith.
  * Utils Functors: sum, sub, multi, div, mod, sqr, sqrt, pow.
 * Fixed size Functors.
  * reverse functors: By receiving an array of functors, one for each param. returning the relation none if no functor to a certain index.
 * Logical AI.
  */
 
 
 
