 package j3chess.test;
 
 import static org.junit.Assert.assertEquals;
 import j3chess.Chessboard;
 import j3chess.EntitySystem;
 import j3chess.J3ChessApp;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class GameBasicTest {
	static EntitySystem mEntitySystem;
	static Chessboard mChessboard;
	
     @BeforeClass
     public static void setup() {
         mEntitySystem = new EntitySystem();
         mChessboard = new Chessboard(mEntitySystem);
     }
    
     /**
     * This test checks whether a field that is clicked on is actually selected.
      */
     @Test
    public void twoWayClick() {
    	assertEquals("Field 0,0 is not where it's meant to be",
    			mChessboard.getFieldByXY(0.94f, 0.55f), mChessboard.getField(0, 0));
    	assertEquals("Field 0,0 is not where it's meant to be",
    			mChessboard.getFieldByXY(0.94f, 0.55f), mChessboard.getField(0, 0));
    	assertEquals("Field 0,0 is not where it's meant to be",
    			mChessboard.getFieldByXY(0.94f, 0.55f), mChessboard.getField(0, 0));
     }
 }
