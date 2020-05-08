 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package isad.w6.practicum.o1;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author jappie
  */
 public class TreeTest {
 	Tree<String> instance;
 	public TreeTest() {
 	}
 	
 	@BeforeClass
 	public static void setUpClass() {
 	}
 	
 	@AfterClass
 	public static void tearDownClass() {
 	}
 	
 	@Before
 	public void setUp() {
 		System.out.print("Practicum bullshit: ");
 		Node<String> root = new Node<String>("F", null, 
 								new Node<String>("D", 
 							new Node<String>("I"), new Node<String>("C", 
 							new Node<String>("G", null, new Node<String>("A")),
 				
 																	new Node<String>("B", 
 														new Node<String>("A")))));
 		instance = new Tree<String>(root);
 	}
 	
 	@After
 	public void tearDown() {
 	}
 
 	@Test
 	public void testTreePreOrder() {
 		System.out.println("tree working");
 
 		assertEquals("FDICGABA", instance.printPreOrder());
 		
 	}
 	
 	@Test
 	public void testCoordinates() {
 		System.out.println("coordinates");
 		instance.calcX();
 		instance.calcY();
		assertEquals("F(1,5)D(3,4)I(2,1)C(6,3)G(4,2)A(5,1)B(8,2)A(7,1)", instance.printPreWithCoordinates());
 		
 	}
 }
