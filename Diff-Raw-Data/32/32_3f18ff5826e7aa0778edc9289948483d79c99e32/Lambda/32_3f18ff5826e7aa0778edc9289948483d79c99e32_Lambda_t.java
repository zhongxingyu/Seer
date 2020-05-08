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
 
 final class Lambda extends Function {
 
 	private Object params;
 	private List body;
 	private Env env;
 	private Symbol name;
 
 	Lambda(Symbol name, Env env, Object params, List body) {
 		this.params = params;
 		this.body = body;
 		this.env = env;
 		this.name = name;
 	}
 
 	void nameIt(Symbol sym) {
 		if (name == null)
 			name = sym;
 	}
 
 	static {
 		Subr.defSpecial("Lambda", "lambda", 1, 0, true);
 	}
 
 	public static Lambda lambda(Object params, List body, Env env, boolean tailp) {
 		return new Lambda(null, env, params, body);
 	}
 
 	private final static Symbol Slambda = Symbol.intern("lambda");
 
 	public Object invoke0(List args) {
 		try {
 			return Eval.evalBody(body, Env.lambdaBind(params, args, env), true);
 		} catch (Throwable e) {
 			if (e == Contin.escapeToken)
 				throw (RuntimeException) e;
 			else {
 				if (e != Eval.backtraceToken) {
 					IO.println(Eval.errorMessage(e));
 					IO.print("Backtrace: " + (name == null ? Slambda : name));
 				} else
 					IO.print(" < " + (name == null ? Slambda : name));
 				throw Eval.backtraceToken;
 			}
 		}
 	}
 
 	public String toString() {
 		if (name == null)
 			return "#<function>";
 		else
 			return "#<function " + name + ">";
 	}
 
 	static void init() {
 	}
 }
