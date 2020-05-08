 package titocc.compiler.elements;
 
 import java.io.IOException;
 import java.util.Stack;
 import titocc.compiler.Assembler;
 import titocc.compiler.Register;
 import titocc.compiler.Scope;
 import titocc.tokenizer.SyntaxException;
 import titocc.tokenizer.TokenStream;
 
 public class ExpressionStatement extends Statement
 {
 	private Expression expression;
 
 	public ExpressionStatement(Expression expression, int line, int column)
 	{
 		super(line, column);
 		this.expression = expression;
 	}
 
 	public Expression expression()
 	{
 		return expression;
 	}
 
 	@Override
 	public void compile(Assembler asm, Scope scope, Stack<Register> registers)
 			throws IOException, SyntaxException
 	{
		expression.compileAsVoid(asm, scope, registers);
 	}
 
 	@Override
 	public String toString()
 	{
 		return "(EXPR_ST " + expression + ")";
 	}
 
 	public static ExpressionStatement parse(TokenStream tokens)
 	{
 		int line = tokens.getLine(), column = tokens.getColumn();
 		tokens.pushMark();
 		ExpressionStatement exprStatement = null;
 
 		Expression expr = Expression.parse(tokens);
 		if (expr != null)
 			if (tokens.read().toString().equals(";"))
 				exprStatement = new ExpressionStatement(expr, line, column);
 
 		tokens.popMark(exprStatement == null);
 		return exprStatement;
 	}
 }
