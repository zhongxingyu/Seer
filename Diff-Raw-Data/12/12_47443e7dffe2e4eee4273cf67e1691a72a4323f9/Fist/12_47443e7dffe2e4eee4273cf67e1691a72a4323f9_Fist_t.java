 package weapons;
 
 import org.newdawn.slick.geom.Rectangle;
 
 import dudes.Dude;
 
 public class Fist extends Weapon {
 	
 	public Fist(Dude owner) {
 		super();
 		this.owner = owner;
 		weaponSprite = null;
 		attackSprite = null;
 		damage = 5;
		attackWidth = 60;
 		attackHeight = 6;
 		attackTime = 200;
 		delayTime = 500;
 	}
 
 	@Override
 	public void attack() {
 		float[] corner = owner.weaponLoc();
 		corner[0] -= attackWidth / 2;
 		corner[1] -= attackHeight / 2;
 		Rectangle hitbox = new Rectangle(corner[0], corner[1], attackWidth,
 				attackHeight);
 		attacks.add(new Attack(attackSprite, owner.isRight, hitbox, "player"));
 	}
 
 	@Override
 	protected boolean updateAttack(Attack attack) {
 		if(owner.isAttacking) {
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 }
