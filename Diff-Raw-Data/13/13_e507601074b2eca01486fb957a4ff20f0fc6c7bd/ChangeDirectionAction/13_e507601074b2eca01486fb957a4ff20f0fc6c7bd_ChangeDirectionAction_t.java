 package edu.brown.cs32.goingrogue.gameobjects.actions;
 
 import edu.brown.cs32.goingrogue.gameobjects.creatures.Creature;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Ben Weedon (bweedon)
  */
 public class ChangeDirectionAction extends Action {
 
     private double _direction;
    
     public ChangeDirectionAction(Creature sourceCreature, double direction) {
         super(0, new ChangeDirectionRange(sourceCreature));
         _direction = direction;
        _type=ActionType.CHANGE_DIR;
     }
 
     @Override
     public void act(Creature creature) {
         creature.setDirection(_direction);
     }
 
     @Override
     public List<ActionAnimation> getActionAnimations() {
         return new ArrayList<>();
     }
 }
