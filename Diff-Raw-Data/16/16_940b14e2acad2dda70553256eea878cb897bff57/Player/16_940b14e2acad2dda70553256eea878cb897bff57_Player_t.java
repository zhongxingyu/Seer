 package se.doverfelt.dungeonCrawl;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 
 import it.randomtower.engine.ME;
//import it.randomtower.engine.ResourceManager;
 import it.randomtower.engine.entity.Entity;
 
 public class Player extends Creature {
 	
 	private String lastDir = "STAND_RIGHT";
 	
 	private SpriteSheet g;
 	
 	public boolean isColliding = false;
 	public Entity collidingCreature;
 	
 	public static float SPEED = 1;
 	
 	public Player(float x, float y) {
 		super(x, y, 30, 3, 2);
 		
 		Image g1 = ResourceManager.getSpriteSheet("char").getScaledCopy(2);
 		
 		g = new SpriteSheet(g1, 32 * 2, 32 * 2);
 		
 		setHitBox(14, 5, g.getSubImage(1, 1).getWidth() - 28, g.getSubImage(1, 1).getHeight() - 10);
 		
 		setupAnimations(g);
 		
 		addType(PLAYER);
 		
 		setAI(new PlayerAI(this, (WorldOverworld) world));
 		
 	}
 	
 	private void setupAnimations(SpriteSheet g) {
 		
 		setGraphic(g);
 		duration = 125;
 		addAnimation("STAND_DOWN", false, 3, 0);
 		addAnimation("STAND_RIGHT", false, 0, 0);
 		addAnimation("STAND_LEFT", false, 1, 0);
 		addAnimation("STAND_UP", false, 2, 0);
 		addAnimation(ME.WALK_RIGHT, true, 0, 1, 0, 2, 0);
 		addAnimation(ME.WALK_LEFT, true, 1, 1, 0, 2, 0);
 		addAnimation(ME.WALK_UP, true, 2, 1, 0, 2, 0);
 		addAnimation(ME.WALK_DOWN, true, 3, 1, 0, 2, 0);
 		
 	}
 
 	@Override
 	public void update(GameContainer gc, int delta) throws SlickException{
 		super.update(gc, delta);
 		
 		Input input = gc.getInput();
 		
 		if (input.isKeyPressed(Input.KEY_SPACE) && isColliding && collidingCreature.isType(PLAYER)) {
 			attack((Creature)collidingCreature);
 		}
 		
 		if (input.isKeyDown(Input.KEY_W)) {
 			//setHitBox(-10, 5, g.getSubImage(1, 1).getWidth() - 14, g.getSubImage(1, 1).getHeight() - 10);
 			move(0, -SPEED);
 			currentAnim = ME.WALK_UP;
 			lastDir = "STAND_UP";
 		}
 		if (input.isKeyDown(Input.KEY_S)) {
 			//setHitBox(-10, 5, g.getSubImage(1, 1).getWidth() - 14, g.getSubImage(1, 1).getHeight() - 10);
 			move(0, SPEED);
 			currentAnim = ME.WALK_DOWN;
 			lastDir = "STAND_DOWN";
 		}
 		if (input.isKeyDown(Input.KEY_A)) {
 			//setHitBox(14, 5, g.getSubImage(1, 1).getWidth() - 10, g.getSubImage(1, 1).getHeight() -10);
 			move(-SPEED, 0);
 			currentAnim = ME.WALK_LEFT;
 			lastDir = "STAND_LEFT";
 		}
 		if (input.isKeyDown(Input.KEY_D)) {
 			//setHitBox(-10, 5, g.getSubImage(1, 1).getWidth() - 14, g.getSubImage(1, 1).getHeight() - 10);
 			move(SPEED, 0);
 			currentAnim = ME.WALK_RIGHT;
 			lastDir = "STAND_RIGHT";
 		}
 		if (!input.isKeyDown(Input.KEY_W) && !input.isKeyDown(Input.KEY_A) && !input.isKeyDown(Input.KEY_S) && !input.isKeyDown(Input.KEY_D)) {
 			currentAnim = lastDir;
 		}
 
         if (input.isKeyPressed(Input.KEY_SPACE) && this.collide(Creature.ZOMBIE, ((WorldOverworld)world).zombie.x, ((WorldOverworld)world).zombie.y) != null) {
             this.collidingCreature.destroy();
         }
 		
 		if (animations != null) {
 			if (currentAnim != null) {
 				Animation anim = animations.get(currentAnim);
 				if (anim != null) {
 					anim.update(delta);
 				}
 			}
 		}
 		
 	}
 
 }
