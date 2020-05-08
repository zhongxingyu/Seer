 package com.gummy.types;
 
 import java.util.List;
 
 import com.gummy.core.Environment;
 import com.gummy.core.InterpreterException;
 import com.gummy.core.Marshall;
 import com.gummy.forms.Begin;
 
 /**
  * This contains a simple (non-variadic) procedure, which includes a list of
  * bindings, expressions and a scope. The reason for this seperation is improved
  * performance.
  * 
  * @author Dominic Charley-Roy
  * 
  */
 public class SimpleCompoundProcedure extends Procedure {
 
 	private List<Object> bindings;
 	private List<Object> expressions;
 	private Environment outerScope;
 
 	public SimpleCompoundProcedure(Pair bindings, Pair expressions,
 			Environment outerScope) {
 		this.bindings = Pair.expand(bindings);
 		this.expressions = Pair.expand(expressions);
 		this.outerScope = outerScope;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.gummy.types.Procedure#apply(com.gummy.core.Environment,
 	 * java.util.List)
 	 */
 	@Override
	public Object apply(Environment environment, List<Object> arguments) {
 		// Make sure we have the right number of arguments
 		if (arguments.size() != this.bindings.size()) {
 			throw new InterpreterException("Non-matching number of arguments. "
 					+ bindings.size() + " argument(s) expected.");
 		}
 
 		// Create the new scope based on the outer scope
 		Environment scope = new Environment(outerScope);
 
 		// Iterate through the arguments, attaching them to bindings
 		for (int i = 0; i < arguments.size(); i++) {
 			scope.defineValue(Marshall.getSymbol(bindings.get(i)),
					Expression.eval(arguments.get(i), outerScope));
 		}
 
 		// Evaluate all statements apart the last one
 		for (int i = 0; i < expressions.size() - 1; i++)
 			Expression.eval(expressions.get(i), scope);
 
 		// Evaluate and return the last expression
 		return Expression.eval(expressions.get(expressions.size() - 1), scope);
 	}
 
 }
