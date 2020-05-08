 package swp_compiler_ss13.fuc.ir;
 
 import java.util.EmptyStackException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import swp_compiler_ss13.common.ast.AST;
 import swp_compiler_ss13.common.ast.ASTNode;
 import swp_compiler_ss13.common.ast.nodes.StatementNode;
 import swp_compiler_ss13.common.ast.nodes.binary.ArithmeticBinaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.binary.AssignmentNode;
 import swp_compiler_ss13.common.ast.nodes.binary.DoWhileNode;
 import swp_compiler_ss13.common.ast.nodes.binary.LogicBinaryExpressionNode;
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
 import swp_compiler_ss13.common.backend.Quadruple;
 import swp_compiler_ss13.common.ir.IntermediateCodeGenerator;
 import swp_compiler_ss13.common.ir.IntermediateCodeGeneratorException;
 import swp_compiler_ss13.common.parser.SymbolTable;
 import swp_compiler_ss13.common.types.Type;
 import swp_compiler_ss13.common.types.Type.Kind;
 import swp_compiler_ss13.common.types.primitive.DoubleType;
 import swp_compiler_ss13.common.types.primitive.LongType;
 
 /**
  * Create the intermediate code representation for the given AST
  * 
  * @author "Frank Zechert"
  * @version 1
  */
 public class IntermediateCodeGeneratorImpl implements IntermediateCodeGenerator {
 
 	/**
 	 * The generated intermediate code
 	 */
 	private List<Quadruple> irCode;
 
 	/**
 	 * List of used names. This is needed for single static assignment.
 	 */
 	private List<String> usedNames;
 
 	/**
 	 * The stack of identifier renames
 	 */
 	private Stack<Map<String, String>> currentScopeRenames;
 
 	/**
 	 * The stack of symbol tables
 	 */
 	private Stack<SymbolTable> currentSymbolTable;
 
 	/**
 	 * Store for intermediate results
 	 */
 	private Stack<String> intermediateResults;
 
 	private Stack<Type> intermediateTypes;
 
 	/**
 	 * Reset the intermediate code generator
 	 */
 	private void reset() {
 		this.irCode = new LinkedList<>();
 		this.usedNames = new LinkedList<>();
 		this.currentScopeRenames = new Stack<>();
 		this.currentSymbolTable = new Stack<>();
 		this.intermediateResults = new Stack<>();
 		this.intermediateTypes = new Stack<>();
 	}
 
 	@Override
 	public List<Quadruple> generateIntermediateCode(AST ast) throws IntermediateCodeGeneratorException {
 		this.reset();
 		BlockNode program = ast.getRootNode();
 		this.processBlockNode(program);
 		return this.irCode;
 	}
 
 	/**
 	 * call the method that handles the node in the AST
 	 * 
 	 * @param node
 	 *            The node to handle
 	 * @throws IntermediateCodeGeneratorException
 	 *             An error occurred
 	 */
 	private void callProcessing(ASTNode node) throws IntermediateCodeGeneratorException {
 		switch (node.getNodeType()) {
 		case ArithmeticBinaryExpressionNode:
 			this.processArithmeticBinaryExpressionNode((ArithmeticBinaryExpressionNode) node);
 			break;
 		case ArithmeticUnaryExpressionNode:
 			this.processArithmeticUnaryExpressionNode((ArithmeticUnaryExpressionNode) node);
 			break;
 		case ArrayIdentifierNode:
 			this.processArrayIdentifierNode((ArrayIdentifierNode) node);
 			break;
 		case AssignmentNode:
 			this.processAssignmentNode((AssignmentNode) node);
 			break;
 		case BasicIdentifierNode:
 			this.processBasicIdentifierNode((BasicIdentifierNode) node);
 			break;
 		case BlockNode:
 			this.processBlockNode((BlockNode) node);
 			break;
 		case BranchNode:
 			this.processBranchNode((BranchNode) node);
 			break;
 		case BreakNode:
 			this.processBreakNode((BreakNode) node);
 			break;
 		case DeclarationNode:
 			this.processDeclarationNode((DeclarationNode) node);
 			break;
 		case DoWhileNode:
 			this.processDoWhileNode((DoWhileNode) node);
 			break;
 		case LiteralNode:
 			this.processLiteralNode((LiteralNode) node);
 			break;
 		case LogicBinaryExpressionNode:
 			this.processLogicBinaryExpressionNode((LogicBinaryExpressionNode) node);
 			break;
 		case LogicUnaryExpressionNode:
 			this.processLogicUnaryExpressionNode((LogicUnaryExpressionNode) node);
 			break;
 		case PrintNode:
 			this.processPrintNode((PrintNode) node);
 			break;
 		case RelationExpressionNode:
 			this.processRelationExpressionNode((RelationExpressionNode) node);
 			break;
 		case ReturnNode:
 			this.processReturnNode((ReturnNode) node);
 			break;
 		case StructIdentifierNode:
 			this.processStructIdentifierNode((StructIdentifierNode) node);
 			break;
 		case WhileNode:
 			this.processWhileNode((WhileNode) node);
 			break;
 		default:
 			break;
 		}
 	}
 
 	/**
 	 * Process a block node and generate the needed IR Code
 	 * 
 	 * @param node
 	 *            The block node to process
 	 * @throws IntermediateCodeGeneratorException
 	 *             an error occurred while process the node
 	 */
 	private void processBlockNode(BlockNode node) throws IntermediateCodeGeneratorException {
 		// push new renaming scope
 		this.currentScopeRenames.push(new HashMap<String, String>());
 		// push current symbol table
 		this.currentSymbolTable.push(node.getSymbolTable());
 
 		// get declarations
 		Iterator<DeclarationNode> declIterator = node.getDeclarationIterator();
 		while (declIterator.hasNext()) {
 			this.processDeclarationNode(declIterator.next());
 		}
 
 		Iterator<StatementNode> statementIterator = node.getStatementIterator();
 		while (statementIterator.hasNext()) {
 			StatementNode statement = statementIterator.next();
 			this.callProcessing(statement);
 		}
 
 		// pop the symbol scope and the renaming scope
 		this.currentScopeRenames.pop();
 		this.currentSymbolTable.pop();
 	}
 
 	private void processWhileNode(WhileNode node) throws IntermediateCodeGeneratorException {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void processStructIdentifierNode(StructIdentifierNode node) throws IntermediateCodeGeneratorException {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void processReturnNode(ReturnNode node) throws IntermediateCodeGeneratorException {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void processRelationExpressionNode(RelationExpressionNode node) throws IntermediateCodeGeneratorException {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void processPrintNode(PrintNode node) throws IntermediateCodeGeneratorException {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void processLogicUnaryExpressionNode(LogicUnaryExpressionNode node)
 			throws IntermediateCodeGeneratorException {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void processLogicBinaryExpressionNode(LogicBinaryExpressionNode node)
 			throws IntermediateCodeGeneratorException {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void processLiteralNode(LiteralNode node) throws IntermediateCodeGeneratorException {
 		String literal = node.getLiteral();
 		Type type = node.getLiteralType();
 		this.intermediateTypes.push(type);
 		switch (type.getKind()) {
 		case DOUBLE:
 		case LONG:
 			this.intermediateResults.push("#" + literal);
 			break;
 		case STRING:
 			this.intermediateResults.push("#\"" + literal + "\"");
 			break;
 		default:
 			throw new IntermediateCodeGeneratorException("Literal node of type " + node.getLiteralType().toString()
 					+ " is not supported");
 		}
 	}
 
 	private void processDoWhileNode(DoWhileNode node) throws IntermediateCodeGeneratorException {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void processDeclarationNode(DeclarationNode node) throws IntermediateCodeGeneratorException {
 		String identifierName = node.getIdentifier();
 		Type identifierType = node.getType();
 		String id = this.saveIdentifier(identifierName, identifierType);
 
 		final Quadruple quadruple = QuadrupleFactory.declaration(id, node.getType());
 		this.irCode.add(quadruple);
 	}
 
 	private void processBreakNode(BreakNode node) throws IntermediateCodeGeneratorException {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void processBranchNode(BranchNode node) throws IntermediateCodeGeneratorException {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void processBasicIdentifierNode(BasicIdentifierNode node) throws IntermediateCodeGeneratorException {
 		String identifier = node.getIdentifier();
 		Type identifierType = this.currentSymbolTable.peek().lookupType(identifier);
 		String actualIdentifier = this.loadIdentifier(identifier);
 		this.intermediateResults.push(actualIdentifier);
 		this.intermediateTypes.push(identifierType);
 	}
 
 	private void processAssignmentNode(AssignmentNode node) throws IntermediateCodeGeneratorException {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void processArrayIdentifierNode(ArrayIdentifierNode node) throws IntermediateCodeGeneratorException {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void processArithmeticUnaryExpressionNode(ArithmeticUnaryExpressionNode node)
 			throws IntermediateCodeGeneratorException {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * Binary Arithmetic Operation
 	 * 
 	 * @param node
 	 * @throws IntermediateCodeGeneratorException
 	 */
 	private void processArithmeticBinaryExpressionNode(ArithmeticBinaryExpressionNode node)
 			throws IntermediateCodeGeneratorException {
 		this.callProcessing(node.getLeftValue());
 		this.callProcessing(node.getRightValue());
 
 		Type rightType = this.intermediateTypes.pop();
 		Type leftType = this.intermediateTypes.pop();
 
 		String rightValue = this.intermediateResults.pop();
 		String leftValue = this.intermediateResults.pop();
 
 		if (leftType.getKind() == Kind.LONG && rightType.getKind() == Kind.LONG) {
 			// Just long types
 			String temp = this.createAndSaveTemporaryIdentifier(new LongType());
 			Quadruple tac = QuadrupleFactory.longArithmeticBinaryOperation(node.getOperator(), leftValue, rightValue,
 					temp);
 			this.irCode.add(tac);
 			this.intermediateResults.push(temp);
 			this.intermediateTypes.push(new LongType());
 		} else {
 			// double types or mix of double and long
 			String castLeft = leftValue;
 			if (leftType.getKind() == Kind.LONG) {
 				// cast the left value to double
 				castLeft = this.createAndSaveTemporaryIdentifier(new DoubleType());
 				Quadruple cleft = QuadrupleFactory.castLongToDouble(leftValue, castLeft);
 				this.irCode.add(cleft);
 			}
 			String castRight = rightValue;
 			if (rightType.getKind() == Kind.LONG) {
 				// cast the right value to double
 				castRight = this.createAndSaveTemporaryIdentifier(new DoubleType());
 				Quadruple cright = QuadrupleFactory.castLongToDouble(rightValue, castRight);
 				this.irCode.add(cright);
 			}
 			// double binary operation
 			String temp = this.createAndSaveTemporaryIdentifier(new DoubleType());
			Quadruple tac = QuadrupleFactory.longArithmeticBinaryOperation(node.getOperator(), castLeft, castRight,
 					temp);
 			this.irCode.add(tac);
 			this.intermediateResults.push(temp);
 			this.intermediateTypes.push(new DoubleType());
 		}
 
 	}
 
 	/**
 	 * Save the given identifier to the interal store of variables
 	 * 
 	 * @param identifier
 	 *            The name of the variable
 	 * @param type
 	 *            the type of the variable
 	 * @return The renamed name of the variable that was stored
 	 * @throws IntermediateCodeGeneratorException
 	 *             an exception occurred
 	 */
 	private String saveIdentifier(String identifier, Type type) throws IntermediateCodeGeneratorException {
 		if (!this.usedNames.contains(identifier)) {
 			this.usedNames.add(identifier);
 			this.currentScopeRenames.peek().put(identifier, identifier);
 			return identifier;
 		} else {
 			// rename is required to keep single static assignment
 			String newName = this.currentSymbolTable.peek().getNextFreeTemporary();
 			this.currentSymbolTable.peek().putTemporary(newName, type);
 			this.usedNames.add(newName);
 			this.currentScopeRenames.peek().put(identifier, newName);
 			this.irCode.add(QuadrupleFactory.declaration(newName, type));
 			return newName;
 		}
 	}
 
 	/**
 	 * Create a new temporary value and save it to the internal store of
 	 * variables
 	 * 
 	 * @param type
 	 *            The type of the new variable
 	 * @return The name of the new variable
 	 * @throws IntermediateCodeGeneratorException
 	 *             An error occurred
 	 */
 	private String createAndSaveTemporaryIdentifier(Type type) throws IntermediateCodeGeneratorException {
 		String id = this.currentSymbolTable.peek().getNextFreeTemporary();
 		this.currentSymbolTable.peek().putTemporary(id, type);
 		this.usedNames.add(id);
 		this.currentScopeRenames.peek().put(id, id);
 		this.irCode.add(QuadrupleFactory.declaration(id, type));
 		return id;
 	}
 
 	/**
 	 * Load the given identifier and return its actual name (if renaming was
 	 * done)
 	 * 
 	 * @param id
 	 *            The identifier name to load
 	 * @return The actual name of the identifier
 	 * @throws IntermediateCodeGeneratorException
 	 *             Identifier was not found
 	 */
 	private String loadIdentifier(String id) throws IntermediateCodeGeneratorException {
 		@SuppressWarnings("unchecked")
 		Stack<Map<String, String>> renameScopes = (Stack<Map<String, String>>) this.currentScopeRenames.clone();
 		try {
 			while (true) {
 				Map<String, String> renamedIds = renameScopes.pop();
 				if (renamedIds.containsKey(id)) {
 					return renamedIds.get(id);
 				}
 			}
 		} catch (EmptyStackException e) {
 			throw new IntermediateCodeGeneratorException("Undeclared variable found: " + id);
 		}
 	}
 }
