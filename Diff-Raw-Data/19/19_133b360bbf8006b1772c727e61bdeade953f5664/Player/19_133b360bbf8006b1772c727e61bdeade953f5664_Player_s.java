 package net.sf.freecol.common.model;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.MissingResourceException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 import net.sf.freecol.FreeCol;
 import net.sf.freecol.client.gui.i18n.Messages;
 import net.sf.freecol.common.model.Map.Position;
 import net.sf.freecol.common.util.EmptyIterator;
 
 import org.w3c.dom.Element;
 
 /**
  * Represents a player. The player can be either a human player or an AI-player.
  * 
  * <br>
  * <br>
  * 
  * In addition to storing the name, nation e.t.c. of the player, it also stores
  * various defaults for the player. One example of this is the
  * {@link #getEntryLocation entry location}.
  */
 public class Player extends FreeColGameObject implements Abilities, Nameable, Modifiers {
     private static final Logger logger = Logger.getLogger(Player.class.getName());
 
     public static final String COPYRIGHT = "Copyright (C) 2003-2007 The FreeCol Team";
 
     public static final String LICENSE = "http://www.gnu.org/licenses/gpl.html";
 
     public static final String REVISION = "$Revision$";
 
     /**
      * The number of players. At the moment, we have 24 players: 8
      * Europeans + 8 REF + 8 native + "unknown player".
      */
     public static final int NUMBER_OF_PLAYERS = 25;
 
     /**
      * The index of this player
      */
     private int index;
 
     /**
      * Constants for describing the stance towards a player.
      */
     public static final int WAR = -2, CEASE_FIRE = -1, PEACE = 0, ALLIANCE = 1;
 
     /** The maximum line of sight a unit can have in the game. */
     public static final int MAX_LINE_OF_SIGHT = 2;
 
     /** Constants for describing difficulty level. */
     public static final int VERY_EASY = 0, EASY = 1, MEDIUM = 2, HARD = 3, VERY_HARD = 4;
 
     /**
      * Contains booleans to see which tribes this player has met.
      */
     private boolean[] contacted = new boolean[NUMBER_OF_PLAYERS];
 
     /**
      * Only used by AI - stores the tension levels,
      * 0-1000 with 1000 maximum hostility.
      */
     // TODO: move this to AIPlayer
     private Tension[] tension = new Tension[NUMBER_OF_PLAYERS];
 
     /**
      * Stores the stance towards the other players. One of:
      * WAR, CEASE_FIRE, PEACE and ALLIANCE.
      */
     private int[] stance = new int[NUMBER_OF_PLAYERS];
 
     private static final Color noNationColor = Color.BLACK;
 
     /**
      * Nation/NationType related variables.
      */
 
     /**
      * The name of this player. This defaults to the user name in case
      * of a human player and the rulerName of the NationType in case
      * of an AI player.
      */
     private String name;
 
     /**
      * The NationType of this player.
      */
     private NationType nationType;
 
     /**
      * The nation ID of this player, e.g. "model.nation.dutch".
      */
     private String nationID;
 
     /**
      * The name this player uses for the New World.
      */
     private String newLandName = null;
 
     // Represented on the network as "color.getRGB()":
     private Color color = Color.BLACK;
 
     private boolean admin;
 
     /**
      * The current score of this player.
      */
     private int score;
 
     private int gold;
 
     /** The market for Europe. */
     private Market market;
 
     private Europe europe;
 
     private Monarch monarch;
 
     private boolean ready;
 
     /** True if this is an AI player. */
     private boolean ai;
 
     /** True if player has been attacked by privateers. */
     private boolean attackedByPrivateers = false;
 
     private int oldSoL;
 
     private int crosses;
 
     private int bells;
 
     private boolean dead = false;
 
     // any founding fathers in this Player's congress
     private List<FoundingFather> allFathers = new ArrayList<FoundingFather>();
 
     private FoundingFather currentFather;
 
     /** The current tax rate for this player. */
     private int tax = 0;
 
     private int[] arrears, sales, incomeBeforeTaxes, incomeAfterTaxes;
 
     // 0 = pre-rebels; 1 = in rebellion; 2 = independence granted
     private int rebellionState;
 
     public static final int REBELLION_PRE_WAR = 0;
 
     public static final int REBELLION_IN_WAR = 1;
 
     public static final int REBELLION_POST_WAR = 2;
 
     // new simple schema for crosses
     // TODO: make this depend on difficulty
     public static final int CROSSES_INCREMENT = 6;
 
     private int crossesRequired = 12;
 
     // No need for a persistent storage of this variable:
     private int colonyNameIndex = 0;
 
     private Location entryLocation;
 
     private Iterator<Unit> nextActiveUnitIterator = new UnitIterator(this, new ActivePredicate());
 
     private Iterator<Unit> nextGoingToUnitIterator = new UnitIterator(this, new GoingToPredicate());
 
     // Settlements this player owns
     private List<Settlement> settlements = new ArrayList<Settlement>();
 
     // Trade routes of this player
     private List<TradeRoute> tradeRoutes = new ArrayList<TradeRoute>();
 
     // Model messages for this player
     private List<ModelMessage> modelMessages = new ArrayList<ModelMessage>();
 
     // Temporary variables:
     protected boolean[][] canSeeTiles = null;
 
     /**
      * Stores the abilities of this Player.
      */
     private HashMap<String, Boolean> abilities = new HashMap<String, Boolean>();    
 
     /**
      * Stores the Modifiers of this Player.
      */
     private HashMap<String, Modifier> modifiers = new HashMap<String, Modifier>();
 
 
     /**
      * 
      * This constructor should only be used by subclasses.
      * 
      */
     protected Player() {
     }
 
     /**
      * 
      * Creates an new AI <code>Player</code> with the specified name.
      * 
      * 
      * 
      * @param game The <code>Game</code> this <code>Player</code> belongs
      *            to.
      * 
      * @param name The name that this player will use.
      * 
      * @param admin Whether or not this AI player shall be considered an Admin.
      * 
      * @param ai Whether or not this AI player shall be considered an AI player
      *            (usually true here).
      * 
      * @param nation The nation of the <code>Player</code>.
      * 
      */
     public Player(Game game, String name, boolean admin, boolean ai, Nation nation) {
         this(game, name, admin, nation);
         this.ai = ai;
     }
 
     /**
      * 
      * Creates a new <code>Player</code> with specified name.
      * 
      * 
      * 
      * @param game The <code>Game</code> this <code>Player</code> belongs
      *            to.
      * 
      * @param name The name that this player will use.
      * 
      * @param admin 'true' if this Player is an admin,
      * 
      * 'false' otherwise.
      * 
      */
     public Player(Game game, String name, boolean admin) {
         this(game, name, admin, game.getVacantNation());
     }
 
     /**
      * 
      * Creates a new (human) <code>Player</code> with specified name.
      * 
      * @param game The <code>Game</code> this <code>Player</code> belongs
      *            to.
      * 
      * @param name The name that this player will use.
      * 
      * @param admin 'true' if this Player is an admin,
      * 
      * 'false' otherwise.
      * 
      * @param nation The nation of the <code>Player</code>.
      * 
      */
     public Player(Game game, String name, boolean admin, Nation newNation) {
         super(game);
         
         if (game == null)
             this.index = -1;
         else {
             this.index = game.getNextPlayerIndex();
         }
         this.name = name;
         this.admin = admin;
         if (newNation != null && newNation.getType() != null) {
             this.nationType = newNation.getType();
             this.color = newNation.getColor();
             this.nationID = newNation.getID();
             if (nationType.isEuropean()) {
                 /*
                  * 
                  * Setting the amount of gold to
                  * "getGameOptions().getInteger(GameOptions.STARTING_MONEY)"
                  * 
                  * just before starting the game. See
                  * "net.sf.freecol.server.control.PreGameController".
                  * 
                  */
                 gold = 0;
                 europe = new Europe(game, this);
                 if (!nationType.isREF()) {
                     monarch = new Monarch(game, this, "");
                 }
                 arrears = new int[Goods.NUMBER_OF_TYPES];
                 sales = new int[Goods.NUMBER_OF_TYPES];
                 incomeBeforeTaxes = new int[Goods.NUMBER_OF_TYPES];
                 incomeAfterTaxes = new int[Goods.NUMBER_OF_TYPES];
             } else {
                 gold = 1500;
             }      
         } else {
             this.nationID = "";
             this.color = noNationColor;
         }
         for (int k = 0; k < tension.length; k++) {
             tension[k] = new Tension(0);
         }
         market = new Market(getGame(), this);
         crosses = 0;
         bells = 0;
         currentFather = null;
         rebellionState = 0;
     }
 
     /**
      * 
      * Initiates a new <code>Player</code> from an <code>Element</code>
      * and registers this <code>Player</code> at the specified game.
      * 
      * @param game The <code>Game</code> this object belongs to.
      * @param in The input stream containing the XML.
      * @throws XMLStreamException if a problem was encountered
      * during parsing.
      */
     public Player(Game game, XMLStreamReader in) throws XMLStreamException {
         super(game, in);
         readFromXML(in);
     }
 
     /**
      * Initiates a new <code>Player</code> from an <code>Element</code>
      * and registers this <code>Player</code> at the specified game.
      * 
      * @param game The <code>Game</code> this object belongs to.
      * @param e An XML-element that will be used to initialize
      * this object.
      * 
      */
     public Player(Game game, Element e) {
         super(game, e);
         readFromXMLElement(e);
     }
 
     /**
      * Initiates a new <code>Player</code> with the given ID. The
      * object should later be initialized by calling either {@link
      * #readFromXML(XMLStreamReader)} or {@link
      * #readFromXMLElement(Element)}.
      * 
      * @param game The <code>Game</code> in which this object belong.
      * @param id The unique identifier for this object.
      */
     public Player(Game game, String id) {
         super(game, id);
     }
 
     /**
      * Returns the index of this Player.
      *
      * @return an <code>int</code> value
      */
     public int getIndex() {
         return index;
     }
 
 
     /**
      * Returns this Player's Market.
      * 
      * @return This Player's Market.
      */
     public Market getMarket() {
         return market;
     }
 
     /**
      * Resets this Player's Market.
      */
     public void reinitialiseMarket() {
         market = new Market(getGame(), this);
     }
 
     /**
      * Checks if this player owns the given <code>Settlement</code>.
      * 
      * @param s The <code>Settlement</code>.
      * @return <code>true</code> if this <code>Player</code>
      * owns the given <code>Settlement</code>.
      */
     public boolean hasSettlement(Settlement s) {
         return settlements.contains(s);
     }
 
     /**
      * Adds the given <code>Settlement</code> to this
      * <code>Player</code>'s list of settlements.
      *
      * @param s
      */
     public void addSettlement(Settlement s) {
         if (!settlements.contains(s)) {
             settlements.add(s);
             if (s.getOwner() != this) {
                 s.setOwner(this);
             }
         }
     }
 
     /**
      * Removes the given <code>Settlement</code> from this
      * <code>Player</code>'s list of settlements.
      * 
      * @param s The <code>Settlement</code> to remove.
      */
     public void removeSettlement(Settlement s) {
         if (settlements.contains(s)) {
             if (s.getOwner() == this) {
                 throw new IllegalStateException(
                                                 "Cannot remove the ownership of the given settlement before it has been given to another player.");
             }
             settlements.remove(s);
         }
     }
 
     /**
      * Adds a <code>ModelMessage</code> for this player.
      * 
      * @param modelMessage The <code>ModelMessage</code>.
      */
     public void addModelMessage(ModelMessage modelMessage) {
         modelMessages.add(modelMessage);
     }
 
     /**
      * Returns all ModelMessages for this player.
      * 
      * @return all ModelMessages for this player.
      */
     public List<ModelMessage> getModelMessages() {
         return modelMessages;
     }
 
     /**
      * Returns all new ModelMessages for this player.
      * 
      * @return all new ModelMessages for this player.
      */
     public List<ModelMessage> getNewModelMessages() {
 
         ArrayList<ModelMessage> out = new ArrayList<ModelMessage>();
 
         for (int index = modelMessages.size() - 1; index >= 0; index--) {
             ModelMessage message = modelMessages.get(index);
             if (message.hasBeenDisplayed()) {
                 break;
             } else {
                 out.add(0, message);
             }
         }
 
         return out;
     }
 
     /**
      * Removes all undisplayed model messages for this player.
      */
     public void removeModelMessages() {
         Iterator<ModelMessage> messageIterator = modelMessages.iterator();
         while (messageIterator.hasNext()) {
             ModelMessage message = messageIterator.next();
             if (message.hasBeenDisplayed()) {
                 messageIterator.remove();
             }
         }
     }
 
     /**
      * Removes all the model messages for this player.
      */
     public void clearModelMessages() {
         modelMessages.clear();
     }
 
     /**
      * Checks if this player is a "royal expeditionary force.
      * 
      * @return <code>true</code> is the given nation is a royal
      * expeditionary force and <code>false</code> otherwise.
      */
     public boolean isREF() {
         return nationType != null && nationType.isREF();
     }
 
     /**
      * Returns the current score of the player.
      *
      * @return an <code>int</code> value
      */
     public int getScore() {
         return score;
     }
 
     /**
      * Gets the total percentage of rebels in all this player's colonies.
      * 
      * @return The total percentage of rebels in all this player's colonies.
      */
     public int getSoL() {
         int sum = 0;
         int number = 0;
         Iterator<Settlement> it = getSettlementIterator();
         while (it.hasNext()) {
             Colony c = (Colony) it.next();
             sum += c.getSoL();
             number++;
         }
         if (number > 0) {
             return sum / number;
         } else {
             return 0;
         }
     }
 
     /**
      * Declares independece.
      */
     public void declareIndependence() {
         if (getSoL() < 50) {
             throw new IllegalStateException("Cannot declare independence. SoL is only: " + getSoL());
         }
         if (getRebellionState() != REBELLION_PRE_WAR) {
             throw new IllegalStateException("Independence has already been declared.");
         }
         setRebellionState(REBELLION_IN_WAR);
         setStance(getREFPlayer(), WAR);
         setTax(0);
         // Dispose all units in Europe.
         Iterator<Unit> it = europe.getUnitIterator();
         String unitNames = "";
         while (it.hasNext()) {
             Unit u = it.next();
             unitNames += ", " + u.getName();
             u.dispose();
         }
         if (unitNames.length() > 0) {
             addModelMessage(this, "model.player.independence.unitsSeized", 
                             new String[][] {{"%units%", unitNames.substring(2)}},
                             ModelMessage.UNIT_LOST);
         }
         europe.dispose();
         europe = null;
     }
 
     /**
      * Gives independece to this <code>Player</code>.
      */
     public void giveIndependence() {
         if (!isEuropean()) {
             throw new IllegalStateException("The player \"" + getName() + "\" is not european");
         }
         if (getRebellionState() == Player.REBELLION_POST_WAR) {
             throw new IllegalStateException("The player \"" + getName() + "\" is already independent");
         }
         setRebellionState(Player.REBELLION_POST_WAR);
         setStance(getREFPlayer(), Player.PEACE);
         addModelMessage(this, "model.player.independence", null, ModelMessage.DEFAULT);
     }
 
     /**
      * Gets the <code>Player</code> controlling the "Royal Expeditionary
      * Force" for this player.
      * 
      * @return The player, or <code>null</code> if this player does
      * not have a royal expeditionary force.
      */
     public Player getREFPlayer() {
         return getGame().getPlayer(nationID + "REF");
     }
 
     /**
      * Gets the name this player has choosen for the new land.
      * 
      * @return The name of the new world as chosen by the
      * <code>Player</code>.  If no land name was chosen, the default
      * name is returned.
      */
     public String getNewLandName() {
         if (newLandName == null) {
             return Messages.message(nationID + ".newLandName");
         } else {
             return newLandName;
         }
     }
 
     /**
      * Returns true if the player already selected a new name for the discovered
      * land.
      * 
      * @return true if the player already set a name for the newly discovered
      *         land, otherwise false.
      */
     public boolean isNewLandNamed() {
         return newLandName != null;
     }
 
     /**
      * Returns the <code>Colony</code> with the given name.
      * 
      * @param name The name of the <code>Colony</code>.
      * @return The <code>Colony</code> or <code>null</code> if this
      * player does not have a <code>Colony</code> with the specified
      * name.
      */
     public Colony getColony(String name) {
         Iterator<Settlement> it = getSettlementIterator();
         while (it.hasNext()) {
             Colony colony = (Colony) it.next();
             if (colony.getName().equals(name)) {
                 return colony;
             }
         }
         return null;
     }
 
     /**
      * Creates a unique colony name.  This is done by fetching a new
      * default colony name from the list of default names.
      * 
      * @return A <code>String</code> containing a new unused colony
      * name from the list, if any is available, and otherwise an
      * automatically generated name.
      */
     public String getDefaultColonyName() {
         try {
             String name = "";
             do {
                 name = Messages.message(nationID + ".newColonyName." +
                                         Integer.toString(colonyNameIndex));
                 colonyNameIndex++;
             } while (getGame().getColony(name) != null);
             return name;
         } catch (MissingResourceException e) {
             String name = null;
             do {
                 name = Messages.message("Colony") + colonyNameIndex;
                 colonyNameIndex++;
             } while (getColony(name) != null);
             return name;
         }
     }
 
     /**
      * Sets the name this player uses for the new land.
      * 
      * @param newLandName This <code>Player</code>'s name for the new world.
      */
     public void setNewLandName(String newLandName) {
         this.newLandName = newLandName;
     }
 
     /**
      * Checks if this player is european. This includes the "Royal Expeditionay
      * Force".
      * 
      * @return <i>true</i> if this player is european and <i>false</i>
      *         otherwise.
      */
     public boolean isEuropean() {
         return nationType != null && nationType.isEuropean();
     }
 
     /**
      * Checks if this player is indian. This method returns
      * the opposite of {@link #isEuropean()}.
      * 
      * @return <i>true</i> if this player is indian and <i>false</i>
      * otherwise.
      */
     public boolean isIndian() {
         return !(nationType == null || isEuropean());
     }
 
     /**
      * Checks whether this player is at war with any other player.
      * 
      * @return <i>true</i> if this player is at war with any other.
      */
     public boolean isAtWar() {
         for (Player player : getGame().getPlayers()) {
             if (getStance(player) == WAR) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Returns the price of the given land.
      * 
      * @param tile The <code>Tile</code> to get the price for.
      * @return The price of the land.
      */
     public int getLandPrice(Tile tile) {
         Player nationOwner = tile.getOwner();
         if (nationOwner == null ||
             nationOwner == this ||
             nationOwner.isEuropean()) {
             return 0;
         }
         int price = 0;
         for (int i = 0; i < Goods.NUMBER_OF_TYPES; i++) {
             price += tile.potential(i);
         }
         // TODO: make this depend on difficulty
         price = price * 40 + 100;
         Modifier modifier = getModifier("model.modifier.landPaymentModifier");
         if (modifier == null) {
             return price;
         } else {
             return (int) modifier.applyTo(price);
         }
     }
 
     /**
      * Buys the given land.
      * 
      * @param tile The <code>Tile</code> to buy.
      */
     public void buyLand(Tile tile) {
         Player owner = tile.getOwner();
         if (owner == null) {
             throw new IllegalStateException("The Tile is not owned by any nation!");
         }
         if (owner == this) {
             throw new IllegalStateException("The Player already owns the Tile.");
         }
         if (owner.isEuropean()) {
             throw new IllegalStateException("The owner is an european player");
         }
         int price = getLandPrice(tile);
         modifyGold(-price);
         owner.modifyGold(price);
         tile.setOwner(this);
     }
 
     /**
      * Returns whether this player has met with the <code>Player</code> if the
      * given <code>nation</code>.
      * 
      * @param player The Player.
      * @return <code>true</code> if this <code>Player</code> has contacted
      *         the given nation.
      */
     public boolean hasContacted(Player player) {
         if (player == null) {
             return true;
         } else {
             return contacted[player.getIndex()];
         }
     }
 
     /**
      * Sets whether this player has contacted the given player.
      * 
      * @param player The <code>Player</code>.
      * @param b <code>true</code> if this <code>Player</code> has
      * contacted the given <code>Player</code>.
      */
     public void setContacted(Player player, boolean b) {
 
         if (player == null || player == this) {
             return;
         }
 
         if (b == true && b != contacted[player.getIndex()]) {
             boolean contactedIndians = false;
             boolean contactedEuro = false;
 
             for (int i = 0; i < getGame().getPlayers().size(); i++) {
                 if (contacted[i]) {
                     if (getGame().getPlayer(i).isEuropean()) {
                         contactedEuro = true;
                     } else {
                         contactedIndians = true;
                     }
                 }
             }
             // these dialogs should only appear on the first event
             if (player.isEuropean()) {
                 if (!contactedEuro) {
                     addModelMessage(this, "EventPanel.MEETING_EUROPEANS", null, ModelMessage.FOREIGN_DIPLOMACY, player);
                 }
             } else {
                 if (!contactedIndians) {
                     addModelMessage(this, "EventPanel.MEETING_NATIVES", null, ModelMessage.FOREIGN_DIPLOMACY, player);
                 }
                 // special cases for Aztec/Inca
                 if (player.getNationType() == FreeCol.getSpecification().getNationType("model.nation.aztec")) {
                     addModelMessage(this, "EventPanel.MEETING_AZTEC", null, ModelMessage.FOREIGN_DIPLOMACY, player);
                 } else if (player.getNationType() == FreeCol.getSpecification().getNationType("model.nation.inca")) {
                     addModelMessage(this, "EventPanel.MEETING_INCA", null, ModelMessage.FOREIGN_DIPLOMACY, player);
                 }
             }
         }
 
         contacted[player.getIndex()] = b;
     }
 
     /**
      * Returns whether this player has been attacked by privateers.
      * 
      * @return <code>true</code> if this <code>Player</code> has
      * been attacked by privateers.
      */
     public boolean hasBeenAttackedByPrivateers() {
         return attackedByPrivateers;
     }
 
     /** Sets the variable attackedByPrivateers to true. */
     public void setAttackedByPrivateers() {
         attackedByPrivateers = true;
     }
 
     /**
      * Gets the default <code>Location</code> where the units arriving from
      * {@link Europe} will be put.
      * 
      * @return The <code>Location</code>.
      * @see Unit#getEntryLocation
      */
     public Location getEntryLocation() {
         return entryLocation;
     }
 
     /**
      * Sets the <code>Location</code> where the units arriving from
      * {@link Europe} will be put as a default.
      * 
      * @param entryLocation The <code>Location</code>.
      * @see #getEntryLocation
      */
     public void setEntryLocation(Location entryLocation) {
         this.entryLocation = entryLocation;
     }
 
     /**
      * Checks if this <code>Player</code> has explored the given
      * <code>Tile</code>.
      * 
      * @param tile The <code>Tile</code>.
      * @return <i>true</i> if the <code>Tile</code> has been explored and
      *         <i>false</i> otherwise.
      */
     public boolean hasExplored(Tile tile) {
         return tile.isExplored();
     }
 
     /**
      * Sets the given tile to be explored by this player and updates the
      * player's information about the tile.
      * 
      * @param tile The <code>Tile</code> to set explored.
      * @see Tile#updatePlayerExploredTile(Player)
      */
     public void setExplored(Tile tile) {
         logger.warning("Implemented by ServerPlayer");
         // Implemented by ServerPlayer.
     }
 
     /**
      * Sets the tiles within the given <code>Unit</code>'s line of sight to
      * be explored by this player.
      * 
      * @param unit The <code>Unit</code>.
      * @see #setExplored(Tile)
      * @see #hasExplored
      */
     public void setExplored(Unit unit) {
         if (getGame() == null || getGame().getMap() == null || unit == null || unit.getLocation() == null
             || unit.getTile() == null || isIndian()) {
             return;
         }
         if (canSeeTiles == null) {
             resetCanSeeTiles();
         }
         Iterator<Position> positionIterator = getGame().getMap().getCircleIterator(unit.getTile().getPosition(), true,
                                                                                    unit.getLineOfSight());
         while (positionIterator.hasNext()) {
             Map.Position p = positionIterator.next();
             canSeeTiles[p.getX()][p.getY()] = true;
         }
     }
 
     /**
      * Forces an update of the <code>canSeeTiles</code>. This method should
      * be used to invalidate the current <code>canSeeTiles</code>. The method
      * {@link #resetCanSeeTiles} will be called whenever it is needed.
      */
     public void invalidateCanSeeTiles() {
         canSeeTiles = null;
     }
 
     /**
      * Resets this player's "can see"-tiles. This is done by setting all the
      * tiles within a {@link Unit}s line of sight visible. The other tiles are
      * made unvisible. <br>
      * <br>
      * Use {@link #invalidateCanSeeTiles} whenever possible.
      */
     public void resetCanSeeTiles() {
         Map map = getGame().getMap();
         if (map != null) {
             canSeeTiles = new boolean[map.getWidth()][map.getHeight()];
             if (!getGameOptions().getBoolean(GameOptions.FOG_OF_WAR)) {
                 Iterator<Position> positionIterator = getGame().getMap().getWholeMapIterator();
                 while (positionIterator.hasNext()) {
                     Map.Position p = positionIterator.next();
                     canSeeTiles[p.getX()][p.getY()] = hasExplored(getGame().getMap().getTile(p));
                 }
             } else {
                 Iterator<Unit> unitIterator = getUnitIterator();
                 while (unitIterator.hasNext()) {
                     Unit unit = unitIterator.next();
                     if (unit.getLocation() == null || !(unit.getLocation() instanceof Tile)) {
                         continue;
                     }
                     Map.Position position = unit.getTile().getPosition();
                     if (position == null) {
                         logger.warning("position == null");
                     }
                     canSeeTiles[position.getX()][position.getY()] = true;
                     /*
                      * if (getGame().getViewOwner() == null &&
                      * !hasExplored(map.getTile(position))) {
                      * 
                      * logger.warning("Trying to set a non-explored Tile to be
                      * visible (1). Unit: " + unit.getName() + ", Tile: " +
                      * position);
                      * 
                      * throw new IllegalStateException("Trying to set a
                      * non-explored Tile to be visible. Unit: " + unit.getName() + ",
                      * Tile: " + position); }
                      */
                     Iterator<Position> positionIterator = map.getCircleIterator(position, true, unit.getLineOfSight());
                     while (positionIterator.hasNext()) {
                         Map.Position p = positionIterator.next();
                         canSeeTiles[p.getX()][p.getY()] = true;
                         /*
                          * if (getGame().getViewOwner() == null &&
                          * !hasExplored(map.getTile(p))) {
                          * 
                          * logger.warning("Trying to set a non-explored Tile to
                          * be visible (2). Unit: " + unit.getName() + ", Tile: " +
                          * p);
                          * 
                          * throw new IllegalStateException("Trying to set a
                          * non-explored Tile to be visible. Unit: " +
                          * unit.getName() + ", Tile: " + p); }
                          */
                     }
                 }
                 Iterator<Settlement> colonyIterator = getSettlementIterator();
                 while (colonyIterator.hasNext()) {
                     Settlement colony = colonyIterator.next();
                     Map.Position position = colony.getTile().getPosition();
                     canSeeTiles[position.getX()][position.getY()] = true;
                     /*
                      * if (getGame().getViewOwner() == null &&
                      * !hasExplored(map.getTile(position))) {
                      * 
                      * logger.warning("Trying to set a non-explored Tile to be
                      * visible (3). Colony: " + colony + "(" +
                      * colony.getTile().getPosition() + "), Tile: " + position);
                      * 
                      * throw new IllegalStateException("Trying to set a
                      * non-explored Tile to be visible. Colony: " + colony + "(" +
                      * colony.getTile().getPosition() + "), Tile: " + position); }
                      */
                     Iterator<Position> positionIterator = map
                         .getCircleIterator(position, true, colony.getLineOfSight());
                     while (positionIterator.hasNext()) {
                         Map.Position p = positionIterator.next();
                         canSeeTiles[p.getX()][p.getY()] = true;
                         /*
                          * if (getGame().getViewOwner() == null &&
                          * !hasExplored(map.getTile(p))) {
                          * 
                          * logger.warning("Trying to set a non-explored Tile to
                          * be visible (4). Colony: " + colony + "(" +
                          * colony.getTile().getPosition() + "), Tile: " + p);
                          * 
                          * throw new IllegalStateException("Trying to set a
                          * non-explored Tile to be visible. Colony: " + colony +
                          * "(" + colony.getTile().getPosition() + "), Tile: " +
                          * p); }
                          */
                     }
                 }
             }
         }
     }
 
     /**
      * Checks if this <code>Player</code> can see the given <code>Tile</code>.
      * The <code>Tile</code> can be seen if it is in a {@link Unit}'s line of
      * sight.
      * 
      * @param tile The given <code>Tile</code>.
      * @return <i>true</i> if the <code>Player</code> can see the given
      *         <code>Tile</code> and <i>false</i> otherwise.
      */
     public boolean canSee(Tile tile) {
         if (tile == null) {
             return false;
         }
         if (canSeeTiles == null) {
             resetCanSeeTiles();
             if (canSeeTiles == null) {
                 return false;
             }
         }
         return canSeeTiles[tile.getX()][tile.getY()];
     }
 
     /**
      * Returns the state of this players rebellion status.
      * 
      * <pre>
      *   0 = Have not declared independence
      *   1 = Declared independence, at war with king
      *   2 = Independence granted
      * </pre>
      * 
      * @return The rebellion state.
      */
     public int getRebellionState() {
         return rebellionState;
     }
 
     /**
      * Checks if this <code>Player</code> can build colonies.
      * 
      * @return <code>true</code> if this player is european, not the royal
      *         expeditionary force and not currently fighting the war of
      *         independence.
      */
     public boolean canBuildColonies() {
         // TODO: hasAbility("model.ability.buildColony");
         return isEuropean() && getRebellionState() != REBELLION_IN_WAR && !isREF();
     }
 
     /**
      * Checks if this <code>Player</code> can get founding fathers.
      * 
      * @return <code>true</code> if this player is european, not the royal
      *         expeditionary force and not currently fighting the war of
      *         independence.
      */
     public boolean canHaveFoundingFathers() {
         // TODO: hasAbility("model.ability.electFoundingFather");
         return isEuropean() && getRebellionState() != REBELLION_IN_WAR && !isREF();
     }
 
     /**
      * Sets the rebellion status.
      * 
      * @param state The state of this player's rebellion
      * @see #getRebellionState
      */
     public void setRebellionState(int state) {
         rebellionState = state;
     }
 
     /**
      * Adds a founding father to this players continental congress.
      * 
      * @param newFather a <code>FoundingFather</code> value
      * @see FoundingFather
      */
     public void addFather(FoundingFather newFather) {
         allFathers.add(newFather);
     }
 
     /**
      * Determines whether this player has a certain Founding father.
      * 
      * @param someFather a <code>FoundingFather</code> value
      * @return Whether this player has this Founding father
      * @see FoundingFather
      */
     public boolean hasFather(FoundingFather someFather) {
         return allFathers.contains(someFather);
     }
 
     /**
      * Returns the number of founding fathers in this players congress. Used to
      * calculate number of bells needed to recruit new fathers.
      * 
      * @return The number of founding fathers in this players congress
      */
     public int getFatherCount() {
         return allFathers.size();
     }
 
     /**
      * Sets this players liberty bell production to work towards recruiting
      * <code>father</code> to its congress.
      * 
      * @param someFather a <code>FoundingFather</code> value
      * @see FoundingFather
      */
     public void setCurrentFather(FoundingFather someFather) {
         currentFather = someFather;
     }
 
     /**
      * Gets the {@link FoundingFather founding father} this player is working
      * towards.
      * 
      * @return The current FoundingFather or null if there is none
      * @see #setCurrentFather
      * @see FoundingFather
      */
     public FoundingFather getCurrentFather() {
         return currentFather;
     }
 
     /**
      * Returns the bell production bonus.
      * 
      * @return The bell production bonus.
      */
     public int getBellsBonus() {
         return (int) getModifier("model.goods.Bells").getValue();
     }
 
     /**
      * Gets called when this player's turn has ended.
      */
     public void endTurn() {
         removeModelMessages();
         resetCanSeeTiles();
     }
 
     /**
      * Checks if this <code>Player</code> can move units to
      * <code>Europe</code>.
      * 
      * @return <code>true</code> if this <code>Player</code> has an instance
      *         of <code>Europe</code>.
      */
     public boolean canMoveToEurope() {
         return getEurope() != null;
     }
 
     /**
      * Returns the europe object that this player has.
      * 
      * @return The europe object that this player has or <code>null</code> if
      *         this <code>Player</code> does not have an instance
      *         <code>Europe</code>.
      */
     public Europe getEurope() {
         return europe;
     }
 
     /**
      * Describe <code>getEuropeName</code> method here.
      *
      * @return a <code>String</code> value
      */
     public String getEuropeName() {
         if (europe == null) {
             return null;
         } else {
             return Messages.message(nationID + ".europe");
         }
     }
 
     /**
      * Returns the monarch object this player has.
      * 
      * @return The monarch object this player has or <code>null</code> if this
      *         <code>Player</code> does not have an instance
      *         <code>Monarch</code>.
      */
     public Monarch getMonarch() {
         return monarch;
     }
 
     /**
      * Sets the monarch object this player has.
      * 
      * @param monarch The monarch object this player should have.
      */
     public void setMonarch(Monarch monarch) {
         this.monarch = monarch;
     }
 
     /**
      * Returns the amount of gold that this player has.
      * 
      * @return The amount of gold that this player has or <code>-1</code> if
      *         the amount of gold is unknown.
      */
     public int getGold() {
         return gold;
     }
 
     /**
      * Sets the amount of gold that this player has.
      * 
      * @param gold The new amount of gold.
      * @exception IllegalArgumentException if the new amount is negative.
      * @see #modifyGold
      */
     public void setGold(int gold) {
         if (this.gold == -1) {
             return;
         }
         this.gold = gold;
     }
 
     /**
      * Determines whether this player is an AI player.
      * 
      * @return Whether this player is an AI player.
      */
     public boolean isAI() {
         return ai;
     }
 
     /**
      * Sets whether this player is an AI player.
      * 
      * @param ai <code>true</code> if this <code>Player</code> is controlled
      *            by the computer.
      */
     public void setAI(boolean ai) {
         this.ai = ai;
     }
 
     /**
      * Modifies the amount of gold that this player has. The argument can be
      * both positive and negative.
      * 
      * @param amount The amount of gold that should be added to this player's
      *            gold amount (can be negative!).
      * @exception IllegalArgumentException if the player gets a negative amount
      *                of gold after adding <code>amount</code>.
      */
     public void modifyGold(int amount) {
         if (this.gold == -1) {
             return;
         }
         if ((gold + amount) >= 0) {
             gold += amount;
         } else {
             // This can happen if the server and the client get out of synch.
             // Perhaps it can also happen if the client tries to adjust gold
             // for another player, where the balance is unknown. Just keep
             // going and do the best thing possible, we don't want to crash
             // the game here.
             logger.warning("Cannot add " + amount + " gold for " + this + ": would be negative!");
             gold = 0;
         }
     }
 
     /**
      * Gets a new active unit.
      * 
      * @return A <code>Unit</code> that can be made active.
      */
     public Unit getNextActiveUnit() {
         return nextActiveUnitIterator.next();
     }
 
     /**
      * Gets a new going_to unit.
      * 
      * @return A <code>Unit</code> that can be made active.
      */
     public Unit getNextGoingToUnit() {
         return nextGoingToUnitIterator.next();
     }
 
     /**
      * Checks if a new active unit can be made active.
      * 
      * @return <i>true</i> if this is the case and <i>false</i> otherwise.
      */
     public boolean hasNextActiveUnit() {
         return nextActiveUnitIterator.hasNext();
     }
 
     /**
      * Checks if a new active unit can be made active.
      * 
      * @return <i>true</i> if this is the case and <i>false</i> otherwise.
      */
     public boolean hasNextGoingToUnit() {
         return nextGoingToUnitIterator.hasNext();
     }
 
     /**
      * Checks if this player is an admin.
      * 
      * @return <i>true</i> if the player is an admin and <i>false</i>
      *         otherwise.
      */
     public boolean isAdmin() {
         return admin;
     }
 
     /**
      * Checks if this player is dead. A <code>Player</code> dies when it
      * looses the game.
      * 
      * @return <code>true</code> if this <code>Player</code> is dead.
      */
     public boolean isDead() {
         return dead;
     }
 
     /**
      * Sets this player to be dead or not.
      * 
      * @param dead Should be set to <code>true</code> when this
      *            <code>Player</code> dies.
      * @see #isDead
      */
     public void setDead(boolean dead) {
         this.dead = dead;
     }
 
     /**
      * Returns the name of this player.
      * 
      * @return The name of this player.
      */
     public String getName() {
         return name;
     }
 
     public String toString() {
         return getName();
     }
 
     /**
      * Set the <code>Name</code> value.
      * 
      * @param newName The new Name value.
      */
     public void setName(String newName) {
         this.name = newName;
     }
 
     /**
      * Returns the name of this player.
      * 
      * @return The name of this player.
      */
     public String getUsername() {
         return name;
     }
 
     /**
      * Returns the nation type of this player.
      * 
      * @return The nation type of this player.
      */
     public NationType getNationType() {
         return nationType;
     }
 
     /**
      * Sets the nation type of this player.
      * 
      * @param newNationType a <code>NationType</code> value
      */
     public void setNationType(NationType newNationType) {
         nationType = newNationType;
     }
 
     /**
      * Sets the nation for this player.
      * 
      * @param n The new nation for this player.
      */
     public void setNation(Nation newNation) {
         nationID = newNation.getID();
         //color = newNation.getColor();
     }
 
     /**
      * Return the ID of this Player's nation.
      *
      * @return a <code>String</code> value
      */
     public String getNationID() {
         return nationID;
     }
 
     /**
      * Returns the nation of this player as a String.
      * 
      * @return The nation of this player as a String.
      */
     public String getNationAsString() {
         return Messages.message(nationID + ".name");
     }
 
     /**
      * Get the <code>RulerName</code> value.
      *
      * @return a <code>String</code> value
      */
     public final String getRulerName() {
         return Messages.message(nationID + ".ruler");
     }
 
     /**
      * Returns the color of this player.
      * 
      * @return The color of this player.
      */
     public Color getColor() {
         return color;
     }
 
     /**
      * Sets the color for this player.
      * 
      * @param c The new color for this player.
      */
     public void setColor(Color c) {
         color = c;
     }
 
     /**
      * Checks if this <code>Player</code> is ready to start the game.
      * 
      * @return <code>true</code> if this <code>Player</code> is ready to
      *         start the game.
      */
     public boolean isReady() {
         return ready;
     }
 
     /**
      * Sets this <code>Player</code> to be ready/not ready for starting the
      * game.
      * 
      * @param ready This indicates if the player is ready to start the game.
      */
     public void setReady(boolean ready) {
         this.ready = ready;
     }
 
     /**
      * Gets an <code>Iterator</code> containing all the units this player
      * owns.
      * 
      * @return The <code>Iterator</code>.
      * @see Unit
      */
     public Iterator<Unit> getUnitIterator() {
         ArrayList<Unit> units = new ArrayList<Unit>();
         Map map = getGame().getMap();
         Iterator<Position> tileIterator = map.getWholeMapIterator();
         while (tileIterator.hasNext()) {
             Tile t = map.getTile(tileIterator.next());
             if (t != null && t.getFirstUnit() != null && t.getFirstUnit().getOwner().equals(this)) {
                 Iterator<Unit> unitIterator = t.getUnitIterator();
                 while (unitIterator.hasNext()) {
                     Unit u = unitIterator.next();
                     Iterator<Unit> childUnitIterator = u.getUnitIterator();
                     while (childUnitIterator.hasNext()) {
                         Unit childUnit = childUnitIterator.next();
                         units.add(childUnit);
                     }
                     units.add(u);
                 }
             }
             if (t.getSettlement() != null && t.getSettlement().getOwner() != null
                 && t.getSettlement().getOwner().equals(this)) {
                 Iterator<Unit> unitIterator = t.getSettlement().getUnitIterator();
                 while (unitIterator.hasNext()) {
                     Unit u = unitIterator.next();
                     units.add(u);
                 }
             }
         }
         if (getEurope() != null) {
             Iterator<Unit> unitIterator = getEurope().getUnitIterator();
             while (unitIterator.hasNext()) {
                 Unit u = unitIterator.next();
                 Iterator<Unit> childUnitIterator = u.getUnitIterator();
                 while (childUnitIterator.hasNext()) {
                     Unit childUnit = childUnitIterator.next();
                     units.add(childUnit);
                 }
                 units.add(u);
             }
         }
         return units.iterator();
     }
 
     /**
      * Gets an <code>Iterator</code> containing all the settlements this
      * player owns.
      * 
      * @return The <code>Iterator</code>.
      * @see Colony
      */
     public Iterator<Settlement> getSettlementIterator() {
         if (isIndian()) {
             return EmptyIterator.getInstance();
         } else {
             return settlements.iterator();
         }
     }
 
     /**
      * Gets an <code>Iterator</code> containing all the colonies this player
      * owns.
      * 
      * @return The <code>Iterator</code>.
      * @see Colony
      */
     public Iterator<Colony> getColonyIterator() {
         return getColonies().iterator();
     }
 
     /**
      * Returns the settlements this player owns.
      * 
      * @return The settlements this player owns.
      */
     public List<Settlement> getSettlements() {
         return settlements;
     }
 
     public List<Colony> getColonies() {
         ArrayList<Colony> colonies = new ArrayList<Colony>();
         for (Settlement s : settlements) {
             if (s instanceof Colony) {
                 colonies.add((Colony) s);
             } else {
                 throw new RuntimeException("getColonies can only be called for players whose settlements are colonies.");
             }
         }
         return colonies;
     }
 
     /**
      * Returns the closest <code>Location</code> in which the given ship can
      * get repaired. This is the closest {@link Colony} with a drydock, or
      * {@link Europe} if this player has no colonies with a drydock.
      * 
      * @param unit The ship that needs a location to be repaired.
      * @return The closest <code>Location</code> in which the ship can be
      *         repaired.
      * @exception IllegalArgumentException if the <code>unit</code> is not a
      *                ship.
      */
     public Location getRepairLocation(Unit unit) {
         if (!unit.isNaval()) {
             throw new IllegalArgumentException();
         }
         Location closestLocation = null;
         int shortestDistance = Integer.MAX_VALUE;
         Iterator<Settlement> colonyIterator = getSettlementIterator();
         while (colonyIterator.hasNext()) {
             Colony colony = (Colony) colonyIterator.next();
             if (colony == null || colony.getTile() == unit.getTile()) {
                 // This happens when is called from damageAllShips because
                 // the colony is being captured and can't be repaired in that colony
                 continue;
             }
             int distance;
             if (colony.hasAbility("model.ability.repairShips")
                 && (distance = unit.getTile().getDistanceTo(colony.getTile())) < shortestDistance) {
                 closestLocation = colony;
                 shortestDistance = distance;
             }
         }
         if (closestLocation != null) {
             return closestLocation;
         }
         return getEurope();
     }
 
     /**
      * Gets an <code>Iterator</code> containing all the indian settlements
      * this player owns.
      * 
      * @return The <code>Iterator</code>.
      * @see IndianSettlement
      */
     public Iterator<Settlement> getIndianSettlementIterator() {
         return settlements.iterator();
     }
 
     /**
      * Increments the player's cross count, with benefits thereof.
      * 
      * @param num The number of crosses to add.
      * @see #setCrosses
      */
     public void incrementCrosses(int num) {
         if (!canRecruitUnits()) {
             return;
         }
         crosses += num;
     }
 
     /**
      * Sets the number of crosses this player possess.
      * 
      * @param crosses The number.
      * @see #incrementCrosses
      */
     public void setCrosses(int crosses) {
         if (!canRecruitUnits()) {
             return;
         }
         this.crosses = crosses;
     }
 
     /**
      * Gets the number of crosses this player possess.
      * 
      * @return The number.
      * @see #setCrosses
      */
     public int getCrosses() {
         if (!canRecruitUnits()) {
             return 0;
         }
         return crosses;
     }
 
     /**
      * Get the <code>TradeRoutes</code> value.
      * 
      * @return a <code>List<TradeRoute></code> value
      */
     public final List<TradeRoute> getTradeRoutes() {
         return tradeRoutes;
     }
 
     /**
      * 
      * Set the <code>TradeRoutes</code> value.
      * 
      * 
      * 
      * @param newTradeRoutes The new TradeRoutes value.
      * 
      */
     public final void setTradeRoutes(final List<TradeRoute> newTradeRoutes) {
         this.tradeRoutes = newTradeRoutes;
     }
 
     /**
      * Checks to see whether or not a colonist can emigrate, and does so if
      * possible.
      * 
      * @return Whether a new colonist should immigrate.
      */
     public boolean checkEmigrate() {
         if (!canRecruitUnits()) {
             return false;
         }
         return getCrossesRequired() <= crosses;
     }
 
     /**
      * Gets the number of crosses required to cause a new colonist to emigrate.
      * 
      * @return The number of crosses required to cause a new colonist to
      *         emigrate.
      */
     public int getCrossesRequired() {
         if (!canRecruitUnits()) {
             return 0;
         }
         return crossesRequired;
     }
 
     /**
      * Sets the number of crosses required to cause a new colonist to emigrate.
      * 
      * @param crossesRequired The number of crosses required to cause a new
      *            colonist to emigrate.
      */
     public void setCrossesRequired(int crossesRequired) {
         if (!canRecruitUnits()) {
             return;
         }
         this.crossesRequired = crossesRequired;
     }
 
     /**
      * Checks if this <code>Player</code> can recruit units by producing
      * crosses.
      * 
      * @return <code>true</code> if units can be recruited by this
      *         <code>Player</code>.
      */
     public boolean canRecruitUnits() {
         return isEuropean() && getRebellionState() < REBELLION_IN_WAR;
     }
 
     /**
      * Updates the amount of crosses needed to emigrate a <code>Unit</code>
      * from <code>Europe</code>.
      */
     public void updateCrossesRequired() {
         if (!canRecruitUnits()) {
             return;
         }
         Modifier modifier = nationType.getModifier("model.modifier.religiousUnrestBonus");
         if (modifier != null) {
             crossesRequired += modifier.applyTo(CROSSES_INCREMENT);
         } else {
             crossesRequired += CROSSES_INCREMENT;
         }
 
         // The book I have tells me the crosses needed is:
         // [(colonist count in colonies + total colonist count) * 2] + 8.
         // So every unit counts as 2 unless they're in a colony,
         // wherein they count as 4.
         /*
          * int count = 8; Map map = getGame().getMap(); Iterator<Position>
          * tileIterator = map.getWholeMapIterator(); while
          * (tileIterator.hasNext()) { Tile t = map.getTile(tileIterator.next());
          * if (t != null && t.getFirstUnit() != null &&
          * t.getFirstUnit().getOwner().equals(this)) { Iterator<Unit>
          * unitIterator = t.getUnitIterator(); while (unitIterator.hasNext()) {
          * Unit u = unitIterator.next(); Iterator<Unit> childUnitIterator =
          * u.getUnitIterator(); while (childUnitIterator.hasNext()) { // Unit
          * childUnit = (Unit) childUnitIterator.next();
          * childUnitIterator.next(); count += 2; } count += 2; } } if (t != null &&
          * t.getColony() != null && t.getColony().getOwner() == this) { count +=
          * t.getColony().getUnitCount() * 4; // Units in colonies // count
          * doubly. // -sjm } } Iterator<Unit> europeUnitIterator =
          * getEurope().getUnitIterator(); while (europeUnitIterator.hasNext()) {
          * europeUnitIterator.next(); count += 2; } if (nation == ENGLISH) {
          * count = (count * 2) / 3; } setCrossesRequired(count);
          */
     }
 
     /**
      * Modifies the hostiliy against the given player.
      * 
      * @param player The <code>Player</code>.
      * @param addToTension The amount to add to the current tension level.
      */
     public void modifyTension(Player player, int addToTension) {
         modifyTension(player, addToTension, null);
     }
 
     public void modifyTension(Player player, int addToTension, IndianSettlement origin) {
         if (player == this || player == null) {
             return;
         }
         tension[player.getIndex()].modify(addToTension);
         
         if (origin != null && isIndian() && origin.getOwner() == player) {
             for (Settlement settlement: settlements) {
                 if (settlement instanceof IndianSettlement && !origin.equals(settlement)) {
                     ((IndianSettlement) settlement).propagatedAlarm(player.getIndex(), addToTension);
                 }
             }
         }
     }
 
     /**
      * Sets the hostility against the given player.
      * 
      * @param player The <code>Player</code>.
      * @param newTension The <code>Tension</code>.
      */
     public void setTension(Player player, Tension newTension) {
         if (player == this || player == null) {
             return;
         }
         tension[player.getIndex()] = newTension;
     }
 
     /**
      * Gets the hostility this player has against the given player.
      * 
      * @param player The <code>Player</code>.
      * @return An object representing the tension level.
      */
     public Tension getTension(Player player) {
         if (player == null) {
             return new Tension();
         } else {
             return tension[player.getIndex()];
         }
     }
 
     private static int getNearbyColonyBonus(Player owner, Tile tile) {
         Game game = tile.getGame();
         Map map = game.getMap();
         Iterator<Position> it = map.getCircleIterator(tile.getPosition(), false, 3);
         while (it.hasNext()) {
             Tile ct = map.getTile(it.next());
             if (ct.getColony() != null && ct.getColony().getOwner() == owner) {
                 return 45;
             }
         }
         it = map.getCircleIterator(tile.getPosition(), false, 4);
         while (it.hasNext()) {
             Tile ct = map.getTile(it.next());
             if (ct.getColony() != null && ct.getColony().getOwner() == owner) {
                 return 25;
             }
         }
         it = map.getCircleIterator(tile.getPosition(), false, 5);
         while (it.hasNext()) {
             Tile ct = map.getTile(it.next());
             if (ct.getColony() != null && ct.getColony().getOwner() == owner) {
                 return 20;
             }
         }
         it = map.getCircleIterator(tile.getPosition(), false, 6);
         while (it.hasNext()) {
             Tile ct = map.getTile(it.next());
             if (ct.getColony() != null && ct.getColony().getOwner() == owner) {
                 return 30;
             }
         }
         it = map.getCircleIterator(tile.getPosition(), false, 7);
         while (it.hasNext()) {
             Tile ct = map.getTile(it.next());
             if (ct.getColony() != null && ct.getColony().getOwner() == owner) {
                 return 15;
             }
         }
         it = map.getCircleIterator(tile.getPosition(), false, 8);
         while (it.hasNext()) {
             Tile ct = map.getTile(it.next());
             if (ct.getColony() != null && ct.getColony().getOwner() == owner) {
                 return 5;
             }
         }
         return 0;
     }
 
     /**
      * Gets the value of building a <code>Colony</code> on the given tile.
      * This method adds bonuses to the colony value if the tile is close to (but
      * not overlapping with) another friendly colony. Penalties for enemy
      * units/colonies are added as well.
      * 
      * @param tile The <code>Tile</code>
      * @return The value of building a colony on the given tile.
      * @see Tile#getColonyValue()
      */
     public int getColonyValue(Tile tile) {
         int value = tile.getColonyValue();
         if (value == 0) {
             return 0;
         } else {
             Iterator<Position> it = getGame().getMap().getCircleIterator(tile.getPosition(), true, 4);
             while (it.hasNext()) {
                 Tile ct = getGame().getMap().getTile(it.next());
                 if (ct.getColony() != null && ct.getColony().getOwner() != this) {
                     if (getStance(ct.getColony().getOwner()) == WAR) {
                         value -= Math.max(0, 20 - tile.getDistanceTo(tile) * 4);
                     } else {
                         value -= Math.max(0, 8 - tile.getDistanceTo(tile) * 2);
                     }
                 }
                 Iterator<Unit> ui = ct.getUnitIterator();
                 while (ui.hasNext()) {
                     Unit u = ui.next();
                     if (u.getOwner() != this && u.isOffensiveUnit() && u.getOwner().isEuropean()) {
                         if (getStance(u.getOwner()) == WAR) {
                             value -= Math.max(0, 40 - tile.getDistanceTo(tile) * 9);
                         }
                     }
                 }
             }
             return Math.max(0, value + getNearbyColonyBonus(this, tile));
         }
     }
 
     /**
      * Returns the stance towards a given player. <BR>
      * <BR>
      * One of: WAR, CEASE_FIRE, PEACE and ALLIANCE.
      * 
      * @param player The <code>Player</code>.
      * @return The stance.
      */
     public int getStance(Player player) {
         if (player == null) {
             return 0;
         } else {
             return stance[player.getIndex()];
         }
     }
 
     /**
      * Returns a string describing the given stance.
      * 
      * @param stance The stance.
      * @return A matching string.
      */
     public static String getStanceAsString(int stance) {
         switch (stance) {
         case WAR:
             return Messages.message("model.player.war");
         case CEASE_FIRE:
             return Messages.message("model.player.ceaseFire");
         case PEACE:
             return Messages.message("model.player.peace");
         case ALLIANCE:
             return Messages.message("model.player.alliance");
         default:
             return "Unknown type of stance.";
         }
     }
 
     /**
      * Sets the stance towards a given player. <BR>
      * <BR>
      * One of: WAR, CEASE_FIRE, PEACE and ALLIANCE.
      * 
      * @param player The <code>Player</code>.
      * @param newStance The stance.
      */
     public void setStance(Player player, int newStance) {
         if (player == null) {
             return;
         }
         int oldStance = stance[player.getIndex()];
         // Ignore requests to change the stance when indian players are
         // involved:
         /*
           if (isIndian() || player.isIndian()) {
           return;
           }
         */
         if (player == this) {
             throw new IllegalStateException("Cannot set the stance towards ourselves.");
         }
         if (newStance == oldStance) {
             return;
         }
         if (newStance == CEASE_FIRE && oldStance != WAR) {
             throw new IllegalStateException("Cease fire can only be declared when at war.");
         }
         stance[player.getIndex()] = newStance;
         if (player.getStance(this) != newStance) {
             getGame().getModelController().setStance(this, player, newStance);
         }
         if (player.getStance(this) != newStance) {
             player.setStance(this, newStance);
         }
         if (oldStance == PEACE && newStance == WAR) {
             modifyTension(player, Tension.TENSION_ADD_DECLARE_WAR_FROM_PEACE);
         } else if (oldStance == CEASE_FIRE && newStance == WAR) {
             modifyTension(player, Tension.TENSION_ADD_DECLARE_WAR_FROM_CEASE_FIRE);
         }
     }
 
     /**
      * Gets the price for a recruit in europe.
      * 
      * @return The price of a single recruit in {@link Europe}.
      */
     public int getRecruitPrice() {
         // return Math.max(0, (getCrossesRequired() - crosses) * 10);
         return getEurope().getRecruitPrice();
     }
 
     /**
      * Increments the player's bell count, with benefits thereof.
      * 
      * @param num The number of bells to add.
      */
     public void incrementBells(int num) {
         if (!canHaveFoundingFathers()) {
             return;
         }
         bells += num;
     }
 
     /**
      * Gets the current amount of bells this <code>Player</code> has.
      * 
      * @return This player's number of bells earned towards the current Founding
      *         Father.
      * @see Goods#BELLS
      * @see #incrementBells
      */
     public int getBells() {
         if (!canHaveFoundingFathers()) {
             return 0;
         }
         return bells;
     }
 
     /**
      * Returns true if this Player has the ability with the given ID.
      *
      * @param id a <code>String</code> value
      * @return a <code>boolean</code> value
      */
     public boolean hasAbility(String id) {
         return abilities.containsKey(id) && abilities.get(id);
     }
 
     /**
      * Sets the ability to newValue;
      *
      * @param id a <code>String</code> value
      * @param newValue a <code>boolean</code> value
      */
     public void setAbility(String id, boolean newValue) {
         abilities.put(id, newValue);
     }
 
     /**
      * Get the <code>Modifier</code> value.
      *
      * @param id a <code>String</code> value
      * @return a <code>Modifier</code> value
      */
     public final Modifier getModifier(String id) {
         return modifiers.get(id);
     }
 
     /**
      * Set the <code>Modifier</code> value.
      *
      * @param id a <code>String</code> value
      * @param newModifier a <code>Modifier</code> value
      */
     public final void setModifier(String id, final Modifier newModifier) {
         modifiers.put(id, newModifier);
     }
 
 
     /**
      * Prepares this <code>Player</code> for a new turn.
      */
     public void newTurn() {
 
         int newSoL = 0;
 
         // reducing tension levels if nation is native
         if (isIndian()) {
             for (int i = 0; i < tension.length; i++) {
                 if (tension[i] != null && tension[i].getValue() > 0) {
                     tension[i].modify( -(4 + tension[i].getValue()/100));
                 }
             }
         }
 
         // settlements
         ArrayList<Settlement> settlements = new ArrayList<Settlement>(getSettlements());
         for (Settlement settlement : settlements) {
             logger.finest("Calling newTurn for settlement " + settlement.toString());
             settlement.newTurn();
             if (isEuropean()) {
                 Colony colony = (Colony) settlement;
                 newSoL += colony.getSoL();
             }
         }
 
         /*
          * Moved founding fathers infront of units so that naval units
          * will get their Magellan bonus the turn Magellan joins the
          * congress.
          */
         if (isEuropean()) {
             if (getBells() >= getTotalFoundingFatherCost() && currentFather != null) {
                 addFather(currentFather);
                 abilities.putAll(currentFather.getAbilities());
                 java.util.Map<String, Modifier> newModifiers = currentFather.getModifiers();
                 for (String key : newModifiers.keySet()) {
                     if (getModifier(key) == null) {
                         setModifier(key, newModifiers.get(key));
                     } else {
                         getModifier(key).combine(newModifiers.get(key));
                     }
                 }
 
                 List<AbstractUnit> units = currentFather.getUnits();
                 if (units != null) {
                     // TODO: make use of armed, mounted, etc.
                     for (int index = 0; index < units.size(); index++) {
                         AbstractUnit unit = units.get(index);
                         String uniqueID = getID() + "newTurn" + currentFather.getID() + String.valueOf(index);
                         getGame().getModelController().createUnit(uniqueID, getEurope(), this, unit.getUnitType());
                     }
                 }
 
                 java.util.Map<UnitType, UnitType> upgrades = currentFather.getUpgrades();
                 if (upgrades != null) {
                     Iterator<Unit> unitIterator = getUnitIterator();
                     while (unitIterator.hasNext()) {
                         Unit unit = unitIterator.next();
                         if (upgrades.get(unit.getType()) != null) {
                             unit.setType(upgrades.get(unit.getType()));
                         }
                     }
                 }
 
                 for (String event : currentFather.getEvents().keySet()) {
                     if (event.equals("model.event.resetNativeAlarm")) {
                         // reduce indian tension and alarm
                         Iterator<Player> pi = getGame().getPlayerIterator();
                         while (pi.hasNext()) {
                             Player p = pi.next();
                             if (!p.isEuropean()) {
                                 p.getTension(this).setValue(0);
                                 Iterator<Settlement> isi = p.getIndianSettlementIterator();
                                 while (isi.hasNext()) {
                                     IndianSettlement is = (IndianSettlement) isi.next();
                                     is.getAlarm(this).setValue(0);
                                 }
                             }
                         }
                     } else if (event.equals("model.event.boycottsLifted")) {
                         for (int index = 0; index < Goods.NUMBER_OF_TYPES; index++) {
                             resetArrears(index);
                         }
                     } else if (event.equals("model.event.freeBuilding")) {
                         BuildingType type = FreeCol.getSpecification().getBuildingType(currentFather.getEvents()
                                                                                        .get(event));
                         for (Colony colony : getColonies()) {
                             Building building = colony.getBuilding(type);
                             if (building == null) {
                                 colony.addWorkLocation(new Building(getGame(), colony, type));
                             }
                         }
                     } else if (event.equals("model.event.seeAllColonies")) {
                         exploreAllColonies();
                     } else if (event.equals("model.event.increaseSonsOfLiberty")) {
                         int value = Integer.parseInt(currentFather.getEvents().get(event));
                         for (Colony colony : getColonies()) {
                             colony.addSoL(value);
                         }
                     }
 
 
                 }
 
                 addModelMessage(this, "model.player.foundingFatherJoinedCongress", new String[][] {
                         { "%foundingFather%", currentFather.getName() },
                         { "%description%", currentFather.getDescription() } },
                     ModelMessage.DEFAULT);
                 currentFather = null;
                 bells = 0;
             }
             
             // CO: since the pioneer already finishes faster, changing
             // it at both locations would double the bonus.
             for (Iterator<Unit> unitIterator = getUnitIterator(); unitIterator.hasNext();) {
                 Unit unit = unitIterator.next();
                 if (logger.isLoggable(Level.FINEST)) {
                     logger.finest("Calling newTurn for unit " + unit.getName() + " " + unit.getID());
                 }
                 unit.newTurn();
             }
 
             if (getEurope() != null) {
                 logger.finest("Calling newTurn for player " + getName() + "'s Europe");
                 getEurope().newTurn();
             }
 
 
             int numberOfColonies = settlements.size(); 
             if (numberOfColonies > 0) {
                 newSoL = newSoL / numberOfColonies;
                 if (oldSoL / 10 != newSoL / 10) {
                     if (newSoL > oldSoL) {
                         addModelMessage(this, "model.player.SoLIncrease", new String[][] {
                                 { "%oldSoL%", String.valueOf(oldSoL) }, { "%newSoL%", String.valueOf(newSoL) } },
                             ModelMessage.SONS_OF_LIBERTY);
                     } else {
                         addModelMessage(this, "model.player.SoLDecrease", new String[][] {
                                 { "%oldSoL%", String.valueOf(oldSoL) }, { "%newSoL%", String.valueOf(newSoL) } },
                             ModelMessage.SONS_OF_LIBERTY);
                     }
                 }
             }
             // remember SoL for check changes at next turn
             oldSoL = newSoL;
             calculateScore();
         } else {
             for (Iterator<Unit> unitIterator = getUnitIterator(); unitIterator.hasNext();) {
                 Unit unit = unitIterator.next();
                 if (logger.isLoggable(Level.FINEST)) {
                     logger.finest("Calling newTurn for unit " + unit.getName() + " " + unit.getID());
                 }
                 unit.newTurn();
             }
         }
     }
 
     /**
      * Calculate the player's score.
      *
      */
     public void calculateScore() {
         score = 0;
         Iterator<Unit> unitIterator = getUnitIterator();
         while (unitIterator.hasNext()) {
             Unit unit = unitIterator.next();
             score += unit.getScoreValue();
         }
         score += (score * oldSoL) / 100;
         score += getGold() / 1000;
     }
 
     private void exploreAllColonies() {
         // explore all tiles surrounding colonies
         ArrayList<Tile> tiles = new ArrayList<Tile>();
         Iterator<Position> tileIterator = getGame().getMap().getWholeMapIterator();
         while (tileIterator.hasNext()) {
             Tile tile = getGame().getMap().getTile((tileIterator.next()));
             if (tile.getColony() != null) {
                 tiles.add(tile);
                 for (int i = 0; i < 8; i++) {
                     Tile addTile = getGame().getMap().getNeighbourOrNull(i, tile);
                     if (addTile != null) {
                         tiles.add(addTile);
                     }
                 }
             }
         }
         getGame().getModelController().exploreTiles(this, tiles);
     }
 
     /**
      * This method writes an XML-representation of this object to the given
      * stream. <br>
      * <br>
      * Only attributes visible to the given <code>Player</code> will be added
      * to that representation if <code>showAll</code> is set to
      * <code>false</code>.
      * 
      * @param out The target stream.
      * @param player The <code>Player</code> this XML-representation should be
      *            made for, or <code>null</code> if
      *            <code>showAll == true</code>.
      * @param showAll Only attributes visible to <code>player</code> will be
      *            added to the representation if <code>showAll</code> is set
      *            to <i>false</i>.
      * @param toSavedGame If <code>true</code> then information that is only
      *            needed when saving a game is added.
      * @throws XMLStreamException if there are any problems writing to the
      *             stream.
      */
     protected void toXMLImpl(XMLStreamWriter out, Player player, boolean showAll, boolean toSavedGame)
         throws XMLStreamException {
         // Start element:
         out.writeStartElement(getXMLElementTagName());
         out.writeAttribute("ID", getID());
         out.writeAttribute("index", String.valueOf(index));
         out.writeAttribute("username", name);
         out.writeAttribute("nationID", nationID);
         if (nationType != null) {
             out.writeAttribute("nationType", nationType.getID());
         }
         out.writeAttribute("color", Integer.toString(color.getRGB()));
         out.writeAttribute("admin", Boolean.toString(admin));
         out.writeAttribute("ready", Boolean.toString(ready));
         out.writeAttribute("dead", Boolean.toString(dead));
         out.writeAttribute("rebellionState", Integer.toString(rebellionState));
         out.writeAttribute("ai", Boolean.toString(ai));
         out.writeAttribute("tax", Integer.toString(tax));
         int[] tensionArray = new int[tension.length];
         for (int i = 0; i < tension.length; i++) {
             tensionArray[i] = tension[i].getValue();
         }
         if (getGame().isClientTrusted() || showAll || equals(player)) {
             out.writeAttribute("gold", Integer.toString(gold));
             out.writeAttribute("crosses", Integer.toString(crosses));
             out.writeAttribute("bells", Integer.toString(bells));
             if (currentFather != null) {
                 out.writeAttribute("currentFather", currentFather.getID());
             }
             out.writeAttribute("crossesRequired", Integer.toString(crossesRequired));
             out.writeAttribute("attackedByPrivateers", Boolean.toString(attackedByPrivateers));
             out.writeAttribute("oldSoL", Integer.toString(oldSoL));
             out.writeAttribute("score", Integer.toString(score));
             // TODO: review this data structure
             char[] fatherCharArray = new char[FreeCol.getSpecification().numberOfFoundingFathers()];
             for (int i = 0; i < fatherCharArray.length; i++) {
                 fatherCharArray[i] = '0';
             }
             for (FoundingFather father : allFathers) {
                 fatherCharArray[father.getIndex()] = '1';
             }
             out.writeAttribute("foundingFathers", new String(fatherCharArray));
             StringBuffer sb = new StringBuffer(contacted.length);
             for (int i = 0; i < contacted.length; i++) {
                 if (contacted[i]) {
                     sb.append('1');
                 } else {
                     sb.append('0');
                 }
             }
             out.writeAttribute("contacted", sb.toString());
            if (europe != null) {
                europe.toXML(out, player, showAll, toSavedGame);
            }
            if (monarch != null) {
                monarch.toXML(out, player, showAll, toSavedGame);
            }
         } else {
             out.writeAttribute("gold", Integer.toString(-1));
             out.writeAttribute("crosses", Integer.toString(-1));
             out.writeAttribute("bells", Integer.toString(-1));
             out.writeAttribute("currentFather", Integer.toString(-1));
             out.writeAttribute("crossesRequired", Integer.toString(-1));
         }
         if (newLandName != null) {
             out.writeAttribute("newLandName", newLandName);
         }
         if (entryLocation != null) {
             out.writeAttribute("entryLocation", entryLocation.getID());
         }
         toArrayElement("tension", tensionArray, out);
         toArrayElement("stance", stance, out);
         if (isEuropean()) {
             toArrayElement("arrears", arrears, out);
             toArrayElement("sales", sales, out);
             toArrayElement("incomeBeforeTaxes", incomeBeforeTaxes, out);
             toArrayElement("incomeAfterTaxes", incomeAfterTaxes, out);
         }
         for (TradeRoute route : getTradeRoutes()) {
             route.toXML(out, this);
         }
         if (market != null) {
             market.toXML(out, player, showAll, toSavedGame);
         }
 
         /*
         for (ModelMessage message : modelMessages) {
             message.toXML(out);
         }
         */
 
         out.writeEndElement();
     }
 
     /**
      * Initialize this object from an XML-representation of this object.
      * 
      * @param in The input stream with the XML.
      */
     protected void readFromXMLImpl(XMLStreamReader in) throws XMLStreamException {
         setID(in.getAttributeValue(null, "ID"));
         index = Integer.parseInt(in.getAttributeValue(null, "index"));
         name = in.getAttributeValue(null, "username");
         nationID = in.getAttributeValue(null, "nationID");
         nationType = FreeCol.getSpecification().getNationType(in.getAttributeValue(null, "nationType"));
         color = new Color(Integer.parseInt(in.getAttributeValue(null, "color")));
         admin = (new Boolean(in.getAttributeValue(null, "admin"))).booleanValue();
         gold = Integer.parseInt(in.getAttributeValue(null, "gold"));
         crosses = Integer.parseInt(in.getAttributeValue(null, "crosses"));
         bells = Integer.parseInt(in.getAttributeValue(null, "bells"));
         oldSoL = getAttribute(in, "oldSoL", 0);
         score = getAttribute(in, "score", 0);
         ready = (new Boolean(in.getAttributeValue(null, "ready"))).booleanValue();
         ai = (new Boolean(in.getAttributeValue(null, "ai"))).booleanValue();
         dead = (new Boolean(in.getAttributeValue(null, "dead"))).booleanValue();
         tax = Integer.parseInt(in.getAttributeValue(null, "tax"));
         rebellionState = Integer.parseInt(in.getAttributeValue(null, "rebellionState"));
         currentFather = FreeCol.getSpecification().getFoundingFather(in.getAttributeValue(null, "currentFather"));
         crossesRequired = Integer.parseInt(in.getAttributeValue(null, "crossesRequired"));
         final String contactedStr = in.getAttributeValue(null, "contacted");
         if (contactedStr != null) {
             for (int i = 0; i < contactedStr.length(); i++) {
                 if (contactedStr.charAt(i) == '1') {
                     contacted[i] = true;
                 } else {
                     contacted[i] = false;
                 }
             }
         }
         final String newLandNameStr = in.getAttributeValue(null, "newLandName");
         if (newLandNameStr != null) {
             newLandName = newLandNameStr;
         }
         // TODO: review this data structure
         final String foundingFathersStr = in.getAttributeValue(null, "foundingFathers");
         if (foundingFathersStr != null) {
             for (int i = 0; i < foundingFathersStr.length(); i++) {
                 if (foundingFathersStr.charAt(i) == '1') {
                     addFather(FreeCol.getSpecification().getFoundingFather(i));
                 }
             }
         }
         attackedByPrivateers = getAttribute(in, "attackedByPrivateers", false);
         final String entryLocationStr = in.getAttributeValue(null, "entryLocation");
         if (entryLocationStr != null) {
             entryLocation = (Location) getGame().getFreeColGameObject(entryLocationStr);
             if (entryLocation == null) {
                 entryLocation = new Tile(getGame(), entryLocationStr);
             }
         }
         tension = null;
         stance = null;
         arrears = null;
         sales = null;
         incomeBeforeTaxes = null;
         incomeAfterTaxes = null;
         while (in.nextTag() != XMLStreamConstants.END_ELEMENT) {
             if (in.getLocalName().equals("tension")) {
                 tension = new Tension[NUMBER_OF_PLAYERS];
                 int[] tensionArray = readFromArrayElement("tension", in, new int[0]);
                 for (int i = 0; i < tensionArray.length; i++) {
                     tension[i] = new Tension(tensionArray[i]);
                 }
             } else if (in.getLocalName().equals("stance")) {
                 stance = readFromArrayElement("stance", in, new int[0]);
             } else if (in.getLocalName().equals("arrears")) {
                 arrears = readFromArrayElement("arrears", in, new int[0]);
             } else if (in.getLocalName().equals("sales")) {
                 sales = readFromArrayElement("sales", in, new int[0]);
             } else if (in.getLocalName().equals("incomeBeforeTaxes")) {
                 incomeBeforeTaxes = readFromArrayElement("incomeBeforeTaxes", in, new int[0]);
             } else if (in.getLocalName().equals("incomeAfterTaxes")) {
                 incomeAfterTaxes = readFromArrayElement("incomeAfterTaxes", in, new int[0]);
             } else if (in.getLocalName().equals(Europe.getXMLElementTagName())) {
                 europe = (Europe) getGame().getFreeColGameObject(in.getAttributeValue(null, "ID"));
                 if (europe != null) {
                     europe.readFromXML(in);
                 } else {
                     europe = new Europe(getGame(), in);
                 }
             } else if (in.getLocalName().equals(Monarch.getXMLElementTagName())) {
                 monarch = (Monarch) getGame().getFreeColGameObject(in.getAttributeValue(null, "ID"));
                 if (monarch != null) {
                     monarch.readFromXML(in);
                 } else {
                     monarch = new Monarch(getGame(), in);
                 }
             } else if (in.getLocalName().equals(TradeRoute.getXMLElementTagName())) {
                 TradeRoute route = new TradeRoute(getGame(), in);
                 getTradeRoutes().add(route);
             } else if (in.getLocalName().equals(Market.getXMLElementTagName())) {
                 market = (Market) getGame().getFreeColGameObject(in.getAttributeValue(null, "ID"));
                 // Get the market
                 if (market != null) {
                     market.readFromXML(in);
                 } else {
                     market = new Market(getGame(), in);
                 }
             }
         }
         if (market == null) {
             market = new Market(getGame(), this);
         }
         if (tension == null) {
             tension = new Tension[NUMBER_OF_PLAYERS];
             for (int i = 0; i < tension.length; i++) {
                 tension[i] = new Tension(0);
             }
         }
         if (stance == null) {
             stance = new int[NUMBER_OF_PLAYERS];
         }
         if (arrears == null) {
             arrears = new int[Goods.NUMBER_OF_TYPES];
         }
         if (sales == null) {
             sales = new int[Goods.NUMBER_OF_TYPES];
         }
         if (incomeBeforeTaxes == null) {
             incomeBeforeTaxes = new int[Goods.NUMBER_OF_TYPES];
         }
         if (incomeAfterTaxes == null) {
             incomeAfterTaxes = new int[Goods.NUMBER_OF_TYPES];
         }
         invalidateCanSeeTiles();
     }
 
     /**
      * Gets the tag name of the root element representing this object.
      * 
      * @return "player"
      */
     public static String getXMLElementTagName() {
         return "player";
     }
 
     /**
      * Generates a random unit type. The unit type that is returned represents
      * the type of a unit that is recruitable in Europe.
      * 
      * @return A random unit type of a unit that is recruitable in Europe.
      */
     public UnitType generateRecruitable() {
         ArrayList<UnitType> recruitableUnits = new ArrayList<UnitType>();
         List<UnitType> unitTypes = FreeCol.getSpecification().getUnitTypeList();
         int total = 0;
         for (UnitType unitType : unitTypes) {
             if (unitType.isRecruitable()) {
                 recruitableUnits.add(unitType);
                 total += unitType.getRecruitProbability();
             }
         }
         
         int random = getGame().getModelController().getRandom(getID() + "newRecruitableUnit", total);
         UnitType recruitable = null;
         total = 0;
         for (UnitType unitType : recruitableUnits) {
             total += unitType.getRecruitProbability();
             if (random < total) {
                 recruitable = unitType;
                 break;
             }
         }
         return recruitable;
     }
 
     /**
      * Gets the number of bells needed for recruiting the next founding father.
      * 
      * @return How many more bells the <code>Player</code> needs in order to
      *         recruit the next founding father.
      * @see Goods#BELLS
      * @see #incrementBells
      */
     public int getRemainingFoundingFatherCost() {
         return getTotalFoundingFatherCost() - getBells();
     }
 
     /**
      * Returns how many bells in total are needed to earn the Founding Father we
      * are trying to recruit.
      * 
      * @return Total number of bells the <code>Player</code> needs to recruit
      *         the next founding father.
      * @see Goods#BELLS
      * @see #incrementBells
      */
     public int getTotalFoundingFatherCost() {
         return (getFatherCount() * getFatherCount() * (5 + getDifficulty()) + 50);
     }
 
     /**
      * Returns how many total bells will be produced if no colonies are lost and
      * nothing unexpected happens.
      * 
      * @return Total number of bells this <code>Player</code>'s
      *         <code>Colony</code>s will make.
      * @see Goods#BELLS
      * @see #incrementBells
      */
     public int getBellsProductionNextTurn() {
         int bellsNextTurn = 0;
         for (Iterator<Settlement> colonies = this.getSettlementIterator(); colonies.hasNext();) {
             Colony colony = (Colony) colonies.next();
             bellsNextTurn += colony.getProductionOf(Goods.BELLS);
         }
         return bellsNextTurn;
     }
 
     /**
      * Returns the arrears due for a type of goods.
      * 
      * @param goodsIndex The index of the goods.
      * @return The arrears due for this type of goods.
      */
     public int getArrears(int goodsIndex) {
         return arrears[goodsIndex];
     }
     public int getArrears(GoodsType type) {
         return arrears[type.getIndex()];
     }
 
     /**
      * Returns the arrears due for a type of goods.
      * 
      * @param goods The goods.
      * @return The arrears due for this type of goods.
      */
     public int getArrears(Goods goods) {
         return arrears[goods.getType().getIndex()];
     }
 
     /**
      * Sets the arrears for a type of goods.
      * 
      * @param goodsIndex The index of the goods.
      */
     public void setArrears(int goodsIndex) {
         arrears[goodsIndex] = (getDifficulty() + 3) * 100 * getMarket().paidForSale(goodsIndex);
     }
     public void setArrears(GoodsType type) {
         setArrears(type.getIndex());
     }
 
     /**
      * Sets the arrears for these goods.
      * 
      * @param goods The goods.
      */
     public void setArrears(Goods goods) {
         setArrears(goods.getType().getIndex());
     }
 
     /**
      * Resets the arrears for this type of goods to zero.
      * 
      * @param goodsIndex The index of the goods to reset the arrears for.
      */
     public void resetArrears(int goodsIndex) {
         arrears[goodsIndex] = 0;
     }
     public void resetArrears(GoodsType type) {
         arrears[type.getIndex()] = 0;
     }
 
     /**
      * Resets the arrears for these goods to zero. This is the same as calling:
      * <br>
      * <br>
      * <code>resetArrears(goods.getType());</code>
      * 
      * @param goods The goods to reset the arrears for.
      * @see #resetArrears(int)
      */
     public void resetArrears(Goods goods) {
         resetArrears(goods.getType().getIndex());
     }
 
     /**
      * Returns true if type of goods can be traded in Europe.
      * 
      * @param goodsIndex The index of the goods.
      * @return True if there are no arrears due for this type of goods.
      */
     public boolean canTrade(GoodsType type) {
         return canTrade(type.getIndex(), Market.EUROPE);
     }
 
     /**
      * Returns true if type of goods can be traded at specified place.
      * 
      * @param goodsIndex The index of the goods.
      * @param marketAccess Way the goods are traded (Europe OR Custom)
      * @return <code>true</code> if type of goods can be traded.
      */
     public boolean canTrade(int goodsIndex, int marketAccess) {
         return (arrears[goodsIndex] == 0 || (marketAccess == Market.CUSTOM_HOUSE && getGameOptions().getBoolean(
                                                                                                                 GameOptions.CUSTOM_IGNORE_BOYCOTT)));
     }
     public boolean canTrade(GoodsType type, int marketAccess) {
         return canTrade(type.getIndex(), marketAccess);
     }
 
     /**
      * Returns true if type of goods can be traded at specified place
      * 
      * @param goods The goods.
      * @param marketAccess Place where the goods are traded (Europe OR Custom)
      * @return True if type of goods can be traded.
      */
     public boolean canTrade(Goods goods, int marketAccess) {
         return canTrade(goods.getType().getIndex(), marketAccess);
     }
 
     /**
      * Returns true if type of goods can be traded in Europe.
      * 
      * @param goods The goods.
      * @return True if there are no arrears due for this type of goods.
      */
     public boolean canTrade(Goods goods) {
         return canTrade(goods, Market.EUROPE);
     }
 
     /**
      * Returns the current tax.
      * 
      * @return The current tax.
      */
     public int getTax() {
         return tax;
     }
 
     /**
      * Sets the current tax. If Thomas Paine has already joined the
      * Continental Congress, the bellsBonus is adjusted accordingly.
      * 
      * @param amount The new tax.
      */
     public void setTax(int amount) {
         if (amount != tax && hasAbility("model.ability.addTaxToBells")) {
             Modifier bellsBonus = getModifier("model.goods.Bells");
             if (bellsBonus == null) {
                 setModifier("model.goods.Bells",
                             new Modifier("model.goods.Bells", amount, Modifier.ADDITIVE));
             } else {
                 int difference = (amount - tax);
                 bellsBonus.setValue(bellsBonus.getValue() + difference);
                 if (bellsBonus.getModifiers() != null) {
                     for (Modifier modifier : bellsBonus.getModifiers()) {
                         FreeColGameObjectType type = FreeCol.getSpecification().getType(modifier.getSource());
                         if (type != null && type.hasAbility("model.ability.addTaxToBells")) {
                             modifier.setValue(bellsBonus.getValue() + difference);
                             break;
                         }
                     }
                 }
             }
         }
         tax = amount;
     }
 
     /**
      * Returns the current sales.
      * 
      * @param goodsIndex The index of the goods.
      * @return The current sales.
      */
     public int getSales(int goodsIndex) {
         return sales[goodsIndex];
     }
 
     /**
      * Modifies the current sales.
      * 
      * @param goodsIndex The index of the goods.
      * @param amount The new sales.
      */
     public void modifySales(int goodsIndex, int amount) {
         sales[goodsIndex] += amount;
     }
 
     /**
      * Returns the current incomeBeforeTaxes.
      * 
      * @param goodsIndex The index of the goods.
      * @return The current incomeBeforeTaxes.
      */
     public int getIncomeBeforeTaxes(int goodsIndex) {
         return incomeBeforeTaxes[goodsIndex];
     }
 
     /**
      * Modifies the current incomeBeforeTaxes.
      * 
      * @param goodsIndex The index of the goods.
      * @param amount The new incomeBeforeTaxes.
      */
     public void modifyIncomeBeforeTaxes(int goodsIndex, int amount) {
         incomeBeforeTaxes[goodsIndex] += amount;
     }
 
     /**
      * Returns the current incomeAfterTaxes.
      * 
      * @param goodsIndex The index of the goods.
      * @return The current incomeAfterTaxes.
      */
     public int getIncomeAfterTaxes(int goodsIndex) {
         return incomeAfterTaxes[goodsIndex];
     }
 
     /**
      * Modifies the current incomeAfterTaxes.
      * 
      * @param goodsIndex The index of the goods.
      * @param amount The new incomeAfterTaxes.
      */
     public void modifyIncomeAfterTaxes(int goodsIndex, int amount) {
         incomeAfterTaxes[goodsIndex] += amount;
     }
 
     /**
      * Returns the difficulty level.
      * 
      * @return The difficulty level.
      */
     public int getDifficulty() {
         return getGame().getGameOptions().getInteger(GameOptions.DIFFICULTY);
     }
 
     /**
      * Returns the most valuable goods available in one of the player's
      * colonies. The goods must not be boycotted, and the amount will not exceed
      * 100.
      * 
      * @return A goods object, or null.
      */
     public Goods getMostValuableGoods() {
         Goods goods = null;
         if (!isEuropean()) {
             return goods;
         }
         int value = 0;
         Iterator<Settlement> colonyIterator = getSettlementIterator();
         while (colonyIterator.hasNext()) {
             Colony colony = (Colony) colonyIterator.next();
             List<Goods> colonyGoods = colony.getCompactGoods();
             for (Goods currentGoods : colonyGoods) {
                 if (getArrears(currentGoods) == 0) {
                     // never discard more than 100 units
                     if (currentGoods.getAmount() > 100) {
                         currentGoods.setAmount(100);
                     }
                     int goodsValue = market.getSalePrice(currentGoods);
                     if (goodsValue > value) {
                         value = goodsValue;
                         goods = currentGoods;
                     }
                 }
             }
         }
         return goods;
     }
 
     /**
      * Checks if the given <code>Player</code> equals this object.
      * 
      * @param o The <code>Player</code> to compare against this object.
      * @return <i>true</i> if the two <code>Player</code> are equal and none
      *         of both have <code>nation == null</code> and <i>false</i>
      *         otherwise.
      */
     public boolean equals(Player o) {
         if (o != null) {
             return getID().equals(o.getID());
         } else {
             return false;
         }
     }
 
 
     /**
      * A predicate that can be applied to a unit.
      */
     public abstract class UnitPredicate {
         public abstract boolean obtains(Unit unit);
     }
 
     /**
      * A predicate for determining active units.
      */
     public class ActivePredicate extends UnitPredicate {
         /**
          * Returns true if the unit is active (and going nowhere).
          */
         public boolean obtains(Unit unit) {
             return (!unit.isDisposed() && (unit.getMovesLeft() > 0) && (unit.getState() == Unit.ACTIVE)
                     && (unit.getDestination() == null) && !(unit.getLocation() instanceof WorkLocation) && unit
                     .getTile() != null);
         }
     }
 
     /**
      * A predicate for determining units going somewhere.
      */
     public class GoingToPredicate extends UnitPredicate {
         /**
          * Returns true if the unit has order to go somewhere.
          */
         public boolean obtains(Unit unit) {
             return (!unit.isDisposed() && (unit.getMovesLeft() > 0) && (unit.getDestination() != null)
                     && !(unit.getLocation() instanceof WorkLocation) && unit.getTile() != null);
         }
     }
 
     /**
      * An <code>Iterator</code> of {@link Unit}s that can be made active.
      */
     public class UnitIterator implements Iterator<Unit> {
         private Iterator<Unit> unitIterator = null;
 
         private Player owner;
 
         private Unit nextUnit = null;
 
         private UnitPredicate predicate;
 
 
         /**
          * Creates a new <code>NextActiveUnitIterator</code>.
          * 
          * @param owner The <code>Player</code> that needs an iterator of it's
          *            units.
          * @param predicate An object for deciding wether a <code>Unit</code>
          *            should be included in the <code>Iterator</code> or not.
          */
         public UnitIterator(Player owner, UnitPredicate predicate) {
             this.owner = owner;
             this.predicate = predicate;
         }
 
         public boolean hasNext() {
             if (nextUnit != null && predicate.obtains(nextUnit)) {
                 return true;
             }
             if (unitIterator == null) {
                 unitIterator = createUnitIterator();
             }
             while (unitIterator.hasNext()) {
                 nextUnit = unitIterator.next();
                 if (predicate.obtains(nextUnit)) {
                     return true;
                 }
             }
             unitIterator = createUnitIterator();
             while (unitIterator.hasNext()) {
                 nextUnit = unitIterator.next();
                 if (predicate.obtains(nextUnit)) {
                     return true;
                 }
             }
             nextUnit = null;
             return false;
         }
 
         public Unit next() {
             if (nextUnit == null || !predicate.obtains(nextUnit)) {
                 hasNext();
             }
             Unit temp = nextUnit;
             nextUnit = null;
             return temp;
         }
 
         /**
          * Removes from the underlying collection the last element returned by
          * the iterator (optional operation).
          * 
          * @exception UnsupportedOperationException no matter what.
          */
         public void remove() {
             throw new UnsupportedOperationException();
         }
 
         /**
          * Returns an <code>Iterator</code> for the units of this player that
          * can be active.
          */
         private Iterator<Unit> createUnitIterator() {
             ArrayList<Unit> units = new ArrayList<Unit>();
             Map map = getGame().getMap();
             Iterator<Position> tileIterator = map.getWholeMapIterator();
             while (tileIterator.hasNext()) {
                 Tile t = map.getTile(tileIterator.next());
                 if (t != null && t.getFirstUnit() != null && t.getFirstUnit().getOwner().equals(owner)) {
                     Iterator<Unit> unitIterator = t.getUnitIterator();
                     while (unitIterator.hasNext()) {
                         Unit u = unitIterator.next();
                         Iterator<Unit> childUnitIterator = u.getUnitIterator();
                         while (childUnitIterator.hasNext()) {
                             Unit childUnit = childUnitIterator.next();
                             if (predicate.obtains(childUnit)) {
                                 units.add(childUnit);
                             }
                         }
                         if (predicate.obtains(u)) {
                             units.add(u);
                         }
                     }
                 }
             }
             return units.iterator();
         }
     }
 }
