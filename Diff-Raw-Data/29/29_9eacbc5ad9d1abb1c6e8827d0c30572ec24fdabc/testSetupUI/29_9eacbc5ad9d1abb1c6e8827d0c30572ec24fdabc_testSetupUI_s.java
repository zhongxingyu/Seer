 import static org.junit.Assert.*;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * This test unit will test the behavior of SetupUI.
  *
  * TODO:
  *  Fully test SetupUI
  *  inputExitGame
  * 
  * @author  Josh Gillham
  * @version 9-23-12
  */
 public class testSetupUI{
     
     /**
      * Provides an input stream to pass to a Scanner class to simulate an input.
      */
     class BadSetupUser extends java.io.InputStream {
         String badInput;
         int index= 0;
         public BadSetupUser( String input ) {
             badInput= input;
         }
         
         public int read() {
             if( index >= badInput.length() )
                 return -1;
             return (int)badInput.charAt( index++ );
         }
         
     }
     SetupUI setup= null;
     
     /**
      * Tests the constructor to ensure that it throws no errors.
      */
     @Before
     public void testConstructor() throws java.io.FileNotFoundException {
         setup= new SetupUI();
     }
     
     /**
      * Tests inputPlayerName.
      */
     @Test
     public void testInputPlayerName_NoInput() {
         String result= setup.inputPlayerName( new java.util.Scanner( new BadSetupUser( "" ) ) );
         assertNull( result );
     }
     /**
      * Tests inputPlayerName.
      */
     @Test
     public void testInputPlayerName() {
         String result= setup.inputPlayerName( new java.util.Scanner( new BadSetupUser( "Test" ) ) );
         assertNotNull( result );
     }
     
     @Test
     public void testInputGameWordLength_NoInput() {
         int result= setup.inputGameWordLength( new java.util.Scanner( new BadSetupUser( "" ) ) );
         assertEquals( 0, result );
     }
     
     @Test
     public void testInputGameWordLength_TooSmall() {
        int result= setup.inputGameWordLength( new java.util.Scanner( new BadSetupUser( Integer.toString( 2 ) ) ) );
         assertEquals( 0, result );
     }
     
     @Test
     public void testInputGameWordLength_TooBig() {
         int result= setup.inputGameWordLength( new java.util.Scanner( new BadSetupUser( Integer.toString( 20 ) ) ) );
         assertEquals( 0, result );
     }
     
     @Test
     public void testInputGameWordLength() {
         int result= setup.inputGameWordLength( new java.util.Scanner( new BadSetupUser( Integer.toString( 5  ) ) ) );
         assertEquals( 5, result );
     }
     
     @Test
     public void testInputMaxAttempts_NoInput() {
         int result= setup.inputMaxAttempts( new java.util.Scanner( new BadSetupUser( "" ) ) );
         assertEquals( 0, result );
     }
     
     @Test
     public void testInputMaxAttempts_TooSmall() {
         int result= setup.inputMaxAttempts( new java.util.Scanner( new BadSetupUser( Integer.toString( 1  ) ) ) );
         assertEquals( 0, result );
     }
     
     @Test
     public void testInputMaxAttempts() {
         int result= setup.inputMaxAttempts( new java.util.Scanner( new BadSetupUser( Integer.toString( Logic.MIN_ATTEMPTS  ) ) ) );
         assertEquals( Logic.MIN_ATTEMPTS, result );
     }
     
     
     
     @Test
     public void testGetGame()throws Exception {
         SetupUI setup2= new SetupUI();
         setup2.addManager( "D" );
         setup2.addPlayer( "D" );
         Logic game= setup2.getGame();
     }
 }
