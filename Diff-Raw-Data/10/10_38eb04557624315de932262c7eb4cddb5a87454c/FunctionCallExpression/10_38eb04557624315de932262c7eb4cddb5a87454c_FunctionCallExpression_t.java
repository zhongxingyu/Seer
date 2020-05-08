 package titocc.compiler.elements;
 
 import java.io.IOException;
 import java.util.Stack;
 import titocc.compiler.Assembler;
 import titocc.compiler.Register;
 import titocc.compiler.Scope;
 import titocc.tokenizer.SyntaxException;
 import titocc.tokenizer.TokenStream;
 
 /**
  * Function call expression.
  */
 public class FunctionCallExpression extends Expression
 {
 	private Expression function;
 	private ArgumentList argumentList;
 
 	public FunctionCallExpression(Expression function, ArgumentList argumentList,
 			int line, int column)
 	{
 		super(line, column);
 		this.function = function;
 		this.argumentList = argumentList;
 	}
 
 	public Expression getFunction()
 	{
 		return function;
 	}
 
 	public ArgumentList getArgumentList()
 	{
 		return argumentList;
 	}
 
 	@Override
 	public void compile(Assembler asm, Scope scope, Stack<Register> registers)
 			throws SyntaxException, IOException
 	{
 		compile(asm, scope, registers, true);
 	}
 
 	@Override
 	public void compileAsVoid(Assembler asm, Scope scope, Stack<Register> registers)
 			throws SyntaxException, IOException
 	{
 		compile(asm, scope, registers, false);
 	}
 
 	private void compile(Assembler asm, Scope scope, Stack<Register> registers,
 			boolean returnValueRequired) throws SyntaxException, IOException
 	{
 		Function func = validateFunction(scope);
 
		if (func.getReturnType().getName().equals("void")) {
			if (returnValueRequired)
 				throw new SyntaxException("Void return value used in an expression.", getLine(), getColumn());
		} else {
 			// Reserve space for return value.
 			asm.emit("add", "sp", "=1");
 		}
 
 		// Push arguments to stack.
 		argumentList.compile(asm, scope, registers);
 
 		// Make the call.
 		asm.emit("call", "sp", func.getReference());
 
 		// Read the return value.
		if (!func.getReturnType().getName().equals("void"))
 			asm.emit("pop", "sp", registers.peek().toString());
 	}
 
 	private Function validateFunction(Scope scope) throws SyntaxException
 	{
 		Function func = function.getFunction(scope);
 		if (func == null)
 			throw new SyntaxException("Expression is not a function.", getLine(), getColumn());
 		if (func.getParameterCount() != argumentList.getArguments().size())
 			throw new SyntaxException("Number of arguments doesn't match the number of parameters.", getLine(), getColumn());
 		return func;
 	}
 
 	@Override
 	public String toString()
 	{
 		return "(FCALL_EXPR " + function + " " + argumentList + ")";
 	}
 
 	public static FunctionCallExpression parse(Expression firstOperand, TokenStream tokens)
 	{
 		FunctionCallExpression expr = null;
 
 		ArgumentList argList = ArgumentList.parse(tokens);
 		if (argList != null) {
 			expr = new FunctionCallExpression(firstOperand, argList,
 					firstOperand.getLine(), firstOperand.getColumn());
 		}
 
 		return expr;
 	}
 }
