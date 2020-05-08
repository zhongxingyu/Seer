 package rsmg.controller;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.Renderable;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.BlobbyTransition;
 
 import rsmg.model.Bullet;
 import rsmg.model.Character;
 import rsmg.model.Enemy;
 import rsmg.model.Level;
 import rsmg.model.Tankbot;
 
 /**
  * The state where the levels are played out.
  * @author Daniel Jonsson
  *
  */
 public class LevelState extends State {
 
 	
 	/**
 	 * The background image behind the tile grid.
 	 */
 	private Image background;
 	
 	/**
 	 * The tiles building up the environment.
 	 */
 	private Image airTile;
 	private Image boxTile;
 	private Image laserBullet;
 	
 	/**
 	 * The character that the player controls.
 	 */
 	private Renderable character;
 	private Animation characterRunningR;
 	private Animation characterRunningL;
 	private Image characterStandingR;
 	private Image characterStandingL;
 	private Image characterJumpingR;
 	private Image characterJumpingL;
 	private Image tankbot;
 	/**
 	 * Reference to the level model.
 	 */
 	private Level level;
 	
 	/**
 	 * Track if the up key is down or not.
 	 */
 	private boolean upKeyIsDown;
 	
 	/**
 	 * Store how much everything should be scaled in the view.
 	 */
 	private int scale;
 	
 	/**
 	 * Construct the level.
 	 * @param stateID The ID to the state.
 	 */
 	public LevelState(int stateID) {
 		super(stateID);
 	}
 	
 	/**
 	 * Initialize level images and the level model.
 	 */
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 
 		scale = (int) ((float)gc.getWidth() / 480);
 		
 		background = new Image("res/sprites/level/bg.jpg", false, Image.FILTER_NEAREST).getScaledCopy(scale);
 		
 		airTile = new Image("res/sprites/level/airTile.png", false, Image.FILTER_NEAREST).getScaledCopy(scale);
 		boxTile = new Image("res/sprites/level/boxTile.png", false, Image.FILTER_NEAREST).getScaledCopy(scale);
 		laserBullet = new Image("res/sprites/level/laserBullet.png", false, Image.FILTER_NEAREST).getScaledCopy(scale);
 		
 		/**
 		 * Make an animation for when the character is running to the right.
 		 */
 		Image characterImage = new Image("res/sprites/level/charPistolRunningSheet.png", false, Image.FILTER_NEAREST);
 		SpriteSheet characterSheet = new SpriteSheet(characterImage.getScaledCopy(scale), 32*scale, 23*scale);
 		characterRunningR = new Animation(characterSheet, 140);
 		
 		/**
 		 * Make an animation for when the character is running to the left.
 		 */
 		characterImage = characterImage.getFlippedCopy(true, false);
 		characterSheet = new SpriteSheet(characterImage.getScaledCopy(scale), 32*scale, 23*scale);
 		characterRunningL = new Animation(characterSheet, 140);
 		
 		/**
 		 * Make an image for when the character is standing still facing the right.
 		 */
 		characterStandingR = new Image("res/sprites/level/charPistolStanding.png", false, Image.FILTER_NEAREST).getScaledCopy(scale);
 		
 		/**
 		 * Make an image for when the character is standing still facing to the left.
 		 */
 		characterStandingL = characterStandingR.getFlippedCopy(true, false);
 		
 		/**
 		 * Make an image for when the character is standing still facing the right.
 		 */
 		character = characterJumpingR = new Image("res/sprites/level/charPistolJumping.png", false, Image.FILTER_NEAREST).getScaledCopy(scale);
 		
 		/**
 		 * Make an image for when the character is standing still facing to the left.
 		 */
 		characterJumpingL = characterJumpingR.getFlippedCopy(true, false);
 		/**
 		 * create an image for how the tankBot looks
 		 */
 		tankbot = new Image("res/sprites/level/tankbot.png", false, Image.FILTER_NEAREST);
 		
 		/**
 		 * Create the level model.
 		 */
 		level = new Level();
 	}
 
 	@Override
 	public void enter(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		super.enter(gc, sbg);
 		gc.setMusicOn(false);
 	}
 
 	@Override
 	public void leave(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		super.leave(gc, sbg);
 		gc.setMusicOn(true);
 	}
 
 	/**
 	 * Draw everything from the game model on the screen.
 	 */
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 
 		drawBackground();
 		drawEnvironment();
 		drawCharacter();
 		drawBullets();
 		drawEnemies();
 	}
 
 	/**
 	 * Draw a background image behind the tile grid.
 	 */
 	private void drawBackground() {
 		background.draw(0, 0);
 	}
 	
 	/**
 	 * Draw bullets on the screen.
 	 */
 	private void drawBullets() {
 		ArrayList<Bullet> bulletList = level.getABulletList();
 		for(Bullet bullet : bulletList)
 			laserBullet.draw((float)bullet.getX()*scale, (float)bullet.getY()*scale);
 	}
	
	/**
	 * Draw enemies on the screen.
	 */
 	private void drawEnemies() {
 		ArrayList<Enemy> enemies = level.getEnemies();
		for (Enemy enemy : enemies) {
			if (enemy instanceof Tankbot)
				tankbot.draw((float)enemy.getX()*scale, (float)enemy.getY()*scale);
 		}
 	}
 	
 	/**
 	 * Draw the environment which consists of the tiles.
 	 */
 	private void drawEnvironment() {
 		for (int y = 0; y < level.getTileGrid().getHeight(); y++) {
 			for (int x = 0; x < level.getTileGrid().getWidth(); x++) {
 				if (level.getTileGrid().getFromCoord(x, y).isSolid())
 					boxTile.draw(x*32*scale, y*32*scale);
 			}
 		}
 	}
 
 	/**
 	 * Draw the character/protagonist on the screen.
 	 */
 	private void drawCharacter() {
 		character.draw(((float)level.getCharacter().getX()-6)*scale, (float)level.getCharacter().getY()*scale);
 	}
 
 	/**
 	 * Handle inputs from the user and update the model.
 	 */
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 		
 		handleKeyboardEvents(gc.getInput(), sbg);
 		
 		/**
 		 * Update the model and give it the time that has passed since last
 		 * update as seconds.
 		 */
 		level.update((double)delta / 1000);
 		
 		/**
 		 * Fix so the right character sprite is shown on the next render()
 		 */
 		if (level.getCharacter().isAirborne()) {
 
 			if (level.getCharacter().isFacingRight())
 				character = characterJumpingR;
 			else
 				character = characterJumpingL;
 
 		} else if (level.getCharacter().isStandingStill()) {
 
 			if (level.getCharacter().isFacingRight())
 				character = characterStandingR;
 			else
 				character = characterStandingL;
 
 		} else { // is running
 
 			if (level.getCharacter().isFacingRight())
 				character = characterRunningR;
 			else
 				character = characterRunningL;
 
 		}
 		
 		/**
 		 * Play a sound if the character has fired his weapon.
 		 */
 		if (level.getCharacter().getWeapon().shot())
 			new Sound("res/sounds/shot.wav").play();
 	}
 	
 	/**
 	 * Handle keyboard events.
 	 * @param input
 	 */
 	public void handleKeyboardEvents(Input input, StateBasedGame sbg) {
 		
 		Character modelCharacter = level.getCharacter();
 		
 		if (input.isKeyDown(Input.KEY_LEFT))
 			modelCharacter.moveLeft();
 		else if (input.isKeyDown(Input.KEY_RIGHT))
 			modelCharacter.moveRight();
 
 		if (input.isKeyDown(Input.KEY_UP)) {
 			if (!upKeyIsDown)
 				modelCharacter.jump();
 			upKeyIsDown = true;
 		} else if (upKeyIsReleased())
 			modelCharacter.jumpReleased();
 
 		if (input.isKeyPressed(Input.KEY_SPACE))
 			modelCharacter.attack();
 		
 		if (input.isKeyPressed(Input.KEY_ESCAPE)) {
 			sbg.enterState(Controller.LEVEL_SELECTION_STATE, null, new BlobbyTransition());
 		}
 		if (input.isKeyPressed(Input.KEY_X)){
 			modelCharacter.setDashing(true);
 		}
 	}
 
 	/**
 	 * Returns if the up key has been released since last loop.
 	 * @return If the up key has been released.
 	 */
 	private boolean upKeyIsReleased() {
 		if (upKeyIsDown) {
 			upKeyIsDown = false;
 			return true;
 		}
 		return false;
 	}
 }
