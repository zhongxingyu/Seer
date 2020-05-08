 package LJava;
 import static LJava.LJ.increment;
 import static LJava.LJ.val;
 import static LJava.LJ.var;
 
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.TreeMap;
 
 import LJava.Group;
 import LJava.Variable;
 import LJava.VariableMap;
 
 
 public class Lazy extends Association implements Iterator<VariableMap>, Iterable<VariableMap> {
 		
 	private interface LazyItem {
 		public boolean satisfy(Object[] rArgs, VariableMap varValues);
 		public boolean lazy(VariableMap varValues);
 		public VariableMap lazy();
 		public VariableMap current();
 		public String toString();
 		public boolean isEmpty();
 		public void startLazy();
 	}
 	
 	private class LazyGroup extends Association implements LazyItem {
 		private class VarIterator {
 			Iterator<Map.Entry<Object, Integer>> iterator;
 			Variable var;
 			int count;
 			public VarIterator(Iterator<Map.Entry<Object, Integer>> i, Variable v, int c) {
 				iterator=i;			var=v;		count=c;
 			}
 		}			
 	
 		private VariableMap answer=new VariableMap();
 		private LinkedList<VarIterator> iStack=new LinkedList<VarIterator>();
 		private TreeMap<Variable, Integer> varsCount=new TreeMap<Variable, Integer>();
 		private HashMap<Object, Integer> valsCount=new HashMap<Object, Integer>();
 		
 		public LazyGroup(Group group, Object[] rArgs) {
 			super(group.name, group.args);
 			HashMap<Variable, Integer> rVarsCountMap=new HashMap<Variable, Integer>();
 			HashMap<Object, Integer> rArgsCountMap=new HashMap<Object, Integer>();
 			buildValsAndVarsCount(rArgs, rVarsCountMap, rArgsCountMap);
 			elimination(rVarsCountMap, rArgsCountMap, group);
 		}
 		
 		private final void buildValsAndVarsCount(Object[] rArgs, HashMap<Variable, Integer> rVarsCountMap, HashMap<Object, Integer> rArgsCountMap) {
 			for (Object element : rArgs) {
 				if (var(element)) increment(rVarsCountMap,(Variable) element,1);
 				else increment(rArgsCountMap, val(element),1);
 			}			
 		}
 		
 		private final void elimination(HashMap<Variable, Integer> rVarsCountMap, HashMap<Object, Integer> rArgsCountMap, Group g) {
 			Integer count=0;
 			for (Map.Entry<Object, Integer> entry : g.argsMap.entrySet()) { //Differing amounts between group's map and r's map
 				Object keyVal=val(entry.getKey());
 				count=(var(keyVal))? rVarsCountMap.remove(keyVal) : rArgsCountMap.remove(keyVal);
 				count=(count==null)? -entry.getValue() : count-entry.getValue();
 				if (count>0) {
 					if (!var(keyVal)) return;
 					rVarsCountMap.put((Variable) keyVal, count);
 				}
 				else if (count<0) valsCount.put(keyVal, -count); 
 			}
 			if (rArgsCountMap.isEmpty() && !rVarsCountMap.isEmpty()) {				
 				varsCount = new TreeMap<Variable, Integer>(new MapComparatorByValue<Variable>(rVarsCountMap));
 				varsCount.putAll(rVarsCountMap);
 				iStack.push(new VarIterator(valsCount.entrySet().iterator(), varsCount.firstKey(), varsCount.get(varsCount.firstKey())));
 			}			
 		}
 		
 		@Override
 		public boolean satisfy(Object[] rArgs, VariableMap varValues) {
 			return lazy(varValues);
 		}
 		
 		@Override
 		public final boolean lazy(VariableMap varValues) {
 			while (!iStack.isEmpty()) {
 				VarIterator i=iStack.pop();
 				if (varsCount.get(i.var)==null) backtrack(i);
 				while (i.iterator.hasNext()) {
 					Map.Entry<Object, Integer> entry=i.iterator.next();
 					int difference=entry.getValue()-i.count;
 					if (difference<0) continue;
 					entry.setValue(difference);
 					varsCount.remove(i.var);
 					answer.updateValsMap(i.var, entry.getKey());
 					iStack.push(i);
 					if (varsCount.isEmpty()) {
 						varValues.add(answer);
 						return true;
 					}
 					i=new VarIterator(valsCount.entrySet().iterator(), varsCount.firstKey(), varsCount.remove(varsCount.firstKey()));
 				}
 			}
 			return false;
 		}
 		
 		@Override
 		public final VariableMap lazy() {
 			VariableMap m=new VariableMap();
 			if (!lazy(m)) return new VariableMap();
 			return m;
 		}
 		
 		@Override
 		public final VariableMap current() {
 			VariableMap map=new VariableMap();
 			map.add(answer);
 			return map;
 		}
 		
 		private final void backtrack(VarIterator i) {
 			Object key=answer.map.remove(i.var).get(0);
 			valsCount.put(key, valsCount.get(key)+i.count);
 			varsCount.put(i.var, i.count);			
 		}
 		
 		@Override
 		public final String toString(){
 			StringBuilder sb = new StringBuilder("lazy "+name+"( VARS: ");
 			sb.append(varsCount.toString());
 			sb.append("  ;  VALUES: ");
 			sb.append(valsCount.toString());
 			sb.append(" )");
 			return sb.toString();
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
 		
 		@Override
 		public boolean isEmpty() {
 			return varsCount.isEmpty();
 		}
 		
 		@Override
 		public void startLazy() {
 			while (!iStack.isEmpty()) {
 				VarIterator i=iStack.pop();
 				if (varsCount.get(i.var)==null) backtrack(i);				
 			}
 			if (!varsCount.isEmpty()) iStack.push(new VarIterator(valsCount.entrySet().iterator(), varsCount.firstKey(), varsCount.get(varsCount.firstKey())));
 		}
 	}
 	
 	
 //LazyAll	
 	private class LazyAll implements LazyItem {
 		
 		private final Constraint c;
 		
 		public LazyAll(Constraint constraint) {
 			c=constraint;
 		}
 		
 		@Override
 		public boolean satisfy(Object[] rArgs, VariableMap varValues){
 			return false;
 		}
 		
 		@Override
 		public boolean lazy(VariableMap varValues) {
 			return c.lazy(varValues);
 		}
 		
 		@Override
 		public VariableMap lazy() {
 			return c.lazy();
 		}
 		
 		@Override
 		public VariableMap current() {
 			return c.current();
 		}
 		
 		@Override
 		public String toString() {
 			return c.toString();
 		}
 		
 		@Override
 		public boolean isEmpty() {
 			return c.getVars().isEmpty();
 		}
 		
 		@Override
 		public void startLazy() {
 			c.startLazy();
 		}		
 	}
 	
 	
 //Start of class Lazy	
 	private final LazyItem lazy;
 	private final HashSet<Integer> ignore=new HashSet<Integer>();
 	private int currentI=0;
 	private boolean end=false;
 
 	
 	public Lazy(Group group, Object[] rArgs) {
 		super(group.name, group.args);
 		lazy=new LazyGroup(group, rArgs);
 	}
 	
 	
 	public Lazy(Constraint c) {
 		super("LazyAll");
 		lazy=new LazyAll(c);
 	}
 	
 	
 	protected boolean satisfy(Object[] rArgs, VariableMap varValues) {
 		return lazy.satisfy(rArgs, varValues);
 	}
 	
 	
 	public boolean lazy(VariableMap varValues) {
 		return lazy.lazy(varValues);
 	}
 	
 	
 	public VariableMap lazy() {
 		return lazy.lazy();
 	}
 	
 	
 	public VariableMap current() {
 		return lazy.current();
 	}
 	
 	
 	public String toString() {
 		return lazy.toString();
 	}
 	
 	
 	protected boolean isEmpty() {
 		return lazy.isEmpty();
 	}
 
 
 	@Override
 	public boolean hasNext() {
 		if (isEmpty()) end=true;
 		return !end;
 	}
 
 
 	@Override
 	public VariableMap next() {
 		currentI++;
		while (ignore.contains(currentI)) lazy();
 		VariableMap m=lazy();
 		end=m.isEmpty();
 		return m; 
 	}
 
 
 	@Override
 	public void remove() {
 		ignore.add(currentI);
 	}
 
 	
 	public void startLazy() {
 		lazy.startLazy();
 		end=false;
 	}
 
 
 	@Override
 	public Iterator<VariableMap> iterator() {
 		return this;
 	}
 	
 }	
 
 
 /* to fix:
  * lazy of LazyGroup is concurrent. 
 */
