 
 
 import static org.junit.Assert.*;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * This class will test the total experience of game focussing on the interaction between
  *  the UI and the Logic.
  *
  * @author  Josh Gillham
  * @version 10-4-12
  */
 public class integrationTestGamePlay {
     SetupBase logicWrapper;
     Logic game;
     String gameWord= "Fuel";
     /**
      * Default constructor for test class integrationTestGamePlay
      */
     public integrationTestGamePlay()
     {
     }
 
     /**
      * Sets up the test fixture.
      *
      * Called before every test case method.
      */
     @Before
     public void setUp() throws Exception {
         logicWrapper= new SetupBase();
         logicWrapper.addManager( "Default" );
         logicWrapper.addPlayer( "Bob" );
         game= logicWrapper.getGame( gameWord );
         
     }
 
     /**
      * Tears down the test fixture.
      *
      * Called after every test case method.
      */
     @After
     public void tearDown()
     {
     }
     abstract class GameEventsBaseTester extends GameEventsBase {
         int badGuesses= 0;
         int errorGuesses= 0;
         int ambiguousGuesses= 0;
         
         public abstract void makeAssertions(); // String guesses, int maxGuesses
         //Calculate the results
 //         {
 //             for( int i= 0; i < guesses.length() && i < maxGuesses; ++i ) {
 //                 
 //             }
 //         }
         public void guess( Logic game, char c ){
             System.out.println( "guess: " + c );
             try {
                 if( !game.makeGuess( c ) )
                     ++badGuesses;
             }catch( Logic.AmbiguousGuessException e ){
                 ++ambiguousGuesses;
             }catch( IllegalArgumentException e ) {
                 ++errorGuesses;
            }catch( PlayerOutOfTurnException e ) {
                e.printStackTrace()
         }
         public void guess( Logic game, String guess ){
             System.out.println( "guess: " + guess );
             try {
                 if( !game.makeGuess( guess ) )
                     ++badGuesses;
             }catch( Logic.AmbiguousGuessException e ){
                 ++ambiguousGuesses;
             }catch( IllegalArgumentException e ) {
                 ++errorGuesses;
             }
         }
     }
 
     
     
     final int MAX_TURNS= 40;
     @Test
     public void testPlayerWins(){
         final int maxGuesses= gameWord.length();
         GameEventsBaseTester tester= new GameEventsBaseTester(){
             int i= 0;
             public void playerUp( Player player ){
                 if( i < maxGuesses )
                     this.guess( game, gameWord.charAt( i++ ) );
             }
             public void makeAssertions(){
                 assertFalse( gameOver );
                 assertNotNull( gameWinningTeam );
                 assertEquals( 0, badGuesses );
                 assertEquals( 0, errorGuesses );
             }
         };
         game.setMaxAttempts( maxGuesses );
         game.setGameEventsHandler( tester );
         
         int counter= 0;
         while( counter++ < MAX_TURNS && game.getGameState() == Logic.Statis.STARTED ) {
             game.rotateTurn();
         }
         assertTrue( counter < MAX_TURNS );
         assertEquals( Logic.Statis.WINNER, game.getGameState() );
         tester.makeAssertions();
         
     }
     
     @Test
     public void testPlayerLooses(){
         final String guesses= "abcdefghijkl";
         final int maxGuesses= gameWord.length();
         GameEventsBaseTester tester= new GameEventsBaseTester(){
             
             int i= 0;
             public void playerUp( Player player ){
                 System.out.println( "guessplayerUp " + i );
                 if( i < maxGuesses )
                     this.guess( game, guesses.charAt( i++ ) );
             }
             public void makeAssertions(){
                 assertTrue( gameOver );
                 assertNull( gameWinningTeam );
                 assertEquals( maxGuesses, badGuesses );
                 assertEquals( 0, errorGuesses );
             }
         };
         game.setMaxAttempts( maxGuesses );
         game.setGameEventsHandler( tester );
         int counter= 0;
         while( counter++ < MAX_TURNS && game.getGameState() == Logic.Statis.STARTED ) {
             System.out.println( "inloop " + counter );
             game.rotateTurn();
         }
         assertTrue( counter < MAX_TURNS );
         assertEquals( Logic.Statis.OVER, game.getGameState() );
         tester.makeAssertions();
     }
     @Test
     public void testPlayerHasAFewBadGuessesButWins(){
         final String guesses= "fyu.eyl";
         final int maxGuesses= gameWord.length() + 3;
         GameEventsBaseTester tester= new GameEventsBaseTester(){
             
             int i= 0;
             public void playerUp( Player player ){
                 if( i < maxGuesses )
                     this.guess( game, guesses.charAt( i++ ) );
             }
             public void makeAssertions(){
                 assertFalse( gameOver );
                 assertNotNull( gameWinningTeam );
                 assertEquals( 1, badGuesses );
                 assertEquals( 1, errorGuesses );
             }
         };
         game.setMaxAttempts( maxGuesses );
         game.setGameEventsHandler( tester );
         int counter= 0;
         while( counter++ < MAX_TURNS && game.getGameState() == Logic.Statis.STARTED ) {
             game.rotateTurn();
         }
         assertTrue( counter < MAX_TURNS );
         assertEquals( Logic.Statis.WINNER, game.getGameState() );
         tester.makeAssertions();
     }
     @Test
     public void testPlayerHasAFewBadGuessesButLooses(){
         final String guesses= "fyu.eyil";
         final int maxGuesses= gameWord.length()+1;
         GameEventsBaseTester tester= new GameEventsBaseTester(){
             
             int i= 0;
             public void playerUp( Player player ){
                 System.out.println( "playerUP: " + i );
                 if( i < guesses.length() - 1 )
                     this.guess( game, guesses.charAt( i++ ) );
             }
             public void makeAssertions(){
                 assertTrue( gameOver );
                 assertNull( gameWinningTeam );
                 assertEquals( 2, badGuesses );
                 assertEquals( 1, errorGuesses );
                 assertEquals( 1, ambiguousGuesses );
             }
         };
         game.setMaxAttempts( maxGuesses );
         game.setGameEventsHandler( tester );
         int counter= 0;
         while( counter++ < MAX_TURNS && game.getGameState() == Logic.Statis.STARTED ) {
             game.rotateTurn();
         }
         assertTrue( counter < MAX_TURNS );
         assertEquals( Logic.Statis.OVER, game.getGameState() );
         tester.makeAssertions();
     }
     @Test
     public void testPlayerGetsFrustrated(){
         final String guesses= "$^&^)&(,.,.<>:\"'fyu.eyil";
         final int maxGuesses= gameWord.length()+1;
         GameEventsBaseTester tester= new GameEventsBaseTester(){
             
             int i= 0;
             public void playerUp( Player player ){
                 System.out.println( "playerUP: " + i );
                 if( i < guesses.length() - 1 )
                     this.guess( game, guesses.charAt( i++ ) );
             }
             public void makeAssertions(){
                 assertTrue( gameOver );
                 assertNull( gameWinningTeam );
                 assertEquals( 2, badGuesses );
                 assertEquals( 17, errorGuesses );
                 assertEquals( 1, ambiguousGuesses );
             }
         };
         game.setMaxAttempts( maxGuesses );
         game.setGameEventsHandler( tester );
         int counter= 0;
         while( counter++ < MAX_TURNS && game.getGameState() == Logic.Statis.STARTED ) {
             game.rotateTurn();
         }
         assertTrue( counter < MAX_TURNS );
         assertEquals( Logic.Statis.OVER, game.getGameState() );
         tester.makeAssertions();
     }
     @Test
     public void testMakesAFewGuessesThenKnowsTheWord(){
         final String[] guesses= {
             "f","u","l","fuel"
         };
         final int maxGuesses= gameWord.length()+1;
         GameEventsBaseTester tester= new GameEventsBaseTester(){
             
             int i= 0;
             public void playerUp( Player player ){
                 System.out.println( "playerUP: " + i );
                 if( i < guesses.length )
                     this.guess( game, guesses[ i++ ] );
             }
             public void makeAssertions(){
                 assertFalse( gameOver );
                 assertNotNull( gameWinningTeam );
                 assertEquals( 0, badGuesses );
                 assertEquals( 0, errorGuesses );
                 assertEquals( 0, ambiguousGuesses );
             }
         };
         game.setMaxAttempts( maxGuesses );
         game.setGameEventsHandler( tester );
         int counter= 0;
         while( counter++ < MAX_TURNS && game.getGameState() == Logic.Statis.STARTED ) {
             game.rotateTurn();
         }
         tester.makeAssertions();
         assertEquals( Logic.Statis.WINNER, game.getGameState() );
         assertTrue( counter < MAX_TURNS );
         
         
     }
 }
