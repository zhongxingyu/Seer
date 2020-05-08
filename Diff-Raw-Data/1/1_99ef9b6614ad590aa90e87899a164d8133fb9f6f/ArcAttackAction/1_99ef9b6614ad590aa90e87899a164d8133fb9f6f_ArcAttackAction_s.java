 package edu.brown.cs32.goingrogue.gameobjects.actions;
 
 import edu.brown.cs32.goingrogue.gameobjects.creatures.Creature;
 import edu.brown.cs32.goingrogue.util.Util;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Objects;
 
 /**
  *
  * @author Ben Weedon (bweedon)
  */
 public class ArcAttackAction extends Action {
 
     private Creature _sourceCreature;
     List<Creature> _actedOn;
     private double _direction;
     private final double KNOCKBACK_DIST = 1.0;
 
     public ArcAttackAction() {
     }
 
     public ArcAttackAction(double direction, double distance, double arcLength, int timer, Creature sourceCreature) {
         super(timer, new ArcAttackRange(direction, distance, arcLength, timer, sourceCreature));
 
         _type = ActionType.ATTACK;
         _sourceCreature = sourceCreature;
         _actedOn = new ArrayList<>();
         _direction = direction;
     }
 
     @Override
     public void act(Creature creature) {
         if (!_actedOn.contains(creature)) {
             creature.incurDamage(_sourceCreature);
             double[] newPos = Util.polarToRectangular(KNOCKBACK_DIST, _direction);
             newPos[0] += creature.getPosition().getX();
             newPos[1] += creature.getPosition().getY();
             creature.setPosition(new Point2D.Double(newPos[0], newPos[1]));
         }
         _actedOn.add(creature);
     }
 
     @Override
     public List<ActionAnimation> getActionAnimations() {
         String creatureSpritePath = _sourceCreature.getSpritePath();
         String weaponSpritePath = _sourceCreature.getInventory().getWeapon().getSpritePath();
 
         Point2D.Double creaturePos = _sourceCreature.getPosition();
         double creatureAngle = ((ArcAttackRange) getRange()).getAngle();
 
         //TODO This is just a temporary fix
         double[] weaponPosPolar = new double[]{0.5, creatureAngle - Math.PI / 2};
 
         double[] weaponPosTemp = Util.polarToRectangular(weaponPosPolar[0], weaponPosPolar[1]);
         weaponPosTemp[0] += creaturePos.x;
         weaponPosTemp[1] += creaturePos.y;
         Point2D.Double weaponPos = new Point2D.Double(weaponPosTemp[0], weaponPosTemp[1]);
 
         ActionAnimation creatureAnimation = new ActionAnimation(creatureSpritePath, creaturePos, creatureAngle, _sourceCreature.getSize());
         ActionAnimation weaponAnimation = new ActionAnimation(weaponSpritePath, weaponPos, creatureAngle,
                 _sourceCreature.getInventory().getWeapon().getSize());
         List<ActionAnimation> list = new ArrayList<>();
         list.add(creatureAnimation);
         list.add(weaponAnimation);
         return list;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 79 * hash + Objects.hashCode(this._sourceCreature);
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
         final ArcAttackAction other = (ArcAttackAction) obj;
         if (!Objects.equals(this._sourceCreature, other._sourceCreature)) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "The Creature " + _sourceCreature.getName() + " just attacked!";
     }
 }
