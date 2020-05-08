 package crono;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import crono.type.*;
 import crono.type.Function.EvalType;
 
 /**
  * Define all builtins here. If the function is not variadic, don't define the
  * boolean variadic() method (defaults to false).
  * If the function fully evaluates its arguments don't define
  * EvalType eval() (defaults to EvalType.FULL)
  * The length of TypeId[] args() must be at least as large as int arity(), but
  * may be larger if the function is variadic (used for structs).
  */
 public enum CronoFunction {
     CONS(new Function(new TypeId[]{CronoType.TYPEID, CronoType.TYPEID},
 		      Cons.TYPEID, 2)
     {
 	public CronoType run(Visitor v, CronoType[] args) {
 	    return new Cons(args[0], args[1]);
 	}
 	public String toString() {
 	    return "cons";
 	}
     }),
     CAR(new Function(new TypeId[]{Cons.TYPEID}, CronoType.TYPEID, 1)
     {
 	private static final String _not_cons = "%s is not a cons cell";
 	
         public CronoType run(Visitor v, CronoType[] args) {
 	    if(!(args[0] instanceof Cons)) {
 		throw new InterpreterException(_not_cons, args[0]);
 	    }
 	    return ((Cons)args[0]).car();
 	}
 	public String toString() {
 	    return "car";
 	}
     }),
     CDR(new Function(new TypeId[]{Cons.TYPEID}, CronoType.TYPEID, 1)
     {
 	private static final String _not_cons = "%s is not a cons cell";
 	
 	public CronoType run(Visitor v, CronoType[] args) {
 	    if(!(args[0] instanceof Cons)) {
 		throw new InterpreterException(_not_cons, args[0]);
 	    }
 	    return ((Cons)args[0]).cdr();
 	}
 	public String toString() {
 	    return "cdr";
 	}
     }),
     LIST(new Function(new TypeId[]{CronoType.TYPEID}, Cons.TYPEID, 1, true)
     {
 	public CronoType run(Visitor v, CronoType[] args) {
 	    Cons c = Nil.NIL;
 	    for(int i = 0; i < args.length; ++i) {
 		c = new Cons(args[i], c);
 	    }
 	    return c;
 	}
 	public String toString() {
 	    return "list";
 	}
     }),
     GET(new Function(new TypeId[]{CronoArray.TYPEID, CronoInteger.TYPEID},
 		     CronoType.TYPEID, 2)
     {
 	public CronoType run(Visitor v, CronoType[] args) {
 	    int index = (int)((CronoInteger)args[1]).value;
 	    ((CronoArray)args[0]).get(index);
 	    return args[0];
 	}
 	public String toString() {
 	    return "get";
 	}
     }),
     PUT(new Function(new TypeId[]{CronoArray.TYPEID, CronoInteger.TYPEID,
 				  CronoType.TYPEID},
 	    CronoType.TYPEID, 3)
     {
 	public CronoType run(Visitor v, CronoType[] args) {
 	    int index = (int)((CronoInteger)args[1]).value;
 	    ((CronoArray)args[0]).put(index, args[2]);
 	    return args[0];
 	}
 	public String toString() {
 	    return "put";
 	}
     }),
     APPEND(new Function(new TypeId[]{CronoArray.TYPEID, CronoType.TYPEID},
 			CronoType.TYPEID, 2)
     {
 	public CronoType run(Visitor v, CronoType[] args) {
 	    ((CronoArray)args[0]).append(args[1]);
 	    return args[0];
 	}
 	public String toString() {
 	    return "append";
 	}
     }),
     INSERT(new Function(new TypeId[]{CronoArray.TYPEID, CronoInteger.TYPEID,
 				     CronoArray.TYPEID}, CronoArray.TYPEID, 3)
     {
 	public CronoType run(Visitor v, CronoType[] args) {
 	    CronoArray dest = (CronoArray)args[0], src = (CronoArray)args[2];
 	    return dest.insert(src, (int)((CronoInteger)args[1]).value);
 	}
 	public String toString() {
 	    return "insert";
 	}
     }),
     CONCAT(new Function(new TypeId[]{CronoArray.TYPEID, CronoArray.TYPEID},
 			CronoArray.TYPEID, 2)
     {
 	public CronoType run(Visitor v, CronoType[] args) {
 	    CronoArray dest = (CronoArray)args[0], src = (CronoArray)args[1];
 	    return dest.concat(src);
 	}
 	public String toString() {
 	    return "concat";
 	}
     }),
     DEFINE(new Function(new TypeId[]{Symbol.TYPEID, CronoType.TYPEID},
 			CronoType.TYPEID, 2, EvalType.NONE)
     {
 	private static final String _bad_type =
 	    "DEFINE: expected :symbol, got %s";
 	
 	public CronoType run(Visitor v, CronoType[] args) {
 	    if(!(args[0] instanceof Symbol)) {
 		throw new InterpreterException(_bad_type, args[0].typeId());
 	    }
 	    CronoType value = args[1].accept(v);
 	    v.getEnv().put(((Symbol)args[0]), value);
 	    return value;
 	}
 	public String toString() {
 	    return "define";
 	}
     }),
     DEFUN(new Function(new TypeId[]{Symbol.TYPEID, Cons.TYPEID,
 				    CronoType.TYPEID}, LambdaFunction.TYPEID,
 	    3, true, EvalType.NONE)
     {
 	private static final String _bad_arg =
 	    "defun: argument list must be symbols; got %s in argument list";
 	public CronoType run(Visitor v, CronoType[] args) {
 	    List<CronoType> list = ((Cons)args[1]).toList();
 	    for(CronoType item : list) {
 		if(!(item instanceof Symbol)) {
 		    throw new InterpreterException(_bad_arg,item.typeId());
 		}
 	    }
 	    
 	    Symbol[] arglist = new Symbol[list.size()];
 	    CronoType[] body;
 	    List<CronoType> blist = new LinkedList<CronoType>();
 	    for(int i = 2; i < args.length; ++i) {
 		blist.add(args[i]);
 	    }
 	    body = new CronoType[blist.size()];
 	    body = blist.toArray(body);
 	    LambdaFunction lfun =  new LambdaFunction(list.toArray(arglist),
 						      body, v.getEnv());
 	    v.getEnv().put((Symbol)args[0], lfun);
 	    lfun.environment.put((Symbol)args[0], lfun);
 	    return lfun;
 	}
 	public String toString() {
 	    return "defun";
 	}
     }),
     UNDEFINE(new Function(new TypeId[]{Symbol.TYPEID}, Nil.TYPEID, 1,
 			  EvalType.NONE)
     {
 	private static final String _bad_type =
 	    "UNDEF: expected :symbol, got %s";
 	
 	public CronoType run(Visitor v, CronoType[] args) {
 	    StringBuilder builder = new StringBuilder();
 	    boolean errors = false;
 	    for(CronoType ct : args) {
 		if(!(ct instanceof Symbol)) {
 		    builder.append(String.format(_bad_type, ct.typeId()));
 		    builder.append('\n');
 		    errors = true;
 		    continue;
 		}
 		v.getEnv().remove((Symbol)ct);
 	    }
 	    
 	    if(errors) {
 		/* Remove last newline */
 		builder.deleteCharAt(builder.length() - 1);
 		throw new InterpreterException(builder.toString());
 	    }
 	    return Nil.NIL;
 	}
 	public String toString() {
 	    return "undef";
 	}
     }),
     LAMBDA(new Function(new TypeId[]{Cons.TYPEID, CronoType.TYPEID},
 			Function.TYPEID, 2, true, EvalType.NONE)
     {
 	private static final String _bad_type =
 	    "\\: expected :cons :any, got %s, %s";
 	private static final String _bad_arg =
 	    "\\: arguments must be :symbol, got %s";
 	
 	public CronoType run(Visitor v, CronoType[] args) {
 	    if(!(args[0] instanceof Cons)) {
 		throw new InterpreterException(_bad_type,
 					       args[0].typeId(),
 					       args[1].typeId());
 	    }
 	    
 	    List<CronoType> list = ((Cons)args[0]).toList();
 	    for(CronoType item : list) {
 		if(!(item instanceof Symbol)) {
 		    throw new InterpreterException(_bad_arg,item.typeId());
 		}
 	    }
 	    
 	    Symbol[] arglist = new Symbol[list.size()];
 	    CronoType[] body;
 	    List<CronoType> blist = new LinkedList<CronoType>();
 	    for(int i = 1; i < args.length; ++i) {
 		blist.add(args[i]);
 	    }
 	    body = new CronoType[blist.size()];
 	    body = blist.toArray(body);
 	    return new LambdaFunction(list.toArray(arglist), body,
 				      v.getEnv());
 	}
 	public String toString() {
 	    return "\\";
 	}
     }),
     LET(new Function(new TypeId[]{Cons.TYPEID, CronoType.TYPEID},
 		     CronoType.TYPEID, 2, true, EvalType.NONE)
     {
 	private static final String _subst_list_type =
	    "let: substitution list must be :cons, got %s";
 	private static final String _subst_not_cons =
	    "let: expected :cons in substitution list, got %s";
 	private static final String _subst_not_sym = 
	    "let: argument names numst be :symbol, got %s";
	private static final String _subst_not_pair =
	    "let: arguments expect pairs; got %s";
 	
 	public CronoType run(Visitor v, CronoType[] args) {
 	    if(!(args[0] instanceof Cons)) {
 		throw new InterpreterException(_subst_list_type,
 					       args[0].typeId());
 	    }
 	    
 	    List<Symbol> symlist = new LinkedList<Symbol>();
 	    List<CronoType> arglist = new LinkedList<CronoType>();
 	    for(CronoType ct : ((Cons)args[0])) {
 		if(!(ct instanceof Cons)) {
 		    throw new InterpreterException(_subst_not_cons,
 						   ct.typeId());
 		}
 		
 		CronoType car = ((Cons)ct).car();
 		CronoType cdr = ((Cons)ct).cdr();
 		if(!(car instanceof Symbol)) {
 		    throw new InterpreterException(_subst_not_sym,
 						   car.typeId());
 		}
		if(cdr instanceof Cons) {
		    if(((Cons)cdr).cdr() != Nil.NIL) {
			throw new InterpreterException(_subst_not_pair,
						       cdr);
		    }
		    cdr = ((Cons)cdr).car();
		}
 		cdr = cdr.accept(v);
 		symlist.add((Symbol)car);
 		arglist.add(cdr);
 	    }
 	    
 	    Symbol[] lsyms = new Symbol[symlist.size()];
 	    lsyms = symlist.toArray(lsyms);
 	    
 	    List<CronoType> bodylist = new LinkedList<CronoType>();
 	    for(int i = 1; i < args.length; ++i) {
 		bodylist.add(args[i]);
 	    }
 	    CronoType[] body = new CronoType[bodylist.size()];
 	    body = bodylist.toArray(body);
 	    
 	    CronoType[] largs = new CronoType[arglist.size()];
 	    largs = arglist.toArray(largs);
 	    
 	    LambdaFunction lambda = new LambdaFunction(lsyms,body,v.getEnv());
 	    return lambda.run(v, largs); /*< -. - */
 	}
 	public String toString() {
 	    return "let";
 	}
     }),
     LETREC(new Function(new TypeId[]{Cons.TYPEID, CronoType.TYPEID},
 			CronoType.TYPEID, 2, true, EvalType.NONE)
     {
 	private static final String _subst_list_type =
 	    "letrec: substitution list must be :cons, got %s";
 	private static final String _subst_not_cons =
 	    "letrec: expected :cons in substitution list, got %s";
 	private static final String _subst_not_sym = 
 	    "letrec: argument names numst be :symbol, got %s";
	private static final String _subst_not_pair =
	    "let: arguments expect pairs; got %s";
 	
 	public CronoType run(Visitor v, CronoType[] args) {
 	    if(!(args[0] instanceof Cons)) {
 		throw new InterpreterException(_subst_list_type,
 					       args[0].typeId());
 	    }
 	    
 	    List<Symbol> symlist = new LinkedList<Symbol>();
 	    List<CronoType> arglist = new LinkedList<CronoType>();
 	    for(CronoType ct : ((Cons)args[0])) {
 		if(!(ct instanceof Cons)) {
 		    throw new InterpreterException(_subst_not_cons,
 						   ct.typeId());
 		}
 		
 		CronoType car = ((Cons)ct).car();
 		CronoType cdr = ((Cons)ct).cdr();
 		if(!(car instanceof Symbol)) {
 		    throw new InterpreterException(_subst_not_sym,
 						   car.typeId());
 		}
 		
		if(cdr instanceof Cons) {
		    if(((Cons)cdr).cdr() != Nil.NIL) {
			throw new InterpreterException(_subst_not_pair,
						       cdr);
		    }
		    cdr = ((Cons)cdr).car();
		}
 		cdr = cdr.accept(v);
 		symlist.add((Symbol)car);
 		arglist.add(cdr);
 		
 		if(cdr instanceof LambdaFunction) {
 		    LambdaFunction lfun = ((LambdaFunction)cdr);
 		    lfun.environment.put((Symbol)car, cdr);
 		}
 	    }
 	    
 	    Symbol[] lsyms = new Symbol[symlist.size()];
 	    lsyms = symlist.toArray(lsyms);
 	    
 	    List<CronoType> bodylist = new LinkedList<CronoType>();
 	    for(int i = 1; i < args.length; ++i) {
 		bodylist.add(args[i]);
 	    }
 	    CronoType[] body = new CronoType[bodylist.size()];
 	    body = bodylist.toArray(body);
 	    
 	    CronoType[] largs = new CronoType[arglist.size()];
 	    largs = arglist.toArray(largs);
 	    
 	    LambdaFunction lambda = new LambdaFunction(lsyms,body,v.getEnv());
 	    return lambda.run(v, largs); /*< -. - */
 	}
 	public String toString() {
 	    return "letrec";
 	}
     }),
     WHILE(new Function(new TypeId[]{CronoType.TYPEID, CronoType.TYPEID},
 		       CronoType.TYPEID, 2, EvalType.NONE)
     {
 	public CronoType run(Visitor v, CronoType[] args) {
 	    CronoType result = Nil.NIL;
 	    while((args[0].accept(v)) != Nil.NIL) {
 		result = args[1].accept(v);
 	    }
 	    return result;
 	}
 	public String toString() {
 	    return "while";
 	}
     }),
     IF(new Function(new TypeId[]{CronoType.TYPEID, CronoType.TYPEID,
 				 CronoType.TYPEID},
 	    CronoType.TYPEID, 3, EvalType.NONE)
     {
 	public CronoType run(Visitor v, CronoType[] args) {
 	    CronoType check = args[0].accept(v);
 	    if(check != Nil.NIL) {
 		return args[1].accept(v);
 	    }
 	    return args[2].accept(v);
 	}
         public String toString() {
 	    return "if";
 	}
     }),
     EQ(new Function(new TypeId[]{CronoType.TYPEID, CronoType.TYPEID},
 		    Cons.TYPEID, 2)
     {
 	public CronoType run(Visitor v, CronoType[] args) {
 	    return (args[0].equals(args[1])) ?
 		TruthValue.T : Nil.NIL;
 	}
 	public String toString() {
 	    return "=";
 	}
     }),
     LT(new Function(new TypeId[]{CronoPrimitive.TYPEID, CronoPrimitive.TYPEID},
 		    Cons.TYPEID, 2)
     {
 	private static final String _bad_type =
 	    "<: expected :primitive :primitive, got %s %s";
 	
 	private Number resolve(CronoType type) {
 	    if(type instanceof CronoInteger) {
 		return ((Long)((CronoInteger)type).value);
 	    }else if(type instanceof CronoFloat) {
 		return  ((Double)((CronoFloat)type).value);
 	    }else if(type instanceof CronoCharacter) {
 		return ((Long)((long)((CronoCharacter)type).ch));
 	    }
 	    return null;
 	}
 	public CronoType run(Visitor v, CronoType[] args) {
 	    if(!(args[0] instanceof CronoPrimitive &&
 		 args[1] instanceof CronoPrimitive)) {
 		throw new InterpreterException(_bad_type, args[0].typeId(),
 					       args[1].typeId());
 	    }
 	    
 	    Number lhs = resolve(args[0]);
 	    Number rhs = resolve(args[1]);
 	    if(lhs instanceof Double || rhs instanceof Double) {
 		return (lhs.doubleValue() < rhs.doubleValue()) ?
 		    TruthValue.T : Nil.NIL;
 	    }
 	    return (lhs.longValue() < rhs.longValue()) ?
 		TruthValue.T : Nil.NIL;
 	}
 	public String toString() {
 	    return "<";
 	}
     }),
     GT(new Function(new TypeId[]{CronoPrimitive.TYPEID, CronoPrimitive.TYPEID},
 		    Cons.TYPEID, 2)
     {
 	private static final String _bad_type =
 	    ">: expected :primitive :primitive, got %s %s";
 	
 	private Number resolve(CronoType type) {
 	    if(type instanceof CronoInteger) {
 		return ((Long)((CronoInteger)type).value);
 	    }else if(type instanceof CronoFloat) {
 		return  ((Double)((CronoFloat)type).value);
 	    }else if(type instanceof CronoCharacter) {
 		return ((Long)((long)((CronoCharacter)type).ch));
 	    }
 	    return null;
 	}
 	public CronoType run(Visitor v, CronoType[] args) {
 	    if(!(args[0] instanceof CronoPrimitive &&
 		 args[1] instanceof CronoPrimitive)) {
 		throw new InterpreterException(_bad_type, args[0].typeId(),
 					       args[1].typeId());
 	    }
 	    
 	    Number lhs = resolve(args[0]);
 	    Number rhs = resolve(args[1]);
 	    if(lhs instanceof Double || rhs instanceof Double) {
 		return (lhs.doubleValue() > rhs.doubleValue()) ?
 		    TruthValue.T : Nil.NIL;
 	    }
 	    return (lhs.longValue() > rhs.longValue()) ?
 		TruthValue.T : Nil.NIL;
 	}
 	public String toString() {
 	    return ">";
 	}
     }),
     ADD(new Function(new TypeId[]{CronoNumber.TYPEID, CronoNumber.TYPEID},
 		     CronoNumber.TYPEID, 2)
     {
 	private static final String _bad_type =
 	    "+: expected types :number :number, got %s %s";
 	
 	public CronoType run(Visitor v, CronoType[] args) {
 	    CronoNumber lhs = null, rhs = null;
 	    if(!(args[0] instanceof CronoNumber &&
 		 args[1] instanceof CronoNumber)) {
 		throw new InterpreterException(_bad_type, args[0].typeId(),
 					       args[0].typeId());
 	    }
 	    
 	    lhs = (CronoNumber)(args[0]);
 	    rhs = (CronoNumber)(args[1]);
 	    
 	    if(lhs instanceof CronoFloat) {
 		double val1, val2;
 		val1 = ((CronoFloat)lhs).value;
 		if(rhs instanceof CronoFloat) {
 		    val2 = ((CronoFloat)rhs).value;
 		}else {
 		    val2 = (double)(((CronoInteger)rhs).value);
 		}
 		return new CronoFloat(val1 + val2);
 	    }else {
 		if(rhs instanceof CronoFloat) {
 		    double val1, val2;
 		    val1 = (double)(((CronoInteger)lhs).value);
 		    val2 = ((CronoFloat)rhs).value;
 		    return new CronoFloat(val1 + val2);
 		}else {
 		    long val1, val2;
 		    val1 = ((CronoInteger)lhs).value;
 		    val2 = ((CronoInteger)rhs).value;
 		    return new CronoInteger(val1 + val2);
 		}
 	    }
 	}
 	public String toString() {
 	    return "+";
 	}
     }),
     SUB(new Function(new TypeId[]{CronoNumber.TYPEID, CronoNumber.TYPEID},
 		     CronoNumber.TYPEID, 2)
     {
 	private static final String _bad_type =
 	    "-: expected types :number :number, got %s %s";
 	
 	public CronoType run(Visitor v, CronoType[] args) {
 	    CronoNumber lhs = null, rhs = null;
 	    if(!(args[0] instanceof CronoNumber &&
 		 args[1] instanceof CronoNumber)) {
 		throw new InterpreterException(_bad_type, args[0].typeId(),
 					       args[1].typeId());
 	    }
 	    
 	    lhs = (CronoNumber)(args[0]);
 	    rhs = (CronoNumber)(args[1]);
 	    
 	    if(lhs instanceof CronoFloat) {
 		double val1, val2;
 		val1 = ((CronoFloat)lhs).value;
 		if(rhs instanceof CronoFloat) {
 		    val2 = ((CronoFloat)rhs).value;
 		}else {
 		    val2 = (double)(((CronoInteger)rhs).value);
 		}
 		return new CronoFloat(val1 - val2);
 	    }else {
 		if(rhs instanceof CronoFloat) {
 		    double val1, val2;
 		    val1 = (double)(((CronoInteger)lhs).value);
 		    val2 = ((CronoFloat)rhs).value;
 		    return new CronoFloat(val1 - val2);
 		}else {
 		    long val1, val2;
 		    val1 = ((CronoInteger)lhs).value;
 		    val2 = ((CronoInteger)rhs).value;
 		    return new CronoInteger(val1 - val2);
 		}
 	    }
 	}
 	public String toString() {
 	    return "-";
 	}
     }),
     MUL(new Function(new TypeId[]{CronoNumber.TYPEID, CronoNumber.TYPEID},
 		     CronoNumber.TYPEID, 2)
     {
 	private static final String _bad_type =
 	    "*: expected types :number :number, got %s %s";
 	
 	public CronoType run(Visitor v, CronoType[] args) {
 	    CronoNumber lhs = null, rhs = null;
 	    if(!(args[0] instanceof CronoNumber &&
 		 args[1] instanceof CronoNumber)) {
 		throw new InterpreterException(_bad_type, args[0].typeId(),
 					       args[1].typeId());
 	    }
 	    
 	    lhs = (CronoNumber)(args[0]);
 	    rhs = (CronoNumber)(args[1]);
 	    
 	    if(lhs instanceof CronoFloat) {
 		double val1, val2;
 		val1 = ((CronoFloat)lhs).value;
 		if(rhs instanceof CronoFloat) {
 		    val2 = ((CronoFloat)rhs).value;
 		}else {
 		    val2 = (double)(((CronoInteger)rhs).value);
 		}
 		return new CronoFloat(val1 * val2);
 	    }else {
 		if(rhs instanceof CronoFloat) {
 		    double val1, val2;
 		    val1 = (double)(((CronoInteger)lhs).value);
 		    val2 = ((CronoFloat)rhs).value;
 		    return new CronoFloat(val1 * val2);
 		}else {
 		    long val1, val2;
 		    val1 = ((CronoInteger)lhs).value;
 		    val2 = ((CronoInteger)rhs).value;
 		    return new CronoInteger(val1 * val2);
 		}
 	    }
 	}
 	public String toString() {
 	    return "*";
 	}
     }),
     DIV(new Function(new TypeId[]{CronoNumber.TYPEID, CronoNumber.TYPEID},
 		     CronoNumber.TYPEID, 2)
     {
 	private static final String _bad_type =
 	    "/: expected types :number :number, got %s %s";
 	
 	public CronoType run(Visitor v, CronoType[] args) {
 	    CronoNumber lhs = null, rhs = null;
 	    if(!(args[0] instanceof CronoNumber &&
 		 args[1] instanceof CronoNumber)) {
 		throw new InterpreterException(_bad_type, args[0].typeId(),
 					       args[1].typeId());
 	    }
 	    
 	    lhs = (CronoNumber)(args[0]);
 	    rhs = (CronoNumber)(args[1]);
 	    
 	    if(lhs instanceof CronoFloat) {
 		double val1, val2;
 		val1 = ((CronoFloat)lhs).value;
 		if(rhs instanceof CronoFloat) {
 		    val2 = ((CronoFloat)rhs).value;
 		}else {
 		    val2 = (double)(((CronoInteger)rhs).value);
 		}
 		return new CronoFloat(val1 / val2);
 	    }else {
 		if(rhs instanceof CronoFloat) {
 		    double val1, val2;
 		    val1 = (double)(((CronoInteger)lhs).value);
 		    val2 = ((CronoFloat)rhs).value;
 		    return new CronoFloat(val1 / val2);
 		}else {
 		    long val1, val2;
 		    val1 = ((CronoInteger)lhs).value;
 		    val2 = ((CronoInteger)rhs).value;
 		    return new CronoInteger(val1 / val2);
 		}
 	    }
 	}
 	
 	public String toString() {
 	    return "/";
 	}
     }),
     INT(new Function(new TypeId[]{CronoPrimitive.TYPEID},CronoInteger.TYPEID,1)
     {
 	public static final String _bad_type =
 	    "INT: Excepted one of :char, :float, or :int; got %s";
 	
 	public CronoType run(Visitor v, CronoType[] args) {
 	    if(args[0] instanceof CronoCharacter) {
 		return new CronoInteger(((long)((CronoCharacter)args[0]).ch));
 	    }else if(args[0] instanceof CronoFloat) {
 		return new CronoInteger(((long)((CronoFloat)args[0]).value));
 	    }else if(args[0] instanceof CronoInteger) {
 		return args[0];
 	    }
 	    throw new InterpreterException(_bad_type, args[0].typeId());
 	}
 	public String toString() {
 	    return "int";
 	}
     }),
     CHAR(new Function(new TypeId[]{CronoPrimitive.TYPEID},
 		      CronoCharacter.TYPEID, 1)
     {
 	private static final String _bad_type =
 	    "CHAR: expected one of :int, or :char; got %s";
 	
 	public CronoType run(Visitor v, CronoType[] args) {
 	    if(args[0] instanceof CronoInteger) {
 		return new CronoCharacter((char)((CronoInteger)args[0]).value);
 	    }else if(args[0] instanceof CronoFloat) {
 		return new CronoCharacter((char)((CronoFloat)args[0]).value);
 	    }else if(args[0] instanceof CronoCharacter) {
 		return args[0];
 	    }
 	    throw new InterpreterException(_bad_type, args[0].typeId());
 	}
 	public String toString() {
 	    return "char";
 	}
     }),
     FLOAT(new Function(new TypeId[]{CronoPrimitive.TYPEID},CronoFloat.TYPEID,1)
     {
 	private static final String _bad_type =
 	    "FLOAT: expected one of :int, or :float; got %s";
 	
 	public CronoType run(Visitor v, CronoType[] args) {
 	    if(args[0] instanceof CronoInteger) {
 		return new CronoFloat((double)((CronoInteger)args[0]).value);
 	    }else if(args[0] instanceof CronoCharacter) {
 		return new CronoFloat((double)((CronoCharacter)args[0]).ch);
 	    }else if(args[0] instanceof CronoFloat) {
 		return args[0];
 	    }
 	    
 	    throw new InterpreterException(_bad_type, args[0].typeId());
 	}
 	public String toString() {
 	    return "float";
 	}
     }),
     LOAD(new Function(new TypeId[]{CronoString.TYPEID}, CronoType.TYPEID, 1)
     {
 	private static final String _bad_type =
 	    "LOAD: expected :string, got %s";
 	private static final String _file_not_found =
 	    "LOAD: could not open file %s";
 	private static final String _bad_parse =
 	    "LOAD: error parsing file:\n%s";
 	
 	private CronoType loadLisp(Visitor v, String fname) {
 	    InputStream is = null;
 	    try {
 		is = new FileInputStream(fname);
 	    }catch(FileNotFoundException fnfe) {
 		throw new InterpreterException(_file_not_found,
 					       fnfe.getMessage());
 	    }
 	    Parser p = new Parser(is);
 	    CronoType program = null;
 	    try {
 		program = p.program();
 	    }catch(ParseException pe) {
 		throw new InterpreterException(_bad_parse, pe.getMessage());
 	    }
 	    if(!(program instanceof Cons)) {
 		return TruthValue.T;
 	    }
 	    for(CronoType item : (Cons)program) {
 		item.accept(v);
 	    }
 	    return TruthValue.T;
 	}
 	private CronoType loadPackage(Visitor v, String fname) {
 	    CronoPackage pack = CronoPackage.load(fname);
 	    Environment env = v.getEnv();
 	    Function[] funcs = pack.functions();
 	    if(funcs != null) {
 		for(Function f : funcs) {
 		    env.put(new Symbol(f.toString()), f);
 		}
 	    }
 	    TypeId[] types = pack.types();
 	    if(types != null) {
 		for(TypeId t : types) {
 		    env.put(t);
 		}
 	    }
 	    
 	    CronoPackage.SymbolPair[] syms = pack.symbols();
 	    if(syms != null) {
 		for(CronoPackage.SymbolPair s : syms) {
 		    env.put(s.sym, s.type);
 		}
 	    }
 	    
 	    return TruthValue.T;
 	}
 	
         public CronoType run(Visitor v, CronoType[] args) {
 	    if(!(args[0] instanceof CronoString)) {
 		throw new InterpreterException(_bad_type, args[0].typeId());
 	    }
 	    String fname = ((CronoString)args[0]).toString();
 	    String lname = fname.toLowerCase();
 	    if(lname.matches("[^\\n\\r]*\\.[^\\n\\r]*")) {
 		return loadLisp(v, fname);
 	    }
 	    return loadPackage(v, fname);
         }
         public String toString() {
 	    return "load";
         }
     }),
     PRINT(new Function(new TypeId[]{CronoType.TYPEID}, Nil.TYPEID, 1, true)
     {
         public CronoType run(Visitor v, CronoType[] args) {
 	    for(int i = 0; i < args.length; ++i) {
 		System.out.print(args[i].toString());
 	    }
 	    return Nil.NIL;
         }
         public String toString() {
 	    return "print";
         }
     }),
     PRINTLN(new Function(new TypeId[]{CronoType.TYPEID}, Nil.TYPEID, 1, true)
     {
 	public CronoType run(Visitor v, CronoType[] args) {
 	    PRINT.function.run(v, args);
 	    System.out.println();
 	    return Nil.NIL;
 	}
 	public String toString() {
 	    return "println";
 	}
     }),
     STRUCT(new Function(new TypeId[]{Symbol.TYPEID}, CronoStruct.TYPEID, 1,
 			false, EvalType.NONE)
     {
 	private static final String _name = "struct";
 	private static final String _no_struct_in_scope =
 	    "struct: No structure definition for %s in scope";
         public CronoType run(Visitor v, CronoType[] args) {
 	    CronoStruct struct = v.getEnv().getStruct((Symbol)args[0]);
 	    if(struct == null) {
 		throw new InterpreterException(_no_struct_in_scope, args[0]);
 	    }
 	    
 	    return struct.copy();
         }
         public String toString() {
 	    return _name;
         }
     }),
     SUBSTRUCT(new Function(new TypeId[]{Symbol.TYPEID, Symbol.TYPEID,
 					Cons.TYPEID},
 	                   Nil.TYPEID, 3, false, EvalType.NONE)
     {
 	private static final String _name = "substruct";
 	private static final String _no_struct_in_scope =
 	    "substruct: no structure definition for %s in scope";
 	
         public CronoType run(Visitor v, CronoType[] args) {
 	    CronoStruct par = v.getEnv().getStruct((Symbol)args[1]);
 	    if(par == null) {
 		throw new InterpreterException(_no_struct_in_scope, args[1]);
 	    }
 	    Map<String, CronoStruct.Field> fields;
 	    fields = CronoStruct.BuildFieldMap(_name, (Cons)args[2]);
 	    v.getEnv().put(new CronoStruct(args[0].toString(), fields, par));
 	    return Nil.NIL;
         }
         public String toString() {
 	    return _name;
         }
     }),
     DEFSTRUCT(new Function(new TypeId[]{Symbol.TYPEID, Cons.TYPEID},
 			   Nil.TYPEID, 2, false, EvalType.NONE)
     {
 	private static final String _name = "defstruct";
         public CronoType run(Visitor v, CronoType[] args) {
 	    Map<String, CronoStruct.Field> fields;
 	    fields = CronoStruct.BuildFieldMap(_name, (Cons)args[1]);
 	    v.getEnv().put(new CronoStruct(args[0].toString(), fields));
 	    return Nil.NIL;
         }
         public String toString() {
 	    return _name;
         }
     }),
     EXEC(new Function(new TypeId[]{CronoType.TYPEID}, CronoType.TYPEID, 1,
 		      true)
     {
 	public CronoType run(Visitor v, CronoType[] args) {
 	    return args[args.length - 1];
 	}
 	public String toString() {
 	    return "exec";
 	}
     }),
     EVAL(new Function(new TypeId[]{CronoString.TYPEID}, Cons.TYPEID, 1)
     {
         public CronoType run(Visitor v, CronoType[] args) {
 	    StringReader reader;
 	    reader = new StringReader(((CronoString)args[0]).toString());
 	    
 	    Parser p = new Parser(reader);
 	    
 	    try {
 		return p.program().accept(v);
 	    }catch(ParseException pe) {
 		throw new InterpreterException("EVAL: parse error\n%s",
 					       pe.getMessage());
 	    }
         }
         public String toString() {
 	    return "eval";
         }
     }),
     ;
     
     public final Function function;
     private CronoFunction(Function fun) {
 	this.function = fun;
     }
 }
