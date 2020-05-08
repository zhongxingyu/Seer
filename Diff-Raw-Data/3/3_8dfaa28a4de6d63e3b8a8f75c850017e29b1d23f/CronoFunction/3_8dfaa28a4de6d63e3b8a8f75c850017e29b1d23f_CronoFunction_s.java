 package crono;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.LinkedList;
 import java.util.List;
 
 import crono.type.Cons;
 import crono.type.CronoCharacter;
 import crono.type.CronoFloat;
 import crono.type.CronoInteger;
 import crono.type.CronoNumber;
 import crono.type.CronoString;
 import crono.type.CronoType;
 import crono.type.Function;
 import crono.type.Function.EvalType;
 import crono.type.Nil;
 import crono.type.LambdaFunction;
 import crono.type.Symbol;
 import crono.type.TruthValue;
 
 public enum CronoFunction {
     CONS(new Function() {
 	public int arity() {
 	    return 2;
 	}
 	public CronoType run(Visitor v, CronoType[] args) {
 	    return new Cons(args[0], args[1]);
 	}
 	public String toString() {
 	    return "cons";
 	}
     }),
     CAR(new Function() {
 	public static final String _not_cons = "%s is not a cons cell";
 	public int arity() {
 	    return 1;
 	}
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
     CDR(new Function() {
 	public static final String _not_cons = "%s is not a cons cell";
 	public int arity() {
 	    return 1;
 	}
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
     DEFINE(new Function() {
 	public static final String _bad_type =
 	    "DEFINE: expected :string, got %s";
 	public int arity() {
 	    return 2;
 	}
 	public EvalType eval() {
 	    return EvalType.NONE;
 	}
 	public CronoType run(Visitor v, CronoType[]args) {
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
     LAMBDA(new Function() {
 	    public static final String _bad_type =
 		"\\: expected :cons :any, got %s, %s";
 	    public static final String _bad_arg =
 		"\\: arguments must be :symbol, got %s";
 	    
 	    public int arity() {
 		return 2;
 	    }
 	    public EvalType eval() {
 		return EvalType.NONE;
 	    }
 	    public CronoType run(Visitor v, CronoType[] args) {
 		if(!(args[0] instanceof Cons)) {
 		    throw new InterpreterException(_bad_type,
						   args[0].typeId());
 		}
 		
 		List<CronoType> list = ((Cons)args[0]).toList();
 		for(CronoType item : list) {
 		    if(!(item instanceof Symbol)) {
 			throw new InterpreterException(_bad_arg,item.typeId());
 		    }
 		}
 		
 		Symbol[] arglist = new Symbol[list.size()];
 		return new LambdaFunction(list.toArray(arglist), args[1],
 					  v.getEnv());
 	    }
 	    public String toString() {
 		return "\\";
 	    }
     }),
     LET(new Function() {
 	public static final String _subst_list_type =
 	    "LET: substitution list must be :cons, got %s";
 	public static final String _subst_not_cons =
 	    "LET: expected :cons in substitution list, got %s";
 	public static final String _subst_not_sym = 
 	    "LET: argument names numst be :symbol, got %s";
 	public int arity() {
 	    return 2;
 	}
 	public boolean variadic() {
 	    return true;
 	}
 	public EvalType eval() {
 	    return EvalType.NONE;
 	}
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
 	    CronoType body = Cons.fromList(bodylist);
 	    CronoType[] largs = new CronoType[arglist.size()];
 	    largs = arglist.toArray(largs);
 	    
 	    LambdaFunction lambda = new LambdaFunction(lsyms,body,v.getEnv());
 	    return lambda.run(v, largs); /*< -. - */
 	}
 	public String toString() {
 	    return "let";
 	}
     }),
     IF(new Function() {
 	    public int arity() {
 		return 3;
 	    }
 	    public EvalType eval() {
 		return EvalType.NONE;
 	    }
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
     EQ(new Function() {
 	    public int arity() {
 		return 2;
 	    }
 	    public CronoType run(Visitor v, CronoType[] args) {
 		return (args[0].equals(args[1])) ?
 		    TruthValue.T : Nil.NIL;
 	    }
 	    public String toString() {
 		return "=";
 	    }
     }),
     ADD(new Function() {
 	public static final String _bad_type =
 	    "+: expected types :number :number, got %s %s";
 	public int arity() {
 	    return 2;
 	}
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
     SUB(new Function() {
 	public static final String _bad_type =
 	    "-: expected types :number :number, got %s %s";
 
 	public int arity() {
 	    return 2;
 	}
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
     MUL(new Function() {
 	public static final String _bad_type =
 	    "*: expected types :number :number, got %s %s";
 
 	public int arity() {
 	    return 2;
 	}
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
     DIV(new Function() {
 	public static final String _bad_type =
 	    "/: expected types :number :number, got %s %s";
 
 	public int arity() {
 	    return 2;
 	}
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
     INT(new Function() {
 	public static final String _bad_type =
 	    "INT: Excepted one of :char, :float, or :int; got %s";
         public int arity() {
 	    return 1;
 	}
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
     CHAR(new Function() {
 	public static final String _bad_type =
 	    "CHAR: expected one of :int, or :char; got %s";
 	public int arity() {
 	    return 1;
 	}
 	public CronoType run(Visitor v, CronoType[] args) {
 	    if(args[0] instanceof CronoInteger) {
 		return new CronoCharacter((char)((CronoInteger)args[0]).value);
 	    }else if(args[0] instanceof CronoCharacter) {
 		return args[0];
 	    }
 	    throw new InterpreterException(_bad_type, args[0].typeId());
 	}
 	public String toString() {
 	    return "char";
 	}
     }),
     FLOAT(new Function() {
 	public static final String _bad_type =
 	    "FLOAT: expected one of :int, or :float; got %s";
 	public int arity() {
 	    return 1;
 	}
 	public CronoType run(Visitor v, CronoType[] args) {
 	    if(args[0] instanceof CronoInteger) {
 		return new CronoFloat((double)((CronoInteger)args[0]).value);
 	    }else if(args[0] instanceof CronoFloat) {
 		return args[0];
 	    }
 	    
 	    throw new InterpreterException(_bad_type, args[0].typeId());
 	}
 	public String toString() {
 	    return "float";
 	}
     }),
     LOAD(new Function() {
 	public static final String _bad_type =
 	    "LOAD: expected :string, got %s";
 	public static final String _file_not_found =
 	    "LOAD: could not open file %s";
 	public static final String _bad_parse =
 	    "LOAD: error parsing file:\n%s";
         public int arity() {
 	    return 1;
         }
         public CronoType run(Visitor v, CronoType[] args) {
 	    if(!(args[0] instanceof CronoString)) {
 		throw new InterpreterException(_bad_type, args[0].typeId());
 	    }
 	    InputStream is;
 	    try {
 		is = new FileInputStream(((CronoString)args[0]).image());
 	    }catch(FileNotFoundException fnfe) {
 		throw new InterpreterException(_file_not_found,
 					       fnfe.getMessage());
 	    }
 	    
 	    Parser p = new Parser(is);
 	    CronoType program;
 	    try {
 		program = p.program(); /*< Fetch entire program */
 	    }catch(ParseException pe) {
 		throw new InterpreterException(_bad_parse, pe.getMessage());
 	    }
 	    
 	    System.err.printf("LOAD: interpreting file %s\n", args[0]);
 	    return program.accept(v);
         }
         public String toString() {
 	    return "load";
         }
     }),
     /*
     PRINT(new Function() {
         public int arity() {
         }
         public CronoType run(Visitor v, CronoType[] args) {
         }
         public String toString() {
         }
     }),
     *//*
     STRUCT(new Function() {
         public int arity() {
         }
         public CronoType run(Visitor v, CronoType[] args) {
         }
         public String toString() {
         }
     }),
     *//*
     SUBSTRUCT(new Function() {
         public int arity() {
         }
         public CronoType run(Visitor v, CronoType[] args) {
         }
         public String toString() {
         }
     }),
     *//*
     NEWSTRUCT(new Function() {
         public int arity() {
         }
         public CronoType run(Visitor v, CronoType[] args) {
         }
         public String toString() {
         }
     }),
     *//*
     EVAL(new Function() {
         public int arity() {
         }
         public CronoType run(Visitor v, CronoType[] args) {
         }
         public String toString() {
         }
     }),
     */
     ;
     
     public final Function function;
     private CronoFunction(Function fun) {
 	this.function = fun;
     }
 }
