 package yalisp;
 
 import static java.util.Arrays.asList;
 
 import java.util.*;
 
 public class Evaluator {
 
 	public static Object eval(Object obj, Map env) {
 		if (obj == null)
 			return null;
 		if (obj instanceof Number)
 			return obj;
 		if (obj instanceof Boolean)
 			return obj;
 		if (obj instanceof Symbol) {
 			if (!env.containsKey(obj))
 				throw new RuntimeException("Cannot find symbol " + obj + " in env " + env);
 			return env.get(obj);
 		}
 
 		// Assume s-expression
 		List sexp = (List) obj;
 
 		// Is it a special form?
		if (sexp.get(0) instanceof Symbol 
				&& asList("def", "if", "fn", "defn").contains(((Symbol) sexp.get(0)).name))
 			return evalSpecialForm(sexp, env);
		
 		// Eval args
 		List args = new ArrayList();
 		for (Object o : sexp.subList(1, sexp.size()))
 			args.add(eval(o, env));
		
		// Call function
		Fn fn = (sexp.get(0) instanceof Symbol) ? 
				(Fn) env.get((Symbol) sexp.get(0)) : 
				(Fn) eval(sexp.get(0), env);
 		return fn.invoke(args.toArray());
 	}
 
 	static Object evalSpecialForm(List sexp, final Map env) {
 		String name = ((Symbol) sexp.get(0)).name;
 		if (name.equals("def")) {
 			env.put(sexp.get(1), eval(sexp.get(2), env));
 			return sexp.get(1);
 		}
 		if (name.equals("if")) {
 			Boolean condition = (Boolean) eval(sexp.get(1), env);
 			return condition ? eval(sexp.get(2), env) : eval(sexp.get(3), env);
 		}
 		if (name.equals("fn"))
 			return makeFn((List) sexp.get(1), sexp.get(2), env);
 		if (name.equals("defn")) {
 			Symbol fnName = (Symbol) sexp.get(1);
 			Fn fn = makeFn((List) sexp.get(2), sexp.get(3), env);
 			env.put(fnName, fn);
 			return fnName;
 		}
 		throw new RuntimeException("Not a special form: " + sexp.get(0));
 	}
 
 	private static Fn makeFn(final List params, final Object body, final Map env) {
 		return new Fn() {
 			public Object invoke(Object... args) {
 				if (params.size() != args.length)
 					throw new RuntimeException("Function passed " + args.length + " expected " + params.size());
 				Map localEnv = new HashMap(env);
 				for (int i = 0; i < params.size(); i++)
 					localEnv.put(params.get(i), args[i]);
 				return eval(body, localEnv);
 			}
 		};
 	}
 
 	static interface Fn {
 		Object invoke(Object... args);
 	}
 
 	static boolean isSymbolAnyOf(Symbol method, String... strings) {
 		return Arrays.asList(strings).contains(method.name);
 	}
 
 }
