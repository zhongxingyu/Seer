 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Test the Board Class methods. 
  *
  * @author Mark Adams
  * @version 2013/05/01
  */
 public class BoardTest 
 {
   Board board;
   
 	@Before
 	public void setUp() throws Exception
 	{
 		this.board = new Board();
 	}
 
 	@Test
 	public void testInitializeToToken()
 	{
 		assertEquals(true, this.board.get(0).get(0).equals(' '));
 		assertEquals(true, this.board.get(0).get(1).equals(' '));
 		assertEquals(true, this.board.get(0).get(2).equals(' '));
 		assertEquals(true, this.board.get(1).get(0).equals(' '));
 		assertEquals(true, this.board.get(1).get(1).equals(' '));
 		assertEquals(true, this.board.get(1).get(2).equals(' '));
 		assertEquals(true, this.board.get(2).get(0).equals(' '));
 		assertEquals(true, this.board.get(2).get(1).equals(' '));
 		assertEquals(true, this.board.get(2).get(2).equals(' '));		
 	}
 	
 	public void testToString()
 	{
		assertEquals(true, this.board.toString() == "   \n   \n   \n");
 	}
 }
