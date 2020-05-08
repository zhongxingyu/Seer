 package effect;
 
 import item.IdentityDisk;
 import item.Item;
 import item.PowerFailure;
 import game.Actor;
 import game.Player;
 
 /**
  * Represents the effect on a square with a power failure.
  * 
  * @invar	This effect can be combined with light grenades.
  * @invar	When a player steps on a power failure, he loses his turn.
  * 			| getLocation().getPlayer().loseTurn()
  * @invar	When a player starts on a power failure, he loses an action.
  * 			| DAMAGE = 1
  * 
  * @author 	Groep 8
  * @version May 2013
  */
 public class PowerFailureEffect extends Effect {
 
 	/**
 	 * If a player is in a square without power at the start of his turn, he can only perform 3 actions in this turn, instead of 4.
 	 */
 	public static final int DAMAGE = 1;
 	
 	/**
 	 * Return whether this effect must be executed
 	 * before the given effect.
 	 */
 	public boolean isPriorTo(Effect effect) {
 		return true;
 	}
 	
 	/**
 	 * Link the given effect to this effect.
 	 * 
 	 * @param	effect
 	 * 			The effect that has to be linked.
 	 * 
 	 * @effect	If the given effect is a LightGrenadeEffect, this effect is removed from the square.
 	 * 			|if(effect instanceof LightGrenadeEffect) 
 	 * 			|	getSquare().removeEffect(this)
 	 *  @effect	If the given effect is a LightGrenadeEffect, a PowerFailureLightGrenadeEffect is added to the square.
 	 * 			|if(effect instanceof LightGrenadeEffect) 
 	 * 			|	getSquare().addEffect(new PowerFailureLightGrenadeEffect())
 	 */
 	@Override
 	public void linkEffect(Effect effect) {
 		if(effect instanceof LightGrenadeEffect) {
 			getSquare().addEffect(new PowerFailureLightGrenadeEffect());
 			getSquare().removeEffect(this);
 		}
 	}
 	
 	/**
 	 * Unlink  the given effect from this effect.
 	 * 
 	 * @param	effect
 	 * 			The effect that has to be unlinked.
 	 * 
 	 * @effect	If the given effect is a PowerFailureEffect, this effect is removed from the square.
 	 * 			|if(effect instanceof PowerFailureEffect) 
 	 * 			|	getSquare().removeEffect(this)
 	 */
 	@Override
 	public void unlinkEffect(Effect effect) {
 		if(effect instanceof PowerFailureEffect) {
 			int nbPowerFailures = 0; 
 			for(Item i: getSquare().getItems()) {
 				if(i instanceof PowerFailure)
 					nbPowerFailures++;
 			}
 			if(nbPowerFailures <= 1)
 				getSquare().removeEffect(this);
 		}
 	}
 	
 	/**
 	 * Checks if the given effect can be combined with this effect.
 	 * 
 	 * @param	effect
 	 * 			The effect that we want to combine.
 	 * 
 	 * @return 	True if the given effect is a LightGrenadeEffect.
 	 * 			|return (effect instanceof LightGrenadeEffect)
 	 */
 	@Override
 	public boolean canCombineWith(Effect effect) {
		return (effect instanceof LightGrenadeEffect);
 	}
 	
 	/**
 	 * Executes what happens when you step on a square with a power failure and no active light grenade.
 	 * 
 	 * @param	Actor
 	 * 			The Actor stepping on this effect.
 	 *
 	 * @effect	If the actor is a player, the turn of the player ends.
 	 * 			| if(actor instanceof Player)
 	 * 			| 	((Player)actor).loseTurn()
 	 * @effect	If the actor is an Identity disk, the range of his move decreases.
 	 * 			| if(actor instanceof IdentityDisk)
 	 * 			| 	((IdentityDisk)actor).decreaseRange()
 	 */
 	public void onStep(Actor actor) {
 		if(actor instanceof Player) {
 			// If a player enters a square without power and that square
 			// does not contain an active light grenade, his turn ends.
 			((Player) actor).loseTurn();
 		}
 		if(actor instanceof IdentityDisk) {
 			// If a player enters a square without power and that square
 			// does not contain an active light grenade, his turn ends.
 			((IdentityDisk) actor).decreaseRange();
 		}
 	}
 
 	/**
 	 * Executes what happens when you leave a square with a power failure which is nothing so far.
 	 * 
 	 * @param	Actor
 	 * 			The Actor leaving this effect.
 	 */
 	public void onLeave(Actor actor) {}
 
 	/**
 	 * Executes what happens when you start on a square with a power failure.
 	 * 
 	 * @param	Actor
 	 * 			The Actor starting on this effect.
 	 * 
 	 * @effect	The player can only perform 2 actions instead of 3.
 	 * 			| getSquare().getPlayer().applyActionDamage(DECREASE)
 	 */
 	public void onStart(Actor actor) {
 		if(actor instanceof Player) {
 			// If a player is on a square without power at the start of his turn, 
 			// he can only perform 3 actions in this turn, instead of 4.
 			getSquare().getPlayer().applyActionDamage(DAMAGE);
 		}
 	}
 	
 	/**
 	 * Executes what happens when you land on a square with a power failure.
 	 * 
 	 * @param	Actor
 	 * 			The Actor landing on this effect.
 	 */
 	public void onLand(Actor actor) {}
 	
 }
