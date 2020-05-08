 package edu.brown.cs32.goingrogue.gameobjects.actions;
 
 import edu.brown.cs32.goingrogue.gameobjects.creatures.Creature;
import edu.brown.cs32.goingrogue.gameobjects.creatures.Inventory;
 import edu.brown.cs32.goingrogue.gameobjects.items.Item;
 import edu.brown.cs32.goingrogue.gameobjects.items.Potion;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Ben Weedon (bweedon)
  */
 public class QuaffAction extends Action {
 
     Range _range;
 
     public QuaffAction(Item item, Creature sourceCreature) {
         super(0, new QuaffRange(item, sourceCreature));
         _type=ActionType.QUAFF;
     }
 
     @Override
     public void act(Creature creature) {
        Potion potion = Inventory.makePotion(((QuaffRange) getRange()).getItem());
         potion.act(((QuaffRange) getRange()).getSourceCreature());
     }
 
     @Override
     public List<ActionAnimation> getActionAnimations() {
         return new ArrayList<>();
     }
 }
