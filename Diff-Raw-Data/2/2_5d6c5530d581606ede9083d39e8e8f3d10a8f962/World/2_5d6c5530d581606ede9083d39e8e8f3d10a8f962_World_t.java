 package com.picotech.lightrunnerlibgdx;
 
 import java.util.ArrayList;
 
 import java.util.HashMap;
 import java.util.Random;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Intersector;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.picotech.lightrunnerlibgdx.GameScreen.GameState;
 import com.picotech.lightrunnerlibgdx.Powerup.Type;
 
 /**
  * The World class holds all of the players, enemies and environment objects. It
  * handles collisions and drawing methods, as well as loading content.
  */
 
 public class World {
 
 	/*
 	 * enum MenuState { PLAY, CHOOSESIDE }
 	 * 
 	 * MenuState menuState = MenuState.PLAY;
 	 */
 
 	Player player;
 	Mirror mirror;
 	Light light;
 	DebugOverlay debug;
 	StatLogger statlogger;
 	Menu menu;
 
 	BitmapFont bf;
 
 	float deltaTime, totalTime;
 	float spawnEnemyTime;
 	boolean enemySpawnInit = true;
 	float loadedContentPercent;
 
 	int enemiesKilled;
 	int score;
 	int level;
 	int powerupf = 2000;
 
 	Vector2 ENEMY_VEL;
 	Vector2 LightSource;
 
 	Rectangle pauseButton;
 
 	// boolean menuScreen;
 	boolean playSelected;
 	boolean controlsSelected;
 	boolean isClearScreen = false;
 	boolean slowActivated = false;
 	// boolean isIncoming = false;
 	boolean playedSound = false;
 	boolean oneHit = false;
 	boolean isSpawning = true;
 	public static boolean debugMode = true;
 	public static boolean soundFX = true;
 
 	ArrayList<Enemy> enemies;
 	ArrayList<Enemy> enemiesDead;
 	ArrayList<Magnet> magnets;
 	ArrayList<Magnet> inactiveMagnets;
 	ArrayList<Powerup> powerups;
 	ArrayList<Powerup> inactivePowerups;
 	public static HashMap<Type, Float> puhm = new HashMap<Type, Float>();
 
 	Color healthBar;
 
 	Random r = new Random();
 
 	/**
 	 * There are two types of worlds, the menu world and the in-game world. The
 	 * behavior of the light depends on whether the game is in the menu or
 	 * playing state.
 	 * 
 	 * @param isMenu
 	 */
 	public World() {
 		level = 1;
 		totalTime = 0;
 
 		pauseButton = new Rectangle(GameScreen.width - 100,
 				GameScreen.height - 100, 200, 80);
 
 		enemies = new ArrayList<Enemy>();
 		enemiesDead = new ArrayList<Enemy>();
 		powerups = new ArrayList<Powerup>();
 		inactivePowerups = new ArrayList<Powerup>();
 
 		// menuScreen = isMenu;
 		player = new Player(new Vector2(0, 300), "characterDirection0.png");
 		mirror = new Mirror(new Vector2(100, 300), "mirror.png");
 		magnets = new ArrayList<Magnet>();
 		inactiveMagnets = new ArrayList<Magnet>();
 		menu = new Menu();
 
 		debug = new DebugOverlay();
 		statlogger = new StatLogger();
 		healthBar = new Color();
 
 		if (isMenu()) {
 			setupMenu();
 		} else {
 			setLight();
 		}
 
 		// Spawning enemies
 		for (int i = 0; i < level; i++) {
 			enemies.add(new Enemy(new Vector2(r.nextInt(950) + 300, r
 					.nextInt(700)), 50, 50, level));
 			enemies.get(enemies.size() - 1).loadContent();
 		}
 
 		// Power-ups
 		if (!isMenu()) {
 			for (Powerup pu : powerups)
 				pu.loadContent();
 			powerupf = r.nextInt(500) + 1500;
 		}
 		// HashMap values
 		puhm.put(Powerup.Type.CLEARSCREEN, 3.5f);
 		puhm.put(Powerup.Type.ENEMYSLOW, 9.5f);
 		puhm.put(Powerup.Type.ONEHITKO, 11f);
 		puhm.put(Powerup.Type.PRISMPOWERUP, 6f);
 		puhm.put(Powerup.Type.SPAWNSTOP, 8f);
 		puhm.put(Powerup.Type.SPAWNMAGNET, 5f);
 	}
 
 	public void setupMenu() {
 		player = new Player(new Vector2(-100, -100), "characterDirection0.png");
 		light = new Light(true);
 		level = 10;
 	}
 
 	private void setLight() {
 		Random r = new Random();
 		if (GameScreen.scheme == GameScreen.LightScheme.TOP) {
 			LightSource = new Vector2(r.nextInt(640) + 420, 720);
 		} else if (GameScreen.scheme == GameScreen.LightScheme.RIGHT) {
 			LightSource = new Vector2(1280, r.nextInt(700 + 10));
 		} else if (GameScreen.scheme == GameScreen.LightScheme.BOTTOM) {
 			LightSource = new Vector2(r.nextInt(640) + 420, 0);
 		}
 
 		light = new Light(LightSource, mirror.getCenter());
 	}
 
 	/**
 	 * Loads all the content of the World.
 	 */
 	public void loadContent() {
 		player.loadContent();
 		mirror.loadContent();
 		menu.loadContent();
 
 		for (Powerup pu : powerups) {
 			pu.loadContent();
 		}
 
 		bf = new BitmapFont();
 		bf.scale(1);
 		bf.setColor(Color.WHITE);
 
 		if (debugMode)
 			debug.loadContent();
 	}
 
 	/**
 	 * Updates the entire World. Includes light, enemy movement, and enemy
 	 * destruction. Also updates the time functions for frame rate-independent
 	 * functions deltaTime and totalTime (which are all in seconds).
 	 */
 	public void update() {
 		// Miscellaneous time updating functions.
 		deltaTime = Gdx.graphics.getDeltaTime();
 		if (GameScreen.state == GameScreen.GameState.PLAYING
 				|| (isMenu() && menu.menuState == Menu.MenuState.MAIN)) {
 			totalTime += deltaTime;
 
 			if ((debug.nothingSelected && debugMode) || !debugMode) {
 				player.update();
 				mirror.rotateAroundPlayer(
 						player.getCenter(),
 						(player.bounds.width / 2) + 2
 								+ (light.getOutgoingBeam().isPrism ? 40 : 0));
 			}
 			// Updating light, player, and the mirror.
 			light.update(mirror, player);
 
 			// Updates all enemies in "enemies".
 			for (Enemy e : enemies) {
 				e.update(soundFX);
 				for (int beam = 1; beam < light.beams.size(); beam++) {
 					if (Intersector.overlapConvexPolygons(
 							light.beams.get(beam).beamPolygon, e.p)) {
 						if (oneHit) {
 							e.alive = false;
 							if (soundFX)
 								Assets.died.play();
 						}
 						if (e.alive) {
 							e.health--;
 							e.losingHealth = true;
 							if (soundFX)
 								Assets.hit.play(.1f);
 						} else {
 							enemiesKilled++;
 						}
 
 					}
 					if (Intersector.overlapConvexPolygons(player.p, e.p)) {
 						if (player.alive)
 							player.health--;
 					}
 				}
 				// adds the number of enemies still alive to a new ArrayList
 				if (e.alive) {
 					//enemiesAlive.add(e);
 					e.isSlow = slowActivated;
 				} else {
 					enemiesDead.add(e);
 				}
 
 				// magnets
 				for (Magnet magnet : magnets) {
 					if (e.getCenter().dst(magnet.getCenter()) < 500) {
 						e.velocity.set(magnet.getPull(e.getCenter()));
 					}
 				}
 			}
 			
 			for (Magnet magnet : magnets) {
 				magnet.update();
 				if (magnet.position.x < -100) {
 					inactiveMagnets.add(magnet);
 				}
 			}
 
 			// removes the "dead" enemies from the main ArrayList
 			enemies.removeAll(enemiesDead);
 			enemiesDead.clear();
 
 			magnets.removeAll(inactiveMagnets);
 			inactiveMagnets.clear();
 
 			// temporarily spawns new enemies, which get progressively faster
 			if ((isSpawning && spawnEnemyTime <= totalTime) || enemySpawnInit) {
 				enemies.add(new Enemy(new Vector2(1280, r.nextInt(700)), 50,
 						50, level));
 				enemies.get(enemies.size() - 1).isSlow = slowActivated;
 				enemies.get(enemies.size() - 1).loadContent();
 				enemySpawnInit = false;
 				spawnEnemyTime = totalTime + (3f / level);
 			}
 
 			// Time-wise level changing
 			if (!isMenu())
 				if (totalTime > 5 * level)
 					level++;
 
 			setScore();
 
 			// Powerups.
 			updatePowerups();
 		}
 
 		// Depending on the MenuState, it will either show the Play
 		// button or the Top-Right-Bottom buttons.
 		float dstX = light.getOutgoingBeam().dst.x;
 		if (isMenu() && menu.menuState == Menu.MenuState.MAIN) {
 			if (dstX > menu.playButton.x - 100
 					&& dstX < menu.playButton.x + menu.playButton.width + 100) {
 				playSelected = true;
 				playBlip();
 			} else {
 				playSelected = false;
 				playedSound = false;
 			}
 		}
 
 		// Debugging overlay.
 		if (debugMode) {
 			debug.update();
 			if (debug.selectedButtons[0]) {
 				System.out.println("Changed mirror.");
 				if (mirror.type == Mirror.Type.CONVEX)
 					mirror.type = Mirror.Type.FLAT;
 				else if (mirror.type == Mirror.Type.FLAT)
 					mirror.type = Mirror.Type.FOCUS;
 				else if (mirror.type == Mirror.Type.FOCUS)
 					mirror.type = Mirror.Type.CONVEX;
 			} else if (debug.selectedButtons[1]) {
 				System.out.println("Reset magnet.");
 				magnets.add(new Magnet(new Vector2(1280, MathUtils.random(100,
 						700)), 48, 48, "magnet.png", .1f));
 				magnets.get(magnets.size() - 1).loadContent();
 			} else if (debug.selectedButtons[2]) {
 				System.out.println("Added powerup.");
 				addPowerup();
 			} else if (debug.selectedButtons[3]) {
 				player.alive = false;
 			}
 			debug.resetButtons();
 		}
 	}
 
 	public void selectControls() {
 		// Randomized light-source choosing.
 		int schemeN = r.nextInt(3) + 1;
 		GameScreen.scheme = GameScreen.selectedScheme = GameScreen.LightScheme
 				.values()[schemeN];
 		controlsSelected = true;
 		playedSound = true;
 		GameScreen.state = GameScreen.GameState.READY;
 	}
 
 	// writes to StatLogger
 	public void updateStatLogger(StatLogger sl) {
 		sl.update(score, (int) totalTime, enemiesKilled);
 	}
 
 	public void setScore() {
 		// Score algorithm, changed as of 8/20/13
 		score = (int) (totalTime * 2 + enemiesKilled * 5);
 	}
 
 	private void playBlip() {
 		if (!playedSound && soundFX) {
 			Assets.blip.play(.5f);
 			playedSound = true;
 		}
 	}
 
 	public boolean collide(Sprite2 pu, Sprite2 player) {
 		return pu.position.x < player.position.x + player.bounds.width
 				&& pu.position.y + pu.bounds.height > player.position.y
 				&& pu.position.y < player.position.y + player.bounds.height;
 	}
 
 	/**
 	 * Handles all the power-up logic.
 	 */
 	private void updatePowerups() {
 		// Randomizing spawns
 		if ((int) (totalTime * 100) % powerupf == 0) {
 			addPowerup();
 		}
 
 		for (int i = 0; i < powerups.size(); i++) {
 			Powerup pu = powerups.get(i);
 			pu.update(deltaTime);
 			// Collision with player
 			if (collide(pu, player) && pu.position.x >= 0) {
 				player.addPowerup(pu);
				pu.position = new Vector2(1000, -500);
 			} else if (pu.position.x < -100){
 				inactivePowerups.add(pu);
 			}
 
 			// Ending power-ups
 			if (pu.timeActive > pu.timeOfEffect) {
 				pu.end();
 
 				switch (pu.type) {
 				case ONEHITKO:
 					oneHit = false;
 					break;
 				case PRISMPOWERUP:
 					GameScreen.scheme = GameScreen.selectedScheme;
 					setLight();
 					light.getOutgoingBeam().setWidth(Light.L_WIDTH);
 					light.getOutgoingBeam().isPrism = false;
 					mirror.setType(Mirror.Type.FLAT, "mirror.png");
 					break;
 				case ENEMYSLOW:
 					slowActivated = false;
 					for (Enemy e : enemies)
 						e.isSlow = false;
 					break;
 				case CLEARSCREEN:
 					isClearScreen = false;
 					break;
 				case SPAWNSTOP:
 					isSpawning = true;
 					break;
 				case SPAWNMAGNET:
 					// do nothing, it goes all the way to the end of the screen
 					break;
 				default:
 					break;
 				}
 
 				powerups.remove(i);
 			}
 		}
 		powerups.removeAll(inactivePowerups);
 		inactivePowerups.clear();
 
 		if (isClearScreen) {
 			enemiesDead.clear();
 			enemies.clear();
 		}
 	}
 
 	public void usePowerup(Powerup pu) {
 		switch (pu.type) {
 		case ONEHITKO:
 			oneHit = true;
 			break;
 		case PRISMPOWERUP:
 			GameScreen.scheme = GameScreen.LightScheme.LEFT;
 			light.getOutgoingBeam().setWidth(Powerup.P_WIDTH);
 			mirror.setType(Mirror.Type.PRISM, "prism.png");
 			break;
 		case ENEMYSLOW:
 			slowActivated = true;
 			for (Enemy e : enemies)
 				e.isSlow = true;
 			break;
 		case CLEARSCREEN:
 			isClearScreen = true;
 			for (int j = 0; j < enemies.size(); j++) {
 				if (enemies.get(j).alive)
 					enemiesKilled++;
 			}
 			setScore();
 			break;
 		case SPAWNSTOP:
 			isSpawning = false;
 			break;
 		case SPAWNMAGNET:
 			magnets.add(new Magnet(
 					new Vector2(1280, MathUtils.random(100, 700)), 48, 48,
 					"magnet.png", .05f));
 			magnets.get(magnets.size() - 1).loadContent();
 		}
 
 		pu.isActive = true;
 		pu.isAura = true;
 		pu.position = new Vector2(10000, 10000);
 	}
 
 	public void addPowerup() {
 		int x = r.nextInt(Powerup.Type.values().length);
 		powerups.add(new Powerup(new Vector2(1300, r.nextInt(600) + 50),
 				Powerup.Type.values()[x]));
 		powerups.get(powerups.size() - 1).loadContent();
 		powerupf = r.nextInt(500) + 2500;
 	}
 
 	/**
 	 * Draws the entire world.
 	 * 
 	 * @param batch
 	 *            the SpriteBatch from WorldRenderer
 	 * @param sr
 	 *            the ShapeRenderer to render light and enemies
 	 */
 	public void draw(SpriteBatch batch, ShapeRenderer sr) {
 		// if (menu.menuState == Menu.MenuState.PAUSE && isMenu()
 		// || GameScreen.state == GameScreen.GameState.PLAYING) {
 
 		for (Enemy e : enemies)
 			e.draw(batch);
 
 		light.draw(sr);
 
 		batch.begin();
 		player.draw(batch, mirror.angle - 90);
 		mirror.draw(batch);
 		for (Magnet magnet : magnets)
 			magnet.draw(batch);
 
 		batch.end();
 		player.drawInventory(batch);
 
 		// powerups
 		for (int i = 0; i < powerups.size(); i++)
 			powerups.get(i).draw(batch);
 
 		if (GameScreen.state == GameState.PLAYING) {
 			batch.begin();
 			batch.draw(Assets.pauseButton, pauseButton.x, pauseButton.y,
 					Assets.pauseButton.getWidth(), Assets.pauseButton.getHeight());
 			batch.end();
 
 			healthBar.set(1 - player.health / 100, player.health / 100, 0, 1);
 
 			// drawing health bar
 			sr.begin(ShapeType.FilledRectangle);
 			sr.setColor(healthBar);
 			sr.filledRect(100, 20, player.health * 10, 10);
 			sr.end();
 
 			if (debugMode) {
 				debug.draw(batch, sr);
 				String powerupString = "";
 				for (Powerup p : powerups) {
 					powerupString += (p.timeActive);
 					powerupString += "\n";
 				}
 				batch.begin();
 				bf.draw(batch, "pu: " + powerupString, 550, 720);
 				batch.end();
 			}
 
 			batch.begin();
 			// Text drawing
 			bf.setColor(Color.WHITE);
 			bf.draw(batch, "Score: " + score, 0, 720);
 			bf.draw(batch, "Enemies Killed: " + enemiesKilled, 225, 720);
 			bf.draw(batch, "Level: " + level, 1000, 720);
 
 			// testing
 			batch.end();
 		}
 
 		if (isMenu())
 			menu.draw(batch);
 	}
 
 	public boolean isMenu() {
 		return GameScreen.state == GameScreen.GameState.MENU;
 	}
 }
