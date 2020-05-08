 package effect;
 
 import game.Actor;
 
 /**
  * Represents the effect of an IdentityDisk on a square.
  * 
  * @invar	This effect does not combine with another effect.
  * 			| canCombinewith(Effect) == false
  * @invar	When a player starts on a IdentityDiskEffect, he skips his turn.
  * 			| getLocation().getPlayer().skipTurn()
  * 
  * @author 	Groep 8
  * 
  * @version February 2013
  */
 public class IdentityDiskEffect extends Effect {
 
 	/**
 	 * Represents the damage from being hit by a Identity disk.
 	 */
 	public final static int DAMAGE = 3;
 	
 	/**
 	 * Return the index of this effect.
 	 * This has influence on the order of executing effects.
 	 */
 	public int getIndex() {
 		return 1;
 	}
 	
 	/**
 	 * Link the given effect to this light grenade effect.
 	 * 
 	 * @param	effect
 	 * 			This is the effect we want to link.
 	 */
 	@Override
 	public void linkEffect(Effect effect) {}
 	
 	/**
 	 * Unlink the given effect from this IdentityDiskEffect.
 	 * 
 	 * @param 	effect
 	 * 			This is the effect we want to unlink.
 	 * 
 	 * @effect 	If the given effect is a IdentityDiskEffect, 
 	 * 			this IdentityDiskEffect is removed as the effect at its location.
 	 * 			|if(effect instanceof IdentityDiskEffect)
 	 * 			|	getSquare().removeEffect(this)
 	 */
 	@Override
 	public void unlinkEffect(Effect effect) {
 		if(effect instanceof IdentityDiskEffect) {
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
 	 * Executes what happens when you step on a square with a power failure and no active light grenade.
 	 * 
 	 * @param	Actor
 	 * 			The Actor stepping on this effect.
 	 */
 	@Override
 	public void onStep(Actor actor) {}
 
 	/**
 	 * Executes what happens when you leave a square with a power failure which is nothing so far.
 	 * 
 	 * @param	Actor
 	 * 			The Actor leaving this effect.
 	 */
 	@Override
 	public void onLeave(Actor actor) {}
 
 	/**
 	 * Executes what happens when you start on a square with a disk item effect.
 	 * 
 	 * @param	Actor
 	 * 			The Actor starting on this effect.
 	 * 
 	 * @effect	When a player is hit by an identity disk, he skips a turn.
 	 * 			| getSquare().getPlayer().skipTurn()
 	 * @effect	Unlink this effect from the square. 
 	 * 			| getSquare().unlinkEffect(new IdentityDiskEffect())
 	 */
 	@Override
 	public void onStart(Actor actor) {
 		getSquare().getPlayer().skipTurn();
 		dropFlag(getSquare().getPlayer());
		getSquare().unlinkEffect(new IdentityDiskEffect());
 	}
 
 	/**
 	 * Executes what happens when you land on a square with a disk item effect.
 	 * 
 	 * @param	Actor
 	 * 			The Actor landing on this effect.
 	 */
 	@Override
 	public void onLand(Actor actor) {}
 	
 }
