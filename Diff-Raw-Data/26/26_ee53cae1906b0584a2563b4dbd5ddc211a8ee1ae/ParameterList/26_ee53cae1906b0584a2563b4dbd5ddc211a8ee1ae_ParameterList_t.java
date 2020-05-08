 package titocc.compiler.elements;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import titocc.compiler.Scope;
 import titocc.compiler.Symbol;
 import titocc.tokenizer.SyntaxException;
 import titocc.tokenizer.TokenStream;
 import titocc.util.Position;
 
 /**
  * List of parameters for a function type. A parameter list can occur in two different contexts: in
  * function definitions, where parameter names are required, and in function declarations or other
  * type declarators where unnamed parameters are allowed.
  *
  * <p> EBNF definition:
  *
  * <br> PARAMETER_LIST = "(" [PARAMETER {"," PARAMETER}] ")"
  */
 public class ParameterList extends CodeElement
 {
 	/**
 	 * List of parameters.
 	 */
 	private final List<Parameter> parameters;
 
 	/**
 	 * Construcs a ParameterList.
 	 *
 	 * @param parameters list of parameters
 	 * @param position starting position of the parameter list
 	 */
 	public ParameterList(List<Parameter> parameters, Position position)
 	{
 		super(position);
 		this.parameters = parameters;
 	}
 
 	/**
 	 * Returns the parameters.
 	 *
 	 * @return the parameters
 	 */
 	public List<Parameter> getParameters()
 	{
 		return parameters;
 	}
 
 	/**
 	 * Deduces paramter types and declares symbols for them.
 	 *
 	 * @param scope scope in which the parameter list is compiled
 	 * @param functionDefinition true if the parameter list is part of a function definition;
 	 * disallows unnamed parameters and declares them in the current scope instead of creating a
 	 * "temporary" subscope
 	 * @return list of parameter symbols
 	 * @throws SyntaxException if the parameters contain errors
 	 * @throws IOException if assembler throws
 	 */
 	public List<Symbol> compile(Scope scope, boolean functionDefinition)
 			throws SyntaxException, IOException
 	{
 		// For function definitions use the given function scope, otherwise
 		// create a temporary subscope for checking duplicate parameter names.
 		Scope paramScope = scope;
 		if (!functionDefinition) {
 			paramScope = new Scope(scope, "");
 			scope.addSubScope(paramScope);
 		}
 
 		List<Symbol> paramSymbols = new ArrayList<Symbol>();
		int i = 0;
 		for (Parameter p : parameters) {
			Symbol paramSymbol = p.compile(paramScope, functionDefinition, i);
 			paramSymbols.add(paramSymbol);
			++i;
 		}
 
 		return paramSymbols;
 	}
 
 	@Override
 	public String toString()
 	{
 		String str = "(PRM_LIST";
 		for (Parameter p : parameters)
 			str += " " + p;
 		return str + ")";
 	}
 
 	/**
 	 * Attempts to parse a parameter list from token stream. If parsing fails the stream is reset to
 	 * its initial position.
 	 *
 	 * @param tokens source token stream
 	 * @return ParameterList object or null if tokens don't form a valid
 	 * parameter list
 	 */
 	public static ParameterList parse(TokenStream tokens)
 	{
 		Position pos = tokens.getPosition();
 		tokens.pushMark();
 		ParameterList paramList = null;
 
 		if (tokens.read().toString().equals("(")) {
 			List<Parameter> params = new LinkedList<Parameter>();
 			Parameter param = Parameter.parse(tokens);
 			while (param != null) {
 				tokens.pushMark();
 				params.add(param);
 				param = null;
 				if (tokens.read().toString().equals(","))
 					param = Parameter.parse(tokens);
 				tokens.popMark(param == null);
 			}
 
 			if (tokens.read().toString().equals(")"))
 				paramList = new ParameterList(params, pos);
 		}
 
 		tokens.popMark(paramList == null);
 		return paramList;
 	}
 }
