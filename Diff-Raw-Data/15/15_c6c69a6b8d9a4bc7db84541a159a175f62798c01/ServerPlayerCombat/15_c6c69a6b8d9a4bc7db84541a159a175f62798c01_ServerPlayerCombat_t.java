 package net.sf.freecol.server.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import net.sf.freecol.common.debug.FreeColDebugger;
 import net.sf.freecol.common.model.Ability;
 import net.sf.freecol.common.model.AbstractGoods;
 import net.sf.freecol.common.model.Building;
 import net.sf.freecol.common.model.Colony;
 import net.sf.freecol.common.model.CombatModel;
 import net.sf.freecol.common.model.EquipmentType;
 import net.sf.freecol.common.model.FreeColGameObject;
 import net.sf.freecol.common.model.Game;
 import net.sf.freecol.common.model.Goods;
 import net.sf.freecol.common.model.HistoryEvent;
 import net.sf.freecol.common.model.IndianSettlement;
 import net.sf.freecol.common.model.Location;
 import net.sf.freecol.common.model.ModelMessage;
 import net.sf.freecol.common.model.Modifier;
 import net.sf.freecol.common.model.Player;
 import net.sf.freecol.common.model.Settlement;
 import net.sf.freecol.common.model.StringTemplate;
 import net.sf.freecol.common.model.Tension;
 import net.sf.freecol.common.model.Tile;
 import net.sf.freecol.common.model.Unit;
 import net.sf.freecol.common.model.UnitType;
 import net.sf.freecol.common.model.WorkLocation;
 import net.sf.freecol.common.model.CombatModel.CombatResult;
 import net.sf.freecol.common.model.Player.Stance;
 import net.sf.freecol.common.model.UnitTypeChange.ChangeType;
 import net.sf.freecol.common.networking.LootCargoMessage;
 import net.sf.freecol.common.util.Utils;
 import net.sf.freecol.server.control.ChangeSet;
 import net.sf.freecol.server.control.ChangeSet.See;
 
 public class ServerPlayerCombat extends ServerPlayer implements ServerModelObject{
 	
 	
 	  /**
      * Combat.
      *
      * @param attacker The <code>FreeColGameObject</code> that is attacking.
      * @param defender The <code>FreeColGameObject</code> that is defending.
      * @param crs A list of <code>CombatResult</code>s defining the result.
      * @param random A pseudo-random number source.
      * @param cs A <code>ChangeSet</code> to update.
      */
 	
 	private static final Logger logger = Logger.getLogger(ServerPlayer.class.getName());
 	
 	FreeColGameObject attacker;
     FreeColGameObject defender;
     List<CombatResult> crs;
     Random random;
     ChangeSet cs;
     
    public ServerPlayerCombat(Game game){
    	super(game);
     	
        //implicitly call spComabt in the constructor ?? Not right now
        // csCombat( attacker, defender, crs, random, cs);
     	
     }
     
     public void csCombat(FreeColGameObject attacker,
                          FreeColGameObject defender,
                          List<CombatResult> crs,
                          Random random,
                          ChangeSet cs) throws IllegalStateException {
     	
     	
         CombatModel combatModel = getGame().getCombatModel();
         boolean isAttack = combatModel.combatIsAttack(attacker, defender);
         boolean isBombard = combatModel.combatIsBombard(attacker, defender);
         Unit attackerUnit = null;
         Settlement attackerSettlement = null;
         Tile attackerTile = null;
         Unit defenderUnit = null;
         ServerPlayer defenderPlayer = null;
         Tile defenderTile = null;
         if (isAttack) {
             attackerUnit = (Unit) attacker;
             attackerTile = attackerUnit.getTile();
             defenderUnit = (Unit) defender;
             defenderPlayer = (ServerPlayer) defenderUnit.getOwner();
             defenderTile = defenderUnit.getTile();
             boolean bombard = attackerUnit.hasAbility(Ability.BOMBARD);
             cs.addAttribute(See.only(this), "sound",
                 (attackerUnit.isNaval()) ? "sound.attack.naval"
                 : (bombard) ? "sound.attack.artillery"
                 : (attackerUnit.isMounted()) ? "sound.attack.mounted"
                 : "sound.attack.foot");
             if (attackerUnit.getOwner().isIndian()
                 && defenderPlayer.isEuropean()
                 && defenderUnit.getLocation().getColony() != null
                 && !defenderPlayer.atWarWith(attackerUnit.getOwner())) {
                 StringTemplate attackerNation
                     = attackerUnit.getApparentOwnerName();
                 Colony colony = defenderUnit.getLocation().getColony();
                 cs.addMessage(See.only(defenderPlayer),
                     new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                         "model.unit.indianSurprise", colony)
                     .addStringTemplate("%nation%", attackerNation)
                     .addName("%colony%", colony.getName()));
             }
         } else if (isBombard) {
             attackerSettlement = (Settlement) attacker;
             attackerTile = attackerSettlement.getTile();
             defenderUnit = (Unit) defender;
             defenderPlayer = (ServerPlayer) defenderUnit.getOwner();
             defenderTile = defenderUnit.getTile();
             cs.addAttribute(See.only(this), "sound", "sound.attack.bombard");
         } else {
             throw new IllegalStateException("Bogus combat");
         }
 
         // If the combat results were not specified (usually the case),
         // query the combat model.
         if (crs == null) {
             crs = combatModel.generateAttackResult(random, attacker, defender);
         }
         if (crs.isEmpty()) {
             throw new IllegalStateException("empty attack result");
         }
         // Extract main result, insisting it is one of the fundamental cases,
         // and add the animation.
         // Set vis so that loser always sees things.
         // TODO: Bombard animations
         See vis; // Visibility that insists on the loser seeing the result.
         CombatResult result = crs.remove(0);
         switch (result) {
         case NO_RESULT:
             vis = See.perhaps();
             break; // Do not animate if there is no result.
         case WIN:
             vis = See.perhaps().always(defenderPlayer);
             if (isAttack) {
                 if (attackerTile == null || defenderTile == null
                     || attackerTile == defenderTile
                     || !attackerTile.isAdjacent(defenderTile)) {
                     logger.warning("Bogus attack from " + attackerTile
                         + " to " + defenderTile
                         + "\n" + FreeColDebugger.stackTraceToString());
                 } else {
                     cs.addAttack(vis, attackerUnit, defenderUnit,
                                  attackerTile, defenderTile, true);
                 }
             }
             break;
         case LOSE:
             vis = See.perhaps().always(this);
             if (isAttack) {
                 if (attackerTile == null || defenderTile == null
                     || attackerTile == defenderTile
                     || !attackerTile.isAdjacent(defenderTile)) {
                     logger.warning("Bogus attack from " + attackerTile
                         + " to " + defenderTile
                         + "\n" + FreeColDebugger.stackTraceToString());
                 } else {
                     cs.addAttack(vis, attackerUnit, defenderUnit,
                                  attackerTile, defenderTile, false);
                 }
             }
             break;
         default:
             throw new IllegalStateException("generateAttackResult returned: "
                                             + result);
         }
         // Now process the details.
         boolean attackerTileDirty = false;
         boolean defenderTileDirty = false;
         boolean moveAttacker = false;
         boolean burnedNativeCapital = false;
         Settlement settlement = defenderTile.getSettlement();
         Colony colony = defenderTile.getColony();
         IndianSettlement natives = (settlement instanceof IndianSettlement)
             ? (IndianSettlement) settlement
             : null;
         int attackerTension = 0;
         int defenderTension = 0;
         for (CombatResult cr : crs) {
             boolean ok;
             switch (cr) {
             case AUTOEQUIP_UNIT:
                 ok = isAttack && settlement != null;
                 if (ok) {
                     csAutoequipUnit(defenderUnit, settlement, cs);
                 }
                 break;
             case BURN_MISSIONS:
                 ok = isAttack && result == CombatResult.WIN
                     && natives != null
                     && isEuropean() && defenderPlayer.isIndian();
                 if (ok) {
                     defenderTileDirty |= natives.getMissionary(this) != null;
                     csBurnMissions(attackerUnit, natives, cs);
                 }
                 break;
             case CAPTURE_AUTOEQUIP:
                 ok = isAttack && result == CombatResult.WIN
                     && settlement != null
                     && defenderPlayer.isEuropean();
                 if (ok) {
                     csCaptureAutoEquip(attackerUnit, defenderUnit, cs);
                     attackerTileDirty = defenderTileDirty = true;
                 }
                 break;
             case CAPTURE_COLONY:
                 ok = isAttack && result == CombatResult.WIN
                     && colony != null
                     && isEuropean() && defenderPlayer.isEuropean();
                 if (ok) {
                     csCaptureColony(attackerUnit, colony, random, cs);
                     attackerTileDirty = defenderTileDirty = false;
                     moveAttacker = true;
                     defenderTension += Tension.TENSION_ADD_MAJOR;
                 }
                 break;
             case CAPTURE_CONVERT:
                 ok = isAttack && result == CombatResult.WIN
                     && natives != null
                     && isEuropean() && defenderPlayer.isIndian();
                 if (ok) {
                     csCaptureConvert(attackerUnit, natives, random, cs);
                     attackerTileDirty = true;
                 }
                 break;
             case CAPTURE_EQUIP:
                 ok = isAttack && result != CombatResult.NO_RESULT;
                 if (ok) {
                     if (result == CombatResult.WIN) {
                         csCaptureEquip(attackerUnit, defenderUnit, cs);
                     } else {
                         csCaptureEquip(defenderUnit, attackerUnit, cs);
                     }
                     attackerTileDirty = defenderTileDirty = true;
                 }
                 break;
             case CAPTURE_UNIT:
                 ok = isAttack && result != CombatResult.NO_RESULT;
                 if (ok) {
                     if (result == CombatResult.WIN) {
                         csCaptureUnit(attackerUnit, defenderUnit, cs);
                     } else {
                         csCaptureUnit(defenderUnit, attackerUnit, cs);
                     }
                     attackerTileDirty = defenderTileDirty = true;
                 }
                 break;
             case DAMAGE_COLONY_SHIPS:
                 ok = isAttack && result == CombatResult.WIN
                     && colony != null;
                 if (ok) {
                     csDamageColonyShips(attackerUnit, colony, cs);
                     defenderTileDirty = true;
                 }
                 break;
             case DAMAGE_SHIP_ATTACK:
                 ok = isAttack && result != CombatResult.NO_RESULT
                     && ((result == CombatResult.WIN) ? defenderUnit
                         : attackerUnit).isNaval();
                 if (ok) {
                     if (result == CombatResult.WIN) {
                         csDamageShipAttack(attackerUnit, defenderUnit, cs);
                         defenderTileDirty = true;
                     } else {
                         csDamageShipAttack(defenderUnit, attackerUnit, cs);
                         attackerTileDirty = true;
                     }
                 }
                 break;
             case DAMAGE_SHIP_BOMBARD:
                 ok = isBombard && result == CombatResult.WIN
                     && defenderUnit.isNaval();
                 if (ok) {
                     csDamageShipBombard(attackerSettlement, defenderUnit, cs);
                     defenderTileDirty = true;
                 }
                 break;
             case DEMOTE_UNIT:
                 ok = isAttack && result != CombatResult.NO_RESULT;
                 if (ok) {
                     if (result == CombatResult.WIN) {
                         csDemoteUnit(attackerUnit, defenderUnit, cs);
                         defenderTileDirty = true;
                     } else {
                         csDemoteUnit(defenderUnit, attackerUnit, cs);
                         attackerTileDirty = true;
                     }
                 }
                 break;
             case DESTROY_COLONY:
                 ok = isAttack && result == CombatResult.WIN
                     && colony != null
                     && isIndian() && defenderPlayer.isEuropean();
                 if (ok) {
                     csDestroyColony(attackerUnit, colony, random, cs);
                     attackerTileDirty = defenderTileDirty = true;
                     moveAttacker = true;
                     attackerTension -= Tension.TENSION_ADD_NORMAL;
                     defenderTension += Tension.TENSION_ADD_MAJOR;
                 }
                 break;
             case DESTROY_SETTLEMENT:
                 ok = isAttack && result == CombatResult.WIN
                     && natives != null
                     && defenderPlayer.isIndian();
                 if (ok) {
                     burnedNativeCapital = settlement.isCapital();
                     csDestroySettlement(attackerUnit, natives, random, cs);
                     attackerTileDirty = defenderTileDirty = true;
                     moveAttacker = true;
                     attackerTension -= Tension.TENSION_ADD_NORMAL;
                     if (!burnedNativeCapital) {
                         defenderTension += Tension.TENSION_ADD_MAJOR;
                     }
                 }
                 break;
             case EVADE_ATTACK:
                 ok = isAttack && result == CombatResult.NO_RESULT
                     && defenderUnit.isNaval();
                 if (ok) {
                     csEvadeAttack(attackerUnit, defenderUnit, cs);
                 }
                 break;
             case EVADE_BOMBARD:
                 ok = isBombard && result == CombatResult.NO_RESULT
                     && defenderUnit.isNaval();
                 if (ok) {
                     csEvadeBombard(attackerSettlement, defenderUnit, cs);
                 }
                 break;
             case LOOT_SHIP:
                 ok = isAttack && result != CombatResult.NO_RESULT
                     && attackerUnit.isNaval() && defenderUnit.isNaval();
                 if (ok) {
                     if (result == CombatResult.WIN) {
                         csLootShip(attackerUnit, defenderUnit, cs);
                     } else {
                         csLootShip(defenderUnit, attackerUnit, cs);
                     }
                 }
                 break;
             case LOSE_AUTOEQUIP:
                 ok = isAttack && result == CombatResult.WIN
                     && settlement != null
                     && defenderPlayer.isEuropean();
                 if (ok) {
                     csLoseAutoEquip(attackerUnit, defenderUnit, cs);
                     defenderTileDirty = true;
                 }
                 break;
             case LOSE_EQUIP:
                 ok = isAttack && result != CombatResult.NO_RESULT;
                 if (ok) {
                     if (result == CombatResult.WIN) {
                         csLoseEquip(attackerUnit, defenderUnit, cs);
                         defenderTileDirty = true;
                     } else {
                         csLoseEquip(defenderUnit, attackerUnit, cs);
                         attackerTileDirty = true;
                     }
                 }
                 break;
             case PILLAGE_COLONY:
                 ok = isAttack && result == CombatResult.WIN
                     && colony != null
                     && isIndian() && defenderPlayer.isEuropean();
                 if (ok) {
                     csPillageColony(attackerUnit, colony, random, cs);
                     defenderTileDirty = true;
                     attackerTension -= Tension.TENSION_ADD_NORMAL;
                 }
                 break;
             case PROMOTE_UNIT:
                 ok = isAttack && result != CombatResult.NO_RESULT;
                 if (ok) {
                     if (result == CombatResult.WIN) {
                         csPromoteUnit(attackerUnit, defenderUnit, cs);
                         attackerTileDirty = true;
                     } else {
                         csPromoteUnit(defenderUnit, attackerUnit, cs);
                         defenderTileDirty = true;
                     }
                 }
                 break;
             case SINK_COLONY_SHIPS:
                 ok = isAttack && result == CombatResult.WIN
                     && colony != null;
                 if (ok) {
                     csSinkColonyShips(attackerUnit, colony, cs);
                     defenderTileDirty = true;
                 }
                 break;
             case SINK_SHIP_ATTACK:
                 ok = isAttack && result != CombatResult.NO_RESULT
                     && ((result == CombatResult.WIN) ? defenderUnit
                         : attackerUnit).isNaval();
                 if (ok) {
                     if (result == CombatResult.WIN) {
                         csSinkShipAttack(attackerUnit, defenderUnit, cs);
                         defenderTileDirty = true;
                     } else {
                         csSinkShipAttack(defenderUnit, attackerUnit, cs);
                         attackerTileDirty = true;
                     }
                 }
                 break;
             case SINK_SHIP_BOMBARD:
                 ok = isBombard && result == CombatResult.WIN
                     && defenderUnit.isNaval();
                 if (ok) {
                     csSinkShipBombard(attackerSettlement, defenderUnit, cs);
                     defenderTileDirty = true;
                 }
                 break;
             case SLAUGHTER_UNIT:
                 ok = isAttack && result != CombatResult.NO_RESULT;
                 if (ok) {
                     if (result == CombatResult.WIN) {
                         csSlaughterUnit(attackerUnit, defenderUnit, cs);
                         defenderTileDirty = true;
                         attackerTension -= Tension.TENSION_ADD_NORMAL;
                         defenderTension += getSlaughterTension(defenderUnit);
                     } else {
                         csSlaughterUnit(defenderUnit, attackerUnit, cs);
                         attackerTileDirty = true;
                         attackerTension += getSlaughterTension(attackerUnit);
                         defenderTension -= Tension.TENSION_ADD_NORMAL;
                     }
                 }
                 break;
             default:
                 ok = false;
                 break;
             }
             if (!ok) {
                 throw new IllegalStateException("Attack (result=" + result
                                                 + ") has bogus subresult: "
                                                 + cr);
             }
         }
 
         // Handle stance and tension.
         // - Privateers do not provoke stance changes but can set the
         //     attackedByPrivateers flag
         // - Attacks among Europeans imply war
         // - Burning of a native capital results in surrender
         // - Other attacks involving natives do not imply war, but
         //     changes in Tension can drive Stance, however this is
         //     decided by the native AI in their turn so just adjust tension.
         if (attacker.hasAbility(Ability.PIRACY)) {
             if (!defenderPlayer.getAttackedByPrivateers()) {
                 defenderPlayer.setAttackedByPrivateers(true);
                 cs.addPartial(See.only(defenderPlayer), defenderPlayer,
                               "attackedByPrivateers");
             }
         } else if (defender.hasAbility(Ability.PIRACY)) {
             ; // do nothing
         } else if (burnedNativeCapital) {
             defenderPlayer.getTension(this).setValue(Tension.SURRENDERED);
             cs.add(See.perhaps().always(this), defenderPlayer); // TODO: just the tension
             csChangeStance(Stance.PEACE, defenderPlayer, true, cs);
             for (IndianSettlement is : defenderPlayer.getIndianSettlements()) {
                 if (is.hasContacted(this)) {
                     is.getAlarm(this).setValue(Tension.SURRENDERED);
                     // Only update attacker with settlements that have
                     // been seen, as contact can occur with its members.
                     if (is.getTile().isExploredBy(this)) {
                         cs.add(See.perhaps().always(this), is);
                     } else {
                         cs.add(See.only(defenderPlayer), is);
                     }
                 }
             }
         } else if (isEuropean() && defenderPlayer.isEuropean()) {
             csChangeStance(Stance.WAR, defenderPlayer, true, cs);
         } else { // At least one player is non-European
             if (isEuropean()) {
                 csChangeStance(Stance.WAR, defenderPlayer, true, cs);
             } else if (isIndian()) {
                 if (result == CombatResult.WIN) {
                     attackerTension -= Tension.TENSION_ADD_MINOR;
                 } else if (result == CombatResult.LOSE) {
                     attackerTension += Tension.TENSION_ADD_MINOR;
                 }
             }
             if (defenderPlayer.isEuropean()) {
                 defenderPlayer.csChangeStance(Stance.WAR, this, true, cs);
             } else if (defenderPlayer.isIndian()) {
                 if (result == CombatResult.WIN) {
                     defenderTension += Tension.TENSION_ADD_MINOR;
                 } else if (result == CombatResult.LOSE) {
                     defenderTension -= Tension.TENSION_ADD_MINOR;
                 }
             }
             if (attackerTension != 0) {
                 cs.add(See.only(null).perhaps(defenderPlayer),
                        modifyTension(defenderPlayer, attackerTension));
             }
             if (defenderTension != 0) {
                 cs.add(See.only(null).perhaps(this),
                        defenderPlayer.modifyTension(this, defenderTension));
             }
         }
 
         // Move the attacker if required.
         if (moveAttacker) {
             attackerUnit.setMovesLeft(attackerUnit.getInitialMovesLeft());
             ((ServerUnit) attackerUnit).csMove(defenderTile, random, cs);
             attackerUnit.setMovesLeft(0);
             // Move adds in updates for the tiles, but...
             attackerTileDirty = defenderTileDirty = false;
             // ...with visibility of perhaps().
             // Thus the defender might see the change,
             // but because its settlement is gone it also might not.
             // So add in another defender-specific update.
             // The worst that can happen is a duplicate update.
             cs.add(See.only(defenderPlayer), defenderTile);
         } else if (isAttack) {
             // The Revenger unit can attack multiple times, so spend
             // at least the eventual cost of moving to the tile.
             // Other units consume the entire move.
             if (attacker.hasAbility("model.ability.multipleAttacks")) {
                 int movecost = attackerUnit.getMoveCost(defenderTile);
                 attackerUnit.setMovesLeft(attackerUnit.getMovesLeft()
                                           - movecost);
             } else {
                 attackerUnit.setMovesLeft(0);
             }
             if (!attackerTileDirty) {
                 cs.addPartial(See.only(this), attacker, "movesLeft");
             }
         }
 
         // Make sure we always update the attacker and defender tile
         // if it is not already done yet.
         if (attackerTileDirty) cs.add(vis, attackerTile);
         if (defenderTileDirty) cs.add(vis, defenderTile);
     }//end method
     
  
     private int getSlaughterTension(Unit loser) {
         // Tension rises faster when units die.
         Settlement settlement = loser.getSettlement();
         if (settlement != null) {
             if (settlement instanceof IndianSettlement) {
                 return (((IndianSettlement) settlement).isCapital())
                     ? Tension.TENSION_ADD_CAPITAL_ATTACKED
                     : Tension.TENSION_ADD_SETTLEMENT_ATTACKED;
             } else {
                 return Tension.TENSION_ADD_NORMAL;
             }
         } else { // attack in the open
             return (loser.getIndianSettlement() != null)
                 ? Tension.TENSION_ADD_UNIT_DESTROYED
                 : Tension.TENSION_ADD_MINOR;
         }
     }
     
     /**
      * Notifies of automatic arming.
      *
      * @param unit The <code>Unit</code> that is auto-equipping.
      * @param settlement The <code>Settlement</code> being defended.
      * @param cs A <code>ChangeSet</code> to update.
      */
     private void csAutoequipUnit(Unit unit, Settlement settlement,
                                  ChangeSet cs) {
         ServerPlayer player = (ServerPlayer) unit.getOwner();
         cs.addMessage(See.only(player),
             new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                 "model.unit.automaticDefence", unit)
             .addStringTemplate("%unit%", unit.getLabel())
             .addName("%colony%", settlement.getName()));
     }
     /**
      * Defender autoequips but loses and attacker captures the equipment.
      *
      * @param attacker The <code>Unit</code> that attacked.
      * @param defender The <code>Unit</code> that defended and loses equipment.
      * @param cs A <code>ChangeSet</code> to update.
      */
     private void csCaptureAutoEquip(Unit attacker, Unit defender,
                                     ChangeSet cs) {
         EquipmentType equip
             = defender.getBestCombatEquipmentType(defender.getAutomaticEquipment());
         csLoseAutoEquip(attacker, defender, cs);
         csCaptureEquipment(attacker, defender, equip, cs);
     }
     /**
      * Captures a colony.
      *
      * @param attacker The attacking <code>Unit</code>.
      * @param colony The <code>Colony</code> to capture.
      * @param random A pseudo-random number source.
      * @param cs The <code>ChangeSet</code> to update.
      */
     private void csCaptureColony(Unit attacker, Colony colony, Random random,
                                  ChangeSet cs) {
         Game game = attacker.getGame();
         ServerPlayer attackerPlayer = (ServerPlayer) attacker.getOwner();
         StringTemplate attackerNation = attackerPlayer.getNationName();
         ServerPlayer colonyPlayer = (ServerPlayer) colony.getOwner();
         StringTemplate colonyNation = colonyPlayer.getNationName();
         Tile tile = colony.getTile();
         List<Unit> units = new ArrayList<Unit>();
         units.addAll(colony.getUnitList());
         units.addAll(tile.getUnitList());
         int plunder = colony.getPlunder(attacker, random);
 
         // Handle history and messages before colony handover
         cs.addHistory(attackerPlayer,
                       new HistoryEvent(game.getTurn(),
                                        HistoryEvent.EventType.CONQUER_COLONY)
                       .addStringTemplate("%nation%", colonyNation)
                       .addName("%colony%", colony.getName()));
         cs.addHistory(colonyPlayer,
                       new HistoryEvent(game.getTurn(),
                                        HistoryEvent.EventType.COLONY_CONQUERED)
                       .addStringTemplate("%nation%", attackerNation)
                       .addName("%colony%", colony.getName()));
         cs.addMessage(See.only(attackerPlayer),
                       new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                                        "model.unit.colonyCaptured",
                                        colony)
                       .addName("%colony%", colony.getName())
                       .addAmount("%amount%", plunder));
         cs.addMessage(See.only(colonyPlayer),
                       new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                                        "model.unit.colonyCapturedBy",
                                        colony.getTile())
                       .addName("%colony%", colony.getName())
                       .addAmount("%amount%", plunder)
                       .addStringTemplate("%player%", attackerNation));
 
         // Allocate some plunder
         if (plunder > 0) {
             attackerPlayer.modifyGold(plunder);
             colonyPlayer.modifyGold(-plunder);
             cs.addPartial(See.only(attackerPlayer), attackerPlayer, "gold");
             cs.addPartial(See.only(colonyPlayer), colonyPlayer, "gold");
         }
 
         // Hand over the colony.
         colony.changeOwner(attackerPlayer);
         // Remove goods party modifiers as they apply to a different monarch.
         for (Modifier m : colony.getModifiers()) {
             if ("model.modifier.colonyGoodsParty".equals(m.getSource())) {
                 colony.removeModifier(m);
             }
         }
 
         // Inform former owner of loss of owned tiles, and process possible
         // increase in line of sight.  Leave other exploration etc to csMove.
         for (Tile t : colony.getOwnedTiles()) {
             if (t == tile) continue;
             cs.add(See.perhaps().always(colonyPlayer), t);
         }
         if (colony.getLineOfSight() > attacker.getLineOfSight()) {
             for (Tile t : tile.getSurroundingTiles(attacker.getLineOfSight(),
                                                    colony.getLineOfSight())) {
                 // New owner has now explored within settlement line of sight.
                 attackerPlayer.setExplored(t);
                 cs.add(See.only(attackerPlayer), t);
             }
         }
 
         // Inform the former owner of loss of units.
         cs.addRemoves(See.only(colonyPlayer), null, units);
 
         cs.addAttribute(See.only(attackerPlayer), "sound",
                         "sound.event.captureColony");
     }
 
     /**
      * Extracts a convert from a native settlement.
      *
      * @param attacker The <code>Unit</code> that is attacking.
      * @param natives The <code>IndianSettlement</code> under attack.
      * @param random A pseudo-random number source.
      * @param cs A <code>ChangeSet</code> to update.
      */
     private void csCaptureConvert(Unit attacker, IndianSettlement natives,
                                   Random random, ChangeSet cs) {
         ServerPlayer attackerPlayer = (ServerPlayer) attacker.getOwner();
         StringTemplate convertNation = natives.getOwner().getNationName();
         List<UnitType> converts = getGame().getSpecification()
             .getUnitTypesWithAbility("model.ability.convert");
         UnitType type = Utils.getRandomMember(logger, "Choose convert",
                                               converts, random);
         Unit convert = natives.getUnitList().get(0);
         convert.clearEquipment();
 
         cs.addMessage(See.only(attackerPlayer),
                       new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                                        "model.unit.newConvertFromAttack",
                                        convert)
                       .addStringTemplate("%nation%", convertNation)
                       .addStringTemplate("%unit%", convert.getLabel()));
 
         convert.setOwner(attacker.getOwner());
         // do not change nationality: convert was forcibly captured and wants to run away
         convert.setType(type);
         convert.setLocation(attacker.getTile());
     }
 
     /**
      * Burns a players missions.
      *
      * @param attacker The <code>Unit</code> that attacked.
      * @param settlement The <code>IndianSettlement</code> that was attacked.
      * @param cs The <code>ChangeSet</code> to update.
      */
 
     private void csBurnMissions(Unit attacker, IndianSettlement settlement,
     		ChangeSet cs) {
     	ServerPlayer attackerPlayer = (ServerPlayer) attacker.getOwner();
     	StringTemplate attackerNation = attackerPlayer.getNationName();
     	ServerPlayer nativePlayer = (ServerPlayer) settlement.getOwner();
     	StringTemplate nativeNation = nativePlayer.getNationName();
 
     	// Message only for the European player
     	cs.addMessage(See.only(attackerPlayer),
     			new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
     					"model.unit.burnMissions", attacker, settlement)
     	.addStringTemplate("%nation%", attackerNation)
     	.addStringTemplate("%enemyNation%", nativeNation));
 
     	// Burn down the missions
     	for (IndianSettlement s : nativePlayer.getIndianSettlements()) {
     		if (s.getMissionary(attackerPlayer) != null) {
     			csKillMissionary(s, null, cs);
     		}
     	}
 }
     
     
     /**
      * Captures equipment.
      *
      * @param winner The <code>Unit</code> that captures equipment.
      * @param loser The <code>Unit</code> that defended and loses equipment.
      * @param cs A <code>ChangeSet</code> to update.
      */
     private void csCaptureEquip(Unit winner, Unit loser, ChangeSet cs) {
         EquipmentType equip
             = loser.getBestCombatEquipmentType(loser.getEquipment());
         csLoseEquip(winner, loser, cs);
         csCaptureEquipment(winner, loser, equip, cs);
     }
     /**
      * Capture equipment.
      *
      * @param winner The <code>Unit</code> that is capturing equipment.
      * @param loser The <code>Unit</code> that is losing equipment.
      * @param equip The <code>EquipmentType</code> to capture.
      * @param cs A <code>ChangeSet</code> to update.
      */
     protected void csCaptureEquipment(Unit winner, Unit loser,
     		EquipmentType equip, ChangeSet cs) {
     	ServerPlayer winnerPlayer = (ServerPlayer) winner.getOwner();
     	ServerPlayer loserPlayer = (ServerPlayer) loser.getOwner();
     	if ((equip = winner.canCaptureEquipment(equip, loser)) != null) {
     		// TODO: what if winner captures equipment that is
     		// incompatible with their current equipment?
     		// Currently, can-not-happen, so ignoring the return from
     		// changeEquipment.  Beware.
     		winner.changeEquipment(equip, 1);
 
     		// Currently can not capture equipment back so this only
     		// makes sense for native players, and the message is
     		// native specific.
     		if (winnerPlayer.isIndian()) {
     			StringTemplate winnerNation = winnerPlayer.getNationName();
     			cs.addMessage(See.only(loserPlayer),
     					new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
     							"model.unit.equipmentCaptured",
     							winnerPlayer)
     			.addStringTemplate("%nation%", winnerNation)
     			.add("%equipment%", equip.getNameKey()));
 
     			// CHEAT: Immediately transferring the captured goods
     			// back to a potentially remote settlement is pretty
     			// dubious.  Apparently Col1 did it.  Better would be
     			// to give the capturing unit a go-home-with-plunder mission.
     			IndianSettlement settlement = winner.getIndianSettlement();
     			if (settlement != null) {
     				for (AbstractGoods goods : equip.getRequiredGoods()) {
     					settlement.addGoods(goods);
     					winnerPlayer.logCheat("teleported " + goods.toString()
     							+ " back to " + settlement.getName());
     				}
     				cs.add(See.only(winnerPlayer), settlement);
     			}
     		}
     	}
 }
     
     /**
      * Slaughter a unit.
      *
      * @param winner The <code>Unit</code> that is slaughtering.
      * @param loser The <code>Unit</code> to slaughter.
      * @param cs A <code>ChangeSet</code> to update.
      */
     private void csSlaughterUnit(Unit winner, Unit loser, ChangeSet cs) {
         ServerPlayer winnerPlayer = (ServerPlayer) winner.getOwner();
         StringTemplate winnerNation = winner.getApparentOwnerName();
         StringTemplate winnerLocation = winner.getLocation()
             .getLocationNameFor(winnerPlayer);
         ServerPlayer loserPlayer = (ServerPlayer) loser.getOwner();
         StringTemplate loserNation = loser.getApparentOwnerName();
         StringTemplate loserLocation = loser.getLocation()
             .getLocationNameFor(loserPlayer);
         String messageId = loser.getType().getId() + ".destroyed";
 
         cs.addMessage(See.only(winnerPlayer),
             new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                 messageId, winner)
             .setDefaultId("model.unit.unitSlaughtered")
             .addStringTemplate("%nation%", loserNation)
             .addStringTemplate("%unit%", loser.getLabel())
             .addStringTemplate("%enemyNation%", winnerPlayer.getNationName())
             .addStringTemplate("%enemyUnit%", winner.getLabel())
             .addStringTemplate("%location%", winnerLocation));
         cs.addMessage(See.only(loserPlayer),
             new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                 messageId, loser.getTile())
             .setDefaultId("model.unit.unitSlaughtered")
             .addStringTemplate("%nation%", loserPlayer.getNationName())
             .addStringTemplate("%unit%", loser.getLabel())
             .addStringTemplate("%enemyNation%", winnerNation)
             .addStringTemplate("%enemyUnit%", winner.getLabel())
             .addStringTemplate("%location%", loserLocation));
         if (loserPlayer.isIndian() && loserPlayer.checkForDeath() == IS_DEAD) {
             StringTemplate nativeNation = loserPlayer.getNationName();
             cs.addGlobalHistory(getGame(),
                 new HistoryEvent(getGame().getTurn(),
                     HistoryEvent.EventType.DESTROY_NATION)
                 .addStringTemplate("%nation%", winnerNation)
                 .addStringTemplate("%nativeNation%", nativeNation));
         }
 
         // Transfer equipment, do not generate messages for the loser.
         EquipmentType equip;
         while ((equip = loser.getBestCombatEquipmentType(loser.getEquipment()))
                != null) {
             loser.changeEquipment(equip, -loser.getEquipmentCount(equip));
             csCaptureEquipment(winner, loser, equip, cs);
         }
 
         // Destroy unit.
         cs.addDispose(See.perhaps().always(loserPlayer),
             loser.getLocation(), loser);
     }
     
     
     /**
      * Capture a unit.
      *
      * @param winner A <code>Unit</code> that is capturing.
      * @param loser A <code>Unit</code> to capture.
      * @param cs A <code>ChangeSet</code> to update.
      */
     private void csCaptureUnit(Unit winner, Unit loser, ChangeSet cs) {
         ServerPlayer loserPlayer = (ServerPlayer) loser.getOwner();
         StringTemplate loserNation = loserPlayer.getNationName();
         StringTemplate loserLocation = loser.getLocation()
             .getLocationNameFor(loserPlayer);
         StringTemplate oldName = loser.getLabel();
         String messageId = loser.getType().getId() + ".captured";
         ServerPlayer winnerPlayer = (ServerPlayer) winner.getOwner();
         StringTemplate winnerNation = winnerPlayer.getNationName();
         StringTemplate winnerLocation = winner.getLocation()
             .getLocationNameFor(winnerPlayer);
 
         // Capture the unit
         UnitType type = loser.getTypeChange((winnerPlayer.isUndead())
                                             ? ChangeType.UNDEAD
                                             : ChangeType.CAPTURE, winnerPlayer);
         loser.setOwner(winnerPlayer);
         if (type != null) loser.setType(type);
         loser.setLocation(winner.getTile());
         loser.setState(Unit.UnitState.ACTIVE);
 
         // Winner message post-capture when it owns the loser
         cs.addMessage(See.only(winnerPlayer),
                       new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                                        messageId, loser)
                       .setDefaultId("model.unit.unitCaptured")
                       .addStringTemplate("%nation%", loserNation)
                       .addStringTemplate("%unit%", oldName)
                       .addStringTemplate("%enemyNation%", winnerNation)
                       .addStringTemplate("%enemyUnit%", winner.getLabel())
                       .addStringTemplate("%location%", winnerLocation));
         cs.addMessage(See.only(loserPlayer),
                       new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                                        messageId, loser.getTile())
                       .setDefaultId("model.unit.unitCaptured")
                       .addStringTemplate("%nation%", loserNation)
                       .addStringTemplate("%unit%", oldName)
                       .addStringTemplate("%enemyNation%", winnerNation)
                       .addStringTemplate("%enemyUnit%", winner.getLabel())
                       .addStringTemplate("%location%", loserLocation));
     }
     
     
     /**
      * Damage a building or a ship or steal some goods or gold.
      *
      * @param attacker The attacking <code>Unit</code>.
      * @param colony The <code>Colony</code> to pillage.
      * @param random A pseudo-random number source.
      * @param cs A <code>ChangeSet</code> to update.
      */
     protected void csPillageColony(Unit attacker, Colony colony,
                                  Random random, ChangeSet cs) {
         ServerPlayer attackerPlayer = (ServerPlayer) attacker.getOwner();
         StringTemplate attackerNation = attacker.getApparentOwnerName();
         ServerPlayer colonyPlayer = (ServerPlayer) colony.getOwner();
         StringTemplate colonyNation = colonyPlayer.getNationName();
 
         // Collect the damagable buildings, ships, movable goods.
         List<Building> buildingList = colony.getBurnableBuildingList();
         List<Unit> shipList = colony.getShipList();
         List<Goods> goodsList = colony.getLootableGoodsList();
 
         // Pick one, with one extra choice for stealing gold.
         int pillage = Utils.randomInt(logger, "Pillage choice", random,
             buildingList.size() + shipList.size() + goodsList.size()
             + ((colony.canBePlundered()) ? 1 : 0));
         if (pillage < buildingList.size()) {
             Building building = buildingList.get(pillage);
             csDamageBuilding(building, cs);
             cs.addMessage(See.only(colonyPlayer),
                 new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                     "model.unit.buildingDamaged", colony)
                 .add("%building%", building.getNameKey())
                 .addName("%colony%", colony.getName())
                 .addStringTemplate("%enemyNation%", attackerNation)
                 .addStringTemplate("%enemyUnit%", attacker.getLabel()));
         } else if (pillage < buildingList.size() + shipList.size()) {
             Unit ship = shipList.get(pillage - buildingList.size());
             if (ship.getRepairLocation() == null) {
                 csSinkShipAttack(attacker, ship, cs);
             } else {
                 csDamageShipAttack(attacker, ship, cs);
             }
         } else if (pillage < buildingList.size() + shipList.size()
                    + goodsList.size()) {
             Goods goods = goodsList.get(pillage - buildingList.size()
                 - shipList.size());
             goods.setAmount(Math.min(goods.getAmount() / 2, 50));
             colony.removeGoods(goods);
             if (attacker.canAdd(goods)) attacker.add(goods);
             cs.addMessage(See.only(colonyPlayer),
                 new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                     "model.unit.goodsStolen", colony, goods)
                 .addAmount("%amount%", goods.getAmount())
                 .add("%goods%", goods.getType().getNameKey())
                 .addName("%colony%", colony.getName())
                 .addStringTemplate("%enemyNation%", attackerNation)
                 .addStringTemplate("%enemyUnit%", attacker.getLabel()));
 
         } else {
             int plunder = Math.max(1, colony.getPlunder(attacker, random) / 5);
             colonyPlayer.modifyGold(-plunder);
             attackerPlayer.modifyGold(plunder);
             cs.addPartial(See.only(colonyPlayer), colonyPlayer, "gold");
             cs.addMessage(See.only(colonyPlayer),
                 new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                     "model.unit.indianPlunder", colony)
                 .addAmount("%amount%", plunder)
                 .addName("%colony%", colony.getName())
                 .addStringTemplate("%enemyNation%", attackerNation)
                 .addStringTemplate("%enemyUnit%", attacker.getLabel()));
         }
         cs.addMessage(See.all().except(colonyPlayer),
             new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                 "model.unit.indianRaid", colonyPlayer)
             .addStringTemplate("%nation%", attackerNation)
             .addName("%colony%", colony.getName())
             .addStringTemplate("%colonyNation%", colonyNation));
     }
     
     /**
      * Damage a building in a colony by downgrading it if possible and
      * destroying it otherwise. Workers in a destroyed building are
      * automatically assigned to other work locations.
      *
      * @param building a <code>Building</code> value
      * @param cs a <code>ChangeSet</code> value
      */
     private void csDamageBuilding(Building building, ChangeSet cs) {
         Colony colony = building.getColony();
         if (building.getType().getUpgradesFrom() == null) {
             // Eject units to any available work location.
             unit: for (Unit u : building.getUnitList()) {
                 for (WorkLocation wl : colony.getAvailableWorkLocations()) {
                     if (wl == building || !wl.canAdd(u)) continue;
                     u.setLocation(wl);
                     continue unit;
                 }
                 u.setLocation(colony.getTile());
             }
             colony.removeBuilding(building);
             cs.addDispose(See.only((ServerPlayer) colony.getOwner()), colony, building);
         } else if (building.canBeDamaged()) {
             building.damage();
         }
         if (isAI()) {
             colony.firePropertyChange(Colony.REARRANGE_WORKERS, true, false);
         }
     }
 
     /**
      * Damages all ships in a colony in preparation for capture.
      *
      * @param attacker The <code>Unit</code> that is damaging.
      * @param colony The <code>Colony</code> to damage ships in.
      * @param cs A <code>ChangeSet</code> to update.
      */
     private void csDamageColonyShips(Unit attacker, Colony colony,
                                      ChangeSet cs) {
         List<Unit> units = colony.getTile().getUnitList();
         while (!units.isEmpty()) {
             Unit unit = units.remove(0);
             if (unit.isNaval()) csDamageShipAttack(attacker, unit, cs);
         }
     }
 
     /**
      * Damage a ship through normal attack.
      *
      * @param attacker The attacker <code>Unit</code>.
      * @param ship The <code>Unit</code> which is a ship to damage.
      * @param cs A <code>ChangeSet</code> to update.
      */
     private void csDamageShipAttack(Unit attacker, Unit ship, ChangeSet cs) {
         ServerPlayer attackerPlayer = (ServerPlayer) attacker.getOwner();
         StringTemplate attackerNation = attacker.getApparentOwnerName();
         ServerPlayer shipPlayer = (ServerPlayer) ship.getOwner();
         Location repair = ship.getRepairLocation();
         StringTemplate repairLoc = repair.getLocationNameFor(shipPlayer);
         StringTemplate shipNation = ship.getApparentOwnerName();
 
         cs.addMessage(See.only(attackerPlayer),
             new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                 "model.unit.enemyShipDamaged", attacker)
             .addStringTemplate("%unit%", attacker.getLabel())
             .addStringTemplate("%enemyNation%", shipNation)
             .addStringTemplate("%enemyUnit%", ship.getLabel()));
         cs.addMessage(See.only(shipPlayer),
             new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                 "model.unit.shipDamaged", ship)
             .addStringTemplate("%unit%", ship.getLabel())
             .addStringTemplate("%enemyUnit%", attacker.getLabel())
             .addStringTemplate("%enemyNation%", attackerNation)
             .addStringTemplate("%repairLocation%", repairLoc));
 
         csDamageShip(ship, repair, cs);
     }
 
     /**
      * Damage a ship through bombard.
      *
      * @param settlement The attacker <code>Settlement</code>.
      * @param ship The <code>Unit</code> which is a ship to damage.
      * @param cs A <code>ChangeSet</code> to update.
      */
     private void csDamageShipBombard(Settlement settlement, Unit ship,
                                      ChangeSet cs) {
         ServerPlayer attackerPlayer = (ServerPlayer) settlement.getOwner();
         ServerPlayer shipPlayer = (ServerPlayer) ship.getOwner();
         Location repair = ship.getRepairLocation();
         StringTemplate repairLoc = repair.getLocationNameFor(shipPlayer);
         StringTemplate shipNation = ship.getApparentOwnerName();
 
         cs.addMessage(See.only(attackerPlayer),
             new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                 "model.unit.enemyShipDamagedByBombardment", settlement)
             .addName("%colony%", settlement.getName())
             .addStringTemplate("%nation%", shipNation)
             .addStringTemplate("%unit%", ship.getLabel()));
         cs.addMessage(See.only(shipPlayer),
             new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                 "model.unit.shipDamagedByBombardment", ship)
             .addName("%colony%", settlement.getName())
             .addStringTemplate("%unit%", ship.getLabel())
             .addStringTemplate("%repairLocation%", repairLoc));
 
         csDamageShip(ship, repair, cs);
     }
     
     /**
      * Damage a ship.
      *
      * @param ship The naval <code>Unit</code> to damage.
      * @param repair The <code>Location</code> to send it to.
      * @param cs A <code>ChangeSet</code> to update.
      */
     void csDamageShip(Unit ship, Location repair, ChangeSet cs) {
         ServerPlayer player = (ServerPlayer) ship.getOwner();
 
         // Lose the goods and units aboard
         for (Goods g : ship.getGoodsContainer().getCompactGoods()) {
             ship.remove(g);
         }
         for (Unit u : ship.getUnitList()) {
             ship.remove(u);
             cs.addDispose(See.only(player), null, u); // Only owner-visible
         }
 
         // Damage the ship and send it off for repair
         ship.setHitpoints(1);
         ship.setDestination(null);
         ship.setLocation((repair instanceof Colony) ? repair.getTile()
             : repair);
         ship.setState(Unit.UnitState.ACTIVE);
         ship.setMovesLeft(0);
         cs.add(See.only(player), (FreeColGameObject)repair);
     }
     
     /**
      * Demotes a unit.
      *
      * @param winner The <code>Unit</code> that won.
      * @param loser The <code>Unit</code> that lost and should be demoted.
      * @param cs A <code>ChangeSet</code> to update.
      */
     private void csDemoteUnit(Unit winner, Unit loser, ChangeSet cs) {
         ServerPlayer loserPlayer = (ServerPlayer) loser.getOwner();
         StringTemplate loserNation = loser.getApparentOwnerName();
         StringTemplate loserLocation = loser.getLocation()
             .getLocationNameFor(loserPlayer);
         StringTemplate oldName = loser.getLabel();
         String messageId = loser.getType().getId() + ".demoted";
         ServerPlayer winnerPlayer = (ServerPlayer) winner.getOwner();
         StringTemplate winnerNation = winner.getApparentOwnerName();
         StringTemplate winnerLocation = winner.getLocation()
             .getLocationNameFor(winnerPlayer);
 
         UnitType type = loser.getTypeChange(ChangeType.DEMOTION, loserPlayer);
         if (type == null || type == loser.getType()) {
             logger.warning("Demotion failed, type="
                 + ((type == null) ? "null" : "same type: " + type));
             return;
         }
         loser.setType(type);
 
         cs.addMessage(See.only(winnerPlayer),
             new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                 messageId, winner)
             .setDefaultId("model.unit.unitDemoted")
             .addStringTemplate("%nation%", loserNation)
             .addStringTemplate("%oldName%", oldName)
             .addStringTemplate("%unit%", loser.getLabel())
             .addStringTemplate("%enemyNation%", winnerPlayer.getNationName())
             .addStringTemplate("%enemyUnit%", winner.getLabel())
             .addStringTemplate("%location%", winnerLocation));
         cs.addMessage(See.only(loserPlayer),
             new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                 messageId, loser)
             .setDefaultId("model.unit.unitDemoted")
             .addStringTemplate("%nation%", loserPlayer.getNationName())
             .addStringTemplate("%oldName%", oldName)
             .addStringTemplate("%unit%", loser.getLabel())
             .addStringTemplate("%enemyNation%", winnerNation)
             .addStringTemplate("%enemyUnit%", winner.getLabel())
             .addStringTemplate("%location%", loserLocation));
     }
     /**
      * Destroy a colony.
      *
      * @param attacker The <code>Unit</code> that attacked.
      * @param colony The <code>Colony</code> that was attacked.
      * @param random A pseudo-random number source.
      * @param cs The <code>ChangeSet</code> to update.
      */
     private void csDestroyColony(Unit attacker, Colony colony, Random random,
                                  ChangeSet cs) {
         Game game = attacker.getGame();
         ServerPlayer attackerPlayer = (ServerPlayer) attacker.getOwner();
         StringTemplate attackerNation = attacker.getApparentOwnerName();
         ServerPlayer colonyPlayer = (ServerPlayer) colony.getOwner();
         StringTemplate colonyNation = colonyPlayer.getNationName();
         int plunder = colony.getPlunder(attacker, random);
 
         // Handle history and messages before colony destruction.
         cs.addHistory(colonyPlayer,
             new HistoryEvent(game.getTurn(),
                 HistoryEvent.EventType.COLONY_DESTROYED)
             .addStringTemplate("%nation%", attackerNation)
             .addName("%colony%", colony.getName()));
         cs.addMessage(See.only(colonyPlayer),
             new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                 "model.unit.colonyBurning", colony.getTile())
             .addName("%colony%", colony.getName())
             .addAmount("%amount%", plunder)
             .addStringTemplate("%nation%", attackerNation)
             .addStringTemplate("%unit%", attacker.getLabel()));
         cs.addMessage(See.all().except(colonyPlayer),
             new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                 "model.unit.colonyBurning.other", colonyPlayer)
             .addName("%colony%", colony.getName())
             .addStringTemplate("%nation%", colonyNation)
             .addStringTemplate("%attackerNation%", attackerNation));
 
         // Allocate some plunder.
         if (plunder > 0) {
             attackerPlayer.modifyGold(plunder);
             colonyPlayer.modifyGold(-plunder);
             cs.addPartial(See.only(attackerPlayer), attackerPlayer, "gold");
             cs.addPartial(See.only(colonyPlayer), colonyPlayer, "gold");
         }
 
         // Dispose of the colony and its contents.
         csDisposeSettlement(colony, cs);
     }
     /**
      * Destroys an Indian settlement.
      *
      * @param attacker an <code>Unit</code> value
      * @param settlement an <code>IndianSettlement</code> value
      * @param random A pseudo-random number source.
      * @param cs A <code>ChangeSet</code> to update.
      */
  /*                           UNABLE TO REFACTOR LEFT IN SERVERPLAYER CLASS
   * 
   * protected void csDestroySettlement(Unit attacker,
                                      IndianSettlement settlement,
                                      Random random, ChangeSet cs) {
         Game game = getGame();
         Tile tile = settlement.getTile();
         ServerPlayer attackerPlayer = (ServerPlayer) attacker.getOwner();
         ServerPlayer nativePlayer = (ServerPlayer) settlement.getOwner();
         StringTemplate attackerNation = attackerPlayer.getNationName();
         StringTemplate nativeNation = nativePlayer.getNationName();
         String settlementName = settlement.getName();
         boolean capital = settlement.isCapital();
         int plunder = settlement.getPlunder(attacker, random);
 
         // Remaining units lose their home.
         for (Unit u : settlement.getOwnedUnits()) {
             u.setIndianSettlement(null);
             cs.add(See.only(nativePlayer), u);
         }
                 
         // Destroy the settlement, update settlement tiles.
         csDisposeSettlement(settlement, cs);
 
         // Make the treasure train if there is treasure.
         if (plunder > 0) {
             List<UnitType> unitTypes = game.getSpecification()
                 .getUnitTypesWithAbility(Ability.CARRY_TREASURE);
             UnitType type = Utils.getRandomMember(logger, "Choose train",
                 unitTypes, random);
             Unit train = new ServerUnit(game, tile, attackerPlayer, type);
             train.setTreasureAmount(plunder);
         }
 
         // This is an atrocity.
         int atrocities = Player.SCORE_SETTLEMENT_DESTROYED;
         if (settlement.getType().getClaimableRadius() > 1) atrocities *= 2;
         if (capital) atrocities = (atrocities * 3) / 2;
         attackerPlayer.modifyScore(atrocities);
         cs.addPartial(See.only(attackerPlayer), attackerPlayer, "score");
 
         // Finish with messages and history.
         cs.addMessage(See.only(attackerPlayer),
             new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                              "model.unit.indianTreasure", attacker)
                 .addName("%settlement%", settlementName)
                 .addAmount("%amount%", plunder));
         cs.addHistory(attackerPlayer,
             new HistoryEvent(game.getTurn(),
                              HistoryEvent.EventType.DESTROY_SETTLEMENT)
                 .addStringTemplate("%nation%", nativeNation)
                 .addName("%settlement%", settlementName));
         if (capital) {
             cs.addMessage(See.only(attackerPlayer),
                 new ModelMessage(ModelMessage.MessageType.FOREIGN_DIPLOMACY,
                                  "indianSettlement.capitalBurned", attacker)
                     .addName("%name%", settlementName)
                     .addStringTemplate("%nation%", nativeNation));
         }
         if (nativePlayer.checkForDeath() == IS_DEAD) {
             cs.addGlobalHistory(game,
                 new HistoryEvent(game.getTurn(),
                                  HistoryEvent.EventType.DESTROY_NATION)
                     .addStringTemplate("%nation%", attackerNation)
                     .addStringTemplate("%nativeNation%", nativeNation));
         }
         cs.addAttribute(See.only(attackerPlayer), "sound",
             "sound.event.destroySettlement");
     }*/
     /**
      * Evade a normal attack.
      *
      * @param attacker The attacker <code>Unit</code>.
      * @param defender A naval <code>Unit</code> that evades the attacker.
      * @param cs A <code>ChangeSet</code> to update.
      */
     private void csEvadeAttack(Unit attacker, Unit defender, ChangeSet cs) {
         ServerPlayer attackerPlayer = (ServerPlayer) attacker.getOwner();
         StringTemplate attackerNation = attacker.getApparentOwnerName();
         ServerPlayer defenderPlayer = (ServerPlayer) defender.getOwner();
         StringTemplate defenderNation = defender.getApparentOwnerName();
 
         cs.addMessage(See.only(attackerPlayer),
             new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                 "model.unit.enemyShipEvaded", attacker)
             .addStringTemplate("%unit%", attacker.getLabel())
             .addStringTemplate("%enemyUnit%", defender.getLabel())
             .addStringTemplate("%enemyNation%", defenderNation));
         cs.addMessage(See.only(defenderPlayer),
             new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                 "model.unit.shipEvaded", defender)
             .addStringTemplate("%unit%", defender.getLabel())
             .addStringTemplate("%enemyUnit%", attacker.getLabel())
             .addStringTemplate("%enemyNation%", attackerNation));
     }
     
     /**
      * Evade a bombardment.
      *
      * @param settlement The attacker <code>Settlement</code>.
      * @param defender A naval <code>Unit</code> that evades the attacker.
      * @param cs A <code>ChangeSet</code> to update.
      */
     private void csEvadeBombard(Settlement settlement, Unit defender,
     		ChangeSet cs) {
     	ServerPlayer attackerPlayer = (ServerPlayer) settlement.getOwner();
     	ServerPlayer defenderPlayer = (ServerPlayer) defender.getOwner();
     	StringTemplate defenderNation = defender.getApparentOwnerName();
 
     	cs.addMessage(See.only(attackerPlayer),
     			new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
     					"model.unit.shipEvadedBombardment", settlement)
     	.addName("%colony%", settlement.getName())
     	.addStringTemplate("%unit%", defender.getLabel())
     	.addStringTemplate("%nation%", defenderNation));
     	cs.addMessage(See.only(defenderPlayer),
     			new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
     					"model.unit.shipEvadedBombardment", defender)
     	.addName("%colony%", settlement.getName())
     	.addStringTemplate("%unit%", defender.getLabel())
     	.addStringTemplate("%nation%", defenderNation));
     }
     
     /**
      * Loot a ship.
      *
      * @param winner The winning naval <code>Unit</code>.
      * @param loser The losing naval <code>Unit</code>
      * @param cs A <code>ChangeSet</code> to update.
      */
     private void csLootShip(Unit winner, Unit loser, ChangeSet cs) {
         ServerPlayer winnerPlayer = (ServerPlayer) winner.getOwner();
         if (loser.getGoodsList().size() > 0 && winner.hasSpaceLeft()) {
             List<Goods> capture = new ArrayList<Goods>(loser.getGoodsList());
             for (Goods g : capture) g.setLocation(null);
             LootSession session = new LootSession(winner, loser);
             session.setCapture(capture);
             cs.add(See.only(winnerPlayer), ChangeSet.ChangePriority.CHANGE_LATE,
                 new LootCargoMessage(winner, loser.getId(), capture));
         }
         loser.getGoodsContainer().removeAll();
         loser.setState(Unit.UnitState.ACTIVE);
     }
     /**
      * Unit autoequips but loses equipment.
      *
      * @param attacker The <code>Unit</code> that attacked.
      * @param defender The <code>Unit</code> that defended and loses equipment.
      * @param cs A <code>ChangeSet</code> to update.
      */
     private void csLoseAutoEquip(Unit attacker, Unit defender, ChangeSet cs) {
         ServerPlayer defenderPlayer = (ServerPlayer) defender.getOwner();
         StringTemplate defenderNation = defenderPlayer.getNationName();
         Settlement settlement = defender.getSettlement();
         StringTemplate defenderLocation = defender.getLocation()
             .getLocationNameFor(defenderPlayer);
         EquipmentType equip = defender
             .getBestCombatEquipmentType(defender.getAutomaticEquipment());
         ServerPlayer attackerPlayer = (ServerPlayer) attacker.getOwner();
         StringTemplate attackerLocation = attacker.getLocation()
             .getLocationNameFor(attackerPlayer);
         StringTemplate attackerNation = attacker.getApparentOwnerName();
 
         // Autoequipment is not actually with the unit, it is stored
         // in the settlement of the unit.  Remove it from there.
         for (AbstractGoods goods : equip.getRequiredGoods()) {
             settlement.removeGoods(goods);
         }
 
         cs.addMessage(See.only(attackerPlayer),
             new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                 "model.unit.unitWinColony", attacker)
             .addStringTemplate("%location%", attackerLocation)
             .addStringTemplate("%nation%", attackerPlayer.getNationName())
             .addStringTemplate("%unit%", attacker.getLabel())
             .addStringTemplate("%settlement%", settlement.getLocationNameFor(attackerPlayer))
             .addStringTemplate("%enemyNation%", defenderNation)
             .addStringTemplate("%enemyUnit%", defender.getLabel()));
         cs.addMessage(See.only(defenderPlayer),
             new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                 "model.unit.unitLoseAutoEquip", defender)
             .addStringTemplate("%location%", defenderLocation)
             .addStringTemplate("%nation%", defenderNation)
             .addStringTemplate("%unit%", defender.getLabel())
             .addStringTemplate("%settlement%", settlement.getLocationNameFor(defenderPlayer))
             .addStringTemplate("%enemyNation%", attackerNation)
             .addStringTemplate("%enemyUnit%", attacker.getLabel()));
     }
 
     
     /**
      * Unit drops some equipment.
      *
      * @param winner The <code>Unit</code> that won.
      * @param loser The <code>Unit</code> that lost and loses equipment.
      * @param cs A <code>ChangeSet</code> to update.
      */
     private void csLoseEquip(Unit winner, Unit loser, ChangeSet cs) {
         ServerPlayer loserPlayer = (ServerPlayer) loser.getOwner();
         StringTemplate loserNation = loserPlayer.getNationName();
         StringTemplate loserLocation = loser.getLocation()
             .getLocationNameFor(loserPlayer);
         StringTemplate oldName = loser.getLabel();
         ServerPlayer winnerPlayer = (ServerPlayer) winner.getOwner();
         StringTemplate winnerNation = winner.getApparentOwnerName();
         StringTemplate winnerLocation = winner.getLocation()
             .getLocationNameFor(winnerPlayer);
         EquipmentType equip
             = loser.getBestCombatEquipmentType(loser.getEquipment());
 
         // Remove the equipment, accounting for possible loss of
         // mobility due to horses going away.
         loser.changeEquipment(equip, -1);
         loser.setMovesLeft(Math.min(loser.getMovesLeft(),
                                     loser.getInitialMovesLeft()));
 
         String messageId;
         if (loser.getEquipment().isEmpty()) {
             messageId = "model.unit.unitDemotedToUnarmed";
             loser.setState(Unit.UnitState.ACTIVE);
         } else {
             messageId = loser.getType().getId() + ".demoted";
         }
 
         cs.addMessage(See.only(winnerPlayer),
             new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                 messageId, winner)
             .setDefaultId("model.unit.unitDemoted")
             .addStringTemplate("%nation%", loserNation)
             .addStringTemplate("%oldName%", oldName)
             .addStringTemplate("%unit%", loser.getLabel())
             .addStringTemplate("%enemyNation%", winnerPlayer.getNationName())
             .addStringTemplate("%enemyUnit%", winner.getLabel())
             .addStringTemplate("%location%", winnerLocation));
         cs.addMessage(See.only(loserPlayer),
             new ModelMessage(ModelMessage.MessageType.COMBAT_RESULT,
                 messageId, loser)
             .setDefaultId("model.unit.unitDemoted")
             .addStringTemplate("%nation%", loserNation)
             .addStringTemplate("%oldName%", oldName)
             .addStringTemplate("%unit%", loser.getLabel())
             .addStringTemplate("%enemyNation%", winnerNation)
             .addStringTemplate("%enemyUnit%", winner.getLabel())
             .addStringTemplate("%location%", loserLocation));
     }
     /**
      * Disposes of a settlement and reassign its tiles.
      *
      * @param settlement The <code>Settlement</code> under attack.
      * @param cs A <code>ChangeSet</code> to update.
      */
     public void csDisposeSettlement(Settlement settlement, ChangeSet cs) {
         logger.finest("Disposing of " + settlement.getName());
         ServerPlayer owner = (ServerPlayer) settlement.getOwner();
 
         // Get rid of the any missionary first.
         if (settlement instanceof IndianSettlement) {
             Unit missionary = ((IndianSettlement)settlement).getMissionary();
             if (missionary != null) {
                 ((ServerPlayer)missionary.getOwner())
                     .csKillMissionary((IndianSettlement)settlement,
                         "indianSettlement.mission.destroyed", cs);
             }
         }
             
         // Try to reassign the tiles
         List<Tile> owned = settlement.getOwnedTiles();
         Tile centerTile = settlement.getTile();
         Settlement centerClaimant = null;
         HashMap<Settlement, Integer> votes = new HashMap<Settlement,Integer>();
         HashMap<Tile, Settlement> claims = new HashMap<Tile, Settlement>();
         Settlement claimant;
 
         while (!owned.isEmpty()) {
             Tile tile = owned.remove(0);
             votes.clear();
             for (Tile t : tile.getSurroundingTiles(1)) {
                 claimant = t.getOwningSettlement();
                 if (claimant != null && claimant != settlement
                     // BR#3375773 found a case where tiles were
                     // still owned by a settlement that had been
                     // previously destroyed.  These should be gone, but...
                     && !claimant.isDisposed()
                     && claimant.getOwner() != null
                     && claimant.getOwner().canOwnTile(tile)
                     && (claimant.getOwner().isIndian()
                         || claimant.getTile().getDistanceTo(tile)
                         <= claimant.getRadius())) {
                     // Weight claimant settlements:
                     //   settlements owned by the same player
                     //     > settlements owned by same type of player
                     //     > other settlements
                     int value = (claimant.getOwner() == owner) ? 3
                         : (claimant.getOwner().isEuropean()
                             == owner.isEuropean()) ? 2
                         : 1;
                     if (votes.get(claimant) != null) {
                         value += votes.get(claimant).intValue();
                     }
                     votes.put(claimant, new Integer(value));
                 }
             }
             claimant = null;
             if (!votes.isEmpty()) {
                 int bestValue = 0;
                 for (Settlement key : votes.keySet()) {
                     int value = votes.get(key).intValue();
                     if (bestValue < value) {
                         bestValue = value;
                         claimant = key;
                     }
                 }
             }
             claims.put(tile, claimant);
         }
         for (Tile t : claims.keySet()) {
             claimant = claims.get(t);
             if (t == centerTile) {
                 centerClaimant = claimant; // Defer until settlement gone
             } else {
                 if (claimant == null) {
                     t.changeOwnership(null, null);
                 } else {
                     t.changeOwnership(claimant.getOwner(), claimant);
                 }
                 cs.add(See.perhaps().always(owner), t);
             }
         }
 
         // Settlement goes away
         if (!owner.removeSettlement(settlement)) {
             throw new IllegalStateException("Failed to remove settlement: "
                 + settlement);
         }
         if (owner.hasSettlement(settlement)) {
             throw new IllegalStateException("Still has settlement: "
                 + settlement);
         }
         cs.addDispose(See.perhaps().always(owner), centerTile, settlement);
         // Now the settlement is gone, the center tile can be claimed.
         if (centerClaimant == null) {
             centerTile.changeOwnership(null, null);
         } else {
             centerTile.changeOwnership(centerClaimant.getOwner(),
                                        centerClaimant);
         }
     }
 
     //////////////////////////////////////////end imported methods//////////////////////////////////////
   
     //added to override inherited methods
 	@Override
 	public String getServerXMLElementTagName() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	@Override
 	public void csNewTurn(Random random, ChangeSet cs) {
 		// TODO Auto-generated method stub
 		
 	}
 }
