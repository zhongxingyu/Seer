
 package disasteroids.weapons;
 
 import disasteroids.GameObject;
 import disasteroids.sound.Sound;
 import disasteroids.sound.SoundLibrary;
 import java.awt.Color;
 import java.awt.Graphics;
 
 /**
  *
  * @author Matt Weir
  * @since May 30, 2009
  */
 public class GuidedMissileManager extends Weapon
 {

     @Override
     protected void genericInit()
     {
         super.genericInit();
 
         ammo = -1;
 
         //need bonuses later
     }
 
     @Override
    public void drawOrphanUnit(Graphics g, double x, double y, Color col)
     {
        new GuidedMissile(this, col, x, y, 0, 0, 0).draw(g);
     }
 
     @Override
     public String getName()
     {
         return "Guided Missile Launcher";
     }
 
     @Override
    public void shoot(GameObject parent, Color color, double angle)
     {
         if ( !canShoot() )
             return;
 
         units.add( new GuidedMissile( this, color, parent.getFiringOriginX(), parent.getFiringOriginY(), parent.getDx(), parent.getDy(), angle ) );
 
         if ( !isInfiniteAmmo() )
             --ammo;
 
        timeTillNextShot = getBonusValue( 0 ).getValue();
         Sound.playInternal( SoundLibrary.MISSILE_SHOOT );
     }
 
     @Override
    public void berserk(GameObject parent, Color color)
     {
         //need to think of a berserk for this, maybe just normal missile beserk?
     }
 
     @Override
     public int getMaxUnits()
     {
         return 4;
     }
 
     @Override
     public int getEntryAmmo()
     {
        throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public int getSpeed()
     {
         //just a little faster than regular misile
        return 1;
     }
 
     public int life()
     {
         return 125;
     }

 }
