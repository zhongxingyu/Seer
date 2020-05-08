 package oopj13stu2;
 
 import java.util.ArrayList;
 
 import oopj13stu2.*;
 
 public class NmGame {
 
     int playerTurnsElapsed;
     int sticksRemaining;
     ArrayList<Player> players;
 
     public NmGame(int sticksToStartWith) {
 
         playerTurnsElapsed = 0;
         sticksRemaining = sticksToStartWith;
 
         // Create Player objects and populate players list
         players = new ArrayList<Player>();
 
         players.add( new BotPlayer( "Computer 1") );
         //players.add( new BotPlayer( "Computer 2") );
         players.add( new HumanPlayer( "kgm") );

     }
 
     private void printGreeting() {
         System.out.println("Welcome to Nm, this game is played with " + sticksRemaining + " matches.");
        // TODO: move printing player # and name here, remove from Player constructor.
     }
 
     public void play() {
 
         // print welcome message, identify players
         printGreeting();
 
         // play the game until there's only one stick left to pick, if so the player in turn loses
         while( sticksRemaining > 1 ) {
 
            Player phasingPlayer = players.get( playerTurnsElapsed % players.size() );
 
            int drawnSticks = phasingPlayer.strategy( sticksRemaining );
            System.out.println( phasingPlayer.getPlayerName() + " removes " + drawnSticks + " matches." );
 
            sticksRemaining -= drawnSticks;
            System.out.println( "Remaining matches: " + sticksRemaining );
 
            playerTurnsElapsed += 1;
         }
 
         // game end
         System.out.println("Game over after " + playerTurnsElapsed + " turns, " +
                 players.get(playerTurnsElapsed % players.size()).getPlayerName() + " loses, " +
                 players.get((playerTurnsElapsed + 1) % players.size()).getPlayerName() + " wins.");
     }
 
     // autorun
     public static void main (String[] arg) {
 
         int sticksFromArguments = 10;
 
         // get # of sticks from command line argument
         try {
             sticksFromArguments = Integer.parseInt( arg[0] );
         }
        catch (ArrayIndexOutOfBoundsException e)            {
             // default sticksFromArguments used
         }
 
        oopj13stu2.NmGame nm = new oopj13stu2.NmGame( sticksFromArguments );
 
         nm.play();
 
 
     }
 }
