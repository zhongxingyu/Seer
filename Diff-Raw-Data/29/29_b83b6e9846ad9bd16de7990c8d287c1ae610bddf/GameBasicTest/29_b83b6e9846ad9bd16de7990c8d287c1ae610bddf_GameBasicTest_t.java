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
     * This test checks whether a field that is clicked on is actually there.
      */
     @Test
    public final void twoWayClick() {
        assertEquals("Field is not what it's meant to be",
                mChessboard.getFieldByXY(0.93f, 0.052f),
                mChessboard.getField(0, 0));
        assertEquals("Field is not what it's meant to be",
                mChessboard.getFieldByXY(0.223f, -0.762f),
                mChessboard.getField(19, 1));
        assertEquals("Field is not what it's meant to be",
                mChessboard.getFieldByXY(0.93f, -0.75f), null);
     }
 }
