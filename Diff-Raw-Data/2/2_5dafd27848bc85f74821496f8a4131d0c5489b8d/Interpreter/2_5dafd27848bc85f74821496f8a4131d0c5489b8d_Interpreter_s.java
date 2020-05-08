 package crono;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 
 import crono.type.Atom;
 import crono.type.Cons;
 import crono.type.CronoType;
 import crono.type.Function;
 import crono.type.LambdaFunction;
 import crono.type.Nil;
 import crono.type.Quote;
 import crono.type.Symbol;
 import crono.type.CronoTypeId;
 import crono.type.TypeId;
 
 public class Interpreter extends Visitor {
     private static final String _scope_err = "No object %s in scope";
     private static final String _too_many_args =
 	"Too many arguments to %s: %d/%d recieved";
     private static final String _type_scope_err = "No type %s in scope";
     private static final String _type_mismatch =
 	"Function '%s' expected arguments %s; got %s";
     
     public boolean show_env;
     public boolean show_closure;
     public boolean dprint_enable;
     public boolean dprint_ident;
     public boolean dynamic;
     
     protected int indent_level;
     protected Function.EvalType eval;
     
     public Interpreter() {
 	show_env = false;
 	show_closure = false;
 	dprint_enable = false;
 	dprint_ident = true;
 	dynamic = false;
 	
 	indent_level = 0;
 	eval = Function.EvalType.FULL;
 	
 	env_stack = new Stack<Environment>();
 	reset(); /*< Set up initial environment and types */
     }
     public void reset() {
 	env_stack.clear();
 	pushEnv(new Environment());
     }
     
     public CronoType visit(Cons c) {
 	if(eval == Function.EvalType.NONE) {
 	    return c;
 	}
 	
 	Iterator<CronoType> iter = c.iterator();
 	if(!(iter.hasNext())) {
 	    return c; /*< C is an empty list (may be Nil or T) */
 	}
 	
 	CronoType value = iter.next().accept(this);
 	if(value instanceof Function) {
 	    Function fun = ((Function)value);
 	    
 	    Function.EvalType reserve = eval;
 	    /* Set the eval type to the current function's type; this keeps
 	     * type errors in builtins from happening, ex:
 	     * (+ arg1 arg2) under partial evaluation would fail since +
 	     * expects two numbers.
 	     */
 	    eval = fun.eval;
 	    if(eval.level > reserve.level) {
 		eval = reserve;
 	    }
 	    List<CronoType> args = new ArrayList<CronoType>();
 	    while(iter.hasNext()) {
 		args.add(iter.next().accept(this));
 	    }
 	    eval = reserve;
 	    
 	    int arglen = args.size();
 	    int nargs = fun.arity;
 	    if(arglen < nargs) {
 		if(arglen == 0) {
 		    /* Special case -- we don't have to do anything to the
 		     * function to return it properly. */
 		    return fun;
 		}
 		
 		/* Curry it */
 		if(fun instanceof LambdaFunction) {
 		    LambdaFunction lfun = ((LambdaFunction)fun);
 		    Environment env = getEnv();
 		    if(!dynamic) {
 			/* Use the lambda's stored environment */
 			env = lfun.environment;
 		    }
 		    /* We want to preserve the current environment */
 		    env = new Environment(env);
 		    
 		    /* Put known values into the new environment */
 		    for(int i = 0; i < arglen; ++i) {
 			env.put(lfun.arglist[i], args.get(i));
 		    }
 		    /* Create new argument list and remove remaining args from
 		     * the new environment */
 		    List<Symbol> largs = new ArrayList<Symbol>();
 		    for(int i = arglen; i < lfun.arglist.length; ++i) {
 			largs.add(lfun.arglist[i]);
 			env.remove(lfun.arglist[i]);
 		    }
 		    
 		    /* Evaluate the body as much as possible */
 		    reserve = eval;
 		    eval = Function.EvalType.PARTIAL;
 		    pushEnv(env);
 		    CronoType[] lbody = new CronoType[lfun.body.length];
 		    for(int i = 0; i < lfun.body.length; ++i) {
 			lbody[i] = lfun.body[i].accept(this);
 		    }
 		    popEnv();
 		    eval = reserve;
 		    
 		    /* Return the new, partially evaluated lambda */
 		    Symbol[] arglist = new Symbol[largs.size()];
 		    return new LambdaFunction(largs.toArray(arglist),
 					      lbody, lfun.environment);
 		}
 		/* Builtin partial evaluation */
 		
 		List<CronoType> body = new LinkedList<CronoType>();
 		body.add(fun);
 		
 		body.addAll(args); /*< Dump args in order into the new cons */
 		CronoType[] barr = new CronoType[body.size()];
 		barr = body.toArray(barr);
 		
 		/* Add symbols for missing args */
 		List<Symbol> arglist = new ArrayList<Symbol>();
 		Symbol sym;
 		for(int i = arglen, n = 0; i < nargs; ++i, ++n) {
 		    sym = new Symbol(String.format("_i?%d!_", n));
 		    body.add(sym);
 		    arglist.add(sym);
 		}
 		
 		/* Create a new lambda */
 		Symbol[] narglist = new Symbol[arglist.size()];
 		return new LambdaFunction(arglist.toArray(narglist), barr,
 					  getEnv());
 	    }
 	    if(arglen > nargs && !fun.variadic) {
 		throw new RuntimeException(String.format(_too_many_args, fun,
 							 arglen, nargs));
 	    }
 	    
 	    /* Full evaluation */
 	    if(fun instanceof LambdaFunction && dynamic) {
 		/* We have to trick the lambda function if we want dynamic
 		 * scoping. I hate making so many objects left and right, but
 		 * this is the easiest way to do what I want here. */
 		LambdaFunction lfun = ((LambdaFunction)fun);
 		lfun = new LambdaFunction(lfun.arglist, lfun.body, getEnv());
 		
 		CronoType[] argarray = new CronoType[args.size()];
 		return lfun.run(this, args.toArray(argarray));
 	    }
 	    if(eval == Function.EvalType.FULL) {
 		CronoType[] argarray = new CronoType[args.size()];
 		argarray = args.toArray(argarray);
 		TypeId[] types = new TypeId[args.size()];
 		for(int i = 0; i < types.length; ++i) {
 		    types[i] = argarray[i].typeId();
 		}
		for(int i = 0; i < argarray.length; ++i) {
 		    if(!(fun.args[i].isType(argarray[i]))) {
 			String argstr = Arrays.toString(types);
 			String expected = Arrays.toString(fun.args);
 			throw new InterpreterException(_type_mismatch, fun,
 						       expected, argstr);
 		    }
 		}
 		return ((Function)value).run(this, args.toArray(argarray));
 	    }else {
 		args.add(0, value);
 		return Cons.fromList(args);
 	    }
 	}
 	
 	/* The initial value is not a function */
 	List<CronoType> list = new LinkedList<CronoType>();
 	list.add(value);
 	while(iter.hasNext()) {
 	    list.add(iter.next().accept(this));
 	}
 	return Cons.fromList(list);
     }
     
     public CronoType visit(Atom a) {
 	if(eval == Function.EvalType.NONE) {
 	    return a;
 	}
 	
 	CronoType t = a;
 	if(t instanceof Symbol) {
 	    t = getEnv().get((Symbol)a);
 	    if(t == null) {
 		if(eval == Function.EvalType.FULL) {
 		    throw new RuntimeException(String.format(_scope_err,
 							     a.toString()));
 		}
 		t = a;
 	    }
 	}
 	/* Not else-if, so that we perform a double-resolution on a symbol that
 	 * represents a TypeId */
 	if(t instanceof CronoTypeId) {
 	    CronoType res = t; /*< Save symbol resolution in new CronoType */
 	    t = getEnv().getType((CronoTypeId)t);
 	    if(t == null) {
 		if(eval == Function.EvalType.FULL) {
 		    throw new InterpreterException(_type_scope_err, a);
 		}
 		t = res; /*< Revert to symbol resolution */
 	    }
 	}
 	return t;
     }
     
     public CronoType visit(Quote q) {
 	return q.node;
     }
 }
