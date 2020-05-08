 package LJava;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.TreeMap;
 
 import static LJava.LJ.updateValuesMap;
 import static LJava.LJ.var;
 import static LJava.LJ.val;
 
 
 public class Group extends Association{
 	
 	private final Map<Object, Integer> argsMap;
 	
 	public Group(String n, Object... params) {
 		super(n, params);
 		HashMap<Object, Integer> map=new HashMap<Object, Integer>();
 		if (params!=null) {
 			for (Object element : params) increment(map, element);		
 		}
 		argsMap=Collections.unmodifiableMap(map);
 	}
 	
 
 	@Override
 	public boolean isGroup() {
 		return true;
 	}
 	
 	@Override
 	protected boolean satisfy(Relation r, VariableValuesMap varValues){
 		if (r.argsLength>0) {
 			HashMap<Object, Integer> constraintMap=new HashMap<Object, Integer>();
 			HashMap<Object, Integer> vals=new HashMap<Object, Integer>();
 			HashMap<Variable, Integer> vars=new HashMap<Variable, Integer>();
 			for (Object element : r.args) { //Mapping r's parameters.
 				if (var(element)) increment(vars, (Variable) element);
 				else increment(vals, val(element));
 			}
 					
 			Integer count=0;
 			for (Map.Entry<Object, Integer> entry : argsMap.entrySet()) { //Differing amounts between group's map and r's map			
 				if (var(entry.getKey())) {
 					count=vars.get((Variable) entry.getKey());
 					if (count==null) constraintMap.put(val(entry.getKey()), entry.getValue());					
 					else {
 						count=count-entry.getValue();
 						if (count==0) vars.remove((Variable) entry.getKey());
 						else if (count<0) constraintMap.put(val(entry.getKey()), -count);
 						else vars.put((Variable) entry.getKey(), count);
 					}
 					count=0;
 				}
 				else {
 					count=vals.get(val(entry.getKey()));
 					if (count==null) count=0;
 					else vals.remove(val(entry.getKey()));
 					count=entry.getValue()-count;					
 					if (count>0) constraintMap.put(val(entry.getKey()), count);					
 				}
 				if (count<0) return false;
 			}
 			if (!vals.isEmpty()) return false;
 			
 			if (vars.isEmpty()) return true;
 			if (constraintMap.isEmpty()) return false;
 					
 			
 			TreeMap<Integer, LinkedList<Object>> sortedVals= new TreeMap<Integer, LinkedList<Object>>();
 			LinkedList<Object> valList=new LinkedList<Object>(); 
 			for (Map.Entry<Object, Integer> entry : constraintMap.entrySet()) {
 				valList=sortedVals.get(entry.getValue());
 				if (valList==null) valList=new LinkedList<Object>();
 				valList.add(entry.getKey());
 				sortedVals.put(entry.getValue(), valList);
 			}
 			
			TreeMap<Integer, LinkedList<Variable>> sortedVars= new TreeMap<Integer, LinkedList<Variable>>();
 			LinkedList<Variable> varList=new LinkedList<Variable>(); 
 			for (Map.Entry<Variable, Integer> entry : vars.entrySet()) {
 				varList=sortedVars.get(entry.getValue());
 				if (varList==null) varList=new LinkedList<Variable>();
 				varList.add(entry.getKey());
 				sortedVars.put(entry.getValue(), varList);
 			}
 			
 			HashMap<Variable,Object> varResults=new HashMap<Variable, Object>();
 			for (Map.Entry<Integer, LinkedList<Variable>> entry : sortedVars.entrySet()) {				
 				for (Variable v : entry.getValue()) {
 					int lastKey=sortedVals.lastKey();
 					if (lastKey<entry.getKey()) return false;
 					
 					valList=sortedVals.get(lastKey);
 					Object o=valList.get(0);
 					varResults.put(v,o);
 					valList.remove(0);
 					int place=lastKey-entry.getKey();
 					if (place>0) {
 						valList=sortedVals.get(place);
 						if (valList==null) valList=new LinkedList<Object>();
 						valList.add(o);
 						sortedVals.put(place, valList);
					}					
 				}
 			}
 			updateValuesMap(varResults, varValues);
 		}		
 		return true;
 	}
 	
 	
 	
 	
 	private <T> void increment(HashMap<T, Integer> m, T element) {
 		Integer count=m.get(element);
 		if (count==null) m.put(element,1);
 		else m.put(element,count+1);		
 	}
 }
