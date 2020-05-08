 package util.type;
 
 import util.ast.node.ArgumentsNode;
 import util.ast.node.BiOpNode;
 import util.ast.node.ExceptionTypeNode;
 import util.ast.node.FunctionNode;
 import util.ast.node.PrimitiveTypeNode;
 import util.ast.node.DerivedTypeNode;
 import util.ast.node.DictTypeNode;
 import util.ast.node.TypeNode;
 import util.ast.node.UnOpNode;
import util.error.TypeMismatchError;
 
 /**
  * A convenience class for defining and manipulating internal type
  * representations.
  * 
  * Types are represented by TypeNode classes {DerivedTypeNode, DictTypeNode,
  * ExceptionTypeNode, PrimitiveTypeNode}, in conjunction with the static enums
  * Primitive, Derived and Exception defined by this class.
  * 
  * @author sam
  * 
  */
 public class Types {
 
 	/**
 	 * Primitive types in hog.
 	 * 
 	 * The valid primitive types are:
 	 * <ul>
 	 * <li>BOOL : a boolean</li>
 	 * <li>TEXT : a string, wrapped in single- or double-quotes.</li>
 	 * <li>VOID : the null type</li>
 	 * <li>INT : an integer</li>
 	 * <li>REAL : a real number (double)</li>
 	 * <li>CHECK_INNER_TYPE: !TODO! WHAT AM I?</li>
 	 * </ul>
 	 * 
 	 * @author sam
 	 * 
 	 */
 	public static enum Primitive {
 		BOOL, TEXT, VOID, INT, REAL
 	}
 
 	/**
 	 * Derived (collection) types in hog.
 	 * 
 	 * The valid derived types are:
 	 * <ul>
 	 * <li>LIST : an ordered collection</li>
 	 * <li>ITER : an iterator over a collection</li>
 	 * <li>DICT : a key->value dictionary</li>
 	 * <li>MULTISET : an unordered collection, allowing duplicates</li>
 	 * <li>SET : an unordered collection, disallowing duplicates</li>
 	 * </ul>
 	 * 
 	 * @author sam
 	 * 
 	 */
 	public static enum Derived {
 		LIST, ITER, DICT, MULTISET, SET
 	}
 
 	/**
 	 * Exception types in hog.
 	 * 
 	 * The valid expection types are:
 	 * <ul>
 	 * <li>ARITHMETIC : attempting to compute the impossible (most likely
 	 * division by zero),</li>
 	 * <li>ARRAY_OUT_OF_BOUNDS : attempting to access an invalid index into a
 	 * collection,</li>
 	 * <li>FILE_LOAD : error while reading from a file,</li>
 	 * <li>FILE_NOT_FOUND : opening a non-existent file,</li>
 	 * <li>INCORRECT_ARGUMENT : evaluating a function call using incorrect
 	 * (either number or type of) arguments,</li>
 	 * <li>NULL_REFERENCE : attempting to access a null (dead) variable,
 	 * <li>TYPE_MISMATCH : invalid operands and operator type combination</li>
 	 * </ul>
 	 * 
 	 * @author sam
 	 * 
 	 */
 	public static enum Exception {
 		FILE_NOT_FOUND, FILE_LOAD, ARRAY_OUT_OF_BOUNDS, INCORRECT_ARGUMENT, TYPE_MISMATCH, NULL_REFERENCE, ARITHMETIC
 	}
 
 	/**
 	 * 
 	 * @author sam
 	 * 
 	 */
 	public static enum Flags {
 		RESERVED_WORD, CHECK_INNER_TYPE, CHECK_ENTIRE_TYPE
 	}
 
 	/**
 	 * 
 	 * @param one
 	 * @param two
 	 * @return
 	 */
 	public static boolean isSameType(TypeNode one, TypeNode two) {
 
 		if (one.isPrimitive() && two.isPrimitive()) {
 			return isSameType((PrimitiveTypeNode) one, (PrimitiveTypeNode) two);
 		} else if (one.isDict() && two.isDict()) {
 			return isSameType((DictTypeNode) one, (DictTypeNode) two);
 		} else if (one.isDerived() && two.isDerived()) {
 			return isSameType((DerivedTypeNode) one, (DerivedTypeNode) two);
 		} else if (one.isException() && two.isException()) {
 			return isSameType((ExceptionTypeNode) one, (ExceptionTypeNode) two);
 		}
 
 		return false;
 
 	}
 
 	/**
 	 * 
 	 * @param one
 	 * @param two
 	 * @return
 	 */
 	private static boolean isSameType(ExceptionTypeNode one,
 			ExceptionTypeNode two) {
 		return one.getExceptionType() == two.getExceptionType();
 	}
 
 	/**
 	 * 
 	 * @param one
 	 * @param two
 	 * @return
 	 */
 	private static boolean isSameType(DictTypeNode one, DictTypeNode two) {
 		if (one.getKeyType() == two.getKeyType()) {
 			return isSameType(one.getValueType(), two.getValueType());
 		}
 		return false;
 	}
 
 	/**
 	 * 
 	 * @param one
 	 * @param two
 	 * @return
 	 */
 	private static boolean isSameType(PrimitiveTypeNode one,
 			PrimitiveTypeNode two) {
 		return one.getType() == two.getType();
 	}
 
 	/**
 	 * 
 	 * @param one
 	 * @param two
 	 * @return
 	 */
 	private static boolean isSameType(DerivedTypeNode one, DerivedTypeNode two) {
 		if (one.getLocalType() == two.getLocalType()) {
 			return isSameType(one.getInnerTypeNode(), two.getInnerTypeNode());
 		}
 		return false;
 	}
 
 	/**
 	 * 
 	 * @param op
 	 * @param left
 	 * @param right
 	 * @return
 	 */
 	public static boolean isCompatible(BiOpNode.OpType op, TypeNode left,
 			TypeNode right) {
 
 		switch (op) {
 		case ASSIGN:
 		case NOT_EQLS:
 		case DBL_EQLS:
 			return isSameType(left, right);
 		case PLUS:
 			if (left.isText() && right.isText())
 				return true;
 		case MINUS:
 		case DIVIDE:
 		case TIMES:
 		case MOD:
 			return left.isNumeric() && right.isNumeric();
 		case OR:
 		case AND:
 			return left.isBoolean() && right.isBoolean();
 		case LESS:
 		case GRTR:
 		case LESS_EQL:
 		case GRTR_EQL:
 			return left.isPrimitive() && right.isPrimitive()
 					&& isSameType(left, right);
 		}
 
 		// should never get here:
 		throw new UnsupportedOperationException("OpType " + op
 				+ "not implemented!");
 	}
 
 	/**
 	 * 
 	 * @param op
 	 * @param left
 	 * @param right
 	 * @return
 	 * @throws TypeMismatchError
 	 */
 	public static TypeNode getResult(BiOpNode.OpType op, TypeNode left,
 			TypeNode right) throws TypeMismatchError {
 
 		if (!isCompatible(op, left, right)) {
 			throw new TypeMismatchError("Tried to call " + op + " on operands "
 					+ left + " (left) and " + right + " (right)!");
 		}
 
 		switch (op) {
 		case ASSIGN:
 			return new PrimitiveTypeNode(Types.Primitive.VOID);
 		case PLUS:
 			if (left.isText() && right.isText())
 				return new PrimitiveTypeNode(Types.Primitive.TEXT);
 		case MINUS:
 		case DIVIDE:
 		case TIMES:
 		case MOD:
 			return getHigherNumericType((PrimitiveTypeNode) left,
 					(PrimitiveTypeNode) right);
 		case OR:
 		case AND:
 		case NOT_EQLS:
 		case DBL_EQLS:
 		case LESS:
 		case GRTR:
 		case LESS_EQL:
 		case GRTR_EQL:
 			return new PrimitiveTypeNode(Types.Primitive.BOOL);
 		}
 
 		// should never get here.
 		throw new UnsupportedOperationException("OpType " + op
 				+ "not implemented!");
 	}
 
 	/**
 	 * 
 	 * @param op
 	 * @param operand
 	 * @return
 	 */
 	public static boolean isCompatible(UnOpNode.OpType op, TypeNode operand) {
 
 		switch (op) {
 		case UMINUS:
 		case INCR:
 		case DECR:
 			return operand.isNumeric();
 		case CAST:
 			throw new UnsupportedOperationException("TODO");
 		case NOT:
 			return operand.isBoolean();
 		case NONE:
 			return true;
 		}
 
 		// should never get here.
 		throw new UnsupportedOperationException("OpType " + op
 				+ "not implemented!");
 	}
 
 	/**
 	 * 
 	 * @param op
 	 * @param operand
 	 * @return
 	 * @throws TypeMismatchError
 	 */
 	public static TypeNode getResult(UnOpNode.OpType op, TypeNode operand)
 			throws TypeMismatchError {
 
 		if (isCompatible(op, operand)) {
 			throw new TypeMismatchError("Tried to call " + op + " on "
 					+ operand + "!");
 		}
 
 		switch (op) {
 		case UMINUS:
 		case INCR:
 		case DECR:
 			return (PrimitiveTypeNode) operand;
 		case CAST:
 			throw new UnsupportedOperationException("TODO");
 		case NOT:
 			return (PrimitiveTypeNode) operand;
 		case NONE:
 			return operand;
 		}
 
 		// should never get here.
 		throw new UnsupportedOperationException("OpType " + op
 				+ "not implemented!");
 	}
 
 	/**
 	 * Find the higher of two numeric types.
 	 * 
 	 * If one (or both) of the types are REAL, then REAL is returned, otherwise,
 	 * INT is returned (unless an exception is thrown).
 	 * 
 	 * @param left
 	 *            a numeric PrimitiveTypeNode
 	 * @param right
 	 *            a numeric PrimitiveTypeNode
 	 * @return a PrimitiveTypeNode representing the higher numeric type.
 	 * @throws TypeMismatchError
 	 *             if one of the passed in types is not numeric.
 	 */
 	public static PrimitiveTypeNode getHigherNumericType(TypeNode left,
 			TypeNode right) throws TypeMismatchError {
 
 		if (!left.isNumeric() || !right.isNumeric()) {
 			throw new TypeMismatchError("Tried to get higher numeric type for "
 					+ (left.isNumeric() ? right : left));
 		}
 
 		if (((PrimitiveTypeNode) left).getType() == Primitive.REAL
 				|| ((PrimitiveTypeNode) right).getType() == Primitive.REAL) {
 			return new PrimitiveTypeNode(Primitive.REAL);
 		}
 		return new PrimitiveTypeNode(Primitive.INT);
 	}
 
 	/**
 	 * Get a lower-case string representation of this node's type.
 	 * 
 	 * Note: for derived type, returns lower-case representation of the <i>local</i> type.
 	 * 
 	 * @param node a TypeNode to get the string representation for.
 	 * @return a lower-case string of the TypeNode's type.
 	 */
 	public static String getLowercaseTypeName(TypeNode node) {
 
 		// prim: lower case version
 		// derived: lower case version of outer
 		// exception : lower case version of node
 		
 		
 		String typeName = null;
 
 		if (node instanceof PrimitiveTypeNode) {
 			
 			typeName = ((PrimitiveTypeNode) node).getType().toString().toLowerCase();
 
 		} else if (node instanceof DerivedTypeNode) {
 			
 			typeName = ((DerivedTypeNode) node).getLocalType().toString().toLowerCase();
 
 		} else if (node instanceof ExceptionTypeNode) {
 			
 			typeName = ((ExceptionTypeNode) node).getExceptionType().toString().toLowerCase();
 
 		} else {
 			throw new UnsupportedOperationException(
 					"No support for TypeNode type: ");
 		}
 
 		return typeName;
 
 	}
 
 	public static boolean isCompatible(String functionName,
 			ArgumentsNode arguments) {
 
 		throw new UnsupportedOperationException("TODO");
 	}
 
 }
