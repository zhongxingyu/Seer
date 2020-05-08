 package edu.brown.cs32.goingrogue.gameobjects.creatures;
 
 import edu.brown.cs32.goingrogue.gameobjects.actions.Action;
 import edu.brown.cs32.goingrogue.gameobjects.actions.MoveAction;
 import edu.brown.cs32.goingrogue.util.CreatureSize;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 import static edu.brown.cs32.bweedon.geometry.Point2DUtil.getAngleFromTo;
 import edu.brown.cs32.goingrogue.gameobjects.actions.ArcAttackAction;
 import edu.brown.cs32.jcadler.GameLogic.RogueMap.Room;
 
 /**
  *
  * @author Ben Weedon (bweedon)
  */
 public class AICreature extends Creature {
 
     private List<Creature> _creatures;
     private List<Room> _rooms;
     private final double DIST_TO_ATTACK = 0.5;
 
     public AICreature(Point2D.Double pos, double direction, String name, List<Attribute> attributes,
             CreatureStats stats, String spritePath, CreatureSize size, List<Creature> creatures,
             List<Room> rooms) {
         super(pos, direction, name, attributes, stats, spritePath, size);
         _creatures = creatures;
         _rooms = rooms;
     }
 
     @Override
     public boolean isItem() {
         return false;
     }
 
     @Override
     public List<Action> getActionsWithUpdate(int delta) {
         Creature closestCreature = null;
         for (int i = 0; i < _creatures.size(); ++i) {
             Creature currCreature = _creatures.get(i);
             Point2D currCreaturePos = currCreature.getPosition();
             if ((closestCreature == null) && (!currCreature.equals(this))) {
                 closestCreature = currCreature;
            } else if ((getPosition().distance(currCreaturePos)
                     < getPosition().distance(closestCreature.getPosition()))
                     && (!currCreature.equals(this))
                     && (currCreature.getAttributes().contains(Attribute.PLAYER))) {
                 closestCreature = currCreature;
             }
         }
 
         List<Action> returnActions = new ArrayList<>();
         if (closestCreature != null) {
             setDirection(getAngleFromTo(getPosition(), closestCreature.getPosition()));
             if (getPosition().distance(closestCreature.getPosition()) < DIST_TO_ATTACK) {
                 returnActions.add(
                         new ArcAttackAction(getDirection(), getWeaponRange(), getWeaponArcLength(),
                         getWeaponAttackTimer(), this));
                 return returnActions;
             } else {
                 returnActions.add(new MoveAction(getDirection(), this));
                return returnActions;
             }
         }
 
         setActions(returnActions);
         return returnActions;
 //        return new ArrayList<>();
     }
 }
