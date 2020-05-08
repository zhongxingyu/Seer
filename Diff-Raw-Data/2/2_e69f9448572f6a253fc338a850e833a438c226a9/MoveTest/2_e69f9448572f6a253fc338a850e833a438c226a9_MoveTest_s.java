 package TestClasses;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import ChiniMess.Board;
 import ChiniMess.Move;
 import ChiniMess.Square;
 
 public class MoveTest {
 
 	@Test
     public void equalConstructorTest() {
         Move m1 = new Move(new Square("a1"), new Square("b2"));
         Move m2 = new Move("a1b2");
         assertEquals("should be equal", m1, m2);
 
         m1 = new Move("b2c3");
         m2 = new Move(new Square(1,1),new Square(2,2));
         assertEquals("should be equal", m1, m2);
         
         m1 = new Move("c3b2");
         m2 = new Move(new Square(2,2),new Square(1,1));
         assertEquals("should be equal", m1, m2);
         
         m1 = new Move("d5e1");
         m2 = new Move(new Square(3,4),new Square(4,0));
         assertEquals("should be equal", m1, m2);
         
         m1 = new Move("e6a2");
         m2 = new Move(new Square(4,5),new Square(0,1));
         assertEquals("should be equal", m1, m2);
     }
     
     @Test
     public void equalToString(){      
         assertEquals(new Move("a1e6").toString(), "a1-e6");
         assertEquals(new Move("b2d5").toString(), "b2-d5");
         assertEquals(new Move("c3c4").toString(), "c3-c4");
         assertEquals(new Move("d4b3").toString(), "d4-b3");
         assertEquals(new Move("e5a2").toString(), "e5-a2");
     }
     
     @Test
     public void isValidTest(){      
         assertTrue(new Move("a1a2").isValid());
         assertTrue(new Move("a1b3").isValid());
         assertTrue(new Move("a1c4").isValid());
         assertTrue(new Move("a1d5").isValid());
         assertTrue(new Move("a1e6").isValid());
         assertTrue(new Move("b2a1").isValid());
         assertTrue(new Move("b2b3").isValid());
         assertTrue(new Move("b2c4").isValid());
         assertTrue(new Move("b2d5").isValid());
         assertTrue(new Move("b3b2").isValid());
         assertTrue(new Move("c3a2").isValid());
         assertTrue(new Move("c3b1").isValid());
         assertTrue(new Move("c3c2").isValid());
         assertTrue(new Move("c3d6").isValid());
         assertTrue(new Move("c3e5").isValid());
         assertTrue(new Move("d2e1").isValid());
         assertTrue(new Move("d2d3").isValid());
         assertTrue(new Move("d2c1").isValid());
         assertTrue(new Move("d2a2").isValid());
         assertTrue(new Move("d2b4").isValid());
         assertTrue(new Move("e4a1").isValid());
         assertTrue(new Move("e4b3").isValid());
         assertTrue(new Move("e4c4").isValid());
         assertTrue(new Move("e2a1").isValid());
         assertTrue(new Move("e6d3").isValid());
    
         //False
         assertFalse(new Move("a1a1").isValid());
         assertFalse(new Move("a6b7").isValid());
         assertFalse(new Move("b2b2").isValid());
         assertFalse(new Move("c3c3").isValid());
         assertFalse(new Move("d4d4").isValid());
         assertFalse(new Move("e4e4").isValid());
         assertFalse(new Move("4dd4").isValid());
         
     }
     
     @Test
     public void pathIsFreeTest(){
     	
     	Board board = new Board();
     	
     	Move m = new Move("a2a4");			//move on Y-axis
     	assertTrue(m.pathIsFree(board));
     	m = new Move("a2a5");
     	assertTrue(m.pathIsFree(board));
     	m = new Move("b2b3");
     	assertTrue(m.pathIsFree(board));
     	m = new Move("c2c4");
     	assertTrue(m.pathIsFree(board));
       	m = new Move("d2d5");
     	assertTrue(m.pathIsFree(board));
       	m = new Move("e2e3");
     	assertTrue(m.pathIsFree(board));
         m = new Move("a5a4");			
     	assertTrue(m.pathIsFree(board));
     	m = new Move("a5a2");
     	assertTrue(m.pathIsFree(board));
     	m = new Move("b5b3");
     	assertTrue(m.pathIsFree(board));
     	m = new Move("c5c4");
     	assertTrue(m.pathIsFree(board));
       	m = new Move("d5d2");
     	assertTrue(m.pathIsFree(board));
       	m = new Move("b6b3"); //special Knight move
     	assertTrue(m.pathIsFree(board));
       	m = new Move("e5e3");
     	assertTrue(m.pathIsFree(board));	//end move on Y-axis
     	
     	m = new Move("a2b3");				//move diagonal
     	assertTrue(m.pathIsFree(board));
     	m = new Move("b2a3");
     	assertTrue(m.pathIsFree(board));
     	m = new Move("c2b3");
     	assertTrue(m.pathIsFree(board));
       	m = new Move("d2e3");
     	assertTrue(m.pathIsFree(board));
       	m = new Move("e2b5");
     	assertTrue(m.pathIsFree(board));
         m = new Move("a5b4");			
     	assertTrue(m.pathIsFree(board));
     	m = new Move("a5c3");
     	assertTrue(m.pathIsFree(board));
     	m = new Move("b5a4");
     	assertTrue(m.pathIsFree(board));
     	m = new Move("c5e3");
     	assertTrue(m.pathIsFree(board));
       	m = new Move("d5a2");
     	assertTrue(m.pathIsFree(board));
       	m = new Move("e5d4");
     	assertTrue(m.pathIsFree(board));
      	m = new Move("a2d5");
     	assertTrue(m.pathIsFree(board));
      	m = new Move("d5a2");
     	assertTrue(m.pathIsFree(board));
      	m = new Move("e2b5");
     	assertTrue(m.pathIsFree(board));
      	m = new Move("b5e2");
     	assertTrue(m.pathIsFree(board));	//end move diagonal
     	
     	board = new Board("22 B\n"
     					+ "kqbnr\n"
     					+ "p....\n"
     					+ ".....\n"
     					+ "P....\n"
     					+ "P..PP\n"
     					+ "RNBQK");
     	
     	m = new Move("a2c2");				//move on X-axis
     	assertTrue(m.pathIsFree(board));
     	m = new Move("a2b2");
     	assertTrue(m.pathIsFree(board));
     	m = new Move("a4e4");
     	assertTrue(m.pathIsFree(board));
       	m = new Move("d5c5");
     	assertTrue(m.pathIsFree(board));
       	m = new Move("d5b5");
     	assertTrue(m.pathIsFree(board));
        
     	board = new Board();
     	
     	m = new Move("a1a2"); 			//move on Y-axis
     	assertFalse(m.pathIsFree(board));
     	m = new Move("b1b3");
     	assertFalse(m.pathIsFree(board));
     	m = new Move("a2a6");
     	assertFalse(m.pathIsFree(board));
     	m = new Move("a6a5");
     	assertFalse(m.pathIsFree(board));
     	m = new Move("c5c1");
     	assertFalse(m.pathIsFree(board));
     	m = new Move("d6d3");
     	assertFalse(m.pathIsFree(board));
     	m = new Move("e1e6");
     	assertFalse(m.pathIsFree(board));
     	m = new Move("b5b1");
     	assertFalse(m.pathIsFree(board));
     	
        	m = new Move("a1c3"); 			//move on diagonal
     	assertFalse(m.pathIsFree(board));
     	m = new Move("c1a3");
     	assertFalse(m.pathIsFree(board));
     	m = new Move("a6c4"); //b5c4
     	assertFalse(m.pathIsFree(board));
     	m = new Move("a2e6");
     	assertFalse(m.pathIsFree(board));
     	m = new Move("e6a2");
     	assertFalse(m.pathIsFree(board));
     	m = new Move("e5a1");
     	assertFalse(m.pathIsFree(board));
     	m = new Move("a1e5");
     	assertFalse(m.pathIsFree(board));
     	m = new Move("a1e5"); 
     	assertFalse(m.pathIsFree(board));
     	m = new Move("e6a2");
     	assertFalse(m.pathIsFree(board));
     	m = new Move("a6e2");
     	assertFalse(m.pathIsFree(board));
     	m = new Move("e1a5");
     	assertFalse(m.pathIsFree(board));
     	
     	
     	assertFalse(m.pathIsFree(board));//move on x-axis
     	m = new Move("a2e2");
     	assertFalse(m.pathIsFree(board));
     	m = new Move("e1a1");
     	assertFalse(m.pathIsFree(board));
     	m = new Move("e6a6");
     	assertFalse(m.pathIsFree(board));
     	m = new Move("a5e5");
     	assertFalse(m.pathIsFree(board));
 
 
     }
 
 }
