 package entity;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.SpriteSheet;
 
 import util.Resource;
 import input.Keys;
 
 public class Player extends Entity {

 	private State state;
 
 	public enum State {
 		standing, walking, pushing
 	};
 
 	private Direction direction;
 
 	public enum Direction {
 		north, east, south, west
 	};
 
 	/**
 	 * Standing animations.
 	 */
 	Animation standingNorth;
 	Animation standingEast;
 	Animation standingSouth;
 	Animation standingWest;
 
 	/**
 	 * Walking animations.
 	 */
 	Animation walkingNorth;
 	Animation walkingEast;
 	Animation walkingSouth;
 	Animation walkingWest;
 
 	private static final float WALKING_SPEED = 0.0992f;
 
 	/**
 	 * Keys associated with the player.
 	 */
 	Keys keys;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param x
 	 * @param y
 	 * @param width
 	 * @param height
 	 * @param keys
 	 */
 	public Player(float x, float y, float width, float height, Keys keys) {
 		super(x, y, width, height);
 		this.keys = keys;
 		init();
 	}
 
 	private void init() {
 		state = State.standing;
 		direction = Direction.south;
 
 		SpriteSheet player = Resource.getSpriteSheet("/res/image/player.png",
 				32, 32);
 
 		standingNorth = new Animation();
 		standingEast = new Animation();
 		standingSouth = new Animation();
 		standingWest = new Animation();
 
 		walkingNorth = new Animation();
 		walkingEast = new Animation();
 		walkingSouth = new Animation();
 		walkingWest = new Animation();
 
		int off = 5;

 		standingNorth.addFrame(player.getSprite(7, off), 100);
 		standingEast.addFrame(player.getSprite(4, off), 100);
 		standingSouth.addFrame(player.getSprite(1, off), 100);
 		standingWest.addFrame(player.getSprite(10, off), 100);
 
 		walkingNorth.addFrame(player.getSprite(6, off), 100);
 		walkingNorth.addFrame(player.getSprite(8, off), 100);
 		walkingEast.addFrame(player.getSprite(3, off), 100);
 		walkingEast.addFrame(player.getSprite(5, off), 100);
 		walkingSouth.addFrame(player.getSprite(0, off), 100);
 		walkingSouth.addFrame(player.getSprite(2, off), 100);
 		walkingWest.addFrame(player.getSprite(9, off), 100);
 		walkingWest.addFrame(player.getSprite(11, off), 100);
 
 	}
 
 	public Direction getDirection() {
 		return direction;
 	}
 
 	public State getState() {
 		return state;
 	}
 
 	private void setDirection(Direction direction) {
 		this.direction = direction;
 	}
 
 	private void setState(State state) {
 		this.state = state;
 	}
 
 	@Override
 	public void update(int delta) {
 		setState(State.standing);
 
		if (keys.up.isDown() && !keys.down.isDown()) {
 			this.move(0, -WALKING_SPEED * delta);
 			setDirection(Direction.north);
 			setState(State.walking);
 		}
 
		if (keys.right.isDown() && !keys.left.isDown()) {
 			this.move(WALKING_SPEED * delta, 0);
 			setDirection(Direction.east);
 			setState(State.walking);
 		}
 
		if (keys.down.isDown() && !keys.up.isDown()) {
 			this.move(0, WALKING_SPEED * delta);
 			setDirection(Direction.south);
 			setState(State.walking);
 		}
 
		if (keys.left.isDown() && !keys.right.isDown()) {
 			this.move(-WALKING_SPEED * delta, 0);
 			setDirection(Direction.west);
 			setState(State.walking);
 		}
 
		updateAnimations(delta);
		
	}

	private void updateAnimations(long delta) {
 		standingNorth.update(delta);
 		standingEast.update(delta);
 		standingSouth.update(delta);
 		standingWest.update(delta);

 		walkingNorth.update(delta);
 		walkingEast.update(delta);
 		walkingSouth.update(delta);
 		walkingWest.update(delta);
 	}
 
 	@Override
 	public void render() {
 
 		if (state == State.standing) {
 			switch (direction) {
 			case north:
 				standingNorth.getCurrentFrame().draw(getX(), getY());
 				break;
 			case east:
 				standingEast.getCurrentFrame().draw(getX(), getY());
 				break;
 			case south:
 				standingSouth.getCurrentFrame().draw(getX(), getY());
 				break;
 			case west:
 				standingWest.getCurrentFrame().draw(getX(), getY());
 				break;
 			}
 		}
 
 		if (state == State.walking) {
 			switch (direction) {
 			case north:
 				walkingNorth.getCurrentFrame().draw(getX(), getY());
 				break;
 			case east:
 				walkingEast.getCurrentFrame().draw(getX(), getY());
 				break;
 			case south:
 				walkingSouth.getCurrentFrame().draw(getX(), getY());
 				break;
 			case west:
 				walkingWest.getCurrentFrame().draw(getX(), getY());
 				break;
 			}
 		}
 	}
 }
