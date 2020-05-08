 package vooga.rts.manager;
 
 import java.awt.Graphics2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import vooga.rts.IGameLoop;
 import vooga.rts.gamedesign.sprite.rtsprite.interactive.units.Unit;
 import vooga.rts.util.Location3D;
 
 
 /**
  * The Unit Manager is responsible for everything regarding
  * the units of the players.
  * It provides methods for selecting units, grouping them,
  * deselecting, executing commands on them, etc.
  * 
  * @author Jonathan Schmidt
  * 
  */
 public class UnitManager implements IGameLoop {
     private List<Unit> myUnits;
     private List<Unit> mySelectedUnits;
 
     private Map<Integer, List<Unit>> myGroups;
     private boolean myMultiSelect;
 
     /**
      * Creates a new Unit Manager
      */
     public UnitManager () {
         myUnits = new ArrayList<Unit>();
         mySelectedUnits = new ArrayList<Unit>();
         myGroups = new HashMap<Integer, List<Unit>>();
         myMultiSelect = false;
     }
 
     /**
      * Selects a specific Unit and marks it as selected.
      * 
      * @param u
      */
     public void select (Unit u) {
         if (!mySelectedUnits.contains(u)) {
             if (myUnits.contains(u)) {
                 mySelectedUnits.add(u);
                 u.select(true);
             }
         }
     }
 
     /**
      * Selects the top most unit that is underneath
      * the provided Point location.
      * This is used for selecting units by mouse click.
      * 
      * @param loc
      */
     public void select (Location3D loc) {
         deselectAll();
         for (int i = getAllUnits().size() - 1; i >= 0; i--) {
             Unit u = getAllUnits().get(i);
             if (u.intersects(loc)) {
                 select(u);
                 return;
             }
         }
     }
 
     /**
      * Selects all the units in provided rectangle.
      * Allows a user to drag around the desired units.
      * 
      * @param area The area to select the units in.
      */
     public void select (Rectangle2D area) {
         deselectAll();
        for (Unit u : myUnits) {
        	System.out.println(u.getBounds());
             if (area.intersects(u.getBounds())) {
            	System.out.println("yolo");
                 select(u);
             }
         }
     }
 
     /**
      * Deselects the specified unit.
      * 
      * @param u The unit to deselect
      */
     public void deselect (Unit u) {
         if (mySelectedUnits.contains(u)) {
             mySelectedUnits.remove(u);
             u.select(false);
         }
     }
 
     /**
      * Groups the currently selected units together with a
      * specified group ID
      * 
      * @param groupID The ID of the group
      */
     public void group (int groupID) {
         myGroups.put(groupID, new ArrayList<Unit>(mySelectedUnits));
     }
 
     /**
      * Activates a previously create group of units.
      * 
      * @param groupID The ID of the group to select
      */
     public void activateGroup (int groupID) {
         if (myGroups.containsKey(groupID)) {
             mySelectedUnits = new ArrayList<Unit>(myGroups.get(groupID));
         }
     }
 
     /**
      * Deselects all the units
      */
     public void deselectAll () {
         if (myMultiSelect) {
             return;
         }
         for (Unit u : mySelectedUnits) {
             u.select(false);
         }
         mySelectedUnits.clear();
     }
 
     /**
      * Returns the list of selected units.
      * 
      * @return The selected units
      */
     public List<Unit> getSelected () {
         return mySelectedUnits;
     }
 
     /**
      * Returns the list of all the units in the manager.
      * 
      * @return List of all units
      */
     public List<Unit> getAllUnits () {
         return myUnits;
     }
 
     @Override
     public void update (double elapsedTime) {
         for (Unit u : myUnits) {
             u.update(elapsedTime);
         }
     }
 
     @Override
     public void paint (Graphics2D pen) {
         for (Unit u : myUnits) {
             u.paint(pen);
         }
     }
 
     /**
      * Adds a unit to the unit manager.
      * This will be done when a new unit is created.
      * 
      * @param u The unit that is to be added.
      */
     public void addUnit (Unit u) {
         myUnits.add(u);
     }
 
     /**
      * Sets the Unit Manager into multi select mode which
      * allows the user to select more than one unit at a time.
      * 
      * @param val whether it is multi select or not
      */
     public void setMultiSelect (boolean val) {
         myMultiSelect = val;
     }
 
 }
