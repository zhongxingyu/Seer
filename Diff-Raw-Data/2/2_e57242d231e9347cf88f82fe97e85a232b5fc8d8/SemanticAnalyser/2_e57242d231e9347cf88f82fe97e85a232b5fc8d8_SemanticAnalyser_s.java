 package swp_compiler_ss13.fuc.semantic_analyser;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import swp_compiler_ss13.common.ast.AST;
 import swp_compiler_ss13.common.ast.ASTNode;
 import swp_compiler_ss13.common.ast.nodes.ExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.IdentifierNode;
 import swp_compiler_ss13.common.ast.nodes.StatementNode;
 import swp_compiler_ss13.common.ast.nodes.binary.ArithmeticBinaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.binary.AssignmentNode;
 import swp_compiler_ss13.common.ast.nodes.leaf.BasicIdentifierNode;
 import swp_compiler_ss13.common.ast.nodes.leaf.LiteralNode;
 import swp_compiler_ss13.common.ast.nodes.marynary.BlockNode;
 import swp_compiler_ss13.common.ast.nodes.unary.ArithmeticUnaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.unary.ReturnNode;
 import swp_compiler_ss13.common.parser.ReportLog;
 import swp_compiler_ss13.common.parser.SymbolTable;
 
 public class SemanticAnalyser {
 
 	private static Logger logger = Logger.getLogger(SemanticAnalyser.class);
 
 	enum Attribute {
 		/**
 		 * num, basic, array,...
 		 */
 		TYPE,
 		/**
 		 * <code>"1"</code> - identifier is initialized,<br/>
 		 * <code>"0"</code> - identifier is not initialized
 		 * 
 		 * @see ExpressionNode
 		 */
 		INITIALIZATION_STATUS,
 		/**
 		 * name of identifier
 		 * 
 		 * @see IdentifierNode
 		 */
 		IDENTIFIER
 	}
 
 	private static final String IS_NOT_INITIALIZED = "0";
 	private static final String IS_INITIALIZED = "1";
 	private static final String NO_ATTRIBUTE_VALUE = "no Value";
 
 	private final ReportLog errorLog;
 	private final Map<ASTNode, Map<Attribute, String>> attributes;
 	private final Map<SymbolTable, Set<String>> initializations;
 
 	public SemanticAnalyser(ReportLog log) {
 		this.attributes = new HashMap<>();
 		this.initializations = new HashMap<>();
 		this.errorLog = log;
 	}
 
 	public AST analyse(AST ast) {
 		this.traverseAstNode(ast.getRootNode(), ast.getRootSymbolTable());
 		return ast;
 	}
 
 	protected void traverseAstNode(ASTNode node, SymbolTable table) {
 		switch (node.getNodeType()) {
 		case BasicIdentifierNode:
 			logger.trace("handle BasicIdentifierNode");
 			this.handleNode((BasicIdentifierNode) node, table);
 			break;
 		case BreakNode:
 			break;
 		case LiteralNode:
 			logger.trace("handle LiteralNode");
 			this.handleNode((LiteralNode) node, table);
 			break;
 		case ArithmeticUnaryExpressionNode:
 			logger.trace("handle ArithmeticUnaryExpressionNode");
 			this.handleNode((ArithmeticUnaryExpressionNode) node, table);
 			break;
 		case ArrayIdentifierNode:
 			break;
 		case DeclarationNode:
 			break;
 		case LogicUnaryExpressionNode:
 			break;
 		case PrintNode:
 			break;
 		case ReturnNode:
 			logger.trace("handle ReturnNode");
 			this.handleNode((ReturnNode) node, table);
 			break;
 		case StructIdentifierNode:
 			break;
 		case ArithmeticBinaryExpressionNode:
 			logger.trace("handle ArithmeticBinaryExpressionNode");
 			this.handleNode((ArithmeticBinaryExpressionNode) node, table);
 			break;
 		case AssignmentNode:
 			logger.trace("handle AssignmentNode");
 			this.handleNode((AssignmentNode) node, table);
 			break;
 		case DoWhileNode:
 			break;
 		case LogicBinaryExpressionNode:
 			break;
 		case RelationExpressionNode:
 			break;
 		case WhileNode:
 			break;
 		case BranchNode:
 			break;
 		case BlockNode:
 			logger.trace("handle BlockNode");
 			this.handleNode((BlockNode) node);
 			break;
 		default:
 			throw new IllegalArgumentException("unknown ASTNodeType");
 		}
 	}
 
 	protected void handleNode(LiteralNode node, SymbolTable table) {
 		this.setAttribute(node, Attribute.INITIALIZATION_STATUS, IS_INITIALIZED);
 	}
 
 	protected void handleNode(ArithmeticBinaryExpressionNode node, SymbolTable table) {
 		ExpressionNode expression = node.getLeftValue();
 		this.traverseAstNode(expression, table);
 		this.checkInitialization(expression);
 		expression = node.getRightValue();
 		this.traverseAstNode(expression, table);
 		this.checkInitialization(expression);
 		this.setAttribute(node, Attribute.INITIALIZATION_STATUS, IS_INITIALIZED);
 	}
 
 	protected void handleNode(ArithmeticUnaryExpressionNode node, SymbolTable table) {
 		ExpressionNode expression = node.getRightValue();
 		this.traverseAstNode(expression, table);
 		this.checkInitialization(expression);
 		this.setAttribute(node, Attribute.INITIALIZATION_STATUS, IS_INITIALIZED);
 	}
 
 	protected void handleNode(BlockNode node) {
 		SymbolTable newTable = node.getSymbolTable();
 		for (StatementNode child : node.getStatementList()) {
 			this.traverseAstNode(child, newTable);
 		}
 	}
 
 	protected void handleNode(AssignmentNode node, SymbolTable table) {
 		this.traverseAstNode(node.getLeftValue(), table);
 		this.traverseAstNode(node.getRightValue(), table);
 		this.addIdentifier(table, this.getAttribute(node.getLeftValue(), Attribute.IDENTIFIER));
 		this.setAttribute(node, Attribute.INITIALIZATION_STATUS, IS_INITIALIZED);
 	}
 
 	protected void handleNode(BasicIdentifierNode node, SymbolTable table) {
 		this.setAttribute(node, Attribute.IDENTIFIER, node.getIdentifier());
 		this.setAttribute(node, Attribute.INITIALIZATION_STATUS, this.isInitialized(
 				table.getDeclaringSymbolTable(node.getIdentifier()), node.getIdentifier()) ? IS_INITIALIZED
 				: IS_NOT_INITIALIZED);
 	}
 
 	protected void handleNode(ReturnNode node, SymbolTable table) {
 		IdentifierNode identifier = node.getRightValue();
 		this.traverseAstNode(identifier, table);
 		this.checkInitialization(identifier);
 	}
 
 	private void checkInitialization(ExpressionNode identifier) {
 		switch (this.getAttribute(identifier, Attribute.INITIALIZATION_STATUS)) {
 		case IS_NOT_INITIALIZED:
			this.errorLog.reportError("Variable" + this.getAttribute(identifier, Attribute.IDENTIFIER)
 					+ " is not initialized", -1, -1, "NotInitializedException");
 			break;
 		case IS_INITIALIZED:
 			break;
 		default:
 			IllegalStateException e = new IllegalStateException(
 					"child node has no initialization information");
 			logger.error("couldn't check initialization of node", e);
 			throw e;
 		}
 	}
 
 	private boolean isInitialized(SymbolTable table, String identifier) {
 		SymbolTable declarationTable = table.getDeclaringSymbolTable(identifier);
 		Set<String> identifiers = this.initializations.get(declarationTable);
 		if (identifiers == null) {
 			return false;
 		}
 		return identifiers.contains(identifier);
 	}
 
 	protected void addIdentifier(SymbolTable table, String identifier) {
 		SymbolTable declarationTable = table.getDeclaringSymbolTable(identifier);
 		if (this.initializations.get(declarationTable) == null) {
 			this.initializations.put(declarationTable, new HashSet<String>());
 		}
 		this.initializations.get(declarationTable).add(identifier);
 	}
 
 	protected String getAttribute(ASTNode node, Attribute attribute) {
 		Map<Attribute, String> nodeMap = this.attributes.get(node);
 		if (nodeMap == null) {
 			return NO_ATTRIBUTE_VALUE;
 		}
 		String value = nodeMap.get(attribute);
 		return value == null ? NO_ATTRIBUTE_VALUE : value;
 	}
 
 	protected void setAttribute(ASTNode node, Attribute attribute, String value) {
 		if (this.attributes.get(node) == null) {
 			this.attributes.put(node, new HashMap<Attribute, String>());
 		}
 		this.attributes.get(node).put(attribute, value);
 	}
 }
