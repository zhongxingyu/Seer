 package edu.wheaton.simulator.expression;
 
 import net.sourceforge.jeval.EvaluationException;
 import net.sourceforge.jeval.Evaluator;
 import net.sourceforge.jeval.function.Function;
 import net.sourceforge.jeval.function.FunctionConstants;
 import net.sourceforge.jeval.function.FunctionException;
 import net.sourceforge.jeval.function.FunctionResult;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.Entity;
 
 public abstract class AbstractExpressionFunction {
 
 	// RETURN_TYPE_NUMERIC and RETURN_TYPE_BOOL are supposed to have the same
 	// value.
 	// The distinction here is for clarity.
 	public static final int RESULT_TYPE_NUMERIC = FunctionConstants.FUNCTION_RESULT_TYPE_NUMERIC;
 	public static final int RESULT_TYPE_BOOL = FunctionConstants.FUNCTION_RESULT_TYPE_NUMERIC;
 	public static final int RESULT_TYPE_STRING = FunctionConstants.FUNCTION_RESULT_TYPE_STRING;
 	
 	/*
 	 * A wrapper constructed from a JEval Evaluator instance that is not known until the point at which the
 	 * function is called by JEval. For now it is null.
 	 */
 	private Expression evaluator=null;
 	
 	/**
 	 * The name of the function as it is to appear in an Expression (ex: "add")
 	 */
 	public abstract String getName();
 
 	/**
 	 * Returns one of the three constants defined at the top of this class
 	 */
 	public abstract int getResultType();
 
 	/**
 	 * When this method is called from within an Expression, it's parameters are evaluated
 	 * down to either a string ("'im a string'"), a boolean ("1"), or a double ("1.0" or "1")
 	 * and then passed to this function which then performs the intended logic
 	 */
 	public abstract String execute(String[] args) throws EvaluationException;
 
 	/**
 	 * returns the wrapper for the JEval Evaluator that is null until JEval calls the function
 	 * and provides a means to construct an ExpressionEvaluator
 	 * @return
 	 */
 	private Expression getExprEval(){
 		return evaluator;
 	}
 	
 	/**
 	 * Returns the Entity mapped to the given 'aliasName'
 	 * 
 	 * @param aliasName
 	 * 		the name used to refer to the entity in the expression
 	 */
 	public Entity resolveEntity(String aliasName) {
 		aliasName = Expression.correctNonStrVal(aliasName);
 		return getExprEval().getEntity(aliasName.replaceAll("'", ""));
 	}
 	
 	/**
 	 * Same as resolveEntity except that the return value is casted
 	 * to Agent
 	 */
 	public Agent resolveAgent(String aliasName){
 		aliasName = Expression.correctNonStrVal(aliasName);
 		return (Agent)resolveEntity(aliasName);
 	}
 
 	/**
 	 * Returns the variable value (doubleStr,stringStr, or boolStr) mapped to the given 'aliasName'
 	 * 
 	 * @param aliasName
 	 * 		the name used to refer to the variable in the expression
 	 */
 	public String resolveVariable(String variableName)
 			throws EvaluationException {
 		variableName = Expression.correctNonStrVal(variableName);
 		return getExprEval().getVariableValue(variableName);
 	}
 
 	/**
 	 * Formats this object into a form that is usable by JEval such that
 	 * the layer of abstraction is not lost.
 	 */
 	protected Function toJEvalFunction() {
 		final AbstractExpressionFunction xEnclosingWrapper = this;
 		return new Function() {
 
 			/*
 			 * used to differentiate between ExpressionFunction.execute() and Function.execute()
 			 */
 			protected final AbstractExpressionFunction enclosingWrapper = xEnclosingWrapper;
 
 			@Override
			public FunctionResult execute(Evaluator eval, String arguments)
 					throws FunctionException {
 				
 				/*
 				 * wraps the passed 'Evaluator' into an ExpressionEvaluator to provide a higher level
 				 * of abstraction away from the internals of JEval
 				 */
				setExprEval(wrapEvaluator(eval));
 				
 				String[] args = arguments.split(",");
 				try {
 					return new FunctionResult(enclosingWrapper.execute(args),
 							getResultType());
 				} catch (EvaluationException e) {
 					throw new FunctionException(arguments);
 				}
 			}
 
 			@Override
 			public String getName() {
 				return enclosingWrapper.getName();
 			}
 
 		};
 	}
 	
 	private static Expression wrapEvaluator(Evaluator evaluator){
 		return new Expression(evaluator,(Expression.EntityFieldResolver)evaluator.getVariableResolver());
 	}
 
 	/**
 	 * Used to set the ExpressionEvaluator once constructed
 	 * @param evaluator
 	 */
 	private void setExprEval(Expression evaluator) {
 		this.evaluator = evaluator;
 	}
 }
