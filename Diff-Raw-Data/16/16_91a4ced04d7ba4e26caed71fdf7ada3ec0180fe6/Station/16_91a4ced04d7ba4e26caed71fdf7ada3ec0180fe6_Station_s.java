 /*
  * DISASTEROIDS
  * Station.java
  */
 package disasteroids;
 
 import disasteroids.sound.Sound;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.util.Calendar;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 /**
  * A satellite that shoots missiles at passing ships.
  * @author Phillip Cohen, Andy Kooiman
  * @since January 6, 2008
  */
 public class Station extends GameObject implements ShootingObject
 {
     /**
      * The angle we're facing.
      * @since January 6, 2008
      */
     private double angle;
 
     /**
      * Our firing manager.
      * @since January 6, 2008
      */
     private MissileManager manager;
 
     /**
      * Width/height of the station.
      * @since January 6, 2008
      */
     int size = 35;
 
     /**
      * Timer to show the clock easter egg. If 0, the egg should be hidden.
      * @since January 9, 2008 
      */
     private int easterEggCounter = 0;
 
     /**
      * Creates the station at the given position and random floating speed.
      * 
      * @param x     x coordinate in game
      * @param y     y coordinate in game
      * @since January 6, 2008
      */
     public Station( double x, double y )
     {
         setLocation( x, y );
         setDx( Math.random() * 2 - 1 );
         setDy( Math.random() * 2 - 1 );
         angle = 0;
         manager = new MissileManager();
         manager.setPopQuantity( 0 );
         manager.setLife( 50 );
     }
 
     /**
      * Moves, acquires a target, and shoots.
      * 
      * @since January 6, 2008
      */
     public void act()
     {
         move();
         checkCollision();
         manager.act( true );
 
         // Easter egg.
         if ( easterEggCounter > 0 )
         {
             easterEggCounter--;
             return;
         }
 
         // Find players within our range.        
         int range = 300;
         Ship closestShip = null;
         for ( Ship s : Game.getInstance().players )
         {
             if ( getProximity( s ) < range )
             {
                 if ( closestShip == null || getProximity( s ) > getProximity( closestShip ) )
                     closestShip = s;
             }
         }
 
         // Aim towards closest ship.
         if ( closestShip != null )
         {
            angle = Math.atan( ( closestShip.getY() - getY() ) / (double) ( closestShip.getX() - getX() ) );
             if ( closestShip.getX() - getX() < 0 )
                 angle += Math.PI;
 
             // Fire!
             if ( manager.add( (int) ( centerX() + 25 * Math.cos( 0 - angle ) ), (int) ( centerY() - 25 * Math.sin( 0 - angle ) ), 0 - angle, 0, 0, Color.white, false ) )
                 Sound.playInternal( Sound.STATION_SHOOT_SOUND );  // Play a custom sound.
         }
         else
             angle += 0.01;
     }
 
     /**
      * Checks if players run into us and takes action.
      * 
      * @since January 9, 2007
      */
     private void checkCollision()
     {
         // Go through all of the ships.        
         for ( Ship s : Game.getInstance().players )
         {
             // Were we hit by the ship's body?
             if ( s.livesLeft() >= 0 )
             {
                 if ( ( s.getX() + Ship.RADIUS > getX() && s.getX() - Ship.RADIUS < getX() + size ) &&
                         ( s.getY() + Ship.RADIUS > getY() && s.getY() - Ship.RADIUS < getY() + size ) )
                 {
                     if ( s.looseLife() )
                     {
                         return;
                     }
                 }
             }
         }
     }
 
     /**
      * Returns the distance to a given ship using pythagoras.
      * 
      * @param s     the ship
      * @return      the distance to it
      * @since January 6, 2008
      */
     private double getProximity( Ship s )
     {
         return Math.sqrt( Math.pow( getX() - s.getX(), 2 ) + Math.pow( getY() - s.getY(), 2 ) );
     }
 
     /**
      * Draws this and our bullets to the given context. Uses RelativeGraphics.
      * 
      * @param g the context
      * @since January 6, 2008
      */
     public void draw( Graphics g )
     {
         int rX = RelativeGraphics.translateX( getX() );
         int rY = RelativeGraphics.translateY( getY() );
 
         int cX = RelativeGraphics.translateX( centerX() );
         int cY = RelativeGraphics.translateY( centerY() );
 
         // Draw the base.
         g.setColor( Color.darkGray );
         g.fillRect( rX, rY, size, size );
         g.setColor( new Color( 20, 20, 20 ) );
         g.drawRect( rX, rY, size, size );
 
         // Draw the corners.
         g.setColor( new Color( 20, 20, 20 ) );
         g.fillRect( rX - 2, rY - 2, 10, 10 );
         g.fillRect( rX + 27, rY, 10, 10 );
         g.fillRect( rX + 27, rY + 27, 10, 10 );
         g.fillRect( rX, rY + 27, 10, 10 );
         g.setFont( new Font( "Tahoma", Font.PLAIN, 8 ) );
 
         // Draw the easter egg clock.
         if ( easterEggCounter > 0 )
         {
             // Numbers.
             g.setColor( Color.white );
             g.drawString( "12", cX - 3, rY + 10 );
             g.drawString( "3", rX + size - 7, cY + 5 );
             g.drawString( "6", cX - 1, rY + size - 4 );
             g.drawString( "9", rX + 4, cY + 5 );
 
             // Hour hand.
             double hourAngle = ( ( Calendar.getInstance().get( Calendar.HOUR ) / 12.0 ) * 2 - 0.5 ) * Math.PI;
             int eX = (int) ( cX + 5 * Math.cos( hourAngle ) );
             int eY = (int) ( cY + 5 * Math.sin( hourAngle ) );
             g.drawLine( cX, cY, eX, eY );
             g.drawLine( cX, cY + 1, eX, eY + 1 );
             g.drawLine( cX + 1, cY, eX + 1, eY );
 
             // Minute hand.
             double minuteAngle = ( ( Calendar.getInstance().get( Calendar.MINUTE ) / 60.0 ) * 2 - 0.5 ) * Math.PI;
             eX = (int) ( cX + 10 * Math.cos( minuteAngle ) );
             eY = (int) ( cY + 10 * Math.sin( minuteAngle ) );
             g.drawLine( cX, cY, eX, eY );
             g.drawLine( cX, cY + 1, eX, eY + 1 );
 
             // Second hand.
             double secondAngle = ( ( Calendar.getInstance().get( Calendar.SECOND ) / 60.0 ) * 2 - 0.5 ) * Math.PI;
             eX = (int) ( cX + 15 * Math.cos( secondAngle ) );
             eY = (int) ( cY + 15 * Math.sin( secondAngle ) );
             g.drawLine( cX, cY, eX, eY );
         }
         else
         {
             // Draw the turret.
             g.setColor( Color.white );
             int eX = (int) ( cX + 15 * Math.cos( angle ) );
             int eY = (int) ( cY + 15 * Math.sin( angle ) );
             g.drawLine( cX, cY, eX, eY );
             g.drawLine( cX, cY + 1, eX, eY + 1 );
             g.drawLine( cX + 1, cY, eX + 1, eY );
         }
         manager.draw( g );
     }
 
     /**
      * Returns the center position of the station.
      * 
      * @return      the x coordinate of the center
      * @since January 6, 2008
      */
     int centerX()
     {
         return (int) ( getX() + size / 2 );
     }
 
     /**
      * Returns the center position of the station.
      * 
      * @return      the y coordinate of the center
      * @since January 6, 2008
      */
     int centerY()
     {
         return (int) ( getY() + size / 2 );
     }
 
     /**
      * Returns a linked queue containing our one weapon manager. Used for ShootingObject.
      * 
      * @return  thread-safe queue containg our <code>MissileManager</code>
      * @since January 6, 2008
      */
     public ConcurrentLinkedQueue<WeaponManager> getManagers()
     {
         ConcurrentLinkedQueue<WeaponManager> c = new ConcurrentLinkedQueue<WeaponManager>();
         c.add( manager );
         return c;
     }
 
     /**
      * Shows the easter egg for a short while.
      * @since January 9, 2008
      */
     public void setEasterEgg()
     {
         if ( easterEggCounter <= 0 )
             easterEggCounter = 290;
     }
 }
