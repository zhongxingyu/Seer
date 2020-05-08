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
 
 import java.util.Enumeration;
import java.util.Hashtable;
 
 final class Symbol {
 
 	private final static int SKspecial = 0;
 	private final static int SKordinary = 1;
 	private final static int SKundefined = 2;
 
 	private String name;
 	private Object value = null;
 	// Object value = null;
 	private int kind = SKundefined;
 	private String printName = null;
 
	private final static Hashtable<String, Symbol> symbolTable = new Hashtable<String, Symbol>();
 
 	Symbol(String s) {
 		name = s;
 	}
 
 	private final static Boolean T = Boolean.TRUE;
 	private final static Boolean F = Boolean.FALSE;
 
 	static {
 		Subr.def("Symbol", "intern", "string->symbol", 1);
 	}
 
 	public static Symbol intern(String s) {
 		Symbol sym = (Symbol) symbolTable.get(s);
 		if (sym == null) {
 			sym = new Symbol(s);
 			symbolTable.put(s, sym);
 		}
 		return sym;
 	}
 
 	static Symbol makeSpecial(String s, Subr f) {
 		Symbol sym = (Symbol) symbolTable.get(s);
 
 		if (sym == null) {
 			sym = new Symbol(s);
 			sym.kind = SKspecial;
 			sym.value = f;
 			symbolTable.put(s, sym);
 		} else if (sym.kind == SKundefined) {
 			sym.kind = SKspecial;
 			sym.value = f;
 		} else
 			IO.println("doubly defined symbol " + s);
 
 		return sym;
 	}
 
 	static Symbol makeOrdinary(String s, Object val) {
 		Symbol sym = (Symbol) symbolTable.get(s);
 
 		if (sym == null) {
 			sym = new Symbol(s);
 			sym.kind = SKordinary;
 			sym.value = val;
 			symbolTable.put(s, sym);
 		} else if (sym.kind == SKundefined) {
 			sym.kind = SKordinary;
 			sym.value = val;
 		} else
 			IO.println("doubly defined symbol " + s);
 
 		return sym;
 	}
 
 	Symbol define(Object val) {
 		if (kind == SKspecial) {
 			throw Eval.error("cannot define special form " + this);
 		} else {
 			kind = SKordinary;
 			value = val;
 		}
 		return this;
 	}
 
 	Object valueOf() {
 		if (kind == SKordinary)
 			return value;
 		else
 			throw Eval.error("undefined variable " + this);
 	}
 
 	Function functionOf() {
 		if (kind == SKordinary || kind == SKspecial)
 			return (Function) value;
 		else
 			throw Eval.error("undefined function " + this);
 	}
 
 	Object setValue(Object val) {
 		if (kind == SKordinary)
 			return value = val;
 		else
 			throw Eval.error("undefined variable " + this);
 	}
 
 	public String toString() {
 		if (printName == null)
 			return printName = (IO.needsEscape(name) ? "|" + name + "|" : name);
 		else
 			return printName;
 	}
 
 	static {
 		Subr.def("Symbol", "symbolp", "symbol?", 1);
 	}
 
 	public static Boolean symbolp(Object obj) {
 		return obj instanceof Symbol ? T : F;
 	}
 
 	static {
 		Subr.def("Symbol", "symbol2string", "symbol->string", 1);
 	}
 
 	public static String symbol2string(Symbol sym) {
 		return sym.name;
 	}
 
 	// the following functions are required to execute the Boyer benchmark.
 	private List property = List.nil;
 
 	static {
 		Subr.def("Symbol", "get", 2);
 	}
 
 	public static Object get(Symbol sym, Object key) {
 		for (List plist = sym.property; plist != List.nil; plist = (List) ((List) plist.cdr).cdr)
 			if (plist.car == key)
 				return ((List) plist.cdr).car;
 		return List.nil;
 	}
 
 	static {
 		Subr.def("Symbol", "put", 3);
 	}
 
 	public static Object put(Symbol sym, Object key, Object val) {
 		for (List plist = sym.property; plist != List.nil; plist = (List) ((List) plist.cdr).cdr)
 			if (plist.car == key)
 				return ((Pair) plist.cdr).car = val;
 		sym.property = new Pair(key, new Pair(val, sym.property));
 		return val;
 	}
 
 	private static int gensymCounter = 0;
 
 	static {
 		Subr.def("Symbol", "gensym", 0);
 	}
 
 	public static Symbol gensym() {
 		String s = "#$" + gensymCounter++;
 		Symbol sym = new Symbol(s);
 		sym.printName = s;
 		return sym;
 	}
 
 	// kokomade
 
 	private static int traceLevel = 1;
 
 	private final static Subr tracerFun = Subr.make("Symbol", "tracer", 3);
 
 	public static Object tracer(Symbol name, Function f, List args) {
 		int prevLevel = traceLevel;
 		try {
 			for (int i = 0; i < traceLevel; i++)
 				IO.print("  ");
 			IO.print(traceLevel + "> ");
 			IO.println(new Pair(name, args));
 			traceLevel++;
 			Object val = f.invoke0(args);
 			traceLevel--;
 			while (val instanceof Call)
 				val = ((Call) val).fun.invoke0(((Call) val).args);
 			for (int i = 0; i < traceLevel; i++)
 				IO.print("  ");
 			IO.print("<" + traceLevel + " ");
 			IO.println(List.list(name, val));
 			return val;
 		} finally {
 			traceLevel = prevLevel;
 		}
 	}
 
	private final static Hashtable<Symbol, Pair> traceSet = new Hashtable<Symbol, Pair>();
 	private final static Symbol Sx = intern("x");
 	private final static Symbol Squote = intern("quote");
 
 	static List trace(List syms) {
 		List traced = List.nil;
 		for (; syms != List.nil; syms = (List) syms.cdr) {
 			Symbol sym = (Symbol) syms.car;
 			if (sym.kind == SKordinary && sym.value instanceof Function) {
 				Pair x = (Pair) traceSet.get(sym);
 				if (x != null && x.car == sym.value) {
 					IO.println("function " + sym + " is already traced");
 					continue;
 				}
 				Lambda wrapper = new Lambda(sym, null, Sx, List.list(List.list(
 						tracerFun, List.list(Squote, sym), sym.value, Sx)));
 				traceSet.put(sym, new Pair(wrapper, sym.value));
 				sym.value = wrapper;
 				traced = new Pair(sym, traced);
 			} else
 				IO.println("function " + sym + " is not defined");
 		}
 		return traced;
 	}
 
 	static List untrace(List syms) {
 		List untraced = List.nil;
 		if (syms == List.nil) {
			Enumeration<Symbol> keys = traceSet.keys();
 			while (keys.hasMoreElements()) {
 				Symbol sym = (Symbol) keys.nextElement();
 				Pair x = (Pair) traceSet.get(sym);
 				if (x.car == sym.value) {
 					sym.value = x.cdr;
 					untraced = new Pair(sym, untraced);
 				}
 			}
 			traceSet.clear();
 		} else
 			for (List ss = syms; ss != List.nil; ss = (List) ss.cdr) {
 				Symbol sym = (Symbol) ss.car;
 				Pair x = (Pair) traceSet.get(sym);
 				if (x == null)
 					IO.println("function " + sym + " is not traced");
 				else {
 					if (x.car == sym.value) {
 						sym.value = x.cdr;
 						untraced = new Pair(sym, untraced);
 					} else
 						IO.println("function " + sym + " has been redefined");
 					traceSet.remove(sym);
 				}
 			}
 		return untraced;
 	}
 
 	static void init() {
 	}
 }
