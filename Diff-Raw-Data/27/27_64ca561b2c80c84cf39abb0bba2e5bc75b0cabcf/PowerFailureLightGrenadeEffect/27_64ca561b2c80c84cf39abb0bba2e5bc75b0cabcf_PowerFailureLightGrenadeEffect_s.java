 package effect;
 
 import item.Item;
 import item.PowerFailure;
 import game.Actor;
 import game.Player;
 
 /**
  * Represents the effect of a light grenade on a square with a power failure.
  * 
  * @invar	This effect cannot be linked with another effect.
  * 			| canCombineWith(effect) == false
  * @invar	This effect either unlinks to a power failure or a light grenade.
  * @invar	When a player steps on a PowerFailureLightGrenadeEffect, he loses 4 actions.
  * 			| DAMAGE = 4
  * @invar	When a player steps on a PowerFailureLightGrenadeEffect, it unlinks to a power failure.
  * 			| unlinkEffect(new PowerFailureEffect())
  * 
  * @author 	Groep 8
  * 
  * @version February 2013
  */
 public class PowerFailureLightGrenadeEffect extends PowerFailureEffect {
 
 	/**
 	 * Represents the damage from an exploding light grenade together with a power failure.
 	 */
 	public final static int DAMAGE = 4;
 	
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
 	 * @effect	If the given effect is a LightGrenadeEffect, this effect is removed from the square.
 	 * 			|if(effect instanceof LightGrenadeEffect) 
 	 * 			|	getSquare().removeEffect(this)
 	 * @effect	If the given effect is a LightGrenadeEffect, a PowerFailureEffect is added to the square.
 	 * 			|if(effect instanceof LightGrenadeEffect) 
 	 * 			|	getSquare().addEffect(new PowerFailureEffect())
 	 * 
 	 * @effect	If the given effect is a PowerFailureEffect, this effect is removed from the square.
 	 * 			|if(effect instanceof PowerFailureEffect) 
 	 * 			|	getSquare().removeEffect(this)
 	 * @effect	If the given effect is a PowerFailureEffect, a LightGrenadeEffect is added to the square.
 	 * 			|if(effect instanceof PowerFailureEffect) 
 	 * 			|	getSquare().addEffect(new LightGrenadeEffect())
 	 */
 	@Override
 	public void unlinkEffect(Effect effect) {
 		if(effect instanceof LightGrenadeEffect) {
 			getSquare().addEffect(new PowerFailureEffect());
 			getSquare().removeEffect(this);
 		}	
 		if(effect instanceof PowerFailureEffect) {
 			int nbPowerFailures = 0; 
 			for(Item i: getSquare().getItems()) {
 				if(i instanceof PowerFailure)
 					nbPowerFailures++;
 			}
 			if(nbPowerFailures <= 1)
 				getSquare().addEffect(new LightGrenadeEffect());
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
	
	/**
 	 * Executes what happens when you step on a square with this power failure light grenade effect.
 	 * 
 	 * @param	Actor
 	 * 			The Actor stepping on this effect.
 	 * 
 	 * @effect	The player on the square loses 4 actions.
 	 * 			| getSquare().getPlayer().applyActionDamage(DAMAGE)
 	 * @effect	This effect is unlinked from the square
 	 * 			| getSquare().unlinkEffect(new LightGrenadeEffect())
 	 * @effect	If the player has a flag, he drops his flag.
 	 * 			| dropFlag((Player)actor)
 	 */
 	@Override
 	public void onStep(Actor actor) {
 		if(actor instanceof Player) {
 			((Player) actor).applyActionDamage(DAMAGE);
 			dropFlag((Player)actor);
 			getSquare().unlinkEffect(new LightGrenadeEffect());
 		}
 	}
 
 }
