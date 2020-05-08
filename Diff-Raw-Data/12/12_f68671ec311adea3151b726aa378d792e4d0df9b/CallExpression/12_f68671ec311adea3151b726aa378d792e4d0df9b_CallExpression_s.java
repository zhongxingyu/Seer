 package translator.Expressions;
 
 import java.util.ArrayList;
 
 import translator.JavaType;
 import translator.JavaClass;
 import translator.JavaMethod;
 import translator.JavaMethodSignature;
 import translator.JavaScope;
 import translator.Visibility;
 import translator.JavaStatic;
 import xtc.tree.GNode;
 
 /**
  * Good times, good times.
  */
 public class CallExpression extends JavaExpression {
 	/**
 	 * The object or class making this method call
 	 */
 	private JavaExpression caller;
 	
 	/**
 	 * String holding the name of the method.
 	 * This is not really useful and will be removed later (I hope)
 	 */
 	private String methodName;
 	
 	/**
 	 * The signature of the method to call.
 	 */
 	private JavaMethodSignature sig = new JavaMethodSignature();
 	
 	/**
 	 * The called method.
 	 */
 	JavaMethod method;
 
 	/**
 	 * Is this expression part of a method chain?
 	 */
 	private boolean chaining;
 	
 	/**
 	 * If we have an implied "this" for this expression.
 	 */
 	private boolean impliedThis;
 	
 	/**
 	 * Why java, why?! Just override constructors :(
 	 */
 	public CallExpression(JavaScope scope, GNode n) {
 		super(scope, n);
 		
 		//setup the other stuff we need
 		//we do this here and not in onInstantiate because, when onInstantiate is called,
 		//this.sig is not yet instantiated...so we can't use it.
 		//seriously, wtf, java?
 		this.visit(n);
 		this.finishSetup();
 	}
 
 	/**
 	 * Populate our list of arguments and set our caller.
 	 */
 	public void onInstantiate(GNode n) {
 		//we only need the method name to begin with
 		this.methodName = n.get(2).toString();
 		
 		//our caller
 		this.setupCaller((GNode)n.get(0));
 		
 		//remove our caller from the visitor since we had to do him manually 
 		//(each n[0] could be any number of things, so visitors didnt work)
 		n.set(0, null);
 	}
 
 	/**
 	 * Does some final work on all our new data.
 	 */
 	private void finishSetup() {
 		if ((this.caller instanceof CallExpression) && (this.method != null && !this.method.isStatic())) {
 			this.chaining = true;
 			this.getMyMethod().hasChaining();
 		} else {
 			this.chaining = false;
 		}
 		
 		//use our caller to setup our method and return type
 		if (this.caller.getType() != null && this.caller.getType().getJavaClass() != null) {
 			this.method = this.caller.getType().getJavaClass().getMethod(this.methodName, this.sig);
 			
			if (this.method != null)
 				this.setType(this.method.getType());
			else
				JavaStatic.runtime.error("Expressions.CallExpression: Method not found: " + this.methodName + (this.methodName.equals("println") ? " (Did you implement an overloaded version of System.out.println() to handle this?)" : ""));
 		} else {
 			JavaStatic.runtime.warning("Expressions.CallExpression: No suitable caller could be found for: " + this.methodName);
 		}
 	}
 	
 	/**
 	 * Find out who is calling us.
 	 */
 	private void setupCaller(GNode n) {
 		this.impliedThis = false;
 	
 		//well, that was easy: we don't have a caller, so it's an implied "this"
 		if (n == null)
 			this.impliedThis = true; //if no caller is specified, it's assumed local to "this"
 			
 		//there is a caller!
 		else
 			this.caller = (JavaExpression)this.dispatch(n);
 	}
 
 	public String print() {
 		String ret = "";
 		
 		//already issued a warning if we couldn't find a caller
 		if (this.method != null) {
 			this.setType(this.method.getType());
 			
 			//check if we have a "__this" or something else
 			if (this.impliedThis) {
 				//every method that is not static needs "__this"
 				ret += "__this->" + this.throughVTable();
 			} else {
 				ret += this.caller.print() + (this.method.isStatic() ? "::" : "->" + this.throughVTable());
 			}
 			
 			ret += this.method.getCppName(false) + "(";
 			
 			//do we need to pass in a "this"?
 			if (!this.method.isStatic()) {
 				//if we're operating on a class and not a primitive
 				if (this.caller.getType().getJavaClass() != null)
 					ret += this.caller.print() + (this.sig.size() > 0 ? ", " : "");
 			}
 			
 			//make our substringing easier -- only do it when we have args
 			if (this.sig.size() > 0) {
 				for (JavaScope s : this.sig.getArguments())
 					ret += ((JavaExpression)s).print() + ", ";
 				
 				ret = ret.substring(0, ret.length() - 2);
 			}
 			
 			ret += ")";
 		}
 		
 		return ret;
 	}
 	
 	/**
 	 * Determines whether or not our method should be routed through the vTable.  This is reserved
 	 * for Protected (or more visible) methods, and not static.  Note: this method doesn't check if things
 	 * are static.
 	 *
 	 * @return "__vptr->" if the method should be routed through the vTable, an empty string, otherwise.
 	 */
 	public String throughVTable() {
 		//if our method has the visbility to be routed through the vTable
 		if (this.method.isAtLeastVisible(Visibility.PROTECTED))
 			return "__vptr->";
 		return "";
 	}
 
 	/**
 	 * ==================================================================================================
 	 * Visitor Methods
 	 */
 	
 	/**
 	 * It's a little-known fact that arguments come in all shapes and sizes.  Quite painfully, I might add.
 	 */
 	public void visitArguments(GNode n) {
 		for (int i = 0; i < n.size(); i++) {
 			JavaExpression e = (JavaExpression)this.dispatch((GNode)n.get(i));
 			
 			if (e != null) {
 				this.sig.add(e.getType(), e);
 				if (e.getType() == null)
 					JavaStatic.runtime.error("Expressions.CallExpression: Argument type for method \"" + this.methodName + "\" could not be determined (argument " + (i + 1) + " of " + n.size() + ").");
 			} else {
 				JavaStatic.runtime.error("Expressions.CallExpression: Type could not be found for method argument.");
 			}
 		}
 	}
 }
