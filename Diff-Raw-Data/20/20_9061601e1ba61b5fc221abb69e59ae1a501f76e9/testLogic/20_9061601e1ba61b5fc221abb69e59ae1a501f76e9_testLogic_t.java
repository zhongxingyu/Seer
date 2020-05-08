 import static org.junit.Assert.*;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import java.util.List;
 import java.util.LinkedList;
 
 /**
  * This test unit will check the integrity of the Logic class.
  * 
  * TODO:
  *  testGetStatusWord_InGame()
  *  testMakeGuess_InGame()
  *  testGetAttempts()
  *  -testRotateTurn()
  *  getGuesses
  *
  * @author  Josh Gillham
  * @version 9-23-12
  */
 public class testLogic {
     
     /** Keeps the Test_InstrumentLogic class for other tests. */
     SetupBase wrapLogic;
     /** Keeps a Logic instance for other tests. */
     Logic game;
     
     
     /**
      * Test the constructor to make sure it can successfully construct
      */
     @Before
     public void testConstructor() throws Exception {
         wrapLogic= new SetupBase();
         wrapLogic.addManager( "Alpha" );
         wrapLogic.addPlayer( "bob" );
         game= wrapLogic.getGame();
     }
     
     @Test( expected= NullPointerException.class )
     public void testConstructor_Null() throws Exception{
         Logic g= new Logic( null, Dictionary.MIN_WORDLENGTH );
     }
     
     /**
      * Tests the constructor to ensure it fails when there are no teams.
      */
     @Test( expected= Logic.NoTeamsException.class )
     public void testConstructor_NoTeams() throws Exception{
         Logic g= new Logic( new LinkedList< Manager >(), Dictionary.MIN_WORDLENGTH );
     }
     
     /**
      * Tests the constructor to ensure it fails when there are no players.
      */
     @Test( expected= Logic.EmptyTeamsException.class )
     public void testConstructor_NoPlayers() throws Exception{
         List< Manager > teams= new LinkedList< Manager >();
         teams.add( new Manager( "Alpha" ) );
         Logic g= new Logic( teams, Dictionary.MIN_WORDLENGTH );
     }
     
     /**
      * Tests the constructor to ensure it fails when there are empty teams.
      */
     @Test( expected= Logic.EmptyTeamsException.class )
     public void testConstructor_NoPlayers2() throws Exception{
         List< Manager > teams= new LinkedList< Manager >();
         Manager man= new Manager( "Alpha" );
         man.addPlayer( "bob" );
         teams.add( new Manager( "Bravo" ) );
         Logic g= new Logic( teams, Dictionary.MIN_WORDLENGTH );
     }
     
     /**
      * Tests the constructor to ensure it fails when the wordLength is smaller than the dictionary limit.
      */
     @Test( expected= IllegalArgumentException.class )
     public void testConstructor_WordTooSmall() throws Exception{
         wrapLogic.getGame( Dictionary.MIN_WORDLENGTH - 1 );
     }
     
     /**
      * Tests the constructor to ensure it fails when the wordLength is larger than the dictionary limit.
      */
     @Test( expected= IllegalArgumentException.class )
     public void testConstructor_WordTooLarge() throws Exception{
         wrapLogic.getGame( Dictionary.LARGEST_WORD + 1 );
     }
     
     /**
      * Tests the constructor to ensure it fails when the gameWord is null.
      */
     @Test( expected= NullPointerException.class )
     public void testConstructor_NullGameWord() throws Exception{
         wrapLogic.getGame( null );
     }
     
     /**
      * Tests the constructor to ensure it fails when the gameWord is empty.
      */
     @Test( expected= IllegalArgumentException.class )
     public void testConstructor_EmptyGameWord() throws Exception{
         wrapLogic.getGame( "" );
     }
     
     /**
      * GetStatusWord should return all underscores before the game starts.
      */
     /* @Test
     public void testGetStatusWord_BeforeGame() {
         for( int i= Dictionary.MIN_WORDLENGTH; i <= Dictionary.MAX_WORDLENGTH; ++i ) {
             Logic g= new Logic( teams, i );
             String word= g.getWordStatus();
             for( int k= 0; k <  word.length(); ++k )
                 assertEquals( '_', word.charAt( k ) );
         }
     }*/
     
     /**
      * Test makeGuess with a bad argument. Error expected.
      */
     @Test( expected= IllegalArgumentException.class )
     public void testMakeGuess_wDigit()throws Exception {
         game.makeGuess( '9' );
     }
     
     /**
      * Test makeGuess. Should not throw an error.
      */
     @Test
     public void testMakeGuess() throws Exception {
         Logic newGame= wrapLogic.getGame();
         newGame.makeGuess( 'a' );
     }
     
     /**
      * Test makeGuess. Should throw an error.
      */
     @Test( expected= Logic.PlayerOutOfTurnException.class )
     public void testMakeGuess_NoRotateTurn() throws Exception {
         Logic newGame= wrapLogic.getGame();
         newGame.makeGuess( 'a' );
         newGame.makeGuess( 'b' );
     }
     
     /**
      * Test makeGuess. Should not throw an error.
      */
     @Test
     public void testMakeGuess_RotateTurn() throws Exception {
         Logic newGame= wrapLogic.getGame();
         newGame.makeGuess( 'a' );
         newGame.rotateTurn();
         newGame.makeGuess( 'b' );
     }
     
     /**
      * Tests makeGuess to make sure good guesses return true.
      */
     @Test
     public void testMakeGuess_GoodGuess() throws Exception{
         {
             Logic newGame= wrapLogic.getGame( "logic" );
             assertTrue( newGame.makeGuess( 'l' ) );
         }
         
         {
             Logic newGame= wrapLogic.getGame( "logic" );
             assertTrue( newGame.makeGuess( 'g' ) );
         }
         
         {
             Logic newGame= wrapLogic.getGame( "logic" );
             assertTrue( newGame.makeGuess( 'c' ) );
         }
     }
     
     /**
      * Tests makeGuess to make sure good guesses return true. Capital letters shouldn't matter.
      */
     @Test
     public void testMakeGuess_GoodGuess_Capital() throws Exception{
         {
             Logic newGame= wrapLogic.getGame( "logic" );
             assertTrue( newGame.makeGuess( 'L' ) );
         }
         
         {
             Logic newGame= wrapLogic.getGame( "logic" );
             assertTrue( newGame.makeGuess( 'G' ) );
         }
         
         {
             Logic newGame= wrapLogic.getGame( "logic" );
             assertTrue( newGame.makeGuess( 'C' ) );
         }
     }
     
     /**
      * Tests makeGuess to make sure bad guesses return false.
      */
     @Test
     public void testMakeGuess_BadGuess() throws Exception{
         Logic newGame= wrapLogic.getGame( "logic" );
         assertFalse( newGame.makeGuess( 'a' ) );
     }
     
     /**
      * Test makeGuess with a bad argument. Error expected.
      */
     @Test( expected= IllegalArgumentException.class )
     public void testMakeGuess_String_wLength()throws Exception {
         game.makeGuess( "op" );
     }
     
     /**
      * Test makeGuess with a bad argument. Error expected.
      */
     @Test( expected= NullPointerException.class )
     public void testMakeGuess_String_wNull()throws Exception {
         game.makeGuess( null );
     }
     
     /**
      * Test makeGuess with a bad argument. Error expected.
      */
     @Test( expected= Logic.PlayerOutOfTurnException.class )
     public void testMakeGuess_String_NoRotateTurn()throws Exception {
         game.makeGuess( "cat" );
         game.makeGuess( "mouse" );
     }
     
     /**
     * Test makeGuess.
      */
    @Test
     public void testMakeGuess_String_RotateTurn()throws Exception {
         game.makeGuess( "cat" );
         game.rotateTurn();
        game.makeGuess( "rat" );
     }
     
     /**
      * Test makeGuess with a bad argument. Error expected.
      */
    @Test( expected= IllegalArgumentException.class )
     public void testMakeGuess_String_Nonletters()throws Exception {
         game.makeGuess( "ca$t" );
     }
     
     /**
     * Test makeGuess with a bad argument. Error expected.
     */
    @Test( expected= IllegalArgumentException.class )
    public void testMakeGuess_String_TooShort()throws Exception {
        game.makeGuess( "sound" );
    }
    
    /**
      * Test UI event setter to make sure this doesn't throw an error.
      */
     @Test
     public void testSetGameEventsHandler() {
         game.setGameEventsHandler( new GameEventsBase() );
     }
     
     /**
      * Test UI event setter to make sure null does not throw an error.
      */
     @Test
     public void testSetGameEventsHandler_null() {
         game.setGameEventsHandler( null );
     }
     
     /**
      * Test UI event setter to make sure null does not throw an error.
      */
     @Test
     public void testGetAttempts()throws Exception {
         Logic newGame= wrapLogic.getGame( "Logic" );
         assertEquals( 0, newGame.getAttempts() );
         newGame.makeGuess( 'l' );
         assertEquals( 1, newGame.getAttempts() );
     }
     
     @Test( expected= IllegalArgumentException.class )
     public void testSetMaxAttempts() throws Exception {
         Logic newGame= wrapLogic.getGame( "Logic" );
         newGame.setMaxAttempts( Logic.MIN_ATTEMPTS - 1 );
     }
     
     @Test
     public void testRotateTurn() throws Exception {
         Logic newGame= wrapLogic.getGame( "Logic" );
         newGame.rotateTurn();
     }
     
     @Test
     public void testGetGameState() throws Exception {
         Logic newGame= wrapLogic.getGame( "Logic" );
         assertEquals( Logic.Statis.STARTED, newGame.getGameState() );
     }
     
     @Test
     public void testGetGuesses() throws Exception {
         Logic newGame= wrapLogic.getGame( "Logic" );
         assertEquals( 0, newGame.getGuesses().length );
         newGame.makeGuess( 'j' );
         assertEquals( 1, newGame.getGuesses().length );
     }
 }
