 package weapons;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 
 import dudes.Dude;
 
 public class KnightKnife extends Weapon{
 	
 	
 	public KnightKnife(Dude owner) {
 		this(owner.pos[0], owner.pos[1]);
 		assignOwner(owner);
 	}
 
 	public KnightKnife(float x, float y) {
 		super();
 		try {
 			weaponSprite = new Image("Assets/FloorItems/sword.png");
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 		this.x = x;
 		this.y = y;
 		attackSprite = null;
 		damage = 5;
 		attackWidth = 100;
 		attackHeight = 6;
		attackTime = 1000;
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
