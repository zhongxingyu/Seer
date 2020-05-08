 package com.group7.dragonwars.engine.GoalArbitration;
 
 import java.util.List;
 
 import com.group7.dragonwars.engine.GameState;
 import com.group7.dragonwars.engine.Logic;
 import com.group7.dragonwars.engine.Pair;
 import com.group7.dragonwars.engine.Player;
 import com.group7.dragonwars.engine.Position;
 import com.group7.dragonwars.engine.Unit;
 
 public class StateTree {
     private int maxSize = 200;
     private GameState gameState;
     private Node base = null;
     private Player stateTreeOwner;
 
     private List<AtomicAction> actions;
 
     public StateTree(GameState gamestate, int maxsize, Player owner) {
         gameState = gamestate;
         stateTreeOwner = owner;
     }
 
     private void Explore() {
         Logic logic = new Logic();
         base = new Node(null, 0, 0, null);
         base.setSize(1);
 
         for (Player player : gameState.getPlayers()) {
             if (player.equals(stateTreeOwner))
                 continue;
 
             for (Unit playerUnit : stateTreeOwner.getOwnedUnits()) {
                 float bestValue = -1;
                 AtomicAction currentBest = null;
 
                 for (Unit unit : player.getOwnedUnits()) {
                     if (base.getSize() >= maxSize)
                         break;
 
                     //Evaluate cost and gain, don't add if below threshold
                     Pair<Double, Double> damageExchange = logic.calculateDamage(gameState.getMap(), playerUnit, unit);
                     float damageRatio = (float)(damageExchange.getLeft()/damageExchange.getRight());
 
                     if (damageRatio < 0)		// In enemy's favour
                         continue;
 
                     List<Position> path = logic.findPath(gameState.getMap(), playerUnit, unit.getPosition());
 
                     // calculate direction to move or attack
                     if (path.size() == 1) {
                         //calculate whether this move is better than the current best
                         if (damageRatio > bestValue) {
                             currentBest = new AttackAt(gameState, playerUnit, unit, damageRatio);
                             bestValue = damageRatio;
                         }
                     } else if (path.size() > 1) {
                         currentBest =  new MoveTo(gameState, playerUnit, unit.getPosition(), damageRatio/path.size());
 
                         //calculate whether this move is better than the current best
                         if (damageRatio/path.size() > bestValue) {
                             currentBest = new AttackAt(gameState, playerUnit, unit, damageRatio);
                             bestValue = damageRatio/path.size();
                         }
                     }
                 }
 
                 if (base.getSize() >= maxSize)
                     break;
 
                 base.AddChildNode(bestValue, currentBest);
             }
 
         }
 
        base.Collaplse();
         actions = base.getActions();
     }
 
     public List<AtomicAction> getActions() {
         Explore();
         return actions;
     }
}
