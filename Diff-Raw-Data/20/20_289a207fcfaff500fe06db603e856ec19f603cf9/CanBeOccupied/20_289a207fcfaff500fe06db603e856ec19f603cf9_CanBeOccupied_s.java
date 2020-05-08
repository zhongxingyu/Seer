 package vooga.rts.gamedesign.strategy.occupystrategy;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import vooga.rts.action.InteractiveAction;
 import vooga.rts.commands.Command;
 import vooga.rts.gamedesign.sprite.gamesprites.GameEntity;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.InteractiveEntity;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.units.Unit;
 import vooga.rts.gamedesign.state.DetectableState;
 import vooga.rts.gamedesign.state.MovementState;
 import vooga.rts.gamedesign.state.OccupyState;
 import vooga.rts.util.Location3D;
 
 
 /**
  * 
  * This class implements OccupyStrategy and is used as an instance in
  * InteractiveEntity for objects that can be occupied by types of Units specified.
  * 
  * @author Wenshun Liu
  * 
  */
 public class CanBeOccupied implements OccupyStrategy {
     public static final int DEFAULT_MAX_OCCUPIERS = 10;
 
     //TODO: another way to verify different types of Units? Probably names in xml?
     private List<Integer> myOccupierHashCodes;
     private int myMaxOccupiers;
     private int myOccupierID;
 
     /**
      * Creates a new occupy strategy that represents an entity that can be
      * occupied. It is created with a list of what entities can occupy it,
      * what entities are occupying it, and the max number of entities that can
      * occupy it.
      */
     public CanBeOccupied () {
         myOccupierHashCodes = new ArrayList<Integer>();
         myMaxOccupiers = DEFAULT_MAX_OCCUPIERS;
         myOccupierID = 0;
     }
 
     public void getOccupied (InteractiveEntity entity, Unit u) {
         if (myOccupierHashCodes.size() < myMaxOccupiers) {
             if (myOccupierID == 0) {
                 myOccupierID = u.getPlayerID();
             }
             myOccupierHashCodes.add(u.hashCode());
             entity.setChanged();
             u.getEntityState().setOccupyState(OccupyState.OCCUPYING);
             entity.notifyObservers(u);
         }
     }
 
     /**
      * Creates and adds occupy strategy specific actions to entity
      */
     public void createOccupyActions (final InteractiveEntity entity) {
         addDeoccupyAction(entity);
     }
 
     /**
      * Creates and adds the action, in which the occupied entity will remove
      * and return all its occupiers back to the original player.
      * 
      * @param entity the object that is occupied.
      */
     private void addDeoccupyAction (final InteractiveEntity entity) {
         entity.addAction("deoccupy", new InteractiveAction(entity) {
             @Override
             public void update (Command command) {
             }
 
             @Override
             public void apply () {
                 myOccupierID = 0;
                 Iterator<Integer> it = myOccupierHashCodes.iterator();
                 while (it.hasNext()) {
                     Integer hashCode = it.next();
                     entity.setChanged();
                     entity.notifyObservers(hashCode);
                     it.remove();
                 }
             }
         });
     }
 
     /**
      * Sets the entity's current occupier id, which represents the player id
      * that is currently occupying the entity.
      */
     public void setOccupierID (int id) {
         myOccupierID = id;
     }
 
     /**
      * Returns the entity's current occupier id, which represents the player id
      * that is currently occupying the entity.
      */
     public int getOccupierID () {
         return myOccupierID;
     }
 
     /**
      * Returns the hash code of the list of occupiers.
      */
     public List<Integer> getOccupiers () {
         return myOccupierHashCodes;
     }
 
     /**
      * Returns the max number of occupiers this entity can take.
      */
     public int getMaxOccupiers () {
         return myMaxOccupiers;
     }
 }
