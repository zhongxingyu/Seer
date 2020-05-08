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
 
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
 import java.util.Hashtable;
 
 final class Subr extends Function {
 
 	private Symbol name;
 	private Method method;
 	private int nrequireds;
 	private int nrequiredsPLUSnoptionals;
 	private int argLength;
 	private boolean restp;
 	private boolean specialp;
 
	private static Hashtable<String, Method[]> methodTable = new Hashtable<String, Method[]>(
			16);
 
 	private static Method findMethod(String cname, String mname) {
 		Method[] methods = (Method[]) methodTable.get(cname);
 		if (methods == null)
 			try {
 				methods = Class.forName(cname).getMethods();
 				methodTable.put(cname, methods);
 			} catch (ClassNotFoundException e) {
 				IO.println("class " + cname + " not found");
 				return null;
 			}
 		Method m = null;
 		for (int i = 0; i < methods.length; i++)
 			if (methods[i].getName().equals(mname))
 				if (m == null)
 					m = methods[i];
 				else
 					IO.println("method " + cname + "." + mname + " overloaded");
 
 		if (m == null)
 			IO.println("method " + cname + "." + mname + " not found");
 		return m;
 	}
 
 	private static int MaxArgLength = 3;
 	private static Object[] argVector[];
 
 	private Subr(String cname, String mname, int nr, int no, boolean rp,
 			boolean sp) {
 		method = findMethod(cname, mname);
 		nrequireds = nr;
 		nrequiredsPLUSnoptionals = nr + no;
 		restp = rp;
 		specialp = sp;
 		argLength = nr + no + (rp ? 1 : 0) + (sp ? 2 : 0);
 		if (argLength > MaxArgLength)
 			MaxArgLength = argLength;
 	}
 
 	private static void def(String cname, String mname, String sname, int nr,
 			int no, boolean rp) {
 		Subr f = new Subr(cname, mname, nr, no, rp, false);
 		f.name = Symbol.makeOrdinary(sname, f);
 	}
 
 	static void defSpecial(String cname, String mname, String sname, int nr,
 			int no, boolean rp) {
 		Subr f = new Subr(cname, mname, nr, no, rp, true);
 		f.name = Symbol.makeSpecial(sname, f);
 	}
 
 	static Subr make(String cname, String mname, int nr) {
 		Subr f = new Subr(cname, mname, nr, 0, false, false);
 		f.name = Symbol.intern(mname);
 		return f;
 	}
 
 	static void def(String cname, String mname, int nr) {
 		def(cname, mname, mname, nr, 0, false);
 	}
 
 	static void def(String cname, String mname, int nr, int no) {
 		def(cname, mname, mname, nr, no, false);
 	}
 
 	static void def(String cname, String mname, int nr, boolean rp) {
 		def(cname, mname, mname, nr, 0, rp);
 	}
 
 	static void def(String cname, String mname, String sname, int nr) {
 		def(cname, mname, sname, nr, 0, false);
 	}
 
 	static void def(String cname, String mname, String sname, int nr, int op) {
 		def(cname, mname, sname, nr, op, false);
 	}
 
 	static void def(String cname, String mname, String sname, int nr, boolean rp) {
 		def(cname, mname, sname, nr, 0, rp);
 	}
 
 	static void defSpecial(String cname, String mname, int nr, int no,
 			boolean rp) {
 		defSpecial(cname, mname, mname, nr, no, rp);
 	}
 
 	static Env argEnv;
 	static boolean argTailp;
 
 	public Object invoke0(List args) {
 		try {
 			Object[] argV = argVector[argLength];
 			int i = 0;
 			while (i < nrequireds)
 				if (args == List.nil)
 					throw Eval.error("too few arguments to " + name);
 				else {
 					argV[i++] = args.car;
 					args = (List) args.cdr;
 				}
 			while (i < nrequiredsPLUSnoptionals)
 				if (args == List.nil)
 					argV[i++] = null;
 				else {
 					argV[i++] = args.car;
 					args = (List) args.cdr;
 				}
 			if (restp)
 				argV[i++] = args;
 			else if (args != List.nil)
 				throw Eval.error("too many arguments to " + name);
 
 			if (specialp) {
 				argV[i++] = argEnv;
 				argV[i] = (argTailp ? Boolean.TRUE : Boolean.FALSE);
 			}
 
 			try {
 				return method.invoke(null, argV);
 			} catch (InvocationTargetException e) {
 				throw e.getTargetException();
 			} catch (IllegalArgumentException e) {
 				Class[] types = method.getParameterTypes();
 				if (Array.getLength(types) != argLength)
 					throw Eval.systemError("wrong argLength");
 				for (int j = 0; j < argLength; j++) {
 					String cname = checkArg(argV[j], types[j]);
 					if (cname != null) {
 						int n = j + 1;
 						throw Eval.error((n == 1 ? "1st" : n == 2 ? "2nd"
 								: n == 3 ? "3rd" : n + "th")
 								+ " argument "
 								+ IO.printString(argV[j])
 								+ " to "
 								+ name
 								+ " not " + cname + " object");
 					}
 				}
 				IO.println(Eval.errorMessage(e));
 				throw Eval.systemError("failed error analysis");
 			}
 		} catch (Throwable e) {
 			if (e == Contin.escapeToken)
 				throw (RuntimeException) e;
 			else {
 				if (e != Eval.backtraceToken) {
 					IO.println(Eval.errorMessage(e));
 					IO.print("Backtrace: " + name);
 				} else
 					IO.print(" < " + name);
 				throw Eval.backtraceToken;
 			}
 		}
 	}
 
 	private static String checkArg(Object arg, Class t)
 			throws ClassNotFoundException {
 		if (t.isPrimitive()) {
 			if (t == Boolean.TYPE) {
 				return (arg != null && arg instanceof Boolean) ? null
 						: "Boolean";
 			} else if (t == Character.TYPE) {
 				return (arg != null && arg instanceof Character) ? null
 						: "Character";
 			} else if (t == Integer.TYPE) {
 				return (arg != null && arg instanceof Integer) ? null
 						: "Integer";
 			} else if (t == Double.TYPE) {
 				return (arg != null && arg instanceof Double) ? null : "Double";
 			} else
 				throw Eval.systemError("wrong arg type");
 		} else if (arg == null || t.isInstance(arg))
 			return null;
 		else if (t.isArray())
 			return "Vector";
 		else if (Class.forName("java.io.Reader").isAssignableFrom(t))
 			return "InputPort";
 		else if (Class.forName("java.io.Writer").isAssignableFrom(t))
 			return "OutputPort";
 		else {
 			String cname = t.getName();
 			return cname.substring(cname.lastIndexOf('.') + 1);
 		}
 	}
 
 	public boolean isSpecialForm() {
 		return specialp;
 	}
 
 	public String toString() {
 		if (specialp)
 			return "#<special form " + name + ">";
 		else
 			return "#<function " + name + ">";
 	}
 
 	static void init() {
 		argVector = new Object[MaxArgLength + 1][];
 		for (int i = 0; i <= MaxArgLength; i++)
 			argVector[i] = new Object[i];
 
 		methodTable = null;
 	}
 
 	static void clean() {
 		for (int i = 1; i <= MaxArgLength; i++)
 			for (int j = 0; j < i; j++)
 				argVector[i][j] = null;
 		argEnv = null;
 	}
 }
