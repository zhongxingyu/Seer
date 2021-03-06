 package jkind.solvers.yices;
 
 import java.math.BigInteger;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import jkind.lustre.NamedType;
 import jkind.sexp.Sexp;
 import jkind.sexp.Symbol;
 import jkind.solvers.BoolValue;
 import jkind.solvers.Eval;
 import jkind.solvers.Model;
 import jkind.solvers.NumericValue;
 import jkind.solvers.Value;
 
 public class YicesModel extends Model {
 	private Map<String, String> aliases;
 	private Map<String, Value> valueAssignments;
 	private Map<String, Map<BigInteger, Value>> functionAssignments;
 
 	public YicesModel() {
 		this.aliases = new HashMap<>();
 		this.valueAssignments = new HashMap<>();
 		this.functionAssignments = new HashMap<>();
 	}
	
 	public void addAlias(String from, String to) {
 		aliases.put(from, to);
 	}
	
 	public void addValue(String id, Value v) {
 		valueAssignments.put(id, v);
 	}
	
 	public void addFunctionValue(String fn, BigInteger arg, Value v) {
 		Map<BigInteger, Value> fnMap = functionAssignments.get(fn);
 		if (fnMap == null) {
 			fnMap = new HashMap<>();
 			functionAssignments.put(fn, fnMap);
 		}
		
 		fnMap.put(arg, v);
 	}
	
 	private String getAlias(String id) {
 		String result = id;
 		while (aliases.containsKey(result)) {
 			result = aliases.get(result);
 		}
 		return result;
 	}
	
 	@Override
 	public Value getValue(Symbol sym) {
 		return valueAssignments.get(getAlias(sym.toString()));
 	}
 
 	public Map<BigInteger, Value> getFunction(String fn) {
 		return functionAssignments.get(getAlias(fn));
 	}
	
 	@Override
 	public Value getFunctionValue(String fn, BigInteger index) {
 		fn = getAlias(fn);
		if (functionAssignments.containsKey(fn)) {
 			return functionAssignments.get(fn).get(index);
 		} else if (definitions.containsKey(fn)) {
 			Sexp s = definitions.get(fn).getLambda().instantiate(new Symbol(index.toString()));
 			return new Eval(this).eval(s);
 		} else {
 			Value value = getDefaultValue(fn);
 			addFunctionValue(fn, index, value);
 			return value;
 		}
 	}
	
 	private Value getDefaultValue(String fn) {
 		if (declarations.get(fn).getType() == NamedType.BOOL) {
 			return BoolValue.TRUE;
 		} else {
 			return new NumericValue("0");
 		}
 	}
 
 	@Override
 	public Set<String> getFunctions() {
 		Set<String> fns = new HashSet<>(functionAssignments.keySet());
 		for (String alias : aliases.keySet()) {
 			if (getFunction(alias) != null) {
 				fns.add(alias);
 			}
 		}
 		return fns;
 	}
 }
