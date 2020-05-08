 package titocc.compiler.elements;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Stack;
 import titocc.compiler.Assembler;
 import titocc.compiler.Register;
 import titocc.compiler.Scope;
 import titocc.tokenizer.SyntaxException;
 import titocc.tokenizer.TokenStream;
 
 public class AssignmentExpression extends Expression
 {
 	private enum Type
 	{
 		SIMPLE, SYMMETRIC, ASYMMETRIC
 	};
 
 	private static class Operator
 	{
 		public String mnemonic;
 		public Type type;
 
 		public Operator(String mnemonic, Type type)
 		{
 			this.mnemonic = mnemonic;
 			this.type = type;
 		}
 	}
 	static final Map<String, Operator> assignmentOperators = new HashMap<String, Operator>()
 	{
 		{
 			put("=", new Operator("", Type.SIMPLE));
 			put("+=", new Operator("add", Type.SYMMETRIC));
 			put("*=", new Operator("mul", Type.SYMMETRIC));
 			put("&=", new Operator("and", Type.SYMMETRIC));
 			put("|=", new Operator("or", Type.SYMMETRIC));
 			put("^=", new Operator("xor", Type.SYMMETRIC));
 			put("-=", new Operator("sub", Type.ASYMMETRIC));
 			put("/=", new Operator("div", Type.ASYMMETRIC));
 			put("%=", new Operator("mod", Type.ASYMMETRIC));
 			put("<<=", new Operator("shl", Type.ASYMMETRIC));
 			put(">>=", new Operator("shr", Type.ASYMMETRIC));
 		}
 	};
 	private Operator operator;
	private String operatorString;
 	private Expression left, right;
 
 	public AssignmentExpression(String operator, Expression left,
 			Expression right, int line, int column)
 	{
 		super(line, column);
		this.operatorString = operator;
 		this.operator = assignmentOperators.get(operator);
 		this.left = left;
 		this.right = right;
 	}
 
 	public Expression getLeft()
 	{
 		return left;
 	}
 
 	public Expression getRight()
 	{
 		return right;
 	}
 
 	@Override
 	public void compile(Assembler asm, Scope scope, Stack<Register> registers)
 			throws SyntaxException, IOException
 	{
 		// Compile LHS and get reference to the variable.
 		String leftRef = compileLeft(asm, scope, registers);
 
 		// Compile RHS and the assignment operation.
 		if (operator.type == Type.SIMPLE)
 			compileSimple(asm, scope, registers, leftRef);
 		else if (operator.type == Type.SYMMETRIC)
 			compileSymmetric(asm, scope, registers, leftRef);
 		else
 			compileAsymmetric(asm, scope, registers, leftRef);
 	}
 
 	private String compileLeft(Assembler asm, Scope scope, Stack<Register> registers)
 			throws SyntaxException, IOException
 	{
 		// Currently assumes that the return value can always be reduced to
 		// variable name, so we can do a simple compile time evaluation. This
 		// will have to be changed if arrays, pointers and indirection operator
 		// are implemented.
 		String leftRef = left.getLvalueReference(scope);
 		if (leftRef == null)
 			throw new SyntaxException("Left side cannot be assigned to.", getLine(), getColumn());
		//left.compile(asm, scope, registers);
 		return leftRef;
 	}
 
 	private void compileSimple(Assembler asm, Scope scope, Stack<Register> registers,
 			String leftRef) throws SyntaxException, IOException
 	{
 		// Load RHS in the first register and stores it in the LHS variable.
 		right.compile(asm, scope, registers);
 		asm.emit("store", registers.peek().toString(), leftRef);
 	}
 
 	private void compileSymmetric(Assembler asm, Scope scope, Stack<Register> registers,
 			String leftRef) throws SyntaxException, IOException
 	{
 		// Load RHS in first register.
 		right.compile(asm, scope, registers);
 
 		// Because the operation is symmetric, we can use the left operand
 		// as the right operand in the assembly instruction, saving one register.
 		asm.emit(operator.mnemonic, registers.peek().toString(), leftRef);
 		asm.emit("store", registers.peek().toString(), leftRef);
 	}
 
 	private void compileAsymmetric(Assembler asm, Scope scope, Stack<Register> registers,
 			String leftRef) throws SyntaxException, IOException
 	{
 		// Make sure there are two registers available.
 		Register pushedRegister = pushRegister(asm, registers);
 
 		// Load RHS in first register.
 		right.compile(asm, scope, registers);
 		Register rightRegister = registers.pop();
 
 		// Load LHS in second register and operate on it.
 		asm.emit("load", registers.peek().toString(), leftRef);
 		asm.emit(operator.mnemonic, registers.peek().toString(), rightRegister.toString());
 
 		// Store result to LHS variable.
 		asm.emit("store", registers.peek().toString(), leftRef);
 
 		registers.push(rightRegister);
 
 		// Pop register if one was pushed to stack.
 		popRegister(asm, registers, pushedRegister);
 	}
 
 	@Override
 	public String toString()
 	{
		return "(ASGN_EXPR " + operatorString + " " + left + " " + right + ")";
 	}
 
 	public static Expression parse(TokenStream tokens)
 	{
 		int line = tokens.getLine(), column = tokens.getColumn();
 		tokens.pushMark();
 		Expression expr = BinaryExpression.parse(tokens);
 
 		if (expr != null) {
 			tokens.pushMark();
 			Expression right = null;
 			String op = tokens.read().toString();
 			if (assignmentOperators.containsKey(op))
 				right = AssignmentExpression.parse(tokens);
 
 			tokens.popMark(right == null);
 			if (right != null)
 				expr = new AssignmentExpression(op, expr, right, line, column);
 		}
 
 		tokens.popMark(expr == null);
 		return expr;
 	}
 }
