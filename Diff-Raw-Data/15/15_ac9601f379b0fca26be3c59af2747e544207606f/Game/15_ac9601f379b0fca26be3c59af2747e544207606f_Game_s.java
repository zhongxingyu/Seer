 package oopj13stu2;
 
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
 import java.util.ArrayList;
 
 import oopj13stu2.*;
 
 public class Game {
 
     int playerTurnsElapsed;
     int sticksRemaining;
     ArrayList<Player> players;
 
     public Game( int sticksToStartWith ) {
 
         playerTurnsElapsed = 0;
 
         // Läs in antal stickor från första argumentet
         sticksRemaining = sticksToStartWith;
 
         // Create Player objects and populate players list
         players = new ArrayList<Player>();
 
         players.add( new BotPlayer( "Computer 1") );
       //  players.add( new BotPlayer( "Computer 2") );
         players.add( new HumanPlayer( "kgm") );
 
     }
 
    private void greeting() {
         System.out.println("Welcome to Nm, this game is played with " + sticksRemaining + " matches.");
         // TODO: move printing player # and name here, remove from Player constructor.
     }
 
     public void play() {
 
        greeting();
 
         // play the game until there's only one stick left to pick, if so the player in turn loses
         while( sticksRemaining > 1 ) {
 
            Player phasingPlayer = players.get( playerTurnsElapsed % players.size() );
 
            int drawnSticks = phasingPlayer.strategy( sticksRemaining );
            System.out.println( phasingPlayer.getPlayerName() + " removes " + drawnSticks + " matches." );
 
            sticksRemaining -= drawnSticks;
            System.out.println( "Remaining matches: " + sticksRemaining );
 
            playerTurnsElapsed += 1;
 
         }
 
        System.out.println("Game over, turn " + playerTurnsElapsed + " sticks left " + sticksRemaining); //DEBUG
 
     }
 
     // autorun
     public static void main (String[] arg) {
 
         Game nm = new Game( Integer.parseInt( arg[0] ) ); //TODO: error handling if no args given
 
         nm.play();
 
 
     }
 }
