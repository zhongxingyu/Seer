 package zombiefu.creature;
 
 import zombiefu.items.Waffe;
 import jade.util.Guard;
 import jade.util.datatype.ColoredChar;
 import jade.util.datatype.Coordinate;
 import jade.util.datatype.Direction;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import zombiefu.items.Item;
 import zombiefu.items.MensaCard;
 import zombiefu.ki.StupidMover;
 import zombiefu.ki.MoveAlgorithm;
 import zombiefu.ki.TargetNotFoundException;
 import zombiefu.util.TargetIsNotInThisWorldException;
 import zombiefu.util.ZombieTools;
 
 public abstract class Monster extends Creature {
 
     protected MoveAlgorithm movealg;
     private Waffe waffe;
 
     public Monster(ColoredChar face, String n, int h, int a, int d, Waffe w, MoveAlgorithm m) {
         super(face, n, h, a, d);
         waffe = w;
         movealg = m;
     }
 
     public Monster(ColoredChar face, String n, int h, int a, int d, Waffe w) {
         this(face, n, h, a, d, w, new StupidMover());
     }
 
     public Monster(ColoredChar face, MoveAlgorithm m) {
         super(face);
         waffe = new Waffe(ColoredChar.create('|'), "Faust", 1);
         movealg = m;
     }
 
     public Monster(ColoredChar face) {
         this(face, new StupidMover());
     }
 
     protected void moveRandomly() {
         tryToMove(ZombieTools.getRandomDirection());
     }
 
     private Coordinate getPlayerPosition() throws TargetIsNotInThisWorldException {
         Guard.argumentIsNotNull(world());
         Player player = world().getActor(Player.class);
 
         if (player == null) {
             throw new TargetIsNotInThisWorldException();
         }
 
         return player.pos();
     }
 
     protected boolean positionIsVisible(Coordinate pos) throws TargetIsNotInThisWorldException {
         return pos().distance(getPlayerPosition()) <= 10;
     }
 
     protected Direction directionToPlayer() throws TargetNotFoundException, TargetIsNotInThisWorldException {
         return movealg.directionTo(world(), pos(), getPlayerPosition());
     }

     protected void moveToPlayer() throws TargetIsNotInThisWorldException, TargetNotFoundException {
         tryToMove(directionToPlayer());
     }
 
     @Override
     public void act() {
         try {
             if (positionIsVisible(getPlayerPosition())) {
                 moveToPlayer();
             } else {
                 moveRandomly();
             }
         } catch (TargetIsNotInThisWorldException ex) {
         } catch (TargetNotFoundException ex) {
             moveRandomly();
         }
     }
 
     @Override
     public Waffe getActiveWeapon() {
         return waffe;
     }
 
     protected abstract Item itemDroppedOnKill();
 
     @Override
     protected void killed(Creature killer) {
         Item it = itemDroppedOnKill();
         if (it != null) {
             world().addActor(it, pos());
         }
         expire();
         ZombieTools.sendMessage(killer.getName() + " hat " + getName() + " getötet.");
     }
 
     @Override
     protected Direction getAttackDirection() {
        // TODO: Überprüfen, ob Gegner wirklich in einer Linie
         try {
             return directionToPlayer();
        } catch (TargetNotFoundException e) {
        } catch (TargetIsNotInThisWorldException ex) {
        }
         return null;
     }
 }
