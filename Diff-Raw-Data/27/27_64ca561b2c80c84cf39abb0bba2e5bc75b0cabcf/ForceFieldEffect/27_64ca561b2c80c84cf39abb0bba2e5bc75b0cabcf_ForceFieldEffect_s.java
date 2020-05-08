 package effect;
 
 import game.Actor;
 import item.IdentityDisk;
 
 /**
  * Represents the effect of an ForceField on a square.
  * 
  * @invar	This effect does not combine with another effect.
  * 			| canCombinewith(Effect) == false
  * @invar	When a identity disk hits a ForceFieldEffect, it is destroyed.
  * 			| IdentityDisk.terminate()
  * 
  * @author 	Groep 8
  * 
  * @version April 2013
  */
 public class ForceFieldEffect extends Effect {
 
 	/**
 	 * Return whether this effect must be executed
 	 * before the given effect.
 	 */
 	@Override
 	public boolean isPriorTo(Effect effect) {
 		return (effect instanceof TeleporterEffect);
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
 	 * Executes what happens when you start on a square with a force field effect.
 	 * 
 	 * @param	Actor
 	 * 			The Actor starting on this effect.
 	 */
 	@Override
 	public void onStart(Actor actor) {
 		if(actor instanceof IdentityDisk){
 			((IdentityDisk) actor).terminate();
 		}
 	}
 
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
 	 */
 	@Override
 	public void onLeave(Actor actor){}
 
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
