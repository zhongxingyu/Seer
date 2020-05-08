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
 	
	@Test
 	public void testToString()
 	{
		assertEquals(true, this.board.toString().equals("   \n   \n   \n"));
		assertEquals(false, this.board.toString().equals("   \n   \n   "));
		assertEquals(false, this.board.toString().equals("         "));
	}
	
	@Test
	public void testSetPos()
	{
		this.board.setPos(0, 1, 'X');
		assertEquals(true, this.board.get(0).get(1) == 'X');
		assertEquals(false, this.board.get(0).get(1) == ' ');
		
		this.board.setPos(2, 2, '?');
		assertEquals(true, this.board.get(2).get(2) == '?');
		assertEquals(false, this.board.get(2).get(2) == '0');
		
		this.board.setPos(1, 1, ' ');
		assertEquals(true, this.board.get(1).get(1) == ' ');
		assertEquals(false, this.board.get(1).get(1) == '?');
 	}
 }
