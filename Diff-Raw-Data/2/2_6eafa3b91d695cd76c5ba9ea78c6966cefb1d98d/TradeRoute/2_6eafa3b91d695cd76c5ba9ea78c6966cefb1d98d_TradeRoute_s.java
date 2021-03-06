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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 import net.sf.freecol.FreeCol;
 import net.sf.freecol.common.model.Player;
 
 import org.w3c.dom.Element;
 
 /**
  * A TradeRoute holds all information for a unit to follow along a trade route.
  */
 public class TradeRoute extends FreeColGameObject implements Cloneable, Ownable {
 
     public static final TradeRoute NO_TRADE_ROUTE = new TradeRoute();
 
     // private static final Logger logger =
     // Logger.getLogger(TradeRoute.class.getName());
 
     /**
      * The name of this trade route.
      */
     private String name;
 
     /**
      * Whether the trade route has been modified. This is of interest only to
      * the client and can be ignored for XML serialization.
      */
     private boolean modified = false;
 
     /**
      * The <code>Player</code> who owns this trade route. This is necessary to
      * ensure that malicious clients can not modify the trade routes of other
      * players.
      */
     private Player owner;
 
     /**
      * A list of stops.
      */
     private ArrayList<Stop> stops = new ArrayList<Stop>();
 
     /**
      * Creates a new <code>TradeRoute</code> instance.
      *
      */
     private TradeRoute() {};
 
     /**
      * Creates a new <code>TradeRoute</code> instance.
      *
      * @param game a <code>Game</code> value
      * @param name a <code>String</code> value
      * @param player a <code>Player</code> value
      */
     public TradeRoute(Game game, String name, Player player) {
         super(game);
         this.name = name;
         this.owner = player;
     }
 
     /**
      * Creates a new <code>TradeRoute</code> instance.
      *
      * @param game a <code>Game</code> value
      * @param in a <code>XMLStreamReader</code> value
      * @exception XMLStreamException if an error occurs
      */
     public TradeRoute(Game game, XMLStreamReader in) throws XMLStreamException {
         super(game, in);
         readFromXML(in);
     }
 
     /**
      * Creates a new <code>TradeRoute</code> instance.
      *
      * @param game a <code>Game</code> value
      * @param e an <code>Element</code> value
      */
     public TradeRoute(Game game, Element e) {
         super(game, e);
         readFromXMLElement(e);
     }
 
     /**
      * Copy all fields from another trade route to this one. This is useful when
      * an updated route is received on the server side from the client.
      * 
      * @param other The route to copy from.
      */
     public synchronized void updateFrom(TradeRoute other) {
         setName(other.getName());
         stops.clear();
         for (Stop otherStop : other.getStops()) {
             addStop(new Stop(otherStop));
         }
     }
 
     /**
      * Get the <code>Modified</code> value.
      * 
      * @return a <code>boolean</code> value
      */
     public final boolean isModified() {
         return modified;
     }
 
     /**
      * Set the <code>Modified</code> value.
      * 
      * @param newModified The new Modified value.
      */
     public final void setModified(final boolean newModified) {
         this.modified = newModified;
     }
 
     /**
      * Get the <code>Name</code> value.
      * 
      * @return a <code>String</code> value
      */
     public final String getName() {
         return name;
     }
 
     /**
      * Set the <code>Name</code> value.
      * 
      * @param newName The new Name value.
      */
     public final void setName(final String newName) {
         this.name = newName;
     }
 
     /**
      * Add a new <code>Stop</code> to this trade route.
      * 
      * @param stop The <code>Stop</code> to add.
      */
     public void addStop(Stop stop) {
         stops.add(stop);
     }
 
     /**
      * Get the <code>Owner</code> value.
      * 
      * @return a <code>Player</code> value
      */
     public final Player getOwner() {
         return owner;
     }
 
     /**
      * Set the <code>Owner</code> value.
      * 
      * @param newOwner The new Owner value.
      */
     public final void setOwner(final Player newOwner) {
         this.owner = newOwner;
     }
 
     public String toString() {
         return getName();
     }
 
     /**
      * Get the <code>Stops</code> value.
      * 
      * @return an <code>ArrayList<Stop></code> value
      */
     public final ArrayList<Stop> getStops() {
         return stops;
     }
 
     /**
      * Set the <code>Stops</code> value.
      * 
      * @param newStops The new Stops value.
      */
     public final void setStops(final ArrayList<Stop> newStops) {
         this.stops = newStops;
     }
 
     /**
      * A traderoute does not do anything on a new turn.
      */
     public void newTurn() {
     }
 
     /**
      * Clone the trade route and return a deep copy.
      * <p>
      * The copied trade route has no reference back to the original and can
      * safely be used as a temporary copy. It is NOT registered with the game,
      * but will have the same unique id as the original.
      * 
      * @return deep copy of trade route.
      */
     public TradeRoute clone() {
         try {
             TradeRoute copy = (TradeRoute) super.clone();
             copy.replaceStops(getStops());
             return copy;
         } catch (CloneNotSupportedException e) {
             throw new IllegalStateException("Clone should be supported!", e);
         }
     }
 
     /**
      * Replace all the stops for this trade route with the stops passed from
      * another trade route.
      * 
      * This method will create a deep copy as it creates new stops based on the given ones.
      * 
      * @param otherStops The new stops to use.
      * @see #clone()
      */
     private void replaceStops(List<Stop> otherStops) {
         stops = new ArrayList<Stop>();
         for (Stop otherStop : otherStops) {
             addStop(new Stop(otherStop));
         }
     }
 
 
     public class Stop {
 
         private String locationId;
 
         private ArrayList<GoodsType> cargo = new ArrayList<GoodsType>();
 
         /**
          * Whether the stop has been modified. This is of interest only to the
          * client and can be ignored for XML serialization.
          */
         private boolean modified = false;
 
 
         public Stop(Location location) {
             this.locationId = location.getId();
         }
 
         /**
          * Copy constructor. Creates a stop based on the given one.
          * 
          * @param other
          */
         private Stop(Stop other) {
             this.locationId = other.locationId;
             this.cargo = new ArrayList<GoodsType>(other.cargo);
         }
 
         private Stop(XMLStreamReader in) throws XMLStreamException {
             locationId = in.getAttributeValue(null, "location");
             List<GoodsType> goodsList = FreeCol.getSpecification().getGoodsTypeList();
             for (int cargoIndex : readFromArrayElement("cargo", in, new int[0])) {
                 addCargo(goodsList.get(cargoIndex));
             }
             in.nextTag();
         }
 
         /**
          * Get the <code>Modified</code> value.
          * 
          * @return a <code>boolean</code> value
          */
         public final boolean isModified() {
             return modified;
         }
 
         /**
          * Set the <code>Modified</code> value.
          * 
          * @param newModified The new Modified value.
          */
         public final void setModified(final boolean newModified) {
             this.modified = newModified;
         }
 
         /**
          * Get the <code>Location</code> value.
          * 
          * @return a <code>Location</code> value
          */
         public final Location getLocation() {
             Game g = getGame();
             return g != null ? (Location) g.getFreeColGameObject(locationId) : null;
         }
 
         /**
          * Get the <code>Cargo</code> value.
          * 
          * @return a cloned <code>ArrayList<Integer></code> value
          */
         @SuppressWarnings("unchecked")
         public final ArrayList<GoodsType> getCargo() {
             return (ArrayList<GoodsType>) cargo.clone();
         }
 
         /**
          * Set the cargo values.
          * 
          * @param cargo and arraylist of cargo values.
          */
         public final void setCargo(ArrayList<GoodsType> cargo) {
             this.cargo.clear();
             this.cargo.addAll(cargo);
         }
 
         public void addCargo(GoodsType newCargo) {
             cargo.add(newCargo);
         }
 
         /*
         public void addCargo(int newCargoIndex) {
             cargo.add(FreeCol.getSpecification().getGoodsType(newCargoIndex));
         }
         */
 
         public String toString() {
             Location l = getLocation();
             return l != null ? l.getLocationName() : locationId;
         }
 
         public void toXMLImpl(XMLStreamWriter out) throws XMLStreamException {
             out.writeStartElement(getStopXMLElementTagName());
             out.writeAttribute("location", this.locationId);
             int[] cargoIndexArray = new int[cargo.size()];
             for (int index = 0; index < cargoIndexArray.length; index++) {
                 cargoIndexArray[index] = cargo.get(index).getIndex();
             }
             toArrayElement("cargo", cargoIndexArray, out);
             out.writeEndElement();
         }
     }
 
 
    public void toXMLImpl(XMLStreamWriter out, Player player, boolean showAll, boolean toSavedGame)
             throws XMLStreamException {
         // Start element:
         out.writeStartElement(getXMLElementTagName());
 
         out.writeAttribute("ID", getId());
         out.writeAttribute("name", getName());
         out.writeAttribute("owner", getOwner().getId());
         for (Stop stop : stops) {
             stop.toXMLImpl(out);
         }
 
         out.writeEndElement();
     }
 
     /**
      * Initialize this object from an XML-representation of this object.
      * 
      * @param in The input stream with the XML.
      */
     protected void readFromXMLImpl(XMLStreamReader in) throws XMLStreamException {
         setId(in.getAttributeValue(null, "ID"));
         setName(in.getAttributeValue(null, "name"));
         String ownerID = in.getAttributeValue(null, "owner");
         
         Game game = getGame();
         if (game != null){
         	if (ownerID.equals(Player.UNKNOWN_ENEMY)) {
         		owner = game.getUnknownEnemy(); 
         	} else {
 	            owner = (Player) getGame().getFreeColGameObject(ownerID);
 	            if (owner == null) {
 	                owner = new Player(getGame(), in.getAttributeValue(null, "owner"));
 	            }
 	        }
         }
         
         while (in.nextTag() != XMLStreamConstants.END_ELEMENT) {
             if (getStopXMLElementTagName().equals(in.getLocalName())) {
                 stops.add(new Stop(in));
             }
         }
     }
 
     /**
      * Returns the tag name of the root element representing this object.
      * 
      * @return "tradeRoute".
      */
     public static String getXMLElementTagName() {
         return "tradeRoute";
     }
 
     /**
      * Returns the tag name of the root element representing this object.
      * 
      * @return "tradeRouteStop".
      */
     public static String getStopXMLElementTagName() {
         return "tradeRouteStop";
     }
 }
