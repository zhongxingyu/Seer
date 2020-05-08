 /**
  * DISASTEROIDS
  * BulletManager.java
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
  * A default weapon that rapidly fires weak bullets.
  * @author Andy Kooiman
  */
 public class BulletManager extends Weapon
 {
     private int speed = 20;
 
     // Bonus IDs.
     public int BONUS_INTERVALSHOOT, BONUS_RADIUS, BONUS_DAMAGE, BONUS_THREEWAYSHOT;
 
     public BulletManager()
     {
        super();
     }
 
     @Override
     protected void genericInit()
     {
         super.genericInit();
         ammo = -1;
         BONUS_INTERVALSHOOT = getNewBonusID();
         BONUS_RADIUS = getNewBonusID();
         BONUS_DAMAGE = getNewBonusID();
         BONUS_THREEWAYSHOT = getNewBonusID();
         bonusValues.put( BONUS_INTERVALSHOOT, new BonusValue( 4, 1, "Rapid fire" ) );
         bonusValues.put( BONUS_RADIUS, new BonusValue( 2, 6, "Huge bullets" ) );
         bonusValues.put( BONUS_DAMAGE, new BonusValue( 10, 60, "More damaging bullets" ) );
         bonusValues.put( BONUS_THREEWAYSHOT, new BonusValue( 0, 1, "Three way shoot" ) );
     }
 
     @Override
     public void shoot( GameObject parent, Color color, double angle )
     {
         if ( !canShoot() )
             return;
 
         units.add( new Bullet( this, color, parent.getFiringOriginX(), parent.getFiringOriginY(), parent.getDx(), parent.getDy(), angle ) );
 
         // This bonus fires two extra bullets at an angle.
         if ( getBonusValue( BONUS_THREEWAYSHOT ).getValue() == 1 )
         {
             units.add( new Bullet( this, color, parent.getFiringOriginX(), parent.getFiringOriginY(), parent.getDx(), parent.getDy(), angle + Math.PI / 8 ) );
             units.add( new Bullet( this, color, parent.getFiringOriginX(), parent.getFiringOriginY(), parent.getDx(), parent.getDy(), angle - Math.PI / 8 ) );
         }
 
         if ( !isInfiniteAmmo() )
             --ammo;
 
         timeTillNextShot = getBonusValue( BONUS_INTERVALSHOOT ).getValue();
         Sound.playInternal( SoundLibrary.BULLET_SHOOT );
     }
 
     @Override
     public void berserk( GameObject parent, Color color )
     {
         int firedShots = 0;
         for ( double angle = 0; angle < 2 * Math.PI; angle += Math.PI / 50 )
         {
             if ( !canBerserk() )
                 break;
 
             units.add( new Bullet( this, color, parent.getFiringOriginX(), parent.getFiringOriginY(), parent.getDx(), parent.getDy(), angle ) );
 
             if ( !isInfiniteAmmo() )
                 --ammo;
 
             firedShots += 1;
         }
 
         if ( firedShots > 0 )
         {
             timeTillNextBerserk = firedShots * 2;
             Sound.playInternal( SoundLibrary.BERSERK );
         }
     }
 
     public int getMaxUnits()
     {
         return 500;
     }
 
     @Override
     public String getName()
     {
         return "Machine Gun";
     }
 
     @Override
     public int getEntryAmmo()
     {
         return 0;
     }
 
     @Override
     public void drawOrphanUnit( Graphics g, double x, double y, Color color )
     {
         new Bullet( this, color, x, y, 0, 0, 0 ).draw( g );
     }
 
     //                                                                            \\
     // --------------------------------- BONUS ---------------------------------- \\
     //                                                                            \\
     public int getSpeed()
     {
         return speed;
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
         stream.writeInt( speed );
 
         // Flatten all of the units.
         stream.writeInt( units.size() );
         for ( Unit u : units )
             ( (Bullet) u ).flatten( stream );
     }
 
     /**
      * Reads <code>this</code> from a stream for client/server transmission.
      */
     public BulletManager( DataInputStream stream ) throws IOException
     {
         super( stream );
         speed = stream.readInt();
 
         // Restore all of the units.
         int size = stream.readInt();
         for ( int i = 0; i < size; i++ )
             units.add( new Bullet( stream, this ) );
     }
 }
