 /**
  * 
  */
 package effect;
 
 import game.Actor;
 import game.Player;
 import item.Teleporter;
 
 /**
  * Represent the effect of a teleporter on a player.
  * 
  * @invar	A teleporter teleports to another teleporter.
  * 			| destination instanceof Teleporter && destination != null
  * @invar	When a actor steps on a teleporter, it is teleported to the destination,
  * 			unless the destination has an obstacle or other player.
  * 			| if destination has no obstacle or player
  * 			|	actor.teleport(destination)
  * @invar	This effect cannot combine with another effect.
  * 			| canCombineWith(effect) == false
  * 
  * @author 	Groep 8
  * 
  * @version April 2013
  */
 public class TeleporterEffect extends Effect {
 	
 	/**
 	 * This is the destination for the teleporter.
 	 */
 	private Teleporter destination;
 	
 	/**
 	 * Return the index of this effect.
 	 * This has influence on the order of executing effects.
 	 * 
 	 * @return	The order of this effect.
 	 */
 	public int getIndex() {
 		return 2;
 	}
 	
 	/**
 	 * This is the constructor for the TeleporterEffect.
 	 * 
 	 * @param 	destination
 	 * 			This is the destination for the teleporter.
 	 * 
 	 * @effect	The destination to where the actor teleports is set.
 	 * 			|setDestination(destination)
 	 */
 	public TeleporterEffect(Teleporter destination)
 	{
 		setDestination(destination);
 	}
 	
 	/**
 	 * This method returns the destination of the teleporter.
 	 * 
 	 * @return 	destination
 	 * 			The destination of this teleporter.
 	 */
 	public Teleporter getDestination() {
 		return destination;
 	}
 
 	/**
 	 * This method sets the destination of the TeleporterEffect.
 	 * 
 	 * @param 	destination
 	 * 			This is the teleporter to which the Actor will be teleported on stepping on this TeleporterEffect.
 	 * 
 	 * @effect 	The destination is set.
 	 * 			|this.destination = destination
 	 */
 	private void setDestination(Teleporter destination) {
 		this.destination = destination;
 	}
 
 
 	/**
 	 * This method specifies what happens when an Actor starts on a teleporter.
 	 * 
 	 * @param	actor
 	 * 			This is the Actor starting on the teleporter.
 	 */
 	@Override
 	public void onStart(Actor actor) {}
 
 	/**
 	 * This method specifies what happens when an Actor steps on a teleporter.
 	 * 
 	 * @param 	actor
 	 * 			This is the Actor stepping on the teleporter.
 	 * 
 	 * @effect	The actor is teleported to the destination.
 	 * 			|actor.teleport(getDestination())
 	 * @effect	If the actor is a player, he drops his flag.
 	 * 			|if(actor instanceof Player)
 	 * 			|	dropFlag((Player)actor)
 	 */
 	@Override
 	public void onStep(Actor actor) {
		actor.teleport(getDestination());
 		if(actor instanceof Player)
 			dropFlag((Player)actor);
 	}
 
 	/**
 	 * This method specifies what happens when an Actor lands on a teleporter.
 	 * 
 	 * @param 	actor
 	 * 			This is the Actor landing on the teleporter.
 	 */
 	@Override
 	public void onLand(Actor actor) {}
 
 	/**
 	 * This method specifies what happens when an Actor leaves a teleporter.
 	 * 
 	 * @param 	actor
 	 * 			This is the Actor leaving the teleporter.
 	 */
 	@Override
 	public void onLeave(Actor actor) {}
 
 	/**
 	 * Link the given effect to this effect.
 	 * 
 	 * @param	effect
 	 * 			The effect that has to be linked.
 	 */
 	@Override
 	public void linkEffect(Effect effect) {}
 
 	/**
 	 * Unlink  the given effect from this effect.
 	 * 
 	 * @param	effect
 	 * 			The effect that has to be unlinked.
 	 * 
 	 * @effect	If the given effect is a TeleporterEffect, this effect is removed from the square.
 	 * 			|if(effect instanceof TeleporterEffect) 
 	 * 			|	getSquare().removeEffect(this)
 	 */
 	@Override
 	public void unlinkEffect(Effect effect) {
 		if(effect instanceof TeleporterEffect)
 			getSquare().removeEffect(this);
 	}
 
 	/**
 	 * Checks if the given effect can be combined with this effect.
 	 * 
 	 * @param	effect
 	 * 			The effect that we want to combine.
 	 * 
 	 * @return	false
 	 */
 	@Override
 	public boolean canCombineWith(Effect effect) {
 		return false;
 	}
 
 }
