 package weapons;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.geom.Rectangle;
 
 import dudes.Dude;
 
 public class KnightKnife extends Weapon{
 	
 	public KnightKnife(Dude owner) {
 		this(owner.pos[0], owner.pos[1], owner.kind);
 		assignOwner(owner);
 	}
 
 	public KnightKnife(float x, float y, int k) {
 		super();
 		groundSprite = null;
 		this.x = x;
 		this.y = y;
 		damage = 5;
 		attackWidth = 50;
 		attackHeight = 6;
 		attackTime = 1000;
 		delayTime = 500;
 		kind = k;
 	}
 	
 	@Override
 	public void init() throws SlickException {
 		if(kind==0){
 			weaponSheet = new SpriteSheet("Assets/Weapons/KnightKnife/new_sheet.png", spriteSizeX, spriteSizeY);
 		} else if(kind==1){
 			weaponSheet = new SpriteSheet("Assets/Weapons/KnightKnife/camoKnightSheet.png", spriteSizeX, spriteSizeY);
 		} else{
 			weaponSheet = new SpriteSheet("Assets/Weapons/KnightKnife/darkKnightSheet.png", spriteSizeX, spriteSizeY);
 		}
 		defaultSprite[0] = weaponSheet.getSprite(0, 5);
 		defaultSprite[1] = weaponSheet.getSprite(0, 4);
 		initAnimations();
 	}
 	
 	@Override
 	public void attack() {
 		float[] corner = owner.weaponLoc();
		//corner[0] -= attackWidth / 2;
		//corner[1] -= attackHeight / 2;
 		Rectangle hitbox;
 		if (owner.isRight){
 			hitbox = new Rectangle(corner[0], corner[1], attackWidth,
 				attackHeight);
 		}
 		else{
 			hitbox = new Rectangle(corner[0]-attackWidth, corner[1], attackWidth,
 					attackHeight);
 		}
 		attacks.add(new Attack(owner.isRight, hitbox, "player"));
 	}
 
 	@Override
 	public void initAnimations(){
 		// flinch left
 		anims[0] = new Animation(weaponSheet, 0, 4, 3, 4, true, 80, true);
 		anims[0].setLooping(false);
 		// flinch right
 		anims[1] = new Animation(weaponSheet, 0, 5, 3, 5, true, 80, true);
 		anims[1].setLooping(false);
 
 		// punch left
 		anims[2] = new Animation(weaponSheet, 0, 0, 2, 0, true, 80, true);
 		anims[2].setLooping(false);
 		// punch right
 		anims[3] = new Animation(weaponSheet, 0, 1, 2, 1, true, 80, true);
 		anims[3].setLooping(false);
 
 		// walk left
 		anims[4] = new Animation(weaponSheet, 0, 2, 3, 2, true, 80, true);
 		// walk right
 		anims[5] = new Animation(weaponSheet, 0, 3, 3, 3, true, 80, true);
 		
 		// die left
 		anims[6] = new Animation(weaponSheet, 0, 6, 2, 6, true, 200, true);
 		anims[6].setLooping(false);
 		// die right
 		anims[7] = new Animation(weaponSheet, 0, 7, 2, 7, true, 200, true);
 		anims[7].setLooping(false);
 
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
