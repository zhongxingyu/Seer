 package swp_compiler_ss13.fuc.semantic_analyser;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import swp_compiler_ss13.common.ast.AST;
 import swp_compiler_ss13.common.ast.nodes.binary.AssignmentNode;
 import swp_compiler_ss13.common.ast.nodes.binary.DoWhileNode;
 import swp_compiler_ss13.common.ast.nodes.binary.WhileNode;
 import swp_compiler_ss13.common.ast.nodes.leaf.BasicIdentifierNode;
 import swp_compiler_ss13.common.ast.nodes.leaf.BreakNode;
 import swp_compiler_ss13.common.ast.nodes.leaf.LiteralNode;
 import swp_compiler_ss13.common.ast.nodes.marynary.BlockNode;
 import swp_compiler_ss13.common.ast.nodes.unary.DeclarationNode;
 import swp_compiler_ss13.common.parser.SymbolTable;
 import swp_compiler_ss13.common.types.primitive.BooleanType;
 import swp_compiler_ss13.common.types.primitive.LongType;
 import swp_compiler_ss13.fuc.ast.ASTImpl;
 import swp_compiler_ss13.fuc.ast.AssignmentNodeImpl;
 import swp_compiler_ss13.fuc.ast.BasicIdentifierNodeImpl;
 import swp_compiler_ss13.fuc.ast.BlockNodeImpl;
 import swp_compiler_ss13.fuc.ast.BreakNodeImpl;
 import swp_compiler_ss13.fuc.ast.DeclarationNodeImpl;
 import swp_compiler_ss13.fuc.ast.DoWhileNodeImpl;
 import swp_compiler_ss13.fuc.ast.LiteralNodeImpl;
 import swp_compiler_ss13.fuc.ast.WhileNodeImpl;
 import swp_compiler_ss13.fuc.parser.errorHandling.ReportLogImpl;
 import swp_compiler_ss13.fuc.symbolTable.SymbolTableImpl;
 
 public class LoopTests {
 
 	private SemanticAnalyser analyser;
 	private ReportLogImpl log;
 
 	public LoopTests() {
 	}
 
 	@Before
 	public void setUp() {
 		log = new ReportLogImpl();
 		analyser = new SemanticAnalyser(this.log);
 	}
 
 	@After
 	public void tearDown() {
 		analyser = null;
 		log = null;
 	}
 
 	/**
 	 * # error: usage of long as condition<br/>
 	 * long l;<br/>
 	 * l = 1;<br/>
 	 * <br/>
 	 * while (l){<br/>
 	 * l = 0;<br/>
 	 * }
 	 */
 	@Test
 	public void testWhileConditionTypeError() {
		SymbolTable symbolTable = new SymbolTableImpl();
		
 		// long l;
 		DeclarationNode declaration_l = new DeclarationNodeImpl();
 		declaration_l.setIdentifier("l");
 		declaration_l.setType(new LongType());
 
 		// l = 1;
 		BasicIdentifierNode identifier_l1 = new BasicIdentifierNodeImpl();
 		identifier_l1.setIdentifier("l");
 		LiteralNode literal_1 = new LiteralNodeImpl();
 		literal_1.setLiteral("1");
 		literal_1.setLiteralType(new LongType());
 
 		AssignmentNode assignment_l1 = new AssignmentNodeImpl();
 		assignment_l1.setLeftValue(identifier_l1);
 		assignment_l1.setRightValue(literal_1);
 		identifier_l1.setParentNode(assignment_l1);
 		literal_1.setParentNode(assignment_l1);
 
 		// l = 0;
 		BasicIdentifierNode identifier_l2 = new BasicIdentifierNodeImpl();
 		identifier_l2.setIdentifier("l");
 		LiteralNode literal_0 = new LiteralNodeImpl();
 		literal_0.setLiteral("0");
 		literal_0.setLiteralType(new LongType());
 
 		AssignmentNode assignment_l2 = new AssignmentNodeImpl();
 		assignment_l2.setLeftValue(identifier_l2);
 		assignment_l2.setRightValue(literal_0);
 		identifier_l2.setParentNode(assignment_l2);
 		literal_0.setParentNode(assignment_l2);
 
 		// while (l){ ...};
 		BasicIdentifierNode identifier_l3 = new BasicIdentifierNodeImpl();
 		identifier_l3.setIdentifier("l");
 
		SymbolTable whileBlockTable = new SymbolTableImpl(symbolTable);
 		BlockNode whileBlock = new BlockNodeImpl();
 		whileBlock.addStatement(assignment_l2);
 		whileBlock.setSymbolTable(whileBlockTable);
 		assignment_l2.setParentNode(whileBlock);
 
 		WhileNode whileNode = new WhileNodeImpl();
 		whileNode.setCondition(identifier_l3);
 		whileNode.setLoopBody(whileBlock);
 		identifier_l3.setParentNode(whileNode);
 		whileBlock.setParentNode(whileNode);
 
 		// main block
 		symbolTable.insert("l", new LongType());
 
 		BlockNode blockNode = new BlockNodeImpl();
 		blockNode.addDeclaration(declaration_l);
 		blockNode.addStatement(whileNode);
 		blockNode.setSymbolTable(symbolTable);
 		declaration_l.setParentNode(blockNode);
 		whileNode.setParentNode(blockNode);
 
 		// analyse AST
 		AST ast = new ASTImpl();
 		ast.setRootNode(blockNode);
 
 		analyser.analyse(ast);
 
 		// TODO better error-check
 		assertEquals(log.getErrors().size(), 1);
 	}
 
 	/**
 	 * # error: usage of long as condition<br/>
 	 * long l;<br/>
 	 * l = 1;<br/>
 	 * <br/>
 	 * do{<br/>
 	 * l = 0;<br/>
 	 * }while(l);
 	 */
 	@Test
 	public void testDoWhileConditionTypeError() {
		SymbolTable symbolTable = new SymbolTableImpl();
		
 		// long l;
 		DeclarationNode declaration_l = new DeclarationNodeImpl();
 		declaration_l.setIdentifier("l");
 		declaration_l.setType(new LongType());
 
 		// l = 1;
 		BasicIdentifierNode identifier_l1 = new BasicIdentifierNodeImpl();
 		identifier_l1.setIdentifier("l");
 		LiteralNode literal_1 = new LiteralNodeImpl();
 		literal_1.setLiteral("1");
 		literal_1.setLiteralType(new LongType());
 
 		AssignmentNode assignment_l1 = new AssignmentNodeImpl();
 		assignment_l1.setLeftValue(identifier_l1);
 		assignment_l1.setRightValue(literal_1);
 		identifier_l1.setParentNode(assignment_l1);
 		literal_1.setParentNode(assignment_l1);
 
 		// l = 0;
 		BasicIdentifierNode identifier_l2 = new BasicIdentifierNodeImpl();
 		identifier_l2.setIdentifier("l");
 		LiteralNode literal_0 = new LiteralNodeImpl();
 		literal_0.setLiteral("0");
 		literal_0.setLiteralType(new LongType());
 
 		AssignmentNode assignment_l2 = new AssignmentNodeImpl();
 		assignment_l2.setLeftValue(identifier_l2);
 		assignment_l2.setRightValue(literal_0);
 		identifier_l2.setParentNode(assignment_l2);
 		literal_0.setParentNode(assignment_l2);
 
 		// do {...} while(l);
 		BasicIdentifierNode identifier_l3 = new BasicIdentifierNodeImpl();
 		identifier_l3.setIdentifier("l");
 
		SymbolTable whileBlockTable = new SymbolTableImpl(symbolTable);
 		BlockNode doWhileBlock = new BlockNodeImpl();
 		doWhileBlock.addStatement(assignment_l2);
 		doWhileBlock.setSymbolTable(whileBlockTable);
 		assignment_l2.setParentNode(doWhileBlock);
 
 		DoWhileNode doWhileNode = new DoWhileNodeImpl();
 		doWhileNode.setCondition(identifier_l3);
 		doWhileNode.setLoopBody(doWhileBlock);
 		identifier_l3.setParentNode(doWhileNode);
 		doWhileBlock.setParentNode(doWhileNode);
 
 		// main block
 		symbolTable.insert("l", new LongType());
 
 		BlockNode blockNode = new BlockNodeImpl();
 		blockNode.addDeclaration(declaration_l);
 		blockNode.addStatement(doWhileNode);
 		blockNode.setSymbolTable(symbolTable);
 		declaration_l.setParentNode(blockNode);
 		doWhileNode.setParentNode(blockNode);
 
 		// analyse AST
 		AST ast = new ASTImpl();
 		ast.setRootNode(blockNode);
 
 		analyser.analyse(ast);
 
 		// TODO better error-check
 		assertEquals(log.getErrors().size(), 1);
 	}
 
 	/**
 	 * # correct usage of break<br/>
 	 * while (true){<br/>
 	 *     break;<br/>
 	 * }
 	 */
 	@Test
 	public void testInnerBreak() {
		SymbolTable symbolTable = new SymbolTableImpl();
		
 		// while (true){ break;}
 		LiteralNode literal_true = new LiteralNodeImpl();
 		literal_true.setLiteral("true");
 		literal_true.setLiteralType(new BooleanType());
 		
 		BreakNode breakNode = new BreakNodeImpl();
 		
		SymbolTable whileBlockTable = new SymbolTableImpl(symbolTable);
 		BlockNode whileBlock = new BlockNodeImpl();
 		whileBlock.addStatement(breakNode);
 		whileBlock.setSymbolTable(whileBlockTable);
 		breakNode.setParentNode(whileBlock);
 
 		WhileNode whileNode = new WhileNodeImpl();
 		whileNode.setCondition(literal_true);
 		whileNode.setLoopBody(whileBlock);
 		literal_true.setParentNode(whileNode);
 		whileBlock.setParentNode(whileNode);
 
 		// main block
 		BlockNode blockNode = new BlockNodeImpl();
 		blockNode.addStatement(whileNode);
 		blockNode.setSymbolTable(symbolTable);
 		whileNode.setParentNode(blockNode);
 
 		// analyse AST
 		AST ast = new ASTImpl();
 		ast.setRootNode(blockNode);
 
 		analyser.analyse(ast);
 
 		assertFalse(log.hasErrors());
 	}
 
 	/**
 	 * # error: break outside of a loop<br/>
 	 * break;
 	 */
 	@Test
 	public void testBreakOtusideLoopError() {
 		// break;
 		BreakNode breakNode = new BreakNodeImpl();
 
 		// main block
 		SymbolTable symbolTable = new SymbolTableImpl();
 
 		BlockNode blockNode = new BlockNodeImpl();
 		blockNode.addStatement(breakNode);
 		blockNode.setSymbolTable(symbolTable);
 		breakNode.setParentNode(blockNode);
 
 		// analyse AST
 		AST ast = new ASTImpl();
 		ast.setRootNode(blockNode);
 
 		analyser.analyse(ast);
 
 		// TODO better error-check
 		assertEquals(log.getErrors().size(), 1);
 	}
 }
