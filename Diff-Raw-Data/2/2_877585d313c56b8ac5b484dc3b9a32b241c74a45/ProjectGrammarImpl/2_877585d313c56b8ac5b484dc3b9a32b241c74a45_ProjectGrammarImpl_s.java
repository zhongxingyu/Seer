 package swp_compiler_ss13.fuc.parser.parser;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 
 import org.apache.log4j.Logger;
 
 import swp_compiler_ss13.common.ast.nodes.ExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.IdentifierNode;
 import swp_compiler_ss13.common.ast.nodes.StatementNode;
 import swp_compiler_ss13.common.ast.nodes.binary.BinaryExpressionNode.BinaryOperator;
 import swp_compiler_ss13.common.ast.nodes.binary.LoopNode;
 import swp_compiler_ss13.common.ast.nodes.marynary.BlockNode;
 import swp_compiler_ss13.common.ast.nodes.ternary.BranchNode;
 import swp_compiler_ss13.common.ast.nodes.unary.DeclarationNode;
 import swp_compiler_ss13.common.ast.nodes.unary.UnaryExpressionNode.UnaryOperator;
 import swp_compiler_ss13.common.lexer.NumToken;
 import swp_compiler_ss13.common.lexer.Token;
 import swp_compiler_ss13.common.lexer.TokenType;
 import swp_compiler_ss13.common.parser.SymbolTable;
 import swp_compiler_ss13.common.report.ReportLog;
 import swp_compiler_ss13.common.report.ReportType;
 import swp_compiler_ss13.common.types.Type;
 import swp_compiler_ss13.common.types.derived.ArrayType;
 import swp_compiler_ss13.common.types.derived.Member;
 import swp_compiler_ss13.common.types.derived.StructType;
 import swp_compiler_ss13.common.types.primitive.BooleanType;
 import swp_compiler_ss13.common.types.primitive.DoubleType;
 import swp_compiler_ss13.common.types.primitive.LongType;
 import swp_compiler_ss13.common.types.primitive.StringType;
 import swp_compiler_ss13.fuc.ast.ASTNodeImpl;
 import swp_compiler_ss13.fuc.ast.ArithmeticBinaryExpressionNodeImpl;
 import swp_compiler_ss13.fuc.ast.ArithmeticUnaryExpressionNodeImpl;
 import swp_compiler_ss13.fuc.ast.ArrayIdentifierNodeImpl;
 import swp_compiler_ss13.fuc.ast.AssignmentNodeImpl;
 import swp_compiler_ss13.fuc.ast.BasicIdentifierNodeImpl;
 import swp_compiler_ss13.fuc.ast.BinaryExpressionNodeImpl;
 import swp_compiler_ss13.fuc.ast.BlockNodeImpl;
 import swp_compiler_ss13.fuc.ast.BranchNodeImpl;
 import swp_compiler_ss13.fuc.ast.BreakNodeImpl;
 import swp_compiler_ss13.fuc.ast.DeclarationNodeImpl;
 import swp_compiler_ss13.fuc.ast.DoWhileNodeImpl;
 import swp_compiler_ss13.fuc.ast.LiteralNodeImpl;
 import swp_compiler_ss13.fuc.ast.LogicBinaryExpressionNodeImpl;
 import swp_compiler_ss13.fuc.ast.LogicUnaryExpressionNodeImpl;
 import swp_compiler_ss13.fuc.ast.PrintNodeImpl;
 import swp_compiler_ss13.fuc.ast.RelationExpressionNodeImpl;
 import swp_compiler_ss13.fuc.ast.ReturnNodeImpl;
 import swp_compiler_ss13.fuc.ast.StructIdentifierNodeImpl;
 import swp_compiler_ss13.fuc.ast.WhileNodeImpl;
 import swp_compiler_ss13.fuc.parser.grammar.Production;
 import swp_compiler_ss13.fuc.parser.grammar.ProjectGrammar;
 import swp_compiler_ss13.fuc.parser.grammar.Terminal;
 import swp_compiler_ss13.fuc.parser.grammar.TokenEx;
 import swp_compiler_ss13.fuc.parser.parser.ReduceAction.ReduceException;
 import swp_compiler_ss13.fuc.symbolTable.SymbolTableImpl;
 
 public class ProjectGrammarImpl implements IGrammarImpl {
 	// --------------------------------------------------------------------------
 	// --- variables and constants
 	// ----------------------------------------------
 	// --------------------------------------------------------------------------
 	private static final Object NO_VALUE = new String("NoValue");
 	
 	private final Logger log = Logger.getLogger(getClass());
 	
 	private ReportLog reportLog = null;
 
 	// --------------------------------------------------------------------------
 	// --- constructors
 	// ---------------------------------------------------------
 	// --------------------------------------------------------------------------
 	public ProjectGrammarImpl() {
 		
 	}
 	
 	@Override
 	public ReduceAction getReduceAction(Production prod) {
 		switch (prod.getStringRep()) {
 
 		case "program -> decls stmts":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					Object left = objs[0]; // Should be NO_VALUE or BlockNode
 					Object right = objs[1]; // Should be NO_VALUE or BlockNode
 
 					BlockNodeImpl block = joinBlocks(left, right);
 
 					return block;
 				}
 			};
 
 		case "block -> { decls stmts }":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					Object left = objs[1]; // Should be NO_VALUE or BlockNode
 					Object right = objs[2]; // Should be NO_VALUE or BlockNode
 					
 					BlockNodeImpl block = joinBlocks(left, right);
 					
 					Token leftBranch = unpack(objs[0], Token.class);
 					block.setCoverageAtFront(leftBranch);
 									
 					Token rightBranch = unpack(objs[3], Token.class);
 					block.setCoverage(rightBranch);
 										
 					return block;
 				}
 			};
 
 		case "decls -> decls decl":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					Object left = objs[0]; // Should be NO_VALUE, BlockNode or DeclarationNode
 					Object right = objs[1]; // Should be DeclarationNode or
 											// BlockNode
 
 					LinkedList<DeclarationNode> declList = new LinkedList<>();
 					// Handle left
 					if (!left.equals(NO_VALUE)) {
 						if (left instanceof BlockNode) {
 							BlockNode leftBlock = (BlockNode) left;
 							declList.addAll(leftBlock.getDeclarationList());
 						} else {
 							DeclarationNode declLeft = unpack(left, DeclarationNode.class);
 							declList.add(declLeft);
 						}
 					}
 
 					// Handle right
 					if (right instanceof BlockNode) {
 						BlockNode tmpBlock = (BlockNode) right;
 						declList.addAll(tmpBlock.getDeclarationList());
 					} else {
 						DeclarationNode declRight = unpack(right, DeclarationNode.class);
 						declList.add(declRight);
 					}
 
 					// Create new BlockNode
 					BlockNodeImpl block = new BlockNodeImpl();
 					block.setSymbolTable(new SymbolTableImpl());
 					for (DeclarationNode decl : declList) {
 						insertDecl(block, decl);
 						block.setCoverage(decl.coverage());
 					}
 					return block;
 				}
 			};
 
 		case "decls -> ε":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					return NO_VALUE; // Symbolizes epsilon
 				}
 			};
 
 		case "decl -> type id ;":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					DeclarationNode decl = unpack(objs[0], DeclarationNode.class);
 					Token idToken = unpack(objs[1], Token.class);
 					Token semicolon = unpack(objs[2], Token.class);
 					
 					if(decl.getType() instanceof ReduceStringType){
 						List<Token> coverage = decl.coverage();
 						decl = new DeclarationNodeImpl();
 						decl.setType(new StringType(LRParser.STRING_LENGTH));
 						((DeclarationNodeImpl)decl).setCoverage(coverage);
 					}
 
 					// Set ID
 					decl.setIdentifier(idToken.getValue());
 					
 					//Set left token
 					((DeclarationNodeImpl)decl).setCoverage(idToken, semicolon);
 					return decl;
 				}
 			};
 
 		case "stmts -> stmts stmt":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					Object left = objs[0]; // Should be NO_VALUE or BlockNode or other
 					// StatementNode
 					Object right = objs[1]; // Should be StatementNode or
 					// BlockNode
 
 					LinkedList<StatementNode> stmtList = new LinkedList<>();
 					// Handle left
 					if (!left.equals(NO_VALUE)) {
 						// there are just 0ne statement
 						if (left instanceof BlockNode) {
 
 							//looks for real Blocknode with own scope, coverage begins and end with { }
 							BlockNode block = (BlockNode) left;
 							if(block.coverage().get(0).getValue().equalsIgnoreCase("{") &&
 									block.coverage().get(block.coverage().size()-1).getValue().equalsIgnoreCase("}")){
 								stmtList.add((StatementNode)block);
 							}else{
 								stmtList.addAll(block.getStatementList());
 							}
 
 						} else {
 							StatementNode leftStmt = unpack(left, StatementNode.class);
 							stmtList.add(leftStmt);
 						}
 					}
 
 					// Handle right
 					if (right instanceof BlockNode) {
 						
 						//looks for real Blocknode with own scope, coverage begins and end with { }
 						BlockNode block = (BlockNode) right;
 						if(block.coverage().get(0).getValue().equalsIgnoreCase("{") &&
 								block.coverage().get(block.coverage().size()-1).getValue().equalsIgnoreCase("}")){
 							stmtList.add((StatementNode)block);
 							
 						} else {
 							stmtList.addAll(block.getStatementList());
 						}
 					} else {
 						StatementNode rightStmt = unpack(right, StatementNode.class);
 						stmtList.add(rightStmt);
 					}
 
 					// Create new BlockNode
 					BlockNodeImpl block = new BlockNodeImpl();
 					for (StatementNode stmt : stmtList) {
 						block.addStatement(stmt);
 						stmt.setParentNode(block);
 						block.setCoverage(stmt.coverage());
 					}
 					return block;
 				}
 			};
 
 		case "stmts -> ε":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					return NO_VALUE; // Symbolizes epsilon
 				}
 			};
 
 		case "stmt -> assign ;":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					AssignmentNodeImpl assign = unpack(objs[0], AssignmentNodeImpl.class);
 					Token semicolon = unpack(objs[1], Token.class);
 					assign.setCoverage(semicolon);
 					return  assign;
 				}
 			};
 		case "stmt -> block":
 			break;
 
 		case "stmt -> if ( assign ) stmt":
 			return new ReduceAction() {
 
 				@Override
 				public Object create(Object... objs) throws ReduceException {
 					Token ifToken = unpack(objs[0], Token.class);
 					Token leftBrace = unpack(objs[1], Token.class);
 					ExpressionNode condition = unpack(objs[2], ExpressionNode.class);
 					Token rightBrace = unpack(objs[3], Token.class);
 					StatementNode stmtTrue = unpack(objs[4], StatementNode.class);
 					
 					BranchNodeImpl node = new BranchNodeImpl();
 					node.setCoverage(ifToken, leftBrace);
 					
 					node.setCondition(condition);
 					node.setCoverage(condition.coverage());
 					
 					node.setCoverage(rightBrace);
 					
 					node.setStatementNodeOnTrue(stmtTrue);
 					node.setCoverage(stmtTrue.coverage());
 					
 					return node;
 				}
 				
 			};
 		case "stmt -> if ( assign ) stmt else stmt":
 			return new ReduceAction() {
 
 				@Override
 				public Object create(Object... objs) throws ReduceException {
 					Token ifToken = unpack(objs[0], Token.class);
 					Token leftBrace = unpack(objs[1], Token.class);
 					ExpressionNode condition = unpack(objs[2], ExpressionNode.class);
 					Token rightBrace = unpack(objs[3], Token.class);
 					StatementNode stmtTrue = unpack(objs[4], StatementNode.class);
 					Token elsee = unpack(objs[5], Token.class);
 					StatementNode stmtFalse = unpack(objs[6], StatementNode.class);
 
 					
 					BranchNodeImpl node = new BranchNodeImpl();
 					node.setCoverage(ifToken, leftBrace);
 					
 					node.setCondition(condition);
 					node.setCoverage(condition.coverage());
 					
 					node.setCoverage(rightBrace);
 					
 					node.setStatementNodeOnTrue(stmtTrue);
 					node.setCoverage(stmtTrue.coverage());
 							
 					node.setCoverage(elsee);
 					
 					node.setStatementNodeOnFalse(stmtFalse);
 					node.setCoverage(stmtFalse.coverage());
 					return node;
 				}
 
 			};
 			
 		case "stmt -> while ( assign ) stmt":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					WhileNodeImpl whileImpl = new WhileNodeImpl();
 					
 					Token whileToken = unpack(objs[0], Token.class);
 					Token paraLeft = unpack(objs[1], Token.class);
 					
 					whileImpl.setCoverage(whileToken, paraLeft);
 					
 					ExpressionNode condition = unpack(objs[2], ExpressionNode.class);
 					
 					whileImpl.setCondition(condition);
 					whileImpl.setCoverage(condition.coverage());
 					condition.setParentNode(whileImpl);
 					
 					Token paraRight = unpack(objs[3], Token.class);
 					
 					whileImpl.setCoverage(paraRight);
 					
 					StatementNode stmt = unpack(objs[4], StatementNode.class);
 					
 					whileImpl.setLoopBody(stmt);
 					whileImpl.setCoverage(stmt.coverage());
 					stmt.setParentNode(whileImpl);
 					
 					return whileImpl;
 				}
 
 			};
 			
 		case "stmt -> do stmt while ( assign ) ;":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					DoWhileNodeImpl whileImpl = new DoWhileNodeImpl();
 					
 					Token doToken = unpack(objs[0], Token.class);
 					
 					whileImpl.setCoverage(doToken);
 					
 					StatementNode stmt = unpack(objs[1], StatementNode.class);
 					
 					whileImpl.setLoopBody(stmt);
 					whileImpl.setCoverage(stmt.coverage());
 					stmt.setParentNode(whileImpl);
 					
 					Token whileToken = unpack(objs[2], Token.class);
 					Token paraLeft = unpack(objs[3], Token.class);
 					
 					whileImpl.setCoverage(whileToken,paraLeft);
 
 					ExpressionNode condition = unpack(objs[4], ExpressionNode.class);
 					
 					whileImpl.setCondition(condition);
 					whileImpl.setCoverage(condition.coverage());
 					condition.setParentNode(whileImpl);
 					
 					Token paraRight = unpack(objs[5], Token.class);
 					
 					Token sem = unpack(objs[6], Token.class);
 					
 					whileImpl.setCoverage(paraRight,sem);
 					
 					return whileImpl;
 				}
 
 			};
 
 		case "stmt -> break ;":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					Token breakk = unpack(objs[0], Token.class);
 					Token sem = unpack(objs[1], Token.class);
 					
 					BreakNodeImpl breakImpl = new BreakNodeImpl();
 					breakImpl.setCoverage(breakk, sem);
 					return new BreakNodeImpl();
 				}
 
 			};
 		case "stmt -> return ;":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					Token returnn = unpack(objs[0], Token.class);
 					Token sem = unpack(objs[1], Token.class);
 					
 					ReturnNodeImpl returnImpl = new ReturnNodeImpl();
 					returnImpl.setCoverage(returnn, sem);
 					return returnImpl;
 				}
 			};
 		case "stmt -> return loc ;":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					Token returnn = unpack(objs[0], Token.class);
 					IdentifierNode identifier = unpack(objs[1], IdentifierNode.class);
 					Token sem = unpack(objs[2], Token.class);
 					
 					ReturnNodeImpl returnNode = new ReturnNodeImpl();
 					returnNode.setRightValue(identifier);
 					identifier.setParentNode(returnNode);
 					returnNode.setCoverage(returnn);
 					returnNode.setCoverage(identifier.coverage());
 					returnNode.setCoverage(sem);
 					return returnNode;
 				}
 			};
 			
 		case "stmt -> print loc ;":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					Token print = unpack(objs[0], Token.class);
 					IdentifierNode identifier = unpack(objs[1], IdentifierNode.class); // IdentifierNode expected
 					Token sem = unpack(objs[2], Token.class);
 					
 					PrintNodeImpl printNode = new PrintNodeImpl();
 					
 					printNode.setCoverage(print);
 					
 					printNode.setRightValue(identifier);
 					printNode.setCoverage(identifier.coverage());
 					
 					printNode.setCoverage(sem);
 					
 					return printNode;
 				}
 			};
 			
 		case "loc -> loc [ assign ]":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					ArrayIdentifierNodeImpl arrayIdentifier = new ArrayIdentifierNodeImpl();
 					
 					IdentifierNode identifier = unpack(objs[0], IdentifierNode.class);
 					
 					arrayIdentifier.setIdentifierNode(identifier);
 					arrayIdentifier.setCoverage(identifier.coverage());
 					identifier.setParentNode(arrayIdentifier);
 					
 					Token leftSquareBracket = unpack(objs[1], Token.class);
 					arrayIdentifier.setCoverage(leftSquareBracket);
 					
 					ExpressionNode assign = unpack(objs[2], ExpressionNode.class);
 					arrayIdentifier.setIndexNode(assign);
 					arrayIdentifier.setCoverage(assign.coverage());
 					assign.setParentNode(arrayIdentifier);
 					
 					Token rightSquareBracket = unpack(objs[3], Token.class);
 					arrayIdentifier.setCoverage(rightSquareBracket);
 					return arrayIdentifier;
 				}
 			};
 
 		case "loc -> id":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					Token token = unpack(objs[0], Token.class);
 					
 					BasicIdentifierNodeImpl identifierNode = new BasicIdentifierNodeImpl();
 					identifierNode.setIdentifier(token.getValue());
 					identifierNode.setCoverage(token);
 					return identifierNode;
 				}
 			};
 		case "loc -> loc . id":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 
 					StructIdentifierNodeImpl identifierNode = new StructIdentifierNodeImpl();
 					
 					IdentifierNode identifier = unpack(objs[0], IdentifierNode.class);
 					identifierNode.setIdentifierNode(identifier);
 					identifierNode.setCoverage(identifier.coverage());
 					identifier.setParentNode(identifierNode);
 					
 					Token dot = unpack(objs[1], Token.class);
 					identifierNode.setCoverage(dot);
 					
 					Token token = unpack(objs[2], Token.class);
 					identifierNode.setFieldName(token.getValue());
 					identifierNode.setCoverage(token);
 					
 					return identifierNode;
 				}
 			};
 		case "assign -> loc = assign":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					AssignmentNodeImpl assignNode = new AssignmentNodeImpl();
 					
 					IdentifierNode identifier = unpack(objs[0], IdentifierNode.class);
 					Token equalSign = unpack(objs[1], Token.class);
 					ExpressionNode node = unpack(objs[2], ExpressionNode.class);
 					
 					assignNode.setLeftValue(identifier);
 					assignNode.getLeftValue().setParentNode(assignNode);
 					assignNode.setRightValue(node); 
 					assignNode.getRightValue().setParentNode(assignNode);
 					
 					assignNode.setCoverage(identifier.coverage());
 					assignNode.setCoverage(equalSign);
 					assignNode.setCoverage(node.coverage());
 					return assignNode;
 				}
 			};
 		case "assign -> bool":
 			break;
 		case "bool -> bool || join":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					return createBinaryExpr(reportLog, BinaryOperator.LOGICAL_OR, "||", objs);
 				}
 			};
 		case "bool -> join":
 			break;	// Nothing to do here
 		case "join -> join && equality":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					return createBinaryExpr(reportLog, BinaryOperator.LOGICAL_AND, "&&", objs);
 				}
 			};
 		case "join -> equality":
 			break; // Nothing to do here
 
 		case "equality -> equality == rel":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					return createBinaryExpr(reportLog, BinaryOperator.EQUAL, "==", objs);
 				}
 			};
 		case "equality -> equality != rel":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					return createBinaryExpr(reportLog, BinaryOperator.INEQUAL, "!=", objs);
 				}
 			};
 		case "equality -> rel":
 			break; // Nothing to do here
 		case "rel -> expr < expr":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					return createBinaryExpr(reportLog, BinaryOperator.LESSTHAN, "<", objs);
 				}
 			};
 		case "rel -> expr > expr":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					return createBinaryExpr(reportLog, BinaryOperator.GREATERTHAN, "<", objs);
 				}
 			};
 		case "rel -> expr >= expr":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					return createBinaryExpr(reportLog, BinaryOperator.GREATERTHANEQUAL, ">=", objs);
 				}
 			};
 		case "rel -> expr <= expr":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					return createBinaryExpr(reportLog, BinaryOperator.LESSTHANEQUAL, "<=", objs);
 				}
 			};
 		case "rel -> expr":
 			break; // Nothing to do here
 		case "expr -> expr + term":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					return createBinaryExpr(reportLog, BinaryOperator.ADDITION, "+", objs);
 				}
 			};
 		case "expr -> expr - term":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					return createBinaryExpr(reportLog, BinaryOperator.SUBSTRACTION, "-", objs);
 				}
 			};
 		case "expr -> term":
 			break; // Nothing to do here
 		case "term -> term * unary":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					return createBinaryExpr(reportLog, BinaryOperator.MULTIPLICATION, "*", objs);
 				}
 			};
 		case "term -> term / unary":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					return createBinaryExpr(reportLog, BinaryOperator.DIVISION, "/", objs);
 				}
 			};
 		case "term -> unary":
 			break; // Nothing to do here
 		case "unary -> ! unary":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					LogicUnaryExpressionNodeImpl unary = new LogicUnaryExpressionNodeImpl();
 					
 					Token token = unpack(objs[0], Token.class);
 					ExpressionNode expr = unpack(objs[1], ExpressionNode.class);
 
 					unary.setOperator(UnaryOperator.LOGICAL_NEGATE);
 					unary.setRightValue(expr);
 					expr.setParentNode(unary);
 					unary.setCoverage(token);
 					unary.setCoverage(expr.coverage());
 					
 					return unary;
 				}
 			};
 		case "unary -> - unary":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					Token token = unpack(objs[0], Token.class);
 					ExpressionNode expr = unpack(objs[1], ExpressionNode.class);
 
 					ArithmeticUnaryExpressionNodeImpl arithUnary = new ArithmeticUnaryExpressionNodeImpl();
 					arithUnary.setOperator(UnaryOperator.MINUS);
 					arithUnary.setRightValue(expr);
 					expr.setParentNode(arithUnary);
 					
 					arithUnary.setCoverage(token);
 					arithUnary.setCoverage(expr.coverage());
 					
 					return arithUnary;
 				}
 			};
 		case "factor -> ( assign )":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					Token lb = unpack(objs[0], Token.class);
 					ASTNodeImpl astNode = unpack(objs[1], ASTNodeImpl.class);
 					Token rb = unpack(objs[2], Token.class);
 					
 					astNode.setCoverageAtFront(lb);
 					astNode.setCoverage(rb);
 					
 					return  astNode;
 				}
 			};
 		case "unary -> factor":
 		case "factor -> loc":
 			break;	// Nothing to do here
 		case "factor -> num":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					Token token = unpack(objs[0], Token.class);
 					
 					LiteralNodeImpl literal = new LiteralNodeImpl();
 					literal.setLiteral(token.getValue());
 					literal.setLiteralType(new LongType());
 					literal.setCoverage(token);
 					return literal;
 				}
 			};
 		case "factor -> real":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					Token token = unpack(objs[0], Token.class);
 					
 					LiteralNodeImpl literal = new LiteralNodeImpl();
 					literal.setLiteral(token.getValue());
 					literal.setLiteralType(new DoubleType());
 					literal.setCoverage(token);
 					return literal;
 				}
 			};
 		case "factor -> true":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					Token token = unpack(objs[0], Token.class);
 					
 					LiteralNodeImpl literal = new LiteralNodeImpl();
 					literal.setLiteral(token.getValue());
 					literal.setLiteralType(new BooleanType());
 					literal.setCoverage(token);
 					return literal;
 				}
 			};
 		case "factor -> false":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					Token token = unpack(objs[0], Token.class);
 					
 					LiteralNodeImpl literal = new LiteralNodeImpl();
 					literal.setLiteral(token.getValue());
 					literal.setLiteralType(new BooleanType());
 					literal.setCoverage(token);
 					return literal;
 				}
 			};
 		case "factor -> string":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					Token token = unpack(objs[0], Token.class);
 					
 					LiteralNodeImpl literal = new LiteralNodeImpl();
 					literal.setLiteral(token.getValue());
 					literal.setLiteralType(new StringType((long) token
 							.getValue().length()));
 					literal.setCoverage(token);
 					return literal;
 				}
 
 			};
 		case "type -> type [ num ]":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					//typeToken DeclarationNode is wasted after reduce, not needed any more
 					DeclarationNode typeToken = unpack(objs[0], DeclarationNode.class);
 					Token leftBrace = unpack(objs[1], Token.class);
 					NumToken size = unpack(objs[2], NumToken.class);
 					Token rightBrace = unpack(objs[3], Token.class);
 					
 					//create Array declaration
 					Type type = new ArrayType(typeToken.getType(), size.getLongValue().intValue());
 					DeclarationNodeImpl declImpl = new DeclarationNodeImpl();
 					declImpl.setType(type);
 					
 					//set coverage
 					declImpl.setCoverage(typeToken.coverage());
 					declImpl.setCoverage(leftBrace,size,rightBrace);
 					return declImpl;
 				}
 
 			};
 		case "type -> basic":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					Token token = unpack(objs[0], Token.class);
 					DeclarationNodeImpl decl = new DeclarationNodeImpl();
 					
 					//look for BasicType and reduce
 					switch(token.getTokenType()){
 				
 					case BOOL_SYMBOL:
 						decl.setType(new BooleanType());
 						decl.setCoverage(token);
 						return decl;
 						
 					case DOUBLE_SYMBOL:
 						decl.setType(new DoubleType());
 						decl.setCoverage(token);
 						return decl;
 						
 					case LONG_SYMBOL:
 						decl.setType(new LongType());
 						decl.setCoverage(token);
 						return decl;
 						
 					case STRING_SYMBOL:
 						//decl is thrown away afterwards, nobody has to know ReduceStringType
 						decl.setType(new ReduceStringType(Type.Kind.STRING));
 						decl.setCoverage(token);
 						return decl;
 					
 					default:
 						throw new ParserException("Received a Token of unexpected basic type: " + token.toString());
 					}
 				}
 			};
 
 		case "type -> record { decls }":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ReduceException  {
 					
 					DeclarationNodeImpl struct = new DeclarationNodeImpl();
 					
 					Token record = unpack(objs[0], Token.class);
 					Token lcb = unpack(objs[1], Token.class);
 					
 					struct.setCoverage(record, lcb);
 					
 					BlockNode block = unpack(objs[2], BlockNode.class);
 					
 					List<DeclarationNode> decls = block.getDeclarationList();
 					
 					int size = decls.size();
 					Member[] members = new Member[size];
 					
 					for(int i = 0; i<size; i++){
 						DeclarationNode declarationNode = decls.get(i);
 						members[i] = new Member(declarationNode.getIdentifier(), declarationNode.getType());
 						struct.setCoverage(declarationNode.coverage());
 					}
 					
 					StructType type = new StructType("record", members);
 					struct.setType(type);
 					
 					Token rcb = unpack(objs[3], Token.class);
 					
 					struct.setCoverage(rcb);
 					
 					return struct;
 				}
 
 			};
 		default:
 			return null; // Means "no ReduceAction to perform"
 		}
 		return null;
 	}
 
 
 	/**
 	 * Inserts the {@link DeclarationNode} into the block and its {@link SymbolTable} safely.
 	 * 
 	 * @param block
 	 * @param decl
 	 */
 	private void insertDecl(BlockNode block, DeclarationNode decl) throws ReduceException {
 		//Here is no coverage to set.
 		SymbolTable symbolTable = block.getSymbolTable();
 		if (symbolTable.isDeclaredInCurrentScope(decl.getIdentifier())) {
 			reportLog.reportError(ReportType.DOUBLE_DECLARATION, decl.coverage(), "The variable '" + 
 			decl.getIdentifier() + "' of type '" + decl.getType() + "' has been declared twice in this scope!");
 			throw new ParserException("double id exception");
 		}
 		block.addDeclaration(decl);
 		block.getSymbolTable().insert(decl.getIdentifier(), decl.getType());
 		decl.setParentNode(block);
 		
 	}
 
 	private BlockNodeImpl joinBlocks(Object left, Object right) throws ReduceException {
 		BlockNodeImpl newBlock = new BlockNodeImpl();
 		newBlock.setSymbolTable(new SymbolTableImpl());
 		
 		// Handle left
 		if (!left.equals(NO_VALUE)) {
 			BlockNode declsBlock = (BlockNode) left;
 			for (DeclarationNode decl : declsBlock.getDeclarationList()) {
 				insertDecl(newBlock, decl);
 				decl.setParentNode(newBlock);
 				newBlock.setCoverage(decl.coverage());
 			}
 		}
 
 		// Handle right
 		if (!right.equals(NO_VALUE)) {
 			BlockNode stmtsBlock = (BlockNode) right;
 			for (StatementNode stmt : stmtsBlock.getStatementList()) {
 				
 				//look for Blocknodes, Loopnodes and Branchnodes and concat Symboltables
 				if (stmt instanceof BranchNode){
 					BranchNode branch = (BranchNode)stmt;
 					if (branch.getStatementNodeOnTrue() instanceof BlockNode){
 						BlockNode node = (BlockNode) branch.getStatementNodeOnTrue();
 						((SymbolTableImpl)node.getSymbolTable()).setParent(newBlock.getSymbolTable());
 					}
 					if (branch.getStatementNodeOnFalse() instanceof BlockNode){
 						BlockNode node = (BlockNode) branch.getStatementNodeOnFalse();
 						((SymbolTableImpl)node.getSymbolTable()).setParent(newBlock.getSymbolTable());
 					}					
 				} else {
 					if (stmt instanceof LoopNode){
 						LoopNode loop = (LoopNode)stmt;
 						StatementNode loopBody = loop.getLoopBody();
 						if(loopBody instanceof BlockNode){
 							BlockNode node = (BlockNode) loopBody;
 							((SymbolTableImpl)node.getSymbolTable()).setParent(newBlock.getSymbolTable());
 						}
 					} else {
 						if (stmt instanceof BlockNode){
 							((SymbolTableImpl)((BlockNode)stmt).getSymbolTable()).setParent(newBlock.getSymbolTable());
 						} else if (stmt instanceof LoopNode) {
 							StatementNode childStmt = ((LoopNode)stmt).getLoopBody();
 							if (childStmt instanceof BlockNode) {
 								BlockNode block = (BlockNode) childStmt;
 								((SymbolTableImpl)block.getSymbolTable()).setParent(newBlock.getSymbolTable());
 							}
 						}
 					}
 				}
 				
 				newBlock.addStatement(stmt);
 				stmt.setParentNode(newBlock);
 				newBlock.setCoverage(stmt.coverage());
 			}
 		}
 		
 		return newBlock;
 	}
 	
 	/**
 	 * @param reportLog
 	 * @param objs
 	 * @return
 	 * @throws ReduceException 
 	 */
 	private Object createBinaryExpr(final ReportLog reportLog,  
 			final BinaryOperator op, String opStr,
 			Object... objs) throws ReduceException {
 		ExpressionNode leftExpr = unpack(objs[0], ExpressionNode.class);
 		Token opToken = unpack(objs[1], Token.class);
 		ExpressionNode rightExpr = unpack(objs[2], ExpressionNode.class);
 		
 		BinaryExpressionNodeImpl binExpr = null;
 		switch (op) {
 		case ADDITION:
 		case DIVISION:
 		case MULTIPLICATION:
 		case SUBSTRACTION:
 			binExpr = new ArithmeticBinaryExpressionNodeImpl();
 			break;
 		case EQUAL:
 		case INEQUAL:
 		case GREATERTHAN:
 		case GREATERTHANEQUAL:
 		case LESSTHAN:
 		case LESSTHANEQUAL:
 			binExpr = new RelationExpressionNodeImpl();
 			break;
 		case LOGICAL_AND:
 		case LOGICAL_OR:
 			binExpr = new LogicBinaryExpressionNodeImpl();
 			break;
 			default:
 				
 		}
 		
 		binExpr.setLeftValue(leftExpr);
 		binExpr.setCoverage(leftExpr.coverage());
 		leftExpr.setParentNode(binExpr);
 		
 
 		binExpr.setCoverage(opToken);					
 		binExpr.setOperator(op);
 		
 		
 		binExpr.setRightValue(rightExpr);
 		binExpr.setCoverage(rightExpr.coverage());
 		rightExpr.setParentNode(binExpr);
 		
 		return binExpr;
 	}
 	
 	
 	/**
 	 * Casts the given object to an instance of the given class. If an error
 	 * occurs, an {@link ReduceException} is thrown.
 	 * 
 	 * @param obj
 	 * @param clazz
 	 * @return
 	 * @throws ReduceException 
 	 */
 	private <T> T unpack(Object obj, Class<T> clazz) throws ReduceException {
 		try {
 			return clazz.cast(obj);
 		} catch (ClassCastException cce) {
 			throw new ReduceException(obj, clazz, cce);
 		}
 	}
 
 	private static class ReduceStringType extends Type {
 		/**
 		 * its not possible to create a StringType without the length, so
 		 * we need a dummy class to do it right
 		 */
 		protected ReduceStringType(Kind kind) {
 			super(kind);
 		}
 
 		@Override
 		public String getTypeName() {
 			return "String";
 		}
 
 		@Override
 		public String toString() {
 			return getTypeName();
 		}
 	}
 	
 	// --------------------------------------------------------------------------
 	// --- error recovery
 	// ---------------------------------------------------------
 	// --------------------------------------------------------------------------
 	@Override
 	public RecoveryResult tryErrorRecovery(List<Terminal> possibleTerminals, TokenEx curToken,
 			TokenEx lastToken, Stack<Object> valueStack) {
 		if (possibleTerminals.size() == 1) {
 			// Easy! Only one option, do it.
 			Terminal terminal = possibleTerminals.get(0);
 			TokenType newTokenType = terminal.getTokenTypes().next();	// This works for this grammar..
 			return insertTerminal(curToken, lastToken, terminal, terminal.getId(), newTokenType);
 		} else if (curToken.getTokenType() == TokenType.LEFT_BRACE &&
 				possibleTerminals.contains(ProjectGrammar.Complete.rb) &&
 				containsTokensOfType(valueStack, TokenType.IF, TokenType.LEFT_PARAN)) {
 			
 			return insertTerminal(curToken, lastToken, ProjectGrammar.Complete.rb,
 					")", TokenType.RIGHT_PARAN);
 		} else if (possibleTerminals.contains(ProjectGrammar.Complete.sem) &&
 				(containsTokensOfType(valueStack, TokenType.ASSIGNOP) ||
 				containsTokensOfType(valueStack, TokenType.PRINT) ||
 				containsTokensOfType(valueStack, TokenType.RETURN) ||
 				containsTokensOfType(valueStack, TokenType.BOOL_SYMBOL) ||
 				containsTokensOfType(valueStack, TokenType.LONG_SYMBOL) ||
 				containsTokensOfType(valueStack, TokenType.DOUBLE_SYMBOL) ||
 				containsTokensOfType(valueStack, TokenType.STRING_SYMBOL))) {
 			// Next symbol might be a semicolon and we are either:
 			// - on the rhs of an assignment
 			// - on the rhs of a print or return statement
 			// - on a decl statement (unreduced type symbol)
 			return insertTerminal(curToken, lastToken, ProjectGrammar.Complete.sem,
 					";", TokenType.SEMICOLON);
 		} else if ((curToken.getTokenType() == TokenType.ELSE ||
 				curToken.getTokenType() == TokenType.EOF) &&
 				possibleTerminals.contains(ProjectGrammar.Complete.rcb)) {
 			// We seem to be missing an closing curly brace before the else (or EOF)!
 			return insertTerminal(curToken, lastToken, ProjectGrammar.Complete.rcb,
 					"}", TokenType.RIGHT_BRACE);
 		}
 		return null;
 	}
 	
 	/**
 	 * Inserts a fresh token into the token stream just before the one that was
 	 * currently read
 	 * 
 	 * @param curToken The currently read {@link Token}
 	 * @param lastToken The {@link Token} that was read before
 	 * @param newTerminal The {@link Terminal} for the fresh {@link Token}
 	 * @param newTokenVal The value for the fresh {@link Token}
 	 * @param newTokenType The {@link TokenType} for the fresh {@link Token}
 	 * @return The necessary changes to the parsers state
 	 */
 	private RecoveryResult insertTerminal(TokenEx curToken, TokenEx lastToken,
 			Terminal newTerminal, String newTokenVal, TokenType newTokenType) {
 		log.debug("------ starting error recovery ------");
 		log.debug("Found possible next terminal: " + newTerminal);
 		
 		// Modify token stream
 		TokenEx newToken = new TokenEx(newTokenVal, newTokenType, lastToken.getLine(),
 				lastToken.getColumn() + 1, newTerminal);
 		log.debug("Error recovery inserted " + newToken + " before " + curToken + ", lets see if this works...");
 		
		reportLog.reportWarning(ReportType.UNDEFINED, Arrays.<Token>asList(curToken),
 				"Error recovery inserted a missing '" + newTokenVal + "' before " + curToken + "!");
 		
 		// Give it a shot!
 		log.debug("------ end error recovery ------");
 		return new RecoveryResult(newToken, curToken);	// Re-insert curToken into token stream
 	}
 	
 	/**
 	 * @param valueStack The parsers current value stack
 	 * @param types The type-sequence which the method should search for
 	 * @return Whether there are one or more occurrences of the given
 	 * 		types-sequence
 	 */
 	private static boolean containsTokensOfType(Stack<Object> valueStack, TokenType... types) {
 		return countTokensOfType(valueStack, types) > 0;
 	}
 	
 	/**
 	 * @param valueStack The parsers current value stack
 	 * @param types The type-sequence which the method should search for
 	 * @return How much occurrences of the given types-sequence there are on
 	 * 		the stack
 	 */
 	private static int countTokensOfType(Stack<Object> valueStack, TokenType... types) {
 		int occurrences = 0;
 		Iterator<Object> it = valueStack.iterator();
 		while (it.hasNext()) {
 			if (match(it, Arrays.asList(types).iterator())) {
 				occurrences++;
 			}
 		}
 		return occurrences;
 	}
 	
 	/**
 	 * @param objs The value stacks iterator
 	 * @param types The type-sequence the method should loop for
 	 * @return Whether the given types-sequence could be matched against the
 	 * 		value stack
 	 */
 	private static boolean match(Iterator<Object> objs, Iterator<TokenType> types) {
 		while (types.hasNext() && objs.hasNext()) {
 			Object obj = objs.next();
 			if (obj instanceof Token) {
 				Token t = (Token) obj;
 				TokenType type = types.next();
 				if (t.getTokenType() != type) {
 					return false;
 				}
 			} else {
 				return false;
 			}
 		}
 		return !types.hasNext();
 	}
 	
 	
 	@Override
 	public void setReportLog(ReportLog reportLog) {
 		this.reportLog = reportLog;
 	}
 }
