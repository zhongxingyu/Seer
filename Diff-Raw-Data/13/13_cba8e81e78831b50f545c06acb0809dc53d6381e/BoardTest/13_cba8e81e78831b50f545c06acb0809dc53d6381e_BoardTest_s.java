 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Notandi
  * Date: 12.11.2012
  * Time: 17:03
  */
 public class BoardTest extends TestCase {
     public BoardTest() {
     }
 
     public void testUpdateBoardOneInput() throws Exception {
         Board b = new Board();
         char c = 'X';
         assertEquals("tests UpdateBoard wants true back", true, b.updateBoard(c, 0));
     }
 
     public void testUpdateBoardTwoInput() throws Exception {
         Board b = new Board();
         char c = 'X';
         assertEquals("tests UpdateBoard wants true back", true, b.updateBoard(c, 0));
         assertEquals("tests UpdateBoard wants false back", false, b.updateBoard(c, 0));
     }
 
     public void testUpdateBoardTwoDiffInput() throws Exception {
         Board b = new Board();
         char c = 'X';
         char d = 'O';
         assertEquals("tests UpdateBoard wants false back", true, b.updateBoard(d, 0));
         assertEquals("tests UpdateBoard wants true back", false, b.updateBoard(c, 0));
     }
 
     public void setUp() throws Exception {
         super.setUp();
     }
 
     public void tearDown() throws Exception {
         super.tearDown();
     }
 
     public static Test suite() {
         return new TestSuite(BoardTest.class);
     }
 }
