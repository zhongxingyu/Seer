 package crono;
 
 import java.util.ArrayList;
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
 import crono.type.Symbol;
 import crono.type.TypeId;
 
 public class Interpreter extends Visitor {
     private static final String _scope_err = "No object %s in scope";
     private static final String _too_many_args =
 	"Too many arguments to %s: %d/%d recieved";
     private static final String _type_scope_err = "No type %s in scope";
     
     public boolean show_env;
     public boolean show_closure;
     public boolean dprint_enable;
     public boolean dprint_ident;
     public boolean dynamic;
     
     protected int indent_level;
     protected Function.EvalType eval;
     protected Types types;
     
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
 	env_stack.push(new Environment());
 	types = new Types();
     }
     
     public CronoType visit(Cons c) {
 	if(c.isQuoted()) {
 	    c.quote(false);
 	    return c;
 	}
 	if(eval == Function.EvalType.NONE) {
 	    return c;
 	}
 	
 	Iterator<CronoType> iter = c.iterator();
 	if(!(iter.hasNext())) {
 	    return c; /*< C is an empty list (may be Nil or T) */
 	}
 	
 	CronoType value = iter.next().accept(this);
 	if(value instanceof Function) {
 	    Function.EvalType reserve = eval;
 	    /* Set the eval type to the current function's type; this keeps
 	     * type errors in builtins from happening, ex:
 	     * (+ arg1 arg2) under partial evaluation would fail since +
 	     * expects two numbers.
 	     */
 	    eval = ((Function)value).eval();
 	    if(eval.level > reserve.level) {
 		eval = reserve;
 	    }
 	    List<CronoType> args = new ArrayList<CronoType>();
 	    while(iter.hasNext()) {
 		args.add(iter.next().accept(this));
 	    }
 	    eval = reserve;
 	    
 	    int arglen = args.size();
 	    int nargs = ((Function)value).arity();
 	    if(arglen < nargs) {
 		if(arglen == 0) {
 		    /* Special case -- we don't have to do anything to the
 		     * function to return it properly. */
 		    return value;
 		}
 		
 		/* Curry it */
 		if(value instanceof LambdaFunction) {
 		    LambdaFunction lfun = ((LambdaFunction)value);
 		    Environment env = env_stack.peek();
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
 		    env_stack.push(env);
 		    CronoType lbody = lfun.body.accept(this);
 		    env_stack.pop();
 		    eval = reserve;
 		    
 		    /* Return the new, partially evaluated lambda */
 		    Symbol[] arglist = new Symbol[largs.size()];
 		    return new LambdaFunction(largs.toArray(arglist),
 					      lbody, lfun.environment);
 		}
 		/* Builtin partial evaluation */
 		
 		List<CronoType> body = new LinkedList<CronoType>();
 		body.add(value);
 		
 		body.addAll(args); /*< Dump args in order into the new cons */
 		
 		/* Add symbols for missing args */
 		List<Symbol> arglist = new ArrayList<Symbol>();
 		Symbol sym;
 		for(int i = arglen, n = 0; i < nargs; ++i, ++n) {
 		    sym = new Symbol(String.format("__interp_%d", n));
 		    body.add(sym);
 		    arglist.add(sym);
 		}
 		
 		/* Create a new lambda */
 		Symbol[] narglist = new Symbol[arglist.size()];
 		return new LambdaFunction(arglist.toArray(narglist),
 					  Cons.fromList(body),
 					  env_stack.peek());
 	    }
 	    if(arglen > nargs && !((Function)value).variadic()) {
		throw new RuntimeException(String.format(_too_many_args,
 							 arglen, nargs));
 	    }
 	    
 	    /* Full evaluation */
 	    if(value instanceof LambdaFunction && dynamic) {
 		/* We have to trick the lambda function if we want dynamic
 		 * scoping. I hate making so many objects left and right, but
 		 * this is the easiest way to do what I want here. */
 		LambdaFunction lfun = ((LambdaFunction)value);
 		lfun = new LambdaFunction(lfun.arglist, lfun.body,
 					  env_stack.peek());
 		CronoType[] argarray = new CronoType[args.size()];
 		return lfun.run(this, args.toArray(argarray));
 	    }
 	    CronoType[] argarray = new CronoType[args.size()];
 	    if(eval == Function.EvalType.FULL) {
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
 	if(a.isQuoted()) {
 	    a.quote(false);
 	    return a;
 	}
 	if(eval == Function.EvalType.NONE) {
 	    return a;
 	}
 	
 	CronoType t = a;
 	if(t instanceof Symbol) {
 	    t = env_stack.peek().get((Symbol)a);
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
 	if(t instanceof TypeId) {
 	    CronoType res = t; /*< Save symbol resolution in new CronoType */
 	    t = types.get((TypeId)t);
 	    if(t == null) {
 		if(eval == Function.EvalType.FULL) {
 		    throw new RuntimeException(String.format(_type_scope_err,
 							     a.toString()));
 		}
 		t = res; /*< Revert to symbol resolution */
 	    }
 	}
 	return t;
     }
 }
