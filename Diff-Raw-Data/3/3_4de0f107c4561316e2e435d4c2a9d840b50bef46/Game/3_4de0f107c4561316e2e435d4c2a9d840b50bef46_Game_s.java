 package combat;
 /** 
  * @author       DevB  
  * @version      $Id: Game.java,v 1.26 2012/04/08 03:50:18 DevA Exp $
  *
  * This is the Game class...
  *
  * Revision History:
  *   $Log: Game.java,v $
  *   Revision 1.26  2012/04/08 03:50:18  DevA
  *   Cleaned up the code to run with Java 1.6: removed unused imports,
  *   fixed some UI focus issues (introduced by new focus "features" in Java since
  *   our original implementation), and made the CommandInterpreter not a Singleton
  *
  *   Revision 1.25  2003/05/30 14:45:00  DevB
  *   Now has a scoreboard that holds the score instead of
  *   the game managing it.  It simply tells the score when
  *   and who to increment and when to reset.
  *
  *   Revision 1.24  2003/05/30 14:27:09  DevB
  *   Changed some formatting.
  *
  *   Revision 1.23  2000/05/12 04:10:22  DevA
  *    Barriers get cleaned up after a round now. (Using end methods.)
  *    Also fixed a bug I noticed with resetting scores.
  *
  *   Revision 1.22  2000/05/11 22:44:47  DevA
  *   No point issued if players kill each other at the same time.
  *
  *   Revision 1.21  2000/05/11 07:12:04  DevA
  *   Resetting scores to zero on new game and made outputs a bit cleaner.
  *
  *   Revision 1.20  2000/05/11 06:29:12  DevA
  *   Removed most debugs from the system and now everything works
  *   great on a New Game or New Round...at least as far as the user
  *   can tell.  I think that whichever Player won a round is left
  *   hanging a bit under the hood.  It never redraws, but it
  *   never really goes away.  Seems like a waste of processor time
  *   so I'm stil trying to figure this out.
  *
  *   Revision 1.19  2000/05/10 20:57:54  DevC
  *   added mehod to break down Games into rounds;
  *   added scorekeeping calls and calls for new rounds
  *   after one player wins a round
  *
  *   Revision 1.18  2000/05/09 18:17:04  DevB
  *   Added the removeAll call again.
  *
  *   Revision 1.17  2000/05/09 17:40:45  DevA
  *   Added point outputs.
  *
  *   Revision 1.16  2000/05/09 17:20:22  DevA
  *    Threading support for scoring in place in Game and
  *    PlayerManager.
  *
  *   Revision 1.15  2000/05/09 16:07:29  DevA
  *   Extended gameLength
  *
  *   Revision 1.14  2000/05/09 15:26:35  DevA
  *   Taking out debugs.
  *
  *   Revision 1.13  2000/05/09 14:57:56  DevA
  *   Sending a JPanel to LevelBuilder
  *
  *   Revision 1.11  2000/05/09 14:05:46  DevC
  *   tracking bugs
  *
  *   Revision 1.10  2000/05/09 07:28:16  DevA
  *   Just trying to figure out why painting isn't working right.
  *   No real progress.
  *
  *   Revision 1.9  2000/05/09 05:55:57  DevC
  *   removed the listener from the panel and placed it on the frame
  *   in Combat
  *
  *   Revision 1.8  2000/05/09 05:31:30  DevA
  *   Actually adding a KeyListener and have isFocusTraversable method
  *
  *   Revision 1.7  2000/05/09 05:00:16  DevA
  *   Put gameLength back to 2000 everywhere.
  *
  *   Revision 1.6  2000/05/09 04:11:18  DevA
  *   Pausing and resuming and starting a new game seem to work.
  *   Have things showing now (using jpgs instead of bmps) so shifting
  *   gears to test command interpretation and sprites.
  *
  *   Revision 1.5  2000/05/08 22:22:29  DevA
  *   Changed buildLevel back to setLevel.  Now storing the filename
  *   for the active level and parsing in newGame.  Also, newGame
  *   now starts the timer, but that's wrong.  Needs to go back
  *   into Combat eventually.  Just here for testing since
  *   we're not handling games ending yet anyway.
  *
  *   Revision 1.4  2000/05/08 20:04:34  DevB
  *   Changed some comments.
  *
  *   Revision 1.3  2000/05/08 04:18:13  DevB
  *   Conglomerated setLevel and parseLevel into buildLevel
  *   that takes a filename and sets the level to one parsed
  *   by a parser that DevA is writing.
  *
  *   Revision 1.2  2000/05/08 01:32:33  DevB
  *   Made Game inherit from the JPanel class... it will be
  *   responsible for holding the graphics object (since it is
  *   a panel, that will be its graphic object) that all the
  *   Sprites and board need to draw to.
  *
  *   Revision 1.1  2000/05/06 19:48:58  DevB
  *   Initial revision
  *
  */
 
 import java.util.*;
 import java.awt.*;
 import javax.swing.*;
 
 /**
  * Game is the panel on which the game will be played.  It has the graphics
  * object that all the Sprites and board will draw to.
  */
 
 public class Game extends JPanel implements Timed, Runnable {
 	private static final long serialVersionUID = -1;
 
     TimeManager timer;
     String levelFile;
     LevelBuilder level;
     int gameLength;
     boolean gameActive;
     Board theBoard;
     Thread thread;
     boolean combat;
     Scoreboard scores;
     CommandInterpreter ci;
 
     /**
      * Initializes the game time keeper.
      * @param	timer	The TimeManager for the game.
      */
     public Game( TimeManager timer, Scoreboard scoreboard, CommandInterpreter ci)
     {
         setFocusable(true);
         this.ci = ci;
         
         combat = true;
        level = null;
         this.timer = timer;
         timer.addTimed(this);
         levelFile = new String( "level1.lvl" );
         gameActive = false;
         scores = scoreboard;
         timer.start();
         pause();
     }
 
     /**
      * Makes the game active with the current level.
      */
     public void newGame()
     {
         gameLength = 200000;
         if( level != null )
         {
             level.endPlayer( 1 );
             level.endPlayer( 2 );
             level.cleanUp();
         }
         scores.resetScores();
         newRound();
     }
 
     /**
      * Continues play with new "lives" for each player.
      */
     public void newRound()
     {
         if( level != null ) level.cleanUp();
         pause();
         timer.removeAll();
         Rectangle tmp = getBounds();
         getGraphics().clearRect( tmp.x, tmp.y, tmp.width, tmp.height );
         repaint();
         level = new LevelBuilder( levelFile, this, ci );
         LinkedList objects = level.getTimed();
         ListIterator iterator = objects.listIterator(0);
         while( iterator.hasNext() )
         {
             timer.addTimed( (Timed)iterator.next() );
         }
         gameActive = true;
         resume();
     }
 
     /**
      * Sets the active level
      * @param  filename  The filename for the level being set.
      */
     public void setLevel( String filename )
     {
         System.err.println("You will have to select New Game to begin"
           +" on the new level.");
         gameActive = false;
         levelFile = filename;
     }
 
     /**
      * gets the score for the given player
      *
      * @param   int Player number to get score from.
      * @return  The score for that player.
      * @throws  IllegalArgumentException if it is not a valid player number.
      */
     public int getScore(int player)
     {
         return scores.getScoreForPlayer(player);
     }
 
     /**
      * Stops time in the game.
      */
     public void pause()
     {
         timer.pause();
     }
 
     /**
      * Starts time in the game.
      */
     public void resume()
     {
         timer.unpause();
     }
 
     /**
     * Executes all pretick actions
     */
     public void pretick() { }
 
     /**
     * Executes all tick actions
     */
     public void tick()
     {
         if( gameActive )
         {
             gameLength--;
             if( gameLength == 0 )
             {
                 gameActive = false;
                 timer.pause();
             }
         }
     }
     
     public PlayerManager getPlayer1()
     {
     	return this.level.player1;
     }
     
     public PlayerManager getPlayer2()
     {
     	return this.level.player2;
     }
 
     /**
      * runs the thread.  goes while the thread is active
      */
     public void run()
     {
         while( combat )
         {
             if( gameActive )
             {
             	//Get the player states
                 boolean play1 = level.playerAlive(1);
                 boolean play2 = level.playerAlive(2);
                 
                 //If a single player has won
                 if(play1 ^ play2){
                 	//Determine the player number
                 	int player = (play1 ? 1 : 2);
                 	
                 	scores.incrementScoreForPlayer(player);
                 	System.out.println("Player " + player + " wins this round.");
                 	
                 	gameActive = false;
                 	level.endPlayer(player);
                 	newRound();
                 	
                 }
                 //Both players killed each other.
                 else if(!play1 && !play2){
                     gameActive = false;
                     System.out.println("You killed each other.  No points.");
                     newRound();
                 }
                 
                 
                 
             }
             try
             {
                 Thread.sleep(1000);
             }
             catch( InterruptedException e ) {}
         }
     }
 
     /**
     * quit
     * ends this combat game
     */
     public void quit()
     {
         combat = false;
     }
 
     /**
     * Start
     * starts this thread
     */
     public void start()
     {
         thread = new Thread( this);
         thread.start();
     }
 }
