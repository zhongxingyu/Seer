 /*
  * DISASTEROIDS
  * AsteroidManger.java
  */
 
 import java.awt.Graphics;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 /**
  * Manger of the level's asteroids.
  * @author Andy Kooiman
  */
 public class AsteroidManager implements Serializable
 {
     /**
      * A thread-ready list of all <code>Asteroid</code>s.
      * @since Classic
      */
     private ConcurrentLinkedQueue<Asteroid> theAsteroids;
    
    /**
     * The next id number for a new <code>Asteroid</code> created
     * @since January 10, 2008
     */
    private int nextId;
 
     /**
      * Constructs a new <code>AsteroidManager</code>.
      * 
      * @since Classic
      */
     public AsteroidManager()
     {
         theAsteroids = new ConcurrentLinkedQueue<Asteroid>();
     }
 
     /**
      * Creates a level's <code>Asteroid</code>s.
      * 
      * @param level the level which this asteroid field should reflect
      * @since Classic
      */
     public void setUpAsteroidField( int level )
     {
 
         Random rand = RandNumGen.getAsteroidInstance();
         int numBonuses = 0;
         
         // Create regular asteroids.
         for ( int numAsteroids = 0; numAsteroids < ( level + 1 ) * 2; numAsteroids++ )
         {
             theAsteroids.add( new Asteroid( rand.nextInt( Game.getInstance().GAME_WIDTH ),
                                             rand.nextInt( Game.getInstance().GAME_HEIGHT ),
                                             rand.nextDouble() * 6 - 3,
                                             rand.nextDouble() * 6 - 3,
                                             rand.nextInt( 150 ) + 25,
                                             rand.nextInt( level * 10 + 10 ) - 9 ) );
             if ( rand.nextInt( 10 ) == 1 )
                 numBonuses++;
         }
         
         // Create bonus asteroids.
         for ( int numAsteroids = 0; numAsteroids < numBonuses; numAsteroids++ )
         {
             theAsteroids.add( new BonusAsteroid( rand.nextInt( Game.getInstance().GAME_WIDTH ),
                                                  rand.nextInt( Game.getInstance().GAME_HEIGHT ),
                                                  rand.nextDouble() * 6 - 3,
                                                  rand.nextDouble() * 6 - 3,
                                                  rand.nextInt( 150 ) + 25,
                                                  rand.nextInt( level * 10 + 10 ) - 9 ) );
 
         }
     }
 
     /**
      * Instructs all asteroids to act.
      * 
      * @since Classic
      */
     public void act()
     {
         for( Asteroid a : theAsteroids)
                 a.act();
     }
 
     /**
      * Draws all of the asteroids to the given content.
      * 
      * @param g
      * @since Classic
      */
     public void draw( Graphics g )
     {
         for ( Asteroid a : theAsteroids )
             a.draw( g );
     }
 
     /**
      * Adds an <code>Asteroid</code> to the game.
      * 
      * @param a    the <code>Asteroid</code> to be added.
      * @since Classic
      */
     public void add( Asteroid a )
     {
         theAsteroids.add( a );
     }
 
     /**
      * Removes the first asteroid with a given id.
      * 
      * @param id    the id of the asteroid
      * @since January 8, 2007
      */
     public void remove( int id )
     {
         Iterator<Asteroid> itr = theAsteroids.iterator();
         while ( itr.hasNext() )
         {
             Asteroid a = itr.next();
             if ( a.id == id )
             {
                 itr.remove();
                 return;
             }
         }
     }
 
     /**
      * Generates a unique id not being used by any asteroid.
      * 
      * @return  the id
      * @since January 8, 2007
      */
     public int getId()
     {
        return nextId++;
     }
 
     /**
      * Returns the number of <code>Asteroid</code>s on the level.
      * 
      * @return  the number of <code>Asteroid</code>s.
      * @since Classic
      */
     public int size()
     {
         return theAsteroids.size();
     }
 
     /**
      * Removes all current and pending <code>Asteroid</code>s.
      * @author Andy Kooiman
      * @since Classic
      */
     public void clear()
     {
         theAsteroids = new ConcurrentLinkedQueue<Asteroid>();
     }
 
     @Override
     public String toString()
     {
         String returnString = getClass().getName() + "@" + Integer.toHexString( hashCode() ) + "\n[";
         for ( Asteroid a : theAsteroids )
             returnString += a.toString() + "\n";
         returnString += "]";
         return returnString;
     }
 
     /**
      * Writes <code>this</code> to a stream for client/server transmission.
      * 
      * @param stream     the stream to write to (sent to the client)
      * @throws java.io.IOException 
      * @since December 29, 2007
      */
     public void flatten( DataOutputStream stream ) throws IOException
     {
         // Write asteroid count.
         stream.writeInt( theAsteroids.size() );
 
         // Write asteroids.
         for ( Asteroid a : theAsteroids )
             a.flatten( stream );
     }
 
     /**
      * Creates <code>this</code> from a stream for client/server transmission.
      * 
      * @param stream    the stream to read from (sent by the server)
      * @throws java.io.IOException 
      * @since December 29, 2007
      */
     public AsteroidManager( DataInputStream stream ) throws IOException
     {
         this.theAsteroids = new ConcurrentLinkedQueue<Asteroid>();
         int size = stream.readInt();
         for ( int i = 0; i < size; i++ )
             theAsteroids.add( new Asteroid( stream ) );
     }
 }
