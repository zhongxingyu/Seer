 package entity;
 
 import java.util.HashMap;
 
 import level.Level;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.geom.Point;
 
 import core.Main;
 import ui.Font;
 import util.Resource;
 import input.Keys;
 
 public class Player extends Entity {
 
 	/**
 	 * Player width in pixels.
 	 */
 	public static final int WIDTH = 32;
 
 	/**
 	 * Player height in pixels.
 	 */
 	public static final int HEIGHT = 32;
 
 	/**
 	 * State of the player. Used for animations.
 	 */
 	private State state;
 
 	public enum State {
 		standing, walking, sliding
 	};
 
 	/**
 	 * The direction the player is facing.
 	 */
 	private Direction direction;
 
 	public enum Direction {
 		north, east, south, west
 	};
 
 	private HashMap<Integer, Animation> animations = new HashMap<Integer, Animation>();
 
 	static final int ANIMATION_STANDING_NORTH = 100;
 	static final int ANIMATION_STANDING_EAST = 101;
 	static final int ANIMATION_STANDING_SOUTH = 102;
 	static final int ANIMATION_STANDING_WEST = 103;
 
 	static final int ANIMATION_WALKING_NORTH = 200;
 	static final int ANIMATION_WALKING_EAST = 201;
 	static final int ANIMATION_WALKING_SOUTH = 202;
 	static final int ANIMATION_WALKING_WEST = 203;
 
 	static final int ANIMATION_SLIDING_NORTH = 300;
 	static final int ANIMATION_SLIDING_EAST = 301;
 	static final int ANIMATION_SLIDING_SOUTH = 302;
 	static final int ANIMATION_SLIDING_WEST = 303;
 
 	int playerType;
 
 	public static final int PLAYER_TYPE_GHOST = 0;
 	public static final int PLAYER_TYPE_SKELETON = 1;
 	public static final int PLAYER_TYPE_GOBLIN = 2;
 	public static final int PLAYER_TYPE_IMP = 3;
 	public static final int PLAYER_TYPE_GENIE = 4;
 	public static final int PLAYER_TYPE_PRINCESS = 5;
 	public static final int PLAYER_TYPE_REAPER = 6;
 	public static final int PLAYER_TYPE_VIKING = 7;
 
 	/**
 	 * Walking speed.
 	 */
 	public static final float SPEED = 0.105f;
 
 	/**
 	 * Level associated with the player.
 	 */
 	Level level;
 
 	/**
 	 * Indicator of player's movement.
 	 */
 	private boolean moving = false;
 
 	/**
 	 * Destination coordinates when the player is moving.
 	 */
 	private Point destination = null;
 
 	/**
 	 * Time at which was performed last action.
 	 */
 	private long lastAction;
 
 	/**
 	 * Time in milliseconds between any action is available. Triggered when
 	 * performing any action.
 	 */
 	private static final long ACTION_COOLDOWN = 260;
 
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
 	public Player(float x, float y, float width, float height, Keys keys,
 			Level level, int playerType) {
 		super(x, y, width, height);
 		this.keys = keys;
 		this.level = level;
 		this.playerType = playerType;
 
 		state = State.standing;
 		direction = Direction.south;
 		activateCooldown();
 
 		loadAnimations();
 		setVisible(true);
 	}
 
 	private void loadAnimations() {
 		SpriteSheet player = Resource.getSpriteSheet("res/image/player.png",
 				32, 32);
 
 		// STANDING NORTH
 		Animation standingNorth = new Animation();
 		standingNorth.addFrame(player.getSprite(7, playerType), 100);
 		animations.put(ANIMATION_STANDING_NORTH, standingNorth);
 
 		// STANDING EAST
 		Animation standingEast = new Animation();
 		standingEast.addFrame(player.getSprite(4, playerType), 100);
 		animations.put(ANIMATION_STANDING_EAST, standingEast);
 
 		// STANDING SOUTH
 		Animation standingSouth = new Animation();
 		standingSouth.addFrame(player.getSprite(1, playerType), 100);
 		animations.put(ANIMATION_STANDING_SOUTH, standingSouth);
 
 		// STANDING WEST
 		Animation standingWest = new Animation();
 		standingWest.addFrame(player.getSprite(10, playerType), 100);
 		animations.put(ANIMATION_STANDING_WEST, standingWest);
 
 		// WALKING NORTH
 		Animation walkingNorth = new Animation();
 		walkingNorth.addFrame(player.getSprite(6, playerType), 100);
 		walkingNorth.addFrame(player.getSprite(8, playerType), 100);
 		animations.put(ANIMATION_WALKING_NORTH, walkingNorth);
 
 		// WALKING EAST
 		Animation walkingEast = new Animation();
 		walkingEast.addFrame(player.getSprite(3, playerType), 100);
 		walkingEast.addFrame(player.getSprite(5, playerType), 100);
 		animations.put(ANIMATION_WALKING_EAST, walkingEast);
 
 		// WALKING SOUTH
 		Animation walkingSouth = new Animation();
 		walkingSouth.addFrame(player.getSprite(0, playerType), 100);
 		walkingSouth.addFrame(player.getSprite(2, playerType), 100);
 		animations.put(ANIMATION_WALKING_SOUTH, walkingSouth);
 
 		// WALKING WEST
 		Animation walkingWest = new Animation();
 		walkingWest.addFrame(player.getSprite(9, playerType), 100);
 		walkingWest.addFrame(player.getSprite(11, playerType), 100);
 		animations.put(ANIMATION_WALKING_WEST, walkingWest);
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
 
 	public Point getDestination() {
 		return destination;
 	}
 
 	@Override
 	public void update(int delta) {
 
 		/*
 		 * Handle input.
 		 */
 		handleInput(delta);
 
 		/*
		 * If the player is not moving reset state to standing.
 		 */
 		if (!moving) {
 			setState(State.standing);
 		}
 
 		/*
 		 * Destination point is not set. Player is moving.
 		 */
 		if (moving && destination == null) {
 
 			int targetX = (int) getX();
 			int targetY = (int) getY();
 
 			switch (direction) {
 			case north:
 				targetX = (int) getX();
 				targetY = ((int) getY()) - Player.HEIGHT;
 				break;
 			case east:
 				targetX = ((int) getX()) + Player.WIDTH;
 				targetY = (int) getY();
 				break;
 			case south:
 				targetX = (int) getX();
 				targetY = ((int) getY()) + Player.HEIGHT;
 				break;
 			case west:
 				targetX = ((int) getX()) - Player.WIDTH;
 				targetY = (int) getY();
 				break;
 			}
 			this.destination = new Point(targetX, targetY);
 		}
 
 		if (destination != null) {
 
 			/*
 			 * Destination point is set. Check if the destination point is an
 			 * open space. Cancel movement if the destination is blocked.
 			 */
 			if (level.isBlocked(destination)) {
 				destination = null;
 				moving = false;
 				setState(State.standing);
 			}
 
 			/*
			 * Check if the destination is a sliding tile & not occupied by
			 * blocked or movable. If destination is sliding, check if can slide
			 * onto next tile.
 			 */
			Point p = null;
			if (level.isMovable(destination)) {
 				switch (direction) {
 				case north:
 					p = new Point((int) destination.getX(),
 							(int) destination.getY() - Player.HEIGHT);
 					if (level.isBlocked(p) || level.isMovable(p)) {
 						destination = null;
 						moving = false;
 						setState(State.standing);
 					}
 					break;
 				case east:
 					p = new Point((int) destination.getX() + Player.WIDTH,
 							(int) destination.getY());
 					if (level.isBlocked(p) || level.isMovable(p)) {
 						destination = null;
 						moving = false;
 						setState(State.standing);
 					}
 					break;
 				case south:
 					p = new Point((int) destination.getX(),
 							(int) destination.getY() + Player.HEIGHT);
 					if (level.isBlocked(p) || level.isMovable(p)) {
 						destination = null;
 						moving = false;
 						setState(State.standing);
 					}
 					break;
 				case west:
 					p = new Point((int) destination.getX() - Player.WIDTH,
 							(int) destination.getY());
 					if (level.isBlocked(p) || level.isMovable(p)) {
 						destination = null;
 						moving = false;
 						setState(State.standing);
 					}
 					break;
 				}
 			}

 		}
 
 		/*
 		 * Player is moving. Destination point is set. Move towards the
 		 * destination point.
 		 */
 		if (moving && destination != null) {
 
 			state = State.walking;
 
 			float newX = getX();
 			float newY = getY();
 
 			switch (direction) {
 			case north:
 				newY -= SPEED * delta;
 				if (newY < destination.getY()) {
 					newY = destination.getY();
 					moving = false;
 					destination = null;
 				}
 				break;
 			case east:
 				newX += SPEED * delta;
 				if (newX > destination.getX()) {
 					newX = destination.getX();
 					moving = false;
 					destination = null;
 				}
 				break;
 			case south:
 				newY += SPEED * delta;
 				if (newY > destination.getY()) {
 					newY = destination.getY();
 					moving = false;
 					destination = null;
 				}
 				break;
 			case west:
 				newX -= SPEED * delta;
 				if (newX < destination.getX()) {
 					newX = destination.getX();
 					moving = false;
 					destination = null;
 				}
 				break;
 			}
 
 			this.moveTo(newX, newY);
 		}
 
 		/*
 		 * Update animations.
 		 */
 		updateAnimations(delta);
 	}
 
 	private void handleInput(long delta) {
 
 		if (moving) {
 			return; // bugger off if moving is in progress
 		}
 
 		if (isCooldownReady()) {
 			return; // bugger off if action cooldown hasn't finished
 		}
 
 		// UP PRESSED
 		if (keys.up.isDown() && !keys.down.isDown()) {
 			if (direction == Direction.north) {
 				if (state == State.standing || state == State.walking) {
 					setState(State.walking);
 					moving = true;
 					activateCooldown();
 				}
 			}
 
 			if (direction != Direction.north && state == State.standing) {
 				setDirection(Direction.north);
 				// activateCooldown();
 			}
 		}
 
 		// RIGHT PRESSED
 		if (keys.right.isDown() && !keys.left.isDown()) {
 			if (direction == Direction.east) {
 				if (state == State.standing || state == State.walking) {
 					setState(State.walking);
 					moving = true;
 					activateCooldown();
 				}
 			}
 
 			if (direction != Direction.east && state == State.standing) {
 				setDirection(Direction.east);
 				// activateCooldown();
 			}
 		}
 
 		// DOWN PRESSED
 		if (keys.down.isDown() && !keys.up.isDown()) {
 			if (direction == Direction.south) {
 				if (state == State.standing || state == State.walking) {
 					setState(State.walking);
 					moving = true;
 					activateCooldown();
 				}
 			}
 
 			if (direction != Direction.south && state == State.standing) {
 				setDirection(Direction.south);
 				// activateCooldown();
 			}
 		}
 
 		// LEFT PRESSED
 		if (keys.left.isDown() && !keys.right.isDown()) {
 			if (direction == Direction.west) {
 				if (state == State.standing || state == State.walking) {
 					setState(State.walking);
 					moving = true;
 					activateCooldown();
 				}
 			}
 
 			if (direction != Direction.west && state == State.standing) {
 				setDirection(Direction.west);
 				// activateCooldown();
 			}
 		}
 	}
 
 	private boolean isCooldownReady() {
 		return (lastAction + ACTION_COOLDOWN > Main.getTime());
 	}
 
 	private void activateCooldown() {
 		lastAction = Main.getTime();
 	}
 
 	/**
 	 * Updates animations.
 	 * 
 	 * @param delta
 	 *            Delta
 	 */
 	private void updateAnimations(long delta) {
 		for (Integer an : animations.keySet()) {
 			Animation animation = animations.get(an);
 			animation.update(delta);
 		}
 	}
 
 	@Override
 	public void render() {
 
 		if (!isVisible()) {
 			return;
 		}
 
 		if (Main.DEBUG) {
 			renderDebuggInfo();
 		}
 
 		if (state == State.standing) {
 			switch (direction) {
 			case north:
 				animations.get(ANIMATION_STANDING_NORTH).getCurrentFrame()
 						.draw(getX(), getY());
 				break;
 			case east:
 				animations.get(ANIMATION_STANDING_EAST).getCurrentFrame()
 						.draw(getX(), getY());
 				break;
 			case south:
 				animations.get(ANIMATION_STANDING_SOUTH).getCurrentFrame()
 						.draw(getX(), getY());
 				break;
 			case west:
 				animations.get(ANIMATION_STANDING_WEST).getCurrentFrame()
 						.draw(getX(), getY());
 				break;
 			}
 		}
 
 		if (state == State.walking) {
 			switch (direction) {
 			case north:
 				animations.get(ANIMATION_WALKING_NORTH).getCurrentFrame()
 						.draw(getX(), getY());
 				break;
 			case east:
 				animations.get(ANIMATION_WALKING_EAST).getCurrentFrame()
 						.draw(getX(), getY());
 				break;
 			case south:
 				animations.get(ANIMATION_WALKING_SOUTH).getCurrentFrame()
 						.draw(getX(), getY());
 				break;
 			case west:
 				animations.get(ANIMATION_WALKING_WEST).getCurrentFrame()
 						.draw(getX(), getY());
 				break;
 			}
 		}
 	}
 
 	private void renderDebuggInfo() {
 		// PLAYER COORDS
 		String simpleCoords = Integer.toString((int) this.getX() / 32) + "-"
 				+ Integer.toString((int) this.getY() / 32);
 		String coords = Integer.toString((int) this.getX()) + "-"
 				+ Integer.toString((int) this.getY());
 
 		Font.renderText(coords,
 				getX() + WIDTH / 2 - Font.getMessageWidth(coords) / 2,
 				getY() + 49);
 		Font.renderText(simpleCoords,
 				getX() + WIDTH / 2 - Font.getMessageWidth(simpleCoords) / 2,
 				getY() + 40);
 
 		// PLAYER STATE
 		String stateString = "";
 		switch (state) {
 		case standing:
 			stateString = "standing";
 			break;
 		case walking:
 			stateString = "walking";
 			break;
 		case sliding:
 			stateString = "sliding";
 			break;
 		}
 
 		Font.renderText(stateString,
 				getX() + WIDTH / 2 - Font.getMessageWidth(stateString) / 2,
 				getY() + -10);
 
 	}
 
 }
