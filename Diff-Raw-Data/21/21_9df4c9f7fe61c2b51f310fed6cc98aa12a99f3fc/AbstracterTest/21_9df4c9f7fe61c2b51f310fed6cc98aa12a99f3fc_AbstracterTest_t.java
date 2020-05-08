 package compiler.abstracter;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.junit.Test;
 
 import compiler.CompilerException;
 import compiler.graph.BadParenthesisException;
 import compiler.graph.CombinatorNotFoundException;
 import compiler.graph.EmptyStackException;
 import compiler.graph.GraphFactory;
 import compiler.graph.NodeFieldFactory;
 import compiler.graph.GraphSerializer;
 import compiler.graph.Node;
 import compiler.graph.NodeNodeField;
 import compiler.combinators.B;
 import compiler.combinators.Combinator;
 import compiler.combinators.CombinatorManager;
 import compiler.combinators.DummyCombinator;
 import compiler.combinators.Lambda;
 import compiler.combinators.Var;
 import compiler.config.ConfigManager;
 
 public class AbstracterTest {
 	
 	@Test
 	public void firstAbstractionTest(){
 		
 		
 		Var var = new Var("$x");
 		Combinator S = new DummyCombinator("S"); 
 		Combinator K = new DummyCombinator("K");
 		
 		// lambda+ x . S x K
 		Node root = new Node(NodeFieldFactory.create(S),NodeFieldFactory.create(var));
 		Node second = new Node(NodeFieldFactory.create(root),NodeFieldFactory.create(K));
 		root.setNextNode(second);
 		
 		Abstracter ab = new Abstracter(root);
 		Node result = ab.abstraction(root,1,var);
 		String ret = GraphSerializer.serialize(result);
 		
 		assertEquals(ret,"S ( S ( K S ) I ) ( K K )");
 	}
 	
 	@Test
 	public void firstAbstractionParenthesisTest(){
 		
 		
 		Var var = new Var("$x");
 		Combinator S = new DummyCombinator("S"); 
 		Combinator K = new DummyCombinator("K");
 		
 		// lambda+ x . S (x K)
 		Node child = new Node(NodeFieldFactory.create(var),NodeFieldFactory.create(K));
 		Node root = new Node(NodeFieldFactory.create(S),NodeFieldFactory.create(child));
 		
 		Abstracter ab = new Abstracter(root);
 		Node result = ab.abstraction(root,1,var);
 		String ret = GraphSerializer.serialize(result);
 		
 		assertEquals(ret,"S ( K S ) ( S I ( K K ) )");
 		
 		// lambda+ x . x et lambda+ x . K
 		root = new Node(new NodeNodeField(null), NodeFieldFactory.create(K));
 		result = ab.abstraction(root,1,var);
 		ret = GraphSerializer.serialize(result);
 		assertEquals(ret,"K K");
 		
 		root = new Node(new NodeNodeField(null), NodeFieldFactory.create(var));
 		result = ab.abstraction(root,1,var);
 		ret = GraphSerializer.serialize(result);
 		assertEquals(ret,"I I");
 	}
 	
 	@Test
 	public void firstAbstractionParenthesisTest2(){
 		
 		
 		Var var = new Var("$x");
 		Combinator S = new DummyCombinator("S"); 
 		Combinator K = new DummyCombinator("K");
 		
 		// lambda+ x . S (S x K)
 		Node childRoot = new Node(NodeFieldFactory.create(S),NodeFieldFactory.create(var));
 		Node child = new Node(NodeFieldFactory.create(childRoot),NodeFieldFactory.create(K));
 		childRoot.setNextNode(child);
 		Node root = new Node(NodeFieldFactory.create(S),NodeFieldFactory.create(child));
 		
 		Abstracter ab = new Abstracter(root);
 		Node result = ab.abstraction(root,1,var);
 		String ret = GraphSerializer.serialize(result);
 		
 		assertEquals(ret,"S ( K S ) ( S ( S ( K S ) I ) ( K K ) )");
 	}
 	
 	@Test
 	public void findAbstracterTest(){
 		
 		Lambda lambdaPlus = new Lambda(1);
 		Var var = new Var("$x");
 		Combinator S = new DummyCombinator("S"); 
 		Combinator K = new DummyCombinator("K");
 		
 		// lambda+ x . S (x K)
 		Node root = new Node(NodeFieldFactory.create(lambdaPlus), NodeFieldFactory.create(var));
 		Node second = new Node(NodeFieldFactory.create(root), NodeFieldFactory.create(S));
 		root.setNextNode(second);
 		Node child = new Node(NodeFieldFactory.create(var),NodeFieldFactory.create(K));
 		Node third = new Node(NodeFieldFactory.create(second), NodeFieldFactory.create(child));
 		second.setNextNode(third);
 		
 		Abstracter ab = new Abstracter(root);
 		Node result = ab.findAbstracter(root);
 		String ret = GraphSerializer.serialize(result);
 		
 		assertEquals(ret,"S ( K S ) ( S I ( K K ) )");
 		
 	}
 
 	@Test
 	public void twoAbstractersTest(){
 		Lambda lambdaPlus = new Lambda(1);
 		Var var = new Var("$x");
 		Var var2 = new Var("$y");
 		
 		Node root = new Node(NodeFieldFactory.create(lambdaPlus), NodeFieldFactory.create(var));
 		Node second = new Node(NodeFieldFactory.create(root), NodeFieldFactory.create(lambdaPlus));
 		root.setNextNode(second);
 		Node third = new Node(NodeFieldFactory.create(second), NodeFieldFactory.create(var2));
 		second.setNextNode(third);
 		Node fourth = new Node(NodeFieldFactory.create(third), NodeFieldFactory.create(var));
 		third.setNextNode(fourth);
 		
 		Abstracter ab = new Abstracter(root);
 		Node result = ab.findAbstracter(root);
 		String ret = GraphSerializer.serialize(result);
 		
 		assertEquals(ret,"S ( K K ) I");
 	}
 	
 	@Test
 	public void searchVariableTest() throws EmptyStackException, BadParenthesisException, CombinatorNotFoundException{
 		
 		Combinator K = new DummyCombinator("K");
 		Var var = new Var("$x");
 		
 		Node root = new Node(NodeFieldFactory.create(K), NodeFieldFactory.create(K));
 		Node second = new Node(NodeFieldFactory.create(root), NodeFieldFactory.create(var));
 		Node third = new Node(NodeFieldFactory.create(second), NodeFieldFactory.create(K));
 		new Node(NodeFieldFactory.create(third), NodeFieldFactory.create(var));
 		
 		Abstracter ab = new Abstracter(root);
 		Node result = ab.searchVariable(third,var);
 		assertEquals(result,second);
 		
 		Node childRoot = new Node(new NodeNodeField(null), NodeFieldFactory.create(var));
 		Node child = new Node(NodeFieldFactory.create(childRoot), NodeFieldFactory.create(K));
 		second.setArgument(NodeFieldFactory.create(child));
 		result = ab.searchVariable(third,var);
 		assertEquals(result,second);
 		
 		String[] example = {"S","(","S","(","S","K",")","K",")"};
 		Node graph = GraphFactory.create(new ArrayList<String>(Arrays.asList(example)));
 		result = ab.searchVariable(graph,var);
 		assertEquals(result,null);
 		
 	}
 	
 	@Test
 	public void lambdaPlusPlusTest(){
 		
 		// lambda++ x . S K x
 		Lambda lambda = new Lambda(2);
 		Var var = new Var("$x");
 		Combinator S = new DummyCombinator("S"); 
 		Combinator K = new DummyCombinator("K");
 		
 		Node root = new Node(NodeFieldFactory.create(lambda), NodeFieldFactory.create(var));
 		Node second = new Node(NodeFieldFactory.create(root), NodeFieldFactory.create(S));
 		Node third = new Node(NodeFieldFactory.create(second), NodeFieldFactory.create(K));
 		Node fourth = new Node(NodeFieldFactory.create(third), NodeFieldFactory.create(var));
 		
 		Abstracter ab = new Abstracter(root);
 		Node result = ab.findAbstracter(fourth);
 		String ret = GraphSerializer.serialize(result);
 		assertEquals(ret,"S K");
 		
 		// lambda++ x . S x
 		root = new Node(NodeFieldFactory.create(lambda), NodeFieldFactory.create(var));
 		second = new Node(NodeFieldFactory.create(root), NodeFieldFactory.create(S));
 		third = new Node(NodeFieldFactory.create(second), NodeFieldFactory.create(var));
 		third.setNextNode(null);
 
 		result = ab.findAbstracter(third);
 		ret = GraphSerializer.serialize(result);
 		assertEquals(ret,"I S");
 		
 	}
 	
 	@Test
 	public void lambdaPlusPlusTest2(){
 		
 		// lambda++ x . S K x K
 		Lambda lambda = new Lambda(2);
 		Var var = new Var("$x");
 		Combinator S = new DummyCombinator("S"); 
 		Combinator K = new DummyCombinator("K");
 		
 		Node root = new Node(NodeFieldFactory.create(lambda), NodeFieldFactory.create(var));
 		Node second = new Node(NodeFieldFactory.create(root), NodeFieldFactory.create(S));
 		Node third = new Node(NodeFieldFactory.create(second), NodeFieldFactory.create(K));
 		Node fourth = new Node(NodeFieldFactory.create(third), NodeFieldFactory.create(var));
 		Node fifth = new Node(NodeFieldFactory.create(fourth), NodeFieldFactory.create(K));
 		
 		Abstracter ab = new Abstracter(root);
 		Node result = ab.findAbstracter(fifth);
 		String ret = GraphSerializer.serialize(result);
 		assertEquals(ret,"S ( S K ) ( K K )");
 		
 	}
 	
 	@Test
 	public void lambdaPlusPlusTest3(){
 		
 		// lambda++ x . K x K K x
 		Lambda lambda = new Lambda(2);
 		Var var = new Var("$x");
 		Combinator K = new DummyCombinator("K");
 		
 		Node root = new Node(NodeFieldFactory.create(lambda), NodeFieldFactory.create(var));
 		Node second = new Node(NodeFieldFactory.create(root), NodeFieldFactory.create(K));
 		Node third = new Node(NodeFieldFactory.create(second), NodeFieldFactory.create(var));
 		Node fourth = new Node(NodeFieldFactory.create(third), NodeFieldFactory.create(K));
 		Node fifth = new Node(NodeFieldFactory.create(fourth), NodeFieldFactory.create(K));
 		Node sixth = new Node(NodeFieldFactory.create(fifth), NodeFieldFactory.create(var));
 		
 		Abstracter ab = new Abstracter(root);
 		Node result = ab.findAbstracter(sixth);
 		String ret = GraphSerializer.serialize(result);
 		assertEquals(ret,"S K ( K K )");
 		
 	}
 	
 	
 	@Test
 	public void lambdaPlusPlusParenthesisTest(){
 		
 		// lambda++ x . S (K x)
 		Lambda lambda = new Lambda(2);
 		Var var = new Var("$x");
 		Combinator S = new DummyCombinator("S"); 
 		Combinator K = new DummyCombinator("K");
 		
 		Node root = new Node(NodeFieldFactory.create(lambda), NodeFieldFactory.create(var));
 		Node second = new Node(NodeFieldFactory.create(root), NodeFieldFactory.create(S));		
 		Node child = new Node(NodeFieldFactory.create(K), NodeFieldFactory.create(var));
 		Node third = new Node(NodeFieldFactory.create(second), NodeFieldFactory.create(child));
 		
 		Abstracter ab = new Abstracter(root);
 		Node result = ab.findAbstracter(third);
 		String ret = GraphSerializer.serialize(result);
 		assertEquals(ret,"S ( K S ) K");
 		
 	}
 	
 	@Test
 	public void lambda3PlusTest(){
 		
 		// lambda+++x . x S K
 		Lambda lambda = new Lambda(3);
 		Var var = new Var("$x");
 		Combinator S = new DummyCombinator("S"); 
 		Combinator K = new DummyCombinator("K");
 		
 		Node root = new Node(NodeFieldFactory.create(lambda), NodeFieldFactory.create(var));
 		Node second = new Node(NodeFieldFactory.create(root), NodeFieldFactory.create(var));		
 		Node third = new Node(NodeFieldFactory.create(second), NodeFieldFactory.create(S));
 		Node fourth = new Node(NodeFieldFactory.create(third), NodeFieldFactory.create(K));
 		
 		Abstracter ab = new Abstracter(root);
 		Node result = ab.findAbstracter(fourth);
 		String ret = GraphSerializer.serialize(result);
 		assertEquals(ret,"S I ( K ( S K ) )");
 		
 		
 	}
 
 @Test
 public void lambda3PlusTest2(){
 		
 		// lambda+++x . S S K
 		Lambda lambda = new Lambda(3);
 		Var var = new Var("$x");
 		Combinator S = new DummyCombinator("S");
 		Combinator K = new DummyCombinator("K");
 		
 		Node root = new Node(NodeFieldFactory.create(lambda), NodeFieldFactory.create(var));
 		Node second = new Node(NodeFieldFactory.create(root), NodeFieldFactory.create(S));		
 		Node third = new Node(NodeFieldFactory.create(second), NodeFieldFactory.create(S));
 		Node fourth = new Node(NodeFieldFactory.create(third), NodeFieldFactory.create(K));
 	
 		Abstracter ab = new Abstracter(root);
 		Node result = ab.findAbstracter(fourth);
 		String ret = GraphSerializer.serialize(result);
 		assertEquals(ret,"K ( S S K )");
 		
 		
 	}
 
 @Test
 public void debugTest(){
 	
 	// I ( lambda+ x . K )
 	Lambda lambda = new Lambda(3);
 	Var var = new Var("$x");
 	Combinator I = new DummyCombinator("I");
 	Combinator K = new DummyCombinator("K");
 	
 	Node childRoot = new Node(NodeFieldFactory.create(lambda), NodeFieldFactory.create(var));
 	Node child = new Node(NodeFieldFactory.create(childRoot), NodeFieldFactory.create(K));
 	Node root = new Node(NodeFieldFactory.create(I), NodeFieldFactory.create(child));
 	
 	Abstracter ab = new Abstracter(root);
 	Node result = ab.getAbstractedGraph();
 	String ret = GraphSerializer.serialize(result);
 	assertEquals(ret,"I ( K K )");
 }
 
 @Test
 public void lambda4PlusTest() throws CompilerException{
 	CombinatorManager manager = CombinatorManager.getInstance();
 	manager.addCombinator(new B());
 	
 	// lambda++++ x . K ( S K x )
 	Lambda lambda = new Lambda(4);
 	Var var = new Var("$x");
 	Combinator S = new DummyCombinator("S");
 	Combinator K = new DummyCombinator("K");
 	
 	Node root = new Node(NodeFieldFactory.create(lambda), NodeFieldFactory.create(var));
 	Node childRoot = new Node(NodeFieldFactory.create(S), NodeFieldFactory.create(K));
 	Node child = new Node(NodeFieldFactory.create(childRoot), NodeFieldFactory.create(var));
 	Node second = new Node(NodeFieldFactory.create(root), NodeFieldFactory.create(K));
 	Node third = new Node(NodeFieldFactory.create(second), NodeFieldFactory.create(child));
 	
	Abstracter ab = new Abstracter(third);
 	Node result = ab.getAbstractedGraph();
 	String ret = GraphSerializer.serialize(result);
 	assertEquals(ret,"B K ( S K )");
 }
 
 	
 }
