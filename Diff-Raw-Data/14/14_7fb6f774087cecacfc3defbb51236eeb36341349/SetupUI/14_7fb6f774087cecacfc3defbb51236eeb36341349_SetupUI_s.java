 import java.util.List;
 import java.util.LinkedList;
 import java.util.Scanner;
 
 /**
  * Provides the saffolding to get the game up and running. Prompts 
  *  the user for the word length and max attempts. Creates a new Logic 
  *  and GameUI instance when the user is ready to play.
  * 
  * @author Josh Gillham
  * @version 9-23-12
  */
 public class SetupUI extends SetupBase {
     /** Holds the default dictionary file. Should in the root of the project folder. */
     static public final String DICTIONARY_FILE= "smalldictionary.txt";
     
     /** Holds the word length. */
     private int wordLength= 5;
     /** Holds the maximum guesses. */
     private int maxAttempts= 10;
     
     /**
      * Initializes the dictionary. Creates a new instance of SetupUI.
      * 
      * @arg args command line arguments - not used.
      */
     static public void main( String[] args ){
         SetupUI setup= null;
         boolean playing= true;
         int count= 0;
         do {
             ++count;
             if( count > 1 ) {
                 System.out.println( "Game #" + count );
             }
             setup= new SetupUI();
             Logic game= setup.inputSetupGame( count > 1 );
             
             setup.startGame( game );
             while( game.getGameState() == Logic.Statis.STARTED ){
                 game.rotateTurn();
             }
             playing= setup.inputPlayAgain();
         } while( playing );
     }
     
     /**
      * Runs the default setup commands.
      */
     public SetupUI( ) {
         super.addManager( "Default" );
         // Add him to the first team.
         super.addPlayer( "Default" );
     }
     
     /**
      * Launches the Game UI.
      * 
      * @arg game a reference to the game logic.
      * 
      * @return the newly create GameUI.
      */
     public GameUI startGame( Logic game ) {
         GameUI UI= new GameUI( game );
         game.setGameEventsHandler(UI);
         return UI;
     }
     
     /**
      * Ask the player if they would like to play again.
      */
     public boolean inputPlayAgain() {
         try {
             Scanner userInput= new Scanner(System.in);
             System.out.println( "Good effort! Would you like to play again (y = yes)?" );
             String response= userInput.next();
             if( response.equalsIgnoreCase( "y" ) ) {
                 return true;
             }
         } catch( Exception e ) {}
         return false;
     }
     
     /**
      * Walk through the setup steps with the user. Sets private fields as
      *  the user does input.
      */
     public Logic inputSetupGame( boolean evilMode ) {
         Scanner userInput= new Scanner(System.in);
         int tries= 0;
         
         // Get the word length
         tries= 0;
         int responseWordLength= 0;
         while( responseWordLength == 0 && tries++ < 3 )
             responseWordLength= inputGameWordLength( userInput );
         
         if( responseWordLength != 0 )
             this.wordLength= responseWordLength;
         
         // Get the maximum allowed guesses.
         tries= 0;
         int responseMaxAttempts= 0;
         while( responseMaxAttempts == 0 && tries++ < 3 )
             responseMaxAttempts= inputMaxAttempts( userInput );
             
         if( responseMaxAttempts != 0 )
             this.maxAttempts= responseMaxAttempts;
         
         try {
             Logic ret;
             if( evilMode )
                 ret= new EvilLogic( super.getTeams(), wordLength );
             else
                 ret= new Logic( super.getTeams(), wordLength );
             ret.setMaxAttempts( this.maxAttempts );
             return ret;
         }
         // This should never happen
         catch( Exception e ) {
             e.printStackTrace();
         }
         return null;
     }
     
     /**
      * Listens for the player's name and throws out bad input.
      * 
      * @arg inputScanner gets the next input
      * 
      * @return the name 
      * @return null if the input was bad.
      */
     public String inputPlayerName( Scanner inputScanner ) {
         System.out.println( "Enter your name:" );
         try{
             String name= inputScanner.next();
             if( name.isEmpty() )
                 return null;
             return name;
         }catch( Exception e) {
             return null;
         }
     }
     
     /**
      * Listens for the game word length.
      * 
      * @arg inputScanner gets the next input.
      * 
      * @return the word length.
      * @return 0 if the input was bad.
      */
     public int inputGameWordLength( Scanner inputScanner ) {
         System.out.println( "Whats the word length:" );
         try{
             int wordLength= inputScanner.nextInt();
             
             if( !Dictionary.getInstance().checkWordLength( wordLength ) ) {
                 System.out.println( "We are sorry but the dictionary does not have words with that length." );
                 return 0;
             }
             return wordLength;
         }catch( java.util.NoSuchElementException e ) {
             return 0;
         }
     }
     
     /**
      * Listens for the game word length.
      * 
      * @arg inputScanner gets the next input.
      * 
      * @return the word length.
      * @return 0 for errors and illegal input.
      */
     public int inputMaxAttempts( Scanner inputScanner ) {
         System.out.println( "Whats is the max tries to win the game?" );
         try {
        int attempts= inputScanner.nextInt();
        if( attempts < Logic.MIN_ATTEMPTS )
            return 0;
        else
            return attempts;
         } catch( java.util.NoSuchElementException e ) {
             return 0;
         }
     }
 }
