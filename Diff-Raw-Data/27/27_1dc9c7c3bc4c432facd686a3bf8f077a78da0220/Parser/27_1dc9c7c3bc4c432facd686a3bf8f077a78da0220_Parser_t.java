 /*
  * This file is a part of Alchemy OS project.
  *  Copyright (C) 2011-2012, Sergey Basalaev <sbasalaev@gmail.com>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package alchemy.nec;
 
 import alchemy.core.Context;
 import alchemy.fs.File;
 import alchemy.nec.tree.*;
 import alchemy.util.IO;
 import alchemy.util.UTFReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Stack;
 import java.util.Vector;
 
 /**
  * Parses E language.
  * @author Sergey Basalaev
  */
 public class Parser {
 	
 	// Warning categories
 	private static final int W_ERROR = 1;
 	private static final int W_TYPESAFE = 2;
 	private static final int W_MAIN = 4;
 	private static final int W_CAST = 8;
 	private static final int W_VARS = 16;
 	
 	private final Context c;
 	private Tokenizer t;
 	private Unit unit;
 
 	/** Files in the process of parsing. */
 	private Stack files = new Stack();
 	/** Files that are already parsed. */
 	private Vector parsed = new Vector();
 	
 	public Parser(Context c) {
 		this.c = c;
 	}
 
 	public Unit parse(File source) {
 		unit = new Unit();
 		try {
 			// adding builtin types
 			unit.putType(BuiltinType.ANY);
 			unit.putType(BuiltinType.INT);
 			unit.putType(BuiltinType.LONG);
 			unit.putType(BuiltinType.FLOAT);
 			unit.putType(BuiltinType.DOUBLE);
 			unit.putType(BuiltinType.BOOL);
 			unit.putType(BuiltinType.STRING);
 			unit.putType(BuiltinType.ARRAY);
 			unit.putType(BuiltinType.BARRAY);
 			unit.putType(BuiltinType.CARRAY);
 			unit.putType(BuiltinType.FUNCTION);
 			unit.putType(BuiltinType.STRUCTURE);		
 			// adding builtin functions
 			parseFile(new File("/res/nec/embed.eh"));
 			// parsing
 			parseFile(source);
 		} catch (ParseException pe) {
 			warn(W_ERROR, pe.getMessage());
 			return null;
 		} catch (IOException ioe) {
 			IO.println(c.stderr, "I/O error: "+ioe);
 			return null;
 		}
 		return unit;
 	}
 
 
 	/**
 	 * Finds file referenced in 'use' directive.
 	 * Checked files are (in that order):
 	 *   ./name
 	 *   ./name.eh
 	 *   $INCPATH/name
 	 *   $INCPATH/name.eh
 	 */
 	private File resolveFile(String name) throws ParseException {
 		if (name.length() == 0) throw new ParseException("Empty string in 'use'");
 		File f = c.toFile(name);
 		if (c.fs().exists(f)) return f;
 		f = c.toFile(name+".eh");
 		if (c.fs().exists(f)) return f;
 		if (name.charAt(0) != '/') {
 			String[] incpath = IO.split(c.getEnv("INCPATH"), ':');
 			for (int i=0; i<incpath.length; i++) {
 				f = c.toFile(incpath[i]+'/'+name);
 				if (c.fs().exists(f)) return f;
 				f = c.toFile(incpath[i]+'/'+name+".eh");
 				if (c.fs().exists(f)) return f;
 			}
 		}
 		throw new ParseException("File not found: "+name);
 	}
 	
 	private void parseFile(File file) throws ParseException, IOException {
 		//do nothing if this file is already parsed
 		if (parsed.contains(file)) return;
 		//if file is in stack we have cyclic inclusion
 		if (files.contains(file)) {
 			StringBuffer sb = new StringBuffer("Cyclic inclusion");
 			for (int i=0; i<files.size(); i++) {
 				sb.append("\n from ").append(files.elementAt(i));
 			}
 			throw new ParseException(sb.toString());
 		}
 		//push file in stack
 		Tokenizer oldt = t;
 		File olddir = c.getCurDir();
 		files.push(file);
 		c.setCurDir(file.parent());
 		InputStream in = c.fs().read(file);
 		c.addStream(in);
 		UTFReader fred = new UTFReader(in);
 		t = new Tokenizer(fred);
 		//parse
 		while (t.nextToken() != Tokenizer.TT_EOF) {
 			if (t.ttype == ';') {
 				// do nothing
 			} else if (t.ttype != Tokenizer.TT_KEYWORD) {
 				throw new ParseException(t+" unexpected here.");
 			} else if (t.svalue.equals("use")) {
 				if (t.nextToken() != Tokenizer.TT_QUOTED)
 					throw new ParseException("String literal expected after 'use'");
 				File next = resolveFile(t.svalue);
 				parseFile(next);
 			} else if (t.svalue.equals("const")) {
 				if (t.nextToken() != Tokenizer.TT_IDENTIFIER)
 					throw new ParseException("Constant name expected after 'const'");
 				String name = t.svalue;
 				expect('=');
 				Expr expr = (Expr)parseExpr(unit).accept(new Optimizer(), unit);
 				if (!(expr instanceof ConstExpr))
 					throw new ParseException("Could not evaluate value of global constant");
 				Var cnst = new Var(name, expr.rettype());
 				cnst.isConst = true;
 				cnst.constValue = (ConstExpr)expr;
 				unit.addVar(cnst);
 			} else if (t.svalue.equals("type")) {
 				if (t.nextToken() != Tokenizer.TT_IDENTIFIER)
 					throw new ParseException("Type name expected after 'type'");
 				String typename = t.svalue;
 				if (typename.equals("Any"))
 					throw new ParseException("Type Any is already defined.");
 				Type prevtype = unit.getType(typename);
 				if (t.nextToken() == ';') { // forward declaration
 					if (prevtype == null) unit.putType(new NamedType(typename, null));
 				} else if (prevtype != null && prevtype.superType() != null) {
 					throw new ParseException("Type "+typename+" is already defined.");
 				}
 				t.pushBack();
 				switch (t.nextToken()) {
 					case ';':
 						break;
 					case '<': { // defining subtype
 						Type superType = parseType(unit);
 						if (superType instanceof BuiltinType && !superType.equals(BuiltinType.ANY))
 							throw new ParseException("Cannot make a subtype of builtin type.");
 						unit.putType(new NamedType(typename, superType));
 						break;
 					}
 					case '{': { // structure type
 						StructureType struct = new StructureType(typename);
 						Vector fields = new Vector();
 						while (t.nextToken() != '}') {
 							t.pushBack();
 							if (!fields.isEmpty()) expect(',');
 							if (t.nextToken() != Tokenizer.TT_IDENTIFIER)
 								throw new ParseException("Field name expected, got "+t);
 							String fieldname = t.svalue;
 							expect(':');
 							Type vartype = parseType(unit);
 							Var var = new Var(fieldname, vartype);
 							var.index = fields.size();
 							fields.addElement(var);
 						}
 						struct.fields = new Var[fields.size()];
 						for (int i=fields.size()-1; i>=0; i--) {
 							struct.fields[i] = ((Var)fields.elementAt(i));
 						}
 						unit.putType(struct);
 						break;
 					}
 					default:
 						throw new ParseException(t.toString()+" unexpected here");
 				}
 			} else if (t.svalue.equals("var")) {
 				if (t.nextToken() != Tokenizer.TT_IDENTIFIER)
 					throw new ParseException("Variable name expected after 'var'");
 				String varname = t.svalue;
 				expect(':');
 				Type vartype = parseType(unit);
 				unit.addVar(new Var(varname, vartype));
 			} else if (t.svalue.equals("def")) {
 				Func fdef = parseFuncDef();
 				Var fvar = unit.getVar(fdef.signature);
 				if (fvar == null) {
 					fvar = new Var(fdef.signature, fdef.type);
 					fvar.isConst = true;
 					fvar.constValue = new ConstExpr(fdef);
 					unit.addVar(fvar);
 				} else if (!fvar.type.equals(fdef.type)) {
 					if (fvar.type.getClass() == FunctionType.class)
 						throw new ParseException("Definition of function "+fdef.signature+" conflicts with previous definition.");
 					else
 						throw new ParseException("Variable "+fdef.signature+" is already defined");
 				}
 				Func prev = unit.getFunc(fdef.signature);
 				if (prev == null) unit.funcs.addElement(fdef);
 				switch (t.nextToken()) {
 					case ';':
 						break;
 					case '{':
 					case '=':
 						if (prev != null && prev.body != null)
 							throw new ParseException("Function "+fdef.signature+" is already defined.");
 						if (prev != null) {
 							prev.locals = fdef.locals; //actual names for impl.
 							fdef = prev;
 						}
 						Expr body;
 						if (t.ttype == '{') {
 							body = parseBlock(fdef);
 						} else {
 							body = parseExpr(fdef);
 						}
 						fdef.body = cast(body, fdef.type.rettype);
 						// if function is public then increase hits
 						if (fdef.signature.charAt(0) != '_') fdef.hits++;
 						break;
 					default:
 						throw new ParseException(t.toString()+" unexpected here");
 				}
 			}
 		}
 		//move file to parsed
 		in.close();
 		c.removeStream(in);
 		t = oldt;
 		c.setCurDir(olddir);
 		parsed.addElement(files.pop());
 	}
 	
 	/**
 	 * Parses type expression.
 	 */
 	private Type parseType(Scope scope) throws ParseException, IOException {
 		switch (t.nextToken()) {
 			case Tokenizer.TT_IDENTIFIER: { //scalar type
 				Type type = scope.getType(t.svalue);
 				if (type == null) {
 					throw new ParseException("Undefined type "+t);
 				}
 				return type;
 			}
 			case '(': { //function type
 				Vector argtypes = new Vector();
 				while (t.nextToken() != ')') {
 					t.pushBack();
 					if (!argtypes.isEmpty()) expect(',');
 					argtypes.addElement(parseType(scope));
 				}
 				Type rettype;
 				if (t.nextToken() == ':') {
 					rettype = parseType(scope);
 				} else {
 					t.pushBack();
 					rettype = BuiltinType.NONE;
 				}
 				FunctionType type = new FunctionType();
 				type.rettype = rettype;
 				type.args = new Type[argtypes.size()];
 				for (int i=argtypes.size()-1; i>=0; i--) {
 					type.args[i] = (Type)argtypes.elementAt(i);
 				}
 				return type;
 			}
 			case '[': { // array type
 				Type elementType = parseType(scope);
 				expect(']');
 				return new ArrayType(elementType);
 			}
 			default:
 				throw new ParseException(t.toString()+" unexpected here");
 		}
 	}
 
 	/**
 	 * Parses definition of function (without the body).
 	 */
 	private Func parseFuncDef() throws ParseException, IOException {
 		Func func = new Func(unit);
 		//parsing def
 		if (t.nextToken() != Tokenizer.TT_IDENTIFIER)
 			throw new ParseException("Function name expected, got "+t);
 		String str = t.svalue;
 		String fname;
 		NamedType methodholder = null;
 		if (t.nextToken() == '.') {
 			methodholder = unit.getType(str);
 			if (methodholder == null)
 				throw new ParseException("Type "+str+" is not defined");
 			if (t.nextToken() != Tokenizer.TT_IDENTIFIER)
 				throw new ParseException("Function name expected, got "+t);
 			fname = methodholder.toString()+'.'+t.svalue;
 		} else {
 			t.pushBack();
 			fname = str;
 		}
 		expect('(');
 		Vector args = new Vector();
 		if (methodholder != null) {
 			args.addElement(new Var("this", methodholder));
 		}
 		boolean first = true;
 		while (t.nextToken() != ')') {
 			t.pushBack();
 			if (first) first = false;
 			else expect(',');
 			if (t.nextToken() != Tokenizer.TT_IDENTIFIER)
 				throw new ParseException("Variable name expected, got "+t);
 			String varname = t.svalue;
 			expect(':');
 			Type vartype = parseType(func);
 			args.addElement(new Var(varname, vartype));
 		}
 		Type rettype;
 		if (t.nextToken() == ':') {
 			rettype = parseType(func);
 		} else {
 			t.pushBack();
 			rettype = BuiltinType.NONE;
 		}
 		//populating fields
 		func.locals = args;
 		FunctionType ftype = new FunctionType();
 		ftype.rettype = rettype;
 		ftype.args = new Type[args.size()];
 		for (int i=args.size()-1; i>=0; i--) {
 			ftype.args[i] = ((Var)args.elementAt(i)).type;
 		}
 		func.signature = fname;
 		func.type = ftype;
 		//some checkings
 		if (fname.equals("main")) {
 			if (args.size() != 1) {
 				warn(W_MAIN, "Incorrect number of arguments in main(), should be ([String])");
 			} else {
 				Type argtype = ((Var)args.elementAt(0)).type;
 				Type shouldbe = new ArrayType(BuiltinType.STRING);
 				if (!argtype.isSupertypeOf(shouldbe)) {
 					throw new ParseException("Incompatible argument type in main()");
 				}
 				if (!argtype.equals(shouldbe)) {
 					warn(W_MAIN, "argument of main() should be of type [String]");
 				}
 			}
 			if (!rettype.equals(BuiltinType.INT) && !rettype.equals(BuiltinType.NONE)) {
 				warn(W_MAIN, "Incompatible return type in main(), should be Int or <none>.");
 			}
 		}
 		return func;
 	}
 	
 	/**
 	 * Binary operators arranged by priority. In groups of four.
 	 */
 	private static int[] priorops = {
 			// if word operators are to appear they have the lowest priority
 			Tokenizer.TT_KEYWORD, Tokenizer.TT_IDENTIFIER, 0, 0,
 			'^', 0, 0, 0,
 			Tokenizer.TT_BARBAR, '|', 0, 0,
 			Tokenizer.TT_AMPAMP, '&', 0, 0,
 			Tokenizer.TT_LTEQ, Tokenizer.TT_GTEQ, '<', '>',
 			Tokenizer.TT_EQEQ, Tokenizer.TT_NOTEQ, 0, 0,
 			Tokenizer.TT_LTLT, Tokenizer.TT_GTGT, Tokenizer.TT_GTGTGT, 0,
 			'+', '-', 0, 0,
 			'*', '/', '%', 0
 		};
 
 	private int getPriority(Integer operator) {
 		int op = operator.intValue();
 		for (int i=0; i<priorops.length; i++) {
 			if (priorops[i] == op) return i/4;
 		}
 		return -1;
 	}
 	
 	/**
 	 * Parses sequence of expressions delimitered with operators.
 	 */
 	private Expr parseExpr(Scope scope) throws IOException, ParseException {
 		Vector exprs = new Vector();
 		Vector operators = new Vector();
 		while (true) {
 			exprs.addElement(parsePostfix(scope, parseExprNoop(scope)));
 			int opchar = t.nextToken();
 			if (opchar == ';') break;
 			if ("+-/*%^&|<>".indexOf(opchar) >= 0 || opchar <= -20) {
 				operators.addElement(new Integer(opchar));
 			} else {
 				t.pushBack();
 				break;
 			}
 		}
 		while (!operators.isEmpty()) {
 			int index = 0;
 			int priority = 0;
 			for (int i = 0; i < operators.size(); i++) {
 				int p = getPriority((Integer)operators.elementAt(i));
 				if (p > priority) {
 					priority = p;
 					index = i;
 				}
 			}
 			int op = ((Integer)operators.elementAt(index)).intValue();
 			Expr left = (Expr)exprs.elementAt(index);
 			Type ltype = left.rettype();
 			Expr right = (Expr)exprs.elementAt(index+1);
 			Type rtype = right.rettype();
 			Expr newexpr;
 			if (op == Tokenizer.TT_GTGT || op == Tokenizer.TT_LTLT || op == Tokenizer.TT_GTGTGT) {
 				if (!ltype.isSubtypeOf(BuiltinType.INT) && !ltype.isSubtypeOf(BuiltinType.LONG)
 				 || !rtype.isSubtypeOf(BuiltinType.INT))
 					throw new ParseException("Operator "+opstring(op)+" cannot be applied to "+ltype+","+rtype);
 				newexpr = new BinaryExpr(left, op, right);
 			} else if (op == '<' || op == '>' || op == Tokenizer.TT_LTEQ || op == Tokenizer.TT_GTEQ) {
 				if (!(ltype.isSubtypeOf(BuiltinType.NUMBER)) || !(rtype.isSubtypeOf(BuiltinType.NUMBER)))
 					throw new ParseException("Operator "+opstring(op)+" cannot be applied to "+ltype+","+rtype);
 				Type btype = binaryCastType(ltype, rtype);
 				newexpr = new ComparisonExpr(cast(left,btype), op, cast(right,btype));
 			} else if (op == Tokenizer.TT_EQEQ || op == Tokenizer.TT_NOTEQ) {
 				Type btype = binaryCastType(ltype, rtype);
 				newexpr = new ComparisonExpr(cast(left,btype), op, cast(right,btype));
 			} else if (op == Tokenizer.TT_AMPAMP) {
 				if (!ltype.isSubtypeOf(BuiltinType.BOOL) || !rtype.isSubtypeOf(BuiltinType.BOOL))
 					throw new ParseException("Operator "+opstring(op)+" cannot be applied to "+ltype+","+rtype);
 				newexpr = new IfExpr(left, right, new ConstExpr(Boolean.FALSE));
 			} else if (op == Tokenizer.TT_BARBAR) {
 				if (!ltype.isSubtypeOf(BuiltinType.BOOL) || !rtype.isSubtypeOf(BuiltinType.BOOL))
 					throw new ParseException("Operator "+opstring(op)+" cannot be applied to "+ltype+","+rtype);
 				newexpr = new IfExpr(left, new ConstExpr(Boolean.TRUE), right);
 			} else if ("+-/*%".indexOf(op) >= 0) {
 				if (ltype.isSubtypeOf(BuiltinType.STRING)) {
 					// string concatenation
 					right = cast(right, BuiltinType.ANY);
 					if (left instanceof ConcatExpr) {
 						((ConcatExpr)left).exprs.addElement(right);
 						newexpr = left;
 					} else {
 						ConcatExpr cexpr = new ConcatExpr();
 						cexpr.exprs.addElement(left);
 						cexpr.exprs.addElement(right);
 						newexpr = cexpr;
 					}
 				} else {
 					Type btype = binaryCastType(ltype, rtype);
 					if (!(btype.isSubtypeOf(BuiltinType.NUMBER)))
 						throw new ParseException("Operator "+opstring(op)+" cannot be applied to "+ltype+","+rtype);
 					newexpr = new BinaryExpr(cast(left,btype), op, cast(right,btype));
 				}
 			} else if ("^&|".indexOf(op) >= 0) {
 				Type btype = binaryCastType(ltype, rtype);
 				if (!btype.isSubtypeOf(BuiltinType.BOOL) && !btype.isSubtypeOf(BuiltinType.INT) && !btype.isSubtypeOf(BuiltinType.LONG))
 					throw new ParseException("Operator "+opstring(op)+" cannot be applied to "+ltype+","+rtype);
 				newexpr = new BinaryExpr(cast(left,btype), op, cast(right,btype));
 			} else {
 				//should not happen, but...
 				throw new ParseException("There is a bug in compiler."
 						+" Please report it with your source code and"
 						+"the following error message: unusual operator");
 			}
 			exprs.setElementAt(newexpr, index);
 			exprs.removeElementAt(index+1);
 			operators.removeElementAt(index);
 		}
 		return (Expr)exprs.elementAt(0);
 	}
 	
 	private Expr parsePostfix(Scope scope, Expr expr) throws ParseException, IOException {
 		Expr rtexpr = expr;
 		while (true) {
 			switch (t.nextToken()) {
 				case '(':
 					rtexpr = parseFCall(scope, rtexpr, null);
 					break;
 				case '[':
 					rtexpr = parseBrackets(scope, rtexpr);
 					break;
 				case '.':
 					rtexpr = parseDot(scope, rtexpr);
 					break;
 				default:
 					t.pushBack();
 					return rtexpr;
 			}
 		}
 	}
 	
 	/**
 	 * Parses expression without binary operators
 	 */
 	private Expr parseExprNoop(Scope scope) throws ParseException, IOException {
 		int ttype = t.nextToken();
 		switch (ttype) {
 			case ';':
 				return new NoneExpr();
 			case '+':
 			case '-': {
 				Expr sub = parsePostfix(scope, parseExprNoop(scope));
 				Type type = sub.rettype();
 				if (!type.isSubtypeOf(BuiltinType.NUMBER))
 					throw new ParseException("Operator "+(char)ttype+" cannot be applied to "+type);
 				return new UnaryExpr(ttype, sub);				
 			}
 			case '~': {
 				Expr sub = parsePostfix(scope, parseExprNoop(scope));
 				Type type = sub.rettype();
 				if (!type.isSubtypeOf(BuiltinType.INT) && !type.isSubtypeOf(BuiltinType.LONG))
 					throw new ParseException("Operator "+(char)ttype+" cannot be applied to "+type);
 				return new UnaryExpr(ttype, sub);				
 			}
 			case '!': {
 				Expr sub = parsePostfix(scope, parseExprNoop(scope));
 				Type type = sub.rettype();
 				if (!type.isSubtypeOf(BuiltinType.BOOL))
 					throw new ParseException("Operator "+(char)ttype+" cannot be applied to "+type);
 				return new UnaryExpr(ttype, sub);				
 			}
 			case '{':
 				return parseBlock(scope);
 			case '(': {
 				Expr expr = parseExpr(scope);
 				expect(')');
 				return expr;
 			}
 			case Tokenizer.TT_INT:
 				return new ConstExpr(new Integer(t.ivalue));
 			case Tokenizer.TT_LONG:
 				return new ConstExpr(new Long(t.lvalue));
 			case Tokenizer.TT_FLOAT:
 				return new ConstExpr(new Float(t.fvalue));
 			case Tokenizer.TT_DOUBLE:
 				return new ConstExpr(new Double(t.dvalue));
 			case Tokenizer.TT_QUOTED:
 				return new ConstExpr(t.svalue);
 			case Tokenizer.TT_BOOL:
 				return new ConstExpr((t.svalue.equals("true") ? Boolean.TRUE : Boolean.FALSE));
 			case Tokenizer.TT_KEYWORD:
 				return parseKeyword(scope, t.svalue);
 			case Tokenizer.TT_IDENTIFIER:
 				String str = t.svalue;
 				Var var = scope.getVar(str);
 				if (var == null) throw new ParseException("Variable "+str+" is not defined");
 				if (t.nextToken() == '=') { // setting variable value
 					if (var.isConst)
 						throw new ParseException("Cannot assign to constant "+str);
 					Expr value = cast(parseExpr(scope), var.type);
 					if (scope.isLocal(str)) {
 						return new AssignExpr(var, value);
 					} else {
 						// convert to  setstatic("var#hash", value)
 						Func setstatic = unit.getFunc("setstatic");
 						setstatic.hits++;
 						return new FCallExpr(new ConstExpr(setstatic), new Expr[] { new ConstExpr(str+'#'+var.hashCode()), value });
 					}
 				} else { // getting variable value
 					t.pushBack();
 					if (var.isConst && var.constValue != null) {
 						Object cnst = var.constValue.value;
 						if (cnst.getClass() == Func.class) {
 							((Func)cnst).hits++;
 						}
 						return var.constValue;
 					} else if (scope.isLocal(str)) {
 						return new VarExpr(var);
 					} else {
 						// convert to  cast(type)getstatic("var#hash")
 						Func getstatic = unit.getFunc("getstatic");
 						getstatic.hits++;
 						return new CastExpr(var.type, new FCallExpr(new ConstExpr(getstatic), new Expr[] { new ConstExpr(str+'#'+var.hashCode()) }));
 					}
 				}
 			default:
 				throw new ParseException(t.toString()+" unexpected here");
 		}
 	}
 	
 	private Expr parseKeyword(Scope scope, String keyword) throws IOException, ParseException {
 		if (keyword.equals("cast")) {
 			expect('(');
 			Type toType = parseType(scope);
 			expect(')');
 			Expr expr = parseExpr(scope);
 			if (toType.equals(expr.rettype())) {
 				warn(W_CAST, "Unnecessary cast to the same type");
 			} else if (toType.isSupertypeOf(expr.rettype())) {
 				warn(W_CAST, "Unnecessary cast to the supertype");
 			}
 			if (expr.rettype().isSupertypeOf(toType)) {
 				warn(W_TYPESAFE, "Unsafe type cast from "+expr.rettype()+" to "+toType);
 				return new CastExpr(toType, expr);
 			}
 			return cast(expr, toType);		
 		} else if (keyword.equals("null")) {
 			return new ConstExpr(null);
 		} else if (keyword.equals("while")) {
 			expect('(');
 			Expr cond = cast(parseExpr(scope), BuiltinType.BOOL);
 			expect(')');
 			Expr body = cast(parseExpr(scope), BuiltinType.NONE);
 			return new WhileExpr(cond, body);
 		} else if (keyword.equals("do")) {
 			Expr body = cast(parseExpr(scope), BuiltinType.NONE);
 			if (t.nextToken() != Tokenizer.TT_KEYWORD || !t.svalue.equals("while"))
 				throw new ParseException("'while' expected after 'do <expr>'");
 			expect('(');
 			Expr cond = cast(parseExpr(scope), BuiltinType.BOOL);
 			expect(')');
 			return new DoWhileExpr(cond, body);
 		} else if (keyword.equals("for")) {
 			expect('(');
 			BlockExpr forblock = new BlockExpr(scope);
 			BlockExpr forbody = new BlockExpr(forblock);
 			Expr init = cast(parseExpr(forblock), BuiltinType.NONE);
 			expect(',');
 			Expr cond = cast(parseExpr(forblock), BuiltinType.BOOL);
 			expect(',');
 			Expr incr = cast(parseExpr(forbody), BuiltinType.NONE);
 			expect(')');
 			Expr body = cast(parseExpr(forbody), BuiltinType.NONE);
 			forbody.exprs.addElement(body);
 			forbody.exprs.addElement(incr);
 			forblock.exprs.addElement(init);
 			forblock.exprs.addElement(new WhileExpr(cond, forbody));
 			return forblock;
 		} else if (keyword.equals("if")) {
 			expect('(');
 			Expr cond = cast(parseExpr(scope), BuiltinType.BOOL);
 			expect(')');
 			Expr ifexpr = parseExpr(scope);
 			Expr elseexpr;
 			if (t.nextToken() != Tokenizer.TT_KEYWORD || !t.svalue.equals("else")) {
 				t.pushBack();
 				elseexpr = new NoneExpr();
 			} else {
 				elseexpr = parseExpr(scope);
 			}
 			Type btype = binaryCastType(ifexpr.rettype(), elseexpr.rettype());
 			return new IfExpr(cond, cast(ifexpr, btype), cast(elseexpr, btype));
 		} else if (keyword.equals("var") || keyword.equals("const")) {
 			boolean isConst = keyword.equals("const");
 			if (t.nextToken() != Tokenizer.TT_IDENTIFIER)
 				throw new ParseException("Identifier expected after 'var'");
 			String varname = t.svalue;
 			Type vartype = null;
 			Expr varvalue = null;
 			// parsing type
 			if (t.nextToken() == ':') {
 				vartype = parseType(scope);
 			} else {
 				t.pushBack();
 			}
 			// parsing value
 			if (t.nextToken() == '=') {
 				varvalue = parseExpr(scope);
 				if (vartype == null) {
 					vartype = varvalue.rettype();
 					if (vartype.equals(BuiltinType.NONE))
 						throw new ParseException("Cannot convert from <none> to Any");
 				} else {
 					varvalue = cast(varvalue, vartype);
 				}
 			} else {
 				t.pushBack();
 			}
 			// defining variable
 			if (vartype == null) {
 				throw new ParseException("Type of "+varname+" is not defined");
 			}
 			Var v = new Var(varname, vartype);
 			if (isConst) {
 				v.isConst = true;
 				if (varvalue == null) {
 					throw new ParseException("Constant "+varname+" is not initialized");
 				}
 			}
 			if (scope.addVar(v)) {
 				warn(W_VARS, "Variable "+v.name+" hides another variable with the same name");
 			}
 			if (varvalue != null) {
 				return new AssignExpr(v, varvalue);
 			} else {
 				return new NoneExpr();
 			}
 		} else if (keyword.equals("new")) {
 			Type type = parseType(scope);
 			if (type instanceof StructureType) {
 				expect('(');
 				StructureType struct = (StructureType)type;
 				Expr[] init = new Expr[struct.fields.length];
 				boolean first = true;
 				while (t.nextToken() != ')') {
 					t.pushBack();
 					if (first) first = false;
 					else expect(',');
 					if (t.nextToken() != Tokenizer.TT_IDENTIFIER)
 						throw new ParseException("Identifier expected in structure constructor");
 					int index = struct.fields.length-1;
 					while (index >= 0 && !struct.fields[index].name.equals(t.svalue)) index--;
 					if (index < 0)
 						throw new ParseException("Type "+type+" has no member named "+t.svalue);
 					expect('=');
 					init[index] = cast(parseExpr(scope), struct.fields[index].type);
 				}
 				return new NewArrayByEnumExpr(type, init);
 			} else if (type.isSubtypeOf(BuiltinType.ARRAY)
 			        || type.isSubtypeOf(BuiltinType.BARRAY)
 			        || type.isSubtypeOf(BuiltinType.CARRAY)) {
 				if (t.nextToken() == '(') {
 					Expr lenexpr = cast(parseExpr(scope), BuiltinType.INT);
 					expect(')');
 					return new NewArrayExpr(type, lenexpr);
 				} else if (t.ttype == '{') {
 					Vector vinit = new Vector();
 					while (t.nextToken() != '}') {
 						t.pushBack();
 						if (!vinit.isEmpty()) expect(',');
 						Expr e = parseExpr(scope);
 						if (type.equals(BuiltinType.ARRAY)) {
 							e = cast(e, BuiltinType.ANY);
 						} else if (type instanceof ArrayType) {
 							e = cast(e, ((ArrayType)type).elementType());
 						} else {
 							e = cast(e, BuiltinType.INT);
 						}
 						vinit.addElement(e);
 					}
 					Expr[] init = new Expr[vinit.size()];
 					vinit.copyInto(init);
 					return new NewArrayByEnumExpr(type, init);
 				} else {
 					throw new ParseException("'(' or '{' expected in constructor");
 				}
 			} else {
 				throw new ParseException("Applying 'new' to neither array nor structure");
 			}
 		} else {
 			throw new ParseException(t.toString()+" unexpected here");
 		}
 	}
 	
 	/**
 	 * Parses expression part after '(' (function application).
 	 */
 	private Expr parseFCall(Scope scope, Expr fload, Expr firstarg) throws IOException, ParseException {
 		if (!(fload.rettype() instanceof FunctionType))
 			throw new ParseException("Applying () to non-function expression");
 		FunctionType ftype = (FunctionType)fload.rettype();
 		Vector vargs = new Vector();
 		if (firstarg != null) vargs.addElement(firstarg);
 		boolean first = true;
 		while (t.nextToken() != ')') {
 			t.pushBack();
 			if (first) first = false;
 			else expect(',');
 			vargs.addElement(parseExpr(scope));
 		}
 		if (ftype.args.length != vargs.size()) {
 			if (fload instanceof ConstExpr) {
 				Func f = (Func)((ConstExpr)fload).value;
 				throw new ParseException("Wrong number of arguments in call to "+f.signature+"()");
 			} else {
 				throw new ParseException("Wrong number of arguments in function call");
 			}
 		}
 		Expr[] args = new Expr[vargs.size()];
 		for (int i=0; i<args.length; i++) {
 			args[i] = cast((Expr)vargs.elementAt(i), ftype.args[i]);
 		}
 		FCallExpr fcexpr = new FCallExpr(fload, args);
 		if (fload instanceof ConstExpr) {
 			return processSpecialCasts(fcexpr);
 		} else {
 			return fcexpr;
 		}
 	}
 	
 	/**
 	 * Parses expression part after '[' (array load or store).
 	 */
 	private Expr parseBrackets(Scope scope, Expr arexpr) throws IOException, ParseException {
 		Type arrtype = arexpr.rettype();
 		if (!arrtype.isSubtypeOf(BuiltinType.ARRAY)
 		 && !arrtype.isSubtypeOf(BuiltinType.BARRAY)
 		 && !arrtype.isSubtypeOf(BuiltinType.CARRAY)) {
 			throw new ParseException("Applying [] to non-array expression");
 		}
 		Expr indexexpr = cast(parseExpr(scope), BuiltinType.INT);
 		expect(']');
 		if (t.nextToken() == '=') {
 			Expr assignexpr = parseExpr(scope);
 			if (arrtype.isSubtypeOf(BuiltinType.BARRAY) || arrtype.isSubtypeOf(BuiltinType.CARRAY)) {
 				assignexpr = cast(assignexpr, BuiltinType.INT);
 			} else if (arrtype instanceof ArrayType) {
 				assignexpr = cast(assignexpr, ((ArrayType)arrtype).elementType());
 			} else {
 				assignexpr = cast(assignexpr, BuiltinType.ANY);
 			}
 			return new AStoreExpr(arexpr, indexexpr, assignexpr);
 		} else {
 			t.pushBack();
 			if (arrtype.isSubtypeOf(BuiltinType.BARRAY) || arrtype.isSubtypeOf(BuiltinType.CARRAY)) {
 				return new ALoadExpr(arexpr, indexexpr, BuiltinType.INT);
 			} else if (arrtype instanceof ArrayType) {
 				return new ALoadExpr(arexpr, indexexpr, ((ArrayType)arrtype).elementType());
 			} else {
 				return new ALoadExpr(arexpr, indexexpr, BuiltinType.ANY);
 			}
 		}
 	}
 	
 	private Expr parseDot(Scope scope, Expr expr) throws IOException, ParseException {
 		if (t.nextToken() != Tokenizer.TT_IDENTIFIER)
 			throw new ParseException("Identifier expected after '.'");
 		String member = t.svalue;
 		Type type = expr.rettype();
 		if (type.isSubtypeOf(BuiltinType.ARRAY)
 		 || type.isSubtypeOf(BuiltinType.BARRAY)
 		 || type.isSubtypeOf(BuiltinType.CARRAY)) {
 			if (member.equals("len")) {
 				return new ALenExpr(expr);
 			}
 		} else if (type instanceof StructureType) {
 			Var[] fields = ((StructureType)type).fields;
 			int index = -1;
 			for (int i=0; i<fields.length; i++) {
 				if (fields[i].name.equals(member)) {
 					index = i;
 					break;
 				}
 			}
 			if (index >= 0) {
 				ConstExpr indexexpr = new ConstExpr(new Integer(index));
 				if (t.nextToken() == '=') {
 					Expr assignexpr = cast(parseExpr(scope), fields[index].type);
 					return new AStoreExpr(expr, indexexpr, assignexpr);
 				} else {
 					t.pushBack();
 					return new ALoadExpr(expr, indexexpr, fields[index].type);
 				}
 			}
 		}
 		// neither Array.len nor structure field
 		// trying to find method
 		Func method = null;
 		Type stype = type;
 		while (method == null && stype != null) {
 			method = unit.getFunc(stype.toString()+'.'+member);
 			stype = stype.superType();
 		}
 		if (method != null) {
 			method.hits++;
 			if (t.nextToken() == '(') {
 				return parseFCall(scope, new ConstExpr(method), expr);
 			} else {
 				t.pushBack();
 				// creating partially applied function
 				Func curry = unit.getFunc("Function.curry");
 				curry.hits++;
 				Expr[] args = new Expr[2];
 				args[0] = new ConstExpr(method);
 				args[1] = expr;
 				return processSpecialCasts(new FCallExpr(new ConstExpr(curry), args));
 			}
 		}
 		throw new ParseException("Type "+type+" has no member named "+member);
 	}
 
 	private Expr parseBlock(Scope scope) throws ParseException, IOException {
 		BlockExpr block = new BlockExpr(scope);
 		Expr lastexpr = null;
 		while (t.nextToken() != '}') {
 			t.pushBack();
 			lastexpr = parseExpr(block);
 			if (lastexpr.rettype().equals(BuiltinType.NONE))
 				block.exprs.addElement(lastexpr);
 			else
 				block.exprs.addElement(new DiscardExpr(lastexpr));
 		}
 		if (block.exprs.isEmpty()) {
 			return new NoneExpr();
 		} else {
 			//not to discard value of last expression
 			block.exprs.setElementAt(lastexpr, block.exprs.size()-1);
 			return block;
 		}
 	}
 
 	/**
 	 * Does special type checkings and casts for the following functions:
 	 *   Function.curry   - checks if argument is acceptable, computes returned type
 	 *   Structure.clone  - the returned type is the same as of argument
 	 *   acopy            - checks types of arrays
 	 */
 	private Expr processSpecialCasts(FCallExpr expr) throws ParseException {
 		Func f = (Func)((ConstExpr)expr.fload).value;
 		if (f.signature.equals("Function.curry") && expr.args[0].rettype() instanceof FunctionType) {
 			// extra special for f.curry
 			if (expr.args[0] instanceof ConstExpr
 					&& ((Func)((ConstExpr)expr.args[0]).value).signature.equals("Function.curry")
 					&& expr.args[1].rettype() instanceof FunctionType) {
 				FunctionType ftype = (FunctionType)expr.args[1].rettype();
 				if (ftype.args.length == 0)
 					throw new ParseException("Cannot curry function that takes no arguments");
 				FunctionType redftype = new FunctionType();
 				redftype.rettype = ftype.rettype;
 				redftype.args = new Type[ftype.args.length-1];
 				System.arraycopy(ftype.args, 1, redftype.args, 0, redftype.args.length);
 				FunctionType newftype = new FunctionType();
 				newftype.rettype = redftype;
 				newftype.args = new Type[1];
 				newftype.args[0] = ftype.args[0];
 				return new CastExpr(newftype, expr);
 			}
 			FunctionType oldftype = (FunctionType)expr.args[0].rettype();
 			if (oldftype.args.length == 0)
 				throw new ParseException("Cannot curry function that takes no arguments");
 			// testing whether the second argument can be accepted
 			try {
 				cast(expr.args[1], oldftype.args[0]);
 			} catch (ParseException pe) {
 				throw new ParseException("Cannot curry with given argument: "+pe.getMessage());
 			}
 			// creating new type
 			FunctionType newftype = new FunctionType();
 			newftype.rettype = oldftype.rettype;
 			newftype.args = new Type[oldftype.args.length-1];
 			System.arraycopy(oldftype.args, 1, newftype.args, 0, newftype.args.length);
 			return new CastExpr(newftype, expr);
 		} else if (f.signature.equals("Structure.clone") && expr.args[0].rettype() instanceof StructureType) {
 			return new CastExpr(expr.args[0].rettype(), expr);
 		} else if (f.signature.equals("acopy") && expr.args[2].rettype() instanceof ArrayType) {
 			ArrayType toarray = (ArrayType)expr.args[2].rettype();
 			if (expr.args[0].rettype() instanceof ArrayType) {
 				ArrayType fromarray = (ArrayType)expr.args[0].rettype();
				if (toarray.elementType().isSubtypeOf(fromarray.elementType())
					&& !toarray.elementType().equals(fromarray.elementType())) {
					warn(W_TYPESAFE, "Unsafe type cast when copying from "+fromarray+" to "+toarray);
				} else if (!toarray.elementType().isSupertypeOf(fromarray.elementType())) {
 					throw new ParseException("Cast to the incompatible type when copying from "+fromarray+" to "+toarray);
 				}
 			} else if (!toarray.elementType().equals(BuiltinType.ANY)) {
 				warn(W_TYPESAFE, "Unsafe type cast when copying from Array to "+toarray);
 			}
 		}
 		return expr;
 	}
 
 	/**
 	 * Casts expression to the specified type.
 	 */
 	private Expr cast(Expr expr, Type toType) throws ParseException {
 		Type fromType = expr.rettype();
 		if (fromType.equals(BuiltinType.NONE)) {
 			if (toType.equals(BuiltinType.NONE)) return expr;
 			else throw new ParseException("Cannot convert from <none> to "+toType);
 		}
 		if (toType.equals(BuiltinType.NONE)) {
 			return new DiscardExpr(expr);
 		}
 		if (toType.isSupertypeOf(fromType) || toType.equals(BuiltinType.ANY)) {
 			return expr;
 		}
 		if (toType instanceof FunctionType && fromType instanceof FunctionType) {
 			FunctionType fromF = (FunctionType)fromType;
 			FunctionType toF = (FunctionType)toType;
 			boolean cancast = fromF.args.length == toF.args.length;
 			cancast &= toF.rettype.isSupertypeOf(fromType);
 			for (int i=0; i<toF.args.length && cancast; i++) {
 				cancast &= toF.args[i].isSubtypeOf(fromF.args[i]);
 			}
 		}
 		if (fromType.isSubtypeOf(BuiltinType.NUMBER) && toType.isSubtypeOf(BuiltinType.NUMBER)) {
 			return new CastExpr(toType, expr);
 		}
 		throw new ParseException("Cannot convert from "+fromType+" to "+toType);
 	}
 	
 	/**
 	 * Computes return type of binary operator.
 	 */
 	private Type binaryCastType(Type ltype, Type rtype) {
 		if (ltype.equals(BuiltinType.NULL)) return rtype;
 		if (rtype.equals(BuiltinType.NULL)) return ltype;
 		if (ltype.isSubtypeOf(BuiltinType.NUMBER) && rtype.isSubtypeOf(BuiltinType.NUMBER)) {
 		int choice = 0;
 			if (ltype.isSubtypeOf(BuiltinType.DOUBLE)) choice = 4;
 			else if (ltype.isSubtypeOf(BuiltinType.FLOAT)) choice = 3;
 			else if (ltype.isSubtypeOf(BuiltinType.LONG)) choice = 2;
 			else if (ltype.isSubtypeOf(BuiltinType.INT)) choice = 1;
 			if (choice > 0) {
 				if (rtype.isSubtypeOf(BuiltinType.DOUBLE)) choice = Math.max(choice, 4);
 				else if (rtype.isSubtypeOf(BuiltinType.FLOAT)) choice = Math.max(choice, 3);
 				else if (rtype.isSubtypeOf(BuiltinType.LONG)) choice = Math.max(choice, 2);
 				else if (rtype.isSubtypeOf(BuiltinType.INT)) choice = Math.max(choice, 1);
 			}
 			switch (choice) {
 				case 4: return BuiltinType.DOUBLE;
 				case 3: return BuiltinType.FLOAT;
 				case 2: return BuiltinType.LONG;
 				case 1: return BuiltinType.INT;
 			}
 		}
 		return Type.commonSupertype(ltype, rtype);
 	}
 
 	/** Reads next token and if it is not the given character, throws exception. */
 	private void expect(char ttype) throws ParseException, IOException {
 		if (t.nextToken() != ttype) {
 			throw new ParseException("Expected '"+String.valueOf(ttype)+"', got "+t);
 		}
 	}
 
 	/** Returns operator string by ttype. */
 	private String opstring(int ttype) {
 		if (ttype > 0) return String.valueOf((char)ttype);
 		switch (ttype) {
 			case Tokenizer.TT_EQEQ: return "==";
 			case Tokenizer.TT_GTEQ: return ">=";
 			case Tokenizer.TT_GTGT: return ">>";
 			case Tokenizer.TT_GTGTGT: return ">>>";
 			case Tokenizer.TT_LTEQ: return "<=";
 			case Tokenizer.TT_LTLT: return "<<";
 			case Tokenizer.TT_NOTEQ: return "!=";
 			case Tokenizer.TT_AMPAMP: return "&&";
 			case Tokenizer.TT_BARBAR: return "||";
 			default: return null;
 		}
 	}
 
 	/** Prints warning on stderr. */
 	private void warn(int category, String msg) {
 		String wmsg = null;
 		switch (category) {
 			case W_ERROR: wmsg = "[Error]"; break;
 			case W_TYPESAFE: wmsg = "[Warning typesafe]"; break;
 			case W_CAST: wmsg = "[Warning cast]"; break;
 			case W_MAIN: wmsg = "[Warning main]"; break;
 			case W_VARS: wmsg = "[Warning vars]"; break;
 		}
 		IO.println(c.stderr, files.peek().toString() + ':' + t.lineNumber()
 				+ ": "+wmsg+"\n "+msg);
 	}
 }
