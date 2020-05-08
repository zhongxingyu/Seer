 package titocc.compiler.elements;
 
 import java.io.IOException;
 import java.util.Stack;
 import titocc.compiler.Assembler;
 import titocc.compiler.Register;
 import titocc.compiler.Scope;
 import titocc.tokenizer.SyntaxException;
 import titocc.tokenizer.TokenStream;
 
 public class IfStatement extends Statement
 {
 	private Expression test;
 	private Statement trueStatement, elseStatement;
 
 	public IfStatement(Expression test, Statement trueStatement,
 			Statement elseStatement, int line, int column)
 	{
 		super(line, column);
 		this.test = test;
 		this.trueStatement = trueStatement;
 		this.elseStatement = elseStatement;
 	}
 
 	public Expression getTest()
 	{
 		return test;
 	}
 
 	public Statement getTrueStatement()
 	{
 		return trueStatement;
 	}
 
 	public Statement getElseStatement()
 	{
 		return elseStatement;
 	}
 
 	@Override
 	public void compile(Assembler asm, Scope scope, Stack<Register> registers)
 			throws IOException, SyntaxException
 	{
 		// Evaluates and loads the test expression in the first register.
 		test.compile(asm, scope, registers);
 
 		// Skip true statement if test was false.
 		String skipTrueLabel = scope.makeGloballyUniqueName("lbl");
 		asm.emit("jzer", registers.peek().toString(), skipTrueLabel);
 
 		// True statement.
		trueStatement.compile(asm, scope, registers);
 
 		// Else statement.
 		if (elseStatement != null) {
 			String skipElseLabel = scope.makeGloballyUniqueName("lbl");
 			asm.emit("jump", skipElseLabel);
 			asm.addLabel(skipTrueLabel);
			elseStatement.compile(asm, scope, registers);
 			asm.addLabel(skipElseLabel);
 		} else
 			asm.addLabel(skipTrueLabel);
 	}
 
 	@Override
 	public String toString()
 	{
 		return "(IF " + test + " " + trueStatement + " " + elseStatement + ")";
 	}
 
 	public static IfStatement parse(TokenStream tokens)
 	{
 		int line = tokens.getLine(), column = tokens.getColumn();
 		tokens.pushMark();
 		IfStatement ifStatement = null;
 
 		Type retType = Type.parse(tokens);
 
 		if (tokens.read().toString().equals("if")) {
 			if (tokens.read().toString().equals("(")) {
 				Expression test = Expression.parse(tokens);
 				if (test != null) {
 					if (tokens.read().toString().equals(")")) {
 						Statement trueStatement = Statement.parse(tokens);
 						if (trueStatement != null) {
 							Statement elseStatement = parseElseStatement(tokens);
 							ifStatement = new IfStatement(test, trueStatement, elseStatement, line, column);
 						}
 					}
 				}
 			}
 		}
 
 		tokens.popMark(ifStatement == null);
 		return ifStatement;
 	}
 
 	private static Statement parseElseStatement(TokenStream tokens)
 	{
 		int line = tokens.getLine(), column = tokens.getColumn();
 		tokens.pushMark();
 		Statement elseStatement = null;
 
 		if (tokens.read().toString().equals("else"))
 			elseStatement = Statement.parse(tokens);
 
 		tokens.popMark(elseStatement == null);
 		return elseStatement;
 	}
 }
