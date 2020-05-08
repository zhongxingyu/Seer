 package nl.javadude.monopoly.domain;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertTrue;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.not;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertThat;
 
 public class GameTest {
     private final Game theGame = new Game();
     private final Player theFirstPlayer = theGame.addPlayer("myPlayer");
     private final Player theSecondPlayer = theGame.addPlayer("myOtherPlayer");
 
     @Before
     public void setup()
     {
         theGame.startPlay();
     }
 
 
 	@Test
 	public void shouldHaveCorrectCurrentPlayer() {
         assertThat(theGame.getCurrentPlayer(), is(notNullValue()));
         assertThat(theGame.getCurrentPlayer().getName(), is("myPlayer"));
 	}
 
     @Test
     public void testGetSquares_MultipleSquares(){
         assertTrue(theGame.getSquares().size() > 1);
     }
 
     @Test
 	public void shouldNotAllowNextPlayerIfPlayerHasntMovedYet() {
         theGame.nextPlayer();
         assertThat(theGame.getCurrentPlayer().getName(), is(not("myOtherPlayer")));
 	}
 
     @Test
 	public void shouldAllowNextPlayerIfPlayerHasMoved() {
         theGame.move();
         theGame.nextPlayer();
         assertThat(theGame.getCurrentPlayer().getName(), is("myOtherPlayer"));
 	}
 
     @Test
     public void shouldActivateCurrentPlayer() {
         assertThat(theGame.getCurrentPlayer().isActive(), is(true));
         theGame.move();
         theGame.nextPlayer();
         assertThat(theGame.getCurrentPlayer().isActive(), is(true));
     }
 
 
     @Test
     public void previousPlayerShouldHaveEndedTurnWhenNextPlayerComesOn() {
         final Player previousPlayer = theGame.getCurrentPlayer();
         theGame.move();
         theGame.nextPlayer();
         assertThat(previousPlayer.isActive(), is(false));
     }
 
     @Test
     public void currentPlayerIsNotChangedWhenPlayerDidNotMove()
     {
         theGame.nextPlayer();
         verifyCurrentPlayer(theFirstPlayer);
     }
 
     @Test
     public void currentPlayerIsChangedWhenPlayerHasMoved()
     {
         theGame.move();
         theGame.nextPlayer();
         verifyCurrentPlayer(theSecondPlayer);
     }
 
     @Test
     public void nextPlayerMustEndTurnOfPreviousPlayer()
     {
         theGame.move();
         theGame.nextPlayer();
         assertFalse(theFirstPlayer.isActive());
     }
 
     @Test
     public void nextPlayerMustStartTurnOfNewPlayer()
     {
         theGame.move();
         theGame.nextPlayer();
         assertTrue(theSecondPlayer.isActive());
     }
 
     @Test
     public void testAddPlayer() {
         Player myPlayer = new Player(theGame.getBoard(), "player");
         theGame.addPlayer(myPlayer);
         assertTrue(theGame.getPlayers().contains(myPlayer));
     }
 
     @Test
     public void startPlay(){
        Game game = new Game();
         Player myPlayer1 = mock(Player.class);
         Player myPlayer2 = mock(Player.class);
        game.addPlayer(myPlayer1);
        game.addPlayer(myPlayer2);
        game.startPlay();
         verify(myPlayer1).activate();
         verifyZeroInteractions(myPlayer2);
     }
 
     @Test
     public void testGetPlayers(){
         testAddPlayer();
     }
 
     @Test
     public void getCurrentPlayer(){
        Game game = new Game();
         Player myPlayer1 = new Player(theGame.getBoard(), "player1");
         Player myPlayer2 = new Player(theGame.getBoard(), "player2");
        game.addPlayer(myPlayer1);
        game.addPlayer(myPlayer2);
        game.startPlay();
        game.rollDice(1,2);
        game.nextPlayer();
        assertEquals(game.getCurrentPlayer(), myPlayer2);
     }
 
     @Test
     public void testRollDice(){
         //todo
     }
 
     @Test
     public void testCurrentPlayerBuy(){
         //todo
     }
 
     @Test
     public void testNextPlayer(){
         //todo
     }
 
     private void verifyCurrentPlayer(Player aPlayer)
     {
         assertEquals(aPlayer, theGame.getCurrentPlayer());
     }
 
 }
