 package org.aimas.craftingquest.user;
 
 import java.util.HashMap;
 
 import org.aimas.craftingquest.state.BasicUnit;
 import org.aimas.craftingquest.state.Blueprint;
 import org.aimas.craftingquest.state.ICarriable;
 import org.aimas.craftingquest.state.PlayerState;
 import org.aimas.craftingquest.state.Point2i;
 import org.aimas.craftingquest.state.UnitState;
import org.aimas.craftingquest.state.objects.ICrafted;
 import org.aimas.craftingquest.state.objects.IEquippable;
 import org.aimas.craftingquest.state.resources.ResourceType;
 
 
 public interface IPlayerActions {
 
     public void addArtificialIntelligence(IPlayerHooks usercode);
 
     /**
      * Returns the current locally stored player state.
      * @return the locally stored player state that is the result of the last performed transition
      */
     public PlayerState getPlayerState();
 
     /**
      * Returns the server stored player state. It might be useful to call this method at the beginning
      * of a turn since tower drains are always performed then. The method thus allows a player to get an 
      * update on the energy levels of his units and on the remaining strength of his towers right at 
      * the start of a round.
      * @return the player state as seen by the server application at the moment of inquiry or null if
      * the call is made outside the player's turn
      */
     public PlayerState requestState();
     
     /**
 	 * Allows a unit to move to an adjacent cell.
 	 * <p>If the transition is successful, the returned player state will 
 	 * have the specified unit moved to the required position. </p>
 	 * <p>In case of an error, the returned player state will not be different from the current one. 
 	 * It will also contain a <code>TransitionResult</code> which gives the reason for the failure.</p>
 	 * @param unit   the unit to be moved
 	 * @param newPosition - the movement target position
 	 * @return the new player state or null if the player attempts to move outside his turn. 
 	 */
     public PlayerState move(UnitState unit, Point2i newPosition);
 
     /**
 	 * Allows the given unit to dig for resources in the current cell.
 	 * <p> If successful, the {@link UnitState} of the unit performing the action
 	 *  will have its <code>currentCellResources</code> updated with the resources 
 	 *  contained in the currentCell. </p>
 	 * <p>In case of an error, the returned player state will not be different from the current one. 
 	 * It will also contain a <code>TransitionResult</code> which gives the reason for the failure.</p>
 	 * @param unit   the unit performing the digging operation
 	 * @return the new player state or null if the player attempts to move outside his turn.
 	 */
     public PlayerState dig(UnitState unit);
     
     
     /**
 	 * Allows the given unit to pick up the resources contained in the current cell.
 	 * <p> If successful, the {@link UnitState} of the unit performing the action will have
 	 * the specified resources added to its <code>carriedResources</code> field.</p>
 	 * <p>In case of an error, the returned player state will not be different from the current one. 
 	 * It will also contain a <code>TransitionResult</code> which gives the reason for the failure.</p>
 	 * @param unit   the unit performing the operation
 	 * @param desiredResources   the desired resources and their quantity
 	 * @return the new player state or null if the player attempts to move outside his turn.
 	 */
     public PlayerState pickupResources(UnitState unit, HashMap<ResourceType, Integer> desiredResources);
     
     /**
 	 * Allows the given unit to drop the specified quantities of basic resources.
 	 * <p> If successful, the {@link UnitState} of the unit performing the action will have
 	 * the specified resource quantities subtracted from its <code>carriedResources</code> field.</p>
 	 * <p> The dropped resources will now become visible to other units that pass near the current cell</p>
 	 * <p>In case of an error, the returned player state will not be different from the current one. 
 	 * It will also contain a <code>TransitionResult</code> which gives the reason for the failure.</p>
 	 * @param unit   the unit executing the drop action
 	 * @param unwantedResources   the unwanted resources and their quantities
 	 * @return the new player state or null if the player attempts to move outside his turn.
 	 */
     public PlayerState pickupObjects(UnitState unit, HashMap<ICarriable, Integer> desiredObjects);
     
     /**
 	 * Allows the given unit to drop the specified quantities of crafted objects.
 	 * <p> If successful, the {@link UnitState} of the unit performing the action will have
 	 * the specified object quantities subtracted from its <code>carriedObjects</code> field.</p>
 	 * <p> The dropped artifacts will now become visible to other units that pass near the current cell</p>
 	 * <p>In case of an error, the returned player state will not be different from the current one. 
 	 * It will also contain a <code>TransitionResult</code> which gives the reason for the failure.</p>
 	 * @param unit   the unit executing the drop action
 	 * @param unwantedResources   the unwanted objects and their quantities
 	 * @return the new player state or null if the player attempts to move outside his turn.
 	 */
     public PlayerState dropResources(UnitState unit, HashMap<ResourceType, Integer> unwantedResources);
     
     /**
 	 * Allows the given unit to drop the specified quantities of crafted objects.
 	 * <p> If successful, the {@link UnitState} of the unit performing the action will have
 	 * the specified object quantities subtracted from its <code>carriedObjects</code> field.</p>
 	 * <p> The dropped artifacts will now become visible to other units that pass near the current cell</p>
 	 * <p>In case of an error, the returned player state will not be different from the current one. 
 	 * It will also contain a <code>TransitionResult</code> which gives the reason for the failure.</p>
 	 * @param unit   the unit executing the drop action
 	 * @param unwantedResources   the unwanted objects and their quantities
 	 * @return the new player state or null if the player attempts to move outside his turn.
 	 */
     public PlayerState dropObjects(UnitState unit, HashMap<ICarriable, Integer> unwantedObjects);
     
     /**
 	 * Allows the given unit to craft the target object using the specified ingredients. 
 	 * <p> If successful, the target object will be added to the unit's <code>carriedObjects</code> field.
 	 * Also, the unit performing the action will have the ingredient quantities 
 	 * subtracted from its <code>carriedResources</code> / <code>carriedObjects</code> field.</p>
 	 * <p>In case of an error, the returned player state will not be different from the current one. 
 	 * It will also contain a <code>TransitionResult</code> which gives the reason for the failure.</p>
 	 * @param unit   the unit performing the crafting action
 	 * @param blueprint  the blueprint used to craft something
 	 * @return the new player state or null if the player attempts to craft something wrong.
 	 */
     public PlayerState craftObject(UnitState unit, Blueprint blueprint);
     
     /**
  	 * Allows the given unit to equip the target equippable object. 
  	 * <p> If successful, the target object, a sword or an armour, will be equiped.
  	 * <p>In case of an error, the returned player state will not be different from the current one. 
  	 * It will also contain a <code>TransitionResult</code> which gives the reason for the failure.</p>
  	 * @param unit   the unit performing the crafting action
  	 * @param target   the armour / sword that is supposed to be equiped
 	 * @return the new player state or null if the player attempts to equip an invalid object.
  	 */
 	public PlayerState equip(UnitState unit, IEquippable target);
 	
 	/**
  	 * Allows the given unit to place a trap in the current cell. 
  	 * <p> If successful, the target trap will be placed in the current cell.
  	 * <p>In case of an error, the returned player state will not be different from the current one. 
  	 * It will also contain a <code>TransitionResult</code> which gives the reason for the failure.</p>
  	 * @param unit   the unit performing the crafting action
  	 * @param target   the trap that is supposed to be placed in the current cell
 	 * @return the new player state or null if the trap placement fails.
  	 */
 	public PlayerState placeTrap(UnitState unit, Blueprint blueprint);
 
     
     /**
 	 * Allows the given unit to place a tower in its current position.
 	 * <p> If successful, the tower will be placed in the current position of the builder unit. 
 	 * Also, the new tower will be added to the <code>availableTowers</code> field 
 	 * of the returned player state. </p>
 	 * <p>In case of an error, the returned player state will not be different from the current one. 
 	 * It will also contain a <code>TransitionResult</code> which gives the reason for the failure.</p>
 	 * @param unit   the unit building the tower
 	 * @return the new player state or null if the player attempts to move outside his turn.
 	 */
     public PlayerState placeTower(UnitState unit, Blueprint blueprint);
     
     /**
 	 * Allows the given unit to attack an enemy unit if in its vicinity in its current position 
 	 * optionally using an amount of energy points as backup strength for the attack.
 	 * <p> If successful, the unit doing the attack will use its currently equipped weapon to subtract
 	 * a number of energy points from the <code>maxEnergy</code> field of the attacked unit equivalent to the
 	 * <code>damage</code> provoked. </p>
 	 * <p> The attacked unit will retaliate with 50% of the attacker's <code>damage</code> + however much
 	 * backup energy it set for defense</p> 
 	 * <p>In case of an error, the returned player state will not be different from the current one. 
 	 * It will also contain a <code>TransitionResult</code> which gives the reason for the failure.</p>
 	 * @param unit   the unit doing the attack
 	 * @param attackedUnit  the enemy unit being attacked
 	 * @param energyBackup  the amount of energy points used to increase the damage
 	 * @return the new player state or null if the player attempts to move outside his turn.
 	 */
     public PlayerState attack(UnitState unit, BasicUnit attackedUnit, int energyBackup);
     
     /**
 	 * Performs an upgrade for the given <code>craftedObject</code> by paying <code>goldAmount</code> 
 	 * gold nuggets.
 	 * <p> If successful, the PlayerState returned will contain an additional blueprint describing the requirements
 	 * for the next level of the crafted object submitted.
 	 * <p>In case of an error, the returned player state will not be different from the current one. 
 	 * It will also contain a <code>TransitionResult</code> which gives the reason for the failure.</p>
 	 * @param unit   the unit performing the upgrade
	 * @param blueprint  the blueprint that is upgraded
 	 * @param goldAmount  the amount of gold nuggets required for the upgrade  
 	 * @return the new player state or null if the player attempts to move outside his turn.
 	 */
    public PlayerState upgrade(UnitState unit, Blueprint blueprint, int goldAmount);
     
 }
