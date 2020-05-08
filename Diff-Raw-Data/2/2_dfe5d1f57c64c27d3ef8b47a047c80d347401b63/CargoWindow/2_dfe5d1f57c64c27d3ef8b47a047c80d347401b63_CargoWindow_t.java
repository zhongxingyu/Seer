 /*
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * Allows the cargo bay of a ship to be viewed.
  * Nathan Wiehoff
  */
 package gdi;
 
 import cargo.Equipment;
 import cargo.Hardpoint;
 import cargo.Item;
 import cargo.Weapon;
 import celestial.Ship.Ship;
 import celestial.Ship.Station;
 import gdi.component.AstralList;
 import gdi.component.AstralWindow;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import lib.Parser;
 import lib.Parser.Term;
 
 public class CargoWindow extends AstralWindow {
 
     public static final String CMD_TRASH = "Trash";
     public static final String CMD_EJECT = "Eject";
     public static final String CMD_UNMOUNT = "Unmount";
     public static final String CMD_MOUNT = "Mount";
     public static final String CMD_STACK = "Stack";
     public static final String CMD_SPLIT = "Split";
     public static final String CMD_SPLITALL = "Split All";
     public static final String CMD_ASSEMBLE = "Assemble";
     public static final String CMD_PACKAGE = "Package";
     public static final String CMD_DEPLOY = "Deploy";
     AstralList cargoList = new AstralList(this);
     AstralList propertyList = new AstralList(this);
     AstralList optionList = new AstralList(this);
     protected Ship ship;
 
     public CargoWindow() {
         super();
         generate();
     }
 
     private void generate() {
         backColor = windowGrey;
         //size this window
         width = 500;
         height = 400;
         setVisible(true);
         //setup the cargo list
         cargoList.setX(0);
         cargoList.setY(0);
         cargoList.setWidth(width);
         cargoList.setHeight((height / 2) - 1);
         cargoList.setVisible(true);
         //setup the property list
         propertyList.setX(0);
         propertyList.setY(height / 2);
         propertyList.setWidth((int) (width / 1.5));
         propertyList.setHeight((height / 2) - 1);
         propertyList.setVisible(true);
         //setup the fitting label
         optionList.setX((int) (width / 1.5) + 1);
         optionList.setY(height / 2);
         optionList.setWidth((int) (width / 3));
         optionList.setHeight((height / 2) - 1);
         optionList.setVisible(true);
         //pack
         addComponent(cargoList);
         addComponent(propertyList);
         addComponent(optionList);
     }
 
     public void update(Ship ship) {
         setShip(ship);
         cargoList.clearList();
         propertyList.clearList();
         optionList.clearList();
         ArrayList<Item> logicalCargoList = new ArrayList<>();
         if (ship != null) {
             //add equipment
             for (int a = 0; a < ship.getHardpoints().size(); a++) {
                 logicalCargoList.add(ship.getHardpoints().get(a).getMounted());
             }
             //add cargo goods
             ArrayList<Item> cargo = ship.getCargoBay();
             for (int a = 0; a < cargo.size(); a++) {
                 logicalCargoList.add(cargo.get(a));
             }
             //add to display
             for (int a = 0; a < logicalCargoList.size(); a++) {
                 cargoList.addToList(logicalCargoList.get(a));
             }
             //display detailed information about the selected item
             int index = cargoList.getIndex();
             if (index < logicalCargoList.size()) {
                 Item selected = (Item) cargoList.getItemAtIndex(index);
                 //fill
                 propertyList.addToList("--GLOBAL--");
                 propertyList.addToList(" ");
                 propertyList.addToList("Credits:      " + ship.getCash());
                 propertyList.addToList("Bay Volume:   " + ship.getCargo());
                 propertyList.addToList("Volume Used:  " + ship.getBayUsed());
                 propertyList.addToList("Percent Used: " + ship.getBayUsed() / ship.getCargo() * 100.0 + "%");
                 propertyList.addToList(" ");
                 propertyList.addToList("--BASIC--");
                 propertyList.addToList(" ");
                 propertyList.addToList("Name:         " + selected.getName());
                 propertyList.addToList("Type:         " + selected.getType());
                 propertyList.addToList("Mass:         " + selected.getMass());
                 propertyList.addToList("Volume:       " + selected.getVolume());
                 propertyList.addToList(" ");
                 propertyList.addToList("--MARKET--");
                 propertyList.addToList(" ");
                 propertyList.addToList("Min Price:    " + selected.getMinPrice());
                 propertyList.addToList("Max Price:    " + selected.getMaxPrice());
                 propertyList.addToList(" ");
                 propertyList.addToList("--DETAIL--");
                 fillDescriptionLines(selected);
                 fillCommandLines(selected);
             }
         }
     }
 
     public Ship getShip() {
         return ship;
     }
 
     public void setShip(Ship ship) {
         this.ship = ship;
     }
 
     private void fillDescriptionLines(Item selected) {
         /*
          * Fills in the item's description being aware of things like line breaking on spaces.
          */
         String description = selected.getDescription();
         int lineWidth = (((propertyList.getWidth() - 10) / (propertyList.getFont().getSize())));
         int cursor = 0;
         String tmp = "";
         String[] words = description.split(" ");
         for (int a = 0; a < words.length; a++) {
             if (a < 0) {
                 a = 0;
             }
             int len = words[a].length();
             if (cursor < lineWidth && !words[a].matches("/br/")) {
                 if (cursor + len <= lineWidth) {
                     tmp += " " + words[a];
                     cursor += len;
                 } else {
                     if (lineWidth > len) {
                         propertyList.addToList(tmp);
                         tmp = "";
                         cursor = 0;
                         a--;
                     } else {
                         tmp += "[LEN!]";
                     }
                 }
             } else {
                 propertyList.addToList(tmp);
                 tmp = "";
                 cursor = 0;
                 if (!words[a].matches("/br/")) {
                     a--;
                 }
             }
         }
         propertyList.addToList(tmp.toString());
     }
 
     private void fillCommandLines(Item selected) {
         if (selected instanceof Equipment) {
             optionList.addToList("--Fitting--");
             Equipment tmp = (Equipment) selected;
             Hardpoint socket = tmp.getSocket();
             if (socket != null) {
                 //it is mounted
                 optionList.addToList(CMD_UNMOUNT);
             } else {
                 //it is not mounted
                 optionList.addToList(CMD_MOUNT);
                 optionList.addToList(CMD_PACKAGE);
             }
             optionList.addToList(" ");
         } else {
             /*
              * Options for stations
              */
             //determine if this is a station
             if (selected.getGroup().matches("constructionkit")) {
                 if (!ship.isDocked()) {
                     optionList.addToList("--Setup--");
                     optionList.addToList(CMD_DEPLOY);
                     optionList.addToList(" ");
                 }
             }
             /*
              * Options for cannons
              */
             if (selected.getType().matches(Item.TYPE_CANNON)) {
                 optionList.addToList("--Setup--");
                 optionList.addToList(CMD_ASSEMBLE);
                 optionList.addToList(" ");
             }
             /*
              * Options for missiles
              */
             if (selected.getType().matches(Item.TYPE_MISSILE)) {
                 optionList.addToList("--Setup--");
                 optionList.addToList(CMD_ASSEMBLE);
                 optionList.addToList(" ");
             }
         }
         //for packaging and repackaging
         optionList.addToList("--Packaging--");
         optionList.addToList(CMD_STACK);
         optionList.addToList(CMD_SPLIT);
         optionList.addToList(CMD_SPLITALL);
         //doing these last for safety.
         optionList.addToList(" ");
         optionList.addToList("--Dangerous--");
         optionList.addToList(CMD_TRASH);
         if (!ship.isDocked()) {
             optionList.addToList(CMD_EJECT);
         }
     }
 
     @Override
     public void handleMouseClickedEvent(MouseEvent me) {
         super.handleMouseClickedEvent(me);
         //get the module and toggle its enabled status
         if (optionList.isFocused()) {
             String command = (String) optionList.getItemAtIndex(optionList.getIndex());
             parseCommand(command);
         }
     }
 
     private void parseCommand(String command) {
         if (command != null) {
             if (command.matches(CMD_TRASH)) {
                 /*
                  * This command simply destroys an item.
                  */
                 Item selected = (Item) cargoList.getItemAtIndex(cargoList.getIndex());
                 ship.removeFromCargoBay(selected);
             } else if (command.matches(CMD_EJECT)) {
                 Item selected = (Item) cargoList.getItemAtIndex(cargoList.getIndex());
                 ship.ejectCargo(selected);
             } else if (command.matches(CMD_UNMOUNT)) {
                 Item selected = (Item) cargoList.getItemAtIndex(cargoList.getIndex());
                 Equipment tmp = (Equipment) selected;
                 ship.unfit(tmp);
             } else if (command.matches(CMD_MOUNT)) {
                 Item selected = (Item) cargoList.getItemAtIndex(cargoList.getIndex());
                 Equipment tmp = (Equipment) selected;
                 ship.fit(tmp);
             } else if (command.matches(CMD_STACK)) {
                 Item selected = (Item) cargoList.getItemAtIndex(cargoList.getIndex());
                 ArrayList<Item> cargoBay = ship.getCargoBay();
                 if (cargoBay.contains(selected)) {
                     stackItem(cargoBay, selected);
                 }
             } else if (command.matches(CMD_SPLIT)) {
                 Item selected = (Item) cargoList.getItemAtIndex(cargoList.getIndex());
                 ArrayList<Item> cargoBay = ship.getCargoBay();
                 if (cargoBay.contains(selected)) {
                     if (selected.getQuantity() > 1) {
                         Item tmp = new Item(selected.getName());
                         cargoBay.add(tmp);
                         selected.setQuantity(selected.getQuantity() - 1);
                     }
                 }
             } else if (command.matches(CMD_SPLITALL)) {
                 ArrayList<Item> cargoBay = ship.getCargoBay();
                 Item selected = (Item) cargoList.getItemAtIndex(cargoList.getIndex());
                 if (ship.hasInCargo(selected)) {
                     if (selected.getQuantity() > 1) {
                         for (int a = 0; a < cargoBay.size(); a++) {
                             Item tmp = cargoBay.get(a);
                             if (tmp.getName().matches(selected.getName())) {
                                 if (tmp.getType().matches(selected.getType())) {
                                     if (tmp.getGroup().matches(selected.getGroup())) {
                                         cargoBay.remove(tmp);
                                     }
                                 }
                             }
                         }
                         for (int a = 0; a < selected.getQuantity(); a++) {
                             Item tmp = new Item(selected.getName());
                             ship.addToCargoBay(tmp);
                         }
                     }
                 }
             } else if (command.matches(CMD_ASSEMBLE)) {
                 Item selected = (Item) cargoList.getItemAtIndex(cargoList.getIndex());
                 if (selected.getQuantity() == 1) {
                     Weapon tmp = new Weapon(selected.getName());
                     ship.removeFromCargoBay(selected);
                     ship.addToCargoBay(tmp);
                 }
             } else if (command.matches(CMD_PACKAGE)) {
                 Item selected = (Item) cargoList.getItemAtIndex(cargoList.getIndex());
                 if (selected.getQuantity() == 1) {
                    Equipment tmp = (Equipment) selected;
                     ship.removeFromCargoBay(selected);
                     Item nTmp = new Item(tmp.getName());
                     ship.addToCargoBay(nTmp);
                 }
             } else if (command.matches(CMD_DEPLOY)) {
                 Item selected = (Item) cargoList.getItemAtIndex(cargoList.getIndex());
                 if (selected.getQuantity() == 1) {
                     //deploy the station
                     Station ret = new Station(name, selected.getName());
                     ret.setName("Your "+selected.getName());
                     ret.setFaction(ship.getFaction());
                     ret.init(false);
                     //configure coordinates
                     double dx = ship.getWidth();
                     double dy = ship.getHeight();
                     double sx = (ship.getX() + ship.getWidth() / 2) + dx;
                     double sy = (ship.getY() + ship.getHeight() / 2) + dy;
                     ret.setX(sx);
                     ret.setY(sy);
                     //finalize
                     ret.setCurrentSystem(ship.getCurrentSystem());
                     ship.getCurrentSystem().putEntityInSystem(ret);
                     //remove item from cargo
                     selected.setQuantity(0);
                     ship.removeFromCargoBay(selected);
                     //since it's not NPC make sure it has no start cash
                     ret.clearWares();
                 }
             }
         }
     }
 
     private void stackItem(ArrayList<Item> cargoBay, Item selected) {
         if (cargoBay.contains(selected)) {
             for (int a = 0; a < cargoBay.size(); a++) {
                 Item tmp = cargoBay.get(a);
                 if (tmp != selected) {
                     if (selected.getName().matches(tmp.getName())) {
                         if (selected.getGroup().matches(tmp.getGroup())) {
                             if (selected.getType().matches(tmp.getType())) {
                                 tmp.setQuantity(selected.getQuantity() + tmp.getQuantity());
                                 cargoBay.remove(selected);
                                 break;
                             }
                         }
                     }
                 }
             }
         }
     }
 }
