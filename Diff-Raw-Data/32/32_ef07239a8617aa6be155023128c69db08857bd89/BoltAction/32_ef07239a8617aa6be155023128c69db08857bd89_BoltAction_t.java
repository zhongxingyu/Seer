 package com.stewsters.weapons.gun.receiver;
 
 
 import com.stewsters.weapons.gun.attachment.Magazine;
 
 //This should be similar to a mosin-nagant
 // sa
 public class BoltAction implements Receiver {
 
     long timeToReload = 500;
     long lastReloaded = 0;
 
 
     boolean cocked = false;
     boolean roundChambered = false;
     boolean triggerReleased = true;
     boolean boltOpen = false;
 
     Magazine magazine = new Magazine(5, 5);
 
 
    public boolean openBolt() {
         long currentTime = System.currentTimeMillis();
         if (lastReloaded + timeToReload < currentTime) {
             boltOpen = true;
             cocked = false;
             roundChambered = magazine.removeRound();
             lastReloaded = currentTime;
             return true;
         } else return false;
 
     }
 
     /**
      * Can either insert by hand or with a stripper clip
     *
      * @return
      */
    public boolean insertRound() {
        if (boltOpen) {
            if (roundChambered) {
                return magazine.addRound();
            } else {
                roundChambered = true;
                return true;
            }
        }
        return false;
    }
 
    public boolean closeBolt() {
         boltOpen = false;
         cocked = true;
         return true;
     }
 
 
     @Override
     public boolean cock() {
         long currentTime = System.currentTimeMillis();
         if (lastReloaded + timeToReload < currentTime) {
            openBolt();
            closeBolt();
             return true;
         } else return false;
     }
 
     @Override
     public void leftRelease() {
         triggerReleased = true;
     }
 
 
     /**
      * pull trigger
      * uncocks, empties chamber, and may reload
      *
      * @return
      */
     @Override
     public boolean fireRound() {
         if (!triggerReleased)
             return false;
 
         if (!cocked)
             return false;
 
         if (!roundChambered) { //click, no ammo fired
             cocked = false;
             return false;
         }
         triggerReleased = false;
         roundChambered = false;
         cocked = false;
         return true;
 
     }
 
 
 }
