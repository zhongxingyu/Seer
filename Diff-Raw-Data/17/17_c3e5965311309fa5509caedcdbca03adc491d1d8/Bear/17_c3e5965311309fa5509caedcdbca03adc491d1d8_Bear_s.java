 package weapons;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.geom.Rectangle;
 
 import dudes.Dude;
 import dudes.Player;
 
 public class Bear extends Weapon {
 	
 	public Bear(Dude owner) {
 		this(owner.pos[0], owner.pos[1]);
 		assignOwner(owner);
 	}
 	public Bear(float x, float y) {
 		super();
 		this.x = x;
 		this.y = y;
 		damage = 15;
 		attackWidth = 100;
 		attackHeight = 30;
 		attackTime = 200;
 		delayTime = 500;
 		spriteSizeX = 128;
 		spriteSizeY = 128;
 		playerSizeX = 128;
 		playerSizeY = 128;
 		cooldown = 50;
 	}
 	
 	@Override
 	public void init() throws SlickException {
 		weaponSheet = new SpriteSheet("Assets/Weapons/Bear/player" + ((Player)owner).playerID + "bigbear.png", spriteSizeX, spriteSizeY);
 		defaultSprite[0] = weaponSheet.getSprite(0, 5);
 		defaultSprite[1] = weaponSheet.getSprite(0, 4);	
 		initAnimations();
 	}
 	
 	@Override
 	public void attack() {
 		// TODO Auto-generated method stub
		float[] corner = owner.weaponLoc();
		corner[0] -= attackWidth / 2;
		corner[1] -= attackHeight / 2;
		Rectangle hitbox = new Rectangle(corner[0], corner[1], attackWidth,
 				attackHeight);
 		attacks.add(new Attack(owner.isRight, hitbox, "player"));
 	}
 
 	@Override
 	public void createGroundSprite() throws SlickException {
 		groundSprite = new Image("Assets/Weapons/Bear/bearclaw.png");
 	}
 }
