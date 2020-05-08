 /*
  * DISASTEROIDS
  * Alien.java
  */
 package disasteroids;
 
 import disasteroids.gui.AsteroidsFrame;
 import disasteroids.gui.ImageLibrary;
 import disasteroids.gui.ParticleManager;
 import disasteroids.sound.Sound;
 import disasteroids.sound.SoundLibrary;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 /**
  * Little UFOs that fire at players.
  * @author Phillip Cohen
  * @since February 6, 2008
  */
 public class Alien extends GameObject implements ShootingObject
 {
     /**
      * Our firing manager.
      * @since January 6, 2008
      */
     protected MissileManager manager;
 
     /**
      * The color of this <code>Alien</code>
      */
     protected Color color;
 
     /**
      * The diameter(?!?) of this <code>Alien</code>
      */
     private int size;
 
     /**
      * The health of this <code>Alein</code>.  Starts at 100
      */
     private int life;
 
     /**
      * The current status in an explosion
      */
     private int explosionTime;
 
     /**
      * Acceleration vectors.
      * @since March 30, 2008
      */
     private double ax = 0,  ay = 0;
 
     /**
      * The direction this <code>Alien</code> is "pointing."
      */
     double angle = Util.getRandomGenerator().nextDouble() * 2 * Math.PI;
 
     /**
      * Creates a new <code>Alien</code>
      * 
      * @param x X coordinate
      * @param y Y coordinate
      * @param dx X velocity
      * @param dy Y velocity
      * @see GameObject#GameObject(double, double, double, double)
      */
     public Alien( double x, double y, double dx, double dy )
     {
         super( x, y, dx, dy );
         size = Util.getRandomGenerator().nextInt( 50 ) + 30;
         manager = new AlienMissileManager( size );
         color = new Color( Util.getRandomGenerator().nextInt( 60 ), Util.getRandomGenerator().nextInt( 128 ) + 96, Util.getRandomGenerator().nextInt( 60 ) );
         life = 500;
         explosionTime = 0;
     }
 
     /**
      * Iterates through the necessary actions of a single timestep
      */
     public void act()
     {
         generalActBehavior();
 
         ax *= 0.94;
         ay *= 0.94;
 
         if ( Math.abs( ax ) <= 0.01 || Util.getRandomGenerator().nextInt( 60 ) == 0 )
             ax = Util.getRandomGenerator().nextDouble() * 0.12 - 0.06;
         if ( Math.abs( ay ) <= 0.01 || Util.getRandomGenerator().nextInt( 60 ) == 0 )
             ay = Util.getRandomGenerator().nextDouble() * 0.12 - 0.06;
         if ( Util.getRandomGenerator().nextInt( 90 ) == 0 && ( Math.abs( ax ) == ax ) == ( Math.abs( getDx() ) == getDx() ) )
         {
             ax *= -1.8;
             ay *= -1.8;
         }
        
         // Find players within our range.        
         int range = 300;
         Ship closestShip = null;
         {
             Ship closestInvincible = null;
             for ( Ship s : Game.getInstance().players )
                 if ( getProximity( s ) < range )
                 {
                     if ( closestShip == null || getProximity( s ) > getProximity( closestShip ) )
                         closestShip = s;
                     if ( closestInvincible == null || getProximity( s ) > getProximity( closestInvincible ) )
                         closestInvincible = s;
                 }
             if ( closestShip == null && closestInvincible != null )
                 closestShip = closestInvincible;
         }
 
         // Aim towards closest ship.
         if ( closestShip != null )
         {
             double mAngle = calculateAngle( closestShip );
             manager.shoot( this, color, mAngle );
         }
 
         angle += size / 560.0 + 0.015;
     }
     /**
      * Executes generic behavior for the Alien class 
      * (workaround for the subclasses)
      */
     protected void generalActBehavior()
     {
         move();
         setSpeed( Math.min( 3, getDx() + ax ), Math.min( 3, getDy() + ay ) );
         
         checkCollision();
         manager.act();
 
         // Prepare to flash.
         if ( life <= 0 )
         {
             if ( explosionTime == 0 )
             {
                 explosionTime = 20;
 
                 if ( Util.getRandomGenerator().nextInt( 5 ) == 0 )
                     Game.getInstance().createBonus( this );
             }
 
             if ( explosionTime == 1 )
             {
                 Game.getInstance().removeObject( this );
                 Sound.playInternal( SoundLibrary.ALIEN_DIE );
             }
 
             explosionTime--;
         }
 
         // Smoke when low on health.
         if ( life < 80 )
             ParticleManager.createSmoke( getX() + Util.getRandomGenerator().nextInt( size ), centerY(), 1 );
 
         // Flames when doomed!
         if ( life < 40 )
             ParticleManager.createFlames( getX() + Util.getRandomGenerator().nextInt( size ), centerY(), ( 50 - life ) / 10 );
 
     }
 
     /**
      * Checks if any Weapons or players have collided, and updates health, 
      * explosions, and lives as necessary.
      */
     private void checkCollision()
     {
         if ( explosionTime > 0 )
             return;
 
         // Check for missile collision.
         for ( ShootingObject s : Game.getInstance().shootingObjects )
         {
             if ( s == this )
                 continue;
 
             // Loop through the mangers.
             for ( Weapon wm : s.getManagers() )
             {
                 // Loop through the bullets.
                 for ( Weapon.Unit m : wm.getUnits() )
                 {
                     // Were we hit by a bullet?
                     if ( ( m.getX() + m.getRadius() > getX() && m.getX() - m.getRadius() < getX() + size ) &&
                             ( m.getY() + m.getRadius() > getY() && m.getY() - m.getRadius() < getY() + size ) )
                     {
                         m.explode();
                         life -= m.getDamage();
                     }
                 }
             }
         }
 
         // Check for ship collision.  
         for ( Ship s : Game.getInstance().players )
         {
             // Were we hit by the ship's body?
             if ( s.livesLeft() >= 0 )
             {
                 if ( ( s.getX() + s.getRadius() > getX() && s.getX() - s.getRadius() < getX() + size ) &&
                         ( s.getY() + s.getRadius() > getY() && s.getY() - s.getRadius() < getY() + size ) )
                 {
                     if ( s.damage( 5, s.getName() + " was abducted." ) )
                         life -= 20;
                 }
             }
         }
     }
 
     /**
      * Calculates the desired angle at which to shoot to hit the target.
      * @param target The <code>Ship</code> to aim at
      * @return The angle to aim at to hit.
      */
     protected double calculateAngle( Ship target )
     {
         double desiredAngle = 0.0;
         double distance = getProximity( target );
         double time = Math.log( distance ) * ( 5 + Util.getRandomGenerator().nextInt( 2 ) );
         double projectedX = target.getX() + time * target.getDx();
         double projectedY = target.getY() + time * target.getDy();
 
         desiredAngle = Math.atan( ( projectedY - centerY() ) / -( projectedX - centerX() ) );
         if ( projectedX - ( centerX() ) < 0 )
             desiredAngle += Math.PI;
 
         return desiredAngle;
     }
 
     /**
      * The x coordinate of the center of <code>this</code>
      * @return The x coordinate of the center of <code>this</code>
      */
     double centerX()
     {
         return getX() + 8;
     }
 
     /**
      * The y coordinate of the center of <code>this</code>
      * @return The y coordinate of the center of <code>this</code>
      */
     double centerY()
     {
         return getY() + 3;
     }
 
     /**
      * Returns the distance to a given ship using Pythagoras.
      * 
      * @param s     the ship
      * @return      the distance to it
      * @since January 6, 2008
      */
     protected double getProximity( Ship s )
     {
         return Math.sqrt( Math.pow( getX() - s.getX(), 2 ) + Math.pow( getY() - s.getY(), 2 ) );
     }
 
     /**
      * Draws <code>this</code> in the given context, using an <code>Image</code>
      * @param g The context in which to draw.
      */
     public void draw( Graphics g )
     {
         manager.draw( g );
 
         if ( explosionTime > 0 )
         {
             AsteroidsFrame.frame().fillCircle( g, color.darker().darker(), (int) getX(), (int) getY(), (int) ( size * 0.1 * ( explosionTime - 1 ) ) );
             return;
         }
         AsteroidsFrame.frame().drawImage( g, ImageLibrary.getAlien(), (int) getX() + size / 2, (int) getY() + size / 3, angle, ( size * 1.6 ) / ImageLibrary.getAlien().getWidth( null ) );
     }
 
     /**
      * Writes <code>this</code> to a stream for client/server transmission.
      * 
      * @param stream the stream to write to
      * @throws java.io.IOException 
      * @since April 11, 2008
      */
     @Override
     public void flatten( DataOutputStream stream ) throws IOException
     {
         super.flatten( stream );
         stream.writeDouble( angle );
         stream.writeDouble( ax );
         stream.writeDouble( ay );
         stream.writeInt( color.getRGB() );
         stream.writeInt( explosionTime );
         stream.writeInt( life );
         stream.writeInt( size );
     }
 
     /**
      * Creates <code>this</code> from a stream for client/server transmission.
      * 
      * @param stream    the stream to read from (sent by the server)
      * @throws java.io.IOException 
      * @since April 11, 2008
      */
     public Alien( DataInputStream stream ) throws IOException
     {
         super( stream );
         angle = stream.readDouble();
         ax = stream.readDouble();
         ay = stream.readDouble();
         color = new Color( stream.readInt() );
         explosionTime = stream.readInt();
         life = stream.readInt();
         size = stream.readInt();
 
         // TODO [PC]: Sync!
         manager = new AlienMissileManager( size );
     }
 
     @Override
     public int getTypeId()
     {
         return TYPE_ID;
     }
 
     /**
      * Returns a linked queue containing our one weapon manager. Used for ShootingObject.
      * 
      * @return  thread-safe queue containing our <code>MissileManager</code>
      * @since January 6, 2008
      */
     public ConcurrentLinkedQueue<Weapon> getManagers()
     {
         ConcurrentLinkedQueue<Weapon> c = new ConcurrentLinkedQueue<Weapon>();
         c.add( manager );
         return c;
     }
 
     /**
      * The alien's version of a <code>MissileManager</code>, which shoots weird units.
      */
     private class AlienMissileManager extends MissileManager
     {
         /**
          * keeps track of the angle to draw the lines coming radially out of the unit
          */
         double finRotation = 0.0;
 
         /**
          * Creates a new <code>AlienMissileManager</code>
          * @param life How long the units last
          */
         public AlienMissileManager( int life )
         {
             setPopQuantity( 0 );
             setLife( (int) ( life * 1.2 ) );
         }
 
         @Override
         public void shoot( GameObject parent, Color color, double angle )
         {
             if ( !canShoot() )
                 return;
 
             // Shoot a missile, and, randomly, an alien bullet. 
             units.add( new Missile( this, color, parent.getX(), parent.getY(), parent.getDx() / 8, parent.getDy() / 8, angle ) );
             if ( Util.getRandomGenerator().nextBoolean() )
                 units.add( new AlienBullet( this, color, parent.getX(), parent.getY(), parent.getDx(), parent.getDy(), angle ) );
 
             if ( !isInfiniteAmmo() )
                 --ammo;
 
             timeTillNextShot = 12;
         }
 
         /**
          * The circular, spinning unit that does constant damage to whoever touches it.
          */
         private class AlienBullet extends Missile
         {
             public AlienBullet( AlienMissileManager parent, Color color, double x, double y, double dx, double dy, double angle )
             {
                 super( manager, color, x, y, dx, dy, angle );
                 radius = 1;
             }
 
             @Override
             public int getDamage()
             {
                 return 5;
             }
 
             @Override
             public void act()
             {
                 super.act();
                 radius = 5 + ( age - parent.life() * .2 ) * ( parent.life() - age ) * 0.05;
                 finRotation += ( 0.004 * Math.PI ) % Math.PI * 2;
             }
 
             /**
              * Updates position and speed as per current velocity.
              */
             @Override
             public void move()
             {
                 super.move();
                 setDx( ( getDx() + getDy() * -0.2 ) * 0.7 );
                 setDy( ( getDy() + getDx() * 0.2 ) * 0.7 );
             }
 
             @Override
             public void draw( Graphics g )
             {
                 AsteroidsFrame.frame().drawLine( g, color, (int) getX(), (int) getY(), (int) ( getRadius() * 1.7 ), Math.PI * finRotation );
                 AsteroidsFrame.frame().drawLine( g, color, (int) getX(), (int) getY(), (int) ( getRadius() * 1.7 ), Math.PI * ( finRotation + 0.6 ) );
                 AsteroidsFrame.frame().drawLine( g, color, (int) getX(), (int) getY(), (int) ( getRadius() * 1.7 ), Math.PI * ( finRotation + 1.2 ) );
                 AsteroidsFrame.frame().fillCircle( g, color, (int) getX(), (int) getY(), (int) getRadius() );
             }
         }
     }
 }
