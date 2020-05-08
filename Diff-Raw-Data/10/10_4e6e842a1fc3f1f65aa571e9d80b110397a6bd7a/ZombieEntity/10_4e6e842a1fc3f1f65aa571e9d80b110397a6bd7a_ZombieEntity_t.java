 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Random;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.geom.Vector2f;
 
 
 public class ZombieEntity extends Entity {
 
 	float walkingSpeed = .005f;
 	float chasingSpeed = .050f;
 	Vector2f facing;
 	Random rand = new Random();
 	
 	Image texture;
 	Animation anim;
 	
 	String state = "wander";
 	int life = 2000;
 	
 	static int scoreValue = 300;
 	
 	
 	public ZombieEntity(Entity parent, float x, float y) {
 		super(parent);
 		
 		position = new Vector2f(x, y);
 		width = 16.0f;
 		height = 16.0f;
 		facing = new Vector2f(rand.nextInt(360));
 		mask = new Rectangle(x, y, width, height);
 	}
 
 	@Override
 	public void update(GameContainer gc, int delta, GameplayState game) {
 		super.update(gc, delta, game);
 		
 		if (state == "dying") {
 			die(delta, game.player);
 		} else {
 			// If the zombie is within the smell radius of the player or in the flashlight, pursue the player.
 			if (GameplayState.player.getCenterPos().distance(this.getCenterPos()) < GameplayState.player.getSmellRadius()) {
 				state = "pursuePlayer";
 			} else if (GameplayState.player.flashlight.getMask().contains(getMask())) { 
 				state = "pursuePlayer";
 			} else {
 				state = "wander";
 			}
 			
 			SurvivorEntity nearestSurvivor = getNearSurvivor(EntityManager.survivorEntities.values());
 			if ( !state.equals("pursuePlayer") && nearestSurvivor != null) {
 				state = "pursueSurvivor";
 			} 
 			
 			doMovement(delta, game);
 			
 			if (state.equals("pursueSurvivor")) {
 				if (getMask().intersects(nearestSurvivor.getMask())) {
 					nearestSurvivor.convertToZombie(game, delta);
 				}
 			}
 		}
 	}
 		
 	private void doMovement(int delta, GameplayState game) {
 		if (state.equals("wander")) {
 			doWanderMovement(delta, game);
 		} else if (state.equals("pursuePlayer")) {
 			doPursuePlayerMovement(delta, game);
 		} else if (state.equals("pursueSurvivor")) {
 			doPursueSurvivorMovement(delta, game);
 		} else if (state.equals("dying")) {
 		}
 	}
 	
 	private void doWanderMovement(int delta, GameplayState game) {
 		// If this zombie is going to hit a wall, set its facing vector to a randomly-chosen direction.
 		// Use a while loop to ensure success
 		int i = 0;
 		Shape nextMask = getNextMask(delta);
 		while (collideWithWall(game.terrain, delta) || collideWithSpotlight(EntityManager.spotlightEntities.values(), delta, nextMask) != null)
 		{
 			facing.add(rand.nextInt(360));
 			i++;
 			if (i > 30) {
 				state = "dying";
 				return;
 			}
 			nextMask = getNextMask(delta);
 		}
 		
 		position.add(facing.normalise().scale(walkingSpeed*delta));
		anim.update(delta);
 	}
 	
 	private void doPursueEntityMovement(int delta, GameplayState game, Entity target) {
 		if (!getMask().intersects(target.getMask())) {
 			// First, obtain the vector towards the entity
 			facing = target.position.copy().sub(position);
 			if (collideWithWall(game.terrain, delta) || collideWithSpotlight(EntityManager.spotlightEntities.values(), delta, getNextMask(delta)) != null)
 			{
 				// Try to walk towards the entity without going into a wall
 				facing = getVerticalAxisVectorTowardsEntity(target);
 				if (collideWithWall(game.terrain, delta) || collideWithSpotlight(EntityManager.spotlightEntities.values(), delta, getNextMask(delta)) != null) {
 					facing = getHorizontalAxisVectorTowardsEntity(target);
 					if (collideWithWall(game.terrain, delta) || collideWithSpotlight(EntityManager.spotlightEntities.values(), delta, getNextMask(delta)) != null) {
 						// If both fail, just resort to wandering
 						doWanderMovement(delta, game);
 						return;
 					}
 				}
 			}
 			position.add(facing.normalise().scale(chasingSpeed*delta));
			anim.update(delta*2);
 		}
 	}
 	
 	
 	private void doPursuePlayerMovement(int delta, GameplayState game) {
 		doPursueEntityMovement(delta, game, GameplayState.player);
 	}
 	
 	private void doPursueSurvivorMovement(int delta, GameplayState game) {
 		SurvivorEntity target = getNearSurvivor(EntityManager.survivorEntities.values());
 		if (target != null) {
 			doPursueEntityMovement(delta, game, target);
 		} else {
 			doWanderMovement(delta, game);
 		}
 	}
 	
 	private SurvivorEntity getNearSurvivor(Collection<SurvivorEntity> survivors) {
 		for (SurvivorEntity s : survivors) {
 			if (getCenterPos().distance(s.getCenterPos()) < s.smellRadius) {
 				return s;
 			}
 		}
 		return null;
 	}
 	
 	
 	private Vector2f getVerticalAxisVectorTowardsEntity(Entity e) {
 		if (e.position.y < position.y)
 			return new Vector2f(0f, -1f);
 		else 
 			return new Vector2f(0f, 1f);
 	}
 	
 	private Vector2f getHorizontalAxisVectorTowardsEntity(Entity e) {
 		if (e.position.x < position.x)
 			return new Vector2f(-1f, 0f);
 		else 
 			return new Vector2f(1f, 0f);
 	}
 	
 	/*
 	 * Returns true if this entity collides with any wallEntity in the surrounding area.
 	 */
 	public boolean collideWithWall(World world, int delta) {
 		Shape nextMask = getNextMask(delta);
 		ArrayList<Integer> surroundingCells = world.getSurroundingCellContents(position.x, position.y);
 		for (int id : surroundingCells) {
 			// Only check collisions for occupied cells
 			if (id != 0) {
 				Shape mask = ((WallEntity)EntityManager.getEntity(id)).getMask();
 				if ( !(nextMask.getMaxY() < mask.getMinY() ||
 						nextMask.getMinY() > mask.getMaxY() ||
 						nextMask.getMaxX() < mask.getMinX() ||
 						nextMask.getMinX() > mask.getMaxX())) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	private SpotlightEntity collideWithSpotlight(Collection<SpotlightEntity> spotlights, int delta, Shape mask) {
 		for (SpotlightEntity s : spotlights) {
 			// Check the distance from spotlights for optimization here!
 			if (s.getMask().intersects(mask) || s.getMask().contains(mask)) {
 				return s;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public void render(Graphics g) {
 		super.render(g);
 		anim.draw(position.x, position.y, width, height);
 	}
 
 	@Override
 	protected void initTextures() {
 		SpriteSheet ss = new SpriteSheet(TextureManager.getTexture("textures/zombie-anim.gif"), 16, 16);
		int[] frames = {0, 0, 1, 0, 0, 0, 2, 0};
		int[] durations = {130, 200, 130, 200};
		anim = new Animation(ss, frames, durations);
		anim.setAutoUpdate(false);
 	}
 
 	@Override
 	public Shape getMask() {
 		mask.setLocation(position);
 		return mask;
 	}
 	
 	private Shape getNextMask(int delta) {
 		float speed;
 		if (this.state == "pursuePlayer" || this.state == "pursueSurvivor")
 			speed = chasingSpeed;
 		else
 			speed = walkingSpeed;
 		Vector2f nextPos = position.copy().add(facing.normalise().scale(speed*delta));
 		return new Rectangle(nextPos.x, nextPos.y, mask.getWidth(), mask.getHeight());
 	}
 	
 	private void die(int delta, PlayerEntity player) {
 		life -= delta;
 		if (life < 0) {
 			delete();
 			player.score += scoreValue;
 		}
 	}
 
 }
