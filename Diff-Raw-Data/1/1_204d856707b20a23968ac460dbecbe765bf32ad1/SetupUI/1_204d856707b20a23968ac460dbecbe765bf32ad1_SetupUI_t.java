 import java.util.List;
 import java.util.LinkedList;
 import java.util.Scanner;
 
 /**
  * Provides the saffolding to get the game up and running. Prompts 
  *  the user for the word length and max attempts. Creates a new Logic 
  *  and GameUI instance when the user is ready to play.
  *  
  * The API will be generic so that this game can be run from the console
  *  or with a swing pane.
  * 
  * @author Josh Gillham
  * @version 9-23-12
  */
 public class SetupUI extends SetupBase {
     /** Holds the default dictionary file. Should in the root of the project folder. */
     static public final String DICTIONARY_FILE= "smalldictionary.txt";
     /**
      * Initializes the dictionary. Creates a new instance of SetupUI.
      * 
      * @arg args command line arguments - not used.
      */
     static public void main( String[] args ){
         SetupUI setup= null;
         try{
             setup= new SetupUI();
         } catch( java.io.FileNotFoundException e ) {
             e.printStackTrace();
             System.exit( 1 );
         }
         setup.inputSetupGame();
         Logic game= setup.getGame( );
         
         setup.startGame( game );
         while( game.getGameState() == Logic.Statis.STARTED ){
             game.rotateTurn();
         }
         
     }
     /** Holds a copy of the player name. */
     private String name= null;
     /** Holds the word length. */
     private int wordLength= 0;
     /** Holds the maximum guesses. */
     private int maxAttempts= 0;
     
     
     /**
      * Brings up the user interface.
      * 
      * @throws FileNotFoundException when dictionary could not be loaded.
      */
     public SetupUI( ) throws java.io.FileNotFoundException {
         super.addManager( "Default" );
     }
     
     public Logic getGame() {
         Logic game= super.getGame( wordLength );
         game.setMaxAttempts( maxAttempts );
         return game;
     }
     
     /**
      * Launches the Game UI.
      * 
      * @return the newly create GameUI.
      */
     public GameUI startGame( Logic game ) {
         GameUI UI= new GameUI( game );
         game.setGameEventsHandler(UI);
         return UI;
     }
     
     /**
      * Walk through the setup steps with the user.
      * 
      * @return a new game logic.
      */
     public void inputSetupGame() {
         Scanner userInput= new Scanner(System.in);
         // Get their name
         int tries= 0;
         String name= null;
         while( name == null && tries++ < 3 )
             name= inputPlayerName( userInput );
         // The player doesn't want to play?
         if( name == null )
             System.exit( 1 );
         
         // Add him to the first team.
         super.addPlayer( name );
         
         // Get the word length
         tries= 0;
         while( wordLength == 0 && tries++ < 3 )
             wordLength= inputGameWordLength( userInput );
         
         // Get the maximum allowed guesses.
         tries= 0;
         while( maxAttempts == 0 && tries++ < 3 )
             maxAttempts= inputMaxAttempts( userInput );
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
      */
     public int inputGameWordLength( Scanner inputScanner ) {
         System.out.println( "Whats the word length:" );
         int wordLength= inputScanner.nextInt();
         
         if( !Dictionary.checkWordLength( wordLength ) ) {
             System.out.println( "Please enter a length between " + Dictionary.MIN_WORDLENGTH + " and " + Dictionary.LARGEST_WORD + "." );
             return 0;
         }
         return wordLength;
     }
     
     /**
      * Listens for the game word length.
      * 
      * @arg inputScanner gets the next input.
      * 
      * @return the word length.
      */
     public int inputMaxAttempts( Scanner inputScanner ) {
         System.out.println( "Whats is the max tries to win the game?" );
         int attempts= inputScanner.nextInt();
         return attempts;
     }
 }
