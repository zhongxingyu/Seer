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
 
 import celestial.Celestial;
 import celestial.Jumphole;
 import celestial.Planet;
 import celestial.Ship.Ship;
import celestial.Ship.Ship.Autopilot;
 import celestial.Ship.Station;
 import celestial.Star;
 import engine.Entity;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Random;
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
     //backplate
     private String back;
     //what it contains
     private ArrayList<Entity> entities = new ArrayList<>();
     //quick reference
     private ArrayList<Entity> jumpholeList = new ArrayList<>();
     private ArrayList<Entity> celestialList = new ArrayList<>();
     private ArrayList<Entity> stationList = new ArrayList<>();
     private ArrayList<Entity> shipList = new ArrayList<>();
     //who owns it
     private String owner = "Neutral";
     //what contains it
     private Universe universe;
 
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
             Parser tmp = new Parser("PLANET.txt");
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
 
     private Planet makePlanet(Term planetTerm) {
         Planet planet = null;
         {
             //extract terms
             String pName = planetTerm.getValue("name");
             String texture = planetTerm.getValue("texture");
             //find logical texture
             Parser tmp = new Parser("PLANET.txt");
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
                 Parser lParse = new Parser("LOADOUTS.txt");
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
         //put in the correct sublist
         if (entity instanceof Station) {
             stationList.add(entity);
         } else if (entity instanceof Ship) {
             shipList.add(entity);
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
     }
 
     public void disposeGraphics() {
         for (int a = 0; a < entities.size(); a++) {
             if (entities.get(a) instanceof Celestial) {
                 Celestial tmp = (Celestial) entities.get(a);
                 tmp.disposeGraphics();
             }
         }
     }
 
     @Override
     public void periodicUpdate(double tpf) {
         for (int a = 0; a < entities.size(); a++) {
             entities.get(a).periodicUpdate(tpf);
             if (entities.get(a).getState() == Entity.State.DEAD) {
                 //remove the entity
                 pullEntityFromSystem(entities.get(a));
             } else if (entities.get(a) instanceof Ship) {
                 Ship test = (Ship) entities.get(a);
                 double perc = test.getFuel() / test.getMaxFuel();
                 if (perc < 0.03) {
                     if (!entities.contains(universe.getPlayerShip())) {
                         System.out.println("Removing derelict ship " + test.getName()+" :: "+test.getAutopilot());
                         test.setState(State.DYING);
                     } else {
                         //don't do this while the player is watching
                     }
                 }
             }
         }
         //cleanup graphics if the player is not present
         if (!entities.contains(universe.playerShip) && universe.playerShip.getState() == State.ALIVE) {
             disposeGraphics();
         }
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
 }
