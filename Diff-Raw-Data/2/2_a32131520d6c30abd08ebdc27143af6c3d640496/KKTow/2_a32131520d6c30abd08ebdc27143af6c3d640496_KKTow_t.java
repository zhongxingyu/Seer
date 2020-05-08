 package thrust.entities.behaviors;
 /**
  * An entity that is a threat to the spaceship.
  * @author Ciaran Hale (ciaran.hale@ucd.connect.ie)
 * @version 23 April 2008
  */
 
 
 public class KKTow implements Tow {
 
   public boolean kk_tow() {
 
     if (towed()) {
 
       return true;
 
 
 
 
 
     }
     return false;
 
   }
   public void tow() {
     // TODO Auto-generated method stub
 
   }
 
   public boolean towed() {
     // TODO Auto-generated method stub
     return false;
   }
 
 
 
 }
