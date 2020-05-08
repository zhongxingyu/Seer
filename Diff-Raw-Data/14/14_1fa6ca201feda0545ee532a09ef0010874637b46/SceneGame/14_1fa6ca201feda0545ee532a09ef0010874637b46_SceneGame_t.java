 /*
  * Copyright (C) 2013 Andrew Tulay
  * @mail: mhyhre@gmail.com
  * 
  * This work is licensed under a Creative Commons 
  * Attribution-NonCommercial-NoDerivs 3.0 Unported License.
  * You may obtain a copy of the License at
  *
  *		http://creativecommons.org/licenses/by-nc-nd/3.0/legalcode
  *
  */
 
 package mhyhre.lightrabbit.Scenes;
 
 import java.util.LinkedList;
 import java.util.List;
 import mhyhre.lightrabbit.MainActivity;
 import mhyhre.lightrabbit.MhyhreScene;
 import mhyhre.lightrabbit.R;
 import mhyhre.lightrabbit.game.BulletUnit;
 import mhyhre.lightrabbit.game.CloudsManager;
 import mhyhre.lightrabbit.game.Collisions;
 import mhyhre.lightrabbit.game.EnemiesManager;
 import mhyhre.lightrabbit.game.Enemy;
 import mhyhre.lightrabbit.game.GameHUD;
 import mhyhre.lightrabbit.game.GameMessageManager;
 import mhyhre.lightrabbit.game.SkyManager;
 import mhyhre.lightrabbit.game.WaterPolygon;
 import mhyhre.lightrabbit.game.Levels.Event;
 import mhyhre.lightrabbit.game.Levels.EventManager;
 
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.sprite.batch.SpriteBatch;
 import org.andengine.entity.text.Text;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import android.util.Log;
 
 public class SceneGame extends MhyhreScene {
 
 	boolean mLoaded = true;
 	boolean mPause = false;
 
 	private final float reloadingTime = 1.0f;
 	private final int maxBulletsOnScreen = 50;
 
 	private Background mBackground;
 
 	Sprite boat;
 	SpriteBatch bulletBatch;
 
 	GameHUD HUD;
 
 	List<BulletUnit> mBullets;
 	float lastFireTime = 0;
 
 	CloudsManager mClouds;
 	SkyManager mSkyes;
 	EnemiesManager mEnemies;
 	EventManager mEventManager;
 	GameMessageManager mMessageManager;
 
 	private WaterPolygon water;
 
 	private int totalGold = 100;
 	final int maxHealth = 3;
 	int currentHealth = 3;
 
 	float timeCounter = 0;
 
 	private float boatSpeed = 0;
	private float boatAcceleration = 3;
 
 	public SceneGame() {
 
 		mBackground = new Background(0.8f, 0.8f, 0.8f);
 		setBackground(mBackground);
 		setBackgroundEnabled(true);
 
 		HUD = new GameHUD();
 
 		createGameObjects();
 
 		mEventManager = new EventManager();
 
 		attachChild(HUD);
 
 		Log.i(MainActivity.DEBUG_ID, "Scene game created");
 	}
 
 	public void load(String levelName) {
 
 		mEventManager.loadEvents(levelName);
 		mLoaded = true;
 	}
 
 	public void start() {
 
 		mPause = false;
 	}
 
 	public void pause() {
 
 		mPause = true;
 	}
 
 	private void createGameObjects() {
 
 		mBullets = new LinkedList<BulletUnit>();
 
 		mClouds = new CloudsManager(5, MainActivity.Me.getVertexBufferObjectManager());
 
 		water = new WaterPolygon(16, MainActivity.Me.getVertexBufferObjectManager());
 
 		boat = new Sprite(100, 100, MainActivity.Res.getTextureRegion("boat_body"), MainActivity.Me.getVertexBufferObjectManager());
 
 		mEnemies = new EnemiesManager(water, MainActivity.Me.getVertexBufferObjectManager());
 
 		bulletBatch = new SpriteBatch(MainActivity.Res.getTextureAtlas("texture01"), maxBulletsOnScreen, MainActivity.Me.getVertexBufferObjectManager());
 
 		mSkyes = new SkyManager(mBackground, water, MainActivity.Me.getVertexBufferObjectManager());
 
 		mMessageManager = new GameMessageManager();
 		mMessageManager.Hide();
 
 		attachChild(mSkyes);
 		attachChild(mClouds);
 		attachChild(mEnemies);
 		attachChild(bulletBatch);
 		attachChild(boat);
 		attachChild(water);
 
 		attachChild(mMessageManager);
 	}
 
 	@Override
 	public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
 
 		if (!mLoaded)
 			return true;
 
 		HUD.onSceneTouchEvent(pSceneTouchEvent);
 
 		mMessageManager.onSceneTouchEvent(pSceneTouchEvent);
 		if (mMessageManager.isActiveMessage() == false) {
 			return super.onSceneTouchEvent(pSceneTouchEvent);
 		}
 		return true;
 	}
 
 	@Override
 	protected void onManagedUpdate(final float pSecondsElapsed) {
 
 		// if(!mLoaded) return;
 		// if(mPause) return;
 
		updateControlls();
 
 		updatePlayer();
 
 		timeCounter += pSecondsElapsed;
 		float halfRange = 0.050f;
 
 		Event gameEvent = mEventManager.getCurrentEvent();
 
 		if (mMessageManager.isActiveMessage() == false) {
 
 			// if all events complete
 			if (gameEvent == null) {
 
 				// if all enemies destroyed
 				if (mEnemies.isWaitState() == false) {
 
 				}
 			} else {
 
 				if (timeCounter > (gameEvent.getIntArg() - halfRange)) {
 
 					if (mEnemies.isWaitState()) {
 						timeCounter = 0;
 					} else {
 						mEventManager.nextEvent();
 					}
 
 					mEnemies.addNewEnemy(gameEvent);
 				}
 			}
 		}
 
 		updateBullets();
 
 		mEnemies.update();
 
 		enemiesSharks();
 
 		HUD.updateHealthIndicator(currentHealth, maxHealth);
 
 		super.onManagedUpdate(pSecondsElapsed);
 	}
 
 	private void updatePlayer() {
 		if (boat.getX() > (MainActivity.getWidth() - 32 - boat.getWidth()) && boatSpeed > 0)
 			boatSpeed = 0;
 
 		if (boat.getX() < 32 && boatSpeed < 0)
 			boatSpeed = 0;
 
 		boat.setX(boat.getX() + boatSpeed);
 		boat.setY(water.getYPositionOnWave(boat.getX()) + 5);
 		boat.setRotation(water.getAngleOnWave(boat.getX()) / 2.0f);
 	}
 
 	private void enemiesSharks() {
 
 		for (Enemy enemy : mEnemies.getEnemiesList()) {
 
 			if (Collisions.sptireCircleByCircle(boat, 20, enemy.getX(), enemy.getY(), 20)) {
 
 				if (enemy.isDied() == false) {
 
 					MainActivity.vibrate(30);
 
 					if (currentHealth > 0)
 						currentHealth--;
 
 					enemy.setDied(true);
 				}
 			}
 		}
 	}
 
 	private void updateBullets() {
 		// Bullets
 		ITextureRegion bulletRegion = MainActivity.Res.getTextureRegion("bullet");
 		ITextureRegion bulletBoomRegion = MainActivity.Res.getTextureRegion("bullet_boom");
 		ITextureRegion bulletResultRegion;
 
 		for (int i = 0; i < mBullets.size(); i++) {
 
 			BulletUnit bullet = mBullets.get(i);
 
 			for (Enemy enemy : mEnemies.getEnemiesList()) {
 
 				if (bullet.getBoom() == 0 && bullet.collideWithCircle(enemy.getX(), enemy.getY(), enemy.getRadius())) {
 
 					if (enemy.isDied() == false) {
 
 						enemy.setHealth(enemy.getHealth() - bullet.getBoomPower());
 
 						if (enemy.getHealth() <= 0) {
 
 							enemy.setDied(true);
 
 							totalGold += 50;
 							HUD.updateGoldIndicator(totalGold);
 						}
 					}
 
 					bullet.setSink(true);
 					bullet.setBoom(10);
 				}
 			}
 
 			if (bullet.getY() < water.getYPositionOnWave(bullet.getX())) {
 				bullet.setSink(true);
 			}
 
 			bullet.update();
 
 			if (bullet.getY() < 0) {
 				mBullets.remove(i);
 				i--;
 				continue;
 			}
 
 			if (bullet.getX() > MainActivity.getWidth() || bullet.getX() < 0) {
 				mBullets.remove(i);
 				i--;
 				continue;
 			}
 
 			if (bullet.getBoom() > 0) {
 				bulletResultRegion = bulletBoomRegion;
 			} else {
 				bulletResultRegion = bulletRegion;
 			}
 			if (bullet.getBoom() != -1) {
 				bulletBatch.draw(bulletResultRegion, bullet.getX() - bulletResultRegion.getWidth() / 2, bullet.getY() - bulletResultRegion.getHeight() / 2,
 						bulletResultRegion.getWidth(), bulletResultRegion.getHeight(), 1, 1, 1, 1, 1);
 
 			}
 		}
 
 		bulletBatch.submit();
 	}
 
 
 
	private void updateControlls() {
 
 		boatSpeed = 0;
 
 		if (HUD.isKeyDown(GameHUD.Buttons.LEFT)) {
			boatSpeed -= boatAcceleration;
 		}
 
 		if (HUD.isKeyDown(GameHUD.Buttons.RIGHT)) {
			boatSpeed += boatAcceleration;
 		}
 
 		if (HUD.isKeyDown(GameHUD.Buttons.FIRE)) {
 
 			if (timeCounter - lastFireTime > reloadingTime) {
 
 				if (mBullets.size() < maxBulletsOnScreen) {
 
 					MainActivity.vibrate(30);
 
 					BulletUnit bullet = new BulletUnit(boat.getX(), boat.getY() + 15);
 					bullet.setAccelerationByAngle(boat.getRotation() - 15, 8);
 
 					mBullets.add(bullet);
 
 					lastFireTime = timeCounter;
 
 				}
 			}
 		}
 
 	}
 
 }
