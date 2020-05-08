 package org.aimas.craftingquest.core;
 
 import gnu.cajo.invoke.Remote;
 
 import java.util.HashMap;
 
 import org.aimas.craftingquest.state.Blueprint;
 import org.aimas.craftingquest.state.ICarriable;
 import org.aimas.craftingquest.state.PlayerState;
 import org.aimas.craftingquest.state.Point2i;
 import org.aimas.craftingquest.state.Transition;
 import org.aimas.craftingquest.state.Transition.ActionType;
 import org.aimas.craftingquest.state.UnitState;
 import org.aimas.craftingquest.state.objects.IEquippable;
 import org.aimas.craftingquest.state.resources.ResourceType;
 import org.aimas.craftingquest.user.IPlayerActions;
 import org.aimas.craftingquest.user.IPlayerHooks;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 public final class Client0 implements IClient, IPlayerActions {
 
 	/* communication */
 	private int id;
 	private long secret;
 	private Object server;
 	private Remote client;
 
 	/* action sync */
 	private Boolean actionSync = new Boolean(true);
 	
 	/* game */
 	private PlayerState state;
 	private IPlayerHooks player;
 	private PausableThreadPoolExecutor ptpe;
 
 	/* logging */
 	private static Logger logger = Logger.getLogger(Client0.class); 
 	
 	/* public */
 	public Client0(String host, int port, String serverName, long secret) throws Exception {
 		this.secret = secret;
 		ptpe = new PausableThreadPoolExecutor();
 		
 		server = Remote.getItem("//" + host + ":" + port + "/" + serverName);
 		client = new Remote(this);
 		
 		id = (Integer) Remote.invoke(server, "addRemoteClient", client);
 		PropertyConfigurator.configure("logging.properties");
 	}
 
 	/* communication */
 	@Override
 	final public Long getSecret(int n) {
 		return secret;
 	}
 
 	@Override
 	final public void onEvent(Event event) {
 		logger.info("[client][onEvent][" + event.type.name() + "]");
 		switch (event.type) {
 		case Nothing:
 			break;
 		case Initialization:
 			if (player != null) {
 				ptpe.execute(new Runnable() {
 					public void run() {
 						player.initGame();
 						player.initPlayer();
 					}
 				});
 			}
 		case NewRound:
 			synchronized(actionSync) {
 				actionSync = true;
 				if (player != null) {
 					ptpe.execute(new Runnable() {
 						public void run() {
 							player.beginRound();
 						}
 					});
 				}
 			}
 			
 			break;
 		case EndRound:
 			synchronized(actionSync) {
 				actionSync = false;
 			}
 			
 			break;
 		case GameEnd:
 			if (player != null) {
 				ptpe.execute(new Runnable() {
 					public void run() {
 						player.finishGame();
 					}
 				});
 			}
 			break;
 		default:
 			break;
 		}
 	}
 
 	/* interface */
 	public void addArtificialIntelligence(IPlayerHooks usercode) {
 		// assume object was initalized do a check
 		this.player = usercode;
 		this.state = doGenericAction(new Transition(Transition.ActionType.PlayerReady, null));
 	}
 	
 	public PlayerState getPlayerState() {
 		return state;
 	}
 
 	private synchronized PlayerState doGenericAction(Transition action) {
 		try {
 			synchronized(actionSync) {
 				if (actionSync) {
 					action.secret = secret;
 					
 					PlayerState responseState = (PlayerState) Remote.invoke(server, "process", action);
 					if (responseState != null) {
 						this.state = responseState;
 					}
 					
 					return getPlayerState();
 				}
 				else {
 					return null;
 				}
 			}
 			
 			
 		} catch (Exception ex) {
 			logger.error("Could not send action request.", ex);
 		}
 		return null; // means grail error
 	}
 
 	/**
 	 * Returns the server stored player state. It might be useful to call this method at the beginning of 
 	 * a turn since tower drains are always performed then. The method thus allows a player to get an 
 	 * update on the energy levels of his units and on the remaining strength of his towers right at 
 	 * the start of a round.
 	 * @return the player state as seen by the server application at the moment of inquiry or null if the 
 	 * call is made outside the player's turn
 	 */
 	public PlayerState requestState() {
 		return doGenericAction(new Transition(Transition.ActionType.RequestState, null));
 	}
 	
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
 	@Override
 	public PlayerState move(UnitState unit, Point2i newPosition) {
 		return doGenericAction(new Transition(Transition.ActionType.Move, new Object[] { unit.id, newPosition }));
 	}
 
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
 	@Override
 	public PlayerState dig(UnitState unit) {
 		return doGenericAction(new Transition(ActionType.Dig, new Object[] {unit.id, unit.pos}));
 	}
 	
 	
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
 	@Override
 	public PlayerState pickupResources(UnitState unit, HashMap<ResourceType, Integer> desiredResources){
 		return doGenericAction(new Transition(ActionType.PickupResources, new Object[] {unit.id, desiredResources}));
 	}
 	
 	/**
 	 * Allows the given unit to pick up the crafted objects contained in the current cell.
 	 * Crafted objects can be picked up from a cell if a unit has previously dropped them there.
 	 * <p> If successful, the {@link UnitState} of the unit performing the action will have
 	 * the specified resources added to its <code>carriedObjects</code> field.</p>
 	 * <p>In case of an error, the returned player state will not be different from the current one. 
 	 * It will also contain a <code>TransitionResult</code> which gives the reason for the failure.</p>
 	 * @param unit   the unit performing the operation
 	 * @param desiredObjects   the desired crafted objects and their quantity
 	 * @return the new player state or null if the player attempts to move outside his turn.
 	 */
 	@Override
 	public PlayerState pickupObjects(UnitState unit, HashMap<ICarriable, Integer> desiredObjects) {
 		return doGenericAction(new Transition(ActionType.PickupObjects, new Object[] {unit.id, desiredObjects}));
 	}
 	
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
 	@Override
 	public PlayerState dropResources(UnitState unit, HashMap<ResourceType, Integer> unwantedResources) {
 		return doGenericAction(new Transition(ActionType.DropResources, new Object[] {unit.id, unwantedResources}));
 	}
 	
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
 	@Override
 	public PlayerState dropObjects(UnitState unit, HashMap<ICarriable, Integer> unwantedObjects) {
 		return doGenericAction(new Transition(ActionType.DropObjects, new Object[] {unit.id, unwantedObjects}));
 	}
 	
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
 	@Override
 	public PlayerState placeTrap(UnitState unit, Blueprint blueprint) {
 		return doGenericAction(new Transition(ActionType.PlaceTrap, new Object[] {unit.id, blueprint}));
 	}
 	
 	/**
  	 * Allows the given unit to craft the target object using the specified ingredients. 
  	 * <p> If successful, the target object will be added to the unit's <code>carriedObjects</code> field.
  	 * Also, the unit performing the action will have the ingredient quantities 
  	 * subtracted from its <code>carriedResources</code> / <code>carriedObjects</code> field.</p>
  	 * <p>In case of an error, the returned player state will not be different from the current one. 
  	 * It will also contain a <code>TransitionResult</code> which gives the reason for the failure.</p>
  	 * @param unit   the unit performing the crafting action
  	 * @param target   the artifact that is supposed to be built
  	 * @param usedObjects   the list of object ingredients, if any. The value of this parameter may be null if the target object is of a simple type 
  	 * @param usedResources   the list of basic resource ingredients, if any. The value of this parameter may be null if the target object is of a complex type
 	 * @return the new player state or null if the player attempts to move outside his turn.
  	 */
 	@Override
 	public PlayerState craftObject(UnitState unit, Blueprint blueprint) {
 		return doGenericAction(new Transition(ActionType.CraftObject, new Object[] {unit.id, blueprint}));
 	}
 
 	/**
  	 * Allows the given unit to equip the target crafted object. 
  	 * <p> If successful, the target object, a sword or an armour, will be equiped.
  	 * <p>In case of an error, the returned player state will not be different from the current one. 
  	 * It will also contain a <code>TransitionResult</code> which gives the reason for the failure.</p>
  	 * @param unit   the unit performing the crafting action
  	 * @param target   the armour / sword that is supposed to be equiped
 	 * @return the new player state or null if the player attempts to equip an invalid object.
  	 */
 	@Override
 	public PlayerState equip(UnitState unit, IEquippable target) {
 		return doGenericAction(new Transition(ActionType.Equip, new Object[] {unit.id, target}));
 	}	
 	
 	
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
 	@Override
	public PlayerState placeTower(UnitState unit) {
		return doGenericAction(new Transition(ActionType.PlaceTower, new Object[] {unit.id}));
 	}
     
 	
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
     public PlayerState attack(UnitState unit, BasicUnit attackedUnit, int energyBackup) {
     	return doGenericAction(new Transition(ActionType.Attack, new Object[] {
     			unit.id, attackedUnit.playerID, attackedUnit.unitId, energyBackup}));
     }
     
     /**
 	 * Performs an upgrade for the given <code>craftedObject</code> by paying <code>goldAmount</code> 
 	 * gold nuggets.
 	 * <p> If successful, the PlayerState returned will contain an additional blueprint describing the requirements
 	 * for the next level of the crafted object submitted.
 	 * <p>In case of an error, the returned player state will not be different from the current one. 
 	 * It will also contain a <code>TransitionResult</code> which gives the reason for the failure.</p>
 	 * @param unit   the unit performing the upgrade
 	 * @param craftedObject  the type of object for which an upgrade is desired
 	 * @param goldAmount  the amount of gold nuggets required for the upgrade  
 	 * @return the new player state or null if the player attempts to move outside his turn.
 	 */
    public PlayerState upgrade(UnitState unit, CraftedObject craftedObject, int goldAmount) {
     	return doGenericAction(new Transition(ActionType.Upgrade, new Object[] {
    			unit.id, craftedObject, goldAmount }));
     }
 }
