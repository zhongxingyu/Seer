 package test.jneander.tictactoe.junit4;
 
 import org.junit.Before;
 import org.junit.Test;
import static org.junit.Assert.*;
 
 import com.jneander.tictactoe.game.Game;
 import com.jneander.tictactoe.game.Mark;
 
 public class GameTest {
   private Game game;
   private final int boardSize = 3;
   private final int indexCount = boardSize * boardSize;
 
   @Before
   public void setUp() {
     game = new Game();
   }
 
   @Test
   public void newGameHasNoWinner() {
     assertEquals( Mark.BLANK, game.getWinner() );
   }
 
   @Test
   public void newGameIsNotOver() {
     assertFalse( game.isGameOver() );
   }
 
   @Test
   public void newGameHasNoMarks() {
     for ( int spaceIndex = 0; spaceIndex < indexCount; spaceIndex++ )
       assertFalse( game.positionIsMarked( spaceIndex ) );
   }
 
   @Test
   public void canMakePlayerMark() {
     game.makePlayerMarkAtPosition( 0 );
     assertTrue( game.positionIsMarked( 0 ) );
 
     game.makePlayerMarkAtPosition( 8 );
     assertTrue( game.positionIsMarked( 8 ) );
   }
 
   @Test
   public void gameCanReset() {
     game.makePlayerMarkAtPosition( 0 );
     game.reset();
     assertFalse( game.positionIsMarked( 0 ) );
   }
 }
