 /*
  * DISASTEROIDS
  * Asteroid.java
  */
 package disasteroids;
 
 import disasteroids.gui.AsteroidsFrame;
 import disasteroids.gui.ImageLibrary;
 import disasteroids.gui.Local;
 import disasteroids.sound.Sound;
 import disasteroids.sound.SoundLibrary;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 /**
  * A game object which the players remove to score.
  * @author Andy Kooiman, Phillip Cohen
  */
 public class Asteroid extends GameObject implements GameElement
 {
     /**
      * The number of children we've spawned.
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
      * The angle offset, for Graphics only
      */
     protected double angle;
 
     Ship killer;
 
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
     public Asteroid( double x, double y, double dx, double dy, int size, int lifeMax )
     {
         super( x, y, dx, dy );
         this.radius = size / 2;
         this.life = this.lifeMax = Math.max( 1, lifeMax );
         angle = 0;
         // Enforce a minimum size.
         if ( size < 25 )
             size = 25 + Util.getRandomGenerator().nextInt( 25 );
 
         // Enforce a mininum speed.
         checkMovement();
 
         id = Game.getInstance().asteroidManager.getId();
     }
 
     /**
      * Constructs a new <code>Asteroid</code> from a parent <code>Asteroid</code>.
      * This is used when a missile splits an <code>Asteroid</code>.
      * 
      * @param parent	the parent <code>Asteroid</code> to kill from
      * @since Classic
      */
     public Asteroid( Asteroid parent )
     {
         super( parent.getX(), parent.getY(), Util.getRandomGenerator().nextDouble() * 2 - 1, Util.getRandomGenerator().nextDouble() * 2 - 1 );
         parent.children++;
         angle = 0;
         if ( parent.children > 2 )
             this.radius = 5;
         else
             this.radius = parent.radius / 2;
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
         AsteroidsFrame.frame().drawImage( g, ImageLibrary.getAsteroid(), (int) getX(), (int) getY(), angle, radius * 2.0 / ImageLibrary.getAsteroid().getWidth( null ) );
     }
 
     /**
      * Steps <code>this</code> through one timestep.
      * 
      * @since Classic
      */
     public void act()
     {
         // Asteroids are removed when kill.
         if ( children > 1 || radius == 5 )
         {
             remove();
         }
 
         if ( life <= 0 )
         {
             remove();
         }
 
         move();
         checkCollision();
         if ( !Game.getInstance().isPaused() )
             angle += radius % 2 == 0 ? .05 : -.05;
     }
 
     /**
      * Kills us: splits and awards points.
      * 
      * @since Classic
      */
     protected void kill()
     {
         if ( children > 2 )
         {
             return;
         }
 
         if ( killer != null )
         {
             killer.increaseScore( radius * 2 );
             killer.setNumAsteroidsKilled( killer.getNumAsteroidsKilled() + 1 );
 
             // Write the score on the background.
             if ( AsteroidsFrame.frame() != null )
                 Local.getStarBackground().writeOnBackground( "+" + String.valueOf( radius * 2 ), (int) getX(), (int) getY(), killer.getColor().darker() );
         }
 
         if ( radius >= 12 )
         {
             Game.getInstance().asteroidManager.add( new Asteroid( this ), true );
             Game.getInstance().asteroidManager.add( new Asteroid( this ), true );
         }
     }
 
     /**
      * Removes us from the manager and hence the game.
      * 
      * @since January 8, 2007
      */
     void remove()
     {
         Game.getInstance().asteroidManager.remove( this.id, killer, true );
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
                if ( Util.getDistance( this, s ) <  radius + s.getRadius() )
                 {
                     if ( s.damage( radius / 2.0 + 8, s.getName() + ( Math.abs( getSpeed() ) > Math.abs( s.getSpeed() ) ? " was hit by" : " slammed into" ) + " an asteroid." ) )
                     {
                         killer = s;
                         kill();
                         remove();
                         return;
                     }
                 }
             }
         }
 
         // Go through ships, stations, etc.
         for ( ShootingObject s : Game.getInstance().shootingObjects )
         {
             for ( Weapon wm : s.getManagers() )
             {
                 // Loop through all this ship's Missiles.
                 for ( Weapon.Unit m : wm.getUnits() )
                 {
                     // Were we hit by a missile?
                     if ( Util.getDistance( this, m ) < Math.pow( radius + m.getRadius(), 2 ) )
                     {
                         Sound.playInternal( SoundLibrary.ASTEROID_DIE );
 
                         m.explode();
                         life = Math.max( 0, life - m.getDamage() );
                         if ( life <= 0 )
                         {
                             killer = null;
                             if ( s instanceof Ship )
                             {
                                 killer = ( (Ship) s );
                             }
                             kill();
                             remove();
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
             {
                 setDx( getDx() - 1 );
             }
             else
             {
                 setDx( getDx() - 1 );
             }
         }
 
         if ( Math.abs( getDy() ) < .5 )
         {
             if ( getDy() < 0 )
             {
                 setDx( getDy() - 1 );
             }
             else
             {
                 setDx( getDy() - 1 );
             }
         }
 
     }
 
     /**
      * Generates and returns a <code>String</code> representation of <code>this</code>
      * It will have the form "[Asteroid@(#,#), radius #]".
      * @return a <code>String</code> representation of <code>this</code>
      */
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
     @Override
     public void flatten( DataOutputStream stream ) throws IOException
     {
         super.flatten( stream );
         stream.writeInt( id );
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
         super( stream );
         id = stream.readInt();
         radius = stream.readInt();
         life = stream.readInt();
         lifeMax = Math.max( 1, stream.readInt() );
         children = stream.readInt();
     }
 }
