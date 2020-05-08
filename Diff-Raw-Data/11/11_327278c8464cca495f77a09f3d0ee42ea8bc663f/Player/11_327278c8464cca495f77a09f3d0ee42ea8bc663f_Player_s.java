 package edu.brown.cs32.goingrogue.gameobjects.creatures;
 
 import java.awt.geom.Point2D;
 import java.util.List;
 
 import edu.brown.cs32.goingrogue.gameobjects.actions.ArcAttackAction;
 import edu.brown.cs32.goingrogue.gameobjects.actions.ChangeDirectionAction;
 import edu.brown.cs32.goingrogue.gameobjects.actions.MoveAction;
 import edu.brown.cs32.goingrogue.gameobjects.actions.PickupAction;
 import edu.brown.cs32.goingrogue.gameobjects.actions.PickupRange;
 import edu.brown.cs32.goingrogue.gameobjects.actions.Action;
 import edu.brown.cs32.goingrogue.gameobjects.actions.ActionType;
 import edu.brown.cs32.goingrogue.gameobjects.actions.QuaffAction;
 import edu.brown.cs32.goingrogue.util.CreatureSize;
 
 /**
  *
  * @author Ben Weedon (bweedon)
  */
 public class Player extends Creature {
 
     private Action attack;
     private int _xp;
     private int _maxHealth;
     private int _initMaxHealth;
 
     public Player(){}
     public Player(Point2D.Double pos, double direction, String name,
             List<Attribute> attributes, CreatureStats stats, String sprite, CreatureSize size) {
         super(pos, direction, name, attributes, stats, sprite, size);
 
         _xp = 0;
         _maxHealth = this.getStats().getHealth();
 
         _initMaxHealth = _maxHealth;
     }
 
     @Override
     public List<Action> getActionsWithUpdate(int delta) {
         for (Action currAction : getActions()) {
             if (currAction.type().equals(ActionType.MOVE)) {
                 ((MoveAction) currAction).setDelta(delta);
             }
         }
         return getActions();
     }
 
     @Override
     public boolean isItem() {
         return false;
     }
 
     public int getXP() {
         return _xp;
     }
 
     public void incXP(int amt) {
         _xp += amt;
         while (_xp > getNextLevelXP()) {
             _xp -= getNextLevelXP();
             _level++;
         }
     }
 
     public int getNextLevelXP() {
         return 100 * (_level * _level + _level - 1);
     }
 
     public int getMaxHealth() {
         return _initMaxHealth + 20 * (_level - 1);
     }
     
     
     //Methods for interfacing with the keyboard
     
     public void moveUp() {
         addAction(new MoveAction((3.0 * Math.PI) / 2.0, this, 1));
        addAction(new ChangeDirectionAction(this, 0));
     }
 
     public void moveRight() {
         addAction(new MoveAction(0, this, 1));
        addAction(new ChangeDirectionAction(this, Math.PI / 2.0));
     }
 
     public void moveDown() {
         addAction(new MoveAction(Math.PI / 2.0, this, 1));
        addAction(new ChangeDirectionAction(this, Math.PI));
     }
 
     public void moveLeft() {
         addAction(new MoveAction(Math.PI, this, 1));
        addAction(new ChangeDirectionAction(this, -Math.PI / 2.0));
     }
 
     public void attack() {
         if (attack == null || attack.getTimer() <= 0) {
             Action a = new ArcAttackAction(getDirection(), getWeaponRange(), getWeaponArcLength(),
                     getWeaponAttackTimer(), Player.this);
             attack = a;
             addAction(a);
         }
     }
 
     public void pickUp() {
         addAction(new PickupAction(new PickupRange(this), this));
     }
 
     public void quaff() {
         if (getInventory().getNumPotions() > 0) {
             addAction(new QuaffAction(getInventory().getPotion(0), this));
             getInventory().dropPotion(0);
         }
     }
 }
