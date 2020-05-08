 package game.entity;
 
 import game.WormsGame;
 import game.data.Gamemode;
 import game.data.TurnPhase;
 import static game.data.TurnPhase.*;
 
 public abstract class LivingEntity extends Entity {
 
 	// Health
 	private int health;
 	private int takeDamage;
 
 	// Render
 	/**
 	 * The full amount of damage that has been given this turn
 	 */
 	private int fullDamage;
 	/**
 	 * Determines if the damage shoud be shown
 	 */
 	private boolean showDamage;
 
 	public LivingEntity(WormsGame wormsGame, int health, int x, int y, int width, int height) {
 		super(wormsGame, x, y, width, height);
 		this.health = health;
 		showDamage = false;
 		fullDamage = 0;
 	}
 
 	public void onTick(TurnPhase turn) {
 		if (isFalling())
 			fallDuration++;
 		if (turn == DAMAGE) {
 			if (takeDamage > 0) {
 				if (health <= 0) {
 					takeDamage = 0;
 					onDeath();
 				} else {
 					--health;
 					--takeDamage;
 				}
 			}
 		}
 	}
 
 	public void setFalling(boolean newIsFalling) {
 		if (isFalling() && !newIsFalling && fallDistance > 0) {
 			int dmgDistance = (int) fallDistance - Gamemode.FREE_FALL_DISTANCE;
 			if (dmgDistance > 0) {
 				int fallDamage = (int) (dmgDistance * 0.1);
 				System.out.println("Taking " + fallDamage + " fall dmg over " + dmgDistance + " units");
 				takeDamgage(fallDamage);
 			}
 			fallDistance = 0F;
 		}
 		if (!isFalling()) {
 			fallDuration = (newIsFalling) ? 1 : 0;
 		}
 	}
 
 	public void takeDamgage(int dmg) {
 		takeDamage += dmg;
 		fullDamage = takeDamage;
 	}
 
 	public boolean isDead() {
 		return health == 0;
 	}
 
 	public int getHealth() {
 		return health;
 	}
 
 	public void onTurnChange(TurnPhase turn) {
 		if (turn == DAMAGE) {
 			if (fullDamage > 0) {
 				getWormsGame().addText(x, y - 20, "" + fullDamage, glColorId);
 				showDamage = true;
 			}
 		}
 	}
 
 	/**
 	 * Process the death of this cube
 	 */
 	public void onDeath() {
 		Explosion e = new Explosion(this, getPoint(), 50, 1, 25);
 		e.explode();
 	}
 
 	/**
 	 * Returns if the damage has been deducted
 	 * 
 	 * @return
 	 */
 	public boolean canAdvance() {
 		return takeDamage == 0;
 	}
 
 	public void render() {
 		if (isDead())
 			return;
 		if (showDamage) {
 			if (takeDamage == 0) {
 				fullDamage = 0;
 				showDamage = false;
 			}
 		}
 	}
 
 }
