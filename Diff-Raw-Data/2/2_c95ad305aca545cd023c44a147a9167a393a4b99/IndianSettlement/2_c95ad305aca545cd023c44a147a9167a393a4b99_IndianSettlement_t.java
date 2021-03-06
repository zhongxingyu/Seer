 /**
  *  Copyright (C) 2002-2007  The FreeCol Team
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
 
 package net.sf.freecol.common.model;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 import net.sf.freecol.common.model.Tension;
 import net.sf.freecol.common.model.Tension.Level;
 
 import org.w3c.dom.Element;
 
 
 /**
  * Represents an Indian settlement.
  */
 public class IndianSettlement extends Settlement {
 
     private static final Logger logger = Logger.getLogger(IndianSettlement.class.getName());
 
     public static final int MAX_HORSES_PER_TURN = 2;
     public static final int TALES_RADIUS = 6;
 
     public static final String UNITS_TAG_NAME = "units";
     public static final String OWNED_UNITS_TAG_NAME = "ownedUnits";
     public static final String IS_VISITED_TAG_NAME = "isVisited";
     public static final String ALARM_TAG_NAME = "alarm";
     public static final String MISSIONARY_TAG_NAME = "missionary";
     public static final String WANTED_GOODS_TAG_NAME = "wantedGoods";
 
     /** The amount of goods a brave can produce a single turn. */
     //private static final int WORK_AMOUNT = 5;
 
     /**
      * The amount of raw material that should be available before
      * producing manufactured goods.
      */
     public static final int KEEP_RAW_MATERIAL = 50;
 
     /**
      * This is the skill that can be learned by Europeans at this
      * settlement.  At the server side its value will be null when the
      * skill has already been taught to a European.  At the client
      * side the value null is also possible in case the player hasn't
      * checked out the settlement yet.
      */
     protected UnitType learnableSkill = null;
 
     /** The goods this settlement wants. */
     protected GoodsType[] wantedGoods = new GoodsType[] {null, null, null};
 
     /**
      * A map that tells if a player has visited the settlement.
      *
      * At the client side, only the information regarding the player
      * on that client should be included.
      */
     protected Set<Player> visitedBy = new HashSet<Player>();
 
     /** Units present at this settlement. */
     protected List<Unit> units = Collections.emptyList();
 
     /** Units that belong to this settlement. */
     protected ArrayList<Unit> ownedUnits = new ArrayList<Unit>();
 
     /** The missionary at this settlement. */
     protected Unit missionary = null;
 
     /** Used for monitoring the progress towards creating a convert. */
     protected int convertProgress = 0;
 
     /** The number of the turn during which the last tribute was paid. */
     protected int lastTribute = 0;
 
     /**
      * Stores the alarm levels. <b>Only used by AI.</b>
      * "Alarm" means: Tension-with-respect-to-a-player-from-an-IndianSettlement.
      * Alarm is overloaded with the concept of "contact".  If a settlement
      * has never been contacted by a player, alarm.get(player) will be null.
      * Acts causing contact initialize this variable.
      */
     private java.util.Map<Player, Tension> alarm = new HashMap<Player, Tension>();
 
     // sort goods types descending by price
     private final Comparator<GoodsType> wantedGoodsComparator = new Comparator<GoodsType>() {
         public int compare(GoodsType goodsType1, GoodsType goodsType2) {
             return getPrice(goodsType2, 100) - getPrice(goodsType1, 100);
         }
     };
 
     // sort goods descending by amount and price when amounts are equal
     private final Comparator<Goods> exportGoodsComparator = new Comparator<Goods>() {
         public int compare(Goods goods1, Goods goods2) {
             if (goods2.getAmount() == goods1.getAmount()) {
                 return getPrice(goods2) - getPrice(goods1);
             } else {
                 return goods2.getAmount() - goods1.getAmount();
             }
         }
     };
 
 
     /**
      * Constructor for ServerIndianSettlement.
      */
     protected IndianSettlement() {
         // empty constructor
     }
 
     /**
      * Constructor for ServerIndianSettlement.
      *
      * @param game The <code>Game</code> in which this object belong.
      * @param owner The <code>Player</code> owning this settlement.
      * @param name The name for this settlement.
      * @param tile The location of the <code>IndianSettlement</code>.
      */
     protected IndianSettlement(Game game, Player owner, String name,
                                Tile tile) {
         super(game, owner, name, tile);
     }
 
 
     /**
      * Initiates a new <code>IndianSettlement</code> from an <code>Element</code>.
      *
      * @param game The <code>Game</code> in which this object belong.
      * @param in The input stream containing the XML.
      * @throws XMLStreamException if a problem was encountered
      *      during parsing.
      */
     public IndianSettlement(Game game, XMLStreamReader in) throws XMLStreamException {
         super(game, in);
         readFromXML(in);
     }
 
     /**
      * Initiates a new <code>IndianSettlement</code> from an <code>Element</code>.
      *
      * @param game The <code>Game</code> in which this object belong.
      * @param e An XML-element that will be used to initialize
      *      this object.
      */
     public IndianSettlement(Game game, Element e) {
         super(game, e);
         readFromXMLElement(e);
     }
 
     /**
      * Initiates a new <code>IndianSettlement</code>
      * with the given ID. The object should later be
      * initialized by calling either
      * {@link #readFromXML(XMLStreamReader)} or
      * {@link #readFromXMLElement(Element)}.
      *
      * @param game The <code>Game</code> in which this object belong.
      * @param id The unique identifier for this object.
      */
     public IndianSettlement(Game game, String id) {
         super(game, id);
     }
 
     /**
      * Gets the name of this <code>Settlement</code> for a particular player.
      *
      * @param player A <code>Player</code> to return the name for.
      * @return The name as a <code>String</code>.
      */
     public String getNameFor(Player player) {
         return (hasContactedSettlement(player)) ? getName()
             : "indianSettlement.nameUnknown";
     }
 
     /**
      * Returns a suitable (non-unique) name.
      * @return The name of this settlement.
      */
     public StringTemplate getLocationName() {
         return StringTemplate.name(getName());
     }
 
     /**
      * Returns a suitable (non-unique) name for a particular player.
      *
      * @param player The <code>Player</code> to prepare the name for.
      * @return The name of this settlement.
      */
     public StringTemplate getLocationNameFor(Player player) {
         return StringTemplate.name(getNameFor(player));
     }
 
     /**
      * Get the year of the last tribute.
      *
      * @return The year of the last tribute.
      */
     public int getLastTribute() {
         return lastTribute;
     }
 
     /**
      * Set the year of the last tribute.
      *
      * @param lastTribute The new last tribute year.
      */
     public void setLastTribute(int lastTribute) {
         this.lastTribute = lastTribute;
     }
 
     /**
      * Gets the alarm level towards the given player.
      *
      * @param player The <code>Player</code> to get the alarm level for.
      * @return The current alarm level or null if the settlement has not
      *     encoutered the player.
      */
     public Tension getAlarm(Player player) {
         return alarm.get(player);
     }
 
     /**
      * Sets alarm towards the given player.
      *
      * @param newAlarm The new alarm value.
      */
     public void setAlarm(Player player, Tension newAlarm) {
         if (player != null && player != owner) alarm.put(player, newAlarm);
      }
 
     /**
      * Change the alarm level of this settlement by a given amount.
      *
      * @param player The <code>Player</code> the alarm level changes wrt.
      * @param amount The amount to change the alarm by.
      * @return True if the <code>Tension.Level</code> of the
      *     settlement alarm changes as a result of this change.
      */
     private boolean changeAlarm(Player player, int amount) {
         Tension alarm = getAlarm(player);
         Level oldLevel = alarm.getLevel();
         alarm.modify(amount);
         return oldLevel != alarm.getLevel();
     }
 
     /**
      * Gets a messageId for a short alarm message associated with the
      * alarm level of this player.
      *
      * @param player The other <code>Player</code>.
      * @return The alarm messageId.
      */
     public String getShortAlarmLevelMessageId(Player player) {
         return (!player.hasContacted(owner)) ? "wary"
             : (hasContactedSettlement(player)) ? getAlarm(player).toString()
             : "indianSettlement.tensionUnknown";
     }
 
     /**
      * Gets a messageId for an alarm message associated with the
      * alarm level of this player.
      *
      * @param player The other <code>Player</code>.
      * @return The alarm messageId.
      */
     public String getAlarmLevelMessageId(Player player) {
         Tension alarm = (hasContactedSettlement(player)) ? getAlarm(player)
             : new Tension(Tension.TENSION_MIN);
         return "indianSettlement.alarm." + alarm.toString();
     }
 
     /**
      * Has a player contacted this settlement?
      *
      * @param player The <code>Player</code> to check.
      * @return True if the player has contacted this settlement.
      */
     public boolean hasContactedSettlement(Player player) {
         return getAlarm(player) != null;
     }
 
     /**
      * Make contact with this settlement (if it has not been
      * previously contacted).  Initialize tension level to the general
      * level with respect to the contacting player--- effectively the
      * average reputation of this player with the overall tribe.
      *
      * @param player The <code>Player</code> making contact.
      * @return True if this was indeed the first contact between settlement
      *     and player.
      */
     public boolean makeContactSettlement(Player player) {
         if (!hasContactedSettlement(player)) {
             setAlarm(player, new Tension(owner.getTension(player).getValue()));
             return true;
          }
          return false;
      }
 
     /**
      * Modifies the alarm level towards the given player due to an event
      * at this settlement, and propagate the alarm upwards through the
      * tribe.
      *
      * @param player The <code>Player</code>.
      * @param addToAlarm The amount to add to the current alarm level.
      * @return A list of settlements whose alarm level has changed.
      */
     public List<FreeColGameObject> modifyAlarm(Player player, int addToAlarm) {
         boolean change = makeContactSettlement(player);
         Level oldLevel = getAlarm(player).getLevel();
         change |= changeAlarm(player, addToAlarm);
 
         // Propagate alarm upwards.  Capital has a greater impact.
         List<FreeColGameObject> modified = owner.modifyTension(player,
                 ((isCapital()) ? addToAlarm : addToAlarm/2), this);
         if (change) {
             modified.add(this);
         }
         logger.finest("Alarm at " + getName()
             + " toward " + player.getName()
             + " modified by " + Integer.toString(addToAlarm)
             + " now = " + Integer.toString(getAlarm(player).getValue()));
         return modified;
     }
 
     /**
      * Propagates a global change in tension down to a settlement.
      * Only apply the change if the settlement is aware of the player
      * causing alarm.
      *
      * @param player The <code>Player</code> towards whom the alarm is felt.
      * @param addToAlarm The amount to add to the current alarm level.
      * @return True if the alarm level changes as a result of this change.
      */
     public boolean propagateAlarm(Player player, int addToAlarm) {
         if (hasContactedSettlement(player)) {
             return changeAlarm(player, addToAlarm);
         }
         return false;
     }
 
 
     /**
      * Returns true if a European player has visited this settlement
      * to speak with the chief.
      *
      * @return True if a European player has visited this settlement.
      */
     public boolean hasBeenVisited() {
         Iterator<Player> playerIterator = visitedBy.iterator();
         while (playerIterator.hasNext()) {
             if (playerIterator.next().isEuropean()) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Returns true if a the given player has visited this settlement
      * to speak with the chief.
      *
      * @param player The <code>Player</code> to check.
      * @return True if the player has visited this settlement to speak
      *     with the chief.
      */
     public boolean hasBeenVisited(Player player) {
         return visitedBy.contains(player);
     }
 
     /**
      * Sets the visited status of this settlement to true, indicating
      * that a European player has had a chat with the chief.
      *
      * @param player The visiting <code>Player</code>.
      */
     public void setVisited(Player player) {
         if (!hasBeenVisited(player)) {
             makeContactSettlement(player); // Just to be sure
             visitedBy.add(player);
         }
     }
 
     /**
      * Is a unit permitted to make contact with this settlement?
      * The unit must be from a nation that has already made contact,
      * or in the first instance, must be arriving by land, with the
      * exception of trading ships.
      *
      * @param unit The <code>Unit</code> that proposes to contact this
      *             settlement.
      * @return True if the settlement accepts such contact.
      */
     public boolean allowContact(Unit unit) {
         return hasBeenVisited(unit.getOwner())
             || !unit.isNaval()
             || unit.getGoodsCount() > 0;
     }
 
 
     /**
      * Adds the given <code>Unit</code> to the list of units that belongs to this
      * <code>IndianSettlement</code>.
      *
      * @param unit The <code>Unit</code> to be added.
      */
     public void addOwnedUnit(Unit unit) {
         if (unit == null) {
             throw new IllegalArgumentException("Parameter 'unit' must not be 'null'.");
         }
 
         if (!ownedUnits.contains(unit)) {
             ownedUnits.add(unit);
         }
     }
 
 
     /**
      * Gets an iterator over all the units this
      * <code>IndianSettlement</code> is owning.
      *
      * @return The <code>Iterator</code>.
      */
     public Iterator<Unit> getOwnedUnitsIterator() {
         return ownedUnits.iterator();
     }
 
 
     /**
      * Removes the given <code>Unit</code> to the list of units that
      * belongs to this <code>IndianSettlement</code>. Returns true if
      * the Unit was removed.
      *
      * @param unit The <code>Unit</code> to be removed from the
      *       list of the units this <code>IndianSettlement</code>
      *       owns.
      * @return a <code>boolean</code> value
      */
     public boolean removeOwnedUnit(Unit unit) {
         if (unit == null) {
             throw new IllegalArgumentException("Parameter 'unit' must not be 'null'.");
         }
         return ownedUnits.remove(unit);
     }
 
 
     /**
      * Returns the skill that can be learned at this settlement.
      * @return The skill that can be learned at this settlement.
      */
     public UnitType getLearnableSkill() {
         return learnableSkill;
     }
 
     /**
      * Gets the missionary from this settlement.
      *
      * @return The missionary at this settlement, or null if there is none.
      */
     public Unit getMissionary() {
         return missionary;
     }
 
     /**
      * Sets the missionary for this settlement.
      *
      * @param missionary The missionary for this settlement.
      */
     public void setMissionary(Unit missionary) {
         this.missionary = missionary;
     }
 
     /**
      * Gets the missionary from this settlement if there is one and
      * it is owned by a specified player.
      *
      * @param player The player purported to own the missionary
      * @return The missionary from this settlement if there is one and
      *     it is owned by the specified player, otherwise null.
      */
     public Unit getMissionary(Player player) {
         return (missionary == null || missionary.getOwner() != player) ? null
             : missionary;
     }
 
     /**
      * Gets the convert progress status for this settlement.
      *
      * @return The convert progress status.
      */
     public int getConvertProgress() {
         return convertProgress;
     }
 
     /**
      * Sets the convert progress status for this settlement.
      *
      * @param progress The new convert progress status.
      */
     public void setConvertProgress(int progress) {
         convertProgress = progress;
     }
 
 
     public GoodsType[] getWantedGoods() {
         return wantedGoods;
     }
 
     public void setWantedGoods(int index, GoodsType type) {
         if (0 <= index && index <= 2) {
             wantedGoods[index] = type;
         }
     }
 
     /**
      * Sets the learnable skill for this Indian settlement.
      * @param skill The new learnable skill for this Indian settlement.
      */
     public void setLearnableSkill(UnitType skill) {
         learnableSkill = skill;
     }
 
 
     /**
      * Adds a <code>Locatable</code> to this Location.
      *
      * @param locatable The <code>Locatable</code> to add to this Location.
      */
     public void add(Locatable locatable) {
         if (locatable instanceof Unit) {
             if (!units.contains(locatable)) {
                 Unit indian = (Unit)locatable;
                 if (units.equals(Collections.emptyList())) {
                     units = new ArrayList<Unit>();
                 }
                 units.add(indian);
                 if (indian.getIndianSettlement() == null) {
                     // Adopt homeless Indians
                     indian.setIndianSettlement(this);
                 }
             }
         } else if (locatable instanceof Goods) {
             addGoods((Goods)locatable);
         } else {
             logger.warning("Tried to add an unrecognized 'Locatable' to a IndianSettlement.");
         }
     }
 
 
     /**
      * Removes a <code>Locatable</code> from this Location.
      *
      * @param locatable The <code>Locatable</code> to remove from this Location.
      */
     public void remove(Locatable locatable) {
         if (locatable instanceof Unit) {
             if (!units.remove(locatable)) {
                 logger.warning("Failed to remove unit " + ((Unit)locatable).getId() + " from IndianSettlement");
             }
         } else if (locatable instanceof Goods) {
             removeGoods((Goods)locatable);
         } else {
             logger.warning("Tried to remove an unrecognized 'Locatable' from a IndianSettlement.");
         }
     }
 
 
     /**
      * Returns the amount of Units at this Location.
      *
      * @return The amount of Units at this Location.
      */
     public int getUnitCount() {
         return units.size();
     }
 
     public List<Unit> getUnitList() {
         return units;
     }
 
     public Iterator<Unit> getUnitIterator() {
         return units.iterator();
     }
 
     public Unit getFirstUnit() {
         if (units.isEmpty()) {
             return null;
         } else {
             return units.get(0);
         }
     }
 
     public Unit getLastUnit() {
         if (units.isEmpty()) {
             return null;
         } else {
             return units.get(units.size() - 1);
         }
     }
 
     /**
      * Gets the <code>Unit</code> that is currently defending this <code>IndianSettlement</code>.
      * @param attacker The unit that would be attacking this <code>IndianSettlement</code>.
      * @return The <code>Unit</code> that has been chosen to defend this <code>IndianSettlement</code>.
      */
     @Override
     public Unit getDefendingUnit(Unit attacker) {
         Unit defender = null;
         float defencePower = -1.0f;
         for (Unit nextUnit : units) {
             float tmpPower = attacker.getGame().getCombatModel().getDefencePower(attacker, nextUnit);
             if (tmpPower > defencePower) {
                 defender = nextUnit;
                 defencePower = tmpPower;
             }
         }
         return defender;
     }
 
     // TODO: make this work again
     private int getPriceAddition() {
         return 3;
     }
 
     /**
      * Gets the amount of gold this <code>IndianSettlment</code>
      * is willing to pay for the given <code>Goods</code>.
      *
      * <br><br>
      *
      * It is only meaningful to call this method from the
      * server, since the settlement's {@link GoodsContainer}
      * is hidden from the clients.
      *
      * @param goods The <code>Goods</code> to price.
      * @return The price.
      */
     public int getPrice(Goods goods) {
         return getPrice(goods.getType(), goods.getAmount());
     }
 
 
     /**
      * Gets the amount of gold this <code>IndianSettlment</code>
      * is willing to pay for the given <code>Goods</code>.
      *
      * <br><br>
      *
      * It is only meaningful to call this method from the
      * server, since the settlement's {@link GoodsContainer}
      * is hidden from the clients.
      *
      * @param type The type of <code>Goods</code> to price.
      * @param amount The amount of <code>Goods</code> to price.
      * @return The price.
      */
     public int getPrice(GoodsType type, int amount) {
         int returnPrice = 0;
 
     	GoodsType armsType = getSpecification().getGoodsType("model.goods.muskets");
     	GoodsType horsesType = getSpecification().getGoodsType("model.goods.horses");
     	EquipmentType armsEqType = getSpecification().getEquipmentType("model.equipment.indian.muskets");
     	EquipmentType horsesEqType = getSpecification().getEquipmentType("model.equipment.indian.horses");
 
     	int musketsToArmIndian = armsEqType.getAmountRequiredOf(armsType);
     	int horsesToMountIndian = horsesEqType.getAmountRequiredOf(horsesType);
         int musketsCurrAvail = getGoodsCount(armsType);
         int horsesCurrAvail = getGoodsCount(horsesType);
 
         if (amount > 100) {
             throw new IllegalArgumentException();
         }
 
         if (type == armsType) {
             int need = 0;
             int supply = musketsCurrAvail;
             for (int i=0; i<ownedUnits.size(); i++) {
                 need += musketsToArmIndian;
                 if (ownedUnits.get(i).isArmed()) {
                     supply += musketsToArmIndian;
                 }
             }
 
             int sets = ((musketsCurrAvail + amount) / musketsToArmIndian)
                 - (musketsCurrAvail / musketsToArmIndian);
             int startPrice = (19+getPriceAddition()) - (supply / musketsToArmIndian);
             for (int i=0; i<sets; i++) {
                 if ((startPrice-i) < 8 && (need > supply || musketsCurrAvail < musketsToArmIndian)) {
                     startPrice = 8+i;
                 }
                 returnPrice += musketsToArmIndian * (startPrice-i);
             }
         } else if (type == horsesType) {
             int need = 0;
             int supply = horsesCurrAvail;
             for (int i=0; i<ownedUnits.size(); i++) {
                 need += horsesToMountIndian;
                 if (ownedUnits.get(i).isMounted()) {
                     supply += horsesToMountIndian;
                 }
             }
 
             int sets = (horsesCurrAvail + amount) / horsesToMountIndian
                 - (horsesCurrAvail / horsesToMountIndian);
             int startPrice = (24+getPriceAddition()) - (supply/horsesToMountIndian);
 
             for (int i=0; i<sets; i++) {
                 if ((startPrice-(i*4)) < 4 &&
                     (need > supply ||
                     		horsesCurrAvail < horsesToMountIndian * 2)) {
                     startPrice = 4+(i*4);
                 }
                 returnPrice += horsesToMountIndian * (startPrice-(i*4));
             }
         } else if (type.isFarmed()) {
             returnPrice = 0;
         } else {
             int currentGoods = getGoodsCount(type);
 
             // Increase amount if raw materials are produced:
             GoodsType rawType = type.getRawMaterial();
             if (rawType != null) {
                 int rawProduction = getMaximumProduction(rawType);
                 if (currentGoods < 100) {
                     if (rawProduction < 5) {
                         currentGoods += rawProduction * 10;
                     } else if (rawProduction < 10) {
                         currentGoods += 50 + Math.max((rawProduction-5) * 5, 0);
                     } else if (rawProduction < 20) {
                         currentGoods += 75 + Math.max((rawProduction-10) * 2, 0);
                     } else {
                         currentGoods += 100;
                     }
                 }
             }
             if (type.isTradeGoods()) {
                 currentGoods += 20;
             }
 
             int valueGoods = Math.min(currentGoods + amount, 200) - currentGoods;
             if (valueGoods < 0) {
                 valueGoods = 0;
             }
 
             returnPrice = (int) (((20.0+getPriceAddition())-(0.05*(currentGoods+valueGoods)))*(currentGoods+valueGoods)
                                  - ((20.0+getPriceAddition())-(0.05*(currentGoods)))*(currentGoods));
         }
 
         // Bonus for top 3 types of goods:
         if (type == wantedGoods[0]) {
             returnPrice = (returnPrice*12)/10;
         } else if (type == wantedGoods[1]) {
             returnPrice = (returnPrice*11)/10;
         } else if (type == wantedGoods[2]) {
             returnPrice = (returnPrice*105)/100;
         }
 
         return returnPrice;
     }
 
     /**
      * Gets the maximum possible production of the given type of goods.
      * @param goodsType The type of goods to check.
      * @return The maximum amount, of the given type of goods, that can
      *         be produced in one turn.
      */
     public int getMaximumProduction(GoodsType goodsType) {
         int amount = 0;
         for (Tile workTile: getTile().getSurroundingTiles(getRadius())) {
             if (workTile.getOwningSettlement() == null || workTile.getOwningSettlement() == this) {
                 // TODO: make unitType brave
                 amount += workTile.potential(goodsType, null);
             }
         }
 
         return amount;
     }
 
 
     /**
      * Updates the variable wantedGoods.
      *
      * <br><br>
      *
      * It is only meaningful to call this method from the
      * server, since the settlement's {@link GoodsContainer}
      * is hidden from the clients.
      */
     public void updateWantedGoods() {
         /* TODO: Try the different types goods in "random" order
          * (based on the numbers of units on this tile etc): */
         List<GoodsType> goodsTypes = new ArrayList<GoodsType>(getSpecification().getGoodsTypeList());
         Collections.sort(goodsTypes, wantedGoodsComparator);
         int wantedIndex = 0;
         for (GoodsType goodsType : goodsTypes) {
             // Indians do not ask for horses or guns
             if (goodsType.isMilitaryGoods())
                 continue;
             // no sense asking for bells or crosses
             if (!goodsType.isStorable())
                 continue;
             if (wantedIndex < wantedGoods.length) {
                 wantedGoods[wantedIndex] = goodsType;
                 wantedIndex++;
             } else {
                 break;
             }
         }
     }
 
     public boolean contains(Locatable locatable) {
         if (locatable instanceof Unit) {
             return units.contains(locatable);
         } else {
             return false;
         }
     }
 
 
     public boolean canAdd(Locatable locatable) {
         return true;
     }
 
     public int getProductionOf(GoodsType type) {
         int potential = 0;
         for (Tile workTile: getTile().getSurroundingTiles(getRadius())) {
             if ((workTile.getOwningSettlement() == null ||
                  workTile.getOwningSettlement() == this) && !workTile.isOccupied()) {
                 // TODO: make unitType brave
                 potential += workTile.potential(type, null);
             }
         }
 
         //TODO: This currently limits production _per_food_type_ to units*3.
         //With multiple food types, this may lead to varying results.
         //If hard-coded limiting makes sense at all, it should be done
         //after adding up all food types.
         if (type.isFoodType()) {
             potential = Math.min(potential, ownedUnits.size()*3);
         }
         return potential;
     }
 
     /**
      * Native settlements do not generate SoL.
      *
      * @return 0.
      */
     public int getSoL() {
         return 0;
     }
 
     public boolean checkForNewMissionaryConvert() {
 
         /* Increase convert progress and generate convert if needed. */
         if (missionary != null && getGame().getViewOwner() == null) {
             int increment = 8;
 
             // Update increment if missionary is an expert.
             if (missionary.hasAbility("model.ability.expertMissionary")) {
                 increment = 13;
             }
 
             // Increase increment if alarm level is high.
             increment += 2 * getAlarm(missionary.getOwner()).getValue() / 100;
             convertProgress += increment;
 
             if (convertProgress >= 100 && getUnitCount() > 2) {
                 convertProgress = 0;
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Dispose of this native settlement.
      *
      * @return A list of disposed objects.
      */
     @Override
     public List<FreeColGameObject> disposeList() {
         // Orphan the units whose home settlement this is.
         while (ownedUnits.size() > 0) {
             ownedUnits.remove(0).setIndianSettlement(null);
         }
 
         List<FreeColGameObject> objects = new ArrayList<FreeColGameObject>();
         while (units.size() > 0) {
             objects.addAll(units.remove(0).disposeList());
         }
         objects.addAll(super.disposeList());
         return objects;
     }
 
     /**
      * Dispose of this native settlement.
      */
     @Override
     public void dispose() {
         disposeList();
     }
 
     /**
      * Creates the {@link GoodsContainer}.
      * <br><br>
      * DO NOT USE OTHER THAN IN {@link net.sf.freecol.server.FreeColServer#loadGame}:
      * Only for compatibility when loading savegames with pre-0.0.3 protocols.
      */
     public void createGoodsContainer() {
         goodsContainer = new GoodsContainer(getGame(), this);
     }
 
 
     /**
      * This method writes an XML-representation of this object to
      * the given stream.
      *
      * <br><br>
      *
      * Only attributes visible to the given <code>Player</code> will
      * be added to that representation if <code>showAll</code> is
      * set to <code>false</code>.
      *
      * @param out The target stream.
      * @param player The <code>Player</code> this XML-representation
      *      should be made for, or <code>null</code> if
      *      <code>showAll == true</code>.
      * @param showAll Only attributes visible to <code>player</code>
      *      will be added to the representation if <code>showAll</code>
      *      is set to <i>false</i>.
      * @param toSavedGame If <code>true</code> then information that
      *      is only needed when saving a game is added.
      * @throws XMLStreamException if there are any problems writing
      *      to the stream.
      */
     @Override
     protected void toXMLImpl(XMLStreamWriter out, Player player, boolean showAll, boolean toSavedGame)
         throws XMLStreamException {
         boolean full = getGame().isClientTrusted() || showAll || player == getOwner();
         PlayerExploredTile pet = (player == null) ? null
             : getTile().getPlayerExploredTile(player);
 
         if (toSavedGame && !showAll) {
             logger.warning("toSavedGame is true, but showAll is false");
         }
 
         // Start element:
         out.writeStartElement(getXMLElementTagName());
 
         out.writeAttribute(ID_ATTRIBUTE, getId());
         out.writeAttribute("tile", tile.getId());
         out.writeAttribute("name", getName());
         out.writeAttribute("owner", owner.getId());
         out.writeAttribute("settlementType", getType().getId());
 
         if (full) {
             out.writeAttribute("lastTribute", Integer.toString(lastTribute));
             out.writeAttribute("convertProgress", Integer.toString(convertProgress));
             writeAttribute(out, "learnableSkill", learnableSkill);
             for (int i = 0; i < wantedGoods.length; i++) {
                 String tag = "wantedGoods" + Integer.toString(i);
                 out.writeAttribute(tag, wantedGoods[i].getId());
             }
         } else if (pet != null) {
             writeAttribute(out, "learnableSkill", pet.getSkill());
             GoodsType[] wanted = pet.getWantedGoods();
             int i, j = 0;
             for (i = 0; i < wanted.length; i++) {
                 if (wanted[i] != null) {
                     String tag = "wantedGoods" + Integer.toString(j);
                     out.writeAttribute(tag, wanted[i].getId());
                     j++;
                 }
             }
         }
 
         // attributes end here
 
         goodsContainer.toXML(out, player, showAll, toSavedGame);
 
         if (full) {
             Iterator<Player> playerIterator = visitedBy.iterator();
             while (playerIterator.hasNext()) {
                 out.writeStartElement(IS_VISITED_TAG_NAME);
                 out.writeAttribute("player", playerIterator.next().getId());
                 out.writeEndElement();
             }
             for (Entry<Player, Tension> entry : alarm.entrySet()) {
                 out.writeStartElement(ALARM_TAG_NAME);
                 out.writeAttribute("player", entry.getKey().getId());
                 out.writeAttribute(VALUE_TAG, String.valueOf(entry.getValue().getValue()));
                 out.writeEndElement();
             }
             if (missionary != null) {
                 out.writeStartElement(MISSIONARY_TAG_NAME);
                 missionary.toXML(out, player, showAll, toSavedGame);
                 out.writeEndElement();
             }
             if (!units.isEmpty()) {
                 out.writeStartElement(UNITS_TAG_NAME);
                 for (Unit unit : units) {
                     unit.toXML(out, player, showAll, toSavedGame);
                 }
                 out.writeEndElement();
             }
             for (Unit unit : ownedUnits) {
                 out.writeStartElement(OWNED_UNITS_TAG_NAME);
                 out.writeAttribute(ID_ATTRIBUTE, unit.getId());
                 out.writeEndElement();
             }
         } else if (pet != null) {
             if (hasBeenVisited(player)) {
                 out.writeStartElement(IS_VISITED_TAG_NAME);
                 out.writeAttribute("player", player.getId());
                 out.writeEndElement();
             }
             if (getAlarm(player) != null) {
                 out.writeStartElement(ALARM_TAG_NAME);
                 out.writeAttribute("player", player.getId());
                 out.writeAttribute(VALUE_TAG, String.valueOf(getAlarm(player).getValue()));
                 out.writeEndElement();
             }
             if (pet.getMissionary() != null) {
                 out.writeStartElement(MISSIONARY_TAG_NAME);
                 pet.getMissionary().toXML(out, player, showAll, toSavedGame);
                 out.writeEndElement();
             }
         }
 
         out.writeEndElement();
     }
 
     /**
      * Initialize this object from an XML-representation of this object.
      * @param in The input stream with the XML.
      * @throws XMLStreamException if a problem was encountered
      *      during parsing.
      */
     @Override
     protected void readFromXMLImpl(XMLStreamReader in) throws XMLStreamException {
         super.readAttributes(in);
         owner.addSettlement(this);
        setFeatureContainer(new FeatureContainer(getSpecification()));
         ownedUnits.clear();
 
         for (int i = 0; i < wantedGoods.length; i++) {
             String tag = WANTED_GOODS_TAG_NAME + Integer.toString(i);
             String wantedGoodsId = getAttribute(in, tag, null);
             if (wantedGoodsId != null) {
                 wantedGoods[i] = getSpecification().getGoodsType(wantedGoodsId);
             }
         }
 
         convertProgress = getAttribute(in, "convertProgress", 0);
         lastTribute = getAttribute(in, "lastTribute", 0);
         learnableSkill = getSpecification().getType(in, "learnableSkill", UnitType.class, null);
 
         visitedBy.clear();
         alarm = new HashMap<Player, Tension>();
         missionary = null;
         units.clear();
         ownedUnits.clear();
         while (in.nextTag() != XMLStreamConstants.END_ELEMENT) {
             if (IS_VISITED_TAG_NAME.equals(in.getLocalName())) {
                 Player player = (Player)getGame().getFreeColGameObject(in.getAttributeValue(null, "player"));
                 visitedBy.add(player);
                 in.nextTag(); // close tag is always generated.
             } else if (ALARM_TAG_NAME.equals(in.getLocalName())) {
                 Player player = (Player) getGame().getFreeColGameObject(in.getAttributeValue(null, "player"));
                 alarm.put(player, new Tension(getAttribute(in, VALUE_TAG, 0)));
                 in.nextTag(); // close element
             } else if (WANTED_GOODS_TAG_NAME.equals(in.getLocalName())) {
                 String[] wantedGoodsID = readFromArrayElement(WANTED_GOODS_TAG_NAME, in, new String[0]);
                 for (int i = 0; i < wantedGoodsID.length; i++) {
                     if (i == 3)
                         break;
                     wantedGoods[i] = getSpecification().getGoodsType(wantedGoodsID[i]);
                 }
             } else if (MISSIONARY_TAG_NAME.equals(in.getLocalName())) {
                 in.nextTag();
                 missionary = updateFreeColGameObject(in, Unit.class);
                 in.nextTag();
             } else if (UNITS_TAG_NAME.equals(in.getLocalName())) {
                 units = new ArrayList<Unit>();
                 while (in.nextTag() != XMLStreamConstants.END_ELEMENT) {
                     if (in.getLocalName().equals(Unit.getXMLElementTagName())) {
                         Unit unit = updateFreeColGameObject(in, Unit.class);
                         if (unit.getLocation() != this) {
                             logger.warning("fixing unit location");
                             unit.setLocation(this);
                         }
                         units.add(unit);
                     }
                 }
             } else if (OWNED_UNITS_TAG_NAME.equals(in.getLocalName())) {
                 Unit unit = getFreeColGameObject(in, ID_ATTRIBUTE, Unit.class);
                 if (unit.getOwner() != null && unit.getOwner() != owner) {
                     logger.warning("Error in savegame: unit " + unit.getId()
                                    + " does not belong to settlement " + getId());
                 } else {
                     ownedUnits.add(unit);
                     owner.setUnit(unit);
                 }
                 in.nextTag();
             } else if (in.getLocalName().equals(GoodsContainer.getXMLElementTagName())) {
                 goodsContainer = (GoodsContainer) getGame().getFreeColGameObject(in.getAttributeValue(null, ID_ATTRIBUTE));
                 if (goodsContainer != null) {
                     goodsContainer.readFromXML(in);
                 } else {
                     goodsContainer = new GoodsContainer(getGame(), this, in);
                 }
             }
         }
     }
 
 
     /**
      * An Indian settlement is no colony.
      *
      * @return null
      */
     public Colony getColony() {
         return null;
     }
 
     /**
      * Returns an array with goods to sell
      */
     public List<Goods> getSellGoods() {
         List<Goods> settlementGoods = getCompactGoods();
         for(Goods goods : settlementGoods) {
             if (goods.getAmount() > 100) {
                 goods.setAmount(100);
             }
         }
         Collections.sort(settlementGoods, exportGoodsComparator);
 
         List<Goods> result = new ArrayList<Goods>();
         int count = 0;
         for (Goods goods : settlementGoods) {
             if (goods.getType().isNewWorldGoodsType() && goods.getAmount() > 0) {
                 result.add(goods);
                 count++;
                 if (count > 2) {
                     return result;
                 }
             }
         }
 
         return result;
     }
 
     /**
      * Get the amount of gold plundered when this settlement is captured.
      */
     public int getPlunder() {
         return owner.getGold() / 10;
     }
 
     /**
      * Gets the amount of gold this <code>IndianSettlment</code>
      * is willing to pay for the given <code>Goods</code>.
      *
      * <br><br>
      *
      * It is only meaningful to call this method from the
      * server, since the settlement's {@link GoodsContainer}
      * is hidden from the clients.
      *
      * @param goods The <code>Goods</code> to price.
      * @return The price.
      */
     public int getPriceToSell(Goods goods) {
         return getPriceToSell(goods.getType(), goods.getAmount());
     }
 
     /**
      * Gets the amount of gold this <code>IndianSettlment</code>
      * is willing to pay for the given <code>Goods</code>.
      *
      * <br><br>
      *
      * It is only meaningful to call this method from the
      * server, since the settlement's {@link GoodsContainer}
      * is hidden from the clients.
      *
      * @param type The type of <code>Goods</code> to price.
      * @param amount The amount of <code>Goods</code> to price.
      * @return The price.
      */
     public int getPriceToSell(GoodsType type, int amount) {
         if (amount > 100) {
             throw new IllegalArgumentException();
         }
 
         int price = 10 - getProductionOf(type);
         if (price < 1) price = 1;
         return amount * price;
     }
 
     public String toString() {
         StringBuilder s = new StringBuilder(getName());
         s.append(" at (").append(tile.getX()).append(",").append(tile.getY()).append(")");
         return s.toString();
     }
 
     /**
      * Allows spread of horses and arms between settlements
      * @param settlement
      */
     public void tradeGoodsWithSetlement(IndianSettlement settlement) {
         GoodsType armsType = getSpecification().getGoodsType("model.goods.muskets");
         GoodsType horsesType = getSpecification().getGoodsType("model.goods.horses");
 
         List<GoodsType> goodsToTrade = new ArrayList<GoodsType>();
         goodsToTrade.add(armsType);
         goodsToTrade.add(horsesType);
 
         for(GoodsType goods : goodsToTrade){
             int goodsInStock = getGoodsCount(goods);
             if(goodsInStock <= 50){
                 continue;
             }
             int goodsTraded = goodsInStock / 2;
             settlement.addGoods(goods, goodsTraded);
             removeGoods(goods, goodsTraded);
         }
     }
 
     /**
      * Partial writer, so that "remove" messages can be brief.
      *
      * @param out The target stream.
      * @param fields The fields to write.
      * @throws XMLStreamException If there are problems writing the stream.
      */
     @Override
     protected void toXMLPartialImpl(XMLStreamWriter out, String[] fields)
         throws XMLStreamException {
         toXMLPartialByClass(out, getClass(), fields);
     }
 
     /**
      * Partial reader, so that "remove" messages can be brief.
      *
      * @param in The input stream with the XML.
      * @throws XMLStreamException If there are problems reading the stream.
      */
     @Override
     protected void readFromXMLPartialImpl(XMLStreamReader in)
         throws XMLStreamException {
         readFromXMLPartialByClass(in, getClass());
     }
 
     /**
      * Returns the tag name of the root element representing this object.
      * @return "indianSettlement".
      */
     public static String getXMLElementTagName() {
         return "indianSettlement";
     }
 }
