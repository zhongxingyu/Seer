 package sts.game;
 
 import java.util.Set;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import sts.Local;
 import sts.Main;
 
 /**
  *
  * @author Phillip Cohen
  */
 public class Game
 {
     private final static int LEVEL_WIDTH = 1000,  LEVEL_HEIGHT = 600;
 
     private ConcurrentLinkedQueue<Player> players;
 
     private Player nature = new Player( Color.white, "Nature" );
 
     private RunnerThread runnerThread = new RunnerThread();
 
     private static Game instance;
 
     private int villagersSoldToSlavery = 0;
 
     public static Game getInstance()
     {
         return instance;
     }
 
     public Game( Set<Player> p )
     {
         instance = this;
         this.players = new ConcurrentLinkedQueue<Player>( p );
         prepareLevel();
         runnerThread.startLoop();
     }
 
     public void act()
     {
         for ( Player p : players )
         {
 
             p.act();
 
             for ( GameObject go : p.getOwnedObjects() )
             {
                 if ( go instanceof HQ && go.getHealth() <= 0 )
                     lose( p );
             }
         }
     }
 
     public void draw( Graphics2D g )
     {
         for ( Player p : players )
             p.draw( g );
         nature.draw( g );
     }
 
     /**
      * Returns a set of objects whose bounding box contains the point.
      */
     public Set<GameObject> getObjectsWithinArea( int x, int y, int width, int height )
     {
         boolean hasLocalPlayer = false;
         Set<GameObject> objects = new HashSet<GameObject>();
         for ( Player p : players )
         {
             for ( GameObject go : p.getOwnedObjects() )
             {
                 if ( go.isClickContained( x, y, width, height ) )
                 {
                     // If this belongs to us, mark it...
                     if ( p == Local.getLocalPlayer() )
                         hasLocalPlayer = true;
                     // ...and skip all other players.
                     else if ( hasLocalPlayer )
                         continue;
 
                     objects.add( go );
                 }
             }
         }
 
         // Remove other players' units if any of ours are selected.
         if ( hasLocalPlayer )
         {
             for ( Iterator<GameObject> itr = objects.iterator(); itr.hasNext();)
             {
                 GameObject go = itr.next();
                 if ( go.getOwningPlayer() != Local.getLocalPlayer() )
                     itr.remove();
             }
         }
 
         // Add nature units if nothing else.
         if ( objects.isEmpty() )
         {
             for ( GameObject go : nature.getOwnedObjects() )
             {
                 if ( go.isClickContained( x, y, width, height ) )
                     objects.add( go );
             }
         }
 
         return objects;
     }
 
     public ConcurrentLinkedQueue<Player> getPlayers()
     {
         return players;
     }
 
     void lose( Player loser )
     {
         System.out.println( loser.getName() + " loses!" );
         for ( GameObject go : loser.getOwnedObjects() )
         {
             loser.removeObject( go );
         }
         for ( Player p : players )
         {
             if ( p == loser )
             {
                 players.remove( p );
             }
         }
     }
 
     /**
      * Sets up the level for the players, including the placement of their HQs and resources.
      * @see http://www.assembla.com/spaces/Schramts/tickets/5
      */
     private void prepareLevel()
     {
         int numPlayers = players.size();
         int currentPlayer = 0;
         for ( Player p : players )
         {
             double angle = Math.PI * 3 / 4 + 2 * Math.PI * currentPlayer / numPlayers;//The angle from the center to the player
 
             int x = (int) ( Game.LEVEL_WIDTH / 2 + Game.LEVEL_WIDTH * Math.cos( angle ) / 3 );
             int y = (int) ( Game.LEVEL_HEIGHT / 2 + Game.LEVEL_HEIGHT * Math.sin( angle ) / 3 );
             currentPlayer++;
             addStuffForPlayer( p, x, y );
         }
     }
 
     /**
      * Sets up a player with a HQ, 3 villagers, and a gold patch
      * @param p The player to set up
      * @param x The x coord of the HQ
      * @param y The y coord of the HQ
      */
     private void addStuffForPlayer( Player p, int x, int y )
     {
         p.giveObject( new HQ( x, y, p, true ) );
         p.giveObject( new Villager( x + 70, y, p ) );
         p.giveObject( new Villager( x - 70, y, p ) );
         p.giveObject( new Villager( x, y + 70, p ) );
         p.giveObject( new Infantry( x + 70, y + 70, 0, 0, p ) );
 
 
         addGoldPatch( x, y );
     }
 
     private void addGoldPatch( int x, int y )
     {
         double angle = Math.random() * 2 * Math.PI;
         x += (int) ( 120 * Math.cos( angle ) );
         y += (int) ( 120 * Math.sin( angle ) );
         for ( int xCoord = x - 20; xCoord <= x + 60; xCoord += 9 )
         {
             for ( int yCoord = y - 20; yCoord <= y + 60; yCoord += 9 )
             {
                 nature.giveObject( new GoldPile( xCoord, yCoord, nature ) );
             }
         }
     }
 
     public int getSlaveryGold()
     {
        return (int) Math.max( 5, -0.1 * Math.pow( villagersSoldToSlavery, 2 ) + 3 * villagersSoldToSlavery + 100 );
     }
 
     public int sellIntoSlavery( Villager v )
     {
         int gold = getSlaveryGold();
         villagersSoldToSlavery++;
 
         v.getOwningPlayer().removeObject( v );
 
         return gold;
     }
 
     public int getLevelWidth()
     {
         return LEVEL_WIDTH;
     }
 
     public int getLevelHeight()
     {
         return LEVEL_HEIGHT;
     }
 
     public Player getNature()
     {
         return nature;
     }
 
     /**
      * The thread that runs the game.
      */
     private class RunnerThread extends Thread
     {
         /**
          * The time in milliseconds between each action loop.
          */
         private final static int PERIOD = 50;
 
         /**
          * Whether the thread is enabled. Always true unless the thread is being destroyed.
          */
         private boolean enabled = true;
 
         public RunnerThread()
         {
             super( "Game runner thread" );
             setPriority( Thread.MAX_PRIORITY );
         }
 
         /**
          * Starts/resumes the game loop.
          */
         public void startLoop()
         {
             enabled = true;
             start();
         }
 
         /**
          * Stops the game loop and thread nicely. Note: the current timestep will be completed first.
          */
         public void stopLoop()
         {
             enabled = false;
         }
 
         /**
          * Starts an infinite loop which acts the game, sleeps, and repeats.
          * 
          * The amount of time to sleep is set by <code>period></code>.
          * If the game is running behind, it uses this sleep time as a cushion.
          */
         @Override
         public void run()
         {
             long timeOfLast = System.currentTimeMillis();
             while ( enabled )
             {
                 try
                 {
                     timeOfLast = System.currentTimeMillis();
 
                     if ( shouldRun() )
                         act();
 
                     while ( enabled && System.currentTimeMillis() - timeOfLast < PERIOD )
                         Thread.sleep( 2 );
 
                 }
                 catch ( InterruptedException ex )
                 {
                     Main.fatalError( "Game loop interrupted while sleeping.", ex );
                 }
             }
         }
 
         /**
          * Returns whether the game should run in the next step or not.
          */
         public boolean shouldRun()
         {
             return enabled;
         }
 
         public boolean isRunning()
         {
             return enabled;
         }
     }
 }
