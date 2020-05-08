 package vooga.rts.gamedesign.sprite.rtsprite;
 
 import java.awt.Dimension;
 
 
 import vooga.rts.gamedesign.strategy.attackstrategy.InstantDamage;
 import vooga.rts.util.Location;
 import vooga.rts.util.Pixmap;
 import vooga.rts.util.Sound;
 
 public class Bullet extends Projectile{
 
 	public Bullet(Pixmap pixmap, Location loc, Dimension size, Sound sound,
 			int damage, int health) {
 		super(pixmap, loc, size, sound, damage, health);
 		setAttackStrategy(new InstantDamage());
		//where was this supposed to go? :( 
		((InstantDamage)getAttackStrategy()).setDamage(damage);
 	}
 }
 
