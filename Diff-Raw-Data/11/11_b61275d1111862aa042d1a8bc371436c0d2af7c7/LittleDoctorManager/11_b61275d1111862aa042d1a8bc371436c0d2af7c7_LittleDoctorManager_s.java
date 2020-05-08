 /*
  * DISASTEROIDS
  * LittleDoctorManager.java
  */
 package disasteroids.weapons;
 
 import disasteroids.*;
 import disasteroids.sound.Sound;
 import disasteroids.sound.SoundLibrary;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 /**
  * The classic weapon that fires <code>Missiles</code>.
  * @author Andy Kooiman
  */
 public class LittleDoctorManager extends Weapon
 {
     /**
      * The current initial speed for <code>Missile</code>s in this manager.
      * @since Classic
      */
     private double speed = 5;
 
     /**
      * The number of timesteps for which the <code>Missile</code>s will live before self destructing.
      */
     private int life = 60, firstLife = 200;
 
     protected int maxShots = 1200;
 
     protected int maxGenerations = 3;
 
 
     public LittleDoctorManager( ShootingObject parent )
     {
         super( parent );
     }
 
     @Override
     protected void genericInit()
     {
         super.genericInit();
         //TODO: Bonuses
     }
 
     @Override
     public void drawOrphanUnit( Graphics g, double x, double y, Color col )
     {
         new LittleDoctor( this, col, x, y, 0, 0, 0, 0 ).draw( g );
     }
 
     @Override
     public void shoot( GameObject parent, Color color, double angle )
     {
         if ( !canShoot() )
             return;
 
         addUnit( new LittleDoctor( this, color, parent.getFiringOriginX(), parent.getFiringOriginY(), parent.getDx(), parent.getDy(), angle, 0 ) );
 
         if ( !isInfiniteAmmo() )
             --ammo;
 
         timeTillNextShot =50;
         Sound.playInternal( SoundLibrary.MISSILE_SHOOT );
     }
 
     public void shoot( Color color, double x, double y, double dx, double dy, double angle )
     {
     }
 
     @Override
     public void act()
     {
         super.act();
     }
 
     /**
      * Launches several clones of the given missile.
      */
     public void pop( LittleDoctor origin )
     {
         if ( units.size() >= maxShots )
             return;
 
         double angleOffset = Util.getGameplayRandomGenerator().nextAngle();
         for ( int i = 0; i < 30; i++ )
             addUnit( new LittleDoctor( this, origin.color, origin.getX(), origin.getY(), 0, 0, angleOffset + i * 2 * Math.PI/30, origin.getGeneration() + 1 ) );
     }
 
     @Override
     public void berserk( GameObject parent, Color color )
     {
         int firedShots = 0;
         for ( double angle = 0; angle <= 2 * Math.PI; angle += Math.PI / 10 )
         {
             if ( !canBerserk() )
                 break;
 
             addUnit( new LittleDoctor( this, color, parent.getFiringOriginX(), parent.getFiringOriginY(), parent.getDx(), parent.getDy(), angle, 0 ) );
 
             if ( !isInfiniteAmmo() )
                 --ammo;
 
             ++firedShots;
         }
 
         if ( firedShots > 0 )
         {
             timeTillNextBerserk = firedShots * 10;
             Sound.playInternal( SoundLibrary.BERSERK );
         }
     }
 
     /**
      * Explodes all missiles without popping any.
      */
     @Override
     public void explodeAllUnits()
     {
         for ( Unit w : units )
             w.remove(); 
     }
 
     public int getMaxUnits()
     {
         return maxShots;
     }
 
     public int life(int generation)
     {
         if(generation == 0 )
             return firstLife;
         return life/Math.max(1, generation / 3);
     }
 
     public void setLife( int life )
     {
         this.life = life;
     }
 
     public double speed()
     {
         return speed;
     }
 
     public String getName()
     {
         return "Lil' Doctor";
     }
 
     @Override
     public int getEntryAmmo()
     {
         return 30;
     }
 
     public int getMaxGenerations()
     {
         return maxGenerations;
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
         stream.writeInt( life );
         stream.writeInt( maxShots );
         stream.writeDouble( speed );
 
         // Flatten all of the units.

         stream.writeInt( units.size() );
         for ( Unit u : units )
            ( (Missile) u ).flatten( stream );
         
     }
 
     /**
      * Reads <code>this</code> from a stream for client/server transmission.
      */
     public LittleDoctorManager( DataInputStream stream, ShootingObject parent ) throws IOException
     {
         super( stream, parent );
         life = stream.readInt();
         maxShots = stream.readInt();
         speed = stream.readDouble();
 
         // Restore all of the units.
         int size = stream.readInt();
         for ( int i = 0; i < size; i++ )
             addUnit( new LittleDoctor( stream, this ) );
         
     }
 }
