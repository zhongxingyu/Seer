 package effect;
 
 import exception.InvalidMoveException;
 import item.IdentityDisk;
 import game.Actor;
 import game.Player;
 
 /**
  * Represents the effect of an ForceField on a square.
  * 
  * @invar	This effect does not combine with another effect.
  * 			| canCombinewith(Effect) == false
 * @invar	When a player starts on a ForceFieldEffect, he cannot move.
  * @invar	When a identity disk hits a ForceFieldEffect, it is destroyed.
  * 			| IdentityDisk.terminate()
  * 
  * @author 	Groep 8
  * 
  * @version April 2013
  */
 public class ForceFieldEffect extends Effect {
 
 	/**
 	 * Executes what happens when you start on a square with a force field effect.
 	 * 
 	 * @param	Actor
 	 * 			The Actor starting on this effect.
 	 */
 	@Override
 	public void onStart(Actor actor) {}
 
 	/**
 	 * Executes what happens when an actor step on a square with a force field effect.
 	 * 
 	 * @param	Actor
 	 * 			The Actor stepping on this effect.
 	 * 
 	 * @effect	When the actor is a identity disk and hits a force field, the identity disk is destroyed.
 	 * 			| if(actor instanceof IdentityDisk)
 	 * 			| 	actor.terminate()
 	 */
 	@Override
 	public void onStep(Actor actor) {
 		if(actor instanceof IdentityDisk){
 			((IdentityDisk) actor).terminate();
 		}
 
 	}
 
 	/**
 	 * Executes what happens when an actor lands on a square with a force field effect.
 	 * 
 	 * @param	Actor
 	 * 			The Actor stepping on this effect.
 	 * 
 	 * @effect	When the actor is a identity disk and hits a force field, the identity disk is destroyed.
 	 * 			| if(actor instanceof IdentityDisk)
 	 * 			| 	actor.terminate()
 	 */
 	@Override
 	public void onLand(Actor actor) {
 		if(actor instanceof IdentityDisk){
 			((IdentityDisk) actor).terminate();
 		}
 	}
 
 	/**
 	 * Executes what happens when an actor leaves a square with a force field effect.
 	 * 
 	 * @param	Actor
 	 * 			The Actor leaving this effect.
 	 * 
 	 * @effect	When the actor is a player and leaves a force field effect, the player cannot move.
 	 * 			| if(actor instanceof Player)
 	 * 			| 	throw new InvalidMoveException()
 	 * 
 	 * @throws	InvalidMoveException
 	 * 			This is thrown when a player tries to leave a active force field.
 	 */
 	@Override
 	public void onLeave(Actor actor) throws InvalidMoveException {
 		if(actor instanceof Player){
 			throw new InvalidMoveException();
 		}
 
 	}
 
 	/**
 	 * Return the index of this effect.
 	 * This has influence on the order of executing effects.
 	 */
 	@Override
 	public int getIndex() {
 		return 3;
 	}
 
 	/**
 	 * Link the given effect to this Force field effect.
 	 * 
 	 * @param	effect
 	 * 			This is the effect we want to link.
 	 */
 	@Override
 	public void linkEffect(Effect effect) {}
 
 	/**
 	 * Unlink the given effect from this ForceFieldEffect.
 	 * 
 	 * @param 	effect
 	 * 			This is the effect we want to unlink.
 	 * 
 	 * @effect 	If the given effect is a ForceFieldEffect, 
 	 * 			this ForceFieldEffect is removed as the effect at its location.
 	 * 			|if(effect instanceof ForceFieldEffect)
 	 * 			|	getSquare().removeEffect(this)
 	 */
 	@Override
 	public void unlinkEffect(Effect effect) {
 		if(effect instanceof ForceFieldEffect) {
 			getSquare().removeEffect(this);
 		}
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
