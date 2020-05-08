 package test.ast.node;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import util.ast.node.BiOpNode;
 import util.ast.node.CastExpressionNode;
 import util.ast.node.ExpressionNode;
 import util.ast.node.IdNode;
 import util.ast.node.MultiplicativeExpressionNode;
 import util.ast.node.BiOpNode.OpType;
 
 /**
  * 
  * Tests for the functionality provided by the AbstractSyntaxTree class.
  * 
  * @author !TODO!
  * 
  */
 public class ExpressionNodeTester {
 
 	private ExpressionNode A;
 	private ExpressionNode B;
 	private ExpressionNode C;
 	private ExpressionNode D;
 	private ExpressionNode E;
 
 	@BeforeClass
 	public static void setUpClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() {
 		// A (MultExprNode) -> B (MultExprNode) * C (idNode)
 		// B -> D * E
 		C = new IdNode("C");
 		D = new CastExpressionNode();
 		E = new CastExpressionNode();
 		B = new MultiplicativeExpressionNode(OpType.TIMES, D, E);
 		A = new MultiplicativeExpressionNode(OpType.TIMES, B, C);
 	}
 
 	@After
 	public void tearDown() {
 	}
 
 	@Test
 	public void toStringTest1() {
 
 		String properName = "MultiplicativeExpressionNode<unknown,TIMES> Children: "
 				+ "[MultiplicativeExpressionNode<unknown,TIMES>; IdNode<unknown,C>]";
 
 		assertEquals("Nodes should return the proper name when toString() is called.",
 				properName, A.toString());
		
 	}
 	
 	@Test
 	public void toStringTest2() {
 
 		A.setType(ExpressionNode.Type.REAL);
 		B.setType(ExpressionNode.Type.REAL);
 		C.setType(ExpressionNode.Type.INT);
 		D.setType(ExpressionNode.Type.INT);
 		
 		String properName = "MultiplicativeExpressionNode<REAL,TIMES> Children: "
 			+ "[MultiplicativeExpressionNode<REAL,TIMES>; IdNode<INT,C>]";
 
 		assertEquals("Nodes should return the proper name when toString() is called.",
 				properName, A.toString());
 
 	}
 }
