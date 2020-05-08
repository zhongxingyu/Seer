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
  * Allows the management of a player's property.
  * Nathan Wiehoff
  */
 package gdi;
 
 import cargo.Hardpoint;
 import cargo.Item;
 import celestial.Celestial;
 import celestial.Ship.Ship;
 import celestial.Ship.Ship.Autopilot;
 import celestial.Ship.Ship.Behavior;
 import celestial.Ship.Station;
 import engine.Entity;
 import engine.Entity.State;
 import gdi.component.AstralInput;
 import gdi.component.AstralList;
 import gdi.component.AstralWindow;
 import java.awt.event.MouseEvent;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 
 public class PropertyWindow extends AstralWindow {
 
     private enum Mode {
 
         NONE, //idle
         WAITING_FOR_STATION, //waiting for a station to dock at
         WAITING_FOR_CREDITS, //waiting for credits to be specified
         WAITING_FOR_NAME, //waiting for a new name
         WAITING_FOR_ATTACK, //waiting for a ship to attack
         WAITING_FOR_CELESTIAL, //waiting for a celestial to fly to
         WAITING_FOR_CELESTIAL_RANGE, //waiting for range to fly to celestial to
         WAITING_FOR_FOLLOW, //waiting for a ship to follow
         WAITING_FOR_FOLLOW_RANGE, //waiting for a range to follow at
     };
     private Mode mode = Mode.NONE;
     public static final String CMD_SWITCH = "Switch Ship";
     public static final String CMD_PATROL = "Start Patrol AI";
     public static final String CMD_TRADE = "Start Trade AI";
     public static final String CMD_NONE = "End Program";
     public static final String CMD_MOVEFUNDS = "Credit Transfer";
     public static final String CMD_RENAME = "Rename";
     public static final String CMD_UNDOCK = "Undock";
     public static final String CMD_DOCK = "Dock";
     public static final String CMD_FLYTO = "Move To Position";
     public static final String CMD_FOLLOW = "Follow";
     public static final String CMD_ATTACK = "Attack";
     public static final String CMD_DESTRUCT = "Self Destruct";
     public static final String CMD_ALLSTOP = "All Stop";
     AstralInput input = new AstralInput();
     AstralList propertyList = new AstralList(this);
     AstralList infoList = new AstralList(this);
     AstralList optionList = new AstralList(this);
     AstralList inputList = new AstralList(this);
     protected Ship ship;
 
     public PropertyWindow() {
         super();
         generate();
     }
 
     private void generate() {
         backColor = windowGrey;
         //size this window
         width = 500;
         height = 400;
         setVisible(false);
         //setup the cargo list
         propertyList.setX(0);
         propertyList.setY(0);
         propertyList.setWidth(width);
         propertyList.setHeight((height / 2) - 1);
         propertyList.setVisible(true);
         //setup the property list
         infoList.setX(0);
         infoList.setY(height / 2);
         infoList.setWidth((int) (width / 1.5));
         infoList.setHeight((height / 2) - 1);
         infoList.setVisible(true);
         //setup the command list
         optionList.setX((int) (width / 1.5) + 1);
         optionList.setY(height / 2);
         optionList.setWidth((int) (width / 3));
         optionList.setHeight((height / 2) - 1);
         optionList.setVisible(true);
         //setup input method
         input.setName("Input");
         input.setText("|");
         input.setVisible(false);
         input.setWidth(width / 3);
         input.setX((getWidth() / 2) - input.getWidth() / 2);
         input.setHeight((input.getFont().getSize() + 2));
         input.setY((getHeight() / 2) - input.getHeight() / 2);
         //setup input list
         inputList.setWidth((int) (width / 1.5));
         inputList.setX((getWidth() / 2) - inputList.getWidth() / 2);
         inputList.setHeight((height / 2) - 1);
         inputList.setVisible(false);
         inputList.setY((getHeight() / 2) - inputList.getHeight() / 2);
         //pack
         addComponent(propertyList);
         addComponent(infoList);
         addComponent(optionList);
         //do last
         addComponent(inputList);
         addComponent(input);
     }
 
     private void showInput(String text) {
         input.setText(text);
         input.setVisible(true);
         input.setFocused(true);
     }
 
     private void showInputList(ArrayList<Object> options) {
         inputList.clearList();
         for (int a = 0; a < options.size(); a++) {
             inputList.addToList(options.get(a));
         }
         inputList.setVisible(true);
         inputList.setFocused(true);
     }
 
     private void hideInputList() {
         inputList.setVisible(false);
         inputList.setIndex(0);
     }
 
     private void behave(Ship selected) {
         if (mode == Mode.NONE) {
             //do nothing
         } else if (mode == Mode.WAITING_FOR_CREDITS) {
             if (input.canReturn()) {
                 Ship player = ship.getUniverse().getPlayerShip();
                 try {
                     int val = Integer.parseInt(input.getText());
                     if (val > 0) {
                         //we are pushing
                         long source = player.getCash();
                         if (source >= val) {
                             selected.setCash(selected.getCash() + val);
                             player.setCash(player.getCash() - val);
                         } else {
                             //insufficient credits
                         }
                     } else {
                         //we are pulling
                         long source = selected.getCash();
                         long tfr = -val;
                         if (source >= tfr) {
                             player.setCash(player.getCash() + tfr);
                             selected.setCash(selected.getCash() - tfr);
                         } else {
                             //insufficient credits
                         }
                     }
                     //hide it
                     input.setVisible(false);
                     //normal mode
                     mode = Mode.NONE;
                 } catch (Exception e) {
                     System.out.println("Malformed input");
                 }
             }
         } else if (mode == Mode.WAITING_FOR_NAME) {
             try {
                 if (input.canReturn()) {
                     //get name
                     String nm = input.getText();
                     //push
                     selected.setName(nm);
                     //normal mode
                     mode = Mode.NONE;
                 }
             } catch (Exception e) {
                 System.out.println("Malformed input");
             }
         } else if (mode == Mode.WAITING_FOR_STATION) {
             Object raw = inputList.getItemAtIndex(inputList.getIndex());
             if (raw instanceof Station) {
                 //grab it
                 Station pick = (Station) raw;
                 //order docking
                 selected.cmdAbortDock();
                 selected.cmdDock(pick);
                 //hide it
                 hideInputList();
                 //normal mode
                 mode = Mode.NONE;
             } else {
                 //probably selected some info text
             }
         } else if (mode == Mode.WAITING_FOR_ATTACK) {
             Object raw = inputList.getItemAtIndex(inputList.getIndex());
             if (raw instanceof Ship) {
                 //grab it
                 Ship pick = (Ship) raw;
                 //order attack
                 selected.cmdFightTarget(pick);
                 //hide it
                 hideInputList();
                 //normal mode
                 mode = Mode.NONE;
             } else {
                 //probably selected some info text
             }
         } else if (mode == Mode.WAITING_FOR_CELESTIAL) {
             Object raw = inputList.getItemAtIndex(inputList.getIndex());
             if (raw instanceof Celestial) {
                 //grab it
                 Celestial pick = (Celestial) raw;
                 //store celestial
                 selected.setFlyToTarget(pick);
                 //hide it
                 hideInputList();
                 //show the next step
                 showInput("1000");
                 //get range
                 mode = Mode.WAITING_FOR_CELESTIAL_RANGE;
             } else {
                 //probably selected some info text
             }
         } else if (mode == Mode.WAITING_FOR_CELESTIAL_RANGE) {
             try {
                 if (input.canReturn()) {
                     //get input
                     String nm = input.getText();
                     Double range = Double.parseDouble(nm);
                     //start command
                     selected.cmdFlyToCelestial(selected.getFlyToTarget(), range);
                     //normal mode
                     mode = Mode.NONE;
                 }
             } catch (Exception e) {
                 System.out.println("Malformed input");
             }
         } else if (mode == Mode.WAITING_FOR_FOLLOW) {
             Object raw = inputList.getItemAtIndex(inputList.getIndex());
             if (raw instanceof Ship) {
                 //grab it
                 Ship pick = (Ship) raw;
                 //store celestial
                 selected.setFlyToTarget(pick);
                 //hide it
                 hideInputList();
                 //show the next step
                 showInput("100");
                 //get range
                 mode = Mode.WAITING_FOR_FOLLOW_RANGE;
             } else {
                 //probably selected some info text
             }
         } else if (mode == Mode.WAITING_FOR_FOLLOW_RANGE) {
             try {
                 if (input.canReturn()) {
                     //get input
                     String nm = input.getText();
                     Double range = Double.parseDouble(nm);
                     //start command
                     selected.cmdFollowShip((Ship) selected.getFlyToTarget(), range);
                     //normal mode
                     mode = Mode.NONE;
                 }
             } catch (Exception e) {
                 System.out.println("Malformed input");
             }
         }
     }
 
     public void update(Ship ship) {
         setShip(ship);
         propertyList.clearList();
         infoList.clearList();
         optionList.clearList();
         ArrayList<Ship> logicalPropertyList = new ArrayList<>();
         if (ship != null) {
             //get global list
             ArrayList<Ship> prop = ship.getUniverse().getPlayerProperty();
             for (int a = 0; a < prop.size(); a++) {
                 logicalPropertyList.add(prop.get(a));
             }
             //push list to window
             for (int a = 0; a < prop.size(); a++) {
                 propertyList.addToList(prop.get(a));
             }
             //display detailed information about the selected item
             int index = propertyList.getIndex();
             if (index < logicalPropertyList.size()) {
                 Ship selected = (Ship) propertyList.getItemAtIndex(index);
                 behave(selected);
                 infoList.addToList("--Basic--");
                 infoList.addToList(" ");
                 infoList.addToList("Credits:      " + selected.getCash());
                 infoList.addToList("Behavior:     " + selected.getBehavior());
                 infoList.addToList("Autopilot:    " + selected.getAutopilot());
                 /*
                  * Specifics
                  */
                 infoList.addToList(" ");
                 infoList.addToList("--Advanced--");
                 infoList.addToList(" ");
                 fillSpecifics(selected);
                 infoList.addToList(" ");
                 infoList.addToList("--Integrity--");
                 infoList.addToList(" ");
                 infoList.addToList("Shield:       " + roundTwoDecimal(100.0 * (selected.getShield() / selected.getMaxShield())) + "%");
                 infoList.addToList("Hull:         " + roundTwoDecimal(100.0 * (selected.getHull() / selected.getMaxHull())) + "%");
                 infoList.addToList("Fuel:         " + roundTwoDecimal(100.0 * (selected.getFuel() / selected.getMaxFuel())) + "%");
                 infoList.addToList(" ");
                 infoList.addToList("--Fitting--");
                 infoList.addToList(" ");
                 ArrayList<Hardpoint> fit = selected.getHardpoints();
                 for (int a = 0; a < fit.size(); a++) {
                     infoList.addToList(fit.get(a));
                 }
                 infoList.addToList(" ");
                 infoList.addToList("--Cargo--");
                 infoList.addToList(" ");
                 ArrayList<Item> cargo = selected.getCargoBay();
                 for (int a = 0; a < cargo.size(); a++) {
                     infoList.addToList(cargo.get(a));
                 }
                 infoList.addToList(" ");
                 //more
                 fillDescriptionLines(selected);
                 fillCommandLines(selected);
             }
         }
     }
 
     private void fillSpecifics(Ship selected) {
         if (selected != null) {
             /*
              * More autopilot info
              */
             if (selected.getAutopilot() == Autopilot.FLY_TO_CELESTIAL) {
                 infoList.addToList("Waypoint:     " + selected.getFlyToTarget().getName());
             }
             if (selected.getPort() != null) {
                 if (selected.getAutopilot() == Autopilot.DOCK_STAGE1) {
                     infoList.addToList("Docking At:   " + selected.getPort().getParent().getName());
                 } else if (selected.getAutopilot() == Autopilot.DOCK_STAGE2) {
                     infoList.addToList("Docking At:   " + selected.getPort().getParent().getName());
                 } else if (selected.getAutopilot() == Autopilot.DOCK_STAGE3) {
                     infoList.addToList("Docking At:   " + selected.getPort().getParent().getName());
                 }
             }
             /*
              * More behavior info 
              */
             if (selected.getBehavior() == Behavior.PATROL) {
                 //what are we flying to?
                 if (selected.getTarget() != null) {
                     infoList.addToList("Attacking:    " + selected.getTarget().getName());
                 } else {
                     infoList.addToList("NO AIM");
                 }
             } else if (selected.getBehavior() == Behavior.SECTOR_TRADE) {
                 Station start = selected.getBuyFromStation();
                 Station end = selected.getSellToStation();
                 Item ware = selected.getWorkingWare();
                 if (start != null && end != null && ware != null) {
                     infoList.addToList("Ware:         " + selected.getWorkingWare().getName());
                     infoList.addToList("From:         " + start.getName());
                     infoList.addToList("To:           " + end.getName());
                 }
             }
         }
     }
 
     private double roundTwoDecimal(double d) {
         try {
             DecimalFormat twoDForm = new DecimalFormat("#.##");
             return Double.parseDouble(twoDForm.format(d));
         } catch (Exception e) {
             System.out.println("Not a Number");
             return 0;
         }
     }
 
     public Ship getShip() {
         return ship;
     }
 
     public void setShip(Ship ship) {
         this.ship = ship;
     }
 
     private void fillDescriptionLines(Ship selected) {
         /*
          * Fills in the item's description being aware of things like line breaking on spaces.
          */
         if (selected != null) {
             Item shipItem = new Item(selected.getType());
             String description = shipItem.getDescription();
             //fill
             int lineWidth = (((infoList.getWidth() - 10) / (infoList.getFont().getSize())));
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
                             infoList.addToList(tmp);
                             tmp = "";
                             cursor = 0;
                             a--;
                         } else {
                             tmp += "[LEN!]";
                         }
                     }
                 } else {
                     infoList.addToList(tmp);
                     tmp = "";
                     cursor = 0;
                     if (!words[a].matches("/br/")) {
                         a--;
                     }
                 }
             }
             infoList.addToList(tmp.toString());
         }
     }
 
     private void fillCommandLines(Ship selected) {
         if (selected != null) {
             /*
              * Funds transfer can happen no matter where the ships are located.
              */
             optionList.addToList("--Transfer--");
             optionList.addToList(" ");
             optionList.addToList(CMD_MOVEFUNDS);
             /*
              * Some actions are only possible while both ships are docked in the same
              * station. This is the block for those.
              */
             if (selected.isDocked() && selected.getUniverse().getPlayerShip().isDocked()) {
                 if (selected.getPort() != null) {
                     Station a = selected.getPort().getParent();
                     Station b = selected.getUniverse().getPlayerShip().getPort().getParent();
                     if (a == b) {
                         optionList.addToList(CMD_SWITCH);
                     }
                 }
             }
             optionList.addToList(" ");
             /*
              * These activate behaviors on a ship
              */
             optionList.addToList("--Console--");
             optionList.addToList(" ");
             optionList.addToList(CMD_RENAME);
             optionList.addToList(" ");
             optionList.addToList(CMD_NONE);
             optionList.addToList(" ");
             optionList.addToList(CMD_TRADE);
             optionList.addToList(CMD_PATROL);
             optionList.addToList(" ");
             /*
              * Some things can't be done while docked.
              */
             if (selected.isDocked()) {
                 optionList.addToList(CMD_UNDOCK);
             } else {
                 optionList.addToList(CMD_DOCK);
                 optionList.addToList(CMD_FLYTO);
                 optionList.addToList(CMD_FOLLOW);
                 optionList.addToList(CMD_ALLSTOP);
                 optionList.addToList(" ");
                 optionList.addToList("--Combat--");
                 optionList.addToList(" ");
                 optionList.addToList(CMD_ATTACK);
                 optionList.addToList(" ");
                 optionList.addToList("--Red Zone--");
                 optionList.addToList(" ");
                 optionList.addToList(CMD_DESTRUCT);
             }
         }
     }
 
     @Override
     public void handleMouseClickedEvent(MouseEvent me) {
         super.handleMouseClickedEvent(me);
         if (optionList.isFocused()) {
             String command = (String) optionList.getItemAtIndex(optionList.getIndex());
             parseCommand(command);
         }
     }
 
     private void parseCommand(String command) {
         if (command != null && mode == Mode.NONE) {
             Ship selected = (Ship) propertyList.getItemAtIndex(propertyList.getIndex());
             if (command.matches(CMD_SWITCH)) {
                 /*
                  * Switch to another ship.
                  */ ship.getUniverse().setPlayerShip(selected);
             } else if (command.matches(CMD_NONE)) {
                 //abort current behavior
                 selected.setBehavior(Behavior.NONE);
                 selected.setAutopilot(Autopilot.NONE);
                 selected.cmdAbortDock();
             } else if (command.matches(CMD_TRADE)) {
                 selected.setBehavior(Behavior.SECTOR_TRADE);
             } else if (command.matches(CMD_PATROL)) {
                 selected.setBehavior(Behavior.PATROL);
             } else if (command.matches(CMD_MOVEFUNDS)) {
                 mode = Mode.WAITING_FOR_CREDITS;
                 showInput("0");
             } else if (command.matches(CMD_RENAME)) {
                 mode = Mode.WAITING_FOR_NAME;
                 showInput(selected.getName());
             } else if (command.matches(CMD_UNDOCK)) {
                 selected.cmdUndock();
             } else if (command.matches(CMD_DOCK)) {
                 mode = Mode.WAITING_FOR_STATION;
                 ArrayList<Object> choice = new ArrayList<>();
                 choice.add("--Select Station To Dock At--");
                 choice.add(" ");
                 ArrayList<Station> st = selected.getFriendlyStationsInSystem();
                 for (int a = 0; a < st.size(); a++) {
                     choice.add(st.get(a));
                 }
                if (choice.size() > 0) {
                     showInputList(choice);
                 } else {
                     mode = Mode.NONE;
                 }
             } else if (command.matches(CMD_ATTACK)) {
                 mode = Mode.WAITING_FOR_ATTACK;
                 ArrayList<Object> choice = new ArrayList<>();
                 choice.add("--Select Target To Attack--");
                 choice.add(" ");
                 ArrayList<Ship> sh = selected.getShipsInSensorRange();
                 for (int a = 0; a < sh.size(); a++) {
                     choice.add(sh.get(a));
                 }
                if (choice.size() > 0) {
                     showInputList(choice);
                 } else {
                     mode = Mode.NONE;
                 }
             } else if (command.matches(CMD_DESTRUCT)) {
                 selected.setState(State.DYING);
             } else if (command.matches(CMD_FLYTO)) {
                 mode = Mode.WAITING_FOR_CELESTIAL;
                 ArrayList<Object> choice = new ArrayList<>();
                 choice.add("--Select Target To Fly To--");
                 choice.add(" ");
                 ArrayList<Entity> tmp = selected.getCurrentSystem().getCelestialList();
                 for (int a = 0; a < tmp.size(); a++) {
                     choice.add(tmp.get(a));
                 }
                 ArrayList<Entity> jhp = selected.getCurrentSystem().getJumpholeList();
                 for (int a = 0; a < jhp.size(); a++) {
                     choice.add(jhp.get(a));
                 }
                if (choice.size() > 0) {
                     showInputList(choice);
                 } else {
                     mode = Mode.NONE;
                 }
             } else if (command.matches(CMD_FOLLOW)) {
                 mode = Mode.WAITING_FOR_FOLLOW;
                 ArrayList<Object> choice = new ArrayList<>();
 
                 choice.add("--Select Target To Follow--");
                 choice.add(" ");
                 ArrayList<Ship> sh = selected.getShipsInSensorRange();
                 for (int a = 0; a < sh.size(); a++) {
                     choice.add(sh.get(a));
                 }
                if (choice.size() > 0) {
                     showInputList(choice);
                 } else {
                     mode = Mode.NONE;
                 }
             } else if (command.matches(CMD_ALLSTOP)) {
                 selected.cmdAllStop();
             }
         }
     }
 }
