 package edu.brown.cs32.goingrogue.gameobjects.creatures;
 
 import java.awt.geom.Point2D;
 import java.util.List;
 
 import edu.brown.cs32.goingrogue.gameobjects.actions.ArcAttackAction;
 import edu.brown.cs32.goingrogue.gameobjects.actions.ChangeDirectionAction;
 import edu.brown.cs32.goingrogue.gameobjects.actions.MoveAction;
 import edu.brown.cs32.goingrogue.gameobjects.actions.PickupAction;
 import edu.brown.cs32.goingrogue.gameobjects.actions.PickupRange;
 import edu.brown.cs32.goingrogue.gameobjects.actions.Action;
 import edu.brown.cs32.goingrogue.gameobjects.creatures.util.CombatUtil;
 import edu.brown.cs32.goingrogue.util.CreatureSize;
 
 /**
  *
  * @author Ben Weedon (bweedon)
  */
 public class Player extends Creature {
 
     private InputHandler handle;
 
     public Player(Point2D.Double pos, double direction, String name,
             List<Attribute> attributes, CreatureStats stats, String sprite, CreatureSize size) {
         super(pos, direction, name, attributes, stats, sprite, size);
         handle = new InputHandler(this);
     }
 
     @Override
     public boolean isItem() {
         return false;
     }
 
     public InputHandler getHandler() {
         return handle;
     }
 
     public class InputHandler {
 
         Player player;
         Action attack;
 
         InputHandler(Player p) {
             player = p;
             attack = null;
         }
 
         public void moveUp() {
             addAction(new MoveAction((3.0 * Math.PI) / 2.0, player));
             addAction(new ChangeDirectionAction(player, (3.0 * Math.PI) / 2.0));
         }
 
         public void moveRight() {
             addAction(new MoveAction(0, player));
             addAction(new ChangeDirectionAction(player, 0));
         }
 
         public void moveDown() {
             addAction(new MoveAction(Math.PI / 2.0, player));
             addAction(new ChangeDirectionAction(player, Math.PI / 2.0));
         }
 
         public void moveLeft() {
             addAction(new MoveAction(Math.PI, player));
             addAction(new ChangeDirectionAction(player, Math.PI));
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
            addAction(new PickupAction(new PickupRange(player), player));
         }
     }
 }
