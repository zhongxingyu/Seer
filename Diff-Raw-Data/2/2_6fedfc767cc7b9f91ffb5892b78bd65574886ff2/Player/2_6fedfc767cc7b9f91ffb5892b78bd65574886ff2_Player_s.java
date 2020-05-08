 package model;
 
 import java.util.Hashtable;
 import java.util.Iterator;
 
 import model.exception.DeadBossException;
 import model.exception.DeadUnitException;
 import model.units.Unit;
 
 /**
  * @author Aurel
  * 
  */
 public class Player {
     UnitFactory factory;
     private Hashtable<String, Unit> units;
     private boolean isTurn = true;
     private String boss;
 
     public Player() {
 	boss = "Pegasus";
 	units = new Hashtable<String, Unit>();
 	factory = new UnitFactoryPegasus();
     }
 
     public Player(String boss) {
 	units = new Hashtable<String, Unit>();
 	this.boss = boss;
	if (this.boss == "Dragon") {
 	    factory = new UnitFactoryDragon();
 	} else {
 	    factory = new UnitFactoryPegasus();
 	}
 
     }
 
     /**
      * Create a new unit.
      * 
      * @param name
      *            the name of the new unit
      */
     public void addUnit(String name) {
 	units.put(name, factory.getUnit(name));
     }
 
     /**
      * Attack between two units.
      * 
      * @param att
      *            the attacking unit
      * @param def
      *            the defending unit
      * @param tank
      *            if the tank is in range of defending unit
      * @return the result of the fight
      * @throws DeadUnitException
      *             if a unit is dead
      * @throws DeadBossException
      *             if a boss is dead
      */
     public String attackWith(Unit att, Unit def, boolean tank)
 	    throws DeadUnitException, DeadBossException {
 	return att.attack(def, tank);
     }
 
     /**
      * Deletes a unit according to the name.
      * 
      * @param name
      */
     public void delUnit(String name) {
 	units.remove(name);
     }
 
     /**
      * Returns the name of the boss.
      * 
      * @return the boss
      */
     public String getBoss() {
 	return boss;
     }
 
     /**
      * Return the names of units that can be created.
      * 
      * @return the names
      */
     public String[] getNamesOfUnits() {
 	return factory.getNamesOfUnits();
     }
 
     /**
      * Returns if it is the turn of the player to play.
      * 
      * @return true if the turn of player false otherwise
      */
     public boolean getTurn() {
 	return isTurn;
     }
 
     /**
      * Returns a unit according x and y axis.
      * 
      * @param x
      *            the x axis
      * @param y
      *            the y axis
      * @return the unit
      */
     public Unit getUnit(int x, int y) {
 	Iterator<Unit> it = units.values().iterator();
 	Unit temp;
 	while (it.hasNext()) {
 	    temp = it.next();
 	    if (temp.getTile().x == x && temp.getTile().y == y)
 		return temp;
 	}
 	return null;
     }
 
     /**
      * Returns a unit according to the name.
      * 
      * @param name
      * @return the unit
      */
     public Unit getUnit(String name) {
 	return units.get(name);
     }
 
     /**
      * Returns the units of a player.
      * 
      * @return the units
      */
     public Hashtable<String, Unit> getUnits() {
 	return units;
     }
 
     /**
      * Set the turn of the player.
      * 
      * @param b
      *            the turn
      */
     public void setTurn(boolean b) {
 	isTurn = b;
     }
 
 }
