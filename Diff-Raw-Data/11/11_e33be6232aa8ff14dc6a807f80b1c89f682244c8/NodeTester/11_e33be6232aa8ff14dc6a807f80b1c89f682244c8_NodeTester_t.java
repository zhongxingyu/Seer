 package test.ast.node;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import util.ast.node.ExpressionNode;
 import util.ast.node.IdNode;
 import util.ast.node.MockExpressionNode;
 import util.ast.node.BiOpNode;
 import util.ast.node.MockNode;
 import util.ast.node.UnOpNode;
 import util.type.Types;
 
 /**
  * 
  * Tests for the functionality provided by the AbstractSyntaxTree class.
  * 
  * @author sam
  * 
  */
 public class NodeTester {
 
 	private ExpressionNode A;
 	private ExpressionNode B;
 	private ExpressionNode C;
 	private ExpressionNode D;
 	private ExpressionNode E;
 	private ExpressionNode F;
 	private ExpressionNode G;
 
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
 		D = new UnOpNode(UnOpNode.OpType.CAST, new MockExpressionNode());
 		E = new UnOpNode(UnOpNode.OpType.CAST, new MockExpressionNode());
 		B = new BiOpNode(BiOpNode.OpType.TIMES, D, E);
 		A = new BiOpNode(BiOpNode.OpType.TIMES, B, C);
 	}
 
 	@After
 	public void tearDown() {
 	}
 
 	@Test
 	public void toStringTest1() {
 
 		String properName = "BiOpNode<TIMES>";
 
 		assertEquals(
 				"Nodes should return the proper name when toString() is called.",
 				properName, A.toString());
 
 	}
 
 	@Test
 	public void toStringTest2() {
 
 		String properName = "BiOpNode<TIMES>";
 
 		assertEquals(
 				"Nodes should return the proper name when toString() is called.",
 				properName, A.toString());
 
 	}
 
 	@Test(expected = UnsupportedOperationException.class)
 	public void doubleAddParentTest() {
 		// note that this.addChild(that) implicitly sets that's parent to be
 		// this. In this case, D already has it's parent set to B.
 		B.addChild(D);
 	}
 
 	@Test
 	public void addChildTest() {
 		MockNode parent = new MockNode();
 		MockNode childOne = new MockNode();
 		MockNode childTwo = new MockNode();
 		parent.addChild(childOne);
 		parent.addChild(childTwo);
 		assertEquals("addChild should properly add children", 2, parent
 				.getChildren().size());
 		assertEquals(
 				"addChild should always add a new child as rightmost child",
 				childTwo, parent.getChildren().get(1));
 		assertEquals(
 				"addChild should always add a new child as rightmost child",
 				childOne, parent.getChildren().get(0));
 		parent.addChild(null);
 		assertEquals("adding a null child should do nothing", 2, parent
 				.getChildren().size());
 	}
 
 	@Test
	public void removeChildTest() {
		A.removeChild(C);
		assertEquals("removeChild should properly remove a child", 1, A
				.getChildren().size());
		assertEquals("removeChild should properly remove a child", false, A
				.hasChild(C));
		assertEquals("removeChild should properly unset childs parent", null, C
				.getParent());
	}

	@Test
 	public void setUnsetParentTest() {
 		MockNode parent = new MockNode();
 		MockNode child = new MockNode();
 		assertEquals("Nodes should have null parents by default", null, child
 				.getParent());
 		parent.addChild(child);
 		assertEquals(
 				"Adding a child to a parent should set parent to be child's parent.",
 				parent, child.getParent());
 		child.unsetParent();
 		assertEquals("Unsetting a child's parent should set parent to null",
 				null, child.getParent());
 		assertEquals(
 				"Unsetting a child's parent removes the child from the parent's children list.",
 				false, parent.getChildren().contains(child));
 	}
 }
