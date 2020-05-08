 
 
 import static org.junit.Assert.*;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * This test unit will check the integrity of the Game class.
  * 
  * TODO:
  *  testGetStatusWord_InGame()
  *
  * @author  Josh Gillham
  * @version 9-19-12
  */
 public class testGame
 {
     /**
      * Default constructor for test class testGame
      */
     public testGame()
     {
     }
 
     /**
      * Sets up the test fixture.
      *
      * Called before every test case method.
      */
     @Before
     public void setUp()
     {
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
     Game game;
     
     /**
      * Test the constructor to make sure it can successfully construct
      */
     @Before
     public void testConstructor() {
         try{
             game= new Game( Game.MIN_ATTEMPTS, Dictionary.MIN_WORDLENGTH );
         } catch( Exception e ) {
             fail( "Exception should not have occured." );
         }
     }
     
     /**
      * Test the constructor to make sure it will throw errors when given bad
      *  arguments
      */
     @Test
     public void testConstructor_BadArgs() {
         try{
             Game g= new Game( Game.MIN_ATTEMPTS - 1, Dictionary.MIN_WORDLENGTH );
             fail( "Constructor should have thrown an error." );
         }catch( Exception e ) {}
         
         try{
             Game g= new Game( Game.MIN_ATTEMPTS, Dictionary.MIN_WORDLENGTH - 1 );
             fail( "Constructor should have thrown an error." );
         }catch( Exception e ) {}
         
         try{
             Game g= new Game( Game.MIN_ATTEMPTS, Dictionary.MAX_WORDLENGTH + 1 );
             fail( "Constructor should have thrown an error." );
         }catch( Exception e ) {}       
     }
     
     /**
      * GetStatusWord should return all underscores before the game starts.
      */
     @Test
     public void testGetStatusWord_BeforeGame() {
         for( int i= Dictionary.MIN_WORDLENGTH; i <= Dictionary.MAX_WORDLENGTH; ++i ) {
             Game g= new Game( Game.MIN_ATTEMPTS, i );
             String word= g.getWordStatus();
             for( int k= 0; k <  word.length(); ++k )
                 assertEquals( '_', word.charAt( k ) );
         }
     }
     
     /**
      * Test makeGuess with a bad argument.
      */
     @Test( expected= NullPointerException.class )
     public void testMakeGuess_NullPlayer() {
        Game g= new Game( Game.MIN_ATTEMPTS, i );
         g.makeGuess( null, 'a' );
     }
     
     /**
      * Test makeGuess with a bad argument.
      */
    @Test( expected= NoSuchElementException.class )
     public void testMakeGuess_BadPlayer() {
        Game g= new Game( Game.MIN_ATTEMPTS, i );
         g.makeGuess( new Player( "Name" ), 'a' );
     }
     
     /**
      * Test makeGuess with a bad argument.
      */
     @Test( expected= IllegalArgumentException.class )
    public void testMakeGuess_BadPlayer() {
        Game g= new Game( Game.MIN_ATTEMPTS, i );
        Player player= g.addTeam().addPlayer();
         g.makeGuess( player, '9' );
     }
     
     /**
      * Test makeGuess with a bad argument.
      */
     @Test( expected= IllegalArgumentException.class )
    public void testMakeGuess_BadPlayer() {
        Game g= new Game( Game.MIN_ATTEMPTS, i );
        Player player= g.addTeam().addPlayer();
         g.startGame();
         g.makeGuess( player, '9' );
     }
     
     
 }
