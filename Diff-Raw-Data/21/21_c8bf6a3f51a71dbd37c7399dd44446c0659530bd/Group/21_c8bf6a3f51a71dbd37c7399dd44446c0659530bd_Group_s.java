 package LJava;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeMap;
 
 import static LJava.LJ.var;
 import static LJava.LJ.val;
 
 
 public class Group extends Association{
 	
 	private final Map<Object, Integer> argsMap;
 	
 	public Group(String n, Object... params) {
 		super(n, params);
 		HashMap<Object, Integer> map=new HashMap<Object, Integer>();
 		if (params!=null)
 			for (Object element : params) increment(map, element,1);		
 		argsMap=Collections.unmodifiableMap(map);
 	}
 	
 
 	@Override
 	public boolean isGroup() {
 		return true;
 	}
 	
 	
 	@Override
 	protected boolean satisfy(Object[] rArgs, VariableMap varValues){	
 		HashMap<Variable, Integer> rVarsCountMap=new HashMap<Variable, Integer>();
 		HashMap<Object, Integer> remainedVals=new HashMap<Object, Integer>();
 		HashMap<Object, Integer> rArgsCountMap=new HashMap<Object, Integer>();
 		for (Object element : rArgs) {
 			if (var(element)) increment(rVarsCountMap,(Variable) element,1);
 			else increment(rArgsCountMap, val(element),1);
 		}
 		
 		Integer count=0;
 		for (Map.Entry<Object, Integer> entry : argsMap.entrySet()) { //Differing amounts between group's map and r's map
 			Object key=entry.getKey();
 			Object keyVal=val(key);
 			if (var(keyVal)) count=rVarsCountMap.remove(keyVal);
 			else count=rArgsCountMap.remove(keyVal);
 			if (count==null) count=0; 					
 			count=count-entry.getValue();
 			if (count>0) {
 				if (var(keyVal)) rVarsCountMap.put((Variable) keyVal, count);
 				else return false;
 			}
 			else if (count<0) remainedVals.put(keyVal, -count); 
 		}	
 		if (!rArgsCountMap.isEmpty()) return false;
 		if (rVarsCountMap.isEmpty()) return true;
 		TreeMap<Variable, Integer> vars = new TreeMap<Variable, Integer>(new MapComparatorByValue<Variable>(rVarsCountMap));
 		vars.putAll(rVarsCountMap);
 		return (setValsToVars(vars,remainedVals,varValues, 0)>0);
 	}
 	
 	
 	private int setValsToVars(TreeMap<Variable, Integer> vars, HashMap<Object, Integer> vals, VariableMap varResults, int recordsAdded) {
 		if (vars.isEmpty()) return 1;
 		Variable var=vars.firstKey();
 		Integer varAmount = vars.remove(var);
 		Iterator<Map.Entry<Object, Integer>> valIterator=vals.entrySet().iterator();
 		while (valIterator.hasNext()) {
 			Map.Entry<Object, Integer> valAmount = valIterator.next();
 			if (valAmount.getValue()<varAmount) continue;
			int remain=valAmount.getValue()-varAmount;
			valAmount.setValue(remain);
 			int recordsReturned=setValsToVars(vars, vals, varResults, 0);
 			if (recordsReturned<1) break;
 			recordsAdded=recordsAdded+recordsReturned; 
			valAmount.setValue(remain+varAmount);
 			for (int i=0; i<recordsReturned; i ++) varResults.updateValsMap(var, valAmount.getKey());
 		}
 		vars.put(var, varAmount);
 		return recordsAdded;
 	}
 	
 	
 	private <T> void increment(Map<T, Integer> m, T element, int delta) {
 		Integer count=m.get(element);
 		if (count==null) count=0;
 		m.put(element,count+delta);	
 	}
 
 		
 	private class MapComparatorByValue<T> implements Comparator<T> {	
 		Map<T,Integer> sourceMap;		
 		public MapComparatorByValue(HashMap<T,Integer> m) {
 			sourceMap=m;
 		}		
 		@Override
 		public int compare(T a, T b) {
 			if (sourceMap.get(a)>sourceMap.get(b)) return -1;
 			else if (sourceMap.get(a)<sourceMap.get(b)) return 1;
 			int aHash=a.hashCode();
 			int bHash=b.hashCode();
 			if (aHash>bHash) return -1;
 			else if (aHash<bHash) return 1;
 			return 0;
 		}
 	}
 	
 	
 /* to fix:
 * currently Group is running permutations of itself even if it already found an answer to exists(). It is slower then it can be: should CUT inside Group as needed.
 * Still running on all values (including those with amount=0). It might be slightly more efficient...	
  */
 }
