 package gcl;
 
 import gcl.Codegen.ConstantLike;
 import gcl.SemanticActions.GCLErrorStream;
 
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.io.PrintWriter;
 
 //-------------------- Semantic Records ---------------------
 /**
  * This interface is implemented by all semantic Item classes that represent
  * semantic errors. It permits a simple test to filter out all error objects
  * that appear in various semantic routines. The pattern is 'tag interface'.
  */
 interface GeneralError { /* Nothing */
 }
 
 /**
  * Root of the semantic "record" hierarchy. All parameters of parsing functions
  * and semantic functions are objects from this set of classes.
  */
 abstract class SemanticItem {
 	// Note: Only expressions and procedures need semanticLevel, but this is the
 	// only common ancestor class.
 	private int level = -9; // This value should never appear
 
 	public String toString() {
 		return "Unknown semantic item. ";
 	}
 
 	/**
 	 * Polymorphically guarantee that a SemanticItem is an expression. This is
 	 * an example of a soft cast.
 	 * 
 	 * @return "this" if it is an expression and an ErrorExpression otherwise.
 	 */
 	public Expression expectExpression(final SemanticActions.GCLErrorStream err) {
 		err.semanticError(GCLError.EXPRESSION_REQUIRED);
 		return new ErrorExpression("Expression Required");
 	}
 
 	public int semanticLevel() {
 		return level;
 	}
 
 	public SemanticItem() {
 	}
 
 	public SemanticItem(final int level) {
 		this.level = level;
 	}
 
 	public TypeDescriptor expectType(final SemanticActions.GCLErrorStream err) {
 		if (this instanceof TypeDescriptor)
 			return (TypeDescriptor) this;
 		else
 			err.semanticError(GCLError.TYPE_REQUIRED);
 		return ErrorType.NO_TYPE;
 	}
 }
 
 /**
  * A general semantic error. There are more specific error classes also.
  * Immutable.
  */
 class SemanticError extends SemanticItem implements GeneralError {
 	public SemanticError(final String message) {
 		this.message = message;
 		CompilerOptions.message(message);
 	}
 
 	public Expression expectExpression(final SemanticActions.GCLErrorStream err) {
 		// Soft cast
 		return new ErrorExpression("$ Expression Required");
 		// Don't complain on error records. The complaint previously
 		// occurred when this object was created.
 	}
 
 	public String toString() {
 		return message;
 	}
 
 	private final String message;
 }
 
 /**
  * An object to represent a user defined identifier in a gcl program. Immutable.
  */
 class Identifier extends SemanticItem {
 	public Identifier(final String value) {
 		this.value = value;
 	}
 
 	public String name() {
 		return value;
 	}
 
 	public String toString() {
 		return value;
 	}
 
 	public int hashCode() {
 		return value.hashCode();
 	}
 
 	public boolean equals(Object o) {
 		return (o instanceof Identifier)
 				&& value.equals(((Identifier) o).value);
 	}
 
 	private final String value;
 }
 
 /** Root of the operator hierarchy */
 abstract class Operator extends SemanticItem implements Mnemonic {
 	public Operator(final String op, final SamOp opcode) {
 		value = op;
 		this.opcode = opcode;
 	}
 
 	public abstract ConstantExpression foldConstant(ConstantExpression left,
 			ConstantExpression right);
 
 	public String toString() {
 		return value;
 	}
 
 	public final SamOp opcode() {
 		return opcode;
 	}
 
 	private final String value;
 	private final SamOp opcode;
 }
 
 /**
  * Relational operators such as = and # Typesafe enumeration pattern as well as
  * immutable
  */
 abstract class RelationalOperator extends Operator {
 	public static final RelationalOperator EQUAL = new RelationalOperator(
 			"equal", JEQ) {
 		@Override
 		public ConstantExpression foldConstant(ConstantExpression left,
 				ConstantExpression right) {
 			if (left.value() == right.value())
 				return new ConstantExpression(SemanticActions.BOOLEAN_TYPE, 1);
 			else
 				return new ConstantExpression(SemanticActions.BOOLEAN_TYPE, 0);
 		}
 	};
 
 	public static final RelationalOperator NOT_EQUAL = new RelationalOperator(
 			"notequal", JNE) {
 		@Override
 		public ConstantExpression foldConstant(ConstantExpression left,
 				ConstantExpression right) {
 			if (left.value() == right.value())
 				return new ConstantExpression(SemanticActions.BOOLEAN_TYPE, 0);
 			else
 				return new ConstantExpression(SemanticActions.BOOLEAN_TYPE, 1);
 		}
 	};
 
 	public static final RelationalOperator LESS_THAN = new RelationalOperator(
 			"lessthan", JLT) {
 		@Override
 		public ConstantExpression foldConstant(ConstantExpression left,
 				ConstantExpression right) {
 			if (left.value() < right.value())
 				return new ConstantExpression(SemanticActions.BOOLEAN_TYPE, 1);
 			else
 				return new ConstantExpression(SemanticActions.BOOLEAN_TYPE, 0);
 		}
 	};
 
 	public static final RelationalOperator LESS_THAN_EQUAL = new RelationalOperator(
 			"lessthanequal", JLE) {
 		@Override
 		public ConstantExpression foldConstant(ConstantExpression left,
 				ConstantExpression right) {
 			if (left.value() <= right.value())
 				return new ConstantExpression(SemanticActions.BOOLEAN_TYPE, 1);
 			else
 				return new ConstantExpression(SemanticActions.BOOLEAN_TYPE, 0);
 		}
 	};
 
 	public static final RelationalOperator GREATER_THAN = new RelationalOperator(
 			"greaterthan", JGT) {
 		@Override
 		public ConstantExpression foldConstant(ConstantExpression left,
 				ConstantExpression right) {
 			if (left.value() > right.value())
 				return new ConstantExpression(SemanticActions.BOOLEAN_TYPE, 1);
 			else
 				return new ConstantExpression(SemanticActions.BOOLEAN_TYPE, 0);
 		}
 	};
 
 	public static final RelationalOperator GREATER_THAN_EQUAL = new RelationalOperator(
 			"greaterthanequal", JGE) {
 		@Override
 		public ConstantExpression foldConstant(ConstantExpression left,
 				ConstantExpression right) {
 			if (left.value() >= right.value())
 				return new ConstantExpression(SemanticActions.BOOLEAN_TYPE, 1);
 			else
 				return new ConstantExpression(SemanticActions.BOOLEAN_TYPE, 0);
 		}
 	};
 
 	private RelationalOperator(final String op, final SamOp opcode) {
 		super(op, opcode);
 	}
 
 }
 
 abstract class BooleanOperator extends Operator {
 	public static final BooleanOperator AND = new BooleanOperator("and", BA) {
 		@Override
 		public ConstantExpression foldConstant(ConstantExpression left,
 				ConstantExpression right) {
 			if (left.value() == 1 && right.value() == 1)
 				return new ConstantExpression(SemanticActions.BOOLEAN_TYPE, 1);
 			else
 				return new ConstantExpression(SemanticActions.BOOLEAN_TYPE, 0);
 		}
 	};
 
 	public static final BooleanOperator OR = new BooleanOperator("or", BO) {
 		@Override
 		public ConstantExpression foldConstant(ConstantExpression left,
 				ConstantExpression right) {
 			if (left.value() == 1 || right.value() == 1)
 				return new ConstantExpression(SemanticActions.BOOLEAN_TYPE, 1);
 			else
 				return new ConstantExpression(SemanticActions.BOOLEAN_TYPE, 0);
 		}
 	};
 
 	private BooleanOperator(final String op, final SamOp opcode) {
 		super(op, opcode);
 	}
 }
 
 /**
  * Add operators such as + and - Typesafe enumeration pattern as well as
  * immutable
  */
 abstract class AddOperator extends Operator {
 	public static final AddOperator PLUS = new AddOperator("plus", IA) {
 		@Override
 		public ConstantExpression foldConstant(ConstantExpression left,
 				ConstantExpression right) {
 			return new ConstantExpression(SemanticActions.INTEGER_TYPE,
 					left.value() + right.value());
 		}
 	};
 
 	public static final AddOperator MINUS = new AddOperator("minus", IS) {
 		@Override
 		public ConstantExpression foldConstant(ConstantExpression left,
 				ConstantExpression right) {
 			return new ConstantExpression(SemanticActions.INTEGER_TYPE,
 					left.value() - right.value());
 		}
 	};
 
 	private AddOperator(final String op, final SamOp opcode) {
 		super(op, opcode);
 	}
 }
 
 /**
  * Multiply operators such as * and / Typesafe enumeration pattern as well as
  * immutable
  */
 abstract class MultiplyOperator extends Operator {
 	public static final MultiplyOperator TIMES = new MultiplyOperator("times",
 			IM) {
 		@Override
 		public ConstantExpression foldConstant(ConstantExpression left,
 				ConstantExpression right) {
 			return new ConstantExpression(SemanticActions.INTEGER_TYPE,
 					left.value() * right.value());
 		}
 	};
 
 	public static final MultiplyOperator DIVIDE = new MultiplyOperator(
 			"divide", ID) {
 		@Override
 		public ConstantExpression foldConstant(ConstantExpression left,
 				ConstantExpression right) {
 			return new ConstantExpression(SemanticActions.INTEGER_TYPE,
 					left.value() / right.value());
 		}
 	};
 
 	public static final MultiplyOperator MODULUS = new MultiplyOperator(
 			"modulus", ID) {
 		@Override
 		public ConstantExpression foldConstant(ConstantExpression left,
 				ConstantExpression right) {
 			return new ConstantExpression(SemanticActions.INTEGER_TYPE,
 					left.value() % right.value());
 		}
 	};
 
 	private MultiplyOperator(final String op, final SamOp opcode) {
 		super(op, opcode);
 	}
 }
 
 /**
  * Root of the expression object hierarchy. Represent integer and boolean
  * expressions.
  */
 abstract class Expression extends SemanticItem implements Codegen.MaccSaveable {
 	public Expression(final TypeDescriptor type, final int level) {
 		super(level);
 		this.type = type;
 	}
 
 	/**
 	 * Polymorphically determine if an expression needs to be pushed on the run
 	 * stack as part of parallel assignment.
 	 * 
 	 * @return true if the expression could appear as a LHS operand.
 	 */
 	public boolean needsToBePushed() {
 		return false;
 	}
 
 	public TypeDescriptor type() {
 		return type;
 	}
 
 	public Expression expectExpression(final SemanticActions.GCLErrorStream err) {
 		return this;
 	} // soft cast
 
 	public void discard() {
 	} // (Function return only) default is to do nothing
 
 	private final TypeDescriptor type;
 
 	public ConstantExpression expectConstant(Expression expr,
 			final SemanticActions.GCLErrorStream err) {
 		if (expr instanceof ConstantExpression)
 			return (ConstantExpression) expr;
 		else
 			err.semanticError(GCLError.TYPE_REQUIRED);
 		return null;
 	}
 }
 
 /** Used to represent errors where expressions are expected. Immutable. */
 class ErrorExpression extends Expression implements GeneralError,
 		CodegenConstants {
 
 	private final String message;
 
 	public ErrorExpression(final String message) {
 		super(ErrorType.NO_TYPE, GLOBAL_LEVEL);
 		this.message = message;
 		CompilerOptions.message(message);
 	}
 
 	public String toString() {
 		return message;
 	}
 }
 
 /**
  * Constant expressions such as 53 and true. Immutable. Use this for boolean
  * constants also, with 1 for true and 0 for false.
  */
 class ConstantExpression extends Expression implements CodegenConstants,
 		Codegen.ConstantLike {
 
 	private final int value;
 
 	public ConstantExpression(final TypeDescriptor type, final int value) {
 		super(type, GLOBAL_LEVEL);
 		this.value = value;
 	}
 
 	public String toString() {
 		return "ConstantExpression: " + value + " with type " + type();
 	}
 
 	public boolean equals(Object other) {
 		return (other instanceof ConstantExpression)
 				&& type().baseType().isCompatible(
 						((ConstantExpression) other).type().baseType())
 				&& ((ConstantExpression) other).value == value;
 	}
 
 	public int hashCode() {
 		return value * type().baseType().hashCode();
 	}
 
 	public int value() {
 		return value;
 	}
 }
 
 /**
  * Variable expressions such as x and y[3]. Variable here means storable.
  * Objects here are immutable. A level 0 expression is a temporary.
  */
 class VariableExpression extends Expression implements CodegenConstants {
 
 	private final int offset; // relative offset of cell or register number
 	private final boolean isDirect; // if false this is a pointer to a location.
 
 	/**
 	 * Create a variable expression object
 	 * 
 	 * @param type
 	 *            the type of this variable
 	 * @param scope
 	 *            the nesting level (if >0) or 0 for a register, or -1 for
 	 *            stacktop
 	 * @param offset
 	 *            the relative offset of the cells of this variable, or the
 	 *            register number if scope is 0
 	 * @param direct
 	 *            if false this represents a pointer to the variable
 	 */
 	public VariableExpression(final TypeDescriptor type, final int level,
 			final int offset, final boolean direct) {
 		super(type, level);
 		this.offset = offset;
 		this.isDirect = direct;
 	}
 
 	/**
 	 * Create a temporary expression. The level is 0 and the offset is the
 	 * register number
 	 * 
 	 * @param type
 	 *            the type of this value
 	 * @param register
 	 *            the register number in which to hold it
 	 * @param direct
 	 *            is the value in the register (true) or a pointer (false)
 	 */
 	public VariableExpression(final TypeDescriptor type, final int register,
 			final boolean direct) {
 		this(type, 0, register, direct);
 	}
 
 	public boolean needsToBePushed() { // used by parallel assignment
 		return semanticLevel() > CPU_LEVEL
 				|| (semanticLevel() == CPU_LEVEL && !isDirect);
 	}
 
 	/**
 	 * The relative address of the variable. What it is relative to depends on
 	 * its scopeLevel. If the level is 1 it is relative to R15.
 	 * 
 	 * @return the relative offset from its base register.
 	 */
 	public int offset() {
 		return offset;
 	}
 
 	public boolean isDirect() {
 		return isDirect;
 	}
 
 	public void discard(final Codegen codegen) // used for function return only
 	{
 		if (semanticLevel() == STACK_LEVEL) {
 			codegen.gen2Address(Mnemonic.IA, STACK_POINTER, IMMED, UNUSED,
 					type().size());
 		}
 	}
 
 	public String toString() {
 		return "VariableExpression: level(" + semanticLevel() + ") offset("
 				+ offset + ") " + (isDirect ? "direct" : "indirect")
 				+ ", with type " + type();
 	}
 }
 
 /** Carries information needed by the assignment statement */
 class AssignRecord extends SemanticItem {
 
 	private final ArrayList<Expression> lhs = new ArrayList<Expression>(3);
 	private final ArrayList<Expression> rhs = new ArrayList<Expression>(3);
 
 	public void left(Expression left) {
 		if (left == null) {
 			left = new ErrorExpression("$ Pushing bad lhs in assignment.");
 		}
 		lhs.add(left);
 	}
 
 	public void right(Expression right) {
 		if (right == null) {
 			right = new ErrorExpression("$ Pushing bad rhs in assignment.");
 		}
 		rhs.add(right);
 	}
 
 	public Expression left(final int index) {
 		return lhs.get(index);
 	}
 
 	public Expression right(final int index) {
 		return rhs.get(index);
 	}
 
 	/**
 	 * Determine whether the assignment statement is legal.
 	 * 
 	 * @return true if there are the same number of operands on the left and
 	 *         right and the types are compatible, etc.
 	 */
 	public boolean verify(final SemanticActions.GCLErrorStream err) { // incomplete.
 																		// More
 																		// tests
 																		// needed.
 		boolean result = true;
 		if (lhs.size() != rhs.size()) {
 			result = false;
 			err.semanticError(GCLError.LISTS_MUST_MATCH);
 		}
 		// more
 		return result;
 	}
 
 	/**
 	 * The number of matched operands of a parallel assignment. In an incorrect
 	 * input program the lhs and rhs may not match.
 	 * 
 	 * @return the min number of lhs, rhs variable expressions.
 	 */
 	public int size() {
 		return Math.min(rhs.size(), lhs.size());
 	}
 }
 
 /**
  * Used to pass a list of expressions around the parser/semantic area. It is
  * used in the creation of tuple expressions and may be useful elsewhere.
  */
 class ExpressionList extends SemanticItem {
 	/**
 	 * Enter a new expression into the list
 	 * 
 	 * @param expression
 	 *            the expression to be entered
 	 */
 	public void enter(final Expression expression) {
 		elements.add(expression);
 	}
 
 	/**
 	 * Provide an enumeration service over the expressions in the list in the
 	 * order they were inserted.
 	 * 
 	 * @return an enumeration over the expressions.
 	 */
 	public Iterator<Expression> elements() {
 		return elements.iterator();
 	}
 
 	private final ArrayList<Expression> elements = new ArrayList<Expression>();
 }
 
 /**
  * Specifies the kind of procedure parameter. The value NOT_PARAM is used for
  * variables that are not parameters. Typesafe enumeration
  */
 class ParameterKind extends SemanticItem {
 	private ParameterKind() {
 	}
 
 	public static final ParameterKind NOT_PARAM = new ParameterKind();
 	public static final ParameterKind VALUE_PARAM = new ParameterKind();
 	public static final ParameterKind REFERENCE_PARAM = new ParameterKind();
 
 }
 
 /** Used to carry information for guarded commands such as if and do */
 class GCRecord extends SemanticItem // For guarded command statements if and do.
 {
 	private final int outLabel;
 	private int nextLabel;
 
 	public GCRecord(final int outLabel, final int nextLabel) {
 		this.outLabel = outLabel;
 		this.nextLabel = nextLabel;
 	}
 
 	/**
 	 * Mutator for the internal label in an if or do.
 	 * 
 	 * @param label
 	 *            The new value for this label.
 	 */
 	public void nextLabel(int label) {
 		nextLabel = label;
 	}
 
 	/**
 	 * Returns the current value of the "internal" label of an if or do.
 	 * 
 	 * @return the "next" label to appear in a sequence.
 	 */
 	public int nextLabel() {
 		return nextLabel;
 	}
 
 	/**
 	 * The external label of an if or do statement.
 	 * 
 	 * @return the external label's numeric value.
 	 */
 	public int outLabel() {
 		return outLabel;
 	}
 
 	public String toString() {
 		return "GCRecord out: " + outLabel + " next: " + nextLabel;
 	}
 }
 
 class ModuleRecord extends SemanticItem {
 
 	private SymbolTable scope;
 	private Identifier moduleId;
 	private Codegen codegen;
 	private int label;
 
 	public ModuleRecord(SymbolTable scope, Identifier id, Codegen codegen) {
 		this.scope = scope;
 		this.moduleId = id;
 		this.codegen = codegen;
 		this.label = this.codegen.getLabel();
 	}
 
 	public int getLabel() {
 		return label;
 	}
 
 	public void setPrivateScope(SymbolTable scope) {
 		this.scope = scope;
 	}
 
 	public SymbolTable getScope() {
 		return scope;
 	}
 
 	public Identifier getID() {
 		return moduleId;
 	}
 
 }
 
 // --------------------- Types ---------------------------------
 /**
  * Root of the type hierarchy. Objects to represent gcl types such as integer
  * and the various array and tuple types. These are immutable after they are
  * locked.
  */
 abstract class TypeDescriptor extends SemanticItem implements Cloneable {
 
 	private int size = 0; // default size. This varies in subclasses.
 
 	public TypeDescriptor(final int size) {
 		this.size = size;
 	}
 
 	/**
 	 * The number of bytes required to store a variable of this type.
 	 * 
 	 * @return the byte size.
 	 */
 	public int size() {
 		return size;
 	}
 
 	/**
 	 * Determine if two types are assignment (or other) compatible. This must be
 	 * a reflexive, symmetric, and transitive relation.
 	 * 
 	 * @param other
 	 *            the other type to be compared to this.
 	 * @return true if they are compatible.
 	 */
 	public boolean isCompatible(final TypeDescriptor other) { // override this.
 
 		return false;
 	}
 
 	/**
 	 * Polymorphically determine the underlying type of this type. Useful mostly
 	 * for range types.
 	 * 
 	 * @return this for non-ranges. The base type for ranges.
 	 */
 	public TypeDescriptor baseType() {
 		return this;
 	}
 
 	public Object clone() {
 		return this;
 	}// Default version. Override in mutable subclasses.
 
 	public String toString() {
 		return "Unknown type.";
 	}
 }
 
 /** Represents an error where a type is expected. Singleton. Immutable. */
 class ErrorType extends TypeDescriptor implements GeneralError {
 	private ErrorType() {
 		super(0);
 	}
 
 	public String toString() {
 		return "Error type.";
 	}
 
 	public Expression expectExpression() // Soft cast
 	{
 		return new ErrorExpression("Expression Required");
 		// Don't complain on error records. The complaint previously
 		// occurred when this object was referenced.
 	}
 
 	public static final ErrorType NO_TYPE = new ErrorType();
 }
 
 /** Integer type. Created at initialization. Singleton. Immutable. */
 class IntegerType extends TypeDescriptor implements CodegenConstants {
 	private IntegerType() {
 		super(INT_SIZE);
 	}
 
 	public String toString() {
 		return "integer type.";
 	}
 
 	static public final IntegerType INTEGER_TYPE = new IntegerType();
 
 	public boolean isCompatible(final TypeDescriptor other) {
 		return other != null && other.baseType() instanceof IntegerType;
 	}
 }
 
 /** Boolean type. Created at initialization. Singleton. Immutable. */
 class BooleanType extends TypeDescriptor implements CodegenConstants {
 	private BooleanType() {
 		super(INT_SIZE);
 	}
 
 	public String toString() {
 		return "Boolean type.";
 	}
 
 	public boolean isCompatible(final TypeDescriptor other) {
 		return other != null && other.baseType() instanceof BooleanType;
 	}
 
 	static public final BooleanType BOOLEAN_TYPE = new BooleanType();
 
 }
 
 /** Integer type. Created at initialization. Singleton. Immutable. */
 class RangeType extends TypeDescriptor implements CodegenConstants {
 	public RangeType(TypeDescriptor baseType, int lowBound, int highBound,
 			Codegen.Location lowBoundOffset) {
 		super(baseType.size());
 		this.lowBound = lowBound;
 		this.highBound = highBound;
 		this.baseType = baseType;
 		this.lowBoundOffset = lowBoundOffset;
 	}
 
 	public String toString() {
 		return "Range type.";
 	}
 
 	public TypeDescriptor baseType() {
 		return baseType;
 	}
 
 	public boolean isCompatible(final TypeDescriptor other) {
 		return baseType.isCompatible(other);
 	}
 
 	public int getLowBound() {
 		return this.lowBound;
 	}
 
 	public int getHighBound() {
 		return this.highBound;
 	}
 
 	public Codegen.Location getLowBoundOffset() {
 		return this.lowBoundOffset;
 	}
 
 	private int lowBound;
 	private int highBound;
 	private TypeDescriptor baseType;
 	private Codegen.Location lowBoundOffset;
 }
 
 /** String constant. Need a different instance every use because of the size. */
 class StringConstant extends SemanticItem implements ConstantLike {
 	private final String message;
 	private final int size;
 
 	public StringConstant(String message) {
 		int size;
 		message = message.substring(1, message.length() - 1);
 		if (message.length() % 2 == 0)
 			size = message.length() + 2;
 		else {
 			size = message.length() + 1;
 		}
 		message = message.replaceAll(":", "::");
 		message = message.replaceAll("'", ":'");
 		message = message.replaceAll("\"", ":\"");
 		message = '"' + message + '"';
 		this.size = size;
 		this.message = message;
 	}
 
 	public int size() {
 		return this.size;
 	}
 
 	public String samString() {
 		return this.message;
 	}
 }
 
 /**
  * Use this when you need to build a list of types and know the total size of
  * all of them. Used in creation of tuples.
  */
 class TypeList extends SemanticItem {
 
 	private final ArrayList<TypeDescriptor> elements = new ArrayList<TypeDescriptor>(
 			2);
 	private final ArrayList<Identifier> names = new ArrayList<Identifier>(2);
 	private final ArrayList<Procedure> methods = new ArrayList<Procedure>(2);
 	private final ArrayList<Identifier> methodNames = new ArrayList<Identifier>(
 			2);
 	private int size = 0; // sum of the sizes of the types
 	private static int next = 0;
 
 	/**
 	 * Add a new type-name pair to the list and accumulate its size
 	 * 
 	 * @param aType
 	 *            the type to be added
 	 * @param name
 	 *            the name associated with the field
 	 */
 	public void enter(final TypeDescriptor aType, final Identifier name) {
 		elements.add(aType);
 		names.add(name);
 		size += aType.size();
 	} // TODO check that the names are distinct.
 
 	public void enterProc(final Procedure method) {
 		methods.add(method);
 		methodNames.add(method.getName());
 	} // TODO check that the names are distinct.
 
 	/**
 	 * Add a new type to the list, using a default name This is used to define
 	 * anonymous fields in a tuple value
 	 * 
 	 * @param aType
 	 *            the type of the entry to be added
 	 */
 	public void enter(final TypeDescriptor aType) {
 		enter(aType, new Identifier("none_" + next));
 		next++; // unique "names" for anonymous fields.
 	}
 
 	/**
 	 * The total size of the types in the list
 	 * 
 	 * @return the sum of the sizes
 	 */
 	public int size() {
 		return size;
 	}
 
 	/**
 	 * An enumeration service for the types in the list in order of insertion
 	 * 
 	 * @return an enumeration over the type descriptors.
 	 */
 	public Iterator<TypeDescriptor> elements() {
 		return elements.iterator();
 	}
 
 	/**
 	 * An enumeration service for the method names of the fields
 	 * 
 	 * @return an enumeration over the identifiers
 	 */
 	public Iterator<Identifier> methodNames() {
 		return methodNames.iterator();
 	}
 
 	/**
 	 * An enumeration service for the methods in the list in order of insertion
 	 * 
 	 * @return an enumeration over the Procedures.
 	 */
 	public Iterator<Procedure> methods() {
 		return methods.iterator();
 	}
 
 	/**
 	 * An enumeration service for the names of the fields
 	 * 
 	 * @return an enumeration over the identifiers
 	 */
 	public Iterator<Identifier> names() {
 		return names.iterator();
 	}
 }
 
 /**
  * Represents the various tuple types. Created as needed. These are built
  * incrementally and locked at the end to make them immutable afterwards.
  */
 class TupleType extends TypeDescriptor { // mutable
 
 	private final HashMap<Identifier, TupleField> fields = new HashMap<Identifier, TupleField>(
 			4);
 	private final ArrayList<Identifier> names = new ArrayList<Identifier>(4);
 	private SymbolTable methods = null; // later
 
 	/**
 	 * Create a tuple type from a list of its component types. We will need to
 	 * add the "methods" to this later.
 	 * 
 	 * @param carrier
 	 *            the list of component types
 	 */
 	public TupleType(final TypeList carrier) {
 		super(carrier.size());
 		methods = SymbolTable.unchained();
 		Iterator<TypeDescriptor> e = carrier.elements();
 		Iterator<Identifier> n = carrier.names();
 		Iterator<Procedure> m = carrier.methods();
 		Iterator<Identifier> n2 = carrier.methodNames();
 		int inset = 0;
 		while (e.hasNext()) {
 			TypeDescriptor t = e.next();
 			Identifier id = n.next();
 			fields.put(id, new TupleField(inset, t));
 			inset += t.size();
 			names.add(id);
 		}
 		while (m.hasNext()) {
 			Procedure t = m.next();
 			Identifier id = n2.next();
 			methods.newEntry("Procedure", id, t, this);
 			t.setParentTuple(this);
 		}
 	}
 
 	public String toString() {
 		String result = "tupleType:[";
 		for (int i = 0; i < fields.size(); ++i) {
 			result += fields.get(names.get(i)) + " : " + names.get(i) + ", ";// type);
 		}
 		result += "] with size: " + size();
 		return result;
 	}
 
 	/**
 	 * Get the number of data fields in this tuple
 	 * 
 	 * @return the number of fields in this tuple
 	 */
 	public int fieldCount() {
 		return names.size();
 	}
 
 	public Identifier getName(int offset) {
 		return names.get(offset);
 	}
 
 	public boolean isCompatible(TypeDescriptor other) {
 		if (other instanceof TupleType && ((TupleType) other).fieldCount() == this.fieldCount()) {
 			for (int i = 0; i < fieldCount(); i++) {
 				TupleType otherTuple = (TupleType) other;
				if (!(otherTuple.getType(otherTuple.getName(i)) == this.getType(this.getName(i))))
 					return false;
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Retrieve a named component type of the tuple. It might throw
 	 * NoSuchElementException if the argument is invalid.
 	 * 
 	 * @param fieldName
 	 *            the name of the desired component type
 	 * @return the type of the named component
 	 */
 	public TypeDescriptor getType(final Identifier fieldName) { // null return
 																// value
 																// possible
 
 		TupleField temp = fields.get(fieldName);
 		if (temp == null)
 			return null;
 		else
 			return temp.type();
 	}
 
 	public Procedure getProcedure(final Identifier procName) {
 		SemanticItem proc = methods.lookupIdentifier(procName).semanticRecord();
 		if (proc instanceof SemanticError) {
 			return null;
 		}
 		return (Procedure) methods.lookupIdentifier(procName).semanticRecord();
 	}
 
 	public int getInset(Identifier id) {
 		return fields.get(id).inset();
 	}
 
 	private class TupleField {
 		public TupleField(final int inset, final TypeDescriptor t) {
 			this.inset = inset;
 			this.type = t;
 		}
 
 		public String toString() {
 			return type.toString();
 		}
 
 		public TypeDescriptor type() {
 			return type;
 		}
 
 		public int inset() {
 			return inset;
 		}
 
 		private final int inset;
 		private final TypeDescriptor type;
 	}
 
 }
 
 class ArrayCarrier extends SemanticItem {
 
 	private ArrayList<TypeDescriptor> arrayCarrier = new ArrayList<TypeDescriptor>();
 
 	public ArrayCarrier() {
 
 	}
 
 	public void push(TypeDescriptor subscriptType,
 			final SemanticActions.GCLErrorStream err) {
 		if (!(subscriptType instanceof RangeType)) {
 			err.semanticError(GCLError.RANGE_REQUIRED);
 			return;
 		} else {
 			arrayCarrier.add(subscriptType);
 		}
 	}
 
 	public TypeDescriptor pop() {
 		TypeDescriptor temp = arrayCarrier.get(arrayCarrier.size() - 1);
 		arrayCarrier.remove(arrayCarrier.size() - 1);
 		return temp;
 	}
 
 	public int getSize() {
 		return arrayCarrier.size();
 	}
 }
 
 class ArrayType extends TypeDescriptor { // mutable
 
 	private TypeDescriptor componentType;
 	private TypeDescriptor subscriptType;
 
 	public ArrayType(final TypeDescriptor componentType,
 			TypeDescriptor subscriptType,
 			final SemanticActions.GCLErrorStream err) {
 		super(componentType.size()
 				* (((RangeType) subscriptType).getHighBound()
 						- ((RangeType) subscriptType).getLowBound() + 1));
 		this.componentType = componentType;
 		if (subscriptType instanceof RangeType)
 			this.subscriptType = subscriptType;
 		else {
 			err.semanticError(GCLError.RANGE_REQUIRED);
 			this.subscriptType = ErrorType.NO_TYPE;
 		}
 	}
 
 	public String toString() {
 		String result = "ArrayType:[";
 		result += "] with size: " + size();
 		return result;
 	}
 
 	public boolean isCompatible(TypeDescriptor other) {
 		if (other instanceof ArrayType){
 			return ((ArrayType) other).getComponentType().isCompatible(this.getComponentType()) 
 			&& ((ArrayType) other).getSubscriptType().isCompatible(this.getSubscriptType());
 		} else {
 			return false;
 		}
 	}
 
 	public TypeDescriptor getSubscriptType() {
 		return subscriptType;
 	}
 
 	public TypeDescriptor getComponentType() {
 		return componentType;
 	}
 
 	public TypeDescriptor getType() {
 		return this;
 	}
 
 }
 
 class Procedure extends SemanticItem {
 	final static int DEFAULT_FRAME_SIZE = 8;
 	private Procedure parentProcedure;
 	private SymbolTable scope;
 	private Identifier name;
 	private int label;
 	private boolean defined;
 	private int level;
 	private int localDataSize = 8;
 	private int frameSize = 8;
 	private ArrayList<Loader> params = new ArrayList<Loader>(2);
 	private TupleType parentTuple;
 
 	public Procedure(Procedure parentProcedure, Identifier name,
 			SymbolTable scope, Codegen codegen) {
 		this.parentProcedure = parentProcedure;
 		this.name = name;
 		this.scope = scope.openScope(true);
 		defined = false;
 		label = codegen.getLabel();
 		if (parentProcedure == null) {
 			level = CodegenConstants.GLOBAL_LEVEL + 1;
 		} else {
 			level = parentProcedure.semanticLevel() + 1;
 		}
 	}
 
 	public int semanticLevel() {
 		return this.level;
 	}
 
 	public boolean alreadyDefined() {
 		return defined;
 	}
 
 	public int frameSize() {
 		return this.frameSize;
 	}
 	
 	public int localDataSize() {
 		return this.localDataSize;
 	}
 
 	public Procedure getParentProcedure() {
 		return this.parentProcedure;
 	}
 
 	public Identifier getName() {
 		return this.name;
 	}
 
 	public SymbolTable getScope() {
 		return this.scope;
 	}
 
 	public int getLabel() {
 		return this.label;
 	}
 
 	public void genLink(Codegen codegen) {
 		defined = true;
 		codegen.genLabel('P', label);
 		codegen.gen2Address(Codegen.STO, Codegen.FRAME_POINTER,
 				new Codegen.Location(Codegen.INDXD, Codegen.STACK_POINTER, +0));
 		codegen.gen2Address(Codegen.LDA, Codegen.FRAME_POINTER,
 				new Codegen.Location(Codegen.INDXD, Codegen.STACK_POINTER, +0));
 		codegen.gen2Address(Codegen.STO, Codegen.STATIC_POINTER,
 				new Codegen.Location(Codegen.INDXD, Codegen.FRAME_POINTER, +4));
 		codegen.gen2Address(Codegen.LD, Codegen.STATIC_POINTER,
 				new Codegen.Location(Codegen.INDXD, Codegen.FRAME_POINTER, +2));
 		codegen.gen2Address(Codegen.IS, Codegen.STACK_POINTER, Codegen.IMMED,
 				Codegen.UNUSED, this.localDataSize - DEFAULT_FRAME_SIZE);
 		codegen.genPushPopToStack(Codegen.PUSH);
 
 	}
 
 	public void genUnlink(Codegen codegen) {
 		codegen.genLabel('U', label);
 		codegen.genPushPopToStack(Codegen.POP);
 		codegen.gen2Address(Codegen.IA, Codegen.STACK_POINTER, Codegen.IMMED,
 				Codegen.UNUSED, this.localDataSize - DEFAULT_FRAME_SIZE);
 		codegen.gen2Address(Codegen.LD, Codegen.STATIC_POINTER,
 				new Codegen.Location(Codegen.INDXD, Codegen.FRAME_POINTER, +4));
 		codegen.gen2Address(Codegen.LD, Codegen.FRAME_POINTER,
 				new Codegen.Location(Codegen.INDXD, Codegen.FRAME_POINTER, +0));
 		codegen.gen1Address(Codegen.JMP, Codegen.IREG, Codegen.STATIC_POINTER,
 				0);
 	}
 
 	public VariableExpression reserveLocalAddress(TypeDescriptor type) {
 		this.localDataSize += type.size();
 		int offset = -1 * (this.localDataSize - DEFAULT_FRAME_SIZE);
 		return new VariableExpression(type, this.semanticLevel(), offset,
 				Codegen.DIRECT);
 	}
 
 	public Expression reserveParameterAddress(TypeDescriptor type, ParameterKind paramKind, GCLErrorStream err) {
 		int offset = frameSize;
 		Loader paramLoader = null;
 		boolean direct = true;
 		if(paramKind == ParameterKind.REFERENCE_PARAM){
 			paramLoader = new ReferenceLoader(type, offset);
 			direct = false;
 		}
 		if(paramKind == ParameterKind.VALUE_PARAM){
 			if(type instanceof TupleType || type instanceof ArrayType){
 				paramLoader = new BlockLoader(type, offset);
 			}else{
 				paramLoader = new ValueLoader(type, offset);
 			}
 		} 
 		frameSize += paramLoader.size();
 		params.add(paramLoader);
 		return new VariableExpression(type, this.level, offset, direct);
 	}
 	
 	public void call(ExpressionList arguments, Codegen codegen, GCLErrorStream err){
 		if(arguments != null){
 			Iterator<Expression> argumentIterator = arguments.elements();
 			for(Loader argumentLoader : params){
 				
 				if(argumentIterator.hasNext()){
 					Expression argumentExpression = argumentIterator.next();
 					if(argumentLoader.checkType(argumentExpression, err)){
 						argumentLoader.load(argumentExpression, codegen, err);
 					}
 				}else{
 					err.semanticError(GCLError.INVALID_ARGUMENT, "Not enough arguments");
 				}
 				
 			}
 
 			if(argumentIterator.hasNext()){
 			err.semanticError(GCLError.INVALID_ARGUMENT, "Too many arguments");
 			}
 		}
 	}
 	
 	public void setParentTuple(TupleType parentTuple){
 		this.parentTuple = parentTuple;
 	}
 	
 	public TupleType getParentTuple(){
 		return this.parentTuple;
 	}
 	
 	public int getLevel(){
 		return this.level;
 	}
 }
 
 abstract class Loader{
 	protected TypeDescriptor type;
 	protected int offset;
 	
 	public Loader(TypeDescriptor type, int offset){
 		this.type = type;
 		this.offset = offset;
 	}
 	
 	public boolean checkType(Expression param, GCLErrorStream err){
 		if(!type.isCompatible(param.type())){
 			 err.semanticError(GCLError.INCOMPATIBLE_TYPE, "Expected: "+type.toString()+ " Got: " + param.type());
 			return false;
 		}
 			return true;
 		}
 
 	public abstract void load(Expression param, Codegen codegen, GCLErrorStream err);
 	public abstract int size();
 	}
 
 class ValueLoader extends Loader{
 	public ValueLoader(TypeDescriptor type, int offset){
 		super(type, offset);
 	}
 
 	@Override
 	public int size() {
 		return type.size();
 	}
 
 	@Override
 	public void load(Expression param, Codegen codegen, GCLErrorStream err) {
 		int valueRegister = codegen.loadRegister(param);
 		codegen.gen2Address(Codegen.STO, valueRegister, new Codegen.Location(Codegen.INDXD, Codegen.STACK_POINTER, offset));
 		codegen.freeTemp(Codegen.DREG, valueRegister);
 	}
 }
 
 
 class ReferenceLoader extends Loader{
 	public ReferenceLoader(TypeDescriptor type, int offset){
 		super(type, offset);
 	}
 	
 	public boolean checkType(Expression param, GCLErrorStream err){
 		if(param instanceof ConstantExpression){
 			err.semanticError(GCLError.INVALID_LOAD, "Cannot pass constants by reference");
 			return false;
 		}
 		return super.checkType(param, err);
 	}
 	
 	@Override
 	public int size() {
 		return 2;
 	}
 
 	@Override
 	public void load(Expression param, Codegen codegen, GCLErrorStream err) {
 		int referenceRegister = codegen.loadAddress(param);
 		codegen.gen2Address(Codegen.STO, referenceRegister, new Codegen.Location(Codegen.INDXD, Codegen.STACK_POINTER, offset));
 		codegen.freeTemp(Codegen.DREG, referenceRegister);
 	}
 }
 
 
 class BlockLoader extends Loader{
 	public BlockLoader(TypeDescriptor type, int offset){
 		super(type, offset);
 	}
 
 	@Override
 	public int size() {
 		return type.size();
 	}
 
 	@Override
 	public void load(Expression param, Codegen codegen, GCLErrorStream err) {
 		if(!(param instanceof VariableExpression)){
 			if(!(param instanceof ErrorExpression)){
 				err.semanticError(GCLError.INVALID_ARGUMENT, "Only variables can be passed by reference");
 			}
 			return;
 		}
 
 		int blockRegister = codegen.getTemp(2);
 		int sizeRegister = blockRegister +1;
 		Codegen.Location parameterLocation = codegen.buildOperands(param);
 		codegen.gen2Address(Codegen.LD, blockRegister, parameterLocation);
 		codegen.gen2Address(Codegen.LD, sizeRegister, Codegen.IMMED, Codegen.UNUSED, size());
 		codegen.gen2Address(Codegen.BKT, blockRegister, new Codegen.Location(Codegen.INDXD, Codegen.STACK_POINTER, offset));
 		codegen.freeTemp(Codegen.DREG, blockRegister);
 		codegen.freeTemp(Codegen.DREG, sizeRegister);
 		codegen.freeTemp(parameterLocation);
 	}
 }
 // --------------------- Semantic Error Values ----------------------------
 
 /**
  * Represents the various gcl errors User errors represent an error in the input
  * program. They must be reported.
  * <p>
  * Compiler errors represent an error in the compiler itself. They must be
  * fixed. These are used to report errors to the user.
  */
 abstract class GCLError {
 	// The following are user errors. Report them.
 	static final GCLError INTEGER_REQUIRED = new Value(1,
 			"ERROR -> Integer type required. ");
 	static final GCLError ALREADY_DEFINED = new Value(2,
 			"ERROR -> The item is already defined. ");
 	static final GCLError NAME_NOT_DEFINED = new Value(3,
 			"ERROR -> The name is not defined. ");
 	static final GCLError TYPE_REQUIRED = new Value(4,
 			"ERROR -> TypeReference name required. ");
 	static final GCLError LISTS_MUST_MATCH = new Value(5,
 			"ERROR -> List lengths must be the same. ");
 	static final GCLError NOT_VARIABLE = new Value(6,
 			"ERROR -> The Left Hand Side is not a variable access. ");
 	static final GCLError EXPRESSION_REQUIRED = new Value(7,
 			"ERROR -> Expression required. ");
 	static final GCLError TOO_MANY_UNDERSCORES = new Value(8,
 			"ERROR -> Two or more underscores in a row. ");
 	static final GCLError OUT_OF_RANGE = new Value(9,
 			"ERROR -> Value is out of range. ");
 	static final GCLError RANGE_REQUIRED = new Value(10,
 			"ERROR -> Range type required. ");
 	static final GCLError MODULE_REQUIRED = new Value(11,
 			"ERROR -> Module required. ");
 	static final GCLError TUPLE_REQUIRED = new Value(12,
 			"ERROR -> TupleType required. ");
 	static final GCLError FIELD_NOT_FOUND = new Value(13,
 			"ERROR -> Tuple field not found. ");
 	static final GCLError PROCEDURE_REQUIRED = new Value(14,
 			"ERROR -> Procedure Required. ");
 	static final GCLError INVALID_RETURN = new Value(15,
 			"ERROR -> Procedure Required. ");
 	static final GCLError INCOMPATIBLE_TYPE = new Value(16,
 			"ERROR -> Incompatible types ");
 	static final GCLError BOOLEAN_REQUIRED = new Value(17,
 			"ERROR -> Boolean type required ");
 	static final GCLError INVALID_RANGE = new Value(18,
 	"ERROR -> Invalid range for the RangeType ");
 	static final GCLError CONSTANT_EXPRESSION = new Value(19,
 	"ERROR -> Cannot assign to constant expressions ");
 	static final GCLError PROCEDURE_NOT_DEFINED = new Value(20,
 	"ERROR -> This procedure was declared but never defined ");
 	static final GCLError INVALID_ARGUMENT = new Value(21,
 	"ERROR -> Procedure got invalid arguments ");
 	static final GCLError INVALID_LOAD = new Value(22,
 	"ERROR -> Procedure attempted to load an invalid value");
 
 
 	// The following are compiler errors. Repair them.
 	static final GCLError ILLEGAL_LOAD = new Value(91,
 			"COMPILER ERROR -> The expression is null. ");
 	static final GCLError NOT_A_POINTER = new Value(92,
 			"COMPILER ERROR -> LoadPointer saw a non-pointer. ");
 	static final GCLError ILLEGAL_MODE = new Value(93,
 			"COMPILER ERROR -> Sam mode out of range. ");
 	static final GCLError NO_REGISTER_AVAILABLE = new Value(94,
 			"COMPILER ERROR -> There is no available register. ");
 	static final GCLError ILLEGAL_LOAD_ADDRESS = new Value(95,
 			"COMPILER ERROR -> Attempt to LoadAddress not a variable. ");
 	static final GCLError ILLEGAL_LOAD_SIZE = new Value(96,
 			"COMPILER ERROR -> Attempt to load value with size > 4 bytes. ");
 	static final GCLError UNKNOWN_ENTRY = new Value(97,
 			"COMPILER ERROR -> An unknown entry was found. ");
 	static final GCLError INVALID_CASE = new Value(98,
 			"COMPILER ERROR -> Invalid case for array or tuple. ");
 
 	// More of each kind of error as you go along building the language.
 
 	public abstract int value();
 
 	public abstract String message();
 
 	static class Value extends GCLError {
 		private Value(int value, String msg) {
 			this.message = msg;
 			this.value = value;
 		}
 
 		public int value() {
 			return value;
 		}
 
 		public String message() {
 			return message;
 		}
 
 		private final int value;
 		private final String message;
 	}
 } // end GCLError
 
 // --------------------- SemanticActions ---------------------------------
 
 public class SemanticActions implements Mnemonic, CodegenConstants {
 
 	private final Codegen codegen;
 
 	static final IntegerType INTEGER_TYPE = IntegerType.INTEGER_TYPE;
 	static final BooleanType BOOLEAN_TYPE = BooleanType.BOOLEAN_TYPE;
 	static final TypeDescriptor NO_TYPE = ErrorType.NO_TYPE;
 
 	private Procedure currentProcedure;
 	private ModuleRecord currentModule;
 	private SemanticLevel currentLevel = new SemanticLevel();
 	private GCLErrorStream err = null;
 
 	SemanticActions(final Codegen codeGenerator, final GCLErrorStream err) {
 		this.codegen = codeGenerator;
 		codegen.setSemanticLevel(currentLevel());
 		this.err = err;
 		init();
 	}
 
 	/** Used to produce messages when an error occurs */
 	static class GCLErrorStream extends Errors { // Errors is defined in Parser
 		GCLErrorStream(final Scanner scanner) {
 			super(scanner);
 		}
 
 		void semanticError(final GCLError errNum) {
 			PrintWriter out = scanner.outFile();
 			out.print("At ");
 			semanticError(errNum.value(), scanner.currentToken().line(),
 					scanner.currentToken().column());
 			out.println(errNum.message());
 			out.println();
 			CompilerOptions.genHalt();
 		}
 
 		void semanticError(final GCLError errNum, final String extra) {
 			scanner.outFile().println(extra);
 			semanticError(errNum);
 		}
 	} // end GCLErrorStream
 
 	/***************************************************************
 	 * Auxiliary Determine if a symboltable entry can safely be redefined at
 	 * this point. Only one definition is legal in a given scope.
 	 * 
 	 * @param entry
 	 *            a symbol table entry to be checked.
 	 * @return true if it is ok to redefine this entry at this point.
 	 */
 	private boolean OKToRedefine(final SymbolTable.Entry entry) {
 		if (entry == SymbolTable.NULL_ENTRY) {
 			return true;
 		}
 		if (!(entry.identifier().name() == currentModule.getScope()
 				.lookupIdentifierCurrentScope(entry.identifier()).identifier()
 				.name()))
 			return true;
 		return false; // more later
 	}
 
 	/***************************************************************************
 	 * Auxiliary Report that the identifier is already defined in this scope if
 	 * it is. Called from most declarations.
 	 * 
 	 * @param ID
 	 *            an Identifier
 	 * @param scope
 	 *            the symbol table used to find the identifier.
 	 **************************************************************************/
 	private void complainIfDefinedHere(final SymbolTable scope,
 			final Identifier id) {
 		SymbolTable.Entry entry = scope.lookupIdentifierCurrentScope(id);
 		if (!OKToRedefine(entry)) {
 			err.semanticError(GCLError.ALREADY_DEFINED);
 		}
 	}
 
 	private void complainIfInvalidName(final Identifier id) {
 		if (id.name().contains("__")) {
 			err.semanticError(GCLError.TOO_MANY_UNDERSCORES);
 		}
 	}
 
 	/***************************************************************************
 	 * auxiliary moveBlock moves a block (using blocktransfer) from source to
 	 * dest. Both source and destination refer to expr entries .
 	 **************************************************************************/
 	private void moveBlock(final Expression source, final Expression destination) {
 		if (source instanceof ErrorExpression) {
 			return;
 		}
 		if (destination instanceof ErrorExpression) {
 			return;
 		}
 		int size = source.type().size();
 		int reg = codegen.getTemp(2); // need 2 registers for BKT
 		Codegen.Location sourceLocation = codegen.buildOperands(source);
 		codegen.gen2Address(LDA, reg, sourceLocation);
 		codegen.gen2Address(LD, reg + 1, IMMED, UNUSED, size);
 		sourceLocation = codegen.buildOperands(destination);
 		codegen.gen2Address(BKT, reg, sourceLocation);
 		codegen.freeTemp(DREG, reg);
 		codegen.freeTemp(sourceLocation);
 	}
 
 	/***************************************************************************
 	 * auxiliary moveBlock moves a block (using blocktransfer) from source to
 	 * dest. Source refers to an expr entry. mode, base, and displacement give
 	 * the dest.
 	 **************************************************************************/
 	private void moveBlock(final Expression source, final Codegen.Mode mode,
 			final int base, final int displacement) {
 		if (source instanceof ErrorExpression) {
 			return;
 		}
 		int size = source.type().size();
 		int reg = codegen.getTemp(2); // need 2 registers for BKT
 		Codegen.Location sourceLocation = codegen.buildOperands(source);
 		codegen.gen2Address(LDA, reg, sourceLocation);
 		codegen.gen2Address(LD, reg + 1, IMMED, UNUSED, size);
 		codegen.gen2Address(BKT, reg, mode, base, displacement);
 		codegen.freeTemp(DREG, reg);
 		codegen.freeTemp(sourceLocation);
 	}
 
 	/***************************************************************************
 	 * auxiliary moveBlock moves a block (using blocktransfer) from source to
 	 * destination. Source is given by mode, base, displacement and destination
 	 * refers to an expr entry .
 	 **************************************************************************/
 	private void moveBlock(final Codegen.Mode mode, final int base,
 			final int displacement, final Expression destination) {
 		if (destination instanceof ErrorExpression) {
 			return;
 		}
 		int size = destination.type().size();
 		int reg = codegen.getTemp(2); // need 2 registers for BKT
 		if (mode == IREG) {// already have an address
 			codegen.gen2Address(LD, reg, DREG, base, UNUSED);
 		} else {
 			codegen.gen2Address(LDA, reg, mode, base, displacement);
 		}
 		codegen.gen2Address(LD, reg + 1, IMMED, UNUSED, size);
 		Codegen.Location destinationLocation = codegen
 				.buildOperands(destination);
 		codegen.gen2Address(BKT, reg, destinationLocation);
 		codegen.freeTemp(DREG, reg);
 		codegen.freeTemp(destinationLocation);
 	}
 
 	/**
 	 * Set a bit of a word corresponding to a register number.
 	 * 
 	 * @param reg
 	 *            the register to transform
 	 * @return an integer with one bit set
 	 */
 	private int regToBits(final int reg) {
 		return (int) Math.pow(2, reg);
 	}
 
 	/**
 	 * auxiliary Push an expression onto the run time stack
 	 * 
 	 * @param source
 	 *            the expression to be pushed
 	 */
 	private void pushExpression(final Expression source) {
 		if (source.type().size() == INT_SIZE) {
 			int reg = codegen.loadRegister(source);
 			codegen.genPushRegister(reg);
 			codegen.freeTemp(DREG, reg);
 		} else { // blockmove
 			int size = source.type().size();
 			codegen.gen2Address(IS, STACK_POINTER, IMMED, UNUSED, size);
 			moveBlock(source, IREG, STACK_POINTER, UNUSED);
 		}
 	}
 
 	/**
 	 * **************** auxiliary Pop an expression from the run time stack into
 	 * a given destination
 	 * 
 	 * @param destination
 	 *            the destination for the pop
 	 */
 	private void popExpression(final Expression destination) {
 		if (destination.type().size() == INT_SIZE) {
 			int reg = codegen.getTemp(1);
 			codegen.genPopRegister(reg);
 			Codegen.Location destinationLocation = codegen
 					.buildOperands(destination);
 
 			// if (destination.type() instanceof RangeType){
 			// codegen.gen2Address(TRNG, reg, ((RangeType)
 			// destination.type()).getLowBoundOffset());
 			// }
 
 			codegen.gen2Address(STO, reg, destinationLocation);
 			codegen.freeTemp(DREG, reg);
 			codegen.freeTemp(destinationLocation);
 		} else { // blockmove
 			moveBlock(IREG, STACK_POINTER, UNUSED, destination);
 			codegen.gen2Address(IA, STACK_POINTER, IMMED, UNUSED, destination
 					.type().size());
 		}
 	}
 
 	/**
 	 * auxiliary Move the value of an expression from its source to a
 	 * destination
 	 * 
 	 * @param source
 	 *            the source of the expression
 	 * @param destination
 	 *            the destination to which to move the value
 	 */
 	private void simpleMove(final Expression source,
 			final Expression destination) {
 		if (destination.type().size() == INT_SIZE) {
 			int reg = codegen.loadRegister(source);
 			Codegen.Location destinationLocation = codegen
 					.buildOperands(destination);
 			codegen.gen2Address(STO, reg, destinationLocation);
 			codegen.freeTemp(DREG, reg);
 			codegen.freeTemp(destinationLocation);
 		} else {
 			moveBlock(source, destination);
 		}
 	}
 
 	/**
 	 * **************** auxiliary Move the value of an expression from a source
 	 * to a destination
 	 * 
 	 * @param source
 	 *            the source of the move
 	 * @param mode
 	 *            the mode of the destination's location
 	 * @param base
 	 *            the base of the destination location
 	 * @param displacement
 	 *            the displacement of the destination location
 	 */
 	private void simpleMove(final Expression source, final Codegen.Mode mode,
 			final int base, final int displacement) {
 		if (source.type().size() == INT_SIZE) {
 			int reg = codegen.loadRegister(source);
 			codegen.gen2Address(STO, reg, mode, base, displacement);
 			codegen.freeTemp(DREG, reg);
 			codegen.freeTemp(mode, base);
 		} else {
 			moveBlock(source, mode, base, displacement);
 		}
 	}
 
 	/***************************************************************************
 	 * Transform an identifier into the semantic item that it represents
 	 * 
 	 * @param scope
 	 *            the current scope
 	 * @param ID
 	 *            and identifier to be transformed
 	 * @return the semantic item that the identifier represents.
 	 */
 	SemanticItem semanticValue(final SymbolTable scope, final Identifier id) {
 		SymbolTable.Entry symbol = scope.lookupIdentifier(id);
 		if (symbol == SymbolTable.NULL_ENTRY) {
 			err.semanticError(GCLError.NAME_NOT_DEFINED);
 			return new SemanticError("Identifier not found in symbol table.");
 		} else {
 			return symbol.semanticRecord();
 		}
 	}
 
 	/***************************************************************************
 	 * Transform an identifier into the semantic item that it represents
 	 * 
 	 * @param scope
 	 *            the current scope
 	 * @param ID
 	 *            and identifier of the module
 	 * @param module
 	 *            the identifier to be transformed
 	 * @return the semantic item that the identifier represents.
 	 */
 	SemanticItem semanticValue(final SymbolTable scope,
 			final Identifier moduleID, final Identifier id) {
 		SymbolTable.Entry module = currentModule.getScope().lookupIdentifier(
 				moduleID);
 		if (!(module.semanticRecord() instanceof ModuleRecord)) {
 			err.semanticError(GCLError.MODULE_REQUIRED);
 			return new SemanticError("Identifier expected to be a module");
 		}
 		if (((ModuleRecord) module.semanticRecord()).getID().name() == currentModule
 				.getID().name())
 			return semanticValue(scope, id);
 
 		SymbolTable.Entry symbol = ((ModuleRecord) module.semanticRecord())
 				.getScope().lookupIdentifier(id);
 		if (symbol == SymbolTable.NULL_ENTRY) {
 			err.semanticError(GCLError.NAME_NOT_DEFINED);
 			return new SemanticError("Identifier not found in symbol table.");
 		} else {
 			return symbol.semanticRecord();
 		}
 	}
 
 	/***************************************************************************
 	 * Generate code for an assignment. Copy the RHS expressions to the
 	 * corresponding LHS variables.
 	 * 
 	 * @param expressions
 	 *            an assignment record with two expr vectors (RHSs, LHSs )
 	 **************************************************************************/
 	void parallelAssign(final AssignRecord expressions) {
 		int i;
 		// part 1. checks and optimizations
 		// Shouldn't put checks here otherwise it wouldn't recover from errors
 		if (!expressions.verify(err)) {
 			return;
 		}
 		int entries = expressions.size(); // number of entries to process
 		if (CompilerOptions.optimize && entries == 1) { // whatever
 		} // optimizations possible
 			// part 2. pushing except consts, temps, and stackvariables
 		for (i = 0; i < entries; ++i) {
 			Expression rightExpression = expressions.right(i);
 			if (rightExpression.needsToBePushed()) {
 				pushExpression(rightExpression);
 			}
 		}
 		// part 3. popping the items pushed in part 2 & copying the rest
 		for (i = entries - 1; i >= 0; --i) {
 			Expression rightExpression = expressions.right(i);
 			Expression leftExpression = expressions.left(i);
 
 			//Error checks
 			if (!(leftExpression.type().isCompatible(rightExpression.type()))) {
 				err.semanticError(GCLError.INCOMPATIBLE_TYPE, "Got: " + leftExpression.type() + " Expected: " + rightExpression.type());
 			}
 			
 			if (leftExpression instanceof ConstantExpression){
 				err.semanticError(GCLError.CONSTANT_EXPRESSION);
 			}
 				
 			if (leftExpression.type() instanceof RangeType) {
 			
 			
 				if (rightExpression instanceof ConstantExpression)
 					if (((RangeType) leftExpression.type()).getLowBound() <= ((ConstantExpression) rightExpression)
 							.value()
 							&& ((RangeType) leftExpression.type())
 									.getHighBound() >= ((ConstantExpression) rightExpression)
 									.value()) {
 					} else {
 						err.semanticError(GCLError.OUT_OF_RANGE);
 						return;
 					}
 				else {
 					int reg = codegen.loadRegister(rightExpression);
 					codegen.gen2Address(TRNG, reg, ((RangeType) leftExpression
 							.type()).getLowBoundOffset());
 				}
 			}
 	
 			if (rightExpression.needsToBePushed()) {
 				popExpression(leftExpression);
 			} else { // the item wasn't pushed, so normal copy
 				simpleMove(rightExpression, leftExpression);
 	
 			}
 		}
 	}
 
 	/***************************************************************************
 	 * Generate code to read into an integer variable. (Must be an assignable
 	 * variable)
 	 * 
 	 * @param expression
 	 *            (integer variable) expression
 	 **************************************************************************/
 	void readVariable(final Expression expression) {
 		if (expression instanceof GeneralError) {
 			return;
 		}
 
 		if (!expression.type().isCompatible(INTEGER_TYPE)) {
 			err.semanticError(GCLError.INTEGER_REQUIRED, "   while Reading");
 			return;
 		}
 		Codegen.Location expressionLocation = codegen.buildOperands(expression);
 		codegen.gen1Address(RDI, expressionLocation);
 		if (expression.type() instanceof RangeType) {
 			int reg = codegen.loadRegister(expression);
 			codegen.gen2Address(TRNG, reg,
 					((RangeType) expression.type()).getLowBoundOffset());
 		}
 	}
 
 	/***************************************************************************
 	 * Generate code to write an integer expression.
 	 * 
 	 * @param expression
 	 *            (integer) expression
 	 **************************************************************************/
 	void writeExpression(final Expression expression) {
 		if (expression instanceof GeneralError) {
 			return;
 		}
 		if (!expression.type().isCompatible(INTEGER_TYPE)) {
 			err.semanticError(GCLError.INTEGER_REQUIRED, "   while Writing");
 			return;
 		}
 		Codegen.Location expressionLocation = codegen.buildOperands(expression);
 		codegen.gen1Address(WRI, expressionLocation);
 		codegen.freeTemp(expressionLocation);
 	}
 
 	/***************************************************************************
 	 * Generate code to write a string constant.
 	 * 
 	 * @param String
 	 *            message
 	 **************************************************************************/
 	void writeStringConstant(StringConstant message) {
 		if (message instanceof GeneralError) {
 			return;
 		}
 		Codegen.Location stringLocation = codegen.buildOperands(message);
 		codegen.gen1Address(WRST, stringLocation);
 	}
 
 	/***************************************************************************
 	 * Generate code to write an end of line mark.
 	 **************************************************************************/
 	void genEol() {
 		codegen.gen0Address(WRNL);
 	}
 
 	/***************************************************************************
 	 * Generate code to add two integer expressions. Result in Register.
 	 * 
 	 * @param left
 	 *            an expression (lhs)Must be integer
 	 * @param op
 	 *            an add operator
 	 * @param right
 	 *            an expression (rhs)Must be integer
 	 * @return result expression -integer (in register)
 	 **************************************************************************/
 	Expression addExpression(final Expression left, final AddOperator op,
 			final Expression right) {
 		//Error Check
 		if (!(left.type().isCompatible(right.type()))) {
 			err.semanticError(GCLError.INCOMPATIBLE_TYPE);
 		}
 		
 		if (left instanceof ConstantExpression
 				&& right instanceof ConstantExpression)
 			return op.foldConstant((ConstantExpression) left,
 					(ConstantExpression) right);
 		int reg = codegen.loadRegister(left);
 		Codegen.Location rightLocation = codegen.buildOperands(right);
 		codegen.gen2Address(op.opcode(), reg, rightLocation);
 		codegen.freeTemp(rightLocation);
 		return new VariableExpression(INTEGER_TYPE, reg, DIRECT); // temporary
 	}
 
 	/***************************************************************************
 	 * Generate code to negate an integer expression. Result in Register.
 	 * 
 	 * @param expression
 	 *            expression to be negated -must be integer
 	 * @return result expression -integer (in register)
 	 **************************************************************************/
 	Expression negateExpression(final Expression expression) {
 		//Error Check
 		if (!(expression.type() instanceof IntegerType)) {
 			err.semanticError(GCLError.INCOMPATIBLE_TYPE);
 		}
 		if (expression instanceof ConstantExpression)
 			return negateConstant((ConstantExpression) expression);
 		Codegen.Location expressionLocation = codegen.buildOperands(expression);
 		int reg = codegen.getTemp(1);
 		codegen.gen2Address(INEG, reg, expressionLocation);
 		codegen.freeTemp(expressionLocation);
 		return new VariableExpression(INTEGER_TYPE, reg, DIRECT); // temporary
 	}
 
 	Expression booleanNegate(final Expression expression) {
 		//Error Check
 		if (!(expression.type() instanceof BooleanType)) {
 			err.semanticError(GCLError.INCOMPATIBLE_TYPE);
 		}
 		Codegen.Location expressionLocation = codegen.buildOperands(expression);
 		int bnreg = codegen.getTemp(1);
 		codegen.gen2Address(LD, bnreg, IMMED, UNUSED, 1);
 		codegen.gen2Address(IS, bnreg, expressionLocation);
 		codegen.freeTemp(expressionLocation);
 		return new VariableExpression(BOOLEAN_TYPE, bnreg, DIRECT); // temporary
 	}
 
 	ConstantExpression negateConstant(final ConstantExpression expression) {
 		return new ConstantExpression(INTEGER_TYPE, -1 * expression.value());
 	}
 
 	Expression modulusExpression(final Expression left, final Expression right) {
 		//Error Check
 		if (!(left.type().isCompatible(right.type()))) {
 			err.semanticError(GCLError.INCOMPATIBLE_TYPE);
 		}
 		int reg1 = codegen.loadRegister(left);
 		Codegen.Location rightLocation = codegen.buildOperands(right);
 		codegen.buildOperands(left);
 		int reg2 = codegen.getTemp(1);
 		codegen.gen2Address(LD, reg2, DREG, reg1, 1);
 		codegen.gen2Address(ID, reg1, rightLocation);
 		codegen.gen2Address(IM, reg1, rightLocation);
 		codegen.gen2Address(IS, reg2, DREG, reg1, 1);
 		codegen.freeTemp(DREG, reg1);
 		codegen.freeTemp(rightLocation);
 		codegen.freeTemp(DREG, reg2);
 		return new VariableExpression(INTEGER_TYPE, reg2, DIRECT); // temporary
 	}
 
 	/***************************************************************************
 	 * Generate code to multiply two integer expressions. Result in Register.
 	 * 
 	 * @param left
 	 *            an expression (lhs)Must be integer
 	 * @param op
 	 *            a multiplicative operator
 	 * @param right
 	 *            an expression (rhs)Must be integer
 	 * @return result expression -integer (in register)
 	 **************************************************************************/
 	Expression multiplyExpression(final Expression left,
 			final MultiplyOperator op, final Expression right) {
 		//Error Check
 		if (!(left.type().isCompatible(right.type()))) {
 			err.semanticError(GCLError.INCOMPATIBLE_TYPE);
 		}
 		if (left instanceof ConstantExpression
 				&& right instanceof ConstantExpression)
 			return op.foldConstant((ConstantExpression) left,
 					(ConstantExpression) right);
 		int reg;
 		if (op.equals(MultiplyOperator.MODULUS)) {
 			return modulusExpression(left, right);
 		} else {
 			reg = codegen.loadRegister(left);
 			Codegen.Location rightLocation = codegen.buildOperands(right);
 			codegen.gen2Address(op.opcode(), reg, rightLocation);
 			codegen.freeTemp(rightLocation);
 		}
 		return new VariableExpression(INTEGER_TYPE, reg, DIRECT); // temporary
 	}
 
 	Expression booleanExpression(final Expression left,
 			final BooleanOperator op, final Expression right) {
 		//Error Check
 		if (!(left.type().isCompatible(right.type()))) {
 			err.semanticError(GCLError.INCOMPATIBLE_TYPE);
 		}
 		if (left instanceof ConstantExpression
 				&& right instanceof ConstantExpression)
 			return op.foldConstant((ConstantExpression) left,
 					(ConstantExpression) right);
 		int reg = codegen.loadRegister(left);
 		Codegen.Location rightLocation = codegen.buildOperands(right);
 		codegen.gen2Address(op.opcode(), reg, rightLocation);
 		codegen.freeTemp(rightLocation);
 		return new VariableExpression(BOOLEAN_TYPE, reg, DIRECT); // temporary
 	}
 
 	/***************************************************************************
 	 * Generate code to compare two expressions. Result (0-1) in Register.
 	 * 
 	 * @param left
 	 *            an expression (lhs)
 	 * @param op
 	 *            a relational operator
 	 * @param right
 	 *            an expression (rhs)
 	 * @return result expression -0(false) or 1(true) (in register)
 	 **************************************************************************/
 	Expression compareExpression(final Expression left,
 			final RelationalOperator op, final Expression right) {
 		//Error Check
 		if (!(left.type().isCompatible(right.type()))) {
 			err.semanticError(GCLError.INCOMPATIBLE_TYPE);
 		}
 		if (left instanceof ConstantExpression
 				&& right instanceof ConstantExpression)
 			return op.foldConstant((ConstantExpression) left,
 					(ConstantExpression) right);
 		int booleanreg = codegen.getTemp(1);
 		int resultreg = codegen.loadRegister(left);
 		Codegen.Location rightLocation = codegen.buildOperands(right);
 		codegen.gen2Address(LD, booleanreg, IMMED, UNUSED, 1);
 		codegen.gen2Address(IC, resultreg, rightLocation);
 		codegen.gen1Address(op.opcode(), PCREL, UNUSED, 4);
 		codegen.gen2Address(LD, booleanreg, IMMED, UNUSED, 0);
 		codegen.freeTemp(DREG, resultreg);
 		codegen.freeTemp(rightLocation);
 		return new VariableExpression(BOOLEAN_TYPE, booleanreg, DIRECT); // temporary
 	}
 
 	/***************************************************************************
 	 * Create a label record with the outlabel for an IF statement.
 	 * 
 	 * @return GCRecord entry with two label slots for this statement.
 	 **************************************************************************/
 	GCRecord startIf() {
 		return new GCRecord(codegen.getLabel(), 0);
 	}
 
 	GCRecord startDo() {
 		int record = codegen.getLabel();
 		codegen.genLabel('J', record);
 		return new GCRecord(record, 0);
 	}
 
 	int startFor(Expression exp) {
 		if (!(exp.type() instanceof RangeType)) {
 			err.semanticError(GCLError.RANGE_REQUIRED);
 			return 0;
 		}
 		int reg = codegen.getTemp(1);
 		int startLabel = codegen.getLabel();
 		codegen.gen2Address(LD, reg, IMMED, UNUSED,
 				((RangeType) exp.type()).getLowBound());
 		Codegen.Location expressionLocation = codegen.buildOperands(exp);
 		codegen.gen2Address(STO, reg, expressionLocation);
 		codegen.genLabel('F', startLabel);
 		codegen.freeTemp(DREG, reg);
 		return startLabel;
 	}
 
 	void endFor(Expression exp, int startLabel) {
 		if (!(exp.type() instanceof RangeType)) {
 			err.semanticError(GCLError.RANGE_REQUIRED);
 			return;
 		}
 		if (exp.type() instanceof ArrayType) {
 			Codegen.Location arrayLocation = codegen.buildOperands(exp);
 			codegen.freeTemp(arrayLocation);
 		}
 		int reg = codegen.loadRegister(exp);
 		Codegen.Location expressionLocation = codegen.buildOperands(exp);
 		int endLabel = codegen.getLabel();
 		codegen.gen2Address(IC, reg, IMMED, UNUSED,
 				((RangeType) exp.type()).getHighBound());
 		codegen.genJumpLabel(JEQ, 'F', endLabel);
 		codegen.gen2Address(IA, reg, IMMED, UNUSED, 1);
 		codegen.gen2Address(STO, reg, expressionLocation);
 		codegen.genJumpLabel(JMP, 'F', startLabel);
 		codegen.genLabel('F', endLabel);
 		codegen.freeTemp(DREG, reg);
 
 	}
 
 	/***************************************************************************
 	 * Generate the final label for an IF. (Halt of we fall through to here).
 	 * 
 	 * @param entry
 	 *            GCRecord holding the labels for this statement.
 	 **************************************************************************/
 	void endIf(final GCRecord entry) {
 		codegen.gen0Address(HALT);
 		codegen.genLabel('J', entry.outLabel());
 	}
 
 	/***************************************************************************
 	 * If the expr represents true, jump to the next else part.
 	 * 
 	 * @param expression
 	 *            Expression to be tested: must be boolean
 	 * @param entry
 	 *            GCRecord with the associated labels. This is updated
 	 **************************************************************************/
 	void ifTest(final Expression expression, final GCRecord entry, final GCLErrorStream err) {
 		if(expression.type().isCompatible(BOOLEAN_TYPE))
 		{
 			int resultreg = codegen.loadRegister(expression);
 			int nextElse = codegen.getLabel();
 			entry.nextLabel(nextElse);
 			codegen.gen2Address(IC, resultreg, IMMED, UNUSED, 1);
 			codegen.genJumpLabel(JNE, 'J', nextElse);
 			codegen.freeTemp(DREG, resultreg);
 		} else {
 			err.semanticError(GCLError.BOOLEAN_REQUIRED, "if statements must be booleans");
 		}
 	}
 
 	/***************************************************************************
 	 * Generate a jump to the out label and insert the next else label.
 	 * 
 	 * @param entry
 	 *            GCRecord with the labels
 	 **************************************************************************/
 	void elseIf(final GCRecord entry) {
 		codegen.genJumpLabel(JMP, 'J', entry.outLabel());
 		codegen.genLabel('J', entry.nextLabel());
 	}
 
 	/*void terminateArray(final Expression exp) {
 
 		if (exp.type() instanceof ArrayType) {
 			if (((RangeType) ((ArrayType) exp.type()).getSubscriptType()
 					.expectType(err)).baseType() instanceof ErrorType) {
 				return;
 			}
 			terminateArray(new VariableExpression(
 					((ArrayType) exp.type()).getComponentType(),
 					((VariableExpression) exp).offset() - 1, INDIRECT));
 			codegen.freeTemp(DREG, ((VariableExpression) exp).offset());
 		}
 	}*/
 
 	/***************************************************************************
 	 * Create a tuple from a list of expressions Both the type and the value
 	 * must be created.
 	 * 
 	 * @param tupleFields
 	 *            an expression list with the fields of the tuple
 	 * @return an expression representing the tuple value as a whole.
 	 **************************************************************************/
 	Expression buildTuple(final ExpressionList tupleFields) {
 		Iterator<Expression> elements = tupleFields.elements();
 		TypeList items = new TypeList();
 		int address = codegen.variableBlockSize(); // beginning of the tuple
 		while (elements.hasNext()) {
 			Expression field = elements.next();
 			TypeDescriptor aType = field.type();
 			items.enter(aType);
 			int size = aType.size();
 			int where = codegen.reserveGlobalAddress(size);
 			CompilerOptions.message("Tuple component of size " + size + " at "
 					+ where);
 			// Now bring all the components together into a contiguous block
 			simpleMove(field, INDXD, VARIABLE_BASE, where);
 		}
 		TupleType tupleType = new TupleType(items);
 		return new VariableExpression(tupleType, GLOBAL_LEVEL, address, DIRECT);
 	}
 
 	public TypeDescriptor buildRange(TypeDescriptor baseType,
 			ConstantExpression lowBound, ConstantExpression highBound) {
 		if(lowBound.value() < 0 || lowBound.value() > highBound.value()) {
 			err.semanticError(GCLError.INVALID_RANGE, "Ranges cannot be negative and the highBound must be larger than the lowBound");
 			return NO_TYPE;
 		}
 		Codegen.Location lowBoundOffset = codegen.buildOperands(lowBound);
 		codegen.buildOperands(highBound);
 		return new RangeType(baseType, lowBound.value(), highBound.value(),
 				lowBoundOffset);
 	}
 
 	public ArrayType buildArray(TypeDescriptor baseType, ArrayCarrier carrier,
 			final SemanticActions.GCLErrorStream err) {
 		if (carrier.getSize() == 0) {
 			return new ArrayType(baseType, new RangeType(NO_TYPE, 0, 0, null),
 					err);
 		}
 		if (carrier.getSize() > 1) {
 			ArrayType temp = new ArrayType(baseType, carrier.pop(), err);
 			return buildArray(temp, carrier, err);
 		}
 
 		return new ArrayType(baseType, carrier.pop(), err);
 	}
 
 	public void declareModule(final SymbolTable scope, final Identifier id) {
 		ModuleRecord tempModule = new ModuleRecord(scope, id, codegen);
 		codegen.genJumpLabel(JMP, 'M', tempModule.getLabel());
 		scope.newEntry("module", id, tempModule, currentModule);
 		currentModule = tempModule;
 	}
 
 	public void declarePrivateScope(final SymbolTable scope) {
 		currentModule.setPrivateScope(scope);
 	}
 
 	public Procedure declareProcedure(SymbolTable scope, Identifier id) {
 		currentLevel().increment();
 		SymbolTable newScope = scope.openScope(true);
 		currentProcedure = new Procedure(currentProcedure, id, newScope,
 				codegen);
 		return currentProcedure;
 	}
 
 	public void endDeclareProcedure() {
 		currentLevel().decrement();
 		currentProcedure = currentProcedure.getParentProcedure();
 	}
 
 	public Procedure defineProcedure(Identifier procName,
 			SemanticItem tupleObject) {
 		Procedure proc = null;
 		TupleType procsTuple = null;
 
 		if (tupleObject instanceof Expression) {
 			tupleObject = ((Expression) tupleObject).type();
 		}
 
 		if (tupleObject instanceof TupleType) {
 			procsTuple = ((TupleType) tupleObject);
 		} else {
 			err.semanticError(GCLError.TYPE_REQUIRED, "TupleType required");
 		}
 		
 		proc = procsTuple.getProcedure(procName);
 		if (proc == null) {
 			err.semanticError(GCLError.PROCEDURE_REQUIRED);
 			return null;
 		}
 		if (proc.alreadyDefined()) {
 			err.semanticError(GCLError.ALREADY_DEFINED,
 					"This procedure has already been defined for this tuple.");
 			return null;
 		}
 
 
 		currentLevel().increment();
 		currentProcedure = proc;
 
 		return proc;
 	}
 
 	public void endDefineProcedure(Procedure proc) {
 		if (proc == null)
 			return;
 		proc.genUnlink(codegen);
 		proc.getScope().closeScope();
 		currentProcedure = proc.getParentProcedure();
 		currentLevel().decrement();
 	}
 
 	void doLink() {
 		if (currentLevel().isGlobal()) {
 			codegen.genLabel('M', currentModule.getLabel());
 		} else {
 			currentProcedure.genLink(codegen);
 		}
 	}
 
 	void callProcedure(Expression tupleExpression, Identifier procedureName, ExpressionList arguments) {
 		TupleType tuple = null;
 		if (tupleExpression.type() instanceof TupleType) {
 			tuple = (TupleType) tupleExpression.type();
 
 			Procedure procedure = tuple.getProcedure(procedureName);
 			if (procedure != null) {
 				if (!(procedure.alreadyDefined())){
 					err.semanticError(GCLError.PROCEDURE_NOT_DEFINED); 
 					return;
 				}
 				
 				int thisRegister = codegen.loadAddress(tupleExpression);
 				codegen.gen2Address(IS, Codegen.STACK_POINTER, Codegen.IMMED,
 						Codegen.UNUSED, procedure.frameSize());
 				codegen.gen2Address(STO, thisRegister, new Codegen.Location(
 						Codegen.INDXD, Codegen.STACK_POINTER, +6));
 				codegen.freeTemp(DREG, thisRegister);
 
 				int diff = currentLevel().value() - procedure.semanticLevel();
 				Codegen.Location persistedStaticInNewFrame = new Codegen.Location(
 						Codegen.INDXD, Codegen.STACK_POINTER, +2);
 				if (diff <= 0) {
 					codegen.gen2Address(STO, Codegen.FRAME_POINTER,
 							persistedStaticInNewFrame);
 				} else if (diff == 1) {
 					codegen.gen2Address(STO, Codegen.STATIC_POINTER,
 							persistedStaticInNewFrame);
 				} else if (diff > 1) {
 					for (int i = 0; i < diff - 1; i++) {
 						codegen.gen2Address(LD, Codegen.STATIC_POINTER,
 								new Codegen.Location(INDXD,
 										Codegen.STATIC_POINTER, +2));
 					}
 					codegen.gen2Address(STO, Codegen.STATIC_POINTER,
 							persistedStaticInNewFrame);
 				} else {
 					// err.semanticError(GCLError.UNHANDLED_CASE,
 					// "Corrupt leveling scheme.");
 				}
 
 				procedure.call(arguments, codegen, err);
 				codegen.genJumpSubroutine(Codegen.STATIC_POINTER,
 						procedure.getLabel());
 				codegen.gen2Address(LD, Codegen.STATIC_POINTER,
 						new Codegen.Location(Codegen.INDXD,
 								Codegen.FRAME_POINTER, +2));
 				codegen.gen2Address(IA, Codegen.STACK_POINTER, Codegen.IMMED,
 						Codegen.UNUSED, procedure.frameSize());
 			}
 		}
 	}
 	
 	Expression resolveThis(){
 		return new VariableExpression(currentProcedure.getParentTuple(), currentProcedure.getLevel(), +6, Codegen.INDIRECT);
 		//return currentProcedure.thisTupleExpression();
 		}
 
 	void doReturn() {
 		if (currentLevel().isGlobal()) {
 			err.semanticError(GCLError.INVALID_RETURN,
 					"returns cannot exist in a global level");
 			return;
 		}
 		int procedureLabel = currentProcedure.getLabel();
 		codegen.genJumpLabel(JMP, 'U', procedureLabel);
 	}
 
 	public Expression subscriptAction(Expression array, Expression subscript) {
 		if((array.type() instanceof ErrorType) || (subscript.type() instanceof ErrorType))
 		{
 			return new ErrorExpression("Either the array or the subscript is an error");
 		}
 		
 		if (!(array.type() instanceof ArrayType)){
 			 err.semanticError(GCLError.TYPE_REQUIRED); 
 			 return new ErrorExpression("ArrayType Required"); }
 		 
 		if (!(((ArrayType) array.type()).getSubscriptType().isCompatible(subscript.type()))){
 			err.semanticError(GCLError.TYPE_REQUIRED, "Expected: " + ((ArrayType) array.type()).getSubscriptType());
 			return new ErrorExpression("Incompatible Types");
 		}
 		
 		ArrayType arrayType = ((ArrayType) array.type());
 		RangeType subscriptType = (RangeType) arrayType.getSubscriptType();
 
 		int arrayReg = codegen.loadAddress(array);
 		int subscriptReg = codegen.loadRegister(subscript);
 
 		codegen.gen2Address(TRNG, subscriptReg, subscriptType.getLowBoundOffset());
 		codegen.gen2Address(IS, subscriptReg, IMMED, UNUSED, subscriptType.getLowBound());
 		codegen.gen2Address(IM,subscriptReg,IMMED,UNUSED,((TypeDescriptor) ((ArrayType) array.type()).getComponentType()).size());
 		codegen.gen2Address(IA, arrayReg, DREG, subscriptReg, UNUSED);
 		codegen.freeTemp(DREG, subscriptReg);
 		return new VariableExpression(arrayType.getComponentType(), CPU_LEVEL, arrayReg, INDIRECT);
 	}
 
 	public Expression extractField(Expression exp, Identifier id) {
 		if (exp instanceof ErrorExpression)
 			return exp;
 		if (!(exp.type() instanceof TupleType)) {
 			err.semanticError(GCLError.TUPLE_REQUIRED);
 			return new ErrorExpression("TupleType Required");
 		}
 		if (((TupleType) exp.type()).getType(id) == null) {
 			err.semanticError(GCLError.FIELD_NOT_FOUND);
 			return new ErrorExpression("Tuple field not found");
 		}
 		
 		int inset = ((TupleType) exp.type()).getInset(id);
 		TypeDescriptor type = ((TupleType) exp.type()).getType(id);
 		
 		if (exp.semanticLevel() > 0 && ((VariableExpression) exp).isDirect())
 			return new VariableExpression(type, GLOBAL_LEVEL, ((VariableExpression) exp).offset() + inset, DIRECT);
 		if (exp.semanticLevel() == 0 && !((VariableExpression) exp).isDirect()) {
 			int reg = codegen.loadPointer(exp);
 			codegen.gen2Address(IA, reg, IMMED, UNUSED, inset);
 			return new VariableExpression(type, exp.semanticLevel(), reg, INDIRECT);
 		}
 		
 		if (exp.semanticLevel() > 0 && !((VariableExpression) exp).isDirect()) {
 			int reg = codegen.loadPointer(exp);
 			codegen.gen2Address(IA, reg, IMMED, UNUSED, inset);
 			return new VariableExpression(type, CPU_LEVEL, reg, INDIRECT);
 		}
 		
 		err.semanticError(GCLError.INVALID_CASE);
 		return new ErrorExpression("Invalid case for this tuple");
 	}
 
 	/***************************************************************************
 	 * Enter the identifier into the symbol table, marking it as a variable of
 	 * the given type. This method handles global variables as well as local
 	 * variables and procedure parameters.
 	 * 
 	 * @param scope
 	 *            the current symbol table
 	 * @param type
 	 *            the type to be of the variable being defined
 	 * @param ID
 	 *            identifier to be defined
 	 * @param procParam
 	 *            the kind of procedure param it is (if any).
 	 **************************************************************************/
 	void declareVariable(final SymbolTable scope, final TypeDescriptor type,
 			final Identifier id, final ParameterKind procParam) {
 		complainIfDefinedHere(scope, id);
 		complainIfInvalidName(id);
 		Expression expr = null;
 		if (currentLevel().isGlobal()) { // Global variable
 			int addressOffset = codegen.reserveGlobalAddress(type.size());
 			expr = new VariableExpression(type, currentLevel().value(),
 					addressOffset, DIRECT);
 		} else { // may be param or local in a proc
 			if (procParam == ParameterKind.NOT_PARAM) {
 				expr = currentProcedure.reserveLocalAddress(type);
 			} else {
 				expr = currentProcedure.reserveParameterAddress(type, procParam, err);
 			}
 		}
 		SymbolTable.Entry variable = scope.newEntry("variable", id, expr,
 				currentModule);
 		CompilerOptions.message("Entering: " + variable);
 	}
 
 	void declareConstant(final SymbolTable scope, final Identifier id,
 			Expression expr) {
 		complainIfDefinedHere(scope, id);
 		complainIfInvalidName(id);
 		SymbolTable.Entry constantVariable = scope.newEntry("constant", id,
 				expr, currentModule);
 		CompilerOptions.message("Entering: " + constantVariable);
 	}
 
 	void declareType(final SymbolTable scope, final Identifier id,
 			TypeDescriptor type) {
 		complainIfDefinedHere(scope, id);
 		complainIfInvalidName(id);
 		SymbolTable.Entry constantVariable = scope.newEntry("typedefinition",
 				id, type, currentModule);
 		CompilerOptions.message("Entering: " + constantVariable);
 	}
 
 	/***************************************************************************
 	 * Set up the registers and other run time initializations.
 	 **************************************************************************/
 	void startCode() {
 		codegen.genCodePreamble();
 	}
 
 	/***************************************************************************
 	 * Write out the termination code, Including constant defs and global
 	 * variables.
 	 **************************************************************************/
 	void finishCode() {
 		codegen.genCodePostamble();
 	}
 
 	/**
 	 * Get a reference to the object that maintains the current semantic
 	 * (procedure nesting) level.
 	 * 
 	 * @return the current semantic level object.
 	 */
 	SemanticLevel currentLevel() {
 		return currentLevel;
 	}
 
 	/**
 	 * Objects of this class represent the semantic level at which the compiler
 	 * is currently translating. The global level is the level of modules. Level
 	 * 2 is the level of procedures. Level 3... are the levels of nested
 	 * procedures. Each item declared at a level is tagged with the level number
 	 * at its declaration. These numbers are used by the compiler to set up the
 	 * runtime so that non-local variables (and other items) can be found at
 	 * runtime.
 	 */
 	static class SemanticLevel {
 		private int currentLevel = GLOBAL_LEVEL;// Never less than one. Current
 
 		// procedure nest level
 
 		/**
 		 * The semantic level's integer value
 		 * 
 		 * @return the semantic level as an int. Never less than one.
 		 */
 		public int value() {
 			return currentLevel;
 		}
 
 		/**
 		 * Determine if the semantic level represents the global (i.e. 1) level.
 		 * 
 		 * @return true if the level is global. False if it is procedural at any
 		 *         level.
 		 */
 		public boolean isGlobal() {
 			return currentLevel == GLOBAL_LEVEL;
 		}
 
 		private void increment() {
 			currentLevel++;
 		}
 
 		private void decrement() {
 			currentLevel--;
 		}
 
 		private SemanticLevel() {
 			// nothing.
 		}
 		// can create a new object only within the containing class
 	}
 
 	GCLErrorStream err() {
 		return err;
 	}
 
 	public final void init() {
 		currentLevel = new SemanticLevel();
 		codegen.setSemanticLevel(currentLevel());
 	}
 
 }// SemanticActions
