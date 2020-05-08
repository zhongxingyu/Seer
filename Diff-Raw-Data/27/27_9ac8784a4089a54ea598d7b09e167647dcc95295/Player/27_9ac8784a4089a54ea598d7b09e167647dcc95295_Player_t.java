 package entities.players;
 
 import game.config.Config;
 import items.Sword;
 import items.Weapon;
 
 import java.awt.Dimension;
 import java.util.Map;
 
 import map.Cell;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.geom.Rectangle;
 
 import sounds.SoundGroup;
 import sounds.Sounds;
 import utils.MapLoader;
 import entities.Entity;
 import entities.players.abilities.AbilityFinder;
 import entities.players.abilities.IPlayerAbility;
 
 public class Player extends Entity {
 	
 	private static final Dimension PLAYER_DEFAULT_SIZE = new Dimension(1, 1);
 	private static final int PLAYER_DEFAULT_MAXHEALTH = 100;
 	
 	private final Animation left, right, leftPause, rightPause;
 	private Animation sprite;
 	private final Map<String, IPlayerAbility> abilities = AbilityFinder.initialiseAbilities();
 	private static final Sound SOUND_JUMP = Sounds.loadSound("jump.ogg");
 	
 	private static SoundGroup SOUND_LANDING; 
 	//TODO ^ Why is this not being used anywhere? 
 	//TODO ^ Because if it is enabled it crashes if you try to jump just as you hit the ground; When it is disabled as it is now and you jump just as you hit the ground, the jump sound is being played but it looks as if double_jump is performed because player moves only slightly up; I belive there is something wrong with the isOnGround or jumping method
 	
 	
 	private float speed = 0.3f;
 	private Weapon sword;
 	private boolean onGround = true;
 	private boolean isRight = true;
 
 	public Player(float x, float y, int width, int height, int maxhealth) {
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
 			sword = new Sword(new Rectangle(1,1,1,1), new Image[]{new Image("data/images/sword/right0.png")}, 5);
 			
 			SOUND_LANDING = new SoundGroup("player/landing");
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
 	public void update(Input input) {
 		
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
 			moveX(-speed);
 			sprite = left;
 			isRight = false;
 			sprite.update(DELTA);
 		}
 		else if (input.isKeyDown(Input.KEY_D) || input.isKeyDown(Input.KEY_RIGHT)) {
 			moveX(speed);
 			sprite = right;
 			isRight = true;
 			sprite.update(DELTA);
 		}
 		else if (!input.isKeyPressed(Input.KEY_SPACE))
 		{
 			if (sprite == left)
 			{
 				sprite = leftPause;
 			}
 			else if (sprite == right)
 			{
 				sprite = rightPause;
 			}
 		}
 		if (input.isKeyPressed(Input.KEY_W)) {
 		    sword.attack(this);
 		}
 		if (input.isKeyPressed(Input.KEY_S)) {
 		    useAbility("rangedattack");
 		}
 		if (input.isKeyPressed(Input.KEY_E)){
 			useAbility("speeddash");
 		}
 		if (input.isKeyPressed(Input.KEY_Q)){
 			useAbility("forwardteleport");
 		}
 		
 		if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)){
			translateSmooth(10, input.getMouseX()/32f + getWidth()/2f, input.getMouseY()/32f + getHeight()/2f);
 		}
 		
 		if (!onGround && this.isOnGround()){
 			//SOUND_LANDING.playSingle(1.0f, 0.3f * this.getdY());
 		}
 		onGround = this.isOnGround();
 		
 		sword.update(DELTA, MapLoader.getCurrentCell().getEntities(), this);
 		updateTranslateSmooth();
 		frameMove();
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
 		if (getX() >= currentCell.getWidth() - 2 && getdX() > 0) {
 			currentCell = MapLoader.setCurrentCell(this,MapLoader.getCurrentX() + 1, MapLoader.getCurrentY());
 			setPosition(1, getY());
 		}
 		//bottom
 		if (getY() >= currentCell.getHeight() - 2 && getdY() > 0) {
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
 	public void render() {
 		sprite.draw((int)((getX()-1)*Config.getTileSize() - 4), (int)((getY()-1)*Config.getTileSize() - 25), new Color(255,255,255));
 		
 		if (sword != null && sword.used()) {
 		    sword.render();
 		}
 		// Health bar above player
 		new Graphics().fillRect(getX()*32 - 32, getY()*32 - 32 - 25, 32*getHealth()/100, 3);
 	}
 }
