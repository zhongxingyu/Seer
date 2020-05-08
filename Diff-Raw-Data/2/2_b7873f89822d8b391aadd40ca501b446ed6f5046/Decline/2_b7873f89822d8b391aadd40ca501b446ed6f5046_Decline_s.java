 // Package Declaration //
 package com.gamedev.decline;
 
 // Java Package Support //
 // { Not Applicable }
 
 // Badlogic Package Support //
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Intersector;
 import com.badlogic.gdx.utils.Array;
 
 /**
  * 
  * com/gamedev/decline/Decline.java
  * 
  * @author(s) : Ian Middleton, Zach Coker, Zach Ogle
  * @version : 2.0 Last Update : 3/25/2013 Update By : Ian Middleton
  * 
  *          Source code for the Decline class. The Decline class takes care of
  *          essentially "running" the entire game. All high-level game elements
  *          in addition to the camera, background, and batch are instantiated
  *          and executed here.
  * 
  */
 public class Decline implements ApplicationListener {
 
 	// Global Singleton //
 	private GlobalSingleton gs = GlobalSingleton.getInstance();
 
 	// Constants //
 	public static final int HEALTH_PACK = 5;
 	public static final int ENEMY_DAMAGE = 10;
 	int enemyShotDamage = 10;
 
 	// Internal Variables //
 	OrthographicCamera camera;
 	SpriteBatch batch;
 	RepeatingBackground background;
 	Hero hero;
 	BulletManager bm;
 	EnemyManager em;
 	ItemManager im;
 	boolean shoot = false;
 	boolean ableToShoot = true;
 	AmmoCountDisplay ammoDisplay;
 	Music jungleMusic;
 	Sound heroHitSound;
 	Sound enemyHitSound;
 	Sound bulletShotSound;
 	Sound itemPickUpSound;
	boolean bossFight = true;
 
 	/**
 	 * Function run when the game is started. Basically a high-level constructor
 	 * for the game.
 	 * 
 	 * (non-Javadoc)
 	 * 
 	 * @see com.badlogic.gdx.ApplicationListener#create()
 	 */
 	@Override
 	public void create() {
 		float width = Gdx.graphics.getWidth();
 		float height = Gdx.graphics.getHeight();
 
 		Gdx.app.log("Screen Width", String.valueOf(width));
 		Gdx.app.log("Screen Height", String.valueOf(height));
 
 		camera = new OrthographicCamera(width, height);
 		camera.setToOrtho(false);
 
 		jungleMusic = Gdx.audio
 				.newMusic(Gdx.files.internal("jungle_noise.mp3"));
 		jungleMusic.setLooping(true);
 		jungleMusic.play();
 
 		//heroHitSound = Gdx.audio.newSound(Gdx.files.internal("hero_hit.mp3"));
 		//enemyHitSound = Gdx.audio.newSound(Gdx.files.internal("enemy_hit.wav"));
 		//bulletShotSound = Gdx.audio.newSound(Gdx.files.internal("shotgun.wav"));
 		//itemPickUpSound = Gdx.audio.newSound(Gdx.files.internal("bloop.wav"));
 
 		batch = new SpriteBatch();
 
 		hero = new Hero(new Texture(Gdx.files.internal("hero_weapon.png")),
 				new Texture(Gdx.files.internal("hero_crouch.png")),
 		                new Texture(Gdx.files.internal("data/heart.jpg")));
 		hero.setOrigin(hero.getWidth() / 2, hero.getHeight() / 2);
 		hero.setToInitialDrawPosition();
 
 		bm = new BulletManager(new Texture(Gdx.files.internal("bullets.png")));
 
 		em = new EnemyManager(new Texture(Gdx.files.internal("enemy.png")));
 
 		
 		ammoDisplay = new AmmoCountDisplay(hero);
 		background = new RepeatingBackground(new Texture(
 				Gdx.files.internal("background.png")));
 		im = new ItemManager(new Texture(Gdx.files.internal("ammoBox.png")),
 				new Texture(Gdx.files.internal("healthKit.png")));
 		gs.setHealthBarManager(new HealthBarManager());
 		gs.getHealthBarManager().add(hero);
 	}
 
 	/**
 	 * Cleans up all resources when the game is closed.
 	 * 
 	 * (non-Javadoc)
 	 * 
 	 * @see com.badlogic.gdx.ApplicationListener#dispose()
 	 * 
 	 *      *** INCOMPLETE ***
 	 */
 	@Override
 	public void dispose() {
 		batch.dispose();
 	}// end setHealth()
 
 	/**
 	 * Calls the draw functions for all objects and then calls the update
 	 * function.
 	 * 
 	 * (non-Javadoc)
 	 * 
 	 * @see com.badlogic.gdx.ApplicationListener#render()
 	 */
 	@Override
 	public void render() {
 		handleEvent();
 		handleCollision();
 		update();
 
 		Gdx.gl.glClearColor(1, 1, 1, 1);
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
 		batch.setProjectionMatrix(camera.combined);
 		batch.begin();
 		background.draw(batch);
 		if (gs.getIsHeroAlive()) {
 			hero.draw(batch);
 		}
 		bm.draw(batch);
 		em.draw(batch);
 		im.draw(batch);
 		hero.drawAmmoCount(batch);
 		hero.drawLives(batch);
 		batch.end();
 		gs.getHealthBarManager().draw();
 	}// end setHealth()
 
 	/**
 	 * Handles all keyboard events in the game.
 	 */
 	private void handleEvent() {
 		// if hero is hiding
 		if (gs.getIsHeroHiding()) {
 			// and the user is not trying to hide
 			// make the hero stand up
 			if (!Gdx.input.isKeyPressed(Keys.DOWN)) {
 				hero.stand();
 			}
 			// else set the hero movement to zero
 			else {
 				gs.setHeroMovement(0);
 			}
 		}
 		// if the hero is not hiding
 		else {
 			// if left is pressed move left
 			if (Gdx.input.isKeyPressed(Keys.LEFT)) {
 				if(bossFight){
 					hero.moveLeft();
 				}else{
 					hero.moveLeftScroll();
 				}
 				if (gs.getHeroOrientation() == GlobalSingleton.RIGHT) {
 					gs.setHeroOrientation(GlobalSingleton.LEFT);
 				}
 				gs.setHeroMovement(-Hero.SPEED);
 			} else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
 				if(bossFight){
 					hero.moveRight();
 				}else{
 					hero.moveRightScroll();
 				}
 				if (gs.getHeroOrientation() == GlobalSingleton.LEFT) {
 					gs.setHeroOrientation(GlobalSingleton.RIGHT);
 				}
 				gs.setHeroMovement(Hero.SPEED);
 			} else {
 				gs.setHeroMovement(0);
 			}
 			if (Gdx.input.isKeyPressed(Keys.UP) && !gs.getIsHeroJumping()) {
 				gs.setIsHeroJumping(true);
 			}
 			if (Gdx.input.isKeyPressed(Keys.DOWN)) {
 				hero.hide();
 			}
 			if (Gdx.input.isKeyPressed(Keys.SPACE) && ableToShoot) {
 				shoot = true;
 				ableToShoot = false;
 			}
 		}
 
 		
 
 		if (shoot == true) {
 			bm.shootBullet();
 			//bulletShotSound.play();
 			hero.setAmmo(hero.getAmmo() - 1);
 			if (hero.getAmmo() == 0) {
 				ableToShoot = false;
 			}
 			shoot = false;
 		}
 
 		if (!(Gdx.input.isKeyPressed(Keys.SPACE)) && hero.getAmmo() != 0) {
 			ableToShoot = true;
 		}
 	}// end handleEvent()
 
 	/**
 	 * Handles all collisions between CollidableObjects.
 	 */
 	private void handleCollision() {
 		Array<Bullet> activeBullets = bm.getActiveBullets();
 		Array<Enemy> activeEnemies = em.getActiveEnemies();
 		Array<Ammo> activeAmmo = im.getActiveAmmo();
 		Array<HealthPack> activeHealthPacks = im.getActiveHealthPacks();
 		
 		if(bossFight){
 			
 		}
 
 		for (int i = 0; i < activeBullets.size; i++) {
 			for (int j = 0; j < activeEnemies.size; j++) {
 				if (activeBullets.get(i).collidesWith(activeEnemies.get(j))) {
 					bm.removeActiveBullet(i);
 					em.enemyDamagedEvent(j, enemyShotDamage);
 					//enemyHitSound.play();
 					break;
 				}// end if
 			}// end for
 		}// end for
 		if (!gs.getIsHeroHiding()) {
 			for (int i = 0; i < activeEnemies.size; i++) {
 				if (hero.collidesWith(activeEnemies.get(i))) {
 					hero.setHealth(hero.getHealth() - ENEMY_DAMAGE);
 					if (hero.getHealth() < 0) {
 						hero.setHealth(0);
 					}// end if
 					em.enemyDamagedEvent(i,activeEnemies.get(i).getMaxHealth());
 					//heroHitSound.play();
 				}// end if
 			}// end for
 		}
 
 		for (int i = 0; i < activeAmmo.size; i++) {
 			if (hero.collidesWith(activeAmmo.get(i))) {
 				hero.setAmmo(hero.getAmmo()
 						+ im.getActiveAmmo().get(i).getAmountOfAmmoStored());
 				ableToShoot = true;
 				if (hero.getAmmo() > Hero.MAX_AMMO) {
 					hero.setAmmo(Hero.MAX_AMMO);
 				}// end if
 				im.removeActiveAmmo(i);
 				//itemPickUpSound.play();
 			}// end if
 		}// end for
 
 		for (int i = 0; i < activeHealthPacks.size; i++) {
 			if (hero.collidesWith(activeHealthPacks.get(i))) {
 				hero.setHealth(hero.getHealth() + HEALTH_PACK);
 				if (hero.getHealth() > Hero.MAX_HEALTH) {
 					hero.setHealth(Hero.MAX_HEALTH);
 				}
 				im.removeActiveHealthPack(i);
 				//itemPickUpSound.play();
 			}
 		}
 	}
 
 	/**
 	 * Calls the update functions for all objects.
 	 */
 	private void update() {
 		hero.update();
 		bm.update();
 		em.update();
 		im.update();
 		gs.getHealthBarManager().update();
 	}// end update()
 
 	/**
 	 * Function used for resizing of the game.
 	 * 
 	 * (non-Javadoc)
 	 * 
 	 * @see com.badlogic.gdx.ApplicationListener#resize(int, int)
 	 * 
 	 *      *** INCOMPLETE ***
 	 */
 	@Override
 	public void resize(int width, int height) {
 	}// end resize()
 
 	/**
 	 * Function used for pausing of the game.
 	 * 
 	 * (non-Javadoc)
 	 * 
 	 * @see com.badlogic.gdx.ApplicationListener#pause()
 	 * 
 	 *      *** INCOMPLETE ***
 	 */
 	@Override
 	public void pause() {
 	}// end pause()
 
 	/**
 	 * Function used for resuming of the game
 	 * 
 	 * (non-Javadoc)
 	 * 
 	 * @see com.badlogic.gdx.ApplicationListener#resume()
 	 * 
 	 *      *** INCOMPLETE ***
 	 */
 	@Override
 	public void resume() {
 	}// end resume()
 }// end Decline class
