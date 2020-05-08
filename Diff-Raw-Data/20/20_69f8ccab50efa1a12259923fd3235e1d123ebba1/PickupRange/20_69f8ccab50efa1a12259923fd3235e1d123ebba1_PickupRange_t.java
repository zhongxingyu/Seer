 package edu.brown.cs32.goingrogue.gameobjects.actions;
 
 import edu.brown.cs32.goingrogue.gameobjects.creatures.Creature;
 import java.util.Objects;
 
 /**
  *
  * @author Ben Weedon (bweedon)
  */
 public class PickupRange implements Range {
 
     private Creature _sourceCreature;
    private final double PICKUP_RANGE = 1.0;
 
     public PickupRange(Creature sourceCreature) {
         _sourceCreature = sourceCreature;
     }
 
     @Override
     public boolean inRange(Creature creature) {
         if (!creature.isItem()) {
             return false;
         }
        //return (_sourceCreature.getPosition().distance(creature.getPosition()) <= PICKUP_RANGE);
        return _sourceCreature.getRectangle().intersects(creature.getRectangle());
     }
 
     @Override
     public void decrementTimer(int delta) {
         // do nothing
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 59 * hash + Objects.hashCode(this._sourceCreature);
         hash = 59 * hash + (int) (Double.doubleToLongBits(this.PICKUP_RANGE) ^ (Double.doubleToLongBits(this.PICKUP_RANGE) >>> 32));
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final PickupRange other = (PickupRange) obj;
         if (!Objects.equals(this._sourceCreature, other._sourceCreature)) {
             return false;
         }
         if (Double.doubleToLongBits(this.PICKUP_RANGE) != Double.doubleToLongBits(other.PICKUP_RANGE)) {
             return false;
         }
         return true;
     }
 }
