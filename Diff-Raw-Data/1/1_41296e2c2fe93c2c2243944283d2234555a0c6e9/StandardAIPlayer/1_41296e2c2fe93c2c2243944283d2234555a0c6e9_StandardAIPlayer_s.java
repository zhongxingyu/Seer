 /**
  *  Copyright (C) 2002-2011  The FreeCol Team
  *
  *  This file is part of FreeCol.
  *
  *  FreeCol is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  FreeCol is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /* "STANDARD" AIPLAYER ********************************************************
  *
  * This class is able to control _any_ Player type,
  * whether European (both Colonial and REF), or Indian.
  *
  * It is currently used as the standard AI for all non-player nations.
  *
  *****************************************************************************/
 
 package net.sf.freecol.server.ai;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 import net.sf.freecol.common.model.Ability;
 import net.sf.freecol.common.model.AbstractGoods;
 import net.sf.freecol.common.model.Colony;
 import net.sf.freecol.common.model.ColonyTile;
 import net.sf.freecol.common.model.ColonyTradeItem;
 import net.sf.freecol.common.model.CombatModel;
 import net.sf.freecol.common.model.DiplomaticTrade;
 import net.sf.freecol.common.model.EquipmentType;
 import net.sf.freecol.common.model.Europe;
 import net.sf.freecol.common.model.FoundingFather;
 import net.sf.freecol.common.model.FreeColGameObject;
 import net.sf.freecol.common.model.GoldTradeItem;
 import net.sf.freecol.common.model.Goods;
 import net.sf.freecol.common.model.GoodsTradeItem;
 import net.sf.freecol.common.model.GoodsType;
 import net.sf.freecol.common.model.IndianSettlement;
 import net.sf.freecol.common.model.Location;
 import net.sf.freecol.common.model.Map;
 import net.sf.freecol.common.model.Ownable;
 import net.sf.freecol.common.model.PathNode;
 import net.sf.freecol.common.model.Player;
 import net.sf.freecol.common.model.Player.PlayerType;
 import net.sf.freecol.common.model.Player.Stance;
 import net.sf.freecol.common.model.Settlement;
 import net.sf.freecol.common.model.Specification;
 import net.sf.freecol.common.model.StanceTradeItem;
 import net.sf.freecol.common.model.Tension;
 import net.sf.freecol.common.model.Tile;
 import net.sf.freecol.common.model.TradeItem;
 import net.sf.freecol.common.model.Unit;
 import net.sf.freecol.common.model.Unit.UnitState;
 import net.sf.freecol.common.model.UnitTradeItem;
 import net.sf.freecol.common.model.UnitType;
 import net.sf.freecol.common.model.WorkLocation;
 import net.sf.freecol.common.model.pathfinding.CostDeciders;
 import net.sf.freecol.common.model.pathfinding.GoalDecider;
 import net.sf.freecol.common.networking.NetworkConstants;
 import net.sf.freecol.server.ai.mission.BuildColonyMission;
 import net.sf.freecol.server.ai.mission.CashInTreasureTrainMission;
 import net.sf.freecol.server.ai.mission.DefendSettlementMission;
 import net.sf.freecol.server.ai.mission.IdleAtColonyMission;
 import net.sf.freecol.server.ai.mission.IndianBringGiftMission;
 import net.sf.freecol.server.ai.mission.IndianDemandMission;
 import net.sf.freecol.server.ai.mission.Mission;
 import net.sf.freecol.server.ai.mission.PioneeringMission;
 import net.sf.freecol.server.ai.mission.PrivateerMission;
 import net.sf.freecol.server.ai.mission.ScoutingMission;
 import net.sf.freecol.server.ai.mission.TransportMission;
 import net.sf.freecol.server.ai.mission.UnitSeekAndDestroyMission;
 import net.sf.freecol.server.ai.mission.UnitWanderHostileMission;
 import net.sf.freecol.server.ai.mission.UnitWanderMission;
 import net.sf.freecol.server.ai.mission.WishRealizationMission;
 import net.sf.freecol.server.ai.mission.WorkInsideColonyMission;
 import net.sf.freecol.server.model.ServerPlayer;
 
 import org.w3c.dom.Element;
 
 
 /**
  * Objects of this class contains AI-information for a single {@link
  * Player} and is used for controlling this player.
  *
  * The method {@link #startWorking} gets called by the
  * {@link AIInGameInputHandler} when it is this player's turn.
  */
 public class StandardAIPlayer extends AIPlayer {
 
     private static final Logger logger = Logger.getLogger(StandardAIPlayer.class.getName());
 
     private static final int MAX_DISTANCE_TO_BRING_GIFT = 5;
 
     private static final int MAX_NUMBER_OF_GIFTS_BEING_DELIVERED = 1;
 
     private static final int MAX_DISTANCE_TO_MAKE_DEMANDS = 5;
 
     private static final int MAX_NUMBER_OF_DEMANDS = 1;
 
     private static enum AIStrategy { NONE, TRADE, IMMIGRATION, COOPERATION, CONQUEST }
 
     /** The strategy of this player. */
     private AIStrategy strategy = AIStrategy.NONE;
 
     /**
      * Stores temporary information for sessions (trading with another player
      * etc).
      */
     private HashMap<String, Integer> sessionRegister = new HashMap<String, Integer>();
 
     /**
      * Creates a new <code>AIPlayer</code>.
      *
      * @param aiMain The main AI-class.
      * @param player The player that should be associated with this
      *            <code>AIPlayer</code>.
      */
     public StandardAIPlayer(AIMain aiMain, ServerPlayer player) {
         super(aiMain, player.getId());
         setPlayer(player);
     }
 
     /**
      * Creates a new <code>AIPlayer</code> and reads the information from the
      * given <code>Element</code>.
      *
      * @param aiMain The main AI-class.
      * @param element The XML-element containing information.
      */
     public StandardAIPlayer(AIMain aiMain, Element element) {
         super(aiMain, element.getAttribute(ID_ATTRIBUTE));
         readFromXMLElement(element);
     }
 
     /**
      * Creates a new <code>AIPlayer</code>.
      *
      * @param aiMain The main AI-object.
      * @param in The input stream containing the XML.
      * @throws XMLStreamException if a problem was encountered during parsing.
      */
     public StandardAIPlayer(AIMain aiMain, XMLStreamReader in) throws XMLStreamException {
         super(aiMain, in.getAttributeValue(null, ID_ATTRIBUTE));
         readFromXML(in);
     }
 
 
 /* IMPLEMENTATION (AIPlayer interface) ****************************************/
 
 
     /**
      * Tells this <code>AIPlayer</code> to make decisions. The
      * <code>AIPlayer</code> is done doing work this turn when this method
      * returns.
      */
     public void startWorking() {
         final Player player = getPlayer();
         logger.finest("Entering method startWorking: "
                       + player + ", year " + getGame().getTurn());
         this.strategy = AIStrategy.TRADE;
         sessionRegister.clear();
         clearAIUnits();
         if (player.isREF() && !player.isWorkForREF()) {
             logger.warning("No work for REF: " + player);
             return;
         }
         cheat();
         determineStances();
         rearrangeWorkersInColonies();
         abortInvalidAndOneTimeMissions();
         ensureCorrectMissions();
         giveNavalMissions();
         secureSettlements();
         giveNormalMissions();
         bringGifts();
         demandTribute();
         createAIGoodsInColonies();
         createTransportLists();
         doMissions();
         rearrangeWorkersInColonies();
         abortInvalidMissions();
         // Some of the mission might have been invalidated by a another mission.
         giveNormalMissions();
         doMissions();
         rearrangeWorkersInColonies();
         abortInvalidMissions();
         ensureCorrectMissions();
         clearAIUnits();
     }
 
     /**
      * Returns an <code>Iterator</code> over all the
      * <code>TileImprovement</code>s needed by all of this player's colonies.
      *
      * @return The <code>Iterator</code>.
      * @see net.sf.freecol.common.model.TileImprovement
      */
     public Iterator<TileImprovementPlan> getTileImprovementPlanIterator() {
         ArrayList<TileImprovementPlan> tileImprovements = new ArrayList<TileImprovementPlan>();
         Iterator<AIColony> acIterator = getAIColonyIterator();
         while (acIterator.hasNext()) {
             AIColony ac = acIterator.next();
             Iterator<TileImprovementPlan> it = ac.getTileImprovementPlanIterator();
             while (it.hasNext()) {
                 tileImprovements.add(it.next());
             }
         }
         return tileImprovements.iterator();
     }
 
     /**
      * Remove a <code>TileImprovementPlan</code> from the list
      */
     public void removeTileImprovementPlan(TileImprovementPlan plan){
     	Iterator<AIColony> colonyIter = getAIColonyIterator();
         while (colonyIter.hasNext()) {
             AIColony colony = colonyIter.next();
             if(colony.removeTileImprovementPlan(plan)){
             	return;
             }
         }
         logger.warning("Not found given TileImprovementPlan to remove");
     }
 
     /**
      * This is a temporary method which are used for forcing the
      * computer players into building more colonies. The method will
      * be removed after the proper code for deciding whether a colony
      * should be built or not has been implemented.
      *
      * @return <code>true</code> if the AI should build more colonies.
      */
     public boolean hasFewColonies() {
         if (!getPlayer().canBuildColonies()) {
             return false;
         }
         int numberOfColonies = 0;
         int numberOfWorkers = 0;
         for (Settlement settlement : getPlayer().getSettlements()) {
             numberOfColonies++;
             numberOfWorkers += settlement.getUnitCount();
         }
 
         boolean result = numberOfColonies <= 2
             || (numberOfColonies >= 3
                 && numberOfWorkers / numberOfColonies > numberOfColonies - 2);
         logger.finest("hasFewColonies = " + result);
         return result;
     }
 
     /**
      * Returns an <code>Iterator</code> for all the wishes. The items are
      * sorted by the {@link Wish#getValue value}, with the item having the
      * highest value appearing first in the <code>Iterator</code>.
      *
      * @return The <code>Iterator</code>.
      * @see Wish
      */
     public Iterator<Wish> getWishIterator() {
         ArrayList<Wish> wishList = new ArrayList<Wish>();
         Iterator<AIColony> ai = getAIColonyIterator();
         while (ai.hasNext()) {
             AIColony ac = ai.next();
             Iterator<Wish> wishIterator = ac.getWishIterator();
             while (wishIterator.hasNext()) {
                 Wish w = wishIterator.next();
                 wishList.add(w);
             }
         }
         Collections.sort(wishList);
         return wishList.iterator();
     }
 
     /**
      * Selects the most useful founding father offered.
      *
      * @param foundingFathers The founding fathers on offer.
      * @return The founding father selected.
      */
     public FoundingFather selectFoundingFather(List<FoundingFather> foundingFathers) {
         // TODO: improve choice
         int age = getGame().getTurn().getAge();
         FoundingFather bestFather = null;
         int bestWeight = -1;
         for (FoundingFather father : foundingFathers) {
             if (father == null) continue;
 
             // For the moment, arbitrarily: always choose the one
             // offering custom houses.  Allowing the AI to build CH
             // early alleviates the complexity problem of handling all
             // TransportMissions correctly somewhat.
             if (father.hasAbility("model.ability.buildCustomHouse")) {
                 return father;
             }
 
             int weight = father.getWeight(age);
             if (weight > bestWeight) {
                 bestWeight = weight;
                 bestFather = father;
             }
         }
         return bestFather;
     }
 
     /**
      * Decides whether to accept the monarch's tax raise or not.
      *
      * @param tax The new tax rate to be considered.
      * @return <code>true</code> if the tax raise should be accepted.
      */
     public boolean acceptTax(int tax) {
         Goods toBeDestroyed = getPlayer().getMostValuableGoods();
         if (toBeDestroyed == null) {
             return false;
         }
 
         GoodsType goodsType = toBeDestroyed.getType();
         if (goodsType.isFoodType() || goodsType.isBreedable()) {
             // we should be able to produce food and horses ourselves
             return false;
         } else if (goodsType.isMilitaryGoods() ||
                    goodsType.isTradeGoods() ||
                    goodsType.isBuildingMaterial()) {
             if (getGame().getTurn().getAge() == 3) {
                 // by this time, we should be able to produce
                 // enough ourselves
                 return false;
             } else {
                 return true;
             }
         } else {
             int averageIncome = 0;
             int numberOfGoods = 0;
             List<GoodsType> goodsTypes = getAIMain().getGame().getSpecification().getGoodsTypeList();
             for (GoodsType type : goodsTypes) {
                 if (type.isStorable()) {
                     averageIncome += getPlayer().getIncomeAfterTaxes(type);
                     numberOfGoods++;
                 }
             }
             averageIncome = averageIncome / numberOfGoods;
             if (getPlayer().getIncomeAfterTaxes(toBeDestroyed.getType()) > averageIncome) {
                 // this is a more valuable type of goods
                 return false;
             } else {
                 return true;
             }
         }
     }
 
     /**
      * Decides whether to accept an Indian demand, or not.
      *
      * @param unit The unit making demands.
      * @param colony The colony where demands are being made.
      * @param goods The goods demanded.
      * @param gold The amount of gold demanded.
      * @return <code>true</code> if this <code>AIPlayer</code> accepts the
      *         indian demand and <code>false</code> otherwise.
      */
     public boolean acceptIndianDemand(Unit unit, Colony colony, Goods goods, int gold) {
         // TODO: make a better choice
         if (strategy == AIStrategy.CONQUEST) {
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Decides whether to accept a mercenary offer, or not.
      *
      * @return <code>true</code> if this <code>AIPlayer</code> accepts the
      *         offer and <code>false</code> otherwise.
      */
     public boolean acceptMercenaryOffer() {
         // TODO: make a better choice
         if (strategy == AIStrategy.CONQUEST || getPlayer().isAtWar()) {
             return true;
         } else {
             return false;
         }
     }
 
     public boolean acceptDiplomaticTrade(DiplomaticTrade agreement) {
         boolean validOffer = true;
         Stance stance = null;
         int value = 0;
         Iterator<TradeItem> itemIterator = agreement.iterator();
         while (itemIterator.hasNext()) {
             TradeItem item = itemIterator.next();
             if (item instanceof GoldTradeItem) {
                 int gold = ((GoldTradeItem) item).getGold();
                 if (item.getSource() == getPlayer()) {
                     value -= gold;
                 } else {
                     value += gold;
                 }
             } else if (item instanceof StanceTradeItem) {
                 // TODO: evaluate whether we want this stance change
                 stance = ((StanceTradeItem) item).getStance();
                 switch (stance) {
                     case UNCONTACTED:
                         validOffer = false; //never accept invalid stance change
                         break;
                     case WAR: // always accept war without cost
                         break;
                     case CEASE_FIRE:
                         value -= 500;
                         break;
                     case PEACE:
                         if (!agreement.getSender().hasAbility("model.ability.alwaysOfferedPeace")) {
                             // TODO: introduce some kind of counter in order to avoid
                             // Benjamin Franklin exploit
                             value -= 1000;
                         }
                         break;
                     case ALLIANCE:
                         value -= 2000;
                         break;
                     }
 
             } else if (item instanceof ColonyTradeItem) {
                 // TODO: evaluate whether we might wish to give up a colony
                 if (item.getSource() == getPlayer()) {
                     validOffer = false;
                     break;
                 } else {
                     value += 1000;
                 }
             } else if (item instanceof UnitTradeItem) {
                 // TODO: evaluate whether we might wish to give up a unit
                 if (item.getSource() == getPlayer()) {
                     validOffer = false;
                     break;
                 } else {
                     value += 100;
                 }
             } else if (item instanceof GoodsTradeItem) {
                 Goods goods = ((GoodsTradeItem) item).getGoods();
                 if (item.getSource() == getPlayer()) {
                     value -= getPlayer().getMarket().getBidPrice(goods.getType(), goods.getAmount());
                 } else {
                     value += getPlayer().getMarket().getSalePrice(goods.getType(), goods.getAmount());
                 }
             }
         }
         if (validOffer) {
             logger.info("Trade value is " + value + ", accept if >=0");
         } else {
             logger.info("Trade offer is considered invalid!");
         }
         return (value>=0)&&validOffer;
     }
 
 
     /**
      * Called after another <code>Player</code> sends a <code>trade</code> message
      *
      *
      * @param goods The goods which we are going to offer
      */
     public void registerSellGoods(Goods goods) {
         String goldKey = "tradeGold#" + goods.getType().getId() + "#" + goods.getAmount()
             + "#" + goods.getLocation().getId();
         sessionRegister.put(goldKey, null);
     }
 
     /**
      * Called when another <code>Player</code> proposes to buy.
      *
      *
      * @param unit The foreign <code>Unit</code> trying to trade.
      * @param settlement The <code>Settlement</code> this player owns and
      *            which the given <code>Unit</code> is trading.
      * @param goods The goods the given <code>Unit</code> is trying to sell.
      * @param gold The suggested price.
      * @return The price this <code>AIPlayer</code> suggests or
      *         {@link NetworkConstants#NO_TRADE}.
      */
     public int buyProposition(Unit unit, Settlement settlement, Goods goods, int gold) {
         logger.finest("Entering method buyProposition");
         String goldKey = "tradeGold#" + goods.getType().getId() + "#" + goods.getAmount()
             + "#" + settlement.getId();
         String hagglingKey = "tradeHaggling#" + unit.getId();
 
         Integer registered = sessionRegister.get(goldKey);
         if (registered == null) {
             int price = ((IndianSettlement) settlement).getPriceToSell(goods)
                 + getPlayer().getTension(unit.getOwner()).getValue();
             sessionRegister.put(goldKey, new Integer(price));
             return price;
         } else {
             int price = registered.intValue();
             if (price < 0 || price == gold) {
                 return price;
             } else if (gold < (price * 9) / 10) {
                 logger.warning("Cheating attempt: sending a offer too low");
                 sessionRegister.put(goldKey, new Integer(-1));
                 return NetworkConstants.NO_TRADE;
             } else {
                 int haggling = 1;
                 if (sessionRegister.containsKey(hagglingKey)) {
                     haggling = sessionRegister.get(hagglingKey).intValue();
                 }
                 if (getAIRandom().nextInt(3 + haggling) <= 3) {
                     sessionRegister.put(goldKey, new Integer(gold));
                     sessionRegister.put(hagglingKey, new Integer(haggling + 1));
                     return gold;
                 } else {
                     sessionRegister.put(goldKey, new Integer(-1));
                     return NetworkConstants.NO_TRADE;
                 }
             }
         }
     }
 
     /**
      * Called when another <code>Player</code> proposes a sale.
      *
      * @param unit The foreign <code>Unit</code> trying to trade.
      * @param settlement The <code>Settlement</code> this player owns and
      *            which the given <code>Unit</code> if trying to sell goods.
      * @param goods The goods the given <code>Unit</code> is trying to sell.
      * @param gold The suggested price.
      * @return The price this <code>AIPlayer</code> suggests or
      *         {@link NetworkConstants#NO_TRADE}.
      */
     public int sellProposition(Unit unit, Settlement settlement, Goods goods, int gold) {
         logger.finest("Entering method sellProposition");
         if (settlement instanceof IndianSettlement) {
             String goldKey = "tradeGold#" + goods.getType().getId() + "#" + goods.getAmount() + "#" + unit.getId();
             String hagglingKey = "tradeHaggling#" + unit.getId();
             int price;
             if (sessionRegister.containsKey(goldKey)) {
                 price = sessionRegister.get(goldKey).intValue();
                 if (price <= 0) {
                     return price;
                 }
             } else {
                 price = ((IndianSettlement) settlement).getPriceToBuy(goods) - getPlayer().getTension(unit.getOwner()).getValue();
                 if (price <= 0) return 0;
                 sessionRegister.put(goldKey, new Integer(price));
             }
             if (gold < 0 || price == gold) {
                 return price;
             } else if (gold > (price * 11) / 10) {
                 logger.warning("Cheating attempt: haggling with a request too high");
                 sessionRegister.put(goldKey, new Integer(-1));
                 return NetworkConstants.NO_TRADE;
             } else {
                 int haggling = 1;
                 if (sessionRegister.containsKey(hagglingKey)) {
                     haggling = sessionRegister.get(hagglingKey).intValue();
                 }
                 if (getAIRandom().nextInt(3 + haggling) <= 3) {
                     sessionRegister.put(goldKey, new Integer(gold));
                     sessionRegister.put(hagglingKey, new Integer(haggling + 1));
                     return gold;
                 } else {
                     sessionRegister.put(goldKey, new Integer(-1));
                     return NetworkConstants.NO_TRADE;
                 }
             }
         } else if (settlement instanceof Colony) {
             Colony colony = (Colony) settlement;
             Player otherPlayer = unit.getOwner();
             // the client should have prevented this
             if (getPlayer().atWarWith(otherPlayer)) {
                 return NetworkConstants.NO_TRADE;
             }
             // don't pay for more than fits in the warehouse
             int amount = colony.getWarehouseCapacity() - colony.getGoodsCount(goods.getType());
             amount = Math.min(amount, goods.getAmount());
             // get a good price
             Tension.Level tensionLevel = getPlayer().getTension(otherPlayer).getLevel();
             int percentage = (9 - tensionLevel.ordinal()) * 10;
             // what we could get for the goods in Europe (minus taxes)
             int netProfits = ((100 - getPlayer().getTax()) * getPlayer().getMarket().getSalePrice(goods.getType(), amount)) / 100;
             int price = (netProfits * percentage) / 100;
             return price;
         } else {
             throw new IllegalArgumentException("Unknown type of settlement.");
         }
     }
 
 /* Internal methods ***********************************************************/
 
 
     /**
      * Cheats for the AI :-)
      */
     private void cheat() {
         logger.finest("Entering method cheat");
         // TODO-AI-CHEATING: REMOVE WHEN THE AI IS GOOD ENOUGH:
         for (GoodsType goodsType : getAIMain().getGame().getSpecification().getGoodsTypeList()) {
             getPlayer().getMarket().setArrears(goodsType, 0);
         }
 
         //TODO: This seems to buy units the AIPlayer can't possibly use (see BR#2566180)
         if (getAIMain().getFreeColServer().isSingleplayer() && getPlayer().isEuropean() && !getPlayer().isREF() && getPlayer().isAI()
                 && getPlayer().getPlayerType() == PlayerType.COLONIAL) {
             Europe europe = getPlayer().getEurope();
             List<UnitType> unitTypes = getAIMain().getGame().getSpecification().getUnitTypeList();
 
             if (getAIRandom().nextInt(10) == 1) {
                 int price = 0;
                 UnitType unitToTrain = null;
                 for (UnitType unitType : unitTypes) {
                     if (unitType.hasPrice()) {
                         int unitPrice = europe.getUnitPrice(unitType);
                         if (unitToTrain == null || unitPrice < price) {
                             unitToTrain = unitType;
                             price = unitPrice;
                         }
                     }
                 }
                 Unit unit = null;
                 if (unitToTrain != null) {
                     getPlayer().modifyGold(price);
                     AIUnit aiUnit = this.trainAIUnitInEurope(unitToTrain);
                     if (aiUnit != null) unit = aiUnit.getUnit();
                 }
                 if (unit != null && unit.isColonist()) {
                     // no need to equip artillery units with muskets or horses
                     // TODO: cleanup magic numbers 50 and 1
                     Specification spec = getAIMain().getGame().getSpecification();
                     GoodsType muskets = spec.getGoodsType("model.goods.muskets");
                     GoodsType horses = spec.getGoodsType("model.goods.horses");
                     getPlayer().modifyGold(getPlayer().getMarket().getBidPrice(muskets, 50));
                     getPlayer().modifyGold(getPlayer().getMarket().getBidPrice(horses, 50));
 
                     AIMessage.askClearSpeciality(getAIUnit(unit));
                     EquipmentType horsesEq = spec.getEquipmentType("model.equipment.horses");
                     EquipmentType musketsEq = spec.getEquipmentType("model.equipment.muskets");
                     AIMessage.askEquipUnit(getAIUnit(unit), horsesEq, 1);
                     AIMessage.askEquipUnit(getAIUnit(unit), musketsEq, 1);
                 }
             }
             if (getAIRandom().nextInt(40) == 21) {
                 int total = 0;
                 ArrayList<UnitType> navalUnits = new ArrayList<UnitType>();
                 for (UnitType unitType : unitTypes) {
                     if (unitType.hasAbility(Ability.NAVAL_UNIT) && unitType.hasPrice()) {
                         navalUnits.add(unitType);
                         total += europe.getUnitPrice(unitType);
                     }
                 }
 
                 UnitType unitToPurchase = null;
                 int random = getAIRandom().nextInt(total);
                 total = 0;
                 for (UnitType unitType : navalUnits) {
                     total += unitType.getPrice();
                     if (random < total) {
                         unitToPurchase = unitType;
                         break;
                     }
                 }
                 getPlayer().modifyGold(europe.getUnitPrice(unitToPurchase));
                 this.trainAIUnitInEurope(unitToPurchase);
             }
         }
     }
 
     /**
      * Ensures that all workers inside a colony gets a
      * {@link WorkInsideColonyMission}.
      */
     private void ensureCorrectMissions() {
         logger.finest("Entering method ensureCorrectMissions");
         if (getPlayer().isIndian()) return;
 
         for (AIUnit au : getAIUnits()) {
             if (au.hasMission()) continue;
 
             // TODO: Find out why this happens, or even if it still does.
             Unit u = au.getUnit();
             if (u.getLocation() instanceof WorkLocation) {
                 AIColony ac = getAIColony(u.getColony());
                 if (ac == null) {
                     logger.warning("No AIColony for unit: " + u
                                    + " at: " + u.getLocation());
                     continue;
                 }
                 au.setMission(new WorkInsideColonyMission(getAIMain(), au, ac));
             }
         }
     }
 
     /**
      * Determines the stances towards each player.
      * That is: should we declare war?
      * TODO: something better, that includes peacemaking.
      */
     private void determineStances() {
         logger.finest("Entering method determineStances");
         Player player = getPlayer();
         for (Player p : getGame().getPlayers()) {
             if (p != player && !p.isDead()) determineStance(p);
         }
     }
 
     /**
      * Aborts all the missions which are no longer valid.
      */
     private void abortInvalidMissions() {
         for (AIUnit au : getAIUnits()) {
             if (au.getMission() == null) continue;
             if (!au.getMission().isValid()) {
                 logger.finest("Abort invalid mission for: " + au.getUnit());
                 au.setMission(null);
             }
         }
     }
 
     /**
      * Aborts all the missions which are no longer valid.
      */
     private void abortInvalidAndOneTimeMissions() {
         for (AIUnit au : getAIUnits()) {
             Mission mission = au.getMission();
             if (mission == null) continue;
             if (!mission.isValid()) {
                 logger.finest("Abort invalid mission: " + mission
                               + " for: " + au.getUnit());
                 au.setMission(null);
             } else if (mission instanceof UnitWanderHostileMission
                        || mission instanceof UnitWanderMission
                        || mission instanceof IdleAtColonyMission
                        // TODO: Mission.isOneTime()
                        ) {
                 logger.finest("Abort one-time mission: " + mission
                               + " for: " + au.getUnit());
                 au.setMission(null);
             }
         }
     }
 
     /**
      * Gives missions to all the naval units this player owns.
      */
     private void giveNavalMissions() {
         logger.finest("Entering method giveNavalMissions");
         if (!getPlayer().isEuropean()) return;
 
         for (AIUnit au : getAIUnits()) {
             if (au.hasMission() || !au.getUnit().isNaval()) continue;
 
             Mission mission = (PrivateerMission.isValid(au))
                 ? new PrivateerMission(getAIMain(), au)
                 : new TransportMission(getAIMain(), au);
             au.setMission(mission);
         }
     }
 
     /**
      * Calls {@link AIColony#rearrangeWorkers} for every colony this player
      * owns.
      */
     private void rearrangeWorkersInColonies() {
         logger.finest("Entering method rearrangeWorkersInColonies");
         if (!getPlayer().isEuropean()) {
             return;
         }
         Iterator<AIColony> ci = getAIColonyIterator();
         while (ci.hasNext()) {
             AIColony c = ci.next();
             if (c.getColony().getOwner() != getPlayer()) continue;
             ArrayList<Tile> oldWorkTiles = new ArrayList<Tile>();
             for (ColonyTile colonyTile : c.getColony().getColonyTiles()) {
                 if (colonyTile.getUnit() != null) {
                     oldWorkTiles.add(colonyTile.getWorkTile());
                 }
             }
 
             c.rearrangeWorkers(getConnection());
         }
     }
 
     /**
      * Takes the necessary actions to secure the settlements. This is done by
      * making new military units or to give existing units new missions.
      */
     private void secureSettlements() {
         logger.finest("Entering method secureSettlements");
         if (getPlayer().isEuropean()) {
             // Ok, we are a European player. Things are about to get fun.
 
             for (Colony colony : getPlayer().getColonies()) {
                 equipSoldiersOutsideColony(colony);
                 reOrganizeSoldiersOfColony(colony);
                 // Temporarily deactive this feature:
                 //secureColony(colony);
             }
         } else {
             List<IndianSettlement> settlements
                 = getPlayer().getIndianSettlements();
             for (IndianSettlement is : settlements) {
                 // Spread arms and horses between camps
                 // TODO: maybe make this dependent on difficulty level?
                 int n = getAIRandom().nextInt(settlements.size());
                 IndianSettlement settlement = settlements.get(n);
                 if (settlement != is) {
                     is.tradeGoodsWithSetlement(settlement);
                 }
             }
             for (IndianSettlement is : settlements) {
                 equipBraves(is);
                 secureIndianSettlement(is);
             }
         }
     }
 
     /**
      * Equips braves with horses and muskets.
      * Keeps some for the settlement defence.
      *
      * @param is The <code>IndianSettlement</code> where the equipping occurs.
      */
     public void equipBraves(IndianSettlement is) {
         Specification spec = getGame().getSpecification();
         GoodsType armsType = spec.getGoodsType("model.goods.muskets");
         GoodsType horsesType = spec.getGoodsType("model.goods.horses");
         EquipmentType armsEqType = spec.getEquipmentType("model.equipment.indian.muskets");
         EquipmentType horsesEqType = spec.getEquipmentType("model.equipment.indian.horses");
 
         int musketsToArmIndian = armsEqType.getAmountRequiredOf(armsType);
         int horsesToMountIndian = horsesEqType.getAmountRequiredOf(horsesType);
         int armsAvail = is.getGoodsCount(armsType);
         int horsesAvail = is.getGoodsCount(horsesType);
 
         for (Unit brave : is.getUnitList()) {
             if (armsAvail < musketsToArmIndian) {
                 break;
             }
             if (brave.isArmed()) {
                 continue;
             }
             logger.info("Equipping brave with muskets");
             AIMessage.askEquipUnit(getAIUnit(brave), armsEqType, 1);
             if (!brave.isArmed()) {
                 logger.warning("Brave has not been armed");
             }
             armsAvail = is.getGoodsCount(armsType);
         }
 
         for (Unit brave : is.getUnitList()) {
             if (horsesAvail < horsesToMountIndian) {
                 break;
             }
             if (brave.isMounted()) {
                 continue;
             }
             logger.info("Equipping brave with horses");
             AIMessage.askEquipUnit(getAIUnit(brave), horsesEqType, 1);
             if (!brave.isMounted()) {
                 logger.warning("Brave has not mounted");
             }
             horsesAvail = is.getGoodsCount(horsesType);
         }
     }
 
     void equipSoldiersOutsideColony(Colony colony) {
         boolean colonyHasArmedUnits = false;
         boolean canArmUnit = false;
 
         EquipmentType musketsEqType = getAIMain().getGame().getSpecification().getEquipmentType("model.equipment.muskets");
         EquipmentType horsesEqType = getAIMain().getGame().getSpecification().getEquipmentType("model.equipment.horses");
 
         // Create comparator to sort units by skill level
         // Prefer unit with less qualifications
         Comparator<Unit> comp = Unit.getSkillLevelComparator();
 
         List<Unit>unarmedExpertSoldiers = new ArrayList<Unit>();
         List<Unit>unarmedColonists = new ArrayList<Unit>();
         List<Unit>unmountedSoldiers = new ArrayList<Unit>();
 
         // First process all units
         for(Unit unit : colony.getTile().getUnitList()){
             boolean isArmed = unit.isArmed();
             boolean isMounted = unit.isMounted();
             boolean isExpertSoldier = unit.hasAbility("model.ability.expertSoldier");
             if(isExpertSoldier){
                 if(isArmed){
                     colonyHasArmedUnits = true;
                     if(!isMounted){
                         unmountedSoldiers.add(unit);
                     }
                 }
                 else{
                     unarmedExpertSoldiers.add(unit);
                 }
                 continue;
             }
             if(unit.isArmed()){
                 colonyHasArmedUnits = true;
                 if(!isMounted){
                     unmountedSoldiers.add(unit);
                 }
             }
             else{
                 unarmedColonists.add(unit);
             }
         }
 
         //Try to equip all unarmed expert soldiers first
         for(Unit unit : unarmedExpertSoldiers){
             canArmUnit = colony.canBuildEquipment(musketsEqType);
             boolean isMounted = unit.isMounted();
             // arming unit with equipment from colony stock
             if(canArmUnit){
                 AIMessage.askEquipUnit(getAIUnit(unit), musketsEqType, 1);
                 colonyHasArmedUnits = true;
                 if(!isMounted){
                     unmountedSoldiers.add(unit);
                 }
                 continue;
             }
         }
 
         // Colony still does not have a soldier to protect it
         // Try to arm a non expert unit
         if(!colonyHasArmedUnits){
             canArmUnit = colony.canBuildEquipment(musketsEqType);
             // colony cannot arm other units, nothing else to do
             if(!canArmUnit || unarmedColonists.size() == 0){
                 return;
             }
 
             Collections.sort(unarmedColonists, comp);
             for (Unit unit : unarmedColonists) {
                 if (unit.canBeEquippedWith(musketsEqType)) {
                     cheatEquipmentGoods(colony, musketsEqType, 1);
                     AIMessage.askEquipUnit(getAIUnit(unit), musketsEqType, 1);
                     if(!unit.isMounted()){
                         unmountedSoldiers.add(unit);
                     }
                     break;
                 }
             }
         }
 
         comp = new Comparator<Unit>(){
             public int compare(Unit unit1, Unit unit2){
                 boolean isUnit1Expert = unit1.hasAbility("model.ability.expertSoldier");
                 boolean isUnit2Expert = unit2.hasAbility("model.ability.expertSoldier");
 
                 if(isUnit1Expert && !isUnit2Expert){
                     return -1;
                 }
                 if(!isUnit1Expert && isUnit2Expert){
                     return 1;
                 }
                 return 0;
             }
         };
         Collections.sort(unmountedSoldiers,comp);
 
         while(unmountedSoldiers.size() > 0 
                 && colony.canBuildEquipment(horsesEqType)){
             Unit soldier = unmountedSoldiers.remove(0);
             cheatEquipmentGoods(colony, horsesEqType, 1);
             AIMessage.askEquipUnit(getAIUnit(soldier), horsesEqType, 1);
         }
     }
 
     /**
      * CHEAT.  Add bogus goods to an AI Colony so that an attempt
      * to equip a unit will succeed.
      */
     private void cheatEquipmentGoods(Colony colony, EquipmentType type,
                                      int amount) {
         for (AbstractGoods goods : type.getGoodsRequired()) {
             GoodsType goodsType = goods.getType();
             int n = amount * goods.getAmount()
                 - colony.getGoodsCount(goodsType);
             if (n > 0) {
                 colony.addGoods(goodsType, n);
             }
         }
     }
 
 
     void reOrganizeSoldiersOfColony(Colony colony){
         List<Unit>unarmedExpertSoldiers = new ArrayList<Unit>();
         List<Unit>armedNonExpertSoldiers = new ArrayList<Unit>();
         List<Unit>colonistsInside = new ArrayList<Unit>();
 
 
         // Create comparator to sort units by skill level
         // Prefer unit with less qualifications
         Comparator<Unit> comp = Unit.getSkillLevelComparator();
 
         Comparator<Unit> reverseComp = Collections.reverseOrder(comp);
 
         // First process all units
         for(Unit unit : colony.getTile().getUnitList()){
             boolean isArmed = unit.isArmed();
             boolean isExpertSoldier = unit.hasAbility("model.ability.expertSoldier"); 
             if(isExpertSoldier){
                 if(!isArmed){
                     unarmedExpertSoldiers.add(unit);
                 }
                 // ignore armed expert soldiers
                 continue;
             }
             if(isArmed){
                 armedNonExpertSoldiers.add(unit);
                 continue;
             }
         }
 
         // we want to remove the ones with the biggest skill possible from soldier duty
         Collections.sort(armedNonExpertSoldiers, reverseComp);
 
         //Try to equip all unarmed expert soldiers first
         for(Unit unit : unarmedExpertSoldiers){
             // there is no non-expert armed unit, no other way to arm this unit and the rest
             if(armedNonExpertSoldiers.size() == 0){
                 return;
             }
 
             // found other non expert soldier, take equipment from it
             Unit otherSoldier = armedNonExpertSoldiers.remove(0);
             switchEquipmentWith(unit, otherSoldier);
             unit.setState(UnitState.ACTIVE);
             otherSoldier.setState(UnitState.ACTIVE);
             getAIUnit(unit).setMission(null);
             getAIUnit(otherSoldier).setMission(null);
         }
 
         if(armedNonExpertSoldiers.size() == 0){
             return;
         }
 
         // Try to swap soldier roles with less skilled units from inside the colony
         colonistsInside.addAll(colony.getUnitList());
         Collections.sort(colonistsInside, comp);
 
         for(Unit unit : armedNonExpertSoldiers){
             // there is no other unit to trade with
             if(colonistsInside.size() == 0){
                 return;
             }
 
             Unit unitInside = colonistsInside.remove(0);
 
             // no more units inside with lesser skill levels
             if(unit.getSkillLevel() <= unitInside.getSkillLevel()){
                 return;
             }
 
             Location loc = unitInside.getLocation();
 
             unitInside.putOutsideColony();
             switchEquipmentWith(unit, unitInside);
             unit.setLocation(loc);
             unit.setState(UnitState.IN_COLONY);
             unitInside.setState(UnitState.ACTIVE);
             getAIUnit(unit).setMission(null);
             getAIUnit(unitInside).setMission(null);
         }
     }
 
     /**
      * Remove all equipment on a unit.
      *
      * @param unit The <code>Unit</code> to remove equipment from.
      */
     public void removeAllEquipment(Unit unit) {
         AIUnit aiUnit = getAIUnit(unit);
         Set<EquipmentType> kit = unit.getEquipment().keySet();
         for (EquipmentType type : new ArrayList<EquipmentType>(kit)) {
             AIMessage.askEquipUnit(aiUnit, type, -unit.getEquipmentCount(type));
         }
     }
 
     /**
      * Switches equipment between colonists
      */
     public void switchEquipmentWith(Unit unit1, Unit unit2){
         if(!unit1.isColonist() || !unit2.isColonist()){
             throw new IllegalArgumentException("Both units need to be colonists to switch equipment");
         }
 
         if(unit1.getTile() != unit2.getTile()){
             throw new IllegalStateException("Units can only switch equipment in the same location");
         }
 
         if(unit1.getSettlement() == null){
             throw new IllegalStateException("Units can only switch equipment in a settlement");
         }
 
         // TODO: use the TypeCountMap that getEquipment() returns and
         // swap the counts as well.
         List<EquipmentType> equipList1 = new ArrayList<EquipmentType>(unit1.getEquipment().keySet());
         List<EquipmentType> equipList2 = new ArrayList<EquipmentType>(unit2.getEquipment().keySet());
         removeAllEquipment(unit1);
         removeAllEquipment(unit2);
         for(EquipmentType equip : equipList2){
             AIMessage.askEquipUnit(getAIUnit(unit1), equip, 1);
         }
         for(EquipmentType equip : equipList1){
             AIMessage.askEquipUnit(getAIUnit(unit2), equip, 1);
         }
     }
 
 
     /**
      * Takes the necessary actions to secure an indian settlement
      *
      * TODO: Package for access by a test only - necessary?
      */
     void secureIndianSettlement(IndianSettlement is) {
         // if not at war, no need to secure settlement
         if (!is.getOwner().isAtWar()) {
             return;
         }
 
         int defenders = is.getTile().getUnitCount();
         int threat = 0;
         int worstThreat = 0;
         Location bestTarget = null;
 
         for (Tile t: is.getTile().getSurroundingTiles(2)) {
             // Do not check ocean tiles
             // Indians do not have naval power
             if(!t.isLand()){
                 continue;
             }
 
             // No units on tile
             if (t.getUnitCount() == 0) {
                 continue;
             }
 
             Player enemy = t.getFirstUnit().getOwner();
 
             // Own units on tile
             if (enemy == getPlayer()) {
                 defenders++;
                 continue;
             }
 
             if (!getPlayer().hasContacted(enemy)) continue;
             int value = getPlayer().getTension(enemy).getValue();
             int threatModifier = 0;
             int unitThreat = 0;
             if (value >= Tension.TENSION_ADD_MAJOR) {
                 threatModifier = 2;
                 unitThreat = t.getUnitCount() * 2;
             } else if (value >= Tension.TENSION_ADD_MINOR) {
                 threatModifier = 1;
                 unitThreat = t.getUnitCount();
             }
 
             threat += threatModifier;
             if (unitThreat > worstThreat) {
                 if (t.getSettlement() != null) {
                     bestTarget = t.getSettlement();
                 } else {
                     bestTarget = t.getFirstUnit();
                 }
                 worstThreat = unitThreat;
             }
         }
         //Note: this is totally arbitrary
         if (threat > defenders && bestTarget != null) {
             AIUnit newDefenderAI = getBraveForSeekAndDestroy(is);
             if (newDefenderAI != null) {
                 Tile targetTile = bestTarget.getTile();
                 if (isTargetValidForSeekAndDestroy(newDefenderAI.getUnit(), targetTile)) {
                     newDefenderAI.setMission(new UnitSeekAndDestroyMission(getAIMain(), newDefenderAI, bestTarget));
                 }
             }
         }
     }
 
     private AIUnit getBraveForSeekAndDestroy(final IndianSettlement indianSettlement) {
         final Iterator<Unit> it = indianSettlement.getOwnedUnitsIterator();
         while (it.hasNext()) {
             final AIUnit chosenOne = getAIUnit(it.next());
             if (chosenOne.getUnit().getLocation() instanceof Tile
                 && (chosenOne.getMission() == null
                     || chosenOne.getMission() instanceof UnitWanderHostileMission)) {
                 return chosenOne;
             }
         }
         return null;
     }
 
     /**
      * Takes the necessary actions to secure a european colony
      */
     /*
     private void secureColony(Colony colony) {
         GoodsType musketType = getAIMain().getGame().getSpecification().getGoodsType("model.goods.muskets");
         GoodsType horsesType = getAIMain().getGame().getSpecification().getGoodsType("model.goods.horses");
         final EquipmentType muskets = getAIMain().getGame().getSpecification().getEquipmentType("model.equipment.muskets");
         final EquipmentType horses = getAIMain().getGame().getSpecification().getEquipmentType("model.equipment.horses");
 
         Map map = getPlayer().getGame().getMap();
         int olddefenders = 0;
         int defenders = 0;
         int threat = 0;
         int worstThreat = 0;
         Location bestTarget = null;
         Iterator<Unit> ui = colony.getTile().getUnitIterator();
         while (ui.hasNext()) {
             if ((ui.next()).isDefensiveUnit()) {
                 defenders++;
             }
         }
         Iterator<Position> positionIterator = map.getCircleIterator(colony.getTile().getPosition(), true, 5);
         while (positionIterator.hasNext()) {
             Tile t = map.getTile(positionIterator.next());
             if (t.getFirstUnit() != null) {
                 if (t.getFirstUnit().getOwner() == getPlayer()) {
                     Iterator<Unit> uit = t.getUnitIterator();
                     while (uit.hasNext()) {
                         if (uit.next().isOffensiveUnit()) {
                             defenders++;
                         }
                     }
                 } else {
                     int thisThreat = 0;
                     if (getPlayer().getTension(t.getFirstUnit().getOwner()).getValue() >= Tension.TENSION_ADD_MAJOR) {
                         Iterator<Unit> uit = t.getUnitIterator();
                         while (uit.hasNext()) {
                             if (uit.next().isOffensiveUnit()) {
                                 thisThreat += 2;
                             }
                         }
                     } else if (getPlayer().getTension(t.getFirstUnit().getOwner()).getValue() >= Tension.TENSION_ADD_MINOR) {
                         Iterator<Unit> uit = t.getUnitIterator();
                         while (uit.hasNext()) {
                             if (uit.next().isOffensiveUnit()) {
                                 thisThreat++;
                             }
                         }
                     }
                     threat += thisThreat;
                     if (thisThreat > worstThreat) {
                         if (t.getSettlement() != null) {
                             bestTarget = t.getSettlement();
                         } else {
                             bestTarget = t.getFirstUnit();
                         }
                         worstThreat = thisThreat;
                     }
                 }
             }
         }
         olddefenders = defenders;
         if (colony.hasStockade()) {
             defenders += (defenders * (colony.getStockade().getLevel()) / 2);
         }
         if (threat > defenders) {
             // We're under attack! Man the stockade!
             ArrayList<Unit> recruits = new ArrayList<Unit>();
             ArrayList<Unit> others = new ArrayList<Unit>();
             int inColonyCount = 0;
             // Let's make some more soldiers, if we can.
             // First, find some people we can recruit.
             ui = colony.getUnitIterator();
             while (ui.hasNext()) {
                 Unit u = (ui.next());
                 if (u.isOffensiveUnit()) {
                     continue; // don't bother dealing with current
                     // soldiers at the moment
                 }
                 if (u.getLocation() != colony.getTile()) {
                     // If we are not on the tile we are in the colony.
                     inColonyCount++;
                 }
                 if (u.hasAbility("model.ability.expertSoldier")) {
                     recruits.add(u);
                 } else if (u.hasAbility(Ability.CAN_BE_EQUIPPED)) {
                     others.add(u);
                 }
             }
             // ATTENTION: skill may be Integer.MIN_VALUE!
             Collections.sort(others, Unit.getSkillLevelComparator());
             recruits.addAll(others);
             // Don't overdo it - leave at least one person behind.
             int recruitCount = threat - defenders;
             if (recruitCount > recruits.size() - 1) {
                 recruitCount = recruits.size() - 1;
             }
             if (recruitCount > inColonyCount - 1) {
                 recruitCount = inColonyCount - 1;
             }
             // Actually go through and arm our people.
             boolean needMuskets = false;
             boolean needHorses = false;
             ui = recruits.iterator();
             while (ui.hasNext() && recruitCount > 0) {
                 Unit u = (ui.next());
                 if (!u.isArmed() && u.canBeEquippedWith(muskets)) {
                     recruitCount--;
                     Element equipUnitElement = Message.createNewRootElement("equipUnit");
                     equipUnitElement.setAttribute("unit", u.getId());
                     equipUnitElement.setAttribute("type", muskets.getId());
                     equipUnitElement.setAttribute("amount", "1");
                     u.equipWith(muskets);
                     sendAndWaitSafely(equipUnitElement);
                     Element putOutsideColonyElement = Message.createNewRootElement("putOutsideColony");
                     putOutsideColonyElement.setAttribute("unit", u.getId());
                     u.putOutsideColony();
                     sendAndWaitSafely(putOutsideColonyElement);
                     // Check if the unit can fortify before sending the order
                     if (u.checkSetState(UnitState.FORTIFYING)) {
                         Element changeStateElement = Message.createNewRootElement("changeState");
                         changeStateElement.setAttribute("unit", u.getId());
                         changeStateElement.setAttribute("state", UnitState.FORTIFYING.toString());
                         sendAndWaitSafely(changeStateElement);
                     }
                     olddefenders++;
                     if (!u.isMounted() && u.canBeEquippedWith(horses)) {
                         equipUnitElement = Message.createNewRootElement("equipUnit");
                         equipUnitElement.setAttribute("unit", u.getId());
                         equipUnitElement.setAttribute("type", horses.getId());
                         equipUnitElement.setAttribute("amount", "1");
                         sendAndWaitSafely(equipUnitElement);
                     } else {
                         needHorses = true;
                     }
                 } else {
                     needMuskets = true;
                     break;
                 }
             }
             AIColony ac = null;
             if (needMuskets || needHorses) {
                 Iterator<AIColony> aIterator = getAIColonyIterator();
                 while (aIterator.hasNext()) {
                     AIColony temp = aIterator.next();
                     if (temp != null && temp.getColony() == colony) {
                         ac = temp;
                         break;
                     }
                 }
             }
             if (needMuskets && ac != null) {
                 // Check and see if we have already made a GoodsWish for
                 // here.
                 Iterator<Wish> wishes = ac.getWishIterator();
                 boolean made = false;
                 while (wishes.hasNext()) {
                     Wish w = wishes.next();
                     if (!(w instanceof GoodsWish)) {
                         continue;
                     }
                     GoodsWish gw = (GoodsWish) w;
                     if (gw.getGoodsType() == musketType) {
                         made = true;
                     }
                 }
                 if (made == false) {
                     // Add a new GoodsWish onto the stack.
                     ac
                             .addGoodsWish(new GoodsWish(getAIMain(), colony, (threat - olddefenders) * 50,
                                     musketType));
                 }
             }
             if (needHorses && ac != null) {
                 // Check and see if we have already made a GoodsWish for
                 // here.
                 Iterator<Wish> wishes = ac.getWishIterator();
                 boolean made = false;
                 while (wishes.hasNext()) {
                     Wish w = wishes.next();
                     if (!(w instanceof GoodsWish)) {
                         continue;
                     }
                     GoodsWish gw = (GoodsWish) w;
                     if (gw.getGoodsType() == horsesType) {
                         made = true;
                     }
                 }
                 if (made == false) {
                     // Add a new GoodsWish onto the stack.
                     ac.addGoodsWish(new GoodsWish(getAIMain(), colony, (threat - defenders) * 50, horsesType));
                 }
             }
             defenders = olddefenders;
             if (colony.hasStockade()) {
                 defenders += (defenders * (colony.getStockade().getLevel()) / 2);
             }
         }
         if (defenders > (threat * 2)) {
             // We're so big and tough, we can go wipe out this threat.
             // Pick someone to go make it happen.
             Unit u = null;
             Iterator<Unit> uit = colony.getUnitIterator();
             while (uit.hasNext()) {
                 Unit candidate = uit.next();
                 if (candidate.isOffensiveUnit() && candidate.getState() == UnitState.FORTIFIED) {
                     u = candidate;
                     break;
                 }
             }
             if (u != null) {
                 u.setState(UnitState.ACTIVE);
                 u.setLocation(colony.getTile());
                 AIUnit newDefenderAI = (AIUnit) getAIMain().getAIObject(u);
                 if (bestTarget != null) {
                     newDefenderAI.setMission(new UnitSeekAndDestroyMission(getAIMain(), newDefenderAI, bestTarget));
                 } else {
                     newDefenderAI.setMission(new UnitWanderHostileMission(getAIMain(), newDefenderAI));
                 }
             }
         }
     }
     */
 
     /**
      * Gives a mission to non-naval units.
      */
     private void giveNormalMissions() {
         logger.finest("Entering method giveNormalMissions");
 
         // Create a datastructure for the worker wishes:
         java.util.Map<UnitType, ArrayList<Wish>> workerWishes =
             new HashMap<UnitType, ArrayList<Wish>>();
         for (UnitType unitType : getAIMain().getGame().getSpecification().getUnitTypeList()) {
             workerWishes.put(unitType, new ArrayList<Wish>());
         }
         if (getPlayer().isEuropean()) {
             Iterator<AIColony> aIterator = getAIColonyIterator();
             while (aIterator.hasNext()) {
                 Iterator<Wish> wIterator = aIterator.next().getWishIterator();
                 while (wIterator.hasNext()) {
                     Wish w = wIterator.next();
                     if (w instanceof WorkerWish && w.getTransportable() == null) {
                         workerWishes.get(((WorkerWish) w).getUnitType()).add(w);
                     }
                 }
             }
         }
 
         final boolean fewColonies = hasFewColonies();
         boolean isPioneerReq = PioneeringMission.getPlayerPioneers(this).size() == 0;
         Iterator<AIUnit> aiUnitsIterator = getAIUnitIterator();
         while (aiUnitsIterator.hasNext()) {
             AIUnit aiUnit = aiUnitsIterator.next();
 
             if (aiUnit.hasMission()) {
                 continue;
             }
 
             Unit unit = aiUnit.getUnit();
 
             if (unit.isUninitialized()) {
                 logger.warning("Trying to assign a mission to an uninitialized object: " + unit.getId());
                 continue;
             }
 
             if (unit.getState() == UnitState.IN_COLONY
                 && unit.getSettlement().getUnitCount() <= 1) {
                 // The unit has its hand full keeping the colony alive.
                 continue;
             }
 
             // only processing naval units
             if(unit.isNaval()){
             	continue;
             }
 
             if (unit.canCarryTreasure()) {
                 aiUnit.setMission(new CashInTreasureTrainMission(getAIMain(), aiUnit));
                 continue;
             }
 
             if (unit.hasAbility("model.ability.scoutIndianSettlement") &&
                        ScoutingMission.isValid(aiUnit)) {
                 aiUnit.setMission(new ScoutingMission(getAIMain(), aiUnit));
                 continue;
             }
 
             if (unit.isOffensiveUnit() || unit.isDefensiveUnit()){
             	Player owner = unit.getOwner();
             	boolean isPastStart = getGame().getTurn().getNumber() > 5
             			&& !owner.getSettlements().isEmpty();
 
             	if(!unit.isColonist() 
             			|| isPastStart
             			|| owner.isIndian()
             			|| owner.isREF()){
             		giveMilitaryMission(aiUnit);
             		continue;
             	}
             }
 
             // Setup as a pioneer if unit is:
             //      - already with tools, or
             //      - an expert pioneer, or
             //      - a non-expert unit and there are no other units assigned as pioneers
             boolean isPioneer = unit.hasAbility("model.ability.improveTerrain")
                                 || unit.hasAbility("model.ability.expertPioneer");
             boolean isExpert = unit.getSkillLevel() > 0;
             if ((isPioneer || (isPioneerReq && !isExpert)) && PioneeringMission.isValid(aiUnit)) {
                 aiUnit.setMission(new PioneeringMission(getAIMain(), aiUnit));
                 isPioneerReq = false;
                 continue;
             }
 
             if (unit.isColonist() && unit.getOwner().isEuropean()) {
                 /*
                  * Motivated by (speed) performance: This map stores the
                  * distance between the unit and the destination of a Wish:
                  */
                 HashMap<Location, Integer> distances = new HashMap<Location, Integer>(121);
                 for (ArrayList<Wish> al : workerWishes.values()) {
                     for (Wish w : al) {
                         if (!distances.containsKey(w.getDestination())) {
                             distances.put(w.getDestination(), unit.getTurnsToReach(w.getDestination()));
                         }
                     }
                 }
 
                 // Check if this unit is needed as an expert (using:
                 // "WorkerWish"):
                 ArrayList<Wish> wishList = workerWishes.get(unit.getType());
                 WorkerWish bestWish = null;
                 int bestTurns = Integer.MAX_VALUE;
                 for (int i = 0; i < wishList.size(); i++) {
                     WorkerWish ww = (WorkerWish) wishList.get(i);
                     if (ww.getTransportable() != null) {
                         wishList.remove(i);
                         i--;
                         continue;
                     }
                     int turns = distances.get(ww.getDestination());
                     if (turns == Integer.MAX_VALUE) {
                         if (ww.getDestination().getTile() == null) {
                             turns = 5;
                         } else {
                             turns = 10;
                         }
                     } else if (turns > 5) {
                         turns = 5;
                     }
                     if (bestWish == null
                             || ww.getValue() - (turns * 2) > bestWish.getValue() - (bestTurns * 2)) {
                         bestWish = ww;
                         bestTurns = turns;
                     }
                 }
                 if (bestWish != null) {
                     bestWish.setTransportable(aiUnit);
                     aiUnit.setMission(new WishRealizationMission(getAIMain(), aiUnit, bestWish));
                     continue;
                 }
                 // Find a site for a new colony:
                 Tile colonyTile = null;
                 if (getPlayer().canBuildColonies()) {
                     colonyTile = BuildColonyMission.findColonyLocation(aiUnit.getUnit());
                     if (colonyTile != null) {
                         bestTurns = unit.getTurnsToReach(colonyTile);
                     }
                 }
 
                 // Check if we can find a better site to work than a new colony:
                 if (!fewColonies || colonyTile == null || bestTurns > 10) {
                     for (List<Wish> wishes : workerWishes.values()) {
                         for (int j = 0; j < wishes.size(); j++) {
                             WorkerWish ww = (WorkerWish) wishes.get(j);
                             if (ww.getTransportable() != null) {
                                 wishes.remove(j);
                                 j--;
                                 continue;
                             }
                             int turns = distances.get(ww.getDestination());
                             if (turns == Integer.MAX_VALUE) {
                                 if (ww.getDestination().getTile() == null) {
                                     turns = 5;
                                 } else {
                                     turns = 10;
                                 }
                             } else if (turns > 5) {
                                 turns = 5;
                             }
                             // TODO: Choose to build colony if the value of the
                             // wish is low.
                             if (bestWish == null
                                     || ww.getValue() - (turns * 2) > bestWish.getValue() - (bestTurns * 2)) {
                                 bestWish = ww;
                                 bestTurns = turns;
                             }
                         }
                     }
                 }
                 if (bestWish != null) {
                     bestWish.setTransportable(aiUnit);
                     aiUnit.setMission(new WishRealizationMission(getAIMain(), aiUnit, bestWish));
                     continue;
                 }
                 // Choose to build a new colony:
                 if (colonyTile != null) {
                     Mission mission = new BuildColonyMission(getAIMain(),
                                                              aiUnit,
                                                              colonyTile,
                                                              getPlayer().getColonyValue(colonyTile));
                     aiUnit.setMission(mission);
 
                     boolean isUnitOnCarrier = aiUnit.getUnit().isOnCarrier();
                     if (isUnitOnCarrier) {
                         AIUnit carrier = getAIUnit((Unit) aiUnit.getUnit().getLocation());
 
                         //make verification of carrier mission
                         Mission carrierMission = carrier.getMission();
 
                         boolean isCarrierMissionToTransport = carrierMission instanceof TransportMission;
                         if(!isCarrierMissionToTransport){
                         	throw new IllegalStateException("Carrier carrying unit not on a transport mission");
                         }
                         //transport unit to carrier destination (is this what is truly wanted?)
                         ((TransportMission) carrierMission).addToTransportList(aiUnit);
                     }
                     continue;
                 }
             }
 
             if (!aiUnit.hasMission()) {
                 if (aiUnit.getUnit().isOffensiveUnit()) {
                     aiUnit.setMission(new UnitWanderHostileMission(getAIMain(), aiUnit));
                 } else {
                     //non-offensive units should take shelter in a nearby colony,
                     //not try to be hostile
                     aiUnit.setMission(new IdleAtColonyMission(getAIMain(), aiUnit));
                 }
             }
         }
     }
 
     /**
      * Brings gifts to nice players with nearby colonies.
      */
     private void bringGifts() {
         logger.finest("Entering method bringGifts");
         if (!getPlayer().isIndian()) {
             return;
         }
         for (IndianSettlement indianSettlement : getPlayer().getIndianSettlements()) {
             // Do not bring gifts all the time:
             if (getAIRandom().nextInt(10) != 1) {
                 continue;
             }
             int alreadyAssignedUnits = 0;
             Iterator<Unit> ownedUnits = indianSettlement.getOwnedUnitsIterator();
             while (ownedUnits.hasNext()) {
                 if (getAIUnit(ownedUnits.next()).getMission() instanceof IndianBringGiftMission) {
                     alreadyAssignedUnits++;
                 }
             }
             if (alreadyAssignedUnits > MAX_NUMBER_OF_GIFTS_BEING_DELIVERED) {
                 continue;
             }
             // Creates a list of nearby colonies:
             ArrayList<Colony> nearbyColonies = new ArrayList<Colony>();
             for (Tile t: indianSettlement.getTile().getSurroundingTiles(MAX_DISTANCE_TO_BRING_GIFT)) {
                 if (t.getColony() != null
                         && IndianBringGiftMission.isValidMission(getPlayer(), t.getColony().getOwner())) {
                     nearbyColonies.add(t.getColony());
                 }
             }
             if (nearbyColonies.size() > 0) {
                 Colony target = nearbyColonies.get(getAIRandom().nextInt(nearbyColonies.size()));
                 Iterator<Unit> it2 = indianSettlement.getOwnedUnitsIterator();
                 AIUnit chosenOne = null;
                 while (it2.hasNext()) {
                     chosenOne = getAIUnit(it2.next());
                     if (chosenOne.getUnit().getLocation() instanceof Tile
                         && (chosenOne.getMission() == null
                             || chosenOne.getMission() instanceof UnitWanderHostileMission)) {
                         // Check that the colony can be reached:
                         PathNode pn = chosenOne.getUnit().findPath(indianSettlement.getTile(),
                                                                    target.getTile());
                         if (pn != null && pn.getTotalTurns() <= MAX_DISTANCE_TO_BRING_GIFT) {
                             chosenOne.setMission(new IndianBringGiftMission(getAIMain(), chosenOne, target));
                             break;
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Demands goods from players with nearby colonies.
      */
     private void demandTribute() {
         logger.finest("Entering method demandTribute");
         if (!getPlayer().isIndian()) {
             return;
         }
         for (IndianSettlement indianSettlement : getPlayer().getIndianSettlements()) {
             // Do not demand goods all the time:
             if (getAIRandom().nextInt(10) != 1) {
                 continue;
             }
             int alreadyAssignedUnits = 0;
             Iterator<Unit> ownedUnits = indianSettlement.getOwnedUnitsIterator();
             while (ownedUnits.hasNext()) {
                 if (getAIUnit(ownedUnits.next()).getMission() instanceof IndianDemandMission) {
                     alreadyAssignedUnits++;
                 }
             }
             if (alreadyAssignedUnits > MAX_NUMBER_OF_DEMANDS) {
                 continue;
             }
             // Creates a list of nearby colonies:
             ArrayList<Colony> nearbyColonies = new ArrayList<Colony>();
 
             for (Tile t: indianSettlement.getTile().getSurroundingTiles(MAX_DISTANCE_TO_MAKE_DEMANDS)) {
                 if (t.getColony() != null) {
                     nearbyColonies.add(t. getColony());
                 }
             }
             if (nearbyColonies.size() > 0) {
                 int targetTension = Integer.MIN_VALUE;
                 Colony target = null;
                 for (int i = 0; i < nearbyColonies.size(); i++) {
                     Colony t = nearbyColonies.get(i);
                     Player to = t.getOwner();
                     if (!getPlayer().hasContacted(to)
                         || !indianSettlement.hasContactedSettlement(to)) {
                         continue;
                     }
                     int tension = 1 + getPlayer().getTension(to).getValue()
                         + indianSettlement.getAlarm(to).getValue();
                     tension = getAIRandom().nextInt(tension);
                     if (tension > targetTension) {
                         targetTension = tension;
                         target = t;
                     }
                 }
                 if (target != null) {
                     Iterator<Unit> it2 = indianSettlement.getOwnedUnitsIterator();
                     AIUnit chosenOne = null;
                     while (it2.hasNext()) {
                         chosenOne = getAIUnit(it2.next());
                         if (chosenOne.getUnit().getLocation() instanceof Tile
                             && (chosenOne.getMission() == null
                                 || chosenOne.getMission() instanceof UnitWanderHostileMission)) {
                             // Check that the colony can be reached:
                             PathNode pn = chosenOne.getUnit().findPath(indianSettlement.getTile(),
                                                                        target.getTile());
                             if (pn != null && pn.getTotalTurns() <= MAX_DISTANCE_TO_MAKE_DEMANDS) {
                                 // Make it less probable that nice players get targeted
                                 // for a demand mission:
                                 Player tp = target.getOwner();
                                 int tension = 1 + getPlayer().getTension(tp).getValue()
                                     + indianSettlement.getAlarm(tp).getValue();
                                 if (getAIRandom().nextInt(tension) > Tension.Level.HAPPY.getLimit()) {
                                     chosenOne.setMission(new IndianDemandMission(getAIMain(), chosenOne,
                                                                                  target));
                                     break;
                                 }
                             }
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Calls {@link AIColony#createAIGoods()} for every colony this player owns.
      */
     private void createAIGoodsInColonies() {
         logger.finest("Entering method createAIGoodsInColonies");
         if (!getPlayer().isEuropean()) {
             return;
         }
         Iterator<AIColony> ci = getAIColonyIterator();
         while (ci.hasNext()) {
             AIColony c = ci.next();
             c.createAIGoods();
         }
     }
 
     /**
      * Makes every unit perform their mission.
      */
     private void doMissions() {
         logger.finest("Entering method doMissions");
         Iterator<AIUnit> aiUnitsIterator = getAIUnitIterator();
         while (aiUnitsIterator.hasNext()) {
             AIUnit aiUnit = aiUnitsIterator.next();
             if (aiUnit.hasMission() && aiUnit.getMission().isValid()
                     && !(aiUnit.getUnit().isOnCarrier())) {
                 try {
                     aiUnit.doMission(getConnection());
                 } catch (Exception e) {
                     logger.log(Level.WARNING, "doMissions failed", e);
                 }
             }
         }
     }
 
     /**
      * Evaluate allocating a unit to the defence of a colony.
      * Temporary helper method for giveMilitaryMission.
      *
      * Only public for testAssignDefendSettlementMission.
      *
      * @param unit The <code>Unit</code> that is to defend.
      * @param colony The <code>Colony</code> to defend.
      * @param turns The turns for the unit to reach the colony.
      * @return A value for such a mission.
      */
     public int getDefendColonyMissionValue(Unit unit, Colony colony,
                                            int turns) {
         // Sanitation
         if (unit == null || colony == null) return Integer.MIN_VALUE;
 
         int value = 10025 - 10 * turns;
         int defenders = 0;
 
         // Reduce value in proportion to the number of defenders.
         for (AIUnit au : getAIUnits()) {
             Mission m = au.getMission();
             if (m != null && m instanceof DefendSettlementMission
                 && ((DefendSettlementMission) m).getSettlement() == colony
                 && au.getUnit().getColony() == colony) {
                 value -= 500;
                 defenders++;
             }
         }
 
         // The REF garrisons thinly.
         if (unit.getOwner().isREF()) {
             if (defenders > 0) return 0;
             value -= 100;
         }
 
         // Reduce value if defenders are protected.
         if (colony.hasStockade()) {
             if (defenders > colony.getStockade().getLevel() + 1) {
                 value = 20 - defenders;
             } else {
                 value -= 1000 * colony.getStockade().getLevel();
             }
         }
 
         // Do not return negative except for error cases.
         return Math.max(0, value);
     }
 
     /**
      * Evaluate a potential seek and destroy mission for a given unit
      * to a given tile.
      * TODO: revisit and rebalance the mass of magic numbers.
      *
      * @param unit The <code>Unit</code> to do the mission.
      * @param newTile The <code>Tile</code> to go to.
      * @param turns How long to travel to the tile.
      * @return A score for the proposed mission.
      */
     int getUnitSeekAndDestroyMissionValue(Unit unit, Tile newTile, int turns) {
         if (!isTargetValidForSeekAndDestroy(unit, newTile)) {
             return Integer.MIN_VALUE;
         }
         Settlement settlement = newTile.getSettlement();
         Unit defender = newTile.getDefendingUnit(unit);
         // Take distance to target into account
         int value = 10020 - turns * 100;
 
         if (settlement != null) {
             // Do not cheat and look inside the settlement.
             // Just use visible facts about it.
             // TODO: if we are the REF and there is a significant Tory
             // population inside, assume traitors have briefed us.
             if (settlement instanceof Colony) {
                 // Favour high population and weak fortifications.
                 Colony colony = (Colony) settlement;
                 value += 50 * colony.getUnitCount();
                 if (colony.hasStockade()) {
                     value -= 1000 * colony.getStockade().getLevel();
                 }
             } else if (settlement instanceof IndianSettlement) {
                 // Favour the most hostile settlements
                 IndianSettlement is = (IndianSettlement) settlement;
                 Tension tension = is.getAlarm(unit.getOwner());
                 if (tension != null) value += tension.getValue();
             }
         } else if (defender != null) {
             CombatModel combatModel = unit.getGame().getCombatModel();
             float off = combatModel.getOffencePower(unit, defender);
             float def = combatModel.getDefencePower(unit, defender);
 
             Unit train = getBestTreasureTrain(newTile);
             if (train != null) {
                 value += Math.min(train.getTreasureAmount() / 10, 50);
             }
 
             if (defender.getType().getOffence() > 0) {
                 value += 200 - def * 2 - turns * 50;
             }
 
             value += combatModel.getOffencePower(defender, unit)
                 - combatModel.getDefencePower(defender, unit);
 
             if (!defender.isNaval()
                 && defender.hasAbility("model.ability.expertSoldier")
                 && !defender.isArmed()) {
                 value += 10 - def * 2 - turns * 25;
             }
             if (value < 0) value = 0;
         }
         logger.finest("getUnitSeekAndDestroyMissionValue " + unit.getId()
                       + " v " + ((settlement != null) ? settlement.getId()
                                  : (defender != null) ? defender.getId()
                                  : "none")
                       + " = " + value);
         return value;
     }
 
     /**
      * Find out if a tile contains a suitable target for seek-and-destroy.
      * TODO: Package for access by a test only - necessary?
      *
      * @param attacker The attacking <code>Unit</code>.
      * @param tile The <code>Tile</code> to attack into.
      * @return True if an attack can be launched.
      */
     public boolean isTargetValidForSeekAndDestroy(Unit attacker, Tile tile) {
         Player attackerPlayer = attacker.getOwner();
 
         // Insist the attacker exists.
         if (attacker == null) return false;
 
         // Determine the defending player.
         Settlement settlement = tile.getSettlement();
         Unit defender = tile.getDefendingUnit(attacker);
         Player defenderPlayer = (settlement != null) ? settlement.getOwner()
             : (defender != null) ? defender.getOwner()
             : null;
 
         // Insist there be a defending player.
         if (defenderPlayer == null) return false;
 
         // Can not attack our own units.
         if (attackerPlayer == defenderPlayer) return false;
 
         // If European, do not attack if not at war.
         // If native, do not attack if not at war and at least content.
         // Otherwise some attacks are allowed even if not at war.
         boolean atWar = attackerPlayer.atWarWith(defenderPlayer);
         if (attackerPlayer.isEuropean()) {
             if (!atWar) return false;
         } else if (attackerPlayer.isIndian()) {
             if (!atWar && attackerPlayer.getTension(defenderPlayer)
                 .getLevel().compareTo(Tension.Level.CONTENT) <= 0) {
                 return false;
             }
         }
 
         // A naval unit can never attack a land unit or settlement,
         // but a land unit *can* attack a naval unit if it is on land.
         // Otherwise naval units can only fight at sea, land units
         // only on land.
         if (attacker.isNaval()) {
             if (settlement != null
                 || !defender.isNaval() || defender.getTile().isLand()) {
                 return false;
             }
         } else {
             if (defender != null && !defender.getTile().isLand()) {
                 return false;
             }
         }
 
         // Otherwise, attack.
         return true;
     }
 
     /**
      * Gives a military mission to the given unit.
      *
      * Old comment: Temporary method for giving a military mission.
      * This method will be removed when "MilitaryStrategy" and the
      * "Tactic"-classes has been implemented.
      *
      * @param aiUnit The <code>AIUnit</code> to give a mission to.
      */
     public void giveMilitaryMission(AIUnit aiUnit) {
         logger.finest("Entering giveMilitaryMission for: " + aiUnit.getUnit());
         final AIMain aiMain = getAIMain();
         final Player player = getPlayer();
         Mission mission = null;
 
         if (player.isIndian()) {
             mission = new UnitWanderHostileMission(aiMain, aiUnit);
         } else {
             final Unit unit = aiUnit.getUnit();
             final Map map = aiMain.getGame().getMap();
             final Unit carrier = (!unit.isOnCarrier()) ? null
                 : (Unit) unit.getLocation();
             Ownable bestTarget = null;         // The best target for a mission.
             int bestValue = Integer.MIN_VALUE; // The value of the target above.
 
             // Determine starting tile.
             Tile startTile = ((carrier != null) ? carrier : unit).getTile();
             if (startTile == null) {
                 startTile = ((carrier != null) ? carrier : unit)
                     .getFullEntryLocation();
             }
             if (startTile == null) {
                 logger.warning("giveMilitaryMission failed, no tile for: "
                                + unit);
                 return;
             }
 
             // Check if the unit is located on a Tile with a
             // Settlement which requires defenders.
             if (unit.getColony() != null) {
                 bestTarget = unit.getColony();
                 bestValue = getDefendColonyMissionValue(unit,
                                                         (Colony) bestTarget, 0);
             }
 
             // Checks if a nearby colony requires additional defence:
             GoalDecider gd = new GoalDecider() {
                     private PathNode best = null;
 
                     private int bestValue = 0;
 
                     public PathNode getGoal() {
                         return best;
                     }
 
                     public boolean hasSubGoals() {
                         return true;
                     }
 
                     private int getValue(PathNode pathNode) {
                         // Note: check *unit*, not *u*, which could be
                         // the carrier unit is on.
                         Colony colony = pathNode.getTile().getColony();
                         return (colony == null
                                 || colony.getOwner() != unit.getOwner())
                             ? Integer.MIN_VALUE
                             : getDefendColonyMissionValue(unit, colony,
                                                           pathNode.getTurns());
                     }
 
                     public boolean check(Unit u, PathNode pathNode) {
                         int value = getValue(pathNode);
                         if (value > bestValue) {
                             bestValue = value;
                             best = pathNode;
                             return true;
                         }
                         return false;
                     }
                 };
             final int MAXIMUM_TURNS_TO_SETTLEMENT = 10;
             PathNode bestPath = map.search(unit, startTile, gd,
                                            CostDeciders.avoidIllegal(),
                                            MAXIMUM_TURNS_TO_SETTLEMENT,
                                            carrier);
             if (bestPath != null) {
                 PathNode last = bestPath.getLastNode();
                 Colony colony = last.getTile().getColony();
                 int value = getDefendColonyMissionValue(unit, colony,
                                                         last.getTurns());
                 if (value > bestValue) {
                     bestTarget = colony;
                     bestValue = value;
                 }
             }
 
             // Search for the closest target of a pre-existing
             // UnitSeekAndDestroyMission.
             Location bestExistingTarget = null;
             int bestDistance = Integer.MAX_VALUE;
             for (AIUnit au : getAIUnits()) {
                 Unit u = au.getUnit();
                 if (u.getTile() == null
                     || !(au.getMission() instanceof UnitSeekAndDestroyMission))
                     continue;
                 UnitSeekAndDestroyMission usdm
                     = (UnitSeekAndDestroyMission) au.getMission();
                 int distance = unit.getTurnsToReach(startTile,
                                                     usdm.getTarget().getTile());
                 if (distance < bestDistance) {
                     bestExistingTarget = usdm.getTarget();
                     bestDistance = distance;
                 }
             }
             if (bestExistingTarget != null) {
                 int value = getUnitSeekAndDestroyMissionValue(unit,
                         bestExistingTarget.getTile(), bestDistance);
                 if (value > bestValue) {
                     bestValue = value;
                     bestTarget = (Ownable) bestExistingTarget;
                 }
             }
 
             // General check for available targets for seek-and-destroy.
             GoalDecider targetDecider = new GoalDecider() {
                     private PathNode bestTarget = null;
 
                     private int bestNewTargetValue = Integer.MIN_VALUE;
 
                     public PathNode getGoal() {
                         return bestTarget;
                     }
 
                     public boolean hasSubGoals() {
                         return true;
                     }
 
                     private int getValue(PathNode pathNode) {
                         // Note: check *unit*, not *u*, which could be
                         // the carrier unit is on.
                         Tile tile = pathNode.getTile();
                         return (isTargetValidForSeekAndDestroy(unit, tile))
                             ? getUnitSeekAndDestroyMissionValue(unit, tile,
                                                                 pathNode.getTurns())
                             : Integer.MIN_VALUE;
                     }
 
                     public boolean check(Unit u, PathNode pathNode) {
                         int value = getValue(pathNode);
                         if (value > bestNewTargetValue) {
                             bestTarget = pathNode;
                             bestNewTargetValue = value;
                             return true;
                         }
                         return false;
                     }
                 };
             PathNode newTarget = map.search(unit, startTile, targetDecider,
                                             CostDeciders.avoidIllegal(),
                                             Integer.MAX_VALUE, carrier);
             if (newTarget != null) {
                 Tile targetTile = newTarget.getLastNode().getTile();
                 int value = getUnitSeekAndDestroyMissionValue(unit,
                         targetTile, newTarget.getTotalTurns());
                 if (value > bestValue) {
                     bestValue = value;
                     if (targetTile.getSettlement() != null) {
                         bestTarget = targetTile.getSettlement();
                     } else if (getBestTreasureTrain(targetTile) != null) {
                         bestTarget = getBestTreasureTrain(targetTile);
                     } else {
                         bestTarget = targetTile.getDefendingUnit(unit);
                     }
                 }
             }
 
             // Use the best target.
             if (bestTarget != null && bestValue > 0) {
                 if (bestTarget instanceof Settlement
                     && ((Settlement) bestTarget).getOwner() == player) {
                     Settlement settlement = (Settlement) bestTarget;
                     mission = new DefendSettlementMission(aiMain, aiUnit,
                                                           settlement);
                 } else {
                     mission = new UnitSeekAndDestroyMission(aiMain, aiUnit,
                                                             (Location) bestTarget);
                 }
             } else {
                 // Just wander around if there is nothing better to do.
                 mission = new UnitWanderHostileMission(aiMain, aiUnit);
             }
         }
 
         aiUnit.setMission(mission);
         logger.finest("giveMilitaryMission found: " + mission
                       + " for unit: " + aiUnit);
     }
 
     private int getTurns(Transportable t, TransportMission tm) {
         if (t.getTransportDestination() != null
             && t.getTransportDestination().getTile()
             == tm.getUnit().getTile()) {
             return 0;
         }
         PathNode path = tm.getPath(t);
         return (path == null) ? -1 : path.getTotalTurns();
     }
 
     /**
      * Assign transportable units and goods to available carriers
      * transport lists.
      */
     private void createTransportLists() {
         logger.finest("Entering method createTransportLists");
         if (!getPlayer().isEuropean()) return;
         ArrayList<Transportable> transportables
             = new ArrayList<Transportable>();
 
         // Collect non-naval units needing transport.
         for (AIUnit au : getAIUnits()) {
             if (!au.getUnit().isNaval()
                 && au.getTransport() == null
                 && au.getTransportDestination() != null) {
                 transportables.add(au);
             }
         }
 
         // Add goods to transport
         Iterator<AIColony> aci = getAIColonyIterator();
         while (aci.hasNext()) {
             AIColony ac = aci.next();
             Iterator<AIGoods> agi = ac.getAIGoodsIterator();
             while (agi.hasNext()) {
                 AIGoods ag = agi.next();
                 if (ag.getTransportDestination() != null && ag.getTransport() == null) {
                     transportables.add(ag);
                 }
             }
         }
 
         // Update the priority.
         for (Transportable t : transportables) {
             t.increaseTransportPriority();
         }
 
         // Order the transportables by priority.
         Collections.sort(transportables, new Comparator<Transportable>() {
             public int compare(Transportable o1, Transportable o2) {
                 if (o1 == o2) return 0;
                 int result = o2.getTransportPriority() - o1.getTransportPriority();
                 return (result == 0) ? o1.getId().compareTo(o2.getId())
                     : result;
             }
         });
 
         // Collect current transport missions with space available.
         ArrayList<TransportMission> availableMissions
             = new ArrayList<TransportMission>();
         for (AIUnit au : getAIUnits()) {
             if (au.hasMission() && au.getMission() instanceof TransportMission
                 && au.getUnit().getSpaceLeft() > 0) {
                 availableMissions.add((TransportMission) au.getMission());
             }
         }
 
         // If a transportable is already on a carrier, just add it to
         // its transport list.  Note however that it may not be
         // possible to complete such transport, in which case, the
         // carrier should dump the transportable in the nearest
         // colony.
         for (Transportable t : new ArrayList<Transportable>(transportables)) {
             Location transportableLoc = t.getTransportLocatable().getLocation();
             boolean onCarrier = transportableLoc instanceof Unit;
             if (onCarrier) {
                 AIUnit carrierAI = getAIUnit((Unit) transportableLoc);
                 Mission m = carrierAI.getMission();
                 if (m instanceof TransportMission) {
                     ((TransportMission) m).addToTransportList(t);
                 }
                 transportables.remove(t);
             }
         }
 
         // For all remaining transportables, find the best carrier.
         // That is the one with space available that is closest.
         // TODO: this concentrates on packing, but ignores destinations
         // which is definitely going to be inefficient
         // TODO: be smarter about removing bestTransport from
         // availableMissions when it is full.
         while (!transportables.isEmpty() && !availableMissions.isEmpty()) {
             Transportable t = transportables.remove(0);
             TransportMission bestTransport = null;
             int bestTransportSpace = 0;
             int bestTransportTurns = Integer.MAX_VALUE;
             for (TransportMission tm : availableMissions) {
                 int transportSpace = tm.getAvailableSpace(t);
                 if (transportSpace <= 0) continue;
 
                 if (t.getTransportSource().getTile()
                     == tm.getUnit().getTile()) {
                     if (bestTransportTurns > 0
                         || (bestTransportTurns == 0
                             && transportSpace > bestTransportSpace)) {
                         bestTransport = tm;
                         bestTransportSpace = transportSpace;
                         bestTransportTurns = 0;
                     }
                     continue;
                 }
                 int totalTurns = getTurns(t, tm);
                 if (totalTurns <= 0) continue;
                 if (totalTurns < bestTransportTurns
                     || (totalTurns == bestTransportTurns
                         && transportSpace > bestTransportSpace)) {
                     bestTransport = tm;
                     bestTransportSpace = transportSpace;
                     bestTransportTurns = totalTurns;
                 }
             }
             if (bestTransport == null) {
                 logger.finest("Transport failed for: " + t);
                 continue;
             }
             bestTransport.addToTransportList(t);
             logger.finest("Transport found for: " + t
                           + " using: " + bestTransport);
 
             // See if any other transportables are present at the same
             // location and can fit.
             for (int i = 0; i < transportables.size(); i++) {
                 Transportable t2 = transportables.get(i);
                 if (t2.getTransportLocatable().getLocation()
                     == t.getTransportLocatable().getLocation()
                     && bestTransport.getAvailableSpace(t2) > 0) {
                     transportables.remove(t2);
                     bestTransport.addToTransportList(t2);
                     logger.finest("Transport piggyback for: " + t2
                                   + " using: " + t);
                 }
             }
         }
     }
 
     /**
      * Finds the treasure train carrying the largest treasure located
      * on the given <code>Tile</code>.
      *
      * @param tile The <code>Tile</code> to check.
      * @return The best treasure train found or null if none present.
      */
     private Unit getBestTreasureTrain(Tile tile) {
         Unit best = null;
         for (Unit unit : tile.getUnitList()) {
             if (unit.canCarryTreasure()
                 && (best == null
                     || best.getTreasureAmount() < unit.getTreasureAmount())) {
                 best = unit;
             }
         }
         return best;
     }
 
 
     /**
      * Writes this object to an XML stream.
      *
      * @param out The target stream.
      * @throws XMLStreamException if there are any problems writing to the
      *             stream.
      */
     @Override
     protected void toXMLImpl(XMLStreamWriter out) throws XMLStreamException {
         out.writeStartElement(getXMLElementTagName());
         out.writeAttribute(ID_ATTRIBUTE, getId());
         out.writeEndElement();
     }
 
     /**
      * Reads information for this object from an XML stream.
      *
      * @param in The input stream with the XML.
      * @throws XMLStreamException if there are any problems reading from the
      *             stream.
      */
     @Override
     protected void readFromXMLImpl(XMLStreamReader in)
         throws XMLStreamException {
         setPlayer((ServerPlayer) getAIMain()
             .getFreeColGameObject(in.getAttributeValue(null, ID_ATTRIBUTE)));
         in.nextTag();
     }
 
     /**
      * Returns the tag name of the root element representing this object.
      *
      * ATTN: For compatibility, this has the same tag name as its
      * super class, the previous implementation of all
      * AIPlayers. Eventually change this at some point.
      *
      * @return the tag name.
      */
     public static String getXMLElementTagName() {
         return "aiPlayer";
     }
 }
