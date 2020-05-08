 package test;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import spiderman.Direction;
 import spiderman.NamedRelationship;
 import storage.BasicGraph;
 import storage.Graph;
 import storage.Node;
 
 public class TestDB2 {
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public final void test() {
 		Graph g = new BasicGraph(true);
 		
 		Node<String> frank = g.addNode("Frank");
 		Node<String> bill = g.addNode("Bill");
 		Node<String> hank = g.addNode("Hank");
 		Node<String> mike = g.addNode("Mike");
 		Node<String> chicago = g.addNode("Chicago");
 		Node<String> sally = g.addNode("Sally");
 		
 		g.addRelationship(new NamedRelationship("HATES"), Direction.Directed, frank, bill);
 		g.addRelationship(new NamedRelationship("DATING"), Direction.Undirected, bill, sally);
 		g.addRelationship(new NamedRelationship("EMPLOYS"), Direction.Directed, sally, hank);
 		g.addRelationship(new NamedRelationship("LEASES_TO"), Direction.Directed, hank, mike);
 		g.addRelationship(new NamedRelationship("LIVES_IN"), Direction.Directed, mike, chicago);
 		g.addRelationship(new NamedRelationship("LIVES_IN"), Direction.Directed, hank, chicago);
 		g.addRelationship(new NamedRelationship("LIVES_IN"), Direction.Directed, frank, chicago);
 		
		

 
 
 		
 	}
 
 }
