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
  * Displays the contents of a station your ship is currently docked at and allows
  * you to buy and sell to the station.
  */
 package gdi;
 
 import cargo.Item;
 import celestial.Ship.Ship;
 import celestial.Ship.Station;
 import gdi.component.AstralInput;
 import gdi.component.AstralLabel;
 import gdi.component.AstralList;
 import gdi.component.AstralWindow;
 import java.awt.Color;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 
 public class TradeWindow extends AstralWindow {
 
     private Ship ship;
     private Station docked;
     AstralLabel buyLabel = new AstralLabel();
     AstralLabel sellLabel = new AstralLabel();
     AstralList productList = new AstralList(this);
     AstralList resourceList = new AstralList(this);
     AstralList cargoList = new AstralList(this);
     AstralList propertyList = new AstralList(this);
     AstralList optionList = new AstralList(this);
     AstralInput input = new AstralInput();
     //logical
     AstralList lastFocus = cargoList;
     //behavior
 
     private enum Behavior {
 
         WAITING_TO_BUY,
         WAITING_TO_SELL,
         NONE
     };
     private Behavior action = Behavior.NONE;
 
     public TradeWindow() {
         super();
         generate();
     }
 
     private void generate() {
         backColor = windowGrey;
         //size this window
         width = 500;
         height = 400;
         setVisible(false);
         //setup input method
         input.setName("Input");
         input.setText("|");
         input.setY(getHeight() / 2);
         input.setVisible(false);
         input.setWidth(150);
         input.setX((getWidth() / 2) - input.getWidth() / 2);
         input.setHeight((input.getFont().getSize() + 2));
         //setup buying label
         buyLabel.setName("buy");
         buyLabel.setText("Buying");
         buyLabel.setX(0);
         buyLabel.setY(0);
         buyLabel.setWidth(width / 2);
         buyLabel.setHeight(buyLabel.getFont().getSize() + 2);
         buyLabel.setVisible(true);
         //setup selling label
         sellLabel.setName("sell");
         sellLabel.setText("Selling");
         sellLabel.setX(width / 2);
         sellLabel.setY(0);
         sellLabel.setWidth(width / 2);
         sellLabel.setHeight(sellLabel.getFont().getSize() + 2);
         sellLabel.setVisible(true);
         //setup the buying list
         resourceList.setX(0);
         resourceList.setY(buyLabel.getFont().getSize() + 2);
         resourceList.setWidth((width / 2) - 1);
         resourceList.setHeight((height / 2) - 3);
         resourceList.setVisible(true);
         //setup the selling list
         productList.setX(width / 2);
         productList.setY(sellLabel.getFont().getSize() + 2);
         productList.setWidth((width / 2) - 1);
         productList.setHeight((height / 2) - 3);
         productList.setVisible(true);
         //setup cargo list
         cargoList.setX(0);
         cargoList.setY((height / 2) + sellLabel.getFont().getSize());
         cargoList.setWidth((int) (width / 1.5));
         cargoList.setHeight((height / 4) - (buyLabel.getFont().getSize() + 2));
         cargoList.setVisible(true);
         //setup property list
         propertyList.setX(0);
         propertyList.setY((height / 2) + (height / 4));
         propertyList.setWidth((int) (width / 1.5));
         propertyList.setHeight((height / 4) - 1);
         propertyList.setVisible(true);
         //setup options list
         optionList.setX((int) (width / 1.5) + 1);
         optionList.setY((height / 2) + sellLabel.getFont().getSize());
         optionList.setWidth((int) (width / 3) - 1);
         optionList.setHeight((height / 2) - (sellLabel.getFont().getSize() + 1));
         optionList.setVisible(true);
         //pack
         addComponent(buyLabel);
         addComponent(sellLabel);
         addComponent(resourceList);
         addComponent(productList);
         addComponent(cargoList);
         addComponent(propertyList);
         addComponent(optionList);
         addComponent(input);
     }
 
     public void update(Ship ship) {
         this.ship = ship;
         if (ship.isDocked()) {
             docked = ship.getPort().getParent();
             productList.clearList();
             resourceList.clearList();
             cargoList.clearList();
             propertyList.clearList();
             optionList.clearList();
             {
                 ArrayList<Item> logicalCargoList = new ArrayList<>();
                 //add cargo goods
                 ArrayList<Item> cargo = ship.getCargoBay();
                 for (int a = 0; a < cargo.size(); a++) {
                     logicalCargoList.add(cargo.get(a));
                 }
                 //add to display
                 for (int a = 0; a < logicalCargoList.size(); a++) {
                     cargoList.addToList(logicalCargoList.get(a));
                 }
                 //display products and resources
                 Station station = ship.getPort().getParent();
                 ArrayList<Item> selling = station.getStationSelling();
                 for (int a = 0; a < selling.size(); a++) {
                     productList.addToList(selling.get(a));
                 }
                 ArrayList<Item> buying = station.getStationBuying();
                 for (int a = 0; a < buying.size(); a++) {
                     resourceList.addToList(buying.get(a));
                 }
                 //display info on selected
                 int index = lastFocus.getIndex();
                 if (lastFocus.getItemAtIndex(index) != null) {
                     Item selected = (Item) lastFocus.getItemAtIndex(index);
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
             //behave
             behave();
         } else {
             //you can't trade when you're not docked
             setVisible(false);
             docked = null;
         }
     }
 
     private void behave() {
         if (action == Behavior.NONE) {
             //do nothing
         } else if (action == Behavior.WAITING_TO_BUY) {
             //check if a value was returned
             try {
                 if (input.canReturn()) {
                     int val = Integer.parseInt(input.getText());
                    //perform trade
                    int index = lastFocus.getIndex();
                    Item selected = (Item) lastFocus.getItemAtIndex(index);
                    docked.buy(ship, selected, val);
                     //hide it
                     input.setVisible(false);
                     //normal mode
                     action = Behavior.NONE;
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         } else if (action == Behavior.WAITING_TO_SELL) {
             //check if a value was returned
             try {
                 if (input.canReturn()) {
                     int val = Integer.parseInt(input.getText());
                    //perform trade
                    int index = lastFocus.getIndex();
                    Item selected = (Item) lastFocus.getItemAtIndex(index);
                    docked.sell(ship, selected, val);
                     //hide it
                     input.setVisible(false);
                     //normal mode
                     action = Behavior.NONE;
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
         } else {
             //unreachable
         }
     }
 
     @Override
     public void handleMouseClickedEvent(MouseEvent me) {
         if (action == Behavior.NONE) {
             super.handleMouseClickedEvent(me);
             //get the module and toggle its enabled status
             if (productList.isFocused()) {
                 lastFocus = productList;
             } else if (resourceList.isFocused()) {
                 lastFocus = resourceList;
             } else if (cargoList.isFocused()) {
                 lastFocus = cargoList;
             }
             //handle trade commands
             if (optionList.isFocused()) {
                 String command = (String) optionList.getItemAtIndex(optionList.getIndex());
                 parseCommand(command);
             }
         }
     }
 
     private void parseCommand(String command) {
         if (command != null) {
             if (command.matches("Sell")) {
                 //get item
                 /*int index = lastFocus.getIndex();
                  Item selected = (Item) lastFocus.getItemAtIndex(index);
                  docked.sell(ship, selected, 1);*/
                 showInput();
                 action = Behavior.WAITING_TO_SELL;
             } else if (command.matches("Buy")) {
                 /*int index = lastFocus.getIndex();
                  Item selected = (Item) lastFocus.getItemAtIndex(index);
                  docked.buy(ship, selected, 1);*/
                 showInput();
                 action = Behavior.WAITING_TO_BUY;
             }
         }
     }
 
     private void showInput() {
         input.setText("1");
         input.setVisible(true);
         input.setFocused(true);
     }
 
     private void fillCommandLines(Item selected) {
         optionList.addToList("--Market--");
         optionList.addToList("Price: " + docked.getPrice(selected));
         optionList.addToList(" ");
         optionList.addToList("--Trade--");
         if (lastFocus == cargoList) {
             optionList.addToList("Sell");
         } else if (lastFocus == productList) {
             optionList.addToList("Buy");
         } else if (lastFocus == resourceList) {
             optionList.addToList("Resources Not Sold.");
             optionList.addToList("Check Cargo Bay.");
         }
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
 }
