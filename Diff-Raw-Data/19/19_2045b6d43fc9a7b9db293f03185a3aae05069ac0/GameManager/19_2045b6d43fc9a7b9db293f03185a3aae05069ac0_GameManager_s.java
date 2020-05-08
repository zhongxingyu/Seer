 package org.rubenrr.walkeitor.manager;
 
 import android.util.Log;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.extension.tmx.TMXTile;
 import org.andengine.util.algorithm.path.Path;
 import org.rubenrr.walkeitor.config.ElementConfig;
 import org.rubenrr.walkeitor.config.status.StatusConfig;
 import org.rubenrr.walkeitor.config.TileConfig;
 import org.rubenrr.walkeitor.element.building.Building;
 import org.rubenrr.walkeitor.element.building.Mine;
 import org.rubenrr.walkeitor.element.consumable.Consumable;
 import org.rubenrr.walkeitor.element.resource.Resource;
 import org.rubenrr.walkeitor.element.unit.Person;
 import org.rubenrr.walkeitor.element.unit.Unit;
 import org.rubenrr.walkeitor.manager.command.Command;
 import org.rubenrr.walkeitor.manager.util.Movement;
 import org.rubenrr.walkeitor.manager.util.Storage;
 import org.rubenrr.walkeitor.manager.worker.WorkerTask;
 import org.rubenrr.walkeitor.util.AStarPathModifier;
 import org.rubenrr.walkeitor.util.TileLocatable;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * This class handles all in-game elements (units, resources and buildings)
  *
  * User: Ruben Rubio Rey
  * Date: 7/04/13
  * Time: 5:19 PM
  */
 public class GameManager {
 
     private static GameManager instance = null;
 
     public static GameManager getInstance() {
         if (instance == null) {
             synchronized(GameManager.class) {
                 if (instance == null) {
                     instance = new GameManager();
                 }
             }
         }
         return instance;
     }
 
     // Manages the tasks for the workers
     private WorkerTask workertask;
 
     private GameManager() {
         this.workertask = new WorkerTask();
     }
 
     // Current scene status in the game
     private StatusConfig status = StatusConfig.READY;
 
     // get the information about the elements that exists in the scene
     // exclude elements that are not constructed / built
     private List<Building> buildings = new ArrayList<Building>();
     private List<Resource> resources = new ArrayList<Resource>();
     private List<Unit> units = new ArrayList<Unit>();
 
     public void addBuilding(final Building building) {
         this.buildings.add(building);
     }
 
     public void addResource(final Resource resource) {
         this.resources.add(resource);
     }
 
     /**
      * Get the building that contains the specified amount of consumable,
      * or the building who is closed to the given amount of consumable
      *
      * @param consumable
      */
     public Building getBuildingThatContains(Consumable consumable) {
         Log.d("getBuildingThatContains/Command","Searching building for " + consumable.toString());
         int lastMatch = 0;
         Building buildingFound = null;
 
         for (Building building: this.buildings) {
             Storage storage = building.getStorage();
             if (storage != null) {
                 int match = building.getStorage().contains(consumable);
                 if (match > lastMatch) {
                     lastMatch = match;
                     buildingFound = building;
 
                     if (match == 100) {
                         break;
                     }
                 }
             } else {
                 Log.w("GameManager/getBuildingThatContains/Command",  "Building has no storage" + building.toString());
             }
         }
 
         Log.d("getBuildingThatContains/Command","Results, matched " + lastMatch + "  for building " + buildingFound);
 
         return buildingFound;
     }
 
     /**
      * Manages the new units in the game
      *
      * @param unit
      */
     public void addUnit(final Unit unit) {
 
         // If the unit in the game is a Worker, it is a new idle one
         if (unit.getElementConfig().equals(ElementConfig.UNIT_WORKER)) {
             this.addIdleWorker((Person)unit);
         }
 
         this.units.add(unit);
     }
 
     public List<Building> getBuildings() {
         return this.buildings;
     }
 
     public void removeBuilding(Building building) {
         this.buildings.remove(building);
         SceneManager.getInstance().detachChild(building);
     }
 
     /**
      * Check if the mine collides with the resource, therefore we know over what
      * resource is linked
      *
      * @param mine
      * @param elementConfig
      */
     public Resource getResourceColidesWithMine(Mine mine, ElementConfig elementConfig) {
         Resource matched = null;
         for (Resource resource: this.resources ) {
             if (resource.collidesWith(mine) && resource.getElementConfig().equals(elementConfig)) {
                 if (matched == null) {
                     matched = resource;
                 }
             }
         }
         return matched;
     }
 
     /**
      * Get resources filtering by subtype
      *
      * TODO filter by subtype
      *
      * @param subtype
      * @return
      */
     public List<TileLocatable> getResourcesBySubtype(String subtype) {
 
         List<TileLocatable> resourceSubtype = new ArrayList<TileLocatable>();
 
         for (Resource resource: this.resources ) {
             if (resource.getSubtype().equals(subtype)) {
                 resourceSubtype.add(resource);
             }
         }
 
         return resourceSubtype;
     }
 
     // get the information about the selected elements in the scene
     private ArrayList<Unit> unitsSelected = new ArrayList<Unit>();
 
     /**
      * Unselect all selected units
      */
     public void unselectUnits() {
         for (Sprite unit : unitsSelected ) {
 
             // remove fade
             unit.setAlpha(1f);
         }
         unitsSelected.clear();
     }
 
 
     public void selectUnit(final Unit unit) {
 
         // so far we can just select one unit, therefore we could unselect everything and keep going!
         this.unselectUnits();
 
         this.status = StatusConfig.UNIT_SELECTED;
         //unit.this
         Log.d("GameManager", "Unit selected: " + unit.toString());
 
         // set element look and feel "selected" fading it out
         unit.setAlpha(0.30f);
 
         // set the element "selected"
         this.unitsSelected.add(unit);
 
 
     }
 
     /**
      * Get the game status
      * @return
      */
     public StatusConfig getStatus() {
         return status;
     }
 
     /**
      * Default movement function. It suppose that we are trying to move
      * units that are selected
      *
      * @param posX
      * @param posY
      */
     public void moveTo(float posX, float posY) {
 
         // we will only move if there are units selected
         if (this.status.equals(StatusConfig.UNIT_SELECTED)) {
             // Temporarily simple algorithm to move units.
             // TODO get speed based on distance and unit capabilities
             // TODO Several unit must support formation
 
             this.moveUnitsToCoordinates(this.unitsSelected, this.getObstacles(), posX, posY, null);
 
             this.unselectUnits();
 
         }
 
     }
 
     private List<TileLocatable> getObstacles() {
         List<TileLocatable> buildingLocatables = new ArrayList<TileLocatable>();
         for (Building building : this.buildings) {
             buildingLocatables.add(building);
         }
         return buildingLocatables;
     }
 
     private void moveUnitToCoordinates(Unit unit, List<TileLocatable> obstacles, float posX, float posY, AStarPathModifier.IAStarPathModifierListener pathModifierListener) {
         List<Unit> units = new ArrayList<Unit>();
         units.add(unit);
         this.moveUnitsToCoordinates(units, obstacles, posX, posY, pathModifierListener);
     }
 
     private void moveUnitsToCoordinates(List<Unit> units, List<TileLocatable> obstacles, float posX, float posY, AStarPathModifier.IAStarPathModifierListener pathModifierListener) {
         final int[] tileDimensions = TileConfig.TILE_SIZE.getTileDimensions();
        for (Unit unit : unitsSelected ) {
             unit.clearMenu();
             Path path = Movement.generatePath(unit, obstacles, posX,  posY);
             if (path != null ) { // TODO Better of to be handled with exceptions ?
                 unit.registerEntityModifier(new AStarPathModifier(2, path, tileDimensions, pathModifierListener ));
             } else {
                 Log.w("Movement", "Unable to generate path to destination");
             }
         }
     }
 
     /**
      * Move Unit to the given TileLocatable
      *
      * @param unit
      * @param tileLocatable
      */
     public void moveTo(final Unit unit, final TileLocatable tileLocatable) {
         this.moveTo(unit, tileLocatable, null);
 
     }
 
     public void moveTo(final Unit unit, final TileLocatable tileLocatable, final AStarPathModifier.IAStarPathModifierListener pathModifierListener) {
        final float x = tileLocatable.getTileColumn() * tileLocatable.getColumnTileSize();
        final float y = tileLocatable.getTileRow() * tileLocatable.getRowTileSize();
        Log.d("MoveTo", "Requested to unit " + unit.toString() + "to move to (" + x + "," + y + ")");
        // TODO THIS DOES NOT WORK, COMMAND IS EXECUTED BUT UNIT IS NOT MOVED. LOOKING FOR THE ISSUE
         this.moveUnitToCoordinates(unit, this.getObstacles(), x, y, pathModifierListener);
     }
 
 
     /**
      * Starts the construction process of a building
      * @param elementConfig
      */
     public void constructBuilding(ElementConfig elementConfig) {
         if ( ! elementConfig.getCategory().equals("building")) {
             throw new IllegalArgumentException("ElemnentConfig should be a building");
         }
 
         ElementConfig dependence = elementConfig.getDependence();
         if (  dependence != null ) {
 
         }
 
     }
 
     //
     // Methods delegated for WorkerTask
     //
     public void addIdleWorker(Person worker) {
         workertask.addIdleWorker(worker);
     }
 
     public void removeIdleWorker(Person worker) {
         workertask.removeIdleWorker(worker);
     }
 
     /**
      * New task arrives.
      * The population of workers should fix it.
      *
      * @param command
      */
     public void addTask(Command command) {
         workertask.addTask(command);
     }
     //
     // / Methods delegated for WorkerTask
     //
 
 
 }
