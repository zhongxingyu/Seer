 package effect;
 
 import game.Actor;
 
 
 public class IdentityDiskEffect extends Effect {
 
 	/**
 	 * Represents the damage from being hit by a Identity disk.
 	 */
 	public final static int DAMAGE = 3;
 	
 	/**
 	 * 
 	 * @param effect
 	 */
 	@Override
 	public void linkEffect(Effect effect) {}
 	
 	
 	/**
 	 * 
 	 */
 	@Override
 	public void unlinkEffect(Effect effect) {
 		if(effect instanceof IdentityDiskEffect) {
 			getSquare().removeEffect(this);
		
 		}
 	}
 			
 	@Override
 	public boolean canCombineWith(Effect effect) {
 		return false;
 	}
 	
 	/**
 	 * Executes what happens when you step on a square with a power failure and no active light grenade.
 	 */
 	@Override
 	public void onStep(Actor actor) {}
 
 	/**
 	 * Executes what happens when you leave a square with a power failure which is nothing so far.
 	 */
 	@Override
 	public void onLeave(Actor actor) {}
 
 	/**
 	 * Executes what happens when you start on a square with a disk item effect.
 	 * 
 	 * @param	square
 	 * 			The square the effect belongs to.
 	 * 
 	 * @effect	When a player is hit by its own identity disk, he loses al his remaining actions.
 	 * 			| getSquare().getPlayer().emptyActions()
 	 * @effect	When a player is hit by an identity disk, he loses his next turn.
 	 * 			| getSquare().getPlayer().applyActionDamage(DAMAGE)
 	 * @effect	Unlink this effect from the square. 
 	 * 			| unlinkEffect(this)
 	 */
 	@Override
 	public void onStart(Actor actor) {
 		getSquare().getPlayer().emptyActions();
 		getSquare().getPlayer().applyActionDamage(DAMAGE);
 		unlinkEffect(this);
 	}
 
 	/**
 	 * Executes what happens when you land on a square with a disk item effect.
 	 */
 	@Override
 	public void onLand(Actor actor) {}
 	
 }
