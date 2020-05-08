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
 	 * If we should redirect our return to the __chain variable.
 	 */
 	private boolean outputToChain;
 	
 	/**
 	 * If we have an implied "this" for this expression.
 	 */
 	private boolean impliedThis;
 
 	/**
 	 * Are we a super class constructor?
 	 */
 	private boolean superConstructor;
 	
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
 		this.superConstructor = false;
 		//get the method name and check if it's "super"
 		if ((this.methodName = n.get(2).toString()).equals("super") && this.getMyMethod().isConstructor()) {
 			this.superConstructor = true;
 			this.methodName = this.getJavaClass().getParent().getName(false);
 			this.getMyMethod().hasSuper();
 		}
 		
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
 		//use our caller to setup our method and return type
 		if (this.caller.getType() != null && this.caller.getType().getJavaClass() != null) {
 			this.method = this.caller.getType().getJavaClass().getMethod(this.methodName, this.sig);
 			
 			if (this.method != null) {
 				this.setType(this.method.getType());
 			} else {
				//System.out.println(this.caller.getType().getJavaClass().getName() + "." + this.methodName);
				//System.out.println(this.sig.getCppMangledArgumentList() + " -- " + this.sig.size());
 				JavaStatic.runtime.error(
 					"Expressions.CallExpression: Method not found: " + 
 					this.methodName +
 					(this.methodName.equals("println") || this.methodName.equals("print") ?
 						" (Did you implement an overloaded version of System.out.print/ln() to handle this?)" : ""
 					)
 				);
 			}
 		} else {
 			JavaStatic.runtime.warning("Expressions.CallExpression: No suitable caller could be found for: " + this.methodName);
 		}
 		
 		//once we find our method, check if we're chaining with him
 		if ((this.caller instanceof CallExpression) && this.method != null) {
 			this.chaining = true;
 			this.getMyMethod().hasChaining();
 			((CallExpression)this.caller).outputToChain = true; 
 		} else {
 			this.chaining = false;
 			this.outputToChain = false;
 		}
 	}
 	
 	/**
 	 * Find out who is calling us.
 	 */
 	private void setupCaller(GNode n) {
 		this.impliedThis = false;
 		
 		//if we're a call to our super class.
 		if (this.superConstructor) {
 			this.caller = (JavaExpression)this.dispatch(GNode.create("SuperExpression", null));
 		}
 		//well, that was easy: we don't have a caller, so it's an implied "this"
 		else if (n == null) {
 			//there's probably a better way to do with an expression type, but meh
 			this.impliedThis = true;
 			
 			//this is only for method resolution and should never have its print() method called.
 			this.caller = (JavaExpression)this.dispatch(GNode.create("ThisExpression", null));
 		//there is a caller!
 		} else {
 			this.caller = (JavaExpression)this.dispatch(n);
 		}
 	}
 
 	public String print() {
 		String ret = "";
 		
 		//already issued a warning if we couldn't find a caller
 		if (this.method == null)
 			return ret;
 
 		//set our type to the method's return type
 		this.setType(this.method.getType());
 		
 		//set ourself as static based on our method type
 		this.caller.isStaticAccess(this.method.isStatic());
 		
 		//if we're static, we do different routing than non-static, and we chain differently
 		if (this.method.isStatic())
 			ret += this.doStaticMethod();
 		else
 			ret += this.doNonstaticMethod();
 
 		return ret;
 	}
 	
 	/**
 	 * Does all of the formatting necessary for a call to a static method.
 	 */
 	private String doStaticMethod() {
 		String ret = "";
 		
 		//if we we're implicitly called on our class
 		if (this.impliedThis) {
 			ret += this.method.getJavaClass().getCppName(true, false) + "::";
 		} else {
 			//if our caller isn't a call expression, then our life is much easier
 			if (!(this.caller instanceof CallExpression)) {
 				ret += this.caller.print() + "::";
 			
 			//our caller is a call expression...so even though he's static in Java, he's not static in C++
 			//his return type MUST be a type, so we have to treat it like a variable that we're acting on,
 			//so it must follow all the rules of a variable...but we already have that defined!
 			//so let's just pass it off to doNonstaticMethod()
 			} else {
 				return this.doNonstaticMethod();
 			}
 		}
 		
 		ret += this.getMethodCall();
 		
 		return ret;
 	}
 	
 	/**
 	 * Does all of the formatting necessary for a call to a non-static method.
 	 */
 	private String doNonstaticMethod() {
 		String ret = "";
 		//if we're a super expression we need to do things differently
 		if (this.caller instanceof SuperExpression) {
 			ret += this.caller.print() + "::" + (this.superConstructor?"__CONSTRUCTOR__":"") + 
 				this.method.getCppName(false) + "(__this" + 
 				(this.sig.size() > 0 ? ", " : "");
 			if (this.sig.size() > 0) {
 				for (JavaScope s : this.sig.getArguments())
 					ret += ((JavaExpression)s).print() + ", ";
 			
 				ret = ret.substring(0, ret.length() - 2);
 			}
 			ret += ")";
 		}
 		//check if we have a "__this" or something else
 		else if (this.impliedThis) {
 			//every method that is not static needs "__this->" as the caller
 			//but if it is static, we need to get the method's class name as that is what he is being called on.
 			ret += "__this->" + this.throughVTable() + this.getMethodCall();
 		} else {
 			//if we're chaining, then we need to do some fancy storing into a temp variable for passing around "__this"
 			if (this.outputToChain)
 				ret += "((" + this.method.getType().getCppName() + ")(__chain = (java::lang::Object)";
 		
 			ret += this.caller.print() + "->" + this.throughVTable() + this.getMethodCall();
 		
 			if (this.outputToChain)
 				ret += "))";
 		}
 		
 		return ret;
 	}
 	
 	/**
 	 * Gets the method call (in method(arg1, arg2) form).
 	 */
 	private String getMethodCall() {
 		//prepare to print the arguments
 		String ret = this.method.getCppName(false) + "(";
 		
 		//do we need to pass in a "
 		//also make sure we're operating on a class and not a primitive
 		if (!this.method.isStatic() && this.caller.getType().getJavaClass() != null) {
 			//if we're chaining, then our "__this" comes from the chain variable
 			if (this.chaining)
 				ret += "__chain"; //since this is java::lang::Object, it can be auto-cast into the required type for the "__this" in the method
 			else
 				ret += this.caller.print();
 			
 			ret += (this.sig.size() > 0 ? ", " : "");
 		}
 		
 		//make our substringing easier -- only do it when we have args
 		if (this.sig.size() > 0) {
 			for (JavaScope s : this.sig.getArguments())
 				ret += ((JavaExpression)s).print() + ", ";
 			
 			ret = ret.substring(0, ret.length() - 2);
 		}
 		
 		return ret + ")";
 	}
 	
 	/**
 	 * Determines whether or not our method should be routed through the vTable.  This is reserved
 	 * for Protected (or more visible) methods, and not static.
 	 *
 	 * @return "__vptr->" if the method should be routed through the vTable, an empty string, otherwise.
 	 */
 	public String throughVTable() {
 		//if our method has the visbility to be routed through the vTable
 		if (this.method.isAtLeastVisible(Visibility.PROTECTED) && !this.method.isStatic())
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
 					JavaStatic.runtime.error(
 						"Expressions.CallExpression: Argument type for method \"" + this.methodName +
 						"\" could not be determined (argument " + (i + 1) + " of " + n.size() + ")."
 					);
 			} else {
 				JavaStatic.runtime.error("Expressions.CallExpression: Type could not be found for method argument.");
 			}
 		}
 	}
 }
