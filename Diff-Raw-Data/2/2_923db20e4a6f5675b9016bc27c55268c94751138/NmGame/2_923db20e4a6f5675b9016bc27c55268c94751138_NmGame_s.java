 /*
     Inlämningsuppgift 2 Objektorienterad programmering med Java sommaren 2013
     Kristian Grossman-Madsen
     https://github.com/kilskrift/oopj13stu2.git
 
     See report.txt for a guide to the code, and README for build instructions.
     You can reach me at 0707-793346 or kgm@kgm.cc if you need to. Jag talar svenska :)
  */
 
 package cc.kgm.oopj13st.u2;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
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
 
         Iterator<Player> p = players.iterator();
         int i = 1;
        while( p.hasNext() ) System.out.println("Player " + i + ": " + p.next().getPlayerName() );
     }
 
     public void play() {
 
         // print welcome message, identify players
         printGreeting();
 
         // play the game until there's only one stick left to pick, if so the player in turn loses
         while( sticksRemaining > 1 ) {
 
            Player phasingPlayer = players.get( playerTurnsElapsed % players.size() ); // 0..n-1, if n players
 
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
         //catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {  // only line of java 7 used
         catch (ArrayIndexOutOfBoundsException e ) {
             // default sticksFromArguments used
         }
         catch (NumberFormatException e) {  // java 6
             // default sticksFromArguments used
         }
 
         NmGame nm = new NmGame( sticksFromArguments );
 
         // start game
         nm.play();
     }
 }
