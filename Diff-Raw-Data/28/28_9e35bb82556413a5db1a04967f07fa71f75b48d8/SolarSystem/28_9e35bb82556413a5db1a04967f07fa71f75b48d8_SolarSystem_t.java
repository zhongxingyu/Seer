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
  * Solar systems are a collection of planets and other celestials in a convenient
  * package. It provides zoning for the universe.
  */
 package universe;
 
 import cargo.Equipment;
 import cargo.Hardpoint;
 import cargo.Weapon;
 import celestial.Asteroid;
 import celestial.Celestial;
 import celestial.Jumphole;
 import celestial.Planet;
 import celestial.Ship.Projectile;
 import celestial.Ship.Ship;
 import celestial.Ship.Station;
 import celestial.Star;
 import engine.Entity;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Random;
 import lib.Faction;
 import lib.Parser;
 import lib.Parser.Term;
 
 /**
  *
  * @author Nathan Wiehoff
  */
 public class SolarSystem implements Entity, Serializable {
     //this system
 
     protected String name;
     double x;
     double y;
     //music
     private String ambientMusic = "audio/music/Undefined.wav";
     private String dangerMusic = "audio/music/Committing.wav";
     //backplate
     private String back;
     //what it contains
     private ArrayList<Entity> entities = new ArrayList<>();
     //quick reference
     private ArrayList<Entity> jumpholeList = new ArrayList<>();
     private final ArrayList<Entity> celestialList = new ArrayList<>();
     private final ArrayList<Entity> asteroidList = new ArrayList<>();
     private final ArrayList<Entity> stationList = new ArrayList<>();
     private final ArrayList<Entity> shipList = new ArrayList<>();
     //who owns it
     private String owner = "Neutral";
     //what contains it
     private final Universe universe;
    //for making cleanup more efficient
    private boolean hasGraphics = false;
 
     public SolarSystem(Universe universe, String name, Parser parse) {
         this.name = name; //needed for lookup
         this.universe = universe;
         //generate
         generateSystem(parse);
     }
 
     private void generateSystem(Parser parse) {
         /*
          * Adds all member objects. Member objects are any object that is
          * a member of this system according to the "system" param and is
          * one of the following
          * 
          * Star
          * Planet
          * Asteroid
          * Ship
          * Station
          */
         ArrayList<Term> stars = parse.getTermsOfType("Star");
         for (int a = 0; a < stars.size(); a++) {
             if (stars.get(a).getValue("system").matches(getName())) {
                 //this planet needs to be created and stored
                 putEntityInSystem(makeStar(stars.get(a)));
             }
         }
         ArrayList<Term> planets = parse.getTermsOfType("Planet");
         for (int a = 0; a < planets.size(); a++) {
             if (planets.get(a).getValue("system").matches(getName())) {
                 //this planet needs to be created and stored
                 putEntityInSystem(makePlanet(planets.get(a)));
             }
         }
         ArrayList<Term> asteroids = parse.getTermsOfType("Asteroid");
         for (int a = 0; a < asteroids.size(); a++) {
             if (asteroids.get(a).getValue("system").matches(getName())) {
                 //this asteroid needs to be created and stored
                 putEntityInSystem(makeAsteroid(asteroids.get(a)));
             }
         }
         ArrayList<Term> ships = parse.getTermsOfType("Ship");
         for (int a = 0; a < ships.size(); a++) {
             if (ships.get(a).getValue("system").matches(getName())) {
                 //this planet needs to be created and stored
                 putEntityInSystem(makeShip(ships.get(a)));
             }
         }
 
         ArrayList<Term> stations = parse.getTermsOfType("Station");
         for (int a = 0; a < stations.size(); a++) {
             if (stations.get(a).getValue("system").matches(getName())) {
                 //this planet needs to be created and stored
                 putEntityInSystem(makeStation(stations.get(a)));
             }
         }
 
         ArrayList<Term> jumpholes = parse.getTermsOfType("Jumphole");
         for (int a = 0; a < jumpholes.size(); a++) {
             if (jumpholes.get(a).getValue("system").matches(getName())) {
                 //this planet needs to be created and stored
                 putEntityInSystem(makeJumphole(jumpholes.get(a)));
             }
         }
     }
 
     private Star makeStar(Term starTerm) {
         Star star = null;
         {
             //extract terms
             String pName = starTerm.getValue("name");
             String texture = starTerm.getValue("texture");
             //find logical texture
             Parser tmp = Universe.getCache().getPlanetCache();
             Term tex = null;
             ArrayList<Term> list = tmp.getTermsOfType("Star");
             for (int a = 0; a < list.size(); a++) {
                 if (list.get(a).getValue("name").matches(texture)) {
                     tex = list.get(a);
                     break;
                 }
             }
             //extract terms
             int diameter = Integer.parseInt(starTerm.getValue("d"));
             double px = Double.parseDouble(starTerm.getValue("x"));
             double py = Double.parseDouble(starTerm.getValue("y"));
             int seed = Integer.parseInt(starTerm.getValue("seed"));
             //make planet and store
             star = new Star(pName, tex, diameter);
             star.setX(px);
             star.setY(py);
             star.setSeed(seed);
         }
         return star;
     }
 
     private Asteroid makeAsteroid(Term asteroidTerm) {
         Asteroid asteroid = null;
         {
             String pName = asteroidTerm.getValue("name");
             double px = Double.parseDouble(asteroidTerm.getValue("x"));
             double py = Double.parseDouble(asteroidTerm.getValue("y"));
             double th = Double.parseDouble(asteroidTerm.getValue("t"));
             Asteroid ast = new Asteroid(pName);
             ast.setX(px);
             ast.setY(py);
             ast.setTheta(th);
             ast.setCurrentSystem(this);
             asteroid = ast;
         }
         return asteroid;
     }
 
     private Planet makePlanet(Term planetTerm) {
         Planet planet = null;
         {
             //extract terms
             String pName = planetTerm.getValue("name");
             String texture = planetTerm.getValue("texture");
             //find logical texture
             Parser tmp = Universe.getCache().getPlanetCache();
             Term tex = null;
             ArrayList<Term> list = tmp.getTermsOfType("Planet");
             for (int a = 0; a < list.size(); a++) {
                 if (list.get(a).getValue("name").matches(texture)) {
                     tex = list.get(a);
                     break;
                 }
             }
             //extract terms
             int diameter = Integer.parseInt(planetTerm.getValue("d"));
             double px = Double.parseDouble(planetTerm.getValue("x"));
             double py = Double.parseDouble(planetTerm.getValue("y"));
             int seed = Integer.parseInt(planetTerm.getValue("seed"));
             //make planet and store
             planet = new Planet(pName, tex, diameter);
             planet.setX(px);
             planet.setY(py);
             planet.setSeed(seed);
         }
         return planet;
     }
 
     private Ship makeShip(Term shipTerm) {
         Ship ret = null;
         Random rnd = new Random();
         {
             String ship = shipTerm.getValue("ship");
             String near = shipTerm.getValue("near");
             String name = shipTerm.getValue("name");
             String install = shipTerm.getValue("install");
             String faction = shipTerm.getValue("faction");
             String cargo = shipTerm.getValue("cargo");
             String template = shipTerm.getValue("template");
             if (template != null) {
                 //load this template
                 Parser lParse = Universe.getCache().getUniverseCache();
                 ArrayList<Term> lods = lParse.getTermsOfType("Loadout");
                 for (int a = 0; a < lods.size(); a++) {
                     if (lods.get(a).getValue("name").matches(template)) {
                         //get terms
                         cargo = lods.get(a).getValue("cargo");
                         install = lods.get(a).getValue("install");
                         ship = lods.get(a).getValue("ship");
                         break;
                     }
                 }
             }
 
             //create player
             ret = new Ship(name, ship);
             if (template != null) {
                 ret.setTemplate(template);
             }
             //check template
             ret.setEquip(install);
             ret.setFaction(faction);
             ret.init(false);
             ret.addInitialCargo(cargo);
             //put it in the right system next to the start object
             if (near != null) {
                 for (int b = 0; b < entities.size(); b++) {
                     if (entities.get(b).getName().matches(near)) {
                         ret.setX(entities.get(b).getX() + rnd.nextInt(12800) - 6400);
                         ret.setY(entities.get(b).getY() + rnd.nextInt(12800) - 6400);
                         break;
                     }
                 }
             } else {
                 //it was given specific xy coordinates i guess
                 String sx = shipTerm.getValue("x");
                 String sy = shipTerm.getValue("y");
                 if (sx != null && sy != null) {
                     double tx = Double.parseDouble(sx);
                     double ty = Double.parseDouble(sy);
                     ret.setX(tx);
                     ret.setY(ty);
                 } else {
                     //or not? just throw it somewhere.
                     ret.setX(rnd.nextInt(100000) - 50000);
                     ret.setY(rnd.nextInt(100000) - 50000);
                 }
             }
             ret.setCurrentSystem(this);
         }
         return ret;
     }
 
     private Station makeStation(Term shipTerm) {
         Station ret = null;
         Random rnd = new Random();
         {
             String ship = shipTerm.getValue("ship");
             String near = shipTerm.getValue("near");
             String name = shipTerm.getValue("name");
             String faction = shipTerm.getValue("faction");
             //create player
             ret = new Station(name, ship);
             ret.setFaction(faction);
             //put it in the right system next to the start object
             if (near != null) {
                 for (int b = 0; b < entities.size(); b++) {
                     if (entities.get(b).getName().matches(near)) {
                         ret.setX(entities.get(b).getX() + rnd.nextInt(2000) - 1000);
                         ret.setY(entities.get(b).getY() + rnd.nextInt(2000) - 1000);
                         break;
                     }
                 }
             } else {
                 //it was given specific xy coordinates i guess
                 String sx = shipTerm.getValue("x");
                 String sy = shipTerm.getValue("y");
                 if (sx != null && sy != null) {
                     double tx = Double.parseDouble(sx);
                     double ty = Double.parseDouble(sy);
                     ret.setX(tx);
                     ret.setY(ty);
                 } else {
                     //or not? just throw it somewhere.
                     ret.setX(rnd.nextInt(100000) - 50000);
                     ret.setY(rnd.nextInt(100000) - 50000);
                 }
             }
             ret.setCurrentSystem(this);
         }
         return ret;
     }
 
     private Jumphole makeJumphole(Term planetTerm) {
         Jumphole ret = null;
         {
             String pName = planetTerm.getValue("name");
             String out = planetTerm.getValue("out");
             double px = Double.parseDouble(planetTerm.getValue("x"));
             double py = Double.parseDouble(planetTerm.getValue("y"));
             Jumphole hole = new Jumphole(pName, universe);
             hole.setX(px);
             hole.setY(py);
             hole.setOut(out);
             hole.setCurrentSystem(this);
             ret = hole;
         }
         return ret;
     }
 
     public ArrayList<Entity> getEntities() {
         return entities;
     }
 
     public void setEntities(ArrayList<Entity> celestials) {
         this.entities = celestials;
     }
 
     public void putEntityInSystem(Entity entity) {
         entities.add(entity);
         if (entity instanceof Asteroid) {
             Asteroid tmp = (Asteroid) entity;
             tmp.setCurrentSystem(this);
             asteroidList.add(tmp);
         } else if (entity instanceof Celestial) {
             //let it know where it is
             Celestial tmp = (Celestial) entity;
             tmp.setCurrentSystem(this);
             celestialList.add(tmp);
         }
         //put in the correct sublist
         if (entity instanceof Station) {
             stationList.add(entity);
             Station test = (Station) entity;
             if (test.getFaction().matches("Player")) {
                 //yep, add it to the global list
                 universe.getPlayerProperty().add(entity);
             }
         } else if (entity instanceof Ship) {
             shipList.add(entity);
             //is this player owned?
             if (!(entity instanceof Projectile)) {
                 Ship test = (Ship) entity;
                 if (test.getFaction().matches("Player")) {
                     //yep, add it to the global list
                     universe.getPlayerProperty().add(entity);
                 }
             }
         } else if (entity instanceof Jumphole) {
             jumpholeList.add(entity);
         } else if (entity instanceof Celestial) {
             celestialList.add(entity);
         }
     }
 
     public void pullEntityFromSystem(Entity entity) {
         entities.remove(entity);
         stationList.remove(entity);
         shipList.remove(entity);
         celestialList.remove(entity);
         jumpholeList.remove(entity);
         asteroidList.remove(entity);
         //remove from global list
         universe.getPlayerProperty().remove(entity);
     }
 
     @Override
     public void init(boolean loadedGame) {
         for (int a = 0; a < entities.size(); a++) {
             entities.get(a).init(loadedGame);
         }
     }
 
     public void initGraphics() {
         for (int a = 0; a < entities.size(); a++) {
             if (entities.get(a) instanceof Celestial) {
                 Celestial tmp = (Celestial) entities.get(a);
                 tmp.initGraphics();
             }
         }
        //mark system as having graphics
        hasGraphics = true;
     }
 
     public void disposeGraphics() {
         for (int a = 0; a < entities.size(); a++) {
             if (entities.get(a) instanceof Celestial) {
                 Celestial tmp = (Celestial) entities.get(a);
                 tmp.disposeGraphics();
             }
         }
        //mark system as not having graphics
        hasGraphics = false;
     }
 
     @Override
     public void periodicUpdate(double tpf) {
         updateEntities(tpf);
         updateSov();
     }
 
     @Override
     public void render(Graphics f, double dx, double dy) {
         //please never call this, talk to the entities directly.
     }
 
     @Override
     public State getState() {
         return State.ALIVE;
     }
 
     @Override
     public ArrayList<Rectangle> getBounds() {
         return new ArrayList<>();
     }
 
     @Override
     public boolean collideWith(Entity target) {
         return false;
     }
 
     @Override
     public boolean collideWith(Rectangle target) {
         return false;
     }
 
     @Override
     public double getX() {
         return x;
     }
 
     @Override
     public double getY() {
         return y;
     }
 
     @Override
     public void setX(double x) {
         this.x = (int) x;
     }
 
     @Override
     public void setY(double y) {
         this.y = (int) y;
     }
 
     @Override
     public String getName() {
         return name;
     }
 
     @Override
     public void setName(String name) {
         this.name = name;
     }
 
     @Override
     public void informOfCollisionWith(Entity target) {
         //?!
     }
 
     public ArrayList<Entity> getCelestialList() {
         return celestialList;
     }
 
     public ArrayList<Entity> getStationList() {
         return stationList;
     }
 
     public ArrayList<Entity> getShipList() {
         return shipList;
     }
 
     public String getBack() {
         return back;
     }
 
     public void setBack(String back) {
         this.back = back;
     }
 
     public ArrayList<Entity> getJumpholeList() {
         return jumpholeList;
     }
 
     public void setJumpholeList(ArrayList<Entity> jumpholeList) {
         this.jumpholeList = jumpholeList;
     }
 
     public Universe getUniverse() {
         return universe;
     }
 
     public String getOwner() {
         return owner;
     }
 
     public void setOwner(String owner) {
         this.owner = owner;
     }
 
     public String getAmbientMusic() {
         return ambientMusic;
     }
 
     public void setAmbientMusic(String ambientMusic) {
         this.ambientMusic = ambientMusic;
     }
 
     public String getDangerMusic() {
         return dangerMusic;
     }
 
     public void setDangerMusic(String dangerMusic) {
         this.dangerMusic = dangerMusic;
     }
 
     @Override
     public String toString() {
         return name + ", " + owner;
     }
 
     public ArrayList<Entity> getAsteroidList() {
         return asteroidList;
     }
 
     private void updateEntities(double tpf) {
         for (int a = 0; a < entities.size(); a++) {
             entities.get(a).periodicUpdate(tpf);
             if (a < entities.size()) {
                 if (entities.get(a).getState() == Entity.State.DEAD) {
                     //remove the entity
                     pullEntityFromSystem(entities.get(a));
                 } else if (entities.get(a) instanceof Ship) {
                     if (!entities.contains(universe.getPlayerShip())) {
                         Ship test = (Ship) entities.get(a);
                         //don't do OOS checks on player property obviously
                         if (!test.getFaction().matches(universe.getPlayerShip().getFaction())) {
                             //remove entities the player can't see that are out of fuel
                             double fuelPercent = test.getFuel() / test.getMaxFuel();
                             if (fuelPercent < 0.03) {
                                 System.out.println("Removing derelict ship [F] " + test.getName() + " :: " + test.getAutopilot());
                                 test.setState(State.DYING);
                             }
                             //see if this entity has a weapon with ammo left
                             boolean hasAmmo = false;
                             ArrayList<Hardpoint> hp = test.getHardpoints();
                             if (hp.size() > 0) {
                                 for (int l = 0; l < hp.size(); l++) {
                                     Equipment mounted = hp.get(l).getMounted();
                                     if (mounted instanceof Weapon) {
                                         Weapon tmp = (Weapon) mounted;
                                         if (tmp.hasAmmo()) {
                                             hasAmmo = true;
                                             break;
                                         }
                                     }
                                 }
                             } else {
                                 hasAmmo = true;
                             }
                             //remove entities that are completely out of ammo
                             if (hasAmmo) {
                                 //do nothing
                             } else {
                                 System.out.println("Removing derelict ship [A] " + test.getName() + " :: " + test.getAutopilot());
                                 test.setState(State.DYING);
                             }
                             //remove entities that bailed outside the player system
                            if (test.isBailed()) {
                                 System.out.println("Removing bailed ship " + test.getName());
                                 test.setState(State.DYING);
                             } else {
                                 //do nothing
                             }
                         } else {
                             discover();
                         }
                     } else {
                         discover();
                     }
                 }
             } else {
                 //it jumped out
             }
         }
        //cleanup graphics if the player is not present
        if (!entities.contains(universe.playerShip)) {
            if (hasGraphics) {
                //ony called when the system had graphics
                disposeGraphics();
                System.out.println("System " + getName() + " disposed graphics.");
            }
        } else {
            //system must have graphics if the player is inside it
            hasGraphics = true;
        }
     }
 
     private void updateSov() {
         if (owner.matches("Player")) {
             //count player's stations
             int count = 0;
             for (int a = 0; a < stationList.size(); a++) {
                 Station test = (Station) stationList.get(a);
                 if (test.getFaction().matches("Player")) {
                     count++;
                 }
             }
             if (count <= 0) {
                 //return to a random sov holder
                 Parser fp = Universe.getCache().getFactionCache();
                 ArrayList<Faction> choices = new ArrayList<>();
                 ArrayList<Term> it = fp.getTermsOfType("Faction");
                 for (int a = 0; a < it.size(); a++) {
                     Faction test = new Faction(it.get(a).getValue("name"));
                     if (test.isEmpire()) {
                         choices.add(test);
                     }
                 }
                 Faction newOwner = choices.get(new Random().nextInt(choices.size()));
                 owner = newOwner.getName();
                 //notify player
                 Ship ship = universe.getPlayerShip();
                 ship.composeMessage(ship, getName() + " lost", "It seems you've lost your "
                         + "grip. Sorry to hear that. We at Paralegal can help. /br/ /br/ "
                         + "Paralegal: Beyond the Law", null);
                 ship.composeMessage(ship, newOwner.getName(), "We'll deal with " + getName() + " now.", null);
             } else {
                 //player retains sov
             }
         } else {
             //do nothing
         }
     }
 
     private void discover() {
         //add to discovered list if needed
         if (universe.getDiscoveredSpace().contains(this)) {
             //do nothing
         } else {
             //add to discovered space
             universe.getDiscoveredSpace().add(this);
         }
     }
 }
