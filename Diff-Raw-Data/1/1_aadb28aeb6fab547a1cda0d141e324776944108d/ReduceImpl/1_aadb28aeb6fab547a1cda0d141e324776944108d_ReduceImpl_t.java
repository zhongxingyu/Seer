 package swp_compiler_ss13.fuc.parser.parser;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import swp_compiler_ss13.common.ast.ASTNode;
 import swp_compiler_ss13.common.ast.nodes.ExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.IdentifierNode;
 import swp_compiler_ss13.common.ast.nodes.StatementNode;
 import swp_compiler_ss13.common.ast.nodes.binary.AssignmentNode;
 import swp_compiler_ss13.common.ast.nodes.binary.BinaryExpressionNode.BinaryOperator;
 import swp_compiler_ss13.common.ast.nodes.binary.DoWhileNode;
 import swp_compiler_ss13.common.ast.nodes.binary.WhileNode;
 import swp_compiler_ss13.common.ast.nodes.leaf.LiteralNode;
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
 import swp_compiler_ss13.fuc.symbolTable.SymbolTableImpl;
 
 public class ReduceImpl {
 	
 	
 	// --------------------------------------------------------------------------
 	// --- variables and constants
 	// ----------------------------------------------
 	// --------------------------------------------------------------------------
 	private static final Object NO_VALUE = new String("NoValue");
 
 	private static final Logger log = Logger.getLogger(ReduceImpl.class);
 
 	// --------------------------------------------------------------------------
 	// --- constructors
 	// ---------------------------------------------------------
 	// --------------------------------------------------------------------------
 	
 	/**
 	 * Defines a ReduceAction for every rule in the Grammar
 	 * @param prod 
 	 * @param reportLog
 	 * @return
 	 */
 	public static ReduceAction getReduceAction(Production prod, final ReportLog reportLog) {
 		switch (prod.getStringRep()) {
 
 		case "program -> decls stmts":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					Object left = objs[0]; // Should be NO_VALUE or BlockNode
 					Object right = objs[1]; // Should be NO_VALUE or BlockNode
 
 					BlockNodeImpl block = joinBlocks(left, right, reportLog);
 
 					return block;
 				}
 			};
 
 		case "block -> { decls stmts }":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					Object left = objs[1]; // Should be NO_VALUE or BlockNode
 					Object right = objs[2]; // Should be NO_VALUE or BlockNode
 					
 					BlockNodeImpl block = joinBlocks(left, right, reportLog);
 					
 					Token leftBranch = (Token)objs[0];
 					block.setCoverageAtFront(leftBranch);
 									
 					Token rightBranch = (Token)objs[3];
 					block.setCoverage(rightBranch);
 										
 					return block;
 				}
 			};
 
 		case "decls -> decls decl":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					Object left = objs[0]; // Should be NO_VALUE, BlockNode or DeclarationNode
 					Object right = objs[1]; // Should be DeclarationNode or
 											// BlockNode
 
 					LinkedList<DeclarationNode> declList = new LinkedList<>();
 					// Handle left
 					if (!left.equals(NO_VALUE)) {
 						if (left instanceof BlockNode) {
 							BlockNode leftBlock = (BlockNode) left;
 							declList.addAll(leftBlock.getDeclarationList());
 						} else if (!(left instanceof DeclarationNode)) {
 							log.error("Error in decls -> decls decl: Left must be a DeclarationNode!");
 						} else {
 							declList.add((DeclarationNode) left);
 						}
 					}
 
 					// Handle right
 					if (right instanceof BlockNode) {
 						BlockNode tmpBlock = (BlockNode) right;
 						declList.addAll(tmpBlock.getDeclarationList());
 					} else {
 						if (!(right instanceof DeclarationNode)) {
 							log.error("Error in decls -> decls decl: Right must be a DeclarationNode!");
 						} else {
 							declList.add((DeclarationNode) right);
 						}
 					}
 
 					// Create new BlockNode
 					BlockNodeImpl block = new BlockNodeImpl();
 					block.setSymbolTable(new SymbolTableImpl());
 					for (DeclarationNode decl : declList) {
 						insertDecl(block, decl, reportLog);
 						block.setCoverage(decl.coverage());
 					}
 					return block;
 				}
 			};
 
 		case "decls -> ε":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					return NO_VALUE; // Symbolizes epsilon
 				}
 			};
 
 		case "decl -> type id ;":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					
 					if(!(objs[0] instanceof DeclarationNode)){
 						if(objs[0] instanceof ASTNode){
 							reportLog.reportError(ReportType.UNDEFINED, ((ASTNode)objs[0]).coverage(), "there is no Declarationnode found!");
 							throw new ParserException("Declarationnode expected");
 						}
 						if(objs[0] instanceof Token){
 							List<Token> list = new ArrayList<Token>();
 							list.add((Token)objs[0]);
 							reportLog.reportError(ReportType.UNDEFINED, list, "there is no Declarationnode found!");
 							throw new ParserException("Declarationnode expected");
 
 						}
 					}
 					
 					DeclarationNode decl = (DeclarationNode) objs[0];
 					
 					if(!(objs[1] instanceof Token)){
 						writeReportError(reportLog, objs[1], "Identifier");
 					}
 					
 					Token idToken = (Token) objs[1];
 					
 					if(!(objs[2] instanceof Token || ((Token)objs[2]).getTokenType()!=TokenType.SEMICOLON)){
 						writeReportError(reportLog, objs[2], "Token ;");
 					}
 					
 					Token semicolon = (Token) objs[2];
 					
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
 				public Object create(Object... objs) throws ParserException  {
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
 
 						} else if (!(left instanceof StatementNode)) {
 							log.error("Error in decls -> decls decl: Left must be a DeclarationNode!");
 						} else {
 							stmtList.add((StatementNode) left);
 						}
 					}
 
 					// Handle right
 					if (right instanceof BlockNode) {
 						
 						//looks for real Blocknode with own scope, coverage begins and end with { }
 						BlockNode block = (BlockNode) right;
 						if(block.coverage().get(0).getValue().equalsIgnoreCase("{") &&
 								block.coverage().get(block.coverage().size()-1).getValue().equalsIgnoreCase("}")){
 							stmtList.add((StatementNode)block);
 							
 						}else{
 							stmtList.addAll(block.getStatementList());
 						}
 					} else {
 						if (!(right instanceof StatementNode)) {
 							log.error("Error in decls -> decls decl: Right must be a DeclarationNode!");
 						} else {
 							stmtList.add((StatementNode) right);
 						}
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
 				public Object create(Object... objs) throws ParserException  {
 					return NO_VALUE; // Symbolizes epsilon
 				}
 			};
 
 		case "stmt -> assign ;":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					AssignmentNode assign = (AssignmentNode) objs[0];
 					Token semicolon = (Token) objs[1];
 					((AssignmentNodeImpl)assign).setCoverage(semicolon);
 					return  assign;
 				}
 			};
 		case "stmt -> block":
 			break;
 
 		case "stmt -> if ( assign ) stmt":
 			return new ReduceAction() {
 
 				@Override
 				public Object create(Object... objs) throws ParserException {
 					Token ifToken = (Token)objs[0];
 					Token leftBrace = (Token)objs[1];
 					Object assign = objs[2];
 					Token rightBrace = (Token)objs[3];
 					Object stmtTrue = objs[4];
 
 					
 					BranchNodeImpl node = new BranchNodeImpl();
 					node.setCoverage(ifToken, leftBrace);
 					
 					
 					if(assign instanceof ExpressionNode){
 						ExpressionNode condition = (ExpressionNode)assign;
 						node.setCondition(condition);
 						node.setCoverage(condition.coverage());
 					}else{
 						writeReportError(reportLog, assign, "Expression");
 					}
 					
 					node.setCoverage(rightBrace);
 					
 					if(stmtTrue instanceof BlockNode){
 						node.setStatementNodeOnTrue((BlockNode)stmtTrue);
 					}else{
 						if(stmtTrue instanceof StatementNode){
 							StatementNode block = (StatementNode)stmtTrue;
 							node.setStatementNodeOnTrue(block);
 							node.setCoverage(block.coverage());
 						}else{
 							writeReportError(reportLog, stmtTrue, "Block or Statement");
 						}
 					}
 					
 					return node;
 				}
 				
 			};
 		case "stmt -> if ( assign ) stmt else stmt":
 			return new ReduceAction() {
 
 				@Override
 				public Object create(Object... objs) throws ParserException {
 					Token ifToken = (Token)objs[0];
 					Token leftBrace = (Token)objs[1];
 					Object assign = objs[2];
 					Token rightBrace = (Token)objs[3];
 					Object stmtTrue = objs[4];
 					Token elsee = (Token)objs[5];
 					Object stmtFalse = objs[6];
 
 					
 					BranchNodeImpl node = new BranchNodeImpl();
 					node.setCoverage(ifToken, leftBrace);
 					
 					if(assign instanceof ExpressionNode){
 						ExpressionNode condition = (ExpressionNode) assign;
 						node.setCondition(condition);
 						node.setCoverage(condition.coverage());
 					}else{
 						writeReportError(reportLog, assign, "Expression");
 					}
 					
 					node.setCoverage(rightBrace);
 					
 					if(stmtTrue instanceof BlockNode){
 						BlockNode block = (BlockNode)stmtTrue;
 						node.setStatementNodeOnTrue(block);
 						node.setCoverage(block.coverage());
 					}else{
 						if(stmtTrue instanceof StatementNode){
 							StatementNode block = (StatementNode)stmtTrue;
 							node.setStatementNodeOnTrue(block);
 							node.setCoverage(block.coverage());
 						}else{
 							writeReportError(reportLog, stmtTrue, "Statement or BlockNode");
 						}
 					}
 					
 					node.setCoverage(elsee);
 							
 					if(stmtFalse instanceof BlockNode){
 						BlockNode block = (BlockNode)stmtFalse;
 						node.setStatementNodeOnFalse(block);
 						node.setCoverage(block.coverage());
 					}else{
 						if(stmtFalse instanceof StatementNode){
 							StatementNode block = (StatementNode)stmtFalse;
 							node.setStatementNodeOnFalse(block);
 							node.setCoverage(block.coverage());						
 						}else{
 							writeReportError(reportLog, stmtFalse, "Block or Statement");
 						}
 					}
 					return node;
 				}
 
 			};
 			
 		case "stmt -> while ( assign ) stmt":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					WhileNodeImpl whileImpl = new WhileNodeImpl();
 					
 					Token whileToken = (Token)objs[0];
 					Token paraLeft = (Token)objs[1];
 					
 					whileImpl.setCoverage(whileToken,paraLeft);
 					
 					Object assign = objs[2];
 					
 					if(assign instanceof ExpressionNode){
 						ExpressionNode expression = (ExpressionNode) assign;
 						whileImpl.setCondition(expression);
 						whileImpl.setCoverage(expression.coverage());
 						expression.setParentNode(whileImpl);
 					}else{
 						writeReportError(reportLog, assign, "Expression");
 					}
 					
 					Token paraRight = (Token)objs[3];
 					
 					whileImpl.setCoverage(paraRight);
 					
 					Object stmt = objs[2];
 					
 					if(stmt instanceof StatementNode){
 						StatementNode block = (StatementNode) stmt;
 						//TODO: whileImpl.setLoopBody(block);
 						whileImpl.setCoverage(block.coverage());
 						block.setParentNode(whileImpl);
 					}else{
 						if(stmt instanceof BlockNode){
 							BlockNode block = (BlockNode) stmt;
 							whileImpl.setLoopBody(block);
 							whileImpl.setCoverage(block.coverage());
 							block.setParentNode(whileImpl);
 						}
 						writeReportError(reportLog, stmt, "Statement");
 					}
 					
 					return whileImpl;
 				}
 
 			};
 			
 		case "stmt -> do stmt while ( assign )":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					DoWhileNodeImpl whileImpl = new DoWhileNodeImpl();
 					
 					Token doToken = (Token)objs[0];
 					
 					whileImpl.setCoverage(doToken);
 					
 					Object stmt = objs[1];
 					
 					if(stmt instanceof StatementNode){
 						StatementNode block = (StatementNode) stmt;
 						//TODO: whileImpl.setLoopBody(block);
 						whileImpl.setCoverage(block.coverage());
 						block.setParentNode(whileImpl);
 					}else{
 						if(stmt instanceof BlockNode){
 							BlockNode block = (BlockNode) stmt;
 							whileImpl.setLoopBody(block);
 							whileImpl.setCoverage(block.coverage());
 							block.setParentNode(whileImpl);
 						}
 						writeReportError(reportLog, stmt, "Statement");
 					}
 					
 					Token whileToken = (Token)objs[3];
 					Token paraLeft = (Token)objs[4];
 					
 					whileImpl.setCoverage(whileToken,paraLeft);
 
 					Object assign = objs[2];
 					
 					if(assign instanceof ExpressionNode){
 						ExpressionNode expression = (ExpressionNode) assign;
 						whileImpl.setCondition(expression);
 						whileImpl.setCoverage(expression.coverage());
 						expression.setParentNode(whileImpl);
 					}else{
 						writeReportError(reportLog, assign, "Expression");
 					}
 					
 					Token paraRight = (Token)objs[3];
 					
 					whileImpl.setCoverage(paraRight);
 					
 					return whileImpl;
 				}
 
 			};
 
 		case "stmt -> break ;":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					BreakNodeImpl breakImpl = new BreakNodeImpl();
 					breakImpl.setCoverage((Token)objs[0],(Token)objs[1]);
 					return new BreakNodeImpl();
 				}
 
 			};
 		case "stmt -> return ;":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					ReturnNodeImpl returnImpl = new ReturnNodeImpl();
 					returnImpl.setCoverage((Token)objs[0],(Token)objs[1]);
 					return returnImpl;
 				}
 			};
 		case "stmt -> return loc ;":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					ReturnNodeImpl returnNode = new ReturnNodeImpl();
 					IdentifierNode identifier = (IdentifierNode) objs[1];
 					returnNode.setRightValue(identifier);
 					identifier.setParentNode(returnNode);
 					returnNode.setCoverage((Token)objs[0]);
 					returnNode.setCoverage(identifier.coverage());
 					returnNode.setCoverage((Token)objs[2]);
 					return returnNode;
 				}
 			};
 			
 		case "stmt -> print loc ;":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					Object id = objs[1]; // IdentifierNode expected
 					// semicolon gets dropped
 					
 					PrintNodeImpl printNode = new PrintNodeImpl();
 					
 					printNode.setCoverage((Token)objs[0]);
 					
 					if(id instanceof IdentifierNode){
 						IdentifierNode idNode = (IdentifierNode) id;
 						printNode.setRightValue(idNode);
 						printNode.setCoverage(idNode.coverage());
 					}else{
 						writeReportError(reportLog, ((ASTNode)id).coverage(), "Identifier");
 					}
 					
 					printNode.setCoverage((Token)objs[2]);
 					
 					return printNode;
 				}
 			};
 			
 		case "loc -> loc [ assign ]":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					ArrayIdentifierNodeImpl arrayIdentifier= new ArrayIdentifierNodeImpl();
 					
 					if(!(objs[0] instanceof IdentifierNode)){
 						writeReportError(reportLog, objs[0], "Identifier");
 					}
 					
 					IdentifierNode node = (IdentifierNode)objs[0];
 					arrayIdentifier.setIdentifierNode(node);
 					arrayIdentifier.setCoverage(node.coverage());
 					node.setParentNode(arrayIdentifier);
 					
 					if(!(objs[1] instanceof Token)){
 						writeReportError(reportLog, objs[1], "Token [");
 					}
 					
 					Token leftSquareBracket = (Token) objs[1];
 					arrayIdentifier.setCoverage(leftSquareBracket);
 
 					if(!(objs[2] instanceof LiteralNode)){
 						writeReportError(reportLog, objs[2], "Literal");
 					}					
 					
 					LiteralNode literal = (LiteralNode)objs[2];
 					if(!(literal.getLiteralType() instanceof LongType)){
 						writeReportError(reportLog, objs[2], "Number");
 					}
 					
 					int index = Integer.parseInt(literal.getLiteral());
 					arrayIdentifier.setIndex(index);
 					arrayIdentifier.setCoverage(literal.coverage());
 					
 					if(!(objs[3] instanceof Token)){
 						writeReportError(reportLog, objs[1], "Token [");
 					}
 					
 					Token rightSquareBracket = (Token) objs[3];
 					arrayIdentifier.setCoverage(rightSquareBracket);
 					return arrayIdentifier;
 				}
 			};
 
 		case "loc -> id":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					BasicIdentifierNodeImpl identifierNode = new BasicIdentifierNodeImpl();
 					
 					if(!(objs[0] instanceof Token)){
 						writeReportError(reportLog, objs[0], "Token id");
 					}
 					
 					Token token = (Token) objs[0];
 					identifierNode.setIdentifier(token.getValue());
 					identifierNode.setCoverage(token);
 					return identifierNode;
 				}
 			};
 		case "loc -> loc.id":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 
 					StructIdentifierNodeImpl identifierNode = new StructIdentifierNodeImpl();
 					
 					if(!(objs[0] instanceof IdentifierNode)){
 						writeReportError(reportLog, objs[0], "Identifier");
 					}
 					
 					IdentifierNode node = (IdentifierNode)objs[0];
 					identifierNode.setIdentifierNode(node);
 					identifierNode.setCoverage(node.coverage());
 					node.setParentNode(identifierNode);
 					
 					if(!(objs[1] instanceof Token)){
 						writeReportError(reportLog, objs[1], "Token .");
 					}
 					
 					Token dot = (Token) objs[1];
 					identifierNode.setCoverage(dot);
 
 					if(!(objs[2] instanceof Token)){
 						writeReportError(reportLog, objs[2], "Token id");
 					}
 					
 					Token token = (Token) objs[2];
 					identifierNode.setFieldName(token.getValue());
 					identifierNode.setCoverage(token);
 					
 					return identifierNode;
 				}
 			};
 		case "assign -> loc = assign":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					AssignmentNodeImpl assignNode = new AssignmentNodeImpl();
 					IdentifierNode identifier = (IdentifierNode) objs[0];
 					Token equalSign = (Token) objs[1];
 					ExpressionNode node = (ExpressionNode) objs[2];
 					assignNode.setLeftValue(identifier);
 					assignNode.getLeftValue().setParentNode(assignNode);
 					assignNode.setRightValue(node); 
 					assignNode.getRightValue().setParentNode(assignNode);
 					
 					AssignmentNodeImpl assignImpl = ((AssignmentNodeImpl)assignNode);
 					
 					assignImpl.setCoverage(identifier.coverage());
 					assignImpl.setCoverage(equalSign);
 					assignImpl.setCoverage(node.coverage());
 					return assignNode;
 				}
 			};
 		case "assign -> bool":
 			break;
 		case "bool -> bool || join":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					return createBinaryExpr(reportLog, BinaryOperator.LOGICAL_OR, "||", objs);
 				}
 			};
 		case "bool -> join":
 			break;	// Nothing to do here
 		case "join -> join && equality":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					return createBinaryExpr(reportLog, BinaryOperator.LOGICAL_AND, "&&", objs);
 				}
 			};
 		case "join -> equality":
 			break; // Nothing to do here
 
 		case "equality -> equality == rel":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					return createBinaryExpr(reportLog, BinaryOperator.EQUAL, "==", objs);
 				}
 			};
 		case "equality -> equality != rel":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					return createBinaryExpr(reportLog, BinaryOperator.INEQUAL, "!=", objs);
 				}
 			};
 		case "equality -> rel":
 			break; // Nothing to do here
 		case "rel -> expr < expr":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					return createBinaryExpr(reportLog, BinaryOperator.LESSTHAN, "<", objs);
 				}
 			};
 		case "rel -> expr > expr":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					return createBinaryExpr(reportLog, BinaryOperator.GREATERTHAN, "<", objs);
 				}
 			};
 		case "rel -> expr >= expr":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					return createBinaryExpr(reportLog, BinaryOperator.GREATERTHANEQUAL, ">=", objs);
 				}
 			};
 		case "rel -> expr <= expr":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					return createBinaryExpr(reportLog, BinaryOperator.LESSTHANEQUAL, "<=", objs);
 				}
 			};
 		case "rel -> expr":
 			break; // Nothing to do here
 		case "expr -> expr + term":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					return createBinaryExpr(reportLog, BinaryOperator.ADDITION, "+", objs);
 				}
 			};
 		case "expr -> expr - term":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					return createBinaryExpr(reportLog, BinaryOperator.SUBSTRACTION, "-", objs);
 				}
 			};
 		case "expr -> term":
 			break; // Nothing to do here
 		case "term -> term * unary":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					return createBinaryExpr(reportLog, BinaryOperator.MULTIPLICATION, "*", objs);
 				}
 			};
 		case "term -> term / unary":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					return createBinaryExpr(reportLog, BinaryOperator.DIVISION, "/", objs);
 				}
 			};
 		case "term -> unary":
 			break; // Nothing to do here
 		case "unary -> ! unary":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					
 					LogicUnaryExpressionNodeImpl unary = new LogicUnaryExpressionNodeImpl();
 					
 					if(!(objs[0] instanceof Token)){
 						writeReportError(reportLog, objs[0], "Token !");
 					}
 					
 					Token token = (Token) objs[0];
 					
 					if(!(objs[1] instanceof ExpressionNode)){
 						writeReportError(reportLog, objs[1], "Expression");
 					}
 					
 					ExpressionNode expr = (ExpressionNode)objs[1];
 
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
 				public Object create(Object... objs) throws ParserException  {
 					
 					if(!(objs[0] instanceof Token)){
 						writeReportError(reportLog, objs[0], "Token -");
 					}
 					
 					Token token = (Token) objs[0];
 					
 					if(!(objs[1] instanceof ExpressionNode)){
 						writeReportError(reportLog, objs[1], "Expression");
 					}
 					
 					ExpressionNode expr = (ExpressionNode)objs[1];
 
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
 				public Object create(Object... objs) throws ParserException  {
 					
 					if(!(objs[0] instanceof Token)){
 						writeReportError(reportLog, objs[0], "Token (");
 					}
 					
 					if(!(objs[1] instanceof ExpressionNode)){
 						writeReportError(reportLog, objs[1], "Expression");
 					}
 					
 					if(!(objs[2] instanceof Token)){
 						writeReportError(reportLog, objs[2], "Token )");
 					}
 
 					ASTNodeImpl astNode = (ASTNodeImpl) objs[1];
 					astNode.setCoverageAtFront((Token)objs[0]);
 					astNode.setCoverage((Token)objs[2]);
 					
 					return  astNode;
 				}
 			};
 		case "unary -> factor":
 		case "factor -> loc":
 			break;	// Nothing to do here
 		case "factor -> num":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					LiteralNodeImpl literal = new LiteralNodeImpl();
 					Token token = (Token) objs[0];
 					literal.setLiteral(token.getValue());
 					literal.setLiteralType(new LongType());
 					literal.setCoverage(token);
 					return literal;
 				}
 			};
 		case "factor -> real":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					LiteralNodeImpl literal = new LiteralNodeImpl();
 					Token token = (Token) objs[0];
 					literal.setLiteral(token.getValue());
 					literal.setLiteralType(new DoubleType());
 					literal.setCoverage(token);
 					return literal;
 				}
 			};
 		case "factor -> true":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					LiteralNodeImpl literal = new LiteralNodeImpl();
 					Token token = (Token) objs[0];
 					literal.setLiteral(token.getValue());
 					literal.setLiteralType(new BooleanType());
 					literal.setCoverage(token);
 					return literal;
 				}
 			};
 		case "factor -> false":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					LiteralNodeImpl literal = new LiteralNodeImpl();
 					Token token = (Token) objs[0];
 					literal.setLiteral(token.getValue());
 					literal.setLiteralType(new BooleanType());
 					literal.setCoverage(token);
 					return literal;
 				}
 			};
 		case "factor -> string":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					LiteralNodeImpl literal = new LiteralNodeImpl();
 					Token token = (Token) objs[0];
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
 				public Object create(Object... objs) throws ParserException  {
 					//typeToken DeclarationNode is wasted after reduce, not needed any more
 					DeclarationNode typeToken = (DeclarationNode) objs[0];
 					Token leftBrace = (Token) objs[1];
 					
 					if(!(objs[2] instanceof NumToken)){
 						writeReportError(reportLog,objs[2],"Number");
 					}
 					NumToken size = (NumToken) objs[2];
 					Token rightBrace = (Token) objs[3];
 					
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
 				public Object create(Object... objs) throws ParserException  {
 					
 					if(!(objs[0] instanceof Token)){
 						writeReportError(reportLog, objs[0], "Type");
 					}
 					
 					Token token = (Token)objs[0];
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
 						writeReportError(reportLog, token, "Basic Type");
 						return null;
 					
 					}
 				}
 			};
 
 		case "type -> record { decls }":
 			return new ReduceAction() {
 				@Override
 				public Object create(Object... objs) throws ParserException  {
 					
 					DeclarationNodeImpl struct = new DeclarationNodeImpl();
 					
 					if(!(objs[0] instanceof Token)){
 						writeReportError(reportLog, objs[0], "Record");
 					}
 					
 					if(!(objs[1] instanceof Token)){
 						writeReportError(reportLog, objs[1], "{");
 					}
 					
 					struct.setCoverage((Token)objs[0], (Token)objs[1]);
 					
 					if(!(objs[2] instanceof BlockNode)){
 						writeReportError(reportLog, objs[2], "Declarations" );
 					}
 					
 					
 					BlockNode blockNode = (BlockNode)objs[2];
 					List<DeclarationNode> decls = blockNode.getDeclarationList();
 					
 					int size = decls.size();
 					Member[] members = new Member[size];
 					
 					for(int i = 0; i<size; i++){
 						DeclarationNode declarationNode = decls.get(i);
 						members[i] = new Member(declarationNode.getIdentifier(), declarationNode.getType());
						struct.setCoverage(declarationNode.coverage());
 					}
 					
 					StructType type = new StructType("record", members);
 					struct.setType(type);
 					
 					struct.setCoverage(blockNode.coverage());
 					
 					if(!(objs[3] instanceof Token)){
 						writeReportError(reportLog, objs[1], "}");
 					}
 					
 					struct.setCoverage((Token)objs[3]);
 					
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
 	private static void insertDecl(BlockNode block, DeclarationNode decl, final ReportLog reportLog) throws ParserException {
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
 
 	private static BlockNodeImpl joinBlocks(Object left, Object right, ReportLog reportLog) throws ParserException{
 		BlockNodeImpl newBlock = new BlockNodeImpl();
 		newBlock.setSymbolTable(new SymbolTableImpl());
 		
 		// Handle left
 		if (!left.equals(NO_VALUE)) {
 			BlockNode declsBlock = (BlockNode) left;
 			for (DeclarationNode decl : declsBlock.getDeclarationList()) {
 				insertDecl(newBlock, decl, reportLog);
 				decl.setParentNode(newBlock);
 				newBlock.setCoverage(decl.coverage());
 			}
 		}
 
 		// Handle right
 		if (!right.equals(NO_VALUE)) {
 			BlockNode stmtsBlock = (BlockNode) right;
 			for (StatementNode stmt : stmtsBlock.getStatementList()) {
 				
 				//look for Blocknodes, Loopnodes and Branchnodes and concat Symboltables
 				if(stmt instanceof BranchNode){
 					BranchNode branch = (BranchNode)stmt;
 					if(branch.getStatementNodeOnTrue() instanceof BlockNode){
 						BlockNode node = (BlockNode) branch.getStatementNodeOnTrue();
 						((SymbolTableImpl)node.getSymbolTable()).setParent(newBlock.getSymbolTable());
 					}
 					if(branch.getStatementNodeOnFalse() instanceof BlockNode){
 						BlockNode node = (BlockNode) branch.getStatementNodeOnFalse();
 						((SymbolTableImpl)node.getSymbolTable()).setParent(newBlock.getSymbolTable());
 					}					
 				}else{
 					if(stmt instanceof BlockNode){
 						((SymbolTableImpl)((BlockNode)stmt).getSymbolTable()).setParent(newBlock.getSymbolTable());
 					}else{
 						if(stmt instanceof DoWhileNode){
 							BlockNode block = ((DoWhileNode)stmt).getLoopBody();
 							((SymbolTableImpl)block.getSymbolTable()).setParent(newBlock.getSymbolTable());
 						}else{
 							if(stmt instanceof WhileNode){
 								BlockNode block = ((WhileNode)stmt).getLoopBody();
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
 	 * Gets ReportLog, the Object, thats made some trouble and the message whats expected instead.
 	 * Throws in all cases a PaserException.
 	 * 
 	 * @param reportLog
 	 * @param obj
 	 * @param msg
 	 * @throws ParserException
 	 */
 	private static void writeReportError(final ReportLog reportLog,
 			Object obj,String msg) throws ParserException{
 		if(obj instanceof ASTNode){
 			reportLog.reportError(ReportType.UNDEFINED, ((ASTNode)obj).coverage(), "There is no " + msg + " found!");
 			throw new ParserException(msg +" expected");
 		}
 		if(obj instanceof Token){
 			List<Token> list = new ArrayList<Token>();
 			list.add((Token)obj);
 			reportLog.reportError(ReportType.UNDEFINED, list, "There is no " + msg + " found!");
 			throw new ParserException(msg +" expected");
 		}
 		reportLog.reportError(ReportType.UNDEFINED, null, "Object is not defined in AST");
 		throw new ParserException("Object not defined in AST");
 	}
 	
 	/**
 	 * @param reportLog
 	 * @param objs
 	 * @return
 	 */
 	private static Object createBinaryExpr(final ReportLog reportLog,  
 			final BinaryOperator op, String opStr,
 			Object... objs) {
 		if(!(objs[0] instanceof ExpressionNode)){
 			writeReportError(reportLog, objs[0], "Expression");
 		}
 		
 		if(!(objs[1] instanceof Token)){
 			writeReportError(reportLog, objs[1], "Token " + opStr);
 		}
 		if(!(objs[2] instanceof ExpressionNode)){
 			writeReportError(reportLog, objs[2], "Expression");
 		}
 		
 		ExpressionNode leftExpr = (ExpressionNode) objs[0];
 		Token opToken = (Token) objs[1];
 		ExpressionNode rightExpr = (ExpressionNode) objs[2];
 		
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
 
 	private static class ReduceStringType extends Type{
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
 }
