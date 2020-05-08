 package nuthatch.engine.impl;
 
 import java.util.ArrayList;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import nuthatch.pattern.Environment;
 import nuthatch.pattern.EnvironmentFactory;
 import nuthatch.pattern.VarName;
 import nuthatch.strategy.Strategy;
 import nuthatch.tree.TreeCursor;
 import nuthatch.tree.errors.BranchNotFoundError;
 import nuthatch.tree.util.BranchUtil;
 
 public class EnvEngine<Value, Type, E extends EnvEngine<Value, Type, E>> extends RegisteredEngine<Value, Type, E> {
 	private Environment<Object> env = null;
 	private List<Map<String, Object>> subtreeScoping;
 	private Map<String, Object> subtreeVars;
 	private Map<String, Object> globalVars;
 
 
 	public EnvEngine(TreeCursor<Value, Type> cursor, Strategy<E> strat) {
 		super(cursor, strat);
 		clear();
 	}
 
 
 	public void clear() {
 		env = EnvironmentFactory.env();
 		globalVars = new IdentityHashMap<String, Object>();
 		subtreeVars = new IdentityHashMap<String, Object>();
 		subtreeScoping = new ArrayList<Map<String, Object>>();
 	}
 
 
 	@Override
 	public void engage() {
 		super.engage();
 	}
 
 
 	public void enterScope() {
 		env.enterScope();
 	}
 
 
 	public void exitScope() {
 		env.exitScope();
 	}
 
 
 	public <T> T getGlobalVar(VarName<T> name) {
 		return (T) globalVars.get(name.getName());
 	}
 
 
 	public <T> T getScopedVar(VarName<T> name) {
 		return (T) env.get(name.getName());
 	}
 
 
 	public <T> T getSubtreeVar(VarName<T> name) {
 		return (T) subtreeVars.get(name.getName());
 	}
 
 
 	@Override
 	public TreeCursor<Value, Type> go(int i) throws BranchNotFoundError {
 		int j = BranchUtil.normalBranch(i, getNumChildren());
 		if(j == 0) {
 			int k = subtreeScoping.size();
			Map<String, Object> map = subtreeScoping.remove(k - 1);
			if(map != null) {
				for(Entry<String, Object> entry : map.entrySet()) {
					if(entry.getValue() == null) {
						subtreeVars.remove(entry.getKey());
					}
					else {
						subtreeVars.put(entry.getKey(), entry.getValue());
 					}
 				}
 			}
 		}
 		else if(j > 0) {
 			subtreeScoping.add(null);
 		}
 		else {
 			throw new BranchNotFoundError(Integer.toString(i));
 		}
 
 		return super.go(j);
 	}
 
 
 	public <T> void setGlobalVar(VarName<T> name, T value) {
 		globalVars.put(name.getName(), value);
 	}
 
 
 	public <T> void setScopedVar(VarName<T> name, T value) {
 		env.put(name.getName(), value);
 	}
 
 
 	public <T> void setSubtreeVar(VarName<T> name, T value) {
 		Map<String, Object> map = subtreeScoping.get(subtreeScoping.size() - 1);
 		String n = name.getName();
 		if(map == null) {
 			map = new IdentityHashMap<>();
 			subtreeScoping.set(subtreeScoping.size() - 1, map);
 		}
 		if(!map.containsKey(n)) {
 			// store old value if this is a new variable at this subtree
 			map.put(n, subtreeVars.get(n));
 		}
 
 		subtreeVars.put(name.getName(), value);
 	}
 }
