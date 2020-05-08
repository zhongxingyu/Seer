 package eu.stratosphere.sopremo.expressions;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
import com.google.common.base.Preconditions;

 import eu.stratosphere.sopremo.EvaluationException;
 import eu.stratosphere.sopremo.expressions.tree.ChildIterator;
 import eu.stratosphere.sopremo.expressions.tree.ConcatenatingChildIterator;
 import eu.stratosphere.sopremo.expressions.tree.ListChildIterator;
 import eu.stratosphere.sopremo.function.SopremoFunction;
 import eu.stratosphere.sopremo.packages.EvaluationScope;
 import eu.stratosphere.sopremo.pact.SopremoUtil;
 import eu.stratosphere.sopremo.type.IJsonNode;
 import eu.stratosphere.sopremo.type.JsonUtil;
 
 /**
  * Calls the specified function with the provided parameters and returns the result.
  */
 @OptimizerHints(scope = Scope.ANY, minNodes = 0, maxNodes = OptimizerHints.UNBOUND)
 public class FunctionCall extends EvaluationExpression {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 90022725022477041L;
 
 	private final String functionName;
 
 	private final SopremoFunction function;
 
 	private List<EvaluationExpression> paramExprs;
 
 	/**
 	 * Initializes a MethodCall with the given function name and expressions which evaluate to the method parameters.
 	 * 
 	 * @param functionName
 	 *        the name of the function that should be called
 	 * @param function
 	 *        the function itself
 	 * @param params
 	 *        expressions which evaluate to the method parameters
 	 */
 	public FunctionCall(final String functionName, final SopremoFunction function, final EvaluationExpression... params) {
 		this(functionName, function, Arrays.asList(params));
 	}
 
 	/**
 	 * Initializes a MethodCall with the given function name and expressions which evaluate to the method parameters.
 	 * 
 	 * @param functionName
 	 *        the name of the function that should be called
 	 * @param function
 	 *        the function itself
 	 * @param params
 	 *        expressions which evaluate to the method parameters
 	 */
 	public FunctionCall(final String functionName, final SopremoFunction function,
 			final List<EvaluationExpression> params) {
 		if (functionName == null)
 			throw new NullPointerException("Function name must not be null");
 		if (function == null)
 			throw new NullPointerException("Function must not be null");
 		for (EvaluationExpression param : params)
 			if (param == null)
 				throw new NullPointerException("Params must not be null " + params);
 		this.functionName = functionName;
 		this.function = function;
 		this.paramExprs = params;
 	}
 
 	/**
 	 * Initializes a MethodCall with the given function name and expressions which evaluate to the method parameters.
 	 * 
 	 * @param functionName
 	 *        the name of the function that should be called
 	 * @param context
 	 *        the {@link EvaluationContext} which contains the
 	 *        {@link eu.stratosphere.sopremo.packages.IFunctionRegistry} that contains the definition under the function
 	 *        name
 	 * @param params
 	 *        expressions which evaluate to the method parameters
 	 */
 	public FunctionCall(final String functionName, final EvaluationScope scope,
 			final EvaluationExpression... params) {
 		this(functionName, checkIfMethodExists(functionName, scope), Arrays.asList(params));
 	}
 
 	private static SopremoFunction checkIfMethodExists(final String functionName, final EvaluationScope scope) {
 		final SopremoFunction function = (SopremoFunction) scope.getFunctionRegistry().get(functionName);
 		if (function == null)
 			throw new IllegalArgumentException(String.format("No method %s found", functionName));
 		return function;
 	}
 
 	/**
 	 * Initializes a MethodCall with the given function name and expressions which evaluate to the method parameters.
 	 * 
 	 * @param functionName
 	 *        the name of the function that should be called
 	 * @param context
 	 *        the {@link EvaluationContext} which contains the
 	 *        {@link eu.stratosphere.sopremo.packages.IFunctionRegistry} that contains the definition under the function
 	 *        name
 	 * @param params
 	 *        expressions which evaluate to the method parameters
 	 */
 	public FunctionCall(final String functionName, final EvaluationScope scope,
 			final List<EvaluationExpression> params) {
 		this(functionName, checkIfMethodExists(functionName, scope), params);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see eu.stratosphere.sopremo.expressions.EvaluationExpression#createCopy()
 	 */
 	@Override
 	protected EvaluationExpression createCopy() {
 		return new FunctionCall(this.functionName, this.function.clone(), SopremoUtil.deepClone(this.paramExprs));
 	}
 
 	@Override
 	public boolean equals(final Object obj) {
 		if (!super.equals(obj))
 			return false;
 		final FunctionCall other = (FunctionCall) obj;
 		return this.function.equals(other.function) && this.paramExprs.equals(other.paramExprs);
 	}
 
 	/**
 	 * Returns the function.
 	 * 
 	 * @return the function
 	 */
 	public SopremoFunction getFunction() {
 		return this.function;
 	}
 
 	/**
 	 * Returns the functionName.
 	 * 
 	 * @return the functionName
 	 */
 	public String getFunctionName() {
 		return this.functionName;
 	}
 
 	/**
 	 * Returns the paramExprs.
 	 * 
 	 * @return the paramExprs
 	 */
 	public List<EvaluationExpression> getParameters() {
 		return this.paramExprs;
 	}
 
 	@Override
 	public int hashCode() {
 		int hash = super.hashCode();
 		hash = hash * 53 + this.function.hashCode();
 		hash = hash * 53 + this.paramExprs.hashCode();
 		return hash;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see eu.stratosphere.sopremo.expressions.EvaluationExpression#evaluate(eu.stratosphere.sopremo.type.IJsonNode)
 	 */
 	@Override
 	public IJsonNode evaluate(IJsonNode node) {
 		final List<EvaluationExpression> paramExprs = this.paramExprs;
 		final IJsonNode[] params = new IJsonNode[paramExprs.size()];
 		for (int index = 0; index < params.length; index++)
 			params[index] = paramExprs.get(index).evaluate(node);
 
 		try {
 			return this.function.call(JsonUtil.asArray(params));
 		} catch (Exception e) {
 			throw new EvaluationException(e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see eu.stratosphere.sopremo.expressions.ExpressionParent#iterator()
 	 */
 	@Override
 	public ChildIterator iterator() {
 		return new ConcatenatingChildIterator(super.iterator(), new ListChildIterator(this.paramExprs.listIterator()));
 	}
 
 	@Override
 	public void appendAsString(final Appendable appendable) throws IOException {
 		this.function.appendAsString(appendable);
 		appendable.append('(');
 		this.appendChildExpressions(appendable, this.paramExprs, ", ");
 		appendable.append(')');
 	}
 
 }
