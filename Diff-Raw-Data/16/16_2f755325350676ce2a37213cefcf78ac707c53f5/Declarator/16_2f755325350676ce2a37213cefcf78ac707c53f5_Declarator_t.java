 package titocc.compiler.elements;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import titocc.compiler.Scope;
 import titocc.compiler.Symbol;
 import titocc.compiler.types.ArrayType;
 import titocc.compiler.types.CType;
 import titocc.compiler.types.FunctionType;
 import titocc.compiler.types.PointerType;
 import titocc.compiler.types.VoidType;
 import titocc.tokenizer.IdentifierToken;
 import titocc.tokenizer.SyntaxException;
 import titocc.tokenizer.Token;
 import titocc.tokenizer.TokenStream;
 import titocc.util.Position;
 
 /**
  * Identifier that is modified with arbitrary pointer/array/function declarators. Note that the
  * declarators are parsed in reverse order. I.e. the innermost declarator becomes the outermost
  * modifier to the type: ((*a)[2])[3] is identifier "a" that is a pointer to a 2-sized array of
  * 3-sized arrays.
  *
  * <br> A declarator can be abstract, in which case it doesn't necessarily specify a name. Unlike in
  * standard's terminology the parser for abstract declarator also matches an empty declarator (for
  * consistency).
  *
  * <br> EBNF definition:
  *
  * <br> DECLARATOR = "*" DECLARATOR | DIRECT_DECLARATOR
  *
  * <br> DIRECT_DECLARATOR = IDENTIFIER | "(" DECLARATOR ")"
  * | DIRECT_DECLARATOR "[" ASSIGNMENT_EXPRESSION "]" | DIRECT_DECLARATOR PARAMETER_LIST
  *
  * <br> ABSTRACT_DECLARATOR = "*" ABSTRACT_DECLARATOR | DIRECT_ABSTRACT_DECLARATOR
  *
  * <br> DIRECT_ABSTRACT_DECLARATOR = [IDENTIFIER] | "(" ABSTRACT_DECLARATOR ")"
  * | [DIRECT_ABSTRACT_DECLARATOR] "[" ASSIGNMENT_EXPRESSION "]"
  * | [DIRECT_ABSTRACT_DECLARATOR] PARAMETER_LIST
  */
 public abstract class Declarator extends CodeElement
 {
 	/**
 	 * Declarator that is just a simple identifier. When name is null then it is an abstract
 	 * declarator without a name.
 	 */
 	private static class IdentifierDeclarator extends Declarator
 	{
 		private final String name;
 
 		public IdentifierDeclarator(String name, Position position)
 		{
 			super(null, position);
 			this.name = name;
 		}
 
 		@Override
 		public String getName()
 		{
 			return name;
 		}
 
 		@Override
 		public CType compile(CType type, Scope scope, List<Symbol> paramSymbolsOut)
 		{
 			return type;
 		}
 
 		@Override
 		public String toString()
 		{
 			return "(DCLTOR " + name + ")";
 		}
 	}
 
 	/**
 	 * Declarator that declares an array.
 	 */
 	private static class ArrayDeclarator extends Declarator
 	{
 		private final Expression arrayLength;
 
 		public ArrayDeclarator(Declarator subDeclarator, Expression arrayLength, Position position)
 		{
 			super(subDeclarator, position);
 			this.arrayLength = arrayLength;
 		}
 
 		@Override
 		public CType compile(CType type, Scope scope, List<Symbol> paramSymbolsOut)
 				throws SyntaxException, IOException
 		{
 			if (!type.isObject())
 				throw new SyntaxException("Array elements must have object type.", getPosition());
 
 			Integer len = arrayLength.getCompileTimeValue();
 			if (len == null) {
 				throw new SyntaxException("Array length must be a compile time constant.",
 						getPosition());
 			} else if (len <= 0) {
 				throw new SyntaxException("Array length must be a positive integer.",
 						getPosition());
 			}
 
 			return subDeclarator.compile(new ArrayType(type, len), scope, paramSymbolsOut);
 		}
 
 		@Override
 		public String toString()
 		{
 			return "(DCLTOR " + subDeclarator + " " + arrayLength + ")";
 		}
 	}
 
 	/**
 	 * Declarator that declares a pointer.
 	 */
 	private static class PointerDeclarator extends Declarator
 	{
 		public PointerDeclarator(Declarator subDeclarator, Position position)
 		{
 			super(subDeclarator, position);
 		}
 
 		@Override
 		public CType compile(CType type, Scope scope, List<Symbol> paramSymbolsOut)
 				throws SyntaxException, IOException
 		{
 			return subDeclarator.compile(new PointerType(type), scope, paramSymbolsOut);
 		}
 
 		@Override
 		public String toString()
 		{
 			return "(DCLTOR " + subDeclarator + ")";
 		}
 	}
 
 	/**
 	 * Declarator that declares a function.
 	 */
 	private static class FunctionDeclarator extends Declarator
 	{
 		private final ParameterList paramList;
 
 		public FunctionDeclarator(Declarator subDeclarator, ParameterList paramList,
 				Position position)
 		{
 			super(subDeclarator, position);
 			this.paramList = paramList;
 		}
 
 		@Override
 		public CType compile(CType type, Scope scope, List<Symbol> paramSymbolsOut)
 				throws SyntaxException, IOException
 		{
			// Only recognize the innermost function declarator as function definition, in case
			// there are several (e.g. a function returning a function pointer).
			boolean funcDefn = paramSymbolsOut != null
					&& subDeclarator instanceof IdentifierDeclarator;
 
 			checkReturnType(type);
 
 			// Compile the parameter list.
 			List<Symbol> paramSymbols = paramList.compile(scope, funcDefn);
 			List<CType> paramTypes = new ArrayList<CType>();
 			for (Symbol sym : paramSymbols)
 				paramTypes.add(sym.getType());
 
			// Only add parameter symbols if this is a declarator corresponding to a function
			// definition.
			if (funcDefn)
 				paramSymbolsOut.addAll(paramSymbols);
 
 			return subDeclarator.compile(new FunctionType(type, paramTypes), scope,
 					paramSymbolsOut);
 		}
 
 		private void checkReturnType(CType retType) throws SyntaxException
 		{
 			// Return type must be void or object type, excluding arrays ($6.9.1/3).
 			if (retType instanceof VoidType)
 				return;
 			if (retType.isObject() && !(retType instanceof ArrayType))
 				return;
 
 			throw new SyntaxException("Invalid function return type. Void or"
 					+ " non-array object type required.", getPosition());
 		}
 
 		@Override
 		public String toString()
 		{
 			return "(DCLTOR " + subDeclarator + " " + paramList + ")";
 		}
 	}
 
 	protected final Declarator subDeclarator;
 
 	/**
 	 * Constructs a Declarator.
 	 *
 	 * @param subDeclarator next declarator inside this one or null if there aren't any
 	 * @param position starting position of the declarator
 	 */
 	protected Declarator(Declarator subDeclarator, Position position)
 	{
 		super(position);
 		this.subDeclarator = subDeclarator;
 	}
 
 	/**
 	 * Returns the variable name for this declarator.
 	 *
 	 * @return the variable name or null if abstract declarator
 	 */
 	public String getName()
 	{
 		return subDeclarator.getName();
 	}
 
 	/**
 	 * Does semantic analysis for the declarator and deduces the resulting type. Modifies the
 	 * argument type with this declarator and all its subdeclarators. For example given type "int",
 	 * two nested 2-sized array declarators would return int[2][2]. For a function declarators used
 	 * in function definition also generates code for parameters.
 	 *
 	 * @param type type to be modified
 	 * @param scope scope in which the declarator is compiled
 	 * @param paramSymbolsOut if the declarator is part of a function definition, the parameter
 	 * symbols are added in this list
 	 * @return modified type
 	 * @throws SyntaxException
 	 */
 	public abstract CType compile(CType type, Scope scope, List<Symbol> paramSymbolsOut)
 			throws SyntaxException, IOException;
 
 	/**
 	 * Attempts to parse a declarator from token stream. If parsing fails the stream is reset to its
 	 * initial position.
 	 *
 	 * @param tokens source token stream
 	 * @param allowAbstract allows the declarator to be abstract, i.e. without name
 	 * @return Declarator object or null if tokens don't form a valid declarator
 	 */
 	public static Declarator parse(TokenStream tokens, boolean allowAbstract)
 	{
 		Position pos = tokens.getPosition();
 		tokens.pushMark();
 		Declarator declarator = null;
 
 		if (tokens.read().toString().equals("*")) {
 			declarator = Declarator.parse(tokens, allowAbstract);
 			if (declarator != null)
 				declarator = new PointerDeclarator(declarator, pos);
 		}
 
 		tokens.popMark(declarator == null);
 
 		if (declarator == null)
 			declarator = parseDirectDeclarator(tokens, allowAbstract);
 
 		return declarator;
 	}
 
 	/**
 	 * Parses a direct declarator. It is either a variable name, declarator inside () parentheses,
 	 * an array declarator or a function declarator. declarator inside () parentheses.
 	 */
 	private static Declarator parseDirectDeclarator(TokenStream tokens, boolean allowAbstract)
 	{
 		Position pos = tokens.getPosition();
 
 		// Identifier declarator.
 		Declarator declarator = parseIdentifierDeclarator(tokens);
 
 		// Parenthesized declarator.
 		if (declarator == null)
 			declarator = parseParenthesizedDeclarator(tokens, allowAbstract);
 
 		// Null/abstract declarator.
 		if (declarator == null && allowAbstract)
 			declarator = new IdentifierDeclarator(null, pos);
 
 		// Parse array/function declarators in loop to avoid left recursion.
 		if (declarator != null) {
 			while (true) {
 				// Array declarator.
 				Expression arrayLength = parseArrayLength(tokens);
 				if (arrayLength != null) {
 					declarator = new ArrayDeclarator(declarator, arrayLength, pos);
 					continue;
 				}
 
 				// Function declarator.
 				ParameterList paramList = ParameterList.parse(tokens);
 				if (paramList != null) {
 					declarator = new FunctionDeclarator(declarator, paramList, pos);
 					continue;
 				}
 
 				break;
 			}
 		}
 
 		return declarator;
 	}
 
 	/**
 	 * Parses an identifier declarator.
 	 */
 	private static Declarator parseIdentifierDeclarator(TokenStream tokens)
 	{
 		Declarator declarator = null;
 		tokens.pushMark();
 
 		Token id = tokens.read();
 		if (id instanceof IdentifierToken)
 			declarator = new IdentifierDeclarator(id.toString(), id.getPosition());
 
 		tokens.popMark(declarator == null);
 		return declarator;
 	}
 
 	/**
 	 * Parses a parenthesized declarator.
 	 */
 	private static Declarator parseParenthesizedDeclarator(TokenStream tokens,
 			boolean allowAbstract)
 	{
 		Declarator declarator = null;
 		tokens.pushMark();
 
 		if (tokens.read().toString().equals("(")) {
 			declarator = Declarator.parse(tokens, allowAbstract);
 			if (declarator != null && !tokens.read().toString().equals(")"))
 				declarator = null;
 		}
 
 		tokens.popMark(declarator == null);
 		return declarator;
 	}
 
 	/**
 	 * Parses the array length of an array declarator.
 	 */
 	private static Expression parseArrayLength(TokenStream tokens)
 	{
 		tokens.pushMark();
 		Expression subscript = null;
 
 		if (tokens.read().toString().equals("[")) {
 			subscript = AssignmentExpression.parse(tokens);
 			if (subscript != null && !tokens.read().toString().equals("]"))
 				subscript = null;
 		}
 
 		tokens.popMark(subscript == null);
 		return subscript;
 	}
 }
