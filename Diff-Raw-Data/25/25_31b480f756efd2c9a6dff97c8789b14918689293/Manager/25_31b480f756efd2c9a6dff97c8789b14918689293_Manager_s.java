 package vooga.rts.manager;
 
 import java.awt.Graphics2D;
 import java.awt.Shape;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Queue;
 import vooga.rts.action.Action;
 import vooga.rts.action.IActOn;
 import vooga.rts.commands.ClickCommand;
 import vooga.rts.commands.Command;
 import vooga.rts.commands.DragCommand;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.InteractiveEntity;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.buildings.Building;
 import vooga.rts.gamedesign.state.DetectableState;
 import vooga.rts.gamedesign.state.MovementState;
 import vooga.rts.gamedesign.state.OccupyState;
 import vooga.rts.manager.actions.DragSelectAction;
 import vooga.rts.manager.actions.LeftClickAction;
 import vooga.rts.manager.actions.RightClickAction;
 import vooga.rts.state.GameState;
 import vooga.rts.state.State;
 import vooga.rts.util.Location3D;
 
 
 /**
  * The Manager class is responsible for managing all of the units and buildings
  * that each player controls. Commands are passed through to the Manager
  * and the appropriate actions are executed on the selected units or even the
  * manager.
  * 
  * @author Jonathan Schmidt
  * @author Challen Herzberg-Brovold
  * 
  */
 
 public class Manager extends Observable implements State, IActOn, Observer {
 
     private List<InteractiveEntity> myEntities;
     private List<InteractiveEntity> mySelectedEntities;
     private Map<Integer, List<InteractiveEntity>> myGroups;
     private boolean myMultiSelect;
     private Map<String, Action> myActions;
 
     private Queue<InteractiveEntity> myAddQueue;
     private int myPlayer;
 
     Iterator<InteractiveEntity> myUpdateIterator;
 
     public Manager (int playerID) {
     	myPlayer = playerID;
         myEntities = new ArrayList<InteractiveEntity>();
         mySelectedEntities = new ArrayList<InteractiveEntity>();
         myGroups = new HashMap<Integer, List<InteractiveEntity>>();
         myMultiSelect = false;
         myActions = new HashMap<String, Action>();
         myAddQueue = new LinkedList<InteractiveEntity>();
         addActions();
     }
 
     @Override
     public void paint (Graphics2D pen) {
         for (InteractiveEntity u : myEntities) {
             u.paint(pen);
         }
     }
 
     @Override
     public void update (double elapsedTime) {
         myEntities.addAll(myAddQueue);
         myAddQueue.clear();
         myUpdateIterator = myEntities.iterator();
         while (myUpdateIterator.hasNext()) {
             InteractiveEntity u = myUpdateIterator.next();
             u.update(elapsedTime);
         }
         myUpdateIterator = null;
     }
 
     @Override
     public void receiveCommand (Command command) {
         updateAction(command);
     }
 
     /**
      * Checks to see if the manager can handle the command, if not sends it to
      * the selected entities.
      */
     @Override
     public void updateAction (Command command) {
         if (myActions.containsKey(command.getMethodName())) {
             Action current = myActions.get(command.getMethodName());
             current.update(command);
             current.apply();
         }
         else {
             applyAction(command);
         }
     }
 
     @Override
     public void addAction (String input, Action action) {
         myActions.put(input, action);

     }
 
     /**
      * Retrieves the appropriate action for all the selected entities which
      * can handle the input, then applies the action.
      * 
      * @param command input used to retrieve the appropriate command
      */
     public void applyAction (Command command) {
         Iterator<InteractiveEntity> it = mySelectedEntities.iterator();
         while (it.hasNext()) {
             InteractiveEntity u = it.next();
             if (u.containsInput(command)) {
                 u.updateAction(command);
                 u.getAction(command).apply();
             }
         }
     }
 
     /**
      * Adds an entity to the manager. This will be done when a new entity is
      * created.
      * 
      * @param u
      *        The entity that is to be added.
      */
     public void add (InteractiveEntity entity) {
     	entity.setPlayerID(myPlayer);
         entity.addObserver(GameState.getMap().getNodeMap());
         entity.addObserver(this);
         entity.setChanged();
         entity.notifyObservers(entity.getWorldLocation());
         myAddQueue.add(entity);
     }
 
     public void remove (InteractiveEntity entity) {
         if (myUpdateIterator != null) {
             myUpdateIterator.remove();
         }
         else {
             myEntities.remove(entity);
         }
         entity.deleteObserver(this);
         mySelectedEntities.remove(entity);
     }
 
     /**
     * Deselects the topmost unit at the given location.s
      * 
      * @param location at which to deselect the unit.
      */
     public void deselect (Location3D location) {
         for (int i = getAllEntities().size() - 1; i >= 0; i--) {
             InteractiveEntity ie = getAllEntities().get(i);
             if (ie.intersects(location)) {
                 deselect(ie);
                 return;
             }
         }
         deselectAll();
     }
 
     private void notifyDeselect () {
         setChanged();
         notifyObservers(false);
     }
 
     /**
      * Deselects the specified entity.
      * 
      * @param u The entity to deselect
      */
     public void deselect (InteractiveEntity ie) {
         if (mySelectedEntities.contains(ie)) {
             mySelectedEntities.remove(ie);
             ie.select(false);
         }
     }
 
     /**
      * Deselects all selected entities.
      */
     public void deselectAll () {
         notifyDeselect();
 
         if (myMultiSelect) {
             return;
         }
         for (InteractiveEntity ie : mySelectedEntities) {
             ie.select(false);
         }
         mySelectedEntities.clear();
     }
 
     /**
      * Returns the list of all the entities in the manager.
      * 
      * @return List of all entities
      */
     public List<InteractiveEntity> getAllEntities () {
         return myEntities;
     }
 
     /**
      * Sets the entities to a specified list of entities
      * 
      * @param entityList
      */
     public void setAllEntities (List<InteractiveEntity> entityList) {
         myEntities = entityList;
     }
 
     /**
      * Returns the list of selected entities.
      * 
      * @return The selected entities
      */
     public List<InteractiveEntity> getSelected () {
         return mySelectedEntities;
     }
 
     /**
      * Groups the currently selected entities together with a specified group ID
      * 
      * @param groupID
      *        The ID of the group
      */
     public void group (int groupID) {
         myGroups.put(groupID, new ArrayList<InteractiveEntity>(mySelectedEntities));
     }
 
     /**
      * Selects a specific entity and marks it as selected.
      * 
      * @param entity
      */
     public void select (InteractiveEntity entity) {
         deselectAll();
         if (!mySelectedEntities.contains(entity)) {
             if (myEntities.contains(entity)) {
                 if (entity.select(true)) {
                     mySelectedEntities.add(entity);
                 }
             }
         }
         notifySelect();
     }
 
     public void notifySelect () {
         setChanged();
         notifyObservers(true);
     }
 
     /**
      * Selects the top most interactive entity that is underneath the provided
      * Point location. This is used for selecting entities by mouse click.
      * 
      * @param loc
      */
     public void select (Location3D loc) {
         deselectAll();
         for (int i = getAllEntities().size() - 1; i >= 0; i--) {
             InteractiveEntity ie = getAllEntities().get(i);
             if (ie.intersects(loc)) {
                 select(ie);
                 return;
             }
         }
     }
 
     /**
      * Selects all the entities in provided rectangle. Allows a user to drag
      * around the desired entities.
      * 
      * @param area
      *        The area to select the entities in.
      */
     public void select (Shape area) {
         deselectAll();
         boolean multi = myMultiSelect;
         setMultiSelect(true);
         for (InteractiveEntity ie : myEntities) {
             if (area.intersects(ie.getBounds()) || area.contains(ie.getBounds())) {
                 select(ie);
             }
         }
         setMultiSelect(multi);
     }
 
     /**
      * Sets the Manager into multi select mode which allows the user to select
      * more than one entity at a time.
      * 
      * @param val
      *        whether it is multi select or not
      */
     public void setMultiSelect (boolean val) {
         myMultiSelect = val;
     }
 
     public void addActions () {
         addAction(DragCommand.DRAG, new DragSelectAction(this));
         addAction(ClickCommand.LEFT_CLICK, new LeftClickAction(this));
         addAction(ClickCommand.RIGHT_CLICK, new RightClickAction(this));
     }
 
     /**
      * Activates a previously create group of entities.
      * 
      * @param groupID
      *        The ID of the group to select
      */
     public void activateGroup (int groupID) {
         if (myGroups.containsKey(groupID)) {
             mySelectedEntities = new ArrayList<InteractiveEntity>(myGroups.get(groupID));
         }
     }
 
     /**
      * Finds the InteractiveEntity in myEntities based on its hash code.
      * 
      * @param hashCode the hash code of the entity that is looking for
      * @return the index of the entity with the hashCode
      */
     private int findEntityWithHashCode (int hashCode) {
         for (int i = 0; i < myEntities.size(); ++i) {
             if (myEntities.get(i).hashCode() == hashCode) {
                 return i;
             }
         }
         return -1;
     }
 
     @Override
     public void update (Observable entity, Object state) {
         if (entity instanceof InteractiveEntity) {
             InteractiveEntity ie = (InteractiveEntity) entity;
             if (ie.isDead()) {
                 remove(ie);
             }
         }
 
         // While Shepherds watch their flocks by night.
         if (state instanceof InteractiveEntity) {
             InteractiveEntity sent = (InteractiveEntity) state;
             if (!myEntities.contains(sent)) {
                 add(sent);
             }
         }
         else 
         {
             if (state instanceof Integer) {
                 int index = findEntityWithHashCode((Integer) state);
                 InteractiveEntity unit = myEntities.get(index);
                 unit.getEntityState().setOccupyState(OccupyState.NOT_OCCUPYING);
                 unit.setVisible(true);
 
                 unit.setWorldLocation((((Building) entity).getRallyPoint()));
                 unit.move((((Building) entity).getRallyPoint()));
                 myEntities.get(index).stopMoving();
                 unit.getEntityState().setMovementState(MovementState.STATIONARY);
             }
         }
 
     }
 }
