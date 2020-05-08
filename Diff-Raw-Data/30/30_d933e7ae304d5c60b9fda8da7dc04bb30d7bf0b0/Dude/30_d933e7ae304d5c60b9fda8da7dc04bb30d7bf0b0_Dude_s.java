 package dudes;
 
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
	public SpriteSheet sprite;
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
 
 	public abstract float[] weaponLoc();
 
 	public Dude() {
 		invincibleDuration = 2000;
 	}
 
 	public void attack() {
 		this.weapon.attack();
 	}
 
 	public void flinch(int milliseconds) {
 
 	}
 
 	public void hurt(int damage, int flinch) {
 		if (!invincible) {
 			this.health = Math.max(0, this.health - damage);
 			invincible = true;
 			invincibleTimer = 0;
 		}
 		else{
 			if (invincibleTimer > invincibleDuration){
 				invincible = false;
 				this.health = Math.max(0, this.health - damage);
 			}
 		}
 	}
 
 	public void render(Graphics g) throws SlickException {
		new Image("Assets/npcs/daughterFront.png").draw(pos[0], pos[1], 2);
 
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
 		g.draw(this.hitbox);
 		for (Attack attack : this.weapon.attacks) {
 			g.draw(attack.hitbox);
 		}
 	}
 }
