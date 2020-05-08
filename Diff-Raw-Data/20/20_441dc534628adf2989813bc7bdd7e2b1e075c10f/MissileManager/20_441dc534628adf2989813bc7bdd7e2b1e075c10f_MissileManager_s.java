 /*
  * DISASTEROIDS
  * MissileManager.java
  */
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.LinkedList;
 import java.util.ListIterator;
 import java.util.Queue;
 
 /**
  * A manager that each <code>Ship</code> uses for its <code>Missile</code>s.
  * @author Andy Kooiman
  */
 public class MissileManager
 {
     /**
      * The time between each shot
      * @since December 15, 2007
      */
     private int intervalShoot;
     /**
      * The current initial speed for <code>Missile</code>s in this manager.
      * @since Classic
      */
     private double speed = 10;
 
     /**
      * The current probability of a huge blast in <code>Missile</code>s of this manager.
      * @since Classic
      */
     private int hugeBlastProb = 5;
 
     /**
      * The current size of huge blasts.
      * @since Classic
      */
     private int hugeBlastSize = 50;
 
     /**
      * The current probability that <code>Missile</code>s of this manager will split.
      * @since Classic
      */
     private int probPop = 2000;
 
     /**
      * The number of <code>Missile</code>s generated when splitting occurs.
      * @since Classic
      */
     private int popQuantity = 5;
 
     /**
      * The list of currently valid <code>Missile</code>s.
      * @since Classic
      */
    private LinkedList<Misile> theMissiles = new LinkedList<Misile>();
 
     /**
      * The list of <code>Missile</code>s waiting to be added.
      * @since Classic
      */
    private Queue<Misile> toBeAdded = new LinkedList<Misile>();
 
     /**
      * Gets all currently valid <code>Missile</code>s.
      * @return All currently valid <code>Missile</code>s.
      * @author Andy Kooiman
      * @since Classic
      */
    public synchronized LinkedList<Misile> getMissiles()
     {
         return theMissiles;
     }
 
     /**
      * Iterates through each <code>Missile</code> and either removes it or instructs it to act.
      * @author Andy Kooiman
      * @since Classic
      */
     public synchronized void act()
     {
         Graphics g = AsteroidsFrame.getGBuff();
        ListIterator<Misile> iter = theMissiles.listIterator();
         while ( iter.hasNext() )
         {
            Misile m = iter.next();
             if ( m.needsRemoval() )
                 iter.remove();
             else
                 m.act();
         }
 
         while ( !toBeAdded.isEmpty() )
         {
             theMissiles.add( toBeAdded.remove() );
         }
     }
 
     /**
      * Instructs each <code>Missile</code> to explode, without splitting.
      * @author Andy Kooiman
      * @since Classic
      */
     public void explodeAll()
     {
         int probPopTemp = probPop;
         probPop = Integer.MAX_VALUE;
        for ( Misile m : theMissiles )
             m.explode();
         probPop = probPopTemp;
     }
 
     /**
      * Creates and prepares to add a <code>Missile</code> with the specified properties.
      * @param x The x coordinate.
      * @param y The y coordinate.
      * @param angle The angle the <code>Missile</code> will be pointing (not necessarily the angle it will be traveling).
      * @param dx The x component of velocity.
      * @param dy The y component of velocity (up is negative).
      * @param col The <code>Color</code> of the <code>Missile</code>..
      * @return True if the <code>Missile</code> was successfully added, false otherwise.
      * @author Andy Kooiman
      * @since Classic
      */
     public boolean addMissile( int x, int y, double angle, double dx, double dy, Color col )
     {
         if ( theMissiles.size() > 1000 )
             return false;
        return toBeAdded.add( new Misile( this, x, y, angle, dx, dy, col ) );
     }
 
     /**
      * Sets the probability of a huge blast.
      * @param newProb The new probability.
      * @author Andy Kooiman
      * @since Classic
      */
     public void setHugeBlastProb( int newProb )
     {
         hugeBlastProb = newProb;
     }
 
     /**
      * Sets the size of huge blasts.
      * @param newSize The new size.
      * @author Andy Kooiman
      * @since Classic
      */
     public void setHugeBlastSize( int newSize )
     {
         hugeBlastSize = newSize;
     }
 
     /**
      * Sets the probability of <code>Missile</code>s splitting.
      * @param newProb The new probability.
      * @author Andy Kooiman
      * @since Classic
      */
     public void setProbPop( int newProb )
     {
         probPop = newProb;
     }
 
     /**
      * Increases the number of new <code>Missile</code>s added when a split occurs.
      * @param increase The number to increase the current pop quantity by.
      * @author Andy Kooiman
      * @since Classic
      */
     public void increasePopQuantity( int increase )
     {
         popQuantity += increase;
     }
 
     /**
      * Sets the new default speed.
      * @param newSpeed The new speed.
      * @author Andy Kooiman
      * @since Classic
      */
     public void setSpeed( int newSpeed )
     {
         speed = newSpeed;
     }
 
     /**
      * Increases the default speed.
      * @param dSpeed The change in speed.
      * @author Andy Kooiman
      * @since Classic
      */
     public void increaseSpeed( int dSpeed )
     {
         speed += dSpeed;
     }
 
     /**
      * Sets the pop quantity to a value.
      * @param newQuantity The new pop quantity.
      * @author Andy Kooiman
      * @since Classic
      */
     public void setPopQuantity( int newQuantity )
     {
         popQuantity = newQuantity;
     }
 
     /**
      * Removes all Missiles from this manager.
      * @author Andy Kooiman.
      * @since Classic
      */
     public void clear()
     {
         theMissiles.clear();
     }
 
     /**
      * Gets the number of currently valid <code>Missile</code>s in this manager.
      * @return The number of <code>Missile</code>s.
      * @author Andy Kooiman
      * @since Classic
      */
     public int getNumLivingMissiles()
     {
         return theMissiles.size();
     }
 
     /**
      * Gets the probability of a split occurring.
      * @return The probability of a split.
      * @author Andy Kooiman
      * @since Classic
      */
     public int probPop()
     {
         return probPop;
     }
 
     /**
      * Gets the current size of a huge blast.
      * @return The current size of a huge blast.
      * @author Andy Kooiman
      * @since Classic
      */
     public int hugeBlastSize()
     {
         return hugeBlastSize;
     }
 
     /**
      * Gets the current default speed for new <code>Missile</code>s of this manager.
      * @return The current default speed.
      * @author Andy Kooiman
      * @since Classic
      */
     public double speed()
     {
         return speed;
     }
 
     /**
      * Gets the current probabiltiy of a huge blast.
      * @return The current probabiltiy of a huge blast.
      * @author Andy Kooiman
      * @since Classic
      */
     public int hugeBlastProb()
     {
         return hugeBlastProb;
     }
 
     /**
      * Gets the current quantity of <code>Missile</code>s created when a split occurs.
      * @return The number of <code>Missile</code>s created per split.
      * @author Andy Kooiman
      * @since Classic
      */
     public int popQuantity()
     {
         return popQuantity;
     }
 
     int getIntervalShoot() {
         return intervalShoot;
     }
 
     public void setIntervalShoot(int i) {
         intervalShoot=i;
     }
 }
