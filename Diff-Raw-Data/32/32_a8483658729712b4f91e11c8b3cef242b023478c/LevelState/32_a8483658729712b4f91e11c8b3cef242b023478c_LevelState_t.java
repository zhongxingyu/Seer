 package rsmg.controller;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.lwjgl.Sys;
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.Renderable;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.FadeInTransition;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 
 import rsmg.io.CharacterProgress;
 import rsmg.io.Levels;
 import rsmg.levelfactory.LevelFactory;
 import rsmg.model.Level;
 import rsmg.model.object.bullet.Bullet;
 import rsmg.model.object.bullet.RotatableBullet;
 import rsmg.model.object.item.Item;
 import rsmg.model.object.unit.Enemy;
 import rsmg.model.object.unit.PCharacter;
 import rsmg.model.object.unit.weapon.Weapon;
 import rsmg.model.variables.Constants;
 import rsmg.model.variables.ObjectName;
 
 /**
  * The state where the levels are played out.
  * 
  * @author Daniel Jonsson
  * @author Johan Grnvall
  * @author Johan Rigns
  *
  */
 class LevelState extends State {
 
 	/**
 	 * The background image behind the tile grid.
 	 */
 	private Image background;
 	
 	/**
 	 * The health bar images.
 	 */
 	private Image healthBar;
 	private Rectangle healthBarOverlayRectangle;
 	private Graphics healthBarOverlayGraphics;
 	
 	/**
 	 * The gun cooldown bar images.
 	 */
 	private Image weaponCooldownBar;
 	private Rectangle weaponCooldownBarOverlayRectangle;
 	private Graphics weaponCooldownBarOverlayGraphics;
 	
 	/**
 	 * Maps containing images.
 	 */
 	private Map<ObjectName, Renderable> tiles;
 	private Map<ObjectName, Renderable> bullets;
 	private Map<ObjectName, Renderable> items;
 	private Map<ObjectName, Renderable> enemies;
 	
 	/**
 	 * A class representing the character on the screen.
 	 * This inner class handles which character image that should be drawn on a given time.
 	 */
 	private CharacterImage character;
 	
 	/**
 	 * Reference to the level model.
 	 */
 	private Level level;
 
 	/**
 	 * Track if the up key is down or not.
 	 */
 	private boolean upKeyIsDown;
 	
 	/**
 	 * Track if the right key is down or not
 	 */
 	private boolean rightKeyIsDown;
 	
 	/**
 	 * Track if the left key is down or not
 	 */
 	private boolean leftKeyIsDown;
 	
 	/**
 	 * Store how much everything should be scaled in the view.
 	 */
 	private int scale;
 	
 	/**
 	 * Store the level number.
 	 */
 	private int levelNumber;
 	
 	/**
 	 * Camera coordinates for placing all object except the Character
 	 */
 	private float cameraX;
 	private float cameraY;
 	
 	/**
 	 * Camera coordinates for placing the Character
 	 */
 	private float characterX;
 	private float characterY;
 
 	/**
 	 * Number of tiles that are visible on the screen's width.
 	 */
 	private int numberOfTilesVisibleX;
 
 	/**
 	 * Number of tiles that are visible on the screen's height.
 	 */
 	private int numberOfTilesVisibleY;
 
 	/**
 	 * Construct the level.
 	 * @param stateID The ID to the state.
 	 */
 	LevelState(int stateID) {
 		super(stateID);
 	}
 	
 	/**
 	 * Initialize level images and the level model.
 	 */
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		
 		scale = (int) ((float)gc.getWidth() / 480);
 		int filter = Image.FILTER_NEAREST;
 		String folderPath = "res/sprites/level/";
 
 		// Store the maximum number of tiles that can possibly fit on the width
 		// and height of the screen. This is used to reduce the number of tiles
 		// that are drawn to only the visible ones.
 		numberOfTilesVisibleX = gc.getWidth() / Constants.TILESIZE / scale + 2;
 		numberOfTilesVisibleY = gc.getHeight() / Constants.TILESIZE / scale + 2;
 		
 		/**
 		 * The background image in the level.
 		 */
 		background = new Image(folderPath+"bg.jpg", false, filter).getScaledCopy(scale);
 		
 		healthBar = new Image(folderPath+"healthBar.png", false, filter);
 		healthBarOverlayRectangle = new Rectangle(23, 23, 147, 17);
 		healthBarOverlayGraphics = new Graphics();
 		healthBarOverlayGraphics.setColor(new Color(0.85f, 0.3f, 0.3f, 0.5f));
 		
 		weaponCooldownBar = new Image(folderPath+"cooldownBar.png", false, filter);
 		weaponCooldownBarOverlayRectangle = new Rectangle(23, 53, 147, 17);
 		weaponCooldownBarOverlayGraphics = new Graphics();
 		weaponCooldownBarOverlayGraphics.setColor(new Color(0.85f, 0.85f, 0.85f, 0.5f));
 		
 		/**
 		 * Create the character image.
 		 */
 		character = new CharacterImage(folderPath, scale, filter);
 		
 		/**
 		 * Create a map with all enemy images / Animations.
 		 */
 		enemies = new EnumMap<ObjectName, Renderable>(ObjectName.class);
 		Image tankbot = new Image(folderPath+"tankBot.png", false, filter).getScaledCopy(scale);
 		Image bucketBot = new Image(folderPath+"bucketBot.png", false, filter).getScaledCopy(scale);
 		Image ballBot = new Image(folderPath+"ballBot.png", false, filter).getScaledCopy(scale);
 		Animation rocketBot = new Animation(new SpriteSheet(new Image(
 				folderPath+"rocketBot.png", false, filter).getScaledCopy(scale), 19*scale, 25*scale), 200);
 		Image spikes = new Image(folderPath+"spikes.png", false, filter).getScaledCopy(scale);
 		Image miniBallBot = new Image(folderPath+"miniBallBot.png", false, filter).getScaledCopy(scale);
 		Image bossBot = new Image(folderPath+"bossBot.png", false, filter).getScaledCopy(scale*2);
 		
 		enemies.put(ObjectName.MINIBALLBOT, miniBallBot);
 		enemies.put(ObjectName.TANKBOT, tankbot);
 		enemies.put(ObjectName.BUCKETBOT, bucketBot);
 		enemies.put(ObjectName.BALLBOT, ballBot);
 		enemies.put(ObjectName.ROCKETBOT, rocketBot);
 		enemies.put(ObjectName.SPIKES, spikes);
 		enemies.put(ObjectName.BOSSBOT, bossBot);
 		
 		
 		/**
 		 * Create a map with all item images.
 		 */
 		items = new EnumMap<ObjectName, Renderable>(ObjectName.class);
 		Image healthPack = new Image(folderPath+"healthPack.png", false, filter).getScaledCopy(scale);	
 		Image laserPistol = new Image(folderPath+"laserPistol.png", false, filter).getScaledCopy(scale);
 		Image rocketLauncher = new Image(folderPath+"rocketLauncher.png", false, filter).getScaledCopy(scale);
 		Image upgradePoint = new Image(folderPath+"upgradePoint.png", false, filter).getScaledCopy(scale);
 		Image shotGun = new Image(folderPath+"shotGun.png", false, filter).getScaledCopy(scale);
 		items.put(ObjectName.HEALTH_PACK, healthPack);
 		items.put(ObjectName.LASER_PISTOL, laserPistol);
 		items.put(ObjectName.ROCKET_LAUNCHER, rocketLauncher);
 		items.put(ObjectName.UPGRADE_POINT, upgradePoint);
 		items.put(ObjectName.SHOTGUN, shotGun);
 		
 		/**
 		 * Create a map with all bullet images.
 		 */
 		bullets = new EnumMap<ObjectName, Renderable>(ObjectName.class);
 		Image laserBullet = new Image(folderPath+"laserBullet.png", false, filter).getScaledCopy(scale);
 		bullets.put(ObjectName.LASER_BULLET, laserBullet);
 		
 		Image shotgunBullet = new Image(folderPath+"shotgunBullet.png", false, filter).getScaledCopy(scale);
 		bullets.put(ObjectName.SHOTGUN_BULLET, shotgunBullet);
 		
 		Image stone = new Image(folderPath+"stone.png", false, filter).getScaledCopy(scale);
 		bullets.put(ObjectName.STONE, stone);
 		
 		Image pistolBullet = new Image(folderPath+"pistolBullet.png", false, filter).getScaledCopy(scale);
 		bullets.put(ObjectName.PISTOL_BULLET, pistolBullet);
 		
 		Image laserBolt = new Image(folderPath+"laserBolt.png", false, filter).getScaledCopy(scale);
 		bullets.put(ObjectName.LASERBOLT, laserBolt);
 		
 		Image laserBlast = new Image(folderPath+"laserBlast.png", false, filter).getScaledCopy(scale);
 		bullets.put(ObjectName.LASERBLAST, laserBlast);
 		
 		/**
 		 * create an animation for the rocket
 		 */
 		Image rocketImage = new Image(folderPath+"rocketSheet.png", true, filter);
 		SpriteSheet rocketSheetR = new SpriteSheet(rocketImage.getScaledCopy(scale), 15*scale, 14*scale);
 		SpriteSheet rocketSheetL = new SpriteSheet(rocketImage.getFlippedCopy(true, false).getScaledCopy(scale), 15*scale, 14*scale);
 		Animation rocketR = new Animation(rocketSheetR, 140);
 		Animation rocketL = new Animation(rocketSheetL, 140);
 		
 		bullets.put(ObjectName.ROCKETR, rocketR);
 		bullets.put(ObjectName.ROCKETL, rocketL);
 		
 		/**
 		 * create an animation for laserfire effect
 		 */
 		Image laserFireImage = new Image(folderPath+"laserFire.png", false, filter).getScaledCopy(scale*2);
 		SpriteSheet laserFireSheet = new SpriteSheet(laserFireImage, 10*2*scale, 14*2*scale);
 		Animation laserFire = new Animation(laserFireSheet, 200);
 		bullets.put(ObjectName.LASERFIRE, laserFire);
 		
 		
 		/**
 		 * create an animation for explosions
 		 */
 		Image explosionImage = new Image(folderPath+"explosion.png", false, filter).getScaledCopy(scale);
 		SpriteSheet bigExplosionSheet = new SpriteSheet(explosionImage.getScaledCopy((float)1.5), 45*scale, 45*scale);
 		SpriteSheet explosionSheet = new SpriteSheet(explosionImage, 30*scale, 30*scale);
 		
 		Animation explosion = new Animation(explosionSheet,140);
 		Animation bigExplosion = new Animation(bigExplosionSheet, 140);
 		
 		bullets.put(ObjectName.BIG_EXPLOSION, bigExplosion);
 		bullets.put(ObjectName.EXPLOSION, explosion);
 		
 		/**
 		 * Create a map with all tile images.
 		 */
 		tiles = new EnumMap<ObjectName, Renderable>(ObjectName.class);
 		Image boxTile1 = new Image(folderPath+"tiles/boxTile1.png", false, filter).getScaledCopy(scale);
 		Image boxTile2 = new Image(folderPath+"tiles/boxTile2.png", false, filter).getScaledCopy(scale);
 		Image boxTile3 = new Image(folderPath+"tiles/boxTile3.png", false, filter).getScaledCopy(scale);
 		Image boxTile4 = new Image(folderPath+"tiles/boxTile4.png", false, filter).getScaledCopy(scale);
 		Image airTile = new Image(folderPath+"tiles/airTile.png", false, filter).getScaledCopy(scale);
 		Image endTile = new Image(folderPath+"tiles/endTile.png", false, filter).getScaledCopy(scale);
 		tiles.put(ObjectName.BOX_TILE1, boxTile1);
 		tiles.put(ObjectName.BOX_TILE2, boxTile2);
 		tiles.put(ObjectName.BOX_TILE3, boxTile3);
 		tiles.put(ObjectName.BOX_TILE4, boxTile4);
 		tiles.put(ObjectName.AIR_TILE, airTile);
 		tiles.put(ObjectName.SPAWN_TILE, airTile);
 		tiles.put(ObjectName.END_TILE, endTile);
 	}
 	
 	/**
 	 * Initialize a level.
 	 * @param levelNumber The level's number.
 	 */
 	public void initLevel(int levelNumber) {
 		this.levelNumber = levelNumber;
 		level = LevelFactory.getLevel(levelNumber);
 	}
 
 	@Override
 	public void enter(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		super.enter(gc, sbg);
 	}
 
 	@Override
 	public void leave(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		super.leave(gc, sbg);
 	}
 
 	/**
 	 * Draw everything from the game model on the screen.
 	 */
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		
 		setUpCamera(gc.getWidth(), gc.getHeight());
 		
 		drawBackground();
 		drawEnvironment();
 		drawCharacter();
 		drawEnemies();
 		drawBullets();
 		drawItems();
 		drawHealthBar();
 		drawWeaponCooldownBar();
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
 		
 		for (Bullet bullet : level.getEnemyBulletList()) {
 			if(bullet instanceof RotatableBullet){
 				((Image)bullets.get(bullet.getName())).setRotation(-((float) (((RotatableBullet)bullet).getRotation()*180/Math.PI)));
 			}
 			bullets.get(bullet.getName()).draw((float)bullet.getX()*scale+cameraX, (float)bullet.getY()*scale+cameraY);
 		}
 		
 		for (Bullet bullet : level.getAlliedBulletList()) {
 			bullets.get(bullet.getName()).draw((float)bullet.getX()*scale+cameraX, (float)bullet.getY()*scale+cameraY);
 		}
 	}
 	
 	/**
 	 * Draw enemies on the screen.
 	 */
 	private void drawEnemies() {
 		for (Enemy enemy : level.getEnemies()) {
 			
 			//different facing Animations are not yet supported. 
 
 			int offsetX = 0;
 			int offsetY = 0;
 			if (enemy.getName().equals(ObjectName.BOSSBOT)) {
 				offsetX = -194;
 			}
 			//different facing Animations are not yet supported. 
 			Renderable enemyRenderable;
 			if(enemy.isFacingRight() && (enemies.get(enemy.getName()) instanceof Image)) {
 				enemyRenderable = ((Image)enemies.get(enemy.getName())).getFlippedCopy(true, false);
 			} else {
 				enemyRenderable = enemies.get(enemy.getName());
 			}
 			
 			//make the enemy flash if he recently took damage
 
 			if (enemy.recentlytookDamage()) {
 				if (enemyRenderable instanceof Animation) {
 					((Animation)enemyRenderable).drawFlash((float)enemy.getX()*scale+cameraX+offsetX, (float)enemy.getY()*scale+cameraY+offsetY, (float)enemy.getWidth()*scale, (float)enemy.getHeight()*scale);
 				} else if(enemyRenderable instanceof Image) {
 					((Image)enemyRenderable).drawFlash((float)enemy.getX()*scale+cameraX+offsetX, (float)enemy.getY()*scale+cameraY+offsetY);
 				}
 				
 			} else {
 					enemyRenderable.draw((float)enemy.getX()*scale+cameraX+offsetX, (float)enemy.getY()*scale+cameraY+offsetY);	
 			}
 		}
 	}
 	
 	/**	
 	 * Draw items on the screen.
 	 */
 	private void drawItems() {
 		for(Item item : level.getItemList())
 			items.get(item.getName()).draw((float)item.getX()*scale+cameraX, (float)item.getY()*scale+cameraY);
 	}
 	
 	/**
 	 * Draw the environment which consists of the tiles.
 	 */
 	private void drawEnvironment() {
 		
 		// These are the tiles that are visible farthest to the left and to the
 		// top of the screen.
 		int tileVisibleLeft = level.getTileGrid().getTilePosFromRealPos(-cameraX/scale);
 		int tileVisibleTop = level.getTileGrid().getTilePosFromRealPos(-cameraY/scale);
 		
 		// The tiles that are visible farthest to the right and to the bottom of
 		// the screen.
 		int tileVisibleRight = tileVisibleLeft + numberOfTilesVisibleX;
 		int tileVisibleBottom = tileVisibleTop + numberOfTilesVisibleY;
 		
 		// Check so the tiles calculated to the right and to the bottom exist in
 		// the tile grid. If not, set them to the tile grid's maximum values.
 		tileVisibleRight = (tileVisibleRight > level.getTileGrid().getWidth()) ? level.getTileGrid().getWidth() : tileVisibleRight;
 		tileVisibleBottom = (tileVisibleBottom > level.getTileGrid().getHeight()) ? level.getTileGrid().getHeight() : tileVisibleBottom;
 		
 		// Now only draw the tiles that are visible on the screen. This greatly
 		// improves the performance compared to drawing all tiles in the whole
 		// tile grid.
 		for (int y = tileVisibleTop; y < tileVisibleBottom; y++) {
 			for (int x = tileVisibleLeft; x < tileVisibleRight; x++) {
 				tiles.get(level.getTileGrid().getFromCoord(x, y).getName()).draw(x*Constants.TILESIZE*scale+cameraX, y*Constants.TILESIZE*scale+cameraY);
 			}
 		}
 	}
 
 	/**
 	 * Draw the character/protagonist on the screen.
 	 */
 	private void drawCharacter() {
 		character.draw();
 	}
 
 	/**
 	 * Draw the health bar.
 	 */
 	private void drawHealthBar() {
 		healthBar.draw(20, 20);
 		healthBarOverlayGraphics.fill(healthBarOverlayRectangle);
 	}
 
 	/**
 	 * Draw the cooldown bar for the weapon.
 	 */
 	private void drawWeaponCooldownBar() {
 		weaponCooldownBar.draw(20, 50);
 		weaponCooldownBarOverlayGraphics.fill(weaponCooldownBarOverlayRectangle);
 	}
 	
 	
 	/**
 	 * Handle inputs from the user and update the model.
 	 */
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 		
 		handleKeyboardEvents(gc.getInput(), sbg);
 		
 		/**
 		 * Play sounds: gun shots, dash.... 
 		 */
 		playSounds();
 		
 		/**
 		 * Update the model and give it the time that has passed since last
 		 * update as seconds.
 		 */
 		level.update((double)delta / 1000);
 		
 		/**
 		 * Update the character image.
 		 */
 		character.update();
 		
 		/**
 		 * Update health bar's overlay size.
 		 */
 		healthBarOverlayRectangle.setWidth(147 * level.getCharacter().getHealth() / level.getCharacter().getMaxHealth());
 		
 		/**
 		 * Update weapon cooldown bar's overlay size.
 		 */
 		weaponCooldownBarOverlayRectangle.setWidth(147 * level.getCharacter().getWeaponLoadedPercentage());
 		
 		/**
 		 * Check if the player has won the level. If he has, init the next level
 		 * into this state. But if he has won the last level, change state to
 		 * level selection state.
 		 */
 		if (level.hasWon()) {
 
 			// List of the level numbers
 			List<Integer> levelNumbers = (ArrayList<Integer>)Levels.getLevelNumbers();
 			Collections.sort(levelNumbers);
 			
 			// Check if number of unlocked of levels should be updated
 			int unlockedLevels = levelNumbers.indexOf(levelNumber)+2;
 			if (unlockedLevels > CharacterProgress.getUnlockedLevels()) {
 				CharacterProgress.setUnlockedLevels(unlockedLevels);
 				CharacterProgress.saveFile();
 			}
 			
 			// Update available upgrade points
 			if (level.getCharacter().getUpgradePoints() > 0) {
 				CharacterProgress.setUpgradePoints(CharacterProgress.getUpgradePoints()+level.getCharacter().getUpgradePoints());
 				CharacterProgress.saveFile();
 			}
 			
 			// If this was the last level
 			if (levelNumbers.indexOf(levelNumber) + 1 == levelNumbers.size()) {
 				sbg.enterState(Controller.CREDITS_STATE, new FadeOutTransition(), new FadeInTransition());
 			} else if (level.getCharacter().getUpgradePoints() > 0) { // If the player has collected upgrade points
 				MusicHandler.startTrack(MusicHandler.Track.MENU_MUSIC);
 				sbg.enterState(Controller.UPGRADES_STATE, new FadeOutTransition(), new FadeInTransition());
 			} else { // If the player hasn't collected any upgrade points
 				MusicHandler.startTrack(MusicHandler.Track.MENU_MUSIC);
 				sbg.enterState(Controller.LEVEL_SELECTION_STATE, new FadeOutTransition(), new FadeInTransition());
 			}
 			
 		} else if (level.hasLost()) {
 			Controller.initLevel(levelNumber);
 		}
 	}
 	
 	/**
 	 * Plays sounds in the game
 	 * @throws SlickException
 	 */
 	private void playSounds() throws SlickException {
 		PCharacter character = level.getCharacter();
 		
 		/**
 		 * Play sound if the character is dashing
 		 */
 		if(character.isDashing() && character.getDistanceDashed() == 0)
 			new Sound("res/sounds/dash.wav").play();
 		
 		/**
 		 * Play a sound if the character has fired his weapon.
 		 */
 		Weapon weapon = character.getWeapon();
 		boolean isShooting = weapon.shot();
 		if (isShooting && weapon.getName().equals(ObjectName.PISTOL))
 			new Sound("res/sounds/pistol.wav").play();
 		else if(isShooting && weapon.getName().equals(ObjectName.SHOTGUN))
 			new Sound("res/sounds/shotgun.wav").play();
 		else if(isShooting && weapon.getName().equals(ObjectName.ROCKET_LAUNCHER))
 			new Sound("res/sounds/rocketLauncher.wav").play();
 	}
 	
 	/**
 	 * Handle keyboard events.
 	 * @param input
 	 */
 	private void handleKeyboardEvents(Input input, StateBasedGame sbg) {
 		
 		PCharacter modelCharacter = level.getCharacter();
 		
 		// left arrow key
 		if (input.isKeyDown(Input.KEY_LEFT)) {
 			modelCharacter.moveLeft();
 			leftKeyIsDown = true;
 		} else if(LeftIsReleased()) {
 			modelCharacter.moveLeftReleased();
 		}
 		
 		// right arrow key
 		if (input.isKeyDown(Input.KEY_RIGHT) && !leftKeyIsDown) {
 			modelCharacter.moveRight();
 			rightKeyIsDown = true;
 			
 		} else if(rightIsReleased()) {
 			modelCharacter.moveRightReleased();
 		}
 		
 		
 		// up arrow key
 		if (input.isKeyDown(Input.KEY_UP)) {
 			if (!upKeyIsDown)
 				modelCharacter.jump();
 			upKeyIsDown = true;
 		} else if (upKeyIsReleased()) {
 			modelCharacter.jumpReleased();
 		}
 		
 
 		// space bar
 		if (input.isKeyPressed(Input.KEY_SPACE))
 			modelCharacter.attack();
 
 		// x key
 		if (input.isKeyPressed(Input.KEY_X))
 			modelCharacter.dash();
 		
 		// escape key
 		if (input.isKeyPressed(Input.KEY_ESCAPE))
 			sbg.enterState(Controller.PAUSE_MENU_STATE, null, new FadeInTransition());
 		
 		// digit key 1
 		if (input.isKeyPressed(Input.KEY_1)) 
 			modelCharacter.changeWeapon(ObjectName.PISTOL);
 		
 		// digit key 2
 		if (input.isKeyPressed(Input.KEY_2)) 
			modelCharacter.changeWeapon(ObjectName.LASER_PISTOL);
 		
 		// digit key 3
 		if (input.isKeyPressed(Input.KEY_3))
			modelCharacter.changeWeapon(ObjectName.SHOTGUN);
 		
 		// digit key 4
 		if (input.isKeyPressed(Input.KEY_4))
			modelCharacter.changeWeapon(ObjectName.ROCKET_LAUNCHER);
 		
 		// digit key 0. Cheat to instantaneously complete the level.
 		if (input.isKeyPressed(Input.KEY_0))
 			level.completeLevel();
 		
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
 	
 	/**
 	 * Returns if the up key has been released since last loop.
 	 * @return If the up key has been released.
 	 */
 	private boolean rightIsReleased() {
 		if (rightKeyIsDown) {
 			rightKeyIsDown = false;
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Returns if the up key has been released since last loop.
 	 * @return If the up key has been released.
 	 */
 	private boolean LeftIsReleased() {
 		if (leftKeyIsDown) {
 			leftKeyIsDown = false;
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Sets up the camera by telling that either the camera will move(environment, items and enemies)(cameraX,cameraY) 
 	 * or the Character(characterX,characterY)
 	 */
 	private void setUpCamera(int screenWidth, int screenHeight) {
 		
 		// Camera in Y-axis
 		int centerY = screenHeight/2 - (int)level.getCharacter().getHeight()*scale/2;
 		float posY = (float)level.getCharacter().getY()*scale;
 		int levelHeight = level.getTileGrid().getHeight()*Constants.TILESIZE*scale;
 		
 		// If the Character is in the upper part of the screen
 		if (posY < centerY) {
 			characterY = posY;
 			cameraY = 0;
 		}
 		// If the Character is in the bottom part of the screen
 		else if (levelHeight - posY < centerY + (int)level.getCharacter().getHeight()*scale) {
 			cameraY = -(levelHeight - screenHeight);
 			characterY = posY + cameraY;
 		}
 		// Else the Character is in the middle of the screen
 		else {
 			characterY = centerY;
 			cameraY = -(posY-centerY);
 		}
 		
 		// Camera in X-axis
 		int centerX = screenWidth/2 - (int)level.getCharacter().getWidth()*scale/2;
 		float posX = (float)level.getCharacter().getX()*scale;
 		int levelWidth = level.getTileGrid().getWidth()*Constants.TILESIZE*scale;
 		
 		// If the Character is in the left part of the screen
 		if (posX < centerX) {
 			characterX = posX;
 			cameraX = 0;
 		}
 		// If the Character is in the right part of the screen
 		else if (levelWidth - posX < centerX + (int)level.getCharacter().getWidth()*scale) {
 			cameraX = -(levelWidth - screenWidth);
 			characterX = posX + cameraX;
 		}
 		// Else the Character is in the middle of the screen
 		else {
 			characterX = centerX; // Places the character img in the middle horizontally of the screen.
 			cameraX = -(posX-centerX); // Moves everything else on the screen.
 		}
 	}
 	
 	/**
 	 * A class containing all necessary data and methods to draw the character.
 	 * 
 	 * @author Daniel Jonsson
 	 * 
 	 */
 	private class CharacterImage {
 		
 		/**
 		 * Some constants to use the maps more efficiently.
 		 */
 		private static final String runRKey = "runRight";
 		private static final String runLKey = "runLeft";
 		private static final String standRKey = "standRight";
 		private static final String standLKey = "standLeft";
 		private static final String jumpRKey = "jumpRight";
 		private static final String jumpLKey = "jumpLeft";
 		private static final String dashRKey = "dashRight";
 		private static final String dashLKey = "dashLeft";
 		private static final String deadKey = "dead";
 		
 		
 		private Map<String, Renderable> charMap;
 		private Map<String, Renderable> pistolMap;
 		private Map<String, Renderable> rpgMap;
 		private Map<String, Renderable> shotgunMap;
 		private Map<String, Renderable> laserPistolMap;
 		private Map<String, Integer> charXOffsets;
 		private String key;
 		
 		/**
 		 * Init all images and stuff.
 		 * 
 		 * @param folderPath
 		 *            Path to where the images can be found.
 		 * @param scale
 		 *            How much all images should be scaled.
 		 * @param filter
 		 *            What filter the scaling should use.
 		 * @throws SlickException
 		 */
 		public CharacterImage(String folderPath, int scale, int filter) throws SlickException {
 
 			// Make the dashing images.
 			Image dashingR = new Image(folderPath+"charDashing.png", false, filter).getScaledCopy(scale);
 			Image dashingL = dashingR.getFlippedCopy(true, false);
 			
 			// Death image.
 			Image dead = new Image(folderPath+"charDead.png", false, filter).getScaledCopy(scale);
 			
 			// Make all sprite maps.
 			laserPistolMap = makeCharSpriteMap(folderPath+"charLaserPistolStanding.png", folderPath+"charLaserPistolJumping.png", folderPath+"charLaserPistolRunningSheet.png", dashingR, dashingL, dead, scale, filter);
 			pistolMap = makeCharSpriteMap(folderPath+"charPistolStanding.png", folderPath+"charPistolJumping.png", folderPath+"charPistolRunningSheet.png", dashingR, dashingL, dead, scale, filter);
 			rpgMap = makeCharSpriteMap(folderPath+"charRPGStanding.png", folderPath+"charRPGJumping.png", folderPath+"charRPGRunningSheet.png", dashingR, dashingL, dead, scale, filter);
 			shotgunMap = makeCharSpriteMap(folderPath+"charShotgunStanding.png", folderPath+"charShotgunJumping.png", folderPath+"charShotgunRunningSheet.png", dashingR, dashingL, dead, scale, filter);
 			charMap = pistolMap;
 			key = standRKey;
 			
 			// Put all character image offsets in two maps
 			charXOffsets = new HashMap<String, Integer>();
 			charXOffsets.put(jumpLKey, -8*scale);
 			charXOffsets.put(jumpRKey, -6*scale);
 			charXOffsets.put(runLKey, -8*scale);
 			charXOffsets.put(runRKey, -6*scale);
 			charXOffsets.put(standLKey, -8*scale);
 			charXOffsets.put(standRKey, -6*scale);
 			charXOffsets.put(dashLKey, -6*scale);
 			charXOffsets.put(dashRKey, -36*scale);
 			charXOffsets.put(deadKey, -12*scale);
 		}
 		
 		/**
 		 * Create a sprite map for the character.
 		 * 
 		 * @param standingImage
 		 *            Path to the image where he is standing still.
 		 * @param jumpingImage
 		 *            Path to the image where he is jumping.
 		 * @param runningSheet
 		 *            Path to the sprite sheet where he is running.
 		 * @param dashLeft
 		 *            Reference to the image where he is dashing left.
 		 * @param dashRight
 		 *            Reference to the image where he is dashing right.
 		 * @param dead
 		 *            Reference to the image where he is dead.
 		 * @param scale
 		 *            How much all images should be scaled.
 		 * @param filter
 		 *            Filter for the scaling.
 		 * @return A sprite map for the CharacterImage.
 		 * @throws SlickException
 		 */
 		private Map<String, Renderable> makeCharSpriteMap(String standingImage,
 				String jumpingImage, String runningSheet, Image dashLeft,
 				Image dashRight, Image dead, int scale, int filter)
 				throws SlickException {
 			
 			Map<String, Renderable> spriteMap = new HashMap<String, Renderable>();
 			
 			// Make the running animations.
 			Image characterImage = new Image(runningSheet, false, filter);
 			int width = characterImage.getWidth()/3;
 			int height = characterImage.getHeight();
 			SpriteSheet characterSheet = new SpriteSheet(characterImage.getScaledCopy(scale), width*scale, height*scale);
 			Animation runningR = new Animation(characterSheet, 140);
 			characterImage = characterImage.getFlippedCopy(true, false);
 			characterSheet = new SpriteSheet(characterImage.getScaledCopy(scale), width*scale, height*scale);
 			Animation runningL = new Animation(characterSheet, 140);
 			
 			// Make the standing still images.
 			Image standingR = new Image(standingImage, false, filter).getScaledCopy(scale);	
 			Image standingL = standingR.getFlippedCopy(true, false);
 			
 			// Make the jumping images.
 			Image jumpingR = new Image(jumpingImage, false, filter).getScaledCopy(scale);
 			Image jumpingL = jumpingR.getFlippedCopy(true, false);
 			
 			// Put all images in the sprite map.
 			spriteMap.put(jumpLKey, jumpingL);
 			spriteMap.put(jumpRKey, jumpingR);
 			spriteMap.put(standLKey, standingL);
 			spriteMap.put(standRKey, standingR);
 			spriteMap.put(runLKey, runningL);
 			spriteMap.put(runRKey, runningR);
 			spriteMap.put(dashLKey, dashRight);
 			spriteMap.put(dashRKey, dashLeft);
 			spriteMap.put(deadKey, dead);
 			return spriteMap;
 		}
 		
 		/**
 		 * Draw the character on the screen.
 		 */
 		public void draw() {
 			
 			Renderable charImg = charMap.get(key);
 			int offset = charXOffsets.get(key);
 			
 			// Make the character flash white if he is immortal.
 			if (level.getCharacter().isImmortal() && Sys.getTime() % 400 < 200) {
 				if (charImg instanceof Image) {
 					((Image) charImg).drawFlash(characterX+offset, characterY);
 				} else if (charImg instanceof Animation) {
 					Animation characterAnimation = ((Animation) charImg);
 					characterAnimation.drawFlash(characterX+offset, characterY, characterAnimation.getWidth(), characterAnimation.getHeight());
 				}
 
 			} else {
 				charImg.draw(characterX+offset, characterY);
 			}
 		}
 		
 		/**
 		 * Update which map of the character that should be drawn on next draw() call.
 		 */
 		public void update() {
 
 			// Update which map should be used.
 			ObjectName weaponName = level.getCharacter().getWeapon().getName();
 			switch (weaponName) {
 				case PISTOL : 
 					charMap = pistolMap;
 					break;
 				case ROCKET_LAUNCHER :
 					charMap = rpgMap;
 					break;
 				case SHOTGUN :
 					charMap = shotgunMap;
 					break;
 				case LASER_PISTOL :
 					charMap = laserPistolMap;
 					break;
 			}
 			
 			// Update which key should be used.
 			if (level.getCharacter().isDead()) { // Char is dead
 				
 				key = deadKey;
 
 			} else if (level.getCharacter().isDashing()) { // Char is dashing
 				
 				if(level.getCharacter().isFacingRight())
 					key = dashRKey;
 				else
 					key = dashLKey;	
 
 			} else if (level.getCharacter().isAirborne()) { // Char is jumping
 					
 				if (level.getCharacter().isFacingRight())
 					key = jumpRKey;
 				else
 					key = jumpLKey;
 			
 			} else if (level.getCharacter().isRunning()) { // Char is standing still
 				
 				if (level.getCharacter().isFacingRight())
 					key = runRKey;
 				else
 					key = runLKey;
 				
 			} else { // Char is standing still
 		
 				if (level.getCharacter().isFacingRight())
 					key = standRKey;
 				else
 					key = standLKey;
 			}
 		}
 	}
 }
