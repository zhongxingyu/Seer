 package vooga.rts.gamedesign.sprite.rtsprite;
 
 import java.awt.Dimension;
 
 
 import vooga.rts.util.Location;
 import vooga.rts.util.Pixmap;
 import vooga.rts.util.Sound;
 
 
 
 /**
  * 
  * @author Ryan Fishel
  * @author Kevin Oh
  * @author Francesco Agosti
  * @author Wenshun Liu 
  *
  */
 public abstract class Projectile extends RTSprite implements IMovable {
 
 	
 	private Integer myDamage;
 	
 	private boolean isSelected;
 	
 	
 	public Projectile(Pixmap pixmap, Location loc, Dimension size, Sound sound, int damage, int health){
 		super(pixmap, loc, size, sound, damage, health);
 		myDamage = damage;
		setVelocity(0, 60);
 	}
 	
 	
 	public void attack(RTSprite other) throws CloneNotSupportedException{
 		
 		other.accept(this);
 		move(other.getCenter());
 	}
 	
 	/**
 	 * Creates a copy of the projectile so that we can keep shooting a new
 	 * instance of the projectile every time we shoot. 
 	 * @param toClone is the projectile that we are making a copy of
 	 * @return the cloned copy of the projectile
 	 * @throws CloneNotSupportedException 
 	 */
 	public Projectile clone(Projectile toClone) throws CloneNotSupportedException{
 			return (Projectile) toClone.clone();
 
 	}
 	
 
 }
