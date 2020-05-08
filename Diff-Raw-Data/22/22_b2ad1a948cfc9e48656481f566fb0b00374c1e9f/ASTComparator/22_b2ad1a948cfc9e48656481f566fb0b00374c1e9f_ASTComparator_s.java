 package swp_compiler_ss13.fuc.parser;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 
import junit.extensions.PA;
 import swp_compiler_ss13.common.ast.AST;
 import swp_compiler_ss13.common.ast.ASTNode;
 import swp_compiler_ss13.common.ast.nodes.binary.ArithmeticBinaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.binary.AssignmentNode;
 import swp_compiler_ss13.common.ast.nodes.binary.BinaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.binary.DoWhileNode;
 import swp_compiler_ss13.common.ast.nodes.binary.LogicBinaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.binary.LoopNode;
 import swp_compiler_ss13.common.ast.nodes.binary.RelationExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.binary.WhileNode;
 import swp_compiler_ss13.common.ast.nodes.leaf.BasicIdentifierNode;
 import swp_compiler_ss13.common.ast.nodes.leaf.BreakNode;
 import swp_compiler_ss13.common.ast.nodes.leaf.LiteralNode;
 import swp_compiler_ss13.common.ast.nodes.marynary.BlockNode;
 import swp_compiler_ss13.common.ast.nodes.ternary.BranchNode;
 import swp_compiler_ss13.common.ast.nodes.unary.ArithmeticUnaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.unary.ArrayIdentifierNode;
 import swp_compiler_ss13.common.ast.nodes.unary.DeclarationNode;
 import swp_compiler_ss13.common.ast.nodes.unary.LogicUnaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.unary.PrintNode;
 import swp_compiler_ss13.common.ast.nodes.unary.ReturnNode;
 import swp_compiler_ss13.common.ast.nodes.unary.StructIdentifierNode;
 import swp_compiler_ss13.common.ast.nodes.unary.UnaryExpressionNode;
 import swp_compiler_ss13.common.optimization.Liveliness;
 import swp_compiler_ss13.common.parser.SymbolTable;
 import swp_compiler_ss13.common.types.Type;
 import swp_compiler_ss13.common.types.derived.ArrayType;
 import swp_compiler_ss13.common.types.derived.Member;
 import swp_compiler_ss13.common.types.derived.StructType;
 
 public class ASTComparator {
 	
 	private static final Logger log = Logger.getLogger(ASTComparator.class);
 	
 	/**
 	 * Compares the two given {@link AST}s and assert their equality
 	 * 
 	 * @param expected
 	 * @param actual
 	 */
 	public static void compareAST(AST expected, AST actual) {
 		assertNotNull(actual);
 		
 		// Iterate both trees
 		Iterator<ASTNode> actualIt = actual.getDFSLTRIterator();
 		Iterator<ASTNode> expectedIt = expected.getDFSLTRIterator();
 		while (expectedIt.hasNext() && actualIt.hasNext()) {
 			ASTNode expectedNode = expectedIt.next();
 			ASTNode actualNode = actualIt.next();
 			
 			log.debug("Expected: " + expectedNode.toString() + " | Actual: " + actualNode.toString());
 			compare(expectedNode, actualNode);
 		}
 		
 		if (actualIt.hasNext() != expectedIt.hasNext()) {
 			fail("ASTs have different number of nodes!");
 		}
 		
 		// Success!
 	}
 	
 	private static void compare(ASTNode expected, ASTNode actual) {
 		assertEquals(expected.getNodeType(), actual.getNodeType());
 		
 		switch (expected.getNodeType()) {
 			case BasicIdentifierNode:
 				compare((BasicIdentifierNode) expected, (BasicIdentifierNode) actual);
 				break;
 			case BreakNode:
 				compare((BreakNode) expected, (BreakNode) actual);
 				break;
 			case LiteralNode:
 				compare((LiteralNode) expected, (LiteralNode) actual);
 				break;
 			case ArithmeticUnaryExpressionNode:
 				compare((ArithmeticUnaryExpressionNode) expected, (ArithmeticUnaryExpressionNode) actual);
 				break;
 			case ArrayIdentifierNode:
 				compare((ArrayIdentifierNode) expected, (ArrayIdentifierNode) actual);
 				break;
 			case DeclarationNode:
 				compare((DeclarationNode) expected, (DeclarationNode) actual);
 				break;
 			case LogicUnaryExpressionNode:
 				compare((LogicUnaryExpressionNode) expected, (LogicUnaryExpressionNode) actual);
 				break;
 			case PrintNode:
 				compare((PrintNode) expected, (PrintNode) actual);
 				break;
 			case ReturnNode:
 				compare((ReturnNode) expected, (ReturnNode) actual);
 				break;
 			case StructIdentifierNode:
 				compare((StructIdentifierNode) expected, (StructIdentifierNode) actual);
 				break;
 			case ArithmeticBinaryExpressionNode:
 				compare((ArithmeticBinaryExpressionNode) expected, (ArithmeticBinaryExpressionNode) actual);
 				break;
 			case AssignmentNode:
 				compare((AssignmentNode) expected, (AssignmentNode) actual);
 				break;
 			case DoWhileNode:
 				compare((DoWhileNode) expected, (DoWhileNode) actual);
 				break;
 			case LogicBinaryExpressionNode:
 				compare((LogicBinaryExpressionNode) expected, (LogicBinaryExpressionNode) actual);
 				break;
 			case RelationExpressionNode:
 				compare((RelationExpressionNode) expected, (RelationExpressionNode) actual);
 				break;
 			case WhileNode:
 				compare((WhileNode) expected, (WhileNode) actual);
 				break;
 			case BranchNode:
 				compare((BranchNode) expected, (BranchNode) actual);
 				break;
 			case BlockNode:
 				compare((BlockNode) expected, (BlockNode) actual);
 				break;
 			default:
 				throw new IllegalArgumentException("Unknown ASTNodeType!");
 		}
 	}
 	
 	private static void compare(DeclarationNode expected, DeclarationNode actual) {
 		assertEquals(expected.getIdentifier(), actual.getIdentifier());
 		compare(expected.getType(), actual.getType());
 	}
 	
 	private static void compare(Type expected, Type actual) {
 		switch (expected.getKind()) {
 		case ARRAY:
 			compare((ArrayType) expected, (ArrayType) actual);
 			break;
 		case STRUCT:
 			compare((StructType) expected, (StructType) actual);
 			break;
 		case BOOLEAN:
 		case DOUBLE:
 		case LONG:
 		case STRING:
 			compareBasicType(expected, actual);
 			break;
 		default:
 			throw new IllegalArgumentException("Unknown declaration type!");
 		}
 	}
 	
 	private static void compareBasicType(Type expected, Type actual) {
 		assertEquals(expected.getKind(), actual.getKind());
 		assertEquals(expected.getTypeName(), actual.getTypeName());
 		assertEquals(expected.getWidth(), actual.getWidth());
 	}
 
 	private static void compare(StructType expected, StructType actual) {
 		compareBasicType(expected, actual);
 		assertEquals(expected.members().length, actual.members().length);
 		
 		for (int i = 0; i < expected.members().length; i++) {
 			Member exp = expected.members()[i];
 			Member act = actual.members()[i];
 			compare(exp, act);
 		}
 	}
 	
 	private static void compare(Member expected, Member actual) {
 		assertEquals(expected.getName(), actual.getName());
 		compare(expected.getType(), actual.getType());
 	}
 
 	private static void compare(ArrayType expected, ArrayType actual) {
 		compareBasicType(expected, actual);
 		compare(expected.getInnerType(), actual.getInnerType());
 		assertEquals(expected.getLength(), actual.getLength());
 		assertEquals(expected.getTypeName(), actual.getTypeName());
 	}
 
 	private static void compare(BlockNode expected, BlockNode actual) {
 		compare(expected.getSymbolTable(), actual.getSymbolTable());
 	}
 
 	@SuppressWarnings("unchecked")
 	private static void compare(SymbolTable expected, SymbolTable actual) {
 		if (expected == null) {
 			if (actual == null) {
 				return;	// True
 			} else {
 				fail("Expected no SymbolTable but found one!");
 			}
 		} else {
 			if (actual == null) {
 				fail("Expected a SymbolTable but found none!");
 			} else {
 				// Check...
 			}
 		}
 		
 		HashMap<String, Type> expectedSymbolMap = (HashMap<String, Type>) PA.getValue(expected, "symbolMap");
 		HashMap<String, Type> actualSymbolMap = (HashMap<String, Type>) PA.getValue(actual, "symbolMap");
 		{
 			assertEquals(expectedSymbolMap.entrySet().size(), actualSymbolMap.entrySet().size());
 			Iterator<Entry<String, Type>> expIt = expectedSymbolMap.entrySet().iterator();
 			Iterator<Entry<String, Type>> actIt = actualSymbolMap.entrySet().iterator();
 			while (expIt.hasNext()) {
 				Entry<String, Type> exp = expIt.next();
 				Entry<String, Type> act = actIt.next();
 				assertEquals(exp.getKey(), act.getKey());
 				compare(exp.getValue(), act.getValue());
 			}
 		}
 		
 		HashMap<String, Liveliness> expectedLiveMap = (HashMap<String, Liveliness>) PA.getValue(expected, "liveMap");
 		HashMap<String, Liveliness> actualLiveMap = (HashMap<String, Liveliness>) PA.getValue(actual, "liveMap");
 		{
 			assertEquals(expectedLiveMap.entrySet().size(), actualLiveMap.entrySet().size());
 			Iterator<Entry<String, Liveliness>> expIt = expectedLiveMap.entrySet().iterator();
 			Iterator<Entry<String, Liveliness>> actIt = actualLiveMap.entrySet().iterator();
 			while (expIt.hasNext()) {
 				Entry<String, Liveliness> exp = expIt.next();
 				Entry<String, Liveliness> act = actIt.next();
 				assertEquals(exp.getKey(), act.getKey());
 				compare(exp.getValue(), act.getValue());
 			}
 		}
 		
 		HashMap<String, String> expectedAliasMap = (HashMap<String, String>) PA.getValue(expected, "aliasMap");
 		HashMap<String, String> actualAliasMap = (HashMap<String, String>) PA.getValue(actual, "aliasMap");
 		{
 			assertEquals(expectedAliasMap.entrySet().size(), actualAliasMap.entrySet().size());
 			Iterator<Entry<String, String>> expIt = expectedAliasMap.entrySet().iterator();
 			Iterator<Entry<String, String>> actIt = actualAliasMap.entrySet().iterator();
 			while (expIt.hasNext()) {
 				Entry<String, String> exp = expIt.next();
 				Entry<String, String> act = actIt.next();
 				assertEquals(exp.getKey(), act.getKey());
 				assertEquals(exp.getValue(), act.getValue());
 			}
 		}
 		
 		// Check parent and root
 		// TODO Whole SymbolTable hierarchy gets checked everytime a BlockNode occurs...
 		compare(expected.getParentSymbolTable(), actual.getParentSymbolTable());
 		if (expected.getRootSymbolTable() == expected) {
 			if (actual.getRootSymbolTable() == actual) {
 				return;	// Reached root
 			} else {
 				fail("Expected SymbolTable root, but did found one!");
 			}
 		}
 		compare(expected.getRootSymbolTable(), actual.getRootSymbolTable());
 	}
 	
 	private static void compare(Liveliness expected, Liveliness actual) {
 		assertEquals(expected.isAlive(), actual.isAlive());
 		assertEquals(expected.getNextUse(), actual.getNextUse());
 	}
 	
 	private static void compare(BranchNode expected, BranchNode actual) {
 		compare(expected.getCondition(), actual.getCondition());
 		compare(expected.getStatementNodeOnFalse(), actual.getStatementNodeOnFalse());
 		compare(expected.getStatementNodeOnTrue(), actual.getStatementNodeOnTrue());
 	}
 	
 	private static void compare(BreakNode expected, BreakNode actual) {
 		// Intentionally left blank
 	}
 	
 	private static void compare(AssignmentNode expected, AssignmentNode actual) {
 		compare(expected.getLeftValue(), actual.getLeftValue());
 		compare(expected.getRightValue(), actual.getRightValue());
 	}
 	
 	private static void compare(BinaryExpressionNode expected, BinaryExpressionNode actual) {
 		compare(expected.getLeftValue(), expected.getLeftValue());
 		compare(expected.getRightValue(), expected.getRightValue());
 		assertEquals(expected.getOperator(), actual.getOperator());
 	}
 	
 	private static void compare(ArrayIdentifierNode expected, ArrayIdentifierNode actual) {
 		compare(expected.getIdentifierNode(), actual.getIdentifierNode());
 		assertEquals(expected.getIndex(), actual.getIndex());
 	}
 	
 	private static void compare(BasicIdentifierNode expected, BasicIdentifierNode actual) {
 		assertEquals(expected.getIdentifier(), actual.getIdentifier());
 	}
 	
 	private static void compare(StructIdentifierNode expected, StructIdentifierNode actual) {
 		compare(expected.getIdentifierNode(), actual.getIdentifierNode());
 		assertEquals(expected.getFieldName(), actual.getFieldName());
 	}
 	
 	private static void compare(LiteralNode expected, LiteralNode actual) {
 		assertEquals(expected.getLiteral(), actual.getLiteral());
 		compare(expected.getLiteralType(), actual.getLiteralType());
 	}
 	
 	private static void compare(UnaryExpressionNode expected, UnaryExpressionNode actual) {
 		assertEquals(expected.getOperator(), actual.getOperator());
 		compare(expected.getRightValue(), actual.getRightValue());
 	}
 	
 	private static void compare(LoopNode expected, LoopNode actual) {
 		compare(expected.getCondition(), actual.getCondition());
 		compare(expected.getLoopBody(), actual.getLoopBody());
 	}
 	
 	private static void compare(PrintNode expected, PrintNode actual) {
 		compare(expected.getRightValue(), actual.getRightValue());
 	}
 	
 	private static void compare(ReturnNode expected, ReturnNode actual) {
 		compare(expected.getRightValue(), actual.getRightValue());
 	}
 }
