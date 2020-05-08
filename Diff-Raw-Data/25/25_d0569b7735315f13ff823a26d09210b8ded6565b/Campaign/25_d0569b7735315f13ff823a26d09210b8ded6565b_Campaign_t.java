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
  * A complex, scripted, mission that the player can embark on. Has multiple
  * nodes involved.
  */
 package universe;
 
 import celestial.Celestial;
 import celestial.Ship.Ship;
 import celestial.Ship.Ship.Behavior;
 import celestial.Ship.Station;
 import engine.Entity;
 import java.awt.geom.Point2D;
 import java.io.Serializable;
 import java.util.ArrayList;
 import lib.Faction;
 import lib.Parser;
 import lib.Parser.Term;
 
 /**
  *
  * @author nwiehoff
  */
 public class Campaign implements Serializable {
 
     private String name;
     private Parser script;
     private Term node;
     //universe
     private Universe universe;
 
     public Campaign(Universe universe, String name) {
         this.universe = universe;
         this.name = name;
         System.out.println("Starting campaign: " + name);
         //load campaign script
         script = new Parser("campaign/" + name + ".txt");
         //locate the start of the campaign
         node = findNode("CAMPAIGN_START");
         if (node != null) {
             //campaign started
         } else {
             messagePlayer("Error", "This campaign does not exist!");
         }
     }
 
     private Term findNode(String name) {
         ArrayList<Term> nodes = script.getTermsOfType("Node");
         for (int a = 0; a < nodes.size(); a++) {
             if (nodes.get(a).getValue("name").equals(name)) {
                 return nodes.get(a);
             }
         }
         return null;
     }
 
     private void messagePlayer(String name, String body) {
         universe.playerShip.composeMessage(universe.playerShip, name, body, null);
     }
 
     public void periodicUpdate(double tpf) {
         if (node != null) {
             checkAdvance();
             checkFailure();
         }
     }
 
     private void checkAdvance() {
         //check to see if we can advance
         String advance = node.getValue("advance");
         if (advance.equals("none")) {
             next();
         } else if (advance.equals("END")) {
             checkEndAdvance();
         } else if (advance.equals("SILENT_END")) {
             checkSilentEndAdvance();
         } else {
             //these are the more complex ones which have parameters
             String[] split = advance.split("::");
             //condition::parameter
             if (split.length == 2) {
                 String condition = split[0].trim();
                 String parameter = split[1].trim();
                 if (condition.equals("ENTERSYSTEM")) {
                     checkEnterSystemAdvance(parameter);
                 } else if (condition.equals("DOCK")) {
                     checkDockAdvance(parameter);
                 } else if (condition.equals("NONEALIVE")) {
                     checkNoneAliveAdvance(parameter);
                 } else if (condition.equals("GOTO")) {
                     checkGotoAdvance(parameter);
                 }
             }
         }
     }
 
     private void checkGotoAdvance(String parameter) throws NumberFormatException {
         String[] arr = parameter.split(",");
         String lSys = arr[0].trim();
         String lEnt = arr[1].trim();
         String dist = arr[2].trim();
         //parse distance
         double distance = Double.parseDouble(dist);
         //see if we are in the right system to check
         Ship player = universe.getPlayerShip();
         if (player.getCurrentSystem().getName().equals(lSys)) {
             //see if we are near this celestial
             ArrayList<Entity> celestials = player.getCurrentSystem().getCelestialList();
             Celestial pick = null;
             for (int a = 0; a < celestials.size(); a++) {
                 if (celestials.get(a) instanceof Ship) {
                     //make sure it isn't player owned. We don't want the player's named ships to confuse the script.
                     Ship test = (Ship) celestials.get(a);
                     if (test.getFaction().equals(player.getFaction())) {
                         //don't include this object
                     } else {
                         if (test.getName().equals(lEnt)) {
                             pick = test;
                             break;
                         }
                     }
                 } else if (celestials.get(a) instanceof Celestial) {
                     Celestial test = (Celestial) celestials.get(a);
                     if (test.getName().equals(lEnt)) {
                         pick = test;
                         break;
                     }
                 }
             }
             //test distance
             if (pick != null) {
                 double d = player.distanceTo(pick);
                 if (d <= distance) {
                     //trigger reached
                     next();
                 } else {
                     //sad christmas
                 }
             }
         } else {
             //no point in testing further
         }
     }
 
     private void checkNoneAliveAdvance(String parameter) {
         //triggered when no ship/station of a certain group is left alive
         String group = parameter;
         boolean foundOne = false;
         //iterate through all entities to check groups
         for (int a = 0; a < universe.getSystems().size(); a++) {
             if (!foundOne) {
                 ArrayList<Entity> ships = universe.getSystems().get(a).getShipList();
                 ArrayList<Entity> stations = universe.getSystems().get(a).getStationList();
                 for (int b = 0; b < ships.size(); b++) {
                     Ship tmp = (Ship) ships.get(b);
                     if (tmp.getGroup().equals(group)) {
                         foundOne = true;
                         break;
                     }
                 }
                 for (int b = 0; b < stations.size(); b++) {
                     Ship tmp = (Ship) stations.get(b);
                     if (tmp.getGroup().equals(group)) {
                         foundOne = true;
                         break;
                     }
                 }
             } else {
                 //no point
             }
         }
         //check to see if we found one
         if (!foundOne) {
             //trigger reached
             next();
         }
     }
 
     private void checkDockAdvance(String parameter) {
         //triggered when a player docks at a certain station
         Ship player = universe.getPlayerShip();
        String[] args = parameter.split(",");
        String sys = args[0].trim();
        String stn = args[1].trim();
         if (player.isDocked()) {
             Station host = player.getPort().getParent();
            if (host.getName().equals(stn)) {
                if (host.getCurrentSystem().getName().equals(sys)) {
                    //trigger reached
                    next();
                }
             } else {
                 //not yet
             }
         }
     }
 
     private void checkEnterSystemAdvance(String parameter) {
         //triggered when the player is in a certain system
         if (universe.getPlayerShip().getCurrentSystem().getName().equals(parameter)) {
             //trigger reached
             next();
         } else {
             //not yet
         }
     }
 
     private void checkSilentEndAdvance() {
         //this is the end of the campaign, but doesn't say anything
         node = null;
         //stop campaign by removing reference
         universe.getPlayerCampaigns().remove(this);
         //store campaign in the completed campaigns list
         universe.getCompletedCampaigns().add(this);
     }
 
     private void checkEndAdvance() {
         //this is the end of the campaign
         node = null;
         messagePlayer("Campaign Complete", "Congratulations! You've finished '" + name + "'");
         //stop campaign by removing reference
         universe.getPlayerCampaigns().remove(this);
         //store campaign in the completed campaigns list
         universe.getCompletedCampaigns().add(this);
     }
 
     private void checkFailure() {
         //check to see if the mission has been failed
         String failure = node.getValue("fail");
         //not all nodes have failure conditions
         if (failure != null) {
             if (failure.equals("none")) {
                 //do nothing
             } else {
                 //these are the more complex ones which have parameters
                 String[] split = failure.split("::");
                 //condition::parameter
                 if (split.length == 2) {
                     String condition = split[0].trim();
                     String parameter = split[1].trim();
                     if (condition.equals("ENTERSYSTEM")) {
                         checkEnterSystemFail(parameter);
                     } else if (condition.equals("DOCK")) {
                         checkDockFail(parameter);
                     } else if (condition.equals("NONEALIVE")) {
                         checkNoneAliveFail(parameter);
                     }
                 }
             }
         }
     }
 
     private void checkNoneAliveFail(String parameter) {
         //triggered when no ship/station of a certain group is left alive
         String group = parameter;
         boolean foundOne = false;
         //iterate through all entities to check groups
         for (int a = 0; a < universe.getSystems().size(); a++) {
             if (!foundOne) {
                 ArrayList<Entity> ships = universe.getSystems().get(a).getShipList();
                 ArrayList<Entity> stations = universe.getSystems().get(a).getStationList();
                 for (int b = 0; b < ships.size(); b++) {
                     Ship tmp = (Ship) ships.get(b);
                     if (tmp.getGroup().equals(group)) {
                         foundOne = true;
                         break;
                     }
                 }
                 for (int b = 0; b < stations.size(); b++) {
                     Ship tmp = (Ship) stations.get(b);
                     if (tmp.getGroup().equals(group)) {
                         foundOne = true;
                         break;
                     }
                 }
             } else {
                 //no point
             }
         }
         //check to see if we found one
         if (!foundOne) {
             //trigger reached
             fail();
         }
     }
 
     private void checkDockFail(String parameter) {
         //triggered when a player docks at a certain station
         Ship player = universe.getPlayerShip();
        String[] args = parameter.split(",");
        String sys = args[0].trim();
        String stn = args[1].trim();
         if (player.isDocked()) {
             Station host = player.getPort().getParent();
            if (host.getName().equals(stn)) {
                if (host.getCurrentSystem().getName().equals(sys)) {
                    //trigger reached
                    fail();
                }
             } else {
                 //not yet
             }
         }
     }
 
     private void checkEnterSystemFail(String parameter) {
         //triggered when the player is in a certain system
         if (universe.getPlayerShip().getCurrentSystem().getName().equals(parameter)) {
             //trigger reached
             fail();
         } else {
             //not yet
         }
     }
 
     private void next() {
         //automatically advance to next node
         String next = node.getValue("next");
         advance(next);
     }
 
     private void fail() {
         //automatically advance to the failure node
         String next = node.getValue("failure");
         advance(next);
     }
 
     private void advance(String next) {
         Term tmp = findNode(next);
         if (tmp != null) {
             node = tmp;
             //display the chapter message
             String chapter = node.getValue("chapter");
             String objective = node.getValue("objective");
             String body = node.getValue("body");
             //compose
             String main = chapter + " /br/ /br/ ";
             if (body != null) {
                 main += objective + " /br/ /br/ " + body;
             } else {
                 main += objective;
             }
             //send
             messagePlayer(chapter, main);
             //call any functions this node has
             int a = 0;
             String function = null;
             while ((function = node.getValue("call" + a)) != null) {
                 System.out.println("Calling " + function);
                 //call this function
                 String[] arr = function.split("::");
                 if (arr.length == 2) {
                     do2FieldFunction(arr);
                 } else if (arr.length == 3) {
                     do3FieldFunction(arr);
                 } else if (arr.length == 4) {
                     do4FieldFunction(arr);
                 }
                 //increment
                 a++;
             }
         } else {
             messagePlayer("Error", "Unexpected end of story.");
         }
     }
 
     private void do2FieldFunction(String[] arr) {
         //object::method
         String object = arr[0].trim();
         String method = arr[1].trim();
         if (object.equals("CURRENT_STATION")) {
             //modify the current station the player is at
             Station station = universe.getPlayerShip().getPort().getParent();
             if (station != null && universe.getPlayerShip().isDocked()) {
                 if (method.equals("makeMortal()")) {
                     //make this station mortal
                     station.makeMortal();
                 }
             } else {
                 //do nothing
             }
         }
     }
 
     private void do3FieldFunction(String[] arr) {
         //action::param1::details
         String action = arr[0].trim();
         String param1 = arr[1].trim();
         String param2 = arr[2].trim();
         if (action.equals("SPAWNSHIP")) {
             parseSpawnShip(param1, param2);
         } else if (action.equals("SPAWNSTATION")) {
             parseSpawnStation(param1, param2);
         } else if (action.equals("PLAYER")) {
             /*
              * This block is dedicated to handling functions that modify the current player ship
              */
             if (param1.equals("SETSTANDING")) {
                 setPlayerStanding(param2);
             } else if (param1.equals("ADDCASH")) {
                 givePlayerCash(param2);
             }
         } else if (action.equals("COMMGROUP")) {
             /*
              * Dedicated to sending orders to a group of ships
              */
             String group = param1;
             //split to find orders
             String[] split = param2.split(",");
             String command = split[0];
             //find ships in this group
             for (int a = 0; a < universe.getSystems().size(); a++) {
                 ArrayList<Entity> ships = universe.getSystems().get(a).getShipList();
                 for (int b = 0; b < ships.size(); b++) {
                     Ship tmp = (Ship) ships.get(b);
                     if (tmp.getGroup().equals(group)) {
                         if (command.equals("FLYTO")) {
                             parseFlyTo(split, tmp);
                         }
                         if (command.equals("FOLLOW")) {
                             parseFollow(split, tmp);
                         }
                         if (command.equals("BEHAVE")) {
                             parseBehave(split, tmp);
                         }
                         if (command.equals("DOCKAT")) {
                             parseDockAt(split, tmp);
                         }
                     }
                 }
             }
         }
     }
 
     private void parseDockAt(String[] split, Ship tmp) {
         String target = split[1].trim();
         //fly to only works in the current system the object is in
         Station dockTarget = null;
         //find the target in system
         for (int x = 0; x < tmp.getCurrentSystem().getEntities().size(); x++) {
             Entity test = tmp.getCurrentSystem().getEntities().get(x);
             if (test instanceof Station) {
                 Station stat = (Station) test;
                 if (stat.getName().equals(target)) {
                     dockTarget = stat;
                     break;
                 }
             }
         }
         //if we found it, go there
         if (dockTarget != null) {
             tmp.cmdDock(dockTarget);
         }
     }
 
     private void parseSpawnShip(String param1, String param2) throws NumberFormatException {
         //the group name
         String group = param1;
         //split param2 into details
         //The Archers,Native Land,-5439.0,11587.0,Pirate Raider,Archer Pirate,PATROL
         String[] split = param2.split(",");
         String faction = split[0];
         String sys = split[1];
         String sx = split[2];
         String sy = split[3];
         String load = split[4];
         String name = split[5];
         String behave = split[6];
         //find the correct system
         SolarSystem system = null;
         for (int x = 0; x < universe.getSystems().size(); x++) {
             if (universe.getSystems().get(x).getName().equals(sys)) {
                 system = universe.getSystems().get(x);
                 break;
             }
         }
         //generate coordinates
         double x = Double.parseDouble(sx);
         double y = Double.parseDouble(sy);
         //setup faction
         Faction myFaction = new Faction(faction);
         //setup behavior
         Behavior behavior = Behavior.NONE;
         if (behave.equals("PATROL")) {
             behavior = Behavior.PATROL;
         } else if (behave.equals("SECTOR_TRADE")) {
             behavior = Behavior.SECTOR_TRADE;
         } else if (behave.equals("UNIVERSE_TRADE")) {
             behavior = Behavior.UNIVERSE_TRADE;
         } else if (behave.equals("TEST")) {
             behavior = Behavior.TEST;
         } else if (behave.equals("NONE")) {
             behavior = Behavior.NONE;
         }
         //spawn ship
         //spawnShip(Faction faction, SolarSystem system, Point2D.Double loc, String loadout, String name, Behavior behavior)
         universe.getGod().spawnShip(myFaction, system, new Point2D.Double(x, y), load, name, behavior, group);
     }
 
     private void parseSpawnStation(String param1, String param2) throws NumberFormatException {
         //the group name
         String group = param1;
         //split param2 into details
         //SPAWNSTATION::TestGroup::ITC,The Highway,-4439.0,11587.0,ITC Power Converter,Rogue ITC Station
         String[] split = param2.split(",");
         String faction = split[0];
         String sys = split[1];
         String sx = split[2];
         String sy = split[3];
         String load = split[4];
         String name = split[5];
         //find the correct system
         SolarSystem system = null;
         for (int x = 0; x < universe.getSystems().size(); x++) {
             if (universe.getSystems().get(x).getName().equals(sys)) {
                 system = universe.getSystems().get(x);
                 break;
             }
         }
         //generate coordinates
         double x = Double.parseDouble(sx);
         double y = Double.parseDouble(sy);
         //setup faction
         Faction myFaction = new Faction(faction);
         //spawn station
         //spawnStation(Faction faction, SolarSystem system, Point2D.Double loc, String type, String name)
         universe.getGod().spawnStation(myFaction, system, new Point2D.Double(x, y), load, name, group);
     }
 
     private void givePlayerCash(String param2) throws NumberFormatException {
         long mod = Long.parseLong(param2.trim());
         universe.getPlayerShip().setCash(universe.getPlayerShip().getCash() + mod);
     }
 
     private void setPlayerStanding(String param2) throws NumberFormatException {
         String[] split = param2.split(",");
         String fact = split[0].trim();
         String sRaw = split[1].trim();
         double standing = Double.parseDouble(sRaw);
         universe.getPlayerShip().getMyFaction().setStanding(fact, standing);
     }
 
     private void parseFlyTo(String[] split, Ship tmp) throws NumberFormatException {
         String target = split[1].trim();
         String sRange = split[2].trim();
         //fly to only works in the current system the object is in
         Celestial flyToTarget = null;
         double flyToRange = Double.parseDouble(sRange);
         //find the target in system
         for (int x = 0; x < tmp.getCurrentSystem().getEntities().size(); x++) {
             Entity test = tmp.getCurrentSystem().getEntities().get(x);
             if (test instanceof Celestial) {
                 Celestial cel = (Celestial) test;
                 if (cel.getName().equals(target)) {
                     flyToTarget = cel;
                     break;
                 }
             }
         }
         //if we found it, go there
         if (flyToTarget != null) {
             tmp.cmdFlyToCelestial(flyToTarget, flyToRange);
         } else if (target.equals("PLAYER")) {
             tmp.cmdFlyToCelestial(universe.getPlayerShip(), flyToRange);
         }
     }
 
     private void parseFollow(String[] split, Ship tmp) throws NumberFormatException {
         String target = split[1].trim();
         String sRange = split[2].trim();
         //fly to only works in the current system the object is in
         Ship followTarget = null;
         double followRange = Double.parseDouble(sRange);
         //find the target in system
         for (int x = 0; x < tmp.getCurrentSystem().getEntities().size(); x++) {
             Entity test = tmp.getCurrentSystem().getEntities().get(x);
             if (test instanceof Ship) {
                 Ship cel = (Ship) test;
                 if (cel.getName().equals(target)) {
                     followTarget = cel;
                     break;
                 }
             }
         }
         //if we found it, go there
         if (followTarget != null) {
             tmp.cmdFollowShip(followTarget, followRange);
         } else if (target.equals("PLAYER")) {
             tmp.cmdFollowShip(universe.getPlayerShip(), followRange);
         }
     }
 
     private void parseBehave(String[] split, Ship tmp) {
         String behave = split[1].trim();
         //determine correct new behavior
         Behavior behavior = Behavior.NONE;
         if (behave.equals("PATROL")) {
             behavior = Behavior.PATROL;
         } else if (behave.equals("SECTOR_TRADE")) {
             behavior = Behavior.SECTOR_TRADE;
         } else if (behave.equals("UNIVERSE_TRADE")) {
             behavior = Behavior.UNIVERSE_TRADE;
         } else if (behave.equals("TEST")) {
             behavior = Behavior.TEST;
         } else if (behave.equals("NONE")) {
             behavior = Behavior.NONE;
         }
         //set behavior
         tmp.setBehavior(behavior);
     }
 
     private void do4FieldFunction(String[] arr) {
         //type::system::entity::method
         String type = arr[0];
         String system = arr[1];
         String entity = arr[2];
         String method = arr[3];
         if (type.equals("STATION")) {
             //working with a space station
             for (int x = 0; x < universe.getSystems().size(); x++) {
                 if (universe.getSystems().get(x).getName().equals(system)) {
                     SolarSystem sys = universe.getSystems().get(x);
                     for (int b = 0; b < sys.getEntities().size(); b++) {
                         Entity working = sys.getEntities().get(b);
                         if (working instanceof Station) {
                             Station tmpStat = (Station) working;
                             if (tmpStat.getName().equals(entity)) {
                                 if (method.equals("makeMortal()")) {
                                     //make this station mortal
                                     tmpStat.makeMortal();
                                 } else if (method.equals("makeImmortal()")) {
                                     //make this station immortal
                                     tmpStat.makeImmortal();
                                 }
                             }
                         }
                     }
                 }
             }
         }
     }
 
     public String getName() {
         return name;
     }
 }
