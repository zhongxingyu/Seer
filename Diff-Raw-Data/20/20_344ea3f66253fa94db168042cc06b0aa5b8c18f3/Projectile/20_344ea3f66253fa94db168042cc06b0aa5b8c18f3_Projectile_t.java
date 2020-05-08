 package character.weapon;
 
 import java.awt.Color;
 
 import main.Painter;
 import character.NonPlayerCharacter;
 
 class Projectile extends NonPlayerCharacter {
 	private boolean visible;
 
 	/**
 	 * @param power
 	 *            irrelevant for this item and can be set to an arbitrary number
 	 * @param speed
 	 *            sets speed of motion, recommend 7 to 10
 	 * @param startX
 	 *            sets starting position (originate near the attacking enemy)
 	 * @param startY
 	 *            sets starting position (originate near the attacking enemy)
 	 * @param visible
 	 *            sets whether the bullet is visible or not
 	 */
 	Projectile(int power, int speed, int startX, int startY, boolean visible) {
 		super(1, power);
 		this.centerX = startX;
 		this.centerY = startY;
 		this.speedX = speed;
 		this.visible = visible;
 	}
 
 	/**
 	 * sets to invisible, can also be used to assign a new active sprite (eg:
 	 * explosion)
 	 */
 
 	@Override
 	public void die() {
 		this.visible = false;
 		// TODO remove from the container
 
 	}
 
 	/**
 	 * this isn't ever going to function, do not use it
 	 */
 	@Deprecated
 	@Override
 	public void attack() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void update() {
 		this.centerX += this.attributes.getAttribute(MOVE_SPEED);
 		this.centerX += this.speedX;
 		if (this.centerX > 800)
 			this.visible = false;
 
 	}
 
 	public boolean isVisible() {
 		return this.visible;
 	}
 
 	@Override
 	public void paint() {
		System.out.println(Color.BLACK);
 		Painter.paint(this.centerX, this.centerY, 10, 5, Color.BLACK);
 	}
 
 	@Deprecated
 	@Override
 	public void update(long elapsedTime) {
 		// TODO Auto-generated method stub
 
 	}
 }
