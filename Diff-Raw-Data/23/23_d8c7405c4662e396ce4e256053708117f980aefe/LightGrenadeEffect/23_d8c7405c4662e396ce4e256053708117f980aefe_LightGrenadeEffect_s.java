 package effect;
 
 import game.Actor;
 import game.Player;
 
 
 /**
  * Represents the effect of a light grenade on a square.
  * 
  * @invar	This effect can be combined with a power failure effect.
  * @invar	When a player steps or lands on a light grenade effect, he loses 3 actions.
  * 			| DAMAGE = 3
  * 
  * @author 	Groep 8
  * @version February 2013
  */
 public class LightGrenadeEffect extends Effect {
 	
 	/**
 	 * Represents the damage from an exploding light grenade.
 	 */
 	public final static int DAMAGE = 3;	
 	
 	/**
 	 * Return the index of this effect.
 	 * This has influence on the order of executing effects.
 	 */
 	public int getIndex() {
 		return 0;
 	}
 	
 	/**
 	 * Link the given effect to this light grenade effect.
 	 * @effect 	If the effect is a PowerFailureEffect,
 	 * 			this effect is replaced by a PowerFailureLightGrenadeEffect at its location.
 	 * 			|getSquare().addEffect(new PowerFailureLightGrenadeEffect())
 	 * @effect	This effect is removed from the square.
 	 * 			|getSquare().removeEffect(this)
 	 */
 	@Override
 	public void linkEffect(Effect effect) {
 		if(effect instanceof PowerFailureEffect) {
 			getSquare().addEffect(new PowerFailureLightGrenadeEffect());
 			getSquare().removeEffect(this);
 		}
 	}
 	
 	
 	/**
 	 * Unlink the given effect from this light grenade effect.
 	 * @effect 	If the given effect is a LightGrenadeEffect, 
 	 * 			this LightGrenadeEffect is removed as the effect at its location.
 	 * 			|if(effect instanceof LightGrenadeEffect)
 	 * 			|	getSquare().removeEffect(this)
 	 */
 	@Override
 	public void unlinkEffect(Effect effect) {
 		if(effect instanceof LightGrenadeEffect) {
 			getSquare().removeEffect(this);
 		}
 	}
 	
 	/**
 	 * Checks if the given effect can be combined with this effect.
 	 * 
 	 * @param	effect
 	 * 			The effect that we want to combine.
 	 * @return 	True if the given effect is a PowerFailureEffect.
 	 * 			|return (effect instanceof PowerFailureEffect)
 	 */
 	@Override
 	public boolean canCombineWith(Effect effect) {
 		return (effect instanceof PowerFailureEffect);
 	}
 	
 	/**
 	 * Executes what happens when you step on a square with this light grenade effect.
 	 * 
 	 * @param	Actor
 	 * 			The Actor stepping on this effect.
 	 * 
 	 * @effect	The player on the square loses 3 actions.
 	 * 			| getSquare().getPlayer().applyActionDamage(DAMAGE)
 	 * @effect	This effect is unlinked from the square.
 	 * 			| getSquare().unlinkEffect(new LightGrenadeEffect)
 	 */
 	public void onStep(Actor actor) {
 		if(actor instanceof Player) {
 			getSquare().getPlayer().applyActionDamage(DAMAGE);
			getSquare().unlinkEffect(new LightGrenadeEffect());
 			dropFlag((Player)actor);
 		}
 	}
 	
 	/**
 	 * Executes what happens when you leave a square with this light grenade effect.
 	 * 
 	 * @param	Actor
 	 * 			The Actor leaving this effect.
 	 */
 	public void onLeave(Actor actor) {}
 	
 	/**
 	 * Executes what happens when you start on a square with this light grenade effect.
 	 * 
 	 * @param	Actor
 	 * 			The Actor starting on this effect.
 	 */
 	public void onStart(Actor actor) {}
 
 	/**
 	 * Executes what happens when you land on a square with this light grenade effect.
 	 * 
 	 * @param	Actor
 	 * 			The Actor landing on this effect.
 	 * 
 	 * @effect	The player on the square loses 3 actions.
 	 * 			| getSquare().getPlayer().applyActionDamage(DAMAGE)
 	 * @effect	This effect is unlinked from the square.
 	 * 			| getSquare().unlinkEffect(new LightGrenadeEffect())
 	 */
 	public void onLand(Actor actor) {
 		if(actor instanceof Player) {
 			((Player) actor).applyActionDamage(DAMAGE);
			getSquare().unlinkEffect(new LightGrenadeEffect());
 			dropFlag((Player)actor);
 		}
 	}
 	
 }
