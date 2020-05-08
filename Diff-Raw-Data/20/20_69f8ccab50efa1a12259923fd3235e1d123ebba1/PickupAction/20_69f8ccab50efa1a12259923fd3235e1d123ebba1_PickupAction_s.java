 package edu.brown.cs32.goingrogue.gameobjects.actions;
 
 import edu.brown.cs32.goingrogue.gameobjects.creatures.Creature;
 import edu.brown.cs32.goingrogue.gameobjects.items.GridItem;
 import edu.brown.cs32.goingrogue.gameobjects.items.Item;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Objects;
 
 /**
  *
  * @author Ben Weedon (bweedon)
  */
 public class PickupAction extends Action {
 
     private Creature _sourceCreature;
 
     public PickupAction(int timer, Range range, Creature sourceCreature) {
         super(timer, range);
         _type = ActionType.PICKUP;
         _sourceCreature = sourceCreature;
     }
 
     @Override
     public void act(Creature creature) {
        if (creature.isItem()) {
            ((GridItem) creature).pickUp();
            _sourceCreature.addItem(new Item((GridItem) creature));
        }
     }
 
     @Override
     public List<ActionAnimation> getActionAnimations() {
         String spritePath = _sourceCreature.getSpritePath();
         Point2D.Double pos = new Point2D.Double(0.0, 0.0);
         double angle = 0.0;
        List<ActionAnimation> list=new ArrayList<>();
         list.add(new ActionAnimation(spritePath, pos, angle, _sourceCreature.getSize()));
         return list;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 23 * hash + Objects.hashCode(this._sourceCreature);
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
         final PickupAction other = (PickupAction) obj;
         if (!Objects.equals(this._sourceCreature, other._sourceCreature)) {
             return false;
         }
         return true;
     }
 }
