 package com.group7.dragonwars.engine;
 
 import java.util.*;
 import java.io.*;
 import java.lang.Math;
 
 import android.util.Log;
 
 public class GameState {
 
     private GameMap map;
     private Logic logic;
     private List<Player> players = new ArrayList<Player>();
     private Integer playerIndex = 0;
     private Integer turns = 1;
     private Boolean gameFinished = false;
 
     public GameState(GameMap map, Logic logic, List<Player> players) {
         this.map = map;
         this.logic = logic;
         this.players = players;
     }
 
 
 
     public void attack(Unit attacker, Unit defender) {
         Set<Position> attackable = logic.getAttackableUnitPositions(map,
                                    attacker);
 
         //if (!attackable.contains(defender.getPosition()))
         //    return;
         boolean contains = false;
         for (Position pos : attackable) {
             if (pos.equals(defender.getPosition()))
                 contains = true;
         }
         if (!contains) return;
 
         Pair<Double, Double> damage = logic.calculateDamage(map, attacker,
                                       defender);
         Log.v(null, "Dmg to atckr: " + damage.getRight() + " Dmg to dfndr: " + damage.getLeft());
 
         defender.reduceHealth(damage.getLeft());
 
         Boolean died = removeUnitIfDead(defender);
 
         if (died)
             return;
 
         /* Possibly counter */
         attacker.reduceHealth(damage.getRight());
         removeUnitIfDead(attacker);
 
     }
 
     public Boolean move(Unit unit, Position destination) {
         /* We are assuming that the destination was already
          * checked to be within this unit's reach
          */
 
         List<Position> path = logic.findPath(map, unit, destination);
         Integer movementCost = logic.calculateMovementCost(map, unit, path);
 
         if (!map.isValidField(destination) || path.size() == 0) {
             return false;
         }
 
         GameField destField = map.getField(destination);
         if (destField.hostsUnit()) {
             return false;
         }
 
 
         /* Double check */
         if (unit.getRemainingMovement() < movementCost) {
             return false;
         }
 
 
         GameField currentField = map.getField(unit.getPosition());
         destField.setUnit(unit);
         unit.reduceMovement(movementCost);
 
         currentField.setUnit(null);
         unit.setPosition(destination);
         unit.setMoved(true);
 
         return true;
 
     }
 
     private Boolean removeUnitIfDead(Unit unit) {
         if (unit.isDead()) {
             map.getField(unit.getPosition()).setUnit(null);
             unit.getOwner().removeUnit(unit);
             return true;
         }
 
         return false;
     }
 
     private void updateBuildingCaptureCounters() {
         for (GameField gf : map) {
 
             /* No building. */
             if (!gf.hostsBuilding())
                 continue;
 
             Building b = gf.getBuilding();
 
             /* Unit on the building. */
             if (gf.hostsUnit()) {
                 Unit unit = gf.getUnit();
                 Integer turnReduce = unit.getHealth().intValue();
 
                 /* Unit already owns the building or is capturing for >1 turn. */
                 if (unit.getOwner().equals(b.getLastCapturer())) {
                     b.reduceCaptureTime(turnReduce);
                     continue;
                 } else {
                     b.resetCaptureTime();
                     b.setLastCapturer(unit.getOwner());
                     b.reduceCaptureTime(turnReduce);
                 }
             }
             /* No unit on the building. */
             else {
                 if (b.hasOwner())
                     continue;
                 else
                     b.resetCaptureTime();
             }
         }
     }
 
     public void nextPlayer() throws GameFinishedException {
        Iterator<Player> iter = players.iterator();
 
         while (iter.hasNext()) {
             Player p = iter.next();
             if (p.hasLost()) {
                 iter.remove();
             }
         }
 
         if (players.size() <= 1) {
             throw new GameFinishedException(players.get(0));
         }
 
         playerIndex++;
         if (playerIndex == players.size()) {
             playerIndex = 0;
             advanceTurn();
         }
     }
 
     private void advanceTurn() {
         updateBuildingCaptureCounters();
 
         for (Player p : players) {
             Integer goldWorth = 0;
 
             for (Building b : p.getOwnedBuildings())
                 goldWorth += b.getCaptureWorth();
             p.setGoldAmount(goldWorth + p.getGoldAmount());
 
             for (Unit u : p.getOwnedUnits()) {
                 u.resetTurnStatistics();
             }
         }
         ++this.turns;
     }
 
     public Integer getTurns() {
         return this.turns;
     }
 
     public GameMap getMap() {
         return this.map;
     }
 
     public List<Player> getPlayers() {
         return this.players;
     }
 
     public Player getCurrentPlayer() {
         return players.get(playerIndex);
     }
 
     public Boolean produceUnit(final GameField field, final Unit unit) {
     	// produces a unit "at" a building
         if (!field.hostsBuilding() || field.hostsUnit())
             return false;
 
         Building building = field.getBuilding();
 
         for (Unit u : building.getProducibleUnits()) {
             if (u.getName().equals(unit.getName())) {
             	Player player = building.getOwner();
 
             	if (player.getGoldAmount() < u.getProductionCost()) {
                     return false;
             	}
 
                 Unit newUnit = new Unit(u);
                 newUnit.setPosition(building.getPosition());
                 newUnit.setOwner(player);
 
                 player.setGoldAmount(player.getGoldAmount() - unit.getProductionCost());
                 player.addUnit(newUnit);
             	field.setUnit(newUnit);
 
                 return true;
             }
         }
 
         return false;
     }
 
 }
