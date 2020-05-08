 package titocc.compiler.elements;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 import titocc.compiler.Assembler;
 import titocc.compiler.InternalSymbol;
 import titocc.compiler.Registers;
 import titocc.compiler.Scope;
 import titocc.compiler.Symbol;
 import titocc.tokenizer.IdentifierToken;
 import titocc.tokenizer.SyntaxException;
 import titocc.tokenizer.Token;
 import titocc.tokenizer.TokenStream;
 
 /**
  * Function declaration and definition. Forward declarations are not currently
  * supported so this is always both declaration and definition. Functions
  * consist of return type, function name, parameter list and a BlockStatement
  * body.
  *
  * <p> EBNF definition:
  *
  * <br> FUNCTION = TYPE_SPECIFIER IDENTIFIER PARAMETER_LIST BLOCK_STATEMENT
  */
 public class Function extends Declaration implements Symbol
 {
 	private TypeSpecifier returnType;
 	private String name;
 	private ParameterList parameterList;
 	private BlockStatement body;
 	private String globallyUniqueName;
 	InternalSymbol retValSymbol, endSymbol;
 
 	/**
 	 * Constructs a Function.
 	 *
 	 * @param returnType return type
 	 * @param name function name
 	 * @param parameterList parameter list
 	 * @param body body of the function
 	 * @param line starting line number of the function
 	 * @param column starting column/character of the function
 	 */
 	public Function(TypeSpecifier returnType, String name, ParameterList parameterList,
 			BlockStatement body, int line, int column)
 	{
 		super(line, column);
 		this.returnType = returnType;
 		this.name = name;
 		this.parameterList = parameterList;
 		this.body = body;
 	}
 
 	/**
 	 * Returns the return type.
 	 *
 	 * @return the return type
 	 */
 	public TypeSpecifier getReturnType()
 	{
 		return returnType;
 	}
 
 	@Override
 	public String getName()
 	{
 		return name;
 	}
 
 	/**
 	 * Returns the parameter list()
 	 *
 	 * @return the parameter list
 	 */
 	public ParameterList getParameterList()
 	{
 		return parameterList;
 	}
 
 	/**
 	 * Returns the function body.
 	 *
 	 * @return the function body
 	 */
 	public BlockStatement getBody()
 	{
 		return body;
 	}
 
 	/**
 	 * Returns the number of parameters.
 	 *
 	 * @return number of parameters
 	 */
 	public int getParameterCount()
 	{
 		return parameterList.getParameters().size();
 	}
 
 	@Override
 	public void compile(Assembler asm, Scope scope, Registers regs)
 			throws IOException, SyntaxException
 	{
 		if (!scope.add(this))
 			throw new SyntaxException("Redefinition of \"" + name + "\".", getLine(), getColumn());
 		globallyUniqueName = scope.makeGloballyUniqueName(name);
 
 		asm.addEmptyLines(1);
 
 		// Create new scope.
 		Scope functionScope = new Scope(scope, name + "_");
 		scope.addSubScope(functionScope);
 
 		addInternalSymbols(functionScope);
 		compileParameters(asm, functionScope);
 
 		// Compile body before prologue because we want to know all the local
 		// variables in the prologue.
 		StringWriter bodyWriter = new StringWriter();
 		Assembler bodyAsm = new Assembler(bodyWriter);
 		compileBody(bodyAsm, functionScope, regs);
 		List<Symbol> localVariables = getLocalVariables(functionScope);
 		bodyAsm.finish();
 
 		compilePrologue(asm, localVariables);
 		asm.getWriter().append(bodyWriter.toString());
 		compileEpilogue(asm, localVariables.size());
 	}
 
 	private void addInternalSymbols(Scope scope)
 	{
 		// Add symbol for the function end so that return statements can jump to it.
 		endSymbol = new InternalSymbol("End", scope, ""); //__End
 		scope.add(endSymbol);
 
 		// Add symbol for location of the return value.
 		retValSymbol = new InternalSymbol("Ret", scope, "(fp)"); //__Ret
 		scope.add(retValSymbol);
 	}
 
 	private void compileParameters(Assembler asm, Scope scope)
 			throws IOException, SyntaxException
 	{
 		// Define constants for return value and parameters and add their symbols.
 		asm.addLabel(retValSymbol.getGlobalName());
 		asm.emit("equ", "-" + (getParameterCount() + 2));
 		parameterList.compile(asm, scope);
 	}
 
 	private void compilePrologue(Assembler asm, List<Symbol> localVariables)
 			throws IOException, SyntaxException
 	{
 		// Define constants for local variables.
 		int varIdx = 0;
 		for (Symbol var : localVariables) {
 			asm.addLabel(var.getGlobalName());
			asm.emit("equ", "" + (1 + varIdx));
 			++varIdx;
 		}
 
 		// Label for function entry point.
 		asm.addLabel(getReference());
 
 		// Allocate stack space for local variables.
 		if (localVariables.size() > 0)
 			asm.emit("add", "sp", "=" + localVariables.size());
 
 		// Push registers.
 		asm.emit("pushr", "sp");
 	}
 
 	private void compileBody(Assembler asm, Scope scope, Registers registers)
 			throws IOException, SyntaxException
 	{
 		// Compile statements directly, so that BlockStatement doesn't create
 		// new scope, and the statements are in the same scope as parameters.
 		for (Statement st : body.getStatements())
 			st.compile(asm, scope, registers);
 	}
 
 	private void compileEpilogue(Assembler asm, int localVariableCount)
 			throws IOException, SyntaxException
 	{
 		// Pop registers from stack.
 		asm.addLabel(endSymbol.getReference());
 		asm.emit("popr", "sp");
 
 		// Remove local variables from stack.
 		if (localVariableCount > 0)
 			asm.emit("sub", "sp", "=" + localVariableCount);
 
 		// Exit from function.
 		asm.emit("exit", "sp", "=" + getParameterCount());
 	}
 
 	private List<Symbol> getLocalVariables(Scope scope)
 	{
 		List<Symbol> localVariables = new ArrayList<Symbol>();
 
 		for (Symbol symbol : scope.getSymbols()) {
 			if (symbol instanceof VariableDeclaration)
 				localVariables.add(symbol);
 		}
 
 		for (Scope subscope : scope.getSubScopes())
 			localVariables.addAll(getLocalVariables(subscope));
 
 		return localVariables;
 	}
 
 	@Override
 	public String getGlobalName()
 	{
 		return globallyUniqueName;
 	}
 
 	@Override
 	public String getReference()
 	{
 		return globallyUniqueName;
 	}
 
 	@Override
 	public String toString()
 	{
 		return "(FUNC " + returnType + " " + name + " " + parameterList
 				+ " " + body + ")";
 	}
 
 	/**
 	 * Attempts to parse a function from token stream. If parsing fails the
 	 * stream is reset to its initial position.
 	 *
 	 * @param tokens source token stream
 	 * @return Function object or null if tokens don't form a valid function
 	 */
 	public static Function parse(TokenStream tokens)
 	{
 		int line = tokens.getLine(), column = tokens.getColumn();
 		tokens.pushMark();
 		Function function = null;
 
 		TypeSpecifier retType = TypeSpecifier.parse(tokens);
 
 		if (retType != null) {
 			Token id = tokens.read();
 			if (id instanceof IdentifierToken) {
 				ParameterList paramList = ParameterList.parse(tokens);
 				if (paramList != null) {
 					BlockStatement body = BlockStatement.parse(tokens);
 					if (body != null) {
 						function = new Function(retType, id.toString(), paramList,
 								body, line, column);
 					}
 				}
 			}
 		}
 
 		tokens.popMark(function == null);
 		return function;
 	}
 }
