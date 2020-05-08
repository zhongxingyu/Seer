 /*
  * DISASTEROIDS
  * PredatorAlien.java
  */
 
 package disasteroids;
 
 
 
 import java.io.IOException;
 import java.io.DataInputStream;
 
 /**
  *
  * @author Greg Cotten
  */
 public class PredatorAlien extends Alien
 {
     protected Ship prey;
     
     public PredatorAlien(double x, double y, double dx, double dy) 
     {
         super(x,y,dx,dy);
     }
     
     public PredatorAlien( DataInputStream stream ) throws IOException
     {
         super(stream);
     }
     
      /**
      * If this does not have prey, it will default to Alien's act() method.
       * Otherwise, this will go through normal Predator behavior.
      */
     public void act()
     {
         if(!getPrey())
         {
             super.act();
             return;
         }
         accelerateAnalyze(prey);
         generalActBehavior();
         
     }
     
     /**
      * Adjusts acceleration of PredatorAlien to chase prey.
      * This will be fun to write.
      */
     protected void accelerateAnalyze(Ship s)
     {
         int radius = 200; //min distance from ship
         double leniance = 125;
        int proximity = getProximity(s);
         
        if(proximity<radius-(int)(leniance/2.0))
             accelerateAway(s);
        if(proximity>radius+(int)(leniance/2.0))
             accelerateToward(s);
         else
             deccelerate();
         
     }
     
     protected void deccelerate()
     {
     
     }
     
    protected void accelerateAway()
     {
         
     }
     
    protected void accelerateToward()
     {
         
     }
      
      /**
      * Finds a player to hunt within range; If no player is found, getPrey()
       * returns false.
      */
     public boolean getPrey()
     {
         // Find players within our range.        
         int range = 500;//good range for finding prey?
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
             angle = mAngle; //orients toward player
             return true;
         }
         else
             return false;
     }
 }
