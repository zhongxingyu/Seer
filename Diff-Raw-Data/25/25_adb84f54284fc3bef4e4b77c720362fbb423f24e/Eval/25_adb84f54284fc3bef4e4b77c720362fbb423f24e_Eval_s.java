 // A Lisp Driver to be embedded in Java Applications
 
 // The contents of this file are subject to the Mozilla Public License
 // Version 1.0 (the "License"); you may not use this file except in
 // compliance with the License. You may obtain a copy of the License at
 // http://www.mozilla.org/MPL/
 //
 // Software distributed under the License is distributed on an "AS IS"
 // basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 // License for the specific language governing rights and limitations
 // under the License.
 //
 // The Original Code is JAKLD code, released November 26, 2002.
 //
 // The Initial Developer of the Original Code is Taiichi Yuasa.
 // Portions created by Taiichi Yuasa are Copyright (C) 2002
 // Taiichi Yuasa. All Rights Reserved.
 //
 // Contributor(s): Taiichi Yuasa <yuasa@kuis.kyoto-u.ac.jp>
 
 //import java.io.*;
 import java.lang.reflect.Array;
 
 final class Eval {
 
 	private static boolean systemInitialized = false;
 
 	static void initializeSystem() {
 		if (!systemInitialized) {
 			Call.init();
 			Char.init();
 			Env.init();
 			IO.init();
 			Lambda.init();
 			Macro.init();
 			List.init();
 			LString.init();
 			Num.init();
 			Symbol.init();
 			Subr.init(); // This should come last.
 
 			systemInitialized = true;
 		}
 	}
 
 	public static void main(String argv[]) {
 
 		IO.println("JAKLD tail recursive (September 3, 2008)");
 		IO.println("(c) Copyright Taiichi Yuasa, 2002.  All rights reserved.");
 
 		initializeSystem();
 
 		readEvalPrintLoop();
 
 		IO.println("\nSayonara");
 	}
 
 	public static void readEvalPrintLoop() {
 		for (;;)
 			try {
 				IO.print("\n>");
 				Object expr = IO.read(null);
 				if (expr == IO.eofObject)
 					break;
 				IO.println(topLevelEval(expr));
 			} catch (Throwable e) {
 				if (e != backtraceToken) {
 					IO.println(errorMessage(e));
 					IO.println("at top-level");
 				} else
 					IO.println(" < top-level");
 			}
 	}
 
 	public static void loadProgram(String filename) {
 		try {
 			IO.load(new LString(filename), null, null);
 		} catch (Throwable e) {
 			if (e != backtraceToken) {
 				IO.println(errorMessage(e));
 				IO.println("at top-level");
 			} else
 				IO.println(" < top-level");
 		}
 	}
 
 	public static Object runProgram(String command) {
 		try {
 			return topLevelEval(IO.read(IO.openInputString(command)));
 		} catch (Throwable e) {
 			if (e != backtraceToken) {
 				IO.println(errorMessage(e));
 				IO.println("at top-level");
 			} else
 				IO.println(" < top-level");
 		}
 		return null;
 	}
 
 	private final static Symbol Sdefine = Symbol.intern("define");
 	private final static Symbol SdefineMacro = Symbol.intern("define-macro");
 	private final static Symbol Sbegin = Symbol.intern("begin");
 	private final static Symbol Strace = Symbol.intern("trace");
 	private final static Symbol Suntrace = Symbol.intern("untrace");
 
 	static Object topLevelEval(Object expr) {
 		if (expr instanceof Pair) {
 			Pair pair = (Pair) expr;
 			if (pair.car instanceof Symbol) {
 				if (pair.car == Sdefine) {
 					pair = (Pair) pair.cdr;
 					return topLevelDefine(pair.car, (List) pair.cdr);
 				} else if (pair.car == SdefineMacro) {
 					pair = (Pair) pair.cdr;
 					return topLevelDefineMacro(pair.car, (List) pair.cdr);
 				} else if (pair.car == Sbegin) {
 					Object val = List.nil;
 					for (List body = (List) pair.cdr; body != List.nil; body = (List) body.cdr)
 						val = topLevelEval(body.car);
 					return val;
 				} else if (pair.car == Strace) {
 					return Symbol.trace((List) pair.cdr);
 				} else if (pair.car == Suntrace) {
 					return Symbol.untrace((List) pair.cdr);
 				} else
 					return eval(expr, null);
 			} else
 				return eval(expr, null);
 		} else if (expr instanceof Symbol)
 			return ((Symbol) expr).valueOf();
 		else
 			return expr;
 	}
 
 	static Object eval(Object expr, Env env) {
 		return eval(expr, env, false);
 	}
 
 	static Object eval(Object expr, Env env, boolean tailp) {
 		if (expr instanceof Pair) {
 			Pair pair = (Pair) expr;
 			Function f;
 			if (pair.car instanceof Symbol) {
 				f = Env.fref((Symbol) pair.car, env);
 				if (f.isSpecialForm()) {
 					Subr.argEnv = env;
 					Subr.argTailp = tailp;
 					return f.invoke0((List) pair.cdr);
 				}
 			} else {
 				Object fval = eval(pair.car, env);
 				try {
 					f = (Function) fval;
 				} catch (ClassCastException e) {
 					if (fval instanceof Function)
 						throw e;
 					else
 						throw error(IO.printString(fval) + " is not a function");
 				}
 			}
 
 			if (f instanceof Macro) {
 				pair.car = ((Macro) f).expand();
 				return eval(pair, env, tailp);
 			}
 
 			List list = (List) pair.cdr;
 			List args = List.nil;
 			for (; list != List.nil; list = (List) list.cdr) {
 				args = new Pair(eval(list.car, env), args);
 			}
 			args = List.nreverse(args);
 			return (tailp ? new Call(f, args) : f.invoke(args));
 		} else if (expr instanceof Symbol)
 			return Env.vref((Symbol) expr, env);
 		else
 			return expr;
 	}
 
 	private final static Boolean T = Boolean.TRUE;
 	private final static Boolean F = Boolean.FALSE;
 
 	static {
 		Subr.defSpecial("Eval", "begin", "begin", 0, 0, true);
 	}
 
 	public static Object begin(List body, Env env, boolean tailp) {
 		Object val = List.nil;
 		for (; body != List.nil; body = (List) body.cdr)
 			val = eval(body.car, env, tailp && (body.cdr == List.nil));
 		return val;
 	}
 
 	static Object evalBody(List body, Env env, boolean tailp) {
 		Env newenv = env;
 		List es = body;
 		for (; es != List.nil; es = (List) es.cdr) {
 			Object elem = es.car;
 			if (!(elem instanceof Pair && ((Pair) elem).car == Sdefine))
 				break;
 			Object name = ((Pair) ((Pair) elem).cdr).car;
 			newenv = new Env(name instanceof Symbol ? (Symbol) name
 					: (Symbol) ((Pair) name).car, null, newenv);
 		}
 		Env ep = env;
 		for (List ds = body; ds != es; ds = (List) ds.cdr) {
 			List rest = (List) ((Pair) ds.car).cdr;
 			Object name = rest.car;
 			rest = (List) rest.cdr;
 			if (name instanceof Symbol) {
 				if (rest.cdr != List.nil)
 					throw new Error("too many arguments to define");
 				ep = Env.recBind(eval(rest.car, newenv), newenv, ep);
 			} else
 				ep = Env.recBind(new Lambda((Symbol) ((Pair) name).car, newenv,
 						((Pair) name).cdr, rest), newenv, ep);
 		}
 		Object val = List.nil;
 		for (; es != List.nil; es = (List) es.cdr)
 			val = eval(es.car, newenv, tailp && (es.cdr == List.nil));
 		return val;
 	}
 
 	private static Symbol topLevelDefine(Object name, List rest) {
 		if (name instanceof Symbol) {
 			if (rest.cdr != List.nil)
 				throw new Error("too many arguments to define");
 			Object x = eval(rest.car, null);
 			if (x instanceof Lambda)
 				((Lambda) x).nameIt((Symbol) name);
 			return ((Symbol) name).define(x);
 		} else {
 			Symbol s = (Symbol) ((Pair) name).car;
 			return s.define(new Lambda(s, null, ((Pair) name).cdr, rest));
 		}
 	}
 
 	private static Symbol topLevelDefineMacro(Object name, List rest) {
 		if (name instanceof Symbol) {
 			if (rest.cdr != List.nil)
 				throw new Error("too many arguments to define-macro");
 			Macro m = new Macro((Symbol) name, (Object) rest.car);
 			return ((Symbol) name).define(m);
 		} else {
 			Symbol s = (Symbol) ((Pair) name).car;
 			Lambda l = new Lambda(s, null, ((Pair) name).cdr, rest);
 			Macro m = new Macro((Symbol) s, (Object) l);
 			return s.define(m);
 		}
 	}
 
 	static {
 		Subr.defSpecial("Eval", "define", 0, 0, true);
 	}
 
 	public static void define(List args, Env env, boolean tailp) {
 		throw new Error("special form define in a bad place");
 	}
 
 	static {
 		Subr.defSpecial("Eval", "quote", 1, 0, false);
 	}
 
 	public static Object quote(Object obj, Env env, boolean tailp) {
 		return obj;
 	}
 
 	static {
 		Subr.defSpecial("Eval", "quasiquote", 1, 0, false);
 	}
 
 	public static Object quasiquote(Object x, Env env, boolean tailp) {
 		return qq(x, 0, env);
 	}
 
 	private final static Symbol Squasiquote = Symbol.intern("quasiquote");
 	private final static Symbol Sunquote = Symbol.intern("unquote");
 	private final static Symbol SunquoteSplicing = Symbol
 			.intern("unquote-splicing");
 
 	// I keep here the examples in IEEE Scheme. I never type them in again.
 	//
 	// `(list ,(+ 1 2) 4)
 	// (let ((name 'a)) `(list ,name ',name))
 	// `(a ,(+ 1 2) ,@(map abs '(4 -5 6)) b)
 	// `((foo ,(- 10 3)) ,@(cdr '(c)) . ,(car '(cons)))
 	// `#(10 5 ,(sqrt 4) ,@(map sqrt '(16 9)) 8)
 	// `,(+ 2 3)
 	// `(a `(b ,(+ 1 2) ,(foo ,(+ 1 3) d) e) f)
 	// (let ((name1 'x) (name2 'y)) `(a `(b ,,name1 ,',name2 d) e))
 	// (quasiquote (list (unquote (+ 1 2)) 4))
 	// '(quasiquote (list (unquote (+ 1 2)) 4))
 
 	private static Object qq(Object x, int level, Env env) {
 		if (x instanceof Pair) {
 			Object xcar = ((Pair) x).car;
 			Object xcdr = ((Pair) x).cdr;
 			if (xcar == Squasiquote)
 				return new Pair(Squasiquote, qq(xcdr, level + 1, env));
 			else if (xcar == Sunquote)
 				if (level > 0)
 					return new Pair(Sunquote, qq(xcdr, level - 1, env));
 				else
 					return eval(((Pair) xcdr).car, env);
 			else if (xcar instanceof Pair
 					&& ((Pair) xcar).car == SunquoteSplicing)
 				if (level > 0)
 					return new Pair(new Pair(SunquoteSplicing, qq(
 							((Pair) xcar).cdr, level - 1, env)), qq(xcdr,
 							level, env));
 				else
 					return append2(
 							(List) eval(((Pair) ((Pair) xcar).cdr).car, env),
 							qq(xcdr, level, env));
 			else
 				return new Pair(qq(xcar, level, env), qq(xcdr, level, env));
 		} else if (x instanceof Object[]) {
 			Object[] v = (Object[]) x;
 			List list = List.nil;
 			for (int i = Array.getLength(v) - 1; i >= 0; i--)
 				list = new Pair(v[i], list);
 			return List.list2vector((List) qq(list, level, env));
 		} else
 			return x;
 	}
 
 	private static Object append2(List x, Object y) {
 		if (x == List.nil)
 			return y;
 		else {
 			Pair val = new Pair(x.car, null);
 			Pair last = val;
 			while ((x = (List) x.cdr) != List.nil) {
 				Pair z = new Pair(x.car, null);
 				last.cdr = z;
 				last = z;
 			}
 			last.cdr = y;
 			return val;
 		}
 	}
 
 	static {
 		Subr.defSpecial("Eval", "Lif", "if", 2, 1, false);
 	}
 
 	public static Object Lif(Object cond, Object e1, Object e2, Env env,
 			boolean tailp) {
 		if (eval(cond, env) != F)
 			return eval(e1, env, tailp);
 		else if (e2 == null)
 			return List.nil;
 		else
 			return eval(e2, env, tailp);
 	}
 
 	private final static Symbol Selse = Symbol.intern("else");
 	private final static Symbol SEqLt = Symbol.intern("=>");
 
 	static {
 		Subr.defSpecial("Eval", "cond", 1, 0, true);
 	}
 
 	public static Object cond(List clause, List clauses, Env env, boolean tailp) {
 		for (;;) {
 			if (clause.car == Selse)
 				return begin((List) clause.cdr, env, tailp);
 			else {
 				Object c = eval(clause.car, env);
 				if (c != F) {
 					clause = (List) clause.cdr;
 					if (clause == List.nil)
 						return c;
 					else if (clause.car == SEqLt) {
 						Function f = (Function) eval(((Pair) clause.cdr).car,
 								env);
 						return (tailp ? new Call(f, List.list(c)) : f
 								.invoke1(c));
 					} else
 						return begin(clause, env, tailp);
 				}
 			}
 			if (clauses == List.nil)
 				return List.nil;
 			clause = (List) clauses.car;
 			clauses = (List) clauses.cdr;
 		}
 	}
 
 	static {
 		Subr.defSpecial("Eval", "Lcase", "case", 2, 0, true);
 	}
 
 	public static Object Lcase(Object expr, List clause, List clauses, Env env,
 			boolean tailp) {
 		Object key = eval(expr, env);
 		for (;;) {
 			if (clause.car == Selse)
 				return begin((List) clause.cdr, env, tailp);
 			else
 				for (List dl = (List) clause.car; dl != List.nil; dl = (List) dl.cdr)
 					if (eqv(dl.car, key) == T)
 						return begin((List) clause.cdr, env, tailp);
 			if (clauses == List.nil)
 				return List.nil;
 			clause = (List) clauses.car;
 			clauses = (List) clauses.cdr;
 		}
 	}
 
 	static {
 		Subr.defSpecial("Eval", "let", 1, 0, true);
 	}
 
 	public static Object let(Object first, List body, Env env, boolean tailp) {
 		List bindings;
 		if (first instanceof Symbol) {
 			Symbol name = (Symbol) first;
 			bindings = (List) body.car;
 			body = (List) body.cdr;
 			List params = List.nil;
 			for (List bs = bindings; bs != List.nil; bs = (List) bs.cdr)
 				params = new Pair(((Pair) bs.car).car, params);
 			params = List.nreverse(params);
 			env = new Env(name, null, env);
 			Env.recBind1(new Lambda(name, env, params, body), env);
 		} else
 			bindings = (List) first;
 
 		Env newenv = env;
 		for (; bindings != List.nil; bindings = (List) bindings.cdr) {
 			Pair bd = (Pair) bindings.car;
 			newenv = new Env((Symbol) bd.car, eval(((Pair) bd.cdr).car, env),
 					newenv);
 		}
 		return evalBody(body, newenv, tailp);
 	}
 
 	static {
 		Subr.defSpecial("Eval", "letA", "let*", 1, 0, true);
 	}
 
 	public static Object letA(List bindings, List body, Env env, boolean tailp) {
 		for (; bindings != List.nil; bindings = (List) bindings.cdr) {
 			Pair x = (Pair) bindings.car;
 			env = new Env((Symbol) x.car, eval(((Pair) x.cdr).car, env), env);
 		}
 		return evalBody(body, env, tailp);
 	}
 
 	static {
 		Subr.defSpecial("Eval", "letrec", 1, 0, true);
 	}
 
 	public static Object letrec(List bindings, List body, Env env, boolean tailp) {
 		Env newenv = env;
 		for (List bs = bindings; bs != List.nil; bs = (List) bs.cdr)
 			newenv = new Env((Symbol) ((Pair) bs.car).car, null, newenv);
 		Env ep = env;
 		for (List bs = bindings; bs != List.nil; bs = (List) bs.cdr)
 			ep = Env.recBind(eval(((Pair) ((Pair) bs.car).cdr).car, newenv),
 					newenv, ep);
 		return evalBody(body, newenv, tailp);
 	}
 
 	static {
 		Subr.defSpecial("Eval", "Ldo", "do", 2, 0, true);
 	}
 
 	public static Object Ldo(List bindings, List post, List body, Env env,
 			boolean tailp) {
 		Env newenv = env;
 		for (List bs = bindings; bs != List.nil; bs = (List) bs.cdr) {
 			Pair x = (Pair) bs.car;
 			newenv = new Env((Symbol) x.car, eval(((Pair) x.cdr).car, env),
 					newenv);
 		}
 		while (eval(post.car, newenv) == F) {
 			for (List es = body; es != List.nil; es = (List) es.cdr)
 				eval(es.car, newenv);
 			Env prevenv = newenv;
 			newenv = env;
 			for (List bs = bindings; bs != List.nil; bs = (List) bs.cdr) {
 				Pair x = (Pair) bs.car;
 				List y = (List) ((Pair) x.cdr).cdr;
 				newenv = new Env((Symbol) x.car, y == List.nil ? Env.vref(
 						(Symbol) x.car, prevenv) : eval(y.car, prevenv), newenv);
 			}
 		}
 		return begin((List) post.cdr, newenv, tailp);
 	}
 
 	static {
 		Subr.def("Eval", "functionp", "procedure?", 1);
 	}
 
 	public static Boolean functionp(Object obj) {
 		return obj instanceof Function ? T : F;
 	}
 
 	static {
 		Subr.def("Eval", "apply", 2, true);
 	}
 
 	public static Object apply(Function f, Object arg, List args) {
 		if (args == List.nil)
 			return new Call(f, copyList((List) arg));
 		else {
 			Pair newargs = new Pair(arg, args);
 			Pair last = newargs;
 			for (; args.cdr != List.nil; args = (List) args.cdr)
 				last = (Pair) args;
 			last.cdr = copyList((List) args.car);
 			return new Call(f, newargs);
 		}
 	}
 
 	private static List copyList(List list) {
 		if (list == List.nil)
 			return List.nil;
 		else {
 			Pair val = List.list(list.car);
 			Pair last = val;
 			while ((list = (List) list.cdr) != List.nil) {
 				Pair x = List.list(list.car);
 				last.cdr = x;
 				last = x;
 			}
 			return val;
 		}
 	}
 
 	static {
 		Subr.def("Eval", "map", 2, true);
 	}
 
 	public static List map(Function f, List list, List rest) {
 		List val = List.nil;
 		for (; list != List.nil; list = (List) list.cdr) {
 			Pair args = List.list(list.car);
 			Pair last = args;
 			for (List rp = rest; rp != List.nil; rp = (List) rp.cdr) {
 				Pair nextlist = (Pair) rp.car;
 				Pair x = List.list(nextlist.car);
 				last.cdr = x;
 				last = x;
 				rp.car = nextlist.cdr;
 			}
 			val = new Pair(f.invoke(args), val);
 		}
 		return List.nreverse(val);
 	}
 
 	static {
 		Subr.def("Eval", "forEach", "for-each", 2, true);
 	}
 
 	public static List forEach(Function f, List list, List rest) {
 		for (; list != List.nil; list = (List) list.cdr) {
 			Pair args = List.list(list.car);
 			Pair last = args;
 			for (List rp = rest; rp != List.nil; rp = (List) rp.cdr) {
 				Pair nextlist = (Pair) rp.car;
 				Pair x = List.list(nextlist.car);
 				last.cdr = x;
 				last = x;
 				rp.car = nextlist.cdr;
 			}
 			f.invoke(args);
 		}
 		return List.nil;
 	}
 
 	static {
 		Subr.defSpecial("Eval", "and", 0, 0, true);
 	}
 
 	public static Object and(List args, Env env, boolean tailp) {
 		Object val = T;
 		for (; args != List.nil; args = (List) args.cdr) {
 			val = eval(args.car, env, tailp && (args.cdr == List.nil));
 			if (val == F)
 				return F;
 		}
 		return val;
 	}
 
 	static {
 		Subr.defSpecial("Eval", "or", 0, 0, true);
 	}
 
 	public static Object or(List args, Env env, boolean tailp) {
 		for (; args != List.nil; args = (List) args.cdr) {
 			Object val = eval(args.car, env, tailp && (args.cdr == List.nil));
 			if (val != F)
 				return val;
 		}
 		return F;
 	}
 
 	static {
 		Subr.def("Eval", "not", 1);
 	}
 
 	public static Boolean not(Object obj) {
 		return obj == F ? T : F;
 	}
 
 	static {
 		Subr.def("Eval", "eq", "eq?", 2);
 	}
 
 	public static Boolean eq(Object obj1, Object obj2) {
 		return obj1 == obj2 ? T : F;
 	}
 
 	static {
 		Subr.def("Eval", "eqv", "eqv?", 2);
 	}
 
 	public static Boolean eqv(Object obj1, Object obj2) {
 		if (obj1 == obj2)
 			return T;
 		else if (obj1 instanceof Number)
 			return obj2 instanceof Number
 					&& Num.compare((Number) obj1, (Number) obj2) == 0 ? T : F;
 		else if (obj1 instanceof Character)
 			return ((Character) obj1).equals(obj2) ? T : F;
		else if (obj1 instanceof String && ((String) obj1).length() == 0)
			return obj2 instanceof String && ((String) obj2).length() == 0 ? T
					: F;
 		else
 			return F;
 	}
 
 	static {
 		Subr.def("Eval", "equal", "equal?", 2);
 	}
 
 	public static Boolean equal(Object obj1, Object obj2) {
 		for (;;)
 			if (eqv(obj1, obj2) == T)
 				return T;
 			else if (obj1 instanceof Pair)
 				if (obj2 instanceof Pair) {
 					Pair x = (Pair) obj1;
 					Pair y = (Pair) obj2;
 					if (equal(x.car, y.car) == F)
 						return F;
 					obj1 = x.cdr;
 					obj2 = y.cdr;
 				} else
 					return F;
			else if (obj1 instanceof String)
				return obj2 instanceof String
						&& ((String) obj1).equals((String) obj2) ? T : F;
 			else
 				return F;
 	}
 
 	static {
 		Subr.def("Eval", "booleanp", "boolean?", 1);
 	}
 
 	public static Boolean booleanp(Object obj) {
 		return obj instanceof Boolean ? T : F;
 	}
 
 	static {
 		Subr.defSpecial("Eval", "time", 1, 0, false);
 	}
 
 	public static Object time(Object expr, Env env, boolean tailp) {
 		long time = System.currentTimeMillis();
 		Object val = eval(expr, env);
 		time = System.currentTimeMillis() - time;
 		IO.println("time: " + time / 1000.0 + " secs");
 		return val;
 	}
 
 	static {
 		Subr.def("Eval", "gbc", 0);
 	}
 
 	public static Boolean gbc() {
 		List.clean();
 		Subr.clean();
 		System.gc();
 		return T;
 	}
 
 	static String errorMessage(Throwable e) {
 		String m = e.getMessage();
 		if (e instanceof ClassCastException && m != null) {
 			// the following code depends on JVM implementation
 			m = m.substring(m.lastIndexOf('.') + 1);
 			if (m.equals("List"))
 				m = "Nil";
 			else if (m.equals("Object"))
 				m = "Vector";
 			else if (m.equals("Contin"))
 				m = "Continuation";
 			else if (m.equals("PushbackReader"))
 				m = "InputPort";
 			else if (m.equals("StringWriter") || m.equals("OutputStreamWriter"))
 				m = "OutputPort";
 			else if (m.equals("Misc"))
 				m = "EOF";
 
 			return "RuntimeException: unexpected " + m + " object";
 		} else {
 			String s = e.getClass().getName();
 			if (m == null)
 				return s.substring(s.lastIndexOf('.') + 1);
 			else
 				return s.substring(s.lastIndexOf('.') + 1) + ": " + m;
 		}
 	}
 
 	final static RuntimeException backtraceToken = error("backtraceToken");
 
 	static RuntimeException systemError(String s) {
 		return new RuntimeException("system error(" + s + "), contact Taiichi");
 	}
 
 	static RuntimeException error(String s) {
 		return new RuntimeException(s);
 	}
 
 }
