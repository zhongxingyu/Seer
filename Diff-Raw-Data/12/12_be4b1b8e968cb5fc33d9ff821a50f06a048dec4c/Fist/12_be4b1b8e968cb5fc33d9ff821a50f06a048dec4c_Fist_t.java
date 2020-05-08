 package weapons;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.geom.Rectangle;
 
 import dudes.Dude;
 import dudes.Player;
 
 public class Fist extends Weapon {
 	
 	public Fist(Dude owner) {
 		this(owner.pos[0], owner.pos[1]);
 		assignOwner(owner);
 	}
 	
 	public Fist(float x, float y) {
 		super();
		damage = 10;
 		attackWidth = 30;
 		attackHeight = 6;
 		attackTime = 200;
 		delayTime = 500;
 		playerSize = 64;
 	}
 
 	@Override
 	public void init() throws SlickException {
 		weaponSheet = new SpriteSheet("Assets/Weapons/Fist/player" + ((Player)owner).playerID + "Fist.png", playerSize, playerSize);
 		playerSheet = ((Player)owner).sprites;
 		defaultSprite[0] = weaponSheet.getSprite(0, 5);
 		defaultSprite[1] = weaponSheet.getSprite(0, 4);
 		initAnimations();
 	}
 	
 	@Override
 	public void attack() {
 		float[] corner = owner.weaponLoc();
 		Rectangle hitbox;
 		if (owner.isRight){
 			hitbox = new Rectangle(corner[0], corner[1], attackWidth,
 				attackHeight);
 		}
 		else {
 			hitbox = new Rectangle(corner[0]-attackWidth, corner[1], attackWidth,
 					attackHeight);
 		}
 		attacks.add(new Attack(owner.isRight, hitbox, "player"));
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
