 package com.gummy.types;
 
 import java.util.List;
 
 import com.gummy.core.Environment;
 import com.gummy.core.InterpreterException;
 import com.gummy.core.Marshall;
 import com.gummy.forms.Begin;
 
 /**
  * This contains a variadic procedure, which includes a list of bindings,
  * expressions and a scope. The last binding is assumed to be the variadic one.
  * The reason for this seperation is improved performance.
  * 
  * @author Dominic Charley-Roy
  * 
  */
 public class VariadicCompoundProcedure extends Procedure {
 
 	private List<Object> bindings;
 	private List<Object> expressions;
 	private Environment outerScope;
 
 	public VariadicCompoundProcedure(Pair bindings, Pair expressions,
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
	public Object apply(Environment callingScope, List<Object> arguments) {
 		// Make sure we have the right number of arguments
 		if (arguments.size() < this.bindings.size() - 1) {
 			throw new InterpreterException(
 					"Non-matching number of arguments. At least "
 							+ (bindings.size() - 1) + " argument(s) expected.");
 		}
 
 		// Create the new scope based on the outer scope
 		Environment scope = new Environment(outerScope);
 
 		// Iterate through the fixed, attaching them to bindings
 		for (int i = 0; i < bindings.size() - 1; i++) {
 			scope.defineValue(Marshall.getSymbol(bindings.get(i)),
					Expression.eval(arguments.get(i), callingScope));
 		}
 		// Attach the non-fixed elements to the varaidic binding
 		scope.defineValue(
 				Marshall.getSymbol(bindings.get(bindings.size() - 1)), Pair
 						.evaluateFromList(arguments.subList(
 								bindings.size() - 1, arguments.size()),
								callingScope));
 
 		// Evaluate all statements apart the last one
 		for (int i = 0; i < expressions.size() - 1; i++)
 			Expression.eval(expressions.get(i), scope);
 
 		// Evaluate and return the last expression
 		return Expression.eval(expressions.get(expressions.size() - 1), scope);
 	}
 }
