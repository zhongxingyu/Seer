 package com.ahsgaming.valleyofbones.units;
 
 import com.ahsgaming.valleyofbones.GameController;
 import com.ahsgaming.valleyofbones.Player;
 import com.ahsgaming.valleyofbones.ai.AStar;
 import com.ahsgaming.valleyofbones.map.HexMap;
 import com.ahsgaming.valleyofbones.screens.LevelScreen;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.TimeUtils;
 
 import java.util.Collection;
 import java.util.HashMap;
 
 /**
  * valley-of-bones
  * (c) 2013 Jami Couch
  * Created on 1/16/14 by jami
  * ahsgaming.com
  */
 public class UnitManager {
     HashMap<Integer, Unit> units;
     GameController gameController;
 
     public UnitManager(GameController gameController) {
         units = new HashMap<Integer, Unit>();
         this.gameController = gameController;
     }
 
     public void addUnit(Unit unit) {
         units.put(unit.getId(), unit);
     }
 
     public void removeUnit(int id) {
         units.remove(id);
     }
 
     public Unit getUnit(int id) {
         return units.get(id);
     }
 
     public Unit getUnit(Vector2 boardPos) {
         for (Unit u: units.values()) {
             if (u.view.boardPosition.epsilonEquals(boardPos, 0.1f))
                 return u;
         }
         return null;
     }
 
     public Array<Unit> getUnits() {
         Array<Unit> unitArray = new Array<Unit>();
         for (Unit u: units.values()) {
             unitArray.add(u);
         }
         return unitArray;
     }
 
     public Array<Unit> getUnits(int playerId) {
         Array<Unit> returnVal = new Array<Unit>();
         for (Unit unit: units.values()) {
             if (unit.owner != null && unit.owner.getPlayerId() == playerId) {
                 returnVal.add(unit);
             }
         }
         return returnVal;
     }
 
     public Array<Unit> getUnitsInArea(Vector2 boardPos, int radius) {
         Array<Unit> returnVal = new Array<Unit>();
         for (Unit unit: units.values()) {
             if (HexMap.getMapDist(boardPos, unit.view.boardPosition) <= radius)
                 returnVal.add(unit);
         }
         return returnVal;
     }
 
     public void update(float delta) {
         Array<Integer> toRemove = new Array<Integer>();
         for (Unit unit: units.values()) {
             updateUnit(unit, delta);
 
             if (!unit.getData().isAlive() && !unit.getView().hasActions()) {
                 toRemove.add(unit.getId());
             }
         }
         for (int i: toRemove) {
             units.remove(i);
             gameController.getMap().invalidateViews();
         }
     }
 
     public void updateUnit(Unit unit, float delta) {
         if (unit.data.capturable)
             findNewOwner(unit);
 
         if (unit.data.curHP <= 0) {
             if (unit.data.capturable) {
                 unit.data.curHP = 0;
                 if (unit.owner != unit.data.uncontested) {
                     // set map dirty
                     unit.data.modified = TimeUtils.millis();
                 }
                 unit.owner = unit.data.uncontested;
                 unit.data.attacksLeft = 0;
                 unit.data.movesLeft = 0;
             }
             if (unit.data.mindControlUnit != null && unit.data.mindControlUnit.data.curHP > 0) {
                 applyDamage(unit.data.mindControlUnit, unit.data.mindControlUnit.data.curHP + unit.data.mindControlUnit.data.armor);
             }
         }
 
         unit.view.act(delta);
     }
 
     public void startTurn(Player player) {
         for (Unit unit: units.values()) {
             if (unit.data.ability.equals("increasing-returns")) {
                 for (int i = 0; i < unit.data.upkeep.size; i++) {
                     if (gameController.getGameTurn() % unit.data.abilityArgs.get("interval") == 0) {
                         unit.data.upkeep.set(
                                 i,
                                 Math.max(
                                         unit.data.abilityArgs.get("max"),
                                         unit.data.upkeep.get(i) + unit.data.abilityArgs.get("bonus")
                                 )
                         );
                     }
                 }
 
             }
             if (unit.owner != player) continue;
 
             if (!unit.data.building) {
                 unit.data.movesLeft = (unit.data.movesLeft % 1) + unit.data.moveSpeed * (unit.data.stealthActive ? 0.5f : 1f);
                 unit.data.movesThisTurn = 0;
                 unit.data.attacksLeft = (unit.data.attacksLeft % 1) + unit.data.attackSpeed;
             }
 
             if (unit.data.capturable && (unit.data.uncontested == unit.owner || unit.data.uncontested == null))
                 unit.data.setCurHP(unit.data.curHP += 5 * (unit.data.capUnitCount + 1)); // +1 here so that it auto-builds
 
             unit.view.clearPath();
             unit.view.addToPath(unit.view.getBoardPosition());
 
             unit.data.stealthEntered = false;
             unit.data.mindControlUsed = false;
             unit.data.virginUnit = false;
 
             if (unit.data.mindControlUnit != null) {
                 if (unit.data.mindControlUnit.data.curHP <= 0)
                     unit.data.mindControlUnit = null;
             }
 
             unit.data.modified = TimeUtils.millis();
         }
     }
 
     public void endTurn(Player player) {
         for (Unit unit: units.values()) {
             if (unit.owner != player) continue;
 
             if (unit.data.building) {
                 unit.data.buildTimeLeft--;
 
                 if (unit.data.buildTimeLeft <= 0)
                     unit.data.building = false;
             }
 
             unit.data.modified = TimeUtils.millis();
         }
     }
 
     public boolean attack (Unit attacker, Unit defender) {
         if (canAttack(attacker, defender)) {
             if (attacker.data.ability.equals("sabotage")) {
                 if (defender.data.capturable) {
                     defender.data.movesLeft = 0;
                     defender.data.attacksLeft = 0;
                     applyDamage(defender, defender.data.curHP + defender.data.armor);
                 } else {
                     if (defender.data.protoId.equals("castle-base"))
                         return false; // saboteur can't attack castle
                     if (defender.data.subtype.equals("light")) {
                         // saboteur 'assassinates' light units
                         applyDamage(defender, defender.data.curHP + defender.data.armor);
                     } else {
                         applyDamage(defender, (int)Math.floor(defender.data.curHP * 0.5f) + defender.data.armor);
                     }
                 }
                 attacker.data.attacksLeft--;
             } else if (attacker.data.ability.equals("mind-control")) {
                 if (!defender.data.type.equals("building") && !defender.data.ability.equals("sabotage")
                         && !attacker.data.mindControlUsed && attacker.data.mindControlUnit == null) {
                     defender.owner = attacker.owner;
                     defender.data.movesLeft = 0;
                     defender.data.attacksLeft = 0;
                     defender.data.modified = TimeUtils.millis();
                     attacker.data.mindControlUsed = true;
                     attacker.data.mindControlUnit = defender;
                     attacker.data.attacksLeft--;
                 }
             } else {
                 attacker.data.attacksLeft--;
                 applyDamage(defender, attacker.data.attackDamage * attacker.data.getBonus(defender.data.subtype));
 
                 if (attacker.data.stealthActive)
                     activateAbility(attacker);
 
                 attacker.view.addAction(UnitView.Actions.sequence(
                         UnitView.Actions.colorTo(new Color(1, 1, 0.5f, 1), 0.1f),
                         UnitView.Actions.delay(0.2f),
                         UnitView.Actions.colorTo(new Color(1, 1, 1, 1), 0.1f)
                 ));
             }
             attacker.data.modified = TimeUtils.millis();
             return true;
         }
         return false;
     }
 
     public void moveUnit(Unit unit, Vector2 boardPosition) {
         if (unit.data.ability.equals("shift")) {
             if (unit.data.movesThisTurn > 0) return;
 
             unit.view.lastBoardPosition = unit.view.boardPosition;
             unit.view.boardPosition = boardPosition;
             unit.data.movesThisTurn++;
 
             Vector2 pos = gameController.getMap().boardToMapCoords(boardPosition.x, boardPosition.y);
             unit.view.addAction(UnitView.Actions.sequence(
                     UnitView.Actions.colorTo(new Color(1, 1, 1, 0), 0.4f),
                     UnitView.Actions.moveTo(pos.x, pos.y, 0.2f),
                     UnitView.Actions.colorTo(new Color(1, 1, 1, 1), 0.4f)
             ));
             return;
         }
 
         AStar.AStarNode path = findPath(unit, boardPosition);
         if (path != null && path.gx <= unit.data.movesLeft) {
 
             unit.data.movesLeft -= path.gx;
             unit.data.movesThisTurn += path.gx;
 
             unit.view.lastBoardPosition = unit.view.boardPosition;
             unit.view.boardPosition = boardPosition;
 
             Array<Vector2> nodes = new Array<Vector2>();
             AStar.AStarNode cur = path;
             while (cur.parent != null) {  // don't add the last node, it is the starting position
                 nodes.add(cur.location);
                 cur = cur.parent;
             }
 
             while (nodes.size > 0) {
                 Vector2 boardPos = nodes.pop();
                 unit.view.addToPath(boardPos);
                 Vector2 pos = gameController.getMap().boardToMapCoords(boardPos.x, boardPos.y);
                 unit.view.addAction(UnitView.Actions.moveTo(pos.x, pos.y, 1 / unit.data.moveSpeed));
             }
             unit.data.modified = TimeUtils.millis();
         }
     }
 
     AStar.AStarNode findPath(Unit unit, Vector2 boardPosition) {
         return AStar.getPath(unit.getView().getBoardPosition(), boardPosition, gameController, (int)unit.data.movesLeft);
     }
 
     public void activateAbility(Unit unit) {
         if (unit.data.ability.equals("stealth")) {
             // cant re-enter stealth on the same turn or enter after firing or moving twice
             if (!unit.data.stealthActive && (unit.data.stealthEntered || unit.data.attacksLeft != unit.data.attackSpeed || unit.data.movesThisTurn > Math.floor(unit.data.moveSpeed * 0.5f)))
                 return; // cant re-enter stealth this turn
 
             unit.data.stealthActive = !unit.data.stealthActive;
             if (!unit.data.building) {
                 if (unit.data.stealthActive) {
                     unit.data.stealthEntered = true;
 
                     unit.data.movesLeft = (float)Math.floor(unit.data.moveSpeed * 0.5f) - unit.data.movesThisTurn; //- unit.data.moveSpeed - unit.data.movesLeft;
                     if (unit.data.movesLeft < 0) unit.data.movesLeft = 0;
                 } else {
                     unit.data.movesLeft = unit.data.moveSpeed - unit.data.movesThisTurn; //(float)Math.floor(unit.data.moveSpeed * 0.5f - unit.data.movesLeft);
                 }
             }
             unit.data.modified = TimeUtils.millis();
         } else if (unit.data.ability.equals("mind-control")) {
             if (!unit.data.mindControlUsed && unit.data.mindControlUnit != null) {
                 applyDamage(unit.data.mindControlUnit, unit.data.mindControlUnit.data.curHP + unit.data.mindControlUnit.data.armor);
                 unit.data.mindControlUnit = null;
                 unit.data.modified = TimeUtils.millis();
                 unit.data.mindControlUsed = true;
                 unit.data.attacksLeft = 0;
             }
         }
     }
 
     public void applyDamage(Unit unit, float amount) {
         float damage = amount - unit.data.armor;
         if (damage > 0) {
             unit.data.curHP -= damage;
 
             // TODO figure out a better way to do this
             if (LevelScreen.getInstance() != null)
                 LevelScreen.getInstance().addFloatingLabel(String.format("-%d", (int)damage), unit.view.getX() + unit.view.getWidth() * 0.5f, unit.view.getY() + unit.view.getHeight() * 0.5f);
 
             unit.view.addAction(UnitView.Actions.sequence(
                     UnitView.Actions.colorTo(new Color(1.0f, 0.5f, 0.5f, 1.0f), 0.1f),
                     UnitView.Actions.colorTo(new Color(1.0f, 1.0f, 1.0f, 1.0f), 0.1f),
                     UnitView.Actions.colorTo(new Color(1.0f, 0.5f, 0.5f, 1.0f), 0.1f),
                     UnitView.Actions.colorTo(new Color(1.0f, 1.0f, 1.0f, 1.0f), 0.1f)
             ));
 
             unit.data.modified = TimeUtils.millis();
         }
     }
 
     public boolean canAttack(Unit attacker, Unit defender) {
         return (
                 attacker.data.attacksLeft > 0
                 && HexMap.getMapDist(attacker.view.boardPosition, defender.view.boardPosition) <= attacker.data.attackRange
                 && canPlayerSee(attacker.owner, defender)
         );
     }
 
     public boolean canPlayerSee(Player player, Unit unit) {
         if (unit.owner == player || player == null) return true;
 
         for (Unit u: units.values()) {
             if (u.owner == player && canUnitSee(u, unit)) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean canPlayerDetect(Player player, Unit unit) {
         if (unit.owner == player || player == null) return true;
 
         for (Unit u: units.values()) {
            if (u.owner == player && unit.data.isDetector() && canUnitSee(u, unit)) {
                 return true;
             }
         }
         return false;
     }
 
     boolean canUnitSee(Unit looker, Unit target) {
         return (
                 (!target.data.isInvisible() || looker.data.isDetector())
                 && HexMap.getMapDist(looker.view.boardPosition, target.view.boardPosition) <= looker.data.sightRange
         );
     }
 
     void findNewOwner(Unit unit) {
         Player p = null;
         unit.data.capUnitCount = 0;
         Vector2[] adjacent = HexMap.getAdjacent(unit.view.boardPosition);
         for (Unit u: units.values()) {
             if (u == unit || u.owner == null || u.data.building)
                 continue;
 
             boolean adj = false;
             for (Vector2 pos: adjacent) {
                 if (pos.epsilonEquals(u.view.boardPosition, 0.1f)) {
                     adj = true;
                     break;
                 }
             }
             if (!adj)
                 continue;
 
             if (p == null || p == u.owner) {
                 p = u.owner;
                 unit.data.capUnitCount++;
             } else {
                 unit.data.uncontested = null;
                 unit.data.capUnitCount = 0;
                 return;
             }
         }
         unit.data.uncontested = p;
     }
 }
