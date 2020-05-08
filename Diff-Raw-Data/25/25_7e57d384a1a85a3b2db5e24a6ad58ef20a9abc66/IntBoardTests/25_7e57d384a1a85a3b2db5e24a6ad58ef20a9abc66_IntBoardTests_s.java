 import java.util.LinkedList;
 import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 
 public class IntBoardTests extends TestCase {
 	IntBoard board;
 	LinkedList testList;
 	TreeSet targets;
 	
 	@Before
 	public void setup(){
 		board = new IntBoard();
 	}
 	
 	public IntBoardTests(){
 		board = new IntBoard();
 		board.calcAdjacencies();
 	}
 	
 	//pass
 	@Test
 	public void testAdjacency_0(){
 		testList = board.getAdjList(0);
 		Assert.assertTrue(!testList.isEmpty());
 		Assert.assertTrue(testList.contains(1));
 		Assert.assertTrue(testList.contains(4));
 		Assert.assertTrue(testList.size() == 2);
 	}
 	
 	//pass
 	@Test
 	public void testAdjacency_7(){
 		testList = board.getAdjList(7);
 		Assert.assertTrue(!testList.isEmpty());
 		Assert.assertTrue(testList.contains(3));
 		Assert.assertTrue(testList.contains(6));
 		Assert.assertTrue(testList.contains(11));
 		Assert.assertTrue(testList.size() == 3);
 			
 	}
 	
 	//pass
 	@Test
 	public void testAdjacency_8(){
 		testList = board.getAdjList(8);
 		Assert.assertTrue(!testList.isEmpty());
 		Assert.assertTrue(testList.contains(4));
 		Assert.assertTrue(testList.contains(9));
 		Assert.assertTrue(testList.contains(12));
 		Assert.assertTrue(testList.size() == 3);
 	}	
 	
 	//pass
 	@Test
 	public void testAdjacency_10(){
 		testList = board.getAdjList(10);
 		Assert.assertTrue(!testList.isEmpty());
 		Assert.assertTrue(testList.contains(6));
 		Assert.assertTrue(testList.contains(9));
 		Assert.assertTrue(testList.contains(11));
 		Assert.assertTrue(testList.contains(14));
 		Assert.assertTrue(testList.size() == 4);
 			
 	}
 	
 	//pass
 	@Test
 	public void testAdjacency_5(){
 		testList = board.getAdjList(5);
 		Assert.assertTrue(!testList.isEmpty());
 		Assert.assertTrue(testList.contains(6));
 		Assert.assertTrue(testList.contains(9));
 		Assert.assertTrue(testList.contains(1));
 		Assert.assertTrue(testList.contains(4));
 		Assert.assertTrue(testList.size() == 4);
 			
 	}
 	
 	//pass
 	@Test
 	public void testAdjacency_15(){
 		testList = board.getAdjList(15);
 		Assert.assertTrue(!testList.isEmpty());
 		Assert.assertTrue(testList.contains(11));
 		Assert.assertTrue(testList.contains(14));
 		Assert.assertTrue(testList.size() == 2);
 			
 	}
 	
 	//fail
 	@Test
 	public void testTargets0_3()
 	{
		board.calcTargets(0, 3);
 		targets= board.getTargets();
 		Assert.assertEquals(6, targets.size());
 		Assert.assertTrue(targets.contains(12));
 		Assert.assertTrue(targets.contains(9));
 		Assert.assertTrue(targets.contains(1));
 		Assert.assertTrue(targets.contains(6));
 		Assert.assertTrue(targets.contains(3));
 		Assert.assertTrue(targets.contains(4));
 	}
 	
 	//fail
 	@Test
 	public void testTargets0_1()
 	{
		board.calcTargets(0, 1);
 		targets= board.getTargets();
 		Assert.assertEquals(2, targets.size());
 		Assert.assertTrue(targets.contains(1));
 		Assert.assertTrue(targets.contains(4));
 		
 	}
 	
 	//fail
 	@Test
 	public void testTargets1_2()
 	{
		board.calcTargets(1, 2);
 		targets= board.getTargets();
 		Assert.assertEquals(4, targets.size());
 		Assert.assertTrue(targets.contains(6));
 		Assert.assertTrue(targets.contains(9));
 		Assert.assertTrue(targets.contains(3));
 		Assert.assertTrue(targets.contains(4));
 		
 	}
 	
 	//fail
 	@Test
 	public void testTargets5_1()
 	{
		board.calcTargets(5, 1);
 		targets= board.getTargets();
 		Assert.assertEquals(4, targets.size());
 		Assert.assertTrue(targets.contains(6));
 		Assert.assertTrue(targets.contains(9));
 		Assert.assertTrue(targets.contains(1));
 		Assert.assertTrue(targets.contains(4));
 		
 	}
 	
 	//fail
 	@Test
 	public void testTargets13_1()
 	{
		board.calcTargets(5, 1);
 		targets= board.getTargets();
 		Assert.assertEquals(3, targets.size());
 		Assert.assertTrue(targets.contains(9));
 		Assert.assertTrue(targets.contains(12));
 		Assert.assertTrue(targets.contains(14));
 		
 	}
 	
 	//fail
 	@Test
 	public void testTargets11_1()
 	{
		board.calcTargets(11, 1);
 		targets= board.getTargets();
 		Assert.assertEquals(3, targets.size());
 		Assert.assertTrue(targets.contains(7));
 		Assert.assertTrue(targets.contains(10));
 		Assert.assertTrue(targets.contains(15));
 		
 	}
 	
 	//pass
 	@Test
 	public void testIndex(){
 		int actual = board.calcIndex(2, 3);
 		int expected = 11;
 		Assert.assertEquals(expected, actual);
 	}
 	
 }
