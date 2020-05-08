 package entities.players;
 
 import items.Sword;
 import items.Weapon;
 
 import java.awt.Dimension;
 import java.util.Map;
 
 import map.Cell;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.StateBasedGame;
 
 import sounds.SoundGroup;
 import sounds.Sounds;
 import utils.MapLoader;
 import entities.Entity;
 import entities.players.abilities.AbilityFinder;
 import entities.players.abilities.IPlayerAbility;
 
 public class Player extends Entity {
 	
 	private static final int PLAYER_DEFAULT_LAYER = 0;
 	private static final Dimension PLAYER_DEFAULT_SIZE = new Dimension(1, 1);
 	private static final int PLAYER_DEFAULT_MAXHEALTH = 100;
 	
 	private final Animation left, right, leftPause, rightPause;
 	private Animation sprite;
 	private final Map<String, IPlayerAbility> abilities = AbilityFinder.initialiseAbilities();
 	private static final Sound SOUND_JUMP = Sounds.loadSound("jump.ogg");
 	SoundGroup footsteps;
 	
 	private static SoundGroup SOUND_LANDING; 
 	
 	private float speed = 0.3f;
 	private Weapon sword;
 	private boolean onGround = true;
 	private boolean isRight = true;
 
 	public Player(float x, float y, float width, float height, int maxhealth) {
 		super(x,y, width,height, maxhealth);
 		//Image[] movementRight = null;
 		Image movementRightRaw = null;
 		Image movementLeftRaw = null;
 		SpriteSheet movementRightSheet = null;
 		SpriteSheet movementLeftSheet = null;
 		//Image[] movementLeft = null;
 		
 		try {
 			movementRightRaw = new Image("data/images/walk3.png");
 			movementLeftRaw = movementRightRaw.getFlippedCopy(true, false);
 			movementRightSheet = new SpriteSheet(movementRightRaw, 40, 60);
 			movementLeftSheet = new SpriteSheet(movementLeftRaw, 40, 60);
 			//movementLeft = new Image[]{new Image("data/images/dvl1_lf1.png"), new Image("data/images/dvl1_lf2.png")};
 			
 			//temp weapon
 			//I know the file names suck
 			sword = new Sword(new Rectangle(1,1,1,1), new Image[]{
 				new Image("data/images/stick/stick_0010_Vector-Smart-Object-copy-4.png"),
 				new Image("data/images/stick/stick_0009_Vector-Smart-Object-copy-5.png"),
 				new Image("data/images/stick/stick_0008_Vector-Smart-Object-copy-6.png"),
 				new Image("data/images/stick/stick_0007_Vector-Smart-Object-copy-7.png"),
 				new Image("data/images/stick/stick_0006_Vector-Smart-Object-copy-8.png"),
 				new Image("data/images/stick/stick_0005_Vector-Smart-Object-copy-9.png"),
 				new Image("data/images/stick/stick_0004_Vector-Smart-Object-copy-10.png"),
 				new Image("data/images/stick/stick_0003_Vector-Smart-Object-copy-11.png"),
 				new Image("data/images/stick/stick_0002_Vector-Smart-Object-copy-12.png"),
 				new Image("data/images/stick/stick_0001_Vector-Smart-Object-copy-13.png"),
 				new Image("data/images/stick/stick_0000_Vector-Smart-Object-copy-14.png")},			
 					5);
 			
 			SOUND_LANDING = new SoundGroup("player/landing");
 			footsteps = new SoundGroup("player/footsteps/grass");
 		} catch (SlickException e) {
 			//do shit all
 		}
 		//int[] duration = {200,200};
 		right = new Animation(movementRightSheet, 22);
 		//left = new Animation(movementLeft, duration, false);
 		left = new Animation(movementLeftSheet, 22); //TODO: make it left
 		rightPause = new Animation(movementRightSheet, 0, 0, 0, 0, true, 1000, false);
 		leftPause = new Animation(movementLeftSheet, 0, 0, 0, 0, true, 1000, false);
 		sprite = rightPause;
 		
 	}
 
 	public Player(float x, float y) {
 		this(x,y,PLAYER_DEFAULT_SIZE.width,PLAYER_DEFAULT_SIZE.height,PLAYER_DEFAULT_MAXHEALTH);
 	}
 
 	@Override
 	public Player clone() {
 		return new Player(getX(), getY(), getWidth(), getHeight(),getMaxHealth());
 	}
 	
 	/**
 	 * Checks if the player has the ability, if the player does the ability is 'used'. 
 	 * @param key The name of the ability (equal to the name of the ability class without the "Ability.java" bit on the end.<br />
 	 * For example, DoubleJumpAbility.java would be referenced to be calling useAbility("DoubleJump").<br />
 	 * The key is not case sensitive.
 	 */
 	public void useAbility(String key)
 	{
 		//lookup the key in the allowed abilities
 		//if the key is in the map then ability.use(this)
 		IPlayerAbility tempability = abilities.get(key.toLowerCase());
 		if(tempability != null)
 		{
 			tempability.use(this);
 		}
 		
 	}
 	
 	private void playerJump() {
 		useAbility("doublejump");
 		if (isOnGround()) {
 			super.jump();
 			Sounds.play(SOUND_JUMP, 1.0f, 0.3f);
 		}
 	}
 
 	/**
 	 * This is the only method that needs to be called to update the player.
 	 * Input is assumed to already have been checked for being pressed
 	 * the previous frame.
 	 * @param input The keyboard input at the time of this frame.
 	 * @param delta The time in microseconds since the last update.
 	 */
 	@Override
 	  
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) {
 		Input input = gc.getInput();
         if (isDead()) {
             //MapLoader.getCurrentCell().removeEntity(this);
         	this.takeDamage(-100); // auto-consume-reviving-potion upon the event of death
             return;
         }
 		if (input.isKeyPressed(Input.KEY_SPACE)) {
 			playerJump();
 			
 			if (sprite == left)
 			{
 				sprite = leftPause;
 			}
 			else if (sprite == right)
 			{
 				sprite = rightPause;
 			}
 			
 		}
 		if (input.isKeyDown(Input.KEY_A) || input.isKeyDown(Input.KEY_LEFT)) {
 			accelerate(-speed,0f);
 			sprite = left;
 			isRight = false;
 			sprite.update(DELTA);
 		}
 		else if (input.isKeyDown(Input.KEY_D) || input.isKeyDown(Input.KEY_RIGHT)) {
 			accelerate(speed,0f);
 			sprite = right;
 			isRight = true;
 			sprite.update(DELTA);
 		}
 		else if (!input.isKeyPressed(Input.KEY_SPACE)) {
 			if (sprite == left)
 			{
 				sprite = leftPause;
 			}
 			else if (sprite == right)
 			{
 				sprite = rightPause;
 			}
 		}
 		if (input.isKeyPressed(Input.KEY_W) || input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
 		    sword.attack(this);
 		}
 		if (input.isKeyPressed(Input.KEY_S) || input.isMouseButtonDown(Input.MOUSE_RIGHT_BUTTON)) {
 		    useAbility("rangedattack");
 		}
 		if (input.isKeyPressed(Input.KEY_E)){
 			useAbility("speeddash");
 		}
 		if (input.isKeyPressed(Input.KEY_Q)){
 			useAbility("forwardteleport");
 		}
 		
 		if (input.isMouseButtonDown(Input.MOUSE_MIDDLE_BUTTON)){
 			translateSmooth(10, input.getMouseX()/32f + getWidth()/2f, input.getMouseY()/32f + getHeight()/2f);
 		}
 		
 		footsteps.playRandom(gc, this, 150, 0.8f, 0.2f, 0.05f, 0.02f);
 		
 		sword.update(DELTA, MapLoader.getCurrentCell().getEntities(), this);
 		
 		updateTranslateSmooth();
 		frameMove();
 		
 		//TODO: changing this back to this.isOnGround in if statement fixes crashes for me..
 		boolean newOnGround = isOnGround();
 		if (!onGround && newOnGround){
 			SOUND_LANDING.playSingle(1.0f, 0.3f * this.getdY());
 		}
 		onGround = newOnGround;
 		
 //		if (!onGround && this.isOnGround()){
 //			SOUND_LANDING.playSingle(1.0f, 0.3f * this.getdY());
 //		}
 //		
 		checkMapChanged();
 	}
 	
 	/**
 	 * Checks the player's x and y position to see if they have reached the edge of the map. 
 	 * @return -1 for no change, 0 for up, 1 for right, 2 for down, 3 for left
 	 */
 	public void checkMapChanged() {
 		Cell currentCell = MapLoader.getCurrentCell();
 		//check top
 		if (getY() < 1 && getdY() < 0) {
 			currentCell = MapLoader.setCurrentCell(this,MapLoader.getCurrentX(), MapLoader.getCurrentY() - 1);
 			setPosition(getX(), currentCell.getHeight() - getHeight() - 1);
 		}
 		//right
 		if (getX() >= currentCell.getWidth() - 1 - getWidth() && getdX() > 0) {
 			currentCell = MapLoader.setCurrentCell(this,MapLoader.getCurrentX() + 1, MapLoader.getCurrentY());
 			setPosition(1, getY());
 		}
 		//bottom
 		if (getY() >= currentCell.getHeight() - 1 - getHeight() && getdY() > 0) {
 			currentCell = MapLoader.setCurrentCell(this,MapLoader.getCurrentX(), MapLoader.getCurrentY() + 1);
 			setPosition(getX(), 1);
 		}
 		//left
 		if (getX() < 1 && getdX() < 0) {
 			currentCell = MapLoader.setCurrentCell(this,MapLoader.getCurrentX() - 1, MapLoader.getCurrentY());
 			setPosition(currentCell.getWidth() - getWidth() - 1, getY());
 		}
 	}
 	
 	/** 
 	 * returns -1 for left, +1 for right
 	 */
 	public int getDirection() {
 	    if (isRight) {
 	        return 1;
 	    } else {
 	        return -1;
 	    }
 	}
 	
 	
 	
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) {
 		//sprite.draw((int)((getX()-1)*Config.getTileSize() - 4), (int)((getY()-1)*Config.getTileSize() - 25), new Color(255,255,255));
 		
		renderSprite(sprite, -4f, -25f);
 		
 		if (sword != null && sword.used()) {
 		    sword.render();
 		}
 		// Health bar above player
		renderHealthBar(-15f);
 	}
 
 	@Override
 	public void collide(Entity e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public int getLayer() {
 		return PLAYER_DEFAULT_LAYER;
 	}
 }
