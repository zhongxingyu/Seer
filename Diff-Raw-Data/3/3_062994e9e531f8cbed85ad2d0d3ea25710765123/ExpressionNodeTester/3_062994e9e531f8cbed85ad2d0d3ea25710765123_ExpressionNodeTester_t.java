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
		// A -> B * C
 		B = new MultiplicativeExpressionNode();
 		C = new BiOpNode(BiOpNode.OpType.TIMES);
 		D = new CastExpressionNode();
 		E = new CastExpressionNode();
 		A.addChild(B);
 		A.addChild(C);
 		A = new MultiplicativeExpressionNode(OpType.TIMES, B, C);
 		A.addChild(D);
 		B.addChild(E);
 	}
 
 	@After
 	public void tearDown() {
 	}
 
 	@Test
 	public void toStringTest1() {
 
 		String properName = "MultiplicativeExpressionNode<unknown>, Children: "
 				+ "[MultiplicativeExpressionNode<unknown>, TIMES<unknown>, "
 				+ "CastExpressionNode<unknown>]";
 
 		assertEquals("Nodes should return the proper name when toString() is called.",
 				properName, A.toString());
 
 	}
 	
 	@Test
 	public void toStringTest2() {
 
 		A.setType(ExpressionNode.Type.REAL);
 		B.setType(ExpressionNode.Type.REAL);
 		C.setType(ExpressionNode.Type.REAL);
 		D.setType(ExpressionNode.Type.INT);
 		
 		String properName = "MultiplicativeExpressionNode<REAL>, Children: "
 				+ "[MultiplicativeExpressionNode<REAL>, TIMES<REAL>, "
 				+ "CastExpressionNode<INT>]";
 
 		assertEquals("Nodes should return the proper name when toString() is called.",
 				properName, A.toString());
 
 	}
 }
