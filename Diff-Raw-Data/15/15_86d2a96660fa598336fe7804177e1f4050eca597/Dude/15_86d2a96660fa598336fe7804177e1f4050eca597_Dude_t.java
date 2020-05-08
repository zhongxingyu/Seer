 package dudes;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.geom.Shape;
 
 import weapons.Attack;
 import weapons.Weapon;
 
 public abstract class Dude {
 	public int health;
 	public Weapon weapon;
 	public SpriteSheet sprites;
 	public int[] spriteIndex = new int[2];
 	public Animation currentAnimation;
 	public float[] pos = new float[2];
 	public Shape hitbox;
 	public boolean isRight;
 	public boolean isAttacking;
 	public float moveSpeed;
 	public int maxHealth;
 	public Color healthFill;
 	public int attackTime;
 	public boolean invincible;
 	public int invincibleDuration;
 	public int invincibleTimer;
 	
 	public boolean flinching;
 	public int flinchDur;
 	public int flinchTime;
 	
 	public boolean delayed;
 	public int delayDur;
 	public int delayTime;
 	
 	public abstract float[] weaponLoc();
 
 	public Dude() {
 		invincibleDuration = 2000;
 	}
 
 	public void attack() {
 		this.weapon.attack();
 	}
 
 	public void flinch(int milliseconds) {
 		if (!flinching){
 			this.flinchTime = 0;
 			this.flinchDur = milliseconds;
 			this.flinching = true;
 			isAttacking = false;
 			delayed = false;
 		}
 
 	}
 
 	public void hurt(int damage, int flinch) {
 		if (!invincible) {
 			this.health = Math.max(0, this.health - damage);
 			invincible = true;
 			invincibleTimer = 0;
 			flinch(flinch);
 		}
 		else{
 			if (invincibleTimer > invincibleDuration){
 				invincible = false;
 				this.health = Math.max(0, this.health - damage);
 				flinch(flinch);
 			}
 		}
 	}
 
 	public void render(Graphics g) throws SlickException {
 		if(currentAnimation!=null){
 			currentAnimation.draw(pos[0],pos[1]);
 		} else{
 			sprites.getSprite(spriteIndex[0], spriteIndex[1]).draw(pos[0], pos[1]);
 		}
 
 		// Render a health bar for the Dude
 		int offset = -10;
 		float x = pos[0];
 		float y = pos[1] + offset;
 		int width = 100;
 		int height = 10;
 		int padding = 2;
 		int healthRemaining = width * health / maxHealth;
 		g.drawRect(x - padding - width / 2, y - padding - height / 2, width
 				+ padding * 2, height + padding * 2);
 		g.setColor(healthFill);
 		g.fillRect(x - width / 2, y - height / 2, healthRemaining, height);
		//g.draw(this.hitbox);
 		for (Attack attack : this.weapon.attacks) {
 			g.draw(attack.hitbox);
 		}
 	}
 }
