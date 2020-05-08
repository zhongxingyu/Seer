 /*
  * DISASTEROIDS
  * Missile.java
  */
 package disasteroids.weapons;
 
 import disasteroids.*;
 import disasteroids.gui.MainWindow;
 import disasteroids.gui.ParticleManager;
 import disasteroids.gui.Particle;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.Random;
 
 /**
  * A more elaborate bullet that sets off chain reactions.
  * @author Andy Kooiman
  * @since Classic
  */
 public class Missile extends Unit
 {
     /**
      * The angle that we're pointing. Not necessarily the angle that we're moving at.
      * @since Classic
      */
     protected double angle;
 
     /**
      * The current stage of explosion. If 0, we're not exploding.
      * @since Classic
      */
     protected int explosionStage = 0;
 
     /**
      * The current radius of <code>this</code>.
      * @since Classic
      */
     protected double radius = 3;
 
     /**
      * The missile launcher that fired us.
      * @since Classic
      */
     protected MissileManager parent;
 
     /**
      * What "generation" this missile is.  Missiles shot from the ship are generation 0, wile those formed
      * through splitting are higher generations.
      * @since May 31, 2009
      */
     protected int generation;
 
     /**
      * Whether <code>this</code> will have a huge blast when it explodes.
      * @since Classic
      */
     protected boolean hugeBlast;
 
     public Missile( MissileManager parent, Color color, double x, double y, double dx, double dy, double angle, int generation )
     {
         super( color, x, y, dx, dy );
         this.parent = parent;
         this.angle = angle;
         this.generation = generation;
         hugeBlast = ( Util.getGameplayRandomGenerator().nextInt( parent.getBonusValue( parent.BONUS_HUGEBLASTPROB ).getValue() ) <= 1 );
     }
 
     /**
      * Draws the missile and any of its explosions.
      * 
      * @param g
      * @since Classic
      */
     public void draw( Graphics g )
     {
         // Draw the body.
         MainWindow.frame().drawLine( g, color, (int) getX(), (int) getY(), 10, angle + Math.PI );
         MainWindow.frame().fillCircle( g, color, (int) getX(), (int) getY(), (int) radius );
 
         // Draw the explosion.
         Color col = color;
         switch ( explosionStage )
         {
             case 1:
             case 2:
             case 3:
             case 4:
                 if ( explosionStage % 2 != 0 )
                     col = Color.yellow;
                 MainWindow.frame().fillCircle( g, col, (int) getX(), (int) getY(), (int) radius );
                 break;
             case 5:
             case 6:
             case 7:
             case 8:
                 if ( explosionStage % 2 != 0 )
                     col = Color.yellow;
                 radius = 5;
                 MainWindow.frame().fillCircle( g, col, (int) getX(), (int) getY(), (int) radius );
                 break;
             case 9:
             case 10:
             case 11:
                 if ( hugeBlast )
                 {
                     radius = parent.getBonusValue( parent.BONUS_HUGEBLASTSIZE ).getValue();
                     MainWindow.frame().fillCircle( g, col, (int) getX(), (int) getY(), (int) radius );
                 }
                 else
                 {
                     radius = 14;
                     col = Color.yellow;
                     MainWindow.frame().fillCircle( g, col, (int) getX(), (int) getY(), (int) radius );
                     this.explosionStage++;
                 }
                 break;
         }
     }
 
     /**
      * Moves, then slows down.
      * 
      * @since Classic
      */
     @Override
     public void move()
     {
         super.move();
         setDx( ( getDx() + parent.speed() * Math.cos( angle ) / 50 ) * .98 );
         setDy( ( getDy() - parent.speed() * Math.sin( angle ) / 50 ) * .98 );
     }
 
     /**
      * Steps <code>this</code> through one iteration.
      * 
      * @author Andy Kooiman
      * @since Classic
      */
     @Override
     public void act()
     {
         super.act();
 
         // Create particles when launched. 
         if ( age < 30 )
         {
             Random rand = Util.getGameplayRandomGenerator();
             for ( int i = 0; i < (int) ( 7 - Math.sqrt( getDx() * getDx() + getDy() * getDy() ) ); i++ )
                 ParticleManager.addParticle( new Particle(
                         getX() + rand.nextInt( 8 ) - 4,
                         getY() + rand.nextInt( 8 ) - 4,
                         rand.nextInt( 4 ),
                         color,
                         rand.nextDouble() * 3,
                         angle + rand.nextDouble() * .4 - .2 + Math.PI,
                         30, 10 ) );
         }
         // Explode when old.
         if ( age > parent.life() && explosionStage == 0 )
             explode();
 
         // Move through the explosion sequence.
         if ( explosionStage > 0 )
         {
             this.explosionStage++;
             switch ( explosionStage )
             {
                 case 0:
                     return;
                 case 1:
                 case 2:
                 case 3:
                 case 4:
                     setDx( getDx() * .8 );
                     setDy( getDy() * .8 );
                     radius = 3;
                     break;
                 case 5:
                 case 6:
                 case 7:
                 case 8:
                     setDx( getDx() * .8 );
                     setDy( getDy() * .8 );
                     break;
                 case 9:
                 case 10:
                 case 11:
                     setDx( getDx() * .8 );
                     setDy( getDy() * .8 );
                     break;
                 default:
                     parent.remove( this );
             }
         }
     }
 
     /**
      * Starts an elaborate explosion sequence. Also potentially pops the missile into several clones.
      * 
      * @since Classic
      */
     public void explode()
     {
         // Already exploding.
         if ( isExploding() )
             return;
 
         // Optionally pop into several other missiles.
         if ( generation < parent.getMaxGenerations() && Util.getGameplayRandomGenerator().nextInt( parent.getBonusValue( parent.BONUS_POPPINGPROB ).getValue() ) <= 101 )
             parent.pop( this );
 
         explosionStage = 1;
     }
 
     /**
      * Returns the damage this <code>Unit</code> will do.
      * 
      * @return The damage done by this <code>Unit</code>
      */
     public int getDamage()
     {
         if ( isExploding() )
             return 10;
         else
             return 40;
     }
 
     /**
      * Returns the radius of the damage area. This is the missile's body plus the explosion, if any.
      */
     public double getRadius()
     {
         return radius;
     }
 
     /**
      * Returns whether we've started to explode.
      */
     public boolean isExploding()
     {
         return explosionStage > 0;
     }
 
     @Override
     public void remove()
     {
         parent.remove( this );
     }
 
     /**
      * Getter for the generation field
      * @return <code>this</code> Missile's generation.
      */
     public int getGeneration()
     {
         return generation;
     }
 
     //                                                                            \\
     // ------------------------------ NETWORKING -------------------------------- \\
     //                                                                            \\
     /**
      * Writes <code>this</code> to a stream for client/server transmission.
      */
     @Override
     public void flatten( DataOutputStream stream ) throws IOException
     {
         super.flatten( stream );
         stream.writeDouble( angle );
         stream.writeInt( explosionStage );
         stream.writeBoolean( hugeBlast );
         stream.writeDouble( radius );
         stream.writeInt( generation );
     }
 
     /**
      * Reads <code>this</code> from a stream for client/server transmission.
      */
     public Missile( DataInputStream stream, MissileManager parent ) throws IOException
     {
         super( stream );
         angle = stream.readDouble();
         explosionStage = stream.readInt();
         hugeBlast = stream.readBoolean();
         radius = stream.readDouble();
         generation = stream.readInt();
 
         this.parent = parent;
     }
 }
