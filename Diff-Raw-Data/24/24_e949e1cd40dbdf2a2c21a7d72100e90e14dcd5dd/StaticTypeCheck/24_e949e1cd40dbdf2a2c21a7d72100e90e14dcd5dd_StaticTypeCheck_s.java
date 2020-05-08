 // StaticTypeCheck.java
 
 import java.util.*;
 
 // Static type checking for Clite is defined by the functions 
 // V and the auxiliary functions typing and typeOf.  These
 // functions use the classes in the Abstract Syntax of Clite.
 
 // Consider turning arraydecls into variable decls for the purposes of typechecking.
 // This may have consequences when we get past the TypeTransformer. Hopefully
 // information will not be lost.
 
 public class StaticTypeCheck {
 
     // public static void V(Function f)
     // public static TypeMap typing(Declarations p, Declarations l)
 
     public static TypeMap typing (Declarations G, Functions F, Function f) {
     	TypeMap map = new TypeMap();
 	for (Declaration di : G) {
 		map.put (di.v, di.t);
 	}
 	for (Function fi : F) {
 		map.put (new Variable(fi.id), new FunctionMap (fi.t, typing(fi.params)));
 	}
 	for (Declaration pi : f.params) {
 		map.put (pi.v, pi.t);
 	}
 	for (Declaration li : f.locals) {
 		map.put (li.v, li.t);
 	}
 	return map;
     }
 
 
     public static TypeMap typing (Declarations G, Functions F ) {
     	TypeMap map = new TypeMap();
 	for (Declaration di : G) {
 		map.put (di.v, di.t);
 	}
 	for (Function fi : F) {
 		map.put (new Variable(fi.id), new FunctionMap (fi.t, typing(fi.params)));
 	}
 	return map;
     }
 
     public static TypeMap typing (Declarations d) {
         TypeMap map = new TypeMap();
         for (Declaration di : d) {
             map.put (di.v, di.t);
 	}
         return map;
     }
 
     public static void check(boolean test, String msg) {
         if (test)  return;
         System.err.println(msg);
         System.exit(1);
     }
 
     public static void V (Declarations d) {
         for (int i=0; i<d.size() - 1; i++)
             for (int j=i+1; j<d.size(); j++) {
                 Declaration di = d.get(i);
                 Declaration dj = d.get(j);
                 check( ! (di.v.equals(dj.v)),
                        "duplicate global: " + dj.v);
 		check( ! (di.t.equals(Type.VOID)),
 		       "global with type void: " + di.v);
 		check( ! (dj.t.equals(Type.VOID)),
 		       "global with type void: " + dj.v);
             }
     } 
 
     public static void V (Declarations G, Functions F) {
     	for (int i=0; i<G.size(); i++)
 	    for (int j=0; j<F.size(); j++) {
 	    	Declaration Gi = G.get(i);
 		Function Fj = F.get(i);
 		check( !(Gi.v.equals(new Variable(Fj.id))),
 			"duplicate name: " + Fj.id);
 		}
     }
 
     public static void V (Functions F) {
         for (int i=0; i<F.size() - 1; i++)
             for (int j=i+1; j<F.size(); j++) {
                 Function Fi = F.get(i);
                 Function Fj = F.get(j);
                 check( ! (Fi.id.equals(Fj.id)),
                        "duplicate function name: " + Fi);
             }
     } 
 
     public static void V (Program p) {
 	// Since the TypeMap is an extension of HashMap, strictly obeying the formalized type rules is erroneous. 
 	// Concrete syntax guarentees that the final function declared must be main, so that type checking has been omitted.
 	V (p.globals);
 	V (p.functions);
         V (p.globals, p.functions);
 	for(Function fi : p.functions) 
 		V(fi, typing(p.globals, p.functions, fi));
     } 
 
     public static void V (Function f, TypeMap tm) {
 	Declarations params_and_locals = new Declarations();
 	for(int i=0; i<f.locals.size(); i++) {
 		params_and_locals.add(f.locals.get(i));
 	}
 	for(int i=0; i<f.params.size(); i++) {
 		params_and_locals.add(f.params.get(i));
 	}
 	V(params_and_locals);
 	if (! f.t.equals(Type.VOID) && !f.id.equals("main")) {
 		// Find the return statements
 		boolean contains_ret = false; //sorry Sherri
 		for (Statement s : f.body.members) { // This doesn't cut it, because return statements can be nested inside other Abstract Syntax structures
 			contains_ret = s.hasReturn();
 			/*
 			if (s.getClass().equals(Return.class)) {
 				Return r = (Return) s;
 				check( typeOf(r.result, tm).equals(f.t),
 					"retun statement in function " + f.id + " with incorrect type " + typeOf(r.result, tm));
 				contains_ret = true;	
 			}
 			*/
 		}
 		if (!contains_ret)
 			check( false, "non-void function " + f.id + " does not contain return statement");	
 	} else if (f.t.equals(Type.VOID)) {
 		for (Statement s : f.body.members) {
 			check( ! s.getClass().equals(Return.class), 
 				"return statement in void function " + f.id);
 		}
	} else {
 		for (Statement s : f.body.members) {
 			check (! s.getClass().equals(Return.class),
 				"return statement in main");
 		}
 	}
 	V(f.body, tm);
     }
 
     public static Type typeOf (Expression e, TypeMap tm) {
         if (e instanceof Value) return ((Value)e).type;
         if (e instanceof Variable) {
             Variable v = (Variable)e;
             check (tm.containsKey(v), "undefined variable: " + v);
             return (Type) tm.get(v);
 	}
 	if (e instanceof ArrayRef) {
 	    ArrayRef a = (ArrayRef)e;
 	    Variable key = new Variable(a.id);
 	    check (tm.containsKey(key), "undefined arrayref: " + a);
 	    return (Type) tm.get(key);
 	}
         if (e instanceof Binary) {
             Binary b = (Binary)e;
             if (b.op.ArithmeticOp( ))
                 if (typeOf(b.term1,tm)== Type.FLOAT || typeOf(b.term2,tm)== Type.FLOAT)
                     return (Type.FLOAT);
                 else return (Type.INT);
             if (b.op.RelationalOp( ) || b.op.BooleanOp( )) 
                 return (Type.BOOL);
         }
         if (e instanceof Unary) {
             Unary u = (Unary)e;
             if (u.op.NotOp( ))        return (Type.BOOL);
             else if (u.op.NegateOp( )) return typeOf(u.term,tm);
             else if (u.op.intOp( ))    return (Type.INT);
             else if (u.op.floatOp( )) return (Type.FLOAT);
             else if (u.op.charOp( ))  return (Type.CHAR);
         } if (e instanceof CallExpression) {
 	    CallExpression c = (CallExpression) e;
 	    FunctionMap fm = (FunctionMap) tm.get(new Variable(c.name));
 	    return fm.getType();
 	}
         throw new IllegalArgumentException("should never reach here");
     } 
 
     public static void V (Expression e, TypeMap tm) {
         if (e instanceof Value) 
             return;
 	if (e instanceof ArrayRef) {
 	    ArrayRef a = (ArrayRef)e;
 	    check( tm.containsKey(new Variable(a.id))
 		   , "undeclared variable: " + a);
 	    Type typ = typeOf(a.index, tm);
 	    check ( typ == Type.INT
 		    , " non-int expression as index for " + a);
 	    return;
 	}
         if (e instanceof Variable) { 
             Variable v = (Variable)e;
             check( tm.containsKey(v)
                    , "undeclared variable: " + v);
             return;
         }
 	if (e instanceof Binary) {
             Binary b = (Binary) e;
             Type typ1 = typeOf(b.term1, tm);
             Type typ2 = typeOf(b.term2, tm);
             V (b.term1, tm);
             V (b.term2, tm);
             if (b.op.ArithmeticOp( )) {
 		if (typ1 == Type.FLOAT && typ2 == Type.INT) 
 			check( true, "should never reach here");
 		else if (typ1 == Type.INT && typ2 == Type.FLOAT)		
 			check( true, "should never reach here");
 		else
                 	check( typ1 == typ2 &&
                        		(typ1 == Type.INT || typ1 == Type.FLOAT)
                        		, "type error for " + b.op);
             } else if (b.op.RelationalOp( )) 
                 check( typ1 == typ2 , "type error for " + b.op);
             else if (b.op.BooleanOp( )) 
                 check( typ1 == Type.BOOL && typ2 == Type.BOOL,
                        b.op + ": non-bool operand");
 	    else
                 throw new IllegalArgumentException("should never reach here");
             return;
         }
         // student exercise Unary
 	
 	if (e instanceof Unary) {
 	    Unary u = (Unary) e;
 	    Type typ = typeOf(u.term, tm); 
 	    V (u.term, tm);
 	    if (u.op.NotOp( ))
 		check( typ == Type.BOOL, u.op + ": non-bool operand");
 	    else if (u.op.NegateOp( ))
 		check( typ == Type.INT || typ == Type.FLOAT, "type error for " + u.op);
 	    else if (u.op.intOp( ) || u.op.floatOp( ) || u.op.charOp( ))
 		check( typ != Type.BOOL, u.op + ": bool operand");
 	    else
 		throw new IllegalArgumentException("should never reach here");
 	    return;
 	} 
 	if (e instanceof CallExpression) {
 		CallExpression c = (CallExpression) e;
 		//Looking for typemap associated with call's name
 		Object o = tm.get(new Variable(c.name));
 		check ( o != null, "Call " + c + " references non-existent function");
 
 		FunctionMap fm = (FunctionMap) o; 
 		check( ! fm.getType().equals(Type.VOID), "call expression " + c + " to void function");
		TypeMap called_params = (TypeMap) fm.getParams();	
 
 		Iterator it = called_params.entrySet().iterator();
 		for (int i=0; i<c.args.size(); i++) {
 			check (it.hasNext(), "too many arguments provided to function " + c.name);
 			Map.Entry<VariableRef, Object> current_param= (Map.Entry<VariableRef, Object>) it.next();
 			Type param_type = (Type) current_param.getValue();
 			Expression current_arg = c.args.get(i);
 			if (param_type == Type.FLOAT) {
 				check( typeOf(current_arg, tm) == Type.FLOAT || typeOf(current_arg, tm) == Type.INT,
 					current_arg + " is not coercible to Float");
 			} else {
 				check( typeOf(current_arg, tm).equals(param_type),
 					"Argument given to function " + c.name + " is not coercible to " + param_type);
 			}
 		}
 		check( ! it.hasNext(), "not enough arguments provided to function " + c.name);
 		return;
 	}
         throw new IllegalArgumentException("should never reach here");
     }
 
     public static void V (Statement s, TypeMap tm) {
         if ( s == null )
             throw new IllegalArgumentException( "AST error: null statement");
         else if (s instanceof Skip) return;
         else if (s instanceof Assignment) {
             Assignment a = (Assignment)s;
 	    Variable target;
 	    if (a.target instanceof ArrayRef) {
 	    	target = new Variable(a.target.id);
 	    }
 	    else { 
 	        target = (Variable) a.target;
 	    }
 	    check( tm.containsKey(target)
 		   , " undefined variable target in assignment: " + a.target);
             V(a.source, tm);
             Type ttype = (Type)tm.get(target);
             Type srctype = typeOf(a.source, tm);
             if (ttype != srctype) {
                 if (ttype == Type.FLOAT)
                     check( srctype == Type.INT
                            , "mixed mode assignment to " + target);
                 else if (ttype == Type.INT)
                     check( srctype == Type.CHAR
                            , "mixed mode assignment to " + target);
                 else
                     check( false
                            , "mixed mode assignment to " + target);
             }
             return;
         } 
 	// student exercise
 	if (s instanceof Conditional) {
 	    Conditional c = (Conditional) s;
 	    V(c.test, tm);
 	    V(c.thenbranch, tm);
 	    V(c.elsebranch, tm);
 	    Type ttype = typeOf(c.test, tm);
 	    check( ttype == Type.BOOL, "test expression not of type bool: " + c.test);
 	    return;
 	}
 	if (s instanceof Loop) {
 	    Loop l = (Loop) s;
 	    V(l.test, tm);
 	    V(l.body, tm);
 	    Type ttype = typeOf(l.test, tm);
 	    check(ttype == Type.BOOL, "test expression not of type bool: " + l.test);	
 	    return;
 	}
 	if (s instanceof Block) {
 	    Block b = (Block) s;
 	    for (int i=0; i<b.members.size(); i++)
 		V(b.members.get(i), tm);
 	    return;
 	}
 	if (s instanceof CallStatement) {
 		CallStatement c = (CallStatement) s;
 		//Looking for typemap associated with call's name
 		Object o = tm.get(new Variable(c.name));
 		check ( o != null, "Call " + c + " references non-existent function");
 		
 		FunctionMap fm = (FunctionMap) o; 
 		check( fm.getType().equals(Type.VOID), "call statement " + c + "to non-void function");
 		TypeMap called_params = (TypeMap) fm.getParams();	
 
 		Iterator it = called_params.entrySet().iterator();
 		for (int i=0; i<c.args.size(); i++) {
 			check (it.hasNext(), "too many arguments provided to function " + c.name);
 			Map.Entry<VariableRef, Object> current_param= (Map.Entry<VariableRef, Object>) it.next();
 			Type param_type = (Type) current_param.getValue();
 			Expression current_arg = c.args.get(i);
 			if (param_type == Type.FLOAT) {
 				check( typeOf(current_arg, tm) == Type.FLOAT || typeOf(current_arg, tm) == Type.INT,
 					current_arg + " is not coercible to Float");
 			} else {
 				check( typeOf(current_arg, tm).equals(param_type),
 					"Argument given to function " + c.name + " is not coercible to " + param_type);
 			}
 		}
 		check( ! it.hasNext(), "not enough arguments provided to function " + c.name);
 		return;
 	}
 	if (s instanceof Return) 
 		return;
 	if (s instanceof Print) {
 	    Print p = (Print) s;
 	    V(p.to_print, tm);
 	    return;
 	}
 	throw new IllegalArgumentException("should never reach here");
     }
 
     public static void main(String args[]) {
         Parser parser  = new Parser(new Lexer(args[0]));
         Program prog = parser.program();
         prog.display();           // student exercise
         System.out.println("\nBegin type checking...");
         System.out.println("Type map:");
 	TypeMap globals = typing(prog.globals, prog.functions);
 	globals.display();
         V(prog);
     } //main
 
 } // class StaticTypeCheck
 
