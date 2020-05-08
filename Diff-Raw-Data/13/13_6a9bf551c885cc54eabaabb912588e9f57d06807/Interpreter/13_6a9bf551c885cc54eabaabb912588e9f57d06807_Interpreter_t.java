 
 public class Interpreter {
 	private ReferenceFrame builtInEnv, filescope;
 	
 	public Interpreter(){
 		this.builtInEnv=new ReferenceFrame(null);
 		this.filescope=new ReferenceFrame(builtInEnv);
 		
 		builtInEnv.addReference(new Ident("quit"), new BuiltIn());
 		builtInEnv.addReference(new Ident("read"), new BuiltIn());
 		builtInEnv.addReference(new Ident("apply"), new BuiltIn());
 		builtInEnv.addReference(new Ident("write"), new BuiltIn());
 		builtInEnv.addReference(new Ident("eval"), new BuiltIn());
 		builtInEnv.addReference(new Ident("symbol?"), new BuiltIn());
 		builtInEnv.addReference(new Ident("number?"), new BuiltIn());
 		builtInEnv.addReference(new Ident("read"), new BuiltIn());
 		builtInEnv.addReference(new Ident("car"), new BuiltIn());
 		builtInEnv.addReference(new Ident("cdr"), new BuiltIn());
 		builtInEnv.addReference(new Ident("cons"), new BuiltIn());
 		builtInEnv.addReference(new Ident("set-car!"), new BuiltIn());
 		builtInEnv.addReference(new Ident("set-cdr!"), new BuiltIn());
 		builtInEnv.addReference(new Ident("null?"), new BuiltIn());
 		builtInEnv.addReference(new Ident("pair?"), new BuiltIn());
 		builtInEnv.addReference(new Ident("eq?"), new BuiltIn());
 		builtInEnv.addReference(new Ident("procedure?"), new BuiltIn());
 		builtInEnv.addReference(new Ident("newline"), new BuiltIn());
 		builtInEnv.addReference(new Ident("display"), new BuiltIn());
 		builtInEnv.addReference(new Ident("interaction-environment"), new BuiltIn());
 		builtInEnv.addReference(new Ident("b+"), new BuiltIn());
 		builtInEnv.addReference(new Ident("b-"), new BuiltIn());
 		builtInEnv.addReference(new Ident("b*"), new BuiltIn());
 		builtInEnv.addReference(new Ident("b/"), new BuiltIn());
 		builtInEnv.addReference(new Ident("b="), new BuiltIn());
 		builtInEnv.addReference(new Ident("b<"), new BuiltIn());
 		builtInEnv.addReference(new Ident("sfoo"), new StrLit("This is foo!!!!"));
 		builtInEnv.addReference(new Ident("ifoo"), new IntLit(1325));
 		builtInEnv.addReference(new Ident("bfoo"), new BooleanLit(true));
 	}
 	
 	public Node eval(Node expr, ReferenceFrame ref) {
 		if (expr instanceof Cons) {
 			Special form = ((Cons) expr).getForm();
 			if (form instanceof Regular) {
 				return regular(expr, ref);
 			} else if (form instanceof Define) {
 				define(expr, ref);
 			} else if (form instanceof Set) {
 				set(expr, ref);
 			} else if (form instanceof Let) {
 				return let(expr, ref);
 			}
 			return expr;
 		} else {
 			if (expr instanceof Ident) {
 				Node val = ref.assq((Ident)expr);
 				if (val != null) {
 					expr = val;
 				} else {
 					System.err.println(((Ident)expr).getName() + " is not defined.");
 				}
 			}
 			return expr;
 		}
 	}
 	
 	public Node eval(Node expr) {
 		return this.eval(expr, filescope);
 	}
 	
 	public Node apply(Closure closure, Node actualParam){
 		ReferenceFrame ref = new ReferenceFrame(closure.getRef());
 		Node formalParam = closure.getArgs();
 		if (formalParam != null && actualParam != null) {
 			Node arg = actualParam, param = formalParam;
 			while (arg != null && param != null) {
 				ref.addReference((Ident)param.getCar(), arg.getCar());
 				arg = arg.getCdr();
 				param = param.getCdr();
 			}
 			if (arg !=null || param != null)
 				System.err.println("Invalid paramters quantity to function");
 		}
 		return eval((Node)closure.getCode(), ref);
 	}
 	
 	private Node regular(Node expr, ReferenceFrame ref) {
		Cons newCdr = null;
 		if (expr.getCdr() != null) {
 			Cons cdr = (Cons)expr.getCdr();
			newCdr = new Cons(eval(cdr.getCar(), ref), eval(cdr.getCdr(), ref));
 		}
 		Node first = eval(expr.getCar(), ref);
 		if (first instanceof Closure)
			return apply((Closure)first, expr.getCdr()).getCar(); 
		else
			return new Cons(first, newCdr);
 	}
 	
 	private ReferenceFrame define(Node expr, ReferenceFrame ref) {
 		Node key = expr.getCdr().getCar();
 		Node val = expr.getCdr().getCdr();
 		if (key instanceof Cons) { // procedure
 			Cons func = (Cons)key;
 			Ident id = (Ident)func.getCar();
 			Node args = func.getCdr();
 			ref.addReference(id, new Closure(args, val, ref));
 			return ref;
 		} else { // variable
 			val = val.getCar();
 			Node tmp = eval(val, ref);
 			if (tmp != null) val = tmp;
 			ref.addReference((Ident)key, val);
 			return ref;
 		}
 	}
 	
 	private ReferenceFrame set(Node expr, ReferenceFrame ref) {
 		Ident key = (Ident)expr.getCdr().getCar();
 		Node val = expr.getCdr().getCdr().getCar();
 		if (ref.assq(key) != null) {
 			Node tmp = eval(val, ref);
 			if (tmp != null) val = tmp;
 			ref.addReference(key, val);
 		} else {
 			System.err.println("The variable used in set! must be defined.");
 		}
 		return ref;
 	}
 	
 	private Node let(Node expr, ReferenceFrame ref) {
 		ReferenceFrame letRef = new ReferenceFrame(ref);
 		Node defs = expr.getCdr().getCar();
 		while (defs != null) {
 			Ident key = (Ident)defs.getCar().getCar();
 			Node val = defs.getCar().getCdr().getCar();
 			if (val instanceof Ident) {
 				Node tmp = ref.assq((Ident)val);
 				if (tmp != null) val = tmp;
 				else System.err.println(((Ident)val).getName() + " is not defined.");
 			}
 			if (letRef.hasIdent(key)) {
 				System.err.println(key.getName() + " cannot be double defined in let.");
 			} else {
 				letRef.addReference(key, val);
 			}
 			defs = defs.getCdr();
 		}
 		Node body = expr.getCdr().getCdr();
 		return eval(body, letRef).getCar();
 	}
 }
