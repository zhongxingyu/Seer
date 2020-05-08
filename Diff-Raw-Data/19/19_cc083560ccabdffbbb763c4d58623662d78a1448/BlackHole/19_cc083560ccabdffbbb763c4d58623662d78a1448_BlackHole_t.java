 /*
  * DISASTEROIDS
  * BlackHole.java
  */
 package disasteroids;
 
 import disasteroids.gui.MainWindow;
 import disasteroids.gui.ImageLibrary;
 import disasteroids.weapons.Unit;
 import disasteroids.weapons.Weapon;
 import java.awt.Graphics;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 /**
  * A stationary black mass that sucks in everything around it.
  * Once it has destroyed a certain number of objects, it removes itself.
  * @author Phillip Cohen
  */
 public class BlackHole extends GameObject
 {
     /**
      * The radius of our attraction circle, in pixels.
      */
    final static int ATTRACTION_RADIUS = 450;
 
     /**
      * The pulling power this black hole exerts, in pixels/second^2.
      */
     private int power;
 
     /**
      * How many objects we have left to destroy. Can be -1 for infinite.
      */
     private int numLeftToEat;
 
     public BlackHole( double x, double y )
     {
         super( x, y, Util.getRandomGenerator().nextInt( 9 ) + 8, 30 );
     }
 
     public BlackHole( double x, double y, int power, int numToEat )
     {
         super( x, y, 0, 0 );
         this.power = power;
         this.numLeftToEat = numToEat;
     }
 
     /**
      * Finds everything nearby - and SUUUCKS!
      */
     public void act()
     {
         // Find nearby objects.
         Set<GameObject> victims = getNearbyVictims();
 
         // Pull each one towards us.
         for ( GameObject go : victims )
         {
             double angle = Util.getAngle( this, go );
            
            double magnitude = Math.min( Math.pow( power, 1.7 ) / Math.pow( Util.getDistance( this, go ), 1.7 ), 1 );
             go.setVelocity( go.getDx() + magnitude * Math.cos( angle ), go.getDy() + magnitude * Math.sin( angle ) );
 
             // Too close to the center! Destroy him!
             if ( isPrey( go ) && Util.getDistance( this, go ) < 35 )
             {
                 go.inBlackHole();
                 if ( numLeftToEat > 0 )
                 {
                     // Remove after we've eaten enough, for gameplay reasons.
                     if ( --numLeftToEat == 0 )
                     {
                         Game.getInstance().getObjectManager().removeObject( this );
                         return;
                     }
                 }
             }
         }
     }
 
     /**
      * Returns a set of all nearby game objects.
      * @see BlackHole#ATTRACTION_RADIUS
      */
     private Set<GameObject> getNearbyVictims()
     {
         Set<GameObject> closeObjects = new HashSet<GameObject>();
 
         // Add game objects.
         for ( long id : Game.getInstance().getObjectManager().getAllIds() )
             if ( Util.getDistance( this, Game.getInstance().getObjectManager().getObject( id ) ) < ATTRACTION_RADIUS )
                 closeObjects.add( Game.getInstance().getObjectManager().getObject( id ) );
 
         // Add the units of weapons.
         for ( ShootingObject s : Game.getInstance().getObjectManager().getShootingObjects() )
             for ( Weapon w : s.getManagers() )
                 for ( Unit u : w.getUnits() )
                     if ( Util.getDistance( this, u ) < ATTRACTION_RADIUS )
                         closeObjects.add( u );
 
         // Remove anything that we shouldn't eat.
         for ( Iterator<GameObject> i = closeObjects.iterator(); i.hasNext(); )
             if ( !isPrey( i.next() ) )
                 i.remove();
 
         return closeObjects;
     }
 
     /**
      * Returns whether we should attract <code>victim</code>.
      * Normally true, except for things like invincible ships and other black holes.
      */
     private boolean isPrey( GameObject victim )
     {
         return !( victim instanceof Ship && ( (Ship) victim ).cannotDie() ||
                 victim instanceof BlackHole );
     }
 
     public void draw( Graphics g )
     {
         MainWindow.frame().drawImage( g, ImageLibrary.getBlackHole(), (int) getX(), (int) getY() );
     }
 
     /**
      * Writes <code>this</code> to a stream for client/server transmission.
      */
     @Override
     public void flatten( DataOutputStream stream ) throws IOException
     {
         super.flatten( stream );
         stream.writeInt( numLeftToEat );
         stream.writeInt( power );
     }
 
     /**
      * Creates <code>this</code> from a stream for client/server transmission.
      */
     public BlackHole( DataInputStream stream ) throws IOException
     {
         super( stream );
         numLeftToEat = stream.readInt();
         power = stream.readInt();
     }
 }
