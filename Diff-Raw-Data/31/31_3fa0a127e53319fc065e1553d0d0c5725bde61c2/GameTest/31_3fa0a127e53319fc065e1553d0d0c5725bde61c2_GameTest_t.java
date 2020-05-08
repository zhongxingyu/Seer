 package test.jneander.tictactoe.junit4;
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

 import org.junit.Before;
 import org.junit.Test;
 
import com.jneander.tictactoe.game.Console;
 import com.jneander.tictactoe.game.Game;
 import com.jneander.tictactoe.game.Mark;
 
 public class GameTest {
   private Game game;
   private final int boardSize = 3;
   private final int indexCount = boardSize * boardSize;
  private Console console;
 
   @Before
   public void setUp() {
     game = new Game();
    console = new Console() {

      @Override
      public void reportGameTied() {}

      @Override
      public void reportGameLost() {}

      @Override
      public void reportGameWon() {}

      @Override
      public void reportIllegalMove() {}

      @Override
      public void displayComputerMark( int position ) {}
    };
    game.setConsole( console );
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
    game.reset(); // second reset also resets turn order
     assertFalse( game.positionIsMarked( 0 ) );
   }
 }
