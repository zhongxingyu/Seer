 /*
  * DISASTEROIDS
  * Asteroid.java
  */
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Random;
 
 /**
  * A game object which the players destroy to score.
  * @author Andy Kooiman, Phillip Cohen
  */
 public class Asteroid extends GameObject implements GameElement, Serializable
 {
     /**
      * The number of child we've spawned.
      * @since Classic
      */
     protected int children;
 
     /**
      * Our diameter.
      * @since Classic
      */
     protected int radius;
 
     /**
      * Our border and fill colors.
      * @since December 15 2007
      */
     protected Color fill = Color.white,  outline = Color.gray;
 
     /**
      * Our life and its scale. Life is deducted when we're shot.
      * @since December 21, 2007
      */
     protected int lifeMax,  life;
 
     /**
      * Our unique id. Used in the manager.
      * @since January 8, 2007
      */
     int id;
 
     /**
      * Constructs a new Asteroid from scratch.
      * 
      * @param x				the x coordinate
      * @param y				the y coordinate
      * @param dx			the x velocity
      * @param dy			the y velocity (up is negative)
      * @param size			the diameter
      * @param lifeMax                   total amount of life
      * @since Classic
      */
     public Asteroid( int x, int y, double dx, double dy, int size, int lifeMax )
     {
         Random rand = RandNumGen.getAsteroidInstance();
         setLocation( x, y );
         setSpeed( dx, dy );
         this.radius = size / 2;
         this.life = this.lifeMax = Math.max( 1, lifeMax );
 
         // Enforce a minimum size.
         if ( size < 25 )
             size = 25 + rand.nextInt( 25 );
 
         // Enforce a mininum speed.
         checkMovement();
 
         id = Game.getInstance().asteroidManager.getId();
     }
 
     /**
      * Constructs a new <code>Asteroid</code> from a parent <code>Asteroid</code>.
      * This is used when a missile splits an <code>Asteroid</code>.
      * 
      * @param parent	the parent <code>Asteroid</code> to split from
      * @since Classic
      */
     public Asteroid( Asteroid parent )
     {
         parent.children++;
         if ( parent.children > 2 )
             this.radius = 5;
         else
             this.radius = parent.radius / 2;
         Random rand = RandNumGen.getAsteroidInstance();
         setLocation( parent.getX(), parent.getY() );
         setSpeed( rand.nextDouble() * 2 - 1, rand.nextDouble() * 2 - 1 );
 
         // Live half as long as parents.
         this.life = this.lifeMax = parent.lifeMax / 2 + 1;
 
         // Enforce a mininum speed.
         checkMovement();
 
         id = Game.getInstance().asteroidManager.getId();
     }
 
     /**
      * Draws <code>this</code>.
      * 
      * @param g 
      * @since Classic
      */
     public void draw( Graphics g )
     {
         lifeMax = Math.max( lifeMax, 1 );
         Color f = new Color( fill.getRed() * life / lifeMax, fill.getGreen() * life / lifeMax, fill.getBlue() * life / lifeMax );
         AsteroidsFrame.frame().drawOutlinedCircle( g, f, outline, (int) getX(), (int) getY(), radius );
     }
 
     /**
      * Steps <code>this</code> through one timestep, then draws it.
      * 
      * @since Classic
      */
     public void act()
     {
         // Asteroids are removed when split.
         if ( children > 1 || radius == 5 )
             destroy();
 
         if ( life <= 0 )
             destroy();
 
         move();
         checkCollision();
     }
 
     /**
      * Called when the <code>Asteroid</code> is killed, as an indication to split into two new <code>Asteroid</code>s.
      * 
      * @param killer The <code>Ship</code> which killed <code>this</code>.
      * @since Classic
      */
     protected void split( Ship killer )
     {
         if ( children > 2 )
         {
             destroy();
             return;
         }
 
         if ( killer != null )
         {
             killer.increaseScore( radius * 2 );
             killer.setNumAsteroidsKilled( killer.getNumAsteroidsKilled() + 1 );
         }
         if ( AsteroidsFrame.frame() != null && killer != null )
             AsteroidsFrame.frame().writeOnBackground( "+" + String.valueOf( radius * 2 ), (int) getX(), (int) getY(), killer.getColor().darker() );
 
         if ( radius < 12 )
             destroy();
         else
         {
             Game.getInstance().asteroidManager.add( new Asteroid( this ) ,true );
             Game.getInstance().asteroidManager.add( new Asteroid( this ) ,true );
             destroy();
         }
     }
 
     /**
      * Removes us from the manager and hence the game.
      * 
      * @since January 8, 2007
      */
     void destroy()
     {
         Game.getInstance().asteroidManager.remove( this.id ,true );
     }
 
     /**
      * Checks, and acts, if we were hit by a missile or ship.
      * 
      * @since Classic
      */
     private void checkCollision()
     {
         // Go through all of the ships.        
         for ( Ship s : Game.getInstance().players )
         {
             // Were we hit by the ship's body?
             if ( s.livesLeft() >= 0 )
             {
                 if ( Math.pow( getX() - s.getX(), 2 ) + ( Math.pow( getY() - s.getY(), 2 ) ) < ( radius + Ship.RADIUS ) * ( radius + Ship.RADIUS ) )
                 {
                     if ( s.looseLife() )
                     {
                         split( s );
                         return;
                     }
                 }
             }
         }
 
         // Go through ships, stations, etc.
         for ( ShootingObject s : Game.getInstance().shootingObjects )
         {
             for ( WeaponManager wm : s.getManagers() )
             {
                 // Loop through all this ship's Missiles.
                 for ( Weapon m : wm.getWeapons() )
                 {
                     // Were we hit by a missile?
                     if ( Math.pow( getX() - m.getX(), 2 ) + Math.pow( getY() - m.getY(), 2 ) < Math.pow( radius + m.getRadius(), 2 ) )
                     {
                         Sound.bloomph();
                         m.explode();
                         life = Math.max( 0, life - m.getDamage() );
                         if ( life <= 0 )
                         {
                             if ( s instanceof Ship )
                                 split( (Ship) s );
                             else
                                 split( null );
                             return;
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Makes sure that we're moving fast enough.
      * 
      * @since Classic
      */
     private void checkMovement()
     {
         if ( Math.abs( getDx() ) < .5 )
         {
             if ( getDx() < 0 )
                 setDx( getDx() - 1 );
             else
                 setDx( getDx() - 1 );
         }
 
         if ( Math.abs( getDy() ) < .5 )
         {
             if ( getDy() < 0 )
                 setDx( getDy() - 1 );
             else
                 setDx( getDy() - 1 );
         }
 
     }
 
     @Override
     public String toString()
     {
         return "[Asteroid@ (" + getX() + "," + getY() + "), radius " + radius + "]";
     }
 
     /**
      * Writes <code>this</code> to a stream for client/server transmission.
      * 
      * @param stream the stream to write to
      * @throws java.io.IOException 
      * @since December 29, 2007
      */
     public void flatten( DataOutputStream stream ) throws IOException
     {
         stream.writeDouble( getX() );
         stream.writeDouble( getY() );
         stream.writeDouble( getDx() );
         stream.writeDouble( getDy() );
         stream.writeInt( radius );
         stream.writeInt( life );
         stream.writeInt( lifeMax );
         stream.writeInt( children );
     }
 
     /**
      * Creates <code>this</code> from a stream for client/server transmission.
      * 
      * @param stream    the stream to read from (sent by the server)
      * @throws java.io.IOException 
      * @since December 29, 2007
      */
     public Asteroid( DataInputStream stream ) throws IOException
     {
         setLocation( stream.readDouble(), stream.readDouble() );
         setSpeed( stream.readDouble(), stream.readDouble() );
         radius = stream.readInt();
         life = stream.readInt();
         lifeMax = Math.max( 1, stream.readInt() );
         children = stream.readInt();
     }
 }
