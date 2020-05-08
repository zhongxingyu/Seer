 package titocc.compiler.elements;
 
 import java.util.HashMap;
 import java.util.Map;
 import titocc.compiler.IntermediateCompiler;
 import titocc.compiler.Rvalue;
 import titocc.compiler.Scope;
 import titocc.compiler.VirtualRegister;
 import titocc.compiler.types.CType;
 import titocc.compiler.types.PointerType;
 import titocc.compiler.types.VoidType;
 import titocc.tokenizer.SyntaxException;
 import titocc.tokenizer.TokenStream;
 import titocc.util.Position;
 
 /**
  * Expression formed by a binary operator and two operands.
  *
  * <p> EBNF Definition:
  *
  * <br> BINARY_EXPRESSION = [BINARY_EXPRESSION "||"] BINARY_EXPRESSION2
  *
  * <br> BINARY_EXPRESSION2 = [BINARY_EXPRESSION2 "&&"] BINARY_EXPRESSION3
  *
  * <br> BINARY_EXPRESSION3 = [BINARY_EXPRESSION3 "|"] BINARY_EXPRESSION4
  *
  * <br> BINARY_EXPRESSION4 = [BINARY_EXPRESSION4 "^"] BINARY_EXPRESSION5
  *
  * <br> BINARY_EXPRESSION5 = [BINARY_EXPRESSION5 "&"] BINARY_EXPRESSION6
  *
  * <br> BINARY_EXPRESSION6 = [BINARY_EXPRESSION6 "=="] BINARY_EXPRESSION7
  *
  * <br> BINARY_EXPRESSION7 = [BINARY_EXPRESSION7 "!=") BINARY_EXPRESSION8
  *
  * <br> BINARY_EXPRESSION8 = [BINARY_EXPRESSION8 ("<" | "<=" | ">" | ">=")] BINARY_EXPRESSION9
  *
  * <br> BINARY_EXPRESSION9 = [BINARY_EXPRESSION9 ("<<" | ">>")] BINARY_EXPRESSION10
  *
  * <br> BINARY_EXPRESSION10 = [BINARY_EXPRESSION10 ("+" | "-")] BINARY_EXPRESSION11
  *
  * <br> BINARY_EXPRESSION11 = [BINARY_EXPRESSION11 ("*" | "/" | "%")] PREFIX_EXPRESSION
  */
 public class BinaryExpression extends Expression
 {
 	enum Type
 	{
 		BITWISE, ARITHMETIC, EQUALITY, RELATIONAL, SHIFT, LOGICAL
 	};
 
 	/**
 	 * Binary operator with mnemonic and operation type.
 	 */
 	static class Operator
 	{
 		String mnemonic;
 
 		Type type;
 
 		int priority;
 
 		public Operator(String mnemonic, Type type, int priority)
 		{
 			this.mnemonic = mnemonic;
 			this.type = type;
 			this.priority = priority;
 		}
 	}
 
 	/**
 	 * Binary operators, their main instructions and priorities.
 	 */
 	static final Map<String, Operator> binaryOperators = new HashMap<String, Operator>()
 	{
 		{
 			put("||", new Operator("jnzer", Type.LOGICAL, 1));
 			put("&&", new Operator("jzer", Type.LOGICAL, 2));
 			put("|", new Operator("or", Type.BITWISE, 3));
 			put("^", new Operator("xor", Type.BITWISE, 4));
 			put("&", new Operator("and", Type.BITWISE, 5));
 			put("==", new Operator("jequ", Type.EQUALITY, 6));
 			put("!=", new Operator("jnequ", Type.EQUALITY, 7));
 			put("<", new Operator("jles", Type.RELATIONAL, 8));
 			put("<=", new Operator("jngre", Type.RELATIONAL, 8));
 			put(">", new Operator("jgre", Type.RELATIONAL, 8));
 			put(">=", new Operator("jnles", Type.RELATIONAL, 8));
 			put("<<", new Operator("shl", Type.SHIFT, 9));
 			put(">>", new Operator("shr", Type.SHIFT, 9));
 			put("+", new Operator("add", Type.ARITHMETIC, 10));
 			put("-", new Operator("sub", Type.ARITHMETIC, 10));
 			put("*", new Operator("mul", Type.ARITHMETIC, 11));
 			put("/", new Operator("div", Type.ARITHMETIC, 11));
 			put("%", new Operator("mod", Type.ARITHMETIC, 11));
 		}
 	};
 
 	/**
 	 * Binary operator as a string.
 	 */
 	private final String operator;
 
 	/**
 	 * Left hand side expression;
 	 */
 	private final Expression left;
 
 	/**
 	 * Right hand side expression.
 	 */
 	private final Expression right;
 
 	/**
 	 * Constructs a BinaryExpression.
 	 *
 	 * @param operator operator as string
 	 * @param left left operand
 	 * @param right right operand
 	 * @param position starting position of the binary expression
 	 */
 	public BinaryExpression(String operator, Expression left, Expression right,
 			Position position)
 	{
 		super(position);
 		this.operator = operator;
 		this.left = left;
 		this.right = right;
 	}
 
 	/**
 	 * Returns the operator.
 	 *
 	 * @return the operator
 	 */
 	public String getOperator()
 	{
 		return operator;
 	}
 
 	/**
 	 * Returns the left operand.
 	 *
 	 * @return the left operand
 	 */
 	public Expression getLeft()
 	{
 		return left;
 	}
 
 	/**
 	 * Returns the right operand
 	 *
 	 * @return the right operand
 	 */
 	public Expression getRight()
 	{
 		return right;
 	}
 
 	@Override
 	public Rvalue compile(IntermediateCompiler ic, Scope scope) throws SyntaxException
 	{
 		checkTypes(scope);
 
 		Type opType = binaryOperators.get(operator).type;
 		if (opType == Type.LOGICAL)
 			return compileLogicalOperator(ic, scope);
 		else if (opType == Type.BITWISE)
 			return compileBitwiseOperator(ic, scope);
 		else if (opType == Type.RELATIONAL || opType == Type.EQUALITY)
 			return compileComparisonOperator(ic, scope);
 		else if (opType == Type.SHIFT)
 			return compileShiftOperator(ic, scope);
 		else //if (opType == Type.ARITHMETIC)
 			return compileArithmeticOperator(ic, scope);
 	}
 
 	private CType getCommonTypeIfArithmetic(Scope scope) throws SyntaxException
 	{
 		if (left.getType(scope).decay().isArithmetic()
 				&& right.getType(scope).decay().isArithmetic())
 			return CType.getCommonType(left.getType(scope).decay(), right.getType(scope).decay());
 		else
 			return null;
 	}
 
 	private Rvalue compileLogicalOperator(IntermediateCompiler ic, Scope scope)
 			throws SyntaxException
 	{
 		Rvalue lhs = left.compileWithConversion(ic, scope, CType.BOOLISH);
 		// Short circuit evaluation; only evaluate RHS if necessary.
 		String jumpLabel = scope.makeGloballyUniqueName("lbl");
 		String jumpLabel2 = scope.makeGloballyUniqueName("lbl");
 		ic.emit(binaryOperators.get(operator).mnemonic, lhs.getRegister(), jumpLabel);
 		Rvalue rhs = right.compileWithConversion(ic, scope, CType.BOOLISH);
 		ic.emit(binaryOperators.get(operator).mnemonic, rhs.getRegister(), jumpLabel);
 		ic.emit("load", lhs.getRegister(), operator.equals("||") ? "=0" : "=1");
 		ic.emit("jump", VirtualRegister.NONE, jumpLabel2);
 		ic.addLabel(jumpLabel);
 		ic.emit("load", lhs.getRegister(), operator.equals("||") ? "=1" : "=0");
 		ic.addLabel(jumpLabel2);
 		return lhs;
 	}
 
 	private Rvalue compileBitwiseOperator(IntermediateCompiler ic, Scope scope)
 			throws SyntaxException
 	{
 		CType commonType = getCommonTypeIfArithmetic(scope);
 		Rvalue lhs = left.compileWithConversion(ic, scope, commonType);
 		Rvalue rhs = right.compileWithConversion(ic, scope, commonType);
 		return commonType.compileBinaryBitwiseOperator(ic, scope, lhs, rhs, operator);
 	}
 
 	private Rvalue compileComparisonOperator(IntermediateCompiler ic, Scope scope)
 			throws SyntaxException
 	{
 		CType commonType = getCommonTypeIfArithmetic(scope);
 		CType targetType = commonType != null ? commonType : CType.INTPTR_T;
 		Rvalue lhs = left.compileWithConversion(ic, scope, targetType);
 		Rvalue rhs = right.compileWithConversion(ic, scope, targetType);
 		return targetType.compileBinaryComparisonOperator(ic, scope, lhs, rhs, operator);
 	}
 
 	private Rvalue compileShiftOperator(IntermediateCompiler ic, Scope scope)
 			throws SyntaxException
 	{
 		// Both operators are only promoted ($6.5.7/3); no "usual arithmetic conversions" take
 		// place. However we may simply convert the right operand to int, since using larger values
 		// is undefined behavior anyway.
 		CType leftType = left.getType(scope).decay().promote();
 		CType rightType = CType.INT;
 
 		Rvalue lhs = left.compileWithConversion(ic, scope, leftType);
 		Rvalue rhs = right.compileWithConversion(ic, scope, rightType);
		return leftType.compileBinaryBitwiseOperator(ic, scope, lhs, rhs, operator);
 	}
 
 	private Rvalue compileArithmeticOperator(IntermediateCompiler ic, Scope scope)
 			throws SyntaxException
 	{
 		CType commonType = getCommonTypeIfArithmetic(scope);
 		if (commonType != null) {
 			Rvalue lhs = left.compileWithConversion(ic, scope, commonType);
 			Rvalue rhs = right.compileWithConversion(ic, scope, commonType);
 			return commonType.compileBinaryArithmeticOperator(ic, scope, lhs, rhs, operator);
 		} else
 			return compilePointerArithmeticOperator(ic, scope);
 	}
 
 	private Rvalue compilePointerArithmeticOperator(IntermediateCompiler ic, Scope scope)
 			throws SyntaxException
 	{
 		int leftIncrSize = left.getType(scope).decay().getIncrementSize();
 		int rightIncrSize = right.getType(scope).decay().getIncrementSize();
 		String mnemonic = binaryOperators.get(operator).mnemonic;
 
 		// Standard doesn't specify conversion for the integer operand in pointer arithmetic.
 		// However, any values that don't fit ptrdiff_t would be undefined behavior, so we may do a
 		// conversion to ptrdiff_t.
 		CType leftType = leftIncrSize > 1 ? new PointerType(CType.CHAR) : CType.PTRDIFF_T;
 		CType rightType = rightIncrSize > 1 ? new PointerType(CType.CHAR) : CType.PTRDIFF_T;
 
 		Rvalue lhs = left.compileWithConversion(ic, scope, leftType);
 
 		if (leftIncrSize == rightIncrSize) {
 			// POINTER - POINTER or POINTERTO32BIT +- INTEGER
 			Rvalue rhs = right.compileWithConversion(ic, scope, rightType);
 			ic.emit(mnemonic, lhs.getRegister(), rhs.getRegister());
 			if (leftIncrSize > 1)
 				ic.emit("div", lhs.getRegister(), "=" + leftIncrSize);
 		} else if (leftIncrSize > 1) {
 			// POINTER + INTEGER or POINTER - INTEGER.
 			Rvalue rhs = right.compileWithConversion(ic, scope, rightType);
 			ic.emit("mul", rhs.getRegister(), "=" + leftIncrSize);
 			ic.emit(mnemonic, lhs.getRegister(), rhs.getRegister());
 		} else if (rightIncrSize > 1) {
 			// INTEGER + POINTER.
 			ic.emit("mul", lhs.getRegister(), "=" + rightIncrSize);
 			Rvalue rhs = right.compileWithConversion(ic, scope, rightType);
 			ic.emit(mnemonic, lhs.getRegister(), rhs.getRegister());
 		}
 
 		return lhs;
 	}
 
 	@Override
 	public Integer getCompileTimeValue()
 	{
 		// Compile time evaluation of binary operators could be implemented here.
 		return null;
 	}
 
 	@Override
 	public CType getType(Scope scope) throws SyntaxException
 	{
 		return checkTypes(scope);
 	}
 
 	@Override
 	public String toString()
 	{
 		return "(BIN_EXPR " + operator + " " + left + " " + right + ")";
 	}
 
 	private CType checkTypes(Scope scope) throws SyntaxException
 	{
 		Operator op = binaryOperators.get(operator);
 		CType leftType = left.getType(scope).decay();
 		CType rightType = right.getType(scope).decay();
 		CType leftDeref = leftType.dereference();
 		CType rightDeref = rightType.dereference();
 
 		if (op.type == Type.LOGICAL) {
 			if (leftType.isScalar() && rightType.isScalar())
 				return CType.INT;
 		} else if (op.type == Type.EQUALITY) {
 			if (leftType.isArithmetic() && rightType.isArithmetic())
 				return CType.INT;
 			if (leftDeref.equals(rightDeref))
 				return CType.INT;
 			if (leftDeref instanceof VoidType && (rightDeref.isObject()
 					|| rightDeref.isIncomplete()))
 				return CType.INT;
 			if (rightDeref instanceof VoidType && (leftDeref.isObject()
 					|| leftDeref.isIncomplete()))
 				return CType.INT;
 			if (leftType.isPointer() && rightType.isInteger()
 					&& new Integer(0).equals(right.getCompileTimeValue()))
 				return CType.INT;
 			if (rightType.isPointer() && leftType.isInteger()
 					&& new Integer(0).equals(left.getCompileTimeValue()))
 				return CType.INT;
 		} else if (op.type == Type.RELATIONAL) {
 			if (leftType.isArithmetic() && rightType.isArithmetic()) //TODO arithmetic->real
 				return CType.INT;
 			if (leftDeref.equals(rightDeref) && (leftDeref.isObject() || leftDeref.isIncomplete()))
 				return CType.INT;
 		} else if (operator.equals("+")) {
 			if (leftType.isArithmetic() && rightType.isArithmetic())
 				return CType.getCommonType(leftType, rightType);
 			if (leftDeref.isObject() && rightType.isInteger())
 				return leftType;
 			if (leftType.isInteger() && rightDeref.isObject())
 				return rightType;
 		} else if (operator.equals("-")) {
 			if (leftType.isArithmetic() && rightType.isArithmetic())
 				return CType.getCommonType(leftType, rightType);
 			if (leftDeref.isObject() && rightType.isInteger())
 				return leftType;
 			if (leftDeref.isObject() && rightDeref.equals(leftDeref))
 				return CType.PTRDIFF_T;
 		} else if (op.type == Type.BITWISE) {
 			if (leftType.isInteger() && rightType.isInteger())
 				return CType.getCommonType(leftType, rightType);
 		} else if (op.type == Type.SHIFT) {
 			if (leftType.isInteger() && (rightType.isInteger()))
 				return leftType.promote();
 		} else if (op.type == Type.ARITHMETIC) {
 			if (leftType.isArithmetic() && (rightType.isInteger()
 					|| (!operator.equals("%") && rightType.isArithmetic())))
 				return CType.getCommonType(leftType, rightType);
 		}
 
 		throw new SyntaxException("Incompatible operands for operator " + operator + ".",
 				getPosition());
 	}
 
 	/**
 	 * Attempts to parse a syntactic binary expression from token stream. If parsing fails the
 	 * stream is reset to its initial position.
 	 *
 	 * @param tokens source token stream
 	 * @return Expression object or null if tokens don't form a valid expression
 	 */
 	public static Expression parse(TokenStream tokens)
 	{
 		return parseImpl(tokens, 0);
 	}
 
 	/**
 	 * Recursive implementation of the parsing method. Each call parses one
 	 * priority level of binary operators.
 	 */
 	private static Expression parseImpl(TokenStream tokens, int priority)
 	{
 		if (priority == 12)
 			return PrefixExpression.parse(tokens);
 
 		Position pos = tokens.getPosition();
 		tokens.pushMark();
 		Expression expr = parseImpl(tokens, priority + 1);
 
 		if (expr != null) {
 			while (true) {
 				tokens.pushMark();
 				Expression right = null;
 				String op = tokens.read().toString();
 				if (binaryOperators.containsKey(op) && binaryOperators.get(op).priority == priority)
 					right = parseImpl(tokens, priority + 1);
 
 				tokens.popMark(right == null);
 				if (right != null)
 					expr = new BinaryExpression(op, expr, right, pos);
 				else
 					break;
 			}
 		}
 
 		tokens.popMark(expr == null);
 		return expr;
 	}
 }
