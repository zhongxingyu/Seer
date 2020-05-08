 package titocc.compiler.elements;
 
 import java.util.HashMap;
 import java.util.Map;
 import titocc.compiler.IntermediateCompiler;
 import titocc.compiler.Lvalue;
 import titocc.compiler.Rvalue;
 import titocc.compiler.Scope;
 import titocc.compiler.VirtualRegister;
 import titocc.compiler.types.CType;
 import titocc.compiler.types.PointerType;
 import titocc.tokenizer.SyntaxException;
 import titocc.tokenizer.TokenStream;
 import titocc.util.Position;
 
 /**
  * Expression formed by any of the assignment operators and two operands.
  *
  * <p> EBNF definition:
  *
  * <br> ASSIGNMENT_EXPRESSION = BINARY_EXPRESSION [ASSIGNMENT_OPERATOR ASSIGNMENT_EXPRESSION]
  *
  * <br> ASSIGNMENT_OPERATOR = "=" | "+=" | "*=" | "&=" | "|=" | "^=" | "-=" | "/=" | "%=" | "<<=" |
  * ">>="
  */
 public class AssignmentExpression extends Expression
 {
 	private enum Commutativity
 	{
 		SIMPLE, COMMUTATIVE, NONCOMMUTATIVE
 	};
 
 	/**
 	 * Assignment operator with operator mnemonic and operation type and corresponding binary
 	 * operator.
 	 */
 	private static class Operator
 	{
 		final String mnemonic;
 
 		final Commutativity commutativity;
 
 		final BinaryExpression.Type type;
 
 		final String binaryOperator;
 
 		Operator(String mnemonic, Commutativity commutativity, String binaryOperator)
 		{
 			this.mnemonic = mnemonic;
 			this.commutativity = commutativity;
 			this.binaryOperator = binaryOperator;
 			if (binaryOperator.isEmpty())
 				this.type = null;
 			else
 				this.type = BinaryExpression.binaryOperators.get(binaryOperator).type;
 		}
 	}
 
 	/**
 	 * Map of assignment operators.
 	 */
 	static final Map<String, Operator> assignmentOperators = new HashMap<String, Operator>()
 	{
 		{
 			put("=", new Operator("", Commutativity.SIMPLE, ""));
 			put("+=", new Operator("add", Commutativity.COMMUTATIVE, "+"));
 			put("*=", new Operator("mul", Commutativity.COMMUTATIVE, "*"));
 			put("&=", new Operator("and", Commutativity.COMMUTATIVE, "&"));
 			put("|=", new Operator("or", Commutativity.COMMUTATIVE, "|"));
 			put("^=", new Operator("xor", Commutativity.COMMUTATIVE, "^"));
 			put("-=", new Operator("sub", Commutativity.NONCOMMUTATIVE, "-"));
 			put("/=", new Operator("div", Commutativity.NONCOMMUTATIVE, "/"));
 			put("%=", new Operator("mod", Commutativity.NONCOMMUTATIVE, "%"));
 			put("<<=", new Operator("shl", Commutativity.NONCOMMUTATIVE, "<<"));
 			put(">>=", new Operator("shr", Commutativity.NONCOMMUTATIVE, ">>"));
 		}
 	};
 
 	/**
 	 * Assignment operator.
 	 */
 	private final Operator operator;
 
 	/**
 	 * String representation of the operator.
 	 */
 	private final String operatorString;
 
 	/**
 	 * Left hand side expression.
 	 */
 	private final Expression left;
 
 	/**
 	 * Right hand side expression.
 	 */
 	private final Expression right;
 
 	/**
 	 * Constructs a new AssignmentExpression
 	 *
 	 * @param operator string representation of the operator
 	 * @param left left operand
 	 * @param right right operand
 	 */
 	public AssignmentExpression(String operator, Expression left,
 			Expression right, Position position)
 	{
 		super(position);
 		this.operatorString = operator;
 		this.operator = assignmentOperators.get(operator);
 		this.left = left;
 		this.right = right;
 	}
 
 	/**
 	 * Returns the left operand.
 	 *
 	 * @return left operand
 	 */
 	public Expression getLeft()
 	{
 		return left;
 	}
 
 	/**
 	 * Returns the right operand.
 	 *
 	 * @return right operand
 	 */
 	public Expression getRight()
 	{
 		return right;
 	}
 
 	@Override
 	public Rvalue compile(IntermediateCompiler ic, Scope scope) throws SyntaxException
 	{
 		checkTypes(scope);
 
 		if (operator.commutativity == Commutativity.SIMPLE)
 			return compileSimpleAssignment(ic, scope);
 		else
 			return compileCompoundAssignment(ic, scope);
 	}
 
 	private Rvalue compileSimpleAssignment(IntermediateCompiler ic, Scope scope)
 			throws SyntaxException
 	{
 		Rvalue rhs = right.compileWithConversion(ic, scope, left.getType(scope).decay());
 		Lvalue lhs = left.compileAsLvalue(ic, scope, false);
 		ic.emit("store", rhs.getRegister(), "0", lhs.getRegister());
 		return rhs;
 	}
 
 	private Rvalue compileCompoundAssignment(IntermediateCompiler ic, Scope scope)
 			throws SyntaxException
 	{
 		CType leftType, rightType;
 		if (operator.type == BinaryExpression.Type.SHIFT) {
 			leftType = left.getType(scope).decay().promote();
 			rightType = CType.INT;
 		} else if (left.getType(scope).decay().isPointer()) {
 			leftType = new PointerType(CType.CHAR);
 			rightType = CType.PTRDIFF_T;
 		} else {
 			leftType = rightType = CType.getCommonType(left.getType(scope).decay(),
 					right.getType(scope).decay());
 		}
 
 		return compileImpl(ic, scope, leftType, rightType);
 	}
 
 	private Rvalue compileImpl(IntermediateCompiler ic, Scope scope,
 			CType leftType, CType rightType) throws SyntaxException
 	{
 		// Compile operands.
 		Rvalue rhs = right.compileWithConversion(ic, scope, rightType);
 		Lvalue lhs = left.compileAsLvalue(ic, scope, false);
 
 		// Load LHS to register.
 		Rvalue lhsVal = new Rvalue(new VirtualRegister());
 		ic.emit("load", lhsVal.getRegister(), "0", lhs.getRegister());
 		lhsVal = left.getType(scope).compileConversion(ic, scope, lhsVal, leftType);
 
 		// Compile the binary operator.
 		String binOp = operator.binaryOperator;
 		Rvalue retVal;
 		if (leftType.isPointer()) {
 			// Scale integer operand if necessary.
 			int leftIncrSize = left.getType(scope).decay().getIncrementSize();
 			if (leftIncrSize > 1)
 				ic.emit("mul", rhs.getRegister(), "=" + leftIncrSize);
 			ic.emit(operator.mnemonic, lhsVal.getRegister(), rhs.getRegister());
 			retVal = lhsVal;
 		} else if (operator.type == BinaryExpression.Type.BITWISE)
 			retVal = leftType.compileBinaryBitwiseOperator(ic, scope, lhsVal, rhs, binOp);
 		else if (operator.type == BinaryExpression.Type.SHIFT)
			retVal = leftType.compileBinaryBitwiseOperator(ic, scope, lhsVal, rhs, binOp);
 		else //if (operator.type == BinaryExpression.Type.ARITHMETIC)
 			retVal = leftType.compileBinaryArithmeticOperator(ic, scope, lhsVal, rhs, binOp);
 
 		// Convert to the original type of the left operand.
 		retVal = leftType.compileConversion(ic, scope, retVal, left.getType(scope));
 
 		// Assign result back to lvalue.
 		ic.emit("store", retVal.getRegister(), "0", lhs.getRegister());
 
 		return retVal;
 	}
 
 	private void checkTypes(Scope scope) throws SyntaxException
 	{
 		CType leftType = left.getType(scope).decay();
 		CType rightType = right.getType(scope).decay();
 
 		// Compound assignment rules defined in ($6.5.16.2).
 		if (operatorString.equals("=")) {
 			if (right.isAssignableTo(leftType, scope))
 				return;
 		} else if (operatorString.equals("+=") || operatorString.equals("-=")) {
 			if (leftType.dereference().isObject() && rightType.isInteger())
 				return;
 			if (leftType.isArithmetic() && rightType.isArithmetic())
 				return;
 		} else if (operatorString.equals("&=") || operatorString.equals("|=")
 				|| operatorString.equals("^=")) {
 			if (leftType.isInteger() && rightType.isInteger())
 				return;
 		} else if (operatorString.equals("%=")) {
 			if (leftType.isArithmetic() && rightType.isInteger())
 				return;
 		} else {
 			if (leftType.isArithmetic() && rightType.isArithmetic())
 				return;
 		}
 
 		throw new SyntaxException("Incompatible operands for operator " + operatorString + ".",
 				getPosition());
 	}
 
 	@Override
 	public CType getType(Scope scope) throws SyntaxException
 	{
 		return left.getType(scope);
 	}
 
 	@Override
 	public String toString()
 	{
 		return "(ASGN_EXPR " + operatorString + " " + left + " " + right + ")";
 	}
 
 	/**
 	 * Attempts to parse a syntactic assignment expression from token stream. If parsing fails the
 	 * stream is reset to its initial position.
 	 *
 	 * @param tokens source token stream
 	 * @return Expression object or null if tokens don't form a valid expression
 	 */
 	public static Expression parse(TokenStream tokens)
 	{
 		Position pos = tokens.getPosition();
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
 				expr = new AssignmentExpression(op, expr, right, pos);
 		}
 
 		tokens.popMark(expr == null);
 		return expr;
 	}
 }
